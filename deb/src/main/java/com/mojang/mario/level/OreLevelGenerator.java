package com.mojang.mario.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import com.mojang.mario.mapedit.LevelView;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.util.RandomFreq;
import com.mojang.mario.util.Logger;

/**
 * OreLevelGenerator roughly uses the ORE level generation algorithm created 
 * by Michael Mateas and Peter Mawhorter for the Level Generation Track of the Mario AI competition.
 * 
 * In essence, it connects pieces of level together by using the anchor point tile as a connector.
 * Depending on the chunk library, some very interesting levels with non-linearity can be created.
 * Unfortunately, the person making the chunk library needs a lot of knowledge about how it works.
 */
public class OreLevelGenerator 
{
    private static long lastSeed;
    private static final double RATIO_LAST_TO_EXPAND = 0.1;

    private boolean shouldBuildStart;
    private boolean shouldBuildEnd;
    @SuppressWarnings("unused")
    private int type;
    @SuppressWarnings("unused")
    private int difficulty;
    private int width;
    private int height;
    private Random random;

    private Level level;
    private int lastContextIdx;
    private RandomFreq randomGen;
    private ArrayList<AnchorPoint> anchorPoints;
    private ArrayList<AnchorPoint> failedToFilter;
    private ArrayList<Chunk> chunkListStart;
    private ArrayList<Chunk> chunkListEnd;

    /**
     * createLevel is the entry point for this generator. Given parameters, produces a level that matches them.
     * @param width width of the main area of the level.
     * @param height height of the main area of the level.
     * @param seed seed to use for random elements.
     * @param difficulty not used by this generator.
     * @param type not used by this generator.
     * @param buildStart Whether or not to build a starting area.
     * @param buildEnd Whether or not to build an area with a level exit.
     * @return Generated level.
     */
    public static Level createLevel(int width, int height, long seed, int difficulty, int type, boolean buildStart, boolean buildEnd)
    {
        OreLevelGenerator levelGenerator = new OreLevelGenerator(width, height, buildStart, buildEnd);
        return levelGenerator.createLevel(seed, difficulty, type);
    }    

    /**
     * createLevel with no starting point, no ending point, and using initial as the first anchor
     * @param width Desired width of the level. 
     * @param height Desired height of the level.
     * @param seed Seed for the random number generators.
     * @param initial Anchor point describing the first anchor point to expand from.
     * @return A level of size, width x height
     */
    public static Level createLevel(int width, int height, long seed, AnchorPoint initial)
    {
        OreLevelGenerator levelGenerator = new OreLevelGenerator(width, height, false, false);
        levelGenerator.addAnchorPoint(initial);
        return levelGenerator.createLevel(seed, 1, LevelGenerator.TYPE_OVERGROUND);
    }

    private OreLevelGenerator(int width, int height, boolean shouldBuildStart, boolean shouldBuildEnd)
    {
        Logger.setLevel(Logger.LEVEL_INFO);

        this.width = width;
        this.height = height;
        this.shouldBuildStart = shouldBuildStart;
        this.shouldBuildEnd = shouldBuildEnd;
        this.anchorPoints = new ArrayList<>();
        this.failedToFilter = new ArrayList<>();
        this.chunkListStart = new ArrayList<>();
        this.chunkListEnd = new ArrayList<>();

        for (Level level : ChunkLibrary.getChunks())
        {
            List<String> tags = ChunkLibrary.getTags(level);
            Chunk chunk = Chunk.fromLevel(level);
            if (tags.contains("place last"))
            {
                chunkListEnd.add(chunk);
            }
            else
            {
                chunkListStart.add(chunk);
            }
        }
    }

    private Level createLevel(long seed, int difficulty, int type)
    {
        this.type = type;
        this.difficulty = difficulty;
    
        lastSeed = seed;
        random = new Random(seed);
        level = new Level(width, height);

        if (shouldBuildStart)
        {
            buildStart();
        }

        Logger.d("ORE", "Starting chunkListStart");
        mainLoop(chunkListStart);
        Logger.d("ORE", "Starting chunkListEnd");
        mainLoop(chunkListEnd);

        if (shouldBuildEnd)
        {
            buildEnd();
        }

        return level;
    }

