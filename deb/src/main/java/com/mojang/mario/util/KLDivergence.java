package com.mojang.mario.util;

import java.util.HashMap;
import java.util.Map;
import com.mojang.mario.level.Level;

/**
 * Metric for comparing two levels.
 * D_KL(P, Q) returns 0 if the levels are the same, otherwise
 * is positive the more the levels differ.
 */
public class KLDivergence {
    
    private static final double EPS = 0.0000001;

    /**
     * convolution over the level, returning unique windows and their number
     * of occurences in a map.
     * @param p Level to convolve over
     * @param kwidth width of kernel
     * @param kheight height of kernel
     * @return Map containing unique windows and their occurences
     */
    public static Map<Level, Integer> convolution(Level p, int kwidth, int kheight)
    {
        Map<Level, Integer> patterns = new HashMap<Level, Integer>();
        int ex = p.width - kwidth;
        int ey = p.height - kheight;
        for (int x = 0; x < ex; x++)
        {
            for (int y = 0; y < ey; y++)
            {
                Level level = p.getArea(x, y, kwidth, kheight);
                if (patterns.containsKey(level))
                {
                    int currentValue = patterns.get(level);
                    patterns.put(level, currentValue + 1);
                }
                else
                {
                    patterns.put(level, 1);
                }
            }
        }
        
        return patterns;
    }

    /**
     * Find klDivergence between p, q.
     * @param p Level used for p distribution in formula
     * @param q Level used for q distribution in formula
     * @param kwidth Width of the kernel
     * @param kheight Height of the kernel
     * @return klDivergence of P,Q == 0 if levels are the same
     */
    public static double klDivergence(Level p, Level q, int kwidth, int kheight)
    {
        // System.out.println("Starting klDivergence");
        Map<Level, Integer> px = convolution(p, kwidth, kheight);
        Map<Level, Integer> qx = convolution(q, kwidth, kheight);
        // System.out.printf("Kernels in px %d\nKernels in qx %d\n", px.size(), qx.size());

        double sum = 0.0;
        for (Level x : px.keySet())
        {
            double ppx = epsCorrectedP(px, x);
            double pqx = epsCorrectedP(qx, x);
            // System.out.printf("ppx %.4f pqx %.4f sum %.6f\n", ppx, pqx, sum);

            sum += ppx * Math.log(ppx/pqx);
        }

        return sum;
    }

    /**
     * espCorrectedP adjusts the probability distribution's entry for pattern.
     * Makes sure that for any entry, p is never zero of getting it
     * @param px
     * @param pattern
     * @return
     */
    public static double epsCorrectedP(Map<Level, Integer> px, Level pattern)
    {
        int res = px.containsKey(pattern)?px.get(pattern):0;
        int n = px.size();
        // System.out.printf("correctedP %d %d\n", res, n);
        double numerator = res + EPS;
        double denominator = (n + EPS) * (1 + EPS);
        double result = numerator / denominator;
        return result;
    }
}
