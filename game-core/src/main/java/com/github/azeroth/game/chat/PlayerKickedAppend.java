package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.ChannelNotify;

final class PlayerKickedAppend implements IChannelAppender {
    private final ObjectGuid kicker;
    private final ObjectGuid kickee;

    public PlayerKickedAppend() {
    }

    public PlayerKickedAppend(ObjectGuid kicker, ObjectGuid kickee) {
        kicker = kicker;
        kickee = kickee;
    }

    public ChatNotify getNotificationType() {
        return ChatNotify.PlayerKickedNotice;
    }

    public void append(ChannelNotify data) {
        data.senderGuid = kicker;
        data.targetGuid = kickee;
    }

    public PlayerKickedAppend clone() {
        PlayerKickedAppend varCopy = new PlayerKickedAppend();

        varCopy.kicker = this.kicker;
        varCopy.kickee = this.kickee;

        return varCopy;
    }
}
