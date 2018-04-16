package competition.venue.year.type.Gaudl;

import competition.venue.year.type.Gaudl.gp.MarioData;
import org.jgap.gp.IGPProgram;

import org.platformer.agents.Agent;
import org.platformer.benchmark.platform.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Swen Gaudl, swen.gaudl@gmail.com
 * Date: 11/27/12
 * Time: 1:47 AM
 * Package: touring.Gaudl
 */

public class Mario_GPAgent implements Agent
{
	MarioData m_data;
	String agentName;
	IGPProgram executionCode;
	Object[] executionArguments;
	
	public Mario_GPAgent(IGPProgram prog, Object[] args,MarioData data) {
		m_data = data;
		executionArguments = args;
		executionCode = prog;
	}
	
	@Override
	public boolean[] getAction() {
		boolean [] output = new boolean[Environment.numberOfKeys];
		executionCode.execute_void(0, executionArguments);
		//executionCode.execute_boolean(0, executionArguments);
		if (m_data instanceof MarioData)
			output = m_data.getActions();
		
		return output;
	}
	
	

	@Override
	public void integrateObservation(Environment environment) {
		byte[][] levelScene = environment.getLevelSceneObservationZ(1);
	    byte[][] enemies = environment.getEnemiesObservationZ(0);
	    byte[][] mergedObservation = environment.getMergedObservationZZ(1, 0);
		
		m_data.setEnvironment(environment);
	}

	@Override
	public void giveIntermediateReward(float intermediateReward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
	}

	@Override
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
			int egoCol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return this.agentName;
	}

	@Override
	public void setName(String name) {
		this.agentName = name;
		
	}
    
}
