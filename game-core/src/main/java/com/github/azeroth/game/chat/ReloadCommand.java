package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.item.ItemEnchantmentManager;
import com.github.azeroth.game.loot.LootManager;
import com.github.azeroth.game.loot.LootStorage;
import com.github.azeroth.game.spell.SkillDiscovery;
import com.github.azeroth.game.spell.SkillExtraItems;
import com.github.azeroth.game.spell.SkillPerfectItems;


class ReloadCommand {

    private static boolean handleReloadAccessRequirementCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Access Requirement definitions...");
        global.getObjectMgr().loadAccessRequirements();
        handler.sendGlobalGMSysMessage("DB table `access_requirement` reloaded.");

        return true;
    }


    private static boolean handleReloadAchievementRewardCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Achievement Reward data...");
        global.getAchievementMgr().loadRewards();
        handler.sendGlobalGMSysMessage("DB table `achievement_reward` reloaded.");

        return true;
    }


    private static boolean handleReloadQuestAreaTriggersCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Quest Area Triggers...");
        global.getObjectMgr().loadQuestAreaTriggers();
        handler.sendGlobalGMSysMessage("DB table `areatrigger_involvedrelation` (quest area triggers) reloaded.");

        return true;
    }


    private static boolean handleReloadAreaTriggerTavernCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Tavern Area Triggers...");
        global.getObjectMgr().loadTavernAreaTriggers();
        handler.sendGlobalGMSysMessage("DB table `areatrigger_tavern` reloaded.");

        return true;
    }


    private static boolean handleReloadAreaTriggerTeleportCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading AreaTrigger teleport definitions...");
        global.getObjectMgr().loadAreaTriggerTeleports();
        handler.sendGlobalGMSysMessage("DB table `areatrigger_teleport` reloaded.");

        return true;
    }


    private static boolean handleReloadAreaTriggerTemplateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Reloading areatrigger_template table...");
        global.getAreaTriggerDataStorage().LoadAreaTriggerTemplates();
        handler.sendGlobalGMSysMessage("AreaTrigger templates reloaded. Already spawned AT won't be affected. New scriptname need a reboot.");

        return true;
    }


    private static boolean handleReloadAuctionsCommand(CommandHandler handler) {
        // Reload dynamic data tables from the database
        Log.outInfo(LogFilter.Server, "Re-Loading auctions...");
        global.getAuctionHouseMgr().loadAuctions();
        handler.sendGlobalGMSysMessage("Auctions reloaded.");

        return true;
    }


    private static boolean handleReloadAutobroadcastCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Autobroadcasts...");
        global.getWorldMgr().loadAutobroadcasts();
        handler.sendGlobalGMSysMessage("DB table `autobroadcast` reloaded.");

        return true;
    }


    private static boolean handleReloadBattlegroundTemplate(CommandHandler handler) {
        Log.outInfo(LogFilter.misc, "Re-Loading Battleground templates...");
        global.getBattlegroundMgr().loadBattlegroundTemplates();
        handler.sendGlobalGMSysMessage("DB table `battleground_template` reloaded.");

        return true;
    }


    private static boolean handleReloadCharacterTemplate(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Character templates...");
        global.getCharacterTemplateDataStorage().LoadCharacterTemplates();
        handler.sendGlobalGMSysMessage("DB table `character_template` and `character_template_class` reloaded.");

        return true;
    }


    private static boolean handleReloadConditions(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading conditions...");
        global.getConditionMgr().loadConditions(true);
        handler.sendGlobalGMSysMessage("Conditions reloaded.");

        return true;
    }


    private static boolean handleReloadConfigCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading config settings...");
        global.getWorldMgr().loadConfigSettings(true);
        global.getMapMgr().InitializeVisibilityDistanceInfo();
        handler.sendGlobalGMSysMessage("World config settings reloaded.");

        return true;
    }


    private static boolean handleReloadConversationTemplateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Reloading conversation_* tables...");
        global.getConversationDataStorage().LoadConversationTemplates();
        handler.sendGlobalGMSysMessage("Conversation templates reloaded.");

        return true;
    }


    private static boolean handleReloadLinkedRespawnCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Loading Linked Respawns... (`creature_linked_respawn`)");
        global.getObjectMgr().loadLinkedRespawn();
        handler.sendGlobalGMSysMessage("DB table `creature_linked_respawn` (creature linked respawns) reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesCreatureCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`creature_loot_template`)");
        LootManager.loadLootTemplates_Creature();
        LootStorage.CREATURE.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `creature_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadCreatureMovementOverrideCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Creature movement overrides...");
        global.getObjectMgr().loadCreatureMovementOverrides();
        handler.sendGlobalGMSysMessage("DB table `creature_movement_override` reloaded.");

        return true;
    }


    private static boolean handleReloadOnKillReputationCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading creature award reputation definitions...");
        global.getObjectMgr().loadReputationOnKill();
        handler.sendGlobalGMSysMessage("DB table `creature_onkill_reputation` reloaded.");

        return true;
    }


    private static boolean handleReloadCreatureQuestEnderCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Loading Quests Relations... (`creature_questender`)");
        global.getObjectMgr().loadCreatureQuestEnders();
        handler.sendGlobalGMSysMessage("DB table `creature_questender` reloaded.");

        return true;
    }


    private static boolean handleReloadCreatureQuestStarterCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Loading Quests Relations... (`creature_queststarter`)");
        global.getObjectMgr().loadCreatureQuestStarters();
        handler.sendGlobalGMSysMessage("DB table `creature_queststarter` reloaded.");

        return true;
    }


    private static boolean handleReloadCreatureSummonGroupsCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Reloading creature summon groups...");
        global.getObjectMgr().loadTempSummons();
        handler.sendGlobalGMSysMessage("DB table `creature_summon_groups` reloaded.");

        return true;
    }


    private static boolean handleReloadCreatureTemplateCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        int entry;

        while ((entry = args.NextUInt32(" ")) != 0) {
            var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_CREATURE_TEMPLATE);
            stmt.AddValue(0, entry);
            stmt.AddValue(1, 0);
            var result = DB.World.query(stmt);

            if (result.isEmpty()) {
                handler.sendSysMessage(SysMessage.CommandCreaturetemplateNotfound, entry);

                continue;
            }

            var cInfo = global.getObjectMgr().getCreatureTemplate(entry);

            if (cInfo == null) {
                handler.sendSysMessage(SysMessage.CommandCreaturestorageNotfound, entry);

                continue;
            }

            Log.outInfo(LogFilter.Server, "Reloading creature template entry {0}", entry);

            global.getObjectMgr().loadCreatureTemplate(result.GetFields());
            global.getObjectMgr().checkCreatureTemplate(cInfo);
        }

        global.getObjectMgr().initializeQueriesData(QueryDataGroup.Creatures);
        handler.sendGlobalGMSysMessage("Creature template reloaded.");

        return true;
    }


    private static boolean handleReloadCreatureText(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Creature Texts...");
        global.getCreatureTextMgr().loadCreatureTexts();
        handler.sendGlobalGMSysMessage("Creature Texts reloaded.");

        return true;
    }


    private static boolean handleReloadCypherStringCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading trinity_string Table!");
        global.getObjectMgr().loadSysMessage();
        handler.sendGlobalGMSysMessage("DB table `trinity_string` reloaded.");

        return true;
    }


    private static boolean handleReloadCriteriaDataCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Additional Criteria data...");
        global.getCriteriaMgr().loadCriteriaData();
        handler.sendGlobalGMSysMessage("DB table `criteria_data` reloaded.");

        return true;
    }


    private static boolean handleReloadDisablesCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading disables table...");
        global.getDisableMgr().loadDisables();
        Log.outInfo(LogFilter.Server, "Checking quest disables...");
        global.getDisableMgr().checkQuestDisables();
        handler.sendGlobalGMSysMessage("DB table `disables` reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesDisenchantCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`disenchant_loot_template`)");
        LootManager.loadLootTemplates_Disenchant();
        LootStorage.DISENCHANT.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `disenchant_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadEventScriptsCommand(CommandHandler handler, StringArguments args) {
        if (global.getMapMgr().IsScriptScheduled()) {
            handler.sendSysMessage("DB scripts used currently, please attempt reload later.");

            return false;
        }

        if (args != null) {
            Log.outInfo(LogFilter.Server, "Re-Loading Scripts from `event_scripts`...");
        }

        global.getObjectMgr().loadEventScripts();

        if (args != null) {
            handler.sendGlobalGMSysMessage("DB table `event_scripts` reloaded.");
        }

        return true;
    }


    private static boolean handleReloadLootTemplatesFishingCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`fishing_loot_template`)");
        LootManager.loadLootTemplates_Fishing();
        LootStorage.FISHING.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `fishing_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadGameGraveyardZoneCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Graveyard-zone links...");

        global.getObjectMgr().loadGraveyardZones();

        handler.sendGlobalGMSysMessage("DB table `game_graveyard_zone` reloaded.");

        return true;
    }


    private static boolean handleReloadGameTeleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Game Tele coordinates...");

        global.getObjectMgr().loadGameTele();

        handler.sendGlobalGMSysMessage("DB table `game_tele` reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesGameobjectCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`gameobject_loot_template`)");
        LootManager.loadLootTemplates_Gameobject();
        LootStorage.GAMEOBJECT.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `gameobject_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadGOQuestEnderCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Loading Quests Relations... (`gameobject_questender`)");
        global.getObjectMgr().loadGameobjectQuestEnders();
        handler.sendGlobalGMSysMessage("DB table `gameobject_questender` reloaded.");

        return true;
    }


    private static boolean handleReloadGOQuestStarterCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Loading Quests Relations... (`gameobject_queststarter`)");
        global.getObjectMgr().loadGameobjectQuestStarters();
        handler.sendGlobalGMSysMessage("DB table `gameobject_queststarter` reloaded.");

        return true;
    }


    private static boolean handleReloadGossipMenuCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading `gossip_menu` Table!");
        global.getObjectMgr().loadGossipMenu();
        handler.sendGlobalGMSysMessage("DB table `gossip_menu` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadGossipMenuOptionCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading `gossip_menu_option` Table!");
        global.getObjectMgr().loadGossipMenuItems();
        handler.sendGlobalGMSysMessage("DB table `gossip_menu_option` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadItemRandomBonusListTemplatesCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Random item bonus list definitions...");
        ItemEnchantmentManager.loadItemRandomBonusListTemplates();
        handler.sendGlobalGMSysMessage("DB table `item_random_bonus_list_template` reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesItemCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`item_loot_template`)");
        LootManager.loadLootTemplates_Item();
        LootStorage.items.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `item_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadLfgRewardsCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading lfg dungeon rewards...");
        global.getLFGMgr().loadRewards();
        handler.sendGlobalGMSysMessage("DB table `lfg_dungeon_rewards` reloaded.");

        return true;
    }


    private static boolean handleReloadAchievementRewardLocaleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Achievement Reward Data locale...");
        global.getAchievementMgr().loadRewardLocales();
        handler.sendGlobalGMSysMessage("DB table `achievement_reward_locale` reloaded.");

        return true;
    }


    private static boolean handleReloadCreatureTemplateLocaleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Creature Template locale...");
        global.getObjectMgr().loadCreatureLocales();
        handler.sendGlobalGMSysMessage("DB table `Creature Template Locale` reloaded.");

        return true;
    }


    private static boolean handleReloadCreatureTextLocaleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Creature Texts locale...");
        global.getCreatureTextMgr().loadCreatureTextLocales();
        handler.sendGlobalGMSysMessage("DB table `creature_text_locale` reloaded.");

        return true;
    }


    private static boolean handleReloadGameobjectTemplateLocaleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Gameobject Template locale... ");
        global.getObjectMgr().loadGameObjectLocales();
        handler.sendGlobalGMSysMessage("DB table `gameobject_template_locale` reloaded.");

        return true;
    }


    private static boolean handleReloadGossipMenuOptionLocaleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Gossip Menu Option locale... ");
        global.getObjectMgr().loadGossipMenuItemsLocales();
        handler.sendGlobalGMSysMessage("DB table `gossip_menu_option_locale` reloaded.");

        return true;
    }


    private static boolean handleReloadPageTextLocaleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Page Text locale... ");
        global.getObjectMgr().loadPageTextLocales();
        handler.sendGlobalGMSysMessage("DB table `page_text_locale` reloaded.");

        return true;
    }


    private static boolean handleReloadPointsOfInterestLocaleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Points Of Interest locale... ");
        global.getObjectMgr().loadPointOfInterestLocales();
        handler.sendGlobalGMSysMessage("DB table `points_of_interest_locale` reloaded.");

        return true;
    }


    private static boolean handleReloadMailLevelRewardCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Player level dependent mail rewards...");
        global.getObjectMgr().loadMailLevelRewards();
        handler.sendGlobalGMSysMessage("DB table `mail_level_reward` reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesMailCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`mail_loot_template`)");
        LootManager.loadLootTemplates_Mail();
        LootStorage.MAIL.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `mail_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadLootTemplatesMillingCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`milling_loot_template`)");
        LootManager.loadLootTemplates_Milling();
        LootStorage.MILLING.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `milling_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }

    private static boolean handleReloadSpellClickSpellsCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading `npc_spellclick_spells` Table!");
        global.getObjectMgr().loadNPCSpellClickSpells();
        handler.sendGlobalGMSysMessage("DB table `npc_spellclick_spells` reloaded.");

        return true;
    }


    private static boolean handleReloadNpcVendorCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading `npc_vendor` Table!");
        global.getObjectMgr().loadVendors();
        handler.sendGlobalGMSysMessage("DB table `npc_vendor` reloaded.");

        return true;
    }


    private static boolean handleReloadPageTextsCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Page text...");
        global.getObjectMgr().loadPageTexts();
        handler.sendGlobalGMSysMessage("DB table `page_text` reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesPickpocketingCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`pickpocketing_loot_template`)");
        LootManager.loadLootTemplates_Pickpocketing();
        LootStorage.PICKPOCKETING.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `pickpocketing_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadPointsOfInterestCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading `points_of_interest` Table!");
        global.getObjectMgr().loadPointsOfInterest();
        handler.sendGlobalGMSysMessage("DB table `points_of_interest` reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesProspectingCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`prospecting_loot_template`)");
        LootManager.loadLootTemplates_Prospecting();
        LootStorage.PROSPECTING.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `prospecting_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadQuestGreetingCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Quest greeting ... ");
        global.getObjectMgr().loadQuestGreetings();
        handler.sendGlobalGMSysMessage("DB table `quest_greeting` reloaded.");

        return true;
    }


    private static boolean handleReloadQuestTemplateLocaleCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Quest locale... ");
        global.getObjectMgr().loadQuestTemplateLocale();
        global.getObjectMgr().loadQuestObjectivesLocale();
        global.getObjectMgr().loadQuestGreetingLocales();
        global.getObjectMgr().loadQuestOfferRewardLocale();
        global.getObjectMgr().loadQuestRequestItemsLocale();
        handler.sendGlobalGMSysMessage("DB table `quest_template_locale` reloaded.");
        handler.sendGlobalGMSysMessage("DB table `quest_objectives_locale` reloaded.");
        handler.sendGlobalGMSysMessage("DB table `quest_greeting_locale` reloaded.");
        handler.sendGlobalGMSysMessage("DB table `quest_offer_reward_locale` reloaded.");
        handler.sendGlobalGMSysMessage("DB table `quest_request_items_locale` reloaded.");

        return true;
    }


    private static boolean handleReloadQuestPOICommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Quest POI ...");
        global.getObjectMgr().loadQuestPOI();
        global.getObjectMgr().initializeQueriesData(QueryDataGroup.POIs);
        handler.sendGlobalGMSysMessage("DB Table `quest_poi` and `quest_poi_points` reloaded.");

        return true;
    }


    private static boolean handleReloadQuestTemplateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Quest templates...");
        global.getObjectMgr().loadQuests();
        global.getObjectMgr().initializeQueriesData(QueryDataGroup.Quests);
        handler.sendGlobalGMSysMessage("DB table `quest_template` (quest definitions) reloaded.");

        // dependent also from `gameobject` but this table not reloaded anyway
        Log.outInfo(LogFilter.Server, "Re-Loading GameObjects for quests...");
        global.getObjectMgr().loadGameObjectForQuests();
        handler.sendGlobalGMSysMessage("Data GameObjects for quests reloaded.");

        return true;
    }


    private static boolean handleReloadRBACCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Reloading RBAC tables...");
        global.getAccountMgr().loadRBAC();
        global.getWorldMgr().reloadRBAC();
        handler.sendGlobalGMSysMessage("RBAC data reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesReferenceCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`reference_loot_template`)");
        LootManager.loadLootTemplates_Reference();
        handler.sendGlobalGMSysMessage("DB table `reference_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadReputationRewardRateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading `reputation_reward_rate` Table!");
        global.getObjectMgr().loadReputationRewardRate();
        handler.sendGlobalSysMessage("DB table `reputation_reward_rate` reloaded.");

        return true;
    }


    private static boolean handleReloadReputationSpilloverTemplateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading `reputation_spillover_template` Table!");
        global.getObjectMgr().loadReputationSpilloverTemplate();
        handler.sendGlobalSysMessage("DB table `reputation_spillover_template` reloaded.");

        return true;
    }


    private static boolean handleReloadReservedNameCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Loading ReservedNames... (`reserved_name`)");
        global.getObjectMgr().loadReservedPlayersNames();
        handler.sendGlobalGMSysMessage("DB table `reserved_name` (player reserved names) reloaded.");

        return true;
    }


    private static boolean handleReloadSceneTemplateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.misc, "Reloading scene_template table...");
        global.getObjectMgr().loadSceneTemplates();
        handler.sendGlobalGMSysMessage("Scenes templates reloaded. New scriptname need a reboot.");

        return true;
    }


    private static boolean handleReloadSkillDiscoveryTemplateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Skill Discovery Table...");
        SkillDiscovery.loadSkillDiscoveryTable();
        handler.sendGlobalGMSysMessage("DB table `skill_discovery_template` (recipes discovered at crafting) reloaded.");

        return true;
    }

    private static boolean handleReloadSkillPerfectItemTemplateCommand(CommandHandler handler) {
        // latched onto HandleReloadSkillExtraItemTemplateCommand as it's part of that table group (and i don't want to chance all the command IDs)
        Log.outInfo(LogFilter.misc, "Re-Loading Skill Perfection Data Table...");
        SkillPerfectItems.loadSkillPerfectItemTable();
        handler.sendGlobalGMSysMessage("DB table `skill_perfect_item_template` (perfect item procs when crafting) reloaded.");

        return true;
    }


    private static boolean handleReloadSkillExtraItemTemplateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Skill Extra Item Table...");
        SkillExtraItems.loadSkillExtraItemTable();
        handler.sendGlobalGMSysMessage("DB table `skill_extra_item_template` (extra item creation when crafting) reloaded.");

        return handleReloadSkillPerfectItemTemplateCommand(handler);
    }


    private static boolean handleReloadSkillFishingBaseLevelCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Skill Fishing base level requirements...");
        global.getObjectMgr().loadFishingBaseSkillLevel();
        handler.sendGlobalGMSysMessage("DB table `skill_fishing_base_level` (fishing base level for zone/subzone) reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesSkinningCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`skinning_loot_template`)");
        LootManager.loadLootTemplates_Skinning();
        LootStorage.SKINNING.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `skinning_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadSmartScripts(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Smart Scripts...");
        global.getSmartAIMgr().loadFromDB();
        handler.sendGlobalGMSysMessage("Smart Scripts reloaded.");

        return true;
    }


    private static boolean handleReloadSpellAreaCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading SpellArea data...");
        global.getSpellMgr().loadSpellAreas();
        handler.sendGlobalGMSysMessage("DB table `spell_area` (spell dependences from area/quest/auras state) reloaded.");

        return true;
    }


    private static boolean handleReloadSpellGroupsCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Spell groups...");
        global.getSpellMgr().loadSpellGroups();
        handler.sendGlobalGMSysMessage("DB table `spell_group` (spell groups) reloaded.");

        return true;
    }


    private static boolean handleReloadSpellGroupStackRulesCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Spell Group Stack Rules...");
        global.getSpellMgr().loadSpellGroupStackRules();
        handler.sendGlobalGMSysMessage("DB table `spell_group_stack_rules` (spell stacking definitions) reloaded.");

        return true;
    }


    private static boolean handleReloadSpellLearnSpellCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Spell Learn spells...");
        global.getSpellMgr().loadSpellLearnSpells();
        handler.sendGlobalGMSysMessage("DB table `spell_learn_spell` reloaded.");

        return true;
    }


    private static boolean handleReloadSpellLinkedSpellCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Spell Linked spells...");
        global.getSpellMgr().loadSpellLinked();
        handler.sendGlobalGMSysMessage("DB table `spell_linked_spell` reloaded.");

        return true;
    }


    private static boolean handleReloadLootTemplatesSpellCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables... (`spell_loot_template`)");
        LootManager.loadLootTemplates_Spell();
        LootStorage.spell.checkLootRefs();
        handler.sendGlobalGMSysMessage("DB table `spell_loot_template` reloaded.");
        global.getConditionMgr().loadConditions(true);

        return true;
    }


    private static boolean handleReloadSpellPetAurasCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Spell pet auras...");
        global.getSpellMgr().loadSpellPetAuras();
        handler.sendGlobalGMSysMessage("DB table `spell_pet_auras` reloaded.");

        return true;
    }


    private static boolean handleReloadSpellProcsCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Spell Proc conditions and data...");
        global.getSpellMgr().loadSpellProcs();
        handler.sendGlobalGMSysMessage("DB table `spell_proc` (spell proc conditions and data) reloaded.");

        return true;
    }


    private static boolean handleReloadSpellRequiredCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Spell Required data... ");
        global.getSpellMgr().loadSpellRequired();
        handler.sendGlobalGMSysMessage("DB table `spell_required` reloaded.");

        return true;
    }


    private static boolean handleReloadSpellScriptsCommand(CommandHandler handler, StringArguments args) {
        if (global.getMapMgr().IsScriptScheduled()) {
            handler.sendSysMessage("DB scripts used currently, please attempt reload later.");

            return false;
        }

        if (args != null) {
            Log.outInfo(LogFilter.Server, "Re-Loading Scripts from `spell_scripts`...");
        }

        global.getObjectMgr().loadSpellScripts();

        if (args != null) {
            handler.sendGlobalGMSysMessage("DB table `spell_scripts` reloaded.");
        }

        return true;
    }


    private static boolean handleReloadSpellScriptNamesCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.misc, "Reloading spell_script_names table...");
        global.getObjectMgr().loadSpellScriptNames();
        //Global.ScriptMgr.NotifyScriptIDUpdate();
        global.getObjectMgr().validateSpellScripts();
        handler.sendGlobalGMSysMessage("Spell scripts reloaded.");

        return true;
    }


    private static boolean handleReloadSpellTargetPositionCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Spell target coordinates...");
        global.getSpellMgr().loadSpellTargetPositions();
        handler.sendGlobalGMSysMessage("DB table `spell_target_position` (destination coordinates for spell targets) reloaded.");

        return true;
    }


    private static boolean handleReloadSpellThreatsCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Aggro Spells Definitions...");
        global.getSpellMgr().loadSpellThreats();
        handler.sendGlobalGMSysMessage("DB table `spell_threat` (spell aggro definitions) reloaded.");

        return true;
    }


    private static boolean handleReloadSupportSystemCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Re-Loading Support System Tables...");
        global.getSupportMgr().loadBugTickets();
        global.getSupportMgr().loadComplaintTickets();
        global.getSupportMgr().loadSuggestionTickets();
        handler.sendGlobalGMSysMessage("DB tables `gm_*` reloaded.");

        return true;
    }


    private static boolean handleReloadTrainerCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.misc, "Re-Loading `trainer` Table!");
        global.getObjectMgr().loadTrainers();
        global.getObjectMgr().loadCreatureTrainers();
        handler.sendGlobalGMSysMessage("DB table `trainer` reloaded.");
        handler.sendGlobalGMSysMessage("DB table `trainer_locale` reloaded.");
        handler.sendGlobalGMSysMessage("DB table `trainer_spell` reloaded.");
        handler.sendGlobalGMSysMessage("DB table `creature_trainer` reloaded.");

        return true;
    }


    private static boolean handleReloadVehicleAccessoryCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Reloading vehicle_accessory table...");
        global.getObjectMgr().loadVehicleAccessories();
        handler.sendGlobalGMSysMessage("Vehicle accessories reloaded.");

        return true;
    }


    private static boolean handleReloadVehicleTemplateCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Reloading vehicle_template table...");
        global.getObjectMgr().loadVehicleTemplate();
        handler.sendGlobalGMSysMessage("Vehicle templates reloaded.");

        return true;
    }


    private static boolean handleReloadVehicleTemplateAccessoryCommand(CommandHandler handler) {
        Log.outInfo(LogFilter.Server, "Reloading vehicle_template_accessory table...");
        global.getObjectMgr().loadVehicleTemplateAccessories();
        handler.sendGlobalGMSysMessage("Vehicle template accessories reloaded.");

        return true;
    }


    private static boolean handleReloadWpCommand(CommandHandler handler, StringArguments args) {
        if (args != null) {
            Log.outInfo(LogFilter.Server, "Re-Loading Waypoints data from 'waypoints_data'");
        }

        global.getWaypointMgr().load();

        if (args != null) {
            handler.sendGlobalGMSysMessage("DB Table 'waypoint_data' reloaded.");
        }

        return true;
    }


    private static boolean handleReloadWpScriptsCommand(CommandHandler handler, StringArguments args) {
        if (global.getMapMgr().IsScriptScheduled()) {
            handler.sendSysMessage("DB scripts used currently, please attempt reload later.");

            return false;
        }

        if (args != null) {
            Log.outInfo(LogFilter.Server, "Re-Loading Scripts from `waypoint_scripts`...");
        }

        global.getObjectMgr().loadWaypointScripts();

        if (args != null) {
            handler.sendGlobalGMSysMessage("DB table `waypoint_scripts` reloaded.");
        }

        return true;
    }


    private static class AllCommand {

        private static boolean handleReloadAllCommand(CommandHandler handler) {
            handleReloadSkillFishingBaseLevelCommand(handler);

            handleReloadAllAchievementCommand(handler);
            handleReloadAllAreaCommand(handler);
            handleReloadAllLootCommand(handler);
            handleReloadAllNpcCommand(handler);
            handleReloadAllQuestCommand(handler);
            handleReloadAllSpellCommand(handler);
            handleReloadAllItemCommand(handler);
            handleReloadAllGossipsCommand(handler);
            handleReloadAllLocalesCommand(handler);

            handleReloadAccessRequirementCommand(handler);
            handleReloadMailLevelRewardCommand(handler);
            handleReloadReservedNameCommand(handler);
            handleReloadCypherStringCommand(handler);
            handleReloadGameTeleCommand(handler);

            handleReloadCreatureMovementOverrideCommand(handler);
            handleReloadCreatureSummonGroupsCommand(handler);

            handleReloadVehicleAccessoryCommand(handler);
            handleReloadVehicleTemplateAccessoryCommand(handler);

            handleReloadAutobroadcastCommand(handler);
            handleReloadBattlegroundTemplate(handler);
            handleReloadCharacterTemplate(handler);

            return true;
        }


        private static boolean handleReloadAllAchievementCommand(CommandHandler handler) {
            handleReloadCriteriaDataCommand(handler);
            handleReloadAchievementRewardCommand(handler);

            return true;
        }


        private static boolean handleReloadAllAreaCommand(CommandHandler handler) {
            handleReloadAreaTriggerTeleportCommand(handler);
            handleReloadAreaTriggerTavernCommand(handler);
            handleReloadGameGraveyardZoneCommand(handler);

            return true;
        }


        private static boolean handleReloadAllGossipsCommand(CommandHandler handler) {
            handleReloadGossipMenuCommand(handler);
            handleReloadGossipMenuOptionCommand(handler);
            handleReloadPointsOfInterestCommand(handler);

            return true;
        }


        private static boolean handleReloadAllItemCommand(CommandHandler handler) {
            handleReloadPageTextsCommand(handler);
            handleReloadItemRandomBonusListTemplatesCommand(handler);

            return true;
        }


        private static boolean handleReloadAllLocalesCommand(CommandHandler handler) {
            handleReloadAchievementRewardLocaleCommand(handler);
            handleReloadCreatureTemplateLocaleCommand(handler);
            handleReloadCreatureTextLocaleCommand(handler);
            handleReloadGameobjectTemplateLocaleCommand(handler);
            handleReloadGossipMenuOptionLocaleCommand(handler);
            handleReloadPageTextLocaleCommand(handler);
            handleReloadPointsOfInterestCommand(handler);
            handleReloadQuestTemplateLocaleCommand(handler);

            return true;
        }


        private static boolean handleReloadAllLootCommand(CommandHandler handler) {
            Log.outInfo(LogFilter.Server, "Re-Loading Loot Tables...");
            LootManager.loadLootTables();
            handler.sendGlobalGMSysMessage("DB tables `*_loot_template` reloaded.");
            global.getConditionMgr().loadConditions(true);

            return true;
        }


        private static boolean handleReloadAllNpcCommand(CommandHandler handler) {
            handleReloadTrainerCommand(handler);
            handleReloadNpcVendorCommand(handler);
            handleReloadPointsOfInterestCommand(handler);
            handleReloadSpellClickSpellsCommand(handler);

            return true;
        }


        private static boolean handleReloadAllQuestCommand(CommandHandler handler) {
            handleReloadQuestAreaTriggersCommand(handler);
            handleReloadQuestGreetingCommand(handler);
            handleReloadQuestPOICommand(handler);
            handleReloadQuestTemplateCommand(handler);

            Log.outInfo(LogFilter.Server, "Re-Loading Quests Relations...");
            global.getObjectMgr().loadQuestStartersAndEnders();
            handler.sendGlobalGMSysMessage("DB tables `*_queststarter` and `*_questender` reloaded.");

            return true;
        }


        private static boolean handleReloadAllScriptsCommand(CommandHandler handler) {
            if (global.getMapMgr().IsScriptScheduled()) {
                handler.sendSysMessage("DB scripts used currently, please attempt reload later.");

                return false;
            }

            Log.outInfo(LogFilter.Server, "Re-Loading Scripts...");
            handleReloadEventScriptsCommand(handler, null);
            handleReloadSpellScriptsCommand(handler, null);
            handler.sendGlobalGMSysMessage("DB tables `*_scripts` reloaded.");
            handleReloadWpScriptsCommand(handler, null);
            handleReloadWpCommand(handler, null);

            return true;
        }


        private static boolean handleReloadAllSpellCommand(CommandHandler handler) {
            handleReloadSkillDiscoveryTemplateCommand(handler);
            handleReloadSkillExtraItemTemplateCommand(handler);
            handleReloadSpellRequiredCommand(handler);
            handleReloadSpellAreaCommand(handler);
            handleReloadSpellGroupsCommand(handler);
            handleReloadSpellLearnSpellCommand(handler);
            handleReloadSpellLinkedSpellCommand(handler);
            handleReloadSpellProcsCommand(handler);
            handleReloadSpellTargetPositionCommand(handler);
            handleReloadSpellThreatsCommand(handler);
            handleReloadSpellGroupStackRulesCommand(handler);
            handleReloadSpellPetAurasCommand(handler);

            return true;
        }
    }
}
