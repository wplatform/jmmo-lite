package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class TitleEarned extends ServerPacket {
    public int index;

    public TitleEarned(ServerOpCode opcode) {
        super(opcode);
    }

    @Override
    public void write() {
        this.writeInt32(index);
    }
}
