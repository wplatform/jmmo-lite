package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ServerPacket;

public class CharCustomizeFailure extends ServerPacket {
    public byte result;
    public ObjectGuid charGUID = ObjectGuid.EMPTY;

    public CharCustomizeFailure() {
        super(ServerOpcode.CharCustomizeFailure);
    }

    @Override
    public void write() {
        this.writeInt8(result);
        this.writeGuid(charGUID);
    }
}
