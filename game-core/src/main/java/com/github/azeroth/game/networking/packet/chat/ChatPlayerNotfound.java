package com.github.azeroth.game.networking.packet.chat;


import com.github.azeroth.game.networking.ServerPacket;

public class ChatPlayerNotfound extends ServerPacket {
    private final String name;

    public ChatPlayerNotfound(String name) {
        super(ServerOpcode.ChatPlayerNotfound);
        name = name;
    }

    @Override
    public void write() {
        this.writeBits(name.getBytes().length, 9);
        this.writeString(name);
    }
}
