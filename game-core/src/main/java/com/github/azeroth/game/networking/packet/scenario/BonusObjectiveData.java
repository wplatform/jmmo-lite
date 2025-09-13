package com.github.azeroth.game.networking.packet.scenario;

import com.github.azeroth.game.networking.WorldPacket;

final class BonusObjectiveData {
    public int bonusObjectiveID;
    public boolean objectiveComplete;

    public void write(WorldPacket data) {
        data.writeInt32(bonusObjectiveID);
        data.writeBit(objectiveComplete);
        data.flushBits();
    }

    public BonusObjectiveData clone() {
        BonusObjectiveData varCopy = new BonusObjectiveData();

        varCopy.bonusObjectiveID = this.bonusObjectiveID;
        varCopy.objectiveComplete = this.objectiveComplete;

        return varCopy;
    }
}
