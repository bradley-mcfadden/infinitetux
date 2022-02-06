package com.mojang.mario.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;
import com.mojang.mario.*;
import com.mojang.mario.level.*;
import com.mojang.mario.sprites.Platform;
import com.mojang.mario.sprites.Sprite;


/**
 * LevelEditView provides a graphic representation of the level being built.
 */
public class LevelEditView extends JComponent 
    implements MouseListener, MouseMotionListener
{
    private static final long serialVersionUID = -7696446733303717142L;

    private LevelRenderer levelRenderer;
    private Level level;

    private int xTile = -1;
    private int yTile = -1;
    private TilePicker tilePicker;
    private EnemyPicker enemyPicker;
    private HazardPicker hazardPicker;
    private int editingMode = LevelEditor.MODE_TILE;


    /**
     * Constructor.
     */
    public LevelEditView()
    {
        level = new Level(256, 15);
        Dimension size = new Dimension(level.width * 16, level.height * 16);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Constructor.
     * @param enemyPicker EnemyPicker to use with this LevelEditView.
     * @param tilePicker TilePicker to use with this LevelEditView.
     * @param hazardPicker HazardPicker to use with this LevelEditView.
     */
    public LevelEditView(EnemyPicker enemyPicker, TilePicker tilePicker, HazardPicker hazardPicker)
    {
        this();

        this.enemyPicker = enemyPicker;
        this.tilePicker = tilePicker;
        this.hazardPicker = hazardPicker;
    }
    
    /**
     * setLevel being displayed by the view.
     * @param level Level to display in the view. Should not be null.
     */
    public void setLevel(Level level)
    {
        this.level = level;
        Dimension size = new Dimension(level.width * 16, level.height * 16);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        repaint();
        levelRenderer.setLevel(level);
    }
    
    /**
     * getLevel being displayed.
     * @return Level being displayed.
     */
    public Level getLevel()
    {
        return level;
    }

    public void addNotify()
    {
        super.addNotify();
        Art.init(getGraphicsConfiguration(), null);
        levelRenderer = new LevelRenderer(level, getGraphicsConfiguration(), level.width * 16, level.height * 16);
        levelRenderer.renderBehaviors = true;
        levelRenderer.setIsLevelEditor(true);
    }

    public void paintComponent(Graphics g)
    {
        g.setColor(new Color(0x8090ff));
        g.fillRect(0, 0, level.width * 16, level.height * 16);
        levelRenderer.render(g, 0, 0);
        g.setColor(Color.BLACK);
        g.drawRect(xTile * 16 - 1, yTile * 16 - 1, 17, 17);
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
        xTile = -1;
        yTile = -1;
        repaint();
    }

    /**
     * Depending on editing mode, perform some action on mouse press.
     */
    public void mousePressed(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;

        if (editingMode == LevelEditor.MODE_TILE)
        {
            if (e.getButton() == 3)
            {
                tilePicker.setPickedTile(level.getBlock(xTile, yTile));
            }
            else
            {
                System.out.println("Picked tile " + tilePicker.pickedTile + " at " + xTile + " " + yTile);
                if (tilePicker.pickedTile == (byte)-1) 
                {
                    int xExitOld = level.xExit;
                    int yExitOld = level.yExit;
                    level.setBlock(xTile, yTile, tilePicker.pickedTile);
                    levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);
                    levelRenderer.repaint(xExitOld - 1, yExitOld - 2, 3, 3);
                }
                else
                {
                    level.setBlock(xTile, yTile, tilePicker.pickedTile);
                    levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);
                }

                repaint();
            }
        } 
        else if (editingMode == LevelEditor.MODE_ENEMY)
        {
            System.out.println("Placed enemy at " + xTile + "," + yTile);
            level.setSpriteTemplate(xTile, yTile, enemyPicker.pickedEnemy);
            levelRenderer.repaint(xTile - 2, yTile - 2, 5, 5);

            repaint();

        }
        else if (editingMode == LevelEditor.MODE_HAZARD)
        {
            System.out.println("Placed hazard at " + xTile + "," + yTile);
            Sprite hazard = hazardPicker.pickedHazard.sprite;
            
            if (hazard instanceof Platform) 
            {
                Platform platform = (Platform)hazard;
                platform.setPosition(xTile * 16, yTile * 16);
                SpriteTemplate hazardTemplate = new SpriteTemplate(platform.copy());
                level.addHazard(hazardTemplate);
            }
            else
            {

            }

            levelRenderer.repaint(0, 0, level.width, level.height);

            repaint();
        }
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * @note Should probably get rid of this.
     */
    public void mouseDragged(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;

        level.setBlock(xTile, yTile, tilePicker.pickedTile);
        levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);

        repaint();
    }

    public void mouseMoved(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        ((LevelEditor)this.getRootPane().getParent()).setCoordinates(xTile, yTile);
       
        repaint();
    }

    /**
     * setEnemyPicker to argument.
     * @param enemyPicker
     */
    public void setEnemyPicker(EnemyPicker enemyPicker) {
        this.enemyPicker = enemyPicker;
    }

    /**
     * setHazardPicker to argument.
     * @param hazardPicker
     */
    public void setHazardPicker(HazardPicker hazardPicker) {
        this.hazardPicker = hazardPicker;
    }

    /**
     * setTilePicker to argument.
     * @param tilePicker
     */
    public void setTilePicker(TilePicker tilePicker) {
        this.tilePicker = tilePicker;
    }

    /**
     * setEditingMode to change how LevelEditView reacts to user input.
     * @param mode One of MODE_ENEMY, MODE_TILE, MODE_HAZARD.
     */
    public void setEditingMode(int mode)
    {
        editingMode = mode;
    }
}