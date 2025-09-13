package com.github.azeroth.game.entity.player;

public class ResurrectionData {
    private ObjectGuid guid = ObjectGuid.EMPTY;
    private Worldlocation location = new worldLocation();

    private int health;

    private int mana;

    private int aura;

    public final ObjectGuid getGuid() {
        return guid;
    }

    public final void setGuid(ObjectGuid value) {
        guid = value;
    }

    public final WorldLocation getLocation() {
        return location;
    }

    public final void setLocation(WorldLocation value) {
        location = value;
    }


    public final int getHealth() {
        return health;
    }


    public final void setHealth(int value) {
        health = value;
    }


    public final int getMana() {
        return mana;
    }


    public final void setMana(int value) {
        mana = value;
    }


    public final int getAura() {
        return aura;
    }


    public final void setAura(int value) {
        aura = value;
    }
}
