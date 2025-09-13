package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class YouLeftAppend implements IChannelAppender {
    private final Channel channel;

    public YouLeftAppend() {
    }

    public YouLeftAppend(Channel channel) {
        channel = channel;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.YouLeftNotice;
    }

    public void append(ChannelNotify data) {
        data.chatChannelID = (int) channel.getChannelId();
    }

    public YouLeftAppend clone() {
        YouLeftAppend varCopy = new YouLeftAppend();

        varCopy.channel = this.channel;

        return varCopy;
    }
}
