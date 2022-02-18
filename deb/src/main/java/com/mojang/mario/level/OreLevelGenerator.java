package com.mojang.mario.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import com.mojang.mario.mapedit.LevelView;
import com.mojang.mario.sprites.Enemy;

public class OreLevelGenerator 
{
    private static long lastSeed;

    private boolean shouldBuildStart;
    private boolean shouldBuildEnd;
    private int type;
    private int difficulty;
    private int width;
    private int height;
    private Random random;

    private Level level;
    private int lastContextIdx;
    private ArrayList<AnchorPoint> anchorPoints;
    private ArrayList<AnchorPoint> failedToFilter;
    private ArrayList<Chunk> chunkList;
    private int numIntegrated = 0;
    //private Component[][] queryChunk;

    public static Level createLevel(int width, int height, long seed, int difficulty, int type, boolean buildStart, boolean buildEnd)
    {
        OreLevelGenerator levelGenerator = new OreLevelGenerator(width, height, buildStart, buildEnd);
        return levelGenerator.createLevel(seed, difficulty, type);
    }    

    private OreLevelGenerator(int width, int height, boolean shouldBuildStart, boolean shouldBuildEnd)
    {
        this.width = width;
        this.height = height;
        this.shouldBuildStart = shouldBuildStart;
        this.shouldBuildEnd = shouldBuildEnd;
        this.anchorPoints = new ArrayList<>();
        this.failedToFilter = new ArrayList<>();
        this.chunkList = new ArrayList<>();

        for (Level level : ChunkLibrary.getChunks())
        {
            chunkList.add(Chunk.fromLevel(level));
        }
    }

    private Level createLevel(long seed, int difficulty, int type)
    {
        this.type = type;
        this.difficulty = difficulty;
    
        lastSeed = seed;
        random = new Random(seed);
        level = new Level(width, height);
        //this.queryChunk = new Component[width][height];

        Collections.shuffle(anchorPoints, random);

        if (shouldBuildStart)
        {
            buildStart();
        }
        while (failedToFilter.size() < anchorPoints.size())
        {
            AnchorPoint context = contextSelection();
            System.out.println("Starting filtering");
            List<Chunk> compatibleChunks = chunkFiltering(context);
            System.out.println("Finished filtering");
            if (compatibleChunks == null || compatibleChunks.size() == 0)
            {
                System.out.println("No compatible chunks for the current context");
                failedToFilter.add(context);
            }
            else
            {
                Chunk selectedChunk = chunkSelection(compatibleChunks);
                AnchorPoint chunkStart = chunkIntegration(context, selectedChunk);
                List<AnchorPoint> toRemove = new ArrayList<>();
                for (AnchorPoint ap : selectedChunk.anchors)
                {
                    System.out.printf("cx %d apx %d csx %d\n", context.x, ap.x, chunkStart.x);
                    System.out.printf("Anchor point from %d %d ", ap.x, ap.y);
                    ap.x = context.x + ap.x - chunkStart.x;
                    ap.y = context.y + ap.y - chunkStart.y;

                    if (ap.x > level.width || ap.y >= level.height || ap.y < 0)
                    {
                        toRemove.add(ap);
                    }
                    System.out.printf("to %d %d\n", ap.x, ap.y);
                }
                for (AnchorPoint ap : toRemove)
                {
                    System.out.printf("Removing ap at %d %d\n", ap.x, ap.y);
                    selectedChunk.anchors.remove(ap);
                }
                //anchorPoints.addAll(selectedChunk.anchors);
                for (AnchorPoint ap : selectedChunk.anchors)
                {
                    if (!ap.equals(context))
                    {
                        anchorPoints.add(ap);
                    }
                }
                //anchorPoints.remove(context);

                LevelView.show(level);
                JOptionPane.showConfirmDialog(null, "Please");
                failedToFilter.clear();
            }
            shuffleContext();
        }
        if (shouldBuildEnd)
        {
            buildEnd();
        }

        return level;
    }

