package com.github.azeroth.game.networking.packet.guild;


import java.util.ArrayList;


public class GuildNewsPkt extends ServerPacket {
    public ArrayList<GuildNewsEvent> newsEvents;

    public GuildNewsPkt() {
        super(ServerOpcode.GuildNews);
        newsEvents = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeInt32(newsEvents.size());

        for (var newsEvent : newsEvents) {
            newsEvent.write(this);
        }
    }
}
