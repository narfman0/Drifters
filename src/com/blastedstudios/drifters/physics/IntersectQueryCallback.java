package com.blastedstudios.drifters.physics;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;

public class IntersectQueryCallback implements QueryCallback {
	public boolean called = false;
	public Fixture fixture;

	@Override public boolean reportFixture(Fixture fixture) {
		called = true;
		this.fixture = fixture;
		return false;
	}
}
