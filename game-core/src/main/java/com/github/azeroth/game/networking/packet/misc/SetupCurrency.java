package com.github.azeroth.game.networking.packet.misc;


import java.util.ArrayList;


public class SetupCurrency extends ServerPacket {
    public ArrayList<record> data = new ArrayList<>();

    public SetupCurrency() {
        super(ServerOpcode.SetupCurrency);
    }

    @Override
    public void write() {
        this.writeInt32(data.size());

        for (var data : data) {
            this.writeInt32(data.type);
            this.writeInt32(data.quantity);

            this.writeBit(data.weeklyQuantity != null);
            this.writeBit(data.maxWeeklyQuantity != null);
            this.writeBit(data.trackedQuantity != null);
            this.writeBit(data.maxQuantity != null);
            this.writeBit(data.totalEarned != null);
            this.writeBit(data.lastSpendTime != null);
            this.writeBits(data.flags, 5);
            this.flushBits();

            if (data.weeklyQuantity != null) {
                this.writeInt32(data.weeklyQuantity.intValue());
            }

            if (data.maxWeeklyQuantity != null) {
                this.writeInt32(data.maxWeeklyQuantity.intValue());
            }

            if (data.trackedQuantity != null) {
                this.writeInt32(data.trackedQuantity.intValue());
            }

            if (data.maxQuantity != null) {
                this.writeInt32(data.maxQuantity.intValue());
            }

            if (data.totalEarned != null) {
                this.writeInt32(data.totalEarned.intValue());
            }

            if (data.lastSpendTime != null) {
                this.writeInt64(data.lastSpendTime.longValue());
            }
        }
    }

    public final static class Record {
        public int type;
        public int quantity;
        public Integer weeklyQuantity = null; // Currency count obtained this Week.
        public Integer maxWeeklyQuantity = null; // Weekly Currency cap.
        public Integer trackedQuantity = null;
        public Integer maxQuantity = null;
        public Integer totalEarned = null;
        public Long lastSpendTime = null;
        public byte flags;

        public Record clone() {
            Record varCopy = new record();

            varCopy.type = this.type;
            varCopy.quantity = this.quantity;
            varCopy.weeklyQuantity = this.weeklyQuantity;
            varCopy.maxWeeklyQuantity = this.maxWeeklyQuantity;
            varCopy.trackedQuantity = this.trackedQuantity;
            varCopy.maxQuantity = this.maxQuantity;
            varCopy.totalEarned = this.totalEarned;
            varCopy.lastSpendTime = this.lastSpendTime;
            varCopy.flags = this.flags;

            return varCopy;
        }
    }
}
