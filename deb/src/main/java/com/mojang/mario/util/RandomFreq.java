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

    /**
     * Constructor.
     * @param bound Upper bound on returned number
     * @param seed Seed for the random number generation
     */
    public RandomFreq(int bound, long seed)
    {
        counts = new int[bound];
        intervals = new double[bound];
        random = new Random(seed); 
        updateIntervals();
    }

    /**
     * Constructor.
     * @param bound Upper bound on returned numbers.
     */
    public RandomFreq(int bound)
    {
        this(bound, System.nanoTime());
    }

    private void updateIntervals()
    {
         Logger.i("RandomFreq", "Starting updateIntervals");
         double runningSum = 0.0;
         for (int i = 0; i < intervals.length; i++)
         {
             double tmp = 1.0 / Math.pow(counts[i] + 1, 2);
             intervals[i] = tmp + runningSum;
             runningSum += tmp;
             // System.out.printf("%.2f ", intervals[i]);
         }
         max = runningSum;
         Logger.i("RandomFreq", "ending updateIntervals");
    }

    /**
     * get a number between [0, bound)
     * @return Integer between [0, bound)
     */
    public int get()
    {
        int i = getNoUpdate();
        updateIntervals();
        return i;
    }

    /**
     * getNoUpdate returns a number, but does not update the frequencies
     * so effectively using getNoUpdate is Random.nextInt() with a lot more
     * overhead. Useful if you can't guarantee you'll use the number.
     * @return Integer between [0, bound)
     */
    public int getNoUpdate()
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

    /**
     * updateValue i by incrementing its counter.
     * Useful with getNoUpdate to apply an update after checking
     * if the number is needed.
     * @param i value to update counter for, in range [0,bound)
     */
    public void updateValue(int i)
    {
        counts[i]++;
        updateIntervals();
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
