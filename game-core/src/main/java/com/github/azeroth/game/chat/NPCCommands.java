package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.FormationMgr;
import com.github.azeroth.game.entity.VendorItem;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.transport;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.loot.LootItem;
import com.github.azeroth.game.loot.NotNormalLootItem;
import com.github.azeroth.game.domain.spawn.SpawnGroupTemplateData;
import com.github.azeroth.game.movement.generator.FollowMovementGenerator;
import game.PhasingHandler;
import game.WorldConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;



class NPCCommands {
    
    private static boolean handleNpcDespawnGroup(CommandHandler handler, String[] opts) {
        if (opts.isEmpty()) {
            return false;
        }

        var deleteRespawnTimes = false;
        int groupId = 0;

        // Decode arguments
        for (var variant : opts) {
            tangible.OutObject<Integer> tempOut_groupId = new tangible.OutObject<Integer>();
            if (!tangible.TryParseHelper.tryParseInt(variant, tempOut_groupId)) {
                groupId = tempOut_groupId.outArgValue;
                deleteRespawnTimes = true;
            } else {
                groupId = tempOut_groupId.outArgValue;
            }
        }

        var player = handler.getSession().getPlayer();

        int despawnedCount;
        tangible.OutObject<Integer> tempOut_despawnedCount = new tangible.OutObject<Integer>();
        if (!player.getMap().spawnGroupDespawn(groupId, deleteRespawnTimes, tempOut_despawnedCount)) {
            despawnedCount = tempOut_despawnedCount.outArgValue;
            handler.sendSysMessage(SysMessage.SpawngroupBadgroup, groupId);

            return false;
        } else {
            despawnedCount = tempOut_despawnedCount.outArgValue;
        }

        handler.sendSysMessage(String.format("Despawned a total of %1$s Objects.", despawnedCount));

        return true;
    }

    
    private static boolean handleNpcEvadeCommand(CommandHandler handler, EvadeReason why, String force) {
        var creatureTarget = handler.getSelectedCreature();

        if (!creatureTarget || creatureTarget.isPet()) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        if (!creatureTarget.isAIEnabled()) {
            handler.sendSysMessage(SysMessage.CreatureNotAiEnabled);

            return false;
        }

        if (force.equalsIgnoreCase("force")) {
            creatureTarget.clearUnitState(UnitState.Evade);
        }

        creatureTarget.getAI().enterEvadeMode(why.GetValueOrDefault(EvadeReason.other));

        return true;
    }

    
    private static boolean handleNpcInfoCommand(CommandHandler handler) {
        var target = handler.getSelectedCreature();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        var cInfo = target.getCreatureTemplate();

        var faction = target.getFaction();
        var npcflags = (long) target.getUnitData().npcFlags.get(1) << 32 | target.getUnitData().npcFlags.get(0);
        var mechanicImmuneMask = cInfo.mechanicImmuneMask;
        var displayid = target.getDisplayId();
        var nativeid = target.getNativeDisplayId();
        var entry = target.getEntry();

        var curRespawnDelay = target.getRespawnCompatibilityMode() ? target.getRespawnTimeEx() - gameTime.GetGameTime() : target.getMap().getCreatureRespawnTime(target.getSpawnId()) - gameTime.GetGameTime();

        if (curRespawnDelay < 0) {
            curRespawnDelay = 0;
        }

        var curRespawnDelayStr = time.secsToTimeString((long) curRespawnDelay, TimeFormat.ShortText, false);
        var defRespawnDelayStr = time.secsToTimeString(target.getRespawnDelay(), TimeFormat.ShortText, false);

        handler.sendSysMessage(SysMessage.NpcinfoChar, target.getName(), target.getSpawnId(), target.getGUID().toString(), entry, faction, npcflags, displayid, nativeid);

        if (target.getCreatureData() != null && target.getCreatureData().getSpawnGroupData().getGroupId() != 0) {
            var groupData = target.getCreatureData().getSpawnGroupData();
            handler.sendSysMessage(SysMessage.SpawninfoGroupId, groupData.getName(), groupData.getGroupId(), groupData.getFlags(), target.getMap().isSpawnGroupActive(groupData.getGroupId()));
        }

        handler.sendSysMessage(SysMessage.SpawninfoCompatibilityMode, target.getRespawnCompatibilityMode());
        handler.sendSysMessage(SysMessage.NpcinfoLevel, target.getLevel());
        handler.sendSysMessage(SysMessage.NpcinfoEquipment, target.getCurrentEquipmentId(), target.getOriginalEquipmentId());
        handler.sendSysMessage(SysMessage.NpcinfoHealth, target.getCreateHealth(), target.getMaxHealth(), target.getHealth());
        handler.sendSysMessage(SysMessage.NpcinfoMovementData, target.getMovementTemplate().toString());

        handler.sendSysMessage(SysMessage.NpcinfoUnitFieldFlags, (int) target.getUnitData().flags);

        for (UnitFlags value : UnitFlag.values()) {
            if (target.hasUnitFlag(value)) {
                handler.sendSysMessage("{0} (0x{1:X})", UnitFlag.forValue(value), value);
            }
        }

        handler.sendSysMessage(SysMessage.NpcinfoUnitFieldFlags2, (int) target.getUnitData().flags2);

        for (UnitFlags2 value : UnitFlag2.values()) {
            if (target.hasUnitFlag2(value)) {
                handler.sendSysMessage("{0} (0x{1:X})", UnitFlag2.forValue(value), value);
            }
        }

        handler.sendSysMessage(SysMessage.NpcinfoUnitFieldFlags3, (int) target.getUnitData().flags3);

        for (UnitFlags3 value : unitFlags3.values()) {
            if (target.hasUnitFlag3(value)) {
                handler.sendSysMessage("{0} (0x{1:X})", unitFlags3.forValue(value), value);
            }
        }

        handler.sendSysMessage(SysMessage.NpcinfoDynamicFlags, target.getDynamicFlags());
        handler.sendSysMessage(SysMessage.CommandRawpawntimes, defRespawnDelayStr, curRespawnDelayStr);
        handler.sendSysMessage(SysMessage.NpcinfoLoot, cInfo.lootId, cInfo.pickPocketId, cInfo.skinLootId);
        handler.sendSysMessage(SysMessage.NpcinfoDungeonId, target.getInstanceId());

        var data = global.getObjectMgr().getCreatureData(target.getSpawnId());

        if (data != null) {
            handler.sendSysMessage(SysMessage.NpcinfoPhases, data.phaseId, data.phaseGroup);
        }

        PhasingHandler.printToChat(handler, target);

        handler.sendSysMessage(SysMessage.NpcinfoArmor, target.getArmor());
        handler.sendSysMessage(SysMessage.NpcinfoPosition, target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ());
        handler.sendSysMessage(SysMessage.ObjectinfoAiInfo, target.getAIName(), target.getScriptName());
        handler.sendSysMessage(SysMessage.ObjectinfoStringIds, target.getStringIds()[0], target.getStringIds()[1], target.getStringIds()[2]);
        handler.sendSysMessage(SysMessage.NpcinfoReactstate, target.getReactState());
        var ai = target.getAI();

        if (ai != null) {
            handler.sendSysMessage(SysMessage.ObjectinfoAiType, "ai");
        }

        handler.sendSysMessage(SysMessage.NpcinfoFlagsExtra, cInfo.flagsExtra);

        for (int value : CreatureFlagExtra.values()) {
            if (cInfo.flagsExtra.hasFlag(CreatureFlagExtra.forValue(value))) {
                handler.sendSysMessage("{0} (0x{1:X})", CreatureFlagExtra.forValue(value), value);
            }
        }

        handler.sendSysMessage(SysMessage.NpcinfoNpcFlags, target.getUnitData().npcFlags.get(0));

        for (int value : NPCFlags.values()) {
            if (npcflags.hasFlag(value)) {
                handler.sendSysMessage("{0} (0x{1:X})", NPCFlags.forValue(value), value);
            }
        }

        handler.sendSysMessage(SysMessage.NpcinfoMechanicImmune, mechanicImmuneMask);

        for (int value : mechanics.values()) {
            if ((boolean) (mechanicImmuneMask & (1 << (value - 1)))) {
                handler.sendSysMessage("{0} (0x{1:X})", mechanics.forValue(value), value);
            }
        }

        return true;
    }

    
    private static boolean handleNpcMoveCommand(CommandHandler handler, Long spawnId) {
        var creature = handler.getSelectedCreature();
        var player = handler.getSession().getPlayer();

        if (player == null) {
            return false;
        }

        if (spawnId == null && creature == null) {
            return false;
        }

        var lowguid = spawnId != null ? spawnId.longValue() : creature.getSpawnId();

        // Attempting creature load from DB data
        var data = global.getObjectMgr().getCreatureData(lowguid);

        if (data == null) {
            handler.sendSysMessage(SysMessage.CommandCreatguidnotfound, lowguid);

            return false;
        }

        if (player.getLocation().getMapId() != data.getMapId()) {
            handler.sendSysMessage(SysMessage.CommandCreatureatsamemap, lowguid);

            return false;
        }

        global.getObjectMgr().removeCreatureFromGrid(data);
        data.spawnPoint.relocate(player.getLocation());
        global.getObjectMgr().addCreatureToGrid(data);

        // update position in DB
        var stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_POSITION);
        stmt.AddValue(0, player.getLocation().getX());
        stmt.AddValue(1, player.getLocation().getY());
        stmt.AddValue(2, player.getLocation().getZ());
        stmt.AddValue(3, player.getLocation().getO());
        stmt.AddValue(4, lowguid);

