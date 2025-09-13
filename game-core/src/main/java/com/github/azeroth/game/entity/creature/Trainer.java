package com.github.azeroth.game.entity.creature;


import com.github.azeroth.common.Locale;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.common.Logs;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.defines.SkillType;
import com.github.azeroth.defines.SpellEffectName;
import com.github.azeroth.game.battlepet.BattlePetMgr;
import com.github.azeroth.game.domain.creature.TrainerSpell;
import com.github.azeroth.game.domain.creature.TrainerSpellState;
import com.github.azeroth.game.domain.creature.TrainerType;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.npc.TrainerBuyFailed;
import com.github.azeroth.game.networking.packet.npc.TrainerList;
import com.github.azeroth.game.networking.packet.npc.TrainerListSpell;
import com.github.azeroth.game.spell.SpellManager;

import java.util.List;


public class Trainer {

    public final int FAIL_REASON_UNAVAILABLE = 0;
    public final int FAIL_REASON_NOT_ENOUGH_MONEY = 1;
    private final int id;
    private final TrainerType type;
    private final List<TrainerSpell> spells;
    private final LocalizedString greeting = new LocalizedString();

    public Trainer(int id, TrainerType type, String greeting, List<TrainerSpell> spells) {
        this.id = id;
        this.type = type;
        this.spells = spells;
        this.greeting.set(Locale.enUS, greeting);
    }

    public final void sendSpells(Creature npc, Player player, Locale locale) {
        var reputationDiscount = player.getReputationPriceDiscount(npc);

        TrainerList trainerList = new TrainerList();
        trainerList.trainerGUID = npc.getGUID();
        trainerList.trainerType = type;
        trainerList.trainerID = id;
        trainerList.greeting = greeting.get(locale);

        for (var trainerSpell : spells) {
            if (!player.isSpellFitByClassAndRace(trainerSpell.spellId)) {
                continue;
            }

            if (player.getWorldContext().getConditionManager().isObjectMeetingTrainerSpellConditions(id, trainerSpell.spellId, player)) {
                Logs.CONDITION.debug("SendSpells: conditions not met for trainer id {} spell {} player '{}' ({})",
                        id, trainerSpell.spellId, player.getName(), player.getGUID());

                continue;
            }

            TrainerListSpell trainerListSpell = new TrainerListSpell();
            trainerListSpell.spellID = trainerSpell.spellId;
            trainerListSpell.moneyCost = (int) (trainerSpell.moneyCost * reputationDiscount);
            trainerListSpell.reqSkillLine = trainerSpell.reqSkillLine;
            trainerListSpell.reqSkillRank = trainerSpell.reqSkillRank;
            trainerListSpell.reqAbility = trainerSpell.reqAbility;
            trainerListSpell.usable = getSpellState(player, trainerSpell);
            trainerListSpell.reqLevel = trainerSpell.reqLevel;
            trainerList.spells.add(trainerListSpell);
        }

        player.sendPacket(trainerList);
    }

    public final void teachSpell(Creature npc, Player player, int spellId) {
        var trainerSpell = getSpell(spellId);

        if (trainerSpell == null || !canTeachSpell(player, trainerSpell)) {
            sendTeachFailure(npc, player, spellId, FAIL_REASON_UNAVAILABLE);

            return;
        }

        var sendSpellVisual = true;
        var speciesEntry = BattlePetMgr.getBattlePetSpeciesBySpell(trainerSpell.spellId);

        if (speciesEntry != null) {
            if (player.getSession().getBattlePetMgr().hasMaxPetCount(speciesEntry, player.getGUID())) {
                // Don't send any error to client (intended)
                return;
            }

            sendSpellVisual = false;
        }

        var reputationDiscount = player.getReputationPriceDiscount(npc);
        var moneyCost = (long) (trainerSpell.moneyCost * reputationDiscount);

        if (!player.hasEnoughMoney(moneyCost)) {
            sendTeachFailure(npc, player, spellId, FAIL_REASON_NOT_ENOUGH_MONEY);

            return;
        }

        player.modifyMoney(-moneyCost);

        if (sendSpellVisual) {
            npc.sendPlaySpellVisualKit(179, 0, 0); // 53 SpellCastDirected
            player.sendPlaySpellVisualKit(362, 1, 0); // 113 EmoteSalute
        }

        boolean castable = player.getWorldContext().getSpellManager().getSpellInfo(trainerSpell.spellId, Difficulty.NONE).hasEffect(SpellEffectName.LEARN_SPELL);
        // learn explicitly or cast explicitly
        if (castable) {
            player.castSpell(player, trainerSpell.spellId, true);
        } else {
            var dependent = false;

            if (speciesEntry != null) {
                player.getSession().getBattlePetMgr().addPet(speciesEntry.getId(), BattlePetMgr.selectPetDisplay(speciesEntry), BattlePetMgr.rollPetBreed(speciesEntry.getId()), BattlePetMgr.getDefaultPetQuality(speciesEntry.getId()));
                // If the spell summons a battle pet, we fake that it has been learned and the battle pet is added
                // marking as dependent prevents saving the spell to database (intended)
                dependent = true;
            }

            player.learnSpell(trainerSpell.spellId, dependent);
        }
    }

