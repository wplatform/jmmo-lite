package com.github.azeroth.game.movement.model;


import java.util.ArrayList;

public class SplineChainResumeInfo {
    public int pointID;
    public ArrayList<SplineChainLink> chain = new ArrayList<>();
    public boolean walkMode;
    public byte splineIndex;
    public byte pointIndex;
    public int timeToNext;

    public SplineChainResumeInfo() {
    }

    public SplineChainResumeInfo(int id, ArrayList<SplineChainLink> chain, boolean walk, byte splineIndex, byte wpIndex, int msToNext) {
        this.pointID = id;
        this.chain = chain;
        this.walkMode = walk;
        this.splineIndex = splineIndex;
        this.pointIndex = wpIndex;
        this.timeToNext = msToNext;
    }

    public final boolean empty() {
        return chain.isEmpty();
    }

    public final void clear() {
        chain.clear();
    }
}
