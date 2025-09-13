package com.github.azeroth.game.networking.packet.chat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class CTextEmote extends ClientPacket {
    public ObjectGuid target = ObjectGuid.EMPTY;
    public int emoteID;
    public int soundIndex;
    public int[] spellVisualKitIDs;
    public int sequenceVariation;

    public CTextEmote(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        target = this.readPackedGuid();
        emoteID = this.readInt32();
        soundIndex = this.readInt32();

        spellVisualKitIDs = new int[this.readUInt32()];
        sequenceVariation = this.readInt32();

        for (var i = 0; i < spellVisualKitIDs.length; ++i) {
            SpellVisualKitIDs[i] = this.readUInt32();
        }
    }
}
