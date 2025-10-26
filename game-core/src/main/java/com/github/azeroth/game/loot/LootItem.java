package com.github.azeroth.game.loot;


import com.github.azeroth.dbc.defines.ItemContext;
import com.github.azeroth.defines.LootSlotType;
import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.entity.item.ItemEnchantmentManager;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.Objects;


public class LootItem {
    public int itemid;
    public int lootListId;
    public int randomBonusListId;
    public ArrayList<Integer> bonusListIDs = new ArrayList<>();
    public ItemContext context;
    public ArrayList<Condition> conditions = new ArrayList<>(); // additional loot condition
    public ArrayList<ObjectGuid> allowedGUIDs = new ArrayList<>();
    public ObjectGuid rollWinnerGUID = ObjectGuid.EMPTY; // Stores the guid of person who won loot, if his bags are full only he can see the item in loot list!
    public byte count;
    public boolean is_looted;
    public boolean is_blocked;
    public boolean freeforall; // free for all
    public boolean is_underthreshold;
    public boolean is_counted;
    public boolean needs_quest; // quest drop
    public boolean follow_loot_rules;

    public LootItem() {
    }

    public LootItem(LootStoreItem li) {
        itemid = li.itemId;
        conditions = li.conditions;

        var proto = global.getObjectMgr().getItemTemplate(itemid);
        freeforall = proto != null && proto.hasFlag(ItemFlags.MultiDrop);
        follow_loot_rules = !li.needsQuest || (proto != null && proto.getFlagsCu().hasFlag(ItemFlagsCustom.FollowLootRules));

        needs_quest = li.needsQuest;

        randomBonusListId = ItemEnchantmentManager.generateItemRandomBonusListId(itemid);
    }

    public static boolean allowedForPlayer(Player player, Loot loot, int itemid, boolean needs_quest, boolean follow_loot_rules, boolean strictUsabilityCheck, ArrayList<Condition> conditions) {
        // DB conditions check
        if (!global.getConditionMgr().isObjectMeetToConditions(player, conditions)) {
            return false;
        }

        var pProto = global.getObjectMgr().getItemTemplate(itemid);

        if (pProto == null) {
            return false;
        }

        // not show loot for not own team
        if (pProto.hasFlag(ItemFlags2.FactionHorde) && player.getTeam() != Team.Horde) {
            return false;
        }

        if (pProto.hasFlag(ItemFlags2.FactionAlliance) && player.getTeam() != Team.ALLIANCE) {
            return false;
        }

        // Master looter can see all items even if the character can't loot them
        if (loot != null && loot.getLootMethod() == lootMethod.MasterLoot && follow_loot_rules && Objects.equals(loot.getLootMasterGUID(), player.getGUID())) {
            return true;
        }

        // Don't allow loot for players without profession or those who already know the recipe
        if (pProto.hasFlag(ItemFlags.HideUnusableRecipe)) {
            if (!player.hasSkill(SkillType.forValue(pProto.getRequiredSkill()))) {
                return false;
            }

            for (var itemEffect : pProto.getEffects()) {
                if (itemEffect.triggerType != ItemSpelltriggerType.OnLearn) {
                    continue;
                }

                if (player.hasSpell((int) itemEffect.spellID)) {
                    return false;
                }
            }
        }

        // check quest requirements
        if (!pProto.getFlagsCu().hasFlag(ItemFlagsCustom.IgnoreQuestStatus) && ((needs_quest || (pProto.getStartQuest() != 0 && player.getQuestStatus(pProto.getStartQuest()) != QuestStatus.NONE)) && !player.hasQuestForItem(itemid))) {
            return false;
        }

        if (strictUsabilityCheck) {
            if ((pProto.isWeapon() || pProto.isArmor()) && !pProto.isUsableByLootSpecialization(player, true)) {
                return false;
            }

            if (player.canRollNeedForItem(pProto, null, false) != InventoryResult.Ok) {
                return false;
            }
        }

        return true;
    }

    /**
     * Basic checks for player/item compatibility - if false no chance to see the item in the loot - used only for loot generation
     *
     * @param player
     * @param loot
     * @return
     */
    public final boolean allowedForPlayer(Player player, Loot loot) {
        return allowedForPlayer(player, loot, itemid, needs_quest, follow_loot_rules, false, conditions);
    }

    public final void addAllowedLooter(Player player) {
        allowedGUIDs.add(player.getGUID());
    }

    public final boolean hasAllowedLooter(ObjectGuid looter) {
        return allowedGUIDs.contains(looter);
    }

    public final LootSlotType getUiTypeForPlayer(Player player, Loot loot) {
        if (is_looted) {
            return null;
        }

        if (!allowedGUIDs.contains(player.getGUID())) {
            return null;
        }

        if (freeforall) {
            var ffaItems = loot.getPlayerFFAItems().get(player.getGUID());

            if (ffaItems != null) {
                var ffaItemItr = tangible.ListHelper.find(ffaItems, ffaItem -> ffaItem.lootListId == lootListId);

                if (ffaItemItr != null && !ffaItemItr.is_looted) {
                    return loot.getLootMethod() == lootMethod.FreeForAll ? LootSlotType.Owner : LootSlotType.AllowLoot;
                }
            }

            return null;
        }

        if (needs_quest && !follow_loot_rules) {
            return loot.getLootMethod() == lootMethod.FreeForAll ? LootSlotType.Owner : LootSlotType.AllowLoot;
        }

        switch (loot.getLootMethod()) {
            case FreeForAll:
                return LootSlotType.owner;
            case RoundRobin:
                if (!loot.roundRobinPlayer.isEmpty() && ObjectGuid.opNotEquals(loot.roundRobinPlayer, player.getGUID())) {
                    return null;
                }

                return LootSlotType.AllowLoot;
            case MasterLoot:
                if (is_underthreshold) {
                    if (!loot.roundRobinPlayer.isEmpty() && ObjectGuid.opNotEquals(loot.roundRobinPlayer, player.getGUID())) {
                        return null;
                    }

                    return LootSlotType.AllowLoot;
                }

                return Objects.equals(loot.getLootMasterGUID(), player.getGUID()) ? LootSlotType.Master : LootSlotType.locked;
            case GroupLoot:
            case NeedBeforeGreed:
                if (is_underthreshold) {
                    if (!loot.roundRobinPlayer.isEmpty() && ObjectGuid.opNotEquals(loot.roundRobinPlayer, player.getGUID())) {
                        return null;
                    }
                }

                if (is_blocked) {
                    return LootSlotType.RollOngoing;
                }

                if (rollWinnerGUID.isEmpty()) // all passed
                {
                    return LootSlotType.AllowLoot;
                }

                if (Objects.equals(rollWinnerGUID, player.getGUID())) {
                    return LootSlotType.owner;
                }

                return null;
            case PersonalLoot:
                return LootSlotType.owner;
            default:
                break;
        }

        return null;
    }

    public final ArrayList<ObjectGuid> getAllowedLooters() {
        return allowedGUIDs;
    }
}
