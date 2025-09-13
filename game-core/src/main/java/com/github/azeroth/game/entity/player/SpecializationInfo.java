package com.github.azeroth.game.entity.player;


import java.util.ArrayList;
import java.util.HashMap;

public class SpecializationInfo {

    private HashMap<Integer, PlayerSpellState>[] talents = new HashMap<Integer, PlayerSpellState>[MAX_SPECIALIZATIONS];

    private int[][] pvpTalents = new int[MAX_SPECIALIZATIONS][];

    private ArrayList<Integer>[] glyphs = new ArrayList<Integer>[MAX_SPECIALIZATIONS];

    private int resetTalentsCost;
    private long resetTalentsTime;

    private byte activeGroup;

    public SpecializationInfo() {
        for (byte i = 0; i < MAX_SPECIALIZATIONS; ++i) {
            getTalents()[i] = new HashMap<Integer, PlayerSpellState>();
            getPvpTalents()[i] = new int[PlayerConst.MaxPvpTalentSlots];
            getGlyphs()[i] = new ArrayList<>();
        }
    }


    public final HashMap<Integer, PlayerSpellState>[] getTalents() {
        return talents;
    }


    public final void setTalents(HashMap<Integer, PlayerSpellState>[] value) {
        talents = value;
    }


    public final int[][] getPvpTalents() {
        return pvpTalents;
    }


    public final void setPvpTalents(int[][] value) {
        pvpTalents = value;
    }


    public final ArrayList<Integer>[] getGlyphs() {
        return glyphs;
    }


    public final void setGlyphs(ArrayList<Integer>[] value) {
        glyphs = value;
    }


    public final int getResetTalentsCost() {
        return resetTalentsCost;
    }


    public final void setResetTalentsCost(int value) {
        resetTalentsCost = value;
    }

    public final long getResetTalentsTime() {
        return resetTalentsTime;
    }

    public final void setResetTalentsTime(long value) {
        resetTalentsTime = value;
    }


    public final byte getActiveGroup() {
        return activeGroup;
    }


    public final void setActiveGroup(byte value) {
        activeGroup = value;
    }
}
