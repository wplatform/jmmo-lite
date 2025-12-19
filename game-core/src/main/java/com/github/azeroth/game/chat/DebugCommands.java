package com.github.azeroth.game.chat;


import com.github.azeroth.game.WorldSafeLocsEntry;
import com.github.azeroth.game.entity.gobject.transport;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.CreatureSearcher;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.networking.packet.CastFailed;
import com.github.azeroth.game.networking.packet.ChannelNotify;
import com.github.azeroth.game.networking.packet.ChatPkt;
import com.github.azeroth.game.networking.packet.moveUpdate;
import com.github.azeroth.game.listener.interfaces.iitem.IItemOnExpire;


import java.util.Objects;

class DebugCommands {

    private static boolean handleDebugAnimCommand(CommandHandler handler, Emote emote) {
        var unit = handler.getSelectedUnit();

        if (unit) {
            unit.handleEmoteCommand(emote);
        }

        handler.sendSysMessage(String.format("Playing emote %1$s", emote));

        return true;
    }


    private static boolean handleDebugAreaTriggersCommand(CommandHandler handler) {
        var player = handler.getPlayer();

        if (!player.isDebugAreaTriggers()) {
            handler.sendSysMessage(SysMessage.DebugAreatriggerOn);
            player.setDebugAreaTriggers(true);
        } else {
            handler.sendSysMessage(SysMessage.DebugAreatriggerOff);
            player.setDebugAreaTriggers(false);
        }

        return true;
    }


    private static boolean handleDebugArenaCommand(CommandHandler handler) {
        global.getBattlegroundMgr().toggleArenaTesting();

        return true;
    }


    private static boolean handleDebugBattlegroundCommand(CommandHandler handler) {
        global.getBattlegroundMgr().toggleTesting();

        return true;
    }


    private static boolean handleDebugBoundaryCommand(CommandHandler handler, String fill, int durationArg) {
        var player = handler.getPlayer();

        if (!player) {
            return false;
        }

        var target = handler.getSelectedCreature();

        if (!target || !target.isAIEnabled()) {
            return false;
        }

        var duration = durationArg != 0 ? duration.FromSeconds(durationArg) : duration.Zero;

        if (duration <= duration.Zero || duration >= durationofMinutes(30)) // arbitrary upper limit
        {
            duration = durationofMinutes(3);
        }

        var errMsg = target.getAi().visualizeBoundary(duration, player, Objects.equals(fill, "fill"));

        if (errMsg.getValue() > 0) {
            handler.sendSysMessage(errMsg);
        }

        return true;
    }


    private static boolean handleDebugCombatListCommand(CommandHandler handler) {
        var target = handler.getSelectedUnit();

        if (!target) {
            target = handler.getPlayer();
        }

        handler.sendSysMessage(String.format("Combat refs: (Combat state: %1$s | Manager state: %2$s)", target.isInCombat(), target.getCombatManager().getHasCombat()));

        for (var refe : target.getCombatManager().getPvPCombatRefs().entrySet()) {
            var unit = refe.getValue().getOther(target);
            handler.sendSysMessage(String.format("[PvP] %1$s (SpawnID %2$s)", unit.getName(), (unit.IsCreature ? unit.AsCreature.SpawnId : 0)));
        }

        for (var refe : target.getCombatManager().getPvECombatRefs().entrySet()) {
            var unit = refe.getValue().getOther(target);
            handler.sendSysMessage(String.format("[PvE] %1$s (SpawnID %2$s)", unit.getName(), (unit.IsCreature ? unit.AsCreature.SpawnId : 0)));
        }

        return true;
    }


    private static boolean handleDebugConversationCommand(CommandHandler handler, int conversationEntry) {
        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        return conversation.CreateConversation(conversationEntry, target, target.getLocation(), target.getGUID()) != null;
    }


    private static boolean handleDebugDummyCommand(CommandHandler handler) {
        handler.sendSysMessage("This command does nothing right now. Edit your local core (DebugCommands.cs) to make it do whatever you need for testing.");

        return true;
    }



    private static boolean handleDebugEnterVehicleCommand(CommandHandler handler, int entry) {
        return handleDebugEnterVehicleCommand(handler, entry, -1);
    }

    private static boolean handleDebugEnterVehicleCommand(CommandHandler handler, int entry, byte seatId) {
        var target = handler.getSelectedUnit();

        if (!target || !target.isVehicle()) {
            return false;
        }

        if (entry == 0) {
            handler.getPlayer().enterVehicle(target, seatId);
        } else {
            var check = new AllCreaturesOfEntryInRange(handler.getPlayer(), entry, 20.0f);
            var searcher = new CreatureSearcher(handler.getPlayer(), check, gridType.All);
            Cell.visitGrid(handler.getPlayer(), searcher, 30.0f);
            var passenger = searcher.getTarget();

            if (!passenger || passenger == target) {
                return false;
            }

            passenger.enterVehicle(target, seatId);
        }

        handler.sendSysMessage("Unit {0} entered vehicle {1}", entry, seatId);

        return true;
    }


