package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.networking.WorldPacket;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

public sealed abstract class UpdateMaskObject permits ActivePlayerData, AreaTriggerData, ArenaCooldown,
        BitVector, BitVectors, CompletedProject, ContainerData, ConversationData, CorpseData, CraftingOrder,
        CraftingOrderData, CraftingOrderItem, DynamicObjectData, GameObjectData, ItemData, ObjectData, PVPInfo,
        PlayerData, QuestLog, QuestSession, ReplayedQuest, ResearchHistory, RestInfo, ScaleCurve, SceneObjectData,
        SelectedAzeriteEssences, SkillInfo, SocketedGem, StableInfo, StablePetInfo, TraitConfig, UnitData, VendorData,
        VisibleItem, VisualAnim, ZonePlayerForcedReaction, BankTabSettings, DeclinedNames, CustomTabardInfo {



    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    protected final UpdateMask changesMask;
    protected final Map<String, ChangeMark> fieldChangeMarks;


    protected UpdateMaskObject(int changeMask) {
        this.changesMask = new UpdateMask(changeMask);
        this.fieldChangeMarks = UpdateFields.FIELD_CHANGE_MARKS_BY_CLASS.get(getClass());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }


    public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        ChangeMark changeMark = fieldChangeMarks.get(propertyName);
        switch (changeMark.type()) {
            case ARRAY -> {
                int firstElementBit = changeMark.firstElementBit();
                if (firstElementBit >= 0) {
                    if (newValue instanceof UpdateMaskObject) {
                        changesMask.set(firstElementBit + index);
                    } else {
                        changesMask.set(firstElementBit);
                    }
                }
            }
            case DYNAMIC -> {
                int bit = changeMark.bit();
                if (bit >= 0) {
                    changesMask.set(bit);
                }
            }

            case OBJECT -> {
                int bit = changeMark.bit();
                if (bit >= 0) {
                    changesMask.set(bit);
                }
            }
        }
        changeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public void firePropertyChange(PropertyChangeEvent event) {
        changeSupport.firePropertyChange(event);
    }


    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public abstract void clearChangesMask();


    private final void fireArrayMarkChanged(Class<?> elementType, int bit, int firstElementBit, int index) {
        changesMask.set(bit);
        if (firstElementBit >= 0) {
            if (UpdateMaskObject.class.isAssignableFrom(elementType)) {
                changesMask.set(firstElementBit + index);
            } else {
                changesMask.set(firstElementBit);
            }
        }
    }


    public final void writeCompleteDynamicFieldUpdateMask(int size, WorldPacket data) {
        writeCompleteDynamicFieldUpdateMask(size, data, 32);
    }

    public final void writeCompleteDynamicFieldUpdateMask(int size, WorldPacket data, int bitsForSize) {
        data.writeBits(size, bitsForSize);

        if (size > 32) {
            if (data.hasUnfinishedBitPack()) {
                for (var block = 0; block < size / 32; ++block) {
                    data.writeBits(0xFFFFFFFF, 32);
                }
            } else {
                for (var block = 0; block < size / 32; ++block) {
                    data.writeInt32(0xFFFFFFFF);
                }
            }
        } else if (size == 32) {
            data.writeBits(0xFFFFFFFF, 32);

            return;
        }

        if ((size % 32) != 0) {
            data.writeBits(0xFFFFFFFF, size % 32);
        }
    }



}
