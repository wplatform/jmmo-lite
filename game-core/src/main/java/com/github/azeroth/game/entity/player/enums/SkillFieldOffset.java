package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SkillFieldOffset {
    SKILL_ID_OFFSET(0),
    SKILL_STEP_OFFSET(64),
    SKILL_RANK_OFFSET(SKILL_STEP_OFFSET.value + 64),
    SUBSKILL_START_RANK_OFFSET(SKILL_RANK_OFFSET.value + 64),
    SKILL_MAX_RANK_OFFSET(SUBSKILL_START_RANK_OFFSET.value + 64),
    SKILL_TEMP_BONUS_OFFSET(SKILL_MAX_RANK_OFFSET.value + 64),
    SKILL_PERM_BONUS_OFFSET(SKILL_TEMP_BONUS_OFFSET.value + 64);

    public final int value;

}
