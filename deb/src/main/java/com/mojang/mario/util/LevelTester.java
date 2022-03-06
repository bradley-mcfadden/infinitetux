package com.mojang.mario.util;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.LOGGER;


/**
 * LevelTester returns EvaluationInfo from using an agent on a com.mojang.mario.level.Level.
 */
public class LevelTester {

    public static final String ASTAR_AGENT = "competition.cig.robinbaumgarten.AStarAgent";
    public static final String HUMAN_AGENT = "ch.idsia.ai.agents.human.HumanKeyboardAgent";

    /**
     * test the level with the specfied agent, and get EvalutionInfo for it.
     * @param level Level to testS
     * @param agent Agent to use
     * @return EvaluationInfo of results of testing
     */
    public static EvaluationInfo test(com.mojang.mario.level.Level level, String agent) {
        System.out.println("Calling test level with agent " + agent);
        Agent controller = new HumanKeyboardAgent();
        if (agent != null) {
            controller = AgentsPool.load (agent);
            AgentsPool.addAgent(controller);
        }
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        LOGGER.setVerboseMode(LOGGER.VERBOSE_MODE.ALL);
        ch.idsia.mario.engine.level.Level tmpLevel = LevelAdapter.convert(level);
        System.out.println("Calling setLevel and setAgent");
        options.setLevel(tmpLevel);
        options.setAgent(controller);
        
        System.out.print("after setLevel ");
        System.out.println(options.getLevel() == null);
        Task task = new ProgressTask(options);
        options.setMarioMode(0);
        options.setMaxFPS(true);
        options.setVisualization(false);
        options.setNumberOfTrials(2);
        options.setMatlabFileName("");
        options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
        options.setLevelDifficulty(10);
        System.out.println("Start playing");
        task.setOptions(options);

        System.out.println ("Score: " + task.evaluate(controller)[0]);
        EvaluationInfo info = task.getLastResult();

        return info;
    }
}
