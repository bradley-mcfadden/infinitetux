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
    private static final int N = 250;
    private static final int WIDTH = 128;
    private static final int HEIGHT = 15;
    private static final int DIFFICULTY = 3;
    private static final int KWIDTH = 4;
    private static final int KHEIGHT = 4;
    private static final String ORE_FILE = "ore.csv";
    private static final String NOTCH_FILE = "notch.csv";

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
        for (int i = 0; i < N; i++)
        {
            System.out.printf("ORE %d/%d\n", i+1, N);
            Level level1 = OreLevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis(), DIFFICULTY, LevelGenerator.TYPE_OVERGROUND, true, true);
            Level level2 = OreLevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis()+10, DIFFICULTY, LevelGenerator.TYPE_OVERGROUND, true, true);
            double stat = KLDivergence.klDivergence(level1, level2, KWIDTH, KHEIGHT);
            // LevelView.show(level1);
            // LevelView.show(level2);
            // JOptionPane.showInputDialog(null, "");
            oreFile.println(stat);
        }
        oreFile.close();

        PrintWriter notchFile = null;
        try {
            notchFile = new PrintWriter(NOTCH_FILE);
        } catch (IOException ie) {
            ie.printStackTrace();
            System.exit(1);
        }

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
        notchFile.close();
    }
}
