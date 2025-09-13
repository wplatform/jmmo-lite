package com.github.azeroth.game.networking.packet.lfg;

public final class LfgPlayerQuestRewardItem {
    public int itemID;
    public int quantity;

    public LfgPlayerQuestRewardItem() {
    }
    public LfgPlayerQuestRewardItem(int itemId, int quantity) {
        itemID = itemId;
        quantity = quantity;
    }

    public LfgPlayerQuestRewardItem clone() {
        LfgPlayerQuestRewardItem varCopy = new LfgPlayerQuestRewardItem();

        varCopy.itemID = this.itemID;
        varCopy.quantity = this.quantity;

        return varCopy;
    }
}
