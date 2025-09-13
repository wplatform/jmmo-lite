package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.networking.WorldPacket;

public final class QuestChoiceItem {
    public lootItemType lootItemType = Framework.Constants.lootItemType.values()[0];
    public itemInstance item;

    public int quantity;

    public void read(WorldPacket data) {
        data.resetBitPos();
        lootItemType = lootItemType.forValue(data.readBit(2));
        item = new itemInstance();
        item.read(data);
        quantity = data.readUInt32();
    }

    public void write(WorldPacket data) {
        data.writeBits((byte) lootItemType.getValue(), 2);
        item.write(data);
        data.writeInt32(quantity);
    }

    public QuestChoiceItem clone() {
        QuestChoiceItem varCopy = new questChoiceItem();

        varCopy.lootItemType = this.lootItemType;
        varCopy.item = this.item;
        varCopy.quantity = this.quantity;

        return varCopy;
    }
}
