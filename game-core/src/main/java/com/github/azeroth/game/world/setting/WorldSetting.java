package com.github.azeroth.game.world.setting;

import com.github.azeroth.common.Locale;
import com.github.azeroth.defines.Expansion;
import lombok.AllArgsConstructor;

import java.nio.file.Path;


/**
 * The all config items from worldserver.conf
 * worldserver.worlddatabaseinfo.jdbcUrl
 * worldserver.worlddatabaseinfo.username
 * worldserver.worlddatabaseinfo.password
 * worldserver.worlddatabaseinfo.driverClassname
 * worldserver.worlddatabaseinfo.connectionTimeout
 * worldserver.worlddatabaseinfo.idleTimeout
 * worldserver.worlddatabaseinfo.maxLifetime
 * worldserver.worlddatabaseinfo.jpa.hibernate.hbm2ddl.auto
 * worldserver.worlddatabaseinfo.jpa.hibernate.show_sql
 * worldserver.worlddatabaseinfo.jpa.hibernate.order_inserts
 * worldserver.worlddatabaseinfo.jpa.hibernate.order_updates
 * worldserver.worlddatabaseinfo.jpa.hibernate.dialect
 * worldserver.characterdatabaseinfo.jdbcUrl
 * worldserver.characterdatabaseinfo.username
 * worldserver.characterdatabaseinfo.password
 * worldserver.characterdatabaseinfo.driverClassname
 * worldserver.characterdatabaseinfo.connectionTimeout
 * worldserver.characterdatabaseinfo.idleTimeout
 * worldserver.characterdatabaseinfo.maxLifetime
 * worldserver.characterdatabaseinfo.jpa.hibernate.hbm2ddl.auto
 * worldserver.characterdatabaseinfo.jpa.hibernate.show_sql
 * worldserver.characterdatabaseinfo.jpa.hibernate.order_inserts
 * worldserver.characterdatabaseinfo.jpa.hibernate.order_updates
 * worldserver.characterdatabaseinfo.jpa.hibernate.dialect
 * worldserver.hotfixdatabaseinfo.jdbcUrl
 * worldserver.hotfixdatabaseinfo.username
 * worldserver.hotfixdatabaseinfo.password
 * worldserver.hotfixdatabaseinfo.driverClassname
 * worldserver.hotfixdatabaseinfo.connectionTimeout
 * worldserver.hotfixdatabaseinfo.idleTimeout
 * worldserver.hotfixdatabaseinfo.maxLifetime
 * worldserver.hotfixdatabaseinfo.jpa.hibernate.hbm2ddl.auto
 * worldserver.hotfixdatabaseinfo.jpa.hibernate.show_sql
 * worldserver.hotfixdatabaseinfo.jpa.hibernate.order_inserts
 * worldserver.hotfixdatabaseinfo.jpa.hibernate.order_updates
 * worldserver.hotfixdatabaseinfo.jpa.hibernate.dialect
 * worldserver.logindatabase.WorkerThreads
 * worldserver.logindatabase.SynchThreads
 * worldserver.worlddatabase.WorkerThreads
 * worldserver.worlddatabase.SynchThreads
 * worldserver.characterdatabase.WorkerThreads
 * worldserver.characterdatabase.SynchThreads
 * worldserver.hotfixdatabase.WorkerThreads
 * worldserver.hotfixdatabase.SynchThreads
 * worldserver.playersave.stats.MinLevel
 * worldserver.playersave.stats.SaveOnlyOnLogout
 * worldserver.logdb.opt.ClearInterval
 * worldserver.logdb.opt.ClearTime
 * worldserver.auction.ReplicateItemsCooldown
 * worldserver.auction.SearchDelay
 * worldserver.auction.TaintedSearchDelay
 * <p>
 * worldserver.guild.CharterCost
 * worldserver.guild.EventLogRecordsCount
 * worldserver.guild.ResetHour
 * worldserver.guild.BankEventLogRecordsCount
 * worldserver.guild.NewsLogRecordsCount
 * worldserver.guild.SaveInterval
 * worldserver.guild.AllowMultipleGuildMaster
 * worldserver.arenateam.chartercost.2v2
 * worldserver.arenateam.chartercost.3v3
 * worldserver.arenateam.chartercost.5v5
 * worldserver.charactercreating.Disable
 * worldserver.charactercreating.disabled.RaceMask
 * worldserver.charactercreating.disabled.ClassMask
 * worldserver.charactercreating.EvokersPerRealm
 * worldserver.charactercreating.MinLevelForDemonHunter
 * worldserver.charactercreating.MinLevelForEvoker
 * worldserver.charactercreating.DisableAlliedRaceAchievementRequirement
 * worldserver.recruitafriend.MaxLevel
 * worldserver.recruitafriend.MaxDifference

 * worldserver.resetschedule.WeekDay
 * worldserver.resetschedule.Hour
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * worldserver.calendar.DeleteOldEventsHour
 * worldserver.skillchance.Prospecting
 * worldserver.skillchance.Milling
 * worldserver.skillchance.Orange
 * worldserver.skillchance.Yellow
 * worldserver.skillchance.Green
 * worldserver.skillchance.Grey
 * worldserver.skillchance.MiningSteps
 * worldserver.skillchance.SkinningSteps
 * worldserver.event.Announce
 * worldserver.server.LoginInfo
 * worldserver.command.LookupMaxResults
 * worldserver.dungeonfinder.OptionsMask
 * worldserver.account.PasswordChangeSecurity
 * worldserver.featuresystem.bpaystore.Enabled
 * worldserver.featuresystem.characterundelete.Enabled
 * worldserver.featuresystem.characterundelete.Cooldown
 * worldserver.updates.EnableDatabases
 * worldserver.updates.AutoSetup
 * worldserver.updates.Redundancy
 * worldserver.updates.ArchivedRedundancy
 * worldserver.updates.AllowRehash
 * worldserver.updates.CleanDeadRefMaxCount
 * worldserver.hotswap.Enabled
 * worldserver.hotswap.ScriptDir
 * worldserver.hotswap.EnableReCompiler
 * worldserver.hotswap.EnableEarlyTermination
 * worldserver.hotswap.EnableBuildFileRecreation
 * worldserver.hotswap.EnableInstall
 * worldserver.hotswap.EnablePrefixCorrection
 * worldserver.hotswap.ReCompilerBuildType
 * worldserver.warden.Enabled
 * worldserver.warden.NumInjectionChecks
 * worldserver.warden.NumLuaSandboxChecks
 * worldserver.warden.NumClientModChecks
 * worldserver.warden.ClientResponseDelay
 * worldserver.warden.ClientCheckHoldOff
 * worldserver.warden.ClientCheckFailAction
 * worldserver.warden.BanDuration

 * worldserver.corpse.decay.Normal
 * worldserver.corpse.decay.Elite
 * worldserver.corpse.decay.RareElite
 * worldserver.corpse.decay.Obsolete
 * worldserver.corpse.decay.Rare
 * worldserver.corpse.decay.Trivial
 * worldserver.corpse.decay.MinusMob

 * worldserver.listenrange.Say
 * worldserver.listenrange.TextEmote
 * worldserver.listenrange.Yell
 * worldserver.chatstrictlinkchecking.Severity
 * worldserver.chatstrictlinkchecking.Kick
 * <p>
 * worldserver.channel.RestrictedLfg
 * worldserver.chatlevelreq.Channel
 * worldserver.chatlevelreq.Whisper
 * worldserver.chatlevelreq.Emote
 * worldserver.chatlevelreq.Say
 * worldserver.chatlevelreq.Yell
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * worldserver.support.Enabled
 * worldserver.support.TicketsEnabled
 * worldserver.support.BugsEnabled
 * worldserver.support.ComplaintsEnabled
 * worldserver.support.SuggestionsEnabled
 * <p>
 * worldserver.skillgain.Crafting
 * worldserver.skillgain.Gathering
 * worldserver.durabilityloss.InPvP
 * worldserver.durabilityloss.OnDeath
 * worldserver.durabilitylosschance.Damage
 * worldserver.durabilitylosschance.Absorb
 * worldserver.durabilitylosschance.Parry
 * worldserver.durabilitylosschance.Block
 * worldserver.death.SicknessLevel
 * worldserver.death.corpsereclaimdelay.PvP
 * worldserver.death.corpsereclaimdelay.PvE
 * worldserver.death.bones.World
 * worldserver.death.bones.BattlegroundOrArena
 * worldserver.die.command.Mode
 * worldserver.stats.limits.Enable
 * worldserver.stats.limits.Dodge
 * worldserver.stats.limits.Parry
 * worldserver.stats.limits.Block
 * worldserver.stats.limits.Crit
 * worldserver.autobroadcast.On
 * worldserver.autobroadcast.Center
 * worldserver.autobroadcast.Timer
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * worldserver.wintergrasp.Enable
 * worldserver.wintergrasp.PlayerMax
 * worldserver.wintergrasp.PlayerMin
 * worldserver.wintergrasp.PlayerMinLvl
 * worldserver.wintergrasp.BattleTimer
 * worldserver.wintergrasp.NoBattleTimer
 * worldserver.wintergrasp.CrashRestartTimer
 * worldserver.tolbarad.Enable
 * worldserver.tolbarad.PlayerMax
 * worldserver.tolbarad.PlayerMin
 * worldserver.tolbarad.PlayerMinLvl
 * worldserver.tolbarad.BattleTimer
 * worldserver.tolbarad.BonusTime
 * worldserver.tolbarad.NoBattleTimer
 * worldserver.tolbarad.CrashRestartTimer
 * worldserver.arena.MaxRatingDifference
 * worldserver.arena.RatingDiscardTimer
 * worldserver.arena.RatedUpdateTimer
 * worldserver.arena.AutoDistributePoints
 * worldserver.arena.AutoDistributeInterval
 * worldserver.arena.queueannouncer.Enable
 * worldserver.arena.arenaseason.ID
 * worldserver.arena.arenaseason.InProgress
 * worldserver.arena.ArenaStartRating
 * worldserver.arena.ArenaStartPersonalRating
 * worldserver.arena.ArenaStartMatchmakerRating
 * worldserver.arena.ArenaWinRatingModifier1
 * worldserver.arena.ArenaWinRatingModifier2
 * worldserver.arena.ArenaLoseRatingModifier
 * worldserver.arena.ArenaMatchmakerRatingModifier
 * worldserver.arenalog.ExtendedInfo
 * worldserver.network.Threads
 * worldserver.network.OutKBuff
 * worldserver.network.OutUBuff
 * worldserver.network.TcpNodelay
 * worldserver.console.Enable
 * worldserver.ra.Enable
 * worldserver.ra.IP
 * worldserver.ra.Port
 * worldserver.ra.MinLevel
 * worldserver.soap.Enabled
 * worldserver.soap.IP
 * worldserver.soap.Port
 * worldserver.chardelete.Method
 * worldserver.chardelete.MinLevel
 * worldserver.chardelete.deathknight.MinLevel
 * worldserver.chardelete.demonhunter.MinLevel
 * worldserver.chardelete.KeepDays
 * worldserver.playerstart.AllReputation
 * worldserver.playerstart.AllSpells
 * worldserver.playerstart.MapsExplored
 * worldserver.playerstart.String
 * worldserver.xp.boost.Daymask
 * worldserver.xp.boost.Rate
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * worldserver.levelreq.Trade
 * worldserver.levelreq.Auction
 * worldserver.levelreq.Mail
 * worldserver.playerdump.DisallowPaths
 * worldserver.playerdump.DisallowOverwrite

 * worldserver.nograyaggro.Above
 * worldserver.nograyaggro.Below
 * <p>
 * worldserver.appender.Console
 * worldserver.appender.Server
 * worldserver.appender.GM
 * worldserver.appender.DBErrors
 * worldserver.logger.root
 * worldserver.logger.server
 * worldserver.logger.commands.gm
 * worldserver.logger.scripts.hotswap
 * worldserver.logger.sql.sql
 * worldserver.logger.sql.updates
 * worldserver.logger.mmaps
 * worldserver.log.async.Enable
 * worldserver.allow.ip.based.action.Logging
 * worldserver.currency.ResetInterval
 * worldserver.currency.ResetDay
 * worldserver.currency.ResetHour
 * worldserver.packetspoof.Policy
 * worldserver.packetspoof.BanMode
 * worldserver.packetspoof.BanDuration
 * worldserver.metric.Enable
 * worldserver.metric.Interval
 * worldserver.metric.ConnectionInfo
 * worldserver.metric.OverallStatusInterval
 * worldserver.pvp.factionbalance.LevelCheckDiff
 * worldserver.pvp.factionbalance.Pct5
 * worldserver.pvp.factionbalance.Pct10
 * worldserver.pvp.factionbalance.Pct20
 * worldserver.load.Locales
 */
