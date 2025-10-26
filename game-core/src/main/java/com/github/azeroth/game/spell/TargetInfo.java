package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.unit.DamageInfo;
import com.github.azeroth.game.entity.unit.HealInfo;
import com.github.azeroth.game.entity.unit.SpellNonMeleeDamage;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnDealDamage;

import java.util.HashMap;


public class TargetInfo extends TargetInfoBase {
    public ObjectGuid targetGuid = ObjectGuid.EMPTY;
    public long timeDelay;
    public double damage;
    public double healing;

    public SpellMissInfo missCondition = SpellMissInfo.values()[0];
    public SpellMissInfo reflectResult = SpellMissInfo.values()[0];

    public boolean isAlive;
    public boolean isCrit;

    // info set at PreprocessTarget, used by DoTargetSpellHit
    public DiminishingGroup drGroup = DiminishingGroup.values()[0];
    public int auraDuration;
    public HashMap<Integer, Double> auraBasePoints = new HashMap<Integer, Double>();
    public boolean positive = true;
    public UnitAura hitAura;

    private Unit spellHitTarget; // changed for example by reflect
    private boolean enablePVP; // need to enable PVP at DoDamageAndTriggers?

    @Override
    public void preprocessTarget(Spell spell) {
        var unit = Objects.equals(spell.getCaster().getGUID(), targetGuid) ? spell.getCaster().toUnit() : global.getObjAccessor().GetUnit(spell.getCaster(), targetGuid);

        if (unit == null) {
            return;
        }

        // Need init unitTarget by default unit (can changed in code on reflect)
        spell.unitTarget = unit;

        // Reset damage/healing counter
        spell.damageInEffects = damage;
        spell.healingInEffects = healing;

        spellHitTarget = null;

        if (missCondition == SpellMissInfo.NONE || (missCondition == SpellMissInfo.Block && !spell.spellInfo.hasAttribute(SpellAttr3.CompletelyBlocked))) {
            spellHitTarget = unit;
        } else if (missCondition == SpellMissInfo.Reflect && reflectResult == SpellMissInfo.NONE) {
            spellHitTarget = spell.getCaster().toUnit();
        }

        if (spell.getOriginalCaster() && missCondition != SpellMissInfo.Evade && !spell.getOriginalCaster().isFriendlyTo(unit) && (!spell.spellInfo.isPositive() || spell.spellInfo.hasEffect(SpellEffectName.dispel)) && (spell.spellInfo.getHasInitialAggro() || unit.isEngaged())) {
            unit.setInCombatWith(spell.getOriginalCaster());
        }

        // if target is flagged for pvp also flag caster if a player
        // but respect current pvp rules (buffing/healing npcs flagged for pvp only flags you if they are in combat)
        enablePVP = (missCondition == SpellMissInfo.NONE || spell.spellInfo.hasAttribute(SpellAttr3.PvpEnabling)) && unit.isPvP() && (unit.isInCombat() || unit.isCharmedOwnedByPlayerOrPlayer()) && spell.getCaster().isPlayer(); // need to check PvP state before spell effects, but act on it afterwards

        if (spellHitTarget) {
            var missInfo = spell.preprocessSpellHit(spellHitTarget, this);

            if (missInfo != SpellMissInfo.NONE) {
                if (missInfo != SpellMissInfo.Miss) {
                    spell.getCaster().sendSpellMiss(unit, spell.spellInfo.getId(), missInfo);
                }

                spell.damageInEffects = 0;
                spell.healingInEffects = 0;
                spellHitTarget = null;
            }
        }

        // scripts can modify damage/healing for current target, save them
        damage = spell.damageInEffects;
        healing = spell.healingInEffects;
    }

    @Override
    public void doTargetSpellHit(Spell spell, SpellEffectInfo spellEffectInfo) {
        var unit = Objects.equals(spell.getCaster().getGUID(), targetGuid) ? spell.getCaster().toUnit() : global.getObjAccessor().GetUnit(spell.getCaster(), targetGuid);

        if (unit == null) {
            return;
        }

        // Need init unitTarget by default unit (can changed in code on reflect)
        // Or on missInfo != SPELL_MISS_NONE unitTarget undefined (but need in trigger subsystem)
        spell.unitTarget = unit;
        spell.targetMissInfo = missCondition;

        // Reset damage/healing counter
        spell.damageInEffects = damage;
        spell.healingInEffects = healing;

        if (unit.isAlive() != isAlive) {
            return;
        }

        if (spell.getState() == SpellState.Delayed && !spell.isPositive() && (gameTime.GetGameTimeMS() - timeDelay) <= unit.getLastSanctuaryTime()) {
            return; // No missinfo in that case
        }

        if (spellHitTarget) {
            spell.doSpellEffectHit(spellHitTarget, spellEffectInfo, this);
        }

        // scripts can modify damage/healing for current target, save them
        damage = spell.damageInEffects;
        healing = spell.healingInEffects;
    }

