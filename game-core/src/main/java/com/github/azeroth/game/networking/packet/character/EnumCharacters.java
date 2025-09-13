package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class EnumCharacters extends ClientPacket {
    public EnumCharacters(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}

// @todo: CharCustomizeResult

//Structs

