package com.github.azeroth.game.networking.packet.quest;

public final class QuestCurrency {

    public int currencyID;
    public int amount;

    public QuestCurrency(int currencyID) {
        this(currencyID, 0);
    }

    public QuestCurrency() {
    }
    public QuestCurrency(int currencyID, int amount) {
        this.currencyID = currencyID;
        this.amount = amount;
    }

}
