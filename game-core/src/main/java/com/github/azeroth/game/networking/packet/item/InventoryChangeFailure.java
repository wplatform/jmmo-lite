package com.github.azeroth.game.networking.packet.item;


public class InventoryChangeFailure extends ServerPacket {
    public InventoryResult bagResult = InventoryResult.values()[0];
    public byte containerBSlot;
    public ObjectGuid srcContainer = ObjectGuid.EMPTY;
    public ObjectGuid dstContainer = ObjectGuid.EMPTY;
    public int srcSlot;
    public int limitCategory;
    public int level;
    public ObjectGuid[] item = new ObjectGuid[2];

    public InventoryChangeFailure() {
        super(ServerOpcode.InventoryChangeFailure);
    }

    @Override
    public void write() {
        this.writeInt8((byte) bagResult.getValue());
        this.writeGuid(Item[0]);
        this.writeGuid(Item[1]);
        this.writeInt8(containerBSlot); // bag type subclass, used with EQUIP_ERR_EVENT_AUTOEQUIP_BIND_CONFIRM and EQUIP_ERR_WRONG_BAG_TYPE_2

        switch (bagResult) {
            case CantEquipLevelI:
            case PurchaseLevelTooLow:
                this.writeInt32(level);

                break;
            case EventAutoequipBindConfirm:
                this.writeGuid(srcContainer);
                this.writeInt32(srcSlot);
                this.writeGuid(dstContainer);

                break;
            case ItemMaxLimitCategoryCountExceededIs:
            case ItemMaxLimitCategorySocketedExceededIs:
            case ItemMaxLimitCategoryEquippedExceededIs:
                this.writeInt32(limitCategory);

                break;
        }
    }
}
