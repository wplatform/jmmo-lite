package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.networking.ServerPacket;

public class CorpseLocation extends ServerPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;
    public ObjectGuid transport = ObjectGuid.EMPTY;
    public Vector3 position;
    public int actualMapID;
    public int mapID;
    public boolean valid;

    public CorpseLocation() {
        super(ServerOpcode.CorpseLocation);
    }

    @Override
    public void write() {
        this.writeBit(valid);
        this.flushBits();

        this.writeGuid(player);
        this.writeInt32(actualMapID);
        this.writeVector3(position);
        this.writeInt32(mapID);
        this.writeGuid(transport);
    }
}
