package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class CorpseData extends UpdateMaskObject {
    public Dynamic<ChrCustomizationChoice> customizations = new Dynamic<ChrCustomizationChoice>(0, 1);

    public FieldType<Integer> dynamicFlags = new FieldType<>(0, 2);
    public FieldType<ObjectGuid> owner = new FieldType<>(0, 3);
    public FieldType<ObjectGuid> partyGUID = new FieldType<>(0, 4);
    public FieldType<ObjectGuid> guildGUID = new FieldType<>(0, 5);

    public FieldType<Integer> displayID = new FieldType<>(0, 6);

    public FieldType<Byte> raceID = new FieldType<>(0, 7);

    public FieldType<Byte> sex = new FieldType<>(0, 8);

    public FieldType<Byte>class =new FieldType<>(0,9);

    public FieldType<Integer> flags = new FieldType<>(0, 10);
    public FieldType<Integer> factionTemplate = new FieldType<>(0, 11);

    public FieldType<Integer> stateSpellVisualKitID = new FieldType<>(0, 12);

    public Array<Integer> items = new Array<Integer>(19, 13, 14);


    public CorpseData(int changeMask) {
        super(33);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Corpse owner, Player receiver) {
        data.writeInt32(dynamicFlags);
        data.writeGuid(owner);
        data.writeGuid(partyGUID);
        data.writeGuid(guildGUID);
        data.writeInt32(displayID);

        for (var i = 0; i < 19; ++i) {
            data.writeInt32(items.get(i));
        }

        data.writeInt8(raceID);
        data.writeInt8(sex);
        data.writeInt8( class);
        data.writeInt32(customizations.size());
        data.writeInt32(flags);
        data.writeInt32(factionTemplate);
        data.writeInt32(stateSpellVisualKitID);

        for (var i = 0; i < customizations.size(); ++i) {
            customizations.get(i).writeCreate(data, owner, receiver);
        }
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Corpse owner, Player receiver) {
        writeUpdate(data, getChangesMask(), false, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Corpse owner, Player receiver) {
        data.writeBits(changesMask.getBlocksMask(0), 2);

        for (int i = 0; i < 2; ++i) {
            if (changesMask.getBlock(i) != 0) {
                data.writeBits(changesMask.getBlock(i), 32);
            }
        }

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                if (!ignoreNestedChangesMask) {
                    customizations.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(customizations.size(), data);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                for (var i = 0; i < customizations.size(); ++i) {
                    if (customizations.hasChanged(i) || ignoreNestedChangesMask) {
                        customizations.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(2)) {
                data.writeInt32(dynamicFlags);
            }

            if (changesMask.get(3)) {
                data.writeGuid(owner);
            }

            if (changesMask.get(4)) {
                data.writeGuid(partyGUID);
            }

            if (changesMask.get(5)) {
                data.writeGuid(guildGUID);
            }

            if (changesMask.get(6)) {
                data.writeInt32(displayID);
            }

            if (changesMask.get(7)) {
                data.writeInt8(raceID);
            }

            if (changesMask.get(8)) {
                data.writeInt8(sex);
            }

            if (changesMask.get(9)) {
                data.writeInt8( class);
            }

            if (changesMask.get(10)) {
                data.writeInt32(flags);
            }

            if (changesMask.get(11)) {
                data.writeInt32(factionTemplate);
            }

            if (changesMask.get(12)) {
                data.writeInt32(stateSpellVisualKitID);
            }
        }

        if (changesMask.get(13)) {
            for (var i = 0; i < 19; ++i) {
                if (changesMask.get(14 + i)) {
                    data.writeInt32(items.get(i));
                }
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(customizations);
        clearChangesMask(dynamicFlags);
        clearChangesMask(owner);
        clearChangesMask(partyGUID);
        clearChangesMask(guildGUID);
        clearChangesMask(displayID);
        clearChangesMask(raceID);
        clearChangesMask(sex);
        clearChangesMask( class);
        clearChangesMask(flags);
        clearChangesMask(factionTemplate);
        clearChangesMask(stateSpellVisualKitID);
        clearChangesMask(items);
        getChangesMask().resetAll();
    }
}
