package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.item.ItemEnchantmentManager;
import com.github.azeroth.game.entity.item.ItemPosCount;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.Objects;
import java.util.locale;



class MiscAddItemCommands {

    private static boolean handleAddItemCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        int itemId = 0;

        if (args.get(0) == '[') // [name] manual form
        {
            var itemName = args.NextString("]").substring(1);

            if (!StringUtil.isEmpty(itemName)) {
                var record = CliDB.ItemSparseStorage.values().FirstOrDefault(itemSparse ->
                {
                    for (Locale i = 0; i.getValue() < locale.Total.getValue(); ++i) {
                        if (Objects.equals(itemName, itemSparse.Display[i])) {
                            return true;
                        }
                    }

                    return false;
                });

                if (record == null) {
                    handler.sendSysMessage(SysMessage.CommandCouldnotfind, itemName);

                    return false;
                }

                itemId = record.id;
            } else {
                return false;
            }
        } else // item_id or [name] Shift-click form |color|Hitem:item_id:0:0:0|h[name]|h|r
        {
            var idStr = handler.extractKeyFromLink(args, "Hitem");

            if (StringUtil.isEmpty(idStr)) {
                return false;
            }

            tangible.OutObject<Integer> tempOut_itemId = new tangible.OutObject<Integer>();
            if (!tangible.TryParseHelper.tryParseInt(idStr, tempOut_itemId)) {
                itemId = tempOut_itemId.outArgValue;
                return false;
            } else {
                itemId = tempOut_itemId.outArgValue;
            }
        }

        var count = args.NextInt32(" ");

        if (count == 0) {
            count = 1;
        }

        ArrayList<Integer> bonusListIDs = new ArrayList<>();
        var bonuses = args.NextString(" ");
        var context = args.NextString(" ");

        // semicolon separated bonuslist ids (parse them after all arguments are extracted by strtok!)
        if (!bonuses.isEmpty()) {
            var tokens = new LocalizedString();

            for (var i = 0; i < tokens.length; ++i) {
                int id;
                tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
                if (tangible.TryParseHelper.tryParseInt(tokens.get(i), tempOut_id)) {
                    id = tempOut_id.outArgValue;
                    bonusListIDs.add(id);
                } else {
                    id = tempOut_id.outArgValue;
                }
            }
        }

        var itemContext = itemContext.NONE;

        if (!context.isEmpty()) {
            itemContext = context.<itemContext>ToEnum();

            if (itemContext != itemContext.NONE && itemContext.getValue() < itemContext.max.getValue()) {
                var contextBonuses = global.getDB2Mgr().GetDefaultItemBonusTree(itemId, itemContext);
                bonusListIDs.addAll(contextBonuses);
            }
        }

        var player = handler.getSession().getPlayer();
        var playerTarget = handler.getSelectedPlayer();

        if (!playerTarget) {
            playerTarget = player;
        }

        var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

        if (itemTemplate == null) {
            handler.sendSysMessage(SysMessage.CommandItemidinvalid, itemId);

            return false;
        }

        // Subtract
        if (count < 0) {
            var destroyedItemCount = playerTarget.destroyItemCount(itemId, (int) -count, true, false);

            if (destroyedItemCount > 0) {
                // output the amount of items successfully destroyed
                handler.sendSysMessage(SysMessage.Removeitem, itemId, destroyedItemCount, handler.getNameLink(playerTarget));

                // check to see if we were unable to destroy all of the amount requested.
                var unableToDestroyItemCount = (int) (-count - destroyedItemCount);

                if (unableToDestroyItemCount > 0) {
                    // output message for the amount of items we couldn't destroy
                    handler.sendSysMessage(SysMessage.RemoveitemFailure, itemId, unableToDestroyItemCount, handler.getNameLink(playerTarget));
                }
            } else {
                // failed to destroy items of the amount requested
                handler.sendSysMessage(SysMessage.RemoveitemFailure, itemId, -count, handler.getNameLink(playerTarget));
            }

            return true;
        }

        // Adding items

