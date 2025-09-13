package com.github.azeroth.game.spell.auras.model;

import com.github.azeroth.dbc.defines.DbcDefine;

public class AuraLoadEffectInfo {
    public final int[] amounts = new int[DbcDefine.MAX_SPELL_EFFECTS];
    public final int[] baseAmounts = new int[DbcDefine.MAX_SPELL_EFFECTS];
}
