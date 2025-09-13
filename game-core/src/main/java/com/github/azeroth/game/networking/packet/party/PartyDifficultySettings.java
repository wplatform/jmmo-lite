package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.WorldPacket;

final class PartyDifficultySettings {
    public int dungeonDifficultyID;
    public int raidDifficultyID;
    public int legacyRaidDifficultyID;

    public void write(WorldPacket data) {
        data.writeInt32(dungeonDifficultyID);
        data.writeInt32(raidDifficultyID);
        data.writeInt32(legacyRaidDifficultyID);
    }

    public PartyDifficultySettings clone() {
        PartyDifficultySettings varCopy = new PartyDifficultySettings();

        varCopy.dungeonDifficultyID = this.dungeonDifficultyID;
        varCopy.raidDifficultyID = this.raidDifficultyID;
        varCopy.legacyRaidDifficultyID = this.legacyRaidDifficultyID;

        return varCopy;
    }
}
