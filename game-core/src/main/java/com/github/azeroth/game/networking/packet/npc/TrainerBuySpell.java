package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class TrainerBuySpell extends ClientPacket {
    public ObjectGuid trainerGUID = ObjectGuid.EMPTY;
    public int trainerID;
    public int spellID;

    public TrainerBuySpell(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        trainerGUID = this.readPackedGuid();
        trainerID = this.readUInt32();
        spellID = this.readUInt32();
    }
}
