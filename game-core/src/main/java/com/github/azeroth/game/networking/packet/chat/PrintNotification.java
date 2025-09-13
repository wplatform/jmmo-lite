package com.github.azeroth.game.networking.packet.chat;


public class PrintNotification extends ServerPacket {
    public String notifyText;

    public PrintNotification(String notifyText) {
        super(ServerOpcode.PrintNotification);
        notifyText = notifyText;
    }

    @Override
    public void write() {
        this.writeBits(notifyText.getBytes().length, 12);
        this.writeString(notifyText);
    }
}
