package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;


public class CreateCharacter extends ClientPacket {
    public CharactercreateInfo createInfo;

    public CreateCharacter(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        createInfo = new CharacterCreateInfo();
        var nameLength = this.<Integer>readBit(6);
        var hasTemplateSet = this.readBit();
        createInfo.isTrialBoost = this.readBit();
        createInfo.useNPE = this.readBit();

        createInfo.raceId = race.forValue(this.readUInt8());
        createInfo.classId = playerClass.forValue(this.readUInt8());
        createInfo.sex = gender.forValue((byte) this.readUInt8());
        var customizationCount = this.readUInt32();

        createInfo.name = this.readString(nameLength);

        if (createInfo.templateSet != null) {
            createInfo.templateSet = this.readUInt32();
        }

        for (var i = 0; i < customizationCount; ++i) {
            ChrCustomizationChoice tempVar = new ChrCustomizationChoice();
            tempVar.chrCustomizationOptionID = this.readUInt32();
            tempVar.chrCustomizationChoiceID = this.readUInt32();
            createInfo.customizations.set(i, tempVar);
        }

        collections.sort(createInfo.customizations);
    }
}
