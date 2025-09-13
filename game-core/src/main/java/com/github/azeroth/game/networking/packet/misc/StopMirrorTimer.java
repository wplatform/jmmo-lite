package com.github.azeroth.game.networking.packet.misc;


public class StopMirrorTimer extends ServerPacket {
    public MirrortimerType timer = MirrorTimerType.values()[0];

    public stopMirrorTimer(MirrorTimerType timer) {
        super(ServerOpcode.StopMirrorTimer);
        timer = timer;
    }

    @Override
    public void write() {
        this.writeInt32(timer.getValue());
    }
}
