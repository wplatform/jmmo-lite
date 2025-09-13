package com.github.azeroth.game.networking.packet.lfg;

public final class LfgPlayerQuestRewardCurrency {
    public int currencyID;
    public int quantity;

    public LfgPlayerQuestRewardCurrency() {
    }
    public LfgPlayerQuestRewardCurrency(int currencyId, int quantity) {
        currencyID = currencyId;
        quantity = quantity;
    }

    public LfgPlayerQuestRewardCurrency clone() {
        LfgPlayerQuestRewardCurrency varCopy = new LfgPlayerQuestRewardCurrency();

        varCopy.currencyID = this.currencyID;
        varCopy.quantity = this.quantity;

        return varCopy;
    }
}