    private static boolean handleDebugGetItemStateCommand(CommandHandler handler, String itemState) {
        var state = ItemUpdateState.Unchanged;
        var listQueue = false;
        var checkAll = false;

        if (Objects.equals(itemState, "unchanged")) {
            state = ItemUpdateState.Unchanged;
        } else if (Objects.equals(itemState, "changed")) {
            state = ItemUpdateState.changed;
        } else if (Objects.equals(itemState, "new")) {
            state = ItemUpdateState.New;
        } else if (Objects.equals(itemState, "removed")) {
            state = ItemUpdateState.removed;
        } else if (Objects.equals(itemState, "queue")) {
            listQueue = true;
        } else if (Objects.equals(itemState, "check_all")) {
            checkAll = true;
        } else {
            return false;
        }

        var player = handler.getSelectedPlayer();

        if (!player) {
            player = handler.getPlayer();
        }

        if (!listQueue && !checkAll) {
            itemState = "The player has the following " + itemState + " items: ";
            handler.sendSysMessage(itemState);

            for (byte i = PlayerSlot.start.getValue(); i < PlayerSlot.End.getValue(); ++i) {
                if (i >= InventorySlots.BuyBackStart && i < InventorySlots.BuyBackEnd) {
                    continue;
                }

                var item = player.getItemByPos(InventorySlots.Bag0, i);

                if (item) {
                    var bag = item.getAsBag();

                    if (bag) {
                        for (byte j = 0; j < bag.getBagSize(); ++j) {
                            var item2 = bag.getItemByPos(j);

                            if (item2) {
                                if (item2.getState() == state) {
                                    handler.sendSysMessage("bag: 255 slot: {0} guid: {1} owner: {2}", item2.getSlot(), item2.getGUID().toString(), item2.getOwnerGUID().toString());
                                }
                            }
                        }
                    } else if (item.getState() == state) {
                        handler.sendSysMessage("bag: 255 slot: {0} guid: {1} owner: {2}", item.getSlot(), item.getGUID().toString(), item.getOwnerGUID().toString());
                    }
                }
            }
        }

        if (listQueue) {
            var updateQueue = player.getItemUpdateQueue();

            for (var i = 0; i < updateQueue.size(); ++i) {
                var item = updateQueue.get(i);

                if (!item) {
                    continue;
                }

                var container = item.getContainer();
                var bagSlot = container ? container.getSlot() : InventorySlots.Bag0;

                var st = "";

                switch (item.getState()) {
                    case Unchanged:
                        st = "unchanged";

                        break;
                    case Changed:
                        st = "changed";

                        break;
                    case New:
                        st = "new";

                        break;
                    case Removed:
                        st = "removed";

                        break;
                }

                handler.sendSysMessage("bag: {0} slot: {1} guid: {2} - state: {3}", bagSlot, item.getSlot(), item.getGUID().toString(), st);
            }

            if (updateQueue.isEmpty()) {
                handler.sendSysMessage("The player's updatequeue is empty");
            }
        }

        if (checkAll) {
            var error = false;
            var updateQueue = player.getItemUpdateQueue();

            for (byte i = PlayerSlot.start.getValue(); i < PlayerSlot.End.getValue(); ++i) {
                if (i >= InventorySlots.BuyBackStart && i < InventorySlots.BuyBackEnd) {
                    continue;
                }

                var item = player.getItemByPos(InventorySlots.Bag0, i);

                if (!item) {
                    continue;
                }

                if (item.getSlot() != i) {
                    handler.sendSysMessage("Item with slot {0} and guid {1} has an incorrect slot value: {2}", i, item.getGUID().toString(), item.getSlot());
                    error = true;

                    continue;
                }

                if (ObjectGuid.opNotEquals(item.getOwnerGUID(), player.getGUID())) {
                    handler.sendSysMessage("The item with slot {0} and itemguid {1} does have non-matching owner guid ({2}) and player guid ({3}) !", item.getSlot(), item.getGUID().toString(), item.getOwnerGUID().toString(), player.getGUID().toString());
                    error = true;

                    continue;
                }

                var container = item.getContainer();

                if (container) {
                    handler.sendSysMessage("The item with slot {0} and guid {1} has a container (slot: {2}, guid: {3}) but shouldn't!", item.getSlot(), item.getGUID().toString(), container.getSlot(), container.getGUID().toString());
                    error = true;

                    continue;
                }

                if (item.isInUpdateQueue()) {
                    var qp = (short) item.getQueuePos();

                    if (qp > updateQueue.size()) {
                        handler.sendSysMessage("The item with slot {0} and guid {1} has its queuepos ({2}) larger than the update queue size! ", item.getSlot(), item.getGUID().toString(), qp);
                        error = true;

                        continue;
                    }

                    if (updateQueue.get(qp) == null) {
                        handler.sendSysMessage("The item with slot {0} and guid {1} has its queuepos ({2}) pointing to NULL in the queue!", item.getSlot(), item.getGUID().toString(), qp);
                        error = true;

                        continue;
                    }

                    if (!updateQueue.get(qp).equals(item)) {
                        handler.sendSysMessage("The item with slot {0} and guid {1} has a queuepos ({2}) that points to another item in the queue (bag: {3}, slot: {4}, guid: {5})", item.getSlot(), item.getGUID().toString(), qp, updateQueue.get(qp).getBagSlot(), updateQueue.get(qp).getSlot(), updateQueue.get(qp).getGUID().toString());
                        error = true;

                        continue;
                    }
                } else if (item.getState() != ItemUpdateState.Unchanged) {
                    handler.sendSysMessage("The item with slot {0} and guid {1} is not in queue but should be (state: {2})!", item.getSlot(), item.getGUID().toString(), item.getState());
                    error = true;

                    continue;
                }

                var bag = item.getAsBag();

                if (bag) {
                    for (byte j = 0; j < bag.getBagSize(); ++j) {
                        var item2 = bag.getItemByPos(j);

                        if (!item2) {
                            continue;
                        }

                        if (item2.getSlot() != j) {
                            handler.sendSysMessage("The item in bag {0} and slot {1} (guid: {2}) has an incorrect slot value: {3}", bag.getSlot(), j, item2.getGUID().toString(), item2.getSlot());
                            error = true;

                            continue;
                        }

                        if (ObjectGuid.opNotEquals(item2.getOwnerGUID(), player.getGUID())) {
                            handler.sendSysMessage("The item in bag {0} at slot {1} and with itemguid {2}, the owner's guid ({3}) and the player's guid ({4}) don't match!", bag.getSlot(), item2.getSlot(), item2.getGUID().toString(), item2.getOwnerGUID().toString(), player.getGUID().toString());
                            error = true;

                            continue;
                        }

                        var container1 = item2.getContainer();

                        if (!container1) {
                            handler.sendSysMessage("The item in bag {0} at slot {1} with guid {2} has no container!", bag.getSlot(), item2.getSlot(), item2.getGUID().toString());
                            error = true;

                            continue;
                        }

                        if (container1 != bag) {
                            handler.sendSysMessage("The item in bag {0} at slot {1} with guid {2} has a different container(slot {3} guid {4})!", bag.getSlot(), item2.getSlot(), item2.getGUID().toString(), container1.getSlot(), container1.getGUID().toString());
                            error = true;

                            continue;
                        }

                        if (item2.isInUpdateQueue()) {
                            var qp = (short) item2.getQueuePos();

                            if (qp > updateQueue.size()) {
                                handler.sendSysMessage("The item in bag {0} at slot {1} having guid {2} has a queuepos ({3}) larger than the update queue size! ", bag.getSlot(), item2.getSlot(), item2.getGUID().toString(), qp);
                                error = true;

                                continue;
                            }

                            if (updateQueue.get(qp) == null) {
                                handler.sendSysMessage("The item in bag {0} at slot {1} having guid {2} has a queuepos ({3}) that points to NULL in the queue!", bag.getSlot(), item2.getSlot(), item2.getGUID().toString(), qp);
                                error = true;

                                continue;
                            }

                            if (!updateQueue.get(qp).equals(item2)) {
                                handler.sendSysMessage("The item in bag {0} at slot {1} having guid {2} has a queuepos ({3}) that points to another item in the queue (bag: {4}, slot: {5}, guid: {6})", bag.getSlot(), item2.getSlot(), item2.getGUID().toString(), qp, updateQueue.get(qp).getBagSlot(), updateQueue.get(qp).getSlot(), updateQueue.get(qp).getGUID().toString());
                                error = true;

                                continue;
                            }
                        } else if (item2.getState() != ItemUpdateState.Unchanged) {
                            handler.sendSysMessage("The item in bag {0} at slot {1} having guid {2} is not in queue but should be (state: {3})!", bag.getSlot(), item2.getSlot(), item2.getGUID().toString(), item2.getState());
                            error = true;

                            continue;
                        }
                    }
                }
            }

            for (var i = 0; i < updateQueue.size(); ++i) {
                var item = updateQueue.get(i);

                if (!item) {
                    continue;
                }

                if (ObjectGuid.opNotEquals(item.getOwnerGUID(), player.getGUID())) {
                    handler.sendSysMessage("queue({0}): For the item with guid {0}, the owner's guid ({1}) and the player's guid ({2}) don't match!", i, item.getGUID().toString(), item.getOwnerGUID().toString(), player.getGUID().toString());
                    error = true;

                    continue;
                }

                if (item.getQueuePos() != i) {
                    handler.sendSysMessage("queue({0}): For the item with guid {1}, the queuepos doesn't match it's position in the queue!", i, item.getGUID().toString());
                    error = true;

                    continue;
                }

                if (item.getState() == ItemUpdateState.removed) {
                    continue;
                }

                var test = player.getItemByPos(item.getBagSlot(), item.getSlot());

                if (test == null) {
                    handler.sendSysMessage("queue({0}): The bag({1}) and slot({2}) values for the item with guid {3} are incorrect, the player doesn't have any item at that position!", i, item.getBagSlot(), item.getSlot(), item.getGUID().toString());
                    error = true;

                    continue;
                }

                if (test != item) {
                    handler.sendSysMessage("queue({0}): The bag({1}) and slot({2}) values for the item with guid {3} are incorrect, an item which guid is {4} is there instead!", i, item.getBagSlot(), item.getSlot(), item.getGUID().toString(), test.getGUID().toString());
                    error = true;

                    continue;
                }
            }

            if (!error) {
                handler.sendSysMessage("All OK!");
            }
        }

        return true;
    }


