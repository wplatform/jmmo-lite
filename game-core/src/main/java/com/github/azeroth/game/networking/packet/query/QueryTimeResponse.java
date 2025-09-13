package com.github.azeroth.game.networking.packet.query;


public class QueryTimeResponse extends ServerPacket {
    public long currentTime;

    public QueryTimeResponse() {
        super(ServerOpcode.QueryTimeResponse);
    }

    @Override
    public void write() {
        this.writeInt64(currentTime);
    }
}
