package com.github.azeroth.game.entity.player.model;

import com.github.azeroth.game.entity.player.enums.RuneType;
import com.github.azeroth.game.spell.AuraEffect;
import com.github.azeroth.game.spell.SpellInfo;
import com.github.azeroth.game.spell.auras.enums.AuraType;

public class RuneInfo {

    public RuneType baseRune;
    public RuneType currentRune;
    public float cooldown;
    public AuraEffect convertAura;
    public AuraType convertAuraType;
    public SpellInfo convertAuraInfo;

}
