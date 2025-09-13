package com.github.azeroth.game.networking.packet.system;

import com.github.azeroth.game.networking.WorldPacket;

public final class EuropaTicketConfig {
    public boolean ticketsEnabled;
    public boolean bugsEnabled;
    public boolean complaintsEnabled;
    public boolean suggestionsEnabled;

    public savedThrottleObjectState throttleState = new savedThrottleObjectState();

    public void write(WorldPacket data) {
        data.writeBit(ticketsEnabled);
        data.writeBit(bugsEnabled);
        data.writeBit(complaintsEnabled);
        data.writeBit(suggestionsEnabled);

        throttleState.write(data);
    }

    public EuropaTicketConfig clone() {
        EuropaTicketConfig varCopy = new EuropaTicketConfig();

        varCopy.ticketsEnabled = this.ticketsEnabled;
        varCopy.bugsEnabled = this.bugsEnabled;
        varCopy.complaintsEnabled = this.complaintsEnabled;
        varCopy.suggestionsEnabled = this.suggestionsEnabled;
        varCopy.throttleState = this.throttleState;

        return varCopy;
    }
}