    @Override
    public void doDamageAndTriggers(Spell spell) {
        var unit = Objects.equals(spell.getCaster().getGUID(), targetGuid) ? spell.getCaster().toUnit() : global.getObjAccessor().GetUnit(spell.getCaster(), targetGuid);

        if (unit == null) {
            return;
        }

        // other targets executed before this one changed pointer
        spell.unitTarget = unit;

        if (spellHitTarget) {
            spell.unitTarget = spellHitTarget;
        }

        // Reset damage/healing counter
        spell.damageInEffects = damage;
        spell.healingInEffects = healing;

        // Get original caster (if exist) and calculate damage/healing from him data
        // Skip if m_originalCaster not available
        var caster = spell.getOriginalCaster() ? spell.getOriginalCaster() : spell.getCaster().toUnit();

        if (caster != null) {
            // Fill base trigger info
            var procAttacker = spell.procAttacker;
            var procVictim = spell.procVictim;
            var procSpellType = ProcFlagsSpellType.NONE;
            var hitMask = ProcFlagsHit.NONE;

            // Spells with this flag cannot trigger if effect is cast on self
            var canEffectTrigger = (!spell.spellInfo.hasAttribute(SpellAttr3.SuppressCasterProcs) || !spell.spellInfo.hasAttribute(SpellAttr3.SuppressTargetProcs)) && spell.unitTarget.getCanProc();

            // Trigger info was not filled in Spell::prepareDataForTriggerSystem - we do it now
            if (canEffectTrigger && !procAttacker && !procVictim) {
                var positive = true;

                if (spell.damageInEffects > 0) {
                    positive = false;
                } else if (spell.healingInEffects == 0) {
                    for (var i = 0; i < spell.spellInfo.getEffects().size(); ++i) {
                        // in case of immunity, check all effects to choose correct procFlags, as none has technically hit
                        if (!effects.contains(i)) {
                            continue;
                        }

                        if (!spell.spellInfo.isPositiveEffect(i)) {
                            positive = false;

                            break;
                        }
                    }
                }

                switch (spell.spellInfo.getDmgClass()) {
                    case None:
                    case Magic:
                        if (spell.spellInfo.hasAttribute(SpellAttr3.TreatAsPeriodic)) {
                            if (positive) {
                                procAttacker.Or(procFlags.DealHelpfulPeriodic);
                                procVictim.Or(procFlags.TakeHelpfulPeriodic);
                            } else {
                                procAttacker.Or(procFlags.DealHarmfulPeriodic);
                                procVictim.Or(procFlags.TakeHarmfulPeriodic);
                            }
                        } else if (spell.spellInfo.hasAttribute(SpellAttr0.IsAbility)) {
                            if (positive) {
                                procAttacker.Or(procFlags.DealHelpfulAbility);
                                procVictim.Or(procFlags.TakeHelpfulAbility);
                            } else {
                                procAttacker.Or(procFlags.DealHarmfulAbility);
                                procVictim.Or(procFlags.TakeHarmfulAbility);
                            }
                        } else {
                            if (positive) {
                                procAttacker.Or(procFlags.DealHelpfulSpell);
                                procVictim.Or(procFlags.TakeHelpfulSpell);
                            } else {
                                procAttacker.Or(procFlags.DealHarmfulSpell);
                                procVictim.Or(procFlags.TakeHarmfulSpell);
                            }
                        }

                        break;
                }
            }

            // All calculated do it!
            // Do healing
            var hasHealing = false;
            DamageInfo spellDamageInfo = null;
            HealInfo healInfo = null;

            if (spell.healingInEffects > 0) {
                hasHealing = true;
                var addhealth = spell.healingInEffects;

                if (isCrit) {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.critical.getValue());
                    addhealth = unit.spellCriticalHealingBonus(caster, spell.spellInfo, addhealth, null);
                } else {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.NORMAL.getValue());
                }

                healInfo = new HealInfo(caster, spell.unitTarget, (int) addhealth, spell.spellInfo, spell.spellInfo.getSchoolMask());
                caster.healBySpell(healInfo, isCrit);
                spell.unitTarget.getThreatManager().forwardThreatForAssistingMe(caster, healInfo.getEffectiveHeal() * 0.5f, spell.spellInfo);
                spell.healingInEffects = (int) healInfo.getEffectiveHeal();

                procSpellType = ProcFlagsSpellType.forValue(procSpellType.getValue() | ProcFlagsSpellType.Heal.getValue());
            }

