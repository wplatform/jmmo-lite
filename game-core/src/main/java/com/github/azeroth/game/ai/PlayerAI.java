package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.AuraRemoveMode;
import com.github.azeroth.game.spell.Spell;
import com.github.azeroth.game.spell.SpellCastTargets;

import java.util.ArrayList;


public class PlayerAI extends UnitAI {
    private final int selfSpec;
    private final boolean isSelfHealer;
    protected Player me;
    private boolean isSelfRangedAttacker;
    public PlayerAI(Player player) {
        super(player);
        me = player;
        selfSpec = player.getPrimarySpecialization();
        isSelfHealer = isPlayerHealer(player);
        isSelfRangedAttacker = isPlayerRangedAttacker(player);
    }

    public final Tuple<spell, unit> verifySpellCast(int spellId, SpellTarget target) {
        Unit pTarget = null;

        switch (target) {
            case None:
                break;
            case Victim:
                pTarget = me.getVictim();

                if (!pTarget) {
                    return null;
                }

                break;
            case Charmer:
                pTarget = me.getCharmer();

                if (!pTarget) {
                    return null;
                }

                break;
            case Self:
                pTarget = me;

                break;
        }

        return verifySpellCast(spellId, pTarget);
    }

    public final Tuple<spell, unit> selectSpellCast(ArrayList<Tuple<Tuple<spell, unit>, Integer>> spells) {
        if (spells.isEmpty()) {
            return null;
        }

        int totalWeights = 0;

        for (var wSpell : spells) {
            totalWeights += wSpell.item2;
        }

        Tuple<spell, unit> selected = null;
        var randNum = RandomUtil.URand(0, totalWeights - 1);

        for (var wSpell : spells) {
            if (selected != null) {
                //delete wSpell.first.first;
                continue;
            }

            if (randNum < wSpell.item2) {
                selected = wSpell.Item1;
            } else {
                randNum -= wSpell.item2;
            }
            //delete wSpell.first.first;
        }

        spells.clear();

        return selected;
    }

    public final <T extends unit> void verifyAndPushSpellCast(ArrayList<Tuple<Tuple<spell, unit>, Integer>> spells, int spellId, T target, int weight) {
        var spell = verifySpellCast(spellId, target);

        if (spell != null) {
            spells.add(Tuple.create(spell, weight));
        }
    }

    public final void doCastAtTarget(Tuple<spell, unit> spell) {
        SpellCastTargets targets = new SpellCastTargets();
        targets.setUnitTarget(spell.item2);
        spell.Item1.prepare(targets);
    }

    public final void doAutoAttackIfReady() {
        if (isRangedAttacker()) {
            doRangedAttackIfReady();
        } else {
            doMeleeAttackIfReady();
        }
    }

    public final void cancelAllShapeshifts() {
        var shapeshiftAuras = me.getAuraEffectsByType(AuraType.ModShapeshift);
        ArrayList<aura> removableShapeshifts = new ArrayList<>();

        for (var auraEff : shapeshiftAuras) {
            var aura = auraEff.getBase();

            if (aura == null) {
                continue;
            }

            var auraInfo = aura.getSpellInfo();

            if (auraInfo == null) {
                continue;
            }

            if (auraInfo.hasAttribute(SpellAttr0.NoAuraCancel)) {
                continue;
            }

            if (!auraInfo.isPositive() || auraInfo.isPassive()) {
                continue;
            }

            removableShapeshifts.add(aura);
        }

        for (var aura : removableShapeshifts) {
            me.removeOwnedAura(aura, AuraRemoveMode.Cancel);
        }
    }

    public final Creature getCharmer() {
        if (me.getCharmerGUID().isCreature()) {
            return ObjectAccessor.getCreature(me, me.getCharmerGUID());
        }

        return null;
    }

    public final boolean isHealer() {
        return isHealer(null);
    }

    // helper functions to determine player info

    public final boolean isHealer(Player who) {
        return (!who || who == me) ? _isSelfHealer : isPlayerHealer(who);
    }

    public final boolean isRangedAttacker() {
        return isRangedAttacker(null);
    }

    public final boolean isRangedAttacker(Player who) {
        return (!who || who == me) ? _isSelfRangedAttacker : isPlayerRangedAttacker(who);
    }

    public final int getSpec() {
        return getSpec(null);
    }

    public final int getSpec(Player who) {
        return (who == null || who == me) ? _selfSpec : who.getPrimarySpecialization();
    }

    public final void setIsRangedAttacker(boolean state) {
        isSelfRangedAttacker = state;
    } // this allows overriding of the default ranged attacker detection

    public Unit selectAttackTarget() {
        return me.getCharmer() ? me.getCharmer().getVictim() : null;
    }

