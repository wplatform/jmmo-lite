package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.WorldPacket;

public final class SpellMissStatus {
    public SpellMissInfo reason = SpellMissInfo.values()[0];
    public SpellMissInfo reflectStatus = SpellMissInfo.values()[0];

    public SpellMissStatus() {
    }

    public SpellMissStatus(SpellMissInfo reason, SpellMissInfo reflectStatus) {
        reason = reason;
        reflectStatus = reflectStatus;
    }

    public void write(WorldPacket data) {
        data.writeBits((byte) reason.getValue(), 4);

        if (reason == SpellMissInfo.Reflect) {
            data.writeBits(reflectStatus, 4);
        }

        data.flushBits();
    }

    public SpellMissStatus clone() {
        SpellMissStatus varCopy = new SpellMissStatus();

        varCopy.reason = this.reason;
        varCopy.reflectStatus = this.reflectStatus;

        return varCopy;
    }
}
