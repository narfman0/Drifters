package com.blastedstudios.drifters.client;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.blastedstudios.drifters.network.Generated.GunShot;

public class ContactListener implements
		com.badlogic.gdx.physics.box2d.ContactListener {

	@Override public void postSolve(Contact contact, ContactImpulse oldManifold) {
		Body gunshotBody = null;
		if(contact.getFixtureA().getBody().getUserData() instanceof GunShot)
			gunshotBody = contact.getFixtureA().getBody();
		else if(contact.getFixtureB().getBody().getUserData() instanceof GunShot)
			gunshotBody = contact.getFixtureB().getBody();
		if(gunshotBody != null)
			gunshotBody.setUserData(WorldStepTimerTask.REMOVE_USER_DATA);
	}

	@Override public void beginContact(Contact contact) {}
	@Override public void endContact(Contact contact) {}
	@Override public void preSolve(Contact contact, Manifold arg1) {}
}
