package com.github.azeroth.game.chat;



class EventCommands {
    
    private static boolean handleEventInfoCommand(CommandHandler handler, short eventId) {
        var events = global.getGameEventMgr().getEventMap();

        if (eventId >= events.length) {
            handler.sendSysMessage(SysMessage.EventNotExist);

            return false;
        }

        var eventData = events[eventId];

        if (!eventData.isValid()) {
            handler.sendSysMessage(SysMessage.EventNotExist);

            return false;
        }

        var activeEvents = global.getGameEventMgr().getActiveEventList();
        var active = activeEvents.contains(eventId);
        var activeStr = active ? global.getObjectMgr().getSysMessage(SysMessage.active) : "";

        var startTimeStr = time.UnixTimeToDateTime(eventData.start).ToLongDateString();
        var endTimeStr = time.UnixTimeToDateTime(eventData.end).ToLongDateString();

        var delay = global.getGameEventMgr().nextCheck(eventId);
        var nextTime = gameTime.GetGameTime() + delay;
        var nextStr = nextTime >= eventData.start && nextTime < eventData.end ? time.UnixTimeToDateTime(gameTime.GetGameTime() + delay).ToShortTimeString() : "-";

        var occurenceStr = time.secsToTimeString(eventData.occurence * time.Minute, 0, false);
        var lengthStr = time.secsToTimeString(eventData.length * time.Minute, 0, false);

        handler.sendSysMessage(SysMessage.eventInfo, eventId, eventData.description, activeStr, startTimeStr, endTimeStr, occurenceStr);

        return true;
    }

    
    private static boolean handleEventActiveListCommand(CommandHandler handler) {
        int counter = 0;

        var events = global.getGameEventMgr().getEventMap();
        var activeEvents = global.getGameEventMgr().getActiveEventList();

        var active = global.getObjectMgr().getSysMessage(SysMessage.active);

        for (var eventId : activeEvents) {
            var eventData = events[eventId];

            if (handler.getSession() != null) {
                handler.sendSysMessage(SysMessage.EventEntryListChat, eventId, eventId, eventData.description, active);
            } else {
                handler.sendSysMessage(SysMessage.EventEntryListConsole, eventId, eventData.description, active);
            }

            ++counter;
        }

        if (counter == 0) {
            handler.sendSysMessage(SysMessage.Noeventfound);
        }

        return true;
    }

    
    private static boolean handleEventStartCommand(CommandHandler handler, short eventId) {
        var events = global.getGameEventMgr().getEventMap();

        if (eventId < 1 || eventId >= events.length) {
            handler.sendSysMessage(SysMessage.EventNotExist);

            return false;
        }

        var eventData = events[eventId];

        if (!eventData.isValid()) {
            handler.sendSysMessage(SysMessage.EventNotExist);

            return false;
        }

        var activeEvents = global.getGameEventMgr().getActiveEventList();

        if (activeEvents.contains(eventId)) {
            handler.sendSysMessage(SysMessage.EventAlreadyActive, eventId);

            return false;
        }

        global.getGameEventMgr().startEvent(eventId, true);

        return true;
    }

    
    private static boolean handleEventStopCommand(CommandHandler handler, short eventId) {
        var events = global.getGameEventMgr().getEventMap();

        if (eventId < 1 || eventId >= events.length) {
            handler.sendSysMessage(SysMessage.EventNotExist);

            return false;
        }

        var eventData = events[eventId];

        if (!eventData.isValid()) {
            handler.sendSysMessage(SysMessage.EventNotExist);

            return false;
        }

        var activeEvents = global.getGameEventMgr().getActiveEventList();

        if (!activeEvents.contains(eventId)) {
            handler.sendSysMessage(SysMessage.EventNotActive, eventId);

            return false;
        }

        global.getGameEventMgr().stopEvent(eventId, true);

        return true;
    }
}
