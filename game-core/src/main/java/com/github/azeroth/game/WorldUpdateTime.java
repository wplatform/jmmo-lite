package com.github.azeroth.game;


import game.UpdateTime;

public class WorldUpdateTime extends UpdateTime {
    private int recordUpdateTimeInverval;
    private int recordUpdateTimeMin;
    private int lastRecordTime;

    public final void loadFromConfig() {
        recordUpdateTimeInverval = ConfigMgr.GetDefaultValue("RecordUpdateTimeDiffInterval", 60000);
        recordUpdateTimeMin = ConfigMgr.GetDefaultValue("MinRecordUpdateTimeDiff", 100);
    }

    public final void setRecordUpdateTimeInterval(int t) {
        recordUpdateTimeInverval = t;
    }

    public final void recordUpdateTime(int gameTimeMs, int diff, int sessionCount) {
        if (recordUpdateTimeInverval > 0 && diff > recordUpdateTimeMin) {
            if (time.GetMSTimeDiff(lastRecordTime, gameTimeMs) > recordUpdateTimeInverval) {
                Log.outDebug(LogFilter.misc, String.format("Update time diff: %1$s. Players online: %2$s.", getAverageUpdateTime(), sessionCount));
                lastRecordTime = gameTimeMs;
            }
        }
    }

    public final void recordUpdateTimeDuration(String text) {
        recordUpdateTimeDuration(text, recordUpdateTimeMin);
    }
}
