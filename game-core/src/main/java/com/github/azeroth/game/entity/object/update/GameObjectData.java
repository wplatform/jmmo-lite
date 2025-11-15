package com.github.azeroth.game.entity.object.update;


import com.badlogic.gdx.math.Quaternion;
import com.github.azeroth.game.domain.gobject.QuaternionData;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;
import java.util.List;


public final class GameObjectData extends UpdateMaskObject {
    @ChangeMark(blockBit = 0, bit = 1)
    private final List<Integer> stateWorldEffectIDs = UpdateFields.newList("stateWorldEffectIDs", this);
    @ChangeMark(blockBit = 0, bit = 2, type = FieldType.DYNAMIC)
    private final List<Integer> enableDoodadSets = UpdateFields.newList("enableDoodadSets", this);
    @ChangeMark(blockBit = 0, bit = 3, type = FieldType.DYNAMIC)
    private final List<Integer> worldEffects = UpdateFields.newList("worldEffects", this);
    @ChangeMark(blockBit = 0, bit = 4)
    private int displayID;
    @ChangeMark(blockBit = 0, bit = 5)
    private int spellVisualID;
    @ChangeMark(blockBit = 0, bit = 6)
    private int stateSpellVisualID;
    @ChangeMark(blockBit = 0, bit = 7)
    private int spawnTrackingStateAnimID;
    @ChangeMark(blockBit = 0, bit = 8)
    private int spawnTrackingStateAnimKitID;
    @ChangeMark(blockBit = 0, bit = 9)
    private ObjectGuid createdBy;
    @ChangeMark(blockBit = 0, bit = 10)
    private ObjectGuid guildGUID;
    @ChangeMark(blockBit = 0, bit = 11)
    private int flags;
    @ChangeMark(blockBit = 0, bit = 12)
    private QuaternionData parentRotation;
    @ChangeMark(blockBit = 0, bit = 13)
    private int factionTemplate;
    @ChangeMark(blockBit = 0, bit = 14)
    private int level;
    @ChangeMark(blockBit = 0, bit = 15)
    private int state;
    @ChangeMark(blockBit = 0, bit = 16)
    private int typeID;
    @ChangeMark(blockBit = 0, bit = 17)
    private int percentHealth;
    @ChangeMark(blockBit = 0, bit = 18)
    private int artKit;
    @ChangeMark(blockBit = 0, bit = 19)
    private int customParam;

