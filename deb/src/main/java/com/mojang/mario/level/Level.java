package com.mojang.mario.level;

import java.io.*;

import com.mojang.mario.sprites.Enemy;


public class Level
{
    public static final String[] BIT_DESCRIPTIONS = {//
    "BLOCK UPPER", //
            "BLOCK ALL", //
            "BLOCK LOWER", //
            "SPECIAL", //
            "BUMPABLE", //
            "BREAKABLE", //
            "PICKUPABLE", //
            "ANIMATED",//
    };

    public static byte[] TILE_BEHAVIORS = new byte[256];

    public static final int BIT_BLOCK_UPPER = 1 << 0;
    public static final int BIT_BLOCK_ALL = 1 << 1;
    public static final int BIT_BLOCK_LOWER = 1 << 2;
    public static final int BIT_SPECIAL = 1 << 3;
    public static final int BIT_BUMPABLE = 1 << 4;
    public static final int BIT_BREAKABLE = 1 << 5;
    public static final int BIT_PICKUPABLE = 1 << 6;
    public static final int BIT_ANIMATED = 1 << 7;

    private static final int FILE_HEADER = 0x271c4178;
    
    private static final String MAP_FILE = "map.lvl";
    private static final String ENEMY_FILE = "enemy.lvl";
    private static final String HAZARD_FILE = "hazard.lvl";

    public int width;
    public int height;

    public byte[][] map;
    public byte[][] data;

    public SpriteTemplate[][] spriteTemplates;

    public int xExit;
    public int yExit;

    public Level(int width, int height)
    {
        this.width = width;
        this.height = height;

        xExit = 10;
        yExit = 10;
        map = new byte[width][height];
        data = new byte[width][height];
        spriteTemplates = new SpriteTemplate[width][height];
    }

    public static void loadBehaviors(DataInputStream dis) throws IOException
    {
        dis.readFully(Level.TILE_BEHAVIORS);
    }

    public static void saveBehaviors(DataOutputStream dos) throws IOException
    {
        dos.write(Level.TILE_BEHAVIORS);
    }

