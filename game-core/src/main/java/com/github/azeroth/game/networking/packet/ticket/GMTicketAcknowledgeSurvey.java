package com.github.azeroth.game.networking.packet.ticket;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GMTicketAcknowledgeSurvey extends ClientPacket {
    private int caseID;

    public GMTicketAcknowledgeSurvey(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        caseID = this.readInt32();
    }
}
