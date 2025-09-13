package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class WrongFactionAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.WrongFactionNotice;
    }

    public void append(ChannelNotify data) {
    }
}
