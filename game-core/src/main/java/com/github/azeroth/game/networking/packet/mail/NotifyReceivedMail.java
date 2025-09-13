package com.github.azeroth.game.networking.packet.mail;


public class NotifyReceivedMail extends ServerPacket {
    public float delay = 0.0f;

    public NotifyReceivedMail() {
        super(ServerOpcode.NotifyReceivedMail);
    }

    @Override
    public void write() {
        this.writeFloat(delay);
    }
}