    private static boolean handleDebugGuidLimitsCommand(CommandHandler handler, int mapId) {
        if (mapId != 0) {
            global.getMapMgr().DoForAllMapsWithMapId(mapId, map -> handleDebugGuidLimitsMap(handler, map));
        } else {
            global.getMapMgr().DoForAllMaps(map -> handleDebugGuidLimitsMap(handler, map));
        }

        handler.sendSysMessage(String.format("Guid Warn Level: %1$s", WorldConfig.getIntValue(WorldCfg.RespawnGuidWarnLevel)));
        handler.sendSysMessage(String.format("Guid Alert Level: %1$s", WorldConfig.getIntValue(WorldCfg.RespawnGuidAlertLevel)));

        return true;
    }



    private static boolean handleDebugInstanceSpawns(CommandHandler handler, Object optArg) {
        var player = handler.getPlayer();

        if (player == null) {
            return false;
        }

        var explain = false;
        int groupID = 0;

        if (optArg instanceof String && (optArg instanceof String ? (String) optArg : null).Equals("explain", StringComparison.OrdinalIgnoreCase)) {
            explain = true;
        } else {
            groupID = (int) optArg;
        }

        if (groupID != 0 && global.getObjectMgr().getSpawnGroupData(groupID) == null) {
            handler.sendSysMessage(String.format("There is no spawn group with ID %1$s.", groupID));

            return false;
        }

        var map = player.getMap();
        var mapName = map.getMapName();
        var instance = player.getInstanceScript();

        if (instance == null) {
            handler.sendSysMessage(String.format("%1$s has no instance script.", mapName));

            return false;
        }

        var spawnGroups = instance.getInstanceSpawnGroups();

        if (spawnGroups.isEmpty()) {
            handler.sendSysMessage(String.format("%1$s's instance script does not manage any spawn groups.", mapName));

            return false;
        }

        MultiMap<Integer, Tuple<Boolean, Byte, Byte>> store = new MultiMap<Integer, Tuple<Boolean, Byte, Byte>>();

        for (var info : spawnGroups) {
            if (groupID != 0 && info.spawnGroupId != groupID) {
                continue;
            }

            boolean isSpawn;

            if (info.flags.hasFlag(InstanceSpawnGroupFlags.BlockSpawn)) {
                isSpawn = false;
            } else if (info.flags.hasFlag(InstanceSpawnGroupFlags.ActivateSpawn)) {
                isSpawn = true;
            } else {
                continue;
            }

            store.add(info.spawnGroupId, Tuple.create(isSpawn, info.bossStateId, info.bossStates));
        }

        if (groupID != 0 && !store.ContainsKey(groupID)) {
            handler.sendSysMessage(String.format("%1$s's instance script does not manage group '%2$s'.", mapName, global.getObjectMgr().getSpawnGroupData(groupID).getName()));

            return false;
        }

        if (groupID == 0) {
            handler.sendSysMessage(String.format("Spawn groups managed by %1$s (%2$s):", mapName, map.getId()));
        }

        for (var key : store.keySet()) {
            var groupData = global.getObjectMgr().getSpawnGroupData(key);

            if (groupData == null) {
                continue;
            }

            if (explain) {
                handler.sendSysMessage(" |-- '{}' ({})", groupData.getName(), key);
                boolean isBlocked = false, isSpawned = false;

                for (var tuple : store.get(key)) {
                    var isSpawn = tuple.Item1;
                    var bossStateId = tuple.item2;
                    var actualState = instance.getBossState(bossStateId);

                    if ((tuple.Item3 & (1 << actualState.getValue())) != 0) {
                        if (isSpawn) {
                            isSpawned = true;

                            if (isBlocked) {
                                handler.sendSysMessage(String.format(" | |-- '%1$s' would be allowed to spawn by boss state %2$s being %3$s, but this is overruled", groupData.getName(), bossStateId, EncounterState.forValue(actualState)));
                            } else {
                                handler.sendSysMessage(String.format(" | |-- '%1$s' is allowed to spawn because boss state %2$s is %3$s.", groupData.getName(), bossStateId, EncounterState.forValue(bossStateId)));
                            }
                        } else {
                            isBlocked = true;
                            handler.sendSysMessage(String.format(" | |-- '%1$s' is blocked from spawning because boss state %2$s is %3$s.", groupData.getName(), bossStateId, EncounterState.forValue(bossStateId)));
                        }
                    } else {
                        handler.sendSysMessage(String.format(" | |-- '%1$s' could've been %2$s if boss state %3$s matched mask 0x%4$.2X; but it is %5$s . 0x%6$.2X, which does not match.", groupData.getName(), (isSpawn ? "allowed to spawn" : "blocked from spawning"), bossStateId, tuple.Item3, EncounterState.forValue(actualState), (1 << actualState.getValue())));
                    }
                }

                if (isBlocked) {
                    handler.sendSysMessage(String.format(" | |=> '%1$s' is not active due to a blocking rule being matched", groupData.getName()));
                } else if (isSpawned) {
                    handler.sendSysMessage(String.format(" | |=> '%1$s' is active due to a spawn rule being matched", groupData.getName()));
                } else {
                    handler.sendSysMessage(String.format(" | |=> '%1$s' is not active due to none of its rules being matched", groupData.getName()));
                }
            } else {
                handler.sendSysMessage(String.format(" - '%1$s' (%2$s) is %3$sactive", groupData.getName(), key, (map.isSpawnGroupActive(key) ? "" : "not ")));
            }
        }

        return true;
    }


