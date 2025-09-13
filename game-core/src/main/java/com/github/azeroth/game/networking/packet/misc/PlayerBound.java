package com.github.azeroth.game.networking.packet.misc;


public class PlayerBound extends ServerPacket {
    private final int areaID;

    private final ObjectGuid binderID;

    public PlayerBound(ObjectGuid binderId, int areaId) {
        super(ServerOpcode.PlayerBound);
        binderID = binderId;
        areaID = areaId;
    }

    @Override
    public void write() {
        this.writeGuid(binderID);
        this.writeInt32(areaID);
    }
}
