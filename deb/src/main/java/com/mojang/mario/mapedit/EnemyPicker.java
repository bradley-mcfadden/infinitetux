package com.mojang.mario.mapedit;

import com.mojang.mario.level.SpriteTemplate;
import com.mojang.mario.sprites.Enemy;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class EnemyPicker extends JPanel implements ActionListener{
    private static final String RED_KOOPA = "Red Koopa";
    private static final String GREEN_KOOPA = "Green Koopa";
    private static final String GOOMBA = "Goomba";
    private static final String SPIKY = "Spiky";
    private static final String FLOWER = "Flower";
    private static final String THWOMP = "Thwomp";
    private static final String NONE = "None";

    private JRadioButton redKoopaButton;
    private JRadioButton greenKoopaButton;
    private JRadioButton goombaButton;
    private JRadioButton spikyButton;
    private JRadioButton flowerButton;
    private JRadioButton thwompButton;
    private JRadioButton noneButton;

    private JCheckBox wingedButton;

    private LevelEditor editor;

    public SpriteTemplate pickedEnemy;

    public EnemyPicker(LevelEditor editor)
    {
        super(new BorderLayout());
        this.editor = editor;
        pickedEnemy = new SpriteTemplate(Enemy.ENEMY_NULL, false);
        add(buildEnemyPanel(), BorderLayout.WEST);
        add(buildPropertyPanel(), BorderLayout.CENTER);
        setBorder(new TitledBorder(new EtchedBorder(), "Enemy picker"));
    }

    private JPanel buildEnemyPanel()
    {
        
        JPanel enemyPanel = new JPanel(new GridLayout(0, 1));
        enemyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        redKoopaButton = new JRadioButton(RED_KOOPA);
        greenKoopaButton = new JRadioButton(GREEN_KOOPA);
        goombaButton = new JRadioButton(GOOMBA);
        spikyButton = new JRadioButton(SPIKY);
        flowerButton = new JRadioButton(FLOWER);
        thwompButton = new JRadioButton(THWOMP);
        noneButton = new JRadioButton(NONE);

        redKoopaButton.setVerticalAlignment(SwingConstants.TOP);
        greenKoopaButton.setVerticalAlignment(SwingConstants.TOP);
        goombaButton.setVerticalAlignment(SwingConstants.TOP);
        spikyButton.setVerticalAlignment(SwingConstants.TOP);
        flowerButton.setVerticalAlignment(SwingConstants.TOP);
        thwompButton.setVerticalAlignment(SwingConstants.TOP);
        noneButton.setVerticalAlignment(SwingConstants.TOP);

        enemyPanel.add(redKoopaButton);
        enemyPanel.add(greenKoopaButton);
        enemyPanel.add(goombaButton);
        enemyPanel.add(spikyButton);
        enemyPanel.add(flowerButton);
        enemyPanel.add(thwompButton);
        enemyPanel.add(noneButton);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(redKoopaButton);
        buttonGroup.add(greenKoopaButton);
        buttonGroup.add(goombaButton);
        buttonGroup.add(spikyButton);
        buttonGroup.add(flowerButton);
        buttonGroup.add(thwompButton);
        buttonGroup.add(noneButton);

        redKoopaButton.addActionListener(this);
        greenKoopaButton.addActionListener(this);
        goombaButton.addActionListener(this);
        spikyButton.addActionListener(this);
        flowerButton.addActionListener(this);
        thwompButton.addActionListener(this);
        noneButton.addActionListener(this);

        noneButton.setSelected(true);

        return enemyPanel;
    }

    private JPanel buildPropertyPanel() 
    {
        JPanel propertyPanel = new JPanel(new GridLayout(0, 1));
        propertyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        wingedButton = new JCheckBox("Winged");
        
        wingedButton.setVerticalAlignment(SwingConstants.TOP);
        
        propertyPanel.add(wingedButton);
        wingedButton.addActionListener(this);
        return propertyPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        editor.setEditingMode(LevelEditor.MODE_ENEMY);
        boolean winged = wingedButton.isSelected();
        if (e.getSource() == redKoopaButton)
        {
            pickedEnemy = new SpriteTemplate(Enemy.ENEMY_RED_KOOPA, winged);
        }
        else if (e.getSource() == greenKoopaButton)
        {
            pickedEnemy = new SpriteTemplate(Enemy.ENEMY_GREEN_KOOPA, winged);
        }
        else if (e.getSource() == goombaButton)
        {
            pickedEnemy = new SpriteTemplate(Enemy.ENEMY_GOOMBA, winged);
        }
        else if (e.getSource() == spikyButton)
        {
            pickedEnemy = new SpriteTemplate(Enemy.ENEMY_SPIKY, winged);
        }
        else if (e.getSource() == flowerButton)
        {
            pickedEnemy = new SpriteTemplate(Enemy.ENEMY_FLOWER, winged);
        }
        else if (e.getSource() == thwompButton)
        {
            pickedEnemy = new SpriteTemplate(Enemy.ENEMY_THWOMP, winged);
        }
        else if (e.getSource() == noneButton)
        {
            pickedEnemy = new SpriteTemplate(Enemy.ENEMY_NULL, winged);
        }
        
    }
}
