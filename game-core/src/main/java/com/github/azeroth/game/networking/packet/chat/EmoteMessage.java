package com.github.azeroth.game.networking.packet.chat;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class EmoteMessage extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public int emoteID;

    public ArrayList<Integer> spellVisualKitIDs = new ArrayList<>();
    public int sequenceVariation;

    public EmoteMessage() {
        super(ServerOpcode.emote);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeInt32(emoteID);
        this.writeInt32(spellVisualKitIDs.size());
        this.writeInt32(sequenceVariation);

        for (var id : spellVisualKitIDs) {
            this.writeInt32(id);
        }
    }
}
