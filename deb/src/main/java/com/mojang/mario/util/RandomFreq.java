package com.mojang.mario.util;

import java.util.Random;
/**
 * RandomFreq is a number generator.
 * It tries to favour numbers that have not been chosen
 * often to give a balanced distribution.
 */
public class RandomFreq {

    private int[] counts;
    private double[] intervals;
    private double max;
    private Random random;

    public RandomFreq(int bound, long seed)
    {
        counts = new int[bound];
        intervals = new double[bound];
        random = new Random(seed); 
        updateIntervals();
    }

    public RandomFreq(int bound)
    {
        this(bound, System.nanoTime());
    }

    private void updateIntervals()
    {
         System.out.println("Starting updateIntervals");
         double runningSum = 0.0;
         for (int i = 0; i < intervals.length; i++)
         {
             double tmp = 1.0 / Math.pow(counts[i] + 1, 2);
             intervals[i] = tmp + runningSum;
             runningSum += tmp;
             System.out.printf("%.2f ", intervals[i]);
         }
         max = runningSum;
         System.out.println("eanding updateIntervals");
    }

    public int get()
    {
        double num = random.nextDouble() * max;
        int i = -1;
        for (int j = 0; j < intervals.length; j++)
        {
             if (num < intervals[j])
             {
                 i = j;
                 counts[j]++; 
                 break;
             }
        }
        updateIntervals();
        return i;
    }

    public static void main(String[] args)
    {
         int[] freq = new int[6];
         RandomFreq random = new RandomFreq(6);
         for (int i = 0; i < 42; i++)
         {
             int result = random.get();
             freq[result]++;
         }
         for (int i = 0; i < freq.length; i++)
         {
             System.out.printf("%d : %d times\n", i, freq[i]);
         }
    }
}
