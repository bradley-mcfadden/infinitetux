package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;

import com.mojang.mario.TestLevelFrameLauncher;
import com.mojang.mario.level.*;


public class LevelEditor extends JFrame implements ActionListener
{
    private static final long serialVersionUID = 7461321112832160393L;

    private JButton loadButton;
    private JButton saveButton;
    private JButton newButton;
    private JButton testButton;
    private JMenuItem changeDirectory;
    private JTextField nameField;
    private LevelEditView levelEditView;
    private TilePicker tilePicker;
    private JLabel coordinates;
    private String coordinateText="X=P , Y=Q";
    private TestLevelFrameLauncher levelTester;
    private JCheckBox[] bitmapCheckboxes = new JCheckBox[8];

    private String workingDirectory;

    public LevelEditor()
    {
        super("Map Edit");
        
        try
        {
            Level.loadBehaviors(new DataInputStream(new FileInputStream("tiles.dat")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // JOptionPane.showMessageDialog(this, e.toString(), "Failed to load tile behaviors", JOptionPane.ERROR_MESSAGE);
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

        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.add(BorderLayout.WEST, tilePickerPanel);

        JPanel borderPanel = new JPanel(new BorderLayout());
        levelEditView = new LevelEditView(tilePicker);
        borderPanel.add(BorderLayout.CENTER, new JScrollPane(levelEditView));
        borderPanel.add(BorderLayout.SOUTH, lowerPanel);
        borderPanel.add(BorderLayout.NORTH, buildButtonPanel());
        setContentPane(borderPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        changeDirectory = new JMenuItem("Change levels directory");
        changeDirectory.addActionListener(this);
        fileMenu.add(changeDirectory);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        levelTester = new TestLevelFrameLauncher();

        tilePicker.addTilePickChangedListener(this);
    }

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

    public JPanel buildButtonPanel()
    {
        loadButton = new JButton("Load");
        saveButton = new JButton("Save");
        newButton = new JButton("New");
        testButton = new JButton("Test");
        String userDir = System.getProperty("user.dir");
        File programDirectory = new File(userDir + "/.infinitetux");
        programDirectory.mkdirs();
        File levelDirectory = new File(programDirectory.getPath() + "/levels");
        levelDirectory.mkdirs();
        workingDirectory = levelDirectory.getAbsolutePath();
        
        nameField = new JTextField("test.lvl", 10);
        
        coordinates = new JLabel(coordinateText,10);
        loadButton.addActionListener(this);
        saveButton.addActionListener(this);
        newButton.addActionListener(this);
        testButton.addActionListener(this);
        
        JPanel panel = new JPanel();
        panel.add(nameField);
        panel.add(loadButton);
        panel.add(saveButton);
        panel.add(newButton);
        panel.add(testButton);
        panel.add(coordinates);
        return panel;
    }

    public void actionPerformed(ActionEvent e)
    {
        try
        {
            if (e.getSource() == loadButton)
            {
                levelEditView.setLevel(Level.load(new DataInputStream(new FileInputStream(workingDirectory + "/" + nameField.getText().trim()))));
            }
            if (e.getSource() == saveButton)
            {
                levelEditView.getLevel().save(new DataOutputStream(new FileOutputStream(workingDirectory + "/" + nameField.getText().trim())));
            }
            if (e.getSource() == newButton)
            {
                levelEditView.getLevel().save(new DataOutputStream(new FileOutputStream(workingDirectory + "/" + nameField.getText().trim())));
            }     
            if (e.getSource() == testButton) 
            {
                Level level = levelEditView.getLevel();
                level.save(new DataOutputStream(new FileOutputStream(nameField.getText().trim())));
                levelTester.testLevel(level);
            }       
            if (e.getSource() == changeDirectory)
            {
                JFileChooser chooser = new JFileChooser(workingDirectory);
                chooser.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        return file.exists() && file.isDirectory() && file.canWrite();
                    }

                    @Override
                    public String getDescription() {
                        return "Existing, writeable folders";
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
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, ex.toString(), "Failed to load/save", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void setCoordinates(int x , int y)
    {
        coordinateText="X=" + x +" , " +"Y="+y;
        coordinates.setText(coordinateText);    
    }
    
    public static void main(String[] args)
    {
        new LevelEditor().setVisible(true);
    }

    public void setPickedTile(byte pickedTile)
    {
        int bm = Level.TILE_BEHAVIORS[pickedTile&0xff]&0xff;
        
        for (int i=0; i<8; i++)
        {
            bitmapCheckboxes[i].setSelected((bm&(1<<i))>0);
        }
    }

    public static void loadLevel(String levelPath)
    {
        LevelEditor editor = new LevelEditor();
        editor.setVisible(true);
        try
        {
            editor.levelEditView.setLevel(Level.load(new DataInputStream(new FileInputStream(editor.workingDirectory + "/" + levelPath))));
        } catch (IOException ie)
        {
            JOptionPane.showMessageDialog(editor, ie.getMessage(), "Error reloading level", JOptionPane.ERROR_MESSAGE);
            ie.printStackTrace();
        }
    }
}
