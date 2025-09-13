package com.github.azeroth.game.networking.packet.battlepet;


import com.github.azeroth.game.networking.ServerPacket;

public class QueryBattlePetNameResponse extends ServerPacket {
    public ObjectGuid battlePetID = ObjectGuid.EMPTY;
    public int creatureID;
    public long timestamp;
    public boolean allow;

    public boolean hasDeclined;
    public DeclinedName declinedNames;
    public String name;

    public QueryBattlePetNameResponse() {
        super(ServerOpcode.QueryBattlePetNameResponse);
    }

    @Override
    public void write() {
        this.writeGuid(battlePetID);
        this.writeInt32(creatureID);
        this.writeInt64(timestamp);

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

            this.writeString(name);
        }

        this.flushBits();
    }
}
