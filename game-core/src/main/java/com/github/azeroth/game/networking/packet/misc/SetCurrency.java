package com.github.azeroth.game.networking.packet.misc;


import java.util.ArrayList;


public class SetCurrency extends ServerPacket {
    public int type;
    public int quantity;
    public CurrencyGainflags flags = CurrencyGainFlags.values()[0];
    public ArrayList<UiEventToast> toasts = new ArrayList<>();
    public Integer weeklyQuantity = null;
    public Integer trackedQuantity = null;
    public Integer maxQuantity = null;
    public Integer totalEarned = null;
    public Integer quantityChange = null;
    public CurrencyGainSource quantityGainSource = null;
    public CurrencyDestroyReason quantityLostSource = null;
    public Integer firstCraftOperationID = null;
    public Long lastSpendTime = null;
    public boolean suppressChatLog;

    public SetCurrency() {
        super(ServerOpcode.SetCurrency);
    }

    @Override
    public void write() {
        this.writeInt32(type);
        this.writeInt32(quantity);
        this.writeInt32((int) flags.getValue());
        this.writeInt32(toasts.size());

        for (var toast : toasts) {
            toast.write(this);
        }

        this.writeBit(weeklyQuantity != null);
        this.writeBit(trackedQuantity != null);
        this.writeBit(maxQuantity != null);
        this.writeBit(totalEarned != null);
        this.writeBit(suppressChatLog);
        this.writeBit(quantityChange != null);
        this.writeBit(quantityGainSource != null);
        this.writeBit(quantityLostSource != null);
        this.writeBit(firstCraftOperationID != null);
        this.writeBit(lastSpendTime != null);
        this.flushBits();

        if (weeklyQuantity != null) {
            this.writeInt32(weeklyQuantity.intValue());
        }

        if (trackedQuantity != null) {
            this.writeInt32(trackedQuantity.intValue());
        }

        if (maxQuantity != null) {
            this.writeInt32(maxQuantity.intValue());
        }

        if (totalEarned != null) {
            this.writeInt32(totalEarned.intValue());
        }

        if (quantityChange != null) {
            this.writeInt32(quantityChange.intValue());
        }

        if (quantityGainSource != null) {
            this.writeInt32((int) quantityGainSource);
        }

        if (quantityLostSource != null) {
            this.writeInt32((int) quantityLostSource);
        }

        if (firstCraftOperationID != null) {
            this.writeInt32(firstCraftOperationID.intValue());
        }

        if (lastSpendTime != null) {
            this.writeInt64(lastSpendTime.longValue());
        }
    }
}
