package com.mojang.mario.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import com.mojang.mario.mapedit.LevelView;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.util.RandomFreq;

// TODO: thorough documentation
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
    private RandomFreq randomGen;
    private ArrayList<AnchorPoint> anchorPoints;
    private ArrayList<AnchorPoint> failedToFilter;
    private ArrayList<Chunk> chunkListStart;
    private ArrayList<Chunk> chunkListEnd;
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
        this.chunkListStart = new ArrayList<>();
        this.chunkListEnd = new ArrayList<>();

        for (Level level : ChunkLibrary.getChunks())
        {
            Chunk chunk = Chunk.fromLevel(level);
            if (chunk.anchors.size() >= 2)
            {
                chunkListStart.add(chunk);
            }
            else if (chunk.anchors.size() == 1)
            {
                chunkListEnd.add(chunk);
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

        mainLoop(chunkListStart);
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
        while (failedToFilter.size() < anchorPoints.size())
        {
            AnchorPoint context = contextSelection();
            System.out.println("Starting filtering");
            List<Chunk> compatibleChunks = chunkFiltering(context, chunkList);
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
                for (AnchorPoint ap : selectedChunk.anchors)
                {
                    if (!ap.equals(context))
                    {
                        anchorPoints.add(ap);
                    }
                }
                anchorPoints.remove(context);

                LevelView.show(level);
                JOptionPane.showConfirmDialog(null, "Please");
                failedToFilter.clear();
            }
            shuffleContext();
        }
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

            AnchorPoint a = testChunk.anchors.get(0);

            // If any part of the test chunk is outside the level, do not place it
            if (level.isOutside(tdata, ox-a.x, oy-a.y))
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

                    int idx = ox - a.x + xi;
                    int idy = oy - a.y + yi;
                    if (idx < 0 || idx >= level.width || idy < 0 || idy >= level.height) 
                    {
                        continue;
                    }
                    Component queryComp = Component.fromByte(ox - a.x + xi, oy - a.y + yi, level.map[ox-a.x+xi][oy-a.y+yi]);
                    if (queryComp.type == Component.NULL)
                    {
                        
                    }
                    else 
                    {
                        if (level.map[idx][idy] == tdata.map[xi][yi])
                        {
                        }
                        else
                        {
                            System.out.printf("Overlap found, must reject: solid block at %d %d\n", idx, idy);
                            rejectTestChunk = true;
                            break;
                        }
                    }
                    if (Component.fromSpriteTemplate(ox - a.x + xi, oy - a.y + yi, level.spriteTemplates[ox-a.x+xi][oy-a.y+yi]).type == Component.NULL)
                    {  
                    }
                    else 
                    {
                        SpriteTemplate lst, tst;
                        lst = level.spriteTemplates[idx][idy];
                        tst = tdata.spriteTemplates[idx][idy];
                        if (lst == tst)
                        {
                        } 
                        else if (lst!=null && tst!=null && lst.getType() == tst.getType())
                        {
                        }
                        else {
                            System.out.printf("Overlap found, must reject: enemy at %d %d\n", idx, idy);
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

            if (!rejectTestChunk)
            {
                filteredChunks.add(matchedChunk);
            }
        }
        return filteredChunks;
    }

    private Chunk chunkSelection(List<Chunk> compatibleChunks)
    {
        int i = randomGen.get();

        return compatibleChunks.get(i);
    }

    private AnchorPoint chunkIntegration(AnchorPoint context, Chunk selection)
    {
        AnchorPoint a = selection.anchors.get(0);
        System.out.printf("Leftmost anchor point in selection %d %d\n", a.x, a.y);
        System.out.printf("Plaing segment at %d %d\n", context.x - a.x, context.y - a.y);
        level./*safeS*/setArea(selection.segment, context.x - a.x, context.y - a.y);
        
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

    // TODO: fill in the gaps underneath platforms, involves finding these gaps I suppose
    // public void decorate()

    // TODO: doc
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

    // TODO: doc
    private static class Chunk {
        private static int idCounter = 0;
        public int id;
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

    // TODO: use ex, ey
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
                    comp.ex = comp.sx + 2;
                    comp.ey = comp.sy + 3;
                }
                else if (st.getType() == Enemy.ENEMY_FLOWER)
                {
                    comp.ex = comp.sx + 2;
                    comp.ey = comp.sy + 2;
                }
                else
                {
                    comp.ex = comp.sx + 1;
                    comp.ey = comp.sy + 1;
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

        // TODO: public List<Component> getHazards(Level level)
    }
}
