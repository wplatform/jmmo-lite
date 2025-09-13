package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class VasPurchase {
    private ArrayList<Integer> itemIDs = new ArrayList<>();
    private ObjectGuid playerGuid = ObjectGuid.EMPTY;
    private long unkLong = 0;
    private int unkInt = 0;
    private int unkInt2 = 0;

    public final ArrayList<Integer> getItemIDs() {
        return itemIDs;
    }

    public final void setItemIDs(ArrayList<Integer> value) {
        itemIDs = value;
    }

    public final ObjectGuid getPlayerGuid() {
        return playerGuid;
    }

    public final void setPlayerGuid(ObjectGuid value) {
        playerGuid = value;
    }

    public final long getUnkLong() {
        return unkLong;
    }

    public final void setUnkLong(long value) {
        unkLong = value;
    }

    public final int getUnkInt() {
        return unkInt;
    }

    public final void setUnkInt(int value) {
        unkInt = value;
    }

    public final int getUnkInt2() {
        return unkInt2;
    }

    public final void setUnkInt2(int value) {
        unkInt2 = value;
    }

    public final void write(WorldPacket this) {
        this.write(getPlayerGuid());
        this.write(getUnkInt());
        this.write(getUnkInt2());
        this.write(getUnkLong());
        this.writeBits(getItemIDs().size(), 2);
        this.flushBits();

        for (var itemId : getItemIDs()) {
            this.write(itemId);
        }
    }
}
