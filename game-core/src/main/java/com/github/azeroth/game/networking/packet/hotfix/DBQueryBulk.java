package com.github.azeroth.game.networking.packet.hotfix;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

class DBQueryBulk extends ClientPacket {

    public int tableHash;
    public ArrayList<DBQueryRecord> queries = new ArrayList<>();

    public DBQueryBulk(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        tableHash = this.readUInt();

        var count = this.<Integer>readBit(13);

        for (int i = 0; i < count; ++i) {
            queries.add(new DBQueryRecord(this.readUInt()));
        }
    }


    public final static class DBQueryRecord {

        public int recordID;

        public DBQueryRecord() {
        }


        public DBQueryRecord(int recordId) {
            recordID = recordId;
        }

        public DBQueryRecord clone() {
            DBQueryRecord varCopy = new DBQueryRecord();

            varCopy.recordID = this.recordID;

            return varCopy;
        }
    }
}
