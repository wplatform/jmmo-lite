package com.github.azeroth.game.loot;


import com.github.azeroth.game.entity.item.bonusData;
import com.github.azeroth.game.entity.player.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class LootRoll {
    private static final Duration LOOT_ROLL_TIMEOUT = durationofMinutes(1);
    private final HashMap<ObjectGuid, PlayerRollVote> m_rollVoteMap = new HashMap<ObjectGuid, PlayerRollVote>();

    private Map m_map;
    private boolean m_isStarted;
    private LootItem m_lootItem;
    private Loot m_loot;
    private RollMask m_voteMask = RollMask.values()[0];
    private LocalDateTime m_endTime = LocalDateTime.MIN;

    // Try to start the group roll for the specified item (it may fail for quest item or any condition
    // If this method return false the roll have to be removed from the container to avoid any problem
    public final boolean tryToStart(Map map, Loot loot, int lootListId, short enchantingSkill) {
        if (!m_isStarted) {
            if (lootListId >= loot.items.size()) {
                return false;
            }

            m_map = map;

            // initialize the data needed for the roll
            m_lootItem = loot.items.get((int) lootListId);

            m_loot = loot;
            m_lootItem.is_blocked = true; // block the item while rolling

            int playerCount = 0;

            for (var allowedLooter : m_lootItem.getAllowedLooters()) {
                var plr = global.getObjAccessor().getPlayer(m_map, allowedLooter);

                if (!plr || !m_lootItem.hasAllowedLooter(plr.getGUID())) // check if player meet the condition to be able to roll this item
                {
                    m_rollVoteMap.get(allowedLooter).vote = RollVote.NotValid;

                    continue;
                }

                // initialize player vote map
                m_rollVoteMap.get(allowedLooter).vote = plr.getPassOnGroupLoot() ? RollVote.Pass : RollVote.NotEmitedYet;

                if (!plr.getPassOnGroupLoot()) {
                    plr.addLootRoll(this);
                }

                ++playerCount;
            }

            // initialize item prototype and check enchant possibilities for this group
            var itemTemplate = global.getObjectMgr().getItemTemplate(m_lootItem.itemid);
            m_voteMask = RollMask.AllMask;

            if (itemTemplate.hasFlag(ItemFlags2.CanOnlyRollGreed)) {
                m_voteMask = RollMask.forValue(m_voteMask.getValue() & ~RollMask.Need.getValue());
            }

            var disenchant = getItemDisenchantLoot();

            if (disenchant == null || disenchant.SkillRequired > enchantingSkill) {
                m_voteMask = RollMask.forValue(m_voteMask.getValue() & ~RollMask.DISENCHANT.getValue());
            }

            if (playerCount > 1) // check if more than one player can loot this item
            {
                // start the roll
                sendStartRoll();
                m_endTime = gameTime.Now() + LOOT_ROLL_TIMEOUT;
                m_isStarted = true;

                return true;
            }

            // no need to start roll if one or less player can loot this item so place it under threshold
            m_lootItem.is_underthreshold = true;
            m_lootItem.is_blocked = false;
        }

        return false;
    }

    // Add vote from playerGuid
    public final boolean playerVote(Player player, RollVote vote) {
        var playerGuid = player.getGUID();

        TValue voter;
        if (!(m_rollVoteMap.containsKey(playerGuid) && (voter = m_rollVoteMap.get(playerGuid)) == voter)) {
            return false;
        }

        voter.vote = vote;

        if (vote != RollVote.Pass && vote != RollVote.NotValid) {
            voter.rollNumber = (byte) RandomUtil.URand(1, 100);
        }

        switch (vote) {
            case Pass: // Player choose pass
            {
                sendRoll(playerGuid, -1, RollVote.Pass, null);

                break;
            }
            case Need: // player choose Need
            {
                sendRoll(playerGuid, 0, RollVote.Need, null);
                player.updateCriteria(CriteriaType.RollAnyNeed, 1);

                break;
            }
            case Greed: // player choose Greed
            {
                sendRoll(playerGuid, -1, RollVote.Greed, null);
                player.updateCriteria(CriteriaType.RollAnyGreed, 1);

                break;
            }
            case Disenchant: // player choose Disenchant
            {
                sendRoll(playerGuid, -1, RollVote.DISENCHANT, null);
                player.updateCriteria(CriteriaType.RollAnyGreed, 1);

                break;
            }
            default: // Roll removed case
                return false;
        }

        return true;
    }

    // check if we can found a winner for this roll or if timer is expired
    public final boolean updateRoll() {
        java.util.Map.entry<ObjectGuid, PlayerRollVote> winner = null;

        tangible.RefObject<java.util.Map.entry<ObjectGuid, PlayerRollVote>> tempRef_winner = new tangible.RefObject<java.util.Map.entry<ObjectGuid, PlayerRollVote>>(winner);
        if (allPlayerVoted(tempRef_winner) || m_endTime.compareTo(gameTime.Now()) <= 0) {
            winner = tempRef_winner.refArgValue;
            finish(winner);

            return true;
        } else {
            winner = tempRef_winner.refArgValue;
        }

        return false;
    }

    public final boolean isLootItem(ObjectGuid lootObject, int lootListId) {
        return Objects.equals(m_loot.getGUID(), lootObject) && m_lootItem.lootListId == lootListId;
    }

    // Send the roll for the whole group
    private void sendStartRoll() {
        var itemTemplate = global.getObjectMgr().getItemTemplate(m_lootItem.itemid);


        for (var(playerGuid, roll) : m_rollVoteMap) {
            if (roll.vote != RollVote.NotEmitedYet) {
                continue;
            }

            var player = global.getObjAccessor().getPlayer(m_map, playerGuid);

            if (player == null) {
                continue;
            }

            StartLootRoll startLootRoll = new StartLootRoll();
            startLootRoll.lootObj = m_loot.getGUID();
            startLootRoll.mapID = (int) m_map.getId();
            startLootRoll.rollTime = (int) LOOT_ROLL_TIMEOUT.TotalMilliseconds;
            startLootRoll.method = m_loot.getLootMethod();
            startLootRoll.validRolls = m_voteMask;

            // In NEED_BEFORE_GREED need disabled for non-usable item for player
            if (m_loot.getLootMethod() == lootMethod.NeedBeforeGreed && player.canRollNeedForItem(itemTemplate, m_map, true) != InventoryResult.Ok) {
                startLootRoll.validRolls = RollMask.forValue(startLootRoll.validRolls.getValue() & ~RollMask.Need.getValue());
            }

            fillPacket(startLootRoll.item);
            startLootRoll.item.UIType = LootSlotType.RollOngoing;

            player.sendPacket(startLootRoll);
        }

        // Handle auto pass option

        for (var(playerGuid, roll) : m_rollVoteMap) {
            if (roll.vote != RollVote.Pass) {
                continue;
            }

            sendRoll(playerGuid, -1, RollVote.Pass, null);
        }
    }

    // Send all passed message
    private void sendAllPassed() {
        LootAllPassed lootAllPassed = new LootAllPassed();
        lootAllPassed.lootObj = m_loot.getGUID();
        fillPacket(lootAllPassed.item);
        lootAllPassed.item.UIType = LootSlotType.AllowLoot;
        lootAllPassed.write();


        for (var(playerGuid, roll) : m_rollVoteMap) {
            if (roll.vote != RollVote.NotValid) {
                continue;
            }

            var player = global.getObjAccessor().getPlayer(m_map, playerGuid);

            if (player == null) {
                continue;
            }

            player.sendPacket(lootAllPassed);
        }
    }

    // Send roll of targetGuid to the whole group (included targuetGuid)
    private void sendRoll(ObjectGuid targetGuid, int rollNumber, RollVote rollType, ObjectGuid rollWinner) {
        LootRollBroadcast lootRoll = new LootRollBroadcast();
        lootRoll.lootObj = m_loot.getGUID();
        lootRoll.player = targetGuid;
        lootRoll.roll = rollNumber;
        lootRoll.rollType = rollType;
        lootRoll.autopassed = false;
        fillPacket(lootRoll.item);
        lootRoll.item.UIType = LootSlotType.RollOngoing;
        lootRoll.write();


        for (var(playerGuid, roll) : m_rollVoteMap) {
            if (roll.vote == RollVote.NotValid) {
                continue;
            }

            if (rollWinner.equals(playerGuid)) {
                continue;
            }

            var player = global.getObjAccessor().getPlayer(m_map, playerGuid);

            if (player == null) {
                continue;
            }

            player.sendPacket(lootRoll);
        }

        if (rollWinner != null) {
            var player = global.getObjAccessor().getPlayer(m_map, rollWinner.getValue());

            if (player != null) {
                lootRoll.item.UIType = LootSlotType.AllowLoot;
                lootRoll.clear();
                player.sendPacket(lootRoll);
            }
        }
    }

    // Send roll 'value' of the whole group and the winner to the whole group
    private void sendLootRollWon(ObjectGuid targetGuid, int rollNumber, RollVote rollType) {
        // Send roll values

        for (var(playerGuid, roll) : m_rollVoteMap) {
            switch (roll.vote) {
                case RollVote.Pass:
                    break;
                case RollVote.NotEmitedYet:
                case RollVote.NotValid:
                    sendRoll(playerGuid, 0, RollVote.Pass, targetGuid);

                    break;
                default:
                    sendRoll(playerGuid, roll.rollNumber, roll.vote, targetGuid);

                    break;
            }
        }

        LootRollWon lootRollWon = new LootRollWon();
        lootRollWon.lootObj = m_loot.getGUID();
        lootRollWon.winner = targetGuid;
        lootRollWon.roll = rollNumber;
        lootRollWon.rollType = rollType;
        fillPacket(lootRollWon.item);
        lootRollWon.item.UIType = LootSlotType.locked;
        lootRollWon.mainSpec = true; // offspec rolls not implemented
        lootRollWon.write();


        for (var(playerGuid, roll) : m_rollVoteMap) {
            if (roll.vote == RollVote.NotValid) {
                continue;
            }

            if (Objects.equals(playerGuid, targetGuid)) {
                continue;
            }

            var player1 = global.getObjAccessor().getPlayer(m_map, playerGuid);

            if (player1 == null) {
                continue;
            }

            player1.sendPacket(lootRollWon);
        }

        var player = global.getObjAccessor().getPlayer(m_map, targetGuid);

        if (player != null) {
            lootRollWon.item.UIType = LootSlotType.AllowLoot;
            lootRollWon.clear();
            player.sendPacket(lootRollWon);
        }
    }

    private void fillPacket(LootItemData lootItem) {
        lootItem.quantity = m_lootItem.count;
        lootItem.lootListID = (byte) m_lootItem.lootListId;
        lootItem.canTradeToTapList = m_lootItem.allowedGUIDs.size() > 1;
        lootItem.loot = new itemInstance(m_lootItem);
    }

    /**
     * \brief Check if all player have voted and return true in that case. Also return current winner.
     * \param winnerItr > will be different than m_rollCoteMap.end() if winner exist. (Someone voted greed or need)
     * \returns true if all players voted
     */
    private boolean allPlayerVoted(tangible.RefObject<java.util.Map.entry<ObjectGuid, PlayerRollVote>> winnerPair) {
        int notVoted = 0;
        var isSomeoneNeed = false;

        winnerPair.refArgValue = null;

        for (var pair : m_rollVoteMap.entrySet()) {
            switch (pair.getValue().vote) {
                case RollVote.Need:
                    if (!isSomeoneNeed || winnerPair.refArgValue.getValue() == null || pair.getValue().rollNumber > winnerPair.refArgValue.getValue().rollNumber) {
                        isSomeoneNeed = true; // first passage will force to set winner because need is prioritized
                        winnerPair.refArgValue = pair;
                    }

                    break;
                case RollVote.Greed:
                case RollVote.Disenchant:
                    if (!isSomeoneNeed) // if at least one need is detected then winner can't be a greed
                    {
                        if (winnerPair.refArgValue.getValue() == null || pair.getValue().rollNumber > winnerPair.refArgValue.getValue().rollNumber) {
                            winnerPair.refArgValue = pair;
                        }
                    }

                    break;
                // Explicitly passing excludes a player from winning loot, so no action required.
                case RollVote.Pass:
                    break;
                case RollVote.NotEmitedYet:
                    ++notVoted;

                    break;
                default:
                    break;
            }
        }

        return notVoted == 0;
    }

    private ItemDisenchantLootRecord getItemDisenchantLoot() {
        ItemInstance itemInstance = new itemInstance(m_lootItem);

        BonusData bonusData = new bonusData(itemInstance);

        if (!bonusData.canDisenchant) {
            return null;
        }

        var itemTemplate = global.getObjectMgr().getItemTemplate(m_lootItem.itemid);
        var itemLevel = item.getItemLevel(itemTemplate, bonusData, 1, 0, 0, 0, 0, false, 0);

        return item.getDisenchantLoot(itemTemplate, (int) bonusData.quality.getValue(), itemLevel);
    }

    // terminate the roll
    private void finish(java.util.Map.entry<ObjectGuid, PlayerRollVote> winnerPair) {
        m_lootItem.is_blocked = false;

        if (winnerPair.getValue() == null) {
            sendAllPassed();
        } else {
            m_lootItem.rollWinnerGUID = winnerPair.getKey();

            sendLootRollWon(winnerPair.getKey(), winnerPair.getValue().rollNumber, winnerPair.getValue().vote);

            var player = global.getObjAccessor().findConnectedPlayer(winnerPair.getKey());

            if (player != null) {
                if (winnerPair.getValue().vote == RollVote.Need) {
                    player.updateCriteria(CriteriaType.RollNeed, m_lootItem.itemid, winnerPair.getValue().rollNumber);
                } else if (winnerPair.getValue().vote == RollVote.DISENCHANT) {
                    player.updateCriteria(CriteriaType.castSpell, 13262);
                } else {
                    player.updateCriteria(CriteriaType.RollGreed, m_lootItem.itemid, winnerPair.getValue().rollNumber);
                }

                if (winnerPair.getValue().vote == RollVote.DISENCHANT) {
                    var disenchant = getItemDisenchantLoot();
                    Loot loot = new loot(m_map, m_loot.getOwnerGUID(), LootType.Disenchanting, null);
                    loot.fillLoot(disenchant.id, LootStorage.DISENCHANT, player, true, false, LootModes.Default, itemContext.NONE);

                    if (!loot.autoStore(player, ItemConst.NullBag, ItemConst.NullSlot, true)) {
                        for (int i = 0; i < loot.items.size(); ++i) {
                            var disenchantLoot = loot.lootItemInSlot(i, player);

                            if (disenchantLoot != null) {
                                player.sendItemRetrievalMail(disenchantLoot.itemid, disenchantLoot.count, disenchantLoot.context);
                            }
                        }
                    } else {
                        m_loot.notifyItemRemoved((byte) m_lootItem.lootListId, m_map);
                    }
                } else {
                    player.storeLootItem(m_loot.getOwnerGUID(), (byte) m_lootItem.lootListId, m_loot);
                }
            }
        }

        m_isStarted = false;
    }

    protected void finalize() throws Throwable {
        if (m_isStarted) {
            sendAllPassed();
        }


        for (var(playerGuid, roll) : m_rollVoteMap) {
            if (roll.vote != RollVote.NotEmitedYet) {
                continue;
            }

            var player = global.getObjAccessor().getPlayer(m_map, playerGuid);

            if (!player) {
                continue;
            }

            player.removeLootRoll(this);
        }
    }
}
