package com.blastedstudios.drifters.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.blastedstudios.drifters.network.Generated.Race;
import com.blastedstudios.drifters.network.Generated.GunShot;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.network.Generated.NetBeingList;
import com.blastedstudios.drifters.network.Generated.PlayerReward;
import com.blastedstudios.drifters.network.Generated.ReloadRequest;
import com.blastedstudios.drifters.network.Generated.ShotDamage;
import com.blastedstudios.drifters.physics.PhysicsHelper;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.Being;

public class WorldManager implements EventListener {
	private static Logger logger = Logger.getLogger(WorldManager.class.getCanonicalName());
	private boolean debugDraw;
	private World world;
	private Box2DDebugRenderer renderer;
	private Map<String,Being> beings;
	private Being player;
	private ParticleEffect particleEffect;
	private Timer timer;
	private WeaponLockerManager weaponLockerManager;
	
	public WorldManager(NetBeing netBeing){
		weaponLockerManager = new WeaponLockerManager();
		beings = Collections.synchronizedMap(new HashMap<String, Being>());
		debugDraw = Properties.getBool("debug.draw");
		world = new World(new Vector2(0, -20), true);
		PhysicsHelper.createLoaderBodies(world, Gdx.files.internal("data/world/world.json"), "world");
		timer = new Timer("WorldManager", true);
		timer.schedule(new WorldStepTimerTask(world), 100, 33);

		player = new Being(world, netBeing.getName(),
				netBeing.getBeingClass(), netBeing.getPosX(), netBeing.getPosY(), 
				netBeing.getMaxHp(), netBeing.getHp(), netBeing.getGunsList(), 
				netBeing.getCurrentGun(), netBeing.getRace(), netBeing.getCash(),
				netBeing.getLevel(), netBeing.getXp());
		renderer = new Box2DDebugRenderer();
		particleEffect = new ParticleEffect();
		particleEffect.dispose();
		//TODO particleEffect.load(Gdx.files.internal("data/particles/blood.p"), Gdx.files.internal("data/textures"));
		EventManager.addListener(EventEnum.CHARACTER_POSITION_SERVER, this);
		EventManager.addListener(EventEnum.CHARACTER_RELOAD_SUCCESS, this);
		EventManager.addListener(EventEnum.CHARACTER_REWARD, this);
		EventManager.addListener(EventEnum.CHARACTER_SHOT, this);
		EventManager.addListener(EventEnum.GUN_SHOT, this);
	}

	public void render(SpriteBatch spriteBatch, Camera cam){
		if(debugDraw)
			synchronized(world){
				renderer.render(world, cam.combined);
			}
		player.render(world);
		//spriteBatch.begin();
		//particleEffect.draw(spriteBatch, 1/30f);
		//spriteBatch.end();
	}

	@Override public void handleEvent(EventEnum event, Object... data) {
		switch(event){
		case CHARACTER_POSITION_SERVER:
			handleCharacterPositionReceived((NetBeingList) data[0]);
			break;
		case CHARACTER_RELOAD_SUCCESS:
			ReloadRequest request = (ReloadRequest) data[0];
			if(request.getBeing().equals(player.getName()))
				player.reload();
			break;
		case CHARACTER_REWARD:
			PlayerReward reward = (PlayerReward) data[0];
			if(reward.getPlayer().equals(player.getName())){
				player.addCash(reward.getReward());
				String reason = reward.getReason().toString().replaceAll("_", " ").toLowerCase();
				logger.info("Player rewarded $" + reward.getReward() + " because: " + reason);
			}
			break;
		case CHARACTER_SHOT:
			handleShotDamage((ShotDamage) data[0]);
			break;
		case GUN_SHOT:
			final GunShot shot = (GunShot) data[0];
			timer.schedule(new TimerTask() {
				@Override public void run() {
					Race faction = getBeing(shot.getBeing()).getFactionType();
					PhysicsHelper.createBullet(world, shot, faction);
				}
			}, 0);
			break;
		
		default:
			break;
		}
	}

	private void handleShotDamage(final ShotDamage shotDamage) {
		if(beings.containsKey(shotDamage.getBeing())){
			final Being being = beings.get(shotDamage.getBeing());
			being.setHp(being.getHp() - shotDamage.getDamage());
			timer.schedule(new TimerTask() {
				@Override public void run() {
					synchronized (world) {
						if(being.getHp() <= 0)
							being.death(world, shotDamage);
					}
				//TODO add this in
				//particleEffect.setPosition(being.getPosition().x, being.getPosition().y);
				//particleEffect.start();
			}}, 0);
			logger.info("Being shot: " + being.getName() + " for dmg: " + shotDamage.getDamage());
		}
		
	}
	
	private void handleCharacterPositionReceived(NetBeingList beingList){
		for(final NetBeing netBeing : beingList.getBeingsList()){
			if(player.getName().equals(netBeing.getName()) || netBeing.getHp() <= 0)
				continue;
			final Vector2 position = new Vector2(netBeing.getPosX(), netBeing.getPosY());
			final Vector2 velocity = new Vector2(netBeing.getVelX(), netBeing.getVelY());
			if(!beings.containsKey(netBeing.getName())){
				final Being being = new Being(world, netBeing.getName(), 
						netBeing.getBeingClass(), position.x, position.y, netBeing.getMaxHp(),
						netBeing.getHp(), netBeing.getGunsList(), netBeing.getCurrentGun(),
						netBeing.getRace(), netBeing.getCash(), netBeing.getLevel(),
						netBeing.getXp());
				timer.schedule(new TimerTask() {
					@Override public void run() {
						synchronized(world){
							being.setVelocity(velocity);
							beings.put(netBeing.getName(), being);
						}
					}}, 0);
			}else{
				final Being being = beings.get(netBeing.getName());
				timer.schedule(new TimerTask() {
					@Override public void run() {
						synchronized(world){
							being.setPosition(position.x, position.y, 0);
							being.setVelocity(velocity);
						}
					}}, 0);
			}
		}
	}

	public void handleTouch(int x, int y) {
		double shotAngle = Math.atan2(Gdx.graphics.getHeight()/2 - y, x - Gdx.graphics.getWidth()/2);
		GunShot.Builder gunshot = GunShot.newBuilder();
		gunshot.setPosX(player.getPosition().x);
		gunshot.setPosY(player.getPosition().y);
		gunshot.setDirX((float)Math.cos(shotAngle));
		gunshot.setDirY((float)Math.sin(shotAngle));
		gunshot.setBeing(player.getName());
		gunshot.setGun(player.getEquippedGun());
		EventManager.sendEvent(EventEnum.GUN_SHOT_REQUEST, gunshot.build());
	}
	
	public Being getBeing(String name){
		if(player.getName().equals(name))
			return player;
		else if(beings.containsKey(name))
			return beings.get(name);
		logger.warning("Cannot find being with name " + name);
		return null;
	}

	public World getWorld() {
		return world;
	}
	
	public Being getPlayer(){
		return player;
	}
	
	public boolean isInWeaponLockerRange(){
		return weaponLockerManager.isWithinRange(player.getPosition());
	}
}
