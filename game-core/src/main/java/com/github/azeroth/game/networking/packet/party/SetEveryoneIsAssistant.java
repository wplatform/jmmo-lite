package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetEveryoneIsAssistant extends ClientPacket {
    public byte partyIndex;
    public boolean everyoneIsAssistant;

    public setEveryoneIsAssistant(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readUInt8();
        everyoneIsAssistant = this.readBit();
    }
}
