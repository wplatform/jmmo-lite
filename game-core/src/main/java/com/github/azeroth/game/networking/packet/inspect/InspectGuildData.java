package com.github.azeroth.game.networking.packet.inspect;

import com.github.azeroth.game.networking.WorldPacket;

public final class InspectGuildData {
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public int numGuildMembers;
    public int achievementPoints;

    public void write(WorldPacket data) {
        data.writeGuid(guildGUID);
        data.writeInt32(numGuildMembers);
        data.writeInt32(achievementPoints);
    }

    public InspectGuildData clone() {
        InspectGuildData varCopy = new InspectGuildData();

        varCopy.guildGUID = this.guildGUID;
        varCopy.numGuildMembers = this.numGuildMembers;
        varCopy.achievementPoints = this.achievementPoints;

        return varCopy;
    }
}
