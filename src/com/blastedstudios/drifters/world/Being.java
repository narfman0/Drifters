package com.blastedstudios.drifters.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.blastedstudios.drifters.network.Generated.Gun;
import com.blastedstudios.drifters.network.Generated.Race;
import com.blastedstudios.drifters.network.Generated.ShotDamage;
import com.blastedstudios.drifters.network.Generated.NetBeing.Class;
import com.blastedstudios.drifters.network.Generated.ShotDamage.BodyPart;
import com.blastedstudios.drifters.physics.Ragdoll;
import com.blastedstudios.drifters.util.Properties;

public class Being {
	private static Logger logger = Logger.getLogger(Being.class.getCanonicalName());
	private final static float ACCELEROMETER_ROLL_THRESHOLD = Properties.getFloat("accelerometer.roll.threshold"),
			CHARACTER_IMPULSE_MAGNITUDE = Properties.getFloat("character.impulse.magnitude"),
			CHARACTER_JUMP_IMPULSE = Properties.getFloat("character.jump.impulse"),
			MAX_VELOCITY = Properties.getFloat("character.velocity.max");
	protected boolean jump = false, moveRight = false, moveLeft = false, dead = false, reloading = false;
	protected Ragdoll ragdoll;
	private long lastGroundTime = 0, lastFireTime = 0;
	private float stillTime = 0;
	protected String name;
	protected Class beingClass;
	protected Race race;
	protected float maxHP, hp;
	private List<Gun> guns;
	private int currentGun, cash, level, xp;
	private float lastGunHeading;
	private static final Map<BodyPart,Float> bodypartDmgMap = new HashMap<BodyPart, Float>();
	
	public Being(World world, String name, Class type, 
			float x, float y, float maxHP, float hp, List<Gun> guns, int currentGun,
			Race race, int cash, int level, int xp){
		this.name = name;
		this.beingClass = type;
		this.maxHP = maxHP;
		this.hp = hp;
		this.guns = guns;
		this.currentGun = currentGun;
		this.race = race;
		this.cash = cash;
		this.level = level;
		this.xp = xp;
		ragdoll = new Ragdoll(world, x, y, this);
		logger.info("Being " + name + " initialized at " + x + "," + y);
		if(bodypartDmgMap.isEmpty())
			for(BodyPart bodyType : BodyPart.values())
				bodypartDmgMap.put(bodyType, Properties.getFloat("character.bodypart." + 
						bodyType.name().toLowerCase() + ".dmgmultiplier"));
	}
	
	public void render(World world){
		if(dead)
			return;
		synchronized(world){
			//point gun at last looked at position
			ragdoll.aim(lastGunHeading);
			
			boolean grounded = isGrounded(world);
			if(grounded) {
				lastGroundTime = System.nanoTime();
			} else if(System.nanoTime() - lastGroundTime < 100000000)
				grounded = true;
			Vector2 vel = ragdoll.getLinearVelocity(), pos = ragdoll.getPosition();

			// cap max velocity on x		
			if(Math.abs(vel.x) > MAX_VELOCITY) {			
				vel.x = Math.signum(vel.x) * MAX_VELOCITY;
				ragdoll.setLinearVelocity(vel.x, vel.y);
			}

			// calculate stilltime & damp
			if(!moveLeft && !moveRight) {			
				stillTime += Gdx.graphics.getDeltaTime();
				ragdoll.setLinearVelocity(vel.x * 0.9f, vel.y);
			}
			else
				stillTime = 0;

			// disable friction while jumping
			if(!grounded)	
				ragdoll.setFriction(0f);			
			else {
				if(!moveLeft && !moveRight && stillTime > 0.2)
					ragdoll.setFriction(100f);
				else 
					ragdoll.setFriction(0.2f);
			}

			// jump, but only when grounded
			if(jump && grounded && vel.y < CHARACTER_JUMP_IMPULSE) {
				logger.fine("jump before: " + vel);
				ragdoll.setTransform(pos.x, pos.y + 0.01f, 0);
				ragdoll.applyLinearImpulse(0, CHARACTER_JUMP_IMPULSE-vel.y, pos.x, pos.y);			
				logger.fine("jump, " + vel);				
			}

			// apply left impulse, but only if max velocity is not reached yet
			if(moveLeft && vel.x > -MAX_VELOCITY ||
					Gdx.input.getRoll() < -ACCELEROMETER_ROLL_THRESHOLD)
				ragdoll.applyLinearImpulse(-CHARACTER_IMPULSE_MAGNITUDE, 0, pos.x, pos.y);
			// apply right impulse, but only if max velocity is not reached yet
			if(moveRight && vel.x < MAX_VELOCITY ||
					Gdx.input.getRoll() > ACCELEROMETER_ROLL_THRESHOLD)
				ragdoll.applyLinearImpulse(CHARACTER_IMPULSE_MAGNITUDE, 0, pos.x, pos.y);
			ragdoll.torsoBody.setAwake(true);
		}
	}

