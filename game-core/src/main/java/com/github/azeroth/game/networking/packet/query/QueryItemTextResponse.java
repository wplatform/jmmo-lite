package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;

public class QueryItemTextResponse extends ServerPacket {
    public ObjectGuid id = ObjectGuid.EMPTY;
    public boolean valid;
    public String text;

    public QueryItemTextResponse() {
        super(ServerOpcode.QueryItemTextResponse);
    }

    @Override
    public void write() {
        this.writeBit(valid);
        this.writeBits(text.getBytes().length, 13);
        this.flushBits();

        this.writeString(text);
        this.writeGuid(id);
    }
}
