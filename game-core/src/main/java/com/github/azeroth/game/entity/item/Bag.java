package com.github.azeroth.game.entity.item;


import com.github.azeroth.game.entity.ContainerData;
import com.github.azeroth.game.entity.UpdateMask;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

import java.io.IOException;


public class Bag extends Item {
    private final ContainerData m_containerData;
    private Item[] m_bagslot = new Item[36];

    public bag() {
        setObjectTypeMask(TypeMask.forValue(getObjectTypeMask().getValue() | TypeMask.Container.getValue()));
        setObjectTypeId(TypeId.Container);

        m_containerData = new containerData();
    }

    @Override
    public void close() throws IOException {
        for (byte i = 0; i < ItemConst.MaxBagSize; ++i) {
            var item = m_bagslot[i];

            if (item) {
                if (item.isInWorld()) {
                    Log.outFatal(LogFilter.PlayerItems, "Item {0} (slot {1}, bag slot {2}) in bag {3} (slot {4}, bag slot {5}, m_bagslot {6}) is to be deleted but is still in world.", item.getEntry(), item.getSlot(), item.getBagSlot(), getEntry(), getSlot(), getBagSlot(), i);

                    item.removeFromWorld();
                }

                m_bagslot[i].close();
            }
        }

        super.close();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();

        for (int i = 0; i < getBagSize(); ++i) {
            if (m_bagslot[i] != null) {
                m_bagslot[i].addToWorld();
            }
        }
    }

    @Override
    public void removeFromWorld() {
        for (int i = 0; i < getBagSize(); ++i) {
            if (m_bagslot[i] != null) {
                m_bagslot[i].removeFromWorld();
            }
        }

        super.removeFromWorld();
    }

    @Override
    public boolean create(long guidlow, int itemid, ItemContext context, Player owner) {
        var itemProto = global.getObjectMgr().getItemTemplate(itemid);

        if (itemProto == null || itemProto.getContainerSlots() > ItemConst.MaxBagSize) {
            return false;
        }

        create(ObjectGuid.create(HighGuid.Item, guidlow));

        setBonusData(new bonusData(itemProto));

        setEntry(itemid);
        setObjectScale(1.0f);

        if (owner) {
            setOwnerGUID(owner.getGUID());
            setContainedIn(owner.getGUID());
        }

        setUpdateFieldValue(getValues().modifyValue(getItemData()).modifyValue(getItemData().maxDurability), itemProto.getMaxDurability());
        setDurability(itemProto.getMaxDurability());
        setCount(1);
        setContext(context);

        // Setting the number of Slots the Container has
        setBagSize(itemProto.getContainerSlots());

        // Cleaning 20 slots
        for (byte i = 0; i < ItemConst.MaxBagSize; ++i) {
            setSlot(i, ObjectGuid.Empty);
        }

        m_bagslot = new Item[ItemConst.MaxBagSize];

        return true;
    }

    @Override
    public boolean loadFromDB(long guid, ObjectGuid owner_guid, SQLFields fields, int entry) {
        if (!super.loadFromDB(guid, owner_guid, fields, entry)) {
            return false;
        }

        var itemProto = getTemplate(); // checked in item.LoadFromDB
        setBagSize(itemProto.getContainerSlots());

        // cleanup bag content related item value fields (its will be filled correctly from `character_inventory`)
        for (byte i = 0; i < ItemConst.MaxBagSize; ++i) {
            setSlot(i, ObjectGuid.Empty);
            m_bagslot[i] = null;
        }

        return true;
    }

    @Override
    public void deleteFromDB(SQLTransaction trans) {
        for (byte i = 0; i < ItemConst.MaxBagSize; ++i) {
            if (m_bagslot[i] != null) {
                m_bagslot[i].deleteFromDB(trans);
            }
        }

        super.deleteFromDB(trans);
    }

    public final int getFreeSlots() {
        int slots = 0;

        for (int i = 0; i < getBagSize(); ++i) {
            if (m_bagslot[i] == null) {
                ++slots;
            }
        }

        return slots;
    }

    public final void removeItem(byte slot, boolean update) {
        if (m_bagslot[slot] != null) {
            m_bagslot[slot].setContainer(null);
        }

        m_bagslot[slot] = null;
        setSlot(slot, ObjectGuid.Empty);
    }

    public final void storeItem(byte slot, Item pItem, boolean update) {
        if (pItem != null && ObjectGuid.opNotEquals(pItem.getGUID(), getGUID())) {
            m_bagslot[slot] = pItem;
            setSlot(slot, pItem.getGUID());
            pItem.setContainedIn(getGUID());
            pItem.setOwnerGUID(getOwnerGUID());
            pItem.setContainer(this);
            pItem.setSlot(slot);
        }
    }

