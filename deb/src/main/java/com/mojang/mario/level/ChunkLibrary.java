package com.mojang.mario.level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ChunkLibrary manages chunks internally and provides
 * an application-wide point of access for them.
 */
public class ChunkLibrary {
    
    private List<LoadingFinishedListener> lfListeners;
    private List<Level> chunks;
    private File programDirectory;

    private static final String CHUNK_PARENT_DIR_NAME = "chunks";
    private static ChunkLibrary ref;


    /**
     * init is called to set up the ChunkLibrary. Can be called several
     * times without consequence.
     * @param programDirectory Main program directory. Should exist.
     */
    public static void init(File programDirectory)
    {
        if (ref == null)
        {
            ref = new ChunkLibrary();
            ref.programDirectory = programDirectory;
            ref.chunks = new ArrayList<>();
            ref.lfListeners = new ArrayList<>();
        }
    }

    /**
     * addChunk to the library.
     * @param chunk Any Level that is not null.
     * @throws NullPointerException if null,
     * @throws IllegalStateException if ChunkLibrary has not been loaded.
     */
    public static void addChunk(Level chunk)
    {
        if (ref != null)
        {
            ref.chunks.add(chunk);
        }
        else
        {
            throw new IllegalStateException("ChunkLibrary has not been initialized");
        }
    }

    /**
     * removeChunk from the library.
     * @param chunk Chunk to remove.
     * @throws IllegalStateException if ChunkLibrary has not been intialized.
     */
    public static void removeChunk(Level chunk)
    {
        if (ref != null)
        {
            int i = ref.chunks.indexOf(chunk);
            ref.chunks.remove(chunk);

            File chunksDirectory = new File(ref.programDirectory.getPath() + File.separatorChar + CHUNK_PARENT_DIR_NAME);
            File levelDirectory = new File(String.format("%s%s%03d", chunksDirectory, File.separator, i));
            File[] contents = levelDirectory.listFiles();
            for (File file : contents)
            {
                file.delete();
            }
            levelDirectory.delete();
        }
        else
        {
            throw new IllegalStateException("ChunkLibrary has not been initialized");
        }
    }

    /**
     * getChunks return a list of chunks that have been loaded.
     * @return Non null list of loaded chunks.
     * @throws IllegalStateException if ChunkLibrary has not been loaded.
     */
    public static List<Level> getChunks()
    {
        if (ref != null)
            return ref.chunks;
        else
            throw new IllegalStateException("ChunkLibrary has not been initialized");
    }

    /**
     * saveChunks in the panel to the chunks directory in program directory.
     * @throws IllegalStateException if ChunkLibrary.init() has not been called.
     */
    public static void saveChunks()
    {
        if (ref == null) 
        {
            throw new IllegalStateException("ChunkLibrary has not been intialized");
        }
        File chunksDirectory = new File(ref.programDirectory.getPath() + File.separatorChar + CHUNK_PARENT_DIR_NAME);
        chunksDirectory.mkdirs();

        int n = ref.chunks.size();
        for (int i = 0; i < n; i++)
        {
            Level chunk = ref.chunks.get(i);
            chunk.xExit = chunk.yExit = -1;
            String name = String.format("%03d", i);
            File chunkDir = new File(chunksDirectory.getPath() + File.separatorChar + name);
            chunkDir.mkdirs();
            try {
                chunk.save(chunkDir);
            } catch (IOException ie) {
                System.err.println(ie);
            }
        }
    }

    /**
     * loadChunks in the panel to the chunks directory in program directory.
     * @throws IllegalStateException if ChunkLibrary.init() has not been called.
     */
    public static void loadChunks()
    {
        if (ref == null) 
        {
            throw new IllegalStateException("ChunkLibrary has not been intialized");
        }
        if (Runtime.getRuntime().availableProcessors() > 1) 
        {
            loadChunksThreaded();
        } 
        else 
        {
            long start = System.currentTimeMillis();

            File chunksDirectory = new File(ref.programDirectory.getPath() + File.separatorChar + CHUNK_PARENT_DIR_NAME);
            File[] chunkDirs = chunksDirectory.listFiles();
            if (chunkDirs != null)
            {
                for (File chunkDir : chunkDirs)
                {
                    Level chunk = null; 
                    try {
                        chunk = Level.load(chunkDir);
                    } catch (IOException ie) {
                        System.err.println(ie);
                    }
                    if (chunk != null)
                    {
                        addChunk(chunk);
                    }
                }
            }

            long end = System.currentTimeMillis();
            System.out.println("Loaded levels in " + (end - start) + " ms");
        }
        // int i =11;
        for (LoadingFinishedListener lf : ref.lfListeners)
        {
            // System.out.println(i++ +"");
            lf.onLoadingFinished();
        }
    }

    private static void loadChunksThreaded()
    {
        long start = System.currentTimeMillis();
        File chunksDirectory = new File(ref.programDirectory.getPath() + File.separatorChar + CHUNK_PARENT_DIR_NAME);
        File[] chunkDirs = chunksDirectory.listFiles();
        if (chunkDirs != null)
        {
            List<Loader> loaders = new ArrayList<>(chunkDirs.length);
            List<Thread> threads = new ArrayList<>(chunkDirs.length);
            Loader tmpLoader;
            Thread tmpThread;
            for (File chunkDir : chunkDirs)
            {
                tmpLoader = new Loader(chunkDir);
                tmpThread = new Thread(tmpLoader);
                loaders.add(tmpLoader);
                threads.add(tmpThread);
                tmpThread.start();
            }
            int n = loaders.size();
            for (int i = 0; i < n; i++)
            {
                tmpThread = threads.get(i);
                try {
                    tmpThread.join();
                    tmpLoader = loaders.get(i);
                    Level result = tmpLoader.getResult();
                    if (result != null)
                    {
                        addChunk(result);
                    }
                } catch (InterruptedException ie) {
                    System.err.println("Thread " + i + " interrupted while joining");
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Loaded levels in " + (end - start) + " ms");
    }

    /**
     * addLoadingFinishedListener add a listener to be notified when chunk loading is done.
     * @param listener
     * @throws IllegalStateException if ChunkLibrary is null
     */
    public static void addLoadingFinishedListener(LoadingFinishedListener listener)
    {
        if (ref == null)
        {
            throw new IllegalStateException("ChunkLibrary is not initialized");
        }
        ref.lfListeners.add(listener);
    }

    private static class Loader implements Runnable {
        private File chunkFolderName;
        private Level result;

        public Loader(File chunkFolderName)
        {
            this.chunkFolderName = chunkFolderName;
        }

        @Override
        public void run() 
        {
            try {
                result = Level.load(chunkFolderName);
            } catch (IOException ie) {
                System.err.println(ie);
            }
        }

        public Level getResult()
        {
            return result;
        }
    }

    /**
     * LoadingFinishedListener can be notified when loadChunks() is done
     */
    public interface LoadingFinishedListener {
        void onLoadingFinished();
    }
}
