package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.mojang.mario.level.SpriteTemplate;
import com.mojang.mario.sprites.Platform;
import com.mojang.mario.sprites.PlatformH;
import com.mojang.mario.sprites.PlatformV;

public class PlatformPanel extends JPanel 
implements ActionListener, ChangeListener {

    private JSlider widthSlider;
    private JSlider pathLengthSlider;
    private JRadioButton verticalRadio;
    private JRadioButton horizontalRadio;
    private JRadioButton startRadio;
    private JRadioButton centerRadio;
    private JRadioButton endRadio;

    private Platform pickedPlatform;
    private SpriteTemplate pickedHazard;

    public PlatformPanel(HazardPicker hazardPicker) 
    {
        super(new GridLayout(0, 1));

        JLabel widthLabel = new JLabel("Width");
        widthSlider = new JSlider(JSlider.HORIZONTAL, 1, 16, 3);
        widthSlider.setMinorTickSpacing(2);
        widthSlider.addChangeListener(this);

        JLabel pathLengthLabel = new JLabel("Path length");
        pathLengthSlider = new JSlider(JSlider.HORIZONTAL, 1, 32, 10);
        pathLengthSlider.addChangeListener(this);

        JLabel orientationLabel = new JLabel("Orientation");
        ButtonGroup orientationGroup = new ButtonGroup();
        verticalRadio = new JRadioButton("Vertical");
        verticalRadio.addActionListener(this);

        horizontalRadio = new JRadioButton("Horizontal");
        horizontalRadio.addActionListener(this);

        orientationGroup.add(verticalRadio);
        orientationGroup.add(horizontalRadio);

        JLabel startPositionLabel = new JLabel("Start position");
        ButtonGroup startPositionGroup = new ButtonGroup();
        startRadio = new JRadioButton("Left");
        centerRadio = new JRadioButton("Center");
        endRadio = new JRadioButton("Right");

        startRadio.addActionListener(this);
        centerRadio.addActionListener(this);
        endRadio.addActionListener(this);

        startPositionGroup.add(startRadio);
        startPositionGroup.add(centerRadio);
        startPositionGroup.add(endRadio);

        widthLabel.setVerticalAlignment(SwingConstants.TOP);
        widthSlider.setAlignmentY(SwingConstants.TOP);
        pathLengthLabel.setVerticalAlignment(SwingConstants.TOP);
        pathLengthSlider.setAlignmentY(SwingConstants.TOP);
        orientationLabel.setVerticalAlignment(SwingConstants.TOP);
        verticalRadio.setVerticalAlignment(SwingConstants.TOP);
        horizontalRadio.setVerticalAlignment(SwingConstants.TOP);
        startPositionLabel.setVerticalAlignment(SwingConstants.TOP);
        startRadio.setVerticalAlignment(SwingConstants.TOP);
        centerRadio.setVerticalAlignment(SwingConstants.TOP);
        endRadio.setVerticalAlignment(SwingConstants.TOP);

        add(widthLabel);
        add(widthSlider);
        add(pathLengthLabel);
        add(pathLengthSlider);
        add(orientationLabel);
        add(verticalRadio);
        add(horizontalRadio);
        add(startPositionLabel);
        add(startRadio);
        add(centerRadio);
        add(endRadio);

        horizontalRadio.setSelected(true);
        centerRadio.setSelected(true);

        pickedPlatform = new PlatformH(0, 0, widthSlider.getValue(), pathLengthSlider.getValue());
        pickedPlatform.setStartPosition(PlatformH.START_CENTER);
        pickedHazard = new SpriteTemplate(pickedPlatform);
    }

    @Override
    public void stateChanged(ChangeEvent e) 
    {
        Object o = e.getSource();
        if (o == pathLengthSlider)
        {
            JSlider pathLengthSlider = (JSlider)o;
            int value = pathLengthSlider.getValue();
            if (value % 2 == 1)
            {
                value = value + 1;
            }
            pickedPlatform.trackLength = value;
        }
        else if (o == widthSlider)
        {
            JSlider widthSlider = (JSlider)o;
            pickedPlatform.width = widthSlider.getValue();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        Object o = e.getSource();
        if (o == verticalRadio)
        {
            JRadioButton button = (JRadioButton)o;
            if (button.isSelected())
            {
                startRadio.setText("Top");
                centerRadio.setText("Center");
                endRadio.setText("Bottom");
                centerRadio.setSelected(true);
                pickedPlatform = new PlatformV(pickedPlatform);
                pickedPlatform.setStartPosition(PlatformV.START_CENTER);
                pickedHazard.sprite = pickedPlatform;
            }

        }
        else if (o == horizontalRadio)
        {
            JRadioButton button = (JRadioButton)o;
            if (button.isSelected())
            {
                startRadio.setText("Start");
                centerRadio.setText("Center");
                endRadio.setText("End");
                centerRadio.setSelected(true);
                pickedPlatform = new PlatformH(pickedPlatform);
                pickedPlatform.setStartPosition(PlatformH.START_CENTER);
                pickedHazard.sprite = pickedPlatform;
            }

        }
        else if (o == startRadio)
        {
            if (horizontalRadio.isSelected())
            {
                pickedPlatform.setStartPosition(PlatformH.START_LEFT);
            }
            else
            {
                pickedPlatform.setStartPosition(PlatformV.START_TOP);
            }
        }
        else if (o == centerRadio)
        {
            if (horizontalRadio.isSelected())
            {
                pickedPlatform.setStartPosition(PlatformH.START_CENTER);
            }
            else
            {
                pickedPlatform.setStartPosition(PlatformV.START_CENTER);
            }
        }
        else if (o == endRadio)
        {
            if (horizontalRadio.isSelected())
            {
                pickedPlatform.setStartPosition(PlatformH.START_RIGHT);
            }
            else
            {
                pickedPlatform.setStartPosition(PlatformV.START_BOTTOM);
            }

        }
    }

    public SpriteTemplate getSpriteTemplate()
    {
        return pickedHazard;
    }
}
