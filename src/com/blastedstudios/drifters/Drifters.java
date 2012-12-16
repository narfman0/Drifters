package com.blastedstudios.drifters;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.blastedstudios.drifters.client.ui.MainScreen;

public class Drifters extends Game {
	@Override public void create () {
		setScreen(new MainScreen(this));
	}
	
	public static void main (String[] argv) {
		new LwjglApplication(new Drifters(), "Drifters", 1024, 768, false);
	}
}
