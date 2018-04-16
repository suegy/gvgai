/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package competition.venue.year.type.Gaudl;

import java.io.IOException;
import java.lang.reflect.Modifier;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import competition.venue.year.type.Gaudl.gp.WeightedGPRouletteSelector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;
import org.jgap.InvalidConfigurationException;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.GPProblem;
import org.jgap.gp.impl.BranchTypingCross;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.impl.TournamentSelector;
import org.jgap.gp.terminal.Terminal;
import org.jgap.gp.terminal.Variable;

import competition.venue.year.type.Gaudl.Genes.And;
import competition.venue.year.type.Gaudl.Genes.CanJump;
import competition.venue.year.type.Gaudl.Genes.CanShoot;
import competition.venue.year.type.Gaudl.Genes.Down;
import competition.venue.year.type.Gaudl.Genes.IfElse;
import competition.venue.year.type.Gaudl.Genes.IsAirAt;
import competition.venue.year.type.Gaudl.Genes.IsBreakableAt;
import competition.venue.year.type.Gaudl.Genes.IsCoinAt;
import competition.venue.year.type.Gaudl.Genes.IsEnemyAt;
import competition.venue.year.type.Gaudl.Genes.IsFireFlowerAt;
import competition.venue.year.type.Gaudl.Genes.IsMushroomAt;
import competition.venue.year.type.Gaudl.Genes.IsTall;
import competition.venue.year.type.Gaudl.Genes.IsWalkableAt;
import competition.venue.year.type.Gaudl.Genes.Jump;
import competition.venue.year.type.Gaudl.Genes.LastActionWas;
import competition.venue.year.type.Gaudl.Genes.Left;
import competition.venue.year.type.Gaudl.Genes.LongJump;
import competition.venue.year.type.Gaudl.Genes.Not;
import competition.venue.year.type.Gaudl.Genes.Right;
import competition.venue.year.type.Gaudl.Genes.Run;
import competition.venue.year.type.Gaudl.Genes.Shoot;
import competition.venue.year.type.Gaudl.Genes.SubProgram;
import competition.venue.year.type.Gaudl.Genes.Wait;
import org.platformer.benchmark.tasks.BasicTask;
import org.platformer.tools.PlatformerAIOptions;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey at idsia dot ch Date: Mar 17, 2010 Time: 8:28:00 AM
 * Package: ch.idsia.scenarios
 */
