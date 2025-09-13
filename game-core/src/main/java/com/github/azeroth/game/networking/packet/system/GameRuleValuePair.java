package com.github.azeroth.game.networking.packet.system;

import com.github.azeroth.game.networking.WorldPacket;

public final class GameRuleValuePair {
    public int rule;
    public int value;

    public void write(WorldPacket data) {
        data.writeInt32(rule);
        data.writeInt32(value);
    }

    public GameRuleValuePair clone() {
        GameRuleValuePair varCopy = new GameRuleValuePair();

        varCopy.rule = this.rule;
        varCopy.value = this.value;

        return varCopy;
    }
}
