package com.blastedstudios.drifters.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.drifters.util.parser.Parser;

public class ParserWindow extends Window {
	final TextField commandField;
	final Parser parser;
	
	public ParserWindow(final Skin skin, final Parser parser) {
		super("Parser", skin);
		this.parser = parser;
		commandField = new TextField("", skin);
		commandField.setMessageText("Enter command or chat");
		final Button sendButton = new TextButton("Send", skin);
		sendButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				execute();
				//event.getListenerActor().getParent().remove();
			}
		});
		add(commandField);
		add(sendButton);
		row();
		pack();
		setX(0);
		setY(0);
	}
	
	private void execute(){
		if(commandField.getText().startsWith("/"))
			parser.execute(commandField.getText().substring(1));
		else
			;//TODO send chat text
	}
}