public final class GPSystemStandAlone extends GPProblem
{
protected static Variable vx;
private GPGenotype Geno;
public Thread gpThread;
public static final int popSize = 50;
private transient Logger LOGGER;
private transient Genson jsonSerialiser;

public GPSystemStandAlone(){
    jsonSerialiser = new GensonBuilder()
            .useClassMetadata(true)
            .useMethods(false)
            .setSkipNull(true)
            .useFields(true, new VisibilityFilter(Modifier.TRANSIENT,Modifier.STATIC))
            .useClassMetadataWithStaticType(false)
            .create();
    // configuring the Logger for JGAP
    LOGGER = Logger.getLogger(this.getClass());

}
    public void setup (GPFitnessFunction metric) {
	GPConfiguration config;

	//Thread gpThread = null;
	try {
        config = new GPConfiguration();
        //config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
        config.setProgramCreationMaxTries(-1);
        //config.setStrictProgramCreation(true);
        //config.setMinimumPopSizePercent(popSize);
        config.setMinInitDepth(1);
        config.setMaxInitDepth(7);
        config.setPopulationSize(popSize);
        //Taken from anttrail. WORTH INVESTIGATING.
        config.setCrossoverProb(0.6f);//orig: 0.9f
        config.setReproductionProb(0.4f); //orig: 0.1f
        config.setNewChromsPercent(0.2f); //orig: 0.3f
        config.setMutationProb(0.015f);
        config.setFunctionProb(0.8f);
        //config.setUseProgramCache(true);
        config.setCrossoverMethod(new BranchTypingCross(config,false));
        config.setSelectionMethod(new TournamentSelector(3));
        //config.setSelectionMethod(new WeightedGPRouletteSelector(config));
        config.setPreservFittestIndividual(true);
        config.setFitnessFunction(metric);
        setGPConfiguration(config);
        Geno = create();
        Geno.setVerboseOutput(true);
        gpThread = new Thread(Geno);
    }
    catch (InvalidConfigurationException e){
        System.err.println("wrong config: \n"+e);
    }
}

@SuppressWarnings("rawtypes")
@Override
public GPGenotype create() throws InvalidConfigurationException {
	Class [] types = { CommandGene.VoidClass, 
		//	CommandGene.BooleanClass
			};
	GPConfiguration conf  = getGPConfiguration();
	Class [][] argTypes = {{},
		//	{CommandGene.BooleanClass,CommandGene.BooleanClass}
			};
	CommandGene [][] nodes = {
			{
				//vx = Variable.create(conf,"X", CommandGene.IntegerClass),
				new Terminal(conf, CommandGene.IntegerClass,-6,6,true),
				new Terminal(conf, CommandGene.IntegerClass,-15,15,true),
				//new Terminal(conf, CommandGene.IntegerClass,-4,4,true),
				new SubProgram(conf,new Class[] {CommandGene.VoidClass,CommandGene.VoidClass}),
				//new SubProgram(conf),
				//new Add(conf, CommandGene.IntegerClass),
				//new GpGreaterThan(conf, CommandGene.IntegerClass),
				//new Equals(conf, CommandGene.IntegerClass),
				new IfElse(conf, CommandGene.BooleanClass),
				//new Equals(conf, CommandGene.BooleanClass),
				//new Equals(conf, CommandGene.IntegerClass),
				//new Or(conf),
				new And(conf),
				new Not(conf),
				new CanJump(conf),
				new CanShoot(conf),
				new LastActionWas(conf),
				new IsTall(conf),
				//new ObjectAtXYIs(conf),
				//new True(conf),
				new IsBreakableAt(conf),
				new IsCoinAt(conf),
				new IsAirAt(conf),
				new IsEnemyAt(conf),
				new IsFireFlowerAt(conf),
				new IsMushroomAt(conf),
				//new IsPrincessAt(conf),
				new IsWalkableAt(conf),
				//new SubProgram(conf,new Class[] {CommandGene.VoidClass,CommandGene.VoidClass,CommandGene.VoidClass}),
				new Down(conf),
				new Wait(conf),
				new Left(conf),
				new Right(conf),
				new Shoot(conf),
				new Jump(conf),
				new LongJump(conf),
				//new JumpLeft(conf),
				//new JumpRight(conf),
				//new LongJumpLeft(conf),
				//new LongJumpRight(conf),
				new Run(conf),
		//		new ADF(conf, 1, 2)
			},
			/*{
				new And(conf),
			}*/
	};
	GPGenotype geno = GPGenotype.randomInitialGenotype(conf, types, argTypes, nodes,
			250, true);

	geno.switchParsimonyPressure(false);
	return geno;
}

public GPFitnessFunction setMetric(String [] args) {
    final PlatformerAIOptions marioAIOptions = new PlatformerAIOptions(args);
    final BasicTask basicTask = new BasicTask(marioAIOptions);
    GameplayMetricFitness metric = new GameplayMetricFitness(basicTask,marioAIOptions);
    //GamalyzerFitness metric = new GamalyzerFitness(basicTask,marioAIOptions);
    //TraceFitness metric = new TraceFitness(basicTask,marioAIOptions);
    //CombinedTraceGamalyzer metric = new CombinedTraceGamalyzer(basicTask,marioAIOptions);

    return metric;
}

public void train(){

    if (gpThread instanceof Thread)
        gpThread.start();

    while (gpThread.isAlive()) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public static void main(String[] args) throws InterruptedException
{
    boolean training = true;
//        final String argsString = "-vis on";
	try {
		Logger.getRoot().setLevel(Level.INFO);
		RollingFileAppender logger = new RollingFileAppender(new SimpleLayout(), "genotype.log");
		logger.setMaxBackupIndex(10);
		logger.setMaxFileSize("100MB");
		Logger.getRootLogger().addAppender(logger);
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    GPSystemStandAlone marioGP = new GPSystemStandAlone();

    if (training) {
        GPFitnessFunction metric = marioGP.setMetric(args);
        marioGP.setup(metric);
        marioGP.train();
    } else { // loading and playing with an agent

    }
    System.exit(0);
}

}
