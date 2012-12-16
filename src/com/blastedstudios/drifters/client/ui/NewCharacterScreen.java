package com.blastedstudios.drifters.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.drifters.Drifters;
import com.blastedstudios.drifters.network.Generated.NetAccount;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.network.Generated.NetBeing.BeingType;
import com.blastedstudios.drifters.network.Generated.FactionType;
import com.blastedstudios.drifters.ui.AbstractScreen;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;

public class NewCharacterScreen extends AbstractScreen<Drifters> implements Screen {
	final Label label;
	final NetAccount account;
	private EventListener failedListener, successListener;
	
	public NewCharacterScreen(final Drifters game, final NetAccount account) {
		super(game);
		this.account = account;
		label = new Label("", skin);
		final TextField nameField = new TextField("", skin);
		nameField.setMessageText("<name>");
		final List classList = new List(BeingType.values(), skin);
		final Button backButton = new TextButton("Back", skin);
		FactionType[] choosableTypes = {FactionType.GORILLAS, FactionType.STRANGERS,
				FactionType.ZEALOTS};
		final List factionList = new List(choosableTypes, skin);
		backButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				game.setScreen(new AccountScreen(game, account));
			}
		});
		final Button createButton = new TextButton("Create", skin);
		createButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				createButton.setTouchable(Touchable.disabled);
				BeingType type = BeingType.valueOf(classList.getSelection()); 
				FactionType factionType = FactionType.valueOf(factionList.getSelection());
				String name = nameField.getText(); 
				failedListener = new CharacterCreateFailed();
				successListener = new CharacterCreateSuccess();
				EventManager.addListener(EventEnum.CHARACTER_CREATE_FAILED, failedListener);
				EventManager.addListener(EventEnum.CHARACTER_CREATE_SUCCESS, successListener);
				EventManager.sendEvent(EventEnum.CHARACTER_CREATE_REQUEST, account.getEmail(), 
						name, type, factionType);
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
	
	private void removeListeners(){
		EventManager.removeListener(EventEnum.CHARACTER_CREATE_SUCCESS, successListener);
		EventManager.removeListener(EventEnum.CHARACTER_CREATE_FAILED, failedListener);
	}
	
	class CharacterCreateFailed implements EventListener{
		@Override public void handleEvent(EventEnum event, Object... data) {
			removeListeners();
			label.setText((String)data[0]);
		}
	}
	
	class CharacterCreateSuccess implements EventListener{
		@Override public void handleEvent(EventEnum event, Object... data) {
			removeListeners();
			final NetBeing being = (NetBeing) data[0];
			EventManager.addListener(EventEnum.CHARACTER_CHOSEN_COMPLETE, new EventListener() {
				@Override public void handleEvent(EventEnum event, Object... data) {
					EventManager.removeListener(EventEnum.CHARACTER_CHOSEN_COMPLETE, this);
					game.setScreen(new GameplayScreen(game, account, being));
				}
			});
			EventManager.sendEvent(EventEnum.CHARACTER_CHOSEN_INITIATE, being);
		}
	}
}