    public GameObjectData() {
        super(20);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, GameObject owner, Player receiver) {
        data.writeInt32(displayID);
        data.writeInt32(spellVisualID);
        data.writeInt32(stateSpellVisualID);
        data.writeInt32(spawnTrackingStateAnimID);
        data.writeInt32(spawnTrackingStateAnimKitID);
        data.writeInt32(((ArrayList<Integer>) stateWorldEffectIDs).size());
        data.writeInt32(stateWorldEffectsQuestObjectiveID);

        for (var i = 0; i < ((ArrayList<Integer>) stateWorldEffectIDs).size(); ++i) {
            data.writeInt32(((ArrayList<Integer>) stateWorldEffectIDs).get(i));
        }

        data.writeGuid(createdBy);
        data.writeGuid(guildGUID);
        data.writeInt32(getViewerGameObjectFlags(this, owner, receiver));
        Quaternion rotation = parentRotation;
        data.writeFloat(rotation.X);
        data.writeFloat(rotation.Y);
        data.writeFloat(rotation.Z);
        data.writeFloat(rotation.W);
        data.writeInt32(factionTemplate);
        data.writeInt8(getViewerGOState(this, owner, receiver));
        data.writeInt8(typeID);
        data.writeInt8(percentHealth);
        data.writeInt32(artKit);
        data.writeInt32(enableDoodadSets.size());
        data.writeInt32(customParam);
        data.writeInt32(level);
        data.writeInt32(animGroupInstance);
        data.writeInt32(uiWidgetItemID);
        data.writeInt32(uiWidgetItemQuality);
        data.writeInt32(uiWidgetItemUnknown1000);
        data.writeInt32(worldEffects.size());

        for (var i = 0; i < enableDoodadSets.size(); ++i) {
            data.writeInt32(enableDoodadSets.get(i));
        }

        for (var i = 0; i < worldEffects.size(); ++i) {
            data.writeInt32(worldEffects.get(i));
        }
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, GameObject owner, Player receiver) {
        writeUpdate(data, getChangesMask(), false, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, GameObject owner, Player receiver) {
        data.writeBits(changesMask.getBlock(0), 25);

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeBits(((ArrayList<Integer>) stateWorldEffectIDs).size(), 32);

                for (var i = 0; i < ((ArrayList<Integer>) stateWorldEffectIDs).size(); ++i) {
                    data.writeInt32(((ArrayList<Integer>) stateWorldEffectIDs).get(i));
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(2)) {
                if (!ignoreNestedChangesMask) {
                    enableDoodadSets.writeUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(enableDoodadSets.size(), data);
                }
            }

            if (changesMask.get(3)) {
                if (!ignoreNestedChangesMask) {
                    worldEffects.writeUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(worldEffects.size(), data);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(2)) {
                for (var i = 0; i < enableDoodadSets.size(); ++i) {
                    if (enableDoodadSets.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(enableDoodadSets.get(i));
                    }
                }
            }

            if (changesMask.get(3)) {
                for (var i = 0; i < worldEffects.size(); ++i) {
                    if (worldEffects.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(worldEffects.get(i));
                    }
                }
            }

            if (changesMask.get(4)) {
                data.writeInt32(displayID);
            }

            if (changesMask.get(5)) {
                data.writeInt32(spellVisualID);
            }

            if (changesMask.get(6)) {
                data.writeInt32(stateSpellVisualID);
            }

            if (changesMask.get(7)) {
                data.writeInt32(spawnTrackingStateAnimID);
            }

            if (changesMask.get(8)) {
                data.writeInt32(spawnTrackingStateAnimKitID);
            }

            if (changesMask.get(9)) {
                data.writeInt32(stateWorldEffectsQuestObjectiveID);
            }

            if (changesMask.get(10)) {
                data.writeGuid(createdBy);
            }

            if (changesMask.get(11)) {
                data.writeGuid(guildGUID);
            }

            if (changesMask.get(12)) {
                data.writeInt32(getViewerGameObjectFlags(this, owner, receiver));
            }

            if (changesMask.get(13)) {
                data.writeFloat(((Quaternion) parentRotation).X);
                data.writeFloat(((Quaternion) parentRotation).Y);
                data.writeFloat(((Quaternion) parentRotation).Z);
                data.writeFloat(((Quaternion) parentRotation).W);
            }

            if (changesMask.get(14)) {
                data.writeInt32(factionTemplate);
            }

            if (changesMask.get(15)) {
                data.writeInt8(getViewerGOState(this, owner, receiver));
            }

            if (changesMask.get(16)) {
                data.writeInt8(typeID);
            }

            if (changesMask.get(17)) {
                data.writeInt8(percentHealth);
            }

            if (changesMask.get(18)) {
                data.writeInt32(artKit);
            }

            if (changesMask.get(19)) {
                data.writeInt32(customParam);
            }

            if (changesMask.get(20)) {
                data.writeInt32(level);
            }

            if (changesMask.get(21)) {
                data.writeInt32(animGroupInstance);
            }

            if (changesMask.get(22)) {
                data.writeInt32(uiWidgetItemID);
            }

            if (changesMask.get(23)) {
                data.writeInt32(uiWidgetItemQuality);
            }

            if (changesMask.get(24)) {
                data.writeInt32(uiWidgetItemUnknown1000);
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(stateWorldEffectIDs);
        clearChangesMask(enableDoodadSets);
        clearChangesMask(worldEffects);
        clearChangesMask(displayID);
        clearChangesMask(spellVisualID);
        clearChangesMask(stateSpellVisualID);
        clearChangesMask(spawnTrackingStateAnimID);
        clearChangesMask(spawnTrackingStateAnimKitID);
        clearChangesMask(stateWorldEffectsQuestObjectiveID);
        clearChangesMask(createdBy);
        clearChangesMask(guildGUID);
        clearChangesMask(flags);
        clearChangesMask(parentRotation);
        clearChangesMask(factionTemplate);
        clearChangesMask(state);
        clearChangesMask(typeID);
        clearChangesMask(percentHealth);
        clearChangesMask(artKit);
        clearChangesMask(customParam);
        clearChangesMask(level);
        clearChangesMask(animGroupInstance);
        clearChangesMask(uiWidgetItemID);
        clearChangesMask(uiWidgetItemQuality);
        clearChangesMask(uiWidgetItemUnknown1000);
        getChangesMask().resetAll();
    }

    private int getViewerGameObjectFlags(GameObjectData gameObjectData, GameObject gameObject, Player receiver) {
        int flags = gameObjectData.flags;

        if (gameObject.getGoType() == GameObjectTypes.chest) {
            if (gameObject.getTemplate().chest.usegrouplootrules != 0 && !gameObject.isLootAllowedFor(receiver)) {
                flags |= (int) (GameObjectFlags.locked.getValue() | GameObjectFlags.NotSelectable.getValue());
            }
        }

        return flags;
    }

    private byte getViewerGOState(GameObjectData gameObjectData, GameObject gameObject, Player receiver) {
        return (byte) gameObject.getGoStateFor(receiver.getGUID()).getValue();
    }
}
