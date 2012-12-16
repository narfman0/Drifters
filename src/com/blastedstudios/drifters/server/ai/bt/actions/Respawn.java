// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 11/04/2012 10:41:54
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.actions;

/** ModelAction class created from MMPM action Respawn. */
public class Respawn extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of Respawn. */
	public Respawn(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.blastedstudios.drifters.server.ai.bt.actions.execution.Respawn
	 * task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.blastedstudios.drifters.server.ai.bt.actions.execution.Respawn(
				this, executor, parent);
	}
}