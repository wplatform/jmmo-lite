package com.github.azeroth.game.entity.player;


import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.group.PlayerGroup;

import java.util.TreeSet;


public class KillRewarder {
    private final Player[] killers;
    private final Unit victim;
    private final boolean isBattleground;
    private final boolean isPvP;
    private float groupRate;
    private Player maxNotGrayMember;
    private int count;
    private int sumLevel;
    private int xp;
    private boolean isFullXp;
    private byte maxLevel;

    public KillRewarder(Player[] killers, Unit victim, boolean isBattleground) {
        killers = killers;
        victim = victim;
        groupRate = 1.0f;
        maxNotGrayMember = null;
        count = 0;
        sumLevel = 0;
        xp = 0;
        isFullXp = false;
        maxLevel = 0;
        isBattleground = isBattleground;
        isPvP = false;

        // mark the credit as pvp if victim is player
        if (victim.isTypeId(TypeId.PLAYER)) {
            isPvP = true;
        }
        // or if its owned by player and its not a vehicle
        else if (victim.getCharmerOrOwnerGUID().isPlayer()) {
            isPvP = !victim.isVehicle();
        }
    }

    public final void reward() {
        TreeSet<PlayerGroup> processedGroups = new TreeSet<PlayerGroup>();

        for (var killer : killers) {
            _InitGroupData(killer);

            // 3. Reward killer (and group, if necessary).
            var group = killer.getGroup();

            if (group != null) {
                if (!processedGroups.add(group)) {
                    continue;
                }

                // 3.1. If killer is in group, reward group.
                _RewardGroup(group, killer);
            } else {
                // 3.2. Reward single killer (not group case).
                // 3.2.1. Initialize initial XP amount based on killer's level.
                _InitXP(killer, killer);

                // To avoid unnecessary calculations and calls,
                // proceed only if XP is not ZERO or player is not on battleground
                // (battleground rewards only XP, that's why).
                if (!isBattleground || xp != 0) {
                    // 3.2.2. Reward killer.
                    _RewardPlayer(killer, false);
                }
            }
        }

        // 5. Credit instance encounter.
        // 6. Update guild achievements.
        // 7. Credit scenario criterias
        var victim = victim.toCreature();

        if (victim != null) {
            if (victim.isDungeonBoss()) {
                var instance = victim.getInstanceScript();

                if (instance != null) {
                    instance.updateEncounterStateForKilledCreature(victim.getEntry(), victim);
                }
            }

            if (!killers.isEmpty()) {
                var guildId = victim.getMap().getOwnerGuildId();
                var guild = global.getGuildMgr().getGuildById(guildId);

                if (guild != null) {
                    guild.updateCriteria(CriteriaType.KillCreature, victim.getEntry(), 1, 0, victim, killers.first());
                }

                var scenario = victim.getScenario();

                if (scenario != null) {
                    scenario.updateCriteria(CriteriaType.KillCreature, victim.getEntry(), 1, 0, victim, killers.first());
                }
            }
        }
    }

