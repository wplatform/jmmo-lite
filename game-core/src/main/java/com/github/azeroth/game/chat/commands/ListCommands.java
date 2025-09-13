package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.PlayerIdentifier;
import com.github.azeroth.game.domain.spawn.RespawnInfo;
import com.github.azeroth.game.domain.spawn.SpawnData;
import com.github.azeroth.game.spell.SpellInfo;
import game.PhasingHandler;

import java.util.ArrayList;

class ListCommands {

    private static boolean handleListCreatureCommand(CommandHandler handler, int creatureId, Integer countArg) {
        var cInfo = global.getObjectMgr().getCreatureTemplate(creatureId);

        if (cInfo == null) {
            handler.sendSysMessage(SysMessage.CommandInvalidcreatureid, creatureId);

            return false;
        }

        var count = (countArg == null ? 10 : countArg.intValue());

        if (count == 0) {
            return false;
        }

        int creatureCount = 0;
        var result = DB.World.query("SELECT COUNT(guid) FROM creature WHERE id='{0}'", creatureId);

        if (!result.isEmpty()) {
            creatureCount = result.<Integer>Read(0);
        }

        if (handler.getSession() != null) {
            var player = handler.getSession().getPlayer();

            result = DB.World.query("SELECT guid, position_x, position_y, position_z, map, (POW(position_x - '{0}', 2) + POW(position_y - '{1}', 2) + POW(position_z - '{2}', 2)) AS order_ FROM creature WHERE id = '{3}' ORDER BY order_ ASC LIMIT {4}", player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), creatureId, count);
        } else {
            result = DB.World.query("SELECT guid, position_x, position_y, position_z, map FROM creature WHERE id = '{0}' LIMIT {1}", creatureId, count);
        }

        if (!result.isEmpty()) {
            do {
                var guid = result.<Long>Read(0);
                var x = result.<Float>Read(1);
                var y = result.<Float>Read(2);
                var z = result.<Float>Read(3);
                var mapId = result.<SHORT>Read(4);
                var liveFound = false;

                // Get map (only support base map from console)
                Map thisMap = null;

                if (handler.getSession() != null) {
                    thisMap = handler.getSession().getPlayer().getMap();
                }

                // If map found, try to find active version of this creature
                if (thisMap) {
                    var creBounds = thisMap.getCreatureBySpawnIdStore().get(guid);

                    for (var creature : creBounds) {
                        handler.sendSysMessage(SysMessage.CreatureListChat, guid, guid, cInfo.name, x, y, z, mapId, creature.getGUID().toString(), creature.isAlive() ? "*" : " ");
                    }

                    liveFound = !creBounds.isEmpty();
                }

                if (!liveFound) {
                    if (handler.getSession()) {
                        handler.sendSysMessage(SysMessage.CreatureListChat, guid, guid, cInfo.name, x, y, z, mapId, "", "");
                    } else {
                        handler.sendSysMessage(SysMessage.CreatureListConsole, guid, cInfo.name, x, y, z, mapId, "", "");
                    }
                }
            } while (result.NextRow());
        }

        handler.sendSysMessage(SysMessage.CommandListcreaturemessage, creatureId, creatureCount);

