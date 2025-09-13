package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ServerPacket;

public class RatedPvpInfo extends ServerPacket {
    private final bracketInfo[] bracket = new BracketInfo[6];

    public RatedPvpInfo() {
        super(ServerOpcode.RatedPvpInfo);
    }

    @Override
    public void write() {
        for (var bracket : bracket) {
            bracket.write(this);
        }
    }
}
