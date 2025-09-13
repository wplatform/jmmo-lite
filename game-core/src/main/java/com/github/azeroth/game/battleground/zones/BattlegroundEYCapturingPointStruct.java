package com.github.azeroth.game.battleground.zones;


final class BattlegroundEYCapturingPointStruct {
    public int despawnNeutralObjectType;
    public int spawnObjectTypeAlliance;
    public int messageIdAlliance;
    public int spawnObjectTypeHorde;
    public int messageIdHorde;
    public int graveYardId;
    public BattlegroundEYCapturingPointStruct() {
    }
    public BattlegroundEYCapturingPointStruct(int _DespawnNeutralObjectType, int _SpawnObjectTypeAlliance, int _MessageIdAlliance, int _SpawnObjectTypeHorde, int _MessageIdHorde, int _GraveYardId) {
        despawnNeutralObjectType = _DespawnNeutralObjectType;
        spawnObjectTypeAlliance = _SpawnObjectTypeAlliance;
        messageIdAlliance = _MessageIdAlliance;
        spawnObjectTypeHorde = _SpawnObjectTypeHorde;
        messageIdHorde = _MessageIdHorde;
        graveYardId = _GraveYardId;
    }

    public BattlegroundEYCapturingPointStruct clone() {
        BattlegroundEYCapturingPointStruct varCopy = new BattlegroundEYCapturingPointStruct();

        varCopy.despawnNeutralObjectType = this.despawnNeutralObjectType;
        varCopy.spawnObjectTypeAlliance = this.spawnObjectTypeAlliance;
        varCopy.messageIdAlliance = this.messageIdAlliance;
        varCopy.spawnObjectTypeHorde = this.spawnObjectTypeHorde;
        varCopy.messageIdHorde = this.messageIdHorde;
        varCopy.graveYardId = this.graveYardId;

        return varCopy;
    }
}
