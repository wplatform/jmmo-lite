package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class YouJoinedAppend implements IChannelAppender {
    private final Channel channel;

    public YouJoinedAppend() {
    }

    public YouJoinedAppend(Channel channel) {
        channel = channel;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.YouJoinedNotice;
    }

    public void append(ChannelNotify data) {
        data.chatChannelID = (int) channel.getChannelId();
    }

    public YouJoinedAppend clone() {
        YouJoinedAppend varCopy = new YouJoinedAppend();

        varCopy.channel = this.channel;

        return varCopy;
    }
}
