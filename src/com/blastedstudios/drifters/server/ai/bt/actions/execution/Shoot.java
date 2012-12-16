// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                MUST BE CAREFULLY COMPLETED              
//                                                         
//           ABSTRACT METHODS MUST BE IMPLEMENTED          
//                                                         
// Generated on 10/28/2012 10:57:56
// ******************************************************* 
package com.blastedstudios.drifters.server.ai.bt.actions.execution;

import com.badlogic.gdx.math.Vector2;
import com.blastedstudios.drifters.network.Generated.GunShot;
import com.blastedstudios.drifters.server.ai.ArtificialBeing;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.world.Being;

/** ExecutionAction class created from MMPM action Shoot. */
public class Shoot extends jbt.execution.task.leaf.action.ExecutionAction {
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
	 * Constructor. Constructs an instance of Shoot that is able to run a
	 * com.blastedstudios.drifters.server.ai.bt.actions.Shoot.
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
	public Shoot(com.blastedstudios.drifters.server.ai.bt.actions.Shoot modelTask,
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
		Being self = (Being) getContext().getVariable(ArtificialBeing.SELF);
		Vector2 target = new Vector2(getTarget()[0], getTarget()[1]);
		Vector2 dir = target.cpy().sub(self.getPosition()).nor();
		GunShot.Builder gunShot = GunShot.newBuilder();
		gunShot.setBeing(self.getName());
		gunShot.setGun(self.getEquippedGun());
		gunShot.setPosX(self.getPosition().x);
		gunShot.setPosY(self.getPosition().y);
		gunShot.setDirX(dir.x);
		gunShot.setDirY(dir.y);
		EventManager.sendEvent(EventEnum.GUN_SHOT_REQUEST, gunShot.build());
		if(self.getEquippedGun().getCurrentRounds() == 0)
			EventManager.sendEvent(EventEnum.CHARACTER_RELOAD_REQUEST, self.getName());
		return jbt.execution.core.ExecutionTask.Status.SUCCESS;
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