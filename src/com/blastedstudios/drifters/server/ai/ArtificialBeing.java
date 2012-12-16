package com.blastedstudios.drifters.server.ai;

import java.util.List;

import jbt.execution.core.BTExecutorFactory;
import jbt.execution.core.ContextFactory;
import jbt.execution.core.IBTExecutor;
import jbt.execution.core.IBTLibrary;
import jbt.execution.core.IContext;

import com.badlogic.gdx.physics.box2d.World;
import com.blastedstudios.drifters.network.Generated.Gun;
import com.blastedstudios.drifters.network.Generated.NetBeing.Class;
import com.blastedstudios.drifters.network.Generated.Race;
import com.blastedstudios.drifters.server.Server;
import com.blastedstudios.drifters.server.ai.bt.DriftersBotBTLibrary;
import com.blastedstudios.drifters.world.Being;

public class ArtificialBeing extends Being {
	public final static String AI_WORLD = "AIWorld", AI_THREAD = "AIThread", 
			OBJECTIVE = "Objective", SELF = "Self", WORLD = "World";
	private static IBTLibrary btLibrary = new DriftersBotBTLibrary();
	private IContext context;
	private IBTExecutor btExecutor;
	
	public ArtificialBeing(Server server, AIWorld aiWorld, String name,
			Class type, float x, float y, float maxHP, float hp, List<Gun> guns,
			int currentGun, Race factionType, int cash, int level, int xp) {
		super(server.world.getWorld(), name, type, x, y, maxHP, hp, guns, currentGun, factionType, cash, level, xp);
		context = ContextFactory.createContext(btLibrary);
		context.setVariable(AI_WORLD, aiWorld);
		context.setVariable(SELF, this);
		context.setVariable(WORLD, server.world);
		context.setVariable(AI_THREAD, server.aiThread);
		btExecutor = BTExecutorFactory.createBTExecutor(btLibrary.getBT("Root"), context);
	}
	
	@Override public void render(World world){
		super.render(world);
		btExecutor.tick();
	}

	public Objective getObjective() {
		return (Objective) context.getVariable(OBJECTIVE);
	}

	public void setObjective(Objective objective) {
		context.setVariable(OBJECTIVE, objective);
	}
}