    private static boolean handleDebugItemExpireCommand(CommandHandler handler, long guid) {
        var item = handler.getPlayer().getItemByGuid(ObjectGuid.create(HighGuid.Item, guid));

        if (!item) {
            return false;
        }

        handler.getPlayer().destroyItem(item.getBagSlot(), item.getSlot(), true);
        var itemTemplate = item.getTemplate();
        global.getScriptMgr().<IItemOnExpire>RunScriptRet(p -> p.OnExpire(handler.getPlayer(), itemTemplate), itemTemplate.getScriptId());

        return true;
    }


    private static boolean handleDebugLoadCellsCommand(CommandHandler handler, Integer mapId, Integer tileX, Integer tileY) {
        if (mapId != null) {
            global.getMapMgr().DoForAllMapsWithMapId(mapId.intValue(), map -> handleDebugLoadCellsCommandHelper(handler, map, tileX, tileY));

            return true;
        }

        var player = handler.getPlayer();

        if (player != null) {
            // Fallback to player's map if no map has been specified
            return handleDebugLoadCellsCommandHelper(handler, player.getMap(), tileX, tileY);
        }

        return false;
    }


    private static boolean handleDebugLoadCellsCommandHelper(CommandHandler handler, Map map, Integer tileX, Integer tileY) {
        if (!map) {
            return false;
        }

        // Load 1 single tile if specified, otherwise load the whole map
        if (tileX != null && tileY != null) {
            handler.sendSysMessage(String.format("Loading cell (mapId: %1$s tile: %2$s, %3$s). Current GameObjects %4$s, Creatures %5$s", map.getId(), tileX, tileY, map.getObjectsStore().size()
            (p -> p.Value instanceof gameObject), map.getObjectsStore().size() (p -> p.Value instanceof CREATURE)));

            // Some unit convertions to go from TileXY to GridXY to WorldXY
            var x = (((float) (64 - 1 - tileX.intValue()) - 0.5f - MapDefine.CenterGridId) * MapDefine.SizeofGrids) + (MapDefine.CenterGridOffset * 2);
            var y = (((float) (64 - 1 - tileY.intValue()) - 0.5f - MapDefine.CenterGridId) * MapDefine.SizeofGrids) + (MapDefine.CenterGridOffset * 2);
            map.loadGrid(x, y);

            handler.sendSysMessage(String.format("Cell loaded (mapId: %1$s tile: %2$s, %3$s) After load - GameObject %4$s, Creatures %5$s", map.getId(), tileX, tileY, map.getObjectsStore().size()
            (p -> p.Value instanceof gameObject), map.getObjectsStore().size() (p -> p.Value instanceof CREATURE)));
        } else {
            handler.sendSysMessage(String.format("Loading all cells (mapId: %1$s). Current GameObjects %2$s, Creatures %3$s", map.getId(), map.getObjectsStore().size()
            (p -> p.Value instanceof gameObject), map.getObjectsStore().size() (p -> p.Value instanceof CREATURE)));

            map.loadAllCells();

            handler.sendSysMessage(String.format("Cells loaded (mapId: %1$s) After load - GameObject %2$s, Creatures %3$s", map.getId(), map.getObjectsStore().size()
            (p -> p.Value instanceof gameObject), map.getObjectsStore().size() (p -> p.Value instanceof CREATURE)));
        }

        return true;
    }


    private static boolean handleDebugGetLootRecipientCommand(CommandHandler handler) {
        var target = handler.getSelectedCreature();

        if (!target) {
            return false;
        }

        handler.sendSysMessage(String.format("Loot recipients for creature %1$s (%2$s, SpawnID %3$s) are:", target.getName(), target.getGUID(), target.getSpawnId()));

        for (var tapperGuid : target.getTapList()) {
            var tapper = global.getObjAccessor().getPlayer(target, tapperGuid);
            handler.sendSysMessage(String.format("* %1$s", (tapper != null ? tapper.getName() : "offline")));
        }

        return true;
    }


    private static boolean handleDebugLoSCommand(CommandHandler handler) {
        var unit = handler.getSelectedUnit();

        if (unit) {
            var player = handler.getPlayer();
            handler.sendSysMessage(String.format("Checking LoS %1$s . %2$s:", player.getName(), unit.getName()));
            handler.sendSysMessage(String.format("    VMAP LoS: %1$s", (player.isWithinLOSInMap(unit, LineOfSightChecks.Vmap) ? "clear" : "obstructed")));
            handler.sendSysMessage(String.format("    GObj LoS: %1$s", (player.isWithinLOSInMap(unit, LineOfSightChecks.Gobject) ? "clear" : "obstructed")));
            handler.sendSysMessage(String.format("%1$s is %2$sin line of sight of %3$s.", unit.getName(), (player.isWithinLOSInMap(unit) ? "" : "not "), player.getName()));

            return true;
        }

        return false;
    }


    private static boolean handleDebugMoveflagsCommand(CommandHandler handler, Integer moveFlags, Integer moveFlagsExtra) {
        var target = handler.getSelectedUnit();

        if (!target) {
            target = handler.getPlayer();
        }

        if (moveFlags == null) {
            //! Display case
            handler.sendSysMessage(SysMessage.MoveflagsGet, target.getUnitMovementFlags(), target.getUnitMovementFlags2());
        } else {
            // @fixme: port master's HandleDebugMoveflagsCommand; flags need different handling

            target.setUnitMovementFlags(MovementFlag.forValue(moveFlags));

            if (moveFlagsExtra != null) {
                target.setUnitMovementFlags2(MovementFlag2.forValue(moveFlagsExtra));
            }

            if (!target.isTypeId(TypeId.PLAYER)) {
                target.destroyForNearbyPlayers(); // Force new SMSG_UPDATE_OBJECT:CreateObject
            } else {
                MoveUpdate moveUpdate = new moveUpdate();
                moveUpdate.status = target.getMovementInfo();
                target.sendMessageToSet(moveUpdate, true);
            }

            handler.sendSysMessage(SysMessage.MoveflagsSet, target.getUnitMovementFlags(), target.getUnitMovementFlags2());
        }

        return true;
    }


