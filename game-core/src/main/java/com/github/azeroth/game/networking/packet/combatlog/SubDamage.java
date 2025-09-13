package com.github.azeroth.game.networking.packet.combatlog;

public final class SubDamage {
    public int schoolMask;
    public float FDamage; // Float damage (Most of the time equals to damage)
    public int damage;
    public int absorbed;
    public int resisted;

    public SubDamage clone() {
        SubDamage varCopy = new SubDamage();

        varCopy.schoolMask = this.schoolMask;
        varCopy.FDamage = this.FDamage;
        varCopy.damage = this.damage;
        varCopy.absorbed = this.absorbed;
        varCopy.resisted = this.resisted;

        return varCopy;
    }
}
