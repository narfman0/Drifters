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

import com.badlogic.gdx.math.Vector2;
import com.blastedstudios.drifters.server.ai.AIWorld;
import com.blastedstudios.drifters.server.ai.ArtificialBeing;
import com.blastedstudios.drifters.server.ai.Objective;
import com.blastedstudios.drifters.world.Being;

/** ExecutionAction class created from MMPM action Move. */
public class Move extends jbt.execution.task.leaf.action.ExecutionAction {
	private static final Logger logger = Logger.getLogger(Move.class.getCanonicalName());
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
	 * Constructor. Constructs an instance of Move that is able to run a
	 * com.blastedstudios.drifters.server.ai.bt.actions.Move.
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
	public Move(com.blastedstudios.drifters.server.ai.bt.actions.Move modelTask,
			jbt.execution.core.BTExecutor executor,
			jbt.execution.core.ExecutionTask parent, float[] target,
			java.lang.String targetLoc) {
		super(modelTask, executor, parent);

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

	protected jbt.execution.core.ExecutionTask.Status internalTick() {
		Objective objective = (Objective) getContext().getVariable(ArtificialBeing.OBJECTIVE);
		if(objective != null){
			Being self = (Being) getContext().getVariable(ArtificialBeing.SELF);
			Vector2 position = self.getPosition();
			if(objective.getObjectivePath() == null){
				AIWorld aiWorld = (AIWorld) getContext().getVariable(ArtificialBeing.AI_WORLD);
				Vector2 targetLocation = new Vector2(getTarget()[0], getTarget()[1]);
				objective.setObjectivePath(aiWorld.getPathToPoint(position, targetLocation));
			}
			if(!objective.getObjectivePath().isEmpty()){
				if(position.x < objective.getObjectivePath().peek().x){
					self.setMoveRight(true);
					self.setMoveLeft(false);
				}else if(position.x > objective.getObjectivePath().peek().x){
					self.setMoveRight(false);
					self.setMoveLeft(true);
				}
				self.setJump(position.y < objective.getObjectivePath().peek().y);
				if(objective.getObjectivePath().peek().dst(position) < 1)
					objective.getObjectivePath().poll();
				if(System.nanoTime() % 100 == 0)//small chance of clearing current path
					objective.getObjectivePath().clear();
			}else{
				getContext().clearVariable(ArtificialBeing.OBJECTIVE);
				return Status.SUCCESS;
			}
		}else
			logger.warning("Invalid state: objective null");
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