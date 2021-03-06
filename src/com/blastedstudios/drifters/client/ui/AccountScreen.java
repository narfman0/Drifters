package com.blastedstudios.drifters.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.drifters.Drifters;
import com.blastedstudios.drifters.client.SaveManager;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.ui.AbstractScreen;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;

public class AccountScreen extends AbstractScreen<Drifters> implements Screen {
	public AccountScreen(final Drifters game) {
		super(game);
		final Button backButton = new TextButton("Back", skin);
		backButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				EventManager.sendEvent(EventEnum.LOGOUT_INITIATE);
				game.setScreen(new MainScreen(game));
			}
		});
		final Button newButton = new TextButton("Create New...", skin);
		newButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				game.setScreen(new NewCharacterScreen(game));
			}
		});
		Window window = new Window("Account", skin);
		window.add(newButton);
		window.row();
		for(final NetBeing being : SaveManager.load()){
			final Button beingButton = new TextButton(being.getName(), skin);
			beingButton.addListener(new ClickListener() {
				@Override public void clicked(InputEvent event, float x, float y) {
					game.setScreen(new GameplayScreen(game, being));
				}
			});
			window.add(beingButton);
			window.row();
		}
		window.add(backButton);
		window.pack();
		window.setX(Gdx.graphics.getWidth()/2 - window.getWidth()/2);
		window.setY(Gdx.graphics.getHeight()/2 - window.getHeight()/2);
		stage.addActor(window);
	}
}
