package com.mojang.mario.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.mojang.mario.Art;
import com.mojang.mario.LevelRenderer;
import com.mojang.mario.level.Level;
import com.mojang.mario.level.LevelGenerator;

/**
 * LevelView is a view-only render of a Level.
 * It provides some event handling.
 */
// TODO: Documentation, testing
public class LevelView extends JComponent 
    implements MouseListener {

    private LevelRenderer levelRenderer;
    private Level level;
    private List<Highlight> highlights;
    private ClickListener listener;

    /**
     * Constructor.
     * @param level Level to provide a view of. Should not be null.
     * @throws NullPointerException if level is null.
     */
    public LevelView(Level level)
    {
        this.level = level;
        Dimension size = new Dimension(level.width * 16, level.height * 16);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        repaint();

        addMouseListener(this);

        highlights = new ArrayList<>();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        Art.init(getGraphicsConfiguration(), null);
        levelRenderer = new LevelRenderer(level, getGraphicsConfiguration(), level.width * 16, level.height * 16);
        levelRenderer.renderBehaviors = false;
        levelRenderer.setIsLevelEditor(true);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(new Color(0x8090ff));
        g.fillRect(0, 0, level.width * 16, level.height * 16);
        levelRenderer.render(g, 0, 0);
        g.setColor(Color.BLACK);
        for (Highlight hl : highlights)
        {
            hl.draw(g);
        }
    }

    /**
     * setHighlight will highlight this component by painting over it
     * with a semi-transparent yellow color.
     */
    public void setHighlight()
    {
        highlights.add(new Highlight(0, 0, level.width, level.height, Highlight.YELLOW, ""));
        repaint();
    }

    /**
     * clear any highlight on this component.
     */
    public void clearHighlight()
    {
        highlights.clear();
        repaint();
    }

    /**
     * getLevel the level this view renders
     * @return the level this view renders
     */
    public Level getLevel()
    {
        return level;
    }

    /**
     * setClickListener to receive callbacks from this item.
     * @param listener ClickListener that will receive callbacks.
     */
    public void setClickListener(ClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void mouseClicked(MouseEvent e) 
    {
    }

    @Override
    public void mousePressed(MouseEvent e) 
    {
        if (listener != null) listener.clickPerformed(this);
    }

    @Override
    public void mouseReleased(MouseEvent e) 
    {        
    }

    @Override
    public void mouseEntered(MouseEvent e) 
    {        
    }

    @Override
    public void mouseExited(MouseEvent e) 
    {        
    }

    /**
     * save the chunk into the given directory.
     * @param chunkFolderName Directory to save chunk in, should exist.
     */
    public void save(File chunkFolderName)
    {
        try {
            level.save(chunkFolderName);
            level.xExit = level.yExit = -1;
        } catch (IOException ie) {
            System.err.println("Unable to save chunk at " + chunkFolderName.getName());
            ie.printStackTrace();
        }
    }

    /**
     * load a chunk from the directory.
     * @param chunkFolderName Directory to load chunk from, should exist.
     * @return Loaded chunk, or null if an error occurred.
     */
    public static LevelView load(File chunkFolderName)
    {
        LevelView chunk = null;
        try {
            chunk = new LevelView(Level.load(chunkFolderName));
            chunk.getLevel().xExit = -1;
            chunk.getLevel().yExit = -1;
        } catch (IOException ie) {
            System.err.println("Unable to load chunk at " + chunkFolderName.getName());
            ie.printStackTrace();
        }
        return chunk;
    }

    /**
     * ClickListener provides a callback for objects interested
     * in being notified when this component is clicked.
     */
    public interface ClickListener {
        /**
         * clickPerformed is the callback for source being clicked.
         * @param source What was clicked?
         */
        void clickPerformed(LevelView source);
    }

    public static void main(String[] args)
    {
        Level level = LevelGenerator.createLevel(300, 15, 15L, 3, LevelGenerator.TYPE_OVERGROUND);
        level = level.getArea(30, 0, 15, 15);
        JFrame frame = new JFrame("Test");
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new LevelView(level));
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
