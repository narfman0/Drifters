package com.blastedstudios.drifters.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.drifters.Drifters;
import com.blastedstudios.drifters.ui.AbstractScreen;

public class MainScreen extends AbstractScreen<Drifters> {
	public MainScreen(final Drifters game){
		super(game);
		final Button localButton = new TextButton("Play Local", skin);
		final Button exitButton = new TextButton("Exit", skin);
		localButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				game.setScreen(new AccountScreen(game));
			}
		});
		exitButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});
		Window window = new Window("Drifters", skin);
		window.add(localButton);
		window.row();
		window.add(exitButton);
		window.pack();
		window.setX(Gdx.graphics.getWidth()/2 - window.getWidth()/2);
		window.setY(Gdx.graphics.getHeight()/2 - window.getHeight()/2);
		stage.addActor(window);
	}
}
