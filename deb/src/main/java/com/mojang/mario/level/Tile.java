package com.mojang.mario.level;

/**
 * Tile contains constants used to refer to specific tiles.
 * This is useful for a level generator.
 */
public interface Tile {
    byte AIR = (byte)0;
    byte ANCHOR_POINT = (byte)-2;
    byte BREAKABLE = (byte)16;
    byte COIN_REWARD = (byte)32;
    byte POWERUP_REWARD = (byte)22;
    byte HIDDEN_COIN_REWARD = (byte)17;
    byte HIDDEN_POWERUP_REWARD = (byte)18;
    byte FLOOR_LEFT_CORNER = (byte)-128;
    byte FLOOR_MID = (byte)-127;
    byte FLOOR_RIGHT_CORNER = (byte)-126;
    byte LEVEL_EXIT = (byte)-1;
    byte FLOOR_DECORATIVE = (byte)-125;
    byte FLOOR_LEFT_BLOCK = (byte)-112;
    byte FLOOR_RIGHT_BLOCK = (byte)-110;
    byte ICE = (byte)9;
    byte BULLET_BILL_TOP = (byte)14;
    byte BULLET_BILL_MID = (byte)30;
    byte BULLET_BILL_BOT = (byte)46;
    byte TUBE_TOP_LEFT = (byte)10;
    byte TUBE_TOP_RIGHT = (byte)11;
    byte TUBE_MID_LEFT = (byte)26;
    byte TUBE_MID_RIGHT = (byte)27;
    byte HILLTOP_LEFT_CORNER = (byte)-124;
    byte HILLTOP_MID = (byte)-123;
    byte HILLTOP_RIGHT_CORNER = (byte)-122;
    byte HILL_LEFT_EDGE = (byte)-108;
    byte HILL_DECORATIVE = (byte)-107;
    byte HILL_RIGHT_EDGE = (byte)-106;
}
