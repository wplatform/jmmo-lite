package com.github.azeroth.game.networking.packet.query;


public class CorpseTransportQuery extends ServerPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;
    public Vector3 position;
    public float facing;

    public CorpseTransportQuery() {
        super(ServerOpcode.CorpseTransportQuery);
    }

    @Override
    public void write() {
        this.writeGuid(player);
        this.writeVector3(position);
        this.writeFloat(facing);
    }
}