    public static Level load(DataInputStream dis) throws IOException
    {
        long header = dis.readLong();
        if (header != Level.FILE_HEADER) throw new IOException("Bad level header");
        @SuppressWarnings("unused")
		int version = dis.read() & 0xff;

        int width = dis.readShort() & 0xffff;
        int height = dis.readShort() & 0xffff;
        Level level = new Level(width, height);
        level.map = new byte[width][height];
        level.data = new byte[width][height];
        for (int i = 0; i < width; i++)
        {
            dis.readFully(level.map[i]);
            dis.readFully(level.data[i]);
        }

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                byte tmpByte = level.map[i][j];
                if (tmpByte == -1)
                {
                    level.map[i][j] = 0x00;
                    level.setLevelExit(tmpByte / 16, tmpByte % 16);
                }
            }
        }

        return level;
    }

    public static Level load(File levelDirectory) throws IOException
    {
        Level level = loadMap(new DataInputStream(new FileInputStream(levelDirectory + "/" + MAP_FILE)));
        loadEnemy(level, new DataInputStream(new FileInputStream(levelDirectory + "/" + ENEMY_FILE)));
        loadHazard(level, new DataInputStream(new FileInputStream(levelDirectory + "/" + HAZARD_FILE)));
        return level;
    }

    public void save(File levelDirectory) throws IOException
    {
        saveMap(new DataOutputStream(new FileOutputStream(levelDirectory + "/" + MAP_FILE)));
        saveEnemy(new DataOutputStream(new FileOutputStream(levelDirectory + "/" + ENEMY_FILE)));
        saveHazard(new DataOutputStream(new FileOutputStream(levelDirectory + "/" + HAZARD_FILE)));
    }

    public void save(DataOutputStream dos) throws IOException
    {
        dos.writeLong(Level.FILE_HEADER);
        dos.write((byte) 0);

        dos.writeShort((short) width);
        dos.writeShort((short) height);

        byte tmpByte = map[xExit][yExit];
        map[xExit][yExit] = -1;
        for (int i = 0; i < width; i++)
        {
            dos.write(map[i]);
            dos.write(data[i]);
        }
        map[xExit][yExit] = tmpByte;

        dos.close();
        
    }

    public static Level loadMap(DataInputStream dis) throws IOException
    {
        long header = dis.readLong();
        if (header != Level.FILE_HEADER) throw new IOException("Bad level header");
        @SuppressWarnings("unused")
		int version = dis.read() & 0xff;

        int width = dis.readShort() & 0xffff;
        int height = dis.readShort() & 0xffff;
        
        Level level = new Level(width, height);
        level.map = new byte[width][height];
        level.data = new byte[width][height];
        for (int i = 0; i < width; i++)
        {
            dis.readFully(level.map[i]);
            dis.readFully(level.data[i]);
        }

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                byte tmpByte = level.map[i][j];
                if (tmpByte == -1)
                {
                    level.map[i][j] = 0x00;
                    level.setLevelExit(tmpByte / 16, tmpByte % 16);
                }
            }
        }

        return level;
    }

    public static void loadEnemy(Level level, DataInputStream dis) throws IOException
    {
        long header = dis.readLong();
        if (header != Level.FILE_HEADER) throw new IOException("Bad level header");
        @SuppressWarnings("unused")
        int version = dis.read() & 0xff;
        
        level.spriteTemplates = new SpriteTemplate[level.width][level.height];
        byte[] buffer = new byte[level.height];
        for (int i = 0; i < level.width; i++)
        {
            dis.readFully(buffer);
            for (int j = 0; j < level.height; j++)
            {
                if (buffer[j] != Enemy.ENEMY_NULL)
                {
                    level.setSpriteTemplate(i, j, new SpriteTemplate(buffer[j]));
                }
            }
        }
    }

    public static void loadHazard(Level level, DataInputStream dis) throws IOException
    {

    }

    public void saveMap(DataOutputStream dos) throws IOException
    {
        dos.writeLong(Level.FILE_HEADER);
        dos.write((byte) 0);

        dos.writeShort((short) width);
        dos.writeShort((short) height);

        byte tmpByte = map[xExit][yExit];
        map[xExit][yExit] = -1;
        for (int i = 0; i < width; i++)
        {
            dos.write(map[i]);
            dos.write(data[i]);
        }
        map[xExit][yExit] = tmpByte;

        dos.close();
    }

    public void saveEnemy(DataOutputStream dos) throws IOException
    {
        dos.writeLong(Level.FILE_HEADER);
        dos.write((byte) 0);

        for (int i = 0; i < width; i++)
        {
            byte[] buffer = new byte[height];
            for (int j = 0; j < height; j++)
            {
                buffer[j] = SpriteTemplate.getCode(spriteTemplates[i][j]);
            }
        }
        dos.close();
    }

    public void saveHazard(DataOutputStream dos) throws IOException
    {

    }

    public void tick()
    {
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (data[x][y] > 0) data[x][y]--;
            }
        }
    }

    public byte getBlockCapped(int x, int y)
    {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;
        return map[x][y];
    }

    public byte getBlock(int x, int y)
    {
        if (x < 0) x = 0;
        if (y < 0) return 0;
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;
        return map[x][y];
    }

    public void setBlock(int x, int y, byte b)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        if (b != -1)
        {
            map[x][y] = b;
            if (xExit == x && yExit == y + 1)
            {
                xExit = 10;
                yExit = 10;
            }
        }
        else
        {
            map[x][y] = b;
            setLevelExit(x, y);
        }
    }

    public void setBlockData(int x, int y, byte b)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        data[x][y] = b;
    }

    public boolean isBlocking(int x, int y, float xa, float ya)
    {
        byte block = getBlock(x, y);
        boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
        blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
        blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;

        return blocking;
    }

    public SpriteTemplate getSpriteTemplate(int x, int y)
    {
        if (x < 0) return null;
        if (y < 0) return null;
        if (x >= width) return null;
        if (y >= height) return null;
        return spriteTemplates[x][y];
    }

    public void setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        spriteTemplates[x][y] = spriteTemplate;
    }

    public void setLevelExit(int x, int y)
    {
        xExit = x;
        yExit = y + 1;
    }

    // TODO: int x, int y, Hazard hazard
    public void setHazard()
    {

    }

    // TODO: int x, int y returns Hazard
    public void getHazard()
    {

    }
}