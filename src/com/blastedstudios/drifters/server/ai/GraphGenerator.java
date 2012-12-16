package com.blastedstudios.drifters.server.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.blastedstudios.drifters.physics.IntersectQueryCallback;
import com.blastedstudios.drifters.util.Properties;

/**
 * Generates graph from world JSON file. This includes normal physics objects
 * and strategic points, to be loaded separately 
 * @author narfman0
 *
 */
public class GraphGenerator {
	private static final Logger logger = Logger.getLogger(GraphGenerator.class.getCanonicalName());
	private static final float 
		GRAPH_THRESHOLD = .01f,
		PATHING_LENGTH = Properties.getFloat("server.world.pathing.length"),
		WORLD_MAX_X = Properties.getFloat("world.dimensions.x.max"),
		WORLD_MIN_X = Properties.getFloat("world.dimensions.x.min"),
		WORLD_MIN_Y = Properties.getFloat("world.dimensions.y.min"),
		WORLD_MAX_Y = Properties.getFloat("world.dimensions.y.max");
	
	public static SimpleWeightedGraph<Vector2, DefaultWeightedEdge> generateGraph(World world){
		SimpleWeightedGraph<Vector2, DefaultWeightedEdge> graph = 
				new SimpleWeightedGraph<Vector2, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		for(float y=WORLD_MIN_Y; y<WORLD_MAX_Y; y++)
			for(float x=WORLD_MIN_X; x<WORLD_MAX_X; x++){
				if(isValidNode(x,y,world)){
					graph.addVertex(new Vector2(x, y));
					if(x>WORLD_MIN_X && isValidNode(x-1,y,world)){
						DefaultWeightedEdge edge = graph.addEdge(new Vector2(x,y), new Vector2(x-1,y));
						if(isAirNode(x,y,world))
							graph.setEdgeWeight(edge, 2);
					}
					if(y>WORLD_MIN_Y && isValidNode(x,y-1,world)){
						DefaultWeightedEdge edge = graph.addEdge(new Vector2(x,y), new Vector2(x,y-1));
						if(isAirNode(x,y,world))
							graph.setEdgeWeight(edge, 2);
					}if(x > WORLD_MIN_X && y>WORLD_MIN_Y && isValidNode(x-1,y-1,world))
						graph.setEdgeWeight(graph.addEdge(new Vector2(x,y), new Vector2(x-1,y-1)), 
								isAirNode(x,y,world) ? 2.5f : 1.414f);
					if(x+1 < WORLD_MAX_X && y>WORLD_MIN_Y && isValidNode(x+1,y-1,world))
						graph.setEdgeWeight(graph.addEdge(new Vector2(x,y), new Vector2(x+1,y-1)), 
								isAirNode(x,y,world) ? 2.5f : 1.414f);
				}
			}
		return graph;
	}

	public static List<StrategicPoint> generateStrategicPoints(World world){
		final List<StrategicPoint> points = new ArrayList<StrategicPoint>();
		for(float y=WORLD_MIN_Y; y<WORLD_MAX_Y; y++)
			for(float x=WORLD_MIN_X; x<WORLD_MAX_X; x++){
				//check if strat point already exists at this location
				boolean contains = false;
				for(StrategicPoint point : points)
					if(point.contains(x, y))
						contains = true;
				if(contains)
					continue;
				
				//if not add the whole thing
				IntersectQueryCallback callback =new IntersectQueryCallback();
				world.QueryAABB(callback, x-GRAPH_THRESHOLD, y-GRAPH_THRESHOLD, x+GRAPH_THRESHOLD, y+GRAPH_THRESHOLD);
				if(callback.called){
					StrategicPoint point = new StrategicPoint(getStrategicPointExtents(world, x, y));
					points.add(point);
					logger.info("Created strategic point: " + point.toString());
				}
			}
		return points;
	}

	private static Vector2[] getStrategicPointExtents(World world, float blX, float blY){
		Vector2[] aabb = new Vector2[2];
		aabb[0] = new Vector2(blX,blY);	//bottom left
		aabb[1] = new Vector2();	//top right
		for(float x=blX;; x+=GRAPH_THRESHOLD){
			IntersectQueryCallback callback =new IntersectQueryCallback();
			world.QueryAABB(callback, x-GRAPH_THRESHOLD, blY-GRAPH_THRESHOLD, x+GRAPH_THRESHOLD, blY+GRAPH_THRESHOLD);
			if(callback.called)
				aabb[1].x = x;
			else
				break;
		}
		for(float y=blY;; y+=GRAPH_THRESHOLD){
			IntersectQueryCallback callback =new IntersectQueryCallback();
			world.QueryAABB(callback, blX-GRAPH_THRESHOLD, y-GRAPH_THRESHOLD, blX+GRAPH_THRESHOLD, y+GRAPH_THRESHOLD);
			if(callback.called)
				aabb[1].y = y;
			else
				break;
		}
		return aabb;
	}
	
	/**
	 * For world building only (when creating nodes)
	 * @return if that x/y intersects with physical objects loaded from json
	 */
	private static boolean isValidNode(float x, float y, World world){
		IntersectQueryCallback closeCallback = new IntersectQueryCallback(),
				insideFixtureCallback = new IntersectQueryCallback();
		world.QueryAABB(closeCallback, x-PATHING_LENGTH, y-PATHING_LENGTH, x+PATHING_LENGTH, y);
		world.QueryAABB(insideFixtureCallback, x-.01f, y-.01f, x+.01f, y+.01f);
		return closeCallback.called && !insideFixtureCallback.called;
	}

	private static boolean isAirNode(float x, float y, World world){
		IntersectQueryCallback closeCallback = new IntersectQueryCallback(),
				farCallback = new IntersectQueryCallback();
		world.QueryAABB(closeCallback, x-1, y-1, x+1, y);
		world.QueryAABB(farCallback, x-PATHING_LENGTH, y-PATHING_LENGTH, x+PATHING_LENGTH, y);
		return farCallback.called && !closeCallback.called;
	}
}
