package com.github.azeroth.game.calendar;


import com.github.azeroth.game.mail.MailDraft;
import com.github.azeroth.game.mail.MailReceiver;
import com.github.azeroth.game.mail.MailSender;

import java.util.ArrayList;
import java.util.Objects;

public class CalendarManager {
    private final ArrayList<CalendarEvent> events;
    private final MultiMap<Long, CalendarInvite> invites;
    private final ArrayList<Long> freeEventIds = new ArrayList<>();
    private final ArrayList<Long> freeInviteIds = new ArrayList<>();
    private long maxEventId;
    private long maxInviteId;

    private CalendarManager() {
        events = new ArrayList<>();
        invites = new MultiMap<Long, CalendarInvite>();
    }

    public final void loadFromDB() {
        var oldMSTime = System.currentTimeMillis();

        int count = 0;
        maxEventId = 0;
        maxInviteId = 0;

        //                                              0        1      2      3            4          5          6     7      8
        var result = DB.characters.query("SELECT eventID, owner, title, description, eventType, textureID, date, flags, LockDate FROM calendar_events");

        if (!result.isEmpty()) {
            do {
                var eventID = result.<Long>Read(0);
                var ownerGUID = ObjectGuid.create(HighGuid.Player, result.<Long>Read(1));
                var title = result.<String>Read(2);
                var description = result.<String>Read(3);
                var type = CalendarEventType.forValue(result.<Byte>Read(4));
                var textureID = result.<Integer>Read(5);
                var date = result.<Long>Read(6);
                var flags = CalendarFlags.forValue(result.<Integer>Read(7));
                var lockDate = result.<Long>Read(8);
                long guildID = 0;

                if (flags.hasFlag(CalendarFlags.GuildEvent) || flags.hasFlag(CalendarFlags.WithoutInvites)) {
                    guildID = global.getCharacterCacheStorage().getCharacterGuildIdByGuid(ownerGUID);
                }

                CalendarEvent calendarEvent = new CalendarEvent(eventID, ownerGUID, guildID, type, textureID, date, flags, title, description, lockDate);
                events.add(calendarEvent);

                maxEventId = Math.max(maxEventId, eventID);

                ++count;
            } while (result.NextRow());
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s calendar events in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
        count = 0;
        oldMSTime = System.currentTimeMillis();

        //                                    0         1        2        3       4       5             6               7
        result = DB.characters.query("SELECT inviteID, eventID, Invitee, sender, status, responseTime, ModerationRank, Note FROM calendar_invites");

        if (!result.isEmpty()) {
            do {
                var inviteId = result.<Long>Read(0);
                var eventId = result.<Long>Read(1);
                var invitee = ObjectGuid.create(HighGuid.Player, result.<Long>Read(2));
                var senderGUID = ObjectGuid.create(HighGuid.Player, result.<Long>Read(3));
                var status = CalendarInviteStatus.forValue(result.<Byte>Read(4));
                var responseTime = result.<Long>Read(5);
                var rank = CalendarModerationRank.forValue(result.<Byte>Read(6));
                var note = result.<String>Read(7);

                CalendarInvite invite = new CalendarInvite(inviteId, eventId, invitee, senderGUID, responseTime, status, rank, note);
                invites.add(eventId, invite);

                maxInviteId = Math.max(maxInviteId, inviteId);

                ++count;
            } while (result.NextRow());
        }

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s calendar invites in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));

        for (long i = 1; i < maxEventId; ++i) {
            if (getEvent(i) == null) {
                freeEventIds.add(i);
            }
        }

