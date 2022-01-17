package com.mojang.mario.sprites;

import java.awt.Color;
import java.awt.Graphics;

import com.mojang.mario.LevelScene;

public class Thwomp extends Sprite {

    private LevelScene world;
    private int direction;
    private int state;
    private int speed = 2;
    int width = 8;
    int height = 36;


    private static final int STATE_WAIT = 10;
    private static final int STATE_CRUSH = 20;
    private static final int STATE_RISE = 30;
    

    public Thwomp(LevelScene world, int x, int y)
    {
        this.x = x;
        this.y = y;
        this.world = world;

        state = STATE_WAIT;
    }

    @Override
    public void move() 
    {
        if (state == STATE_WAIT)
        {
            // if mario is below me
            float xMarioD = world.mario.x - x;
            float yMarioD = world.mario.y - y;

            if (xMarioD > -width*2-4 && xMarioD < width*2+4)
            {
                if (yMarioD > 0)
                {
                    state = STATE_CRUSH;
                    direction = 1;
                    ya = direction * speed;
                }
            }
        }
        else if (state == STATE_CRUSH || state == STATE_RISE) 
        {
            move(0, ya);
        }
    }

    private boolean move(float xa, float ya)
    {
        boolean collide = false;
        if (ya > 0)
        {
            if (isBlocking(x + xa - width, y + ya + height, xa, 0)) collide = true;
            else if (isBlocking(x + xa + width, y + ya + height, xa, 0)) collide = true;
            else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
            else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;
            
            if (collide)
            {
                state = STATE_RISE;
                y = (int) (y / 16 + 1) * 16 - 1;
                direction = -1;
                this.ya = direction * speed;
            }
        }
        else if (ya < 0)
        {
            if (isBlocking(x + xa - width, y + ya - 16, xa, 0)) collide = true;
            else if (isBlocking(x + xa + width, y + ya - 16, xa, 0)) collide = true;
            else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
            else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;
            if (y - 16 - ya < 0) collide = true;

            if (collide)
            {
                state = STATE_WAIT;
                this.ya = 0;
                y = (int) (y / 16) * 16 - 1;
            }
        }

        if (collide)
        {
            return false;
        }
        else
        {
            x += xa;
            y += ya;
            return true;
        }
    }

    @Override
    public void collideCheck()
    {
        float leftBound = x - 16;
        float rightBound = leftBound + 32;
        float upperBound = y - 16;
        float lowerBound = upperBound + 48 + ya;

        float dx1 = leftBound - world.mario.x + 8;
        float dx2 = rightBound - (world.mario.x - 8);
        float dy1 = upperBound - world.mario.y;
        float dy2 = lowerBound - (world.mario.y - world.mario.height) + 4;
        if (dx1 < 0 && dx2 > 0)
        {
            if (dy1 < 0 && dy2 > 0)
                world.mario.getHurt();   
        }
    }

    private boolean isBlocking(float _x, float _y, float xa, float ya)
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
        og.setColor(Color.RED);
        og.fillRect((int)x - 8, (int)y - 16 + 1, 32, 48);
        og.setColor(oldColor);   
    }
}