    private void mainLoop(List<Chunk> chunkList)
    {
        randomGen = new RandomFreq(chunkList.size(), lastSeed);
        failedToFilter = new ArrayList<>();
        lastContextIdx = 0;
        List<AnchorPoint> usedAnchorPoints = new ArrayList<>(anchorPoints.size());
        // While there are still untried anchor points,
        while (failedToFilter.size() < anchorPoints.size())
        {
            // Select an anchor point
            AnchorPoint context = contextSelection();
            Logger.i("ORE", "Starting filtering");
            // Get the list of matching chunks
            List<Chunk> compatibleChunks = chunkFiltering(context, chunkList);
            Logger.i("ORE", "Finished filtering");
            if (compatibleChunks == null || compatibleChunks.size() == 0)
            {
                // No chunks are available, so mark this context as failed, and move on
                Logger.i("ORE", "No compatible chunks for the current context");
                failedToFilter.add(context);
            }
            else
            {
                // Select a chunk from the list of matched chunks
                Chunk selectedChunk = chunkSelection(compatibleChunks);
                // Integrate the chunk into the level relative to the context
                AnchorPoint chunkStart = chunkIntegration(context, selectedChunk);
                // Mark the context as used
                List<AnchorPoint> toRemove = new ArrayList<>();
                for (AnchorPoint ap : selectedChunk.anchors)
                {
                    Logger.i("ORE", String.format("Select context has anchor point (%d,%d) ", ap.x, ap.y));
                    ap.x = context.x + ap.x - chunkStart.x;
                    ap.y = context.y + ap.y - chunkStart.y;

                    // If anchor points are out of bounds, don't add them
                    if (ap.x > level.width || ap.y >= level.height || ap.y < 0)
                    {
                        toRemove.add(ap);
                    }
                    // If anchor points overlap existing anchor points, then don't add them
                    if (anchorPoints.contains(ap)) {
                        toRemove.add(ap);
                    }
                    Logger.i("ORE", String.format("Moving anchor point to %d %d\n", ap.x, ap.y));
                }
                for (AnchorPoint ap : toRemove)
                {
                    Logger.i("ORE", String.format("Clipping anchor at %d %d\n", ap.x, ap.y));
                    selectedChunk.anchors.remove(ap);
                }
                for (AnchorPoint ap : selectedChunk.anchors)
                {
                    if (/*!ap.equals(context) ||*/ !anchorPoints.contains(ap))
                    {
                        anchorPoints.add(ap);
                    }
                }
                usedAnchorPoints.add(context);
                anchorPoints.remove(context);

                // TODO Remove me
                // LevelView.show(level);
                // JOptionPane.showConfirmDialog(null, "Please");
                // End remove me

                failedToFilter.clear();
            }
            shuffleContext();
        }
        
        anchorPoints.addAll(usedAnchorPoints);
        Collections.shuffle(anchorPoints);

        // trim the list of anchor points to a 10th of its size
        ArrayList<AnchorPoint> tmpAnchorPoints = new ArrayList<AnchorPoint>();
        int n = (int)Math.ceil(anchorPoints.size() * RATIO_LAST_TO_EXPAND);
        for (int i = 0; i < n; i++)
        {
            tmpAnchorPoints.add(anchorPoints.get(i));
        }
        anchorPoints = tmpAnchorPoints;
    }

    /**
     * buildStart creates a safe starting area
     * @return
     */
    private int buildStart()
    {
        int floor = 7; //height - 3 - random.nextInt(4);
        for (int x = 0; x < 5; x++)
        {
            for (int y = floor; y < height; y++) 
            {
                if (y > floor)
                {
                    level.setBlock(x, y, Tile.FLOOR_DECORATIVE);
                }
                else
                {
                    level.setBlock(x, y, Tile.FLOOR_MID);
                }
            }
        }
        for (int x = 5; x < 7; x++)
        {
            for (int y = floor + 1; y < height; y++)
            {
                if (y > floor + 1)
                {
                    if (x == 7 - 1) 
                    {
                        level.setBlock(x, y, Tile.FLOOR_RIGHT_BLOCK);
                    }
                    else
                    {
                        level.setBlock(x, y, Tile.FLOOR_DECORATIVE);
                    }
                }
                else
                {
                    level.setBlock(x, y, Tile.FLOOR_MID);
                }
            }
        }
        AnchorPoint startPoint = new AnchorPoint(7, floor, false);
        level.setBlock(startPoint.x, startPoint.y, Tile.ANCHOR_POINT);
        anchorPoints.add(startPoint);

        return 7;
    }

