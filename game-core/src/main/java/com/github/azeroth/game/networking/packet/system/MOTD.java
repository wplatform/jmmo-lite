package com.github.azeroth.game.networking.packet.system;


import java.util.ArrayList;


public class MOTD extends ServerPacket {
    public ArrayList<String> text;

    public MOTD() {
        super(ServerOpcode.Motd);
    }

    @Override
    public void write() {
        this.writeBits(text.size(), 4);
        this.flushBits();

        for (var line : text) {
            this.writeBits(line.getBytes().length, 7);
            this.flushBits();
            this.writeString(line);
        }
    }
}
