package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public class QueryPageTextResponse extends ServerPacket {

    public int pageTextID;
    public boolean allow;
    public ArrayList<PageTextInfo> pages = new ArrayList<>();

    public QueryPageTextResponse() {
        super(ServerOpcode.QueryPageTextResponse);
    }

    @Override
    public void write() {
        this.writeInt32(pageTextID);
        this.writeBit(allow);
        this.flushBits();

        if (allow) {
            this.writeInt32(pages.size());

            for (var pageText : pages) {
                pageText.write(this);
            }
        }
    }


    public final static class PageTextInfo {

        public int id;

        public int nextPageID;
        public int playerConditionID;

        public byte flags;
        public String text;

        public void write(WorldPacket data) {
            data.writeInt32(id);
            data.writeInt32(nextPageID);
            data.writeInt32(playerConditionID);
            data.writeInt8(flags);
            data.writeBits(text.getBytes().length, 12);
            data.flushBits();

            data.writeString(text);
        }

        public PageTextInfo clone() {
            PageTextInfo varCopy = new PageTextInfo();

            varCopy.id = this.id;
            varCopy.nextPageID = this.nextPageID;
            varCopy.playerConditionID = this.playerConditionID;
            varCopy.flags = this.flags;
            varCopy.text = this.text;

            return varCopy;
        }
    }
}
