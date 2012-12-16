package com.blastedstudios.drifters.client;

import java.util.Iterator;
import java.util.TimerTask;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class WorldStepTimerTask extends TimerTask {
	private final World world;
	public static final String REMOVE_USER_DATA = "r";
	
	public WorldStepTimerTask(World world){
		this.world = world;
		world.setContactListener(new ContactListener());
	}

	@Override public void run() {
		synchronized(world){
			world.step(.033f, 4, 4);

			Iterator<Body> iter = world.getBodies();
			while(iter.hasNext()){
				Body next = iter.next(); 
				if(next.getUserData() != null && next.getUserData().equals(REMOVE_USER_DATA))
					world.destroyBody(next);
			}
		}
	}
}