    private void _InitGroupData(Player killer) {
        var group = killer.getGroup();

        if (group != null) {
            // 2. In case when player is in group, initialize variables necessary for group calculations:
            for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                var member = refe.getSource();

                if (member != null) {
                    if (killer == member || (member.isAtGroupRewardDistance(victim) && member.isAlive())) {
                        var lvl = member.getLevel();
                        // 2.1. _count - number of alive group members within reward distance;
                        ++count;
                        // 2.2. _sumLevel - sum of levels of alive group members within reward distance;
                        sumLevel += lvl;

                        // 2.3. _maxLevel - maximum level of alive group member within reward distance;
                        if (maxLevel < lvl) {
                            maxLevel = (byte) lvl;
                        }

                        // 2.4. _maxNotGrayMember - maximum level of alive group member within reward distance,
                        //      for whom victim is not gray;
                        var grayLevel = Formulas.getGrayLevel(lvl);

                        if (victim.getLevelForTarget(member) > grayLevel && (!maxNotGrayMember || maxNotGrayMember.getLevel() < lvl)) {
                            maxNotGrayMember = member;
                        }
                    }
                }
            }

            // 2.5. _isFullXP - flag identifying that for all group members victim is not gray,
            //      so 100% XP will be rewarded (50% otherwise).
            isFullXp = maxNotGrayMember && (maxLevel == maxNotGrayMember.getLevel());
        } else {
            count = 1;
        }
    }

    private void _InitXP(Player player, Player killer) {
        // Get initial value of XP for kill.
        // XP is given:
        // * on Battlegrounds;
        // * otherwise, not in pvP;
        // * not if killer is on vehicle.
        if (isBattleground || (!isPvP && killer.getVehicle1() == null)) {
            xp = Formulas.XPGain(player, victim, isBattleground);
        }
    }

    private void _RewardHonor(Player player) {
        // Rewarded player must be alive.
        if (player.isAlive()) {
            player.rewardHonor(victim, count, -1, true);
        }
    }

    private void _RewardXP(Player player, float rate) {
        var xp = xp;

        if (player.getGroup() != null) {
            // 4.2.1. If player is in group, adjust XP:
            //        * set to 0 if player's level is more than maximum level of not gray member;
            //        * cut XP in half if _isFullXP is false.
            if (maxNotGrayMember != null && player.isAlive() && maxNotGrayMember.getLevel() >= player.getLevel()) {
                xp = _isFullXp ? (int) (xp * rate) : (int) (xp * rate / 2) + 1; // Reward only HALF of XP if some of group members are gray.
            } else {
                xp = 0;
            }
        }

        if (xp != 0) {
            // 4.2.2. Apply auras modifying rewarded XP (SPELL_AURA_MOD_XP_PCT and SPELL_AURA_MOD_XP_FROM_CREATURE_TYPE).
            xp = (int) (xp * player.getTotalAuraMultiplier(AuraType.ModXpPct));
            xp = (int) (xp * player.getTotalAuraMultiplierByMiscValue(AuraType.ModXpFromCreatureType, victim.getCreatureType().getValue()));

            // 4.2.3. Give XP to player.
            player.giveXP(xp, victim, groupRate);
            var pet = player.getCurrentPet();

            if (pet) {
                // 4.2.4. If player has pet, reward pet with XP (100% for single player, 50% for group case).
                pet.GivePetXP(player.getGroup() != null ? xp / 2 : xp);
            }
        }
    }

    private void _RewardReputation(Player player, float rate) {
        // 4.3. Give reputation (player must not be on BG).
        // Even dead players and corpses are rewarded.
        player.rewardReputation(victim, rate);
    }

    private void _RewardKillCredit(Player player) {
        // 4.4. Give kill credit (player must not be in group, or he must be alive or without corpse).
        if (player.getGroup() == null || player.isAlive() || player.getCorpse() == null) {
            var target = victim.toCreature();

            if (target != null) {
                player.killedMonster(target.getCreatureTemplate(), target.getGUID());
                player.updateCriteria(CriteriaType.KillAnyCreature, (long) target.getCreatureType().getValue(), 1, 0, target);
            }
        }
    }

    private void _RewardPlayer(Player player, boolean isDungeon) {
        // 4. Reward player.
        if (!isBattleground) {
            // 4.1. Give honor (player must be alive and not on BG).
            _RewardHonor(player);

            // 4.1.1 Send player killcredit for quests with PlayerSlain
            if (victim.isTypeId(TypeId.PLAYER)) {
                player.killedPlayerCredit(victim.getGUID());
            }
        }

        // Give XP only in PvE or in Battlegrounds.
        // Give reputation and kill credit only in PvE.
        if (!isPvP || isBattleground) {
            var rate = player.getGroup() != null ? _groupRate * player.getLevel() / _sumLevel : 1.0f;

            if (xp != 0) {
                // 4.2. Give XP.
                _RewardXP(player, rate);
            }

            if (!isBattleground) {
                // If killer is in dungeon then all members receive full reputation at kill.
                _RewardReputation(player, isDungeon ? 1.0f : rate);
                _RewardKillCredit(player);
            }
        }
    }

    private void _RewardGroup(PlayerGroup group, Player killer) {
        if (maxLevel != 0) {
            if (maxNotGrayMember != null) {
                // 3.1.1. Initialize initial XP amount based on maximum level of group member,
                //        for whom victim is not gray.
                _InitXP(maxNotGrayMember, killer);
            }

            // To avoid unnecessary calculations and calls,
            // proceed only if XP is not ZERO or player is not on Battleground
            // (Battlegroundrewards only XP, that's why).
            if (!isBattleground || xp != 0) {
                var isDungeon = !isPvP && CliDB.MapStorage.get(killer.getLocation().getMapId()).IsDungeon();

                if (!isBattleground) {
                    // 3.1.2. Alter group rate if group is in raid (not for Battlegrounds).
                    var isRaid = !isPvP && CliDB.MapStorage.get(killer.getLocation().getMapId()).isRaid() && group.isRaidGroup();
                    groupRate = Formulas.XPInGroupRate(count, isRaid);
                }

                // 3.1.3. Reward each group member (even dead or corpse) within reward distance.
                for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                    var member = refe.getSource();

                    if (member) {
                        // Killer may not be at reward distance, check directly
                        if (killer == member || member.isAtGroupRewardDistance(victim)) {
                            _RewardPlayer(member, isDungeon);
                        }
                    }
                }
            }
        }
    }
}
