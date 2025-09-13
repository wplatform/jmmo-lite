package com.github.azeroth.game.networking.packet.pet;

public class PetSpellCooldown {
    public int spellID;
    public int duration;
    public int categoryDuration;
    public float modRate = 1.0f;
    public short category;
}
