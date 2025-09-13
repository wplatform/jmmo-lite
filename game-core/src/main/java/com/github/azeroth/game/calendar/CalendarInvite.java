package com.github.azeroth.game.calendar;


public class CalendarInvite {
    private long inviteId;
    private long eventId;
    private ObjectGuid inviteeGuid = ObjectGuid.EMPTY;
    private ObjectGuid senderGuid = ObjectGuid.EMPTY;
    private long responseTime;
    private CalendarInvitestatus status = CalendarInviteStatus.values()[0];
    private CalendarModerationrank rank = CalendarModerationRank.values()[0];
    private String note;

    public CalendarInvite() {
        setInviteId(1);
        setResponseTime(0);
        setStatus(CalendarInviteStatus.Invited);
        setRank(CalendarModerationRank.player);
        setNote("");
    }

    public CalendarInvite(CalendarInvite calendarInvite, long inviteId, long eventId) {
        setInviteId(inviteId);
        setEventId(eventId);
        setInviteeGuid(calendarInvite.getInviteeGuid());
        setSenderGuid(calendarInvite.getSenderGuid());
        setResponseTime(calendarInvite.getResponseTime());
        setStatus(calendarInvite.getStatus());
        setRank(calendarInvite.getRank());
        setNote(calendarInvite.getNote());
    }

    public CalendarInvite(long inviteId, long eventId, ObjectGuid invitee, ObjectGuid senderGUID, long responseTime, CalendarInviteStatus status, CalendarModerationRank rank, String note) {
        setInviteId(inviteId);
        setEventId(eventId);
        setInviteeGuid(invitee);
        setSenderGuid(senderGUID);
        setResponseTime(responseTime);

        setStatus(status);
        setRank(rank);
        setNote(note);
    }

    public final long getInviteId() {
        return inviteId;
    }

    public final void setInviteId(long value) {
        inviteId = value;
    }

    public final long getEventId() {
        return eventId;
    }

    public final void setEventId(long value) {
        eventId = value;
    }

    public final ObjectGuid getInviteeGuid() {
        return inviteeGuid;
    }

    public final void setInviteeGuid(ObjectGuid value) {
        inviteeGuid = value;
    }

    public final ObjectGuid getSenderGuid() {
        return senderGuid;
    }

    public final void setSenderGuid(ObjectGuid value) {
        senderGuid = value;
    }

    public final long getResponseTime() {
        return responseTime;
    }

    public final void setResponseTime(long value) {
        responseTime = value;
    }

    public final CalendarInviteStatus getStatus() {
        return status;
    }

    public final void setStatus(CalendarInviteStatus value) {
        status = value;
    }

    public final CalendarModerationRank getRank() {
        return rank;
    }

    public final void setRank(CalendarModerationRank value) {
        rank = value;
    }

    public final String getNote() {
        return note;
    }

    public final void setNote(String value) {
        note = value;
    }

    protected void finalize() throws Throwable {
        if (getInviteId() != 0 && getEventId() != 0) {
            global.getCalendarMgr().freeInviteId(getInviteId());
        }
    }
}
