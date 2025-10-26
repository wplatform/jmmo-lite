package com.github.azeroth.game.loot;


import com.github.azeroth.defines.LootMode;
import com.github.azeroth.game.entity.player.Player;

public final class LootGroupInvalidSelector {
    private final LootMode lootMode;
    private final Player personalLooter;

    public LootGroupInvalidSelector() {
    }

    public LootGroupInvalidSelector(LootMode lootMode, Player personalLooter) {
        this.lootMode = lootMode;
        this.personalLooter = personalLooter;
    }

    public boolean check(LootStoreItem item) {
        if ((item.lootMode & lootMode) == 0) {
            return true;
        }

        if (personalLooter && !LootItem.allowedForPlayer(personalLooter, null, item.itemId, item.needsQuest, !item.needsQuest || global.getObjectMgr().getItemTemplate(item.itemId).hasFlag(ItemFlagsCustom.FollowLootRules), true, item.conditions)) {
            return true;
        }

        return false;
    }

}
