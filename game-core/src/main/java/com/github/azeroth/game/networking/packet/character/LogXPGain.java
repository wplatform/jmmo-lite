package com.github.azeroth.game.networking.packet.character;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.enums.PlayerLogXPReason;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class LogXPGain extends ServerPacket {
    public ObjectGuid victim = ObjectGuid.EMPTY;
    public int original;
    public PlayerLogXPReason reason;
    public int amount;
    public float groupBonus;

    public LogXPGain() {
        super(ServerOpCode.SMSG_LOG_XP_GAIN);
    }

    @Override
    public void write() {
        this.writeGuid(victim);
        this.writeInt32(original);
        this.writeInt8((byte) reason.ordinal());
        this.writeInt32(amount);
        this.writeFloat(groupBonus);
    }
}
