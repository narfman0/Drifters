package com.blastedstudios.drifters.physics;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.blastedstudios.drifters.network.Generated.ShotDamage;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.Being;

public class Ragdoll {
	private static float DENSITY = Properties.getFloat("character.ragdoll.density"),
			DEATH_IMPULSE = Properties.getFloat("character.impulse.death");
	public final Body torsoBody, headBody, rLegBody, lLegBody, rArmBody, lArmBody;
	public final Fixture torsoFixture, headFixture, rLegFixture, lLegFixture, 
		rArmFixture, lArmFixture;
	private Joint headJoint, rLegJoint, lLegJoint, rArmJoint, lArmJoint;
	private List<Body> bodies;
	private static final float torsoY = .27f, legX = .1f, legY = -.2f, armX = -.3f, 
			armY = .28f, headY = .6f; 
	
	public Ragdoll(World world, float x, float y, Being being){
		bodies = new ArrayList<Body>();
		short mask = PhysicsHelper.getMask(being.getFactionType());
		short cat = PhysicsHelper.getCategory(being.getFactionType());
		synchronized (world) {
			{
				PolygonShape torso = new PolygonShape();
				torso.setAsBox(.15f, .3f, new Vector2(0,torsoY),0);
				torsoBody = world.createBody(getDynamicBody());
				torsoFixture = torsoBody.createFixture(torso, DENSITY);
				Filter filter = torsoFixture.getFilterData();
				filter.maskBits = mask;
				filter.categoryBits = cat;
				torsoFixture.setFilterData(filter);
				torso.dispose();			
			}{
				PolygonShape lLeg = new PolygonShape();
				lLeg.setAsBox(.1f, .25f, new Vector2(-legX, legY), -.2f);
				lLegBody = world.createBody(getDynamicBody());
				lLegFixture = lLegBody.createFixture(lLeg, DENSITY);
				Filter filter = torsoFixture.getFilterData();
				filter.maskBits = mask;
				filter.categoryBits = cat;
				lLegFixture.setFilterData(filter);
				lLeg.dispose();
				lLegJoint = PhysicsHelper.addWeld(world, lLegBody, torsoBody, new Vector2(-.1f,-.1f));
			}{
				PolygonShape rLeg = new PolygonShape();
				rLeg.setAsBox(.1f, .25f, new Vector2(legX, legY), .2f);
				rLegBody = world.createBody(getDynamicBody());
				rLegFixture = rLegBody.createFixture(rLeg, DENSITY);
				Filter filter = torsoFixture.getFilterData();
				filter.maskBits = mask;
				filter.categoryBits = cat;
				rLegFixture.setFilterData(filter);
				rLeg.dispose();
				rLegJoint = PhysicsHelper.addWeld(world, rLegBody, torsoBody, new Vector2(.1f,-.1f));
			}{
				PolygonShape lArm = new PolygonShape();
				lArm.setAsBox(.1f, .22f, new Vector2(-armX, armY), -1.57f);
				lArmBody = world.createBody(getDynamicBody());
				lArmFixture = lArmBody.createFixture(lArm, DENSITY);
				Filter filter = torsoFixture.getFilterData();
				filter.maskBits = mask;
				filter.categoryBits = cat;
				lArmFixture.setFilterData(filter);
				lArm.dispose();
				lArmJoint = PhysicsHelper.addRevolute(world, lArmBody, torsoBody, new Vector2(-.1f,.25f));
			}{
				PolygonShape rArm = new PolygonShape();
				rArm.setAsBox(.1f, .22f, new Vector2(armX, armY), 1.57f);
				rArmBody = world.createBody(getDynamicBody());
				rArmFixture = rArmBody.createFixture(rArm, DENSITY);
				Filter filter = torsoFixture.getFilterData();
				filter.maskBits = mask;
				filter.categoryBits = cat;
				rArmFixture.setFilterData(filter);
				rArm.dispose();
				rArmJoint = PhysicsHelper.addRevolute(world, rArmBody, torsoBody, new Vector2(.1f,.25f));
			}{
				CircleShape head = new CircleShape();
				head.setRadius(.2f);
				head.setPosition(new Vector2(0, headY));
				headBody = world.createBody(getDynamicBody());
				headFixture = headBody.createFixture(head, DENSITY);
				Filter filter = torsoFixture.getFilterData();
				filter.maskBits = mask;
				filter.categoryBits = cat;
				headFixture.setFilterData(filter);
				head.dispose();
				headJoint = PhysicsHelper.addWeld(world, headBody, torsoBody, new Vector2(0,.45f));
			}
			torsoBody.setTransform(new Vector2(x,y), 0);
			headBody.setTransform(new Vector2(x,y), 0);
			rArmBody.setTransform(new Vector2(x,y), 0);
			lArmBody.setTransform(new Vector2(x,y), 0);
			lLegBody.setTransform(new Vector2(x,y), 0);
			rLegBody.setTransform(new Vector2(x,y), 0);
			torsoBody.setBullet(true);
			torsoBody.setFixedRotation(true);
			torsoBody.setUserData(being);
			bodies.add(torsoBody);
			bodies.add(headBody);
			bodies.add(lLegBody);
			bodies.add(rLegBody);
			bodies.add(lArmBody);
			bodies.add(rArmBody);
		}
	}
	
