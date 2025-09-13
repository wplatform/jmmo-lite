package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PlayerNotFoundAppend implements IChannelAppender {
    private final String playerName;

    public PlayerNotFoundAppend() {
    }

    public PlayerNotFoundAppend(String playerName) {
        playerName = playerName;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PlayerNotFoundNotice;
    }

    public void append(ChannelNotify data) {
        data.sender = playerName;
    }

    public PlayerNotFoundAppend clone() {
        PlayerNotFoundAppend varCopy = new PlayerNotFoundAppend();

        varCopy.playerName = this.playerName;

        return varCopy;
    }
}