        return true;
    }


    private static boolean handleListItemCommand(CommandHandler handler, int itemId, Integer countArg) {
        var count = (countArg == null ? 10 : countArg.intValue());

        if (count == 0) {
            return false;
        }

        // inventory case
        int inventoryCount = 0;

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_INVENTORY_COUNT_ITEM);
        stmt.AddValue(0, itemId);
        var result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            inventoryCount = result.<Integer>Read(0);
        }

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_INVENTORY_ITEM_BY_ENTRY);
        stmt.AddValue(0, itemId);
        stmt.AddValue(1, count);
        result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            do {
                var itemGuid = ObjectGuid.create(HighGuid.Item, result.<Long>Read(0));
                var itemBag = result.<Integer>Read(1);
                var itemSlot = result.<Byte>Read(2);
                var ownerGuid = ObjectGuid.create(HighGuid.Player, result.<Long>Read(3));
                var ownerAccountId = result.<Integer>Read(4);
                var ownerName = result.<String>Read(5);

                String itemPos;

                if (player.isEquipmentPos((byte) itemBag, itemSlot)) {
                    itemPos = "[equipped]";
                } else if (player.isInventoryPos((byte) itemBag, itemSlot)) {
                    itemPos = "[in inventory]";
                } else if (player.isReagentBankPos((byte) itemBag, itemSlot)) {
                    itemPos = "[in reagent bank]";
                } else if (player.isBankPos((byte) itemBag, itemSlot)) {
                    itemPos = "[in bank]";
                } else {
                    itemPos = "";
                }

                handler.sendSysMessage(SysMessage.ItemlistSlot, itemGuid.toString(), ownerName, ownerGuid.toString(), ownerAccountId, itemPos);

                count--;
            } while (result.NextRow());
        }

        // mail case
        int mailCount = 0;

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAIL_COUNT_ITEM);
        stmt.AddValue(0, itemId);
        result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            mailCount = result.<Integer>Read(0);
        }

        if (count > 0) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAIL_ITEMS_BY_ENTRY);
            stmt.AddValue(0, itemId);
            stmt.AddValue(1, count);
            result = DB.characters.query(stmt);
        } else {
            result = null;
        }

        if (result != null && !result.isEmpty()) {
            do {
                var itemGuid = result.<Long>Read(0);
                var itemSender = result.<Long>Read(1);
                var itemReceiver = result.<Long>Read(2);
                var itemSenderAccountId = result.<Integer>Read(3);
                var itemSenderName = result.<String>Read(4);
                var itemReceiverAccount = result.<Integer>Read(5);
                var itemReceiverName = result.<String>Read(6);

                var itemPos = "[in mail]";

                handler.sendSysMessage(SysMessage.ItemlistMail, itemGuid, itemSenderName, itemSender, itemSenderAccountId, itemReceiverName, itemReceiver, itemReceiverAccount, itemPos);

                count--;
            } while (result.NextRow());
        }

        // auction case
        int auctionCount = 0;

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_AUCTIONHOUSE_COUNT_ITEM);
        stmt.AddValue(0, itemId);
        result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            auctionCount = result.<Integer>Read(0);
        }

        if (count > 0) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_AUCTIONHOUSE_ITEM_BY_ENTRY);
            stmt.AddValue(0, itemId);
            stmt.AddValue(1, count);
            result = DB.characters.query(stmt);
        } else {
            result = null;
        }

        if (result != null && !result.isEmpty()) {
            do {
                var itemGuid = ObjectGuid.create(HighGuid.Item, result.<Long>Read(0));
                var owner = ObjectGuid.create(HighGuid.Player, result.<Long>Read(1));
                var ownerAccountId = result.<Integer>Read(2);
                var ownerName = result.<String>Read(3);

                var itemPos = "[in auction]";

                handler.sendSysMessage(SysMessage.ItemlistAuction, itemGuid.toString(), ownerName, owner.toString(), ownerAccountId, itemPos);
            } while (result.NextRow());
        }

        // guild bank case
        int guildCount = 0;

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GUILD_BANK_COUNT_ITEM);
        stmt.AddValue(0, itemId);
        result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            guildCount = result.<Integer>Read(0);
        }

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GUILD_BANK_ITEM_BY_ENTRY);
        stmt.AddValue(0, itemId);
        stmt.AddValue(1, count);
        result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            do {
                var itemGuid = ObjectGuid.create(HighGuid.Item, result.<Long>Read(0));
                var guildGuid = ObjectGuid.create(HighGuid.Guild, result.<Long>Read(1));
                var guildName = result.<String>Read(2);

                var itemPos = "[in guild bank]";

                handler.sendSysMessage(SysMessage.ItemlistGuild, itemGuid.toString(), guildName, guildGuid.toString(), itemPos);

                count--;
            } while (result.NextRow());
        }

        if (inventoryCount + mailCount + auctionCount + guildCount == 0) {
            handler.sendSysMessage(SysMessage.CommandNoitemfound);

            return false;
        }

        handler.sendSysMessage(SysMessage.CommandListitemmessage, itemId, inventoryCount + mailCount + auctionCount + guildCount, inventoryCount, mailCount, auctionCount, guildCount);

        return true;
    }


    private static boolean handleListMailCommand(CommandHandler handler, PlayerIdentifier player) {
        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null) {
            return false;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAIL_LIST_COUNT);
        stmt.AddValue(0, player.getGUID().getCounter());
        var result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            var countMail = result.<Integer>Read(0);

            var nameLink = handler.playerLink(player.getName());
            handler.sendSysMessage(SysMessage.ListMailHeader, countMail, nameLink, player.getGUID().toString());
            handler.sendSysMessage(SysMessage.AccountListBar);

            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAIL_LIST_INFO);
            stmt.AddValue(0, player.getGUID().getCounter());
            var result1 = DB.characters.query(stmt);

            if (!result1.isEmpty()) {
                do {
                    var messageId = result1.<Integer>Read(0);
                    var senderId = result1.<Long>Read(1);
                    var sender = result1.<String>Read(2);
                    var receiverId = result1.<Long>Read(3);
                    var receiver = result1.<String>Read(4);
                    var subject = result1.<String>Read(5);
                    var deliverTime = result1.<Long>Read(6);
                    var expireTime = result1.<Long>Read(7);
                    var money = result1.<Long>Read(8);
                    var hasItem = result1.<Byte>Read(9);
                    var gold = (int) (money / MoneyConstants.gold);
                    var silv = (int) (money % MoneyConstants.gold) / MoneyConstants.Silver;
                    var copp = (int) (money % MoneyConstants.gold) % MoneyConstants.Silver;
                    var receiverStr = handler.playerLink(receiver);
                    var senderStr = handler.playerLink(sender);
                    handler.sendSysMessage(SysMessage.ListMailInfo1, messageId, subject, gold, silv, copp);
                    handler.sendSysMessage(SysMessage.ListMailInfo2, senderStr, senderId, receiverStr, receiverId);
                    handler.sendSysMessage(SysMessage.ListMailInfo3, time.UnixTimeToDateTime(deliverTime).ToLongDateString(), time.UnixTimeToDateTime(expireTime).ToLongDateString());

                    if (hasItem == 1) {
                        var result2 = DB.characters.query("SELECT item_guid FROM mail_items WHERE mail_id = '{0}'", messageId);

                        if (!result2.isEmpty()) {
                            do {
                                var item_guid = result2.<Integer>Read(0);
                                stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAIL_LIST_ITEMS);
                                stmt.AddValue(0, item_guid);
                                var result3 = DB.characters.query(stmt);

                                if (!result3.isEmpty()) {
                                    do {
                                        var item_entry = result3.<Integer>Read(0);
                                        var item_count = result3.<Integer>Read(1);

                                        var itemTemplate = global.getObjectMgr().getItemTemplate(item_entry);

                                        if (itemTemplate == null) {
                                            continue;
                                        }

                                        if (handler.getSession() != null) {
                                            var color = ItemConst.ItemQualityColors[itemTemplate.getQuality().getValue()];
                                            var itemStr = String.format("|c%1$s|Hitem:%2$s:0:0:0:0:0:0:0:%3$s:0:0:0:0:0|h[%4$s]|h|r", color, item_entry, handler.getSession().getPlayer().getLevel(), itemTemplate.getName(handler.getSessionDbcLocale()));
                                            handler.sendSysMessage(SysMessage.ListMailInfoItem, itemStr, item_entry, item_guid, item_count);
                                        } else {
                                            handler.sendSysMessage(SysMessage.ListMailInfoItem, itemTemplate.getName(handler.getSessionDbcLocale()), item_entry, item_guid, item_count);
                                        }
                                    } while (result3.NextRow());
                                }
                            } while (result2.NextRow());
                        }
                    }

                    handler.sendSysMessage(SysMessage.AccountListBar);
                } while (result1.NextRow());
            } else {
                handler.sendSysMessage(SysMessage.ListMailNotFound);
            }

            return true;
        } else {
            handler.sendSysMessage(SysMessage.ListMailNotFound);
        }

        return true;
    }


    private static boolean handleListObjectCommand(CommandHandler handler, int gameObjectId, Integer countArg) {
        var gInfo = global.getObjectMgr().getGameObjectTemplate(gameObjectId);

        if (gInfo == null) {
            handler.sendSysMessage(SysMessage.CommandListobjinvalidid, gameObjectId);

            return false;
        }

        var count = (countArg == null ? 10 : countArg.intValue());

        if (count == 0) {
            return false;
        }

        int objectCount = 0;
        var result = DB.World.query("SELECT COUNT(guid) FROM gameobject WHERE id='{0}'", gameObjectId);

        if (!result.isEmpty()) {
            objectCount = result.<Integer>Read(0);
        }

        if (handler.getSession() != null) {
            var player = handler.getSession().getPlayer();

            result = DB.World.query("SELECT guid, position_x, position_y, position_z, map, id, (POW(position_x - '{0}', 2) + POW(position_y - '{1}', 2) + POW(position_z - '{2}', 2)) AS order_ FROM gameobject WHERE id = '{3}' ORDER BY order_ ASC LIMIT {4}", player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), gameObjectId, count);
        } else {
            result = DB.World.query("SELECT guid, position_x, position_y, position_z, map, id FROM gameobject WHERE id = '{0}' LIMIT {1}", gameObjectId, count);
        }

        if (!result.isEmpty()) {
            do {
                var guid = result.<Long>Read(0);
                var x = result.<Float>Read(1);
                var y = result.<Float>Read(2);
                var z = result.<Float>Read(3);
                var mapId = result.<SHORT>Read(4);
                var entry = result.<Integer>Read(5);
                var liveFound = false;

                // Get map (only support base map from console)
                Map thisMap = null;

                if (handler.getSession() != null) {
                    thisMap = handler.getSession().getPlayer().getMap();
                }

                // If map found, try to find active version of this object
                if (thisMap) {
                    var goBounds = thisMap.getGameObjectBySpawnIdStore().get(guid);

                    for (var go : goBounds) {
                        handler.sendSysMessage(SysMessage.GoListChat, guid, entry, guid, gInfo.name, x, y, z, mapId, go.getGUID().toString(), go.isSpawned() ? "*" : " ");
                    }

                    liveFound = !goBounds.isEmpty();
                }

                if (!liveFound) {
                    if (handler.getSession()) {
                        handler.sendSysMessage(SysMessage.GoListChat, guid, entry, guid, gInfo.name, x, y, z, mapId, "", "");
                    } else {
                        handler.sendSysMessage(SysMessage.GoListConsole, guid, gInfo.name, x, y, z, mapId, "", "");
                    }
                }
            } while (result.NextRow());
        }

        handler.sendSysMessage(SysMessage.CommandListobjmessage, gameObjectId, objectCount);

        return true;
    }


    private static boolean handleListRespawnsCommand(CommandHandler handler, Integer range) {
        var player = handler.getSession().getPlayer();
        var map = player.getMap();

        var locale = handler.getSession().getSessionDbcLocale();
        var stringOverdue = global.getObjectMgr().getSysMessage(SysMessage.ListRespawnsOverdue, locale);

        var zoneId = player.getZone();
        var zoneName = getZoneName(zoneId, locale);

        for (SpawnObjectType type = 0; type.getValue() < SpawnObjectType.NumSpawnTypes.getValue(); type++) {
            if (range != null) {
                handler.sendSysMessage(SysMessage.ListRespawnsRange, type, range.intValue());
            } else {
                handler.sendSysMessage(SysMessage.ListRespawnsZone, type, zoneName, zoneId);
            }

            handler.sendSysMessage(SysMessage.ListRespawnsListheader);
            ArrayList<RespawnInfo> respawns = new ArrayList<>();
            map.getRespawnInfo(respawns, SpawnObjectTypeMask.forValue(1 << type.getValue()));

            for (var ri : respawns) {
                var data = global.getObjectMgr().getSpawnMetadata(ri.getObjectType(), ri.getSpawnId());

                if (data == null) {
                    continue;
                }

                int respawnZoneId = 0;
                var edata = data.toSpawnData();

                if (edata != null) {
                    respawnZoneId = map.getZoneId(PhasingHandler.EMPTY_PHASE_SHIFT, edata.spawnPoint);

                    if (range != null) {
                        if (!player.getLocation().isInDist(edata.spawnPoint, range.intValue())) {
                            continue;
                        }
                    } else {
                        if (zoneId != respawnZoneId) {
                            continue;
                        }
                    }
                }

                var gridY = ri.getGridId() / MapDefine.MaxGrids;
                var gridX = ri.getGridId() % MapDefine.MaxGrids;

                var respawnTime = ri.getRespawnTime() > gameTime.GetGameTime() ? time.secsToTimeString((long) (ri.getRespawnTime() - gameTime.GetGameTime()), TimeFormat.ShortText, false) : stringOverdue;


                handler.sendSysMessage(String.format("%1$s | %2$s | [{2:2},{3:2}] | %5$s (%6$s) | %7$s%8$s", ri.getSpawnId(), ri.getEntry(), gridX, gridY, getZoneName(respawnZoneId, locale), respawnZoneId, respawnTime, (map.isSpawnGroupActive(data.getSpawnGroupData().getGroupId()) ? "" : " (inactive)")));
            }
        }

        return true;
    }


    private static boolean handleListScenesCommand(CommandHandler handler) {
        var target = handler.getSelectedPlayer();

        if (!target) {
            target = handler.getSession().getPlayer();
        }

        if (!target) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var instanceByPackageMap = target.getSceneMgr().getSceneTemplateByInstanceMap();

        handler.sendSysMessage(SysMessage.DebugSceneObjectList, target.getSceneMgr().getActiveSceneCount());

        for (var instanceByPackage : instanceByPackageMap.entrySet()) {
            handler.sendSysMessage(SysMessage.DebugSceneObjectDetail, instanceByPackage.getValue().scenePackageId, instanceByPackage.getKey());
        }

        return true;
    }


    private static boolean handleListSpawnPointsCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();
        var map = player.getMap();
        var mapId = map.getId();
        var showAll = map.isBattlegroundOrArena() || map.isDungeon();
        handler.sendSysMessage(String.format("Listing all spawn points in map %1$s (%2$s)%3$s:", mapId, map.getMapName(), (showAll ? "" : " within 5000yd")));

        for (var pair : global.getObjectMgr().getAllCreatureData().entrySet()) {
            SpawnData data = pair.getValue();

            if (data.getMapId() != mapId) {
                continue;
            }

            var cTemp = global.getObjectMgr().getCreatureTemplate(data.id);

            if (cTemp == null) {
                continue;
            }

            if (showAll || data.spawnPoint.isInDist2D(player.getLocation(), 5000.0f)) {



                handler.sendSysMessage(String.format("Type: %1$s | SpawnId: %2$s | Entry: %3$s (%4$s) | X: {4:3} | Y: {5:3} | Z: {6:3}", data.getType(), data.getSpawnId(), data.id, cTemp.name, data.spawnPoint.getX(), data.spawnPoint.getY(), data.spawnPoint.getZ()));
            }
        }

        for (var pair : global.getObjectMgr().getAllGameObjectData().entrySet()) {
            SpawnData data = pair.getValue();

            if (data.getMapId() != mapId) {
                continue;
            }

            var goTemp = global.getObjectMgr().getGameObjectTemplate(data.id);

            if (goTemp == null) {
                continue;
            }

            if (showAll || data.spawnPoint.isInDist2D(player.getLocation(), 5000.0f)) {



                handler.sendSysMessage(String.format("Type: %1$s | SpawnId: %2$s | Entry: %3$s (%4$s) | X: {4:3} | Y: {5:3} | Z: {6:3}", data.getType(), data.getSpawnId(), data.id, goTemp.name, data.spawnPoint.getX(), data.spawnPoint.getY(), data.spawnPoint.getZ()));
            }
        }

        return true;
    }


    private static String getZoneName(int zoneId, Locale locale) {
        var zoneEntry = CliDB.AreaTableStorage.get(zoneId);

        return zoneEntry != null ? zoneEntry.AreaName[locale] : "<unknown zone>";
    }


    private static class ListAuraCommands {

        private static boolean handleListAllAurasCommand(CommandHandler handler) {
            return listAurasCommand(handler, null, null);
        }


        private static boolean handleListAurasByIdCommand(CommandHandler handler, int spellId) {
            return listAurasCommand(handler, spellId, null);
        }


        private static boolean handleListAurasByNameCommand(CommandHandler handler, Tail namePart) {
            return listAurasCommand(handler, null, namePart);
        }


        private static boolean listAurasCommand(CommandHandler handler, Integer spellId, String namePart) {
            var unit = handler.getSelectedUnit();

            if (!unit) {
                handler.sendSysMessage(SysMessage.SelectCharOrCreature);

                return false;
            }

            var talentStr = handler.getSysMessage(SysMessage.Talent);
            var passiveStr = handler.getSysMessage(SysMessage.Passive);

            var auras = unit.getAppliedAuras();
            handler.sendSysMessage(SysMessage.CommandTargetListauras, unit.getAppliedAurasCount());

            for (var aurApp : auras) {
                var aura = aurApp.base;
                var name = aura.spellInfo.SpellName[handler.getSessionDbcLocale()];
                var talent = aura.spellInfo.hasAttribute(SpellCustomAttributes.IsTalent);

                if (!shouldListAura(aura.spellInfo, spellId, namePart, handler.getSessionDbcLocale())) {
                    continue;
                }

                var ss_name = "|cffffffff|Hspell:" + aura.id + "|h[" + name + "]|h|r";

                handler.sendSysMessage(SysMessage.CommandTargetAuradetail, aura.id, (handler.getSession() != null ? ss_name : name), aurApp.effectMask.ToMask(), aura.charges, aura.stackAmount, aurApp.slot, aura.duration, aura.maxDuration, (aura.IsPassive ? passiveStr : ""), (talent ? talentStr : ""), aura.casterGuid.IsPlayer ? "player" : "creature", aura.casterGuid.toString());
            }

            for (AuraType auraType = 0; AuraType.getValue() < AuraType.Total.getValue(); ++auraType) {
                var auraList = unit.getAuraEffectsByType(auraType);

                if (auraList.isEmpty()) {
                    continue;
                }

                var sizeLogged = false;

                for (var effect : auraList) {
                    if (!shouldListAura(effect.getSpellInfo(), spellId, namePart, handler.getSessionDbcLocale())) {
                        continue;
                    }

                    if (!sizeLogged) {
                        sizeLogged = true;
                        handler.sendSysMessage(SysMessage.CommandTargetListauratype, auraList.size(), auraType);
                    }

                    handler.sendSysMessage(SysMessage.CommandTargetAurasimple, effect.getId(), effect.getEffIndex(), effect.getAmount());
                }
            }

            return true;
        }


        private static boolean shouldListAura(SpellInfo spellInfo, Integer spellId, String namePart, Locale locale) {
            if (spellId != null) {
                return spellInfo.getId() == spellId.intValue();
            }

            if (!namePart.isEmpty()) {
                var name = spellInfo.getSpellName().get(locale);

                return name.Like(namePart);
            }

            return true;
        }
    }
}
