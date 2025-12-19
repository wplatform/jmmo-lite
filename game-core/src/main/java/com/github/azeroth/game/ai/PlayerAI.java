package com.github.azeroth.game.ai;



import game.spells.*;

import java.util.*;






public class PlayerAI extends UnitAI {
    public enum SpellTarget {
        None,
        Victim,
        Charmer,
        Self;

        public static final int SIZE = java.lang.Integer.SIZE;

        public int getValue() {
            return this.ordinal();
        }

        public static SpellTarget forValue(int value) {
            return values()[value];
        }
    }

//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: protected new Player Me;
    protected Player me;

//ORIGINAL LINE: readonly uint _selfSpec;
    private final int selfSpec;
    private final boolean isSelfHealer;
    private boolean isSelfRangedAttacker;

    public PlayerAI(Player player) {
        super(player);
        me = player;
        selfSpec = player.getPrimarySpecialization();
        isSelfHealer = isPlayerHealer(player);
        isSelfRangedAttacker = isPlayerRangedAttacker(player);
    }


//ORIGINAL LINE: public Tuple<Spell, Unit> VerifySpellCast(uint spellId, SpellTarget target)
    public final Tuple<Spell, Unit> verifySpellCast(int spellId, SpellTarget target) {
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


//ORIGINAL LINE: public Tuple<Spell, Unit> SelectSpellCast(List<Tuple<Tuple<Spell, Unit>, uint>> spells)
    public final Tuple<Spell, Unit> selectSpellCast(ArrayList<Tuple<Tuple<Spell, Unit>, Integer>> spells) {
        if (spells.Empty()) {
            return null;
        }


//ORIGINAL LINE: uint totalWeights = 0;
        int totalWeights = 0;

        for (var wSpell : spells) {
            totalWeights += wSpell.getItem2();
        }

        Tuple<Spell, Unit> selected = null;
        var randNum = RandomHelper.URand(0, totalWeights - 1);

        for (var wSpell : spells) {
            if (selected != null) {
                //delete wSpell.first.first;
                continue;
            }

            if (randNum < wSpell.getItem2()) {
                selected = wSpell.getItem1();
            } else {
                randNum -= wSpell.getItem2();
            }
            //delete wSpell.first.first;
        }

        spells.clear();

        return selected;
    }

//ORIGINAL LINE: public void VerifyAndPushSpellCast<T>(List<Tuple<Tuple<Spell, Unit>, uint>> spells, uint spellId, T target, uint weight) where T : Unit
    public final <T extends Unit> void verifyAndPushSpellCast(ArrayList<Tuple<Tuple<Spell, Unit>, Integer>> spells, int spellId, T target, int weight) {
        var spell = verifySpellCast(spellId, target);

        if (spell != null) {
            spells.add(Tuple.Create(spell, weight));
        }
    }

    public final void doCastAtTarget(Tuple<Spell, Unit> spell) {
        SpellCastTargets targets = new SpellCastTargets();
        targets.setUnitTarget(spell.getItem2());
        spell.getItem1().prepare(targets);
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
        ArrayList<Aura> removableShapeshifts = new ArrayList<Aura>();

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
            return ObjectAccessor.getCreature(me, me.getCharmerGUID().clone());
        }

        return null;
    }

    // helper functions to determine player info

    public final boolean isHealer() {
        return isHealer(null);
    }


//ORIGINAL LINE: public bool IsHealer(Player who = null)
    public final boolean isHealer(Player who) {
        return (!who || who == me) ? isSelfHealer : isPlayerHealer(who);
    }


    public final boolean isRangedAttacker() {
        return isRangedAttacker(null);
    }


//ORIGINAL LINE: public bool IsRangedAttacker(Player who = null)
    public final boolean isRangedAttacker(Player who) {
        return (!who || who == me) ? isSelfRangedAttacker : isPlayerRangedAttacker(who);
    }


    public final int getSpec() {
        return getSpec(null);
    }


//ORIGINAL LINE: public uint GetSpec(Player who = null)

    public final int getSpec(Player who) {
        return (who == null || who == me) ? selfSpec : who.getPrimarySpecialization();
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
            case Priest -> who.getPrimarySpecialization() == TalentSpecialization.PriestDiscipline || who.getPrimarySpecialization() == TalentSpecialization.PriestHoly;
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
                    if ((boolean)((1 << (int)rangedTemplate.getSubClass()) & ItemSubClassWeapon.MaskRanged.getValue())) {
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


//ORIGINAL LINE: Tuple<Spell, Unit> VerifySpellCast(uint spellId, Unit target)
    private Tuple<Spell, Unit> verifySpellCast(int spellId, Unit target) {
        // Find highest spell rank that we know

//ORIGINAL LINE: uint knownRank, nextRank;
        int knownRank, nextRank;

        if (me.hasSpell(spellId)) {
            // this will save us some lookups if the player has the highest rank (expected case)
            knownRank = spellId;
            nextRank = Global.getSpellMgr().getNextSpellInChain(spellId);
        } else {
            knownRank = 0;
            nextRank = Global.getSpellMgr().getFirstSpellInChain(spellId);
        }

        while (nextRank != 0 && me.hasSpell(nextRank)) {
            knownRank = nextRank;
            nextRank = Global.getSpellMgr().getNextSpellInChain(knownRank);
        }

        if (knownRank == 0) {
            return null;
        }

        var spellInfo = Global.getSpellMgr().getSpellInfo(knownRank, me.getMap().getDifficultyID());

        if (spellInfo == null) {
            return null;
        }

        if (me.getSpellHistory().hasGlobalCooldown(spellInfo)) {
            return null;
        }

        Spell spell = new Spell(me, spellInfo, TriggerCastFlags.None);

        if (spell.canAutoCast(target)) {
            return Tuple.Create(spell, target);
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


//ORIGINAL LINE: uint rangedAttackSpell = 0;
        int rangedAttackSpell = 0;

        var rangedItem = me.getItemByPos(InventorySlots.Bag0, EquipmentSlot.Ranged);
        var rangedTemplate = rangedItem ? rangedItem.getTemplate() : null;

        if (rangedTemplate != null) {
            switch (ItemSubClassWeapon.forValue(rangedTemplate.getSubClass())) {
                case Bow:
                case Gun:
                case Crossbow:
                    rangedAttackSpell = spells.SHOOT;

                    break;
                case Thrown:
                    rangedAttackSpell = spells.THROW;

                    break;
                case Wand:
                    rangedAttackSpell = spells.WAND;

                    break;
            }
        }

        if (rangedAttackSpell == 0) {
            return;
        }

        var spellInfo = Global.getSpellMgr().getSpellInfo(rangedAttackSpell, me.getMap().getDifficultyID());

        if (spellInfo == null) {
            return;
        }

        Spell spell = new Spell(me, spellInfo, TriggerCastFlags.CastDirectly);

        if (spell.checkPetCast(victim) != SpellCastResult.SpellCastOk) {
            return;
        }

        SpellCastTargets targets = new SpellCastTargets();
        targets.setUnitTarget(victim);
        spell.prepare(targets);

        me.resetAttackTimer(WeaponAttackType.RangedAttack);
    }
}