            // Do damage
            var hasDamage = false;

            if (spell.damageInEffects > 0) {
                hasDamage = true;
                // Fill base damage struct (unitTarget - is real spell target)
                SpellNonMeleeDamage damageInfo = new SpellNonMeleeDamage(caster, spell.unitTarget, spell.spellInfo, spell.spellVisual, spell.spellSchoolMask, spell.castId);

                // Check damage immunity
                if (spell.unitTarget.isImmunedToDamage(spell.spellInfo)) {
                    hitMask = ProcFlagsHit.Immune;
                    spell.damageInEffects = 0;

                    // no packet found in sniffs
                } else {
                    caster.setLastDamagedTargetGuid(spell.unitTarget.getGUID());

                    // Add bonuses and fill damageInfo struct
                    caster.calculateSpellDamageTaken(damageInfo, spell.damageInEffects, spell.spellInfo, spell.attackType, isCrit, missCondition == SpellMissInfo.Block, spell);

                    var p = caster.toPlayer();

                    if (p != null) {
                        tangible.RefObject<Double> tempRef_Damage = new tangible.RefObject<Double>(damageInfo.damage);
                        global.getScriptMgr().<IPlayerOnDealDamage>ForEach(p.getUnitClass(), d -> d.OnDamage(p, spell.unitTarget, tempRef_Damage, spell.spellInfo));
                        damageInfo.damage = tempRef_Damage.refArgValue;
                    }

                    tangible.RefObject<Double> tempRef_Damage2 = new tangible.RefObject<Double>(damageInfo.damage);
                    tangible.RefObject<Double> tempRef_Absorb = new tangible.RefObject<Double>(damageInfo.absorb);
                    unit.dealDamageMods(damageInfo.attacker, damageInfo.target, tempRef_Damage2, tempRef_Absorb);
                    damageInfo.absorb = tempRef_Absorb.refArgValue;
                    damageInfo.damage = tempRef_Damage2.refArgValue;

                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | unit.createProcHitMask(damageInfo, missCondition).getValue());
                    procVictim.Or(procFlags.TakeAnyDamage);

                    spell.damageInEffects = (int) damageInfo.damage;

                    caster.dealSpellDamage(damageInfo, true);

                    // Send log damage message to client
                    caster.sendSpellNonMeleeDamageLog(damageInfo);
                }

