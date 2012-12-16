package com.blastedstudios.drifters.server.ai;

import com.badlogic.gdx.math.Vector2;
import com.blastedstudios.drifters.network.Generated.FactionType;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.Properties;

public class StrategicPoint {
	private static final float CAPTURE_AMOUNT = Properties.getFloat("server.strategicpoint.capture.max");
	public final Vector2[] aabb;
	private CapturedStruct captured;
	
	public StrategicPoint(Vector2[] aabb){
		this.aabb = aabb;
	}
	
	public Vector2 getCenter(){
		return aabb[0].cpy().add(aabb[1]).mul(.5f);
	}
	
	/**
	 * @return bottommost centered location in strategic point
	 */
	public Vector2 getBase(){
		return new Vector2((aabb[0].x + aabb[1].x)/2f, Math.min(aabb[0].y, aabb[1].y));
	}
	
	public CapturedStruct getPercentCaptured() {
		return captured;
	}

	public void setCaptured(CapturedStruct captured) {
		this.captured = captured;
	}
	
	public boolean contains(float x, float y){
		return aabb[0].x <= x && aabb[1].x >= x &&
				aabb[0].y <= y && aabb[1].y >= y;
	}

	public void capture(FactionType faction, float amount){
		if(captured == null)
			captured = new CapturedStruct(faction, amount);
		else if(captured.getPercentCaptured() < 100 &&
				captured.faction.equals(faction)){
			captured.addPercentCaptured(amount);
			if(captured.getPercentCaptured() >= 100)
				EventManager.sendEvent(EventEnum.WORLD_STRATEGIC_POINT_CAPTURED, this);
		}else if(!captured.faction.equals(faction)){
			captured.addPercentCaptured(-amount);
			if(captured.getPercentCaptured() <= 0){
				captured = null;
				EventManager.sendEvent(EventEnum.WORLD_STRATEGIC_POINT_LOST, this);
			}
		}
	}
	
	public boolean isCaptured(){
		return captured != null && captured.percentCaptured >= CAPTURE_AMOUNT;
	}
	
	public boolean isCaptured(FactionType faction){
		return isCaptured() && captured.faction.equals(faction);
	}
	
	public FactionType getFaction(){
		return captured != null ? captured.faction : null;
	}
	
	@Override public String toString(){
		return "StrategicPoint coords="+ getCenter() + (captured == null ? "" : captured.toString());
	}

	class CapturedStruct{
		public final FactionType faction;
		private float percentCaptured;
		
		public CapturedStruct(FactionType faction, float percentCaptured){
			this.faction = faction;
			this.percentCaptured = percentCaptured;
		}

		public float getPercentCaptured() {
			return percentCaptured;
		}

		public void setPercentCaptured(float percentCaptured) {
			this.percentCaptured = Math.max(0,Math.min(percentCaptured,CAPTURE_AMOUNT));
		}

		public void addPercentCaptured(float add) {
			this.percentCaptured = Math.max(0,Math.min(percentCaptured+add,CAPTURE_AMOUNT));
		}
		
		@Override public String toString(){
			return "faction:" + faction.name() + " percent:" + percentCaptured;
		}
	}
}