    /**
     * contextSelection From the list of unused anchor points, choose
     * an anchor point to expand.
     * 
     * In this case, loop through anchor points in order. 
     * @return The chosen AnchorPoint
     */
    private AnchorPoint contextSelection()
    {
        AnchorPoint ap = anchorPoints.get(lastContextIdx);
        Logger.i("ORE", String.format("Selected context at %d %d", ap.x, ap.y));
        lastContextIdx++;
        return ap;
    }

    /**
     * chunkFiltering Filter the list of possible chunks to determine possible chunks 
     * that can be placed at context.
     * @param context The context to filter chunkList by
     * @param chunkList A list of candidate chunks to filter
     * @return A list of viable chunks
     */
    private List<Chunk> chunkFiltering(AnchorPoint context, List<Chunk> chunkList)
    {
        List<Chunk> filteredChunks = new ArrayList<>();
        for (Chunk testChunk : chunkList)
        {
            Chunk matchedChunk = testChunk.copy();
            Level tdata = testChunk.segment;
            //1 align the test chunk with the chosen context
            // test chunk from context.x to context.x + testChunk.width
            //                 context.y to context.y + testChunk.height
            int ox = context.x;
            int oy = context.y;
            //2 for each component in the test chunk
            // def of a component: non-null SpriteTemplate, blocking tile
            boolean rejectTestChunk = false;

            // For each anchor point in the test chunk
            for (AnchorPoint a : testChunk.anchors)
            {
                // If any part of the test chunk is outside the level, do not place it
                if (level.isOutside(tdata, ox-a.x, oy-a.y))
                {
                    continue;
                }

                // If placing this chunk has no effect, don't place it
                if (level.equals(tdata, ox-a.x, oy-a.y))
                {
                    continue;
                }
                
                for (int xi = 0; xi < tdata.width && !rejectTestChunk; xi++)
                {
                    for (int yi = 0; yi < tdata.height && !rejectTestChunk; yi++)
                    {
                        // def of a component: non-null SpriteTemplate, blocking tile
                        // no block in the test chunk at this point
                        Component testComp = Component.fromByte(xi, yi, tdata.map[xi][yi]);
                        if (testComp.type == Component.NULL)
                        {
                            testComp = Component.fromSpriteTemplate(xi, yi, tdata.spriteTemplates[xi][yi]);
                        }
                        // no enemies in the test chunk at this point either, so don't test
                        if (testComp.type == Component.NULL)
                        {
                            continue;
                        }

                        // idx, idy is the top left corner of the testChunk when placed in the queryChunk
                        int idx = ox - a.x + xi;
                        int idy = oy - a.y + yi;
                        if (idx < 0 || idx >= level.width || idy < 0 || idy >= level.height) 
                        {
                            continue;
                        }
                        // Check enemy in test chunk's area for overlap with tiles or enemies in query chunk
                        if (testComp.type == Component.ENEMY || testComp.type == Component.TILE)
                        {
                            // Size of the test component
                            int tstCompW = testComp.ex - testComp.sx;
                            int tstCompH = testComp.ey - testComp.sy;
                            Logger.d("chunkFilter", String.format("Enemy at %d %d, checking to %d %d", idx, idy, idx+tstCompW, idy+tstCompH));
                            
                            // For each tile in the test chunk, check a 5x5 area for sprite templates, and a 1x1 area for tiles
                            int startY = Math.max(0, idy-1);
                            for (int i = idx; i < idx + tstCompW && !rejectTestChunk; i++)
                            {
                                for (int j = startY; j < idy + tstCompH; j++)
                                {
                                    // Check 5x5 area for sprite templates
                                    Logger.d("chunkFilter", String.format("Testing for enemies from %d %d to %d %d", i - 2, j - 3, i + 2, j + 1));
                                    for (Component comp : Component.getSpriteTemplates(level, i - 2, j - 3, i + 2, j + 1))
                                    {
                                        Logger.d("chunkFilter", String.format("Checking component at %d %d to %d %d", comp.sx, comp.sy, comp.ex, comp.ey));
                                        // If an overlap is found, reject
                                        if (Component.overlaps(testComp, comp))
                                        {
                                            Logger.d("chunkFilter", String.format("Enemy at %d %d to %d %d in query chunk, REJECT", comp.sx, comp.sy, comp.ex, comp.ey));
                                            rejectTestChunk = true;
                                            break;
                                        }
                                    }

                                    Component tstBlock = Component.fromByte(i, j, level.map[i][j]);
                                    Component tstEnemy = Component.fromSpriteTemplate(i, j, level.spriteTemplates[i][j]);
                                    // Reject if an enemy is at position i,j in queryChunk
                                    if (/*tstBlock.type != Component.NULL || */tstEnemy.type != Component.NULL)
                                    {
                                        Logger.d("chunkFilter", String.format("Enemy at %d %d in query chunk, REJECT", i, j));
                                        rejectTestChunk = true;
                                        break;
                                    }
                                    else
                                    {
                                        Logger.d("chunkFilter", String.format("All clear at %d %d tstBlock %d tstEnemy %d", i, j, tstBlock.type, tstEnemy.type));
                                    }
                                }
                            }
                        }
                        // Check for overlap between blocks
                        // Component queryComp = Component.fromByte(ox - a.x + xi, oy - a.y + yi, level.map[ox-a.x+xi][oy-a.y+yi]);
                        Component queryComp = Component.fromByte(idx, idy, level.map[idx][idy]);
                        if (queryComp.type == Component.NULL)
                        {
                            
                        }
                        else 
                        {
                            if (level.map[idx][idy] == tdata.map[xi][yi])
                            {
                            }
                            // Reject due to non-matching blocks
                            else
                            {
                                Logger.i("ORE", String.format("Overlap found, must reject: solid block at %d %d", idx, idy));
                                rejectTestChunk = true;
                                break;
                            }
                        }
                        // Check for overlap between enemies
                        // If there is an enemy sprite in query chunk
    //                    if (Component.fromSpriteTemplate(ox - a.x + xi, oy - a.y + yi, level.spriteTemplates[ox-a.x+xi][oy-a.y+yi]).type == Component.NULL)
                        if (Component.fromSpriteTemplate(idx, idy, level.spriteTemplates[idx][idy]).type == Component.NULL)
                        {  
                        }
                        else 
                        {
                            SpriteTemplate lst, tst;
                            lst = level.spriteTemplates[idx][idy];
                            tst = tdata.spriteTemplates[xi][yi];
                            if (lst == tst)
                            {
                            } 
                            // If the queryChunk sprite is the same as the testChunk sprite
                            else if (lst!=null && tst!=null && lst.getType() == tst.getType())
                            {
                            }
                            else 
                            {
                                Logger.i("ORE", String.format("Overlap found, must reject: enemy at %d %d", idx, idy));
                                rejectTestChunk = true;
                                break;
                            }
                        }
                    }
                    if (rejectTestChunk)
                    {
                        break;
                    }
                }

                // If chunk is not rejected, add to test chunk to matched chunks
                if (!rejectTestChunk)
                {
                    Chunk matched = matchedChunk.copy();
                    matched.matchedAnchor = a;
                    filteredChunks.add(matched);
                }
            }
        }
        return filteredChunks;
    }

