package com.github.azeroth.game.networking.packet.trait;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ClassTalentsSetUsesSharedActionBars extends ClientPacket {
    public int configID;
    public boolean usesShared;
    public boolean isLastSelectedSavedConfig;

    public ClassTalentsSetUsesSharedActionBars(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        configID = this.readInt32();
        usesShared = this.readBit();
        isLastSelectedSavedConfig = this.readBit();
    }
}
