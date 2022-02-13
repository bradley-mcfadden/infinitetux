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

import com.mojang.mario.level.Level;
import com.mojang.mario.level.LevelGenerator;

// TODO: Save/load list of chunks
public class ChunkLibraryPanel extends JPanel 
    implements ActionListener, LevelView.ClickListener {
        
    private JPanel chunkPanel;
    private JButton closeButton;
    private List<LevelView> chunks;
    private LevelView currentSelection;
    private SelectionChangedListener selectionChangedListener;
    private VisibilityListener visibilityChangedListener;
    private LevelEditor editor;
    
    public ChunkLibraryPanel()
    {
        chunks = new ArrayList<>();
        buildLayout();

        addChunk(LevelGenerator.createLevel(15, 15, 15L, 3, LevelGenerator.TYPE_OVERGROUND));
        addChunk(LevelGenerator.createLevel(15, 15, 1500L, 3, LevelGenerator.TYPE_OVERGROUND));
        addChunk(LevelGenerator.createLevel(15, 15, 15L, 3, LevelGenerator.TYPE_OVERGROUND));
        addChunk(LevelGenerator.createLevel(15, 15, 1500L, 3, LevelGenerator.TYPE_OVERGROUND));
        addChunk(LevelGenerator.createLevel(15, 15, 15L, 3, LevelGenerator.TYPE_OVERGROUND));
        addChunk(LevelGenerator.createLevel(15, 15, 1500L, 3, LevelGenerator.TYPE_OVERGROUND));
    }

    public ChunkLibraryPanel(LevelEditor editor)
    {
        this();
        this.editor = editor;
    }

    private void buildLayout()
    {
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Chunk Library");
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        JLabel spacer = new JLabel("    \n    ");
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        closeButton = new JButton("X");

        GridLayout chunkLayout = new GridLayout(0, 1);
        chunkLayout.setVgap(8);

        chunkPanel = new JPanel(chunkLayout);
        chunkPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        titlePanel.add(BorderLayout.WEST, titleLabel);
        titlePanel.add(BorderLayout.CENTER, spacer);
        titlePanel.add(BorderLayout.EAST, closeButton);
        
        BorderLayout mainLayout = new BorderLayout();
        mainLayout.setVgap(8);
        setLayout(mainLayout);
  
        add(BorderLayout.NORTH, titlePanel);
        add(BorderLayout.CENTER, new JScrollPane(chunkPanel));

        closeButton.addActionListener(this);
    }

    public void setEditor(LevelEditor editor)
    {
        this.editor = editor;
    }

    public void addChunk(Level level)
    {
        LevelView levelView = new LevelView(level);
        levelView.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        levelView.setClickListener(this);
        chunkPanel.add(levelView);
        chunks.add(levelView);
        repaint();
    }

    public void removeChunk(LevelView chunk)
    {
        chunkPanel.remove(chunk);
        chunks.remove(chunk);
        repaint();
    }

    public LevelView currentSelection() { return currentSelection; }

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

    public void setVisibiltyChangedListener(VisibilityListener listener)
    {
        visibilityChangedListener = listener;
    }

    private void notifyVisibilityChangedListener()
    {
        if (visibilityChangedListener != null)
        {
            visibilityChangedListener.onVisibilityChanged(this, this.isVisible());
        }
    }

    @Override
    public void clickPerformed(LevelView source) {
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
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeButton) {
            setVisible(false);
            notifyVisibilityChangedListener();
        }
    }

    public interface SelectionChangedListener {
        void onSelectionChanged(LevelView selection);
    }

    public interface VisibilityListener {
        void onVisibilityChanged(ChunkLibraryPanel panel, boolean isVisible);
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
