// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 10/28/2012 10:57:56
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.actions.execution;

import java.util.logging.Logger;

import com.blastedstudios.drifters.server.ai.ArtificialBeing;
import com.blastedstudios.drifters.server.ai.Objective;

/** ExecutionAction class created from MMPM action CurrentObjective. */
public class CurrentObjective extends
		jbt.execution.task.leaf.action.ExecutionAction {
	private static final Logger logger = Logger.getLogger(CurrentObjective.class.getCanonicalName());

	/**
	 * Constructor. Constructs an instance of CurrentObjective that is able to
	 * run a com.blastedstudios.drifters.server.ai.bt.actions.CurrentObjective.
	 */
	public CurrentObjective(
			com.blastedstudios.drifters.server.ai.bt.actions.CurrentObjective modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		super(modelTask, executor, parent);

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
		if(getContext().getVariable(ArtificialBeing.OBJECTIVE) != null){
			Objective objective = (Objective) getContext().getVariable(ArtificialBeing.OBJECTIVE);
			getContext().setVariable("CurrentObjectiveTarget", new float[]{objective.position.x, objective.position.y});
			return Status.SUCCESS;
		}else
			logger.warning("Invalid state: objective null");
		return Status.FAILURE;
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