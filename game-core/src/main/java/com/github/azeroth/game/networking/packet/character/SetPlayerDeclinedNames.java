package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.unit.declinedName;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetPlayerDeclinedNames extends ClientPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;
    public DeclinedName declinedNames;

    public SetPlayerDeclinedNames(WorldPacket packet) {
        super(packet);
        declinedNames = new declinedName();
    }

    @Override
    public void read() {
        player = this.readPackedGuid();

        var stringLengths = new byte[SharedConst.MaxDeclinedNameCases];

        for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
            stringLengths[i] = this.<Byte>readBit(7);
        }

        for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
            declinedNames.name.charAt(i) = this.readString(stringLengths[i]);
        }
    }
}
