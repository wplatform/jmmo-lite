package com.github.azeroth.defines;

public enum LootSlotType {
    ALLOW_LOOT,                    // player can loot the item.
    ROLL_ONGOING,                  // roll is ongoing. player cannot loot.
    MASTER,                        // item can only be distributed by group loot master.
    LOCKED,                        // item is shown in red. player cannot loot.
    OWNER                          // ignore binding confirmation and etc, for single player looting

}
