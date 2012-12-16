package com.blastedstudios.drifters.server.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;
import com.blastedstudios.drifters.network.Generated.NetBeing.BeingType;
import com.blastedstudios.drifters.network.Generated.FactionType;
import com.blastedstudios.drifters.server.Server;
import com.blastedstudios.drifters.server.ai.Objective.ObjectiveEnum;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.GunFactory;

public class Faction {
	private static final Logger logger = Logger.getLogger(Faction.class.getCanonicalName());
	public static final int BEING_COUNT = Properties.getInt("server.ai.faction.beings.count");
	public final Map<String,ArtificialBeing> beings;
	private final List<String> namePrefixes;
	private final Server server;
	private final FactionType factionType;
	private float x, y;
	private final AIWorld aiWorld;
	private final List<Objective> objectives;
	private final List<StrategicPoint> capturedStrategicPoints;
	private static Random rand;
	
	public Faction(Server server, AIWorld aiWorld, FactionType factionType, List<String> namePrefixes,
			float x, float y){
		this.server = server;
		this.aiWorld = aiWorld;
		this.factionType = factionType;
		this.namePrefixes = namePrefixes;
		this.x = x;
		this.y = y;
		rand = new Random();
		objectives = new ArrayList<Objective>();
		beings = new HashMap<String,ArtificialBeing>();
		capturedStrategicPoints = new ArrayList<StrategicPoint>();
	}
	
	public ArtificialBeing create(){
		String name = namePrefixes.get(0) + "-" + beings.size(); 
		beings.put(name,new ArtificialBeing(server, aiWorld, name, 
				null, BeingType.ASSAULT, x+(rand.nextFloat()*4-2), y+rand.nextFloat(), 100, 100, 
				Arrays.asList(GunFactory.getAk47()), 
				0, factionType, 0, 1, 0));
		return beings.get(name);
	}

	public void update() {
		//if no strategic points and you aren't capturing, clear the list to capture
		if(capturedStrategicPoints.isEmpty() && (!objectives.isEmpty() && 
				!objectives.get(0).type.equals(ObjectiveEnum.CAPTURE)))
			objectives.clear();
		
		//capture first strat point, clear
		for(int i=0; i<objectives.size(); i++){
			Objective objective = objectives.get(i);
			for(StrategicPoint point : aiWorld.getStrategicPoints())
				if(objective.type.equals(ObjectiveEnum.CAPTURE) && objective.position.equals(point.getBase()))
					if(point.isCaptured(factionType) && !capturedStrategicPoints.contains(point)){
						capturedStrategicPoints.add(point);
						objectives.remove(i);
						logger.info("Captured strategic point " + point);
					}
		}
		
		//clear not owned strat points
		for(StrategicPoint point : aiWorld.getStrategicPoints())
			if(!point.isCaptured(factionType) && capturedStrategicPoints.contains(point))
				capturedStrategicPoints.remove(point);
		
		//add capture objective if empty
		if(capturedStrategicPoints.isEmpty() && objectives.isEmpty()){
			StrategicPoint closest = aiWorld.getClosestStrategicPoint(getAveragePosition()); 
			if(closest != null){
				objectives.add(new Objective(1, ObjectiveEnum.CAPTURE, closest.getBase(), null));
				logger.info("Faction " + factionType.name() + " objective now " + objectives.get(0));
			}else
				logger.fine("closest strat point null, likely odd average position: " + getAveragePosition());
		}else if(objectives.isEmpty()){
			StrategicPoint closest = aiWorld.getClosestStrategicPointNotOwned(getAveragePosition(), factionType); 
			if(closest != null){
				objectives.add(new Objective(1, ObjectiveEnum.CAPTURE, closest.getBase(), null));
				logger.info("Faction " + factionType.name() + " objective now " + objectives.get(0));
			}else
				logger.fine("closest strat point null, likely odd average position: " + getAveragePosition());
		}
		
		//check and ensure prioritized objectives are carried out according to score
		//i.e. if 100, 100% of soldier are focused on it.
		PriorityQueue<Objective> queue = new PriorityQueue<Objective>(objectives);
		while(!queue.isEmpty()){
			Objective objective = queue.poll();
			int totalTasked = 0;
			for(ArtificialBeing being : beings.values()){
				if(totalTasked / beings.size() > objective.score)
					break;
				if(being.getObjective() == null){
					Queue<Vector2> path = aiWorld.getPathToPoint(being.getPosition(), objective.position);
					being.setObjective(new Objective(1, objective.type, objective.position, path));
					++totalTasked;
				}
				if(being.getObjective().equals(objective))
					++totalTasked;
			}
		}
		
	}
	
	public Vector2 getAveragePosition(){
		Vector2 avePosition = new Vector2();
		for(ArtificialBeing being : beings.values())
			avePosition.add(being.getPosition());
		return avePosition.mul(1f/(float)beings.size());
	}
	
	public List<StrategicPoint> getCapturedStrategicPoints(){
		return capturedStrategicPoints;
	}
}
