package com.blastedstudios.drifters.server.ai;

import java.util.Queue;

import com.badlogic.gdx.math.Vector2;

public class Objective implements Comparable<Objective>{
	public final ObjectiveEnum type;
	public final Vector2 position;
	/**
	 * Score from 0-1 of importance
	 */
	public final Float score;
	private Queue<Vector2> objectivePath;
	
	public Objective(float score, ObjectiveEnum type, Vector2 position, Queue<Vector2> objectivePath){
		this.score = score;
		this.type = type;
		this.position = position;
		this.objectivePath = objectivePath;
	}

	public enum ObjectiveEnum{
		ATTACK, CAPTURE, DEFEND
	}

	@Override public int compareTo(Objective o) {
		return score.compareTo(o.score);
	}
	
	@Override public String toString(){
		return type.name() + " " + position;
	}
	
	@Override public boolean equals(Object o){
		return o instanceof Objective && score.equals(((Objective)o).score) 
				&& type.equals(((Objective)o).type) && position.equals(((Objective)o).position);
	}
	
	public Objective clone(){
		return new Objective(score, type, position, getObjectivePath());
	}

	public Queue<Vector2> getObjectivePath() {
		return objectivePath;
	}

	public void setObjectivePath(Queue<Vector2> objectivePath) {
		this.objectivePath = objectivePath;
	}
}
