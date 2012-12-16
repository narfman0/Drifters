package com.blastedstudios.drifters.client.ui.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.drifters.world.Being;

public class CharacterWindow extends Window {
	public CharacterWindow(final Skin skin, Being being){
		super("Character Info", skin);
		final Button exitButton = new TextButton("Exit", skin);
		exitButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				event.getListenerActor().getParent().remove();
			}
		});
		add(new Label("Name: " + being.getName(), skin));
		row();
		add(new Label("Cash: " + being.getCash(), skin));
		row();
		add(new Label("Level: " + being.getLevel(), skin));
		row();
		add(new Label("XP: " + being.getXp(), skin));
		row();
		add(new Label("HP: " + (int)being.getHp() + "/" + (int)being.getHp(), skin));
		row();
		add(exitButton);
		pack();
		setX(0);
		setY(Gdx.graphics.getHeight()/2 - getHeight()/2);
	}
}
