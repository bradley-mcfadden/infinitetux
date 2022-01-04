package com.mojang.mario.level;

import com.mojang.mario.LevelScene;
import com.mojang.mario.sprites.*;

public class SpriteTemplate
{
    public int lastVisibleTick = -1;
    public Sprite sprite;
    public boolean isDead = false;
    private boolean winged;
    
    private int type;
    
    public SpriteTemplate(int type, boolean winged)
    {
        this.type = type;
        this.winged = winged;
    }

    public SpriteTemplate(byte code)
    {
        this.type = code ^ 0x80;
        this.winged = (code >> 7) == 1;
    }
    
    public void spawn(LevelScene world, int x, int y, int dir)
    {
        if (isDead) return;

        if (type==Enemy.ENEMY_FLOWER)
        {
            sprite = new FlowerEnemy(world, x*16+15, y*16+24);
        }
        else
        {
            sprite = new Enemy(world, x*16+8, y*16+15, dir, type, winged);
        }
        sprite.spriteTemplate = this;
        world.addSprite(sprite);
    }

    public byte getCode()
    {
        return (byte)((this.winged?1:0 << 7) + this.type);
    }

    public static byte getCode(SpriteTemplate sprite)
    {
        if (sprite == null) return Enemy.ENEMY_NULL;
        else return (byte)sprite.type;
    }
}