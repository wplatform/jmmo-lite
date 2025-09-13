package com.github.azeroth.game.networking.packet.misc;


public class TutorialFlags extends ServerPacket {
    public int[] tutorialData = new int[SharedConst.MaxAccountTutorialValues];

    public TutorialFlags() {
        super(ServerOpcode.TutorialFlags);
    }

    @Override
    public void write() {
        for (byte i = 0; i < Tutorials.max.getValue(); ++i) {
            this.writeInt32(TutorialData[i]);
        }
    }
}
