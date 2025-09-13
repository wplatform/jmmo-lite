package com.github.azeroth.game.networking.packet.trait;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class TraitConfigCommitFailed extends ServerPacket {
    public int configID;
    public int spellID;
    public int reason;


    public TraitConfigCommitFailed(int configId, int spellId) {
        this(configId, spellId, 0);
    }

    public TraitConfigCommitFailed(int configId) {
        this(configId, 0, 0);
    }

    public TraitConfigCommitFailed() {
        this(0, 0, 0);
    }

    public TraitConfigCommitFailed(int configId, int spellId, int reason) {
        super(ServerOpCode.TraitConfigCommitFailed);
        configID = configId;
        spellID = spellId;
        reason = reason;
    }

    @Override
    public void write() {
        this.writeInt32(configID);
        this.writeInt32(spellID);
        this.writeBits(reason, 4);
        this.flushBits();
    }
}
