package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class TutorialSetFlag extends ClientPacket {
    public Tutorialaction action = TutorialAction.values()[0];
    public int tutorialBit;

    public TutorialSetFlag(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        action = TutorialAction.forValue(this.<Byte>readBit(2));

        if (action == TutorialAction.Update) {
            tutorialBit = this.readUInt32();
        }
    }
}
