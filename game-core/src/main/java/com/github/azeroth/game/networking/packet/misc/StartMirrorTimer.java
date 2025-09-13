package com.github.azeroth.game.networking.packet.misc;


public class StartMirrorTimer extends ServerPacket {
    public int scale;
    public int maxValue;
    public MirrortimerType timer = MirrorTimerType.values()[0];
    public int spellID;
    public int value;
    public boolean paused;

    public StartMirrorTimer(MirrorTimerType timer, int value, int maxValue, int scale, int spellID, boolean paused) {
        super(ServerOpcode.StartMirrorTimer);
        timer = timer;
        value = value;
        maxValue = maxValue;
        scale = scale;
        spellID = spellID;
        paused = paused;
    }

    @Override
    public void write() {
        this.writeInt32(timer.getValue());
        this.writeInt32(value);
        this.writeInt32(maxValue);
        this.writeInt32(scale);
        this.writeInt32(spellID);
        this.writeBit(paused);
        this.flushBits();
    }
}