                // Do triggers for unit
                if (canEffectTrigger) {
                    spellDamageInfo = new DamageInfo(damageInfo, DamageEffectType.SpellDirect, spell.attackType, hitMask);
                    procSpellType = ProcFlagsSpellType.forValue(procSpellType.getValue() | ProcFlagsSpellType.damage.getValue());
                }
            }

            // Passive spell hits/misses or active spells only misses (only triggers)
            if (!hasHealing && !hasDamage) {
                // Fill base damage struct (unitTarget - is real spell target)
                SpellNonMeleeDamage damageInfo = new SpellNonMeleeDamage(caster, spell.unitTarget, spell.spellInfo, spell.spellVisual, spell.spellSchoolMask);
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | unit.createProcHitMask(damageInfo, missCondition).getValue());

                // Do triggers for unit
                if (canEffectTrigger) {
                    spellDamageInfo = new DamageInfo(damageInfo, DamageEffectType.NoDamage, spell.attackType, hitMask);
                    procSpellType = ProcFlagsSpellType.forValue(procSpellType.getValue() | ProcFlagsSpellType.NoDmgHeal.getValue());
                }

                // Failed Pickpocket, reveal rogue
                if (missCondition == SpellMissInfo.resist && spell.spellInfo.hasAttribute(SpellCustomAttributes.PickPocket) && spell.unitTarget.isCreature()) {
                    var unitCaster = spell.getCaster().toUnit();
                    unitCaster.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.Interacting);
                    spell.unitTarget.toCreature().engageWithTarget(unitCaster);
                }
            }

            // Do triggers for unit
            if (canEffectTrigger) {
                if (spell.spellInfo.hasAttribute(SpellAttr3.SuppressCasterProcs)) {
                    procAttacker = new ProcFlagsInit();
                }

                if (spell.spellInfo.hasAttribute(SpellAttr3.SuppressTargetProcs)) {
                    procVictim = new ProcFlagsInit();
                }

                unit.procSkillsAndAuras(caster, spell.unitTarget, procAttacker, procVictim, procSpellType, ProcFlagsSpellPhase.hit, hitMask, spell, spellDamageInfo, healInfo);

                // item spells (spell hit of non-damage spell may also activate items, for example seal of corruption hidden hit)
                if (caster.isPlayer() && procSpellType.hasFlag(ProcFlagsSpellType.damage.getValue() | ProcFlagsSpellType.NoDmgHeal.getValue())) {
                    if (spell.spellInfo.getDmgClass() == SpellDmgClass.Melee || spell.spellInfo.getDmgClass() == SpellDmgClass.Ranged) {
                        if (!spell.spellInfo.hasAttribute(SpellAttr0.CancelsAutoAttackCombat) && !spell.spellInfo.hasAttribute(SpellAttr4.SuppressWeaponProcs)) {
                            caster.toPlayer().castItemCombatSpell(spellDamageInfo);
                        }
                    }
                }
            }

            // set hitmask for finish procs
            spell.hitMask = ProcFlagsHit.forValue(spell.hitMask.getValue() | hitMask.getValue());

            // Do not take combo points on dodge and miss
            if (missCondition != SpellMissInfo.NONE && spell.needComboPoints && Objects.equals(spell.targets.getUnitTargetGUID(), targetGuid)) {
                spell.needComboPoints = false;
            }

            // _spellHitTarget can be null if spell is missed in DoSpellHitOnUnit
            if (missCondition != SpellMissInfo.Evade && spellHitTarget && !spell.getCaster().isFriendlyTo(unit) && (!spell.isPositive() || spell.spellInfo.hasEffect(SpellEffectName.dispel))) {
                var unitCaster = spell.getCaster().toUnit();

                if (unitCaster != null) {
                    unitCaster.atTargetAttacked(unit, spell.spellInfo.getHasInitialAggro());

                    if (spell.spellInfo.hasAttribute(SpellAttr6.TapsImmediately)) {
                        var targetCreature = unit.toCreature();

                        if (targetCreature != null) {
                            if (unitCaster.isPlayer()) {
                                targetCreature.setTappedBy(unitCaster);
                            }
                        }
                    }
                }

                if (!spell.spellInfo.hasAttribute(SpellAttr3.DoNotTriggerTargetStand) && !unit.isStandState()) {
                    unit.setStandState(UnitStandStateType.Stand);
                }
            }

            // Check for SPELL_ATTR7_INTERRUPT_ONLY_NONPLAYER
            if (missCondition == SpellMissInfo.NONE && spell.spellInfo.hasAttribute(SpellAttr7.InterruptOnlyNonplayer) && !unit.isPlayer()) {
                caster.castSpell(unit, 32747, new CastSpellExtraArgs(spell));
            }
        }

        if (spellHitTarget) {
            //AI functions
            var cHitTarget = spellHitTarget.toCreature();

            if (cHitTarget != null) {
                var hitTargetAI = cHitTarget.getAI();

                if (hitTargetAI != null) {
                    hitTargetAI.spellHit(spell.getCaster(), spell.spellInfo);
                }
            }

            if (spell.getCaster().isCreature() && spell.getCaster().toCreature().isAIEnabled()) {
                spell.getCaster().toCreature().getAI().spellHitTarget(spellHitTarget, spell.spellInfo);
            } else if (spell.getCaster().isGameObject() && spell.getCaster().toGameObject().getAI() != null) {
                spell.getCaster().toGameObject().getAI().spellHitTarget(spellHitTarget, spell.spellInfo);
            }

            if (hitAura != null) {
                var aurApp = hitAura.getApplicationOfTarget(spellHitTarget.getGUID());

                if (aurApp != null) {
                    var effMask = effects.ToHashSet();
                    // only apply unapplied effects (for reapply case)
                    effMask.IntersectWith(aurApp.getEffectsToApply());

                    for (var i = 0; i < spell.spellInfo.getEffects().size(); ++i) {
                        if (effMask.contains(i) && aurApp.hasEffect(i)) {
                            effMask.remove(i);
                        }
                    }

                    if (effMask.count != 0) {
                        spellHitTarget._ApplyAura(aurApp, effMask);
                    }
                }
            }

            // Needs to be called after dealing damage/healing to not remove breaking on damage auras
            spell.doTriggersOnSpellHit(spellHitTarget);
        }

        if (enablePVP) {
            spell.getCaster().toPlayer().updatePvP(true);
        }

        spell.spellAura = hitAura;
        spell.callScriptAfterHitHandlers();
        spell.spellAura = null;
    }
}
