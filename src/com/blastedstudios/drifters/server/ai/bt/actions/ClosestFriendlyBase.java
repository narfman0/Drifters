// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 10/28/2012 10:57:56
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.actions;

/** ModelAction class created from MMPM action ClosestFriendlyBase. */
public class ClosestFriendlyBase extends jbt.model.task.leaf.action.ModelAction {

	/** Constructor. Constructs an instance of ClosestFriendlyBase. */
	public ClosestFriendlyBase(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a com.blastedstudios.drifters.server.ai.bt.actions.execution.
	 * ClosestFriendlyBase task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.blastedstudios.drifters.server.ai.bt.actions.execution.ClosestFriendlyBase(
				this, executor, parent);
	}
}