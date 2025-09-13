package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PlayerInviteBannedAppend implements IChannelAppender {
    private final String playerName;

    public PlayerInviteBannedAppend() {
    }

    public PlayerInviteBannedAppend(String playerName) {
        playerName = playerName;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PlayerInviteBannedNotice;
    }

    public void append(ChannelNotify data) {
        data.sender = playerName;
    }

    public PlayerInviteBannedAppend clone() {
        PlayerInviteBannedAppend varCopy = new PlayerInviteBannedAppend();

        varCopy.playerName = this.playerName;

        return varCopy;
    }
}