	private BodyDef getDynamicBody(){
		BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		return def;
	}
	
	public void setFriction(float friction){
		torsoFixture.setFriction(friction);
		headFixture.setFriction(friction);
		rArmFixture.setFriction(friction);
		lArmFixture.setFriction(friction);
		rLegFixture.setFriction(friction);
		lLegFixture.setFriction(friction);
	}

	public boolean standingOn(Contact contact){
		return contact.getFixtureA() == rLegFixture ||
				contact.getFixtureB() == rLegFixture ||
				contact.getFixtureA() == lLegFixture ||
				contact.getFixtureB() == lLegFixture;
	}

	public void aim(float heading) {
		/*TODO Vector2 origin = torsoBody.getPosition();
		Vector2 newLArm = new Vector2(-.3f, .28f).add(origin);
		Vector2 newRArm = new Vector2(.3f, .28f).add(origin);
		Vector2 headingMod = new Vector2((float)Math.cos(heading), (float)Math.sin(heading)).mul(.1f);
		newLArm.add(headingMod);
		newRArm.add(headingMod);
		lArmBody.setTransform(newLArm, heading);
		rArmBody.setTransform(newRArm, heading);*/
	}

	public void death(World world, ShotDamage shotDamage) {
		torsoBody.setFixedRotation(false);
		torsoBody.setBullet(false);
		Vector2 dir = new Vector2(shotDamage.getDirX(), shotDamage.getDirY());
		switch(shotDamage.getBodyPart()){
		case HEAD:
			headBody.applyLinearImpulse(dir.mul(DEATH_IMPULSE), headBody.getPosition());
			world.destroyJoint(headJoint);
			headJoint = null;
			break;
		case LARM:
			lArmBody.applyLinearImpulse(dir.mul(DEATH_IMPULSE), lArmBody.getPosition());
			world.destroyJoint(lArmJoint);
			lArmJoint = null;
			break;
		case RARM:
			rArmBody.applyLinearImpulse(dir.mul(DEATH_IMPULSE), rArmBody.getPosition());
			world.destroyJoint(rArmJoint);
			rArmJoint = null;
			break;
		case LLEG:
			lLegBody.applyLinearImpulse(dir.mul(DEATH_IMPULSE), lLegBody.getPosition());
			world.destroyJoint(lLegJoint);
			lLegJoint = null;
			break;
		case RLEG:
			rLegBody.applyLinearImpulse(dir.mul(DEATH_IMPULSE), rLegBody.getPosition());
			world.destroyJoint(rLegJoint);
			rLegJoint = null;
			break;
		default:
			torsoBody.applyLinearImpulse(dir.mul(DEATH_IMPULSE), torsoBody.getPosition());
			break;
		}
	}
	
	public Vector2 getPosition(){
		return torsoBody.getPosition();
	}
	
	public Vector2 getLinearVelocity(){
		return torsoBody.getLinearVelocity();
	}

	public void setLinearVelocity(float x, float y) {
		torsoBody.setLinearVelocity(x, y);
		//for(Body body : bodies)
		//	body.setLinearVelocity(x, y);
	}

	public void applyLinearImpulse(float i, float j, float x, float y) {
		i /= bodies.size();
		j /= bodies.size();
		for(Body body : bodies)
			body.applyLinearImpulse(i,j,x,y);
	}
	
	/*
	 torsoY = .27f, legX = .1f, legY = -.2f, armX = -.3f, armY = .28f, headY = .6f
	 */

	public void setTransform(float x, float y, float angle) {
		torsoBody.setTransform(x, y + torsoY, angle);
		/*headBody.setTransform(x, y + headY, angle);
		lArmBody.setTransform(x-armX, y+armY, angle);
		rArmBody.setTransform(x+armX, y+armY, angle);
		lLegBody.setTransform(x-legX, y+legY, angle);
		rLegBody.setTransform(x+legX, y+legY, angle);*/
	}
	
	public boolean isOwned(Fixture fixture){
		return fixture.equals(torsoFixture) || fixture.equals(headFixture) || fixture.equals(rLegFixture) ||
				fixture.equals(lLegFixture) || fixture.equals(rArmFixture) || fixture.equals(lArmFixture);
	}

	public void dispose(World world) {
		disposeJoints(world);
		disposeBodies(world);
	}

	private void disposeBodies(World world) {
		for(Body body : bodies)
			world.destroyBody(body);
	}

	private void disposeJoints(World world) {
		if(headJoint != null){
			world.destroyJoint(headJoint);
			headJoint = null;
		}
		if(lArmJoint != null){
			world.destroyJoint(lArmJoint);
			lArmJoint = null;
		}
		if(rArmJoint != null){
			world.destroyJoint(rArmJoint);
			rArmJoint = null;
		}
		if(lLegJoint != null){
			world.destroyJoint(lLegJoint);
			lLegJoint = null;
		}
		if(rLegJoint != null){
			world.destroyJoint(rLegJoint);
			rLegJoint = null;
		}
	}
}
