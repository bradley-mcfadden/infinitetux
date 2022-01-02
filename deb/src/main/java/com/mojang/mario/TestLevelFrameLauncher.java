package com.mojang.mario;

import com.mojang.mario.level.*;

import java.awt.*;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class TestLevelFrameLauncher implements LevelEndListener {
    private JFrame frame;
    private MarioComponent mario;

    public void testLevel(Level level) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                
        frame = new JFrame("Infinite Tux Level Tester");
        mario = new MarioComponent(640, 480);
        TestScene scene = new TestScene(level, mario.getGraphicsConfiguration(), mario, 0, 0, 0);
        mario.setTestScene(scene);
        mario.addLevelEndListener(this);
        frame.setLayout( new GridBagLayout() );
        frame.add(mario, new GridBagConstraints());        
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
        frame.setVisible(true);

        mario.start();
        frame.addKeyListener(mario);
        frame.addFocusListener(mario);
    }

    public void onLevelEnd() 
    {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        mario.stop();
    }
}
