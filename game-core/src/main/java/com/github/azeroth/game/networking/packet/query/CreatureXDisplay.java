package com.github.azeroth.game.networking.packet.query;

public class CreatureXDisplay {

    public int creatureDisplayID;
    public float scale;
    public float probability;


    public CreatureXDisplay(int creatureDisplayID, float displayScale, float probability) {
        this.creatureDisplayID = creatureDisplayID;
        this.scale = displayScale;
        this.probability = probability;
    }
}
