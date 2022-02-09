package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;

import com.mojang.mario.TestLevelFrameLauncher;
import com.mojang.mario.level.*;


/**
 * LevelEditor does exactly what the name implies.
 * Allows the user to place tiles, enemies, and hazards in a level, save levels,
 * and playtest their levels.
 */
public class LevelEditor extends JFrame 
    implements ActionListener, LevelEditView.ActionCompleteListener
{
    private static final long serialVersionUID = 7461321112832160393L;

    private JButton testButton;
    private JButton resizeButton;
    private JButton generateButton;
    private JMenuItem changeDirectoryItem;
    private JMenuItem openLevelItem;
    private JMenuItem newLevelItem;
    private JMenuItem saveLevelItem;
    private JMenuItem saveLevelAsItem;
    private JMenuItem undoActionItem;
    private JMenuItem redoActionItem;
    private JLabel levelNameLabel;
    private JTextField nameField;
    private LevelEditView levelEditView;
    private TilePicker tilePicker;
    private EnemyPicker enemyPicker;
    private HazardPicker hazardPicker;
    private JLabel coordinates;
    private String coordinateText="X=P , Y=Q";
    private TestLevelFrameLauncher levelTester;
    private JCheckBox[] bitmapCheckboxes = new JCheckBox[8];

    private ArrayList<Level> actionQueue;
    private int nextStatePtr;

    private String workingDirectory;
    private String levelName;

    public static final int MODE_TILE = 1;
    public static final int MODE_ENEMY = 2;
    public static final int MODE_HAZARD = 3;

    /**
     * Constructor.
     */
    public LevelEditor()
    {
        super("Map Edit");
        
        levelName = "test";
    
        try
        {
            Level.loadBehaviors(new DataInputStream(new FileInputStream("tiles.dat")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width * 8 / 10, screenSize.height * 8 / 10);
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tilePicker = new TilePicker();
        JPanel tilePickerPanel = new JPanel(new BorderLayout());
        tilePickerPanel.add(BorderLayout.WEST, tilePicker);
        tilePickerPanel.add(BorderLayout.CENTER, buildBitmapPanel());
        tilePickerPanel.setBorder(new TitledBorder(new EtchedBorder(), "Tile picker"));

        enemyPicker = new EnemyPicker(this);
        hazardPicker = new HazardPicker(this);

        JPanel lowerPanel = new JPanel(new GridLayout(1, 3));
        lowerPanel.add(tilePickerPanel);
        lowerPanel.add(enemyPicker);
        lowerPanel.add(hazardPicker);

        JPanel borderPanel = new JPanel(new BorderLayout());
        levelEditView = new LevelEditView(enemyPicker, tilePicker, hazardPicker);
        levelEditView.setActionCompleteListener(this);
        borderPanel.add(BorderLayout.CENTER, new JScrollPane(levelEditView));
        borderPanel.add(BorderLayout.SOUTH, lowerPanel);
        borderPanel.add(BorderLayout.NORTH, buildButtonPanel());
        setContentPane(borderPanel);

        setJMenuBar(buildMenuBar());

        levelTester = new TestLevelFrameLauncher();

        tilePicker.addTilePickChangedListener(this);

        actionQueue = new ArrayList<>();
        saveState();
    }

    /**
     * Change the editing mode of the editor.
     * Determines what gets placed when the user clicks on the canvas.
     * @param mode One of MODE_ENEMY, MODE_HAZARD, MODE_TILE.
     */
    public void setEditingMode(int mode)
    {
        levelEditView.setEditingMode(mode);
    }

    /**
     * Panel beside level picker that allows setting tile behviours.
     * @return Tile behaviour panel.
     */
    public JPanel buildBitmapPanel()
    {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for (int i=0; i<8; i++)
        {
            bitmapCheckboxes[i] = new JCheckBox(Level.BIT_DESCRIPTIONS[i]);
            panel.add(bitmapCheckboxes[i]);
            if (Level.BIT_DESCRIPTIONS[i].startsWith("- ")) bitmapCheckboxes[i].setEnabled(false);
            
            final int id = i;
            bitmapCheckboxes[i].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent arg0)
                {
                    int bm = Level.TILE_BEHAVIORS[tilePicker.pickedTile&0xff]&0xff;
                    bm&=255-(1<<id);
                    if (bitmapCheckboxes[id].isSelected()) bm|=(1<<id);
                    Level.TILE_BEHAVIORS[tilePicker.pickedTile&0xff] = (byte)bm;

                    try
                    {
                        Level.saveBehaviors(new DataOutputStream(new FileOutputStream("tiles.dat")));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(LevelEditor.this, e.toString(), "Failed to load tile behaviors", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        return panel;
    }

    /**
     * buildButtonPanel containing file system controls and level meta information.
     * @return Built panel.
     */
    public JPanel buildButtonPanel()
    {
        testButton = new JButton("Test");
        resizeButton = new JButton("Resize");
        generateButton = new JButton("Generate");
        String userDir = System.getProperty("user.dir");
        File programDirectory = new File(userDir + "/.infinitetux");
        programDirectory.mkdirs();
        File levelDirectory = new File(programDirectory.getPath() + "/levels");
        levelDirectory.mkdirs();
        workingDirectory = levelDirectory.getAbsolutePath();
        
        nameField = new JTextField("test", 10);
        levelNameLabel = new JLabel(levelName);
        
        coordinates = new JLabel(coordinateText, 10);
        testButton.addActionListener(this);
        resizeButton.addActionListener(this);
        generateButton.addActionListener(this);
        
        JPanel panel = new JPanel();
        panel.add(levelNameLabel);
        panel.add(testButton);
        panel.add(resizeButton);
        panel.add(generateButton);
        panel.add(coordinates);
        return panel;
    }

    public JMenuBar buildMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        newLevelItem = new JMenuItem("New");
        openLevelItem = new JMenuItem("Open");
        saveLevelItem = new JMenuItem("Save");
        saveLevelAsItem = new JMenuItem("Save as");
        changeDirectoryItem = new JMenuItem("Change levels directory");

        newLevelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('n'), KeyEvent.CTRL_DOWN_MASK));
        openLevelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('o'), KeyEvent.CTRL_DOWN_MASK));
        saveLevelItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('s'), KeyEvent.CTRL_DOWN_MASK));
        saveLevelAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('s'), KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

        newLevelItem.addActionListener(this);
        openLevelItem.addActionListener(this);
        saveLevelItem.addActionListener(this);
        saveLevelAsItem.addActionListener(this);
        changeDirectoryItem.addActionListener(this);

        fileMenu.add(newLevelItem);
        fileMenu.add(openLevelItem);
        fileMenu.add(saveLevelItem);
        fileMenu.add(saveLevelAsItem);
        fileMenu.add(changeDirectoryItem);

        JMenu editMenu = new JMenu("Edit");
        undoActionItem = new JMenuItem("Undo");
        redoActionItem = new JMenuItem("Redo");

        undoActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('z'), KeyEvent.CTRL_DOWN_MASK));
        redoActionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('z'), KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

        undoActionItem.addActionListener(this);
        redoActionItem.addActionListener(this);

        editMenu.add(undoActionItem);
        editMenu.add(redoActionItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        return menuBar;
    }

    /**
     * Handles user interface events.
     */
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            if (e.getSource() == openLevelItem)
            {
                JFileChooser chooser = new JFileChooser(workingDirectory);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Folders";
                    }
                });
                int returnValue = chooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) 
                {
                    String name = chooser.getSelectedFile().getAbsolutePath();
                    int directoryIndex = name.lastIndexOf(File.separatorChar);
                    workingDirectory = name.substring(0, directoryIndex);
                    nameField.setText(name.substring(directoryIndex + 1, name.length()));
                    levelEditView.setLevel(Level.load(new File(getLevelDirectory())));
                }
            }
            if (e.getSource() == saveLevelItem)
            {
                String saveLocation = getLevelDirectory();
                if (saveLocation != null)
                {
                    levelEditView.getLevel().save(new File(saveLocation));
                }
            }
            if (e.getSource() == saveLevelAsItem)
            {
                String newLevelName = (String)JOptionPane.showInputDialog(
                    null, "Enter a name for the level", "Enter new level name", 
                    JOptionPane.PLAIN_MESSAGE, null, null, levelName
                );
                if (isValidPath(newLevelName))
                {
                    levelName = newLevelName;
                    levelNameLabel.setText(levelName);
                }
                else
                {
                    JOptionPane.showMessageDialog(null, newLevelName + " is not a valid filename.", "Invalid level name", JOptionPane.ERROR_MESSAGE);
                }
            }
            if (e.getSource() == newLevelItem)
            {
                String saveLocation = getLevelDirectory();
                if (saveLocation != null)
                {
                    levelEditView.getLevel().save(new File(saveLocation));
                    nameField.setText("");
                    levelEditView.setLevel(new Level(256, 15));
                    JOptionPane.showMessageDialog(null, "Previous level saved to: " + saveLocation);
                }
            }   
            if (e.getSource() == undoActionItem)
            {
                undoAction();
            }  
            if (e.getSource() == redoActionItem)
            {
                redoAction();
            }
            if (e.getSource() == testButton) 
            {
                String saveLocation = getLevelDirectory();
                Level level = levelEditView.getLevel();
                level.save(new File(saveLocation));
                levelTester.testLevel(level);
    
            }       
            if (e.getSource() == resizeButton)
            {
                Level level = levelEditView.getLevel();
                String inputWidth = JOptionPane.showInputDialog(null, "Enter new level width", level.width+"");
                if (inputWidth == null) return;
                String inputHeight = JOptionPane.showInputDialog(null, "Enter new level height", level.height+"");
                if (inputHeight == null) return;
                String inputStart = JOptionPane.showInputDialog(null, "Enter new level start", 0+"");
                if (inputStart == null) return;
                int levelWidth = level.width;
                int levelHeight = level.height;
                int startX = 0;
                try
                {
                    levelWidth = Integer.parseInt(inputWidth.trim());
                    if (levelWidth < -1) throw new NumberFormatException();
                }
                catch (NumberFormatException ne)
                {
                    JOptionPane.showMessageDialog(null, "Invalid input for level width", "Please enter a positive number", JOptionPane.ERROR_MESSAGE);
                }
                try
                {
                    levelHeight = Integer.parseInt(inputHeight.trim());
                    if (levelHeight < -1) throw new NumberFormatException();
                }
                catch (NumberFormatException ne)
                {
                    JOptionPane.showMessageDialog(null, "Invalid input for level height", "Please enter a positive number", JOptionPane.ERROR_MESSAGE);
                }
                try
                {
                    startX = Integer.parseInt(inputStart.trim());
                    if (startX < -1) throw new NumberFormatException();
                }
                catch (NumberFormatException ne)
                {
                    JOptionPane.showMessageDialog(null, "Invalid input for level start", "Please enter a positive number", JOptionPane.ERROR_MESSAGE);
                }

                if (startX > levelWidth || startX < 0) JOptionPane.showMessageDialog(null, "Invalid input for level start", "Please enter a number between 0 and " + levelWidth, JOptionPane.ERROR_MESSAGE);
                int startY = 0;
                level.resize(startX, startY, levelWidth, levelHeight);
                levelEditView.repaint();
                levelEditView.resize();
                levelEditView.setLevel(level);
            }
            if (e.getSource() == generateButton)
            {
                GenerateDialog.Results params = GenerateDialog.getDialog();
                if (params != null)
                {
                    Level level = LevelGenerator.createLevel(params.width, params.height, params.seed, params.difficulty, params.type);
                    updateLevel(level);

                    saveState();
                }
            }
            if (e.getSource() == changeDirectoryItem)
            {
                JFileChooser chooser = new JFileChooser(workingDirectory);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        return file.exists() && file.isDirectory() && file.canWrite();
                    }

                    @Override
                    public String getDescription() {
                        return "Folders";
                    }
                });
                int returnValue = chooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) 
                {
                    String newDirectory = chooser.getSelectedFile().getAbsolutePath();
                    workingDirectory = newDirectory;
                }
            }
        }
        catch (NullPointerException npe)
        {
            npe.printStackTrace();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, ex.toString(), "Failed to load/save", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    /*
     * Gets the level directory, which contains map.lvl, enemy.lvl, hazard.lvl.
     */
    private String getLevelDirectory()
    {
        File levelDirectory = new File(workingDirectory + "/" + levelName);
        if (!levelDirectory.exists()) 
        {
            levelDirectory.mkdirs();
        }
        return levelDirectory.getPath();
    }

    /**
     * Update the coordinate display.
     * @param x x-value to change coordinate display to.
     * @param y y-value to change coordinate display to.
     */
    public void setCoordinates(int x , int y)
    {
        coordinateText="X=" + x +" , " +"Y="+y;
        coordinates.setText(coordinateText);    
    }

    /**
     * setPickedTile to tile described by pickedTile.
     * Updates the tile behaviour boxes to match the seleted tile.
     * @param pickedTile
     */
    public void setPickedTile(byte pickedTile)
    {
        int bm = Level.TILE_BEHAVIORS[pickedTile&0xff]&0xff;
        
        for (int i=0; i<8; i++)
        {
            bitmapCheckboxes[i].setSelected((bm&(1<<i))>0);
        }
    }

    @Deprecated
    /**
     * loadLevel was used by the old level editor to load levels from a string.
     * It may still work, but needs testing with the new system.
     * @param levelPath Path to level directory to load.
     */
    public static void loadLevel(String levelPath)
    {
        LevelEditor editor = new LevelEditor();
        editor.setVisible(true);
        try
        {
            editor.levelEditView.setLevel(Level.load(new File(levelPath)));
        } catch (IOException ie)
        {
            JOptionPane.showMessageDialog(editor, ie.getMessage(), "Error reloading level", JOptionPane.ERROR_MESSAGE);
            ie.printStackTrace();
        }
    }

    /**
     * <pre>
     * Checks if a string is a valid path.
     * Null safe.
     *  
     * Calling examples:
     *    isValidPath("c:/test");      //returns true
     *    isValidPath("c:/te:t");      //returns false
     *    isValidPath("c:/te?t");      //returns false
     *    isValidPath("c/te*t");       //returns false
     *    isValidPath("good.txt");     //returns true
     *    isValidPath("not|good.txt"); //returns false
     *    isValidPath("not:good.txt"); //returns false
     * </pre>
     * @param path Path of string to check
     * @retrun True if path is valid
     */
    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    /**
     * Navigate down history to last action.
     */
    private void undoAction()
    {
        if (actionQueue.size() < 2) return;
        nextStatePtr--;
        Level oldState = actionQueue.get(Math.max(nextStatePtr - 1, 0));
        updateLevel(oldState);
    }

    /**
     * Reload last state.
     */
    private void redoAction()
    {
        if (actionQueue.size() < 2) return;
        Level oldState = actionQueue.get(nextStatePtr);
        updateLevel(oldState);
        nextStatePtr++;
    }

    /**
     * Add a new action to history.
     * Note that this erases any actions that can be redone.
     */
    private void saveState()
    {
        // Discard actions in future if nextStatePtr points to a non-empty slot.
        if (nextStatePtr < actionQueue.size())
        {
            int lastIndex = actionQueue.size() - 1;
            for (int i = lastIndex; i >= nextStatePtr; i--)
            {
                actionQueue.remove(i);
            }
        }
        actionQueue.add(new Level(levelEditView.getLevel()));
        nextStatePtr++;
    }

    /**
     * updateLevel update the level and its render
     * @param level New level state to view
     */
    private void updateLevel(Level level)
    {
        levelEditView.setLevel(level);
        levelEditView.resize();
        levelEditView.repaint();
    }

    @Override
    public void onActionComplete() 
    {
        saveState();    
    }

    public static void main(String[] args)
    {
        new LevelEditor().setVisible(true);
    }
}