    private static boolean handleDebugNearGraveyard(CommandHandler handler, String linked) {
        var player = handler.getPlayer();
        WorldSafeLocsEntry nearestLoc = null;

        if (Objects.equals(linked, "linked")) {
            var bg = player.getBattleground();

            if (bg) {
                nearestLoc = bg.getClosestGraveYard(player);
            } else {
                var bf = global.getBattleFieldMgr().getBattlefieldToZoneId(player.getMap(), player.getZoneId());

                if (bf != null) {
                    nearestLoc = bf.getClosestGraveYard(player);
                } else {
                    nearestLoc = global.getObjectMgr().getClosestGraveYard(player.getLocation(), player.getTeam(), player);
                }
            }
        } else {
            var x = player.getLocation().getX();
            var y = player.getLocation().getY();
            var z = player.getLocation().getZ();
            var distNearest = Float.MAX_VALUE;

            for (var pair : global.getObjectMgr().getWorldSafeLocs().entrySet()) {
                var worldSafe = pair.getValue();

                if (worldSafe.loc.mapId == player.getLocation().getMapId()) {
                    var dist = (worldSafe.loc.X - x) * (worldSafe.loc.X - x) + (worldSafe.loc.Y - y) * (worldSafe.loc.Y - y) + (worldSafe.loc.Z - z) * (worldSafe.loc.Z - z);

                    if (dist < distNearest) {
                        distNearest = dist;
                        nearestLoc = worldSafe;
                    }
                }
            }
        }

        if (nearestLoc != null) {
            handler.sendSysMessage(SysMessage.CommandNearGraveyard, nearestLoc.id, nearestLoc.loc.getX(), nearestLoc.loc.getY(), nearestLoc.loc.getZ());
        } else {
            handler.sendSysMessage(SysMessage.CommandNearGraveyardNotfound);
        }

        return true;
    }


    private static boolean handleDebugObjectCountCommand(CommandHandler handler, Integer mapId) {

//		void HandleDebugObjectCountMap(Map map)
//			{
//				handler.sendSysMessage(string.format("Map Id: {0} Name: '{1}' Instance Id: {2} Creatures: {3} GameObjects: {4} SetActive Objects: {5}", map.id, map.MapName, map.instanceId, map.ObjectsStore.OfType<Creature>().count(), map.ObjectsStore.OfType<GameObject>().count(), map.ActiveNonPlayersCount));
//
//				Dictionary<uint, uint> creatureIds = new();
//
//				foreach (var p in map.ObjectsStore)
//					if (p.value.IsCreature)
//					{
//						if (!creatureIds.ContainsKey(p.value.entry))
//							creatureIds[p.value.Entry] = 0;
//
//						creatureIds[p.value.Entry]++;
//					}
//
//				var orderedCreatures = creatureIds.OrderBy(p => p.value).where(p => p.value > 5);
//
//				handler.sendSysMessage("Top Creatures count:");
//
//				foreach (var p in orderedCreatures)
//					handler.sendSysMessage(string.format("Entry: {0} Count: {1}", p.key, p.value));
//			}

        if (mapId != null) {
            global.getMapMgr().DoForAllMapsWithMapId(mapId.intValue(), map -> HandleDebugObjectCountMap(map));
        } else {
            global.getMapMgr().DoForAllMaps(map -> HandleDebugObjectCountMap(map));
        }

        return true;
    }


    private static boolean handleDebugPhaseCommand(CommandHandler handler) {
        var target = handler.getSelectedUnit();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        if (target.getDBPhase() > 0) {
            handler.sendSysMessage(String.format("Target creature's PhaseId in DB: %1$s", target.getDBPhase()));
        } else if (target.getDBPhase() < 0) {
            handler.sendSysMessage(String.format("Target creature's PhaseGroup in DB: %1$s", Math.abs(target.getDBPhase())));
        }

        PhasingHandler.printToChat(handler, target);

        return true;
    }


    private static boolean handleDebugWarModeBalanceCommand(CommandHandler handler, String command, Integer rewardValue) {
        // USAGE: .debug pvp fb <alliance|horde|neutral|off> [pct]
        // neutral     Sets faction balance off.
        // alliance    Set faction balance to alliance.
        // horde       Set faction balance to horde.
        // off         Reset the faction balance and use the calculated value of it
        switch (command) {
            case "alliance":
                global.getWorldMgr().setForcedWarModeFactionBalanceState(TeamId.ALLIANCE, (rewardValue == null ? 0 : rewardValue.intValue()));

                break;
            case "horde":
                global.getWorldMgr().setForcedWarModeFactionBalanceState(TeamId.HORDE, (rewardValue == null ? 0 : rewardValue.intValue()));

                break;
            case "neutral":
                global.getWorldMgr().setForcedWarModeFactionBalanceState(TeamIds.Neutral);

                break;
            case "off":
                global.getWorldMgr().disableForcedWarModeFactionBalanceState();

                break;
            default:
                handler.sendSysMessage(SysMessage.BadValue);

                return false;
        }

        return true;
    }


    private static boolean handleDebugQuestResetCommand(CommandHandler handler, String arg) {
        boolean daily = false, weekly = false, monthly = false;

        if (Objects.equals(arg, "ALL")) {
            daily = weekly = monthly = true;
        } else if (Objects.equals(arg, "DAILY")) {
            daily = true;
        } else if (Objects.equals(arg, "WEEKLY")) {
            weekly = true;
        } else if (Objects.equals(arg, "MONTHLY")) {
            monthly = true;
        } else {
            return false;
        }

        var now = GameTime.getGameTime();

        if (daily) {
            global.getWorldMgr().dailyReset();
            handler.sendSysMessage(String.format("Daily quests have been reset. Next scheduled reset: %1$s", time.UnixTimeToDateTime(global.getWorldMgr().getPersistentWorldVariable(WorldManager.NEXTDAILYQUESTRESETTIMEVARID)).ToShortTimeString()));
        }

        if (weekly) {
            global.getWorldMgr().resetWeeklyQuests();
            handler.sendSysMessage(String.format("Weekly quests have been reset. Next scheduled reset: %1$s", time.UnixTimeToDateTime(global.getWorldMgr().getPersistentWorldVariable(WorldManager.NEXTWEEKLYQUESTRESETTIMEVARID)).ToShortTimeString()));
        }

        if (monthly) {
            global.getWorldMgr().resetMonthlyQuests();
            handler.sendSysMessage(String.format("Monthly quests have been reset. Next scheduled reset: %1$s", time.UnixTimeToDateTime(global.getWorldMgr().getPersistentWorldVariable(WorldManager.NEXTMONTHLYQUESTRESETTIMEVARID)).ToShortTimeString()));
        }

        return true;
    }


