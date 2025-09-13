package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ServerPacket;

public class LFGQueueStatus extends ServerPacket {
    public Rideticket ticket;
    public int slot;
    public int avgWaitTimeMe;
    public int avgWaitTime;
    public int[] avgWaitTimeByRole = new int[3];
    public byte[] lastNeeded = new byte[3];
    public int queuedTime;

    public LFGQueueStatus() {
        super(ServerOpcode.LfgQueueStatus);
    }

    @Override
    public void write() {
        ticket.write(this);

        this.writeInt32(slot);
        this.writeInt32(avgWaitTimeMe);
        this.writeInt32(avgWaitTime);

        for (var i = 0; i < 3; i++) {
            this.writeInt32(AvgWaitTimeByRole[i]);
            this.writeInt8(LastNeeded[i]);
        }

        this.writeInt32(queuedTime);
    }
}
