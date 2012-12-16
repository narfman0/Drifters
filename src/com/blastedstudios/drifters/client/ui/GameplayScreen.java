package com.blastedstudios.drifters.client.ui;

import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.blastedstudios.drifters.Drifters;
import com.blastedstudios.drifters.client.WorldManager;
import com.blastedstudios.drifters.client.ui.gameplay.CharacterWindow;
import com.blastedstudios.drifters.client.ui.gameplay.InventoryWindow;
import com.blastedstudios.drifters.client.ui.gameplay.WeaponLockerWindow;
import com.blastedstudios.drifters.network.Generated.NetAccount;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.ui.AbstractScreen;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;
import com.blastedstudios.drifters.world.MovingPlatform;

public class GameplayScreen extends AbstractScreen<Drifters> implements EventListener {
	private static Logger logger = Logger.getLogger(GameplayScreen.class.getCanonicalName());
	private OrthographicCamera cam;
	private Array<MovingPlatform> platforms = new Array<MovingPlatform>();
	private WorldManager worldManager;
	private SpriteBatch spriteBatch;
	private Window characterWindow, weaponLockerWindow, inventoryWindow;
	
	public GameplayScreen(Drifters game, NetAccount account, NetBeing netBeing){
		super(game);	
		worldManager = new WorldManager(netBeing, account);
		cam = new OrthographicCamera(28, 20);
		spriteBatch = new SpriteBatch();
		EventManager.addListener(EventEnum.LOGOUT_COMPLETE, this);
	}

	@Override public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		cam.position.set(worldManager.getPlayer().getPosition().x, worldManager.getPlayer().getPosition().y, 0);
		cam.update();
		cam.apply(Gdx.gl10);
		
		worldManager.render(spriteBatch, cam);
		worldManager.getPlayer().setMoveLeft(Gdx.input.isKeyPressed(Keys.A));
		worldManager.getPlayer().setMoveRight(Gdx.input.isKeyPressed(Keys.D));
		if(Gdx.input.isKeyPressed(Keys.R)){
			EventManager.sendEvent(EventEnum.CHARACTER_RELOAD_REQUEST, worldManager.getPlayer().getName());
			worldManager.getPlayer().setReloading(true);
		}
		worldManager.getPlayer().setJump(Gdx.input.isKeyPressed(Keys.W));
		for(int i=0; i<10; i++)
			if(Gdx.input.isKeyPressed(Keys.NUM_1 + i))
				if(worldManager.getPlayer().getGuns().size() > i && i != worldManager.getPlayer().getCurrentGun()){
					worldManager.getPlayer().setCurrentGun(i);
					logger.info("New gun selected: " + worldManager.getPlayer().getGuns().get(i));
				}
		if(Gdx.input.isKeyPressed(Keys.ESCAPE)){
			EventManager.sendEvent(EventEnum.LOGOUT_INITIATE);
			game.setScreen(new MainScreen(game));
		}
		if(Gdx.input.isKeyPressed(Keys.B) && worldManager.isInWeaponLockerRange()){
			if(weaponLockerWindow == null){
				weaponLockerWindow = new WeaponLockerWindow(skin);
				stage.addActor(weaponLockerWindow);
			}else{
				weaponLockerWindow.remove();
				weaponLockerWindow = null;
			}
		}
		if(Gdx.input.isKeyPressed(Keys.C)){
			if(characterWindow == null){
				characterWindow = new CharacterWindow(skin, worldManager.getPlayer());
				stage.addActor(characterWindow);
			}else{
				characterWindow.remove();
				characterWindow = null;
			}
		}
		if(Gdx.input.isKeyPressed(Keys.I)){
			if(inventoryWindow == null){
				inventoryWindow = new InventoryWindow(skin, worldManager.getPlayer());
				stage.addActor(inventoryWindow);
			}else{
				inventoryWindow.remove();
				inventoryWindow = null;
			}
		}
		if(Gdx.input.isTouched()){
			//TODO only if android device do this?
			//if(x < Gdx.graphics.getWidth()*.7f && y > Gdx.graphics.getHeight()*.7f){
			//	player.setJump(true);
			//}else{
			int x = Gdx.input.getX(), y = Gdx.input.getY();
			worldManager.handleTouch(x,y);
			cam.unproject(new Vector3(x, y, 0));
		}
		if(System.currentTimeMillis() % 5 == 0)
			EventManager.sendEvent(EventEnum.CHARACTER_POSITION_CLIENT, worldManager.getPlayer());
 
		// update platforms
		for(int i = 0; i < platforms.size; i++) {
			MovingPlatform platform = platforms.get(i);
			platform.update(Math.max(1/30.0f, Gdx.graphics.getDeltaTime()));
		}
 
		cam.project(new Vector3(worldManager.getPlayer().getPosition().x, worldManager.getPlayer().getPosition().y, 0));
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}
 
	@Override public void handleEvent(EventEnum event, Object... data) {
		switch(event){
		case LOGOUT_COMPLETE:
			EventManager.removeListener(EventEnum.LOGOUT_COMPLETE, this);
			game.setScreen(new MainScreen(game));
		default:
			break;
		}
	}
}
