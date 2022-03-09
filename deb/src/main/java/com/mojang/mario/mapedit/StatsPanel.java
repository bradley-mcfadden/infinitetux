package com.mojang.mario.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.mojang.mario.level.Level;
import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.util.LevelStats;
import com.mojang.mario.util.LevelTester;

import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.xy.XYDataset; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation; 

import ch.idsia.tools.EvaluationInfo;

/**
 * StatsPanel displays graphs for showing the user information about the current level.
 */
public class StatsPanel extends JPanel {
    private JFreeChart enemyChart;
    private JFreeChart agentChart;

    /*
    public StatsPanel(XYDataset levelDataset, XYDataset agentDataset)
    {
        setLayout(new GridLayout(0, 1));
        enemyChart = ChartFactory.createXYLineChart(
            "Component Distribution", "x", "count", levelDataset, 
            PlotOrientation.VERTICAL, true, true, false
        );

        agentChart = ChartFactory.createXYLineChart(
            "Agent Actions by Time", "frame", "actionState", agentDataset,
            PlotOrientation.VERTICAL, true, true, false
        );

        NumberAxis axis = (NumberAxis)enemyChart.getXYPlot().getDomainAxis();
        axis.setTickUnit(new NumberTickUnit(20));
        axis = (NumberAxis)enemyChart.getXYPlot().getRangeAxis();
        axis.setTickUnit(new NumberTickUnit(1));

        axis = (NumberAxis)agentChart.getXYPlot().getDomainAxis();
        axis.setTickUnit(new NumberTickUnit(20));
        axis = (NumberAxis)agentChart.getXYPlot().getRangeAxis();
        axis.setTickUnit(new NumberTickUnit(1));

        ChartPanel chartPanel = new ChartPanel(enemyChart);
        chartPanel.setPreferredSize(new Dimension(400, 400));
        ChartPanel agentPanel = new ChartPanel(agentChart);
        agentPanel.setPreferredSize(new Dimension(400, 400));
        add(chartPanel);
        add(agentPanel);
    }

    public StatsPanel(CategoryDataset datasetComponents, CategoryDataset datasetAgent)
    {
        setLayout(new GridLayout(0, 1));
        enemyChart = ChartFactory.createLineChart(
            "Component Distribution", "x", "count", datasetComponents
        );
        enemyChart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        enemyChart.getCategoryPlot().setDomainGridlinesVisible(true);
        agentChart = ChartFactory.createLineChart(
            "Agent Actions by Time", "frame", "actionState", datasetAgent
        );
        ChartPanel componentPanel = new ChartPanel(enemyChart);
        componentPanel.setPreferredSize(new Dimension(400, 400));
        ChartPanel agentPanel = new ChartPanel(agentChart);
        agentPanel.setPreferredSize(new Dimension(400, 400));

        add(componentPanel);
        add(agentPanel);
    }*/

    /**
     * Constructor
     */
    public StatsPanel()
    {
        setLayout(new GridLayout(0, 1));
        enemyChart = ChartFactory.createXYLineChart(
            "Component Distribution", "x", "count", null, 
            PlotOrientation.VERTICAL, true, true, false
        );

        agentChart = ChartFactory.createXYLineChart(
            "Agent Actions by Time", "frame", "actionState", null,
            PlotOrientation.VERTICAL, true, true, false
        );

        NumberAxis axis = (NumberAxis)enemyChart.getXYPlot().getDomainAxis();
        axis.setTickUnit(new NumberTickUnit(20));
        axis = (NumberAxis)enemyChart.getXYPlot().getRangeAxis();
        axis.setTickUnit(new NumberTickUnit(1));

        axis = (NumberAxis)agentChart.getXYPlot().getDomainAxis();
        axis.setTickUnit(new NumberTickUnit(20));
        axis = (NumberAxis)agentChart.getXYPlot().getRangeAxis();
        axis.setTickUnit(new NumberTickUnit(1));

        ChartPanel chartPanel = new ChartPanel(enemyChart);
        chartPanel.setPreferredSize(new Dimension(400, 400));
        chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        ChartPanel agentPanel = new ChartPanel(agentChart);
        agentPanel.setPreferredSize(new Dimension(400, 400));
        agentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(chartPanel);
        add(agentPanel);
    }

    /**
     * setAgentDataset for agent trace graph
     * @param agentDataset Dataset containing agent's actions over frames
     */
    public void setAgentDataset(XYDataset agentDataset)
    {
        agentChart.getXYPlot().setDataset(agentDataset);
    }

    /**
     * setComponentDataset for level elements by locations
     * @param componentDataset Dataset containing level components and their positions
     */
    public void setComponentDataset(XYDataset componentDataset)
    {
        enemyChart.getXYPlot().setDataset(componentDataset);
    }

    public static void main(String[] args)
    {
        Level level = LevelGenerator.createLevel(300, 15, System.currentTimeMillis(), 3, LevelGenerator.TYPE_OVERGROUND);
        EvaluationInfo info = LevelTester.test(level, LevelTester.ASTAR_AGENT);

        XYDataset componentDataset = LevelStats.getLevelDistributionXY(level);
        XYDataset agentDataset = LevelStats.getAgentActionXY(info);
        
        StatsPanel statsPanel = new StatsPanel();
        JFrame frame = new JFrame("Title");
        frame.setContentPane(statsPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        statsPanel.setComponentDataset(componentDataset);
        statsPanel.setAgentDataset(agentDataset);

        LevelView.show(level);
    }
    
}
