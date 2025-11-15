package com.github.azeroth.game.networking.packet.system;

import com.github.azeroth.game.networking.WorldPacket;

public final class SavedThrottleObjectState {
    public int maxTries;
    public int perMilliseconds;
    public int tryCount;
    public int lastResetTimeBeforeNow;

    public void write(WorldPacket data) {
        data.writeInt32(maxTries);
        data.writeInt32(perMilliseconds);
        data.writeInt32(tryCount);
        data.writeInt32(lastResetTimeBeforeNow);
    }
}
