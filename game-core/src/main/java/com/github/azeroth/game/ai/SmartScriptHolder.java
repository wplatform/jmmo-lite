package game.ai;

import Framework.Constants.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class SmartScriptHolder implements java.lang.Comparable<SmartScriptHolder> {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public const uint DefaultPriority = uint.MaxValue;
    public static final int DEFAULT_PRIORITY = Integer.MAX_VALUE;

    public int entryOrGuid;
    public SmartScriptType sourceType = SmartScriptType.values()[0];
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint EventId;
    public int eventId;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint Link;
    public int link;
    public SmartEvent event = new SmartEvent();
    public SmartAction action = new SmartAction();
    public SmartTarget target = new SmartTarget();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint Timer;
    public int timer;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint Priority;
    public int priority;
    public boolean active;
    public boolean runOnce;
    public boolean enableTimed;

    public SmartScriptHolder() {
    }

    public SmartScriptHolder(SmartScriptHolder other) {
        entryOrGuid = other.entryOrGuid;
        sourceType = other.sourceType;
        eventId = other.eventId;
        link = other.link;
        event = other.event.clone();
        action = other.action.clone();
        target = other.target.clone();
        timer = other.timer;
        active = other.active;
        runOnce = other.runOnce;
        enableTimed = other.enableTimed;
    }

    public final int compareTo(SmartScriptHolder other) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var result = Priority.CompareTo(other.Priority);
        var result = (new Integer(priority)).compareTo(other.priority);

        if (result == 0) {
            result = (new Integer(entryOrGuid)).compareTo(other.entryOrGuid);
        }

        if (result == 0) {
            result = sourceType.CompareTo(other.sourceType);
        }

        if (result == 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: result = EventId.CompareTo(other.EventId);
            result = (new Integer(eventId)).compareTo(other.eventId);
        }

        if (result == 0) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: result = Link.CompareTo(other.Link);
            result = (new Integer(link)).compareTo(other.link);
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
        return action.type;
    }

    public final SmartTargets getTargetType() {
        return target.type;
    }

    @Override
    public String toString() {
        return String.format("Entry %1$s SourceType %2$s Event %3$s Action %4$s", entryOrGuid, getScriptType(), eventId, getActionType());
    }
}