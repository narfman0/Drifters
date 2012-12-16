package com.blastedstudios.drifters.physics;

import java.util.ArrayList;
import java.util.List;

import aurelienribon.bodyeditor.BodyEditorLoader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.blastedstudios.drifters.network.Generated.GunShot;
import com.blastedstudios.drifters.network.Generated.Race;
import com.blastedstudios.drifters.util.Properties;

public class PhysicsHelper {
	private static final short 
		CAT_SCENERY		= 0x0001,
		CAT_GORRILAS	= 0x0002,
		CAT_ZEALOTS		= 0x0004,
		CAT_STRANGERS	= 0x0008,
		CAT_NOTHING		= 0x0010,
		MASK_GORRILAS	= (short)-1 & ~CAT_GORRILAS,
		MASK_ZEALOTS	= (short)-1 & ~CAT_ZEALOTS,
		MASK_STRANGERS	= (short)-1 & ~CAT_STRANGERS,
		MASK_NOTHING	= CAT_SCENERY,
		MASK_SCENERY	= -1;
	private static final float DENSITY_DEFAULT = Properties.getFloat("world.loader.density"),
			BULLET_DENSITY = Properties.getFloat("gun.bullet.density"),
			BULLET_RADIUS = Properties.getFloat("gun.bullet.radius"),
			FRICTION_DEFAULT = Properties.getFloat("world.loader.friction"),
			RESITUTION_DEFAULT = Properties.getFloat("world.loader.restitution");
	public static final float LOADER_WIDTH = Properties.getFloat("world.loader.width");

	public static Body createBox(World world, BodyType type, float width, 
			float height, float density, float x, float y, Race faction) {
		synchronized(world){
			BodyDef def = new BodyDef();
			def.type = type;
			Body box = world.createBody(def);
			PolygonShape poly = new PolygonShape();
			poly.setAsBox(width, height);
			Fixture fixture = box.createFixture(poly, density);
			fixture.getFilterData().categoryBits = getCategory(faction);
			fixture.getFilterData().maskBits = getMask(faction);
			poly.dispose();
			box.setTransform(x, y, 0);
			return box;
		}
	}	
 
	public static Body createEdge(World world, BodyType type, float x1, float y1, 
			float x2, float y2, float density, Race faction) {
		synchronized(world){
			BodyDef def = new BodyDef();
			def.type = type;
			Body box = world.createBody(def);
			EdgeShape boxShape = new EdgeShape();		
			boxShape.set(new Vector2(0, 0), new Vector2(x2 - x1, y2 - y1));
			Fixture fixture = box.createFixture(boxShape, density);
			fixture.getFilterData().categoryBits = getCategory(faction);
			fixture.getFilterData().maskBits = getMask(faction);
			box.setTransform(x1, y1, 0);
			boxShape.dispose();
			return box;
		}
	}
 
	public static Body createCircle(World world, BodyType type, float radius, 
			float density, Race faction) {
		synchronized(world){
			BodyDef def = new BodyDef();
			def.type = type;
			Body box = world.createBody(def);
			CircleShape circleShape = new CircleShape();
			circleShape.setRadius(radius);
			Fixture fixture = box.createFixture(circleShape, density);
			fixture.getFilterData().categoryBits = getCategory(faction);
			fixture.getFilterData().maskBits = getMask(faction);
			circleShape.dispose();
			return box;
		}
	}

	public static Body createLoaderBody(World world, FileHandle handle, 
			BodyType type, float width, String name){
		synchronized(world){
			BodyEditorLoader loader = new BodyEditorLoader(handle);
			// 1. Create a BodyDef, as usual.
			BodyDef bd = new BodyDef();
			bd.type = type;

			// 2. Create a FixtureDef, as usual.
			FixtureDef fd = new FixtureDef();
			fd.density = DENSITY_DEFAULT;
			fd.friction = FRICTION_DEFAULT;
			fd.restitution = RESITUTION_DEFAULT;
			fd.filter.categoryBits = CAT_SCENERY;
			fd.filter.maskBits = MASK_SCENERY;

			// 3. Create a Body, as usual.
			Body body = world.createBody(bd);

			// 4. Create the body fixture automatically by using the loader.
			loader.attachFixture(body, name, fd, width);
			return body;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Body> createLoaderBodies(World world, FileHandle handle, String name){
		ArrayList<Body> bodies = new ArrayList<Body>();
		JsonReader reader = new JsonReader();
		OrderedMap<String, Object> object = (OrderedMap<String, Object>) reader.parse(handle);
		for(Object bodyKey : (Array<Object>)object.get("rigidBodies")){
			OrderedMap<String,Object> bodyMap = (OrderedMap<String, Object>) bodyKey;
			if(bodyMap.get("name").equals(name))
				bodies.add(createLoaderBody(world, handle, BodyType.StaticBody, LOADER_WIDTH, (String)bodyMap.get("name")));
		}
		return bodies;
	}
	
	public static Body createBullet(World world, GunShot gunshot, Race faction){
		Body gunshotBody = PhysicsHelper.createCircle(world, BodyType.DynamicBody, 
				BULLET_RADIUS, BULLET_DENSITY, faction);
		synchronized(world){
			Vector2 direction = new Vector2(gunshot.getDirX(), gunshot.getDirY()),
					position = new Vector2(gunshot.getPosX(), gunshot.getPosY()).add(direction),
					impulse = direction.tmp().mul(gunshot.getGun().getMuzzleVelocity());
			gunshotBody.setTransform(position, 0);
			gunshotBody.applyLinearImpulse(impulse,position);
			gunshotBody.setBullet(true);
			gunshotBody.setUserData(gunshot);
		}
		return gunshotBody;
	}
	
	public static Joint addRevolute(World world, Body bodyA, Body bodyB, Vector2 anchor){
		RevoluteJointDef joint = new RevoluteJointDef();
		joint.initialize(bodyA, bodyB, anchor);
		return world.createJoint(joint);
	}
	
	public static Joint addWeld(World world, Body bodyA, Body bodyB, Vector2 anchor){
		WeldJointDef joint = new WeldJointDef();
		joint.initialize(bodyA, bodyB, anchor);
		return world.createJoint(joint);
	}

	public static short getMask(Race factionType){
		if(factionType == null)
			return MASK_NOTHING;
		switch(factionType){
		case HUMAN:
			return MASK_GORRILAS;
		case ELF:
			return MASK_STRANGERS;
		case DWARF:
			return MASK_ZEALOTS;
		default:
			return MASK_SCENERY;
		}
	}
	
	public static short getCategory(Race factionType){
		if(factionType == null)
			return CAT_NOTHING;
		switch(factionType){
		case HUMAN:
			return CAT_GORRILAS;
		case ELF:
			return CAT_STRANGERS;
		case DWARF:
			return CAT_ZEALOTS;
		default:
			return CAT_SCENERY;
		}
	}
}
