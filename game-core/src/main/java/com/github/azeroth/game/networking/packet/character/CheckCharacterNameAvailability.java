package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CheckCharacterNameAvailability extends ClientPacket {

    public int sequenceIndex;
    public String name;

    public CheckCharacterNameAvailability(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        sequenceIndex = this.readUInt32();
        name = this.readString(this.<Integer>readBit(6));
    }
}
