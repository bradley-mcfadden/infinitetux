package com.mojang.mario.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;

import com.mojang.mario.*;

/**
 * TilePicker displays a grid of tiles, and allows the user to choose
 * from them.
 */
// TODO: Anchor point
public class TilePicker extends JComponent implements MouseListener, MouseMotionListener
{
    private static final long serialVersionUID = -7696446733303717142L;

    private int xTile = -1;
    private int yTile = -1;
    
    public byte pickedTile;

    @SuppressWarnings("unused")
	private byte paint = 0;
    private LevelEditor tilePickChangedListener;

    /**
     * Constructor.
     */
    public TilePicker()
    {
        Dimension size = new Dimension(256, 256);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Constructor.
     * @param editor Listener for changed tile selection.
     */
    public TilePicker(LevelEditor editor)
    {
        this();
        addTilePickChangedListener(editor);
    }

    /**
     * @see JComponent.addNotify
     */
    public void addNotify()
    {
        super.addNotify();
        Art.init(getGraphicsConfiguration(), null);
    }

    /**
     * Paints the component.
     * Display is a grid of tiles.
     */
    public void paintComponent(Graphics g)
    {
        g.setColor(new Color(0x8090ff));
        g.fillRect(0, 0, 256, 256);
        
        for (int x=0; x<16; x++)
            for (int y=0; y<16; y++)
            {
                g.drawImage(Art.level[x][y], (x << 4), (y << 4), null);
            }

        g.setColor(Color.WHITE);
        int xPickedTile = (pickedTile&0xff)%16;
        int yPickedTile = (pickedTile&0xff)/16;
        g.drawRect(xPickedTile * 16, yPickedTile * 16, 15, 15);

        g.setColor(Color.BLACK);
        g.drawRect(xTile * 16 - 1, yTile * 16 - 1, 17, 17);
    }

    /**
     * Changes this to be the active picker.
     */
    public void mouseClicked(MouseEvent e)
    {
        if (tilePickChangedListener != null)
            tilePickChangedListener.setEditingMode(LevelEditor.MODE_TILE);
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
     * Set picked tile, and update current xTile and yTile.
     */
    public void mousePressed(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        
        setPickedTile((byte)(xTile+yTile*16));
        repaint();
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * Update xTile and yTile on mouse drag.
     */
    public void mouseDragged(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;

        repaint();
    }

    /**
     * Update xTile and yTile on mouse move.
     */
    public void mouseMoved(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        repaint();
    }

    /**
     * Notify parent that pickedTile was updated.
     * @param block
     */
    public void setPickedTile(byte block)
    {
        pickedTile = block;
        repaint();
        if (tilePickChangedListener!=null)
            tilePickChangedListener.setPickedTile(pickedTile);
    }

    /**
     * Add a listener for pickedTile changes.
     * @param editor
     */
    public void addTilePickChangedListener(LevelEditor editor)
    {
        this.tilePickChangedListener = editor;
        if (tilePickChangedListener!=null)
            tilePickChangedListener.setPickedTile(pickedTile);
    }
}