        // check space and find places
        ArrayList<ItemPosCount> dest = new ArrayList<>();
        int noSpaceForCount;
        tangible.OutObject<Integer> tempOut_noSpaceForCount = new tangible.OutObject<Integer>();
        var msg = playerTarget.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, itemId, (int) count, tempOut_noSpaceForCount);
        noSpaceForCount = tempOut_noSpaceForCount.outArgValue;

        if (msg != InventoryResult.Ok) // convert to possible store amount
        {
            count -= (int) noSpaceForCount;
        }

        if (count == 0 || dest.isEmpty()) // can't add any
        {
            handler.sendSysMessage(SysMessage.ItemCannotCreate, itemId, noSpaceForCount);

            return false;
        }

        var item = playerTarget.storeNewItem(dest, itemId, true, ItemEnchantmentManager.generateItemRandomBonusListId(itemId), null, itemContext, bonusListIDs);

        // remove binding (let GM give it to another player later)
        if (player == playerTarget) {
            for (var posCount : dest) {
                var item1 = player.getItemByPos(posCount.pos);

                if (item1) {
                    item1.setBinding(false);
                }
            }
        }

        if (count > 0 && item) {
            player.sendNewItem(item, (int) count, false, true);
            handler.sendSysMessage(SysMessage.Additem, itemId, count, handler.getNameLink(playerTarget));

            if (player != playerTarget) {
                playerTarget.sendNewItem(item, (int) count, true, false);
            }
        }

        if (noSpaceForCount > 0) {
            handler.sendSysMessage(SysMessage.ItemCannotCreate, itemId, noSpaceForCount);
        }

        return true;
    }



    private static boolean handleAddItemSetCommand(CommandHandler handler, int itemSetId, String bonuses, Byte context) {
        // prevent generation all items with itemset field value '0'
        if (itemSetId == 0) {
            handler.sendSysMessage(SysMessage.NoItemsFromItemsetFound, itemSetId);

            return false;
        }

        ArrayList<Integer> bonusListIDs = new ArrayList<>();

        // semicolon separated bonuslist ids (parse them after all arguments are extracted by strtok!)
        if (!bonuses.isEmpty()) {
            var tokens = new LocalizedString();

            for (var i = 0; i < tokens.length; ++i) {
                int id;
                tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
                if (tangible.TryParseHelper.tryParseInt(tokens.get(i), tempOut_id)) {
                    id = tempOut_id.outArgValue;
                    bonusListIDs.add(id);
                } else {
                    id = tempOut_id.outArgValue;
                }
            }
        }

        var itemContext = itemContext.NONE;

        if (context != null) {
            itemContext = itemContext.forValue(context);
        }

        var player = handler.getSession().getPlayer();
        var playerTarget = handler.getSelectedPlayer();

        if (!playerTarget) {
            playerTarget = player;
        }

        Log.outDebug(LogFilter.Server, global.getObjectMgr().getSysMessage(SysMessage.Additemset), itemSetId);

        var found = false;
        var its = global.getObjectMgr().getItemTemplates();

        for (var template : its.entrySet()) {
            if (template.getValue().ItemSet != itemSetId) {
                continue;
            }

            found = true;
            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = playerTarget.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, template.getValue().id, 1);

            if (msg == InventoryResult.Ok) {
                ArrayList<Integer> bonusListIDsForItem = new ArrayList<Integer>(bonusListIDs); // copy, bonuses for each depending on context might be different for each item

                if (itemContext != itemContext.NONE && itemContext.getValue() < itemContext.max.getValue()) {
                    var contextBonuses = global.getDB2Mgr().GetDefaultItemBonusTree(template.getValue().id, itemContext);
                    bonusListIDsForItem.addAll(contextBonuses);
                }

                var item = playerTarget.storeNewItem(dest, template.getValue().id, true, 0, null, itemContext, bonusListIDsForItem);

                // remove binding (let GM give it to another player later)
                if (player == playerTarget) {
                    item.setBinding(false);
                }

                player.sendNewItem(item, 1, false, true);

                if (player != playerTarget) {
                    playerTarget.sendNewItem(item, 1, true, false);
                }
            } else {
                player.sendEquipError(msg, null, null, template.getValue().id);
                handler.sendSysMessage(SysMessage.ItemCannotCreate, template.getValue().id, 1);
            }
        }

        if (!found) {
            handler.sendSysMessage(SysMessage.CommandNoitemsetfound, itemSetId);

            return false;
        }

        return true;
    }


    private static boolean handleAddItemToCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var player = handler.getSession().getPlayer();

        Player playerTarget;
        tangible.OutObject<Player> tempOut_playerTarget = new tangible.OutObject<Player>();
        if (!handler.extractPlayerTarget(args, tempOut_playerTarget)) {
            playerTarget = tempOut_playerTarget.outArgValue;
            return false;
        } else {
            playerTarget = tempOut_playerTarget.outArgValue;
        }

        var tailArgs = new StringArguments(args.NextString(""));

        if (tailArgs.isEmpty()) {
            return false;
        }

        int itemId = 0;

        if (tailArgs.get(0) == '[') // [name] manual form
        {
            var itemNameStr = tailArgs.NextString("]");

            if (!itemNameStr.isEmpty()) {
                var itemName = itemNameStr.substring(1);

                var itr = CliDB.ItemSparseStorage.values().FirstOrDefault(sparse ->
                {
                    for (var i = locale.enUS; i.getValue() < locale.Total.getValue(); ++i) {
                        if (Objects.equals(itemName, sparse.Display[i])) {
                            return true;
                        }
                    }

                    return false;
                });

                if (itr == null) {
                    handler.sendSysMessage(SysMessage.CommandCouldnotfind, itemName);

                    return false;
                }

                itemId = itr.id;
            } else {
                return false;
            }
        } else // item_id or [name] Shift-click form |color|Hitem:item_id:0:0:0|h[name]|h|r
        {
            var id = handler.extractKeyFromLink(tailArgs, "Hitem");

            if (id.isEmpty()) {
                return false;
            }

            itemId = Integer.parseInt(id);
        }

        var ccount = tailArgs.NextString(" ");

        var count = 1;

        if (!ccount.isEmpty()) {
            count = Integer.parseInt(ccount);
        }

        if (count == 0) {
            count = 1;
        }

        ArrayList<Integer> bonusListIDs = new ArrayList<>();
        var bonuses = tailArgs.NextString(" ");

        var context = tailArgs.NextString(" ");

        var itemContext = itemContext.NONE;

        if (!context.isEmpty()) {
            itemContext = context.<itemContext>ToEnum();

            if (itemContext != itemContext.NONE && itemContext.getValue() < itemContext.max.getValue()) {
                var contextBonuses = global.getDB2Mgr().GetDefaultItemBonusTree(itemId, itemContext);
                bonusListIDs.addAll(contextBonuses);
            }
        }

        // semicolon separated bonuslist ids
        if (!bonuses.isEmpty()) {
            for (var token : bonuses.split(';', StringSplitOptions.RemoveEmptyEntries)) {
                int bonusListId;
                tangible.OutObject<Integer> tempOut_bonusListId = new tangible.OutObject<Integer>();
                if (tangible.TryParseHelper.tryParseInt(token, tempOut_bonusListId)) {
                    bonusListId = tempOut_bonusListId.outArgValue;
                    bonusListIDs.add(bonusListId);
                } else {
                    bonusListId = tempOut_bonusListId.outArgValue;
                }
            }
        }

        var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

        if (itemTemplate == null) {
            handler.sendSysMessage(SysMessage.CommandItemidinvalid, itemId);

            return false;
        }

        // Subtract
        if (count < 0) {
            var destroyedItemCount = playerTarget.destroyItemCount(itemId, (int) -count, true, false);

            if (destroyedItemCount > 0) {
                // output the amount of items successfully destroyed
                handler.sendSysMessage(SysMessage.Removeitem, itemId, destroyedItemCount, handler.getNameLink(playerTarget));

                // check to see if we were unable to destroy all of the amount requested.
                var unableToDestroyItemCount = (int) (-count - destroyedItemCount);

                if (unableToDestroyItemCount > 0) {
                    // output message for the amount of items we couldn't destroy
                    handler.sendSysMessage(SysMessage.RemoveitemFailure, itemId, unableToDestroyItemCount, handler.getNameLink(playerTarget));
                }
            } else {
                // failed to destroy items of the amount requested
                handler.sendSysMessage(SysMessage.RemoveitemFailure, itemId, -count, handler.getNameLink(playerTarget));
            }

            return true;
        }

        // Adding items

        // check space and find places
        ArrayList<ItemPosCount> dest = new ArrayList<>();
        int noSpaceForCount;
        tangible.OutObject<Integer> tempOut_noSpaceForCount = new tangible.OutObject<Integer>();
        var msg = playerTarget.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, itemId, (int) count, tempOut_noSpaceForCount);
        noSpaceForCount = tempOut_noSpaceForCount.outArgValue;

        if (msg != InventoryResult.Ok) // convert to possible store amount
        {
            count -= (int) noSpaceForCount;
        }

        if (count == 0 || dest.isEmpty()) // can't add any
        {
            handler.sendSysMessage(SysMessage.ItemCannotCreate, itemId, noSpaceForCount);

            return false;
        }

        var item = playerTarget.storeNewItem(dest, itemId, true, ItemEnchantmentManager.generateItemRandomBonusListId(itemId), null, itemContext, bonusListIDs);

        // remove binding (let GM give it to another player later)
        if (player == playerTarget) {
            for (var itemPostCount : dest) {
                var item1 = player.getItemByPos(itemPostCount.pos);

                if (item1 != null) {
                    item1.setBinding(false);
                }
            }
        }

        if (count > 0 && item) {
            player.sendNewItem(item, (int) count, false, true);

            if (player != playerTarget) {
                playerTarget.sendNewItem(item, (int) count, true, false);
            }
        }

        if (noSpaceForCount > 0) {
            handler.sendSysMessage(SysMessage.ItemCannotCreate, itemId, noSpaceForCount);
        }

        return true;
    }
}
