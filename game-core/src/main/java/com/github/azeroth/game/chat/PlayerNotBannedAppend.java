package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PlayerNotBannedAppend implements IChannelAppender {
    private final String playerName;

    public PlayerNotBannedAppend() {
    }

    public PlayerNotBannedAppend(String playerName) {
        playerName = playerName;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PlayerNotBannedNotice;
    }

    public void append(ChannelNotify data) {
        data.sender = playerName;
    }

    public PlayerNotBannedAppend clone() {
        PlayerNotBannedAppend varCopy = new PlayerNotBannedAppend();

        varCopy.playerName = this.playerName;

        return varCopy;
    }
}
