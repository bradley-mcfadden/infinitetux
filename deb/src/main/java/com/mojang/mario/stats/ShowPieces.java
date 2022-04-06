package com.mojang.mario.stats;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import com.mojang.mario.level.ChunkLibrary;
import com.mojang.mario.level.Level;
import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.level.OreLevelGenerator;
import com.mojang.mario.mapedit.LevelView;
import com.mojang.mario.util.KLDivergence;
import com.mojang.mario.util.Logger;

public class ShowPieces {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 15;
    private static final int DIFFICULTY = 3;
    private static final int KWIDTH = 6;
    private static final int KHEIGHT = 6;

    private static File programDirectory;

    private static void setupDirectory()
    {
        String userDir = System.getProperty("user.dir");
        programDirectory = new File(userDir + "/.infinitetux");
        programDirectory.mkdirs();
    }

    public static void main(String[] args) {
        Logger.setLevel(Logger.LEVEL_ERROR);
        setupDirectory();
        ChunkLibrary.init(programDirectory);
        ChunkLibrary.loadChunksNoThreading();
        System.out.println("Chunks loaded");


        Level level = LevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis(), DIFFICULTY, LevelGenerator.TYPE_OVERGROUND);
        // Level level = OreLevelGenerator.createLevel(WIDTH, HEIGHT, System.currentTimeMillis()+10, DIFFICULTY, LevelGenerator.TYPE_OVERGROUND, true, true);

        Map<Level, Integer> dist = KLDivergence.convolution(level, KWIDTH, KHEIGHT);

        List<Entry<Level, Integer>> list = new ArrayList<>(dist.entrySet());
        list.sort(Entry.comparingByValue());
        Collections.reverse(list);

        int total = list.size();
        System.out.println("Total chunks " + total);
        // Scanner scanner = new Scanner(System.in);
        int i = 0;
        for (Entry<Level, Integer> entry : list) {
            i++;
            Level partial = entry.getKey();
            int count = entry.getValue();
            System.out.println("Current chunk " + count);
            if (i < 41)
                LevelView.showAndCapture(partial, i);
            else
                break;
            //else
            //    LevelView.show(partial);
            // scanner.nextLine();
        }
        // scanner.close();

    }
}