    public final void addGreetingLocale(Locale locale, String greeting) {
        this.greeting.set(locale, greeting);
    }

    private TrainerSpell getSpell(int spellId) {
        return spells.stream().filter(e -> e.spellId == spellId).findFirst().orElse(null);
    }

    private boolean canTeachSpell(Player player, TrainerSpell trainerSpell) {
        var state = getSpellState(player, trainerSpell);

        if (state != TrainerSpellState.AVAILABLE) {
            return false;
        }

        SpellManager spellManager = player.getWorldContext().getSpellManager();
        var trainerSpellInfo = spellManager.getSpellInfo(trainerSpell.spellId, Difficulty.NONE);

        if (trainerSpellInfo.isPrimaryProfessionFirstRank() && player.getFreePrimaryProfessionPoints() == 0) {
            return false;
        }

        for (var effect : trainerSpellInfo.getEffects()) {
            if (!effect.isEffect(SpellEffectName.LEARN_SPELL)) {
                continue;
            }

            var learnedSpellInfo = spellManager.getSpellInfo(effect.triggerSpell, Difficulty.NONE);

            if (learnedSpellInfo != null && learnedSpellInfo.isPrimaryProfessionFirstRank() && player.getFreePrimaryProfessionPoints() == 0) {
                return false;
            }
        }

        return true;
    }

    private TrainerSpellState getSpellState(Player player, TrainerSpell trainerSpell) {
        if (player.hasSpell(trainerSpell.spellId)) {
            return TrainerSpellState.KNOWN;
        }

        // check race/class requirement
        if (!player.isSpellFitByClassAndRace(trainerSpell.spellId)) {
            return TrainerSpellState.UNAVAILABLE;
        }

        // check skill requirement
        if (trainerSpell.reqSkillLine != 0 && player.getBaseSkillValue(SkillType.values()[trainerSpell.reqSkillLine]) < trainerSpell.reqSkillRank) {
            return TrainerSpellState.UNAVAILABLE;
        }

        for (var reqAbility : trainerSpell.reqAbility) {
            if (reqAbility != 0 && !player.hasSpell(reqAbility)) {
                return TrainerSpellState.UNAVAILABLE;
            }
        }

        // check level requirement
        if (player.getLevel() < trainerSpell.reqLevel) {
            return TrainerSpellState.UNAVAILABLE;
        }

        // check ranks
        var hasLearnSpellEffect = false;
        var knowsAllLearnedSpells = true;

        for (var spellEffectInfo : player.getWorldContext().getSpellManager().getSpellInfo(trainerSpell.spellId, Difficulty.NONE).getEffects()) {
            if (!spellEffectInfo.isEffect(SpellEffectName.LEARN_SPELL)) {
                continue;
            }

            hasLearnSpellEffect = true;

            if (!player.hasSpell(spellEffectInfo.triggerSpell)) {
                knowsAllLearnedSpells = false;
            }
        }

        if (hasLearnSpellEffect && knowsAllLearnedSpells) {
            return TrainerSpellState.KNOWN;
        }

        return TrainerSpellState.AVAILABLE;
    }

    private void sendTeachFailure(Creature npc, Player player, int spellId, int reason) {
        TrainerBuyFailed trainerBuyFailed = new TrainerBuyFailed();
        trainerBuyFailed.trainerGUID = npc.getGUID();
        trainerBuyFailed.spellID = spellId;
        trainerBuyFailed.trainerFailedReason = reason;
        player.sendPacket(trainerBuyFailed);
    }
}
