package com.mojang.mario.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.mojang.mario.level.ChunkLibrary;
import com.mojang.mario.level.Level;
import com.mojang.mario.level.LevelGenerator;

/**
 * ChunkLibraryPanel manages a list of "chunks", or Level
 * sections that the user can interact with.
 * 
 * The list of chunks can be modified with the addChunk()
 * and removeChunk() methods.
 */
public class ChunkLibraryPanel extends JPanel 
    implements ActionListener, ChunkLibrary.LoadingFinishedListener, LevelView.ClickListener {
    
    private JPanel chunkPanel;
    private JButton closeButton;
    private JButton addButton;
    private JButton removeButton;
    private JButton tagButton;

    private List<LevelView> chunks;
    private LevelView currentSelection;
    private SelectionChangedListener selectionChangedListener;
    private LevelEditor editor;

    private boolean areChunksLoaded;
    private int chunkPanelOffset;
    
    /**
     * Constructor.
     */
    public ChunkLibraryPanel()
    {
        chunkPanelOffset = 8;
        chunks = new ArrayList<>();
        buildLayout();
    }

    /**
     * Constructor that sets a reference to LevelEditor.
     * @param editor LevelEditor for the purpose of callbacks.
     */
    public ChunkLibraryPanel(LevelEditor editor)
    {
        this();
        this.editor = editor;
    }

    private void buildLayout()
    {
        JPanel topPanel = new JPanel();
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Chunk Library");
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        JLabel spacer = new JLabel("    \n    ");
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        closeButton = new JButton("X");

        chunkPanel = new JPanel(null);
        chunkPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane chunkPane = new JScrollPane(chunkPanel);
        chunkPane.setMaximumSize(new Dimension(20 * 16, Integer.MAX_VALUE));
        chunkPane.setPreferredSize(new Dimension(20 * 16, Integer.MAX_VALUE));

        titlePanel.add(BorderLayout.WEST, titleLabel);
        titlePanel.add(BorderLayout.CENTER, spacer);
        titlePanel.add(BorderLayout.EAST, closeButton);
        
        BorderLayout mainLayout = new BorderLayout();
        mainLayout.setVgap(2);
        setLayout(mainLayout);
  
        BorderLayout topLayout = new BorderLayout();
        topLayout.setVgap(2);
        topPanel.setLayout(topLayout);

        topPanel.add(BorderLayout.NORTH, titlePanel);
        topPanel.add(BorderLayout.SOUTH, buildButtonPanel());

        add(BorderLayout.NORTH, topPanel);
        add(BorderLayout.CENTER, chunkPane);

        closeButton.addActionListener(this);
    }

    private JPanel buildButtonPanel()
    {
        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        tagButton = new JButton("Tag");

        removeButton.setEnabled(false);
        tagButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(tagButton);

        removeButton.addActionListener(this);
        tagButton.addActionListener(this);
        return buttonPanel;
    }

    /**
     * setEditor set callback to LevelEditor.
     * @param editor LevelEditor object
     */
    public void setEditor(LevelEditor editor)
    {
        this.editor = editor;
    }

    /**
     * addChunk to this panel.
     * Renders the chunk along with the others,
     * and will set it up for future saving and loading.
     * @param level Level to create a chunk from.
     */
    public void addChunk(Level level)
    {
        LevelView levelView = new LevelView(level);
        addChunk(levelView);
    }

    /**
     * addChunk to this panel.
     * Renders the chunk along with the others,
     * and will set it up for future saving and loading.
     * @param level LevelView to add.
     */
    public void addChunk(LevelView levelView)
    {
        addChunkToPanel(levelView);
        ChunkLibrary.addChunk(levelView.getLevel());
    }

    private void addChunkToPanel(LevelView levelView)
    {
        chunks.add(levelView);
        levelView.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        levelView.setClickListener(this);
        Dimension levelPSize = levelView.getPreferredSize();
        Dimension cpPSize = chunkPanel.getPreferredSize();
        Dimension newPSize = new Dimension(Math.max(cpPSize.width-16, levelPSize.width) + 16, chunkPanelOffset+levelPSize.height+8);        
        chunkPanel.add(levelView);
        levelView.setBounds(8, chunkPanelOffset, levelPSize.width, levelPSize.height);
        chunkPanelOffset += levelPSize.height + 8;
        chunkPanel.setMaximumSize(newPSize);
        chunkPanel.setPreferredSize(newPSize);
        chunkPanel.revalidate();
        if (!areChunksLoaded)
        {
            repaint();
        }
    }

    private void addChunkToPanel(Level level)
    {
        LevelView levelView = new LevelView(level);
        addChunkToPanel(levelView);
    }

    /**
     * removeChunk from this panel.
     * Stops rendering of the chunk, and prevents
     * it from being loaded or saved in the future.
     * @param chunk Chunk to remove.
     */
    public void removeChunk(LevelView chunk)
    {
        ChunkLibrary.removeChunk(chunk.getLevel());
        removeChunkFromPanel(chunk);
    }

    private void removeChunkFromPanel(LevelView chunk)
    {
        chunks.remove(chunk);
        chunkPanel.remove(chunk);
        Dimension levelPSize = chunk.getPreferredSize();
        Dimension cpPSize = chunkPanel.getPreferredSize();
        Dimension newPSize = new Dimension(Math.max(cpPSize.width, levelPSize.width), chunkPanelOffset-levelPSize.height+8);        
        chunkPanelOffset -= levelPSize.height + 8;
        chunkPanel.setMaximumSize(newPSize);
        chunkPanel.setPreferredSize(newPSize);

        int yOffset = 8;
        for (LevelView tmpChunk : chunks)
        {
            Dimension tmpSize = tmpChunk.getPreferredSize();
            tmpChunk.setBounds(8, yOffset, tmpSize.width, tmpSize.height);
            yOffset += tmpSize.height + 8;
        }
        repaint();
    }

    /**
     * currentSelection returns the current selected chunk.
     * @return Selected chunk, or null if nothing is selected.
     */
    public LevelView currentSelection() 
    { 
        return currentSelection; 
    }

    /**
     * getAddChunkButton has only a single particular use, for allowing 
     * LevelEditor to set the callback on the button.
     * @return addButton
     */
    public JButton getAddChunkButton()
    {
        return addButton;
    }

    /**
     * loadChunks in the panel to the chunks directory in program directory.
     */
    public void loadChunks()
    {
        areChunksLoaded = false;
        //System.out.println(ChunkLibrary.getChunks().size());
        for (Level chunk : ChunkLibrary.getChunks())
        {
            System.out.println("LOL");
            if (chunk != null)
            {
                addChunkToPanel(chunk);
            }
        }
        areChunksLoaded = true;
        // repaint();
        revalidate();
    }

    /**
     * setSelectionChangedListener set listener for selection changing.
     * @param listener Object to be notified when selection is changed.
     */
    public void setSelectionChangedListener(SelectionChangedListener listener)
    {
        selectionChangedListener = listener;
    }

    private void notifySelectionChangedListener()
    {
        if (selectionChangedListener != null)
        {
            selectionChangedListener.onSelectionChanged(currentSelection);
        }
    }

    @Override
    public void clickPerformed(LevelView source) {
        removeButton.setEnabled(true);
        for (LevelView chunk : chunks)
        {
            chunk.clearHighlight();
        }
        currentSelection = source;
        source.setHighlight();
        notifySelectionChangedListener();
        if (editor != null)
        {
            editor.setEditingMode(LevelEditor.MODE_PLACE_CHUNK);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if (e.getSource() == closeButton) 
        {
            setVisible(false);
            if (editor != null)
            {
                editor.setEditingMode(LevelEditor.MODE_SELECT);
            }
        }
        if (e.getSource() == removeButton)
        {
            if (currentSelection != null)
            {
                removeChunk(currentSelection);
            }
        }
    }

    @Override
    public void onLoadingFinished() {
        loadChunks();        
    }

    /**
     * SelectionChangedListener is an interface allowing an object to
     * be notified when the user's selection in this component changes.
     */
    public interface SelectionChangedListener {
        /**
         * onSelectionChanged
         * @param selection
         */
        void onSelectionChanged(LevelView selection);
    }

    public static void main(String[] args)
    {
        ChunkLibraryPanel chunkLibrary = new ChunkLibraryPanel();
        chunkLibrary.addChunk(LevelGenerator.createLevel(15, 15, 15L, 3, LevelGenerator.TYPE_OVERGROUND));
        JFrame frame = new JFrame();
        frame.setContentPane(chunkLibrary);
        frame.pack();
        frame.setSize(300, 300);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
