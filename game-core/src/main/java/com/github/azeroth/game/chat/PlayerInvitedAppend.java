package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PlayerInvitedAppend implements IChannelAppender {
    private final String playerName;

    public PlayerInvitedAppend() {
    }

    public PlayerInvitedAppend(String playerName) {
        playerName = playerName;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PlayerInvitedNotice;
    }

    public void append(ChannelNotify data) {
        data.sender = playerName;
    }

    public PlayerInvitedAppend clone() {
        PlayerInvitedAppend varCopy = new PlayerInvitedAppend();

        varCopy.playerName = this.playerName;

        return varCopy;
    }
}
