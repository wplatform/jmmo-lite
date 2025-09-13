package com.github.azeroth.game.networking.packet.mail;


import java.util.ArrayList;


public class MailListResult extends ServerPacket {
    public int totalNumRecords;
    public ArrayList<MailListEntry> mails = new ArrayList<>();

    public MailListResult() {
        super(ServerOpcode.MailListResult);
    }

    @Override
    public void write() {
        this.writeInt32(mails.size());
        this.writeInt32(totalNumRecords);

        mails.forEach(p -> p.write(this));
    }
}
