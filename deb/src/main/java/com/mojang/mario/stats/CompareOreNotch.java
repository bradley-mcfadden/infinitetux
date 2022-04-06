package com.mojang.mario.stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import com.mojang.mario.level.ChunkLibrary;
import com.mojang.mario.level.Level;
import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.level.OreLevelGenerator;
import com.mojang.mario.mapedit.LevelView;
import com.mojang.mario.util.KLDivergence;
import com.mojang.mario.util.Logger;

public class CompareOreNotch {
    private static final int N = 2500;
    private static final int WIDTH = 256;
    private static final int HEIGHT = 15;
    private static final int DIFFICULTY = 3;
    private static final int KWIDTH = 6;
    private static final int KHEIGHT = 6;
    private static final String ORE_FILE = "ore6.csv";
    private static final String NOTCH_FILE = "notch6.csv";

    private static File programDirectory;

    private static void setupDirectory()
    {
        String userDir = System.getProperty("user.dir");
        programDirectory = new File(userDir + "/.infinitetux");
        programDirectory.mkdirs();
    }

    public static void main(String[] args) {
        
        System.out.println("ORE levels");
        PrintWriter oreFile = null;
        try {
            oreFile = new PrintWriter(ORE_FILE);
        } catch (IOException ie) {
            ie.printStackTrace();
            System.exit(1);
        }

        Logger.setLevel(Logger.LEVEL_ERROR);
        setupDirectory();
        ChunkLibrary.init(programDirectory);
        ChunkLibrary.loadChunksNoThreading();
        System.out.println("Chunks loaded");

        int numThreads = 12; //N/OREWorker.N_LOOPS;
        OREWorker[] workers = new OREWorker[numThreads];
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++)
        {
            workers[i] = new OREWorker();
            threads[i] = new Thread(workers[i]);
            threads[i].start();
        }
        System.out.println("Starting ORE testing");
        
        for (int i = 0; i < threads.length; i++)
        {
            try {
                threads[i].join();
                for (double d : workers[i].getStat())
                {
                    oreFile.println(d);
                }
                System.out.printf("ORE %d/%d\n", (i+1)*N, N*numThreads);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        oreFile.close();
        /*
        for (int i = 0; i < N; i++)
        {
            // System.out.printf("ORE %d/%d\n", i+1, N);
            Level level1 = OreLevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis(), DIFFICULTY, LevelGenerator.TYPE_OVERGROUND, true, true);
            Level level2 = OreLevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis()+10, DIFFICULTY, LevelGenerator.TYPE_OVERGROUND, true, true);
            double stat = KLDivergence.klDivergence(level1, level2, KWIDTH, KHEIGHT);
            // LevelView.show(level1);
            // LevelView.show(level2);
            // JOptionPane.showInputDialog(null, "");
            oreFile.println(stat);
        }
        oreFile.close();
        */

        PrintWriter notchFile = null;
        try {
            notchFile = new PrintWriter(NOTCH_FILE);
        } catch (IOException ie) {
            ie.printStackTrace();
            System.exit(1);
        }

        /*
        System.out.println("Notch levels");
        for (int i = 0; i < N; i++)
        {
            System.out.printf("Notch %d/%d\n", i+1, N);
            Level level1 = LevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis(), DIFFICULTY, LevelGenerator.TYPE_OVERGROUND);
            Level level2 = LevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis()+10, DIFFICULTY, LevelGenerator.TYPE_OVERGROUND);
            double stat = KLDivergence.klDivergence(level1, level2, KWIDTH, KHEIGHT);
            notchFile.println(stat);

            //LevelView.show(level1);
            //LevelView.show(level2);
            //JOptionPane.showInputDialog(null, "ci?");
        }
        */
        int numThreads1 = 12; // N/NotchWorker.N_LOOPS;
        NotchWorker[] workers1 = new NotchWorker[numThreads1];
        Thread[] threads1 = new Thread[numThreads1];
        for (int i = 0; i < numThreads; i++)
        {
            workers1[i] = new NotchWorker();
            threads1[i] = new Thread(workers1[i]);
            threads1[i].start();
        }
        for (int i = 0; i < threads1.length; i++)
        {
            try {
                threads1[i].join();
                for (double d : workers1[i].getStat())
                {
                    notchFile.println(d);
                }
                System.out.printf("Notch %d/%d\n", (i+1)*N, N*numThreads1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        notchFile.close();
    }

    private static class OREWorker implements Runnable {
        // private static final int N_LOOPS = 1000;
        private double[] stat;
        @Override
        public void run() {
            stat = new double[N];
            long id = Thread.currentThread().getId();
            for (int i = 0; i < N; i++) 
            {
                System.out.printf("Thread %d starting %d/%d \n", id, i, N);
                Level level1 = OreLevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis(), DIFFICULTY, LevelGenerator.TYPE_OVERGROUND, true, true);
                Level level2 = OreLevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis()+10, DIFFICULTY, LevelGenerator.TYPE_OVERGROUND, true, true);
                stat[i] = KLDivergence.klDivergence(level1, level2, KWIDTH, KHEIGHT);

                // if (i % 10 == 0) {
                //     System.out.printf("Thread %d done %d/%d \n", id, i, N);
                // }
            }
        }

        public double[] getStat() { return stat; }

    }

    private static class NotchWorker implements Runnable {
        // private static final int N_LOOPS = 1000;
        private double[] stat;
        @Override
        public void run() {
            stat = new double[N];
            long id = Thread.currentThread().getId();
            for (int i = 0; i < N; i++) 
            {
                System.out.printf("Notch Thread %d starting %d/%d \n", id, i, N);
                Level level1 = LevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis(), DIFFICULTY, LevelGenerator.TYPE_OVERGROUND);
                Level level2 = LevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis()+10, DIFFICULTY, LevelGenerator.TYPE_OVERGROUND);
                stat[i] = KLDivergence.klDivergence(level1, level2, KWIDTH, KHEIGHT);
            }
        }

        public double[] getStat() { return stat; }

    }
}