    private static boolean handleDebugRaidResetCommand(CommandHandler handler, int mapId, int difficulty) {
        var mEntry = CliDB.MapStorage.get(mapId);

        if (mEntry == null) {
            handler.sendSysMessage("Invalid map specified.");

            return true;
        }

        if (!mEntry.IsDungeon()) {
            handler.sendSysMessage(String.format("'%1$s' is not a dungeon map.", mEntry.MapName[handler.getSessionDbcLocale()]));

            return true;
        }

        if (difficulty != 0 && CliDB.DifficultyStorage.HasRecord(difficulty)) {
            handler.sendSysMessage(String.format("Invalid difficulty %1$s.", difficulty));

            return false;
        }

        if (difficulty != 0 && global.getDB2Mgr().GetMapDifficultyData(mEntry.id, Difficulty.forValue((byte) difficulty)) == null) {
            handler.sendSysMessage(String.format("Difficulty %1$s is not valid for '%2$s'.", Difficulty.forValue((byte) difficulty), mEntry.MapName[handler.getSessionDbcLocale()]));

            return true;
        }

        return true;
    }


    private static boolean handleDebugSetAuraStateCommand(CommandHandler handler, AuraStateType state, boolean apply) {
        var unit = handler.getSelectedUnit();

        if (!unit) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        if (!state != null) {
            // reset all states
            for (AuraStateType s = 0; s.getValue() < AuraStateType.max.getValue(); ++s) {
                unit.modifyAuraState(s, false);
            }

            return true;
        }

        unit.modifyAuraState(state.GetValueOrDefault(0), apply);

        return true;
    }


    private static boolean handleDebugSpawnVehicleCommand(CommandHandler handler, int entry, int id) {
        var pos = new Position();
        pos.setO(handler.getPlayer().getLocation().getO());
        handler.getPlayer().getClosePoint(pos, handler.getPlayer().getCombatReach());

        if (id == 0) {
            return handler.getPlayer().summonCreature(entry, pos);
        }

        var creatureTemplate = global.getObjectMgr().getCreatureTemplate(entry);

        if (creatureTemplate == null) {
            return false;
        }

        var vehicleRecord = CliDB.VehicleStorage.get(id);

        if (vehicleRecord == null) {
            return false;
        }

        var map = handler.getPlayer().getMap();

        var creature = CREATURE.createCreature(entry, map, pos, id);

        if (!creature) {
            return false;
        }

        map.addToMap(creature);

        return true;
    }


    private static boolean handleDebugThreatListCommand(CommandHandler handler) {
        var target = handler.getSelectedUnit();

        if (target == null) {
            target = handler.getPlayer();
        }

        var mgr = target.getThreatManager();

        if (!target.isAlive()) {
            handler.sendSysMessage(String.format("%1$s (%2$s) is not alive.", target.getName(), target.getGUID()));

            return true;
        }

        int count = 0;
        var threatenedByMe = target.getThreatManager().getThreatenedByMeList();

        if (threatenedByMe.isEmpty()) {
            handler.sendSysMessage(String.format("%1$s (%2$s) does not threaten any units.", target.getName(), target.getGUID()));
        } else {
            handler.sendSysMessage(String.format("List of units threatened by %1$s (%2$s)", target.getName(), target.getGUID()));

            for (var pair : threatenedByMe.entrySet()) {
                Unit unit = pair.getValue().owner;
                handler.sendSysMessage(String.format("   %1$s.   %2$s   (%3$s, SpawnID %4$s)  - threat %5$s", ++count, unit.getName(), unit.getGUID(), (unit.isCreature() ? unit.toCreature().getSpawnId() : 0), pair.getValue().threat));
            }

            handler.sendSysMessage("End of threatened-by-me list.");
        }

        if (mgr.getCanHaveThreatList()) {
            if (!mgr.isThreatListEmpty(true)) {
                if (target.isEngaged()) {
                    handler.sendSysMessage(String.format("Threat list of %1$s (%2$s, SpawnID %3$s):", target.getName(), target.getGUID(), (target.isCreature() ? target.toCreature().getSpawnId() : 0)));
                } else {
                    handler.sendSysMessage(String.format("%1$s (%2$s, SpawnID %3$s) is not engaged, but still has a threat list? Well, here it is:", target.getName(), target.getGUID(), (target.isCreature() ? target.toCreature().getSpawnId() : 0)));
                }

                count = 0;
                var fixateVictim = mgr.getFixateTarget();

                for (var refe : mgr.getSortedThreatList()) {
                    var unit = refe.getVictim();
                    handler.sendSysMessage(String.format("   %1$s.   %2$s   (%3$s)  - threat %4$s[%5$s][%6$s]", ++count, unit.getName(), unit.getGUID(), refe.getThreat(), (unit == fixateVictim ? "FIXATE" : refe.getTauntState()), refe.getOnlineState()));
                }

                handler.sendSysMessage("End of threat list.");
            } else if (!target.isEngaged()) {
                handler.sendSysMessage(String.format("%1$s (%2$s, SpawnID %3$s) is not currently engaged.", target.getName(), target.getGUID(), (target.isCreature() ? target.toCreature().getSpawnId() : 0)));
            } else {
                handler.sendSysMessage(String.format("%1$s (%2$s, SpawnID %3$s) seems to be engaged, but does not have a threat list??", target.getName(), target.getGUID(), (target.isCreature() ? target.toCreature().getSpawnId() : 0)));
            }
        } else if (target.isEngaged()) {
            handler.sendSysMessage(String.format("%1$s (%2$s) is currently engaged. (This unit cannot have a threat list.)", target.getName(), target.getGUID()));
        } else {
            handler.sendSysMessage(String.format("%1$s (%2$s) is not currently engaged. (This unit cannot have a threat list.)", target.getName(), target.getGUID()));
        }

        return true;
    }


