package com.github.azeroth.game.networking.packet.mail;


public class MailCommandResult extends ServerPacket {
    public long mailID;
    public int command;
    public int errorCode;
    public int bagResult;
    public long attachID;
    public int qtyInInventory;

    public MailCommandResult() {
        super(ServerOpcode.MailCommandResult);
    }

    @Override
    public void write() {
        this.writeInt64(mailID);
        this.writeInt32(command);
        this.writeInt32(errorCode);
        this.writeInt32(bagResult);
        this.writeInt64(attachID);
        this.writeInt32(qtyInInventory);
    }
}
