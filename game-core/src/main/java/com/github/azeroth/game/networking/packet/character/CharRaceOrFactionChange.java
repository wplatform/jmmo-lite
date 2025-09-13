package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.collections;

public class CharRaceOrFactionChange extends ClientPacket {
    public CharraceOrFactionChangeInfo raceOrFactionChangeInfo;

    public CharRaceOrFactionChange(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        raceOrFactionChangeInfo = new CharRaceOrFactionChangeInfo();

        raceOrFactionChangeInfo.factionChange = this.readBit();

        var nameLength = this.<Integer>readBit(6);

        raceOrFactionChangeInfo.guid = this.readPackedGuid();
        raceOrFactionChangeInfo.sexID = gender.forValue((byte) this.readUInt8());
        raceOrFactionChangeInfo.raceID = race.forValue(this.readUInt8());
        raceOrFactionChangeInfo.initialRaceID = race.forValue(this.readUInt8());
        var customizationCount = this.readUInt32();
        raceOrFactionChangeInfo.name = this.readString(nameLength);

        for (var i = 0; i < customizationCount; ++i) {
            ChrCustomizationChoice tempVar = new ChrCustomizationChoice();
            tempVar.chrCustomizationOptionID = this.readUInt32();
            tempVar.chrCustomizationChoiceID = this.readUInt32();
            raceOrFactionChangeInfo.customizations.set(i, tempVar);
        }

        collections.sort(raceOrFactionChangeInfo.customizations);
    }
}
