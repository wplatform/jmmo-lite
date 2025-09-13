package com.github.azeroth.game.entity.player.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(onMethod_ = {@Override})
@RequiredArgsConstructor
public
enum PlayerFlagEx implements EnumFlag.FlagValue {
    REAGENT_BANK_UNLOCKED(0x0001),
    MERCENARY_MODE(0x0002),
    ARTIFACT_FORGE_CHEAT(0x0004),
    IN_PVP_COMBAT(0x0040),       // Forbids /follow
    MENTOR(0x0080),
    NEWCOMER(0x0100),
    UNLOCKED_AOE_LOOT(0x0200);

    public final int value;

}
