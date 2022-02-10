package com.mojang.mario;

import com.mojang.mario.level.*
;
import java.awt.*;

public class TestScene extends LevelScene {

    public TestScene(Level level, GraphicsConfiguration graphicsConfiguration, MarioComponent renderer, long seed, int levelDifficulty, int type) 
    {
        super(graphicsConfiguration, renderer, seed, levelDifficulty, type);
        this.level = level;
    }

    public void setGraphicsConfiguration(GraphicsConfiguration graphicsConfiguration)
    {
        this.graphicsConfiguration = graphicsConfiguration;
    }

    public void setRenderer(MarioComponent renderer)
    {
        this.renderer = renderer;
    }

    public void init() 
    {
        loadTileData();
        setupMusic();
        finishInit();
    }
}
