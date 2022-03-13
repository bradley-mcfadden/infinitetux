package com.mojang.mario.util;

/**
 * Logger is a simple standard out logger that
 * allows tags and a log level to be set.
 * 
 * In order of decreasing priority, ERROR, WARN, DEBUG, and INFO
 * are the log levels. 
 * Setting the log level will print all errors at or above that 
 * level. So, to see everything, use setLevel(LEVEL_INFO)
 */
public class Logger {
    public static final int LEVEL_ERROR = 1;
    public static final int LEVEL_WARN = 2;
    public static final int LEVEL_DEBUG = 3;
    public static final int LEVEL_INFO = 4;

    private static int level = -1000;

    /**
     * setLevel Set logging level to any of the named values
     * @param level LEVEL_ERROR, LEVEL_WARN, LEVEL_DEBUG, or LEVEL_INFO
     * @throws IllegalArgumentException if a value other than a log level is given.
     */
    public static void setLevel(int level)
    {/*
        switch(level) 
        {
        case LEVEL_ERROR:
        case LEVEL_WARN:
        case LEVEL_DEBUG:
        case LEVEL_INFO:
            Logger.level = level;
            break;
        default:
            throw new IllegalArgumentException("setLevel() called without using one of the Logger constants");
        }*/
    }

    /**
     * e logs at the LEVEL_ERROR level.
     * @param tag Appended to the start of the message.
     * @param message Appears after the tag.
     */
    public static void e(String tag, String message) 
    {
        if (Logger.level >= LEVEL_ERROR)
        {
            System.out.printf("%s:E %s\n", tag, message);
        }
    }

    /**
     * d logs at the LEVEL_DEBUG level.
     * @param tag Appended to the start of the message.
     * @param message Appears after the tag.
     */
    public static void d(String tag, String message) 
    {
        if (Logger.level >= LEVEL_DEBUG)
        {
            System.out.printf("%s:D %s\n", tag, message);
        }
    }

    /**
     * i logs at the LEVEL_INFO level
     * @param tag Prefixed at the message start
     * @param message Appears after the tag
     */
    public static void i(String tag, String message) 
    {
        if (Logger.level >= LEVEL_INFO)
        {
            System.out.printf("%s:I %s\n", tag, message);
        }
    }

    /**
     * w logs at the LEVEL_WARN level
     * @param tag Appears before the message on the same line
     * @param message Appears after the tag
     */
    public static void w(String tag, String message) 
    {
        if (Logger.level >= LEVEL_WARN)
        {
            System.out.printf("%s:W %s\n", tag, message);
        }
    } 
}
