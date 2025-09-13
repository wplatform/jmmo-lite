package com.github.azeroth.game.networking.packet.social;


import com.github.azeroth.game.entity.player.SocialFlag;

import java.util.ArrayList;


public class ContactList extends ServerPacket {
    public ArrayList<ContactInfo> contacts;
    public SocialFlag flags = SocialFlag.values()[0];

    public ContactList() {
        super(ServerOpcode.ContactList);
        contacts = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeInt32((int) flags.getValue());
        this.writeBits(contacts.size(), 8);
        this.flushBits();

        for (var contact : contacts) {
            contact.write(this);
        }
    }
}
