package com.blastedstudios.drifters.server.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.blastedstudios.drifters.network.Generated.FactionType;
import com.blastedstudios.drifters.server.Server;
import com.blastedstudios.drifters.server.world.WorldManager;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.Being;

public class AIThread {
	private static final int AI_DELAY = Properties.getInt("server.ai.start.delay"), 
			AI_FREQUENCY = Properties.getInt("server.ai.update.frequency"); 
	private static final float CAPTURE_RATE = Properties.getFloat("server.strategicpoint.capture.rate");
	private final HashMap<String,ArtificialBeing> aiBeings;
	private final Map<String, Being> beings;
	private final Faction gorillas, zealots;
	private final Timer timer;
	private final Server server;
	private final AIWorld aiWorld;
	
	public AIThread(Server server, Map<String, Being> beings){
		this.server = server;
		this.beings = beings;
		timer = new Timer("AI", true);
		timer.schedule(new TimerTask() {
			@Override public void run() {
				update();
			}
		}, AI_DELAY, AI_FREQUENCY);

		timer.schedule(new TimerTask() {
			@Override public void run() {
				ArrayList<Being> updateList = new ArrayList<Being>();
				for(Being being : aiBeings.values())
					if(!being.isDead())
						updateList.add(being);
				EventManager.sendEvent(EventEnum.CHARACTER_POSITION_SERVER, updateList);
			}
		}, AI_DELAY, WorldManager.CHARACTER_REFRESH_RATE);
		
		aiWorld = new AIWorld(server.world.getWorld());
		gorillas = new Faction(server, aiWorld, FactionType.GORILLAS, Arrays.asList("Peon"), -100, 2);
		zealots = new Faction(server, aiWorld, FactionType.ZEALOTS, Arrays.asList("Private"), 100, 2);
		aiBeings = new HashMap<String,ArtificialBeing>();
	}

	private void update() {
		while(gorillas.beings.size() < Faction.BEING_COUNT){
			ArtificialBeing being = gorillas.create();
			aiBeings.put(being.getName(), being);
			beings.put(being.getName(), being);
		}
		while(zealots.beings.size() < Faction.BEING_COUNT){
			ArtificialBeing being = zealots.create();
			aiBeings.put(being.getName(), being);
			beings.put(being.getName(), being);
		}
		
		for(Being being : aiBeings.values())
			being.render(server.world.getWorld());
		
		for(Being being : beings.values())
			for(StrategicPoint point : aiWorld.getStrategicPoints())
				if(point.contains(being.getPosition().x, being.getPosition().y)){
					point.capture(being.getFactionType(), CAPTURE_RATE);
					if(point.isCaptured(being.getFactionType()))
						being.setHp(being.getHp()+1);
				}
		
		gorillas.update();
		zealots.update();
	}
	
	public void render(Box2DDebugRenderer renderer, Matrix4 proj){
		aiWorld.render(renderer, proj);
	}
	
	public Map<String, ArtificialBeing> getAIBeings(){
		return aiBeings;
	}
	
	/**
	 * @return sorted list of closest bases
	 */
	public List<Vector2> getClosestBases(Vector2 origin, FactionType factionType){
		ArrayList<Vector2> bases = new ArrayList<Vector2>();
		Faction faction = null;
		switch(factionType){
		case GORILLAS:
			faction = gorillas;
			break;
		case ZEALOTS:
			faction = zealots;
			break;
		default:
		}
		for(StrategicPoint point : faction.getCapturedStrategicPoints()){
			if(bases.isEmpty()){
				bases.add(point.getCenter());
				continue;
			}
			for(int i=0; i<bases.size(); i++)
				if(origin.dst2(bases.get(i)) < origin.dst2(point.getCenter()) || i==bases.size()-1){
					bases.add(i, point.getCenter());
					break;
				}
		}
		return bases;
	}
}
