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
                    SpriteTemplate st = new SpriteTemplate(buffer[j]);
                    level.setSpriteTemplate(i, j, st);
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
        System.out.println();
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
            try {
            System.arraycopy(map[i], startY, tmpMap[i-startX], 0, ny);
            System.arraycopy(data[i], startY, tmpData[i-startX], 0, ny);
            for (int j = startY; j < endY; j++) 
            {
                if (spriteTemplates[i][j] != null)
                {
                    tmpSprite[i-startX][j-startY] = new SpriteTemplate(spriteTemplates[i][j]);
                }
            }
            } catch (ArrayIndexOutOfBoundsException ai)
            {
                System.out.printf("Tried to do array copy from %d to %d on array of length %d by %d\n", startY, startY +ny, map.length, map[0].length);
                System.out.printf("Dest array %d to %d length %d by %d\n", 0, ny, tmpMap.length, tmpMap[0].length);
                ai.printStackTrace();
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
        System.out.printf("setting area at %d %d\n", x,y );
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

    public void safeSetArea(Level level, int x, int y)
    {
        Point[] bounds = clip(level, x, y);
        if (bounds == null)
        {
            level.resize(0, 0, 1, 1);
            level.map[0][0] = Tile.AIR;
        }
        else
        {
            Point min = bounds[0];
            Point size = bounds[1];
            level.resize(min.x-x, min.y-y, size.x, size.y);
            mergeArea(level, min.x, min.y);
            System.out.printf("new level segment is %d %d and %d %d\n", min.x, min.y, size.x, size.y);
        }
    }

    public Point[] clip(Level level, int x, int y)
    {
        System.out.printf("clipping level %d %d %d %d\n", x, y, level.width, level.height);
        int sy = y;
        int ey = y + level.height;
        int sx = x;
        int ex = x + level.width;
    
        ArrayList<Point> leftClipper = new ArrayList<>();
        ArrayList<Point> rightClipper = new ArrayList<>();
        ArrayList<Point> bottomClipper = new ArrayList<>();
        ArrayList<Point> topClipper = new ArrayList<>();
        ArrayList<Point> out = new ArrayList<>();
        leftClipper.add(new Point(ex, ey));
        leftClipper.add(new Point(ex, sy));
        leftClipper.add(new Point(sx, sy));
        leftClipper.add(new Point(sx, ey));
        leftClipper.add(leftClipper.get(0));
        for (int i = 0; i < leftClipper.size()-1; i++)
        {
            Point pt1 = leftClipper.get(i);
            Point pt2 = leftClipper.get(i+1);
            if (pt1.x >= 0)
            {
                if (pt2.x >= 0)
                {
                    rightClipper.add(pt1);
                }
                else
                {
                    rightClipper.add(new Point(0, pt2.y));
                }
            }
            else
            {
                if (pt2.x >= 0)
                {
                    rightClipper.add(new Point(0, pt1.y));
                    rightClipper.add(pt2);
                }
            }
        }
        if (rightClipper.size() > 0)
        rightClipper.add(rightClipper.get(0));
        // System.out.println("After left clipping");
        
        // for (Point p : rightClipper)
        // {
            // System.out.printf("%d,%d ", p.x, p.y);
        // }
        // System.out.println();
        for (int i = 0; i < rightClipper.size()-1; i++)
        {
            Point pt1 = rightClipper.get(i);
            Point pt2 = rightClipper.get(i+1);
            if (pt1.x < width)
            {
                if (pt2.x < width)
                {
                    bottomClipper.add(pt1);
                }
                else
                {
                    bottomClipper.add(new Point(width - 1, pt2.y));
                }
            }
            else
            {
                if (pt2.x < width)
                {
                    bottomClipper.add(new Point(width - 1, pt1.y));
                    bottomClipper.add(pt2);
                }
            }
        }
        if (bottomClipper.size() > 0)
        bottomClipper.add(bottomClipper.get(0));
        // System.out.println("After right clipping");
        // for (Point p : bottomClipper)
        // {
            // System.out.printf("%d,%d ", p.x, p.y);
        // }
        // System.out.println();
        for (int i = 0; i < bottomClipper.size()-1; i++)
        {
            Point pt1 = bottomClipper.get(i);
            Point pt2 = bottomClipper.get(i+1);
            if (pt1.y >= 0)
            {
                if (pt2.y >= 0)
                {
                    topClipper.add(pt1);
                }
                else
                {
                    topClipper.add(new Point(pt2.x, 0));
                }
            }
            else
            {
                if (pt2.y >= 0)
                {
                    topClipper.add(new Point(pt2.x, 0));
                    topClipper.add(pt2);
                }
            }
        }
        if (topClipper.size() > 0)
        topClipper.add(topClipper.get(0));
        // System.out.println("After bottom clipping");
        // for (Point p : topClipper)
        // {
           // System.out.printf("%d,%d ", p.x, p.y);
        // }
        // System.out.println();
        for (int i = 0; i < topClipper.size()-1; i++)
        {
            Point pt1 = topClipper.get(i);
            Point pt2 = topClipper.get(i+1);
            if (pt1.y < height)
            {
                if (pt2.y < height)
                {
                    out.add(pt1);
                }
                else
                {
                    out.add(new Point(pt2.x, height - 1));
                }
            }
            else
            {
                if (pt2.y < height)
                {
                    out.add(new Point(pt2.x, height - 1));
                    out.add(pt2);
                }
            }
        }
        System.out.println("After top clipping");
        for (Point p : out)
        {
            System.out.printf("%d,%d ", p.x, p.y);
        }
        System.out.println();
        if (out.size() > 0) 
        {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (Point pt : out)
            {
                if (pt.x < minX) minX = pt.x;
                if (pt.x > maxX) maxX = pt.x;
                if (pt.y < minY) minY = pt.y;
                if (pt.y > maxY) maxY = pt.y;
            }
            return new Point[]{
                new Point(minX, minY),
                new Point(maxX-minX, maxY-minY+1)
            };
        }
        else
        {
            return null;
        }
    }

    /**
     * mergeArea over this one. Components in src that are
     * transparent are not written.
     * @param src
     * @param x
     * @param y
     */
    public void mergeArea(Level src, int x, int y)
    {
        int ex = Math.min(width, x + src.width);
        int ey = Math.min(height, y + src.height);
        for (int xi = x; xi < ex; xi++)
        {
            for (int yi = y; yi < ey; yi++)
            {
                byte b = src.map[xi-x][yi-y];
                if (b != Tile.AIR)
                {
                    map[xi][yi] = b;
                    data[xi][yi] = src.data[xi-x][yi-y];
                }

                SpriteTemplate st = src.spriteTemplates[xi-x][yi-y];
                if (st != null && st.getType() != Enemy.ENEMY_NULL)
                {
                    spriteTemplates[xi][yi] = new SpriteTemplate(st);
                }
            }
        }
    }

    public boolean equals(Level other, int x, int y)
    {
        Point[] bounds = clip(other, x, y);
        if (bounds == null) return false;

        Point min = bounds[0];
        Point size = bounds[1];

        System.out.printf("Size of other %d %d %d %d\n", min.x, min.y, size.x, size.y);
        // System.out.printf("Attemping to resize segments w params %d %d %d %d\n", min.x, min.y, size.x, size.y);
        Level queryCopy = new Level(this);
        System.out.println(queryCopy);
        queryCopy = queryCopy.getArea(x, y, size.x, size.y);

        Level testCopy = new Level(this);
        testCopy.mergeArea(other, min.x, min.y);
        testCopy = testCopy.getArea(min.x, min.y, size.x, size.y);

        // System.out.println("TEST COPY " + testCopy.width + " " + testCopy.height);
        System.out.println(testCopy);

        // System.out.println("QUERY COPY" + queryCopy.width + " " + queryCopy.height);
        System.out.println(queryCopy);

        System.out.printf("Checking area from %d %d to %d %d\n", min.x, min.y, min.x+size.x, min.y+size.y);
        for (int xi = 0; xi < size.x; xi++)
        {
            for (int yi = 0; yi < size.y; yi++)
            {
                if (queryCopy.map[xi][yi] != testCopy.map[xi][yi])
                {
                    System.out.printf("not the same %d %d\n", xi, yi);
                    return false;
                }
                SpriteTemplate st1 = queryCopy.spriteTemplates[xi][yi];
                SpriteTemplate st2 = testCopy.spriteTemplates[xi][yi];

                if ((st1 == null && st2 == null) || (st1 != null && st2 != null && st1.getType() == st2.getType()))
                {
                    // pass
                }
                else
                {
                    // return false;
                }
            }
        }

        System.out.println("the same");

        return true;
    }

    public boolean isOutside(Level other, int x, int y)
    {
        int sx = x;
        int sy = y;
        int ex = x + other.width;
        int ey = y + other.height;

        if (sx < width && sx >= 0 && ex < width && ex >= 0 
        && sy < height && sy >= 0 && ey < height && sy >= 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                builder.append(String.format("%+04d ", map[x][y]));
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    public static class Point
    {
        int x, y;

        Point(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }
}