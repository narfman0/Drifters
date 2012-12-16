package com.blastedstudios.drifters.server.world;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.blastedstudios.drifters.client.WorldStepTimerTask;
import com.blastedstudios.drifters.network.Generated.GunShot;
import com.blastedstudios.drifters.world.Being;

public class ContactListener implements
		com.badlogic.gdx.physics.box2d.ContactListener {
	private WorldManager worldManager;
	
	public ContactListener(WorldManager worldManager){
		this.worldManager = worldManager;
	}

	@Override public void postSolve(Contact contact, ContactImpulse oldManifold) {
		Body gunshotBody = null;
		Being being = null;
		Fixture hit = null;
		if(contact.getFixtureA().getBody().getUserData() instanceof GunShot)
			gunshotBody = contact.getFixtureA().getBody();
		else if(contact.getFixtureB().getBody().getUserData() instanceof GunShot)
			gunshotBody = contact.getFixtureB().getBody();
		if(contact.getFixtureA().getBody().getUserData() instanceof Being){
			hit = contact.getFixtureA();
			being = (Being) contact.getFixtureA().getBody().getUserData();
		}else if(contact.getFixtureB().getBody().getUserData() instanceof Being){
			hit = contact.getFixtureA();
			being = (Being) contact.getFixtureB().getBody().getUserData();
		}
		if(gunshotBody != null){
			if(being != null && gunshotBody.getUserData() instanceof GunShot)
				worldManager.processHit(being, hit, (GunShot)gunshotBody.getUserData());
			gunshotBody.setUserData(WorldStepTimerTask.REMOVE_USER_DATA);
		}
	}

	@Override public void beginContact(Contact contact) {}
	@Override public void endContact(Contact contact) {}
	@Override public void preSolve(Contact contact, Manifold oldManifold) {}
}
