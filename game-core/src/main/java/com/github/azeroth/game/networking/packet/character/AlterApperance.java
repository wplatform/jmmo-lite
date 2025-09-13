package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;


public class AlterApperance extends ClientPacket {
    public byte newSex;
    public Array<ChrCustomizationChoice> customizations = new Array<ChrCustomizationChoice>(72);
    public int customizedRace;

    public AlterApperance(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var customizationCount = this.readUInt32();
        newSex = this.readUInt8();
        customizedRace = this.readInt32();

        for (var i = 0; i < customizationCount; ++i) {
            ChrCustomizationChoice tempVar = new ChrCustomizationChoice();
            tempVar.chrCustomizationOptionID = this.readUInt32();
            tempVar.chrCustomizationChoiceID = this.readUInt32();
            customizations.set(i, tempVar);
        }

        collections.sort(customizations);
    }
}