    private boolean isPlayerHealer(Player who) {
        if (!who) {
            return false;
        }

        return switch (who.getClass()) {
            case Paladin -> who.getPrimarySpecialization() == TalentSpecialization.PaladinHoly;
            case Priest ->
                    who.getPrimarySpecialization() == TalentSpecialization.PriestDiscipline || who.getPrimarySpecialization() == TalentSpecialization.PriestHoly;
            case Shaman -> who.getPrimarySpecialization() == TalentSpecialization.ShamanRestoration;
            case Monk -> who.getPrimarySpecialization() == TalentSpecialization.MonkMistweaver;
            case Druid -> who.getPrimarySpecialization() == TalentSpecialization.DruidRestoration;
            default -> false;
        };
    }

    private boolean isPlayerRangedAttacker(Player who) {
        if (!who) {
            return false;
        }

        switch (who.getClass()) {
            case Warrior:
            case Paladin:
            case Rogue:
            case Deathknight:
            default:
                return false;
            case Mage:
            case Warlock:
                return true;
            case Hunter: {
                // check if we have a ranged weapon equipped
                var rangedSlot = who.getItemByPos(InventorySlots.Bag0, EquipmentSlot.Ranged);

                var rangedTemplate = rangedSlot ? rangedSlot.getTemplate() : null;

                if (rangedTemplate != null) {
                    if ((boolean) ((1 << (int) rangedTemplate.getSubClass()) & ItemSubClassWeapon.MaskRanged.getValue())) {
                        return true;
                    }
                }

                return false;
            }
            case Priest:
                return who.getPrimarySpecialization() == TalentSpecialization.PriestShadow;
            case Shaman:
                return who.getPrimarySpecialization() == TalentSpecialization.ShamanElemental;
            case Druid:
                return who.getPrimarySpecialization() == TalentSpecialization.DruidBalance;
        }
    }

    private Tuple<spell, unit> verifySpellCast(int spellId, Unit target) {
        // Find highest spell rank that we know
        int knownRank, nextRank;

        if (me.hasSpell(spellId)) {
            // this will save us some lookups if the player has the highest rank (expected case)
            knownRank = spellId;
            nextRank = global.getSpellMgr().getNextSpellInChain(spellId);
        } else {
            knownRank = 0;
            nextRank = global.getSpellMgr().getFirstSpellInChain(spellId);
        }

        while (nextRank != 0 && me.hasSpell(nextRank)) {
            knownRank = nextRank;
            nextRank = global.getSpellMgr().getNextSpellInChain(knownRank);
        }

        if (knownRank == 0) {
            return null;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(knownRank, me.getMap().getDifficultyID());

        if (spellInfo == null) {
            return null;
        }

        if (me.getSpellHistory().hasGlobalCooldown(spellInfo)) {
            return null;
        }

        Spell spell = new spell(me, spellInfo, TriggerCastFlags.NONE);

        if (spell.canAutoCast(target)) {
            return Tuple.create(spell, target);
        }

        return null;
    }

    private void doRangedAttackIfReady() {
        if (me.hasUnitState(UnitState.Casting)) {
            return;
        }

        if (!me.isAttackReady(WeaponAttackType.RangedAttack)) {
            return;
        }

        var victim = me.getVictim();

        if (!victim) {
            return;
        }

        int rangedAttackSpell = 0;

        var rangedItem = me.getItemByPos(InventorySlots.Bag0, EquipmentSlot.Ranged);
        var rangedTemplate = rangedItem ? rangedItem.getTemplate() : null;

        if (rangedTemplate != null) {
            switch (ItemSubClassWeapon.forValue(rangedTemplate.getSubClass())) {
                case Bow:
                case Gun:
                case Crossbow:
                    rangedAttackSpell = spells.shoot;

                    break;
                case Thrown:
                    rangedAttackSpell = spells. throw ;

                    break;
                case Wand:
                    rangedAttackSpell = spells.wand;

                    break;
            }
        }

        if (rangedAttackSpell == 0) {
            return;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(rangedAttackSpell, me.getMap().getDifficultyID());

        if (spellInfo == null) {
            return;
        }

        Spell spell = new spell(me, spellInfo, TriggerCastFlags.CastDirectly);

        if (spell.checkPetCast(victim) != SpellCastResult.SpellCastOk) {
            return;
        }

        SpellCastTargets targets = new SpellCastTargets();
        targets.setUnitTarget(victim);
        spell.prepare(targets);

        me.resetAttackTimer(WeaponAttackType.RangedAttack);
    }

    public enum SpellTarget {
        NONE,
        victim,
        Charmer,
        Self;

        public static final int SIZE = Integer.SIZE;

        public static SpellTarget forValue(int value) {
            return values()[value];
        }

        public int getValue() {
            return this.ordinal();
        }
    }
}
