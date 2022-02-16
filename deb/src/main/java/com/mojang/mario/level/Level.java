package com.mojang.mario.level;

import java.io.*;
import java.util.*;
import com.mojang.mario.sprites.*;


/**
 * Level - representation for a level as used by the level editor and
 * in game.
 */
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
    public List<SpriteTemplate> hazards;

    public int xExit;
    public int yExit;

    /**
     * Constructor. 
     * @param width Width of level
     * @param height Height of level
     */
    public Level(int width, int height)
    {
        this.width = width;
        this.height = height;

        xExit = 10;
        yExit = 10;
        map = new byte[width][height];
        data = new byte[width][height];
        spriteTemplates = new SpriteTemplate[width][height];
        hazards = new ArrayList<>();
    }

    /**
     * Deep copy constructor.
     * @param other Level to copy from.
     */
    public Level(Level other)
    {
        this.width = other.width;
        this.height = other.height;
        this.xExit = other.xExit;
        this.yExit = other.yExit;
        
        map = new byte[width][height];
        data = new byte[width][height];
        spriteTemplates = new SpriteTemplate[width][height];
        hazards = new ArrayList<>(other.hazards.size());
        
        for (int i = 0; i < map.length; i++)
        {
            System.arraycopy(other.map[i], 0, map[i], 0, map[i].length);
        }

        for (int i = 0; i < data.length; i++)
        {
            System.arraycopy(other.data[i], 0, data[i], 0, data[i].length);
        }

        for (int i = 0; i < spriteTemplates.length; i++) {
            for (int j = 0; j < spriteTemplates[i].length; j++) {
                SpriteTemplate temp = other.spriteTemplates[i][j];
                if (temp != null) 
                {
                    spriteTemplates[i][j] = new SpriteTemplate(temp);
                }
            }
        }

        int n = other.hazards.size();
        for (int i = 0; i < n; i++) 
        {
            hazards.add(new SpriteTemplate((SpriteTemplate)other.hazards.get(i)));
        }

    }

    /**
     * Loads tile behaviours into array. Not sure what this means.
     * @param dis Stream to load from.
     * @throws IOException If there is an issue with @param dis
     */
    public static void loadBehaviors(DataInputStream dis) throws IOException
    {
        dis.readFully(Level.TILE_BEHAVIORS);
    }

    /**
     * Saves tile behaviours into array. Not sure what this means.
     * @param dos Stream to load from.
     * @throws IOException If there is an issue with @param dos
     */
    public static void saveBehaviors(DataOutputStream dos) throws IOException
    {
        dos.write(Level.TILE_BEHAVIORS);
    }

    /**
     * This was the old way of loading a level, from a .lvl file.
     * @Deprecated Currently isn't used, should probably be deleted.
     * @param dis Stream to load level from
     * @return Level constructed from @param dis.
     * @throws IOException If a problem occurs with @param dis
     */
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

    /**
     * Load level from a directory. 
     * @param levelDirectory File (Folder) where level contents are stored.
     *                       Should contain hazard.lvl, enemy.lvl, map.lvl
     * @return Constructed level.
     * @throws IOException If the level directory is corrupt.
     */
    public static Level load(File levelDirectory) throws IOException
    {
        Level level = loadMap(new DataInputStream(new FileInputStream(levelDirectory + File.separator + MAP_FILE)));
        loadEnemy(level, new DataInputStream(new FileInputStream(levelDirectory + File.separator + ENEMY_FILE)));
        loadHazard(level, new DataInputStream(new FileInputStream(levelDirectory + File.separator + HAZARD_FILE)));
        return level;
    }

    /**
     * Save level to a directory.
     * Saves level to the directory, with three parts: enemy.lvl, map.lvl, hazard.lvl
     * @param levelDirectory File (Folder) to save directory at
     * @throws IOException If there's a problem saving any of the files.
     */
    public void save(File levelDirectory) throws IOException
    {
        saveMap(new DataOutputStream(new FileOutputStream(levelDirectory + File.separator + MAP_FILE)));
        saveEnemy(new DataOutputStream(new FileOutputStream(levelDirectory + File.separator + ENEMY_FILE)));
        saveHazard(new DataOutputStream(new FileOutputStream(levelDirectory + File.separator + HAZARD_FILE)));
    }

    /**
     * Save level to a data output steam.
     * @Deprecated This was used by the old level editor. Shouldn't be used now.
     * @param dos Stream pointing to a file to save the level at.
     * @throws IOException If there is some problem with @param dos
     */
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

    /**
     * Construct a level, and load map and block data into it.
     * @param dis File (map.lvl) to load map information from.
     * @return Partial level that was constructed.
     * @throws IOException If there is some problem with the stream, or the file header is bad.
     */
    public static Level loadMap(DataInputStream dis) throws IOException
    {
        long header = dis.readLong();
        if (header != Level.FILE_HEADER) throw new IOException("Bad file header");
        @SuppressWarnings("unused")
		int version = dis.read() & 0xff;

        int width = dis.readShort() & 0xffff;
        int height = dis.readShort() & 0xffff;
        // System.out.printf("Width %d Height %d\n", width, height);
        
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

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                //System.out.printf("%+04d ", level.map[i][j]);
            }
            //System.out.println();
        }

        return level;
    }

    /**
     * Load enemies into @param level.
     * @param level Level object to load enemies into.
     * @param dis Stream to read file from.
     * @throws IOException If there is a problem with the stream, or the file header is bad.
     */
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
                    //System.out.println(buffer[j]);
                    level.setSpriteTemplate(i, j, new SpriteTemplate(buffer[j]));
                }
            }
        }
    }

    /**
     * Load hazards into @param level
     * @param level Level object ot load hazards into.
     * @param dis Stream to read file from.
     * @throws IOException If there is a problem with the stream, or the file header is bad.
     */
    public static void loadHazard(Level level, DataInputStream dis) throws IOException
    {
        long header = dis.readLong();
        if (header != Level.FILE_HEADER) throw new IOException("Bad level header");
        @SuppressWarnings("unused")
        int version = dis.read() & 0xff;

        level.hazards = new ArrayList<>();

        byte[] rep = new byte[5];
        while(dis.read(rep, 0, 5) > 0)
        {
            int type = rep[0] & 0x0F;
            if (type == Hazard.HAZARD_PLATFORM)
            {
                boolean ori = ((rep[0] >> (byte)4) & (byte)0x01) == 1;
                int startPos = (rep[0] >> (byte)5) & (byte)0x11;
                int x = rep[1];
                int y = rep[2];
                int width = rep[3];
                int trackLength = rep[4];
                
                Platform platform;
                if (ori)
                {
                    platform = new PlatformH(x, y, width, trackLength);
                    platform.setStartPosition(startPos);
                }
                else
                {
                    platform = new PlatformV(x, y, width, trackLength);
                    platform.setStartPosition(startPos);
                }
                level.hazards.add(new SpriteTemplate(platform));
            }
        }
        dis.close();
    }

    /**
     * Saves the map (blocks and block data) into @param dos.
     * @param dos Stream that should point to a file name map.lvl.
     * @throws IOException If there is a problem with the stream.
     */
    public void saveMap(DataOutputStream dos) throws IOException
    {
        dos.writeLong(Level.FILE_HEADER);
        dos.write((byte) 0);

        dos.writeShort((short) width);
        dos.writeShort((short) height);

        //System.out.printf("Width %d Height %d\n", width, height);

        byte tmpByte = (byte)0;
        if (xExit != -1 && yExit != -1) {
            tmpByte = map[xExit][yExit];
            map[xExit][yExit] = -1;
        }
        for (int i = 0; i < width; i++)
        {
            //System.out.println(map[i].length);
            //System.out.println(data[i].length);
            dos.write(map[i]);
            dos.write(data[i]);
        }

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                //System.out.printf("%+04d ", map[i][j]);
            }
            //System.out.println();
        }
        //System.out.println();
        if (xExit != -1 && yExit != -1)
        {
            map[xExit][yExit] = tmpByte;
        }

        dos.close();
    }

    /**
     * Saves enemy data to @param dos.
     * @param dos Stream that should point to a file named enemy.lvl.
     * @throws IOException If there is a problem with the stream.
     */
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
            dos.write(buffer);
        }
        dos.close();
    }

    /**
     * Saves hazard data to @param dos.
     * @param dos Stream that should point to a file named hazard.lvl.
     * @throws IOException If there is a problem with the stream.
     */
    public void saveHazard(DataOutputStream dos) throws IOException
    {
        dos.writeLong(Level.FILE_HEADER);
        dos.write((byte)0);

        byte[] rep = new byte[5];
        for (SpriteTemplate st : hazards)
        {
            if (st.sprite instanceof Platform)
            {
                Platform plat = (Platform)st.sprite;
                rep[0] = (byte) Hazard.HAZARD_PLATFORM & 0x0F;
                rep[0] |= plat instanceof PlatformH?1<<4:0;
                int startPosCode = 0;
                if (plat.startPos == PlatformH.START_LEFT || plat.startPos == PlatformV.START_TOP)
                {
                    startPosCode = 1;
                }
                else if (plat.startPos == PlatformH.START_CENTER || plat.startPos == PlatformV.START_CENTER)
                {
                    startPosCode = 2;
                }
                else if (plat.startPos == PlatformH.START_RIGHT || plat.startPos == PlatformV.START_BOTTOM)
                {
                    startPosCode = 3;
                }
                rep[0] |= (byte)startPosCode<<5;
                rep[1] = (byte)plat.x;
                rep[2] = (byte)plat.y;
                rep[3] = (byte)plat.width;
                rep[4] = (byte)plat.trackLength;

                dos.write(rep);
            }
        }
        dos.close();
    }

    /**
     * Not sure what the point of this is.
     */
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

    /**
     * Returns the block at x, y.
     * If x or y are out of range, bound them.
     * @param x x-value of block to return [0, width)
     * @param y y-value of block to return [0, height)
     * @return Block at x, y
     */
    public byte getBlockCapped(int x, int y)
    {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;
        return map[x][y];
    }

    /**
     * Returns the block at x, y.
     * If x or y are out of range, return 0.
     * @param x x-value of block to return [0, width)
     * @param y y-value of block to return [o, height)
     * @return Block at x, y
     */
    public byte getBlock(int x, int y)
    {
        if (x < 0) x = 0;
        if (y < 0) return 0;
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;
        return map[x][y];
    }

    /**
     * Set the block at @param x, @param y to @param b.
     * Returns if x or y are out of bounds.
     * If setting the block as a level exit (code -1),
     * will also call setLevelExit(x, y).
     * @param x x-value of block to set.
     * @param y y-value of block to set.
     * @param b Code to set the block to.
     * @see setLevelExit
     */
    public void setBlock(int x, int y, byte b)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        if (b != -1)
        {
            map[x][y] = b;
            if (xExit == x && yExit == y)
            {
                xExit = 10;
                yExit = 10;
            }
        }
        else
        {
            map[xExit][yExit] = (byte)0;
            map[x][y] = b;
            setLevelExit(x, y);
        }
    }

    /**
     * Set block data at x, y.
     * Returns if out of range.
     * @param x x-value of block
     * @param y y-value of block
     * @param b Data to set for block.
     */
    public void setBlockData(int x, int y, byte b)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        data[x][y] = b;
    }

    /**
     * Checks if block at x,y should stop sprites from passing through.
     * A block should stop sprites if it has BIT_BLOCK_ALL, BIT_BLOCK_UPPER, BIT_BLOCK_LOWER
     * @param x x-value of block to check
     * @param y y-value of block to check
     * @param xa x-velocity of sprite
     * @param ya y-velocity of sprite
     * @return Returns true if block should stop entity.
     */
    public boolean isBlocking(int x, int y, float xa, float ya)
    {
        byte block = getBlock(x, y);
        boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
        blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
        blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;

        return blocking;
    }

    /**
     * Returns the @see SpriteTemplate at x, y
     * @param x x-value of grid
     * @param y y-value of grid
     * @return SpriteTemplate at x, y or null if one is not set.
     */
    public SpriteTemplate getSpriteTemplate(int x, int y)
    {
        if (x < 0) return null;
        if (y < 0) return null;
        if (x >= width) return null;
        if (y >= height) return null;
        return spriteTemplates[x][y];
    }

    /**
     * Set the @see SpriteTemplate at x, y
     * @param x x-value of tile to set
     * @param y y-value of tile to set
     * @param spriteTemplate SpriteTemplate to set at x, y. May be null.
     */
    public void setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        spriteTemplates[x][y] = spriteTemplate;
    }

    /**
     * Move the level exit from xExit, yExit to x, y
     * @param x x-value to move level exit to.
     * @param y y-value to move level exit to.
     */
    public void setLevelExit(int x, int y)
    {
        setBlock(xExit, yExit, (byte)0);
        xExit = x;
        yExit = y;
    }

    /**
     * addHazard to the level. Position set within the Sprite itself.
     * @param hazard SpriteTemplate of a hazard to add to the level.
     */
    public void addHazard(SpriteTemplate hazard)
    {
        hazards.add(hazard);
    }

    /**
     * removeHazard overlapping with xTile, yTile
     * @param xTile xTile to check hazards for
     * @param yTile yTile to check hazards for
     */
    public void removeHazard(int xTile, int yTile)
    {
        int n = hazards.size();
        for (int i = n - 1; i >= 0; i--)
        {
            SpriteTemplate hazard = hazards.get(i);
            if (hazard.getType() == Hazard.HAZARD_PLATFORM)
            {
                Platform plat = (Platform)hazard.sprite;
                int px = (int)plat.px / 16;
                int py = (int)plat.py / 16;
                int w2 = plat.width / 2;
                if (xTile >= px - w2 && xTile < px + w2 && yTile == py)
                {
                    hazards.remove(hazard);
                }
            }
        }
    }

    /**
     * resize allows the level width and height to be changed.
     * The level exit is moved to the default position if it gets clipped.
     * Sprites and hazards are moved as well.
     */
    public void resize(int startX, int startY, int newWidth, int newHeight) {
        if (newWidth == width && newHeight == height) return;
        // allocate new arrays for blocks, sprites
        int endX = startX + Math.min(newWidth, map.length);
        int endY = startY + Math.min(newHeight, map[0].length);
        int ny = endY - startY;
        byte[][] tmpMap = new byte[newWidth][newHeight];
        byte[][] tmpData = new byte[newWidth][newHeight];
        SpriteTemplate[][] tmpSprite = new SpriteTemplate[newWidth][newHeight];
        
        
        width = newWidth;
        height = newHeight;
        for (int i = startX; i < endX; i++) 
        {
            System.arraycopy(map[i], startY, tmpMap[i-startX], 0, ny);
            System.arraycopy(data[i], startY, tmpData[i-startX], 0, ny);
            for (int j = startY; j < endY; j++) 
            {
                if (spriteTemplates[i][j] != null)
                {
                    tmpSprite[i-startX][j-startY] = new SpriteTemplate(spriteTemplates[i][j]);
                }
            }
        }
        map = tmpMap;
        data = tmpData;
        spriteTemplates = tmpSprite;

        // Remove any platforms that end up out of bounds
        int nHazards = hazards.size();
        for (int i = nHazards - 1; i >= 0; i--)
        {
            SpriteTemplate st = hazards.get(i);
            if (st.sprite != null)
            {
                if (st.sprite instanceof Platform)
                {
                    Platform platform = (Platform)st.sprite;
                    int tStart = (int)platform.start / 16;
                    int tEnd = (int)platform.end / 16;
                    if (tStart < startX && tEnd > endX)
                    {
                        hazards.remove(st);
                    }
                    else
                    {
                        Platform tmpPlatform;
                        if (platform instanceof PlatformH)
                        {
                            tmpPlatform = new PlatformH((int)platform.x + startX, (int)platform.y + endY, platform.width, platform.trackLength);
                        }
                        else
                        {
                            tmpPlatform = new PlatformV((int)platform.x + startX, (int)platform.y + endY, platform.width, platform.trackLength);
                        }
                        tmpPlatform.setStartPosition(platform.startPos);
                        st.sprite = tmpPlatform;
                    }
                }
            }
        }

        // Move the level exit
        if (xExit < startX || xExit > endX)
        {
            setLevelExit(10, 10);
        }
    }

    /**
     * clearArea is a bounded delete. It sets tiles to air,
     * removes enemies and hazards in the area. The bounds
     * of the area are capped at the upper and lower ends of
     * the level.
     * @param x x tile of area's upper left corner
     * @param y y tile of area's upper left corner
     * @param w width in tiles of area to delete
     * @param h height in tiles of area to delete
     */
    public void clearArea(int x, int y, int w, int h)
    {
        // clear tiles and enemies
        int ex = x + w;
        int ey = y + h; 
        for (int xi = x; xi < ex; xi++)
        {
            for (int yi = y; yi < ey; yi++)
            {
                map[xi][yi] = Tile.AIR;
                data[xi][yi] = (byte)0;
                spriteTemplates[xi][yi] = null;
            }
        }
        int n = hazards.size();
        for (int i = n - 1; i >= 0; i--)
        {
            SpriteTemplate st = hazards.get(i);
            if (st.sprite != null)
            {
                if (st.sprite instanceof Platform)
                {
                    Platform plat = (Platform)st.sprite;
                    int px = (int)plat.px/16;
                    int py = (int)plat.py/16;
                    int pw = plat.width;
                    int ph = plat.height;

                    if (px + pw/2 <= ex && px - pw/2 > x && py + ph/2 <= ey && py - ph/2 > y)
                    {

                    }  
                    else
                    {
                        hazards.remove(st);
                    }
                }
            }
        }
    }

    /**
     * getArea returns a subsection of this level. Not bounded.
     * @param x x tile of upper left corner of area to sample
     * @param y y tile of upper left corner of area to sample
     * @param w width in tiles of area to sample
     * @param h height in tiles of area to sample
     * @return Subsection of Level, or null if x,y,w,h is not a 
     *         subsection of this level.
     */
    public Level getArea(int x, int y, int w, int h)
    {
        Level tmp = new Level(this);
        tmp.resize(x, y, w, h);
        return tmp;
    }

    /**
     * setArea overwrites part of the level from x, y, to
     * level.width+x, level.height+y. Hazards are added
     * if they are contained in the subsection.
     * This method is bounded.
     * @param level Level to overwrite with
     * @param x xTile to start overwriting at
     * @param y yTile to start overwriting at
     * @throws NullPointerException if level is null
     */
    public void setArea(Level level, int x, int y)
    {
        int w = level.width;
        int h = level.height;
        int ex = Math.min(x + w, width);
        int ey = Math.min(y + h, height);

        for (int xi = x; xi < ex; xi++)
        {
            for (int yi = y; yi < ey; yi++)
            {
                map[xi][yi] = level.map[xi-x][yi-y];
                data[xi][yi] = level.data[xi-x][yi-y];
                if (level.spriteTemplates[xi-x][yi-y] != null) 
                {
                    spriteTemplates[xi][yi] = new SpriteTemplate(level.spriteTemplates[xi-x][yi-y]);
                }
            }
        }

        int n = level.hazards.size();
        for (int i = n - 1; i >= 0; i--)
        {
            SpriteTemplate st = level.hazards.get(i);
            if (st.sprite != null)
            {
                if (st.sprite instanceof Platform)
                {
                    Platform plat = (Platform)st.sprite;
                    int px = (int)plat.px/16;
                    int py = (int)plat.py/16;
                    int pw = plat.width;
                    int ph = plat.height;

                    if (px + pw/2 <= ex && px - pw/2 > x && py + ph/2 <= ey && py - ph/2 > y)
                    {
                        hazards.add(new SpriteTemplate(plat));
                    }  
                }
            }
        }
    }
}