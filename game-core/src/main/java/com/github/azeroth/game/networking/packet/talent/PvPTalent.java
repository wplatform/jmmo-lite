package com.github.azeroth.game.networking.packet.talent;

import com.github.azeroth.game.networking.WorldPacket;

public final class PvPTalent {
    public short pvPTalentID;
    public byte slot;

    public PvPTalent() {
    }

    public PvPTalent(WorldPacket data) {
        pvPTalentID = data.readUInt16();
        slot = data.readUInt8();
    }

    public void write(WorldPacket data) {
        data.writeInt16(pvPTalentID);
        data.writeInt8(slot);
    }

    public PvPTalent clone() {
        PvPTalent varCopy = new PvPTalent();

        varCopy.pvPTalentID = this.pvPTalentID;
        varCopy.slot = this.slot;

        return varCopy;
    }
}
