package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.entity.unit.declinedName;
import com.github.azeroth.game.networking.ServerPacket;

class QueryPetNameResponse extends ServerPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;
    public boolean allow;

    public boolean hasDeclined;
    public declinedName declinedNames = new declinedName();
    public long timestamp;
    public String name = "";

    public QueryPetNameResponse() {
        super(ServerOpcode.QueryPetNameResponse);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
        this.writeBit(allow);

        if (allow) {
            this.writeBits(name.getBytes().length, 8);
            this.writeBit(hasDeclined);

            for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
                this.writeBits(declinedNames.name.charAt(i).getBytes().length, 7);
            }

            for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
                this.writeString(declinedNames.name.charAt(i));
            }

            this.writeInt64(timestamp);
            this.writeString(name);
        }

        this.flushBits();
    }
}
