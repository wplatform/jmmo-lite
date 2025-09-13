package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.WorldPacket;

public class LFGBlackListSlot {
    public int slot;
    public int reason;
    public int subReason1;
    public int subReason2;
    public int softLock;

    public LFGBlackListSlot(int slot, int reason, int subReason1, int subReason2, int softLock) {
        slot = slot;
        reason = reason;
        subReason1 = subReason1;
        subReason2 = subReason2;
        softLock = softLock;
    }

    public final void write(WorldPacket data) {
        data.writeInt32(slot);
        data.writeInt32(reason);
        data.writeInt32(subReason1);
        data.writeInt32(subReason2);
        data.writeInt32(softLock);
    }
}