@AllArgsConstructor
public class WorldSetting {

    public Locale dbcLocale;
    public boolean dbcEnforceItemAttributes;

    public int realmID;
    public Path dataDir;
    public Path logsDir;
    public int maxPingTime;
    public int worldServerPort;
    public int instanceServerPort;
    public String bindIP;
    public int threadPool;
    public String cmakeCommand;
    public Path buildDirectory;
    public Path sourceDirectory;
    public Path mySQLExecutable;
    public Path ipLocationFile;
    public int useProcessors;
    public int processPriority;
    public int realmsStateUpdateDelay;
    public byte compression;
    public int playerLimit;
    public int maxOverSpeedPings;
    public int gridUnload;
    public int baseMapLoadAllGrids;
    public int instanceMapLoadAllGrids;
    public int battlegroundMapLoadAllGrids;
    public int socketTimeOutTime;
    public int socketTimeOutTimeActive;
    public int sessionAddDelay;
    public int gridCleanUpDelay;
    public int minWorldUpdateTime;
    public int mapUpdateInterval;
    public int changeWeatherInterval;
    public int playerSaveInterval;
    public int disconnectToleranceInterval;
    public boolean detectPosCollision;
    public boolean checkGameObjectLoS;
    public int updateUptimeInterval;
    public int maxCoreStuckTime;
    public int addonChannel;
    public int cleanCharacterDB;
    public int persistentCharacterCleanFlags;
    public int pidFile;
    public int packetLogFile;
    public int gameType;
    public int realmZone;
    public int strictPlayerNames;
    public int strictCharterNames;
    public int strictPetNames;
    public int declinedNames;
    public Expansion expansion;
    public int minPlayerName;
    public int minCharterName;
    public int minPetName;
    public int maxWhoListReturns;
    public int charactersPerAccount;
    public int charactersPerRealm;
    public int skipCinematics;
    public int maxPlayerLevel;
    public int minDualSpecLevel;
    public int startPlayerLevel;
    public int startDeathKnightPlayerLevel;
    public int startDemonHunterPlayerLevel;
    public int startEvokerPlayerLevel;
    public int startAlliedRacePlayerLevel;
    public int startPlayerMoney;
    public int disableWaterBreath;
    public int allFlightPaths;
    public int instantFlightPaths;
    public int activateWeather;
    public int castUnstuck;
    public int instancesResetAnnounce;
    public int maxPrimaryTradeSkill;
    public int minPetitionSigns;
    public int maxGroupXPDistance;
    public int maxRecruitAFriendBonusDistance;
    public int minQuestScaledXPRatio;
    public int minCreatureScaledXPRatio;
    public int minDiscoveredScaledXPRatio;
    public int mailDeliveryDelay;
    public int cleanOldMailTime;
    public int offhandCheckAtSpellUnlearn;
    public int clientCacheVersion;
    public int beepAtStart;
    public int flashAtStart;
    public String motd;
    public int accountInstancesPerHour;
    public int birthdayTime;
    public int cacheDataQueries;
    public int allowLoggingIPAddressesInDatabase;
    public int tOTPMasterSecret;
    public int talentsInspecting;
    public int threatRadius;
    public int creatureFamilyFleeAssistanceRadius;
    public int creatureFamilyAssistanceRadius;
    public int creatureFamilyAssistanceDelay;
    public int creatureFamilyFleeDelay;
    public int worldBossLevelDiff;
    public int monsterSight;
    public int chatFakeMessagePreventing;
    public int partyLevelReq;
    public int preserveCustomChannels;
    public int preserveCustomChannelInterval;
    public int preserveCustomChannelDuration;
    public int partyRaidWarnings;
    public int honorPointsAfterDuel;
    public int resetDuelCooldowns;
    public int resetDuelHealthMana;
    public int noResetTalentsCost;
    public int showKickInWorld;
    public int showMuteInWorld;
    public int showBanInWorld;
    public int preventRenameCharacterOnCustomization;


