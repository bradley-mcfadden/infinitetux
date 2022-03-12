package com.mojang.mario.util;

import com.mojang.mario.level.Tile;

/**
 * LevelAdapter contains methods for converting between ch.idsia Levels and com.mojang.mario Levels.
 */
public class LevelAdapter {
    /**
     * convert a com.mojang.mario.level.Level into a ch.idsia.mario.engine.level.Level
     * @param level The mojang level to convert
     * @return A idsia level with all common tiles and enemies shared between implementations.
     */
    public static ch.idsia.mario.engine.level.Level convert(com.mojang.mario.level.Level srcLevel) {

        ch.idsia.mario.engine.level.Level dstLevel = new ch.idsia.mario.engine.level.Level(srcLevel.width, srcLevel.height);
        
        for (int i = 0; i < srcLevel.width; i++)
        {
            // map, data
            // System.arraycopy(srcLevel.map[i], 0, dstLevel.map[i], 0, srcLevel.height);
            // System.arraycopy(srcLevel.data[i], 0, dstLevel.data[i], 0, srcLevel.height);
            for (int j = 0; j < srcLevel.height; j++)
            {
                byte b = srcLevel.map[i][j];
                if (b == Tile.ANCHOR_POINT || b == Tile.PRESERVE_POINT)
                {

                }
                else
                {
                    dstLevel.map[i][j] = b;
                    dstLevel.data[i][j] = b;
                }
            }
            // sprites
            for (int j = 0; j < srcLevel.height; j++)
            {
                com.mojang.mario.level.SpriteTemplate srcSt = srcLevel.spriteTemplates[i][j];
                if (srcSt != null) 
                {
                    int type = srcSt.getType();
                    // Only consider enemies between Red Koopa and Flower
                    if (type >= 0 && type < 5) 
                    {
                        ch.idsia.mario.engine.level.SpriteTemplate dstSt = new ch.idsia.mario.engine.level.SpriteTemplate(srcSt.getType(), srcSt.getWinged()); 
                        dstLevel.spriteTemplates[i][j] = dstSt;
                    }
                }
            }
        }
        
        // level exit
        dstLevel.xExit = srcLevel.xExit;
        dstLevel.yExit = srcLevel.yExit;

        // tile behaviours ?
        System.arraycopy(
            com.mojang.mario.level.Level.TILE_BEHAVIORS, 0, 
            ch.idsia.mario.engine.level.Level.TILE_BEHAVIORS, 0, 
            com.mojang.mario.level.Level.TILE_BEHAVIORS.length
        );

        return dstLevel;
    }
}
