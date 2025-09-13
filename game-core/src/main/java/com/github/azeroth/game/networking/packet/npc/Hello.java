package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

// CMSG_BANKER_ACTIVATE
// CMSG_BINDER_ACTIVATE
// CMSG_BINDER_CONFIRM
// CMSG_GOSSIP_HELLO
// CMSG_LIST_INVENTORY
// CMSG_TRAINER_LIST
// CMSG_BATTLEMASTER_HELLO
public class Hello extends ClientPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;

    public Hello(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unit = this.readPackedGuid();
    }
}

//Structs