    private static boolean handleDebugThreatInfoCommand(CommandHandler handler) {
        var target = handler.getSelectedUnit();

        if (target == null) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        handler.sendSysMessage(String.format("Threat info for %1$s (%2$s):", target.getName(), target.getGUID()));

        var mgr = target.getThreatManager();

        {
            // _singleSchoolModifiers
            var mods = mgr.singleSchoolModifiers;
            handler.sendSysMessage(" - Single-school threat modifiers:");

            handler.sendSysMessage(String.format(" |-- Physical: {0:0.##}", mods[SpellSchool.NORMAL.getValue()] * 100.0f));

            handler.sendSysMessage(String.format(" |-- Holy    : {0:0.##}", mods[SpellSchool.Holy.getValue()] * 100.0f));

            handler.sendSysMessage(String.format(" |-- Fire    : {0:0.##}", mods[SpellSchool.Fire.getValue()] * 100.0f));

            handler.sendSysMessage(String.format(" |-- Nature  : {0:0.##}", mods[SpellSchool.Nature.getValue()] * 100.0f));

            handler.sendSysMessage(String.format(" |-- Frost   : {0:0.##}", mods[SpellSchool.Frost.getValue()] * 100.0f));

            handler.sendSysMessage(String.format(" |-- Shadow  : {0:0.##}", mods[SpellSchool.Shadow.getValue()] * 100.0f));

            handler.sendSysMessage(String.format(" |-- Arcane  : {0:0.##}", mods[SpellSchool.Arcane.getValue()] * 100.0f));
        }

        {
            // _multiSchoolModifiers
            var mods = mgr.multiSchoolModifiers;
            handler.sendSysMessage(String.format("- Multi-school threat modifiers (%1$s entries):", mods.size()));

            for (var pair : mods.entrySet()) {

                handler.sendSysMessage(String.format(" |-- Mask %1$X: {1:0.XX}", pair.getKey(), pair.getValue()));
            }
        }

        {
            // _redirectInfo
            var redirectInfo = mgr.redirectInfo;

            if (redirectInfo.isEmpty()) {
                handler.sendSysMessage(" - No redirects being applied");
            } else {
                handler.sendSysMessage(String.format(" - %1$s redirects being applied:", redirectInfo.size()));

                for (var pair : redirectInfo) {
                    var unit = global.getObjAccessor().GetUnit(target, pair.Item1);
                    handler.sendSysMessage(String.format(" |-- %1$.2d to %2$s", pair.item2, (unit != null ? unit.getName() : pair.Item1)));
                }
            }
        }

        {
            // _redirectRegistry
            var redirectRegistry = mgr.redirectRegistry;

            if (redirectRegistry.isEmpty()) {
                handler.sendSysMessage(" - No redirects are registered");
            } else {
                handler.sendSysMessage(String.format(" - %1$s spells may have redirects registered", redirectRegistry.size()));

                for (var outerPair : redirectRegistry.entrySet()) // (spellId, (guid, pct))
                {
                    var spell = global.getSpellMgr().getSpellInfo(outerPair.getKey(), Difficulty.NONE);
                    handler.sendSysMessage(String.format(" |-- #%1$s %2$s (%3$s entries):", outerPair.getKey(), (spell != null ? spell.SpellName[Global.getWorldMgr().getDefaultDbcLocale()] : "<unknown>"), outerPair.getValue().size()));

                    for (var innerPair : outerPair.getValue()) // (guid, pct)
                    {
                        var unit = global.getObjAccessor().GetUnit(target, innerPair.key);
                        handler.sendSysMessage(String.format("   |-- %1$s to %2$s", innerPair.value, (unit != null ? unit.getName() : innerPair.key)));
                    }
                }
            }
        }

        return true;
    }


    private static boolean handleDebugTransportCommand(CommandHandler handler, String operation) {
        var transport = handler.getPlayer().<transport>GetTransport();

        if (!transport) {
            return false;
        }

        var start = false;

        if (Objects.equals(operation, "stop")) {
            transport.enableMovement(false);
        } else if (Objects.equals(operation, "start")) {
            transport.enableMovement(true);
            start = true;
        } else {
            Position pos = transport.getLocation();
            handler.sendSysMessage("Transport {0} is {1}", transport.getName(), transport.getGoState() == GOState.Ready ? "stopped" : "moving");
            handler.sendSysMessage("Transport position: {0}", pos.toString());

            return true;
        }

        handler.sendSysMessage("Transport {0} {1}", transport.getName(), start ? "started" : "stopped");

        return true;
    }


    private static boolean handleDebugWardenForce(CommandHandler handler, short[] checkIds) {
		/*if (checkIds.isEmpty())
			return false;

		Warden  warden = handler.GetSession().GetWarden();
		if (warden == null)
		{
			handler.sendSysMessage("Warden system is not enabled");
			return true;
		}

		size_t const nQueued = warden->DEBUG_ForceSpecificChecks(checkIds);
		handler->PSendSysMessage("%zu/%zu checks queued for your Warden, they should be sent over the next few minutes (depending on settings)", nQueued, checkIds.size());*/
        return true;
    }


    private static boolean handleDebugUpdateWorldStateCommand(CommandHandler handler, int variable, int value) {
        handler.getPlayer().sendUpdateWorldState(variable, value);

        return true;
    }


    private static boolean handleWPGPSCommand(CommandHandler handler) {
        var player = handler.getPlayer();





        Log.outInfo(LogFilter.SqlDev, String.format("(@PATH, XX, {0:3F}, {1:3F}, {2:5F}, {3:5F}, 0, 0, 0, 100, 0)", player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getO()));

        handler.sendSysMessage("Waypoint SQL written to SQL Developer log");

        return true;
    }


    private static boolean handleDebugWSExpressionCommand(CommandHandler handler, int expressionId) {
        var target = handler.getSelectedPlayerOrSelf();

        if (target == null) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var wsExpressionEntry = CliDB.WorldStateExpressionStorage.get(expressionId);

        if (wsExpressionEntry == null) {
            return false;
        }

        if (ConditionManager.isPlayerMeetingExpression(target, wsExpressionEntry)) {
            handler.sendSysMessage(String.format("Expression %1$s meet", expressionId));
        } else {
            handler.sendSysMessage(String.format("Expression %1$s not meet", expressionId));
        }

        return true;
    }

    private static void handleDebugGuidLimitsMap(CommandHandler handler, Map map) {
        handler.sendSysMessage(String.format("Map Id: %1$s Name: '%2$s' Instance Id: %3$s Highest Guid Creature: %4$s GameObject: %5$s", map.getId(), map.getMapName(), map.getInstanceId(), map.generateLowGuid(HighGuid.Creature), map.getMaxLowGuid(HighGuid.GameObject)));
    }


    private static class DebugAsanCommands {

        private static boolean handleDebugMemoryLeak(CommandHandler handler) {
            return true;
        }


        private static boolean handleDebugOutOfBounds(CommandHandler handler) {
            return true;
        }
    }


    private static class DebugPlayCommands {

        private static boolean handleDebugPlayCinematicCommand(CommandHandler handler, int cinematicId) {
            var cineSeq = CliDB.CinematicSequencesStorage.get(cinematicId);

            if (cineSeq == null) {
                handler.sendSysMessage(SysMessage.CinematicNotExist, cinematicId);

                return false;
            }

            // Dump camera locations
            var list = M2Storage.GetFlyByCameras(cineSeq.Camera[0]);

            if (list != null) {
                handler.sendSysMessage("Waypoints for sequence {0}, camera {1}", cinematicId, cineSeq.Camera[0]);
                int count = 1;

                for (var cam : list) {
                    handler.sendSysMessage("{0} - {1}ms [{2}, {3}, {4}] Facing {5} ({6} degrees)", count, cam.timeStamp, cam.locations.X, cam.locations.Y, cam.locations.Z, cam.locations.W, cam.locations.W * (180 / Math.PI));
                    count++;
                }

                handler.sendSysMessage("{0} waypoints dumped", list.size());
            }

            handler.getPlayer().sendCinematicStart(cinematicId);

            return true;
        }


        private static boolean handleDebugPlayMovieCommand(CommandHandler handler, int movieId) {
            if (!CliDB.MovieStorage.containsKey(movieId)) {
                handler.sendSysMessage(SysMessage.MovieNotExist, movieId);

                return false;
            }

            handler.getPlayer().sendMovieStart(movieId);

            return true;
        }


