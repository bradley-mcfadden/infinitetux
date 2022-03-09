package com.mojang.mario.util;

import com.mojang.mario.level.Level;
import com.mojang.mario.level.SpriteTemplate;
import com.mojang.mario.level.Tile;
import com.mojang.mario.sprites.Enemy;


import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ch.idsia.tools.ActionTrace;
import ch.idsia.tools.EvaluationInfo;

/**
 * LevelStats provides datasets for use in graphs 
 */
public class LevelStats {
    /**
     * getLevelDistributionXY returns an XYDataset for components by their x position
     * @param level Level to sample from
     * @return XYDataset containing enemy and reward series
     */
    public static XYDataset getLevelDistributionXY(Level level)
    {
        XYSeries enemySeries = new XYSeries("enemies");
        XYSeries rewardSeries = new XYSeries("rewards");
        boolean reachedExit = false;

        for (int x = 0; x < level.width && !reachedExit; x++) {
            int enemyCountAtX = 0;
            int rewardCountAtX = 0;
            for (int y = 0; y < level.height; y++) {
                SpriteTemplate st = level.spriteTemplates[x][y];
                if (st != null && st.getType() != Enemy.ENEMY_NULL)
                {
                    enemyCountAtX++;
                    // series.addValue(1.0, (Comparable<Integer>)x, (Comparable<String>)Y_KEY);
                } else if (level.map[x][y] == Tile.BULLET_BILL_TOP) {
                    enemyCountAtX++;
                } else if ((level.map[x][y] >= Tile.COIN_REWARD  && level.map[x][y] <= 35) 
                || (level.map[x][y] >= 20 && level.map[x][y]  <= 23)
                || level.map[x][y] == Tile.HIDDEN_POWERUP_REWARD || level.map[x][y] == Tile.HIDDEN_COIN_REWARD) {
                    rewardCountAtX++;
                } else if (level.map[x][y] == Tile.LEVEL_EXIT) {
                    reachedExit = true;
                    break;
                }
            }
            enemySeries.add(x, enemyCountAtX);
            rewardSeries.add(x, rewardCountAtX);
        }
        XYSeriesCollection table = new XYSeriesCollection();
        table.addSeries(enemySeries);
        table.addSeries(rewardSeries);
        return table;
    }

    /**
     * getAgentActionXY reads info to determine agent actions during play time
     * @param info Results of agent's run through the level
     * @return Time series with each key press the agent may have made at a given frame.
     */
    public static XYDataset getAgentActionXY(EvaluationInfo info)
    {
        XYSeries[] series = new XYSeries[ActionTrace.KEYS.length];
        for (int i = 0; i < series.length; i++)
            series[i] = new XYSeries(ActionTrace.KEYS[i]);
        for (ActionTrace action : info.actions) 
        {
            for (int i = 0; i < ActionTrace.KEYS.length; i++)
                series[i].add(action.getFrame(), action.getKey(i) + 2 * i);
        }
        XYSeriesCollection table = new XYSeriesCollection();
        for (XYSeries ser : series) 
            table.addSeries(ser);
        return table;
    }
}
