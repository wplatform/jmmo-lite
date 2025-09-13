package com.github.azeroth.game.ai;


public class SmartScriptHolder implements Comparable<SmartScriptHolder> {
    public static final int defaultPriority = Integer.MAX_VALUE;

    public int entryOrGuid;
    public SmartScriptType sourceType = SmartScriptType.values()[0];
    public int eventId;
    public int link;
    public Smartevent event = new smartEvent();
    public smartAction tangible.Action0Param =new
    public Smarttarget target = new smartTarget();
    public int timer;
    public int priority;
    public boolean active;
    public boolean runOnce;
    public boolean enableTimed;
    smartAction();

    public SmartScriptHolder() {
    }

    public SmartScriptHolder(SmartScriptHolder other) {
        entryOrGuid = other.entryOrGuid;
        sourceType = other.sourceType;
        eventId = other.eventId;
        link = other.link;
        event = other.event;
        tangible.Action0Param = other.action;
        target = other.target;
        timer = other.timer;
        active = other.active;
        runOnce = other.runOnce;
        enableTimed = other.enableTimed;
    }

    public final int compareTo(SmartScriptHolder other) {
        var result = (new integer(priority)).compareTo(other.priority);

        if (result == 0) {
            result = (new integer(entryOrGuid)).compareTo(other.entryOrGuid);
        }

        if (result == 0) {
            result = sourceType.CompareTo(other.sourceType);
        }

        if (result == 0) {
            result = (new integer(eventId)).compareTo(other.eventId);
        }

        if (result == 0) {
            result = (new integer(link)).compareTo(other.link);
        }

        return result;
    }

    public final SmartScriptType getScriptType() {
        return sourceType;
    }

    public final SmartEvents getEventType() {
        return event.type;
    }

    public final SmartActions getActionType() {
        return tangible.Action0Param.type;
    }

    public final SmartTargets getTargetType() {
        return target.type;
    }

    @Override
    public String toString() {
        return String.format("Entry %1$s SourceType %2$s Event %3$s Action %4$s", entryOrGuid, getScriptType(), eventId, getActionType());
    }
}
