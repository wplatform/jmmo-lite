package com.github.azeroth.game.networking.packet.battleground;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class JoinSkirmish extends ClientPacket {
    public byte roles = 0;
    public bracketType bracket = BracketType.forValue(0);
    public boolean joinAsGroup = false;
    public boolean unkBool = false;

    public JoinSkirmish(WorldPacket worldPacket) {
        super(worldPacket);
    }

    @Override
    public void read() {
        joinAsGroup = this.readBit() != 0;
        unkBool = this.readBit() != 0;
        roles = this.readBit();
        bracket = BracketType.forValue(this.readBit());
    }
}
