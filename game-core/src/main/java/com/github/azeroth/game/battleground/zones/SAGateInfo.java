package com.github.azeroth.game.battleground.zones;


class SAGateInfo {
    public int gateId;
    public int gameObjectId;
    public int worldState;
    public int damagedText;
    public int destroyedText;

    public SAGateInfo(int gateId, int gameObjectId, int worldState, int damagedText, int destroyedText) {
        gateId = gateId;
        gameObjectId = gameObjectId;
        worldState = worldState;
        damagedText = damagedText;
        destroyedText = destroyedText;
    }
}
