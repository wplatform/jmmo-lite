package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ServerPacket;

public class DailyQuestsReset extends ServerPacket {
    public int count;

    public DailyQuestsReset() {
        super(ServerOpcode.DailyQuestsReset);
    }

    @Override
    public void write() {
        this.writeInt32(count);
    }
}
