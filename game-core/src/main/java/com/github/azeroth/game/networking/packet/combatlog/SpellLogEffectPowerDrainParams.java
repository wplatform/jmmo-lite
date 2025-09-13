package com.github.azeroth.game.networking.packet.combatlog;

public final class SpellLogEffectPowerDrainParams {
    public ObjectGuid victim = ObjectGuid.EMPTY;
    public int points;
    public int powerType;
    public float amplitude;

    public SpellLogEffectPowerDrainParams clone() {
        SpellLogEffectPowerDrainParams varCopy = new SpellLogEffectPowerDrainParams();

        varCopy.victim = this.victim;
        varCopy.points = this.points;
        varCopy.powerType = this.powerType;
        varCopy.amplitude = this.amplitude;

        return varCopy;
    }
}
