package com.github.azeroth.game.networking.packet.system;

import com.github.azeroth.game.networking.WorldPacket;

public final class EuropaTicketConfig {
    public boolean ticketsEnabled;
    public boolean bugsEnabled;
    public boolean complaintsEnabled;
    public boolean suggestionsEnabled;

    public SavedThrottleObjectState throttleState = new SavedThrottleObjectState();

    public void write(WorldPacket data) {
        data.writeBit(ticketsEnabled);
        data.writeBit(bugsEnabled);
        data.writeBit(complaintsEnabled);
        data.writeBit(suggestionsEnabled);

        throttleState.write(data);
    }
}
