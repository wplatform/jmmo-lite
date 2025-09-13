package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class TraitConfig extends UpdateMaskObject {
    public Dynamic<TraitEntry> entries = new Dynamic<TraitEntry>(0, 1);
    public FieldType<Integer> ID = new FieldType<>(0, 2);
    public updateFieldString name = new updateFieldString(0, 3);
    public FieldType<Integer> type = new FieldType<>(4, 5);
    public FieldType<Integer> skillLineID = new FieldType<>(4, 6);
    public FieldType<Integer> chrSpecializationID = new FieldType<>(4, 7);
    public FieldType<Integer> combatConfigFlags = new FieldType<>(8, 9);
    public FieldType<Integer> localIdentifier = new FieldType<>(8, 10);
    public FieldType<Integer> traitSystemID = new FieldType<>(8, 11);

    public TraitConfig() {
        super(12);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(ID);
        data.writeInt32(type);
        data.writeInt32(entries.size());

        if (type == 2) {
            data.writeInt32(skillLineID);
        }

        if (type == 1) {
            data.writeInt32(chrSpecializationID);
            data.writeInt32(combatConfigFlags);
            data.writeInt32(localIdentifier);
        }

        if (type == 3) {
            data.writeInt32(traitSystemID);
        }

        for (var i = 0; i < entries.size(); ++i) {
            entries.get(i).writeCreate(data, owner, receiver);
        }

        data.writeBits(name.getValue().getBytes().length, 9);
        data.writeString(name);
        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 12);

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                if (!ignoreChangesMask) {
                    entries.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(entries.size(), data);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                for (var i = 0; i < entries.size(); ++i) {
                    if (entries.hasChanged(i) || ignoreChangesMask) {
                        entries.get(i).writeUpdate(data, ignoreChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(2)) {
                data.writeInt32(ID);
            }
        }

        if (changesMask.get(4)) {
            if (changesMask.get(5)) {
                data.writeInt32(type);
            }

            if (changesMask.get(6)) {
                if (type == 2) {
                    data.writeInt32(skillLineID);
                }
            }

            if (changesMask.get(7)) {
                if (type == 1) {
                    data.writeInt32(chrSpecializationID);
                }
            }
        }

        if (changesMask.get(8)) {
            if (changesMask.get(9)) {
                if (type == 1) {
                    data.writeInt32(combatConfigFlags);
                }
            }

            if (changesMask.get(10)) {
                if (type == 1) {
                    data.writeInt32(localIdentifier);
                }
            }

            if (changesMask.get(11)) {
                if (type == 3) {
                    data.writeInt32(traitSystemID);
                }
            }
        }

        if (changesMask.get(0)) {
            if (changesMask.get(3)) {
                data.writeBits(name.getValue().getBytes().length, 9);
                data.writeString(name);
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(entries);
        clearChangesMask(ID);
        clearChangesMask(name);
        clearChangesMask(type);
        clearChangesMask(skillLineID);
        clearChangesMask(chrSpecializationID);
        clearChangesMask(combatConfigFlags);
        clearChangesMask(localIdentifier);
        clearChangesMask(traitSystemID);
        getChangesMask().resetAll();
    }
}
