package com.github.azeroth.game.networking.packet.misc;


public class PreRessurect extends ServerPacket {
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;

    public PreRessurect() {
        super(ServerOpcode.PreRessurect);
    }

    @Override
    public void write() {
        this.writeGuid(playerGUID);
    }
}
