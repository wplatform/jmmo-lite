package com.github.azeroth.game.networking.packet.party;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.HashMap;

public class SendRaidTargetUpdateAll extends ServerPacket {
    public byte partyIndex;
    public HashMap<Byte, ObjectGuid> targetIcons = new HashMap<Byte, ObjectGuid>();

    public SendRaidTargetUpdateAll() {
        super(ServerOpcode.SendRaidTargetUpdateAll);
    }

    @Override
    public void write() {
        this.writeInt8(partyIndex);

        this.writeInt32(targetIcons.size());

        for (var pair : targetIcons.entrySet()) {
            this.writeGuid(pair.getValue());
            this.writeInt8(pair.getKey());
        }
    }
}