        for (long i = 1; i < maxInviteId; ++i) {
            if (getInvite(i) == null) {
                freeInviteIds.add(i);
            }
        }
    }

    public final void addEvent(CalendarEvent calendarEvent, CalendarSendEventType sendType) {
        events.add(calendarEvent);
        updateEvent(calendarEvent);
        sendCalendarEvent(calendarEvent.getOwnerGuid(), calendarEvent, sendType);
    }


    public final void addInvite(CalendarEvent calendarEvent, CalendarInvite invite) {
        addInvite(calendarEvent, invite, null);
    }

    public final void addInvite(CalendarEvent calendarEvent, CalendarInvite invite, SQLTransaction trans) {
        if (!calendarEvent.isGuildAnnouncement() && ObjectGuid.opNotEquals(calendarEvent.getOwnerGuid(), invite.getInviteeGuid())) {
            sendCalendarEventInvite(invite);
        }

        if (!calendarEvent.isGuildEvent() || Objects.equals(invite.getInviteeGuid(), calendarEvent.getOwnerGuid())) {
            sendCalendarEventInviteAlert(calendarEvent, invite);
        }

        if (!calendarEvent.isGuildAnnouncement()) {
            invites.add(invite.getEventId(), invite);
            updateInvite(invite, trans);
        }
    }

    public final void removeEvent(long eventId, ObjectGuid remover) {
        var calendarEvent = getEvent(eventId);

        if (calendarEvent == null) {
            sendCalendarCommandResult(remover, CalendarError.EventInvalid);

            return;
        }

        removeEvent(calendarEvent, remover);
    }

    public final void removeInvite(long inviteId, long eventId, ObjectGuid remover) {
        var calendarEvent = getEvent(eventId);

        if (calendarEvent == null) {
            return;
        }

        CalendarInvite calendarInvite = null;

        for (var invite : invites.get(eventId)) {
            if (invite.getInviteId() == inviteId) {
                calendarInvite = invite;

                break;
            }
        }

        if (calendarInvite == null) {
            return;
        }

        SQLTransaction trans = new SQLTransaction();
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CALENDAR_INVITE);
        stmt.AddValue(0, calendarInvite.getInviteId());
        trans.append(stmt);
        DB.characters.CommitTransaction(trans);

        if (!calendarEvent.isGuildEvent()) {
            sendCalendarEventInviteRemoveAlert(calendarInvite.getInviteeGuid(), calendarEvent, CalendarInviteStatus.removed);
        }

        sendCalendarEventInviteRemove(calendarEvent, calendarInvite, (int) calendarEvent.getFlags().getValue());

        // we need to find out how to use CALENDAR_INVITE_REMOVED_MAIL_SUBJECT to force client to display different mail
        //if (itr._invitee != remover)
        //    MailDraft(calendarEvent.buildCalendarMailSubject(remover), calendarEvent.buildCalendarMailBody())
        //        .sendMailTo(trans, MailReceiver(itr.GetInvitee()), calendarEvent, MAIL_CHECK_MASK_COPIED);

        invites.remove(eventId, calendarInvite);
    }

    public final void updateEvent(CalendarEvent calendarEvent) {
        SQLTransaction trans = new SQLTransaction();
        var stmt = DB.characters.GetPreparedStatement(CharStatements.REP_CALENDAR_EVENT);
        stmt.AddValue(0, calendarEvent.getEventId());
        stmt.AddValue(1, calendarEvent.getOwnerGuid().getCounter());
        stmt.AddValue(2, calendarEvent.getTitle());
        stmt.AddValue(3, calendarEvent.getDescription());
        stmt.AddValue(4, (byte) calendarEvent.getEventType().getValue());
        stmt.AddValue(5, calendarEvent.getTextureId());
        stmt.AddValue(6, calendarEvent.getDate());
        stmt.AddValue(7, (int) calendarEvent.getFlags().getValue());
        stmt.AddValue(8, calendarEvent.getLockDate());
        trans.append(stmt);
        DB.characters.CommitTransaction(trans);
    }


    public final void updateInvite(CalendarInvite invite) {
        updateInvite(invite, null);
    }

    public final void updateInvite(CalendarInvite invite, SQLTransaction trans) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.REP_CALENDAR_INVITE);
        stmt.AddValue(0, invite.getInviteId());
        stmt.AddValue(1, invite.getEventId());
        stmt.AddValue(2, invite.getInviteeGuid().getCounter());
        stmt.AddValue(3, invite.getSenderGuid().getCounter());
        stmt.AddValue(4, (byte) invite.getStatus().getValue());
        stmt.AddValue(5, invite.getResponseTime());
        stmt.AddValue(6, (byte) invite.getRank().getValue());
        stmt.AddValue(7, invite.getNote());
        DB.characters.ExecuteOrAppend(trans, stmt);
    }

    public final void removeAllPlayerEventsAndInvites(ObjectGuid guid) {
        for (var calendarEvent : events) {
            if (Objects.equals(calendarEvent.getOwnerGuid(), guid)) {
                removeEvent(calendarEvent.getEventId(), ObjectGuid.Empty); // don't send mail if removing a character
            }
        }

        var playerInvites = getPlayerInvites(guid);

        for (var calendarInvite : playerInvites) {
            removeInvite(calendarInvite.getInviteId(), calendarInvite.getEventId(), guid);
        }
    }

    public final void removePlayerGuildEventsAndSignups(ObjectGuid guid, long guildId) {
        for (var calendarEvent : events) {
            if (Objects.equals(calendarEvent.getOwnerGuid(), guid) && (calendarEvent.isGuildEvent() || calendarEvent.isGuildAnnouncement())) {
                removeEvent(calendarEvent.getEventId(), guid);
            }
        }

        var playerInvites = getPlayerInvites(guid);

        for (var playerCalendarEvent : playerInvites) {
            var calendarEvent = getEvent(playerCalendarEvent.getEventId());

            if (calendarEvent != null) {
                if (calendarEvent.isGuildEvent() && calendarEvent.getGuildId() == guildId) {
                    removeInvite(playerCalendarEvent.getInviteId(), playerCalendarEvent.getEventId(), guid);
                }
            }
        }
    }

    public final CalendarEvent getEvent(long eventId) {
        for (var calendarEvent : events) {
            if (calendarEvent.getEventId() == eventId) {
                return calendarEvent;
            }
        }

        Log.outDebug(LogFilter.Calendar, "CalendarMgr:GetEvent: {0} not found!", eventId);

        return null;
    }

    public final CalendarInvite getInvite(long inviteId) {
        for (var calendarEvent : invites.VALUES) {
            if (calendarEvent.inviteId == inviteId) {
                return calendarEvent;
            }
        }

        Log.outDebug(LogFilter.Calendar, "CalendarMgr:GetInvite: {0} not found!", inviteId);

        return null;
    }

    public final long getFreeEventId() {
        if (freeEventIds.isEmpty()) {
            return ++maxEventId;
        }

        var eventId = freeEventIds.FirstOrDefault();
        freeEventIds.remove(0);

        return eventId;
    }

    public final void freeInviteId(long id) {
        if (id == maxInviteId) {
            --_maxInviteId;
        } else {
            freeInviteIds.add(id);
        }
    }

    public final long getFreeInviteId() {
        if (freeInviteIds.isEmpty()) {
            return ++maxInviteId;
        }

        var inviteId = freeInviteIds.FirstOrDefault();
        freeInviteIds.remove(0);

        return inviteId;
    }

    public final void deleteOldEvents() {
        var oldEventsTime = gameTime.GetGameTime() - SharedConst.CalendarOldEventsDeletionTime;

        for (var calendarEvent : events) {
            if (calendarEvent.getDate() < oldEventsTime) {
                removeEvent(calendarEvent, ObjectGuid.Empty);
            }
        }
    }


    public final ArrayList<CalendarEvent> getEventsCreatedBy(ObjectGuid guid) {
        return getEventsCreatedBy(guid, false);
    }

    public final ArrayList<CalendarEvent> getEventsCreatedBy(ObjectGuid guid, boolean includeGuildEvents) {
        ArrayList<CalendarEvent> result = new ArrayList<>();

        for (var calendarEvent : events) {
            if (Objects.equals(calendarEvent.getOwnerGuid(), guid) && (includeGuildEvents || (!calendarEvent.isGuildEvent() && !calendarEvent.isGuildAnnouncement()))) {
                result.add(calendarEvent);
            }
        }

        return result;
    }

    public final ArrayList<CalendarEvent> getGuildEvents(long guildId) {
        ArrayList<CalendarEvent> result = new ArrayList<>();

        if (guildId == 0) {
            return result;
        }

        for (var calendarEvent : events) {
            if (calendarEvent.isGuildEvent() || calendarEvent.isGuildAnnouncement()) {
                if (calendarEvent.getGuildId() == guildId) {
                    result.add(calendarEvent);
                }
            }
        }

        return result;
    }

    public final ArrayList<CalendarEvent> getPlayerEvents(ObjectGuid guid) {
        ArrayList<CalendarEvent> events = new ArrayList<>();

        for (var pair : invites.KeyValueList) {
            if (Objects.equals(pair.value.inviteeGuid, guid)) {
                var event = getEvent(pair.key);

                if (event != null) // null check added as attempt to fix #11512
                {
                    events.add(event);
                }
            }
        }

        var player = global.getObjAccessor().findPlayer(guid);

        if ((player == null ? null : player.getGuildId()) != 0) {
            for (var calendarEvent : events) {
                if (calendarEvent.getGuildId() == player.getGuildId()) {
                    events.add(calendarEvent);
                }
            }
        }

        return events;
    }

    public final ArrayList<CalendarInvite> getEventInvites(long eventId) {
        return invites.get(eventId);
    }

    public final ArrayList<CalendarInvite> getPlayerInvites(ObjectGuid guid) {
        ArrayList<CalendarInvite> invites = new ArrayList<>();

        for (var calendarEvent : invites.VALUES) {
            if (Objects.equals(calendarEvent.inviteeGuid, guid)) {
                invites.add(calendarEvent);
            }
        }

        return invites;
    }

    public final int getPlayerNumPending(ObjectGuid guid) {
        var invites = getPlayerInvites(guid);

        int pendingNum = 0;

        for (var calendarEvent : invites) {
            switch (calendarEvent.getStatus()) {
                case Invited:
                case Tentative:
                case NotSignedUp:
                    ++pendingNum;

                    break;
                default:
                    break;
            }
        }

        return pendingNum;
    }

    public final void sendCalendarEventInvite(CalendarInvite invite) {
        var calendarEvent = getEvent(invite.getEventId());

        var invitee = invite.getInviteeGuid();
        var player = global.getObjAccessor().findPlayer(invitee);

        var level = player ? player.getLevel() : global.getCharacterCacheStorage().getCharacterLevelByGuid(invitee);

        CalendarInviteAdded packet = new CalendarInviteAdded();
        packet.eventID = calendarEvent != null ? calendarEvent.getEventId() : 0;
        packet.inviteGuid = invitee;
        packet.inviteID = calendarEvent != null ? invite.getInviteId() : 0;
        packet.level = (byte) level;
        packet.responseTime = invite.getResponseTime();
        packet.status = invite.getStatus();
        packet.type = (byte) (calendarEvent != null ? calendarEvent.isGuildEvent() ? 1 : 0 : 0); // Correct ?
        packet.clearPending = calendarEvent == null || !calendarEvent.isGuildEvent(); // Correct ?

        if (calendarEvent == null) // Pre-invite
        {
            player = global.getObjAccessor().findPlayer(invite.getSenderGuid());

            if (player) {
                player.sendPacket(packet);
            }
        } else {
            if (ObjectGuid.opNotEquals(calendarEvent.getOwnerGuid(), invite.getInviteeGuid())) // correct?
            {
                sendPacketToAllEventRelatives(packet, calendarEvent);
            }
        }
    }

    public final void sendCalendarEventUpdateAlert(CalendarEvent calendarEvent, long originalDate) {
        CalendarEventUpdatedAlert packet = new CalendarEventUpdatedAlert();
        packet.clearPending = true; // FIXME
        packet.date = calendarEvent.getDate();
        packet.description = calendarEvent.getDescription();
        packet.eventID = calendarEvent.getEventId();
        packet.eventName = calendarEvent.getTitle();
        packet.eventType = calendarEvent.getEventType();
        packet.flags = calendarEvent.getFlags();
        packet.lockDate = calendarEvent.getLockDate(); // Always 0 ?
        packet.originalDate = originalDate;
        packet.textureID = calendarEvent.getTextureId();

        sendPacketToAllEventRelatives(packet, calendarEvent);
    }

    public final void sendCalendarEventStatus(CalendarEvent calendarEvent, CalendarInvite invite) {
        CalendarInviteStatusPacket packet = new CalendarInviteStatusPacket();
        packet.clearPending = true; // FIXME
        packet.date = calendarEvent.getDate();
        packet.eventID = calendarEvent.getEventId();
        packet.flags = calendarEvent.getFlags();
        packet.inviteGuid = invite.getInviteeGuid();
        packet.responseTime = invite.getResponseTime();
        packet.status = invite.getStatus();

        sendPacketToAllEventRelatives(packet, calendarEvent);
    }

    public final void sendCalendarEventModeratorStatusAlert(CalendarEvent calendarEvent, CalendarInvite invite) {
        CalendarModeratorStatus packet = new CalendarModeratorStatus();
        packet.clearPending = true; // FIXME
        packet.eventID = calendarEvent.getEventId();
        packet.inviteGuid = invite.getInviteeGuid();
        packet.status = invite.getStatus();

        sendPacketToAllEventRelatives(packet, calendarEvent);
    }

    public final void sendCalendarEvent(ObjectGuid guid, CalendarEvent calendarEvent, CalendarSendEventType sendType) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (!player) {
            return;
        }

        var eventInviteeList = invites.get(calendarEvent.getEventId());

        CalendarSendEvent packet = new CalendarSendEvent();
        packet.date = calendarEvent.getDate();
        packet.description = calendarEvent.getDescription();
        packet.eventID = calendarEvent.getEventId();
        packet.eventName = calendarEvent.getTitle();
        packet.eventType = sendType;
        packet.flags = calendarEvent.getFlags();
        packet.getEventType = calendarEvent.getEventType();
        packet.lockDate = calendarEvent.getLockDate(); // Always 0 ?
        packet.ownerGuid = calendarEvent.getOwnerGuid();
        packet.textureID = calendarEvent.getTextureId();

        var guild = global.getGuildMgr().getGuildById(calendarEvent.getGuildId());
        packet.eventGuildID = (guild ? guild.getGUID() : ObjectGuid.Empty);

        for (var calendarInvite : eventInviteeList) {
            var inviteeGuid = calendarInvite.inviteeGuid;
            var invitee = global.getObjAccessor().findPlayer(inviteeGuid);

            var inviteeLevel = invitee ? invitee.getLevel() : global.getCharacterCacheStorage().getCharacterLevelByGuid(inviteeGuid);
            var inviteeGuildId = invitee ? invitee.getGuildId() : global.getCharacterCacheStorage().getCharacterGuildIdByGuid(inviteeGuid);

            CalendarEventInviteInfo inviteInfo = new CalendarEventInviteInfo();
            inviteInfo.guid = inviteeGuid;
            inviteInfo.level = (byte) inviteeLevel;
            inviteInfo.status = calendarInvite.status;
            inviteInfo.moderator = calendarInvite.rank;
            inviteInfo.inviteType = (byte) (calendarEvent.isGuildEvent() && calendarEvent.getGuildId() == inviteeGuildId ? 1 : 0);
            inviteInfo.inviteID = calendarInvite.inviteId;
            inviteInfo.responseTime = calendarInvite.responseTime;
            inviteInfo.notes = calendarInvite.note;

            packet.invites.add(inviteInfo);
        }

        player.sendPacket(packet);
    }

    public final void sendCalendarClearPendingAction(ObjectGuid guid) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            player.sendPacket(new CalendarClearPendingAction());
        }
    }


    public final void sendCalendarCommandResult(ObjectGuid guid, CalendarError err) {
        sendCalendarCommandResult(guid, err, null);
    }

    public final void sendCalendarCommandResult(ObjectGuid guid, CalendarError err, String param) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            CalendarCommandResult packet = new CalendarCommandResult();
            packet.command = 1; // FIXME
            packet.result = err;

            switch (err) {
                case OtherInvitesExceeded:
                case AlreadyInvitedToEventS:
                case IgnoringYouS:
                    packet.name = param;

                    break;
            }

            player.sendPacket(packet);
        }
    }

    private void removeEvent(CalendarEvent calendarEvent, ObjectGuid remover) {
        if (calendarEvent == null) {
            sendCalendarCommandResult(remover, CalendarError.EventInvalid);

            return;
        }

        sendCalendarEventRemovedAlert(calendarEvent);

        SQLTransaction trans = new SQLTransaction();
        PreparedStatement stmt;
        MailDraft mail = new MailDraft(calendarEvent.buildCalendarMailSubject(remover), calendarEvent.buildCalendarMailBody());

        var eventInvites = invites.get(calendarEvent.getEventId());

        for (var i = 0; i < eventInvites.count; ++i) {
            var invite = eventInvites[i];
            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CALENDAR_INVITE);
            stmt.AddValue(0, invite.inviteId);
            trans.append(stmt);

            // guild events only? check invite status here?
            // When an event is deleted, all invited (accepted/declined? - verify) guildies are notified via in-game mail. (wowwiki)
            if (!remover.isEmpty() && ObjectGuid.opNotEquals(invite.inviteeGuid, remover)) {
                mail.sendMailTo(trans, new MailReceiver(invite.inviteeGuid.counter), new MailSender(calendarEvent), MailCheckMask.Copied);
            }
        }

        invites.remove(calendarEvent.getEventId());

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CALENDAR_EVENT);
        stmt.AddValue(0, calendarEvent.getEventId());
        trans.append(stmt);
        DB.characters.CommitTransaction(trans);

        events.remove(calendarEvent);
    }

    private void freeEventId(long id) {
        if (id == maxEventId) {
            --_maxEventId;
        } else {
            freeEventIds.add(id);
        }
    }

    private void sendCalendarEventRemovedAlert(CalendarEvent calendarEvent) {
        CalendarEventRemovedAlert packet = new CalendarEventRemovedAlert();
        packet.clearPending = true; // FIXME
        packet.date = calendarEvent.getDate();
        packet.eventID = calendarEvent.getEventId();

        sendPacketToAllEventRelatives(packet, calendarEvent);
    }

    private void sendCalendarEventInviteRemove(CalendarEvent calendarEvent, CalendarInvite invite, int flags) {
        CalendarInviteRemoved packet = new CalendarInviteRemoved();
        packet.clearPending = true; // FIXME
        packet.eventID = calendarEvent.getEventId();
        packet.flags = flags;
        packet.inviteGuid = invite.getInviteeGuid();

        sendPacketToAllEventRelatives(packet, calendarEvent);
    }

    private void sendCalendarEventInviteAlert(CalendarEvent calendarEvent, CalendarInvite invite) {
        CalendarInviteAlert packet = new CalendarInviteAlert();
        packet.date = calendarEvent.getDate();
        packet.eventID = calendarEvent.getEventId();
        packet.eventName = calendarEvent.getTitle();
        packet.eventType = calendarEvent.getEventType();
        packet.flags = calendarEvent.getFlags();
        packet.inviteID = invite.getInviteId();
        packet.invitedByGuid = invite.getSenderGuid();
        packet.moderatorStatus = invite.getRank();
        packet.ownerGuid = calendarEvent.getOwnerGuid();
        packet.status = invite.getStatus();
        packet.textureID = calendarEvent.getTextureId();

        var guild = global.getGuildMgr().getGuildById(calendarEvent.getGuildId());
        packet.eventGuildID = guild ? guild.getGUID() : ObjectGuid.Empty;

        if (calendarEvent.isGuildEvent() || calendarEvent.isGuildAnnouncement()) {
            guild = global.getGuildMgr().getGuildById(calendarEvent.getGuildId());

            if (guild) {
                guild.broadcastPacket(packet);
            }
        } else {
            var player = global.getObjAccessor().findPlayer(invite.getInviteeGuid());

            if (player) {
                player.sendPacket(packet);
            }
        }
    }

    private void sendCalendarEventInviteRemoveAlert(ObjectGuid guid, CalendarEvent calendarEvent, CalendarInviteStatus status) {
        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            CalendarInviteRemovedAlert packet = new CalendarInviteRemovedAlert();
            packet.date = calendarEvent.getDate();
            packet.eventID = calendarEvent.getEventId();
            packet.flags = calendarEvent.getFlags();
            packet.status = status;

            player.sendPacket(packet);
        }
    }

    private void sendPacketToAllEventRelatives(ServerPacket packet, CalendarEvent calendarEvent) {
        // Send packet to all guild members
        if (calendarEvent.isGuildEvent() || calendarEvent.isGuildAnnouncement()) {
            var guild = global.getGuildMgr().getGuildById(calendarEvent.getGuildId());

            if (guild) {
                guild.broadcastPacket(packet);
            }
        }

        // Send packet to all invitees if event is non-guild, in other case only to non-guild invitees (packet was broadcasted for them)
        var invites = invites.get(calendarEvent.getEventId());

        for (var playerCalendarEvent : invites) {
            var player = global.getObjAccessor().findPlayer(playerCalendarEvent.inviteeGuid);

            if (player) {
                if (!calendarEvent.isGuildEvent() || player.getGuildId() != calendarEvent.getGuildId()) {
                    player.sendPacket(packet);
                }
            }
        }
    }
}
