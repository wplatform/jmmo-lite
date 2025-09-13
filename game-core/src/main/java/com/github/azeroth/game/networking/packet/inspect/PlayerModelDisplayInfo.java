package com.github.azeroth.game.networking.packet.inspect;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class PlayerModelDisplayInfo {
    public ObjectGuid GUID = ObjectGuid.EMPTY;
    public ArrayList<InspectItemData> items = new ArrayList<>();
    public String name;
    public int specializationID;
    public byte genderID;
    public byte race;
    public byte classID;
    public ArrayList<ChrCustomizationChoice> customizations = new ArrayList<>();

    public final void initialize(Player player) {
        GUID = player.getGUID();
        specializationID = player.getPrimarySpecialization();
        name = player.getName();
        genderID = (byte) player.getNativeGender().getValue();
        race = (byte) player.getRace().getValue();
        classID = (byte) player.getClass().getValue();

        for (var customization : player.getPlayerData().customizations) {
            customizations.add(customization);
        }

        for (byte i = 0; i < EquipmentSlot.End; ++i) {
            var item = player.getItemByPos(InventorySlots.Bag0, i);

            if (item != null) {
                items.add(new InspectItemData(item, i));
            }
        }
    }

    public final void write(WorldPacket data) {
        data.writeGuid(GUID);
        data.writeInt32(specializationID);
        data.writeInt32(items.size());
        data.writeBits(name.getBytes().length, 6);
        data.writeInt8(genderID);
        data.writeInt8(race);
        data.writeInt8(classID);
        data.writeInt32(customizations.size());
        data.writeString(name);

        for (var customization : customizations) {
            data.writeInt32(customization.chrCustomizationOptionID);
            data.writeInt32(customization.chrCustomizationChoiceID);
        }

        for (var item : items) {
            item.write(data);
        }
    }
}