    public int chatFloodMessageCount;
    public int chatFloodMessageDelay;
    public int chatFloodAddonMessageCount;
    public int chatFloodAddonMessageDelay;
    public int chatFloodMuteTime;
    public int pvpTokenEnable;
    public int pvpTokenMapAllowType;
    public int pvpTokenItemID;
    public int pvpTokenItemCount;


    public boolean instanceIgnoreLevel;
    public boolean instanceIgnoreRaid;
    public boolean instanceUnloadDelay;


    public boolean allowtwosideInteractionCalendar;
    public boolean allowtwosideInteractionChannel;
    public boolean allowtwosideInteractionGroup;
    public boolean allowtwosideInteractionGuild;
    public boolean allowtwosideInteractionAuction;
    public boolean allowtwosideTrade;


    public int creaturePickPocketRefillDelay;
    public int creatureMovingStopTimeForPlayer;
    public int creatureRegenHPCannotReachTargetInRaid;
    public boolean creatureCheckInvalidPosition;

    public boolean calculateCreatureZoneAreaData;
    public boolean calculateGameObjectZoneAreaData;


    public boolean gameObjectCheckInvalidPosition;
    public boolean lootEnableAELoot;
    public boolean blackMarketEnabled;
    public boolean blackMarketMaxAuctions;
    public boolean blackMarketUpdatePeriod;

    public GmSetting gm;
    public QuestSetting quest;
    public VisibilitySetting visibility;
    public RateSetting rate;
    public AuctionHouseBotSetting auctionHouse;
    public MapSetting map;
    public BattlegroundSetting battleground;
    public RespawnSetting respawn;

    public int getConfigMaxSkillValue() {
        int lvl = maxPlayerLevel;
        return lvl > 60 ? 300 + ((lvl - 60) * 75) / 10 : lvl * 5;
    }
}