package com.github.azeroth.game.networking.packet.ticket;


public class ComplaintResult extends ServerPacket {
    public SupportSpamType complaintType = SupportSpamType.values()[0];
    public byte result;

    public ComplaintResult() {
        super(ServerOpcode.ComplaintResult);
    }

    @Override
    public void write() {
        this.writeInt32((int) complaintType.getValue());
        this.writeInt8(result);
    }
}
