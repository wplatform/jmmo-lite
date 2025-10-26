package com.github.azeroth.game.battleground;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BattlegroundSpell {
    SPIRIT_HEAL_CHANNEL_AOE   (22011),                // used for AoE resurrections
    SPIRIT_HEAL_PLAYER_AURA   (156758),               // individual player timers for resurrection
    SPIRIT_HEAL_CHANNEL_SELF  (305122),               // channel visual for individual area spirit healers
    WAITING_FOR_RESURRECT     (2584),                 // Waiting to Resurrect
    VISUAL_SPIRIT_HEAL_CHANNEL      (3060),
    SPIRIT_HEAL               (22012),                // Spirit Heal
    RESURRECTION_VISUAL       (24171),                // Resurrection Impact Visual
    ARENA_PREPARATION         (32727),                // use this one, 32728 not correct
    PREPARATION               (44521),                // Preparation
    SPIRIT_HEAL_MANA          (44535),                // Spirit Heal
    RECENTLY_DROPPED_ALLIANCE_FLAG (42792),           // makes Alliance flag unselectable
    RECENTLY_DROPPED_HORDE_FLAG (50326),              // makes Horde flag unselectable
    RECENTLY_DROPPED_NEUTRAL_FLAG (50327),            // makes Neutral flag unselectable
    AURA_PLAYER_INACTIVE      (43681),                // Inactive
    HONORABLE_DEFENDER_25Y    (68652),                // +50% honor when standing at a capture point that you control, 25yards radius (added in 3.2)
    HONORABLE_DEFENDER_60Y    (66157),                // +50% honor when standing at a capture point that you control, 60yards radius (added in 3.2), probably for 40+ player battlegrounds
    MERCENARY_CONTRACT_HORDE  (193472),
    MERCENARY_CONTRACT_ALLIANCE (193475),
    MERCENARY_HORDE_1         (193864),
    MERCENARY_HORDE_REACTIONS (195838),
    MERCENARY_ALLIANCE_1      (193863),
    MERCENARY_ALLIANCE_REACTIONS (195843),
    MERCENARY_SHAPESHIFT      (193970),
    PET_SUMMONED              (6962); // used after resurrection

    public final int spellId;
}
