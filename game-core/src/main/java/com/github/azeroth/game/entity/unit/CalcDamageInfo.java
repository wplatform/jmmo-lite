package com.github.azeroth.game.entity.unit;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.defines.WeaponAttackType;
import com.github.azeroth.game.domain.unit.HitInfo;
import com.github.azeroth.game.domain.unit.MeleeHitOutcome;
import com.github.azeroth.game.domain.unit.VictimState;
import com.github.azeroth.game.spell.enums.ProcFlag;
import lombok.Data;

@Data
public class CalcDamageInfo {
    public double damage;
    public double absorb;
    private Unit attacker;
    private Unit target;
    private int damageSchoolMask;
    private double originalDamage;
    private double resist;
    private double blocked;
    private VictimState targetState;
    // Helper
    private WeaponAttackType attackType;
    private EnumFlag<ProcFlag> procAttacker;
    private EnumFlag<ProcFlag> procVictim;
    private double cleanDamage;
    private MeleeHitOutcome hitOutCome;
    private EnumFlag<HitInfo> hitInfo;
}
