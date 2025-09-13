package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

final class GarrisonMissionBonusAbility {
    public int garrMssnBonusAbilityID;
    public long startTime;

    public void write(WorldPacket data) {
        data.writeInt64(startTime);
        data.writeInt32(garrMssnBonusAbilityID);
    }

    public GarrisonMissionBonusAbility clone() {
        GarrisonMissionBonusAbility varCopy = new GarrisonMissionBonusAbility();

        varCopy.garrMssnBonusAbilityID = this.garrMssnBonusAbilityID;
        varCopy.startTime = this.startTime;

        return varCopy;
    }
}
