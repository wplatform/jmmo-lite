package com.github.azeroth.game.server;


import com.github.azeroth.game.listener.interfaces.iworld.IWorldOnConfigLoad;

import java.util.HashMap;

public class WorldConfig extends ConfigMgr {
    private static final HashMap<WorldCfg, object> VALUES = new HashMap<WorldCfg, object>();


    public static void load() {
        load(false);
    }

    public static void load(boolean reload) {
        if (reload) {
            load("WorldServer.conf");
        }

        // Read support system setting from the config file
        VALUES.put(WorldCfg.SupportEnabled, GetDefaultValue("Support.Enabled", true));
        VALUES.put(WorldCfg.SupportTicketsEnabled, GetDefaultValue("Support.TicketsEnabled", false));
        VALUES.put(WorldCfg.SupportBugsEnabled, GetDefaultValue("Support.BugsEnabled", false));
        VALUES.put(WorldCfg.SupportComplaintsEnabled, GetDefaultValue("Support.ComplaintsEnabled", false));
        VALUES.put(WorldCfg.SupportSuggestionsEnabled, GetDefaultValue("Support.SuggestionsEnabled", false));

        // Send server info on login?
        VALUES.put(WorldCfg.EnableSinfoLogin, GetDefaultValue("Server.LoginInfo", 0));

        // Read all rates from the config file

//		static void SetRegenRate(WorldCfg rate, string configKey)
//			{
//				Values[rate] = GetDefaultValue(configKey, 1.0f);
//
//				if ((float)Values[rate] < 0.0f)
//				{
//					Log.outError(LogFilter.ServerLoading, "{0} ({1}) must be > 0. Using 1 instead.", configKey, Values[rate]);
//					Values[rate] = 1;
//				}
//			}

        SetRegenRate(WorldCfg.RateHealth, "Rate.Health");
        SetRegenRate(WorldCfg.RatePowerMana, "Rate.Mana");
        SetRegenRate(WorldCfg.RatePowerRageIncome, "Rate.Rage.Gain");
        SetRegenRate(WorldCfg.RatePowerRageLoss, "Rate.Rage.Loss");
        SetRegenRate(WorldCfg.RatePowerFocus, "Rate.Focus");
        SetRegenRate(WorldCfg.RatePowerEnergy, "Rate.Energy");
        SetRegenRate(WorldCfg.RatePowerComboPointsLoss, "Rate.ComboPoints.Loss");
        SetRegenRate(WorldCfg.RatePowerRunicPowerIncome, "Rate.RunicPower.Gain");
        SetRegenRate(WorldCfg.RatePowerRunicPowerLoss, "Rate.RunicPower.Loss");
        SetRegenRate(WorldCfg.RatePowerSoulShards, "Rate.SoulShards.Loss");
        SetRegenRate(WorldCfg.RatePowerLunarPower, "Rate.LunarPower.Loss");
        SetRegenRate(WorldCfg.RatePowerHolyPower, "Rate.HolyPower.Loss");
        SetRegenRate(WorldCfg.RatePowerMaelstrom, "Rate.Maelstrom.Loss");
        SetRegenRate(WorldCfg.RatePowerChi, "Rate.Chi.Loss");
        SetRegenRate(WorldCfg.RatePowerInsanity, "Rate.Insanity.Loss");
        SetRegenRate(WorldCfg.RatePowerArcaneCharges, "Rate.ArcaneCharges.Loss");
        SetRegenRate(WorldCfg.RatePowerFury, "Rate.Fury.Loss");
        SetRegenRate(WorldCfg.RatePowerPain, "Rate.Pain.Loss");

        VALUES.put(WorldCfg.RateSkillDiscovery, GetDefaultValue("Rate.skill.Discovery", 1.0f));
        VALUES.put(WorldCfg.RateDropItemPoor, GetDefaultValue("Rate.Drop.item.Poor", 1.0f));
        VALUES.put(WorldCfg.RateDropItemNormal, GetDefaultValue("Rate.Drop.item.Normal", 1.0f));
        VALUES.put(WorldCfg.RateDropItemUncommon, GetDefaultValue("Rate.Drop.item.Uncommon", 1.0f));
        VALUES.put(WorldCfg.RateDropItemRare, GetDefaultValue("Rate.Drop.item.Rare", 1.0f));
        VALUES.put(WorldCfg.RateDropItemEpic, GetDefaultValue("Rate.Drop.item.Epic", 1.0f));
        VALUES.put(WorldCfg.RateDropItemLegendary, GetDefaultValue("Rate.Drop.item.Legendary", 1.0f));
        VALUES.put(WorldCfg.RateDropItemArtifact, GetDefaultValue("Rate.Drop.item.Artifact", 1.0f));
        VALUES.put(WorldCfg.RateDropItemReferenced, GetDefaultValue("Rate.Drop.item.Referenced", 1.0f));
        VALUES.put(WorldCfg.RateDropItemReferencedAmount, GetDefaultValue("Rate.Drop.item.ReferencedAmount", 1.0f));
        VALUES.put(WorldCfg.RateDropMoney, GetDefaultValue("Rate.Drop.Money", 1.0f));
        VALUES.put(WorldCfg.RateXpKill, GetDefaultValue("Rate.XP.Kill", 1.0f));
        VALUES.put(WorldCfg.RateXpBgKill, GetDefaultValue("Rate.XP.BattlegroundKill", 1.0f));
        VALUES.put(WorldCfg.RateXpQuest, GetDefaultValue("Rate.XP.Quest", 1.0f));
        VALUES.put(WorldCfg.RateXpExplore, GetDefaultValue("Rate.XP.Explore", 1.0f));

        VALUES.put(WorldCfg.XpBoostDaymask, GetDefaultValue("XP.Boost.Daymask", 0));
        VALUES.put(WorldCfg.RateXpBoost, GetDefaultValue("XP.Boost.Rate", 2.0f));

        VALUES.put(WorldCfg.RateRepaircost, GetDefaultValue("Rate.RepairCost", 1.0f));

        if ((Float) VALUES.get(WorldCfg.RateRepaircost) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "Rate.RepairCost ({0}) must be >=0. Using 0.0 instead.", VALUES.get(WorldCfg.RateRepaircost));
            VALUES.put(WorldCfg.RateRepaircost, 0.0f);
        }

        VALUES.put(WorldCfg.RateReputationGain, GetDefaultValue("Rate.Reputation.Gain", 1.0f));
        VALUES.put(WorldCfg.RateReputationLowLevelKill, GetDefaultValue("Rate.Reputation.LowLevel.Kill", 1.0f));
        VALUES.put(WorldCfg.RateReputationLowLevelQuest, GetDefaultValue("Rate.Reputation.LowLevel.Quest", 1.0f));
        VALUES.put(WorldCfg.RateReputationRecruitAFriendBonus, GetDefaultValue("Rate.Reputation.RecruitAFriendBonus", 0.1f));
        VALUES.put(WorldCfg.RateCreatureNormalDamage, GetDefaultValue("Rate.CREATURE.NORMAL.Damage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteEliteDamage, GetDefaultValue("Rate.CREATURE.Elite.Elite.Damage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteRareeliteDamage, GetDefaultValue("Rate.CREATURE.Elite.RAREELITE.Damage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteWorldbossDamage, GetDefaultValue("Rate.CREATURE.Elite.WORLDBOSS.Damage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteRareDamage, GetDefaultValue("Rate.CREATURE.Elite.RARE.Damage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureNormalHp, GetDefaultValue("Rate.CREATURE.NORMAL.HP", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteEliteHp, GetDefaultValue("Rate.CREATURE.Elite.Elite.HP", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteRareeliteHp, GetDefaultValue("Rate.CREATURE.Elite.RAREELITE.HP", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteWorldbossHp, GetDefaultValue("Rate.CREATURE.Elite.WORLDBOSS.HP", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteRareHp, GetDefaultValue("Rate.CREATURE.Elite.RARE.HP", 1.0f));
        VALUES.put(WorldCfg.RateCreatureNormalSpelldamage, GetDefaultValue("Rate.CREATURE.NORMAL.SpellDamage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteEliteSpelldamage, GetDefaultValue("Rate.CREATURE.Elite.Elite.SpellDamage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteRareeliteSpelldamage, GetDefaultValue("Rate.CREATURE.Elite.RAREELITE.SpellDamage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteWorldbossSpelldamage, GetDefaultValue("Rate.CREATURE.Elite.WORLDBOSS.SpellDamage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureEliteRareSpelldamage, GetDefaultValue("Rate.CREATURE.Elite.RARE.SpellDamage", 1.0f));
        VALUES.put(WorldCfg.RateCreatureAggro, GetDefaultValue("Rate.CREATURE.Aggro", 1.0f));
        VALUES.put(WorldCfg.RateRestIngame, GetDefaultValue("Rate.Rest.InGame", 1.0f));
        VALUES.put(WorldCfg.RateRestOfflineInTavernOrCity, GetDefaultValue("Rate.Rest.Offline.InTavernOrCity", 1.0f));
        VALUES.put(WorldCfg.RateRestOfflineInWilderness, GetDefaultValue("Rate.Rest.Offline.InWilderness", 1.0f));
        VALUES.put(WorldCfg.RateDamageFall, GetDefaultValue("Rate.damage.Fall", 1.0f));
        VALUES.put(WorldCfg.RateAuctionTime, GetDefaultValue("Rate.Auction.Time", 1.0f));
        VALUES.put(WorldCfg.RateAuctionDeposit, GetDefaultValue("Rate.Auction.Deposit", 1.0f));
        VALUES.put(WorldCfg.RateAuctionCut, GetDefaultValue("Rate.Auction.Cut", 1.0f));
        VALUES.put(WorldCfg.RateHonor, GetDefaultValue("Rate.Honor", 1.0f));
        VALUES.put(WorldCfg.RateInstanceResetTime, GetDefaultValue("Rate.InstanceResetTime", 1.0f));
        VALUES.put(WorldCfg.RateTalent, GetDefaultValue("Rate.Talent", 1.0f));

        if ((Float) VALUES.get(WorldCfg.RateTalent) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "Rate.Talent ({0}) must be > 0. Using 1 instead.", VALUES.get(WorldCfg.RateTalent));
            VALUES.put(WorldCfg.RateTalent, 1.0f);
        }

        VALUES.put(WorldCfg.RateMovespeed, GetDefaultValue("Rate.MoveSpeed", 1.0f));

        if ((Float) VALUES.get(WorldCfg.RateMovespeed) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "Rate.MoveSpeed ({0}) must be > 0. Using 1 instead.", VALUES.get(WorldCfg.RateMovespeed));
            VALUES.put(WorldCfg.RateMovespeed, 1.0f);
        }

        VALUES.put(WorldCfg.RateCorpseDecayLooted, GetDefaultValue("Rate.Corpse.Decay.Looted", 0.5f));

        VALUES.put(WorldCfg.RateDurabilityLossOnDeath, GetDefaultValue("DurabilityLoss.OnDeath", 10.0f));

        if ((Float) VALUES.get(WorldCfg.RateDurabilityLossOnDeath) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "DurabilityLoss.OnDeath ({0}) must be >=0. Using 0.0 instead.", VALUES.get(WorldCfg.RateDurabilityLossOnDeath));
            VALUES.put(WorldCfg.RateDurabilityLossOnDeath, 0.0f);
        }

        if ((Float) VALUES.get(WorldCfg.RateDurabilityLossOnDeath) > 100.0f) {
            Log.outError(LogFilter.ServerLoading, "DurabilityLoss.OnDeath ({0}) must be <= 100. Using 100.0 instead.", VALUES.get(WorldCfg.RateDurabilityLossOnDeath));
            VALUES.put(WorldCfg.RateDurabilityLossOnDeath, 0.0f);
        }

        VALUES.put(WorldCfg.RateDurabilityLossOnDeath, (Float) VALUES.get(WorldCfg.RateDurabilityLossOnDeath) / 100.0f);

        VALUES.put(WorldCfg.RateDurabilityLossDamage, GetDefaultValue("DurabilityLossChance.Damage", 0.5f));

        if ((Float) VALUES.get(WorldCfg.RateDurabilityLossDamage) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "DurabilityLossChance.damage ({0}) must be >=0. Using 0.0 instead.", VALUES.get(WorldCfg.RateDurabilityLossDamage));
            VALUES.put(WorldCfg.RateDurabilityLossDamage, 0.0f);
        }

        VALUES.put(WorldCfg.RateDurabilityLossAbsorb, GetDefaultValue("DurabilityLossChance.Absorb", 0.5f));

        if ((Float) VALUES.get(WorldCfg.RateDurabilityLossAbsorb) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "DurabilityLossChance.absorb ({0}) must be >=0. Using 0.0 instead.", VALUES.get(WorldCfg.RateDurabilityLossAbsorb));
            VALUES.put(WorldCfg.RateDurabilityLossAbsorb, 0.0f);
        }

        VALUES.put(WorldCfg.RateDurabilityLossParry, GetDefaultValue("DurabilityLossChance.Parry", 0.05f));

        if ((Float) VALUES.get(WorldCfg.RateDurabilityLossParry) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "DurabilityLossChance.Parry ({0}) must be >=0. Using 0.0 instead.", VALUES.get(WorldCfg.RateDurabilityLossParry));
            VALUES.put(WorldCfg.RateDurabilityLossParry, 0.0f);
        }

