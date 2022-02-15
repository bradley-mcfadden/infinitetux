package com.mojang.mario.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.mojang.mario.level.Level;
import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.mapedit.LevelView.Loader;

/**
 * ChunkLibraryPanel manages a list of "chunks", or Level
 * sections that the user can interact with.
 * 
 * Clicking on a chunk highlights it, and the user can get
 * the current selection with the getSelection() method.
 * 
 * The list of chunks can be modified with the addChunk()
 * and removeChunk() methods.
 */
public class ChunkLibraryPanel extends JPanel 
    implements ActionListener, LevelView.ClickListener {
    
    private static final String CHUNK_PARENT_DIR_NAME = "chunks";

    private JPanel chunkPanel;
    private JButton closeButton;
    private JButton addButton;
    private JButton removeButton;
    private JButton tagButton;

    private File programDirectory;
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
     * setProgramDirectory for library to use to save chunks in
     * @param programDirectory Folder that should exist
     */
    public void setProgramDirectory(File programDirectory)
    {
        this.programDirectory = programDirectory;
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
        levelView.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        levelView.setClickListener(this);
        Dimension levelPSize = levelView.getPreferredSize();
        Dimension cpPSize = chunkPanel.getPreferredSize();
        Dimension newPSize = new Dimension(Math.max(cpPSize.width, levelPSize.width), chunkPanelOffset+levelPSize.height+8);        
        chunkPanel.add(levelView);
        levelView.setBounds(8, chunkPanelOffset, levelPSize.width, levelPSize.height);
        chunkPanelOffset += levelPSize.height + 8;
        chunkPanel.setMaximumSize(newPSize);
        chunkPanel.setPreferredSize(newPSize);
        chunkPanel.revalidate();
        chunks.add(levelView);
        
        if (!areChunksLoaded)
        {
            repaint();
        }
    }

    /**
     * removeChunk from this panel.
     * Stops rendering of the chunk, and prevents
     * it from being loaded or saved in the future.
     * @param chunk Chunk to remove.
     */
    public void removeChunk(LevelView chunk)
    {
        chunkPanel.remove(chunk);
        chunks.remove(chunk);

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
     * saveChunks in the panel to the chunks directory in program directory.
     */
    public void saveChunks()
    {
        File chunksDirectory = new File(programDirectory.getPath() + File.separatorChar + CHUNK_PARENT_DIR_NAME);
        chunksDirectory.mkdirs();

        int n = chunks.size();
        for (int i = 0; i < n; i++)
        {
            LevelView chunk = chunks.get(i);
            String name = String.format("%03d", i);
            File chunkDir = new File(chunksDirectory.getPath() + File.separatorChar + name);
            chunkDir.mkdirs();
            chunk.save(chunkDir);
        }
    }

    /**
     * loadChunks in the panel to the chunks directory in program directory.
     */
    public void loadChunks()
    {
        areChunksLoaded = false;
        if (Runtime.getRuntime().availableProcessors() > 1) 
        {
            loadChunksThreaded();
        } 
        else 
        {
            long start = System.currentTimeMillis();

            File chunksDirectory = new File(programDirectory.getPath() + File.separatorChar + CHUNK_PARENT_DIR_NAME);
            File[] chunkDirs = chunksDirectory.listFiles();
            // System.out.println(chunksDirectory.getAbsolutePath());
            if (chunkDirs != null)
            {
                for (File chunkDir : chunkDirs)
                {
                    LevelView chunk = LevelView.load(chunkDir);
                    if (chunk != null)
                    {
                        addChunk(chunk);
                    }
                }
                repaint();
            }

            long end = System.currentTimeMillis();
            System.out.println("Loaded levels in " + (end - start) + " ms");
        }
        areChunksLoaded = true;
    }

    private void loadChunksThreaded()
    {
        long start = System.currentTimeMillis();
        File chunksDirectory = new File(programDirectory.getPath() + File.separatorChar + CHUNK_PARENT_DIR_NAME);
        File[] chunkDirs = chunksDirectory.listFiles();
        if (chunkDirs != null)
        {
            List<Loader> loaders = new ArrayList<>(chunkDirs.length);
            List<Thread> threads = new ArrayList<>(chunkDirs.length);
            Loader tmpLoader;
            Thread tmpThread;
            for (File chunkDir : chunkDirs)
            {
                tmpLoader = new Loader(chunkDir);
                tmpThread = new Thread(tmpLoader);
                loaders.add(tmpLoader);
                threads.add(tmpThread);
                tmpThread.start();
            }
            int n = loaders.size();
            for (int i = 0; i < n; i++)
            {
                tmpThread = threads.get(i);
                try {
                    tmpThread.join();
                    tmpLoader = loaders.get(i);
                    LevelView result = tmpLoader.getResult();
                    if (result != null)
                    {
                        addChunk(result);
                    }
                } catch (InterruptedException ie) {
                    System.err.println("Thread " + i + " interrupted while joining");
                }
            }
            repaint();
        }
        long end = System.currentTimeMillis();
        System.out.println("Loaded levels in " + (end - start) + " ms");
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
                currentSelection.setVisible(false);
                int i = chunks.indexOf(currentSelection);
                chunks.remove(currentSelection);
                removeChunk(currentSelection);

                File chunksDirectory = new File(programDirectory.getPath() + File.separatorChar + CHUNK_PARENT_DIR_NAME);
                File levelDirectory = new File(String.format("%s%s%03d", chunksDirectory, File.separator, i));
                File[] contents = levelDirectory.listFiles();
                for (File file : contents)
                {
                    file.delete();
                }
                levelDirectory.delete();
            }
        }
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