    /**
     * chunkSelection From the list of compatible chunks, choose a chunk to intergrate.
     * 
     * In this case, the chunk selection tries to balance the chosen chunk so that all
     * chunks are chosen a similar amount of times.
     * @param compatibleChunks List of viable chunks
     * @return Chosen chunk
     */
    private Chunk chunkSelection(List<Chunk> compatibleChunks)
    {
        int i = randomGen.get();
        while (i > compatibleChunks.size() - 1) 
        {
            i = randomGen.get();
        }
        randomGen.updateValue(i);
        return compatibleChunks.get(i);
    }

    /**
     * chunkIntegration Integrate selection into the level at context.
     * @param context Point in the level to integrate selection.
     * @param selection Chunk to integrate into the level.
     * @return Anchor point in selection that was connected to context.
     */
    private AnchorPoint chunkIntegration(AnchorPoint context, Chunk selection)
    {
        AnchorPoint a = selection.matchedAnchor;
        Logger.i("ORE", String.format("Leftmost anchor point in integrated chunk %d %d", a.x, a.y));
        Logger.i("ORE", String.format("Placing integrated chunk at %d %d", context.x - a.x, context.y - a.y));
        level./*safeS*/mergeArea(selection.segment, context.x - a.x, context.y - a.y);
        
        return new AnchorPoint(a);
    }

