package com.github.azeroth.game.networking.packet.bank;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AutoBankReagent extends ClientPacket {
    public invUpdate inv = new invUpdate();
    public byte slot;
    public byte packSlot;

    public AutoBankReagent(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        inv = new invUpdate(this);
        packSlot = this.readUInt8();
        slot = this.readUInt8();
    }
}
