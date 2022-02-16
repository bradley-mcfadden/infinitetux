package com.mojang.mario.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;
import java.util.ArrayList;
import com.mojang.mario.*;
import com.mojang.mario.level.*;
import com.mojang.mario.mapedit.ChunkLibraryPanel.SelectionChangedListener;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.Platform;
import com.mojang.mario.sprites.Sprite;


/**
 * LevelEditView provides a graphic representation of the level being built.
 */
public class LevelEditView extends JComponent 
    implements MouseListener, MouseMotionListener, SelectionChangedListener
{
    private static final long serialVersionUID = -7696446733303717142L;

    private LevelRenderer levelRenderer;
    private Level level;
    private ActionCompleteListener actionCompleteListener;

    private int xTile = -1;
    private int yTile = -1;
    private TilePicker tilePicker;
    private EnemyPicker enemyPicker;
    private HazardPicker hazardPicker;
    private int editingMode = LevelEditor.MODE_TILE;

    private ArrayList<Highlight> highlights;
    private Highlight lastSelect;
    private Highlight chunkTarget;
    private int lastXTile, lastYTile;

    private LevelView selectedChunk;

    /**
     * Constructor.
     */
    public LevelEditView()
    {
        level = new Level(256, 15);
        highlights = new ArrayList<>();
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
        setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
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

    /**
     * getSelected retrieves the current Highlight
     * @returns Current Highlighted area, or null.
     */
    public Highlight getSelected()
    {
        return lastSelect;
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
        for (Highlight h : highlights)
        {
            h.draw(g);
        }
        g.setColor(Color.BLACK);
        g.drawRect(xTile * 16 - 1, yTile * 16 - 1, 17, 17);
    }

    public void mouseClicked(MouseEvent e)
    {
        int xT = e.getX() / 16;
        int yT = e.getY() / 16;
        if (editingMode == LevelEditor.MODE_PLACE_CHUNK)
        {
            if (selectedChunk != null)
            {
                Level chnk = selectedChunk.getLevel();
                level.setArea(chnk, xT, yT);
                notifyListener();
                levelRenderer.repaint(xT, yT, Math.min(level.width, xT+chnk.width), Math.min(level.height, yT+chnk.height));
            }
        }
    }

    public void mouseEntered(MouseEvent e)
    {
        int xT = e.getX() / 16;
        int yT = e.getY() / 16;
        if (editingMode == LevelEditor.MODE_PLACE_CHUNK)
        {
            if (selectedChunk != null)
            {
                Level level = selectedChunk.getLevel();
                chunkTarget = addHighlight(xT, yT, level.width, level.height, Highlight.GREEN, "");
            }
        }
    }

    public void mouseExited(MouseEvent e)
    {
        xTile = -1;
        yTile = -1;
        if (editingMode == LevelEditor.MODE_PLACE_CHUNK)
        {
            removeHighlight(chunkTarget);
        }
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
            level.setSpriteTemplate(xTile, yTile, enemyPicker.pickedEnemy);
            levelRenderer.repaint(xTile - 2, yTile - 2, 5, 5);
            repaint();
        }
        else if (editingMode == LevelEditor.MODE_HAZARD)
        {
            if (hazardPicker.pickedHazard.getType() == Enemy.ENEMY_NULL)
            {
                level.removeHazard(xTile, yTile);
            }
            else
            {
                Sprite hazard = hazardPicker.pickedHazard.sprite;
                
                if (hazard instanceof Platform) 
                {
                    Platform platform = (Platform)hazard;
                    platform.setPosition(xTile * 16, yTile * 16);
                    SpriteTemplate hazardTemplate = new SpriteTemplate(platform.copy());
                    level.addHazard(hazardTemplate);
                }
            }
            levelRenderer.repaint(0, 0, level.width, level.height);
            repaint();
        }
        else if (editingMode == LevelEditor.MODE_SELECT)
        {
            lastXTile = xTile;
            lastYTile = yTile;
            clearSelection();
            setSelection(xTile, yTile, 1, 1);
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        if (editingMode == LevelEditor.MODE_TILE || editingMode == LevelEditor.MODE_ENEMY || editingMode == LevelEditor.MODE_HAZARD)
        {    
            notifyListener();
        }
        else if (editingMode == LevelEditor.MODE_SELECT)
        {
            
        }
        requestFocusInWindow();
    }

    /**
     * For placing elements, calls mousePressed. 
     * In MODE_SELECT, drags a select box around.
     */
    public void mouseDragged(MouseEvent e)
    {
        if (editingMode == LevelEditor.MODE_TILE || editingMode == LevelEditor.MODE_ENEMY || editingMode == LevelEditor.MODE_HAZARD)
        {
            mousePressed(e);
        }
        else if (editingMode == LevelEditor.MODE_SELECT)
        {
            int xT = e.getX() / 16;
            int yT = e.getY() / 16;

            if (lastSelect != null)
            {
                int x = lastSelect.getX();
                int w = lastSelect.getW();
                int y = lastSelect.getY();
                int h = lastSelect.getH();
                if (xT != lastXTile)
                {
                    if (x - xT <= 0) 
                    {
                        int newX = Math.min(x, xT);
                        int newW = Math.abs(x - xT) + 1;
                        if (newX + newW > level.width)
                        {
                            newW = level.height - newX;
                        }
                        lastSelect.setX(newX);
                        lastSelect.setW(newW);
                    }
                    else
                    {
                        int rx = x + w;
                        int newX = xT;
                        if (newX < 0) 
                        {
                            newX = 0;
                        }
                        int newW = rx - newX;

                        lastSelect.setX(newX);
                        lastSelect.setW(newW);
                    }
                }
                if (yT != lastYTile)
                {
                    if (y - yT <= 0)
                    {
                        int newY = Math.min(y, yT);
                        int newH = Math.abs(y - yT) + 1;
                        if (newY + newH > level.height)
                        {
                            newH = level.height - newY;
                        }

                        lastSelect.setY(newY);
                        lastSelect.setH(newH);
                    }
                    else
                    {
                        int ry = y + h;
                        int newY = yT;
                        if (newY < 0) 
                        {
                            newY = 0;
                        }
                        int newH = ry - newY;
                        lastSelect.setY(newY);
                        lastSelect.setH(newH);
                    }
                }
                if (xT != lastXTile || yT != lastYTile)
                {
                    x = lastSelect.getX();
                    y = lastSelect.getY();
                    w = lastSelect.getW();
                    h = lastSelect.getH();
                    lastSelect.setMessage(String.format("%d,%d to %d,%d", x, y, x + w, y + h));
                    lastXTile = xT;
                    lastYTile = yT;
                    repaint();
                }
            }
        }
    }

    public void mouseMoved(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        ((LevelEditor)this.getRootPane().getParent()).setCoordinates(xTile, yTile);
       
        if (editingMode == LevelEditor.MODE_PLACE_CHUNK)
        {
            if (chunkTarget != null)
            {
                chunkTarget.setX(xTile);
                chunkTarget.setY(yTile);
            }
        }
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
        if (editingMode != LevelEditor.MODE_SELECT) 
        {
            clearSelection();
        }
    }

    /**
     * Resize to match current level size.
     */
    public void resize()
    {
        Dimension size = new Dimension(level.width * 16, level.height * 16);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        repaint();
    }

    /**
     * Notifies actionComplteListener that an editor action just finished.
     */
    private void notifyListener()
    {
        if (actionCompleteListener != null)
        {
            actionCompleteListener.onActionComplete();
        }
    }

    /**
     * setActionCompleteListener sets the callback for editor actions
     * @param listener
     */
    public void setActionCompleteListener(ActionCompleteListener listener)
    {
        actionCompleteListener = listener;
    }

    /**
     * addHighlight to the list of Highlights to be displayed.
     * @param x x tile pos of left corner
     * @param y y tile pos of left corner
     * @param w width of Highlight in tiles
     * @param h height of Highlight in tiles
     * @param color Color of Highlight
     * @param message message to display in Highlight
     * @return Highlight object
     */
    public Highlight addHighlight(int x, int y, int w, int h, Color color, String message)
    {
        Highlight hl = new Highlight(x, y, w, h, color, message);
        highlights.add(hl);
        return hl;
    }

    /**
     * removeHighlight from list of Highlight to draw.
     * @param hl Highlight to remove.
     */
    public void removeHighlight(Highlight hl)
    {
        highlights.remove(hl);
    }

    /**
     * setSelection sets the current selection area.
     * @param x x tile of selection to set
     * @param y y tile of selection to set
     * @param w w in tiles of selection
     * @param h h in tiles of selection
     */
    public void setSelection(int x, int y, int w, int h)
    {
        clearSelection();
        lastSelect = addHighlight(x, y, w, h, Highlight.YELLOW, String.format("%d,%d to %d,%d", x, y, x+w, y+h));
    }

    /**
     * clearSelection removes the last selection.
     */
    public void clearSelection()
    {
        if (lastSelect != null)
        {
            removeHighlight(lastSelect);
            lastSelect = null;
        }
    }

    @Override
    public void onSelectionChanged(LevelView selection) {
        selectedChunk = selection;

        if (selection == null)
        {
            removeHighlight(chunkTarget);
        }
    }

    /**
     * Callback interface for completed actions
     */
    public interface ActionCompleteListener
    {
        /**
         * Callback method
         */
        void onActionComplete();
    }
}