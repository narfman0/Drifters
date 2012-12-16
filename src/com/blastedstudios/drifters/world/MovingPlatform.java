package com.blastedstudios.drifters.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.blastedstudios.drifters.network.Generated.FactionType;
import com.blastedstudios.drifters.physics.PhysicsHelper;

public class MovingPlatform {
	private Body platform;		
	private Vector2 pos = new Vector2();
	private Vector2 dir = new Vector2();
	private float dist = 0;
	private float maxDist = 0;		

	public MovingPlatform(World world, float x, float y, float width, float height, float dx, float dy, float maxDist) {
		platform = PhysicsHelper.createBox(world, BodyType.KinematicBody, width, height, 1, x,y, FactionType.NEUTRAL);			
		pos.x = x;
		pos.y = y;
		dir.x = dx;
		dir.y = dy;
		this.maxDist = maxDist;
		platform.getFixtureList().get(0).setUserData("p");
		platform.setUserData(this);
	}

	public void update(float deltaTime) {
		dist += dir.len() * deltaTime;
		if(dist > maxDist) {
			dir.mul(-1);
			dist = 0;
		}

		platform.setLinearVelocity(dir);			
	}
	
	public float getDist(){
		return dist;
	}
}
