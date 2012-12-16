package com.blastedstudios.drifters.server;

import java.util.logging.Logger;

import com.badlogic.gdx.Game;
import com.blastedstudios.drifters.server.ai.AIThread;
import com.blastedstudios.drifters.server.network.ServerSocketThread;
import com.blastedstudios.drifters.server.ui.ServerScreen;
import com.blastedstudios.drifters.server.world.WorldManager;
import com.blastedstudios.drifters.util.Properties;

public class Server extends Game{
	private static Logger logger = Logger.getLogger(Server.class.getCanonicalName());
	public static String DRIFTERS_DIRECTORY = System.getProperty("user.home") + "/.driftersServer";
	public WorldManager world;
	public ServerSocketThread socketThread;
	public AccountThread accountThread;
	public AIThread aiThread;
	
	@Override public void create() {
		accountThread = new AccountThread(this);
		world = new WorldManager(this);
		socketThread = new ServerSocketThread(this);
		aiThread = new AIThread(this, world.getBeings());
		if(!Properties.getBool("server.headless"))
			setScreen(new ServerScreen(this));
		logger.info("Server creation complete");
	}
}
