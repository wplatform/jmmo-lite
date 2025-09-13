package com.github.azeroth.game.chat;


import com.github.azeroth.game.domain.condition.DisableType;
import com.github.azeroth.game.entity.item.ItemPosCount;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnQuestStatusChange;
import com.github.azeroth.game.listener.interfaces.iquest.IQuestOnQuestStatusChange;
import game.Quest;

import java.util.ArrayList;



class QuestCommands {

    private static boolean handleQuestAdd(CommandHandler handler, Quest quest) {
        var player = handler.getSelectedPlayer();

        if (!player) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        if (global.getDisableMgr().isDisabledFor(DisableType.Quest, quest.id, null)) {
            handler.sendSysMessage(SysMessage.CommandQuestNotfound, quest.id);

            return false;
        }

        // check item starting quest (it can work incorrectly if added without item in inventory)
        var itc = global.getObjectMgr().getItemTemplates();
        var result = itc.values().FirstOrDefault(p -> p.StartQuest == quest.id);

        if (result != null) {
            handler.sendSysMessage(SysMessage.CommandQuestStartfromitem, quest.id, result.id);

            return false;
        }

        if (player.isActiveQuest(quest.id)) {
            return false;
        }

        // ok, normal (creature/GO starting) quest
        if (player.canAddQuest(quest, true)) {
            player.addQuestAndCheckCompletion(quest, null);
        }

        return true;
    }


    private static boolean handleQuestComplete(CommandHandler handler, Quest quest) {
        var player = handler.getSelectedPlayer();

        if (!player) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // If player doesn't have the quest
        if (player.getQuestStatus(quest.id) == QuestStatus.NONE || global.getDisableMgr().isDisabledFor(DisableType.Quest, quest.id, null)) {
            handler.sendSysMessage(SysMessage.CommandQuestNotfound, quest.id);

            return false;
        }

        for (var obj : quest.objectives) {
            completeObjective(player, obj);
        }

        player.completeQuest(quest.id);

        return true;
    }


    private static boolean handleQuestRemove(CommandHandler handler, Quest quest) {
        var player = handler.getSelectedPlayer();

        if (!player) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        var oldStatus = player.getQuestStatus(quest.id);

        if (oldStatus != QuestStatus.NONE) {
            // remove all quest entries for 'entry' from quest log
            for (byte slot = 0; slot < SharedConst.MaxQuestLogSize; ++slot) {
                var logQuest = player.getQuestSlotQuestId(slot);

                if (logQuest == quest.id) {
                    player.setQuestSlot(slot, 0);

                    // we ignore unequippable quest items in this case, its' still be equipped
                    player.takeQuestSourceItem(logQuest, false);

                    if (quest.hasFlag(QuestFlag.Pvp)) {
                        player.pvpInfo.isHostile = player.pvpInfo.isInHostileArea || player.hasPvPForcingQuest();
                        player.updatePvPState();
                    }
                }
            }

            player.removeActiveQuest(quest.id, false);
            player.removeRewardedQuest(quest.id);

            global.getScriptMgr().<IPlayerOnQuestStatusChange>ForEach(p -> p.OnQuestStatusChange(player, quest.id));
            global.getScriptMgr().<IQuestOnQuestStatusChange>RunScript(script -> script.OnQuestStatusChange(player, quest, oldStatus, QuestStatus.NONE), quest.getScriptId());

            handler.sendSysMessage(SysMessage.CommandQuestRemoved);

            return true;
        } else {
            handler.sendSysMessage(SysMessage.CommandQuestNotfound, quest.id);

            return false;
        }
    }


    private static boolean handleQuestReward(CommandHandler handler, Quest quest) {
        var player = handler.getSelectedPlayer();

        if (!player) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // If player doesn't have the quest
        if (player.getQuestStatus(quest.id) != QuestStatus.Complete || global.getDisableMgr().isDisabledFor(DisableType.Quest, quest.id, null)) {
            handler.sendSysMessage(SysMessage.CommandQuestNotfound, quest.id);

            return false;
        }

        player.rewardQuest(quest, lootItemType.item, 0, player);

        return true;
    }

    private static void completeObjective(Player player, QuestObjective obj) {
        switch (obj.type) {
            case Item: {
                var curItemCount = player.getItemCount((int) obj.objectID, true);
                ArrayList<ItemPosCount> dest = new ArrayList<>();
                var msg = player.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, (int) obj.objectID, (int) (obj.Amount - curItemCount));

                if (msg == InventoryResult.Ok) {
                    var item = player.storeNewItem(dest, (int) obj.objectID, true);
                    player.sendNewItem(item, (int) (obj.Amount - curItemCount), true, false);
                }

                break;
            }
            case Monster: {
                var creatureInfo = global.getObjectMgr().getCreatureTemplate((int) obj.objectID);

                if (creatureInfo != null) {
                    for (var z = 0; z < obj.amount; ++z) {
                        player.killedMonster(creatureInfo, ObjectGuid.Empty);
                    }
                }

                break;
            }
            case GameObject: {
                for (var z = 0; z < obj.amount; ++z) {
                    player.killCreditGO((int) obj.objectID);
                }

                break;
            }
            case MinReputation: {
                var curRep = player.getReputationMgr().getReputation((int) obj.objectID);

                if (curRep < obj.amount) {
                    var factionEntry = CliDB.FactionStorage.get(obj.objectID);

                    if (factionEntry != null) {
                        player.getReputationMgr().setReputation(factionEntry, obj.amount);
                    }
                }

                break;
            }
            case MaxReputation: {
                var curRep = player.getReputationMgr().getReputation((int) obj.objectID);

                if (curRep > obj.amount) {
                    var factionEntry = CliDB.FactionStorage.get(obj.objectID);

                    if (factionEntry != null) {
                        player.getReputationMgr().setReputation(factionEntry, obj.amount);
                    }
                }

                break;
            }
            case Money: {
                player.modifyMoney(obj.amount);

                break;
            }
            case PlayerKills: {
                for (var z = 0; z < obj.amount; ++z) {
                    player.killedPlayerCredit(ObjectGuid.Empty);
                }

                break;
            }
        }
    }


    private static class ObjectiveCommands {

        private static boolean handleQuestObjectiveComplete(CommandHandler handler, int objectiveId) {
            var player = handler.getSelectedPlayerOrSelf();

            if (!player) {
                handler.sendSysMessage(SysMessage.NoCharSelected);

                return false;
            }

            var obj = global.getObjectMgr().getQuestObjective(objectiveId);

            if (obj == null) {
                handler.sendSysMessage(SysMessage.QuestObjectiveNotfound);

                return false;
            }

            completeObjective(player, obj);

            return true;
        }
    }
}
