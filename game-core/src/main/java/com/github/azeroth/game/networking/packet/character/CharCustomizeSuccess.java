package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ServerPacket;

public class CharCustomizeSuccess extends ServerPacket {
    private final String charName = "";
    private final byte sexID;
    private final Array<ChrCustomizationChoice> customizations = new Array<ChrCustomizationChoice>(72);

    private final ObjectGuid charGUID;

    public CharCustomizeSuccess(CharCustomizeInfo customizeInfo) {
        super(ServerOpcode.CharCustomizeSuccess);
        charGUID = customizeInfo.charGUID;
        sexID = (byte) customizeInfo.sexID.getValue();
        charName = customizeInfo.charName;
        customizations = customizeInfo.customizations;
    }

    @Override
    public void write() {
        this.writeGuid(charGUID);
        this.writeInt8(sexID);
        this.writeInt32(customizations.size());

        for (var customization : customizations) {
            this.writeInt32(customization.chrCustomizationOptionID);
            this.writeInt32(customization.chrCustomizationChoiceID);
        }

        this.writeBits(charName.getBytes().length, 6);
        this.flushBits();
        this.writeString(charName);
    }
}
