package com.github.azeroth.game.networking.packet.battlepet;


import com.github.azeroth.game.entity.unit.declinedName;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BattlePetModifyName extends ClientPacket {
    public ObjectGuid petGuid = ObjectGuid.EMPTY;
    public String name;
    public DeclinedName declinedNames;

    public BattlePetModifyName(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGuid = this.readPackedGuid();
        var nameLength = this.<Integer>readBit(7);

        if (this.readBit()) {
            declinedNames = new declinedName();

            var declinedNameLengths = new byte[SharedConst.MaxDeclinedNameCases];

            for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
                declinedNameLengths[i] = this.<Byte>readBit(7);
            }

            for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
                declinedNames.name.charAt(i) = this.readString(declinedNameLengths[i]);
            }
        }

        name = this.readString(nameLength);
    }
}
