package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;

import java.util.ArrayList;
import java.util.Objects;

class SimpleCharmedPlayerAI extends PlayerAI {
    private static final float CASTER_CHASE_DISTANCE = 28.0f;


    private int castCheckTimer;
    private boolean chaseCloser;
    private boolean forceFacing;
    private boolean isFollowing;

    public SimpleCharmedPlayerAI(Player player) {
        super(player);
        castCheckTimer = 2500;
        chaseCloser = false;
        forceFacing = true;
    }

    @Override
    public boolean canAIAttack(Unit who) {
        if (!me.isValidAttackTarget(who) || who.hasBreakableByDamageCrowdControlAura()) {
            return false;
        }

        var charmer = me.getCharmer();

        if (charmer != null) {
            if (!charmer.isValidAttackTarget(who)) {
                return false;
            }
        }

        return super.canAIAttack(who);
    }

    @Override
    public Unit selectAttackTarget() {
        var charmer = me.getCharmer();

        if (charmer) {
            var charmerAI = charmer.getAI();

            if (charmerAI != null) {
                return charmerAI.selectTarget(SelectTargetMethod.random, 0, new ValidTargetSelectPredicate(this));
            }

            return charmer.getVictim();
        }

        return null;
    }


    @Override
    public void updateAI(int diff) {
        var charmer = getCharmer();

        if (!charmer) {
            return;
        }

        //kill self if charm aura has infinite duration
        if (charmer.isInEvadeMode()) {
            var auras = me.getAuraEffectsByType(AuraType.ModCharm);

            for (var effect : auras) {
                if (Objects.equals(effect.getCasterGuid(), charmer.getGUID()) && effect.getBase().isPermanent()) {
                    me.killSelf();

                    return;
                }
            }
        }

        if (charmer.isEngaged()) {
            var target = me.getVictim();

            if (!target || !canAIAttack(target)) {
                target = selectAttackTarget();

                if (!target || !canAIAttack(target)) {
                    if (!isFollowing) {
                        isFollowing = true;
                        me.attackStop();
                        me.castStop();

                        if (me.hasUnitState(UnitState.chase)) {
                            me.getMotionMaster().remove(MovementGeneratorType.chase);
                        }

                        me.getMotionMaster().moveFollow(charmer, SharedConst.PetFollowDist, SharedConst.PetFollowAngle);
                    }

                    return;
                }

                isFollowing = false;

                if (isRangedAttacker()) {
                    chaseCloser = !me.isWithinLOSInMap(target);

                    if (chaseCloser) {
                        attackStart(target);
                    } else {
                        attackStartCaster(target, CASTER_CHASE_DISTANCE);
                    }
                } else {
                    attackStart(target);
                }

                forceFacing = true;
            }

            if (me.isStopped() && !me.hasUnitState(UnitState.CannotTurn)) {
                var targetAngle = me.getLocation().getAbsoluteAngle(target.getLocation());

                if (forceFacing || Math.abs(me.getLocation().getO() - targetAngle) > 0.4f) {
                    me.setFacingTo(targetAngle);
                    forceFacing = false;
                }
            }

            if (castCheckTimer <= diff) {
                if (me.hasUnitState(UnitState.Casting)) {
                    castCheckTimer = 0;
                } else {
                    if (isRangedAttacker()) // chase to zero if the target isn't in line of sight
                    {
                        var inLOS = me.isWithinLOSInMap(target);

                        if (chaseCloser != !inLOS) {
                            chaseCloser = !inLOS;

                            if (chaseCloser) {
                                attackStart(target);
                            } else {
                                attackStartCaster(target, CASTER_CHASE_DISTANCE);
                            }
                        }
                    }

                    var shouldCast = selectAppropriateCastForSpec();

                    if (shouldCast != null) {
                        doCastAtTarget(shouldCast);
                    }

                    castCheckTimer = 500;
                }
            } else {
                _castCheckTimer -= diff;
            }

            doAutoAttackIfReady();
        } else if (!isFollowing) {
            isFollowing = true;
            me.attackStop();
            me.castStop();

            if (me.hasUnitState(UnitState.chase)) {
                me.getMotionMaster().remove(MovementGeneratorType.chase);
            }

            me.getMotionMaster().moveFollow(charmer, SharedConst.PetFollowDist, SharedConst.PetFollowAngle);
        }
    }

    @Override
    public void onCharmed(boolean isNew) {
        if (me.isCharmed()) {
            me.castStop();
            me.attackStop();

            if (me.getMotionMaster().size() <= 1) // if there is no current movement (we dont want to erase/overwrite any existing stuff)
            {
                me.getMotionMaster().movePoint(0, me.getLocation(), false); // force re-sync of current position for all clients
            }
        } else {
            me.castStop();
            me.attackStop();

            me.getMotionMaster().clear(MovementGeneratorPriority.NORMAL);
        }

        super.onCharmed(isNew);
    }

