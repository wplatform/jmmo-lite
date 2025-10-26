package com.github.azeroth.game.domain.pet;

import com.github.azeroth.game.domain.unit.ReactState;
import lombok.Data;

@Data
public class PetInfo {

    private String name;
    private String actionBar;
    private int petNumber;
    private int creatureId;
    private int displayId;
    private int experience;
    private int health;
    private int mana;
    private int lastSaveTime;
    private int createdBySpellId;
    private short specializationId;
    private byte level;
    private ReactState reactState;
    private PetType type;
    boolean wasRenamed;

}
