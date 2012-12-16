// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 11/04/2012 10:41:54
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.actions.execution;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.blastedstudios.drifters.server.ai.AIThread;
import com.blastedstudios.drifters.server.ai.ArtificialBeing;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.Being;

/** ExecutionAction class created from MMPM action Respawn. */
public class Respawn extends jbt.execution.task.leaf.action.ExecutionAction {
	private static int RESPAWN_TIMER = Properties.getInt("character.respawn.timer"),
		VELOCITY_THRESHOLD = Properties.getInt("character.respawn.velocity.threshold");
	private Long startTime;

	/**
	 * Constructor. Constructs an instance of Respawn that is able to run a
	 * com.blastedstudios.drifters.server.ai.bt.actions.Respawn.
	 */
	public Respawn(jbt.model.core.ModelTask modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		super(modelTask, executor, parent);

		if (!(modelTask instanceof com.blastedstudios.drifters.server.ai.bt.actions.Respawn)) {
			throw new java.lang.RuntimeException(
					"The ModelTask must subclass com.blastedstudios.drifters.server.ai.bt.actions.Respawn");
		}
	}

	protected void internalSpawn() {
		/*
		 * Do not remove this first line unless you know what it does and you
		 * need not do it.
		 */
		this.getExecutor().requestInsertionIntoList(
				jbt.execution.core.BTExecutor.BTExecutorList.TICKABLE, this);
		startTime = Long.MAX_VALUE;
		System.out.println(this.getClass().getCanonicalName() + " spawned");
	}

	protected Status internalTick() {
		Being self = (Being) getContext().getVariable(ArtificialBeing.SELF);
		
		if(startTime == Long.MAX_VALUE && self.getVelocity().len2()<VELOCITY_THRESHOLD)
			startTime = System.currentTimeMillis();
		
		if(System.currentTimeMillis() - startTime > RESPAWN_TIMER){
			AIThread aiThread = (AIThread) getContext().getVariable(ArtificialBeing.AI_THREAD);
			List<Vector2> bases = aiThread.getClosestBases(self.getPosition(), self.getFactionType());
			if(bases.isEmpty())	//temp fix to make sure there's somewhere to respawn
				bases.add(self.getPosition().cpy());
			Vector2 closest = bases.get(0);
			float[] target = new float[]{closest.x, closest.y};
			getContext().setVariable("RespawnTarget", target);
			return Status.SUCCESS;
		}
		return Status.RUNNING;
	}

	protected void internalTerminate() {}

	protected void restoreState(jbt.execution.core.ITaskState state) {}

	protected jbt.execution.core.ITaskState storeState() {
		return null;
	}

	protected jbt.execution.core.ITaskState storeTerminationState() {
		return null;
	}
}