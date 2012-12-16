package com.blastedstudios.drifters.server.ai;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.ClosestFirstIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.blastedstudios.drifters.network.Generated.FactionType;
import com.blastedstudios.drifters.physics.PhysicsHelper;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;
import com.blastedstudios.drifters.util.Properties;

public class AIWorld implements EventListener {
	private static final Logger logger = Logger.getLogger(AIWorld.class.getCanonicalName());
	private static boolean nodesVisible = Properties.getBool("server.world.pathing.nodes.visible"),
		edgesVisible = Properties.getBool("server.world.pathing.edges.visible");
	private List<Body> graphVisibleBodies;
	private final World world;
	private final SimpleWeightedGraph<Vector2, DefaultWeightedEdge> movementGraph;
	private final List<StrategicPoint> strategicPoints;
	
	public AIWorld(World gameWorld){
		synchronized (gameWorld) {
			movementGraph = GraphGenerator.generateGraph(gameWorld);
		}
		logger.info("Initialized graph with " +movementGraph.edgeSet().size() + " edges and " + 
				movementGraph.vertexSet().size() + " vertices");
		world = new World(new Vector2(), true);
		PhysicsHelper.createLoaderBodies(world, Gdx.files.internal("data/world/world.json"), "strategicPoints");
		strategicPoints = GraphGenerator.generateStrategicPoints(world);
		logger.info("Loaded ai world and found " + strategicPoints.size() + " strategic points");
		createGraphVisible();
		EventManager.addListener(EventEnum.PARSER_AI_GRAPH_VISIBLE, this);
	}
	
	/**
	 * @return closest valid runtime node (not to be used while building world)
	 */
	private Vector2 getClosestNode(float x, float y){
		if(movementGraph.containsVertex(new Vector2(x,y)))
			return new Vector2(x,y);
		for(float dst=1; dst<100; dst++)
			for(float angle=0; angle<Math.PI*2; angle+=Math.PI/12){
				int xtmp = (int) (x + Math.cos(angle)*dst);
				int ytmp = (int) (y + Math.sin(angle)*dst);
				if(movementGraph.containsVertex(new Vector2(xtmp,ytmp)))
					return new Vector2(xtmp,ytmp);
			}
		return new Vector2(x,y);
	}
	
	public StrategicPoint getClosestStrategicPoint(Vector2 origin){
		return getClosestStrategicPointNotOwned(origin, null);
	}

	public StrategicPoint getClosestStrategicPointNotOwned(
			Vector2 origin, FactionType factionType) {
		//vector2s are rounded because of hashcode graph trick, so must round
		Vector2 originRounded = getClosestNode(Math.round(origin.x),Math.round(origin.y));
		try{
			ClosestFirstIterator<Vector2, DefaultWeightedEdge> iter = 
					new ClosestFirstIterator<Vector2, DefaultWeightedEdge>(movementGraph, originRounded);
			for(Vector2 loc = iter.next(); iter.hasNext(); loc = iter.next())
				for(StrategicPoint position : strategicPoints)
					if(!position.isCaptured(factionType) && position.contains(loc.x,  loc.y))
						return position;
		}catch(Exception e){
			logger.warning("Error pathfinding for originVertex="+originRounded+" with message: "+e.getMessage());
		}
		return null;
	}
	
	public Queue<Vector2> getPathToPoint(Vector2 origin, Vector2 destination){
		LinkedList<Vector2> steps = new LinkedList<Vector2>();
		Vector2 originRounded = getClosestNode(Math.round(origin.x), Math.round(origin.y));
		Vector2 destinationRounded = getClosestNode(Math.round(destination.x),Math.round(destination.y));
		try{
			List<DefaultWeightedEdge> list = DijkstraShortestPath.findPathBetween(
					movementGraph, originRounded, destinationRounded);
			for(DefaultWeightedEdge edge : list)
				steps.add(movementGraph.getEdgeSource(edge));
			steps.add(movementGraph.getEdgeTarget(list.get(list.size()-1)));
		}catch(Exception e){
			logger.warning("Error pathfinding for origin="+originRounded+
					" destination="+destinationRounded+" with message: "+e.getMessage());
		}
		return steps;
	}

	public void render(Box2DDebugRenderer renderer, Matrix4 proj){
		if(nodesVisible || edgesVisible)
			renderer.render(world, proj);
	}
	
	public List<StrategicPoint> getStrategicPoints(){
		return strategicPoints;
	}
	
	private void createGraphVisible(){
		if(graphVisibleBodies == null)
			graphVisibleBodies = new ArrayList<Body>();
		for(int i=0; i<graphVisibleBodies.size(); i++)
			world.destroyBody(graphVisibleBodies.get(i));
		graphVisibleBodies.clear();
		if(nodesVisible)
			for(Vector2 node : movementGraph.vertexSet())
				PhysicsHelper.createCircle(world, BodyType.StaticBody, .1f, 1, FactionType.NEUTRAL).setTransform(node, 0);
		if(edgesVisible)
			for(DefaultWeightedEdge edge : movementGraph.edgeSet()){
				Vector2 source = movementGraph.getEdgeSource(edge), target = movementGraph.getEdgeTarget(edge);
				PhysicsHelper.createEdge(world, BodyType.StaticBody, source.x, source.y, target.x, target.y, 1, FactionType.NEUTRAL);
			}
	}

	@Override public void handleEvent(EventEnum event, Object... data) {
		switch(event){
		case PARSER_AI_GRAPH_VISIBLE:
			boolean visible = Boolean.parseBoolean((String)data[0]);
			nodesVisible = visible;
			edgesVisible = visible;
			createGraphVisible();
			break;
		default:
			break;
		
		}
	}
}