    private int buildStart()
    {
        int floor = height - 3 - random.nextInt(4);
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
                    level.setBlock(x, y, Tile.FLOOR_DECORATIVE);
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

    private AnchorPoint contextSelection()
    {
        AnchorPoint ap = anchorPoints.get(lastContextIdx);
        System.out.printf("Using anchor point %d %d\n", ap.x, ap.y);
        lastContextIdx++;
        return ap;
    }

    private List<Chunk> chunkFiltering(AnchorPoint context)
    {
        List<Chunk> filteredChunks = new ArrayList<>();
        for (Chunk testChunk : chunkList)
        {
            Chunk matchedChunk = testChunk.copy();
            Level data = matchedChunk.segment;
            Level tdata = testChunk.segment;
            //1 align the test chunk with the chosen context
            // test chunk from context.x to context.x + testChunk.width
            //                 context.y to context.y + testChunk.height
            int ox = context.x;
            int oy = context.y;
            //2 for each component in the test chunk
            // def of a component: non-null SpriteTemplate, blocking tile
            boolean rejectTestChunk = false;

            AnchorPoint a = testChunk.anchors.get(0);

            // if placing the test chunk does not change query chunk, reject test chunk
            System.out.printf("Checking for problems in query chunk from %d %d to %d %d\n", ox - a.x, oy - a.y, ox-a.x + tdata.width, oy-a.y+tdata.height);
            if (level.equals(tdata, ox-a.x, oy-a.y))
            {
                System.out.println("Rejecting due to no change");
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

                    int idx = ox - a.x + xi;
                    int idy = oy - a.y + yi;
                    if (idx < 0 || idx >= level.width || idy < 0 || idy >= level.height) continue;
                    Component queryComp = Component.fromByte(ox - a.x + xi, oy - a.y + yi, level.map[ox-a.x+xi][oy-a.y+yi]);
                    if (queryComp.type == Component.NULL)
                    {
                        
                    }
                    else 
                    {
                        if (level.map[idx][idy] == tdata.map[xi][yi]) continue;
                        System.out.printf("Overlap found, must reject: solid block at %d %d\n", idx, idy);
                        rejectTestChunk = true;
                        break;
                    }
                    if (Component.fromSpriteTemplate(ox - a.x + xi, oy - a.y + yi, level.spriteTemplates[ox-a.x+xi][oy-a.y+yi]).type == Component.NULL)
                    {  

                    }
                    else 
                    {
                        // if (level.spriteTemplates[idx][idy] == tdata.spriteTemplates[xi][yi])
                        System.out.printf("Overlap found, must reject: enemy at %d %d\n", idx, idy);
                        rejectTestChunk = true;
                        break;
                    }
                    
                    /// a if it duplicates an existing component,
                    /// eliminate it, continue to the next component
                    //# not sure how to do this yet

                    /// b If on top of an existing component, reject
                    /// this test chunk
                    

                    // boolean exitFlag = false; 
                    // for (int xii = tileComp.sx; xii < tileComp.ex && !exitFlag; xii++)
                    // {
                    //     for (int yii = tileComp.sy; yii < tileComp.ey; yii++)
                    //     {
                    //         if (queryChunk[xii][yii] != null) {
                    //             exitFlag = true;
                    //             rejectTestChunk = true;
                    //         }
                    //     }
                    // }
                    // if (exitFlag)
                    // {
                    //     break;
                    // }
                    /// c If not a platform, for 8 adjacent neighbours
                    /// in query chunk
                    // int sx = tileComp.sx + ox - 1;
                    // if (sx < 0) sx = 0;
                    // int ex = sx + 2;
                    // if (ex >= queryChunk.length) ex = queryChunk.length;

                    // int sy = Math.max(0, tileComp.sx + oy - 1);
                    // int ey = Math.min(queryChunk[0].length, sy + 2); 
                    // for (int xii = tileComp.sx + ox - 1; xii < tileComp.sx + ox +1;)
                    // {
                    //     //// ca If neighbour is duplicated by component in test chunk, continue
                    //     //// cb If neighbour matches test component, eliminate test component, test rest of chunk
                        
                    //     //// cc Else reject test chunk
                    // }
                }
                if (rejectTestChunk)
                {
                    break;
                }

                /// c If not a platform, for 8 adjacent neighbours
                /// in query chunk
                // for (int xii = tileComp.sx + ox - 1; ;)
                //// ca If neighbour is duplicated by component in test chunk, continue
                //// cb If neighbour matches test component, eliminate test component, test rest of chunk
                //// cc Else reject test chunk
            }
            /// c If not a platform, for 8 adjacent neighbours
            /// in query chunk
            //// ca If neighbour is duplicated by component in test chunk, continue
            //// cb If neighbour matches test component, eliminate test component, test rest of chunk
            //// cc Else reject test chunk

            // d Else if is a platform, for all 25 neighbours wi 2 tiles in query chunk
            /// i If neighbour overlaps matching component in test chunk, continue
            /// ii If neighbour isn't a platform and blocks test cmpnt from extending,
            /// reject the test chunk
            
            /// iii If neighbour is a platform, reject test chunk unless compatible
            // e Eliminate components in test chunk beyod level edges
            
            // f If not reject, return all matching components as matching chunk

            if (!rejectTestChunk)
            filteredChunks.add(matchedChunk);
        }
        return filteredChunks;
    }

