package com.blastedstudios.drifters.server.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.blastedstudios.drifters.client.WorldStepTimerTask;
import com.blastedstudios.drifters.network.Generated.Gun;
import com.blastedstudios.drifters.network.Generated.Gun.WeaponType;
import com.blastedstudios.drifters.network.Generated.Race;
import com.blastedstudios.drifters.network.Generated.GunShot;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.network.Generated.PlayerReward;
import com.blastedstudios.drifters.network.Generated.PlayerReward.RewardReason;
import com.blastedstudios.drifters.network.Generated.ShotDamage;
import com.blastedstudios.drifters.network.Generated.ShotDamage.BodyPart;
import com.blastedstudios.drifters.network.Generated.WeaponLocker;
import com.blastedstudios.drifters.physics.PhysicsHelper;
import com.blastedstudios.drifters.server.Server;
import com.blastedstudios.drifters.server.ai.StrategicPoint;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.Being;

public class WorldManager implements EventListener {
	private static Logger logger = Logger.getLogger(WorldManager.class.getCanonicalName());
	public static int CHARACTER_REFRESH_RATE = Properties.getInt("network.character.update.rate"),
			WORLD_INIT_TIME = Properties.getInt("server.world.initialize.time");
	private World world;
	private Map<String, Being> beings;
	private Timer timer;
	private Server server;
	private ContactListener contactListener;
	private HashMap<StrategicPoint, WeaponLocker> weaponLockers;
	
	public WorldManager(Server server){
		this.server = server;
		contactListener = new ContactListener(this);
		weaponLockers = new HashMap<StrategicPoint, WeaponLocker>();
		EventManager.addListener(EventEnum.CHARACTER_CHOSEN_INITIATE, this);
		EventManager.addListener(EventEnum.CHARACTER_POSITION_CLIENT, this);
		EventManager.addListener(EventEnum.CHARACTER_RELOAD_REQUEST, this);
		EventManager.addListener(EventEnum.GUN_SHOT_REQUEST, this);
		EventManager.addListener(EventEnum.LOGOUT_COMPLETE, this);
		EventManager.addListener(EventEnum.WORLD_STRATEGIC_POINT_CAPTURED, this);
		EventManager.addListener(EventEnum.WORLD_STRATEGIC_POINT_LOST, this);
		timer = new Timer("WorldManager", true);
		timer.schedule(new TimerTask() {
			@Override public void run() {
				synchronized(world){
					world.step(.033f, 4, 4);
					Iterator<Body> iter = world.getBodies();
					for(Body next = iter.next(); iter.hasNext(); next = iter.next())
						if(next.getUserData() != null && next.getUserData().equals(WorldStepTimerTask.REMOVE_USER_DATA))
							world.destroyBody(next);
				}
			}
		}, WORLD_INIT_TIME, 33);
		timer.schedule(new TimerTask() {
			@Override public void run() {
				ArrayList<Being> updateList = new ArrayList<Being>();
				for(Being being : beings.values())
					if(!being.isDead())
						updateList.add(being);
				EventManager.sendEvent(EventEnum.CHARACTER_POSITION_SERVER, updateList);
			}
		}, WORLD_INIT_TIME, CHARACTER_REFRESH_RATE);
		beings = Collections.synchronizedMap(new HashMap<String, Being>());
		world = new World(new Vector2(0,-20), true);
		world.setContactListener(contactListener);
		PhysicsHelper.createLoaderBodies(world, Gdx.files.internal("data/world/world.json"), "world");
	}