	private boolean isGrounded(World world) {				
		List<Contact> contactList = world.getContactList();
		for(int i = 0; i < contactList.size(); i++) {
			Contact contact = contactList.get(i);
			if(contact.isTouching() && ragdoll.standingOn(contact)) {				
				Vector2 pos = ragdoll.getPosition();
				WorldManifold manifold = contact.getWorldManifold();
				boolean below = true;
				for(int j = 0; j < manifold.getNumberOfContactPoints(); j++)
					below &= (manifold.getPoints()[j].y < pos.y - .1f);
				if(below)
					return true;			
				return false;
			}
		}
		return false;
	}
	
	public void reload(){
		guns = new ArrayList<Gun>(guns);
		Gun.Builder gun = Gun.newBuilder(guns.remove(currentGun));
		gun.setCurrentRounds(gun.getRoundsPerClip());
		guns.add(currentGun, gun.build());
		reloading = false;
	}

	/**
	 * This method both returns the damage multiplier and sets the body part
	 * that received the damage form the shot
	 * @param bodyPart which got shot
	 * @param shotDamage populate with body part that was shot
	 * @return dmg multiplier
	 */
	public float handleShotDamage(Fixture bodyPart, ShotDamage.Builder shotDamage){
		BodyPart bodyPartEnum = BodyPart.TORSO;
		if(ragdoll.headFixture == bodyPart)
			shotDamage.setBodyPart(bodyPartEnum = BodyPart.HEAD);
		if(ragdoll.torsoFixture == bodyPart)
			shotDamage.setBodyPart(bodyPartEnum = BodyPart.TORSO);
		if(ragdoll.lArmFixture == bodyPart)
			shotDamage.setBodyPart(bodyPartEnum = BodyPart.LARM);
		if(ragdoll.rArmFixture == bodyPart)
			shotDamage.setBodyPart(bodyPartEnum = BodyPart.RARM);
		if(ragdoll.lLegFixture == bodyPart)
			shotDamage.setBodyPart(bodyPartEnum = BodyPart.LLEG);
		if(ragdoll.rLegFixture == bodyPart)
			shotDamage.setBodyPart(bodyPartEnum = BodyPart.RLEG);
		return bodypartDmgMap.get(bodyPartEnum);
	}

	public void death(World world, ShotDamage shotDamage) {
		ragdoll.death(world, shotDamage);
		dead = true;
		reloading = false;
	}

	public void respawn(World world, float x, float y) {
		dead = false;
		hp = maxHP;
		synchronized(world){
			ragdoll.dispose(world);
			ragdoll = new Ragdoll(world, x, y, this);
		}
	}
	
	public boolean buy(Gun gun){
		boolean afford = cash >= gun.getCost();
		if(afford){
			cash -= gun.getCost();
			guns.add(gun);
		}
		return afford;
	}
	
	public void aim(float heading){
		lastGunHeading = heading;
	}
	
	public void setJump(boolean jump){
		this.jump = jump;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Class getType(){
		return beingClass;
	}
	
	public Vector2 getPosition(){
		return ragdoll.getPosition().cpy();
	}

	public void setPosition(float x, float y, float angle) {
		ragdoll.setTransform(x,y, angle);
	}

	public void setVelocity(Vector2 velocity) {
		ragdoll.setLinearVelocity(velocity.x, velocity.y);
	}

	public Vector2 getVelocity() {
		return ragdoll.getLinearVelocity().cpy();
	}

	@Override public String toString(){
		return "name:" + name + " type:" + beingClass + " loc=" + getPosition();
	}

	public boolean isMoveRight() {
		return moveRight;
	}

	public void setMoveRight(boolean moveRight) {
		this.moveRight = moveRight;
	}

	public boolean isMoveLeft() {
		return moveLeft;
	}

	public void setMoveLeft(boolean moveLeft) {
		this.moveLeft = moveLeft;
	}

	public float getMaxHP() {
		return maxHP;
	}

	public void setMaxHP(float maxHP) {
		this.maxHP = maxHP;
	}

	public float getHp() {
		return hp;
	}

	public void setHp(float hp) {
		this.hp = Math.min(Math.max(0, hp), maxHP);
	}

	public int getCurrentGun() {
		return currentGun;
	}

	public void setCurrentGun(int currentGun) {
		this.currentGun = currentGun;
	}
	
	public Gun getEquippedGun(){
		return guns.get(currentGun);
	}

	public List<Gun> getGuns() {
		return guns;
	}

	public void setGuns(List<Gun> guns) {
		this.guns = guns;
	}
	
	public boolean isDead(){
		return dead;
	}
	
	public Race getFactionType(){
		return race;
	}
	
	public void setFactionType(Race factionType){
		this.race = factionType;
	}

	public int getCash() {
		return cash;
	}

	public void setCash(int cash) {
		this.cash = cash;
	}
	
	public void addCash(int cash){
		this.cash += cash;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}
	
	public boolean isOwned(Fixture fixture){
		return ragdoll.isOwned(fixture);
	}

	public boolean isReloading() {
		return reloading;
	}

	public void setReloading(boolean reloading) {
		this.reloading = reloading;
	}
	
	public Ragdoll getRagdoll(){
		return ragdoll;
	}

	public long getLastFireTime() {
		return lastFireTime;
	}

	public void setLastFireTime(long lastFireTime) {
		this.lastFireTime = lastFireTime;
	}
}
