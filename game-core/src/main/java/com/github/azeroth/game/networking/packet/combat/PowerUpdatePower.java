package com.github.azeroth.game.networking.packet.combat;

public final class PowerUpdatePower {
    public int power;
    public byte powerType;

    public PowerUpdatePower() {
    }
    public PowerUpdatePower(int power, byte powerType) {
        power = power;
        powerType = powerType;
    }

    public PowerUpdatePower clone() {
        PowerUpdatePower varCopy = new PowerUpdatePower();

        varCopy.power = this.power;
        varCopy.powerType = this.powerType;

        return varCopy;
    }
}
