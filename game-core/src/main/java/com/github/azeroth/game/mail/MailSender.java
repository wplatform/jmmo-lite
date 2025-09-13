package com.github.azeroth.game.mail;


import com.github.azeroth.game.blackmarket.BlackMarketEntry;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import game.*;

public class MailSender {
    private final MailMessageType m_messageType;
    private final long m_senderId; // player low guid or other object entry
    private final MailStationery m_stationery;


    public MailSender(MailMessageType messageType, long sender_guidlow_or_entry) {
        this(messageType, sender_guidlow_or_entry, MailStationery.Default);
    }

    public MailSender(MailMessageType messageType, long sender_guidlow_or_entry, MailStationery stationery) {
        m_messageType = messageType;
        m_senderId = sender_guidlow_or_entry;
        m_stationery = stationery;
    }


    public MailSender(WorldObject sender) {
        this(sender, MailStationery.Default);
    }

    public MailSender(WorldObject sender, MailStationery stationery) {
        m_stationery = stationery;

        switch (sender.getObjectTypeId()) {
            case Unit:
                m_messageType = MailMessageType.CREATURE;
                m_senderId = sender.getEntry();

                break;
            case GameObject:
                m_messageType = MailMessageType.GAMEOBJECT;
                m_senderId = sender.getEntry();

                break;
            case Player:
                m_messageType = MailMessageType.NORMAL;
                m_senderId = sender.getGUID().getCounter();

                break;
            default:
                m_messageType = MailMessageType.NORMAL;
                m_senderId = 0; // will show mail from not existed player
                Log.outError(LogFilter.Server, "MailSender:MailSender - Mail have unexpected sender typeid ({0})", sender.getObjectTypeId());

                break;
        }
    }

    public MailSender(CalendarEvent sender) {
        m_messageType = MailMessageType.Calendar;
        m_senderId = (int) sender.getEventId();
        m_stationery = MailStationery.Default;
    }

    public MailSender(AuctionHouseObject sender) {
        m_messageType = MailMessageType.Auction;
        m_senderId = sender.getAuctionHouseId();
        m_stationery = MailStationery.Auction;
    }

    public MailSender(BlackMarketEntry sender) {
        m_messageType = MailMessageType.Blackmarket;
        m_senderId = sender.getTemplate().sellerNPC;
        m_stationery = MailStationery.Auction;
    }

    public MailSender(Player sender) {
        m_messageType = MailMessageType.NORMAL;
        m_stationery = sender.isGameMaster() ? MailStationery.Gm : MailStationery.Default;
        m_senderId = sender.getGUID().getCounter();
    }

    public MailSender(int senderEntry) {
        m_messageType = MailMessageType.CREATURE;
        m_senderId = senderEntry;
        m_stationery = MailStationery.Default;
    }

    public final MailMessageType getMailMessageType() {
        return m_messageType;
    }

    public final long getSenderId() {
        return m_senderId;
    }

    public final MailStationery getStationery() {
        return m_stationery;
    }
}
