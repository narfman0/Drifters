// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 10/28/2012 10:57:56
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.conditions.execution;

import com.blastedstudios.drifters.physics.VisibleQueryCallback;
import com.blastedstudios.drifters.server.ai.ArtificialBeing;
import com.blastedstudios.drifters.server.world.WorldManager;
import com.blastedstudios.drifters.world.Being;

/** ExecutionCondition class created from MMPM condition HighDanger. */
public class HighDanger extends
		jbt.execution.task.leaf.condition.ExecutionCondition {

	/**
	 * Constructor. Constructs an instance of HighDanger that is able to run a
	 * com.blastedstudios.drifters.server.ai.bt.conditions.HighDanger.
	 */
	public HighDanger(
			com.blastedstudios.drifters.server.ai.bt.conditions.HighDanger modelTask,
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
		WorldManager world = (WorldManager) getContext().getVariable(ArtificialBeing.WORLD);
		Being self = (Being) getContext().getVariable(ArtificialBeing.SELF);
		int enemyCount = 0;
		synchronized (world.getWorld()) {
			for(Being being : world.getAllBeings())
				if(being.getFactionType() != self.getFactionType()){
					VisibleQueryCallback callback = new VisibleQueryCallback(self.getRagdoll(), being.getRagdoll());
					world.getWorld().rayCast(callback, self.getPosition(), being.getPosition());
					if(!callback.called){
						enemyCount++;
						getContext().setVariable("HighDangerTarget", new float[]{being.getPosition().x,being.getPosition().y});
						if(enemyCount > 1)
							return Status.SUCCESS;
					}
				}
		}
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