	@Override public void handleEvent(EventEnum event, Object... data) {
		switch(event){
		case CHARACTER_POSITION_CLIENT:{
			final NetBeing netBeing = (NetBeing) data[0];
			final Being being;
			synchronized(beings){
				being = beings.get(netBeing.getName());
			}
			being.setMaxHP(netBeing.getMaxHp());
			being.setHp(netBeing.getHp());
			timer.schedule(new TimerTask() {
				@Override public void run() {
					synchronized (world) {
						being.setPosition(being.getPosition().x, being.getPosition().y, 0f);
						being.setVelocity(new Vector2(netBeing.getVelX(), netBeing.getVelY()));
					}
				}
			}, 0);
			break;
		}case CHARACTER_CHOSEN_INITIATE:{
			final NetBeing netBeing = (NetBeing) data[0];
			timer.schedule(new TimerTask() {
				@Override public void run() {
					Being constructedBeing = new Being(world, netBeing.getName(), 
							netBeing.getBeingClass(), netBeing.getPosX(), netBeing.getPosY(), 
							netBeing.getMaxHp(), netBeing.getHp(), netBeing.getGunsList(), 
							netBeing.getCurrentGun(), netBeing.getRace(), netBeing.getCash(),
							netBeing.getLevel(), netBeing.getXp());
					logger.info("Being chosen: " + netBeing.getName());
					synchronized(beings){
						beings.put(constructedBeing.getName(), constructedBeing);
					}
				}
			}, 0);
			break;
		}case CHARACTER_RELOAD_REQUEST:{
			synchronized (beings) {
				final Being being = beings.get((String)data[0]);
				if(!being.isReloading()){
					being.setReloading(true);
					timer.schedule(new TimerTask() {
						@Override public void run() {
							being.reload();
							EventManager.sendEvent(EventEnum.CHARACTER_RELOAD_SUCCESS, being.getName());
						}
					}, (int)being.getEquippedGun().getReloadSpeed()*1000);
				}
			}
			break;
		}case GUN_SHOT_REQUEST:{
			final GunShot gunshot = (GunShot) data[0];
			Being being;
			synchronized(beings){
				being = beings.get(gunshot.getBeing());
			}
			if(being.getEquippedGun().getType().equals(WeaponType.MELEE) ||
					(being.getEquippedGun().getCurrentRounds() > 0 && 
					(System.currentTimeMillis() - being.getLastFireTime())/1000 > being.getEquippedGun().getRateOfFire())){
				being.setLastFireTime(System.currentTimeMillis());
				being.setGuns(new ArrayList<Gun>(being.getGuns()));
				Gun gun = being.getGuns().remove(being.getCurrentGun());
				Gun.Builder gunBuilder = Gun.newBuilder(gun);
				gunBuilder.setCurrentRounds(gun.getCurrentRounds()-1);
				being.getGuns().add(being.getCurrentGun(), gunBuilder.build());
				timer.schedule(new TimerTask() {
					@Override public void run() {
						synchronized (world) {
							Race faction = getBeing(gunshot.getBeing()).getFactionType();
							PhysicsHelper.createBullet(world, gunshot, faction);
							EventManager.sendEvent(EventEnum.GUN_SHOT, gunshot);
						}
					}
				}, 0);
			}
			break;
		}case LOGOUT_COMPLETE:{
			NetBeing being = (NetBeing) data[0];
			if(beings.containsKey(being.getName())){
				synchronized(beings){
					beings.remove(being.getName());
				}
				//TODO send network message to remote players informing of player leave
			}
			break;
		}case WORLD_STRATEGIC_POINT_CAPTURED:{
			final StrategicPoint point = (StrategicPoint) data[0];
			timer.schedule(new TimerTask() {
				@Override public void run() {
					WeaponLocker.Builder lockerBuilder = WeaponLocker.newBuilder();
					lockerBuilder.setFaction(point.getFaction());
					lockerBuilder.setPosX(point.getCenter().x);
					lockerBuilder.setPosY(point.getCenter().y);
					WeaponLocker locker = lockerBuilder.build();
					weaponLockers.put(point, locker);
					EventManager.sendEvent(EventEnum.WORLD_WEAPON_LOCKER_ADDED, locker);
				}
			},0);
			break;
		}case WORLD_STRATEGIC_POINT_LOST:{
			final StrategicPoint point = (StrategicPoint) data[0];
			timer.schedule(new TimerTask() {
				@Override public void run() {
					WeaponLocker locker = weaponLockers.remove(point);
					EventManager.sendEvent(EventEnum.WORLD_WEAPON_LOCKER_REMOVED, locker);
				}
			},0);
			break;
		}
		default:
			break;
		}
	}
	
	public Being getBeing(String name){
		if(beings.containsKey(name))
			return beings.get(name);
		else if(server.aiThread.getAIBeings().containsKey(name))
			return server.aiThread.getAIBeings().get(name);
		logger.warning("Cannot find being with name " + name);
		return null;
	}
	
	/**
	 * @return player controlled beings
	 */
	public Map<String,Being> getBeings(){
		return beings;
	}
	
	/**
	 * @return player and ai controlled beings
	 */
	public List<Being> getAllBeings(){
		List<Being> allBeings = new ArrayList<Being>(beings.values());
		allBeings.addAll(server.aiThread.getAIBeings().values());
		return allBeings;
	}
	
	public World getWorld(){
		return world;
	}

	public void processHit(final Being being, Fixture bodyPart, GunShot gunshot) {
		ShotDamage.Builder shotDamageBuilder = ShotDamage.newBuilder();
		shotDamageBuilder.setBeing(being.getName());
		shotDamageBuilder.setDirX(gunshot.getDirX());
		shotDamageBuilder.setDirY(gunshot.getDirY());
		float bodypartDmgModifier = being.handleShotDamage(bodyPart, shotDamageBuilder);
		shotDamageBuilder.setDamage(gunshot.getGun().getDamage() * bodypartDmgModifier);
		being.setHp(being.getHp() - shotDamageBuilder.getDamage());
		final ShotDamage shotDamage = shotDamageBuilder.build();
		if(being.getHp() <= 0)
			timer.schedule(new TimerTask() {
				@Override public void run() {
					being.death(world, shotDamage);
				}
			}, 0);
		EventManager.sendEvent(EventEnum.CHARACTER_SHOT, shotDamage);
		PlayerReward.Builder rewardBuilder = PlayerReward.newBuilder();
		rewardBuilder.setPlayer(gunshot.getBeing());
		if(being.getHp() <= 0)
			rewardBuilder.setReason(RewardReason.ENEMY_KILLED);
		else
			rewardBuilder.setReason(RewardReason.ENEMY_HIT);
		rewardBuilder.setReward(getReward(being, gunshot, shotDamage));
		EventManager.sendEvent(EventEnum.CHARACTER_REWARD, rewardBuilder.build());
		logger.warning("Processed gunshot on being " + being.getName());
	}
	
	private static int getReward(Being being, GunShot gunshot, ShotDamage shotDamage){
		float reward = ((9f+(float)being.getLevel())/10f)*shotDamage.getDamage();
		if(shotDamage.getBodyPart() == BodyPart.HEAD)
			reward += 10;
		if(being.getHp()<=0)
			reward += 25;
		return (int)reward;
	}
	
	public Collection<WeaponLocker> getWeaponLockers(){
		return weaponLockers.values();
	}
}
