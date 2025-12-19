package com.github.azeroth.game.domain.instance;


import lombok.Data;

@Data
public class DoorInfo {
    private BossInfo bossInfo;
    private EncounterDoorBehavior behavior;

}
