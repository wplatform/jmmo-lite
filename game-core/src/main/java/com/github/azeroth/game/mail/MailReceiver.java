package com.github.azeroth.game.mail;


import com.github.azeroth.game.entity.player.Player;

public class MailReceiver {
    private final Player m_receiver;
    private final long m_receiver_lowguid;

    public MailReceiver(long receiver_lowguid) {
        m_receiver = null;
        m_receiver_lowguid = receiver_lowguid;
    }

    public MailReceiver(Player receiver) {
        m_receiver = receiver;
        m_receiver_lowguid = receiver.getGUID().getCounter();
    }

    public MailReceiver(Player receiver, long receiver_lowguid) {
        m_receiver = receiver;
        m_receiver_lowguid = receiver_lowguid;
    }

    public MailReceiver(Player receiver, ObjectGuid receiverGuid) {
        m_receiver = receiver;
        m_receiver_lowguid = receiverGuid.getCounter();
    }

    public final Player getPlayer() {
        return m_receiver;
    }

    public final long getPlayerGUIDLow() {
        return m_receiver_lowguid;
    }
}