    @Override
    public void buildCreateUpdateBlockForPlayer(UpdateData data, Player target) {
        super.buildCreateUpdateBlockForPlayer(data, target);

        for (var i = 0; i < getBagSize(); ++i) {
            if (m_bagslot[i] != null) {
                m_bagslot[i].buildCreateUpdateBlockForPlayer(data, target);
            }
        }
    }

    @Override
    public void buildValuesCreate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt8((byte) flags.getValue());
        getObjectData().writeCreate(buffer, flags, this, target);
        getItemData().writeCreate(buffer, flags, this, target);
        m_containerData.writeCreate(buffer, flags, this, target);

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void buildValuesUpdate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt32(getValues().getChangedObjectTypeMask());

        if (getValues().hasChanged(TypeId.object)) {
            getObjectData().writeUpdate(buffer, flags, this, target);
        }

        if (getValues().hasChanged(TypeId.item)) {
            getItemData().writeUpdate(buffer, flags, this, target);
        }

        if (getValues().hasChanged(TypeId.Container)) {
            m_containerData.writeUpdate(buffer, flags, this, target);
        }

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void clearUpdateMask(boolean remove) {
        getValues().clearChangesMask(m_containerData);
        super.clearUpdateMask(remove);
    }

    public final boolean isEmpty() {
        for (var i = 0; i < getBagSize(); ++i) {
            if (m_bagslot[i] != null) {
                return false;
            }
        }

        return true;
    }

    public final Item getItemByPos(byte slot) {
        if (slot < getBagSize()) {
            return m_bagslot[slot];
        }

        return null;
    }

    public final int getBagSize() {
        return m_containerData.numSlots;
    }

    private void setBagSize(int numSlots) {
        setUpdateFieldValue(getValues().modifyValue(m_containerData).modifyValue(m_containerData.numSlots), numSlots);
    }

    private void buildValuesUpdateForPlayerWithMask(UpdateData data, UpdateMask requestedObjectMask, UpdateMask requestedItemMask, UpdateMask requestedContainerMask, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        UpdateMask valuesMask = new UpdateMask(getObjectTypeId().max.getValue());

        if (requestedObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().object.getValue());
        }

        getItemData().filterDisallowedFieldsMaskForFlag(requestedItemMask, flags);

        if (requestedItemMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().item.getValue());
        }

        if (requestedContainerMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().Container.getValue());
        }

        WorldPacket buffer = new WorldPacket();
        buffer.writeInt32(valuesMask.getBlock(0));

        if (valuesMask.get(getObjectTypeId().object.getValue())) {
            getObjectData().writeUpdate(buffer, requestedObjectMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().item.getValue())) {
            getItemData().writeUpdate(buffer, requestedItemMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().Container.getValue())) {
            m_containerData.writeUpdate(buffer, requestedContainerMask, true, this, target);
        }

        WorldPacket buffer1 = new WorldPacket();
        buffer1.writeInt8((byte) UpdateType.VALUES.getValue());
        buffer1.writeGuid(getGUID());
        buffer1.writeInt32(buffer.getSize());
        buffer1.writeBytes(buffer.getByteBuf());

        data.addUpdateBlock(buffer1);
    }

    private byte getSlotByItemGUID(ObjectGuid guid) {
        for (byte i = 0; i < getBagSize(); ++i) {
            if (m_bagslot[i] != null) {
                if (Objects.equals(m_bagslot[i].getGUID(), guid)) {
                    return i;
                }
            }
        }

        return ItemConst.NullSlot;
    }

    private void setSlot(int slot, ObjectGuid guid) {

        setUpdateFieldValue(ref getValues().modifyValue(m_containerData).modifyValue(m_containerData.slots, slot), guid);
    }

    private static class ValuesUpdateForPlayerWithMaskSender implements IDoWork<Player> {
        private final Bag owner;
        private final objectFieldData objectMask = new objectFieldData();
        private final com.github.azeroth.game.entity.item.itemData itemMask = new itemData();
        private final containerData containerMask = new containerData();

        public ValuesUpdateForPlayerWithMaskSender(Bag owner) {
            owner = owner;
        }

        public final void invoke(Player player) {
            UpdateData udata = new UpdateData(owner.getLocation().getMapId());

            owner.buildValuesUpdateForPlayerWithMask(udata, objectMask.getUpdateMask(), itemMask.getUpdateMask(), containerMask.getUpdateMask(), player);

            com.github.azeroth.game.networking.packet.UpdateObject packet;
            tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject> tempOut_packet = new tangible.OutObject<com.github.azeroth.game.networking.packet.UpdateObject>();
            udata.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }
}
