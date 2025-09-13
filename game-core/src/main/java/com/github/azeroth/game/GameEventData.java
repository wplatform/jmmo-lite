package com.github.azeroth.game;


import java.util.ArrayList;
import java.util.HashMap;

public class GameEventData {
    public long start; // occurs after this time
    public long end; // occurs before this time
    public long nextstart; // after this time the follow-up events count this phase completed
    public int occurence; // time between end and start
    public int length; // length of the event (time.Minutes) after finishing all conditions
    public HolidayIds holiday_id = HolidayIds.values()[0];
    public byte holidayStage;
    public GameEventState state = GameEventState.values()[0]; // state of the game event, these are saved into the game_event table on change!
    public HashMap<Integer, GameEventFinishCondition> conditions = new HashMap<Integer, GameEventFinishCondition>(); // conditions to finish
    public ArrayList<SHORT> prerequisite_events = new ArrayList<>(); // events that must be completed before starting this event
    public String description;
    public byte announce; // if 0 dont announce, if 1 announce, if 2 take config value

    public GameEventData() {
        start = 1;
    }

    public final boolean isValid() {
        return length > 0 || state.getValue() > GameEventState.NORMAL.getValue();
    }
}
