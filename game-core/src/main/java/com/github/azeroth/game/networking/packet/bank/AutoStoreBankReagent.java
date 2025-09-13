package com.github.azeroth.game.networking.packet.bank;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AutoStoreBankReagent extends ClientPacket {
    public invUpdate inv = new invUpdate();
    public byte slot;
    public byte packSlot;

    public AutoStoreBankReagent(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        inv = new invUpdate(this);
        slot = this.readUInt8();
        packSlot = this.readUInt8();
    }
}
