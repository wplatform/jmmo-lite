package com.github.azeroth.game.networking.packet.combat;


public class AttackSwingError extends ServerPacket {
    private final AttackSwingErr reason;


    public AttackSwingError() {
        this(AttackSwingErr.CantAttack);
    }

    public AttackSwingError(AttackSwingErr reason) {
        super(ServerOpcode.AttackSwingError);
        reason = reason;
    }

    @Override
    public void write() {
        this.writeBits((int) reason.getValue(), 3);
        this.flushBits();
    }
}
