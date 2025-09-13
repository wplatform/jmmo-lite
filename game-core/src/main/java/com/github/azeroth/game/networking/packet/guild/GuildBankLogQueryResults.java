package com.github.azeroth.game.networking.packet.guild;


import java.util.ArrayList;


public class GuildBankLogQueryResults extends ServerPacket {
    public int tab;
    public ArrayList<GuildBankLogentry> entry;
    public Long weeklyBonusMoney = null;

    public GuildBankLogQueryResults() {
        super(ServerOpcode.GuildBankLogQueryResults);
        entry = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeInt32(tab);
        this.writeInt32(entry.size());
        this.writeBit(weeklyBonusMoney != null);
        this.flushBits();

        for (var logEntry : entry) {
            this.writeGuid(logEntry.playerGUID);
            this.writeInt32(logEntry.timeOffset);
            this.writeInt8(logEntry.entryType);

            this.writeBit(logEntry.money != null);
            this.writeBit(logEntry.itemID != null);
            this.writeBit(logEntry.count != null);
            this.writeBit(logEntry.otherTab != null);
            this.flushBits();

            if (logEntry.money != null) {
                this.writeInt64(logEntry.money.longValue());
            }

            if (logEntry.itemID != null) {
                this.writeInt32(logEntry.itemID.intValue());
            }

            if (logEntry.count != null) {
                this.writeInt32(logEntry.count.intValue());
            }

            if (logEntry.otherTab != null) {
                this.writeInt8(logEntry.otherTab.byteValue());
            }
        }

        if (weeklyBonusMoney != null) {
            this.writeInt64(weeklyBonusMoney.longValue());
        }
    }
}