        VALUES.put(WorldCfg.RateDurabilityLossBlock, GetDefaultValue("DurabilityLossChance.Block", 0.05f));

        if ((Float) VALUES.get(WorldCfg.RateDurabilityLossBlock) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "DurabilityLossChance.Block ({0}) must be >=0. Using 0.0 instead.", VALUES.get(WorldCfg.RateDurabilityLossBlock));
            VALUES.put(WorldCfg.RateDurabilityLossBlock, 0.0f);
        }

        VALUES.put(WorldCfg.RateMoneyQuest, GetDefaultValue("Rate.Quest.money.Reward", 1.0f));

        if ((Float) VALUES.get(WorldCfg.RateMoneyQuest) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "Rate.Quest.money.reward ({0}) must be >=0. Using 0 instead.", VALUES.get(WorldCfg.RateMoneyQuest));
            VALUES.put(WorldCfg.RateMoneyQuest, 0.0f);
        }

        VALUES.put(WorldCfg.RateMoneyMaxLevelQuest, GetDefaultValue("Rate.Quest.money.max.level.Reward", 1.0f));

        if ((Float) VALUES.get(WorldCfg.RateMoneyMaxLevelQuest) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, "Rate.Quest.money.max.level.reward ({0}) must be >=0. Using 0 instead.", VALUES.get(WorldCfg.RateMoneyMaxLevelQuest));
            VALUES.put(WorldCfg.RateMoneyMaxLevelQuest, 0.0f);
        }

        // Read other configuration items from the config file
        VALUES.put(WorldCfg.DurabilityLossInPvp, GetDefaultValue("DurabilityLoss.InPvP", false));

        VALUES.put(WorldCfg.Compression, GetDefaultValue("Compression", 1));

        if ((Integer) VALUES.get(WorldCfg.Compression) < 1 || (Integer) VALUES.get(WorldCfg.Compression) > 9) {
            Log.outError(LogFilter.ServerLoading, "Compression level ({0}) must be in range 1..9. Using default compression level (1).", VALUES.get(WorldCfg.Compression));
            VALUES.put(WorldCfg.Compression, 1);
        }

        VALUES.put(WorldCfg.AddonChannel, GetDefaultValue("AddonChannel", true));
        VALUES.put(WorldCfg.CleanCharacterDb, GetDefaultValue("CleanCharacterDB", false));
        VALUES.put(WorldCfg.PersistentCharacterCleanFlags, GetDefaultValue("PersistentCharacterCleanFlags", 0));
        VALUES.put(WorldCfg.AuctionReplicateDelay, GetDefaultValue("Auction.ReplicateItemsCooldown", 900));
        VALUES.put(WorldCfg.AuctionSearchDelay, GetDefaultValue("Auction.SearchDelay", 300));

        if ((Integer) VALUES.get(WorldCfg.AuctionSearchDelay) < 100 || (Integer) VALUES.get(WorldCfg.AuctionSearchDelay) > 10000) {
            Log.outError(LogFilter.ServerLoading, "Auction.SearchDelay ({0}) must be between 100 and 10000. Using default of 300ms", VALUES.get(WorldCfg.AuctionSearchDelay));
            VALUES.put(WorldCfg.AuctionSearchDelay, 300);
        }

        VALUES.put(WorldCfg.AuctionTaintedSearchDelay, GetDefaultValue("Auction.TaintedSearchDelay", 3000));

        if ((Integer) VALUES.get(WorldCfg.AuctionTaintedSearchDelay) < 100 || (Integer) VALUES.get(WorldCfg.AuctionTaintedSearchDelay) > 10000) {
            Log.outError(LogFilter.ServerLoading, String.format("Auction.TaintedSearchDelay (%1$s) must be between 100 and 10000. Using default of 3s", VALUES.get(WorldCfg.AuctionTaintedSearchDelay)));
            VALUES.put(WorldCfg.AuctionTaintedSearchDelay, 3000);
        }

        VALUES.put(WorldCfg.ChatChannelLevelReq, GetDefaultValue("ChatLevelReq.Channel", 1));
        VALUES.put(WorldCfg.ChatWhisperLevelReq, GetDefaultValue("ChatLevelReq.Whisper", 1));
        VALUES.put(WorldCfg.ChatEmoteLevelReq, GetDefaultValue("ChatLevelReq.Emote", 1));
        VALUES.put(WorldCfg.ChatSayLevelReq, GetDefaultValue("ChatLevelReq.Say", 1));
        VALUES.put(WorldCfg.ChatYellLevelReq, GetDefaultValue("ChatLevelReq.Yell", 1));
        VALUES.put(WorldCfg.PartyLevelReq, GetDefaultValue("PartyLevelReq", 1));
        VALUES.put(WorldCfg.TradeLevelReq, GetDefaultValue("LevelReq.Trade", 1));
        VALUES.put(WorldCfg.AuctionLevelReq, GetDefaultValue("LevelReq.Auction", 1));
        VALUES.put(WorldCfg.MailLevelReq, GetDefaultValue("LevelReq.Mail", 1));
        VALUES.put(WorldCfg.PreserveCustomChannels, GetDefaultValue("PreserveCustomChannels", false));
        VALUES.put(WorldCfg.PreserveCustomChannelDuration, GetDefaultValue("PreserveCustomChannelDuration", 14));
        VALUES.put(WorldCfg.PreserveCustomChannelInterval, GetDefaultValue("PreserveCustomChannelInterval", 5));
        VALUES.put(WorldCfg.GridUnload, GetDefaultValue("GridUnload", true));
        VALUES.put(WorldCfg.BasemapLoadGrids, GetDefaultValue("BaseMapLoadAllGrids", false));

        if ((Boolean) VALUES.get(WorldCfg.BasemapLoadGrids) && (Boolean) VALUES.get(WorldCfg.GridUnload)) {
            Log.outError(LogFilter.ServerLoading, "BaseMapLoadAllGrids enabled, but GridUnload also enabled. GridUnload must be disabled to enable base map pre-loading. Base map pre-loading disabled");
            VALUES.put(WorldCfg.BasemapLoadGrids, false);
        }

        VALUES.put(WorldCfg.InstancemapLoadGrids, GetDefaultValue("InstanceMapLoadAllGrids", false));

        if ((Boolean) VALUES.get(WorldCfg.InstancemapLoadGrids) && (Boolean) VALUES.get(WorldCfg.GridUnload)) {
            Log.outError(LogFilter.ServerLoading, "InstanceMapLoadAllGrids enabled, but GridUnload also enabled. GridUnload must be disabled to enable instance map pre-loading. Instance map pre-loading disabled");
            VALUES.put(WorldCfg.InstancemapLoadGrids, false);
        }

        VALUES.put(WorldCfg.IntervalSave, GetDefaultValue("PlayerSaveInterval", 15 * time.Minute * time.InMilliseconds));
        VALUES.put(WorldCfg.IntervalDisconnectTolerance, GetDefaultValue("DisconnectToleranceInterval", 0));
        VALUES.put(WorldCfg.StatsSaveOnlyOnLogout, GetDefaultValue("PlayerSave.stats.SaveOnlyOnLogout", true));

        VALUES.put(WorldCfg.MinLevelStatSave, GetDefaultValue("PlayerSave.stats.MinLevel", 0));

        if ((Integer) VALUES.get(WorldCfg.MinLevelStatSave) > SharedConst.maxLevel) {
            Log.outError(LogFilter.ServerLoading, "PlayerSave.stats.minLevel ({0}) must be in range 0..80. Using default, do not save character stats (0).", VALUES.get(WorldCfg.MinLevelStatSave));
            VALUES.put(WorldCfg.MinLevelStatSave, 0);
        }

        VALUES.put(WorldCfg.IntervalGridclean, GetDefaultValue("GridCleanUpDelay", 5 * time.Minute * time.InMilliseconds));

        if ((Integer) VALUES.get(WorldCfg.IntervalGridclean) < MapDefine.MinGridDelay) {
            Log.outError(LogFilter.ServerLoading, "GridCleanUpDelay ({0}) must be greater {1} Use this minimal value.", VALUES.get(WorldCfg.IntervalGridclean), MapDefine.MinGridDelay);
            VALUES.put(WorldCfg.IntervalGridclean, MapDefine.MinGridDelay);
        }

        VALUES.put(WorldCfg.IntervalMapupdate, GetDefaultValue("MapUpdateInterval", 10));

        if ((Integer) VALUES.get(WorldCfg.IntervalMapupdate) < MapDefine.MinMapUpdateDelay) {
            Log.outError(LogFilter.ServerLoading, "MapUpdateInterval ({0}) must be greater {1}. Use this minimal value.", VALUES.get(WorldCfg.IntervalMapupdate), MapDefine.MinMapUpdateDelay);
            VALUES.put(WorldCfg.IntervalMapupdate, MapDefine.MinMapUpdateDelay);
        }

        VALUES.put(WorldCfg.IntervalChangeweather, GetDefaultValue("ChangeWeatherInterval", 10 * time.Minute * time.InMilliseconds));

        if (reload) {
            var val = GetDefaultValue("WorldServerPort", 8085);

            if (val != (Integer) VALUES.get(WorldCfg.PortWorld)) {
                Log.outError(LogFilter.ServerLoading, "WorldServerPort option can't be changed at worldserver.conf reload, using current value ({0}).", VALUES.get(WorldCfg.PortWorld));
            }

            val = GetDefaultValue("InstanceServerPort", 8086);

            if (val != (Integer) VALUES.get(WorldCfg.PortInstance)) {
                Log.outError(LogFilter.ServerLoading, "InstanceServerPort option can't be changed at worldserver.conf reload, using current value ({0}).", VALUES.get(WorldCfg.PortInstance));
            }
        } else {
            VALUES.put(WorldCfg.PortWorld, GetDefaultValue("WorldServerPort", 8085));
            VALUES.put(WorldCfg.PortInstance, GetDefaultValue("InstanceServerPort", 8086));
        }

        // Config values are in "milliseconds" but we handle SocketTimeOut only as "seconds" so divide by 1000
        VALUES.put(WorldCfg.SocketTimeoutTime, GetDefaultValue("SocketTimeOutTime", 900000) / 1000);
        VALUES.put(WorldCfg.SocketTimeoutTimeActive, GetDefaultValue("SocketTimeOutTimeActive", 60000) / 1000);
        VALUES.put(WorldCfg.SessionAddDelay, GetDefaultValue("SessionAddDelay", 10000));

        VALUES.put(WorldCfg.GroupXpDistance, GetDefaultValue("MaxGroupXPDistance", 74.0f));
        VALUES.put(WorldCfg.MaxRecruitAFriendDistance, GetDefaultValue("MaxRecruitAFriendBonusDistance", 100.0f));
        VALUES.put(WorldCfg.MinQuestScaledXpRatio, GetDefaultValue("MinQuestScaledXPRatio", 0));

        if ((Integer) VALUES.get(WorldCfg.MinQuestScaledXpRatio) > 100) {
            Log.outError(LogFilter.ServerLoading, String.format("MinQuestScaledXPRatio (%1$s) must be in range 0..100. Set to 0.", VALUES.get(WorldCfg.MinQuestScaledXpRatio)));
            VALUES.put(WorldCfg.MinQuestScaledXpRatio, 0);
        }

        VALUES.put(WorldCfg.MinCreatureScaledXpRatio, GetDefaultValue("MinCreatureScaledXPRatio", 0));

        if ((Integer) VALUES.get(WorldCfg.MinCreatureScaledXpRatio) > 100) {
            Log.outError(LogFilter.ServerLoading, String.format("MinCreatureScaledXPRatio (%1$s) must be in range 0..100. Set to 0.", VALUES.get(WorldCfg.MinCreatureScaledXpRatio)));
            VALUES.put(WorldCfg.MinCreatureScaledXpRatio, 0);
        }

        VALUES.put(WorldCfg.MinDiscoveredScaledXpRatio, GetDefaultValue("MinDiscoveredScaledXPRatio", 0));

        if ((Integer) VALUES.get(WorldCfg.MinDiscoveredScaledXpRatio) > 100) {
            Log.outError(LogFilter.ServerLoading, String.format("MinDiscoveredScaledXPRatio (%1$s) must be in range 0..100. Set to 0.", VALUES.get(WorldCfg.MinDiscoveredScaledXpRatio)));
            VALUES.put(WorldCfg.MinDiscoveredScaledXpRatio, 0);
        }

        /** @todo Add MonsterSight (with meaning) in worldserver.conf or put them as define
         */
        VALUES.put(WorldCfg.SightMonster, GetDefaultValue("MonsterSight", 50.0f));

        VALUES.put(WorldCfg.RegenHpCannotReachTargetInRaid, GetDefaultValue("Creature.RegenHPCannotReachTargetInRaid", true));

        if (reload) {
            var val = GetDefaultValue("GameType", 0);

            if (val != (Integer) VALUES.get(WorldCfg.GameType)) {
                Log.outError(LogFilter.ServerLoading, "GameType option can't be changed at worldserver.conf reload, using current value ({0}).", VALUES.get(WorldCfg.GameType));
            }
        } else {
            VALUES.put(WorldCfg.GameType, GetDefaultValue("GameType", 0));
        }

        if (reload) {
            var val = (int) GetDefaultValue("RealmZone", RealmZones.Development);

            if (val != (Integer) VALUES.get(WorldCfg.RealmZone)) {
                Log.outError(LogFilter.ServerLoading, "RealmZone option can't be changed at worldserver.conf reload, using current value ({0}).", VALUES.get(WorldCfg.RealmZone));
            }
        } else {
            VALUES.put(WorldCfg.RealmZone, GetDefaultValue("RealmZone", RealmZones.Development.getValue()));
        }

        VALUES.put(WorldCfg.AllowTwoSideInteractionCalendar, GetDefaultValue("AllowTwoSide.Interaction.Calendar", false));
        VALUES.put(WorldCfg.AllowTwoSideInteractionChannel, GetDefaultValue("AllowTwoSide.Interaction.Channel", false));
        VALUES.put(WorldCfg.AllowTwoSideInteractionGroup, GetDefaultValue("AllowTwoSide.Interaction.Group", false));
        VALUES.put(WorldCfg.AllowTwoSideInteractionGuild, GetDefaultValue("AllowTwoSide.Interaction.Guild", false));
        VALUES.put(WorldCfg.AllowTwoSideInteractionAuction, GetDefaultValue("AllowTwoSide.Interaction.Auction", true));
        VALUES.put(WorldCfg.AllowTwoSideTrade, GetDefaultValue("AllowTwoSide.Trade", false));
        VALUES.put(WorldCfg.StrictPlayerNames, GetDefaultValue("StrictPlayerNames", 0));
        VALUES.put(WorldCfg.StrictCharterNames, GetDefaultValue("StrictCharterNames", 0));
        VALUES.put(WorldCfg.StrictPetNames, GetDefaultValue("StrictPetNames", 0));

        VALUES.put(WorldCfg.MinPlayerName, GetDefaultValue("MinPlayerName", 2));

        if ((Integer) VALUES.get(WorldCfg.MinPlayerName) < 1 || (Integer) VALUES.get(WorldCfg.MinPlayerName) > 12) {
            Log.outError(LogFilter.ServerLoading, "MinPlayerName ({0}) must be in range 1..{1}. Set to 2.", VALUES.get(WorldCfg.MinPlayerName), 12);
            VALUES.put(WorldCfg.MinPlayerName, 2);
        }

        VALUES.put(WorldCfg.MinCharterName, GetDefaultValue("MinCharterName", 2));

        if ((Integer) VALUES.get(WorldCfg.MinCharterName) < 1 || (Integer) VALUES.get(WorldCfg.MinCharterName) > 24) {
            Log.outError(LogFilter.ServerLoading, "MinCharterName ({0}) must be in range 1..{1}. Set to 2.", VALUES.get(WorldCfg.MinCharterName), 24);
            VALUES.put(WorldCfg.MinCharterName, 2);
        }

        VALUES.put(WorldCfg.MinPetName, GetDefaultValue("MinPetName", 2));

        if ((Integer) VALUES.get(WorldCfg.MinPetName) < 1 || (Integer) VALUES.get(WorldCfg.MinPetName) > 12) {
            Log.outError(LogFilter.ServerLoading, "MinPetName ({0}) must be in range 1..{1}. Set to 2.", VALUES.get(WorldCfg.MinPetName), 12);
            VALUES.put(WorldCfg.MinPetName, 2);
        }

        VALUES.put(WorldCfg.CharterCostGuild, GetDefaultValue("Guild.CharterCost", 1000));
        VALUES.put(WorldCfg.CharterCostArena2v2, GetDefaultValue("ArenaTeam.CharterCost.2v2", 800000));
        VALUES.put(WorldCfg.CharterCostArena3v3, GetDefaultValue("ArenaTeam.CharterCost.3v3", 1200000));
        VALUES.put(WorldCfg.CharterCostArena5v5, GetDefaultValue("ArenaTeam.CharterCost.5v5", 2000000));

        VALUES.put(WorldCfg.CharacterCreatingDisabled, GetDefaultValue("CharacterCreating.Disabled", 0));
        VALUES.put(WorldCfg.CharacterCreatingDisabledRacemask, GetDefaultValue("CharacterCreating.disabled.RaceMask", 0));
        VALUES.put(WorldCfg.CharacterCreatingDisabledClassmask, GetDefaultValue("CharacterCreating.disabled.ClassMask", 0));

        VALUES.put(WorldCfg.CharactersPerRealm, GetDefaultValue("CharactersPerRealm", 60));

        if ((Integer) VALUES.get(WorldCfg.CharactersPerRealm) < 1 || (Integer) VALUES.get(WorldCfg.CharactersPerRealm) > 200) {
            Log.outError(LogFilter.ServerLoading, "CharactersPerRealm ({0}) must be in range 1..200. Set to 200.", VALUES.get(WorldCfg.CharactersPerRealm));
            VALUES.put(WorldCfg.CharactersPerRealm, 200);
        }

        // must be after CharactersPerRealm
        VALUES.put(WorldCfg.CharactersPerAccount, GetDefaultValue("CharactersPerAccount", 60));

        if ((Integer) VALUES.get(WorldCfg.CharactersPerAccount) < (Integer) VALUES.get(WorldCfg.CharactersPerRealm)) {
            Log.outError(LogFilter.ServerLoading, "CharactersPerAccount ({0}) can't be less than CharactersPerRealm ({1}).", VALUES.get(WorldCfg.CharactersPerAccount), VALUES.get(WorldCfg.CharactersPerRealm));
            VALUES.put(WorldCfg.CharactersPerAccount, VALUES.get(WorldCfg.CharactersPerRealm));
        }

        VALUES.put(WorldCfg.CharacterCreatingEvokersPerRealm, GetDefaultValue("CharacterCreating.EvokersPerRealm", 1));

        if ((Integer) VALUES.get(WorldCfg.CharacterCreatingEvokersPerRealm) < 0 || (Integer) VALUES.get(WorldCfg.CharacterCreatingEvokersPerRealm) > 10) {
            Log.outError(LogFilter.ServerLoading, String.format("CharacterCreating.EvokersPerRealm (%1$s) must be in range 0..10. Set to 1.", VALUES.get(WorldCfg.CharacterCreatingEvokersPerRealm)));
            VALUES.put(WorldCfg.CharacterCreatingEvokersPerRealm, 1);
        }

        VALUES.put(WorldCfg.CharacterCreatingMinLevelForDemonHunter, GetDefaultValue("CharacterCreating.MinLevelForDemonHunter", 0));
        VALUES.put(WorldCfg.CharacterCreatingMinLevelForEvoker, GetDefaultValue("CharacterCreating.MinLevelForEvoker", 50));
        VALUES.put(WorldCfg.CharacterCreatingDisableAlliedRaceAchievementRequirement, GetDefaultValue("CharacterCreating.DisableAlliedRaceAchievementRequirement", false));

        VALUES.put(WorldCfg.SkipCinematics, GetDefaultValue("SkipCinematics", 0));

        if ((Integer) VALUES.get(WorldCfg.SkipCinematics) < 0 || (Integer) VALUES.get(WorldCfg.SkipCinematics) > 2) {
            Log.outError(LogFilter.ServerLoading, "SkipCinematics ({0}) must be in range 0..2. Set to 0.", VALUES.get(WorldCfg.SkipCinematics));
            VALUES.put(WorldCfg.SkipCinematics, 0);
        }

        if (reload) {
            var val = GetDefaultValue("MaxPlayerLevel", SharedConst.DefaultMaxLevel);

            if (val != (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
                Log.outError(LogFilter.ServerLoading, "MaxPlayerLevel option can't be changed at config reload, using current value ({0}).", VALUES.get(WorldCfg.MaxPlayerLevel));
            }
        } else {
            VALUES.put(WorldCfg.MaxPlayerLevel, GetDefaultValue("MaxPlayerLevel", SharedConst.DefaultMaxLevel));
        }

        if ((Integer) VALUES.get(WorldCfg.MaxPlayerLevel) > SharedConst.maxLevel) {
            Log.outError(LogFilter.ServerLoading, "MaxPlayerLevel ({0}) must be in range 1..{1}. Set to {1}.", VALUES.get(WorldCfg.MaxPlayerLevel), SharedConst.maxLevel);
            VALUES.put(WorldCfg.MaxPlayerLevel, SharedConst.maxLevel);
        }

        VALUES.put(WorldCfg.MinDualspecLevel, GetDefaultValue("MinDualSpecLevel", 40));

        VALUES.put(WorldCfg.StartPlayerLevel, GetDefaultValue("StartPlayerLevel", 1));

        if ((Integer) VALUES.get(WorldCfg.StartPlayerLevel) < 1) {
            Log.outError(LogFilter.ServerLoading, "StartPlayerLevel ({0}) must be in range 1..MaxPlayerLevel({1}). Set to 1.", VALUES.get(WorldCfg.StartPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel));
            VALUES.put(WorldCfg.StartPlayerLevel, 1);
        } else if ((Integer) VALUES.get(WorldCfg.StartPlayerLevel) > (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, "StartPlayerLevel ({0}) must be in range 1..MaxPlayerLevel({1}). Set to {2}.", VALUES.get(WorldCfg.StartPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel));
            VALUES.put(WorldCfg.StartPlayerLevel, VALUES.get(WorldCfg.MaxPlayerLevel));
        }

        VALUES.put(WorldCfg.StartDeathKnightPlayerLevel, GetDefaultValue("StartDeathKnightPlayerLevel", 8));

        if ((Integer) VALUES.get(WorldCfg.StartDeathKnightPlayerLevel) < 1) {
            Log.outError(LogFilter.ServerLoading, "StartDeathKnightPlayerLevel ({0}) must be in range 1..MaxPlayerLevel({1}). Set to 1.", VALUES.get(WorldCfg.StartDeathKnightPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel));

            VALUES.put(WorldCfg.StartDeathKnightPlayerLevel, 1);
        } else if ((Integer) VALUES.get(WorldCfg.StartDeathKnightPlayerLevel) > (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, "StartDeathKnightPlayerLevel ({0}) must be in range 1..MaxPlayerLevel({1}). Set to {2}.", VALUES.get(WorldCfg.StartDeathKnightPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel));

            VALUES.put(WorldCfg.StartDeathKnightPlayerLevel, VALUES.get(WorldCfg.MaxPlayerLevel));
        }

        VALUES.put(WorldCfg.StartDemonHunterPlayerLevel, GetDefaultValue("StartDemonHunterPlayerLevel", 8));

        if ((Integer) VALUES.get(WorldCfg.StartDemonHunterPlayerLevel) < 1) {
            Log.outError(LogFilter.ServerLoading, "StartDemonHunterPlayerLevel ({0}) must be in range 1..MaxPlayerLevel({1}). Set to 1.", VALUES.get(WorldCfg.StartDemonHunterPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel));

            VALUES.put(WorldCfg.StartDemonHunterPlayerLevel, 1);
        } else if ((Integer) VALUES.get(WorldCfg.StartDemonHunterPlayerLevel) > (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, "StartDemonHunterPlayerLevel ({0}) must be in range 1..MaxPlayerLevel({1}). Set to {2}.", VALUES.get(WorldCfg.StartDemonHunterPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel));

            VALUES.put(WorldCfg.StartDemonHunterPlayerLevel, VALUES.get(WorldCfg.MaxPlayerLevel));
        }

        VALUES.put(WorldCfg.StartEvokerPlayerLevel, GetDefaultValue("StartEvokerPlayerLevel", 58));

        if ((Integer) VALUES.get(WorldCfg.StartEvokerPlayerLevel) < 1) {
            Log.outError(LogFilter.ServerLoading, String.format("StartEvokerPlayerLevel (%1$s) must be in range 1..MaxPlayerLevel(%2$s). Set to 1.", VALUES.get(WorldCfg.StartEvokerPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel)));
            VALUES.put(WorldCfg.StartEvokerPlayerLevel, 1);
        } else if ((Integer) VALUES.get(WorldCfg.StartEvokerPlayerLevel) > (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, String.format("StartEvokerPlayerLevel (%1$s) must be in range 1..MaxPlayerLevel(%2$s). Set to %3$s.", VALUES.get(WorldCfg.StartEvokerPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel)));
            VALUES.put(WorldCfg.StartEvokerPlayerLevel, VALUES.get(WorldCfg.MaxPlayerLevel));
        }

        VALUES.put(WorldCfg.StartAlliedRaceLevel, GetDefaultValue("StartAlliedRacePlayerLevel", 10));

        if ((Integer) VALUES.get(WorldCfg.StartAlliedRaceLevel) < 1) {
            Log.outError(LogFilter.ServerLoading, String.format("StartAlliedRaceLevel (%1$s) must be in range 1..MaxPlayerLevel(%2$s). Set to 1.", VALUES.get(WorldCfg.StartAlliedRaceLevel), VALUES.get(WorldCfg.MaxPlayerLevel)));
            VALUES.put(WorldCfg.StartAlliedRaceLevel, 1);
        } else if ((Integer) VALUES.get(WorldCfg.StartAlliedRaceLevel) > (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, String.format("StartAlliedRaceLevel (%1$s) must be in range 1..MaxPlayerLevel(%2$s). Set to %3$s.", VALUES.get(WorldCfg.StartAlliedRaceLevel), VALUES.get(WorldCfg.MaxPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel)));
            VALUES.put(WorldCfg.StartAlliedRaceLevel, VALUES.get(WorldCfg.MaxPlayerLevel));
        }

        VALUES.put(WorldCfg.StartPlayerMoney, GetDefaultValue("StartPlayerMoney", 0));

        if ((Integer) VALUES.get(WorldCfg.StartPlayerMoney) < 0) {
            Log.outError(LogFilter.ServerLoading, "StartPlayerMoney ({0}) must be in range 0..{1}. Set to {2}.", VALUES.get(WorldCfg.StartPlayerMoney), PlayerConst.MaxMoneyAmount, 0);
            VALUES.put(WorldCfg.StartPlayerMoney, 0);
        } else if ((Integer) VALUES.get(WorldCfg.StartPlayerMoney) > 0x7FFFFFFF - 1) // TODO: (See MaxMoneyAMOUNT)
        {
            Log.outError(LogFilter.ServerLoading, "StartPlayerMoney ({0}) must be in range 0..{1}. Set to {2}.", VALUES.get(WorldCfg.StartPlayerMoney), 0x7FFFFFFF - 1, 0x7FFFFFFF - 1);

            VALUES.put(WorldCfg.StartPlayerMoney, 0x7FFFFFFF - 1);
        }

        VALUES.put(WorldCfg.CurrencyResetHour, GetDefaultValue("Currency.ResetHour", 3));

        if ((Integer) VALUES.get(WorldCfg.CurrencyResetHour) > 23) {
            Log.outError(LogFilter.ServerLoading, "StartPlayerMoney ({0}) must be in range 0..{1}. Set to {2}.", VALUES.put(WorldCfg.CurrencyResetHour, 3));
        }

        VALUES.put(WorldCfg.CurrencyResetDay, GetDefaultValue("Currency.ResetDay", 3));

        if ((Integer) VALUES.get(WorldCfg.CurrencyResetDay) > 6) {
            Log.outError(LogFilter.ServerLoading, "Currency.ResetDay ({0}) can't be load. Set to 3.", VALUES.get(WorldCfg.CurrencyResetDay));
            VALUES.put(WorldCfg.CurrencyResetDay, 3);
        }

        VALUES.put(WorldCfg.CurrencyResetInterval, GetDefaultValue("Currency.ResetInterval", 7));

        if ((Integer) VALUES.get(WorldCfg.CurrencyResetInterval) <= 0) {
            Log.outError(LogFilter.ServerLoading, "Currency.ResetInterval ({0}) must be > 0, set to default 7.", VALUES.get(WorldCfg.CurrencyResetInterval));
            VALUES.put(WorldCfg.CurrencyResetInterval, 7);
        }

        VALUES.put(WorldCfg.MaxRecruitAFriendBonusPlayerLevel, GetDefaultValue("RecruitAFriend.MaxLevel", 60));

        if ((Integer) VALUES.get(WorldCfg.MaxRecruitAFriendBonusPlayerLevel) > (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, "RecruitAFriend.maxLevel ({0}) must be in the range 0..maxLevel({1}). Set to {2}.", VALUES.get(WorldCfg.MaxRecruitAFriendBonusPlayerLevel), VALUES.get(WorldCfg.MaxPlayerLevel), 60);

            VALUES.put(WorldCfg.MaxRecruitAFriendBonusPlayerLevel, 60);
        }

        VALUES.put(WorldCfg.MaxRecruitAFriendBonusPlayerLevelDifference, GetDefaultValue("RecruitAFriend.MaxDifference", 4));
        VALUES.put(WorldCfg.AllTaxiPaths, GetDefaultValue("AllFlightPaths", false));
        VALUES.put(WorldCfg.InstantTaxi, GetDefaultValue("InstantFlightPaths", false));

        VALUES.put(WorldCfg.InstanceIgnoreLevel, GetDefaultValue("Instance.IgnoreLevel", false));
        VALUES.put(WorldCfg.InstanceIgnoreRaid, GetDefaultValue("Instance.IgnoreRaid", false));

        VALUES.put(WorldCfg.CastUnstuck, GetDefaultValue("CastUnstuck", true));
        VALUES.put(WorldCfg.ResetScheduleWeekDay, GetDefaultValue("ResetSchedule.WeekDay", 2));
        VALUES.put(WorldCfg.ResetScheduleHour, GetDefaultValue("ResetSchedule.Hour", 8));
        VALUES.put(WorldCfg.InstanceUnloadDelay, GetDefaultValue("Instance.UnloadDelay", 30 * time.Minute * time.InMilliseconds));
        VALUES.put(WorldCfg.DailyQuestResetTimeHour, GetDefaultValue("Quests.DailyResetTime", 3));

        if ((Integer) VALUES.get(WorldCfg.DailyQuestResetTimeHour) > 23) {
            Log.outError(LogFilter.ServerLoading, String.format("Quests.DailyResetTime (%1$s) must be in range 0..23. Set to 3.", VALUES.get(WorldCfg.DailyQuestResetTimeHour)));
            VALUES.put(WorldCfg.DailyQuestResetTimeHour, 3);
        }

        VALUES.put(WorldCfg.WeeklyQuestResetTimeWDay, GetDefaultValue("Quests.WeeklyResetWDay", 3));

        if ((Integer) VALUES.get(WorldCfg.WeeklyQuestResetTimeWDay) > 6) {
            Log.outError(LogFilter.ServerLoading, String.format("Quests.WeeklyResetDay (%1$s) must be in range 0..6. Set to 3 (Wednesday).", VALUES.get(WorldCfg.WeeklyQuestResetTimeWDay)));
            VALUES.put(WorldCfg.WeeklyQuestResetTimeWDay, 3);
        }

        VALUES.put(WorldCfg.MaxPrimaryTradeSkill, GetDefaultValue("MaxPrimaryTradeSkill", 2));
        VALUES.put(WorldCfg.MinPetitionSigns, GetDefaultValue("MinPetitionSigns", 4));

        if ((Integer) VALUES.get(WorldCfg.MinPetitionSigns) > 4) {
            Log.outError(LogFilter.ServerLoading, "MinPetitionSigns ({0}) must be in range 0..4. Set to 4.", VALUES.get(WorldCfg.MinPetitionSigns));
            VALUES.put(WorldCfg.MinPetitionSigns, 4);
        }

        VALUES.put(WorldCfg.GmLoginState, GetDefaultValue("GM.LoginState", 2));
        VALUES.put(WorldCfg.GmVisibleState, GetDefaultValue("GM.Visible", 2));
        VALUES.put(WorldCfg.GmChat, GetDefaultValue("GM.Chat", 2));
        VALUES.put(WorldCfg.GmWhisperingTo, GetDefaultValue("GM.WhisperingTo", 2));
        VALUES.put(WorldCfg.GmFreezeDuration, GetDefaultValue("GM.FreezeAuraDuration", 0));

        VALUES.put(WorldCfg.GmLevelInGmList, GetDefaultValue("GM.InGMList.Level", AccountTypes.Administrator.getValue()));
        VALUES.put(WorldCfg.GmLevelInWhoList, GetDefaultValue("GM.InWhoList.Level", AccountTypes.Administrator.getValue()));
        VALUES.put(WorldCfg.StartGmLevel, GetDefaultValue("GM.StartLevel", 1));

        if ((Integer) VALUES.get(WorldCfg.StartGmLevel) < (Integer) VALUES.get(WorldCfg.StartPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, "GM.StartLevel ({0}) must be in range StartPlayerLevel({1})..{2}. Set to {3}.", VALUES.get(WorldCfg.StartGmLevel), VALUES.get(WorldCfg.StartPlayerLevel), SharedConst.maxLevel, VALUES.get(WorldCfg.StartPlayerLevel));

            VALUES.put(WorldCfg.StartGmLevel, VALUES.get(WorldCfg.StartPlayerLevel));
        } else if ((Integer) VALUES.get(WorldCfg.StartGmLevel) > SharedConst.maxLevel) {
            Log.outError(LogFilter.ServerLoading, "GM.StartLevel ({0}) must be in range 1..{1}. Set to {1}.", VALUES.get(WorldCfg.StartGmLevel), SharedConst.maxLevel);
            VALUES.put(WorldCfg.StartGmLevel, SharedConst.maxLevel);
        }

        VALUES.put(WorldCfg.AllowGmGroup, GetDefaultValue("GM.AllowInvite", false));
        VALUES.put(WorldCfg.GmLowerSecurity, GetDefaultValue("GM.LowerSecurity", false));
        VALUES.put(WorldCfg.ForceShutdownThreshold, GetDefaultValue("GM.ForceShutdownThreshold", 30));

        VALUES.put(WorldCfg.GroupVisibility, GetDefaultValue("Visibility.GroupMode", 1));

        VALUES.put(WorldCfg.MailDeliveryDelay, GetDefaultValue("MailDeliveryDelay", time.Hour));
        VALUES.put(WorldCfg.CleanOldMailTime, GetDefaultValue("CleanOldMailTime", 4));

        if ((Integer) VALUES.get(WorldCfg.CleanOldMailTime) > 23) {
            Log.outError(LogFilter.ServerLoading, String.format("CleanOldMailTime (%1$s) must be an hour, between 0 and 23. Set to 4.", VALUES.get(WorldCfg.CleanOldMailTime)));
            VALUES.put(WorldCfg.CleanOldMailTime, 4);
        }

        VALUES.put(WorldCfg.UptimeUpdate, GetDefaultValue("UpdateUptimeInterval", 10));

        if ((Integer) VALUES.get(WorldCfg.UptimeUpdate) <= 0) {
            Log.outError(LogFilter.ServerLoading, "UpdateUptimeInterval ({0}) must be > 0, set to default 10.", VALUES.get(WorldCfg.UptimeUpdate));
            VALUES.put(WorldCfg.UptimeUpdate, 10);
        }

        // log db cleanup interval
        VALUES.put(WorldCfg.LogdbClearinterval, GetDefaultValue("LogDB.Opt.ClearInterval", 10));

        if ((Integer) VALUES.get(WorldCfg.LogdbClearinterval) <= 0) {
            Log.outError(LogFilter.ServerLoading, "LogDB.Opt.ClearInterval ({0}) must be > 0, set to default 10.", VALUES.get(WorldCfg.LogdbClearinterval));
            VALUES.put(WorldCfg.LogdbClearinterval, 10);
        }

        VALUES.put(WorldCfg.LogdbCleartime, GetDefaultValue("LogDB.Opt.ClearTime", 1209600)); // 14 days default
        Log.outInfo(LogFilter.ServerLoading, "Will clear `logs` table of entries older than {0} seconds every {1} minutes.", VALUES.get(WorldCfg.LogdbCleartime), VALUES.get(WorldCfg.LogdbClearinterval));

        VALUES.put(WorldCfg.SkillChanceOrange, GetDefaultValue("SkillChance.Orange", 100));
        VALUES.put(WorldCfg.SkillChanceYellow, GetDefaultValue("SkillChance.Yellow", 75));
        VALUES.put(WorldCfg.SkillChanceGreen, GetDefaultValue("SkillChance.Green", 25));
        VALUES.put(WorldCfg.SkillChanceGrey, GetDefaultValue("SkillChance.Grey", 0));

        VALUES.put(WorldCfg.SkillChanceMiningSteps, GetDefaultValue("SkillChance.MiningSteps", 75));
        VALUES.put(WorldCfg.SkillChanceSkinningSteps, GetDefaultValue("SkillChance.SkinningSteps", 75));

        VALUES.put(WorldCfg.SkillProspecting, GetDefaultValue("SkillChance.Prospecting", false));
        VALUES.put(WorldCfg.SkillMilling, GetDefaultValue("SkillChance.Milling", false));

        VALUES.put(WorldCfg.SkillGainCrafting, GetDefaultValue("SkillGain.Crafting", 1));

        VALUES.put(WorldCfg.SkillGainGathering, GetDefaultValue("SkillGain.Gathering", 1));

        VALUES.put(WorldCfg.MaxOverspeedPings, GetDefaultValue("MaxOverspeedPings", 2));

        if ((Integer) VALUES.get(WorldCfg.MaxOverspeedPings) != 0 && (Integer) VALUES.get(WorldCfg.MaxOverspeedPings) < 2) {
            Log.outError(LogFilter.ServerLoading, "MaxOverspeedPings ({0}) must be in range 2..infinity (or 0 to disable check). Set to 2.", VALUES.get(WorldCfg.MaxOverspeedPings));
            VALUES.put(WorldCfg.MaxOverspeedPings, 2);
        }

        VALUES.put(WorldCfg.Weather, GetDefaultValue("ActivateWeather", true));

        VALUES.put(WorldCfg.DisableBreathing, GetDefaultValue("DisableWaterBreath", AccountTypes.Console.getValue()));

        if (reload) {
            var val = GetDefaultValue("Expansion", expansion.Dragonflight.getValue());

            if (val != (Integer) VALUES.get(WorldCfg.expansion)) {
                Log.outError(LogFilter.ServerLoading, "Expansion option can't be changed at worldserver.conf reload, using current value ({0}).", VALUES.get(WorldCfg.expansion));
            }
        } else {
            VALUES.put(WorldCfg.expansion, GetDefaultValue("Expansion", expansion.Dragonflight));
        }

        VALUES.put(WorldCfg.ChatFloodMessageCount, GetDefaultValue("ChatFlood.MessageCount", 10));
        VALUES.put(WorldCfg.ChatFloodMessageDelay, GetDefaultValue("ChatFlood.MessageDelay", 1));
        VALUES.put(WorldCfg.ChatFloodMuteTime, GetDefaultValue("ChatFlood.MuteTime", 10));

        VALUES.put(WorldCfg.EventAnnounce, GetDefaultValue("Event.Announce", false));

        VALUES.put(WorldCfg.CreatureFamilyFleeAssistanceRadius, GetDefaultValue("CreatureFamilyFleeAssistanceRadius", 30.0f));
        VALUES.put(WorldCfg.CreatureFamilyAssistanceRadius, GetDefaultValue("CreatureFamilyAssistanceRadius", 10.0f));
        VALUES.put(WorldCfg.CreatureFamilyAssistanceDelay, GetDefaultValue("CreatureFamilyAssistanceDelay", 1500));
        VALUES.put(WorldCfg.CreatureFamilyFleeDelay, GetDefaultValue("CreatureFamilyFleeDelay", 7000));

        VALUES.put(WorldCfg.WorldBossLevelDiff, GetDefaultValue("WorldBossLevelDiff", 3));

        VALUES.put(WorldCfg.QuestEnableQuestTracker, GetDefaultValue("Quests.EnableQuestTracker", false));

        // note: disable value (-1) will assigned as 0xFFFFFFF, to prevent overflow at calculations limit it to max possible player Level maxLevel(100)
        VALUES.put(WorldCfg.QuestLowLevelHideDiff, GetDefaultValue("Quests.LowLevelHideDiff", 4));

        if ((Integer) VALUES.get(WorldCfg.QuestLowLevelHideDiff) > SharedConst.maxLevel) {
            VALUES.put(WorldCfg.QuestLowLevelHideDiff, SharedConst.maxLevel);
        }

        VALUES.put(WorldCfg.QuestHighLevelHideDiff, GetDefaultValue("Quests.HighLevelHideDiff", 7));

        if ((Integer) VALUES.get(WorldCfg.QuestHighLevelHideDiff) > SharedConst.maxLevel) {
            VALUES.put(WorldCfg.QuestHighLevelHideDiff, SharedConst.maxLevel);
        }

        VALUES.put(WorldCfg.QuestIgnoreRaid, GetDefaultValue("Quests.IgnoreRaid", false));
        VALUES.put(WorldCfg.QuestIgnoreAutoAccept, GetDefaultValue("Quests.IgnoreAutoAccept", false));
        VALUES.put(WorldCfg.QuestIgnoreAutoComplete, GetDefaultValue("Quests.IgnoreAutoComplete", false));

        VALUES.put(WorldCfg.RandomBgResetHour, GetDefaultValue("Battleground.random.ResetHour", 6));

        if ((Integer) VALUES.get(WorldCfg.RandomBgResetHour) > 23) {
            Log.outError(LogFilter.ServerLoading, "Battleground.random.ResetHour ({0}) can't be load. Set to 6.", VALUES.get(WorldCfg.RandomBgResetHour));
            VALUES.put(WorldCfg.RandomBgResetHour, 6);
        }

        VALUES.put(WorldCfg.CalendarDeleteOldEventsHour, GetDefaultValue("Calendar.DeleteOldEventsHour", 6));

        if ((Integer) VALUES.get(WorldCfg.CalendarDeleteOldEventsHour) > 23) {
            Log.outError(LogFilter.misc, String.format("Calendar.DeleteOldEventsHour (%1$s) can't be load. Set to 6.", VALUES.get(WorldCfg.CalendarDeleteOldEventsHour)));
            VALUES.put(WorldCfg.CalendarDeleteOldEventsHour, 6);
        }

        VALUES.put(WorldCfg.GuildResetHour, GetDefaultValue("Guild.ResetHour", 6));

        if ((Integer) VALUES.get(WorldCfg.GuildResetHour) > 23) {
            Log.outError(LogFilter.Server, "Guild.ResetHour ({0}) can't be load. Set to 6.", VALUES.get(WorldCfg.GuildResetHour));
            VALUES.put(WorldCfg.GuildResetHour, 6);
        }

        VALUES.put(WorldCfg.DetectPosCollision, GetDefaultValue("DetectPosCollision", true));

        VALUES.put(WorldCfg.RestrictedLfgChannel, GetDefaultValue("Channel.RestrictedLfg", true));
        VALUES.put(WorldCfg.TalentsInspecting, GetDefaultValue("TalentsInspecting", 1));
        VALUES.put(WorldCfg.ChatFakeMessagePreventing, GetDefaultValue("ChatFakeMessagePreventing", false));
        VALUES.put(WorldCfg.ChatStrictLinkCheckingSeverity, GetDefaultValue("ChatStrictLinkChecking.Severity", 0));
        VALUES.put(WorldCfg.ChatStrictLinkCheckingKick, GetDefaultValue("ChatStrictLinkChecking.Kick", 0));

        VALUES.put(WorldCfg.CorpseDecayNormal, GetDefaultValue("Corpse.Decay.NORMAL", 60));
        VALUES.put(WorldCfg.CorpseDecayRare, GetDefaultValue("Corpse.Decay.RARE", 300));
        VALUES.put(WorldCfg.CorpseDecayElite, GetDefaultValue("Corpse.Decay.ELITE", 300));
        VALUES.put(WorldCfg.CorpseDecayRareelite, GetDefaultValue("Corpse.Decay.RAREELITE", 300));
        VALUES.put(WorldCfg.CorpseDecayWorldboss, GetDefaultValue("Corpse.Decay.WORLDBOSS", 3600));

        VALUES.put(WorldCfg.DeathSicknessLevel, GetDefaultValue("Death.SicknessLevel", 11));
        VALUES.put(WorldCfg.DeathCorpseReclaimDelayPvp, GetDefaultValue("Death.CorpseReclaimDelay.PvP", true));
        VALUES.put(WorldCfg.DeathCorpseReclaimDelayPve, GetDefaultValue("Death.CorpseReclaimDelay.PvE", true));
        VALUES.put(WorldCfg.DeathBonesWorld, GetDefaultValue("Death.Bones.World", true));
        VALUES.put(WorldCfg.DeathBonesBgOrArena, GetDefaultValue("Death.Bones.BattlegroundOrArena", true));

        VALUES.put(WorldCfg.DieCommandMode, GetDefaultValue("Die.command.Mode", true));

        VALUES.put(WorldCfg.ThreatRadius, GetDefaultValue("ThreatRadius", 60.0f));

        // always use declined names in the russian client
        VALUES.put(WorldCfg.DeclinedNamesUsed, (RealmZones) VALUES.get(WorldCfg.RealmZone) == RealmZones.Russian || GetDefaultValue("DeclinedNames", false));

        VALUES.put(WorldCfg.ListenRangeSay, GetDefaultValue("ListenRange.Say", 25.0f));
        VALUES.put(WorldCfg.ListenRangeTextemote, GetDefaultValue("ListenRange.TextEmote", 25.0f));
        VALUES.put(WorldCfg.ListenRangeYell, GetDefaultValue("ListenRange.Yell", 300.0f));

        VALUES.put(WorldCfg.BattlegroundCastDeserter, GetDefaultValue("Battleground.CastDeserter", true));
        VALUES.put(WorldCfg.BattlegroundQueueAnnouncerEnable, GetDefaultValue("Battleground.QueueAnnouncer.Enable", false));
        VALUES.put(WorldCfg.BattlegroundQueueAnnouncerPlayeronly, GetDefaultValue("Battleground.QueueAnnouncer.PlayerOnly", false));
        VALUES.put(WorldCfg.BattlegroundStoreStatisticsEnable, GetDefaultValue("Battleground.StoreStatistics.Enable", false));
        VALUES.put(WorldCfg.BattlegroundReportAfk, GetDefaultValue("Battleground.ReportAFK", 3));

        if ((Integer) VALUES.get(WorldCfg.BattlegroundReportAfk) < 1) {
            Log.outError(LogFilter.ServerLoading, "Battleground.ReportAFK ({0}) must be >0. Using 3 instead.", VALUES.get(WorldCfg.BattlegroundReportAfk));
            VALUES.put(WorldCfg.BattlegroundReportAfk, 3);
        }

        if ((Integer) VALUES.get(WorldCfg.BattlegroundReportAfk) > 9) {
            Log.outError(LogFilter.ServerLoading, "Battleground.ReportAFK ({0}) must be <10. Using 3 instead.", VALUES.get(WorldCfg.BattlegroundReportAfk));
            VALUES.put(WorldCfg.BattlegroundReportAfk, 3);
        }

        VALUES.put(WorldCfg.BattlegroundInvitationType, GetDefaultValue("Battleground.InvitationType", 0));
        VALUES.put(WorldCfg.BattlegroundPrematureFinishTimer, GetDefaultValue("Battleground.PrematureFinishTimer", 5 * time.Minute * time.InMilliseconds));
        VALUES.put(WorldCfg.BattlegroundPremadeGroupWaitForMatch, GetDefaultValue("Battleground.PremadeGroupWaitForMatch", 30 * time.Minute * time.InMilliseconds));
        VALUES.put(WorldCfg.BgXpForKill, GetDefaultValue("Battleground.GiveXPForKills", false));
        VALUES.put(WorldCfg.ArenaMaxRatingDifference, GetDefaultValue("Arena.MaxRatingDifference", 150));
        VALUES.put(WorldCfg.ArenaRatingDiscardTimer, GetDefaultValue("Arena.RatingDiscardTimer", 10 * time.Minute * time.InMilliseconds));
        VALUES.put(WorldCfg.ArenaRatedUpdateTimer, GetDefaultValue("Arena.RatedUpdateTimer", 5 * time.InMilliseconds));
        VALUES.put(WorldCfg.ArenaQueueAnnouncerEnable, GetDefaultValue("Arena.QueueAnnouncer.Enable", false));
        VALUES.put(WorldCfg.ArenaSeasonId, GetDefaultValue("Arena.ArenaSeason.ID", 32));
        VALUES.put(WorldCfg.ArenaStartRating, GetDefaultValue("Arena.ArenaStartRating", 0));
        VALUES.put(WorldCfg.ArenaStartPersonalRating, GetDefaultValue("Arena.ArenaStartPersonalRating", 1000));
        VALUES.put(WorldCfg.ArenaStartMatchmakerRating, GetDefaultValue("Arena.ArenaStartMatchmakerRating", 1500));
        VALUES.put(WorldCfg.ArenaSeasonInProgress, GetDefaultValue("Arena.ArenaSeason.InProgress", false));
        VALUES.put(WorldCfg.ArenaLogExtendedInfo, GetDefaultValue("ArenaLog.ExtendedInfo", false));
        VALUES.put(WorldCfg.ArenaWinRatingModifier1, GetDefaultValue("Arena.ArenaWinRatingModifier1", 48.0f));
        VALUES.put(WorldCfg.ArenaWinRatingModifier2, GetDefaultValue("Arena.ArenaWinRatingModifier2", 24.0f));
        VALUES.put(WorldCfg.ArenaLoseRatingModifier, GetDefaultValue("Arena.ArenaLoseRatingModifier", 24.0f));
        VALUES.put(WorldCfg.ArenaMatchmakerRatingModifier, GetDefaultValue("Arena.ArenaMatchmakerRatingModifier", 24.0f));

        if (reload) {
            global.getWorldStateMgr().setValue(WorldStates.CurrentPvpSeasonId, getBoolValue(WorldCfg.ArenaSeasonInProgress) ? getIntValue(WorldCfg.ArenaSeasonId) : 0, false, null);
            global.getWorldStateMgr().setValue(WorldStates.PreviousPvpSeasonId, getIntValue(WorldCfg.ArenaSeasonId) - (getBoolValue(WorldCfg.ArenaSeasonInProgress) ? 1 : 0), false, null);
        }

        VALUES.put(WorldCfg.OffhandCheckAtSpellUnlearn, GetDefaultValue("OffhandCheckAtSpellUnlearn", true));

        VALUES.put(WorldCfg.CreaturePickpocketRefill, GetDefaultValue("Creature.PickPocketRefillDelay", 10 * time.Minute));
        VALUES.put(WorldCfg.CreatureStopForPlayer, GetDefaultValue("Creature.MovingStopTimeForPlayer", 3 * time.Minute * time.InMilliseconds));

        var clientCacheId = GetDefaultValue("ClientCacheVersion", 0);

        if (clientCacheId != 0) {
            // overwrite DB/old value
            if (clientCacheId > 0) {
                VALUES.put(WorldCfg.ClientCacheVersion, clientCacheId);
            } else {
                Log.outError(LogFilter.ServerLoading, "ClientCacheVersion can't be negative {0}, ignored.", clientCacheId);
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Client cache version set to: {0}", clientCacheId);

        VALUES.put(WorldCfg.GuildNewsLogCount, GetDefaultValue("Guild.NewsLogRecordsCount", GuildConst.NewsLogMaxRecords));

        if ((Integer) VALUES.get(WorldCfg.GuildNewsLogCount) > GuildConst.NewsLogMaxRecords) {
            VALUES.put(WorldCfg.GuildNewsLogCount, GuildConst.NewsLogMaxRecords);
        }

        VALUES.put(WorldCfg.GuildEventLogCount, GetDefaultValue("Guild.EventLogRecordsCount", GuildConst.EventLogMaxRecords));

        if ((Integer) VALUES.get(WorldCfg.GuildEventLogCount) > GuildConst.EventLogMaxRecords) {
            VALUES.put(WorldCfg.GuildEventLogCount, GuildConst.EventLogMaxRecords);
        }

        VALUES.put(WorldCfg.GuildBankEventLogCount, GetDefaultValue("Guild.BankEventLogRecordsCount", GuildConst.BankLogMaxRecords));

        if ((Integer) VALUES.get(WorldCfg.GuildBankEventLogCount) > GuildConst.BankLogMaxRecords) {
            VALUES.put(WorldCfg.GuildBankEventLogCount, GuildConst.BankLogMaxRecords);
        }

        // Load the CharDelete related config options
        VALUES.put(WorldCfg.ChardeleteMethod, GetDefaultValue("CharDelete.Method", 0));
        VALUES.put(WorldCfg.ChardeleteMinLevel, GetDefaultValue("CharDelete.MinLevel", 0));
        VALUES.put(WorldCfg.ChardeleteDeathKnightMinLevel, GetDefaultValue("CharDelete.DeathKnight.MinLevel", 0));
        VALUES.put(WorldCfg.ChardeleteDemonHunterMinLevel, GetDefaultValue("CharDelete.DemonHunter.MinLevel", 0));
        VALUES.put(WorldCfg.ChardeleteKeepDays, GetDefaultValue("CharDelete.KeepDays", 30));

        // No aggro from gray mobs
        VALUES.put(WorldCfg.NoGrayAggroAbove, GetDefaultValue("NoGrayAggro.Above", 0));
        VALUES.put(WorldCfg.NoGrayAggroBelow, GetDefaultValue("NoGrayAggro.Below", 0));

        if ((Integer) VALUES.get(WorldCfg.NoGrayAggroAbove) > (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, "NoGrayAggro.Above ({0}) must be in range 0..{1}. Set to {1}.", VALUES.get(WorldCfg.NoGrayAggroAbove), VALUES.get(WorldCfg.MaxPlayerLevel));
            VALUES.put(WorldCfg.NoGrayAggroAbove, VALUES.get(WorldCfg.MaxPlayerLevel));
        }

        if ((Integer) VALUES.get(WorldCfg.NoGrayAggroBelow) > (Integer) VALUES.get(WorldCfg.MaxPlayerLevel)) {
            Log.outError(LogFilter.ServerLoading, "NoGrayAggro.Below ({0}) must be in range 0..{1}. Set to {1}.", VALUES.get(WorldCfg.NoGrayAggroBelow), VALUES.get(WorldCfg.MaxPlayerLevel));
            VALUES.put(WorldCfg.NoGrayAggroBelow, VALUES.get(WorldCfg.MaxPlayerLevel));
        }

        if ((Integer) VALUES.get(WorldCfg.NoGrayAggroAbove) > 0 && (Integer) VALUES.get(WorldCfg.NoGrayAggroAbove) < (Integer) VALUES.get(WorldCfg.NoGrayAggroBelow)) {
            Log.outError(LogFilter.ServerLoading, "NoGrayAggro.Below ({0}) cannot be greater than NoGrayAggro.Above ({1}). Set to {1}.", VALUES.get(WorldCfg.NoGrayAggroBelow), VALUES.get(WorldCfg.NoGrayAggroAbove));
            VALUES.put(WorldCfg.NoGrayAggroBelow, VALUES.get(WorldCfg.NoGrayAggroAbove));
        }

        // Respawn Settings
        VALUES.put(WorldCfg.RespawnMinCheckIntervalMs, GetDefaultValue("Respawn.MinCheckIntervalMS", 5000));
        VALUES.put(WorldCfg.RespawnDynamicMode, GetDefaultValue("Respawn.DynamicMode", 0));

        if ((Integer) VALUES.get(WorldCfg.RespawnDynamicMode) > 1) {
            Log.outError(LogFilter.ServerLoading, String.format("Invalid value for Respawn.DynamicMode (%1$s). Set to 0.", VALUES.get(WorldCfg.RespawnDynamicMode)));
            VALUES.put(WorldCfg.RespawnDynamicMode, 0);
        }

        VALUES.put(WorldCfg.RespawnDynamicEscortNpc, GetDefaultValue("Respawn.DynamicEscortNPC", false));
        VALUES.put(WorldCfg.RespawnGuidWarnLevel, GetDefaultValue("Respawn.GuidWarnLevel", 12000000));

        if ((Integer) VALUES.get(WorldCfg.RespawnGuidWarnLevel) > 16777215) {
            Log.outError(LogFilter.ServerLoading, String.format("Respawn.GuidWarnLevel (%1$s) cannot be greater than maximum GUID (16777215). Set to 12000000.", VALUES.get(WorldCfg.RespawnGuidWarnLevel)));
            VALUES.put(WorldCfg.RespawnGuidWarnLevel, 12000000);
        }

        VALUES.put(WorldCfg.RespawnGuidAlertLevel, GetDefaultValue("Respawn.GuidAlertLevel", 16000000));

        if ((Integer) VALUES.get(WorldCfg.RespawnGuidAlertLevel) > 16777215) {
            Log.outError(LogFilter.ServerLoading, String.format("Respawn.GuidWarnLevel (%1$s) cannot be greater than maximum GUID (16777215). Set to 16000000.", VALUES.get(WorldCfg.RespawnGuidAlertLevel)));
            VALUES.put(WorldCfg.RespawnGuidAlertLevel, 16000000);
        }

        VALUES.put(WorldCfg.RespawnRestartQuietTime, GetDefaultValue("Respawn.RestartQuietTime", 3));

        if ((Integer) VALUES.get(WorldCfg.RespawnRestartQuietTime) > 23) {
            Log.outError(LogFilter.ServerLoading, String.format("Respawn.RestartQuietTime (%1$s) must be an hour, between 0 and 23. Set to 3.", VALUES.get(WorldCfg.RespawnRestartQuietTime)));
            VALUES.put(WorldCfg.RespawnRestartQuietTime, 3);
        }

        VALUES.put(WorldCfg.RespawnDynamicRateCreature, GetDefaultValue("Respawn.DynamicRateCreature", 10.0f));

        if ((Float) VALUES.get(WorldCfg.RespawnDynamicRateCreature) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, String.format("Respawn.DynamicRateCreature (%1$s) must be positive. Set to 10.", VALUES.get(WorldCfg.RespawnDynamicRateCreature)));
            VALUES.put(WorldCfg.RespawnDynamicRateCreature, 10.0f);
        }

        VALUES.put(WorldCfg.RespawnDynamicMinimumCreature, GetDefaultValue("Respawn.DynamicMinimumCreature", 10));
        VALUES.put(WorldCfg.RespawnDynamicRateGameobject, GetDefaultValue("Respawn.DynamicRateGameObject", 10.0f));

        if ((Float) VALUES.get(WorldCfg.RespawnDynamicRateGameobject) < 0.0f) {
            Log.outError(LogFilter.ServerLoading, String.format("Respawn.DynamicRateGameObject (%1$s) must be positive. Set to 10.", VALUES.get(WorldCfg.RespawnDynamicRateGameobject)));
            VALUES.put(WorldCfg.RespawnDynamicRateGameobject, 10.0f);
        }

        VALUES.put(WorldCfg.RespawnDynamicMinimumGameObject, GetDefaultValue("Respawn.DynamicMinimumGameObject", 10));
        VALUES.put(WorldCfg.RespawnGuidWarningFrequency, GetDefaultValue("Respawn.WarningFrequency", 1800));

        VALUES.put(WorldCfg.EnableMmaps, GetDefaultValue("mmap.EnablePathFinding", true));
        VALUES.put(WorldCfg.VmapIndoorCheck, GetDefaultValue("vmap.EnableIndoorCheck", false));

        VALUES.put(WorldCfg.MaxWho, GetDefaultValue("MaxWhoListReturns", 49));
        VALUES.put(WorldCfg.StartAllSpells, GetDefaultValue("PlayerStart.AllSpells", false));

        if ((Boolean) VALUES.get(WorldCfg.StartAllSpells)) {
            Log.outWarn(LogFilter.ServerLoading, "PlayerStart.AllSpells Enabled - may not function as intended!");
        }

        VALUES.put(WorldCfg.HonorAfterDuel, GetDefaultValue("HonorPointsAfterDuel", 0));
        VALUES.put(WorldCfg.ResetDuelCooldowns, GetDefaultValue("ResetDuelCooldowns", false));
        VALUES.put(WorldCfg.ResetDuelHealthMana, GetDefaultValue("ResetDuelHealthMana", false));
        VALUES.put(WorldCfg.StartAllExplored, GetDefaultValue("PlayerStart.MapsExplored", false));
        VALUES.put(WorldCfg.StartAllRep, GetDefaultValue("PlayerStart.AllReputation", false));
        VALUES.put(WorldCfg.PvpTokenEnable, GetDefaultValue("PvPToken.Enable", false));
        VALUES.put(WorldCfg.PvpTokenMapType, GetDefaultValue("PvPToken.MapAllowType", 4));
        VALUES.put(WorldCfg.PvpTokenId, GetDefaultValue("PvPToken.ItemID", 29434));
        VALUES.put(WorldCfg.PvpTokenCount, GetDefaultValue("PvPToken.ItemCount", 1));

        if ((Integer) VALUES.get(WorldCfg.PvpTokenCount) < 1) {
            VALUES.put(WorldCfg.PvpTokenCount, 1);
        }

        VALUES.put(WorldCfg.NoResetTalentCost, GetDefaultValue("NoResetTalentsCost", false));
        VALUES.put(WorldCfg.ShowKickInWorld, GetDefaultValue("ShowKickInWorld", false));
        VALUES.put(WorldCfg.ShowMuteInWorld, GetDefaultValue("ShowMuteInWorld", false));
        VALUES.put(WorldCfg.ShowBanInWorld, GetDefaultValue("ShowBanInWorld", false));
        VALUES.put(WorldCfg.Numthreads, GetDefaultValue("MapUpdate.Threads", 10));
        VALUES.put(WorldCfg.MaxResultsLookupCommands, GetDefaultValue("Command.LookupMaxResults", 0));

        // Warden
        VALUES.put(WorldCfg.WardenEnabled, GetDefaultValue("Warden.Enabled", false));
        VALUES.put(WorldCfg.WardenNumInjectChecks, GetDefaultValue("Warden.NumInjectionChecks", 9));
        VALUES.put(WorldCfg.WardenNumLuaChecks, GetDefaultValue("Warden.NumLuaSandboxChecks", 1));
        VALUES.put(WorldCfg.WardenNumClientModChecks, GetDefaultValue("Warden.NumClientModChecks", 1));
        VALUES.put(WorldCfg.WardenClientBanDuration, GetDefaultValue("Warden.BanDuration", 86400));
        VALUES.put(WorldCfg.WardenClientCheckHoldoff, GetDefaultValue("Warden.ClientCheckHoldOff", 30));
        VALUES.put(WorldCfg.WardenClientFailAction, GetDefaultValue("Warden.ClientCheckFailAction", 0));
        VALUES.put(WorldCfg.WardenClientResponseDelay, GetDefaultValue("Warden.ClientResponseDelay", 600));

        // Feature System
        VALUES.put(WorldCfg.FeatureSystemBpayStoreEnabled, GetDefaultValue("FeatureSystem.BpayStore.Enabled", false));
        VALUES.put(WorldCfg.FeatureSystemCharacterUndeleteEnabled, GetDefaultValue("FeatureSystem.CharacterUndelete.Enabled", false));
        VALUES.put(WorldCfg.FeatureSystemCharacterUndeleteCooldown, GetDefaultValue("FeatureSystem.CharacterUndelete.Cooldown", 2592000));
        VALUES.put(WorldCfg.FeatureSystemWarModeEnabled, GetDefaultValue("FeatureSystem.WarMode.Enabled", false));

        // Dungeon finder
        VALUES.put(WorldCfg.LfgOptionsmask, GetDefaultValue("DungeonFinder.OptionsMask", 1));

        // DBC_ItemAttributes
        VALUES.put(WorldCfg.DbcEnforceItemAttributes, GetDefaultValue("DBC.EnforceItemAttributes", true));

        // Accountpassword Secruity
        VALUES.put(WorldCfg.AccPasschangesec, GetDefaultValue("Account.PasswordChangeSecurity", 0));

        // Random Battleground Rewards
        VALUES.put(WorldCfg.BgRewardWinnerHonorFirst, GetDefaultValue("Battleground.RewardWinnerHonorFirst", 27000));
        VALUES.put(WorldCfg.BgRewardWinnerConquestFirst, GetDefaultValue("Battleground.RewardWinnerConquestFirst", 10000));
        VALUES.put(WorldCfg.BgRewardWinnerHonorLast, GetDefaultValue("Battleground.RewardWinnerHonorLast", 13500));
        VALUES.put(WorldCfg.BgRewardWinnerConquestLast, GetDefaultValue("Battleground.RewardWinnerConquestLast", 5000));
        VALUES.put(WorldCfg.BgRewardLoserHonorFirst, GetDefaultValue("Battleground.RewardLoserHonorFirst", 4500));
        VALUES.put(WorldCfg.BgRewardLoserHonorLast, GetDefaultValue("Battleground.RewardLoserHonorLast", 3500));

        // Max instances per hour
        VALUES.put(WorldCfg.MaxInstancesPerHour, GetDefaultValue("AccountInstancesPerHour", 5));

        // Anounce reset of instance to whole party
        VALUES.put(WorldCfg.InstancesResetAnnounce, GetDefaultValue("InstancesResetAnnounce", false));

        // Autobroadcast
        //AutoBroadcast.On
        VALUES.put(WorldCfg.AutoBroadcast, GetDefaultValue("AutoBroadcast.On", false));
        VALUES.put(WorldCfg.AutoBroadcastCenter, GetDefaultValue("AutoBroadcast.Center", 0));
        VALUES.put(WorldCfg.AutoBroadcastInterval, GetDefaultValue("AutoBroadcast.Timer", 60000));

        // Guild save interval
        VALUES.put(WorldCfg.GuildSaveInterval, GetDefaultValue("Guild.SaveInterval", 15));

        // misc
        VALUES.put(WorldCfg.PdumpNoPaths, GetDefaultValue("PlayerDump.DisallowPaths", true));
        VALUES.put(WorldCfg.PdumpNoOverwrite, GetDefaultValue("PlayerDump.DisallowOverwrite", true));

        // Wintergrasp battlefield
        VALUES.put(WorldCfg.WintergraspEnable, GetDefaultValue("Wintergrasp.Enable", false));
        VALUES.put(WorldCfg.WintergraspPlrMax, GetDefaultValue("Wintergrasp.PlayerMax", 100));
        VALUES.put(WorldCfg.WintergraspPlrMin, GetDefaultValue("Wintergrasp.PlayerMin", 0));
        VALUES.put(WorldCfg.WintergraspPlrMinLvl, GetDefaultValue("Wintergrasp.PlayerMinLvl", 77));
        VALUES.put(WorldCfg.WintergraspBattletime, GetDefaultValue("Wintergrasp.BattleTimer", 30));
        VALUES.put(WorldCfg.WintergraspNobattletime, GetDefaultValue("Wintergrasp.NoBattleTimer", 150));
        VALUES.put(WorldCfg.WintergraspRestartAfterCrash, GetDefaultValue("Wintergrasp.CrashRestartTimer", 10));

        // Tol Barad battlefield
        VALUES.put(WorldCfg.TolbaradEnable, GetDefaultValue("TolBarad.Enable", true));
        VALUES.put(WorldCfg.TolbaradPlrMax, GetDefaultValue("TolBarad.PlayerMax", 100));
        VALUES.put(WorldCfg.TolbaradPlrMin, GetDefaultValue("TolBarad.PlayerMin", 0));
        VALUES.put(WorldCfg.TolbaradPlrMinLvl, GetDefaultValue("TolBarad.PlayerMinLvl", 85));
        VALUES.put(WorldCfg.TolbaradBattleTime, GetDefaultValue("TolBarad.BattleTimer", 15));
        VALUES.put(WorldCfg.TolbaradBonusTime, GetDefaultValue("TolBarad.BonusTime", 5));
        VALUES.put(WorldCfg.TolbaradNoBattleTime, GetDefaultValue("TolBarad.NoBattleTimer", 150));
        VALUES.put(WorldCfg.TolbaradRestartAfterCrash, GetDefaultValue("TolBarad.CrashRestartTimer", 10));

        // Stats limits
        VALUES.put(WorldCfg.StatsLimitsEnable, GetDefaultValue("Stats.Limits.Enable", false));
        VALUES.put(WorldCfg.StatsLimitsDodge, GetDefaultValue("Stats.Limits.Dodge", 95.0f));
        VALUES.put(WorldCfg.StatsLimitsParry, GetDefaultValue("Stats.Limits.Parry", 95.0f));
        VALUES.put(WorldCfg.StatsLimitsBlock, GetDefaultValue("Stats.Limits.Block", 95.0f));
        VALUES.put(WorldCfg.StatsLimitsCrit, GetDefaultValue("Stats.Limits.Crit", 95.0f));

        //packet spoof punishment
        VALUES.put(WorldCfg.PacketSpoofPolicy, GetDefaultValue("PacketSpoof.Policy", 1)); //Kick
        VALUES.put(WorldCfg.PacketSpoofBanmode, GetDefaultValue("PacketSpoof.BanMode", BanMode.Account.getValue()));

        if ((Integer) VALUES.get(WorldCfg.PacketSpoofBanmode) == 1 || (Integer) VALUES.get(WorldCfg.PacketSpoofBanmode) > 2) {
            VALUES.put(WorldCfg.PacketSpoofBanmode, BanMode.Account.getValue());
        }

        VALUES.put(WorldCfg.PacketSpoofBanduration, GetDefaultValue("PacketSpoof.BanDuration", 86400));

        VALUES.put(WorldCfg.IpBasedActionLogging, GetDefaultValue("Allow.IP.Based.action.Logging", false));

        // AHBot
        VALUES.put(WorldCfg.AhbotUpdateInterval, GetDefaultValue("AuctionHouseBot.Update.Interval", 20));

        VALUES.put(WorldCfg.CalculateCreatureZoneAreaData, GetDefaultValue("Calculate.CREATURE.Zone.area.Data", false));
        VALUES.put(WorldCfg.CalculateGameobjectZoneAreaData, GetDefaultValue("Calculate.Gameoject.Zone.area.Data", false));

        // Black Market
        VALUES.put(WorldCfg.BlackmarketEnabled, GetDefaultValue("BlackMarket.Enabled", true));

        VALUES.put(WorldCfg.BlackmarketMaxAuctions, GetDefaultValue("BlackMarket.MaxAuctions", 12));
        VALUES.put(WorldCfg.BlackmarketUpdatePeriod, GetDefaultValue("BlackMarket.UpdatePeriod", 24));

        // prevent character rename on character customization
        VALUES.put(WorldCfg.PreventRenameCustomization, GetDefaultValue("PreventRenameCharacterOnCustomization", false));

        // Allow 5-man parties to use raid warnings
        VALUES.put(WorldCfg.ChatPartyRaidWarnings, GetDefaultValue("PartyRaidWarnings", false));

        // Allow to cache data queries
        VALUES.put(WorldCfg.CacheDataQueries, GetDefaultValue("CacheDataQueries", true));

        // Check Invalid Position
        VALUES.put(WorldCfg.CreatureCheckInvalidPostion, GetDefaultValue("Creature.CheckInvalidPosition", false));
        VALUES.put(WorldCfg.GameobjectCheckInvalidPostion, GetDefaultValue("GameObject.CheckInvalidPosition", false));

        // Whether to use LoS from game objects
        VALUES.put(WorldCfg.CheckGobjectLos, GetDefaultValue("CheckGameObjectLoS", true));

        // FactionBalance
        VALUES.put(WorldCfg.FactionBalanceLevelCheckDiff, GetDefaultValue("Pvp.FactionBalance.LevelCheckDiff", 0));
        VALUES.put(WorldCfg.CallToArms5Pct, GetDefaultValue("Pvp.FactionBalance.Pct5", 0.6f));
        VALUES.put(WorldCfg.CallToArms10Pct, GetDefaultValue("Pvp.FactionBalance.Pct10", 0.7f));
        VALUES.put(WorldCfg.CallToArms20Pct, GetDefaultValue("Pvp.FactionBalance.Pct20", 0.8f));

        // Specifies if IP addresses can be logged to the database
        VALUES.put(WorldCfg.AllowLogginIpAddressesInDatabase, GetDefaultValue("AllowLoggingIPAddressesInDatabase", true));

        // call ScriptMgr if we're reloading the configuration
        if (reload) {
            global.getScriptMgr().<IWorldOnConfigLoad>ForEach(p -> p.OnConfigLoad(reload));
        }
    }

    public static int getUIntValue(WorldCfg confi) {
        return (int) VALUES.get(confi);
    }

    public static int getIntValue(WorldCfg confi) {
        return (int) VALUES.get(confi);
    }

    public static long getUInt64Value(WorldCfg confi) {
        return (long) VALUES.get(confi);
    }

    public static boolean getBoolValue(WorldCfg confi) {
        return (boolean) VALUES.get(confi);
    }

    public static float getFloatValue(WorldCfg confi) {
        return (float) VALUES.get(confi);
    }

    public static void setValue(WorldCfg confi, Object value) {
        VALUES.put(confi, value);
    }
}
