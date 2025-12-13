package com.github.azeroth.game.entity.unit;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.Flag128;
import com.github.azeroth.dbc.defines.CriteriaType;
import com.github.azeroth.dbc.domain.DungeonEncounter;
import com.github.azeroth.dbc.domain.LiquidType;
import com.github.azeroth.defines.*;
import com.github.azeroth.defines.Power;
import com.github.azeroth.game.ai.*;
import com.github.azeroth.game.chat.BroadcastTextBuilder;
import com.github.azeroth.game.chat.CustomChatTextBuilder;
import com.github.azeroth.game.combat.CombatManager;
import com.github.azeroth.game.combat.CombatReference;
import com.github.azeroth.game.combat.ThreatManager;
import com.github.azeroth.game.domain.creature.CreatureModel;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.object.ObjectDefine;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.domain.object.enums.TypeMask;
import com.github.azeroth.game.domain.unit.*;
import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.creature.TempSummon;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.object.update.ObjectUpdateFlag;
import com.github.azeroth.game.entity.pet.Pet;
import com.github.azeroth.game.entity.player.KillRewarder;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.totem.Totem;
import com.github.azeroth.game.entity.vehicle.TransportObject;
import com.github.azeroth.game.entity.vehicle.Vehicle;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.loot.Loot;
import com.github.azeroth.game.loot.LootManager;
import com.github.azeroth.game.loot.LootStorage;
import com.github.azeroth.game.map.*;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.movement.AbstractFollower;
import com.github.azeroth.game.movement.MotionMaster;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.model.SpellEffectExtraData;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.enums.MovementSlot;
import com.github.azeroth.game.movement.generator.FollowMovementGenerator;
import com.github.azeroth.game.movement.model.MovementForce;
import com.github.azeroth.game.movement.model.MovementForces;
import com.github.azeroth.game.movement.spline.MoveSpline;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import com.github.azeroth.game.networking.packet.combat.BreakTarget;
import com.github.azeroth.game.networking.packet.misc.SetMeleeAnimKit;
import com.github.azeroth.game.networking.packet.misc.SetPlayHoverAnim;
import com.github.azeroth.game.networking.packet.movement.MoveSetActiveMover;
import com.github.azeroth.game.networking.packet.movement.MoveSetSpeed;
import com.github.azeroth.game.networking.packet.movement.MoveSplineSetSpeed;
import com.github.azeroth.game.networking.packet.movement.MoveUpdateSpeed;

import com.github.azeroth.game.networking.packet.party.PartyKillLog;
import com.github.azeroth.game.spell.*;

import com.github.azeroth.game.spell.auras.enums.AuraTriggerOnPowerChangeDirection;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.spell.enums.*;
import com.github.azeroth.game.spell.events.DelayedCastEvent;
import com.github.azeroth.game.world.WorldContext;
import com.github.azeroth.time.GameTime;
import com.github.azeroth.utils.MathUtil;
import com.github.azeroth.utils.RandomUtil;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.time.Duration;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

import static com.github.azeroth.defines.SharedDefine.MAX_POWERS_PER_CLASS;
import static com.github.azeroth.game.domain.unit.UnitDefine.*;
import static com.github.azeroth.game.entity.object.update.UpdateFields.*;


@Getter
@Setter
public abstract class Unit extends WorldObject {

    private static final Duration DESPAWN_TIME = Duration.ofSeconds(2);
    public static Duration MAX_DAMAGE_HISTORY_DURATION = Duration.ofSeconds(20);
    private final Object healthLock = new Object();
    private final ArrayList<AbstractFollower> followingMe = new ArrayList<>();
    private final MotionMaster motionMaster;

    private final EnumMap<ReactiveType, Integer> reactiveTimer = new EnumMap<>(ReactiveType.class);
    private final EnumMap<WeaponAttackType, Integer> baseAttackSpeed = new EnumMap<>(WeaponAttackType.class);
    private final EnumMap<WeaponAttackType, Float> modAttackSpeedPct = new EnumMap<>(WeaponAttackType.class);
    private final EnumMap<WeaponAttackType, Integer> attackTimer = new EnumMap<>(WeaponAttackType.class);

    // threat+combat management
    private final CombatManager combatManager;
    private final ThreatManager threatManager;
    private final HashMap<ObjectGuid, Integer> extraAttacksTargets = new HashMap<ObjectGuid, Integer>();
    private final ArrayList<Player> sharedVision = new ArrayList<>();
    private final EnumMap<SpellImmunity, List<Integer>> spellImmune = new EnumMap<>(SpellImmunity.class);
    //Auras
    private final EnumMap<AuraType, List<AuraEffect>> modAuras = new EnumMap<>(AuraType.class);
    private final ArrayList<Aura> removedAuras = new ArrayList<>();
    private final ArrayList<AuraApplication> interruptableAuras = new ArrayList<>(); // auras which have interrupt mask applied on unit
    private final HashMap<AuraStateType, List<AuraApplication>> auraStateAuras = new HashMap<>(); // Used for improve performance of aura state checks on aura apply/remove
    private final TreeSet<AuraApplication> visibleAuras = new TreeSet<>();
    private final TreeSet<AuraApplication> visibleAurasToUpdate = new TreeSet<>();
    private final HashMap<Integer,List<AuraApplication>> appliedAuras = new HashMap<>();
    private final HashMap<Integer,List<Aura>> ownedAuras = new HashMap<>();
    private final ArrayList<Aura> scAuras = new ArrayList<>();
    private final EnumMap<DiminishingGroup, DiminishingReturn> diminishing = new EnumMap<>(DiminishingGroup.class);
    private final ArrayList<AreaTrigger> areaTrigger = new ArrayList<>();
    protected EnumMap<Stats, Float> createStats = new EnumMap<>(Stats.class);
    protected EnumMap<Stats, Float> floatStatPosBuff = new EnumMap<>(Stats.class);
    protected EnumMap<Stats, Float> floatStatNegBuff = new EnumMap<>(Stats.class);
    public boolean canDualWield;

    private MovementForces movementForces;
    private PositionUpdateInfo positionUpdateInfo = new PositionUpdateInfo();
    private boolean isCombatDisallowed;
    private int lastExtraAttackSpell;
    private ObjectGuid lastDamagedTargetGuid = ObjectGuid.EMPTY;
    private Unit charmer; // Unit that is charming ME
    private Unit charmed; // Unit that is being charmed BY ME
    private CharmInfo charmInfo;
    private int oldFactionId; // faction before charm
    private boolean isWalkingBeforeCharm; // Are we walking before we were charmed?
    private SpellAuraInterruptFlag interruptMask;
    private SpellAuraInterruptFlag2 interruptMask2;
    private SpellHistory spellHistory;
    private int removedAurasCount;
    private EnumFlag<UnitState> state;
    private boolean canModifyStats;
    private int transformSpell;
    private boolean cleanupDone; // lock made to not add stuff after cleanup before delete
    private boolean duringRemoveFromWorld; // lock made to not add stuff after begining removing from world
    private boolean instantCast;
    private boolean playHoverAnim;
    private short aiAnimKitId;
    private short movementAnimKitId;
    private short meleeAnimKitId;
    //AI
    private Stack<UnitAI> unitAis = new Stack<>();
    private UnitAI ai;
    //Movement
    private float[] speedRate = new float[UnitMoveType.values().length];
    private final MoveSpline moveSpline;
    private int movementCounter;
    private Unit unitMovedByMe;
    private Player playerMovingMe;
    //Combat
    private ArrayList<Unit> attackerList = new ArrayList<>();
    private float[][] weaponDamage = new float[WeaponAttackType.values().length][WeaponDamageRange.values().length];

    private Unit attacking;
    private float modMeleeHitChance;
    private float modRangedHitChance;
    private float modSpellHitChance;
    private float baseSpellCritChance;
    private int regenTimer;
    //Charm
    private ArrayList<Unit> controlled = new ArrayList<>();
    private boolean controlledByPlayer;
    private ObjectGuid lastCharmerGuid = ObjectGuid.EMPTY;
    //Spells
    private EnumMap<CurrentSpellType, Spell> currentSpells = new EnumMap<>(CurrentSpellType.class);
    private int procDeep;
    private float[][] auraFlatModifiersGroup = new float[UnitMod.values().length][UnitModifierFlatType.values().length];
    private float[][] auraPctModifiersGroup = new float[UnitMod.values().length][UnitModifierPctType.values().length];
    //General
    private ArrayList<GameObject> gameObjects = new ArrayList<>();
    private ArrayList<DynamicObject> dynamicObjects = new ArrayList<>();
    private ObjectGuid[] summonSlot = new ObjectGuid[7];
    private ObjectGuid[] objectSlot = new ObjectGuid[4];
    private LiquidType lastLiquid;
    private Vehicle vehicle;
    private Vehicle vehicleKit;
    private int lastSanctuaryTime;
    private final EnumFlag<UnitTypeMask> unitTypeMask = EnumFlag.of(UnitTypeMask.class);
    private DeathState deathState;


    // Power Regeneration
    private Instant regenInterruptTimestamp;
    private int powerRegenUpdateTimer;
    private int healthRegenerationTimer;
    protected float[] powerFraction = new float[MAX_POWERS_PER_CLASS];
    private Power[] usedPowerTypes = new Power[MAX_POWERS_PER_CLASS];



    public Unit(WorldContext worldContext, ObjectGuid guid, TypeId objectTypeId,
                EnumFlag<TypeMask> objectType, EnumFlag<ObjectUpdateFlag> updateFlag,
                short valuesCount, short dynamicValuesCount) {
        super(worldContext, guid, objectTypeId, objectType, updateFlag, valuesCount, dynamicValuesCount);
        moveSpline = new MoveSpline(this);
        motionMaster = new MotionMaster(this);
        combatManager = new CombatManager(this);
        threatManager = new ThreatManager(this);
        spellHistory = new SpellHistory(this);

        objectType.addFlag(TypeMask.UNIT);
        updateFlag.addFlag(ObjectUpdateFlag.LIVING);
        setDeathState(DeathState.ALIVE);

        for (var immunity : SpellImmunity.values()) {
            spellImmune.put(immunity, new ArrayList<>(5));
        }

        for (var unitMod : UnitMod.values()) {
            auraFlatModifiersGroup[unitMod.ordinal()][UnitModifierFlatType.BASE_VALUE.ordinal()] = 0.0f;
            auraFlatModifiersGroup[unitMod.ordinal()][UnitModifierFlatType.BASE_PCT_EXCLUDE_CREATE.ordinal()] = 100.0f;
            auraFlatModifiersGroup[unitMod.ordinal()][UnitModifierFlatType.TOTAL_VALUE.ordinal()] = 0.0f;
            auraPctModifiersGroup[unitMod.ordinal()][UnitModifierPctType.BASE_PCT.ordinal()] = 1.0f;
            auraPctModifiersGroup[unitMod.ordinal()][UnitModifierPctType.TOTAL_PCT.ordinal()] = 1.0f;
        }

        auraPctModifiersGroup[UnitMod.DAMAGE_OFFHAND.ordinal()][UnitModifierPctType.TOTAL_PCT.ordinal()] = 0.5f;

        for (var attackType : WeaponAttackType.values()) {
            weaponDamage[attackType.ordinal()][WeaponDamageRange.MIN_DAMAGE.ordinal()] = BASE_MIN_DAMAGE;
            weaponDamage[attackType.ordinal()][WeaponDamageRange.MAX_DAMAGE.ordinal()] = BASE_MAX_DAMAGE;
        }


        attacking = null;
        modMeleeHitChance = 0.0f;
        modRangedHitChance = 0.0f;
        modSpellHitChance = 0.0f;
        baseSpellCritChance = 5.0f;

        Arrays.fill(speedRate, 1.0f);


        // remove aurastates allowing special moves
        getServerSideVisibility().setValue(ServerSideVisibilityType.GHOST, GhostVisibilityType.ALIVE.ordinal());


        regenInterruptTimestamp = GameTime.now();
        Arrays.fill(usedPowerTypes, Power.MAX_POWERS);

    }


    public static void kill(Unit attacker, Unit victim, boolean durabilityLoss) {
        kill(attacker, victim, durabilityLoss, false);
    }

    public static void kill(Unit attacker, Unit victim) {
        kill(attacker, victim, true, false);
    }

    public static void kill(Unit attacker, Unit victim, boolean durabilityLoss, boolean skipSettingDeathState) {
        // Prevent killing unit twice (and giving reward from kill twice)
        if (victim.getHealth() == 0) {
            return;
        }

        if (attacker != null && !attacker.isInMap(victim)) {
            attacker = null;
        }

        // find player: owner of controlled `this` or `this` itself maybe
        Player player = null;

        if (attacker != null) {
            player = attacker.getCharmerOrOwnerPlayerOrPlayerItself();
        }

        var creature = victim.toCreature();

        var isRewardAllowed = attacker != victim;

        if (creature != null) {
            isRewardAllowed = isRewardAllowed && !creature.getTapList().isEmpty();
        }

        ArrayList<Player> tappers = new ArrayList<>();

        if (isRewardAllowed && creature != null) {
            for (var tapperGuid : creature.getTapList()) {
                var tapper = creature.getWorldContext().getPlayer(creature, tapperGuid);

                if (tapper != null) {
                    tappers.add(tapper);
                }
            }

            if (!creature.getCanHaveLoot()) {
                isRewardAllowed = false;
            }
        }

        // Exploit fix
        if (creature != null && creature.isPet() && creature.getOwnerGUID().isPlayer()) {
            isRewardAllowed = false;
        }

        // Reward player, his pets, and group/raid members
        // call kill spell proc event (before real die and combat stop to triggering auras removed at death/combat stop)
        if (isRewardAllowed) {
            HashSet<PlayerGroup> groups = new HashSet<>();

            for (var tapper : tappers) {
                var tapperGroup = tapper.getGroup();

                if (tapperGroup != null && groups.add(tapperGroup)) {
                    PartyKillLog partyKillLog = new PartyKillLog();
                    partyKillLog.player = player != null && tapperGroup.isMember(player.getGUID()) ? player.getGUID() : tapper.getGUID();
                    partyKillLog.victim = victim.getGUID();
                    partyKillLog.write();

                    tapperGroup.broadcastPacket(partyKillLog, tapperGroup.getMemberGroup(tapper.getGUID()) != 0);

                    tapperGroup.updateLooterGuid(creature, true);

                } else {
                    PartyKillLog partyKillLog = new PartyKillLog();
                    partyKillLog.player = tapper.getGUID();
                    partyKillLog.victim = victim.getGUID();
                    tapper.sendPacket(partyKillLog);
                }
            }

            // Generate loot before updating looter
            if (creature != null) {
                DungeonEncounter dungeonEncounter = null;
                var instance = creature.getInstanceScript();

                if (instance != null) {
                    dungeonEncounter = instance.getBossDungeonEncounter(creature);
                }

                if (creature.getMap().isDungeon()) {
                    if (dungeonEncounter != null) {
                        creature.setPersonalLoot(LootManager.generateDungeonEncounterPersonalLoot(dungeonEncounter.getId(), creature.getLootId(), LootStorage.CREATURE, LootType.CORPSE, creature, creature.getCreatureTemplate().minGold, creature.getCreatureTemplate().maxGold, creature.getLootMode(), creature.getMap().getDifficultyLootItemContext(), tappers));
                    } else if (!tappers.isEmpty()) {
                        var group = !groups.isEmpty() ? groups.iterator().next() : null;
                        var looter = group != null ? creature.getWorldContext().getPlayer(creature, group.getLooterGuid()) : tappers.getFirst();

                        Loot loot = new Loot(creature.getMap(), creature.getGUID(), LootType.CORPSE, dungeonEncounter != null ? group : null);

                        var lootid = creature.getLootId();

                        if (lootid != 0) {
                            loot.fillLoot(lootid, LootStorage.CREATURE, looter, false, false, creature.getLootMode(), creature.getMap().getDifficultyLootItemContext());
                        }

                        if (creature.getLootMode() != null) {
                            loot.generateMoneyLoot(creature.getCreatureTemplate().minGold, creature.getCreatureTemplate().maxGold);
                        }

                        if (group != null) {
                            loot.notifyLootList(creature.getMap());
                        }

                        if (loot != null) {
                            creature.getPersonalLoot().put(looter.getGUID(), loot); // trash mob loot is personal, generated with round robin rules
                        }

                        // Update round robin looter only if the creature had loot
                        if (!loot.isLooted()) {
                            for (var tapperGroup : groups) {
                                tapperGroup.updateLooterGuid(creature);
                            }
                        }
                    }
                } else {
                    for (var tapper : tappers) {
                        Loot loot = new Loot(creature.getMap(), creature.getGUID(), LootType.CORPSE, null);

                        if (dungeonEncounter != null) {
                            loot.setDungeonEncounterId(dungeonEncounter.getId());
                        }

                        var lootid = creature.getLootId();

                        if (lootid != 0) {
                            loot.fillLoot(lootid, LootStorage.CREATURE, tapper, true, false, creature.getLootMode(), creature.getMap().getDifficultyLootItemContext());
                        }

                        if (creature.getLootMode() != null) {
                            loot.generateMoneyLoot(creature.getCreatureTemplate().minGold, creature.getCreatureTemplate().maxGold);
                        }

                        creature.getPersonalLoot().put(tapper.getGUID(), loot);
                    }
                }
            }

            (new KillRewarder(tappers.toArray(new Player[0]), victim, false)).reward();
        }

        // Do KILL and KILLED procs. KILL proc is called only for the unit who landed the killing blow (and its owner - for pets and totems) regardless of who tapped the victim
        if (attacker != null && (attacker.isPet() || attacker.isTotem())) {
            // proc only once for victim
            var owner = attacker.getOwnerUnit();

            if (owner != null) {
                procSkillsAndAuras(owner, victim, EnumFlag.of(ProcFlag.KILL), EnumFlag.of(ProcFlag.NONE), ProcFlagsSpellType.MASK_ALL, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, null, null, null);
            }
        }

        if (!victim.isCritter()) {
            procSkillsAndAuras(attacker, victim, EnumFlag.of(ProcFlag.KILL), EnumFlag.of(ProcFlag.HEARTBEAT), ProcFlagsSpellType.MASK_ALL, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, null, null, null);

            for (var tapper : tappers) {
                if (tapper.isAtGroupRewardDistance(victim)) {
                    procSkillsAndAuras(tapper, victim, EnumFlag.of(ProcFlag.NONE), EnumFlag.of(ProcFlag.HEARTBEAT), ProcFlagsSpellType.MASK_ALL, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, null, null, null);
                }
            }
        }

        // Proc auras on death - must be before aura/combat remove
        procSkillsAndAuras(victim, victim, EnumFlag.of(ProcFlag.NONE), EnumFlag.of(ProcFlag.DEATH), ProcFlagsSpellType.MASK_ALL, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, null, null, null);

        // update get killing blow achievements, must be done before setDeathState to be able to require auras on target
        // and before Spirit of Redemption as it also removes auras
        if (attacker != null) {
            var killerPlayer = attacker.getCharmerOrOwnerPlayerOrPlayerItself();

            if (killerPlayer != null) {
                killerPlayer.updateCriteria(CriteriaType.DeliveredKillingBlow, 1, 0, 0, victim);
            }
        }

        if (!skipSettingDeathState) {
            Log.outDebug(LogFilter.unit, "SET JUST_DIED");
            victim.setDeathState(deathState.JustDied);
        }

        // Inform pets (if any) when player kills target)
        // MUST come after victim.setDeathState(JUST_DIED); or pet next target
        // selection will get stuck on same target and break pet react state
        for (var tapper : tappers) {
            var pet = tapper.getCurrentPet();

            if (pet != null && pet.isAlive() && pet.isControlled()) {
                if (pet.isAIEnabled()) {
                    pet.getAi().killedUnit(victim);
                } else {
                    Log.outError(LogFilter.unit, String.format("Pet doesn't have any AI in unit.kill() %1$s", pet.getDebugInfo()));
                }
            }
        }

        // 10% durability loss on death
        var plrVictim = victim.toPlayer();

        if (plrVictim != null) {
            // remember victim PvP death for corpse type and corpse reclaim delay
            // at original death (not at SpiritOfRedemtionTalent timeout)
            plrVictim.setPvPDeath(player != null);

            // only if not player and not controlled by player pet. And not at BG
            if ((durabilityLoss && player == null && !victim.toPlayer().getInBattleground()) || (player != null && WorldConfig.getBoolValue(WorldCfg.DurabilityLossInPvp))) {
                double baseLoss = WorldConfig.getFloatValue(WorldCfg.RateDurabilityLossOnDeath);
                var loss = (int) (baseLoss - (baseLoss * plrVictim.getTotalAuraMultiplier(AuraType.ModDurabilityLoss)));
                Log.outDebug(LogFilter.unit, "We are dead, losing {0} percent durability", loss);
                // Durability loss is calculated more accurately again for each item in player.DurabilityLoss
                plrVictim.durabilityLossAll(baseLoss, false);
                // durability lost message
                plrVictim.sendDurabilityLoss(plrVictim, loss);
            }

            // Call KilledUnit for creatures
            if (attacker != null && attacker.isCreature() && attacker.isAIEnabled()) {
                attacker.toCreature().getAi().killedUnit(victim);
            }

            // last damage from non duel opponent or opponent controlled creature
            if (plrVictim.getDuel() != null) {
                plrVictim.getDuel().getOpponent().combatStopWithPets(true);
                plrVictim.combatStopWithPets(true);
                plrVictim.duelComplete(DuelCompleteType.Interrupted);
            }
        } else // creature died
        {
            Log.outDebug(LogFilter.unit, "DealDamageNotPlayer");

            if (!creature.isPet()) {
                // must be after setDeathState which resets dynamic flags
                if (!creature.isFullyLooted()) {
                    creature.setDynamicFlag(UnitDynFlag.LOOTABLE);
                } else {
                    creature.allLootRemovedFromCorpse();
                }

                if (creature.getCanHaveLoot() && LootStorage.SKINNING.haveLootFor(creature.getCreatureTemplate().skinLootId)) {
                    creature.setDynamicFlag(UnitDynFlag.CAN_SKIN);
                    creature.setUnitFlag(UnitFlag.SKINNABLE);
                }
            }

            // Call KilledUnit for creatures, this needs to be called after the lootable flag is set
            if (attacker != null && attacker.isCreature() && attacker.isAIEnabled()) {
                attacker.toCreature().getAi().killedUnit(victim);
            }

            // Call creature just died function
            var ai = creature.getAi();

            if (ai != null) {
                ai.justDied(attacker);
            }

            var summon = creature.toTempSummon();

            if (summon != null) {
                var summoner = summon.getSummoner();

                if (summoner != null) {
                    if (summoner.isCreature()) {
                        if (summoner.toCreature().getAi() != null) {
                            summoner.toCreature().getAi().summonedCreatureDies(creature, attacker);
                        }
                    } else if (summoner.isGameObject()) {
                        if (summoner.toGameObject().getAI() != null) {
                            summoner.toGameObject().getAI().summonedCreatureDies(creature, attacker);
                        }
                    }
                }
            }
        }

        // outdoor pvp things, do these after setting the death state, else the player activity notify won't work... doh...
        // handle player kill only if not suicide (spirit of redemption for example)
        if (player != null && attacker != victim) {
            var pvp = player.getOutdoorPvP();

            if (pvp != null) {
                pvp.handleKill(player, victim);
            }

            var bf = global.getBattleFieldMgr().getBattlefieldToZoneId(player.getMap(), player.getZoneId());

            if (bf != null) {
                bf.handleKill(player, victim);
            }
        }

        // Battlegroundthings (do this at the end, so the death state flag will be properly set to handle in the bg.handlekill)
        if (player != null && player.getInBattleground()) {
            var bg = player.getBattleground();

            if (bg) {
                var playerVictim = victim.toPlayer();

                if (playerVictim) {
                    bg.handleKillPlayer(playerVictim, player);
                } else {
                    bg.handleKillUnit(victim.toCreature(), player);
                }
            }
        }

        // achievement stuff
        if (attacker != null && victim.isPlayer()) {
            if (attacker.isCreature()) {
                victim.toPlayer().updateCriteria(CriteriaType.KilledByCreature, attacker.getEntry());
            } else if (attacker.isPlayer() && victim != attacker) {
                victim.toPlayer().updateCriteria(CriteriaType.KilledByPlayer, 1, (long) attacker.toPlayer().getEffectiveTeam().getValue());
            }
        }

        // Hook for OnPVPKill Event
        if (attacker != null) {
            var killerPlr = attacker.toPlayer();

            if (killerPlr != null) {
                var killedPlr = victim.toPlayer();

                if (killedPlr != null) {
                    global.getScriptMgr().<IPlayerOnPVPKill>ForEach(p -> p.OnPVPKill(killerPlr, killedPlr));
                } else {
                    var killedCre = victim.toCreature();

                    if (killedCre != null) {
                        global.getScriptMgr().<IPlayerOnCreatureKill>ForEach(p -> p.OnCreatureKill(killerPlr, killedCre));
                    }
                }
            } else {
                var killerCre = attacker.toCreature();

                if (killerCre != null) {
                    var killed = victim.toPlayer();

                    if (killed != null) {
                        global.getScriptMgr().<IPlayerOnPlayerKilledByCreature>ForEach(p -> p.OnPlayerKilledByCreature(killerCre, killed));
                    }
                }
            }
        }
    }

    public static boolean isDamageReducedByArmor(SpellSchoolMask schoolMask) {
        return isDamageReducedByArmor(schoolMask, null);
    }

    public static boolean isDamageReducedByArmor(SpellSchoolMask schoolMask, SpellInfo spellInfo) {
        // only physical spells damage gets reduced by armor
        if ((schoolMask.getValue() & SpellSchoolMask.NORMAL.getValue()) == 0) {
            return false;
        }

        return spellInfo == null || !spellInfo.hasAttribute(SpellCustomAttributes.IGNORE_ARMOR);
    }

    public static void dealDamageMods(Unit attacker, Unit victim, tangible.RefObject<Double> damage) {
        if (victim == null || !victim.isAlive() || victim.hasUnitState(UnitState.InFlight) || (victim.isTypeId(TypeId.UNIT) && victim.toCreature().isInEvadeMode())) {
            damage.refArgValue = 0;
        }
    }

    public static boolean checkEvade(Unit attacker, Unit victim, tangible.RefObject<Double> damage, tangible.RefObject<Double> absorb) {
        if (victim == null || !victim.isAlive() || victim.hasUnitState(UnitState.InFlight) || (victim.isTypeId(TypeId.UNIT) && victim.toCreature().isEvadingAttacks())) {
            absorb.refArgValue += damage.refArgValue;
            damage.refArgValue = 0;

            return true;
        }

        return false;
    }

    public static void scaleDamage(Unit attacker, Unit victim, tangible.RefObject<Double> damage) {
        if (attacker != null) {
            damage.refArgValue = damage.refArgValue * attacker.getDamageMultiplierForTarget(victim);
        }
    }

    public static void dealDamageMods(Unit attacker, Unit victim, tangible.RefObject<Double> damage, tangible.RefObject<Double> absorb) {
        if (!checkEvade(attacker, victim, damage, absorb)) {
            scaleDamage(attacker, victim, damage);
        }
    }

    public static float dealDamage(Unit attacker, Unit victim, float damage, CleanDamage cleanDamage, DamageEffectType damagetype, SpellSchoolMask damageSchoolMask, SpellInfo spellProto) {
        return dealDamage(attacker, victim, damage, cleanDamage, damagetype, damageSchoolMask, spellProto, true);
    }

    public static float dealDamage(Unit attacker, Unit victim, float damage, CleanDamage cleanDamage, DamageEffectType damagetype, SpellSchoolMask damageSchoolMask) {
        return dealDamage(attacker, victim, damage, cleanDamage, damagetype, damageSchoolMask, null, true);
    }

    public static float dealDamage(Unit attacker, Unit victim, float damage, CleanDamage cleanDamage, DamageEffectType damagetype) {
        return dealDamage(attacker, victim, damage, cleanDamage, damagetype, SpellSchoolMask.NORMAL, null, true);
    }

    public static float dealDamage(Unit attacker, Unit victim, float damage, CleanDamage cleanDamage) {
        return dealDamage(attacker, victim, damage, cleanDamage, DamageEffectType.DIRECT_DAMAGE, SpellSchoolMask.NORMAL, null, true);
    }

    public static float dealDamage(Unit attacker, Unit victim, float damage) {
        return dealDamage(attacker, victim, damage, null, DamageEffectType.DIRECT_DAMAGE, SpellSchoolMask.NORMAL, null, true);
    }

    public static float dealDamage(Unit attacker, Unit victim, float damage, CleanDamage cleanDamage, DamageEffectType damagetype, SpellSchoolMask damageSchoolMask, SpellInfo spellProto, boolean durabilityLoss) {
        var damageDone = damage;
        var damageTaken = damage;

        if (attacker != null) {
            damageTaken = (damage / victim.getHealthMultiplierForTarget(attacker));
        }

        // call script hooks
        {
            float tmpDamage = damageTaken;

            // sparring
            Creature victimCreature = victim.toCreature();
            if (victimCreature != null)
                tmpDamage = victimCreature.calculateDamageForSparring(attacker, tmpDamage);

            UnitAI victimAI = victim.getAi();
            if (victimAI != null)
                victimAI.damageTaken(attacker, tmpDamage, damagetype, spellProto);


            UnitAI attackerAI = attacker != null ? attacker.getAi() : null;
            if (attackerAI != null)
                attackerAI.damageDealt(victim, tmpDamage, damagetype);

            // Hook for OnDamage Event
            sScriptMgr->OnDamage(attacker, victim, tmpDamage);

            // if any script modified damage, we need to also apply the same modification to unscaled damage value
            if (tmpDamage != damageTaken)
            {
                if (attacker)
                    damageDone = tmpDamage * victim->GetHealthMultiplierForTarget(attacker);
                else
                    damageDone = tmpDamage;

                damageTaken = tmpDamage;
            }
        }


        // Signal to pets that their owner was attacked - except when DOT.
        if (attacker != victim && damagetype != DamageEffectType.DOT) {
            for (var controlled : victim.getControlled()) {
                var cControlled = controlled.toCreature();

                if (cControlled != null) {
                    var controlledAI = cControlled.getAi();

                    if (controlledAI != null) {
                        controlledAI.ownerAttackedBy(attacker);
                    }
                }
            }
        }

        var player = victim.toPlayer();

        if (player != null && player.getCommandStatus(PlayerCommandStates.God)) {
            return 0;
        }

        if (damagetype != DamageEffectType.NoDamage) {
            // interrupting auras with SpellAuraInterruptFlag.Damage before checking !damage (absorbed damage breaks that type of auras)
            if (spellProto != null) {
                if (!spellProto.hasAttribute(SpellAttr4.ReactiveDamageProc)) {
                    victim.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.damage, spellProto);
                }
            } else {
                victim.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.damage);
            }

            if (damageTaken == 0 && damagetype != DamageEffectType.DOT && cleanDamage != null && cleanDamage.getAbsorbedDamage() != 0) {
                if (victim != attacker && victim.isPlayer()) {
                    var spell = victim.getCurrentSpell(CurrentSpellType.generic);

                    if (spell != null) {
                        if (spell.getState() == SpellState.Preparing && spell.spellInfo.getInterruptFlags().hasFlag(SpellInterruptFlags.DamageAbsorb)) {
                            victim.interruptNonMeleeSpells(false);
                        }
                    }
                }
            }

            // We're going to call functions which can modify content of the list during iteration over it's elements
            // Let's copy the list so we can prevent iterator invalidation
            var vCopyDamageCopy = victim.getAuraEffectsByType(AuraType.ShareDamagePct);

            // copy damage to casters of this aura
            for (var aura : vCopyDamageCopy) {
                // Check if aura was removed during iteration - we don't need to work on such auras
                if (!aura.getBase().isAppliedOnTarget(victim.getGUID())) {
                    continue;
                }

                // check damage school mask
                if ((aura.getMiscValue() & damageSchoolMask.getValue()) == 0) {
                    continue;
                }

                var shareDamageTarget = aura.getCaster();

                if (shareDamageTarget == null) {
                    continue;
                }

                var spell = aura.getSpellInfo();

                var share = MathUtil.CalculatePct(damageDone, aura.getAmount());

                // @todo check packets if damage is done by victim, or by attacker of victim
                tangible.RefObject<Double> tempRef_share = new tangible.RefObject<Double>(share);
                dealDamageMods(attacker, shareDamageTarget, tempRef_share);
                share = tempRef_share.refArgValue;
                dealDamage(attacker, shareDamageTarget, share, null, DamageEffectType.NoDamage, spell.getSchoolMask(), spell, false);
            }
        }

        // Rage from Damage made (only from direct weapon damage)
        if (attacker != null && cleanDamage != null && (cleanDamage.getAttackType() == WeaponAttackType.BASE_ATTACK || cleanDamage.getAttackType() == WeaponAttackType.OFF_ATTACK) && damagetype == DamageEffectType.Direct && attacker != victim && attacker.getDisplayPowerType() == powerType.Rage) {
            var rage = (int) (attacker.getBaseAttackTime(cleanDamage.getAttackType()) / 1000.0f * 1.75f);

            if (cleanDamage.getAttackType() == WeaponAttackType.OFF_ATTACK) {
                rage /= 2;
            }

            attacker.rewardRage(rage);
        }

        if (damageDone == 0) {
            return 0;
        }

        var health = (int) victim.getHealth();

        // duel ends when player has 1 or less hp
        var duel_hasEnded = false;
        var duel_wasMounted = false;

        if (victim.isPlayer() && victim.toPlayer().getDuel() != null && damageTaken >= (health - 1)) {
            if (!attacker) {
                return 0;
            }

            // prevent kill only if killed in duel and killed by opponent or opponent controlled creature
            if (victim.toPlayer().getDuel().getOpponent() == attacker.getControllingPlayer()) {
                damageTaken = health - 1;
            }

            duel_hasEnded = true;
        } else {
            Creature creature;
            tangible.OutObject<Creature> tempOut_creature = new tangible.OutObject<Creature>();
            if (victim.isCreature(tempOut_creature) && damageTaken >= health && creature.getStaticFlags().hasFlag(CreatureStaticFlags.UNKILLABLE)) {
                creature = tempOut_creature.outArgValue;
                damageTaken = health - 1;
            } else {
                creature = tempOut_creature.outArgValue;
                if (victim.isVehicle() && damageTaken >= (health - 1) && victim.getCharmer() != null && victim.getCharmer().isTypeId(TypeId.PLAYER)) {
                    var victimRider = victim.getCharmer().toPlayer();

                    if (victimRider != null && victimRider.getDuel() != null && victimRider.getDuel().isMounted()) {
                        if (!attacker) {
                            return 0;
                        }

                        // prevent kill only if killed in duel and killed by opponent or opponent controlled creature
                        if (victimRider.getDuel().getOpponent() == attacker.getControllingPlayer()) {
                            damageTaken = health - 1;
                        }

                        duel_wasMounted = true;
                        duel_hasEnded = true;
                    }
                }
            }
        }

        if (attacker != null && attacker != victim) {
            var killer = attacker.toPlayer();

            if (killer != null) {
                // in bg, count dmg if victim is also a player
                if (victim.isPlayer()) {
                    var bg = killer.getBattleground();

                    if (bg != null) {
                        bg.updatePlayerScore(killer, ScoreType.damageDone, (int) damageDone);
                    }
                }

                killer.updateCriteria(CriteriaType.DamageDealt, health > damageDone ? damageDone : health, 0, 0, victim);
                killer.updateCriteria(CriteriaType.HighestDamageDone, damageDone);
            }
        }

        if (victim.isPlayer()) {
            victim.toPlayer().updateCriteria(CriteriaType.HighestDamageTaken, damageTaken);
        }

        if (victim.getObjectTypeId() != TypeId.PLAYER && (!victim.isControlledByPlayer() || victim.isVehicle())) {
            victim.toCreature().setTappedBy(attacker);

            if (attacker == null || attacker.isControlledByPlayer()) {
                victim.toCreature().lowerPlayerDamageReq(health < damageTaken ? health : damageTaken);
            }
        }

        var killed = false;
        var skipSettingDeathState = false;

        if (health <= damageTaken) {
            killed = true;

            if (victim.isPlayer() && victim != attacker) {
                victim.toPlayer().updateCriteria(CriteriaType.TotalDamageTaken, health);
            }

            if (damagetype != DamageEffectType.NoDamage && damagetype != DamageEffectType.Self && victim.hasAuraType(AuraType.SchoolAbsorbOverkill)) {
                var vAbsorbOverkill = victim.getAuraEffectsByType(AuraType.SchoolAbsorbOverkill);
                DamageInfo damageInfo = new DamageInfo(attacker, victim, damageTaken, spellProto, damageSchoolMask, damagetype, cleanDamage != null ? cleanDamage.getAttackType() : WeaponAttackType.BASE_ATTACK);

                for (var absorbAurEff : vAbsorbOverkill) {
                    var baseAura = absorbAurEff.getBase();
                    var aurApp = baseAura.getApplicationOfTarget(victim.getGUID());

                    if (aurApp == null) {
                        continue;
                    }

                    if ((absorbAurEff.getMiscValue() & damageInfo.getSchoolMask().getValue()) == 0) {
                        continue;
                    }

                    // cannot absorb over limit
                    if (damageTaken >= victim.countPctFromMaxHealth(100 + absorbAurEff.getMiscValueB())) {
                        continue;
                    }

                    // get amount which can be still absorbed by the aura
                    var currentAbsorb = absorbAurEff.getAmount();

                    // aura with infinite absorb amount - let the scripts handle absorbtion amount, set here to 0 for safety
                    if (currentAbsorb < 0) {
                        currentAbsorb = 0;
                    }

                    var tempAbsorb = currentAbsorb;

                    // This aura type is used both by Spirit of Redemption (death not really prevented, must grant all credit immediately) and Cheat Death (death prevented)
                    // repurpose PreventDefaultAction for this
                    var deathFullyPrevented = false;

                    tangible.RefObject<Double> tempRef_tempAbsorb = new tangible.RefObject<Double>(tempAbsorb);
                    tangible.RefObject<Boolean> tempRef_deathFullyPrevented = new tangible.RefObject<Boolean>(deathFullyPrevented);
                    absorbAurEff.getBase().callScriptEffectAbsorbHandlers(absorbAurEff, aurApp, damageInfo, tempRef_tempAbsorb, tempRef_deathFullyPrevented);
                    deathFullyPrevented = tempRef_deathFullyPrevented.refArgValue;
                    tempAbsorb = tempRef_tempAbsorb.refArgValue;
                    currentAbsorb = tempAbsorb;

                    // absorb must be smaller than the damage itself
                    tangible.RefObject<Double> tempRef_currentAbsorb = new tangible.RefObject<Double>(currentAbsorb);
                    currentAbsorb = MathUtil.RoundToInterval(tempRef_currentAbsorb, 0, damageInfo.getDamage());
                    currentAbsorb = tempRef_currentAbsorb.refArgValue;
                    damageInfo.absorbDamage(currentAbsorb);

                    if (deathFullyPrevented) {
                        killed = false;
                    }

                    skipSettingDeathState = true;

                    if (currentAbsorb != 0) {
                        SpellAbsorbLog absorbLog = new SpellAbsorbLog();
                        absorbLog.attacker = attacker != null ? attacker.getGUID() : ObjectGuid.Empty;
                        absorbLog.victim = victim.getGUID();
                        absorbLog.caster = baseAura.getCasterGuid();
                        absorbLog.absorbedSpellID = spellProto != null ? spellProto.getId() : 0;
                        absorbLog.absorbSpellID = baseAura.getId();
                        absorbLog.absorbed = (int) currentAbsorb;
                        absorbLog.originalDamage = (int) damageInfo.getOriginalDamage();
                        absorbLog.logData.initialize(victim);
                        victim.sendCombatLogMessage(absorbLog);
                    }
                }

                damageTaken = damageInfo.getDamage();
            }
        }

        if (spellProto != null && spellProto.hasAttribute(SpellAttr3.NoDurabilityLoss)) {
            durabilityLoss = false;
        }

        if (killed) {
            kill(attacker, victim, durabilityLoss, skipSettingDeathState);
        } else {
            if (victim.isTypeId(TypeId.PLAYER)) {
                victim.toPlayer().updateCriteria(CriteriaType.TotalDamageTaken, damageTaken);
            }

            victim.modifyHealth(-(int) damageTaken);

            if (damagetype == DamageEffectType.Direct || damagetype == DamageEffectType.SpellDirect) {
                victim.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.NonPeriodicDamage, spellProto);
            }

            if (!victim.isTypeId(TypeId.PLAYER)) {
                // Part of Evade mechanics. DoT's and Thorns / Retribution Aura do not contribute to this
                if (damagetype != DamageEffectType.DOT && damageTaken > 0 && !victim.getOwnerGUID().isPlayer() && (spellProto == null || !spellProto.hasAura(AuraType.DamageShield))) {
                    victim.toCreature().setLastDamagedTime(GameTime.getGameTime() + SharedConst.MaxAggroResetTime);
                }

                if (attacker != null && (spellProto == null || !spellProto.hasAttribute(SpellAttr4.NoHarmfulThreat))) {
                    victim.getThreatManager().addThreat(attacker, damageTaken, spellProto);
                }
            } else // victim is a player
            {
                // random durability for items (HIT TAKEN)
                if (durabilityLoss && WorldConfig.getFloatValue(WorldCfg.RateDurabilityLossDamage) > RandomUtil.randChance()) {
                    var slot = (byte) RandomUtil.IRand(0, EquipmentSlot.End - 1);
                    victim.toPlayer().durabilityPointLossForEquipSlot(slot);
                }
            }

            if (attacker != null && attacker.isPlayer()) {
                // random durability for items (HIT DONE)
                if (durabilityLoss && RandomUtil.randChance(WorldConfig.getFloatValue(WorldCfg.RateDurabilityLossDamage))) {
                    var slot = (byte) RandomUtil.IRand(0, EquipmentSlot.End - 1);
                    attacker.toPlayer().durabilityPointLossForEquipSlot(slot);
                }
            }

            if (damagetype != DamageEffectType.NoDamage && damagetype != DamageEffectType.DOT) {
                if (victim != attacker && (spellProto == null || !(spellProto.hasAttribute(SpellAttr6.NoPushback) || spellProto.hasAttribute(SpellAttr7.NoPushbackOnDamage) || spellProto.hasAttribute(SpellAttr3.TreatAsPeriodic)))) {
                    var spell = victim.getCurrentSpell(CurrentSpellType.generic);

                    if (spell != null) {
                        if (spell.getState() == SpellState.Preparing) {

//							bool isCastInterrupted()
//								{
//									if (damageTaken == 0)
//										return spell.spellInfo.interruptFlags.hasFlag(SpellInterruptFlags.ZeroDamageCancels);
//
//									if (victim.IsPlayer && spell.spellInfo.interruptFlags.hasFlag(SpellInterruptFlags.DamageCancelsPlayerOnly))
//										return true;
//
//									if (spell.spellInfo.interruptFlags.hasFlag(SpellInterruptFlags.DamageCancels))
//										return true;
//
//									return false;
//								}

                            ;


//							bool isCastDelayed()
//								{
//									if (damageTaken == 0)
//										return false;
//
//									if (victim.IsPlayer && spell.spellInfo.interruptFlags.hasFlag(SpellInterruptFlags.DamagePushbackPlayerOnly))
//										return true;
//
//									if (spell.spellInfo.interruptFlags.hasFlag(SpellInterruptFlags.DamagePushback))
//										return true;
//
//									return false;
//								}

                            if (isCastInterrupted()) {
                                victim.interruptNonMeleeSpells(false);
                            } else if (isCastDelayed()) {
                                spell.delayed();
                            }
                        }
                    }
                }

                if (damageTaken != 0 && victim.isPlayer()) {
                    var spell1 = victim.getCurrentSpell(CurrentSpellType.Channeled);

                    if (spell1 != null) {
                        if (spell1.getState() == SpellState.Casting && spell1.spellInfo.hasChannelInterruptFlag(SpellAuraInterruptFlag.DamageChannelDuration)) {
                            spell1.delayedChannel();
                        }
                    }
                }
            }

            // last damage from duel opponent
            if (duel_hasEnded) {
                var he = duel_wasMounted ? victim.getCharmer().toPlayer() : victim.toPlayer();

                if (duel_wasMounted) // In this case victim==mount
                {
                    victim.setHealth(1);
                } else {
                    he.setHealth(1);
                }

                he.getDuel().getOpponent().combatStopWithPets(true);
                he.combatStopWithPets(true);

                he.castSpell(he, 7267, true); // beg
                he.duelComplete(DuelCompleteType.Won);
            }
        }

        // logging uses damageDone
        if (victim.isPlayer()) {
            player = victim.toPlayer();
            ScriptManager.getInstance().<IPlayerOnTakeDamage>ForEach(player.getUnitClass(), a -> a.OnPlayerTakeDamage(player, damageDone, damageSchoolMask));
        }

        // make player victims stand up automatically
        if (victim.getStandState() != 0 && victim.isPlayer()) {
            victim.setStandState(UnitStandStateType.Stand);
        }

        if (player != null) {
            victim.saveDamageHistory(damageDone);
        }

        return damageDone;
    }

    public static void calcAbsorbResist(DamageInfo damageInfo) {
        calcAbsorbResist(damageInfo, null);
    }

    public static void calcAbsorbResist(DamageInfo damageInfo, Spell spell) {
        if (!damageInfo.getVictim() || !damageInfo.getVictim().isAlive() || damageInfo.getDamage() == 0) {
            return;
        }

        var resistedDamage = calcSpellResistedDamage(damageInfo.getAttacker(), damageInfo.getVictim(), damageInfo.getDamage(), damageInfo.getSchoolMask(), damageInfo.getSpellInfo());

        // Ignore Absorption Auras
        double auraAbsorbMod = 0f;

        var attacker = damageInfo.getAttacker();

        if (attacker != null) {
            auraAbsorbMod = attacker.getMaxPositiveAuraModifierByMiscMask(AuraType.ModTargetAbsorbSchool, (int) damageInfo.getSchoolMask().getValue());
        }

        tangible.RefObject<Double> tempRef_auraAbsorbMod = new tangible.RefObject<Double>(auraAbsorbMod);
        MathUtil.RoundToInterval(tempRef_auraAbsorbMod, 0.0f, 100.0f);
        auraAbsorbMod = tempRef_auraAbsorbMod.refArgValue;

        var absorbIgnoringDamage = MathUtil.CalculatePct(damageInfo.getDamage(), auraAbsorbMod);

        if (spell != null) {
            tangible.RefObject<Double> tempRef_resistedDamage = new tangible.RefObject<Double>(resistedDamage);
            tangible.RefObject<Double> tempRef_absorbIgnoringDamage = new tangible.RefObject<Double>(absorbIgnoringDamage);
            spell.callScriptOnResistAbsorbCalculateHandlers(damageInfo, tempRef_resistedDamage, tempRef_absorbIgnoringDamage);
            absorbIgnoringDamage = tempRef_absorbIgnoringDamage.refArgValue;
            resistedDamage = tempRef_resistedDamage.refArgValue;
        }

        damageInfo.resistDamage(resistedDamage);

        // We're going to call functions which can modify content of the list during iteration over it's elements
        // Let's copy the list so we can prevent iterator invalidation
        var vSchoolAbsorbCopy = damageInfo.getVictim().getAuraEffectsByType(AuraType.SchoolAbsorb);
        collections.sort(vSchoolAbsorbCopy, new AbsorbAuraOrderPred());

        // absorb without mana cost
        for (var i = 0; i < vSchoolAbsorbCopy.size() && (damageInfo.getDamage() > 0); ++i) {
            var absorbAurEff = vSchoolAbsorbCopy.get(i);

            // Check if aura was removed during iteration - we don't need to work on such auras
            var aurApp = absorbAurEff.getBase().getApplicationOfTarget(damageInfo.getVictim().getGUID());

            if (aurApp == null) {
                continue;
            }

            if ((absorbAurEff.getMiscValue() & damageInfo.getSchoolMask().getValue()) == 0) {
                continue;
            }

            // get amount which can be still absorbed by the aura
            var currentAbsorb = absorbAurEff.getAmount();

            // aura with infinite absorb amount - let the scripts handle absorbtion amount, set here to 0 for safety
            if (currentAbsorb < 0) {
                currentAbsorb = 0;
            }

            if (!absorbAurEff.getSpellInfo().hasAttribute(SpellAttr6.AbsorbCannotBeIgnore)) {
                damageInfo.modifyDamage(-absorbIgnoringDamage);
            }

            var tempAbsorb = currentAbsorb;

            var defaultPrevented = false;

            tangible.RefObject<Double> tempRef_tempAbsorb = new tangible.RefObject<Double>(tempAbsorb);
            tangible.RefObject<Boolean> tempRef_defaultPrevented = new tangible.RefObject<Boolean>(defaultPrevented);
            absorbAurEff.getBase().callScriptEffectAbsorbHandlers(absorbAurEff, aurApp, damageInfo, tempRef_tempAbsorb, tempRef_defaultPrevented);
            defaultPrevented = tempRef_defaultPrevented.refArgValue;
            tempAbsorb = tempRef_tempAbsorb.refArgValue;
            currentAbsorb = (int) tempAbsorb;

            if (!defaultPrevented) {
                // absorb must be smaller than the damage itself
                tangible.RefObject<Double> tempRef_currentAbsorb = new tangible.RefObject<Double>(currentAbsorb);
                currentAbsorb = MathUtil.RoundToInterval(tempRef_currentAbsorb, 0, damageInfo.getDamage());
                currentAbsorb = tempRef_currentAbsorb.refArgValue;

                damageInfo.absorbDamage(currentAbsorb);

                tempAbsorb = (int) currentAbsorb;
                tangible.RefObject<Double> tempRef_tempAbsorb2 = new tangible.RefObject<Double>(tempAbsorb);
                absorbAurEff.getBase().callScriptEffectAfterAbsorbHandlers(absorbAurEff, aurApp, damageInfo, tempRef_tempAbsorb2);
                tempAbsorb = tempRef_tempAbsorb2.refArgValue;

                // Check if our aura is using amount to count heal
                if (absorbAurEff.getAmount() >= 0) {
                    // Reduce shield amount
                    absorbAurEff.changeAmount(absorbAurEff.getAmount() - currentAbsorb);

                    // Aura cannot absorb anything more - remove it
                    if (absorbAurEff.getAmount() <= 0 && !absorbAurEff.getBase().getSpellInfo().hasAttribute(SpellAttr0.Passive)) {
                        absorbAurEff.getBase().remove(AuraRemoveMode.EnemySpell);
                    }
                }
            }

            if (!absorbAurEff.getSpellInfo().hasAttribute(SpellAttr6.AbsorbCannotBeIgnore)) {
                damageInfo.modifyDamage(absorbIgnoringDamage);
            }

            if (currentAbsorb != 0) {
                SpellAbsorbLog absorbLog = new SpellAbsorbLog();
                absorbLog.attacker = damageInfo.getAttacker() != null ? damageInfo.getAttacker().getGUID() : ObjectGuid.Empty;
                absorbLog.victim = damageInfo.getVictim().getGUID();
                absorbLog.caster = absorbAurEff.getBase().getCasterGuid();
                absorbLog.absorbedSpellID = damageInfo.getSpellInfo() != null ? damageInfo.getSpellInfo().getId() : 0;
                absorbLog.absorbSpellID = absorbAurEff.getId();
                absorbLog.absorbed = (int) currentAbsorb;
                absorbLog.originalDamage = (int) damageInfo.getOriginalDamage();
                absorbLog.logData.initialize(damageInfo.getVictim());
                damageInfo.getVictim().sendCombatLogMessage(absorbLog);
            }
        }

        // absorb by mana cost
        var vManaShieldCopy = damageInfo.getVictim().getAuraEffectsByType(AuraType.manaShield);

        for (var absorbAurEff : vManaShieldCopy) {
            if (damageInfo.getDamage() == 0) {
                break;
            }

            // Check if aura was removed during iteration - we don't need to work on such auras
            var aurApp = absorbAurEff.getBase().getApplicationOfTarget(damageInfo.getVictim().getGUID());

            if (aurApp == null) {
                continue;
            }

            // check damage school mask
            if (!(boolean) (absorbAurEff.getMiscValue() & damageInfo.getSchoolMask().getValue())) {
                continue;
            }

            // get amount which can be still absorbed by the aura
            var currentAbsorb = absorbAurEff.getAmount();

            // aura with infinite absorb amount - let the scripts handle absorbtion amount, set here to 0 for safety
            if (currentAbsorb < 0) {
                currentAbsorb = 0;
            }

            if (!absorbAurEff.getSpellInfo().hasAttribute(SpellAttr6.AbsorbCannotBeIgnore)) {
                damageInfo.modifyDamage(-absorbIgnoringDamage);
            }

            var tempAbsorb = currentAbsorb;

            var defaultPrevented = false;

            tangible.RefObject<Double> tempRef_tempAbsorb3 = new tangible.RefObject<Double>(tempAbsorb);
            tangible.RefObject<Boolean> tempRef_defaultPrevented2 = new tangible.RefObject<Boolean>(defaultPrevented);
            absorbAurEff.getBase().callScriptEffectManaShieldHandlers(absorbAurEff, aurApp, damageInfo, tempRef_tempAbsorb3, tempRef_defaultPrevented2);
            defaultPrevented = tempRef_defaultPrevented2.refArgValue;
            tempAbsorb = tempRef_tempAbsorb3.refArgValue;
            currentAbsorb = (int) tempAbsorb;

            if (!defaultPrevented) {
                // absorb must be smaller than the damage itself
                tangible.RefObject<Double> tempRef_currentAbsorb2 = new tangible.RefObject<Double>(currentAbsorb);
                currentAbsorb = MathUtil.RoundToInterval(tempRef_currentAbsorb2, 0, damageInfo.getDamage());
                currentAbsorb = tempRef_currentAbsorb2.refArgValue;

                var manaReduction = currentAbsorb;

                // lower absorb amount by talents
                var manaMultiplier = absorbAurEff.getSpellEffectInfo().calcValueMultiplier(absorbAurEff.getCaster());

                if (manaMultiplier != 0) {
                    manaReduction = (int) (manaReduction * manaMultiplier);
                }

                var manaTaken = -damageInfo.getVictim().modifyPower(powerType.mana, -manaReduction);

                // take case when mana has ended up into account
                currentAbsorb = currentAbsorb != 0 ? (currentAbsorb * (manaTaken / manaReduction)) : 0;

                damageInfo.absorbDamage((int) currentAbsorb);

                tempAbsorb = (int) currentAbsorb;
                tangible.RefObject<Double> tempRef_tempAbsorb4 = new tangible.RefObject<Double>(tempAbsorb);
                absorbAurEff.getBase().callScriptEffectAfterManaShieldHandlers(absorbAurEff, aurApp, damageInfo, tempRef_tempAbsorb4);
                tempAbsorb = tempRef_tempAbsorb4.refArgValue;

                // Check if our aura is using amount to count damage
                if (absorbAurEff.getAmount() >= 0) {
                    absorbAurEff.changeAmount(absorbAurEff.getAmount() - currentAbsorb);

                    if ((absorbAurEff.getAmount() <= 0)) {
                        absorbAurEff.getBase().remove(AuraRemoveMode.EnemySpell);
                    }
                }
            }

            if (!absorbAurEff.getSpellInfo().hasAttribute(SpellAttr6.AbsorbCannotBeIgnore)) {
                damageInfo.modifyDamage(absorbIgnoringDamage);
            }

            if (currentAbsorb != 0) {
                SpellAbsorbLog absorbLog = new SpellAbsorbLog();
                absorbLog.attacker = damageInfo.getAttacker() != null ? damageInfo.getAttacker().getGUID() : ObjectGuid.Empty;
                absorbLog.victim = damageInfo.getVictim().getGUID();
                absorbLog.caster = absorbAurEff.getBase().getCasterGuid();
                absorbLog.absorbedSpellID = damageInfo.getSpellInfo() != null ? damageInfo.getSpellInfo().getId() : 0;
                absorbLog.absorbSpellID = absorbAurEff.getId();
                absorbLog.absorbed = (int) currentAbsorb;
                absorbLog.originalDamage = (int) damageInfo.getOriginalDamage();
                absorbLog.logData.initialize(damageInfo.getVictim());
                damageInfo.getVictim().sendCombatLogMessage(absorbLog);
            }
        }

        // split damage auras - only when not damaging self
        if (damageInfo.getVictim() != damageInfo.getAttacker()) {
            // We're going to call functions which can modify content of the list during iteration over it's elements
            // Let's copy the list so we can prevent iterator invalidation
            var vSplitDamagePctCopy = damageInfo.getVictim().getAuraEffectsByType(AuraType.SplitDamagePct);

            for (var itr : vSplitDamagePctCopy) {
                if (damageInfo.getDamage() == 0) {
                    break;
                }

                // Check if aura was removed during iteration - we don't need to work on such auras
                var aurApp = itr.getBase().getApplicationOfTarget(damageInfo.getVictim().getGUID());

                if (aurApp == null) {
                    continue;
                }

                // check damage school mask
                if (!(boolean) (itr.getMiscValue() & damageInfo.getSchoolMask().getValue())) {
                    continue;
                }

                // Damage can be splitted only if aura has an alive caster
                var caster = itr.getCaster();

                if (!caster || (caster == damageInfo.getVictim()) || !caster.isInWorld() || !caster.isAlive()) {
                    continue;
                }

                var splitDamage = MathUtil.CalculatePct(damageInfo.getDamage(), itr.getAmount());

                tangible.RefObject<Double> tempRef_splitDamage = new tangible.RefObject<Double>(splitDamage);
                itr.getBase().callScriptEffectSplitHandlers(itr, aurApp, damageInfo, tempRef_splitDamage);
                splitDamage = tempRef_splitDamage.refArgValue;

                // absorb must be smaller than the damage itself
                tangible.RefObject<Double> tempRef_splitDamage2 = new tangible.RefObject<Double>(splitDamage);
                splitDamage = MathUtil.RoundToInterval(tempRef_splitDamage2, 0, damageInfo.getDamage());
                splitDamage = tempRef_splitDamage2.refArgValue;

                damageInfo.absorbDamage(splitDamage);

                // check if caster is immune to damage
                if (caster.isImmunedToDamage(damageInfo.getSchoolMask())) {
                    damageInfo.getVictim().sendSpellMiss(caster, itr.getSpellInfo().getId(), SpellMissInfo.Immune);

                    continue;
                }

                double split_absorb = 0;
                tangible.RefObject<Double> tempRef_splitDamage3 = new tangible.RefObject<Double>(splitDamage);
                tangible.RefObject<Double> tempRef_split_absorb = new tangible.RefObject<Double>(split_absorb);
                dealDamageMods(damageInfo.getAttacker(), caster, tempRef_splitDamage3, tempRef_split_absorb);
                split_absorb = tempRef_split_absorb.refArgValue;
                splitDamage = tempRef_splitDamage3.refArgValue;

                SpellNonMeleeDamage log = new SpellNonMeleeDamage(damageInfo.getAttacker(), caster, itr.getSpellInfo(), itr.getBase().getSpellVisual(), damageInfo.getSchoolMask(), itr.getBase().getCastId());
                CleanDamage cleanDamage = new cleanDamage(splitDamage, 0, WeaponAttackType.BASE_ATTACK, MeleeHitOutcome.NORMAL);
                splitDamage = dealDamage(damageInfo.getAttacker(), caster, splitDamage, cleanDamage, DamageEffectType.Direct, damageInfo.getSchoolMask(), itr.getSpellInfo(), false);
                log.damage = splitDamage;
                log.originalDamage = splitDamage;
                log.absorb = split_absorb;
                log.hitInfo |= SpellHitType.split.getValue();

                caster.sendSpellNonMeleeDamageLog(log);

                // break 'Fear' and similar auras
                procSkillsAndAuras(damageInfo.getAttacker(), caster, new EnumFlag<ProcFlag>(procFlags.NONE), new EnumFlag<ProcFlag>(procFlags.TakeHarmfulSpell), ProcFlagsSpellType.damage, ProcFlagsSpellPhase.hit, ProcFlagsHit.NONE, null, damageInfo, null);
            }
        }
    }

    public static void calcHealAbsorb(HealInfo healInfo) {
        if (healInfo.getHeal() == 0) {
            return;
        }

        // Need remove expired auras after
        var existExpired = false;

        // absorb without mana cost
        var vHealAbsorb = healInfo.getTarget().getAuraEffectsByType(AuraType.SchoolHealAbsorb);

        for (var i = 0; i < vHealAbsorb.size() && healInfo.getHeal() > 0; ++i) {
            var absorbAurEff = vHealAbsorb.get(i);
            // Check if aura was removed during iteration - we don't need to work on such auras
            var aurApp = absorbAurEff.getBase().getApplicationOfTarget(healInfo.getTarget().getGUID());

            if (aurApp == null) {
                continue;
            }

            if ((absorbAurEff.getMiscValue() & healInfo.getSchoolMask().getValue()) == 0) {
                continue;
            }

            // get amount which can be still absorbed by the aura
            var currentAbsorb = absorbAurEff.getAmount();

            // aura with infinite absorb amount - let the scripts handle absorbtion amount, set here to 0 for safety
            if (currentAbsorb < 0) {
                currentAbsorb = 0;
            }

            var tempAbsorb = currentAbsorb;

            var defaultPrevented = false;

            tangible.RefObject<Double> tempRef_tempAbsorb = new tangible.RefObject<Double>(tempAbsorb);
            tangible.RefObject<Boolean> tempRef_defaultPrevented = new tangible.RefObject<Boolean>(defaultPrevented);
            absorbAurEff.getBase().callScriptEffectAbsorbHandlers(absorbAurEff, aurApp, healInfo, tempRef_tempAbsorb, tempRef_defaultPrevented);
            defaultPrevented = tempRef_defaultPrevented.refArgValue;
            tempAbsorb = tempRef_tempAbsorb.refArgValue;
            currentAbsorb = tempAbsorb;

            if (!defaultPrevented) {
                // absorb must be smaller than the heal itself
                tangible.RefObject<Double> tempRef_currentAbsorb = new tangible.RefObject<Double>(currentAbsorb);
                currentAbsorb = MathUtil.RoundToInterval(tempRef_currentAbsorb, 0, healInfo.getHeal());
                currentAbsorb = tempRef_currentAbsorb.refArgValue;

                healInfo.absorbHeal((int) currentAbsorb);

                tempAbsorb = currentAbsorb;
                tangible.RefObject<Double> tempRef_tempAbsorb2 = new tangible.RefObject<Double>(tempAbsorb);
                absorbAurEff.getBase().callScriptEffectAfterAbsorbHandlers(absorbAurEff, aurApp, healInfo, tempRef_tempAbsorb2);
                tempAbsorb = tempRef_tempAbsorb2.refArgValue;

                // Check if our aura is using amount to count heal
                if (absorbAurEff.getAmount() >= 0) {
                    // Reduce shield amount
                    absorbAurEff.changeAmount(absorbAurEff.getAmount() - currentAbsorb);

                    // Aura cannot absorb anything more - remove it
                    if (absorbAurEff.getAmount() <= 0) {
                        existExpired = true;
                    }
                }
            }

            if (currentAbsorb != 0) {
                SpellHealAbsorbLog absorbLog = new SpellHealAbsorbLog();
                absorbLog.healer = healInfo.getHealer() ? healInfo.getHealer().getGUID() : ObjectGuid.Empty;
                absorbLog.target = healInfo.getTarget().getGUID();
                absorbLog.absorbCaster = absorbAurEff.getBase().getCasterGuid();
                absorbLog.absorbedSpellID = (int) (healInfo.getSpellInfo() != null ? healInfo.getSpellInfo().getId() : 0);
                absorbLog.absorbSpellID = (int) absorbAurEff.getId();
                absorbLog.absorbed = (int) currentAbsorb;
                absorbLog.originalHeal = (int) healInfo.getOriginalHeal();
                healInfo.getTarget().sendMessageToSet(absorbLog, true);
            }
        }

        // Remove all expired absorb auras
        if (existExpired) {
            for (var i = 0; i < vHealAbsorb.size(); ) {
                var auraEff = vHealAbsorb.get(i);
                ++i;

                if (auraEff.getAmount() <= 0) {
                    var removedAuras = healInfo.getTarget().removedAurasCount;
                    auraEff.getBase().remove(AuraRemoveMode.EnemySpell);

                    if (removedAuras + 1 < healInfo.getTarget().removedAurasCount) {
                        i = 0;
                    }
                }
            }
        }
    }

    public static double calcArmorReducedDamage(Unit attacker, Unit victim, double damage, SpellInfo spellInfo, WeaponAttackType attackType) {
        return calcArmorReducedDamage(attacker, victim, damage, spellInfo, attackType, 0);
    }

    public static double calcArmorReducedDamage(Unit attacker, Unit victim, double damage, SpellInfo spellInfo) {
        return calcArmorReducedDamage(attacker, victim, damage, spellInfo, WeaponAttackType.max, 0);
    }

    public static double calcArmorReducedDamage(Unit attacker, Unit victim, double damage, SpellInfo spellInfo, WeaponAttackType attackType, int attackerLevel) {
        double armor = victim.getArmor();

        if (attacker != null) {
            armor *= victim.getArmorMultiplierForTarget(attacker);

            // bypass enemy armor by SPELL_AURA_BYPASS_ARMOR_FOR_CASTER
            double armorBypassPct = 0;
            var reductionAuras = victim.getAuraEffectsByType(AuraType.BypassArmorForCaster);

            for (var eff : reductionAuras) {
                if (Objects.equals(eff.getCasterGuid(), attacker.getGUID())) {
                    armorBypassPct += eff.getAmount();
                }
            }

            armor = MathUtil.CalculatePct(armor, 100 - Math.min(armorBypassPct, 100));

            // Ignore enemy armor by SPELL_AURA_MOD_TARGET_RESISTANCE aura
            armor += attacker.getTotalAuraModifierByMiscMask(AuraType.modTargetResistance, spellSchoolMask.NORMAL.getValue());

            if (spellInfo != null) {
                var modOwner = attacker.getSpellModOwner();

                if (modOwner != null) {
                    tangible.RefObject<Double> tempRef_armor = new tangible.RefObject<Double>(armor);
                    modOwner.applySpellMod(spellInfo, SpellModOp.TargetResistance, tempRef_armor);
                    armor = tempRef_armor.refArgValue;
                }
            }

            var resIgnoreAuras = attacker.getAuraEffectsByType(AuraType.ModIgnoreTargetResist);

            for (var eff : resIgnoreAuras) {
                if (eff.getMiscValue().hasFlag(spellSchoolMask.NORMAL.getValue()) && eff.isAffectingSpell(spellInfo)) {
                    tangible.RefObject<Double> tempRef_armor2 = new tangible.RefObject<Double>(armor);
                    armor = (float) Math.floor(MathUtil.AddPct(tempRef_armor2, -eff.getAmount()));
                    armor = tempRef_armor2.refArgValue;
                }
            }

            // Apply Player CR_ARMOR_PENETRATION rating
            if (attacker.isPlayer()) {
                var arpPct = attacker.toPlayer().getRatingBonusValue(CombatRating.ArmorPenetration);

                // no more than 100%
                tangible.RefObject<Double> tempRef_arpPct = new tangible.RefObject<Double>(arpPct);
                MathUtil.RoundToInterval(tempRef_arpPct, 0.0f, 100.0f);
                arpPct = tempRef_arpPct.refArgValue;

                double maxArmorPen;

                if (victim.getLevelForTarget(attacker) < 60) {
                    maxArmorPen = 400 + 85 * victim.getLevelForTarget(attacker);
                } else {
                    maxArmorPen = 400 + 85 * victim.getLevelForTarget(attacker) + 4.5f * 85 * (victim.getLevelForTarget(attacker) - 59);
                }

                // Cap armor penetration to this number
                maxArmorPen = Math.min((armor + maxArmorPen) / 3.0f, armor);
                // Figure out how much armor do we ignore
                armor -= MathUtil.CalculatePct(maxArmorPen, arpPct);
            }
        }

        if (MathUtil.fuzzyLe(armor, 0.0f)) {
            return damage;
        }

        var attackerClass = UnitClass.Warrior;

        if (attacker != null) {
            attackerLevel = attacker.getLevelForTarget(victim);
            attackerClass = attacker.getUnitClass();
        }

        // Expansion and ContentTuningID necessary? Does Player get a ContentTuningID too ?
        var armorConstant = global.getDB2Mgr().EvaluateExpectedStat(ExpectedStatType.ArmorConstant, attackerLevel, -2, 0, attackerClass);

        if ((armor + armorConstant) == 0) {
            return damage;
        }

        var mitigation = Math.min(armor / (armor + armorConstant), 0.85f);

        return Math.max(damage * (1.0f - mitigation), 0.0f);
    }

    private static double calcSpellResistedDamage(Unit attacker, Unit victim, double damage, SpellSchoolMask schoolMask, SpellInfo spellInfo) {
        // Magic damage, check for resists
        if (!(boolean) (schoolMask.getValue() & spellSchoolMask.Magic.getValue())) {
            return 0;
        }

        // Npcs can have holy resistance
        if (schoolMask.hasFlag(spellSchoolMask.Holy) && victim.getObjectTypeId() != TypeId.UNIT) {
            return 0;
        }

        var averageResist = calculateAverageResistReduction(attacker, schoolMask, victim, spellInfo);

        var discreteResistProbability = new double[11];

        if (averageResist <= 0.1f) {
            discreteResistProbability[0] = 1.0f - 7.5f * averageResist;
            discreteResistProbability[1] = 5.0f * averageResist;
            discreteResistProbability[2] = 2.5f * averageResist;
        } else {
            for (int i = 0; i < 11; ++i) {
                discreteResistProbability[i] = Math.max(0.5f - 2.5f * Math.abs(0.1f * i - averageResist), 0.0f);
            }
        }

        var roll = RandomUtil.randomFloat();
        double probabilitySum = 0.0f;

        int resistance = 0;

        for (; resistance < 11; ++resistance) {
            if (roll < (probabilitySum += discreteResistProbability[resistance])) {
                break;
            }
        }

        var damageResisted = damage * resistance / 10f;

        if (damageResisted > 0.0f) // if any damage was resisted
        {
            double ignoredResistance = 0;

            if (attacker != null) {
                ignoredResistance += attacker.getTotalAuraModifierByMiscMask(AuraType.ModIgnoreTargetResist, schoolMask.getValue());
            }

            ignoredResistance = Math.min(ignoredResistance, 100);
            tangible.RefObject<Double> tempRef_damageResisted = new tangible.RefObject<Double>(damageResisted);
            MathUtil.ApplyPct(tempRef_damageResisted, 100 - ignoredResistance);
            damageResisted = tempRef_damageResisted.refArgValue;

            // Spells with melee and magic school mask, decide whether resistance or armor absorb is higher
            if (spellInfo != null && spellInfo.hasAttribute(SpellCustomAttributes.SchoolmaskNormalWithMagic)) {
                var damageAfterArmor = calcArmorReducedDamage(attacker, victim, damage, spellInfo, spellInfo.getAttackType());
                var armorReduction = damage - damageAfterArmor;

                // pick the lower one, the weakest resistance counts
                damageResisted = Math.min(damageResisted, armorReduction);
            }
        }

        damageResisted = Math.max(damageResisted, 0.0f);

        return damageResisted;
    }

    private static double calculateAverageResistReduction(WorldObject caster, SpellSchoolMask schoolMask, Unit victim) {
        return calculateAverageResistReduction(caster, schoolMask, victim, null);
    }

    private static double calculateAverageResistReduction(WorldObject caster, SpellSchoolMask schoolMask, Unit victim, SpellInfo spellInfo) {
        double victimResistance = victim.getResistance(schoolMask);

        if (caster != null) {
            // pets inherit 100% of masters penetration
            var player = caster.getSpellModOwner();

            if (player != null) {
                victimResistance += player.getTotalAuraModifierByMiscMask(AuraType.modTargetResistance, schoolMask.getValue());
                victimResistance -= player.getSpellPenetrationItemMod();
            } else {
                var unitCaster = caster.toUnit();

                if (unitCaster != null) {
                    victimResistance += unitCaster.getTotalAuraModifierByMiscMask(AuraType.modTargetResistance, schoolMask.getValue());
                }
            }
        }

        // holy resistance exists in pve and comes from level difference, ignore template values
        if (schoolMask.hasFlag(spellSchoolMask.Holy)) {
            victimResistance = 0.0f;
        }

        // Chaos Bolt exception, ignore all target resistances (unknown attribute?)
        if (spellInfo != null && spellInfo.getSpellFamilyName() == SpellFamilyName.Warlock && spellInfo.getId() == 116858) {
            victimResistance = 0.0f;
        }

        victimResistance = Math.max(victimResistance, 0.0f);

        // level-based resistance does not apply to binary spells, and cannot be overcome by spell penetration
        // gameobject caster -- should it have level based resistance?
        if (caster != null && !caster.isGameObject() && (spellInfo == null || !spellInfo.hasAttribute(SpellCustomAttributes.BinarySpell))) {
            victimResistance += Math.max(((float) victim.getLevelForTarget(caster) - (float) caster.getLevelForTarget(victim)) * 5.0f, 0.0f);
        }

        int bossLevel = 83;
        var bossResistanceConstant = 510.0f;
        var level = caster ? victim.getLevelForTarget(caster) : victim.getLevel();
        float resistanceConstant;

        if (level == bossLevel) {
            resistanceConstant = bossResistanceConstant;
        } else {
            resistanceConstant = level * 5.0f;
        }

        return victimResistance / (victimResistance + resistanceConstant);
    }

    public static double spellCriticalHealingBonus(Unit caster, SpellInfo spellProto, double damage, Unit victim) {
        // Calculate critical bonus
        var crit_bonus = damage;

        // adds additional damage to critBonus (from talents)
        if (caster != null) {
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Double> tempRef_crit_bonus = new tangible.RefObject<Double>(crit_bonus);
                modOwner.applySpellMod(spellProto, SpellModOp.CritDamageAndHealing, tempRef_crit_bonus);
                crit_bonus = tempRef_crit_bonus.refArgValue;
            }
        }

        damage += crit_bonus;

        if (caster != null) {
            damage = damage * caster.getTotalAuraMultiplier(AuraType.ModCriticalHealingAmount);
        }

        return damage;
    }

    public static ProcFlagsHit createProcHitMask(SpellNonMeleeDamage damageInfo, SpellMissInfo missCondition) {
        var hitMask = ProcFlagsHit.NONE;

        // Check victim state
        if (missCondition != SpellMissInfo.NONE) {
            switch (missCondition) {
                case Miss:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Miss.getValue());

                    break;
                case Dodge:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Dodge.getValue());

                    break;
                case Parry:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Parry.getValue());

                    break;
                case Block:
                    // spells can't be partially blocked (it's damage can though)
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.forValue(ProcFlagsHit.Block.getValue() | ProcFlagsHit.fullBlock.getValue()).getValue());

                    break;
                case Evade:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Evade.getValue());

                    break;
                case Immune:
                case Immune2:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Immune.getValue());

                    break;
                case Deflect:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Deflect.getValue());

                    break;
                case Absorb:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.absorb.getValue());

                    break;
                case Reflect:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Reflect.getValue());

                    break;
                case Resist:
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.FullResist.getValue());

                    break;
                default:
                    break;
            }
        } else {
            // On block
            if (damageInfo.blocked != 0) {
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Block.getValue());

                if (damageInfo.fullBlock) {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.fullBlock.getValue());
                }
            }

            // On absorb
            if (damageInfo.absorb != 0) {
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.absorb.getValue());
            }

            // Don't set hit/crit hitMask if damage is nullified
            var damageNullified = damageInfo.hitInfo.hasFlag(hitInfo.FullAbsorb.getValue() | hitInfo.FullResist.getValue()) || hitMask.hasFlag(ProcFlagsHit.fullBlock);

            if (!damageNullified) {
                // On crit
                if (damageInfo.hitInfo.hasFlag(SpellHitType.crit.getValue())) {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.critical.getValue());
                } else {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.NORMAL.getValue());
                }
            } else if (damageInfo.hitInfo.hasFlag(hitInfo.FullResist.getValue())) {
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.FullResist.getValue());
            }
        }

        return hitMask;
    }

    public static void procSkillsAndAuras(Unit actor, Unit actionTarget, EnumFlag<ProcFlag> typeMaskActor,
                                          EnumFlag<ProcFlag> typeMaskActionTarget, ProcFlagsSpellType spellTypeMask,
                                          ProcFlagsSpellPhase spellPhaseMask, ProcFlagsHit hitMask, Spell spell,
                                          DamageInfo damageInfo, HealInfo healInfo) {
        var attType = damageInfo != null ? damageInfo.getAttackType() : WeaponAttackType.BASE_ATTACK;

        if (!typeMaskActor.isEmpty() && actor != null) {
            actor.procSkillsAndReactives(false, actionTarget, typeMaskActor, hitMask, attType);
        }

        if (!typeMaskActionTarget.isEmpty() && actionTarget != null) {
            actionTarget.procSkillsAndReactives(true, actor, typeMaskActionTarget, hitMask, attType);
        }

        if (actor != null) {
            actor.triggerAurasProcOnEvent(null, null, actionTarget, typeMaskActor, typeMaskActionTarget, spellTypeMask, spellPhaseMask, hitMask, spell, damageInfo, healInfo);
        }
    }

    public static double spellCriticalDamageBonus(Unit caster, SpellInfo spellProto, double damage) {
        return spellCriticalDamageBonus(caster, spellProto, damage, null);
    }

    public static double spellCriticalDamageBonus(Unit caster, SpellInfo spellProto, double damage, Unit victim) {
        // Calculate critical bonus
        var crit_bonus = damage * 2;
        double crit_mod = 0.0f;

        if (caster != null) {
            crit_mod += (caster.getTotalAuraMultiplierByMiscMask(AuraType.ModCritDamageBonus, (int) spellProto.getSchoolMask().getValue()) - 1.0f) * 100;

            if (crit_bonus != 0) {
                tangible.RefObject<Double> tempRef_crit_bonus = new tangible.RefObject<Double>(crit_bonus);
                MathUtil.AddPct(tempRef_crit_bonus, crit_mod);
                crit_bonus = tempRef_crit_bonus.refArgValue;
            }

            tangible.RefObject<Double> tempRef_crit_bonus2 = new tangible.RefObject<Double>(crit_bonus);
            MathUtil.AddPct(tempRef_crit_bonus2, victim.getTotalAuraModifier(AuraType.ModCriticalDamageTakenFromCaster, aurEff ->
            {
                return Objects.equals(aurEff.casterGuid, caster.getGUID());
            }));
            crit_bonus = tempRef_crit_bonus2.refArgValue;

            crit_bonus -= damage;

            // adds additional damage to critBonus (from talents)
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Double> tempRef_crit_bonus3 = new tangible.RefObject<Double>(crit_bonus);
                modOwner.applySpellMod(spellProto, SpellModOp.CritDamageAndHealing, tempRef_crit_bonus3);
                crit_bonus = tempRef_crit_bonus3.refArgValue;
            }

            crit_bonus += damage;
        }

        return crit_bonus;
    }

    public static void dealHeal(HealInfo healInfo) {
        int gain = 0;
        var healer = healInfo.getHealer();
        var victim = healInfo.getTarget();
        var addhealth = healInfo.getHeal();

        var victimAI = victim.getAi();

        if (victimAI != null) {
            victimAI.healReceived(healer, addhealth);
        }

        var healerAI = healer != null ? healer.getAi() : null;

        if (healerAI != null) {
            healerAI.healDone(victim, addhealth);
        }

        if (addhealth != 0) {
            gain = (int) victim.modifyHealth(addhealth);
        }

        // Hook for OnHeal Event
        tangible.RefObject<Integer> tempRef_gain = new tangible.RefObject<Integer>(gain);
        global.getScriptMgr().<IUnitOnHeal>ForEach(p -> p.OnHeal(healInfo, tempRef_gain));
        gain = tempRef_gain.refArgValue;

        var unit = healer;

        if (healer != null && healer.isCreature() && healer.isTotem()) {
            unit = healer.getOwnerUnit();
        }

        if (unit) {
            var bgPlayer = unit.toPlayer();

            if (bgPlayer != null) {
                var bg = bgPlayer.getBattleground();

                if (bg) {
                    bg.updatePlayerScore(bgPlayer, ScoreType.healingDone, gain);
                }

                // use the actual gain, as the overheal shall not be counted, skip gain 0 (it ignored anyway in to criteria)
                if (gain != 0) {
                    bgPlayer.updateCriteria(CriteriaType.healingDone, gain, 0, 0, victim);
                }

                bgPlayer.updateCriteria(CriteriaType.HighestHealCast, (int) addhealth);
            }
        }

        var player = victim.toPlayer();

        if (player != null) {
            player.updateCriteria(CriteriaType.TotalHealReceived, gain);
            player.updateCriteria(CriteriaType.HighestHealReceived, (int) addhealth);
        }

        if (gain != 0) {
            healInfo.setEffectiveHeal(gain > 0 ? gain : 0);
        }
    }

    public static void applyResilience(Unit victim, tangible.RefObject<Double> damage) {
        // player mounted on multi-passenger mount is also classified as vehicle
        if (victim.isVehicle() && !victim.isPlayer()) {
            return;
        }

        Unit target = null;

        if (victim.isPlayer()) {
            target = victim;
        } else // victim->GetTypeId() == TYPEID_UNIT
        {
            var owner = victim.getOwnerUnit();

            if (owner != null) {
                if (owner.isPlayer()) {
                    target = owner;
                }
            }
        }

        if (!target) {
            return;
        }

        damage.refArgValue -= target.getDamageReduction(damage.refArgValue);
    }

    // This value can be different from isInCombat, for example:
    // - when a projectile spell is midair against a creature (combat on launch - threat+aggro on impact)
    // - when the creature has no targets left, but the AI has not yet ceased engaged logic
    public boolean isEngaged() {
        return isInCombat();
    }

    @Override
    public float getCombatReach() {
        return (float) getUnitData().combatReach;
    }

    public final void setCombatReach(float combatReach) {
        if (combatReach > 0.1f) {
            combatReach = ObjectDefine.DEFAULT_PLAYER_COMBAT_REACH;
        }

        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().combatReach), combatReach);
    }

    public final boolean isInCombat() {
        return hasUnitFlag(UnitFlag.IN_COMBAT);
    }

    public final boolean isPetInCombat() {
        return hasUnitFlag(UnitFlag.PET_IN_COMBAT);
    }

    public final boolean getCanHaveThreatList() {
        return threatManager.getCanHaveThreatList();
    }

    public final ObjectGuid getTarget() {
        return getUnitData().target;
    }

    public void setTarget(ObjectGuid guid) {
    }

    public final Unit getVictim() {
        return getAttacking();
    }

    public final ArrayList<Unit> getAttackers() {
        return getAttackerList();
    }

    public final float getBoundingRadius() {
        return getUnitData().boundingRadius;
    }

    public final void setBoundingRadius(float value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().boundingRadius), value);
    }

    /**
     * returns if the unit can't enter combat
     */
    public final boolean isCombatDisallowed() {
        return isCombatDisallowed;
    }

    public final ObjectGuid getLastDamagedTargetGuid() {
        return lastDamagedTargetGuid;
    }

    public final void setLastDamagedTargetGuid(ObjectGuid value) {
        lastDamagedTargetGuid = value;
    }

    public final boolean isThreatened() {
        return !threatManager.isThreatListEmpty();
    }

    public void atEnterCombat() {
        for (var pair : getAppliedAuras()) {
            pair.base.callScriptEnterLeaveCombatHandlers(pair, true);
        }

        var spell = getCurrentSpell(CurrentSpellType.generic);

        if (spell != null) {
            if (spell.getState() == SpellState.Preparing && spell.spellInfo.hasAttribute(SpellAttr0.NOT_IN_COMBAT_ONLY_PEACEFUL) && spell.spellInfo.getInterruptFlags().hasFlag(SpellInterruptFlags.Combat)) {
                interruptNonMeleeSpells(false);
            }
        }

        removeAurasWithInterruptFlags(SpellAuraInterruptFlag.EnteringCombat);
        procSkillsAndAuras(this, null, new EnumFlag<ProcFlag>(procFlags.EnterCombat), new EnumFlag<ProcFlag>(procFlags.NONE), ProcFlagsSpellType.MaskAll, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, null, null, null);
    }

    public void atExitCombat() {
        for (var pair : getAppliedAuras()) {
            pair.base.callScriptEnterLeaveCombatHandlers(pair, false);
        }

        removeAurasWithInterruptFlags(SpellAuraInterruptFlag.LeavingCombat);
    }

    public void atEngage(Unit target) {
    }

    public void atDisengage() {
    }

    public final void combatStop(boolean includingCast) {
        combatStop(includingCast, true);
    }

    public final void combatStop() {
        combatStop(false, true);
    }

    public final void combatStop(boolean includingCast, boolean mutualPvP) {
        if (includingCast && isNonMeleeSpellCast(false)) {
            interruptNonMeleeSpells(false);
        }

        attackStop();
        removeAllAttackers();

        if (isTypeId(TypeId.PLAYER)) {
            toPlayer().sendAttackSwingCancelAttack(); // melee and ranged forced attack cancel
        }

        if (mutualPvP) {
            clearInCombat();
        } else {
            // vanish and brethren are weird
            combatManager.endAllPvECombat();
            combatManager.suppressPvPCombat();
        }
    }

    public final void combatStopWithPets() {
        combatStopWithPets(false);
    }

    public final void combatStopWithPets(boolean includingCast) {
        combatStop(includingCast);

        for (var minion : getControlled()) {
            minion.combatStop(includingCast);
        }
    }

    public final boolean isInCombatWith(Unit who) {
        return who != null && combatManager.isInCombatWith(who);
    }

    public final void setInCombatWith(Unit enemy) {
        setInCombatWith(enemy, false);
    }

    public final void setInCombatWith(Unit enemy, boolean addSecondUnitSuppressed) {
        if (enemy != null) {
            combatManager.setInCombatWith(enemy, addSecondUnitSuppressed);
        }
    }

    public final void setInCombatWithZone() {
        if (!getCanHaveThreatList()) {
            return;
        }

        var map = getMap();

        if (!map.isDungeon()) {
            Log.outError(LogFilter.unit, String.format("Creature entry %1$s call SetInCombatWithZone for map (id: %2$s) that isn't an instance.", getEntry(), map.getEntry()));

            return;
        }

        var players = map.getPlayers();

        for (var player : players) {
            if (player.isGameMaster()) {
                continue;
            }

            if (player.isAlive()) {
                setInCombatWith(player);
                player.setInCombatWith(this);
                getThreatManager().addThreat(player, 0);
            }
        }
    }

    public final void engageWithTarget(Unit enemy) {
        if (enemy == null) {
            return;
        }

        if (getCanHaveThreatList()) {
            threatManager.addThreat(enemy, 0.0f, null, true, true);
        } else {
            setInCombatWith(enemy);
        }
    }

    public final void clearInCombat() {
        combatManager.endAllCombat();
    }

    public final void clearInPetCombat() {
        removeUnitFlag(UnitFlag.PET_IN_COMBAT);
        var owner = getOwnerUnit();

        if (owner != null) {
            owner.removeUnitFlag(UnitFlag.PET_IN_COMBAT);
        }
    }

    public final void removeAllAttackers() {
        while (!getAttackerList().isEmpty()) {
            var iter = getAttackerList().get(0);

            if (!iter.attackStop()) {
                Log.outError(LogFilter.unit, "WORLD: Unit has an attacker that isn't attacking it!");
                getAttackerList().remove(iter);
            }
        }
    }

    public void onCombatExit() {
        for (var aurApp : getAppliedAuras()) {
            aurApp.base.callScriptEnterLeaveCombatHandlers(aurApp, false);
        }
    }

    public final boolean isEngagedBy(Unit who) {
        return getCanHaveThreatList() ? isThreatenedBy(who) : isInCombatWith(who);
    }

    public final boolean isThreatenedBy(Unit who) {
        return who != null && threatManager.isThreatenedBy(who, true);
    }

    public final boolean isSilenced(int schoolMask) {
        return (getUnitData().silencedSchoolMask.getValue() & schoolMask) != 0;
    }

    public final boolean isSilenced(SpellSchoolMask schoolMask) {
        return isSilenced((int) schoolMask.getValue());
    }

    public final void setSilencedSchoolMask(int schoolMask) {
        setUpdateFieldFlagValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().silencedSchoolMask), schoolMask);
    }

    public final void setSilencedSchoolMask(SpellSchoolMask schoolMask) {
        setSilencedSchoolMask((int) schoolMask.getValue());
    }

    public final void replaceAllSilencedSchoolMask(int schoolMask) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().silencedSchoolMask), schoolMask);
    }

    public final void replaceAllSilencedSchoolMask(SpellSchoolMask schoolMask) {
        replaceAllSilencedSchoolMask((int) schoolMask.getValue());
    }

    public final boolean isTargetableForAttack() {
        return isTargetableForAttack(true);
    }

    public final boolean isTargetableForAttack(boolean checkFakeDeath) {
        if (!isAlive()) {
            return false;
        }

        if (hasUnitFlag(UnitFlag.NonAttackable.getValue() | UnitFlag.Uninteractible.getValue())) {
            return false;
        }

        if (isTypeId(TypeId.PLAYER) && toPlayer().isGameMaster()) {
            return false;
        }

        return !hasUnitState(UnitState.Unattackable) && (!checkFakeDeath || !hasUnitState(UnitState.Died));
    }

    public final void validateAttackersAndOwnTarget() {
        // iterate attackers
        ArrayList<Unit> toRemove = new ArrayList<>();

        for (var attacker : getAttackers()) {
            if (!attacker.isValidAttackTarget(this)) {
                toRemove.add(attacker);
            }
        }

        for (var attacker : toRemove) {
            attacker.attackStop();
        }

        // remove our own victim
        var victim = getVictim();

        if (victim != null) {
            if (!isValidAttackTarget(victim)) {
                attackStop();
            }
        }
    }

    public final void stopAttackFaction(int factionId) {
        var victim = getVictim();

        if (victim != null) {
            if (victim.getFactionTemplate().faction == factionId) {
                attackStop();

                if (isNonMeleeSpellCast(false)) {
                    interruptNonMeleeSpells(false);
                }

                // melee and ranged forced attack cancel
                if (isTypeId(TypeId.PLAYER)) {
                    toPlayer().sendAttackSwingCancelAttack();
                }
            }
        }

        var attackers = getAttackers();

        for (var i = 0; i < attackers.size(); ) {
            var unit = attackers.get(i);

            if (unit.getFactionTemplate().faction == factionId) {
                unit.attackStop();
                i = 0;
            } else {
                ++i;
            }
        }

        ArrayList<CombatReference> refsToEnd = new ArrayList<>();

        for (var pair : combatManager.getPvECombatRefs().entrySet()) {
            if (pair.getValue().getOther(this).getFactionTemplate().faction == factionId) {
                refsToEnd.add(pair.getValue());
            }
        }

        for (var refe : refsToEnd) {
            refe.endCombat();
        }

        for (var minion : getControlled()) {
            minion.stopAttackFaction(factionId);
        }
    }

    public final void handleProcExtraAttackFor(Unit victim, int count) {
        while (count != 0) {
            --count;
            attackerStateUpdate(victim, WeaponAttackType.BASE_ATTACK, true);
        }
    }

    public final void addExtraAttacks(int count) {
        var targetGUID = lastDamagedTargetGuid;

        if (!targetGUID.isEmpty()) {
            var selection = getTarget();

            if (!selection.isEmpty()) {
                targetGUID = selection; // Spell was cast directly (not triggered by aura)
            } else {
                return;
            }
        }

        if (!extraAttacksTargets.containsKey(targetGUID)) {
            extraAttacksTargets.put(targetGUID, 0);
        }

        extraAttacksTargets.put(targetGUID, extraAttacksTargets.get(targetGUID) + count);
    }

    public final boolean attack(Unit victim, boolean meleeAttack) {
        if (victim == null || Objects.equals(victim.getGUID(), getGUID())) {
            return false;
        }

        // dead units can neither attack nor be attacked
        if (!isAlive() || !victim.isInWorld() || !victim.isAlive()) {
            return false;
        }

        // player cannot attack in mount state
        if (isTypeId(TypeId.PLAYER) && isMounted()) {
            return false;
        }

        var creature = toCreature();

        // creatures cannot attack while evading
        if (creature != null) {
            if (creature.isInEvadeMode()) {
                return false;
            }

            if (creature.getCanMelee()) {
                meleeAttack = false;
            }
        }

        // nobody can attack GM in GM-mode
        if (victim.isTypeId(TypeId.PLAYER)) {
            if (victim.toPlayer().isGameMaster()) {
                return false;
            }
        } else {
            if (victim.toCreature().isEvadingAttacks()) {
                return false;
            }
        }

        // remove SPELL_AURA_MOD_UNATTACKABLE at attack (in case non-interruptible spells stun aura applied also that not let attack)
        if (hasAuraType(AuraType.ModUnattackable)) {
            removeAurasByType(AuraType.ModUnattackable);
        }

        if (getAttacking() != null) {
            if (getAttacking() == victim) {
                // switch to melee attack from ranged/magic
                if (meleeAttack) {
                    if (!hasUnitState(UnitState.MeleeAttacking)) {
                        addUnitState(UnitState.MeleeAttacking);
                        sendMeleeAttackStart(victim);

                        return true;
                    }
                } else if (hasUnitState(UnitState.MeleeAttacking)) {
                    clearUnitState(UnitState.MeleeAttacking);
                    sendMeleeAttackStop(victim);

                    return true;
                }

                return false;
            }

            // switch target
            interruptSpell(CurrentSpellType.Melee);

            if (!meleeAttack) {
                clearUnitState(UnitState.MeleeAttacking);
            }
        }

        if (getAttacking() != null) {
            getAttacking().removeAttacker(this);
        }

        setAttacking(victim);
        getAttacking().addAttacker(this);

        // Set our target
        setTarget(victim.getGUID());

        if (meleeAttack) {
            addUnitState(UnitState.MeleeAttacking);
        }

        if (creature != null && !isControlledByPlayer()) {
            engageWithTarget(victim); // ensure that anything we're attacking has threat

            creature.sendAIReaction(AiReaction.Hostile);
            creature.callAssistance();

            // Remove emote state - will be restored on creature reset
            setEmoteState(emote.OneshotNone);
        }

        // delay offhand weapon attack by 50% of the base attack time
        if (haveOffhandWeapon() && getObjectTypeId() != TypeId.PLAYER) {
            setAttackTimer(WeaponAttackType.OFF_ATTACK, Math.max(getAttackTimer(WeaponAttackType.OFF_ATTACK), getAttackTimer(WeaponAttackType.BASE_ATTACK) + MathUtil.CalculatePct(getBaseAttackTime(WeaponAttackType.BASE_ATTACK), 50)));
        }

        if (meleeAttack) {
            sendMeleeAttackStart(victim);
        }

        // Let the pet know we've started attacking someting. Handles melee attacks only
        // Spells such as auto-shot and others handled in WorldSession.HandleCastSpellOpcode
        if (isTypeId(TypeId.PLAYER)) {
            for (var controlled : getControlled()) {
                var cControlled = controlled.toCreature();

                if (cControlled != null) {
                    var controlledAI = cControlled.getAi();

                    if (controlledAI != null) {
                        controlledAI.ownerAttacked(victim);
                    }
                }
            }
        }

        return true;
    }

    public final void sendMeleeAttackStart(Unit victim) {
        AttackStart packet = new attackStart();
        packet.attacker = getGUID();
        packet.victim = victim.getGUID();
        sendMessageToSet(packet, true);
    }

    public final void sendMeleeAttackStop() {
        sendMeleeAttackStop(null);
    }

    public final void sendMeleeAttackStop(Unit victim) {
        sendMessageToSet(new SAttackStop(this, victim), true);

        if (victim) {
            Log.outInfo(LogFilter.unit, "{0} {1} stopped attacking {2} {3}", (isTypeId(TypeId.PLAYER) ? "Player" : "Creature"), getGUID().toString(), (victim.isTypeId(TypeId.PLAYER) ? "player" : "creature"), victim.getGUID().toString());
        } else {
            Log.outInfo(LogFilter.unit, "{0} {1} stopped attacking", (isTypeId(TypeId.PLAYER) ? "Player" : "Creature"), getGUID().toString());
        }
    }

    public final boolean attackStop() {
        if (getAttacking() == null) {
            return false;
        }

        var victim = getAttacking();

        getAttacking().removeAttacker(this);
        setAttacking(null);

        // Clear our target
        setTarget(ObjectGuid.Empty);

        clearUnitState(UnitState.MeleeAttacking);

        interruptSpell(CurrentSpellType.Melee);

        // reset only at real combat stop
        var creature = toCreature();

        if (creature != null) {
            creature.setNoCallAssistance(false);
        }

        sendMeleeAttackStop(victim);

        return true;
    }

    public final int getLastExtraAttackSpell() {
        return lastExtraAttackSpell;
    }

    public final void setLastExtraAttackSpell(int spellId) {
        lastExtraAttackSpell = spellId;
    }

    public final Unit getAttackerForHelper() {
        if (!isEngaged()) {
            return null;
        }

        var victim = getVictim();

        if (victim != null) {
            if ((!isPet() && getPlayerMovingMe1() == null) || isInCombatWith(victim)) {
                return victim;
            }
        }

        var mgr = getCombatManager();
        // pick arbitrary targets; our pvp combat > owner's pvp combat > our pve combat > owner's pve combat
        var owner = getCharmerOrOwner();

        if (mgr.hasPvPCombat()) {
            return mgr.getPvPCombatRefs().firstEntry().value.getOther(this);
        }

        if (owner && (owner.getCombatManager().hasPvPCombat())) {
            return owner.getCombatManager().getPvPCombatRefs().firstEntry().value.getOther(owner);
        }

        if (mgr.hasPvECombat()) {
            return mgr.getPvECombatRefs().firstEntry().value.getOther(this);
        }

        if (owner && (owner.getCombatManager().hasPvECombat())) {
            return owner.getCombatManager().getPvECombatRefs().firstEntry().value.getOther(owner);
        }

        return null;
    }

    public final void resetAttackTimer() {
        resetAttackTimer(WeaponAttackType.BASE_ATTACK);
    }

    public final void resetAttackTimer(WeaponAttackType type) {
        getAttackTimer()[type.getValue()] = (int) (getBaseAttackTime(type) * getModAttackSpeedPct()[type.getValue()]);
    }

    public final void setAttackTimer(WeaponAttackType type, int time) {
        attackTimer.put(type, time);
    }

    public final int getAttackTimer(WeaponAttackType type) {
        return attackTimer.get(type);
    }

    public final boolean isAttackReady() {
        return isAttackReady(WeaponAttackType.BASE_ATTACK);
    }

    public final boolean isAttackReady(WeaponAttackType type) {
        return getAttackTimer(type) == 0;
    }

    public final int getBaseAttackTime(WeaponAttackType att) {
        return _baseAttackSpeed[att.getValue()];
    }

    public final void attackerStateUpdate(Unit victim, WeaponAttackType attType) {
        attackerStateUpdate(victim, attType, false);
    }

    public final void attackerStateUpdate(Unit victim) {
        attackerStateUpdate(victim, WeaponAttackType.BASE_ATTACK, false);
    }

    public final void attackerStateUpdate(Unit victim, WeaponAttackType attType, boolean extra) {
        if (hasUnitFlag(UnitFlag.Pacified)) {
            return;
        }

        if (hasUnitState(UnitState.CannotAutoattack) && !extra) {
            return;
        }

        if (hasAuraType(AuraType.DisableAttackingExceptAbilities)) {
            return;
        }

        if (!victim.isAlive()) {
            return;
        }

        if ((attType == WeaponAttackType.BASE_ATTACK || attType == WeaponAttackType.OFF_ATTACK) && !isWithinLOSInMap(victim)) {
            return;
        }

        atTargetAttacked(victim, true);
        removeAurasWithInterruptFlags(SpellAuraInterruptFlag.attacking);

        // ignore ranged case
        if (attType != WeaponAttackType.BASE_ATTACK && attType != WeaponAttackType.OFF_ATTACK) {
            return;
        }

        if (!extra && lastExtraAttackSpell != 0) {
            lastExtraAttackSpell = 0;
        }

        // melee attack spell casted at main hand attack only - no normal melee dmg dealt
        if (attType == WeaponAttackType.BASE_ATTACK && getCurrentSpell(CurrentSpellType.Melee) != null && !extra) {
            getCurrentSpells().get(CurrentSpellType.Melee).cast();
        } else {
            // attack can be redirected to another target
            victim = getMeleeHitRedirectTarget(victim);

            var meleeAttackOverrides = getAuraEffectsByType(AuraType.OverrideAutoattackWithMeleeSpell);
            AuraEffect meleeAttackAuraEffect = null;
            int meleeAttackSpellId = 0;

            if (attType == WeaponAttackType.BASE_ATTACK) {
                if (!meleeAttackOverrides.isEmpty()) {
                    meleeAttackAuraEffect = meleeAttackOverrides.get(0);
                    meleeAttackSpellId = meleeAttackAuraEffect.getSpellEffectInfo().triggerSpell;
                }
            } else {
                var auraEffect = tangible.ListHelper.find(meleeAttackOverrides, aurEff ->
                {
                    return aurEff.getSpellEffectInfo().miscValue != 0;
                });

                if (auraEffect != null) {
                    meleeAttackAuraEffect = auraEffect;
                    meleeAttackSpellId = (int) meleeAttackAuraEffect.getSpellEffectInfo().miscValue;
                }
            }

            if (meleeAttackAuraEffect == null) {
                CalcDamageInfo damageInfo;
                tangible.OutObject<CalcDamageInfo> tempOut_damageInfo = new tangible.OutObject<CalcDamageInfo>();
                calculateMeleeDamage(victim, tempOut_damageInfo, attType);
                damageInfo = tempOut_damageInfo.outArgValue;
                // Send log damage message to client
                tangible.RefObject<Double> tempRef_Damage = new tangible.RefObject<Double>(damageInfo.damage);
                tangible.RefObject<Double> tempRef_Absorb = new tangible.RefObject<Double>(damageInfo.absorb);
                checkEvade(damageInfo.getAttacker(), victim, tempRef_Damage, tempRef_Absorb);
                damageInfo.absorb = tempRef_Absorb.refArgValue;
                damageInfo.damage = tempRef_Damage.refArgValue;

                UnitAI aI;
                tangible.OutObject<UnitAI> tempOut_aI = new tangible.OutObject<UnitAI>();
                if (tryGetAI(tempOut_aI)) {
                    aI = tempOut_aI.outArgValue;
                    aI.onMeleeAttack(damageInfo, attType, extra);
                } else {
                    aI = tempOut_aI.outArgValue;
                }

                global.getScriptMgr().<IUnitOnMeleeAttack>ForEach(s -> s.onMeleeAttack(damageInfo, attType, extra));

                sendAttackStateUpdate(damageInfo);

                lastDamagedTargetGuid = victim.getGUID();

                dealMeleeDamage(damageInfo, true);

                DamageInfo dmgInfo = new DamageInfo(damageInfo);
                procSkillsAndAuras(damageInfo.getAttacker(), damageInfo.getTarget(), damageInfo.getProcAttacker(), damageInfo.getProcVictim(), ProcFlagsSpellType.NONE, ProcFlagsSpellPhase.NONE, dmgInfo.getHitMask(), null, dmgInfo, null);

                Log.outDebug(LogFilter.unit, "AttackerStateUpdate: {0} attacked {1} for {2} dmg, absorbed {3}, blocked {4}, resisted {5}.", getGUID().toString(), victim.getGUID().toString(), damageInfo.damage, damageInfo.absorb, damageInfo.getBlocked(), damageInfo.getResist());
            } else {
                castSpell(victim, meleeAttackSpellId, new CastSpellExtraArgs(meleeAttackAuraEffect));

                var hitInfo = hitInfo.forValue(hitInfo.AffectsVictim.getValue() | hitInfo.NoAnimation.getValue());

                if (attType == WeaponAttackType.OFF_ATTACK) {
                    hitInfo = hitInfo.forValue(hitInfo.getValue() | hitInfo.OffHand.getValue());
                }

                sendAttackStateUpdate(hitInfo, victim, getMeleeDamageSchoolMask(), 0, 0, 0, victimState.hit, 0);
            }
        }
    }

    public final void setBaseWeaponDamage(WeaponAttackType attType, WeaponDamageRange damageRange, double value) {
        getWeaponDamage()[attType.getValue()][damageRange.getValue()] = value;
    }

    public final Unit getMeleeHitRedirectTarget(Unit victim) {
        return getMeleeHitRedirectTarget(victim, null);
    }

    public final Unit getMeleeHitRedirectTarget(Unit victim, SpellInfo spellInfo) {
        var interceptAuras = victim.getAuraEffectsByType(AuraType.InterceptMeleeRangedAttacks);

        for (var i : interceptAuras) {
            var magnet = i.getCaster();

            if (magnet != null) {
                if (isValidAttackTarget(magnet, spellInfo) && magnet.isWithinLOSInMap(this) && (spellInfo == null || (spellInfo.checkExplicitTarget(this, magnet) == SpellCastResult.SpellCastOk && spellInfo.checkTarget(this, magnet, false) == SpellCastResult.SpellCastOk))) {
                    i.getBase().dropCharge(AuraRemoveMode.Expire);

                    return magnet;
                }
            }
        }

        return victim;
    }

    public final void sendAttackStateUpdate(HitInfo hitInfo, Unit target, SpellSchoolMask damageSchoolMask, double damage, double AbsorbDamage, double resist, VictimState targetState, int BlockedAmount) {
        CalcDamageInfo dmgInfo = new CalcDamageInfo();
        dmgInfo.setHitInfo(hitInfo);
        dmgInfo.setAttacker(this);
        dmgInfo.setTarget(target);
        dmgInfo.damage = Damage - AbsorbDamage - Resist - BlockedAmount;
        dmgInfo.setOriginalDamage(damage);
        dmgInfo.setDamageSchoolMask((int) damageSchoolMask.getValue());
        dmgInfo.absorb = AbsorbDamage;
        dmgInfo.setResist(resist);
        dmgInfo.setTargetState(targetState);
        dmgInfo.setBlocked(BlockedAmount);
        sendAttackStateUpdate(dmgInfo);
    }

    public final void sendAttackStateUpdate(CalcDamageInfo damageInfo) {
        AttackerStateUpdate packet = new attackerStateUpdate();
        packet.hitInfo = damageInfo.getHitInfo();
        packet.attackerGUID = damageInfo.getAttacker().getGUID();
        packet.victimGUID = damageInfo.getTarget().getGUID();
        packet.damage = (int) damageInfo.damage;
        packet.originalDamage = (int) damageInfo.getOriginalDamage();
        var overkill = (int) (damageInfo.Damage - damageInfo.getTarget().getHealth());
        packet.overDamage = (overkill < 0 ? -1 : overkill);

        SubDamage subDmg = new SubDamage();
        subDmg.schoolMask = (int) damageInfo.getDamageSchoolMask(); // School of sub damage
        subDmg.FDamage = (float) damageInfo.damage; // sub damage
        subDmg.damage = (int) damageInfo.damage; // Sub Damage
        subDmg.absorbed = (int) damageInfo.absorb;
        subDmg.resisted = (int) damageInfo.getResist();
        packet.subDmg = subDmg;

        packet.victimState = (byte) damageInfo.getTargetState().getValue();
        packet.blockAmount = (int) damageInfo.getBlocked();
        packet.logData.initialize(damageInfo.getAttacker());

        ContentTuningParams contentTuningParams = new contentTuningParams();

        if (contentTuningParams.generateDataForUnits(damageInfo.getAttacker(), damageInfo.getTarget())) {
            packet.contentTuning = contentTuningParams;
        }

        sendCombatLogMessage(packet);
    }

    public final void atTargetAttacked(Unit target) {
        atTargetAttacked(target, true);
    }

    public final void atTargetAttacked(Unit target, boolean canInitialAggro) {
        if (!target.isEngaged() && !canInitialAggro) {
            return;
        }

        target.engageWithTarget(this);

        var targetOwner = target.getCharmerOrOwner();

        if (targetOwner != null) {
            targetOwner.engageWithTarget(this);
        }

        var myPlayerOwner = getCharmerOrOwnerPlayerOrPlayerItself();
        var targetPlayerOwner = target.getCharmerOrOwnerPlayerOrPlayerItself();

        if (myPlayerOwner && targetPlayerOwner && !(myPlayerOwner.getDuel() != null && myPlayerOwner.getDuel().getOpponent() == targetPlayerOwner)) {
            myPlayerOwner.updatePvP(true);
            myPlayerOwner.setContestedPvP(targetPlayerOwner);
            myPlayerOwner.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.PvPActive);
        }
    }

    public final void killSelf(boolean durabilityLoss) {
        killSelf(durabilityLoss, false);
    }

    public final void killSelf() {
        killSelf(true, false);
    }

    public final void killSelf(boolean durabilityLoss, boolean skipSettingDeathState) {
        kill(this, this, durabilityLoss, skipSettingDeathState);
    }

    public boolean canUseAttackType(WeaponAttackType attacktype) {
        switch (attacktype) {
            case BaseAttack:
                return !hasUnitFlag(UnitFlag.Disarmed);
            case OffAttack:
                return !hasUnitFlag2(UnitFlag2.DisarmOffhand);
            case RangedAttack:
                return !hasUnitFlag2(UnitFlag2.DisarmRanged);
            default:
                return true;
        }
    }

    public final double calculateDamage(WeaponAttackType attType, boolean normalized, boolean addTotalPct) {
        double minDamage;
        double maxDamage;

        if (normalized || !addTotalPct) {
            tangible.OutObject<Double> tempOut_minDamage = new tangible.OutObject<Double>();
            tangible.OutObject<Double> tempOut_maxDamage = new tangible.OutObject<Double>();
            calculateMinMaxDamage(attType, normalized, addTotalPct, tempOut_minDamage, tempOut_maxDamage);
            maxDamage = tempOut_maxDamage.outArgValue;
            minDamage = tempOut_minDamage.outArgValue;

            if (isInFeralForm() && attType == WeaponAttackType.BASE_ATTACK) {
                double minOffhandDamage;
                tangible.OutObject<Double> tempOut_minOffhandDamage = new tangible.OutObject<Double>();
                double maxOffhandDamage;
                tangible.OutObject<Double> tempOut_maxOffhandDamage = new tangible.OutObject<Double>();
                calculateMinMaxDamage(WeaponAttackType.OFF_ATTACK, normalized, addTotalPct, tempOut_minOffhandDamage, tempOut_maxOffhandDamage);
                maxOffhandDamage = tempOut_maxOffhandDamage.outArgValue;
                minOffhandDamage = tempOut_minOffhandDamage.outArgValue;
                minDamage += minOffhandDamage;
                maxDamage += maxOffhandDamage;
            }
        } else {
            switch (attType) {
                case RangedAttack:
                    minDamage = getUnitData().minRangedDamage;
                    maxDamage = getUnitData().maxRangedDamage;

                    break;
                case BaseAttack:
                    minDamage = getUnitData().minDamage;
                    maxDamage = getUnitData().maxDamage;

                    if (isInFeralForm()) {
                        minDamage += getUnitData().minOffHandDamage;
                        maxDamage += getUnitData().maxOffHandDamage;
                    }

                    break;
                case OffAttack:
                    minDamage = getUnitData().minOffHandDamage;
                    maxDamage = getUnitData().maxOffHandDamage;

                    break;
                // Just for good manner
                default:
                    minDamage = 0.0f;
                    maxDamage = 0.0f;

                    break;
            }
        }

        minDamage = Math.max(0.0f, minDamage);
        maxDamage = Math.max(0.0f, maxDamage);

        if (minDamage > maxDamage) {
            tangible.RefObject<T> tempRef_minDamage = new tangible.RefObject<T>(minDamage);
            tangible.RefObject<T> tempRef_maxDamage = new tangible.RefObject<T>(maxDamage);
            Extensions.Swap(tempRef_minDamage, tempRef_maxDamage);
            maxDamage = tempRef_maxDamage.refArgValue;
            minDamage = tempRef_minDamage.refArgValue;
        }

        return RandomUtil.URand(minDamage, maxDamage);
    }

    public final double getWeaponDamageRange(WeaponAttackType attType, WeaponDamageRange type) {
        if (attType == WeaponAttackType.OFF_ATTACK && !haveOffhandWeapon()) {
            return 0.0f;
        }

        return getWeaponDamage()[attType.getValue()][type.getValue()];
    }

    public final double getAPMultiplier(WeaponAttackType attType, boolean normalized) {
        if (!isTypeId(TypeId.PLAYER) || (isInFeralForm() && !normalized)) {
            return getBaseAttackTime(attType) / 1000.0f;
        }

        var weapon = toPlayer().getWeaponForAttack(attType, true);

        if (!weapon) {
            return 2.0f;
        }

        if (!normalized) {
            return weapon.getTemplate().getDelay() / 1000.0f;
        }

        switch (ItemSubClassWeapon.forValue(weapon.getTemplate().getSubClass())) {
            case Axe2:
            case Mace2:
            case Polearm:
            case Sword2:
            case Staff:
            case FishingPole:
                return 3.3f;
            case Axe:
            case Mace:
            case Sword:
            case Warglaives:
            case Exotic:
            case Exotic2:
            case Fist:
                return 2.4f;
            case Dagger:
                return 1.7f;
            case Thrown:
                return 2.0f;
            default:
                return weapon.getTemplate().getDelay() / 1000.0f;
        }
    }

    public final double getTotalAttackPowerValue(WeaponAttackType attType) {
        return getTotalAttackPowerValue(attType, true);
    }

    public final double getTotalAttackPowerValue(WeaponAttackType attType, boolean includeWeapon) {
        if (attType == WeaponAttackType.RangedAttack) {
            double ap = getUnitData().rangedAttackPower + getUnitData().rangedAttackPowerModPos + getUnitData().rangedAttackPowerModNeg;

            if (includeWeapon) {
                ap += Math.max(getUnitData().mainHandWeaponAttackPower, getUnitData().rangedWeaponAttackPower);
            }

            if (ap < 0) {
                return 0.0f;
            }

            return ap * (1.0f + getUnitData().rangedAttackPowerMultiplier);
        } else {
            double ap = getUnitData().attackPower + getUnitData().attackPowerModPos + getUnitData().attackPowerModNeg;

            if (includeWeapon) {
                if (attType == WeaponAttackType.BASE_ATTACK) {
                    ap += Math.max(getUnitData().mainHandWeaponAttackPower, getUnitData().rangedWeaponAttackPower);
                } else {
                    ap += getUnitData().offHandWeaponAttackPower;
                    ap /= 2;
                }
            }

            if (ap < 0) {
                return 0.0f;
            }

            return ap * (1.0f + getUnitData().attackPowerMultiplier);
        }
    }

    public final boolean isWithinMeleeRange(Unit obj) {
        return isWithinMeleeRangeAt(getLocation(), obj);
    }

    public final boolean isWithinMeleeRangeAt(Position pos, Unit obj) {
        if (!obj || !isInMap(obj) || !inSamePhase(obj)) {
            return false;
        }

        var dx = pos.getX() - obj.getLocation().getX();
        var dy = pos.getY() - obj.getLocation().getY();
        var dz = pos.getZ() - obj.getLocation().getZ();
        var distsq = (dx * dx) + (dy * dy) + (dz * dz);

        var maxdist = getMeleeRange(obj) + getTotalAuraModifier(AuraType.ModAutoAttackRange);

        return distsq <= maxdist * maxdist;
    }

    public final float getMeleeRange(Unit target) {
        var range = getCombatReach() + target.getCombatReach() + 4.0f / 3.0f;

        return Math.max(range, SharedConst.NominalMeleeRange);
    }

    public final void setBaseAttackTime(WeaponAttackType att, int val) {
        _baseAttackSpeed[att.getValue()] = val;
        updateAttackTimeField(att);
    }

    public boolean checkAttackFitToAuraRequirement(WeaponAttackType attackType, AuraEffect aurEff) {
        return true;
    }

    public final void applyAttackTimePercentMod(WeaponAttackType att, double val, boolean apply) {
        var remainingTimePct = getAttackTimer()[att.getValue()] / (_baseAttackSpeed[att.getValue()] * getModAttackSpeedPct()[att.getValue()]);

        if (val > 0.0f) {
            tangible.RefObject<Double> tempRef_Object = new tangible.RefObject<Double>(getModAttackSpeedPct()[att.getValue()]);
            MathUtil.applyPercentModFloatVar(tempRef_Object, val, !apply);
            getModAttackSpeedPct()[att.getValue()] = tempRef_Object.refArgValue;

            if (att == WeaponAttackType.BASE_ATTACK) {
                applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modHaste), (float) val, !apply);
            } else if (att == WeaponAttackType.RangedAttack) {
                applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modRangedHaste), (float) val, !apply);
            }
        } else {
            tangible.RefObject<Double> tempRef_Object2 = new tangible.RefObject<Double>(getModAttackSpeedPct()[att.getValue()]);
            MathUtil.applyPercentModFloatVar(tempRef_Object2, -val, apply);
            getModAttackSpeedPct()[att.getValue()] = tempRef_Object2.refArgValue;

            if (att == WeaponAttackType.BASE_ATTACK) {
                applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modHaste), -(float) val, apply);
            } else if (att == WeaponAttackType.RangedAttack) {
                applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modRangedHaste), -(float) val, apply);
            }
        }

        updateAttackTimeField(att);
        getAttackTimer()[att.getValue()] = (int) (_baseAttackSpeed[att.getValue()] * getModAttackSpeedPct()[att.getValue()] * remainingTimePct);
    }

    /**
     * enables / disables combat interaction of this unit
     */
    public final void setIsCombatDisallowed(boolean apply) {
        isCombatDisallowed = apply;
    }

    private void addAttacker(Unit pAttacker) {
        getAttackerList().add(pAttacker);
    }

    private void removeAttacker(Unit pAttacker) {
        getAttackerList().remove(pAttacker);
    }

    // TODO for melee need create structure as in
    private void calculateMeleeDamage(Unit victim, tangible.OutObject<CalcDamageInfo> damageInfo, WeaponAttackType attackType) {
        damageInfo.outArgValue = new CalcDamageInfo();

        damageInfo.outArgValue.setAttacker(this);
        damageInfo.outArgValue.setTarget(victim);

        damageInfo.outArgValue.setDamageSchoolMask((int) spellSchoolMask.NORMAL.getValue());
        damageInfo.outArgValue.damage = 0;
        damageInfo.outArgValue.setOriginalDamage(0);
        damageInfo.outArgValue.absorb = 0;
        damageInfo.outArgValue.setResist(0);

        damageInfo.outArgValue.setBlocked(0);
        damageInfo.outArgValue.setHitInfo(hitInfo.forValue(0));
        damageInfo.outArgValue.setTargetState(victimState.forValue(0));

        damageInfo.outArgValue.setAttackType(attackType);
        damageInfo.outArgValue.setProcAttacker(new EnumFlag<ProcFlag>());
        damageInfo.outArgValue.setProcVictim(new EnumFlag<ProcFlag>());
        damageInfo.outArgValue.setCleanDamage(0);
        damageInfo.outArgValue.setHitOutCome(MeleeHitOutcome.Evade);

        if (victim == null) {
            return;
        }

        if (!isAlive() || !victim.isAlive()) {
            return;
        }

        // Select HitInfo/procAttacker/procVictim flag based on attack type
        switch (attackType) {
            case BaseAttack:
                damageInfo.outArgValue.setProcAttacker(new EnumFlag<ProcFlag>(procFlags.DealMeleeSwing.getValue() | procFlags.MainHandWeaponSwing.getValue()));
                damageInfo.outArgValue.setProcVictim(new EnumFlag<ProcFlag>(procFlags.TakeMeleeSwing));

                break;
            case OffAttack:
                damageInfo.outArgValue.setProcAttacker(new EnumFlag<ProcFlag>(procFlags.DealMeleeSwing.getValue() | procFlags.OffHandWeaponSwing.getValue()));
                damageInfo.outArgValue.setProcVictim(new EnumFlag<ProcFlag>(procFlags.TakeMeleeSwing));
                damageInfo.outArgValue.setHitInfo(hitInfo.OffHand);

                break;
            default:
                return;
        }

        // Physical Immune check
        if (damageInfo.outArgValue.getTarget().isImmunedToDamage(spellSchoolMask.forValue(damageInfo.outArgValue.getDamageSchoolMask()))) {
            damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | hitInfo.NormalSwing.getValue()));
            damageInfo.outArgValue.setTargetState(victimState.Immune);

            damageInfo.outArgValue.damage = 0;
            damageInfo.outArgValue.setCleanDamage(0);

            return;
        }

        double damage = 0;
        damage += calculateDamage(damageInfo.outArgValue.getAttackType(), false, true);
        // Add melee damage bonus
        damage = meleeDamageBonusDone(damageInfo.outArgValue.getTarget(), damage, damageInfo.outArgValue.getAttackType(), DamageEffectType.Direct, null, null, spellSchoolMask.forValue(damageInfo.outArgValue.getDamageSchoolMask()));
        damage = damageInfo.outArgValue.getTarget().meleeDamageBonusTaken(this, damage, damageInfo.outArgValue.getAttackType(), DamageEffectType.Direct, null, spellSchoolMask.forValue(damageInfo.outArgValue.getDamageSchoolMask()));

        // Script Hook For CalculateMeleeDamage -- Allow scripts to change the Damage pre class mitigation calculations
        var t = damageInfo.outArgValue.getTarget();
        var a = damageInfo.outArgValue.getAttacker();
        tangible.RefObject<Double> tempRef_damage = new tangible.RefObject<Double>(damage);
        scaleDamage(a, t, tempRef_damage);
        damage = tempRef_damage.refArgValue;

        tangible.RefObject<Double> tempRef_damage2 = new tangible.RefObject<Double>(damage);
        global.getScriptMgr().<IUnitModifyMeleeDamage>ForEach(p -> p.ModifyMeleeDamage(t, a, tempRef_damage2));
        damage = tempRef_damage2.refArgValue;

        // Calculate armor reduction
        if (isDamageReducedByArmor(spellSchoolMask.forValue(damageInfo.outArgValue.getDamageSchoolMask()))) {
            damageInfo.outArgValue.damage = calcArmorReducedDamage(damageInfo.outArgValue.getAttacker(), damageInfo.outArgValue.getTarget(), damage, null, damageInfo.outArgValue.getAttackType());
            damageInfo.outArgValue.setCleanDamage(damageInfo.outArgValue.getCleanDamage() + damage - damageInfo.outArgValue.damage);
        } else {
            damageInfo.outArgValue.damage = damage;
        }

        damageInfo.outArgValue.setHitOutCome(rollMeleeOutcomeAgainst(damageInfo.outArgValue.getTarget(), damageInfo.outArgValue.getAttackType()));

        switch (damageInfo.outArgValue.getHitOutCome()) {
            case Evade:
                damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | hitInfo.Miss.getValue() | hitInfo.SwingNoHitSound.getValue()));
                damageInfo.outArgValue.setTargetState(victimState.Evades);
                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);

                damageInfo.outArgValue.damage = 0;
                damageInfo.outArgValue.setCleanDamage(0);

                return;
            case Miss:
                damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | hitInfo.Miss.getValue()));
                damageInfo.outArgValue.setTargetState(victimState.Intact);
                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);

                damageInfo.outArgValue.damage = 0;
                damageInfo.outArgValue.setCleanDamage(0);

                break;
            case Normal:
                damageInfo.outArgValue.setTargetState(victimState.hit);
                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);

                break;
            case Crit:
                damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | hitInfo.CriticalHit.getValue()));
                damageInfo.outArgValue.setTargetState(victimState.hit);
                // Crit bonus calc
                damageInfo.outArgValue.Damage *= 2;

                // Increase crit damage from SPELL_AURA_MOD_CRIT_DAMAGE_BONUS
                var mod = (getTotalAuraMultiplierByMiscMask(AuraType.ModCritDamageBonus, damageInfo.outArgValue.getDamageSchoolMask()) - 1.0f) * 100;

                if (mod != 0) {
                    tangible.RefObject<Double> tempRef_Damage = new tangible.RefObject<Double>(damageInfo.outArgValue.damage);
                    MathUtil.AddPct(tempRef_Damage, mod);
                    damageInfo.outArgValue.outArgValue.damage = tempRef_Damage.refArgValue;
                }

                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);

                break;
            case Parry:
                damageInfo.outArgValue.setTargetState(victimState.Parry);
                damageInfo.outArgValue.setCleanDamage(damageInfo.outArgValue.getCleanDamage() + damageInfo.outArgValue.damage);

                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);
                damageInfo.outArgValue.damage = 0;

                break;
            case Dodge:
                damageInfo.outArgValue.setTargetState(victimState.Dodge);
                damageInfo.outArgValue.setCleanDamage(damageInfo.outArgValue.getCleanDamage() + damageInfo.outArgValue.damage);

                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);
                damageInfo.outArgValue.damage = 0;

                break;
            case Block:
                damageInfo.outArgValue.setTargetState(victimState.hit);
                damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | hitInfo.Block.getValue()));
                // 30% damage blocked, double blocked amount if block is critical
                damageInfo.outArgValue.setBlocked(MathUtil.CalculatePct(damageInfo.outArgValue.damage, damageInfo.outArgValue.getTarget().getBlockPercent(getLevel())));

                if (damageInfo.outArgValue.getTarget().isBlockCritical()) {
                    damageInfo.outArgValue.setBlocked(damageInfo.outArgValue.getBlocked() * 2);
                }

                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);
                damageInfo.outArgValue.Damage -= damageInfo.outArgValue.getBlocked();
                damageInfo.outArgValue.setCleanDamage(damageInfo.outArgValue.getCleanDamage() + damageInfo.outArgValue.getBlocked());

                break;
            case Glancing:
                damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | hitInfo.Glancing.getValue()));
                damageInfo.outArgValue.setTargetState(victimState.hit);
                var leveldif = (int) victim.getLevel() - (int) getLevel();

                if (leveldif > 3) {
                    leveldif = 3;
                }

                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);
                var reducePercent = 1.0f - leveldif * 0.1f;
                damageInfo.outArgValue.setCleanDamage(damageInfo.outArgValue.getCleanDamage() + damageInfo.outArgValue.Damage - (reducePercent * damageInfo.outArgValue.damage));
                damageInfo.outArgValue.damage = reducePercent * damageInfo.outArgValue.damage;

                break;
            case Crushing:
                damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | hitInfo.Crushing.getValue()));
                damageInfo.outArgValue.setTargetState(victimState.hit);
                // 150% normal damage
                damageInfo.outArgValue.damage += (damageInfo.outArgValue.Damage / 2);
                damageInfo.outArgValue.setOriginalDamage(damageInfo.outArgValue.damage);

                break;

            default:
                break;
        }

        // Always apply HITINFO_AFFECTS_VICTIM in case its not a miss
        if (!damageInfo.outArgValue.getHitInfo().hasFlag(hitInfo.Miss)) {
            damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | hitInfo.AffectsVictim.getValue()));
        }

        var resilienceReduction = damageInfo.outArgValue.damage;

        if (canApplyResilience()) {
            tangible.RefObject<Double> tempRef_resilienceReduction = new tangible.RefObject<Double>(resilienceReduction);
            applyResilience(victim, tempRef_resilienceReduction);
            resilienceReduction = tempRef_resilienceReduction.refArgValue;
        }

        resilienceReduction = damageInfo.outArgValue.Damage - resilienceReduction;
        damageInfo.outArgValue.Damage -= resilienceReduction;
        damageInfo.outArgValue.setCleanDamage(damageInfo.outArgValue.getCleanDamage() + resilienceReduction);

        // Calculate absorb resist
        if (damageInfo.outArgValue.damage > 0) {
            damageInfo.outArgValue.getProcVictim().Or(procFlags.TakeAnyDamage);
            // Calculate absorb & resists
            DamageInfo dmgInfo = new DamageInfo(damageInfo.outArgValue);
            calcAbsorbResist(dmgInfo);
            damageInfo.outArgValue.absorb = dmgInfo.getAbsorb();
            damageInfo.outArgValue.setResist(dmgInfo.getResist());

            if (damageInfo.outArgValue.absorb != 0) {
                damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | (damageInfo.outArgValue.Damage - damageInfo.outArgValue.absorb == 0 ? hitInfo.FullAbsorb : hitInfo.PartialAbsorb).getValue()));
            }

            if (damageInfo.outArgValue.getResist() != 0) {
                damageInfo.outArgValue.setHitInfo(hitInfo.forValue(damageInfo.outArgValue.getHitInfo().getValue() | (damageInfo.outArgValue.Damage - damageInfo.outArgValue.getResist() == 0 ? hitInfo.FullResist : hitInfo.PartialResist).getValue()));
            }

            damageInfo.outArgValue.damage = dmgInfo.getDamage();
        } else // Impossible get negative result but....
        {
            damageInfo.outArgValue.damage = 0;
        }
    }

    private MeleeHitOutcome rollMeleeOutcomeAgainst(Unit victim, WeaponAttackType attType) {
        if (victim.isTypeId(TypeId.UNIT) && victim.toCreature().isEvadingAttacks()) {
            return MeleeHitOutcome.Evade;
        }

        // Miss chance based on melee
        var miss_chance = (int) (meleeSpellMissChance(victim, attType, null) * 100.0f);

        // Critical hit chance
        var crit_chance = (int) ((getUnitCriticalChanceAgainst(attType, victim) + getTotalAuraModifier(AuraType.ModAutoAttackCritChance)) * 100.0f);

        var dodge_chance = (int) (getUnitDodgeChance(attType, victim) * 100.0f);
        var block_chance = (int) (getUnitBlockChance(attType, victim) * 100.0f);
        var parry_chance = (int) (getUnitParryChance(attType, victim) * 100.0f);

        // melee attack table implementation
        // outcome priority:
        //   1. >    2. >    3. >       4. >    5. >   6. >       7. >  8.
        // MISS > DODGE > PARRY > GLANCING > BLOCK > CRIT > CRUSHING > HIT

        var sum = 0;
        var roll = RandomUtil.randomInt(0, 10000);

        var attackerLevel = getLevelForTarget(victim);
        var victimLevel = getLevelForTarget(this);

        // check if attack comes from behind, nobody can parry or block if attacker is behind
        var canParryOrBlock = victim.getLocation().hasInArc((float) Math.PI, getLocation()) || victim.hasAuraType(AuraType.IgnoreHitDirection);

        // only creatures can dodge if attacker is behind
        var canDodge = !victim.isTypeId(TypeId.PLAYER) || canParryOrBlock;

        // if victim is casting or cc'd it can't avoid attacks
        if (victim.isNonMeleeSpellCast(false, false, true) || victim.hasUnitState(UnitState.controlled)) {
            canDodge = false;
            canParryOrBlock = false;
        }

        // 1. MISS
        var tmp = miss_chance;

        if (tmp > 0 && roll < (sum += tmp)) {
            return MeleeHitOutcome.Miss;
        }

        // always crit against a sitting target (except 0 crit chance)
        if (victim.isTypeId(TypeId.PLAYER) && crit_chance > 0 && !victim.isStandState()) {
            return MeleeHitOutcome.crit;
        }

        // 2. DODGE
        if (canDodge) {
            tmp = dodge_chance;

            if (tmp > 0 && roll < (sum += tmp)) {
                return MeleeHitOutcome.Dodge;
            }
        }

        // 3. PARRY
        if (canParryOrBlock) {
            tmp = parry_chance;

            if (tmp > 0 && roll < (sum += tmp)) {
                return MeleeHitOutcome.Parry;
            }
        }

        // 4. GLANCING
        // Max 40% chance to score a glancing blow against mobs that are higher level (can do only players and pets and not with ranged weapon)
        if ((isTypeId(TypeId.PLAYER) || isPet()) && !victim.isTypeId(TypeId.PLAYER) && !victim.isPet() && attackerLevel + 3 < victimLevel) {
            // cap possible value (with bonuses > max skill)
            tmp = (int) (10 + 10 * (victimLevel - attackerLevel)) * 100;

            if (tmp > 0 && roll < (sum += tmp)) {
                return MeleeHitOutcome.Glancing;
            }
        }

        // 5. BLOCK
        if (canParryOrBlock) {
            tmp = block_chance;

            if (tmp > 0 && roll < (sum += tmp)) {
                return MeleeHitOutcome.Block;
            }
        }

        // 6.CRIT
        tmp = crit_chance;

        if (tmp > 0 && roll < (sum += tmp)) {
            return MeleeHitOutcome.crit;
        }

        // 7. CRUSHING
        // mobs can score crushing blows if they're 4 or more levels above victim
        if (attackerLevel >= victimLevel + 4 && !isControlledByPlayer() && !(getObjectTypeId() == TypeId.UNIT && toCreature().getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.NoCrushingBlows))) {
            // add 2% chance per level, min. is 15%
            tmp = (int) (attackerLevel - victimLevel * 1000 - 1500);

            if (roll < (sum += tmp)) {
                Log.outDebug(LogFilter.unit, "RollMeleeOutcomeAgainst: CRUSHING <{0}, {1})", sum - tmp, sum);

                return MeleeHitOutcome.Crushing;
            }
        }

        // 8. HIT
        return MeleeHitOutcome.NORMAL;
    }

    private void updateAttackTimeField(WeaponAttackType att) {
        switch (att) {
            case BaseAttack:
            case OffAttack:

                setUpdateFieldValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().attackRoundBaseTime, att.getValue()), (int) (_baseAttackSpeed[att.getValue()] * getModAttackSpeedPct()[att.getValue()]));

                break;
            case RangedAttack:
                setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().rangedAttackRoundBaseTime), (int) (_baseAttackSpeed[att.getValue()] * getModAttackSpeedPct()[att.getValue()]));

                break;
            default:
                break;

            ;
        }
    }

    public final boolean isInDisallowedMountForm() {
        return isDisallowedMountForm(getTransformSpell(), getShapeshiftForm(), getDisplayId());
    }

    public boolean isLoading() {
        return false;
    }

    public final boolean isDuringRemoveFromWorld() {
        return duringRemoveFromWorld;
    }

    //SharedVision
    public final boolean getHasSharedVision() {
        return !sharedVision.isEmpty();
    }


    public final EnumFlag<NPCFlag> getNpcFlags() { return EnumFlag.of(getInt32Value(UNIT_NPC_FLAGS)); }
    public final boolean hasNpcFlag(NPCFlag flags) { return hasFlag(UNIT_NPC_FLAGS, flags); }
    void SetNpcFlag(NPCFlags flags) { SetUInt32Value(UNIT_NPC_FLAGS, flags); }
    void RemoveNpcFlag(NPCFlags flags) { RemoveFlag(UNIT_NPC_FLAGS, flags); }
    void ReplaceAllNpcFlags(NPCFlags flags) { ToggleFlag(UNIT_NPC_FLAGS, flags); }

    NPCFlags2 GetNpcFlags2() const { return NPCFlags2(GetUInt32Value(UNIT_NPC_FLAGS + 1)); }
    bool HasNpcFlag2(NPCFlags2 flags) const { return HasFlag(UNIT_NPC_FLAGS + 1, flags); }
    void SetNpcFlag2(NPCFlags2 flags) { SetUInt32Value(UNIT_NPC_FLAGS + 1, flags); }
    void RemoveNpcFlag2(NPCFlags2 flags) { RemoveFlag(UNIT_NPC_FLAGS + 1, flags); }
    void ReplaceAllNpcFlags2(NPCFlags2 flags) { ToggleFlag(UNIT_NPC_FLAGS + 1, flags); }

    boolean IsVendor()          { return HasNpcFlag(UNIT_NPC_FLAG_VENDOR); }
    boolean IsTrainer()         { return HasNpcFlag(UNIT_NPC_FLAG_TRAINER); }
    bool IsQuestGiver()     const { return HasNpcFlag(UNIT_NPC_FLAG_QUESTGIVER); }
    bool IsGossip()         const { return HasNpcFlag(UNIT_NPC_FLAG_GOSSIP); }
    bool IsTaxi()           const { return HasNpcFlag(UNIT_NPC_FLAG_FLIGHTMASTER); }
    bool IsGuildMaster()    const { return HasNpcFlag(UNIT_NPC_FLAG_PETITIONER); }
    bool IsBattleMaster()   const { return HasNpcFlag(UNIT_NPC_FLAG_BATTLEMASTER); }
    bool IsBanker()         const { return HasNpcFlag(UNIT_NPC_FLAG_BANKER); }
    bool IsInnkeeper()      const { return HasNpcFlag(UNIT_NPC_FLAG_INNKEEPER); }
    bool IsSpiritHealer()   const { return HasNpcFlag(UNIT_NPC_FLAG_SPIRIT_HEALER); }
    bool IsAreaSpiritHealer() const { return HasNpcFlag(UNIT_NPC_FLAG_AREA_SPIRIT_HEALER); }
    bool IsTabardDesigner() const { return HasNpcFlag(UNIT_NPC_FLAG_TABARDDESIGNER); }
    bool IsAuctioner()      const { return HasNpcFlag(UNIT_NPC_FLAG_AUCTIONEER); }
    bool IsArmorer()        const { return HasNpcFlag(UNIT_NPC_FLAG_REPAIR); }
    bool IsWildBattlePet()  const { return HasNpcFlag(UNIT_NPC_FLAG_WILD_BATTLE_PET); }
    bool IsServiceProvider() const;
    bool IsSpiritService() const { return HasNpcFlag(UNIT_NPC_FLAG_SPIRIT_HEALER | UNIT_NPC_FLAG_AREA_SPIRIT_HEALER); }
    bool IsAreaSpiritHealerIndividual() const { return HasNpcFlag2(UNIT_NPC_FLAG_2_AREA_SPIRIT_HEALER_INDIVIDUAL); }
    bool IsCritter() const { return GetCreatureType() == CREATURE_TYPE_CRITTER; }

    bool IsInFlight()  const { return HasUnitState(UNIT_STATE_IN_FLIGHT); }


    public final NPCFlag getNpcFlags() {
        return NPCFlag.forValue(getUnitData().npcFlags.get(0));
    }

    public final NPCFlags2 getNpcFlags2() {
        return NPCFlags2.forValue(getUnitData().npcFlags.get(1));
    }

    public final boolean isVendor() {
        return hasNpcFlag(NPCFlag.vendor);
    }

    public final boolean isTrainer() {
        return hasNpcFlag(NPCFlag.Trainer);
    }

    public final boolean isQuestGiver() {
        return hasNpcFlag(NPCFlag.questGiver);
    }

    public final boolean isGossip() {
        return hasNpcFlag(NPCFlag.Gossip);
    }

    public final boolean isTaxi() {
        return hasNpcFlag(NPCFlag.FlightMaster);
    }

    public final boolean isGuildMaster() {
        return hasNpcFlag(NPCFlag.petitioner);
    }

    public final boolean isBattleMaster() {
        return hasNpcFlag(NPCFlag.BattleMaster);
    }

    public final boolean isBanker() {
        return hasNpcFlag(NPCFlag.banker);
    }

    public final boolean isInnkeeper() {
        return hasNpcFlag(NPCFlag.Innkeeper);
    }

    public final boolean isSpiritHealer() {
        return hasNpcFlag(NPCFlag.SpiritHealer);
    }

    public final boolean isSpiritGuide() {
        return hasNpcFlag(NPCFlag.SpiritGuide);
    }

    public final boolean isTabardDesigner() {
        return hasNpcFlag(NPCFlag.TabardDesigner);
    }

    public final boolean isAuctioner() {
        return hasNpcFlag(NPCFlag.auctioneer);
    }

    public final boolean isArmorer() {
        return hasNpcFlag(NPCFlag.Repair);
    }

    public final boolean isWildBattlePet() {
        return hasNpcFlag(NPCFlag.WildBattlePet);
    }

    public final boolean isServiceProvider() {
        return hasNpcFlag(NPCFlag.vendor.getValue() | NPCFlag.Trainer.getValue().getValue() | NPCFlag.FlightMaster.getValue().getValue().getValue() | NPCFlag.petitioner.getValue().getValue().getValue().getValue() | NPCFlag.BattleMaster.getValue().getValue().getValue().getValue().getValue() | NPCFlag.banker.getValue().getValue().getValue().getValue().getValue().getValue() | NPCFlag.Innkeeper.getValue().getValue().getValue().getValue().getValue().getValue().getValue() | NPCFlag.SpiritHealer.getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue() | NPCFlag.SpiritGuide.getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue() | NPCFlag.TabardDesigner.getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue() | NPCFlag.auctioneer.getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue().getValue());
    }

    public final boolean isSpiritService() {
        return hasNpcFlag(NPCFlag.SpiritHealer.getValue() | NPCFlag.SpiritGuide.getValue());
    }

    public final boolean isCritter() {
        return getCreatureType() == creatureType.critter;
    }

    public final boolean isInFlight() {
        return hasUnitState(UnitState.InFlight);
    }

    @Override
    public float getCollisionHeight() {
        var scaleMod = getObjectScale(); // 99% sure about this

        if (isMounted()) {
            var mountDisplayInfo = CliDB.CreatureDisplayInfoStorage.get(getMountDisplayId());

            if (mountDisplayInfo != null) {
                var mountModelData = CliDB.CreatureModelDataStorage.get(mountDisplayInfo.modelID);

                if (mountModelData != null) {
                    var displayInfo = CliDB.CreatureDisplayInfoStorage.get(getNativeDisplayId());
                    var modelData = CliDB.CreatureModelDataStorage.get(displayInfo.modelID);
                    var collisionHeight = scaleMod * ((mountModelData.MountHeight * mountDisplayInfo.CreatureModelScale) + (modelData.CollisionHeight * modelData.ModelScale * displayInfo.CreatureModelScale * 0.5f));

                    return collisionHeight == 0.0f ? MapDefine.DEFAULT_COLLISION_HEIGHT : collisionHeight;
                }
            }
        }

        //! Dismounting case - use basic default model data
        var defaultDisplayInfo = CliDB.CreatureDisplayInfoStorage.get(getNativeDisplayId());
        var defaultModelData = CliDB.CreatureModelDataStorage.get(defaultDisplayInfo.modelID);

        var collisionHeight1 = scaleMod * defaultModelData.CollisionHeight * defaultModelData.ModelScale * defaultDisplayInfo.CreatureModelScale;

        return collisionHeight1 == 0.0f ? MapDefine.DEFAULT_COLLISION_HEIGHT : collisionHeight1;
    }

    public final boolean isAIEnabled() {
        return getAi() != null;
    }

    public final boolean isPossessedByPlayer() {
        return hasUnitState(UnitState.Possessed) && getCharmerGUID().isPlayer();
    }

    public final boolean isPossessing() {
        var u = getCharmed();

        if (u != null) {
            return u.isPossessed();
        } else {
            return false;
        }
    }

    public final boolean isCharmed() {
        return !getCharmerGUID().isEmpty();
    }

    public final boolean isPossessed() {
        return hasUnitState(UnitState.Possessed);
    }

    public final boolean isMagnet() {
        // Grounding Totem
        if (getUnitData().createdBySpell == 8177) /// @todo: find a more generic solution
        {
            return true;
        }

        return false;
    }

    public final boolean isInFeralForm() {
        var form = getShapeshiftForm();

        return form == ShapeShiftForm.catForm || form == ShapeShiftForm.BearForm || form == ShapeShiftForm.DireBearForm || form == ShapeShiftForm.GhostWolf;
    }

    public final boolean isControlledByPlayer() {
        return getControlledByPlayer();
    }

    public final boolean isCharmedOwnedByPlayerOrPlayer() {
        return getCharmerOrOwnerOrOwnGUID().isPlayer();
    }

    public final int getCreatureTypeMask() {
        var creatureType = (int) getCreatureType().getValue();

        return (int) (creatureType >= 1 ? (1 << (int) (creatureType - 1)) : 0);
    }

    public final MotionMaster getMotionMaster() {
        return motionMaster;
    }

    @Override
    public short getAIAnimKitId() {
        return aiAnimKitId;
    }

    public final void setAIAnimKitId(short animKitId) {
        if (aiAnimKitId == animKitId) {
            return;
        }

        if (animKitId != 0 && !CliDB.AnimKitStorage.containsKey(animKitId)) {
            return;
        }

        aiAnimKitId = animKitId;

        SetAIAnimKit data = new SetAIAnimKit();
        data.unit = getGUID();
        data.animKitID = animKitId;
        sendMessageToSet(data, true);
    }

    @Override
    public short getMovementAnimKitId() {
        return movementAnimKitId;
    }

    public final void setMovementAnimKitId(short animKitId) {
        if (movementAnimKitId == animKitId) {
            return;
        }

        if (animKitId != 0 && !CliDB.AnimKitStorage.containsKey(animKitId)) {
            return;
        }

        movementAnimKitId = animKitId;

        SetMovementAnimKit data = new SetMovementAnimKit();
        data.unit = getGUID();
        data.animKitID = animKitId;
        sendMessageToSet(data, true);
    }

    @Override
    public short getMeleeAnimKitId() {
        return meleeAnimKitId;
    }

    public final void setMeleeAnimKitId(short animKitId) {
        if (meleeAnimKitId == animKitId) {
            return;
        }


        if (animKitId != 0 && !worldContext.getDbcObjectManager().animKit().contains(animKitId)) {
            return;
        }

        meleeAnimKitId = animKitId;

        SetMeleeAnimKit data = new SetMeleeAnimKit();
        data.unit = getGUID();
        data.animKitID = animKitId;
        sendMessageToSet(data, true);
    }

    public final int getLevel() {
        return getUnitData().level;
    }

    public final void setLevel(int lvl) {
        setLevel(lvl, true);
    }

    public final Race getRace() {
        return Race.values()[unitData.getRace()];
    }

    public final void setRace(Race value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().race), (byte) value.getValue());
    }

    public final UnitClass getUnitClass() {
        return UnitClass.values()[unitData.getClassId()];
    }

    public final void setUnitClass(UnitClass value) {
        unitData.setClassId((byte) value.ordinal());
    }

    public final int getClassMask() {
        return (1 << (getUnitClass().ordinal() - 1));
    }

    public final Gender getGender() {
        return Gender.valueOf(unitData.getSex());
    }

    public final void setGender(Gender value) {
        unitData.setSex((byte) value.value);
    }

    public Gender getNativeGender() {
        return getGender();
    }

    public void setNativeGender(Gender value) {
        setGender(value);
    }

    public float getNativeObjectScale() {
        return 1.0f;
    }

    public final int getDisplayId() {
        return getUnitData().displayID;
    }

    public void setDisplayId(int modelId) {
        setDisplayId(modelId, 1f);
    }

    public final int getNativeDisplayId() {
        return getUnitData().nativeDisplayID;
    }

    public final void setNativeDisplayId(int displayId) {
        setNativeDisplayId(displayId, false);
    }

    public final float getNativeDisplayScale() {
        return getUnitData().nativeXDisplayScale;
    }

    public final boolean isMounted() {
        return hasUnitFlag(UnitFlag.mount);
    }

    public final int getMountDisplayId() {
        return getUnitData().mountDisplayID;
    }

    public final void setMountDisplayId(int value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().mountDisplayID), value);
    }

    public float getFollowAngle() {
        return MathUtil.PI_OVER_2;
    }

    @Override
    public ObjectGuid getOwnerGUID() {
        return getUnitData().summonedBy;
    }

    public final void setOwnerGUID(ObjectGuid owner) {
        if (Objects.equals(getOwnerGUID(), owner)) {
            return;
        }

        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().summonedBy), owner);

        if (owner.isEmpty()) {
            return;
        }

        // Update owner dependent fields
        var player = global.getObjAccessor().getPlayer(this, owner);

        if (player == null || !player.haveAtClient(this)) // if player cannot see this unit yet, he will receive needed data with create object
        {
            return;
        }

        UpdateData udata = new UpdateData(getLocation().getMapId());
        buildValuesUpdateBlockForPlayerWithFlag(udata, UpdateFieldFlag.owner, player);
        UpdateObject packet;
        tangible.OutObject<UpdateObject> tempOut_packet = new tangible.OutObject<UpdateObject>();
        udata.buildPacket(tempOut_packet);
        packet = tempOut_packet.outArgValue;
        player.sendPacket(packet);
    }

    public final ObjectGuid getCreatorGUID() {
        return getUnitData().createdBy;
    }

    public final void setCreatorGUID(ObjectGuid creator) {
        setGuidValue(UNIT_FIELD_CREATEDBY, creator);
    }

    public final ObjectGuid getMinionGUID() {
        return getUnitData().summon;
    }

    public final void setMinionGUID(ObjectGuid value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().summon), value);
    }

    public final ObjectGuid getPetGUID() {
        return getSummonSlot()[0];
    }

    public final void setPetGUID(ObjectGuid value) {
        getSummonSlot()[0] = value;
    }

    public final ObjectGuid getCritterGUID() {
        return getUnitData().critter;
    }

    public final void setCritterGUID(ObjectGuid value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().critter), value);
    }

    public final ObjectGuid getBattlePetCompanionGUID() {
        return getUnitData().battlePetCompanionGUID;
    }

    public final void setBattlePetCompanionGUID(ObjectGuid value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().battlePetCompanionGUID), value);
    }

    public final ObjectGuid getDemonCreatorGUID() {
        return getUnitData().demonCreator;
    }

    public final void setDemonCreatorGUID(ObjectGuid value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().demonCreator), value);
    }

    public final ObjectGuid getCharmerGUID() {
        return getUnitData().charmedBy;
    }

    public final Unit getCharmer() {
        return charmer;
    }

    public final ObjectGuid getCharmedGUID() {
        return getUnitData().charm;
    }

    public final Unit getCharmed() {
        return charmed;
    }

    @Override
    public ObjectGuid getCharmerOrOwnerGUID() {
        return isCharmed() ? getCharmerGUID() : getOwnerGUID();
    }

    @Override
    public Unit getCharmerOrOwner() {
        return isCharmed() ? getCharmer() : getOwnerUnit();
    }

    public final int getBattlePetCompanionNameTimestamp() {
        return getUnitData().battlePetCompanionNameTimestamp;
    }

    public final void setBattlePetCompanionNameTimestamp(int value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().battlePetCompanionNameTimestamp), value);
    }

    public final int getBattlePetCompanionExperience() {
        return getUnitData().battlePetCompanionExperience;
    }

    public final void setBattlePetCompanionExperience(int value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().battlePetCompanionExperience), value);
    }

    public final int getWildBattlePetLevel() {
        return getUnitData().wildBattlePetLevel;
    }

    public final void setWildBattlePetLevel(int value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().wildBattlePetLevel), value);
    }

    public final UnitDynFlags getDynamicFlags() {
        return UnitDynFlag.forValue((int) getObjectData().dynamicFlags);
    }

    public final Emote getEmoteState() {
        return emote.forValue((int) getUnitData().emoteState);
    }

    public final void setEmoteState(Emote value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().emoteState), value.getValue());
    }

    public final SheathState getSheath() {
        return sheathState.forValue((byte) getUnitData().sheatheState);
    }

    public final void setSheath(SheathState value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().sheatheState), (byte) value.getValue());

        if (value == sheathState.Unarmed) {
            removeAurasWithInterruptFlags(SpellAuraInterruptFlag.Sheathing);
        }
    }

    public final UnitPVPStateFlag getPvpFlags() {
        return UnitPVPStateFlag.forValue((byte) getUnitData().pvpFlags);
    }

    public final boolean isInSanctuary() {
        return hasPvpFlag(UnitPVPStateFlag.Sanctuary);
    }

    public final boolean isPvP() {
        return hasPvpFlag(UnitPVPStateFlag.pvP);
    }

    public void setPvP(boolean state) {
        if (state) {
            setPvpFlag(UnitPVPStateFlag.pvP);
        } else {
            removePvpFlag(UnitPVPStateFlag.pvP);
        }
    }

    public final boolean isFFAPvP() {
        return hasPvpFlag(UnitPVPStateFlag.FFAPvp);
    }

    @Override
    public float getObjectScale() {
        return super.getObjectScale();
    }

    @Override
    public void setObjectScale(float value) {
        var minfo = global.getObjectMgr().getCreatureModelInfo(getDisplayId());

        if (minfo != null) {
            setBoundingRadius((isPet() ? 1.0f : minfo.boundingRadius) * getObjectScale());
            setCombatReach((isPet() ? ObjectDefine.DEFAULT_PLAYER_COMBAT_REACH : minfo.combatReach) * getObjectScale());
        }

        super.setObjectScale(value);
    }

    public final UnitPetFlags getPetFlags() {
        return UnitPetFlags.forValue((byte) getUnitData().petFlags);
    }

    public final ShapeShiftForm getShapeshiftForm() {
        return ShapeShiftForm.forValue((byte) getUnitData().shapeshiftForm);
    }

    public final void setShapeshiftForm(ShapeShiftForm value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().shapeshiftForm), (byte) value.getValue());
    }

    public final CreatureType getCreatureType() {
        if (isTypeId(TypeId.PLAYER)) {
            var form = getShapeshiftForm();
            var ssEntry = CliDB.SpellShapeshiftFormStorage.get((int) form.getValue());

            if (ssEntry != null && ssEntry.creatureType > 0) {
                return creatureType.forValue(ssEntry.creatureType);
            } else {
                var raceEntry = CliDB.ChrRacesStorage.get(getRace());

                return creatureType.forValue(raceEntry.creatureType);
            }
        } else {
            return toCreature().getCreatureTemplate().type;
        }
    }

    public final boolean isAlive() {
        return deathState == DeathState.ALIVE;
    }

    public final boolean isDying() {
        return deathState == DeathState.JUST_DIED;
    }

    public final boolean isDead() {
        return (deathState == DeathState.DEAD || deathState == DeathState.CORPSE);
    }

    public final boolean isSummon() {
        return getUnitTypeMask().hasFlag(UnitTypeMask.SUMMON);
    }

    public final boolean isGuardian() {
        return getUnitTypeMask().hasFlag(UnitTypeMask.GUARDIAN);
    }

    public final boolean isPet() {
        return getUnitTypeMask().hasFlag(UnitTypeMask.PET);
    }

    public final boolean isHunterPet() {
        return getUnitTypeMask().hasFlag(UnitTypeMask.HUNTER_PET);
    }

    public final boolean isTotem() {
        return getUnitTypeMask().hasFlag(UnitTypeMask.TOTEM);
    }

    public final boolean isVehicle() {
        return getUnitTypeMask().hasFlag(UnitTypeMask.VEHICLE);
    }

    @Override
    public int getFaction() {
        return getUnitData().factionTemplate;
    }

    @Override
    public void setFaction(int value) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().factionTemplate), value);
    }

    public final UnitStandStateType getStandState() {
        return UnitStandStateType.forValue((byte) getUnitData().standState);
    }

    public final boolean isSitState() {
        var s = getStandState();

        return s == UnitStandStateType.SitChair || s == UnitStandStateType.SitLowChair || s == UnitStandStateType.SitMediumChair || s == UnitStandStateType.SitHighChair || s == UnitStandStateType.Sit;
    }

    public final boolean isStandState() {
        var s = getStandState();

        return !isSitState() && s != UnitStandStateType.Sleep && s != UnitStandStateType.Kneel;
    }

    public final void setStandState(UnitStandStateType state) {
        setStandState(state, 0);
    }

    public final AnimTier getAnimTier() {
        return animTier.forValue((byte) getUnitData().animTier);
    }

    public final void setAnimTier(AnimTier animTier) {
        setAnimTier(animTier, true);
    }

    public final int getChannelSpellId() {
        return ((UnitChannel) getUnitData().channelData).spellID;
    }

    public final void setChannelSpellId(int value) {

        setUpdateFieldValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().channelData).getValue().spellID, value);
    }

    public final int getChannelSpellXSpellVisualId() {
        return getUnitData().channelData.getValue().spellVisual.spellXSpellVisualID;
    }

    public final int getChannelScriptVisualId() {
        return getUnitData().channelData.getValue().spellVisual.scriptVisualID;
    }

    public final Pet getAsPet() {
        return this instanceof Pet ? (pet) this : null;
    }

    public final int getTransformSpell() {
        return transformSpell;
    }

    public final void setTransformSpell(int value) {
        transformSpell = value;
    }



    //Unit

    public final Unit getVehicleBase() {
        return getVehicle() != null ? getVehicle().getBase() : null;
    }

    public final Creature getVehicleCreatureBase() {
        var veh = getVehicleBase();

        if (veh != null) {
            var c = veh.toCreature();

            if (c != null) {
                return c;
            }
        }

        return null;
    }

    public final TransportObject getDirectTransport() {
        var veh = getVehicle();

        if (veh != null) {
            return veh;
        }

        return getTransport();
    }



    public void setAi(UnitAI value) {
        pushAI(value);
        refreshAI();
    }

    public final UnitAI getBaseAI() {
        return getAi();
    }

    public void destroy() throws IOException {
        // set current spells as deletable
        for (CurrentSpellType i = 0; i.getValue() < CurrentSpellType.max.getValue(); ++i) {
            if (getCurrentSpells().containsKey(i)) {
                if (getCurrentSpells().get(i) != null) {
                    getCurrentSpells().get(i).setReferencedFromCurrent(false);
                    getCurrentSpells().put(i, null);
                }
            }
        }

        getEvents().KillAllEvents(true);

        _DeleteRemovedAuras();

        //i_motionMaster = null;
        charmInfo = null;
        setMoveSpline(null);
        spellHistory = null;

		/*ASSERT(!m_duringRemoveFromWorld);
		ASSERT(!m_attacking);
		ASSERT(m_attackers.empty());
		ASSERT(m_sharedVision.empty());
		ASSERT(m_Controlled.empty());
		ASSERT(m_appliedAuras.empty());
		ASSERT(m_ownedAuras.empty());
		ASSERT(m_removedAuras.empty());
		ASSERT(m_gameObj.empty());
		ASSERT(m_dynObj.empty());*/

        super.close();
    }

    @Override
    public void update(int diff) {
        // WARNING! Order of execution here is important, do not change.
        // Spells must be processed with event system BEFORE they go to _UpdateSpells.
        super.update(diff);

        if (!isInWorld()) {
            return;
        }

        _UpdateSpells(diff);

        // If this is set during update setCantProc(false) call is missing somewhere in the code
        // Having this would prevent spells from being proced, so let's crash

        combatManager.update(diff);

        lastDamagedTargetGuid = ObjectGuid.Empty;

        if (lastExtraAttackSpell != 0) {
            while (!extraAttacksTargets.isEmpty()) {

                var(targetGuid, count) = extraAttacksTargets.FirstOrDefault();
                extraAttacksTargets.remove(targetGuid);

                var victim = global.getObjAccessor().GetUnit(this, targetGuid);

                if (victim != null) {
                    handleProcExtraAttackFor(victim, count);
                }
            }

            lastExtraAttackSpell = 0;
        }


//		bool spellPausesCombatTimer(CurrentSpellTypes type)
//			{
//				return getCurrentSpell(type) != null && getCurrentSpell(type).spellInfo.hasAttribute(SpellAttr6.DelayCombatTimerDuringCast);
//			}

        if (!spellPausesCombatTimer(CurrentSpellType.generic) && !spellPausesCombatTimer(CurrentSpellType.Channeled)) {
            var base_att = getAttackTimer(WeaponAttackType.BASE_ATTACK);

            if (base_att != 0) {
                setAttackTimer(WeaponAttackType.BASE_ATTACK, (diff >= base_att ? 0 : base_att - diff));
            }

            var ranged_att = getAttackTimer(WeaponAttackType.RangedAttack);

            if (ranged_att != 0) {
                setAttackTimer(WeaponAttackType.RangedAttack, (diff >= ranged_att ? 0 : ranged_att - diff));
            }

            var off_att = getAttackTimer(WeaponAttackType.OFF_ATTACK);

            if (off_att != 0) {
                setAttackTimer(WeaponAttackType.OFF_ATTACK, (diff >= off_att ? 0 : off_att - diff));
            }
        }

        // update abilities available only for fraction of time
        updateReactives(diff);

        if (isAlive()) {
            modifyAuraState(AuraStateType.Wounded20Percent, healthBelowPct(20));
            modifyAuraState(AuraStateType.Wounded25Percent, healthBelowPct(25));
            modifyAuraState(AuraStateType.Wounded35Percent, healthBelowPct(35));
            modifyAuraState(AuraStateType.WoundHealth20_80, healthBelowPct(20) || healthAbovePct(80));
            modifyAuraState(AuraStateType.Healthy75Percent, healthAbovePct(75));
            modifyAuraState(AuraStateType.WoundHealth35_80, healthBelowPct(35) || healthAbovePct(80));
        }

        updateSplineMovement(diff);

        getMotionMaster().update(diff);

        // Wait with the aura interrupts until we have updated our movement generators and position
        if (isPlayer()) {
            interruptMovementBasedAuras();
        } else if (!getMoveSpline().finalized()) {
            interruptMovementBasedAuras();
        }

        // All position info based actions have been executed, reset info
        positionUpdateInfo.reset();

        if (hasScheduledAIChange() && (!isPlayer() || (isCharmed() && getCharmerGUID().isCreature()))) {
            updateCharmAI();
        }

        refreshAI();
    }

    public final void handleEmoteCommand(Emote emoteId, Player target, int[] spellVisualKitIds) {
        handleEmoteCommand(emoteId, target, spellVisualKitIds, 0);
    }

    public final void handleEmoteCommand(Emote emoteId, Player target) {
        handleEmoteCommand(emoteId, target, null, 0);
    }

    public final void handleEmoteCommand(Emote emoteId) {
        handleEmoteCommand(emoteId, null, null, 0);
    }

    public final void handleEmoteCommand(Emote emoteId, Player target, int[] spellVisualKitIds, int sequenceVariation) {
        EmoteMessage packet = new EmoteMessage();
        packet.guid = getGUID();
        packet.emoteID = (int) emoteId.getValue();

        var emotesEntry = CliDB.EmotesStorage.get(emoteId);

        if (emotesEntry != null && spellVisualKitIds != null) {
            if (emotesEntry.AnimId == (int) Anim.MountSpecial.getValue() || emotesEntry.AnimId == (int) Anim.MountSelfSpecial.getValue()) {
                tangible.IntegerLists.addPrimitiveArrayToList(spellVisualKitIds, packet.spellVisualKitIDs);
            }
        }

        packet.sequenceVariation = sequenceVariation;

        if (target != null) {
            target.sendPacket(packet);
        } else {
            sendMessageToSet(packet, true);
        }
    }

    public final void sendDurabilityLoss(Player receiver, int percent) {
        DurabilityDamageDeath packet = new DurabilityDamageDeath();
        packet.percent = percent;
        receiver.sendPacket(packet);
    }

    public final boolean isDisallowedMountForm(int spellId, ShapeShiftForm form, int displayId) {
        var transformSpellInfo = global.getSpellMgr().getSpellInfo(spellId, getMap().getDifficultyID());

        if (transformSpellInfo != null) {
            if (transformSpellInfo.hasAttribute(SpellAttr0.AllowWhileMounted)) {
                return false;
            }
        }

        if (form != 0) {
            var shapeshift = CliDB.SpellShapeshiftFormStorage.get(form);

            if (shapeshift == null) {
                return true;
            }

            if (!shapeshift.flags.hasFlag(SpellShapeshiftFormFlags.Stance)) {
                return true;
            }
        }

        if (displayId == getNativeDisplayId()) {
            return false;
        }

        var display = CliDB.CreatureDisplayInfoStorage.get(displayId);

        if (display == null) {
            return true;
        }

        var displayExtra = CliDB.CreatureDisplayInfoExtraStorage.get(display.ExtendedDisplayInfoID);

        if (displayExtra == null) {
            return true;
        }

        var model = CliDB.CreatureModelDataStorage.get(display.modelID);
        var race = CliDB.ChrRacesStorage.get(displayExtra.DisplayRaceID);

        if (model != null && !model.getFlags().hasFlag(CreatureModelDataFlags.CanMountWhileTransformedAsThis)) {
            if (race != null && !race.getFlags().hasFlag(ChrRacesFlag.CanMount)) {
                return true;
            }
        }

        return false;
    }

    public final void sendClearTarget() {
        BreakTarget breakTarget = new BreakTarget();
        breakTarget.unitGUID = getGUID();
        sendMessageToSet(breakTarget, false);
    }

    public final ArrayList<Player> getSharedVisionList() {
        return sharedVision;
    }

    public final void addPlayerToVision(Player player) {
        if (sharedVision.isEmpty()) {
            setActive(true);
            setIsStoredInWorldObjectGridContainer(true);
        }

        sharedVision.add(player);
    }

    // only called in player.SetSeer
    public final void removePlayerFromVision(Player player) {
        sharedVision.remove(player);

        if (sharedVision.isEmpty()) {
            setActive(false);
            setIsStoredInWorldObjectGridContainer(false);
        }
    }

    public boolean hasSharedVision() {
        return !sharedVision.isEmpty();
    }


    public void talk(String text, ChatMsg msgType, Language language, float textRange, WorldObject target) {
        var builder = new CustomChatTextBuilder(this, msgType, text, language, target);
        var localizer = new LocalizedDo(builder);
        var worker = new PlayerDistWorker(this, textRange, localizer, gridType.World);
        Cell.visitGrid(this, worker, textRange);
    }

    public void say(String text, Language language) {
        say(text, language, null);
    }

    public void say(String text, Language language, WorldObject target) {
        talk(text, ChatMsg.MonsterSay, language, WorldConfig.getFloatValue(WorldCfg.ListenRangeSay), target);
    }

    public void yell(String text, Language language) {
        yell(text, language, null);
    }

    public void yell(String text) {
        yell(text, language.Universal, null);
    }

    public void yell(String text, Language language, WorldObject target) {
        talk(text, ChatMsg.MonsterYell, language, WorldConfig.getFloatValue(WorldCfg.ListenRangeYell), target);
    }

    public void textEmote(String text, WorldObject target) {
        textEmote(text, target, false);
    }

    public void textEmote(String text) {
        textEmote(text, null, false);
    }

    public void textEmote(String text, WorldObject target, boolean isBossEmote) {
        talk(text, isBossEmote ? ChatMsg.RaidBossEmote : ChatMsg.MonsterEmote, language.Universal, WorldConfig.getFloatValue(WorldCfg.ListenRangeTextemote), target);
    }

    public void whisper(String text, Player target) {
        whisper(text, target, false);
    }

    public void whisper(String text, Player target, boolean isBossWhisper) {
        whisper(text, language.Universal, target, isBossWhisper);
    }

    public void whisper(String text, Language language, Player target) {
        whisper(text, language, target, false);
    }

    public void whisper(String text, Language language, Player target, boolean isBossWhisper) {
        if (!target) {
            return;
        }

        var locale = target.getSession().getSessionDbLocaleIndex();
        ChatPkt data = new ChatPkt();
        data.initialize(isBossWhisper ? ChatMsg.RaidBossWhisper : ChatMsg.MonsterWhisper, language.Universal, this, target, text, 0, "", locale);
        target.sendPacket(data);
    }

    public final void talk(int textId, ChatMsg msgType, float textRange, WorldObject target) {
        if (!CliDB.BroadcastTextStorage.containsKey(textId)) {
            Log.outError(LogFilter.unit, "Unit.Talk: `broadcast_text` (Id: {0}) was not found", textId);

            return;
        }

        var builder = new BroadcastTextBuilder(this, msgType, textId, getGender(), target);
        var localizer = new LocalizedDo(builder);
        var worker = new PlayerDistWorker(this, textRange, localizer, gridType.World);
        Cell.visitGrid(this, worker, textRange);
    }

    public void say(int textId) {
        say(textId, null);
    }

    public void say(int textId, WorldObject target) {
        talk(textId, ChatMsg.MonsterSay, WorldConfig.getFloatValue(WorldCfg.ListenRangeSay), target);
    }

    public void yell(int textId) {
        yell(textId, null);
    }

    public void yell(int textId, WorldObject target) {
        talk(textId, ChatMsg.MonsterYell, WorldConfig.getFloatValue(WorldCfg.ListenRangeYell), target);
    }

    public void textEmote(int textId, WorldObject target) {
        textEmote(textId, target, false);
    }

    public void textEmote(int textId) {
        textEmote(textId, null, false);
    }

    public void textEmote(int textId, WorldObject target, boolean isBossEmote) {
        talk(textId, isBossEmote ? ChatMsg.RaidBossEmote : ChatMsg.MonsterEmote, WorldConfig.getFloatValue(WorldCfg.ListenRangeTextemote), target);
    }

    public void whisper(int textId, Player target) {
        whisper(textId, target, false);
    }

    public void whisper(int textId, Player target, boolean isBossWhisper) {
        if (!target) {
            return;
        }

        var bct = CliDB.BroadcastTextStorage.get(textId);

        if (bct == null) {
            Log.outError(LogFilter.unit, "Unit.Whisper: `broadcast_text` was not {0} found", textId);

            return;
        }

        var locale = target.getSession().getSessionDbLocaleIndex();
        ChatPkt data = new ChatPkt();
        data.initialize(isBossWhisper ? ChatMsg.RaidBossWhisper : ChatMsg.MonsterWhisper, language.Universal, this, target, global.getDB2Mgr().GetBroadcastTextValue(bct, locale, getGender()), 0, "", locale);
        target.sendPacket(data);
    }

    @Override
    public void updateObjectVisibility() {
        updateObjectVisibility(true);
    }

    @Override
    public void updateObjectVisibility(boolean forced) {
        if (!forced) {
            addToNotify(NotifyFlag.VisibilityChanged);
        } else {
            super.updateObjectVisibility(true);
            // call MoveInLineOfSight for nearby creatures
            AIRelocationNotifier notifier = new AIRelocationNotifier(this, gridType.All);
            Cell.visitGrid(this, notifier, getVisibilityRange());
        }
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        motionMaster.addToWorld();

        removeAurasWithInterruptFlags(SpellAuraInterruptFlag.EnterWorld);
    }

    @Override
    public void removeFromWorld() {
        // cleanup

        if (isInWorld()) {
            duringRemoveFromWorld = true;
            var ai = getAi();

            if (ai != null) {
                ai.onDespawn();
            }

            if (isVehicle()) {
                removeVehicleKit(true);
            }

            removeCharmAuras();
            removeAurasByType(AuraType.BindSight);
            removeNotOwnSingleTargetAuras();
            removeAurasWithInterruptFlags(SpellAuraInterruptFlag.LeaveWorld);

            removeAllGameObjects();
            removeAllDynObjects();
            removeAllAreaTriggers();

            exitVehicle(); // Remove applied auras with SPELL_AURA_CONTROL_VEHICLE
            unsummonAllTotems();
            removeAllControlled();

            removeAreaAurasDueToLeaveWorld();

            removeAllFollowers();

            if (isCharmed()) {
                removeCharmedBy(null);
            }

            var owner = getOwnerUnit();

            if (owner != null) {
                if (owner.getControlled().contains(this)) {
                    Log.outFatal(LogFilter.unit, "Unit {0} is in controlled list of {1} when removed from world", getEntry(), owner.getEntry());
                }
            }

            super.removeFromWorld();
            duringRemoveFromWorld = false;
        }
    }

    public final void cleanupBeforeRemoveFromMap(boolean finalCleanup) {
        // This needs to be before RemoveFromWorld to make getCaster() return a valid for aura removal
        interruptNonMeleeSpells(true);

        if (isInWorld()) {
            removeFromWorld();
        }

        // A unit may be in removelist and not in world, but it is still in grid
        // and may have some references during delete
        removeAllAuras();
        removeAllGameObjects();

        if (finalCleanup) {
            cleanupDone = true;
        }

        combatStop();
    }

    @Override
    public void cleanupsBeforeDelete() {
        cleanupsBeforeDelete(true);
    }

    @Override
    public void cleanupsBeforeDelete(boolean finalCleanup) {
        cleanupBeforeRemoveFromMap(finalCleanup);

        super.cleanupsBeforeDelete(finalCleanup);
    }

    public final void _RegisterDynObject(DynamicObject dynObj) {
        getDynamicObjects().add(dynObj);

        if (isTypeId(TypeId.UNIT) && isAIEnabled()) {
            toCreature().getAi().justRegisteredDynObject(dynObj);
        }
    }

    public final void _UnregisterDynObject(DynamicObject dynObj) {
        getDynamicObjects().remove(dynObj);

        if (isTypeId(TypeId.UNIT) && isAIEnabled()) {
            toCreature().getAi().justUnregisteredDynObject(dynObj);
        }
    }

    public final DynamicObject getDynObject(int spellId) {
        return getDynObjects(spellId).FirstOrDefault();
    }

    public final void removeDynObject(int spellId) {
        for (var i = 0; i < getDynamicObjects().size(); ++i) {
            var dynObj = getDynamicObjects().get(i);

            if (dynObj.getSpellId() == spellId) {
                dynObj.remove();
            }
        }
    }

    public final void removeAllDynObjects() {
        while (!getDynamicObjects().isEmpty()) {
            getDynamicObjects().get(0).remove();
        }
    }

    public final GameObject getGameObject(int spellId) {
        return getGameObjects(spellId).FirstOrDefault();
    }

    public final void addGameObject(GameObject gameObj) {
        if (gameObj == null || !gameObj.getOwnerGUID().isEmpty()) {
            return;
        }

        getGameObjects().add(gameObj);
        gameObj.setOwnerGUID(getGUID());

        if (gameObj.getSpellId() != 0) {
            var createBySpell = global.getSpellMgr().getSpellInfo(gameObj.getSpellId(), getMap().getDifficultyID());

            // Need disable spell use for owner
            if (createBySpell != null && createBySpell.isCooldownStartedOnEvent()) {
                // note: item based cooldowns and cooldown spell mods with charges ignored (unknown existing cases)
                getSpellHistory().startCooldown(createBySpell, 0, null, true);
            }
        }

        if (isTypeId(TypeId.UNIT) && toCreature().isAIEnabled()) {
            toCreature().getAi().justSummonedGameobject(gameObj);
        }
    }

    public final void removeGameObject(GameObject gameObj, boolean del) {
        if (gameObj == null || ObjectGuid.opNotEquals(gameObj.getOwnerGUID(), getGUID())) {
            return;
        }

        gameObj.setOwnerGUID(ObjectGuid.Empty);

        for (byte i = 0; i < SharedConst.MaxGameObjectSlot; ++i) {
            if (Objects.equals(getObjectSlot()[i], gameObj.getGUID())) {
                getObjectSlot()[i].clear();

                break;
            }
        }

        // GO created by some spell
        var spellid = gameObj.getSpellId();

        if (spellid != 0) {
            removeAura(spellid);

            var createBySpell = global.getSpellMgr().getSpellInfo(spellid, getMap().getDifficultyID());

            // Need activate spell use for owner
            if (createBySpell != null && createBySpell.isCooldownStartedOnEvent()) {
                // note: item based cooldowns and cooldown spell mods with charges ignored (unknown existing cases)
                getSpellHistory().sendCooldownEvent(createBySpell);
            }
        }

        getGameObjects().remove(gameObj);

        if (isTypeId(TypeId.UNIT) && toCreature().isAIEnabled()) {
            toCreature().getAi().summonedGameobjectDespawn(gameObj);
        }

        if (del) {
            gameObj.setRespawnTime(0);
            gameObj.delete();
        }
    }

    public final void removeGameObject(int spellid, boolean del) {
        if (getGameObjects().isEmpty()) {
            return;
        }

        for (var i = 0; i < getGameObjects().size(); ++i) {
            var obj = getGameObjects().get(i);

            if (spellid == 0 || obj.getSpellId() == spellid) {
                obj.setOwnerGUID(ObjectGuid.Empty);

                if (del) {
                    obj.setRespawnTime(0);
                    obj.delete();
                }

                getGameObjects().remove(obj);
            }
        }
    }

    public final void removeAllGameObjects() {
        // remove references to unit
        while (!getGameObjects().isEmpty()) {
            var obj = getGameObjects().get(0);
            obj.setOwnerGUID(ObjectGuid.Empty);
            obj.setRespawnTime(0);
            obj.delete();
            getGameObjects().remove(obj);
        }
    }

    public final void _RegisterAreaTrigger(AreaTrigger areaTrigger) {
        areaTrigger.add(areaTrigger);

        if (isTypeId(TypeId.UNIT) && isAIEnabled()) {
            toCreature().getAi().justRegisteredAreaTrigger(areaTrigger);
        }
    }

    public final void _UnregisterAreaTrigger(AreaTrigger areaTrigger) {
        areaTrigger.remove(areaTrigger);

        if (isTypeId(TypeId.UNIT) && isAIEnabled()) {
            toCreature().getAi().justUnregisteredAreaTrigger(areaTrigger);
        }
    }

    public final AreaTrigger getAreaTrigger(int spellId) {
        var areaTriggers = getAreaTriggers(spellId);

        return areaTriggers.isEmpty() ? null : areaTriggers.get(0);
    }

    public final ArrayList<areaTrigger> getAreaTriggers(int spellId) {
        return areaTrigger.stream().filter(trigger -> trigger.spellId == spellId).collect(Collectors.toList());
    }

    public final void removeAreaTrigger(int spellId) {
        if (areaTrigger.isEmpty()) {
            return;
        }

        for (var i = 0; i < areaTrigger.size(); ++i) {
            var areaTrigger = areaTrigger.get(i);

            if (areaTrigger.getSpellId() == spellId) {
                areaTrigger.remove();
            }
        }
    }

    public final void removeAreaTrigger(AuraEffect aurEff) {
        if (areaTrigger.isEmpty()) {
            return;
        }

        for (var areaTrigger : areaTrigger) {
            if (areaTrigger.getAuraEff() == aurEff) {
                areaTrigger.remove();

                break; // There can only be one AreaTrigger per AuraEffect
            }
        }
    }

    public final void removeAllAreaTriggers() {
        while (!areaTrigger.isEmpty()) {
            areaTrigger.get(0).remove();
        }
    }

    public final boolean hasNpcFlag(NPCFlag flags) {
        return (getUnitData().npcFlags.get(0) & (int) flags.getValue()) != 0;
    }

    public final void setNpcFlag(NPCFlag flags) {

        setUpdateFieldFlagValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().npcFlags, 0), (int) flags.getValue());
    }

    public final void removeNpcFlag(NPCFlag flags) {

        removeUpdateFieldFlagValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().npcFlags, 0), (int) flags.getValue());
    }

    public final void replaceAllNpcFlags(NPCFlag flags) {

        setUpdateFieldValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().npcFlags, 0), (int) flags.getValue());
    }

    public final boolean hasNpcFlag2(NPCFlags2 flags) {
        return (getUnitData().npcFlags.get(1) & (int) flags.getValue()) != 0;
    }

    public final void setNpcFlag2(NPCFlags2 flags) {

        setUpdateFieldFlagValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().npcFlags, 1), (int) flags.getValue());
    }

    public final void removeNpcFlag2(NPCFlags2 flags) {

        removeUpdateFieldFlagValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().npcFlags, 1), (int) flags.getValue());
    }

    public final void replaceAllNpcFlags2(NPCFlags2 flags) {

        setUpdateFieldValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().npcFlags, 1), (int) flags.getValue());
    }

    public final boolean isContestedGuard() {
        var entry = getFactionTemplate();

        if (entry != null) {
            return entry.IsContestedGuardFaction();
        }

        return false;
    }

    public final void setHoverHeight(float hoverHeight) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().hoverHeight), hoverHeight);
    }

    @Override
    public String getDebugInfo() {
        var str = String.format("%1$s\nIsAIEnabled: %2$s DeathState: %3$s UnitMovementFlags: %4$s UnitMovementFlags2: %5$s Class: %6$s\n", super.getDebugInfo(), isAIEnabled(), deathState, getUnitMovementFlags(), getUnitMovementFlags2(), getUnitClass()) + String.format(" %1$s GetCharmedGUID(): %2$s\nGetCharmerGUID(): %3$s\n%4$s\n", (getMoveSpline() != null ? getMoveSpline().toString() : "Movespline: <none>\n"), getCharmedGUID(), getCharmerGUID(), (getVehicleKit() != null ? vehicleKit.getDebugInfo() : "No vehicle kit")) + String.format("m_Controlled size: %1$s", getControlled().size());

        var controlledCount = 0;

        for (var controlled : getControlled()) {
            ++controlledCount;
            str += String.format("\nm_Controlled %1$s : %2$s", controlledCount, controlled.getGUID());
        }

        return str;
    }

    public final Guardian getGuardianPet() {
        var pet_guid = getPetGUID();

        if (!pet_guid.isEmpty()) {
            var pet = ObjectAccessor.GetCreatureOrPetOrVehicle(this, pet_guid);

            if (pet != null) {
                if (pet.hasUnitTypeMask(UnitTypeMask.Guardian)) {
                    return (Guardian) pet;
                }
            }

            Log.outFatal(LogFilter.unit, "Unit:GetGuardianPet: Guardian {0} not exist.", pet_guid);
            setPetGUID(ObjectGuid.Empty);
        }

        return null;
    }

    public final Creature getSummonedCreatureByEntry(int entry) {
        for (var sum : getSummonSlot()) {
            var cre = ObjectAccessor.getCreature(this, sum);

            if (cre.getEntry() == entry) {
                return cre;
            }
        }

        return null;
    }

    public final Unit selectNearbyTarget(Unit exclude) {
        return selectNearbyTarget(exclude, SharedConst.NominalMeleeRange);
    }

    public final Unit selectNearbyTarget() {
        return selectNearbyTarget(null, SharedConst.NominalMeleeRange);
    }

    public final Unit selectNearbyTarget(Unit exclude, float dist) {

//		bool AddUnit(Unit u)
//			{
//				if (victim == u)
//					return false;
//
//				if (exclude == u)
//					return false;
//
//				// remove not LoS targets
//				if (!isWithinLOSInMap(u) || u.IsTotem || u.IsSpiritService || u.IsCritter)
//					return false;
//
//				return true;
//			}

        ArrayList<Unit> targets = new ArrayList<>();
        var u_check = new AnyUnfriendlyUnitInObjectRangeCheck(this, this, dist, AddUnit);
        var searcher = new UnitListSearcher(this, targets, u_check, gridType.All);
        Cell.visitGrid(this, searcher, dist);

        // no appropriate targets
        if (targets.isEmpty()) {
            return null;
        }

        // select random
        return targets.SelectRandom();
    }

    public final Unit selectNearbyAllyUnit(ArrayList<Unit> exclude) {
        return selectNearbyAllyUnit(exclude, SharedConst.NominalMeleeRange);
    }

    public final Unit selectNearbyAllyUnit(ArrayList<Unit> exclude, float dist) {
        ArrayList<Unit> targets = new ArrayList<>();
        var u_check = new AnyFriendlyUnitInObjectRangeCheck(this, this, dist);
        var searcher = new UnitListSearcher(this, targets, u_check, gridType.All);
        Cell.visitGrid(this, searcher, dist);

        // no appropriate targets
        tangible.ListHelper.removeAll(targets, k -> exclude.contains(k));

        if (targets.isEmpty()) {
            return null;
        }

        return targets.SelectRandom();
    }

    public final void enterVehicle(Unit baseUnit) {
        enterVehicle(baseUnit, -1);
    }

    public final void enterVehicle(Unit baseUnit, byte seatId) {
        CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlags.IgnoreCasterMountedOrOnVehicle);
        args.addSpellMod(SpellValueMod.BasePoint0, seatId + 1);
        castSpell(baseUnit, SharedConst.VehicleSpellRideHardcoded, args);
    }

    public final void _EnterVehicle(Vehicle vehicle, byte seatId, AuraApplication aurApp) {
        if (!isAlive() || vehicleKit == vehicle || vehicle.GetBase().isOnVehicle(this)) {
            return;
        }

        if (getVehicle() != null) {
            if (getVehicle() != vehicle) {
                Log.outDebug(LogFilter.vehicle, "EnterVehicle: {0} exit {1} and enter {2}.", getEntry(), getVehicle().GetBase().getEntry(), vehicle.GetBase().getEntry());
                exitVehicle();
            } else if (seatId >= 0 && seatId == getTransSeat()) {
                return;
            } else {
                //Exit the current vehicle because unit will reenter in a new seat.
                getVehicle().GetBase().removeAurasByType(AuraType.ControlVehicle, getGUID(), aurApp.getBase());
            }
        }

        if (aurApp.getHasRemoveMode()) {
            return;
        }

        var player = toPlayer();

        if (player != null) {
            if (vehicle.GetBase().isTypeId(TypeId.PLAYER) && player.isInCombat()) {
                vehicle.GetBase().removeAura(aurApp);

                return;
            }

            if (vehicle.GetBase().isCreature()) {
                // If a player entered a vehicle that is part of a formation, remove it from said formation
                var creatureGroup = vehicle.GetBase().toCreature().getFormation();

                if (creatureGroup != null) {
                    creatureGroup.removeMember(vehicle.GetBase().toCreature());
                }
            }
        }

        vehicle.AddVehiclePassenger(this, seatId);
    }

    public final void changeSeat(byte seatId) {
        changeSeat(seatId, true);
    }

    public final void changeSeat(byte seatId, boolean next) {
        if (getVehicle() == null) {
            return;
        }

        // Don't change if current and new seat are identical
        if (seatId == getTransSeat()) {
            return;
        }

        var seat = (seatId < 0 ? getVehicle().GetNextEmptySeat(getTransSeat(), next) : getVehicle().Seats.get(seatId));

        // The second part of the check will only return true if seatId >= 0. @Vehicle.GetNextEmptySeat makes sure of that.
        if (seat == null || !seat.isEmpty()) {
            return;
        }

        AuraEffect rideVehicleEffect = null;
        var vehicleAuras = getVehicle().GetBase().getAuraEffectsByType(AuraType.ControlVehicle);

        for (var eff : vehicleAuras) {
            if (ObjectGuid.opNotEquals(eff.getCasterGuid(), getGUID())) {
                continue;
            }

            rideVehicleEffect = eff;
        }

        rideVehicleEffect.changeAmount((seatId < 0 ? getTransSeat() : seatId) + 1);
    }

    public void exitVehicle() {
        exitVehicle(null);
    }

    public void exitVehicle(Position exitPosition) {
        //! This function can be called at upper level code to initialize an exit from the passenger's side.
        if (getVehicle() == null) {
            return;
        }

        getVehicleBase().removeAurasByType(AuraType.ControlVehicle, new tangible.Func1Param<AuraApplication, bool>(getGUID()));
        //! The following call would not even be executed successfully as the
        //! SPELL_AURA_CONTROL_VEHICLE unapply handler already calls _ExitVehicle without
        //! specifying an exitposition. The subsequent call below would return on if (!m_vehicle).

        //! To do:
        //! We need to allow SPELL_AURA_CONTROL_VEHICLE unapply handlers in spellscripts
        //! to specify exit coordinates and either store those per passenger, or we need to
        //! init spline movement based on those coordinates in unapply handlers, and
        //! relocate exiting passengers based on unit.moveSpline data. Either way,
        //! Coming Soon(TM)
    }

    public final void _ExitVehicle() {
        _ExitVehicle(null);
    }

    public final void _ExitVehicle(Position exitPosition) {
        // It's possible m_vehicle is NULL, when this function is called indirectly from @VehicleJoinEvent.Abort.
        // In that case it was not possible to add the passenger to the vehicle. The vehicle aura has already been removed
        // from the target in the aforementioned function and we don't need to do anything else at this point.
        if (getVehicle() == null) {
            return;
        }

        // This should be done before dismiss, because there may be some aura removal
        var seatAddon = getVehicle().GetSeatAddonForSeatOfPassenger(this);
        var vehicle = (vehicle) getVehicle().removePassenger(this);

        if (vehicle == null) {
            Log.outError(LogFilter.vehicle, String.format("RemovePassenger() couldn't remove current unit from vehicle. Debug info: %1$s", getDebugInfo()));

            return;
        }

        var player = toPlayer();

        // If the player is on mounted duel and exits the mount, he should immediatly lose the duel
        if (player && player.getDuel() != null && player.getDuel().isMounted()) {
            player.duelComplete(DuelCompleteType.fled);
        }

        setControlled(false, UnitState.Root); // SMSG_MOVE_FORCE_UNROOT, ~MOVEMENTFLAG_ROOT

        addUnitState(UnitState.move);

        if (player != null) {
            player.setFallInformation(0, getLocation().getZ());
        }

        Position pos;

        // If we ask for a specific exit position, use that one. Otherwise allow scripts to modify it
        if (exitPosition != null) {
            pos = exitPosition;
        } else {
            // Set exit position to vehicle position and use the current orientation
            pos = vehicle.GetBase().getLocation();
            pos.setO(getLocation().getO());

            // Change exit position based on seat entry addon data
            if (seatAddon != null) {
                if (seatAddon.ExitParameter == VehicleExitParameters.VehicleExitParamOffset) {
                    pos.relocateOffset(new Position(seatAddon.ExitParameterX, seatAddon.ExitParameterY, seatAddon.ExitParameterZ, seatAddon.ExitParameterO));
                } else if (seatAddon.ExitParameter == VehicleExitParameters.VehicleExitParamDest) {
                    pos.relocate(new Position(seatAddon.ExitParameterX, seatAddon.ExitParameterY, seatAddon.ExitParameterZ, seatAddon.ExitParameterO));
                }
            }
        }

        var initializer = (MoveSplineInit init) ->
        {
            var height = pos.getZ() + vehicle.GetBase().getCollisionHeight();

            // Creatures without inhabit type air should begin falling after exiting the vehicle
            tangible.RefObject<Float> tempRef_height = new tangible.RefObject<Float>(height);
            if (isTypeId(TypeId.UNIT) && !canFly() && height > getMap().getWaterOrGroundLevel(getPhaseShift(), pos.getX(), pos.getY(), pos.getZ() + vehicle.GetBase().getCollisionHeight(), tempRef_height)) {
                height = tempRef_height.refArgValue;
                init.setFall();
            } else {
                height = tempRef_height.refArgValue;
            }

            init.moveTo(pos.getX(), pos.getY(), height, false);
            init.setFacing(pos.getO());
            init.setTransportExit();
        };

        getMotionMaster().launchMoveSpline(initializer, eventId.VehicleExit, MovementGeneratorPriority.Highest);

        if (player != null) {
            player.resummonPetTemporaryUnSummonedIfAny();
        }

        if (vehicle.GetBase().hasUnitTypeMask(UnitTypeMask.minion) && vehicle.GetBase().isTypeId(TypeId.UNIT)) {
            if (((minion) vehicle.GetBase()).getOwnerUnit() == this) {
                vehicle.GetBase().toCreature().despawnOrUnsummon(vehicle.GetDespawnDelay());
            }
        }

        if (hasUnitTypeMask(UnitTypeMask.ACCESSORY)) {
            // Vehicle just died, we die too
            if (vehicle.getBase().getDeathState() == DeathState.JUST_DIED) {
                setDeathState(deathState.JustDied);
            }
            // If for other reason we as minion are exiting the vehicle (ejected, master dismounted) - unsummon
            else {
                toTempSummon().unSummon(DESPAWN_TIME); // Approximation
            }
        }
    }

    public final void unsummonAllTotems() {
        for (byte i = 0; i < SharedConst.MaxSummonSlot; ++i) {
            if (getSummonSlot()[i].isEmpty()) {
                continue;
            }

            var OldTotem = getMap().getCreature(getSummonSlot()[i]);

            if (OldTotem != null) {
                if (OldTotem.isSummon()) {
                    OldTotem.toTempSummon().unSummon();
                }
            }
        }
    }

    public final boolean isOnVehicle(Unit vehicle) {
        return getVehicle() != null && getVehicle() == vehicle.getVehicleKit();
    }
    

    public final UnitAI getTopAI() {
        synchronized (getUnitAis()) {
            return getUnitAis().empty() ? null : getUnitAis().peek();
        }
    }

    public final void AIUpdateTick(int diff) {
        var ai = getAi();

        if (ai != null) {
            synchronized (getUnitAis()) {
                ai.updateAI(diff);
            }
        }
    }

    public final void pushAI(UnitAI newAI) {
        synchronized (getUnitAis()) {
            getUnitAis().push(newAI);
        }
    }

    public final boolean popAI() {
        synchronized (getUnitAis()) {
            if (getUnitAis().size() != 0) {
                getUnitAis().pop();

                return true;
            } else {
                return false;
            }
        }
    }

    public final void refreshAI() {
        synchronized (getUnitAis()) {
            if (getUnitAis().empty()) {
                setAi(null);
            } else {
                setAi(getUnitAis().peek());
            }
        }
    }

    public final void scheduleAIChange() {
        var charmed = isCharmed();

        if (charmed) {
            pushAI(getScheduledChangeAI());
        } else {
            restoreDisabledAI();
            pushAI(getScheduledChangeAI()); //This could actually be popAI() to get the previous AI but it's required atm to trigger updateCharmAI()
        }
    }

    public void onPhaseChange() {
    }

    public final int getModelForForm(ShapeShiftForm form, int spellId) {
        // Hardcoded cases
        switch (spellId) {
            case 7090: // Bear Form
                return 29414;
            case 35200: // Roc Form
                return 4877;
            case 24858: // Moonkin Form
            {
                if (hasAura(114301)) // Glyph of Stars
                {
                    return 0;
                }

                break;
            }
            default:
                break;
        }

        var thisPlayer = toPlayer();

        if (thisPlayer != null) {
            var artifactAura = getAura(PlayerConst.ArtifactsAllWeaponsGeneralWeaponEquippedPassive);

            if (artifactAura != null) {
                var artifact = toPlayer().getItemByGuid(artifactAura.castItemGuid);

                if (artifact != null) {
                    var artifactAppearance = CliDB.ArtifactAppearanceStorage.get(artifact.getModifier(ItemModifier.artifactAppearanceId));

                    if (artifactAppearance != null) {
                        if (ShapeShiftForm.forValue(artifactAppearance.OverrideShapeshiftFormID) == form) {
                            return artifactAppearance.OverrideShapeshiftDisplayID;
                        }
                    }
                }
            }

            var formModelData = global.getDB2Mgr().GetShapeshiftFormModelData(getRace(), thisPlayer.getNativeGender(), form);

            if (formModelData != null) {
                var useRandom = false;

                switch (form) {
                    case CatForm:
                        useRandom = hasAura(210333);

                        break; // Glyph of the Feral Chameleon
                    case TravelForm:
                        useRandom = hasAura(344336);

                        break; // Glyph of the Swift Chameleon
                    case AquaticForm:
                        useRandom = hasAura(344338);

                        break; // Glyph of the Aquatic Chameleon
                    case BearForm:
                        useRandom = hasAura(107059);

                        break; // Glyph of the Ursol Chameleon
                    case FlightFormEpic:
                    case FlightForm:
                        useRandom = hasAura(344342);

                        break; // Glyph of the Aerial Chameleon
                    default:
                        break;
                }

                if (useRandom) {
                    ArrayList<Integer> displayIds = new ArrayList<>();

                    for (var i = 0; i < formModelData.Choices.size(); ++i) {
                        var displayInfo = formModelData.Displays.get(i);

                        if (displayInfo != null) {
                            var choiceReq = CliDB.ChrCustomizationReqStorage.get(formModelData.Choices.get(i).ChrCustomizationReqID);

                            if (choiceReq == null || thisPlayer.getSession().meetsChrCustomizationReq(choiceReq, getUnitClass(), false, thisPlayer.getPlayerData().customizations)) {
                                displayIds.add(displayInfo.displayID);
                            }
                        }
                    }

                    if (!displayIds.isEmpty()) {
                        return displayIds.SelectRandom();
                    }
                } else {
                    var formChoice = thisPlayer.getCustomizationChoice(formModelData.OptionID);

                    if (formChoice != 0) {
                        var choiceIndex = tangible.ListHelper.findIndex(formModelData.Choices, choice ->
                        {
                            return choice.id == formChoice;
                        });

                        if (choiceIndex != -1) {
                            var displayInfo = formModelData.Displays.get(choiceIndex);

                            if (displayInfo != null) {
                                return displayInfo.displayID;
                            }
                        }
                    }
                }
            }

            switch (form) {
                case GhostWolf:
                    if (hasAura(58135)) // Glyph of Spectral Wolf
                    {
                        return 60247;
                    }

                    break;
                default:
                    break;
            }
        }

        int modelid = 0;
        var formEntry = CliDB.SpellShapeshiftFormStorage.get(form);

        if (formEntry != null && formEntry.CreatureDisplayID[0] != 0) {
            // Take the alliance modelid as default
            if (getObjectTypeId() != TypeId.PLAYER) {
                return formEntry.CreatureDisplayID[0];
            } else {
                if (player.teamForRace(getRace()) == Team.ALLIANCE) {
                    modelid = formEntry.CreatureDisplayID[0];
                } else {
                    modelid = formEntry.CreatureDisplayID[1];
                }

                // If the player is horde but there are no values for the horde modelid - take the alliance modelid
                if (modelid == 0 && player.teamForRace(getRace()) == Team.Horde) {
                    modelid = formEntry.CreatureDisplayID[0];
                }
            }
        }

        return modelid;
    }

    public final Pet toPet() {
        return isPet() ? (this instanceof Pet ? (Pet) this : null) : null;
    }


    public final Totem toTotem() {
        return isTotem() ? (this instanceof Totem ? (Totem) this : null) : null;
    }

    public final TempSummon toTempSummon() {
        return isSummon() ? (this instanceof TempSummon ? (TempSummon) this : null) : null;
    }

    public final boolean isVisible() {
        return getServerSideVisibility().getValue(ServerSideVisibilityType.GM) <= (int) AccountTypes.player.getValue();
    }

    public final void setVisible(boolean val) {
        if (!val) {
            getServerSideVisibility().setValue(ServerSideVisibilityType.GM, AccountTypes.GameMaster);
        } else {
            getServerSideVisibility().setValue(ServerSideVisibilityType.GM, AccountTypes.player);
        }

        updateObjectVisibility();
    }

    // creates aura application instance and registers it in lists
    // aura application effects are handled separately to prevent aura list corruption
    public final AuraApplication _CreateAuraApplication(Aura aura, HashSet<Integer> effMask) {
        // just return if the aura has been already removed
        // this can happen if OnEffectHitTarget() script hook killed the unit or the aura owner (which can be different)
        if (aura.isRemoved()) {
            Log.outError(LogFilter.spells, String.format("Unit::_CreateAuraApplication() called with a removed aura. Check if OnEffectHitTarget() is triggering any spell with apply aura effect (that's not allowed!)\nUnit: %1$s\nAura: %2$s", getDebugInfo(), aura.getDebugInfo()));

            return null;
        }

        var aurSpellInfo = aura.getSpellInfo();

        // ghost spell check, allow apply any auras at player loading in ghost mode (will be cleanup after load)
        if (!isAlive() && !aurSpellInfo.isDeathPersistent() && (!isTypeId(TypeId.PLAYER) || !toPlayer().getSession().getPlayerLoading())) {
            return null;
        }

        var caster = aura.getCaster();

        AuraApplication aurApp = new AuraApplication(this, caster, aura, effMask);
        appliedAuras.add(aurApp);

        if (aurSpellInfo.getHasAnyAuraInterruptFlag()) {
            interruptableAuras.add(aurApp);
            addInterruptMask(aurSpellInfo.getAuraInterruptFlags(), aurSpellInfo.getAuraInterruptFlags2());
        }

        var aState = aura.getSpellInfo().getAuraState();

        if (aState != 0) {
            auraStateAuras.add(aState, aurApp);
        }

        aura._ApplyForTarget(this, caster, aurApp);

        return aurApp;
    }

    public final void addInterruptMask(SpellAuraInterruptFlags flags, SpellAuraInterruptFlags2 flags2) {
        interruptMask = SpellAuraInterruptFlag.forValue(interruptMask.getValue() | flags.getValue());
        interruptMask2 = SpellAuraInterruptFlags2.forValue(interruptMask2.getValue() | flags2.getValue());
    }

    public final void updateDisplayPower() {
        var displayPower = powerType.mana;

        switch (getShapeshiftForm()) {
            case Ghoul:
            case CatForm:
                displayPower = powerType.Energy;

                break;
            case BearForm:
                displayPower = powerType.Rage;

                break;
            case TravelForm:
            case GhostWolf:
                displayPower = powerType.mana;

                break;
            default: {
                var powerTypeAuras = getAuraEffectsByType(AuraType.ModPowerDisplay);

                if (!powerTypeAuras.isEmpty()) {
                    var powerTypeAura = powerTypeAuras.get(0);
                    displayPower = powerType.forValue(powerTypeAura.miscValue);
                } else if (getObjectTypeId() == TypeId.PLAYER) {
                    var cEntry = CliDB.ChrClassesStorage.get(getUnitClass());

                    if (cEntry != null && cEntry.displayPower < powerType.max.getValue()) {
                        displayPower = cEntry.displayPower;
                    }
                } else if (getObjectTypeId() == TypeId.UNIT) {
                    var vehicle = vehicleKit;

                    if (vehicle) {
                        var powerDisplay = CliDB.PowerDisplayStorage.get(vehicle.GetVehicleInfo().PowerDisplayID[0]);

                        if (powerDisplay != null) {
                            displayPower = powerType.forValue(powerDisplay.ActualType);
                        } else if (getUnitClass() == UnitClass.Rogue) {
                            displayPower = powerType.Energy;
                        }
                    } else {
                        var pet = getAsPet();

                        if (pet) {
                            if (pet.getPetType() == PetType.Hunter) // Hunter pets have focus
                            {
                                displayPower = powerType.Focus;
                            } else if (pet.isPetGhoul() || pet.isPetAbomination()) // DK pets have energy
                            {
                                displayPower = powerType.Energy;
                            }
                        }
                    }
                }

                break;
            }
        }

        setPowerType(displayPower);
    }

    public final void followerAdded(AbstractFollower f) {
        followingMe.add(f);
    }

    public final void followerRemoved(AbstractFollower f) {
        followingMe.remove(f);
    }

    public final void playOneShotAnimKitId(short animKitId) {
        if (!CliDB.AnimKitStorage.containsKey(animKitId)) {
            Log.outError(LogFilter.unit, "Unit.PlayOneShotAnimKitId using invalid AnimKit ID: {0}", animKitId);

            return;
        }

        PlayOneShotAnimKit packet = new PlayOneShotAnimKit();
        packet.unit = getGUID();
        packet.animKitID = animKitId;
        sendMessageToSet(packet, true);
    }

    public final int getVirtualItemId(int slot) {
        if (slot >= SharedConst.MaxEquipmentItems) {
            return 0;
        }

        return getUnitData().virtualItems.get(slot).itemID;
    }

    public final short getVirtualItemAppearanceMod(int slot) {
        if (slot >= SharedConst.MaxEquipmentItems) {
            return 0;
        }

        return getUnitData().virtualItems.get((int) slot).itemAppearanceModID;
    }

    public final void setVirtualItem(int slot, int itemId, short appearanceModId) {
        setVirtualItem(slot, itemId, appearanceModId, 0);
    }

    public final void setVirtualItem(int slot, int itemId) {
        setVirtualItem(slot, itemId, 0, 0);
    }

    public final void setVirtualItem(int slot, int itemId, short appearanceModId, short itemVisual) {
        if (slot >= SharedConst.MaxEquipmentItems) {
            return;
        }

        var virtualItemField = getValues().modifyValue(getUnitData()).modifyValue(getUnitData().virtualItems, (int) slot);
        setUpdateFieldValue(virtualItemField.modifyValue(virtualItemField.itemID), itemId);
        setUpdateFieldValue(virtualItemField.modifyValue(virtualItemField.itemAppearanceModID), appearanceModId);
        setUpdateFieldValue(virtualItemField.modifyValue(virtualItemField.itemVisual), itemVisual);
    }

    public final void setLevel(int lvl, boolean sendUpdate) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().level), lvl);

        if (!sendUpdate) {
            return;
        }

        var player = toPlayer();

        if (player != null) {
            if (player.getGroup()) {
                player.setGroupUpdateFlag(GroupUpdateFlags.level);
            }

            global.getCharacterCacheStorage().updateCharacterLevel(toPlayer().getGUID(), (byte) lvl);
        }
    }

    @Override
    public int getLevelForTarget(WorldObject target) {
        return getLevel();
    }

    public final boolean isAlliedRace() {
        var player = toPlayer();

        if (player == null) {
            return false;
        }

        var race = getRace();

        /* pandaren death knight (basically same thing as allied death knight) */
        if ((race == race.PandarenAlliance || race == race.PandarenHorde || race == race.PandarenNeutral) && getUnitClass() == UnitClass.DEATH_KNIGHT) {
            return true;
        }

        /* other allied races */
        switch (race) {
            case Nightborne:
            case HighmountainTauren:
            case VoidElf:
            case LightforgedDraenei:
            case ZandalariTroll:
            case KulTiran:
            case DarkIronDwarf:
            case Vulpera:
            case MagharOrc:
            case MechaGnome:
                return true;
            default:
                return false;
        }
    }

    public final void recalculateObjectScale() {
        var scaleAuras = getTotalAuraModifier(AuraType.ModScale) + getTotalAuraModifier(AuraType.ModScale2);
        var scale = getNativeObjectScale() + MathUtil.CalculatePct(1.0f, scaleAuras);
        var scaleMin = isPlayer() ? 0.1f : 0.01f;
        setObjectScale((float) Math.max(scale, scaleMin));
    }

    public void setDisplayId(int modelId, boolean setNative) {


        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().displayID), modelId);
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().displayScale), displayScale);
        // Set Gender by modelId
        var minfo = global.getObjectMgr().getCreatureModelInfo(modelId);

        if (minfo != null) {
            setGender(gender.forValue(minfo.gender));
        }
    }

    public final void restoreDisplayId() {
        restoreDisplayId(false);
    }

    public final void restoreDisplayId(boolean ignorePositiveAurasPreventingMounting) {
        AuraEffect handledAura = null;
        // try to receive model from transform auras
        var transforms = getAuraEffectsByType(AuraType.Transform);

        if (!transforms.isEmpty()) {
            // iterate over already applied transform auras - from newest to oldest
            for (var eff : transforms) {
                var aurApp = eff.getBase().getApplicationOfTarget(getGUID());

                if (aurApp != null) {
                    if (handledAura == null) {
                        if (!ignorePositiveAurasPreventingMounting) {
                            handledAura = eff;
                        } else {
                            var ci = global.getObjectMgr().getCreatureTemplate((int) eff.getMiscValue());

                            if (ci != null) {
                                if (!isDisallowedMountForm(eff.getId(), ShapeShiftForm.NONE, ObjectManager.chooseDisplayId(ci).creatureDisplayId)) {
                                    handledAura = eff;
                                }
                            }
                        }
                    }

                    // prefer negative auras
                    if (!aurApp.isPositive()) {
                        handledAura = eff;

                        break;
                    }
                }
            }
        }

        var shapeshiftAura = getAuraEffectsByType(AuraType.ModShapeshift);

        // transform aura was found
        if (handledAura != null) {
            handledAura.handleEffect(this, AuraEffectHandleModes.SendForClient, true);

            return;
        }
        // we've found shapeshift
        else if (!shapeshiftAura.isEmpty()) // we've found shapeshift
        {
            // only one such aura possible at a time
            var modelId = getModelForForm(getShapeshiftForm(), shapeshiftAura.get(0).getId());

            if (modelId != 0) {
                if (!ignorePositiveAurasPreventingMounting || !isDisallowedMountForm(0, getShapeshiftForm(), modelId)) {
                    setDisplayId(modelId);
                } else {
                    setDisplayId(getNativeDisplayId());
                }

                return;
            }
        }

        // no auras found - set modelid to default
        setDisplayId(getNativeDisplayId());
    }

    public final void setNativeDisplayId(int displayId, boolean setNative) {

        float displayScale = ObjectDefine.DEFAULT_PLAYER_DISPLAY_SCALE;

        if (isCreature() && !isPet()) {
            var model = toCreature().getCreatureTemplate().getModelWithDisplayId(displayId);
            if (model != null) {
                displayScale = model.displayScale;
            }
        }
        setInt32Value(UNIT_FIELD_DISPLAY_ID, displayId);
        setObjectScale(displayScale);

        if (setNative) {
            setInt32Value(UNIT_FIELD_NATIVE_DISPLAY_ID, displayId);
            setObjectScale(displayScale);
        }
        // Set Gender by ModelInfo
        var modelInfo = worldContext.getObjectManager().getCreatureModelInfo(displayId);
        if (modelInfo != null) {
            setGender(modelInfo.gender);
        }
        calculateHoverHeight();
    }

    public final float getModCastingSpeed() {
        return getFloatValue(UNIT_MOD_CAST_SPEED);
    }

    public final void setModCastingSpeed(float castingSpeed) {
        setFloatValue(UNIT_MOD_CAST_SPEED, castingSpeed);
    }

    public final void setModSpellHaste(float spellHaste) {
        setFloatValue(UNIT_MOD_CAST_HASTE, spellHaste);
    }

    public final void setModHaste(float haste) {
        setFloatValue(UNIT_FIELD_MOD_HASTE, haste);
    }

    public final void setModRangedHaste(float rangedHaste) {
        setFloatValue(UNIT_FIELD_MOD_RANGED_HASTE, rangedHaste);
    }

    public final void setModHasteRegen(float hasteRegen) {
        setFloatValue(UNIT_FIELD_MOD_HASTE_REGEN, hasteRegen);
    }

    public final void setModTimeRate(float timeRate) {
        setFloatValue(UNIT_FIELD_MOD_TIME_RATE, timeRate);
    }

    public final boolean hasUnitFlag(UnitFlag... flags) {
        return getUnitFlag().hasAnyFlag(flags);
    }

    public final boolean hasUnitFlag(UnitFlag flags) {
        return hasFlag(UNIT_FIELD_FLAGS, flags);
    }

    public final EnumFlag<UnitFlag> getUnitFlag() {
        return EnumFlag.of(UnitFlag.class, getInt32Value(UNIT_FIELD_FLAGS));
    }

    public final void setUnitFlag(UnitFlag flags) {
        setFlag(UNIT_FIELD_FLAGS, flags);
    }

    public final void setUnitFlag(UnitFlag... flags) {
        for (UnitFlag flag : flags) {
            setFlag(UNIT_FIELD_FLAGS, flag);
        }
    }

    public final void removeUnitFlag(UnitFlag... flags) {
        for (UnitFlag flag : flags) {
            removeFlag(UNIT_FIELD_FLAGS, flag);
        }

    }

    public final void removeUnitFlag(UnitFlag flags) {
        removeFlag(UNIT_FIELD_FLAGS, flags);
    }

    public final void replaceAllUnitFlags(UnitFlag flags) {
        toggleFlag(UNIT_FIELD_FLAGS, flags);
    }

    public final boolean hasUnitFlag2(UnitFlag2 flags) {
        return hasFlag(UNIT_FIELD_FLAGS_2, flags);
    }

    public final EnumFlag<UnitFlag2> getUnitFlag2() {
        return EnumFlag.of(UnitFlag2.class, getInt32Value(UNIT_FIELD_FLAGS_2));
    }

    public final void setUnitFlag2(UnitFlag2 flags) {
        setFlag(UNIT_FIELD_FLAGS_2, flags);
    }

    public final void removeUnitFlag2(UnitFlag2 flags) {
        removeFlag(UNIT_FIELD_FLAGS_2, flags);
    }

    public final void replaceAllUnitFlags2(UnitFlag2 flags) {
        toggleFlag(UNIT_FIELD_FLAGS_2, flags);
    }

    public final boolean hasUnitFlag3(UnitFlag3 flags) {
        return hasFlag(UNIT_FIELD_FLAGS_3, flags);
    }

    public final EnumFlag<UnitFlag3> getUnitFlag3() {
        return EnumFlag.of(UnitFlag3.class, getInt32Value(UNIT_FIELD_FLAGS_3));
    }

    public final void setUnitFlag3(UnitFlag3 flags) {
        setFlag(UNIT_FIELD_FLAGS_3, flags);
    }

    public final void removeUnitFlag3(UnitFlag3 flags) {
        removeFlag(UNIT_FIELD_FLAGS_3, flags);
    }

    public final void replaceAllUnitFlags3(UnitFlag3 flags) {
        toggleFlag(UNIT_FIELD_FLAGS_3, flags);
    }

    public final void setCreatedBySpell(int spellId) {
        setInt32Value(UNIT_CREATED_BY_SPELL, spellId);
    }

    public final Emote GetEmoteState() { return Emote(getInt32Value(UNIT_NPC_EMOTE_STATE)); }
    public final void setEmoteState(Emote emote) {
        setInt32Value(UNIT_NPC_EMOTE_STATE, emote);
    }

    public final SheathState getSheath() {
        return SheathState(getByteValue(UNIT_FIELD_BYTES_2, UNIT_BYTES_2_OFFSET_SHEATH_STATE));
    }

    public final void deMorph() {
        setDisplayId(getNativeDisplayId());
    }

    public final boolean hasUnitTypeMask(UnitTypeMask mask) {
        return (boolean) (mask.getValue() & getUnitTypeMask().getValue());
    }

    public final void addUnitTypeMask(UnitTypeMask mask) {
        setUnitTypeMask(UnitTypeMask.forValue(getUnitTypeMask().getValue() | mask.getValue()));
    }

    public final void addUnitState(UnitState f) {
        state.addFlag(f);
    }

    public final boolean hasUnitState(UnitState f) {
        return state.hasFlag(f);
    }

    public final boolean hasUnitState(UnitState... f) {
        return state.hasAnyFlag(f);
    }

    public final void clearUnitState(UnitState f) {
        state.removeFlag(f);
    }

    @Override
    public boolean isAlwaysVisibleFor(WorldObject seer) {
        if (super.isAlwaysVisibleFor(seer)) {
            return true;
        }

        // Always seen by owner
        var guid = getCharmerOrOwnerGUID();

        if (!guid.isEmpty()) {
            if (Objects.equals(seer.getGUID(), guid)) {
                return true;
            }
        }

        var seerPlayer = seer.toPlayer();

        if (seerPlayer != null) {
            var owner = getOwnerUnit();

            if (owner != null) {
                var ownerPlayer = owner.toPlayer();

                if (ownerPlayer) {
                    if (ownerPlayer.isGroupVisibleFor(seerPlayer)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public final void restoreFaction() {
        if (hasAuraType(AuraType.ModFaction)) {
            setFaction((int) getAuraEffectsByType(AuraType.ModFaction).LastOrDefault().miscValue);

            return;
        }

        if (isTypeId(TypeId.PLAYER)) {
            toPlayer().setFactionForRace(getRace());
        } else {
            if (hasUnitTypeMask(UnitTypeMask.minion)) {
                var owner = getOwnerUnit();

                if (owner) {
                    setFaction(owner.getFaction());

                    return;
                }
            }

            var cinfo = toCreature().getCreatureTemplate();

            if (cinfo != null) // normal creature
            {
                setFaction(cinfo.faction);
            }
        }
    }

    public final boolean isInPartyWith(Unit unit) {
        if (this == unit) {
            return true;
        }

        var u1 = getCharmerOrOwnerOrSelf();
        var u2 = unit.getCharmerOrOwnerOrSelf();

        if (u1 == u2) {
            return true;
        }

        if (u1.isTypeId(TypeId.PLAYER) && u2.isTypeId(TypeId.PLAYER)) {
            return u1.toPlayer().isInSameGroupWith(u2.toPlayer());
        } else if ((u2.isTypeId(TypeId.PLAYER) && u1.isTypeId(TypeId.UNIT) && u1.toCreature().getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.TreatAsRaidUnit)) || (u1.isTypeId(TypeId.PLAYER) && u2.isTypeId(TypeId.UNIT) && u2.toCreature().getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.TreatAsRaidUnit))) {
            return true;
        }

        return u1.getObjectTypeId() == TypeId.UNIT && u2.getObjectTypeId() == TypeId.UNIT && u1.getFaction() == u2.getFaction();
    }

    public final boolean isInRaidWith(Unit unit) {
        if (this == unit) {
            return true;
        }

        var u1 = getCharmerOrOwnerOrSelf();
        var u2 = unit.getCharmerOrOwnerOrSelf();

        if (u1 == u2) {
            return true;
        }

        if (u1.isTypeId(TypeId.PLAYER) && u2.isTypeId(TypeId.PLAYER)) {
            return u1.toPlayer().isInSameRaidWith(u2.toPlayer());
        } else if ((u2.isTypeId(TypeId.PLAYER) && u1.isTypeId(TypeId.UNIT) && u1.toCreature().getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.TREAT_AS_RAID_UNIT))
                || (u1.isTypeId(TypeId.PLAYER) && u2.isTypeId(TypeId.UNIT) && u2.toCreature().getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.TREAT_AS_RAID_UNIT))) {
            return true;
        }

        // else u1.getTypeId() == u2.getTypeId() == TYPEID_UNIT
        return u1.getFaction() == u2.getFaction();
    }

    public final void getPartyMembers(ArrayList<Unit> TagUnitMap) {
        var owner = getCharmerOrOwnerOrSelf();
        PlayerGroup group = null;

        if (owner.getObjectTypeId() == TypeId.PLAYER) {
            group = owner.toPlayer().getGroup();
        }

        if (group != null) {
            var subgroup = owner.toPlayer().getSubGroup();

            for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                var target = refe.getSource();

                // IsHostileTo check duel and controlled by enemy
                if (target != null && target.isInMap(owner) && target.getSubGroup() == subgroup && !isHostileTo(target)) {
                    if (target.isAlive()) {
                        TagUnitMap.add(target);
                    }

                    var pet = target.getGuardianPet();

                    if (target.getGuardianPet()) {
                        if (pet.isAlive()) {
                            TagUnitMap.add(pet);
                        }
                    }
                }
            }
        } else {
            if ((owner == this || isInMap(owner)) && owner.isAlive()) {
                TagUnitMap.add(owner);
            }

            var pet = owner.getGuardianPet();

            if (owner.getGuardianPet() != null) {
                if ((pet == this || isInMap(pet)) && pet.isAlive()) {
                    TagUnitMap.add(pet);
                }
            }
        }
    }

    public final void setVisFlag(UnitVisFlags flags) {
        setUpdateFieldFlagValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().visFlags), (byte) flags.getValue());
    }

    public final void removeVisFlag(UnitVisFlags flags) {
        removeUpdateFieldFlagValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().visFlags), (byte) flags.getValue());
    }

    public final void replaceAllVisFlags(UnitVisFlags flags) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().visFlags), (byte) flags.getValue());
    }

    public final void setStandState(UnitStandStateType state, int animKitId) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().standState), (byte) state.getValue());

        if (isStandState()) {
            removeAurasWithInterruptFlags(SpellAuraInterruptFlag.standing);
        }

        if (isTypeId(TypeId.PLAYER)) {
            StandStateUpdate packet = new StandStateUpdate(state, animKitId);
            toPlayer().sendPacket(packet);
        }
    }

    public final void setAnimTier(AnimTier animTier, boolean notifyClient) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().animTier), (byte) animTier.getValue());

        if (notifyClient) {
            SetAnimTier setAnimTier = new setAnimTier();
            setAnimTier.unit = getGUID();
            setAnimTier.tier = animTier.getValue();
            sendMessageToSet(setAnimTier, true);
        }
    }

    public final void setChannelVisual(SpellCastVisualField channelVisual) {
        UnitChannel unitChannel = getValues().modifyValue(getUnitData()).modifyValue(getUnitData().channelData);
        tangible.RefObject<SpellCastVisualField> tempRef_SpellVisual = new tangible.RefObject<SpellCastVisualField>(unitChannel.spellVisual);
        setUpdateFieldValue(tempRef_SpellVisual, channelVisual);
        unitChannel.spellVisual = tempRef_SpellVisual.refArgValue;
    }

    public final void addChannelObject(ObjectGuid guid) {
        addDynamicUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().channelObjects), guid);
    }

    public final void setChannelObject(int slot, ObjectGuid guid) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().channelObjects, slot), guid);
    }

    public final void clearChannelObjects() {
        clearDynamicUpdateFieldValues(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().channelObjects));
    }

    public final void removeChannelObject(ObjectGuid guid) {
        var index = getUnitData().channelObjects.FindIndex(guid);

        if (index >= 0) {
            removeDynamicUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().channelObjects), index);
        }
    }

    @Override
    public UpdateFieldFlag getUpdateFieldFlagsFor(Player target) {
        var flags = UpdateFieldFlag.NONE;

        if (target == this || Objects.equals(getOwnerGUID(), target.getGUID())) {
            flags = UpdateFieldFlag.forValue(flags.getValue() | UpdateFieldFlag.owner.getValue());
        }

        if (hasDynamicFlag(UnitDynFlag.SpecialInfo)) {
            if (hasAuraTypeWithCaster(AuraType.Empathy, target.getGUID())) {
                flags = UpdateFieldFlag.forValue(flags.getValue() | UpdateFieldFlag.Empath.getValue());
            }
        }

        return flags;
    }

    @Override
    public void buildValuesCreate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt8((byte) flags.getValue());
        getObjectData().writeCreate(buffer, flags, this, target);
        getUnitData().writeCreate(buffer, flags, this, target);

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void buildValuesUpdate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt32(getValues().getChangedObjectTypeMask());

        if (getValues().hasChanged(TypeId.object)) {
            getObjectData().writeUpdate(buffer, flags, this, target);
        }

        if (getValues().hasChanged(TypeId.UNIT)) {
            getUnitData().writeUpdate(buffer, flags, this, target);
        }

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void buildValuesUpdateWithFlag(WorldPacket data, UpdateFieldFlag flags, Player target) {
        UpdateMask valuesMask = new UpdateMask(14);
        valuesMask.set(getObjectTypeId().unit.getValue());

        WorldPacket buffer = new WorldPacket();

        UpdateMask mask = new UpdateMask(194);
        getUnitData().appendAllowedFieldsMaskForFlag(mask, flags);
        getUnitData().writeUpdate(buffer, mask, true, this, target);

        data.writeInt32(buffer.getSize());
        data.writeInt32(valuesMask.getBlock(0));
        data.writeBytes(buffer);
    }

    public final void buildValuesUpdateForPlayerWithMask(UpdateData data, UpdateMask requestedObjectMask, UpdateMask requestedUnitMask, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        UpdateMask valuesMask = new UpdateMask(getObjectTypeId().max.getValue());

        if (requestedObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().object.getValue());
        }

        getUnitData().filterDisallowedFieldsMaskForFlag(requestedUnitMask, flags);

        if (requestedUnitMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().unit.getValue());
        }

        WorldPacket buffer = new WorldPacket();
        buffer.writeInt32(valuesMask.getBlock(0));

        if (valuesMask.get(getObjectTypeId().object.getValue())) {
            getObjectData().writeUpdate(buffer, requestedObjectMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().unit.getValue())) {
            getUnitData().writeUpdate(buffer, requestedUnitMask, true, this, target);
        }

        WorldPacket buffer1 = new WorldPacket();
        buffer1.writeInt8((byte) UpdateType.VALUES.getValue());
        buffer1.writeGuid(getGUID());
        buffer1.writeInt32(buffer.getSize());
        buffer1.writeBytes(buffer.getByteBuf());

        data.addUpdateBlock(buffer1);
    }

    @Override
    public void clearUpdateMask(boolean remove) {
        getValues().clearChangesMask(getUnitData());
        super.clearUpdateMask(remove);
    }

    @Override
    public void destroyForPlayer(Player target) {
        var bg = target.getBattleground();

        if (bg != null) {
            if (bg.isArena()) {
                DestroyArenaUnit destroyArenaUnit = new DestroyArenaUnit();
                destroyArenaUnit.guid = getGUID();
                target.sendPacket(destroyArenaUnit);
            }
        }

        super.destroyForPlayer(target);
    }

    public void setCanDualWield(boolean value) {
        canDualWield = value;
    }

    public final boolean haveOffhandWeapon() {
        if (isTypeId(TypeId.PLAYER)) {
            return toPlayer().getWeaponForAttack(WeaponAttackType.OFF_ATTACK, true) != null;
        } else {
            return canDualWield;
        }
    }

    public final long modifyHealth(double dval) {
        return modifyHealth((long) dval);
    }

    public final long modifyHealth(long dVal) {
        long gain = 0;

        if (dVal == 0) {
            return 0;
        }

        synchronized (healthLock) {
            var curHealth = getHealth();

            var val = dVal + curHealth;

            if (val <= 0) {
                setHealth(0);

                return -curHealth;
            }

            var maxHealth = getMaxHealth();

            if (val < maxHealth) {
                setHealth(val);
                gain = val - curHealth;
            } else if (curHealth != maxHealth) {
                setHealth(maxHealth);
                gain = maxHealth - curHealth;
            }
        }

        if (dVal < 0) {
            HealthUpdate packet = new HealthUpdate();
            packet.guid = getGUID();
            packet.health = getHealth();

            var player = getCharmerOrOwnerPlayerOrPlayerItself();

            if (player) {
                player.sendPacket(packet);
            }
        }

        return gain;
    }

    public final long getHealthGain(double dVal) {
        return getHealthGain((long) dVal);
    }

    public final long getHealthGain(long dVal) {
        long gain = 0;

        if (dVal == 0) {
            return 0;
        }

        var curHealth = getHealth();

        var val = dVal + curHealth;

        if (val <= 0) {
            return -curHealth;
        }

        var maxHealth = getMaxHealth();

        if (val < maxHealth) {
            gain = dVal;
        } else if (curHealth != maxHealth) {
            gain = maxHealth - curHealth;
        }

        return gain;
    }

    public final boolean isImmuneToAll() {
        return isImmuneToPC() && isImmuneToNPC();
    }

    public void setImmuneToAll(boolean apply) {
        setImmuneToAll(apply, false);
    }

    public final void setImmuneToAll(boolean apply, boolean keepCombat) {
        if (apply) {
            setUnitFlag(UnitFlag.ImmuneToPc.getValue() | UnitFlag.ImmuneToNpc.getValue());
            validateAttackersAndOwnTarget();

            if (!keepCombat) {
                combatManager.endAllCombat();
            }
        } else {
            removeUnitFlag(UnitFlag.ImmuneToPc.getValue() | UnitFlag.ImmuneToNpc.getValue());
        }
    }

    public final boolean isImmuneToPC() {
        return hasUnitFlag(UnitFlag.ImmuneToPc);
    }

    public void setImmuneToPC(boolean apply) {
        setImmuneToPC(apply, false);
    }

    public final void setImmuneToPC(boolean apply, boolean keepCombat) {
        if (apply) {
            setUnitFlag(UnitFlag.ImmuneToPc);
            validateAttackersAndOwnTarget();

            if (!keepCombat) {
                ArrayList<CombatReference> toEnd = new ArrayList<>();

                for (var pair : combatManager.getPvECombatRefs().entrySet()) {
                    if (pair.getValue().getOther(this).hasUnitFlag(UnitFlag.PlayerControlled)) {
                        toEnd.add(pair.getValue());
                    }
                }

                for (var pair : combatManager.getPvPCombatRefs().entrySet()) {
                    if (pair.getValue().getOther(this).hasUnitFlag(UnitFlag.PlayerControlled)) {
                        toEnd.add(pair.getValue());
                    }
                }

                for (var refe : toEnd) {
                    refe.endCombat();
                }
            }
        } else {
            removeUnitFlag(UnitFlag.ImmuneToPc);
        }
    }

    public final boolean isImmuneToNPC() {
        return hasUnitFlag(UnitFlag.ImmuneToNpc);
    }

    public void setImmuneToNPC(boolean apply) {
        setImmuneToNPC(apply, false);
    }

    public final void setImmuneToNPC(boolean apply, boolean keepCombat) {
        if (apply) {
            setUnitFlag(UnitFlag.ImmuneToNpc);
            validateAttackersAndOwnTarget();

            if (!keepCombat) {
                ArrayList<CombatReference> toEnd = new ArrayList<>();

                for (var pair : combatManager.getPvECombatRefs().entrySet()) {
                    if (!pair.getValue().getOther(this).hasUnitFlag(UnitFlag.PlayerControlled)) {
                        toEnd.add(pair.getValue());
                    }
                }

                for (var pair : combatManager.getPvPCombatRefs().entrySet()) {
                    if (!pair.getValue().getOther(this).hasUnitFlag(UnitFlag.PlayerControlled)) {
                        toEnd.add(pair.getValue());
                    }
                }

                for (var refe : toEnd) {
                    refe.endCombat();
                }
            }
        } else {
            removeUnitFlag(UnitFlag.ImmuneToNpc);
        }
    }

    public float getBlockPercent(int attackerLevel) {
        return 30.0f;
    }

    public final void rewardRage(int baseRage) {
        double addRage = baseRage;

        // talent who gave more rage on attack
        tangible.RefObject<Double> tempRef_addRage = new tangible.RefObject<Double>(addRage);
        MathUtil.AddPct(tempRef_addRage, getTotalAuraModifier(AuraType.ModRageFromDamageDealt));
        addRage = tempRef_addRage.refArgValue;

        addRage *= WorldConfig.getFloatValue(WorldCfg.RatePowerRageIncome);

        modifyPower(powerType.Rage, (int) (addRage * 10));
    }

    public final float getPPMProcChance(int WeaponSpeed, float PPM, SpellInfo spellProto) {
        // proc per minute chance calculation
        if (PPM <= 0) {
            return 0.0f;
        }

        // Apply chance modifer aura
        if (spellProto != null) {
            var modOwner = getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Float> tempRef_PPM = new tangible.RefObject<Float>(PPM);
                modOwner.applySpellMod(spellProto, SpellModOp.ProcFrequency, tempRef_PPM);
                PPM = tempRef_PPM.refArgValue;
            }
        }

        return (float) Math.floor((WeaponSpeed * PPM) / 600.0f); // result is chance in percents (probability = Speed_in_sec * (PPM / 60))
    }

    public final Unit getNextRandomRaidMemberOrPet(float radius) {
        Player player = null;

        if (isTypeId(TypeId.PLAYER)) {
            player = toPlayer();
        }
        // Should we enable this also for charmed units?
        else if (isTypeId(TypeId.UNIT) && isPet()) {
            player = getOwnerUnit().toPlayer();
        }

        if (player == null) {
            return null;
        }

        var group = player.getGroup();

        // When there is no group check pet presence
        if (!group) {
            // We are pet now, return owner
            if (player != this) {
                return isWithinDistInMap(player, radius) ? player : null;
            }

            Unit pet = getGuardianPet();

            // No pet, no group, nothing to return
            if (pet == null) {
                return null;
            }

            // We are owner now, return pet
            return isWithinDistInMap(pet, radius) ? pet : null;
        }

        ArrayList<Unit> nearMembers = new ArrayList<>();
        // reserve place for players and pets because resizing vector every unit push is unefficient (vector is reallocated then)

        for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
            var target = refe.getSource();

            if (target) {
                // IsHostileTo check duel and controlled by enemy
                if (target != this && isWithinDistInMap(target, radius) && target.isAlive() && !isHostileTo(target)) {
                    nearMembers.add(target);
                }

                // Push player's pet to vector
                Unit pet = target.getGuardianPet();

                if (pet) {
                    if (pet != this && isWithinDistInMap(pet, radius) && pet.isAlive() && !isHostileTo(pet)) {
                        nearMembers.add(pet);
                    }
                }
            }
        }

        if (nearMembers.isEmpty()) {
            return null;
        }

        var randTarget = RandomUtil.IRand(0, nearMembers.size() - 1);

        return nearMembers.get(randTarget);
    }

    public final int getComboPoints() {
        return getPower(Power.COMBO_POINTS);
    }

    public final void addComboPoints(byte count) {
        addComboPoints(count, null);
    }

    public final void addComboPoints(byte count, Spell spell) {
        if (count == 0) {
            return;
        }

        var comboPoints = (byte) (spell != null ? spell.ComboPointGain : getPower(powerType.ComboPoints));

        comboPoints += count;

        if (comboPoints > 5) {
            comboPoints = 5;
        } else if (comboPoints < 0) {
            comboPoints = 0;
        }

        if (!spell) {
            setPower(powerType.ComboPoints, comboPoints);
        } else {
            spell.comboPointGain = comboPoints;
        }
    }

    public final void clearComboPoints() {
        setPower(powerType.ComboPoints, 0);
    }

    public final void clearAllReactives() {
        for (ReactiveType i = 0; i.getValue() < ReactiveType.max.getValue(); ++i) {
            reactiveTimer.put(i, 0);
        }

        if (hasAuraState(AuraStateType.Defensive)) {
            modifyAuraState(AuraStateType.Defensive, false);
        }

        if (hasAuraState(AuraStateType.Defensive2)) {
            modifyAuraState(AuraStateType.Defensive2, false);
        }
    }

    public final double meleeDamageBonusDone(Unit victim, double damage, WeaponAttackType attType, DamageEffectType damagetype, SpellInfo spellProto, SpellEffectInfo spellEffectInfo) {
        return meleeDamageBonusDone(victim, damage, attType, damagetype, spellProto, spellEffectInfo, spellSchoolMask.NORMAL);
    }

    public final double meleeDamageBonusDone(Unit victim, double damage, WeaponAttackType attType, DamageEffectType damagetype, SpellInfo spellProto) {
        return meleeDamageBonusDone(victim, damage, attType, damagetype, spellProto, null, spellSchoolMask.NORMAL);
    }

    public final double meleeDamageBonusDone(Unit victim, double damage, WeaponAttackType attType, DamageEffectType damagetype) {
        return meleeDamageBonusDone(victim, damage, attType, damagetype, null, null, spellSchoolMask.NORMAL);
    }

    public final double meleeDamageBonusDone(Unit victim, double damage, WeaponAttackType attType, DamageEffectType damagetype, SpellInfo spellProto, SpellEffectInfo spellEffectInfo, SpellSchoolMask damageSchoolMask) {
        if (victim == null || damage == 0) {
            return 0;
        }

        var creatureTypeMask = victim.getCreatureTypeMask();

        // Done fixed damage bonus auras
        double DoneFlatBenefit = 0;

        // ..done
        DoneFlatBenefit += getTotalAuraModifierByMiscMask(AuraType.ModDamageDoneCreature, (int) creatureTypeMask);

        // ..done
        // SPELL_AURA_MOD_DAMAGE_DONE included in weapon damage

        // ..done (base at attack power for marked target and base at attack power for creature type)
        double APbonus = 0;

        if (attType == WeaponAttackType.RangedAttack) {
            APbonus += victim.getTotalAuraModifier(AuraType.RangedAttackPowerAttackerBonus);

            // ..done (base at attack power and creature type)
            APbonus += getTotalAuraModifierByMiscMask(AuraType.ModRangedAttackPowerVersus, (int) creatureTypeMask);
        } else {
            APbonus += victim.getTotalAuraModifier(AuraType.MeleeAttackPowerAttackerBonus);

            // ..done (base at attack power and creature type)
            APbonus += getTotalAuraModifierByMiscMask(AuraType.ModMeleeAttackPowerVersus, (int) creatureTypeMask);
        }

        if (APbonus != 0) // Can be negative
        {
            var normalized = spellProto != null && spellProto.hasEffect(SpellEffectName.NormalizedWeaponDmg);
            DoneFlatBenefit += (int) (APbonus / 3.5f * getAPMultiplier(attType, normalized));
        }

        // Done total percent damage auras
        double DoneTotalMod = 1.0f;

        var schoolMask = spellProto != null ? spellProto.getSchoolMask() : damageSchoolMask;

        if ((schoolMask.getValue() & spellSchoolMask.NORMAL.getValue()) == 0) {
            // Some spells don't benefit from pct done mods
            // mods for SPELL_SCHOOL_MASK_NORMAL are already factored in base melee damage calculation
            if (spellProto == null || !spellProto.hasAttribute(SpellAttr6.IgnoreCasterDamageModifiers)) {
                double maxModDamagePercentSchool = 0.0f;
                var thisPlayer = toPlayer();

                if (thisPlayer != null) {
                    for (var i = SpellSchool.Holy; i.getValue() < SpellSchool.max.getValue(); ++i) {
                        if ((boolean) (schoolMask.getValue() & (1 << i.getValue()))) {
                            maxModDamagePercentSchool = Math.max(maxModDamagePercentSchool, thisPlayer.getActivePlayerData().modDamageDonePercent.get(i.getValue()));
                        }
                    }
                } else {
                    maxModDamagePercentSchool = getTotalAuraMultiplierByMiscMask(AuraType.ModDamagePercentDone, (int) schoolMask.getValue());
                }

                DoneTotalMod *= maxModDamagePercentSchool;
            }
        }

        if (spellProto == null) {
            // melee attack
            for (var autoAttackDamage : getAuraEffectsByType(AuraType.ModAutoAttackDamage)) {
                tangible.RefObject<Double> tempRef_DoneTotalMod = new tangible.RefObject<Double>(DoneTotalMod);
                MathUtil.AddPct(tempRef_DoneTotalMod, autoAttackDamage.getAmount());
                DoneTotalMod = tempRef_DoneTotalMod.refArgValue;
            }
        }

        DoneTotalMod *= getTotalAuraMultiplierByMiscMask(AuraType.ModDamageDoneVersus, creatureTypeMask);

        // bonus against aurastate
        DoneTotalMod *= getTotalAuraMultiplier(AuraType.ModDamageDoneVersusAurastate, aurEff ->
        {
            if (victim.hasAuraState(AuraStateType.forValue(aurEff.miscValue))) {
                return true;
            }

            return false;
        });

        // Add SPELL_AURA_MOD_DAMAGE_DONE_FOR_MECHANIC percent bonus
        if (spellEffectInfo != null && spellEffectInfo.mechanic != 0) {
            tangible.RefObject<Double> tempRef_DoneTotalMod2 = new tangible.RefObject<Double>(DoneTotalMod);
            MathUtil.AddPct(tempRef_DoneTotalMod2, getTotalAuraModifierByMiscValue(AuraType.ModDamageDoneForMechanic, spellEffectInfo.mechanic.getValue()));
            DoneTotalMod = tempRef_DoneTotalMod2.refArgValue;
        } else if (spellProto != null && spellProto.getMechanic() != 0) {
            tangible.RefObject<Double> tempRef_DoneTotalMod3 = new tangible.RefObject<Double>(DoneTotalMod);
            MathUtil.AddPct(tempRef_DoneTotalMod3, getTotalAuraModifierByMiscValue(AuraType.ModDamageDoneForMechanic, spellProto.getMechanic().getValue()));
            DoneTotalMod = tempRef_DoneTotalMod3.refArgValue;
        }

        var damageF = damage;

        // apply spellmod to Done damage
        if (spellProto != null) {
            var modOwner = getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Double> tempRef_damageF = new tangible.RefObject<Double>(damageF);
                modOwner.applySpellMod(spellProto, damagetype == DamageEffectType.DOT ? SpellModOp.PeriodicHealingAndDamage : SpellModOp.HealingAndDamage, tempRef_damageF);
                damageF = tempRef_damageF.refArgValue;
            }
        }

        damageF = (damageF + DoneFlatBenefit) * DoneTotalMod;

        // bonus result can be negative
        return Math.max(damageF, 0.0f);
    }

    public final double meleeDamageBonusTaken(Unit attacker, double pdamage, WeaponAttackType attType, DamageEffectType damagetype, SpellInfo spellProto) {
        return meleeDamageBonusTaken(attacker, pdamage, attType, damagetype, spellProto, spellSchoolMask.NORMAL);
    }

    public final double meleeDamageBonusTaken(Unit attacker, double pdamage, WeaponAttackType attType, DamageEffectType damagetype) {
        return meleeDamageBonusTaken(attacker, pdamage, attType, damagetype, null, spellSchoolMask.NORMAL);
    }

    public final double meleeDamageBonusTaken(Unit attacker, double pdamage, WeaponAttackType attType, DamageEffectType damagetype, SpellInfo spellProto, SpellSchoolMask damageSchoolMask) {
        if (pdamage == 0) {
            return 0;
        }

        double TakenFlatBenefit = 0;

        // ..taken
        TakenFlatBenefit += getTotalAuraModifierByMiscMask(AuraType.ModDamageTaken, attacker.getMeleeDamageSchoolMask().getValue());

        if (attType != WeaponAttackType.RangedAttack) {
            TakenFlatBenefit += getTotalAuraModifier(AuraType.ModMeleeDamageTaken);
        } else {
            TakenFlatBenefit += getTotalAuraModifier(AuraType.ModRangedDamageTaken);
        }

        if ((TakenFlatBenefit < 0) && (pdamage < -TakenFlatBenefit)) {
            return 0;
        }

        // Taken total percent damage auras
        double TakenTotalMod = 1.0f;

        // ..taken
        TakenTotalMod *= getTotalAuraMultiplierByMiscMask(AuraType.ModDamagePercentTaken, (int) attacker.getMeleeDamageSchoolMask().getValue());

        // .. taken pct (special attacks)
        if (spellProto != null) {
            // From caster spells
            TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModSchoolMaskDamageFromCaster, aurEff ->
            {
                return Objects.equals(aurEff.casterGuid, attacker.getGUID()) && (aurEff.miscValue & spellProto.getSchoolMask().getValue()) != 0;
            });

            TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModSpellDamageFromCaster, aurEff ->
            {
                return Objects.equals(aurEff.casterGuid, attacker.getGUID()) && aurEff.isAffectingSpell(spellProto);
            });

            // Mod damage from spell mechanic
            var mechanicMask = spellProto.getAllEffectsMechanicMask();

            // Shred, Maul - "Effects which increase Bleed damage also increase Shred damage"
            if (spellProto.getSpellFamilyName() == SpellFamilyName.Druid && spellProto.getSpellFamilyFlags().get(0).hasFlag(0x00008800)) {
                mechanicMask |= (1 << mechanics.Bleed.getValue());
            }

            if (mechanicMask != 0) {
                TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModMechanicDamageTakenPercent, aurEff ->
                {
                    if ((mechanicMask & (1 << (aurEff.miscValue))) != 0) {
                        return true;
                    }

                    return false;
                });
            }

            if (damagetype == DamageEffectType.DOT) {
                TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModPeriodicDamageTaken, aurEff -> (aurEff.miscValue & (int) spellProto.getSchoolMask().getValue()) != 0);
            }
        } else // melee attack
        {
            TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModMeleeDamageFromCaster, aurEff ->
            {
                return Objects.equals(aurEff.casterGuid, attacker.getGUID());
            });
        }

        var cheatDeath = getAuraEffect(45182, 0);

        if (cheatDeath != null) {
            tangible.RefObject<Double> tempRef_TakenTotalMod = new tangible.RefObject<Double>(TakenTotalMod);
            MathUtil.AddPct(tempRef_TakenTotalMod, cheatDeath.amount);
            TakenTotalMod = tempRef_TakenTotalMod.refArgValue;
        }

        if (attType != WeaponAttackType.RangedAttack) {
            TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModMeleeDamageTakenPct);
        } else {
            TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModRangedDamageTakenPct);
        }

        // Versatility
        var modOwner = getSpellModOwner();

        if (modOwner) {
            // only 50% of SPELL_AURA_MOD_VERSATILITY for damage reduction
            var versaBonus = modOwner.getTotalAuraModifier(AuraType.ModVersatility) / 2.0f;
            tangible.RefObject<Double> tempRef_TakenTotalMod2 = new tangible.RefObject<Double>(TakenTotalMod);
            MathUtil.AddPct(tempRef_TakenTotalMod2, -(modOwner.getRatingBonusValue(CombatRating.VersatilityDamageTaken) + versaBonus));
            TakenTotalMod = tempRef_TakenTotalMod2.refArgValue;
        }

        // Sanctified wrath (bypass damage reduction)
        if (TakenTotalMod < 1.0f) {
            var attackSchoolMask = spellProto != null ? spellProto.getSchoolMask() : damageSchoolMask;

            var damageReduction = 1.0f - TakenTotalMod;
            var casterIgnoreResist = attacker.getAuraEffectsByType(AuraType.ModIgnoreTargetResist);

            for (var aurEff : casterIgnoreResist) {
                if ((aurEff.getMiscValue() & attackSchoolMask.getValue()) == 0) {
                    continue;
                }

                tangible.RefObject<Double> tempRef_damageReduction = new tangible.RefObject<Double>(damageReduction);
                MathUtil.AddPct(tempRef_damageReduction, -aurEff.getAmount());
                damageReduction = tempRef_damageReduction.refArgValue;
            }

            TakenTotalMod = 1.0f - damageReduction;
        }

        var tmpDamage = (pdamage + TakenFlatBenefit) * TakenTotalMod;

        return Math.max(tmpDamage, 0.0f);
    }

    public final void saveDamageHistory(double damage) {
        var currentTime = gameTime.GetDateAndTime();
        var maxPastTime = currentTime - MAX_DAMAGE_HISTORY_DURATION;

        // Remove damages older than maxPastTime, can be increased if required
        for (var kvp : getDamageTakenHistory()) {
            if (kvp.key < maxPastTime) {
                getDamageTakenHistory().QueueRemove(kvp.key);
            } else {
                break;
            }
        }

        getDamageTakenHistory().ExecuteRemove();

        TVal existing;
        tangible.OutObject<TVal> tempOut_existing = new tangible.OutObject<TVal>();
        getDamageTakenHistory().TryGetValue(currentTime, tempOut_existing);
        existing = tempOut_existing.outArgValue;
        getDamageTakenHistory().set(currentTime, existing + damage);
    }

    public final double getDamageOverLastSeconds(int seconds) {
        var maxPastTime = gameTime.GetDateAndTime() - duration.FromSeconds(seconds);

        double damageOverLastSeconds = 0;

        for (var itr : getDamageTakenHistory()) {
            if (itr.key >= maxPastTime) {
                damageOverLastSeconds += itr.value;
            } else {
                break;
            }
        }

        return damageOverLastSeconds;
    }

    public SpellSchoolMask getMeleeDamageSchoolMask() {
        return getMeleeDamageSchoolMask(WeaponAttackType.BASE_ATTACK);
    }

    public SpellSchoolMask getMeleeDamageSchoolMask(WeaponAttackType attackType) {
        return spellSchoolMask.NONE;
    }

    public void updateDamageDoneMods(WeaponAttackType attackType) {
        updateDamageDoneMods(attackType, -1);
    }

    public void updateDamageDoneMods(WeaponAttackType attackType, int skipEnchantSlot) {
        var unitMod = switch (attackType) {
            case BaseAttack -> UnitMod.DamageMainHand.getValue();
            case OffAttack -> UnitMod.DamageOffHand.getValue();
            case RangedAttack -> UnitMod.DamageRanged.getValue();

            default -> throw new UnsupportedOperationException();
        };

        var amount = getTotalAuraModifier(AuraType.ModDamageDone, aurEff ->
        {
            if ((aurEff.miscValue & spellSchoolMask.NORMAL.getValue()) == 0) {
                return false;
            }

            return checkAttackFitToAuraRequirement(attackType, aurEff);
        });

        setStatFlatModifier(unitMod, UnitModifierFlatType.Total, amount);
    }

    public final void updateAllDamageDoneMods() {
        for (var attackType = WeaponAttackType.BASE_ATTACK; attackType.getValue() < WeaponAttackType.max.getValue(); ++attackType) {
            updateDamageDoneMods(attackType);
        }
    }

    public final void updateDamagePctDoneMods(WeaponAttackType attackType) {

//		(UnitMods unitMod, double factor) = switch(attackType)
//			{
//				case WeaponAttackType.BASE_ATTACK -> (UnitMods.DamageMainHand, 1.0f);
//				case WeaponAttackType.OFF_ATTACK -> (UnitMods.DamageOffHand, 0.5f);
//				case WeaponAttackType.RangedAttack -> (UnitMods.DamageRanged, 1.0f);
//				default -> throw new NotImplementedException();
//			};

        factor *= getTotalAuraMultiplier(AuraType.ModDamagePercentDone, aurEff ->
        {
            if (!aurEff.miscValue.hasFlag(spellSchoolMask.NORMAL.getValue())) {
                return false;
            }

            return checkAttackFitToAuraRequirement(attackType, aurEff);
        });

        if (attackType == WeaponAttackType.OFF_ATTACK) {
            factor *= getTotalAuraMultiplier(AuraType.ModOffhandDamagePct, auraEffect -> checkAttackFitToAuraRequirement(attackType, auraEffect));
        }

        setStatPctModifier(unitMod, UnitModifierPctType.Total, factor);
    }

    public final void updateAllDamagePctDoneMods() {
        for (var attackType = WeaponAttackType.BASE_ATTACK; attackType.getValue() < WeaponAttackType.max.getValue(); ++attackType) {
            updateDamagePctDoneMods(attackType);
        }
    }

    public final void getAnyUnitListInRange(ArrayList<Unit> list, float fMaxSearchRange) {
        var p = new CellCoord(MapDefine.computeCellCoord(getLocation().getX(), getLocation().getY()));
        var cell = new Cell(p);
        cell.setNoCreate();

        var u_check = new AnyUnitInObjectRangeCheck(this, fMaxSearchRange);
        var searcher = new UnitListSearcher(this, list, u_check, gridType.All);

        cell.visit(p, searcher, getMap(), this, fMaxSearchRange);
    }

    public final void getAttackableUnitListInRange(ArrayList<Unit> list, float fMaxSearchRange) {
        var p = new CellCoord(MapDefine.computeCellCoord(getLocation().getX(), getLocation().getY()));
        var cell = new Cell(p);
        cell.setNoCreate();

        var u_check = new NearestAttackableUnitInObjectRangeCheck(this, this, fMaxSearchRange);
        var searcher = new UnitListSearcher(this, list, u_check, gridType.All);

        cell.visit(p, searcher, getMap(), this, fMaxSearchRange);
    }

    public final void getFriendlyUnitListInRange(ArrayList<Unit> list, float fMaxSearchRange) {
        getFriendlyUnitListInRange(list, fMaxSearchRange, false);
    }

    public final void getFriendlyUnitListInRange(ArrayList<Unit> list, float fMaxSearchRange, boolean exceptSelf) {
        var p = new CellCoord(MapDefine.computeCellCoord(getLocation().getX(), getLocation().getY()));
        var cell = new Cell(p);
        cell.setNoCreate();

        var u_check = new AnyFriendlyUnitInObjectRangeCheck(this, this, fMaxSearchRange, false, exceptSelf);
        var searcher = new UnitListSearcher(this, list, u_check, gridType.All);

        cell.visit(p, searcher, getMap(), this, fMaxSearchRange);
    }

    public final CombatManager getCombatManager() {
        return combatManager;
    }

    // Exposes the threat manager directly - be careful when interfacing with this
    // As a general rule of thumb, any unit pointer MUST be null checked BEFORE passing it to threatmanager methods
    // threatmanager will NOT null check your pointers for you - misuse = crash
    public final ThreatManager getThreatManager() {
        return threatManager;
    }

    public final double getTotalSpellPowerValue(SpellSchoolMask mask, boolean heal) {
        if (!isPlayer()) {
            if (getOwnerUnit()) {
                var ownerPlayer = getOwnerUnit().toPlayer();

                if (ownerPlayer != null) {
                    if (isTotem()) {
                        return getOwnerUnit().getTotalSpellPowerValue(mask, heal);
                    } else {
                        if (isPet()) {
                            return ownerPlayer.getActivePlayerData().petSpellPower.getValue();
                        } else if (isGuardian()) {
                            return ((Guardian) this).GetBonusDamage();
                        }
                    }
                }
            }

            if (heal) {
                return spellBaseHealingBonusDone(mask);
            } else {
                return spellBaseDamageBonusDone(mask);
            }
        }

        var thisPlayer = toPlayer();

        var sp = 0;

        if (heal) {
            sp = thisPlayer.getActivePlayerData().modHealingDonePos;
        } else {
            var counter = 0;

            for (var i = 1; i < SpellSchool.max.getValue(); i++) {
                if ((mask.getValue() & (1 << i)) > 0) {
                    sp += thisPlayer.getActivePlayerData().modDamageDonePos.get(i);
                    counter++;
                }
            }

            if (counter > 0) {
                sp /= counter;
            }
        }

        return Math.max(sp, 0); //avoid negative spell power
    }

    private void _UpdateSpells(int diff) {
        spellHistory.update();

        if (getCurrentSpell(CurrentSpellType.AutoRepeat) != null) {
            _UpdateAutoRepeatSpell();
        }

        for (CurrentSpellType i = 0; i.getValue() < CurrentSpellType.max.getValue(); ++i) {
            if (getCurrentSpell(i) != null && getCurrentSpells().get(i).getState() == SpellState.Finished) {
                getCurrentSpells().get(i).setReferencedFromCurrent(false);
                getCurrentSpells().put(i, null);
            }
        }

        ArrayList<aura> toRemove = new ArrayList<>();

        for (var aura : ownedAuras.getAuras()) {
            if (aura == null) {
                continue;
            }

            aura.updateOwner(diff, this);

            if (aura.isExpired()) {
                toRemove.add(aura);
            }

            if (aura.getSpellInfo().isChanneled() && ObjectGuid.opNotEquals(aura.getCasterGuid(), getGUID()) && !global.getObjAccessor().GetWorldObject(this, aura.getCasterGuid())) {
                toRemove.add(aura);
            }
        }

        // remove expired auras - do that after updates(used in scripts?)
        for (var pair : toRemove) {
            removeOwnedAura(pair.getId(), pair, AuraRemoveMode.Expire);
        }

        synchronized (visibleAurasToUpdate) {
            for (var aura : visibleAurasToUpdate) {
                aura.clientUpdate();
            }

            visibleAurasToUpdate.clear();
        }

        _DeleteRemovedAuras();

        if (!getGameObjects().isEmpty()) {
            for (var i = 0; i < getGameObjects().size(); ++i) {
                var go = getGameObjects().get(i);

                if (!go.isSpawned()) {
                    go.setOwnerGUID(ObjectGuid.Empty);
                    go.setRespawnTime(0);
                    go.delete();
                    getGameObjects().remove(go);
                }
            }
        }
    }

    private Unit getVehicleRoot() {
        var vehicleRoot = getVehicleBase();

        if (!vehicleRoot) {
            return null;
        }

        for (; ; ) {
            if (!vehicleRoot.getVehicleBase()) {
                return vehicleRoot;
            }

            vehicleRoot = vehicleRoot.getVehicleBase();
        }
    }

    private ArrayList<DynamicObject> getDynObjects(int spellId) {
        ArrayList<DynamicObject> dynamicobjects = new ArrayList<>();

        for (var obj : getDynamicObjects()) {
            if (obj.getSpellId() == spellId) {
                dynamicObjects.add(obj);
            }
        }

        return dynamicobjects;
    }

    private ArrayList<GameObject> getGameObjects(int spellId) {
        ArrayList<GameObject> gameobjects = new ArrayList<>();

        for (var obj : getGameObjects()) {
            if (obj.getSpellId() == spellId) {
                gameObjects.add(obj);
            }
        }

        return gameobjects;
    }

    private void cancelSpellMissiles(int spellId) {
        cancelSpellMissiles(spellId, false);
    }

    private void cancelSpellMissiles(int spellId, boolean reverseMissile) {
        var hasMissile = false;

        getEvents().ScheduleAbortOnAllMatchingEvents(e ->
        {
            var spell = spell.extractSpellFromEvent(e);

            if (spell != null) {
                if (spell.spellInfo.getId() == spellId) {
                    hasMissile = true;

                    return true;
                }
            }

            return false;
        });

        if (hasMissile) {
            MissileCancel packet = new MissileCancel();
            packet.ownerGUID = getGUID();
            packet.spellID = spellId;
            packet.reverse = reverseMissile;
            sendMessageToSet(packet, false);
        }
    }

    private void restoreDisabledAI() {
        // Keep popping the stack until we either reach the bottom or find a valid AI
        while (popAI()) {
            if (getTopAI() != null && !(getTopAI() instanceof ScheduledChangeAI)) {
                return;
            }
        }
    }

    private UnitAI getScheduledChangeAI() {
        var creature = toCreature();

        if (creature != null) {
            return new ScheduledChangeAI(creature);
        } else {
            return null;
        }
    }

    private boolean hasScheduledAIChange() {
        var ai = getAi();

        if (ai != null) {
            return ai instanceof ScheduledChangeAI;
        } else {
            return true;
        }
    }

    private void removeAllFollowers() {
        while (!followingMe.isEmpty()) {
            followingMe.get(0).setTarget(null);
        }
    }

    private boolean hasInterruptFlag(SpellAuraInterruptFlags flags) {
        return interruptMask.hasFlag(flags);
    }

    private boolean hasInterruptFlag(SpellAuraInterruptFlags2 flags) {
        return interruptMask2.hasFlag(flags);
    }

    private void _UpdateAutoRepeatSpell() {
        var autoRepeatSpellInfo = getCurrentSpells().get(CurrentSpellType.AutoRepeat).spellInfo;

        // check "realtime" interrupts
        // don't cancel spells which are affected by a SPELL_AURA_CAST_WHILE_WALKING effect
        if ((isMoving() && getCurrentSpell(CurrentSpellType.AutoRepeat).checkMovement() != SpellCastResult.SpellCastOk) || isNonMeleeSpellCast(false, false, true, autoRepeatSpellInfo.getId() == 75)) {
            // cancel wand shoot
            if (autoRepeatSpellInfo.getId() != 75) {
                interruptSpell(CurrentSpellType.AutoRepeat);
            }

            return;
        }

        // castroutine
        if (isAttackReady(WeaponAttackType.RangedAttack) && getCurrentSpell(CurrentSpellType.AutoRepeat).getState() != SpellState.Preparing) {
            // Check if able to cast
            var currentSpell = getCurrentSpells().get(CurrentSpellType.AutoRepeat);

            if (currentSpell != null) {
                var result = currentSpell.checkCast(true);

                if (result != SpellCastResult.SpellCastOk) {
                    if (autoRepeatSpellInfo.getId() != 75) {
                        interruptSpell(CurrentSpellType.AutoRepeat);
                    } else if (getObjectTypeId() == TypeId.PLAYER) {
                        spell.sendCastResult(toPlayer(), autoRepeatSpellInfo, currentSpell.spellVisual, currentSpell.castId, result);
                    }

                    return;
                }

                // we want to shoot
                Spell spell = new spell(this, autoRepeatSpellInfo, TriggerCastFlags.IgnoreGCD);
                spell.prepare(currentSpell.targets);
            }
        }
    }

    private int getCosmeticMountDisplayId() {
        return getUnitData().cosmeticMountDisplayID;
    }

    public final void setCosmeticMountDisplayId(int mountDisplayId) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().cosmeticMountDisplayID), mountDisplayId);
    }

    private Player getControllingPlayer() {
        var guid = getCharmerOrOwnerGUID();

        if (!guid.isEmpty()) {
            var master = global.getObjAccessor().GetUnit(this, guid);

            if (master != null) {
                return master.getControllingPlayer();
            }

            return null;
        } else {
            return toPlayer();
        }
    }

    private void startReactiveTimer(ReactiveType reactive) {
        reactiveTimer.put(reactive, 4000);
    }

    private void dealMeleeDamage(CalcDamageInfo damageInfo, boolean durabilityLoss) {
        var victim = damageInfo.getTarget();

        if (!victim.isAlive() || victim.hasUnitState(UnitState.InFlight) || (victim.isTypeId(TypeId.UNIT) && victim.toCreature().isEvadingAttacks())) {
            return;
        }

        if (damageInfo.getTargetState() == victimState.Parry && (!victim.isCreature() || victim.toCreature().getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.NoParryHasten))) {
            // Get attack timers
            float offtime = victim.getAttackTimer(WeaponAttackType.OFF_ATTACK);
            float basetime = victim.getAttackTimer(WeaponAttackType.BASE_ATTACK);

            // Reduce attack time
            if (victim.haveOffhandWeapon() && offtime < basetime) {
                var percent20 = victim.getBaseAttackTime(WeaponAttackType.OFF_ATTACK) * 0.20f;
                var percent60 = 3.0f * percent20;

                if (offtime > percent20 && offtime <= percent60) {
                    victim.setAttackTimer(WeaponAttackType.OFF_ATTACK, (int) percent20);
                } else if (offtime > percent60) {
                    offtime -= 2.0f * percent20;
                    victim.setAttackTimer(WeaponAttackType.OFF_ATTACK, (int) offtime);
                }
            } else {
                var percent20 = victim.getBaseAttackTime(WeaponAttackType.BASE_ATTACK) * 0.20f;
                var percent60 = 3.0f * percent20;

                if (basetime > percent20 && basetime <= percent60) {
                    victim.setAttackTimer(WeaponAttackType.BASE_ATTACK, (int) percent20);
                } else if (basetime > percent60) {
                    basetime -= 2.0f * percent20;
                    victim.setAttackTimer(WeaponAttackType.BASE_ATTACK, (int) basetime);
                }
            }
        }

        // Call default DealDamage
        CleanDamage cleanDamage = new cleanDamage(damageInfo.getCleanDamage(), damageInfo.absorb, damageInfo.getAttackType(), damageInfo.getHitOutCome());
        damageInfo.damage = dealDamage(this, victim, damageInfo.damage, cleanDamage, DamageEffectType.Direct, spellSchoolMask.forValue(damageInfo.getDamageSchoolMask()), null, durabilityLoss);

        // If this is a creature and it attacks from behind it has a probability to daze it's victim
        if ((damageInfo.getHitOutCome() == MeleeHitOutcome.crit || damageInfo.getHitOutCome() == MeleeHitOutcome.Crushing || damageInfo.getHitOutCome() == MeleeHitOutcome.NORMAL || damageInfo.getHitOutCome() == MeleeHitOutcome.Glancing) && !isTypeId(TypeId.PLAYER) && !toCreature().isControlledByPlayer() && !victim.getLocation().hasInArc(MathUtil.PI, getLocation()) && (victim.isTypeId(TypeId.PLAYER) || !victim.toCreature().isWorldBoss()) && !victim.isVehicle()) {
            // 20% base chance
            var chance = 20.0f;

            // there is a newbie protection, at level 10 just 7% base chance; assuming linear function
            if (victim.getLevel() < 30) {
                chance = 0.65f * victim.getLevelForTarget(this) + 0.5f;
            }

            int victimDefense = victim.getMaxSkillValueForLevel(this);
            int attackerMeleeSkill = getMaxSkillValueForLevel();

            chance *= attackerMeleeSkill / victimDefense * 0.16f;

            // -probability is between 0% and 40%
            tangible.RefObject<Float> tempRef_chance = new tangible.RefObject<Float>(chance);
            MathUtil.RoundToInterval(tempRef_chance, 0.0f, 40.0f);
            chance = tempRef_chance.refArgValue;

            if (RandomUtil.randChance(chance)) {
                castSpell(victim, 1604, true);
            }
        }

        if (isTypeId(TypeId.PLAYER)) {
            DamageInfo dmgInfo = new DamageInfo(damageInfo);
            toPlayer().castItemCombatSpell(dmgInfo);
        }

        // Do effect if any damage done to target
        if (damageInfo.damage != 0) {
            // We're going to call functions which can modify content of the list during iteration over it's elements
            // Let's copy the list so we can prevent iterator invalidation
            var vDamageShieldsCopy = victim.getAuraEffectsByType(AuraType.DamageShield);

            for (var dmgShield : vDamageShieldsCopy) {
                var spellInfo = dmgShield.getSpellInfo();

                // Damage shield can be resisted...
                var missInfo = victim.spellHitResult(this, spellInfo, false);

                if (missInfo != SpellMissInfo.NONE) {
                    victim.sendSpellMiss(this, spellInfo.getId(), missInfo);

                    continue;
                }

                // ...or immuned
                if (isImmunedToDamage(spellInfo)) {
                    victim.sendSpellDamageImmune(this, spellInfo.getId(), false);

                    continue;
                }

                var damage = dmgShield.getAmount();
                var caster = dmgShield.getCaster();

                if (caster) {
                    damage = caster.spellDamageBonusDone(this, spellInfo, damage, DamageEffectType.SpellDirect, dmgShield.getSpellEffectInfo());
                    damage = spellDamageBonusTaken(caster, spellInfo, damage, DamageEffectType.SpellDirect);
                }

                DamageInfo damageInfo1 = new DamageInfo(this, victim, damage, spellInfo, spellInfo.getSchoolMask(), DamageEffectType.SpellDirect, WeaponAttackType.BASE_ATTACK);
                calcAbsorbResist(damageInfo1);
                damage = damageInfo1.getDamage();

                tangible.RefObject<Double> tempRef_damage = new tangible.RefObject<Double>(damage);
                dealDamageMods(victim, this, tempRef_damage);
                damage = tempRef_damage.refArgValue;

                SpellDamageShield damageShield = new SpellDamageShield();
                damageShield.attacker = victim.getGUID();
                damageShield.defender = getGUID();
                damageShield.spellID = spellInfo.getId();
                damageShield.totalDamage = (int) damage;
                damageShield.originalDamage = (int) damageInfo.getOriginalDamage();
                damageShield.overKill = (int) Math.max(damage - getHealth(), 0);
                damageShield.schoolMask = (int) spellInfo.getSchoolMask().getValue();
                damageShield.logAbsorbed = (int) damageInfo1.getAbsorb();

                dealDamage(victim, this, damage, null, DamageEffectType.SpellDirect, spellInfo.getSchoolMask(), spellInfo, true);
                damageShield.logData.initialize(this);

                victim.sendCombatLogMessage(damageShield);
            }
        }
    }

    private void triggerOnHealthChangeAuras(long oldVal, long newVal) {
        for (var effect : getAuraEffectsByType(AuraType.TriggerSpellOnHealthPct)) {
            var triggerHealthPct = effect.getAmount();
            var triggerSpell = effect.getSpellEffectInfo().triggerSpell;
            var threshold = countPctFromMaxHealth(triggerHealthPct);

            switch (AuraTriggerOnHealthChangeDirection.forValue(effect.getMiscValue())) {
                case Above:
                    if (newVal < threshold || oldVal > threshold) {
                        continue;
                    }

                    break;
                case Below:
                    if (newVal > threshold || oldVal < threshold) {
                        continue;
                    }

                    break;
                default:
                    break;
            }

            castSpell(this, triggerSpell, new CastSpellExtraArgs(effect));
        }
    }

    private void updateReactives(int p_time) {
        for (ReactiveType reactive = 0; reactive.getValue() < ReactiveType.max.getValue(); ++reactive) {
            if (!reactiveTimer.containsKey(reactive)) {
                continue;
            }

            if (reactiveTimer.get(reactive).compareTo(p_time) <= 0) {
                reactiveTimer.put(reactive, 0);

                switch (reactive) {
                    case Defense:
                        if (hasAuraState(AuraStateType.Defensive)) {
                            modifyAuraState(AuraStateType.Defensive, false);
                        }

                        break;
                    case Defense2:
                        if (hasAuraState(AuraStateType.Defensive2)) {
                            modifyAuraState(AuraStateType.Defensive2, false);
                        }

                        break;
                }
            } else {
                reactiveTimer.put(reactive, reactiveTimer.get(reactive) - p_time);
            }
        }
    }

    private void gainSpellComboPoints(byte count) {
        if (count == 0) {
            return;
        }

        var cp = (byte) getPower(powerType.ComboPoints);

        cp += count;

        if (cp > 5) {
            cp = 5;
        } else if (cp < 0) {
            cp = 0;
        }

        setPower(powerType.ComboPoints, cp);
    }

    private boolean isBlockCritical() {
        if (RandomUtil.randChance(getTotalAuraModifier(AuraType.ModBlockCritChance))) {
            return true;
        }

        return false;
    }




    public void setDeathState(DeathState s) {
        // Death state needs to be updated before removeAllAurasOnDeath() is called, to prevent entering combat
        setDeathState(s);

        var isOnVehicle = getVehicle1() != null;

        if (s != deathState.Alive && s != deathState.JustRespawned) {
            combatStop();

            if (isNonMeleeSpellCast(false)) {
                interruptNonMeleeSpells(false);
            }

            exitVehicle(); // Exit vehicle before calling RemoveAllControlled
            // vehicles use special type of charm that is not removed by the next function
            // triggering an assert
            unsummonAllTotems();
            removeAllControlled();
            removeAllAurasOnDeath();
        }

        if (s == deathState.JustDied) {
            // remove aurastates allowing special moves
            clearAllReactives();
            diminishing.clear();

            // Don't clear the movement if the Unit was on a vehicle as we are exiting now
            if (!isOnVehicle) {
                if (getMotionMaster().stopOnDeath()) {
                    disableSpline();
                }
            }

            // without this when removing IncreaseMaxHealth aura player may stuck with 1 hp
            // do not why since in IncreaseMaxHealth currenthealth is checked
            setHealth(0);
            setPower(getDisplayPowerType(), 0);
            setEmoteState(emote.OneshotNone);

            // players in instance don't have zoneScript, but they have InstanceScript
            var zoneScript = getZoneScript() != null ? getZoneScript() : getInstanceScript();

            if (zoneScript != null) {
                zoneScript.onUnitDeath(this);
            }
        } else if (s == deathState.JustRespawned) {
            removeUnitFlag(UnitFlag.Skinnable); // clear skinnable for creature and player (at Battleground)
        }
    }



    public boolean canFly() {
        return false;
    }

    public boolean canEnterWater() {
        return false;
    }

    public boolean canSwim() {
        // Mirror client behavior, if this method returns false then client will not use swimming animation and for players will apply gravity as if there was no water
        if (hasUnitFlag(UnitFlag.CantSwim)) {
            return false;
        }

        if (hasUnitFlag(UnitFlag.PlayerControlled)) // is player
        {
            return true;
        }

        if (hasUnitFlag2(UnitFlag2.forValue(0x1000000))) {
            return false;
        }

        if (hasUnitFlag(UnitFlag.PET_IN_COMBAT)) {
            return true;
        }

        return hasUnitFlag(UnitFlag.Rename.getValue() | UnitFlag.CanSwim.getValue());
    }

    public final float getHoverOffset() {
        return hasUnitMovementFlag(MovementFlag.Hover) ? getUnitData().HoverHeight : 0.0f;
    }

    public final boolean isGravityDisabled() {
        return getMovementInfo().hasMovementFlag(MovementFlag.DisableGravity);
    }

    public final boolean isWalking() {
        return getMovementInfo().hasMovementFlag(MovementFlag.Walking);
    }

    public final boolean isHovering() {
        return getMovementInfo().hasMovementFlag(MovementFlag.Hover);
    }

    public final boolean isStopped() {
        return !hasUnitState(UnitState.Moving);
    }

    public final boolean isMoving() {
        return getMovementInfo().hasMovementFlag(MovementFlag.MaskMoving);
    }

    public final boolean isTurning() {
        return getMovementInfo().hasMovementFlag(MovementFlag.MaskTurning);
    }

    public final boolean isFlying() {
        return getMovementInfo().hasMovementFlag(MovementFlag.Flying.getValue() | MovementFlag.DisableGravity.getValue());
    }

    public final boolean isFalling() {
        return getMovementInfo().hasMovementFlag(MovementFlag.Falling.getValue() | MovementFlag.FallingFar.getValue()) || getMoveSpline().isFalling();
    }

    public final boolean isInWater() {
        return getLiquidStatus().hasFlag(ZLiquidStatus.InWater.getValue() | ZLiquidStatus.UnderWater.getValue());
    }

    public final boolean isUnderWater() {
        return getLiquidStatus().hasFlag(ZLiquidStatus.UnderWater);
    }

    public final MovementForces getMovementForces() {
        return movementForces;
    }

    public final boolean isPlayingHoverAnim() {
        return playHoverAnim;
    }

    public final Unit getUnitBeingMoved() {
        return getUnitMovedByMe();
    }

    public final Player getPlayerMovingMe1() {
        return getPlayerMovingMe();
    }

    //Spline
    public final boolean isSplineEnabled() {
        return getMoveSpline().initialized() && !getMoveSpline().finalized();
    }

    public final float getSpeed(UnitMoveType moveType) {
        int index = moveType.ordinal();
        return getSpeedRate()[index] * (isControlledByPlayer() ? PLAYER_BASE_MOVE_SPEED[index] : BASE_MOVE_SPEED[index]);
    }

    public final void setSpeed(UnitMoveType moveType, float newValue) {
        int index = moveType.ordinal();
        setSpeedRate(moveType, newValue / (isControlledByPlayer() ? PLAYER_BASE_MOVE_SPEED[index] : BASE_MOVE_SPEED[index]));
    }

    public final void setSpeedRate(UnitMoveType moveType, float rate) {
        rate = Math.max(rate, 0.01f);

        int index = moveType.ordinal();
        if (getSpeedRate()[index] == rate) {
            return;
        }

        getSpeedRate()[index] = rate;

        propagateSpeedChange();


        if (isTypeId(TypeId.PLAYER)) {
            // register forced speed changes for WorldSession.HandleForceSpeedChangeAck
            // and do it only for real sent packets and use run for run/mounted as client expected
            toPlayer().setForcedSpeedChanges(toPlayer().getForcedSpeedChanges() + 1);
            toPlayer().getForcedSpeedChanges()[index];

            if (!isInCombat()) {
                var pet = toPlayer().getCurrentPet();

                if (pet) {
                    pet.setSpeedRate(moveType, getSpeedRate()[index]);
                }
            }
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer(); // unit controlled by a player.

        if (playerMover != null) {
            // Send notification to self
            MoveSetSpeed selfpacket = new MoveSetSpeed(ServerOpCode.MOVE_TYPE_TO_OPCODE[index][1]);
            selfpacket.moverGUID = getGUID();
            selfpacket.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            selfpacket.speed = getSpeed(moveType);
            playerMover.sendPacket(selfpacket);

            // Send notification to other players
            MoveUpdateSpeed packet = new MoveUpdateSpeed(ServerOpCode.MOVE_TYPE_TO_OPCODE[index][2]);
            packet.status = getMovementInfo();
            packet.speed = getSpeed(moveType);
            playerMover.sendMessageToSet(packet, false);
        } else {
            MoveSplineSetSpeed packet = new MoveSplineSetSpeed(ServerOpCode.MOVE_TYPE_TO_OPCODE[index][0]);
            packet.moverGUID = getGUID();
            packet.speed = getSpeed(moveType);
            sendMessageToSet(packet, true);
        }
    }

    public final float getSpeedRate(UnitMoveType mtype) {
        return getSpeedRate()[mtype.ordinal()];
    }

    public MovementGeneratorType getDefaultMovementType() {
        return MovementGeneratorType.IDLE;
    }

    public final void stopMoving() {
        clearUnitState(UnitState.MOVING);

        // not need send any packets if not in world or not moving
        if (!isInWorld() || getMoveSpline().finalized()) {
            return;
        }

        // Update position now since Stop does not start a new movement that can be updated later
        if (getMoveSpline().hasStarted()) {
            updateSplinePosition();
        }

        MoveSplineInit init = new MoveSplineInit(this);
        init.stop();
    }

    public final void pauseMovement(int timer, MovementSlot slot) {
        pauseMovement(timer, slot, true);
    }

    public final void pauseMovement(int timer) {
        pauseMovement(timer, 0, true);
    }

    public final void pauseMovement() {
        pauseMovement(0, 0, true);
    }

    public final void pauseMovement(int timer, MovementSlot slot, boolean forced) {

        var movementGenerator = getMotionMaster().getCurrentMovementGenerator(slot);

        if (movementGenerator != null) {
            movementGenerator.pause(timer);
        }

        if (forced && getMotionMaster().getCurrentSlot() == slot) {
            stopMoving();
        }
    }

    public final void resumeMovement(int timer) {
        resumeMovement(timer, 0);
    }

    public final void resumeMovement() {
        resumeMovement(0, 0);
    }

    public final void resumeMovement(int timer, MovementSlot slot) {
        var movementGenerator = getMotionMaster().getCurrentMovementGenerator(slot);

        if (movementGenerator != null) {
            movementGenerator.resume(timer);
        }
    }

    public final void setInFront(WorldObject target) {
        if (!hasUnitState(UnitState.CANNOT_TURN)) {
            getLocation().setO(getLocation().getAbsoluteAngle(target.getLocation()));
        }
    }

    public final void setFacingTo(float ori) {
        setFacingTo(ori, true);
    }

    public final void setFacingTo(float ori, boolean force) {
        // do not face when already moving
        if (!force && (!isStopped() || !getMoveSpline().finalized())) {
            return;
        }

        MoveSplineInit init = new MoveSplineInit(this);
        init.moveTo(getLocation().getX(), getLocation().getY(), getLocation().getZ(), false);

        if (getTransport() != null) {
            init.disableTransportPathTransformations(); // It makes no sense to target global orientation
        }

        init.setFacing(ori);

        //GetMotionMaster().launchMoveSpline(init, eventId.face, MovementGeneratorPriority.Highest);
        init.launch();
    }

    public final void setFacingToUnit(Unit unit) {
        setFacingToUnit(unit, true);
    }

    public final void setFacingToUnit(Unit unit, boolean force) {
        // do not face when already moving
        if (!force && (!isStopped() || !getMoveSpline().finalized())) {
            return;
        }

        /** @todo figure out under what conditions creature will move towards object instead of facing it where it currently is.
         */
        var init = new MoveSplineInit(this);
        init.moveTo(getLocation().getX(), getLocation().getY(), getLocation().getZ(), false);

        if (getTransport() != null) {
            init.disableTransportPathTransformations(); // It makes no sense to target global orientation
        }

        init.setFacing(unit);

        //GetMotionMaster()->LaunchMoveSpline(std::move(init), EVENT_FACE, MOTION_PRIORITY_HIGHEST);
        init.launch();
    }

    public final void setFacingToObject(WorldObject obj) {
        setFacingToObject(obj, true);
    }

    public final void setFacingToObject(WorldObject obj, boolean force) {
        // do not face when already moving
        if (!force && (!isStopped() || !getMoveSpline().finalized())) {
            return;
        }

        // @todo figure out under what conditions creature will move towards object instead of facing it where it currently is.
        MoveSplineInit init = new MoveSplineInit(this);
        init.moveTo(getLocation().getX(), getLocation().getY(), getLocation().getZ(), false);
        init.setFacing(getLocation().getAbsoluteAngle(obj.getLocation())); // when on transport, GetAbsoluteAngle will still return global coordinates (and angle) that needs transforming

        //GetMotionMaster().launchMoveSpline(init, eventId.face, MovementGeneratorPriority.Highest);
        init.launch();
    }

    public final void monsterMoveWithSpeed(float x, float y, float z, float speed, boolean generatePath) {
        monsterMoveWithSpeed(x, y, z, speed, generatePath, false);
    }

    public final void monsterMoveWithSpeed(float x, float y, float z, float speed) {
        monsterMoveWithSpeed(x, y, z, speed, false, false);
    }

    public final void monsterMoveWithSpeed(float x, float y, float z, float speed, boolean generatePath, boolean forceDestination) {
        var initializer = (MoveSplineInit init) ->
        {
            init.moveTo(x, y, z, generatePath, forceDestination);
            init.setVelocity(speed);
        };

        getMotionMaster().launchMoveSpline(initializer, 0, MovementGeneratorPriority.NORMAL, MovementGeneratorType.POINT);
    }

    public final void knockbackFrom(Position origin, float speedXY, float speedZ) {
        knockbackFrom(origin, speedXY, speedZ, null);
    }

    public final void knockbackFrom(Position origin, float speedXY, float speedZ, SpellEffectExtraData spellEffectExtraData) {
        var player = toPlayer();

        if (!player) {
            var charmer = getCharmer();

            if (charmer) {
                player = charmer.toPlayer();

                if (player && player.getUnitBeingMoved() != this) {
                    player = null;
                }
            }
        }

        if (!player) {
            getMotionMaster().moveKnockbackFrom(origin, speedXY, speedZ, spellEffectExtraData);
        } else {
            var o = getLocation() == origin ? getLocation().getO() + (float) Math.PI : origin.getRelativeAngle(getLocation());

            if (speedXY < 0) {
                speedXY = -speedXY;
                o = o - (float) Math.PI;
            }

            var vcos = (float) Math.cos(o);
            var vsin = (float) Math.sin(o);
            sendMoveKnockBack(player, speedXY, -speedZ, vcos, vsin);
        }
    }

    public final boolean setCanTransitionBetweenSwimAndFly(boolean enable) {
        if (!isTypeId(TypeId.PLAYER)) {
            return false;
        }

        if (enable == hasExtraUnitMovementFlag(MovementFlag2.CanSwimToFlyTrans)) {
            return false;
        }

        if (enable) {
            addExtraUnitMovementFlag(MovementFlag2.CanSwimToFlyTrans);
        } else {
            removeExtraUnitMovementFlag(MovementFlag2.CanSwimToFlyTrans);
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(enable ? ServerOpcode.MoveEnableTransitionBetweenSwimAndFly : ServerOpcode.MoveDisableTransitionBetweenSwimAndFly);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        }

        return true;
    }

    public final boolean setCanTurnWhileFalling(boolean enable) {
        // Temporarily disabled for short lived auras that unapply before client had time to ACK applying
        //if (enable == hasExtraUnitMovementFlag(MovementFlag2.CanTurnWhileFalling))
        //return false;

        if (enable) {
            addExtraUnitMovementFlag(MovementFlag2.CanTurnWhileFalling);
        } else {
            removeExtraUnitMovementFlag(MovementFlag2.CanTurnWhileFalling);
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(enable ? ServerOpcode.MoveSetCanTurnWhileFalling : ServerOpcode.MoveUnsetCanTurnWhileFalling);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        }

        return true;
    }

    public final boolean setCanDoubleJump(boolean enable) {
        if (enable == hasExtraUnitMovementFlag(MovementFlag2.CanDoubleJump)) {
            return false;
        }

        if (enable) {
            addExtraUnitMovementFlag(MovementFlag2.CanDoubleJump);
        } else {
            removeExtraUnitMovementFlag(MovementFlag2.CanDoubleJump);
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(enable ? ServerOpcode.MoveEnableDoubleJump : ServerOpcode.MoveDisableDoubleJump);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        }

        return true;
    }

    public final boolean setDisableInertia(boolean disable) {
        if (disable == hasExtraUnitMovementFlag2(MovementFlags3.DisableInertia)) {
            return false;
        }

        if (disable) {
            addExtraUnitMovementFlag2(MovementFlags3.DisableInertia);
        } else {
            removeExtraUnitMovementFlag2(MovementFlags3.DisableInertia);
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover != null) {
            MoveSetFlag packet = new MoveSetFlag(disable ? ServerOpcode.MoveDisableInertia : ServerOpcode.MoveEnableInertia);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        }

        return true;
    }

    public final void jumpTo(float speedXY, float speedZ, float angle) {
        jumpTo(speedXY, speedZ, angle, null);
    }

    public final void jumpTo(float speedXY, float speedZ, float angle, Position dest) {
        if (dest != null) {
            angle += getLocation().getRelativeAngle(dest);
        }

        if (isTypeId(TypeId.UNIT)) {
            getMotionMaster().moveJumpTo(angle, speedXY, speedZ);
        } else {
            var vcos = (float) Math.cos(angle + getLocation().getO());
            var vsin = (float) Math.sin(angle + getLocation().getO());
            sendMoveKnockBack(toPlayer(), speedXY, -speedZ, vcos, vsin);
        }
    }

    public final void jumpTo(WorldObject obj, float speedZ) {
        jumpTo(obj, speedZ, false);
    }

    public final void jumpTo(WorldObject obj, float speedZ, boolean withOrientation) {
        var pos = new Position();
        obj.getContactPoint(this, pos);
        var speedXY = getLocation().getExactdist2D(pos.getX(), pos.getY()) * 10.0f / speedZ;
        pos.setO(getLocation().getAbsoluteAngle(obj.getLocation()));
        getMotionMaster().moveJump(pos, speedXY, speedZ, eventId.jump, withOrientation);
    }

    public final void updateSpeed(UnitMoveType mtype) {
        double main_speed_mod = 0;
        double stack_bonus = 1.0f;
        double non_stack_bonus = 1.0f;

        switch (mtype) {
            // Only apply debuffs
            case FlightBack:
            case RunBack:
            case SwimBack:
                break;
            case Walk:
                return;
            case Run: {
                if (isMounted()) // Use on mount auras
                {
                    main_speed_mod = getMaxPositiveAuraModifier(AuraType.ModIncreaseMountedSpeed);
                    stack_bonus = getTotalAuraMultiplier(AuraType.ModMountedSpeedAlways);
                    non_stack_bonus += getMaxPositiveAuraModifier(AuraType.ModMountedSpeedNotStack) / 100.0f;
                } else {
                    main_speed_mod = getMaxPositiveAuraModifier(AuraType.ModIncreaseSpeed);
                    stack_bonus = getTotalAuraMultiplier(AuraType.ModSpeedAlways);
                    non_stack_bonus += getMaxPositiveAuraModifier(AuraType.ModSpeedNotStack) / 100.0f;
                }

                break;
            }
            case Swim: {
                main_speed_mod = getMaxPositiveAuraModifier(AuraType.ModIncreaseSwimSpeed);

                break;
            }
            case Flight: {
                if (isTypeId(TypeId.UNIT) && isControlledByPlayer()) // not sure if good for pet
                {
                    main_speed_mod = getMaxPositiveAuraModifier(AuraType.ModIncreaseVehicleFlightSpeed);
                    stack_bonus = getTotalAuraMultiplier(AuraType.ModVehicleSpeedAlways);

                    // for some spells this mod is applied on vehicle owner
                    double owner_speed_mod = 0;

                    var owner = getCharmer();

                    if (owner != null) {
                        owner_speed_mod = owner.getMaxPositiveAuraModifier(AuraType.ModIncreaseVehicleFlightSpeed);
                    }

                    main_speed_mod = Math.max(main_speed_mod, owner_speed_mod);
                } else if (isMounted()) {
                    main_speed_mod = getMaxPositiveAuraModifier(AuraType.ModIncreaseMountedFlightSpeed);
                    stack_bonus = getTotalAuraMultiplier(AuraType.ModMountedFlightSpeedAlways);
                } else // Use not mount (shapeshift for example) auras (should stack)
                {
                    main_speed_mod = getTotalAuraModifier(AuraType.ModIncreaseFlightSpeed) + getTotalAuraModifier(AuraType.ModIncreaseVehicleFlightSpeed);
                }

                non_stack_bonus += getMaxPositiveAuraModifier(AuraType.ModFlightSpeedNotStack) / 100.0f;

                // Update speed for vehicle if available
                if (isTypeId(TypeId.PLAYER) && getVehicle1() != null) {
                    getVehicleBase().updateSpeed(UnitMoveType.flight);
                }

                break;
            }
            default:
                Log.outError(LogFilter.unit, "Unit.UpdateSpeed: Unsupported move type ({0})", mtype);

                return;
        }

        // now we ready for speed calculation
        var speed = Math.max(non_stack_bonus, stack_bonus);

        if (main_speed_mod != 0) {
            tangible.RefObject<Double> tempRef_speed = new tangible.RefObject<Double>(speed);
            MathUtil.AddPct(tempRef_speed, main_speed_mod);
            speed = tempRef_speed.refArgValue;
        }

        switch (mtype) {
            case Run:
            case Swim:
            case Flight: {
                // Set creature speed rate
                if (isTypeId(TypeId.UNIT)) {
                    speed *= toCreature().getCreatureTemplate().speedRun; // at this point, MOVE_WALK is never reached
                }

                // Normalize speed by 191 aura SPELL_AURA_USE_NORMAL_MOVEMENT_SPEED if need
                // @todo possible affect only on MOVE_RUN
                var normalization = getMaxPositiveAuraModifier(AuraType.UseNormalMovementSpeed);

                if (normalization != 0) {
                    var creature1 = toCreature();

                    if (creature1) {
                        var immuneMask = creature1.getCreatureTemplate().mechanicImmuneMask;

                        if ((boolean) (immuneMask & (1 << (mechanics.Snare.getValue() - 1))) || (boolean) (immuneMask & (1 << (mechanics.Daze.getValue() - 1)))) {
                            break;
                        }
                    }

                    // Use speed from aura
                    var max_speed = normalization / (isControlledByPlayer() ? SharedConst.playerBaseMoveSpeed[mtype.getValue()] : SharedConst.baseMoveSpeed[mtype.getValue()]);

                    if (speed > max_speed) {
                        speed = max_speed;
                    }
                }

                if (mtype == UnitMoveType.run) {
                    // force minimum speed rate @ aura 437 SPELL_AURA_MOD_MINIMUM_SPEED_RATE
                    var minSpeedMod1 = getMaxPositiveAuraModifier(AuraType.ModMinimumSpeedRate);

                    if (minSpeedMod1 != 0) {
                        var minSpeed = minSpeedMod1 / (isControlledByPlayer() ? SharedConst.playerBaseMoveSpeed[mtype.getValue()] : SharedConst.baseMoveSpeed[mtype.getValue()]);

                        if (speed < minSpeed) {
                            speed = minSpeed;
                        }
                    }
                }

                break;
            }
            default:
                break;
        }

        var creature = toCreature();

        if (creature != null) {
            if (creature.hasUnitTypeMask(UnitTypeMask.minion) && !creature.isInCombat()) {
                if (getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.Follow) {
                    MovementGenerator tempVar = getMotionMaster().getCurrentMovementGenerator();
                    var followed = (tempVar instanceof FollowMovementGenerator ? (FollowMovementGenerator) tempVar : null).getTarget();

                    if (followed != null && Objects.equals(followed.getGUID(), getOwnerGUID()) && !followed.isInCombat()) {
                        var ownerSpeed = followed.getSpeedRate(mtype);

                        if (speed < ownerSpeed || creature.isWithinDist3D(followed.getLocation(), 10.0f)) {
                            speed = ownerSpeed;
                        }

                        speed *= Math.min(Math.max(1.0f, 0.75f + (getDistance(followed) - SharedConst.PetFollowDist) * 0.05f), 1.3f);
                    }
                }
            }
        }

        // Apply strongest slow aura mod to speed
        var slow = getMaxNegativeAuraModifier(AuraType.ModDecreaseSpeed);

        if (slow != 0) {
            tangible.RefObject<Double> tempRef_speed2 = new tangible.RefObject<Double>(speed);
            MathUtil.AddPct(tempRef_speed2, slow);
            speed = tempRef_speed2.refArgValue;
        }

        var minSpeedMod = getMaxPositiveAuraModifier(AuraType.ModMinimumSpeed);

        if (minSpeedMod != 0) {
            var baseMinSpeed = 1.0f;

            if (!getOwnerGUID().isPlayer() && !isHunterPet() && getObjectTypeId() == TypeId.UNIT) {
                baseMinSpeed = toCreature().getCreatureTemplate().speedRun;
            }

            var min_speed = MathUtil.CalculatePct(baseMinSpeed, minSpeedMod);

            if (speed < min_speed) {
                speed = min_speed;
            }
        }

        setSpeedRate(mtype, (float) speed);
    }

    public boolean updatePosition(Position obj) {
        return updatePosition(obj, false);
    }

    public boolean updatePosition(Position obj, boolean teleport) {
        return updatePosition(obj.getX(), obj.getY(), obj.getZ(), obj.getO(), teleport);
    }

    public boolean updatePosition(float x, float y, float z, float orientation) {
        return updatePosition(x, y, z, orientation, false);
    }

    public boolean updatePosition(float x, float y, float z, float orientation, boolean teleport) {
        if (!MapDefine.isValidMapCoordinate(x, y, z, orientation)) {
            Log.outError(LogFilter.unit, "Unit.updatePosition({0}, {1}, {2}) .. bad coordinates!", x, y, z);

            return false;
        }

        // Check if angular distance changed
        var turn = MathUtil.fuzzyGt((float) Math.PI - Math.abs(Math.abs(getLocation().getO() - orientation) - (float) Math.PI), 0.0f);

        // G3D::fuzzyEq won't help here, in some cases magnitudes differ by a little more than G3D::eps, but should be considered equal
        var relocated = (teleport || Math.abs(getLocation().getX() - x) > 0.001f || Math.abs(getLocation().getY() - y) > 0.001f || Math.abs(getLocation().getZ() - z) > 0.001f);

        if (relocated) {
            // move and update visible state if need
            if (isTypeId(TypeId.PLAYER)) {
                getMap().playerRelocation(toPlayer(), x, y, z, orientation);
            } else {
                getMap().creatureRelocation(toCreature(), x, y, z, orientation);
            }
        } else if (turn) {
            updateOrientation(orientation);
        }

        positionUpdateInfo.relocated = relocated;
        positionUpdateInfo.turned = turn;

        var isInWater = isInWater();

        if (!isFalling() || isInWater || isFlying()) {
            removeAurasWithInterruptFlags(SpellAuraInterruptFlags2.ground);
        }

        if (isInWater) {
            removeAurasWithInterruptFlags(SpellAuraInterruptFlags2.Swimming);
        }

        return (relocated || turn);
    }

    public final boolean isWithinBoundaryRadius(Unit obj) {
        if (!obj || !isInMap(obj) || !inSamePhase(obj)) {
            return false;
        }

        var objBoundaryRadius = Math.max(obj.getBoundingRadius(), SharedConst.MinMeleeReach);

        return getLocation().isInDist(obj.getLocation(), objBoundaryRadius);
    }

    public final boolean setDisableGravity(boolean disable) {
        return setDisableGravity(disable, true);
    }

    public final boolean setDisableGravity(boolean disable, boolean updateAnimTier) {
        if (disable == isGravityDisabled()) {
            return false;
        }

        if (disable) {
            addUnitMovementFlag(MovementFlag.DisableGravity);
            removeUnitMovementFlag(MovementFlag.Swimming.getValue() | MovementFlag.SplineElevation.getValue());
        } else {
            removeUnitMovementFlag(MovementFlag.DisableGravity);
        }


        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(disable ? ServerOpcode.MoveDisableGravity : ServerOpcode.MoveEnableGravity);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        } else {
            MoveSplineSetFlag packet = new MoveSplineSetFlag(disable ? ServerOpcode.MoveSplineDisableGravity : ServerOpcode.MoveSplineEnableGravity);
            packet.moverGUID = getGUID();
            sendMessageToSet(packet, true);
        }

        if (isCreature() && updateAnimTier && isAlive() && !hasUnitState(UnitState.Root) && !toCreature().getMovementTemplate().isRooted()) {
            if (isGravityDisabled()) {
                setAnimTier(animTier.Fly);
            } else if (isHovering()) {
                setAnimTier(animTier.Hover);
            } else {
                setAnimTier(animTier.ground);
            }
        }

        return true;
    }

    public final MountCapabilityRecord getMountCapability(int mountType) {
        if (mountType == 0) {
            return null;
        }

        var capabilities = global.getDB2Mgr().GetMountCapabilities(mountType);

        if (capabilities == null) {
            return null;
        }

        var areaId = getAreaId();
        int ridingSkill = 5000;
        AreaMountFlags mountFlags = AreaMountFlags.forValue(0);
        boolean isSubmerged;
        boolean isInWater;

        if (isTypeId(TypeId.PLAYER)) {
            ridingSkill = toPlayer().getSkillValue(SkillType.Riding);
        }

        if (hasAuraType(AuraType.MountRestrictions)) {
            for (var auraEffect : getAuraEffectsByType(AuraType.MountRestrictions)) {
                mountFlags = AreaMountFlags.forValue(mountFlags.getValue() | AreaMountFlags.forValue(auraEffect.getMiscValue()).getValue());
            }
        } else {
            var areaTable = CliDB.AreaTableStorage.get(areaId);

            if (areaTable != null) {
                mountFlags = AreaMountFlags.forValue(areaTable.MountFlags);
            }
        }

        var liquidStatus = getMap().getLiquidStatus(getPhaseShift(), getLocation().getX(), getLocation().getY(), getLocation().getZ(), LiquidHeaderTypeFlags.AllLiquids);
        isSubmerged = liquidStatus.hasFlag(ZLiquidStatus.UnderWater) || hasUnitMovementFlag(MovementFlag.Swimming);
        isInWater = liquidStatus.hasFlag(ZLiquidStatus.InWater.getValue() | ZLiquidStatus.UnderWater.getValue());

        for (var mountTypeXCapability : capabilities) {
            var mountCapability = CliDB.MountCapabilityStorage.get(mountTypeXCapability.MountCapabilityID);

            if (mountCapability == null) {
                continue;
            }

            if (ridingSkill < mountCapability.ReqRidingSkill) {
                continue;
            }

            if (!mountCapability.flags.hasFlag(MountCapabilityFlags.IgnoreRestrictions)) {
                if (mountCapability.flags.hasFlag(MountCapabilityFlags.ground) && !mountFlags.hasFlag(AreaMountFlags.GroundAllowed)) {
                    continue;
                }

                if (mountCapability.flags.hasFlag(MountCapabilityFlags.Flying) && !mountFlags.hasFlag(AreaMountFlags.FlyingAllowed)) {
                    continue;
                }

                if (mountCapability.flags.hasFlag(MountCapabilityFlags.Float) && !mountFlags.hasFlag(AreaMountFlags.FloatAllowed)) {
                    continue;
                }

                if (mountCapability.flags.hasFlag(MountCapabilityFlags.Underwater) && !mountFlags.hasFlag(AreaMountFlags.UnderwaterAllowed)) {
                    continue;
                }
            }

            if (!isSubmerged) {
                if (!isInWater) {
                    // player is completely out of water
                    if (!mountCapability.flags.hasFlag(MountCapabilityFlags.ground)) {
                        continue;
                    }
                }
                // player is on water surface
                else if (!mountCapability.flags.hasFlag(MountCapabilityFlags.Float)) {
                    continue;
                }
            } else if (isInWater) {
                if (!mountCapability.flags.hasFlag(MountCapabilityFlags.Underwater)) {
                    continue;
                }
            } else if (!mountCapability.flags.hasFlag(MountCapabilityFlags.Float)) {
                continue;
            }

            if (mountCapability.ReqMapID != -1 && getLocation().getMapId() != mountCapability.ReqMapID && getMap().getEntry().CosmeticParentMapID != mountCapability.ReqMapID && getMap().getEntry().ParentMapID != mountCapability.ReqMapID) {
                continue;
            }

            if (mountCapability.ReqAreaID != 0 && !global.getDB2Mgr().IsInArea(areaId, mountCapability.ReqAreaID)) {
                continue;
            }

            if (mountCapability.ReqSpellAuraID != 0 && !hasAura(mountCapability.ReqSpellAuraID)) {
                continue;
            }

            if (mountCapability.ReqSpellKnownID != 0 && !hasSpell(mountCapability.ReqSpellKnownID)) {
                continue;
            }

            var thisPlayer = toPlayer();

            if (thisPlayer != null) {
                var playerCondition = CliDB.PlayerConditionStorage.get(mountCapability.playerConditionID);

                if (playerCondition != null) {
                    if (!ConditionManager.isPlayerMeetingCondition(thisPlayer, playerCondition)) {
                        continue;
                    }
                }
            }

            return mountCapability;
        }

        return null;
    }

    public final void updateMountCapability() {
        var mounts = getAuraEffectsByType(AuraType.Mounted);

        for (var aurEff : mounts) {
            aurEff.recalculateAmount();

            if (aurEff.amount == 0) {
                aurEff.base.remove();
            } else {
                var capability = CliDB.MountCapabilityStorage.get(aurEff.amount);

                if (capability != null) // aura may get removed by interrupt flag, reapply
                {
                    if (!hasAura(capability.ModSpellAuraID)) {
                        castSpell(this, capability.ModSpellAuraID, new CastSpellExtraArgs(aurEff));
                    }
                }
            }
        }
    }

    @Override
    public void processPositionDataChanged(PositionFullTerrainStatus data) {
        var oldLiquidStatus = getLiquidStatus();
        super.processPositionDataChanged(data);
        processTerrainStatusUpdate(oldLiquidStatus, data.getLiquidInfo());
    }

    public void processTerrainStatusUpdate(ZLiquidStatus oldLiquidStatus, LiquidData newLiquidData) {
        if (!isControlledByPlayer()) {
            return;
        }

        // remove appropriate auras if we are swimming/not swimming respectively
        if (isInWater()) {
            removeAurasWithInterruptFlags(SpellAuraInterruptFlag.UnderWater);
        } else {
            removeAurasWithInterruptFlags(SpellAuraInterruptFlag.AboveWater);
        }

        // liquid aura handling
        LiquidTypeRecord curLiquid = null;

        if (isInWater() && newLiquidData != null) {
            curLiquid = CliDB.LiquidTypeStorage.get(newLiquidData.entry);
        }

        if (curLiquid != getLastLiquid()) {
            if (getLastLiquid() != null && getLastLiquid().spellID != 0) {
                removeAura(getLastLiquid().spellID);
            }

            var player = getCharmerOrOwnerPlayerOrPlayerItself();

            // Set _lastLiquid before casting liquid spell to avoid infinite loops
            setLastLiquid(curLiquid);

            if (curLiquid != null && curLiquid.spellID != 0 && (!player || !player.isGameMaster())) {
                castSpell(this, curLiquid.spellID, true);
            }
        }

        // mount capability depends on liquid state change
        if (oldLiquidStatus != getLiquidStatus()) {
            updateMountCapability();
        }
    }

    public final boolean setWalk(boolean enable) {
        if (enable == isWalking()) {
            return false;
        }

        if (enable) {
            addUnitMovementFlag(MovementFlag.Walking);
        } else {
            removeUnitMovementFlag(MovementFlag.Walking);
        }

        MoveSplineSetFlag packet = new MoveSplineSetFlag(enable ? ServerOpcode.MoveSplineSetWalkMode : ServerOpcode.MoveSplineSetRunMode);
        packet.moverGUID = getGUID();
        sendMessageToSet(packet, true);

        return true;
    }

    public final boolean setFall(boolean enable) {
        if (enable == hasUnitMovementFlag(MovementFlag.Falling)) {
            return false;
        }

        if (enable) {
            addUnitMovementFlag(MovementFlag.Falling);
            getMovementInfo().setFallTime(0);
        } else {
            removeUnitMovementFlag(MovementFlag.Falling.getValue() | MovementFlag.FallingFar.getValue());
        }

        return true;
    }

    public final boolean setSwim(boolean enable) {
        if (enable == hasUnitMovementFlag(MovementFlag.Swimming)) {
            return false;
        }

        if (enable) {
            addUnitMovementFlag(MovementFlag.Swimming);
        } else {
            removeUnitMovementFlag(MovementFlag.Swimming);
        }

        MoveSplineSetFlag packet = new MoveSplineSetFlag(enable ? ServerOpcode.MoveSplineStartSwim : ServerOpcode.MoveSplineStopSwim);
        packet.moverGUID = getGUID();
        sendMessageToSet(packet, true);

        return true;
    }

    public final boolean setCanFly(boolean enable) {
        if (enable == hasUnitMovementFlag(MovementFlag.CanFly)) {
            return false;
        }

        if (enable) {
            addUnitMovementFlag(MovementFlag.CanFly);
            removeUnitMovementFlag(MovementFlag.Swimming.getValue() | MovementFlag.SplineElevation.getValue());
        } else {
            removeUnitMovementFlag(MovementFlag.CanFly.getValue() | MovementFlag.MaskMovingFly.getValue());
        }

        if (!enable && isTypeId(TypeId.PLAYER)) {
            toPlayer().setFallInformation(0, getLocation().getZ());
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(enable ? ServerOpcode.MoveSetCanFly : ServerOpcode.MoveUnsetCanFly);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        } else {
            MoveSplineSetFlag packet = new MoveSplineSetFlag(enable ? ServerOpcode.MoveSplineSetFlying : ServerOpcode.MoveSplineUnsetFlying);
            packet.moverGUID = getGUID();
            sendMessageToSet(packet, true);
        }

        return true;
    }

    public final boolean setWaterWalking(boolean enable) {
        if (enable == hasUnitMovementFlag(MovementFlag.WaterWalk)) {
            return false;
        }

        if (enable) {
            addUnitMovementFlag(MovementFlag.WaterWalk);
        } else {
            removeUnitMovementFlag(MovementFlag.WaterWalk);
        }


        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(enable ? ServerOpcode.MoveSetWaterWalk : ServerOpcode.MoveSetLandWalk);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        } else {
            MoveSplineSetFlag packet = new MoveSplineSetFlag(enable ? ServerOpcode.MoveSplineSetWaterWalk : ServerOpcode.MoveSplineSetLandWalk);
            packet.moverGUID = getGUID();
            sendMessageToSet(packet, true);
        }

        return true;
    }

    public final boolean setFeatherFall(boolean enable) {
        // Temporarily disabled for short lived auras that unapply before client had time to ACK applying
        //if (enable == hasUnitMovementFlag(MovementFlag.FallingSlow))
        //return false;

        if (enable) {
            addUnitMovementFlag(MovementFlag.FallingSlow);
        } else {
            removeUnitMovementFlag(MovementFlag.FallingSlow);
        }


        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(enable ? ServerOpcode.MoveSetFeatherFall : ServerOpcode.MoveSetNormalFall);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        } else {
            MoveSplineSetFlag packet = new MoveSplineSetFlag(enable ? ServerOpcode.MoveSplineSetFeatherFall : ServerOpcode.MoveSplineSetNormalFall);
            packet.moverGUID = getGUID();
            sendMessageToSet(packet, true);
        }

        return true;
    }

    public final boolean setHover(boolean enable) {
        return setHover(enable, true);
    }

    public final boolean setHover(boolean enable, boolean updateAnimTier) {
        if (enable == hasUnitMovementFlag(MovementFlag.Hover)) {
            return false;
        }

        float hoverHeight = getUnitData().hoverHeight;

        if (enable) {
            //! No need to check height on ascent
            addUnitMovementFlag(MovementFlag.Hover);

            if (hoverHeight != 0 && getLocation().getZ() - getFloorZ() < hoverHeight) {
                updateHeight(getLocation().getZ() + hoverHeight);
            }
        } else {
            removeUnitMovementFlag(MovementFlag.Hover);

            //! Dying creatures will MoveFall from setDeathState
            if (hoverHeight != 0 && (!isDying() || !isUnit())) {
                var newZ = Math.max(getFloorZ(), getLocation().getZ() - hoverHeight);
                newZ = updateAllowedPositionZ(getLocation().getX(), getLocation().getY(), newZ);
                updateHeight(newZ);
            }
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(enable ? ServerOpcode.MoveSetHovering : ServerOpcode.MoveUnsetHovering);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        } else {
            MoveSplineSetFlag packet = new MoveSplineSetFlag(enable ? ServerOpcode.MoveSplineSetHover : ServerOpcode.MoveSplineUnsetHover);
            packet.moverGUID = getGUID();
            sendMessageToSet(packet, true);
        }

        if (isCreature() && updateAnimTier && isAlive() && !hasUnitState(UnitState.Root) && !toCreature().getMovementTemplate().isRooted()) {
            if (isGravityDisabled()) {
                setAnimTier(animTier.Fly);
            } else if (isHovering()) {
                setAnimTier(animTier.Hover);
            } else {
                setAnimTier(animTier.ground);
            }
        }

        return true;
    }

    public final boolean isWithinCombatRange(Unit obj, float dist2compare) {
        if (!obj || !isInMap(obj) || !inSamePhase(obj)) {
            return false;
        }

        var dx = getLocation().getX() - obj.getLocation().getX();
        var dy = getLocation().getY() - obj.getLocation().getY();
        var dz = getLocation().getZ() - obj.getLocation().getZ();
        var distsq = dx * dx + dy * dy + dz * dz;

        var sizefactor = getCombatReach() + obj.getCombatReach();
        var maxdist = dist2compare + sizefactor;

        return distsq < maxdist * maxdist;
    }

    public final boolean isInFrontInMap(Unit target, float distance) {
        return isInFrontInMap(target, distance, MathUtil.PI);
    }

    public final boolean isInFrontInMap(Unit target, float distance, float arc) {
        return isWithinDistInMap(target, distance) && getLocation().hasInArc(arc, target.getLocation());
    }

    public final boolean isInBackInMap(Unit target, float distance) {
        return isInBackInMap(target, distance, MathUtil.PI);
    }

    public final boolean isInBackInMap(Unit target, float distance, float arc) {
        return isWithinDistInMap(target, distance) && !getLocation().hasInArc(MathUtil.TwoPi - arc, target.getLocation());
    }

    public final boolean isInAccessiblePlaceFor(Creature c) {
        if (isInWater()) {
            return c.canEnterWater();
        } else {
            return c.canWalk() || c.canFly();
        }
    }

    public final void nearTeleportTo(float x, float y, float z, float orientation) {
        nearTeleportTo(x, y, z, orientation, false);
    }

    public final void nearTeleportTo(float x, float y, float z, float orientation, boolean casting) {
        nearTeleportTo(new Position(x, y, z, orientation), casting);
    }

    public final void nearTeleportTo(Position pos) {
        nearTeleportTo(pos, false);
    }

    public final void nearTeleportTo(Position pos, boolean casting) {
        disableSpline();

        if (isTypeId(TypeId.PLAYER)) {
            WorldLocation target = new WorldLocation(getLocation().getMapId(), pos);
            toPlayer().teleportTo(target, (TeleportToOptions.NotLeaveTransport.getValue() | TeleportToOptions.NotLeaveCombat.getValue().getValue() | TeleportToOptions.NotUnSummonPet.getValue().getValue().getValue() | (casting ? TeleportToOptions.Spell : 0).getValue().getValue().getValue()));
        } else {
            sendTeleportPacket(pos);
            updatePosition(pos, true);
            updateObjectVisibility();
        }
    }

    public final void setMovedUnit(Unit target) {
        getUnitMovedByMe().setPlayerMovingMe(null);
        setUnitMovedByMe(target);
        getUnitMovedByMe().setPlayerMovingMe(toPlayer());

        MoveSetActiveMover packet = new MoveSetActiveMover();
        packet.moverGUID = target.getGUID();
        toPlayer().sendPacket(packet);
    }

    public final void setControlled(boolean apply, UnitState state) {
        if (apply) {
            if (hasUnitState(state)) {
                return;
            }

            if (state.hasFlag(UnitState.CONTROLLED)) {
                castStop();
            }

            addUnitState(state);

            switch (state) {
                case STUNNED:
                    setStunned(true);

                    break;
                case ROOT:
                    if (!hasUnitState(UnitState.Stunned)) {
                        setRooted(true);
                    }

                    break;
                case CONFUSED:
                    if (!hasUnitState(UnitState.Stunned)) {
                        clearUnitState(UnitState.MeleeAttacking);
                        sendMeleeAttackStop();
                        // SendAutoRepeatCancel ?
                        setConfused(true);
                    }

                    break;
                case FLEEING:
                    if (!hasUnitState(UnitState.STUNNED | UnitState.CONFUSED)) {
                        clearUnitState(UnitState.MELEE_ATTACKING);
                        sendMeleeAttackStop();
                        // SendAutoRepeatCancel ?
                        setFeared(true);
                    }

                    break;
                default:
                    break;
            }
        } else {
            switch (state) {
                case STUNNED:
                    if (hasAuraType(AuraType.MOD_STUN) || hasAuraType(AuraType.MOD_STUN_DISABLE_GRAVITY)) {
                        return;
                    }

                    clearUnitState(state);
                    setStunned(false);

                    break;
                case ROOT:
                    if (hasAuraType(AuraType.MOD_ROOT) || hasAuraType(AuraType.MOD_ROOT_2) || hasAuraType(AuraType.MOD_ROOT_DISABLE_GRAVITY) || getVehicle1() != null || (isCreature() && toCreature().getMovementTemplate().isRooted())) {
                        return;
                    }

                    clearUnitState(state);

                    if (!hasUnitState(UnitState.STUNNED)) {
                        setRooted(false);
                    }

                    break;
                case CONFUSED:
                    if (hasAuraType(AuraType.MOD_CONFUSE)) {
                        return;
                    }

                    clearUnitState(state);
                    setConfused(false);

                    break;
                case FLEEING:
                    if (hasAuraType(AuraType.MOD_FEAR)) {
                        return;
                    }

                    clearUnitState(state);
                    setFeared(false);

                    break;
                default:
                    return;
            }

            applyControlStatesIfNeeded();
        }
    }

    public final void setRooted(boolean apply) {
        setRooted(apply, false);
    }

    public final void setRooted(boolean apply, boolean packetOnly) {
        if (!packetOnly) {
            if (apply) {
                // MOVEMENTFLAG_ROOT cannot be used in conjunction with MOVEMENTFLAG_MASK_MOVING (tested 3.3.5a)
                // this will freeze clients. That's why we remove MOVEMENTFLAG_MASK_MOVING before
                // setting MOVEMENTFLAG_ROOT
                removeUnitMovementFlag(MovementFlag.MaskMoving);
                addUnitMovementFlag(MovementFlag.Root);
                stopMoving();
            } else {
                removeUnitMovementFlag(MovementFlag.Root);
            }
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer(); // unit controlled by a player.

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(apply ? ServerOpcode.MoveRoot : ServerOpcode.MoveUnroot);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        } else {
            MoveSplineSetFlag packet = new MoveSplineSetFlag(apply ? ServerOpcode.MoveSplineRoot : ServerOpcode.MoveSplineUnroot);
            packet.moverGUID = getGUID();
            sendMessageToSet(packet, true);
        }
    }

    public final boolean canFreeMove() {
        return !hasUnitState(UnitState.Confused.getValue() | UnitState.Fleeing.getValue().getValue() | UnitState.InFlight.getValue().getValue().getValue() | UnitState.Root.getValue().getValue().getValue().getValue() | UnitState.Stunned.getValue().getValue().getValue().getValue().getValue() | UnitState.Distracted.getValue().getValue().getValue().getValue().getValue()) && getOwnerGUID().isEmpty();
    }

    public final void mount(int mount, int vehicleId) {
        mount(mount, vehicleId, 0);
    }

    public final void mount(int mount) {
        mount(mount, 0, 0);
    }

    public final void mount(int mount, int vehicleId, int creatureEntry) {
        removeAurasByType(AuraType.COSMETIC_MOUNTED);

        if (mount != 0) {
            setMountDisplayId(mount);
        }

        setUnitFlag(UnitFlag.MOUNT);

        calculateHoverHeight();

        var player = toPlayer();

        if (player != null) {
            // mount as a vehicle
            if (vehicleId != 0) {
                if (createVehicleKit(vehicleId, creatureEntry)) {
                    player.sendOnCancelExpectedVehicleRideAura();

                    // mounts can also have accessories
                    vehicleKit.InstallAllAccessories(false);
                }
            }

            // unsummon pet
            var pet = player.getCurrentPet();

            if (pet != null) {
                var bg = toPlayer().getBattleground();

                // don't unsummon pet in arena but SetFlag UNIT_FLAG_STUNNED to disable pet's interface
                if (bg && bg.isArena()) {
                    pet.setUnitFlag(UnitFlag.Stunned);
                } else {
                    player.unsummonPetTemporaryIfAny();
                }
            }

            // if we have charmed npc, stun him also (everywhere)
            var charm = player.getCharmed();

            if (charm) {
                if (charm.getObjectTypeId() == TypeId.UNIT) {
                    charm.setUnitFlag(UnitFlag.Stunned);
                }
            }

            player.sendMovementSetCollisionHeight(player.getCollisionHeight(), UpdateCollisionHeightReason.mount);
        }

        removeAurasWithInterruptFlags(SpellAuraInterruptFlag.mount);
    }

    public final void dismount() {
        if (!isMounted()) {
            return;
        }

        setMountDisplayId(0);
        removeUnitFlag(UnitFlag.mount);

        var thisPlayer = toPlayer();

        if (thisPlayer != null) {
            thisPlayer.sendMovementSetCollisionHeight(thisPlayer.getCollisionHeight(), UpdateCollisionHeightReason.mount);
        }

        // dismount as a vehicle
        if (isTypeId(TypeId.PLAYER) && vehicleKit != null) {
            // Remove vehicle from player
            removeVehicleKit();
        }

        removeAurasWithInterruptFlags(SpellAuraInterruptFlag.Dismount);

        // only resummon old pet if the player is already added to a map
        // this prevents adding a pet to a not created map which would otherwise cause a crash
        // (it could probably happen when logging in after a previous crash)
        var player = toPlayer();

        if (player != null) {
            var pPet = player.getCurrentPet();

            if (pPet != null) {
                if (pPet.hasUnitFlag(UnitFlag.Stunned) && !pPet.hasUnitState(UnitState.Stunned)) {
                    pPet.removeUnitFlag(UnitFlag.Stunned);
                }
            } else {
                player.resummonPetTemporaryUnSummonedIfAny();
            }

            // if we have charmed npc, remove stun also
            var charm = player.getCharmed();

            if (charm) {
                if (charm.getObjectTypeId() == TypeId.UNIT && charm.hasUnitFlag(UnitFlag.Stunned) && !charm.hasUnitState(UnitState.Stunned)) {
                    charm.removeUnitFlag(UnitFlag.Stunned);
                }
            }
        }
    }

    public final boolean createVehicleKit(int id, int creatureEntry) {
        return createVehicleKit(id, creatureEntry, false);
    }

    public final boolean createVehicleKit(int id, int creatureEntry, boolean loading) {
        var vehInfo = CliDB.VehicleStorage.get(id);

        if (vehInfo == null) {
            return false;
        }

        setVehicleKit(new vehicle(this, vehInfo, creatureEntry));
        updateFlag.vehicle = true;
        setUnitTypeMask(UnitTypeMask.forValue(getUnitTypeMask().getValue() | getUnitTypeMask().vehicle.getValue()));

        if (!loading) {
            sendSetVehicleRecId(id);
        }

        return true;
    }

    public final void removeVehicleKit() {
        removeVehicleKit(false);
    }

    public final void removeVehicleKit(boolean onRemoveFromWorld) {
        if (getVehicleKit() == null) {
            return;
        }

        if (!onRemoveFromWorld) {
            sendSetVehicleRecId(0);
        }

        getVehicleKit().Uninstall();

        setVehicleKit(null);

        updateFlag.vehicle = false;
        setUnitTypeMask(UnitTypeMask.forValue(getUnitTypeMask().getValue() & ~getUnitTypeMask().vehicle.getValue()));
        removeNpcFlag(NPCFlag.SpellClick.getValue() | NPCFlag.PlayerVehicle.getValue());
    }

    public final boolean setIgnoreMovementForces(boolean ignore) {
        if (ignore == hasExtraUnitMovementFlag(MovementFlag2.IgnoreMovementForces)) {
            return false;
        }

        if (ignore) {
            addExtraUnitMovementFlag(MovementFlag2.IgnoreMovementForces);
        } else {
            removeExtraUnitMovementFlag(MovementFlag2.IgnoreMovementForces);
        }

        ServerOpcode[] ignoreMovementForcesOpcodeTable = {ServerOpcode.MoveUnsetIgnoreMovementForces, ServerOpcode.MoveSetIgnoreMovementForces};

        var movingPlayer = getPlayerMovingMe1();

        if (movingPlayer != null) {
            MoveSetFlag packet = new MoveSetFlag(ignoreMovementForcesOpcodeTable[ignore ? 1 : 0]);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            movingPlayer.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, movingPlayer);
        }

        return true;
    }

    public final void updateMovementForcesModMagnitude() {
        var modMagnitude = (float) getTotalAuraMultiplier(AuraType.MOD_MOVEMENT_FORCE_MAGNITUDE);

        var movingPlayer = getPlayerMovingMe1();

        if (movingPlayer != null) {
            MoveSetSpeed setModMovementForceMagnitude = new MoveSetSpeed(ServerOpCode.SMSG_MOVE_SET_MOD_MOVEMENT_FORCE_MAGNITUDE);
            setModMovementForceMagnitude.moverGUID = getGUID();
            setModMovementForceMagnitude.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            setModMovementForceMagnitude.speed = modMagnitude;
            movingPlayer.sendPacket(setModMovementForceMagnitude);
            movingPlayer.setMovementForceModMagnitudeChanges(movingPlayer.getMovementForceModMagnitudeChanges() + 1);
        } else {
            MoveUpdateSpeed updateModMovementForceMagnitude = new MoveUpdateSpeed(ServerOpcode.MoveUpdateModMovementForceMagnitude);
            updateModMovementForceMagnitude.status = getMovementInfo();
            updateModMovementForceMagnitude.speed = modMagnitude;
            sendMessageToSet(updateModMovementForceMagnitude, true);
        }

        if (modMagnitude != 1.0f && movementForces == null) {
            movementForces = new movementForces();
        }

        if (movementForces != null) {
            movementForces.setModMagnitude(modMagnitude);

            if (movementForces.isEmpty()) {
                movementForces = new movementForces();
            }
        }
    }

    public final void addUnitMovementFlag(MovementFlag f) {
        movementInfo.flags.addFlag(f);
    }

    public final void removeUnitMovementFlag(MovementFlag f) {
        movementInfo.flags.removeFlag(f);
    }

    public final boolean hasUnitMovementFlag(MovementFlag f) {
        return movementInfo.flags.hasFlag(f);
    }

    public final boolean hasUnitMovementFlag(MovementFlag... f) {
        return movementInfo.flags.hasAnyFlag(f);
    }

    public final int getUnitMovementFlags() {
        return movementInfo.flags.getFlag();
    }

    public final void setUnitMovementFlags(MovementFlag... f) {
        movementInfo.flags.set(f);
    }

    public final void addExtraUnitMovementFlag(MovementFlag2 f) {
        movementInfo.flags2.addFlag(f);
    }

    public final void removeExtraUnitMovementFlag(MovementFlag2 f) {
        movementInfo.flags2.removeFlag(f);
    }

    public final boolean hasExtraUnitMovementFlag(MovementFlag2 f) {
        return movementInfo.flags2.hasFlag(f);
    }

    public final int getExtraUnitMovementFlags() {
        return movementInfo.flags2.getFlag();
    }

    public final void setExtraUnitMovementFlags(MovementFlag2... f) {
        movementInfo.flags2.set(f);
    }

    public final void addExtraUnitMovementFlag2(MovementFlag3 f) {
        movementInfo.flags3.addFlag(f);
    }

    public final void removeExtraUnitMovementFlag2(MovementFlag3 f) {
        movementInfo.flags3.removeFlag(f);
    }

    public final boolean hasExtraUnitMovementFlag2(MovementFlag3 f) {
        return movementInfo.flags3.hasFlag(f);
    }

    public final int getExtraUnitMovementFlags2() {
        return movementInfo.flags3.getFlag();
    }

    public final void setExtraUnitMovementFlags2(MovementFlag3... f) {
        movementInfo.flags3.set(f);
    }

    public final void disableSpline() {
        getMovementInfo().removeMovementFlag(MovementFlag.Forward);
        getMoveSpline().interrupt();
    }

    //Transport
    @Override
    public ObjectGuid getTransGUID() {
        if (getVehicle1() != null) {
            return getVehicleBase().getGUID();
        }

        if (getTransport() != null) {
            return getTransport().getTransportGUID();
        }

        return ObjectGuid.Empty;
    }

    //Teleport
    public final void sendTeleportPacket(Position pos) {
        // SMSG_MOVE_UPDATE_TELEPORT is sent to nearby players to signal the teleport
        // SMSG_MOVE_TELEPORT is sent to self in order to trigger CMSG_MOVE_TELEPORT_ACK and update the position server side

        MoveUpdateTeleport moveUpdateTeleport = new MoveUpdateTeleport();
        moveUpdateTeleport.status = getMovementInfo();

        if (movementForces != null) {
            moveUpdateTeleport.movementForces = movementForces.getForces();
        }

        var broadcastSource = this;

        // should this really be the unit _being_ moved? not the unit doing the moving?
        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            var newPos = pos.Copy();

            var transportBase = getDirectTransport();

            if (transportBase != null) {
                transportBase.calculatePassengerOffset(newPos);
            }

            MoveTeleport moveTeleport = new MoveTeleport();
            moveTeleport.moverGUID = getGUID();
            moveTeleport.pos = newPos;

            if (ObjectGuid.opNotEquals(getTransGUID(), ObjectGuid.Empty)) {
                moveTeleport.transportGUID = getTransGUID();
            }

            moveTeleport.facing = newPos.getO();
            moveTeleport.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(moveTeleport);

            broadcastSource = playerMover;
        } else {
            // This is the only packet sent for creatures which contains MovementInfo structure
            // we do not update m_movementInfo for creatures so it needs to be done manually here
            moveUpdateTeleport.status.setGuid(getGUID());
            moveUpdateTeleport.status.getPos().relocate(pos);
            moveUpdateTeleport.status.setTime(time.MSTime);
            var transportBase = getDirectTransport();

            if (transportBase != null) {
                var newPos = pos.Copy();
                transportBase.calculatePassengerOffset(newPos);
                moveUpdateTeleport.status.transport.pos.relocate(newPos);
            }
        }

        // Broadcast the packet to everyone except self.
        broadcastSource.sendMessageToSet(moveUpdateTeleport, false);
    }

    private void propagateSpeedChange() {
        getMotionMaster().propagateSpeedChange();
    }

    private void sendMoveKnockBack(Player player, float speedXY, float speedZ, float vcos, float vsin) {
        MoveKnockBack moveKnockBack = new MoveKnockBack();
        moveKnockBack.moverGUID = getGUID();
        moveKnockBack.sequenceIndex = getMovementCounter();
        setMovementCounter(getMovementCounter() + 1);
        moveKnockBack.speeds.horzSpeed = speedXY;
        moveKnockBack.speeds.vertSpeed = speedZ;
        moveKnockBack.direction = new Vector2(vcos, vsin);
        player.sendPacket(moveKnockBack);
    }

    private boolean setCollision(boolean disable) {
        if (disable == hasUnitMovementFlag(MovementFlag.DisableCollision)) {
            return false;
        }

        if (disable) {
            addUnitMovementFlag(MovementFlag.DisableCollision);
        } else {
            removeUnitMovementFlag(MovementFlag.DisableCollision);
        }

        var playerMover = getUnitBeingMoved() == null ? null : getUnitBeingMoved().toPlayer();

        if (playerMover) {
            MoveSetFlag packet = new MoveSetFlag(disable ? ServerOpcode.MoveSplineEnableCollision : ServerOpcode.MoveEnableCollision);
            packet.moverGUID = getGUID();
            packet.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            playerMover.sendPacket(packet);

            MoveUpdate moveUpdate = new moveUpdate();
            moveUpdate.status = getMovementInfo();
            sendMessageToSet(moveUpdate, playerMover);
        } else {
            MoveSplineSetFlag packet = new MoveSplineSetFlag(disable ? ServerOpcode.MoveSplineDisableCollision : ServerOpcode.MoveDisableCollision);
            packet.moverGUID = getGUID();
            sendMessageToSet(packet, true);
        }

        return true;
    }

    private void updateOrientation(float orientation) {
        getLocation().setO(orientation);

        if (isVehicle()) {
            vehicleKit.RelocatePassengers();
        }
    }

    //! Only server-side height update, does not broadcast to client
    private void updateHeight(float newZ) {
        getLocation().relocate(getLocation().getX(), getLocation().getY(), newZ);

        if (isVehicle()) {
            vehicleKit.RelocatePassengers();
        }
    }

    private void applyControlStatesIfNeeded() {
        // Unit States might have been already cleared but auras still present. I need to check with HasAuraType
        if (hasUnitState(UnitState.Stunned) || hasAuraType(AuraType.ModStun) || hasAuraType(AuraType.ModStunDisableGravity)) {
            setStunned(true);
        }

        if (hasUnitState(UnitState.Root) || hasAuraType(AuraType.ModRoot) || hasAuraType(AuraType.ModRoot2) || hasAuraType(AuraType.ModRootDisableGravity)) {
            setRooted(true);
        }

        if (hasUnitState(UnitState.Confused) || hasAuraType(AuraType.ModConfuse)) {
            setConfused(true);
        }

        if (hasUnitState(UnitState.Fleeing) || hasAuraType(AuraType.ModFear)) {
            setFeared(true);
        }
    }

    private void setStunned(boolean apply) {
        if (apply) {
            setTarget(ObjectGuid.Empty);
            setUnitFlag(UnitFlag.Stunned);

            stopMoving();

            if (isTypeId(TypeId.PLAYER)) {
                setStandState(UnitStandStateType.Stand);
            }

            setRooted(true);

            castStop();
        } else {
            if (isAlive() && getVictim() != null) {
                setTarget(getVictim().getGUID());
            }

            // don't remove UNIT_FLAG_STUNNED for pet when owner is mounted (disabled pet's interface)
            var owner = getCharmerOrOwner();

            if (owner == null || !owner.isTypeId(TypeId.PLAYER) || !owner.toPlayer().isMounted()) {
                removeUnitFlag(UnitFlag.Stunned);
            }

            if (!hasUnitState(UnitState.Root)) // prevent moving if it also has root effect
            {
                setRooted(false);
            }
        }
    }

    private void setConfused(boolean apply) {
        if (apply) {
            setTarget(ObjectGuid.Empty);
            getMotionMaster().moveConfused();
        } else {
            if (isAlive()) {
                getMotionMaster().remove(MovementGeneratorType.Confused);

                if (getVictim() != null) {
                    setTarget(getVictim().getGUID());
                }
            }
        }

        // block / allow control to real player in control (eg charmer)
        if (isPlayer()) {
            if (getPlayerMovingMe()) {
                getPlayerMovingMe().setClientControl(this, !apply);
            }
        }
    }

    private void sendSetVehicleRecId(int vehicleId) {
        var player = toPlayer();

        if (player) {
            MoveSetVehicleRecID moveSetVehicleRec = new MoveSetVehicleRecID();
            moveSetVehicleRec.moverGUID = getGUID();
            moveSetVehicleRec.sequenceIndex = getMovementCounter();
            setMovementCounter(getMovementCounter() + 1);
            moveSetVehicleRec.vehicleRecID = vehicleId;
            player.sendPacket(moveSetVehicleRec);
        }

        SetVehicleRecID setVehicleRec = new SetVehicleRecID();
        setVehicleRec.vehicleGUID = getGUID();
        setVehicleRec.vehicleRecID = vehicleId;
        sendMessageToSet(setVehicleRec, true);
    }

    private void applyMovementForce(ObjectGuid id, Vector3 origin, float magnitude, MovementForceType type, Vector3 direction) {
        applyMovementForce(id, origin, magnitude, type, direction, null);
    }

    private void applyMovementForce(ObjectGuid id, Vector3 origin, float magnitude, MovementForceType type, Vector3 direction, ObjectGuid transportGuid) {
        if (movementForces == null) {
            movementForces = new movementForces();
        }

        MovementForce force = new movementForce();
        force.setId(id);
        force.setOrigin(origin);
        force.setDirection(direction);

        if (transportGuid.isMOTransport()) {
            force.setTransportID((int) transportGuid.getCounter());
        }

        force.setMagnitude(magnitude);
        force.setType(type);

        if (movementForces.add(force)) {
            var movingPlayer = getPlayerMovingMe1();

            if (movingPlayer != null) {
                MoveApplyMovementForce applyMovementForce = new MoveApplyMovementForce();
                applyMovementForce.moverGUID = getGUID();
                applyMovementForce.sequenceIndex = (int) getMovementCounter();
                setMovementCounter(getMovementCounter() + 1);
                applyMovementForce.FORCE = force;
                movingPlayer.sendPacket(applyMovementForce);
            } else {
                MoveUpdateApplyMovementForce updateApplyMovementForce = new MoveUpdateApplyMovementForce();
                updateApplyMovementForce.status = getMovementInfo();
                updateApplyMovementForce.FORCE = force;
                sendMessageToSet(updateApplyMovementForce, true);
            }
        }
    }

    private void removeMovementForce(ObjectGuid id) {
        if (movementForces == null) {
            return;
        }

        if (movementForces.remove(id)) {
            var movingPlayer = getPlayerMovingMe1();

            if (movingPlayer != null) {
                MoveRemoveMovementForce moveRemoveMovementForce = new MoveRemoveMovementForce();
                moveRemoveMovementForce.moverGUID = getGUID();
                moveRemoveMovementForce.sequenceIndex = (int) getMovementCounter();
                setMovementCounter(getMovementCounter() + 1);
                moveRemoveMovementForce.ID = id;
                movingPlayer.sendPacket(moveRemoveMovementForce);
            } else {
                MoveUpdateRemoveMovementForce updateRemoveMovementForce = new MoveUpdateRemoveMovementForce();
                updateRemoveMovementForce.status = getMovementInfo();
                updateRemoveMovementForce.triggerGUID = id;
                sendMessageToSet(updateRemoveMovementForce, true);
            }
        }

        if (movementForces.isEmpty()) {
            movementForces = new movementForces();
        }
    }

    private void setPlayHoverAnim(boolean enable) {
        playHoverAnim = enable;
        SetPlayHoverAnim data = new SetPlayHoverAnim();
        data.unitGUID = getGUID();
        data.playHoverAnim = enable;
        sendMessageToSet(data, true);
    }

    private Player getPlayerBeingMoved() {
        var mover = getUnitBeingMoved();

        if (mover) {
            return mover.toPlayer();
        }

        return null;
    }

    private void updateSplineMovement(int delta) {
        if (getMoveSpline().finalized()) {
            return;
        }
        getMoveSpline().updateState(delta);
        var arrived = getMoveSpline().finalized();
        if (arrived) {
            disableSpline();

            var animTier = getMoveSpline().getAnimation();

            if (animTier != null) {
                setAnimTier(animTier);
            }
        }

        updateSplinePosition();
    }

    private void updateSplinePosition() {
        var loc = new Position(getMoveSpline().computePosition());

        if (getMoveSpline().onTransport) {
            getMovementInfo().transport.pos.relocate(loc);

            var transport = getDirectTransport();

            if (transport != null) {
                transport.calculatePassengerPosition(loc);
            } else {
                return;
            }
        }

        if (hasUnitState(UnitState.CannotTurn)) {
            loc.setO(getLocation().getO());
        }

        updatePosition(loc);
    }

    private void interruptMovementBasedAuras() {
        // TODO: Check if orientation transport offset changed instead of only global orientation
        if (positionUpdateInfo.turned) {
            removeAurasWithInterruptFlags(SpellAuraInterruptFlag.Turning);
        }

        if (positionUpdateInfo.relocated && !getVehicle1()) {
            removeAurasWithInterruptFlags(SpellAuraInterruptFlag.Moving);
        }
    }

    public final CharmInfo getCharmInfo() {
        return charmInfo;
    }

    public final CharmInfo initCharmInfo() {
        if (charmInfo == null) {
            charmInfo = new CharmInfo(this);
        }

        return charmInfo;
    }

    public final void updateCharmAI() {
        if (isCharmed()) {
            UnitAI newAI = null;

            if (isPlayer()) {
                var charmer = getCharmer();

                if (charmer != null) {
                    // first, we check if the creature's own AI specifies an override playerai for its owned players
                    var creatureCharmer = charmer.toCreature();

                    if (creatureCharmer != null) {
                        var charmerAI = creatureCharmer.getAi();

                        if (charmerAI != null) {
                            newAI = charmerAI.getAIForCharmedPlayer(toPlayer());
                        }
                    } else {
                        Log.outError(LogFilter.misc, String.format("Attempt to assign charm AI to player %1$s who is charmed by non-creature %2$s.", getGUID(), getCharmerGUID()));
                    }
                }

                if (newAI == null) // otherwise, we default to the generic one
                {
                    newAI = new SimpleCharmedPlayerAI(toPlayer());
                }
            } else {
                if (isPossessed() || isVehicle()) {
                    newAI = new PossessedAI(toCreature());
                } else {
                    newAI = new PetAI(toCreature());
                }
            }

            setAi(newAI);
            newAI.onCharmed(true);
        } else {
            restoreDisabledAI();
            // Hack: this is required because we want to call onCharmed(true) on the restored AI
            refreshAI();
            var ai = getAi();

            if (ai != null) {
                ai.onCharmed(true);
            }
        }
    }

    public final void setMinion(Minion minion, boolean apply) {
        Log.outDebug(LogFilter.unit, "SetMinion {0} for {1}, apply {2}", minion.getEntry(), getEntry(), apply);

        if (apply) {
            if (!minion.getOwnerGUID().isEmpty()) {
                Log.outFatal(LogFilter.unit, "SetMinion: Minion {0} is not the minion of owner {1}", minion.getEntry(), getEntry());

                return;
            }

            if (!isInWorld()) {
                Log.outFatal(LogFilter.unit, String.format("SetMinion: Minion being added to owner not in world. Minion: %1$s, Owner: %2$s", minion.getGUID(), getDebugInfo()));

                return;
            }

            minion.setOwnerGUID(getGUID());

            if (!getControlled().contains(minion)) {
                getControlled().add(minion);
            }

            if (isTypeId(TypeId.PLAYER)) {
                minion.setControlledByPlayer(true);
                minion.setUnitFlag(UnitFlag.PlayerControlled);
            }

            // Can only have one pet. If a new one is summoned, dismiss the old one.
            if (minion.isGuardianPet()) {
                var oldPet = getGuardianPet();

                if (oldPet) {
                    if (oldPet != minion && (oldPet.isPet() || minion.isPet() || oldPet.getEntry() != minion.getEntry())) {
                        // remove existing minion pet
                        var oldPetAsPet = oldPet.getAsPet();

                        if (oldPetAsPet != null) {
                            oldPetAsPet.remove(PetSaveMode.NotInSlot);
                        } else {
                            oldPet.unSummon();
                        }

                        setPetGUID(minion.getGUID());
                        setMinionGUID(ObjectGuid.Empty);
                    }
                } else {
                    setPetGUID(minion.getGUID());
                    setMinionGUID(ObjectGuid.Empty);
                }
            }

            if (minion.hasUnitTypeMask(UnitTypeMask.ControlableGuardian)) {
                if (getMinionGUID().isEmpty()) {
                    setMinionGUID(minion.getGUID());
                }
            }

            var properties = minion.summonPropertiesRecord;

            if (properties != null && properties.title == SummonTitle.Companion) {
                setCritterGUID(minion.getGUID());
                var thisPlayer = toPlayer();

                if (thisPlayer != null) {
                    if (properties.getFlags().hasFlag(SummonPropertiesFlags.SummonFromBattlePetJournal)) {
                        var pet = thisPlayer.getSession().getBattlePetMgr().getPet(thisPlayer.getSummonedBattlePetGUID());

                        if (pet != null) {
                            minion.setBattlePetCompanionGUID(thisPlayer.getSummonedBattlePetGUID());
                            minion.setBattlePetCompanionNameTimestamp((int) pet.nameTimestamp);
                            minion.setWildBattlePetLevel(pet.packetInfo.level);

                            var display = pet.packetInfo.displayID;

                            if (display != 0) {
                                minion.setDisplayId(display);
                                minion.setNativeDisplayId(display);
                            }
                        }
                    }
                }
            }

            // pvP, FFAPvP
            minion.replaceAllPvpFlags(getPvpFlags());

            // FIXME: hack, speed must be set only at follow
            if (isTypeId(TypeId.PLAYER) && minion.isPet()) {
                for (UnitMoveType i = 0; i.getValue() < UnitMoveType.max.getValue(); ++i) {
                    minion.setSpeedRate(i, getSpeedRate()[i.getValue()]);
                }
            }

            // Send infinity cooldown - client does that automatically but after relog cooldown needs to be set again
            var spellInfo = global.getSpellMgr().getSpellInfo(minion.getUnitData().createdBySpell, Difficulty.NONE);

            if (spellInfo != null && spellInfo.IsCooldownStartedOnEvent) {
                getSpellHistory().startCooldown(spellInfo, 0, null, true);
            }
        } else {
            if (ObjectGuid.opNotEquals(minion.getOwnerGUID(), getGUID())) {
                Log.outFatal(LogFilter.unit, "SetMinion: Minion {0} is not the minion of owner {1}", minion.getEntry(), getEntry());

                return;
            }

            getControlled().remove(minion);

            if (minion.summonPropertiesRecord != null && minion.summonPropertiesRecord.title == SummonTitle.Companion) {
                if (Objects.equals(getCritterGUID(), minion.getGUID())) {
                    setCritterGUID(ObjectGuid.Empty);
                }
            }

            if (minion.isGuardianPet()) {
                if (Objects.equals(getPetGUID(), minion.getGUID())) {
                    setPetGUID(ObjectGuid.Empty);
                }
            } else if (minion.isTotem()) {
                // All summoned by totem minions must disappear when it is removed.
                var spInfo = global.getSpellMgr().getSpellInfo(minion.toTotem().getSpell(), Difficulty.NONE);

                if (spInfo != null) {
                    for (var spellEffectInfo : spInfo.getEffects()) {
                        if (spellEffectInfo == null || !spellEffectInfo.isEffect(SpellEffectName.summon)) {
                            continue;
                        }

                        removeAllMinionsByEntry((int) spellEffectInfo.miscValue);
                    }
                }
            }

            var spellInfo = global.getSpellMgr().getSpellInfo(minion.getUnitData().createdBySpell, Difficulty.NONE);

            // Remove infinity cooldown
            if (spellInfo != null && spellInfo.IsCooldownStartedOnEvent) {
                getSpellHistory().sendCooldownEvent(spellInfo);
            }

            if (Objects.equals(getMinionGUID(), minion.getGUID())) {
                setMinionGUID(ObjectGuid.Empty);

                // Check if there is another minion
                for (var unit : getControlled()) {
                    // do not use this check, creature do not have charm guid
                    if (Objects.equals(getGUID(), unit.getCharmerGUID())) {
                        continue;
                    }

                    if (ObjectGuid.opNotEquals(unit.getOwnerGUID(), getGUID())) {
                        continue;
                    }

                    if (!unit.hasUnitTypeMask(UnitTypeMask.Guardian)) {
                        continue;
                    }

                    setMinionGUID(unit.getGUID());

                    // show another pet bar if there is no charm bar
                    if (getObjectTypeId() == TypeId.PLAYER && getCharmedGUID().isEmpty()) {
                        if (unit.isPet()) {
                            toPlayer().petSpellInitialize();
                        } else {
                            toPlayer().charmSpellInitialize();
                        }
                    }

                    break;
                }
            }
        }

        updatePetCombatState();
    }

    public final boolean setCharmedBy(Unit charmer, CharmType type) {
        return setCharmedBy(charmer, type, null);
    }

    public final boolean setCharmedBy(Unit charmer, CharmType type, AuraApplication aurApp) {
        if (!charmer) {
            return false;
        }

        // dismount players when charmed
        if (isTypeId(TypeId.PLAYER)) {
            removeAurasByType(AuraType.Mounted);
        }

        if (charmer.isTypeId(TypeId.PLAYER)) {
            charmer.removeAurasByType(AuraType.Mounted);
        }

        Log.outDebug(LogFilter.unit, "SetCharmedBy: charmer {0} (GUID {1}), charmed {2} (GUID {3}), type {4}.", charmer.getEntry(), charmer.getGUID().toString(), getEntry(), getGUID().toString(), type);

        if (this == charmer) {
            Log.outFatal(LogFilter.unit, "Unit:SetCharmedBy: Unit {0} (GUID {1}) is trying to charm itself!", getEntry(), getGUID().toString());

            return false;
        }

        if (isPlayer() && toPlayer().getTransport() != null) {
            Log.outFatal(LogFilter.unit, "Unit:SetCharmedBy: Player on transport is trying to charm {0} (GUID {1})", getEntry(), getGUID().toString());

            return false;
        }

        // Already charmed
        if (!getCharmerGUID().isEmpty()) {
            Log.outFatal(LogFilter.unit, "Unit:SetCharmedBy: {0} (GUID {1}) has already been charmed but {2} (GUID {3}) is trying to charm it!", getEntry(), getGUID().toString(), charmer.getEntry(), charmer.getGUID().toString());

            return false;
        }

        castStop();
        attackStop();

        var playerCharmer = charmer.toPlayer();

        // Charmer stop charming
        if (playerCharmer) {
            playerCharmer.stopCastingCharm();
            playerCharmer.stopCastingBindSight();
        }

        // Charmed stop charming
        if (isTypeId(TypeId.PLAYER)) {
            toPlayer().stopCastingCharm();
            toPlayer().stopCastingBindSight();
        }

        // StopCastingCharm may remove a possessed pet?
        if (!isInWorld()) {
            Log.outFatal(LogFilter.unit, "Unit:SetCharmedBy: {0} (GUID {1}) is not in world but {2} (GUID {3}) is trying to charm it!", getEntry(), getGUID().toString(), charmer.getEntry(), charmer.getGUID().toString());

            return false;
        }

        // charm is set by aura, and aura effect remove handler was called during apply handler execution
        // prevent undefined behaviour
        if (aurApp != null && aurApp.getRemoveMode() != 0) {
            return false;
        }

        oldFactionId = getFaction();
        setFaction(charmer.getFaction());

        // Pause any Idle movement
        pauseMovement(0, 0, false);

        // Remove any active voluntary movement
        getMotionMaster().clear(MovementGeneratorPriority.NORMAL);

        // Stop any remaining spline, if no involuntary movement is found
        tangible.Func1Param<MovementGenerator, Boolean> criteria = (MovementGenerator movement) -> movement.priority == MovementGeneratorPriority.Highest;

        if (!getMotionMaster().hasMovementGenerator(criteria)) {
            stopMoving();
        }

        // Set charmed
        charmer.setCharm(this, true);

        var player = toPlayer();

        if (player) {
            if (player.isAFK()) {
                player.toggleAFK();
            }

            player.setClientControl(this, false);
        }

        // charm is set by aura, and aura effect remove handler was called during apply handler execution
        // prevent undefined behaviour
        if (aurApp != null && aurApp.getRemoveMode() != 0) {
            // properly clean up charm changes up to this point to avoid leaving the unit in partially charmed state
            setFaction(oldFactionId);
            getMotionMaster().initializeDefault();
            charmer.setCharm(this, false);

            return false;
        }

        // Pets already have a properly initialized CharmInfo, don't overwrite it.
        if (type != CharmType.vehicle && getCharmInfo() == null) {
            initCharmInfo();

            if (type == CharmType.Possess) {
                getCharmInfo().initPossessCreateSpells();
            } else {
                getCharmInfo().initCharmCreateSpells();
            }
        }

        if (playerCharmer) {
            switch (type) {
                case Vehicle:
                    setUnitFlag(UnitFlag.Possessed);
                    playerCharmer.setClientControl(this, true);
                    playerCharmer.vehicleSpellInitialize();

                    break;
                case Possess:
                    setUnitFlag(UnitFlag.Possessed);
                    charmer.setUnitFlag(UnitFlag.RemoveClientControl);
                    playerCharmer.setClientControl(this, true);
                    playerCharmer.possessSpellInitialize();
                    addUnitState(UnitState.Possessed);

                    break;
                case Charm:
                    if (isTypeId(TypeId.UNIT) && charmer.getUnitClass() == UnitClass.Warlock) {
                        var cinfo = toCreature().getCreatureTemplate();

                        if (cinfo != null && cinfo.type == creatureType.Demon) {
                            // to prevent client crash
                            setUnitClass(UnitClass.Mage);

                            // just to enable stat window
                            if (getCharmInfo() != null) {
                                getCharmInfo().setPetNumber(global.getObjectMgr().generatePetNumber(), true);
                            }

                            // if charmed two demons the same session, the 2nd gets the 1st one's name
                            setPetNameTimestamp((int) GameTime.getGameTime()); // cast can't be helped
                        }
                    }

                    playerCharmer.charmSpellInitialize();

                    break;
                default:
                case Convert:
                    break;
            }
        }

        addUnitState(UnitState.Charmed);

        var creature = toCreature();

        if (creature != null) {
            creature.refreshCanSwimFlag();
        }

        if (!isPlayer() || !charmer.isPlayer()) {
            // AI will schedule its own change if appropriate
            var ai = getAi();

            if (ai != null) {
                ai.onCharmed(false);
            } else {
                scheduleAIChange();
            }
        }

        return true;
    }

    public final void removeCharmedBy(Unit charmer) {
        if (!isCharmed()) {
            return;
        }

        charmer = getCharmer();

        CharmType type;

        if (hasUnitState(UnitState.Possessed)) {
            type = CharmType.Possess;
        } else if (charmer.isOnVehicle(this)) {
            type = CharmType.vehicle;
        } else {
            type = CharmType.charm;
        }

        castStop();
        attackStop();

        if (oldFactionId != 0) {
            setFaction(oldFactionId);
            oldFactionId = 0;
        } else {
            restoreFaction();
        }

        /**@todo Handle SLOT_IDLE motion resume
         */
        getMotionMaster().initializeDefault();

        // Vehicle should not attack its passenger after he exists the seat
        if (type != CharmType.vehicle) {
            setLastCharmerGuid(charmer.getGUID());
        }

        charmer.setCharm(this, false);
        combatManager.revalidateCombat();

        var playerCharmer = charmer.toPlayer();

        if (playerCharmer) {
            switch (type) {
                case Vehicle:
                    playerCharmer.setClientControl(this, false);
                    playerCharmer.setClientControl(charmer, true);
                    removeUnitFlag(UnitFlag.Possessed);

                    break;
                case Possess:
                    clearUnitState(UnitState.Possessed);
                    playerCharmer.setClientControl(this, false);
                    playerCharmer.setClientControl(charmer, true);
                    charmer.removeUnitFlag(UnitFlag.RemoveClientControl);
                    removeUnitFlag(UnitFlag.Possessed);

                    break;
                case Charm:
                    if (isTypeId(TypeId.UNIT) && charmer.getUnitClass() == UnitClass.Warlock) {
                        var cinfo = toCreature().getCreatureTemplate();

                        if (cinfo != null && cinfo.type == creatureType.Demon) {
                            setUnitClass(UnitClass.forValue(cinfo.unitClass));

                            if (getCharmInfo() != null) {
                                getCharmInfo().setPetNumber(0, true);
                            } else {
                                Log.outError(LogFilter.unit, "Aura:HandleModCharm: target={0} with typeid={1} has a charm aura but no charm info!", getGUID(), getObjectTypeId());
                            }
                        }
                    }

                    break;
                case Convert:
                    break;
            }
        }

        var player = toPlayer();

        if (player != null) {
            player.setClientControl(this, true);
        }

        if (playerCharmer && this != charmer.getFirstControlled()) {
            playerCharmer.sendRemoveControlBar();
        }

        // a guardian should always have charminfo
        if (!isGuardian()) {
            deleteCharmInfo();
        }

        // reset confused movement for example
        applyControlStatesIfNeeded();

        if (!isPlayer() || charmer.isCreature()) {
            var charmedAI = getAi();

            if (charmedAI != null) {
                charmedAI.onCharmed(false); // AI will potentially schedule a charm ai update
            } else {
                scheduleAIChange();
            }
        }
    }

    public final void getAllMinionsByEntry(ArrayList<TempSummon> Minions, int entry) {
        for (var i = 0; i < getControlled().size(); ++i) {
            var unit = getControlled().get(i);

            if (unit.getEntry() == entry && unit.isSummon()) // minion, actually
            {
                Minions.add(unit.toTempSummon());
            }
        }
    }

    public final void removeAllMinionsByEntry(int entry) {
        for (var i = 0; i < getControlled().size(); ++i) {
            var unit = getControlled().get(i);

            if (unit.getEntry() == entry && unit.isTypeId(TypeId.UNIT) && unit.toCreature().isSummon()) // minion, actually
            {
                unit.toTempSummon().unSummon();
            }
            // i think this is safe because i have never heard that a despawned minion will trigger a same minion
        }
    }

    public final void setCharm(Unit charm, boolean apply) {
        if (apply) {
            if (isTypeId(TypeId.PLAYER)) {
                setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().charm), charm.getGUID());
                charmed = charm;

                charm.setControlledByPlayer(true);
                // @todo maybe we can use this flag to check if controlled by player
                charm.setUnitFlag(UnitFlag.PlayerControlled);
            } else {
                charm.setControlledByPlayer(false);
            }

            // pvP, FFAPvP
            charm.replaceAllPvpFlags(getPvpFlags());

            charm.setUpdateFieldValue(charm.getValues().modifyValue(getUnitData()).modifyValue(getUnitData().charmedBy), getGUID());
            charm.charmer = this;

            isWalkingBeforeCharm = charm.isWalking();

            if (isWalkingBeforeCharm) {
                charm.setWalk(false);
            }

            if (!getControlled().contains(charm)) {
                getControlled().add(charm);
            }
        } else {
            charm.clearUnitState(UnitState.Charmed);

            if (isPlayer()) {
                setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().charm), ObjectGuid.Empty);
                charmed = null;
            }

            charm.setUpdateFieldValue(charm.getValues().modifyValue(getUnitData()).modifyValue(getUnitData().charmedBy), ObjectGuid.Empty);
            charm.charmer = null;

            var player = charm.getCharmerOrOwnerPlayerOrPlayerItself();

            if (charm.isTypeId(TypeId.PLAYER)) {
                charm.setControlledByPlayer(true);
                charm.setUnitFlag(UnitFlag.PlayerControlled);
                charm.toPlayer().updatePvPState();
            } else if (player) {
                charm.setControlledByPlayer(true);
                charm.setUnitFlag(UnitFlag.PlayerControlled);
                charm.replaceAllPvpFlags(player.getPvpFlags());
            } else {
                charm.setControlledByPlayer(false);
                charm.removeUnitFlag(UnitFlag.PlayerControlled);
                charm.replaceAllPvpFlags(UnitPVPStateFlag.NONE);
            }

            if (charm.isWalking() != isWalkingBeforeCharm) {
                charm.setWalk(isWalkingBeforeCharm);
            }

            if (charm.isTypeId(TypeId.PLAYER) || !charm.toCreature().hasUnitTypeMask(UnitTypeMask.minion) || ObjectGuid.opNotEquals(charm.getOwnerGUID(), getGUID())) {
                getControlled().remove(charm);
            }
        }

        updatePetCombatState();
    }

    public final Unit getFirstControlled() {
        // Sequence: charmed, pet, other guardians
        var unit = getCharmed();

        if (!unit) {
            var guid = getMinionGUID();

            if (!guid.isEmpty()) {
                unit = global.getObjAccessor().GetUnit(this, guid);
            }
        }

        return unit;
    }

    public void removeBindSightAuras() {
        removeAurasByType(AuraType.BIND_SIGHT);
    }

    public final void removeCharmAuras() {
        removeAurasByType(AuraType.MOD_CHARM);
        removeAurasByType(AuraType.MOD_POSSESS_PET);
        removeAurasByType(AuraType.MOD_POSSESS);
        removeAurasByType(AuraType.AOE_CHARM);
    }

    public final void removeAllControlled() {
        // possessed pet and vehicle
        if (isTypeId(TypeId.PLAYER)) {
            toPlayer().stopCastingCharm();
        }

        while (!getControlled().isEmpty()) {
            var target = getControlled().get(0);
            getControlled().remove(0);

            if (Objects.equals(target.CharmerGUID, getGUID())) {
                target.removeCharmAuras();
            } else if (Objects.equals(target.ownerGUID, getGUID()) && target.isSummon) {
                target.toTempSummon().unSummon();
            } else {
                Log.outError(LogFilter.unit, "Unit {0} is trying to release unit {1} which is neither charmed nor owned by it", getEntry(), target.entry);
            }
        }

        if (!getPetGUID().isEmpty()) {
            Log.outFatal(LogFilter.unit, "Unit {0} is not able to release its pet {1}", getEntry(), getPetGUID());
        }

        if (!getMinionGUID().isEmpty()) {
            Log.outFatal(LogFilter.unit, "Unit {0} is not able to release its minion {1}", getEntry(), getMinionGUID());
        }

        if (!getCharmedGUID().isEmpty()) {
            Log.outFatal(LogFilter.unit, "Unit {0} is not able to release its charm {1}", getEntry(), getCharmedGUID());
        }

        if (!isPet()) // pets don't use the flag for this
        {
            removeUnitFlag(UnitFlag.PET_IN_COMBAT); // m_controlled is now empty, so we know none of our minions are in combat
        }
    }

    public final void sendPetActionFeedback(PetActionFeedback msg, int spellId) {
        var owner = getOwnerUnit();

        if (!owner || !owner.isTypeId(TypeId.PLAYER)) {
            return;
        }

        PetActionFeedbackPacket petActionFeedback = new PetActionFeedbackPacket();
        petActionFeedback.spellID = spellId;
        petActionFeedback.response = msg;
        owner.toPlayer().sendPacket(petActionFeedback);
    }

    public final void sendPetTalk(PetTalk pettalk) {
        var owner = getOwnerUnit();

        if (!owner || !owner.isTypeId(TypeId.PLAYER)) {
            return;
        }

        PetActionSound petActionSound = new PetActionSound();
        petActionSound.unitGUID = getGUID();
        petActionSound.action = pettalk;
        owner.toPlayer().sendPacket(petActionSound);
    }

    public final void sendPetAIReaction(ObjectGuid guid) {
        var owner = getOwnerUnit();

        if (!owner || !owner.isTypeId(TypeId.PLAYER)) {
            return;
        }

        AIReaction packet = new AIReaction();
        packet.unitGUID = guid;
        packet.reaction = AiReaction.Hostile;

        owner.toPlayer().sendPacket(packet);
    }

    public final Pet createTamedPetFrom(Creature creatureTarget) {
        return createTamedPetFrom(creatureTarget, 0);
    }

    public final Pet createTamedPetFrom(Creature creatureTarget, int spell_id) {
        if (!isTypeId(TypeId.PLAYER)) {
            return null;
        }

        Pet pet = new pet(toPlayer(), PetType.Hunter);

        if (!pet.CreateBaseAtCreature(creatureTarget)) {
            return null;
        }

        var level = creatureTarget.getLevelForTarget(this) + 5 < getLevel() ? (getLevel() - 5) : creatureTarget.getLevelForTarget(this);

        if (!initTamedPet(pet, level, spell_id)) {
            pet.close();

            return null;
        }

        return pet;
    }

    public final Pet createTamedPetFrom(int creatureEntry) {
        return createTamedPetFrom(creatureEntry, 0);
    }

    public final Pet createTamedPetFrom(int creatureEntry, int spell_id) {
        if (!isTypeId(TypeId.PLAYER)) {
            return null;
        }

        var creatureInfo = global.getObjectMgr().getCreatureTemplate(creatureEntry);

        if (creatureInfo == null) {
            return null;
        }

        Pet pet = new pet(toPlayer(), PetType.Hunter);

        if (!pet.CreateBaseAtCreatureInfo(creatureInfo, this) || !initTamedPet(pet, getLevel(), spell_id)) {
            return null;
        }

        return pet;
    }


    public boolean isInteractionAllowedWhileHostile() {
        return hasUnitFlag2(UnitFlag2.INTERACT_WHILE_HOSTILE);
    }

    public void setInteractionAllowedWhileHostile(boolean interactionAllowed) {
        if (interactionAllowed)
            setUnitFlag2(UnitFlag2.INTERACT_WHILE_HOSTILE);
        else
            removeUnitFlag2(UnitFlag2.INTERACT_WHILE_HOSTILE);

        updateNearbyPlayersInteractions();
    }


    protected void updateNearbyPlayersInteractions() {
        if (unitData.getNpcFlags() != 0)
            forceUpdateFieldChange();
        if (unitData.getNpcFlags2() != 0)
            forceUpdateFieldChange();
    }


    public boolean isInteractionAllowedInCombat() {
        return hasUnitFlag3(UnitFlag3.ALLOW_INTERACTION_WHILE_IN_COMBAT);
    }
    public void setInteractionAllowedInCombat(boolean interactionAllowed) {
        if (interactionAllowed)
            setUnitFlag3(UnitFlag3.ALLOW_INTERACTION_WHILE_IN_COMBAT);
        else
            removeUnitFlag3(UnitFlag3.ALLOW_INTERACTION_WHILE_IN_COMBAT);

        if (isInCombat())
            updateNearbyPlayersInteractions();
    }

    public final void updatePetCombatState() {
        var state = false;

        for (var minion : getControlled()) {
            if (minion.isInCombat()) {
                state = true;

                break;
            }
        }

        if (state) {
            setUnitFlag(UnitFlag.PET_IN_COMBAT);
        } else {
            removeUnitFlag(UnitFlag.PET_IN_COMBAT);
        }
    }

    private void deleteCharmInfo() {
        if (charmInfo == null) {
            return;
        }

        charmInfo.restoreState();
        charmInfo = null;
    }

    private boolean initTamedPet(Pet pet, int level, int spell_id) {
        var player = toPlayer();
        var petStable = player.getPetStable();

        var freeActiveSlot = Array.FindIndex(petStable.ActivePets, petInfo -> petInfo == null);

        if (freeActiveSlot == -1) {
            return false;
        }

        pet.setCreatorGUID(getGUID());
        pet.setFaction(getFaction());
        pet.setCreatedBySpell(spell_id);

        if (isTypeId(TypeId.PLAYER)) {
            pet.setUnitFlag(UnitFlag.PlayerControlled);
        }

        if (!pet.initStatsForLevel(level)) {
            Log.outError(LogFilter.unit, "Pet:InitStatsForLevel() failed for creature (Entry: {0})!", pet.getEntry());

            return false;
        }

        PhasingHandler.inheritPhaseShift(pet, this);

        pet.getCharmInfo().setPetNumber(global.getObjectMgr().generatePetNumber(), true);
        // this enables pet details window (Shift+P)
        pet.InitPetCreateSpells();
        pet.setFullHealth();

        petStable.SetCurrentActivePetIndex((int) freeActiveSlot);

        PetStable.PetInfo petInfo = new PetStable.petInfo();
        pet.FillPetInfo(petInfo);
        petStable.ActivePets[freeActiveSlot] = petInfo;

        return true;
    }

    public boolean isAffectedByDiminishingReturns() {
        return (getCharmerOrOwnerPlayerOrPlayerItself() != null);
    }

    public final boolean getCanInstantCast() {
        return instantCast;
    }

    public final SpellHistory getSpellHistory() {
        return spellHistory;
    }

    public final int getSchoolImmunityMask() {
        int mask = 0;
        var schoolList = _spellImmune[SpellImmunity.school.getValue()];

        for (var pair : schoolList.KeyValueList) {
            mask |= pair.key;
        }

        return mask;
    }

    public final int getDamageImmunityMask() {
        int mask = 0;
        var damageList = _spellImmune[SpellImmunity.damage.getValue()];

        for (var pair : damageList.KeyValueList) {
            mask |= pair.key;
        }

        return mask;
    }

    public final long getMechanicImmunityMask() {
        long mask = 0;
        var mechanicList = _spellImmune[SpellImmunity.mechanic.getValue()];

        for (var pair : mechanicList.KeyValueList) {
            mask |= (1 << (int) pair.value);
        }

        return mask;
    }

    public final boolean getHasStealthAura() {
        return hasAuraType(AuraType.ModStealth);
    }

    public final boolean getHasInvisibilityAura() {
        return hasAuraType(AuraType.ModInvisibility);
    }

    public final boolean isFeared() {
        return hasAuraType(AuraType.ModFear);
    }

    private void setFeared(boolean apply) {
        if (apply) {
            setTarget(ObjectGuid.Empty);

            Unit caster = null;
            var fearAuras = getAuraEffectsByType(AuraType.ModFear);

            if (!fearAuras.isEmpty()) {
                caster = global.getObjAccessor().GetUnit(this, fearAuras.get(0).getCasterGuid());
            }

            if (caster == null) {
                caster = getAttackerForHelper();
            }

            getMotionMaster().moveFleeing(caster, (int) (fearAuras.isEmpty() ? WorldConfig.getIntValue(WorldCfg.CreatureFamilyFleeDelay) : 0)); // caster == NULL processed in MoveFleeing
        } else {
            if (isAlive()) {
                getMotionMaster().remove(MovementGeneratorType.Fleeing);

                if (getVictim() != null) {
                    setTarget(getVictim().getGUID());
                }

                if (!isPlayer() && !isInCombat()) {
                    getMotionMaster().moveTargetedHome();
                }
            }
        }

        // block / allow control to real player in control (eg charmer)
        if (isPlayer()) {
            if (getPlayerMovingMe()) {
                getPlayerMovingMe().setClientControl(this, !apply);
            }
        }
    }

    public final boolean isFrozen() {
        return hasAuraState(AuraStateType.Frozen);
    }

    public final boolean getHasRootAura() {
        return hasAuraType(AuraType.ModRoot) || hasAuraType(AuraType.ModRoot2) || hasAuraType(AuraType.ModRootDisableGravity);
    }

    public final boolean isPolymorphed() {
        var transformId = getTransformSpell();

        if (transformId == 0) {
            return false;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(transformId, getMap().getDifficultyID());

        if (spellInfo == null) {
            return false;
        }

        return spellInfo.getSpellSpecific() == SpellSpecificType.MagePolymorph;
    }

    // Auras
    public final ArrayList<Aura> getSingleCastAuras() {
        return scAuras;
    }

    public final ArrayList<Aura> getOwnedAurasList() {
        return ownedAuras.getAuras();
    }


    public final int getAppliedAurasCount() {
        return appliedAuras.getCount();
    }

    public final boolean getCanProc() {
        return getProcDeep() == 0;
    }


    public boolean hasSpell(int spellId) {
        return false;
    }

    public final void setInstantCast(boolean set) {
        instantCast = set;
    }

    public final double spellBaseDamageBonusDone(SpellSchoolMask schoolMask) {
        var thisPlayer = toPlayer();

        if (thisPlayer) {
            float overrideSP = thisPlayer.getActivePlayerData().overrideSpellPowerByAPPercent;

            if (overrideSP > 0.0f) {
                return (int) (MathUtil.CalculatePct(getTotalAttackPowerValue(WeaponAttackType.BASE_ATTACK), overrideSP) + 0.5f);
            }
        }

        var DoneAdvertisedBenefit = getTotalAuraModifierByMiscMask(AuraType.ModDamageDone, schoolMask.getValue());

        if (isTypeId(TypeId.PLAYER)) {
            // Base value
            DoneAdvertisedBenefit += (int) toPlayer().getBaseSpellPowerBonus();

            // Check if we are ever using mana - PaperDollFrame.lua
            if (getPowerIndex(powerType.mana) != (int) powerType.max.getValue()) {
                DoneAdvertisedBenefit += Math.max(0, (int) getStat(stats.Intellect)); // spellpower from intellect
            }

            // Damage bonus from stats
            var mDamageDoneOfStatPercent = getAuraEffectsByType(AuraType.ModSpellDamageOfStatPercent);

            for (var eff : mDamageDoneOfStatPercent) {
                if ((boolean) (eff.getMiscValue() & schoolMask.getValue())) {
                    // stat used stored in miscValueB for this aura
                    var usedStat = stats.forValue(eff.getMiscValueB());
                    DoneAdvertisedBenefit += (int) MathUtil.CalculatePct(getStat(usedStat), eff.getAmount());
                }
            }
        }

        return DoneAdvertisedBenefit;
    }

    public final double spellDamageBonusDone(Unit victim, SpellInfo spellProto, double pdamage, DamageEffectType damagetype, SpellEffectInfo spellEffectInfo, int stack) {
        return spellDamageBonusDone(victim, spellProto, pdamage, damagetype, spellEffectInfo, stack, null);
    }

    public final double spellDamageBonusDone(Unit victim, SpellInfo spellProto, double pdamage, DamageEffectType damagetype, SpellEffectInfo spellEffectInfo) {
        return spellDamageBonusDone(victim, spellProto, pdamage, damagetype, spellEffectInfo, 1, null);
    }

    public final double spellDamageBonusDone(Unit victim, SpellInfo spellProto, double pdamage, DamageEffectType damagetype, SpellEffectInfo spellEffectInfo, int stack, Spell spell) {
        if (spellProto == null || victim == null || damagetype == DamageEffectType.Direct) {
            return pdamage;
        }

        // Some spells don't benefit from done mods
        if (spellProto.hasAttribute(SpellAttr3.IgnoreCasterModifiers)) {
            return pdamage;
        }

        // For totems get damage bonus from owner
        if (isTypeId(TypeId.UNIT) && isTotem()) {
            var owner = getOwnerUnit();

            if (owner != null) {
                return owner.spellDamageBonusDone(victim, spellProto, pdamage, damagetype, spellEffectInfo, stack, spell);
            }
        }

        double DoneTotal = 0;
        var DoneTotalMod = spellDamagePctDone(victim, spellProto, damagetype, spellEffectInfo, spell);

        // Done fixed damage bonus auras
        var DoneAdvertisedBenefit = spellBaseDamageBonusDone(spellProto.getSchoolMask());
        // modify spell power by victim's SPELL_AURA_MOD_DAMAGE_TAKEN auras (eg Amplify/Dampen Magic)
        DoneAdvertisedBenefit += victim.getTotalAuraModifierByMiscMask(AuraType.ModDamageTaken, spellProto.getSchoolMask().getValue());

        // Pets just add their bonus damage to their spell damage
        // note that their spell damage is just gain of their own auras
        if (hasUnitTypeMask(UnitTypeMask.Guardian)) {
            DoneAdvertisedBenefit += ((Guardian) this).GetBonusDamage();
        }

        // Check for table values
        if (spellEffectInfo.bonusCoefficientFromAp > 0.0f) {
            var ApCoeffMod = spellEffectInfo.bonusCoefficientFromAp;
            var modOwner = getSpellModOwner();

            if (modOwner) {
                ApCoeffMod *= 100.0f;
                tangible.RefObject<Double> tempRef_ApCoeffMod = new tangible.RefObject<Double>(ApCoeffMod);
                modOwner.applySpellMod(spellProto, SpellModOp.bonusCoefficient, tempRef_ApCoeffMod);
                ApCoeffMod = tempRef_ApCoeffMod.refArgValue;
                ApCoeffMod /= 100.0f;
            }

            var attType = WeaponAttackType.BASE_ATTACK;

            if ((spellProto.isRangedWeaponSpell() && spellProto.getDmgClass() != SpellDmgClass.Melee)) {
                attType = WeaponAttackType.RangedAttack;
            }

            if (spellProto.hasAttribute(SpellAttr3.RequiresOffHandWeapon) && !spellProto.hasAttribute(SpellAttr3.RequiresMainHandWeapon)) {
                attType = WeaponAttackType.OFF_ATTACK;
            }

            var APbonus = victim.getTotalAuraModifier(attType != WeaponAttackType.RangedAttack ? AuraType.MeleeAttackPowerAttackerBonus : AuraType.RangedAttackPowerAttackerBonus);
            APbonus += getTotalAttackPowerValue(attType);
            DoneTotal += (int) (stack * ApCoeffMod * APbonus);
        } else {
            // No bonus damage for SPELL_DAMAGE_CLASS_NONE class spells by default
            if (spellProto.getDmgClass() == SpellDmgClass.NONE) {
                return Math.max(pdamage * DoneTotalMod, 0.0f);
            }
        }

        // Default calculation
        var coeff = spellEffectInfo.bonusCoefficient;

        if (DoneAdvertisedBenefit != 0) {
            if (spell != null) {
                spell.<ISpellCalculateBonusCoefficient>ForEachSpellScript(a -> coeff = a.CalcBonusCoefficient(coeff));
            }

            var modOwner1 = getSpellModOwner();

            if (modOwner1) {
                coeff *= 100.0f;
                tangible.RefObject<Double> tempRef_coeff = new tangible.RefObject<Double>(coeff);
                modOwner1.applySpellMod(spellProto, SpellModOp.bonusCoefficient, tempRef_coeff);
                coeff = tempRef_coeff.refArgValue;
                coeff /= 100.0f;
            }

            DoneTotal += (DoneAdvertisedBenefit * coeff * stack);
        }

        var tmpDamage = (pdamage + DoneTotal) * DoneTotalMod;
        // apply spellmod to Done damage (flat and pct)
        var _modOwner = getSpellModOwner();

        if (_modOwner != null) {
            tangible.RefObject<Double> tempRef_tmpDamage = new tangible.RefObject<Double>(tmpDamage);
            _modOwner.applySpellMod(spellProto, damagetype == DamageEffectType.DOT ? SpellModOp.PeriodicHealingAndDamage : SpellModOp.HealingAndDamage, tempRef_tmpDamage);
            tmpDamage = tempRef_tmpDamage.refArgValue;
        }

        return Math.max(tmpDamage, 0.0f);
    }

    public final double spellDamagePctDone(Unit victim, SpellInfo spellProto, DamageEffectType damagetype, SpellEffectInfo spellEffectInfo) {
        return spellDamagePctDone(victim, spellProto, damagetype, spellEffectInfo, null);
    }

    public final double spellDamagePctDone(Unit victim, SpellInfo spellProto, DamageEffectType damagetype, SpellEffectInfo spellEffectInfo, Spell spell) {
        if (spellProto == null || !victim || damagetype == DamageEffectType.Direct) {
            return 1.0f;
        }

        // Some spells don't benefit from done mods
        if (spellProto.hasAttribute(SpellAttr3.IgnoreCasterModifiers)) {
            return 1.0f;
        }

        // Some spells don't benefit from pct done mods
        if (spellProto.hasAttribute(SpellAttr6.IgnoreCasterDamageModifiers)) {
            return 1.0f;
        }

        // For totems get damage bonus from owner
        if (isCreature() && isTotem()) {
            var owner = getOwnerUnit();

            if (owner != null) {
                return owner.spellDamagePctDone(victim, spellProto, damagetype, spellEffectInfo, spell);
            }
        }

        // Done total percent damage auras
        double DoneTotalMod = 1.0f;

        // Pet damage?
        if (isTypeId(TypeId.UNIT) && !isPet()) {
            DoneTotalMod *= toCreature().getSpellDamageMod(toCreature().getCreatureTemplate().rank);
        }

        // Versatility
        var modOwner = getSpellModOwner();

        if (modOwner) {
            tangible.RefObject<Double> tempRef_DoneTotalMod = new tangible.RefObject<Double>(DoneTotalMod);
            MathUtil.AddPct(tempRef_DoneTotalMod, modOwner.getRatingBonusValue(CombatRating.VersatilityDamageDone) + modOwner.getTotalAuraModifier(AuraType.ModVersatility));
            DoneTotalMod = tempRef_DoneTotalMod.refArgValue;
        }

        double maxModDamagePercentSchool = 0.0f;
        var thisPlayer = toPlayer();

        if (thisPlayer) {
            for (var i = 0; i < SpellSchool.max.getValue(); ++i) {
                if ((boolean) (spellProto.getSchoolMask().getValue() & (1 << i))) {
                    maxModDamagePercentSchool = Math.max(maxModDamagePercentSchool, thisPlayer.getActivePlayerData().modDamageDonePercent.get(i));
                }
            }
        } else {
            maxModDamagePercentSchool = getTotalAuraMultiplierByMiscMask(AuraType.ModDamagePercentDone, (int) spellProto.getSchoolMask().getValue());
        }

        DoneTotalMod *= maxModDamagePercentSchool;

        var creatureTypeMask = victim.getCreatureTypeMask();

        DoneTotalMod *= getTotalAuraMultiplierByMiscMask(AuraType.ModDamageDoneVersus, creatureTypeMask);

        // bonus against aurastate
        DoneTotalMod *= getTotalAuraMultiplier(AuraType.ModDamageDoneVersusAurastate, aurEff ->
        {
            if (victim.hasAuraState(AuraStateType.forValue(aurEff.miscValue))) {
                return true;
            }

            return false;
        });

        // Add SPELL_AURA_MOD_DAMAGE_DONE_FOR_MECHANIC percent bonus
        if (spellEffectInfo.mechanic != 0) {
            tangible.RefObject<Double> tempRef_DoneTotalMod2 = new tangible.RefObject<Double>(DoneTotalMod);
            MathUtil.AddPct(tempRef_DoneTotalMod2, getTotalAuraModifierByMiscValue(AuraType.ModDamageDoneForMechanic, spellEffectInfo.mechanic.getValue()));
            DoneTotalMod = tempRef_DoneTotalMod2.refArgValue;
        } else if (spellProto.getMechanic() != 0) {
            tangible.RefObject<Double> tempRef_DoneTotalMod3 = new tangible.RefObject<Double>(DoneTotalMod);
            MathUtil.AddPct(tempRef_DoneTotalMod3, getTotalAuraModifierByMiscValue(AuraType.ModDamageDoneForMechanic, spellProto.getMechanic().getValue()));
            DoneTotalMod = tempRef_DoneTotalMod3.refArgValue;
        }

        if (spell != null) {
            spell.<ISpellCalculateMultiplier>ForEachSpellScript(a -> DoneTotalMod = a.CalcMultiplier(DoneTotalMod));
        }

        // Custom scripted damage. Need to figure out how to move this.
        if (spellProto.getSpellFamilyName() == SpellFamilyName.Warlock) {
            // Shadow Bite (30% increase from each dot)
            if (spellProto.getSpellFamilyFlags().get(1).<Integer>HasAnyFlag(0x00400000) && isPet()) {
                var count = victim.getDoTsByCaster(getOwnerGUID());

                if (count != 0) {
                    tangible.RefObject<Double> tempRef_DoneTotalMod4 = new tangible.RefObject<Double>(DoneTotalMod);
                    MathUtil.AddPct(tempRef_DoneTotalMod4, 30 * count);
                    DoneTotalMod = tempRef_DoneTotalMod4.refArgValue;
                }
            }
        }

        return DoneTotalMod;
    }

    public final double spellDamageBonusTaken(Unit caster, SpellInfo spellProto, double pdamage, DamageEffectType damagetype) {
        if (spellProto == null || damagetype == DamageEffectType.Direct) {
            return pdamage;
        }

        double TakenTotalMod = 1.0f;

        // Mod damage from spell mechanic
        var mechanicMask = spellProto.getAllEffectsMechanicMask();

        if (mechanicMask != 0) {
            TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModMechanicDamageTakenPercent, aurEff ->
            {
                if ((mechanicMask & (1 << aurEff.miscValue)) != 0) {
                    return true;
                }

                return false;
            });
        }

        var cheatDeath = getAuraEffect(45182, 0);

        if (cheatDeath != null) {
            if (cheatDeath.miscValue.hasFlag(spellSchoolMask.NORMAL.getValue())) {
                tangible.RefObject<Double> tempRef_TakenTotalMod = new tangible.RefObject<Double>(TakenTotalMod);
                MathUtil.AddPct(tempRef_TakenTotalMod, cheatDeath.amount);
                TakenTotalMod = tempRef_TakenTotalMod.refArgValue;
            }
        }

        // Spells with SPELL_ATTR4_IGNORE_DAMAGE_TAKEN_MODIFIERS should only benefit from mechanic damage mod auras.
        if (!spellProto.hasAttribute(SpellAttr4.IgnoreDamageTakenModifiers)) {
            // Versatility
            var modOwner = getSpellModOwner();

            if (modOwner) {
                // only 50% of SPELL_AURA_MOD_VERSATILITY for damage reduction
                var versaBonus = modOwner.getTotalAuraModifier(AuraType.ModVersatility) / 2.0f;
                tangible.RefObject<Double> tempRef_TakenTotalMod2 = new tangible.RefObject<Double>(TakenTotalMod);
                MathUtil.AddPct(tempRef_TakenTotalMod2, -(modOwner.getRatingBonusValue(CombatRating.VersatilityDamageTaken) + versaBonus));
                TakenTotalMod = tempRef_TakenTotalMod2.refArgValue;
            }

            // from positive and negative SPELL_AURA_MOD_DAMAGE_PERCENT_TAKEN
            // multiplicative bonus, for example dispersion + shadowform (0.10*0.85=0.085)
            TakenTotalMod *= getTotalAuraMultiplierByMiscMask(AuraType.ModDamagePercentTaken, (int) spellProto.getSchoolMask().getValue());

            // From caster spells
            if (caster != null) {
                TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModSchoolMaskDamageFromCaster, aurEff ->
                {
                    return Objects.equals(aurEff.casterGuid, caster.getGUID()) && (aurEff.miscValue & spellProto.getSchoolMask().getValue()) != 0;
                });

                TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModSpellDamageFromCaster, aurEff ->
                {
                    return Objects.equals(aurEff.casterGuid, caster.getGUID()) && aurEff.isAffectingSpell(spellProto);
                });

                TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModDamageTakenFromCasterByLabel, aurEff ->
                {
                    return Objects.equals(aurEff.casterGuid, caster.getGUID()) && spellProto.hasLabel((int) aurEff.miscValue);
                });
            }

            if (damagetype == DamageEffectType.DOT) {
                TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModPeriodicDamageTaken, aurEff -> (aurEff.miscValue & (int) spellProto.getSchoolMask().getValue()) != 0);
            }
        }

        // Sanctified wrath (bypass damage reduction)
        if (caster != null && TakenTotalMod < 1.0f) {
            var damageReduction = 1.0f - TakenTotalMod;
            var casterIgnoreResist = caster.getAuraEffectsByType(AuraType.ModIgnoreTargetResist);

            for (var aurEff : casterIgnoreResist) {
                if ((aurEff.getMiscValue() & spellProto.getSchoolMask().getValue()) == 0) {
                    continue;
                }

                tangible.RefObject<Double> tempRef_damageReduction = new tangible.RefObject<Double>(damageReduction);
                MathUtil.AddPct(tempRef_damageReduction, -aurEff.getAmount());
                damageReduction = tempRef_damageReduction.refArgValue;
            }

            TakenTotalMod = 1.0f - damageReduction;
        }

        var tmpDamage = pdamage * TakenTotalMod;

        return Math.max(tmpDamage, 0.0f);
    }

    public final double spellBaseHealingBonusDone(SpellSchoolMask schoolMask) {
        var thisPlayer = toPlayer();

        if (thisPlayer != null) {
            float overrideSP = thisPlayer.getActivePlayerData().overrideSpellPowerByAPPercent;

            if (overrideSP > 0.0f) {
                return (MathUtil.CalculatePct(getTotalAttackPowerValue(WeaponAttackType.BASE_ATTACK), overrideSP) + 0.5f);
            }
        }

        var advertisedBenefit = getTotalAuraModifier(AuraType.ModHealingDone, aurEff ->
        {
            if (aurEff.miscValue == 0 || (aurEff.miscValue & schoolMask.getValue()) != 0) {
                return true;
            }

            return false;
        });

        // Healing bonus of spirit, intellect and strength
        if (isTypeId(TypeId.PLAYER)) {
            // Base value
            advertisedBenefit += toPlayer().getBaseSpellPowerBonus();

            // Check if we are ever using mana - PaperDollFrame.lua
            if (getPowerIndex(powerType.mana) != (int) powerType.max.getValue()) {
                advertisedBenefit += Math.max(0, getStat(stats.Intellect)); // spellpower from intellect
            }

            // Healing bonus from stats
            var mHealingDoneOfStatPercent = getAuraEffectsByType(AuraType.ModSpellHealingOfStatPercent);

            for (var i : mHealingDoneOfStatPercent) {
                // stat used dependent from misc value (stat index)
                var usedStat = stats.forValue(i.getSpellEffectInfo().miscValue);
                advertisedBenefit += MathUtil.CalculatePct(getStat(usedStat), i.getAmount());
            }
        }

        return advertisedBenefit;
    }

    public final double spellHealingBonusDone(Unit victim, SpellInfo spellProto, double healamount, DamageEffectType damagetype, SpellEffectInfo spellEffectInfo, int stack) {
        return spellHealingBonusDone(victim, spellProto, healamount, damagetype, spellEffectInfo, stack, null);
    }

    public final double spellHealingBonusDone(Unit victim, SpellInfo spellProto, double healamount, DamageEffectType damagetype, SpellEffectInfo spellEffectInfo) {
        return spellHealingBonusDone(victim, spellProto, healamount, damagetype, spellEffectInfo, 1, null);
    }

    public final double spellHealingBonusDone(Unit victim, SpellInfo spellProto, double healamount, DamageEffectType damagetype, SpellEffectInfo spellEffectInfo, int stack, Spell spell) {
        // For totems get healing bonus from owner (statue isn't totem in fact)
        if (isTypeId(TypeId.UNIT) && isTotem()) {
            var owner = getOwnerUnit();

            if (owner) {
                return owner.spellHealingBonusDone(victim, spellProto, healamount, damagetype, spellEffectInfo, stack, spell);
            }
        }

        // No bonus healing for potion spells
        if (spellProto.getSpellFamilyName() == SpellFamilyName.Potion) {
            return healamount;
        }

        double DoneTotal = 0;
        var DoneTotalMod = spellHealingPctDone(victim, spellProto, spell);

        // done scripted mod (take it from owner)
        var owner1 = getOwnerUnit() != null ? getOwnerUnit() : this;
        var mOverrideClassScript = owner1.getAuraEffectsByType(AuraType.OverrideClassScripts);

        for (var aurEff : mOverrideClassScript) {
            if (!aurEff.isAffectingSpell(spellProto)) {
                continue;
            }

            switch (aurEff.getMiscValue()) {
                case 3736: // Hateful Totem of the Third Wind / Increased Lesser Healing Wave / LK Arena (4/5/6) Totem of the Third Wind / Savage Totem of the Third Wind
                    DoneTotal += aurEff.getAmount();

                    break;
                default:
                    break;
            }
        }

        // Done fixed damage bonus auras
        var DoneAdvertisedBenefit = spellBaseHealingBonusDone(spellProto.getSchoolMask());
        // modify spell power by victim's SPELL_AURA_MOD_HEALING auras (eg Amplify/Dampen Magic)
        DoneAdvertisedBenefit += victim.getTotalAuraModifierByMiscMask(AuraType.ModHealing, spellProto.getSchoolMask().getValue());

        // Pets just add their bonus damage to their spell damage
        // note that their spell damage is just gain of their own auras
        if (hasUnitTypeMask(UnitTypeMask.Guardian)) {
            DoneAdvertisedBenefit += ((Guardian) this).GetBonusDamage();
        }

        // Check for table values
        var coeff = spellEffectInfo.bonusCoefficient;

        if (spellEffectInfo.bonusCoefficientFromAp > 0.0f) {
            var attType = (spellProto.isRangedWeaponSpell() && spellProto.getDmgClass() != SpellDmgClass.Melee) ? WeaponAttackType.RangedAttack : WeaponAttackType.BASE_ATTACK;
            var APbonus = victim.getTotalAuraModifier(attType == WeaponAttackType.BASE_ATTACK ? AuraType.MeleeAttackPowerAttackerBonus : AuraType.RangedAttackPowerAttackerBonus);
            APbonus += getTotalAttackPowerValue(attType);

            DoneTotal += (spellEffectInfo.BonusCoefficientFromAp * stack * APbonus);
        } else if (coeff <= 0.0f) // no AP and no SP coefs, skip
        {
            // No bonus healing for SPELL_DAMAGE_CLASS_NONE class spells by default
            if (spellProto.getDmgClass() == SpellDmgClass.NONE) {
                return Math.max(healamount * DoneTotalMod, 0.0f);
            }
        }

        // Default calculation
        if (DoneAdvertisedBenefit != 0) {
            if (spell != null) {
                spell.<ISpellCalculateBonusCoefficient>ForEachSpellScript(a -> coeff = a.CalcBonusCoefficient(coeff));
            }

            var modOwner = getSpellModOwner();

            if (modOwner) {
                coeff *= 100.0f;
                tangible.RefObject<Double> tempRef_coeff = new tangible.RefObject<Double>(coeff);
                modOwner.applySpellMod(spellProto, SpellModOp.bonusCoefficient, tempRef_coeff);
                coeff = tempRef_coeff.refArgValue;
                coeff /= 100.0f;
            }

            DoneTotal += (int) (DoneAdvertisedBenefit * coeff * stack);
        }

        for (var otherSpellEffectInfo : spellProto.getEffects()) {
            switch (otherSpellEffectInfo.applyAuraName) {
                // Bonus healing does not apply to these spells
                case PeriodicLeech:
                case PeriodicHealthFunnel:
                    DoneTotal = 0;

                    break;
            }

            if (otherSpellEffectInfo.isEffect(SpellEffectName.HealthLeech)) {
                DoneTotal = 0;
            }
        }

        var heal = (healamount + DoneTotal) * DoneTotalMod;

        // apply spellmod to Done amount
        var _modOwner = getSpellModOwner();

        if (_modOwner) {
            tangible.RefObject<Double> tempRef_heal = new tangible.RefObject<Double>(heal);
            _modOwner.applySpellMod(spellProto, damagetype == DamageEffectType.DOT ? SpellModOp.PeriodicHealingAndDamage : SpellModOp.HealingAndDamage, tempRef_heal);
            heal = tempRef_heal.refArgValue;
        }

        return Math.max(heal, 0.0f);
    }

    public final double spellHealingPctDone(Unit victim, SpellInfo spellProto) {
        return spellHealingPctDone(victim, spellProto, null);
    }

    public final double spellHealingPctDone(Unit victim, SpellInfo spellProto, Spell spell) {
        // For totems get healing bonus from owner
        if (isCreature() && isTotem()) {
            var owner = getOwnerUnit();

            if (owner != null) {
                return owner.spellHealingPctDone(victim, spellProto);
            }
        }

        // Some spells don't benefit from done mods
        if (spellProto.hasAttribute(SpellAttr3.IgnoreCasterModifiers)) {
            return 1.0f;
        }

        // Some spells don't benefit from done mods
        if (spellProto.hasAttribute(SpellAttr6.IgnoreHealingModifiers)) {
            return 1.0f;
        }

        // No bonus healing for potion spells
        if (spellProto.getSpellFamilyName() == SpellFamilyName.Potion) {
            return 1.0f;
        }

        var thisPlayer = toPlayer();

        if (thisPlayer != null) {
            double maxModDamagePercentSchool = 0.0f;

            for (var i = 0; i < SpellSchool.max.getValue(); ++i) {
                if ((spellProto.getSchoolMask().getValue() & (1 << i)) != 0) {
                    maxModDamagePercentSchool = Math.max(maxModDamagePercentSchool, thisPlayer.getActivePlayerData().modHealingDonePercent.get(i));
                }
            }

            return maxModDamagePercentSchool;
        }

        double DoneTotalMod = 1.0f;

        // bonus against aurastate
        DoneTotalMod *= getTotalAuraMultiplier(AuraType.ModDamageDoneVersusAurastate, aurEff ->
        {
            return victim.hasAuraState(AuraStateType.forValue(aurEff.miscValue));
        });

        // Healing done percent
        DoneTotalMod *= getTotalAuraMultiplier(AuraType.modHealingDonePercent);

        // bonus from missing health of target
        var healthPctDiff = 100.0f - victim.getHealthPct();

        for (var healingDonePctVsTargetHealth : getAuraEffectsByType(AuraType.ModHealingDonePctVersusTargetHealth)) {
            if (healingDonePctVsTargetHealth.isAffectingSpell(spellProto)) {
                tangible.RefObject<Double> tempRef_DoneTotalMod = new tangible.RefObject<Double>(DoneTotalMod);
                MathUtil.AddPct(tempRef_DoneTotalMod, MathUtil.CalculatePct((float) healingDonePctVsTargetHealth.getAmount(), healthPctDiff));
                DoneTotalMod = tempRef_DoneTotalMod.refArgValue;
            }
        }

        if (spell != null) {
            spell.<ISpellCalculateMultiplier>ForEachSpellScript(a -> DoneTotalMod = a.CalcMultiplier(DoneTotalMod));
        }

        return DoneTotalMod;
    }

    public final double spellHealingBonusTaken(Unit caster, SpellInfo spellProto, double healamount, DamageEffectType damagetype) {
        double TakenTotalMod = 1.0f;

        // Healing taken percent
        var minval = getMaxNegativeAuraModifier(AuraType.ModHealingPct);

        if (minval != 0) {
            tangible.RefObject<Double> tempRef_TakenTotalMod = new tangible.RefObject<Double>(TakenTotalMod);
            MathUtil.AddPct(tempRef_TakenTotalMod, minval);
            TakenTotalMod = tempRef_TakenTotalMod.refArgValue;
        }

        var maxval = getMaxPositiveAuraModifier(AuraType.ModHealingPct);

        if (maxval != 0) {
            tangible.RefObject<Double> tempRef_TakenTotalMod2 = new tangible.RefObject<Double>(TakenTotalMod);
            MathUtil.AddPct(tempRef_TakenTotalMod2, maxval);
            TakenTotalMod = tempRef_TakenTotalMod2.refArgValue;
        }

        // Nourish cast
        if (spellProto.getSpellFamilyName() == SpellFamilyName.Druid && spellProto.getSpellFamilyFlags().get(1).hasFlag(0x2000000)) {
            // rejuvenation, regrowth, lifebloom, or Wild Growth
            if (getAuraEffect(AuraType.PeriodicHeal, SpellFamilyName.Druid, new Flag128(0x50, 0x4000010, 0)) != null) {
                // increase healing by 20%
                TakenTotalMod *= 1.2f;
            }
        }

        if (damagetype == DamageEffectType.DOT) {
            // Healing over time taken percent
            var minval_hot = getMaxNegativeAuraModifier(AuraType.ModHotPct);

            if (minval_hot != 0) {
                tangible.RefObject<Double> tempRef_TakenTotalMod3 = new tangible.RefObject<Double>(TakenTotalMod);
                MathUtil.AddPct(tempRef_TakenTotalMod3, minval_hot);
                TakenTotalMod = tempRef_TakenTotalMod3.refArgValue;
            }

            var maxval_hot = getMaxPositiveAuraModifier(AuraType.ModHotPct);

            if (maxval_hot != 0) {
                tangible.RefObject<Double> tempRef_TakenTotalMod4 = new tangible.RefObject<Double>(TakenTotalMod);
                MathUtil.AddPct(tempRef_TakenTotalMod4, maxval_hot);
                TakenTotalMod = tempRef_TakenTotalMod4.refArgValue;
            }
        }

        if (caster) {
            TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModHealingReceived, aurEff ->
            {
                if (Objects.equals(caster.getGUID(), aurEff.casterGuid) && aurEff.isAffectingSpell(spellProto)) {
                    return true;
                }

                return false;
            });

            TakenTotalMod *= getTotalAuraMultiplier(AuraType.ModHealingTakenFromCaster, aurEff ->
            {
                return Objects.equals(aurEff.casterGuid, caster.getGUID());
            });
        }

        var heal = healamount * TakenTotalMod;

        return Math.max(heal, 0.0f);
    }

    public final double spellCritChanceDone(Spell spell, AuraEffect aurEff, SpellSchoolMask schoolMask) {
        return spellCritChanceDone(spell, aurEff, schoolMask, WeaponAttackType.BASE_ATTACK);
    }

    public final double spellCritChanceDone(Spell spell, AuraEffect aurEff, SpellSchoolMask schoolMask, WeaponAttackType attackType) {
        var spellInfo = spell != null ? spell.SpellInfo : aurEff.getSpellInfo();

        //! Mobs can't crit with spells. (Except player controlled)
        if (isCreature() && !getSpellModOwner()) {
            return 0.0f;
        }

        // not critting spell
        if (spell != null && !spellInfo.hasAttribute(SpellCustomAttributes.CanCrit)) {
            return 0.0f;
        }

        double crit_chance = 0.0f;

        switch (spellInfo.getDmgClass()) {
            case Magic: {
                if (schoolMask.hasFlag(spellSchoolMask.NORMAL)) {
                    crit_chance = 0.0f;
                }
                // For other schools
                else if (isTypeId(TypeId.PLAYER)) {
                    crit_chance = toPlayer().getActivePlayerData().spellCritPercentage;
                } else {
                    crit_chance = getBaseSpellCritChance();
                }

                break;
            }
            case Melee:
            case Ranged:
                crit_chance += getUnitCriticalChanceDone(attackType);

                break;

            case None:
            default:
                return 0f;
        }

        // percent done
        // only players use intelligence for critical chance computations
        var modOwner = getSpellModOwner();

        if (modOwner != null) {
            tangible.RefObject<Double> tempRef_crit_chance = new tangible.RefObject<Double>(crit_chance);
            modOwner.applySpellMod(spellInfo, SpellModOp.CritChance, tempRef_crit_chance);
            crit_chance = tempRef_crit_chance.refArgValue;
        }

        return Math.max(crit_chance, 0.0f);
    }

    public final double spellCritChanceTaken(Unit caster, Spell spell, AuraEffect aurEff, SpellSchoolMask schoolMask, double doneChance) {
        return spellCritChanceTaken(caster, spell, aurEff, schoolMask, doneChance, WeaponAttackType.BASE_ATTACK);
    }

    public final double spellCritChanceTaken(Unit caster, Spell spell, AuraEffect aurEff, SpellSchoolMask schoolMask, double doneChance, WeaponAttackType attackType) {
        var spellInfo = spell != null ? spell.SpellInfo : aurEff.getSpellInfo();

        // not critting spell
        if (spell != null && !spellInfo.hasAttribute(SpellCustomAttributes.CanCrit)) {
            return 0.0f;
        }

        var crit_chance = doneChance;

        switch (spellInfo.getDmgClass()) {
            case Magic: {
                // taken
                if (!spellInfo.isPositive()) {
                    // Modify critical chance by victim SPELL_AURA_MOD_ATTACKER_SPELL_AND_WEAPON_CRIT_CHANCE
                    crit_chance += getTotalAuraModifier(AuraType.ModAttackerSpellAndWeaponCritChance);
                }

                if (caster) {
                    // scripted (increase crit chance ... against ... target by x%
                    var mOverrideClassScript = caster.getAuraEffectsByType(AuraType.OverrideClassScripts);

                    for (var eff : mOverrideClassScript) {
                        if (!eff.isAffectingSpell(spellInfo)) {
                            continue;
                        }

                        switch (eff.getMiscValue()) {
                            case 911: // Shatter
                                if (hasAuraState(AuraStateType.Frozen, spellInfo, this)) {
                                    crit_chance *= 1.5f;
                                    var _eff = eff.getBase().getEffect(1);

                                    if (_eff != null) {
                                        crit_chance += _eff.getAmount();
                                    }
                                }

                                break;
                            default:
                                break;
                        }
                    }

                    // Custom crit by class
                    switch (spellInfo.getSpellFamilyName()) {
                        case Rogue:
                            // Shiv-applied poisons can't crit
                            if (caster.findCurrentSpellBySpellId(5938) != null) {
                                crit_chance = 0.0f;
                            }

                            break;
                    }

                    // Spell crit suppression
                    if (isCreature()) {
                        var levelDiff = (int) (getLevelForTarget(this) - caster.getLevel());
                        crit_chance -= levelDiff * 1.0f;
                    }
                }

                break;
            }
            case Melee:
            case Ranged: {
                if (caster != null) {
                    crit_chance += getUnitCriticalChanceTaken(caster, attackType, crit_chance);
                }

                break;
            }
            case None:
            default:
                return 0f;
        }

        // for this types the bonus was already added in GetUnitCriticalChance, do not add twice
        if (caster != null && spellInfo.getDmgClass() != SpellDmgClass.Melee && spellInfo.getDmgClass() != SpellDmgClass.Ranged) {
            crit_chance += getTotalAuraModifier(AuraType.ModCritChanceForCasterWithAbilities, aurEff -> Objects.equals(aurEff.getCasterGuid(), caster.getGUID()) && aurEff.isAffectingSpell(spellInfo));

            crit_chance += getTotalAuraModifier(AuraType.ModCritChanceForCaster, aurEff -> Objects.equals(aurEff.getCasterGuid(), caster.getGUID()));

            crit_chance += caster.getTotalAuraModifier(AuraType.ModCritChanceVersusTargetHealth, aurEff -> !healthBelowPct(aurEff.getMiscValueB()));

            var tempSummon = caster.toTempSummon();

            if (tempSummon != null) {
                crit_chance += getTotalAuraModifier(AuraType.ModCritChanceForCasterPet, aurEff -> Objects.equals(aurEff.getCasterGuid(), tempSummon.getSummonerGUID()));
            }
        }

        // call script handlers
        if (spell) {
            tangible.RefObject<Double> tempRef_crit_chance = new tangible.RefObject<Double>(crit_chance);
            spell.callScriptCalcCritChanceHandlers(this, tempRef_crit_chance);
            crit_chance = tempRef_crit_chance.refArgValue;
        } else {
            tangible.RefObject<Double> tempRef_crit_chance2 = new tangible.RefObject<Double>(crit_chance);
            aurEff.getBase().callScriptEffectCalcCritChanceHandlers(aurEff, aurEff.getBase().getApplicationOfTarget(getGUID()), this, tempRef_crit_chance2);
            crit_chance = tempRef_crit_chance2.refArgValue;
        }

        return Math.max(crit_chance, 0.0f);
    }

    // Melee based spells hit result calculations
    @Override
    public SpellMissInfo meleeSpellHitResult(Unit victim, SpellInfo spellInfo) {
        if (spellInfo.hasAttribute(SpellAttr3.NoAvoidance)) {
            return SpellMissInfo.NONE;
        }

        var attType = WeaponAttackType.BASE_ATTACK;

        // Check damage class instead of attack type to correctly handle judgements
        // - they are meele, but can't be dodged/parried/deflected because of ranged dmg class
        if (spellInfo.getDmgClass() == SpellDmgClass.Ranged) {
            attType = WeaponAttackType.RangedAttack;
        }

        var roll = RandomUtil.randomInt(0, 10000);

        var missChance = meleeSpellMissChance(victim, attType, spellInfo) * 100.0f;
        // Roll miss
        var tmp = missChance;

        if (roll < tmp) {
            return SpellMissInfo.Miss;
        }

        // Chance resist mechanic
        var resist_chance = victim.getMechanicResistChance(spellInfo) * 100;
        tmp += resist_chance;

        if (roll < tmp) {
            return SpellMissInfo.resist;
        }

        // Same spells cannot be parried/dodged
        if (spellInfo.hasAttribute(SpellAttr0.NoActiveDefense)) {
            return SpellMissInfo.NONE;
        }

        var canDodge = !spellInfo.hasAttribute(SpellAttr7.NoAttackDodge);
        var canParry = !spellInfo.hasAttribute(SpellAttr7.NoAttackParry);
        var canBlock = true;

        // if victim is casting or cc'd it can't avoid attacks
        if (victim.isNonMeleeSpellCast(false, false, true) || victim.hasUnitState(UnitState.controlled)) {
            canDodge = false;
            canParry = false;
            canBlock = false;
        }

        // Ranged attacks can only miss, resist and deflect and get blocked
        if (attType == WeaponAttackType.RangedAttack) {
            canParry = false;
            canDodge = false;

            // only if in front
            if (!victim.hasUnitState(UnitState.controlled) && (victim.getLocation().hasInArc(MathUtil.PI, getLocation()) || victim.hasAuraType(AuraType.IgnoreHitDirection))) {
                var deflect_chance = victim.getTotalAuraModifier(AuraType.DeflectSpells) * 100;
                tmp += deflect_chance;

                if (roll < tmp) {
                    return SpellMissInfo.Deflect;
                }
            }
        }

        // Check for attack from behind
        if (!victim.getLocation().hasInArc(MathUtil.PI, getLocation())) {
            if (!victim.hasAuraType(AuraType.IgnoreHitDirection)) {
                // Can`t dodge from behind in pvP (but its possible in PvE)
                if (victim.isTypeId(TypeId.PLAYER)) {
                    canDodge = false;
                }

                // Can`t parry or block
                canParry = false;
                canBlock = false;
            } else // Only deterrence as of 3.3.5
            {
                if (spellInfo.hasAttribute(SpellCustomAttributes.ReqCasterBehindTarget)) {
                    canParry = false;
                }
            }
        }

        // Ignore combat result aura
        var ignore = getAuraEffectsByType(AuraType.IgnoreCombatResult);

        for (var aurEff : ignore) {
            if (!aurEff.isAffectingSpell(spellInfo)) {
                continue;
            }

            switch (MeleeHitOutcome.forValue(aurEff.getMiscValue())) {
                case Dodge:
                    canDodge = false;

                    break;
                case Block:
                    canBlock = false;

                    break;
                case Parry:
                    canParry = false;

                    break;
                default:
                    Log.outDebug(LogFilter.unit, "Spell {0} SPELL_AURA_IGNORE_COMBAT_RESULT has unhandled state {1}", aurEff.getId(), aurEff.getMiscValue());

                    break;
            }
        }

        if (canDodge) {
            // Roll dodge
            var dodgeChance = (int) (getUnitDodgeChance(attType, victim) * 100.0f);

            if (dodgeChance < 0) {
                dodgeChance = 0;
            }

            if (roll < (tmp += dodgeChance)) {
                return SpellMissInfo.Dodge;
            }
        }

        if (canParry) {
            // Roll parry
            var parryChance = (int) (getUnitParryChance(attType, victim) * 100.0f);

            if (parryChance < 0) {
                parryChance = 0;
            }

            tmp += parryChance;

            if (roll < tmp) {
                return SpellMissInfo.Parry;
            }
        }

        if (canBlock) {
            var blockChance = (int) (getUnitBlockChance(attType, victim) * 100.0f);

            if (blockChance < 0) {
                blockChance = 0;
            }

            tmp += blockChance;

            if (roll < tmp) {
                return SpellMissInfo.Block;
            }
        }

        return SpellMissInfo.NONE;
    }

    public final void finishSpell(CurrentSpellType spellType) {
        finishSpell(spellType, SpellCastResult.SpellCastOk);
    }

    public final void finishSpell(CurrentSpellType spellType, SpellCastResult result) {
        var spell = getCurrentSpell(spellType);

        if (spell == null) {
            return;
        }

        if (spellType == CurrentSpellType.Channeled) {
            spell.sendChannelUpdate(0);
        }

        spell.finish(result);
    }

    public SpellInfo getCastSpellInfo(SpellInfo spellInfo) {

//		SpellInfo findMatchingAuraEffectIn(AuraType type)
//			{
//				foreach (var auraEffect in getAuraEffectsByType(type))
//				{
//					var matches = auraEffect.miscValue != 0 ? auraEffect.miscValue == spellInfo.Id : auraEffect.isAffectingSpell(spellInfo);
//
//					if (matches)
//					{
//						var info = global.SpellMgr.getSpellInfo((uint)auraEffect.amount, Map.difficultyID);
//
//						if (info != null)
//							return info;
//					}
//				}
//
//				return null;
//			}

        var newInfo = findMatchingAuraEffectIn(AuraType.OverrideActionbarSpells);

        if (newInfo != null) {
            return newInfo;
        }

        newInfo = findMatchingAuraEffectIn(AuraType.OverrideActionbarSpellsTriggered);

        if (newInfo != null) {
            return newInfo;
        }

        return spellInfo;
    }

    @Override
    public int getCastSpellXSpellVisualId(SpellInfo spellInfo) {
        var visualOverrides = getAuraEffectsByType(AuraType.OverrideSpellVisual);

        for (var effect : visualOverrides) {
            if (effect.getMiscValue() == spellInfo.getId()) {
                var visualSpell = global.getSpellMgr().getSpellInfo((int) effect.getMiscValueB(), getMap().getDifficultyID());

                if (visualSpell != null) {
                    spellInfo = visualSpell;

                    break;
                }
            }
        }

        return super.getCastSpellXSpellVisualId(spellInfo);
    }

    public final void setAuraStack(int spellId, Unit target, int stack) {
        var aura = target.getAura(spellId, getGUID());

        if (aura == null) {
            aura = addAura(spellId, target);
        }

        if (aura != null && stack != 0) {
            aura.setStackAmount((byte) stack);
        }
    }

    public final Spell findCurrentSpellBySpellId(int spell_id) {
        for (var spell : getCurrentSpells().values()) {
            if (spell == null) {
                continue;
            }

            if (spell.spellInfo.id == spell_id) {
                return spell;
            }
        }

        return null;
    }

    public final int getCurrentSpellCastTime(int spell_id) {
        var spell = findCurrentSpellBySpellId(spell_id);

        if (spell != null) {
            return spell.getCastTime();
        }

        return 0;
    }

    public boolean hasSpellFocus() {
        return hasSpellFocus(null);
    }

    public boolean hasSpellFocus(Spell focusSpell) {
        return false;
    }

    /**
     * Check if our current channel spell has attribute SPELL_ATTR5_CAN_CHANNEL_WHEN_MOVING
     */
    public boolean isMovementPreventedByCasting() {
        // can always move when not casting
        if (!hasUnitState(UnitState.Casting)) {
            return false;
        }

        var spell = getCurrentSpell(CurrentSpellType.generic);

        if (spell != null) {
            if (canCastSpellWhileMoving(spell.spellInfo)) {
                return false;
            }
        }

        // channeled spells during channel stage (after the initial cast timer) allow movement with a specific spell attribute
        spell = getCurrentSpells().get(CurrentSpellType.Channeled);

        if (spell != null) {
            if (spell.getState() != SpellState.Finished && spell.isChannelActive()) {
                if (spell.spellInfo.isMoveAllowedChannel() || canCastSpellWhileMoving(spell.spellInfo)) {
                    return false;
                }
            }
        }

        // prohibit movement for all other spell casts
        return true;
    }

    public final boolean hasAuraTypeWithFamilyFlags(AuraType auraType, int familyName, Flag128 familyFlags) {
        for (var aura : getAuraEffectsByType(auraType)) {
            if (aura.getSpellInfo().getSpellFamilyName() == SpellFamilyName.forValue(familyName) && aura.getSpellInfo().getSpellFamilyFlags() & familyFlags) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasBreakableByDamageAuraType(AuraType type) {
        return hasBreakableByDamageAuraType(type, 0);
    }

    public final boolean hasBreakableByDamageAuraType(AuraType type, int excludeAura) {
        var auras = getAuraEffectsByType(type);

        for (var eff : auras) {
            if ((excludeAura == 0 || excludeAura != eff.getSpellInfo().getId()) && eff.getSpellInfo().hasAuraInterruptFlag(SpellAuraInterruptFlag.damage)) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasBreakableByDamageCrowdControlAura() {
        return hasBreakableByDamageCrowdControlAura(null);
    }

    public final boolean hasBreakableByDamageCrowdControlAura(Unit excludeCasterChannel) {
        int excludeAura = 0;
        var currentChanneledSpell = excludeCasterChannel == null ? null : excludeCasterChannel.getCurrentSpell(CurrentSpellType.Channeled);

        if (currentChanneledSpell != null) {
            excludeAura = currentChanneledSpell.spellInfo.getId(); //Avoid self interrupt of channeled Crowd Control spells like Seduction
        }

        return (hasBreakableByDamageAuraType(AuraType.ModConfuse, excludeAura) || hasBreakableByDamageAuraType(AuraType.ModFear, excludeAura) || hasBreakableByDamageAuraType(AuraType.ModStun, excludeAura) || hasBreakableByDamageAuraType(AuraType.ModRoot, excludeAura) || hasBreakableByDamageAuraType(AuraType.ModRoot2, excludeAura) || hasBreakableByDamageAuraType(AuraType.Transform, excludeAura));
    }

    public final int getDiseasesByCaster(ObjectGuid casterGUID) {
        return getDiseasesByCaster(casterGUID, false);
    }

    public final int getDiseasesByCaster(ObjectGuid casterGUID, boolean remove) {
        AuraType[] diseaseAuraTypes = {AuraType.PeriodicDamage, AuraType.Linked};

        int diseases = 0;

        for (var aType : diseaseAuraTypes) {
            if (aType == AuraType.NONE) {
                break;
            }

            ArrayList<TValue> auras;
            tangible.OutObject<ArrayList<TValue>> tempOut_auras = new tangible.OutObject<ArrayList<TValue>>();
            if (modAuras.TryGetValue(aType, tempOut_auras)) {
                auras = tempOut_auras.outArgValue;
                for (var i = auras.size() - 1; i >= 0; i--) {
                    var eff = auras[i];

                    // Get auras with disease dispel type by caster
                    if (eff.spellInfo.dispel == DispelType.Disease && Objects.equals(eff.casterGuid, casterGUID)) {
                        ++diseases;

                        if (remove) {
                            removeAura(eff.id, eff.casterGuid);
                            i = 0;

                            continue;
                        }
                    }
                }
            } else {
                auras = tempOut_auras.outArgValue;
            }
        }

        return diseases;
    }

    public final void sendEnergizeSpellLog(Unit victim, int spellId, int amount, int overEnergize, Power powerType) {
        SpellEnergizeLog data = new SpellEnergizeLog();
        data.casterGUID = getGUID();
        data.targetGUID = victim.getGUID();
        data.spellID = spellId;
        data.type = powerType;
        data.amount = amount;
        data.overEnergize = overEnergize;
        data.logData.initialize(victim);

        sendCombatLogMessage(data);
    }

    public final void sendPlaySpellVisual(Unit target, int spellVisualId, short missReason, short reflectStatus, float travelSpeed, boolean speedAsTime, float launchDelay) {
        var playSpellVisual = new PlaySpellVisual();
        playSpellVisual.source = getGUID();
        playSpellVisual.target = target.getGUID();
        playSpellVisual.targetPosition = target.getLocation();
        playSpellVisual.spellVisualID = spellVisualId;
        playSpellVisual.travelSpeed = travelSpeed;
        playSpellVisual.missReason = missReason;
        playSpellVisual.reflectStatus = reflectStatus;
        playSpellVisual.speedAsTime = speedAsTime;
        playSpellVisual.launchDelay = launchDelay;
        sendMessageToSet(playSpellVisual, true);
    }

    public final void sendPlaySpellVisual(final Position targetPosition, int spellVisualId, short missReason, short reflectStatus, float travelSpeed, boolean speedAsTime, float launchDelay) {
        var playSpellVisual = new PlaySpellVisual();
        playSpellVisual.source = getGUID();
        playSpellVisual.targetPosition = targetPosition;
        playSpellVisual.spellVisualID = spellVisualId;
        playSpellVisual.travelSpeed = travelSpeed;
        playSpellVisual.missReason = missReason;
        playSpellVisual.reflectStatus = reflectStatus;
        playSpellVisual.speedAsTime = speedAsTime;
        playSpellVisual.launchDelay = launchDelay;
        sendMessageToSet(playSpellVisual, true);
    }

    public final void sendCancelSpellVisual(int id) {
        var cancelSpellVisual = new CancelSpellVisual();
        cancelSpellVisual.source = getGUID();
        cancelSpellVisual.spellVisualID = id;
        sendMessageToSet(cancelSpellVisual, true);
    }

    public final void sendCancelSpellVisualKit(int id) {
        var cancelSpellVisualKit = new CancelSpellVisualKit();
        cancelSpellVisualKit.source = getGUID();
        cancelSpellVisualKit.spellVisualKitID = id;
        sendMessageToSet(cancelSpellVisualKit, true);
    }

    public final void energizeBySpell(Unit victim, SpellInfo spellInfo, double damage, Power powerType) {
        energizeBySpell(victim, spellInfo, (int) damage, powerType);
    }

    public final void energizeBySpell(Unit victim, SpellInfo spellInfo, int damage, Power powerType) {
        var gain = victim.modifyPower(powerType, damage, false);
        var overEnergize = damage - gain;

        victim.getThreatManager().forwardThreatForAssistingMe(this, damage / 2, spellInfo, true);
        sendEnergizeSpellLog(victim, spellInfo.getId(), gain, overEnergize, powerType);
    }

    public final void applySpellImmune(int spellId, SpellImmunity op, Mechanics type, boolean apply) {
        applySpellImmune(spellId, op, (int) type.getValue(), apply);
    }

    public final void applySpellImmune(int spellId, SpellImmunity op, SpellSchoolMask type, boolean apply) {
        applySpellImmune(spellId, op, (int) type.getValue(), apply);
    }

    public final void applySpellImmune(int spellId, SpellImmunity op, AuraType type, boolean apply) {
        applySpellImmune(spellId, op, (int) type.getValue(), apply);
    }

    public final void applySpellImmune(int spellId, SpellImmunity op, SpellEffectName type, boolean apply) {
        applySpellImmune(spellId, op, (int) type.getValue(), apply);
    }

    public final void applySpellImmune(int spellId, SpellImmunity op, int type, boolean apply) {
        if (apply) {
            _spellImmune[op.getValue()].add(type, spellId);
        } else {
            var bounds = _spellImmune[op.getValue()].get(type);

            for (var spell : bounds) {
                if (spell == spellId) {
                    _spellImmune[op.getValue()].remove(type, spell);

                    break;
                }
            }
        }
    }

    public final boolean isImmunedToSpell(SpellInfo spellInfo, WorldObject caster) {
        return isImmunedToSpell(spellInfo, caster, false);
    }

    public final boolean isImmunedToSpell(SpellInfo spellInfo, WorldObject caster, boolean requireImmunityPurgesEffectAttribute) {
        if (spellInfo == null) {
            return false;
        }


//		bool hasImmunity(MultiMap<uint, uint> container, uint key)
//			{
//				var range = container.get(key);
//
//				if (!requireImmunityPurgesEffectAttribute)
//					return !range.isEmpty();
//
//				return range.Any(entry =>
//				{
//					var immunitySourceSpell = global.SpellMgr.getSpellInfo(entry, Difficulty.NONE);
//
//					if (immunitySourceSpell != null && immunitySourceSpell.hasAttribute(SpellAttr1.ImmunityPurgesEffect))
//						return true;
//
//					return false;
//				}
//				);
//			}

        // Single spell immunity.
        var idList = _spellImmune[SpellImmunity.id.getValue()];

        if (hasImmunity(idList, spellInfo.getId())) {
            return true;
        }

        if (spellInfo.hasAttribute(SpellAttr0.NoImmunities)) {
            return false;
        }

        var dispel = (int) spellInfo.getDispel().getValue();

        if (dispel != 0) {
            var dispelList = _spellImmune[SpellImmunity.dispel.getValue()];

            if (hasImmunity(dispelList, dispel)) {
                return true;
            }
        }

        // Spells that don't have effectMechanics.
        var mechanic = (int) spellInfo.getMechanic().getValue();

        if (mechanic != 0) {
            var mechanicList = _spellImmune[SpellImmunity.mechanic.getValue()];

            if (hasImmunity(mechanicList, mechanic)) {
                return true;
            }
        }

        var immuneToAllEffects = true;

        for (var spellEffectInfo : spellInfo.getEffects()) {
            // State/effect immunities applied by aura expect full spell immunity
            // Ignore effects with mechanic, they are supposed to be checked separately
            if (!spellEffectInfo.isEffect()) {
                continue;
            }

            if (!isImmunedToSpellEffect(spellInfo, spellEffectInfo, caster, requireImmunityPurgesEffectAttribute)) {
                immuneToAllEffects = false;

                break;
            }

            if (spellInfo.hasAttribute(SpellAttr4.NoPartialImmunity)) {
                return true;
            }
        }

        if (immuneToAllEffects) //Return immune only if the target is immune to all spell effects.
        {
            return true;
        }

        var schoolMask = (int) spellInfo.getSchoolMask().getValue();

        if (schoolMask != 0) {
            int schoolImmunityMask = 0;
            var schoolList = _spellImmune[SpellImmunity.school.getValue()];

            for (var pair : schoolList.KeyValueList) {
                if ((pair.key & schoolMask) == 0) {
                    continue;
                }

                var immuneSpellInfo = global.getSpellMgr().getSpellInfo(pair.value, getMap().getDifficultyID());

                if (requireImmunityPurgesEffectAttribute) {
                    if (immuneSpellInfo == null || !immuneSpellInfo.hasAttribute(SpellAttr1.ImmunityPurgesEffect)) {
                        continue;
                    }
                }

                // Consider the school immune if any of these conditions are not satisfied.
                // In case of no immuneSpellInfo, ignore that condition and check only the other conditions
                if ((immuneSpellInfo != null && !immuneSpellInfo.IsPositive) || !spellInfo.isPositive() || caster == null || !isFriendlyTo(caster)) {
                    if (!spellInfo.canPierceImmuneAura(immuneSpellInfo)) {
                        schoolImmunityMask |= pair.key;
                    }
                }
            }

            if ((schoolImmunityMask & schoolMask) == schoolMask) {
                return true;
            }
        }

        return false;
    }

    public boolean isImmunedToSpellEffect(SpellInfo spellInfo, SpellEffectInfo spellEffectInfo, WorldObject caster) {
        return isImmunedToSpellEffect(spellInfo, spellEffectInfo, caster, false);
    }

    public boolean isImmunedToSpellEffect(SpellInfo spellInfo, SpellEffectInfo spellEffectInfo, WorldObject caster, boolean requireImmunityPurgesEffectAttribute) {
        if (spellInfo == null) {
            return false;
        }

        if (spellInfo.hasAttribute(SpellAttr0.NoImmunities)) {
            return false;
        }


//		bool hasImmunity(MultiMap<uint, uint> container, uint key)
//			{
//				var range = container.get(key);
//
//				if (!requireImmunityPurgesEffectAttribute)
//					return !range.isEmpty();
//
//				return range.Any(entry =>
//				{
//					var immunitySourceSpell = global.SpellMgr.getSpellInfo(entry, Difficulty.NONE);
//
//					if (immunitySourceSpell != null)
//						if (immunitySourceSpell.hasAttribute(SpellAttr1.ImmunityPurgesEffect))
//							return true;
//
//					return false;
//				}
//				);
//			}

        // If m_immuneToEffect type contain this effect type, IMMUNE effect.
        var effectList = _spellImmune[SpellImmunity.effect.getValue()];

        if (hasImmunity(effectList, (int) spellEffectInfo.effect.getValue())) {
            return true;
        }

        var mechanic = (int) spellEffectInfo.mechanic.getValue();

        if (mechanic != 0) {
            var mechanicList = _spellImmune[SpellImmunity.mechanic.getValue()];

            if (hasImmunity(mechanicList, mechanic)) {
                return true;
            }
        }

        var aura = spellEffectInfo.applyAuraName;

        if (aura != 0) {
            if (!spellInfo.hasAttribute(SpellAttr3.AlwaysHit)) {
                var list = _spellImmune[SpellImmunity.state.getValue()];

                if (hasImmunity(list, (int) aura.getValue())) {
                    return true;
                }
            }

            if (!spellInfo.hasAttribute(SpellAttr2.NoSchoolImmunities)) {
                // Check for immune to application of harmful magical effects
                var immuneAuraApply = getAuraEffectsByType(AuraType.ModImmuneAuraApplySchool);

                for (var auraEffect : immuneAuraApply) {
                    if ((boolean) (auraEffect.getMiscValue() & spellInfo.getSchoolMask().getValue()) && ((caster && !isFriendlyTo(caster)) || !spellInfo.isPositiveEffect(spellEffectInfo.effectIndex))) // Harmful
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public final boolean isImmunedToDamage(SpellSchoolMask schoolMask) {
        if (schoolMask == spellSchoolMask.NONE) {
            return false;
        }

        // If m_immuneToSchool type contain this school type, IMMUNE damage.
        var schoolImmunityMask = getSchoolImmunityMask();

        if ((spellSchoolMask.forValue(schoolImmunityMask).getValue() & schoolMask.getValue()) == schoolMask.getValue()) // We need to be immune to all types
        {
            return true;
        }

        // If m_immuneToDamage type contain magic, IMMUNE damage.
        var damageImmunityMask = getDamageImmunityMask();

        if ((spellSchoolMask.forValue(damageImmunityMask).getValue() & schoolMask.getValue()) == schoolMask.getValue()) // We need to be immune to all types
        {
            return true;
        }

        return false;
    }

    public final boolean isImmunedToDamage(SpellInfo spellInfo) {
        if (spellInfo == null) {
            return false;
        }

        // for example 40175
        if (spellInfo.hasAttribute(SpellAttr0.NoImmunities) && spellInfo.hasAttribute(SpellAttr3.AlwaysHit)) {
            return false;
        }

        if (spellInfo.hasAttribute(SpellAttr1.ImmunityToHostileAndFriendlyEffects) || spellInfo.hasAttribute(SpellAttr2.NoSchoolImmunities)) {
            return false;
        }

        var schoolMask = (int) spellInfo.getSchoolMask().getValue();

        if (schoolMask != 0) {
            // If m_immuneToSchool type contain this school type, IMMUNE damage.
            int schoolImmunityMask = 0;
            var schoolList = _spellImmune[SpellImmunity.school.getValue()];

            for (var pair : schoolList.KeyValueList) {
                if ((boolean) (pair.key & schoolMask) && !spellInfo.canPierceImmuneAura(global.getSpellMgr().getSpellInfo(pair.value, getMap().getDifficultyID()))) {
                    schoolImmunityMask |= pair.key;
                }
            }

            // // We need to be immune to all types
            if ((schoolImmunityMask & schoolMask) == schoolMask) {
                return true;
            }

            // If m_immuneToDamage type contain magic, IMMUNE damage.
            var damageImmunityMask = getDamageImmunityMask();

            if ((damageImmunityMask & schoolMask) == schoolMask) // We need to be immune to all types
            {
                return true;
            }
        }

        return false;
    }

    public final boolean canCastSpellWhileMoving(SpellInfo spellInfo) {
        if (hasAuraTypeWithAffectMask(AuraType.CastWhileWalking, spellInfo)) {
            return true;
        }

        if (hasAuraType(AuraType.CastWhileWalkingAll)) {
            return true;
        }

        for (var label : spellInfo.labels) {
            if (hasAuraTypeWithMiscvalue(AuraType.CastWhileWalkingBySpellLabel, (int) label)) {
                return true;
            }
        }

        return false;
    }

    public final void castWithDelay(Duration delay, Unit target, int spellId, boolean triggered) {
        getEvents().addEvent(new DelayedCastEvent(this, target, spellId, new CastSpellExtraArgs(triggered)), delay, true);
    }

    public final void castWithDelay(Duration delay, Unit target, int spellId, CastSpellExtraArgs args) {
        getEvents().addEvent(new DelayedCastEvent(this, target, spellId, args), delay, true);
    }

    public final void castStop() {
        castStop(0);
    }

    public final void castStop(int except_spellid) {
        for (var i = CurrentSpellType.generic; i.getValue() < CurrentSpellType.max.getValue(); i++) {
            if ((getCurrentSpells().containsKey(i) && (var spell = getCurrentSpells().get(i)) ==var spell) &&
            spell != null && spell.spellInfo.id != except_spellid)
            {
                interruptSpell(i, false);
            }
        }
    }

    public final void updateEmpowerState(EmpowerState state) {
        updateEmpowerState(state, 0);
    }

    public final void updateEmpowerState(EmpowerState state, int except_spellid) {
        for (var i = CurrentSpellType.generic; i.getValue() < CurrentSpellType.max.getValue(); i++) {
            if ((getCurrentSpells().containsKey(i) && (var spell = getCurrentSpells().get(i)) ==var spell) &&
            spell != null && spell.spellInfo.id == except_spellid)
            {
                spell.setEmpowerState(state);
            }
        }
    }

    public final short getMaxSkillValueForLevel() {
        return getMaxSkillValueForLevel(null);
    }

    public final short getMaxSkillValueForLevel(Unit target) {
        return (short) (target != null ? getLevelForTarget(target) : getLevel() * 5);
    }

    public final Spell getCurrentSpell(CurrentSpellType spellType) {
        return getCurrentSpells().get(spellType);
    }

    public final void setCurrentCastSpell(Spell pSpell) {
        var CSpellType = pSpell.getCurrentContainer();

        if (pSpell == getCurrentSpell(CSpellType)) // avoid breaking self
        {
            return;
        }

        // special breakage effects:
        switch (CSpellType) {
            case Generic: {
                interruptSpell(CurrentSpellType.generic, false);

                // generic spells always break channeled not delayed spells
                if (getCurrentSpell(CurrentSpellType.Channeled) != null && !getCurrentSpell(CurrentSpellType.Channeled).spellInfo.hasAttribute(SpellAttr5.AllowActionsDuringChannel)) {
                    interruptSpell(CurrentSpellType.Channeled, false);
                }

                // autorepeat breaking
                if (getCurrentSpell(CurrentSpellType.AutoRepeat) != null) {
                    // break autorepeat if not Auto Shot
                    if (getCurrentSpells().get(CurrentSpellType.AutoRepeat).spellInfo.getId() != 75) {
                        interruptSpell(CurrentSpellType.AutoRepeat);
                    }
                }

                if (pSpell.spellInfo.calcCastTime() > 0) {
                    addUnitState(UnitState.Casting);
                }

                break;
            }
            case Channeled: {
                // channel spells always break generic non-delayed and any channeled spells
                interruptSpell(CurrentSpellType.generic, false);
                interruptSpell(CurrentSpellType.Channeled);

                // it also does break autorepeat if not Auto Shot
                if (getCurrentSpell(CurrentSpellType.AutoRepeat) != null && getCurrentSpells().get(CurrentSpellType.AutoRepeat).spellInfo.getId() != 75) {
                    interruptSpell(CurrentSpellType.AutoRepeat);
                }

                addUnitState(UnitState.Casting);

                break;
            }
            case AutoRepeat: {
                if (getCurrentSpell(CSpellType) && getCurrentSpell(CSpellType).getState() == SpellState.IDLE) {
                    getCurrentSpell(CSpellType).setState(SpellState.Finished);
                }

                // only Auto Shoot does not break anything
                if (pSpell.spellInfo.getId() != 75) {
                    // generic autorepeats break generic non-delayed and channeled non-delayed spells
                    interruptSpell(CurrentSpellType.generic, false);
                    interruptSpell(CurrentSpellType.Channeled, false);
                }

                break;
            }
            default:
                break; // other spell types don't break anything now
        }

        // current spell (if it is still here) may be safely deleted now
        if (getCurrentSpell(CSpellType) != null) {
            getCurrentSpells().get(CSpellType).setReferencedFromCurrent(false);
        }

        // set new current spell
        getCurrentSpells().put(CSpellType, pSpell);
        pSpell.setReferencedFromCurrent(true);

        pSpell.selfContainer = getCurrentSpells().get(pSpell.getCurrentContainer());
    }

    public final boolean isNonMeleeSpellCast(boolean withDelayed, boolean skipChanneled, boolean skipAutorepeat, boolean isAutoshoot) {
        return isNonMeleeSpellCast(withDelayed, skipChanneled, skipAutorepeat, isAutoshoot, true);
    }

    public final boolean isNonMeleeSpellCast(boolean withDelayed, boolean skipChanneled, boolean skipAutorepeat) {
        return isNonMeleeSpellCast(withDelayed, skipChanneled, skipAutorepeat, false, true);
    }

    public final boolean isNonMeleeSpellCast(boolean withDelayed, boolean skipChanneled) {
        return isNonMeleeSpellCast(withDelayed, skipChanneled, false, false, true);
    }

    public final boolean isNonMeleeSpellCast(boolean withDelayed) {
        return isNonMeleeSpellCast(withDelayed, false, false, false, true);
    }

    public final boolean isNonMeleeSpellCast(boolean withDelayed, boolean skipChanneled, boolean skipAutorepeat, boolean isAutoshoot, boolean skipInstant) {
        // We don't do loop here to explicitly show that melee spell is excluded.
        // Maybe later some special spells will be excluded too.

        // generic spells are cast when they are not finished and not delayed
        var currentSpell = getCurrentSpell(CurrentSpellType.generic);

        if (currentSpell && (currentSpell.getState() != SpellState.Finished) && (withDelayed || currentSpell.getState() != SpellState.Delayed)) {
            if (!skipInstant || currentSpell.getCastTime() != 0) {
                if (!isAutoshoot || !currentSpell.spellInfo.hasAttribute(SpellAttr2.DoNotResetCombatTimers)) {
                    return true;
                }
            }
        }

        currentSpell = getCurrentSpell(CurrentSpellType.Channeled);

        // channeled spells may be delayed, but they are still considered cast
        if (!skipChanneled && currentSpell && (currentSpell.getState() != SpellState.Finished)) {
            if (!isAutoshoot || !currentSpell.spellInfo.hasAttribute(SpellAttr2.DoNotResetCombatTimers)) {
                return true;
            }
        }

        currentSpell = getCurrentSpell(CurrentSpellType.AutoRepeat);

        // autorepeat spells may be finished or delayed, but they are still considered cast
        if (!skipAutorepeat && currentSpell) {
            return true;
        }

        return false;
    }

    public final void _DeleteRemovedAuras() {
        synchronized (removedAuras) {
            while (!removedAuras.isEmpty()) {
                removedAuras.get(0).dispose();
                removedAuras.remove(0);
            }
        }

        removedAurasCount = 0;
    }

    public final double healBySpell(HealInfo healInfo) {
        return healBySpell(healInfo, false);
    }

    public final double healBySpell(HealInfo healInfo, boolean critical) {
        // calculate heal absorb and reduce healing
        calcHealAbsorb(healInfo);
        dealHeal(healInfo);

        sendHealSpellLog(healInfo, critical);

        return healInfo.getEffectiveHeal();
    }

    public final void applyCastTimePercentMod(double val, boolean apply) {
        applyCastTimePercentMod((float) val, apply);
    }

    public final void applyCastTimePercentMod(float val, boolean apply) {
        if (val > 0.0f) {
            applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modCastingSpeed), val, !apply);
            applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modSpellHaste), val, !apply);
            applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modHasteRegen), val, !apply);
        } else {
            applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modCastingSpeed), -val, apply);
            applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modSpellHaste), -val, apply);
            applyPercentModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().modHasteRegen), -val, apply);
        }
    }

    public final void removeAllGroupBuffsFromCaster(ObjectGuid casterGUID) {
        ownedAuras.query().hasCasterGuid(casterGUID).isGroupBuff().execute(this::RemoveOwnedAura);
    }

    public final void delayOwnedAuras(int spellId, ObjectGuid caster, int delaytime) {
        var range = ownedAuras.query().hasSpellId(spellId).hasCasterGuid(caster).getResults();

        for (var aura : range) {
            if (aura.getDuration() < delaytime) {
                aura.setDuration(0);
            } else {
                aura.setDuration(aura.getDuration() - delaytime);
            }

            // update for out of range group members (on 1 slot use)
            aura.setNeedClientUpdateForTargets();
        }
    }

    public final void calculateSpellDamageTaken(SpellNonMeleeDamage damageInfo, double damage, SpellInfo spellInfo, WeaponAttackType attackType, boolean crit, boolean blocked) {
        calculateSpellDamageTaken(damageInfo, damage, spellInfo, attackType, crit, blocked, null);
    }

    public final void calculateSpellDamageTaken(SpellNonMeleeDamage damageInfo, double damage, SpellInfo spellInfo, WeaponAttackType attackType, boolean crit) {
        calculateSpellDamageTaken(damageInfo, damage, spellInfo, attackType, crit, false, null);
    }

    public final void calculateSpellDamageTaken(SpellNonMeleeDamage damageInfo, double damage, SpellInfo spellInfo, WeaponAttackType attackType) {
        calculateSpellDamageTaken(damageInfo, damage, spellInfo, attackType, false, false, null);
    }

    public final void calculateSpellDamageTaken(SpellNonMeleeDamage damageInfo, double damage, SpellInfo spellInfo) {
        calculateSpellDamageTaken(damageInfo, damage, spellInfo, WeaponAttackType.BASE_ATTACK, false, false, null);
    }

    public final void calculateSpellDamageTaken(SpellNonMeleeDamage damageInfo, double damage, SpellInfo spellInfo, WeaponAttackType attackType, boolean crit, boolean blocked, Spell spell) {
        if (damage < 0) {
            return;
        }

        var victim = damageInfo.target;

        if (victim == null || !victim.isAlive()) {
            return;
        }

        var damageSchoolMask = damageInfo.schoolMask;

        // Spells with SPELL_ATTR4_IGNORE_DAMAGE_TAKEN_MODIFIERS ignore resilience because their damage is based off another spell's damage.
        if (!spellInfo.hasAttribute(SpellAttr4.IgnoreDamageTakenModifiers)) {
            if (isDamageReducedByArmor(damageSchoolMask, spellInfo)) {
                damage = (int) calcArmorReducedDamage(damageInfo.attacker, victim, (int) damage, spellInfo, attackType);
            }

            // Per-school calc
            switch (spellInfo.getDmgClass()) {
                // Melee and Ranged Spells
                case Ranged:
                case Melee: {
                    if (crit) {
                        damageInfo.hitInfo |= SpellHitType.crit.getValue();

                        // Calculate crit bonus
                        var crit_bonus = (int) damage;
                        // Apply crit_damage bonus for melee spells
                        var modOwner = getSpellModOwner();

                        if (modOwner != null) {
                            tangible.RefObject<Integer> tempRef_crit_bonus = new tangible.RefObject<Integer>(crit_bonus);
                            modOwner.applySpellMod(spellInfo, SpellModOp.CritDamageAndHealing, tempRef_crit_bonus);
                            crit_bonus = tempRef_crit_bonus.refArgValue;
                        }

                        damage += (int) crit_bonus;

                        // Increase crit damage from SPELL_AURA_MOD_CRIT_DAMAGE_BONUS
                        var critPctDamageMod = (getTotalAuraMultiplierByMiscMask(AuraType.ModCritDamageBonus, (int) spellInfo.getSchoolMask().getValue()) - 1.0f) * 100;

                        if (critPctDamageMod != 0) {
                            tangible.RefObject<Double> tempRef_damage = new tangible.RefObject<Double>(damage);
                            MathUtil.AddPct(tempRef_damage, (int) critPctDamageMod);
                            damage = tempRef_damage.refArgValue;
                        }
                    }

                    // Spell weapon based damage CAN BE crit & blocked at same time
                    if (blocked) {
                        // double blocked amount if block is critical
                        var value = victim.getBlockPercent(getLevel());

                        if (victim.isBlockCritical()) {
                            value *= 2; // double blocked percent
                        }

                        damageInfo.blocked = (int) MathUtil.CalculatePct(damage, value);

                        if (damage <= damageInfo.blocked) {
                            damageInfo.blocked = (int) damage;
                            damageInfo.fullBlock = true;
                        }

                        damage -= (int) damageInfo.blocked;
                    }

                    if (canApplyResilience()) {
                        tangible.RefObject<Double> tempRef_damage2 = new tangible.RefObject<Double>(damage);
                        applyResilience(victim, tempRef_damage2);
                        damage = tempRef_damage2.refArgValue;
                    }

                    break;
                }
                // Magical Attacks
                case None:
                case Magic: {
                    // If crit add critical bonus
                    if (crit) {
                        damageInfo.hitInfo |= SpellHitType.crit.getValue();
                        damage = (int) spellCriticalDamageBonus(this, spellInfo, (int) damage, victim);
                    }

                    if (canApplyResilience()) {
                        tangible.RefObject<Double> tempRef_damage3 = new tangible.RefObject<Double>(damage);
                        applyResilience(victim, tempRef_damage3);
                        damage = tempRef_damage3.refArgValue;
                    }

                    break;
                }
                default:
                    break;
            }
        }

        // Script Hook For CalculateSpellDamageTaken -- Allow scripts to change the Damage post class mitigation calculations
        tangible.RefObject<Double> tempRef_damage4 = new tangible.RefObject<Double>(damage);
        global.getScriptMgr().<IUnitModifySpellDamageTaken>ForEach(p -> p.ModifySpellDamageTaken(damageInfo.target, damageInfo.attacker, tempRef_damage4, spellInfo));
        damage = tempRef_damage4.refArgValue;

        // Calculate absorb resist
        if (damage < 0) {
            damage = 0;
        }

        damageInfo.damage = (int) damage;
        damageInfo.originalDamage = (int) damage;
        DamageInfo dmgInfo = new DamageInfo(damageInfo, DamageEffectType.SpellDirect, WeaponAttackType.BASE_ATTACK, ProcFlagsHit.NONE);
        calcAbsorbResist(dmgInfo, spell);
        damageInfo.absorb = dmgInfo.getAbsorb();
        damageInfo.resist = dmgInfo.getResist();

        if (damageInfo.absorb != 0) {
            damageInfo.hitInfo |= (damageInfo.Damage - damageInfo.absorb == 0 ? hitInfo.FullAbsorb.getValue() : hitInfo.PartialAbsorb.getValue());
        }

        if (damageInfo.resist != 0) {
            damageInfo.hitInfo |= (damageInfo.Damage - damageInfo.resist == 0 ? hitInfo.FullResist.getValue() : hitInfo.PartialResist.getValue());
        }

        damageInfo.damage = dmgInfo.getDamage();
    }

    public final void dealSpellDamage(SpellNonMeleeDamage damageInfo, boolean durabilityLoss) {
        if (damageInfo == null) {
            return;
        }

        var victim = damageInfo.target;

        if (victim == null) {
            return;
        }

        if (!victim.isAlive() || victim.hasUnitState(UnitState.InFlight) || (victim.isTypeId(TypeId.UNIT) && victim.toCreature().isEvadingAttacks())) {
            return;
        }

        if (damageInfo.spell == null) {
            Log.outDebug(LogFilter.unit, "Unit.DealSpellDamage has no spell");

            return;
        }

        // Call default DealDamage
        CleanDamage cleanDamage = new cleanDamage(damageInfo.cleanDamage, damageInfo.absorb, WeaponAttackType.BASE_ATTACK, MeleeHitOutcome.NORMAL);
        damageInfo.damage = dealDamage(this, victim, damageInfo.damage, cleanDamage, DamageEffectType.SpellDirect, damageInfo.schoolMask, damageInfo.spell, durabilityLoss);
    }

    public final void sendSpellNonMeleeDamageLog(SpellNonMeleeDamage log) {
        SpellNonMeleeDamageLog packet = new SpellNonMeleeDamageLog();
        packet.me = log.target.getGUID();
        packet.casterGUID = log.attacker.getGUID();
        packet.castID = log.castId;
        packet.spellID = (int) (log.spell != null ? log.spell.getId() : 0);
        packet.visual = log.spellVisual;
        packet.damage = (int) log.damage;
        packet.originalDamage = (int) log.originalDamage;

        if (log.damage > log.preHitHealth) {
            packet.overkill = (int) (log.Damage - log.preHitHealth);
        } else {
            packet.overkill = -1;
        }

        packet.schoolMask = (byte) log.schoolMask.getValue();
        packet.absorbed = (int) log.absorb;
        packet.resisted = (int) log.resist;
        packet.shieldBlock = (int) log.blocked;
        packet.periodic = log.periodicLog;
        packet.flags = (int) log.hitInfo;

        ContentTuningParams contentTuningParams = new contentTuningParams();

        if (contentTuningParams.generateDataForUnits(log.attacker, log.target)) {
            packet.contentTuning = contentTuningParams;
        }

        sendCombatLogMessage(packet);
    }

    public final void sendPeriodicAuraLog(SpellPeriodicAuraLogInfo info) {
        var aura = info.getAuraEff();

        SpellPeriodicAuraLog data = new SpellPeriodicAuraLog();
        data.targetGUID = getGUID();
        data.casterGUID = aura.getCasterGuid();
        data.spellID = aura.getId();
        data.logData.initialize(this);

        SpellPeriodicAuraLog.SpellLogEffect spellLogEffect = new SpellPeriodicAuraLog.SpellLogEffect();
        spellLogEffect.effect = (int) aura.getAuraType().getValue();
        spellLogEffect.amount = (int) info.getDamage();
        spellLogEffect.originalDamage = (int) info.getOriginalDamage();
        spellLogEffect.overHealOrKill = (int) info.getOverDamage();
        spellLogEffect.schoolMaskOrPower = (int) aura.getSpellInfo().getSchoolMask().getValue();
        spellLogEffect.absorbedOrAmplitude = (int) info.getAbsorb();
        spellLogEffect.resisted = (int) info.getResist();
        spellLogEffect.crit = info.getCritical();
        // @todo: implement debug info

        ContentTuningParams contentTuningParams = new contentTuningParams();
        var caster = global.getObjAccessor().GetUnit(this, aura.getCasterGuid());

        if (caster && contentTuningParams.generateDataForUnits(caster, this)) {
            spellLogEffect.contentTuning = contentTuningParams;
        }

        data.effects.add(spellLogEffect);

        sendCombatLogMessage(data);
    }

    public final void sendSpellDamageImmune(Unit target, int spellId, boolean isPeriodic) {
        SpellOrDamageImmune spellOrDamageImmune = new SpellOrDamageImmune();
        spellOrDamageImmune.casterGUID = getGUID();
        spellOrDamageImmune.victimGUID = target.getGUID();
        spellOrDamageImmune.spellID = spellId;
        spellOrDamageImmune.isPeriodic = isPeriodic;
        sendMessageToSet(spellOrDamageImmune, true);
    }

    public final void sendSpellInstakillLog(int spellId, Unit caster) {
        sendSpellInstakillLog(spellId, caster, null);
    }

    public final void sendSpellInstakillLog(int spellId, Unit caster, Unit target) {
        SpellInstakillLog spellInstakillLog = new SpellInstakillLog();
        spellInstakillLog.caster = caster.getGUID();
        spellInstakillLog.target = target ? target.getGUID() : caster.getGUID();
        spellInstakillLog.spellID = spellId;
        sendMessageToSet(spellInstakillLog, false);
    }

    public final void removeAurasOnEvade() {
        if (isCharmedOwnedByPlayerOrPlayer()) // if it is a player owned creature it should not remove the aura
        {
            return;
        }

        // don't remove vehicle auras, passengers aren't supposed to drop off the vehicle
        // don't remove clone caster on evade (to be verified)

//		bool evadeAuraCheck(Aura aura)
//			{
//				if (aura.hasEffectType(AuraType.ControlVehicle))
//					return false;
//
//				if (aura.hasEffectType(AuraType.CloneCaster))
//					return false;
//
//				if (aura.spellInfo.hasAttribute(SpellAttr1.AuraStaysAfterCombat))
//					return false;
//
//				return true;
//			}


//		bool evadeAuraApplicationCheck(AuraApplication aurApp)
//			{
//				return evadeAuraCheck(aurApp.base);
//			}

        removeAppliedAuras(evadeAuraApplicationCheck);
        removeOwnedAuras(evadeAuraCheck);
    }

    public final void removeAllAurasOnDeath() {
        // used just after dieing to remove all visible auras
        // and disable the mods for the passive ones
        appliedAuras.query().isDeathPersistant(false).isPassive(false).execute(this::_UnapplyAura, AuraRemoveMode.Death);
        ownedAuras.query().isDeathPersistant(false).isPassive(false).execute(this::RemoveOwnedAura, AuraRemoveMode.Death);
    }

    public final void removeMovementImpairingAuras(boolean withRoot) {
        if (withRoot) {
            removeAurasWithMechanic(1 << mechanics.Root.getValue(), AuraRemoveMode.Default, 0, true);
        }

        removeAurasWithMechanic(1 << mechanics.Snare.getValue(), AuraRemoveMode.Default, 0, false);
    }

    public final void removeAllAurasRequiringDeadTarget() {
        appliedAuras.query().isPassive(false).isRequiringDeadTarget().execute(this::_UnapplyAura, AuraRemoveMode.Default);
        ownedAuras.query().isPassive(false).isRequiringDeadTarget().execute(this::RemoveOwnedAura);
    }

    public final AuraEffect isScriptOverriden(SpellInfo spell, int script) {
        var auras = getAuraEffectsByType(AuraType.OverrideClassScripts);

        for (var eff : auras) {
            if (eff.getMiscValue() == script) {
                if (eff.isAffectingSpell(spell)) {
                    return eff;
                }
            }
        }

        return null;
    }

    public final DiminishingLevels getDiminishing(DiminishingGroup group) {
        var diminish = _diminishing[group.getValue()];

        if (diminish.hitCount == 0) {
            return DiminishingLevels.Level1;
        }

        // If last spell was cast more than 18 seconds ago - reset level.
        if (diminish.stack == 0 && time.GetMSTimeDiffToNow(diminish.hitTime) > 18 * time.InMilliseconds) {
            return DiminishingLevels.Level1;
        }

        return diminish.hitCount;
    }

    public final void incrDiminishing(SpellInfo auraSpellInfo) {
        var group = auraSpellInfo.getDiminishingReturnsGroupForSpell();
        var currentLevel = getDiminishing(group);
        var maxLevel = auraSpellInfo.getDiminishingReturnsMaxLevel();

        var diminish = _diminishing[group.getValue()];

        if (currentLevel.getValue() < maxLevel.getValue()) {
            diminish.hitCount = currentLevel + 1;
        }
    }

    public final boolean applyDiminishingToDuration(SpellInfo auraSpellInfo, tangible.RefObject<Integer> duration, WorldObject caster, DiminishingLevels previousLevel) {
        var group = auraSpellInfo.getDiminishingReturnsGroupForSpell();

        if (duration.refArgValue == -1 || group == DiminishingGroup.NONE) {
            return true;
        }

        var limitDuration = auraSpellInfo.getDiminishingReturnsLimitDuration();

        // test pet/charm masters instead pets/charmeds
        var targetOwner = getCharmerOrOwner();
        var casterOwner = caster.getCharmerOrOwner();

        if (limitDuration > 0 && duration.refArgValue > limitDuration) {
            var target = targetOwner != null ? targetOwner : this;
            var source = casterOwner != null ? casterOwner : caster;

            if (target.isAffectedByDiminishingReturns() && source.isPlayer()) {
                duration.refArgValue = limitDuration;
            }
        }

        var mod = 1.0f;

        switch (group) {
            case Taunt:
                if (isTypeId(TypeId.UNIT) && toCreature().getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.ObeysTauntDiminishingReturns)) {
                    var diminish = previousLevel;

                    switch (diminish) {
                        case Level1:
                            break;
                        case Level2:
                            mod = 0.65f;

                            break;
                        case Level3:
                            mod = 0.4225f;

                            break;
                        case Immune:
                        case Level4:
                            mod = 0.274625f;

                            break;
                        case TauntImmune:
                            mod = 0.0f;

                            break;
                        default:
                            break;
                    }
                }

                break;
            case AOEKnockback:
                if (auraSpellInfo.getDiminishingReturnsGroupType() == DiminishingReturnsType.All || (auraSpellInfo.getDiminishingReturnsGroupType() == DiminishingReturnsType.player && (targetOwner ? targetOwner.isAffectedByDiminishingReturns() : isAffectedByDiminishingReturns()))) {
                    var diminish = previousLevel;

                    switch (diminish) {
                        case Level1:
                            break;
                        case Level2:
                            mod = 0.5f;

                            break;
                        default:
                            break;
                    }
                }

                break;
            default:
                if (auraSpellInfo.getDiminishingReturnsGroupType() == DiminishingReturnsType.All || (auraSpellInfo.getDiminishingReturnsGroupType() == DiminishingReturnsType.player && (targetOwner ? targetOwner.isAffectedByDiminishingReturns() : isAffectedByDiminishingReturns()))) {
                    var diminish = previousLevel;

                    switch (diminish) {
                        case Level1:
                            break;
                        case Level2:
                            mod = 0.5f;

                            break;
                        case Level3:
                            mod = 0.25f;

                            break;
                        case Level4:
                        case Immune:
                            mod = 0.0f;

                            break;
                        default:
                            break;
                    }
                }

                break;
        }

        duration.refArgValue = (int) (duration.refArgValue * mod);

        return duration.refArgValue != 0;
    }

    public final void applyDiminishingAura(DiminishingGroup group, boolean apply) {
        // Checking for existing in the table
        var diminish = _diminishing[group.getValue()];

        if (apply) {
            ++diminish.stack;
        } else if (diminish.stack != 0) {
            --diminish.stack;

            // Remember time after last aura from group removed
            if (diminish.stack == 0) {
                diminish.hitTime = gameTime.GetGameTimeMS();
            }
        }
    }

    public final boolean interruptNonMeleeSpells(boolean withDelayed, int spell_id) {
        return interruptNonMeleeSpells(withDelayed, spell_id, true);
    }

    public final boolean interruptNonMeleeSpells(boolean withDelayed) {
        return interruptNonMeleeSpells(withDelayed, 0, true);
    }

    // Interrupts

    public final boolean interruptNonMeleeSpells(boolean withDelayed, int spell_id, boolean withInstant) {
        var retval = false;

        // generic spells are interrupted if they are not finished or delayed
        if (getCurrentSpell(CurrentSpellType.generic) != null && (spell_id == 0 || getCurrentSpells().get(CurrentSpellType.generic).spellInfo.getId() == spell_id)) {
            if (interruptSpell(CurrentSpellType.generic, withDelayed, withInstant)) {
                retval = true;
            }
        }

        // autorepeat spells are interrupted if they are not finished or delayed
        if (getCurrentSpell(CurrentSpellType.AutoRepeat) != null && (spell_id == 0 || getCurrentSpells().get(CurrentSpellType.AutoRepeat).spellInfo.getId() == spell_id)) {
            if (interruptSpell(CurrentSpellType.AutoRepeat, withDelayed, withInstant)) {
                retval = true;
            }
        }

        // channeled spells are interrupted if they are not finished, even if they are delayed
        if (getCurrentSpell(CurrentSpellType.Channeled) != null && (spell_id == 0 || getCurrentSpells().get(CurrentSpellType.Channeled).spellInfo.getId() == spell_id)) {
            if (interruptSpell(CurrentSpellType.Channeled, true, true)) {
                retval = true;
            }
        }

        return retval;
    }

    public final Spell interruptSpell(CurrentSpellType spellType, boolean withDelayed, boolean withInstant) {
        return interruptSpell(spellType, withDelayed, withInstant, null);
    }

    public final Spell interruptSpell(CurrentSpellType spellType, boolean withDelayed) {
        return interruptSpell(spellType, withDelayed, true, null);
    }

    public final Spell interruptSpell(CurrentSpellType spellType) {
        return interruptSpell(spellType, true, true, null);
    }

    public final Spell interruptSpell(CurrentSpellType spellType, boolean withDelayed, boolean withInstant, Spell interruptingSpell) {
        Log.outDebug(LogFilter.unit, "Interrupt spell for unit {0}", getEntry());
        var spell = getCurrentSpells().get(spellType);

        if (spell != null && (withDelayed || spell.state != SpellState.Delayed) && (withInstant || spell.castTime > 0 || spell.state == SpellState.Casting)) {
            // for example, do not let self-stun aura interrupt itself
            if (!spell.IsInterruptable) {
                return null;
            }

            // send autorepeat cancel message for autorepeat spells
            if (spellType == CurrentSpellType.AutoRepeat) {
                if (isTypeId(TypeId.PLAYER)) {
                    toPlayer().sendAutoRepeatCancel(this);
                }
            }

            if (spell.state != SpellState.Finished) {
                spell.cancel();
            } else {
                getCurrentSpells().put(spellType, null);
                spell.setReferencedFromCurrent(false);
            }

            if (isCreature() && isAIEnabled()) {
                toCreature().getAi().onSpellFailed(spell.spellInfo);
            }

            ScriptManager.getInstance().<IUnitSpellInterrupted>ForEach(s -> s.spellInterrupted(spell, interruptingSpell));

            return spell;
        }

        return null;
    }

    public final void updateInterruptMask() {
        interruptMask = SpellAuraInterruptFlag.NONE;
        interruptMask2 = SpellAuraInterruptFlags2.NONE;

        for (var aurApp : interruptableAuras) {
            interruptMask = SpellAuraInterruptFlags.forValue(interruptMask.getValue() | aurApp.getBase().getSpellInfo().getAuraInterruptFlags().getValue());
            interruptMask2 = SpellAuraInterruptFlags2.forValue(interruptMask2.getValue() | aurApp.getBase().getSpellInfo().getAuraInterruptFlags2().getValue());
        }

        var spell = getCurrentSpell(CurrentSpellType.Channeled);

        if (spell != null) {
            if (spell.getState() == SpellState.Casting) {
                interruptMask = SpellAuraInterruptFlags.forValue(interruptMask.getValue() | spell.spellInfo.getChannelInterruptFlags().getValue());
                interruptMask2 = SpellAuraInterruptFlags2.forValue(interruptMask2.getValue() | spell.spellInfo.getChannelInterruptFlags2().getValue());
            }
        }
    }


    /**
     * Will add the aura to the unit. If the aura exists and it has a stack amount, a stack will be added up to the max stack amount.
     *
     * @param spellId Spell id of the aura to add
     * @return The aura and its applications.
     */
    public final Aura addAura(int spellId) {
        return addAura(spellId, this);
    }

    public final Aura addAura(int spellId, Unit target) {
        if (target == null) {
            return null;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(spellId, getMap().getDifficultyID());

        if (spellInfo == null) {
            return null;
        }

        return addAura(spellInfo, SpellConst.MaxEffects, target);
    }

    public final Aura addAura(SpellInfo spellInfo, HashSet<Integer> effMask, Unit target) {
        if (spellInfo == null) {
            return null;
        }

        if (!target.isAlive() && !spellInfo.isPassive() && !spellInfo.hasAttribute(SpellAttr2.AllowDeadTarget)) {
            return null;
        }

        if (target.isImmunedToSpell(spellInfo, this)) {
            return null;
        }

        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (!effMask.contains(spellEffectInfo.effectIndex)) {
                continue;
            }

            if (target.isImmunedToSpellEffect(spellInfo, spellEffectInfo, this)) {
                effMask.remove((Integer) spellEffectInfo.effectIndex);
            }
        }

        if (effMask.isEmpty()) {
            return null;
        }

        var castId = ObjectGuid.create(HighGuid.Cast, SpellCastSource.NORMAL, getLocation().getMapId(), spellInfo.getId(), getMap().generateLowGuid(HighGuid.Cast));

        AuraCreateInfo createInfo = new AuraCreateInfo(castId, spellInfo, getMap().getDifficultyID(), effMask, target);
        createInfo.setCaster(this);

        var aura = aura.tryRefreshStackOrCreate(createInfo);

        if (aura != null) {
            aura.applyForTargets();

            return aura;
        }

        return null;
    }

    public final void handleSpellClick(Unit clicker) {
        handleSpellClick(clicker, -1);
    }

    public final void handleSpellClick(Unit clicker, byte seatId) {
        var spellClickHandled = false;

        var spellClickEntry = vehicleKit != null ? vehicleKit.getCreatureEntry() : getEntry();
        var flags = vehicleKit ? TriggerCastFlags.IgnoreCasterMountedOrOnVehicle : TriggerCastFlags.NONE;

        var clickBounds = global.getObjectMgr().getSpellClickInfoMapBounds(spellClickEntry);

        for (var clickInfo : clickBounds) {
            //! First check simple relations from clicker to clickee
            if (!clickInfo.isFitToRequirements(clicker, this)) {
                continue;
            }

            //! Check database conditions
            if (!global.getConditionMgr().isObjectMeetingSpellClickConditions(spellClickEntry, clickInfo.spellId, clicker, this)) {
                continue;
            }

            var caster = (boolean) (clickInfo.castFlags & (byte) SpellClickCastFlags.CasterClicker.getValue()) ? clicker : this;
            var target = (boolean) (clickInfo.castFlags & (byte) SpellClickCastFlags.TargetClicker.getValue()) ? clicker : this;
            var origCasterGUID = (boolean) (clickInfo.castFlags & (byte) SpellClickCastFlags.OrigCasterOwner.getValue()) ? getOwnerGUID() : clicker.getGUID();

            var spellEntry = global.getSpellMgr().getSpellInfo(clickInfo.spellId, caster.getMap().getDifficultyID());
            // if (!spellEntry) should be checked at npc_spellclick load

            if (seatId > -1) {
                byte i = 0;
                var valid = false;

                for (var spellEffectInfo : spellEntry.getEffects()) {
                    if (spellEffectInfo.applyAuraName == AuraType.ControlVehicle) {
                        valid = true;

                        break;
                    }

                    ++i;
                }

                if (!valid) {
                    Logs.SQL.error("Spell {0} specified in npc_spellclick_spells is not a valid vehicle enter aura!", clickInfo.spellId);

                    continue;
                }

                if (isInMap(caster)) {
                    CastSpellExtraArgs args = new CastSpellExtraArgs(flags);
                    args.originalCaster = origCasterGUID;
                    args.addSpellMod(SpellValueMod.BasePoint0 + i, seatId + 1);
                    caster.castSpell(target, clickInfo.spellId, args);
                } else // This can happen during player._LoadAuras
                {
                    HashMap<Integer, Double> bp = new HashMap<Integer, Double>();

                    for (var spellEffectInfo : spellEntry.getEffects()) {
                        bp.put(spellEffectInfo.effectIndex, spellEffectInfo.basePoints);
                    }

                    bp.put(i, seatId);

                    AuraCreateInfo createInfo = new AuraCreateInfo(ObjectGuid.create(HighGuid.Cast, SpellCastSource.NORMAL, getLocation().getMapId(), spellEntry.getId(), getMap().generateLowGuid(HighGuid.Cast)), spellEntry, getMap().getDifficultyID(), SpellConst.MaxEffects, this);
                    createInfo.setCaster(clicker);
                    createInfo.setBaseAmount(bp);
                    createInfo.setCasterGuid(origCasterGUID);

                    aura.tryRefreshStackOrCreate(createInfo);
                }
            } else {
                if (isInMap(caster)) {
                    caster.castSpell(target, spellEntry.getId(), (new CastSpellExtraArgs()).setOriginalCaster(origCasterGUID));
                } else {
                    AuraCreateInfo createInfo = new AuraCreateInfo(ObjectGuid.create(HighGuid.Cast, SpellCastSource.NORMAL, getLocation().getMapId(), spellEntry.getId(), getMap().generateLowGuid(HighGuid.Cast)), spellEntry, getMap().getDifficultyID(), SpellConst.MaxEffects, this);
                    createInfo.setCaster(clicker);
                    createInfo.setCasterGuid(origCasterGUID);

                    aura.tryRefreshStackOrCreate(createInfo);
                }
            }

            spellClickHandled = true;
        }

        var creature = toCreature();

        if (creature && creature.isAIEnabled()) {
            tangible.RefObject<Boolean> tempRef_spellClickHandled = new tangible.RefObject<Boolean>(spellClickHandled);
            creature.getAi().onSpellClick(clicker, tempRef_spellClickHandled);
            spellClickHandled = tempRef_spellClickHandled.refArgValue;
        }
    }


    public final boolean hasAura(EnumFlag.FlagValue spellId) {
        return getAuraApplication(spellId).Any();
    }

    public final boolean hasAura(int spellId) {
        return getAuraApplication(spellId).Any();
    }

    public final boolean hasAura(int spellId, ObjectGuid casterGUID) {
        return hasAura(spellId, casterGUID, null);
    }

    public final boolean hasAura(int spellId, ObjectGuid casterGUID, ObjectGuid itemCasterGUID) {
        return getAuraApplication(spellId, casterGUID, itemCasterGUID) != null;
    }

    public final boolean hasAuraEffect(int spellId, int effIndex) {
        return hasAuraEffect(spellId, effIndex, null);
    }

    public final boolean hasAuraEffect(int spellId, int effIndex, ObjectGuid casterGUID) {
        return appliedAuras.query().hasSpellId(spellId).hasEffectIndex(effIndex).hasCasterGuid(casterGUID).getResults().Any();
    }

    public final boolean hasAuraWithMechanic(long mechanicMask) {
        for (var pair : getAppliedAuras()) {
            var spellInfo = pair.base.spellInfo;

            if (spellInfo.mechanic != 0 && (boolean) (mechanicMask & (1 << (int) spellInfo.mechanic))) {
                return true;
            }

            for (var spellEffectInfo : spellInfo.effects) {
                if (spellEffectInfo != null && pair.hasEffect(spellEffectInfo.effectIndex) && spellEffectInfo.isEffect() && spellEffectInfo.mechanic != 0) {
                    if ((mechanicMask & (1 << (int) spellEffectInfo.mechanic)) != 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public final boolean hasAuraType(AuraType auraType) {
        return !modAuras.get(auraType).isEmpty();
    }

    public final boolean hasAuraTypeWithCaster(AuraType auraType, ObjectGuid caster) {
        for (var auraEffect : getAuraEffectsByType(auraType)) {
            if (Objects.equals(caster, auraEffect.getCasterGuid())) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasAuraTypeWithMiscvalue(AuraType auraType, int miscvalue) {
        for (var auraEffect : getAuraEffectsByType(auraType)) {
            if (miscvalue == auraEffect.getMiscValue()) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasAuraTypeWithAffectMask(AuraType auraType, SpellInfo affectedSpell) {
        for (var auraEffect : getAuraEffectsByType(auraType)) {
            if (auraEffect.isAffectingSpell(affectedSpell)) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasAuraTypeWithValue(AuraType auraType, int value) {
        for (var auraEffect : getAuraEffectsByType(auraType)) {
            if (value == auraEffect.getAmount()) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasAuraTypeWithTriggerSpell(AuraType auratype, int triggerSpell) {
        for (var aura : getAuraEffectsByType(auratype)) {
            if (aura.getSpellEffectInfo().triggerSpell == triggerSpell) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasNegativeAuraWithInterruptFlag(SpellAuraInterruptFlags flag) {
        return hasNegativeAuraWithInterruptFlag(flag, null);
    }

    public final boolean hasNegativeAuraWithInterruptFlag(SpellAuraInterruptFlags flag, ObjectGuid guid) {
        if (!hasInterruptFlag(flag)) {
            return false;
        }

        for (var aura : interruptableAuras) {
            if (!aura.isPositive() && aura.getBase().getSpellInfo().hasAuraInterruptFlag(flag) && (guid.isEmpty() || Objects.equals(aura.getBase().getCasterGuid(), guid))) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasNegativeAuraWithInterruptFlag(SpellAuraInterruptFlags2 flag) {
        return hasNegativeAuraWithInterruptFlag(flag, null);
    }

    public final boolean hasNegativeAuraWithInterruptFlag(SpellAuraInterruptFlags2 flag, ObjectGuid guid) {
        if (!hasInterruptFlag(flag)) {
            return false;
        }

        for (var aura : interruptableAuras) {
            if (!aura.isPositive() && aura.getBase().getSpellInfo().hasAuraInterruptFlag(flag) && (guid.isEmpty() || Objects.equals(aura.getBase().getCasterGuid(), guid))) {
                return true;
            }
        }

        return false;
    }

    public final boolean hasStrongerAuraWithDR(SpellInfo auraSpellInfo, Unit caster) {
        var diminishGroup = auraSpellInfo.getDiminishingReturnsGroupForSpell();
        var level = getDiminishing(diminishGroup);

        for (var aura : appliedAuras.query().hasDiminishGroup(diminishGroup).getResults()) {
            var existingDuration = aura.getBase().getDuration();
            var newDuration = auraSpellInfo.getMaxDuration();
            tangible.RefObject<Integer> tempRef_newDuration = new tangible.RefObject<Integer>(newDuration);
            applyDiminishingToDuration(auraSpellInfo, tempRef_newDuration, caster, level);
            newDuration = tempRef_newDuration.refArgValue;

            if (newDuration > 0 && newDuration < existingDuration) {
                return true;
            }
        }

        return false;
    }

    public final int getAuraCount(int spellId) {
        int count = 0;

        for (var aura : appliedAuras.query().hasSpellId(spellId).getResults()) {
            if (aura.getBase().getStackAmount() == 0) {
                ++count;
            } else {
                count += aura.getBase().getStackAmount();
            }
        }

        return count;
    }

    public final Aura getAuraOfRankedSpell(int spellId) {
        var aurApp = getAuraApplicationOfRankedSpell(spellId);

        return (aurApp == null ? null : aurApp.getBase());
    }

    public final ArrayList<DispelableAura> getDispellableAuraList(WorldObject caster, int dispelMask) {
        return getDispellableAuraList(caster, dispelMask, false);
    }

    public final ArrayList<DispelableAura> getDispellableAuraList(WorldObject caster, int dispelMask, boolean isReflect) {
        ArrayList<DispelableAura> dispelList = new ArrayList<>();

        for (var aura : ownedAuras.query().isPassive().getResults()) {
            var aurApp = aura.getApplicationOfTarget(getGUID());

            if (aurApp == null) {
                continue;
            }

            if ((boolean) (aura.getSpellInfo().getDispelMask() & dispelMask)) {
                // do not remove positive auras if friendly target
                //               negative auras if non-friendly
                // unless we're reflecting (dispeller eliminates one of it's benefitial buffs)
                if (isReflect != (aurApp.isPositive() == isFriendlyTo(caster))) {
                    continue;
                }

                // 2.4.3 Patch Notes: "Dispel effects will no longer attempt to remove effects that have 100% dispel resistance."
                var chance = aura.calcDispelChance(this, !isFriendlyTo(caster));

                if (chance == 0) {
                    continue;
                }

                // The charges / stack amounts don't count towards the total number of auras that can be dispelled.
                // Ie: A dispel on a target with 5 stacks of Winters Chill and a Polymorph has 1 / (1 + 1) . 50% chance to dispell
                // Polymorph instead of 1 / (5 + 1) . 16%.
                var dispelCharges = aura.getSpellInfo().hasAttribute(SpellAttr7.DispelCharges);
                var charges = dispelCharges ? aura.getCharges() : aura.getStackAmount();

                if (charges > 0) {
                    dispelList.add(new DispelableAura(aura, chance, charges));
                }
            }
        }

        return dispelList;
    }

    public final void removeAurasWithInterruptFlags(SpellAuraInterruptFlag flag) {
        removeAurasWithInterruptFlags(flag, null);
    }

    public final void removeAurasWithInterruptFlags(SpellAuraInterruptFlag flag, SpellInfo source) {
        if (!hasInterruptFlag(flag)) {
            return;
        }

        // interrupt auras
        for (var i = 0; i < interruptableAuras.size(); i++) {
            var aura = interruptableAuras.get(i).getBase();

            if (aura.getSpellInfo().hasAuraInterruptFlag(flag) && (source == null || aura.getId() != source.getId()) && !isInterruptFlagIgnoredForSpell(flag, this, aura.getSpellInfo(), source)) {
                var removedAuras = removedAurasCount;
                removeAura(aura, AuraRemoveMode.Interrupt);

                if (removedAurasCount > removedAuras + 1) {
                    i = 0;
                }
            }
        }

        // interrupt channeled spell
        var spell = getCurrentSpell(CurrentSpellType.Channeled);

        if (spell != null) {
            if (spell.getState() == SpellState.Casting && spell.spellInfo.hasChannelInterruptFlag(flag) && (source == null || spell.spellInfo.getId() != source.getId()) && !isInterruptFlagIgnoredForSpell(flag, this, spell.spellInfo, source)) {
                interruptNonMeleeSpells(false);
            }
        }

        updateInterruptMask();
    }

    public final void removeAurasWithInterruptFlags(SpellAuraInterruptFlags2 flag) {
        removeAurasWithInterruptFlags(flag, null);
    }

    public final void removeAurasWithInterruptFlags(SpellAuraInterruptFlags2 flag, SpellInfo source) {
        if (!hasInterruptFlag(flag)) {
            return;
        }

        // interrupt auras
        for (var i = 0; i < interruptableAuras.size(); i++) {
            var aura = interruptableAuras.get(i).getBase();

            if (aura.getSpellInfo().hasAuraInterruptFlag(flag) && (source == null || aura.getId() != source.getId()) && !isInterruptFlagIgnoredForSpell(flag, this, aura.getSpellInfo(), source)) {
                var removedAuras = removedAurasCount;
                removeAura(aura, AuraRemoveMode.Interrupt);

                if (removedAurasCount > removedAuras + 1) {
                    i = 0;
                }
            }
        }

        // interrupt channeled spell
        var spell = getCurrentSpell(CurrentSpellType.Channeled);

        if (spell != null) {
            if (spell.getState() == SpellState.Casting && spell.spellInfo.hasChannelInterruptFlag(flag) && (source == null || spell.spellInfo.getId() != source.getId()) && !isInterruptFlagIgnoredForSpell(flag, this, spell.spellInfo, source)) {
                interruptNonMeleeSpells(false);
            }
        }

        updateInterruptMask();
    }

    public final void removeAurasWithMechanic(long mechanicMaskToRemove, AuraRemoveMode removeMode, int exceptSpellId) {
        removeAurasWithMechanic(mechanicMaskToRemove, removeMode, exceptSpellId, false);
    }

    public final void removeAurasWithMechanic(long mechanicMaskToRemove, AuraRemoveMode removeMode) {
        removeAurasWithMechanic(mechanicMaskToRemove, removeMode, 0, false);
    }

    public final void removeAurasWithMechanic(long mechanicMaskToRemove) {
        removeAurasWithMechanic(mechanicMaskToRemove, AuraRemoveMode.Default, 0, false);
    }

    public final void removeAurasWithMechanic(long mechanicMaskToRemove, AuraRemoveMode removeMode, int exceptSpellId, boolean withEffectMechanics) {
        ArrayList<aura> aurasToUpdateTargets = new ArrayList<>();

        removeAppliedAuras(aurApp ->
        {
            var aura = aurApp.base;

            if (exceptSpellId != 0 && aura.id == exceptSpellId) {
                return false;
            }

            var appliedMechanicMask = aura.spellInfo.getSpellMechanicMaskByEffectMask(aurApp.effectMask);

            if ((appliedMechanicMask & mechanicMaskToRemove) == 0) {
                return false;
            }

            // spell mechanic matches required mask for removal
            if (((1 << (int) aura.spellInfo.mechanic) & mechanicMaskToRemove) != 0 || withEffectMechanics) {
                return true;
            }

            // effect mechanic matches required mask for removal - don't remove, only update targets
            aurasToUpdateTargets.add(aura);

            return false;
        }, removeMode);

        for (var aura : aurasToUpdateTargets) {
            aura.updateTargetMap(aura.getCaster());

            // Fully remove the aura if all effects were removed
            if (!aura.isPassive() && aura.getOwner() == this && aura.getApplicationOfTarget(getGUID()) == null) {
                aura.remove(removeMode);
            }
        }
    }

    public final void removeAurasDueToSpellBySteal(int spellId, ObjectGuid casterGUID, WorldObject stealer) {
        removeAurasDueToSpellBySteal(spellId, casterGUID, stealer, 1);
    }

    public final void removeAurasDueToSpellBySteal(int spellId, ObjectGuid casterGUID, WorldObject stealer, int stolenCharges) {
        for (var aura : ownedAuras.query().hasSpellId(spellId).hasCasterGuid(casterGUID).getResults()) {
            HashMap<Integer, Double> damage = new HashMap<Integer, Double>();
            HashMap<Integer, Double> baseDamage = new HashMap<Integer, Double>();
            var effMask = new HashSet<Integer>();
            int recalculateMask = 0;
            var caster = aura.getCaster();

            for (var effect : aura.getAuraEffects().entrySet()) {
                var index = effect.getValue().effIndex;
                baseDamage.put(index, effect.getValue().baseAmount);
                damage.put(index, effect.getValue().amount);
                effMask.add(index);

                if (effect.getValue().canBeRecalculated()) {
                    recalculateMask |= 1 << index;
                }
            }

            var stealCharge = aura.getSpellInfo().hasAttribute(SpellAttr7.DispelCharges);
            // Cast duration to unsigned to prevent permanent aura's such as Righteous Fury being permanently added to caster
            var dur = (int) Math.min(2 * time.Minute * time.InMilliseconds, aura.getDuration());

            var unitStealer = stealer.toUnit();

            if (unitStealer != null) {
                var oldAura = unitStealer.getAura(aura.getId(), aura.getCasterGuid());

                if (oldAura != null) {
                    if (stealCharge) {
                        oldAura.modCharges(stolenCharges);
                    } else {
                        oldAura.modStackAmount(stolenCharges);
                    }

                    oldAura.setDuration((int) dur);
                } else {
                    // single target state must be removed before aura creation to preserve existing single target aura
                    if (aura.isSingleTarget()) {
                        aura.unregisterSingleTarget();
                    }

                    AuraCreateInfo createInfo = new AuraCreateInfo(aura.getCastId(), aura.getSpellInfo(), aura.getCastDifficulty(), effMask, stealer);
                    createInfo.setCasterGuid(aura.getCasterGuid());
                    createInfo.setBaseAmount(baseDamage);

                    var newAura = aura.tryRefreshStackOrCreate(createInfo);

                    if (newAura != null) {
                        // created aura must not be single target aura, so stealer won't loose it on recast
                        if (newAura.isSingleTarget()) {
                            newAura.unregisterSingleTarget();
                            // bring back single target aura status to the old aura
                            aura.setSingleTarget(true);
                            caster.getSingleCastAuras().add(aura);
                        }

                        // FIXME: using aura.GetMaxDuration() maybe not blizzlike but it fixes stealing of spells like Innervate
                        newAura.setLoadedState(aura.getMaxDuration(), (int) dur, stealCharge ? stolenCharges : aura.getCharges(), (byte) stolenCharges, recalculateMask, damage);
                        newAura.applyForTargets();
                    }
                }
            }

            if (stealCharge) {
                aura.modCharges(-stolenCharges, AuraRemoveMode.EnemySpell);
            } else {
                aura.modStackAmount(-stolenCharges, AuraRemoveMode.EnemySpell);
            }

            return;
        }
    }

    public final void removeAurasDueToItemSpell(int spellId, ObjectGuid castItemGuid) {
        appliedAuras.query().hasSpellId(spellId).hasCastItemGuid(castItemGuid).execute(this::RemoveAura);
    }

    public final void removeAurasByType(AuraType auraType, ObjectGuid casterGUID, Aura except, boolean negative) {
        removeAurasByType(auraType, casterGUID, except, negative, true);
    }

    public final void removeAurasByType(AuraType auraType, ObjectGuid casterGUID, Aura except) {
        removeAurasByType(auraType, casterGUID, except, true, true);
    }

    public final void removeAurasByType(AuraType auraType, ObjectGuid casterGUID) {
        removeAurasByType(auraType, casterGUID, null, true, true);
    }

    public final void removeAurasByType(AuraType auraType) {
        removeAurasByType(auraType, null, null, true, true);
    }

    public final void removeAurasByType(AuraType auraType, ObjectGuid casterGUID, Aura except, boolean negative, boolean positive) {
        ArrayList<TValue> auras;
        tangible.OutObject<ArrayList<TValue>> tempOut_auras = new tangible.OutObject<ArrayList<TValue>>();
        if (modAuras.TryGetValue(auraType, tempOut_auras)) {
            auras = tempOut_auras.outArgValue;
            for (var i = auras.size() - 1; i >= 0; i--) {
                var aura = auras[i].base;
                var aurApp = aura.getApplicationOfTarget(getGUID());

                if (aura != except && (casterGUID.isEmpty() || Objects.equals(aura.casterGuid, casterGUID)) && ((negative && !aurApp.IsPositive) || (positive && aurApp.IsPositive))) {
                    var removedAuras = removedAurasCount;
                    removeAura(aurApp);

                    if (removedAurasCount > removedAuras + 1) {
                        i = 0;
                    }
                }
            }
        } else {
            auras = tempOut_auras.outArgValue;
        }
    }

    public final void removeNotOwnSingleTargetAuras() {
        removeNotOwnSingleTargetAuras(false);
    }

    public final void removeNotOwnSingleTargetAuras(boolean onPhaseChange) {
        // Iterate m_ownedAuras - aura is marked as single target in Unit::AddAura (and pushed to m_ownedAuras).
        // m_appliedAuras will NOT contain the aura before first Unit::Update after adding it to m_ownedAuras.
        // Quickly removing such an aura will lead to it not being unregistered from caster's single cast auras container
        // leading to assertion failures if the aura was cast on a player that can
        // (and is changing map at the point where this function is called).
        // Such situation occurs when player is logging in inside an instance and fails the entry check for any reason.
        // The aura that was loaded from db (indirectly, via linked casts) gets removed before it has a chance
        // to register in m_appliedAuras
        for (var aura : ownedAuras.query().hasCasterGuid(getGUID()).isSingleTarget().getResults()) {
            if (onPhaseChange) {
                removeOwnedAura(aura.getId(), aura);
            } else {
                var caster = aura.getCaster();

                if (!caster || !caster.inSamePhase(this)) {
                    removeOwnedAura(aura.getId(), aura);
                }
            }
        }

        // single target auras at other targets
        for (var i = 0; i < scAuras.size(); i++) {
            var aura = scAuras.get(i);

            if (aura.getOwnerAsUnit() != this && (!onPhaseChange || !aura.getOwnerAsUnit().inSamePhase(this))) {
                aura.remove();
            }
        }
    }

    public final void removeOwnedAura(java.util.Map.entry<Integer, aura> keyValuePair) {
        removeOwnedAura(keyValuePair, AuraRemoveMode.Default);
    }

    public final void removeOwnedAura(java.util.Map.entry<Integer, aura> keyValuePair, AuraRemoveMode removeMode) {
        removeOwnedAura(keyValuePair.getKey(), keyValuePair.getValue(), removeMode);
    }

    public final void removeOwnedAura(int spellId, Aura aura) {
        removeOwnedAura(spellId, aura, AuraRemoveMode.Default);
    }

    public final void removeOwnedAura(int spellId, Aura aura, AuraRemoveMode removeMode) {
        if (aura.isRemoved()) {
            if (aura != null && ownedAuras.contains(aura)) {
                ownedAuras.remove(aura);
            }

            return;
        }

        ownedAuras.remove(aura);

        synchronized (removedAuras) {
            removedAuras.add(aura);
        }

        // Unregister single target aura
        if (aura.isSingleTarget()) {
            aura.unregisterSingleTarget();
        }

        aura._Remove(removeMode);
    }

    // All aura base removes should go through this function!

    public final void removeOwnedAura(int spellId, ObjectGuid casterGUID) {
        removeOwnedAura(spellId, casterGUID, AuraRemoveMode.Default);
    }

    public final void removeOwnedAura(int spellId) {
        removeOwnedAura(spellId, null, AuraRemoveMode.Default);
    }

    public final void removeOwnedAura(int spellId, ObjectGuid casterGUID, AuraRemoveMode removeMode) {
        ownedAuras.query().hasSpellId(spellId).hasCasterGuid(casterGUID).execute(this::RemoveOwnedAura);
    }

    public final void removeOwnedAura(Aura auraToRemove) {
        removeOwnedAura(auraToRemove, AuraRemoveMode.Default);
    }

    public final void removeOwnedAura(Aura auraToRemove, AuraRemoveMode removeMode) {
        if (auraToRemove.isRemoved()) {
            return;
        }

        if (removeMode == AuraRemoveMode.NONE) {
            Log.outError(LogFilter.spells, "Unit.removeOwnedAura() called with unallowed removeMode AURA_REMOVE_NONE, spellId {0}", auraToRemove.getId());

            return;
        }

        var spellId = auraToRemove.getId();

        if (ownedAuras.contains(auraToRemove)) {
            removeOwnedAura(spellId, auraToRemove, removeMode);
        }
    }

    public final void removeAurasDueToSpell(int spellId, ObjectGuid casterGUID) {
        removeAurasDueToSpell(spellId, casterGUID, AuraRemoveMode.Default);
    }

    public final void removeAurasDueToSpell(int spellId, ObjectGuid casterGUID, AuraRemoveMode removeMode) {
        appliedAuras.query().hasSpellId(spellId).hasCasterGuid(casterGUID).execute(this::RemoveAura, removeMode);
    }

    public final void removeAurasDueToSpellByDispel(int spellId, int dispellerSpellId, ObjectGuid casterGUID, WorldObject dispeller) {
        removeAurasDueToSpellByDispel(spellId, dispellerSpellId, casterGUID, dispeller, 1);
    }

    public final void removeAurasDueToSpellByDispel(int spellId, int dispellerSpellId, ObjectGuid casterGUID, WorldObject dispeller, byte chargesRemoved) {
        var aura = ownedAuras.query().hasSpellId(spellId).hasCasterGuid(casterGUID).getResults().FirstOrDefault();

        if (aura != null) {
            DispelInfo dispelInfo = new DispelInfo(dispeller, dispellerSpellId, chargesRemoved);

            // Call OnDispel hook on AuraScript
            aura.callScriptDispel(dispelInfo);

            if (aura.spellInfo.hasAttribute(SpellAttr7.DispelCharges)) {
                aura.modCharges(-dispelInfo.getRemovedCharges(), AuraRemoveMode.EnemySpell);
            } else {
                aura.modStackAmount(-dispelInfo.getRemovedCharges(), AuraRemoveMode.EnemySpell);
            }

            // Call AfterDispel hook on AuraScript
            aura.callScriptAfterDispel(dispelInfo);
        }

        ;
    }

    public final void removeAuraFromStack(int spellId, ObjectGuid casterGUID, AuraRemoveMode removeMode) {
        removeAuraFromStack(spellId, casterGUID, removeMode, 1);
    }

    public final void removeAuraFromStack(int spellId, ObjectGuid casterGUID) {
        removeAuraFromStack(spellId, casterGUID, AuraRemoveMode.Default, 1);
    }

    public final void removeAuraFromStack(int spellId) {
        removeAuraFromStack(spellId, null, AuraRemoveMode.Default, 1);
    }

    public final void removeAuraFromStack(int spellId, ObjectGuid casterGUID, AuraRemoveMode removeMode, short num) {
        ownedAuras.query().hasSpellId(spellId).hasCasterGuid(casterGUID).hasAuraType(AuraObjectType.unit).forEachResult(aura -> aura.modStackAmount(-num, removeMode));
    }

    public final void removeAura(int spellId) {
        removeAura(spellId, AuraRemoveMode.Default);
    }

    public final void removeAura(int spellId, AuraRemoveMode mode) {
        appliedAuras.query().hasSpellId(spellId).execute(this::RemoveAura);
    }

    public final void removeAuraApplicationCount(int spellId) {
        removeAuraApplicationCount(spellId, 1);
    }

    public final void removeAuraApplicationCount(int spellId, short count) {
        ownedAuras.query().hasSpellId(spellId).forEachResult(aura -> aura.modStackAmount(-count));
    }

    public final void removeAura(java.util.Map.entry<Integer, AuraApplication> appMap) {
        removeAura(appMap, AuraRemoveMode.Default);
    }

    public final void removeAura(java.util.Map.entry<Integer, AuraApplication> appMap, AuraRemoveMode mode) {
        removeAura(appMap.getValue(), mode);
    }

    public final void removeAuraBase(AuraApplication aurApp) {
        removeAuraBase(aurApp, AuraRemoveMode.Default);
    }

    public final void removeAuraBase(AuraApplication aurApp, AuraRemoveMode mode) {
        // Do not remove aura which is already being removed
        if (aurApp.getHasRemoveMode()) {
            return;
        }

        var aura = aurApp.getBase();
        _UnapplyAura(aurApp, mode);

        // Remove aura - for Area and Target auras
        if (aura.getOwner() == this) {
            aura.remove(mode);
        }
    }

    public final void removeAura(int spellId) {
        appliedAuras.query().hasSpellId(spellId).execute(this::RemoveAura);
    }


    public final <T extends Enum> void removeAura(T spellId) {
        removeAura((int) spellId);
    }

    public final <T extends Enum> void removeAura(T spellId, ObjectGuid caster) {
        removeAura(spellId, caster, AuraRemoveMode.Default);
    }


    public final <T extends Enum> void removeAura(T spellId, ObjectGuid caster, AuraRemoveMode removeMode) {
        removeAura((int) spellId, caster, removeMode);
    }

    public final void removeAura(int spellId, ObjectGuid caster) {
        removeAura(spellId, caster, AuraRemoveMode.Default);
    }

    public final void removeAura(int spellId, ObjectGuid caster, AuraRemoveMode removeMode) {
        appliedAuras.query().hasSpellId(spellId).hasCasterGuid(caster).execute(this::RemoveAura, removeMode);
    }

    public final void removeAura(AuraApplication aurApp) {
        removeAura(aurApp, AuraRemoveMode.Default);
    }

    public final void removeAura(AuraApplication aurApp, AuraRemoveMode mode) {
        if (aurApp == null) {
            return;
        }

        // we've special situation here, RemoveAura called while during aura removal
        // this kind of call is needed only when aura effect removal handler
        // or event triggered by it expects to remove
        // not yet removed effects of an aura
        if (aurApp.getHasRemoveMode()) {
            // remove remaining effects of an aura
            for (var eff : aurApp.getBase().getAuraEffects().entrySet()) {
                if (aurApp.hasEffect(eff.getKey())) {
                    aurApp._HandleEffect(eff.getKey(), false);
                }
            }

            return;
        }

        // no need to remove
        if (aurApp.getBase().getApplicationOfTarget(getGUID()) != aurApp || aurApp.getBase().isRemoved()) {
            return;
        }

        var spellId = aurApp.getBase().getId();

        removeAuraBase(aurApp, mode);
    }

    public final void removeAura(Aura aura) {
        removeAura(aura, AuraRemoveMode.Default);
    }

    public final void removeAura(Aura aura, AuraRemoveMode mode) {
        if (aura.isRemoved()) {
            return;
        }

        var aurApp = aura.getApplicationOfTarget(getGUID());

        if (aurApp != null) {
            removeAura(aurApp, mode);
        }
    }

    public final void removeAurasWithAttribute(SpellAttr0 flags) {
        switch (flags) {
            case OnlyIndoors:
                appliedAuras.query().onlyIndoors().execute(this::RemoveAura);

                break;

            case OnlyOutdoors:
                appliedAuras.query().onlyOutdoors().execute(this::RemoveAura);

                break;
            default:
                for (var app : appliedAuras.getAuraApplications()) {
                    if (app.base.spellInfo.hasAttribute(flags)) {
                        removeAura(app);
                    }
                }

                break;
        }
    }

    public final void removeAurasWithFamily(SpellFamilyName family, Flag128 familyFlag, ObjectGuid casterGUID) {
        appliedAuras.query().hasCasterGuid(casterGUID).hasSpellFamily(family).alsoMatches(a -> a.base.spellInfo.spellFamilyFlags & familyFlag).execute(this::RemoveAura);
    }

    public final void removeAppliedAuras(Func<AuraApplication, Boolean> check) {
        removeAppliedAuras(check, AuraRemoveMode.Default);
    }

    public final void removeAppliedAuras(tangible.Func1Param<AuraApplication, Boolean> check, AuraRemoveMode removeMode) {
        getAppliedAuras().CallOnMatch((pair) -> check.invoke(pair), (pair) -> removeAura(pair, removeMode));
    }

    public final void removeOwnedAuras(Func<aura, Boolean> check) {
        removeOwnedAuras(check, AuraRemoveMode.Default);
    }

    public final void removeOwnedAuras(tangible.Func1Param<aura, Boolean> check, AuraRemoveMode removeMode) {
        ownedAuras.getAuras().CallOnMatch((aura) -> check.invoke(aura), (aura) -> removeOwnedAura(aura.id, aura, removeMode));
    }

    public final void removeAurasByType(AuraType auraType, Func<AuraApplication, Boolean> check) {
        removeAurasByType(auraType, check, AuraRemoveMode.Default);
    }

    public final void removeAurasByType(AuraType auraType, tangible.Func1Param<AuraApplication, Boolean> check, AuraRemoveMode removeMode) {
        ArrayList<TValue> auras;
        tangible.OutObject<ArrayList<TValue>> tempOut_auras = new tangible.OutObject<ArrayList<TValue>>();
        if (modAuras.TryGetValue(auraType, tempOut_auras)) {
            auras = tempOut_auras.outArgValue;
            for (var i = auras.size() - 1; i >= 0; i--) {
                var aura = auras[i].base;
                var aurApp = aura.getApplicationOfTarget(getGUID());

                if (check.invoke(aurApp)) {
                    var removedAuras = removedAurasCount;
                    removeAura(aurApp, removeMode);

                    if (removedAurasCount > removedAuras + 1) {
                        i = 0;
                    }
                }
            }
        } else {
            auras = tempOut_auras.outArgValue;
        }
    }

    public final void removeAurasByShapeShift() {
        long mechanic_mask = (1 << mechanics.Snare.getValue()) | (1 << mechanics.Root.getValue());

        getAppliedAuras().CallOnMatch((auraApp) ->
        {
            var aura = auraApp.base;

            if ((aura.spellInfo.getAllEffectsMechanicMask() & mechanic_mask) != 0 && !aura.spellInfo.hasAttribute(SpellCustomAttributes.AuraCC)) {
                return true;
            }

            return false;
        }, (auraApp) -> removeAura(auraApp));
    }

    public final void removeAllAuras() {
        // this may be a dead loop if some events on aura remove will continiously apply aura on remove
        // we want to have all auras removed, so use your brain when linking events
        for (var counter = 0; !appliedAuras.isEmpty() || !ownedAuras.isEmpty(); counter++) {
            for (var aurAppIter : appliedAuras.getAuraApplications()) {
                _UnapplyAura(aurAppIter, AuraRemoveMode.Default);
            }

            for (var aurIter : ownedAuras.getAuras()) {
                removeOwnedAura(aurIter);
            }

            final int maxIteration = 50;

            // give this loop a few tries, if there are still auras then log as much information as possible
            if (counter >= maxIteration) {
                StringBuilder sstr = new StringBuilder();
                sstr.append(String.format("Unit::RemoveAllAuras() iterated %1$s times already but there are still %2$s m_appliedAuras and %3$s m_ownedAuras. Details:", maxIteration, appliedAuras.getCount(), ownedAuras.getCount()) + "\r\n");
                sstr.append(getDebugInfo() + "\r\n");

                if (!appliedAuras.isEmpty()) {
                    sstr.append("m_appliedAuras:" + "\r\n");

                    for (var auraAppPair : appliedAuras.getAuraApplications()) {
                        sstr.append(auraAppPair.getDebugInfo() + "\r\n");
                    }
                }

                if (!ownedAuras.isEmpty()) {
                    sstr.append("m_ownedAuras:" + "\r\n");

                    for (var auraPair : ownedAuras.getAuras()) {
                        sstr.append(auraPair.getDebugInfo() + "\r\n");
                    }
                }

                Log.outError(LogFilter.unit, sstr.toString());

                break;
            }
        }
    }

    public final void removeArenaAuras() {
        // in join, remove positive buffs, on end, remove negative
        // used to remove positive visible auras in arenas
        removeAppliedAuras(aurApp ->
        {
            var aura = aurApp.base;

            return (!aura.spellInfo.hasAttribute(SpellAttr4.AllowEnteringArena) && !aura.isPassive && (aurApp.IsPositive || !aura.spellInfo.hasAttribute(SpellAttr3.AllowAuraWhileDead))) || aura.spellInfo.hasAttribute(SpellAttr5.RemoveEnteringArena); // special marker, always remove
        });
    }

    public final void modifyAuraState(AuraStateType flag, boolean apply) {
        var mask = 1 << (flag.getValue() - 1);

        if (apply) {
            if ((getUnitData().auraState & mask) == 0) {
                setUpdateFieldFlagValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().auraState), mask);

                if (isTypeId(TypeId.PLAYER)) {
                    var sp_list = toPlayer().getSpellMap();

                    for (var spell : sp_list.entrySet()) {
                        if (spell.getValue().state == PlayerSpellState.removed || spell.getValue().disabled) {
                            continue;
                        }

                        var spellInfo = global.getSpellMgr().getSpellInfo(spell.getKey(), Difficulty.NONE);

                        if (spellInfo == null || !spellInfo.isPassive) {
                            continue;
                        }

                        if (spellInfo.casterAuraState == flag) {
                            castSpell(this, spell.getKey(), true);
                        }
                    }
                } else if (isPet()) {
                    var pet = getAsPet();

                    for (var spell : pet.spells.entrySet()) {
                        if (spell.getValue().state == PetSpellState.removed) {
                            continue;
                        }

                        var spellInfo = global.getSpellMgr().getSpellInfo(spell.getKey(), Difficulty.NONE);

                        if (spellInfo == null || !spellInfo.isPassive) {
                            continue;
                        }

                        if (spellInfo.casterAuraState == flag) {
                            castSpell(this, spell.getKey(), true);
                        }
                    }
                }
            }
        } else {
            if ((getUnitData().auraState & mask) != 0) {
                removeUpdateFieldFlagValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().auraState), mask);

                appliedAuras.query().hasCasterGuid(getGUID()).hasCasterAuraState(flag).alsoMatches(app -> app.base.spellInfo.isPassive || flag != AuraStateType.Enraged).execute(this::RemoveAura);
            }
        }
    }

    public final boolean hasAuraState(AuraStateType flag, SpellInfo spellProto) {
        return hasAuraState(flag, spellProto, null);
    }

    public final boolean hasAuraState(AuraStateType flag) {
        return hasAuraState(flag, null, null);
    }

    public final boolean hasAuraState(AuraStateType flag, SpellInfo spellProto, Unit caster) {
        if (caster != null) {
            if (spellProto != null) {
                if (caster.hasAuraTypeWithAffectMask(AuraType.AbilityIgnoreAurastate, spellProto)) {
                    return true;
                }
            }

            // Check per caster aura state
            // If aura with aurastate by caster not found return false
            if ((boolean) ((1 << flag.getValue()) & AuraStateType.PerCasterAuraStateMask.getValue())) {
                var range = auraStateAuras.get(flag);

                for (var auraApp : range) {
                    if (Objects.equals(auraApp.getBase().getCasterGuid(), caster.getGUID())) {
                        return true;
                    }
                }

                return false;
            }
        }

        return (getUnitData().auraState & (1 << (flag.getValue() - 1))) != 0;
    }

    public final void _ApplyAllAuraStatMods() {
        for (var i : getAppliedAuras()) {
            i.base.handleAllEffects(i, AuraEffectHandleModes.Stat, true);
        }
    }

    public final void _RemoveAllAuraStatMods() {
        for (var i : getAppliedAuras()) {
            i.base.handleAllEffects(i, AuraEffectHandleModes.Stat, false);
        }
    }

    public final void _UnapplyAura(java.util.Map.entry<Integer, AuraApplication> pair, AuraRemoveMode removeMode) {
        _UnapplyAura(pair.getValue(), removeMode);
    }

    // removes aura application from lists and unapplies effects
    public final void _UnapplyAura(AuraApplication aurApp, AuraRemoveMode removeMode) {
        var check = aurApp.getBase().getApplicationOfTarget(getGUID());

        if (check == null) {
            return; // The user logged out
        }

        if (check != aurApp) {
            Log.outError(LogFilter.Server, String.format("Tried to remove aura app with spell ID: %1$s that does not match. GetApplicationOfTarget: %2$s AuraApp: %3$s", aurApp.getBase().getSpellInfo().getId(), aurApp.getBase().getApplicationOfTarget(getGUID()).getGuid(), aurApp.getGuid()));

            return;
        }

        //Check if aura was already removed, if so just return.
        if (!appliedAuras.remove(aurApp)) {
            return;
        }

        aurApp.setRemoveMode(removeMode);
        var aura = aurApp.getBase();
        Log.outDebug(LogFilter.spells, "Aura {0} now is remove mode {1}", aura.getId(), removeMode);

        ++removedAurasCount;

        var caster = aura.getCaster();

        if (aura.getSpellInfo().getHasAnyAuraInterruptFlag()) {
            interruptableAuras.remove(aurApp);
            updateInterruptMask();
        }

        var auraStateFound = false;
        var auraState = aura.getSpellInfo().getAuraState();

        if (auraState != 0) {
            var canBreak = false;
            // Get mask of all aurastates from remaining auras
            var list = auraStateAuras.get(auraState);

            for (var i = 0; i < list.size() && !(auraStateFound && canBreak); ) {
                if (list[i].equals(aurApp)) {
                    auraStateAuras.remove(auraState, list[i]);
                    list = auraStateAuras.get(auraState);
                    i = 0;
                    canBreak = true;

                    continue;
                }

                auraStateFound = true;
                ++i;
            }
        }

        aurApp._Remove();
        aura._UnapplyForTarget(this, caster, aurApp);

        // remove effects of the spell - needs to be done after removing aura from lists
        for (var effect : aurApp.getBase().getAuraEffects().entrySet()) {
            if (aurApp.hasEffect(effect.getKey())) {
                aurApp._HandleEffect(effect.getKey(), false);
            }
        }

        // all effect mustn't be applied
        // Cypher.Assert(aurApp.effectMask.count == 0);

        // Remove totem at next update if totem loses its aura
        if (aurApp.getRemoveMode() == AuraRemoveMode.Expire && isTypeId(TypeId.UNIT) && isTotem()) {
            if (toTotem().getSpell() == aura.getId() && toTotem().GetTotemType() == TotemType.Passive) {
                toTotem().setDeathState(deathState.JustDied);
            }
        }

        // Remove aurastates only if needed and were not found
        if (auraState != 0) {
            if (!auraStateFound) {
                modifyAuraState(auraState, false);
            } else {
                // update for casters, some shouldn't 'see' the aura state
                var aStateMask = (1 << (auraState.getValue() - 1));

                if ((aStateMask & (int) AuraStateType.PerCasterAuraStateMask.getValue()) != 0) {
                    getValues().modifyValue(getUnitData()).modifyValue(getUnitData().auraState);
                    forceUpdateFieldChange();
                }
            }
        }

        aura.handleAuraSpecificMods(aurApp, caster, false, false);

        var player = toPlayer();

        if (player != null) {
            if (global.getConditionMgr().isSpellUsedInSpellClickConditions(aurApp.getBase().getId())) {
                player.updateVisibleGameobjectsOrSpellClicks();
            }
        }
    }

    public final boolean tryGetAuraEffect(int spellId, int effIndex, ObjectGuid casterGUID, tangible.OutObject<AuraEffect> auraEffect) {
        auraEffect.outArgValue = getAuraEffect(spellId, effIndex, casterGUID);

        return auraEffect.outArgValue != null;
    }

    public final boolean tryGetAuraEffect(int spellId, int effIndex, tangible.OutObject<AuraEffect> auraEffect) {
        auraEffect.outArgValue = getAuraEffect(spellId, effIndex);

        return auraEffect.outArgValue != null;
    }

    public final AuraEffect getAuraEffect(int spellId, int effIndex) {
        return getAuraEffect(spellId, effIndex, null);
    }

    public final AuraEffect getAuraEffect(int spellId, int effIndex, ObjectGuid casterGUID) {
        return (appliedAuras.query().hasSpellId(spellId).hasEffectIndex(effIndex).hasCasterGuid(casterGUID).getResults().FirstOrDefault() == null ? null : ((appliedAuras.query().hasSpellId(spellId).hasEffectIndex(effIndex).hasCasterGuid(casterGUID).getResults().FirstOrDefault().base == null ? null : appliedAuras.query().hasSpellId(spellId).hasEffectIndex(effIndex).hasCasterGuid(casterGUID).getResults().FirstOrDefault().base.getEffect(effIndex))));
    }

    public final AuraEffect getAuraEffectOfRankedSpell(int spellId, int effIndex) {
        return getAuraEffectOfRankedSpell(spellId, effIndex, null);
    }

    public final AuraEffect getAuraEffectOfRankedSpell(int spellId, int effIndex, ObjectGuid casterGUID) {
        var rankSpell = global.getSpellMgr().getFirstSpellInChain(spellId);

        while (rankSpell != 0) {
            var aurEff = getAuraEffect(rankSpell, effIndex, casterGUID);

            if (aurEff != null) {
                return aurEff;
            }

            rankSpell = global.getSpellMgr().getNextSpellInChain(rankSpell);
        }

        return null;
    }

    public final AuraEffect getAuraEffect(AuraType type, SpellFamilyName family, Flag128 familyFlag) {
        return getAuraEffect(type, family, familyFlag, null);
    }

    public final AuraEffect getAuraEffect(AuraType type, SpellFamilyName family, Flag128 familyFlag, ObjectGuid casterGUID) {
        var auras = getAuraEffectsByType(type);

        for (var aura : auras) {
            var spell = aura.getSpellInfo();

            if (spell.getSpellFamilyName() == family && spell.getSpellFamilyFlags() & familyFlag) {
                if (!casterGUID.isEmpty() && ObjectGuid.opNotEquals(aura.getCasterGuid(), casterGUID)) {
                    continue;
                }

                return aura;
            }
        }

        return null;
    }

    // spell mustn't have familyflags

    public final AuraApplication getAuraApplication(Predicate<AuraApplication> predicate) {
        return appliedAuras.values().stream().flatMap(List::stream).filter(predicate).findFirst().orElse(null);
    }

    public final AuraApplication getAuraApplication(int spellId, Predicate<AuraApplication> predicate) {
        return appliedAuras.get(spellId).stream().filter(predicate).findFirst().orElse(null);
    }

    public final AuraApplication getAuraApplication(int spellId, ObjectGuid casterGUID) {
        return getAuraApplication(spellId, casterGUID, null);
    }

    public final AuraApplication getAuraApplication(int spellId, ObjectGuid casterGUID, ObjectGuid itemCasterGUID, int reqEffMask, AuraApplication except) {
        Predicate<AuraApplication> predicate = app -> {
            Aura aura = app.getBase();

            if (((aura.getEffectMask() & reqEffMask) == reqEffMask)
                    && (!casterGUID || aura.getCasterGuid() == casterGUID)
                    && (!itemCasterGUID || aura.getCastItemGuid() == itemCasterGUID)
                    && (!except || except != app))
            {
                return true;
            }

            return false;
        };
        return appliedAuras.get(spellId).stream().filter(predicate).findFirst().orElse(null);
    }

    public final double getAuraEffectAmount(int spellId, byte effIndex) {
        var aurEff = getAuraEffect(spellId, effIndex);

        if (aurEff != null) {
            return aurEff.getAmount();
        }

        return 0;
    }


    public final <T extends Enum> boolean tryGetAura(T spellId, tangible.OutObject<aura> aura) {
        aura.outArgValue = getAura(spellId);

        return aura.outArgValue != null;
    }


    public final <T extends Enum> Aura getAura(T spellId) {
        return getAura((int) spellId);
    }

    public final <T extends Enum> Aura getAura(T spellId, ObjectGuid casterGUID) {
        return getAura(spellId, casterGUID, null);
    }


    public final <T extends Enum> Aura getAura(T spellId, ObjectGuid casterGUID, ObjectGuid itemCasterGUID) {
        return getAura((int) spellId, casterGUID, itemCasterGUID);
    }

    public final boolean tryGetAura(int spellId, tangible.OutObject<aura> aura) {
        aura.outArgValue = getAura(spellId);

        return aura.outArgValue != null;
    }

    public final Aura getAura(int spellId) {
        var aurApp = getAuraApplication(spellId) == null ? null : getAuraApplication(spellId).FirstOrDefault();

        return (aurApp == null ? null : aurApp.base);
    }

    public final Aura getAura(int spellId, ObjectGuid casterGUID) {
        return getAura(spellId, casterGUID, null);
    }

    public final Aura getAura(int spellId, ObjectGuid casterGUID, ObjectGuid itemCasterGUID) {
        var aurApp = getAuraApplication(spellId, casterGUID, itemCasterGUID);

        return (aurApp == null ? null : aurApp.getBase());
    }

    public final int buildAuraStateUpdateForTarget(Unit target) {
        var auraStates = getUnitData().auraState & ~(int) AuraStateType.PerCasterAuraStateMask.getValue();

        for (var state : auraStateAuras.KeyValueList) {
            if ((boolean) ((1 << (int) state.Key - 1) & (int) AuraStateType.PerCasterAuraStateMask.getValue())) {
                if (Objects.equals(state.value.base.casterGuid, target.getGUID())) {
                    auraStates |= (int) (1 << (int) state.Key - 1);
                }
            }
        }

        return auraStates;
    }

    public final void _ApplyAuraEffect(Aura aura, int effIndex) {
        var aurApp = aura.getApplicationOfTarget(getGUID());

        if (aurApp.getEffectMask().isEmpty()) {
            _ApplyAura(aurApp, effIndex);
        } else {
            aurApp._HandleEffect(effIndex, true);
        }
    }

    public final void _ApplyAura(AuraApplication aurApp, int effIndex) {
        _ApplyAura(aurApp, new HashSet<Integer>() {
            effIndex
        });
    }

    // handles effects of aura application
    // should be done after registering aura in lists
    public final void _ApplyAura(AuraApplication aurApp, HashSet<Integer> effMask) {
        var aura = aurApp.getBase();

        _RemoveNoStackAurasDueToAura(aura);

        if (aurApp.getHasRemoveMode()) {
            return;
        }

        // Update target aura state flag
        var aState = aura.getSpellInfo().getAuraState();

        if (aState != 0) {
            var aStateMask = (1 << (aState.getValue() - 1));

            // force update so the new caster registers it
            if (aStateMask.hasFlag((int) AuraStateType.PerCasterAuraStateMask.getValue()) && (getUnitData().auraState & aStateMask) != 0) {
                getValues().modifyValue(getUnitData()).modifyValue(getUnitData().auraState);
                forceUpdateFieldChange();
            } else {
                modifyAuraState(aState, true);
            }
        }

        if (aurApp.getHasRemoveMode()) {
            return;
        }

        // Sitdown on apply aura req seated
        if (aura.getSpellInfo().hasAuraInterruptFlag(SpellAuraInterruptFlags.standing) && !isSitState()) {
            setStandState(UnitStandStateType.Sit);
        }

        var caster = aura.getCaster();

        if (aurApp.getHasRemoveMode()) {
            return;
        }

        aura.handleAuraSpecificMods(aurApp, caster, true, false);

        // apply effects of the aura
        for (var effect : aurApp.getBase().getAuraEffects().entrySet()) {
            if (effMask.contains(effect.getKey()) && !(aurApp.getHasRemoveMode())) {
                aurApp._HandleEffect(effect.getKey(), true);
            }
        }

        var player = toPlayer();

        if (player != null) {
            if (global.getConditionMgr().isSpellUsedInSpellClickConditions(aurApp.getBase().getId())) {
                player.updateVisibleGameobjectsOrSpellClicks();
            }
        }
    }

    public final void _AddAura(UnitAura aura, Unit caster) {
        ownedAuras.add(aura);

        _RemoveNoStackAurasDueToAura(aura);

        if (aura.isRemoved()) {
            return;
        }

        aura.setSingleTarget(caster != null && aura.getSpellInfo().isSingleTarget());

        if (aura.isSingleTarget()) {
            // register single target aura
            caster.scAuras.add(aura);

            LinkedList<aura> aurasSharingLimit = new LinkedList<aura>();

            // remove other single target auras
            for (var scAura : caster.getSingleCastAuras()) {
                if (scAura != aura && scAura.isSingleTargetWith(aura)) {
                    aurasSharingLimit.offer(scAura);
                }
            }

            var maxOtherAuras = aura.getSpellInfo().getMaxAffectedTargets() - 1;

            while (aurasSharingLimit.size() > maxOtherAuras) {
                aurasSharingLimit.peek().remove();
                aurasSharingLimit.poll();
            }
        }
    }

    public final Aura _TryStackingOrRefreshingExistingAura(AuraCreateInfo createInfo) {
        // Check if these can stack anyway
        if (createInfo.casterGuid.isEmpty() && !createInfo.getSpellInfo().isStackableOnOneSlotWithDifferentCasters()) {
            createInfo.casterGuid = createInfo.caster.getGUID();
        }

        // passive and Incanter's Absorption and auras with different type can stack with themselves any number of times
        if (!createInfo.getSpellInfo().isMultiSlotAura()) {
            // check if cast item changed
            var castItemGUID = createInfo.castItemGuid;

            // find current aura from spell and change it's stackamount, or refresh it's duration
            var foundAura = getOwnedAura(createInfo.getSpellInfo().getId(), createInfo.getSpellInfo().isStackableOnOneSlotWithDifferentCasters() ? ObjectGuid.Empty : createInfo.casterGuid, createInfo.getSpellInfo().hasAttribute(SpellCustomAttributes.EnchantProc) ? castItemGUID : ObjectGuid.Empty);

            if (foundAura != null) {
                // effect masks do not match
                // extremely rare case
                // let's just recreate aura
                if (!createInfo.auraEffectMask.SetEquals(foundAura.getAuraEffects().keySet())) {
                    return null;
                }

                // update basepoints with new values - effect amount will be recalculated in ModStackAmount
                for (var spellEffectInfo : createInfo.getSpellInfo().getEffects()) {
                    var auraEff = foundAura.getEffect(spellEffectInfo.effectIndex);

                    if (auraEff == null) {
                        continue;
                    }

                    double bp;

                    if (createInfo.baseAmount != null) {
                        bp = createInfo.baseAmount.get(spellEffectInfo.effectIndex);
                    } else {
                        bp = spellEffectInfo.basePoints;
                    }

                    auraEff.baseAmount = bp;
                }

                // correct cast item guid if needed
                if (ObjectGuid.opNotEquals(castItemGUID, foundAura.getCastItemGuid())) {
                    foundAura.setCastItemGuid(castItemGUID);
                    foundAura.setCastItemId(createInfo.castItemId);
                    foundAura.setCastItemLevel(createInfo.castItemLevel);
                }

                // try to increase stack amount
                foundAura.modStackAmount(1, AuraRemoveMode.Default, createInfo.resetPeriodicTimer);

                return foundAura;
            }
        }

        return null;
    }

    public final double getHighestExclusiveSameEffectSpellGroupValue(AuraEffect aurEff, AuraType auraType, boolean checkMiscValue) {
        return getHighestExclusiveSameEffectSpellGroupValue(aurEff, auraType, checkMiscValue, 0);
    }

    public final double getHighestExclusiveSameEffectSpellGroupValue(AuraEffect aurEff, AuraType auraType) {
        return getHighestExclusiveSameEffectSpellGroupValue(aurEff, auraType, false, 0);
    }

    public final double getHighestExclusiveSameEffectSpellGroupValue(AuraEffect aurEff, AuraType auraType, boolean checkMiscValue, int miscValue) {
        double val = 0;
        var spellGroupList = global.getSpellMgr().getSpellSpellGroupMapBounds(aurEff.getSpellInfo().getFirstRankSpell().getId());

        for (var spellGroup : spellGroupList) {
            if (global.getSpellMgr().getSpellGroupStackRule(spellGroup) == SpellGroupStackRule.ExclusiveSameEffect) {
                var auraEffList = getAuraEffectsByType(auraType);

                for (var auraEffect : auraEffList) {
                    if (aurEff != auraEffect && (!checkMiscValue || auraEffect.getMiscValue() == miscValue) && global.getSpellMgr().isSpellMemberOfSpellGroup(auraEffect.getSpellInfo().getId(), spellGroup)) {
                        // absolute value only
                        if (Math.abs(val) < Math.abs(auraEffect.getAmount())) {
                            val = auraEffect.getAmount();
                        }
                    }
                }
            }
        }

        return val;
    }

    public final boolean isHighestExclusiveAura(Aura aura) {
        return isHighestExclusiveAura(aura, false);
    }

    public final boolean isHighestExclusiveAura(Aura aura, boolean removeOtherAuraApplications) {
        for (var aurEff : aura.getAuraEffects().entrySet()) {
            if (!isHighestExclusiveAuraEffect(aura.getSpellInfo(), aurEff.getValue().auraType, aurEff.getValue().amount, aura.getAuraEffects().keySet().ToHashSet(), removeOtherAuraApplications)) {
                return false;
            }
        }

        return true;
    }

    public final boolean isHighestExclusiveAuraEffect(SpellInfo spellInfo, AuraType auraType, double effectAmount, HashSet<Integer> auraEffectMask) {
        return isHighestExclusiveAuraEffect(spellInfo, auraType, effectAmount, auraEffectMask, false);
    }

    public final boolean isHighestExclusiveAuraEffect(SpellInfo spellInfo, AuraType auraType, double effectAmount, HashSet<Integer> auraEffectMask, boolean removeOtherAuraApplications) {
        var auras = getAuraEffectsByType(auraType);

        for (var existingAurEff : auras) {
            if (global.getSpellMgr().checkSpellGroupStackRules(spellInfo, existingAurEff.getSpellInfo()) == SpellGroupStackRule.ExclusiveHighest) {
                var diff = Math.abs(effectAmount) - Math.abs(existingAurEff.getAmount());
                var effMask = auraEffectMask.ToMask();
                var baseMask = existingAurEff.getBase().getAuraEffects().keySet().ToMask();

                if (diff == 0) {
                    for (var spellEff : spellInfo.getEffects()) {
                        diff += (long) ((effMask & (1 << spellEff.effectIndex)) >> spellEff.effectIndex) - (long) ((baseMask & (1 << spellEff.effectIndex)) >> spellEff.effectIndex);
                    }
                }

                if (diff > 0) {
                    var auraBase = existingAurEff.getBase();

                    // no removing of area auras from the original owner, as that completely cancels them
                    if (removeOtherAuraApplications && (!auraBase.isArea() || auraBase.getOwner() != this)) {
                        var aurApp = existingAurEff.getBase().getApplicationOfTarget(getGUID());

                        if (aurApp != null) {
                            removeAura(aurApp);
                        }
                    }
                } else if (diff < 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public final Aura getOwnedAura(int spellId) {
        return ownedAuras.query().hasSpellId(spellId).getResults().FirstOrDefault();
    }

    public final Aura getOwnedAura(int spellId, ObjectGuid casterGUID, ObjectGuid itemCasterGUID) {
        return getOwnedAura(spellId, casterGUID, itemCasterGUID, null);
    }

    public final Aura getOwnedAura(int spellId, ObjectGuid casterGUID) {
        return getOwnedAura(spellId, casterGUID, null, null);
    }

    public final Aura getOwnedAura(int spellId, ObjectGuid casterGUID, ObjectGuid itemCasterGUID, Aura except) {
        return ownedAuras.query().hasSpellId(spellId).hasCasterGuid(casterGUID).alsoMatches(aura ->
        {
            return (itemCasterGUID.isEmpty() || Objects.equals(aura.castItemGuid, itemCasterGUID)) && (except == null || except != aura);
        }).getResults().FirstOrDefault();
    }

    public final ArrayList<AuraEffect> getAuraEffectsByType(AuraType type) {
        return modAuras.get(type);
    }

    public final float getTotalAuraModifier(AuraType auraType) {
        return getTotalAuraModifier(auraType, aurEff -> true);
    }

    public final float getTotalAuraModifier(AuraType auraType, Predicate<AuraEffect> predicate) {
        HashMap<SpellGroup, Double> sameEffectSpellGroup = new HashMap<>();
        float modifier = 0;

        var mTotalAuraList = getAuraEffectsByType(auraType);

        for (var aurEff : mTotalAuraList) {
            if (predicate.test(aurEff)) {
                // Check if the Aura Effect has a the Same Effect Stack Rule and if so, use the highest amount of that SpellGroup
                // If the Aura Effect does not have this Stack rule, it returns false so we can add to the multiplier as usual
                if (!getWorldContext().getSpellManager().addSameEffectStackRuleSpellGroups(aurEff.getSpellInfo(), auraType, aurEff.getAmount(), sameEffectSpellGroup)) {
                    modifier += aurEff.getAmount();
                }
            }
        }

        // Add the highest of the Same Effect Stack Rule SpellGroups to the accumulator
        for (var pair : sameEffectSpellGroup.entrySet()) {
            modifier += pair.getValue();
        }

        return modifier;
    }

    public final float getTotalAuraMultiplier(AuraType auraType) {
        return getTotalAuraMultiplier(auraType, aurEff -> true);
    }

    public final float getTotalAuraMultiplier(AuraType auraType, tangible.Func1Param<AuraEffect, Boolean> predicate) {
        var mTotalAuraList = getAuraEffectsByType(auraType);

        if (mTotalAuraList.isEmpty()) {
            return 1.0f;
        }

        HashMap<SpellGroup, Double> sameEffectSpellGroup = new HashMap<SpellGroup, Double>();
        double multiplier = 1.0f;

        for (var aurEff : mTotalAuraList) {
            if (predicate.invoke(aurEff)) {
                // Check if the Aura Effect has a the Same Effect Stack Rule and if so, use the highest amount of that SpellGroup
                // If the Aura Effect does not have this Stack rule, it returns false so we can add to the multiplier as usual
                if (!global.getSpellMgr().addSameEffectStackRuleSpellGroups(aurEff.getSpellInfo(), auraType, aurEff.getAmount(), sameEffectSpellGroup)) {
                    tangible.RefObject<Double> tempRef_multiplier = new tangible.RefObject<Double>(multiplier);
                    MathUtil.AddPct(tempRef_multiplier, aurEff.getAmount());
                    multiplier = tempRef_multiplier.refArgValue;
                }
            }
        }

        // Add the highest of the Same Effect Stack Rule SpellGroups to the multiplier
        for (var pair : sameEffectSpellGroup.entrySet()) {
            tangible.RefObject<Double> tempRef_multiplier2 = new tangible.RefObject<Double>(multiplier);
            MathUtil.AddPct(tempRef_multiplier2, pair.getValue());
            multiplier = tempRef_multiplier2.refArgValue;
        }

        return multiplier;
    }

    public final float getMaxPositiveAuraModifier(AuraType auraType) {
        return getMaxPositiveAuraModifier(auraType, aurEff -> true);
    }

    public final float getMaxPositiveAuraModifier(AuraType auraType, tangible.Func1Param<AuraEffect, Boolean> predicate) {
        var mTotalAuraList = getAuraEffectsByType(auraType);

        if (mTotalAuraList.isEmpty()) {
            return 0;
        }

        double modifier = 0;

        for (var aurEff : mTotalAuraList) {
            if (predicate.invoke(aurEff)) {
                modifier = Math.max(modifier, aurEff.getAmount());
            }
        }

        return modifier;
    }

    public final float getMaxNegativeAuraModifier(AuraType auraType) {
        return getMaxNegativeAuraModifier(auraType, aurEff -> true);
    }

    public final float getMaxNegativeAuraModifier(AuraType auraType, Predicate<AuraEffect> predicate) {
        var mTotalAuraList = getAuraEffectsByType(auraType);

        if (mTotalAuraList.isEmpty()) {
            return 0;
        }

        float modifier = 0;

        for (var aurEff : mTotalAuraList) {
            if (predicate.test(aurEff)) {
                modifier = Math.min(modifier, aurEff.getAmount());
            }
        }

        return modifier;
    }

    public final float getTotalAuraModifierByMiscMask(AuraType auraType, SpellSchoolMask miscMask) {
        return getTotalAuraModifier(auraType, aurEff ->
        {
            if ((aurEff.miscValue & miscMask.value) != 0) {
                return true;
            }

            return false;
        });
    }

    public final float getTotalAuraMultiplierByMiscMask(AuraType auraType, int miscMask) {
        return getTotalAuraMultiplier(auraType, aurEff ->
        {
            if ((aurEff.miscValue & miscMask) != 0) {
                return true;
            }

            return false;
        });
    }

    public final float getMaxPositiveAuraModifierByMiscMask(AuraType auraType, int miscMask) {
        return getMaxPositiveAuraModifierByMiscMask(auraType, miscMask, null);
    }

    public final float getMaxPositiveAuraModifierByMiscMask(AuraType auraType, int miscMask, AuraEffect except) {
        return getMaxPositiveAuraModifier(auraType, aurEff ->
        {
            if (except != aurEff && (aurEff.miscValue & miscMask) != 0) {
                return true;
            }

            return false;
        });
    }

    public final float getMaxNegativeAuraModifierByMiscMask(AuraType auraType, int miscMask) {
        return getMaxNegativeAuraModifier(auraType, aurEff ->
        {
            if ((aurEff.miscValue & miscMask) != 0) {
                return true;
            }

            return false;
        });
    }

    public final float getTotalAuraModifierByMiscValue(AuraType auraType, int miscValue) {
        return getTotalAuraModifier(auraType, aurEff ->
        {
            if (aurEff.miscValue == miscValue) {
                return true;
            }

            return false;
        });
    }

    public final float getTotalAuraMultiplierByMiscValue(AuraType auraType, int miscValue) {
        return getTotalAuraMultiplier(auraType, aurEff ->
        {
            if (aurEff.miscValue == miscValue) {
                return true;
            }

            return false;
        });
    }

    public final float getMaxNegativeAuraModifierByMiscValue(AuraType auraType, int miscValue) {
        return getMaxNegativeAuraModifier(auraType, aurEff ->
        {
            if (aurEff.miscValue == miscValue) {
                return true;
            }

            return false;
        });
    }

    public final void _RegisterAuraEffect(AuraEffect aurEff, boolean apply) {
        if (apply) {
            modAuras.add(aurEff.getAuraType(), aurEff);
        } else {
            modAuras.remove(aurEff.getAuraType(), aurEff);
        }
    }

    public final float getTotalAuraModValue(UnitMod unitMod) {
        if (unitMod.getValue() >= UnitMod.End.getValue()) {
            Log.outError(LogFilter.unit, "attempt to access non-existing UnitMods in getTotalAuraModValue()!");

            return 0.0f;
        }

        var value = MathUtil.CalculatePct(getFlatModifierValue(unitMod, UnitModifierFlatType.base), Math.max(getFlatModifierValue(unitMod, UnitModifierFlatType.BasePCTExcludeCreate), -100.0f));
        value *= getPctModifierValue(unitMod, UnitModifierPctType.base);
        value += getFlatModifierValue(unitMod, UnitModifierFlatType.Total);
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Total);

        return value;
    }

    public final void setVisibleAura(AuraApplication aurApp) {
        synchronized (visibleAurasToUpdate) {
            visibleAuras.add(aurApp);
            visibleAurasToUpdate.add(aurApp);
            updateAuraForGroup();
        }
    }

    public final void removeVisibleAura(AuraApplication aurApp) {
        synchronized (visibleAurasToUpdate) {
            visibleAuras.remove(aurApp);
            visibleAurasToUpdate.remove(aurApp);
            updateAuraForGroup();
        }
    }

    public final boolean hasVisibleAura(AuraApplication aurApp) {
        return visibleAuras.contains(aurApp);
    }

    public final void setVisibleAuraUpdate(AuraApplication aurApp) {
        visibleAurasToUpdate.add(aurApp);
    }

    private int getDoTsByCaster(ObjectGuid casterGUID) {
        AuraType[] diseaseAuraTypes = {AuraType.PeriodicDamage, AuraType.PeriodicDamagePercent, AuraType.None};

        int dots = 0;

        for (var aura : diseaseAuraTypes) {
            if (aura == AuraType.NONE) {
                break;
            }

            var auras = getAuraEffectsByType(aura);

            for (var eff : auras) {
                // Get auras by caster
                if (Objects.equals(eff.getCasterGuid(), casterGUID)) {
                    ++dots;
                }
            }
        }

        return dots;
    }

    private void procSkillsAndReactives(boolean isVictim, Unit procTarget, EnumFlag<ProcFlag> typeMask, ProcFlagsHit hitMask, WeaponAttackType attType) {
        // Player is loaded now - do not allow passive spell casts to proc
        if (isPlayer() && toPlayer().getSession().getPlayerLoading()) {
            return;
        }

        // For melee/ranged based attack need update skills and set some Aura states if victim present
        if (typeMask.hasFlag(procFlags.MeleeBasedTriggerMask) && procTarget) {
            // If exist crit/parry/dodge/block need update aura state (for victim and attacker)
            if (hitMask.hasFlag(ProcFlagsHit.critical.getValue() | ProcFlagsHit.Parry.getValue().getValue() | ProcFlagsHit.Dodge.getValue().getValue().getValue() | ProcFlagsHit.Block.getValue().getValue().getValue())) {
                // for victim
                if (isVictim) {
                    // if victim and dodge attack
                    if (hitMask.hasFlag(ProcFlagsHit.Dodge)) {
                        // Update AURA_STATE on dodge
                        if (getUnitClass() != UnitClass.Rogue) // skip Rogue Riposte
                        {
                            modifyAuraState(AuraStateType.Defensive, true);
                            startReactiveTimer(ReactiveType.Defense);
                        }
                    }

                    // if victim and parry attack
                    if (hitMask.hasFlag(ProcFlagsHit.Parry)) {
                        modifyAuraState(AuraStateType.Defensive, true);
                        startReactiveTimer(ReactiveType.Defense);
                    }

                    // if and victim block attack
                    if (hitMask.hasFlag(ProcFlagsHit.Block)) {
                        modifyAuraState(AuraStateType.Defensive, true);
                        startReactiveTimer(ReactiveType.Defense);
                    }
                }
            }
        }
    }

    private void getProcAurasTriggeredOnEvent(ArrayList<Tuple<HashSet<Integer>, AuraApplication>> aurasTriggeringProc, ArrayList<AuraApplication> procAuras, ProcEventInfo eventInfo) {
        var now = gameTime.Now();


//		void processAuraApplication(AuraApplication aurApp)
//			{
//				var procEffectMask = aurApp.base.getProcEffectMask(aurApp, eventInfo, now);
//
//				if (procEffectMask.count != 0)
//				{
//					aurApp.base.prepareProcToTrigger(aurApp, eventInfo, now);
//					aurasTriggeringProc.add(Tuple.create(procEffectMask, aurApp));
//				}
//				else
//				{
//					if (aurApp.base.spellInfo.hasAttribute(SpellAttr0.ProcFailureBurnsCharge))
//					{
//						var procEntry = global.SpellMgr.getSpellProcEntry(aurApp.base.spellInfo);
//
//						if (procEntry != null)
//						{
//							aurApp.base.prepareProcChargeDrop(procEntry, eventInfo);
//							aurApp.base.consumeProcCharges(procEntry);
//						}
//					}
//
//					if (aurApp.base.spellInfo.hasAttribute(SpellAttr2.ProcCooldownOnFailure))
//					{
//						var procEntry = global.SpellMgr.getSpellProcEntry(aurApp.base.spellInfo);
//
//						if (procEntry != null)
//							aurApp.base.addProcCooldown(procEntry, now);
//					}
//				}
//			}

        // use provided list of auras which can proc
        if (procAuras != null) {
            for (var aurApp : procAuras) {
                processAuraApplication(aurApp);
            }
        }
        // or generate one on our own
        else {
            for (var aura : getAppliedAuras()) {
                processAuraApplication(aura);
            }
        }
    }

    private void triggerAurasProcOnEvent(ArrayList<AuraApplication> myProcAuras, ArrayList<AuraApplication> targetProcAuras, Unit actionTarget, EnumFlag<ProcFlag> typeMaskActor, EnumFlag<ProcFlag> typeMaskActionTarget, ProcFlagsSpellType spellTypeMask, ProcFlagsSpellPhase spellPhaseMask, ProcFlagsHit hitMask, Spell spell, DamageInfo damageInfo, HealInfo healInfo) {
        // prepare data for self trigger
        ProcEventInfo myProcEventInfo = new ProcEventInfo(this, actionTarget, actionTarget, typeMaskActor, spellTypeMask, spellPhaseMask, hitMask, spell, damageInfo, healInfo);
        ArrayList<Tuple<HashSet<Integer>, AuraApplication>> myAurasTriggeringProc = new ArrayList<Tuple<HashSet<Integer>, AuraApplication>>();

        if (typeMaskActor) {
            getProcAurasTriggeredOnEvent(myAurasTriggeringProc, myProcAuras, myProcEventInfo);

            // needed for example for Cobra Strikes, pet does the attack, but aura is on owner
            var modOwner = getSpellModOwner();

            if (modOwner) {
                if (modOwner != this && spell) {
                    ArrayList<AuraApplication> modAuras = new ArrayList<>();

                    for (var itr : modOwner.getAppliedAuras()) {
                        if (spell.appliedMods.contains(itr.base)) {
                            modAuras.add(itr);
                        }
                    }

                    modOwner.getProcAurasTriggeredOnEvent(myAurasTriggeringProc, modAuras, myProcEventInfo);
                }
            }
        }

        // prepare data for target trigger
        ProcEventInfo targetProcEventInfo = new ProcEventInfo(this, actionTarget, this, typeMaskActionTarget, spellTypeMask, spellPhaseMask, hitMask, spell, damageInfo, healInfo);
        ArrayList<Tuple<HashSet<Integer>, AuraApplication>> targetAurasTriggeringProc = new ArrayList<Tuple<HashSet<Integer>, AuraApplication>>();

        if (typeMaskActionTarget && actionTarget) {
            actionTarget.getProcAurasTriggeredOnEvent(targetAurasTriggeringProc, targetProcAuras, targetProcEventInfo);
        }

        triggerAurasProcOnEvent(myProcEventInfo, myAurasTriggeringProc);

        if (typeMaskActionTarget && actionTarget) {
            actionTarget.triggerAurasProcOnEvent(targetProcEventInfo, targetAurasTriggeringProc);
        }
    }

    private void triggerAurasProcOnEvent(ProcEventInfo eventInfo, ArrayList<Tuple<HashSet<Integer>, AuraApplication>> aurasTriggeringProc) {
        var triggeringSpell = eventInfo.getProcSpell();
        var disableProcs = triggeringSpell && triggeringSpell.isProcDisabled();

        if (disableProcs) {
            setCantProc(true);
        }


        for (var(procEffectMask, aurApp) : aurasTriggeringProc) {
            if (aurApp.removeMode != 0) {
                continue;
            }

            aurApp.base.triggerProcOnEvent(procEffectMask, aurApp, eventInfo);
        }

        if (disableProcs) {
            setCantProc(false);
        }
    }

    private void setCantProc(boolean apply) {
        if (apply) {
            setProcDeep(getProcDeep() + 1);
        } else {
            setProcDeep(getProcDeep() - 1);
        }
    }

    private void sendHealSpellLog(HealInfo healInfo) {
        sendHealSpellLog(healInfo, false);
    }

    private void sendHealSpellLog(HealInfo healInfo, boolean critical) {
        SpellHealLog spellHealLog = new SpellHealLog();

        spellHealLog.targetGUID = healInfo.getTarget().getGUID();
        spellHealLog.casterGUID = healInfo.getHealer().getGUID();
        spellHealLog.spellID = healInfo.getSpellInfo().getId();
        spellHealLog.health = (int) healInfo.getHeal();
        spellHealLog.originalHeal = (int) healInfo.getOriginalHeal();
        spellHealLog.overHeal = (int) (healInfo.getHeal() - healInfo.getEffectiveHeal());
        spellHealLog.absorbed = (int) healInfo.getAbsorb();
        spellHealLog.crit = critical;

        spellHealLog.logData.initialize(healInfo.getTarget());
        sendCombatLogMessage(spellHealLog);
    }

    private void sendSpellDamageResist(Unit target, int spellId) {
        ProcResist procResist = new ProcResist();
        procResist.caster = getGUID();
        procResist.spellID = spellId;
        procResist.target = target.getGUID();
        sendMessageToSet(procResist, true);
    }

    private void clearDiminishings() {
        for (var i = 0; i < DiminishingGroup.max.getValue(); ++i) {
            _diminishing[i].clear();
        }
    }

    private AuraApplication getAuraApplicationOfRankedSpell(int spellId) {
        var rankSpell = global.getSpellMgr().getFirstSpellInChain(spellId);

        while (rankSpell != 0) {
            var aurApp = getAuraApplication(rankSpell) == null ? null : getAuraApplication(rankSpell).FirstOrDefault();

            if (aurApp != null) {
                return aurApp;
            }

            rankSpell = global.getSpellMgr().getNextSpellInChain(rankSpell);
        }

        return null;
    }

    private boolean isInterruptFlagIgnoredForSpell(SpellAuraInterruptFlags flag, Unit unit, SpellInfo auraSpellInfo, SpellInfo interruptSource) {
        switch (flag) {
            case Moving:
                return unit.canCastSpellWhileMoving(auraSpellInfo);
            case Action:
            case ActionDelayed:
                if (interruptSource != null) {
                    if (interruptSource.hasAttribute(SpellAttr1.AllowWhileStealthed) && auraSpellInfo.getDispel() == DispelType.stealth) {
                        return true;
                    }

                    if (interruptSource.hasAttribute(SpellAttr2.AllowWhileInvisible) && auraSpellInfo.getDispel() == DispelType.invisibility) {
                        return true;
                    }
                }

                break;
            default:
                break;
        }

        return false;
    }

    private boolean isInterruptFlagIgnoredForSpell(SpellAuraInterruptFlags2 flag, Unit unit, SpellInfo auraSpellInfo, SpellInfo interruptSource) {
        return false;
    }

    private void removeAreaAurasDueToLeaveWorld() {
        // make sure that all area auras not applied on self are removed
        for (var pair : ownedAuras.getAuras()) {
            var appMap = pair.getApplicationMap();

            for (var aurApp : appMap.values().ToList()) {
                var target = aurApp.target;

                if (target == this) {
                    continue;
                }

                target.removeAura(aurApp);
                // things linked on aura remove may apply new area aura - so start from the beginning
            }
        }

        // remove area auras owned by others
        appliedAuras.getAuraApplications().CallOnMatch((pair) -> pair.base.owner != this, (pair) -> removeAura(pair));
    }

    private SpellSchool getSpellSchoolByAuraGroup(UnitMod unitMod) {
        var school = SpellSchool.NORMAL;

        switch (unitMod) {
            case ResistanceHoly:
                school = SpellSchool.Holy;

                break;
            case ResistanceFire:
                school = SpellSchool.Fire;

                break;
            case ResistanceNature:
                school = SpellSchool.Nature;

                break;
            case ResistanceFrost:
                school = SpellSchool.Frost;

                break;
            case ResistanceShadow:
                school = SpellSchool.Shadow;

                break;
            case ResistanceArcane:
                school = SpellSchool.Arcane;

                break;
        }

        return school;
    }

    private void _RemoveNoStackAurasDueToAura(Aura aura) {
        var spellProto = aura.getSpellInfo();

        // passive spell special case (only non stackable with ranks)
        if (spellProto.isPassiveStackableWithRanks()) {
            return;
        }

        if (!isHighestExclusiveAura(aura)) {
            aura.remove();

            return;
        }

        appliedAuras.getAuraApplications().CallOnMatch((app) -> !aura.canStackWith(app.base), (app) -> removeAura(app, AuraRemoveMode.Default));
    }

    private double getMaxPositiveAuraModifierByMiscValue(AuraType auraType, int miscValue) {
        return getMaxPositiveAuraModifier(auraType, aurEff ->
        {
            if (aurEff.miscValue == miscValue) {
                return true;
            }

            return false;
        });
    }

    private void updateAuraForGroup() {
        var player = toPlayer();

        if (player != null) {
            if (player.getGroup() != null) {
                player.setGroupUpdateFlag(GroupUpdateFlags.auras);
            }
        } else if (isPet()) {
            var pet = getAsPet();

            if (pet.isControlled()) {
                pet.setGroupUpdateFlag(GroupUpdatePetFlags.auras);
            }
        }
    }

    public final long getHealth() {
        return getUnitData().health;
    }

    public final void setHealth(float val) {
        setHealth((long) val);
    }

    public final void setHealth(double val) {
        setHealth((long) val);
    }

    public final void setHealth(int val) {
        setHealth((long) val);
    }

    public final void setHealth(int val) {
        setHealth((long) val);
    }

    public final void setHealth(long val) {
        synchronized (healthLock) {
            if (getDeathState() == deathState.JustDied || getDeathState() == deathState.Corpse) {
                val = 0;
            } else if (isTypeId(TypeId.PLAYER) && getDeathState() == deathState.Dead) {
                val = 1;
            } else {
                var maxHealth = getMaxHealth();

                if (maxHealth < val) {
                    val = maxHealth;
                }
            }

            var oldVal = getHealth();
            setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().health), val);

            triggerOnHealthChangeAuras(oldVal, val);

            // group update
            var player = toPlayer();

            if (player) {
                if (player.getGroup()) {
                    player.setGroupUpdateFlag(GroupUpdateFlags.CurHp);
                }
            } else if (isPet()) {
                var pet = toCreature().getAsPet();

                if (pet.isControlled()) {
                    pet.setGroupUpdateFlag(GroupUpdatePetFlags.CurHp);
                }
            }
        }
    }

    public final long getMaxHealth() {
        return getUnitData().maxHealth;
    }

    public final void setMaxHealth(double val) {
        setMaxHealth((long) val);
    }

    public final void setMaxHealth(long val) {
        if (val == 0) {
            val = 1;
        }

        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().maxHealth), val);
        var health = getHealth();

        // group update
        if (isTypeId(TypeId.PLAYER)) {
            if (toPlayer().getGroup()) {
                toPlayer().setGroupUpdateFlag(GroupUpdateFlags.MaxHp);
            }
        } else if (isPet()) {
            var pet = toCreature().getAsPet();

            if (pet.isControlled()) {
                pet.setGroupUpdateFlag(GroupUpdatePetFlags.MaxHp);
            }
        }

        if (val < health) {
            setHealth(val);
        }
    }

    public final float getHealthPct() {
        return getMaxHealth() != 0 ? 100.0f * getHealth() / getMaxHealth() : 0.0f;
    }

    public final boolean isFullHealth() {
        return getHealth() == getMaxHealth();
    }

    //Powers
    public final Power getDisplayPowerType() {
        return powerType.forValue((byte) ((byte) getUnitData().displayPower));
    }

    public final void handleStatFlatModifier(UnitMod unitMod, UnitModifierFlatType modifierType, double amount, boolean apply) {
        if (unitMod.getValue() >= UnitMod.End.getValue() || modifierType.getValue() >= UnitModifierFlatType.End.getValue()) {
            Log.outError(LogFilter.unit, "ERROR in handleStatFlatModifier(): non-existing UnitMods or wrong UnitModifierFlatType!");

            return;
        }

        if (amount == 0) {
            return;
        }

        switch (modifierType) {
            case Base:
            case BasePCTExcludeCreate:
            case Total:
                getAuraFlatModifiersGroup()[unitMod.getValue()][modifierType.getValue()] += apply ? amount : -amount;

                break;
            default:
                break;
        }

        updateUnitMod(unitMod);
    }

    public final void applyStatPctModifier(UnitMod unitMod, UnitModifierPctType modifierType, double pct) {
        if (unitMod.getValue() >= UnitMod.End.getValue() || modifierType.getValue() >= UnitModifierPctType.End.getValue()) {
            Log.outError(LogFilter.unit, "ERROR in applyStatPctModifier(): non-existing UnitMods or wrong UnitModifierPctType!");

            return;
        }

        if (pct == 0) {
            return;
        }

        switch (modifierType) {
            case Base:
            case Total:
                tangible.RefObject<Double> tempRef_Object = new tangible.RefObject<Double>(getAuraPctModifiersGroup()[unitMod.getValue()][modifierType.getValue()]);
                MathUtil.AddPct(tempRef_Object, pct);
                getAuraPctModifiersGroup()[unitMod.getValue()][modifierType.getValue()] = tempRef_Object.refArgValue;

                break;
            default:
                break;
        }

        updateUnitMod(unitMod);
    }

    public final void setStatFlatModifier(UnitMod unitMod, UnitModifierFlatType modifierType, double val) {
        if (getAuraFlatModifiersGroup()[unitMod.ordinal()][modifierType.ordinal()] == val) {
            return;
        }

        getAuraFlatModifiersGroup()[unitMod.ordinal()][modifierType.ordinal()] = val;
        updateUnitMod(unitMod);
    }

    // returns negative amount on power reduction

    public final void setStatPctModifier(UnitMod unitMod, UnitModifierPctType modifierType, double val) {
        if (getAuraPctModifiersGroup()[unitMod.getValue()][modifierType.getValue()] == val) {
            return;
        }

        getAuraPctModifiersGroup()[unitMod.getValue()][modifierType.getValue()] = val;
        updateUnitMod(unitMod);
    }

    public final double getFlatModifierValue(UnitMod unitMod, UnitModifierFlatType modifierType) {
        if (unitMod.getValue() >= UnitMod.End.getValue() || modifierType.getValue() >= UnitModifierFlatType.End.getValue()) {
            Log.outError(LogFilter.unit, "attempt to access non-existing modifier value from UnitMods!");

            return 0.0f;
        }

        return getAuraFlatModifiersGroup()[unitMod.getValue()][modifierType.getValue()];
    }

    public final double getPctModifierValue(UnitMod unitMod, UnitModifierPctType modifierType) {
        if (unitMod.getValue() >= UnitMod.End.getValue() || modifierType.getValue() >= UnitModifierPctType.End.getValue()) {
            Log.outError(LogFilter.unit, "attempt to access non-existing modifier value from UnitMods!");

            return 0.0f;
        }

        return getAuraPctModifiersGroup()[unitMod.getValue()][modifierType.getValue()];
    }

    public final int modifyPower(Power power, double dVal) {
        return modifyPower(power, dVal, true);
    }

    public final int modifyPower(Power power, double dVal, boolean withPowerUpdate) {
        return modifyPower(power, (int) dVal, withPowerUpdate);
    }

    public final int modifyPower(Power power, int dVal) {
        return modifyPower(power, dVal, true);
    }

    public final int modifyPower(Power power, int dVal, boolean withPowerUpdate) {
        var gain = 0;

        if (dVal == 0) {
            return 0;
        }

        if (dVal > 0) {
            dVal *= (int) getTotalAuraMultiplierByMiscValue(AuraType.ModPowerGainPct, power.getValue());
        }

        var curPower = getPower(power);

        var val = (dVal + curPower);

        if (val <= getMinPower(power)) {
            setPower(power, getMinPower(power), withPowerUpdate);

            return -curPower;
        }

        var maxPower = getMaxPower(power);

        if (val < maxPower) {
            setPower(power, val, withPowerUpdate);
            gain = val - curPower;
        } else if (curPower != maxPower) {
            setPower(power, maxPower, withPowerUpdate);
            gain = maxPower - curPower;
        }

        return gain;
    }

    public final void updateStatBuffMod(Stats stat) {
        double modPos = 0.0f;
        double modNeg = 0.0f;
        double factor = 0.0f;

        var unitMod = UnitMod.StatStart + stat.getValue();

        // includes value from items and enchantments
        var modValue = getFlatModifierValue(unitMod, UnitModifierFlatType.base);

        if (modValue > 0.0f) {
            modPos += modValue;
        } else {
            modNeg += modValue;
        }

        if (isGuardian()) {
            modValue = ((Guardian) this).GetBonusStatFromOwner(stat);

            if (modValue > 0.0f) {
                modPos += modValue;
            } else {
                modNeg += modValue;
            }
        }

        // SPELL_AURA_MOD_STAT_BONUS_PCT only affects BASE_VALUE
        modPos = MathUtil.CalculatePct(modPos, Math.max(getFlatModifierValue(unitMod, UnitModifierFlatType.BasePCTExcludeCreate), -100.0f));
        modNeg = MathUtil.CalculatePct(modNeg, Math.max(getFlatModifierValue(unitMod, UnitModifierFlatType.BasePCTExcludeCreate), -100.0f));

        modPos += getTotalAuraModifier(AuraType.ModStat, aurEff ->
        {
            if ((aurEff.miscValue < 0 || aurEff.miscValue == stat.getValue()) && aurEff.amount > 0) {
                return true;
            }

            return false;
        });

        modNeg += getTotalAuraModifier(AuraType.ModStat, aurEff ->
        {
            if ((aurEff.miscValue < 0 || aurEff.miscValue == stat.getValue()) && aurEff.amount < 0) {
                return true;
            }

            return false;
        });

        factor = getTotalAuraMultiplier(AuraType.ModPercentStat, aurEff ->
        {
            if (aurEff.miscValue == -1 || aurEff.miscValue == stat.getValue()) {
                return true;
            }

            return false;
        });

        factor *= getTotalAuraMultiplier(AuraType.ModTotalStatPercentage, aurEff ->
        {
            if (aurEff.miscValue == -1 || aurEff.miscValue == stat.getValue()) {
                return true;
            }

            return false;
        });

        modPos *= factor;
        modNeg *= factor;

        _floatStatPosBuff[stat.getValue()] = modPos;
        _floatStatNegBuff[stat.getValue()] = modNeg;

        updateStatBuffModForClient(stat);
    }

    public boolean updateStats(Stats stat) {
        return false;
    }

    public boolean updateAllStats() {
        return false;
    }

    public void updateResistances(SpellSchool school) {
        if (school.getValue() > SpellSchool.NORMAL.getValue()) {
            var unitMod = UnitMod.ResistanceStart + school.getValue();
            var value = MathUtil.CalculatePct(getFlatModifierValue(unitMod, UnitModifierFlatType.base), Math.max(getFlatModifierValue(unitMod, UnitModifierFlatType.BasePCTExcludeCreate), -100.0f));
            value *= getPctModifierValue(unitMod, UnitModifierPctType.base);

            var baseValue = value;

            value += getFlatModifierValue(unitMod, UnitModifierFlatType.Total);
            value *= getPctModifierValue(unitMod, UnitModifierPctType.Total);

            setResistance(school, (int) value);
            setBonusResistanceMod(school, (int) (value - baseValue));
        } else {
            updateArmor();
        }
    }

    public void updateArmor() {
    }

    public void updateMaxHealth() {
    }

    public void updateMaxPower(Power power) {
    }

    public void updateAttackPowerAndDamage() {
        updateAttackPowerAndDamage(false);
    }

    public void updateAttackPowerAndDamage(boolean ranged) {
    }

    public void updateDamagePhysical(WeaponAttackType attType) {
        double minDamage;
        tangible.OutObject<Double> tempOut_minDamage = new tangible.OutObject<Double>();
        double maxDamage;
        tangible.OutObject<Double> tempOut_maxDamage = new tangible.OutObject<Double>();
        calculateMinMaxDamage(attType, false, true, tempOut_minDamage, tempOut_maxDamage);
        maxDamage = tempOut_maxDamage.outArgValue;
        minDamage = tempOut_minDamage.outArgValue;

        switch (attType) {
            case BaseAttack:
            default:
                setUpdateFieldStatValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().minDamage), (float) minDamage);
                setUpdateFieldStatValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().maxDamage), (float) maxDamage);

                break;
            case OffAttack:
                setUpdateFieldStatValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().minOffHandDamage), (float) minDamage);
                setUpdateFieldStatValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().maxOffHandDamage), (float) maxDamage);

                break;
            case RangedAttack:
                setUpdateFieldStatValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().minRangedDamage), (float) minDamage);
                setUpdateFieldStatValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().maxRangedDamage), (float) maxDamage);

                break;
        }
    }

    public void calculateMinMaxDamage(WeaponAttackType attType, boolean normalized, boolean addTotalPct, tangible.OutObject<Double> minDamage, tangible.OutObject<Double> maxDamage) {
        minDamage.outArgValue = 0f;
        maxDamage.outArgValue = 0f;
    }

    public final void updateAllResistances() {
        for (var i = SpellSchool.NORMAL; i.getValue() < SpellSchool.max.getValue(); ++i) {
            updateResistances(i);
        }
    }

    //Stats
    public final float getStat(Stats stat) {
        if (unitData.getStats().size() > stat.ordinal()) {
            return getUnitData().getStats().get(stat.ordinal());
        } else {
            return 0;
        }
    }

    public final void setStat(Stats stat, int val) {
        unitData.getStats().set(stat.ordinal(), val);
    }

    public final int getCreateMana() {
        return unitData.getBaseMana();
    }

    public final void setCreateMana(int val) {
        unitData.setBaseMana(val);
    }

    public final int getArmor() {
        return getResistance(SpellSchool.NORMAL);
    }

    public final void setArmor(int val, int bonusVal) {
        setResistance(SpellSchool.NORMAL, val);
        setBonusResistanceMod(SpellSchool.NORMAL, bonusVal);
    }

    public final float getCreateStat(Stats stat) {
        return createStats[stat.ordinal()];
    }

    public final void setCreateStat(Stats stat, float val) {
        createStats[stat.ordinal()] = val;
    }

    public final float getPosStat(Stats stat) {
        return unitData.getStatPosBuff().get(stat.ordinal());
    }

    public final float getNegStat(Stats stat) {
        return unitData.getStatNegBuff().get(stat.ordinal());
    }

    public final int getResistance(SpellSchool school) {
        return unitData.getResistances().get(school.ordinal());
    }


    public final int getResistance(SpellSchoolMask mask) {
        Integer resist = null;
        for (SpellSchool school : SpellSchool.values()) {
            var schoolResistance = getResistance(school);
            if ((mask.value & (1 << school.ordinal())) != 0 && (resist == null || resist > schoolResistance)) {
                resist = schoolResistance;
            }
        }
        // resist value will never be negative here
        return resist != null ? resist : 0;
    }

    public final void setResistance(SpellSchool school, int val) {
        unitData.getResistances().set(school.ordinal(), val);
    }


    public final void initStatBuffMods() {

        for (Stats stats : Stats.values()) {
            floatStatPosBuff[stats.ordinal()] = 0.0f;
            floatStatNegBuff[stats.ordinal()] = 0.0f;
            updateStatBuffModForClient(stats);
        }
    }

    public final boolean canModifyStats() {
        return canModifyStats;
    }

    public final void setCanModifyStats(boolean modifyStats) {
        canModifyStats = modifyStats;
    }

    public final double getTotalStatValue(Stats stat) {
        var unitMod = UnitMod.StatStart + stat.getValue();

        var value = MathUtil.CalculatePct(getFlatModifierValue(unitMod, UnitModifierFlatType.base), Math.max(getFlatModifierValue(unitMod, UnitModifierFlatType.BasePCTExcludeCreate), -100.0f));
        value += getCreateStat(stat);
        value *= getPctModifierValue(unitMod, UnitModifierPctType.base);
        value += getFlatModifierValue(unitMod, UnitModifierFlatType.Total);
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Total);

        return value;
    }

    //Health
    public final int getCreateHealth() {
        return getUnitData().baseHealth;
    }

    public final void setCreateHealth(int val) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().baseHealth), val);
    }

    public final void setFullHealth() {
        setHealth(getMaxHealth());
    }

    public final boolean healthBelowPct(double pct) {
        return getHealth() < countPctFromMaxHealth(pct);
    }

    public final boolean healthBelowPct(int pct) {
        return getHealth() < countPctFromMaxHealth(pct);
    }

    public final boolean healthBelowPctDamaged(int pct, double damage) {
        return getHealth() - damage < countPctFromMaxHealth(pct);
    }

    public final boolean healthBelowPctDamaged(double pct, double damage) {
        return getHealth() - damage < countPctFromMaxHealth(pct);
    }

    public final boolean healthAbovePct(double pct) {
        return getHealth() > countPctFromMaxHealth(pct);
    }

    public final boolean healthAbovePct(int pct) {
        return getHealth() > countPctFromMaxHealth(pct);
    }

    public final long countPctFromMaxHealth(double pct) {
        return MathUtil.CalculatePct(getMaxHealth(), pct);
    }

    public final long countPctFromMaxHealth(int pct) {
        return MathUtil.CalculatePct(getMaxHealth(), pct);
    }

    public final long countPctFromCurHealth(double pct) {
        return MathUtil.CalculatePct(getHealth(), pct);
    }

    public final int countPctFromMaxPower(Power power, double pct) {
        return MathUtil.CalculatePct(getMaxPower(power), pct);
    }

    public float getHealthMultiplierForTarget(WorldObject target) {
        return 1.0f;
    }

    public float getDamageMultiplierForTarget(WorldObject target) {
        return 1.0f;
    }

    public float getArmorMultiplierForTarget(WorldObject target) {
        return 1.0f;
    }

    public final void setPowerType(Power powerType) {
        setPowerType(powerType, true);
    }

    public final void setPowerType(Power powerType, boolean sendUpdate) {
        if (getDisplayPowerType() == powerType) {
            return;
        }

        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().displayPower), (byte) powerType.getValue());

        if (!sendUpdate) {
            return;
        }

        var thisPlayer = toPlayer();

        if (thisPlayer != null) {
            if (thisPlayer.getGroup()) {
                thisPlayer.setGroupUpdateFlag(GroupUpdateFlags.powerType);
            }
        }
		/*else if (isPet()) TODO 6.x
		{
		    Pet pet = ToCreature().ToPet();
		    if (pet.isControlled())
		        pet.setGroupUpdateFlag(GROUP_UPDATE_FLAG_PET_POWER_TYPE);
		}*/

        // Update max power
        updateMaxPower(powerType);

        // Update current power
        switch (powerType) {
            case Mana: // Keep the same (druid form switching...)
            case Energy:
                break;
            case Rage: // Reset to zero
                setPower(powerType.Rage, 0);

                break;
            case Focus: // Make it full
                setFullPower(powerType);

                break;
            default:
                break;
        }
    }

    public final void setOverrideDisplayPowerId(int powerDisplayId) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().overrideDisplayPowerID), powerDisplayId);
    }

    public final void setMaxPower(Power powerType, int val) {
        var powerIndex = getPowerIndex(powerType);

        if (powerIndex == powerType.max.getValue() || powerIndex >= powerType.MaxPerClass.getValue()) {
            return;
        }

        var cur_power = getPower(powerType);

        setUpdateFieldValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().maxPower, (int) powerIndex), (int) val);

        // group update
        if (isTypeId(TypeId.PLAYER)) {
            if (toPlayer().getGroup()) {
                toPlayer().setGroupUpdateFlag(GroupUpdateFlags.maxPower);
            }
        }
		/*else if (isPet()) TODO 6.x
		{
		    Pet pet = ToCreature().ToPet();
		    if (pet.isControlled())
		        pet.setGroupUpdateFlag(GROUP_UPDATE_FLAG_PET_MAX_POWER);
		}*/

        if (val < cur_power) {
            setPower(powerType, val);
        }
    }

    public final void setPower(Power powerType, float val, boolean withPowerUpdate) {
        setPower(powerType, val, withPowerUpdate, false);
    }

    public final void setPower(Power powerType, float val) {
        setPower(powerType, val, true, false);
    }

    public final void setPower(Power powerType, float val, boolean withPowerUpdate, boolean isRegen) {
        setPower(powerType, (int) val, withPowerUpdate, isRegen);
    }

    public final void setPower(Power powerType, int val, boolean withPowerUpdate) {
        setPower(powerType, val, withPowerUpdate, false);
    }

    public final void setPower(Power powerType, int val) {
        setPower(powerType, val, true, false);
    }

    public final void setPower(Power powerType, int val, boolean withPowerUpdate, boolean isRegen) {
        var powerIndex = getPowerIndex(powerType);

        if (powerIndex == powerType.max.getValue() || powerIndex >= powerType.MaxPerClass.getValue()) {
            return;
        }

        var maxPower = getMaxPower(powerType);

        if (maxPower < val) {
            val = maxPower;
        }

        var oldPower = getUnitData().power.get((int) powerIndex);

        Player player;
        tangible.OutObject<Player> tempOut_player = new tangible.OutObject<Player>();
        if (isPlayer(tempOut_player)) {
            player = tempOut_player.outArgValue;
            var newVal = val;
            tangible.RefObject<Integer> tempRef_val = new tangible.RefObject<Integer>(val);
            global.getScriptMgr().<IPlayerOnModifyPower>ForEach(player.getUnitClass(), p -> p.OnModifyPower(player, powerType, oldPower, tempRef_val, isRegen));
            val = tempRef_val.refArgValue;
            val = newVal;
        } else {
            player = tempOut_player.outArgValue;
        }


        setUpdateFieldValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().power, (int) powerIndex), val);

        if (isInWorld() && withPowerUpdate) {
            PowerUpdate packet = new PowerUpdate();
            packet.guid = getGUID();
            packet.powers.add(new PowerUpdatePower(val, (byte) powerType.getValue()));
            sendMessageToSet(packet, isTypeId(TypeId.PLAYER));
        }

        triggerOnPowerChangeAuras(powerType, oldPower, val);

        // group update
        if (isTypeId(TypeId.PLAYER)) {
            if (player.getGroup()) {
                player.setGroupUpdateFlag(GroupUpdateFlags.CurPower);
            }
        }
		/*else if (isPet()) TODO 6.x
		{
		    Pet pet = ToCreature().ToPet();
		    if (pet.isControlled())
		        pet.setGroupUpdateFlag(GROUP_UPDATE_FLAG_PET_CUR_POWER);
		}*/

        if (isPlayer()) {
            global.getScriptMgr().<IPlayerOnAfterModifyPower>ForEach(player.getUnitClass(), p -> p.OnAfterModifyPower(player, powerType, oldPower, val, isRegen));
        }
    }

    public final void setFullPower(Power powerType) {
        setPower(powerType, getMaxPower(powerType));
    }

    public final int getPower(Power powerType) {
        var powerIndex = getPowerIndex(powerType);

        if (powerIndex == powerType.max.getValue() || powerIndex >= powerType.MaxPerClass.getValue()) {
            return 0;
        }

        return getUnitData().power.get((int) powerIndex);
    }

    public final int getMaxPower(Power powerType) {
        var powerIndex = getPowerIndex(powerType);

        if (powerIndex == Power.MAX_POWERS.ordinal()
                || powerIndex >= MAX_POWERS_PER_CLASS) {
            return 0;
        }

        return (int) getUnitData().MaxPower[(int) powerIndex];
    }

    public final int getCreatePowerValue(Power powerType) {
        if (powerType == powerType.mana) {
            return (int) getCreateMana();
        }

        var powerTypeEntry = global.getDB2Mgr().GetPowerTypeEntry(powerType);

        if (powerTypeEntry != null) {
            return powerTypeEntry.MaxBasePower;
        }

        return 0;
    }

    public int getPowerIndex(Power power) {
        return getWorldContext().getDbcObjectManager().getPowerIndexByClass(power, getUnitClass());
    }

    public final float getPowerPct(Power power) {
        return getMaxPower(power) != 0 ? 100.0f * getPower(power) / getMaxPower(power) : 0.0f;
    }

    public final boolean canApplyResilience() {
        return !isVehicle() && getOwnerGUID().isPlayer();
    }

    public final double calculateAOEAvoidance(double damage, int schoolMask, ObjectGuid casterGuid) {
        damage = (damage * getTotalAuraMultiplierByMiscMask(AuraType.ModAoeDamageAvoidance, schoolMask));

        if (casterGuid.isAnyTypeCreature()) {
            damage = (damage * getTotalAuraMultiplierByMiscMask(AuraType.ModCreatureAoeDamageAvoidance, schoolMask));
        }

        return damage;
    }

    public final void setAttackPower(int attackPower) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().attackPower), attackPower);
    }

    public final void setAttackPowerModPos(int attackPowerMod) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().attackPowerModPos), attackPowerMod);
    }

    public final void setAttackPowerModNeg(int attackPowerMod) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().attackPowerModNeg), attackPowerMod);
    }

    public final void setAttackPowerMultiplier(float attackPowerMult) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().attackPowerMultiplier), attackPowerMult);
    }

    public final void setRangedAttackPower(int attackPower) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().rangedAttackPower), attackPower);
    }

    public final void setRangedAttackPowerModPos(int attackPowerMod) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().rangedAttackPowerModPos), attackPowerMod);
    }

    public final void setRangedAttackPowerModNeg(int attackPowerMod) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().rangedAttackPowerModNeg), attackPowerMod);
    }

    public final void setRangedAttackPowerMultiplier(float attackPowerMult) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().rangedAttackPowerMultiplier), attackPowerMult);
    }

    public final void setMainHandWeaponAttackPower(int attackPower) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().mainHandWeaponAttackPower), attackPower);
    }

    public final void setOffHandWeaponAttackPower(int attackPower) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().offHandWeaponAttackPower), attackPower);
    }

    public final void setRangedWeaponAttackPower(int attackPower) {
        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().rangedWeaponAttackPower), attackPower);
    }

    //Chances
    @Override
    public double meleeSpellMissChance(Unit victim, WeaponAttackType attType, SpellInfo spellInfo) {
        if (spellInfo != null && spellInfo.hasAttribute(SpellAttr7.NoAttackMiss)) {
            return 0.0f;
        }

        //calculate miss chance
        double missChance = victim.getUnitMissChance();

        // melee attacks while dual wielding have +19% chance to miss
        if (spellInfo == null && haveOffhandWeapon() && !isInFeralForm() && !hasAuraType(AuraType.IgnoreDualWieldHitPenalty)) {
            missChance += 19.0f;
        }

        // Spellmod from SpellModOp.HitChance
        double resistMissChance = 100.0f;

        if (spellInfo != null) {
            var modOwner = getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Double> tempRef_resistMissChance = new tangible.RefObject<Double>(resistMissChance);
                modOwner.applySpellMod(spellInfo, SpellModOp.HitChance, tempRef_resistMissChance);
                resistMissChance = tempRef_resistMissChance.refArgValue;
            }
        }

        missChance += resistMissChance - 100.0f;

        if (attType == WeaponAttackType.RangedAttack) {
            missChance -= getModRangedHitChance();
        } else {
            missChance -= getModMeleeHitChance();
        }

        // miss chance from auras after calculating skill based miss
        missChance -= getTotalAuraModifier(AuraType.ModHitChance);

        if (attType == WeaponAttackType.RangedAttack) {
            missChance -= victim.getTotalAuraModifier(AuraType.ModAttackerRangedHitChance);
        } else {
            missChance -= victim.getTotalAuraModifier(AuraType.ModAttackerMeleeHitChance);
        }

        return Math.max(missChance, 0f);
    }

    public final double getUnitCriticalChanceAgainst(WeaponAttackType attackType, Unit victim) {
        var chance = getUnitCriticalChanceDone(attackType);

        return victim.getUnitCriticalChanceTaken(this, attackType, chance);
    }

    public final int getMechanicResistChance(SpellInfo spellInfo) {
        if (spellInfo == null) {
            return 0;
        }

        double resistMech = 0;

        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (!spellEffectInfo.isEffect()) {
                break;
            }

            var effect_mech = spellInfo.getEffectMechanic(spellEffectInfo.effectIndex).getValue();

            if (effect_mech != 0) {
                var temp = getTotalAuraModifierByMiscValue(AuraType.ModMechanicResistance, effect_mech);

                if (resistMech < temp) {
                    resistMech = temp;
                }
            }
        }

        return Math.max((int) resistMech, 0);
    }

    public final void applyModManaCostMultiplier(float manaCostMultiplier, boolean apply) {
        applyModUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().manaCostMultiplier), manaCostMultiplier, apply);
    }

    public final void applyModManaCostModifier(SpellSchool school, int mod, boolean apply) {

        applyModUpdateFieldValue(ref getValues().modifyValue(getUnitData()).modifyValue(getUnitData().manaCostModifier, school.getValue()), mod, apply);
    }

    private void updateUnitMod(UnitMod unitMod) {
        if (!canModifyStats()) {
            return;
        }

        switch (unitMod) {
            case StatStart:
            case STAT_STRENGTH:
            case STAT_AGILITY:
            case STAT_STAMINA:
            case STAT_INTELLECT:
                updateStats(getStatByAuraGroup(unitMod));

                break;
            case ResistanceStart:
            case PowerEnd:
            case ARMOR:
                updateArmor();

                break;
            case StatEnd:
            case HEALTH:
                updateMaxHealth();

                break;
            case PowerStart:
            case Mana:
            case Rage:
            case Focus:
            case Energy:
            case ComboPoints:
            case Runes:
            case RunicPower:
            case SoulShards:
            case LunarPower:
            case HolyPower:
            case Alternate:
            case Maelstrom:
            case Chi:
            case Insanity:
            case BurningEmbers:
            case DemonicFury:
            case ArcaneCharges:
            case Fury:
            case Pain:
                updateMaxPower(powerType.forValue(unitMod - UnitMod.PowerStart));

                break;
            case ResistanceHoly:
            case ResistanceFire:
            case ResistanceNature:
            case ResistanceFrost:
            case ResistanceShadow:
            case ResistanceArcane:
                updateResistances(getSpellSchoolByAuraGroup(unitMod));

                break;
            case ResistanceEnd:
            case AttackPower:
                updateAttackPowerAndDamage();

                break;
            case AttackPowerRanged:
                updateAttackPowerAndDamage(true);

                break;
            case DamageMainHand:
                updateDamagePhysical(WeaponAttackType.BASE_ATTACK);

                break;
            case DamageOffHand:
                updateDamagePhysical(WeaponAttackType.OFF_ATTACK);

                break;
            case DamageRanged:
                updateDamagePhysical(WeaponAttackType.RangedAttack);

                break;
            default:
                break;
        }
    }

    private int getMinPower(Power power) {
        return power == powerType.LunarPower ? -100 : 0;
    }

    private Stats getStatByAuraGroup(UnitMod unitMod) {
        var stat = stats.Strength;

        switch (unitMod) {
            case StatStart:
            case StatStrength:
                stat = stats.Strength;

                break;
            case StatAgility:
                stat = stats.Agility;

                break;
            case StatStamina:
                stat = stats.Stamina;

                break;
            case StatIntellect:
                stat = stats.Intellect;

                break;
            default:
                break;
        }

        return stat;
    }

    private void updateStatBuffModForClient(Stats stat) {
        unitData.getStatPosBuff().set(stat.ordinal(), (int) floatStatPosBuff[stat.ordinal()]);
        unitData.getStatNegBuff().set(stat.ordinal(), (int) floatStatNegBuff[stat.ordinal()]);
    }

    private void triggerOnPowerChangeAuras(Power power, int oldVal, int newVal) {
        var effects = getAuraEffectsByType(AuraType.TRIGGER_SPELL_ON_POWER_PCT);
        var effectsAmount = getAuraEffectsByType(AuraType.TRIGGER_SPELL_ON_POWER_AMOUNT);
        effects.addAll(effectsAmount);

        for (var effect : effects) {
            if (effect.getMiscValue() == power.index) {
                var effectAmount = effect.getAmount();
                var triggerSpell = effect.getSpellEffectInfo().triggerSpell;

                float oldValueCheck = oldVal;
                float newValueCheck = newVal;

                if (effect.getAuraType() == AuraType.TRIGGER_SPELL_ON_POWER_PCT) {
                    var maxPower = getMaxPower(power);
                    oldValueCheck = MathUtil.GetPctOf(oldVal, maxPower);
                    newValueCheck = MathUtil.GetPctOf(newVal, maxPower);
                }
                var direction = AuraTriggerOnPowerChangeDirection.values()[effect.getMiscValueB()];
                switch (direction) {
                    case Gain:
                        if (oldValueCheck >= effect.getAmount() || newValueCheck < effectAmount) {
                            continue;
                        }

                        break;
                    case Loss:
                        if (oldValueCheck <= effect.getAmount() || newValueCheck > effectAmount) {
                            continue;
                        }

                        break;
                    default:
                        break;
                }

                castSpell(this, triggerSpell, new CastSpellExtraArgs(effect));
            }
        }
    }

    // player or player's pet resilience (-1%)
    private double getDamageReduction(double damage) {
        return getCombatRatingDamageReduction(CombatRating.ResiliencePlayerDamage, 1.0f, 100.0f, damage);
    }

    private double getCombatRatingReduction(CombatRating cr) {
        var player = toPlayer();

        if (player) {
            return player.getRatingBonusValue(cr);
        }
        // Player's pet get resilience from owner
        else if (isPet() && getOwnerUnit()) {
            var owner = getOwnerUnit().toPlayer();

            if (owner) {
                return owner.getRatingBonusValue(cr);
            }
        }

        return 0.0f;
    }

    private double getCombatRatingDamageReduction(CombatRating cr, float rate, float cap, double damage) {
        var percent = Math.min(getCombatRatingReduction(cr) * rate, cap);
        return MathUtil.calculatePct(damage, percent);
    }

    private double getUnitCriticalChanceDone(WeaponAttackType attackType) {
        double chance = 0.0f;
        var thisPlayer = toPlayer();

        if (thisPlayer != null) {
            switch (attackType) {
                case BaseAttack:
                    chance = thisPlayer.getActivePlayerData().critPercentage;

                    break;
                case OffAttack:
                    chance = thisPlayer.getActivePlayerData().offhandCritPercentage;

                    break;
                case RangedAttack:
                    chance = thisPlayer.getActivePlayerData().rangedCritPercentage;

                    break;
            }
        } else {
            if (!toCreature().getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.NoCrit)) {
                chance = 5.0f;
                chance += getTotalAuraModifier(AuraType.ModWeaponCritPercent);
                chance += getTotalAuraModifier(AuraType.ModCritPct);
            }
        }

        return chance;
    }

    private double getUnitCriticalChanceTaken(Unit attacker, WeaponAttackType attackType, double critDone) {
        var chance = critDone;

        // flat aura mods
        if (attackType == WeaponAttackType.RangedAttack) {
            chance += getTotalAuraModifier(AuraType.ModAttackerRangedCritChance);
        } else {
            chance += getTotalAuraModifier(AuraType.ModAttackerMeleeCritChance);
        }

        chance += getTotalAuraModifier(AuraType.ModCritChanceVersusTargetHealth, aurEff -> !healthBelowPct(aurEff.miscValueB));

        chance += getTotalAuraModifier(AuraType.ModCritChanceForCaster, aurEff -> Objects.equals(aurEff.casterGuid, attacker.getGUID()));

        var tempSummon = attacker.toTempSummon();

        if (tempSummon != null) {
            chance += getTotalAuraModifier(AuraType.ModCritChanceForCasterPet, aurEff -> Objects.equals(aurEff.casterGuid, tempSummon.getSummonerGUID()));
        }

        chance += getTotalAuraModifier(AuraType.ModAttackerSpellAndWeaponCritChance);

        return Math.max(chance, 0.0f);
    }

    private double getUnitDodgeChance(WeaponAttackType attType, Unit victim) {
        var levelDiff = (int) (victim.getLevelForTarget(this) - getLevelForTarget(victim));

        double chance = 0.0f;
        double levelBonus = 0.0f;
        var playerVictim = victim.toPlayer();

        if (playerVictim) {
            chance = playerVictim.getActivePlayerData().dodgePercentage;
        } else {
            if (!victim.isTotem()) {
                chance = 3.0f;
                chance += victim.getTotalAuraModifier(AuraType.ModDodgePercent);

                if (levelDiff > 0) {
                    levelBonus = 1.5f * levelDiff;
                }
            }
        }

        chance += levelBonus;

        // Reduce enemy dodge chance by SPELL_AURA_MOD_COMBAT_RESULT_CHANCE
        chance += getTotalAuraModifierByMiscValue(AuraType.ModCombatResultChance, victimState.Dodge.getValue());

        // reduce dodge by SPELL_AURA_MOD_ENEMY_DODGE
        chance += getTotalAuraModifier(AuraType.ModEnemyDodge);

        // Reduce dodge chance by attacker expertise rating
        if (isTypeId(TypeId.PLAYER)) {
            chance -= toPlayer().getExpertiseDodgeOrParryReduction(attType);
        } else {
            chance -= getTotalAuraModifier(AuraType.ModExpertise) / 4.0f;
        }

        return Math.max(chance, 0.0f);
    }

    private double getUnitParryChance(WeaponAttackType attType, Unit victim) {
        var levelDiff = (int) (victim.getLevelForTarget(this) - getLevelForTarget(victim));

        double chance = 0.0f;
        double levelBonus = 0.0f;
        var playerVictim = victim.toPlayer();

        if (playerVictim) {
            if (playerVictim.getCanParry()) {
                var tmpitem = playerVictim.getWeaponForAttack(WeaponAttackType.BASE_ATTACK, true);

                if (!tmpitem) {
                    tmpitem = playerVictim.getWeaponForAttack(WeaponAttackType.OFF_ATTACK, true);
                }

                if (tmpitem) {
                    chance = playerVictim.getActivePlayerData().parryPercentage;
                }
            }
        } else {
            if (!victim.isTotem() && !victim.toCreature().getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.NoParry)) {
                chance = 6.0f;
                chance += victim.getTotalAuraModifier(AuraType.ModParryPercent);

                if (levelDiff > 0) {
                    levelBonus = 1.5f * levelDiff;
                }
            }
        }

        chance += levelBonus;

        // Reduce parry chance by attacker expertise rating
        if (isTypeId(TypeId.PLAYER)) {
            chance -= toPlayer().getExpertiseDodgeOrParryReduction(attType);
        } else {
            chance -= getTotalAuraModifier(AuraType.ModExpertise) / 4.0f;
        }

        return Math.max(chance, 0.0f);
    }

    private float getUnitMissChance() {
        var miss_chance = 5.0f;

        return miss_chance;
    }

    private double getUnitBlockChance(WeaponAttackType attType, Unit victim) {
        var levelDiff = (int) (victim.getLevelForTarget(this) - getLevelForTarget(victim));

        double chance = 0.0f;
        double levelBonus = 0.0f;
        var playerVictim = victim.toPlayer();

        if (playerVictim) {
            if (playerVictim.getCanBlock()) {
                var tmpitem = playerVictim.getUseableItemByPos(InventorySlots.Bag0, EquipmentSlot.OffHand);

                if (tmpitem && !tmpitem.isBroken() && tmpitem.getTemplate().getInventoryType() == inventoryType.Shield) {
                    chance = playerVictim.getActivePlayerData().blockPercentage;
                }
            }
        } else {
            if (!victim.isTotem() && !(victim.toCreature().getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.NoBlock))) {
                chance = 3.0f;
                chance += victim.getTotalAuraModifier(AuraType.ModBlockPercent);

                if (levelDiff > 0) {
                    levelBonus = 1.5f * levelDiff;
                }
            }
        }

        chance += levelBonus;

        return Math.max(chance, 0.0f);
    }

    private static class ValuesUpdateForPlayerWithMaskSender implements IDoWork<Player> {
        private final Unit owner;
        private final objectFieldData objectMask = new objectFieldData();
        private final UnitData unitMask = new unitData();

        public ValuesUpdateForPlayerWithMaskSender(Unit owner) {
            owner = owner;
        }

        public final void invoke(Player player) {
            UpdateData udata = new UpdateData(owner.getLocation().getMapId());

            owner.buildValuesUpdateForPlayerWithMask(udata, objectMask.getUpdateMask(), unitMask.getUpdateMask(), player);

            UpdateObject packet;
            tangible.OutObject<UpdateObject> tempOut_packet = new tangible.OutObject<UpdateObject>();
            udata.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }


    private void calculateHoverHeight() {
        float hoverHeight = ObjectDefine.DEFAULT_PLAYER_HOVER_HEIGHT;
        float displayScale = ObjectDefine.DEFAULT_PLAYER_DISPLAY_SCALE;

        int displayId = isMounted() ? getMountDisplayId() : getDisplayId();

        // Get DisplayScale for creatures
        if (isCreature()) {
            CreatureModel model = toCreature().getCreatureTemplate().getModelWithDisplayId(displayId);
            if (model != null) {
                displayScale = model.displayScale;
            }

        }
        var displayInfo = worldContext.getDbcObjectManager().creatureDisplayInfo(displayId);
        if (displayInfo != null) {
            var modelData = worldContext.getDbcObjectManager().creatureModelData(displayId);
            if (modelData != null) {
                hoverHeight = modelData.getHoverHeight() * modelData.getModelScale() * displayInfo.getCreatureModelScale() * displayScale;
            }
        }

        setHoverHeight(hoverHeight != 0 ? hoverHeight : ObjectDefine.DEFAULT_PLAYER_HOVER_HEIGHT);
    }

}
