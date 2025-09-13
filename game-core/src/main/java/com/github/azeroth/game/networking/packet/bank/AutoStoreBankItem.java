package com.github.azeroth.game.networking.packet.bank;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class AutoStoreBankItem extends ClientPacket {
    public invUpdate inv = new invUpdate();
    public byte bag;
    public byte slot;

    public AutoStoreBankItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        inv = new invUpdate(this);
        bag = this.readUInt8();
        slot = this.readUInt8();
    }
}
