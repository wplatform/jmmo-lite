package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PartyUninvite extends ClientPacket {
    public byte partyIndex;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public String reason;

    public PartyUninvite(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readUInt8();
        targetGUID = this.readPackedGuid();

        var reasonLen = this.<Byte>readBit(8);
        reason = this.readString(reasonLen);
    }
}
