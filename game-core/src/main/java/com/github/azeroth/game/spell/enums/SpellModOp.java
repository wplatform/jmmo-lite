package com.github.azeroth.game.spell.enums;

public enum SpellModOp {
    HealingAndDamage,
    Duration,
    Hate,
    PointsIndex0,
    ProcCharges,
    Range,
    Radius,
    CritChance,
    Points,
    ResistPushback,
    ChangeCastTime,
    Cooldown,
    PointsIndex1,
    TargetResistance,
    PowerCost0, // Used when SpellPowerEntry::PowerIndex == 0
    CritDamageAndHealing,
    HitChance,
    ChainTargets,
    ProcChance,
    Period,
    ChainAmplitude,
    StartCooldown,
    PeriodicHealingAndDamage,
    PointsIndex2,
    BonusCoefficient,
    TriggerDamage, // NYI
    ProcFrequency,
    Amplitude,
    DispelResistance,
    CrowdDamage, // NYI
    PowerCostOnMiss,
    Doses,
    PointsIndex3,
    PointsIndex4,
    PowerCost1, // Used when SpellPowerEntry::PowerIndex == 1
    ChainJumpDistance,
    AreaTriggerMaxSummons, // NYI
    MaxAuraStacks,
    ProcCooldown,
    PowerCost2; // Used when SpellPowerEntry::PowerIndex == 2


}
