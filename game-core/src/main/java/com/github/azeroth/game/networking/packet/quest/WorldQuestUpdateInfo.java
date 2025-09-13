package com.github.azeroth.game.networking.packet.quest;

final class WorldQuestUpdateInfo {
    public long lastUpdate;
    public int questID;
    public int timer;
    // WorldState
    public int variableID;
    public int value;

    public WorldQuestUpdateInfo() {
    }
    public WorldQuestUpdateInfo(long lastUpdate, int questID, int timer, int variableID, int value) {
        lastUpdate = lastUpdate;
        questID = questID;
        timer = timer;
        variableID = variableID;
        value = value;
    }

    public WorldQuestUpdateInfo clone() {
        WorldQuestUpdateInfo varCopy = new WorldQuestUpdateInfo();

        varCopy.lastUpdate = this.lastUpdate;
        varCopy.questID = this.questID;
        varCopy.timer = this.timer;
        varCopy.variableID = this.variableID;
        varCopy.value = this.value;

        return varCopy;
    }
}
