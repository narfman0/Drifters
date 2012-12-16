// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 10/28/2012 10:57:56
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.conditions;

/** ModelCondition class created from MMPM condition HighDanger. */
public class HighDanger extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of HighDanger. */
	public HighDanger(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.blastedstudios.drifters.server.ai.bt.conditions.execution.HighDanger
	 * task that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.blastedstudios.drifters.server.ai.bt.conditions.execution.HighDanger(
				this, executor, parent);
	}
}