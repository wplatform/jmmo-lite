package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class MirrorImageComponentedData extends ServerPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;
    public int displayID;
    public int spellVisualKitID;

    public byte raceID;

    public byte gender;

    public byte classID;
    public ArrayList<ChrCustomizationChoice> customizations = new ArrayList<>();
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;

    public ArrayList<Integer> itemDisplayID = new ArrayList<>();

    public MirrorImageComponentedData() {
        super(ServerOpcode.MirrorImageComponentedData);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
        this.writeInt32(displayID);
        this.writeInt32(spellVisualKitID);
        this.writeInt8(raceID);
        this.writeInt8(gender);
        this.writeInt8(classID);
        this.writeInt32(customizations.size());
        this.writeGuid(guildGUID);
        this.writeInt32(itemDisplayID.size());

        for (var customization : customizations) {
            this.writeInt32(customization.chrCustomizationOptionID);
            this.writeInt32(customization.chrCustomizationChoiceID);
        }

        for (var itemDisplayId : itemDisplayID) {
            this.writeInt32(itemDisplayId);
        }
    }
}
