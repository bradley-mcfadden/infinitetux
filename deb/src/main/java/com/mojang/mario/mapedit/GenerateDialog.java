package com.mojang.mario.mapedit;

import java.awt.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mojang.mario.level.LevelGenerator;

/**
 * GenerateDialog creates a dialog that gathers user input
 * for the LevelGenerator.
 */
public class GenerateDialog extends JPanel 
    implements ChangeListener {
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

    private Results results;

    /**
     * Constructor.
     */
    private GenerateDialog()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = new JPanel(new GridLayout(0, 2));
        JPanel leftColumn = new JPanel(new GridLayout(0, 1));
        JPanel rightColumn = new JPanel(new GridLayout(0, 1));

        contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
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

        contentPanel.add(leftColumn);
        contentPanel.add(rightColumn);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setLayout(new GridLayout(0, 1));
        add(mainPanel);        
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
    }

    private Results getResults()
    {
        results = new Results();
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
    }

    /**
     * getDialog creates a GenerateDialog and shows it.
     * @return Results object. null if cancelled or closed.
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

    public static void main(String[] args)
    {
        Results results = getDialog();
        if (results != null)
        {
            System.out.printf("%d %d %d %d %d\n", results.type, results.difficulty, results.seed, results.width, results.height);
        }
        else
        {
            System.out.println("Results are null");
        }
    }
}