    /**
     * shuffleContext shuffle the list of anchor points if they have all been used.
     */
    private void shuffleContext()
    {
        Logger.d("ORE", String.format("shuffleContext lastContextIdx %d anchorPoints.size %d", lastContextIdx, anchorPoints.size()));
        if (lastContextIdx >= anchorPoints.size())
        {
            Collections.shuffle(anchorPoints, random);
            lastContextIdx = 0;
        }
    }

    /**
     * buildEnd for level.
     * 
     * Note that the level end goes past the intended width, which usually resizes the level by 15 units
     * @return
     */
    private int buildEnd()
    {
        level.resize(0, 0, level.width + 15, level.height);
        int floor = height - 1 - random.nextInt(4);
        for (int x = level.width - 15; x < level.width; x++)
        {
            for (int y = floor; y < height; y++) 
            {
                if (y > floor)
                {
                    level.setBlock(x, y, Tile.FLOOR_DECORATIVE);
                }
                else
                {
                    level.setBlock(x, y, Tile.FLOOR_MID);
                }
            }
        }
        //level.setBlock(level.width-10, floor-1, Tile.ANCHOR_POINT);
        level.setBlock(level.width-10, floor-1, Tile.LEVEL_EXIT);

        return 5;
    }

    private void addAnchorPoint(AnchorPoint anchor)
    {
        this.anchorPoints.add(anchor);
    }

    // TODO: fill in the gaps underneath platforms, involves finding these gaps I suppose
    // public void decorate()

    /**
     * AnchorPoint stores information about a point within a level, and whether or
     * not the point has been visited.
     */
    public static class AnchorPoint extends Object{
        public int x;
        public int y;
        public boolean visited;

        /**
         * Constructor.
         * @param x xTile position
         * @param y yTile position
         * @param visited Whether this AP has been visited.
         */
        public AnchorPoint(int x, int y, boolean visited)
        {
            this.x = x;
            this.y = y;
            this.visited = visited;
        }

        /**
         * Copy constructor. Does not copy visited field.
         * @param other AnchorPoint to copy from.
         */
        public AnchorPoint(AnchorPoint other)
        {
            this.x = other.x;
            this.y = other.y;
            this.visited = false;
        }

        /**
         * equals tests whether the coordinates of this and other are the same.
         * @param other AnchorPoint to compare location to.
         * @return True if x and y are the same.
         */
        public boolean equals(AnchorPoint other)
        {
            return this.x == other.x && this.y == other.y;
        }

        @Override
        public boolean equals(Object obj) {
            // TODO Auto-generated method stub
            return equals((AnchorPoint)obj);
        }
    }

    /**
     * Chunk represents a level segment and its anchors.
     */
    private static class Chunk {
        private static int idCounter = 0;
        public AnchorPoint matchedAnchor;
        public int id;
        public Level segment;
        public ArrayList<AnchorPoint> anchors;

        /**
         * fromLevel creates a chunk from a level, and 
         * automatically adds all anchor tiles as anchor points.
         * @param level Level to create a chunk from.
         * @return Newly created chunk.
         */
        public static Chunk fromLevel(Level level)
        {
            if (level == null)
            {
                throw new IllegalArgumentException("Cannot create a Chunk from a null level");
            }
            Chunk chunk = new Chunk();
            chunk.segment = new Level(level);
            chunk.anchors = new ArrayList<>();
            chunk.id = idCounter;
            idCounter++;
            for (int x = 0; x < level.width; x++)
            {
                for (int y = 0; y < level.height; y++)
                {
                    if (level.map[x][y] == Tile.ANCHOR_POINT)
                    {
                        chunk.anchors.add(new AnchorPoint(x, y, false));
                    }
                }
            }
            chunk.anchors.trimToSize();

            return chunk;
        }

        /**
         * copy creates a copy of this Chunk.
         * @return A deep copy of this Chunk.
         */
        public Chunk copy()
        {
            Chunk copy = new Chunk();
            copy.segment = new Level(segment);
            copy.anchors = new ArrayList<>();
            copy.id = id;
            for (AnchorPoint ap : anchors)
            {
                copy.anchors.add(new AnchorPoint(ap));
            }
            return copy;
        }
    }

    /**
     * Component represents the area occupied by some important element
     * in a level, be it a solid block, enemy or platform.
     */
    private static class Component {
        public static final int ENEMY = 1;
        public static final int TILE = 2;
        public static final int HAZARD = 3;
        public static final int NULL = 4;
        public int type;
        public int sx, sy;
        public int ex, ey;

