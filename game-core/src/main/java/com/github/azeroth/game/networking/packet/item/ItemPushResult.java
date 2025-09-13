package com.github.azeroth.game.networking.packet.item;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemPushResult extends ServerPacket {
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;
    public byte slot;
    public int slotInBag;
    public itemInstance item;
    public int questLogItemID; // Item ID used for updating quest progress
    // only set if different than real ID (similar to CreatureTemplate.killCredit)
    public int quantity;
    public int quantityInInventory;
    public int dungeonEncounterID;
    public int battlePetSpeciesID;
    public int battlePetBreedID;
    public int battlePetBreedQuality;
    public int battlePetLevel;
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;
    public ArrayList<UiEventToast> toasts = new ArrayList<>();
    public craftingData craftingData;
    public Integer firstCraftOperationID = null;
    public boolean pushed;
    public DisplayType displayText = displayType.values()[0];
    public boolean created;
    public boolean isBonusRoll;
    public boolean isEncounterLoot;
    public ItemPushResult() {
        super(ServerOpcode.ItemPushResult);
    }

    @Override
    public void write() {
        this.writeGuid(playerGUID);
        this.writeInt8(slot);
        this.writeInt32(slotInBag);
        this.writeInt32(questLogItemID);
        this.writeInt32(quantity);
        this.writeInt32(quantityInInventory);
        this.writeInt32(dungeonEncounterID);
        this.writeInt32(battlePetSpeciesID);
        this.writeInt32(battlePetBreedID);
        this.writeInt32(battlePetBreedQuality);
        this.writeInt32(battlePetLevel);
        this.writeGuid(itemGUID);
        this.writeInt32(toasts.size());

        for (var uiEventToast : toasts) {
            uiEventToast.write(this);
        }

        this.writeBit(pushed);
        this.writeBit(created);
        this.writeBits((int) displayText.getValue(), 3);
        this.writeBit(isBonusRoll);
        this.writeBit(isEncounterLoot);
        this.writeBit(craftingData != null);
        this.writeBit(firstCraftOperationID != null);
        this.flushBits();

        item.write(this);

        if (firstCraftOperationID != null) {
            this.writeInt32(firstCraftOperationID.intValue());
        }

        if (craftingData != null) {
            craftingData.write(this);
        }
    }

    public enum DisplayType {
        hidden(0),
        NORMAL(1),
        EncounterLoot(2);

        public static final int SIZE = Integer.SIZE;
        private static HashMap<Integer, displayType> mappings;
        private int intValue;

        private displayType(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static HashMap<Integer, displayType> getMappings() {
            if (mappings == null) {
                synchronized (displayType.class) {
                    if (mappings == null) {
                        mappings = new HashMap<Integer, displayType>();
                    }
                }
            }
            return mappings;
        }

        public static DisplayType forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }
}
