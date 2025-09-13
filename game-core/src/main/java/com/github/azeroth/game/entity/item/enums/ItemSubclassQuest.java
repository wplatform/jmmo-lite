package com.github.azeroth.game.entity.item.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum ItemSubclassQuest {
    QUEST(0),
    QUEST_UNK3(3), // 1 item (33604)
    QUEST_UNK8(8); // 2 items (37445, 49700)

    public final int value;
}
