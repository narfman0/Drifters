// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 11/04/2012 10:41:54
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.conditions.execution;

import com.blastedstudios.drifters.server.ai.ArtificialBeing;
import com.blastedstudios.drifters.world.Being;

/** ExecutionCondition class created from MMPM condition IsDead. */
public class IsDead extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of IsDead that is able to run a
	 * com.blastedstudios.drifters.server.ai.bt.conditions.IsDead.
	 */
	public IsDead(jbt.model.core.ModelTask modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		super(modelTask, executor, parent);

		if (!(modelTask instanceof com.blastedstudios.drifters.server.ai.bt.conditions.IsDead)) {
			throw new java.lang.RuntimeException(
					"The ModelTask must subclass com.blastedstudios.drifters.server.ai.bt.conditions.IsDead");
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

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		Being self = (Being) getContext().getVariable(ArtificialBeing.SELF);
		return self.isDead() ? Status.SUCCESS : Status.FAILURE;
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