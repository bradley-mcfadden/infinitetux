package com.mojang.mario;

import com.mojang.mario.level.*;
import com.mojang.mario.sprites.Mario;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.*;

public class TestLevelFrameLauncher implements LevelEndListener, WindowListener {
    private JFrame frame;
    private MarioComponent mario;
    private TestScene scene;

    public void testLevel(Level level) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                
        frame = new JFrame("Infinite Tux Level Tester");
        mario = new MarioComponent(640, 480);
        TestScene scene = new TestScene(new Level(level), mario.getGraphicsConfiguration(), mario, 0, 0, 0);
        mario.setTestScene(scene);
        mario.addLevelEndListener(this);
        frame.setLayout( new GridBagLayout() );
        frame.add(mario, new GridBagConstraints());        
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
        frame.setVisible(true);

        mario.start();
        Mario.resetStatic();
        frame.addKeyListener(mario);
        frame.addFocusListener(mario);
        frame.addWindowListener(this);
    }

    public void onLevelEnd() 
    {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e)
    {
        Art.stopMusic();
        mario.stop();
    }

    @Override
    public void windowClosed(WindowEvent e) 
    {

    }

    @Override
    public void windowIconified(WindowEvent e) 
    {
        
    }

    @Override
    public void windowDeiconified(WindowEvent e) 
    {
        
    }

    @Override
    public void windowActivated(WindowEvent e) 
    {
        
    }

    @Override
    public void windowDeactivated(WindowEvent e) 
    {
       
    }
}
