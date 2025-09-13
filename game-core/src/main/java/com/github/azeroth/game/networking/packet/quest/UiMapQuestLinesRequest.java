package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.WorldPacket;

public class UiMapQuestLinesRequest extends ClientPacket {
    public int uiMapID;

    public UiMapQuestLinesRequest(WorldPacket worldPacket) {
        super(worldPacket);
    }

    @Override
    public void read() {
        uiMapID = this.readInt32();
    }
}
