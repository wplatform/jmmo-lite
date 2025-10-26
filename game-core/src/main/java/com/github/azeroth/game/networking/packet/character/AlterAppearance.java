package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.entity.object.update.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;


public class AlterAppearance extends ClientPacket {
    public byte newSex;
    public ChrCustomizationChoice[] customizations = new ChrCustomizationChoice[72];
    public int customizedRace;

    public AlterAppearance(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        var customizationCount = this.readUInt32();
        newSex = this.readByte();
        customizedRace = this.readInt32();

        for (var i = 0; i < customizationCount; ++i) {
            ChrCustomizationChoice tempVar = new ChrCustomizationChoice();
            tempVar.chrCustomizationOptionID = this.readUInt32();
            tempVar.chrCustomizationChoiceID = this.readUInt32();
            customizations[i] = tempVar;

        }
    }
}
