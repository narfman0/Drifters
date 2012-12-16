package com.blastedstudios.drifters.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.drifters.Drifters;
import com.blastedstudios.drifters.network.Generated.Race;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.network.Generated.NetBeing.Class;
import com.blastedstudios.drifters.ui.AbstractScreen;

public class NewCharacterScreen extends AbstractScreen<Drifters> implements Screen {
	final Label label;
	
	public NewCharacterScreen(final Drifters game) {
		super(game);
		label = new Label("", skin);
		final TextField nameField = new TextField("", skin);
		nameField.setMessageText("<name>");
		final List classList = new List(Class.values(), skin);
		final Button backButton = new TextButton("Back", skin);
		Race[] choosableTypes = {Race.HUMAN, Race.ELF, Race.DWARF};
		final List factionList = new List(choosableTypes, skin);
		backButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				game.setScreen(new AccountScreen(game));
			}
		});
		final Button createButton = new TextButton("Create", skin);
		createButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				Class type = Class.valueOf(classList.getSelection()); 
				Race factionType = Race.valueOf(factionList.getSelection());
				String name = nameField.getText(); 
				NetBeing.Builder netBeing = NetBeing.newBuilder();
				netBeing.setBeingClass(type);
				netBeing.setRace(factionType);
				netBeing.setName(name);
				game.setScreen(new GameplayScreen(game, netBeing.build()));
			}
		});
		Window window = new Window("New Character", skin);
		window.add(nameField);
		window.row();
		window.add(classList);
		window.row();
		window.add(factionList);
		window.row();
		window.add(createButton);
		window.row();
		window.add(backButton);
		window.pack();
		window.add(label);
		window.pack();
		window.setX(Gdx.graphics.getWidth()/2 - window.getWidth()/2);
		window.setY(Gdx.graphics.getHeight()/2 - window.getHeight()/2);
		stage.addActor(window);
	}
}
