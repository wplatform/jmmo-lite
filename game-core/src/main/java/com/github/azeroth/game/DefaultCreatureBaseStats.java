package com.github.azeroth.game;


import com.github.azeroth.game.domain.creature.CreatureBaseStats;

public class DefaultCreatureBaseStats extends CreatureBaseStats {
    public DefaultCreatureBaseStats() {
        setBaseMana(0);
        setAttackPower(0);
        setRangedAttackPower(0);
    }
}
