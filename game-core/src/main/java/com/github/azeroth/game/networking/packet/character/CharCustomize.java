package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;


public class CharCustomize extends ClientPacket {
    public CharcustomizeInfo customizeInfo;

    public CharCustomize(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        customizeInfo = new CharCustomizeInfo();
        customizeInfo.charGUID = this.readPackedGuid();
        customizeInfo.sexID = gender.forValue((byte) this.readUInt8());
        var customizationCount = this.readUInt32();

        for (var i = 0; i < customizationCount; ++i) {
            ChrCustomizationChoice tempVar = new ChrCustomizationChoice();
            tempVar.chrCustomizationOptionID = this.readUInt32();
            tempVar.chrCustomizationChoiceID = this.readUInt32();
            customizeInfo.customizations.set(i, tempVar);
        }

        collections.sort(customizeInfo.customizations);

        customizeInfo.charName = this.readString(this.<Integer>readBit(6));
    }
}