    private Tuple<spell, unit> selectAppropriateCastForSpec() {
        ArrayList<Tuple<Tuple<spell, unit>, Integer>> spells = new ArrayList<Tuple<Tuple<spell, unit>, Integer>>();

		/*
			switch (me.getClass())
			{
				case CLASS_WARRIOR:
					if (!me.isWithinMeleeRange(me.victim))
					{
						verifyAndPushSpellCast(spells, SPELL_CHARGE, TARGET_VICTIM, 15);
						verifyAndPushSpellCast(spells, SPELL_INTERCEPT, TARGET_VICTIM, 10);
					}
					verifyAndPushSpellCast(spells, SPELL_ENRAGED_REGEN, TARGET_NONE, 3);
					verifyAndPushSpellCast(spells, SPELL_INTIMIDATING_SHOUT, TARGET_VICTIM, 4);
					if (me.victim && me.victim.hasUnitState(UNIT_STATE_CASTING))
					{
						verifyAndPushSpellCast(spells, SPELL_PUMMEL, TARGET_VICTIM, 15);
						verifyAndPushSpellCast(spells, SPELL_SHIELD_BASH, TARGET_VICTIM, 15);
					}
					verifyAndPushSpellCast(spells, SPELL_BLOODRAGE, TARGET_NONE, 5);
					switch (getSpec())
					{
						case TALENT_SPEC_WARRIOR_PROTECTION:
							verifyAndPushSpellCast(spells, SPELL_SHOCKWAVE, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_CONCUSSION_BLOW, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_DISARM, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_LAST_STAND, TARGET_NONE, 5);
							verifyAndPushSpellCast(spells, SPELL_SHIELD_BLOCK, TARGET_NONE, 1);
							verifyAndPushSpellCast(spells, SPELL_SHIELD_SLAM, TARGET_VICTIM, 4);
							verifyAndPushSpellCast(spells, SPELL_SHIELD_WALL, TARGET_NONE, 5);
							verifyAndPushSpellCast(spells, SPELL_SPELL_REFLECTION, TARGET_NONE, 3);
							verifyAndPushSpellCast(spells, SPELL_DEVASTATE, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_REND, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_THUNDER_CLAP, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_DEMO_SHOUT, TARGET_VICTIM, 1);
							break;
						case TALENT_SPEC_WARRIOR_ARMS:
							verifyAndPushSpellCast(spells, SPELL_SWEEPING_STRIKES, TARGET_NONE, 2);
							verifyAndPushSpellCast(spells, SPELL_MORTAL_STRIKE, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_BLADESTORM, TARGET_NONE, 10);
							verifyAndPushSpellCast(spells, SPELL_REND, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_RETALIATION, TARGET_NONE, 3);
							verifyAndPushSpellCast(spells, SPELL_SHATTERING_THROW, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_SWEEPING_STRIKES, TARGET_NONE, 5);
							verifyAndPushSpellCast(spells, SPELL_THUNDER_CLAP, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_EXECUTE, TARGET_VICTIM, 15);
							break;
						case TALENT_SPEC_WARRIOR_FURY:
							verifyAndPushSpellCast(spells, SPELL_DEATH_WISH, TARGET_NONE, 10);
							verifyAndPushSpellCast(spells, SPELL_BLOODTHIRST, TARGET_VICTIM, 4);
							verifyAndPushSpellCast(spells, SPELL_DEMO_SHOUT, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_EXECUTE, TARGET_VICTIM, 15);
							verifyAndPushSpellCast(spells, SPELL_HEROIC_FURY, TARGET_NONE, 5);
							verifyAndPushSpellCast(spells, SPELL_RECKLESSNESS, TARGET_NONE, 8);
							verifyAndPushSpellCast(spells, SPELL_PIERCING_HOWL, TARGET_VICTIM, 2);
							break;
					}
					break;
				case CLASS_PALADIN:
					verifyAndPushSpellCast(spells, SPELL_AURA_MASTERY, TARGET_NONE, 3);
					verifyAndPushSpellCast(spells, SPELL_LAY_ON_HANDS, TARGET_CHARMER, 8);
					verifyAndPushSpellCast(spells, SPELL_BLESSING_OF_MIGHT, TARGET_CHARMER, 8);
					verifyAndPushSpellCast(spells, SPELL_AVENGING_WRATH, TARGET_NONE, 5);
					verifyAndPushSpellCast(spells, SPELL_DIVINE_PROTECTION, TARGET_NONE, 4);
					verifyAndPushSpellCast(spells, SPELL_DIVINE_SHIELD, TARGET_NONE, 2);
					verifyAndPushSpellCast(spells, SPELL_HAMMER_OF_JUSTICE, TARGET_VICTIM, 6);
					verifyAndPushSpellCast(spells, SPELL_HAND_OF_FREEDOM, TARGET_SELF, 3);
					verifyAndPushSpellCast(spells, SPELL_HAND_OF_PROTECTION, TARGET_SELF, 1);
					if (Creature* creatureCharmer = getCharmer())
					{
						if (creatureCharmer.IsDungeonBoss() || creatureCharmer.isWorldBoss())
							verifyAndPushSpellCast(spells, SPELL_HAND_OF_SACRIFICE, creatureCharmer, 10);
						else
							verifyAndPushSpellCast(spells, SPELL_HAND_OF_PROTECTION, creatureCharmer, 3);
					}

					switch (getSpec())
					{
						case TALENT_SPEC_PALADIN_PROTECTION:
							verifyAndPushSpellCast(spells, SPELL_HAMMER_OF_RIGHTEOUS, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_DIVINE_SACRIFICE, TARGET_NONE, 2);
							verifyAndPushSpellCast(spells, SPELL_SHIELD_OF_RIGHTEOUS, TARGET_VICTIM, 4);
							verifyAndPushSpellCast(spells, SPELL_JUDGEMENT, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_CONSECRATION, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_HOLY_SHIELD, TARGET_NONE, 1);
							break;
						case TALENT_SPEC_PALADIN_HOLY:
							verifyAndPushSpellCast(spells, SPELL_HOLY_SHOCK, TARGET_CHARMER, 3);
							verifyAndPushSpellCast(spells, SPELL_HOLY_SHOCK, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_FLASH_OF_LIGHT, TARGET_CHARMER, 4);
							verifyAndPushSpellCast(spells, SPELL_HOLY_LIGHT, TARGET_CHARMER, 3);
							verifyAndPushSpellCast(spells, SPELL_DIVINE_FAVOR, TARGET_NONE, 5);
							verifyAndPushSpellCast(spells, SPELL_DIVINE_ILLUMINATION, TARGET_NONE, 3);
							break;
						case TALENT_SPEC_PALADIN_RETRIBUTION:
							verifyAndPushSpellCast(spells, SPELL_CRUSADER_STRIKE, TARGET_VICTIM, 4);
							verifyAndPushSpellCast(spells, SPELL_DIVINE_STORM, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_JUDGEMENT, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_HAMMER_OF_WRATH, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_RIGHTEOUS_FURY, TARGET_NONE, 2);
							break;
					}
					break;
				case CLASS_HUNTER:
					verifyAndPushSpellCast(spells, SPELL_DETERRENCE, TARGET_NONE, 3);
					verifyAndPushSpellCast(spells, SPELL_EXPLOSIVE_TRAP, TARGET_NONE, 1);
					verifyAndPushSpellCast(spells, SPELL_FREEZING_ARROW, TARGET_VICTIM, 2);
					verifyAndPushSpellCast(spells, SPELL_RAPID_FIRE, TARGET_NONE, 10);
					verifyAndPushSpellCast(spells, SPELL_KILL_SHOT, TARGET_VICTIM, 10);
					if (me.victim && me.victim.getPowerType() == POWER_MANA && !me.victim.getAuraApplicationOfRankedSpell(SPELL_VIPER_STING, me.getGUID()))
						verifyAndPushSpellCast(spells, SPELL_VIPER_STING, TARGET_VICTIM, 5);

					switch (getSpec())
					{
						case TALENT_SPEC_HUNTER_BEASTMASTER:
							verifyAndPushSpellCast(spells, SPELL_AIMED_SHOT, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_ARCANE_SHOT, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_STEADY_SHOT, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_MULTI_SHOT, TARGET_VICTIM, 2);
							break;
						case TALENT_SPEC_HUNTER_MARKSMAN:
							verifyAndPushSpellCast(spells, SPELL_AIMED_SHOT, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_CHIMERA_SHOT, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_ARCANE_SHOT, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_STEADY_SHOT, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_READINESS, TARGET_NONE, 10);
							verifyAndPushSpellCast(spells, SPELL_SILENCING_SHOT, TARGET_VICTIM, 5);
							break;
						case TALENT_SPEC_HUNTER_SURVIVAL:
							verifyAndPushSpellCast(spells, SPELL_EXPLOSIVE_SHOT, TARGET_VICTIM, 8);
							verifyAndPushSpellCast(spells, SPELL_BLACK_ARROW, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_MULTI_SHOT, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_STEADY_SHOT, TARGET_VICTIM, 1);
							break;
					}
					break;
				case CLASS_ROGUE:
				{
					verifyAndPushSpellCast(spells, SPELL_DISMANTLE, TARGET_VICTIM, 8);
					verifyAndPushSpellCast(spells, SPELL_EVASION, TARGET_NONE, 8);
					verifyAndPushSpellCast(spells, SPELL_VANISH, TARGET_NONE, 4);
					verifyAndPushSpellCast(spells, SPELL_BLIND, TARGET_VICTIM, 2);
					verifyAndPushSpellCast(spells, SPELL_CLOAK_OF_SHADOWS, TARGET_NONE, 2);

					uint32 builder, finisher;
					switch (getSpec())
					{
						case TALENT_SPEC_ROGUE_ASSASSINATION:
							builder = SPELL_MUTILATE, finisher = SPELL_ENVENOM;
							verifyAndPushSpellCast(spells, SPELL_COLD_BLOOD, TARGET_NONE, 20);
							break;
						case TALENT_SPEC_ROGUE_COMBAT:
							builder = SPELL_SINISTER_STRIKE, finisher = SPELL_EVISCERATE;
							verifyAndPushSpellCast(spells, SPELL_ADRENALINE_RUSH, TARGET_NONE, 6);
							verifyAndPushSpellCast(spells, SPELL_BLADE_FLURRY, TARGET_NONE, 5);
							verifyAndPushSpellCast(spells, SPELL_KILLING_SPREE, TARGET_NONE, 25);
							break;
						case TALENT_SPEC_ROGUE_SUBTLETY:
							builder = SPELL_HEMORRHAGE, finisher = SPELL_EVISCERATE;
							verifyAndPushSpellCast(spells, SPELL_PREPARATION, TARGET_NONE, 10);
							if (!me.isWithinMeleeRange(me.victim))
								verifyAndPushSpellCast(spells, SPELL_SHADOWSTEP, TARGET_VICTIM, 25);
							verifyAndPushSpellCast(spells, SPELL_SHADOW_DANCE, TARGET_NONE, 10);
							break;
					}

					if (Unit* victim = me.victim)
					{
						if (victim.hasUnitState(UNIT_STATE_CASTING))
							verifyAndPushSpellCast(spells, SPELL_KICK, TARGET_VICTIM, 25);

						uint8 const cp = me.getPower(POWER_COMBO_POINTS);
						if (cp >= 4)
							verifyAndPushSpellCast(spells, finisher, TARGET_VICTIM, 10);
						if (cp <= 4)
							verifyAndPushSpellCast(spells, builder, TARGET_VICTIM, 5);
					}
					break;
				}
				case CLASS_PRIEST:
					verifyAndPushSpellCast(spells, SPELL_FEAR_WARD, TARGET_SELF, 2);
					verifyAndPushSpellCast(spells, SPELL_POWER_WORD_FORT, TARGET_CHARMER, 1);
					verifyAndPushSpellCast(spells, SPELL_DIVINE_SPIRIT, TARGET_CHARMER, 1);
					verifyAndPushSpellCast(spells, SPELL_SHADOW_PROTECTION, TARGET_CHARMER, 2);
					verifyAndPushSpellCast(spells, SPELL_DIVINE_HYMN, TARGET_NONE, 5);
					verifyAndPushSpellCast(spells, SPELL_HYMN_OF_HOPE, TARGET_NONE, 5);
					verifyAndPushSpellCast(spells, SPELL_SHADOW_WORD_DEATH, TARGET_VICTIM, 1);
					verifyAndPushSpellCast(spells, SPELL_PSYCHIC_SCREAM, TARGET_VICTIM, 3);
					switch (getSpec())
					{
						case TALENT_SPEC_PRIEST_DISCIPLINE:
							verifyAndPushSpellCast(spells, SPELL_POWER_WORD_SHIELD, TARGET_CHARMER, 3);
							verifyAndPushSpellCast(spells, SPELL_INNER_FOCUS, TARGET_NONE, 3);
							verifyAndPushSpellCast(spells, SPELL_PAIN_SUPPRESSION, TARGET_CHARMER, 15);
							verifyAndPushSpellCast(spells, SPELL_POWER_INFUSION, TARGET_CHARMER, 10);
							verifyAndPushSpellCast(spells, SPELL_PENANCE, TARGET_CHARMER, 3);
							verifyAndPushSpellCast(spells, SPELL_FLASH_HEAL, TARGET_CHARMER, 1);
							break;
						case TALENT_SPEC_PRIEST_HOLY:
							verifyAndPushSpellCast(spells, SPELL_DESPERATE_PRAYER, TARGET_NONE, 3);
							verifyAndPushSpellCast(spells, SPELL_GUARDIAN_SPIRIT, TARGET_CHARMER, 5);
							verifyAndPushSpellCast(spells, SPELL_FLASH_HEAL, TARGET_CHARMER, 1);
							verifyAndPushSpellCast(spells, SPELL_RENEW, TARGET_CHARMER, 3);
							break;
						case TALENT_SPEC_PRIEST_SHADOW:
							if (!me.hasAura(SPELL_SHADOWFORM))
							{
								verifyAndPushSpellCast(spells, SPELL_SHADOWFORM, TARGET_NONE, 100);
								break;
							}
							if (Unit* victim = me.victim)
							{
								if (!victim.getAuraApplicationOfRankedSpell(SPELL_VAMPIRIC_TOUCH, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_VAMPIRIC_TOUCH, TARGET_VICTIM, 4);
								if (!victim.getAuraApplicationOfRankedSpell(SPELL_SHADOW_WORD_PAIN, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_SHADOW_WORD_PAIN, TARGET_VICTIM, 3);
								if (!victim.getAuraApplicationOfRankedSpell(SPELL_DEVOURING_PLAGUE, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_DEVOURING_PLAGUE, TARGET_VICTIM, 4);
							}
							verifyAndPushSpellCast(spells, SPELL_MIND_BLAST, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_MIND_FLAY, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_DISPERSION, TARGET_NONE, 10);
							break;
					}
					break;
				case CLASS_DEATH_KNIGHT:
				{
					if (!me.isWithinMeleeRange(me.victim))
						verifyAndPushSpellCast(spells, SPELL_DEATH_GRIP, TARGET_VICTIM, 25);
					verifyAndPushSpellCast(spells, SPELL_STRANGULATE, TARGET_VICTIM, 15);
					verifyAndPushSpellCast(spells, SPELL_EMPOWER_RUNE_WEAP, TARGET_NONE, 5);
					verifyAndPushSpellCast(spells, SPELL_ICEBORN_FORTITUDE, TARGET_NONE, 15);
					verifyAndPushSpellCast(spells, SPELL_ANTI_MAGIC_SHELL, TARGET_NONE, 10);

					bool hasFF = false, hasBP = false;
					if (Unit* victim = me.victim)
					{
						if (victim.hasUnitState(UNIT_STATE_CASTING))
							verifyAndPushSpellCast(spells, SPELL_MIND_FREEZE, TARGET_VICTIM, 25);

						hasFF = !!victim.getAuraApplicationOfRankedSpell(AURA_FROST_FEVER, me.getGUID()), hasBP = !!victim.getAuraApplicationOfRankedSpell(AURA_BLOOD_PLAGUE, me.getGUID());
						if (hasFF && hasBP)
							verifyAndPushSpellCast(spells, SPELL_PESTILENCE, TARGET_VICTIM, 3);
						if (!hasFF)
							verifyAndPushSpellCast(spells, SPELL_ICY_TOUCH, TARGET_VICTIM, 4);
						if (!hasBP)
							verifyAndPushSpellCast(spells, SPELL_PLAGUE_STRIKE, TARGET_VICTIM, 4);
					}
					switch (getSpec())
					{
						case TALENT_SPEC_DEATHKNIGHT_BLOOD:
							verifyAndPushSpellCast(spells, SPELL_RUNE_TAP, TARGET_NONE, 2);
							verifyAndPushSpellCast(spells, SPELL_HYSTERIA, TARGET_SELF, 5);
							if (Creature* creatureCharmer = getCharmer())
								if (!creatureCharmer.IsDungeonBoss() && !creatureCharmer.isWorldBoss())
									verifyAndPushSpellCast(spells, SPELL_HYSTERIA, creatureCharmer, 15);
							verifyAndPushSpellCast(spells, SPELL_HEART_STRIKE, TARGET_VICTIM, 2);
							if (hasFF && hasBP)
								verifyAndPushSpellCast(spells, SPELL_DEATH_STRIKE, TARGET_VICTIM, 8);
							verifyAndPushSpellCast(spells, SPELL_DEATH_COIL_DK, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_MARK_OF_BLOOD, TARGET_VICTIM, 20);
							verifyAndPushSpellCast(spells, SPELL_VAMPIRIC_BLOOD, TARGET_NONE, 10);
							break;
						case TALENT_SPEC_DEATHKNIGHT_FROST:
							if (hasFF && hasBP)
								verifyAndPushSpellCast(spells, SPELL_OBLITERATE, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_HOWLING_BLAST, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_UNBREAKABLE_ARMOR, TARGET_NONE, 10);
							verifyAndPushSpellCast(spells, SPELL_DEATHCHILL, TARGET_NONE, 10);
							verifyAndPushSpellCast(spells, SPELL_FROST_STRIKE, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_BLOOD_STRIKE, TARGET_VICTIM, 1);
							break;
						case TALENT_SPEC_DEATHKNIGHT_UNHOLY:
							if (hasFF && hasBP)
								verifyAndPushSpellCast(spells, SPELL_SCOURGE_STRIKE, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_DEATH_AND_DECAY, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_ANTI_MAGIC_ZONE, TARGET_NONE, 8);
							verifyAndPushSpellCast(spells, SPELL_SUMMON_GARGOYLE, TARGET_VICTIM, 7);
							verifyAndPushSpellCast(spells, SPELL_BLOOD_STRIKE, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_DEATH_COIL_DK, TARGET_VICTIM, 3);
							break;
					}
					break;
				}
				case CLASS_SHAMAN:
					verifyAndPushSpellCast(spells, SPELL_HEROISM, TARGET_NONE, 25);
					verifyAndPushSpellCast(spells, SPELL_BLOODLUST, TARGET_NONE, 25);
					verifyAndPushSpellCast(spells, SPELL_GROUNDING_TOTEM, TARGET_NONE, 2);
					switch (getSpec())
					{
						case TALENT_SPEC_SHAMAN_RESTORATION:
							if (Unit* charmer = me.getCharmer())
								if (!charmer.getAuraApplicationOfRankedSpell(SPELL_EARTH_SHIELD, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_EARTH_SHIELD, charmer, 2);
							if (me.hasAura(SPELL_SHA_NATURE_SWIFT))
								verifyAndPushSpellCast(spells, SPELL_HEALING_WAVE, TARGET_CHARMER, 20);
							else
								verifyAndPushSpellCast(spells, SPELL_LESSER_HEAL_WAVE, TARGET_CHARMER, 1);
							verifyAndPushSpellCast(spells, SPELL_TIDAL_FORCE, TARGET_NONE, 4);
							verifyAndPushSpellCast(spells, SPELL_SHA_NATURE_SWIFT, TARGET_NONE, 4);
							verifyAndPushSpellCast(spells, SPELL_MANA_TIDE_TOTEM, TARGET_NONE, 3);
							break;
						case TALENT_SPEC_SHAMAN_ELEMENTAL:
							if (Unit* victim = me.victim)
							{
								if (victim.getAuraOfRankedSpell(SPELL_FLAME_SHOCK, getGUID()))
									verifyAndPushSpellCast(spells, SPELL_LAVA_BURST, TARGET_VICTIM, 5);
								else
									verifyAndPushSpellCast(spells, SPELL_FLAME_SHOCK, TARGET_VICTIM, 3);
							}
							verifyAndPushSpellCast(spells, SPELL_CHAIN_LIGHTNING, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_LIGHTNING_BOLT, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_ELEMENTAL_MASTERY, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_THUNDERSTORM, TARGET_NONE, 3);
							break;
						case TALENT_SPEC_SHAMAN_ENHANCEMENT:
							if (Aura const* maelstrom = me.getAura(AURA_MAELSTROM_WEAPON))
								if (maelstrom.GetStackAmount() == 5)
									verifyAndPushSpellCast(spells, SPELL_LIGHTNING_BOLT, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_STORMSTRIKE, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_EARTH_SHOCK, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_LAVA_LASH, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_SHAMANISTIC_RAGE, TARGET_NONE, 10);
							break;
					}
					break;
				case CLASS_MAGE:
					if (me.victim && me.victim.hasUnitState(UNIT_STATE_CASTING))
						verifyAndPushSpellCast(spells, SPELL_COUNTERSPELL, TARGET_VICTIM, 25);
					verifyAndPushSpellCast(spells, SPELL_DAMPEN_MAGIC, TARGET_CHARMER, 2);
					verifyAndPushSpellCast(spells, SPELL_EVOCATION, TARGET_NONE, 3);
					verifyAndPushSpellCast(spells, SPELL_MANA_SHIELD, TARGET_NONE, 1);
					verifyAndPushSpellCast(spells, SPELL_MIRROR_IMAGE, TARGET_NONE, 3);
					verifyAndPushSpellCast(spells, SPELL_SPELLSTEAL, TARGET_VICTIM, 2);
					verifyAndPushSpellCast(spells, SPELL_ICE_BLOCK, TARGET_NONE, 1);
					verifyAndPushSpellCast(spells, SPELL_ICY_VEINS, TARGET_NONE, 3);
					switch (getSpec())
					{
						case TALENT_SPEC_MAGE_ARCANE:
							if (Aura* abAura = me.getAura(AURA_ARCANE_BLAST))
								if (abAura.GetStackAmount() >= 3)
									verifyAndPushSpellCast(spells, SPELL_ARCANE_MISSILES, TARGET_VICTIM, 7);
							verifyAndPushSpellCast(spells, SPELL_ARCANE_BLAST, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_ARCANE_BARRAGE, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_ARCANE_POWER, TARGET_NONE, 8);
							verifyAndPushSpellCast(spells, SPELL_PRESENCE_OF_MIND, TARGET_NONE, 7);
							break;
						case TALENT_SPEC_MAGE_FIRE:
							if (me.victim && !me.victim.getAuraApplicationOfRankedSpell(SPELL_LIVING_BOMB))
								verifyAndPushSpellCast(spells, SPELL_LIVING_BOMB, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_COMBUSTION, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_FIREBALL, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_FIRE_BLAST, TARGET_VICTIM, 1);
							verifyAndPushSpellCast(spells, SPELL_DRAGONS_BREATH, TARGET_VICTIM, 2);
							verifyAndPushSpellCast(spells, SPELL_BLAST_WAVE, TARGET_VICTIM, 1);
							break;
						case TALENT_SPEC_MAGE_FROST:
							verifyAndPushSpellCast(spells, SPELL_DEEP_FREEZE, TARGET_VICTIM, 10);
							verifyAndPushSpellCast(spells, SPELL_FROST_NOVA, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_FROSTBOLT, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_COLD_SNAP, TARGET_VICTIM, 5);
							if (me.victim && me.victim.hasAuraState(AURA_STATE_FROZEN, nullptr, me))
								verifyAndPushSpellCast(spells, SPELL_ICE_LANCE, TARGET_VICTIM, 5);
							break;
					}
					break;
				case CLASS_WARLOCK:
					verifyAndPushSpellCast(spells, SPELL_DEATH_COIL_W, TARGET_VICTIM, 2);
					verifyAndPushSpellCast(spells, SPELL_FEAR, TARGET_VICTIM, 2);
					verifyAndPushSpellCast(spells, SPELL_SEED_OF_CORRUPTION, TARGET_VICTIM, 4);
					verifyAndPushSpellCast(spells, SPELL_HOWL_OF_TERROR, TARGET_NONE, 2);
					if (me.victim && !me.victim.getAuraApplicationOfRankedSpell(SPELL_CORRUPTION, me.getGUID()))
						verifyAndPushSpellCast(spells, SPELL_CORRUPTION, TARGET_VICTIM, 10);
					switch (getSpec())
					{
						case TALENT_SPEC_WARLOCK_AFFLICTION:
							if (Unit* victim = me.victim)
							{
								verifyAndPushSpellCast(spells, SPELL_SHADOW_BOLT, TARGET_VICTIM, 7);
								if (!victim.getAuraApplicationOfRankedSpell(SPELL_UNSTABLE_AFFLICTION, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_UNSTABLE_AFFLICTION, TARGET_VICTIM, 8);
								if (!victim.getAuraApplicationOfRankedSpell(SPELL_HAUNT, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_HAUNT, TARGET_VICTIM, 8);
								if (!victim.getAuraApplicationOfRankedSpell(SPELL_CURSE_OF_AGONY, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_CURSE_OF_AGONY, TARGET_VICTIM, 4);
								if (victim.healthBelowPct(25))
									verifyAndPushSpellCast(spells, SPELL_DRAIN_SOUL, TARGET_VICTIM, 100);
							}
							break;
						case TALENT_SPEC_WARLOCK_DEMONOLOGY:
							verifyAndPushSpellCast(spells, SPELL_METAMORPHOSIS, TARGET_NONE, 15);
							verifyAndPushSpellCast(spells, SPELL_SHADOW_BOLT, TARGET_VICTIM, 7);
							if (me.hasAura(AURA_DECIMATION))
								verifyAndPushSpellCast(spells, SPELL_SOUL_FIRE, TARGET_VICTIM, 100);
							if (me.hasAura(SPELL_METAMORPHOSIS))
							{
								verifyAndPushSpellCast(spells, SPELL_IMMOLATION_AURA, TARGET_NONE, 30);
								if (!me.isWithinMeleeRange(me.victim))
									verifyAndPushSpellCast(spells, SPELL_DEMON_CHARGE, TARGET_VICTIM, 20);
							}
							if (me.victim && !me.victim.getAuraApplicationOfRankedSpell(SPELL_IMMOLATE, me.getGUID()))
								verifyAndPushSpellCast(spells, SPELL_IMMOLATE, TARGET_VICTIM, 5);
							if (me.hasAura(AURA_MOLTEN_CORE))
								verifyAndPushSpellCast(spells, SPELL_INCINERATE, TARGET_VICTIM, 10);
							break;
						case TALENT_SPEC_WARLOCK_DESTRUCTION:
							if (me.victim && !me.victim.getAuraApplicationOfRankedSpell(SPELL_IMMOLATE, me.getGUID()))
								verifyAndPushSpellCast(spells, SPELL_IMMOLATE, TARGET_VICTIM, 8);
							if (me.victim && me.victim.getAuraApplicationOfRankedSpell(SPELL_IMMOLATE, me.getGUID()))
								verifyAndPushSpellCast(spells, SPELL_CONFLAGRATE, TARGET_VICTIM, 8);
							verifyAndPushSpellCast(spells, SPELL_SHADOWFURY, TARGET_VICTIM, 5);
							verifyAndPushSpellCast(spells, SPELL_CHAOS_BOLT, TARGET_VICTIM, 10);
							verifyAndPushSpellCast(spells, SPELL_SHADOWBURN, TARGET_VICTIM, 3);
							verifyAndPushSpellCast(spells, SPELL_INCINERATE, TARGET_VICTIM, 7);
							break;
					}
					break;
				case CLASS_MONK:
					switch (getSpec())
					{
						case TALENT_SPEC_MONK_BREWMASTER:
						case TALENT_SPEC_MONK_BATTLEDANCER:
						case TALENT_SPEC_MONK_MISTWEAVER:
							break;
					}
					break;
				case CLASS_DRUID:
					verifyAndPushSpellCast(spells, SPELL_INNERVATE, TARGET_CHARMER, 5);
					verifyAndPushSpellCast(spells, SPELL_BARKSKIN, TARGET_NONE, 5);
					switch (getSpec())
					{
						case TALENT_SPEC_DRUID_RESTORATION:
							if (!me.hasAura(SPELL_TREE_OF_LIFE))
							{
								cancelAllShapeshifts();
								verifyAndPushSpellCast(spells, SPELL_TREE_OF_LIFE, TARGET_NONE, 100);
								break;
							}
							verifyAndPushSpellCast(spells, SPELL_TRANQUILITY, TARGET_NONE, 10);
							verifyAndPushSpellCast(spells, SPELL_NATURE_SWIFTNESS, TARGET_NONE, 7);
							if (Creature* creatureCharmer = getCharmer())
							{
								verifyAndPushSpellCast(spells, SPELL_NOURISH, creatureCharmer, 5);
								verifyAndPushSpellCast(spells, SPELL_WILD_GROWTH, creatureCharmer, 5);
								if (!creatureCharmer.getAuraApplicationOfRankedSpell(SPELL_REJUVENATION, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_REJUVENATION, creatureCharmer, 8);
								if (!creatureCharmer.getAuraApplicationOfRankedSpell(SPELL_REGROWTH, me.getGUID()))
									verifyAndPushSpellCast(spells, SPELL_REGROWTH, creatureCharmer, 8);
								uint8 lifebloomStacks = 0;
								if (Aura const* lifebloom = creatureCharmer.getAura(SPELL_LIFEBLOOM, me.getGUID()))
									lifebloomStacks = lifebloom.GetStackAmount();
								if (lifebloomStacks < 3)
									verifyAndPushSpellCast(spells, SPELL_LIFEBLOOM, creatureCharmer, 5);
								if (creatureCharmer.getAuraApplicationOfRankedSpell(SPELL_REJUVENATION) ||
									creatureCharmer.getAuraApplicationOfRankedSpell(SPELL_REGROWTH))
									verifyAndPushSpellCast(spells, SPELL_SWIFTMEND, creatureCharmer, 10);
								if (me.hasAura(SPELL_NATURE_SWIFTNESS))
									verifyAndPushSpellCast(spells, SPELL_HEALING_TOUCH, creatureCharmer, 100);
							}
							break;
						case TALENT_SPEC_DRUID_BALANCE:
						{
							if (!me.hasAura(SPELL_MOONKIN_FORM))
							{
								cancelAllShapeshifts();
								verifyAndPushSpellCast(spells, SPELL_MOONKIN_FORM, TARGET_NONE, 100);
								break;
							}
							uint32 const mainAttackSpell = me.hasAura(AURA_ECLIPSE_LUNAR) ? SPELL_STARFIRE : SPELL_WRATH;
							verifyAndPushSpellCast(spells, SPELL_STARFALL, TARGET_NONE, 20);
							verifyAndPushSpellCast(spells, mainAttackSpell, TARGET_VICTIM, 10);
							if (me.victim && !me.victim.getAuraApplicationOfRankedSpell(SPELL_INSECT_SWARM, me.getGUID()))
								verifyAndPushSpellCast(spells, SPELL_INSECT_SWARM, TARGET_VICTIM, 7);
							if (me.victim && !me.victim.getAuraApplicationOfRankedSpell(SPELL_MOONFIRE, me.getGUID()))
								verifyAndPushSpellCast(spells, SPELL_MOONFIRE, TARGET_VICTIM, 5);
							if (me.victim && me.victim.hasUnitState(UNIT_STATE_CASTING))
								verifyAndPushSpellCast(spells, SPELL_TYPHOON, TARGET_NONE, 15);
							break;
						}
						case TALENT_SPEC_DRUID_CAT:
						case TALENT_SPEC_DRUID_BEAR:
							if (!me.hasAura(SPELL_CAT_FORM))
							{
								cancelAllShapeshifts();
								verifyAndPushSpellCast(spells, SPELL_CAT_FORM, TARGET_NONE, 100);
								break;
							}
							verifyAndPushSpellCast(spells, SPELL_BERSERK, TARGET_NONE, 20);
							verifyAndPushSpellCast(spells, SPELL_SURVIVAL_INSTINCTS, TARGET_NONE, 15);
							verifyAndPushSpellCast(spells, SPELL_TIGER_FURY, TARGET_NONE, 15);
							verifyAndPushSpellCast(spells, SPELL_DASH, TARGET_NONE, 5);
							if (Unit* victim = me.victim)
							{
								uint8 const cp = me.getPower(POWER_COMBO_POINTS);
								if (victim.hasUnitState(UNIT_STATE_CASTING) && cp >= 1)
									verifyAndPushSpellCast(spells, SPELL_MAIM, TARGET_VICTIM, 25);
								if (!me.isWithinMeleeRange(victim))
									verifyAndPushSpellCast(spells, SPELL_FERAL_CHARGE_CAT, TARGET_VICTIM, 25);
								if (cp >= 4)
									verifyAndPushSpellCast(spells, SPELL_RIP, TARGET_VICTIM, 50);
								if (cp <= 4)
								{
									verifyAndPushSpellCast(spells, SPELL_MANGLE_CAT, TARGET_VICTIM, 10);
									verifyAndPushSpellCast(spells, SPELL_CLAW, TARGET_VICTIM, 5);
									if (!victim.getAuraApplicationOfRankedSpell(SPELL_RAKE, me.getGUID()))
										verifyAndPushSpellCast(spells, SPELL_RAKE, TARGET_VICTIM, 8);
									if (!me.hasAura(SPELL_SAVAGE_ROAR))
										verifyAndPushSpellCast(spells, SPELL_SAVAGE_ROAR, TARGET_NONE, 15);
								}
							}
							break;
					}
					break;
				case CLASS_DEMON_HUNTER:
					switch (getSpec())
					{
						case TALENT_SPEC_DEMON_HUNTER_HAVOC:
						case TALENT_SPEC_DEMON_HUNTER_VENGEANCE:
							break;
					}
					break;
			}
			*/
        return selectSpellCast(spells);
    }
}
