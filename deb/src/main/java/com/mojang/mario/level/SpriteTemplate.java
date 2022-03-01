package com.mojang.mario.level;

import com.mojang.mario.LevelScene;
import com.mojang.mario.sprites.*;
import com.mojang.mario.util.Logger;

import java.awt.Graphics;

/**
 * SpriteTemplate describes a sprite within a level, and can be used
 * as a blueprint to instance enemies from.
 */
public class SpriteTemplate
{
    public int lastVisibleTick = -1;
    public Sprite sprite;
    public boolean isDead = false;
    private boolean winged;  
    private int type;
    
    /**
     * Constructor.
     * @param type Any of the types in Enemy.
     * @param winged Whether or not the enemy can fly. Usually only matters for
     *               red koopas and goombas.
     */
    public SpriteTemplate(int type, boolean winged)
    {
        this.type = type;
        this.winged = winged;
    }

    /**
     * Copy constructor.
     * @param other SpriteTemplate to copy from.
     */
    public SpriteTemplate(SpriteTemplate other)
    {
        this.lastVisibleTick = -1;
        this.sprite = null;
        this.isDead = false;

        if (other != null)
        {
            this.winged = other.winged;
            this.type = other.type;
        }
    }

    /**
     * Construct from a byte. Used when loading a level from a file.
     * @param code LSB is winged, other bits are type.
     */
    public SpriteTemplate(byte code)
    {
        Logger.setLevel(Logger.LEVEL_DEBUG);
        this.type = code & 0x0F;
        this.winged = ((code >> 7) & 0x01) == 1;
        Logger.d("SpriteTemplate", String.format("%d %d %s", code, this.type, this.winged));
    }

    /**
     * Construct a sprite template from a platform.
     * @param platform Platform to use.
     */
    public SpriteTemplate(Platform platform)
    {
        this.type = Hazard.HAZARD_PLATFORM;
        this.sprite = platform;
    }
    
    /**
     * spawn the sprite described by this template into @param world.
     * @param world LevelScene to spawn sprite into.
     * @param x x-coord to spawn sprite at.
     * @param y y-coord to spawn sprite at.
     * @param dir Direction sprite should face. -1 left, 1 right.
     */
    public void spawn(LevelScene world, int x, int y, int dir)
    {
        if (isDead) return;

        if (type==Enemy.ENEMY_FLOWER)
        {
            sprite = new FlowerEnemy(world, x*16+15, y*16+24);
        } 
        else if (type == Enemy.ENEMY_THWOMP)
        {
            sprite = new Thwomp(world, x*16+8, y*16+15);
        } 
        else if (type == Hazard.HAZARD_PLATFORM)
        {
            ((Platform)sprite).world = world;
        }
        else
        {
            sprite = new Enemy(world, x*16+8, y*16+15, dir, type, winged);
        }
        sprite.spriteTemplate = this;
        world.addSprite(sprite);
    }

    /**
     * Render the sprite onto the Graphics.
     * @param g Graphics object to render onto.
     * @param x Grid position to render at.
     * @param y Grid position to render at.
     * @param dir Direction sprite should face. -1 for left, 1 for right.
     */
    public void render(Graphics g, int x, int y, int dir)
    {
        if (type == Enemy.ENEMY_FLOWER)
        {
            sprite = new FlowerEnemy(null, x*16+15, y*16+24);
        }
        else if (type == Enemy.ENEMY_THWOMP)
        {
            sprite = new Thwomp(null, x*16+8, y*16+15);
        }
        else if (type == Hazard.HAZARD_PLATFORM)
        {

        }
        else
        {
            sprite = new Enemy(null, x*16+8, y*16+15, dir, type, winged);
        }
        sprite.spriteTemplate = this;
        sprite.render(g, 1f);
    }

    /**
     * Get the code represented by this sprite.
     * @return LSB=1 if winged, then other bits are type.
     */
    public byte getCode()
    {
        return (byte)(((byte)(this.winged?1:0) << 7) + ((byte)this.type & 0x0F));
    }

    /**
     * getCode for the SpriteTemplate.
     * @param sprite SpriteTemplate to get code from.
     * @return LSB=1 if winged, then other bits are type.
     */
    public static byte getCode(SpriteTemplate sprite)
    {
        if (sprite == null) return Enemy.ENEMY_NULL;
        else return sprite.getCode();
    }

    public String toString()
    {
        String typeString = "unsure " + type;
        switch (type)
        {
            case Enemy.ENEMY_RED_KOOPA:
                typeString = "red koopa";
                break;
            case Enemy.ENEMY_GREEN_KOOPA:
                typeString = "green koopa";
                break;
            case Enemy.ENEMY_GOOMBA:
                typeString = "goomba";
                break;
            case Enemy.ENEMY_SPIKY:
                typeString = "spiky";
                break;
            case Enemy.ENEMY_FLOWER:
                typeString = "flower";
                break;
            case Enemy.ENEMY_THWOMP:
                typeString = "thwomp";
                break;
            case Enemy.ENEMY_NULL:
                typeString = "null";
                break;
        }
        return String.format("SpriteTemplate: %s %s %d", typeString, String.valueOf(winged), getCode());
    }

    /**
     * getType of this sprite.
     * @return One of the constants in Hazard or Enemy.
     */
    public int getType()
    {
        return this.type;
    }

    /**
     * getWinged returned value of winged.
     * @return Whether or not this unit is winged.
     */
    public boolean getWinged()
    {
        return this.winged;
    }
}