package com.github.azeroth.game.networking.packet.bank;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class AutoBankItem extends ClientPacket {
    public invUpdate inv = new invUpdate();

    public byte bag;

    public byte slot;

    public AutoBankItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        inv = new invUpdate(this);
        bag = this.readUInt8();
        slot = this.readUInt8();
    }
}

// CMSG_BUY_REAGENT_BANK
// CMSG_REAGENT_BANK_DEPOSIT