        DB.World.execute(stmt);

        // respawn selected creature at the new location
        if (creature != null) {
            creature.despawnOrUnsummon(duration.FromSeconds(0), duration.FromSeconds(1));
        }

        handler.sendSysMessage(SysMessage.CommandCreaturemoved);

        return true;
    }

    
    private static boolean handleNpcNearCommand(CommandHandler handler, Float dist) {
        var distance = (dist == null ? 10.0f : dist.floatValue());
        int count = 0;

        var player = handler.getPlayer();

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_CREATURE_NEAREST);
        stmt.AddValue(0, player.getLocation().getX());
        stmt.AddValue(1, player.getLocation().getY());
        stmt.AddValue(2, player.getLocation().getZ());
        stmt.AddValue(3, player.getLocation().getMapId());
        stmt.AddValue(4, player.getLocation().getX());
        stmt.AddValue(5, player.getLocation().getY());
        stmt.AddValue(6, player.getLocation().getZ());
        stmt.AddValue(7, distance * distance);
        var result = DB.World.query(stmt);

        if (!result.isEmpty()) {
            do {
                var guid = result.<Long>Read(0);
                var entry = result.<Integer>Read(1);
                var x = result.<Float>Read(2);
                var y = result.<Float>Read(3);
                var z = result.<Float>Read(4);
                var mapId = result.<SHORT>Read(5);

                var creatureTemplate = global.getObjectMgr().getCreatureTemplate(entry);

                if (creatureTemplate == null) {
                    continue;
                }

                handler.sendSysMessage(SysMessage.CreatureListChat, guid, guid, creatureTemplate.name, x, y, z, mapId, "", "");

                ++count;
            } while (result.NextRow());
        }

        handler.sendSysMessage(SysMessage.CommandNearNpcMessage, distance, count);

        return true;
    }

    
    private static boolean handleNpcPlayEmoteCommand(CommandHandler handler, int emote) {
        var target = handler.getSelectedCreature();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        target.setEmoteState(emote.forValue(emote));

        return true;
    }

    
    private static boolean handleNpcSayCommand(CommandHandler handler, Tail text) {
        if (text.isEmpty()) {
            return false;
        }

        var creature = handler.getSelectedCreature();

        if (!creature) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        creature.say(text, language.Universal);

        // make some emotes
        switch (((String) text).LastOrDefault()) {
            case '?':
                creature.handleEmoteCommand(emote.OneshotQuestion);

                break;
            case '!':
                creature.handleEmoteCommand(emote.OneshotExclamation);

                break;
            default:
                creature.handleEmoteCommand(emote.OneshotTalk);

                break;
        }

        return true;
    }

    
    private static boolean handleNpcShowLootCommand(CommandHandler handler, String all) {
        var creatureTarget = handler.getSelectedCreature();

        if (creatureTarget == null || creatureTarget.isPet()) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        var loot = creatureTarget.getLoot();

        if (!creatureTarget.isDead() || loot == null || loot.isLooted()) {
            handler.sendSysMessage(SysMessage.CommandNotDeadOrNoLoot, creatureTarget.getName());

            return false;
        }

        handler.sendSysMessage(SysMessage.CommandNpcShowlootHeader, creatureTarget.getName(), creatureTarget.getEntry());
        handler.sendSysMessage(SysMessage.CommandNpcShowlootMoney, loot.gold / MoneyConstants.gold, (loot.gold % MoneyConstants.gold) / MoneyConstants.Silver, loot.gold % MoneyConstants.Silver);

        if (all.equalsIgnoreCase("all")) // nonzero from strcmp <. not equal
        {
            handler.sendSysMessage(SysMessage.CommandNpcShowlootLabel, "Standard items", loot.items.size());

            for (var item : loot.items) {
                if (!item.is_looted) {
                    _ShowLootEntry(handler, item.itemid, item.count);
                }
            }
        } else {
            handler.sendSysMessage(SysMessage.CommandNpcShowlootLabel, "Standard items", loot.items.size());

            for (var item : loot.items) {
                if (!item.is_looted && !item.freeforall && item.conditions.isEmpty()) {
                    _ShowLootEntry(handler, item.itemid, item.count);
                }
            }

            if (!loot.getPlayerFFAItems().isEmpty()) {
                handler.sendSysMessage(SysMessage.CommandNpcShowlootLabel2, "FFA items per allowed player");
                _IterateNotNormalLootMap(handler, loot.getPlayerFFAItems(), loot.items);
            }
        }

        return true;
    }

    
    private static boolean handleNpcSpawnGroup(CommandHandler handler, String[] opts) {
        if (opts.isEmpty()) {
            return false;
        }

        var ignoreRespawn = false;
        var force = false;
        int groupId = 0;

        // Decode arguments
        for (var variant : opts) {
            switch (variant) {
                case "force":
                    force = true;

                    break;
                case "ignorerespawn":
                    ignoreRespawn = true;

                    break;
                default:
                    tangible.OutObject<Integer> tempOut_groupId = new tangible.OutObject<Integer>();
                    tangible.TryParseHelper.tryParseInt(variant, tempOut_groupId);
                    groupId = tempOut_groupId.outArgValue;

                    break;
            }
        }

        var player = handler.getSession().getPlayer();

        ArrayList<WorldObject> creatureList = new ArrayList<>();

        if (!player.getMap().spawnGroupSpawn(groupId, ignoreRespawn, force, creatureList)) {
            handler.sendSysMessage(SysMessage.SpawngroupBadgroup, groupId);

            return false;
        }

        handler.sendSysMessage(SysMessage.SpawngroupSpawncount, creatureList.size());

        return true;
    }

    
    private static boolean handleNpcTameCommand(CommandHandler handler) {
        var creatureTarget = handler.getSelectedCreature();

        if (!creatureTarget || creatureTarget.isPet()) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        var player = handler.getSession().getPlayer();

        if (!player.getPetGUID().isEmpty()) {
            handler.sendSysMessage(SysMessage.YouAlreadyHavePet);

            return false;
        }

        var cInfo = creatureTarget.getCreatureTemplate();

        if (!cInfo.isTameable(player.getCanTameExoticPets())) {
            handler.sendSysMessage(SysMessage.CreatureNonTameable, cInfo.entry);

            return false;
        }

        // Everything looks OK, create new pet
        var pet = player.createTamedPetFrom(creatureTarget);

        if (!pet) {
            handler.sendSysMessage(SysMessage.CreatureNonTameable, cInfo.entry);

            return false;
        }

        // place pet before player
        var pos = new Position();
        player.getClosePoint(pos, creatureTarget.getCombatReach(), SharedConst.contactDistance);
        pos.setO(MathUtil.PI - player.getLocation().getO());
        pet.getLocation().relocate(pos);

        // set pet to defensive mode by default (some classes can't control controlled pets in fact).
        pet.setReactState(ReactStates.Defensive);

        // calculate proper level
        var level = Math.max(player.getLevel() - 5, creatureTarget.getLevel());

        // prepare visual effect for levelup
        pet.setLevel(level - 1);

        // add to world
        pet.getMap().addToMap(pet.toCreature());

        // visual effect for levelup
        pet.setLevel(level);

        // caster have pet now
        player.setMinion(pet, true);

        pet.SavePetToDB(PetSaveMode.AsCurrent);
        player.petSpellInitialize();

        return true;
    }

    
    private static boolean handleNpcTextEmoteCommand(CommandHandler handler, Tail text) {
        var creature = handler.getSelectedCreature();

        if (!creature) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        creature.textEmote(text);

        return true;
    }

    
    private static boolean handleNpcWhisperCommand(CommandHandler handler, String recv, Tail text) {
        if (text.isEmpty()) {
            handler.sendSysMessage(SysMessage.CmdSyntax);

            return false;
        }

        var creature = handler.getSelectedCreature();

        if (!creature) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        // check online security
        var receiver = global.getObjAccessor().FindPlayerByName(recv);

        if (handler.hasLowerSecurity(receiver, ObjectGuid.Empty)) {
            return false;
        }

        creature.whisper(text, language.Universal, receiver);

        return true;
    }

    
    private static boolean handleNpcYellCommand(CommandHandler handler, Tail text) {
        if (text.isEmpty()) {
            return false;
        }

        var creature = handler.getSelectedCreature();

        if (!creature) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        creature.yell(text, language.Universal);

        // make an emote
        creature.handleEmoteCommand(emote.OneshotShout);

        return true;
    }


    private static void _ShowLootEntry(CommandHandler handler, int itemId, byte itemCount) {
        _ShowLootEntry(handler, itemId, itemCount, false);
    }

    private static void _ShowLootEntry(CommandHandler handler, int itemId, byte itemCount, boolean alternateString) {
        var name = "Unknown item";

        var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

        if (itemTemplate != null) {
            name = itemTemplate.getName(handler.getSessionDbcLocale());
        }

        handler.sendSysMessage(alternateString ? SysMessage.CommandNpcShowlootEntry2 : SysMessage.CommandNpcShowlootEntry, itemCount, ItemConst.ItemQualityColors[(int) (itemTemplate != null ? itemTemplate.getQuality() : itemQuality.Poor)], itemId, name, itemId);
    }

    private static void _IterateNotNormalLootMap(CommandHandler handler, MultiMap<ObjectGuid, NotNormalLootItem> map, ArrayList<LootItem> items) {
        for (var key : map.keySet()) {
            if (map.get(key).isEmpty()) {
                continue;
            }

            var list = map.get(key);

            var player = global.getObjAccessor().findConnectedPlayer(key);
            handler.sendSysMessage(SysMessage.CommandNpcShowlootSublabel, player ? player.getName() : String.format("Offline player (GUID %1$s)", key), list.count);

            for (var it : list) {
                var item = items.get(it.lootListId);

                if (!it.is_looted && !item.is_looted) {
                    _ShowLootEntry(handler, item.itemid, item.count, true);
                }
            }
        }
    }

    
    private static class AddCommands {
        
        private static boolean handleNpcAddCommand(CommandHandler handler, int id) {
            if (global.getObjectMgr().getCreatureTemplate(id) == null) {
                return false;
            }

            var chr = handler.getSession().getPlayer();
            var map = chr.getMap();

            var trans = chr.<transport>GetTransport();

            if (trans) {
                var guid = global.getObjectMgr().generateCreatureSpawnId();
                var data = global.getObjectMgr().newOrExistCreatureData(guid);
                data.setSpawnId(guid);
                data.setSpawnGroupData(global.getObjectMgr().getDefaultSpawnGroup());
                data.id = id;
                data.spawnPoint.relocate(chr.getTransOffsetX(), chr.getTransOffsetY(), chr.getTransOffsetZ(), chr.getTransOffsetO());
                data.setSpawnGroupData(new SpawnGroupTemplateData());

                var creaturePassenger = trans.createNPCPassenger(guid, data);

                if (creaturePassenger != null) {
                    creaturePassenger.saveToDB((int) trans.getTemplate().moTransport.spawnMap, new ArrayList<Difficulty>(Arrays.asList(map.getDifficultyID())));

                    global.getObjectMgr().addCreatureToGrid(data);
                }

                return true;
            }

            var creature = CREATURE.createCreature(id, map, chr.getLocation());

            if (!creature) {
                return false;
            }

            PhasingHandler.inheritPhaseShift(creature, chr);

            creature.saveToDB(map.getId(), new ArrayList<Difficulty>(Arrays.asList(map.getDifficultyID())));

            var db_guid = creature.getSpawnId();

            // To call _LoadGoods(); _LoadQuests(); CreateTrainerSpells()
            // current "creature" variable is deleted and created fresh new, otherwise old values might trigger asserts or cause undefined behavior
            creature.cleanupsBeforeDelete();
            creature = CREATURE.createCreatureFromDB(db_guid, map, true, true);

            if (!creature) {
                return false;
            }

            global.getObjectMgr().addCreatureToGrid(global.getObjectMgr().getCreatureData(db_guid));

            return true;
        }

        

        private static boolean handleNpcAddVendorItemCommand(CommandHandler handler, int itemId, Integer mc, Integer it, Integer ec, String bonusListIds) {
            if (itemId == 0) {
                handler.sendSysMessage(SysMessage.CommandNeeditemsend);

                return false;
            }

            var vendor = handler.getSelectedCreature();

            if (!vendor) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            var maxcount = (mc == null ? 0 : mc.intValue());
            var incrtime = (it == null ? 0 : it.intValue());
            var extendedcost = (ec == null ? 0 : ec.intValue());
            var vendor_entry = vendor.getEntry();

            VendorItem vItem = new VendorItem();
            vItem.setItem(itemId);
            vItem.setMaxcount(maxcount);
            vItem.setIncrtime(incrtime);
            vItem.setExtendedCost(extendedcost);
            vItem.setType(ItemVendorType.item);

            if (!bonusListIds.isEmpty()) {
                var bonusListIDsTok = new LocalizedString();

                if (!bonusListIDsTok.isEmpty()) {
                    for (String token : bonusListIDsTok) {
                        int id;
                        tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
                        if (tangible.TryParseHelper.tryParseInt(token, tempOut_id)) {
                            id = tempOut_id.outArgValue;
                            vItem.getBonusListIDs().add(id);
                        } else {
                            id = tempOut_id.outArgValue;
                        }
                    }
                }
            }

            if (!global.getObjectMgr().isVendorItemValid(vendor_entry, vItem, handler.getSession().getPlayer())) {
                return false;
            }

            global.getObjectMgr().addVendorItem(vendor_entry, vItem);

            var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

            handler.sendSysMessage(SysMessage.ItemAddedToList, itemId, itemTemplate.getName(), maxcount, incrtime, extendedcost);

            return true;
        }

        
        private static boolean handleNpcAddMoveCommand(CommandHandler handler, long lowGuid) {
            // attempt check creature existence by DB data
            var data = global.getObjectMgr().getCreatureData(lowGuid);

            if (data == null) {
                handler.sendSysMessage(SysMessage.CommandCreatguidnotfound, lowGuid);

                return false;
            }

            // Update movement type
            var stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_MOVEMENT_TYPE);
            stmt.AddValue(0, (byte) MovementGeneratorType.Waypoint.getValue());
            stmt.AddValue(1, lowGuid);
            DB.World.execute(stmt);

            handler.sendSysMessage(SysMessage.WaypointAdded);

            return true;
        }

        
        private static boolean handleNpcAddFormationCommand(CommandHandler handler, long leaderGUID) {
            var creature = handler.getSelectedCreature();

            if (!creature || creature.getSpawnId() == 0) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            var lowguid = creature.getSpawnId();

            if (creature.getFormation() != null) {
                handler.sendSysMessage("Selected creature is already member of group {0}", creature.getFormation().getLeaderSpawnId());

                return false;
            }

            if (lowguid == 0) {
                return false;
            }

            var chr = handler.getSession().getPlayer();
            var followAngle = (creature.getLocation().getAbsoluteAngle(chr.getLocation()) - chr.getLocation().getO()) * 180.0f / MathUtil.PI;
            var followDist = (float) Math.sqrt((float) Math.pow(chr.getLocation().getX() - creature.getLocation().getX(), 2f) + (float) Math.pow(chr.getLocation().getY() - creature.getLocation().getY(), 2f));
            int groupAI = 0;
            FormationMgr.addFormationMember(lowguid, followAngle, followDist, leaderGUID, groupAI);
            creature.searchFormation();

            var stmt = DB.World.GetPreparedStatement(WorldStatements.INS_CREATURE_FORMATION);
            stmt.AddValue(0, leaderGUID);
            stmt.AddValue(1, lowguid);
            stmt.AddValue(2, followAngle);
            stmt.AddValue(3, followDist);
            stmt.AddValue(4, groupAI);

            DB.World.execute(stmt);

            handler.sendSysMessage("Creature {0} added to formation with leader {1}", lowguid, leaderGUID);

            return true;
        }

        

        private static boolean handleNpcAddTempSpawnCommand(CommandHandler handler, String lootStr, int id) {
            var loot = false;

            if (!lootStr.isEmpty()) {
                if (lootStr.equalsIgnoreCase("loot")) {
                    loot = true;
                } else if (lootStr.equalsIgnoreCase("noloot")) {
                    loot = false;
                } else {
                    return false;
                }
            }

            if (global.getObjectMgr().getCreatureTemplate(id) == null) {
                return false;
            }

            var chr = handler.getSession().getPlayer();
            chr.summonCreature(id, chr.getLocation(), loot ? TempSummonType.CorpseTimedDespawn : TempSummonType.CorpseDespawn, duration.FromSeconds(30));

            return true;
        }
    }

    
    private static class DeleteCommands {
        
        private static boolean handleNpcDeleteCommand(CommandHandler handler, Long spawnIdArg) {
            long spawnId;

            if (spawnIdArg != null) {
                spawnId = spawnIdArg.longValue();
            } else {
                var creature = handler.getSelectedCreature();

                if (!creature || creature.isPet() || creature.isTotem()) {
                    handler.sendSysMessage(SysMessage.SelectCreature);

                    return false;
                }

                var summon = creature.toTempSummon();

                if (summon != null) {
                    summon.unSummon();
                    handler.sendSysMessage(SysMessage.CommandDelcreatmessage);

                    return true;
                }

                spawnId = creature.getSpawnId();
            }

            if (CREATURE.deleteFromDB(spawnId)) {
                handler.sendSysMessage(SysMessage.CommandDelcreatmessage);

                return true;
            }

            handler.sendSysMessage(SysMessage.CommandCreatguidnotfound, spawnId);

            return false;
        }

        
        private static boolean handleNpcDeleteVendorItemCommand(CommandHandler handler, int itemId) {
            var vendor = handler.getSelectedCreature();

            if (!vendor || !vendor.isVendor()) {
                handler.sendSysMessage(SysMessage.CommandVendorselection);

                return false;
            }

            if (itemId == 0) {
                return false;
            }

            if (!global.getObjectMgr().removeVendorItem(vendor.getEntry(), itemId, ItemVendorType.item)) {
                handler.sendSysMessage(SysMessage.ItemNotInList, itemId);

                return false;
            }

            var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);
            handler.sendSysMessage(SysMessage.ItemDeletedFromList, itemId, itemTemplate.getName());

            return true;
        }
    }

    
    private static class FollowCommands {
        
        private static boolean handleNpcFollowCommand(CommandHandler handler) {
            var player = handler.getSession().getPlayer();
            var creature = handler.getSelectedCreature();

            if (!creature) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            // Follow player - Using pet's default dist and angle
            creature.getMotionMaster().moveFollow(player, SharedConst.PetFollowDist, creature.getFollowAngle());

            handler.sendSysMessage(SysMessage.CreatureFollowYouNow, creature.getName());

            return true;
        }

        
        private static boolean handleNpcUnFollowCommand(CommandHandler handler) {
            var player = handler.getPlayer();
            var creature = handler.getSelectedCreature();

            if (!creature) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            var movement = creature.getMotionMaster().getMovementGenerator(a ->
            {
                if (a.getMovementGeneratorType() == MovementGeneratorType.Follow) {
                    var followMovement = a instanceof FollowMovementGenerator ? (FollowMovementGenerator) a : null;

                    return followMovement != null && followMovement.getTarget() == player;
                }

                return false;
            });

            if (movement != null) {
                handler.sendSysMessage(SysMessage.CreatureNotFollowYou, creature.getName());

                return false;
            }

            creature.getMotionMaster().remove(movement);
            handler.sendSysMessage(SysMessage.CreatureNotFollowYouNow, creature.getName());

            return true;
        }
    }

    
    private static class SetCommands {
        
        private static boolean handleNpcSetAllowMovementCommand(CommandHandler handler) {
			/*
			if (global.WorldMgr.getAllowMovement())
			{
				global.WorldMgr.SetAllowMovement(false);
				handler.sendSysMessage(LANG_CREATURE_MOVE_DISABLED);
			}
			else
			{
				global.WorldMgr.SetAllowMovement(true);
				handler.sendSysMessage(LANG_CREATURE_MOVE_ENABLED);
			}
			*/
            return true;
        }

        
        private static boolean handleNpcSetDataCommand(CommandHandler handler, int data_1, int data_2) {
            var creature = handler.getSelectedCreature();

            if (!creature) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            creature.getAI().setData(data_1, data_2);
            var AIorScript = !Objects.equals(creature.getAIName(), "") ? "AI type: " + creature.getAIName() : (!Objects.equals(creature.getScriptName(), "") ? "Script Name: " + creature.getScriptName() : "No AI or Script Name Set");
            handler.sendSysMessage(SysMessage.NpcSetdata, creature.getGUID(), creature.getEntry(), creature.getName(), data_1, data_2, AIorScript);

            return true;
        }

        
        private static boolean handleNpcSetEntryCommand(CommandHandler handler, int newEntryNum) {
            if (newEntryNum == 0) {
                return false;
            }

            var unit = handler.getSelectedUnit();

            if (!unit || !unit.isTypeId(TypeId.UNIT)) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            var creature = unit.toCreature();

            if (creature.updateEntry(newEntryNum)) {
                handler.sendSysMessage(SysMessage.Done);
            } else {
                handler.sendSysMessage(SysMessage.error);
            }

            return true;
        }

        
        private static boolean handleNpcSetFactionIdCommand(CommandHandler handler, int factionId) {
            if (!CliDB.FactionTemplateStorage.containsKey(factionId)) {
                handler.sendSysMessage(SysMessage.WrongFaction, factionId);

                return false;
            }

            var creature = handler.getSelectedCreature();

            if (!creature) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            creature.setFaction(factionId);

            // Faction is set in creature_template - not inside creature

            // Update in memory..
            var cinfo = creature.getCreatureTemplate();

            if (cinfo != null) {
                cinfo.faction = factionId;
            }

            // ..and DB
            var stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_FACTION);

            stmt.AddValue(0, factionId);
            stmt.AddValue(1, factionId);
            stmt.AddValue(2, creature.getEntry());

            DB.World.execute(stmt);

            return true;
        }

        
        private static boolean handleNpcSetFlagCommand(CommandHandler handler, NPCFlags npcFlags, NPCFlags2 npcFlags2) {
            var creature = handler.getSelectedCreature();

            if (!creature) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            creature.replaceAllNpcFlags(npcFlags);
            creature.replaceAllNpcFlags2(npcFlags2);

            var stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_NPCFLAG);
            stmt.AddValue(0, (long) npcFlags.getValue() | ((long) npcFlags2.getValue() << 32));
            stmt.AddValue(1, creature.getEntry());
            DB.World.execute(stmt);

            handler.sendSysMessage(SysMessage.ValueSavedRejoin);

            return true;
        }

        
        private static boolean handleNpcSetLevelCommand(CommandHandler handler, byte lvl) {
            if (lvl < 1 || lvl > WorldConfig.getIntValue(WorldCfg.MaxPlayerLevel) + 3) {
                handler.sendSysMessage(SysMessage.BadValue);

                return false;
            }

            var creature = handler.getSelectedCreature();

            if (!creature || creature.isPet()) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            creature.setMaxHealth((int) (100 + 30 * lvl));
            creature.setHealth((int) (100 + 30 * lvl));
            creature.setLevel(lvl);
            creature.saveToDB();

            return true;
        }

        
        private static boolean handleNpcSetLinkCommand(CommandHandler handler, long linkguid) {
            var creature = handler.getSelectedCreature();

            if (!creature) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            if (creature.getSpawnId() == 0) {
                handler.sendSysMessage("Selected creature {0} isn't in creature table", creature.getGUID().toString());

                return false;
            }

            if (!global.getObjectMgr().setCreatureLinkedRespawn(creature.getSpawnId(), linkguid)) {
                handler.sendSysMessage("Selected creature can't link with guid '{0}'", linkguid);

                return false;
            }

            handler.sendSysMessage("LinkGUID '{0}' added to creature with DBTableGUID: '{1}'", linkguid, creature.getSpawnId());

            return true;
        }

        
        private static boolean handleNpcSetModelCommand(CommandHandler handler, int displayId) {
            var creature = handler.getSelectedCreature();

            if (!creature || creature.isPet()) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            if (!CliDB.CreatureDisplayInfoStorage.containsKey(displayId)) {
                handler.sendSysMessage(SysMessage.CommandInvalidParam, displayId);

                return false;
            }

            creature.setDisplayId(displayId);
            creature.setNativeDisplayId(displayId);

            creature.saveToDB();

            return true;
        }

        
        private static boolean handleNpcSetMoveTypeCommand(CommandHandler handler, Long lowGuid, String type, String nodel) {
            // 3 arguments:
            // GUID (optional - you can also select the creature)
            // stay|random|way (determines the kind of movement)
            // NODEL (optional - tells the system NOT to delete any waypoints)
            //        this is very handy if you want to do waypoints, that are
            //        later switched on/off according to special events (like escort
            //        quests, etc)
            var doNotDelete = !nodel.isEmpty();

            long lowguid = 0;
            Creature creature = null;

            if (lowGuid == null) // case .setmovetype $move_type (with selected creature)
            {
                creature = handler.getSelectedCreature();

                if (!creature || creature.isPet()) {
                    return false;
                }

                lowguid = creature.getSpawnId();
            } else {
                lowguid = lowGuid.longValue();

                if (lowguid != 0) {
                    creature = handler.getCreatureFromPlayerMapByDbGuid(lowguid);
                }

                // attempt check creature existence by DB data
                if (creature == null) {
                    var data = global.getObjectMgr().getCreatureData(lowguid);

                    if (data == null) {
                        handler.sendSysMessage(SysMessage.CommandCreatguidnotfound, lowguid);

                        return false;
                    }
                } else {
                    lowguid = creature.getSpawnId();
                }
            }

            // now lowguid is low guid really existed creature
            // and creature point (maybe) to this creature or NULL

            MovementGeneratorType move_type;

            switch (type) {
                case "stay":
                    move_type = MovementGeneratorType.IDLE;

                    break;
                case "random":
                    move_type = MovementGeneratorType.random;

                    break;
                case "way":
                    move_type = MovementGeneratorType.Waypoint;

                    break;
                default:
                    return false;
            }

            if (creature) {
                // update movement type
                if (!doNotDelete) {
                    creature.loadPath(0);
                }

                creature.setDefaultMovementType(move_type);
                creature.getMotionMaster().initialize();

                if (creature.isAlive()) // dead creature will reset movement generator at respawn
                {
                    creature.setDeathState(deathState.JustDied);
                    creature.respawn();
                }

                creature.saveToDB();
            }

            if (!doNotDelete) {
                handler.sendSysMessage(SysMessage.MoveTypeSet, type);
            } else {
                handler.sendSysMessage(SysMessage.MoveTypeSetNodel, type);
            }

            return true;
        }

        
        private static boolean handleNpcSetPhaseCommand(CommandHandler handler, int phaseId) {
            if (phaseId == 0) {
                handler.sendSysMessage(SysMessage.PhaseNotfound);

                return false;
            }

            var creature = handler.getSelectedCreature();

            if (!creature || creature.isPet()) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            PhasingHandler.resetPhaseShift(creature);
            PhasingHandler.addPhase(creature, phaseId, true);
            creature.setDBPhase((int) phaseId);

            creature.saveToDB();

            return true;
        }

        
        private static boolean handleNpcSetPhaseGroup(CommandHandler handler, StringArguments args) {
            if (args.isEmpty()) {
                return false;
            }

            var phaseGroupId = args.NextInt32(" ");

            var creature = handler.getSelectedCreature();

            if (!creature || creature.isPet()) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            PhasingHandler.resetPhaseShift(creature);
            PhasingHandler.addPhaseGroup(creature, (int) phaseGroupId, true);
            creature.setDBPhase(-phaseGroupId);

            creature.saveToDB();

            return true;
        }

        
        private static boolean handleNpcSetWanderDistanceCommand(CommandHandler handler, float option) {
            if (option < 0.0f) {
                handler.sendSysMessage(SysMessage.BadValue);

                return false;
            }

            var mtype = MovementGeneratorType.IDLE;

            if (option > 0.0f) {
                mtype = MovementGeneratorType.random;
            }

            var creature = handler.getSelectedCreature();
            long guidLow;

            if (creature) {
                guidLow = creature.getSpawnId();
            } else {
                return false;
            }

            creature.setWanderDistance(option);
            creature.setDefaultMovementType(mtype);
            creature.getMotionMaster().initialize();

            if (creature.isAlive()) // dead creature will reset movement generator at respawn
            {
                creature.setDeathState(deathState.JustDied);
                creature.respawn();
            }

            var stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_WANDER_DISTANCE);
            stmt.AddValue(0, option);
            stmt.AddValue(1, (byte) mtype.getValue());
            stmt.AddValue(2, guidLow);

            DB.World.execute(stmt);

            handler.sendSysMessage(SysMessage.CommandWanderDistance, option);

            return true;
        }

        
        private static boolean handleNpcSetSpawnTimeCommand(CommandHandler handler, int spawnTime) {
            var creature = handler.getSelectedCreature();

            if (!creature) {
                return false;
            }

            var stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_SPAWN_TIME_SECS);
            stmt.AddValue(0, spawnTime);
            stmt.AddValue(1, creature.getSpawnId());
            DB.World.execute(stmt);

            creature.setRespawnDelay(spawnTime);
            handler.sendSysMessage(SysMessage.CommandSpawntime, spawnTime);

            return true;
        }
    }
}