    private Chunk chunkSelection(List<Chunk> compatibleChunks)
    {
        int n = compatibleChunks.size();
        int i = random.nextInt(n);

        return compatibleChunks.get(i);
    }

    private AnchorPoint chunkIntegration(AnchorPoint context, Chunk selection)
    {
        //System.out.println("Number of chunks integrated " + numIntegrated++);
        AnchorPoint a = selection.anchors.get(0);
        System.out.printf("Leftmost anchor point in selection %d %d\n", a.x, a.y);
        System.out.printf("Plaing segment at %d %d\n", context.x - a.x, context.y - a.y);
        level.safeSetArea(selection.segment, context.x - a.x, context.y - a.y);
        //level.setArea(selection.segment, Math.max(context.x - a.x, 0), Math.max(context.y - a.y, 0));
        
        return new AnchorPoint(a);
    }

    private void shuffleContext()
    {
        if (lastContextIdx == anchorPoints.size())
        {
            Collections.shuffle(anchorPoints, random);
            lastContextIdx = 0;
        }
    }

    private int buildEnd()
    {
        int floor = height - 1 - random.nextInt(4);
        for (int x = level.width - 5; x < level.width; x++)
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
        level.setBlock(level.width-5, floor-1, Tile.ANCHOR_POINT);
        level.setBlock(level.width-4, floor-1, Tile.LEVEL_EXIT);

        return 5;
    }

    public static class AnchorPoint {
        public int x;
        public int y;
        public boolean visited;

        public AnchorPoint(int x, int y, boolean visited)
        {
            this.x = x;
            this.y = y;
            this.visited = visited;
        }

        public AnchorPoint(AnchorPoint other)
        {
            this.x = other.x;
            this.y = other.y;
            this.visited = false;
        }

        public boolean equals(AnchorPoint other)
        {
            return this.x == other.x && this.y == other.y;
        }
    }

    private static class Chunk {
        public Level segment;
        public ArrayList<AnchorPoint> anchors;

        public static Chunk fromLevel(Level level)
        {
            if (level == null)
            {
                throw new IllegalArgumentException("Cannot create a Chunk from a null level");
            }
            Chunk chunk = new Chunk();
            chunk.segment = new Level(level);
            chunk.anchors = new ArrayList<>();

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

        public Chunk copy()
        {
            Chunk copy = new Chunk();
            copy.segment = new Level(segment);
            copy.anchors = new ArrayList<>();
            for (AnchorPoint ap : anchors)
            {
                copy.anchors.add(new AnchorPoint(ap));
            }
            return copy;
        }
    }

    private static class Component {
        public static final int ENEMY = 1;
        public static final int TILE = 2;
        public static final int HAZARD = 3;
        public static final int NULL = 4;
        public int type;
        public int sx, sy;
        public int ex, ey;

        public static Component fromByte(int x, int y, byte b)
        {
            Component comp;
            if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_UPPER) > 0 ||
                    ((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_ALL) > 0 ||
                    ((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_LOWER) > 0 ||
                    b == Tile.ANCHOR_POINT)
            {
                comp = new Component();
                comp.type = TILE;
                comp.sx = x;
                comp.sy = y;
            }
            else
            {
                comp = new Component();
                comp.type = NULL;
            }
            return comp;
        }

        public static Component fromSpriteTemplate(int x, int y, SpriteTemplate st)
        {
            Component comp = new Component();
            if (st != null && st.getType() != Enemy.ENEMY_NULL) 
            {
                comp.type = ENEMY;
                comp.sx = x;
                comp.sy = y - 1;
                if (st.getType() == Enemy.ENEMY_THWOMP)
                {
                    comp.ex = x + 1;
                    comp.ey = y + 2;
                }
                else
                {
                    comp.ex = x;
                    comp.ey = y;
                }
            }
            else 
            {
                comp.type = NULL;
            }
            return comp;
        }
    }
}
