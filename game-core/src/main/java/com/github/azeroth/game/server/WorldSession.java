package game;


import com.github.azeroth.game.domain.condition.DisableType;
import com.github.azeroth.game.LoginQueryHolder;
import com.github.azeroth.game.account.RBACData;
import com.github.azeroth.game.ai.PetAI;
import com.github.azeroth.game.arena.ArenaTeam;
import com.github.azeroth.game.battleground.Battleground;
import com.github.azeroth.game.battleground.BattlegroundManager;
import com.github.azeroth.game.battleground.BattlegroundQueueTypeId;
import com.github.azeroth.game.battleground.GroupQueueInfo;
import com.github.azeroth.game.battlepay.*;
import com.github.azeroth.game.battlepet.BattlePetMgr;
import com.github.azeroth.game.blackmarket.BlackMarketEntry;
import com.github.azeroth.game.blackmarket.BlackMarketTemplate;
import com.github.azeroth.game.chat.*;
import com.github.azeroth.game.dungeonfinding.*;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.creature.minion;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemDynamicFieldGems;
import com.github.azeroth.game.entity.item.ItemEnchantmentManager;
import com.github.azeroth.game.entity.item.ItemPosCount;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.*;
import com.github.azeroth.game.entity.player.model.TradeData;
import com.github.azeroth.game.entity.taxi.TaxiPathGraph;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.entity.unit.UnitActionBarEntry;
import com.github.azeroth.game.entity.unit.declinedName;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.guild.Guild;
import com.github.azeroth.game.loot.AELootResult;
import com.github.azeroth.game.loot.Loot;
import com.github.azeroth.game.loot.LootStorage;
import com.github.azeroth.game.mail.MailDraft;
import com.github.azeroth.game.mail.MailReceiver;
import com.github.azeroth.game.mail.MailSender;
import com.github.azeroth.game.map.InstanceLock;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.generator.FlightPathMovementGenerator;
import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.bpay.*;
import com.github.azeroth.game.networking.packet.mythicplus.MythicPlusSeasonData;
import com.github.azeroth.game.networking.packet.quest.*;
import com.github.azeroth.game.networking.packet.trade.TradeStatusPkt;
import com.github.azeroth.game.listener.interfaces.iconversation.IConversationOnConversationLineStarted;
import com.github.azeroth.game.listener.interfaces.iitem.IItemOnUse;
import com.github.azeroth.game.listener.interfaces.iplayer.*;
import com.github.azeroth.game.listener.interfaces.iquest.IQuestOnAckAutoAccept;
import com.github.azeroth.game.listener.interfaces.iquest.IQuestOnQuestStatusChange;
import com.github.azeroth.game.domain.player.PlayerLoginQueryLoad;
import com.github.azeroth.game.spell.AuraRemoveMode;
import com.github.azeroth.game.spell.CastSpellExtraArgs;
import com.github.azeroth.game.spell.Spell;
import com.github.azeroth.game.spell.SpellCastTargets;
import com.github.azeroth.game.supportsystem.BugTicket;
import com.github.azeroth.game.supportsystem.ComplaintTicket;
import com.github.azeroth.game.supportsystem.SuggestionTicket;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class WorldSession implements Closeable {
    private final ArrayList<ObjectGuid> legitCharacters = new ArrayList<>();
    private final WorldSocket[] socket = new WorldSocket[ConnectionType.max.getValue()];
    private final String address;
    private final int accountId;
    private final String accountName;
    private final int battlenetAccountId;
    private final Expansion accountExpansion;
    private final Expansion expansion;
    private final Expansion configuredExpansion;
    private final String os;
    private final DosProtection antiDos;
    private final Locale sessionDbcLocale;
    private final Locale sessionDbLocaleIndex;
    private final AccountData[] accountData = new AccountData[AccountDataTypes.max.getValue()];
    private final int[] tutorials = new int[SharedConst.MaxAccountTutorialValues];
    private final HashMap<Integer, Byte> realmCharacterCounts = new HashMap<Integer, Byte>();
    private final HashMap<Integer, tangible.Action1Param<Google.Protobuf.CodedInputStream>> battlenetResponseCallbacks = new HashMap<Integer, tangible.Action1Param<Google.Protobuf.CodedInputStream>>();
    private final ArrayList<String> registeredAddonPrefixes = new ArrayList<>();
    private final int recruiterId;
    private final boolean isRecruiter;
    private final ActionBlock<WorldPacket> recvQueue;
    private final ConcurrentQueue<WorldPacket> threadUnsafe = new ConcurrentQueue<WorldPacket>();
    private final ConcurrentQueue<WorldPacket> inPlaceQueue = new ConcurrentQueue<WorldPacket>();
    private final ConcurrentQueue<WorldPacket> threadSafeQueue = new ConcurrentQueue<WorldPacket>();
    private final CircularBuffer<Tuple<Long, Integer>> timeSyncClockDeltaQueue = new CircularBuffer<Tuple<Long, Integer>>(6); // first member: clockDelta. Second member: latency of the packet exchange that was used to compute that clockDelta.
    private final HashMap<Integer, Integer> pendingTimeSyncRequests = new HashMap<Integer, Integer>(); // key: counter. value: server time when packet with that counter was sent.
    private final CollectionMgr collectionMgr;
    private final BattlePetMgr battlePetMgr;
    private final BattlepayManager battlePayMgr;
    private final AsyncCallbackProcessor<QueryCallback> queryProcessor = new AsyncCallbackProcessor<QueryCallback>();
    private final AsyncCallbackProcessor<TransactionCallback> transactionCallbacks = new AsyncCallbackProcessor<TransactionCallback>();
    private final AsyncCallbackProcessor<ISqlCallback> queryHolderProcessor = new AsyncCallbackProcessor<ISqlCallback>();
    private final cancellationTokenSource cancellationToken = new cancellationTokenSource();
    private final autoResetEvent asyncMessageQueueSemaphore = new autoResetEvent(false);
    public long muteTime;
    private long guidLow;
    private Player player;
    private AccountTypes security = AccountTypes.values()[0];
    private int expireTime;
    private boolean forceExit;
    private Warden warden; // Remains NULL if Warden system is not enabled by config
    private long logoutTime;
    private boolean inQueue;
    private ObjectGuid playerLoading = ObjectGuid.EMPTY; // code processed in LoginPlayer
    private boolean playerLogout; // code processed in LogoutPlayer
    private boolean playerRecentlyLogout;
    private boolean playerSave;
    private int latency;
    private TutorialsFlag tutorialsChanged = TutorialsFlag.values()[0];
    private Array<Byte> realmListSecret = new Array<Byte>(32);
    private int battlenetRequestToken;
    private boolean filterAddonMessages;
    private long timeOutTime;
    private RBACData rbacData;
    private long timeSyncClockDelta;
    private int timeSyncNextCounter;
    private int timeSyncTimer;
    private connectToKey instanceConnectKey = new connectToKey();
    // Packets cooldown
    private long calendarEventCreationCooldown;
    private commandHandler commandHandler;

    public WorldSession(int id, String name, int battlenetAccountId, WorldSocket sock, AccountTypes sec, Expansion expansion, long mute_time, String os, Locale locale, int recruiter, boolean isARecruiter) {
        muteTime = mute_time;
        antiDos = new DosProtection(this);
        _socket[ConnectionType.realm.getValue()] = sock;
        security = sec;
        accountId = id;
        accountName = name;
        battlenetAccountId = battlenetAccountId;
        configuredExpansion = ConfigMgr.<Integer>GetDefaultValue("Player.OverrideExpansion", -1) == -1 ? expansion.LevelCurrent : expansion.forValue(ConfigMgr.<Integer>GetDefaultValue("Player.OverrideExpansion", -1));
        accountExpansion = expansion.LevelCurrent == _configuredExpansion ? expansion : configuredExpansion;
        expansion = expansion.forValue(Math.min((byte) expansion.getValue(), WorldConfig.getIntValue(WorldCfg.expansion)));
        os = os;
        sessionDbcLocale = global.getWorldMgr().getAvailableDbcLocale(locale);
        sessionDbLocaleIndex = locale;
        recruiterId = recruiter;
        isRecruiter = isARecruiter;
        expireTime = 60000; // 1 min after socket loss, session is deleted
        battlePetMgr = new BattlePetMgr(this);
        collectionMgr = new CollectionMgr(this);
        battlePayMgr = new BattlepayManager(this);
        setCommandHandler(new commandHandler(this));

        recvQueue = new (ProcessQueue, new ExecutionDataflowBlockOptions() {
            MaxDegreeOfParallelism =10,EnsureOrdered =true,CancellationToken =cancellationToken.Token
        });

        Task.run(this::ProcessInPlace, cancellationToken.token);

        address = sock.GetRemoteIpAddress().address.toString();
        resetTimeOutTime(false);
        DB.Login.execute("UPDATE account SET online = 1 WHERE id = {0};", getAccountId()); // One-time query
    }

    private static void setAcceptTradeMode(TradeData myTrade, TradeData hisTrade, Item[] myItems, Item[] hisItems) {
        myTrade.setInAcceptProcess(true);
        hisTrade.setInAcceptProcess(true);

        // store items in local list and set 'in-trade' flag
        for (byte i = 0; i < TradeSlots.count.getValue(); ++i) {
            var item = myTrade.getItem(TradeSlots.forValue(i));

            if (item) {
                Log.outDebug(LogFilter.Network, "player trade item {0} bag: {1} slot: {2}", item.getGUID().toString(), item.getBagSlot(), item.getSlot());
                //Can return null
                myItems[i] = item;
                myItems[i].setInTrade();
            }

            item = hisTrade.getItem(TradeSlots.forValue(i));

            if (item) {
                Log.outDebug(LogFilter.Network, "partner trade item {0} bag: {1} slot: {2}", item.getGUID().toString(), item.getBagSlot(), item.getSlot());
                hisItems[i] = item;
                hisItems[i].setInTrade();
            }
        }
    }

    private static void clearAcceptTradeMode(TradeData myTrade, TradeData hisTrade) {
        myTrade.setInAcceptProcess(false);
        hisTrade.setInAcceptProcess(false);
    }

    private static void clearAcceptTradeMode(Item[] myItems, Item[] hisItems) {
        // clear 'in-trade' flag
        for (byte i = 0; i < TradeSlots.count.getValue(); ++i) {
            if (myItems[i]) {
                myItems[i].setInTrade(false);
            }

            if (hisItems[i]) {
                hisItems[i].setInTrade(false);
            }
        }
    }


    private void handleAdventureJournalOpenQuest(AdventureJournalOpenQuest openQuest) {
        var uiDisplay = global.getDB2Mgr().GetUiDisplayForClass(player.getClass());

        if (uiDisplay != null) {
            if (!player.meetPlayerCondition(uiDisplay.AdvGuidePlayerConditionID)) {
                return;
            }
        }

        var adventureJournal = CliDB.AdventureJournalStorage.get(openQuest.adventureJournalID);

        if (adventureJournal == null) {
            return;
        }

        if (!player.meetPlayerCondition(adventureJournal.playerConditionID)) {
            return;
        }

        var quest = global.getObjectMgr().getQuestTemplate(adventureJournal.questID);

        if (quest == null) {
            return;
        }

        if (player.canTakeQuest(quest, true)) {
            player.getPlayerTalkClass().sendQuestGiverQuestDetails(quest, player.getGUID(), true, false);
        }
    }


    private void handleAdventureJournalUpdateSuggestions(AdventureJournalUpdateSuggestions updateSuggestions) {
        var uiDisplay = global.getDB2Mgr().GetUiDisplayForClass(player.getClass());

        if (uiDisplay != null) {
            if (!player.meetPlayerCondition(uiDisplay.AdvGuidePlayerConditionID)) {
                return;
            }
        }

        AdventureJournalDataResponse response = new AdventureJournalDataResponse();
        response.onLevelUp = updateSuggestions.onLevelUp;

        for (var adventureJournal : CliDB.AdventureJournalStorage.values()) {
            if (player.meetPlayerCondition(adventureJournal.playerConditionID)) {
                AdventureJournalEntry adventureJournalData = new AdventureJournalEntry();
                adventureJournalData.adventureJournalID = (int) adventureJournal.id;
                adventureJournalData.priority = adventureJournal.PriorityMax;
                response.adventureJournalDatas.add(adventureJournalData);
            }
        }

        sendPacket(response);
    }


    private void handleAdventureMapStartQuest(AdventureMapStartQuest startQuest) {
        var quest = global.getObjectMgr().getQuestTemplate(startQuest.questID);

        if (quest == null) {
            return;
        }

        var adventureMapPOI = CliDB.AdventureMapPOIStorage.values().FirstOrDefault(adventureMap ->
        {
            return adventureMap.questID == startQuest.questID && player.meetPlayerCondition(adventureMap.playerConditionID);
        });

        if (adventureMapPOI == null) {
            return;
        }

        if (player.canTakeQuest(quest, true)) {
            player.addQuestAndCheckCompletion(quest, player);
        }
    }


    private void handleArtifactAddPower(ArtifactAddPower artifactAddPower) {
        if (!player.getGameObjectIfCanInteractWith(artifactAddPower.forgeGUID, GameObjectTypes.itemForge)) {
            return;
        }

        var artifact = player.getItemByGuid(artifactAddPower.artifactGUID);

        if (!artifact || artifact.isArtifactDisabled()) {
            return;
        }

        var currentArtifactTier = artifact.getModifier(ItemModifier.ArtifactTier);

        long xpCost = 0;
        var cost = CliDB.ArtifactLevelXPGameTable.GetRow(artifact.getTotalPurchasedArtifactPowers() + 1);

        if (cost != null) {
            xpCost = (long) (currentArtifactTier == PlayerConst.MaxArtifactTier ? cost.XP2 : cost.XP);
        }

        if (xpCost > artifact.getItemData().artifactXP) {
            return;
        }

        if (artifactAddPower.powerChoices.isEmpty()) {
            return;
        }

        var artifactPower = artifact.getArtifactPower(artifactAddPower.powerChoices.get(0).artifactPowerID);

        if (artifactPower == null) {
            return;
        }

        var artifactPowerEntry = CliDB.ArtifactPowerStorage.get(artifactPower.artifactPowerId);

        if (artifactPowerEntry == null) {
            return;
        }

        if (artifactPowerEntry.tier > currentArtifactTier) {
            return;
        }

        int maxRank = artifactPowerEntry.MaxPurchasableRank;

        if (artifactPowerEntry.tier < currentArtifactTier) {
            if (artifactPowerEntry.flags.hasFlag(ArtifactPowerFlag.Final)) {
                maxRank = 1;
            } else if (artifactPowerEntry.flags.hasFlag(ArtifactPowerFlag.MaxRankWithTier)) {
                maxRank += currentArtifactTier - artifactPowerEntry.tier;
            }
        }

        if (artifactAddPower.powerChoices.get(0).rank != artifactPower.purchasedRank + 1 || artifactAddPower.powerChoices.get(0).rank > maxRank) {
            return;
        }

        if (!artifactPowerEntry.flags.hasFlag(ArtifactPowerFlag.NoLinkRequired)) {
            var artifactPowerLinks = global.getDB2Mgr().GetArtifactPowerLinks(artifactPower.artifactPowerId);

            if (artifactPowerLinks != null) {
                var hasAnyLink = false;

                for (var artifactPowerLinkId : artifactPowerLinks) {
                    var artifactPowerLink = CliDB.ArtifactPowerStorage.get(artifactPowerLinkId);

                    if (artifactPowerLink == null) {
                        continue;
                    }

                    var artifactPowerLinkLearned = artifact.getArtifactPower(artifactPowerLinkId);

                    if (artifactPowerLinkLearned == null) {
                        continue;
                    }

                    if (artifactPowerLinkLearned.purchasedRank >= artifactPowerLink.MaxPurchasableRank) {
                        hasAnyLink = true;

                        break;
                    }
                }

                if (!hasAnyLink) {
                    return;
                }
            }
        }

        var artifactPowerRank = global.getDB2Mgr().GetArtifactPowerRank(artifactPower.artifactPowerId, (byte) (artifactPower.currentRankWithBonus + 1 - 1)); // need data for next rank, but -1 because of how db2 data is structured

        if (artifactPowerRank == null) {
            return;
        }

        artifact.setArtifactPower(artifactPower.artifactPowerId, (byte) (artifactPower.purchasedRank + 1), (byte) (artifactPower.currentRankWithBonus + 1));

        if (artifact.isEquipped()) {
            player.applyArtifactPowerRank(artifact, artifactPowerRank, true);

            for (var power : artifact.getItemData().artifactPowers) {
                var scaledArtifactPowerEntry = CliDB.ArtifactPowerStorage.get(power.artifactPowerId);

                if (!scaledArtifactPowerEntry.flags.hasFlag(ArtifactPowerFlag.ScalesWithNumPowers)) {
                    continue;
                }

                var scaledArtifactPowerRank = global.getDB2Mgr().GetArtifactPowerRank(scaledArtifactPowerEntry.id, (byte) 0);

                if (scaledArtifactPowerRank == null) {
                    continue;
                }

                artifact.setArtifactPower(power.artifactPowerId, power.purchasedRank, (byte) (power.currentRankWithBonus + 1));

                player.applyArtifactPowerRank(artifact, scaledArtifactPowerRank, false);
                player.applyArtifactPowerRank(artifact, scaledArtifactPowerRank, true);
            }
        }

        artifact.setArtifactXP(artifact.getItemData().ArtifactXP - xpCost);
        artifact.setState(ItemUpdateState.changed, player);

        var totalPurchasedArtifactPower = artifact.getTotalPurchasedArtifactPowers();
        int artifactTier = 0;

        for (var tier : CliDB.ArtifactTierStorage.values()) {
            if (artifactPowerEntry.flags.hasFlag(ArtifactPowerFlag.Final) && artifactPowerEntry.tier < PlayerConst.MaxArtifactTier) {
                artifactTier = artifactPowerEntry.tier + 1;

                break;
            }

            if (totalPurchasedArtifactPower < tier.MaxNumTraits) {
                artifactTier = tier.ArtifactTier;

                break;
            }
        }

        artifactTier = Math.max(artifactTier, currentArtifactTier);

        for (var i = currentArtifactTier; i <= artifactTier; ++i) {
            artifact.initArtifactPowers(artifact.getTemplate().getArtifactID(), (byte) i);
        }

        artifact.setModifier(ItemModifier.ArtifactTier, artifactTier);
    }


    private void handleArtifactSetAppearance(ArtifactSetAppearance artifactSetAppearance) {
        if (!player.getGameObjectIfCanInteractWith(artifactSetAppearance.forgeGUID, GameObjectTypes.itemForge)) {
            return;
        }

        var artifactAppearance = CliDB.ArtifactAppearanceStorage.get(artifactSetAppearance.artifactAppearanceID);

        if (artifactAppearance == null) {
            return;
        }

        var artifact = player.getItemByGuid(artifactSetAppearance.artifactGUID);

        if (!artifact) {
            return;
        }

        var artifactAppearanceSet = CliDB.ArtifactAppearanceSetStorage.get(artifactAppearance.ArtifactAppearanceSetID);

        if (artifactAppearanceSet == null || artifactAppearanceSet.ArtifactID != artifact.getTemplate().getArtifactID()) {
            return;
        }

        var playerCondition = CliDB.PlayerConditionStorage.get(artifactAppearance.UnlockPlayerConditionID);

        if (playerCondition != null) {
            if (!ConditionManager.isPlayerMeetingCondition(player, playerCondition)) {
                return;
            }
        }

        artifact.setAppearanceModId(artifactAppearance.ItemAppearanceModifierID);
        artifact.setModifier(ItemModifier.artifactAppearanceId, artifactAppearance.id);
        artifact.setState(ItemUpdateState.changed, player);
        var childItem = player.getChildItemByGuid(artifact.getChildItem());

        if (childItem) {
            childItem.setAppearanceModId(artifactAppearance.ItemAppearanceModifierID);
            childItem.setState(ItemUpdateState.changed, player);
        }

        if (artifact.isEquipped()) {
            // change weapon appearance
            player.setVisibleItemSlot(artifact.getSlot(), artifact);

            if (childItem) {
                player.setVisibleItemSlot(childItem.getSlot(), childItem);
            }

            // change druid form appearance
            if (artifactAppearance.OverrideShapeshiftDisplayID != 0 && artifactAppearance.OverrideShapeshiftFormID != 0 && player.getShapeshiftForm() == ShapeShiftForm.forValue(artifactAppearance.OverrideShapeshiftFormID)) {
                player.restoreDisplayId(player.isMounted());
            }
        }
    }


    private void handleConfirmArtifactRespec(ConfirmArtifactRespec confirmArtifactRespec) {
        if (!player.getNPCIfCanInteractWith(confirmArtifactRespec.npcGUID, NPCFlags.ArtifactPowerRespec, NPCFlags2.NONE)) {
            return;
        }

        var artifact = player.getItemByGuid(confirmArtifactRespec.artifactGUID);

        if (!artifact || artifact.isArtifactDisabled()) {
            return;
        }

        long xpCost = 0;
        var cost = CliDB.ArtifactLevelXPGameTable.GetRow(artifact.getTotalPurchasedArtifactPowers() + 1);

        if (cost != null) {
            xpCost = (long) (artifact.getModifier(ItemModifier.ArtifactTier) == 1 ? cost.XP2 : cost.XP);
        }

        if (xpCost > artifact.getItemData().artifactXP) {
            return;
        }

        var newAmount = artifact.getItemData().ArtifactXP - xpCost;

        for (int i = 0; i <= artifact.getTotalPurchasedArtifactPowers(); ++i) {
            var cost1 = CliDB.ArtifactLevelXPGameTable.GetRow(i);

            if (cost1 != null) {
                newAmount += (long) (artifact.getModifier(ItemModifier.ArtifactTier) == 1 ? cost1.XP2 : cost1.XP);
            }
        }

        for (var artifactPower : artifact.getItemData().artifactPowers) {
            var oldPurchasedRank = artifactPower.purchasedRank;

            if (oldPurchasedRank == 0) {
                continue;
            }

            artifact.setArtifactPower(artifactPower.artifactPowerId, (byte) (artifactPower.PurchasedRank - oldPurchasedRank), (byte) (artifactPower.CurrentRankWithBonus - oldPurchasedRank));

            if (artifact.isEquipped()) {
                var artifactPowerRank = global.getDB2Mgr().GetArtifactPowerRank(artifactPower.artifactPowerId, (byte) 0);

                if (artifactPowerRank != null) {
                    player.applyArtifactPowerRank(artifact, artifactPowerRank, false);
                }
            }
        }

        for (var power : artifact.getItemData().artifactPowers) {
            var scaledArtifactPowerEntry = CliDB.ArtifactPowerStorage.get(power.artifactPowerId);

            if (!scaledArtifactPowerEntry.flags.hasFlag(ArtifactPowerFlag.ScalesWithNumPowers)) {
                continue;
            }

            var scaledArtifactPowerRank = global.getDB2Mgr().GetArtifactPowerRank(scaledArtifactPowerEntry.id, (byte) 0);

            if (scaledArtifactPowerRank == null) {
                continue;
            }

            artifact.setArtifactPower(power.artifactPowerId, power.purchasedRank, (byte) 0);

            player.applyArtifactPowerRank(artifact, scaledArtifactPowerRank, false);
        }

        artifact.setArtifactXP(newAmount);
        artifact.setState(ItemUpdateState.changed, player);
    }

    public final void sendAuctionHello(ObjectGuid guid, Creature unit) {
        if (getPlayer().getLevel() < WorldConfig.getIntValue(WorldCfg.AuctionLevelReq)) {
            sendNotification(global.getObjectMgr().getSysMessage(SysMessage.AuctionReq), WorldConfig.getIntValue(WorldCfg.AuctionLevelReq));

            return;
        }

        var ahEntry = global.getAuctionHouseMgr().getAuctionHouseEntry(unit.getFaction());

        if (ahEntry == null) {
            return;
        }

        AuctionHelloResponse auctionHelloResponse = new AuctionHelloResponse();
        auctionHelloResponse.guid = guid;
        auctionHelloResponse.openForBusiness = true;
        sendPacket(auctionHelloResponse);
    }

    public final void sendAuctionCommandResult(int auctionId, AuctionCommand command, AuctionResult errorCode, Duration delayForNextAction) {
        sendAuctionCommandResult(auctionId, command, errorCode, delayForNextAction, 0);
    }

    public final void sendAuctionCommandResult(int auctionId, AuctionCommand command, AuctionResult errorCode, Duration delayForNextAction, InventoryResult bagError) {
        AuctionCommandResult auctionCommandResult = new AuctionCommandResult();
        auctionCommandResult.auctionID = auctionId;
        auctionCommandResult.command = command.getValue();
        auctionCommandResult.errorCode = errorCode.getValue();
        auctionCommandResult.bagResult = bagError.getValue();
        auctionCommandResult.desiredDelay = (int) delayForNextAction.TotalSeconds;
        sendPacket(auctionCommandResult);
    }

    public final void sendAuctionClosedNotification(AuctionPosting auction, float mailDelay, boolean sold) {
        AuctionClosedNotification packet = new AuctionClosedNotification();
        packet.info.initialize(auction);
        packet.proceedsMailDelay = mailDelay;
        packet.sold = sold;
        sendPacket(packet);
    }

    public final void sendAuctionOwnerBidNotification(AuctionPosting auction) {
        AuctionOwnerBidNotification packet = new AuctionOwnerBidNotification();
        packet.info.initialize(auction);
        packet.bidder = auction.bidder;
        packet.minIncrement = auction.calculateMinIncrement();
        sendPacket(packet);
    }


    private void handleAuctionBrowseQuery(AuctionBrowseQuery browseQuery) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, browseQuery.taintedBy != null);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(browseQuery.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (creature == null) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionListItems - %1$s not found or you can't interact with him.", browseQuery.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        Log.outDebug(LogFilter.Auctionhouse, String.format("Auctionhouse search (%1$s), searchedname: %2$s, levelmin: %3$s, levelmax: %4$s, filters: %5$s", browseQuery.auctioneer, browseQuery.name, browseQuery.minLevel, browseQuery.maxLevel, browseQuery.filters));

        AuctionSearchClassFilters classFilters = null;

        AuctionListBucketsResult listBucketsResult = new AuctionListBucketsResult();

        if (!browseQuery.itemClassFilters.isEmpty()) {
            classFilters = new AuctionSearchClassFilters();

            for (var classFilter : browseQuery.itemClassFilters) {
                if (!classFilter.subClassFilters.isEmpty()) {
                    for (var subClassFilter : classFilter.subClassFilters) {
                        if (classFilter.itemClass < itemClass.max.getValue()) {
                            classFilters.Classes[classFilter.ItemClass].subclassMask = AuctionSearchClassFilters.FilterType.forValue(classFilters.Classes[classFilter.ItemClass].subclassMask.getValue() | AuctionSearchClassFilters.FilterType.forValue(1 << subClassFilter.itemSubclass).getValue());

                            if (subClassFilter.itemSubclass < ItemConst.MaxItemSubclassTotal) {
                                classFilters.Classes[classFilter.ItemClass].InvTypes[subClassFilter.ItemSubclass] = subClassFilter.invTypeMask;
                            }
                        }
                    }
                } else {
                    classFilters.Classes[classFilter.ItemClass].subclassMask = AuctionSearchClassFilters.FilterType.SkipSubclass;
                }
            }
        }

        auctionHouse.buildListBuckets(listBucketsResult, player, browseQuery.name, browseQuery.minLevel, browseQuery.maxLevel, browseQuery.filters, classFilters, browseQuery.knownPets, browseQuery.knownPets.length, (byte) browseQuery.maxPetLevel, browseQuery.offset, browseQuery.sorts, browseQuery.sorts.size());

        listBucketsResult.browseMode = AuctionHouseBrowseMode.Search;
        listBucketsResult.desiredDelay = (int) throttle.delayUntilNext.TotalSeconds;
        sendPacket(listBucketsResult);
    }


    private void handleAuctionCancelCommoditiesPurchase(AuctionCancelCommoditiesPurchase cancelCommoditiesPurchase) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, cancelCommoditiesPurchase.taintedBy != null, AuctionCommand.PlaceBid);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(cancelCommoditiesPurchase.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (creature == null) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionListItems - %1$s not found or you can't interact with him.", cancelCommoditiesPurchase.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());
        auctionHouse.cancelCommodityQuote(player.getGUID());
    }


    private void handleAuctionConfirmCommoditiesPurchase(AuctionConfirmCommoditiesPurchase confirmCommoditiesPurchase) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, confirmCommoditiesPurchase.taintedBy != null, AuctionCommand.PlaceBid);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(confirmCommoditiesPurchase.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (creature == null) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionListItems - %1$s not found or you can't interact with him.", confirmCommoditiesPurchase.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        SQLTransaction trans = new SQLTransaction();

        if (auctionHouse.buyCommodity(trans, player, (int) confirmCommoditiesPurchase.itemID, confirmCommoditiesPurchase.quantity, throttle.delayUntilNext)) {
            var buyerGuid = player.getGUID();

            addTransactionCallback(DB.characters.AsyncCommitTransaction(trans)).AfterComplete(success ->
            {
                if (getPlayer() && Objects.equals(getPlayer().getGUID(), buyerGuid)) {
                    if (success) {
                        getPlayer().updateCriteria(CriteriaType.AuctionsWon, 1);
                        sendAuctionCommandResult(0, AuctionCommand.PlaceBid, AuctionResult.Ok, throttle.delayUntilNext);
                    } else {
                        sendAuctionCommandResult(0, AuctionCommand.PlaceBid, AuctionResult.CommodityPurchaseFailed, throttle.delayUntilNext);
                    }
                }
            });
        }
    }


    private void handleAuctionHello(AuctionHelloRequest hello) {
        var unit = getPlayer().getNPCIfCanInteractWith(hello.guid, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAuctionHelloOpcode - %1$s not found or you can't interact with him.", hello.guid));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        sendAuctionHello(hello.guid, unit);
    }


    private void handleAuctionListBiddedItems(AuctionListBiddedItems listBiddedItems) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, listBiddedItems.taintedBy != null);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(listBiddedItems.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (!creature) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAuctionListBidderItems - %1$s not found or you can't interact with him.", listBiddedItems.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        AuctionListBiddedItemsResult result = new AuctionListBiddedItemsResult();

        var player = getPlayer();
        auctionHouse.buildListBiddedItems(result, player, listBiddedItems.offset, listBiddedItems.sorts, listBiddedItems.sorts.size());
        result.desiredDelay = (int) throttle.delayUntilNext.TotalSeconds;
        sendPacket(result);
    }


    private void handleAuctionListBucketsByBucketKeys(AuctionListBucketsByBucketKeys listBucketsByBucketKeys) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, listBucketsByBucketKeys.taintedBy != null);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(listBucketsByBucketKeys.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (creature == null) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionListItems - %1$s not found or you can't interact with him.", listBucketsByBucketKeys.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        AuctionListBucketsResult listBucketsResult = new AuctionListBucketsResult();

        auctionHouse.buildListBuckets(listBucketsResult, player, listBucketsByBucketKeys.bucketKeys, listBucketsByBucketKeys.bucketKeys.size(), listBucketsByBucketKeys.sorts, listBucketsByBucketKeys.sorts.size());

        listBucketsResult.browseMode = AuctionHouseBrowseMode.SpecificKeys;
        listBucketsResult.desiredDelay = (int) throttle.delayUntilNext.TotalSeconds;
        sendPacket(listBucketsResult);
    }


    private void handleAuctionListItemsByBucketKey(AuctionListItemsByBucketKey listItemsByBucketKey) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, listItemsByBucketKey.taintedBy != null);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(listItemsByBucketKey.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (creature == null) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionListItemsByBucketKey - %1$s not found or you can't interact with him.", listItemsByBucketKey.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        AuctionListItemsResult listItemsResult = new AuctionListItemsResult();
        listItemsResult.desiredDelay = (int) throttle.delayUntilNext.TotalSeconds;
        listItemsResult.bucketKey = listItemsByBucketKey.bucketKey;
        var itemTemplate = global.getObjectMgr().getItemTemplate(listItemsByBucketKey.bucketKey.itemID);
        listItemsResult.listType = itemTemplate != null && itemTemplate.getMaxStackSize() > 1 ? AuctionHouseListType.Commodities : AuctionHouseListType.items;

        auctionHouse.buildListAuctionItems(listItemsResult, player, new AuctionsBucketKey(listItemsByBucketKey.bucketKey), listItemsByBucketKey.offset, listItemsByBucketKey.sorts, listItemsByBucketKey.sorts.size());

        sendPacket(listItemsResult);
    }


    private void handleAuctionListItemsByItemID(AuctionListItemsByItemID listItemsByItemID) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, listItemsByItemID.taintedBy != null);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(listItemsByItemID.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (creature == null) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionListItemsByItemID - %1$s not found or you can't interact with him.", listItemsByItemID.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        AuctionListItemsResult listItemsResult = new AuctionListItemsResult();
        listItemsResult.desiredDelay = (int) throttle.delayUntilNext.TotalSeconds;
        listItemsResult.bucketKey.itemID = listItemsByItemID.itemID;
        var itemTemplate = global.getObjectMgr().getItemTemplate(listItemsByItemID.itemID);
        listItemsResult.listType = itemTemplate != null && itemTemplate.getMaxStackSize() > 1 ? AuctionHouseListType.Commodities : AuctionHouseListType.items;

        auctionHouse.buildListAuctionItems(listItemsResult, player, listItemsByItemID.itemID, listItemsByItemID.offset, listItemsByItemID.sorts, listItemsByItemID.sorts.size());

        sendPacket(listItemsResult);
    }


    private void handleAuctionListOwnedItems(AuctionListOwnedItems listOwnedItems) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, listOwnedItems.taintedBy != null);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(listOwnedItems.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (!creature) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAuctionListOwnerItems - %1$s not found or you can't interact with him.", listOwnedItems.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        AuctionListOwnedItemsResult result = new AuctionListOwnedItemsResult();

        auctionHouse.buildListOwnedItems(result, player, listOwnedItems.offset, listOwnedItems.sorts, listOwnedItems.sorts.size());
        result.desiredDelay = (int) throttle.delayUntilNext.TotalSeconds;
        sendPacket(result);
    }


    private void handleAuctionPlaceBid(AuctionPlaceBid placeBid) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, placeBid.taintedBy != null, AuctionCommand.PlaceBid);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(placeBid.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (!creature) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAuctionPlaceBid - %1$s not found or you can't interact with him.", placeBid.auctioneer));

            return;
        }

        // auction house does not deal with copper
        if ((placeBid.BidAmount % MoneyConstants.Silver) != 0) {
            sendAuctionCommandResult(placeBid.auctionID, AuctionCommand.PlaceBid, AuctionResult.BidIncrement, throttle.delayUntilNext);

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        var auction = auctionHouse.getAuction(placeBid.auctionID);

        if (auction == null || auction.isCommodity()) {
            sendAuctionCommandResult(placeBid.auctionID, AuctionCommand.PlaceBid, AuctionResult.ItemNotFound, throttle.delayUntilNext);

            return;
        }

        var player = getPlayer();

        // check auction owner - cannot buy own auctions
        if (Objects.equals(auction.owner, player.getGUID()) || Objects.equals(auction.ownerAccount, getAccountGUID())) {
            sendAuctionCommandResult(placeBid.auctionID, AuctionCommand.PlaceBid, AuctionResult.BidOwn, throttle.delayUntilNext);

            return;
        }

        var canBid = auction.minBid != 0;
        var canBuyout = auction.buyoutOrUnitPrice != 0;

        // buyout attempt with wrong amount
        if (!canBid && placeBid.bidAmount != auction.buyoutOrUnitPrice) {
            sendAuctionCommandResult(placeBid.auctionID, AuctionCommand.PlaceBid, AuctionResult.BidIncrement, throttle.delayUntilNext);

            return;
        }

        var minBid = auction.bidAmount != 0 ? auction.bidAmount + auction.calculateMinIncrement() : auction.minBid;

        if (canBid && placeBid.bidAmount < minBid) {
            sendAuctionCommandResult(placeBid.auctionID, AuctionCommand.PlaceBid, AuctionResult.HigherBid, throttle.delayUntilNext);

            return;
        }

        SQLTransaction trans = new SQLTransaction();
        var priceToPay = placeBid.bidAmount;

        if (!auction.bidder.isEmpty()) {
            // return money to previous bidder
            if (ObjectGuid.opNotEquals(auction.bidder, player.getGUID())) {
                auctionHouse.sendAuctionOutbid(auction, player.getGUID(), placeBid.bidAmount, trans);
            } else {
                priceToPay = placeBid.BidAmount - auction.bidAmount;
            }
        }

        // check money
        if (!player.hasEnoughMoney(priceToPay)) {
            sendAuctionCommandResult(placeBid.auctionID, AuctionCommand.PlaceBid, AuctionResult.NotEnoughMoney, throttle.delayUntilNext);

            return;
        }

        player.modifyMoney(-(long) priceToPay);
        auction.bidder = player.getGUID();
        auction.bidAmount = placeBid.bidAmount;

        if (hasPermission(RBACPermissions.LogGmTrade)) {
            auction.serverFlags = AuctionPostingServerFlag.forValue(auction.serverFlags.getValue() | AuctionPostingServerFlag.GmLogBuyer.getValue());
        } else {
            auction.serverFlags = AuctionPostingServerFlag.forValue(auction.serverFlags.getValue() & ~AuctionPostingServerFlag.GmLogBuyer.getValue());
        }

        if (canBuyout && placeBid.bidAmount == auction.buyoutOrUnitPrice) {
            // buyout
            auctionHouse.sendAuctionWon(auction, player, trans);
            auctionHouse.sendAuctionSold(auction, null, trans);

            auctionHouse.removeAuction(trans, auction);
        } else {
            // place bid
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_AUCTION_BID);
            stmt.AddValue(0, auction.bidder.getCounter());
            stmt.AddValue(1, auction.bidAmount);
            stmt.AddValue(2, (byte) auction.serverFlags.getValue());
            stmt.AddValue(3, auction.id);
            trans.append(stmt);

            auction.bidderHistory.add(player.getGUID());

            if (auction.bidderHistory.contains(player.getGUID())) {
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_AUCTION_BIDDER);
                stmt.AddValue(0, auction.id);
                stmt.AddValue(1, player.getGUID().getCounter());
                trans.append(stmt);
            }

            // Not sure if we must send this now.
            var owner = global.getObjAccessor().findConnectedPlayer(auction.owner);

            if (owner != null) {
                owner.getSession().sendAuctionOwnerBidNotification(auction);
            }
        }

        player.saveInventoryAndGoldToDB(trans);

        addTransactionCallback(DB.characters.AsyncCommitTransaction(trans)).AfterComplete(success ->
        {
            if (getPlayer() && Objects.equals(getPlayer().getGUID(), player.getGUID())) {
                if (success) {
                    getPlayer().updateCriteria(CriteriaType.HighestAuctionBid, placeBid.bidAmount);
                    sendAuctionCommandResult(placeBid.auctionID, AuctionCommand.PlaceBid, AuctionResult.Ok, throttle.delayUntilNext);
                } else {
                    sendAuctionCommandResult(placeBid.auctionID, AuctionCommand.PlaceBid, AuctionResult.DatabaseError, throttle.delayUntilNext);
                }
            }
        });
    }


    private void handleAuctionRemoveItem(AuctionRemoveItem removeItem) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, removeItem.taintedBy != null, AuctionCommand.Cancel);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(removeItem.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (!creature) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAuctionRemoveItem - %1$s not found or you can't interact with him.", removeItem.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        var auction = auctionHouse.getAuction(removeItem.auctionID);
        var player = getPlayer();

        SQLTransaction trans = new SQLTransaction();

        if (auction != null && Objects.equals(auction.owner, player.getGUID())) {
            if (auction.bidder.isEmpty()) // If we have a bidder, we have to send him the money he paid
            {
                var cancelCost = MathUtil.CalculatePct(auction.bidAmount, 5);

                if (!player.hasEnoughMoney(cancelCost)) //player doesn't have enough money
                {
                    sendAuctionCommandResult(0, AuctionCommand.Cancel, AuctionResult.NotEnoughMoney, throttle.delayUntilNext);

                    return;
                }

                auctionHouse.sendAuctionCancelledToBidder(auction, trans);
                player.modifyMoney(-(long) cancelCost);
            }

            auctionHouse.sendAuctionRemoved(auction, player, trans);
        } else {
            sendAuctionCommandResult(0, AuctionCommand.Cancel, AuctionResult.DatabaseError, throttle.delayUntilNext);
            //this code isn't possible ... maybe there should be assert
            Log.outError(LogFilter.Network, String.format("CHEATER: %1$s tried to cancel auction (id: %2$s) of another player or auction is null", player.getGUID(), removeItem.auctionID));

            return;
        }

        // client bug - instead of removing auction in the UI, it only substracts 1 from visible count
        var auctionIdForClient = auction.isCommodity() ? 0 : auction.id;

        // Now remove the auction
        player.saveInventoryAndGoldToDB(trans);
        auctionHouse.removeAuction(trans, auction);

        addTransactionCallback(DB.characters.AsyncCommitTransaction(trans)).AfterComplete(success ->
        {
            if (getPlayer() && Objects.equals(getPlayer().getGUID(), player.getGUID())) {
                if (success) {
                    sendAuctionCommandResult(auctionIdForClient, AuctionCommand.Cancel, AuctionResult.Ok, throttle.delayUntilNext); //inform player, that auction is removed
                } else {
                    sendAuctionCommandResult(0, AuctionCommand.Cancel, AuctionResult.DatabaseError, throttle.delayUntilNext);
                }
            }
        });
    }


    private void handleReplicateItems(AuctionReplicateItems replicateItems) {
        var creature = getPlayer().getNPCIfCanInteractWith(replicateItems.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (!creature) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleReplicateItems - %1$s not found or you can't interact with him.", replicateItems.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        AuctionReplicateResponse response = new AuctionReplicateResponse();

        auctionHouse.buildReplicate(response, getPlayer(), replicateItems.changeNumberGlobal, replicateItems.changeNumberCursor, replicateItems.changeNumberTombstone, replicateItems.count);

        response.desiredDelay = WorldConfig.getUIntValue(WorldCfg.AuctionSearchDelay) * 5;
        response.result = 0;

        sendPacket(response);
    }


    private void handleAuctionRequestFavoriteList(AuctionRequestFavoriteList requestFavoriteList) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_FAVORITE_AUCTIONS);
        stmt.AddValue(0, player.getGUID().getCounter());

        getQueryProcessor().AddCallback(DB.characters.AsyncQuery(stmt)).WithCallback(favoriteAuctionResult ->
        {
            AuctionFavoriteList favoriteItems = new AuctionFavoriteList();

            if (!favoriteAuctionResult.isEmpty()) {
                do {
                    AuctionFavoriteInfo item = new auctionFavoriteInfo();
                    item.order = favoriteAuctionResult.<Integer>Read(0);
                    item.itemID = favoriteAuctionResult.<Integer>Read(1);
                    item.itemLevel = favoriteAuctionResult.<Integer>Read(2);
                    item.battlePetSpeciesID = favoriteAuctionResult.<Integer>Read(3);
                    item.suffixItemNameDescriptionID = favoriteAuctionResult.<Integer>Read(4);
                    favoriteItems.items.add(item);
                } while (favoriteAuctionResult.NextRow());
            }

            sendPacket(favoriteItems);
        });
    }


    private void handleAuctionSellCommodity(AuctionSellCommodity sellCommodity) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, sellCommodity.taintedBy != null, AuctionCommand.SellItem);

        if (throttle.throttled) {
            return;
        }

        if (sellCommodity.unitPrice == 0 || sellCommodity.unitPrice > PlayerConst.MaxMoneyAmount) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionSellItem - Player %1$s (%2$s) attempted to sell item with invalid price.", player.getName(), player.getGUID()));
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);

            return;
        }

        // auction house does not deal with copper
        if ((sellCommodity.UnitPrice % MoneyConstants.Silver) != 0) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);

            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(sellCommodity.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (creature == null) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionListItems - %1$s not found or you can't interact with him.", sellCommodity.auctioneer));

            return;
        }

        int houseId = 0;
        tangible.RefObject<Integer> tempRef_houseId = new tangible.RefObject<Integer>(houseId);
        var auctionHouseEntry = global.getAuctionHouseMgr().getAuctionHouseEntry(creature.getFaction(), tempRef_houseId);
        houseId = tempRef_houseId.refArgValue;

        if (auctionHouseEntry == null) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionSellItem - unit (%1$s) has wrong faction.", sellCommodity.auctioneer));

            return;
        }

        switch (sellCommodity.runTime) {
            case 1 * SharedConst.MinAuctionTime / time.Minute:
            case 2 * SharedConst.MinAuctionTime / time.Minute:
            case 4 * SharedConst.MinAuctionTime / time.Minute:
                break;
            default:
                sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.AuctionHouseBusy, throttle.delayUntilNext);

                return;
        }

        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        // find all items for sale
        long totalCount = 0;
        HashMap < ObjectGuid, (Item item,long useCount)>items2 = new HashMap<ObjectGuid,(Item item, long useCount)>();

        for (var itemForSale : sellCommodity.items) {
            var item = player.getItemByGuid(itemForSale.guid);

            if (!item) {
                sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.ItemNotFound, throttle.delayUntilNext);

                return;
            }

            if (item.getTemplate().getMaxStackSize() == 1) {
                // not commodity, must use different packet
                sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.ItemNotFound, throttle.delayUntilNext);

                return;
            }

            // verify that all items belong to the same bucket
            if (!items2.isEmpty() && AuctionsBucketKey.opNotEquals(AuctionsBucketKey.forItem(item), AuctionsBucketKey.forItem(items2.FirstOrDefault().value.Item1))) {
                sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.ItemNotFound, throttle.delayUntilNext);

                return;
            }

            if (global.getAuctionHouseMgr().getAItem(item.getGUID()) || !item.canBeTraded() || item.isNotEmptyBag() || item.getTemplate().hasFlag(ItemFlags.Conjured) || item.getItemData().expiration != 0 || item.getCount() < itemForSale.useCount) {
                sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);

                return;
            }

            var soldItem = items2.get(item.getGUID());
            soldItem.item = item;
            soldItem.useCount += itemForSale.useCount;
            items2.put(item.getGUID(), soldItem);

            if (item.getCount() < soldItem.useCount) {
                // check that we have enough of this item to sell
                sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.ItemNotFound, throttle.delayUntilNext);

                return;
            }

            totalCount += itemForSale.useCount;
        }

        if (totalCount == 0) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);

            return;
        }

        var auctionTime = duration.FromSeconds((long) durationofMinutes(sellCommodity.runTime).TotalSeconds * WorldConfig.getFloatValue(WorldCfg.RateAuctionTime));
        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        var deposit = global.getAuctionHouseMgr().getCommodityAuctionDeposit(items2.FirstOrDefault().value.item.template, durationofMinutes(sellCommodity.runTime), (int) totalCount);

        if (!player.hasEnoughMoney(deposit)) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.NotEnoughMoney, throttle.delayUntilNext);

            return;
        }

        var auctionId = global.getObjectMgr().generateAuctionID();
        AuctionPosting auction = new AuctionPosting();
        auction.id = auctionId;
        auction.owner = player.getGUID();
        auction.ownerAccount = getAccountGUID();
        auction.buyoutOrUnitPrice = sellCommodity.unitPrice;
        auction.deposit = deposit;
        auction.startTime = gameTime.GetSystemTime();
        auction.endTime = auction.startTime + auctionTime;

        // keep track of what was cloned to undo/modify counts later
        HashMap<item, item> clones = new HashMap<item, item>();

        for (var pair : items2.entrySet()) {
            Item itemForSale;

            if (pair.getValue().Item1.count != pair.getValue().item2) {
                itemForSale = pair.getValue().Item1.cloneItem((int) pair.getValue().item2, player);

                if (itemForSale == null) {
                    Log.outError(LogFilter.Network, String.format("CMSG_AUCTION_SELL_COMMODITY: Could not create clone of item %1$s", pair.getValue().Item1.entry));
                    sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);

                    return;
                }

                clones.put(pair.getValue().Item1, itemForSale);
            }
        }

        if (!global.getAuctionHouseMgr().pendingAuctionAdd(player, auctionHouse.getAuctionHouseId(), auction.id, auction.deposit)) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.NotEnoughMoney, throttle.delayUntilNext);

            return;
        }

		/*TC_LOG_INFO("network", "CMSG_AUCTION_SELL_COMMODITY: %s %s is selling item %s %s to auctioneer %s with count " UI64FMTD " with with unit price " UI64FMTD " and with time %u (in sec) in auctionhouse %u",
			player.getGUID().toString(), player.getName(), items2.begin().second.first.GetNameForLocaleIdx(sWorld.GetDefaultDbcLocale()),
			([&items2]()
	{
			std.stringstream ss;
			auto itr = items2.begin();
			ss << (itr++).first.toString();
			for (; itr != items2.end(); ++itr)
				ss << ',' << itr.first.toString();
			return ss.str();
		} ()),
	creature.getGUID().toString(), totalCount, sellCommodity.unitPrice, uint32(auctionTime.count()), auctionHouse.getAuctionHouseId());*/

        if (hasPermission(RBACPermissions.LogGmTrade)) {
            var logItem = items2.firstEntry().value.Item1;
            Log.outCommand(getAccountId(), String.format("GM %1$s (Account: %2$s) create auction: %3$s (Entry: %4$s Count: %5$s)", getPlayerName(), getAccountId(), logItem.getName(global.getWorldMgr().getDefaultDbcLocale()), logItem.entry, totalCount));
        }

        SQLTransaction trans = new SQLTransaction();

        for (var pair : items2.entrySet()) {
            var itemForSale = pair.getValue().Item1;
            var cloneItr = clones.get(pair.getValue().Item1);

            if (cloneItr != null) {
                var original = itemForSale;
                original.setCount(original.Count - (int) pair.getValue().item2);
                original.setState(ItemUpdateState.changed, player);
                player.itemRemovedQuestCheck(original.entry, (int) pair.getValue().item2);
                original.saveToDB(trans);

                itemForSale = cloneItr;
            } else {
                player.moveItemFromInventory(itemForSale.BagSlot, itemForSale.slot, true);
                itemForSale.deleteFromInventoryDB(trans);
            }

            itemForSale.saveToDB(trans);
            auction.items.add(itemForSale);
        }

        auctionHouse.addAuction(trans, auction);
        player.saveInventoryAndGoldToDB(trans);

        var auctionPlayerGuid = player.getGUID();

        addTransactionCallback(DB.characters.AsyncCommitTransaction(trans)).AfterComplete(success ->
        {
            if (getPlayer() && Objects.equals(getPlayer().getGUID(), auctionPlayerGuid)) {
                if (success) {
                    getPlayer().updateCriteria(CriteriaType.ItemsPostedAtAuction, 1);
                    sendAuctionCommandResult(auctionId, AuctionCommand.SellItem, AuctionResult.Ok, throttle.delayUntilNext);
                } else {
                    sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);
                }
            }
        });
    }


    private void handleAuctionSellItem(AuctionSellItem sellItem) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, sellItem.taintedBy != null, AuctionCommand.SellItem);

        if (throttle.throttled) {
            return;
        }

        if (sellItem.items.size() != 1 || sellItem.items.get(0).useCount != 1) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.ItemNotFound, throttle.delayUntilNext);

            return;
        }

        if (sellItem.minBid == 0 && sellItem.buyoutPrice == 0) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.NotEnoughMoney, throttle.delayUntilNext);

            return;
        }

        if (sellItem.minBid > PlayerConst.MaxMoneyAmount || sellItem.buyoutPrice > PlayerConst.MaxMoneyAmount) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionSellItem - Player %1$s (%2$s) attempted to sell item with higher price than max gold amount.", player.getName(), player.getGUID()));
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.Inventory, throttle.delayUntilNext, InventoryResult.TooMuchGold);

            return;
        }

        // auction house does not deal with copper
        if ((sellItem.MinBid % MoneyConstants.Silver) != 0 || (sellItem.BuyoutPrice % MoneyConstants.Silver) != 0) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);

            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(sellItem.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (!creature) {
            Log.outError(LogFilter.Network, "WORLD: HandleAuctionSellItem - unit (%s) not found or you can't interact with him.", sellItem.auctioneer.toString());

            return;
        }

        int houseId = 0;
        tangible.RefObject<Integer> tempRef_houseId = new tangible.RefObject<Integer>(houseId);
        var auctionHouseEntry = global.getAuctionHouseMgr().getAuctionHouseEntry(creature.getFaction(), tempRef_houseId);
        houseId = tempRef_houseId.refArgValue;

        if (auctionHouseEntry == null) {
            Log.outError(LogFilter.Network, "WORLD: HandleAuctionSellItem - unit (%s) has wrong faction.", sellItem.auctioneer.toString());

            return;
        }

        switch (sellItem.runTime) {
            case 1 * SharedConst.MinAuctionTime / time.Minute:
            case 2 * SharedConst.MinAuctionTime / time.Minute:
            case 4 * SharedConst.MinAuctionTime / time.Minute:
                break;
            default:
                sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.AuctionHouseBusy, throttle.delayUntilNext);

                return;
        }

        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var item = player.getItemByGuid(sellItem.items.get(0).guid);

        if (!item) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.ItemNotFound, throttle.delayUntilNext);

            return;
        }

        if (item.getTemplate().getMaxStackSize() > 1) {
            // commodity, must use different packet
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.ItemNotFound, throttle.delayUntilNext);

            return;
        }

        if (global.getAuctionHouseMgr().getAItem(item.getGUID()) || !item.canBeTraded() || item.isNotEmptyBag() || item.getTemplate().hasFlag(ItemFlags.Conjured) || item.getItemData().expiration != 0 || item.getCount() != 1) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);

            return;
        }

        var auctionTime = duration.FromSeconds((long) (durationofMinutes(sellItem.runTime).TotalSeconds * WorldConfig.getFloatValue(WorldCfg.RateAuctionTime)));
        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        var deposit = global.getAuctionHouseMgr().getItemAuctionDeposit(player, item, durationofMinutes(sellItem.runTime));

        if (!player.hasEnoughMoney(deposit)) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.NotEnoughMoney, throttle.delayUntilNext);

            return;
        }

        var auctionId = global.getObjectMgr().generateAuctionID();

        AuctionPosting auction = new AuctionPosting();
        auction.id = auctionId;
        auction.owner = player.getGUID();
        auction.ownerAccount = getAccountGUID();
        auction.minBid = sellItem.minBid;
        auction.buyoutOrUnitPrice = sellItem.buyoutPrice;
        auction.deposit = deposit;
        auction.bidAmount = sellItem.minBid;
        auction.startTime = gameTime.GetSystemTime();
        auction.endTime = auction.startTime + auctionTime;

        if (hasPermission(RBACPermissions.LogGmTrade)) {
            Log.outCommand(getAccountId(), String.format("GM %1$s (Account: %2$s) create auction: %3$s (Entry: %4$s Count: %5$s)", getPlayerName(), getAccountId(), item.getTemplate().getName(), item.getEntry(), item.getCount()));
        }

        auction.items.add(item);

        Log.outInfo(LogFilter.Network, String.format("CMSG_AuctionAction.SellItem: %1$s %2$s is selling item %3$s %4$s ", player.getGUID(), player.getName(), item.getGUID(), item.getTemplate().getName()) + String.format("to auctioneer %1$s with count %2$s with initial bid %3$s with buyout %4$s and with time %5$s ", creature.getGUID(), item.getCount(), sellItem.minBid, sellItem.buyoutPrice, auctionTime.TotalSeconds) + String.format("(in sec) in auctionhouse %1$s", auctionHouse.getAuctionHouseId()));

        // Add to pending auctions, or fail with insufficient funds error
        if (!global.getAuctionHouseMgr().pendingAuctionAdd(player, auctionHouse.getAuctionHouseId(), auctionId, auction.deposit)) {
            sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.NotEnoughMoney, throttle.delayUntilNext);

            return;
        }

        player.moveItemFromInventory(item.getBagSlot(), item.getSlot(), true);

        SQLTransaction trans = new SQLTransaction();
        item.deleteFromInventoryDB(trans);
        item.saveToDB(trans);

        auctionHouse.addAuction(trans, auction);
        player.saveInventoryAndGoldToDB(trans);

        var auctionPlayerGuid = player.getGUID();

        addTransactionCallback(DB.characters.AsyncCommitTransaction(trans)).AfterComplete(success ->
        {
            if (getPlayer() && Objects.equals(getPlayer().getGUID(), auctionPlayerGuid)) {
                if (success) {
                    getPlayer().updateCriteria(CriteriaType.ItemsPostedAtAuction, 1);
                    sendAuctionCommandResult(auctionId, AuctionCommand.SellItem, AuctionResult.Ok, throttle.delayUntilNext);
                } else {
                    sendAuctionCommandResult(0, AuctionCommand.SellItem, AuctionResult.DatabaseError, throttle.delayUntilNext);
                }
            }
        });
    }


    private void handleAuctionSetFavoriteItem(AuctionSetFavoriteItem setFavoriteItem) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, false);

        if (throttle.throttled) {
            return;
        }

        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHARACTER_FAVORITE_AUCTION);
        stmt.AddValue(0, player.getGUID().getCounter());
        stmt.AddValue(1, setFavoriteItem.item.order);
        trans.append(stmt);

        if (!setFavoriteItem.isNotFavorite) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHARACTER_FAVORITE_AUCTION);
            stmt.AddValue(0, player.getGUID().getCounter());
            stmt.AddValue(1, setFavoriteItem.item.order);
            stmt.AddValue(2, setFavoriteItem.item.itemID);
            stmt.AddValue(3, setFavoriteItem.item.itemLevel);
            stmt.AddValue(4, setFavoriteItem.item.battlePetSpeciesID);
            stmt.AddValue(5, setFavoriteItem.item.suffixItemNameDescriptionID);
            trans.append(stmt);
        }

        DB.characters.CommitTransaction(trans);
    }


    private void handleAuctionGetCommodityQuote(AuctionGetCommodityQuote getCommodityQuote) {
        var throttle = global.getAuctionHouseMgr().checkThrottle(player, getCommodityQuote.taintedBy != null, AuctionCommand.PlaceBid);

        if (throttle.throttled) {
            return;
        }

        var creature = getPlayer().getNPCIfCanInteractWith(getCommodityQuote.auctioneer, NPCFlags.auctioneer, NPCFlags2.NONE);

        if (!creature) {
            Log.outError(LogFilter.Network, String.format("WORLD: HandleAuctionStartCommoditiesPurchase - %1$s not found or you can't interact with him.", getCommodityQuote.auctioneer));

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var auctionHouse = global.getAuctionHouseMgr().getAuctionsMap(creature.getFaction());

        AuctionGetCommodityQuoteResult commodityQuoteResult = new AuctionGetCommodityQuoteResult();

        var quote = auctionHouse.createCommodityQuote(player, (int) getCommodityQuote.itemID, getCommodityQuote.quantity);

        if (quote != null) {
            commodityQuoteResult.totalPrice = quote.totalPrice;
            commodityQuoteResult.quantity = quote.quantity;
            commodityQuoteResult.quoteDuration = (int) (quote.ValidTo - gameTime.Now()).TotalMilliseconds;
        }

        commodityQuoteResult.itemID = getCommodityQuote.itemID;
        commodityQuoteResult.desiredDelay = (int) throttle.delayUntilNext.TotalSeconds;

        sendPacket(commodityQuoteResult);
    }

    public final void sendAuthResponse(BattlenetRpcErrorCode code, boolean queued) {
        sendAuthResponse(code, queued, 0);
    }

    public final void sendAuthResponse(BattlenetRpcErrorCode code, boolean queued, int queuePos) {
        AuthResponse response = new AuthResponse();
        response.result = code;

        if (code == BattlenetRpcErrorCode.Ok) {
            response.successInfo = new AuthResponse.AuthSuccessInfo();
            var forceRaceAndClass = ConfigMgr.GetDefaultValue("character.EnforceRaceAndClassExpansions", true);

            response.successInfo = new AuthResponse.AuthSuccessInfo();
            response.successInfo.activeExpansionLevel = !forceRaceAndClass ? (byte) getExpansion().Dragonflight.getValue() : (byte) getExpansion().getValue();
            response.successInfo.accountExpansionLevel = !forceRaceAndClass ? (byte) getExpansion().Dragonflight.getValue() : (byte) getAccountExpansion().getValue();
            response.successInfo.virtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
            response.successInfo.time = (int) gameTime.GetGameTime();

            var realm = global.getWorldMgr().getRealm();

            // Send current home realm. Also there is no need to send it later in realm queries.
            response.successInfo.virtualRealms.add(new virtualRealmInfo(realm.id.GetAddress(), true, false, realm.name, realm.NormalizedName));

            if (hasPermission(RBACPermissions.UseCharacterTemplates)) {
                for (var templ : global.getCharacterTemplateDataStorage().GetCharacterTemplates().values()) {
                    response.successInfo.templates.add(templ);
                }
            }

            response.successInfo.availableClasses = global.getObjectMgr().getClassExpansionRequirements();
        }

        if (queued) {
            AuthWaitInfo waitInfo = new authWaitInfo();
            waitInfo.waitCount = queuePos;
            response.waitInfo = waitInfo;
        }

        sendPacket(response);
    }

    public final void sendAuthWaitQueue(int position) {
        if (position != 0) {
            WaitQueueUpdate waitQueueUpdate = new WaitQueueUpdate();
            waitQueueUpdate.waitInfo.waitCount = position;
            waitQueueUpdate.waitInfo.waitTime = 0;
            waitQueueUpdate.waitInfo.hasFCM = false;
            sendPacket(waitQueueUpdate);
        } else {
            sendPacket(new WaitQueueFinish());
        }
    }

    public final void sendClientCacheVersion(int version) {
        ClientCacheVersion cache = new ClientCacheVersion();
        cache.cacheVersion = version;
        sendPacket(cache); //enabled it
    }

    public final void sendSetTimeZoneInformation() {
        // @todo: replace dummy values
        SetTimeZoneInformation packet = new SetTimeZoneInformation();
        packet.serverTimeTZ = "Europe/Paris";
        packet.gameTimeTZ = "Europe/Paris";
        packet.serverRegionalTZ = "Europe/Paris";

        sendPacket(packet); //enabled it
    }

    public final void sendFeatureSystemStatusGlueScreen() {
        FeatureSystemStatusGlueScreen features = new FeatureSystemStatusGlueScreen();
        features.bpayStoreAvailable = WorldConfig.getBoolValue(WorldCfg.FeatureSystemBpayStoreEnabled);
        features.bpayStoreDisabledByParentalControls = false;
        features.charUndeleteEnabled = WorldConfig.getBoolValue(WorldCfg.FeatureSystemCharacterUndeleteEnabled);
        features.bpayStoreEnabled = WorldConfig.getBoolValue(WorldCfg.FeatureSystemBpayStoreEnabled);
        features.maxCharactersPerRealm = WorldConfig.getIntValue(WorldCfg.CharactersPerRealm);
        features.minimumExpansionLevel = getExpansion().Classic.getValue();
        features.maximumExpansionLevel = WorldConfig.getIntValue(WorldCfg.expansion);

        var europaTicketConfig = new EuropaTicketConfig();
        europaTicketConfig.throttleState.maxTries = 10;
        europaTicketConfig.throttleState.perMilliseconds = 60000;
        europaTicketConfig.throttleState.tryCount = 1;
        europaTicketConfig.throttleState.lastResetTimeBeforeNow = 111111;
        europaTicketConfig.ticketsEnabled = WorldConfig.getBoolValue(WorldCfg.SupportTicketsEnabled);
        europaTicketConfig.bugsEnabled = WorldConfig.getBoolValue(WorldCfg.SupportBugsEnabled);
        europaTicketConfig.complaintsEnabled = WorldConfig.getBoolValue(WorldCfg.SupportComplaintsEnabled);
        europaTicketConfig.suggestionsEnabled = WorldConfig.getBoolValue(WorldCfg.SupportSuggestionsEnabled);

        features.europaTicketSystemStatus = europaTicketConfig;

        sendPacket(features);
    }

    public final void sendAzeriteRespecNPC(ObjectGuid npc) {
        NPCInteractionOpenResult npcInteraction = new NPCInteractionOpenResult();
        npcInteraction.npc = npc;
        npcInteraction.interactionType = PlayerInteractionType.AzeriteRespec;
        npcInteraction.success = true;
        sendPacket(npcInteraction);
    }


    private void handleAzeriteEssenceUnlockMilestone(AzeriteEssenceUnlockMilestone azeriteEssenceUnlockMilestone) {
        if (!azeriteItem.FindHeartForge(player)) {
            return;
        }

        var item = player.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

        if (!item) {
            return;
        }

        var azeriteItem = item.getAsAzeriteItem();

        if (!azeriteItem || !azeriteItem.CanUseEssences()) {
            return;
        }

        var milestonePower = CliDB.AzeriteItemMilestonePowerStorage.get(azeriteEssenceUnlockMilestone.AzeriteItemMilestonePowerID);

        if (milestonePower == null || milestonePower.requiredLevel > azeriteItem.getLevel()) {
            return;
        }

        // check that all previous milestones are unlocked
        for (var previousMilestone : global.getDB2Mgr().GetAzeriteItemMilestonePowers()) {
            if (previousMilestone == milestonePower) {
                break;
            }

            if (!azeriteItem.HasUnlockedEssenceMilestone(previousMilestone.id)) {
                return;
            }
        }

        azeriteItem.AddUnlockedEssenceMilestone(milestonePower.id);
        player.applyAzeriteItemMilestonePower(azeriteItem, milestonePower, true);
        azeriteItem.setState(ItemUpdateState.changed, player);
    }


    private void handleAzeriteEssenceActivateEssence(AzeriteEssenceActivateEssence azeriteEssenceActivateEssence) {
        ActivateEssenceFailed activateEssenceResult = new ActivateEssenceFailed();
        activateEssenceResult.azeriteEssenceID = azeriteEssenceActivateEssence.azeriteEssenceID;

        var item = player.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Equipment);

        if (item == null) {
            activateEssenceResult.reason = AzeriteEssenceActivateResult.NotEquipped;
            activateEssenceResult.slot = azeriteEssenceActivateEssence.slot;
            sendPacket(activateEssenceResult);

            return;
        }

        var azeriteItem = item.getAsAzeriteItem();

        if (azeriteItem == null || !azeriteItem.CanUseEssences()) {
            activateEssenceResult.reason = AzeriteEssenceActivateResult.ConditionFailed;
            sendPacket(activateEssenceResult);

            return;
        }

        if (azeriteEssenceActivateEssence.slot >= SharedConst.MaxAzeriteEssenceSlot || !azeriteItem.HasUnlockedEssenceSlot(azeriteEssenceActivateEssence.slot)) {
            activateEssenceResult.reason = AzeriteEssenceActivateResult.SlotLocked;
            sendPacket(activateEssenceResult);

            return;
        }

        var selectedEssences = azeriteItem.GetSelectedAzeriteEssences();

        // essence is already in that slot, nothing to do
        if (selectedEssences != null && selectedEssences.azeriteEssenceID.get(azeriteEssenceActivateEssence.slot) == azeriteEssenceActivateEssence.azeriteEssenceID) {
            return;
        }

        var rank = azeriteItem.GetEssenceRank(azeriteEssenceActivateEssence.azeriteEssenceID);

        if (rank == 0) {
            activateEssenceResult.reason = AzeriteEssenceActivateResult.EssenceNotUnlocked;
            activateEssenceResult.arg = azeriteEssenceActivateEssence.azeriteEssenceID;
            sendPacket(activateEssenceResult);

            return;
        }

        if (player.isInCombat()) {
            activateEssenceResult.reason = AzeriteEssenceActivateResult.AffectingCombat;
            activateEssenceResult.slot = azeriteEssenceActivateEssence.slot;
            sendPacket(activateEssenceResult);

            return;
        }

        if (player.isDead()) {
            activateEssenceResult.reason = AzeriteEssenceActivateResult.CantDoThatRightNow;
            activateEssenceResult.slot = azeriteEssenceActivateEssence.slot;
            sendPacket(activateEssenceResult);

            return;
        }

        if (!player.hasPlayerFlag(playerFlags.Resting) && !player.hasUnitFlag2(UnitFlag2.AllowChangingTalents)) {
            activateEssenceResult.reason = AzeriteEssenceActivateResult.NotInRestArea;
            activateEssenceResult.slot = azeriteEssenceActivateEssence.slot;
            sendPacket(activateEssenceResult);

            return;
        }

        if (selectedEssences != null) {
            // need to remove selected essence from another slot if selected
            var removeEssenceFromSlot = -1;

            for (var slot = 0; slot < SharedConst.MaxAzeriteEssenceSlot; ++slot) {
                if (azeriteEssenceActivateEssence.slot != slot && selectedEssences.azeriteEssenceID.get(slot) == azeriteEssenceActivateEssence.azeriteEssenceID) {
                    removeEssenceFromSlot = slot;
                }
            }

            // check cooldown of major essence slot
            if (selectedEssences.azeriteEssenceID.get(0) != 0 && (azeriteEssenceActivateEssence.slot == 0 || removeEssenceFromSlot == 0)) {
                for (int essenceRank = 1; essenceRank <= rank; ++essenceRank) {
                    var azeriteEssencePower = global.getDB2Mgr().GetAzeriteEssencePower(selectedEssences.azeriteEssenceID.get(0), essenceRank);

                    if (player.getSpellHistory().hasCooldown(azeriteEssencePower.MajorPowerDescription)) {
                        activateEssenceResult.reason = AzeriteEssenceActivateResult.CantRemoveEssence;
                        activateEssenceResult.arg = azeriteEssencePower.MajorPowerDescription;
                        activateEssenceResult.slot = azeriteEssenceActivateEssence.slot;
                        sendPacket(activateEssenceResult);

                        return;
                    }
                }
            }


            if (removeEssenceFromSlot != -1) {
                player.applyAzeriteEssence(azeriteItem, selectedEssences.azeriteEssenceID.get(removeEssenceFromSlot), SharedConst.MaxAzeriteEssenceRank, global.getDB2Mgr().GetAzeriteItemMilestonePower(removeEssenceFromSlot).type == AzeriteItemMilestoneType.MajorEssence.getValue(), false);

                azeriteItem.SetSelectedAzeriteEssence(removeEssenceFromSlot, 0);
            }

            if (selectedEssences.azeriteEssenceID.get(azeriteEssenceActivateEssence.slot) != 0) {
                player.applyAzeriteEssence(azeriteItem, selectedEssences.azeriteEssenceID.get(azeriteEssenceActivateEssence.slot), SharedConst.MaxAzeriteEssenceRank, global.getDB2Mgr().GetAzeriteItemMilestonePower(azeriteEssenceActivateEssence.slot).type == AzeriteItemMilestoneType.MajorEssence.getValue(), false);
            }
        } else {
            azeriteItem.CreateSelectedAzeriteEssences(player.getPrimarySpecialization());
        }

        azeriteItem.SetSelectedAzeriteEssence(azeriteEssenceActivateEssence.slot, azeriteEssenceActivateEssence.azeriteEssenceID);

        player.applyAzeriteEssence(azeriteItem, azeriteEssenceActivateEssence.azeriteEssenceID, rank, global.getDB2Mgr().GetAzeriteItemMilestonePower(azeriteEssenceActivateEssence.slot).type == AzeriteItemMilestoneType.MajorEssence.getValue(), true);

        azeriteItem.setState(ItemUpdateState.changed, player);
    }


    private void handleAzeriteEmpoweredItemViewed(AzeriteEmpoweredItemViewed azeriteEmpoweredItemViewed) {
        var item = player.getItemByGuid(azeriteEmpoweredItemViewed.itemGUID);

        if (item == null || !item.isAzeriteEmpoweredItem()) {
            return;
        }

        item.setItemFlag(ItemFieldFlags.AzeriteEmpoweredItemViewed);
        item.setState(ItemUpdateState.changed, player);
    }


    private void handleAzeriteEmpoweredItemSelectPower(AzeriteEmpoweredItemSelectPower azeriteEmpoweredItemSelectPower) {
        var item = player.getItemByPos(azeriteEmpoweredItemSelectPower.containerSlot, azeriteEmpoweredItemSelectPower.slot);

        if (item == null) {
            return;
        }

        var azeritePower = CliDB.AzeritePowerStorage.get(azeriteEmpoweredItemSelectPower.AzeritePowerID);

        if (azeritePower == null) {
            return;
        }

        var azeriteEmpoweredItem = item.getAsAzeriteEmpoweredItem();

        if (azeriteEmpoweredItem == null) {
            return;
        }

        // Validate tier
        var actualTier = azeriteEmpoweredItem.GetTierForAzeritePower(player.getClass(), azeriteEmpoweredItemSelectPower.AzeritePowerID);

        if (azeriteEmpoweredItemSelectPower.tier > SharedConst.MaxAzeriteEmpoweredTier || azeriteEmpoweredItemSelectPower.tier != actualTier) {
            return;
        }

        int azeriteLevel = 0;
        var heartOfAzeroth = player.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

        if (heartOfAzeroth == null) {
            return;
        }

        var azeriteItem = heartOfAzeroth.getAsAzeriteItem();

        if (azeriteItem != null) {
            azeriteLevel = azeriteItem.GetEffectiveLevel();
        }

        // Check required heart of azeroth level
        if (azeriteLevel < azeriteEmpoweredItem.GetRequiredAzeriteLevelForTier((int) actualTier)) {
            return;
        }

        // tiers are ordered backwards, you first select the highest one
        for (var i = actualTier + 1; i < azeriteEmpoweredItem.GetMaxAzeritePowerTier(); ++i) {
            if (azeriteEmpoweredItem.GetSelectedAzeritePower(i) == 0) {
                return;
            }
        }

        var activateAzeritePower = azeriteEmpoweredItem.isEquipped() && heartOfAzeroth.isEquipped();

        if (azeritePower.ItemBonusListID != 0 && activateAzeritePower) {
            player._ApplyItemMods(azeriteEmpoweredItem, azeriteEmpoweredItem.getSlot(), false);
        }

        azeriteEmpoweredItem.SetSelectedAzeritePower(actualTier, azeriteEmpoweredItemSelectPower.AzeritePowerID);

        if (activateAzeritePower) {
            // apply all item mods when azerite power grants a bonus, item level changes and that affects stats and auras that scale with item level
            if (azeritePower.ItemBonusListID != 0) {
                player._ApplyItemMods(azeriteEmpoweredItem, azeriteEmpoweredItem.getSlot(), true);
            } else {
                player.applyAzeritePower(azeriteEmpoweredItem, azeritePower, true);
            }
        }

        azeriteEmpoweredItem.setState(ItemUpdateState.changed, player);
    }

    public final void sendShowBank(ObjectGuid guid) {
        player.getPlayerTalkClass().getInteractionData().reset();
        player.getPlayerTalkClass().getInteractionData().setSourceGuid(guid);
        NPCInteractionOpenResult npcInteraction = new NPCInteractionOpenResult();
        npcInteraction.npc = guid;
        npcInteraction.interactionType = PlayerInteractionType.banker;
        npcInteraction.success = true;
        sendPacket(npcInteraction);
    }


    private void handleAutoBankItem(AutoBankItem packet) {
        if (!canUseBank()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAutoBankItemOpcode - %1$s not found or you can't interact with him.", player.getPlayerTalkClass().getInteractionData().getSourceGuid()));

            return;
        }

        var item = getPlayer().getItemByPos(packet.bag, packet.slot);

        if (!item) {
            return;
        }

        ArrayList<ItemPosCount> dest = new ArrayList<>();
        var msg = getPlayer().canBankItem(ItemConst.NullBag, ItemConst.NullSlot, dest, item, false);

        if (msg != InventoryResult.Ok) {
            getPlayer().sendEquipError(msg, item);

            return;
        }

        if (dest.size() == 1 && dest.get(0).pos == item.getPos()) {
            getPlayer().sendEquipError(InventoryResult.CantSwap, item);

            return;
        }

        getPlayer().removeItem(packet.bag, packet.slot, true);
        getPlayer().itemRemovedQuestCheck(item.getEntry(), item.getCount());
        getPlayer().bankItem(dest, item, true);
    }


    private void handleBankerActivate(Hello packet) {
        var unit = getPlayer().getNPCIfCanInteractWith(packet.unit, NPCFlags.banker, NPCFlags2.NONE);

        if (!unit) {
            Log.outError(LogFilter.Network, "HandleBankerActivate: {0} not found or you can not interact with him.", packet.unit.toString());

            return;
        }

        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        sendShowBank(packet.unit);
    }


    private void handleAutoStoreBankItem(AutoStoreBankItem packet) {
        if (!canUseBank()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAutoBankItemOpcode - %1$s not found or you can't interact with him.", player.getPlayerTalkClass().getInteractionData().getSourceGuid()));

            return;
        }

        var item = getPlayer().getItemByPos(packet.bag, packet.slot);

        if (!item) {
            return;
        }

        if (player.isBankPos(packet.bag, packet.slot)) // moving from bank to inventory
        {
            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = getPlayer().canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, dest, item, false);

            if (msg != InventoryResult.Ok) {
                getPlayer().sendEquipError(msg, item);

                return;
            }

            getPlayer().removeItem(packet.bag, packet.slot, true);
            var storedItem = getPlayer().storeItem(dest, item, true);

            if (storedItem) {
                getPlayer().itemAddedQuestCheck(storedItem.getEntry(), storedItem.getCount());
            }
        } else // moving from inventory to bank
        {
            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = getPlayer().canBankItem(ItemConst.NullBag, ItemConst.NullSlot, dest, item, false);

            if (msg != InventoryResult.Ok) {
                getPlayer().sendEquipError(msg, item);

                return;
            }

            getPlayer().removeItem(packet.bag, packet.slot, true);
            getPlayer().bankItem(dest, item, true);
        }
    }


    private void handleBuyBankSlot(BuyBankSlot packet) {
        if (!canUseBank(packet.guid)) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBuyBankSlot - {0} not found or you can't interact with him.", packet.guid.toString());

            return;
        }

        int slot = getPlayer().getBankBagSlotCount();
        // next slot
        ++slot;

        var slotEntry = CliDB.BankBagSlotPricesStorage.get(slot);

        if (slotEntry == null) {
            return;
        }

        var price = slotEntry.cost;

        if (!getPlayer().hasEnoughMoney(price)) {
            return;
        }

        getPlayer().setBankBagSlotCount((byte) slot);
        getPlayer().modifyMoney(-price);
        getPlayer().updateCriteria(CriteriaType.BankSlotsPurchased);
    }


    private void handleBuyReagentBank(ReagentBank reagentBank) {
        if (!canUseBank(reagentBank.banker)) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleBuyReagentBankOpcode - %1$s not found or you can't interact with him.", reagentBank.banker));

            return;
        }

        if (player.isReagentBankUnlocked()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleBuyReagentBankOpcode - player (%1$s, name: %2$s) tried to unlock reagent bank a 2nd time.", player.getGUID(), player.getName()));

            return;
        }

        long price = 100 * MoneyConstants.gold;

        if (!player.hasEnoughMoney(price)) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleBuyReagentBankOpcode - player (%1$s, name: %2$s) without enough gold.", player.getGUID(), player.getName()));

            return;
        }

        player.modifyMoney(-price);
        player.unlockReagentBank();
    }


    private void handleReagentBankDeposit(ReagentBank reagentBank) {
        if (!canUseBank(reagentBank.banker)) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleReagentBankDepositOpcode - %1$s not found or you can't interact with him.", reagentBank.banker));

            return;
        }

        if (!player.isReagentBankUnlocked()) {
            player.sendEquipError(InventoryResult.ReagentBankLocked);

            return;
        }

        // query all reagents from player's inventory
        var anyDeposited = false;

        for (var item : player.getCraftingReagentItemsToDeposit()) {
            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = player.canBankItem(ItemConst.NullBag, ItemConst.NullSlot, dest, item, false, true, true);

            if (msg != InventoryResult.Ok) {
                if (msg != InventoryResult.ReagentBankFull || !anyDeposited) {
                    player.sendEquipError(msg, item);
                }

                break;
            }

            if (dest.size() == 1 && dest.get(0).pos == item.getPos()) {
                player.sendEquipError(InventoryResult.CantSwap, item);

                continue;
            }

            // store reagent
            player.removeItem(item.getBagSlot(), item.getSlot(), true);
            player.bankItem(dest, item, true);
            anyDeposited = true;
        }
    }


    private void handleAutoBankReagent(AutoBankReagent autoBankReagent) {
        if (!canUseBank()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAutoBankReagentOpcode - %1$s not found or you can't interact with him.", player.getPlayerTalkClass().getInteractionData().getSourceGuid()));

            return;
        }

        if (!player.isReagentBankUnlocked()) {
            player.sendEquipError(InventoryResult.ReagentBankLocked);

            return;
        }

        var item = player.getItemByPos(autoBankReagent.packSlot, autoBankReagent.slot);

        if (!item) {
            return;
        }

        ArrayList<ItemPosCount> dest = new ArrayList<>();
        var msg = player.canBankItem(ItemConst.NullBag, ItemConst.NullSlot, dest, item, false, true, true);

        if (msg != InventoryResult.Ok) {
            player.sendEquipError(msg, item);

            return;
        }

        if (dest.size() == 1 && dest.get(0).pos == item.getPos()) {
            player.sendEquipError(InventoryResult.CantSwap, item);

            return;
        }

        player.removeItem(autoBankReagent.packSlot, autoBankReagent.slot, true);
        player.bankItem(dest, item, true);
    }


    private void handleAutoStoreBankReagent(AutoStoreBankReagent autoStoreBankReagent) {
        if (!canUseBank()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleAutoBankReagentOpcode - %1$s not found or you can't interact with him.", player.getPlayerTalkClass().getInteractionData().getSourceGuid()));

            return;
        }

        if (!player.isReagentBankUnlocked()) {
            player.sendEquipError(InventoryResult.ReagentBankLocked);

            return;
        }

        var pItem = player.getItemByPos(autoStoreBankReagent.slot, autoStoreBankReagent.packSlot);

        if (!pItem) {
            return;
        }

        if (player.isReagentBankPos(autoStoreBankReagent.slot, autoStoreBankReagent.packSlot)) {
            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = player.canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, dest, pItem, false);

            if (msg != InventoryResult.Ok) {
                player.sendEquipError(msg, pItem);

                return;
            }

            player.removeItem(autoStoreBankReagent.slot, autoStoreBankReagent.packSlot, true);
            player.storeItem(dest, pItem, true);
        } else {
            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = player.canBankItem(ItemConst.NullBag, ItemConst.NullSlot, dest, pItem, false, true, true);

            if (msg != InventoryResult.Ok) {
                player.sendEquipError(msg, pItem);

                return;
            }

            player.removeItem(autoStoreBankReagent.slot, autoStoreBankReagent.packSlot, true);
            player.bankItem(dest, pItem, true);
        }
    }


    private void handleBattlemasterHello(Hello hello) {
        var unit = getPlayer().getNPCIfCanInteractWith(hello.unit, NPCFlags.BattleMaster, NPCFlags2.NONE);

        if (!unit) {
            return;
        }

        // Stop the npc if moving
        var pause = unit.getMovementTemplate().getInteractionPauseTimer();

        if (pause != 0) {
            unit.pauseMovement(pause);
        }

        unit.setHomePosition(unit.getLocation());

        var bgTypeId = global.getBattlegroundMgr().getBattleMasterBG(unit.getEntry());

        if (!getPlayer().getBgAccessByLevel(bgTypeId)) {
            // temp, must be gossip message...
            sendNotification(SysMessage.YourBgLevelReqError);

            return;
        }

        global.getBattlegroundMgr().sendBattlegroundList(getPlayer(), hello.unit, bgTypeId);
    }


    private void handleBattlemasterJoin(BattlemasterJoin battlemasterJoin) {
        var isPremade = false;

        if (battlemasterJoin.queueIDs.isEmpty()) {
            Log.outError(LogFilter.Network, String.format("Battleground: no bgtype received. possible cheater? %1$s", player.getGUID()));

            return;
        }

        var bgQueueTypeId = BattlegroundQueueTypeId.fromPacked(battlemasterJoin.queueIDs.get(0));

        if (!global.getBattlegroundMgr().isValidQueueId(bgQueueTypeId)) {
            Log.outError(LogFilter.Network, String.format("Battleground: invalid bg queue %1$s received. possible cheater? %2$s", bgQueueTypeId, player.getGUID()));

            return;
        }

        var battlemasterListEntry = CliDB.BattlemasterListStorage.get(bgQueueTypeId.battlemasterListId);

        if (global.getDisableMgr().isDisabledFor(DisableType.Battleground, bgQueueTypeId.battlemasterListId, null) || battlemasterListEntry.flags.hasFlag(BattlemasterListFlags.disabled)) {
            getPlayer().sendSysMessage(SysMessage.BgDisabled);

            return;
        }

        var bgTypeId = BattlegroundTypeId.forValue(bgQueueTypeId.battlemasterListId);

        // ignore if player is already in BG
        if (getPlayer().getInBattleground()) {
            return;
        }

        // get bg instance or bg template if instance not found
        var bg = global.getBattlegroundMgr().getBattlegroundTemplate(bgTypeId);

        if (!bg) {
            return;
        }

        // expected bracket entry
        var bracketEntry = global.getDB2Mgr().GetBattlegroundBracketByLevel(bg.getMapId(), getPlayer().getLevel());

        if (bracketEntry == null) {
            return;
        }

        var err = GroupJoinBattlegroundResult.NONE;

        var grp = player.getGroup();


//		Team getQueueTeam()
//			{
//				// mercenary applies only to unrated battlegrounds
//				if (!bg.isRated() && !bg.isArena())
//				{
//					if (player.hasAura(BattlegroundConst.SpellMercenaryContractHorde))
//						return Team.Horde;
//
//					if (player.hasAura(BattlegroundConst.SpellMercenaryContractAlliance))
//						return Team.ALLIANCE;
//				}
//
//				return player.team;
//			}

        BattlefieldStatusFailed battlefieldStatusFailed;

        // check queue conditions
        if (grp == null) {
            if (getPlayer().isUsingLfg()) {
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed, bgQueueTypeId, getPlayer(), 0, GroupJoinBattlegroundResult.LfgCantUseBattleground);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            // check RBAC permissions
            if (!getPlayer().canJoinToBattleground(bg)) {
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed2 = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed2, bgQueueTypeId, getPlayer(), 0, GroupJoinBattlegroundResult.JoinTimedOut);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed2.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            // check Deserter debuff
            if (getPlayer().isDeserter()) {
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed3 = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed3, bgQueueTypeId, getPlayer(), 0, GroupJoinBattlegroundResult.Deserters);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed3.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            var isInRandomBgQueue = player.inBattlegroundQueueForBattlegroundQueueType(global.getBattlegroundMgr().BGQueueTypeId((short) BattlegroundTypeId.RB.getValue(), BattlegroundQueueIdType.Battleground, false, 0)) || player.inBattlegroundQueueForBattlegroundQueueType(global.getBattlegroundMgr().BGQueueTypeId((short) BattlegroundTypeId.RandomEpic.getValue(), BattlegroundQueueIdType.Battleground, false, 0));

            if (bgTypeId != BattlegroundTypeId.RB && bgTypeId != BattlegroundTypeId.RandomEpic && isInRandomBgQueue) {
                // player is already in random queue
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed4 = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed4, bgQueueTypeId, getPlayer(), 0, GroupJoinBattlegroundResult.InRandomBg);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed4.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            if (player.inBattlegroundQueue(true) && !isInRandomBgQueue && (bgTypeId == BattlegroundTypeId.RB || bgTypeId == BattlegroundTypeId.RandomEpic)) {
                // player is already in queue, can't start random queue
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed5 = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed5, bgQueueTypeId, getPlayer(), 0, GroupJoinBattlegroundResult.InNonRandomBg);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed5.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            // check if already in queue
            if (getPlayer().getBattlegroundQueueIndex(bgQueueTypeId) < SharedConst.MaxPlayerBGQueues) {
                return; // player is already in this queue
            }

            // check if has free queue slots
            if (!getPlayer().getHasFreeBattlegroundQueueId()) {
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed6 = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed6, bgQueueTypeId, getPlayer(), 0, GroupJoinBattlegroundResult.TooManyQueues);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed6.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            // check Freeze debuff
            if (player.hasAura(9454)) {
                return;
            }

            var bgQueue = global.getBattlegroundMgr().getBattlegroundQueue(bgQueueTypeId);
            var ginfo = bgQueue.addGroup(getPlayer(), null, getQueueTeam(), bracketEntry, isPremade, 0, 0);

            var avgTime = bgQueue.getAverageQueueWaitTime(ginfo, bracketEntry.getBracketId());
            var queueSlot = getPlayer().addBattlegroundQueueId(bgQueueTypeId);

            BattlefieldStatusQueued battlefieldStatusQueued;
            tangible.OutObject<BattlefieldStatusQueued> tempOut_battlefieldStatusQueued = new tangible.OutObject<BattlefieldStatusQueued>();
            global.getBattlegroundMgr().buildBattlegroundStatusQueued(tempOut_battlefieldStatusQueued, bg, getPlayer(), queueSlot, ginfo.joinTime, bgQueueTypeId, avgTime, 0, false);
            battlefieldStatusQueued = tempOut_battlefieldStatusQueued.outArgValue;
            sendPacket(battlefieldStatusQueued);

            Log.outDebug(LogFilter.Battleground, String.format("Battleground: player joined queue for bg queue %1$s, %2$s, NAME %3$s", bgQueueTypeId, player.getGUID(), player.getName()));
        } else {
            if (ObjectGuid.opNotEquals(grp.getLeaderGUID(), getPlayer().getGUID())) {
                return;
            }

            ObjectGuid errorGuid = ObjectGuid.EMPTY;
            tangible.OutObject<ObjectGuid> tempOut_errorGuid = new tangible.OutObject<ObjectGuid>();
            err = grp.canJoinBattlegroundQueue(bg, bgQueueTypeId, 0, bg.getMaxPlayersPerTeam(), false, 0, tempOut_errorGuid);
            errorGuid = tempOut_errorGuid.outArgValue;
            isPremade = (grp.getMembersCount() >= bg.getMinPlayersPerTeam());

            var bgQueue = global.getBattlegroundMgr().getBattlegroundQueue(bgQueueTypeId);
            GroupQueueInfo ginfo = null;
            int avgTime = 0;

            if (err == 0) {
                Log.outDebug(LogFilter.Battleground, "Battleground: the following players are joining as group:");
                ginfo = bgQueue.addGroup(getPlayer(), grp, getQueueTeam(), bracketEntry, isPremade, 0, 0);
                avgTime = bgQueue.getAverageQueueWaitTime(ginfo, bracketEntry.getBracketId());
            }

            for (var refe = grp.getFirstMember(); refe != null; refe = refe.next()) {
                var member = refe.getSource();

                if (!member) {
                    continue; // this should never happen
                }

                if (err != 0) {
                    BattlefieldStatusFailed battlefieldStatus;
                    tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatus = new tangible.OutObject<BattlefieldStatusFailed>();
                    global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatus, bgQueueTypeId, getPlayer(), 0, err, errorGuid);
                    battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                    member.sendPacket(battlefieldStatus);

                    continue;
                }

                // add to queue
                var queueSlot = member.addBattlegroundQueueId(bgQueueTypeId);

                BattlefieldStatusQueued battlefieldStatusQueued;
                tangible.OutObject<BattlefieldStatusQueued> tempOut_battlefieldStatusQueued2 = new tangible.OutObject<BattlefieldStatusQueued>();
                global.getBattlegroundMgr().buildBattlegroundStatusQueued(tempOut_battlefieldStatusQueued2, bg, member, queueSlot, ginfo.joinTime, bgQueueTypeId, avgTime, 0, true);
                battlefieldStatusQueued = tempOut_battlefieldStatusQueued2.outArgValue;
                member.sendPacket(battlefieldStatusQueued);
                Log.outDebug(LogFilter.Battleground, String.format("Battleground: player joined queue for bg queue %1$s, %2$s, NAME %3$s", bgQueueTypeId, member.getGUID(), member.getName()));
            }

            Log.outDebug(LogFilter.Battleground, "Battleground: group end");
        }

        global.getBattlegroundMgr().scheduleQueueUpdate(0, bgQueueTypeId, bracketEntry.getBracketId());
    }


    private void handlePVPLogData(PVPLogDataRequest packet) {
        var bg = getPlayer().getBattleground();

        if (!bg) {
            return;
        }

        // Prevent players from sending BuildPvpLogDataPacket in an arena except for when sent in Battleground.EndBattleground.
        if (bg.isArena()) {
            return;
        }

        PVPMatchStatisticsMessage pvpMatchStatistics = new PVPMatchStatisticsMessage();
        tangible.OutObject<PVPMatchStatistics> tempOut_Data = new tangible.OutObject<PVPMatchStatistics>();
        bg.buildPvPLogDataPacket(tempOut_Data);
        pvpMatchStatistics.data = tempOut_Data.outArgValue;
        sendPacket(pvpMatchStatistics);
    }


    private void handleBattlefieldList(BattlefieldListRequest battlefieldList) {
        var bl = CliDB.BattlemasterListStorage.get(battlefieldList.listID);

        if (bl == null) {
            Log.outDebug(LogFilter.Battleground, "BattlegroundHandler: invalid bgtype ({0}) with player (Name: {1}, GUID: {2}) received.", battlefieldList.listID, getPlayer().getName(), getPlayer().getGUID().toString());

            return;
        }

        global.getBattlegroundMgr().sendBattlegroundList(getPlayer(), ObjectGuid.Empty, BattlegroundTypeId.forValue(battlefieldList.listID));
    }


    private void handleBattleFieldPort(BattlefieldPort battlefieldPort) {
        if (!getPlayer().inBattlegroundQueue()) {
            Log.outDebug(LogFilter.Battleground, "CMSG_BATTLEFIELD_PORT {0} Slot: {1}, Unk: {2}, Time: {3}, AcceptedInvite: {4}. Player not in queue!", getPlayerInfo(), battlefieldPort.ticket.id, battlefieldPort.ticket.type, battlefieldPort.ticket.time, battlefieldPort.acceptedInvite);

            return;
        }

        var bgQueueTypeId = getPlayer().getBattlegroundQueueTypeId(battlefieldPort.ticket.id);

        if (BattlegroundQueueTypeId.opEquals(bgQueueTypeId, null)) {
            Log.outDebug(LogFilter.Battleground, "CMSG_BATTLEFIELD_PORT {0} Slot: {1}, Unk: {2}, Time: {3}, AcceptedInvite: {4}. Invalid queueSlot!", getPlayerInfo(), battlefieldPort.ticket.id, battlefieldPort.ticket.type, battlefieldPort.ticket.time, battlefieldPort.acceptedInvite);

            return;
        }

        var bgQueue = global.getBattlegroundMgr().getBattlegroundQueue(bgQueueTypeId);

        //we must use temporary variable, because GroupQueueInfo pointer can be deleted in BattlegroundQueue.removePlayer() function
        GroupQueueInfo ginfo;
        tangible.OutObject<GroupQueueInfo> tempOut_ginfo = new tangible.OutObject<GroupQueueInfo>();
        if (!bgQueue.getPlayerGroupInfoData(getPlayer().getGUID(), tempOut_ginfo)) {
            ginfo = tempOut_ginfo.outArgValue;
            Log.outDebug(LogFilter.Battleground, "CMSG_BATTLEFIELD_PORT {0} Slot: {1}, Unk: {2}, Time: {3}, AcceptedInvite: {4}. Player not in queue (No player Group info)!", getPlayerInfo(), battlefieldPort.ticket.id, battlefieldPort.ticket.type, battlefieldPort.ticket.time, battlefieldPort.acceptedInvite);

            return;
        } else {
            ginfo = tempOut_ginfo.outArgValue;
        }

        // if action == 1, then instanceId is required
        if (ginfo.isInvitedToBGInstanceGUID == 0 && battlefieldPort.acceptedInvite) {
            Log.outDebug(LogFilter.Battleground, "CMSG_BATTLEFIELD_PORT {0} Slot: {1}, Unk: {2}, Time: {3}, AcceptedInvite: {4}. Player is not invited to any bg!", getPlayerInfo(), battlefieldPort.ticket.id, battlefieldPort.ticket.type, battlefieldPort.ticket.time, battlefieldPort.acceptedInvite);

            return;
        }

        var bgTypeId = BattlegroundTypeId.forValue(bgQueueTypeId.battlemasterListId);
        // BGTemplateId returns Battleground_AA when it is arena queue.
        // Do instance id search as there is no AA bg instances.
        var bg = global.getBattlegroundMgr().getBattleground(ginfo.isInvitedToBGInstanceGUID, bgTypeId == BattlegroundTypeId.AA ? BattlegroundTypeId.None : bgTypeId);

        if (!bg) {
            if (battlefieldPort.acceptedInvite) {
                Log.outDebug(LogFilter.Battleground, "CMSG_BATTLEFIELD_PORT {0} Slot: {1}, Unk: {2}, Time: {3}, AcceptedInvite: {4}. Cant find BG with id {5}!", getPlayerInfo(), battlefieldPort.ticket.id, battlefieldPort.ticket.type, battlefieldPort.ticket.time, battlefieldPort.acceptedInvite, ginfo.isInvitedToBGInstanceGUID);

                return;
            }

            bg = global.getBattlegroundMgr().getBattlegroundTemplate(bgTypeId);

            if (!bg) {
                Log.outError(LogFilter.Network, "BattlegroundHandler: bg_template not found for type id {0}.", bgTypeId);

                return;
            }
        }

        // get real bg type
        bgTypeId = bg.getTypeID();

        // expected bracket entry
        var bracketEntry = global.getDB2Mgr().GetBattlegroundBracketByLevel(bg.getMapId(), getPlayer().getLevel());

        if (bracketEntry == null) {
            return;
        }

        //some checks if player isn't cheating - it is not exactly cheating, but we cannot allow it
        if (battlefieldPort.acceptedInvite && bgQueue.getQueueId().teamSize == 0) {
            //if player is trying to enter Battleground(not arena!) and he has deserter debuff, we must just remove him from queue
            if (!getPlayer().isDeserter()) {
                // send bg command result to show nice message
                BattlefieldStatusFailed battlefieldStatus;
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatus = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatus, bgQueueTypeId, getPlayer(), battlefieldPort.ticket.id, GroupJoinBattlegroundResult.Deserters);
                battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                sendPacket(battlefieldStatus);
                battlefieldPort.acceptedInvite = false;
                Log.outDebug(LogFilter.Battleground, "Player {0} ({1}) has a deserter debuff, do not port him to Battleground!", getPlayer().getName(), getPlayer().getGUID().toString());
            }

            //if player don't match Battlegroundmax level, then do not allow him to enter! (this might happen when player leveled up during his waiting in queue
            if (getPlayer().getLevel() > bg.getMaxLevel()) {
                Log.outDebug(LogFilter.Network, "Player {0} ({1}) has level ({2}) higher than maxlevel ({3}) of Battleground({4})! Do not port him to Battleground!", getPlayer().getName(), getPlayer().getGUID().toString(), getPlayer().getLevel(), bg.getMaxLevel(), bg.getTypeID());

                battlefieldPort.acceptedInvite = false;
            }
        }

        if (battlefieldPort.acceptedInvite) {
            // check Freeze debuff
            if (getPlayer().hasAura(9454)) {
                return;
            }

            if (!getPlayer().isInvitedForBattlegroundQueueType(bgQueueTypeId)) {
                return; // cheating?
            }

            if (!getPlayer().getInBattleground()) {
                getPlayer().setBattlegroundEntryPoint();
            }

            // resurrect the player
            if (!getPlayer().isAlive()) {
                getPlayer().resurrectPlayer(1.0f);
                getPlayer().spawnCorpseBones();
            }

            // stop taxi flight at port
            getPlayer().finishTaxiFlight();

            BattlefieldStatusActive battlefieldStatus;
            tangible.OutObject<BattlefieldStatusActive> tempOut_battlefieldStatus2 = new tangible.OutObject<BattlefieldStatusActive>();
            global.getBattlegroundMgr().buildBattlegroundStatusActive(tempOut_battlefieldStatus2, bg, getPlayer(), battlefieldPort.ticket.id, getPlayer().getBattlegroundQueueJoinTime(bgQueueTypeId), bg.getArenaType());
            battlefieldStatus = tempOut_battlefieldStatus2.outArgValue;
            sendPacket(battlefieldStatus);

            // remove BattlegroundQueue status from BGmgr
            bgQueue.removePlayer(getPlayer().getGUID(), false);
            // this is still needed here if Battleground"jumping" shouldn't add deserter debuff
            // also this is required to prevent stuck at old Battlegroundafter SetBattlegroundId set to new
            var currentBg = getPlayer().getBattleground();

            if (currentBg) {
                currentBg.removePlayerAtLeave(getPlayer().getGUID(), false, true);
            }

            // set the destination instance id
            getPlayer().setBattlegroundId(bg.getInstanceID(), bgTypeId);
            // set the destination team
            getPlayer().setBgTeam(ginfo.team);

            global.getBattlegroundMgr().sendToBattleground(getPlayer(), ginfo.isInvitedToBGInstanceGUID, bgTypeId);
            Log.outDebug(LogFilter.Battleground, String.format("Battleground: player %1$s (%2$s) joined battle for bg %3$s, bgtype %4$s, queue %5$s.", player.getName(), player.getGUID(), bg.getInstanceID(), bg.getTypeID(), bgQueueTypeId));
        } else // leave queue
        {
            // if player leaves rated arena match before match start, it is counted as he played but he lost
            if (bgQueue.getQueueId().rated && ginfo.isInvitedToBGInstanceGUID != 0) {
                var at = global.getArenaTeamMgr().getArenaTeamById((int) ginfo.team.getValue());

                if (at != null) {
                    Log.outDebug(LogFilter.Battleground, "UPDATING memberLost's personal arena rating for {0} by opponents rating: {1}, because he has left queue!", getPlayer().getGUID().toString(), ginfo.opponentsTeamRating);
                    at.memberLost(getPlayer(), ginfo.opponentsMatchmakerRating);
                    at.saveToDB();
                }
            }

            BattlefieldStatusNone battlefieldStatus = new BattlefieldStatusNone();
            battlefieldStatus.ticket = battlefieldPort.ticket;
            sendPacket(battlefieldStatus);

            getPlayer().removeBattlegroundQueueId(bgQueueTypeId); // must be called this way, because if you move this call to queue.removeplayer, it causes bugs
            bgQueue.removePlayer(getPlayer().getGUID(), true);

            // player left queue, we should update it - do not update Arena Queue
            if (bgQueue.getQueueId().teamSize == 0) {
                global.getBattlegroundMgr().scheduleQueueUpdate(ginfo.arenaMatchmakerRating, bgQueueTypeId, bracketEntry.getBracketId());
            }

            Log.outDebug(LogFilter.Battleground, String.format("Battleground: player %1$s (%2$s) left queue for bgtype %3$s, queue %4$s.", player.getName(), player.getGUID(), bg.getTypeID(), bgQueueTypeId));
        }
    }


    private void handleBattlefieldLeave(BattlefieldLeave packet) {
        // not allow leave Battlegroundin combat
        if (getPlayer().isInCombat()) {
            var bg = getPlayer().getBattleground();

            if (bg) {
                if (bg.getStatus() != BattlegroundStatus.WaitLeave) {
                    return;
                }
            }
        }

        getPlayer().leaveBattleground();
    }


    private void handleRequestBattlefieldStatus(RequestBattlefieldStatus packet) {
        // we must update all queues here
        Battleground bg = null;

        for (byte i = 0; i < SharedConst.MaxPlayerBGQueues; ++i) {
            var bgQueueTypeId = getPlayer().getBattlegroundQueueTypeId(i);

            if (BattlegroundQueueTypeId.opEquals(bgQueueTypeId, null)) {
                continue;
            }

            var bgTypeId = BattlegroundTypeId.forValue(bgQueueTypeId.battlemasterListId);
            var arenaType = ArenaTypes.forValue(bgQueueTypeId.teamSize);
            bg = player.getBattleground();

            if (bg && BattlegroundQueueTypeId.opEquals(bg.getQueueId(), bgQueueTypeId)) {
                //i cannot check any variable from player class because player class doesn't know if player is in 2v2 / 3v3 or 5v5 arena
                //so i must use bg pointer to get that information
                BattlefieldStatusActive battlefieldStatus;
                tangible.OutObject<BattlefieldStatusActive> tempOut_battlefieldStatus = new tangible.OutObject<BattlefieldStatusActive>();
                global.getBattlegroundMgr().buildBattlegroundStatusActive(tempOut_battlefieldStatus, bg, player, i, player.getBattlegroundQueueJoinTime(bgQueueTypeId), arenaType);
                battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                sendPacket(battlefieldStatus);

                continue;
            }

            //we are sending update to player about queue - he can be invited there!
            //get GroupQueueInfo for queue status
            var bgQueue = global.getBattlegroundMgr().getBattlegroundQueue(bgQueueTypeId);

            GroupQueueInfo ginfo;
            tangible.OutObject<GroupQueueInfo> tempOut_ginfo = new tangible.OutObject<GroupQueueInfo>();
            if (!bgQueue.getPlayerGroupInfoData(getPlayer().getGUID(), tempOut_ginfo)) {
                ginfo = tempOut_ginfo.outArgValue;
                continue;
            } else {
                ginfo = tempOut_ginfo.outArgValue;
            }

            if (ginfo.isInvitedToBGInstanceGUID != 0) {
                bg = global.getBattlegroundMgr().getBattleground(ginfo.isInvitedToBGInstanceGUID, bgTypeId);

                if (!bg) {
                    continue;
                }

                BattlefieldStatusNeedConfirmation battlefieldStatus;
                tangible.OutObject<BattlefieldStatusNeedConfirmation> tempOut_battlefieldStatus2 = new tangible.OutObject<BattlefieldStatusNeedConfirmation>();
                global.getBattlegroundMgr().buildBattlegroundStatusNeedConfirmation(tempOut_battlefieldStatus2, bg, getPlayer(), i, getPlayer().getBattlegroundQueueJoinTime(bgQueueTypeId), time.GetMSTimeDiff(time.MSTime, ginfo.removeInviteTime), arenaType);
                battlefieldStatus = tempOut_battlefieldStatus2.outArgValue;
                sendPacket(battlefieldStatus);
            } else {
                bg = global.getBattlegroundMgr().getBattlegroundTemplate(bgTypeId);

                if (!bg) {
                    continue;
                }

                // expected bracket entry
                var bracketEntry = global.getDB2Mgr().GetBattlegroundBracketByLevel(bg.getMapId(), getPlayer().getLevel());

                if (bracketEntry == null) {
                    continue;
                }

                var avgTime = bgQueue.getAverageQueueWaitTime(ginfo, bracketEntry.getBracketId());
                BattlefieldStatusQueued battlefieldStatus;
                tangible.OutObject<BattlefieldStatusQueued> tempOut_battlefieldStatus3 = new tangible.OutObject<BattlefieldStatusQueued>();
                global.getBattlegroundMgr().buildBattlegroundStatusQueued(tempOut_battlefieldStatus3, bg, getPlayer(), i, getPlayer().getBattlegroundQueueJoinTime(bgQueueTypeId), bgQueueTypeId, avgTime, arenaType, ginfo.players.size() > 1);
                battlefieldStatus = tempOut_battlefieldStatus3.outArgValue;
                sendPacket(battlefieldStatus);
            }
        }
    }


    private void handleBattlemasterJoinArena(BattlemasterJoinArena packet) {
        // ignore if we already in BG or BG queue
        if (getPlayer().getInBattleground()) {
            return;
        }

        var arenatype = ArenaTypes.forValue(ArenaTeam.getTypeBySlot(packet.teamSizeIndex));

        //check existence
        var bg = global.getBattlegroundMgr().getBattlegroundTemplate(BattlegroundTypeId.AA);

        if (!bg) {
            Log.outError(LogFilter.Network, "Battleground: template bg (all arenas) not found");

            return;
        }

        if (global.getDisableMgr().isDisabledFor(DisableType.Battleground, (int) BattlegroundTypeId.AA.getValue(), null)) {
            getPlayer().sendSysMessage(SysMessage.ArenaDisabled);

            return;
        }

        var bgTypeId = bg.getTypeID();
        var bgQueueTypeId = global.getBattlegroundMgr().BGQueueTypeId((short) bgTypeId.getValue(), BattlegroundQueueIdType.Arena, true, arenatype);
        var bracketEntry = global.getDB2Mgr().GetBattlegroundBracketByLevel(bg.getMapId(), getPlayer().getLevel());

        if (bracketEntry == null) {
            return;
        }

        var grp = getPlayer().getGroup();

        // no group found, error
        if (!grp) {
            return;
        }

        if (ObjectGuid.opNotEquals(grp.getLeaderGUID(), getPlayer().getGUID())) {
            return;
        }

        var ateamId = getPlayer().getArenaTeamId(packet.teamSizeIndex);
        // check real arenateam existence only here (if it was moved to group.CanJoin .. () then we would ahve to get it twice)
        var at = global.getArenaTeamMgr().getArenaTeamById(ateamId);

        if (at == null) {
            return;
        }

        // get the team rating for queuing
        var arenaRating = at.getRating();
        var matchmakerRating = at.getAverageMMR(grp);
        // the arenateam id must match for everyone in the group

        if (arenaRating <= 0) {
            arenaRating = 1;
        }

        var bgQueue = global.getBattlegroundMgr().getBattlegroundQueue(bgQueueTypeId);

        int avgTime = 0;
        GroupQueueInfo ginfo = null;

        ObjectGuid errorGuid = ObjectGuid.EMPTY;
        tangible.OutObject<ObjectGuid> tempOut_errorGuid = new tangible.OutObject<ObjectGuid>();
        var err = grp.canJoinBattlegroundQueue(bg, bgQueueTypeId, (int) arenatype.getValue(), (int) arenatype.getValue(), true, packet.teamSizeIndex, tempOut_errorGuid);
        errorGuid = tempOut_errorGuid.outArgValue;

        if (err == 0) {
            Log.outDebug(LogFilter.Battleground, "Battleground: arena team id {0}, leader {1} queued with matchmaker rating {2} for type {3}", getPlayer().getArenaTeamId(packet.teamSizeIndex), getPlayer().getName(), matchmakerRating, arenatype);

            ginfo = bgQueue.addGroup(getPlayer(), grp, player.getTeam(), bracketEntry, false, arenaRating, matchmakerRating, ateamId);
            avgTime = bgQueue.getAverageQueueWaitTime(ginfo, bracketEntry.getBracketId());
        }

        for (var refe = grp.getFirstMember(); refe != null; refe = refe.next()) {
            var member = refe.getSource();

            if (!member) {
                continue;
            }

            if (err != 0) {
                BattlefieldStatusFailed battlefieldStatus;
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatus = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatus, bgQueueTypeId, getPlayer(), 0, err, errorGuid);
                battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                member.sendPacket(battlefieldStatus);

                continue;
            }

            if (!getPlayer().canJoinToBattleground(bg)) {
                BattlefieldStatusFailed battlefieldStatus;
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatus2 = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatus2, bgQueueTypeId, getPlayer(), 0, GroupJoinBattlegroundResult.BattlegroundJoinFailed, errorGuid);
                battlefieldStatus = tempOut_battlefieldStatus2.outArgValue;
                member.sendPacket(battlefieldStatus);

                return;
            }

            // add to queue
            var queueSlot = member.addBattlegroundQueueId(bgQueueTypeId);

            BattlefieldStatusQueued battlefieldStatusQueued;
            tangible.OutObject<BattlefieldStatusQueued> tempOut_battlefieldStatusQueued = new tangible.OutObject<BattlefieldStatusQueued>();
            global.getBattlegroundMgr().buildBattlegroundStatusQueued(tempOut_battlefieldStatusQueued, bg, member, queueSlot, ginfo.joinTime, bgQueueTypeId, avgTime, arenatype, true);
            battlefieldStatusQueued = tempOut_battlefieldStatusQueued.outArgValue;
            member.sendPacket(battlefieldStatusQueued);

            Log.outDebug(LogFilter.Battleground, String.format("Battleground: player joined queue for arena as group bg queue %1$s, %2$s, NAME %3$s", bgQueueTypeId, member.getGUID(), member.getName()));
        }

        global.getBattlegroundMgr().scheduleQueueUpdate(matchmakerRating, bgQueueTypeId, bracketEntry.getBracketId());
    }


    private void handleReportPvPAFK(ReportPvPPlayerAFK reportPvPPlayerAFK) {
        var reportedPlayer = global.getObjAccessor().findPlayer(reportPvPPlayerAFK.offender);

        if (!reportedPlayer) {
            Log.outDebug(LogFilter.Battleground, "WorldSession.HandleReportPvPAFK: player not found");

            return;
        }

        Log.outDebug(LogFilter.BattlegroundReportPvpAfk, "WorldSession.HandleReportPvPAFK:  {0} [IP: {1}] reported {2}", player.getName(), player.getSession().getRemoteAddress(), reportedPlayer.getGUID().toString());

        reportedPlayer.reportedAfkBy(getPlayer());
    }


    private void handleRequestRatedPvpInfo(RequestRatedPvpInfo packet) {
        RatedPvpInfo ratedPvpInfo = new RatedPvpInfo();
        sendPacket(ratedPvpInfo);
    }


    private void handleGetPVPOptionsEnabled(GetPVPOptionsEnabled packet) {
        // This packet is completely irrelevant, it triggers PVP_TYPES_ENABLED lua event but that is not handled in interface code as of 6.1.2
        PVPOptionsEnabled pvpOptionsEnabled = new PVPOptionsEnabled();
        pvpOptionsEnabled.pugBattlegrounds = true;
        sendPacket(new PVPOptionsEnabled());
    }


    private void handleRequestPvpReward(RequestPVPRewards packet) {
        getPlayer().sendPvpRewards();
    }


    private void handleAreaSpiritHealerQuery(AreaSpiritHealerQuery areaSpiritHealerQuery) {
        var unit = ObjectAccessor.getCreature(getPlayer(), areaSpiritHealerQuery.healerGuid);

        if (!unit) {
            return;
        }

        if (!unit.isSpiritService()) // it's not spirit service
        {
            return;
        }

        var bg = getPlayer().getBattleground();

        if (bg != null) {
            global.getBattlegroundMgr().sendAreaSpiritHealerQuery(getPlayer(), bg, areaSpiritHealerQuery.healerGuid);
        }

        var bf = global.getBattleFieldMgr().getBattlefieldToZoneId(getPlayer().getMap(), getPlayer().getZoneId());

        if (bf != null) {
            bf.sendAreaSpiritHealerQuery(getPlayer(), areaSpiritHealerQuery.healerGuid);
        }
    }


    private void handleAreaSpiritHealerQueue(AreaSpiritHealerQueue areaSpiritHealerQueue) {
        var unit = ObjectAccessor.getCreature(getPlayer(), areaSpiritHealerQueue.healerGuid);

        if (!unit) {
            return;
        }

        if (!unit.isSpiritService()) // it's not spirit service
        {
            return;
        }

        var bg = getPlayer().getBattleground();

        if (bg) {
            bg.addPlayerToResurrectQueue(areaSpiritHealerQueue.healerGuid, getPlayer().getGUID());
        }

        var bf = global.getBattleFieldMgr().getBattlefieldToZoneId(getPlayer().getMap(), getPlayer().getZoneId());

        if (bf != null) {
            bf.addPlayerToResurrectQueue(areaSpiritHealerQueue.healerGuid, getPlayer().getGUID());
        }
    }


    private void handleHearthAndResurrect(HearthAndResurrect packet) {
        if (getPlayer().isInFlight()) {
            return;
        }

        var bf = global.getBattleFieldMgr().getBattlefieldToZoneId(getPlayer().getMap(), getPlayer().getZoneId());

        if (bf != null) {
            bf.playerAskToLeave(player);

            return;
        }

        var atEntry = CliDB.AreaTableStorage.get(getPlayer().getAreaId());

        if (atEntry == null || !atEntry.hasFlag(AreaFlags.CanHearthAndResurrect)) {
            return;
        }

        getPlayer().buildPlayerRepop();
        getPlayer().resurrectPlayer(1.0f);
        getPlayer().teleportTo(getPlayer().getHomeBind());
    }


    private void handleJoinSkirmish(JoinSkirmish packet) {
        var player = getPlayer();

        if (player == null) {
            return;
        }

        var isPremade = false;
        PlayerGroup grp = null;

        var arenatype = (packet.bracket == BracketType.SKIRMISH_3 ? ArenaTypes.Team3v3 : ArenaTypes.Team2v2);

        var bg = BattlegroundManager.getInstance().getBattlegroundTemplate(BattlegroundTypeId.AA);

        if (bg == null) {
            return;
        }

        var getQueueTeam = () ->
        {
            // mercenary applies only to unrated battlegrounds
            if (!bg.isRated() && !bg.isArena()) {
                if (player.hasAura(193472)) // SPELL_MERCENARY_CONTRACT_HORDE
                {
                    return Team.Horde;
                }

                if (player.hasAura(193475)) // SPELL_MERCENARY_CONTRACT_ALLIANCE
                {
                    return Team.ALLIANCE;
                }
            }

            return player.getTeam();
        };

        if (DisableManager.getInstance().isDisabledFor(DisableType.Battleground, (int) BattlegroundTypeId.AA.getValue(), null)) {
            player.sendSysMessage(SysMessage.ArenaDisabled);

            return;
        }

        var bgTypeId = bg.getTypeID();
        var bgQueueTypeId = BattlegroundManager.getInstance().BGQueueTypeId((short) bgTypeId.getValue(), BattlegroundQueueIdType.Arena, true, arenatype);

        if (player.getInBattleground()) {
            return;
        }

        var bracketEntry = global.getDB2Mgr().GetBattlegroundBracketByLevel(bg.getMapId(), player.getLevel());

        if (bracketEntry == null) {
            return;
        }

        var err = GroupJoinBattlegroundResult.NONE;

        if (!packet.joinAsGroup) {
            if (player.isUsingLfg()) {
                BattlefieldStatusFailed battlefieldStatusFailed;
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed, bgQueueTypeId, player, 0, GroupJoinBattlegroundResult.LfgCantUseBattleground);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            if (!player.canJoinToBattleground(bg)) {
                BattlefieldStatusFailed battlefieldStatusFailed;
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed2 = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed2, bgQueueTypeId, player, 0, GroupJoinBattlegroundResult.LfgCantUseBattleground);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed2.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            if (player.getBattlegroundQueueIndex(bgQueueTypeId) < 2) {
                return;
            }

            if (!player.getHasFreeBattlegroundQueueId()) {
                BattlefieldStatusFailed battlefieldStatusFailed;
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatusFailed3 = new tangible.OutObject<BattlefieldStatusFailed>();
                global.getBattlegroundMgr().buildBattlegroundStatusFailed(tempOut_battlefieldStatusFailed3, bgQueueTypeId, player, 0, GroupJoinBattlegroundResult.LfgCantUseBattleground);
                battlefieldStatusFailed = tempOut_battlefieldStatusFailed3.outArgValue;
                sendPacket(battlefieldStatusFailed);

                return;
            }

            var bgQueue = BattlegroundManager.getInstance().getBattlegroundQueue(bgQueueTypeId);
            var ginfo = bgQueue.addGroup(player, grp, getQueueTeam(), bracketEntry, isPremade, 0, 0);

            var avgTime = bgQueue.getAverageQueueWaitTime(ginfo, bracketEntry.getBracketId());
            var queueSlot = player.addBattlegroundQueueId(bgQueueTypeId);

            var battlefieldStatus = new BattlefieldStatusQueued();
            tangible.OutObject<BattlefieldStatusQueued> tempOut_battlefieldStatus = new tangible.OutObject<BattlefieldStatusQueued>();
            BattlegroundManager.getInstance().buildBattlegroundStatusQueued(tempOut_battlefieldStatus, bg, player, queueSlot, ginfo.joinTime, bgQueueTypeId, avgTime, arenatype, false);
            battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
            sendPacket(battlefieldStatus);
        } else {
            grp = player.getGroup();

            if (grp == null) {
                BattlefieldStatusFailed battlefieldStatuss;
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatuss = new tangible.OutObject<BattlefieldStatusFailed>();
                BattlegroundManager.getInstance().buildBattlegroundStatusFailed(tempOut_battlefieldStatuss, bgQueueTypeId, player, 0, GroupJoinBattlegroundResult.LfgCantUseBattleground);
                battlefieldStatuss = tempOut_battlefieldStatuss.outArgValue;
                getPlayer().getSession().sendPacket(battlefieldStatuss);

                return;
            }

            if (ObjectGuid.opNotEquals(grp.getLeaderGUID(), player.getGUID())) {
                BattlefieldStatusFailed battlefieldStatuss;
                tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatuss2 = new tangible.OutObject<BattlefieldStatusFailed>();
                BattlegroundManager.getInstance().buildBattlegroundStatusFailed(tempOut_battlefieldStatuss2, bgQueueTypeId, player, 0, GroupJoinBattlegroundResult.LfgCantUseBattleground);
                battlefieldStatuss = tempOut_battlefieldStatuss2.outArgValue;
                getPlayer().getSession().sendPacket(battlefieldStatuss);

                return;
            }

            ObjectGuid errorGuid = ObjectGuid.EMPTY;
            tangible.OutObject<ObjectGuid> tempOut_errorGuid = new tangible.OutObject<ObjectGuid>();
            err = grp.canJoinBattlegroundQueue(bg, bgQueueTypeId, 0, bg.getMaxPlayersPerTeam(), false, 0, tempOut_errorGuid);
            errorGuid = tempOut_errorGuid.outArgValue;

            var bgQueue = BattlegroundManager.getInstance().getBattlegroundQueue(bgQueueTypeId);
            GroupQueueInfo ginfo = null;
            int avgTime = 0;

            if (err == null) {
                ginfo = bgQueue.addGroup(player, null, getQueueTeam(), bracketEntry, isPremade, 0, 0);
                avgTime = bgQueue.getAverageQueueWaitTime(ginfo, bracketEntry.getBracketId());
            }

            for (var slot : grp.getMemberSlots()) {
                var member = global.getObjAccessor().findPlayer(slot.guid);

                if (member == null) {
                    continue;
                }

                if (err != null) {
                    BattlefieldStatusFailed battlefieldStatuss;
                    tangible.OutObject<BattlefieldStatusFailed> tempOut_battlefieldStatuss3 = new tangible.OutObject<BattlefieldStatusFailed>();
                    BattlegroundManager.getInstance().buildBattlegroundStatusFailed(tempOut_battlefieldStatuss3, bgQueueTypeId, player, 0, GroupJoinBattlegroundResult.LfgCantUseBattleground);
                    battlefieldStatuss = tempOut_battlefieldStatuss3.outArgValue;
                    member.getSession().sendPacket(battlefieldStatuss);

                    continue;
                }

                BattlefieldStatusQueued battlefieldStatus;
                tangible.OutObject<BattlefieldStatusQueued> tempOut_battlefieldStatus2 = new tangible.OutObject<BattlefieldStatusQueued>();
                BattlegroundManager.getInstance().buildBattlegroundStatusQueued(tempOut_battlefieldStatus2, bg, member, member.addBattlegroundQueueId(bgQueueTypeId), ginfo.joinTime, bgQueueTypeId, avgTime, 0, true);
                battlefieldStatus = tempOut_battlefieldStatus2.outArgValue;
                member.sendPacket(battlefieldStatus);
            }
        }

        BattlegroundManager.getInstance().scheduleQueueUpdate(0, bgQueueTypeId, bracketEntry.getBracketId());
    }

    public final void sendBattlenetResponse(int serviceHash, int methodId, int token, IMessage response) {
        Response bnetResponse = new response();
        bnetResponse.bnetStatus = BattlenetRpcErrorCode.Ok;
        bnetResponse.method.type = MathUtil.MakePair64(methodId, serviceHash);
        bnetResponse.method.objectId = 1;
        bnetResponse.method.token = token;

        if (response.CalculateSize() != 0) {
            bnetResponse.data.writeBytes(response.ToByteArray());
        }

        sendPacket(bnetResponse);
    }

    public final void sendBattlenetResponse(int serviceHash, int methodId, int token, BattlenetRpcErrorCode status) {
        Response bnetResponse = new response();
        bnetResponse.bnetStatus = status;
        bnetResponse.method.type = MathUtil.MakePair64(methodId, serviceHash);
        bnetResponse.method.objectId = 1;
        bnetResponse.method.token = token;

        sendPacket(bnetResponse);
    }

    public final void sendBattlenetRequest(int serviceHash, int methodId, IMessage request, tangible.Action1Param<CodedInputStream> callback) {
        battlenetResponseCallbacks.put(battlenetRequestToken, callback);
        sendBattlenetRequest(serviceHash, methodId, request);
    }

    public final void sendBattlenetRequest(int serviceHash, int methodId, IMessage request) {
        Notification notification = new Notification();
        notification.method.type = MathUtil.MakePair64(methodId, serviceHash);
        notification.method.objectId = 1;
        notification.method.token = battlenetRequestToken++;

        if (request.CalculateSize() != 0) {
            notification.data.writeBytes(request.ToByteArray());
        }

        sendPacket(notification);
    }


    private void handleBattlenetRequest(BattlenetRequest request) {
        var handler = global.getServiceMgr().getHandler(request.method.getServiceHash(), request.method.getMethodId());

        if (handler != null) {
            handler.invoke(this, request.method, new CodedInputStream(request.data));
        } else {
            sendBattlenetResponse(request.method.getServiceHash(), request.method.getMethodId(), request.method.token, BattlenetRpcErrorCode.RpcNotImplemented);
            Log.outDebug(LogFilter.SessionRpc, "{0} tried to call invalid service {1}", getPlayerInfo(), request.method.getServiceHash());
        }
    }


    private void handleBattlenetChangeRealmTicket(ChangeRealmTicket changeRealmTicket) {
        setRealmListSecret(changeRealmTicket.secret);

        ChangeRealmTicketResponse realmListTicket = new ChangeRealmTicketResponse();
        realmListTicket.token = changeRealmTicket.token;
        realmListTicket.allow = true;
        realmListTicket.ticket = new byteBuffer();
        realmListTicket.ticket.writeCString("WorldserverRealmListTicket");

        sendPacket(realmListTicket);
    }

    public final void sendStartPurchaseResponse(WorldSession session, Purchase purchase, BpayError result) {
        var response = new StartPurchaseResponse();
        response.setPurchaseID(purchase.purchaseID);
        response.setClientToken(purchase.clientToken);
        response.setPurchaseResult((int) result.getValue());
        session.sendPacket(response);
    }

    public final void sendPurchaseUpdate(WorldSession session, Purchase purchase, BpayError result) {
        var packet = new PurchaseUpdate();
        var data = new BpayPurchase();
        data.setPurchaseID(purchase.purchaseID);
        data.setUnkLong(0);
        data.setUnkLong2(0);
        data.setStatus(purchase.status);
        data.setResultCode((int) result.getValue());
        data.setProductID(purchase.productID);
        data.setUnkInt(purchase.serverToken);
        data.setWalletName(session.getBattlePayMgr().getDefaultWalletName());
        packet.getPurchase().add(data);
        session.sendPacket(packet);
    }


    public final void handleGetPurchaseListQuery(GetPurchaseListQuery UnnamedParameter) {
        if (!getBattlePayMgr().isAvailable()) {
            return;
        }
        var packet = new PurchaseListResponse(); // @TODO
        sendPacket(packet);
    }


    public final void handleUpdateVasPurchaseStates(UpdateVasPurchaseStates UnnamedParameter) {
        if (!getBattlePayMgr().isAvailable()) {
            return;
        }
        var response = new EnumVasPurchaseStatesResponse();
        response.setResult(0);
        sendPacket(response);
    }


    public final void handleBattlePayDistributionAssign(DistributionAssignToTarget packet) {
        if (!getBattlePayMgr().isAvailable()) {
            return;
        }

        getBattlePayMgr().assignDistributionToCharacter(packet.getTargetCharacter(), packet.getDistributionID(), packet.getProductID(), packet.getSpecializationID(), packet.getChoiceID());
    }


    public final void handleGetProductList(GetProductList UnnamedParameter) {
        if (!getBattlePayMgr().isAvailable()) {
            return;
        }

        getBattlePayMgr().sendProductList();
        getBattlePayMgr().sendAccountCredits();
    }

    public final void sendMakePurchase(ObjectGuid targetCharacter, int clientToken, int productID, WorldSession session) {
        if (session == null || !session.getBattlePayMgr().isAvailable()) {
            return;
        }

        var mgr = session.getBattlePayMgr();
        var player = session.getPlayer();
        //    auto accountID = session->GetAccountId();

        var purchase = new purchase();
        purchase.productID = productID;
        purchase.clientToken = clientToken;
        purchase.targetCharacter = targetCharacter;
        purchase.status = (short) BpayUpdateStatus.Loading.getValue();
        purchase.distributionId = mgr.generateNewDistributionId();

        var productInfo = BattlePayDataStoreMgr.getInstance().getProductInfoForProduct(productID);

        purchase.currentPrice = (long) productInfo.getCurrentPriceFixedPoint();

        mgr.registerStartPurchase(purchase);

        var accountCredits = getBattlePayMgr().getBattlePayCredits();
        var purchaseData = mgr.getPurchase();

        if (accountCredits < (long) purchaseData.currentPrice) {
            sendStartPurchaseResponse(session, purchaseData, BpayError.InsufficientBalance);

            return;
        }

        for (var productId : productInfo.getProductIds()) {
            if (BattlePayDataStoreMgr.getInstance().productExist(productId)) {
                var product = BattlePayDataStoreMgr.getInstance().getProduct(productId);

                // if buy is disabled in product addons
                var productAddon = BattlePayDataStoreMgr.getInstance().getProductAddon(productInfo.getEntry());

                if (productAddon != null) {
                    if (productAddon.getDisableBuy() > 0) {
                        sendStartPurchaseResponse(session, purchaseData, BpayError.PurchaseDenied);
                    }
                }

                if (!product.getItems().isEmpty()) {
                    if (player) {
                        if (product.getItems().size() > player.getFreeBagSlotCount()) {
                            getBattlePayMgr().sendBattlePayMessage(11, product.getName());
                            sendStartPurchaseResponse(session, purchaseData, BpayError.PurchaseDenied);

                            return;
                        }
                    }

                    for (var itr : product.getItems()) {
                        if (mgr.alreadyOwnProduct(itr.getItemID())) {
                            getBattlePayMgr().sendBattlePayMessage(12, product.getName());
                            sendStartPurchaseResponse(session, purchaseData, BpayError.PurchaseDenied);

                            return;
                        }
                    }
                }
            } else {
                sendStartPurchaseResponse(session, purchaseData, BpayError.PurchaseDenied);

                return;
            }
        }

        purchaseData.purchaseID = mgr.generateNewPurchaseID();
        purchaseData.serverToken = RandomUtil.Rand32(0xFFFFFFF);

        sendStartPurchaseResponse(session, purchaseData, BpayError.Ok);
        sendPurchaseUpdate(session, purchaseData, BpayError.Ok);

        var confirmPurchase = new confirmPurchase();
        confirmPurchase.setPurchaseID(purchaseData.purchaseID);
        confirmPurchase.setServerToken(purchaseData.serverToken);
        session.sendPacket(confirmPurchase);
    }

    //C++ TO C# CONVERTER WARNING: The original C++ declaration of the following method implementation was not found:
    public final void handleBattlePayStartPurchase(StartPurchase packet) {
        sendMakePurchase(packet.getTargetCharacter(), packet.getClientToken(), packet.getProductID(), this);
    }

    //C++ TO C# CONVERTER WARNING: The original C++ declaration of the following method implementation was not found:
    public final void handleBattlePayConfirmPurchase(ConfirmPurchaseResponse packet) {
        if (!getBattlePayMgr().isAvailable()) {
            return;
        }

        var purchase = getBattlePayMgr().getPurchase();

        if (purchase == null) {
            return;
        }

        var productInfo = BattlePayDataStoreMgr.getInstance().getProductInfoForProduct(purchase.productID);
        var displayInfo = BattlePayDataStoreMgr.getInstance().getDisplayInfo(productInfo.getEntry());

        if (purchase.lock) {
            sendPurchaseUpdate(this, purchase, BpayError.PurchaseDenied);

            return;
        }

        if (purchase.serverToken != packet.getServerToken() || !packet.getConfirmPurchase() || purchase.currentPrice != packet.getClientCurrentPriceFixedPoint()) {
            sendPurchaseUpdate(this, purchase, BpayError.PurchaseDenied);

            return;
        }

        var accountBalance = getBattlePayMgr().getBattlePayCredits();

        if (accountBalance < purchase.currentPrice) {
            sendPurchaseUpdate(this, purchase, BpayError.PurchaseDenied);

            return;
        }

        purchase.lock = true;
        purchase.status = (short) BpayUpdateStatus.Finish.getValue();

        sendPurchaseUpdate(this, purchase, BpayError.other);
        getBattlePayMgr().savePurchase(purchase);
        getBattlePayMgr().processDelivery(purchase);
        getBattlePayMgr().updateBattlePayCredits(purchase.currentPrice);

        if (displayInfo.getName1().length() != 0) {
            getBattlePayMgr().sendBattlePayMessage(1, displayInfo.getName1());
        }

        getBattlePayMgr().sendProductList();
    }

    public final void handleBattlePayAckFailedResponse(BattlePayAckFailedResponse UnnamedParameter) {
    }

    //C++ TO C# CONVERTER WARNING: The original C++ declaration of the following method implementation was not found:
    public final void handleBattlePayRequestPriceInfo(BattlePayRequestPriceInfo UnnamedParameter) {
    }

    public final void sendDisplayPromo(int promotionID) {
        sendPacket(new DisplayPromotion(promotionID));

        if (!getBattlePayMgr().isAvailable()) {
            return;
        }

        if (!BattlePayDataStoreMgr.getInstance().productExist(260)) {
            return;
        }

        var product = BattlePayDataStoreMgr.getInstance().getProduct(260);
        var packet = new DistributionListResponse();
        packet.setResult((int) BpayError.Ok.getValue());

        var data = new bpayDistributionObject();
        data.setTargetPlayer(getPlayer().getGUID());
        data.setDistributionID(getBattlePayMgr().generateNewDistributionId());
        data.setPurchaseID(getBattlePayMgr().generateNewPurchaseID());
        data.setStatus((int) BpayDistributionStatus.AVAILABLE.getValue());
        data.setProductID(260);
        data.setTargetVirtualRealm(0);
        data.setTargetNativeRealm(0);
        data.setRevoked(false);

        var productInfo = BattlePayDataStoreMgr.getInstance().getProductInfoForProduct(product.getProductId());

        // BATTLEPAY PRODUCTS
        var pProduct = new BpayProduct();
        pProduct.setProductId(product.getProductId());
        pProduct.setType(product.getType());
        pProduct.setFlags(product.getFlags());
        pProduct.setUnk1(product.getUnk1());
        pProduct.setDisplayId(product.getDisplayId());
        pProduct.setItemId(product.getItemId());
        pProduct.setUnk4(product.getUnk4());
        pProduct.setUnk5(product.getUnk5());
        pProduct.setUnk6(product.getUnk6());
        pProduct.setUnk7(product.getUnk7());
        pProduct.setUnk8(product.getUnk8());
        pProduct.setUnk9(product.getUnk9());
        pProduct.setUnkString(product.getUnkString());
        pProduct.setUnkBit(product.getUnkBit());
        pProduct.setUnkBits(product.getUnkBits());

        // BATTLEPAY ITEM
        if (!product.getItems().isEmpty()) {
            for (var item : BattlePayDataStoreMgr.getInstance().getItemsOfProduct(product.getProductId())) {
                var pItem = new BpayProductItem();
                pItem.setID(item.getID());
                pItem.setUnkByte(item.getUnkByte());
                pItem.setItemID(item.getItemID());
                pItem.setQuantity(item.getQuantity());
                pItem.setUnkInt1(item.getUnkInt1());
                pItem.setUnkInt2(item.getUnkInt2());
                pItem.setPet(item.isPet());
                pItem.setPetResult(item.getPetResult());

                if (BattlePayDataStoreMgr.getInstance().displayInfoExist(productInfo.getEntry())) {
                    // productinfo entry and display entry must be the same
                    var dispInfo = getBattlePayMgr().writeDisplayInfo(productInfo.getEntry());

                    if (dispInfo.Item1) {
                        pItem.setDisplay(dispInfo.item2);
                    }
                }

                pProduct.getItems().add(pItem);
            }
        }

        // productinfo entry and display entry must be the same
        var display = getBattlePayMgr().writeDisplayInfo(productInfo.getEntry());

        if (display.Item1) {
            pProduct.setDisplay(display.item2);
        }

        data.setProduct(pProduct);

        packet.getDistributionObject().add(data);

        sendPacket(packet);
    }

    public final void sendSyncWowEntitlements() {
        var packet = new SyncWowEntitlements();
        sendPacket(packet);
    }


    private void handleBattlePetRequestJournal(BattlePetRequestJournal battlePetRequestJournal) {
        getBattlePetMgr().sendJournal();
    }


    private void handleBattlePetRequestJournalLock(BattlePetRequestJournalLock battlePetRequestJournalLock) {
        getBattlePetMgr().sendJournalLockStatus();

        if (getBattlePetMgr().getHasJournalLock()) {
            getBattlePetMgr().sendJournal();
        }
    }


    private void handleBattlePetSetBattleSlot(BattlePetSetBattleSlot battlePetSetBattleSlot) {
        var pet = getBattlePetMgr().getPet(battlePetSetBattleSlot.petGuid);

        if (pet != null) {
            var slot = getBattlePetMgr().getSlot(BattlePetSlot.forValue(battlePetSetBattleSlot.slot));

            if (slot != null) {
                slot.pet = pet.packetInfo;
            }
        }
    }


    private void handleBattlePetModifyName(BattlePetModifyName battlePetModifyName) {
        getBattlePetMgr().modifyName(battlePetModifyName.petGuid, battlePetModifyName.name, battlePetModifyName.declinedNames);
    }


    private void handleQueryBattlePetName(QueryBattlePetName queryBattlePetName) {
        QueryBattlePetNameResponse response = new QueryBattlePetNameResponse();
        response.battlePetID = queryBattlePetName.battlePetID;

        var summonedBattlePet = ObjectAccessor.GetCreatureOrPetOrVehicle(player, queryBattlePetName.unitGUID);

        if (!summonedBattlePet || !summonedBattlePet.isSummon()) {
            sendPacket(response);

            return;
        }

        response.creatureID = summonedBattlePet.getEntry();
        response.timestamp = summonedBattlePet.getBattlePetCompanionNameTimestamp();

        var petOwner = summonedBattlePet.toTempSummon().getSummonerUnit();

        if (!petOwner.isPlayer()) {
            sendPacket(response);

            return;
        }

        var battlePet = petOwner.toPlayer().getSession().getBattlePetMgr().getPet(queryBattlePetName.battlePetID);

        if (battlePet == null) {
            sendPacket(response);

            return;
        }

        response.name = battlePet.packetInfo.name;

        if (battlePet.declinedName != null) {
            response.hasDeclined = true;
            response.declinedNames = battlePet.declinedName;
        }

        response.allow = !response.name.isEmpty();

        sendPacket(response);
    }


    private void handleBattlePetDeletePet(BattlePetDeletePet battlePetDeletePet) {
        getBattlePetMgr().removePet(battlePetDeletePet.petGuid);
    }


    private void handleBattlePetSetFlags(BattlePetSetFlags battlePetSetFlags) {
        if (!getBattlePetMgr().getHasJournalLock()) {
            return;
        }

        var pet = getBattlePetMgr().getPet(battlePetSetFlags.petGuid);

        if (pet != null) {
            if (battlePetSetFlags.controlType == FlagsControlType.apply) {
                pet.packetInfo.flags |= (short) battlePetSetFlags.flags;
            } else {
                pet.packetInfo.flags &= (short) ~battlePetSetFlags.flags;
            }

            if (pet.saveInfo != BattlePetSaveInfo.New) {
                pet.saveInfo = BattlePetSaveInfo.changed;
            }
        }
    }


    private void handleBattlePetClearFanfare(BattlePetClearFanfare battlePetClearFanfare) {
        getBattlePetMgr().clearFanfare(battlePetClearFanfare.petGuid);
    }


    private void handleCageBattlePet(CageBattlePet cageBattlePet) {
        getBattlePetMgr().cageBattlePet(cageBattlePet.petGuid);
    }


    private void handleBattlePetSummon(BattlePetSummon battlePetSummon) {
        if (ObjectGuid.opNotEquals(player.getSummonedBattlePetGUID(), battlePetSummon.petGuid)) {
            getBattlePetMgr().summonPet(battlePetSummon.petGuid);
        } else {
            getBattlePetMgr().dismissPet();
        }
    }


    private void handleBattlePetUpdateNotify(BattlePetUpdateNotify battlePetUpdateNotify) {
        getBattlePetMgr().updateBattlePetData(battlePetUpdateNotify.petGuid);
    }

    public final void sendBlackMarketWonNotification(BlackMarketEntry entry, Item item) {
        BlackMarketWon packet = new BlackMarketWon();

        packet.marketID = entry.getMarketId();
        packet.item = new itemInstance(item);

        sendPacket(packet);
    }

    public final void sendBlackMarketOutbidNotification(BlackMarketTemplate templ) {
        BlackMarketOutbid packet = new BlackMarketOutbid();

        packet.marketID = templ.marketID;
        packet.item = templ.item;
        packet.randomPropertiesID = 0;

        sendPacket(packet);
    }


    private void handleBlackMarketOpen(BlackMarketOpen blackMarketOpen) {
        var unit = getPlayer().getNPCIfCanInteractWith(blackMarketOpen.guid, NPCFlags.BlackMarket, NPCFlags2.BlackMarketView);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBlackMarketHello - {0} not found or you can't interact with him.", blackMarketOpen.guid.toString());

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        sendBlackMarketOpenResult(blackMarketOpen.guid, unit);
    }

    private void sendBlackMarketOpenResult(ObjectGuid guid, Creature auctioneer) {
        NPCInteractionOpenResult npcInteraction = new NPCInteractionOpenResult();
        npcInteraction.npc = guid;
        npcInteraction.interactionType = PlayerInteractionType.BlackMarketAuctioneer;
        npcInteraction.success = global.getBlackMarketMgr().isEnabled();
        sendPacket(npcInteraction);
    }


    private void handleBlackMarketRequestItems(BlackMarketRequestItems blackMarketRequestItems) {
        if (!global.getBlackMarketMgr().isEnabled()) {
            return;
        }

        var unit = getPlayer().getNPCIfCanInteractWith(blackMarketRequestItems.guid, NPCFlags.BlackMarket, NPCFlags2.BlackMarketView);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBlackMarketRequestItems - {0} not found or you can't interact with him.", blackMarketRequestItems.guid.toString());

            return;
        }

        BlackMarketRequestItemsResult result = new BlackMarketRequestItemsResult();
        global.getBlackMarketMgr().buildItemsResponse(result, getPlayer());
        sendPacket(result);
    }


    private void handleBlackMarketBidOnItem(BlackMarketBidOnItem blackMarketBidOnItem) {
        if (!global.getBlackMarketMgr().isEnabled()) {
            return;
        }

        var player = getPlayer();
        var unit = player.getNPCIfCanInteractWith(blackMarketBidOnItem.guid, NPCFlags.BlackMarket, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBlackMarketBidOnItem - {0} not found or you can't interact with him.", blackMarketBidOnItem.guid.toString());

            return;
        }

        var entry = global.getBlackMarketMgr().getAuctionByID(blackMarketBidOnItem.marketID);

        if (entry == null) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBlackMarketBidOnItem - {0} (name: {1}) tried to bid on a nonexistent auction (MarketId: {2}).", player.getGUID().toString(), player.getName(), blackMarketBidOnItem.marketID);
            sendBlackMarketBidOnItemResult(BlackMarketError.ItemNotFound, blackMarketBidOnItem.marketID, blackMarketBidOnItem.item);

            return;
        }

        if (entry.getBidder() == player.getGUID().getCounter()) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBlackMarketBidOnItem - {0} (name: {1}) tried to place a bid on an item he already bid on. (MarketId: {2}).", player.getGUID().toString(), player.getName(), blackMarketBidOnItem.marketID);
            sendBlackMarketBidOnItemResult(BlackMarketError.AlreadyBid, blackMarketBidOnItem.marketID, blackMarketBidOnItem.item);

            return;
        }

        if (!entry.validateBid(blackMarketBidOnItem.bidAmount)) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBlackMarketBidOnItem - {0} (name: {1}) tried to place an invalid bid. Amount: {2} (MarketId: {3}).", player.getGUID().toString(), player.getName(), blackMarketBidOnItem.bidAmount, blackMarketBidOnItem.marketID);
            sendBlackMarketBidOnItemResult(BlackMarketError.HigherBid, blackMarketBidOnItem.marketID, blackMarketBidOnItem.item);

            return;
        }

        if (!player.hasEnoughMoney(blackMarketBidOnItem.bidAmount)) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBlackMarketBidOnItem - {0} (name: {1}) does not have enough money to place bid. (MarketId: {2}).", player.getGUID().toString(), player.getName(), blackMarketBidOnItem.marketID);
            sendBlackMarketBidOnItemResult(BlackMarketError.NotEnoughMoney, blackMarketBidOnItem.marketID, blackMarketBidOnItem.item);

            return;
        }

        if (entry.getSecondsRemaining() <= 0) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBlackMarketBidOnItem - {0} (name: {1}) tried to bid on a completed auction. (MarketId: {2}).", player.getGUID().toString(), player.getName(), blackMarketBidOnItem.marketID);
            sendBlackMarketBidOnItemResult(BlackMarketError.DatabaseError, blackMarketBidOnItem.marketID, blackMarketBidOnItem.item);

            return;
        }

        SQLTransaction trans = new SQLTransaction();

        global.getBlackMarketMgr().sendAuctionOutbidMail(entry, trans);
        entry.placeBid(blackMarketBidOnItem.bidAmount, player, trans);

        DB.characters.CommitTransaction(trans);

        sendBlackMarketBidOnItemResult(BlackMarketError.Ok, blackMarketBidOnItem.marketID, blackMarketBidOnItem.item);
    }

    private void sendBlackMarketBidOnItemResult(BlackMarketError result, int marketId, ItemInstance item) {
        BlackMarketBidOnItemResult packet = new BlackMarketBidOnItemResult();

        packet.marketID = marketId;
        packet.item = item;
        packet.result = result;

        sendPacket(packet);
    }

    public final void sendCalendarRaidLockoutAdded(InstanceLock instanceLock) {
        CalendarRaidLockoutAdded calendarRaidLockoutAdded = new CalendarRaidLockoutAdded();
        calendarRaidLockoutAdded.instanceID = instanceLock.getInstanceId();
        calendarRaidLockoutAdded.serverTime = (int) gameTime.GetGameTime();
        calendarRaidLockoutAdded.mapID = (int) instanceLock.getMapId();
        calendarRaidLockoutAdded.difficultyID = instanceLock.getDifficultyId();
        calendarRaidLockoutAdded.timeRemaining = (int) (instanceLock.getEffectiveExpiryTime() - gameTime.GetSystemTime()).TotalSeconds;
        sendPacket(calendarRaidLockoutAdded);
    }


    private void handleCalendarGetCalendar(CalendarGetCalendar calendarGetCalendar) {
        var guid = getPlayer().getGUID();

        var currTime = gameTime.GetGameTime();

        CalendarSendCalendar packet = new CalendarSendCalendar();
        packet.serverTime = currTime;

        var invites = global.getCalendarMgr().getPlayerInvites(guid);

        for (var invite : invites) {
            CalendarSendCalendarInviteInfo inviteInfo = new CalendarSendCalendarInviteInfo();
            inviteInfo.eventID = invite.getEventId();
            inviteInfo.inviteID = invite.getInviteId();
            inviteInfo.inviterGuid = invite.getSenderGuid();
            inviteInfo.status = invite.getStatus();
            inviteInfo.moderator = invite.getRank();
            var calendarEvent = global.getCalendarMgr().getEvent(invite.getEventId());

            if (calendarEvent != null) {
                inviteInfo.inviteType = (byte) (calendarEvent.isGuildEvent() && calendarEvent.getGuildId() == player.getGuildId() ? 1 : 0);
            }

            packet.invites.add(inviteInfo);
        }

        var playerEvents = global.getCalendarMgr().getPlayerEvents(guid);

        for (var calendarEvent : playerEvents) {
            CalendarSendCalendarEventInfo eventInfo = new CalendarSendCalendarEventInfo();
            eventInfo.eventID = calendarEvent.getEventId();
            eventInfo.date = calendarEvent.getDate();
            eventInfo.eventClubID = calendarEvent.getGuildId();
            eventInfo.eventName = calendarEvent.getTitle();
            eventInfo.eventType = calendarEvent.getEventType();
            eventInfo.flags = calendarEvent.getFlags();
            eventInfo.ownerGuid = calendarEvent.getOwnerGuid();
            eventInfo.textureID = calendarEvent.getTextureId();

            packet.events.add(eventInfo);
        }

        for (var instanceLock : global.getInstanceLockMgr().getInstanceLocksForPlayer(player.getGUID())) {
            CalendarSendCalendarRaidLockoutInfo lockoutInfo = new CalendarSendCalendarRaidLockoutInfo();

            lockoutInfo.mapID = (int) instanceLock.getMapId();
            lockoutInfo.difficultyID = (int) instanceLock.getDifficultyId().getValue();
            lockoutInfo.expireTime = (int) Math.max((instanceLock.getEffectiveExpiryTime() - gameTime.GetSystemTime()).TotalSeconds, 0);
            lockoutInfo.instanceID = instanceLock.getInstanceId();

            packet.raidLockouts.add(lockoutInfo);
        }

        sendPacket(packet);
    }


    private void handleCalendarGetEvent(CalendarGetEvent calendarGetEvent) {
        var calendarEvent = global.getCalendarMgr().getEvent(calendarGetEvent.eventID);

        if (calendarEvent != null) {
            global.getCalendarMgr().sendCalendarEvent(getPlayer().getGUID(), calendarEvent, CalendarSendEventType.Get);
        } else {
            global.getCalendarMgr().sendCalendarCommandResult(getPlayer().getGUID(), CalendarError.EventInvalid);
        }
    }


    private void handleCalendarCommunityInvite(CalendarCommunityInviteRequest calendarCommunityInvite) {
        var guild = global.getGuildMgr().getGuildById(getPlayer().getGuildId());

        if (guild) {
            guild.massInviteToEvent(this, calendarCommunityInvite.minLevel, calendarCommunityInvite.maxLevel, GuildRankOrder.forValue(calendarCommunityInvite.maxRankOrder));
        }
    }


    private void handleCalendarAddEvent(CalendarAddEvent calendarAddEvent) {
        var guid = getPlayer().getGUID();

        calendarAddEvent.eventInfo.time = time.LocalTimeToUTCTime(calendarAddEvent.eventInfo.time);

        // prevent events in the past
        // To Do: properly handle timezones and remove the "- time_t(86400L)" hack
        if (calendarAddEvent.eventInfo.time < (gameTime.GetGameTime() - 86400L)) {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventPassed);

            return;
        }

        // If the event is a guild event, check if the player is in a guild
        if (CalendarEvent.modifyIsGuildEventFlags(calendarAddEvent.eventInfo.flags) || CalendarEvent.modifyIsGuildAnnouncementFlags(calendarAddEvent.eventInfo.flags)) {
            if (player.getGuildId() == 0) {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.GuildPlayerNotInGuild);

                return;
            }
        }

        // Check if the player reached the max number of events allowed to create
        if (CalendarEvent.modifyIsGuildEventFlags(calendarAddEvent.eventInfo.flags) || CalendarEvent.modifyIsGuildAnnouncementFlags(calendarAddEvent.eventInfo.flags)) {
            if (global.getCalendarMgr().getGuildEvents(player.getGuildId()).size() >= SharedConst.CalendarMaxGuildEvents) {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.GuildEventsExceeded);

                return;
            }
        } else {
            if (global.getCalendarMgr().getEventsCreatedBy(guid).size() >= SharedConst.CalendarMaxEvents) {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventsExceeded);

                return;
            }
        }

        if (getCalendarEventCreationCooldown() > gameTime.GetGameTime()) {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.internal);

            return;
        }

        setCalendarEventCreationCooldown(gameTime.GetGameTime() + SharedConst.CalendarCreateEventCooldown);

        CalendarEvent calendarEvent = new CalendarEvent(global.getCalendarMgr().getFreeEventId(), guid, 0, CalendarEventType.forValue(calendarAddEvent.eventInfo.eventType), calendarAddEvent.eventInfo.textureID, calendarAddEvent.eventInfo.time, CalendarFlags.forValue(calendarAddEvent.eventInfo.flags), calendarAddEvent.eventInfo.title, calendarAddEvent.eventInfo.description, 0);

        if (calendarEvent.isGuildEvent() || calendarEvent.isGuildAnnouncement()) {
            calendarEvent.setGuildId(player.getGuildId());
        }

        if (calendarEvent.isGuildAnnouncement()) {
            CalendarInvite invite = new CalendarInvite(0, calendarEvent.getEventId(), ObjectGuid.Empty, guid, SharedConst.CalendarDefaultResponseTime, CalendarInviteStatus.NotSignedUp, CalendarModerationRank.player, "");
            // WARNING: By passing pointer to a local variable, the underlying method(s) must NOT perform any kind
            // of storage of the pointer as it will lead to memory corruption
            global.getCalendarMgr().addInvite(calendarEvent, invite);
        } else {
            SQLTransaction trans = null;

            if (calendarAddEvent.eventInfo.invites.length > 1) {
                trans = new SQLTransaction();
            }

            for (var i = 0; i < calendarAddEvent.eventInfo.invites.length; ++i) {
                CalendarInvite invite = new CalendarInvite(global.getCalendarMgr().getFreeInviteId(), calendarEvent.getEventId(), calendarAddEvent.eventInfo.Invites[i].guid, guid, SharedConst.CalendarDefaultResponseTime, CalendarInviteStatus.forValue(calendarAddEvent.eventInfo.Invites[i].status), CalendarModerationRank.forValue(calendarAddEvent.eventInfo.Invites[i].moderator), "");

                global.getCalendarMgr().addInvite(calendarEvent, invite, trans);
            }

            if (calendarAddEvent.eventInfo.invites.length > 1) {
                DB.characters.CommitTransaction(trans);
            }
        }

        global.getCalendarMgr().addEvent(calendarEvent, CalendarSendEventType.Add);
    }


    private void handleCalendarUpdateEvent(CalendarUpdateEvent calendarUpdateEvent) {
        var guid = getPlayer().getGUID();
        long oldEventTime;

        calendarUpdateEvent.eventInfo.time = time.LocalTimeToUTCTime(calendarUpdateEvent.eventInfo.time);

        // prevent events in the past
        // To Do: properly handle timezones and remove the "- time_t(86400L)" hack
        if (calendarUpdateEvent.eventInfo.time < (gameTime.GetGameTime() - 86400L)) {
            return;
        }

        var calendarEvent = global.getCalendarMgr().getEvent(calendarUpdateEvent.eventInfo.eventID);

        if (calendarEvent != null) {
            oldEventTime = calendarEvent.getDate();

            calendarEvent.setEventType(CalendarEventType.forValue(calendarUpdateEvent.eventInfo.eventType));
            calendarEvent.setFlags(CalendarFlags.forValue(calendarUpdateEvent.eventInfo.flags));
            calendarEvent.setDate(calendarUpdateEvent.eventInfo.time);
            calendarEvent.setTextureId((int) calendarUpdateEvent.eventInfo.textureID);
            calendarEvent.setTitle(calendarUpdateEvent.eventInfo.title);
            calendarEvent.setDescription(calendarUpdateEvent.eventInfo.description);

            global.getCalendarMgr().updateEvent(calendarEvent);
            global.getCalendarMgr().sendCalendarEventUpdateAlert(calendarEvent, oldEventTime);
        } else {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventInvalid);
        }
    }


    private void handleCalendarRemoveEvent(CalendarRemoveEvent calendarRemoveEvent) {
        var guid = getPlayer().getGUID();
        global.getCalendarMgr().removeEvent(calendarRemoveEvent.eventID, guid);
    }


    private void handleCalendarCopyEvent(CalendarCopyEvent calendarCopyEvent) {
        var guid = getPlayer().getGUID();

        calendarCopyEvent.date = time.LocalTimeToUTCTime(calendarCopyEvent.date);

        // prevent events in the past
        // To Do: properly handle timezones and remove the "- time_t(86400L)" hack
        if (calendarCopyEvent.date < (gameTime.GetGameTime() - 86400L)) {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventPassed);

            return;
        }

        var oldEvent = global.getCalendarMgr().getEvent(calendarCopyEvent.eventID);

        if (oldEvent != null) {
            // Ensure that the player has access to the event
            if (oldEvent.isGuildEvent() || oldEvent.isGuildAnnouncement()) {
                if (oldEvent.getGuildId() != player.getGuildId()) {
                    global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventInvalid);

                    return;
                }
            } else {
                if (ObjectGuid.opNotEquals(oldEvent.getOwnerGuid(), guid)) {
                    global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventInvalid);

                    return;
                }
            }

            // Check if the player reached the max number of events allowed to create
            if (oldEvent.isGuildEvent() || oldEvent.isGuildAnnouncement()) {
                if (global.getCalendarMgr().getGuildEvents(player.getGuildId()).size() >= SharedConst.CalendarMaxGuildEvents) {
                    global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.GuildEventsExceeded);

                    return;
                }
            } else {
                if (global.getCalendarMgr().getEventsCreatedBy(guid).size() >= SharedConst.CalendarMaxEvents) {
                    global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventsExceeded);

                    return;
                }
            }

            if (getCalendarEventCreationCooldown() > gameTime.GetGameTime()) {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.internal);

                return;
            }

            setCalendarEventCreationCooldown(gameTime.GetGameTime() + SharedConst.CalendarCreateEventCooldown);

            CalendarEvent newEvent = new CalendarEvent(oldEvent, global.getCalendarMgr().getFreeEventId());
            newEvent.setDate(calendarCopyEvent.date);
            global.getCalendarMgr().addEvent(newEvent, CalendarSendEventType.Copy);

            var invites = global.getCalendarMgr().getEventInvites(calendarCopyEvent.eventID);
            SQLTransaction trans = null;

            if (invites.size() > 1) {
                trans = new SQLTransaction();
            }

            for (var invite : invites) {
                global.getCalendarMgr().addInvite(newEvent, new CalendarInvite(invite, global.getCalendarMgr().getFreeInviteId(), newEvent.getEventId()), trans);
            }

            if (invites.size() > 1) {
                DB.characters.CommitTransaction(trans);
            }
            // should we change owner when somebody makes a copy of event owned by another person?
        } else {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventInvalid);
        }
    }


    private void handleCalendarInvite(CalendarInvitePkt calendarInvite) {
        var playerGuid = getPlayer().getGUID();

        var inviteeGuid = ObjectGuid.Empty;
        Team inviteeTeam = Team.forValue(0);
        long inviteeGuildId = 0;

        tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(calendarInvite.name);
        if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
            calendarInvite.name = tempRef_Name.refArgValue;
            return;
        } else {
            calendarInvite.name = tempRef_Name.refArgValue;
        }

        var player = global.getObjAccessor().FindPlayerByName(calendarInvite.name);

        if (player) {
            // Invitee is online
            inviteeGuid = player.getGUID();
            inviteeTeam = player.getTeam();
            inviteeGuildId = player.getGuildId();
        } else {
            // Invitee offline, get data from database
            var guid = global.getCharacterCacheStorage().getCharacterGuidByName(calendarInvite.name);

            if (!guid.isEmpty()) {
                var characterInfo = global.getCharacterCacheStorage().getCharacterCacheByGuid(guid);

                if (characterInfo != null) {
                    inviteeGuid = guid;
                    inviteeTeam = player.teamForRace(characterInfo.raceId);
                    inviteeGuildId = characterInfo.guildId;
                }
            }
        }

        if (inviteeGuid.isEmpty()) {
            global.getCalendarMgr().sendCalendarCommandResult(playerGuid, CalendarError.PlayerNotFound);

            return;
        }

        if (getPlayer().getTeam() != inviteeTeam && !WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionCalendar)) {
            global.getCalendarMgr().sendCalendarCommandResult(playerGuid, CalendarError.NotAllied);

            return;
        }

        var result1 = DB.characters.query("SELECT flags FROM character_social WHERE guid = {0} AND friend = {1}", inviteeGuid, playerGuid);

        if (!result1.isEmpty()) {
            if ((boolean) (result1.<Byte>Read(0) & (byte) SocialFlag.Ignored.getValue())) {
                global.getCalendarMgr().sendCalendarCommandResult(playerGuid, CalendarError.IgnoringYouS, calendarInvite.name);

                return;
            }
        }

        if (!calendarInvite.creating) {
            var calendarEvent = global.getCalendarMgr().getEvent(calendarInvite.eventID);

            if (calendarEvent != null) {
                if (calendarEvent.isGuildEvent() && calendarEvent.getGuildId() == inviteeGuildId) {
                    // we can't invite guild members to guild events
                    global.getCalendarMgr().sendCalendarCommandResult(playerGuid, CalendarError.NoGuildInvites);

                    return;
                }

                CalendarInvite invite = new CalendarInvite(global.getCalendarMgr().getFreeInviteId(), calendarInvite.eventID, inviteeGuid, playerGuid, SharedConst.CalendarDefaultResponseTime, CalendarInviteStatus.Invited, CalendarModerationRank.player, "");
                global.getCalendarMgr().addInvite(calendarEvent, invite);
            } else {
                global.getCalendarMgr().sendCalendarCommandResult(playerGuid, CalendarError.EventInvalid);
            }
        } else {
            if (calendarInvite.isSignUp && inviteeGuildId == getPlayer().getGuildId()) {
                global.getCalendarMgr().sendCalendarCommandResult(playerGuid, CalendarError.NoGuildInvites);

                return;
            }

            CalendarInvite invite = new CalendarInvite(calendarInvite.eventID, 0, inviteeGuid, playerGuid, SharedConst.CalendarDefaultResponseTime, CalendarInviteStatus.Invited, CalendarModerationRank.player, "");
            global.getCalendarMgr().sendCalendarEventInvite(invite);
        }
    }


    private void handleCalendarEventSignup(CalendarEventSignUp calendarEventSignUp) {
        var guid = getPlayer().getGUID();

        var calendarEvent = global.getCalendarMgr().getEvent(calendarEventSignUp.eventID);

        if (calendarEvent != null) {
            if (calendarEvent.isGuildEvent() && calendarEvent.getGuildId() != getPlayer().getGuildId()) {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.GuildPlayerNotInGuild);

                return;
            }

            var status = calendarEventSignUp.Tentative ? CalendarInviteStatus.Tentative : CalendarInviteStatus.SignedUp;
            CalendarInvite invite = new CalendarInvite(global.getCalendarMgr().getFreeInviteId(), calendarEventSignUp.eventID, guid, guid, gameTime.GetGameTime(), status, CalendarModerationRank.player, "");
            global.getCalendarMgr().addInvite(calendarEvent, invite);
            global.getCalendarMgr().sendCalendarClearPendingAction(guid);
        } else {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventInvalid);
        }
    }


    private void handleCalendarRsvp(HandleCalendarRsvp calendarRSVP) {
        var guid = getPlayer().getGUID();

        var calendarEvent = global.getCalendarMgr().getEvent(calendarRSVP.eventID);

        if (calendarEvent != null) {
            // i think we still should be able to remove self from locked events
            if (calendarRSVP.status != CalendarInviteStatus.removed && calendarEvent.isLocked()) {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventLocked);

                return;
            }

            var invite = global.getCalendarMgr().getInvite(calendarRSVP.inviteID);

            if (invite != null) {
                invite.setStatus(calendarRSVP.status);
                invite.setResponseTime(gameTime.GetGameTime());

                global.getCalendarMgr().updateInvite(invite);
                global.getCalendarMgr().sendCalendarEventStatus(calendarEvent, invite);
                global.getCalendarMgr().sendCalendarClearPendingAction(guid);
            } else {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.NoInvite); // correct?
            }
        } else {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventInvalid);
        }
    }


    private void handleCalendarEventRemoveInvite(CalendarRemoveInvite calendarRemoveInvite) {
        var guid = getPlayer().getGUID();

        var calendarEvent = global.getCalendarMgr().getEvent(calendarRemoveInvite.eventID);

        if (calendarEvent != null) {
            if (Objects.equals(calendarEvent.getOwnerGuid(), calendarRemoveInvite.guid)) {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.DeleteCreatorFailed);

                return;
            }

            global.getCalendarMgr().removeInvite(calendarRemoveInvite.inviteID, calendarRemoveInvite.eventID, guid);
        } else {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.NoInvite);
        }
    }


    private void handleCalendarStatus(CalendarStatus calendarStatus) {
        var guid = getPlayer().getGUID();

        var calendarEvent = global.getCalendarMgr().getEvent(calendarStatus.eventID);

        if (calendarEvent != null) {
            var invite = global.getCalendarMgr().getInvite(calendarStatus.inviteID);

            if (invite != null) {
                invite.setStatus(CalendarInviteStatus.forValue(calendarStatus.status));

                global.getCalendarMgr().updateInvite(invite);
                global.getCalendarMgr().sendCalendarEventStatus(calendarEvent, invite);
                global.getCalendarMgr().sendCalendarClearPendingAction(calendarStatus.guid);
            } else {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.NoInvite); // correct?
            }
        } else {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventInvalid);
        }
    }


    private void handleCalendarModeratorStatus(CalendarModeratorStatusQuery calendarModeratorStatus) {
        var guid = getPlayer().getGUID();

        var calendarEvent = global.getCalendarMgr().getEvent(calendarModeratorStatus.eventID);

        if (calendarEvent != null) {
            var invite = global.getCalendarMgr().getInvite(calendarModeratorStatus.inviteID);

            if (invite != null) {
                invite.setRank(CalendarModerationRank.forValue(calendarModeratorStatus.status));
                global.getCalendarMgr().updateInvite(invite);
                global.getCalendarMgr().sendCalendarEventModeratorStatusAlert(calendarEvent, invite);
            } else {
                global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.NoInvite); // correct?
            }
        } else {
            global.getCalendarMgr().sendCalendarCommandResult(guid, CalendarError.EventInvalid);
        }
    }


    private void handleCalendarComplain(CalendarComplain calendarComplain) {
        // what to do with complains?
    }


    private void handleCalendarGetNumPending(CalendarGetNumPending calendarGetNumPending) {
        var guid = getPlayer().getGUID();
        var pending = global.getCalendarMgr().getPlayerNumPending(guid);

        sendPacket(new CalendarSendNumPending(pending));
    }


    private void handleSetSavedInstanceExtend(SetSavedInstanceExtend setSavedInstanceExtend) {
        // cannot modify locks currently in use
        if (player.getLocation().getMapId() == setSavedInstanceExtend.mapID) {
            return;
        }

        var expiryTimes = global.getInstanceLockMgr().updateInstanceLockExtensionForPlayer(player.getGUID(), new MapDb2Entries((int) setSavedInstanceExtend.mapID, Difficulty.forValue((byte) setSavedInstanceExtend.difficultyID)), setSavedInstanceExtend.extend);

        if (LocalDateTime.MIN.equals(expiryTimes.Item1)) {
            return;
        }

        CalendarRaidLockoutUpdated calendarRaidLockoutUpdated = new CalendarRaidLockoutUpdated();
        calendarRaidLockoutUpdated.serverTime = gameTime.GetGameTime();
        calendarRaidLockoutUpdated.mapID = setSavedInstanceExtend.mapID;
        calendarRaidLockoutUpdated.difficultyID = setSavedInstanceExtend.difficultyID;
        calendarRaidLockoutUpdated.oldTimeRemaining = (int) Math.max((expiryTimes.Item1 - gameTime.GetSystemTime()).TotalSeconds, 0);
        calendarRaidLockoutUpdated.newTimeRemaining = (int) Math.max((expiryTimes.Item2 - gameTime.GetSystemTime()).TotalSeconds, 0);
        sendPacket(calendarRaidLockoutUpdated);
    }

    private void sendCalendarRaidLockoutRemoved(InstanceLock instanceLock) {
        CalendarRaidLockoutRemoved calendarRaidLockoutRemoved = new CalendarRaidLockoutRemoved();
        calendarRaidLockoutRemoved.instanceID = instanceLock.getInstanceId();
        calendarRaidLockoutRemoved.mapID = (int) instanceLock.getMapId();
        calendarRaidLockoutRemoved.difficultyID = instanceLock.getDifficultyId();
        sendPacket(calendarRaidLockoutRemoved);
    }


    private void handleJoinChannel(JoinChannel packet) {
        var zone = CliDB.AreaTableStorage.get(getPlayer().getZoneId());

        if (packet.chatChannelId != 0) {
            var channel = CliDB.ChatChannelsStorage.get(packet.chatChannelId);

            if (channel == null) {
                return;
            }

            if (zone == null || !getPlayer().canJoinConstantChannelInZone(channel, zone)) {
                return;
            }
        }

        var cMgr = ChannelManager.forTeam(getPlayer().getTeam());

        if (cMgr == null) {
            return;
        }

        if (packet.chatChannelId != 0) {
            // system channel
            var channel = cMgr.getSystemChannel((int) packet.chatChannelId, zone);

            if (channel != null) {
                channel.joinChannel(getPlayer());
            }
        } else {
            // custom channel
            if (packet.channelName.isEmpty() || Character.isDigit(packet.channelName.charAt(0))) {
                ChannelNotify channelNotify = new ChannelNotify();
                channelNotify.type = ChatNotify.InvalidNameNotice;
                channelNotify.channel = packet.channelName;
                sendPacket(channelNotify);

                return;
            }

            if (packet.password.length() > 127) {
                Log.outError(LogFilter.Network, String.format("Player %1$s tried to create a channel with a password more than %2$s character long - blocked", getPlayer().getGUID(), 127));

                return;
            }

            if (!disallowHyperlinksAndMaybeKick(packet.channelName)) {
                return;
            }

            var channel = cMgr.getCustomChannel(packet.channelName);

            if (channel != null) {
                channel.joinChannel(getPlayer(), packet.password);
            } else {
                channel = cMgr.createCustomChannel(packet.channelName);

                if (channel != null) {
                    channel.setPassword(packet.password);
                    channel.joinChannel(getPlayer(), packet.password);
                }
            }
        }
    }


    private void handleLeaveChannel(LeaveChannel packet) {
        if (StringUtil.isEmpty(packet.channelName) && packet.zoneChannelID == 0) {
            return;
        }

        var zone = CliDB.AreaTableStorage.get(getPlayer().getZoneId());

        if (packet.zoneChannelID != 0) {
            var channel = CliDB.ChatChannelsStorage.get(packet.zoneChannelID);

            if (channel == null) {
                return;
            }

            if (zone == null || !getPlayer().canJoinConstantChannelInZone(channel, zone)) {
                return;
            }
        }

        var cMgr = ChannelManager.forTeam(getPlayer().getTeam());

        if (cMgr != null) {
            var channel = cMgr.getChannel((int) packet.zoneChannelID, packet.channelName, getPlayer(), true, zone);

            if (channel != null) {
                channel.leaveChannel(getPlayer(), true);
            }

            if (packet.zoneChannelID != 0) {
                cMgr.leftChannel((int) packet.zoneChannelID, zone);
            }
        }
    }


    private void handleChannelCommand(ChannelCommand packet) {
        var channel = ChannelManager.getChannelForPlayerByNamePart(packet.channelName, getPlayer());

        if (channel == null) {
            return;
        }

        switch (packet.GetOpcode()) {
            case ChatChannelAnnouncements:
                channel.announce(getPlayer());

                break;
            case ChatChannelDeclineInvite:
                channel.declineInvite(getPlayer());

                break;
            case ChatChannelDisplayList:
            case ChatChannelList:
                channel.list(getPlayer());

                break;
            case ChatChannelOwner:
                channel.sendWhoOwner(getPlayer());

                break;
        }
    }


    private void handleChannelPlayerCommand(ChannelPlayerCommand packet) {
        if (packet.name.length() >= 49) {
            Log.outDebug(LogFilter.ChatSystem, "{0} {1} ChannelName: {2}, Name: {3}, Name too long.", packet.GetOpcode(), getPlayerInfo(), packet.channelName, packet.name);

            return;
        }

        tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(packet.name);
        if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
            packet.name = tempRef_Name.refArgValue;
            return;
        } else {
            packet.name = tempRef_Name.refArgValue;
        }

        var channel = ChannelManager.getChannelForPlayerByNamePart(packet.channelName, getPlayer());

        if (channel == null) {
            return;
        }

        switch (packet.GetOpcode()) {
            case ChatChannelBan:
                channel.ban(getPlayer(), packet.name);

                break;
            case ChatChannelInvite:
                channel.invite(getPlayer(), packet.name);

                break;
            case ChatChannelKick:
                channel.kick(getPlayer(), packet.name);

                break;
            case ChatChannelModerator:
                channel.setModerator(getPlayer(), packet.name);

                break;
            case ChatChannelSetOwner:
                channel.setOwner(getPlayer(), packet.name);

                break;
            case ChatChannelSilenceAll:
                channel.silenceAll(getPlayer(), packet.name);

                break;
            case ChatChannelUnban:
                channel.unBan(getPlayer(), packet.name);

                break;
            case ChatChannelUnmoderator:
                channel.unsetModerator(getPlayer(), packet.name);

                break;
            case ChatChannelUnsilenceAll:
                channel.unsilenceAll(getPlayer(), packet.name);

                break;
        }
    }


    private void handleChannelPassword(ChannelPassword packet) {
        if (packet.password.length() > 31) {
            Log.outDebug(LogFilter.ChatSystem, "{0} {1} ChannelName: {2}, Password: {3}, Password too long.", packet.GetOpcode(), getPlayerInfo(), packet.channelName, packet.password);

            return;
        }

        Log.outDebug(LogFilter.ChatSystem, "{0} {1} ChannelName: {2}, Password: {3}", packet.GetOpcode(), getPlayerInfo(), packet.channelName, packet.password);

        var channel = ChannelManager.getChannelForPlayerByNamePart(packet.channelName, getPlayer());

        if (channel != null) {
            channel.password(getPlayer(), packet.password);
        }
    }

    public final boolean meetsChrCustomizationReq(ChrCustomizationReqRecord req, PlayerClass playerClass, boolean checkRequiredDependentChoices, ArrayList<ChrCustomizationChoice> selectedChoices) {
        if (!req.getFlags().hasFlag(ChrCustomizationReqFlag.HasRequirements)) {
            return true;
        }

        if (req.ClassMask != 0 && (req.ClassMask & (1 << (playerClass.getValue() - 1))) == 0) {
            return false;
        }

        if (req.achievementID != 0) {
            return false;
        }

        if (req.itemModifiedAppearanceID != 0 && !getCollectionMgr().hasItemAppearance(req.itemModifiedAppearanceID).PermAppearance) {
            return false;
        }

        if (req.questID != 0) {
            if (!player) {
                return false;
            }

            if (!player.isQuestRewarded((int) req.questID)) {
                return false;
            }
        }

        if (checkRequiredDependentChoices) {
            var requiredChoices = global.getDB2Mgr().GetRequiredCustomizationChoices(req.id);

            if (requiredChoices != null) {
                for (var key : requiredChoices.keySet()) {
                    var hasRequiredChoiceForOption = false;

                    for (var requiredChoice : requiredChoices.get(key)) {
                        if (selectedChoices.Any(choice -> choice.chrCustomizationChoiceID == requiredChoice)) {
                            hasRequiredChoiceForOption = true;

                            break;
                        }
                    }

                    if (!hasRequiredChoiceForOption) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public final boolean validateAppearance(Race race, PlayerClass playerClass, Gender gender, ArrayList<ChrCustomizationChoice> customizations) {
        var options = global.getDB2Mgr().GetCustomiztionOptions(race, gender);

        if (options.isEmpty()) {
            return false;
        }

        int previousOption = 0;

        for (var playerChoice : customizations) {
            // check uniqueness of options
            if (playerChoice.chrCustomizationOptionID == previousOption) {
                return false;
            }

            previousOption = playerChoice.chrCustomizationOptionID;

            // check if we can use this option
            var customizationOptionData = tangible.ListHelper.find(options, option ->
            {
                return option.id == playerChoice.chrCustomizationOptionID;
            });

            // option not found for race/gender combination
            if (customizationOptionData == null) {
                return false;
            }

            var req = CliDB.ChrCustomizationReqStorage.get(customizationOptionData.ChrCustomizationReqID);

            if (req != null) {
                if (!meetsChrCustomizationReq(req, playerClass, false, customizations)) {
                    return false;
                }
            }

            var choicesForOption = global.getDB2Mgr().GetCustomiztionChoices(playerChoice.chrCustomizationOptionID);

            if (choicesForOption.isEmpty()) {
                return false;
            }

            var customizationChoiceData = tangible.ListHelper.find(choicesForOption, choice ->
            {
                return choice.id == playerChoice.chrCustomizationChoiceID;
            });

            // choice not found for option
            if (customizationChoiceData == null) {
                return false;
            }

            var reqEntry = CliDB.ChrCustomizationReqStorage.get(customizationChoiceData.ChrCustomizationReqID);

            if (reqEntry != null) {
                if (!meetsChrCustomizationReq(reqEntry, playerClass, true, customizations)) {
                    return false;
                }
            }
        }

        return true;
    }

    public final void handleContinuePlayerLogin() {
        if (!getPlayerLoading() || getPlayer()) {
            kickPlayer("WorldSession::HandleContinuePlayerLogin incorrect player state when logging in");

            return;
        }

        LoginQueryHolder holder = new LoginQueryHolder(getAccountId(), playerLoading);
        holder.initialize();

        sendPacket(new ResumeComms(Type.INSTANCE));

        addQueryHolderCallback(DB.characters.DelayQueryHolder(holder)).AfterComplete(holder -> handlePlayerLogin((LoginQueryHolder) holder));
    }

    public final void handlePlayerLogin(LoginQueryHolder holder) {
        var playerGuid = holder.getGuid();

        Player pCurrChar = new player(this);

        if (!pCurrChar.loadFromDB(playerGuid, holder)) {
            setPlayer(null);
            kickPlayer("WorldSession::HandlePlayerLogin Player::LoadFromDB failed");
            playerLoading.clear();

            return;
        }

        pCurrChar.setVirtualPlayerRealm(global.getWorldMgr().getVirtualRealmAddress());

        sendAccountDataTimes(ObjectGuid.Empty, AccountDataTypes.GlobalCacheMask);
        sendTutorialsData();

        pCurrChar.getMotionMaster().initialize();
        pCurrChar.sendDungeonDifficulty();

        LoginVerifyWorld loginVerifyWorld = new LoginVerifyWorld();
        loginVerifyWorld.mapID = (int) pCurrChar.getLocation().getMapId();
        loginVerifyWorld.pos = pCurrChar.getLocation();
        sendPacket(loginVerifyWorld);

        // load player specific part before send times
        loadAccountData(holder.GetResult(PlayerLoginQueryLoad.AccountData), AccountDataTypes.PerCharacterCacheMask);

        sendAccountDataTimes(playerGuid, AccountDataTypes.AllAccountDataCacheMask);

        sendFeatureSystemStatus();

        MOTD motd = new MOTD();
        motd.text = global.getWorldMgr().getMotd();
        sendPacket(motd);

        sendSetTimeZoneInformation();

        {
            // Send PVPSeason
            SeasonInfo seasonInfo = new SeasonInfo();
            seasonInfo.previousArenaSeason = (WorldConfig.getIntValue(WorldCfg.ArenaSeasonId) - (WorldConfig.getBoolValue(WorldCfg.ArenaSeasonInProgress) ? 1 : 0));

            if (WorldConfig.getBoolValue(WorldCfg.ArenaSeasonInProgress)) {
                seasonInfo.currentArenaSeason = WorldConfig.getIntValue(WorldCfg.ArenaSeasonId);
            }

            sendPacket(seasonInfo);
        }

        var resultGuild = holder.GetResult(PlayerLoginQueryLoad.guild);

        if (!resultGuild.isEmpty()) {
            pCurrChar.setInGuild(resultGuild.<Integer>Read(0));
            pCurrChar.setGuildRank(resultGuild.<Byte>Read(1));
            var guild = global.getGuildMgr().getGuildById(pCurrChar.getGuildId());

            if (guild) {
                pCurrChar.setGuildLevel(guild.getLevel());
            }
        } else if (pCurrChar.getGuildId() != 0) {
            pCurrChar.setInGuild(0);
            pCurrChar.setGuildRank((byte) 0);
            pCurrChar.setGuildLevel(0);
        }

        // Send stable contents to display icons on Call Pet spells
        if (pCurrChar.hasSpell(SharedConst.CallPetSpellId)) {
            sendStablePet(ObjectGuid.Empty);
        }

        pCurrChar.getSession().getBattlePetMgr().sendJournalLockStatus();

        pCurrChar.sendInitialPacketsBeforeAddToMap();

        //Show cinematic at the first time that player login
        if (pCurrChar.getCinematic() == 0) {
            pCurrChar.setCinematic(1);
            var playerInfo = global.getObjectMgr().getPlayerInfo(pCurrChar.getRace(), pCurrChar.getClass());

            if (playerInfo != null) {
                switch (pCurrChar.getCreateMode()) {
                    case Normal:
                        if (playerInfo.getIntroMovieId() != null) {
                            pCurrChar.sendMovieStart(playerInfo.getIntroMovieId().intValue());
                        } else if (playerInfo.getIntroSceneId() != null) {
                            pCurrChar.getSceneMgr().playScene(playerInfo.getIntroSceneId().intValue());
                        } else {
                            T chrClassesRecord;
                            tangible.OutObject<T> tempOut_chrClassesRecord = new tangible.OutObject<T>();
                            if (CliDB.ChrClassesStorage.TryGetValue((int) pCurrChar.getClass().getValue(), tempOut_chrClassesRecord) && chrClassesRecord.CinematicSequenceID != 0) {
                                chrClassesRecord = tempOut_chrClassesRecord.outArgValue;
                                pCurrChar.sendCinematicStart(chrClassesRecord.CinematicSequenceID);
                            } else {
                                chrClassesRecord = tempOut_chrClassesRecord.outArgValue;
                                T chrRacesRecord;
                                tangible.OutObject<T> tempOut_chrRacesRecord = new tangible.OutObject<T>();
                                if (CliDB.ChrRacesStorage.TryGetValue((int) pCurrChar.getRace().getValue(), tempOut_chrRacesRecord) && chrRacesRecord.CinematicSequenceID != 0) {
                                    chrRacesRecord = tempOut_chrRacesRecord.outArgValue;
                                    pCurrChar.sendCinematicStart(chrRacesRecord.CinematicSequenceID);
                                } else {
                                    chrRacesRecord = tempOut_chrRacesRecord.outArgValue;
                                }
                            }
                        }

                        break;
                    case NPE:
                        if (playerInfo.getIntroSceneIdNpe() != null) {
                            pCurrChar.getSceneMgr().playScene(playerInfo.getIntroSceneIdNpe().intValue());
                        }

                        break;
                    default:
                        break;
                }
            }
        }

        if (!pCurrChar.getMap().addPlayerToMap(pCurrChar)) {
            var at = global.getObjectMgr().getGoBackTrigger(pCurrChar.getLocation().getMapId());

            if (at != null) {
                pCurrChar.teleportTo(at.target_mapId, at.target_X, at.target_Y, at.target_Z, pCurrChar.getLocation().getO());
            } else {
                pCurrChar.teleportTo(pCurrChar.getHomeBind());
            }
        }

        global.getObjAccessor().addObject(pCurrChar);

        if (pCurrChar.getGuildId() != 0) {
            var guild = global.getGuildMgr().getGuildById(pCurrChar.getGuildId());

            if (guild) {
                guild.sendLoginInfo(this);
            } else {
                // remove wrong guild data
                Log.outError(LogFilter.Server, "Player {0} ({1}) marked as member of not existing guild (id: {2}), removing guild membership for player.", pCurrChar.getName(), pCurrChar.getGUID().toString(), pCurrChar.getGuildId());

                pCurrChar.setInGuild(0);
            }
        }

        pCurrChar.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.Login);

        pCurrChar.sendInitialPacketsAfterAddToMap();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_ONLINE);
        stmt.AddValue(0, pCurrChar.getGUID().getCounter());
        DB.characters.execute(stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_ACCOUNT_ONLINE);
        stmt.AddValue(0, getAccountId());
        DB.Login.execute(stmt);

        pCurrChar.setInGameTime(gameTime.GetGameTimeMS());

        // announce group about member online (must be after add to player list to receive announce to self)
        var group = pCurrChar.getGroup();

        if (group) {
            group.sendUpdate();

            if (Objects.equals(group.getLeaderGUID(), pCurrChar.getGUID())) {
                group.stopLeaderOfflineTimer();
            }
        }

        // friend status
        global.getSocialMgr().sendFriendStatus(pCurrChar, FriendsResult.online, pCurrChar.getGUID(), true);

        // Place character in world (and load zone) before some object loading
        pCurrChar.loadCorpse(holder.GetResult(PlayerLoginQueryLoad.CorpseLocation));

        // setting Ghost+speed if dead
        if (pCurrChar.deathState == deathState.Dead) {
            // not blizz like, we must correctly save and load player instead...
            if (pCurrChar.getRace() == race.NightElf && !pCurrChar.hasAura(20584)) {
                pCurrChar.castSpell(pCurrChar, 20584, new CastSpellExtraArgs(true)); // auras SPELL_AURA_INCREASE_SPEED(+speed in wisp form), SPELL_AURA_INCREASE_SWIM_SPEED(+swim speed in wisp form), SPELL_AURA_TRANSFORM (to wisp form)
            }

            if (!pCurrChar.hasAura(8326)) {
                pCurrChar.castSpell(pCurrChar, 8326, new CastSpellExtraArgs(true)); // auras SPELL_AURA_GHOST, SPELL_AURA_INCREASE_SPEED(why?), SPELL_AURA_INCREASE_SWIM_SPEED(why?)
            }

            pCurrChar.setWaterWalking(true);
        }

        pCurrChar.continueTaxiFlight();

        // reset for all pets before pet loading
        if (pCurrChar.hasAtLoginFlag(AtLoginFlags.ResetPetTalents)) {
            // Delete all of the player's pet spells
            var stmtSpells = DB.characters.GetPreparedStatement(CharStatements.DEL_ALL_PET_SPELLS_BY_OWNER);
            stmtSpells.AddValue(0, pCurrChar.getGUID().getCounter());
            DB.characters.execute(stmtSpells);

            // Then reset all of the player's pet specualizations
            var stmtSpec = DB.characters.GetPreparedStatement(CharStatements.UPD_PET_SPECS_BY_OWNER);
            stmtSpec.AddValue(0, pCurrChar.getGUID().getCounter());
            DB.characters.execute(stmtSpec);
        }

        // Load pet if any (if player not alive and in taxi flight or another then pet will remember as temporary unsummoned)
        pCurrChar.resummonPetTemporaryUnSummonedIfAny();

        // Set FFA PvP for non GM in non-rest mode
        if (global.getWorldMgr().isFFAPvPRealm() && !pCurrChar.isGameMaster() && !pCurrChar.hasPlayerFlag(playerFlags.Resting)) {
            pCurrChar.setPvpFlag(UnitPVPStateFlags.FFAPvp);
        }

        if (pCurrChar.hasPlayerFlag(playerFlags.ContestedPVP)) {
            pCurrChar.setContestedPvP();
        }

        // Apply at_login requests
        if (pCurrChar.hasAtLoginFlag(AtLoginFlags.ResetSpells)) {
            pCurrChar.resetSpells();
            sendNotification(SysMessage.ResetSpells);
        }

        if (pCurrChar.hasAtLoginFlag(AtLoginFlags.ResetTalents)) {
            pCurrChar.resetTalents(true);
            pCurrChar.resetTalentSpecialization();
            pCurrChar.sendTalentsInfoData(); // original talents send already in to SendInitialPacketsBeforeAddToMap, resend reset state
            sendNotification(SysMessage.ResetTalents);
        }

        if (pCurrChar.hasAtLoginFlag(AtLoginFlags.firstLogin)) {
            pCurrChar.removeAtLoginFlag(AtLoginFlags.firstLogin);

            var info = global.getObjectMgr().getPlayerInfo(pCurrChar.getRace(), pCurrChar.getClass());

            for (var spellId : info.getCastSpells()[pCurrChar.getCreateMode().getValue()]) {
                pCurrChar.castSpell(pCurrChar, spellId, new CastSpellExtraArgs(true));
            }

            // start with every map explored
            if (WorldConfig.getBoolValue(WorldCfg.StartAllExplored)) {
                for (int i = 0; i < PlayerConst.EXPLOREDZONESSIZE; i++) {
                    pCurrChar.addExploredZones(i, (long) 0xFFFFFFFFFFFFFFFF);
                }
            }

            //Reputations if "StartAllReputation" is enabled
            if (WorldConfig.getBoolValue(WorldCfg.StartAllRep)) {
                var repMgr = pCurrChar.getReputationMgr();
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(942), 42999, false); // Cenarion Expedition
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(935), 42999, false); // The Sha'tar
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(936), 42999, false); // Shattrath City
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1011), 42999, false); // Lower City
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(970), 42999, false); // Sporeggar
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(967), 42999, false); // The Violet Eye
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(989), 42999, false); // Keepers of Time
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(932), 42999, false); // The Aldor
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(934), 42999, false); // The Scryers
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1038), 42999, false); // Ogri'la
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1077), 42999, false); // Shattered Sun Offensive
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1106), 42999, false); // Argent Crusade
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1104), 42999, false); // Frenzyheart Tribe
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1090), 42999, false); // Kirin Tor
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1098), 42999, false); // Knights of the Ebon Blade
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1156), 42999, false); // The Ashen Verdict
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1073), 42999, false); // The Kalu'ak
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1105), 42999, false); // The Oracles
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1119), 42999, false); // The Sons of Hodir
                repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1091), 42999, false); // The Wyrmrest Accord

                // Factions depending on team, like cities and some more stuff
                switch (pCurrChar.getTeam()) {
                    case Alliance:
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(72), 42999, false); // Stormwind
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(47), 42999, false); // Ironforge
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(69), 42999, false); // Darnassus
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(930), 42999, false); // Exodar
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(730), 42999, false); // Stormpike Guard
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(978), 42999, false); // Kurenai
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(54), 42999, false); // Gnomeregan Exiles
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(946), 42999, false); // Honor Hold
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1037), 42999, false); // Alliance Vanguard
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1068), 42999, false); // Explorers' League
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1126), 42999, false); // The Frostborn
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1094), 42999, false); // The Silver Covenant
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1050), 42999, false); // Valiance Expedition

                        break;
                    case Horde:
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(76), 42999, false); // Orgrimmar
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(68), 42999, false); // Undercity
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(81), 42999, false); // Thunder Bluff
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(911), 42999, false); // Silvermoon City
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(729), 42999, false); // Frostwolf Clan
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(941), 42999, false); // The Mag'har
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(530), 42999, false); // Darkspear Trolls
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(947), 42999, false); // Thrallmar
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1052), 42999, false); // Horde Expedition
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1067), 42999, false); // The Hand of Vengeance
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1124), 42999, false); // The Sunreavers
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1064), 42999, false); // The Taunka
                        repMgr.setOneFactionReputation(CliDB.FactionStorage.get(1085), 42999, false); // Warsong Offensive

                        break;
                    default:
                        break;
                }

                repMgr.sendState(null);
            }
        }

        // show time before shutdown if shutdown planned.
        if (global.getWorldMgr().isShuttingDown()) {
            global.getWorldMgr().shutdownMsg(true, pCurrChar);
        }

        if (WorldConfig.getBoolValue(WorldCfg.AllTaxiPaths)) {
            pCurrChar.setTaxiCheater(true);
        }

        if (pCurrChar.isGameMaster()) {
            sendNotification(SysMessage.GmOn);
        }

        var IP_str = getRemoteAddress();
        Log.outDebug(LogFilter.Network, String.format("Account: %1$s (IP: %2$s) Login Character: [%3$s] (%4$s) Level: %5$s, XP: %6$s/%7$s (%8$s left)", getAccountId(), getRemoteAddress(), pCurrChar.getName(), pCurrChar.getGUID(), pCurrChar.getLevel(), player.getXP(), player.getXPForNextLevel(), player.getXPForNextLevel() - player.getXP()));

        if (!pCurrChar.isStandState() && !pCurrChar.hasUnitState(UnitState.Stunned)) {
            pCurrChar.setStandState(UnitStandStateType.Stand);
        }

        pCurrChar.updateAverageItemLevelTotal();
        pCurrChar.updateAverageItemLevelEquipped();

        playerLoading.clear();

        // Handle Login-Achievements (should be handled after loading)
        player.updateCriteria(CriteriaType.Login, 1);

        global.getScriptMgr().<IPlayerOnLogin>ForEach(p -> p.onLogin(pCurrChar));
    }

    public final void abortLogin(LoginFailureReason reason) {
        if (!getPlayerLoading() || getPlayer()) {
            kickPlayer("WorldSession::AbortLogin incorrect player state when logging in");

            return;
        }

        playerLoading.clear();
        sendPacket(new CharacterLoginFailed(reason));
    }

    public final void sendFeatureSystemStatus() {
        FeatureSystemStatus features = new FeatureSystemStatus();

        // START OF DUMMY VALUES
        features.complaintStatus = (byte) complaintStatus.EnabledWithAutoIgnore.getValue();
        features.twitterPostThrottleLimit = 60;
        features.twitterPostThrottleCooldown = 20;
        features.cfgRealmID = 2;
        features.cfgRealmRecID = 0;
        features.tokenPollTimeSeconds = 300;
        features.voiceEnabled = false;
        features.browserEnabled = false; // Has to be false, otherwise client will crash if "Customer Support" is opened

        EuropaTicketConfig europaTicketSystemStatus = new EuropaTicketConfig();
        europaTicketSystemStatus.throttleState.maxTries = 10;
        europaTicketSystemStatus.throttleState.perMilliseconds = 60000;
        europaTicketSystemStatus.throttleState.tryCount = 1;
        europaTicketSystemStatus.throttleState.lastResetTimeBeforeNow = 111111;
        features.tutorialsEnabled = true;
        features.NPETutorialsEnabled = true;
        // END OF DUMMY VALUES

        europaTicketSystemStatus.ticketsEnabled = WorldConfig.getBoolValue(WorldCfg.SupportTicketsEnabled);
        europaTicketSystemStatus.bugsEnabled = WorldConfig.getBoolValue(WorldCfg.SupportBugsEnabled);
        europaTicketSystemStatus.complaintsEnabled = WorldConfig.getBoolValue(WorldCfg.SupportComplaintsEnabled);
        europaTicketSystemStatus.suggestionsEnabled = WorldConfig.getBoolValue(WorldCfg.SupportSuggestionsEnabled);

        features.europaTicketSystemStatus = europaTicketSystemStatus;

        features.charUndeleteEnabled = WorldConfig.getBoolValue(WorldCfg.FeatureSystemCharacterUndeleteEnabled);
        features.bpayStoreEnabled = WorldConfig.getBoolValue(WorldCfg.FeatureSystemBpayStoreEnabled);
        features.warModeFeatureEnabled = WorldConfig.getBoolValue(WorldCfg.FeatureSystemWarModeEnabled);
        features.isMuted = !getCanSpeak();


        features.textToSpeechFeatureEnabled = false;

        sendPacket(features);
    }


    private void handleCharEnum(EnumCharacters charEnum) {
        // remove expired bans
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_EXPIRED_BANS);
        DB.characters.execute(stmt);

        // get all the data necessary for loading all character (along with their pets) on the account
        EnumCharactersQueryHolder holder = new EnumCharactersQueryHolder();

        if (!holder.initialize(getAccountId(), WorldConfig.getBoolValue(WorldCfg.DeclinedNamesUsed), false)) {
            handleCharEnum(holder);

            return;
        }

        addQueryHolderCallback(DB.characters.DelayQueryHolder(holder)).AfterComplete(result -> handleCharEnum((EnumCharactersQueryHolder) result));
    }

    private void handleCharEnum(EnumCharactersQueryHolder holder) {
        EnumCharactersResult charResult = new EnumCharactersResult();
        charResult.success = true;
        charResult.isDeletedCharacters = holder.isDeletedCharacters();
        charResult.disabledClassesMask = WorldConfig.getUIntValue(WorldCfg.CharacterCreatingDisabledClassmask);

        if (!charResult.isDeletedCharacters) {
            legitCharacters.clear();
        }

        MultiMap<Long, ChrCustomizationChoice> customizations = new MultiMap<Long, ChrCustomizationChoice>();
        var customizationsResult = holder.GetResult(EnumCharacterQueryLoad.customizations);

        if (!customizationsResult.isEmpty()) {
            do {
                ChrCustomizationChoice choice = new ChrCustomizationChoice();
                choice.chrCustomizationOptionID = customizationsResult.<Integer>Read(1);
                choice.chrCustomizationChoiceID = customizationsResult.<Integer>Read(2);
                customizations.add(customizationsResult.<Long>Read(0), choice);
            } while (customizationsResult.NextRow());
        }

        var result = holder.GetResult(EnumCharacterQueryLoad.characters);

        if (!result.isEmpty()) {
            do {
                EnumCharactersResult.CharacterInfo charInfo = new EnumCharactersResult.CharacterInfo(result.GetFields());

                var customizationsForChar = customizations.get(charInfo.guid.getCounter());

                if (!customizationsForChar.isEmpty()) {
                    charInfo.customizations = new Array<ChrCustomizationChoice>(customizationsForChar.toArray(new ChrCustomizationChoice[0]));
                }

                Log.outDebug(LogFilter.Network, "Loading Character {0} from account {1}.", charInfo.guid.toString(), getAccountId());

                if (!charResult.isDeletedCharacters) {
                    if (!validateAppearance(race.forValue(charInfo.raceId), charInfo.classId, gender.forValue((byte) charInfo.sexId), charInfo.customizations)) {
                        Log.outError(LogFilter.player, "Player {0} has wrong Appearance values (Hair/Skin/Color), forcing recustomize", charInfo.guid.toString());

                        charInfo.customizations.clear();

                        if (charInfo.flags2 != CharacterCustomizeFlags.Customize) {
                            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ADD_AT_LOGIN_FLAG);
                            stmt.AddValue(0, (short) AtLoginFlags.Customize.getValue());
                            stmt.AddValue(1, charInfo.guid.getCounter());
                            DB.characters.execute(stmt);
                            charInfo.flags2 = CharacterCustomizeFlags.Customize;
                        }
                    }

                    // Do not allow locked character to login
                    if (!charInfo.flags.hasFlag(CharacterFlags.CharacterLockedForTransfer.getValue() | CharacterFlags.LockedByBilling.getValue())) {
                        legitCharacters.add(charInfo.guid);
                    }
                }

                if (!global.getCharacterCacheStorage().hasCharacterCacheEntry(charInfo.guid)) // This can happen if character are inserted into the database manually. Core hasn't loaded name data yet.
                {
                    global.getCharacterCacheStorage().addCharacterCacheEntry(charInfo.guid, getAccountId(), charInfo.name, charInfo.sexId, charInfo.raceId, (byte) charInfo.classId.getValue(), charInfo.experienceLevel, false);
                }

                charResult.maxCharacterLevel = Math.max(charResult.maxCharacterLevel, charInfo.experienceLevel);

                charResult.characters.add(charInfo);
            } while (result.NextRow() && charResult.characters.size() < 200);
        }

        charResult.isAlliedRacesCreationAllowed = canAccessAlliedRaces();

        for (var requirement : global.getObjectMgr().getRaceUnlockRequirements().entrySet()) {
            EnumCharactersResult.RaceUnlock raceUnlock = new EnumCharactersResult.RaceUnlock();
            raceUnlock.raceID = requirement.getKey();
            raceUnlock.hasExpansion = ConfigMgr.GetDefaultValue("character.EnforceRaceAndClassExpansions", true) ? (byte) getAccountExpansion().getValue() >= requirement.getValue().Expansion : true;
            raceUnlock.hasAchievement = (WorldConfig.getBoolValue(WorldCfg.CharacterCreatingDisableAlliedRaceAchievementRequirement) ? true : requirement.getValue().achievementId != 0 ? false : true); // TODO: fix false here for actual check of criteria.

            charResult.raceUnlockData.add(raceUnlock);
        }

        sendPacket(charResult);
    }


    private void handleCharUndeleteEnum(EnumCharacters enumCharacters) {
        // get all the data necessary for loading all undeleted character (along with their pets) on the account
        EnumCharactersQueryHolder holder = new EnumCharactersQueryHolder();

        if (!holder.initialize(getAccountId(), WorldConfig.getBoolValue(WorldCfg.DeclinedNamesUsed), true)) {
            handleCharEnum(holder);

            return;
        }

        addQueryHolderCallback(DB.characters.DelayQueryHolder(holder)).AfterComplete(result -> handleCharEnum((EnumCharactersQueryHolder) result));
    }

    private void handleCharUndeleteEnumCallback(SQLResult result) {
        EnumCharactersResult charEnum = new EnumCharactersResult();
        charEnum.success = true;
        charEnum.isDeletedCharacters = true;
        charEnum.disabledClassesMask = WorldConfig.getUIntValue(WorldCfg.CharacterCreatingDisabledClassmask);

        if (!result.isEmpty()) {
            do {
                EnumCharactersResult.CharacterInfo charInfo = new EnumCharactersResult.CharacterInfo(result.GetFields());

                Log.outInfo(LogFilter.Network, "Loading undeleted char guid {0} from account {1}.", charInfo.guid.toString(), getAccountId());

                if (!global.getCharacterCacheStorage().hasCharacterCacheEntry(charInfo.guid)) // This can happen if character are inserted into the database manually. Core hasn't loaded name data yet.
                {
                    global.getCharacterCacheStorage().addCharacterCacheEntry(charInfo.guid, getAccountId(), charInfo.name, charInfo.sexId, charInfo.raceId, (byte) charInfo.classId.getValue(), charInfo.experienceLevel, true);
                }

                charEnum.characters.add(charInfo);
            } while (result.NextRow());
        }

        sendPacket(charEnum);
    }


    private void handleCharCreate(CreateCharacter charCreate) {
        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationTeammask)) {
            var mask = WorldConfig.getIntValue(WorldCfg.CharacterCreatingDisabled);

            if (mask != 0) {
                var disabled = false;

                var team = player.teamIdForRace(charCreate.createInfo.raceId);

                switch (team) {
                    case TeamId.ALLIANCE:
                        disabled = (boolean) (mask & (1 << 0));

                        break;
                    case TeamId.HORDE:
                        disabled = (boolean) (mask & (1 << 1));

                        break;
                    case TeamIds.Neutral:
                        disabled = (boolean) (mask & (1 << 2));

                        break;
                }

                if (disabled) {
                    sendCharCreate(ResponseCodes.CharCreateDisabled);

                    return;
                }
            }
        }

        var classEntry = CliDB.ChrClassesStorage.get(charCreate.createInfo.classId);

        if (classEntry == null) {
            Log.outError(LogFilter.Network, "Class ({0}) not found in DBC while creating new char for account (ID: {1}): wrong DBC files or cheater?", charCreate.createInfo.classId, getAccountId());
            sendCharCreate(ResponseCodes.CharCreateFailed);

            return;
        }

        var raceEntry = CliDB.ChrRacesStorage.get(charCreate.createInfo.raceId);

        if (raceEntry == null) {
            Log.outError(LogFilter.Network, "Race ({0}) not found in DBC while creating new char for account (ID: {1}): wrong DBC files or cheater?", charCreate.createInfo.raceId, getAccountId());
            sendCharCreate(ResponseCodes.CharCreateFailed);

            return;
        }

        if (ConfigMgr.GetDefaultValue("character.EnforceRaceAndClassExpansions", true)) {
            // prevent character creating Expansion race without Expansion account
            var raceExpansionRequirement = global.getObjectMgr().getRaceUnlockRequirement(charCreate.createInfo.raceId);

            if (raceExpansionRequirement == null) {
                Log.outError(LogFilter.Cheat, String.format("Account %1$s tried to create character with unavailable race %2$s", getAccountId(), charCreate.createInfo.raceId));
                sendCharCreate(ResponseCodes.CharCreateFailed);

                return;
            }

            if (raceExpansionRequirement.expansion > (byte) getAccountExpansion().getValue()) {
                Log.outError(LogFilter.Cheat, String.format("Expansion %1$s account:[%2$s] tried to Create character with expansion %3$s race (%4$s)", getAccountExpansion(), getAccountId(), raceExpansionRequirement.expansion, charCreate.createInfo.raceId));
                sendCharCreate(ResponseCodes.CharCreateExpansion);

                return;
            }

            //if (raceExpansionRequirement.achievementId && !)
            //{
            //    TC_LOG_ERROR("entities.player.cheat", "Expansion %u account:[%d] tried to Create character without achievement %u race (%u)",
            //        GetAccountExpansion(), getAccountId(), raceExpansionRequirement.achievementId, charCreate.createInfo.race);
            //    sendCharCreate(CHAR_CREATE_ALLIED_RACE_ACHIEVEMENT);
            //    return;
            //}

            // prevent character creating Expansion race without Expansion account
            var raceClassExpansionRequirement = global.getObjectMgr().getClassExpansionRequirement(charCreate.createInfo.raceId, charCreate.createInfo.classId);

            if (raceClassExpansionRequirement != null) {
                if (raceClassExpansionRequirement.activeExpansionLevel > (byte) getExpansion().getValue() || raceClassExpansionRequirement.accountExpansionLevel > (byte) getAccountExpansion().getValue()) {
                    Log.outError(LogFilter.Cheat, String.format("Account:[%1$s] tried to create character with race/class %2$s/%3$s without required expansion ", getAccountId(), charCreate.createInfo.raceId, charCreate.createInfo.classId) + String.format("(had %1$s/%2$s, required %3$s/%4$s)", getExpansion(), getAccountExpansion(), raceClassExpansionRequirement.activeExpansionLevel, raceClassExpansionRequirement.accountExpansionLevel));

                    sendCharCreate(ResponseCodes.CharCreateExpansionClass);

                    return;
                }
            } else {
                var classExpansionRequirement = global.getObjectMgr().getClassExpansionRequirementFallback((byte) charCreate.createInfo.classId.getValue());

                if (classExpansionRequirement != null) {
                    if (classExpansionRequirement.minActiveExpansionLevel > (byte) getExpansion().getValue() || classExpansionRequirement.accountExpansionLevel > (byte) getAccountExpansion().getValue()) {
                        Log.outError(LogFilter.Cheat, String.format("Account:[%1$s] tried to create character with race/class %2$s/%3$s without required expansion ", getAccountId(), charCreate.createInfo.raceId, charCreate.createInfo.classId) + String.format("(had %1$s/%2$s, required %3$s/%4$s)", getExpansion(), getAccountExpansion(), classExpansionRequirement.activeExpansionLevel, classExpansionRequirement.accountExpansionLevel));

                        sendCharCreate(ResponseCodes.CharCreateExpansionClass);

                        return;
                    }
                } else {
                    Log.outError(LogFilter.Cheat, String.format("Expansion %1$s account:[%2$s] tried to Create character for race/class combination that is missing requirements in db (%3$s/%4$s)", getAccountExpansion(), getAccountId(), charCreate.createInfo.raceId, charCreate.createInfo.classId));
                }
            }
        }

        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationRacemask)) {
            if (raceEntry.getFlags().hasFlag(ChrRacesFlag.NPCOnly)) {
                Log.outError(LogFilter.Network, String.format("Race (%1$s) was not playable but requested while creating new char for account (ID: %2$s): wrong DBC files or cheater?", charCreate.createInfo.raceId, getAccountId()));
                sendCharCreate(ResponseCodes.CharCreateDisabled);

                return;
            }

            var raceMaskDisabled = WorldConfig.getUInt64Value(WorldCfg.CharacterCreatingDisabledRacemask);

            if ((boolean) ((long) SharedConst.GetMaskForRace(charCreate.createInfo.raceId) & raceMaskDisabled)) {
                sendCharCreate(ResponseCodes.CharCreateDisabled);

                return;
            }
        }

        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationClassmask)) {
            var classMaskDisabled = WorldConfig.getIntValue(WorldCfg.CharacterCreatingDisabledClassmask);

            if ((boolean) ((1 << (charCreate.createInfo.classId.getValue() - 1)) & classMaskDisabled)) {
                sendCharCreate(ResponseCodes.CharCreateDisabled);

                return;
            }
        }

        // prevent character creating with invalid name
        tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(charCreate.createInfo.name);
        if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
            charCreate.createInfo.name = tempRef_Name.refArgValue;
            Log.outError(LogFilter.Network, "Account:[{0}] but tried to Create character with empty [name] ", getAccountId());
            sendCharCreate(ResponseCodes.CharNameNoName);

            return;
        } else {
            charCreate.createInfo.name = tempRef_Name.refArgValue;
        }

        // check name limitations
        var res = ObjectManager.checkPlayerName(charCreate.createInfo.name, getSessionDbcLocale(), true);

        if (res != ResponseCodes.CharNameSuccess) {
            sendCharCreate(res);

            return;
        }

        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationReservedname) && global.getObjectMgr().isReservedName(charCreate.createInfo.name)) {
            sendCharCreate(ResponseCodes.CharNameReserved);

            return;
        }

        var createInfo = charCreate.createInfo;
        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHECK_NAME);
        stmt.AddValue(0, charCreate.createInfo.name);

        queryProcessor.AddCallback(DB.characters.AsyncQuery(stmt).WithChainingCallback((queryCallback, result) ->
        {
            if (!result.isEmpty()) {
                sendCharCreate(ResponseCodes.CharCreateNameInUse);

                return;
            }

            stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_SUM_REALM_CHARACTERS);
            stmt.AddValue(0, getAccountId());
            queryCallback.SetNextQuery(DB.Login.AsyncQuery(stmt));
        }).WithChainingCallback((queryCallback, result) ->
        {
            long acctCharCount = 0;

            if (!result.isEmpty()) {
                acctCharCount = result.<Long>Read(0);
            }

            if (acctCharCount >= WorldConfig.getUIntValue(WorldCfg.CharactersPerAccount)) {
                sendCharCreate(ResponseCodes.CharCreateAccountLimit);

                return;
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_SUM_CHARS);
            stmt.AddValue(0, getAccountId());
            queryCallback.SetNextQuery(DB.characters.AsyncQuery(stmt));
        }).WithChainingCallback((queryCallback, result) ->
        {
            if (!result.isEmpty()) {
                createInfo.charCount = (Byte) result.<Long>Read(0); // SQL's COUNT() returns uint64 but it will always be less than uint8.Max

                if (createInfo.charCount >= WorldConfig.getIntValue(WorldCfg.CharactersPerRealm)) {
                    sendCharCreate(ResponseCodes.CharCreateServerLimit);

                    return;
                }
            }

            var demonHunterReqLevel = WorldConfig.getIntValue(WorldCfg.CharacterCreatingMinLevelForDemonHunter);
            var hasDemonHunterReqLevel = demonHunterReqLevel == 0;
            var evokerReqLevel = WorldConfig.getUIntValue(WorldCfg.CharacterCreatingMinLevelForEvoker);
            var hasEvokerReqLevel = (evokerReqLevel == 0);
            var allowTwoSideAccounts = !global.getWorldMgr().isPvPRealm() || hasPermission(RBACPermissions.TwoSideCharacterCreation);
            var skipCinematics = WorldConfig.getIntValue(WorldCfg.SkipCinematics);
            var checkClassLevelReqs = (createInfo.classId == playerClass.DemonHunter || createInfo.classId == playerClass.Evoker) && !hasPermission(RBACPermissions.SkipCheckCharacterCreationDemonHunter);
            var evokerLimit = WorldConfig.getIntValue(WorldCfg.CharacterCreatingEvokersPerRealm);
            var hasEvokerLimit = evokerLimit != 0;

            void finalizeCharacterCreation (SQLResult result1)
            {
                var haveSameRace = false;

                if (result1 != null && !result1.isEmpty() && result.GetFieldCount() >= 3) {
                    var team = player.teamForRace(createInfo.raceId);
                    var accRace = result1.<Byte>Read(1);
                    var accClass = result1.<Byte>Read(2);

                    if (checkClassLevelReqs) {
                        if (!hasDemonHunterReqLevel) {
                            var accLevel = result1.<Byte>Read(0);

                            if (accLevel >= demonHunterReqLevel) {
                                hasDemonHunterReqLevel = true;
                            }
                        }

                        if (!hasEvokerReqLevel) {
                            var accLevel = result1.<Byte>Read(0);

                            if (accLevel >= evokerReqLevel) {
                                hasEvokerReqLevel = true;
                            }
                        }
                    }

                    if (accClass == (byte) playerClass.Evoker.getValue()) {
                        --evokerLimit;
                    }

                    // need to check team only for first character
                    // @todo what to if account already has character of both races?
                    if (!allowTwoSideAccounts) {
                        Team accTeam = Team.forValue(0);

                        if (accRace > 0) {
                            accTeam = player.teamForRace(race.forValue(accRace));
                        }

                        if (accTeam != team) {
                            sendCharCreate(ResponseCodes.CharCreatePvpTeamsViolation);

                            return;
                        }
                    }

                    // search same race for cinematic or same class if need
                    // @todo check if cinematic already shown? (already logged in?; cinematic field)
                    while ((skipCinematics == 1 && !haveSameRace) || createInfo.classId == playerClass.DemonHunter || createInfo.classId == playerClass.Evoker) {
                        if (!result1.NextRow()) {
                            break;
                        }

                        accRace = result1.<Byte>Read(1);
                        accClass = result1.<Byte>Read(2);

                        if (!haveSameRace) {
                            haveSameRace = createInfo.raceId == race.forValue(accRace);
                        }

                        if (checkClassLevelReqs) {
                            if (!hasDemonHunterReqLevel) {
                                var acc_level = result1.<Byte>Read(0);

                                if (acc_level >= demonHunterReqLevel) {
                                    hasDemonHunterReqLevel = true;
                                }
                            }

                            if (!hasEvokerReqLevel) {
                                var accLevel = result1.<Byte>Read(0);

                                if (accLevel >= evokerReqLevel) {
                                    hasEvokerReqLevel = true;
                                }
                            }
                        }

                        if (accClass == (byte) playerClass.Evoker.getValue()) {
                            --evokerLimit;
                        }
                    }
                }

                if (checkClassLevelReqs) {
                    if (!hasDemonHunterReqLevel) {
                        sendCharCreate(ResponseCodes.CharCreateNewPlayer);

                        return;
                    }

                    if (!hasEvokerReqLevel) {
                        sendCharCreate(ResponseCodes.CharCreateDracthyrLevelRequirement);

                        return;
                    }
                }

                if (createInfo.classId == playerClass.Evoker && hasEvokerLimit && evokerLimit < 1) {
                    sendCharCreate(ResponseCodes.CharCreateNewPlayer);

                    return;
                }

                // Check name uniqueness in the same step as saving to database
                if (global.getCharacterCacheStorage().getCharacterCacheByName(createInfo.name) != null) {
                    sendCharCreate(ResponseCodes.CharCreateDracthyrDuplicate);

                    return;
                }

                Player newChar = new player(this);
                newChar.getMotionMaster().initialize();

                if (!newChar.create(global.getObjectMgr().getGenerator(HighGuid.Player).generate(), createInfo)) {
                    // Player not create (race/class/etc problem?)
                    newChar.cleanupsBeforeDelete();
                    newChar.destroy();
                    sendCharCreate(ResponseCodes.CharCreateError);

                    return;
                }

                if ((haveSameRace && skipCinematics == 1) || skipCinematics == 2) {
                    newChar.setCinematic(1); // not show intro
                }

                newChar.setLoginFlags(AtLoginFlags.firstLogin); // First login

                SQLTransaction characterTransaction = new SQLTransaction();
                SQLTransaction loginTransaction = new SQLTransaction();

                // Player created, save it now
                newChar.saveToDB(loginTransaction, characterTransaction, true);
                createInfo.charCount += 1;

                stmt = DB.Login.GetPreparedStatement(LoginStatements.REP_REALM_CHARACTERS);
                stmt.AddValue(0, createInfo.charCount);
                stmt.AddValue(1, getAccountId());
                stmt.AddValue(2, global.getWorldMgr().getRealm().id.index);
                loginTransaction.append(stmt);

                DB.Login.CommitTransaction(loginTransaction);

                addTransactionCallback(DB.characters.AsyncCommitTransaction(characterTransaction)).AfterComplete(success ->
                {
                    if (success) {
                        Log.outInfo(LogFilter.player, "Account: {0} (IP: {1}) Create Character: {2} {3}", getAccountId(), getRemoteAddress(), createInfo.name, newChar.getGUID().toString());
                        global.getScriptMgr().<IPlayerOnCreate>ForEach(newChar.getClass(), p -> p.onCreate(newChar));
                        global.getCharacterCacheStorage().addCharacterCacheEntry(newChar.getGUID(), getAccountId(), newChar.getName(), (byte) newChar.getNativeGender().getValue(), (byte) newChar.getRace().getValue(), (byte) newChar.getClass().getValue(), (byte) newChar.getLevel(), false);

                        sendCharCreate(ResponseCodes.CharCreateSuccess, newChar.getGUID());
                    } else {
                        sendCharCreate(ResponseCodes.CharCreateError);
                    }

                    newChar.cleanupsBeforeDelete();
                    newChar.destroy();
                });
            }

            if (!allowTwoSideAccounts || skipCinematics == 1 || createInfo.classId == playerClass.DemonHunter) {
                finalizeCharacterCreation(new SQLResult());

                return;
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_CREATE_INFO);
            stmt.AddValue(0, getAccountId());
            stmt.AddValue(1, (skipCinematics == 1 || createInfo.classId == playerClass.DemonHunter || createInfo.classId == playerClass.Evoker) ? 1200 : 1); // 200 (max chars per realm) + 1000 (max deleted chars per realm)
            queryCallback.WithCallback(finalizeCharacterCreation).SetNextQuery(DB.characters.AsyncQuery(stmt));
        }));
    }


    private void handleCharDelete(CharDelete charDelete) {
        // Initiating
        var initAccountId = getAccountId();

        // can't delete loaded character
        if (global.getObjAccessor().findPlayer(charDelete.guid)) {
            global.getScriptMgr().<IPlayerOnFailedDelete>ForEach(p -> p.OnFailedDelete(charDelete.guid, initAccountId));

            return;
        }

        // is guild leader
        if (global.getGuildMgr().getGuildByLeader(charDelete.guid)) {
            global.getScriptMgr().<IPlayerOnFailedDelete>ForEach(p -> p.OnFailedDelete(charDelete.guid, initAccountId));
            sendCharDelete(ResponseCodes.CharDeleteFailedGuildLeader);

            return;
        }

        // is arena team captain
        if (global.getArenaTeamMgr().getArenaTeamByCaptain(charDelete.guid) != null) {
            global.getScriptMgr().<IPlayerOnFailedDelete>ForEach(p -> p.OnFailedDelete(charDelete.guid, initAccountId));
            sendCharDelete(ResponseCodes.CharDeleteFailedArenaCaptain);

            return;
        }

        var characterInfo = global.getCharacterCacheStorage().getCharacterCacheByGuid(charDelete.guid);

        if (characterInfo == null) {
            global.getScriptMgr().<IPlayerOnFailedDelete>ForEach(p -> p.OnFailedDelete(charDelete.guid, initAccountId));

            return;
        }

        var accountId = characterInfo.accountId;
        var name = characterInfo.name;
        var level = characterInfo.level;

        // prevent deleting other players' character using cheating tools
        if (accountId != getAccountId()) {
            global.getScriptMgr().<IPlayerOnFailedDelete>ForEach(p -> p.OnFailedDelete(charDelete.guid, initAccountId));

            return;
        }

        var IP_str = getRemoteAddress();
        Log.outInfo(LogFilter.player, "Account: {0}, IP: {1} deleted character: {2}, {3}, Level: {4}", accountId, IP_str, name, charDelete.guid.toString(), level);

        // To prevent hook failure, place hook before removing reference from DB
        global.getScriptMgr().<IPlayerOnDelete>ForEach(p -> p.OnDelete(charDelete.guid, initAccountId)); // To prevent race conditioning, but as it also makes sense, we hand the accountId over for successful delete.

        // Shouldn't interfere with character deletion though

        global.getCalendarMgr().removeAllPlayerEventsAndInvites(charDelete.guid);
        player.deleteFromDB(charDelete.guid, accountId);

        sendCharDelete(ResponseCodes.CharDeleteSuccess);
    }


    private void handleRandomizeCharName(GenerateRandomCharacterName packet) {
        if (!player.isValidRace(race.forValue(packet.race))) {
            Log.outError(LogFilter.Network, "Invalid race ({0}) sent by accountId: {1}", packet.race, getAccountId());

            return;
        }

        if (!player.isValidGender(gender.forValue((byte) packet.sex))) {
            Log.outError(LogFilter.Network, "Invalid gender ({0}) sent by accountId: {1}", packet.sex, getAccountId());

            return;
        }

        GenerateRandomCharacterNameResult result = new GenerateRandomCharacterNameResult();
        result.success = true;
        result.name = global.getDB2Mgr().GetNameGenEntry(packet.race, packet.sex);

        sendPacket(result);
    }


    private void handleReorderCharacters(ReorderCharacters reorderChars) {
        SQLTransaction trans = new SQLTransaction();

        for (var reorderInfo : reorderChars.entries) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_LIST_SLOT);
            stmt.AddValue(0, reorderInfo.newPosition);
            stmt.AddValue(1, reorderInfo.playerGUID.getCounter());
            stmt.AddValue(2, getAccountId());
            trans.append(stmt);
        }

        DB.characters.CommitTransaction(trans);
    }


    private void handlePlayerLogin(PlayerLogin playerLogin) {
        if (getPlayerLoading() || getPlayer() != null) {
            Log.outError(LogFilter.Network, "Player tries to login again, accountId = {0}", getAccountId());
            kickPlayer("WorldSession::HandlePlayerLoginOpcode Another client logging in");

            return;
        }

        playerLoading = playerLogin.guid;
        Log.outDebug(LogFilter.Network, "Character {0} logging in", playerLogin.guid.toString());

        if (!legitCharacters.contains(playerLogin.guid)) {
            Log.outError(LogFilter.Network, "Account ({0}) can't login with that character ({1}).", getAccountId(), playerLogin.guid.toString());
            kickPlayer("WorldSession::HandlePlayerLoginOpcode Trying to login with a character of another account");

            return;
        }

        sendConnectToInstance(ConnectToSerial.WorldAttempt1);
    }


    private void handleLoadScreen(LoadingScreenNotify loadingScreenNotify) {
        // TODO: Do something with this packet
    }


    private void handleSetFactionAtWar(SetFactionAtWar packet) {
        getPlayer().getReputationMgr().setAtWar(packet.factionIndex, true);
    }


    private void handleSetFactionNotAtWar(SetFactionNotAtWar packet) {
        getPlayer().getReputationMgr().setAtWar(packet.factionIndex, false);
    }


    private void handleTutorialFlag(TutorialSetFlag packet) {
        switch (packet.action) {
            case Update: {
                var index = (byte) (packet.tutorialBit >>> 5);

                if (index >= SharedConst.MaxAccountTutorialValues) {
                    Log.outError(LogFilter.Network, "CMSG_TUTORIAL_FLAG received bad TutorialBit {0}.", packet.tutorialBit);

                    return;
                }

                var flag = getTutorialInt(index);
                flag |= (int) (1 << (int) (packet.tutorialBit & 0x1F));
                setTutorialInt(index, flag);

                break;
            }
            case Clear:
                for (byte i = 0; i < SharedConst.MaxAccountTutorialValues; ++i) {
                    setTutorialInt(i, (int) 0xFFFFFFFF);
                }

                break;
            case Reset:
                for (byte i = 0; i < SharedConst.MaxAccountTutorialValues; ++i) {
                    setTutorialInt(i, 0x00000000);
                }

                break;
            default:
                Log.outError(LogFilter.Network, "CMSG_TUTORIAL_FLAG received unknown TutorialAction {0}.", packet.action);

                return;
        }
    }


    private void handleSetWatchedFaction(SetWatchedFaction packet) {
        getPlayer().setWatchedFactionIndex(packet.factionIndex);
    }


    private void handleSetFactionInactive(SetFactionInactive packet) {
        getPlayer().getReputationMgr().setInactive(packet.index, packet.state);
    }


    private void handleCheckCharacterNameAvailability(CheckCharacterNameAvailability checkCharacterNameAvailability) {
        // prevent character rename to invalid name
        tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(checkCharacterNameAvailability.name);
        if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
            checkCharacterNameAvailability.name = tempRef_Name.refArgValue;
            sendPacket(new CheckCharacterNameAvailabilityResult(checkCharacterNameAvailability.sequenceIndex, ResponseCodes.CharNameNoName));

            return;
        } else {
            checkCharacterNameAvailability.name = tempRef_Name.refArgValue;
        }

        var res = ObjectManager.checkPlayerName(checkCharacterNameAvailability.name, getSessionDbcLocale(), true);

        if (res != ResponseCodes.CharNameSuccess) {
            sendPacket(new CheckCharacterNameAvailabilityResult(checkCharacterNameAvailability.sequenceIndex, res));

            return;
        }

        // check name limitations
        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationReservedname) && global.getObjectMgr().isReservedName(checkCharacterNameAvailability.name)) {
            sendPacket(new CheckCharacterNameAvailabilityResult(checkCharacterNameAvailability.sequenceIndex, ResponseCodes.CharNameReserved));

            return;
        }

        // Ensure that there is no character with the desired new name
        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHECK_NAME);
        stmt.AddValue(0, checkCharacterNameAvailability.name);

        var sequenceIndex = checkCharacterNameAvailability.sequenceIndex;
        queryProcessor.AddCallback(DB.characters.AsyncQuery(stmt).WithCallback(result ->
        {
            sendPacket(new CheckCharacterNameAvailabilityResult(sequenceIndex, !result.isEmpty() ? ResponseCodes.CharCreateNameInUse : ResponseCodes.success));
        }));
    }


    private void handleRequestForcedReactions(RequestForcedReactions requestForcedReactions) {
        getPlayer().getReputationMgr().sendForceReactions();
    }


    private void handleCharRename(CharacterRenameRequest request) {
        if (!legitCharacters.contains(request.renameInfo.guid)) {
            Log.outError(LogFilter.Network, "Account {0}, IP: {1} tried to rename character {2}, but it does not belong to their account!", getAccountId(), getRemoteAddress(), request.renameInfo.guid.toString());

            kickPlayer("WorldSession::HandleCharRenameOpcode rename character from a different account");

            return;
        }

        // prevent character rename to invalid name
        tangible.RefObject<String> tempRef_NewName = new tangible.RefObject<String>(request.renameInfo.newName);
        if (!ObjectManager.normalizePlayerName(tempRef_NewName)) {
            request.renameInfo.newName = tempRef_NewName.refArgValue;
            sendCharRename(ResponseCodes.CharNameNoName, request.renameInfo);

            return;
        } else {
            request.renameInfo.newName = tempRef_NewName.refArgValue;
        }

        var res = ObjectManager.checkPlayerName(request.renameInfo.newName, getSessionDbcLocale(), true);

        if (res != ResponseCodes.CharNameSuccess) {
            sendCharRename(res, request.renameInfo);

            return;
        }

        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationReservedname) && global.getObjectMgr().isReservedName(request.renameInfo.newName)) {
            sendCharRename(ResponseCodes.CharNameReserved, request.renameInfo);

            return;
        }

        // Ensure that there is no character with the desired new name
        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_FREE_NAME);
        stmt.AddValue(0, request.renameInfo.guid.getCounter());
        stmt.AddValue(1, request.renameInfo.newName);

        queryProcessor.AddCallback(DB.characters.AsyncQuery(stmt).WithCallback(HandleCharRenameCallBack, request.renameInfo));
    }

    private void handleCharRenameCallBack(CharacterRenameInfo renameInfo, SQLResult result) {
        if (result.isEmpty()) {
            sendCharRename(ResponseCodes.CharNameFailure, renameInfo);

            return;
        }

        var oldName = result.<String>Read(0);
        // check name limitations
        var atLoginFlags = AtLoginFlags.forValue(result.<Integer>Read(1));

        if (!atLoginFlags.hasFlag(AtLoginFlags.Rename)) {
            sendCharRename(ResponseCodes.CharCreateError, renameInfo);

            return;
        }

        atLoginFlags = AtLoginFlags.forValue(atLoginFlags.getValue() & ~AtLoginFlags.Rename.getValue());

        SQLTransaction trans = new SQLTransaction();
        var lowGuid = renameInfo.guid.getCounter();

        // Update name and at_login flag in the db
        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_NAME_AT_LOGIN);
        stmt.AddValue(0, renameInfo.newName);
        stmt.AddValue(1, (short) atLoginFlags.getValue());
        stmt.AddValue(2, lowGuid);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_DECLINED_NAME);
        stmt.AddValue(0, lowGuid);
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);

        Log.outInfo(LogFilter.player, "Account: {0} (IP: {1}) Character:[{2}] ({3}) Changed name to: {4}", getAccountId(), getRemoteAddress(), oldName, renameInfo.guid.toString(), renameInfo.newName);

        sendCharRename(ResponseCodes.success, renameInfo);

        global.getCharacterCacheStorage().updateCharacterData(renameInfo.guid, renameInfo.newName);
    }


    private void handleSetPlayerDeclinedNames(SetPlayerDeclinedNames packet) {
        // not accept declined names for unsupported languages
        String name;
        tangible.OutObject<String> tempOut_name = new tangible.OutObject<String>();
        if (!global.getCharacterCacheStorage().getCharacterNameByGuid(packet.player, tempOut_name)) {
            name = tempOut_name.outArgValue;
            sendSetPlayerDeclinedNamesResult(DeclinedNameResult.error, packet.player);

            return;
        } else {
            name = tempOut_name.outArgValue;
        }

        if (!Character.isLetter(name.charAt(0))) // name already stored as only single alphabet using
        {
            sendSetPlayerDeclinedNamesResult(DeclinedNameResult.error, packet.player);

            return;
        }

        for (var i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
            var declinedName = packet.declinedNames.name.charAt(i);

            tangible.RefObject<String> tempRef_declinedName = new tangible.RefObject<String>(declinedName);
            if (!ObjectManager.normalizePlayerName(tempRef_declinedName)) {
                declinedName = tempRef_declinedName.refArgValue;
                sendSetPlayerDeclinedNamesResult(DeclinedNameResult.error, packet.player);

                return;
            } else {
                declinedName = tempRef_declinedName.refArgValue;
            }

            packet.declinedNames.name.charAt(i) = declinedName;
        }

        for (var i = 0; i < SharedConst.MaxDeclinedNameCases; ++i) {
            var declinedName = packet.declinedNames.name.charAt(i);
            tangible.RefObject<String> tempRef_declinedName2 = new tangible.RefObject<String>(declinedName);
            CharacterDatabase.EscapeString(tempRef_declinedName2);
            declinedName = tempRef_declinedName2.refArgValue;
            packet.declinedNames.name.charAt(i) = declinedName;
        }

        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_DECLINED_NAME);
        stmt.AddValue(0, packet.player.getCounter());
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_DECLINED_NAME);
        stmt.AddValue(0, packet.player.getCounter());

        for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
            stmt.AddValue(i + 1, packet.declinedNames.name.charAt(i));
        }

        trans.append(stmt);

        DB.characters.CommitTransaction(trans);

        sendSetPlayerDeclinedNamesResult(DeclinedNameResult.success, packet.player);
    }


    private void handleAlterAppearance(AlterApperance packet) {
        if (!validateAppearance(player.getRace(), player.getClass(), gender.forValue((byte) packet.newSex), packet.customizations)) {
            return;
        }

        var go = getPlayer().findNearestGameObjectOfType(GameObjectTypes.barberChair, 5.0f);

        if (!go) {
            sendPacket(new BarberShopResult(BarberShopResult.ResultEnum.NotOnChair));

            return;
        }

        if (getPlayer().getStandState() != UnitStandStateType.forValue(UnitStandStateType.SitLowChair.getValue() + go.getTemplate().barberChair.chairheight)) {
            sendPacket(new BarberShopResult(BarberShopResult.ResultEnum.NotOnChair));

            return;
        }

        var cost = getPlayer().getBarberShopCost(packet.customizations);

        if (!getPlayer().hasEnoughMoney(cost)) {
            sendPacket(new BarberShopResult(BarberShopResult.ResultEnum.NoMoney));

            return;
        }

        sendPacket(new BarberShopResult(BarberShopResult.ResultEnum.success));

        player.modifyMoney(-cost);
        player.updateCriteria(CriteriaType.MoneySpentAtBarberShop, (long) cost);

        if (player.getNativeGender() != gender.forValue((byte) packet.newSex)) {
            player.setNativeGender(gender.forValue((byte) packet.newSex));
            player.initDisplayIds();
            player.restoreDisplayId(false);
        }

        player.setCustomizations(packet.customizations);

        player.updateCriteria(CriteriaType.GotHaircut, 1);

        player.setStandState(UnitStandStateType.Stand);

        global.getCharacterCacheStorage().updateCharacterGender(player.getGUID(), packet.newSex);
    }


    private void handleCharCustomize(CharCustomize packet) {
        if (!legitCharacters.contains(packet.customizeInfo.charGUID)) {
            Log.outError(LogFilter.Network, "Account {0}, IP: {1} tried to customise {2}, but it does not belong to their account!", getAccountId(), getRemoteAddress(), packet.customizeInfo.charGUID.toString());

            kickPlayer("WorldSession::HandleCharCustomize Trying to customise character of another account");

            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_CUSTOMIZE_INFO);
        stmt.AddValue(0, packet.customizeInfo.charGUID.getCounter());

        queryProcessor.AddCallback(DB.characters.AsyncQuery(stmt).WithCallback(HandleCharCustomizeCallback, packet.customizeInfo));
    }

    private void handleCharCustomizeCallback(CharCustomizeInfo customizeInfo, SQLResult result) {
        if (result.isEmpty()) {
            sendCharCustomize(ResponseCodes.CharCreateError, customizeInfo);

            return;
        }

        var oldName = result.<String>Read(0);
        var plrRace = race.forValue(result.<Byte>Read(1));
        var plrClass = playerClass.forValue(result.<Byte>Read(2));
        var plrGender = gender.forValue(result.<Byte>Read(3));
        var atLoginFlags = AtLoginFlags.forValue(result.<SHORT>Read(4));

        if (!validateAppearance(plrRace, plrClass, plrGender, customizeInfo.customizations)) {
            sendCharCustomize(ResponseCodes.CharCreateError, customizeInfo);

            return;
        }

        if (!atLoginFlags.hasFlag(AtLoginFlags.Customize)) {
            sendCharCustomize(ResponseCodes.CharCreateError, customizeInfo);

            return;
        }

        // prevent character rename
        if (WorldConfig.getBoolValue(WorldCfg.PreventRenameCustomization) && (!Objects.equals(customizeInfo.charName, oldName))) {
            sendCharCustomize(ResponseCodes.CharNameFailure, customizeInfo);

            return;
        }

        atLoginFlags = AtLoginFlags.forValue(atLoginFlags.getValue() & ~AtLoginFlags.Customize.getValue());

        // prevent character rename to invalid name
        tangible.RefObject<String> tempRef_CharName = new tangible.RefObject<String>(customizeInfo.charName);
        if (!ObjectManager.normalizePlayerName(tempRef_CharName)) {
            customizeInfo.charName = tempRef_CharName.refArgValue;
            sendCharCustomize(ResponseCodes.CharNameNoName, customizeInfo);

            return;
        } else {
            customizeInfo.charName = tempRef_CharName.refArgValue;
        }

        var res = ObjectManager.checkPlayerName(customizeInfo.charName, getSessionDbcLocale(), true);

        if (res != ResponseCodes.CharNameSuccess) {
            sendCharCustomize(res, customizeInfo);

            return;
        }

        // check name limitations
        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationReservedname) && global.getObjectMgr().isReservedName(customizeInfo.charName)) {
            sendCharCustomize(ResponseCodes.CharNameReserved, customizeInfo);

            return;
        }

        // character with this name already exist
        // @todo: make async
        var newGuid = global.getCharacterCacheStorage().getCharacterGuidByName(customizeInfo.charName);

        if (!newGuid.isEmpty()) {
            if (ObjectGuid.opNotEquals(newGuid, customizeInfo.charGUID)) {
                sendCharCustomize(ResponseCodes.CharCreateNameInUse, customizeInfo);

                return;
            }
        }

        PreparedStatement stmt;
        SQLTransaction trans = new SQLTransaction();
        var lowGuid = customizeInfo.charGUID.getCounter();

        // Customize
        player.saveCustomizations(trans, lowGuid, customizeInfo.customizations);

        {
            // Name Change and update atLogin flags
            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_NAME_AT_LOGIN);
            stmt.AddValue(0, customizeInfo.charName);
            stmt.AddValue(1, (short) atLoginFlags.getValue());
            stmt.AddValue(2, lowGuid);
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_DECLINED_NAME);
            stmt.AddValue(0, lowGuid);

            trans.append(stmt);
        }

        DB.characters.CommitTransaction(trans);

        global.getCharacterCacheStorage().updateCharacterData(customizeInfo.charGUID, customizeInfo.charName, (byte) customizeInfo.sexID.getValue());

        sendCharCustomize(ResponseCodes.success, customizeInfo);

        Log.outInfo(LogFilter.player, "Account: {0} (IP: {1}), Character[{2}] ({3}) Customized to: {4}", getAccountId(), getRemoteAddress(), oldName, customizeInfo.charGUID.toString(), customizeInfo.charName);
    }


    private void handleEquipmentSetSave(SaveEquipmentSet saveEquipmentSet) {
        if (saveEquipmentSet.set.getSetId() >= ItemConst.MaxEquipmentSetIndex) // client set slots amount
        {
            return;
        }

        if (saveEquipmentSet.set.getType().getValue() > EquipmentSetInfo.EquipmentSetType.transmog.getValue()) {
            return;
        }

        for (byte i = 0; i < EquipmentSlot.End; ++i) {
            if (!(boolean) (saveEquipmentSet.set.getIgnoreMask() & (1 << i))) {
                if (saveEquipmentSet.set.getType() == EquipmentSetInfo.EquipmentSetType.Equipment) {
                    saveEquipmentSet.set.getAppearances()[i] = 0;

                    var itemGuid = saveEquipmentSet.set.getPieces()[i];

                    if (!itemGuid.isEmpty()) {
                        var item = player.getItemByPos(InventorySlots.Bag0, i);

                        // cheating check 1 (item equipped but sent empty guid)
                        if (!item) {
                            return;
                        }

                        // cheating check 2 (sent guid does not match equipped item)
                        if (ObjectGuid.opNotEquals(item.getGUID(), itemGuid)) {
                            return;
                        }
                    } else {
                        saveEquipmentSet.set.setIgnoreMask(saveEquipmentSet.set.getIgnoreMask() | 1 << i);
                    }
                } else {
                    saveEquipmentSet.set.getPieces()[i].clear();

                    if (saveEquipmentSet.set.getAppearances()[i] != 0) {
                        if (!CliDB.ItemModifiedAppearanceStorage.containsKey(saveEquipmentSet.set.getAppearances()[i])) {
                            return;
                        }


                        (var hasAppearance, _) =
                        CollectionMgr.hasItemAppearance((int) saveEquipmentSet.set.Appearances[i]);

                        if (!hasAppearance) {
                            return;
                        }
                    } else {
                        saveEquipmentSet.set.setIgnoreMask(saveEquipmentSet.set.getIgnoreMask() | 1 << i);
                    }
                }
            } else {
                saveEquipmentSet.set.getPieces()[i].clear();
                saveEquipmentSet.set.getAppearances()[i] = 0;
            }
        }

        saveEquipmentSet.set.setIgnoreMask(saveEquipmentSet.set.getIgnoreMask() & 0x7FFFF); // clear invalid bits (i > EQUIPMENT_SLOT_END)

        if (saveEquipmentSet.set.getType() == EquipmentSetInfo.EquipmentSetType.Equipment) {
            saveEquipmentSet.set.getEnchants()[0] = 0;
            saveEquipmentSet.set.getEnchants()[1] = 0;
        } else {
            var validateIllusion = (int arg) ->
            {
                var illusion = CliDB.SpellItemEnchantmentStorage.get(enchantId);

                if (illusion == null) {
                    return false;
                }

                if (illusion.itemVisual == 0 || !illusion.getFlags().hasFlag(SpellItemEnchantmentFlags.AllowTransmog)) {
                    return false;
                }

                var condition = CliDB.PlayerConditionStorage.get(illusion.TransmogUseConditionID);

                if (condition != null) {
                    if (!ConditionManager.isPlayerMeetingCondition(player, condition)) {
                        return false;
                    }
                }

                if (illusion.ScalingClassRestricted > 0 && illusion.ScalingClassRestricted != (byte) player.getClass().getValue()) {
                    return false;
                }

                return true;
            };

            if (saveEquipmentSet.set.getEnchants()[0] != 0 && !validateIllusion.invoke((int) saveEquipmentSet.set.getEnchants()[0])) {
                return;
            }

            if (saveEquipmentSet.set.getEnchants()[1] != 0 && !validateIllusion.invoke((int) saveEquipmentSet.set.getEnchants()[1])) {
                return;
            }
        }

        getPlayer().setEquipmentSet(saveEquipmentSet.set);
    }


    private void handleDeleteEquipmentSet(DeleteEquipmentSet packet) {
        getPlayer().deleteEquipmentSet(packet.ID);
    }


    private void handleUseEquipmentSet(UseEquipmentSet useEquipmentSet) {
        ObjectGuid ignoredItemGuid = new ObjectGuid(0x0C00040000000000L, (long) 0xFFFFFFFFFFFFFFFF);

        for (byte i = 0; i < EquipmentSlot.End; ++i) {
            Log.outDebug(LogFilter.player, "{0}: ContainerSlot: {1}, Slot: {2}", useEquipmentSet.Items[i].item.toString(), useEquipmentSet.Items[i].containerSlot, useEquipmentSet.Items[i].slot);

            // check if item slot is set to "ignored" (raw value == 1), must not be unequipped then
            if (Objects.equals(useEquipmentSet.Items[i].item, ignoredItemGuid)) {
                continue;
            }

            // Only equip weapons in combat
            if (getPlayer().isInCombat() && i != EquipmentSlot.MainHand && i != EquipmentSlot.OffHand) {
                continue;
            }

            var item = getPlayer().getItemByGuid(useEquipmentSet.Items[i].item);

            var dstPos = (short) (i | (InventorySlots.Bag0 << 8));

            if (!item) {
                var uItem = getPlayer().getItemByPos(InventorySlots.Bag0, i);

                if (!uItem) {
                    continue;
                }

                ArrayList<ItemPosCount> itemPosCount = new ArrayList<>();
                var inventoryResult = getPlayer().canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, itemPosCount, uItem, false);

                if (inventoryResult == InventoryResult.Ok) {
                    if (player.canUnequipItem(dstPos, true) != InventoryResult.Ok) {
                        continue;
                    }

                    getPlayer().removeItem(InventorySlots.Bag0, i, true);
                    getPlayer().storeItem(itemPosCount, uItem, true);
                } else {
                    getPlayer().sendEquipError(inventoryResult, uItem);
                }

                continue;
            }

            if (item.getPos() == dstPos) {
                continue;
            }

            tangible.OutObject<SHORT> tempOut_dstPos = new tangible.OutObject<SHORT>();
            if (player.canEquipItem(i, tempOut_dstPos, item, true) != InventoryResult.Ok) {
                dstPos = tempOut_dstPos.outArgValue;
                continue;
            } else {
                dstPos = tempOut_dstPos.outArgValue;
            }

            getPlayer().swapItem(item.getPos(), dstPos);
        }

        UseEquipmentSetResult result = new UseEquipmentSetResult();
        result.GUID = useEquipmentSet.GUID;
        result.reason = 0; // 4 - equipment swap failed - inventory is full
        sendPacket(result);
    }


    private void handleCharRaceOrFactionChange(CharRaceOrFactionChange packet) {
        if (!legitCharacters.contains(packet.raceOrFactionChangeInfo.guid)) {
            Log.outError(LogFilter.Network, "Account {0}, IP: {1} tried to factionchange character {2}, but it does not belong to their account!", getAccountId(), getRemoteAddress(), packet.raceOrFactionChangeInfo.guid.toString());

            kickPlayer("WorldSession::HandleCharFactionOrRaceChange Trying to change faction of character of another account");

            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_RACE_OR_FACTION_CHANGE_INFOS);
        stmt.AddValue(0, packet.raceOrFactionChangeInfo.guid.getCounter());

        queryProcessor.AddCallback(DB.characters.AsyncQuery(stmt).WithCallback(HandleCharRaceOrFactionChangeCallback, packet.raceOrFactionChangeInfo));
    }

    private void handleCharRaceOrFactionChangeCallback(CharRaceOrFactionChangeInfo factionChangeInfo, SQLResult result) {
        if (result.isEmpty()) {
            sendCharFactionChange(ResponseCodes.CharCreateError, factionChangeInfo);

            return;
        }

        // get the players old (at this moment current) race
        var characterInfo = global.getCharacterCacheStorage().getCharacterCacheByGuid(factionChangeInfo.guid);

        if (characterInfo == null) {
            sendCharFactionChange(ResponseCodes.CharCreateError, factionChangeInfo);

            return;
        }

        var oldName = characterInfo.name;
        var oldRace = characterInfo.raceId;
        var playerClass = characterInfo.classId;
        var level = characterInfo.level;

        if (global.getObjectMgr().getPlayerInfo(factionChangeInfo.raceID, playerClass) == null) {
            sendCharFactionChange(ResponseCodes.CharCreateError, factionChangeInfo);

            return;
        }

        var atLoginFlags = AtLoginFlags.forValue(result.<SHORT>Read(0));
        var knownTitlesStr = result.<String>Read(1);

        var usedLoginFlag = (factionChangeInfo.FactionChange ? AtLoginFlags.ChangeFaction : AtLoginFlags.ChangeRace);

        if (!atLoginFlags.hasFlag(usedLoginFlag)) {
            sendCharFactionChange(ResponseCodes.CharCreateError, factionChangeInfo);

            return;
        }

        var newTeamId = player.teamIdForRace(factionChangeInfo.raceID);

        if (newTeamId == TeamIds.Neutral) {
            sendCharFactionChange(ResponseCodes.CharCreateRestrictedRaceclass, factionChangeInfo);

            return;
        }

        if (factionChangeInfo.factionChange == (player.teamIdForRace(oldRace) == newTeamId)) {
            sendCharFactionChange(factionChangeInfo.FactionChange ? ResponseCodes.CharCreateCharacterSwapFaction : ResponseCodes.CharCreateCharacterRaceOnly, factionChangeInfo);

            return;
        }

        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationRacemask)) {
            var raceMaskDisabled = WorldConfig.getUInt64Value(WorldCfg.CharacterCreatingDisabledRacemask);

            if ((boolean) ((long) SharedConst.GetMaskForRace(factionChangeInfo.raceID) & raceMaskDisabled)) {
                sendCharFactionChange(ResponseCodes.CharCreateError, factionChangeInfo);

                return;
            }
        }

        // prevent character rename
        if (WorldConfig.getBoolValue(WorldCfg.PreventRenameCustomization) && (!Objects.equals(factionChangeInfo.name, oldName))) {
            sendCharFactionChange(ResponseCodes.CharNameFailure, factionChangeInfo);

            return;
        }

        // prevent character rename to invalid name
        tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(factionChangeInfo.name);
        if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
            factionChangeInfo.name = tempRef_Name.refArgValue;
            sendCharFactionChange(ResponseCodes.CharNameNoName, factionChangeInfo);

            return;
        } else {
            factionChangeInfo.name = tempRef_Name.refArgValue;
        }

        var res = ObjectManager.checkPlayerName(factionChangeInfo.name, getSessionDbcLocale(), true);

        if (res != ResponseCodes.CharNameSuccess) {
            sendCharFactionChange(res, factionChangeInfo);

            return;
        }

        // check name limitations
        if (!hasPermission(RBACPermissions.SkipCheckCharacterCreationReservedname) && global.getObjectMgr().isReservedName(factionChangeInfo.name)) {
            sendCharFactionChange(ResponseCodes.CharNameReserved, factionChangeInfo);

            return;
        }

        // character with this name already exist
        var newGuid = global.getCharacterCacheStorage().getCharacterGuidByName(factionChangeInfo.name);

        if (!newGuid.isEmpty()) {
            if (ObjectGuid.opNotEquals(newGuid, factionChangeInfo.guid)) {
                sendCharFactionChange(ResponseCodes.CharCreateNameInUse, factionChangeInfo);

                return;
            }
        }

        if (global.getArenaTeamMgr().getArenaTeamByCaptain(factionChangeInfo.guid) != null) {
            sendCharFactionChange(ResponseCodes.CharCreateCharacterArenaLeader, factionChangeInfo);

            return;
        }

        // All checks are fine, deal with race change now
        var lowGuid = factionChangeInfo.guid.getCounter();

        PreparedStatement stmt;
        SQLTransaction trans = new SQLTransaction();

        // resurrect the character in case he's dead
        player.offlineResurrect(factionChangeInfo.guid, trans);

        {
            // Name Change and update atLogin flags
            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_NAME_AT_LOGIN);
            stmt.AddValue(0, factionChangeInfo.name);
            stmt.AddValue(1, (short) ((atLoginFlags.getValue() | AtLoginFlags.Resurrect.getValue()) & ~usedLoginFlag));
            stmt.AddValue(2, lowGuid);

            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_DECLINED_NAME);
            stmt.AddValue(0, lowGuid);

            trans.append(stmt);
        }

        // Customize
        player.saveCustomizations(trans, lowGuid, factionChangeInfo.customizations);

        {
            // Race Change
            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_RACE);
            stmt.AddValue(0, (byte) factionChangeInfo.raceID.getValue());
            stmt.AddValue(1, (short) PlayerExtraFlags.HasRaceChanged.getValue());
            stmt.AddValue(2, lowGuid);

            trans.append(stmt);
        }

        global.getCharacterCacheStorage().updateCharacterData(factionChangeInfo.guid, factionChangeInfo.name, (byte) factionChangeInfo.sexID.getValue(), (byte) factionChangeInfo.raceID.getValue());

        if (oldRace != factionChangeInfo.raceID) {
            // Switch Languages
            // delete all languages first
            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_SKILL_LANGUAGES);
            stmt.AddValue(0, lowGuid);
            trans.append(stmt);

            // Now add them back
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_SKILL_LANGUAGE);
            stmt.AddValue(0, lowGuid);

            // Faction specific languages
            if (newTeamId == TeamId.HORDE) {
                stmt.AddValue(1, 109);
            } else {
                stmt.AddValue(1, 98);
            }

            trans.append(stmt);

            // Race specific languages
            if (factionChangeInfo.raceID != race.Orc && factionChangeInfo.raceID != race.Human) {
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_SKILL_LANGUAGE);
                stmt.AddValue(0, lowGuid);

                switch (factionChangeInfo.raceID) {
                    case Dwarf:
                        stmt.AddValue(1, 111);

                        break;
                    case Draenei:
                    case LightforgedDraenei:
                        stmt.AddValue(1, 759);

                        break;
                    case Gnome:
                        stmt.AddValue(1, 313);

                        break;
                    case NightElf:
                        stmt.AddValue(1, 113);

                        break;
                    case Worgen:
                        stmt.AddValue(1, 791);

                        break;
                    case Undead:
                        stmt.AddValue(1, 673);

                        break;
                    case Tauren:
                    case HighmountainTauren:
                        stmt.AddValue(1, 115);

                        break;
                    case Troll:
                        stmt.AddValue(1, 315);

                        break;
                    case BloodElf:
                    case VoidElf:
                        stmt.AddValue(1, 137);

                        break;
                    case Goblin:
                        stmt.AddValue(1, 792);

                        break;
                    case Nightborne:
                        stmt.AddValue(1, 2464);

                        break;
                    default:
                        Log.outError(LogFilter.player, String.format("Could not find language data for race (%1$s).", factionChangeInfo.raceID));
                        sendCharFactionChange(ResponseCodes.CharCreateError, factionChangeInfo);

                        return;
                }

                trans.append(stmt);
            }

            // Team Conversation
            if (factionChangeInfo.factionChange) {
                // Delete all Flypaths
                stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_TAXI_PATH);
                stmt.AddValue(0, lowGuid);
                trans.append(stmt);

                if (level > 7) {
                    // Update Taxi path
                    // this doesn't seem to be 100% blizzlike... but it can't really be helped.
                    var taximaskstream = "";


                    var factionMask = newTeamId == TeamId.HORDE ? CliDB.HordeTaxiNodesMask : CliDB.AllianceTaxiNodesMask;

                    for (var i = 0; i < factionMask.length; ++i) {
                        // i = (315 - 1) / 8 = 39
                        // m = 1 << ((315 - 1) % 8) = 4
                        var deathKnightExtraNode = playerClass != playerClass.Deathknight || i != 39 ? 0 : 4;
                        taximaskstream += (int) (factionMask[i] | deathKnightExtraNode) + ' ';
                    }

                    stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_TAXIMASK);
                    stmt.AddValue(0, taximaskstream);
                    stmt.AddValue(1, lowGuid);
                    trans.append(stmt);
                }

                if (!WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGuild)) {
                    // Reset guild
                    var guild = global.getGuildMgr().getGuildById(characterInfo.guildId);

                    if (guild != null) {
                        guild.deleteMember(trans, factionChangeInfo.guid, false, false, true);
                    }

                    player.leaveAllArenaTeams(factionChangeInfo.guid);
                }

                if (!hasPermission(RBACPermissions.TwoSideAddFriend)) {
                    // Delete Friend List
                    stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_SOCIAL_BY_GUID);
                    stmt.AddValue(0, lowGuid);
                    trans.append(stmt);

                    stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_SOCIAL_BY_FRIEND);
                    stmt.AddValue(0, lowGuid);
                    trans.append(stmt);
                }

                // Reset homebind and position
                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_PLAYER_HOMEBIND);
                stmt.AddValue(0, lowGuid);
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_PLAYER_HOMEBIND);
                stmt.AddValue(0, lowGuid);

                WorldLocation loc;
                short zoneId = 0;

                if (newTeamId == TeamId.ALLIANCE) {
                    loc = new worldLocation(0, -8867.68f, 673.373f, 97.9034f, 0.0f);
                    zoneId = 1519;
                } else {
                    loc = new worldLocation(1, 1633.33f, -4439.11f, 15.7588f, 0.0f);
                    zoneId = 1637;
                }

                stmt.AddValue(1, loc.getMapId());
                stmt.AddValue(2, zoneId);
                stmt.AddValue(3, loc.getX());
                stmt.AddValue(4, loc.getY());
                stmt.AddValue(5, loc.getZ());
                trans.append(stmt);

                player.savePositionInDB(loc, zoneId, factionChangeInfo.guid, trans);

                // Achievement conversion
                for (var it : global.getObjectMgr().factionChangeAchievements.entrySet()) {
                    var achiev_alliance = it.getKey();
                    var achiev_horde = it.getValue();

                    stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_ACHIEVEMENT_BY_ACHIEVEMENT);
                    stmt.AddValue(0, (short) (newTeamId == TeamId.ALLIANCE ? achiev_alliance : achiev_horde));
                    stmt.AddValue(1, lowGuid);
                    trans.append(stmt);

                    stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_ACHIEVEMENT);
                    stmt.AddValue(0, (short) (newTeamId == TeamId.ALLIANCE ? achiev_alliance : achiev_horde));
                    stmt.AddValue(1, (short) (newTeamId == TeamId.ALLIANCE ? achiev_horde : achiev_alliance));
                    stmt.AddValue(2, lowGuid);
                    trans.append(stmt);
                }

                // Item conversion
                var itemConversionMap = newTeamId == TeamId.ALLIANCE ? global.getObjectMgr().FactionChangeItemsHordeToAlliance : global.getObjectMgr().factionChangeItemsAllianceToHorde;

                for (var it : itemConversionMap) {
                    var oldItemId = it.key;
                    var newItemId = it.value;

                    stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_INVENTORY_FACTION_CHANGE);
                    stmt.AddValue(0, newItemId);
                    stmt.AddValue(1, oldItemId);
                    stmt.AddValue(2, lowGuid);
                    trans.append(stmt);
                }

                // Delete all current quests
                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_QUESTSTATUS);
                stmt.AddValue(0, lowGuid);
                trans.append(stmt);

                // Quest conversion
                for (var it : global.getObjectMgr().factionChangeQuests.entrySet()) {
                    var quest_alliance = it.getKey();
                    var quest_horde = it.getValue();

                    stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_QUESTSTATUS_REWARDED_BY_QUEST);
                    stmt.AddValue(0, lowGuid);
                    stmt.AddValue(1, (newTeamId == TeamId.ALLIANCE ? quest_alliance : quest_horde));
                    trans.append(stmt);

                    stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_QUESTSTATUS_REWARDED_FACTION_CHANGE);
                    stmt.AddValue(0, (newTeamId == TeamId.ALLIANCE ? quest_alliance : quest_horde));
                    stmt.AddValue(1, (newTeamId == TeamId.ALLIANCE ? quest_horde : quest_alliance));
                    stmt.AddValue(2, lowGuid);
                    trans.append(stmt);
                }

                // Mark all rewarded quests as "active" (will count for completed quests achievements)
                stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_QUESTSTATUS_REWARDED_ACTIVE);
                stmt.AddValue(0, lowGuid);
                trans.append(stmt);

                {
                    // Disable all old-faction specific quests
                    var questTemplates = global.getObjectMgr().getQuestTemplates();

                    for (var quest : questTemplates.values()) {
                        var newRaceMask = (long) (newTeamId == TeamId.ALLIANCE ? SharedConst.RaceMaskAlliance : SharedConst.RaceMaskHorde);

                        if (quest.allowableRaces != -1 && !(boolean) (quest.allowableRaces & newRaceMask)) {
                            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_QUESTSTATUS_REWARDED_ACTIVE_BY_QUEST);
                            stmt.AddValue(0, lowGuid);
                            stmt.AddValue(1, quest.id);
                            trans.append(stmt);
                        }
                    }
                }

                // Spell conversion
                for (var it : global.getObjectMgr().factionChangeSpells.entrySet()) {
                    var spell_alliance = it.getKey();
                    var spell_horde = it.getValue();

                    stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_SPELL_BY_SPELL);
                    stmt.AddValue(0, (newTeamId == TeamId.ALLIANCE ? spell_alliance : spell_horde));
                    stmt.AddValue(1, lowGuid);
                    trans.append(stmt);

                    stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_SPELL_FACTION_CHANGE);
                    stmt.AddValue(0, (newTeamId == TeamId.ALLIANCE ? spell_alliance : spell_horde));
                    stmt.AddValue(1, (newTeamId == TeamId.ALLIANCE ? spell_horde : spell_alliance));
                    stmt.AddValue(2, lowGuid);
                    trans.append(stmt);
                }

                // Reputation conversion
                for (var it : global.getObjectMgr().factionChangeReputation.entrySet()) {
                    var reputation_alliance = it.getKey();
                    var reputation_horde = it.getValue();
                    var newReputation = (newTeamId == TeamId.ALLIANCE) ? reputation_alliance : reputation_horde;
                    var oldReputation = (newTeamId == TeamId.ALLIANCE) ? reputation_horde : reputation_alliance;

                    // select old standing set in db
                    stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_REP_BY_FACTION);
                    stmt.AddValue(0, oldReputation);
                    stmt.AddValue(1, lowGuid);

                    result = DB.characters.query(stmt);

                    if (!result.isEmpty()) {
                        var oldDBRep = result.<Integer>Read(0);
                        var factionEntry = CliDB.FactionStorage.get(oldReputation);

                        // old base reputation
                        var oldBaseRep = ReputationMgr.getBaseReputationOf(factionEntry, oldRace, playerClass);

                        // new base reputation
                        var newBaseRep = ReputationMgr.getBaseReputationOf(CliDB.FactionStorage.get(newReputation), factionChangeInfo.raceID, playerClass);

                        // final reputation shouldnt change
                        var FinalRep = oldDBRep + oldBaseRep;
                        var newDBRep = FinalRep - newBaseRep;

                        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_REP_BY_FACTION);
                        stmt.AddValue(0, newReputation);
                        stmt.AddValue(1, lowGuid);
                        trans.append(stmt);

                        stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_REP_FACTION_CHANGE);
                        stmt.AddValue(0, (short) newReputation);
                        stmt.AddValue(1, newDBRep);
                        stmt.AddValue(2, (short) oldReputation);
                        stmt.AddValue(3, lowGuid);
                        trans.append(stmt);
                    }
                }

                // Title conversion
                if (!StringUtil.isEmpty(knownTitlesStr)) {
                    ArrayList<Integer> knownTitles = new ArrayList<>();

                    var tokens = new LocalizedString();

                    for (var index = 0; index < tokens.length; ++index) {
                        int id;
                        tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
                        if (tangible.TryParseHelper.tryParseInt(tokens.get(index), tempOut_id)) {
                            id = tempOut_id.outArgValue;
                            knownTitles.add(id);
                        } else {
                            id = tempOut_id.outArgValue;
                        }
                    }

                    for (var it : global.getObjectMgr().factionChangeTitles.entrySet()) {
                        var title_alliance = it.getKey();
                        var title_horde = it.getValue();

                        var atitleInfo = CliDB.CharTitlesStorage.get(title_alliance);
                        var htitleInfo = CliDB.CharTitlesStorage.get(title_horde);

                        // new team
                        if (newTeamId == TeamId.ALLIANCE) {
                            int maskID = htitleInfo.MaskID;
                            var index = (int) maskID / 32;

                            if (index >= knownTitles.size()) {
                                continue;
                            }

                            var old_flag = (int) (1 << (int) (maskID % 32));
                            var new_flag = (int) (1 << (atitleInfo.MaskID % 32));

                            if ((boolean) (knownTitles.get(index) & old_flag)) {
                                knownTitles.set(index, knownTitles.get(index) & ~old_flag);
                                // use index of the new title
                                knownTitles.set(atitleInfo.MaskID / 32, knownTitles.get(atitleInfo.MaskID / 32) | new_flag);
                            }
                        } else {
                            int maskID = atitleInfo.MaskID;
                            var index = (int) maskID / 32;

                            if (index >= knownTitles.size()) {
                                continue;
                            }

                            var old_flag = (int) (1 << (int) (maskID % 32));
                            var new_flag = (int) (1 << (htitleInfo.MaskID % 32));

                            if ((boolean) (knownTitles.get(index) & old_flag)) {
                                knownTitles.set(index, knownTitles.get(index) & ~old_flag);
                                // use index of the new title
                                knownTitles.set(htitleInfo.MaskID / 32, knownTitles.get(htitleInfo.MaskID / 32) | new_flag);
                            }
                        }

                        var ss = "";

                        for (var index = 0; index < knownTitles.size(); ++index) {
                            ss += knownTitles.get(index) + ' ';
                        }

                        stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_TITLES_FACTION_CHANGE);
                        stmt.AddValue(0, ss);
                        stmt.AddValue(1, lowGuid);
                        trans.append(stmt);

                        // unset any currently chosen title
                        stmt = DB.characters.GetPreparedStatement(CharStatements.RES_CHAR_TITLES_FACTION_CHANGE);
                        stmt.AddValue(0, lowGuid);
                        trans.append(stmt);
                    }
                }
            }
        }

        DB.characters.CommitTransaction(trans);

        Log.outDebug(LogFilter.player, "{0} (IP: {1}) changed race from {2} to {3}", getPlayerInfo(), getRemoteAddress(), oldRace, factionChangeInfo.raceID);

        sendCharFactionChange(ResponseCodes.success, factionChangeInfo);
    }


    private void handleOpeningCinematic(OpeningCinematic packet) {
        // Only players that has not yet gained any experience can use this
        if (getPlayer().getActivePlayerData().XP != 0) {
            return;
        }

        var classEntry = CliDB.ChrClassesStorage.get(getPlayer().getClass());

        if (classEntry != null) {
            var raceEntry = CliDB.ChrRacesStorage.get(getPlayer().getRace());

            if (classEntry.CinematicSequenceID != 0) {
                getPlayer().sendCinematicStart(classEntry.CinematicSequenceID);
            } else if (raceEntry != null) {
                getPlayer().sendCinematicStart(raceEntry.CinematicSequenceID);
            }
        }
    }


    private void handleGetUndeleteCooldownStatus(GetUndeleteCharacterCooldownStatus getCooldown) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_LAST_CHAR_UNDELETE);
        stmt.AddValue(0, getBattlenetAccountId());

        queryProcessor.AddCallback(DB.Login.AsyncQuery(stmt).WithCallback(HandleUndeleteCooldownStatusCallback));
    }

    private void handleUndeleteCooldownStatusCallback(SQLResult result) {
        int cooldown = 0;
        var maxCooldown = WorldConfig.getUIntValue(WorldCfg.FeatureSystemCharacterUndeleteCooldown);

        if (!result.isEmpty()) {
            var lastUndelete = result.<Integer>Read(0);
            var now = (int) gameTime.GetGameTime();

            if (lastUndelete + maxCooldown > now) {
                cooldown = Math.max(0, lastUndelete + maxCooldown - now);
            }
        }

        sendUndeleteCooldownStatusResponse(cooldown, maxCooldown);
    }


    private void handleCharUndelete(UndeleteCharacter undeleteCharacter) {
        if (!WorldConfig.getBoolValue(WorldCfg.FeatureSystemCharacterUndeleteEnabled)) {
            sendUndeleteCharacterResponse(CharacterUndeleteResult.disabled, undeleteCharacter.undeleteInfo);

            return;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_LAST_CHAR_UNDELETE);
        stmt.AddValue(0, getBattlenetAccountId());

        var undeleteInfo = undeleteCharacter.undeleteInfo;

        queryProcessor.AddCallback(DB.Login.AsyncQuery(stmt).WithChainingCallback((queryCallback, result) ->
        {
            if (!result.isEmpty()) {
                var lastUndelete = result.<Integer>Read(0);
                var maxCooldown = WorldConfig.getUIntValue(WorldCfg.FeatureSystemCharacterUndeleteCooldown);

                if (lastUndelete != 0 && (lastUndelete + maxCooldown > gameTime.GetGameTime())) {
                    sendUndeleteCharacterResponse(CharacterUndeleteResult.cooldown, undeleteInfo);

                    return;
                }
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_DEL_INFO_BY_GUID);
            stmt.AddValue(0, undeleteInfo.characterGuid.getCounter());
            queryCallback.SetNextQuery(DB.characters.AsyncQuery(stmt));
        }).WithChainingCallback((queryCallback, result) ->
        {
            if (result.isEmpty()) {
                sendUndeleteCharacterResponse(CharacterUndeleteResult.CharCreate, undeleteInfo);

                return;
            }

            undeleteInfo.name = result.<String>Read(1);
            var account = result.<Integer>Read(2);

            if (account != getAccountId()) {
                sendUndeleteCharacterResponse(CharacterUndeleteResult.unknown, undeleteInfo);

                return;
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHECK_NAME);
            stmt.AddValue(0, undeleteInfo.name);
            queryCallback.SetNextQuery(DB.characters.AsyncQuery(stmt));
        }).WithChainingCallback((queryCallback, result) ->
        {
            if (!result.isEmpty()) {
                sendUndeleteCharacterResponse(CharacterUndeleteResult.NameTakenByThisAccount, undeleteInfo);

                return;
            }

            // @todo: add more safety checks
            // * max char count per account
            // * max death knight count
            // * max demon hunter count
            // * team violation

            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_SUM_CHARS);
            stmt.AddValue(0, getAccountId());
            queryCallback.SetNextQuery(DB.characters.AsyncQuery(stmt));
        }).WithCallback(result ->
        {
            if (!result.isEmpty()) {
                if (result.<Long>Read(0) >= WorldConfig.getUIntValue(WorldCfg.CharactersPerRealm)) // SQL's COUNT() returns uint64 but it will always be less than uint8.Max
                {
                    sendUndeleteCharacterResponse(CharacterUndeleteResult.CharCreate, undeleteInfo);

                    return;
                }
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_RESTORE_DELETE_INFO);
            stmt.AddValue(0, undeleteInfo.name);
            stmt.AddValue(1, getAccountId());
            stmt.AddValue(2, undeleteInfo.characterGuid.getCounter());
            DB.characters.execute(stmt);

            stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_LAST_CHAR_UNDELETE);
            stmt.AddValue(0, getBattlenetAccountId());
            DB.Login.execute(stmt);

            global.getCharacterCacheStorage().updateCharacterInfoDeleted(undeleteInfo.characterGuid, false, undeleteInfo.name);

            sendUndeleteCharacterResponse(CharacterUndeleteResult.Ok, undeleteInfo);
        }));
    }


    private void handleRepopRequest(RepopRequest packet) {
        if (getPlayer().isAlive() || getPlayer().hasPlayerFlag(playerFlags.Ghost)) {
            return;
        }

        if (getPlayer().hasAuraType(AuraType.PreventResurrection)) {
            return; // silently return, client should display the error by itself
        }

        // the world update order is sessions, players, creatures
        // the netcode runs in parallel with all of these
        // creatures can kill players
        // so if the server is lagging enough the player can
        // release spirit after he's killed but before he is updated
        if (getPlayer().deathState == deathState.JustDied) {
            Log.outDebug(LogFilter.Network, "HandleRepopRequestOpcode: got request after player {0} ({1}) was killed and before he was updated", getPlayer().getName(), getPlayer().getGUID().toString());

            getPlayer().killPlayer();
        }

        //this is spirit release confirm?
        getPlayer().removePet(null, PetSaveMode.NotInSlot, true);
        getPlayer().buildPlayerRepop();
        getPlayer().repopAtGraveyard();
    }


    private void handlePortGraveyard(PortGraveyard packet) {
        if (getPlayer().isAlive() || !getPlayer().hasPlayerFlag(playerFlags.Ghost)) {
            return;
        }

        getPlayer().repopAtGraveyard();
    }


    private void handleRequestCemeteryList(RequestCemeteryList requestCemeteryList) {
        var zoneId = getPlayer().getZoneId();
        var team = (int) getPlayer().getTeam().getValue();

        ArrayList<Integer> graveyardIds = new ArrayList<>();
        var range = global.getObjectMgr().graveYardStorage.get(zoneId);

        for (int i = 0; i < range.size() && graveyardIds.size() < 16; ++i) // client max
        {
            var gYard = range[(int) i];

            if (gYard.team == 0 || gYard.team == team) {
                graveyardIds.add(i);
            }
        }

        if (graveyardIds.isEmpty()) {
            Log.outDebug(LogFilter.Network, "No graveyards found for zone {0} for player {1} (team {2}) in CMSG_REQUEST_CEMETERY_LIST", zoneId, guidLow, team);

            return;
        }

        RequestCemeteryListResponse packet = new RequestCemeteryListResponse();
        packet.isGossipTriggered = false;

        for (var id : graveyardIds) {
            packet.cemeteryID.add(id);
        }

        sendPacket(packet);
    }


    private void handleReclaimCorpse(ReclaimCorpse packet) {
        if (getPlayer().isAlive()) {
            return;
        }

        // do not allow corpse reclaim in arena
        if (getPlayer().getInArena()) {
            return;
        }

        // body not released yet
        if (!getPlayer().hasPlayerFlag(playerFlags.Ghost)) {
            return;
        }

        var corpse = getPlayer().getCorpse();

        if (!corpse) {
            return;
        }

        // prevent resurrect before 30-sec delay after body release not finished
        if ((corpse.getGhostTime() + getPlayer().getCorpseReclaimDelay(corpse.getCorpseType() == CorpseType.ResurrectablePVP)) > gameTime.GetGameTime()) {
            return;
        }

        if (!corpse.isWithinDistInMap(getPlayer(), 39, true)) {
            return;
        }

        // resurrect
        getPlayer().resurrectPlayer(getPlayer().getInBattleground() ? 1.0f : 0.5f);

        // spawn bones
        getPlayer().spawnCorpseBones();
    }


    private void handleResurrectResponse(ResurrectResponse packet) {
        if (getPlayer().isAlive()) {
            return;
        }

        if (packet.response != 0) // accept = 0 Decline = 1 timeout = 2
        {
            getPlayer().clearResurrectRequestData(); // reject

            return;
        }

        if (!getPlayer().isRessurectRequestedBy(packet.resurrecter)) {
            return;
        }

        var ressPlayer = global.getObjAccessor().getPlayer(getPlayer(), packet.resurrecter);

        if (ressPlayer) {
            var instance = ressPlayer.InstanceScript;

            if (instance != null) {
                if (instance.isEncounterInProgress()) {
                    if (instance.getCombatResurrectionCharges() == 0) {
                        return;
                    } else {
                        instance.useCombatResurrection();
                    }
                }
            }
        }

        getPlayer().resurrectUsingRequestData();
    }


    private void handleStandStateChange(StandStateChange packet) {
        switch (packet.standState) {
            case Stand:
            case Sit:
            case Sleep:
            case Kneel:
                break;
            default:
                return;
        }

        getPlayer().setStandState(packet.standState);
    }


    private void handleQuickJoinAutoAcceptRequests(QuickJoinAutoAcceptRequest packet) {
        getPlayer().setAutoAcceptQuickJoin(packet.autoAccept);
    }


    private void handleOverrideScreenFlash(OverrideScreenFlash packet) {
        getPlayer().setOverrideScreenFlash(packet.screenFlashEnabled);
    }

    private void sendCharCreate(ResponseCodes result) {
        sendCharCreate(result, null);
    }

    private void sendCharCreate(ResponseCodes result, ObjectGuid guid) {
        CreateChar response = new CreateChar();
        response.code = result;
        response.guid = guid;

        sendPacket(response);
    }

    private void sendCharDelete(ResponseCodes result) {
        DeleteChar response = new DeleteChar();
        response.code = result;

        sendPacket(response);
    }

    private void sendCharRename(ResponseCodes result, CharacterRenameInfo renameInfo) {
        CharacterRenameResult packet = new CharacterRenameResult();
        packet.result = result;
        packet.name = renameInfo.newName;

        if (result == ResponseCodes.success) {
            packet.guid = renameInfo.guid;
        }

        sendPacket(packet);
    }

    private void sendCharCustomize(ResponseCodes result, CharCustomizeInfo customizeInfo) {
        if (result == ResponseCodes.success) {
            CharCustomizeSuccess response = new CharCustomizeSuccess(customizeInfo);
            sendPacket(response);
        } else {
            CharCustomizeFailure failed = new CharCustomizeFailure();
            failed.result = (byte) result.getValue();
            failed.charGUID = customizeInfo.charGUID;
            sendPacket(failed);
        }
    }

    private void sendCharFactionChange(ResponseCodes result, CharRaceOrFactionChangeInfo factionChangeInfo) {
        CharFactionChangeResult packet = new CharFactionChangeResult();
        packet.result = result;
        packet.guid = factionChangeInfo.guid;

        if (result == ResponseCodes.success) {
            packet.display = new CharFactionChangeResult.CharFactionChangeDisplayInfo();
            packet.display.name = factionChangeInfo.name;
            packet.display.sexID = (byte) factionChangeInfo.sexID.getValue();
            packet.display.customizations = factionChangeInfo.customizations;
            packet.display.raceID = (byte) factionChangeInfo.raceID.getValue();
        }

        sendPacket(packet);
    }

    private void sendSetPlayerDeclinedNamesResult(DeclinedNameResult result, ObjectGuid guid) {
        SetPlayerDeclinedNamesResult packet = new SetPlayerDeclinedNamesResult();
        packet.resultCode = result;
        packet.player = guid;

        sendPacket(packet);
    }

    private void sendUndeleteCooldownStatusResponse(int currentCooldown, int maxCooldown) {
        UndeleteCooldownStatusResponse response = new UndeleteCooldownStatusResponse();
        response.onCooldown = (currentCooldown > 0);
        response.maxCooldown = maxCooldown;
        response.currentCooldown = currentCooldown;

        sendPacket(response);
    }

    private void sendUndeleteCharacterResponse(CharacterUndeleteResult result, CharacterUndeleteInfo undeleteInfo) {
        UndeleteCharacterResponse response = new UndeleteCharacterResponse();
        response.undeleteInfo = undeleteInfo;
        response.result = result;

        sendPacket(response);
    }


    private void handleChatMessage(ChatMessage packet) {
        ChatMsg type;

        switch (packet.GetOpcode()) {
            case ChatMessageSay:
                type = ChatMsg.Say;

                break;
            case ChatMessageYell:
                type = ChatMsg.Yell;

                break;
            case ChatMessageGuild:
                type = ChatMsg.guild;

                break;
            case ChatMessageOfficer:
                type = ChatMsg.officer;

                break;
            case ChatMessageParty:
                type = ChatMsg.Party;

                break;
            case ChatMessageRaid:
                type = ChatMsg.raid;

                break;
            case ChatMessageRaidWarning:
                type = ChatMsg.RaidWarning;

                break;
            case ChatMessageInstanceChat:
                type = ChatMsg.InstanceChat;

                break;
            default:
                Log.outError(LogFilter.Network, "HandleMessagechatOpcode : Unknown chat opcode ({0})", packet.GetOpcode());

                return;
        }

        handleChat(type, packet.language, packet.text);
    }


    private void handleChatMessageWhisper(ChatMessageWhisper packet) {
        handleChat(ChatMsg.Whisper, packet.language, packet.text, packet.target);
    }


    private void handleChatMessageChannel(ChatMessageChannel packet) {
        handleChat(ChatMsg.channel, packet.language, packet.text, packet.target, packet.channelGUID);
    }


    private void handleChatMessageEmote(ChatMessageEmote packet) {
        handleChat(ChatMsg.emote, language.Universal, packet.text);
    }

    private void handleChat(ChatMsg type, Language lang, String msg, String target) {
        handleChat(type, lang, msg, target, null);
    }

    private void handleChat(ChatMsg type, Language lang, String msg) {
        handleChat(type, lang, msg, "", null);
    }

    private void handleChat(ChatMsg type, Language lang, String msg, String target, ObjectGuid channelGuid) {
        var sender = getPlayer();

        if (lang == language.Universal && type != ChatMsg.emote) {
            Log.outError(LogFilter.Network, "CMSG_MESSAGECHAT: Possible hacking-attempt: {0} tried to send a message in universal language", getPlayerInfo());
            sendNotification(SysMessage.UnknownLanguage);

            return;
        }

        // prevent talking at unknown language (cheating)
        var languageData = global.getLanguageMgr().getLanguageDescById(lang);

        if (languageData.isEmpty()) {
            sendNotification(SysMessage.UnknownLanguage);

            return;
        }

        if (!languageData.Any(langDesc -> langDesc.skillId == 0 || sender.hasSkill(SkillType.forValue(langDesc.skillId)))) {
            // also check SPELL_AURA_COMPREHEND_LANGUAGE (client offers option to speak in that language)
            if (!sender.hasAuraTypeWithMiscvalue(AuraType.ComprehendLanguage, lang.getValue())) {
                sendNotification(SysMessage.NotLearnedLanguage);

                return;
            }
        }

        // send in universal language if player in .gm on mode (ignore spell effects)
        if (sender.isGameMaster()) {
            lang = language.Universal;
        } else {
            // send in universal language in two side iteration allowed mode
            if (hasPermission(RBACPermissions.TwoSideInteractionChat)) {
                lang = language.Universal;
            } else {
                switch (type) {
                    case Party:
                    case Raid:
                    case RaidWarning:
                        // allow two side chat at group channel if two side group allowed
                        if (WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGroup)) {
                            lang = language.Universal;
                        }

                        break;
                    case Guild:
                    case Officer:
                        // allow two side chat at guild channel if two side guild allowed
                        if (WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGuild)) {
                            lang = language.Universal;
                        }

                        break;
                }
            }

            // but overwrite it by SPELL_AURA_MOD_LANGUAGE auras (only single case used)
            var ModLangAuras = sender.getAuraEffectsByType(AuraType.ModLanguage);

            if (!ModLangAuras.isEmpty()) {
                lang = language.forValue(ModLangAuras.FirstOrDefault().miscValue);
            }
        }

        if (!getCanSpeak()) {
            var timeStr = time.secsToTimeString((long) (MuteTime - gameTime.GetGameTime()), 0, false);
            sendNotification(SysMessage.WaitBeforeSpeaking, timeStr);

            return;
        }

        if (sender.hasAura(1852) && type != ChatMsg.Whisper) {
            sendNotification(global.getObjectMgr().getSysMessage(SysMessage.GmSilence), sender.getName());

            return;
        }

        if (StringUtil.isEmpty(msg)) {
            return;
        }

        if ((new commandHandler(this)).parseCommands(msg)) {
            return;
        }

        switch (type) {
            case Say:
                // Prevent cheating
                if (!sender.isAlive()) {
                    return;
                }

                if (sender.getLevel() < WorldConfig.getIntValue(WorldCfg.ChatSayLevelReq)) {
                    sendNotification(global.getObjectMgr().getSysMessage(SysMessage.SayReq), WorldConfig.getIntValue(WorldCfg.ChatSayLevelReq));

                    return;
                }

                sender.say(msg, lang);

                break;
            case Emote:
                // Prevent cheating
                if (!sender.isAlive()) {
                    return;
                }

                if (sender.getLevel() < WorldConfig.getIntValue(WorldCfg.ChatEmoteLevelReq)) {
                    sendNotification(global.getObjectMgr().getSysMessage(SysMessage.SayReq), WorldConfig.getIntValue(WorldCfg.ChatEmoteLevelReq));

                    return;
                }

                sender.textEmote(msg);

                break;
            case Yell:
                // Prevent cheating
                if (!sender.isAlive()) {
                    return;
                }

                if (sender.getLevel() < WorldConfig.getIntValue(WorldCfg.ChatYellLevelReq)) {
                    sendNotification(global.getObjectMgr().getSysMessage(SysMessage.SayReq), WorldConfig.getIntValue(WorldCfg.ChatYellLevelReq));

                    return;
                }

                sender.yell(msg, lang);

                break;
            case Whisper:
                // @todo implement cross realm whispers (someday)
                var extName = ObjectManager.extractExtendedPlayerName(target);

                tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(extName.name);
                if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
                    extName.name = tempRef_Name.refArgValue;
                    sendChatPlayerNotfoundNotice(target);

                    break;
                } else {
                    extName.name = tempRef_Name.refArgValue;
                }

                var receiver = global.getObjAccessor().FindPlayerByName(extName.name);

                if (!receiver || (lang != language.Addon && !receiver.isAcceptWhispers() && receiver.getSession().hasPermission(RBACPermissions.CanFilterWhispers) && !receiver.isInWhisperWhiteList(sender.getGUID()))) {
                    sendChatPlayerNotfoundNotice(target);

                    return;
                }

                // Apply checks only if receiver is not already in whitelist and if receiver is not a GM with ".whisper on"
                if (!receiver.isInWhisperWhiteList(sender.getGUID()) && !receiver.isGameMasterAcceptingWhispers()) {
                    if (!sender.isGameMaster() && sender.getLevel() < WorldConfig.getIntValue(WorldCfg.ChatWhisperLevelReq)) {
                        sendNotification(global.getObjectMgr().getSysMessage(SysMessage.WhisperReq), WorldConfig.getIntValue(WorldCfg.ChatWhisperLevelReq));

                        return;
                    }

                    if (player.getEffectiveTeam() != receiver.getEffectiveTeam() && !hasPermission(RBACPermissions.TwoSideInteractionChat) && !receiver.isInWhisperWhiteList(sender.getGUID())) {
                        sendChatPlayerNotfoundNotice(target);

                        return;
                    }
                }

                if (player.hasAura(1852) && !receiver.isGameMaster()) {
                    sendNotification(global.getObjectMgr().getSysMessage(SysMessage.GmSilence), player.getName());

                    return;
                }

                if (receiver.getLevel() < WorldConfig.getIntValue(WorldCfg.ChatWhisperLevelReq) || (hasPermission(RBACPermissions.CanFilterWhispers) && !sender.isAcceptWhispers() && !sender.isInWhisperWhiteList(receiver.getGUID()))) {
                    sender.addWhisperWhiteList(receiver.getGUID());
                }

                player.whisper(msg, lang, receiver);

                break;
            case Party: {
                // if player is in Battleground, he cannot say to Battlegroundmembers by /p
                var group = player.getOriginalGroup();

                if (!group) {
                    group = player.getGroup();

                    if (!group || group.isBGGroup()) {
                        return;
                    }
                }

                if (group.isLeader(player.getGUID())) {
                    type = ChatMsg.PartyLeader;
                }

                global.getScriptMgr().onPlayerChat(player, type, lang, msg, group);

                ChatPkt data = new ChatPkt();
                data.initialize(type, lang, sender, null, msg);
                group.broadcastPacket(data, false, group.getMemberGroup(player.getGUID()));
            }

            break;
            case Guild:
                if (player.getGuildId() != 0) {
                    var guild = global.getGuildMgr().getGuildById(player.getGuildId());

                    if (guild) {
                        global.getScriptMgr().onPlayerChat(player, type, lang, msg, guild);

                        guild.broadcastToGuild(this, false, msg, lang == language.Addon ? language.Addon : language.Universal);
                    }
                }

                break;
            case Officer:
                if (player.getGuildId() != 0) {
                    var guild = global.getGuildMgr().getGuildById(player.getGuildId());

                    if (guild) {
                        global.getScriptMgr().onPlayerChat(player, type, lang, msg, guild);

                        guild.broadcastToGuild(this, true, msg, lang == language.Addon ? language.Addon : language.Universal);
                    }
                }

                break;
            case Raid: {
                var group = player.getGroup();

                if (!group || !group.isRaidGroup() || group.isBGGroup()) {
                    return;
                }

                if (group.isLeader(player.getGUID())) {
                    type = ChatMsg.RaidLeader;
                }

                global.getScriptMgr().onPlayerChat(player, type, lang, msg, group);

                ChatPkt data = new ChatPkt();
                data.initialize(type, lang, sender, null, msg);
                group.broadcastPacket(data, false);
            }

            break;
            case RaidWarning: {
                var group = player.getGroup();

                if (!group || !(group.isRaidGroup() || WorldConfig.getBoolValue(WorldCfg.ChatPartyRaidWarnings)) || !(group.isLeader(player.getGUID()) || group.isAssistant(player.getGUID())) || group.isBGGroup()) {
                    return;
                }

                global.getScriptMgr().onPlayerChat(player, type, lang, msg, group);

                ChatPkt data = new ChatPkt();
                //in Battleground, raid warning is sent only to players in Battleground - code is ok
                data.initialize(ChatMsg.RaidWarning, lang, sender, null, msg);
                group.broadcastPacket(data, false);
            }

            break;
            case Channel:
                if (!hasPermission(RBACPermissions.SkipCheckChatChannelReq)) {
                    if (player.getLevel() < WorldConfig.getIntValue(WorldCfg.ChatChannelLevelReq)) {
                        sendNotification(global.getObjectMgr().getSysMessage(SysMessage.ChannelReq), WorldConfig.getIntValue(WorldCfg.ChatChannelLevelReq));

                        return;
                    }
                }

                var chn = !channelGuid.isEmpty() ? ChannelManager.getChannelForPlayerByGuid(channelGuid, sender) : ChannelManager.getChannelForPlayerByNamePart(target, sender);

                if (chn != null) {
                    global.getScriptMgr().onPlayerChat(player, type, lang, msg, chn);
                    chn.say(player.getGUID(), msg, lang);
                }

                break;
            case InstanceChat: {
                var group = player.getGroup();

                if (!group) {
                    return;
                }

                if (group.isLeader(player.getGUID())) {
                    type = ChatMsg.InstanceChatLeader;
                }

                global.getScriptMgr().onPlayerChat(player, type, lang, msg, group);

                ChatPkt packet = new ChatPkt();
                packet.initialize(type, lang, sender, null, msg);
                group.broadcastPacket(packet, false);

                break;
            }
            default:
                Log.outError(LogFilter.ChatSystem, "CHAT: unknown message type {0}, lang: {1}", type, lang);

                break;
        }
    }


    private void handleChatAddonMessage(ChatAddonMessage chatAddonMessage) {
        handleChatAddon(chatAddonMessage.params.type, chatAddonMessage.params.prefix, chatAddonMessage.params.text, chatAddonMessage.params.isLogged);
    }


    private void handleChatAddonMessageTargeted(ChatAddonMessageTargeted chatAddonMessageTargeted) {
        handleChatAddon(chatAddonMessageTargeted.params.type, chatAddonMessageTargeted.params.prefix, chatAddonMessageTargeted.params.text, chatAddonMessageTargeted.params.isLogged, chatAddonMessageTargeted.target, chatAddonMessageTargeted.channelGUID);
    }

    private void handleChatAddon(ChatMsg type, String prefix, String text, boolean isLogged, String target) {
        handleChatAddon(type, prefix, text, isLogged, target, null);
    }

    private void handleChatAddon(ChatMsg type, String prefix, String text, boolean isLogged) {
        handleChatAddon(type, prefix, text, isLogged, "", null);
    }

    private void handleChatAddon(ChatMsg type, String prefix, String text, boolean isLogged, String target, ObjectGuid channelGuid) {
        var sender = getPlayer();

        if (StringUtil.isEmpty(prefix) || prefix.length() > 16) {
            return;
        }

        // Disabled addon channel?
        if (!WorldConfig.getBoolValue(WorldCfg.AddonChannel)) {
            return;
        }

        if (Objects.equals(prefix, AddonChannelCommandHandler.PREFIX) && (new AddonChannelCommandHandler(this)).parseCommands(text)) {
            return;
        }

        switch (type) {
            case Guild:
            case Officer:
                if (sender.getGuildId() != 0) {
                    var guild = global.getGuildMgr().getGuildById(sender.getGuildId());

                    if (guild) {
                        guild.broadcastAddonToGuild(this, type == ChatMsg.officer, text, prefix, isLogged);
                    }
                }

                break;
            case Whisper:
                // @todo implement cross realm whispers (someday)
                var extName = ObjectManager.extractExtendedPlayerName(target);

                tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(extName.name);
                if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
                    extName.name = tempRef_Name.refArgValue;
                    break;
                } else {
                    extName.name = tempRef_Name.refArgValue;
                }

                var receiver = global.getObjAccessor().FindPlayerByName(extName.name);

                if (!receiver) {
                    break;
                }

                sender.whisperAddon(text, prefix, isLogged, receiver);

                break;
            // Messages sent to "RAID" while in a party will get delivered to "PARTY"
            case Party:
            case Raid:
            case InstanceChat: {
                PlayerGroup group = null;
                var subGroup = -1;

                if (type != ChatMsg.InstanceChat) {
                    group = sender.getOriginalGroup();
                }

                if (!group) {
                    group = sender.getGroup();

                    if (!group) {
                        break;
                    }

                    if (type == ChatMsg.Party) {
                        subGroup = sender.getSubGroup();
                    }
                }

                ChatPkt data = new ChatPkt();
                data.initialize(type, isLogged ? language.AddonLogged : language.Addon, sender, null, text, 0, "", locale.enUS, prefix);
                group.broadcastAddonMessagePacket(data, prefix, true, subGroup, sender.getGUID());

                break;
            }
            case Channel:
                var chn = channelGuid != null ? ChannelManager.getChannelForPlayerByGuid(channelGuid.getValue(), sender) : ChannelManager.getChannelForPlayerByNamePart(target, sender);

                if (chn != null) {
                    chn.addonSay(sender.getGUID(), prefix, text, isLogged);
                }

                break;

            default:
                Log.outError(LogFilter.Server, "HandleAddonMessagechat: unknown addon message type {0}", type);

                break;
        }
    }


    private void handleChatMessageAFK(ChatMessageAFK packet) {
        var sender = getPlayer();

        if (sender.isInCombat()) {
            return;
        }

        if (sender.hasAura(1852)) {
            sendNotification(SysMessage.GmSilence, sender.getName());

            return;
        }

        if (sender.isAFK()) // Already AFK
        {
            if (StringUtil.isEmpty(packet.text)) {
                sender.toggleAFK(); // Remove AFK
            } else {
                sender.setAutoReplyMsg(packet.text); // Update message
            }
        } else // New AFK mode
        {
            sender.setAutoReplyMsg(StringUtil.isEmpty(packet.text) ? global.getObjectMgr().getSysMessage(SysMessage.PlayerAfkDefault) : packet.text);

            if (sender.isDND()) {
                sender.toggleDND();
            }

            sender.toggleAFK();
        }

        var guild = sender.getGuild();

        if (guild != null) {
            guild.sendEventAwayChanged(sender.getGUID(), sender.isAFK(), sender.isDND());
        }

        global.getScriptMgr().onPlayerChat(sender, ChatMsg.Afk, language.Universal, packet.text);
    }


    private void handleChatMessageDND(ChatMessageDND packet) {
        var sender = getPlayer();

        if (sender.isInCombat()) {
            return;
        }

        if (sender.hasAura(1852)) {
            sendNotification(SysMessage.GmSilence, sender.getName());

            return;
        }

        if (sender.isDND()) // Already DND
        {
            if (StringUtil.isEmpty(packet.text)) {
                sender.toggleDND(); // Remove DND
            } else {
                sender.setAutoReplyMsg(packet.text); // Update message
            }
        } else // New DND mode
        {
            sender.setAutoReplyMsg(StringUtil.isEmpty(packet.text) ? global.getObjectMgr().getSysMessage(SysMessage.PlayerDndDefault) : packet.text);

            if (sender.isAFK()) {
                sender.toggleAFK();
            }

            sender.toggleDND();
        }

        var guild = sender.getGuild();

        if (guild != null) {
            guild.sendEventAwayChanged(sender.getGUID(), sender.isAFK(), sender.isDND());
        }

        global.getScriptMgr().onPlayerChat(sender, ChatMsg.Dnd, language.Universal, packet.text);
    }


    private void handleEmote(EmoteClient packet) {
        if (!player.isAlive() || player.hasUnitState(UnitState.Died)) {
            return;
        }

        global.getScriptMgr().<IPlayerOnClearEmote>ForEach(p -> p.OnClearEmote(player));
        player.setEmoteState(emote.OneshotNone);
    }


    private void handleTextEmote(CTextEmote packet) {
        if (!player.isAlive()) {
            return;
        }

        if (!getCanSpeak()) {
            var timeStr = time.secsToTimeString((long) (MuteTime - gameTime.GetGameTime()), 0, false);
            sendNotification(SysMessage.WaitBeforeSpeaking, timeStr);

            return;
        }

        global.getScriptMgr().<IPlayerOnTextEmote>ForEach(p -> p.OnTextEmote(player, (int) packet.soundIndex, (int) packet.emoteID, packet.target));
        var em = CliDB.EmotesTextStorage.get(packet.emoteID);

        if (em == null) {
            return;
        }

        var emote = emote.forValue(em.EmoteId);

        switch (emote) {
            case StateSleep:
            case StateSit:
            case StateKneel:
            case OneshotNone:
                break;
            case StateDance:
            case StateRead:
                player.setEmoteState(emote);

                break;
            default:
                // Only allow text-emotes for "dead" entities (feign death included)
                if (player.hasUnitState(UnitState.Died)) {
                    break;
                }

                player.handleEmoteCommand(emote, null, packet.spellVisualKitIDs, packet.sequenceVariation);

                break;
        }

        STextEmote textEmote = new STextEmote();
        textEmote.sourceGUID = player.getGUID();
        textEmote.sourceAccountGUID = getAccountGUID();
        textEmote.targetGUID = packet.target;
        textEmote.emoteID = packet.emoteID;
        textEmote.soundIndex = packet.soundIndex;
        player.sendMessageToSetInRange(textEmote, WorldConfig.getFloatValue(WorldCfg.ListenRangeTextemote), true);

        var unit = global.getObjAccessor().GetUnit(player, packet.target);

        player.updateCriteria(CriteriaType.DoEmote, (int) packet.emoteID, 0, 0, unit);

        // Send scripted event call
        if (unit) {
            var creature = unit.toCreature();

            if (creature) {
                creature.getAI().receiveEmote(player, TextEmotes.forValue(packet.emoteID));
            }
        }

        if (emote != emote.OneshotNone) {
            player.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.Anim);
        }
    }


    private void handleChatIgnoredOpcode(ChatReportIgnored packet) {
        var player = global.getObjAccessor().findPlayer(packet.ignoredGUID);

        if (!player || player.getSession() == null) {
            return;
        }

        ChatPkt data = new ChatPkt();
        data.initialize(ChatMsg.Ignored, language.Universal, player, player, player.getName());
        player.sendPacket(data);
    }

    private void sendChatPlayerNotfoundNotice(String name) {
        sendPacket(new ChatPlayerNotfound(name));
    }

    private void sendPlayerAmbiguousNotice(String name) {
        sendPacket(new ChatPlayerAmbiguous(name));
    }

    private void sendChatRestricted(ChatRestrictionType restriction) {
        sendPacket(new ChatRestricted(restriction));
    }


    private void handleCollectionItemSetFavorite(CollectionItemSetFavorite collectionItemSetFavorite) {
        switch (collectionItemSetFavorite.type) {
            case Toybox:
                getCollectionMgr().toySetFavorite(collectionItemSetFavorite.id, collectionItemSetFavorite.isFavorite);

                break;
            case Appearance: {
                var pair = getCollectionMgr().hasItemAppearance(collectionItemSetFavorite.id);

                if (!pair.Item1 || pair.item2) {
                    return;
                }

                getCollectionMgr().setAppearanceIsFavorite(collectionItemSetFavorite.id, collectionItemSetFavorite.isFavorite);

                break;
            }
            case TransmogSet:
                break;
            default:
                break;
        }
    }


    private void handleAttackSwing(AttackSwing packet) {
        var enemy = global.getObjAccessor().GetUnit(getPlayer(), packet.victim);

        if (!enemy) {
            // stop attack state at client
            sendAttackStop(null);

            return;
        }

        if (!getPlayer().isValidAttackTarget(enemy)) {
            // stop attack state at client
            sendAttackStop(enemy);

            return;
        }

        //! Client explicitly checks the following before sending CMSG_ATTACKSWING packet,
        //! so we'll place the same check here. Note that it might be possible to reuse this snippet
        //! in other places as well.
        var vehicle = getPlayer().getVehicle1();

        if (vehicle) {
            var seat = vehicle.GetSeatForPassenger(getPlayer());

            if (!seat.hasFlag(VehicleSeatFlags.CanAttack)) {
                sendAttackStop(enemy);

                return;
            }
        }

        getPlayer().attack(enemy, true);
    }


    private void handleAttackStop(AttackStop packet) {
        getPlayer().attackStop();
    }


    private void handleSetSheathed(SetSheathed packet) {
        if (packet.currentSheathState >= sheathState.max.getValue()) {
            Log.outError(LogFilter.Network, "Unknown sheath state {0} ??", packet.currentSheathState);

            return;
        }

        getPlayer().setSheath(sheathState.forValue(packet.currentSheathState));
    }

    private void sendAttackStop(Unit enemy) {
        sendPacket(new SAttackStop(getPlayer(), enemy));
    }


    private void handleCanDuel(CanDuel packet) {
        var player = global.getObjAccessor().findPlayer(packet.targetGUID);

        if (!player) {
            return;
        }

        CanDuelResult response = new CanDuelResult();
        response.targetGUID = packet.targetGUID;
        response.result = player.getDuel() == null;
        sendPacket(response);

        if (response.result) {
            if (getPlayer().isMounted()) {
                getPlayer().castSpell(player, 62875);
            } else {
                getPlayer().castSpell(player, 7266);
            }
        }
    }


    private void handleDuelResponse(DuelResponse duelResponse) {
        if (duelResponse.accepted && !duelResponse.forfeited) {
            handleDuelAccepted(duelResponse.arbiterGUID);
        } else {
            handleDuelCancelled();
        }
    }

    private void handleDuelAccepted(ObjectGuid arbiterGuid) {
        var player = getPlayer();

        if (player.getDuel() == null || player == player.getDuel().getInitiator() || player.getDuel().getState() != DuelState.Challenged) {
            return;
        }

        var target = player.getDuel().getOpponent();

        if (target.getPlayerData().duelArbiter != arbiterGuid) {
            return;
        }

        Log.outDebug(LogFilter.Network, "Player 1 is: {0} ({1})", player.getGUID().toString(), player.getName());
        Log.outDebug(LogFilter.Network, "Player 2 is: {0} ({1})", target.getGUID().toString(), target.getName());

        var now = gameTime.GetGameTime();
        player.getDuel().setStartTime(now + 3);
        target.getDuel().setStartTime(now + 3);

        player.getDuel().setState(DuelState.countdown);
        target.getDuel().setState(DuelState.countdown);

        DuelCountdown packet = new DuelCountdown(3000);

        player.sendPacket(packet);
        target.sendPacket(packet);

        player.enablePvpRules();
        target.enablePvpRules();
    }

    private void handleDuelCancelled() {
        var player = getPlayer();

        // no duel requested
        if (player.getDuel() == null || player.getDuel().getState() == DuelState.completed) {
            return;
        }

        // player surrendered in a duel using /forfeit
        if (player.getDuel().getState() == DuelState.inProgress) {
            player.combatStopWithPets(true);
            player.getDuel().getOpponent().combatStopWithPets(true);

            player.castSpell(getPlayer(), 7267, true); // beg
            player.duelComplete(DuelCompleteType.Won);

            return;
        }

        player.duelComplete(DuelCompleteType.Interrupted);
    }


    private void handleGetGarrisonInfo(GetGarrisonInfo getGarrisonInfo) {
        var garrison = player.getGarrison();

        if (garrison != null) {
            garrison.sendInfo();
        }
    }


    private void handleGarrisonPurchaseBuilding(GarrisonPurchaseBuilding garrisonPurchaseBuilding) {
        if (!player.getNPCIfCanInteractWith(garrisonPurchaseBuilding.npcGUID, NPCFlags.NONE, NPCFlags2.GarrisonArchitect)) {
            return;
        }

        var garrison = player.getGarrison();

        if (garrison != null) {
            garrison.placeBuilding(garrisonPurchaseBuilding.plotInstanceID, garrisonPurchaseBuilding.buildingID);
        }
    }


    private void handleGarrisonCancelConstruction(GarrisonCancelConstruction garrisonCancelConstruction) {
        if (!player.getNPCIfCanInteractWith(garrisonCancelConstruction.npcGUID, NPCFlags.NONE, NPCFlags2.GarrisonArchitect)) {
            return;
        }

        var garrison = player.getGarrison();

        if (garrison != null) {
            garrison.cancelBuildingConstruction(garrisonCancelConstruction.plotInstanceID);
        }
    }


    private void handleGarrisonRequestBlueprintAndSpecializationData(GarrisonRequestBlueprintAndSpecializationData garrisonRequestBlueprintAndSpecializationData) {
        var garrison = player.getGarrison();

        if (garrison != null) {
            garrison.sendBlueprintAndSpecializationData();
        }
    }


    private void handleGarrisonGetMapData(GarrisonGetMapData garrisonGetMapData) {
        var garrison = player.getGarrison();

        if (garrison != null) {
            garrison.sendMapData(player);
        }
    }

    public final void sendPartyResult(PartyOperation operation, String member, PartyResult res) {
        sendPartyResult(operation, member, res, 0);
    }

    public final void sendPartyResult(PartyOperation operation, String member, PartyResult res, int val) {
        PartyCommandResult packet = new PartyCommandResult();

        packet.name = member;
        packet.command = (byte) operation.getValue();
        packet.result = (byte) res.getValue();
        packet.resultData = val;
        packet.resultGUID = ObjectGuid.Empty;

        sendPacket(packet);
    }


    private void handlePartyInvite(PartyInviteClient packet) {
        var invitingPlayer = getPlayer();
        var invitedPlayer = global.getObjAccessor().FindPlayerByName(packet.targetName);

        // no player
        if (invitedPlayer == null) {
            sendPartyResult(PartyOperation.Invite, packet.targetName, PartyResult.BadPlayerNameS);

            return;
        }

        // player trying to invite himself (most likely cheating)
        if (invitedPlayer == invitingPlayer) {
            sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.BadPlayerNameS);

            return;
        }

        // restrict invite to GMs
        if (!WorldConfig.getBoolValue(WorldCfg.AllowGmGroup) && !invitingPlayer.isGameMaster() && invitedPlayer.isGameMaster()) {
            sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.BadPlayerNameS);

            return;
        }

        // can't group with
        if (!invitingPlayer.isGameMaster() && !WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGroup) && invitingPlayer.getTeam() != invitedPlayer.getTeam()) {
            sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.PlayerWrongFaction);

            return;
        }

        if (invitingPlayer.getInstanceId() != 0 && invitedPlayer.getInstanceId() != 0 && invitingPlayer.getInstanceId() != invitedPlayer.getInstanceId() && invitingPlayer.getLocation().getMapId() == invitedPlayer.getLocation().getMapId()) {
            sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.TargetNotInInstanceS);

            return;
        }

        // just ignore us
        if (invitedPlayer.getInstanceId() != 0 && invitedPlayer.getDungeonDifficultyId() != invitingPlayer.getDungeonDifficultyId()) {
            sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.IgnoringYouS);

            return;
        }

        if (invitedPlayer.getSocial().hasIgnore(invitingPlayer.getGUID(), invitingPlayer.getSession().getAccountGUID())) {
            sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.IgnoringYouS);

            return;
        }

        if (!invitedPlayer.getSocial().hasFriend(invitingPlayer.getGUID()) && invitingPlayer.getLevel() < WorldConfig.getIntValue(WorldCfg.PartyLevelReq)) {
            sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.InviteRestricted);

            return;
        }

        var group = invitingPlayer.getGroup();

        if (group != null && group.isBGGroup()) {
            group = invitingPlayer.getOriginalGroup();
        }

        if (group == null) {
            group = invitingPlayer.getGroupInvite();
        }

        var group2 = invitedPlayer.getGroup();

        if (group2 != null && group2.isBGGroup()) {
            group2 = invitedPlayer.getOriginalGroup();
        }

        PartyInvite partyInvite;

        // player already in another group or invited
        if (group2 || invitedPlayer.getGroupInvite()) {
            sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.AlreadyInGroupS);

            if (group2) {
                // tell the player that they were invited but it failed as they were already in a group
                partyInvite = new PartyInvite();
                partyInvite.initialize(invitingPlayer, packet.proposedRoles, false);
                invitedPlayer.sendPacket(partyInvite);
            }

            return;
        }

        if (group) {
            // not have permissions for invite
            if (!group.isLeader(invitingPlayer.getGUID()) && !group.isAssistant(invitingPlayer.getGUID())) {
                if (group.isCreated()) {
                    sendPartyResult(PartyOperation.Invite, "", PartyResult.NotLeader);
                }

                return;
            }

            // not have place
            if (group.isFull()) {
                sendPartyResult(PartyOperation.Invite, "", PartyResult.GroupFull);

                return;
            }
        }

        // ok, but group not exist, start a new group
        // but don't create and save the group to the DB until
        // at least one person joins
        if (group == null) {
            group = new PlayerGroup();

            // new group: if can't add then delete
            if (!group.addLeaderInvite(invitingPlayer)) {
                return;
            }

            if (!group.addInvite(invitedPlayer)) {
                group.removeAllInvites();

                return;
            }
        } else {
            // already existed group: if can't add then just leave
            if (!group.addInvite(invitedPlayer)) {
                return;
            }
        }

        partyInvite = new PartyInvite();
        partyInvite.initialize(invitingPlayer, packet.proposedRoles, true);
        invitedPlayer.sendPacket(partyInvite);

        sendPartyResult(PartyOperation.Invite, invitedPlayer.getName(), PartyResult.Ok);
    }


    private void handlePartyInviteResponse(PartyInviteResponse packet) {
        var group = getPlayer().getGroupInvite();

        if (!group) {
            return;
        }

        if (packet.accept) {
            // Remove player from invitees in any case
            group.removeInvite(getPlayer());

            if (Objects.equals(group.getLeaderGUID(), getPlayer().getGUID())) {
                Log.outError(LogFilter.Network, "HandleGroupAcceptOpcode: player {0} ({1}) tried to accept an invite to his own group", getPlayer().getName(), getPlayer().getGUID().toString());

                return;
            }

            // Group is full
            if (group.isFull()) {
                sendPartyResult(PartyOperation.Invite, "", PartyResult.GroupFull);

                return;
            }

            var leader = global.getObjAccessor().findPlayer(group.getLeaderGUID());

            // Forming a new group, create it
            if (!group.isCreated()) {
                // This can happen if the leader is zoning. To be removed once delayed actions for zoning are implemented
                if (!leader) {
                    group.removeAllInvites();

                    return;
                }

                // If we're about to create a group there really should be a leader present
                group.removeInvite(leader);
                group.create(leader);
                global.getGroupMgr().addGroup(group);
            }

            // Everything is fine, do it, PLAYER'S GROUP IS SET IN ADDMEMBER!!!
            if (!group.addMember(getPlayer())) {
                return;
            }

            group.broadcastGroupUpdate();
        } else {
            // Remember leader if online (group will be invalid if group gets disbanded)
            var leader = global.getObjAccessor().findPlayer(group.getLeaderGUID());

            // uninvite, group can be deleted
            getPlayer().uninviteFromGroup();

            if (!leader || leader.getSession() == null) {
                return;
            }

            // report
            GroupDecline decline = new GroupDecline(getPlayer().getName());
            leader.sendPacket(decline);
        }
    }


    private void handlePartyUninvite(PartyUninvite packet) {
        //can't uninvite yourself
        if (Objects.equals(packet.targetGUID, getPlayer().getGUID())) {
            Log.outError(LogFilter.Network, "HandleGroupUninviteGuidOpcode: leader {0}({1}) tried to uninvite himself from the group.", getPlayer().getName(), getPlayer().getGUID().toString());

            return;
        }

        var res = getPlayer().canUninviteFromGroup(packet.targetGUID);

        if (res != PartyResult.Ok) {
            sendPartyResult(PartyOperation.UnInvite, "", res);

            return;
        }

        var grp = getPlayer().getGroup();
        // grp is checked already above in canUninviteFromGroup()

        if (grp.isMember(packet.targetGUID)) {
            player.removeFromGroup(grp, packet.targetGUID, RemoveMethod.kick, getPlayer().getGUID(), packet.reason);

            return;
        }

        var player = grp.getInvited(packet.targetGUID);

        if (player) {
            player.uninviteFromGroup();

            return;
        }

        sendPartyResult(PartyOperation.UnInvite, "", PartyResult.TargetNotInGroupS);
    }


    private void handleSetPartyLeader(SetPartyLeader packet) {
        var player = global.getObjAccessor().findConnectedPlayer(packet.targetGUID);
        var group = getPlayer().getGroup();

        if (!group || !player) {
            return;
        }

        if (!group.isLeader(getPlayer().getGUID()) || player.getGroup() != group) {
            return;
        }

        // Everything's fine, accepted.
        group.changeLeader(packet.targetGUID, packet.partyIndex);
        group.sendUpdate();
    }


    private void handleSetRole(SetRole packet) {
        RoleChangedInform roleChangedInform = new RoleChangedInform();

        var group = getPlayer().getGroup();
        var oldRole = (byte) (group ? group.getLfgRoles(packet.targetGUID) : 0);

        if (oldRole == packet.role) {
            return;
        }

        roleChangedInform.partyIndex = packet.partyIndex;
        roleChangedInform.from = getPlayer().getGUID();
        roleChangedInform.changedUnit = packet.targetGUID;
        roleChangedInform.oldRole = oldRole;
        roleChangedInform.newRole = packet.role;

        if (group) {
            group.broadcastPacket(roleChangedInform, false);
            group.setLfgRoles(packet.targetGUID, LfgRoles.forValue(packet.role));
        } else {
            sendPacket(roleChangedInform);
        }
    }


    private void handleLeaveGroup(LeaveGroup packet) {
        var grp = getPlayer().getGroup();
        var grpInvite = getPlayer().getGroupInvite();

        if (grp == null && grpInvite == null) {
            return;
        }

        if (getPlayer().getInBattleground()) {
            sendPartyResult(PartyOperation.Invite, "", PartyResult.InviteRestricted);

            return;
        }

        /** error handling **/
        /********************/

        // everything's fine, do it
        if (grp != null) {
            sendPartyResult(PartyOperation.Leave, getPlayer().getName(), PartyResult.Ok);
            player.removeFromGroup(RemoveMethod.Leave);
        } else if (grpInvite != null && Objects.equals(grpInvite.getLeaderGUID(), getPlayer().getGUID())) {
            // pending group creation being cancelled
            sendPartyResult(PartyOperation.Leave, getPlayer().getName(), PartyResult.Ok);
            grpInvite.disband();
        }
    }


    private void handleSetLootMethod(SetLootMethod packet) {
        // not allowed to change
        var group = getPlayer().getGroup();

        if (group == null) {
            return;
        }

        if (!group.isLeader(getPlayer().getGUID())) {
            return;
        }

        if (group.isLFGGroup()) {
            return;
        }

        switch (packet.lootMethod) {
            case FreeForAll:
            case MasterLoot:
            case GroupLoot:
            case PersonalLoot:
                break;
            default:
                return;
        }

        if (packet.lootThreshold.getValue() < itemQuality.Uncommon.getValue() || packet.lootThreshold.getValue() > itemQuality.artifact.getValue()) {
            return;
        }

        if (packet.lootMethod == lootMethod.MasterLoot && !group.isMember(packet.lootMasterGUID)) {
            return;
        }

        // everything's fine, do it
        group.setLootMethod(packet.lootMethod);
        group.setMasterLooterGuid(packet.lootMasterGUID);
        group.setLootThreshold(packet.lootThreshold);
        group.sendUpdate();
    }


    private void handleMinimapPing(MinimapPingClient packet) {
        if (!getPlayer().getGroup()) {
            return;
        }

        MinimapPing minimapPing = new MinimapPing();
        minimapPing.sender = getPlayer().getGUID();
        minimapPing.positionX = packet.positionX;
        minimapPing.positionY = packet.positionY;
        getPlayer().getGroup().broadcastPacket(minimapPing, true, -1, getPlayer().getGUID());
    }


    private void handleRandomRoll(RandomRollClient packet) {
        if (packet.min > packet.max || packet.max > 1000000) // < 32768 for urand call
        {
            return;
        }

        getPlayer().doRandomRoll(packet.min, packet.max);
    }


    private void handleUpdateRaidTarget(UpdateRaidTarget packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        if (packet.symbol == -1) // target icon request
        {
            group.sendTargetIconList(this, packet.partyIndex);
        } else // target icon update
        {
            if (group.isRaidGroup() && !group.isLeader(getPlayer().getGUID()) && !group.isAssistant(getPlayer().getGUID())) {
                return;
            }

            if (packet.target.isPlayer()) {
                var target = global.getObjAccessor().findConnectedPlayer(packet.target);

                if (!target || target.isHostileTo(getPlayer())) {
                    return;
                }
            }

            group.setTargetIcon((byte) packet.symbol, packet.target, getPlayer().getGUID(), packet.partyIndex);
        }
    }


    private void handleConvertRaid(ConvertRaid packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        if (getPlayer().getInBattleground()) {
            return;
        }

        // error handling
        if (!group.isLeader(getPlayer().getGUID()) || group.getMembersCount() < 2) {
            return;
        }

        // everything's fine, do it (is it 0 (PartyOperation.Invite) correct code)
        sendPartyResult(PartyOperation.Invite, "", PartyResult.Ok);

        // New 4.x: it is now possible to convert a raid to a group if member count is 5 or less
        if (packet.raid) {
            group.convertToRaid();
        } else {
            group.convertToGroup();
        }
    }


    private void handleRequestPartyJoinUpdates(RequestPartyJoinUpdates packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        group.sendTargetIconList(this, packet.partyIndex);
        group.sendRaidMarkersChanged(this, packet.partyIndex);
    }


    private void handleChangeSubGroup(ChangeSubGroup packet) {
        // we will get correct for group here, so we don't have to check if group is BG raid
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        if (packet.newSubGroup >= MapDefine.MaxRaidSubGroups) {
            return;
        }

        var senderGuid = getPlayer().getGUID();

        if (!group.isLeader(senderGuid) && !group.isAssistant(senderGuid)) {
            return;
        }

        if (!group.hasFreeSlotSubGroup(packet.newSubGroup)) {
            return;
        }

        group.changeMembersGroup(packet.targetGUID, packet.newSubGroup);
    }


    private void handleSwapSubGroups(SwapSubGroups packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        var senderGuid = getPlayer().getGUID();

        if (!group.isLeader(senderGuid) && !group.isAssistant(senderGuid)) {
            return;
        }

        group.swapMembersGroups(packet.firstTarget, packet.secondTarget);
    }


    private void handleSetAssistantLeader(SetAssistantLeader packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        if (!group.isLeader(getPlayer().getGUID())) {
            return;
        }

        group.setGroupMemberFlag(packet.target, packet.apply, GroupMemberFlags.Assistant);
    }


    private void handleSetPartyAssignment(SetPartyAssignment packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        var senderGuid = getPlayer().getGUID();

        if (!group.isLeader(senderGuid) && !group.isAssistant(senderGuid)) {
            return;
        }

        switch (GroupMemberAssignment.forValue(packet.assignment)) {
            case MainAssist:
                group.removeUniqueGroupMemberFlag(GroupMemberFlags.MainAssist);
                group.setGroupMemberFlag(packet.target, packet.set, GroupMemberFlags.MainAssist);

                break;
            case MainTank:
                group.removeUniqueGroupMemberFlag(GroupMemberFlags.MainTank); // Remove main assist flag from current if any.
                group.setGroupMemberFlag(packet.target, packet.set, GroupMemberFlags.MainTank);

                break;
        }

        group.sendUpdate();
    }


    private void handleDoReadyCheckOpcode(DoReadyCheck packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        /** error handling **/
        if (!group.isLeader(getPlayer().getGUID()) && !group.isAssistant(getPlayer().getGUID())) {
            return;
        }

        // everything's fine, do it
        group.startReadyCheck(getPlayer().getGUID(), packet.partyIndex, duration.ofSeconds(MapDefine.ReadycheckDuration));
    }


    private void handleReadyCheckResponseOpcode(ReadyCheckResponseClient packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        // everything's fine, do it
        group.setMemberReadyCheck(getPlayer().getGUID(), packet.isReady);
    }


    private void handleRequestPartyMemberStats(RequestPartyMemberStats packet) {
        PartyMemberFullState partyMemberStats = new PartyMemberFullState();

        var player = global.getObjAccessor().findConnectedPlayer(packet.targetGUID);

        if (!player) {
            partyMemberStats.memberGuid = packet.targetGUID;
            partyMemberStats.memberStats.status = GroupMemberOnlineStatus.Offline;
        } else {
            partyMemberStats.initialize(player);
        }

        sendPacket(partyMemberStats);
    }


    private void handleRequestRaidInfo(RequestRaidInfo packet) {
        // every time the player checks the character screen
        getPlayer().sendRaidInfo();
    }


    private void handleOptOutOfLoot(OptOutOfLoot packet) {
        // ignore if player not loaded
        if (!getPlayer()) // needed because STATUS_AUTHED
        {
            if (packet.passOnLoot) {
                Log.outError(LogFilter.Network, "CMSG_OPT_OUT_OF_LOOT value<>0 for not-loaded character!");
            }

            return;
        }

        getPlayer().setPassOnGroupLoot(packet.passOnLoot);
    }


    private void handleInitiateRolePoll(InitiateRolePoll packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        var guid = getPlayer().getGUID();

        if (!group.isLeader(guid) && !group.isAssistant(guid)) {
            return;
        }

        RolePollInform rolePollInform = new RolePollInform();
        rolePollInform.from = guid;
        rolePollInform.partyIndex = packet.partyIndex;
        group.broadcastPacket(rolePollInform, true);
    }


    private void handleSetEveryoneIsAssistant(SetEveryoneIsAssistant packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        if (!group.isLeader(getPlayer().getGUID())) {
            return;
        }

        group.setEveryoneIsAssistant(packet.everyoneIsAssistant);
    }


    private void handleClearRaidMarker(ClearRaidMarker packet) {
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        if (group.isRaidGroup() && !group.isLeader(getPlayer().getGUID()) && !group.isAssistant(getPlayer().getGUID())) {
            return;
        }

        group.deleteRaidMarker(packet.markerId);
    }


    private void handleGuildQuery(QueryGuildInfo query) {
        var guild = global.getGuildMgr().getGuildByGuid(query.guildGuid);

        if (guild) {
            guild.sendQueryResponse(this);

            return;
        }

        QueryGuildInfoResponse response = new QueryGuildInfoResponse();
        response.guildGUID = query.guildGuid;
        sendPacket(response);
    }


    private void handleGuildInviteByName(GuildInviteByName packet) {
        tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(packet.name);
        if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
            packet.name = tempRef_Name.refArgValue;
            return;
        } else {
            packet.name = tempRef_Name.refArgValue;
        }

        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleInviteMember(this, packet.name);
        }
    }


    private void handleGuildOfficerRemoveMember(GuildOfficerRemoveMember packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleRemoveMember(this, packet.removee);
        }
    }


    private void handleGuildAcceptInvite(AcceptGuildInvite packet) {
        if (getPlayer().getGuildId() == 0) {
            var guild = global.getGuildMgr().getGuildById(getPlayer().getGuildIdInvited());

            if (guild) {
                guild.handleAcceptMember(this);
            }
        }
    }


    private void handleGuildDeclineInvitation(GuildDeclineInvitation packet) {
        if (getPlayer().getGuildId() != 0) {
            return;
        }

        getPlayer().setGuildIdInvited(0);
        getPlayer().setInGuild(0);
    }


    private void handleGuildGetRoster(GuildGetRoster packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleRoster(this);
        } else {
            guild.sendCommandResult(this, GuildCommandType.GetRoster, GuildCommandError.PlayerNotInGuild);
        }
    }


    private void handleGuildPromoteMember(GuildPromoteMember packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleUpdateMemberRank(this, packet.promotee, false);
        }
    }


    private void handleGuildDemoteMember(GuildDemoteMember packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleUpdateMemberRank(this, packet.demotee, true);
        }
    }


    private void handleGuildAssignRank(GuildAssignMemberRank packet) {
        var setterGuid = getPlayer().getGUID();

        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleSetMemberRank(this, packet.member, setterGuid, GuildRankOrder.forValue(packet.rankOrder));
        }
    }


    private void handleGuildLeave(GuildLeave packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleLeaveMember(this);
        }
    }


    private void handleGuildDisband(GuildDelete packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleDelete(this);
        }
    }


    private void handleGuildUpdateMotdText(GuildUpdateMotdText packet) {
        if (!disallowHyperlinksAndMaybeKick(packet.motdText)) {
            return;
        }

        if (packet.motdText.length() > 255) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleSetMOTD(this, packet.motdText);
        }
    }


    private void handleGuildSetMemberNote(GuildSetMemberNote packet) {
        if (!disallowHyperlinksAndMaybeKick(packet.note)) {
            return;
        }

        if (packet.note.length() > 31) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleSetMemberNote(this, packet.note, packet.noteeGUID, packet.isPublic);
        }
    }


    private void handleGuildGetRanks(GuildGetRanks packet) {
        var guild = global.getGuildMgr().getGuildByGuid(packet.guildGUID);

        if (guild) {
            if (guild.isMember(getPlayer().getGUID())) {
                guild.sendGuildRankInfo(this);
            }
        }
    }


    private void handleGuildAddRank(GuildAddRank packet) {
        if (!disallowHyperlinksAndMaybeKick(packet.name)) {
            return;
        }

        if (packet.name.length() > 15) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleAddNewRank(this, packet.name);
        }
    }


    private void handleGuildDeleteRank(GuildDeleteRank packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleRemoveRank(this, GuildRankOrder.forValue(packet.rankOrder));
        }
    }


    private void handleGuildShiftRank(GuildShiftRank shiftRank) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleShiftRank(this, GuildRankOrder.forValue(shiftRank.rankOrder), shiftRank.shiftUp);
        }
    }


    private void handleGuildUpdateInfoText(GuildUpdateInfoText packet) {
        if (!disallowHyperlinksAndMaybeKick(packet.infoText)) {
            return;
        }

        if (packet.infoText.length() > 500) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleSetInfo(this, packet.infoText);
        }
    }


    private void handleSaveGuildEmblem(SaveGuildEmblem packet) {
        guild.EmblemInfo emblemInfo = new guild.emblemInfo();
        emblemInfo.readPacket(packet);

        if (getPlayer().getNPCIfCanInteractWith(packet.vendor, NPCFlags.TabardDesigner, NPCFlags2.NONE)) {
            // Remove fake death
            if (getPlayer().hasUnitState(UnitState.Died)) {
                getPlayer().removeAurasByType(AuraType.FeignDeath);
            }

            if (!emblemInfo.validateEmblemColors()) {
                guild.sendSaveEmblemResult(this, GuildEmblemError.InvalidTabardColors);

                return;
            }

            var guild = getPlayer().getGuild();

            if (guild) {
                guild.handleSetEmblem(this, emblemInfo);
            } else {
                guild.sendSaveEmblemResult(this, GuildEmblemError.NoGuild); // "You are not part of a guild!";
            }
        } else {
            guild.sendSaveEmblemResult(this, GuildEmblemError.InvalidVendor); // "That's not an emblem vendor!"
        }
    }


    private void handleGuildEventLogQuery(GuildEventLogQuery packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.sendEventLog(this);
        }
    }


    private void handleGuildBankMoneyWithdrawn(GuildBankRemainingWithdrawMoneyQuery packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.sendMoneyInfo(this);
        }
    }


    private void handleGuildPermissionsQuery(GuildPermissionsQuery packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.sendPermissions(this);
        }
    }


    private void handleGuildBankActivate(GuildBankActivate packet) {
        var go = getPlayer().getGameObjectIfCanInteractWith(packet.banker, GameObjectTypes.guildBank);

        if (go == null) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            guild.sendCommandResult(this, GuildCommandType.ViewTab, GuildCommandError.PlayerNotInGuild);

            return;
        }

        guild.sendBankList(this, (byte) 0, packet.fullUpdate);
    }


    private void handleGuildBankQueryTab(GuildBankQueryTab packet) {
        if (getPlayer().getGameObjectIfCanInteractWith(packet.banker, GameObjectTypes.guildBank)) {
            var guild = getPlayer().getGuild();

            if (guild) {
                guild.sendBankList(this, packet.tab, true);
            }
            // HACK: client doesn't query entire tab content if it had received SMSG_GUILD_BANK_LIST in this session
            // but we broadcast bank updates to entire guild when *ANYONE* changes anything, incorrectly initializing clients
            // tab content with only data for that change
        }
    }


    private void handleGuildBankDepositMoney(GuildBankDepositMoney packet) {
        if (getPlayer().getGameObjectIfCanInteractWith(packet.banker, GameObjectTypes.guildBank)) {
            if (packet.money != 0 && getPlayer().hasEnoughMoney(packet.money)) {
                var guild = getPlayer().getGuild();

                if (guild) {
                    guild.handleMemberDepositMoney(this, packet.money);
                }
            }
        }
    }


    private void handleGuildBankWithdrawMoney(GuildBankWithdrawMoney packet) {
        if (packet.money != 0 && getPlayer().getGameObjectIfCanInteractWith(packet.banker, GameObjectTypes.guildBank)) {
            var guild = getPlayer().getGuild();

            if (guild) {
                guild.handleMemberWithdrawMoney(this, packet.money);
            }
        }
    }


    private void handleAutoGuildBankItem(AutoGuildBankItem depositGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(depositGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        if (!player.isInventoryPos((depositGuildBankItem.containerSlot == null ? InventorySlots.Bag0 : depositGuildBankItem.containerSlot.byteValue()), depositGuildBankItem.containerItemSlot)) {
            getPlayer().sendEquipError(InventoryResult.InternalBagError, null);
        } else {
            guild.swapItemsWithInventory(getPlayer(), false, depositGuildBankItem.bankTab, depositGuildBankItem.bankSlot, (depositGuildBankItem.containerSlot == null ? InventorySlots.Bag0 : depositGuildBankItem.containerSlot.byteValue()), depositGuildBankItem.containerItemSlot, 0);
        }
    }


    private void handleStoreGuildBankItem(StoreGuildBankItem storeGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(storeGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        if (!player.isInventoryPos((storeGuildBankItem.containerSlot == null ? InventorySlots.Bag0 : storeGuildBankItem.containerSlot.byteValue()), storeGuildBankItem.containerItemSlot)) {
            getPlayer().sendEquipError(InventoryResult.InternalBagError, null);
        } else {
            guild.swapItemsWithInventory(getPlayer(), true, storeGuildBankItem.bankTab, storeGuildBankItem.bankSlot, (storeGuildBankItem.containerSlot == null ? InventorySlots.Bag0 : storeGuildBankItem.containerSlot.byteValue()), storeGuildBankItem.containerItemSlot, 0);
        }
    }


    private void handleSwapItemWithGuildBankItem(SwapItemWithGuildBankItem swapItemWithGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(swapItemWithGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        if (!player.isInventoryPos((swapItemWithGuildBankItem.containerSlot == null ? InventorySlots.Bag0 : swapItemWithGuildBankItem.containerSlot.byteValue()), swapItemWithGuildBankItem.containerItemSlot)) {
            getPlayer().sendEquipError(InventoryResult.InternalBagError, null);
        } else {
            guild.swapItemsWithInventory(getPlayer(), false, swapItemWithGuildBankItem.bankTab, swapItemWithGuildBankItem.bankSlot, (swapItemWithGuildBankItem.containerSlot == null ? InventorySlots.Bag0 : swapItemWithGuildBankItem.containerSlot.byteValue()), swapItemWithGuildBankItem.containerItemSlot, 0);
        }
    }


    private void handleSwapGuildBankItemWithGuildBankItem(SwapGuildBankItemWithGuildBankItem swapGuildBankItemWithGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(swapGuildBankItemWithGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        guild.swapItems(getPlayer(), swapGuildBankItemWithGuildBankItem.BankTab[0], swapGuildBankItemWithGuildBankItem.BankSlot[0], swapGuildBankItemWithGuildBankItem.BankTab[1], swapGuildBankItemWithGuildBankItem.BankSlot[1], 0);
    }


    private void handleMoveGuildBankItem(MoveGuildBankItem moveGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(moveGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        guild.swapItems(getPlayer(), moveGuildBankItem.bankTab, moveGuildBankItem.bankSlot, moveGuildBankItem.bankTab1, moveGuildBankItem.bankSlot1, 0);
    }


    private void handleMergeItemWithGuildBankItem(MergeItemWithGuildBankItem mergeItemWithGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(mergeItemWithGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        if (!player.isInventoryPos((mergeItemWithGuildBankItem.containerSlot == null ? InventorySlots.Bag0 : mergeItemWithGuildBankItem.containerSlot.byteValue()), mergeItemWithGuildBankItem.containerItemSlot)) {
            getPlayer().sendEquipError(InventoryResult.InternalBagError, null);
        } else {
            guild.swapItemsWithInventory(getPlayer(), false, mergeItemWithGuildBankItem.bankTab, mergeItemWithGuildBankItem.bankSlot, (mergeItemWithGuildBankItem.containerSlot == null ? InventorySlots.Bag0 : mergeItemWithGuildBankItem.containerSlot.byteValue()), mergeItemWithGuildBankItem.containerItemSlot, mergeItemWithGuildBankItem.stackCount);
        }
    }


    private void handleSplitItemToGuildBank(SplitItemToGuildBank splitItemToGuildBank) {
        if (!getPlayer().getGameObjectIfCanInteractWith(splitItemToGuildBank.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        if (!player.isInventoryPos((splitItemToGuildBank.containerSlot == null ? InventorySlots.Bag0 : splitItemToGuildBank.containerSlot.byteValue()), splitItemToGuildBank.containerItemSlot)) {
            getPlayer().sendEquipError(InventoryResult.InternalBagError, null);
        } else {
            guild.swapItemsWithInventory(getPlayer(), false, splitItemToGuildBank.bankTab, splitItemToGuildBank.bankSlot, (splitItemToGuildBank.containerSlot == null ? InventorySlots.Bag0 : splitItemToGuildBank.containerSlot.byteValue()), splitItemToGuildBank.containerItemSlot, splitItemToGuildBank.stackCount);
        }
    }


    private void handleMergeGuildBankItemWithItem(MergeGuildBankItemWithItem mergeGuildBankItemWithItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(mergeGuildBankItemWithItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        if (!player.isInventoryPos((mergeGuildBankItemWithItem.containerSlot == null ? InventorySlots.Bag0 : mergeGuildBankItemWithItem.containerSlot.byteValue()), mergeGuildBankItemWithItem.containerItemSlot)) {
            getPlayer().sendEquipError(InventoryResult.InternalBagError, null);
        } else {
            guild.swapItemsWithInventory(getPlayer(), true, mergeGuildBankItemWithItem.bankTab, mergeGuildBankItemWithItem.bankSlot, (mergeGuildBankItemWithItem.containerSlot == null ? InventorySlots.Bag0 : mergeGuildBankItemWithItem.containerSlot.byteValue()), mergeGuildBankItemWithItem.containerItemSlot, mergeGuildBankItemWithItem.stackCount);
        }
    }


    private void handleSplitGuildBankItemToInventory(SplitGuildBankItemToInventory splitGuildBankItemToInventory) {
        if (!getPlayer().getGameObjectIfCanInteractWith(splitGuildBankItemToInventory.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        if (!player.isInventoryPos((splitGuildBankItemToInventory.containerSlot == null ? InventorySlots.Bag0 : splitGuildBankItemToInventory.containerSlot.byteValue()), splitGuildBankItemToInventory.containerItemSlot)) {
            getPlayer().sendEquipError(InventoryResult.InternalBagError, null);
        } else {
            guild.swapItemsWithInventory(getPlayer(), true, splitGuildBankItemToInventory.bankTab, splitGuildBankItemToInventory.bankSlot, (splitGuildBankItemToInventory.containerSlot == null ? InventorySlots.Bag0 : splitGuildBankItemToInventory.containerSlot.byteValue()), splitGuildBankItemToInventory.containerItemSlot, splitGuildBankItemToInventory.stackCount);
        }
    }


    private void handleAutoStoreGuildBankItem(AutoStoreGuildBankItem autoStoreGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(autoStoreGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        guild.swapItemsWithInventory(getPlayer(), true, autoStoreGuildBankItem.bankTab, autoStoreGuildBankItem.bankSlot, InventorySlots.Bag0, ItemConst.NullSlot, 0);
    }


    private void handleMergeGuildBankItemWithGuildBankItem(MergeGuildBankItemWithGuildBankItem mergeGuildBankItemWithGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(mergeGuildBankItemWithGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        guild.swapItems(getPlayer(), mergeGuildBankItemWithGuildBankItem.bankTab, mergeGuildBankItemWithGuildBankItem.bankSlot, mergeGuildBankItemWithGuildBankItem.bankTab1, mergeGuildBankItemWithGuildBankItem.bankSlot1, mergeGuildBankItemWithGuildBankItem.stackCount);
    }


    private void handleSplitGuildBankItem(SplitGuildBankItem splitGuildBankItem) {
        if (!getPlayer().getGameObjectIfCanInteractWith(splitGuildBankItem.banker, GameObjectTypes.guildBank)) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        guild.swapItems(getPlayer(), splitGuildBankItem.bankTab, splitGuildBankItem.bankSlot, splitGuildBankItem.bankTab1, splitGuildBankItem.bankSlot1, splitGuildBankItem.stackCount);
    }


    private void handleGuildBankBuyTab(GuildBankBuyTab packet) {
        if (packet.banker.isEmpty() || getPlayer().getGameObjectIfCanInteractWith(packet.banker, GameObjectTypes.guildBank)) {
            var guild = getPlayer().getGuild();

            if (guild) {
                guild.handleBuyBankTab(this, packet.bankTab);
            }
        }
    }


    private void handleGuildBankUpdateTab(GuildBankUpdateTab packet) {
        if (!disallowHyperlinksAndMaybeKick(packet.name)) {
            return;
        }

        if ((packet.name.length() > 15) || (packet.icon.length() > 127)) {
            return;
        }

        if (!StringUtil.isEmpty(packet.name) && !StringUtil.isEmpty(packet.icon)) {
            if (getPlayer().getGameObjectIfCanInteractWith(packet.banker, GameObjectTypes.guildBank)) {
                var guild = getPlayer().getGuild();

                if (guild) {
                    guild.handleSetBankTabInfo(this, packet.bankTab, packet.name, packet.icon);
                }
            }
        }
    }


    private void handleGuildBankLogQuery(GuildBankLogQuery packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.sendBankLog(this, (byte) packet.tab);
        }
    }


    private void handleGuildBankTextQuery(GuildBankTextQuery packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.sendBankTabText(this, (byte) packet.tab);
        }
    }


    private void handleGuildBankSetTabText(GuildBankSetTabText packet) {
        if (!disallowHyperlinksAndMaybeKick(packet.tabText)) {
            return;
        }

        if (packet.tabText.length() > 500) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild) {
            guild.setBankTabText((byte) packet.tab, packet.tabText);
        }
    }


    private void handleGuildSetRankPermissions(GuildSetRankPermissions packet) {
        if (!disallowHyperlinksAndMaybeKick(packet.rankName)) {
            return;
        }

        if (packet.rankName.length() > 15) {
            return;
        }

        var guild = getPlayer().getGuild();

        if (guild == null) {
            return;
        }

        var rightsAndSlots = new guild.GuildBankRightsAndSlots[GuildConst.MaxBankTabs];

        for (byte tabId = 0; tabId < GuildConst.MaxBankTabs; ++tabId) {
            rightsAndSlots[tabId] = new guild.GuildBankRightsAndSlots(tabId, (byte) packet.TabFlags[tabId], (int) packet.TabWithdrawItemLimit[tabId]);
        }

        guild.handleSetRankInfo(this, GuildRankId.forValue(packet.rankID), packet.rankName, GuildRankRights.forValue(packet.flags), packet.withdrawGoldLimit, rightsAndSlots);
    }


    private void handleGuildRequestPartyState(RequestGuildPartyState packet) {
        var guild = global.getGuildMgr().getGuildByGuid(packet.guildGUID);

        if (guild) {
            guild.handleGuildPartyRequest(this);
        }
    }


    private void handleGuildChallengeUpdateRequest(GuildChallengeUpdateRequest packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleGuildRequestChallengeUpdate(this);
        }
    }


    private void handleDeclineGuildInvites(DeclineGuildInvites packet) {
        if (packet.allow) {
            getPlayer().setPlayerFlag(playerFlags.AutoDeclineGuild);
        } else {
            getPlayer().removePlayerFlag(playerFlags.AutoDeclineGuild);
        }
    }


    private void handleRequestGuildRewardsList(RequestGuildRewardsList packet) {
        if (global.getGuildMgr().getGuildById(getPlayer().getGuildId())) {
            var rewards = global.getGuildMgr().getGuildRewards();

            GuildRewardList rewardList = new GuildRewardList();
            rewardList.version = gameTime.GetGameTime();

            for (var i = 0; i < rewards.size(); i++) {
                GuildRewardItem rewardItem = new GuildRewardItem();
                rewardItem.itemID = rewards.get(i).itemID;
                rewardItem.raceMask = (int) rewards.get(i).raceMask;
                rewardItem.minGuildLevel = 0;
                rewardItem.minGuildRep = rewards.get(i).minGuildRep;
                rewardItem.achievementsRequired = rewards.get(i).achievementsRequired;
                rewardItem.cost = rewards.get(i).cost;
                rewardList.rewardItems.add(rewardItem);
            }

            sendPacket(rewardList);
        }
    }


    private void handleGuildQueryNews(GuildQueryNews packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            if (Objects.equals(guild.getGUID(), packet.guildGUID)) {
                guild.sendNewsUpdate(this);
            }
        }
    }


    private void handleGuildNewsUpdateSticky(GuildNewsUpdateSticky packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleNewsSetSticky(this, (int) packet.newsID, packet.sticky);
        }
    }


    private void handleGuildReplaceGuildMaster(GuildReplaceGuildMaster replaceGuildMaster) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleSetNewGuildMaster(this, "", true);
        }
    }


    private void handleGuildSetGuildMaster(GuildSetGuildMaster packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleSetNewGuildMaster(this, packet.newMasterName, false);
        }
    }


    private void handleGuildSetAchievementTracking(GuildSetAchievementTracking packet) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleSetAchievementTracking(this, packet.achievementIDs);
        }
    }


    private void handleGuildGetAchievementMembers(GuildGetAchievementMembers getAchievementMembers) {
        var guild = getPlayer().getGuild();

        if (guild) {
            guild.handleGetAchievementMembers(this, getAchievementMembers.achievementID);
        }
    }


    private void handleDBQueryBulk(DBQueryBulk dbQuery) {
        var store = global.getDB2Mgr().GetStorage(dbQuery.tableHash);

        for (var record : dbQuery.queries) {
            DBReply dbReply = new DBReply();
            dbReply.tableHash = dbQuery.tableHash;
            dbReply.recordID = record.recordID;

            if (store != null && store.HasRecord(record.recordID)) {
                dbReply.status = HotfixRecord.status.valid;
                dbReply.timestamp = (int) gameTime.GetGameTime();
                store.WriteRecord(record.recordID, getSessionDbcLocale(), dbReply.data);

                var optionalDataEntries = global.getDB2Mgr().GetHotfixOptionalData(dbQuery.tableHash, record.recordID, getSessionDbcLocale());

                for (var optionalData : optionalDataEntries) {
                    dbReply.data.writeInt32(optionalData.key);
                    dbReply.data.writeBytes(optionalData.data);
                }
            } else {
                Log.outTrace(LogFilter.Network, "CMSG_DB_QUERY_BULK: {0} requested non-existing entry {1} in datastore: {2}", getPlayerInfo(), record.recordID, dbQuery.tableHash);
                dbReply.timestamp = (int) gameTime.GetGameTime();
            }

            sendPacket(dbReply);
        }
    }

    private void sendAvailableHotfixes() {
        sendPacket(new AvailableHotfixes(global.getWorldMgr().getRealmId().GetAddress(), global.getDB2Mgr().GetHotfixData()));
    }


    private void handleHotfixRequest(HotfixRequest hotfixQuery) {
        var hotfixes = global.getDB2Mgr().GetHotfixData();

        HotfixConnect hotfixQueryResponse = new HotfixConnect();

        for (var hotfixId : hotfixQuery.hotfixes) {
            var hotfixRecords = hotfixes.get(hotfixId);

            if (hotfixRecords != null) {
                for (var hotfixRecord : hotfixRecords) {
                    HotfixConnect.HotfixData hotfixData = new HotfixConnect.HotfixData();
                    hotfixData.record = hotfixRecord;

                    if (hotfixRecord.HotfixStatus == HotfixRecord.status.valid) {
                        var storage = global.getDB2Mgr().GetStorage(hotfixRecord.tableHash);

                        if (storage != null && storage.HasRecord((int) hotfixRecord.recordID)) {
                            var pos = hotfixQueryResponse.hotfixContent.getSize();
                            storage.WriteRecord((int) hotfixRecord.recordID, getSessionDbcLocale(), hotfixQueryResponse.hotfixContent);

                            var optionalDataEntries = global.getDB2Mgr().GetHotfixOptionalData(hotfixRecord.tableHash, (int) hotfixRecord.recordID, getSessionDbcLocale());

                            if (optionalDataEntries != null) {
                                for (var optionalData : optionalDataEntries) {
                                    hotfixQueryResponse.hotfixContent.writeInt32(optionalData.key);
                                    hotfixQueryResponse.hotfixContent.writeBytes(optionalData.data);
                                }
                            }

                            hotfixData.size = hotfixQueryResponse.hotfixContent.getSize() - pos;
                        } else {
                            var blobData = global.getDB2Mgr().GetHotfixBlobData(hotfixRecord.tableHash, hotfixRecord.recordID, getSessionDbcLocale());

                            if (blobData != null) {
                                hotfixData.size = (int) blobData.length;
                                hotfixQueryResponse.hotfixContent.writeBytes(blobData);
                            } else {
                                // Do not send Status::Valid when we don't have a hotfix blob for current locale
                                hotfixData.record.HotfixStatus = storage != null ? HotfixRecord.status.RecordRemoved : HotfixRecord.status.Invalid;
                            }
                        }
                    }

                    hotfixQueryResponse.hotfixes.add(hotfixData);
                }
            }
        }

        sendPacket(hotfixQueryResponse);
    }


    private void handleInspect(Inspect inspect) {
        var player = global.getObjAccessor().getPlayer(player, inspect.target);

        if (!player) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleInspectOpcode: Target {0} not found.", inspect.target.toString());

            return;
        }

        if (!getPlayer().isWithinDistInMap(player, SharedConst.InspectDistance, false)) {
            return;
        }

        if (getPlayer().isValidAttackTarget(player)) {
            return;
        }

        InspectResult inspectResult = new InspectResult();
        inspectResult.displayInfo.initialize(player);

        if (getPlayer().getCanBeGameMaster() || WorldConfig.getIntValue(WorldCfg.TalentsInspecting) + (getPlayer().getEffectiveTeam() == player.EffectiveTeam ? 1 : 0) > 1) {
            var talents = player.getTalentMap(player.getActiveTalentGroup());

            for (var v : talents) {
                if (v.value != PlayerSpellState.removed) {
                    inspectResult.talents.add((short) v.key);
                }
            }
        }

        inspectResult.talentTraits = new traitInspectInfo();
        var traitConfig = player.getTraitConfig((int) (int) player.activePlayerData.activeCombatTraitConfigID);

        if (traitConfig != null) {
            inspectResult.talentTraits.config = new traitConfigPacket(traitConfig);
            inspectResult.talentTraits.chrSpecializationID = (int) (int) player.activePlayerData.activeCombatTraitConfigID;
        }

        inspectResult.talentTraits.level = (int) player.level;

        var guild = global.getGuildMgr().getGuildById(player.guildId);

        if (guild) {
            InspectGuildData guildData = new InspectGuildData();
            guildData.guildGUID = guild.getGUID();
            guildData.numGuildMembers = guild.getMembersCount();
            guildData.achievementPoints = (int) guild.getAchievementMgr().getAchievementPoints();

            inspectResult.guildData = guildData;
        }

        var heartOfAzeroth = player.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

        if (heartOfAzeroth != null) {
            var azeriteItem = heartOfAzeroth.AsAzeriteItem;

            if (azeriteItem != null) {
                inspectResult.azeriteLevel = azeriteItem.GetEffectiveLevel();
            }
        }

        inspectResult.itemLevel = (int) player.getAverageItemLevel();
        inspectResult.lifetimeMaxRank = player.activePlayerData.lifetimeMaxRank;
        inspectResult.todayHK = player.activePlayerData.todayHonorableKills;
        inspectResult.yesterdayHK = player.activePlayerData.yesterdayHonorableKills;
        inspectResult.lifetimeHK = player.activePlayerData.lifetimeHonorableKills;
        inspectResult.honorLevel = player.playerData.honorLevel;

        sendPacket(inspectResult);
    }


    private void handleQueryInspectAchievements(QueryInspectAchievements inspect) {
        var player = global.getObjAccessor().getPlayer(player, inspect.guid);

        if (!player) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleQueryInspectAchievements: [{0}] inspected unknown Player [{1}]", getPlayer().getGUID().toString(), inspect.guid.toString());

            return;
        }

        if (!getPlayer().isWithinDistInMap(player, SharedConst.InspectDistance, false)) {
            return;
        }

        if (getPlayer().isValidAttackTarget(player)) {
            return;
        }

        player.sendRespondInspectAchievements(getPlayer());
    }

    public final void sendEnchantmentLog(ObjectGuid owner, ObjectGuid caster, ObjectGuid itemGuid, int itemId, int enchantId, int enchantSlot) {
        EnchantmentLog packet = new EnchantmentLog();
        packet.owner = owner;
        packet.caster = caster;
        packet.itemGUID = itemGuid;
        packet.itemID = itemId;
        packet.enchantment = enchantId;
        packet.enchantSlot = enchantSlot;

        getPlayer().sendMessageToSet(packet, true);
    }

    public final void sendItemEnchantTimeUpdate(ObjectGuid Playerguid, ObjectGuid Itemguid, int slot, int duration) {
        ItemEnchantTimeUpdate data = new ItemEnchantTimeUpdate();
        data.itemGuid = Itemguid;
        data.durationLeft = duration;
        data.slot = slot;
        data.ownerGuid = Playerguid;
        sendPacket(data);
    }


    private void handleSplitItem(SplitItem splitItem) {
        if (!splitItem.inv.items.isEmpty()) {
            Log.outError(LogFilter.Network, "WORLD: HandleSplitItemOpcode - Invalid itemCount ({0})", splitItem.inv.items.size());

            return;
        }

        var src = (short) ((splitItem.fromPackSlot << 8) | splitItem.fromSlot);
        var dst = (short) ((splitItem.toPackSlot << 8) | splitItem.toSlot);

        if (src == dst) {
            return;
        }

        if (splitItem.quantity == 0) {
            return; //check count - if zero it's fake packet
        }

        if (!player.isValidPos(splitItem.fromPackSlot, splitItem.fromSlot, true)) {
            player.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        if (!player.isValidPos(splitItem.toPackSlot, splitItem.toSlot, false)) // can be autostore pos
        {
            player.sendEquipError(InventoryResult.WrongSlot);

            return;
        }

        player.splitItem(src, dst, (int) splitItem.quantity);
    }


    private void handleSwapInvenotryItem(SwapInvItem swapInvItem) {
        if (swapInvItem.inv.items.size() != 2) {
            Log.outError(LogFilter.Network, "WORLD: HandleSwapInvItemOpcode - Invalid itemCount ({0})", swapInvItem.inv.items.size());

            return;
        }

        // prevent attempt swap same item to current position generated by client at special checting sequence
        if (swapInvItem.slot1 == swapInvItem.slot2) {
            return;
        }

        if (!getPlayer().isValidPos(InventorySlots.Bag0, swapInvItem.slot1, true)) {
            getPlayer().sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        if (!getPlayer().isValidPos(InventorySlots.Bag0, swapInvItem.slot2, true)) {
            getPlayer().sendEquipError(InventoryResult.WrongSlot);

            return;
        }

        if (player.isBankPos(InventorySlots.Bag0, swapInvItem.slot1) && !canUseBank()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleSwapInvItemOpcode - %1$s not found or you can't interact with him.", player.getPlayerTalkClass().getInteractionData().getSourceGuid()));

            return;
        }

        if (player.isBankPos(InventorySlots.Bag0, swapInvItem.slot2) && !canUseBank()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleSwapInvItemOpcode - %1$s not found or you can't interact with him.", player.getPlayerTalkClass().getInteractionData().getSourceGuid()));

            return;
        }

        var src = (short) ((InventorySlots.Bag0 << 8) | swapInvItem.slot1);
        var dst = (short) ((InventorySlots.Bag0 << 8) | swapInvItem.slot2);

        getPlayer().swapItem(src, dst);
    }


    private void handleAutoEquipItemSlot(AutoEquipItemSlot packet) {
        // cheating attempt, client should never send opcode in that case
        if (packet.inv.items.size() != 1 || !player.isEquipmentPos(InventorySlots.Bag0, packet.itemDstSlot)) {
            return;
        }

        var item = getPlayer().getItemByGuid(packet.item);
        var dstPos = (short) (packet.itemDstSlot | (InventorySlots.Bag0 << 8));
        var srcPos = (short) (packet.inv.items.get(0).slot | (packet.inv.items.get(0).containerSlot << 8));

        if (item == null || item.getPos() != srcPos || srcPos == dstPos) {
            return;
        }

        getPlayer().swapItem(srcPos, dstPos);
    }


    private void handleSwapItem(SwapItem swapItem) {
        if (swapItem.inv.items.size() != 2) {
            Log.outError(LogFilter.Network, "WORLD: HandleSwapItem - Invalid itemCount ({0})", swapItem.inv.items.size());

            return;
        }

        var src = (short) ((swapItem.containerSlotA << 8) | swapItem.slotA);
        var dst = (short) ((swapItem.containerSlotB << 8) | swapItem.slotB);

        var pl = getPlayer();

        // prevent attempt swap same item to current position generated by client at special checting sequence
        if (src == dst) {
            return;
        }

        if (!pl.isValidPos(swapItem.containerSlotA, swapItem.slotA, true)) {
            pl.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        if (!pl.isValidPos(swapItem.containerSlotB, swapItem.slotB, true)) {
            pl.sendEquipError(InventoryResult.WrongSlot);

            return;
        }


        if (player.isBankPos(swapItem.containerSlotA, swapItem.slotA) && !canUseBank()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleSwapInvItemOpcode - %1$s not found or you can't interact with him.", player.getPlayerTalkClass().getInteractionData().getSourceGuid()));

            return;
        }

        if (player.isBankPos(swapItem.containerSlotB, swapItem.slotB) && !canUseBank()) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleSwapInvItemOpcode - %1$s not found or you can't interact with him.", player.getPlayerTalkClass().getInteractionData().getSourceGuid()));

            return;
        }

        pl.swapItem(src, dst);
    }


    private void handleAutoEquipItem(AutoEquipItem autoEquipItem) {
        if (autoEquipItem.inv.items.size() != 1) {
            Log.outError(LogFilter.Network, "WORLD: HandleAutoEquipItem - Invalid itemCount ({0})", autoEquipItem.inv.items.size());

            return;
        }

        var pl = getPlayer();
        var srcItem = pl.getItemByPos(autoEquipItem.packSlot, autoEquipItem.slot);

        if (srcItem == null) {
            return; // only at cheat
        }

        short dest;
        tangible.OutObject<SHORT> tempOut_dest = new tangible.OutObject<SHORT>();
        var msg = pl.canEquipItem(ItemConst.NullSlot, tempOut_dest, srcItem, !srcItem.isBag());
        dest = tempOut_dest.outArgValue;

        if (msg != InventoryResult.Ok) {
            pl.sendEquipError(msg, srcItem);

            return;
        }

        var src = srcItem.getPos();

        if (dest == src) // prevent equip in same slot, only at cheat
        {
            return;
        }

        var dstItem = pl.getItemByPos(dest);

        if (dstItem == null) // empty slot, simple case
        {
            if (!srcItem.getChildItem().isEmpty()) {
                var childEquipResult = player.canEquipChildItem(srcItem);

                if (childEquipResult != InventoryResult.Ok) {
                    player.sendEquipError(msg, srcItem);

                    return;
                }
            }

            pl.removeItem(autoEquipItem.packSlot, autoEquipItem.slot, true);
            pl.equipItem(dest, srcItem, true);

            if (!srcItem.getChildItem().isEmpty()) {
                player.equipChildItem(autoEquipItem.packSlot, autoEquipItem.slot, srcItem);
            }

            pl.autoUnequipOffhandIfNeed();
        } else // have currently equipped item, not simple case
        {
            var dstbag = dstItem.getBagSlot();
            var dstslot = dstItem.getSlot();

            msg = pl.canUnequipItem(dest, !srcItem.isBag());

            if (msg != InventoryResult.Ok) {
                pl.sendEquipError(msg, dstItem);

                return;
            }

            if (!dstItem.hasItemFlag(ItemFieldFlags.Child)) {
                // check dest.src move possibility
                ArrayList<ItemPosCount> sSrc = new ArrayList<>();
                short eSrc = 0;

                if (pl.isInventoryPos(src)) {
                    msg = pl.canStoreItem(autoEquipItem.packSlot, autoEquipItem.slot, sSrc, dstItem, true);

                    if (msg != InventoryResult.Ok) {
                        msg = pl.canStoreItem(autoEquipItem.packSlot, ItemConst.NullSlot, sSrc, dstItem, true);
                    }

                    if (msg != InventoryResult.Ok) {
                        msg = pl.canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, sSrc, dstItem, true);
                    }
                } else if (player.isBankPos(src)) {
                    msg = pl.canBankItem(autoEquipItem.packSlot, autoEquipItem.slot, sSrc, dstItem, true);

                    if (msg != InventoryResult.Ok) {
                        msg = pl.canBankItem(autoEquipItem.packSlot, ItemConst.NullSlot, sSrc, dstItem, true);
                    }

                    if (msg != InventoryResult.Ok) {
                        msg = pl.canBankItem(ItemConst.NullBag, ItemConst.NullSlot, sSrc, dstItem, true);
                    }
                } else if (player.isEquipmentPos(src)) {
                    tangible.OutObject<SHORT> tempOut_eSrc = new tangible.OutObject<SHORT>();
                    msg = pl.canEquipItem(autoEquipItem.slot, tempOut_eSrc, dstItem, true);
                    eSrc = tempOut_eSrc.outArgValue;

                    if (msg == InventoryResult.Ok) {
                        msg = pl.canUnequipItem(eSrc, true);
                    }
                }

                if (msg == InventoryResult.Ok && player.isEquipmentPos(dest) && !srcItem.getChildItem().isEmpty()) {
                    msg = player.canEquipChildItem(srcItem);
                }

                if (msg != InventoryResult.Ok) {
                    pl.sendEquipError(msg, dstItem, srcItem);

                    return;
                }

                // now do moves, remove...
                pl.removeItem(dstbag, dstslot, false);
                pl.removeItem(autoEquipItem.packSlot, autoEquipItem.slot, false);

                // add to dest
                pl.equipItem(dest, srcItem, true);

                // add to src
                if (pl.isInventoryPos(src)) {
                    pl.storeItem(sSrc, dstItem, true);
                } else if (player.isBankPos(src)) {
                    pl.bankItem(sSrc, dstItem, true);
                } else if (player.isEquipmentPos(src)) {
                    pl.equipItem(eSrc, dstItem, true);
                }

                if (player.isEquipmentPos(dest) && !srcItem.getChildItem().isEmpty()) {
                    player.equipChildItem(autoEquipItem.packSlot, autoEquipItem.slot, srcItem);
                }
            } else {
                var parentItem = player.getItemByGuid(dstItem.getCreator());

                if (parentItem) {
                    if (player.isEquipmentPos(dest)) {
                        player.autoUnequipChildItem(parentItem);
                        // dest is now empty
                        player.swapItem(src, dest);
                        // src is now empty
                        player.swapItem(parentItem.getPos(), src);
                    }
                }
            }

            pl.autoUnequipOffhandIfNeed();

            // if inventory item was moved, check if we can remove dependent auras, because they were not removed in Player::RemoveItem (update was set to false)
            // do this after swaps are done, we pass nullptr because both weapons could be swapped and none of them should be ignored
            if ((autoEquipItem.packSlot == InventorySlots.Bag0 && autoEquipItem.slot < InventorySlots.BagEnd) || (dstbag == InventorySlots.Bag0 && dstslot < InventorySlots.BagEnd)) {
                pl.applyItemDependentAuras(null, false);
            }
        }
    }


    private void handleDestroyItem(DestroyItem destroyItem) {
        var pos = (short) ((destroyItem.containerId << 8) | destroyItem.slotNum);

        // prevent drop unequipable items (in combat, for example) and non-empty bags
        if (player.isEquipmentPos(pos) || player.isBagPos(pos)) {
            var msg = player.canUnequipItem(pos, false);

            if (msg != InventoryResult.Ok) {
                player.sendEquipError(msg, player.getItemByPos(pos));

                return;
            }
        }

        var pItem = player.getItemByPos(destroyItem.containerId, destroyItem.slotNum);

        if (pItem == null) {
            player.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        if (pItem.getTemplate().hasFlag(ItemFlags.NoUserDestroy)) {
            player.sendEquipError(InventoryResult.DropBoundItem);

            return;
        }

        if (destroyItem.count != 0) {
            var i_count = destroyItem.count;
            tangible.RefObject<Integer> tempRef_i_count = new tangible.RefObject<Integer>(i_count);
            player.destroyItemCount(pItem, tempRef_i_count, true);
            i_count = tempRef_i_count.refArgValue;
        } else {
            player.destroyItem(destroyItem.containerId, destroyItem.slotNum, true);
        }
    }


    private void handleReadItem(ReadItem readItem) {
        var item = player.getItemByPos(readItem.packSlot, readItem.slot);

        if (item != null && item.getTemplate().getPageText() != 0) {
            var msg = player.canUseItem(item);

            if (msg == InventoryResult.Ok) {
                ReadItemResultOK packet = new ReadItemResultOK();
                packet.item = item.getGUID();
                sendPacket(packet);
            } else {
                // @todo: 6.x research new values
				/*WorldPackets.item.ReadItemResultFailed packet;
				packet.item = item.getGUID();
				packet.subcode = ??;
				packet.delay = ??;
				sendPacket(packet);*/

                Log.outInfo(LogFilter.Network, "STORAGE: Unable to read item");
                player.sendEquipError(msg, item);
            }
        } else {
            player.sendEquipError(InventoryResult.ItemNotFound);
        }
    }


    private void handleSellItem(SellItem packet) {
        if (packet.itemGUID.isEmpty()) {
            return;
        }

        var pl = getPlayer();

        var creature = pl.getNPCIfCanInteractWith(packet.vendorGUID, NPCFlags.vendor, NPCFlags2.NONE);

        if (creature == null) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleSellItemOpcode - {0} not found or you can not interact with him.", packet.vendorGUID.toString());
            pl.sendSellError(SellResult.CantFindVendor, null, packet.itemGUID);

            return;
        }

        if (creature.getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.NoSellVendor)) {
            player.sendSellError(SellResult.CantSellToThisMerchant, creature, packet.itemGUID);

            return;
        }

        // remove fake death
        if (pl.hasUnitState(UnitState.Died)) {
            pl.removeAurasByType(AuraType.FeignDeath);
        }

        var pItem = pl.getItemByGuid(packet.itemGUID);

        if (pItem != null) {
            // prevent sell not owner item
            if (ObjectGuid.opNotEquals(pl.getGUID(), pItem.getOwnerGUID())) {
                pl.sendSellError(SellResult.CantSellItem, creature, packet.itemGUID);

                return;
            }

            // prevent sell non empty bag by drag-and-drop at vendor's item list
            if (pItem.isNotEmptyBag()) {
                pl.sendSellError(SellResult.CantSellItem, creature, packet.itemGUID);

                return;
            }

            // prevent sell currently looted item
            if (Objects.equals(pl.getLootGUID(), pItem.getGUID())) {
                pl.sendSellError(SellResult.CantSellItem, creature, packet.itemGUID);

                return;
            }

            // prevent selling item for sellprice when the item is still refundable
            // this probably happens when right clicking a refundable item, the client sends both
            // CMSG_SELL_ITEM and CMSG_REFUND_ITEM (unverified)
            if (pItem.isRefundable()) {
                return; // Therefore, no feedback to client
            }

            // special case at auto sell (sell all)
            if (packet.amount == 0) {
                packet.amount = pItem.getCount();
            } else {
                // prevent sell more items that exist in stack (possible only not from client)
                if (packet.amount > pItem.getCount()) {
                    pl.sendSellError(SellResult.CantSellItem, creature, packet.itemGUID);

                    return;
                }
            }

            var pProto = pItem.getTemplate();

            if (pProto != null) {
                if (pProto.getSellPrice() > 0) {
                    long money = pProto.getSellPrice() * packet.amount;

                    if (!player.modifyMoney((long) money)) // ensure player doesn't exceed gold limit
                    {
                        player.sendSellError(SellResult.CantSellItem, creature, packet.itemGUID);

                        return;
                    }

                    player.updateCriteria(CriteriaType.MoneyEarnedFromSales, money);
                    player.updateCriteria(CriteriaType.SellItemsToVendors, 1);

                    if (packet.amount < pItem.getCount()) // need split items
                    {
                        var pNewItem = pItem.cloneItem(packet.amount, pl);

                        if (pNewItem == null) {
                            Log.outError(LogFilter.Network, "WORLD: HandleSellItemOpcode - could not create clone of item {0}; count = {1}", pItem.getEntry(), packet.amount);
                            pl.sendSellError(SellResult.CantSellItem, creature, packet.itemGUID);

                            return;
                        }

                        pItem.setCount(pItem.getCount() - packet.amount);
                        pl.itemRemovedQuestCheck(pItem.getEntry(), packet.amount);

                        if (pl.isInWorld()) {
                            pItem.sendUpdateToPlayer(pl);
                        }

                        pItem.setState(ItemUpdateState.changed, pl);

                        pl.addItemToBuyBackSlot(pNewItem);

                        if (pl.isInWorld()) {
                            pNewItem.sendUpdateToPlayer(pl);
                        }
                    } else {
                        pl.removeItem(pItem.getBagSlot(), pItem.getSlot(), true);
                        pl.itemRemovedQuestCheck(pItem.getEntry(), pItem.getCount());
                        item.removeItemFromUpdateQueueOf(pItem, pl);
                        pl.addItemToBuyBackSlot(pItem);
                    }
                } else {
                    pl.sendSellError(SellResult.CantSellItem, creature, packet.itemGUID);
                }

                return;
            }
        }

        pl.sendSellError(SellResult.CantSellItem, creature, packet.itemGUID);

        return;
    }


    private void handleBuybackItem(BuyBackItem packet) {
        var creature = player.getNPCIfCanInteractWith(packet.vendorGUID, NPCFlags.vendor, NPCFlags2.NONE);

        if (creature == null) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBuybackItem - {0} not found or you can not interact with him.", packet.vendorGUID.toString());
            player.sendSellError(SellResult.CantFindVendor, null, ObjectGuid.Empty);

            return;
        }

        // remove fake death
        if (player.hasUnitState(UnitState.Died)) {
            player.removeAurasByType(AuraType.FeignDeath);
        }

        var pItem = player.getItemFromBuyBackSlot(packet.slot);

        if (pItem != null) {
            var price = player.getActivePlayerData().buybackPrice.get((int) (packet.Slot - InventorySlots.BuyBackStart));

            if (!player.hasEnoughMoney(price)) {
                player.sendBuyError(BuyResult.NotEnoughtMoney, creature, pItem.getEntry());

                return;
            }

            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = player.canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, dest, pItem, false);

            if (msg == InventoryResult.Ok) {
                player.modifyMoney(-price);
                player.removeItemFromBuyBackSlot(packet.slot, false);
                player.itemAddedQuestCheck(pItem.getEntry(), pItem.getCount());
                player.storeItem(dest, pItem, true);
            } else {
                player.sendEquipError(msg, pItem);
            }

            return;
        } else {
            player.sendBuyError(BuyResult.CantFindItem, creature, 0);
        }
    }


    private void handleBuyItem(BuyItem packet) {
        // client expects count starting at 1, and we send vendorslot+1 to client already
        if (packet.muid > 0) {
            --packet.muid;
        } else {
            return; // cheating
        }

        switch (packet.itemType) {
            case Item:
                var bagItem = getPlayer().getItemByGuid(packet.containerGUID);

                var bag = ItemConst.NullBag;

                if (bagItem != null && bagItem.isBag()) {
                    bag = bagItem.getSlot();
                } else if (Objects.equals(packet.containerGUID, getPlayer().getGUID())) // The client sends the player guid when trying to store an item in the default backpack
                {
                    bag = InventorySlots.Bag0;
                }

                getPlayer().buyItemFromVendorSlot(packet.vendorGUID, packet.muid, packet.item.itemID, (byte) packet.quantity, bag, (byte) packet.slot);

                break;
            case Currency:
                getPlayer().buyCurrencyFromVendorSlot(packet.vendorGUID, packet.muid, packet.item.itemID, (byte) packet.quantity);

                break;
            default:
                Log.outDebug(LogFilter.Network, "WORLD: received wrong itemType {0} in HandleBuyItem", packet.itemType);

                break;
        }
    }


    private void handleAutoStoreBagItem(AutoStoreBagItem packet) {
        if (!packet.inv.items.isEmpty()) {
            Log.outError(LogFilter.Network, "HandleAutoStoreBagItemOpcode - Invalid itemCount ({0})", packet.inv.items.size());

            return;
        }

        var item = getPlayer().getItemByPos(packet.containerSlotA, packet.slotA);

        if (!item) {
            return;
        }

        if (!getPlayer().isValidPos(packet.containerSlotB, ItemConst.NullSlot, false)) // can be autostore pos
        {
            getPlayer().sendEquipError(InventoryResult.WrongSlot);

            return;
        }

        var src = item.getPos();
        InventoryResult msg;

        // check unequip potability for equipped items and bank bags
        if (player.isEquipmentPos(src) || player.isBagPos(src)) {
            msg = getPlayer().canUnequipItem(src, !player.isBagPos(src));

            if (msg != InventoryResult.Ok) {
                getPlayer().sendEquipError(msg, item);

                return;
            }
        }

        ArrayList<ItemPosCount> dest = new ArrayList<>();
        msg = getPlayer().canStoreItem(packet.containerSlotB, ItemConst.NullSlot, dest, item, false);

        if (msg != InventoryResult.Ok) {
            getPlayer().sendEquipError(msg, item);

            return;
        }

        // no-op: placed in same slot
        if (dest.size() == 1 && dest.get(0).pos == src) {
            // just remove grey item state
            getPlayer().sendEquipError(InventoryResult.InternalBagError, item);

            return;
        }

        getPlayer().removeItem(packet.containerSlotA, packet.slotA, true);
        getPlayer().storeItem(dest, item, true);
    }


    private void handleWrapItem(WrapItem packet) {
        if (packet.inv.items.size() != 2) {
            Log.outError(LogFilter.Network, "HandleWrapItem - Invalid itemCount ({0})", packet.inv.items.size());

            return;
        }

        // Gift
        var giftContainerSlot = packet.inv.items.get(0).containerSlot;
        var giftSlot = packet.inv.items.get(0).slot;
        // Item
        var itemContainerSlot = packet.inv.items.get(1).containerSlot;
        var itemSlot = packet.inv.items.get(1).slot;

        var gift = getPlayer().getItemByPos(giftContainerSlot, giftSlot);

        if (!gift) {
            getPlayer().sendEquipError(InventoryResult.ItemNotFound, gift);

            return;
        }

        if (!gift.getTemplate().hasFlag(ItemFlags.IsWrapper)) // cheating: non-wrapper wrapper
        {
            getPlayer().sendEquipError(InventoryResult.ItemNotFound, gift);

            return;
        }

        var item = getPlayer().getItemByPos(itemContainerSlot, itemSlot);

        if (!item) {
            getPlayer().sendEquipError(InventoryResult.ItemNotFound, item);

            return;
        }

        if (item == gift) // not possable with pacjket from real client
        {
            getPlayer().sendEquipError(InventoryResult.CantWrapWrapped, item);

            return;
        }

        if (item.isEquipped()) {
            getPlayer().sendEquipError(InventoryResult.CantWrapEquipped, item);

            return;
        }

        if (!item.getGiftCreator().isEmpty()) // hasFlag(ITEM_FIELD_FLAGS, ITEM_FLAGS_WRAPPED);
        {
            getPlayer().sendEquipError(InventoryResult.CantWrapWrapped, item);

            return;
        }

        if (item.isBag()) {
            getPlayer().sendEquipError(InventoryResult.CantWrapBags, item);

            return;
        }

        if (item.isSoulBound()) {
            getPlayer().sendEquipError(InventoryResult.CantWrapBound, item);

            return;
        }

        if (item.getMaxStackCount() != 1) {
            getPlayer().sendEquipError(InventoryResult.CantWrapStackable, item);

            return;
        }

        // maybe not correct check  (it is better than nothing)
        if (item.getTemplate().getMaxCount() > 0) {
            getPlayer().sendEquipError(InventoryResult.CantWrapUnique, item);

            return;
        }

        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_GIFT);
        stmt.AddValue(0, item.getOwnerGUID().getCounter());
        stmt.AddValue(1, item.getGUID().getCounter());
        stmt.AddValue(2, item.getEntry());
        stmt.AddValue(3, (int) item.getItemData().dynamicFlags);
        trans.append(stmt);

        item.setEntry(gift.getEntry());

        switch (item.getEntry()) {
            case 5042:
                item.setEntry(5043);

                break;
            case 5048:
                item.setEntry(5044);

                break;
            case 17303:
                item.setEntry(17302);

                break;
            case 17304:
                item.setEntry(17305);

                break;
            case 17307:
                item.setEntry(17308);

                break;
            case 21830:
                item.setEntry(21831);

                break;
        }

        item.setGiftCreator(getPlayer().getGUID());
        item.replaceAllItemFlags(ItemFieldFlags.Wrapped);
        item.setState(ItemUpdateState.changed, getPlayer());

        if (item.getState() == ItemUpdateState.New) // save new item, to have alway for `character_gifts` record in `item_instance`
        {
            // after save it will be impossible to remove the item from the queue
            item.removeItemFromUpdateQueueOf(item, getPlayer());
            item.saveToDB(trans); // item gave inventory record unchanged and can be save standalone
        }

        DB.characters.CommitTransaction(trans);

        int count = 1;
        tangible.RefObject<Integer> tempRef_count = new tangible.RefObject<Integer>(count);
        getPlayer().destroyItemCount(gift, tempRef_count, true);
        count = tempRef_count.refArgValue;
    }


    private void handleSocketGems(SocketGems socketGems) {
        if (socketGems.itemGuid.isEmpty()) {
            return;
        }

        //cheat . tried to socket same gem multiple times
        if ((!socketGems.GemItem[0].isEmpty() && (Objects.equals(socketGems.GemItem[0], socketGems.GemItem[1]) || Objects.equals(socketGems.GemItem[0], socketGems.GemItem[2]))) || (!socketGems.GemItem[1].isEmpty() && (Objects.equals(socketGems.GemItem[1], socketGems.GemItem[2])))) {
            return;
        }

        var itemTarget = getPlayer().getItemByGuid(socketGems.itemGuid);

        if (!itemTarget) //missing item to socket
        {
            return;
        }

        var itemProto = itemTarget.getTemplate();

        if (itemProto == null) {
            return;
        }

        //this slot is excepted when applying / removing meta gem bonus
        var slot = itemTarget.isEquipped() ? itemTarget.getSlot() : ItemConst.NullSlot;

        var gems = new Item[ItemConst.MaxGemSockets];
        var gemData = new ItemDynamicFieldGems[ItemConst.MaxGemSockets];
        var gemProperties = new GemPropertiesRecord[ItemConst.MaxGemSockets];
        var oldGemData = new SocketedGem[ItemConst.MaxGemSockets];


        for (var i = 0; i < ItemConst.MaxGemSockets; ++i) {
            var gem = player.getItemByGuid(socketGems.GemItem[i]);

            if (gem) {
                gems[i] = gem;
                gemData[i].itemId = gem.getEntry();
                gemData[i].context = (byte) gem.getItemData().context;

                for (var b = 0; b < gem.getBonusListIDs().size() && b < 16; ++b) {
                    gemData[i].BonusListIDs[b] = (short) gem.getBonusListIDs().get(b);
                }

                gemProperties[i] = CliDB.GemPropertiesStorage.get(gem.getTemplate().getGemProperties());
            }

            oldGemData[i] = itemTarget.getGem((short) i);
        }

        // Find first prismatic socket
        int firstPrismatic = 0;

        while (firstPrismatic < ItemConst.MaxGemSockets && itemTarget.getSocketColor(firstPrismatic) != 0) {
            ++firstPrismatic;
        }

        for (int i = 0; i < ItemConst.MaxGemSockets; ++i) //check for hack maybe
        {
            if (gemProperties[i] == null) {
                continue;
            }

            // tried to put gem in socket where no socket exists (take care about prismatic sockets)
            if (itemTarget.getSocketColor(i) == 0) {
                // no prismatic socket
                if (itemTarget.getEnchantmentId(EnchantmentSlot.Prismatic) == 0) {
                    return;
                }

                if (i != firstPrismatic) {
                    return;
                }
            }

            // Gem must match socket color
            if (ItemConst.SocketColorToGemTypeMask[itemTarget.getSocketColor(i).getValue()] != gemProperties[i].type) {
                // unless its red, blue, yellow or prismatic
                if (!ItemConst.SocketColorToGemTypeMask[itemTarget.getSocketColor(i).getValue()].hasFlag(SocketColor.Prismatic) || !gemProperties[i].type.hasFlag(SocketColor.Prismatic)) {
                    return;
                }
            }
        }

        // check unique-equipped conditions
        for (var i = 0; i < ItemConst.MaxGemSockets; ++i) {
            if (!gems[i]) {
                continue;
            }

            // continue check for case when attempt add 2 similar unique equipped gems in one item.
            var iGemProto = gems[i].getTemplate();

            // unique item (for new and already placed bit removed enchantments
            if (iGemProto.hasFlag(ItemFlags.UniqueEquippable)) {
                for (var j = 0; j < ItemConst.MaxGemSockets; ++j) {
                    if (i == j) // skip self
                    {
                        continue;
                    }

                    if (gems[j]) {
                        if (iGemProto.getId() == gems[j].getEntry()) {
                            getPlayer().sendEquipError(InventoryResult.ItemUniqueEquippableSocketed, itemTarget);

                            return;
                        }
                    } else if (oldGemData[j] != null) {
                        if (iGemProto.getId() == oldGemData[j].itemId) {
                            getPlayer().sendEquipError(InventoryResult.ItemUniqueEquippableSocketed, itemTarget);

                            return;
                        }
                    }
                }
            }

            // unique limit type item
            var limit_newcount = 0;

            if (iGemProto.getItemLimitCategory() != 0) {
                var limitEntry = CliDB.ItemLimitCategoryStorage.get(iGemProto.getItemLimitCategory());

                if (limitEntry != null) {
                    // NOTE: limitEntry.mode is not checked because if item has limit then it is applied in equip case
                    for (var j = 0; j < ItemConst.MaxGemSockets; ++j) {
                        if (gems[j]) {
                            // new gem
                            if (iGemProto.getItemLimitCategory() == gems[j].getTemplate().getItemLimitCategory()) {
                                ++limit_newcount;
                            }
                        } else if (oldGemData[j] != null) {
                            // existing gem
                            var jProto = global.getObjectMgr().getItemTemplate(oldGemData[j].itemId);

                            if (jProto != null) {
                                if (iGemProto.getItemLimitCategory() == jProto.getItemLimitCategory()) {
                                    ++limit_newcount;
                                }
                            }
                        }
                    }

                    if (limit_newcount > 0 && limit_newcount > player.getItemLimitCategoryQuantity(limitEntry)) {
                        getPlayer().sendEquipError(InventoryResult.ItemUniqueEquippableSocketed, itemTarget);

                        return;
                    }
                }
            }

            // for equipped item check all equipment for duplicate equipped gems
            if (itemTarget.isEquipped()) {
                var res = getPlayer().canEquipUniqueItem(gems[i], slot, (int) Math.max(limit_newcount, 0));

                if (res != 0) {
                    getPlayer().sendEquipError(res, itemTarget);

                    return;
                }
            }
        }

        var SocketBonusActivated = itemTarget.gemsFitSockets(); //save state of socketbonus
        getPlayer().toggleMetaGemsActive(slot, false); //turn off all metagems (except for the target item)

        //if a meta gem is being equipped, all information has to be written to the item before testing if the conditions for the gem are met

        //remove ALL mods - gem can change item level
        if (itemTarget.isEquipped()) {
            player._ApplyItemMods(itemTarget, itemTarget.getSlot(), false);
        }

        for (short i = 0; i < ItemConst.MaxGemSockets; ++i) {
            if (gems[i]) {
                var gemScalingLevel = player.getLevel();
                var fixedLevel = gems[i].getModifier(ItemModifier.TimewalkerLevel);

                if (fixedLevel != 0) {
                    gemScalingLevel = fixedLevel;
                }

                itemTarget.setGem(i, gemData[i], gemScalingLevel);

                if (gemProperties[i] != null && gemProperties[i].EnchantId != 0) {
                    itemTarget.setEnchantment(EnchantmentSlot.Sock1 + i, gemProperties[i].EnchantId, 0, 0, getPlayer().getGUID());
                }

                int gemCount = 1;
                tangible.RefObject<Integer> tempRef_gemCount = new tangible.RefObject<Integer>(gemCount);
                getPlayer().destroyItemCount(gems[i], tempRef_gemCount, true);
                gemCount = tempRef_gemCount.refArgValue;
            }
        }

        if (itemTarget.isEquipped()) {
            player._ApplyItemMods(itemTarget, itemTarget.getSlot(), true);
        }

        var childItem = player.getChildItemByGuid(itemTarget.getChildItem());

        if (childItem) {
            if (childItem.isEquipped()) {
                player._ApplyItemMods(childItem, childItem.getSlot(), false);
            }

            childItem.copyArtifactDataFromParent(itemTarget);

            if (childItem.isEquipped()) {
                player._ApplyItemMods(childItem, childItem.getSlot(), true);
            }
        }

        var SocketBonusToBeActivated = itemTarget.gemsFitSockets(); //current socketbonus state

        if (SocketBonusActivated ^ SocketBonusToBeActivated) //if there was a change...
        {
            getPlayer().applyEnchantment(itemTarget, EnchantmentSlot.Bonus, false);
            itemTarget.setEnchantment(EnchantmentSlot.Bonus, SocketBonusToBeActivated ? itemTarget.getTemplate().getSocketBonus() : 0, 0, 0, getPlayer().getGUID());
            getPlayer().applyEnchantment(itemTarget, EnchantmentSlot.Bonus, true);
            //it is not displayed, client has an inbuilt system to determine if the bonus is activated
        }

        getPlayer().toggleMetaGemsActive(slot, true); //turn on all metagems (except for target item)

        getPlayer().removeTradeableItem(itemTarget);
        itemTarget.clearSoulboundTradeable(getPlayer()); // clear tradeable flag

        itemTarget.sendUpdateSockets();
    }


    private void handleCancelTempEnchantment(CancelTempEnchantment packet) {
        // apply only to equipped item
        if (!player.isEquipmentPos(InventorySlots.Bag0, (byte) packet.slot)) {
            return;
        }

        var item = getPlayer().getItemByPos(InventorySlots.Bag0, (byte) packet.slot);

        if (!item) {
            return;
        }

        if (item.getEnchantmentId(EnchantmentSlot.Temp) == 0) {
            return;
        }

        getPlayer().applyEnchantment(item, EnchantmentSlot.Temp, false);
        item.clearEnchantment(EnchantmentSlot.Temp);
    }


    private void handleGetItemPurchaseData(GetItemPurchaseData packet) {
        var item = getPlayer().getItemByGuid(packet.itemGUID);

        if (!item) {
            Log.outDebug(LogFilter.Network, "HandleGetItemPurchaseData: Item {0} not found!", packet.itemGUID.toString());

            return;
        }

        getPlayer().sendRefundInfo(item);
    }


    private void handleItemRefund(ItemPurchaseRefund packet) {
        var item = getPlayer().getItemByGuid(packet.itemGUID);

        if (!item) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleItemRefund: Item {0} not found!", packet.itemGUID.toString());

            return;
        }

        // Don't try to refund item currently being disenchanted
        if (Objects.equals(getPlayer().getLootGUID(), packet.itemGUID)) {
            return;
        }

        getPlayer().refundItem(item);
    }

    private boolean canUseBank() {
        return canUseBank(null);
    }

    private boolean canUseBank(ObjectGuid bankerGUID) {
        // bankerGUID parameter is optional, set to 0 by default.
        if (bankerGUID.isEmpty()) {
            bankerGUID = player.getPlayerTalkClass().getInteractionData().getSourceGuid();
        }

        var isUsingBankCommand = Objects.equals(bankerGUID, getPlayer().getGUID()) && Objects.equals(bankerGUID, player.getPlayerTalkClass().getInteractionData().getSourceGuid());

        if (!isUsingBankCommand) {
            var creature = getPlayer().getNPCIfCanInteractWith(bankerGUID, NPCFlags.banker, NPCFlags2.NONE);

            if (!creature) {
                return false;
            }
        }

        return true;
    }


    private void handleUseCritterItem(UseCritterItem useCritterItem) {
        var item = getPlayer().getItemByGuid(useCritterItem.itemGuid);

        if (!item) {
            return;
        }

        for (var itemEffect : item.getEffects()) {
            if (itemEffect.triggerType != ItemSpelltriggerType.OnLearn) {
                continue;
            }

            var speciesEntry = BattlePetMgr.getBattlePetSpeciesBySpell((int) itemEffect.spellID);

            if (speciesEntry != null) {
                getBattlePetMgr().addPet(speciesEntry.id, BattlePetMgr.selectPetDisplay(speciesEntry), BattlePetMgr.rollPetBreed(speciesEntry.id), BattlePetMgr.getDefaultPetQuality(speciesEntry.id));
            }
        }

        getPlayer().destroyItem(item.getBagSlot(), item.getSlot(), true);
    }


    private void handleSortBags(SortBags sortBags) {
        // TODO: Implement sorting
        // Placeholder to prevent completely locking out bags clientside
        sendPacket(new BagCleanupFinished());
    }


    private void handleSortBankBags(SortBankBags sortBankBags) {
        // TODO: Implement sorting
        // Placeholder to prevent completely locking out bags clientside
        sendPacket(new BagCleanupFinished());
    }


    private void handleSortReagentBankBags(SortReagentBankBags sortReagentBankBags) {
        // TODO: Implement sorting
        // Placeholder to prevent completely locking out bags clientside
        sendPacket(new BagCleanupFinished());
    }


    private void handleRemoveNewItem(RemoveNewItem removeNewItem) {
        var item = player.getItemByGuid(removeNewItem.getItemGuid());

        if (!item) {
            Log.outDebug(LogFilter.Network, String.format("WorldSession.HandleRemoveNewItem: item (%1$s) not found for %2$s!", removeNewItem.getItemGuid(), getPlayerInfo()));

            return;
        }

        if (item.hasItemFlag(ItemFieldFlags.newItem)) {
            item.removeItemFlag(ItemFieldFlags.newItem);
            item.setState(ItemUpdateState.changed, player);
        }
    }

    public final void sendLfgPlayerLockInfo() {
        // Get Random dungeons that can be done at a certain level and expansion
        var level = getPlayer().getLevel();
        var contentTuningReplacementConditionMask = getPlayer().getPlayerData().ctrOptions.getValue().contentTuningConditionMask;
        var randomDungeons = global.getLFGMgr().getRandomAndSeasonalDungeons(level, (int) getExpansion().getValue(), contentTuningReplacementConditionMask);

        LfgPlayerInfo lfgPlayerInfo = new LfgPlayerInfo();

        // Get player locked Dungeons
        for (var locked : global.getLFGMgr().getLockedDungeons(player.getGUID()).entrySet()) {
            lfgPlayerInfo.blackList.slot.add(new LFGBlackListSlot(locked.getKey(), (int) locked.getValue().lockStatus, locked.getValue().requiredItemLevel, (int) locked.getValue().currentItemLevel, 0));
        }

        for (var slot : randomDungeons) {
            var playerDungeonInfo = new LfgPlayerDungeonInfo();
            playerDungeonInfo.slot = slot;
            playerDungeonInfo.completionQuantity = 1;
            playerDungeonInfo.completionLimit = 1;
            playerDungeonInfo.completionCurrencyID = 0;
            playerDungeonInfo.specificQuantity = 0;
            playerDungeonInfo.specificLimit = 1;
            playerDungeonInfo.overallQuantity = 0;
            playerDungeonInfo.overallLimit = 1;
            playerDungeonInfo.purseWeeklyQuantity = 0;
            playerDungeonInfo.purseWeeklyLimit = 0;
            playerDungeonInfo.purseQuantity = 0;
            playerDungeonInfo.purseLimit = 0;
            playerDungeonInfo.quantity = 1;
            playerDungeonInfo.completedMask = 0;
            playerDungeonInfo.encounterMask = 0;

            var reward = global.getLFGMgr().getRandomDungeonReward(slot, level);

            if (reward != null) {
                var quest = global.getObjectMgr().getQuestTemplate(reward.firstQuest);

                if (quest != null) {
                    playerDungeonInfo.firstReward = !getPlayer().canRewardQuest(quest, false);

                    if (!playerDungeonInfo.firstReward) {
                        quest = global.getObjectMgr().getQuestTemplate(reward.otherQuest);
                    }

                    if (quest != null) {
                        playerDungeonInfo.rewards.rewardMoney = player.getQuestMoneyReward(quest);
                        playerDungeonInfo.rewards.rewardXP = player.getQuestXPReward(quest);

                        for (byte i = 0; i < SharedConst.QuestRewardItemCount; ++i) {
                            var itemId = quest.RewardItemId[i];

                            if (itemId != 0) {
                                playerDungeonInfo.rewards.item.add(new LfgPlayerQuestRewardItem(itemId, quest.RewardItemCount[i]));
                            }
                        }

                        for (byte i = 0; i < SharedConst.QuestRewardCurrencyCount; ++i) {
                            var curencyId = quest.RewardCurrencyId[i];

                            if (curencyId != 0) {
                                playerDungeonInfo.rewards.currency.add(new LfgPlayerQuestRewardCurrency(curencyId, quest.RewardCurrencyCount[i]));
                            }
                        }
                    }
                }
            }

            lfgPlayerInfo.dungeons.add(playerDungeonInfo);
        }

        sendPacket(lfgPlayerInfo);
    }

    public final void sendLfgPartyLockInfo() {
        var guid = getPlayer().getGUID();
        var group = getPlayer().getGroup();

        if (!group) {
            return;
        }

        LfgPartyInfo lfgPartyInfo = new LfgPartyInfo();

        // Get the Locked dungeons of the other party members
        for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
            var plrg = refe.getSource();

            if (!plrg) {
                continue;
            }

            var pguid = plrg.getGUID();

            if (Objects.equals(pguid, guid)) {
                continue;
            }

            LFGBlackList lfgBlackList = new LFGBlackList();
            lfgBlackList.playerGuid = pguid;

            for (var locked : global.getLFGMgr().getLockedDungeons(pguid).entrySet()) {
                lfgBlackList.slot.add(new LFGBlackListSlot(locked.getKey(), (int) locked.getValue().lockStatus, locked.getValue().requiredItemLevel, (int) locked.getValue().currentItemLevel, 0));
            }

            lfgPartyInfo.player.add(lfgBlackList);
        }

        Log.outDebug(LogFilter.Lfg, "SMSG_LFG_PARTY_INFO {0}", getPlayerInfo());
        sendPacket(lfgPartyInfo);
    }

    public final void sendLfgUpdateStatus(LfgUpdateData updateData, boolean party) {
        var join = false;
        var queued = false;

        switch (updateData.updateType) {
            case JoinQueueInitial: // Joined queue outside the dungeon
                join = true;

                break;
            case JoinQueue:
            case AddedToQueue: // Rolecheck Success
                join = true;
                queued = true;

                break;
            case ProposalBegin:
                join = true;

                break;
            case UpdateStatus:
                join = updateData.state != LfgState.Rolecheck && updateData.state != LfgState.NONE;
                queued = updateData.state == LfgState.queued;

                break;
            default:
                break;
        }

        LFGUpdateStatus lfgUpdateStatus = new LFGUpdateStatus();

        var ticket = global.getLFGMgr().getTicket(player.getGUID());

        if (ticket != null) {
            lfgUpdateStatus.ticket = ticket;
        }

        lfgUpdateStatus.subType = (byte) LfgQueueType.Dungeon.getValue(); // other types not implemented
        lfgUpdateStatus.reason = (byte) updateData.updateType.getValue();

        for (var dungeonId : updateData.dungeons) {
            lfgUpdateStatus.slots.add(global.getLFGMgr().getLFGDungeonEntry(dungeonId));
        }

        lfgUpdateStatus.requestedRoles = (int) global.getLFGMgr().getRoles(player.getGUID()).getValue();
        //lfgUpdateStatus.suspendedPlayers;
        lfgUpdateStatus.isParty = party;
        lfgUpdateStatus.notifyUI = true;
        lfgUpdateStatus.joined = join;
        lfgUpdateStatus.lfgJoined = updateData.updateType != LfgUpdateType.RemovedFromQueue;
        lfgUpdateStatus.queued = queued;
        lfgUpdateStatus.queueMapID = global.getLFGMgr().getDungeonMapId(player.getGUID());

        sendPacket(lfgUpdateStatus);
    }

    public final void sendLfgRoleChosen(ObjectGuid guid, LfgRoles roles) {
        RoleChosen roleChosen = new RoleChosen();
        roleChosen.player = guid;
        roleChosen.roleMask = roles;
        roleChosen.accepted = roles.getValue() > 0;
        sendPacket(roleChosen);
    }

    public final void sendLfgRoleCheckUpdate(LfgRoleCheck roleCheck) {
        ArrayList<Integer> dungeons = new ArrayList<>();

        if (roleCheck.rDungeonId != 0) {
            dungeons.add(roleCheck.rDungeonId);
        } else {
            dungeons = roleCheck.dungeons;
        }

        Log.outDebug(LogFilter.Lfg, "SMSG_LFG_ROLE_CHECK_UPDATE {0}", getPlayerInfo());

        LFGRoleCheckUpdate lfgRoleCheckUpdate = new LFGRoleCheckUpdate();
        lfgRoleCheckUpdate.partyIndex = 127;
        lfgRoleCheckUpdate.roleCheckStatus = (byte) roleCheck.state.getValue();
        lfgRoleCheckUpdate.isBeginning = roleCheck.state == LfgRoleCheckState.Initialiting;

        for (var dungeonId : dungeons) {
            lfgRoleCheckUpdate.joinSlots.add(global.getLFGMgr().getLFGDungeonEntry(dungeonId));
        }

        lfgRoleCheckUpdate.groupFinderActivityID = 0;

        if (!roleCheck.roles.isEmpty()) {
            // Leader info MUST be sent 1st :S
            var roles = (byte) roleCheck.roles.find(roleCheck.leader).value;
            lfgRoleCheckUpdate.members.add(new LFGRoleCheckUpdateMember(roleCheck.leader, roles, global.getCharacterCacheStorage().getCharacterCacheByGuid(roleCheck.leader).level, roles > 0));

            for (var it : roleCheck.roles.entrySet()) {
                if (Objects.equals(it.getKey(), roleCheck.leader)) {
                    continue;
                }

                roles = (byte) it.getValue();
                lfgRoleCheckUpdate.members.add(new LFGRoleCheckUpdateMember(it.getKey(), roles, global.getCharacterCacheStorage().getCharacterCacheByGuid(it.getKey()).level, roles > 0));
            }
        }

        sendPacket(lfgRoleCheckUpdate);
    }

    public final void sendLfgJoinResult(LfgJoinResultData joinData) {
        LFGJoinResult lfgJoinResult = new LFGJoinResult();

        var ticket = global.getLFGMgr().getTicket(getPlayer().getGUID());

        if (ticket != null) {
            lfgJoinResult.ticket = ticket;
        }

        lfgJoinResult.result = (byte) joinData.result.getValue();

        if (joinData.result == LfgJoinResult.RoleCheckFailed) {
            lfgJoinResult.resultDetail = (byte) joinData.state.getValue();
        } else if (joinData.result == LfgJoinResult.NoSlots) {
            lfgJoinResult.blackListNames = joinData.playersMissingRequirement;
        }

        for (var it : joinData.lockmap.entrySet()) {
            var blackList = new LFGBlackListPkt();
            blackList.playerGuid = it.getKey();

            for (var lockInfo : it.getValue()) {
                Log.outTrace(LogFilter.Lfg, "SendLfgJoinResult:: {0} DungeonID: {1} Lock status: {2} Required itemLevel: {3} Current itemLevel: {4}", it.getKey().toString(), (lockInfo.key & 0x00FFFFFF), lockInfo.value.lockStatus, lockInfo.value.requiredItemLevel, lockInfo.value.currentItemLevel);

                blackList.slot.add(new LFGBlackListSlot(lockInfo.key, (int) lockInfo.value.lockStatus, lockInfo.value.requiredItemLevel, (int) lockInfo.value.currentItemLevel, 0));
            }

            lfgJoinResult.blackList.add(blackList);
        }

        sendPacket(lfgJoinResult);
    }

    public final void sendLfgQueueStatus(LfgQueueStatusData queueData) {
        Log.outDebug(LogFilter.Lfg, "SMSG_LFG_QUEUE_STATUS {0} state: {1} dungeon: {2}, waitTime: {3}, " + "avgWaitTime: {4}, waitTimeTanks: {5}, waitTimeHealer: {6}, waitTimeDps: {7}, queuedTime: {8}, tanks: {9}, healers: {10}, dps: {11}", getPlayerInfo(), global.getLFGMgr().getState(getPlayer().getGUID()), queueData.dungeonId, queueData.waitTime, queueData.waitTimeAvg, queueData.waitTimeTank, queueData.waitTimeHealer, queueData.waitTimeDps, queueData.queuedTime, queueData.tanks, queueData.healers, queueData.dps);

        LFGQueueStatus lfgQueueStatus = new LFGQueueStatus();

        var ticket = global.getLFGMgr().getTicket(getPlayer().getGUID());

        if (ticket != null) {
            lfgQueueStatus.ticket = ticket;
        }

        lfgQueueStatus.slot = queueData.queueId;
        lfgQueueStatus.avgWaitTimeMe = (int) queueData.waitTime;
        lfgQueueStatus.avgWaitTime = (int) queueData.waitTimeAvg;
        lfgQueueStatus.AvgWaitTimeByRole[0] = (int) queueData.waitTimeTank;
        lfgQueueStatus.AvgWaitTimeByRole[1] = (int) queueData.waitTimeHealer;
        lfgQueueStatus.AvgWaitTimeByRole[2] = (int) queueData.waitTimeDps;
        lfgQueueStatus.LastNeeded[0] = queueData.tanks;
        lfgQueueStatus.LastNeeded[1] = queueData.healers;
        lfgQueueStatus.LastNeeded[2] = queueData.dps;
        lfgQueueStatus.queuedTime = queueData.queuedTime;

        sendPacket(lfgQueueStatus);
    }

    public final void sendLfgPlayerReward(LfgPlayerRewardData rewardData) {
        if (rewardData.rdungeonEntry == 0 || rewardData.sdungeonEntry == 0 || rewardData.quest == null) {
            return;
        }

        Log.outDebug(LogFilter.Lfg, "SMSG_LFG_PLAYER_REWARD {0} rdungeonEntry: {1}, sdungeonEntry: {2}, done: {3}", getPlayerInfo(), rewardData.rdungeonEntry, rewardData.sdungeonEntry, rewardData.done);

        LFGPlayerReward lfgPlayerReward = new LFGPlayerReward();
        lfgPlayerReward.queuedSlot = rewardData.rdungeonEntry;
        lfgPlayerReward.actualSlot = rewardData.sdungeonEntry;
        lfgPlayerReward.rewardMoney = getPlayer().getQuestMoneyReward(rewardData.quest);
        lfgPlayerReward.addedXP = getPlayer().getQuestXPReward(rewardData.quest);

        for (byte i = 0; i < SharedConst.QuestRewardItemCount; ++i) {
            var itemId = rewardData.quest.RewardItemId[i];

            if (itemId != 0) {
                lfgPlayerReward.rewards.add(new LFGPlayerRewards(itemId, rewardData.quest.RewardItemCount[i], 0, false));
            }
        }

        for (byte i = 0; i < SharedConst.QuestRewardCurrencyCount; ++i) {
            var currencyId = rewardData.quest.RewardCurrencyId[i];

            if (currencyId != 0) {
                lfgPlayerReward.rewards.add(new LFGPlayerRewards(currencyId, rewardData.quest.RewardCurrencyCount[i], 0, true));
            }
        }

        sendPacket(lfgPlayerReward);
    }

    public final void sendLfgBootProposalUpdate(LfgPlayerBoot boot) {
        var playerVote = boot.votes.get(getPlayer().getGUID());
        byte votesNum = 0;
        byte agreeNum = 0;
        var secsleft = (int) ((boot.cancelTime - gameTime.GetGameTime()) / 1000);

        for (var it : boot.votes.entrySet()) {
            if (it.getValue() != LfgAnswer.Pending) {
                ++votesNum;

                if (it.getValue() == LfgAnswer.Agree) {
                    ++agreeNum;
                }
            }
        }

        Log.outDebug(LogFilter.Lfg, "SMSG_LFG_BOOT_PROPOSAL_UPDATE {0} inProgress: {1} - didVote: {2} - agree: {3} - victim: {4} votes: {5} - agrees: {6} - left: {7} - needed: {8} - reason {9}", getPlayerInfo(), boot.inProgress, playerVote != LfgAnswer.Pending, playerVote == LfgAnswer.Agree, boot.victim.toString(), votesNum, agreeNum, secsleft, SharedConst.LFGKickVotesNeeded, boot.reason);

        LfgBootPlayer lfgBootPlayer = new LfgBootPlayer();
        lfgBootPlayer.info.voteInProgress = boot.inProgress; // Vote in progress
        lfgBootPlayer.info.votePassed = agreeNum >= SharedConst.LFGKickVotesNeeded; // Did succeed
        lfgBootPlayer.info.myVoteCompleted = playerVote != LfgAnswer.Pending; // Did Vote
        lfgBootPlayer.info.myVote = playerVote == LfgAnswer.Agree; // Agree
        lfgBootPlayer.info.target = boot.victim; // Victim GUID
        lfgBootPlayer.info.totalVotes = votesNum; // Total Votes
        lfgBootPlayer.info.bootVotes = agreeNum; // Agree Count
        lfgBootPlayer.info.timeLeft = secsleft; // Time Left
        lfgBootPlayer.info.votesNeeded = SharedConst.LFGKickVotesNeeded; // Needed Votes
        lfgBootPlayer.info.reason = boot.reason; // Kick reason
        sendPacket(lfgBootPlayer);
    }

    public final void sendLfgProposalUpdate(LfgProposal proposal) {
        var playerGuid = getPlayer().getGUID();
        var guildGuid = proposal.players.get(playerGuid).group;
        var silent = !proposal.isNew && guildGuid == proposal.group;
        var dungeonEntry = proposal.dungeonId;

        Log.outDebug(LogFilter.Lfg, "SMSG_LFG_PROPOSAL_UPDATE {0} state: {1}", getPlayerInfo(), proposal.state);

        // show random dungeon if player selected random dungeon and it's not lfg group
        if (!silent) {
            var playerDungeons = global.getLFGMgr().getSelectedDungeons(playerGuid);

            if (!playerDungeons.contains(proposal.dungeonId)) {
                dungeonEntry = playerDungeons.get(0);
            }
        }

        LFGProposalUpdate lfgProposalUpdate = new LFGProposalUpdate();

        var ticket = global.getLFGMgr().getTicket(getPlayer().getGUID());

        if (ticket != null) {
            lfgProposalUpdate.ticket = ticket;
        }

        lfgProposalUpdate.instanceID = 0;
        lfgProposalUpdate.proposalID = proposal.id;
        lfgProposalUpdate.slot = global.getLFGMgr().getLFGDungeonEntry(dungeonEntry);
        lfgProposalUpdate.state = (byte) proposal.state.getValue();
        lfgProposalUpdate.completedMask = proposal.encounters;
        lfgProposalUpdate.validCompletedMask = true;
        lfgProposalUpdate.proposalSilent = silent;
        lfgProposalUpdate.isRequeue = !proposal.isNew;

        for (var pair : proposal.players.entrySet()) {
            var proposalPlayer = new LFGProposalUpdatePlayer();
            proposalPlayer.roles = (int) pair.getValue().role;
            proposalPlayer.me = (Objects.equals(pair.getKey(), playerGuid));
            proposalPlayer.myParty = !pair.getValue().group.IsEmpty && Objects.equals(pair.getValue().group, proposal.group);
            proposalPlayer.sameParty = !pair.getValue().group.IsEmpty && pair.getValue().group == guildGuid;
            proposalPlayer.responded = (pair.getValue().accept != LfgAnswer.Pending);
            proposalPlayer.accepted = (pair.getValue().accept == LfgAnswer.Agree);

            lfgProposalUpdate.players.add(proposalPlayer);
        }

        sendPacket(lfgProposalUpdate);
    }

    public final void sendLfgDisabled() {
        sendPacket(new LfgDisabled());
    }

    public final void sendLfgOfferContinue(int dungeonEntry) {
        Log.outDebug(LogFilter.Lfg, "SMSG_LFG_OFFER_CONTINUE {0} dungeon entry: {1}", getPlayerInfo(), dungeonEntry);
        sendPacket(new LfgOfferContinue(global.getLFGMgr().getLFGDungeonEntry(dungeonEntry)));
    }

    public final void sendLfgTeleportError(LfgTeleportResult err) {
        Log.outDebug(LogFilter.Lfg, "SMSG_LFG_TELEPORT_DENIED {0} reason: {1}", getPlayerInfo(), err);
        sendPacket(new LfgTeleportDenied(err));
    }


    private void handleLfgJoin(DFJoin dfJoin) {
        if (!global.getLFGMgr().isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue()) || (getPlayer().getGroup() && ObjectGuid.opNotEquals(getPlayer().getGroup().getLeaderGUID(), getPlayer().getGUID()) && (getPlayer().getGroup().getMembersCount() == MapDefine.MaxGroupSize || !getPlayer().getGroup().isLFGGroup()))) {
            return;
        }

        if (dfJoin.slots.isEmpty()) {
            Log.outDebug(LogFilter.Lfg, "CMSG_DF_JOIN {0} no dungeons selected", getPlayerInfo());

            return;
        }

        ArrayList<Integer> newDungeons = new ArrayList<>();

        for (var slot : dfJoin.slots) {
            var dungeon = slot & 0x00FFFFFF;

            if (CliDB.LFGDungeonsStorage.containsKey(dungeon)) {
                newDungeons.add(dungeon);
            }
        }

        Log.outDebug(LogFilter.Lfg, "CMSG_DF_JOIN {0} roles: {1}, Dungeons: {2}", getPlayerInfo(), dfJoin.roles, newDungeons.size());

        global.getLFGMgr().joinLfg(getPlayer(), dfJoin.roles, newDungeons);
    }


    private void handleLfgLeave(DFLeave dfLeave) {
        var group = getPlayer().getGroup();

        Log.outDebug(LogFilter.Lfg, "CMSG_DF_LEAVE {0} in group: {1} sent guid {2}.", getPlayerInfo(), group ? 1 : 0, dfLeave.ticket.requesterGuid.toString());

        // Check cheating - only leader can leave the queue
        if (!group || Objects.equals(group.getLeaderGUID(), dfLeave.ticket.requesterGuid)) {
            global.getLFGMgr().leaveLfg(dfLeave.ticket.requesterGuid);
        }
    }


    private void handleLfgProposalResult(DFProposalResponse dfProposalResponse) {
        Log.outDebug(LogFilter.Lfg, "CMSG_LFG_PROPOSAL_RESULT {0} proposal: {1} accept: {2}", getPlayerInfo(), dfProposalResponse.proposalID, dfProposalResponse.Accepted ? 1 : 0);
        global.getLFGMgr().updateProposal(dfProposalResponse.proposalID, getPlayer().getGUID(), dfProposalResponse.accepted);
    }


    private void handleLfgSetRoles(DFSetRoles dfSetRoles) {
        var guid = getPlayer().getGUID();
        var group = getPlayer().getGroup();

        if (!group) {
            Log.outDebug(LogFilter.Lfg, "CMSG_DF_SET_ROLES {0} Not in group", getPlayerInfo());

            return;
        }

        var gguid = group.getGUID();
        Log.outDebug(LogFilter.Lfg, "CMSG_DF_SET_ROLES: Group {0}, Player {1}, Roles: {2}", gguid.toString(), getPlayerInfo(), dfSetRoles.rolesDesired);
        global.getLFGMgr().updateRoleCheck(gguid, guid, dfSetRoles.rolesDesired);
    }


    private void handleLfgSetBootVote(DFBootPlayerVote dfBootPlayerVote) {
        var guid = getPlayer().getGUID();
        Log.outDebug(LogFilter.Lfg, "CMSG_LFG_SET_BOOT_VOTE {0} agree: {1}", getPlayerInfo(), dfBootPlayerVote.Vote ? 1 : 0);
        global.getLFGMgr().updateBoot(guid, dfBootPlayerVote.vote);
    }


    private void handleLfgTeleport(DFTeleport dfTeleport) {
        Log.outDebug(LogFilter.Lfg, "CMSG_DF_TELEPORT {0} out: {1}", getPlayerInfo(), dfTeleport.TeleportOut ? 1 : 0);
        global.getLFGMgr().teleportPlayer(getPlayer(), dfTeleport.teleportOut, true);
    }


    private void handleDfGetSystemInfo(DFGetSystemInfo dfGetSystemInfo) {
        Log.outDebug(LogFilter.Lfg, "CMSG_LFG_Lock_INFO_REQUEST {0} for {1}", getPlayerInfo(), (dfGetSystemInfo.Player ? "player" : "party"));

        if (dfGetSystemInfo.player) {
            sendLfgPlayerLockInfo();
        } else {
            sendLfgPartyLockInfo();
        }
    }


    private void handleDfGetJoinStatus(DFGetJoinStatus packet) {
        if (!getPlayer().isUsingLfg()) {
            return;
        }

        var guid = getPlayer().getGUID();
        var updateData = global.getLFGMgr().getLfgStatus(guid);

        if (getPlayer().getGroup()) {
            sendLfgUpdateStatus(updateData, true);
            updateData.dungeons.clear();
            sendLfgUpdateStatus(updateData, false);
        } else {
            sendLfgUpdateStatus(updateData, false);
            updateData.dungeons.clear();
            sendLfgUpdateStatus(updateData, true);
        }
    }


    private void handleLogoutRequest(LogoutRequest packet) {
        var pl = getPlayer();

        if (!getPlayer().getLootGUID().isEmpty()) {
            getPlayer().sendLootReleaseAll();
        }

        var instantLogout = (pl.hasPlayerFlag(playerFlags.Resting) && !pl.isInCombat() || pl.isInFlight() || hasPermission(RBACPermissions.InstantLogout));

        var canLogoutInCombat = pl.hasPlayerFlag(playerFlags.Resting);

        var reason = 0;

        if (pl.isInCombat() && !canLogoutInCombat) {
            reason = 1;
        } else if (pl.isFalling()) {
            reason = 3; // is jumping or falling
        } else if (pl.getDuel() != null || pl.hasAura(9454)) // is dueling or frozen by GM via freeze command
        {
            reason = 2; // FIXME - Need the correct value
        }

        LogoutResponse logoutResponse = new LogoutResponse();
        logoutResponse.logoutResult = reason;
        logoutResponse.instant = instantLogout;
        sendPacket(logoutResponse);

        if (reason != 0) {
            setLogoutStartTime(0);

            return;
        }

        // instant logout in taverns/cities or on taxi or for admins, gm's, mod's if its enabled in worldserver.conf
        if (instantLogout) {
            logoutPlayer(true);

            return;
        }

        // not set flags if player can't free move to prevent lost state at logout cancel
        if (pl.canFreeMove()) {
            if (pl.getStandState() == UnitStandStateType.Stand) {
                pl.setStandState(UnitStandStateType.Sit);
            }

            pl.setRooted(true);
            pl.setUnitFlag(UnitFlag.Stunned);
        }

        setLogoutStartTime(gameTime.GetGameTime());
    }


    private void handleLogoutCancel(LogoutCancel packet) {
        // Player have already logged out serverside, too late to cancel
        if (!getPlayer()) {
            return;
        }

        setLogoutStartTime(0);

        sendPacket(new LogoutCancelAck());

        // not remove flags if can't free move - its not set in Logout request code.
        if (getPlayer().canFreeMove()) {
            //!we can move again
            getPlayer().setRooted(false);

            //! Stand Up
            getPlayer().setStandState(UnitStandStateType.Stand);

            //! DISABLE_ROTATE
            getPlayer().removeUnitFlag(UnitFlag.Stunned);
        }
    }

    public final void doLootRelease(Loot loot) {
        var lguid = loot.getOwnerGUID();
        var player = getPlayer();

        if (Objects.equals(player.getLootGUID(), lguid)) {
            player.setLootGUID(ObjectGuid.Empty);
        }

        //Player is not looking at loot list, he doesn't need to see updates on the loot list
        loot.removeLooter(player.getGUID());
        player.sendLootRelease(lguid);
        player.getAELootView().remove(loot.getGUID());

        if (player.getAELootView().isEmpty()) {
            player.removeUnitFlag(UnitFlag.Looting);
        }

        if (!player.isInWorld()) {
            return;
        }

        if (lguid.isGameObject()) {
            var go = player.getMap().getGameObject(lguid);

            // not check distance for GO in case owned GO (fishing bobber case, for example) or Fishing hole GO
            if (!go || ((ObjectGuid.opNotEquals(go.getOwnerGUID(), player.getGUID()) && go.getGoType() != GameObjectTypes.fishingHole) && !go.isWithinDistInMap(player))) {
                return;
            }

            if (loot.isLooted() || go.getGoType() == GameObjectTypes.fishingNode || go.getGoType() == GameObjectTypes.fishingHole) {
                if (go.getGoType() == GameObjectTypes.fishingNode) {
                    go.setLootState(LootState.JustDeactivated);
                } else if (go.getGoType() == GameObjectTypes.fishingHole) {
                    // The fishing hole used once more
                    go.addUse(); // if the max usage is reached, will be despawned in next tick

                    if (go.getUseCount() >= go.getGoValue().fishingHole.maxOpens) {
                        go.setLootState(LootState.JustDeactivated);
                    } else {
                        go.setLootState(LootState.Ready);
                    }
                } else if (go.getGoType() != GameObjectTypes.gatheringNode && go.isFullyLooted()) {
                    go.setLootState(LootState.JustDeactivated);
                }

                go.onLootRelease(player);
            } else {
                // not fully looted object
                go.setLootState(LootState.Activated, player);
            }
        } else if (lguid.isCorpse()) // ONLY remove insignia at BG
        {
            var corpse = ObjectAccessor.getCorpse(player, lguid);

            if (!corpse || !corpse.isWithinDistInMap(player, SharedConst.InteractionDistance)) {
                return;
            }

            if (loot.isLooted()) {
                corpse.setLoot(null);
                corpse.removeCorpseDynamicFlag(CorpseDynFlags.Lootable);
            }
        } else if (lguid.isItem()) {
            var pItem = player.getItemByGuid(lguid);

            if (!pItem) {
                return;
            }

            var proto = pItem.getTemplate();

            // destroy only 5 items from stack in case prospecting and milling
            if (loot.loot_type == LootType.PROSPECTING || loot.loot_type == LootType.MILLING) {
                pItem.setLootGenerated(false);
                pItem.setLoot(null);

                var count = pItem.getCount();

                // >=5 checked in spell code, but will work for cheating cases also with removing from another stacks.
                if (count > 5) {
                    count = 5;
                }

                tangible.RefObject<Integer> tempRef_count = new tangible.RefObject<Integer>(count);
                player.destroyItemCount(pItem, tempRef_count, true);
                count = tempRef_count.refArgValue;
            } else {
                // Only delete item if no loot or money (unlooted loot is saved to db) or if it isn't an openable item
                if (loot.isLooted() || !proto.hasFlag(ItemFlags.HasLoot)) {
                    player.destroyItem(pItem.getBagSlot(), pItem.getSlot(), true);
                }
            }

            return; // item can be looted only single player
        } else {
            var creature = player.getMap().getCreature(lguid);

            if (creature == null) {
                return;
            }

            if (loot.isLooted()) {
                if (creature.isFullyLooted()) {
                    creature.removeDynamicFlag(UnitDynFlags.Lootable);

                    // skip pickpocketing loot for speed, skinning timer reduction is no-op in fact
                    if (!creature.isAlive()) {
                        creature.allLootRemovedFromCorpse();
                    }
                }
            } else {
                // if the round robin player release, reset it.
                if (Objects.equals(player.getGUID(), loot.roundRobinPlayer)) {
                    loot.roundRobinPlayer.clear();
                    loot.notifyLootList(creature.getMap());
                }
            }

            // force dynflag update to update looter and lootable info
            creature.getValues().modifyValue(creature.getObjectData()).modifyValue(creature.getObjectData().dynamicFlags);
            creature.forceUpdateFieldChange();
        }
    }

    public final void doLootReleaseAll() {
        var lootView = player.getAELootView();


        for (var(_, loot) : lootView) {
            doLootRelease(loot);
        }
    }


    private void handleAutostoreLootItem(LootItemPkt packet) {
        var player = getPlayer();
        var aeResult = player.getAELootView().size() > 1 ? new AELootResult() : null;

        // @todo Implement looting by LootObject guid
        for (var req : packet.loot) {
            var loot = player.getAELootView().get(req.object);

            if (loot == null) {
                player.sendLootRelease(ObjectGuid.Empty);

                continue;
            }

            var lguid = loot.getOwnerGUID();

            if (lguid.IsGameObject) {
                var go = player.getMap().getGameObject(lguid);

                // not check distance for GO in case owned GO (fishing bobber case, for example) or Fishing hole GO
                if (!go || ((ObjectGuid.opNotEquals(go.getOwnerGUID(), player.getGUID()) && go.getGoType() != GameObjectTypes.fishingHole) && !go.isWithinDistInMap(player))) {
                    player.sendLootRelease(lguid);

                    continue;
                }
            } else if (lguid.IsCreatureOrVehicle) {
                var creature = player.getMap().getCreature(lguid);

                if (creature == null) {
                    player.sendLootError(req.object, lguid, LootError.NoLoot);

                    continue;
                }

                if (!creature.isWithinDistInMap(player, AELootCreatureCheck.LOOTDISTANCE)) {
                    player.sendLootError(req.object, lguid, LootError.TooFar);

                    continue;
                }
            }

            player.storeLootItem(lguid, req.lootListID, loot, aeResult);

            // If player is removing the last LootItem, delete the empty container.
            if (loot.isLooted() && lguid.IsItem) {
                player.getSession().doLootRelease(loot);
            }
        }

        if (aeResult != null) {
            for (var resultValue : aeResult.getByOrder()) {
                player.sendNewItem(resultValue.item, resultValue.count, false, false, true, resultValue.dungeonEncounterId);
                player.updateCriteria(CriteriaType.LootItem, resultValue.item.entry, resultValue.count);
                player.updateCriteria(CriteriaType.GetLootByType, resultValue.item.entry, resultValue.count, (long) resultValue.lootType);
                player.updateCriteria(CriteriaType.LootAnyItem, resultValue.item.entry, resultValue.count);
            }
        }

        unit.procSkillsAndAuras(player, null, new ProcFlagsInit(procFlags.Looted), new ProcFlagsInit(), ProcFlagsSpellType.MaskAll, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, null, null, null);
    }


    private void handleLootMoney(LootMoney lootMoney) {
        var player = getPlayer();

        for (var lootView : player.getAELootView().entrySet()) {
            var loot = lootView.getValue();
            var guid = loot.getOwnerGUID();
            var shareMoney = loot.loot_type == LootType.Corpse;

            loot.notifyMoneyRemoved(player.getMap());

            if (shareMoney && player.getGroup() != null) //item, pickpocket and players can be looted only single player
            {
                var group = player.getGroup();

                ArrayList<Player> playersNear = new ArrayList<>();

                for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                    var member = refe.getSource();

                    if (!member) {
                        continue;
                    }

                    if (!loot.hasAllowedLooter(member.getGUID())) {
                        continue;
                    }

                    if (player.isAtGroupRewardDistance(member)) {
                        playersNear.add(member);
                    }
                }

                var goldPerPlayer = (long) (loot.gold / playersNear.size());

                for (var pl : playersNear) {
                    var goldMod = MathUtil.CalculatePct(goldPerPlayer, pl.getTotalAuraModifierByMiscValue(AuraType.ModMoneyGain, 1));

                    pl.modifyMoney((long) (goldPerPlayer + goldMod));
                    pl.updateCriteria(CriteriaType.MoneyLootedFromCreatures, goldPerPlayer);

                    LootMoneyNotify packet = new LootMoneyNotify();
                    packet.money = goldPerPlayer;
                    packet.moneyMod = (long) goldMod;
                    packet.soleLooter = playersNear.size() <= 1 ? true : false;
                    pl.sendPacket(packet);
                }
            } else {
                var goldMod = MathUtil.CalculatePct(loot.gold, player.getTotalAuraModifierByMiscValue(AuraType.ModMoneyGain, 1));

                player.modifyMoney((long) (loot.gold + goldMod));
                player.updateCriteria(CriteriaType.MoneyLootedFromCreatures, loot.gold);

                LootMoneyNotify packet = new LootMoneyNotify();
                packet.money = loot.gold;
                packet.moneyMod = (long) goldMod;
                packet.soleLooter = true; // "You loot..."
                sendPacket(packet);
            }

            loot.gold = 0;

            // Delete the money loot record from the DB
            if (loot.loot_type == LootType.item) {
                global.getLootItemStorage().removeStoredMoneyForContainer(guid.counter);
            }

            // Delete container if empty
            if (loot.isLooted() && guid.IsItem) {
                player.getSession().doLootRelease(loot);
            }
        }
    }


    private void handleLoot(LootUnit packet) {
        // Check possible cheat
        if (!getPlayer().isAlive() || !packet.unit.isCreatureOrVehicle()) {
            return;
        }

        var lootTarget = ObjectAccessor.getCreature(getPlayer(), packet.unit);

        if (!lootTarget) {
            return;
        }

        AELootCreatureCheck check = new AELootCreatureCheck(player, packet.unit);

        if (!check.isValidLootTarget(lootTarget)) {
            return;
        }

        // interrupt cast
        if (getPlayer().isNonMeleeSpellCast(false)) {
            getPlayer().interruptNonMeleeSpells(false);
        }

        getPlayer().removeAurasWithInterruptFlags(SpellAuraInterruptFlags.Looting);

        ArrayList<Creature> corpses = new ArrayList<>();
        CreatureListSearcher searcher = new CreatureListSearcher(player, corpses, check, gridType.Grid);
        Cell.visitGrid(player, searcher, AELootCreatureCheck.LOOTDISTANCE);

        if (!corpses.isEmpty()) {
            sendPacket(new AELootTargets((int) corpses.size() + 1));
        }

        getPlayer().sendLoot(lootTarget.getLootForPlayer(getPlayer()));

        if (!corpses.isEmpty()) {
            // main target
            sendPacket(new AELootTargetsAck());

            for (var creature : corpses) {
                getPlayer().sendLoot(creature.getLootForPlayer(getPlayer()), true);
                sendPacket(new AELootTargetsAck());
            }
        }
    }


    private void handleLootRelease(LootRelease packet) {
        // cheaters can modify lguid to prevent correct apply loot release code and re-loot
        // use internal stored guid
        var loot = getPlayer().getLootByWorldObjectGUID(packet.unit);

        if (loot != null) {
            doLootRelease(loot);
        }
    }


    private void handleLootMasterGive(MasterLootItem masterLootItem) {
        AELootResult aeResult = new AELootResult();

        if (getPlayer().getGroup() == null || ObjectGuid.opNotEquals(getPlayer().getGroup().getLooterGuid(), getPlayer().getGUID())) {
            getPlayer().sendLootError(ObjectGuid.Empty, ObjectGuid.Empty, LootError.DidntKill);

            return;
        }

        // player on other map
        var target = global.getObjAccessor().getPlayer(player, masterLootItem.target);

        if (!target) {
            getPlayer().sendLootError(ObjectGuid.Empty, ObjectGuid.Empty, LootError.PlayerNotFound);

            return;
        }

        for (var req : masterLootItem.loot) {
            var loot = player.getAELootView().get(req.object);

            if (loot == null || loot.getLootMethod() != lootMethod.MasterLoot) {
                return;
            }

            if (!player.isInRaidWith(target) || !player.isInMap(target)) {
                player.sendLootError(req.object, loot.getOwnerGUID(), LootError.MasterOther);
                Log.outInfo(LogFilter.Cheat, String.format("MasterLootItem: Player %1$s tried to give an item to ineligible player %2$s !", getPlayer().getName(), target.getName()));

                return;
            }

            if (!loot.hasAllowedLooter(masterLootItem.target)) {
                player.sendLootError(req.object, loot.getOwnerGUID(), LootError.MasterOther);

                return;
            }

            if (req.lootListID >= loot.items.count) {
                Log.outDebug(LogFilter.loot, String.format("MasterLootItem: Player %1$s might be using a hack! (slot %2$s, size %3$s)", getPlayer().getName(), req.lootListID, loot.items.count));

                return;
            }

            var item = loot.items[req.LootListID];

            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = target.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, item.itemid, item.count);

            if (!item.hasAllowedLooter(target.GUID)) {
                msg = InventoryResult.CantEquipEver;
            }

            if (msg != InventoryResult.Ok) {
                if (msg == InventoryResult.ItemMaxCount) {
                    player.sendLootError(req.object, loot.getOwnerGUID(), LootError.MasterUniqueItem);
                } else if (msg == InventoryResult.InvFull) {
                    player.sendLootError(req.object, loot.getOwnerGUID(), LootError.MasterInvFull);
                } else {
                    player.sendLootError(req.object, loot.getOwnerGUID(), LootError.MasterOther);
                }

                return;
            }

            // now move item from loot to target inventory
            var newitem = target.storeNewItem(dest, item.itemid, true, item.randomBonusListId, item.getAllowedLooters(), item.context, item.bonusListIDs);
            aeResult.add(newitem, item.count, loot.loot_type, loot.getDungeonEncounterId());

            // mark as looted
            item.count = 0;
            item.is_looted = true;

            loot.notifyItemRemoved(req.lootListID, getPlayer().getMap());
            --loot.unlootedCount;
        }

        for (var resultValue : aeResult.getByOrder()) {
            target.sendNewItem(resultValue.item, resultValue.count, false, false, true);
            target.updateCriteria(CriteriaType.LootItem, resultValue.item.entry, resultValue.count);
            target.updateCriteria(CriteriaType.GetLootByType, resultValue.item.entry, resultValue.count, (long) resultValue.lootType);
            target.updateCriteria(CriteriaType.LootAnyItem, resultValue.item.entry, resultValue.count);
        }
    }


    private void handleLootRoll(LootRollPacket packet) {
        var lootRoll = getPlayer().getLootRoll(packet.lootObj, packet.lootListID);

        if (lootRoll == null) {
            return;
        }

        lootRoll.playerVote(getPlayer(), packet.rollType);
    }


    private void handleSetLootSpecialization(SetLootSpecialization packet) {
        if (packet.specID != 0) {
            var chrSpec = CliDB.ChrSpecializationStorage.get(packet.specID);

            if (chrSpec != null) {
                if (chrSpec.classID == (int) getPlayer().getClass().getValue()) {
                    getPlayer().setLootSpecId(packet.specID);
                }
            }
        } else {
            getPlayer().setLootSpecId(0);
        }
    }

    public final void sendShowMailBox(ObjectGuid guid) {
        NPCInteractionOpenResult npcInteraction = new NPCInteractionOpenResult();
        npcInteraction.npc = guid;
        npcInteraction.interactionType = PlayerInteractionType.mailInfo;
        npcInteraction.success = true;
        sendPacket(npcInteraction);
    }

    private boolean canOpenMailBox(ObjectGuid guid) {
        if (Objects.equals(guid, getPlayer().getGUID())) {
            if (!hasPermission(RBACPermissions.CommandMailbox)) {
                Log.outWarn(LogFilter.ChatSystem, "{0} attempt open mailbox in cheating way.", getPlayer().getName());

                return false;
            }
        } else if (guid.isGameObject()) {
            if (!getPlayer().getGameObjectIfCanInteractWith(guid, GameObjectTypes.mailbox)) {
                return false;
            }
        } else if (guid.isAnyTypeCreature()) {
            if (!getPlayer().getNPCIfCanInteractWith(guid, NPCFlags.mailbox, NPCFlags2.NONE)) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }


    private void handleSendMail(SendMail sendMail) {
        if (sendMail.info.attachments.size() > SharedConst.MaxClientMailItems) // client limit
        {
            getPlayer().sendMailResult(0, MailResponseType.Send, MailResponseResult.TooManyAttachments);

            return;
        }

        if (!canOpenMailBox(sendMail.info.mailbox)) {
            return;
        }

        if (StringUtil.isEmpty(sendMail.info.target)) {
            return;
        }

        if (!validateHyperlinksAndMaybeKick(sendMail.info.subject) || !validateHyperlinksAndMaybeKick(sendMail.info.body)) {
            return;
        }

        var player = getPlayer();

        if (player.getLevel() < WorldConfig.getIntValue(WorldCfg.MailLevelReq)) {
            sendNotification(SysMessage.MailSenderReq, WorldConfig.getIntValue(WorldCfg.MailLevelReq));

            return;
        }

        var receiverGuid = ObjectGuid.Empty;

        tangible.RefObject<String> tempRef_Target = new tangible.RefObject<String>(sendMail.info.target);
        if (ObjectManager.normalizePlayerName(tempRef_Target)) {
            sendMail.info.target = tempRef_Target.refArgValue;
            receiverGuid = global.getCharacterCacheStorage().getCharacterGuidByName(sendMail.info.target);
        } else {
            sendMail.info.target = tempRef_Target.refArgValue;
        }

        if (receiverGuid.isEmpty()) {
            Log.outInfo(LogFilter.Network, "Player {0} is sending mail to {1} (GUID: not existed!) with subject {2}" + "and body {3} includes {4} items, {5} copper and {6} COD copper with stationeryID = {7}", getPlayerInfo(), sendMail.info.target, sendMail.info.subject, sendMail.info.body, sendMail.info.attachments.size(), sendMail.info.sendMoney, sendMail.info.cod, sendMail.info.stationeryID);

            player.sendMailResult(0, MailResponseType.Send, MailResponseResult.RecipientNotFound);

            return;
        }

        if (sendMail.info.sendMoney < 0) {
            getPlayer().sendMailResult(0, MailResponseType.Send, MailResponseResult.InternalError);

            Log.outWarn(LogFilter.Server, "Player {0} attempted to send mail to {1} ({2}) with negative money value (SendMoney: {3})", getPlayerInfo(), sendMail.info.target, receiverGuid.toString(), sendMail.info.sendMoney);

            return;
        }

        if (sendMail.info.cod < 0) {
            getPlayer().sendMailResult(0, MailResponseType.Send, MailResponseResult.InternalError);

            Log.outWarn(LogFilter.Server, "Player {0} attempted to send mail to {1} ({2}) with negative COD value (Cod: {3})", getPlayerInfo(), sendMail.info.target, receiverGuid.toString(), sendMail.info.cod);

            return;
        }

        Log.outInfo(LogFilter.Network, "Player {0} is sending mail to {1} ({2}) with subject {3} and body {4}" + "includes {5} items, {6} copper and {7} COD copper with stationeryID = {8}", getPlayerInfo(), sendMail.info.target, receiverGuid.toString(), sendMail.info.subject, sendMail.info.body, sendMail.info.attachments.size(), sendMail.info.sendMoney, sendMail.info.cod, sendMail.info.stationeryID);

        if (Objects.equals(player.getGUID(), receiverGuid)) {
            player.sendMailResult(0, MailResponseType.Send, MailResponseResult.CannotSendToSelf);

            return;
        }

        var cost = (int) (!sendMail.info.attachments.isEmpty() ? 30 * sendMail.info.attachments.size() : 30); // price hardcoded in client

        var reqmoney = cost + sendMail.info.sendMoney;

        // Check for overflow
        if (reqmoney < sendMail.info.sendMoney) {
            player.sendMailResult(0, MailResponseType.Send, MailResponseResult.NotEnoughMoney);

            return;
        }

        if (!player.hasEnoughMoney(reqmoney) && !player.isGameMaster()) {
            player.sendMailResult(0, MailResponseType.Send, MailResponseResult.NotEnoughMoney);

            return;
        }


//		void mailCountCheckContinuation(Team receiverTeam, ulong mailsCount, uint receiverLevel, uint receiverAccountId, uint receiverBnetAccountId)
//			{
//				if (player != player)
//					return;
//
//				// do not allow to have more than 100 mails in mailbox.. mails count is in opcode uint8!!! - so max can be 255..
//				if (mailsCount > 100)
//				{
//					player.sendMailResult(0, MailResponseType.Send, MailResponseResult.RecipientCapReached);
//
//					return;
//				}
//
//				// test the receiver's faction... or all items are account bound
//				var accountBound = !sendMail.info.attachments.isEmpty();
//
//				foreach (var att in sendMail.info.attachments)
//				{
//					var item = player.getItemByGuid(att.itemGUID);
//
//					if (item != null)
//					{
//						var itemProto = item.template;
//
//						if (itemProto == null || !itemProto.hasFlag(ItemFlags.IsBoundToAccount))
//						{
//							accountBound = false;
//
//							break;
//						}
//					}
//				}
//
//				if (!accountBound && player.team != receiverTeam && !hasPermission(RBACPermissions.TwoSideInteractionMail))
//				{
//					player.sendMailResult(0, MailResponseType.Send, MailResponseResult.NotYourTeam);
//
//					return;
//				}
//
//				if (receiverLevel < WorldConfig.getIntValue(WorldCfg.MailLevelReq))
//				{
//					sendNotification(SysMessage.MailReceiverReq, WorldConfig.getIntValue(WorldCfg.MailLevelReq));
//
//					return;
//				}
//
//				list<item> items = new();
//
//				foreach (var att in sendMail.info.attachments)
//				{
//					if (att.itemGUID.IsEmpty)
//					{
//						player.sendMailResult(0, MailResponseType.Send, MailResponseResult.MailAttachmentInvalid);
//
//						return;
//					}
//
//					var item = player.getItemByGuid(att.itemGUID);
//
//					// prevent sending bag with items (cheat: can be placed in bag after adding equipped empty bag to mail)
//					if (item == null)
//					{
//						player.sendMailResult(0, MailResponseType.Send, MailResponseResult.MailAttachmentInvalid);
//
//						return;
//					}
//
//					if (!item.canBeTraded(true))
//					{
//						player.sendMailResult(0, MailResponseType.Send, MailResponseResult.EquipError, InventoryResult.MailBoundItem);
//
//						return;
//					}
//
//					if (item.IsBoundAccountWide && item.IsSoulBound && player.session.accountId != receiverAccountId)
//						if (!item.IsBattlenetAccountBound || player.session.BattlenetAccountId == 0 || player.session.BattlenetAccountId != receiverBnetAccountId)
//						{
//							player.sendMailResult(0, MailResponseType.Send, MailResponseResult.EquipError, InventoryResult.NotSameAccount);
//
//							return;
//						}
//
//					if (item.template.hasFlag(ItemFlags.Conjured) || item.itemData.expiration != 0)
//					{
//						player.sendMailResult(0, MailResponseType.Send, MailResponseResult.EquipError, InventoryResult.MailBoundItem);
//
//						return;
//					}
//
//					if (sendMail.info.cod != 0 && item.IsWrapped)
//					{
//						player.sendMailResult(0, MailResponseType.Send, MailResponseResult.CantSendWrappedCod);
//
//						return;
//					}
//
//					if (item.IsNotEmptyBag)
//					{
//						player.sendMailResult(0, MailResponseType.Send, MailResponseResult.EquipError, InventoryResult.DestroyNonemptyBag);
//
//						return;
//					}
//
//					items.add(item);
//				}
//
//				player.sendMailResult(0, MailResponseType.Send, MailResponseResult.Ok);
//
//				player.modifyMoney(-reqmoney);
//				player.updateCriteria(CriteriaType.MoneySpentOnPostage, cost);
//
//				var needItemDelay = false;
//
//				MailDraft draft = new(sendMail.info.subject, sendMail.info.body);
//
//				var trans = new SQLTransaction();
//
//				if (!sendMail.info.attachments.isEmpty() || sendMail.info.sendMoney > 0)
//				{
//					var log = hasPermission(RBACPermissions.LogGmTrade);
//
//					if (!sendMail.info.attachments.isEmpty())
//					{
//						foreach (var item in items)
//						{
//							if (log)
//								Log.outCommand(accountId, string.format("GM {0} ({1}) (Account: {2}) mail item: {3} ", PlayerName, player.GUID, accountId, item.template.getName()) + string.format("(Entry: {0} Count: {1}) to: {2} ({3}) (Account: {4})", item.entry, item.count, sendMail.info.target, receiverGuid, receiverAccountId));
//
//							item.setNotRefundable(player); // makes the item no longer refundable
//							player.moveItemFromInventory(item.BagSlot, item.slot, true);
//
//							item.deleteFromInventoryDB(trans); // deletes item from character's inventory
//							item.setOwnerGUID(receiverGuid);
//							item.setState(ItemUpdateState.changed);
//							item.saveToDB(trans); // recursive and not have transaction guard into self, item not in inventory and can be save standalone
//
//							draft.addItem(item);
//						}
//
//						// if item send to character at another account, then apply item delivery delay
//						needItemDelay = player.session.accountId != receiverAccountId;
//					}
//
//					if (log && sendMail.info.sendMoney > 0)
//						Log.outCommand(accountId, string.format("GM {0} ({1}) (Account: {2}) mail money: {3} to: {4} ({5}) (Account: {6})", PlayerName, player.GUID, accountId, sendMail.info.sendMoney, sendMail.info.target, receiverGuid, receiverAccountId));
//				}
//
//				// If theres is an item, there is a one hour delivery delay if sent to another account's character.
//				var deliver_delay = needItemDelay ? WorldConfig.getUIntValue(WorldCfg.MailDeliveryDelay) : 0;
//
//				// Mail sent between guild members arrives instantly
//				var guild = global.GuildMgr.getGuildById(player.guildId);
//
//				if (guild != null)
//					if (guild.isMember(receiverGuid))
//						deliver_delay = 0;
//
//				// don't ask for COD if there are no items
//				if (sendMail.info.attachments.isEmpty())
//					sendMail.info.cod = 0;
//
//				// will delete item or place to receiver mail list
//				draft.addMoney((ulong)sendMail.info.sendMoney).addCOD((uint)sendMail.info.cod).sendMailTo(trans, new MailReceiver(global.ObjAccessor.findConnectedPlayer(receiverGuid), receiverGuid.counter), new MailSender(player), sendMail.info.body.isEmpty() ? MailCheckMask.Copied : MailCheckMask.HasBody, deliver_delay);
//
//				player.saveInventoryAndGoldToDB(trans);
//				DB.character.CommitTransaction(trans);
//			}

        var receiver = global.getObjAccessor().findPlayer(receiverGuid);

        if (receiver != null) {
            mailCountCheckContinuation(receiver.getTeam(), receiver.getMailSize(), receiver.getLevel(), receiver.getSession().getAccountId(), receiver.getSession().getBattlenetAccountId());
        } else {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAIL_COUNT);
            stmt.AddValue(0, receiverGuid.getCounter());

            getQueryProcessor().AddCallback(DB.characters.AsyncQuery(stmt).WithChainingCallback((queryCallback, mailCountResult) ->
            {
                var characterInfo = global.getCharacterCacheStorage().getCharacterCacheByGuid(receiverGuid);

                if (characterInfo != null) {
                    queryCallback.WithCallback(bnetAccountResult ->
                    {
                        mailCountCheckContinuation(player.teamForRace(characterInfo.raceId), !mailCountResult.isEmpty() ? mailCountResult.<Long>Read(0) : 0, characterInfo.level, characterInfo.accountId, !bnetAccountResult.isEmpty() ? bnetAccountResult.<Integer>Read(0) : 0);
                    }).SetNextQuery(global.getBNetAccountMgr().getIdByGameAccountAsync(characterInfo.accountId));
                }
            }));
        }
    }

    //called when mail is read

    private void handleMailMarkAsRead(MailMarkAsRead markAsRead) {
        if (!canOpenMailBox(markAsRead.mailbox)) {
            return;
        }

        var player = getPlayer();
        var m = player.getMail(markAsRead.mailID);

        if (m != null && m.state != MailState.Deleted) {
            if (player.getUnReadMails() != 0) {
                player.setUnReadMails(player.getUnReadMails() - 1);
            }

            m.checkMask = MailCheckMask.forValue(m.checkMask.getValue() | MailCheckMask.Read.getValue());
            player.setMailsUpdated(true);
            m.state = MailState.changed;
        }
    }

    //called when client deletes mail

    private void handleMailDelete(MailDelete mailDelete) {
        var m = getPlayer().getMail(mailDelete.mailID);
        var player = getPlayer();
        player.setMailsUpdated(true);

        if (m != null) {
            // delete shouldn't show up for COD mails
            if (m.COD != 0) {
                player.sendMailResult(mailDelete.mailID, MailResponseType.Deleted, MailResponseResult.InternalError);

                return;
            }

            m.state = MailState.Deleted;
        }

        player.sendMailResult(mailDelete.mailID, MailResponseType.Deleted, MailResponseResult.Ok);
    }


    private void handleMailReturnToSender(MailReturnToSender returnToSender) {
        if (!canOpenMailBox(player.getPlayerTalkClass().getInteractionData().getSourceGuid())) {
            return;
        }

        var player = getPlayer();
        var m = player.getMail(returnToSender.mailID);

        if (m == null || m.state == MailState.Deleted || m.deliver_time > gameTime.GetGameTime() || m.sender != returnToSender.senderGUID.getCounter()) {
            player.sendMailResult(returnToSender.mailID, MailResponseType.ReturnedToSender, MailResponseResult.InternalError);

            return;
        }

        //we can return mail now, so firstly delete the old one
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_MAIL_BY_ID);
        stmt.AddValue(0, returnToSender.mailID);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_MAIL_ITEM_BY_ID);
        stmt.AddValue(0, returnToSender.mailID);
        trans.append(stmt);

        player.removeMail(returnToSender.mailID);

        // only return mail if the player exists (and delete if not existing)
        if (m.messageType == MailMessageType.NORMAL && m.sender != 0) {
            MailDraft draft = new MailDraft(m.subject, m.body);

            if (m.mailTemplateId != 0) {
                draft = new MailDraft(m.mailTemplateId, false); // items already included
            }

            if (m.hasItems()) {
                for (var itemInfo : m.items) {
                    var item = player.getMItem(itemInfo.item_guid);

                    if (item) {
                        draft.addItem(item);
                    }

                    player.removeMItem(itemInfo.item_guid);
                }
            }

            draft.addMoney(m.money).sendReturnToSender(getAccountId(), m.receiver, m.sender, trans);
        }

        DB.characters.CommitTransaction(trans);

        player.sendMailResult(returnToSender.mailID, MailResponseType.ReturnedToSender, MailResponseResult.Ok);
    }

    //called when player takes item attached in mail

    private void handleMailTakeItem(MailTakeItem takeItem) {
        var attachID = takeItem.attachID;

        if (!canOpenMailBox(takeItem.mailbox)) {
            return;
        }

        var player = getPlayer();

        var m = player.getMail(takeItem.mailID);

        if (m == null || m.state == MailState.Deleted || m.deliver_time > gameTime.GetGameTime()) {
            player.sendMailResult(takeItem.mailID, MailResponseType.ItemTaken, MailResponseResult.InternalError);

            return;
        }

        // verify that the mail has the item to avoid cheaters taking COD items without paying
        if (!m.items.Any(p -> p.item_guid == attachID)) {
            player.sendMailResult(takeItem.mailID, MailResponseType.ItemTaken, MailResponseResult.InternalError);

            return;
        }

        // prevent cheating with skip client money check
        if (!player.hasEnoughMoney(m.COD)) {
            player.sendMailResult(takeItem.mailID, MailResponseType.ItemTaken, MailResponseResult.NotEnoughMoney);

            return;
        }

        var it = player.getMItem(takeItem.attachID);

        ArrayList<ItemPosCount> dest = new ArrayList<>();
        var msg = getPlayer().canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, dest, it, false);

        if (msg == InventoryResult.Ok) {
            SQLTransaction trans = new SQLTransaction();
            m.removeItem(takeItem.attachID);
            m.removedItems.add(takeItem.attachID);

            if (m.COD > 0) //if there is COD, take COD money from player and send them to sender by mail
            {
                var sender_guid = ObjectGuid.create(HighGuid.Player, m.sender);
                var receiver = global.getObjAccessor().findPlayer(sender_guid);

                int sender_accId = 0;

                if (hasPermission(RBACPermissions.LogGmTrade)) {
                    String sender_name;

                    if (receiver) {
                        sender_accId = receiver.getSession().getAccountId();
                        sender_name = receiver.getName();
                    } else {
                        // can be calculated early
                        sender_accId = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(sender_guid);

                        tangible.OutObject<String> tempOut_sender_name = new tangible.OutObject<String>();
                        if (!global.getCharacterCacheStorage().getCharacterNameByGuid(sender_guid, tempOut_sender_name)) {
                            sender_name = tempOut_sender_name.outArgValue;
                            sender_name = global.getObjectMgr().getSysMessage(SysMessage.unknown);
                        } else {
                            sender_name = tempOut_sender_name.outArgValue;
                        }
                    }

                    Log.outCommand(getAccountId(), "GM {0} (Account: {1}) receiver mail item: {2} (Entry: {3} Count: {4}) and send COD money: {5} to player: {6} (Account: {7})", getPlayerName(), getAccountId(), it.getTemplate().getName(), it.getEntry(), it.getCount(), m.COD, sender_name, sender_accId);
                } else if (!receiver) {
                    sender_accId = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(sender_guid);
                }

                // check player existence
                if (receiver || sender_accId != 0) {
                    (new MailDraft(m.subject, "")).addMoney(m.COD).sendMailTo(trans, new MailReceiver(receiver, m.sender), new MailSender(MailMessageType.NORMAL, m.receiver), MailCheckMask.CodPayment);
                }

                player.modifyMoney(-(long) m.COD);
            }

            m.COD = 0;
            m.state = MailState.changed;
            player.setMailsUpdated(true);
            player.removeMItem(it.getGUID().getCounter());

            var count = it.getCount(); // save counts before store and possible merge with deleting
            it.setState(ItemUpdateState.Unchanged); // need to set this state, otherwise item cannot be removed later, if neccessary
            player.moveItemToInventory(dest, it, true);

            player.saveInventoryAndGoldToDB(trans);
            player._SaveMail(trans);
            DB.characters.CommitTransaction(trans);

            player.sendMailResult(takeItem.mailID, MailResponseType.ItemTaken, MailResponseResult.Ok, 0, takeItem.attachID, count);
        } else {
            player.sendMailResult(takeItem.mailID, MailResponseType.ItemTaken, MailResponseResult.EquipError, msg);
        }
    }


    private void handleMailTakeMoney(MailTakeMoney takeMoney) {
        if (!canOpenMailBox(takeMoney.mailbox)) {
            return;
        }

        var player = getPlayer();

        var m = player.getMail(takeMoney.mailID);

        if ((m == null || m.state == MailState.Deleted || m.deliver_time > gameTime.GetGameTime()) || (takeMoney.money > 0 && m.money != (long) takeMoney.money)) {
            player.sendMailResult(takeMoney.mailID, MailResponseType.MoneyTaken, MailResponseResult.InternalError);

            return;
        }

        if (!player.modifyMoney((long) m.money, false)) {
            player.sendMailResult(takeMoney.mailID, MailResponseType.MoneyTaken, MailResponseResult.EquipError, InventoryResult.TooMuchGold);

            return;
        }

        m.money = 0;
        m.state = MailState.changed;
        player.setMailsUpdated(true);

        player.sendMailResult(takeMoney.mailID, MailResponseType.MoneyTaken, MailResponseResult.Ok);

        // save money and mail to prevent cheating
        SQLTransaction trans = new SQLTransaction();
        player.saveGoldToDB(trans);
        player._SaveMail(trans);
        DB.characters.CommitTransaction(trans);
    }

    //called when player lists his received mails

    private void handleGetMailList(MailGetList getList) {
        if (!canOpenMailBox(getList.mailbox)) {
            return;
        }

        var player = getPlayer();

        var mails = player.getMails();

        MailListResult response = new MailListResult();
        var curTime = gameTime.GetGameTime();

        for (var m : mails) {
            // skip deleted or not delivered (deliver delay not expired) mails
            if (m.state == MailState.Deleted || curTime < m.deliver_time) {
                continue;
            }

            // max. 100 mails can be sent
            if (response.mails.size() < 100) {
                response.mails.add(new MailListEntry(m, player));
            }
        }

        player.getPlayerTalkClass().getInteractionData().reset();
        player.getPlayerTalkClass().getInteractionData().setSourceGuid(getList.mailbox);
        sendPacket(response);

        // recalculate m_nextMailDelivereTime and unReadMails
        getPlayer().updateNextMailTimeAndUnreads();
    }

    //used when player copies mail body to his inventory

    private void handleMailCreateTextItem(MailCreateTextItem createTextItem) {
        if (!canOpenMailBox(createTextItem.mailbox)) {
            return;
        }

        var player = getPlayer();

        var m = player.getMail(createTextItem.mailID);

        if (m == null || (StringUtil.isEmpty(m.body) && m.mailTemplateId == 0) || m.state == MailState.Deleted || m.deliver_time > gameTime.GetGameTime()) {
            player.sendMailResult(createTextItem.mailID, MailResponseType.MadePermanent, MailResponseResult.InternalError);

            return;
        }

        Item bodyItem = new item(); // This is not bag and then can be used new item.

        if (!bodyItem.create(global.getObjectMgr().getGenerator(HighGuid.Item).generate(), 8383, itemContext.NONE, player)) {
            return;
        }

        // in mail template case we need create new item text
        if (m.mailTemplateId != 0) {
            var mailTemplateEntry = CliDB.MailTemplateStorage.get(m.mailTemplateId);

            if (mailTemplateEntry == null) {
                player.sendMailResult(createTextItem.mailID, MailResponseType.MadePermanent, MailResponseResult.InternalError);

                return;
            }

            bodyItem.setText(mailTemplateEntry.Body[getSessionDbcLocale()]);
        } else {
            bodyItem.setText(m.body);
        }

        if (m.messageType == MailMessageType.NORMAL) {
            bodyItem.setCreator(ObjectGuid.create(HighGuid.Player, m.sender));
        }

        bodyItem.setItemFlag(ItemFieldFlags.Readable);

        Log.outInfo(LogFilter.Network, "HandleMailCreateTextItem mailid={0}", createTextItem.mailID);

        ArrayList<ItemPosCount> dest = new ArrayList<>();
        var msg = getPlayer().canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, dest, bodyItem, false);

        if (msg == InventoryResult.Ok) {
            m.checkMask = MailCheckMask.forValue(m.checkMask.getValue() | MailCheckMask.Copied.getValue());
            m.state = MailState.changed;
            player.setMailsUpdated(true);

            player.storeItem(dest, bodyItem, true);
            player.sendMailResult(createTextItem.mailID, MailResponseType.MadePermanent, MailResponseResult.Ok);
        } else {
            player.sendMailResult(createTextItem.mailID, MailResponseType.MadePermanent, MailResponseResult.EquipError, msg);
        }
    }


    private void handleQueryNextMailTime(MailQueryNextMailTime queryNextMailTime) {
        MailQueryNextTimeResult result = new MailQueryNextTimeResult();

        if (getPlayer().getUnReadMails() > 0) {
            result.nextMailTime = 0.0f;

            var now = gameTime.GetGameTime();
            ArrayList<Long> sentSenders = new ArrayList<>();

            for (var mail : getPlayer().getMails()) {
                if (mail.checkMask.hasFlag(MailCheckMask.Read)) {
                    continue;
                }

                // and already delivered
                if (now < mail.deliver_time) {
                    continue;
                }

                // only send each mail sender once
                if (sentSenders.Any(p -> p == mail.sender)) {
                    continue;
                }

                result.next.add(new MailQueryNextTimeResult.MailNextTimeEntry(mail));

                sentSenders.add(mail.sender);

                // do not send more than 2 mails
                if (sentSenders.size() > 2) {
                    break;
                }
            }
        } else {
            result.nextMailTime = -Time.Day;
        }

        sendPacket(result);
    }

    public final void sendLoadCUFProfiles() {
        var player = getPlayer();

        LoadCUFProfiles loadCUFProfiles = new LoadCUFProfiles();

        for (byte i = 0; i < PlayerConst.MaxCUFProfiles; ++i) {
            var cufProfile = player.getCUFProfile(i);

            if (cufProfile != null) {
                loadCUFProfiles.CUFProfiles.add(cufProfile);
            }
        }

        sendPacket(loadCUFProfiles);
    }


    private void handleRequestAccountData(RequestAccountData request) {
        if (request.dataType.getValue() > AccountDataTypes.max.getValue()) {
            return;
        }

        var adata = getAccountData(request.dataType);

        UpdateAccountData data = new UpdateAccountData();
        data.player = getPlayer() ? getPlayer().getGUID() : ObjectGuid.Empty;
        data.time = (int) adata.time;
        data.dataType = request.dataType;

        if (!adata.data.isEmpty()) {
            data.size = (int) adata.data.length();
            data.compressedData = new byteBuffer(ZLib.Compress(adata.data.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }

        sendPacket(data);
    }


    private void handleUpdateAccountData(UserClientUpdateAccountData packet) {
        if (packet.dataType.getValue() >= AccountDataTypes.max.getValue()) {
            return;
        }

        if (packet.size == 0) {
            setAccountData(packet.dataType, 0, "");

            return;
        }

        if (packet.size > 0xFFFF) {
            Log.outError(LogFilter.Network, "UpdateAccountData: Account data packet too big, size {0}", packet.size);

            return;
        }

        var data = ZLib.Decompress(packet.compressedData.getData(), packet.size);
        setAccountData(packet.dataType, packet.time, Encoding.Default.getString(data));
    }


    private void handleSetSelection(SetSelection packet) {
        getPlayer().setSelection(packet.selection);
    }


    private void handleObjectUpdateFailed(ObjectUpdateFailed objectUpdateFailed) {
        Log.outError(LogFilter.Network, "Object update failed for {0} for player {1} ({2})", objectUpdateFailed.objectGUID.toString(), getPlayerName(), getPlayer().getGUID().toString());

        // If create object failed for current player then client will be stuck on loading screen
        if (Objects.equals(getPlayer().getGUID(), objectUpdateFailed.objectGUID)) {
            logoutPlayer(true);

            return;
        }

        // Pretend we've never seen this object
        getPlayer().getClientGuiDs().remove(objectUpdateFailed.objectGUID);
    }


    private void handleObjectUpdateRescued(ObjectUpdateRescued objectUpdateRescued) {
        Log.outError(LogFilter.Network, "Object update rescued for {0} for player {1} ({2})", objectUpdateRescued.objectGUID.toString(), getPlayerName(), getPlayer().getGUID().toString());

        // Client received values update after destroying object
        // re-register object in m_clientGUIDs to send DestroyObject on next visibility update
        synchronized (getPlayer().getClientGuiDs()) {
            getPlayer().getClientGuiDs().add(objectUpdateRescued.objectGUID);
        }
    }


    private void handleSetActionButton(SetActionButton packet) {
        long action = packet.getButtonAction();
        var type = packet.getButtonType();

        if (packet.action == 0) {
            getPlayer().removeActionButton(packet.index);
        } else {
            getPlayer().addActionButton(packet.index, action, type);
        }
    }


    private void handleSetActionBarToggles(SetActionBarToggles packet) {
        if (!getPlayer()) // ignore until not logged (check needed because STATUS_AUTHED)
        {
            if (packet.mask != 0) {
                Log.outError(LogFilter.Network, "WorldSession.HandleSetActionBarToggles in not logged state with value: {0}, ignored", packet.mask);
            }

            return;
        }

        getPlayer().setMultiActionBars(packet.mask);
    }


    private void handleCompleteCinematic(CompleteCinematic packet) {
        // If player has sight bound to visual waypoint NPC we should remove it
        getPlayer().getCinematicMgr().endCinematic();
    }


    private void handleNextCinematicCamera(NextCinematicCamera packet) {
        // Sent by client when cinematic actually begun. So we begin the server side process
        getPlayer().getCinematicMgr().nextCinematicCamera();
    }


    private void handleCompleteMovie(CompleteMovie packet) {
        var movie = player.getMovie();

        if (movie == 0) {
            return;
        }

        player.setMovie(0);
        global.getScriptMgr().<IPlayerOnMovieComplete>ForEach(p -> p.OnMovieComplete(player, movie));
    }


    private void handleViolenceLevel(ViolenceLevel violenceLevel) {
        // do something?
    }


    private void handleAreaTrigger(AreaTriggerPkt packet) {
        var player = getPlayer();

        if (player.isInFlight()) {
            Log.outDebug(LogFilter.Network, "HandleAreaTrigger: Player '{0}' (GUID: {1}) in flight, ignore Area Trigger ID:{2}", player.getName(), player.getGUID().toString(), packet.areaTriggerID);

            return;
        }

        var atEntry = CliDB.AreaTriggerStorage.get(packet.areaTriggerID);

        if (atEntry == null) {
            Log.outDebug(LogFilter.Network, "HandleAreaTrigger: Player '{0}' (GUID: {1}) send unknown (by DBC) Area Trigger ID:{2}", player.getName(), player.getGUID().toString(), packet.areaTriggerID);

            return;
        }

        if (packet.entered && !player.isInAreaTriggerRadius(atEntry)) {
            Log.outDebug(LogFilter.Network, "HandleAreaTrigger: Player '{0}' ({1}) too far, ignore Area Trigger ID: {2}", player.getName(), player.getGUID().toString(), packet.areaTriggerID);

            return;
        }

        if (player.isDebugAreaTriggers()) {
            player.sendSysMessage(packet.Entered ? SysMessage.DebugAreatriggerEntered : SysMessage.DebugAreatriggerLeft, packet.areaTriggerID);
        }

        if (!global.getConditionMgr().isObjectMeetingNotGroupedConditions(ConditionSourceType.AreatriggerClientTriggered, atEntry.id, player)) {
            return;
        }

        if (global.getScriptMgr().onAreaTrigger(player, atEntry, packet.entered)) {
            return;
        }

        if (player.isAlive()) {
            // not using player.UpdateQuestObjectiveProgress, ObjectID in quest_objectives can be set to -1, areatrigger_involvedrelation then holds correct id
            var quests = global.getObjectMgr().getQuestsForAreaTrigger(packet.areaTriggerID);

            if (quests != null) {
                var anyObjectiveChangedCompletionState = false;

                for (var questId : quests) {
                    var qInfo = global.getObjectMgr().getQuestTemplate(questId);
                    var slot = player.findQuestSlot(questId);

                    if (qInfo != null && slot < SharedConst.MaxQuestLogSize && player.getQuestStatus(questId) == QuestStatus.INCOMPLETE) {
                        for (var obj : qInfo.objectives) {
                            if (obj.type != QuestObjectiveType.areaTrigger) {
                                continue;
                            }

                            if (!player.isQuestObjectiveCompletable(slot, qInfo, obj)) {
                                continue;
                            }

                            if (player.isQuestObjectiveComplete(slot, qInfo, obj)) {
                                continue;
                            }

                            if (obj.objectID != -1 && obj.objectID != packet.areaTriggerID) {
                                continue;
                            }

                            player.setQuestObjectiveData(obj, 1);
                            player.sendQuestUpdateAddCreditSimple(obj);
                            anyObjectiveChangedCompletionState = true;

                            break;
                        }

                        player.areaExploredOrEventHappens(questId);

                        if (player.canCompleteQuest(questId)) {
                            player.completeQuest(questId);
                        }
                    }
                }

                if (anyObjectiveChangedCompletionState) {
                    player.updateVisibleGameobjectsOrSpellClicks();
                }
            }
        }

        if (global.getObjectMgr().isTavernAreaTrigger(packet.areaTriggerID)) {
            // set resting flag we are in the inn
            player.getRestMgr().setRestFlag(RestFlag.Tavern, atEntry.id);

            if (global.getWorldMgr().isFFAPvPRealm()) {
                player.removePvpFlag(UnitPVPStateFlags.FFAPvp);
            }

            return;
        }

        var bg = player.getBattleground();

        if (bg) {
            bg.handleAreaTrigger(player, packet.areaTriggerID, packet.entered);
        }

        var pvp = player.getOutdoorPvP();

        if (pvp != null) {
            if (pvp.handleAreaTrigger(player, packet.areaTriggerID, packet.entered)) {
                return;
            }
        }

        var at = global.getObjectMgr().getAreaTrigger(packet.areaTriggerID);

        if (at == null) {
            return;
        }

        var teleported = false;

        if (player.getLocation().getMapId() != at.target_mapId) {
            if (!player.isAlive()) {
                if (player.getHasCorpse()) {
                    // let enter in ghost mode in instance that connected to inner instance with corpse
                    var corpseMap = player.getCorpseLocation().getMapId();

                    do {
                        if (corpseMap == at.target_mapId) {
                            break;
                        }

                        var corpseInstance = global.getObjectMgr().getInstanceTemplate(corpseMap);
                        corpseMap = corpseInstance != null ? corpseInstance.Parent : 0;
                    } while (corpseMap != 0);

                    if (corpseMap == 0) {
                        sendPacket(new AreaTriggerNoCorpse());

                        return;
                    }

                    Log.outDebug(LogFilter.Maps, String.format("MAP: Player '%1$s' has corpse in instance %2$s and can enter.", player.getName(), at.target_mapId));
                } else {
                    Log.outDebug(LogFilter.Maps, String.format("Map::CanPlayerEnter - player '%1$s' is dead but does not have a corpse!", player.getName()));
                }
            }

            var denyReason = Map.playerCannotEnter(at.target_mapId, player);

            if (denyReason != null) {
                switch (denyReason.getReason()) {
                    case MapNotAllowed:
                        Log.outDebug(LogFilter.Maps, String.format("MAP: Player '%1$s' attempted to enter map with id %2$s which has no entry", player.getName(), at.target_mapId));

                        break;
                    case Difficulty:
                        Log.outDebug(LogFilter.Maps, String.format("MAP: Player '%1$s' attempted to enter instance map %2$s but the requested difficulty was not found", player.getName(), at.target_mapId));

                        break;
                    case NeedGroup:
                        Log.outDebug(LogFilter.Maps, String.format("MAP: Player '%1$s' must be in a raid group to enter map %2$s", player.getName(), at.target_mapId));
                        player.sendRaidGroupOnlyMessage(RaidGroupReason.Only, 0);

                        break;
                    case LockedToDifferentInstance:
                        Log.outDebug(LogFilter.Maps, String.format("MAP: Player '%1$s' cannot enter instance map %2$s because their permanent bind is incompatible with their group's", player.getName(), at.target_mapId));

                        break;
                    case AlreadyCompletedEncounter:
                        Log.outDebug(LogFilter.Maps, String.format("MAP: Player '%1$s' cannot enter instance map %2$s because their permanent bind is incompatible with their group's", player.getName(), at.target_mapId));

                        break;
                    case TooManyInstances:
                        Log.outDebug(LogFilter.Maps, "MAP: Player '{0}' cannot enter instance map {1} because he has exceeded the maximum number of instances per hour.", player.getName(), at.target_mapId);

                        break;
                    case MaxPlayers:
                    case ZoneInCombat:
                        break;
                    case NotFound:
                        Log.outDebug(LogFilter.Maps, String.format("MAP: Player '%1$s' cannot enter instance map %2$s because instance is resetting.", player.getName(), at.target_mapId));

                        break;
                    default:
                        break;
                }

                if (denyReason.getReason() != TransferAbortReason.NeedGroup) {
                    player.sendTransferAborted(at.target_mapId, denyReason.getReason(), denyReason.getArg(), denyReason.getMapDifficultyXConditionId());
                }

                if (!player.isAlive() && player.getHasCorpse()) {
                    if (player.getCorpseLocation().getMapId() == at.target_mapId) {
                        player.resurrectPlayer(0.5f);
                        player.spawnCorpseBones();
                    }
                }

                return;
            }

            var group = player.getGroup();

            if (group) {
                if (group.isLFGGroup() && player.getMap().isDungeon()) {
                    teleported = player.teleportToBGEntryPoint();
                }
            }
        }

        if (!teleported) {
            WorldSafeLocsEntry entranceLocation = null;
            var mapEntry = CliDB.MapStorage.get(at.target_mapId);

            if (mapEntry.Instanceable()) {
                // Check if we can contact the instancescript of the instance for an updated entrance location
                var targetInstanceId = global.getMapMgr().FindInstanceIdForPlayer(at.target_mapId, player);

                if (targetInstanceId != 0) {
                    var map = global.getMapMgr().findMap(at.target_mapId, targetInstanceId);

                    if (map != null) {
                        var instanceMap = map.getToInstanceMap();

                        if (instanceMap) {
                            var instanceScript = instanceMap.getInstanceScript();

                            if (instanceScript != null) {
                                entranceLocation = global.getObjectMgr().getWorldSafeLoc(instanceScript.getEntranceLocation());
                            }
                        }
                    }
                }

                // Finally check with the instancesave for an entrance location if we did not get a valid one from the instancescript
                if (entranceLocation == null) {
                    var group = player.getGroup();
                    var difficulty = group ? group.getDifficultyID(mapEntry) : player.getDifficultyId(mapEntry);
                    var instanceOwnerGuid = group ? group.getRecentInstanceOwner(at.target_mapId) : player.getGUID();
                    tangible.RefObject<Difficulty> tempRef_difficulty = new tangible.RefObject<Difficulty>(difficulty);
                    var instanceLock = global.getInstanceLockMgr().findActiveInstanceLock(instanceOwnerGuid, new MapDb2Entries(mapEntry, global.getDB2Mgr().GetDownscaledMapDifficultyData(at.target_mapId, tempRef_difficulty)));
                    difficulty = tempRef_difficulty.refArgValue;

                    if (instanceLock != null) {
                        entranceLocation = global.getObjectMgr().getWorldSafeLoc(instanceLock.getData().getEntranceWorldSafeLocId());
                    }
                }
            }

            if (entranceLocation != null) {
                player.teleportTo(entranceLocation.loc, TeleportToOptions.NotLeaveTransport);
            } else {
                player.teleportTo(at.target_mapId, at.target_X, at.target_Y, at.target_Z, at.target_Orientation, TeleportToOptions.NotLeaveTransport);
            }
        }
    }


    private void handlePlayedTime(RequestPlayedTime packet) {
        PlayedTime playedTime = new PlayedTime();
        playedTime.totalTime = getPlayer().getTotalPlayedTime();
        playedTime.levelTime = getPlayer().getLevelPlayedTime();
        playedTime.triggerEvent = packet.triggerScriptEvent; // 0-1 - will not show in chat frame
        sendPacket(playedTime);
    }


    private void handleSaveCUFProfiles(SaveCUFProfiles packet) {
        if (packet.CUFProfiles.size() > PlayerConst.MaxCUFProfiles) {
            Log.outError(LogFilter.player, "HandleSaveCUFProfiles - {0} tried to save more than {1} CUF profiles. Hacking attempt?", getPlayerName(), PlayerConst.MaxCUFProfiles);

            return;
        }

        for (byte i = 0; i < packet.CUFProfiles.size(); ++i) {
            getPlayer().saveCUFProfile(i, packet.CUFProfiles.get(i));
        }

        for (var i = (byte) packet.CUFProfiles.size(); i < PlayerConst.MaxCUFProfiles; ++i) {
            getPlayer().saveCUFProfile(i, null);
        }
    }


    private void handleSetAdvancedCombatLogging(SetAdvancedCombatLogging setAdvancedCombatLogging) {
        getPlayer().setAdvancedCombatLogging(setAdvancedCombatLogging.enable);
    }


    private void handleMountSpecialAnim(MountSpecial mountSpecial) {
        SpecialMountAnim specialMountAnim = new SpecialMountAnim();
        specialMountAnim.unitGUID = player.getGUID();
        tangible.IntegerLists.addPrimitiveArrayToList(mountSpecial.spellVisualKitIDs, specialMountAnim.spellVisualKitIDs);
        specialMountAnim.sequenceVariation = mountSpecial.sequenceVariation;
        getPlayer().sendMessageToSet(specialMountAnim, false);
    }


    private void handleMountSetFavorite(MountSetFavorite mountSetFavorite) {
        collectionMgr.mountSetFavorite(mountSetFavorite.mountSpellID, mountSetFavorite.isFavorite);
    }


    private void handleCloseInteraction(CloseInteraction closeInteraction) {
        if (Objects.equals(player.getPlayerTalkClass().getInteractionData().getSourceGuid(), closeInteraction.sourceGuid)) {
            player.getPlayerTalkClass().getInteractionData().reset();
        }
    }


    private void handleConversationLineStarted(ConversationLineStarted conversationLineStarted) {
        var convo = ObjectAccessor.getConversation(player, conversationLineStarted.conversationGUID);

        if (convo != null) {
            global.getScriptMgr().<IConversationOnConversationLineStarted>RunScript(script -> script.OnConversationLineStarted(convo, conversationLineStarted.lineID, player), convo.getScriptId());
        }
    }


    private void handleRequestLatestSplashScreen(RequestLatestSplashScreen requestLatestSplashScreen) {
        UISplashScreenRecord splashScreen = null;

        for (var itr : CliDB.UISplashScreenStorage.values()) {
            var playerCondition = CliDB.PlayerConditionStorage.get(itr.CharLevelConditionID);

            if (playerCondition != null) {
                if (!ConditionManager.isPlayerMeetingCondition(player, playerCondition)) {
                    continue;
                }
            }

            splashScreen = itr;
        }

        SplashScreenShowLatest splashScreenShowLatest = new SplashScreenShowLatest();
        splashScreenShowLatest.UISplashScreenID = splashScreen != null ? splashScreen.Id : 0;
        sendPacket(splashScreenShowLatest);
    }


    private void handleUnregisterAllAddonPrefixes(ChatUnregisterAllAddonPrefixes packet) {
        registeredAddonPrefixes.clear();
    }


    private void handleAddonRegisteredPrefixes(ChatRegisterAddonPrefixes packet) {
        registeredAddonPrefixes.addAll(Arrays.asList(packet.prefixes));

        if (registeredAddonPrefixes.size() > 64) // shouldn't happen
        {
            filterAddonMessages = false;

            return;
        }

        filterAddonMessages = true;
    }


    private void handleTogglePvP(TogglePvP packet) {
        if (!getPlayer().hasPlayerFlag(playerFlags.InPVP)) {
            getPlayer().setPlayerFlag(playerFlags.InPVP);
            getPlayer().removePlayerFlag(playerFlags.PVPTimer);

            if (!getPlayer().isPvP() || getPlayer().pvpInfo.endTimer != 0) {
                getPlayer().updatePvP(true, true);
            }
        } else if (!getPlayer().isWarModeLocalActive()) {
            getPlayer().removePlayerFlag(playerFlags.InPVP);
            getPlayer().setPlayerFlag(playerFlags.PVPTimer);

            if (!getPlayer().pvpInfo.isHostile && getPlayer().isPvP()) {
                getPlayer().pvpInfo.endTimer = gameTime.GetGameTime(); // start toggle-off
            }
        }
    }


    private void handleSetPvP(SetPvP packet) {
        if (packet.enablePVP) {
            getPlayer().setPlayerFlag(playerFlags.InPVP);
            getPlayer().removePlayerFlag(playerFlags.PVPTimer);

            if (!getPlayer().isPvP() || getPlayer().pvpInfo.endTimer != 0) {
                getPlayer().updatePvP(true, true);
            }
        } else if (!getPlayer().isWarModeLocalActive()) {
            getPlayer().removePlayerFlag(playerFlags.InPVP);
            getPlayer().setPlayerFlag(playerFlags.PVPTimer);

            if (!getPlayer().pvpInfo.isHostile && getPlayer().isPvP()) {
                getPlayer().pvpInfo.endTimer = gameTime.GetGameTime(); // start toggle-off
            }
        }
    }


    private void handleSetWarMode(SetWarMode packet) {
        player.setWarModeDesired(packet.enable);
    }


    private void handleFarSight(FarSight farSight) {
        if (farSight.enable) {
            Log.outDebug(LogFilter.Network, "Added FarSight {0} to player {1}", getPlayer().getActivePlayerData().farsightObject.toString(), getPlayer().getGUID().toString());
            var target = getPlayer().getViewpoint();

            if (target) {
                getPlayer().setSeer(target);
            } else {
                Log.outDebug(LogFilter.Network, "Player {0} (GUID: {1}) requests non-existing seer {2}", getPlayer().getName(), getPlayer().getGUID().toString(), getPlayer().getActivePlayerData().farsightObject.toString());
            }
        } else {
            Log.outDebug(LogFilter.Network, "Player {0} set vision to self", getPlayer().getGUID().toString());
            getPlayer().setSeer(getPlayer());
        }

        getPlayer().updateVisibilityForPlayer();
    }


    private void handleSetTitle(SetTitle packet) {
        // -1 at none
        if (packet.titleID > 0) {
            if (!getPlayer().hasTitle((int) packet.titleID)) {
                return;
            }
        } else {
            packet.titleID = 0;
        }

        getPlayer().setChosenTitle((int) packet.titleID);
    }


    private void handleResetInstances(ResetInstances packet) {
        var map = player.getMap();

        if (map != null && map.isInstanceable()) {
            return;
        }

        var group = getPlayer().getGroup();

        if (group) {
            if (!group.isLeader(getPlayer().getGUID())) {
                return;
            }

            if (group.isLFGGroup()) {
                return;
            }

            group.resetInstances(InstanceResetMethod.Manual, player);
        } else {
            getPlayer().resetInstances(InstanceResetMethod.Manual);
        }
    }


    private void handleSetDungeonDifficulty(SetDungeonDifficulty setDungeonDifficulty) {
        var difficultyEntry = CliDB.DifficultyStorage.get(setDungeonDifficulty.difficultyID);

        if (difficultyEntry == null) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleSetDungeonDifficulty: {0} sent an invalid instance mode {1}!", getPlayer().getGUID().toString(), setDungeonDifficulty.difficultyID);

            return;
        }

        if (difficultyEntry.instanceType != MapTypes.instance) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleSetDungeonDifficulty: {0} sent an non-dungeon instance mode {1}!", getPlayer().getGUID().toString(), difficultyEntry.id);

            return;
        }

        if (!difficultyEntry.flags.hasFlag(DifficultyFlags.CanSelect)) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleSetDungeonDifficulty: {0} sent unselectable instance mode {1}!", getPlayer().getGUID().toString(), difficultyEntry.id);

            return;
        }

        var difficultyID = Difficulty.forValue(difficultyEntry.id);

        if (difficultyID == getPlayer().getDungeonDifficultyId()) {
            return;
        }

        // cannot reset while in an instance
        var map = getPlayer().getMap();

        if (map && map.isInstanceable()) {
            Log.outDebug(LogFilter.Network, "WorldSession:HandleSetDungeonDifficulty: player (Name: {0}, {1}) tried to reset the instance while player is inside!", getPlayer().getName(), getPlayer().getGUID().toString());

            return;
        }

        var group = getPlayer().getGroup();

        if (group) {
            if (!group.isLeader(player.getGUID())) {
                return;
            }

            if (group.isLFGGroup()) {
                return;
            }

            // the difficulty is set even if the instances can't be reset
            group.resetInstances(InstanceResetMethod.OnChangeDifficulty, player);
            group.setDungeonDifficultyID(difficultyID);
        } else {
            getPlayer().resetInstances(InstanceResetMethod.OnChangeDifficulty);
            getPlayer().setDungeonDifficultyId(difficultyID);
            getPlayer().sendDungeonDifficulty();
        }
    }


    private void handleSetRaidDifficulty(SetRaidDifficulty setRaidDifficulty) {
        var difficultyEntry = CliDB.DifficultyStorage.get(setRaidDifficulty.difficultyID);

        if (difficultyEntry == null) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleSetDungeonDifficulty: {0} sent an invalid instance mode {1}!", getPlayer().getGUID().toString(), setRaidDifficulty.difficultyID);

            return;
        }

        if (difficultyEntry.instanceType != MapTypes.raid) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleSetDungeonDifficulty: {0} sent an non-dungeon instance mode {1}!", getPlayer().getGUID().toString(), difficultyEntry.id);

            return;
        }

        if (!difficultyEntry.flags.hasFlag(DifficultyFlags.CanSelect)) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleSetDungeonDifficulty: {0} sent unselectable instance mode {1}!", getPlayer().getGUID().toString(), difficultyEntry.id);

            return;
        }

        if (((int) (difficultyEntry.flags.getValue() & DifficultyFlags.legacy.getValue()) != 0) != (setRaidDifficulty.legacy != 0)) {
            Log.outDebug(LogFilter.Network, "WorldSession.HandleSetDungeonDifficulty: {0} sent not matching legacy difficulty {1}!", getPlayer().getGUID().toString(), difficultyEntry.id);

            return;
        }

        var difficultyID = Difficulty.forValue(difficultyEntry.id);

        if (difficultyID == (setRaidDifficulty.legacy != 0 ? getPlayer().getLegacyRaidDifficultyId() : getPlayer().getRaidDifficultyId())) {
            return;
        }

        // cannot reset while in an instance
        var map = getPlayer().getMap();

        if (map && map.isInstanceable()) {
            Log.outDebug(LogFilter.Network, "WorldSession:HandleSetRaidDifficulty: player (Name: {0}, {1} tried to reset the instance while inside!", getPlayer().getName(), getPlayer().getGUID().toString());

            return;
        }

        var group = getPlayer().getGroup();

        if (group) {
            if (!group.isLeader(player.getGUID())) {
                return;
            }

            if (group.isLFGGroup()) {
                return;
            }

            // the difficulty is set even if the instances can't be reset
            group.resetInstances(InstanceResetMethod.OnChangeDifficulty, player);

            if (setRaidDifficulty.legacy != 0) {
                group.setLegacyRaidDifficultyID(difficultyID);
            } else {
                group.setRaidDifficultyID(difficultyID);
            }
        } else {
            getPlayer().resetInstances(InstanceResetMethod.OnChangeDifficulty);

            if (setRaidDifficulty.legacy != 0) {
                getPlayer().setLegacyRaidDifficultyId(difficultyID);
            } else {
                getPlayer().setRaidDifficultyId(difficultyID);
            }

            getPlayer().sendRaidDifficulty(setRaidDifficulty.legacy != 0);
        }
    }


    private void handleSetTaxiBenchmark(SetTaxiBenchmarkMode packet) {
        if (packet.enable) {
            player.setPlayerFlag(playerFlags.TaxiBenchmark);
        } else {
            player.removePlayerFlag(playerFlags.TaxiBenchmark);
        }
    }


    private void handleGuildSetFocusedAchievement(GuildSetFocusedAchievement setFocusedAchievement) {
        var guild = global.getGuildMgr().getGuildById(getPlayer().getGuildId());

        if (guild) {
            guild.getAchievementMgr().sendAchievementInfo(getPlayer(), setFocusedAchievement.achievementID);
        }
    }


    private void handleInstanceLockResponse(InstanceLockResponse packet) {
        if (!getPlayer().getHasPendingBind()) {
            Log.outInfo(LogFilter.Network, "InstanceLockResponse: Player {0} (guid {1}) tried to bind himself/teleport to graveyard without a pending bind!", getPlayer().getName(), getPlayer().getGUID().toString());

            return;
        }

        if (packet.acceptLock) {
            getPlayer().confirmPendingBind();
        } else {
            getPlayer().repopAtGraveyard();
        }

        getPlayer().setPendingBind(0, 0);
    }


    private void handleWarden3Data(WardenData packet) {
        if (warden == null || packet.data.getSize() == 0) {
            return;
        }

        warden.decryptData(packet.data.getData());
        var opcode = WardenOpcodes.forValue(packet.data.readUInt8());

        switch (opcode) {
            case SmsgModuleUse:
            case CmsgModuleMissing:
                warden.sendModuleToClient();

                break;
            case SmsgModuleCache:
            case CmsgModuleOk:
                warden.requestHash();

                break;
            case CmsgCheatChecksResult:
            case SmsgCheatChecksRequest:
                warden.handleData(packet.data);

                break;
            case SmsgModuleInitialize:
            case CmsgMemChecksResult:
                Log.outDebug(LogFilter.Warden, "NYI WARDEN_CMSG_MEM_CHECKS_RESULT received!");

                break;
            case SmsgMemChecksRequest:
            case CmsgHashResult:
                warden.handleHashResult(packet.data);
                warden.initializeModule();

                break;
            case SmsgHashRequest:
            case CmsgModuleFailed:
                Log.outDebug(LogFilter.Warden, "NYI WARDEN_CMSG_MODULE_FAILED received!");

                break;
            default:
                Log.outDebug(LogFilter.Warden, "Got unknown warden opcode {0} of size {1}.", opcode, packet.data.getSize() - 1);

                break;
        }
    }


    private void handleMovement(ClientPlayerMovement packet) {
        handleMovementOpcode(packet.GetOpcode(), packet.status);
    }

    private void handleMovementOpcode(ClientOpcodes opcode, MovementInfo movementInfo) {
        var mover = getPlayer().getUnitBeingMoved();
        var plrMover = mover.toPlayer();

        if (plrMover && plrMover.isBeingTeleported()) {
            return;
        }

        getPlayer().validateMovementInfo(movementInfo);

        if (ObjectGuid.opNotEquals(movementInfo.getGuid(), mover.getGUID())) {
            Log.outError(LogFilter.Network, "HandleMovementOpcodes: guid error");

            return;
        }

        if (!movementInfo.getPos().isPositionValid()) {
            return;
        }


        if (!mover.getMoveSpline().finalized()) {
            return;
        }

        // stop some emotes at player move
        if (plrMover && (plrMover.getEmoteState() != 0)) {
            plrMover.setEmoteState(emote.OneshotNone);
        }

        //handle special cases
        if (!movementInfo.transport.guid.isEmpty()) {
            // We were teleported, skip packets that were broadcast before teleport
            if (movementInfo.getPos().getExactdist2D(mover.getLocation()) > MapDefine.SizeofGrids) {
                return;
            }

            if (Math.abs(movementInfo.transport.pos.getX()) > 75f || Math.abs(movementInfo.transport.pos.getY()) > 75f || Math.abs(movementInfo.transport.pos.getZ()) > 75f) {
                return;
            }

            if (!MapDefine.isValidMapCoordinatei(movementInfo.getPos().getX() + movementInfo.transport.pos.getX(), movementInfo.getPos().getY() + movementInfo.transport.pos.getY(), movementInfo.getPos().getZ() + movementInfo.transport.pos.getZ(), movementInfo.getPos().getO() + movementInfo.transport.pos.getO())) {
                return;
            }

            if (plrMover) {
                if (plrMover.getTransport() == null) {
                    var go = plrMover.getMap().getGameObject(movementInfo.transport.guid);

                    if (go != null) {
                        var transport = go.toTransportBase();

                        if (transport != null) {
                            transport.addPassenger(plrMover);
                        }
                    }
                } else if (ObjectGuid.opNotEquals(plrMover.getTransport().getTransportGUID(), movementInfo.transport.guid)) {
                    plrMover.getTransport().removePassenger(plrMover);
                    var go = plrMover.getMap().getGameObject(movementInfo.transport.guid);

                    if (go != null) {
                        var transport = go.toTransportBase();

                        if (transport != null) {
                            transport.addPassenger(plrMover);
                        } else {
                            movementInfo.resetTransport();
                        }
                    } else {
                        movementInfo.resetTransport();
                    }
                }
            }

            if (mover.getTransport() == null && !mover.getVehicle1()) {
                movementInfo.transport.reset();
            }
        } else if (plrMover && plrMover.getTransport() != null) // if we were on a transport, leave
        {
            plrMover.getTransport().removePassenger(plrMover);
        }

        // fall damage generation (ignore in flight case that can be triggered also at lags in moment teleportation to another map).
        if (opcode == ClientOpcodes.MoveFallLand && plrMover && !plrMover.isInFlight()) {
            plrMover.handleFall(movementInfo);
        }

        // interrupt parachutes upon falling or landing in water
        if (opcode == ClientOpcodes.MoveFallLand || opcode == ClientOpcodes.MoveStartSwim || opcode == ClientOpcodes.MoveSetFly) {
            mover.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.LandingOrFlight); // Parachutes
        }

        movementInfo.setGuid(mover.getGUID());
        movementInfo.setTime(adjustClientMovementTime(movementInfo.getTime()));
        mover.setMovementInfo(movementInfo);

        // Some vehicles allow the passenger to turn by himself
        var vehicle = mover.getVehicle1();

        if (vehicle) {
            var seat = vehicle.GetSeatForPassenger(mover);

            if (seat != null) {
                if (seat.hasFlag(VehicleSeatFlags.AllowTurning)) {
                    if (movementInfo.getPos().getO() != mover.getLocation().getO()) {
                        mover.getLocation().setO(movementInfo.getPos().getO());
                        mover.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.Turning);
                    }
                }
            }

            return;
        }

        mover.updatePosition(movementInfo.getPos());

        MoveUpdate moveUpdate = new moveUpdate();
        moveUpdate.status = mover.getMovementInfo();
        mover.sendMessageToSet(moveUpdate, getPlayer());

        if (plrMover) // nothing is charmed, or player charmed
        {
            if (plrMover.isSitState() && movementInfo.hasMovementFlag(MovementFlag.MaskMoving.getValue() | MovementFlag.MaskTurning.getValue())) {
                plrMover.setStandState(UnitStandStateType.Stand);
            }

            plrMover.updateFallInformationIfNeed(movementInfo, opcode);

            if (movementInfo.getPos().getZ() < plrMover.getMap().getMinHeight(plrMover.getPhaseShift(), movementInfo.getPos().getX(), movementInfo.getPos().getY())) {
                if (!(plrMover.getBattleground() && plrMover.getBattleground().handlePlayerUnderMap(getPlayer()))) {
                    // NOTE: this is actually called many times while falling
                    // even after the player has been teleported away
                    // @todo discard movement packets after the player is rooted
                    if (plrMover.isAlive()) {
                        Log.outDebug(LogFilter.player, String.format("FALLDAMAGE Below map. Map min height: %1$s, Player debug info:\n%2$s", plrMover.getMap().getMinHeight(plrMover.getPhaseShift(), movementInfo.getPos().getX(), movementInfo.getPos().getY()), plrMover.getDebugInfo()));
                        plrMover.setPlayerFlag(playerFlags.IsOutOfBounds);
                        plrMover.environmentalDamage(EnviromentalDamage.FallToVoid, (int) getPlayer().getMaxHealth());

                        // player can be alive if GM/etc
                        // change the death state to CORPSE to prevent the death timer from
                        // starting in the next player update
                        if (plrMover.isAlive()) {
                            plrMover.killPlayer();
                        }
                    }
                }
            } else {
                plrMover.removePlayerFlag(playerFlags.IsOutOfBounds);
            }

            if (opcode == ClientOpcodes.MoveJump) {
                plrMover.removeAurasWithInterruptFlags(SpellAuraInterruptFlags2.jump); // Mind Control
                unit.procSkillsAndAuras(plrMover, null, new ProcFlagsInit(procFlags.jump), new ProcFlagsInit(procFlags.NONE), ProcFlagsSpellType.MaskAll, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, null, null, null);
            }
        }
    }


    private void handleMoveWorldportAck(WorldPortResponse packet) {
        handleMoveWorldportAck();
    }

    private void handleMoveWorldportAck() {
        var player = getPlayer();

        // ignore unexpected far teleports
        if (!player.isBeingTeleportedFar()) {
            return;
        }

        var seamlessTeleport = player.isBeingTeleportedSeamlessly();
        player.setSemaphoreTeleportFar(false);

        // get the teleport destination
        var loc = player.getTeleportDest();

        // possible errors in the coordinate validity check
        if (!MapDefine.isValidMapCoordinatei(loc)) {
            logoutPlayer(false);

            return;
        }

        // get the destination map entry, not the current one, this will fix homebind and reset greeting
        var mapEntry = CliDB.MapStorage.get(loc.getMapId());

        // reset instance validity, except if going to an instance inside an instance
        if (!player.getInstanceValid() && !mapEntry.IsDungeon()) {
            player.setInstanceValid(true);
        }

        var oldMap = player.getMap();
        var newMap = getPlayer().getTeleportDestInstanceId() != null ? global.getMapMgr().findMap(loc.getMapId(), getPlayer().getTeleportDestInstanceId().intValue()) : global.getMapMgr().CreateMap(loc.getMapId(), getPlayer());

        var transportInfo = player.getMovementInfo().transport;
        var transport = player.getTransport();

        if (transport != null) {
            transport.removePassenger(player);
        }

        if (player.isInWorld()) {
            Log.outError(LogFilter.Network, String.format("Player (Name %1$s) is still in world when teleported from map %2$s to new map %3$s", player.getName(), oldMap.getId(), loc.getMapId()));
            oldMap.removePlayerFromMap(player, false);
        }

        // relocate the player to the teleport destination
        // the CannotEnter checks are done in TeleporTo but conditions may change
        // while the player is in transit, for example the map may get full
        if (newMap == null || newMap.cannotEnter(player) != null) {
            Log.outError(LogFilter.Network, String.format("Map %1$s could not be created for %2$s (%3$s), porting player to homebind", loc.getMapId(), (newMap ? newMap.getMapName() : "Unknown"), player.getGUID()));
            player.teleportTo(player.getHomeBind());

            return;
        }

        var z = loc.getZ() + player.getHoverOffset();
        player.getLocation().relocate(loc.getX(), loc.getY(), z, loc.getO());
        player.setFallInformation(0, player.getLocation().getZ());

        player.resetMap();
        player.setMap(newMap);

        ResumeToken resumeToken = new ResumeToken();
        resumeToken.sequenceIndex = player.getMovementCounter();
        resumeToken.reason = seamlessTeleport ? 2 : 1;
        sendPacket(resumeToken);

        if (!seamlessTeleport) {
            player.sendInitialPacketsBeforeAddToMap();
        }

        // move player between transport copies on each map
        var newTransport = newMap.getTransport(transportInfo.guid);

        if (newTransport != null) {
            player.getMovementInfo().transport = transportInfo;
            newTransport.addPassenger(player);
        }

        if (!player.getMap().addPlayerToMap(player, !seamlessTeleport)) {
            Log.outError(LogFilter.Network, String.format("WORLD: failed to teleport player %1$s (%2$s) to map %3$s (%4$s) because of unknown reason!", player.getName(), player.getGUID(), loc.getMapId(), (newMap ? newMap.getMapName() : "Unknown")));
            player.resetMap();
            player.setMap(oldMap);
            player.teleportTo(player.getHomeBind());

            return;
        }

        // Battleground state prepare (in case join to BG), at relogin/tele player not invited
        // only add to bg group and object, if the player was invited (else he entered through command)
        if (player.getInBattleground()) {
            // cleanup setting if outdated
            if (!mapEntry.IsBattlegroundOrArena()) {
                // We're not in BG
                player.setBattlegroundId(0, BattlegroundTypeId.NONE);
                // reset destination bg team
                player.setBgTeam(0);
            }
            // join to bg case
            else {
                var bg = player.getBattleground();

                if (bg) {
                    if (player.isInvitedForBattlegroundInstance(player.getBattlegroundId())) {
                        bg.addPlayer(player);
                    }
                }
            }
        }

        if (!seamlessTeleport) {
            player.sendInitialPacketsAfterAddToMap();
        } else {
            player.updateVisibilityForPlayer();
            var garrison = player.getGarrison();

            if (garrison != null) {
                garrison.sendRemoteInfo();
            }
        }

        // flight fast teleport case
        if (player.isInFlight()) {
            if (!player.getInBattleground()) {
                if (!seamlessTeleport) {
                    // short preparations to continue flight
                    var movementGenerator = player.getMotionMaster().getCurrentMovementGenerator();
                    movementGenerator.initialize(player);
                }

                return;
            }

            // Battlegroundstate prepare, stop flight
            player.finishTaxiFlight();
        }

        if (!player.isAlive() && player.getTeleportOptions().hasFlag(TeleportToOptions.ReviveAtTeleport)) {
            player.resurrectPlayer(0.5f);
        }

        // resurrect character at enter into instance where his corpse exist after add to map
        if (mapEntry.IsDungeon() && !player.isAlive()) {
            if (player.getCorpseLocation().getMapId() == mapEntry.id) {
                player.resurrectPlayer(0.5f, false);
                player.spawnCorpseBones();
            }
        }

        if (mapEntry.IsDungeon()) {
            // check if this instance has a reset time and send it to player if so
            MapDb2Entries entries = new MapDb2Entries(mapEntry.id, newMap.getDifficultyID());

            if (entries.MapDifficulty.HasResetSchedule()) {
                RaidInstanceMessage raidInstanceMessage = new RaidInstanceMessage();
                raidInstanceMessage.type = InstanceResetWarningType.Welcome;
                raidInstanceMessage.mapID = mapEntry.id;
                raidInstanceMessage.difficultyID = newMap.getDifficultyID();

                var playerLock = global.getInstanceLockMgr().findActiveInstanceLock(getPlayer().getGUID(), entries);

                if (playerLock != null) {
                    raidInstanceMessage.locked = !playerLock.isExpired();
                    raidInstanceMessage.extended = playerLock.isExtended();
                } else {
                    raidInstanceMessage.locked = false;
                    raidInstanceMessage.extended = false;
                }

                sendPacket(raidInstanceMessage);
            }

            // check if instance is valid
            if (!player.checkInstanceValidity(false)) {
                player.setInstanceValid(false);
            }
        }

        // update zone immediately, otherwise leave channel will cause crash in mtmap
        int newzone;
        tangible.OutObject<Integer> tempOut_newzone = new tangible.OutObject<Integer>();
        int newarea;
        tangible.OutObject<Integer> tempOut_newarea = new tangible.OutObject<Integer>();
        player.getZoneAndAreaId(tempOut_newzone, tempOut_newarea);
        newarea = tempOut_newarea.outArgValue;
        newzone = tempOut_newzone.outArgValue;
        player.updateZone(newzone, newarea);

        // honorless target
        if (player.pvpInfo.isHostile) {
            player.castSpell(player, 2479, true);
        }

        // in friendly area
        else if (player.isPvP() && !player.hasPlayerFlag(playerFlags.InPVP)) {
            player.updatePvP(false, false);
        }

        // resummon pet
        player.resummonPetTemporaryUnSummonedIfAny();

        //lets process all delayed operations on successful teleport
        player.processDelayedOperations();
    }


    private void handleSuspendTokenResponse(SuspendTokenResponse suspendTokenResponse) {
        if (!player.isBeingTeleportedFar()) {
            return;
        }

        var loc = getPlayer().getTeleportDest();

        if (CliDB.MapStorage.get(loc.getMapId()).IsDungeon()) {
            UpdateLastInstance updateLastInstance = new UpdateLastInstance();
            updateLastInstance.mapID = loc.getMapId();
            sendPacket(updateLastInstance);
        }

        NewWorld packet = new NewWorld();
        packet.mapID = loc.getMapId();
        packet.loc.pos = loc;
        packet.reason = (int) (!player.isBeingTeleportedSeamlessly() ? NewWorldReason.Normal : NewWorldReason.Seamless);
        sendPacket(packet);

        if (player.isBeingTeleportedSeamlessly()) {
            handleMoveWorldportAck();
        }
    }


    private void handleMoveTeleportAck(MoveTeleportAck packet) {
        var plMover = getPlayer().getUnitBeingMoved().toPlayer();

        if (!plMover || !plMover.isBeingTeleportedNear()) {
            return;
        }

        if (ObjectGuid.opNotEquals(packet.moverGUID, plMover.getGUID())) {
            return;
        }

        plMover.setSemaphoreTeleportNear(false);

        var old_zone = plMover.getZoneId();

        var dest = plMover.getTeleportDest();

        plMover.updatePosition(dest, true);
        plMover.setFallInformation(0, getPlayer().getLocation().getZ());

        int newzone;
        tangible.OutObject<Integer> tempOut_newzone = new tangible.OutObject<Integer>();
        int newarea;
        tangible.OutObject<Integer> tempOut_newarea = new tangible.OutObject<Integer>();
        plMover.getZoneAndAreaId(tempOut_newzone, tempOut_newarea);
        newarea = tempOut_newarea.outArgValue;
        newzone = tempOut_newzone.outArgValue;
        plMover.updateZone(newzone, newarea);

        // new zone
        if (old_zone != newzone) {
            // honorless target
            if (plMover.pvpInfo.isHostile) {
                plMover.castSpell(plMover, 2479, true);
            }

            // in friendly area
            else if (plMover.isPvP() && !plMover.hasPlayerFlag(playerFlags.InPVP)) {
                plMover.updatePvP(false, false);
            }
        }

        // resummon pet
        getPlayer().resummonPetTemporaryUnSummonedIfAny();

        //lets process all delayed operations on successful teleport
        getPlayer().processDelayedOperations();
    }


    private void handleForceSpeedChangeAck(MovementSpeedAck packet) {
        getPlayer().validateMovementInfo(packet.ack.status);

        // now can skip not our packet
        if (ObjectGuid.opNotEquals(getPlayer().getGUID(), packet.ack.status.getGuid())) {
            return;
        }

        /*----------------*/
        // client ACK send one packet for mounted/run case and need skip all except last from its
        // in other cases anti-cheat check can be fail in false case
        UnitMoveType move_type;

        var opcode = packet.GetOpcode();

        switch (opcode) {
            case MoveForceWalkSpeedChangeAck:
                move_type = UnitMoveType.Walk;

                break;
            case MoveForceRunSpeedChangeAck:
                move_type = UnitMoveType.run;

                break;
            case MoveForceRunBackSpeedChangeAck:
                move_type = UnitMoveType.RunBack;

                break;
            case MoveForceSwimSpeedChangeAck:
                move_type = UnitMoveType.swim;

                break;
            case MoveForceSwimBackSpeedChangeAck:
                move_type = UnitMoveType.SwimBack;

                break;
            case MoveForceTurnRateChangeAck:
                move_type = UnitMoveType.turnRate;

                break;
            case MoveForceFlightSpeedChangeAck:
                move_type = UnitMoveType.flight;

                break;
            case MoveForceFlightBackSpeedChangeAck:
                move_type = UnitMoveType.FlightBack;

                break;
            case MoveForcePitchRateChangeAck:
                move_type = UnitMoveType.pitchRate;

                break;
            default:
                Log.outError(LogFilter.Network, "WorldSession.HandleForceSpeedChangeAck: Unknown move type opcode: {0}", opcode);

                return;
        }

        // skip all forced speed changes except last and unexpected
        // in run/mounted case used one ACK and it must be skipped. m_forced_speed_changes[MOVE_RUN] store both.
        if (getPlayer().getForcedSpeedChanges()[move_type.getValue()] > 0) {
            setPlayer(getPlayer() - 1);
            getPlayer().getForcedSpeedChanges()[move_type.getValue()];

            if (getPlayer().getForcedSpeedChanges()[move_type.getValue()] > 0) {
                return;
            }
        }

        if (getPlayer().getTransport() == null && Math.abs(getPlayer().getSpeed(move_type) - packet.speed) > 0.01f) {
            if (getPlayer().getSpeed(move_type) > packet.speed) // must be greater - just correct
            {
                Log.outError(LogFilter.Network, "{0}SpeedChange player {1} is NOT correct (must be {2} instead {3}), force set to correct value", move_type, getPlayer().getName(), getPlayer().getSpeed(move_type), packet.speed);

                getPlayer().setSpeedRate(move_type, getPlayer().getSpeedRate(move_type));
            } else // must be lesser - cheating
            {
                Log.outDebug(LogFilter.Server, "Player {0} from account id {1} kicked for incorrect speed (must be {2} instead {3})", getPlayer().getName(), getPlayer().getSession().getAccountId(), getPlayer().getSpeed(move_type), packet.speed);

                getPlayer().getSession().kickPlayer("WorldSession::HandleForceSpeedChangeAck Incorrect speed");
            }
        }
    }


    private void handleSetActiveMover(SetActiveMover packet) {
        if (getPlayer().isInWorld()) {
            if (ObjectGuid.opNotEquals(player.getUnitBeingMoved().getGUID(), packet.activeMover)) {
                Log.outError(LogFilter.Network, "HandleSetActiveMover: incorrect mover guid: mover is {0} and should be {1},", packet.activeMover.toString(), player.getUnitBeingMoved().getGUID().toString());
            }
        }
    }


    private void handleMoveKnockBackAck(MoveKnockBackAck movementAck) {
        getPlayer().validateMovementInfo(movementAck.ack.status);

        if (ObjectGuid.opNotEquals(getPlayer().getUnitBeingMoved().getGUID(), movementAck.ack.status.getGuid())) {
            return;
        }

        movementAck.ack.status.setTime(adjustClientMovementTime(movementAck.ack.status.getTime()));
        getPlayer().setMovementInfo(movementAck.ack.status);

        MoveUpdateKnockBack updateKnockBack = new MoveUpdateKnockBack();
        updateKnockBack.status = getPlayer().getMovementInfo();
        getPlayer().sendMessageToSet(updateKnockBack, false);
    }


    private void handleMovementAckMessage(MovementAckMessage movementAck) {
        getPlayer().validateMovementInfo(movementAck.ack.status);
    }


    private void handleSummonResponseOpcode(SummonResponse packet) {
        if (!getPlayer().isAlive() || getPlayer().isInCombat()) {
            return;
        }

        getPlayer().summonIfPossible(packet.accept);
    }


    private void handleSetCollisionHeightAck(MoveSetCollisionHeightAck packet) {
        getPlayer().validateMovementInfo(packet.data.status);
    }


    private void handleMoveApplyMovementForceAck(MoveApplyMovementForceAck moveApplyMovementForceAck) {
        var mover = player.getUnitBeingMoved();
        player.validateMovementInfo(moveApplyMovementForceAck.ack.status);

        // prevent tampered movement data
        if (ObjectGuid.opNotEquals(moveApplyMovementForceAck.ack.status.getGuid(), mover.getGUID())) {
            Log.outError(LogFilter.Network, String.format("HandleMoveApplyMovementForceAck: guid error, expected %1$s, got %2$s", mover.getGUID(), moveApplyMovementForceAck.ack.status.getGuid()));

            return;
        }

        moveApplyMovementForceAck.ack.status.setTime(adjustClientMovementTime(moveApplyMovementForceAck.ack.status.getTime()));

        MoveUpdateApplyMovementForce updateApplyMovementForce = new MoveUpdateApplyMovementForce();
        updateApplyMovementForce.status = moveApplyMovementForceAck.ack.status;
        updateApplyMovementForce.FORCE = moveApplyMovementForceAck.FORCE;
        mover.sendMessageToSet(updateApplyMovementForce, false);
    }


    private void handleMoveRemoveMovementForceAck(MoveRemoveMovementForceAck moveRemoveMovementForceAck) {
        var mover = player.getUnitBeingMoved();
        player.validateMovementInfo(moveRemoveMovementForceAck.ack.status);

        // prevent tampered movement data
        if (ObjectGuid.opNotEquals(moveRemoveMovementForceAck.ack.status.getGuid(), mover.getGUID())) {
            Log.outError(LogFilter.Network, String.format("HandleMoveRemoveMovementForceAck: guid error, expected %1$s, got %2$s", mover.getGUID(), moveRemoveMovementForceAck.ack.status.getGuid()));

            return;
        }

        moveRemoveMovementForceAck.ack.status.setTime(adjustClientMovementTime(moveRemoveMovementForceAck.ack.status.getTime()));

        MoveUpdateRemoveMovementForce updateRemoveMovementForce = new MoveUpdateRemoveMovementForce();
        updateRemoveMovementForce.status = moveRemoveMovementForceAck.ack.status;
        updateRemoveMovementForce.triggerGUID = moveRemoveMovementForceAck.ID;
        mover.sendMessageToSet(updateRemoveMovementForce, false);
    }


    private void handleMoveSetModMovementForceMagnitudeAck(MovementSpeedAck setModMovementForceMagnitudeAck) {
        var mover = player.getUnitBeingMoved();
        player.validateMovementInfo(setModMovementForceMagnitudeAck.ack.status);

        // prevent tampered movement data
        if (ObjectGuid.opNotEquals(setModMovementForceMagnitudeAck.ack.status.getGuid(), mover.getGUID())) {
            Log.outError(LogFilter.Network, String.format("HandleSetModMovementForceMagnitudeAck: guid error, expected %1$s, got %2$s", mover.getGUID(), setModMovementForceMagnitudeAck.ack.status.getGuid()));

            return;
        }

        // skip all except last
        if (player.getMovementForceModMagnitudeChanges() > 0) {
            player.setMovementForceModMagnitudeChanges(player.getMovementForceModMagnitudeChanges() - 1);

            if (player.getMovementForceModMagnitudeChanges() == 0) {
                var expectedModMagnitude = 1.0f;
                var movementForces = mover.getMovementForces();

                if (movementForces != null) {
                    expectedModMagnitude = movementForces.getModMagnitude();
                }

                if (Math.abs(expectedModMagnitude - setModMovementForceMagnitudeAck.speed) > 0.01f) {
                    Log.outDebug(LogFilter.misc, String.format("Player %1$s from account id %2$s kicked for incorrect movement force magnitude (must be %3$s instead %4$s)", player.getName(), player.getSession().getAccountId(), expectedModMagnitude, setModMovementForceMagnitudeAck.speed));
                    player.getSession().kickPlayer("WorldSession::HandleMoveSetModMovementForceMagnitudeAck Incorrect magnitude");

                    return;
                }
            }
        }

        setModMovementForceMagnitudeAck.ack.status.setTime(adjustClientMovementTime(setModMovementForceMagnitudeAck.ack.status.getTime()));

        MoveUpdateSpeed updateModMovementForceMagnitude = new MoveUpdateSpeed(ServerOpcode.MoveUpdateModMovementForceMagnitude);
        updateModMovementForceMagnitude.status = setModMovementForceMagnitudeAck.ack.status;
        updateModMovementForceMagnitude.speed = setModMovementForceMagnitudeAck.speed;
        mover.sendMessageToSet(updateModMovementForceMagnitude, false);
    }


    private void handleMoveTimeSkipped(MoveTimeSkipped moveTimeSkipped) {
        var mover = getPlayer().getUnitBeingMoved();

        if (mover == null) {
            Log.outWarn(LogFilter.player, String.format("WorldSession.HandleMoveTimeSkipped wrong mover state from the unit moved by %1$s", getPlayer().getGUID()));

            return;
        }

        // prevent tampered movement data
        if (ObjectGuid.opNotEquals(moveTimeSkipped.moverGUID, mover.getGUID())) {
            Log.outWarn(LogFilter.player, String.format("WorldSession.HandleMoveTimeSkipped wrong guid from the unit moved by %1$s", getPlayer().getGUID()));

            return;
        }

        mover.getMovementInfo().setTime(mover.getMovementInfo().getTime() + moveTimeSkipped.timeSkipped);

        MoveSkipTime moveSkipTime = new MoveSkipTime();
        moveSkipTime.moverGUID = moveTimeSkipped.moverGUID;
        moveSkipTime.timeSkipped = moveTimeSkipped.timeSkipped;
        mover.sendMessageToSet(moveSkipTime, player);
    }


    private void handleMoveSplineDoneOpcode(MoveSplineDone moveSplineDone) {
        var movementInfo = moveSplineDone.status;
        player.validateMovementInfo(movementInfo);

        // in taxi flight packet received in 2 case:
        // 1) end taxi path in far (multi-node) flight
        // 2) switch from one map to other in case multim-map taxi path
        // we need process only (1)

        var curDest = getPlayer().getTaxi().getTaxiDestination();

        if (curDest != 0) {
            var curDestNode = CliDB.TaxiNodesStorage.get(curDest);

            // far teleport case
            if (curDestNode != null && curDestNode.ContinentID != getPlayer().getLocation().getMapId() && getPlayer().getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.flight) {
                MovementGenerator tempVar = getPlayer().getMotionMaster().getCurrentMovementGenerator();
                var flight = tempVar instanceof FlightPathMovementGenerator ? (FlightPathMovementGenerator) tempVar : null;

                if (flight != null) {
                    // short preparations to continue flight
                    flight.setCurrentNodeAfterTeleport();
                    var node = flight.getPath().get((int) flight.getCurrentNode());
                    flight.skipCurrentNode();

                    getPlayer().teleportTo(curDestNode.ContinentID, node.loc.X, node.loc.Y, node.loc.Z, getPlayer().getLocation().getO());
                }
            }

            return;
        }

        // at this point only 1 node is expected (final destination)
        if (getPlayer().getTaxi().getPath().size() != 1) {
            return;
        }

        getPlayer().cleanupAfterTaxiFlight();
        getPlayer().setFallInformation(0, getPlayer().getLocation().getZ());

        if (getPlayer().pvpInfo.isHostile) {
            getPlayer().castSpell(getPlayer(), 2479, true);
        }
    }


    private void handleTimeSyncResponse(TimeSyncResponse timeSyncResponse) {
        if (!pendingTimeSyncRequests.containsKey(timeSyncResponse.sequenceIndex)) {
            return;
        }

        var serverTimeAtSent = pendingTimeSyncRequests.get(timeSyncResponse.sequenceIndex);
        pendingTimeSyncRequests.remove(timeSyncResponse.sequenceIndex);

        // time it took for the request to travel to the client, for the client to process it and reply and for response to travel back to the server.
        // we are going to make 2 assumptions:
        // 1) we assume that the request processing time equals 0.
        // 2) we assume that the packet took as much time to travel from server to client than it took to travel from client to server.
        var roundTripDuration = time.GetMSTimeDiff(serverTimeAtSent, timeSyncResponse.getReceivedTime());
        var lagDelay = roundTripDuration / 2;

		/*
		clockDelta = serverTime - clientTime
		where
		serverTime: time that was displayed on the clock of the SERVER at the moment when the client processed the SMSG_TIME_SYNC_REQUEST packet.
		clientTime:  time that was displayed on the clock of the CLIENT at the moment when the client processed the SMSG_TIME_SYNC_REQUEST packet.

		Once clockDelta has been computed, we can compute the time of an event on server clock when we know the time of that same event on the client clock,
		using the following relation:
		serverTime = clockDelta + clientTime
		*/
        var clockDelta = (long) (serverTimeAtSent + lagDelay) - (long) timeSyncResponse.clientTime;
        timeSyncClockDeltaQueue.PushFront(Tuple.create(clockDelta, roundTripDuration));
        computeNewClockDelta();
    }

    private void computeNewClockDelta() {
        // implementation of the technique described here: https://web.archive.org/web/20180430214420/http://www.mine-control.com/zack/timesync/timesync.html
        // to reduce the skew induced by dropped TCP packets that get resent.

        //accumulator_set < uint32, features < tag::mean, tag::median, tag::variance(lazy) > > latencyAccumulator;
        ArrayList<Integer> latencyList = new ArrayList<>();

        for (var pair : timeSyncClockDeltaQueue) {
            latencyList.add(pair.item2);
        }

        var latencyMedian = (int) Math.rint(latencyList.Average(p -> p)); //median(latencyAccumulator));
        var latencyStandardDeviation = (int) Math.rint(Math.sqrt(latencyList.variance())); //variance(latencyAccumulator)));

        //accumulator_set<long, features<tag::mean>> clockDeltasAfterFiltering;
        ArrayList<Long> clockDeltasAfterFiltering = new ArrayList<>();
        int sampleSizeAfterFiltering = 0;

        for (var pair : timeSyncClockDeltaQueue) {
            if (pair.item2 < latencyStandardDeviation + latencyMedian) {
                clockDeltasAfterFiltering.add(pair.Item1);
                sampleSizeAfterFiltering++;
            }
        }

        if (sampleSizeAfterFiltering != 0) {
            var meanClockDelta = (long) (Math.rint(clockDeltasAfterFiltering.Average()));

            if (Math.abs(meanClockDelta - timeSyncClockDelta) > 25) {
                timeSyncClockDelta = meanClockDelta;
            }
        } else if (timeSyncClockDelta == 0) {
            var back = timeSyncClockDeltaQueue.Back();
            timeSyncClockDelta = back.Item1;
        }
    }


    private void handleMoveInitActiveMoverComplete(MoveInitActiveMoverComplete moveInitActiveMoverComplete) {
        player.setPlayerLocalFlag(PlayerLocalFlags.OverrideTransportServerTime);
        player.setTransportServerTime((int) (gameTime.GetGameTimeMS() - moveInitActiveMoverComplete.ticks));

        player.updateObjectVisibility(false);
    }


    private void requestMythicPlusSeasonData(ClientPacket packet) {
        sendPacket(new MythicPlusSeasonData());
    }

    public final void sendTabardVendorActivate(ObjectGuid guid) {
        NPCInteractionOpenResult npcInteraction = new NPCInteractionOpenResult();
        npcInteraction.npc = guid;
        npcInteraction.interactionType = PlayerInteractionType.TabardVendor;
        npcInteraction.success = true;
        sendPacket(npcInteraction);
    }

    public final void sendTrainerList(Creature npc, int trainerId) {
        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var trainer = global.getObjectMgr().getTrainer(trainerId);

        if (trainer == null) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: SendTrainerList - trainer spells not found for trainer %1$s id %2$s", npc.getGUID(), trainerId));

            return;
        }

        player.getPlayerTalkClass().getInteractionData().reset();
        player.getPlayerTalkClass().getInteractionData().setSourceGuid(npc.getGUID());
        player.getPlayerTalkClass().getInteractionData().setTrainerId(trainerId);
        trainer.sendSpells(npc, player, getSessionDbLocaleIndex());
    }

    public final void sendStablePet(ObjectGuid guid) {
        PetStableList packet = new PetStableList();
        packet.stableMaster = guid;

        var petStable = getPlayer().getPetStable1();

        if (petStable == null) {
            sendPacket(packet);

            return;
        }

        for (int petSlot = 0; petSlot < petStable.ActivePets.length; ++petSlot) {
            if (petStable.ActivePets[petSlot] == null) {
                continue;
            }

            var pet = petStable.ActivePets[petSlot];
            PetStableInfo stableEntry = new PetStableInfo();
            stableEntry.petSlot = petSlot + PetSaveMode.FirstActiveSlot.getValue();
            stableEntry.petNumber = pet.petNumber;
            stableEntry.creatureID = pet.creatureId;
            stableEntry.displayID = pet.displayId;
            stableEntry.experienceLevel = pet.level;
            stableEntry.petFlags = PetStableinfo.active;
            stableEntry.petName = pet.name;

            packet.pets.add(stableEntry);
        }

        for (int petSlot = 0; petSlot < petStable.StabledPets.length; ++petSlot) {
            if (petStable.StabledPets[petSlot] == null) {
                continue;
            }

            var pet = petStable.StabledPets[petSlot];
            PetStableInfo stableEntry = new PetStableInfo();
            stableEntry.petSlot = petSlot + PetSaveMode.FirstStableSlot.getValue();
            stableEntry.petNumber = pet.petNumber;
            stableEntry.creatureID = pet.creatureId;
            stableEntry.displayID = pet.displayId;
            stableEntry.experienceLevel = pet.level;
            stableEntry.petFlags = PetStableinfo.inactive;
            stableEntry.petName = pet.name;

            packet.pets.add(stableEntry);
        }

        sendPacket(packet);
    }

    public final void sendListInventory(ObjectGuid vendorGuid) {
        var vendor = getPlayer().getNPCIfCanInteractWith(vendorGuid, NPCFlags.vendor, NPCFlags2.NONE);

        if (vendor == null) {
            Log.outDebug(LogFilter.Network, "WORLD: SendListInventory - {0} not found or you can not interact with him.", vendorGuid.toString());
            getPlayer().sendSellError(SellResult.CantFindVendor, null, ObjectGuid.Empty);

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        // Stop the npc if moving
        var pause = vendor.getMovementTemplate().getInteractionPauseTimer();

        if (pause != 0) {
            vendor.pauseMovement(pause);
        }

        vendor.setHomePosition(vendor.getLocation());

        var vendorItems = vendor.getVendorItems();
        var rawItemCount = vendorItems != null ? vendorItems.getItemCount() : 0;

        VendorInventory packet = new VendorInventory();
        packet.vendor = vendor.getGUID();

        var discountMod = getPlayer().getReputationPriceDiscount(vendor);
        byte count = 0;

        for (int slot = 0; slot < rawItemCount; ++slot) {
            var vendorItem = vendorItems.getItem(slot);

            if (vendorItem == null) {
                continue;
            }

            VendorItemPkt item = new VendorItemPkt();

            var playerCondition = CliDB.PlayerConditionStorage.get(vendorItem.getPlayerConditionId());

            if (playerCondition != null) {
                if (!ConditionManager.isPlayerMeetingCondition(player, playerCondition)) {
                    item.playerConditionFailed = (int) playerCondition.id;
                }
            }

            if (vendorItem.getType() == ItemVendorType.item) {
                var itemTemplate = global.getObjectMgr().getItemTemplate(vendorItem.getItem());

                if (itemTemplate == null) {
                    continue;
                }

                var leftInStock = vendorItem.getMaxcount() == 0 ? -1 : (int) vendor.getVendorItemCurrentCount(vendorItem);

                if (!getPlayer().isGameMaster()) {
                    if (!(boolean) (itemTemplate.getAllowableClass() & getPlayer().getClassMask()) && itemTemplate.getBonding() == ItemBondingType.OnAcquire) {
                        continue;
                    }

                    if ((itemTemplate.hasFlag(ItemFlags2.FactionHorde) && getPlayer().getTeam() == Team.ALLIANCE) || (itemTemplate.hasFlag(ItemFlags2.FactionAlliance) && getPlayer().getTeam() == Team.Horde)) {
                        continue;
                    }

                    if (leftInStock == 0) {
                        continue;
                    }
                }

                if (!global.getConditionMgr().isObjectMeetingVendorItemConditions(vendor.getEntry(), vendorItem.getItem(), player, vendor)) {
                    Log.outDebug(LogFilter.condition, "SendListInventory: conditions not met for creature entry {0} item {1}", vendor.getEntry(), vendorItem.getItem());

                    continue;
                }

                var price = (long) Math.floor(itemTemplate.getBuyPrice() * discountMod);
                price = itemTemplate.getBuyPrice() > 0 ? Math.max(1, price) : price;

                var priceMod = getPlayer().getTotalAuraModifier(AuraType.ModVendorItemsPrices);

                if (priceMod != 0) {
                    price -= MathUtil.CalculatePct(price, priceMod);
                }

                item.muID = (int) slot + 1;
                item.durability = (int) itemTemplate.getMaxDurability();
                item.extendedCostID = (int) vendorItem.getExtendedCost();
                item.type = vendorItem.getType().getValue();
                item.quantity = leftInStock;
                item.stackCount = (int) itemTemplate.getBuyCount();
                item.price = (long) price;
                item.doNotFilterOnVendor = vendorItem.getIgnoreFiltering();
                item.refundable = itemTemplate.hasFlag(ItemFlags.ItemPurchaseRecord) && vendorItem.getExtendedCost() != 0 && itemTemplate.getMaxStackSize() == 1;

                item.item.itemID = vendorItem.getItem();

                if (!vendorItem.getBonusListIDs().isEmpty()) {
                    item.item.itemBonus = new ItemBonuses();
                    item.item.itemBonus.bonusListIDs = vendorItem.getBonusListIDs();
                }

                packet.items.add(item);
            } else if (vendorItem.getType() == ItemVendorType.currency) {
                var currencyTemplate = CliDB.CurrencyTypesStorage.get(vendorItem.getItem());

                if (currencyTemplate == null) {
                    continue;
                }

                if (vendorItem.getExtendedCost() == 0) {
                    continue; // there's no price defined for currencies, only extendedcost is used
                }

                item.muID = (int) slot + 1; // client expects counting to start at 1
                item.extendedCostID = (int) vendorItem.getExtendedCost();
                item.item.itemID = vendorItem.getItem();
                item.type = vendorItem.getType().getValue();
                item.stackCount = (int) vendorItem.getMaxcount();
                item.doNotFilterOnVendor = vendorItem.getIgnoreFiltering();

                packet.items.add(item);
            } else {
                continue;
            }

            if (++count >= SharedConst.MaxVendorItems) {
                break;
            }
        }

        packet.reason = (byte) (count != 0 ? VendorInventoryReason.None : VendorInventoryReason.Empty);

        sendPacket(packet);
    }


    private void handleTabardVendorActivate(Hello packet) {
        var unit = getPlayer().getNPCIfCanInteractWith(packet.unit, NPCFlags.TabardDesigner, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleTabardVendorActivateOpcode - {0} not found or you can not interact with him.", packet.unit.toString());

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        sendTabardVendorActivate(packet.unit);
    }


    private void handleTrainerList(Hello packet) {
        var npc = getPlayer().getNPCIfCanInteractWith(packet.unit, NPCFlags.Trainer, NPCFlags2.NONE);

        if (!npc) {
            Log.outDebug(LogFilter.Network, String.format("WorldSession.SendTrainerList - %1$s not found or you can not interact with him.", packet.unit));

            return;
        }

        var trainerId = global.getObjectMgr().getCreatureDefaultTrainer(npc.getEntry());

        if (trainerId != 0) {
            sendTrainerList(npc, trainerId);
        } else {
            Log.outDebug(LogFilter.Network, String.format("WorldSession.SendTrainerList - Creature id %1$s has no trainer data.", npc.getEntry()));
        }
    }


    private void handleTrainerBuySpell(TrainerBuySpell packet) {
        var npc = player.getNPCIfCanInteractWith(packet.trainerGUID, NPCFlags.Trainer, NPCFlags2.NONE);

        if (npc == null) {
            Log.outDebug(LogFilter.Network, String.format("WORLD: HandleTrainerBuySpell - %1$s not found or you can not interact with him.", packet.trainerGUID));

            return;
        }

        // remove fake death
        if (player.hasUnitState(UnitState.Died)) {
            player.removeAurasByType(AuraType.FeignDeath);
        }

        if (ObjectGuid.opNotEquals(player.getPlayerTalkClass().getInteractionData().getSourceGuid(), packet.trainerGUID)) {
            return;
        }

        if (player.getPlayerTalkClass().getInteractionData().getTrainerId() != packet.trainerID) {
            return;
        }

        // check present spell in trainer spell list
        var trainer = global.getObjectMgr().getTrainer(packet.trainerID);

        if (trainer == null) {
            return;
        }

        trainer.teachSpell(npc, player, packet.spellID);
    }

    private void sendTrainerBuyFailed(ObjectGuid trainerGUID, int spellID, TrainerFailReason trainerFailedReason) {
        TrainerBuyFailed trainerBuyFailed = new TrainerBuyFailed();
        trainerBuyFailed.trainerGUID = trainerGUID;
        trainerBuyFailed.spellID = spellID; // should be same as in packet from client
        trainerBuyFailed.trainerFailedReason = trainerFailedReason; // 1 == "Not enough money for trainer service." 0 == "Trainer service %d unavailable."
        sendPacket(trainerBuyFailed);
    }


    private void handleGossipHello(Hello packet) {
        var unit = getPlayer().getNPCIfCanInteractWith(packet.unit, NPCFlags.Gossip, NPCFlags2.NONE);

        if (unit == null) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleGossipHello - {0} not found or you can not interact with him.", packet.unit.toString());

            return;
        }

        // set faction visible if needed
        var factionTemplateEntry = CliDB.FactionTemplateStorage.get(unit.getFaction());

        if (factionTemplateEntry != null) {
            getPlayer().getReputationMgr().setVisible(factionTemplateEntry);
        }

        getPlayer().removeAurasWithInterruptFlags(SpellAuraInterruptFlags.Interacting);

        // Stop the npc if moving
        var pause = unit.getMovementTemplate().getInteractionPauseTimer();

        if (pause != 0) {
            unit.pauseMovement(pause);
        }

        unit.setHomePosition(unit.getLocation());

        // If spiritguide, no need for gossip menu, just put player into resurrect queue
        if (unit.isSpiritGuide()) {
            var bg = getPlayer().getBattleground();

            if (bg) {
                bg.addPlayerToResurrectQueue(unit.getGUID(), getPlayer().getGUID());
                global.getBattlegroundMgr().sendAreaSpiritHealerQuery(getPlayer(), bg, unit.getGUID());

                return;
            }
        }

        player.getPlayerTalkClass().clearMenus();

        if (!unit.getAI().onGossipHello(player)) {
            getPlayer().prepareGossipMenu(unit, unit.getCreatureTemplate().gossipMenuId, true);
            getPlayer().sendPreparedGossip(unit);
        }
    }


    private void handleGossipSelectOption(GossipSelectOption packet) {
        var gossipMenuItem = player.getPlayerTalkClass().getGossipMenu().getItem(packet.gossipOptionID);

        if (gossipMenuItem == null) {
            return;
        }

        // Prevent cheating on C# scripted menus
        if (ObjectGuid.opNotEquals(getPlayer().getPlayerTalkClass().getInteractionData().getSourceGuid(), packet.gossipUnit)) {
            return;
        }

        Creature unit = null;
        GameObject go = null;

        if (packet.gossipUnit.isCreatureOrVehicle()) {
            unit = getPlayer().getNPCIfCanInteractWith(packet.gossipUnit, NPCFlags.Gossip, NPCFlags2.NONE);

            if (unit == null) {
                Log.outDebug(LogFilter.Network, "WORLD: HandleGossipSelectOption - {0} not found or you can't interact with him.", packet.gossipUnit.toString());

                return;
            }
        } else if (packet.gossipUnit.isGameObject()) {
            go = getPlayer().getGameObjectIfCanInteractWith(packet.gossipUnit);

            if (go == null) {
                Log.outDebug(LogFilter.Network, "WORLD: HandleGossipSelectOption - {0} not found or you can't interact with it.", packet.gossipUnit.toString());

                return;
            }
        } else {
            Log.outDebug(LogFilter.Network, "WORLD: HandleGossipSelectOption - unsupported {0}.", packet.gossipUnit.toString());

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        if ((unit && unit.getScriptId() != unit.lastUsedScriptID) || (go != null && go.getScriptId() != go.lastUsedScriptID)) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleGossipSelectOption - Script reloaded while in use, ignoring and set new scipt id");

            if (unit != null) {
                unit.lastUsedScriptID = unit.getScriptId();
            }

            if (go != null) {
                go.lastUsedScriptID = go.getScriptId();
            }

            getPlayer().getPlayerTalkClass().sendCloseGossip();

            return;
        }

        if (!StringUtil.isEmpty(packet.promotionCode)) {
            if (unit != null) {
                if (!unit.getAI().onGossipSelectCode(player, packet.gossipID, gossipMenuItem.getOrderIndex(), packet.promotionCode)) {
                    getPlayer().onGossipSelect(unit, packet.gossipOptionID, packet.gossipID);
                }
            } else {
                if (!go.getAI().onGossipSelectCode(player, packet.gossipID, gossipMenuItem.getOrderIndex(), packet.promotionCode)) {
                    player.onGossipSelect(go, packet.gossipOptionID, packet.gossipID);
                }
            }
        } else {
            if (unit != null) {
                if (!unit.getAI().onGossipSelect(player, packet.gossipID, gossipMenuItem.getOrderIndex())) {
                    getPlayer().onGossipSelect(unit, packet.gossipOptionID, packet.gossipID);
                }
            } else {
                if (!go.getAI().onGossipSelect(player, packet.gossipID, gossipMenuItem.getOrderIndex())) {
                    getPlayer().onGossipSelect(go, packet.gossipOptionID, packet.gossipID);
                }
            }
        }
    }


    private void handleSpiritHealerActivate(SpiritHealerActivate packet) {
        var unit = getPlayer().getNPCIfCanInteractWith(packet.healer, NPCFlags.SpiritHealer, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleSpiritHealerActivateOpcode - {0} not found or you can not interact with him.", packet.healer.toString());

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        sendSpiritResurrect();
    }

    private void sendSpiritResurrect() {
        getPlayer().resurrectPlayer(0.5f, true);

        getPlayer().durabilityLossAll(0.25f, true);

        // get corpse nearest graveyard
        WorldSafeLocsEntry corpseGrave = null;
        var corpseLocation = getPlayer().getCorpseLocation();

        if (getPlayer().getHasCorpse()) {
            corpseGrave = global.getObjectMgr().getClosestGraveYard(corpseLocation, getPlayer().getTeam(), getPlayer());
        }

        // now can spawn bones
        getPlayer().spawnCorpseBones();

        // teleport to nearest from corpse graveyard, if different from nearest to player ghost
        if (corpseGrave != null) {
            var ghostGrave = global.getObjectMgr().getClosestGraveYard(getPlayer().getLocation(), getPlayer().getTeam(), getPlayer());

            if (corpseGrave != ghostGrave) {
                getPlayer().teleportTo(corpseGrave.loc);
            }
        }
    }


    private void handleBinderActivate(Hello packet) {
        if (!getPlayer().isInWorld() || !getPlayer().isAlive()) {
            return;
        }

        var unit = getPlayer().getNPCIfCanInteractWith(packet.unit, NPCFlags.Innkeeper, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleBinderActivate - {0} not found or you can not interact with him.", packet.unit.toString());

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        sendBindPoint(unit);
    }

    private void sendBindPoint(Creature npc) {
        // prevent set homebind to instances in any case
        if (getPlayer().getMap().isInstanceable()) {
            return;
        }

        int bindspell = 3286;

        // send spell for homebinding (3286)
        npc.castSpell(getPlayer(), bindspell, true);

        getPlayer().getPlayerTalkClass().sendCloseGossip();
    }


    private void handleRequestStabledPets(RequestStabledPets packet) {
        if (!checkStableMaster(packet.stableMaster)) {
            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        // remove mounts this fix bug where getting pet from stable while mounted deletes pet.
        if (getPlayer().isMounted()) {
            getPlayer().removeAurasByType(AuraType.Mounted);
        }

        sendStablePet(packet.stableMaster);
    }

    private void sendPetStableResult(StableResult result) {
        PetStableResult petStableResult = new PetStableResult();
        petStableResult.result = result;
        sendPacket(petStableResult);
    }


    private void handleSetPetSlot(SetPetSlot setPetSlot) {
        if (!checkStableMaster(setPetSlot.stableMaster) || setPetSlot.destSlot >= (byte) PetSaveMode.LastStableSlot.getValue()) {
            sendPetStableResult(StableResult.InternalError);

            return;
        }

        getPlayer().removeAurasWithInterruptFlags(SpellAuraInterruptFlags.Interacting);

        var petStable = getPlayer().getPetStable1();

        if (petStable == null) {
            sendPetStableResult(StableResult.InternalError);

            return;
        }


        (var srcPet, var srcPetSlot) =pet.GetLoadPetInfo(petStable, 0, setPetSlot.petNumber, null);
        var dstPetSlot = PetSaveMode.forValue(setPetSlot.destSlot);
        var dstPet = pet.GetLoadPetInfo(petStable, 0, 0, dstPetSlot).Item1;

        if (srcPet == null || srcPet.type != PetType.Hunter) {
            sendPetStableResult(StableResult.InternalError);

            return;
        }

        if (dstPet != null && dstPet.type != PetType.Hunter) {
            sendPetStableResult(StableResult.InternalError);

            return;
        }

        PetStable.PetInfo src = null;
        PetStable.PetInfo dst = null;
        PetSaveMode newActivePetIndex = null;

        if (SharedConst.IsActivePetSlot(srcPetSlot) && SharedConst.IsActivePetSlot(dstPetSlot)) {
            // active<.active: only swap ActivePets and CurrentPetIndex (do not despawn pets)
            src = petStable.ActivePets[srcPetSlot - PetSaveMode.FirstActiveSlot];
            dst = petStable.ActivePets[dstPetSlot - PetSaveMode.FirstActiveSlot];

            if (petStable.GetCurrentActivePetIndex().intValue() == (int) srcPetSlot) {
                newActivePetIndex = dstPetSlot;
            } else if (petStable.GetCurrentActivePetIndex().intValue() == (int) dstPetSlot.getValue()) {
                newActivePetIndex = srcPetSlot;
            }
        } else if (SharedConst.IsStabledPetSlot(srcPetSlot) && SharedConst.IsStabledPetSlot(dstPetSlot)) {
            // stabled<.stabled: only swap StabledPets
            src = petStable.StabledPets[srcPetSlot - PetSaveMode.FirstStableSlot];
            dst = petStable.StabledPets[dstPetSlot - PetSaveMode.FirstStableSlot];
        } else if (SharedConst.IsActivePetSlot(srcPetSlot) && SharedConst.IsStabledPetSlot(dstPetSlot)) {
            // active<.stabled: swap petStable contents and despawn active pet if it is involved in swap
            if (petStable.CurrentPetIndex.intValue() == (int) srcPetSlot) {
                var oldPet = player.getCurrentPet();

                if (oldPet != null && !oldPet.isAlive()) {
                    sendPetStableResult(StableResult.InternalError);

                    return;
                }

                player.removePet(oldPet, PetSaveMode.NotInSlot);
            }

            if (dstPet != null) {
                var creatureInfo = global.getObjectMgr().getCreatureTemplate(dstPet.creatureId);

                if (creatureInfo == null || !creatureInfo.isTameable(player.getCanTameExoticPets())) {
                    sendPetStableResult(StableResult.CantControlExotic);

                    return;
                }
            }

            src = petStable.ActivePets[srcPetSlot - PetSaveMode.FirstActiveSlot];
            dst = petStable.StabledPets[dstPetSlot - PetSaveMode.FirstStableSlot];
        } else if (SharedConst.IsStabledPetSlot(srcPetSlot) && SharedConst.IsActivePetSlot(dstPetSlot)) {
            // stabled<.active: swap petStable contents and despawn active pet if it is involved in swap
            if (petStable.CurrentPetIndex.intValue() == (int) dstPetSlot.getValue()) {
                var oldPet = player.getCurrentPet();

                if (oldPet != null && !oldPet.isAlive()) {
                    sendPetStableResult(StableResult.InternalError);

                    return;
                }

                player.removePet(oldPet, PetSaveMode.NotInSlot);
            }

            var creatureInfo = global.getObjectMgr().getCreatureTemplate(srcPet.creatureId);

            if (creatureInfo == null || !creatureInfo.isTameable(player.getCanTameExoticPets())) {
                sendPetStableResult(StableResult.CantControlExotic);

                return;
            }

            src = petStable.StabledPets[srcPetSlot - PetSaveMode.FirstStableSlot];
            dst = petStable.ActivePets[dstPetSlot - PetSaveMode.FirstActiveSlot];
        }

        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_PET_SLOT_BY_ID);
        stmt.AddValue(0, (short) dstPetSlot.getValue());
        stmt.AddValue(1, player.getGUID().getCounter());
        stmt.AddValue(2, srcPet.petNumber);
        trans.append(stmt);

        if (dstPet != null) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_PET_SLOT_BY_ID);
            stmt.AddValue(0, (short) srcPetSlot);
            stmt.AddValue(1, player.getGUID().getCounter());
            stmt.AddValue(2, dstPet.petNumber);
            trans.append(stmt);
        }

        addTransactionCallback(DB.characters.AsyncCommitTransaction(trans)).AfterComplete(success ->
        {
            var currentPlayerGuid = player.getGUID();

            if (player && Objects.equals(player.getGUID(), currentPlayerGuid)) {
                if (success) {
                    tangible.RefObject<T> tempRef_src = new tangible.RefObject<T>(src);
                    tangible.RefObject<T> tempRef_dst = new tangible.RefObject<T>(dst);
                    Extensions.Swap(tempRef_src, tempRef_dst);
                    dst = tempRef_dst.refArgValue;
                    src = tempRef_src.refArgValue;

                    if (newActivePetIndex != null) {
                        getPlayer().getPetStable1().SetCurrentActivePetIndex((int) newActivePetIndex);
                    }

                    sendPetStableResult(StableResult.StableSuccess);
                } else {
                    sendPetStableResult(StableResult.InternalError);
                }
            }
        });
    }


    private void handleRepairItem(RepairItem packet) {
        var unit = getPlayer().getNPCIfCanInteractWith(packet.npcGUID, NPCFlags.Repair, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleRepairItemOpcode - {0} not found or you can not interact with him.", packet.npcGUID.toString());

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        // reputation discount
        var discountMod = getPlayer().getReputationPriceDiscount(unit);

        if (!packet.itemGUID.isEmpty()) {
            Log.outDebug(LogFilter.Network, "ITEM: Repair {0}, at {1}", packet.itemGUID.toString(), packet.npcGUID.toString());

            var item = getPlayer().getItemByGuid(packet.itemGUID);

            if (item) {
                getPlayer().durabilityRepair(item.getPos(), true, discountMod);
            }
        } else {
            Log.outDebug(LogFilter.Network, "ITEM: Repair all items at {0}", packet.npcGUID.toString());
            getPlayer().durabilityRepairAll(true, discountMod, packet.useGuildBank);
        }
    }


    private void handleListInventory(Hello packet) {
        if (!getPlayer().isAlive()) {
            return;
        }

        sendListInventory(packet.unit);
    }


    private void handleDismissCritter(DismissCritter packet) {
        Unit pet = ObjectAccessor.GetCreatureOrPetOrVehicle(getPlayer(), packet.critterGUID);

        if (!pet) {
            Log.outDebug(LogFilter.Network, "Critter {0} does not exist - player '{1}' ({2} / account: {3}) attempted to dismiss it (possibly lagged out)", packet.critterGUID.toString(), getPlayer().getName(), getPlayer().getGUID().toString(), getAccountId());

            return;
        }

        if (Objects.equals(getPlayer().getCritterGUID(), pet.getGUID())) {
            if (pet.isCreature() && pet.isSummon()) {
                if (!player.getSummonedBattlePetGUID().isEmpty() && Objects.equals(player.getSummonedBattlePetGUID(), pet.getBattlePetCompanionGUID())) {
                    player.setBattlePetData(null);
                }

                pet.toTempSummon().unSummon();
            }
        }
    }


    private void handlePetAction(PetAction packet) {
        var guid1 = packet.petGUID; //pet guid
        var guid2 = packet.targetGUID; //tag guid

        var spellid = UnitActionBarEntry.UNIT_ACTION_BUTTON_ACTION(packet.action);
        var flag = ActiveStates.forValue(UnitActionBarEntry.UNIT_ACTION_BUTTON_TYPE(packet.action)); //delete = 0x07 castSpell = C1

        // used also for charmed creature
        var pet = global.getObjAccessor().GetUnit(getPlayer(), guid1);

        if (!pet) {
            Log.outError(LogFilter.Network, "HandlePetAction: {0} doesn't exist for {1}", guid1.toString(), getPlayer().getGUID().toString());

            return;
        }

        if (pet != getPlayer().getFirstControlled()) {
            Log.outError(LogFilter.Network, "HandlePetAction: {0} does not belong to {1}", guid1.toString(), getPlayer().getGUID().toString());

            return;
        }

        if (!pet.isAlive()) {
            var spell = (flag == ActiveStates.enabled || flag == ActiveStates.Passive) ? global.getSpellMgr().getSpellInfo(spellid, pet.getMap().getDifficultyID()) : null;

            if (spell == null) {
                return;
            }

            if (!spell.hasAttribute(SpellAttr0.AllowCastWhileDead)) {
                return;
            }
        }

        // @todo allow control charmed player?
        if (pet.isTypeId(TypeId.PLAYER) && !(flag == ActiveStates.command && spellid == (int) CommandStates.attack.getValue())) {
            return;
        }

        if (getPlayer().getControlled().size() == 1) {
            handlePetActionHelper(pet, guid1, spellid, flag, guid2, packet.actionPosition.X, packet.actionPosition.Y, packet.actionPosition.Z);
        } else {
            //If a pet is dismissed, m_Controlled will change
            ArrayList<Unit> controlled = new ArrayList<>();

            for (var unit : getPlayer().getControlled()) {
                if (unit.getEntry() == pet.getEntry() && unit.isAlive()) {
                    controlled.add(unit);
                }
            }

            for (var unit : controlled) {
                handlePetActionHelper(unit, guid1, spellid, flag, guid2, packet.actionPosition.X, packet.actionPosition.Y, packet.actionPosition.Z);
            }
        }
    }


    private void handlePetStopAttack(PetStopAttack packet) {
        Unit pet = ObjectAccessor.GetCreatureOrPetOrVehicle(getPlayer(), packet.petGUID);

        if (!pet) {
            Log.outError(LogFilter.Network, "HandlePetStopAttack: {0} does not exist", packet.petGUID.toString());

            return;
        }

        if (pet != getPlayer().getCurrentPet() && pet != getPlayer().getCharmed()) {
            Log.outError(LogFilter.Network, "HandlePetStopAttack: {0} isn't a pet or charmed creature of player {1}", packet.petGUID.toString(), getPlayer().getName());

            return;
        }

        if (!pet.isAlive()) {
            return;
        }

        pet.attackStop();
    }

    private void handlePetActionHelper(Unit pet, ObjectGuid guid1, int spellid, ActiveStates flag, ObjectGuid guid2, float x, float y, float z) {
        var charmInfo = pet.getCharmInfo();

        if (charmInfo == null) {
            Log.outError(LogFilter.Network, "WorldSession.handlePetAction(petGuid: {0}, tagGuid: {1}, spellId: {2}, flag: {3}): object (GUID: {4} Entry: {5} TypeId: {6}) is considered pet-like but doesn't have a charminfo!", guid1, guid2, spellid, flag, pet.getGUID().toString(), pet.getEntry(), pet.getObjectTypeId());

            return;
        }

        switch (flag) {
            case Command: //0x07
                switch (CommandStates.forValue(spellid)) {
                    case Stay: // flat = 1792  //STAY
                        pet.getMotionMaster().clear(MovementGeneratorPriority.NORMAL);
                        pet.getMotionMaster().moveIdle();
                        charmInfo.setCommandState(CommandStates.Stay);

                        charmInfo.setIsCommandAttack(false);
                        charmInfo.setIsAtStay(true);
                        charmInfo.setIsCommandFollow(false);
                        charmInfo.setIsFollowing(false);
                        charmInfo.setIsReturning(false);
                        charmInfo.saveStayPosition();

                        break;
                    case Follow: // spellid = 1792  //FOLLOW
                        pet.attackStop();
                        pet.interruptNonMeleeSpells(false);
                        pet.getMotionMaster().moveFollow(getPlayer(), SharedConst.PetFollowDist, pet.getFollowAngle());
                        charmInfo.setCommandState(CommandStates.Follow);

                        charmInfo.setIsCommandAttack(false);
                        charmInfo.setIsAtStay(false);
                        charmInfo.setIsReturning(true);
                        charmInfo.setIsCommandFollow(true);
                        charmInfo.setIsFollowing(false);

                        break;
                    case Attack: // spellid = 1792  //ATTACK
                    {
                        // Can't attack if owner is pacified
                        if (getPlayer().hasAuraType(AuraType.ModPacify)) {
                            // @todo Send proper error message to client
                            return;
                        }

                        // only place where pet can be player
                        var TargetUnit = global.getObjAccessor().GetUnit(getPlayer(), guid2);

                        if (!TargetUnit) {
                            return;
                        }

                        var owner = pet.getOwnerUnit();

                        if (owner) {
                            if (!owner.isValidAttackTarget(TargetUnit)) {
                                return;
                            }
                        }

                        // This is true if pet has no target or has target but targets differs.
                        if (pet.getVictim() != TargetUnit || !pet.getCharmInfo().isCommandAttack()) {
                            if (pet.getVictim()) {
                                pet.attackStop();
                            }

                            if (!pet.isTypeId(TypeId.PLAYER) && pet.toCreature().isAIEnabled()) {
                                charmInfo.setIsCommandAttack(true);
                                charmInfo.setIsAtStay(false);
                                charmInfo.setIsFollowing(false);
                                charmInfo.setIsCommandFollow(false);
                                charmInfo.setIsReturning(false);

                                var AI = pet.toCreature().getAI();

                                if (AI instanceof PetAI) {
                                    ((PetAI) AI)._AttackStart(TargetUnit); // force target switch
                                } else {
                                    ai.attackStart(TargetUnit);
                                }

                                //10% chance to play special pet attack talk, else growl
                                if (pet.isPet() && pet.getAsPet().getPetType() == PetType.summon && pet != TargetUnit && RandomUtil.IRand(0, 100) < 10) {
                                    pet.sendPetTalk(PetTalk.attack);
                                } else {
                                    // 90% chance for pet and 100% chance for charmed creature
                                    pet.sendPetAIReaction(guid1);
                                }
                            } else // charmed player
                            {
                                charmInfo.setIsCommandAttack(true);
                                charmInfo.setIsAtStay(false);
                                charmInfo.setIsFollowing(false);
                                charmInfo.setIsCommandFollow(false);
                                charmInfo.setIsReturning(false);

                                pet.attack(TargetUnit, true);
                                pet.sendPetAIReaction(guid1);
                            }
                        }

                        break;
                    }
                    case Abandon: // abandon (hunter pet) or dismiss (summoned pet)
                        if (Objects.equals(pet.getCharmerGUID(), getPlayer().getGUID())) {
                            getPlayer().stopCastingCharm();
                        } else if (Objects.equals(pet.getOwnerGUID(), getPlayer().getGUID())) {
                            if (pet.isPet()) {
                                if (pet.getAsPet().getPetType() == PetType.Hunter) {
                                    getPlayer().removePet(pet.getAsPet(), PetSaveMode.AsDeleted);
                                } else {
                                    getPlayer().removePet(pet.getAsPet(), PetSaveMode.NotInSlot);
                                }
                            } else if (pet.hasUnitTypeMask(UnitTypeMask.minion)) {
                                ((minion) pet).unSummon();
                            }
                        }

                        break;
                    case MoveTo:
                        pet.stopMoving();
                        pet.getMotionMaster().clear();
                        pet.getMotionMaster().movePoint(0, x, y, z);
                        charmInfo.setCommandState(CommandStates.moveTo);

                        charmInfo.setIsCommandAttack(false);
                        charmInfo.setIsAtStay(true);
                        charmInfo.setIsFollowing(false);
                        charmInfo.setIsReturning(false);
                        charmInfo.saveStayPosition();

                        break;
                    default:
                        Log.outError(LogFilter.Network, "WORLD: unknown PET flag Action {0} and spellid {1}.", flag, spellid);

                        break;
                }

                break;
            case Reaction: // 0x6
                switch (ReactStates.forValue(spellid)) {
                    case Passive: //passive
                        pet.attackStop();

						goto case ReactStates.Defensive
                        ;
                    case Defensive: //recovery
                    case Aggressive: //activete
                        if (pet.isTypeId(TypeId.UNIT)) {
                            pet.toCreature().setReactState(ReactStates.forValue(spellid));
                        }

                        break;
                }

                break;
            case Disabled: // 0x81    spell (disabled), ignore
            case Passive: // 0x01
            case Enabled: // 0xC1    spell
            {
                Unit unit_target = null;

                if (!guid2.isEmpty()) {
                    unit_target = global.getObjAccessor().GetUnit(getPlayer(), guid2);
                }

                // do not cast unknown spells
                var spellInfo = global.getSpellMgr().getSpellInfo(spellid, pet.getMap().getDifficultyID());

                if (spellInfo == null) {
                    Log.outError(LogFilter.Network, "WORLD: unknown PET spell id {0}", spellid);

                    return;
                }

                for (var spellEffectInfo : spellInfo.getEffects()) {
                    if (spellEffectInfo.targetA.getTarget() == targets.UnitSrcAreaEnemy || spellEffectInfo.targetA.getTarget() == targets.UnitDestAreaEnemy || spellEffectInfo.targetA.getTarget() == targets.DestDynobjEnemy) {
                        return;
                    }
                }

                // do not cast not learned spells
                if (!pet.hasSpell(spellid) || spellInfo.isPassive()) {
                    return;
                }

                //  Clear the flags as if owner clicked 'attack'. AI will reset them
                //  after AttackStart, even if spell failed
                if (pet.getCharmInfo() != null) {
                    pet.getCharmInfo().setIsAtStay(false);
                    pet.getCharmInfo().setIsCommandAttack(true);
                    pet.getCharmInfo().setIsReturning(false);
                    pet.getCharmInfo().setIsFollowing(false);
                }

                Spell spell = new spell(pet, spellInfo, TriggerCastFlags.NONE);

                var result = spell.checkPetCast(unit_target);

                //auto turn to target unless possessed
                if (result == SpellCastResult.UnitNotInfront && !pet.isPossessed() && !pet.isVehicle()) {
                    var unit_target2 = spell.targets.getUnitTarget();

                    if (unit_target) {
                        if (!pet.hasSpellFocus()) {
                            pet.setInFront(unit_target);
                        }

                        var player = unit_target.toPlayer();

                        if (player) {
                            pet.sendUpdateToPlayer(player);
                        }
                    } else if (unit_target2) {
                        if (!pet.hasSpellFocus()) {
                            pet.setInFront(unit_target2);
                        }

                        var player = unit_target2.toPlayer();

                        if (player) {
                            pet.sendUpdateToPlayer(player);
                        }
                    }

                    var powner = pet.getCharmerOrOwner();

                    if (powner) {
                        var player = powner.toPlayer();

                        if (player) {
                            pet.sendUpdateToPlayer(player);
                        }
                    }

                    result = SpellCastResult.SpellCastOk;
                }

                if (result == SpellCastResult.SpellCastOk) {
                    unit_target = spell.targets.getUnitTarget();

                    //10% chance to play special pet attack talk, else growl
                    //actually this only seems to happen on special spells, fire shield for imp, torment for voidwalker, but it's stupid to check every spell
                    if (pet.isPet() && (pet.getAsPet().getPetType() == PetType.summon) && (pet != unit_target) && (RandomUtil.IRand(0, 100) < 10)) {
                        pet.sendPetTalk(PetTalk.SpecialSpell);
                    } else {
                        pet.sendPetAIReaction(guid1);
                    }

                    if (unit_target && !getPlayer().isFriendlyTo(unit_target) && !pet.isPossessed() && !pet.isVehicle()) {
                        // This is true if pet has no target or has target but targets differs.
                        if (pet.getVictim() != unit_target) {
                            var ai = pet.toCreature().getAI();

                            if (ai != null) {
                                var petAI = (PetAI) ai;

                                if (petAI != null) {
                                    petAI._AttackStart(unit_target); // force victim switch
                                } else {
                                    ai.attackStart(unit_target);
                                }
                            }
                        }
                    }

                    spell.prepare(spell.targets);
                } else {
                    if (pet.isPossessed() || pet.isVehicle()) // @todo: confirm this check
                    {
                        spell.sendCastResult(getPlayer(), spellInfo, spell.spellVisual, spell.castId, result);
                    } else {
                        spell.sendPetCastResult(result);
                    }

                    if (!pet.getSpellHistory().hasCooldown(spellid)) {
                        pet.getSpellHistory().resetCooldown(spellid, true);
                    }

                    spell.finish(result);
                    spell.close();

                    // reset specific flags in case of spell fail. AI will reset other flags
                    if (pet.getCharmInfo() != null) {
                        pet.getCharmInfo().setIsCommandAttack(false);
                    }
                }

                break;
            }
            default:
                Log.outError(LogFilter.Network, "WORLD: unknown PET flag Action {0} and spellid {1}.", flag, spellid);

                break;
        }
    }


    private void handleQueryPetName(QueryPetName packet) {
        sendQueryPetNameResponse(packet.unitGUID);
    }

    private void sendQueryPetNameResponse(ObjectGuid guid) {
        QueryPetNameResponse response = new QueryPetNameResponse();
        response.unitGUID = guid;

        var unit = ObjectAccessor.GetCreatureOrPetOrVehicle(getPlayer(), guid);

        if (unit) {
            response.allow = true;
            response.timestamp = unit.getUnitData().petNameTimestamp;
            response.name = unit.getName();

            var pet = unit.getAsPet();

            if (pet) {
                var names = pet.GetDeclinedNames();

                if (names != null) {
                    response.hasDeclined = true;
                    response.declinedNames = names;
                }
            }
        }

        getPlayer().sendPacket(response);
    }

    private boolean checkStableMaster(ObjectGuid guid) {
        // spell case or GM
        if (Objects.equals(guid, getPlayer().getGUID())) {
            if (!getPlayer().isGameMaster() && !getPlayer().hasAuraType(AuraType.OpenStable)) {
                Log.outDebug(LogFilter.Network, "{0} attempt open stable in cheating way.", guid.toString());

                return false;
            }
        }
        // stable master case
        else {
            if (!getPlayer().getNPCIfCanInteractWith(guid, NPCFlags.stableMaster, NPCFlags2.NONE)) {
                Log.outDebug(LogFilter.Network, "Stablemaster {0} not found or you can't interact with him.", guid.toString());

                return false;
            }
        }

        return true;
    }


    private void handlePetSetAction(PetSetAction packet) {
        var petguid = packet.petGUID;
        var pet = global.getObjAccessor().GetUnit(getPlayer(), petguid);

        if (!pet || pet != getPlayer().getFirstControlled()) {
            Log.outError(LogFilter.Network, "HandlePetSetAction: Unknown {0} or pet owner {1}", petguid.toString(), getPlayer().getGUID().toString());

            return;
        }

        var charmInfo = pet.getCharmInfo();

        if (charmInfo == null) {
            Log.outError(LogFilter.Network, "WorldSession.HandlePetSetAction: {0} is considered pet-like but doesn't have a charminfo!", pet.getGUID().toString());

            return;
        }

        ArrayList<Unit> pets = new ArrayList<>();

        for (var controlled : player.getControlled()) {
            if (controlled.getEntry() == pet.getEntry() && controlled.isAlive()) {
                pets.add(controlled);
            }
        }

        var position = packet.index;
        var actionData = packet.action;

        var spell_id = UnitActionBarEntry.UNIT_ACTION_BUTTON_ACTION(actionData);
        var act_state = ActiveStates.forValue(UnitActionBarEntry.UNIT_ACTION_BUTTON_TYPE(actionData));

        Log.outDebug(LogFilter.Network, "Player {0} has changed pet spell action. Position: {1}, Spell: {2}, State: {3}", getPlayer().getName(), position, spell_id, act_state);

        for (var petControlled : pets) {
            //if it's act for spell (en/disable/cast) and there is a spell given (0 = remove spell) which pet doesn't know, don't add
            if (!((act_state == ActiveStates.enabled || act_state == ActiveStates.disabled || act_state == ActiveStates.Passive) && spell_id != 0 && !petControlled.hasSpell(spell_id))) {
                var spellInfo = global.getSpellMgr().getSpellInfo(spell_id, petControlled.getMap().getDifficultyID());

                if (spellInfo != null) {
                    //sign for autocast
                    if (act_state == ActiveStates.enabled) {
                        if (petControlled.getObjectTypeId() == TypeId.UNIT && petControlled.isPet()) {
                            ((pet) petControlled).ToggleAutocast(spellInfo, true);
                        } else {
                            for (var unit : getPlayer().getControlled()) {
                                if (unit.getEntry() == petControlled.getEntry()) {
                                    unit.getCharmInfo().toggleCreatureAutocast(spellInfo, true);
                                }
                            }
                        }
                    }
                    //sign for no/turn off autocast
                    else if (act_state == ActiveStates.disabled) {
                        if (petControlled.getObjectTypeId() == TypeId.UNIT && petControlled.isPet()) {
                            petControlled.getAsPet().ToggleAutocast(spellInfo, false);
                        } else {
                            for (var unit : getPlayer().getControlled()) {
                                if (unit.getEntry() == petControlled.getEntry()) {
                                    unit.getCharmInfo().toggleCreatureAutocast(spellInfo, false);
                                }
                            }
                        }
                    }
                }

                charmInfo.setActionBar((byte) position, spell_id, act_state);
            }
        }
    }


    private void handlePetRename(PetRename packet) {
        var petguid = packet.renameData.petGUID;
        var isdeclined = packet.renameData.hasDeclinedNames;
        var name = packet.renameData.newName;

        var petStable = player.getPetStable1();
        var pet = ObjectAccessor.getPet(getPlayer(), petguid);

        // check it!
        if (!pet || !pet.isPet() || pet.getAsPet().getPetType() != PetType.Hunter || !pet.hasPetFlag(UnitPetFlags.CanBeRenamed) || ObjectGuid.opNotEquals(pet.getOwnerGUID(), player.getGUID()) || pet.getCharmInfo() == null || petStable == null || petStable.GetCurrentPet() == null || petStable.GetCurrentPet().petNumber != pet.getCharmInfo().getPetNumber()) {
            return;
        }

        var res = ObjectManager.checkPetName(name);

        if (res != PetNameInvalidReason.success) {
            sendPetNameInvalid(res, name, null);

            return;
        }

        if (global.getObjectMgr().isReservedName(name)) {
            sendPetNameInvalid(PetNameInvalidReason.Reserved, name, null);

            return;
        }

        pet.setName(name);
        pet.setGroupUpdateFlag(GroupUpdatePetFlags.name);
        pet.removePetFlag(UnitPetFlags.CanBeRenamed);

        petStable.GetCurrentPet().name = name;
        petStable.GetCurrentPet().WasRenamed = true;

        PreparedStatement stmt;
        SQLTransaction trans = new SQLTransaction();

        if (isdeclined) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_PET_DECLINEDNAME);
            stmt.AddValue(0, pet.getCharmInfo().getPetNumber());
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_PET_DECLINEDNAME);
            stmt.AddValue(0, pet.getCharmInfo().getPetNumber());
            stmt.AddValue(1, getPlayer().getGUID().toString());

            for (byte i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
                stmt.AddValue(i + 1, packet.renameData.declinedNames.name.charAt(i));
            }

            trans.append(stmt);
        }

        stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_CHAR_PET_NAME);
        stmt.AddValue(0, name);
        stmt.AddValue(1, getPlayer().getGUID().toString());
        stmt.AddValue(2, pet.getCharmInfo().getPetNumber());
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);

        pet.setPetNameTimestamp((int) gameTime.GetGameTime()); // cast can't be helped
    }


    private void handlePetAbandon(PetAbandon packet) {
        if (!getPlayer().isInWorld()) {
            return;
        }

        // pet/charmed
        var pet = ObjectAccessor.GetCreatureOrPetOrVehicle(getPlayer(), packet.pet);

        if (pet && pet.getAsPet() && pet.getAsPet().getPetType() == PetType.Hunter) {
            player.removePet((pet) pet, PetSaveMode.AsDeleted);
        }
    }


    private void handlePetSpellAutocast(PetSpellAutocast packet) {
        var pet = ObjectAccessor.GetCreatureOrPetOrVehicle(getPlayer(), packet.petGUID);

        if (!pet) {
            Log.outError(LogFilter.Network, "WorldSession.HandlePetSpellAutocast: {0} not found.", packet.petGUID.toString());

            return;
        }

        if (pet != getPlayer().getGuardianPet() && pet != getPlayer().getCharmed()) {
            Log.outError(LogFilter.Network, "WorldSession.HandlePetSpellAutocast: {0} isn't pet of player {1} ({2}).", packet.petGUID.toString(), getPlayer().getName(), getPlayer().getGUID().toString());

            return;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(packet.spellID, pet.getMap().getDifficultyID());

        if (spellInfo == null) {
            Log.outError(LogFilter.Network, "WorldSession.HandlePetSpellAutocast: Unknown spell id {0} used by {1}.", packet.spellID, packet.petGUID.toString());

            return;
        }

        ArrayList<Unit> pets = new ArrayList<>();

        for (var controlled : player.getControlled()) {
            if (controlled.getEntry() == pet.getEntry() && controlled.isAlive()) {
                pets.add(controlled);
            }
        }

        for (var petControlled : pets) {
            // do not add not learned spells/ passive spells
            if (!petControlled.hasSpell(packet.spellID) || !spellInfo.isAutocastable()) {
                return;
            }

            var charmInfo = petControlled.getCharmInfo();

            if (charmInfo == null) {
                Log.outError(LogFilter.Network, "WorldSession.HandlePetSpellAutocastOpcod: object {0} is considered pet-like but doesn't have a charminfo!", petControlled.getGUID().toString());

                return;
            }

            if (petControlled.isPet()) {
                petControlled.getAsPet().ToggleAutocast(spellInfo, packet.autocastEnabled);
            } else {
                charmInfo.toggleCreatureAutocast(spellInfo, packet.autocastEnabled);
            }

            charmInfo.setSpellAutocast(spellInfo, packet.autocastEnabled);
        }
    }


    private void handlePetCastSpell(PetCastSpell petCastSpell) {
        var caster = global.getObjAccessor().GetUnit(getPlayer(), petCastSpell.petGUID);

        if (!caster) {
            Log.outError(LogFilter.Network, "WorldSession.HandlePetCastSpell: Caster {0} not found.", petCastSpell.petGUID.toString());

            return;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(petCastSpell.cast.spellID, caster.getMap().getDifficultyID());

        if (spellInfo == null) {
            Log.outError(LogFilter.Network, "WorldSession.HandlePetCastSpell: unknown spell id {0} tried to cast by {1}", petCastSpell.cast.spellID, petCastSpell.petGUID.toString());

            return;
        }

        // This opcode is also sent from charmed and possessed units (players and creatures)
        if (caster != getPlayer().getGuardianPet() && caster != getPlayer().getCharmed()) {
            Log.outError(LogFilter.Network, "WorldSession.HandlePetCastSpell: {0} isn't pet of player {1} ({2}).", petCastSpell.petGUID.toString(), getPlayer().getName(), getPlayer().getGUID().toString());

            return;
        }

        SpellCastTargets targets = new SpellCastTargets(caster, petCastSpell.cast);

        var triggerCastFlags = TriggerCastFlags.NONE;

        if (spellInfo.isPassive()) {
            return;
        }

        // cast only learned spells
        if (!caster.hasSpell(spellInfo.getId())) {
            var allow = false;

            // allow casting of spells triggered by clientside periodic trigger auras
            if (caster.hasAuraTypeWithTriggerSpell(AuraType.PeriodicTriggerSpellFromClient, spellInfo.getId())) {
                allow = true;
                triggerCastFlags = TriggerCastFlags.FullMask;
            }

            if (!allow) {
                return;
            }
        }

        Spell spell = new spell(caster, spellInfo, triggerCastFlags);
        spell.fromClient = true;
        spell.spellMisc.data0 = petCastSpell.cast.Misc[0];
        spell.spellMisc.data1 = petCastSpell.cast.Misc[1];
        spell.targets = targets;

        var result = spell.checkPetCast(null);

        if (result == SpellCastResult.SpellCastOk) {
            var creature = caster.toCreature();

            if (creature) {
                var pet = creature.getAsPet();

                if (pet) {
                    // 10% chance to play special pet attack talk, else growl
                    // actually this only seems to happen on special spells, fire shield for imp, torment for voidwalker, but it's stupid to check every spell
                    if (pet.getPetType() == PetType.summon && (RandomUtil.IRand(0, 100) < 10)) {
                        pet.sendPetTalk(PetTalk.SpecialSpell);
                    } else {
                        pet.sendPetAIReaction(petCastSpell.petGUID);
                    }
                }
            }

            SpellPrepare spellPrepare = new SpellPrepare();
            spellPrepare.clientCastID = petCastSpell.cast.castID;
            spellPrepare.serverCastID = spell.castId;
            sendPacket(spellPrepare);

            spell.prepare(targets);
        } else {
            spell.sendPetCastResult(result);

            if (!caster.getSpellHistory().hasCooldown(spellInfo.getId())) {
                caster.getSpellHistory().resetCooldown(spellInfo.getId(), true);
            }

            spell.finish(result);
            spell.close();
        }
    }

    private void sendPetNameInvalid(PetNameInvalidReason error, String name, DeclinedName declinedName) {
        PetNameInvalid petNameInvalid = new PetNameInvalid();
        petNameInvalid.result = error;
        petNameInvalid.renameData.newName = name;

        for (var i = 0; i < SharedConst.MaxDeclinedNameCases; i++) {
            petNameInvalid.renameData.declinedNames.name.charAt(i) = declinedName.name.charAt(i);
        }

        sendPacket(petNameInvalid);
    }


    private void handleRequestPetInfo(RequestPetInfo requestPetInfo) {
        // Handle the packet CMSG_REQUEST_PET_INFO - sent when player does ingame /reload command

        // Packet sent when player has a pet
        if (player.getCurrentPet()) {
            player.petSpellInitialize();
        } else {
            var charm = player.getCharmed();

            if (charm != null) {
                // Packet sent when player has a possessed unit
                if (charm.hasUnitState(UnitState.Possessed)) {
                    player.possessSpellInitialize();
                }
                // Packet sent when player controlling a vehicle
                else if (charm.hasUnitFlag(UnitFlag.PlayerControlled) && charm.hasUnitFlag(UnitFlag.Possessed)) {
                    player.vehicleSpellInitialize();
                }
                // Packet sent when player has a charmed unit
                else {
                    player.charmSpellInitialize();
                }
            }
        }
    }

    public final void sendPetitionQuery(ObjectGuid petitionGuid) {
        QueryPetitionResponse responsePacket = new QueryPetitionResponse();
        responsePacket.petitionID = (int) petitionGuid.getCounter(); // petitionID (in Trinity always same as GUID_LOPART(petition guid))

        var petition = global.getPetitionMgr().getPetition(petitionGuid);

        if (petition == null) {
            responsePacket.allow = false;
            sendPacket(responsePacket);
            Log.outDebug(LogFilter.Network, String.format("CMSG_PETITION_Select failed for petition (%1$s)", petitionGuid));

            return;
        }

        var reqSignatures = WorldConfig.getUIntValue(WorldCfg.MinPetitionSigns);

        PetitionInfo petitionInfo = new PetitionInfo();
        petitionInfo.petitionID = (int) petitionGuid.getCounter();
        petitionInfo.petitioner = petition.ownerGuid;
        petitionInfo.minSignatures = reqSignatures;
        petitionInfo.maxSignatures = reqSignatures;
        petitionInfo.title = petition.petitionName;

        responsePacket.allow = true;
        responsePacket.info = petitionInfo;

        sendPacket(responsePacket);
    }

    public final void sendPetitionShowList(ObjectGuid guid) {
        var creature = getPlayer().getNPCIfCanInteractWith(guid, NPCFlags.petitioner, NPCFlags2.NONE);

        if (!creature) {
            Log.outDebug(LogFilter.Network, "WORLD: HandlePetitionShowListOpcode - {0} not found or you can't interact with him.", guid.toString());

            return;
        }

        WorldPacket data = new WorldPacket(ServerOpcode.PetitionShowList);
        data.writeGuid(guid); // npc guid

        ServerPetitionShowList packet = new ServerPetitionShowList();
        packet.unit = guid;
        packet.price = WorldConfig.getUIntValue(WorldCfg.CharterCostGuild);
        sendPacket(packet);
    }


    private void handlePetitionBuy(PetitionBuy packet) {
        // prevent cheating
        var creature = getPlayer().getNPCIfCanInteractWith(packet.unit, NPCFlags.petitioner, NPCFlags2.NONE);

        if (!creature) {
            Log.outDebug(LogFilter.Network, "WORLD: HandlePetitionBuyOpcode - {0} not found or you can't interact with him.", packet.unit.toString());

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        var charterItemID = GuildConst.CharterItemId;
        var cost = WorldConfig.getIntValue(WorldCfg.CharterCostGuild);

        // do not let if already in guild.
        if (getPlayer().getGuildId() != 0) {
            return;
        }

        if (global.getGuildMgr().getGuildByName(packet.title)) {
            guild.sendCommandResult(this, GuildCommandType.CreateGuild, GuildCommandError.NameExists_S, packet.title);

            return;
        }

        if (global.getObjectMgr().isReservedName(packet.title) || !ObjectManager.isValidCharterName(packet.title)) {
            guild.sendCommandResult(this, GuildCommandType.CreateGuild, GuildCommandError.NameInvalid, packet.title);

            return;
        }

        var pProto = global.getObjectMgr().getItemTemplate(charterItemID);

        if (pProto == null) {
            getPlayer().sendBuyError(BuyResult.CantFindItem, null, charterItemID);

            return;
        }

        if (!getPlayer().hasEnoughMoney(cost)) {
            //player hasn't got enough money
            getPlayer().sendBuyError(BuyResult.NotEnoughtMoney, creature, charterItemID);

            return;
        }

        ArrayList<ItemPosCount> dest = new ArrayList<>();
        var msg = getPlayer().canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, charterItemID, pProto.getBuyCount());

        if (msg != InventoryResult.Ok) {
            getPlayer().sendEquipError(msg, null, null, charterItemID);

            return;
        }

        getPlayer().modifyMoney(-cost);
        var charter = getPlayer().storeNewItem(dest, charterItemID, true);

        if (!charter) {
            return;
        }

        charter.setPetitionId((int) charter.getGUID().getCounter());
        charter.setState(ItemUpdateState.changed, getPlayer());
        getPlayer().sendNewItem(charter, 1, true, false);

        // a petition is invalid, if both the owner and the type matches
        // we checked above, if this player is in an arenateam, so this must be
        // datacorruption
        var petition = global.getPetitionMgr().getPetitionByOwner(player.getGUID());

        if (petition != null) {
            // clear from petition store
            global.getPetitionMgr().removePetition(petition.petitionGuid);
            Log.outDebug(LogFilter.Network, String.format("Invalid petition GUID: %1$s", petition.petitionGuid.getCounter()));
        }

        // fill petition store
        global.getPetitionMgr().addPetition(charter.getGUID(), player.getGUID(), packet.title, false);
    }


    private void handlePetitionShowSignatures(PetitionShowSignatures packet) {
        var petition = global.getPetitionMgr().getPetition(packet.item);

        if (petition == null) {
            Log.outDebug(LogFilter.PlayerItems, String.format("Petition %1$s is not found for player %2$s %3$s", packet.item, getPlayer().getGUID().getCounter(), getPlayer().getName()));

            return;
        }

        // if has guild => error, return;
        if (player.getGuildId() != 0) {
            return;
        }

        sendPetitionSigns(petition, player);
    }

    private void sendPetitionSigns(Petition petition, Player sendTo) {
        ServerPetitionShowSignatures signaturesPacket = new ServerPetitionShowSignatures();
        signaturesPacket.item = petition.petitionGuid;
        signaturesPacket.owner = petition.ownerGuid;
        signaturesPacket.ownerAccountID = ObjectGuid.create(HighGuid.wowAccount, global.getCharacterCacheStorage().getCharacterAccountIdByGuid(petition.ownerGuid));
        signaturesPacket.petitionID = (int) petition.petitionGuid.getCounter();

        for (var signature : petition.signatures) {
            ServerPetitionShowSignatures.PetitionSignature signaturePkt = new ServerPetitionShowSignatures.PetitionSignature();
            signaturePkt.signer = signature.playerGuid;
            signaturePkt.choice = 0;
            signaturesPacket.signatures.add(signaturePkt);
        }

        sendPacket(signaturesPacket);
    }


    private void handleQueryPetition(QueryPetition packet) {
        sendPetitionQuery(packet.itemGUID);
    }


    private void handlePetitionRenameGuild(PetitionRenameGuild packet) {
        var item = getPlayer().getItemByGuid(packet.petitionGuid);

        if (!item) {
            return;
        }

        var petition = global.getPetitionMgr().getPetition(packet.petitionGuid);

        if (petition == null) {
            Log.outDebug(LogFilter.Network, String.format("CMSG_PETITION_QUERY failed for petition %1$s", packet.petitionGuid));

            return;
        }

        if (global.getGuildMgr().getGuildByName(packet.newGuildName)) {
            guild.sendCommandResult(this, GuildCommandType.CreateGuild, GuildCommandError.NameExists_S, packet.newGuildName);

            return;
        }

        if (global.getObjectMgr().isReservedName(packet.newGuildName) || !ObjectManager.isValidCharterName(packet.newGuildName)) {
            guild.sendCommandResult(this, GuildCommandType.CreateGuild, GuildCommandError.NameInvalid, packet.newGuildName);

            return;
        }

        // update petition storage
        petition.updateName(packet.newGuildName);

        PetitionRenameGuildResponse renameResponse = new PetitionRenameGuildResponse();
        renameResponse.petitionGuid = packet.petitionGuid;
        renameResponse.newGuildName = packet.newGuildName;
        sendPacket(renameResponse);
    }


    private void handleSignPetition(SignPetition packet) {
        var petition = global.getPetitionMgr().getPetition(packet.petitionGUID);

        if (petition == null) {
            Log.outError(LogFilter.Network, String.format("Petition %1$s is not found for player %2$s %3$s", packet.petitionGUID, getPlayer().getGUID(), getPlayer().getName()));

            return;
        }

        var ownerGuid = petition.ownerGuid;
        var signs = petition.signatures.size();

        if (Objects.equals(ownerGuid, getPlayer().getGUID())) {
            return;
        }

        // not let enemies sign guild charter
        if (!WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGuild) && getPlayer().getTeam() != global.getCharacterCacheStorage().getCharacterTeamByGuid(ownerGuid)) {
            guild.sendCommandResult(this, GuildCommandType.CreateGuild, GuildCommandError.NotAllied);

            return;
        }

        if (getPlayer().getGuildId() != 0) {
            guild.sendCommandResult(this, GuildCommandType.InvitePlayer, GuildCommandError.AlreadyInGuild_S, getPlayer().getName());

            return;
        }

        if (getPlayer().getGuildIdInvited() != 0) {
            guild.sendCommandResult(this, GuildCommandType.InvitePlayer, GuildCommandError.AlreadyInvitedToGuild_S, getPlayer().getName());

            return;
        }

        if (++signs > 10) // client signs maximum
        {
            return;
        }

        // Client doesn't allow to sign petition two times by one character, but not check sign by another character from same account
        // not allow sign another player from already sign player account

        PetitionSignResults signResult = new PetitionSignResults();
        signResult.player = getPlayer().getGUID();
        signResult.item = packet.petitionGUID;

        var isSigned = petition.isPetitionSignedByAccount(getAccountId());

        if (isSigned) {
            signResult.error = PetitionSigns.AlreadySigned;

            // close at signer side
            sendPacket(signResult);

            // update for owner if online
            var owner = global.getObjAccessor().findConnectedPlayer(ownerGuid);

            if (owner != null) {
                owner.getSession().sendPacket(signResult);
            }

            return;
        }

        // fill petition store
        petition.addSignature(getAccountId(), player.getGUID(), false);

        Log.outDebug(LogFilter.Network, "PETITION SIGN: {0} by player: {1} ({2} Account: {3})", packet.petitionGUID.toString(), getPlayer().getName(), getPlayer().getGUID().toString(), getAccountId());

        signResult.error = PetitionSigns.Ok;
        sendPacket(signResult);

        // update signs count on charter
        var item = player.getItemByGuid(packet.petitionGUID);

        if (item != null) {
            item.setPetitionNumSignatures((int) signs);
            item.setState(ItemUpdateState.changed, player);
        }

        // update for owner if online
        var owner1 = global.getObjAccessor().findPlayer(ownerGuid);

        if (owner1) {
            owner1.sendPacket(signResult);
        }
    }


    private void handleDeclinePetition(DeclinePetition packet) {
        // Disabled because packet isn't handled by the client in any way
		/*
		Petition petition = sPetitionMgr.getPetition(packet.petitionGUID);
		if (petition == null)
			return;

		// petition owner online
		Player owner = global.ObjAccessor.findConnectedPlayer(petition.ownerGuid);
		if (owner != null)                                               // petition owner online
		{
			PetitionDeclined packet = new PetitionDeclined();
			packet.Decliner = player.getGUID();
			owner.GetSession().sendPacket(packet);
		}
		*/
    }


    private void handleOfferPetition(OfferPetition packet) {
        var player = global.getObjAccessor().findConnectedPlayer(packet.targetPlayer);

        if (!player) {
            return;
        }

        var petition = global.getPetitionMgr().getPetition(packet.itemGUID);

        if (petition == null) {
            return;
        }

        if (!WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGuild) && getPlayer().getTeam() != player.getTeam()) {
            guild.sendCommandResult(this, GuildCommandType.CreateGuild, GuildCommandError.NotAllied);

            return;
        }

        if (player.getGuildId() != 0) {
            guild.sendCommandResult(this, GuildCommandType.InvitePlayer, GuildCommandError.AlreadyInGuild_S, getPlayer().getName());

            return;
        }

        if (player.getGuildIdInvited() != 0) {
            guild.sendCommandResult(this, GuildCommandType.InvitePlayer, GuildCommandError.AlreadyInvitedToGuild_S, getPlayer().getName());

            return;
        }

        sendPetitionSigns(petition, player);
    }


    private void handleTurnInPetition(TurnInPetition packet) {
        // Check if player really has the required petition charter
        var item = getPlayer().getItemByGuid(packet.item);

        if (!item) {
            return;
        }

        var petition = global.getPetitionMgr().getPetition(packet.item);

        if (petition == null) {
            Log.outError(LogFilter.Network, "Player {0} ({1}) tried to turn in petition ({2}) that is not present in the database", getPlayer().getName(), getPlayer().getGUID().toString(), packet.item.toString());

            return;
        }

        var name = petition.petitionName; // we need a copy, Guild::AddMember invalidates petition

        // Only the petition owner can turn in the petition
        if (ObjectGuid.opNotEquals(getPlayer().getGUID(), petition.ownerGuid)) {
            return;
        }

        TurnInPetitionResult resultPacket = new TurnInPetitionResult();

        // Check if player is already in a guild
        if (getPlayer().getGuildId() != 0) {
            resultPacket.result = PetitionTurns.AlreadyInGuild;
            sendPacket(resultPacket);

            return;
        }

        // Check if guild name is already taken
        if (global.getGuildMgr().getGuildByName(name)) {
            guild.sendCommandResult(this, GuildCommandType.CreateGuild, GuildCommandError.NameExists_S, name);

            return;
        }

        var signatures = petition.signatures; // we need a copy, Guild::AddMember invalidates petition
        var requiredSignatures = WorldConfig.getUIntValue(WorldCfg.MinPetitionSigns);

        // Notify player if signatures are missing
        if (signatures.size() < requiredSignatures) {
            resultPacket.result = PetitionTurns.NeedMoreSignatures;
            sendPacket(resultPacket);

            return;
        }
        // Proceed with guild/arena team creation

        // Delete charter item
        getPlayer().destroyItem(item.getBagSlot(), item.getSlot(), true);

        // Create guild
        Guild guild = new guild();

        if (!guild.create(getPlayer(), name)) {
            return;
        }

        // Register guild and add guild master
        global.getGuildMgr().addGuild(guild);

        guild.sendCommandResult(this, GuildCommandType.CreateGuild, GuildCommandError.success, name);

        SQLTransaction trans = new SQLTransaction();

        // Add members from signatures
        for (var signature : signatures) {
            guild.addMember(trans, signature.playerGuid);
        }

        DB.characters.CommitTransaction(trans);

        global.getPetitionMgr().removePetition(packet.item);

        // created
        Log.outDebug(LogFilter.Network, String.format("Player %1$s (%2$s) turning in petition %3$s", getPlayer().getName(), getPlayer().getGUID(), packet.item));

        resultPacket.result = PetitionTurns.Ok;
        sendPacket(resultPacket);
    }


    private void handlePetitionShowList(PetitionShowList packet) {
        sendPetitionShowList(packet.petitionUnit);
    }

    public final void buildNameQueryData(ObjectGuid guid, tangible.OutObject<NameCacheLookupResult> lookupData) {
        lookupData.outArgValue = new NameCacheLookupResult();

        var player = global.getObjAccessor().findPlayer(guid);

        lookupData.outArgValue.player = guid;

        lookupData.outArgValue.data = new playerGuidLookupData();

        if (lookupData.outArgValue.data.initialize(guid, player)) {
            lookupData.outArgValue.result = (byte) ResponseCodes.success.getValue();
        } else {
            lookupData.outArgValue.result = (byte) ResponseCodes.Failure.getValue(); // name unknown
        }
    }


    private void handleQueryPlayerNames(QueryPlayerNames queryPlayerName) {
        QueryPlayerNamesResponse response = new QueryPlayerNamesResponse();

        for (var guid : queryPlayerName.players) {
            NameCacheLookupResult nameCacheLookupResult = new NameCacheLookupResult();
            tangible.OutObject<NameCacheLookupResult> tempOut_nameCacheLookupResult = new tangible.OutObject<NameCacheLookupResult>();
            buildNameQueryData(guid, tempOut_nameCacheLookupResult);
            nameCacheLookupResult = tempOut_nameCacheLookupResult.outArgValue;
            response.players.add(nameCacheLookupResult);
        }

        sendPacket(response);
    }


    private void handleQueryTime(QueryTime packet) {
        sendQueryTimeResponse();
    }

    private void sendQueryTimeResponse() {
        QueryTimeResponse queryTimeResponse = new QueryTimeResponse();
        queryTimeResponse.currentTime = gameTime.GetGameTime();
        sendPacket(queryTimeResponse);
    }


    private void handleGameObjectQuery(QueryGameObject packet) {
        var info = global.getObjectMgr().getGameObjectTemplate(packet.gameObjectID);

        if (info != null) {
            if (!WorldConfig.getBoolValue(WorldCfg.CacheDataQueries)) {
                info.initializeQueryData();
            }

            var queryGameObjectResponse = info.queryData;

            var loc = getSessionDbLocaleIndex();

            if (loc != locale.enUS) {
                var gameObjectLocale = global.getObjectMgr().getGameObjectLocale(queryGameObjectResponse.gameObjectID);

                if (gameObjectLocale != null) {
                    ObjectManager.getLocaleString(gameObjectLocale.name, loc, queryGameObjectResponse.stats.name.charAt(0));
                    tangible.RefObject<String> tempRef_CastBarCaption = new tangible.RefObject<String>(queryGameObjectResponse.stats.castBarCaption);
                    ObjectManager.getLocaleString(gameObjectLocale.castBarCaption, loc, tempRef_CastBarCaption);
                    queryGameObjectResponse.stats.castBarCaption = tempRef_CastBarCaption.refArgValue;
                    tangible.RefObject<String> tempRef_UnkString = new tangible.RefObject<String>(queryGameObjectResponse.stats.unkString);
                    ObjectManager.getLocaleString(gameObjectLocale.unk1, loc, tempRef_UnkString);
                    queryGameObjectResponse.stats.unkString = tempRef_UnkString.refArgValue;
                }
            }

            sendPacket(queryGameObjectResponse);
        } else {
            Log.outDebug(LogFilter.Network, String.format("WORLD: CMSG_GAMEOBJECT_QUERY - Missing gameobject info for (ENTRY: %1$s)", packet.gameObjectID));

            QueryGameObjectResponse response = new QueryGameObjectResponse();
            response.gameObjectID = packet.gameObjectID;
            response.guid = packet.guid;
            sendPacket(response);
        }
    }


    private void handleCreatureQuery(QueryCreature packet) {
        var ci = global.getObjectMgr().getCreatureTemplate(packet.creatureID);

        if (ci != null) {
            if (!WorldConfig.getBoolValue(WorldCfg.CacheDataQueries)) {
                ci.initializeQueryData();
            }

            var queryCreatureResponse = ci.queryData;

            var loc = getSessionDbLocaleIndex();

            if (loc != locale.enUS) {
                var creatureLocale = global.getObjectMgr().getCreatureLocale(ci.entry);

                if (creatureLocale != null) {
                    var name = queryCreatureResponse.stats.name.charAt(0);
                    var nameAlt = queryCreatureResponse.stats.nameAlt.get(0);

                    tangible.RefObject<String> tempRef_name = new tangible.RefObject<String>(name);
                    ObjectManager.getLocaleString(creatureLocale.name, loc, tempRef_name);
                    name = tempRef_name.refArgValue;
                    tangible.RefObject<String> tempRef_nameAlt = new tangible.RefObject<String>(nameAlt);
                    ObjectManager.getLocaleString(creatureLocale.nameAlt, loc, tempRef_nameAlt);
                    nameAlt = tempRef_nameAlt.refArgValue;
                    tangible.RefObject<String> tempRef_Title = new tangible.RefObject<String>(queryCreatureResponse.stats.title);
                    ObjectManager.getLocaleString(creatureLocale.title, loc, tempRef_Title);
                    queryCreatureResponse.stats.title = tempRef_Title.refArgValue;
                    tangible.RefObject<String> tempRef_TitleAlt = new tangible.RefObject<String>(queryCreatureResponse.stats.titleAlt);
                    ObjectManager.getLocaleString(creatureLocale.titleAlt, loc, tempRef_TitleAlt);
                    queryCreatureResponse.stats.titleAlt = tempRef_TitleAlt.refArgValue;

                    queryCreatureResponse.stats.name.charAt(0) = name;
                    queryCreatureResponse.stats.nameAlt.set(0, nameAlt);
                }
            }

            sendPacket(queryCreatureResponse);
        } else {
            Log.outDebug(LogFilter.Network, String.format("WORLD: CMSG_QUERY_CREATURE - NO CREATURE INFO! (ENTRY: %1$s)", packet.creatureID));

            QueryCreatureResponse response = new QueryCreatureResponse();
            response.creatureID = packet.creatureID;
            sendPacket(response);
        }
    }


    private void handleNpcTextQuery(QueryNPCText packet) {
        var npcText = global.getObjectMgr().getNpcText(packet.textID);

        QueryNPCTextResponse response = new QueryNPCTextResponse();
        response.textID = packet.textID;

        if (npcText != null) {
            for (byte i = 0; i < SharedConst.MaxNpcTextOptions; ++i) {
                response.Probabilities[i] = npcText.getData()[i].probability;
                response.BroadcastTextID[i] = npcText.getData()[i].broadcastTextID;

                if (!response.allow && npcText.getData()[i].broadcastTextID != 0) {
                    response.allow = true;
                }
            }
        }

        if (!response.allow) {
            Logs.SQL.error("HandleNpcTextQuery: no BroadcastTextID found for text {0} in `npc_text table`", packet.textID);
        }

        sendPacket(response);
    }


    private void handleQueryPageText(QueryPageText packet) {
        QueryPageTextResponse response = new QueryPageTextResponse();
        response.pageTextID = packet.pageTextID;

        var pageID = packet.pageTextID;

        while (pageID != 0) {
            var pageText = global.getObjectMgr().getPageText(pageID);

            if (pageText == null) {
                break;
            }

            QueryPageTextResponse.PageTextInfo page = new QueryPageTextResponse.PageTextInfo();
            page.id = pageID;
            page.nextPageID = pageText.nextPageID;
            page.text = pageText.text;
            page.playerConditionID = pageText.playerConditionID;
            page.flags = pageText.flags;

            var locale = getSessionDbLocaleIndex();

            if (locale != locale.enUS) {
                var pageLocale = global.getObjectMgr().getPageTextLocale(pageID);

                if (pageLocale != null) {
                    tangible.RefObject<String> tempRef_Text = new tangible.RefObject<String>(page.text);
                    ObjectManager.getLocaleString(pageLocale.getText(), locale, tempRef_Text);
                    page.text = tempRef_Text.refArgValue;
                }
            }

            response.pages.add(page);
            pageID = pageText.nextPageID;
        }

        response.allow = !response.pages.isEmpty();
        sendPacket(response);
    }


    private void handleQueryCorpseLocation(QueryCorpseLocationFromClient queryCorpseLocation) {
        CorpseLocation packet = new CorpseLocation();
        var player = global.getObjAccessor().findConnectedPlayer(queryCorpseLocation.player);

        if (!player || !player.getHasCorpse() || !player.isInSameRaidWith(player)) {
            packet.valid = false; // corpse not found
            packet.player = queryCorpseLocation.player;
            sendPacket(packet);

            return;
        }

        var corpseLocation = player.getCorpseLocation();
        var corpseMapID = corpseLocation.getMapId();
        var mapID = corpseLocation.getMapId();
        var x = corpseLocation.getX();
        var y = corpseLocation.getY();
        var z = corpseLocation.getZ();

        // if corpse at different map
        if (mapID != player.getLocation().getMapId()) {
            // search entrance map for proper show entrance
            var corpseMapEntry = CliDB.MapStorage.get(mapID);

            if (corpseMapEntry != null) {
                if (corpseMapEntry.IsDungeon() && corpseMapEntry.CorpseMapID >= 0) {
                    // if corpse map have entrance
                    var entranceTerrain = global.getTerrainMgr().loadTerrain((int) corpseMapEntry.CorpseMapID);

                    if (entranceTerrain != null) {
                        mapID = (int) corpseMapEntry.CorpseMapID;
                        x = corpseMapEntry.Corpse.X;
                        y = corpseMapEntry.Corpse.Y;
                        z = entranceTerrain.getStaticHeight(player.getPhaseShift(), mapID, x, y, MapDefine.MAX_HEIGHT);
                    }
                }
            }
        }

        packet.valid = true;
        packet.player = queryCorpseLocation.player;
        packet.mapID = (int) corpseMapID;
        packet.actualMapID = (int) mapID;
        packet.position = new Vector3(x, y, z);
        packet.transport = ObjectGuid.Empty;
        sendPacket(packet);
    }


    private void handleQueryCorpseTransport(QueryCorpseTransport queryCorpseTransport) {
        CorpseTransportQuery response = new CorpseTransportQuery();
        response.player = queryCorpseTransport.player;

        var player = global.getObjAccessor().findConnectedPlayer(queryCorpseTransport.player);

        if (player) {
            var corpse = player.getCorpse();

            if (player.isInSameRaidWith(player) && corpse && !corpse.getTransGUID().isEmpty() && Objects.equals(corpse.getTransGUID(), queryCorpseTransport.transport)) {
                response.position = new Vector3(corpse.getTransOffsetX(), corpse.getTransOffsetY(), corpse.getTransOffsetZ());
                response.facing = corpse.getTransOffsetO();
            }
        }

        sendPacket(response);
    }


    private void handleQueryQuestCompletionNPCs(QueryQuestCompletionNPCs queryQuestCompletionNPCs) {
        QuestCompletionNPCResponse response = new QuestCompletionNPCResponse();

        for (var questID : queryQuestCompletionNPCs.questCompletionNPCs) {
            QuestCompletionNPC questCompletionNPC = new QuestCompletionNPC();

            if (global.getObjectMgr().getQuestTemplate(questID) == null) {
                Log.outDebug(LogFilter.Network, "WORLD: Unknown quest {0} in CMSG_QUEST_NPC_QUERY by {1}", questID, getPlayer().getGUID());

                continue;
            }

            questCompletionNPC.questID = questID;

            for (var id : global.getObjectMgr().getCreatureQuestInvolvedRelationReverseBounds(questID)) {
                questCompletionNPC.NPCs.add(id);
            }

            for (var id : global.getObjectMgr().getGOQuestInvolvedRelationReverseBounds(questID)) {
                questCompletionNPC.NPCs.add(id | 0x80000000); // GO mask
            }

            response.questCompletionNPCs.add(questCompletionNPC);
        }

        sendPacket(response);
    }


    private void handleQuestPOIQuery(QuestPOIQuery packet) {
        if (packet.missingQuestCount >= SharedConst.MaxQuestLogSize) {
            return;
        }

        // Read quest ids and add the in a unordered_set so we don't send POIs for the same quest multiple times
        HashSet<Integer> questIds = new HashSet<Integer>();

        for (var i = 0; i < packet.missingQuestCount; ++i) {
            questIds.add(packet.MissingQuestPOIs[i]); // QuestID
        }

        QuestPOIQueryResponse response = new QuestPOIQueryResponse();

        for (var questId : questIds) {
            if (player.findQuestSlot(questId) != SharedConst.MaxQuestLogSize) {
                var poiData = global.getObjectMgr().getQuestPOIData(questId);

                if (poiData != null) {
                    response.questPOIDataStats.add(poiData);
                }
            }
        }

        sendPacket(response);
    }


    private void handleItemTextQuery(ItemTextQuery packet) {
        QueryItemTextResponse queryItemTextResponse = new QueryItemTextResponse();
        queryItemTextResponse.id = packet.id;

        var item = getPlayer().getItemByGuid(packet.id);

        if (item) {
            queryItemTextResponse.valid = true;
            queryItemTextResponse.text = item.getText();
        }

        sendPacket(queryItemTextResponse);
    }


    private void handleQueryRealmName(QueryRealmName queryRealmName) {
        RealmQueryResponse realmQueryResponse = new RealmQueryResponse();
        realmQueryResponse.virtualRealmAddress = queryRealmName.virtualRealmAddress;

        RealmId realmHandle = new RealmId(queryRealmName.virtualRealmAddress);

        tangible.RefObject<String> tempRef_RealmNameActual = new tangible.RefObject<String>(realmQueryResponse.nameInfo.realmNameActual);
        tangible.RefObject<String> tempRef_RealmNameNormalized = new tangible.RefObject<String>(realmQueryResponse.nameInfo.realmNameNormalized);
        if (global.getObjectMgr().getRealmName(realmHandle.index, tempRef_RealmNameActual, tempRef_RealmNameNormalized)) {
            realmQueryResponse.nameInfo.realmNameNormalized = tempRef_RealmNameNormalized.refArgValue;
            realmQueryResponse.nameInfo.realmNameActual = tempRef_RealmNameActual.refArgValue;
            realmQueryResponse.lookupState = (byte) ResponseCodes.success.getValue();
            realmQueryResponse.nameInfo.isInternalRealm = false;
            realmQueryResponse.nameInfo.isLocal = queryRealmName.virtualRealmAddress == global.getWorldMgr().getRealm().id.GetAddress();
        } else {
            realmQueryResponse.nameInfo.realmNameNormalized = tempRef_RealmNameNormalized.refArgValue;
            realmQueryResponse.nameInfo.realmNameActual = tempRef_RealmNameActual.refArgValue;
            realmQueryResponse.lookupState = (byte) ResponseCodes.Failure.getValue();
        }
    }


    private void handleQuestgiverStatusQuery(QuestGiverStatusQuery packet) {
        var questStatus = QuestGiverStatus.NONE;

        var questgiver = global.getObjAccessor().GetObjectByTypeMask(player, packet.questGiverGUID, TypeMask.unit.getValue() | TypeMask.gameObject.getValue());

        if (!questgiver) {
            Log.outInfo(LogFilter.Network, "Error in CMSG_QUESTGIVER_STATUS_QUERY, called for non-existing questgiver {0}", packet.questGiverGUID.toString());

            return;
        }

        switch (questgiver.getTypeId()) {
            case Unit:
                if (!questgiver.toCreature().isHostileTo(getPlayer())) // do not show quest status to enemies
                {
                    questStatus = player.getQuestDialogStatus(questgiver);
                }

                break;
            case GameObject:
                questStatus = player.getQuestDialogStatus(questgiver);

                break;
            default:
                Log.outError(LogFilter.Network, "QuestGiver called for unexpected type {0}", questgiver.getTypeId());

                break;
        }

        //inform client about status of quest
        player.getPlayerTalkClass().sendQuestGiverStatus(questStatus, packet.questGiverGUID);
    }


    private void handleQuestgiverHello(QuestGiverHello packet) {
        var creature = player.getNPCIfCanInteractWith(packet.questGiverGUID, NPCFlags.questGiver, NPCFlags2.NONE);

        if (creature == null) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleQuestgiverHello - {0} not found or you can't interact with him.", packet.questGiverGUID.toString());

            return;
        }

        // remove fake death
        if (player.hasUnitState(UnitState.Died)) {
            player.removeAurasByType(AuraType.FeignDeath);
        }

        // Stop the npc if moving
        var pause = creature.getMovementTemplate().getInteractionPauseTimer();

        if (pause != 0) {
            creature.pauseMovement(pause);
        }

        creature.setHomePosition(creature.getLocation());

        player.getPlayerTalkClass().clearMenus();

        if (creature.getAI().onGossipHello(player)) {
            return;
        }

        player.prepareGossipMenu(creature, creature.getCreatureTemplate().gossipMenuId, true);
        player.sendPreparedGossip(creature);
    }


    private void handleQuestgiverAcceptQuest(QuestGiverAcceptQuest packet) {
        WorldObject obj;

        if (!packet.questGiverGUID.isPlayer()) {
            obj = global.getObjAccessor().GetObjectByTypeMask(player, packet.questGiverGUID, TypeMask.unit.getValue() | TypeMask.gameObject.getValue().getValue() | TypeMask.item.getValue().getValue());
        } else {
            obj = global.getObjAccessor().findPlayer(packet.questGiverGUID);
        }

        var CLOSE_GOSSIP_CLEAR_SHARING_INFO = () ->
        {
            player.getPlayerTalkClass().sendCloseGossip();
            player.clearQuestSharingInfo();
        };

        // no or incorrect quest giver
        if (obj == null) {
            CLOSE_GOSSIP_CLEAR_SHARING_INFO.invoke();

            return;
        }

        var playerQuestObject = obj.toPlayer();

        if (playerQuestObject) {
            if ((player.getPlayerSharingQuest().isEmpty() && ObjectGuid.opNotEquals(player.getPlayerSharingQuest(), packet.questGiverGUID)) || !playerQuestObject.canShareQuest(packet.questID)) {
                CLOSE_GOSSIP_CLEAR_SHARING_INFO.invoke();

                return;
            }

            if (!player.isInSameRaidWith(playerQuestObject)) {
                CLOSE_GOSSIP_CLEAR_SHARING_INFO.invoke();

                return;
            }
        } else {
            if (!obj.hasQuest(packet.questID)) {
                CLOSE_GOSSIP_CLEAR_SHARING_INFO.invoke();

                return;
            }
        }

        // some kind of WPE protection
        if (!player.canInteractWithQuestGiver(obj)) {
            CLOSE_GOSSIP_CLEAR_SHARING_INFO.invoke();

            return;
        }

        var quest = global.getObjectMgr().getQuestTemplate(packet.questID);

        if (quest != null) {
            // prevent cheating
            if (!player.canTakeQuest(quest, true)) {
                CLOSE_GOSSIP_CLEAR_SHARING_INFO.invoke();

                return;
            }

            if (!player.getPlayerSharingQuest().isEmpty()) {
                var player = global.getObjAccessor().findPlayer(player.getPlayerSharingQuest());

                if (player != null) {
                    player.sendPushToPartyResponse(player, QuestPushReason.accepted);
                    player.clearQuestSharingInfo();
                }
            }

            if (player.canAddQuest(quest, true)) {
                player.addQuestAndCheckCompletion(quest, obj);

                if (quest.hasFlag(QuestFlag.PartyAccept)) {
                    var group = player.getGroup();

                    if (group) {
                        for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                            var player = refe.getSource();

                            if (!player || player == player || !player.isInMap(player)) // not self and in same map
                            {
                                continue;
                            }

                            if (player.canTakeQuest(quest, true)) {
                                player.setQuestSharingInfo(player.getGUID(), quest.id);

                                //need confirmation that any gossip window will close
                                player.getPlayerTalkClass().sendCloseGossip();

                                player.sendQuestConfirmAccept(quest, player);
                            }
                        }
                    }
                }

                player.getPlayerTalkClass().sendCloseGossip();

                return;
            }
        }

        CLOSE_GOSSIP_CLEAR_SHARING_INFO.invoke();
    }


    private void handleQuestgiverQueryQuest(QuestGiverQueryQuest packet) {
        // Verify that the guid is valid and is a questgiver or involved in the requested quest
        var obj = global.getObjAccessor().GetObjectByTypeMask(player, packet.questGiverGUID, (TypeMask.unit.getValue() | TypeMask.gameObject.getValue().getValue() | TypeMask.item.getValue().getValue()));

        if (!obj || (!obj.hasQuest(packet.questID) && !obj.hasInvolvedQuest(packet.questID))) {
            player.getPlayerTalkClass().sendCloseGossip();

            return;
        }

        var quest = global.getObjectMgr().getQuestTemplate(packet.questID);

        if (quest != null) {
            if (!player.canTakeQuest(quest, true)) {
                return;
            }

            if (quest.isAutoAccept() && player.canAddQuest(quest, true)) {
                player.addQuestAndCheckCompletion(quest, obj);
            }

            if (quest.isAutoComplete()) {
                player.getPlayerTalkClass().sendQuestGiverRequestItems(quest, obj.getGUID(), player.canCompleteQuest(quest.id), true);
            } else {
                player.getPlayerTalkClass().sendQuestGiverQuestDetails(quest, obj.getGUID(), true, false);
            }
        }
    }


    private void handleQuestQuery(QueryQuestInfo packet) {
        var quest = global.getObjectMgr().getQuestTemplate(packet.questID);

        if (quest != null) {
            player.getPlayerTalkClass().sendQuestQueryResponse(quest);
        } else {
            QueryQuestInfoResponse response = new QueryQuestInfoResponse();
            response.questID = packet.questID;
            sendPacket(response);
        }
    }


    private void handleQuestgiverChooseReward(QuestGiverChooseReward packet) {
        var quest = global.getObjectMgr().getQuestTemplate(packet.questID);

        if (quest == null) {
            return;
        }

        if (packet.choice.item.itemID != 0) {
            switch (packet.choice.lootItemType) {
                case Item:
                    var rewardProto = global.getObjectMgr().getItemTemplate(packet.choice.item.itemID);

                    if (rewardProto == null) {
                        Log.outError(LogFilter.Network, "Error in CMSG_QUESTGIVER_CHOOSE_REWARD: player {0} ({1}) tried to get invalid reward item (Item Entry: {2}) for quest {3} (possible packet-hacking detected)", player.getName(), player.getGUID().toString(), packet.choice.item.itemID, packet.questID);

                        return;
                    }

                    var itemValid = false;

                    for (int i = 0; i < quest.getRewChoiceItemsCount(); ++i) {
                        if (quest.RewardChoiceItemId[i] != 0 && quest.RewardChoiceItemType[i] == lootItemType.item && quest.RewardChoiceItemId[i] == packet.choice.item.itemID) {
                            itemValid = true;

                            break;
                        }
                    }

                    if (!itemValid && quest.packageID != 0) {
                        var questPackageItems = global.getDB2Mgr().GetQuestPackageItems(quest.packageID);

                        if (questPackageItems != null) {
                            for (var questPackageItem : questPackageItems) {
                                if (questPackageItem.itemID != packet.choice.item.itemID) {
                                    continue;
                                }

                                if (player.canSelectQuestPackageItem(questPackageItem)) {
                                    itemValid = true;

                                    break;
                                }
                            }
                        }

                        if (!itemValid) {
                            var questPackageItems1 = global.getDB2Mgr().GetQuestPackageItemsFallback(quest.packageID);

                            if (questPackageItems1 != null) {
                                for (var questPackageItem : questPackageItems1) {
                                    if (questPackageItem.itemID != packet.choice.item.itemID) {
                                        continue;
                                    }

                                    itemValid = true;

                                    break;
                                }
                            }
                        }
                    }

                    if (!itemValid) {
                        Log.outError(LogFilter.Network, "Error in CMSG_QUESTGIVER_CHOOSE_REWARD: player {0} ({1}) tried to get reward item (Item Entry: {2}) wich is not a reward for quest {3} (possible packet-hacking detected)", player.getName(), player.getGUID().toString(), packet.choice.item.itemID, packet.questID);

                        return;
                    }

                    break;
                case Currency:
                    if (!CliDB.CurrencyTypesStorage.HasRecord(packet.choice.item.itemID)) {
                        Log.outError(LogFilter.player, String.format("Error in CMSG_QUESTGIVER_CHOOSE_REWARD: player %1$s (%2$s) tried to get invalid reward currency (Currency ID: %3$s) for quest %4$s (possible packet-hacking detected)", player.getName(), player.getGUID(), packet.choice.item.itemID, packet.questID));

                        return;
                    }

                    var currencyValid = false;

                    for (int i = 0; i < quest.getRewChoiceItemsCount(); ++i) {
                        if (quest.RewardChoiceItemId[i] != 0 && quest.RewardChoiceItemType[i] == lootItemType.currency && quest.RewardChoiceItemId[i] == packet.choice.item.itemID) {
                            currencyValid = true;

                            break;
                        }
                    }

                    if (!currencyValid) {
                        Log.outError(LogFilter.player, String.format("Error in CMSG_QUESTGIVER_CHOOSE_REWARD: player %1$s (%2$s) tried to get reward currency (Currency ID: %3$s) wich is not a reward for quest %4$s (possible packet-hacking detected)", player.getName(), player.getGUID(), packet.choice.item.itemID, packet.questID));

                        return;
                    }

                    break;
            }
        }

        WorldObject obj = getPlayer();

        if (!quest.hasFlag(QuestFlag.AutoComplete)) {
            obj = global.getObjAccessor().GetObjectByTypeMask(player, packet.questGiverGUID, TypeMask.unit.getValue() | TypeMask.gameObject.getValue());

            if (!obj || !obj.hasInvolvedQuest(packet.questID)) {
                return;
            }

            // some kind of WPE protection
            if (!player.canInteractWithQuestGiver(obj)) {
                return;
            }
        }

        if ((!player.canSeeStartQuest(quest) && player.getQuestStatus(packet.questID) == QuestStatus.NONE) || (player.getQuestStatus(packet.questID) != QuestStatus.Complete && !quest.isAutoComplete())) {
            Log.outError(LogFilter.Network, "Error in QuestStatus.Complete: player {0} ({1}) tried to complete quest {2}, but is not allowed to do so (possible packet-hacking or high latency)", player.getName(), player.getGUID().toString(), packet.questID);

            return;
        }

        if (player.canRewardQuest(quest, true)) // first, check if player is allowed to turn the quest in (all objectives completed). If not, we send players to the offer reward screen
        {
            if (player.canRewardQuest(quest, packet.choice.lootItemType, packet.choice.item.itemID, true)) // Then check if player can receive the reward item (if inventory is not full, if player doesn't have too many unique items, and so on). If not, the client will close the gossip window
            {
                var bg = player.getBattleground();

                if (bg != null) {
                    bg.handleQuestComplete(packet.questID, player);
                }

                getPlayer().rewardQuest(quest, packet.choice.lootItemType, packet.choice.item.itemID, obj);
            }
        } else {
            player.getPlayerTalkClass().sendQuestGiverOfferReward(quest, packet.questGiverGUID, true);
        }
    }


    private void handleQuestgiverRequestReward(QuestGiverRequestReward packet) {
        var obj = global.getObjAccessor().GetObjectByTypeMask(player, packet.questGiverGUID, TypeMask.unit.getValue() | TypeMask.gameObject.getValue());

        if (obj == null || !obj.hasInvolvedQuest(packet.questID)) {
            return;
        }

        // some kind of WPE protection
        if (!player.canInteractWithQuestGiver(obj)) {
            return;
        }

        if (player.canCompleteQuest(packet.questID)) {
            player.completeQuest(packet.questID);
        }

        if (player.getQuestStatus(packet.questID) != QuestStatus.Complete) {
            return;
        }

        var quest = global.getObjectMgr().getQuestTemplate(packet.questID);

        if (quest != null) {
            player.getPlayerTalkClass().sendQuestGiverOfferReward(quest, packet.questGiverGUID, true);
        }
    }


    private void handleQuestLogRemoveQuest(QuestLogRemoveQuest packet) {
        if (packet.entry < SharedConst.MaxQuestLogSize) {
            var questId = player.getQuestSlotQuestId(packet.entry);

            if (questId != 0) {
                if (!player.takeQuestSourceItem(questId, true)) {
                    return; // can't un-equip some items, reject quest cancel
                }

                var quest = global.getObjectMgr().getQuestTemplate(questId);
                var oldStatus = player.getQuestStatus(questId);

                if (quest != null) {
                    if (quest.limitTime != 0) {
                        player.removeTimedQuest(questId);
                    }

                    if (quest.hasFlag(QuestFlag.Pvp)) {
                        player.pvpInfo.isHostile = player.pvpInfo.isInHostileArea || player.hasPvPForcingQuest();
                        player.updatePvPState();
                    }
                }

                player.setQuestSlot(packet.entry, 0);
                player.takeQuestSourceItem(questId, true); // remove quest src item from player
                player.abandonQuest(questId); // remove all quest items player received before abandoning quest. note, this does not remove normal drop items that happen to be quest requirements.
                player.removeActiveQuest(questId);
                player.removeCriteriaTimer(CriteriaStartEvent.AcceptQuest, questId);

                Log.outInfo(LogFilter.Network, "Player {0} abandoned quest {1}", player.getGUID().toString(), questId);

                global.getScriptMgr().<IPlayerOnQuestStatusChange>ForEach(p -> p.OnQuestStatusChange(player, questId));

                if (quest != null) {
                    global.getScriptMgr().<IQuestOnQuestStatusChange>RunScript(script -> script.OnQuestStatusChange(player, quest, oldStatus, QuestStatus.NONE), quest.getScriptId());
                }
            }

            player.updateCriteria(CriteriaType.AbandonAnyQuest, 1);
        }
    }


    private void handleQuestConfirmAccept(QuestConfirmAccept packet) {
        var quest = global.getObjectMgr().getQuestTemplate(packet.questID);

        if (quest != null) {
            if (!quest.hasFlag(QuestFlag.PartyAccept)) {
                return;
            }

            var originalPlayer = global.getObjAccessor().findPlayer(player.getPlayerSharingQuest());

            if (originalPlayer == null) {
                return;
            }

            if (!player.isInSameRaidWith(originalPlayer)) {
                return;
            }

            if (!originalPlayer.isActiveQuest(packet.questID)) {
                return;
            }

            if (!player.canTakeQuest(quest, true)) {
                return;
            }

            if (player.canAddQuest(quest, true)) {
                player.addQuestAndCheckCompletion(quest, null); // NULL, this prevent DB script from duplicate running

                if (quest.getSourceSpellID() > 0) {
                    player.castSpell(player, quest.getSourceSpellID(), true);
                }
            }
        }

        player.clearQuestSharingInfo();
    }


    private void handleQuestgiverCompleteQuest(QuestGiverCompleteQuest packet) {
        var autoCompleteMode = packet.fromScript; // 0 - standart complete quest mode with npc, 1 - auto-complete mode

        var quest = global.getObjectMgr().getQuestTemplate(packet.questID);

        if (quest == null) {
            return;
        }

        if (autoCompleteMode && !quest.hasFlag(QuestFlag.AutoComplete)) {
            return;
        }

        WorldObject obj;

        if (autoCompleteMode) {
            obj = getPlayer();
        } else {
            obj = global.getObjAccessor().GetObjectByTypeMask(player, packet.questGiverGUID, TypeMask.unit.getValue() | TypeMask.gameObject.getValue());
        }

        if (!obj) {
            return;
        }

        if (!autoCompleteMode) {
            if (!obj.hasInvolvedQuest(packet.questID)) {
                return;
            }

            // some kind of WPE protection
            if (!player.canInteractWithQuestGiver(obj)) {
                return;
            }
        } else {
            // Do not allow completing quests on other players.
            if (ObjectGuid.opNotEquals(packet.questGiverGUID, player.getGUID())) {
                return;
            }
        }

        if (!player.canSeeStartQuest(quest) && player.getQuestStatus(packet.questID) == QuestStatus.NONE) {
            Log.outError(LogFilter.Network, "Possible hacking attempt: Player {0} ({1}) tried to complete quest [entry: {2}] without being in possession of the quest!", player.getName(), player.getGUID().toString(), packet.questID);

            return;
        }

        if (player.getQuestStatus(packet.questID) != QuestStatus.Complete) {
            if (quest.isRepeatable()) {
                player.getPlayerTalkClass().sendQuestGiverRequestItems(quest, packet.questGiverGUID, player.canCompleteRepeatableQuest(quest), false);
            } else {
                player.getPlayerTalkClass().sendQuestGiverRequestItems(quest, packet.questGiverGUID, player.canRewardQuest(quest, false), false);
            }
        } else {
            if (quest.hasQuestObjectiveType(QuestObjectiveType.item)) // some items required
            {
                player.getPlayerTalkClass().sendQuestGiverRequestItems(quest, packet.questGiverGUID, player.canRewardQuest(quest, false), false);
            } else // no items required
            {
                player.getPlayerTalkClass().sendQuestGiverOfferReward(quest, packet.questGiverGUID, true);
            }
        }
    }


    private void handleQuestgiverCloseQuest(QuestGiverCloseQuest questGiverCloseQuest) {
        if (player.findQuestSlot(questGiverCloseQuest.questID) >= SharedConst.MaxQuestLogSize) {
            return;
        }

        var quest = global.getObjectMgr().getQuestTemplate(questGiverCloseQuest.questID);

        if (quest == null) {
            return;
        }

        global.getScriptMgr().<IQuestOnAckAutoAccept>RunScript(script -> script.OnAcknowledgeAutoAccept(player, quest), quest.getScriptId());
    }


    private void handlePushQuestToParty(PushQuestToParty packet) {
        var quest = global.getObjectMgr().getQuestTemplate(packet.questID);

        if (quest == null) {
            return;
        }

        var sender = getPlayer();

        if (!player.canShareQuest(packet.questID)) {
            sender.sendPushToPartyResponse(sender, QuestPushReason.NotAllowed);

            return;
        }

        // in pool and not currently available (wintergrasp weekly, dalaran weekly) - can't share
        if (global.getQuestPoolMgr().isQuestActive(packet.questID)) {
            sender.sendPushToPartyResponse(sender, QuestPushReason.NotDaily);

            return;
        }

        var group = sender.getGroup();

        if (!group) {
            sender.sendPushToPartyResponse(sender, QuestPushReason.NotInParty);

            return;
        }

        for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
            var receiver = refe.getSource();

            if (!receiver || receiver == sender) {
                continue;
            }

            if (!receiver.getPlayerSharingQuest().isEmpty()) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.Busy);

                continue;
            }

            if (!receiver.isAlive()) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.Dead);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.DeadToRecipient, quest);

                continue;
            }

            switch (receiver.getQuestStatus(packet.questID)) {
                case Rewarded: {
                    sender.sendPushToPartyResponse(receiver, QuestPushReason.AlreadyDone);
                    receiver.sendPushToPartyResponse(sender, QuestPushReason.AlreadyDoneToRecipient, quest);

                    continue;
                }
                case Incomplete:
                case Complete: {
                    sender.sendPushToPartyResponse(receiver, QuestPushReason.OnQuest);
                    receiver.sendPushToPartyResponse(sender, QuestPushReason.OnQuestToRecipient, quest);

                    continue;
                }
                default:
                    break;
            }

            if (!receiver.satisfyQuestLog(false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.LogFull);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.LogFullToRecipient, quest);

                continue;
            }

            if (!receiver.satisfyQuestDay(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.AlreadyDone);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.AlreadyDoneToRecipient, quest);

                continue;
            }

            if (!receiver.satisfyQuestMinLevel(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.LowLevel);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.LowLevelToRecipient, quest);

                continue;
            }

            if (!receiver.satisfyQuestMaxLevel(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.HighLevel);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.HighLevelToRecipient, quest);

                continue;
            }

            if (!receiver.satisfyQuestClass(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.class);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.ClassToRecipient, quest);

                continue;
            }

            if (!receiver.satisfyQuestRace(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.race);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.RaceToRecipient, quest);

                continue;
            }

            if (!receiver.satisfyQuestReputation(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.LowFaction);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.LowFactionToRecipient, quest);

                continue;
            }

            if (!receiver.satisfyQuestDependentQuests(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.Prerequisite);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.PrerequisiteToRecipient, quest);

                continue;
            }

            if (!receiver.satisfyQuestExpansion(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.expansion);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.ExpansionToRecipient, quest);

                continue;
            }

            if (!receiver.canTakeQuest(quest, false)) {
                sender.sendPushToPartyResponse(receiver, QuestPushReason.Invalid);
                receiver.sendPushToPartyResponse(sender, QuestPushReason.InvalidToRecipient, quest);

                continue;
            }

            sender.sendPushToPartyResponse(receiver, QuestPushReason.success);

            if ((quest.isAutoComplete() && quest.isRepeatable() && !quest.isDailyOrWeekly()) || quest.hasFlag(QuestFlag.AutoComplete)) {
                receiver.getPlayerTalkClass().sendQuestGiverRequestItems(quest, sender.getGUID(), receiver.canCompleteRepeatableQuest(quest), true);
            } else {
                receiver.setQuestSharingInfo(sender.getGUID(), quest.id);
                receiver.getPlayerTalkClass().sendQuestGiverQuestDetails(quest, receiver.getGUID(), true, false);

                if (quest.isAutoAccept() && receiver.canAddQuest(quest, true) && receiver.canTakeQuest(quest, true)) {
                    receiver.addQuestAndCheckCompletion(quest, sender);
                    sender.sendPushToPartyResponse(receiver, QuestPushReason.accepted);
                    receiver.clearQuestSharingInfo();
                }
            }
        }
    }


    private void handleQuestPushResult(QuestPushResult packet) {
        if (!player.getPlayerSharingQuest().isEmpty()) {
            if (Objects.equals(player.getPlayerSharingQuest(), packet.senderGUID)) {
                var player = global.getObjAccessor().findPlayer(player.getPlayerSharingQuest());

                if (player) {
                    player.sendPushToPartyResponse(player, packet.result);
                }
            }

            player.clearQuestSharingInfo();
        }
    }


    private void handleQuestgiverStatusMultipleQuery(QuestGiverStatusMultipleQuery packet) {
        player.sendQuestGiverStatusMultiple();
    }


    private void handleQuestgiverStatusTrackedQueryOpcode(QuestGiverStatusTrackedQuery questGiverStatusTrackedQuery) {
        player.sendQuestGiverStatusMultiple(questGiverStatusTrackedQuery.questGiverGUIDs);
    }


    private void handleRequestWorldQuestUpdate(RequestWorldQuestUpdate packet) {
        WorldQuestUpdateResponse response = new WorldQuestUpdateResponse();

        // @todo: 7.x Has to be implemented
        //response.worldQuestUpdates.push_back(WorldPackets::Quest::WorldQuestUpdateInfo(lastUpdate, questID, timer, variableID, value));

        sendPacket(response);
    }


    private void handlePlayerChoiceResponse(ChoiceResponse choiceResponse) {
        if (player.getPlayerTalkClass().getInteractionData().getPlayerChoiceId() != choiceResponse.choiceID) {
            Log.outError(LogFilter.player, String.format("Error in CMSG_CHOICE_RESPONSE: %1$s tried to respond to invalid player choice %2$s (allowed %3$s) (possible packet-hacking detected)", getPlayerInfo(), choiceResponse.choiceID, player.getPlayerTalkClass().getInteractionData().getPlayerChoiceId()));

            return;
        }

        var playerChoice = global.getObjectMgr().getPlayerChoice(choiceResponse.choiceID);

        if (playerChoice == null) {
            return;
        }

        var playerChoiceResponse = playerChoice.getResponseByIdentifier(choiceResponse.responseIdentifier);

        if (playerChoiceResponse == null) {
            Log.outError(LogFilter.player, String.format("Error in CMSG_CHOICE_RESPONSE: %1$s tried to select invalid player choice response %2$s (possible packet-hacking detected)", getPlayerInfo(), choiceResponse.responseIdentifier));

            return;
        }

        global.getScriptMgr().<IPlayerOnPlayerChoiceResponse>ForEach(p -> p.OnPlayerChoiceResponse(player, (int) choiceResponse.choiceID, (int) choiceResponse.responseIdentifier));

        if (playerChoiceResponse.reward != null) {
            var reward = playerChoiceResponse.reward;

            if (reward.titleId != 0) {
                player.setTitle(CliDB.CharTitlesStorage.get(reward.titleId), false);
            }

            if (reward.PackageId != 0) {
                player.rewardQuestPackage((int) reward.PackageId);
            }

            if (reward.SkillLineId != 0 && player.hasSkill(SkillType.forValue(reward.SkillLineId))) {
                player.updateSkillPro((int) reward.SkillLineId, 1000, reward.skillPointCount);
            }

            if (reward.honorPointCount != 0) {
                player.addHonorXp(reward.honorPointCount);
            }

            if (reward.money != 0) {
                player.modifyMoney((long) reward.money, false);
            }

            if (reward.xp != 0) {
                player.giveXP(reward.xp, null, 0.0f);
            }

            for (var item : reward.items) {
                ArrayList<ItemPosCount> dest = new ArrayList<>();

                if (player.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, item.id, (int) item.quantity) == InventoryResult.Ok) {
                    var newItem = player.storeNewItem(dest, item.id, true, ItemEnchantmentManager.generateItemRandomBonusListId(item.id), null, itemContext.QuestReward, item.bonusListIDs);
                    player.sendNewItem(newItem, (int) item.quantity, true, false);
                }
            }

            for (var currency : reward.currency) {
                player.modifyCurrency(currency.id, currency.quantity);
            }

            for (var faction : reward.faction) {
                player.getReputationMgr().modifyReputation(CliDB.FactionStorage.get(faction.id), faction.quantity);
            }
        }
    }


    private void handleQueryQuestItemUsability(QueryQuestItemUsability request) {
        //foreach (var itemGuid in request.itemGUIDs)
        //{
        //    var item = player.getItemByGuid(itemGuid);
        //    player.hasQuestForItem(item.template.id);
        //}
    }


    private void handleUiMapQuestLinesRequest(UiMapQuestLinesRequest request) {
        var response = new UiMapQuestLinesResponse();
        response.uiMapID = request.uiMapID;

        ArrayList<TValue> questPOIBlobEntries;
        tangible.OutObject<ArrayList<TValue>> tempOut_questPOIBlobEntries = new tangible.OutObject<ArrayList<TValue>>();
        if (DB2Manager.getInstance().QuestPOIBlobEntriesByMapId.TryGetValue(request.uiMapID, tempOut_questPOIBlobEntries)) {
            questPOIBlobEntries = tempOut_questPOIBlobEntries.outArgValue;
            for (var questPOIBlob : questPOIBlobEntries) {
                ArrayList<TValue> lineXQuestRecords;
                tangible.OutObject<ArrayList<TValue>> tempOut_lineXQuestRecords = new tangible.OutObject<ArrayList<TValue>>();
                if (getPlayer().meetPlayerCondition(questPOIBlob.playerConditionID) && DB2Manager.getInstance().QuestLinesByQuest.TryGetValue((int) questPOIBlob.questID, tempOut_lineXQuestRecords)) {
                    lineXQuestRecords = tempOut_lineXQuestRecords.outArgValue;
                    for (var lineXRecord : lineXQuestRecords) {
                        ArrayList<QuestLineXQuestRecord> questLineQuests;
                        tangible.OutObject<ArrayList<QuestLineXQuestRecord>> tempOut_questLineQuests = new tangible.OutObject<ArrayList<QuestLineXQuestRecord>>();
                        if (DB2Manager.getInstance().TryGetQuestsForQuestLine(lineXRecord.questID, tempOut_questLineQuests)) {
                            questLineQuests = tempOut_questLineQuests.outArgValue;
                            for (var questLineQuest : questLineQuests) {
                                Quest quest;
                                tangible.OutObject<Quest> tempOut_quest = new tangible.OutObject<Quest>();
                                T contentTune;
                                tangible.OutObject<T> tempOut_contentTune = new tangible.OutObject<T>();
                                if (global.getObjectMgr().tryGetQuestTemplate(questLineQuest.questID, tempOut_quest) && getPlayer().canTakeQuest(quest, false) && CliDB.ContentTuningStorage.TryGetValue(quest.contentTuningId, tempOut_contentTune) && getPlayer().getLevel() >= contentTune.minLevel) {
                                    contentTune = tempOut_contentTune.outArgValue;
                                    quest = tempOut_quest.outArgValue;
                                    response.questLineXQuestIDs.add(questLineQuest.questID);

                                    break;
                                } else {
                                    contentTune = tempOut_contentTune.outArgValue;
                                    quest = tempOut_quest.outArgValue;
                                }
                            }
                        } else {
                            questLineQuests = tempOut_questLineQuests.outArgValue;
                        }
                    }
                } else {
                    lineXQuestRecords = tempOut_lineXQuestRecords.outArgValue;
                }
            }
        } else {
            questPOIBlobEntries = tempOut_questPOIBlobEntries.outArgValue;
        }


        sendPacket(response);
    }


    private void handleQueryScenarioPOI(QueryScenarioPOI queryScenarioPOI) {
        ScenarioPOIs response = new scenarioPOIs();

        // Read criteria tree ids and add the in a unordered_set so we don't send POIs for the same criteria tree multiple times
        ArrayList<Integer> criteriaTreeIds = new ArrayList<>();

        for (var i = 0; i < queryScenarioPOI.missingScenarioPOIs.size(); ++i) {
            criteriaTreeIds.add(queryScenarioPOI.missingScenarioPOIs.get(i)); // CriteriaTreeID
        }

        for (var criteriaTreeId : criteriaTreeIds) {
            var poiVector = global.getScenarioMgr().getScenarioPOIs((int) criteriaTreeId);

            if (poiVector != null) {
                ScenarioPOIData scenarioPOIData = new ScenarioPOIData();
                scenarioPOIData.criteriaTreeID = criteriaTreeId;
                scenarioPOIData.scenarioPOIs = poiVector;
                response.scenarioPOIDataStats.add(scenarioPOIData);
            }
        }

        sendPacket(response);
    }


    private void handleSceneTriggerEvent(SceneTriggerEvent sceneTriggerEvent) {
        Log.outDebug(LogFilter.Scenes, "HandleSceneTriggerEvent: SceneInstanceID: {0} Event: {1}", sceneTriggerEvent.sceneInstanceID, sceneTriggerEvent._Event);

        getPlayer().getSceneMgr().onSceneTrigger(sceneTriggerEvent.sceneInstanceID, sceneTriggerEvent._Event);
    }


    private void handleScenePlaybackComplete(ScenePlaybackComplete scenePlaybackComplete) {
        Log.outDebug(LogFilter.Scenes, "HandleScenePlaybackComplete: SceneInstanceID: {0}", scenePlaybackComplete.sceneInstanceID);

        getPlayer().getSceneMgr().onSceneComplete(scenePlaybackComplete.sceneInstanceID);
    }


    private void handleScenePlaybackCanceled(ScenePlaybackCanceled scenePlaybackCanceled) {
        Log.outDebug(LogFilter.Scenes, "HandleScenePlaybackCanceled: SceneInstanceID: {0}", scenePlaybackCanceled.sceneInstanceID);

        getPlayer().getSceneMgr().onSceneCancel(scenePlaybackCanceled.sceneInstanceID);
    }


    private void handleLearnTalents(LearnTalents packet) {
        LearnTalentFailed learnTalentFailed = new LearnTalentFailed();
        var anythingLearned = false;

        for (int talentId : packet.talents) {
            tangible.RefObject<Integer> tempRef_SpellID = new tangible.RefObject<Integer>(learnTalentFailed.spellID);
            var result = player.learnTalent(talentId, tempRef_SpellID);
            learnTalentFailed.spellID = tempRef_SpellID.refArgValue;

            if (result != 0) {
                if (learnTalentFailed.reason == 0) {
                    learnTalentFailed.reason = (int) result.getValue();
                }

                learnTalentFailed.talents.add((short) talentId);
            } else {
                anythingLearned = true;
            }
        }

        if (learnTalentFailed.reason != 0) {
            sendPacket(learnTalentFailed);
        }

        if (anythingLearned) {
            getPlayer().sendTalentsInfoData();
        }
    }


    private void handleLearnPvpTalents(LearnPvpTalents packet) {
        LearnPvpTalentFailed learnPvpTalentFailed = new LearnPvpTalentFailed();
        var anythingLearned = false;

        for (var pvpTalent : packet.talents) {
            tangible.RefObject<Integer> tempRef_SpellID = new tangible.RefObject<Integer>(learnPvpTalentFailed.spellID);
            var result = player.learnPvpTalent(pvpTalent.pvPTalentID, pvpTalent.slot, tempRef_SpellID);
            learnPvpTalentFailed.spellID = tempRef_SpellID.refArgValue;

            if (result != 0) {
                if (learnPvpTalentFailed.reason == 0) {
                    learnPvpTalentFailed.reason = (int) result.getValue();
                }

                learnPvpTalentFailed.talents.add(pvpTalent);
            } else {
                anythingLearned = true;
            }
        }

        if (learnPvpTalentFailed.reason != 0) {
            sendPacket(learnPvpTalentFailed);
        }

        if (anythingLearned) {
            player.sendTalentsInfoData();
        }
    }


    private void handleConfirmRespecWipe(ConfirmRespecWipe confirmRespecWipe) {
        var unit = getPlayer().getNPCIfCanInteractWith(confirmRespecWipe.respecMaster, NPCFlags.Trainer, NPCFlags2.NONE);

        if (unit == null) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleTalentWipeConfirm - {0} not found or you can't interact with him.", confirmRespecWipe.respecMaster.toString());

            return;
        }

        if (confirmRespecWipe.respecType != SpecResetType.talents) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleConfirmRespecWipe - reset type {0} is not implemented.", confirmRespecWipe.respecType);

            return;
        }

        if (!unit.canResetTalents(player)) {
            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        if (!getPlayer().resetTalents()) {
            return;
        }

        getPlayer().sendTalentsInfoData();
        unit.castSpell(getPlayer(), 14867, true); //spell: "Untalent Visual Effect"
    }


    private void handleUnlearnSkill(UnlearnSkill packet) {
        var rcEntry = global.getDB2Mgr().GetSkillRaceClassInfo(packet.skillLine, getPlayer().getRace(), getPlayer().getClass());

        if (rcEntry == null || !rcEntry.flags.hasFlag(SkillRaceClassInfoFlags.Unlearnable)) {
            return;
        }

        getPlayer().setSkill(packet.skillLine, 0, 0, 0);
    }


    private void handleTradeSkillSetFavorite(TradeSkillSetFavorite tradeSkillSetFavorite) {
        if (!player.hasSpell(tradeSkillSetFavorite.recipeID)) {
            return;
        }

        player.setSpellFavorite(tradeSkillSetFavorite.recipeID, tradeSkillSetFavorite.isFavorite);
    }


    private void handleWho(WhoRequestPkt whoRequest) {
        var request = whoRequest.request;

        // zones count, client limit = 10 (2.0.10)
        // can't be received from real client or broken packet
        if (whoRequest.areas.size() > 10) {
            return;
        }

        // user entered strings count, client limit=4 (checked on 2.0.10)
        // can't be received from real client or broken packet
        if (request.words.size() > 4) {
            return;
        }

        // @todo: handle following packet values
        // VirtualRealmNames
        // ShowEnemies
        // ShowArenaPlayers
        // ExactName
        // ServerInfo

        request.words.forEach(p -> p = p.toLowerCase());

        request.name = request.name.toLowerCase();
        request.guild = request.guild.toLowerCase();

        // client send in case not set max level value 100 but we support 255 max level,
        // update it to show GMs with character after 100 level
        if (whoRequest.request.maxLevel >= 100) {
            whoRequest.request.maxLevel = 255;
        }

        var team = getPlayer().getTeam();

        var gmLevelInWhoList = WorldConfig.getUIntValue(WorldCfg.GmLevelInWhoList);

        WhoResponsePkt response = new WhoResponsePkt();
        response.requestID = whoRequest.requestID;

        var whoList = global.getWhoListStorageMgr().GetWhoList();

        for (var target : whoList) {
            // player can see member of other team only if CONFIG_ALLOW_TWO_SIDE_WHO_LIST
            if (target.getTeam() != team && !hasPermission(RBACPermissions.TwoSideWhoList)) {
                continue;
            }

            // player can see MODERATOR, GAME MASTER, ADMINISTRATOR only if CONFIG_GM_IN_WHO_LIST
            if (target.getSecurity().getValue() > AccountTypes.forValue(gmLevelInWhoList) && !hasPermission(RBACPermissions.WhoSeeAllSecLevels)) {
                continue;
            }

            // check if target is globally visible for player
            if (ObjectGuid.opNotEquals(player.getGUID(), target.getGuid()) && !target.isVisible()) {
                if (global.getAccountMgr().isPlayerAccount(player.getSession().getSecurity()) || target.getSecurity().getValue() > player.getSession().getSecurity().getValue()) {
                    continue;
                }
            }

            // check if target's level is in level range
            var lvl = target.getLevel();

            if (lvl < request.minLevel || lvl > request.maxLevel) {
                continue;
            }

            // check if class matches classmask
            if (!(boolean) (request.classFilter & (1 << target.getClass()))) {
                continue;
            }

            // check if race matches racemask
            if (!(boolean) (request.raceFilter & (1 << target.getRace()))) {
                continue;
            }

            if (!whoRequest.areas.isEmpty()) {
                if (whoRequest.areas.contains((int) target.getZoneId())) {
                    continue;
                }
            }

            var wTargetName = target.getPlayerName().toLowerCase();

            if (!(request.name.isEmpty() || wTargetName.equals(request.name))) {
                continue;
            }

            var wTargetGuildName = target.getGuildName().toLowerCase();

            if (!request.guild.isEmpty() && !wTargetGuildName.equals(request.guild)) {
                continue;
            }

            if (!request.words.isEmpty()) {
                var aname = "";
                var areaEntry = CliDB.AreaTableStorage.get(target.getZoneId());

                if (areaEntry != null) {
                    aname = areaEntry.AreaName[getSessionDbcLocale()].toLowerCase();
                }

                var show = false;

                for (var i = 0; i < request.words.size(); ++i) {
                    if (!StringUtil.isEmpty(request.words.get(i))) {
                        if (wTargetName.equals(request.words.get(i)) || wTargetGuildName.equals(request.words.get(i)) || aname.equals(request.words.get(i))) {
                            show = true;

                            break;
                        }
                    }
                }

                if (!show) {
                    continue;
                }
            }

            WhoEntry whoEntry = new WhoEntry();

            if (!whoEntry.playerData.initialize(target.getGuid(), null)) {
                continue;
            }

            if (!target.getGuildGuid().isEmpty()) {
                whoEntry.guildGUID = target.getGuildGuid();
                whoEntry.guildVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
                whoEntry.guildName = target.getGuildName();
            }

            whoEntry.areaID = (int) target.getZoneId();
            whoEntry.isGM = target.isGamemaster();

            response.response.add(whoEntry);

            // 50 is maximum player count sent to client
            if (response.response.size() >= 50) {
                break;
            }
        }

        sendPacket(response);
    }


    private void handleWhoIs(WhoIsRequest packet) {
        if (!hasPermission(RBACPermissions.OpcodeWhois)) {
            sendNotification(SysMessage.YouNotHavePermission);

            return;
        }

        tangible.RefObject<String> tempRef_CharName = new tangible.RefObject<String>(packet.charName);
        if (!ObjectManager.normalizePlayerName(tempRef_CharName)) {
            packet.charName = tempRef_CharName.refArgValue;
            sendNotification(SysMessage.NeedCharacterName);

            return;
        } else {
            packet.charName = tempRef_CharName.refArgValue;
        }

        var player = global.getObjAccessor().FindPlayerByName(packet.charName);

        if (!player) {
            sendNotification(SysMessage.PlayerNotExistOrOffline, packet.charName);

            return;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_WHOIS);
        stmt.AddValue(0, player.getSession().getAccountId());

        var result = DB.Login.query(stmt);

        if (result.isEmpty()) {
            sendNotification(SysMessage.AccountForPlayerNotFound, packet.charName);

            return;
        }

        var acc = result.<String>Read(0);

        if (StringUtil.isEmpty(acc)) {
            acc = "Unknown";
        }

        var email = result.<String>Read(1);

        if (StringUtil.isEmpty(email)) {
            email = "Unknown";
        }

        var lastip = result.<String>Read(2);

        if (StringUtil.isEmpty(lastip)) {
            lastip = "Unknown";
        }

        WhoIsResponse response = new WhoIsResponse();
        response.accountName = packet.charName + "'s " + "account is " + acc + ", e-mail: " + email + ", last ip: " + lastip;
        sendPacket(response);
    }


    private void handleContactList(SendContactList packet) {
        getPlayer().getSocial().sendSocialList(getPlayer(), packet.flags);
    }


    private void handleAddFriend(AddFriend packet) {
        tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(packet.name);
        if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
            packet.name = tempRef_Name.refArgValue;
            return;
        } else {
            packet.name = tempRef_Name.refArgValue;
        }

        var friendCharacterInfo = global.getCharacterCacheStorage().getCharacterCacheByName(packet.name);

        if (friendCharacterInfo == null) {
            global.getSocialMgr().sendFriendStatus(getPlayer(), FriendsResult.NotFound, ObjectGuid.Empty);

            return;
        }


//		void processFriendRequest()
//			{
//				var playerGuid = player.GUID;
//				var friendGuid = friendCharacterInfo.guid;
//				var friendAccountGuid = ObjectGuid.create(HighGuid.wowAccount, friendCharacterInfo.accountId);
//				var team = player.teamForRace(friendCharacterInfo.raceId);
//				var friendNote = packet.notes;
//
//				if (playerGuid.counter != guidLow)
//					return; // not the player initiating request, do nothing
//
//				var friendResult = FriendsResult.NotFound;
//
//				if (friendGuid == player.GUID)
//				{
//					friendResult = FriendsResult.Self;
//				}
//				else if (player.team != team && !hasPermission(RBACPermissions.TwoSideAddFriend))
//				{
//					friendResult = FriendsResult.Enemy;
//				}
//				else if (player.Social.hasFriend(friendGuid))
//				{
//					friendResult = FriendsResult.Already;
//				}
//				else
//				{
//					var pFriend = global.ObjAccessor.findPlayer(friendGuid);
//
//					if (pFriend != null && pFriend.isVisibleGloballyFor(player))
//						friendResult = FriendsResult.online;
//					else
//						friendResult = FriendsResult.AddedOnline;
//
//					if (player.Social.addToSocialList(friendGuid, friendAccountGuid, SocialFlag.Friend))
//						player.Social.setFriendNote(friendGuid, friendNote);
//					else
//						friendResult = FriendsResult.ListFull;
//				}
//
//				global.SocialMgr.sendFriendStatus(player, friendResult, friendGuid);
//			}

        if (hasPermission(RBACPermissions.AllowGmFriend)) {
            processFriendRequest();

            return;
        }

        // First try looking up friend candidate security from online object
        var friendPlayer = global.getObjAccessor().findPlayer(friendCharacterInfo.guid);

        if (friendPlayer != null) {
            if (!global.getAccountMgr().isPlayerAccount(friendPlayer.getSession().getSecurity())) {
                global.getSocialMgr().sendFriendStatus(getPlayer(), FriendsResult.NotFound, ObjectGuid.Empty);

                return;
            }

            processFriendRequest();

            return;
        }

        // When not found, consult database
        getQueryProcessor().AddCallback(global.getAccountMgr().getSecurityAsync(friendCharacterInfo.accountId, (int) global.getWorldMgr().getRealmId().index, friendSecurity ->
        {
            if (!global.getAccountMgr().isPlayerAccount(AccountTypes.forValue(friendSecurity))) {
                global.getSocialMgr().sendFriendStatus(getPlayer(), FriendsResult.NotFound, ObjectGuid.Empty);

                return;
            }

            processFriendRequest();
        }));
    }


    private void handleDelFriend(DelFriend packet) {
        // @todo: handle VirtualRealmAddress
        getPlayer().getSocial().removeFromSocialList(packet.player.guid, SocialFlag.Friend);

        global.getSocialMgr().sendFriendStatus(getPlayer(), FriendsResult.removed, packet.player.guid);
    }


    private void handleAddIgnore(AddIgnore packet) {
        tangible.RefObject<String> tempRef_Name = new tangible.RefObject<String>(packet.name);
        if (!ObjectManager.normalizePlayerName(tempRef_Name)) {
            packet.name = tempRef_Name.refArgValue;
            return;
        } else {
            packet.name = tempRef_Name.refArgValue;
        }

        var ignoreGuid = ObjectGuid.Empty;
        var ignoreResult = FriendsResult.IgnoreNotFound;

        var characterInfo = global.getCharacterCacheStorage().getCharacterCacheByName(packet.name);

        if (characterInfo != null) {
            ignoreGuid = characterInfo.guid;
            var ignoreAccountGuid = ObjectGuid.create(HighGuid.wowAccount, characterInfo.accountId);

            if (Objects.equals(ignoreGuid, getPlayer().getGUID())) //not add yourself
            {
                ignoreResult = FriendsResult.IgnoreSelf;
            } else if (getPlayer().getSocial().hasIgnore(ignoreGuid, ignoreAccountGuid)) {
                ignoreResult = FriendsResult.IgnoreAlready;
            } else {
                ignoreResult = FriendsResult.IgnoreAdded;

                // ignore list full
                if (!getPlayer().getSocial().addToSocialList(ignoreGuid, ignoreAccountGuid, SocialFlag.Ignored)) {
                    ignoreResult = FriendsResult.IgnoreFull;
                }
            }
        }

        global.getSocialMgr().sendFriendStatus(getPlayer(), ignoreResult, ignoreGuid);
    }


    private void handleDelIgnore(DelIgnore packet) {
        // @todo: handle VirtualRealmAddress
        Log.outDebug(LogFilter.Network, "WorldSession.HandleDelIgnoreOpcode: {0}", packet.player.guid.toString());

        getPlayer().getSocial().removeFromSocialList(packet.player.guid, SocialFlag.Ignored);

        global.getSocialMgr().sendFriendStatus(getPlayer(), FriendsResult.IgnoreRemoved, packet.player.guid);
    }


    private void handleSetContactNotes(SetContactNotes packet) {
        // @todo: handle VirtualRealmAddress
        Log.outDebug(LogFilter.Network, "WorldSession.HandleSetContactNotesOpcode: Contact: {0}, Notes: {1}", packet.player.guid.toString(), packet.notes);
        getPlayer().getSocial().setFriendNote(packet.player.guid, packet.notes);
    }


    private void handleSocialContractRequest(SocialContractRequest socialContractRequest) {
        SocialContractRequestResponse response = new SocialContractRequestResponse();
        response.showSocialContract = false;
        sendPacket(response);
    }


    private void handleUseItem(UseItem packet) {
        var user = getPlayer();

        // ignore for remote control state
        if (user.getUnitBeingMoved() != user) {
            return;
        }

        var item = user.getUseableItemByPos(packet.packSlot, packet.slot);

        if (item == null) {
            user.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        if (ObjectGuid.opNotEquals(item.getGUID(), packet.castItem)) {
            user.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        var proto = item.getTemplate();

        if (proto == null) {
            user.sendEquipError(InventoryResult.ItemNotFound, item);

            return;
        }

        // some item classes can be used only in equipped state
        if (proto.getInventoryType() != inventoryType.NonEquip && !item.isEquipped()) {
            user.sendEquipError(InventoryResult.ItemNotFound, item);

            return;
        }

        var msg = user.canUseItem(item);

        if (msg != InventoryResult.Ok) {
            user.sendEquipError(msg, item);

            return;
        }

        // only allow conjured consumable, bandage, poisons (all should have the 2^21 item flag set in DB)
        if (proto.getClass() == itemClass.Consumable && !proto.hasFlag(ItemFlags.IgnoreDefaultArenaRestrictions) && user.getInArena()) {
            user.sendEquipError(InventoryResult.NotDuringArenaMatch, item);

            return;
        }

        // don't allow items banned in arena
        if (proto.hasFlag(ItemFlags.NotUseableInArena) && user.getInArena()) {
            user.sendEquipError(InventoryResult.NotDuringArenaMatch, item);

            return;
        }

        if (user.isInCombat()) {
            for (var effect : item.getEffects()) {
                var spellInfo = global.getSpellMgr().getSpellInfo((int) effect.spellID, user.getMap().getDifficultyID());

                if (spellInfo != null) {
                    if (!spellInfo.getCanBeUsedInCombat()) {
                        user.sendEquipError(InventoryResult.NotInCombat, item);

                        return;
                    }
                }
            }
        }

        // check also  BIND_WHEN_PICKED_UP and BIND_QUEST_ITEM for .additem or .additemset case by GM (not binded at adding to inventory)
        if (item.getBonding() == ItemBondingType.OnUse || item.getBonding() == ItemBondingType.OnAcquire || item.getBonding() == ItemBondingType.Quest) {
            if (!item.isSoulBound()) {
                item.setState(ItemUpdateState.changed, user);
                item.setBinding(true);
                getCollectionMgr().addItemAppearance(item);
            }
        }

        user.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.ItemUse);

        SpellCastTargets targets = new SpellCastTargets(user, packet.cast);

        // Note: If script stop casting it must send appropriate data to client to prevent stuck item in gray state.
        if (!global.getScriptMgr().<IItemOnUse>RunScriptRet(p -> p.OnUse(user, item, targets, packet.cast.castID), item.getScriptId())) {
            // no script or script not process request by self
            user.castItemUseSpell(item, targets, packet.cast.castID, packet.cast.misc);
        }
    }


    private void handleOpenItem(OpenItem packet) {
        var player = getPlayer();

        // ignore for remote control state
        if (player.getUnitBeingMoved() != player) {
            return;
        }

        // additional check, client outputs message on its own
        if (!player.isAlive()) {
            player.sendEquipError(InventoryResult.PlayerDead);

            return;
        }

        var item = player.getItemByPos(packet.slot, packet.packSlot);

        if (!item) {
            player.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        var proto = item.getTemplate();

        if (proto == null) {
            player.sendEquipError(InventoryResult.ItemNotFound, item);

            return;
        }

        // Verify that the bag is an actual bag or wrapped item that can be used "normally"
        if (!proto.hasFlag(ItemFlags.HasLoot) && !item.isWrapped()) {
            player.sendEquipError(InventoryResult.ClientLockedOut, item);

            Log.outError(LogFilter.Network, "Possible hacking attempt: Player {0} [guid: {1}] tried to open item [guid: {2}, entry: {3}] which is not openable!", player.getName(), player.getGUID().toString(), item.getGUID().toString(), proto.getId());

            return;
        }

        // locked item
        var lockId = proto.getLockID();

        if (lockId != 0) {
            var lockInfo = CliDB.LockStorage.get(lockId);

            if (lockInfo == null) {
                player.sendEquipError(InventoryResult.ItemLocked, item);
                Log.outError(LogFilter.Network, "WORLD:OpenItem: item [guid = {0}] has an unknown lockId: {1}!", item.getGUID().toString(), lockId);

                return;
            }

            // was not unlocked yet
            if (item.isLocked()) {
                player.sendEquipError(InventoryResult.ItemLocked, item);

                return;
            }
        }

        if (item.isWrapped()) // wrapped?
        {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_GIFT_BY_ITEM);
            stmt.AddValue(0, item.getGUID().getCounter());

            var pos = item.getPos();
            var itemGuid = item.getGUID();

            queryProcessor.AddCallback(DB.characters.AsyncQuery(stmt).WithCallback(result -> handleOpenWrappedItemCallback(pos, itemGuid, result)));
        } else {
            // If item doesn't already have loot, attempt to load it. If that
            // fails then this is first time opening, generate loot
            if (!item.getLootGenerated() && !global.getLootItemStorage().loadStoredLoot(item, player)) {
                Loot loot = new loot(player.getMap(), item.getGUID(), LootType.item, null);
                item.setLoot(loot);
                loot.generateMoneyLoot(item.getTemplate().getMinMoneyLoot(), item.getTemplate().getMaxMoneyLoot());
                loot.fillLoot(item.getEntry(), LootStorage.items, player, true, loot.gold != 0);

                // Force save the loot and money items that were just rolled
                //  Also saves the container item ID in Loot struct (not to DB)
                if (loot.gold > 0 || loot.unlootedCount > 0) {
                    global.getLootItemStorage().addNewStoredLoot(item.getGUID().getCounter(), loot, player);
                }
            }

            if (item.getLoot() != null) {
                player.sendLoot(item.getLoot());
            } else {
                player.sendLootError(ObjectGuid.Empty, item.getGUID(), LootError.NoLoot);
            }
        }
    }

    private void handleOpenWrappedItemCallback(short pos, ObjectGuid itemGuid, SQLResult result) {
        if (!getPlayer()) {
            return;
        }

        var item = getPlayer().getItemByPos(pos);

        if (!item) {
            return;
        }

        if (ObjectGuid.opNotEquals(item.getGUID(), itemGuid) || !item.isWrapped()) // during getting result, gift was swapped with another item
        {
            return;
        }

        if (result.isEmpty()) {
            Log.outError(LogFilter.Network, String.format("Wrapped item %1$s don't have record in character_gifts table and will deleted", item.getGUID()));
            getPlayer().destroyItem(item.getBagSlot(), item.getSlot(), true);

            return;
        }

        SQLTransaction trans = new SQLTransaction();

        var entry = result.<Integer>Read(0);
        var flags = result.<Integer>Read(1);

        item.setGiftCreator(ObjectGuid.Empty);
        item.setEntry(entry);
        item.replaceAllItemFlags(ItemFieldFlags.forValue(flags));
        item.setMaxDurability(item.getTemplate().getMaxDurability());
        item.setState(ItemUpdateState.changed, getPlayer());

        getPlayer().saveInventoryAndGoldToDB(trans);

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GIFT);
        stmt.AddValue(0, itemGuid.getCounter());
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);
    }


    private void handleGameObjectUse(GameObjUse packet) {
        var obj = getPlayer().getGameObjectIfCanInteractWith(packet.guid);

        if (obj) {
            // ignore for remote control state
            if (getPlayer().getUnitBeingMoved() != getPlayer()) {
                if (!(getPlayer().isOnVehicle(getPlayer().getUnitBeingMoved()) || getPlayer().isMounted()) && !obj.getTemplate().isUsableMounted()) {
                    return;
                }
            }

            obj.use(getPlayer());
        }
    }


    private void handleGameobjectReportUse(GameObjReportUse packet) {
        // ignore for remote control state
        if (getPlayer().getUnitBeingMoved() != getPlayer()) {
            return;
        }

        var go = getPlayer().getGameObjectIfCanInteractWith(packet.guid);

        if (go) {
            if (go.getAI().onGossipHello(getPlayer())) {
                return;
            }

            getPlayer().updateCriteria(CriteriaType.UseGameobject, go.getEntry());
        }
    }


    private void handleCastSpell(CastSpell cast) {
        // ignore for remote control state (for player case)
        var mover = getPlayer().getUnitBeingMoved();

        if (mover != getPlayer() && mover.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(cast.cast.spellID, mover.getMap().getDifficultyID());

        if (spellInfo == null) {
            Log.outError(LogFilter.Network, "WORLD: unknown spell id {0}", cast.cast.spellID);

            return;
        }

        if (spellInfo.isPassive()) {
            return;
        }

        var caster = mover;

        if (caster.isTypeId(TypeId.UNIT) && !caster.toCreature().hasSpell(spellInfo.getId())) {
            // If the vehicle creature does not have the spell but it allows the passenger to cast own spells
            // change caster to player and let him cast
            if (!getPlayer().isOnVehicle(caster) || spellInfo.checkVehicle(getPlayer()) != SpellCastResult.SpellCastOk) {
                return;
            }

            caster = getPlayer();
        }

        var triggerFlag = TriggerCastFlags.NONE;

        // client provided targets
        SpellCastTargets targets = new SpellCastTargets(caster, cast.cast);

        // check known spell or raid marker spell (which not requires player to know it)
        if (caster.isTypeId(TypeId.PLAYER) && !caster.toPlayer().hasActiveSpell(spellInfo.getId()) && !spellInfo.hasEffect(SpellEffectName.ChangeRaidMarker) && !spellInfo.hasAttribute(SpellAttr8.RaidMarker)) {
            var allow = false;


            // allow casting of unknown spells for special lock cases
            var go = targets.getGOTarget();

            if (go != null) {
                if (go.getSpellForLock(caster.toPlayer()) == spellInfo) {
                    allow = true;
                }
            }

            // allow casting of spells triggered by clientside periodic trigger auras
            if (caster.hasAuraTypeWithTriggerSpell(AuraType.PeriodicTriggerSpellFromClient, spellInfo.getId())) {
                allow = true;
                triggerFlag = TriggerCastFlags.FullMask;
            }

            if (!allow) {
                return;
            }
        }

        // Check possible spell cast overrides
        spellInfo = caster.getCastSpellInfo(spellInfo);

        // can't use our own spells when we're in possession of another unit,
        if (getPlayer().isPossessing()) {
            return;
        }

        // Client is resending autoshot cast opcode when other spell is cast during shoot rotation
        // Skip it to prevent "interrupt" message
        // Also check targets! target may have changed and we need to interrupt current spell
        if (spellInfo.isAutoRepeatRangedSpell()) {
            var autoRepeatSpell = caster.getCurrentSpell(CurrentSpellTypes.AutoRepeat);

            if (autoRepeatSpell != null) {
                if (autoRepeatSpell.spellInfo == spellInfo && Objects.equals(autoRepeatSpell.targets.getUnitTargetGUID(), targets.getUnitTargetGUID())) {
                    return;
                }
            }
        }

        // auto-selection buff level base at target level (in spellInfo)
        if (targets.getUnitTarget() != null) {
            var actualSpellInfo = spellInfo.getAuraRankForLevel(targets.getUnitTarget().getLevelForTarget(caster));

            // if rank not found then function return NULL but in explicit cast case original spell can be casted and later failed with appropriate error message
            if (actualSpellInfo != null) {
                spellInfo = actualSpellInfo;
            }
        }

        if (cast.cast.moveUpdate != null) {
            handleMovementOpcode(ClientOpcodes.MoveStop, cast.cast.moveUpdate);
        }

        Spell spell = new spell(caster, spellInfo, triggerFlag);

        SpellPrepare spellPrepare = new SpellPrepare();
        spellPrepare.clientCastID = cast.cast.castID;
        spellPrepare.serverCastID = spell.castId;
        sendPacket(spellPrepare);

        spell.fromClient = true;
        spell.spellMisc.data0 = cast.cast.Misc[0];
        spell.spellMisc.data1 = cast.cast.Misc[1];
        spell.prepare(targets);
    }


    private void handleCancelCast(CancelCast packet) {
        if (getPlayer().isNonMeleeSpellCast(false)) {
            getPlayer().interruptNonMeleeSpells(false, packet.spellID, false);
        }
    }


    private void handleCancelAura(CancelAura cancelAura) {
        var spellInfo = global.getSpellMgr().getSpellInfo(cancelAura.spellID, player.getMap().getDifficultyID());

        if (spellInfo == null) {
            return;
        }

        // not allow remove spells with attr SPELL_ATTR0_CANT_CANCEL
        if (spellInfo.hasAttribute(SpellAttr0.NoAuraCancel)) {
            return;
        }

        // channeled spell case (it currently casted then)
        if (spellInfo.isChanneled()) {
            var curSpell = getPlayer().getCurrentSpell(CurrentSpellTypes.Channeled);

            if (curSpell != null) {
                if (curSpell.spellInfo.getId() == cancelAura.spellID) {
                    getPlayer().interruptSpell(CurrentSpellTypes.Channeled);
                }
            }

            return;
        }

        // non channeled case:
        // don't allow remove non positive spells
        // don't allow cancelling passive auras (some of them are visible)
        if (!spellInfo.isPositive() || spellInfo.isPassive()) {
            return;
        }

        getPlayer().removeOwnedAura(cancelAura.spellID, cancelAura.casterGUID, AuraRemoveMode.Cancel);
    }


    private void handleCancelGrowthAura(CancelGrowthAura cancelGrowthAura) {
        getPlayer().removeAurasByType(AuraType.ModScale, aurApp ->
        {
            var spellInfo = aurApp.base.spellInfo;

            return !spellInfo.hasAttribute(SpellAttr0.NoAuraCancel) && spellInfo.IsPositive && !spellInfo.isPassive;
        });
    }


    private void handleCancelMountAura(CancelMountAura packet) {
        getPlayer().removeAurasByType(AuraType.Mounted, aurApp ->
        {
            var spellInfo = aurApp.base.spellInfo;

            return !spellInfo.hasAttribute(SpellAttr0.NoAuraCancel) && spellInfo.IsPositive && !spellInfo.isPassive;
        });
    }


    private void handlePetCancelAura(PetCancelAura packet) {
        if (!global.getSpellMgr().hasSpellInfo(packet.spellID, Difficulty.NONE)) {
            Log.outError(LogFilter.Network, "WORLD: unknown PET spell id {0}", packet.spellID);

            return;
        }

        var pet = ObjectAccessor.GetCreatureOrPetOrVehicle(player, packet.petGUID);

        if (pet == null) {
            Log.outError(LogFilter.Network, "HandlePetCancelAura: Attempt to cancel an aura for non-existant {0} by player '{1}'", packet.petGUID.toString(), getPlayer().getName());

            return;
        }

        if (pet != getPlayer().getGuardianPet() && pet != getPlayer().getCharmed()) {
            Log.outError(LogFilter.Network, "HandlePetCancelAura: {0} is not a pet of player '{1}'", packet.petGUID.toString(), getPlayer().getName());

            return;
        }

        if (!pet.isAlive()) {
            pet.sendPetActionFeedback(PetActionFeedback.Dead, 0);

            return;
        }

        pet.removeOwnedAura(packet.spellID, ObjectGuid.Empty, AuraRemoveMode.Cancel);
    }


    private void handleCancelModSpeedNoControlAuras(CancelModSpeedNoControlAuras cancelModSpeedNoControlAuras) {
        var mover = player.getUnitBeingMoved();

        if (mover == null || ObjectGuid.opNotEquals(mover.getGUID(), cancelModSpeedNoControlAuras.targetGUID)) {
            return;
        }

        player.removeAurasByType(AuraType.ModSpeedNoControl, aurApp ->
        {
            var spellInfo = aurApp.base.spellInfo;

            return !spellInfo.hasAttribute(SpellAttr0.NoAuraCancel) && spellInfo.IsPositive && !spellInfo.isPassive;
        });
    }


    private void handleCancelAutoRepeatSpell(CancelAutoRepeatSpell packet) {
        //may be better send SMSG_CANCEL_AUTO_REPEAT?
        //cancel and prepare for deleting
        player.interruptSpell(CurrentSpellTypes.AutoRepeat);
    }


    private void handleCancelChanneling(CancelChannelling cancelChanneling) {
        // ignore for remote control state (for player case)
        var mover = player.getUnitBeingMoved();

        if (mover != player && mover.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo((int) cancelChanneling.channelSpell, mover.getMap().getDifficultyID());

        if (spellInfo == null) {
            return;
        }

        // not allow remove spells with attr SPELL_ATTR0_CANT_CANCEL
        if (spellInfo.hasAttribute(SpellAttr0.NoAuraCancel)) {
            return;
        }

        var spell = mover.getCurrentSpell(CurrentSpellTypes.Channeled);

        if (spell == null || spell.spellInfo.getId() != spellInfo.getId()) {
            return;
        }

        mover.interruptSpell(CurrentSpellTypes.Channeled);
    }


    private void handleTotemDestroyed(TotemDestroyed totemDestroyed) {
        // ignore for remote control state
        if (getPlayer().getUnitBeingMoved() != getPlayer()) {
            return;
        }

        var slotId = totemDestroyed.slot;
        slotId += summonSlot.totem.getValue();

        if (slotId >= SharedConst.MaxTotemSlot) {
            return;
        }

        if (getPlayer().getSummonSlot()[slotId].isEmpty()) {
            return;
        }

        var totem = ObjectAccessor.getCreature(getPlayer(), player.getSummonSlot()[slotId]);

        if (totem != null && totem.isTotem()) // && totem.getGUID() == packet.totemGUID)  Unknown why blizz doesnt send the guid when you right click it.
        {
            totem.toTotem().unSummon();
        }
    }


    private void handleSelfRes(SelfRes selfRes) {
        ArrayList<Integer> selfResSpells = player.getActivePlayerData().selfResSpells;

        if (!selfResSpells.contains(selfRes.spellId)) {
            return;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(selfRes.spellId, player.getMap().getDifficultyID());

        if (spellInfo == null) {
            return;
        }

        if (player.hasAuraType(AuraType.PreventResurrection) && !spellInfo.hasAttribute(SpellAttr7.BypassNoResurrectAura)) {
            return; // silent return, client should display error by itself and not send this opcode
        }

        player.castSpell(player, selfRes.spellId, new CastSpellExtraArgs(player.getMap().getDifficultyID()));
        player.removeSelfResSpell(selfRes.spellId);
    }


    private void handleSpellClick(SpellClick packet) {
        // this will get something not in world. crash
        var unit = ObjectAccessor.GetCreatureOrPetOrVehicle(getPlayer(), packet.spellClickUnitGuid);

        if (unit == null) {
            return;
        }

        // @todo unit.SetCharmedBy: 28782 is not in world but 0 is trying to charm it! . crash
        if (!unit.isInWorld()) {
            return;
        }

        unit.handleSpellClick(getPlayer());
    }


    private void handleMirrorImageDataRequest(GetMirrorImageData packet) {
        var guid = packet.unitGUID;

        // Get unit for which data is needed by client
        var unit = global.getObjAccessor().GetUnit(getPlayer(), guid);

        if (!unit) {
            return;
        }

        if (!unit.hasAuraType(AuraType.CloneCaster)) {
            return;
        }

        // Get creator of the unit (SPELL_AURA_CLONE_CASTER does not stack)
        var creator = unit.getAuraEffectsByType(AuraType.CloneCaster).FirstOrDefault().caster;

        if (!creator) {
            return;
        }

        var player = creator.AsPlayer;

        if (player) {
            MirrorImageComponentedData mirrorImageComponentedData = new MirrorImageComponentedData();
            mirrorImageComponentedData.unitGUID = guid;
            mirrorImageComponentedData.displayID = (int) creator.displayId;
            mirrorImageComponentedData.raceID = (byte) creator.race;
            mirrorImageComponentedData.gender = (byte) creator.gender;
            mirrorImageComponentedData.classID = (byte) creator.class;

            for (var customization : player.playerData.customizations) {
                var chrCustomizationChoice = new ChrCustomizationChoice();
                chrCustomizationChoice.chrCustomizationOptionID = customization.chrCustomizationOptionID;
                chrCustomizationChoice.chrCustomizationChoiceID = customization.chrCustomizationChoiceID;
                mirrorImageComponentedData.customizations.add(chrCustomizationChoice);
            }

            var guild = player.guild;
            mirrorImageComponentedData.guildGUID = (guild ? guild.getGUID() : ObjectGuid.Empty);

            byte[] itemSlots = {EquipmentSlot.Head, EquipmentSlot.Shoulders, EquipmentSlot.Shirt, EquipmentSlot.chest, EquipmentSlot.Waist, EquipmentSlot.Legs, EquipmentSlot.Feet, EquipmentSlot.Wrist, EquipmentSlot.Hands, EquipmentSlot.Tabard, EquipmentSlot.Cloak};

            // Display items in visible slots
            for (var slot : itemSlots) {
                int itemDisplayId;
                var item = player.getItemByPos(InventorySlots.Bag0, slot);

                if (item != null) {
                    itemDisplayId = item.getDisplayId(player);
                } else {
                    itemDisplayId = 0;
                }

                mirrorImageComponentedData.itemDisplayID.add((int) itemDisplayId);
            }

            sendPacket(mirrorImageComponentedData);
        } else {
            MirrorImageCreatureData data = new MirrorImageCreatureData();
            data.unitGUID = guid;
            data.displayID = (int) creator.displayId;
            sendPacket(data);
        }
    }


    private void handleMissileTrajectoryCollision(MissileTrajectoryCollision packet) {
        var caster = global.getObjAccessor().GetUnit(player, packet.target);

        if (caster == null) {
            return;
        }

        var spell = caster.findCurrentSpellBySpellId(packet.spellID);

        if (spell == null || !spell.targets.getHasDst()) {
            return;
        }

        Position pos = spell.targets.getDstPos();
        pos.relocate(packet.collisionPos);
        spell.targets.modDst(pos);

        // we changed dest, recalculate flight time
        spell.recalculateDelayMomentForDst();

        NotifyMissileTrajectoryCollision data = new NotifyMissileTrajectoryCollision();
        data.caster = packet.target;
        data.castID = packet.castID;
        data.collisionPos = packet.collisionPos;
        caster.sendMessageToSet(data, true);
    }


    private void handleUpdateMissileTrajectory(UpdateMissileTrajectory packet) {
        var caster = global.getObjAccessor().GetUnit(getPlayer(), packet.guid);
        var spell = caster ? caster.getCurrentSpell(CurrentSpellTypes.generic) : null;

        if (!spell || spell.spellInfo.getId() != packet.spellID || ObjectGuid.opNotEquals(spell.castId, packet.castID) || !spell.targets.getHasDst() || !spell.targets.getHasSrc()) {
            return;
        }

        var pos = spell.targets.getSrcPos();
        pos.relocate(packet.firePos);
        spell.targets.modSrc(pos);

        pos = spell.targets.getDstPos();
        pos.relocate(packet.impactPos);
        spell.targets.modDst(pos);

        spell.targets.setPitch(packet.pitch);
        spell.targets.setSpeed(packet.speed);

        if (packet.status != null) {
            getPlayer().validateMovementInfo(packet.status);
        }
		/*public uint opcode;
			recvPacket >> opcode;
			recvPacket.SetOpcode(CMSG_MOVE_STOP); // always set to CMSG_MOVE_STOP in client SetOpcode
			//HandleMovementOpcodes(recvPacket);*/
    }


    private void handleRequestCategoryCooldowns(RequestCategoryCooldowns requestCategoryCooldowns) {
        getPlayer().sendSpellCategoryCooldowns();
    }


    private void handleKeyboundOverride(KeyboundOverride keyboundOverride) {
        var player = getPlayer();

        if (!player.hasAuraTypeWithMiscvalue(AuraType.KeyboundOverride, keyboundOverride.overrideID)) {
            return;
        }

        var spellKeyboundOverride = CliDB.SpellKeyboundOverrideStorage.get(keyboundOverride.overrideID);

        if (spellKeyboundOverride == null) {
            return;
        }

        player.castSpell(player, spellKeyboundOverride.data);
    }


    private void handleSpellEmpowerRelease(SpellEmpowerRelease packet) {
        getPlayer().updateEmpowerState(EmpowerState.Canceled, packet.spellID);
    }


    private void handleSpellEmpowerRelestart(SpellEmpowerRelease packet) {
        getPlayer().updateEmpowerState(EmpowerState.Empowering, packet.spellID);
    }


    private void handleSpellEmpowerMinHoldPct(SpellEmpowerMinHold packet) {
        getPlayer().setEmpoweredSpellMinHoldPct(packet.holdPct);
    }

    public final void sendTaxiStatus(ObjectGuid guid) {
        // cheating checks
        var player = getPlayer();
        var unit = ObjectAccessor.getCreature(player, guid);

        if (!unit || unit.isHostileTo(player) || !unit.hasNpcFlag(NPCFlags.FlightMaster)) {
            Log.outDebug(LogFilter.Network, "WorldSession.SendTaxiStatus - {0} not found.", guid.toString());

            return;
        }

        // find taxi node
        var nearest = global.getObjectMgr().getNearestTaxiNode(unit.getLocation().getX(), unit.getLocation().getY(), unit.getLocation().getZ(), unit.getLocation().getMapId(), player.getTeam());

        TaxiNodeStatusPkt data = new TaxiNodeStatusPkt();
        data.unit = guid;

        if (nearest == 0) {
            data.status = TaxiNodeStatus.NONE;
        } else if (unit.getReactionTo(player) >= ReputationRank.Neutral.getValue()) {
            data.status = player.getTaxi().isTaximaskNodeKnown(nearest) ? TaxiNodeStatus.Learned : TaxiNodeStatus.Unlearned;
        } else {
            data.status = TaxiNodeStatus.NotEligible;
        }

        sendPacket(data);
    }

    public final void sendTaxiMenu(Creature unit) {
        // find current node
        var curloc = global.getObjectMgr().getNearestTaxiNode(unit.getLocation().getX(), unit.getLocation().getY(), unit.getLocation().getZ(), unit.getLocation().getMapId(), getPlayer().getTeam());

        if (curloc == 0) {
            return;
        }

        var lastTaxiCheaterState = getPlayer().isTaxiCheater();

        if (unit.getEntry() == 29480) {
            getPlayer().setTaxiCheater(true); // Grimwing in Ebon Hold, special case. NOTE: Not perfect, Zul'Aman should not be included according to WoWhead, and I think taxicheat includes it.
        }

        ShowTaxiNodes data = new ShowTaxiNodes();
        ShowTaxiNodesWindowInfo windowInfo = new ShowTaxiNodesWindowInfo();
        windowInfo.unitGUID = unit.getGUID();
        windowInfo.currentNode = (int) curloc;

        data.windowInfo = windowInfo;

        getPlayer().getTaxi().appendTaximaskTo(data, lastTaxiCheaterState);

        var reachableNodes = new byte[CliDB.TaxiNodesMask.length];
        TaxiPathGraph.getReachableNodesMask(CliDB.TaxiNodesStorage.get(curloc), reachableNodes);

        for (var i = 0; i < reachableNodes.length; ++i) {
            data.CanLandNodes[i] &= reachableNodes[i];
            data.CanUseNodes[i] &= reachableNodes[i];
        }


        sendPacket(data);

        getPlayer().setTaxiCheater(lastTaxiCheaterState);
    }

    public final void sendDoFlight(int mountDisplayId, int path) {
        sendDoFlight(mountDisplayId, path, 0);
    }

    public final void sendDoFlight(int mountDisplayId, int path, int pathNode) {
        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        if (mountDisplayId != 0) {
            getPlayer().mount(mountDisplayId);
        }

        getPlayer().getMotionMaster().moveTaxiFlight(path, pathNode);
    }

    public final boolean sendLearnNewTaxiNode(Creature unit) {
        // find current node
        var curloc = global.getObjectMgr().getNearestTaxiNode(unit.getLocation().getX(), unit.getLocation().getY(), unit.getLocation().getZ(), unit.getLocation().getMapId(), getPlayer().getTeam());

        if (curloc == 0) {
            return true;
        }

        if (getPlayer().getTaxi().setTaximaskNode(curloc)) {
            sendPacket(new NewTaxiPath());

            TaxiNodeStatusPkt data = new TaxiNodeStatusPkt();
            data.unit = unit.getGUID();
            data.status = TaxiNodeStatus.Learned;
            sendPacket(data);

            return true;
        } else {
            return false;
        }
    }

    public final void sendDiscoverNewTaxiNode(int nodeid) {
        if (getPlayer().getTaxi().setTaximaskNode(nodeid)) {
            sendPacket(new NewTaxiPath());
        }
    }

    public final void sendActivateTaxiReply() {
        sendActivateTaxiReply(ActivateTaxiReply.Ok);
    }

    public final void sendActivateTaxiReply(ActivateTaxiReply reply) {
        ActivateTaxiReplyPkt data = new ActivateTaxiReplyPkt();
        data.reply = reply;
        sendPacket(data);
    }


    private void handleEnableTaxiNodeOpcode(EnableTaxiNode enableTaxiNode) {
        var unit = getPlayer().getNPCIfCanInteractWith(enableTaxiNode.unit, NPCFlags.FlightMaster, NPCFlags2.NONE);

        if (unit) {
            sendLearnNewTaxiNode(unit);
        }
    }


    private void handleTaxiNodeStatusQuery(TaxiNodeStatusQuery taxiNodeStatusQuery) {
        sendTaxiStatus(taxiNodeStatusQuery.unitGUID);
    }


    private void handleTaxiQueryAvailableNodes(TaxiQueryAvailableNodes taxiQueryAvailableNodes) {
        // cheating checks
        var unit = getPlayer().getNPCIfCanInteractWith(taxiQueryAvailableNodes.unit, NPCFlags.FlightMaster, NPCFlags2.NONE);

        if (unit == null) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleTaxiQueryAvailableNodes - {0} not found or you can't interact with him.", taxiQueryAvailableNodes.unit.toString());

            return;
        }

        // remove fake death
        if (getPlayer().hasUnitState(UnitState.Died)) {
            getPlayer().removeAurasByType(AuraType.FeignDeath);
        }

        // unknown taxi node case
        if (sendLearnNewTaxiNode(unit)) {
            return;
        }

        // known taxi node case
        sendTaxiMenu(unit);
    }


    private void handleActivateTaxi(ActivateTaxi activateTaxi) {
        var unit = getPlayer().getNPCIfCanInteractWith(activateTaxi.vendor, NPCFlags.FlightMaster, NPCFlags2.NONE);

        if (unit == null) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleActivateTaxiOpcode - {0} not found or you can't interact with it.", activateTaxi.vendor.toString());
            sendActivateTaxiReply(ActivateTaxiReply.TooFarAway);

            return;
        }

        var curloc = global.getObjectMgr().getNearestTaxiNode(unit.getLocation().getX(), unit.getLocation().getY(), unit.getLocation().getZ(), unit.getLocation().getMapId(), getPlayer().getTeam());

        if (curloc == 0) {
            return;
        }

        var from = CliDB.TaxiNodesStorage.get(curloc);
        var to = CliDB.TaxiNodesStorage.get(activateTaxi.node);

        if (to == null) {
            return;
        }

        if (!getPlayer().isTaxiCheater()) {
            if (!getPlayer().getTaxi().isTaximaskNodeKnown(curloc) || !getPlayer().getTaxi().isTaximaskNodeKnown(activateTaxi.node)) {
                sendActivateTaxiReply(ActivateTaxiReply.NotVisited);

                return;
            }
        }

        int preferredMountDisplay = 0;
        var mount = CliDB.MountStorage.get(activateTaxi.flyingMountID);

        if (mount != null) {
            if (getPlayer().hasSpell(mount.sourceSpellID)) {
                var mountDisplays = global.getDB2Mgr().GetMountDisplays(mount.id);

                if (mountDisplays != null) {
                    var usableDisplays = mountDisplays.stream().filter(mountDisplay ->
                    {
                        var playerCondition = CliDB.PlayerConditionStorage.get(mountDisplay.playerConditionID);

                        if (playerCondition != null) {
                            return ConditionManager.isPlayerMeetingCondition(getPlayer(), playerCondition);
                        }

                        return true;
                    }).collect(Collectors.toList());

                    if (!usableDisplays.isEmpty()) {
                        preferredMountDisplay = usableDisplays.SelectRandom().creatureDisplayInfoID;
                    }
                }
            }
        }

        ArrayList<Integer> nodes = new ArrayList<>();
        TaxiPathGraph.getCompleteNodeRoute(from, to, getPlayer(), nodes);
        getPlayer().activateTaxiPathTo(nodes, unit, 0, preferredMountDisplay);
    }


    private void handleTaxiRequestEarlyLanding(TaxiRequestEarlyLanding taxiRequestEarlyLanding) {
        MovementGenerator tempVar = getPlayer().getMotionMaster().getCurrentMovementGenerator();
        var flight = tempVar instanceof FlightPathMovementGenerator ? (FlightPathMovementGenerator) tempVar : null;

        if (flight != null) {
            if (getPlayer().getTaxi().requestEarlyLanding()) {
                flight.loadPath(getPlayer(), (int) flight.getPath().get((int) flight.getCurrentNode()).NodeIndex);
                flight.reset(getPlayer());
            }
        }
    }


    private void handleGMTicketGetCaseStatus(GMTicketGetCaseStatus packet) {
        //TODO: Implement GmCase and handle this packet correctly
        GMTicketCaseStatus status = new GMTicketCaseStatus();
        sendPacket(status);
    }


    private void handleGMTicketSystemStatusOpcode(GMTicketGetSystemStatus packet) {
        // Note: This only disables the ticket UI at client side and is not fully reliable
        // Note: This disables the whole customer support UI after trying to send a ticket in disabled state (MessageBox: "GM Help Tickets are currently unavaiable."). UI remains disabled until the character relogs.
        GMTicketSystemStatusPkt response = new GMTicketSystemStatusPkt();
        response.status = global.getSupportMgr().getSupportSystemStatus() ? 1 : 0;
        sendPacket(response);
    }


    private void handleSubmitUserFeedback(SubmitUserFeedback userFeedback) {
        if (userFeedback.isSuggestion) {
            if (!global.getSupportMgr().getSuggestionSystemStatus()) {
                return;
            }

            SuggestionTicket ticket = new SuggestionTicket(getPlayer());
            ticket.setPosition(userFeedback.header.mapID, userFeedback.header.position);
            ticket.setFacing(userFeedback.header.facing);
            ticket.setNote(userFeedback.note);

            global.getSupportMgr().addTicket(ticket);
        } else {
            if (!global.getSupportMgr().getBugSystemStatus()) {
                return;
            }

            BugTicket ticket = new BugTicket(getPlayer());
            ticket.setPosition(userFeedback.header.mapID, userFeedback.header.position);
            ticket.setFacing(userFeedback.header.facing);
            ticket.setNote(userFeedback.note);

            global.getSupportMgr().addTicket(ticket);
        }
    }


    private void handleSupportTicketSubmitComplaint(SupportTicketSubmitComplaint packet) {
        if (!global.getSupportMgr().getComplaintSystemStatus()) {
            return;
        }

        ComplaintTicket comp = new ComplaintTicket(getPlayer());
        comp.setPosition(packet.header.mapID, packet.header.position);
        comp.setFacing(packet.header.facing);
        comp.setChatLog(packet.chatLog);
        comp.setTargetCharacterGuid(packet.targetCharacterGUID);
        comp.setReportType(reportType.forValue(packet.reportType));
        comp.setMajorCategory(ReportMajorCategory.forValue(packet.majorCategory));
        comp.setMinorCategoryFlags(ReportMinorCategory.forValue(packet.minorCategoryFlags));
        comp.setNote(packet.note);

        global.getSupportMgr().addTicket(comp);
    }


    private void handleBugReport(BugReport bugReport) {
        // Note: There is no way to trigger this with standard UI except /script ReportBug("text")
        if (!global.getSupportMgr().getBugSystemStatus()) {
            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_BUG_REPORT);
        stmt.AddValue(0, bugReport.text);
        stmt.AddValue(1, bugReport.diagInfo);
        DB.characters.execute(stmt);
    }


    private void handleComplaint(Complaint packet) {
        // NOTE: all chat messages from this spammer are automatically ignored by the spam reporter until logout in case of chat spam.
        // if it's mail spam - ALL mails from this spammer are automatically removed by client

        ComplaintResult result = new ComplaintResult();
        result.complaintType = packet.complaintType;
        result.result = 0;
        sendPacket(result);
    }


    private void handleServerTimeOffsetRequest(ServerTimeOffsetRequest packet) {
        ServerTimeOffset response = new ServerTimeOffset();
        response.time = gameTime.GetGameTime();
        sendPacket(response);
    }


    private void handleCommerceTokenGetLog(CommerceTokenGetLog commerceTokenGetLog) {
        CommerceTokenGetLogResponse response = new CommerceTokenGetLogResponse();

        // @todo: fix 6.x implementation
        response.unkInt = commerceTokenGetLog.unkInt;
        response.result = TokenResult.success;

        sendPacket(response);
    }


    private void handleCommerceTokenGetMarketPrice(CommerceTokenGetMarketPrice commerceTokenGetMarketPrice) {
        CommerceTokenGetMarketPriceResponse response = new CommerceTokenGetMarketPriceResponse();

        // @todo: 6.x fix implementation
        response.currentMarketPrice = 300000000;
        response.unkInt = commerceTokenGetMarketPrice.unkInt;
        response.result = TokenResult.success;
        //packet.readUInt("UnkInt32");

        sendPacket(response);
    }


    private void handleAddToy(AddToy packet) {
        if (packet.guid.isEmpty()) {
            return;
        }

        var item = player.getItemByGuid(packet.guid);

        if (!item) {
            player.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        if (!global.getDB2Mgr().IsToyItem(item.getEntry())) {
            return;
        }

        var msg = player.canUseItem(item);

        if (msg != InventoryResult.Ok) {
            player.sendEquipError(msg, item);

            return;
        }

        if (collectionMgr.addToy(item.getEntry(), false, false)) {
            player.destroyItem(item.getBagSlot(), item.getSlot(), true);
        }
    }


    private void handleUseToy(UseToy packet) {
        var itemId = packet.cast.Misc[0];
        var item = global.getObjectMgr().getItemTemplate(itemId);

        if (item == null) {
            return;
        }

        if (!collectionMgr.hasToy(itemId)) {
            return;
        }

        var effect = tangible.ListHelper.find(item.getEffects(), eff -> packet.cast.spellID == eff.spellID);

        if (effect == null) {
            return;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(packet.cast.spellID, Difficulty.NONE);

        if (spellInfo == null) {
            Log.outError(LogFilter.Network, "HandleUseToy: unknown spell id: {0} used by Toy Item entry {1}", packet.cast.spellID, itemId);

            return;
        }

        if (player.isPossessing()) {
            return;
        }

        SpellCastTargets targets = new SpellCastTargets(player, packet.cast);

        Spell spell = new spell(player, spellInfo, TriggerCastFlags.NONE);

        SpellPrepare spellPrepare = new SpellPrepare();
        spellPrepare.clientCastID = packet.cast.castID;
        spellPrepare.serverCastID = spell.castId;
        sendPacket(spellPrepare);

        spell.fromClient = true;
        spell.castItemEntry = itemId;
        spell.spellMisc.data0 = packet.cast.Misc[0];
        spell.spellMisc.data1 = packet.cast.Misc[1];
        spell.castFlagsEx = SpellCastFlagsEx.forValue(spell.castFlagsEx.getValue() | SpellCastFlagsEx.UseToySpell.getValue());
        spell.prepare(targets);
    }


    private void handleToyClearFanfare(ToyClearFanfare toyClearFanfare) {
        collectionMgr.toyClearFanfare(toyClearFanfare.itemID);
    }

    public final void sendTradeStatus(TradeStatusPkt info) {
        info.clear(); // reuse packet
        var trader = getPlayer().getTrader();
        info.partnerIsSameBnetAccount = trader && trader.getSession().getBattlenetAccountId() == getBattlenetAccountId();
        sendPacket(info);
    }

    public final void sendUpdateTrade() {
        sendUpdateTrade(true);
    }

    public final void sendUpdateTrade(boolean trader_data) {
        var view_trade = trader_data ? getPlayer().getTradeData().getTraderData() : getPlayer().getTradeData();

        TradeUpdated tradeUpdated = new TradeUpdated();
        tradeUpdated.whichPlayer = (byte) (trader_data ? 1 : 0);
        tradeUpdated.clientStateIndex = view_trade.getClientStateIndex();
        tradeUpdated.currentStateIndex = view_trade.getServerStateIndex();
        tradeUpdated.gold = view_trade.getMoney();
        tradeUpdated.proposedEnchantment = (int) view_trade.getSpell();

        for (byte i = 0; i < (byte) TradeSlots.count.getValue(); ++i) {
            var item = view_trade.getItem(TradeSlots.forValue(i));

            if (item) {
                TradeUpdated.TradeItem tradeItem = new TradeUpdated.TradeItem();
                tradeItem.slot = i;
                tradeItem.item = new itemInstance(item);
                tradeItem.stackCount = (int) item.getCount();
                tradeItem.giftCreator = item.getGiftCreator();

                if (!item.isWrapped()) {
                    TradeUpdated.UnwrappedTradeItem unwrappedItem = new TradeUpdated.UnwrappedTradeItem();
                    unwrappedItem.enchantID = (int) item.getEnchantmentId(EnchantmentSlot.Perm);
                    unwrappedItem.onUseEnchantmentID = (int) item.getEnchantmentId(EnchantmentSlot.Use);
                    unwrappedItem.creator = item.getCreator();
                    unwrappedItem.charges = item.getSpellCharges();
                    unwrappedItem.lock = item.getTemplate().getLockID() != 0 && !item.hasItemFlag(ItemFieldFlags.unlocked);
                    unwrappedItem.maxDurability = item.getItemData().maxDurability;
                    unwrappedItem.durability = item.getItemData().durability;

                    tradeItem.unwrapped = unwrappedItem;

                    byte g = 0;

                    for (var gemData : item.getItemData().gems) {
                        if (gemData.itemId != 0) {
                            ItemGemData gem = new ItemGemData();
                            gem.slot = g;
                            gem.item = new itemInstance(gemData);
                            tradeItem.unwrapped.gems.add(gem);
                        }

                        ++g;
                    }
                }

                tradeUpdated.items.add(tradeItem);
            }
        }

        sendPacket(tradeUpdated);
    }

    public final void sendCancelTrade() {
        if (getPlayerRecentlyLoggedOut() || getPlayerLogout()) {
            return;
        }

        TradeStatusPkt info = new TradeStatusPkt();
        info.status = TradeStatus.Cancelled;
        sendTradeStatus(info);
    }


    private void handleIgnoreTradeOpcode(IgnoreTrade packet) {
    }


    private void handleBusyTradeOpcode(BusyTrade packet) {
    }

    private void moveItems(Item[] myItems, Item[] hisItems) {
        var trader = getPlayer().getTrader();

        if (!trader) {
            return;
        }

        for (byte i = 0; i < TradeSlots.TradedCount.getValue(); ++i) {
            ArrayList<ItemPosCount> traderDst = new ArrayList<>();
            ArrayList<ItemPosCount> playerDst = new ArrayList<>();
            var traderCanTrade = (myItems[i] == null || trader.canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, traderDst, myItems[i], false) == InventoryResult.Ok);
            var playerCanTrade = (hisItems[i] == null || getPlayer().canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, playerDst, hisItems[i], false) == InventoryResult.Ok);

            if (traderCanTrade && playerCanTrade) {
                // Ok, if trade item exists and can be stored
                // If we trade in both directions we had to check, if the trade will work before we actually do it
                // A roll back is not possible after we stored it
                if (myItems[i]) {
                    // logging
                    Log.outDebug(LogFilter.Network, "partner storing: {0}", myItems[i].getGUID().toString());

                    if (hasPermission(RBACPermissions.LogGmTrade)) {
                        Log.outCommand(player.getSession().getAccountId(), "GM {0} (Account: {1}) trade: {2} (Entry: {3} Count: {4}) to player: {5} (Account: {6})", getPlayer().getName(), getPlayer().getSession().getAccountId(), myItems[i].getTemplate().getName(), myItems[i].getEntry(), myItems[i].getCount(), trader.getName(), trader.getSession().getAccountId());
                    }

                    // adjust time (depends on /played)
                    if (myItems[i].isBOPTradeable()) {
                        myItems[i].setCreatePlayedTime(trader.getTotalPlayedTime() - (getPlayer().getTotalPlayedTime() - myItems[i].getItemData().createPlayedTime));
                    }

                    // store
                    trader.moveItemToInventory(traderDst, myItems[i], true, true);
                }

                if (hisItems[i]) {
                    // logging
                    Log.outDebug(LogFilter.Network, "player storing: {0}", hisItems[i].getGUID().toString());

                    if (hasPermission(RBACPermissions.LogGmTrade)) {
                        Log.outCommand(trader.getSession().getAccountId(), "GM {0} (Account: {1}) trade: {2} (Entry: {3} Count: {4}) to player: {5} (Account: {6})", trader.getName(), trader.getSession().getAccountId(), hisItems[i].getTemplate().getName(), hisItems[i].getEntry(), hisItems[i].getCount(), getPlayer().getName(), getPlayer().getSession().getAccountId());
                    }


                    // adjust time (depends on /played)
                    if (hisItems[i].isBOPTradeable()) {
                        hisItems[i].setCreatePlayedTime(getPlayer().getTotalPlayedTime() - (trader.getTotalPlayedTime() - hisItems[i].getItemData().createPlayedTime));
                    }

                    // store
                    getPlayer().moveItemToInventory(playerDst, hisItems[i], true, true);
                }
            } else {
                // in case of fatal error log error message
                // return the already removed items to the original owner
                if (myItems[i]) {
                    if (!traderCanTrade) {
                        Log.outError(LogFilter.Network, "trader can't store item: {0}", myItems[i].getGUID().toString());
                    }

                    if (getPlayer().canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, playerDst, myItems[i], false) == InventoryResult.Ok) {
                        getPlayer().moveItemToInventory(playerDst, myItems[i], true, true);
                    } else {
                        Log.outError(LogFilter.Network, "player can't take item back: {0}", myItems[i].getGUID().toString());
                    }
                }

                // return the already removed items to the original owner
                if (hisItems[i]) {
                    if (!playerCanTrade) {
                        Log.outError(LogFilter.Network, "player can't store item: {0}", hisItems[i].getGUID().toString());
                    }

                    if (trader.canStoreItem(ItemConst.NullBag, ItemConst.NullSlot, traderDst, hisItems[i], false) == InventoryResult.Ok) {
                        trader.moveItemToInventory(traderDst, hisItems[i], true, true);
                    } else {
                        Log.outError(LogFilter.Network, "trader can't take item back: {0}", hisItems[i].getGUID().toString());
                    }
                }
            }
        }
    }


    private void handleAcceptTrade(AcceptTrade acceptTrade) {
        var my_trade = getPlayer().getTradeData();

        if (my_trade == null) {
            return;
        }

        var trader = my_trade.getTrader();

        var his_trade = trader.getTradeData();

        if (his_trade == null) {
            return;
        }

        var myItems = new Item[TradeSlots.count.getValue()];
        var hisItems = new Item[TradeSlots.count.getValue()];

        // set before checks for propertly undo at problems (it already set in to client)
        my_trade.setAccepted(true);

        TradeStatusPkt info = new TradeStatusPkt();

        if (his_trade.getServerStateIndex() != acceptTrade.stateIndex) {
            info.status = TradeStatus.StateChanged;
            sendTradeStatus(info);
            my_trade.setAccepted(false);

            return;
        }

        if (!getPlayer().isWithinDistInMap(trader, 11.11f, false)) {
            info.status = TradeStatus.TooFarAway;
            sendTradeStatus(info);
            my_trade.setAccepted(false);

            return;
        }

        // not accept case incorrect money amount
        if (!getPlayer().hasEnoughMoney(my_trade.getMoney())) {
            info.status = TradeStatus.Failed;
            info.bagResult = InventoryResult.NotEnoughMoney;
            sendTradeStatus(info);
            my_trade.setAccepted(false, true);

            return;
        }

        // not accept case incorrect money amount
        if (!trader.hasEnoughMoney(his_trade.getMoney())) {
            info.status = TradeStatus.Failed;
            info.bagResult = InventoryResult.NotEnoughMoney;
            trader.getSession().sendTradeStatus(info);
            his_trade.setAccepted(false, true);

            return;
        }

        if (getPlayer().getMoney() >= PlayerConst.MaxMoneyAmount - his_trade.getMoney()) {
            info.status = TradeStatus.Failed;
            info.bagResult = InventoryResult.TooMuchGold;
            sendTradeStatus(info);
            my_trade.setAccepted(false, true);

            return;
        }

        if (trader.getMoney() >= PlayerConst.MaxMoneyAmount - my_trade.getMoney()) {
            info.status = TradeStatus.Failed;
            info.bagResult = InventoryResult.TooMuchGold;
            trader.getSession().sendTradeStatus(info);
            his_trade.setAccepted(false, true);

            return;
        }

        // not accept if some items now can't be trade (cheating)
        for (byte i = 0; i < (byte) TradeSlots.count.getValue(); ++i) {
            var item = my_trade.getItem(TradeSlots.forValue(i));

            if (item) {
                if (!item.canBeTraded(false, true)) {
                    info.status = TradeStatus.Cancelled;
                    sendTradeStatus(info);

                    return;
                }

                if (item.isBindedNotWith(trader)) {
                    info.status = TradeStatus.Failed;
                    info.bagResult = InventoryResult.TradeBoundItem;
                    sendTradeStatus(info);

                    return;
                }
            }

            item = his_trade.getItem(TradeSlots.forValue(i));

            if (item) {
                if (!item.canBeTraded(false, true)) {
                    info.status = TradeStatus.Cancelled;
                    sendTradeStatus(info);

                    return;
                }
            }
        }

        if (his_trade.isAccepted()) {
            setAcceptTradeMode(my_trade, his_trade, myItems, hisItems);

            Spell my_spell = null;
            SpellCastTargets my_targets = new SpellCastTargets();

            Spell his_spell = null;
            SpellCastTargets his_targets = new SpellCastTargets();

            // not accept if spell can't be casted now (cheating)
            var my_spell_id = my_trade.getSpell();

            if (my_spell_id != 0) {
                var spellEntry = global.getSpellMgr().getSpellInfo(my_spell_id, player.getMap().getDifficultyID());
                var castItem = my_trade.getSpellCastItem();

                if (spellEntry == null || !his_trade.getItem(TradeSlots.NonTraded) || (my_trade.hasSpellCastItem() && !castItem)) {
                    clearAcceptTradeMode(my_trade, his_trade);
                    clearAcceptTradeMode(myItems, hisItems);

                    my_trade.setSpell(0);

                    return;
                }

                my_spell = new spell(getPlayer(), spellEntry, TriggerCastFlags.FullMask);
                my_spell.castItem = castItem;
                my_targets.setTradeItemTarget(getPlayer());
                my_spell.targets = my_targets;

                var res = my_spell.checkCast(true);

                if (res != SpellCastResult.SpellCastOk) {
                    my_spell.sendCastResult(res);

                    clearAcceptTradeMode(my_trade, his_trade);
                    clearAcceptTradeMode(myItems, hisItems);

                    my_spell.close();
                    my_trade.setSpell(0);

                    return;
                }
            }

            // not accept if spell can't be casted now (cheating)
            var his_spell_id = his_trade.getSpell();

            if (his_spell_id != 0) {
                var spellEntry = global.getSpellMgr().getSpellInfo(his_spell_id, trader.getMap().getDifficultyID());
                var castItem = his_trade.getSpellCastItem();

                if (spellEntry == null || !my_trade.getItem(TradeSlots.NonTraded) || (his_trade.hasSpellCastItem() && !castItem)) {
                    his_trade.setSpell(0);

                    clearAcceptTradeMode(my_trade, his_trade);
                    clearAcceptTradeMode(myItems, hisItems);

                    return;
                }

                his_spell = new spell(trader, spellEntry, TriggerCastFlags.FullMask);
                his_spell.castItem = castItem;
                his_targets.setTradeItemTarget(trader);
                his_spell.targets = his_targets;

                var res = his_spell.checkCast(true);

                if (res != SpellCastResult.SpellCastOk) {
                    his_spell.sendCastResult(res);

                    clearAcceptTradeMode(my_trade, his_trade);
                    clearAcceptTradeMode(myItems, hisItems);

                    my_spell.close();
                    his_spell.close();

                    his_trade.setSpell(0);

                    return;
                }
            }

            // inform partner client
            info.status = TradeStatus.accepted;
            trader.getSession().sendTradeStatus(info);

            // test if item will fit in each inventory
            TradeStatusPkt myCanCompleteInfo = new TradeStatusPkt();
            TradeStatusPkt hisCanCompleteInfo = new TradeStatusPkt();
            tangible.RefObject<Integer> tempRef_ItemID = new tangible.RefObject<Integer>(hisCanCompleteInfo.itemID);
            hisCanCompleteInfo.bagResult = trader.canStoreItems(myItems, TradeSlots.TradedCount.getValue(), tempRef_ItemID);
            hisCanCompleteInfo.itemID = tempRef_ItemID.refArgValue;
            tangible.RefObject<Integer> tempRef_ItemID2 = new tangible.RefObject<Integer>(myCanCompleteInfo.itemID);
            myCanCompleteInfo.bagResult = getPlayer().canStoreItems(hisItems, TradeSlots.TradedCount.getValue(), tempRef_ItemID2);
            myCanCompleteInfo.itemID = tempRef_ItemID2.refArgValue;

            clearAcceptTradeMode(myItems, hisItems);

            // in case of missing space report error
            if (myCanCompleteInfo.bagResult != InventoryResult.Ok) {
                clearAcceptTradeMode(my_trade, his_trade);

                myCanCompleteInfo.status = TradeStatus.Failed;
                trader.getSession().sendTradeStatus(myCanCompleteInfo);
                myCanCompleteInfo.failureForYou = true;
                sendTradeStatus(myCanCompleteInfo);
                my_trade.setAccepted(false);
                his_trade.setAccepted(false);

                return;
            } else if (hisCanCompleteInfo.bagResult != InventoryResult.Ok) {
                clearAcceptTradeMode(my_trade, his_trade);

                hisCanCompleteInfo.status = TradeStatus.Failed;
                sendTradeStatus(hisCanCompleteInfo);
                hisCanCompleteInfo.failureForYou = true;
                trader.getSession().sendTradeStatus(hisCanCompleteInfo);
                my_trade.setAccepted(false);
                his_trade.setAccepted(false);

                return;
            }

            // execute trade: 1. remove
            for (byte i = 0; i < TradeSlots.TradedCount.getValue(); ++i) {
                if (myItems[i]) {
                    myItems[i].setGiftCreator(getPlayer().getGUID());
                    getPlayer().moveItemFromInventory(myItems[i].getBagSlot(), myItems[i].getSlot(), true);
                }

                if (hisItems[i]) {
                    hisItems[i].setGiftCreator(trader.getGUID());
                    trader.moveItemFromInventory(hisItems[i].getBagSlot(), hisItems[i].getSlot(), true);
                }
            }

            // execute trade: 2. store
            moveItems(myItems, hisItems);

            // logging money
            if (hasPermission(RBACPermissions.LogGmTrade)) {
                if (my_trade.getMoney() > 0) {
                    Log.outCommand(getPlayer().getSession().getAccountId(), "GM {0} (Account: {1}) give money (Amount: {2}) to player: {3} (Account: {4})", getPlayer().getName(), getPlayer().getSession().getAccountId(), my_trade.getMoney(), trader.getName(), trader.getSession().getAccountId());
                }

                if (his_trade.getMoney() > 0) {
                    Log.outCommand(getPlayer().getSession().getAccountId(), "GM {0} (Account: {1}) give money (Amount: {2}) to player: {3} (Account: {4})", trader.getName(), trader.getSession().getAccountId(), his_trade.getMoney(), getPlayer().getName(), getPlayer().getSession().getAccountId());
                }
            }


            // update money
            getPlayer().modifyMoney(-(long) my_trade.getMoney());
            getPlayer().modifyMoney((long) his_trade.getMoney());
            trader.modifyMoney(-(long) his_trade.getMoney());
            trader.modifyMoney((long) my_trade.getMoney());

            if (my_spell) {
                my_spell.prepare(my_targets);
            }

            if (his_spell) {
                his_spell.prepare(his_targets);
            }

            // cleanup
            clearAcceptTradeMode(my_trade, his_trade);
            getPlayer().setTradeData(null);
            trader.setTradeData(null);

            // desynchronized with the other saves here (saveInventoryAndGoldToDB() not have own transaction guards)
            SQLTransaction trans = new SQLTransaction();
            getPlayer().saveInventoryAndGoldToDB(trans);
            trader.saveInventoryAndGoldToDB(trans);
            DB.characters.CommitTransaction(trans);

            info.status = TradeStatus.Complete;
            trader.getSession().sendTradeStatus(info);
            sendTradeStatus(info);
        } else {
            info.status = TradeStatus.accepted;
            trader.getSession().sendTradeStatus(info);
        }
    }


    private void handleUnacceptTrade(UnacceptTrade packet) {
        var my_trade = getPlayer().getTradeData();

        if (my_trade == null) {
            return;
        }

        my_trade.setAccepted(false, true);
    }


    private void handleBeginTrade(BeginTrade packet) {
        var my_trade = getPlayer().getTradeData();

        if (my_trade == null) {
            return;
        }

        TradeStatusPkt info = new TradeStatusPkt();
        my_trade.getTrader().getSession().sendTradeStatus(info);
        sendTradeStatus(info);
    }


    private void handleCancelTrade(CancelTrade cancelTrade) {
        // sent also after LOGOUT COMPLETE
        if (getPlayer()) // needed because STATUS_LOGGEDIN_OR_RECENTLY_LOGGOUT
        {
            getPlayer().tradeCancel(true);
        }
    }


    private void handleInitiateTrade(InitiateTrade initiateTrade) {
        if (getPlayer().getTradeData() != null) {
            return;
        }

        TradeStatusPkt info = new TradeStatusPkt();

        if (!getPlayer().isAlive()) {
            info.status = TradeStatus.Dead;
            sendTradeStatus(info);

            return;
        }

        if (getPlayer().hasUnitState(UnitState.Stunned)) {
            info.status = TradeStatus.Stunned;
            sendTradeStatus(info);

            return;
        }

        if (isLogingOut()) {
            info.status = TradeStatus.LoggingOut;
            sendTradeStatus(info);

            return;
        }

        if (getPlayer().isInFlight()) {
            info.status = TradeStatus.TooFarAway;
            sendTradeStatus(info);

            return;
        }

        if (getPlayer().getLevel() < WorldConfig.getIntValue(WorldCfg.TradeLevelReq)) {
            sendNotification(global.getObjectMgr().getSysMessage(SysMessage.TradeReq), WorldConfig.getIntValue(WorldCfg.TradeLevelReq));
            info.status = TradeStatus.Failed;
            sendTradeStatus(info);

            return;
        }


        var pOther = global.getObjAccessor().findPlayer(initiateTrade.guid);

        if (!pOther) {
            info.status = TradeStatus.NoTarget;
            sendTradeStatus(info);

            return;
        }

        if (pOther == getPlayer() || pOther.getTradeData() != null) {
            info.status = TradeStatus.PlayerBusy;
            sendTradeStatus(info);

            return;
        }

        if (!pOther.isAlive()) {
            info.status = TradeStatus.TargetDead;
            sendTradeStatus(info);

            return;
        }

        if (pOther.isInFlight()) {
            info.status = TradeStatus.TooFarAway;
            sendTradeStatus(info);

            return;
        }

        if (pOther.hasUnitState(UnitState.Stunned)) {
            info.status = TradeStatus.TargetStunned;
            sendTradeStatus(info);

            return;
        }

        if (pOther.getSession().isLogingOut()) {
            info.status = TradeStatus.TargetLoggingOut;
            sendTradeStatus(info);

            return;
        }

        if (pOther.getSocial().hasIgnore(getPlayer().getGUID(), getPlayer().getSession().getAccountGUID())) {
            info.status = TradeStatus.PlayerIgnored;
            sendTradeStatus(info);

            return;
        }

        if ((pOther.getTeam() != getPlayer().getTeam() || pOther.hasPlayerFlagEx(playerFlagsEx.MercenaryMode) || getPlayer().hasPlayerFlagEx(playerFlagsEx.MercenaryMode)) && (!WorldConfig.getBoolValue(WorldCfg.AllowTwoSideTrade) && !hasPermission(RBACPermissions.AllowTwoSideTrade))) {
            info.status = TradeStatus.WrongFaction;
            sendTradeStatus(info);

            return;
        }

        if (!pOther.isWithinDistInMap(getPlayer(), 11.11f, false)) {
            info.status = TradeStatus.TooFarAway;
            sendTradeStatus(info);

            return;
        }

        if (pOther.getLevel() < WorldConfig.getIntValue(WorldCfg.TradeLevelReq)) {
            sendNotification(global.getObjectMgr().getSysMessage(SysMessage.TradeOtherReq), WorldConfig.getIntValue(WorldCfg.TradeLevelReq));
            info.status = TradeStatus.Failed;
            sendTradeStatus(info);

            return;
        }

        // OK start trade
        getPlayer().setTradeData(new TradeData(getPlayer(), pOther));
        pOther.setTradeData(new TradeData(pOther, getPlayer()));

        info.status = TradeStatus.Proposed;
        info.partner = getPlayer().getGUID();
        pOther.getSession().sendTradeStatus(info);
    }


    private void handleSetTradeGold(SetTradeGold setTradeGold) {
        var my_trade = getPlayer().getTradeData();

        if (my_trade == null) {
            return;
        }

        my_trade.updateClientStateIndex();
        my_trade.setMoney(setTradeGold.coinage);
    }


    private void handleSetTradeItem(SetTradeItem setTradeItem) {
        var my_trade = getPlayer().getTradeData();

        if (my_trade == null) {
            return;
        }

        TradeStatusPkt info = new TradeStatusPkt();

        // invalid slot number
        if (setTradeItem.tradeSlot >= (byte) TradeSlots.count.getValue()) {
            info.status = TradeStatus.Cancelled;
            sendTradeStatus(info);

            return;
        }

        // check cheating, can't fail with correct client operations
        var item = getPlayer().getItemByPos(setTradeItem.packSlot, setTradeItem.itemSlotInPack);

        if (!item || (setTradeItem.tradeSlot != (byte) TradeSlots.NonTraded.getValue() && !item.canBeTraded(false, true))) {
            info.status = TradeStatus.Cancelled;
            sendTradeStatus(info);

            return;
        }

        var iGUID = item.getGUID();

        // prevent place single item into many trade slots using cheating and client bugs
        if (my_trade.hasItem(iGUID)) {
            // cheating attempt
            info.status = TradeStatus.Cancelled;
            sendTradeStatus(info);

            return;
        }

        my_trade.updateClientStateIndex();

        if (setTradeItem.tradeSlot != (byte) TradeSlots.NonTraded.getValue() && item.isBindedNotWith(my_trade.getTrader())) {
            info.status = TradeStatus.NotOnTaplist;
            info.tradeSlot = setTradeItem.tradeSlot;
            sendTradeStatus(info);

            return;
        }

        my_trade.setItem(TradeSlots.forValue(setTradeItem.tradeSlot), item);
    }


    private void handleClearTradeItem(ClearTradeItem clearTradeItem) {
        var my_trade = getPlayer().getTradeData();

        if (my_trade == null) {
            return;
        }

        my_trade.updateClientStateIndex();

        // invalid slot number
        if (clearTradeItem.tradeSlot >= (byte) TradeSlots.count.getValue()) {
            return;
        }

        my_trade.setItem(TradeSlots.forValue(clearTradeItem.tradeSlot), null);
    }


    private void handleSetTradeCurrency(SetTradeCurrency setTradeCurrency) {
    }


    private void handleTraitsCommitConfig(TraitsCommitConfig traitsCommitConfig) {
        var configId = traitsCommitConfig.config.ID;
        var existingConfig = player.getTraitConfig(configId);

        if (existingConfig == null) {
            sendPacket(new TraitConfigCommitFailed(configId, 0, TalentLearnResult.FailedUnknown.getValue()));

            return;
        }

        if (player.isInCombat()) {
            sendPacket(new TraitConfigCommitFailed(configId, 0, TalentLearnResult.FailedAffectingCombat.getValue()));

            return;
        }

        if (player.getBattleground() && player.getBattleground().getStatus() == BattlegroundStatus.inProgress) {
            sendPacket(new TraitConfigCommitFailed(configId, 0, TalentLearnResult.InPvpMatch.getValue()));

            return;
        }

        var hasRemovedEntries = false;
        TraitConfigPacket newConfigState = new traitConfigPacket(existingConfig);

        for (var kvp : traitsCommitConfig.config.entries.values()) {
            for (var newEntry : kvp.VALUES) {
                var traitEntry = newConfigState.entries.get(newEntry.traitNodeID) == null ? null : newConfigState.entries.get(newEntry.traitNodeID).get(newEntry.traitNodeEntryID);

                if (traitEntry == null) {
                    newConfigState.addEntry(newEntry);

                    continue;
                }

                if (traitEntry.rank > newEntry.rank) {
                    var traitNode = CliDB.TraitNodeStorage.get(newEntry.traitNodeID);

                    if (traitNode == null) {
                        sendPacket(new TraitConfigCommitFailed(configId, 0, TalentLearnResult.FailedUnknown.getValue()));

                        return;
                    }

                    var traitTree = CliDB.TraitTreeStorage.get(traitNode.TraitTreeID);

                    if (traitTree == null) {
                        sendPacket(new TraitConfigCommitFailed(configId, 0, TalentLearnResult.FailedUnknown.getValue()));

                        return;
                    }

                    if (traitTree.getFlags().hasFlag(TraitTreeFlag.CannotRefund)) {
                        sendPacket(new TraitConfigCommitFailed(configId, 0, TalentLearnResult.FailedCantRemoveTalent.getValue()));

                        return;
                    }

                    var traitNodeEntry = CliDB.TraitNodeEntryStorage.get(newEntry.traitNodeEntryID);

                    if (traitNodeEntry == null) {
                        sendPacket(new TraitConfigCommitFailed(configId, 0, TalentLearnResult.FailedUnknown.getValue()));

                        return;
                    }

                    var traitDefinition = CliDB.TraitDefinitionStorage.get(traitNodeEntry.traitDefinitionID);

                    if (traitDefinition == null) {
                        sendPacket(new TraitConfigCommitFailed(configId, 0, TalentLearnResult.FailedUnknown.getValue()));

                        return;
                    }

                    if (traitDefinition.spellID != 0 && player.getSpellHistory().hasCooldown(traitDefinition.spellID)) {
                        sendPacket(new TraitConfigCommitFailed(configId, traitDefinition.spellID, TalentLearnResult.FailedCantRemoveTalent.getValue()));

                        return;
                    }

                    if (traitDefinition.VisibleSpellID != 0 && player.getSpellHistory().hasCooldown((int) traitDefinition.VisibleSpellID)) {
                        sendPacket(new TraitConfigCommitFailed(configId, traitDefinition.VisibleSpellID, TalentLearnResult.FailedCantRemoveTalent.getValue()));

                        return;
                    }

                    hasRemovedEntries = true;
                }

                if (newEntry.rank != 0) {
                    traitEntry.rank = newEntry.rank;
                } else {
                    newConfigState.entries.remove(traitEntry.traitNodeID);
                }
            }
        }

        var validationResult = TraitMgr.validateConfig(newConfigState, player, true);

        if (validationResult != TalentLearnResult.LearnOk) {
            sendPacket(new TraitConfigCommitFailed(configId, 0, validationResult.getValue()));

            return;
        }

        var needsCastTime = newConfigState.type == TraitConfigType.Combat && hasRemovedEntries;

        if (traitsCommitConfig.savedLocalIdentifier != 0) {
            newConfigState.localIdentifier = traitsCommitConfig.savedLocalIdentifier;
        } else {
            var savedConfig = player.getTraitConfig(traitsCommitConfig.savedLocalIdentifier);

            if (savedConfig != null) {
                newConfigState.localIdentifier = savedConfig.localIdentifier;
            }
        }

        player.updateTraitConfig(newConfigState, traitsCommitConfig.savedConfigID, needsCastTime);
    }


    private void handleClassTalentsRequestNewConfig(ClassTalentsRequestNewConfig classTalentsRequestNewConfig) {
        if (classTalentsRequestNewConfig.config.type != TraitConfigType.Combat) {
            return;
        }

        if ((classTalentsRequestNewConfig.config.combatConfigFlags.getValue() & TraitCombatConfigFlags.ActiveForSpec.getValue()) != TraitCombatConfigFlags.NONE.getValue()) {
            return;
        }

        long configCount = player.getActivePlayerData().traitConfigs.getValues().size() (traitConfig ->
        {
            return TraitConfigType.forValue((int) traitConfig.type) == TraitConfigType.Combat && (TraitCombatConfigFlags.forValue((int) traitConfig.combatConfigFlags).getValue() & TraitCombatConfigFlags.ActiveForSpec.getValue()) == TraitCombatConfigFlags.NONE.getValue();
        });

        if (configCount >= TraitMgr.MAX_COMBAT_TRAIT_CONFIGS) {
            return;
        }


//		int findFreeLocalIdentifier()
//			{
//				var index = 1;
//
//				while (player.activePlayerData.traitConfigs.FindIndexIf(traitConfig => { return(TraitConfigType)(int)traitConfig.type == TraitConfigType.Combat && traitConfig.chrSpecializationID == player.getPrimarySpecialization() && traitConfig.localIdentifier == index; }) >= 0)
//					++index;
//
//				return index;
//			}

        classTalentsRequestNewConfig.config.chrSpecializationID = (int) player.getPrimarySpecialization();
        classTalentsRequestNewConfig.config.localIdentifier = findFreeLocalIdentifier();

        for (var grantedEntry : TraitMgr.getGrantedTraitEntriesForConfig(classTalentsRequestNewConfig.config, player)) {
            var newEntry = classTalentsRequestNewConfig.config.entries.get(grantedEntry.traitNodeID) == null ? null : classTalentsRequestNewConfig.config.entries.get(grantedEntry.traitNodeID).get(grantedEntry.traitNodeEntryID);

            if (newEntry == null) {
                newEntry = new TraitEntryPacket();
                classTalentsRequestNewConfig.config.addEntry(newEntry);
            }

            newEntry.traitNodeID = grantedEntry.traitNodeID;
            newEntry.traitNodeEntryID = grantedEntry.traitNodeEntryID;
            newEntry.rank = grantedEntry.rank;
            newEntry.grantedRanks = grantedEntry.grantedRanks;

            var traitNodeEntry = CliDB.TraitNodeEntryStorage.get(grantedEntry.traitNodeEntryID);

            if (traitNodeEntry != null) {
                if (newEntry.rank + newEntry.grantedRanks > traitNodeEntry.MaxRanks) {
                    newEntry.rank = Math.max(0, traitNodeEntry.MaxRanks - newEntry.grantedRanks);
                }
            }
        }

        var validationResult = TraitMgr.validateConfig(classTalentsRequestNewConfig.config, player);

        if (validationResult != TalentLearnResult.LearnOk) {
            return;
        }

        player.createTraitConfig(classTalentsRequestNewConfig.config);
    }


    private void handleClassTalentsRenameConfig(ClassTalentsRenameConfig classTalentsRenameConfig) {
        player.renameTraitConfig(classTalentsRenameConfig.configID, classTalentsRenameConfig.name);
    }


    private void handleClassTalentsDeleteConfig(ClassTalentsDeleteConfig classTalentsDeleteConfig) {
        player.deleteTraitConfig(classTalentsDeleteConfig.configID);
    }


    private void handleClassTalentsSetStarterBuildActive(ClassTalentsSetStarterBuildActive classTalentsSetStarterBuildActive) {
        var traitConfig = player.getTraitConfig(classTalentsSetStarterBuildActive.configID);

        if (traitConfig == null) {
            return;
        }

        if (TraitConfigType.forValue((int) traitConfig.type) != TraitConfigType.Combat) {
            return;
        }

        if (!(TraitCombatConfigFlags.forValue((int) traitConfig.combatConfigFlags)).hasFlag(TraitCombatConfigFlags.ActiveForSpec)) {
            return;
        }

        if (classTalentsSetStarterBuildActive.active) {
            TraitConfigPacket newConfigState = new traitConfigPacket(traitConfig);

            var freeLocalIdentifier = 1;

            while (player.getActivePlayerData().traitConfigs.FindIndexIf(traitConfig ->
            {
                return TraitConfigType.forValue((int) traitConfig.type) == TraitConfigType.Combat && traitConfig.chrSpecializationID == player.getPrimarySpecialization() && traitConfig.localIdentifier == freeLocalIdentifier;
            }) >= 0) {
                ++freeLocalIdentifier;
            }

            TraitMgr.initializeStarterBuildTraitConfig(newConfigState, player);
            newConfigState.combatConfigFlags = TraitCombatConfigFlags.forValue(newConfigState.combatConfigFlags.getValue() | TraitCombatConfigFlags.StarterBuild.getValue());
            newConfigState.localIdentifier = freeLocalIdentifier;

            player.updateTraitConfig(newConfigState, 0, true);
        } else {
            player.setTraitConfigUseStarterBuild(classTalentsSetStarterBuildActive.configID, false);
        }
    }


    private void handleClassTalentsSetUsesSharedActionBars(ClassTalentsSetUsesSharedActionBars classTalentsSetUsesSharedActionBars) {
        player.setTraitConfigUseSharedActionBars(classTalentsSetUsesSharedActionBars.configID, classTalentsSetUsesSharedActionBars.usesShared, classTalentsSetUsesSharedActionBars.isLastSelectedSavedConfig);
    }

    public final void sendOpenTransmogrifier(ObjectGuid guid) {
        NPCInteractionOpenResult npcInteraction = new NPCInteractionOpenResult();
        npcInteraction.npc = guid;
        npcInteraction.interactionType = PlayerInteractionType.Transmogrifier;
        npcInteraction.success = true;
        sendPacket(npcInteraction);
    }


    private void handleTransmogrifyItems(TransmogrifyItems transmogrifyItems) {
        var player = getPlayer();

        // Validate
        if (!player.getNPCIfCanInteractWith(transmogrifyItems.npc, NPCFlags.Transmogrifier, NPCFlags2.NONE)) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleTransmogrifyItems - unit (GUID: {0}) not found or player can't interact with it.", transmogrifyItems.toString());

            return;
        }

        long cost = 0;
        HashMap<item, int[]> transmogItems = new HashMap<item, int[]>(); // new Dictionary<item, Tuple<uint, uint>>();
        HashMap<item, Integer> illusionItems = new HashMap<item, Integer>();

        ArrayList<item> resetAppearanceItems = new ArrayList<>();
        ArrayList<item> resetIllusionItems = new ArrayList<>();
        ArrayList<Integer> bindAppearances = new ArrayList<>();


//		bool validateAndStoreTransmogItem(Item itemTransmogrified, uint itemModifiedAppearanceId, bool isSecondary)
//			{
//				var itemModifiedAppearance = CliDB.ItemModifiedAppearanceStorage.get(itemModifiedAppearanceId);
//
//				if (itemModifiedAppearance == null)
//				{
//					Log.outDebug(LogFilter.Network, string.format("WORLD: HandleTransmogrifyItems - {0}, Name: {1} tried to transmogrify using invalid appearance ({2}).", player.GUID, player.getName(), itemModifiedAppearanceId));
//
//					return false;
//				}
//
//				if (isSecondary && itemTransmogrified.template.inventoryType != inventoryType.Shoulders)
//				{
//					Log.outDebug(LogFilter.Network, string.format("WORLD: HandleTransmogrifyItems - {0}, Name: {1} tried to transmogrify secondary appearance to non-shoulder item.", player.GUID, player.getName()));
//
//					return false;
//				}
//
//				bool hasAppearance, isTemporary;
//				(hasAppearance, isTemporary) = CollectionMgr.hasItemAppearance(itemModifiedAppearanceId);
//
//				if (!hasAppearance)
//				{
//					Log.outDebug(LogFilter.Network, string.format("WORLD: HandleTransmogrifyItems - {0}, Name: {1} tried to transmogrify using appearance he has not collected ({2}).", player.GUID, player.getName(), itemModifiedAppearanceId));
//
//					return false;
//				}
//
//				var itemTemplate = global.ObjectMgr.getItemTemplate(itemModifiedAppearance.itemID);
//
//				if (player.canUseItem(itemTemplate) != InventoryResult.Ok)
//				{
//					Log.outDebug(LogFilter.Network, string.format("WORLD: HandleTransmogrifyItems - {0}, Name: {1} tried to transmogrify using appearance he can never use ({2}).", player.GUID, player.getName(), itemModifiedAppearanceId));
//
//					return false;
//				}
//
//				// validity of the transmogrification items
//				if (!item.canTransmogrifyItemWithItem(itemTransmogrified, itemModifiedAppearance))
//				{
//					Log.outDebug(LogFilter.Network, string.format("WORLD: HandleTransmogrifyItems - {0}, Name: {1} failed CanTransmogrifyItemWithItem ({2} with appearance {3}).", player.GUID, player.getName(), itemTransmogrified.entry, itemModifiedAppearanceId));
//
//					return false;
//				}
//
//				if (!transmogItems.ContainsKey(itemTransmogrified))
//					transmogItems[itemTransmogrified] = new uint[2];
//
//				if (!isSecondary)
//					transmogItems[itemTransmogrified][0] = itemModifiedAppearanceId;
//				else
//					transmogItems[itemTransmogrified][1] = itemModifiedAppearanceId;
//
//				if (isTemporary)
//					bindAppearances.add(itemModifiedAppearanceId);
//
//				return true;
//			}

        ;

        for (var transmogItem : transmogrifyItems.items) {
            // slot of the transmogrified item
            if (transmogItem.slot >= EquipmentSlot.End) {
                Log.outDebug(LogFilter.Network, "WORLD: HandleTransmogrifyItems - player ({0}, name: {1}) tried to transmogrify wrong slot {2} when transmogrifying items.", player.getGUID().toString(), player.getName(), transmogItem.slot);

                return;
            }

            // transmogrified item
            var itemTransmogrified = player.getItemByPos(InventorySlots.Bag0, (byte) transmogItem.slot);

            if (!itemTransmogrified) {
                Log.outDebug(LogFilter.Network, "WORLD: HandleTransmogrifyItems - player (GUID: {0}, name: {1}) tried to transmogrify an invalid item in a valid slot (slot: {2}).", player.getGUID().toString(), player.getName(), transmogItem.slot);

                return;
            }

            if (transmogItem.itemModifiedAppearanceID != 0 || transmogItem.secondaryItemModifiedAppearanceID > 0) {
                if (transmogItem.itemModifiedAppearanceID != 0 && !validateAndStoreTransmogItem(itemTransmogrified, (int) transmogItem.itemModifiedAppearanceID, false)) {
                    return;
                }

                if (transmogItem.secondaryItemModifiedAppearanceID > 0 && !validateAndStoreTransmogItem(itemTransmogrified, (int) transmogItem.secondaryItemModifiedAppearanceID, true)) {
                    return;
                }

                // add cost
                cost += itemTransmogrified.getSellPrice(player);
            } else {
                resetAppearanceItems.add(itemTransmogrified);
            }

            if (transmogItem.spellItemEnchantmentID != 0) {
                if (transmogItem.slot != EquipmentSlot.MainHand && transmogItem.slot != EquipmentSlot.OffHand) {
                    Log.outDebug(LogFilter.Network, "WORLD: HandleTransmogrifyItems - {0}, Name: {1} tried to transmogrify illusion into non-weapon slot ({2}).", player.getGUID().toString(), player.getName(), transmogItem.slot);

                    return;
                }

                var illusion = global.getDB2Mgr().GetTransmogIllusionForEnchantment((int) transmogItem.spellItemEnchantmentID);

                if (illusion == null) {
                    Log.outDebug(LogFilter.Network, "WORLD: HandleTransmogrifyItems - {0}, Name: {1} tried to transmogrify illusion using invalid enchant ({2}).", player.getGUID().toString(), player.getName(), transmogItem.spellItemEnchantmentID);

                    return;
                }

                var condition = CliDB.PlayerConditionStorage.get(illusion.UnlockConditionID);

                if (condition != null) {
                    if (!ConditionManager.isPlayerMeetingCondition(player, condition)) {
                        Log.outDebug(LogFilter.Network, "WORLD: HandleTransmogrifyItems - {0}, Name: {1} tried to transmogrify illusion using not allowed enchant ({2}).", player.getGUID().toString(), player.getName(), transmogItem.spellItemEnchantmentID);

                        return;
                    }
                }

                illusionItems.put(itemTransmogrified, (int) transmogItem.spellItemEnchantmentID);
                cost += illusion.TransmogCost;
            } else {
                resetIllusionItems.add(itemTransmogrified);
            }
        }

        if (!player.hasAuraType(AuraType.RemoveTransmogCost) && cost != 0) // 0 cost if reverting look
        {
            if (!player.hasEnoughMoney(cost)) {
                return;
            }

            player.modifyMoney(-cost);
        }

        // Everything is fine, proceed
        for (var transmogPair : transmogItems.entrySet()) {
            var transmogrified = transmogPair.getKey();

            if (!transmogrifyItems.currentSpecOnly) {
                transmogrified.setModifier(ItemModifier.TransmogAppearanceAllSpecs, transmogPair.getValue()[0]);
                transmogrified.setModifier(ItemModifier.TransmogAppearanceSpec1, 0);
                transmogrified.setModifier(ItemModifier.TransmogAppearanceSpec2, 0);
                transmogrified.setModifier(ItemModifier.TransmogAppearanceSpec3, 0);
                transmogrified.setModifier(ItemModifier.TransmogAppearanceSpec4, 0);

                transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs, transmogPair.getValue()[1]);
                transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec1, 0);
                transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec2, 0);
                transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec3, 0);
                transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec4, 0);
            } else {
                if (transmogrified.getModifier(ItemModifier.TransmogAppearanceSpec1) == 0) {
                    transmogrified.setModifier(ItemModifier.TransmogAppearanceSpec1, transmogrified.getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.TransmogAppearanceSpec2) == 0) {
                    transmogrified.setModifier(ItemModifier.TransmogAppearanceSpec2, transmogrified.getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.TransmogAppearanceSpec3) == 0) {
                    transmogrified.setModifier(ItemModifier.TransmogAppearanceSpec3, transmogrified.getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.TransmogAppearanceSpec4) == 0) {
                    transmogrified.setModifier(ItemModifier.TransmogAppearanceSpec4, transmogrified.getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.TransmogSecondaryAppearanceSpec1) == 0) {
                    transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec1, transmogrified.getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.TransmogSecondaryAppearanceSpec2) == 0) {
                    transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec2, transmogrified.getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.TransmogSecondaryAppearanceSpec3) == 0) {
                    transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec3, transmogrified.getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.TransmogSecondaryAppearanceSpec4) == 0) {
                    transmogrified.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec4, transmogrified.getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                }

                transmogrified.setModifier(ItemConst.AppearanceModifierSlotBySpec[player.getActiveTalentGroup()], transmogPair.getValue()[0]);
                transmogrified.setModifier(ItemConst.SecondaryAppearanceModifierSlotBySpec[player.getActiveTalentGroup()], transmogPair.getValue()[1]);
            }

            player.setVisibleItemSlot(transmogrified.slot, transmogrified);

            transmogrified.setNotRefundable(player);
            transmogrified.clearSoulboundTradeable(player);
            transmogrified.setState(ItemUpdateState.changed, player);
        }

        for (var illusionPair : illusionItems.entrySet()) {
            var transmogrified = illusionPair.getKey();

            if (!transmogrifyItems.currentSpecOnly) {
                transmogrified.setModifier(ItemModifier.EnchantIllusionAllSpecs, illusionPair.getValue());
                transmogrified.setModifier(ItemModifier.EnchantIllusionSpec1, 0);
                transmogrified.setModifier(ItemModifier.EnchantIllusionSpec2, 0);
                transmogrified.setModifier(ItemModifier.EnchantIllusionSpec3, 0);
                transmogrified.setModifier(ItemModifier.EnchantIllusionSpec4, 0);
            } else {
                if (transmogrified.getModifier(ItemModifier.EnchantIllusionSpec1) == 0) {
                    transmogrified.setModifier(ItemModifier.EnchantIllusionSpec1, transmogrified.getModifier(ItemModifier.EnchantIllusionAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.EnchantIllusionSpec2) == 0) {
                    transmogrified.setModifier(ItemModifier.EnchantIllusionSpec2, transmogrified.getModifier(ItemModifier.EnchantIllusionAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.EnchantIllusionSpec3) == 0) {
                    transmogrified.setModifier(ItemModifier.EnchantIllusionSpec3, transmogrified.getModifier(ItemModifier.EnchantIllusionAllSpecs));
                }

                if (transmogrified.getModifier(ItemModifier.EnchantIllusionSpec4) == 0) {
                    transmogrified.setModifier(ItemModifier.EnchantIllusionSpec4, transmogrified.getModifier(ItemModifier.EnchantIllusionAllSpecs));
                }

                transmogrified.setModifier(ItemConst.IllusionModifierSlotBySpec[player.getActiveTalentGroup()], illusionPair.getValue());
            }

            player.setVisibleItemSlot(transmogrified.slot, transmogrified);

            transmogrified.setNotRefundable(player);
            transmogrified.clearSoulboundTradeable(player);
            transmogrified.setState(ItemUpdateState.changed, player);
        }

        for (var item : resetAppearanceItems) {
            if (!transmogrifyItems.currentSpecOnly) {
                item.setModifier(ItemModifier.TransmogAppearanceAllSpecs, 0);
                item.setModifier(ItemModifier.TransmogAppearanceSpec1, 0);
                item.setModifier(ItemModifier.TransmogAppearanceSpec2, 0);
                item.setModifier(ItemModifier.TransmogAppearanceSpec3, 0);
                item.setModifier(ItemModifier.TransmogAppearanceSpec4, 0);

                item.setModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs, 0);
                item.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec1, 0);
                item.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec2, 0);
                item.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec3, 0);
                item.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec4, 0);
            } else {
                if (item.getModifier(ItemModifier.TransmogAppearanceSpec1) == 0) {
                    item.setModifier(ItemModifier.TransmogAppearanceSpec1, item.getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                }

                if (item.getModifier(ItemModifier.TransmogAppearanceSpec2) == 0) {
                    item.setModifier(ItemModifier.TransmogAppearanceSpec2, item.getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                }

                if (item.getModifier(ItemModifier.TransmogAppearanceSpec2) == 0) {
                    item.setModifier(ItemModifier.TransmogAppearanceSpec3, item.getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                }

                if (item.getModifier(ItemModifier.TransmogAppearanceSpec4) == 0) {
                    item.setModifier(ItemModifier.TransmogAppearanceSpec4, item.getModifier(ItemModifier.TransmogAppearanceAllSpecs));
                }

                if (item.getModifier(ItemModifier.TransmogSecondaryAppearanceSpec1) == 0) {
                    item.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec1, item.getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                }

                if (item.getModifier(ItemModifier.TransmogSecondaryAppearanceSpec2) == 0) {
                    item.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec2, item.getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                }

                if (item.getModifier(ItemModifier.TransmogSecondaryAppearanceSpec3) == 0) {
                    item.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec3, item.getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                }

                if (item.getModifier(ItemModifier.TransmogSecondaryAppearanceSpec4) == 0) {
                    item.setModifier(ItemModifier.TransmogSecondaryAppearanceSpec4, item.getModifier(ItemModifier.TransmogSecondaryAppearanceAllSpecs));
                }

                item.setModifier(ItemConst.AppearanceModifierSlotBySpec[player.getActiveTalentGroup()], 0);
                item.setModifier(ItemConst.SecondaryAppearanceModifierSlotBySpec[player.getActiveTalentGroup()], 0);
                item.setModifier(ItemModifier.EnchantIllusionAllSpecs, 0);
            }

            item.setState(ItemUpdateState.changed, player);
            player.setVisibleItemSlot(item.getSlot(), item);
        }

        for (var item : resetIllusionItems) {
            if (!transmogrifyItems.currentSpecOnly) {
                item.setModifier(ItemModifier.EnchantIllusionAllSpecs, 0);
                item.setModifier(ItemModifier.EnchantIllusionSpec1, 0);
                item.setModifier(ItemModifier.EnchantIllusionSpec2, 0);
                item.setModifier(ItemModifier.EnchantIllusionSpec3, 0);
                item.setModifier(ItemModifier.EnchantIllusionSpec4, 0);
            } else {
                if (item.getModifier(ItemModifier.EnchantIllusionSpec1) == 0) {
                    item.setModifier(ItemModifier.EnchantIllusionSpec1, item.getModifier(ItemModifier.EnchantIllusionAllSpecs));
                }

                if (item.getModifier(ItemModifier.EnchantIllusionSpec2) == 0) {
                    item.setModifier(ItemModifier.EnchantIllusionSpec2, item.getModifier(ItemModifier.EnchantIllusionAllSpecs));
                }

                if (item.getModifier(ItemModifier.EnchantIllusionSpec3) == 0) {
                    item.setModifier(ItemModifier.EnchantIllusionSpec3, item.getModifier(ItemModifier.EnchantIllusionAllSpecs));
                }

                if (item.getModifier(ItemModifier.EnchantIllusionSpec4) == 0) {
                    item.setModifier(ItemModifier.EnchantIllusionSpec4, item.getModifier(ItemModifier.EnchantIllusionAllSpecs));
                }

                item.setModifier(ItemConst.IllusionModifierSlotBySpec[player.getActiveTalentGroup()], 0);
                item.setModifier(ItemModifier.TransmogAppearanceAllSpecs, 0);
            }

            item.setState(ItemUpdateState.changed, player);
            player.setVisibleItemSlot(item.getSlot(), item);
        }

        for (var itemModifedAppearanceId : bindAppearances) {
            var itemsProvidingAppearance = getCollectionMgr().getItemsProvidingTemporaryAppearance(itemModifedAppearanceId);

            for (var itemGuid : itemsProvidingAppearance) {
                var item = player.getItemByGuid(itemGuid);

                if (item) {
                    item.setNotRefundable(player);
                    item.clearSoulboundTradeable(player);
                    getCollectionMgr().addItemAppearance(item);
                }
            }
        }
    }


    private void handleMoveDismissVehicle(MoveDismissVehicle packet) {
        var vehicleGUID = getPlayer().getCharmedGUID();

        if (vehicleGUID.isEmpty()) // something wrong here...
        {
            return;
        }

        getPlayer().validateMovementInfo(packet.status);
        getPlayer().setMovementInfo(packet.status);

        getPlayer().exitVehicle();
    }


    private void handleRequestVehiclePrevSeat(RequestVehiclePrevSeat packet) {
        var vehicle_base = getPlayer().getVehicleBase();

        if (!vehicle_base) {
            return;
        }

        var seat = getPlayer().getVehicle1().GetSeatForPassenger(getPlayer());

        if (!seat.CanSwitchFromSeat()) {
            Log.outError(LogFilter.Network, "HandleRequestVehiclePrevSeat: {0} tried to switch seats but current seatflags {1} don't permit that.", getPlayer().getGUID().toString(), seat.flags);

            return;
        }

        getPlayer().changeSeat((byte) -1, false);
    }


    private void handleRequestVehicleNextSeat(RequestVehicleNextSeat packet) {
        var vehicle_base = getPlayer().getVehicleBase();

        if (!vehicle_base) {
            return;
        }

        var seat = getPlayer().getVehicle1().GetSeatForPassenger(getPlayer());

        if (!seat.CanSwitchFromSeat()) {
            Log.outError(LogFilter.Network, "HandleRequestVehicleNextSeat: {0} tried to switch seats but current seatflags {1} don't permit that.", getPlayer().getGUID().toString(), seat.flags);

            return;
        }

        getPlayer().changeSeat((byte) -1, true);
    }


    private void handleMoveChangeVehicleSeats(MoveChangeVehicleSeats packet) {
        var vehicle_base = getPlayer().getVehicleBase();

        if (!vehicle_base) {
            return;
        }

        var seat = getPlayer().getVehicle1().GetSeatForPassenger(getPlayer());

        if (!seat.CanSwitchFromSeat()) {
            Log.outError(LogFilter.Network, "HandleMoveChangeVehicleSeats, {0} tried to switch seats but current seatflags {1} don't permit that.", getPlayer().getGUID().toString(), seat.flags);

            return;
        }

        getPlayer().validateMovementInfo(packet.status);

        if (ObjectGuid.opNotEquals(vehicle_base.getGUID(), packet.status.getGuid())) {
            return;
        }

        vehicle_base.setMovementInfo(packet.status);

        if (packet.dstVehicle.isEmpty()) {
            getPlayer().changeSeat((byte) -1, packet.dstSeatIndex != 255);
        } else {
            var vehUnit = global.getObjAccessor().GetUnit(getPlayer(), packet.dstVehicle);

            if (vehUnit) {
                var vehicle = vehUnit.getVehicleKit();

                if (vehicle) {
                    if (vehicle.HasEmptySeat((byte) packet.dstSeatIndex)) {
                        vehUnit.handleSpellClick(getPlayer(), (byte) packet.dstSeatIndex);
                    }
                }
            }
        }
    }


    private void handleRequestVehicleSwitchSeat(RequestVehicleSwitchSeat packet) {
        var vehicle_base = getPlayer().getVehicleBase();

        if (!vehicle_base) {
            return;
        }

        var seat = getPlayer().getVehicle1().GetSeatForPassenger(getPlayer());

        if (!seat.CanSwitchFromSeat()) {
            Log.outError(LogFilter.Network, "HandleRequestVehicleSwitchSeat: {0} tried to switch seats but current seatflags {1} don't permit that.", getPlayer().getGUID().toString(), seat.flags);

            return;
        }

        if (Objects.equals(vehicle_base.getGUID(), packet.vehicle)) {
            getPlayer().changeSeat((byte) packet.seatIndex);
        } else {
            var vehUnit = global.getObjAccessor().GetUnit(getPlayer(), packet.vehicle);

            if (vehUnit) {
                var vehicle = vehUnit.getVehicleKit();

                if (vehicle) {
                    if (vehicle.HasEmptySeat((byte) packet.seatIndex)) {
                        vehUnit.handleSpellClick(getPlayer(), (byte) packet.seatIndex);
                    }
                }
            }
        }
    }


    private void handleRideVehicleInteract(RideVehicleInteract packet) {
        var player = global.getObjAccessor().getPlayer(player, packet.vehicle);

        if (player) {
            if (!player.VehicleKit1) {
                return;
            }

            if (!player.isInRaidWith(getPlayer())) {
                return;
            }

            if (!player.isWithinDistInMap(getPlayer(), SharedConst.InteractionDistance)) {
                return;
            }

            // Dont' allow players to enter player vehicle on arena
            if (!player.getMap() || player.getMap().isBattleArena()) {
                return;
            }

            getPlayer().enterVehicle(player);
        }
    }


    private void handleEjectPassenger(EjectPassenger packet) {
        var vehicle = getPlayer().getVehicleKit();

        if (!vehicle) {
            Log.outError(LogFilter.Network, "HandleEjectPassenger: {0} is not in a vehicle!", getPlayer().getGUID().toString());

            return;
        }

        if (packet.passenger.isUnit()) {
            var unit = global.getObjAccessor().GetUnit(getPlayer(), packet.passenger);

            if (!unit) {
                Log.outError(LogFilter.Network, "{0} tried to eject {1} from vehicle, but the latter was not found in world!", getPlayer().getGUID().toString(), packet.passenger.toString());

                return;
            }

            if (!unit.isOnVehicle(vehicle.GetBase())) {
                Log.outError(LogFilter.Network, "{0} tried to eject {1}, but they are not in the same vehicle", getPlayer().getGUID().toString(), packet.passenger.toString());

                return;
            }

            var seat = vehicle.GetSeatForPassenger(unit);

            if (seat.IsEjectable()) {
                unit.exitVehicle();
            } else {
                Log.outError(LogFilter.Network, "{0} attempted to eject {1} from non-ejectable seat.", getPlayer().getGUID().toString(), packet.passenger.toString());
            }
        } else {
            Log.outError(LogFilter.Network, "HandleEjectPassenger: {0} tried to eject invalid {1}", getPlayer().getGUID().toString(), packet.passenger.toString());
        }
    }


    private void handleRequestVehicleExit(RequestVehicleExit packet) {
        var vehicle = getPlayer().getVehicle1();

        if (vehicle) {
            var seat = vehicle.GetSeatForPassenger(getPlayer());

            if (seat != null) {
                if (seat.CanEnterOrExit()) {
                    getPlayer().exitVehicle();
                } else {
                    Log.outError(LogFilter.Network, "{0} tried to exit vehicle, but seatflags {1} (ID: {2}) don't permit that.", getPlayer().getGUID().toString(), seat.id, seat.flags);
                }
            }
        }
    }


    private void handleMoveSetVehicleRecAck(MoveSetVehicleRecIdAck setVehicleRecIdAck) {
        getPlayer().validateMovementInfo(setVehicleRecIdAck.data.status);
    }

    public final void sendVoidStorageTransferResult(VoidTransferError result) {
        sendPacket(new VoidTransferResult(result));
    }


    private void handleVoidStorageUnlock(UnlockVoidStorage unlockVoidStorage) {
        var unit = getPlayer().getNPCIfCanInteractWith(unlockVoidStorage.npc, NPCFlags.VaultKeeper, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageUnlock - {0} not found or player can't interact with it.", unlockVoidStorage.npc.toString());

            return;
        }

        if (getPlayer().isVoidStorageUnlocked()) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageUnlock - player({0}, name: {1}) tried to unlock void storage a 2nd time.", getPlayer().getGUID().toString(), getPlayer().getName());

            return;
        }

        getPlayer().modifyMoney(-SharedConst.VoidStorageUnlockCost);
        getPlayer().unlockVoidStorage();
    }


    private void handleVoidStorageQuery(QueryVoidStorage queryVoidStorage) {
        var player = getPlayer();

        var unit = player.getNPCIfCanInteractWith(queryVoidStorage.npc, NPCFlags.Transmogrifier.getValue() | NPCFlags.VaultKeeper.getValue(), NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageQuery - {0} not found or player can't interact with it.", queryVoidStorage.npc.toString());
            sendPacket(new VoidStorageFailed());

            return;
        }

        if (!getPlayer().isVoidStorageUnlocked()) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageQuery - {0} name: {1} queried void storage without unlocking it.", player.getGUID().toString(), player.getName());
            sendPacket(new VoidStorageFailed());

            return;
        }

        VoidStorageContents voidStorageContents = new VoidStorageContents();

        for (byte i = 0; i < SharedConst.VoidStorageMaxSlot; ++i) {
            var item = player.getVoidStorageItem(i);

            if (item == null) {
                continue;
            }

            VoidItem voidItem = new VoidItem();
            voidItem.guid = ObjectGuid.create(HighGuid.Item, item.getItemId());
            voidItem.creator = item.getCreatorGuid();
            voidItem.slot = i;
            voidItem.item = new itemInstance(item);

            voidStorageContents.items.add(voidItem);
        }

        sendPacket(voidStorageContents);
    }


    private void handleVoidStorageTransfer(VoidStorageTransfer voidStorageTransfer) {
        var player = getPlayer();

        var unit = player.getNPCIfCanInteractWith(voidStorageTransfer.npc, NPCFlags.VaultKeeper, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageTransfer - {0} not found or player can't interact with it.", voidStorageTransfer.npc.toString());

            return;
        }

        if (!player.isVoidStorageUnlocked()) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageTransfer - player ({0}, name: {1}) queried void storage without unlocking it.", player.getGUID().toString(), player.getName());

            return;
        }

        if (voidStorageTransfer.deposits.length > player.getNumOfVoidStorageFreeSlots()) {
            sendVoidStorageTransferResult(VoidTransferError.Full);

            return;
        }

        int freeBagSlots = 0;

        if (!voidStorageTransfer.withdrawals.isEmpty()) {
            // make this a Player function
            for (var i = InventorySlots.BagStart; i < InventorySlots.BagEnd; i++) {
                var bag = player.getBagByPos(i);

                if (bag) {
                    freeBagSlots += bag.getFreeSlots();
                }
            }

            var inventoryEnd = InventorySlots.ItemStart + player.getInventorySlotCount();

            for (var i = InventorySlots.ItemStart; i < inventoryEnd; i++) {
                if (!player.getItemByPos(InventorySlots.Bag0, i)) {
                    ++freeBagSlots;
                }
            }
        }

        if (voidStorageTransfer.withdrawals.length > freeBagSlots) {
            sendVoidStorageTransferResult(VoidTransferError.InventoryFull);

            return;
        }

        if (!player.hasEnoughMoney((voidStorageTransfer.deposits.length * SharedConst.VoidStorageStoreItemCost))) {
            sendVoidStorageTransferResult(VoidTransferError.NotEnoughMoney);

            return;
        }

        VoidStorageTransferChanges voidStorageTransferChanges = new VoidStorageTransferChanges();

        byte depositCount = 0;

        for (var i = 0; i < voidStorageTransfer.deposits.length; ++i) {
            var item = player.getItemByGuid(voidStorageTransfer.Deposits[i]);

            if (!item) {
                Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageTransfer - {0} {1} wants to deposit an invalid item ({2}).", player.getGUID().toString(), player.getName(), voidStorageTransfer.Deposits[i].toString());

                continue;
            }

            VoidStorageItem itemVS = new VoidStorageItem(global.getObjectMgr().generateVoidStorageItemId(), item.getEntry(), item.getCreator(), item.getItemRandomBonusListId(), item.getModifier(ItemModifier.TimewalkerLevel), item.getModifier(ItemModifier.artifactKnowledgeLevel), item.getContext(), item.getBonusListIDs());

            VoidItem voidItem = new VoidItem();
            voidItem.guid = ObjectGuid.create(HighGuid.Item, itemVS.getItemId());
            voidItem.creator = item.getCreator();
            voidItem.item = new itemInstance(itemVS);
            voidItem.slot = player.addVoidStorageItem(itemVS);

            voidStorageTransferChanges.addedItems.add(voidItem);

            player.destroyItem(item.getBagSlot(), item.getSlot(), true);
            ++depositCount;
        }

        long cost = depositCount * SharedConst.VoidStorageStoreItemCost;

        player.modifyMoney(-cost);

        for (var i = 0; i < voidStorageTransfer.withdrawals.length; ++i) {
            byte slot;
            tangible.OutObject<Byte> tempOut_slot = new tangible.OutObject<Byte>();
            var itemVS = player.getVoidStorageItem(voidStorageTransfer.Withdrawals[i].getCounter(), tempOut_slot);
            slot = tempOut_slot.outArgValue;

            if (itemVS == null) {
                Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageTransfer - {0} {1} tried to withdraw an invalid item ({2})", player.getGUID().toString(), player.getName(), voidStorageTransfer.Withdrawals[i].toString());

                continue;
            }

            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = player.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, itemVS.getItemEntry(), 1);

            if (msg != InventoryResult.Ok) {
                sendVoidStorageTransferResult(VoidTransferError.InventoryFull);
                Log.outDebug(LogFilter.Network, "WORLD: HandleVoidStorageTransfer - {0} {1} couldn't withdraw {2} because inventory was full.", player.getGUID().toString(), player.getName(), voidStorageTransfer.Withdrawals[i].toString());

                return;
            }

            var item = player.storeNewItem(dest, itemVS.getItemEntry(), true, itemVS.getRandomBonusListId(), null, itemVS.getContext(), itemVS.getBonusListIDs());
            item.setCreator(itemVS.getCreatorGuid());
            item.setBinding(true);
            getCollectionMgr().addItemAppearance(item);

            voidStorageTransferChanges.removedItems.add(ObjectGuid.create(HighGuid.Item, itemVS.getItemId()));

            player.deleteVoidStorageItem(slot);
        }

        sendPacket(voidStorageTransferChanges);
        sendVoidStorageTransferResult(VoidTransferError.Ok);
    }


    private void handleVoidSwapItem(SwapVoidItem swapVoidItem) {
        var player = getPlayer();

        var unit = player.getNPCIfCanInteractWith(swapVoidItem.npc, NPCFlags.VaultKeeper, NPCFlags2.NONE);

        if (!unit) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidSwapItem - {0} not found or player can't interact with it.", swapVoidItem.npc.toString());

            return;
        }

        if (!player.isVoidStorageUnlocked()) {
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidSwapItem - player ({0}, name: {1}) queried void storage without unlocking it.", player.getGUID().toString(), player.getName());

            return;
        }

        byte oldSlot;
        tangible.OutObject<Byte> tempOut_oldSlot = new tangible.OutObject<Byte>();
        if (player.getVoidStorageItem(swapVoidItem.voidItemGuid.getCounter(), tempOut_oldSlot) == null) {
            oldSlot = tempOut_oldSlot.outArgValue;
            Log.outDebug(LogFilter.Network, "WORLD: HandleVoidSwapItem - player (GUID: {0}, name: {1}) requested swapping an invalid item (slot: {2}, itemid: {3}).", player.getGUID().toString(), player.getName(), swapVoidItem.dstSlot, swapVoidItem.voidItemGuid.toString());

            return;
        } else {
            oldSlot = tempOut_oldSlot.outArgValue;
        }

        var usedDestSlot = player.getVoidStorageItem((byte) swapVoidItem.dstSlot) != null;
        var itemIdDest = ObjectGuid.Empty;

        if (usedDestSlot) {
            itemIdDest = ObjectGuid.create(HighGuid.Item, player.getVoidStorageItem(new Byte(swapVoidItem.dstSlot)).getItemId());
        }

        if (!player.swapVoidStorageItem(oldSlot, (byte) swapVoidItem.dstSlot)) {
            sendVoidStorageTransferResult(VoidTransferError.InternalError1);

            return;
        }

        VoidItemSwapResponse voidItemSwapResponse = new VoidItemSwapResponse();
        voidItemSwapResponse.voidItemA = swapVoidItem.voidItemGuid;
        voidItemSwapResponse.voidItemSlotA = swapVoidItem.dstSlot;

        if (usedDestSlot) {
            voidItemSwapResponse.voidItemB = itemIdDest;
            voidItemSwapResponse.voidItemSlotB = oldSlot;
        }

        sendPacket(voidItemSwapResponse);
    }

    public final boolean getCanSpeak() {
        return muteTime <= gameTime.GetGameTime();
    }

    public final String getPlayerName() {
        return player != null ? player.getName() : "Unknown";
    }

    public final boolean getPlayerLoading() {
        return !playerLoading.isEmpty();
    }

    public final boolean getPlayerLogout() {
        return playerLogout;
    }

    public final boolean getPlayerLogoutWithSave() {
        return playerLogout && playerSave;
    }

    public final boolean getPlayerRecentlyLoggedOut() {
        return playerRecentlyLogout;
    }

    public final boolean getPlayerDisconnected() {
        return !(_socket[ConnectionType.realm.getValue()] != null && _socket[ConnectionType.realm.getValue()].IsOpen() && _socket[Type.INSTANCE.getValue()] != null && _socket[Type.INSTANCE.getValue()].IsOpen());
    }

    public final AccountTypes getSecurity() {
        return security;
    }

    private void setSecurity(AccountTypes value) {
        security = value;
    }

    public final int getAccountId() {
        return accountId;
    }

    public final ObjectGuid getAccountGUID() {
        return ObjectGuid.create(HighGuid.wowAccount, getAccountId());
    }

    public final String getAccountName() {
        return accountName;
    }

    public final int getBattlenetAccountId() {
        return battlenetAccountId;
    }

    public final ObjectGuid getBattlenetAccountGUID() {
        return ObjectGuid.create(HighGuid.BNetAccount, getBattlenetAccountId());
    }

    public final Player getPlayer() {
        return player;
    }

    public final void setPlayer(Player value) {
        player = value;

        if (player) {
            guidLow = player.getGUID().getCounter();
        }
    }

    public final String getRemoteAddress() {
        return address;
    }

    public final Expansion getAccountExpansion() {
        return accountExpansion;
    }

    public final Expansion getExpansion() {
        return expansion;
    }

    public final String getOS() {
        return os;
    }

    public final boolean isLogingOut() {
        return logoutTime != 0 || playerLogout;
    }

    public final long getConnectToInstanceKey() {
        return instanceConnectKey.getRaw();
    }

    public final AsyncCallbackProcessor<QueryCallback> getQueryProcessor() {
        return queryProcessor;
    }

    public final RBACData getRBACData() {
        return rbacData;
    }

    public final Locale getSessionDbcLocale() {
        return sessionDbcLocale;
    }

    public final Locale getSessionDbLocaleIndex() {
        return sessionDbLocaleIndex;
    }

    public final int getLatency() {
        return latency;
    }

    public final void setLatency(int value) {
        latency = value;
    }

    private boolean isConnectionIdle() {
        return timeOutTime < gameTime.GetGameTime() && !inQueue;
    }

    public final int getRecruiterId() {
        return recruiterId;
    }

    public final boolean isARecruiter() {
        return isRecruiter;
    }

    // Packets cooldown
    public final long getCalendarEventCreationCooldown() {
        return calendarEventCreationCooldown;
    }

    public final void setCalendarEventCreationCooldown(long value) {
        calendarEventCreationCooldown = value;
    }

    // Battle Pets
    public final BattlePetMgr getBattlePetMgr() {
        return battlePetMgr;
    }

    public final CollectionMgr getCollectionMgr() {
        return collectionMgr;
    }

    // Battlenet
    public final Array<Byte> getRealmListSecret() {
        return realmListSecret;
    }

    private void setRealmListSecret(Array<Byte> value) {
        realmListSecret = value;
    }

    public final HashMap<Integer, Byte> getRealmCharacterCounts() {
        return realmCharacterCounts;
    }

    public final CommandHandler getCommandHandler() {
        return commandHandler;
    }

    private void setCommandHandler(CommandHandler value) {
        commandHandler = value;
    }

    public final BattlepayManager getBattlePayMgr() {
        return battlePayMgr;
    }

    public final void close() throws IOException {
        cancellationToken.cancel();

        // unload player if not unloaded
        if (player) {
            logoutPlayer(true);
        }

        // - If have unclosed socket, close it
        for (byte i = 0; i < 2; ++i) {
            if (_socket[i] != null) {
                _socket[i].CloseSocket();
                _socket[i] = null;
            }
        }

        // empty incoming packet queue
        recvQueue.Complete();

        DB.Login.execute("UPDATE account SET online = 0 WHERE id = {0};", getAccountId()); // One-time query
    }

    public final void logoutPlayer(boolean save) {
        if (playerLogout) {
            return;
        }

        // finish pending transfers before starting the logout
        while (player && player.isBeingTeleportedFar()) {
            handleMoveWorldportAck();
        }

        playerLogout = true;
        playerSave = save;

        if (player) {
            if (!player.getLootGUID().isEmpty()) {
                doLootReleaseAll();
            }

            // If the player just died before logging out, make him appear as a ghost
            //FIXME: logout must be delayed in case lost connection with client in time of combat
            if (getPlayer().getDeathTimer() != 0) {
                player.combatStop();
                player.buildPlayerRepop();
                player.repopAtGraveyard();
            } else if (getPlayer().hasAuraType(AuraType.SpiritOfRedemption)) {
                // this will kill character by SPELL_AURA_SPIRIT_OF_REDEMPTION
                player.removeAurasByType(AuraType.ModShapeshift);
                player.killPlayer();
                player.buildPlayerRepop();
                player.repopAtGraveyard();
            } else if (getPlayer().getHasPendingBind()) {
                player.repopAtGraveyard();
                player.setPendingBind(0, 0);
            }

            //drop a flag if player is carrying it
            var bg = getPlayer().getBattleground();

            if (bg) {
                bg.eventPlayerLoggedOut(getPlayer());
            }

            // Teleport to home if the player is in an invalid instance
            if (!player.getInstanceValid() && !player.isGameMaster()) {
                player.teleportTo(player.getHomeBind());
            }

            global.getOutdoorPvPMgr().handlePlayerLeaveZone(player, player.getZoneId());

            for (int i = 0; i < SharedConst.MaxPlayerBGQueues; ++i) {
                var bgQueueTypeId = player.getBattlegroundQueueTypeId(i);

                if (BattlegroundQueueTypeId.opNotEquals(bgQueueTypeId, null)) {
                    player.removeBattlegroundQueueId(bgQueueTypeId);
                    var queue = global.getBattlegroundMgr().getBattlegroundQueue(bgQueueTypeId);
                    queue.removePlayer(player.getGUID(), true);
                }
            }

            // Repop at GraveYard or other player far teleport will prevent saving player because of not present map
            // Teleport player immediately for correct player save
            while (player.isBeingTeleportedFar()) {
                handleMoveWorldportAck();
            }

            // If the player is in a guild, update the guild roster and broadcast a logout message to other guild members
            var guild = global.getGuildMgr().getGuildById(player.getGuildId());

            if (guild) {
                guild.handleMemberLogout(this);
            }

            // Remove pet
            player.removePet(null, PetSaveMode.AsCurrent, true);

            /**- Release battle pet journal lock
             */
            if (battlePetMgr.getHasJournalLock()) {
                battlePetMgr.toggleJournalLock(false);
            }

            // Clear whisper whitelist
            player.clearWhisperWhiteList();

            // empty buyback items and save the player in the database
            // some save parts only correctly work in case player present in map/player_lists (pets, etc)
            if (save) {
                for (int j = InventorySlots.BuyBackStart; j < InventorySlots.BuyBackEnd; ++j) {
                    var eslot = j - InventorySlots.BuyBackStart;
                    player.setInvSlot(j, ObjectGuid.Empty);
                    player.setBuybackPrice(eslot, 0);
                    player.setBuybackTimestamp(eslot, 0);
                }

                player.saveToDB();
            }

            // Leave all channels before player delete...
            player.cleanupChannels();

            // If the player is in a group (or invited), remove him. If the group if then only 1 person, disband the group.
            player.uninviteFromGroup();

            //! Send update to group and reset stored max enchanting level
            var group = player.getGroup();

            if (group != null) {
                group.sendUpdate();

                if (Objects.equals(group.getLeaderGUID(), player.getGUID())) {
                    group.startLeaderOfflineTimer();
                }
            }

            //! Broadcast a logout message to the player's friends
            global.getSocialMgr().sendFriendStatus(player, FriendsResult.Offline, player.getGUID(), true);
            player.removeSocial();

            //! Call script hook before deletion
            global.getScriptMgr().<IPlayerOnLogout>ForEach(p -> p.onLogout(player));

            //! Remove the player from the world
            // the player may not be in the world when logging out
            // e.g if he got disconnected during a transfer to another map
            // calls to GetMap in this case may cause crashes
            player.setDestroyedObject(true);
            player.cleanupsBeforeDelete();
            Log.outInfo(LogFilter.player, String.format("Account: %1$s (IP: %2$s) Logout Character:[%3$s] (%4$s) Level: %5$s, XP: %6$s/%7$s (%8$s left)", getAccountId(), getRemoteAddress(), player.getName(), player.getGUID(), player.getLevel(), player.getXP(), player.getXPForNextLevel(), player.getXPForNextLevel() - player.getXP()));

            var map = getPlayer().getMap();

            if (map != null) {
                map.removePlayerFromMap(getPlayer(), true);
            }

            setPlayer(null);

            //! Send the 'logout complete' packet to the client
            //! Client will respond by sending 3x CMSG_CANCEL_TRADE, which we currently dont handle
            LogoutComplete logoutComplete = new LogoutComplete();
            sendPacket(logoutComplete);

            //! Since each account can only have one online character at any given time, ensure all character for active account are marked as offline
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ACCOUNT_ONLINE);
            stmt.AddValue(0, getAccountId());
            DB.characters.execute(stmt);
        }

        if (_socket[Type.INSTANCE.getValue()] != null) {
            _socket[Type.INSTANCE.getValue()].CloseSocket();
            _socket[Type.INSTANCE.getValue()] = null;
        }

        playerLogout = false;
        playerSave = false;
        playerRecentlyLogout = true;
        setLogoutStartTime(0);
    }

    public final boolean updateMap(int diff) {
        drainQueue(threadSafeQueue);

        // Send time sync packet every 10s.
        if (timeSyncTimer > 0) {
            if (diff >= timeSyncTimer) {
                sendTimeSync();
            } else {
                _timeSyncTimer -= diff;
            }
        }

        processQueryCallbacks();

        return true;
    }

    public final boolean updateWorld(int diff) {
        var currentTime = drainQueue(threadUnsafe);

        processQueryCallbacks();


        if (_socket[ConnectionType.realm.getValue()] != null && _socket[ConnectionType.realm.getValue()].IsOpen() && warden != null) {
            warden.update(diff);
        }

        // If necessary, log the player out
        if (shouldLogOut(currentTime) && playerLoading.isEmpty()) {
            logoutPlayer(true);
        }

        //- Cleanup socket if need
        if ((_socket[ConnectionType.realm.getValue()] != null && !_socket[ConnectionType.realm.getValue()].IsOpen()) || (_socket[Type.INSTANCE.getValue()] != null && !_socket[Type.INSTANCE.getValue()].IsOpen())) {
            if (getPlayer() != null && warden != null) {
                warden.update(diff);
            }

            _expireTime -= expireTime > diff ? diff : expireTime;

            if (expireTime < diff || forceExit || !getPlayer()) {
                if (_socket[ConnectionType.realm.getValue()] != null) {
                    _socket[ConnectionType.realm.getValue()].CloseSocket();
                    _socket[ConnectionType.realm.getValue()] = null;
                }

                if (_socket[Type.INSTANCE.getValue()] != null) {
                    _socket[Type.INSTANCE.getValue()].CloseSocket();
                    _socket[Type.INSTANCE.getValue()] = null;
                }
            }
        }

        if (_socket[ConnectionType.realm.getValue()] == null) {
            return false; //Will remove this session from the world session map
        }


        return true;
    }

    public final void queuePacket(WorldPacket packet) {
        recvQueue.Post(packet);
    }

    public final void sendPacket(ServerPacket packet) {
        if (packet == null) {
            return;
        }

        if (packet.GetOpcode() == ServerOpcode.unknown || packet.GetOpcode() == ServerOpcode.max) {
            Log.outError(LogFilter.Network, "Prevented sending of UnknownOpcode to {0}", getPlayerInfo());

            return;
        }

        var conIdx = packet.GetConnection();

        if (conIdx != Type.INSTANCE && PacketManager.IsInstanceOnlyOpcode(packet.GetOpcode())) {
            Log.outError(LogFilter.Network, "Prevented sending of instance only opcode {0} with connection type {1} to {2}", packet.GetOpcode(), packet.GetConnection(), getPlayerInfo());

            return;
        }

        if (_socket[conIdx.getValue()] == null) {
            Log.outTrace(LogFilter.Network, "Prevented sending of {0} to non existent socket {1} to {2}", packet.GetOpcode(), conIdx, getPlayerInfo());

            return;
        }

        _socket[conIdx.getValue()].sendPacket(packet);
    }

    public final void addInstanceConnection(WorldSocket sock) {
        _socket[Type.INSTANCE.getValue()] = sock;
    }

    public final void kickPlayer(String reason) {
        Log.outInfo(LogFilter.Network, String.format("Account: %1$s Character: '%2$s' %3$s kicked with reason: %4$s", getAccountId(), (_player ? player.getName() : "<none>"), (_player ? player.getGUID() : ""), reason));

        for (byte i = 0; i < 2; ++i) {
            if (_socket[i] != null) {
                _socket[i].CloseSocket();
                forceExit = true;
            }
        }
    }

    public final boolean isAddonRegistered(String prefix) {
        if (!filterAddonMessages) // if we have hit the softcap (64) nothing should be filtered
        {
            return true;
        }

        if (registeredAddonPrefixes.isEmpty()) {
            return false;
        }

        return registeredAddonPrefixes.contains(prefix);
    }

    public final void sendAccountDataTimes(ObjectGuid playerGuid, AccountDataTypes mask) {
        AccountDataTimes accountDataTimes = new AccountDataTimes();
        accountDataTimes.playerGuid = playerGuid;
        accountDataTimes.serverTime = gameTime.GetGameTime();

        for (var i = 0; i < AccountDataTypes.max.getValue(); ++i) {
            if ((mask.getValue() & (1 << i)) != 0) {
                accountDataTimes.AccountTimes[i] = getAccountData(AccountDataTypes.forValue(i)).time;
            }
        }

        sendPacket(accountDataTimes);
    }

    public final void loadTutorialsData(SQLResult result) {
        if (!result.isEmpty()) {
            for (var i = 0; i < SharedConst.MaxAccountTutorialValues; i++) {
                _tutorials[i] = result.<Integer>Read(i);
            }

            tutorialsChanged = TutorialsFlag.forValue(tutorialsChanged.getValue() | TutorialsFlag.LoadedFromDB.getValue());
        }

        tutorialsChanged = TutorialsFlag.forValue(tutorialsChanged.getValue() & ~TutorialsFlag.changed.getValue());
    }

    public final void saveTutorialsData(SQLTransaction trans) {
        if (!tutorialsChanged.hasFlag(TutorialsFlag.changed)) {
            return;
        }

        var hasTutorialsInDB = tutorialsChanged.hasFlag(TutorialsFlag.LoadedFromDB);
        var stmt = DB.characters.GetPreparedStatement(hasTutorialsInDB ? CharStatements.UPD_TUTORIALS : CharStatements.INS_TUTORIALS);

        for (var i = 0; i < SharedConst.MaxAccountTutorialValues; ++i) {
            stmt.AddValue(i, _tutorials[i]);
        }

        stmt.AddValue(SharedConst.MaxAccountTutorialValues, getAccountId());
        trans.append(stmt);

        // now has, set flag so next save uses update query
        if (!hasTutorialsInDB) {
            tutorialsChanged = TutorialsFlag.forValue(tutorialsChanged.getValue() | TutorialsFlag.LoadedFromDB.getValue());
        }

        tutorialsChanged = TutorialsFlag.forValue(tutorialsChanged.getValue() & ~TutorialsFlag.changed.getValue());
    }

    public final void sendConnectToInstance(ConnectToSerial serial) {
        var instanceAddress = global.getWorldMgr().getRealm().GetAddressForClient(system.Net.IPAddress.parse(getRemoteAddress()));

        instanceConnectKey.accountId = getAccountId();
        instanceConnectKey.connectionType = Type.INSTANCE;
        instanceConnectKey.key = RandomUtil.URand(0, 0x7FFFFFFF);

        ConnectTo connectTo = new ConnectTo();
        connectTo.key = instanceConnectKey.getRaw();
        connectTo.serial = serial;
        connectTo.payload.port = (short) WorldConfig.getIntValue(WorldCfg.PortInstance);
        connectTo.con = (byte) Type.INSTANCE.getValue();

        if (instanceAddress.AddressFamily == system.Net.Sockets.AddressFamily.InterNetwork) {
            connectTo.payload.where.IPv4 = instanceAddress.address.GetAddressBytes();
            connectTo.payload.where.type = ConnectTo.AddressType.IPv4;
        } else {
            connectTo.payload.where.IPv6 = instanceAddress.address.GetAddressBytes();
            connectTo.payload.where.type = ConnectTo.AddressType.IPv6;
        }

        sendPacket(connectTo);
    }

    public final void sendTutorialsData() {
        TutorialFlags packet = new TutorialFlags();
        system.arraycopy(tutorials, 0, packet.tutorialData, 0, SharedConst.MaxAccountTutorialValues);
        sendPacket(packet);
    }

    public final boolean disallowHyperlinksAndMaybeKick(String str) {
        if (!str.contains('|')) {
            return true;
        }

        Log.outError(LogFilter.Network, String.format("Player %1$s (%2$s) sent a message which illegally contained a hyperlink:\n%3$s", getPlayer().getName(), getPlayer().getGUID(), str));

        if (WorldConfig.getIntValue(WorldCfg.ChatStrictLinkCheckingKick) != 0) {
            kickPlayer("WorldSession::DisallowHyperlinksAndMaybeKick Illegal chat link");
        }

        return false;
    }

    public final void sendNotification(SysMessage str, object... args) {
        sendNotification(global.getObjectMgr().getSysMessage(str), args);
    }

    public final void sendNotification(String str, object... args) {
        var message = String.format(str, args);

        if (!StringUtil.isEmpty(message)) {
            sendPacket(new PrintNotification(message));
        }
    }

    public final String getPlayerInfo() {
        StringBuilder ss = new StringBuilder();
        ss.append("[Player: ");

        if (!playerLoading.isEmpty()) {
            ss.append(String.format("Logging in: %1$s, ", playerLoading.toString()));
        } else if (player) {
            ss.append(String.format("%1$s %2$s, ", player.getName(), player.getGUID().toString()));
        }

        ss.append(String.format("Account: %1$s]", getAccountId()));

        return ss.toString();
    }

    public final void setInQueue(boolean state) {
        inQueue = state;
    }

    public final <R> SQLQueryHolderCallback<R> addQueryHolderCallback(SQLQueryHolderCallback<R> callback) {
        return (SQLQueryHolderCallback<R>) queryHolderProcessor.AddCallback(callback);
    }

    public final boolean canAccessAlliedRaces() {
        if (ConfigMgr.GetDefaultValue("CharacterCreating.DisableAlliedRaceAchievementRequirement", false)) {
            return true;
        } else {
            return getAccountExpansion().getValue() >= getExpansion().getValue().BattleForAzeroth;
        }
    }

    public final void loadPermissions() {
        var id = getAccountId();
        var secLevel = getSecurity();

        Log.outDebug(LogFilter.Rbac, "WorldSession.LoadPermissions [AccountId: {0}, Name: {1}, realmId: {2}, secLevel: {3}]", id, accountName, global.getWorldMgr().getRealm().id.index, secLevel);

        rbacData = new RBACData(id, accountName, (int) global.getWorldMgr().getRealm().id.index, (byte) secLevel.getValue());
        rbacData.loadFromDB();
    }

    public final QueryCallback loadPermissionsAsync() {
        var id = getAccountId();
        var secLevel = getSecurity();

        Log.outDebug(LogFilter.Rbac, "WorldSession.LoadPermissions [AccountId: {0}, Name: {1}, realmId: {2}, secLevel: {3}]", id, accountName, global.getWorldMgr().getRealm().id.index, secLevel);

        rbacData = new RBACData(id, accountName, (int) global.getWorldMgr().getRealm().id.index, (byte) secLevel.getValue());

        return rbacData.loadFromDBAsync();
    }

    public final void initializeSession() {
        AccountInfoQueryHolderPerRealm realmHolder = new AccountInfoQueryHolderPerRealm();
        realmHolder.initialize(getAccountId(), getBattlenetAccountId());

        AccountInfoQueryHolder holder = new AccountInfoQueryHolder();
        holder.initialize(getAccountId(), getBattlenetAccountId());

        AccountInfoQueryHolderPerRealm characterHolder = null;
        AccountInfoQueryHolder loginHolder = null;

        addQueryHolderCallback(DB.characters.DelayQueryHolder(realmHolder)).AfterComplete(result ->
        {
            characterHolder = (AccountInfoQueryHolderPerRealm) result;

            if (loginHolder != null && characterHolder != null) {
                initializeSessionCallback(loginHolder, characterHolder);
            }
        });

        addQueryHolderCallback(DB.Login.DelayQueryHolder(holder)).AfterComplete(result ->
        {
            loginHolder = (AccountInfoQueryHolder) result;

            if (loginHolder != null && characterHolder != null) {
                initializeSessionCallback(loginHolder, characterHolder);
            }
        });
    }

    public final boolean hasPermission(RBACPermissions permission) {
        if (rbacData == null) {
            loadPermissions();
        }

        var hasPermission = rbacData.hasPermission(permission);

        Log.outDebug(LogFilter.Rbac, "WorldSession:HasPermission [AccountId: {0}, Name: {1}, realmId: {2}]", rbacData.getId(), rbacData.getName(), global.getWorldMgr().getRealm().id.index);

        return hasPermission;
    }

    public final void invalidateRBACData() {
        Log.outDebug(LogFilter.Rbac, "WorldSession:Invalidaterbac:RBACData [AccountId: {0}, Name: {1}, realmId: {2}]", rbacData.getId(), rbacData.getName(), global.getWorldMgr().getRealm().id.index);

        rbacData = null;
    }

    public final void resetTimeSync() {
        timeSyncNextCounter = 0;
        pendingTimeSyncRequests.clear();
    }

    public final void sendTimeSync() {
        TimeSyncRequest timeSyncRequest = new TimeSyncRequest();
        timeSyncRequest.sequenceIndex = timeSyncNextCounter;
        sendPacket(timeSyncRequest);

        pendingTimeSyncRequests.put(timeSyncNextCounter, time.MSTime);

        // Schedule next sync in 10 sec (except for the 2 first packets, which are spaced by only 5s)
        timeSyncTimer = timeSyncNextCounter == 0 ? 5000 : 10000;
        timeSyncNextCounter++;
    }

    public final void resetTimeOutTime(boolean onlyActive) {
        if (getPlayer()) {
            timeOutTime = gameTime.GetGameTime() + WorldConfig.getIntValue(WorldCfg.SocketTimeoutTimeActive);
        } else if (!onlyActive) {
            timeOutTime = gameTime.GetGameTime() + WorldConfig.getIntValue(WorldCfg.SocketTimeoutTime);
        }
    }

    private void processQueue(WorldPacket packet) {
        var handler = PacketManager.getHandler(ClientOpcodes.forValue(packet.GetOpcode()));

        if (handler != null) {
            if (handler.getProcessingPlace() == PacketProcessing.Inplace) {
                inPlaceQueue.Enqueue(packet);
                asyncMessageQueueSemaphore.set();
            } else if (handler.getProcessingPlace() == PacketProcessing.ThreadSafe) {
                threadSafeQueue.Enqueue(packet);
            } else {
                threadUnsafe.Enqueue(packet);
            }
        }

    }


//	public static implicit operator bool(WorldSession session)
//		{
//			return session != null;
//		}

    private void processInPlace() {
        while (!cancellationToken.IsCancellationRequested) {
            asyncMessageQueueSemaphore.WaitOne(500);
            drainQueue(inPlaceQueue);
        }
    }

    private long drainQueue(ConcurrentQueue<WorldPacket> _queue) {
        // Before we process anything:
        /** If necessary, kick the player because the client didn't send anything for too long
         (or they've been idling in character select)
         */
        if (isConnectionIdle() && !hasPermission(RBACPermissions.IgnoreIdleConnection)) {
            if (_socket[ConnectionType.realm.getValue()] != null) {
                _socket[ConnectionType.realm.getValue()].CloseSocket();
            }
        }

        WorldPacket firstDelayedPacket = null;
        int processedPackets = 0;
        var currentTime = gameTime.GetGameTime();

        //Check for any packets they was not recived yet.
        T packet;
        tangible.OutObject<WorldPacket> tempOut_packet = new tangible.OutObject<WorldPacket>();
        tangible.OutObject<WorldPacket> tempOut_packet2 = new tangible.OutObject<WorldPacket>();
        while (_socket[ConnectionType.realm.getValue()] != null && !_queue.IsEmpty && (_queue.TryPeek(tempOut_packet) && packet != firstDelayedPacket) && _queue.TryDequeue(tempOut_packet2)) {
            packet = tempOut_packet2.outArgValue;
            packet = tempOut_packet.outArgValue;
            try {
                var handler = PacketManager.getHandler(ClientOpcodes.forValue(packet.GetOpcode()));

                switch (handler.getSessionStatus()) {
                    case Loggedin:
                        if (!player) {
                            if (!playerRecentlyLogout) {
                                if (firstDelayedPacket == null) {
                                    firstDelayedPacket = packet;
                                }

                                queuePacket(packet);
                                Log.outDebug(LogFilter.Network, "Re-enqueueing packet with opcode {0} with with status OpcodeStatus.Loggedin. Player is currently not in world yet.", ClientOpcodes.forValue(packet.GetOpcode()));
                            }

                            break;
                        } else if (player.isInWorld() && antiDos.evaluateOpcode(packet, currentTime)) {
                            handler.invoke(this, packet);
                        }

                        break;
                    case LoggedinOrRecentlyLogout:
                        if (!player && !playerRecentlyLogout && !playerLogout) {
                            logUnexpectedOpcode(packet, handler.getSessionStatus(), "the player has not logged in yet and not recently logout");
                        } else if (antiDos.evaluateOpcode(packet, currentTime)) {
                            handler.invoke(this, packet);
                        }

                        break;
                    case Transfer:
                        if (!player) {
                            logUnexpectedOpcode(packet, handler.getSessionStatus(), "the player has not logged in yet");
                        } else if (player.isInWorld()) {
                            logUnexpectedOpcode(packet, handler.getSessionStatus(), "the player is still in world");
                        } else if (antiDos.evaluateOpcode(packet, currentTime)) {
                            handler.invoke(this, packet);
                        }

                        break;
                    case Authed:
                        // prevent cheating with skip queue wait
                        if (inQueue) {
                            logUnexpectedOpcode(packet, handler.getSessionStatus(), "the player not pass queue yet");

                            break;
                        }

                        if (packet.GetOpcode() == ClientOpcodes.EnumCharacters.getValue()) {
                            playerRecentlyLogout = false;
                        }

                        if (antiDos.evaluateOpcode(packet, currentTime)) {
                            handler.invoke(this, packet);
                        }

                        break;
                    default:
                        Log.outError(LogFilter.Network, "Received not handled opcode {0} from {1}", ClientOpcodes.forValue(packet.GetOpcode()), getPlayerInfo());

                        break;
                }
            } catch (InternalBufferOverflowException ex) {
                Log.outError(LogFilter.Network, "InternalBufferOverflowException: {0} while parsing {1} from {2}.", ex.getMessage(), ClientOpcodes.forValue(packet.GetOpcode()), getPlayerInfo());
            } catch (EndOfStreamException e) {
                Log.outError(LogFilter.Network, "WorldSession:Update EndOfStreamException occured while parsing a packet (opcode: {0}) from client {1}, accountid={2}. Skipped packet.", ClientOpcodes.forValue(packet.GetOpcode()), getRemoteAddress(), getAccountId());
            }

            processedPackets++;

            if (processedPackets > 100) {
                break;
            }
        }
        packet = tempOut_packet2.outArgValue;
        packet = tempOut_packet.outArgValue;

        return currentTime;
    }

    private void logUnexpectedOpcode(WorldPacket packet, SessionStatus status, String reason) {
        Log.outError(LogFilter.Network, "Received unexpected opcode {0} Status: {1} Reason: {2} from {3}", ClientOpcodes.forValue(packet.GetOpcode()), status, reason, getPlayerInfo());
    }

    private void loadAccountData(SQLResult result, AccountDataTypes mask) {
        for (var i = 0; i < AccountDataTypes.max.getValue(); ++i) {
            if ((boolean) (mask.getValue() & (1 << i))) {
                _accountData[i] = new AccountData();
            }
        }

        if (result.isEmpty()) {
            return;
        }

        do {
            int type = result.<Byte>Read(0);

            if (type >= AccountDataTypes.max.getValue()) {
                Log.outError(LogFilter.Server, "Table `{0}` have invalid account data type ({1}), ignore.", mask == AccountDataTypes.GlobalCacheMask ? "account_data" : "character_account_data", type);

                continue;
            }

            if ((mask.getValue() & (1 << type)) == 0) {
                Log.outError(LogFilter.Server, "Table `{0}` have non appropriate for table  account data type ({1}), ignore.", mask == AccountDataTypes.GlobalCacheMask ? "account_data" : "character_account_data", type);

                continue;
            }

            _accountData[type].time = result.<Long>Read(1);
            var bytes = result.<byte[]>Read(2);
            var line = Encoding.Default.getString(bytes);
            _accountData[type].data = line;
        } while (result.NextRow());
    }

    private void setAccountData(AccountDataTypes type, long time, String data) {
        if ((boolean) ((1 << type.getValue()) & AccountDataTypes.GlobalCacheMask.getValue())) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.REP_ACCOUNT_DATA);
            stmt.AddValue(0, getAccountId());
            stmt.AddValue(1, (byte) type.getValue());
            stmt.AddValue(2, time);
            stmt.AddValue(3, data);
            DB.characters.execute(stmt);
        } else {
            // _player can be NULL and packet received after logout but m_GUID still store correct guid
            if (guidLow == 0) {
                return;
            }

            var stmt = DB.characters.GetPreparedStatement(CharStatements.REP_PLAYER_ACCOUNT_DATA);
            stmt.AddValue(0, guidLow);
            stmt.AddValue(1, (byte) type.getValue());
            stmt.AddValue(2, time);
            stmt.AddValue(3, data);
            DB.characters.execute(stmt);
        }

        _accountData[type.getValue()].time = time;
        _accountData[type.getValue()].data = data;
    }

    private boolean validateHyperlinksAndMaybeKick(String str) {
        if (Hyperlink.checkAllLinks(str)) {
            return true;
        }

        Log.outError(LogFilter.Network, String.format("Player %1$s %2$s sent a message with an invalid link:\n%3$s", getPlayer().getName(), getPlayer().getGUID(), str));

        if (WorldConfig.getIntValue(WorldCfg.ChatStrictLinkCheckingKick) != 0) {
            kickPlayer("WorldSession::ValidateHyperlinksAndMaybeKick Invalid chat link");
        }

        return false;
    }

    private void handleWardenData(WardenData packet) {
        if (warden == null || packet.data.getSize() == 0) {
            return;
        }

        warden.handleData(packet.data);
    }

    private void setLogoutStartTime(long requestTime) {
        logoutTime = requestTime;
    }

    private boolean shouldLogOut(long currTime) {
        return (logoutTime > 0 && currTime >= logoutTime + 20);
    }

    private void processQueryCallbacks() {
        queryProcessor.ProcessReadyCallbacks();
        transactionCallbacks.ProcessReadyCallbacks();
        queryHolderProcessor.ProcessReadyCallbacks();
    }

    private TransactionCallback addTransactionCallback(TransactionCallback callback) {
        return transactionCallbacks.AddCallback(callback);
    }

    private void initWarden(BigInteger k) {
        if (Objects.equals(os, "Win")) {
            warden = new WardenWin();
            warden.init(this, k);
        } else if (Objects.equals(os, "Wn64")) {
            // Not implemented
        } else if (Objects.equals(os, "Mc64")) {
            // Not implemented
        }
    }

    private void initializeSessionCallback(SQLQueryHolder<AccountInfoQueryLoad> holder, SQLQueryHolder<AccountInfoQueryLoad> realmHolder) {
        loadAccountData(realmHolder.GetResult(AccountInfoQueryLoad.GlobalAccountDataIndexPerRealm), AccountDataTypes.GlobalCacheMask);
        loadTutorialsData(realmHolder.GetResult(AccountInfoQueryLoad.TutorialsIndexPerRealm));
        collectionMgr.loadAccountToys(holder.GetResult(AccountInfoQueryLoad.GlobalAccountToys));
        collectionMgr.loadAccountHeirlooms(holder.GetResult(AccountInfoQueryLoad.GlobalAccountHeirlooms));
        collectionMgr.loadAccountMounts(holder.GetResult(AccountInfoQueryLoad.mounts));
        collectionMgr.loadAccountItemAppearances(holder.GetResult(AccountInfoQueryLoad.ItemAppearances), holder.GetResult(AccountInfoQueryLoad.ItemFavoriteAppearances));
        collectionMgr.loadAccountTransmogIllusions(holder.GetResult(AccountInfoQueryLoad.transmogIllusions));

        if (!inQueue) {
            sendAuthResponse(BattlenetRpcErrorCode.Ok, false);
        } else {
            sendAuthWaitQueue(0);
        }

        setInQueue(false);
        resetTimeOutTime(false);

        sendSetTimeZoneInformation();
        sendFeatureSystemStatusGlueScreen();
        sendClientCacheVersion(WorldConfig.getUIntValue(WorldCfg.ClientCacheVersion));
        sendAvailableHotfixes();
        sendAccountDataTimes(ObjectGuid.Empty, AccountDataTypes.GlobalCacheMask);
        sendTutorialsData();

        var result = holder.GetResult(AccountInfoQueryLoad.GlobalRealmCharacterCounts);

        if (!result.isEmpty()) {
            do {
                realmCharacterCounts.put((new RealmId(result.<Byte>Read(3), result.<Byte>Read(4), result.<Integer>Read(2))).GetAddress(), result.<Byte>Read(1));
            } while (result.NextRow());
        }

        ConnectionStatus bnetConnected = new ConnectionStatus();
        bnetConnected.state = 1;
        sendPacket(bnetConnected);

        battlePetMgr.loadFromDB(holder.GetResult(AccountInfoQueryLoad.BattlePets), holder.GetResult(AccountInfoQueryLoad.BattlePetSlot));
    }

    private AccountData getAccountData(AccountDataTypes type) {
        return _accountData[type.getValue()];
    }

    private int getTutorialInt(byte index) {
        return _tutorials[index];
    }

    private void setTutorialInt(byte index, int value) {
        if (_tutorials[index] != value) {
            _tutorials[index] = value;
            tutorialsChanged = TutorialsFlag.forValue(tutorialsChanged.getValue() | TutorialsFlag.changed.getValue());
        }
    }

    private int adjustClientMovementTime(int time) {
        var movementTime = (long) time + timeSyncClockDelta;

        if (timeSyncClockDelta == 0 || movementTime < 0 || movementTime > 0xFFFFFFFFL) {
            Log.outWarn(LogFilter.misc, "The computed movement time using clockDelta is erronous. Using fallback instead");

            return gameTime.GetGameTimeMS();
        } else {
            return (int) movementTime;
        }
    }


    private BattlenetRpcErrorCode handleProcessClientRequest(ClientRequest request, ClientResponse response) {
        Bgs.Protocol.Attribute command = null;
        HashMap<String, Bgs.Protocol.Variant> params = new HashMap<String, Bgs.Protocol.Variant>();


//		string removeSuffix(string str)
//			{
//				var pos = str.IndexOf('_');
//
//				if (pos != -1)
//					return str.Substring(0, pos);
//
//				return str;
//			}

        for (var i = 0; i < request.Attribute.count; ++i) {
            var attr = request.Attribute[i];

            if (attr.name.contains("Command_")) {
                command = attr;
                params.put(removeSuffix(attr.name), attr.value);
            } else {
                params.put(attr.name, attr.value);
            }
        }

        if (command == null) {
            Log.outError(LogFilter.SessionRpc, "{0} sent ClientRequest with no command.", getPlayerInfo());

            return BattlenetRpcErrorCode.RpcMalformedRequest;
        }

        return switch (removeSuffix(command.name)) {
            case "Command_RealmListRequest_v1" -> handleRealmListRequest(params, response);
            case "Command_RealmJoinRequest_v1" -> handleRealmJoinRequest(params, response);
            default -> BattlenetRpcErrorCode.RpcNotImplemented.getValue();
        };
    }


    private BattlenetRpcErrorCode handleGetAllValuesForAttribute(GetAllValuesForAttributeRequest request, GetAllValuesForAttributeResponse response) {
        if (!request.AttributeKey.contains("Command_RealmListRequest_v1")) {
            global.getRealmMgr().WriteSubRegions(response);

            return BattlenetRpcErrorCode.Ok;
        }

        return BattlenetRpcErrorCode.RpcNotImplemented;
    }

    private BattlenetRpcErrorCode handleRealmListRequest(HashMap<String, Bgs.Protocol.Variant> params, ClientResponse response) {
        var subRegionId = "";
        var subRegion = params.get("Command_RealmListRequest_v1");

        if (subRegion != null) {
            subRegionId = subRegion.StringValue;
        }

        var compressed = global.getRealmMgr().GetRealmList(global.getWorldMgr().getRealm().Build, subRegionId);

        if (compressed.isEmpty()) {
            return BattlenetRpcErrorCode.UtilServerFailedToSerializeResponse;
        }

        Bgs.Protocol.Attribute attribute = new Bgs.Protocol.Attribute();
        attribute.name = "Param_RealmList";
        attribute.value = new Bgs.Protocol.Variant();
        attribute.value.BlobValue = ByteString.CopyFrom(compressed);
        response.Attribute.add(attribute);

        var realmCharacterCounts = new RealmCharacterCountList();

        for (var characterCount : getRealmCharacterCounts().entrySet()) {
            RealmCharacterCountEntry countEntry = new RealmCharacterCountEntry();
            countEntry.WowRealmAddress = (int) characterCount.getKey();
            countEntry.count = characterCount.getValue();
            realmCharacterCounts.Counts.add(countEntry);
        }

        compressed = Json.Deflate("JSONRealmCharacterCountList", realmCharacterCounts);

        attribute = new Bgs.Protocol.Attribute();
        attribute.name = "Param_CharacterCountList";
        attribute.value = new Bgs.Protocol.Variant();
        attribute.value.BlobValue = ByteString.CopyFrom(compressed);
        response.Attribute.add(attribute);

        return BattlenetRpcErrorCode.Ok;
    }

    private BattlenetRpcErrorCode handleRealmJoinRequest(HashMap<String, Bgs.Protocol.Variant> params, ClientResponse response) {
        var realmAddress = params.get("Param_RealmAddress");

        if (realmAddress != null) {
            return global.getRealmMgr().JoinRealm((int) realmAddress.UintValue, global.getWorldMgr().getRealm().Build, system.Net.IPAddress.parse(getRemoteAddress()), getRealmListSecret(), getSessionDbcLocale(), getOS(), getAccountName(), response);
        }

        return BattlenetRpcErrorCode.Ok;
    }

    private static class AELootCreatureCheck implements ICheck<Creature> {
        public static final float LOOTDISTANCE = 30.0f;

        private final Player looter;
        private final ObjectGuid mainLootTarget;

        public AELootCreatureCheck(Player looter, ObjectGuid mainLootTarget) {
            looter = looter;
            mainLootTarget = mainLootTarget;
        }

        public final boolean invoke(Creature creature) {
            return isValidAELootTarget(creature);
        }

        public final boolean isValidLootTarget(Creature creature) {
            if (creature.isAlive()) {
                return false;
            }

            if (!looter.isWithinDist(creature, LOOTDISTANCE)) {
                return false;
            }

            return looter.isAllowedToLoot(creature);
        }

        private boolean isValidAELootTarget(Creature creature) {
            if (Objects.equals(creature.getGUID(), mainLootTarget)) {
                return false;
            }

            return isValidLootTarget(creature);
        }
    }
}
