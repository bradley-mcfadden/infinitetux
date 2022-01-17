package com.mojang.mario.mapedit;

import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.level.SpriteTemplate;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


public class HazardPicker extends JPanel implements ActionListener {

    public SpriteTemplate pickedHazard;

    private JRadioButton platformRadio;
    private JRadioButton noneRadio;
    private JPanel propertiesPanel;
    private PlatformPanel platformPanel;
    private JPanel nonePanel;
    private LevelEditor editor;

    private static final String PLATFORM = "Platform";
    private static final String NONE = "None";

    public HazardPicker(LevelEditor editor)
    {
        super(new BorderLayout());
        this.editor = editor;
        pickedHazard = new SpriteTemplate(Enemy.ENEMY_NULL, false);
        add(buildHazardPanel(), BorderLayout.WEST);
        add(buildPropertiesPanel(), BorderLayout.CENTER);
        setBorder(new TitledBorder(new EtchedBorder(), "Hazard picker"));
    }

    private JPanel buildHazardPanel()
    {
        JPanel parentPanel = new JPanel(new BorderLayout());
        JPanel hazardPanel = new JPanel(new GridLayout(0, 1));
        
        hazardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        platformRadio = new JRadioButton(PLATFORM);
        noneRadio = new JRadioButton(NONE);

        platformRadio.setVerticalAlignment(SwingConstants.TOP);
        noneRadio.setVerticalAlignment(SwingConstants.TOP);

        hazardPanel.add(platformRadio);
        hazardPanel.add(noneRadio);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(platformRadio);
        buttonGroup.add(noneRadio);

        platformRadio.addActionListener(this);
        noneRadio.addActionListener(this);

        noneRadio.setSelected(true);

        parentPanel.add(hazardPanel, BorderLayout.NORTH);
        return parentPanel;
    }

    private JPanel buildPropertiesPanel()
    {
        propertiesPanel = new JPanel(new FlowLayout());
        propertiesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        platformPanel = new PlatformPanel(this);
        platformPanel.setVisible(false);
        nonePanel = new JPanel();
        propertiesPanel.add(platformPanel);
        propertiesPanel.add(nonePanel);

        return propertiesPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        for (Component c : propertiesPanel.getComponents())
        {
            c.setVisible(false);
        }
        Object o = e.getSource();
        if (o == platformRadio)
        {
            platformPanel.setVisible(true);
            pickedHazard = platformPanel.getSpriteTemplate();
            editor.setEditingMode(LevelEditor.MODE_HAZARD);
        }
        else if (o == noneRadio)
        {
            nonePanel.setVisible(true);
            pickedHazard = new SpriteTemplate(Enemy.ENEMY_NULL, false);
        }
    }

    public void updatePickedHazard(SpriteTemplate pickedHazard)
    {

    }
}