        private static boolean handleDebugPlayMusicCommand(CommandHandler handler, int musicId) {
            if (!CliDB.SoundKitStorage.containsKey(musicId)) {
                handler.sendSysMessage(SysMessage.SoundNotExist, musicId);

                return false;
            }

            var player = handler.getPlayer();

            player.playDirectMusic(musicId, player);

            handler.sendSysMessage(SysMessage.YouHearSound, musicId);

            return true;
        }


        private static boolean handleDebugPlaySoundCommand(CommandHandler handler, int soundId, int broadcastTextId) {
            if (!CliDB.SoundKitStorage.containsKey(soundId)) {
                handler.sendSysMessage(SysMessage.SoundNotExist, soundId);

                return false;
            }

            var player = handler.getPlayer();

            var unit = handler.getSelectedUnit();

            if (!unit) {
                handler.sendSysMessage(SysMessage.SelectCharOrCreature);

                return false;
            }

            if (!player.getTarget().isEmpty()) {
                unit.playDistanceSound(soundId, player);
            } else {
                unit.playDirectSound(soundId, player, broadcastTextId);
            }

            handler.sendSysMessage(SysMessage.YouHearSound, soundId);

            return true;
        }
    }


    private static class DebugPvpCommands {


        private static boolean handleDebugWarModeFactionBalanceCommand(CommandHandler handler, String command) {
            return handleDebugWarModeFactionBalanceCommand(handler, command, 0);
        }

        private static boolean handleDebugWarModeFactionBalanceCommand(CommandHandler handler, String command, int rewardValue) {
            // USAGE: .debug pvp fb <alliance|horde|neutral|off> [pct]
            // neutral     Sets faction balance off.
            // alliance    Set faction balance to alliance.
            // horde       Set faction balance to horde.
            // off         Reset the faction balance and use the calculated value of it
            switch (command.toLowerCase()) {
                default: // workaround for Variant of only ExactSequences not being supported
                    handler.sendSysMessage(SysMessage.BadValue);

                    return false;
                case "alliance":
                    global.getWorldMgr().setForcedWarModeFactionBalanceState(TeamId.ALLIANCE, rewardValue);

                    break;
                case "horde":
                    global.getWorldMgr().setForcedWarModeFactionBalanceState(TeamId.HORDE, rewardValue);

                    break;
                case "neutral":
                    global.getWorldMgr().setForcedWarModeFactionBalanceState(TeamIds.Neutral);

                    break;
                case "off":
                    global.getWorldMgr().disableForcedWarModeFactionBalanceState();

                    break;
            }

            return true;
        }
    }


    private static class DebugSendCommands {

        private static boolean handleDebugSendBuyErrorCommand(CommandHandler handler, BuyResult error) {
            handler.getPlayer().sendBuyError(error, null, 0);

            return true;
        }


        private static boolean handleDebugSendChannelNotifyCommand(CommandHandler handler, ChatNotify type) {
            ChannelNotify packet = new ChannelNotify();
            packet.type = type;
            packet.channel = "test";
            handler.getSession().sendPacket(packet);

            return true;
        }


        private static boolean handleDebugSendChatMsgCommand(CommandHandler handler, ChatMsg type) {
            ChatPkt data = new ChatPkt();
            data.initialize(type, language.Universal, handler.getPlayer(), handler.getPlayer(), "testtest", 0, "chan");
            handler.getSession().sendPacket(data);

            return true;
        }


        private static boolean handleDebugSendEquipErrorCommand(CommandHandler handler, InventoryResult error) {
            handler.getPlayer().sendEquipError(error);

            return true;
        }


        private static boolean handleDebugSendLargePacketCommand(CommandHandler handler) {
            StringBuilder ss = new StringBuilder();

            while (ss.length() < 128000) {
                ss.append("This is a dummy string to push the packet's size beyond 128000 bytes. ");
            }

            handler.sendSysMessage(ss.toString());

            return true;
        }


        private static boolean handleDebugSendOpcodeCommand(CommandHandler handler) {
            handler.sendSysMessage(SysMessage.CmdInvalid);

            return true;
        }


        private static boolean handleDebugSendPlayerChoiceCommand(CommandHandler handler, int choiceId) {
            var player = handler.getPlayer();
            player.sendPlayerChoice(player.getGUID(), choiceId);

            return true;
        }


        private static boolean handleDebugSendQuestPartyMsgCommand(CommandHandler handler, QuestPushReason msg) {
            handler.getPlayer().sendPushToPartyResponse(handler.getPlayer(), msg);

            return true;
        }


        private static boolean handleDebugSendQuestInvalidMsgCommand(CommandHandler handler, QuestFailedReasons msg) {
            handler.getPlayer().sendCanTakeQuestResponse(msg);

            return true;
        }


        private static boolean handleDebugSendSellErrorCommand(CommandHandler handler, SellResult error) {
            handler.getPlayer().sendSellError(error, null, ObjectGuid.Empty);

            return true;
        }


        private static boolean handleDebugSendSetPhaseShiftCommand(CommandHandler handler, int phaseId, int visibleMapId, int uiMapPhaseId) {
            PhaseShift phaseShift = new PhaseShift();

            if (phaseId != 0) {
                phaseShift.addPhase(phaseId, phaseFlags.NONE, null);
            }

            if (visibleMapId != 0) {
                phaseShift.addVisibleMapId(visibleMapId, null);
            }

            if (uiMapPhaseId != 0) {
                phaseShift.addUiMapPhaseId(uiMapPhaseId);
            }

            PhasingHandler.sendToPlayer(handler.getPlayer(), phaseShift);

            return true;
        }


        private static boolean handleDebugSendSpellFailCommand(CommandHandler handler, SpellCastResult result, Integer failArg1, Integer failArg2) {
            CastFailed castFailed = new CastFailed();
            castFailed.castID = ObjectGuid.Empty;
            castFailed.spellID = 133;
            castFailed.reason = result;
            castFailed.failedArg1 = (failArg1 == null ? -1 : failArg1.intValue());
            castFailed.failedArg2 = (failArg2 == null ? -1 : failArg2.intValue());
            handler.getSession().sendPacket(castFailed);

            return true;
        }
    }


    private static class DebugWardenCommands {

        private static boolean handleDebugWardenForce(CommandHandler handler, short[] checkIds) {
			/*if (checkIds.isEmpty())
				return false;

			Warden  warden = handler.GetSession().GetWarden();
			if (warden == null)
			{
				handler.sendSysMessage("Warden system is not enabled");
				return true;
			}

			size_t const nQueued = warden->DEBUG_ForceSpecificChecks(checkIds);
			handler->PSendSysMessage("%zu/%zu checks queued for your Warden, they should be sent over the next few minutes (depending on settings)", nQueued, checkIds.size());*/
            return true;
        }
    }
}
