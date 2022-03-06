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
    private JButton addButton;
    private JButton removeButton;
    private JButton addTagButton;
    private JButton removeTagButton;
    
    private List<LevelView> chunks;
    private LevelView currentSelection;
    private SelectionChangedListener selectionChangedListener;
    private LevelEditor editor;

    private DefaultListModel<String> selectionTagListModel;

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
        chunkPanel = new JPanel(null);
        chunkPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane chunkPane = new JScrollPane(chunkPanel);
        chunkPane.setMaximumSize(new Dimension(20 * 16, Integer.MAX_VALUE));
        chunkPane.setPreferredSize(new Dimension(20 * 16, Integer.MAX_VALUE));
        
        BorderLayout mainLayout = new BorderLayout();
        mainLayout.setVgap(2);
        setLayout(mainLayout);
  
        BorderLayout topLayout = new BorderLayout();
        topLayout.setVgap(2);
        topPanel.setLayout(topLayout);

        // topPanel.add(BorderLayout.NORTH, titlePanel);
        topPanel.add(BorderLayout.SOUTH, buildButtonPanel());

        JPanel selectionPanel = buildSelectionPanel();
        selectionPanel.setMaximumSize(new Dimension(20 * 16, Integer.MAX_VALUE));
        selectionPanel.setPreferredSize(new Dimension(20 * 16, selectionPanel.getPreferredSize().height));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Tags for Selected Chunk"));

        add(BorderLayout.NORTH, topPanel);
        add(BorderLayout.CENTER, chunkPane);
        add(BorderLayout.SOUTH, selectionPanel);
    }

    private JPanel buildButtonPanel()
    {
        addButton = new JButton("Add Chunk");
        removeButton = new JButton("Remove Chunk");

        removeButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        removeButton.addActionListener(this);
        return buttonPanel;
    }

    private JPanel buildSelectionPanel()
    {
        JPanel selectionPanel = new JPanel(new BorderLayout());
        JPanel selectionPanelContent = new JPanel(new GridLayout(0, 1));
        selectionTagListModel = new DefaultListModel<>();
        JList<String> selectionTagList = new JList<>();
        selectionTagList.setModel(selectionTagListModel);
        // selectionTagList.setVisibleRowCount(5);
        selectionPanelContent.add(new JScrollPane(selectionTagList));
        JPanel selectionPanelControl = new JPanel(new GridLayout(0, 2));
        addTagButton = new JButton("Add Tag");
        removeTagButton = new JButton("Remove Tag");
        selectionPanelControl.add(addTagButton);
        selectionPanelControl.add(removeTagButton);

        addTagButton.addActionListener(this);
        removeTagButton.addActionListener(this);

        selectionPanel.add(BorderLayout.NORTH, selectionPanelControl);
        selectionPanel.add(BorderLayout.CENTER, selectionPanelContent);

        return selectionPanel;
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
        if (currentSelection == chunk)
        {
            currentSelection = null;
            editor.setEditingMode(LevelEditor.MODE_SELECT);
            notifySelectionChangedListener();
        }
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
            // System.out.println("LOL");
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
        Level sChunk = source.getLevel();

        removeButton.setEnabled(true);
        addTagButton.setEnabled(true);

        boolean hasTags = ChunkLibrary.getTags(sChunk).size() > 0;
        removeTagButton.setEnabled(hasTags);

        for (LevelView chunk : chunks)
        {
            chunk.clearHighlight();
        }
        currentSelection = source;
        source.setHighlight();

        // clear tags
        selectionTagListModel.clear();
        for (String tag : ChunkLibrary.getTags(sChunk))
        {
            selectionTagListModel.addElement(tag);
        }

        notifySelectionChangedListener();
        if (editor != null)
        {
            editor.setEditingMode(LevelEditor.MODE_PLACE_CHUNK);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if (e.getSource() == removeButton)
        {
            if (currentSelection != null)
            {
                removeChunk(currentSelection);
            }
        }
        if (e.getSource() == addTagButton)
        {
            if (currentSelection != null)
            {
                addTag(currentSelection);
            }
        }
        if (e.getSource() == removeTagButton)
        {
            if (currentSelection != null)
            {
                removeTag(currentSelection);
            }
        }
    }

    private void addTag(LevelView chunk) 
    {
        String[] tags = ChunkLibrary.getAllowedTags();
        String defaultTag = tags[0];
        String selection = (String)JOptionPane.showInputDialog(null, "Select a tag to add to selection", "Add tag to selection", JOptionPane.PLAIN_MESSAGE, null, tags, defaultTag);

        if (selection != null) 
        {
            ChunkLibrary.addTag(chunk.getLevel(), selection);
            removeTagButton.setEnabled(true);
            selectionTagListModel.addElement(selection);
        }
    }

    private void removeTag(LevelView chunk)
    {
        Level level = chunk.getLevel();
        List<String> tags = ChunkLibrary.getTags(level);
        String defaultTag = tags.get(0);
        String selection = (String)JOptionPane.showInputDialog(null, "Select a tag to remove from selection", "Remove tag from selection", JOptionPane.PLAIN_MESSAGE, null, tags.toArray(), defaultTag);

        if (selection != null)
        {
            if (tags.size() == 1)
                removeTagButton.setEnabled(false);
            
            ChunkLibrary.removeTag(level, selection);
            selectionTagListModel.removeElement(selection);
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
