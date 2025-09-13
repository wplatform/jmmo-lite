package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class MountSpecial extends ClientPacket {
    public int[] spellVisualKitIDs;
    public int sequenceVariation;

    public MountSpecial(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        spellVisualKitIDs = new int[this.readUInt32()];
        sequenceVariation = this.readInt32();

        for (var i = 0; i < spellVisualKitIDs.length; ++i) {
            SpellVisualKitIDs[i] = this.readInt32();
        }
    }
}
