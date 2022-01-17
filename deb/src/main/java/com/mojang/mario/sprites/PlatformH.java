package com.mojang.mario.sprites;

import java.awt.Color;
import java.awt.Graphics;

public class PlatformH extends Platform {

    public static final int START_LEFT = 10;
    public static final int START_CENTER = 11;
    public static final int START_RIGHT = 12;

    public PlatformH(int x, int y, int width, int trackLength) {
        super(x, y, width, trackLength);

        this.start = x - trackLength*16/2;
        this.end = x + trackLength*16/2;
    }

    public PlatformH(Platform platform)
    {
        super((int)platform.x, (int)platform.y, (int)platform.width, (int)platform.trackLength);

        this.start = platform.x - platform.trackLength * 16 / 2;
        this.end = platform.x + platform.trackLength * 16 / 2;
    }

    @Override
    public void move()
    {
        xa = speed * direction;
        if (direction == -1)
        {
            if (px - width*16/2 + xa < start)
            {
                direction = 1;
                px = start + width*16/2;
            }
            else
            {
                px += xa;
            }
        }
        else if (direction == 1)
        {
            if (px + width*16/2 + ya > end)
            {
                direction = -1;
                px = end - width*16/2;
            }
            else
            {
                px += xa;
            }
        }
    }

    @Override
    public boolean move(float xa, float ya)
    {
        return true;
    }

    @Override
    public void collideCheck()
    {

    }

    @Override
    public void render(Graphics og, float alpha)
    {
        Color oldColor = og.getColor();
        og.setColor(Color.BLACK);
        og.drawLine((int)start, (int)y + 8, (int)end, (int)y + 8);
        og.drawLine((int)start, (int)y, (int)start, (int)y + 16);
        og.drawLine((int)end, (int)y, (int)end, (int)y + 16);
        og.setColor(oldColor);
        
        super.render(og, alpha);
    }
    
    @Override
    public void setStartPosition(int pos)
    {
        startPos = pos;
        switch (pos)
        {
            case START_LEFT:
                px = start + width*16/2;
                break;
            case START_CENTER:
                px = x;
                break;
            case START_RIGHT:
                px = end - width*16/2;
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
        this.start = this.x - trackLength*16/2;
        this.end = this.x + trackLength*16/2;
        this.py = y;
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
