// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 11/04/2012 10:41:54
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.actions.execution;

import com.badlogic.gdx.math.Vector2;
import com.blastedstudios.drifters.server.ai.ArtificialBeing;
import com.blastedstudios.drifters.server.world.WorldManager;
import com.blastedstudios.drifters.world.Being;

/** ExecutionAction class created from MMPM action Teleport. */
public class Teleport extends jbt.execution.task.leaf.action.ExecutionAction {
	/**
	 * Value of the parameter "target" in case its value is specified at
	 * construction time. null otherwise.
	 */
	private float[] target;
	/**
	 * Location, in the context, of the parameter "target" in case its value is
	 * not specified at construction time. null otherwise.
	 */
	private java.lang.String targetLoc;

	/**
	 * Constructor. Constructs an instance of Teleport that is able to run a
	 * com.blastedstudios.drifters.server.ai.bt.actions.Teleport.
	 * 
	 * @param target
	 *            value of the parameter "target", or null in case it should be
	 *            read from the context. If null,
	 *            <code>targetLoc<code> cannot be null.
	 * @param targetLoc
	 *            in case <code>target</code> is null, this variable represents
	 *            the place in the context where the parameter's value will be
	 *            retrieved from.
	 */
	public Teleport(jbt.model.core.ModelTask modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent, float[] target,
			java.lang.String targetLoc) {
		super(modelTask, executor, parent);

		if (!(modelTask instanceof com.blastedstudios.drifters.server.ai.bt.actions.Teleport)) {
			throw new java.lang.RuntimeException(
					"The ModelTask must subclass com.blastedstudios.drifters.server.ai.bt.actions.Teleport");
		}

		this.target = target;
		this.targetLoc = targetLoc;
	}

	/**
	 * Returns the value of the parameter "target", or null in case it has not
	 * been specified or it cannot be found in the context.
	 */
	public float[] getTarget() {
		if (this.target != null) {
			return this.target;
		} else {
			return (float[]) this.getContext().getVariable(this.targetLoc);
		}
	}

	protected void internalSpawn() {
		/*
		 * Do not remove this first line unless you know what it does and you
		 * need not do it.
		 */
		this.getExecutor().requestInsertionIntoList(
				jbt.execution.core.BTExecutor.BTExecutorList.TICKABLE, this);
		System.out.println(this.getClass().getCanonicalName() + " spawned");
	}

	protected Status internalTick() {
		Being self = (Being) getContext().getVariable(ArtificialBeing.SELF);
		WorldManager world = (WorldManager) getContext().getVariable(ArtificialBeing.WORLD);
		Vector2 target = new Vector2(getTarget()[0], getTarget()[1]);
		self.respawn(world.getWorld(), target.x, target.y);
		return Status.SUCCESS;
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