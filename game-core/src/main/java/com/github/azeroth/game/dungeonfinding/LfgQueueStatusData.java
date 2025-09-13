package com.github.azeroth.game.dungeonfinding;


public class LfgQueueStatusData {
    public byte queueId;
    public int dungeonId;
    public int waitTime;
    public int waitTimeAvg;
    public int waitTimeTank;
    public int waitTimeHealer;
    public int waitTimeDps;
    public int queuedTime;
    public byte tanks;
    public byte healers;
    public byte dps;


    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime, int waitTimeAvg, int waitTimeTank, int waitTimeHealer, int waitTimeDps, int queuedTime, byte tanks, byte healers) {
        this(queueId, dungeonId, waitTime, waitTimeAvg, waitTimeTank, waitTimeHealer, waitTimeDps, queuedTime, tanks, healers, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime, int waitTimeAvg, int waitTimeTank, int waitTimeHealer, int waitTimeDps, int queuedTime, byte tanks) {
        this(queueId, dungeonId, waitTime, waitTimeAvg, waitTimeTank, waitTimeHealer, waitTimeDps, queuedTime, tanks, 0, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime, int waitTimeAvg, int waitTimeTank, int waitTimeHealer, int waitTimeDps, int queuedTime) {
        this(queueId, dungeonId, waitTime, waitTimeAvg, waitTimeTank, waitTimeHealer, waitTimeDps, queuedTime, 0, 0, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime, int waitTimeAvg, int waitTimeTank, int waitTimeHealer, int waitTimeDps) {
        this(queueId, dungeonId, waitTime, waitTimeAvg, waitTimeTank, waitTimeHealer, waitTimeDps, 0, 0, 0, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime, int waitTimeAvg, int waitTimeTank, int waitTimeHealer) {
        this(queueId, dungeonId, waitTime, waitTimeAvg, waitTimeTank, waitTimeHealer, -1, 0, 0, 0, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime, int waitTimeAvg, int waitTimeTank) {
        this(queueId, dungeonId, waitTime, waitTimeAvg, waitTimeTank, -1, -1, 0, 0, 0, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime, int waitTimeAvg) {
        this(queueId, dungeonId, waitTime, waitTimeAvg, -1, -1, -1, 0, 0, 0, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime) {
        this(queueId, dungeonId, waitTime, -1, -1, -1, -1, 0, 0, 0, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId) {
        this(queueId, dungeonId, -1, -1, -1, -1, -1, 0, 0, 0, 0);
    }

    public LfgQueueStatusData(byte queueId) {
        this(queueId, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0);
    }

    public LfgQueueStatusData() {
        this(0, 0, -1, -1, -1, -1, -1, 0, 0, 0, 0);
    }

    public LfgQueueStatusData(byte queueId, int dungeonId, int waitTime, int waitTimeAvg, int waitTimeTank, int waitTimeHealer, int waitTimeDps, int queuedTime, byte tanks, byte healers, byte dps) {
        queueId = queueId;
        dungeonId = dungeonId;
        waitTime = waitTime;
        waitTimeAvg = waitTimeAvg;
        waitTimeTank = waitTimeTank;
        waitTimeHealer = waitTimeHealer;
        waitTimeDps = waitTimeDps;
        queuedTime = queuedTime;
        tanks = tanks;
        healers = healers;
        dps = dps;
    }
}
