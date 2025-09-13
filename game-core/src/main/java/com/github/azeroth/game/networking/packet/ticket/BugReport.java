package com.github.azeroth.game.networking.packet.ticket;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BugReport extends ClientPacket {
    public int type;
    public String text;
    public String diagInfo;

    public BugReport(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        type = this.readBit();
        var diagLen = this.<Integer>readBit(12);
        var textLen = this.<Integer>readBit(10);
        diagInfo = this.readString(diagLen);
        text = this.readString(textLen);
    }
}
