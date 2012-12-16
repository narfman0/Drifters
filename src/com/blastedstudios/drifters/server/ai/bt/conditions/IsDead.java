// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 11/04/2012 10:41:54
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.conditions;

/** ModelCondition class created from MMPM condition IsDead. */
public class IsDead extends jbt.model.task.leaf.condition.ModelCondition {

	/** Constructor. Constructs an instance of IsDead. */
	public IsDead(jbt.model.core.ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns a
	 * com.blastedstudios.drifters.server.ai.bt.conditions.execution.IsDead task
	 * that is able to run this task.
	 */
	public jbt.execution.core.ExecutionTask createExecutor(
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent) {
		return new com.blastedstudios.drifters.server.ai.bt.conditions.execution.IsDead(
				this, executor, parent);
	}
}