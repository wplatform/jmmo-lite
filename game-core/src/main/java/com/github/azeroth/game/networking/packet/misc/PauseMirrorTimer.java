package com.github.azeroth.game.networking.packet.misc;


public class PauseMirrorTimer extends ServerPacket {
    public boolean paused = true;
    public MirrortimerType timer = MirrorTimerType.values()[0];

    public PauseMirrorTimer(MirrorTimerType timer, boolean paused) {
        super(ServerOpcode.PauseMirrorTimer);
        timer = timer;
        paused = paused;
    }

    @Override
    public void write() {
        this.writeInt32(timer.getValue());
        this.writeBit(paused);
        this.flushBits();
    }
}
