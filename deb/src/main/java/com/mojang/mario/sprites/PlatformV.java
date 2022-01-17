package com.mojang.mario.sprites;

import java.awt.Color;
import java.awt.Graphics;

public class PlatformV extends Platform {

    public static final int START_TOP = 21;
    public static final int START_CENTER = 22;
    public static final int START_BOTTOM = 23;

    public PlatformV(int x, int y, int width, int trackLength) 
    {
        super(x, y, width, trackLength);

    }

    public PlatformV(Platform platform)
    {
        super((int)platform.x, (int)platform.y, (int)platform.width, (int)platform.trackLength);
    }


    @Override
    public void render(Graphics og, float alpha)
    {
        Color oldColor = og.getColor();
        og.drawLine((int)x, (int)start, (int)x, (int)end);
        og.drawLine((int)x - 8, (int)start, (int)x + 8, (int)start);
        og.drawLine((int)x - 8, (int)end, (int)x + 8, (int)end);
        og.setColor(oldColor);

        super.render(og, alpha);
    }

    @Override
    public void setStartPosition(int pos)
    {
        startPos = pos;
        switch (pos)
        {
            case START_BOTTOM:
                py = end - height/2;
                break;
            case START_CENTER:
                py = y;
                break;
            case START_TOP:
                py = start + height/2;
                break;
            default:
                break;

        }
    }

    @Override
    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.start = this.y - trackLength*16/2;
        this.end = this.y + trackLength*16/2;
        this.px = x;
        setStartPosition(startPos);
    }

    @Override
    public Platform copy() {
        Platform platform = new PlatformH(this);
        platform.setPosition((int)this.x, (int)this.y);
        return platform;
    
    }

    public void print()
    {
        String text = String.format("x:%.0f y:%.0f px:%.0f py:%.0f width:%d height:%d trackLength:%d start:%.0f end:%.0f, ", x, y, px, py, width, height, trackLength, start, end);
        System.out.println(text);
    }
}
