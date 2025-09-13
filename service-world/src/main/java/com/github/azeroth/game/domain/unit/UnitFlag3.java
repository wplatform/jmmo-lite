package com.github.azeroth.game.domain.unit;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Value masks for UNIT_FIELD_FLAGS_3
// EnumUtils: DESCRIBE THIS
@Getter
@RequiredArgsConstructor
public enum UnitFlag3 implements EnumFlag.FlagValue {
    UNK0(0x00000001),
    UNCONSCIOUS_ON_DEATH(0x00000002),   // TITLE Unconscious on Death DESCRIPTION Shows "Unconscious" in unit tooltip instead of "Dead"
    ALLOW_MOUNTED_COMBAT(0x00000004),   // TITLE Allow mounted combat
    GARRISON_PET(0x00000008),   // TITLE Garrison pet DESCRIPTION Special garrison pet creatures that display one of favorite player battle pets - this flag allows querying name and turns off default battle pet behavior
    UI_CAN_GET_POSITION(0x00000010),   // TITLE UI Can Get Position DESCRIPTION Allows lua functions like UnitPosition to always get the position even for npcs or non-grouped players
    AI_OBSTACLE(0x00000020),
    ALTERNATIVE_DEFAULT_LANGUAGE(0x00000040),
    SUPPRESS_ALL_NPC_FEEDBACK(0x00000080),   // TITLE Suppress all NPC feedback DESCRIPTION Skips playing sounds on left clicking npc for all npcs as long as npc with this flag is visible
    IGNORE_COMBAT(0x00000100),   // TITLE Ignore Combat DESCRIPTION Same as SPELL_AURA_IGNORE_COMBAT
    SUPPRESS_NPC_FEEDBACK(0x00000200),   // TITLE Suppress NPC feedback DESCRIPTION Skips playing sounds on left clicking npc
    UNK10(0x00000400),
    UNK11(0x00000800),
    UNK12(0x00001000),
    FAKE_DEAD(0x00002000),   // TITLE Show as dead
    NO_FACING_ON_INTERACT_AND_FAST_FACING_CHASE(0x00004000),   // Causes the creature to both not change facing on interaction and speeds up smooth facing changes while attacking (clientside)
    UNTARGETABLE_FROM_UI(0x00008000),   // TITLE Untargetable from UI DESCRIPTION Cannot be targeted from lua functions StartAttack, TargetUnit, PetAttack
    NO_FACING_ON_INTERACT_WHILE_FAKE_DEAD(0x00010000),   // Prevents facing changes while interacting if creature has flag FAKE_DEAD
    ALREADY_SKINNED(0x00020000),
    SUPPRESS_ALL_NPC_SOUNDS(0x00040000),   // TITLE Suppress all NPC sounds DESCRIPTION Skips playing sounds on beginning and end of npc interaction for all npcs as long as npc with this flag is visible
    SUPPRESS_NPC_SOUNDS(0x00080000),   // TITLE Suppress NPC sounds DESCRIPTION Skips playing sounds on beginning and end of npc interaction
    ALLOW_INTERACTION_WHILE_IN_COMBAT(0x00100000),
    UNK21(0x00200000),
    DONT_FADE_OUT(0x00400000),
    UNK23(0x00800000),
    FORCE_HIDE_NAMEPLATE(0x01000000),
    UNK25(0x02000000),
    UNK26(0x04000000),
    UNK27(0x08000000),
    UNK28(0x10000000),
    UNK29(0x20000000),
    UNK30(0x40000000),
    UNK31(0x80000000),

    DISALLOWED(UNK0.value | /* UNCONSCIOUS_ON_DEATH | */ /* ALLOW_MOUNTED_COMBAT | */ GARRISON_PET.value |
            /* UI_CAN_GET_POSITION | */ /* AI_OBSTACLE | */ ALTERNATIVE_DEFAULT_LANGUAGE.value | /* SUPPRESS_ALL_NPC_FEEDBACK | */
            IGNORE_COMBAT.value | SUPPRESS_NPC_FEEDBACK.value | UNK10.value | UNK11.value |
            UNK12.value | /* FAKE_DEAD | */ /* NO_FACING_ON_INTERACT_AND_FAST_FACING_CHASE | */ /* UNTARGETABLE_FROM_UI | */
            /* NO_FACING_ON_INTERACT_WHILE_FAKE_DEAD | */ ALREADY_SKINNED.value | /* SUPPRESS_ALL_NPC_SOUNDS | */ /* SUPPRESS_NPC_SOUNDS | */
            ALLOW_INTERACTION_WHILE_IN_COMBAT.value | UNK21.value | /* DONT_FADE_OUT | */ UNK23.value |
            /* FORCE_HIDE_NAMEPLATE | */ UNK25.value | UNK26.value | UNK27.value |
            UNK28.value | UNK29.value | UNK30.value | UNK31.value), // SKIP
    ALLOWED(~DISALLOWED.value); // SKIP

    public final int value;
}
