package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class NotInLFGAppend implements IChannelAppender {
    public ChatNotify getNotificationType() {
        return ChatNotify.NotInLfgNotice;
    }

    public void append(ChannelNotify data) {
    }
}
