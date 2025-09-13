package com.github.azeroth.game.networking.packet.areatrigger;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AreaTriggerPkt extends ClientPacket {
    public int areaTriggerID;
    public boolean entered;
    public boolean fromClient;

    public AreaTriggerPkt(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        areaTriggerID = this.readUInt32();
        entered = this.readBit();
        fromClient = this.readBit();
    }
}

//Structs

