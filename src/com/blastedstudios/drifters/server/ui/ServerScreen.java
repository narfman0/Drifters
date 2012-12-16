package com.blastedstudios.drifters.server.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.blastedstudios.drifters.server.Server;
import com.blastedstudios.drifters.ui.AbstractScreen;
import com.blastedstudios.drifters.ui.ParserWindow;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.parser.Parser;

public class ServerScreen extends AbstractScreen<Server> {
	private OrthographicCamera cam;
	private Box2DDebugRenderer renderer;
	private Parser parser;
	private ParserWindow parserWindow;
	private boolean isEnterPressedLast;

	public ServerScreen(Server server) {
		super(server);
		isEnterPressedLast = false;
		cam = new OrthographicCamera(28, 20);
		renderer = new Box2DDebugRenderer();
		parser = new Parser();
	}

	@Override public void render(float delta) {
		if(Gdx.input.isKeyPressed(Keys.LEFT))
			cam.position.x--;
		if(Gdx.input.isKeyPressed(Keys.RIGHT))
			cam.position.x++;
		if(Gdx.input.isKeyPressed(Keys.DOWN))
			cam.position.y--;
		if(Gdx.input.isKeyPressed(Keys.UP))
			cam.position.y++;
		if(Gdx.input.isKeyPressed(Keys.ENTER)){
			if(!isEnterPressedLast){
				isEnterPressedLast = true;
				if(parserWindow == null)
					stage.addActor(parserWindow = new ParserWindow(skin, parser));
				else{
					parserWindow.remove();
					parserWindow = null;
				}
			}
		}else
			isEnterPressedLast = false;
		cam.update();
		cam.apply(Gdx.gl10);
		
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		EventManager.sendEvent(EventEnum.WORLD_RENDER, renderer, cam.combined);
		synchronized(game.world.getWorld()){
			renderer.render(game.world.getWorld(), cam.combined);
		}
		game.aiThread.render(renderer, cam.combined);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

}
