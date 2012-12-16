package com.blastedstudios.drifters.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

public class VisibleQueryCallback implements RayCastCallback {
	public boolean called = false;
	private final Ragdoll viewer, target;
	
	/**
	 * @param ragdolls which ragdolls should be omitted from query. Usually 
	 * this is the perceiver and the target
	 */
	public VisibleQueryCallback(Ragdoll viewer, Ragdoll target){
		this.viewer = viewer;
		this.target = target;
	}

	public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
		if(target.isOwned(fixture) || viewer.isOwned(fixture) || 
				new FilterDataComparator(viewer.torsoFixture.getFilterData()).compareTo(fixture.getFilterData())==0)
			return -1f;
		called = true;
		return fraction;
	}
	
	private class FilterDataComparator implements Comparable<Filter>{
		private final Filter self;
		public FilterDataComparator(Filter self){
			this.self = self;
		}
		@Override public int compareTo(Filter o) {
			return self.categoryBits == o.categoryBits && self.maskBits == o.maskBits ? 0 : 1;
		}
		
	}
}
