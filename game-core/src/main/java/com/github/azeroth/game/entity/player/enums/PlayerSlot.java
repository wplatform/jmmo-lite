package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum PlayerSlot {

    // first slot for item stored (in any way in player m_items data
    PLAYER_SLOT_START(0),
    // last+1 slot for item stored (in any way in player m_items data
    PLAYER_SLOT_END(195),
    PLAYER_SLOTS_COUNT(PLAYER_SLOT_END.index - PLAYER_SLOT_START.index);

    public final int index;
}
