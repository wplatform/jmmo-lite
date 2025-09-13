package com.github.azeroth.game.movement;


import java.util.ArrayList;

public class SplineChainResumeInfo {
    public int pointID;
    public ArrayList<SplinechainLink> chain = new ArrayList<>();
    public boolean isWalkMode;
    public byte splineIndex;
    public byte pointIndex;
    public int timeToNext;

    public SplineChainResumeInfo() {
    }

    public SplineChainResumeInfo(int id, ArrayList<SplineChainLink> chain, boolean walk, byte splineIndex, byte wpIndex, int msToNext) {
        pointID = id;
        chain = chain;
        isWalkMode = walk;
        splineIndex = splineIndex;
        pointIndex = wpIndex;
        timeToNext = msToNext;
    }

    public final boolean empty() {
        return chain.isEmpty();
    }

    public final void clear() {
        chain.clear();
    }
}
