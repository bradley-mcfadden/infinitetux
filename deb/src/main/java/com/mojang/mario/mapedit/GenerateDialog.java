package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.util.Logger;

/**
 * GenerateDialog creates a dialog that gathers user input
 * for the LevelGenerator.
 */
public class GenerateDialog extends JPanel 
    implements ActionListener, ChangeListener {

    public static final String NEW_LEVEL_LABEL = "New level";
    public static final String RECREATE_AREA_LABEL = "Recreate area of level";

    private JRadioButton newLevelRadio;
    private JRadioButton recreateAreaRadio;

    private JRadioButton undergroundButton;
    private JRadioButton overworldButton;
    private JRadioButton castleButton;

    private JSlider difficultySlider;
    private JLabel difficultyValue;

    private JTextField seedValue;

    private JSlider widthSlider;
    private JLabel widthValue;

    private JSlider heightSlider;
    private JLabel heightValue;

    private JSlider areaStartXSlider, areaStartYSlider, areaEndXSlider, areaEndYSlider;
    private JSlider startPlatXSlider, startPlatYSlider;
    private JLabel areaStartXValue, areaStartYValue, areaEndXValue, areaEndYValue;
    private JLabel startPlatXValue, startPlatYValue; 

    private JPanel cardPanel;

    private Results results;

    /**
     * Constructor.
     */
    private GenerateDialog()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        cardPanel = new JPanel(new CardLayout());

        cardPanel.add(buildNewLevelPanel(), NEW_LEVEL_LABEL);
        cardPanel.add(buildRecreateAreaPanel(), RECREATE_AREA_LABEL);

        // card layout would go here
        JPanel radioPanel = buildRadioPanel();
        // mainPanel.add(radioPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        
        setLayout(new GridLayout(0, 1));
        add(mainPanel);        
    }

    private JPanel buildRadioPanel()
    {
        FlowLayout radioLayout = new FlowLayout();
        radioLayout.setAlignment(FlowLayout.LEFT);
        JPanel radioPanel = new JPanel();
        newLevelRadio = new JRadioButton(NEW_LEVEL_LABEL);
        recreateAreaRadio = new JRadioButton(RECREATE_AREA_LABEL);

        radioPanel.add(newLevelRadio);
        radioPanel.add(recreateAreaRadio);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(newLevelRadio);
        modeGroup.add(recreateAreaRadio);

        newLevelRadio.setSelected(true);

        newLevelRadio.addActionListener(this);
        recreateAreaRadio.addActionListener(this);

        return radioPanel;
    }

    private JPanel buildNewLevelPanel()
    {
        JPanel newLevelPanel = new JPanel(new GridLayout(0, 2));

        JPanel leftColumn = new JPanel(new GridLayout(0, 1));
        JPanel rightColumn = new JPanel(new GridLayout(0, 1));

        newLevelPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        leftColumn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ButtonGroup typeGroup = new ButtonGroup();

        JPanel typePanel = new JPanel(new GridLayout(0, 1));
        typePanel.setBorder(BorderFactory.createTitledBorder("Type"));
        undergroundButton = new JRadioButton("Underworld");
        castleButton = new JRadioButton("Castle");
        overworldButton = new JRadioButton("Overworld");

        undergroundButton.setVerticalAlignment(SwingConstants.TOP);
        castleButton.setVerticalAlignment(SwingConstants.TOP);
        overworldButton.setVerticalAlignment(SwingConstants.TOP);

        overworldButton.setSelected(true);
        
        typeGroup.add(undergroundButton);
        typeGroup.add(castleButton);
        typeGroup.add(overworldButton);

        typePanel.add(overworldButton, true);
        typePanel.add(undergroundButton);
        typePanel.add(castleButton);

        leftColumn.add(typePanel);

        JPanel difficultyPanel = new JPanel(new BorderLayout());
        difficultyPanel.setBorder(BorderFactory.createTitledBorder("Difficulty"));
        difficultySlider = new JSlider(1, 20, 1);
        difficultySlider.setPaintTicks(true);
        difficultyValue = new JLabel(String.valueOf(difficultySlider.getValue()));
        
        difficultyPanel.add(difficultySlider, BorderLayout.CENTER);
        difficultyPanel.add(difficultyValue, BorderLayout.EAST);

        difficultySlider.setAlignmentY(SwingConstants.TOP);

        leftColumn.add(difficultyPanel);
        
        difficultySlider.addChangeListener(this);
        
        JPanel seedPanel = new JPanel(new GridLayout(0, 1));
        seedPanel.setBorder(BorderFactory.createTitledBorder("Seed"));

        seedValue = new JTextField();
        Random random = new Random();
        seedValue.setText(String.valueOf(random.nextLong()));
        seedValue.setAlignmentX(SwingConstants.TOP);
        seedValue.setPreferredSize(seedValue.getMinimumSize());

        seedPanel.add(seedValue);
        rightColumn.add(seedPanel);

        JPanel widthPanel = new JPanel(new BorderLayout());
        widthPanel.setBorder(BorderFactory.createTitledBorder("Width"));
        widthSlider = new JSlider(4, 512, 256);
        widthValue = new JLabel(String.valueOf(widthSlider.getValue()));

        widthPanel.add(widthSlider, BorderLayout.CENTER);
        widthPanel.add(widthValue, BorderLayout.EAST);

        JPanel heightPanel = new JPanel(new BorderLayout());
        heightPanel.setBorder(BorderFactory.createTitledBorder("Height"));
        heightSlider = new JSlider(2, 100, 15);
        heightValue = new JLabel(String.valueOf(heightSlider.getValue()));

        heightPanel.add(heightSlider, BorderLayout.CENTER);
        heightPanel.add(heightValue, BorderLayout.EAST);

        widthPanel.setAlignmentY(SwingConstants.TOP);
        heightPanel.setAlignmentY(SwingConstants.TOP);

        rightColumn.add(widthPanel);
        rightColumn.add(heightPanel);

        widthSlider.addChangeListener(this);
        heightSlider.addChangeListener(this);

        newLevelPanel.add(leftColumn);
        newLevelPanel.add(rightColumn);

        return newLevelPanel;
    }

    private JPanel buildRecreateAreaPanel()
    {
        JPanel recreateAreaPanel = new JPanel(new GridLayout(1, 2));
        JPanel areaPanel = new JPanel(new GridLayout(4, 1));
        areaPanel.setBorder(BorderFactory.createTitledBorder("Area Bounds"));

        BorderLayout layoutSAX = new BorderLayout();
        layoutSAX.setHgap(8);
        JPanel startAreaXPanel = new JPanel(layoutSAX);
        JLabel startAreaXLabel = new JLabel("Start X", SwingConstants.RIGHT);
        areaStartXSlider = new JSlider(0, 2, 1);
        areaStartXSlider.addChangeListener(this);
        areaStartXValue = new JLabel("0");

        startAreaXPanel.add(startAreaXLabel, BorderLayout.WEST);
        startAreaXPanel.add(areaStartXSlider, BorderLayout.CENTER);
        startAreaXPanel.add(areaStartXValue, BorderLayout.EAST);

        BorderLayout layoutSAY = new BorderLayout();
        layoutSAY.setHgap(8);
        JPanel startAreaYPanel = new JPanel(layoutSAY);
        JLabel startAreaYLabel = new JLabel("Start Y", SwingConstants.RIGHT);
        areaStartYSlider = new JSlider(0, 1, 0);
        areaStartYSlider.addChangeListener(this);
        areaStartYValue = new JLabel("0");

        startAreaYPanel.add(startAreaYLabel, BorderLayout.WEST);
        startAreaYPanel.add(areaStartYSlider, BorderLayout.CENTER);
        startAreaYPanel.add(areaStartYValue, BorderLayout.EAST);

        BorderLayout layoutEAX = new BorderLayout();
        layoutEAX.setHgap(8);
        JPanel endAreaXPanel = new JPanel(layoutEAX);
        JLabel endAreaXLabel = new JLabel("End X", SwingConstants.RIGHT);
        areaEndXSlider = new JSlider(0, 1, 0);
        areaEndXSlider.addChangeListener(this);
        areaEndXValue = new JLabel("0");

        endAreaXPanel.add(endAreaXLabel, BorderLayout.WEST);
        endAreaXPanel.add(areaEndXSlider, BorderLayout.CENTER);
        endAreaXPanel.add(areaEndXValue, BorderLayout.EAST);

        BorderLayout layoutEAY = new BorderLayout();
        layoutEAY.setHgap(8);
        JPanel endAreaYPanel = new JPanel(layoutEAY);
        JLabel endAreaYLabel = new JLabel("End Y", SwingConstants.RIGHT);
        areaEndYSlider = new JSlider(0, 1, 0);
        areaEndYSlider.addChangeListener(this);
        areaEndYValue = new JLabel("0");

        endAreaYPanel.add(endAreaYLabel, BorderLayout.WEST);
        endAreaYPanel.add(areaEndYSlider, BorderLayout.CENTER);
        endAreaYPanel.add(areaEndYValue, BorderLayout.EAST);
        
        areaPanel.add(startAreaXPanel);
        areaPanel.add(startAreaYPanel);
        areaPanel.add(endAreaXPanel);
        areaPanel.add(endAreaYPanel);
        
        JPanel startPlatPanel = new JPanel(new GridLayout(4, 1));
        startPlatPanel.setBorder(BorderFactory.createTitledBorder("Anchor position"));

        BorderLayout layoutSPX = new BorderLayout();
        layoutSPX.setHgap(8);
        JPanel startPlatXPanel = new JPanel(layoutSPX);
        JLabel startPlatXLabel = new JLabel("X", SwingConstants.RIGHT);
        startPlatXSlider = new JSlider(0, 1, 0);
        startPlatXSlider.addChangeListener(this);
        startPlatXValue = new JLabel("0");

        startPlatXPanel.add(startPlatXLabel, BorderLayout.WEST);
        startPlatXPanel.add(startPlatXSlider, BorderLayout.CENTER);
        startPlatXPanel.add(startPlatXValue, BorderLayout.EAST);

        BorderLayout layoutSPY = new BorderLayout();
        layoutSPY.setHgap(8);
        JPanel startPlatYPanel = new JPanel(layoutSPY);
        JLabel startPlatYLabel = new JLabel("Y", SwingConstants.RIGHT);
        startPlatYSlider = new JSlider(0, 1, 0);
        startPlatYSlider.addChangeListener(this);
        startPlatYValue = new JLabel("0");

        startPlatYPanel.add(startPlatYLabel, BorderLayout.WEST);
        startPlatYPanel.add(startPlatYSlider, BorderLayout.CENTER);
        startPlatYPanel.add(startPlatYValue, BorderLayout.EAST);

        startPlatPanel.add(startPlatXPanel);
        startPlatPanel.add(startPlatYPanel);

        // recreateAreaPanel.add(areaPanel);
        recreateAreaPanel.add(startPlatPanel);

        return recreateAreaPanel;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == difficultySlider)
        {
            difficultyValue.setText(String.valueOf(difficultySlider.getValue()));
        }
        if (e.getSource() == widthSlider)
        {
            widthValue.setText(String.valueOf(widthSlider.getValue()));
        }
        if (e.getSource() == heightSlider)
        {
            heightValue.setText(String.valueOf(heightSlider.getValue()));
        }
        if (e.getSource() == areaStartXSlider)
        {
            int value = areaStartXSlider.getValue();
            areaStartXValue.setText(String.valueOf(value));
            // if (areaEndXSlider.getValue() < value)
            //     areaEndXSlider.setValue(value);
            // areaEndXSlider.setMinimum(value);
            // if (startPlatXSlider.getValue() < value)
            //     startPlatXSlider.setValue(value);
            // startPlatXSlider.setMinimum(value);
        }
        if (e.getSource() == areaEndXSlider)
        {
            int value = areaEndXSlider.getValue();
            areaEndXValue.setText(String.valueOf(value));
            // if (areaStartXSlider.getValue() > value)
            //     areaStartXSlider.setValue(value);
            // areaStartXSlider.setMaximum(value);
            // if (startPlatXSlider.getValue() > value)
            //     startPlatXSlider.setValue(value);
            // startPlatXSlider.setMaximum(value);        
        }
        if (e.getSource() == areaStartYSlider)
        {
            int value = areaStartYSlider.getValue();
            areaStartYValue.setText(String.valueOf(value));
            // if (areaEndYSlider.getValue() < value)
            //     areaEndYSlider.setValue(value);
            // areaEndYSlider.setMinimum(value);
            // if (startPlatYSlider.getValue() < value)
            //     startPlatYSlider.setValue(value);
            // startPlatYSlider.setMinimum(value);     
        }
        if (e.getSource() == areaEndYSlider)
        {
            // areaEndYValue.setText(String.valueOf(areaEndYSlider.getValue()));
            int value = areaEndYSlider.getValue();
            areaEndYValue.setText(String.valueOf(value));
            // if (areaStartYSlider.getValue() > value)
            //     areaStartYSlider.setValue(value);
            // areaStartYSlider.setMaximum(value);
            // if (startPlatYSlider.getValue() > value)
            //     startPlatYSlider.setValue(value);
            // startPlatYSlider.setMaximum(value);    
        }
        if (e.getSource() == startPlatXSlider)
        {
            startPlatXValue.setText(String.valueOf(startPlatXSlider.getValue()));
        }
        if (e.getSource() == startPlatYSlider)
        {
            startPlatYValue.setText(String.valueOf(startPlatYSlider.getValue()));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JRadioButton button = (JRadioButton)e.getSource();   
        CardLayout cardLayout = (CardLayout)cardPanel.getLayout();
        if (button == newLevelRadio) 
        {
            if (newLevelRadio.isSelected())
                cardLayout.show(cardPanel, NEW_LEVEL_LABEL);
        }
        else if (button == recreateAreaRadio)
        {
            if (recreateAreaRadio.isSelected())
                cardLayout.show(cardPanel, RECREATE_AREA_LABEL);
        }
    }

    private void setMode(String mode)
    {
        if (NEW_LEVEL_LABEL.equals(mode))
        {
            // newLevelRadio.setSelected(true);
            newLevelRadio.doClick();
        }
        else if (RECREATE_AREA_LABEL.equals(mode))
        {
            //recreateAreaRadio.setSelected(true);
            recreateAreaRadio.doClick();
        }
        
    }

    private void setArea(Highlight area)
    {
        int sx = area.getX();
        int sy = area.getY();
        int ex = sx + area.getW();
        int ey = sy + area.getH();

        areaStartXSlider.setValue(sx);
        areaEndXSlider.setValue(ex);
        areaStartYSlider.setValue(sy);
        areaEndYSlider.setValue(ey);
        startPlatXSlider.setValue(ex);
        startPlatYSlider.setValue(ey - 1);
        startPlatXSlider.setMinimum(sx);
        startPlatXSlider.setMaximum(ex);
        startPlatYSlider.setMinimum(sy);
        startPlatYSlider.setMaximum(ex);
    }

    private void setLevelBounds(int width, int height)
    {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("width or height cannot be < 0");
        
        areaStartXSlider.setMaximum(width - 1);
        areaEndXSlider.setMaximum(width - 1);
        areaStartYSlider.setMaximum(height - 1);
        areaEndYSlider.setMaximum(height - 1);
    }

    private Results getResults()
    {
        results = new Results();
        if (newLevelRadio.isSelected())
        {
            if (undergroundButton.isSelected())
                results.type = LevelGenerator.TYPE_UNDERGROUND;
            else if (castleButton.isSelected())
                results.type = LevelGenerator.TYPE_CASTLE;
            else if (overworldButton.isSelected())
                results.type = LevelGenerator.TYPE_OVERGROUND;
            
            results.width = widthSlider.getValue();
            results.height = heightSlider.getValue();
            
            long seed = new Random().nextLong();
            try {
                seed = Long.parseLong(seedValue.getText().trim());
            } catch (NumberFormatException ne) {}

            results.seed = seed;
            results.difficulty = difficultySlider.getValue();
            results.mode = NEW_LEVEL_LABEL;
        }
        else
        {
            int sx = areaStartXSlider.getValue();
            int sy = areaStartYSlider.getValue();
            int ex = areaEndXSlider.getValue();
            int ey = areaEndYSlider.getValue();
            Highlight toRegen = new Highlight(sx, sy, ex-sx+1, ey-sy+1, Highlight.BLUE, "");
            results.area = toRegen;
            results.mode = RECREATE_AREA_LABEL;
            results.startPlatX = startPlatXSlider.getValue();
            results.startPlatY = startPlatYSlider.getValue();
        }
        return results;
    }

    /**
     * Results stores the parameters for level generation.
     */
    public class Results {
        public int type;
        public int difficulty;
        public long seed;
        public int width;
        public int height;
        public Highlight area;
        public int startPlatX;
        public int startPlatY;
        public String mode;
    }

    /**
     * getDialog creates a GenerateDialog and shows it.
     * @return Results object. null if cancelled or closed. MODE == NEW_LEVEL_LABEL
     */
    public static Results getDialog()
    {
        GenerateDialog dialog = new GenerateDialog();
        int ret = JOptionPane.showConfirmDialog(null, dialog, "Generate Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.CANCEL_OPTION) 
        {
            return null;
        } 
        else 
        {
            return dialog.getResults();
        }
    }

    /**
     * getDialog This version returns a dialog that allows the user to configure 
     * the initial anchor point when regenerating an area.
     * @param area The area to be re-generated
     * @param levelWidth Width of the level, in tiles
     * @param levelHeight Height of the level, in tiles
     * @return Results with startPlatX, startPlatY, area, and MODE == RECREATE_LEVEL_LABEL
     */
    public static Results getDialog(Highlight area, int levelWidth, int levelHeight)
    {
        GenerateDialog dialog = new GenerateDialog();
        dialog.setLevelBounds(levelWidth, levelHeight);
        dialog.setArea(area);
        dialog.setMode(GenerateDialog.RECREATE_AREA_LABEL);
        // 
        int ret = JOptionPane.showConfirmDialog(null, dialog, "Generate Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret == JOptionPane.CLOSED_OPTION || ret == JOptionPane.CANCEL_OPTION) 
        {
            return null;
        } 
        else 
        {
            return dialog.getResults();
        }
    }

    public static void main(String[] args)
    {
        Logger.setLevel(Logger.LEVEL_DEBUG);
        Results results = getDialog(new Highlight(20, 5, 20, 10, Highlight.BLUE, ""), 300, 15);
        if (results != null)
        {
            if (results.mode.equals(GenerateDialog.NEW_LEVEL_LABEL))
            {
                System.out.printf("%d %d %d %d %d\n", results.type, results.difficulty, results.seed, results.width, results.height);
        
            }
            else if (results.mode.equals(GenerateDialog.RECREATE_AREA_LABEL))
            {
                Highlight area = results.area;
                System.out.printf("%d %d %d %d %d %d\n", area.getX(), area.getY(), area.getW(), area.getH(), results.startPlatX, results.startPlatY);
            }
        }
        else
        {
            System.out.println("Results are null");
        }
    }
}
