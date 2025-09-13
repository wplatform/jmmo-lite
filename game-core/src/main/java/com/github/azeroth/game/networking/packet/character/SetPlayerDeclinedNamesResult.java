package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ServerPacket;

public class SetPlayerDeclinedNamesResult extends ServerPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;
    public DeclinedNameResult resultCode = DeclinedNameResult.values()[0];

    public SetPlayerDeclinedNamesResult() {
        super(ServerOpcode.SetPlayerDeclinedNamesResult);
    }

    @Override
    public void write() {
        this.writeInt32(resultCode.getValue());
        this.writeGuid(player);
    }
}
