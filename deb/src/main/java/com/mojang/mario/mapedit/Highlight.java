package com.mojang.mario.mapedit;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Class for creating labelled highlights onto Graphics objects.
 */
public class Highlight {
    private static final int OPACITY = 128;
    public static final Color YELLOW = new Color(255, 245, 157, OPACITY);
    public static final Color RED = new Color(239, 154, 154, OPACITY);
    public static final Color PURPLE = new Color(206, 147, 216, OPACITY);
    public static final Color BLUE = new Color(144, 202, 249, OPACITY);
    public static final Color GREEN = new Color(165, 214, 167, OPACITY);

    private int x, y;
    private int w, h;
    private Color color;
    private String message;

    /**
     * Constructor.
     * @param x x tile coordinate
     * @param y y tile coordinate
     * @param w width in tiles
     * @param h height in tiles
     * @param color Color to draw as
     * @param message Message for label.
     */
    public Highlight(int x, int y, int w, int h, Color color, String message)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = color;
        this.message = message;
    }

    /**
     * Draws the Highlight onto the @param g
     * @param g Graphics object to draw onto
     */
    public void draw(Graphics g)
    {
        Color oldColor = g.getColor();
        g.setColor(color);
        g.fillRect(x<<4, y<<4, w<<4, h<<4);
        if (message != null) 
        {
            g.setColor(Color.BLACK);
            g.drawString(message, (x<<4)+2, ((y+1)<<4)-2);
        }
        g.setColor(oldColor);
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getW() { return w; }
    public void setW(int w) { this.w = w; }
    public int getH() { return h; }
    public void setH(int h) { this.h = h; }
    public void setMessage(String message) { this.message = message; }
}
