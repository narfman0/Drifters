// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 10/28/2012 10:57:56
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.actions;

/** ModelAction class created from MMPM action CurrentObjective. */
public class CurrentObjective extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of CurrentObjective. */
	public CurrentObjective(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.blastedstudios.drifters.server.ai.bt.actions.execution.
	 * CurrentObjective task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.blastedstudios.drifters.server.ai.bt.actions.execution.CurrentObjective(
				this, executor, parent);
	}
}