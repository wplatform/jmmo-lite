package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ServerPacket;

class LfgOfferContinue extends ServerPacket {

    public int slot;


    public LfgOfferContinue(int slot) {
        super(ServerOpcode.LfgOfferContinue);
        slot = slot;
    }

    @Override
    public void write() {
        this.writeInt32(slot);
    }
}
