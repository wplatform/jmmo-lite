package com.github.azeroth.game.loot;


import com.github.azeroth.game.entity.player.Player;

public final class LootGroupInvalidSelector {
    private final short lootMode;
    private final Player personalLooter;

    public LootGroupInvalidSelector() {
    }

    public LootGroupInvalidSelector(short lootMode, Player personalLooter) {
        lootMode = lootMode;
        personalLooter = personalLooter;
    }

    public boolean check(LootStoreItem item) {
        if ((item.lootmode & lootMode) == 0) {
            return true;
        }

        if (personalLooter && !LootItem.allowedForPlayer(personalLooter, null, item.itemid, item.needs_quest, !item.needs_quest || global.getObjectMgr().getItemTemplate(item.itemid).hasFlag(ItemFlagsCustom.FollowLootRules), true, item.conditions)) {
            return true;
        }

        return false;
    }

    public LootGroupInvalidSelector clone() {
        LootGroupInvalidSelector varCopy = new LootGroupInvalidSelector();

        varCopy.lootMode = this.lootMode;
        varCopy.personalLooter = this.personalLooter;

        return varCopy;
    }
}
