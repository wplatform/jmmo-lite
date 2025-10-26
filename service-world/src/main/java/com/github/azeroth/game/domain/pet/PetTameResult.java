package com.github.azeroth.game.domain.pet;

public enum PetTameResult {
    Ok,
    InvalidCreature,
    TooMany,
    CreatureAlreadyOwned,
    NotTameable,
    AnotherSummonActive,
    UnitsCantTame,
    NoPetAvailable,
    InternalError,
    TooHighLevel,
    Dead,
    NotDead,
    CantControlExotic,
    InvalidSlot,
    EliteTooHighLevel
}
