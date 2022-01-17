package com.mojang.mario.sprites;

import java.awt.Color;
import java.awt.Graphics;
import com.mojang.mario.LevelScene;

public abstract class Platform extends Sprite {
    public int width;
    public int height = 16;
    public int trackLength;
    public float start;
    public float end;

    public LevelScene world;
    public int direction = -1;
    public int speed = 4;
    public int startPos;
    public float px, py;
    


    public Platform(int x, int y, int width, int trackLength)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.trackLength = trackLength;

        this.px = x;
        this.py = y;
    }
    
    @Override
    public void move()
    {
        ya = speed * direction;
        if (direction == -1)
        {
            if (px - width*16/2 + ya < start)
            {
                direction = -1;
            }
            else
            {
                
            }
        }
        else if (direction == 1)
        {
            if (px - width*16/2 + ya > end)
            {
                direction = 1;
            }
        }
    }

    public boolean move(float xa, float ya)
    {
        return true;
    }

    @Override
    public void collideCheck()
    {

    }

    public boolean isBlocking(float _x, float _y, float xa, float ya)
    {
        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;

        boolean blocking = world.level.isBlocking(x, y, xa, ya);

        @SuppressWarnings("unused")
		byte block = world.level.getBlock(x, y);

        return blocking;    
    }

    @Override
    public void render(Graphics og, float alpha)
    {
        Color oldColor = og.getColor();
        og.setColor(Color.ORANGE);
        og.fillRect((int)px - width*16/2, (int)py - height/2 + 8, width*16, height);
        og.setColor(oldColor);
    }

    public abstract void setStartPosition(int pos);


    public abstract void setPosition(int x, int y);

    public abstract Platform copy();

    public abstract void print();
}
