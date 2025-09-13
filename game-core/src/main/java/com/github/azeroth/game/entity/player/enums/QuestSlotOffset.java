package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum QuestSlotOffset {
    QUEST_ID_OFFSET(0),
    QUEST_STATE_OFFSET(1),
    QUEST_COUNTS_OFFSET(2),
    QUEST_END_TIME_OFFSET(14);
    public final int offset;
}