        /**
         * fromByte create a component from a byte, and a position.
         * @param x xTile that byte was taken from.
         * @param y yTile that byte was taken from
         * @param b byte describing the tile per Tile class
         * @return type==NULL component if not solid or anchor point, otherwise type==TILE
         */
        public static Component fromByte(int x, int y, byte b)
        {
            Component comp = new Component();
            if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_UPPER) > 0 ||
                    ((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_ALL) > 0 ||
                    ((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_LOWER) > 0 ||
                    b == Tile.ANCHOR_POINT)
            {
                comp.type = TILE;
            }
            else
            {
                comp.type = NULL;
            }
            comp.sx = x;
            comp.sy = y;
            comp.ex = x + 1;
            comp.ey = y + 1;
            return comp;
        }

        /**
         * fromSpriteTemplate creates a component for st at x, y
         * @param x xTile position of st
         * @param y yTile position of st
         * @param st SpriteTemplate object at st
         * @return Return a Component of type Component.ENEMY describing the area
         *         occupied by st.
         */
        public static Component fromSpriteTemplate(int x, int y, SpriteTemplate st)
        {
            Component comp = new Component();
            if (st != null && st.getType() != Enemy.ENEMY_NULL) 
            {
                comp.type = ENEMY;
                comp.sx = x;
                comp.sy = y - 1;
                switch (st.getType())
                {
                    case Enemy.ENEMY_THWOMP:
                        comp.ex = comp.sx + 2;
                        comp.ey = comp.sy + 3;
                        break;
                    case Enemy.ENEMY_GREEN_KOOPA:
                    case Enemy.ENEMY_RED_KOOPA:
                        comp.ex = comp.sx + 1;
                        comp.ey = comp.sy + 2;
                        break;
                    case Enemy.ENEMY_FLOWER:
                        comp.ex = comp.sx + 2;
                        comp.ey = comp.sy + 2;
                        break;
                    case Enemy.ENEMY_SPIKY:
                    case Enemy.ENEMY_GOOMBA:
                        comp.ex = comp.sx + 1;
                        comp.ey = comp.sy + 1;
                        break;
                    default:
                        comp.ex = comp.sx + 2;
                        comp.ey = comp.sy + 2;
                }
            }
            else 
            {
                comp.type = NULL;
                comp.ex = comp.sx + 1;
                comp.ey = comp.sy + 1;
            }
            return comp;
        }

        /**
         * getSpriteTemplates returns ENEMY components in level in the area from sx, sy to ex, ey
         * by checking the spriteTemplates array in level.
         * @param level Level object to check for SpriteTemplates in
         * @param sx xTile of upper left corner to check 
         * @param sy yTile of upper left corner to check
         * @param ex xTile of bottom right corner to check
         * @param ey yTile of bottom right corner to check
         * @return List of all spriteTemplates starting in the area
         */
        public static List<Component> getSpriteTemplates(Level level, int sx, int sy, int ex, int ey)
        {
            int startX = Math.max(0, sx);
            int endX = Math.min(ex, level.width);
            int startY = Math.max(0, sy);
            int endY = Math.min(ey, level.height);

            List<Component> foundComponents = new ArrayList<>();
            for (int x = startX; x < endX; x++) 
            {
                for (int y = startY; y < endY; y++) 
                {
                    SpriteTemplate st = level.getSpriteTemplate(x, y);
                    if (st != null)
                    {
                        Component tmpComp = Component.fromSpriteTemplate(x, y, st);
                        Component tmpBlock = Component.fromByte(x, y, level.map[x][y]);
                        if (tmpComp.type == ENEMY)
                            foundComponents.add(tmpComp);
                        if (tmpBlock.type == TILE)
                            foundComponents.add(tmpBlock);
                    }
                }
            }
            return foundComponents;
        }

        /**
         * overlaps checks if comp is inside window's area.
         * @param window Window component to check comp againt
         * @param comp Any component, to check if inside window
         * @return True if comp overlaps window at all.
         */
        public static boolean overlaps(Component window, Component comp)
        {
            if (comp.sx < window.ex && comp.sx >= window.sx || comp.ex < window.ex && comp.ex >= window.sx) 
            {
                if (comp.sy < window.ey && comp.sy >= window.sy || comp.ey < window.ey && comp.ey >= window.sy)
                {
                    return true;
                }
            }
            return false;
        }

        // TODO: public List<Component> getHazards(Level level)
    }
}
