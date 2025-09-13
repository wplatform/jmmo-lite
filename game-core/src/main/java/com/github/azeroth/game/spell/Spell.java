package com.github.azeroth.game.spell;


import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.domain.condition.DisableFlags;
import com.github.azeroth.game.domain.condition.DisableType;
import com.github.azeroth.game.battlepet.BattlePetMgr;
import com.github.azeroth.game.condition.ConditionSourceInfo;
import com.github.azeroth.game.domain.map.MapDb2Entries;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.entity.TraitConfig;
import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.TempSummon;
import com.github.azeroth.game.entity.creature.minion;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.dynamic.DynamicObjectType;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemEnchantmentManager;
import com.github.azeroth.game.entity.item.ItemPosCount;
import com.github.azeroth.game.entity.item.bonusData;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.model.DuelInfo;
import com.github.azeroth.game.entity.unit.*;
import com.github.azeroth.game.domain.unit.CurrentSpellType;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.loot.Loot;
import com.github.azeroth.game.loot.LootStorage;
import com.github.azeroth.game.map.*;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.map.interfaces.IGridNotifier;
import com.github.azeroth.game.movement.*;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.networking.packet.spell.SpellCastVisual;
import com.github.azeroth.game.listener.SpellScript;
import com.github.azeroth.game.listener.interfaces.IHasSpellEffects;
import com.github.azeroth.game.listener.interfaces.ISpellScript;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnDuelRequest;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnQuestStatusChange;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnSpellCast;
import com.github.azeroth.game.listener.interfaces.iquest.IQuestOnQuestStatusChange;
import com.github.azeroth.game.listener.interfaces.ispell.*;
import com.github.azeroth.game.spell.enums.SpellCastFlagsEx;
import com.github.azeroth.game.spell.enums.SpellState;
import com.github.azeroth.game.spell.enums.SpellValueMod;
import com.github.azeroth.game.spell.enums.TriggerCastFlag;

import java.io.IOException;
import java.util.*;


public class Spell {
    private static final ArrayList<ISpellScript> DUMMY = new ArrayList<>();
    private final HashMap<Byte, SpellEmpowerStageRecord> empowerStages = new HashMap<Byte, SpellEmpowerStageRecord>();
    private final HashMap<SpellEffectName, SpellLogEffect> executeLogEffects = new HashMap<SpellEffectName, SpellLogEffect>();
    private final WorldObject caster;
    private final boolean canReflect; // can reflect this spell?
    private final HashMap<Integer, Double> damageMultipliers = new HashMap<Integer, Double>();
    private final ArrayList<GOTargetInfo> uniqueGoTargetInfo = new ArrayList<>();
    private final ArrayList<ItemTargetInfo> uniqueItemInfo = new ArrayList<>();
    private final ArrayList<CorpseTargetInfo> uniqueCorpseTargetInfo = new ArrayList<>();
    private final HashMap<Integer, SpellDestination> destTargets = new HashMap<Integer, SpellDestination>();
    private final ArrayList<HitTriggerSpell> hitTriggerSpells = new ArrayList<>();
    private final TriggerCastFlag triggeredCastFlags;
    private final HashSet<Integer> applyMultiplierMask = new HashSet<Integer>();
    private final HashSet<Integer> channelTargetEffectMask = new HashSet<Integer>(); // Mask req. alive targets
    public SpellInfo spellInfo;
    public Item castItem;
    public ObjectGuid castItemGuid = ObjectGuid.EMPTY;
    public int castItemEntry;
    public int castItemLevel;
    public ObjectGuid castId = ObjectGuid.EMPTY;
    public ObjectGuid originalCastId = ObjectGuid.EMPTY;
    public boolean fromClient;
    public SpellCastFlagsEx castFlagsEx = SpellCastFlagsEx.values()[0];
    public SpellMisc spellMisc = new SpellMisc();
    public Object customArg;
    public SpellCastVisual spellVisual = new SpellCastVisual();
    public SpellCastTargets targets;
    public byte comboPointGain;
    public SpellCustomErrors customErrors = SpellCustomErrors.values()[0];
    public ArrayList<Aura> appliedMods;
    public SpellValue spellValue;
    public Spell selfContainer;
    // Current targets, to be used in SpellEffects (MUST BE USED ONLY IN SPELL EFFECTS)
    public Unit unitTarget;
	private static final ArrayList<(ISpellScript,ISpellEffect)>DUMMYSPELLEFFECTS =new ArrayList<(ISpellScript,ISpellEffect)>();
    public Item itemTarget;
    public GameObject gameObjTarget;
    public Corpse corpseTarget;
    public WorldLocation destTarget;
    public double damage;
    public SpellMissInfo targetMissInfo = SpellMissInfo.values()[0];
    public double variance;
    public SpellEffectInfo effectInfo;
    private final HashMap<class,ArrayList<ISpellScript>>spellScriptsByType =new HashMap<class,ArrayList<ISpellScript>>();
	private final Dictionary<Integer, Dictionary<SpellScriptHookType, ArrayList<(ISpellScript,ISpellEffect)>>>effectHandlers =new();
    // Damage and healing in effects need just calculate
    public double damageInEffects; // Damge   in effects count here
    public double healingInEffects; // Healing in effects count here
    // *****************************************
    // Spell target subsystem
    // *****************************************
    // Targets store structures and data
    public ArrayList<TargetInfo> uniqueTargetInfo = new ArrayList<>();
    public ArrayList<TargetInfo> uniqueTargetInfoOrgi = new ArrayList<>();
    // if need this can be replaced by Aura copy
    // we can't store original aura link to prevent access to deleted auras
    // and in same time need aura data and after aura deleting.
    public SpellInfo triggeredByAuraSpell;
    //Spell data
    public SpellSchoolMask spellSchoolMask; // Spell school (can be overwrite for some spells (wand shoot for example)
    public WeaponAttackType attackType = WeaponAttackType.values()[0]; // For weapon based attack
    public boolean needComboPoints;
    // used in effects handlers
    public UnitAura spellAura;
    public DynObjAura dynObjAura;
    // ******************************************
    // Spell trigger system
    // ******************************************
    public ProcFlagsInit procAttacker; // Attacker trigger flags
    public ProcFlagsInit procVictim; // Victim   trigger flags
    public ProcFlagsHit hitMask = ProcFlagsHit.values()[0];
    private ArrayList<SpellScript> loadedScripts = new ArrayList<>();
    private PathGenerator preGeneratedPath;
    private ObjectGuid originalCasterGuid = ObjectGuid.EMPTY;
    private Unit originalCaster;

    private ArrayList<SpellPowerCost> powerCosts = new ArrayList<>();
    private int casttime; // Calculated spell cast time initialized only in spell.prepare
    private int channeledDuration; // Calculated channeled spell duration in order to calculate correct pushback.
    private boolean autoRepeat;
    private byte runesState;
    private byte delayAtDamageCount;

    // Delayed spells system
    private long delayStart; // time of spell delay start, filled by event handler, zero = just started
    private long delayMoment; // moment of next delay call, used internally
    private boolean launchHandled; // were launch actions handled
    private boolean immediateHandled; // were immediate actions handled? (used by delayed spells only)

    // These vars are used in both delayed spell system and modified immediate spell system
    private boolean referencedFromCurrentSpell;
    private boolean executedCurrently;
    private SpellEffectHandleMode effectHandleMode = SpellEffectHandleMode.values()[0];

    // -------------------------------------------
    private GameObject focusObject;

    private SpellState spellState = SpellState.values()[0];
    private int timer;

    // Empower spell meta
    private EmpowerState empowerState = EmpowerState.NONE;
    private byte empoweredSpellStage;
    private int empoweredSpellDelta;

    private SpellEvent spellEvent;
    private Byte empoweredStage = null;

    public Spell(WorldObject caster, SpellInfo info, TriggerCastFlag triggerFlags, ObjectGuid originalCasterGuid, ObjectGuid originalCastId) {
        this(caster, info, triggerFlags, originalCasterGuid, originalCastId, null);
    }

    public Spell(WorldObject caster, SpellInfo info, TriggerCastFlag triggerFlags, ObjectGuid originalCasterGuid) {
        this(caster, info, triggerFlags, originalCasterGuid, null, null);
    }

    public Spell(WorldObject caster, SpellInfo info, TriggerCastFlag triggerFlags) {
        this(caster, info, triggerFlags, null, null, null);
    }

    public Spell(WorldObject caster, SpellInfo info, TriggerCastFlag triggerFlags, ObjectGuid originalCasterGuid, ObjectGuid originalCastId, Byte empoweredStage) {
        spellInfo = info;

        for (var stage : info.getEmpowerStages().entrySet()) {
            empowerStages.put(stage.getKey(), new () {
                Id = stage.getValue().id, durationMs = stage.getValue().durationMs, SpellEmpowerID = stage.getValue().SpellEmpowerID, stage = stage.getValue().Stage
            });
        }

        caster = (info.hasAttribute(SpellAttr6.OriginateFromController) && caster.getCharmerOrOwner() != null ? caster.getCharmerOrOwner() : caster);
        spellValue = new spellValue(spellInfo, caster);
        needComboPoints = spellInfo.getNeedsComboPoints();

        // Get data for type of attack
        attackType = info.getAttackType();

        spellSchoolMask = spellInfo.getSchoolMask(); // Can be override for some spell (wand shoot for example)

        if (originalCasterGuid.isEmpty()) {
            originalCasterGuid = caster.getGUID();
        }

        var playerCaster = caster.toPlayer();

        if (playerCaster != null) {
            // wand case
            if (attackType == WeaponAttackType.RangedAttack) {
                if ((playerCaster.getClassMask() & (int) playerClass.ClassMaskWandUsers.getValue()) != 0) {
                    var pItem = playerCaster.getWeaponForAttack(WeaponAttackType.RangedAttack);

                    if (pItem != null) {
                        spellSchoolMask = spellSchoolMask.forValue(1 << (int) pItem.getTemplate().getDamageType());
                    }
                }
            }
        }

        var modOwner = caster.getSpellModOwner();

        if (modOwner != null) {
            tangible.RefObject<Integer> tempRef_AuraStackAmount = new tangible.RefObject<Integer>(spellValue.auraStackAmount);
            modOwner.applySpellMod(info, SpellModOp.Doses, tempRef_AuraStackAmount, this);
            spellValue.auraStackAmount = tempRef_AuraStackAmount.refArgValue;
        }


        if (Objects.equals(originalCasterGuid, caster.getGUID())) {
            originalCaster = caster.toUnit();
        } else {
            originalCaster = global.getObjAccessor().GetUnit(caster, originalCasterGuid);

            if (originalCaster != null && !originalCaster.isInWorld()) {
                originalCaster = null;
            } else {
                originalCaster = caster.toUnit();
            }
        }

        triggeredCastFlags = triggerFlags;

        if (info.hasAttribute(SpellAttr2.DoNotReportSpellFailure) || triggeredCastFlags.hasFlag(TriggerCastFlag.TriggeredAllowProc)) {
            triggeredCastFlags = TriggerCastFlag.forValue(triggeredCastFlags.getValue() | TriggerCastFlag.DontReportCastError.getValue());
        }

        if (spellInfo.hasAttribute(SpellAttr4.AllowCastWhileCasting) || triggeredCastFlags.hasFlag(TriggerCastFlag.TriggeredAllowProc)) {
            triggeredCastFlags = TriggerCastFlag.forValue(triggeredCastFlags.getValue() | TriggerCastFlag.IgnoreCastInProgress.getValue());
        }

        castItemLevel = -1;

        if (isIgnoringCooldowns()) {
            castFlagsEx = SpellCastFlagsEx.forValue(castFlagsEx.getValue() | SpellCastFlagsEx.IgnoreCooldown.getValue());
        }

        castId = ObjectGuid.create(HighGuid.Cast, SpellCastSource.NORMAL, caster.getLocation().getMapId(), spellInfo.getId(), caster.getMap().generateLowGuid(HighGuid.Cast));
        originalCastId = originalCastId;
        spellVisual.spellXSpellVisualID = caster.getCastSpellXSpellVisualId(spellInfo);

        //Auto Shot & shoot (wand)
        autoRepeat = spellInfo.isAutoRepeatRangedSpell();

        // Determine if spell can be reflected back to the caster
        // Patch 1.2 notes: Spell Reflection no longer reflects abilities
        canReflect = caster.isUnit() && spellInfo.getDmgClass() == SpellDmgClass.Magic && !spellInfo.hasAttribute(SpellAttr0.IsAbility) && !spellInfo.hasAttribute(SpellAttr1.NoReflection) && !spellInfo.hasAttribute(SpellAttr0.NoImmunities) && !spellInfo.isPassive();
        cleanupTargetList();

        for (var effect : spellInfo.getEffects()) {
            destTargets.put(effect.effectIndex, new SpellDestination(caster));
        }

        targets = new SpellCastTargets();
        appliedMods = new ArrayList<>();
        setEmpoweredStage(empoweredStage);
    }

    public static void sendCastResult(Player caster, SpellInfo spellInfo, SpellCastVisual spellVisual, ObjectGuid castCount, SpellCastResult result, SpellCustomErrors customError, Integer param1) {
        sendCastResult(caster, spellInfo, spellVisual, castCount, result, customError, param1, null);
    }

    public static void sendCastResult(Player caster, SpellInfo spellInfo, SpellCastVisual spellVisual, ObjectGuid castCount, SpellCastResult result, SpellCustomErrors customError) {
        sendCastResult(caster, spellInfo, spellVisual, castCount, result, customError, null, null);
    }

    public static void sendCastResult(Player caster, SpellInfo spellInfo, SpellCastVisual spellVisual, ObjectGuid castCount, SpellCastResult result) {
        sendCastResult(caster, spellInfo, spellVisual, castCount, result, SpellCustomErrors.NONE, null, null);
    }

    public static void sendCastResult(Player caster, SpellInfo spellInfo, SpellCastVisual spellVisual, ObjectGuid castCount, SpellCastResult result, SpellCustomErrors customError, Integer param1, Integer param2) {
        if (result == SpellCastResult.SpellCastOk) {
            return;
        }

        CastFailed packet = new CastFailed();
        packet.visual = spellVisual;
        fillSpellCastFailedArgs(packet, castCount, spellInfo, result, customError, param1, param2, caster);
        caster.sendPacket(packet);
    }

    public static Spell extractSpellFromEvent(BasicEvent basicEvent) {
        var spellEvent = (SpellEvent) basicEvent;

        if (spellEvent != null) {
            return spellEvent.getSpell();
        }

        return null;
    }

    private static <T extends CastFailedBase> void fillSpellCastFailedArgs(T packet, ObjectGuid castId, SpellInfo spellInfo, SpellCastResult result, SpellCustomErrors customError, Integer param1, Integer param2, Player caster) {
        packet.castID = castId;
        packet.spellID = (int) spellInfo.getId();
        packet.reason = result;

        switch (result) {
            case NotReady:
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    packet.failedArg1 = 0; // unknown (second 1 update cooldowns on client flag)
                }

                break;
            case RequiresSpellFocus:
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    packet.failedArg1 = (int) spellInfo.getRequiresSpellFocus(); // SpellFocusObject.dbc id
                }

                break;
            case RequiresArea: // AreaTable.dbc id
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    // hardcode areas limitation case
                    switch (spellInfo.getId()) {
                        case 41617: // Cenarion Mana Salve
                        case 41619: // Cenarion Healing Salve
                            packet.failedArg1 = 3905;

                            break;
                        case 41618: // Bottled Nethergon Energy
                        case 41620: // Bottled Nethergon Vapor
                            packet.failedArg1 = 3842;

                            break;
                        case 45373: // Bloodberry Elixir
                            packet.failedArg1 = 4075;

                            break;
                        default: // default case (don't must be)
                            packet.failedArg1 = 0;

                            break;
                    }
                }

                break;
            case Totems:
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;

                    if (param2 != null) {
                        packet.failedArg2 = (int) param2;
                    }
                } else {
                    if (spellInfo.Totem[0] != 0) {
                        packet.failedArg1 = (int) spellInfo.Totem[0];
                    }

                    if (spellInfo.Totem[1] != 0) {
                        packet.failedArg2 = (int) spellInfo.Totem[1];
                    }
                }

                break;
            case TotemCategory:
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;

                    if (param2 != null) {
                        packet.failedArg2 = (int) param2;
                    }
                } else {
                    if (spellInfo.TotemCategory[0] != 0) {
                        packet.failedArg1 = (int) spellInfo.TotemCategory[0];
                    }

                    if (spellInfo.TotemCategory[1] != 0) {
                        packet.failedArg2 = (int) spellInfo.TotemCategory[1];
                    }
                }

                break;
            case EquippedItemClass:
            case EquippedItemClassMainhand:
            case EquippedItemClassOffhand:
                if (param1 != null && param2 != null) {
                    packet.failedArg1 = (int) param1;
                    packet.failedArg2 = (int) param2;
                } else {
                    packet.failedArg1 = spellInfo.getEquippedItemClass().getValue();
                    packet.failedArg2 = spellInfo.getEquippedItemSubClassMask();
                }

                break;
            case TooManyOfItem: {
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    int item = 0;

                    for (var spellEffectInfo : spellInfo.getEffects()) {
                        if (spellEffectInfo.itemType != 0) {
                            item = spellEffectInfo.itemType;
                        }
                    }

                    var proto = global.getObjectMgr().getItemTemplate(item);

                    if (proto != null && proto.getItemLimitCategory() != 0) {
                        packet.failedArg1 = (int) proto.getItemLimitCategory();
                    }
                }

                break;
            }
            case PreventedByMechanic:
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    packet.failedArg1 = (int) spellInfo.getAllEffectsMechanicMask(); // SpellMechanic.dbc id
                }

                break;
            case NeedExoticAmmo:
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    packet.failedArg1 = spellInfo.getEquippedItemSubClassMask(); // seems correct...
                }

                break;
            case NeedMoreItems:
                if (param1 != null && param2 != null) {
                    packet.failedArg1 = (int) param1;
                    packet.failedArg2 = (int) param2;
                } else {
                    packet.failedArg1 = 0; // Item id
                    packet.failedArg2 = 0; // Item count?
                }

                break;
            case MinSkill:
                if (param1 != null && param2 != null) {
                    packet.failedArg1 = (int) param1;
                    packet.failedArg2 = (int) param2;
                } else {
                    packet.failedArg1 = 0; // skillLine.dbc id
                    packet.failedArg2 = 0; // required skill second
                }

                break;
            case FishingTooLow:
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    packet.failedArg1 = 0; // required fishing skill
                }

                break;
            case CustomError:
                packet.failedArg1 = customError.getValue();

                break;
            case Silenced:
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    packet.failedArg1 = 0; // Unknown
                }

                break;
            case Reagents: {
                if (param1 != null) {
                    packet.failedArg1 = (int) param1;
                } else {
                    for (int i = 0; i < SpellConst.MaxReagents; i++) {
                        if (spellInfo.Reagent[i] <= 0) {
                            continue;
                        }

                        var itemid = (int) spellInfo.Reagent[i];
                        var itemcount = spellInfo.ReagentCount[i];

                        if (!caster.hasItemCount(itemid, itemcount)) {
                            packet.failedArg1 = (int) itemid; // first missing item

                            break;
                        }
                    }
                }

                if (param2 != null) {
                    packet.failedArg2 = (int) param2;
                } else if (param1 == null) {
                    for (var reagentsCurrency : spellInfo.reagentsCurrency) {
                        if (!caster.hasCurrency(reagentsCurrency.CurrencyTypesID, reagentsCurrency.currencyCount)) {
                            packet.failedArg1 = -1;
                            packet.failedArg2 = reagentsCurrency.CurrencyTypesID;

                            break;
                        }
                    }
                }

                break;
            }
            case CantUntalent: {
                packet.failedArg1 = (int) param1;

                break;
            }
            // TODO: SPELL_FAILED_NOT_STANDING
            default:
                break;
        }
    }

    private static boolean processScript(int effIndex, boolean preventDefault, ISpellScript script, ISpellEffect effect, SpellScriptHookType hookType) {
        try {
            script._InitHit();

            script._PrepareScriptCall(hookType);

            if (!script._IsEffectPrevented(effIndex)) {
                if (effect instanceof ISpellEffectHandler seh) {
                    seh.callEffect(effIndex);
                }
            }

            if (!preventDefault) {
                preventDefault = script._IsDefaultEffectPrevented(effIndex);
            }

            script._FinishScriptCall();
        } catch (RuntimeException ex) {
            Log.outException(ex, "");
        }

        return preventDefault;
    }

    public final SpellState getState() {
        return spellState;
    }

    public final void setState(SpellState value) {
        spellState = value;
    }

    public final int getCastTime() {
        return casttime;
    }

    private boolean isAutoRepeat() {
        return autoRepeat;
    }

    private void setAutoRepeat(boolean value) {
        autoRepeat = value;
    }

    public final boolean isDeletable() {
        return !referencedFromCurrentSpell && !executedCurrently;
    }

    public final boolean isInterruptable() {
        return !executedCurrently;
    }

    public final long getDelayStart() {
        return delayStart;
    }

    public final void setDelayStart(long value) {
        delayStart = value;
    }

    public final long getDelayMoment() {
        return delayMoment;
    }

    public final WorldObject getCaster() {
        return caster;
    }

    public final ObjectGuid getOriginalCasterGuid() {
        return originalCasterGuid;
    }

    public final Unit getOriginalCaster() {
        return originalCaster;
    }

    public final ArrayList<SpellPowerCost> getPowerCost() {
        return powerCosts;
    }

    private boolean getDontReport() {
        return (boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.DontReportCastError.getValue());
    }

    public final boolean isEmpowered() {
        return !spellInfo.getEmpowerStages().isEmpty() && caster.isPlayer();
    }

    public final Byte getEmpoweredStage() {
        return empoweredStage;
    }

    public final void setEmpoweredStage(Byte value) {
        empoweredStage = value;
    }

    public final CurrentSpellType getCurrentContainer() {
        if (spellInfo.isNextMeleeSwingSpell()) {
            return CurrentSpellType.MELEE_SPELL;
        } else if (isAutoRepeat()) {
            return CurrentSpellType.AUTOREPEAT_SPELL;
        } else if (spellInfo.isChanneled()) {
            return CurrentSpellType.CHANNELED_SPELL;
        }

        return CurrentSpellType.GENERIC_SPELL;
    }

    public final Difficulty getCastDifficulty() {
        return caster.getMap().getDifficultyID();
    }

    public final boolean isPositive() {
        return spellInfo.isPositive() && (triggeredByAuraSpell == null || triggeredByAuraSpell.isPositive());
    }

    public final Unit getUnitCasterForEffectHandlers() {
        return originalCaster != null ? originalCaster : caster.toUnit();
    }

    public final boolean isTriggered() {
        return triggeredCastFlags.hasFlag(TriggerCastFlag.FullMask);
    }

    public final boolean isIgnoringCooldowns() {
        return triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreSpellAndCategoryCD);
    }

    public final boolean isFocusDisabled() {
        return triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreSetFacing) || (spellInfo.isChanneled() && !spellInfo.hasAttribute(SpellAttr1.TrackTargetInChannel));
    }

    public final boolean isProcDisabled() {
        return triggeredCastFlags.hasFlag(TriggerCastFlag.DisallowProcEvents);
    }

    public final boolean isChannelActive() {
        return caster.isUnit() && caster.toUnit().getChannelSpellId() != 0;
    }

    public final boolean getTriggeredAllowProc() {
        return triggeredCastFlags.hasFlag(TriggerCastFlag.TriggeredAllowProc);
    }

    public void close() throws IOException {
        // unload scripts
        for (var i = 0; i < loadedScripts.size(); ++i) {
            loadedScripts.get(i)._Unload();
        }

        if (referencedFromCurrentSpell && selfContainer && selfContainer == this) {
            // Clean the reference to avoid later crash.
            // If this error is repeating, we may have to add an ASSERT to better track down how we get into this case.
            Log.outError(LogFilter.spells, "SPELL: deleting spell for spell ID {0}. However, spell still referenced.", spellInfo.getId());
            selfContainer = null;
        }
    }

    public final void initExplicitTargets(SpellCastTargets targets) {
        targets = targets;

        // this function tries to correct spell explicit targets for spell
        // client doesn't send explicit targets correctly sometimes - we need to fix such spells serverside
        // this also makes sure that we correctly send explicit targets to client (removes redundant data)
        var neededTargets = spellInfo.getExplicitTargetMask();

        var target = targets.getObjectTarget();

        if (target != null) {
            // check if object target is valid with needed target flags
            // for unit case allow corpse target mask because player with not released corpse is a unit target
            if ((target.toUnit() && !neededTargets.hasFlag(SpellCastTargetFlags.UnitMask.getValue() | SpellCastTargetFlags.CorpseMask.getValue())) || (target.isTypeId(TypeId.gameObject) && !neededTargets.hasFlag(SpellCastTargetFlags.GameobjectMask)) || (target.isTypeId(TypeId.Corpse) && !neededTargets.hasFlag(SpellCastTargetFlags.CorpseMask))) {
                targets.removeObjectTarget();
            }
        } else {
            // try to select correct unit target if not provided by client or by serverside cast
            if (neededTargets.hasFlag(SpellCastTargetFlags.UnitMask)) {
                Unit unit = null;
                // try to use player selection as a target
                var playerCaster = caster.toPlayer();

                if (playerCaster != null) {
                    // selection has to be found and to be valid target for the spell
                    var selectedUnit = global.getObjAccessor().GetUnit(caster, playerCaster.getTarget());

                    if (selectedUnit != null) {
                        if (spellInfo.checkExplicitTarget(caster, selectedUnit) == SpellCastResult.SpellCastOk) {
                            unit = selectedUnit;
                        }
                    }
                }
                // try to use attacked unit as a target
                else if (caster.isTypeId(TypeId.UNIT) && neededTargets.hasFlag(SpellCastTargetFlags.UnitEnemy.getValue() | SpellCastTargetFlags.unit.getValue())) {
                    unit = caster.toUnit().getVictim();
                }

                // didn't find anything - let's use self as target
                if (unit == null && neededTargets.hasFlag(SpellCastTargetFlags.UnitRaid.getValue() | SpellCastTargetFlags.UnitParty.getValue().getValue() | SpellCastTargetFlags.UnitAlly.getValue().getValue())) {
                    unit = caster.toUnit();
                }

                targets.setUnitTarget(unit);
            }
        }

        // check if spell needs dst target
        if (neededTargets.hasFlag(SpellCastTargetFlags.DestLocation)) {
            // and target isn't set
            if (!targets.getHasDst()) {
                // try to use unit target if provided
                var targett = targets.getObjectTarget();

                if (targett != null) {
                    targets.setDst(targett);
                }
                // or use self if not available
                else {
                    targets.setDst(caster);
                }
            }
        } else {
            targets.removeDst();
        }

        if (neededTargets.hasFlag(SpellCastTargetFlags.sourceLocation)) {
            if (!targets.getHasSrc()) {
                targets.setSrc(caster);
            }
        } else {
            targets.removeSrc();
        }
    }

    public final void selectSpellTargets() {
        // select targets for cast phase
        selectExplicitTargets();

        var processedAreaEffectsMask = new HashSet<Integer>();

        for (var spellEffectInfo : spellInfo.getEffects()) {
            // not call for empty effect.
            // Also some spells use not used effect targets for store targets for dummy effect in triggered spells
            if (!spellEffectInfo.isEffect()) {
                continue;
            }

            // set expected type of implicit targets to be sent to client
            var implicitTargetMask = SpellCastTargetFlags.forValue(spellInfo.getTargetFlagMask(spellEffectInfo.targetA.getObjectType()).getValue() | spellInfo.getTargetFlagMask(spellEffectInfo.targetB.getObjectType()).getValue());

            if ((boolean) (implicitTargetMask.getValue() & SpellCastTargetFlags.unit.getValue())) {
                targets.setTargetFlag(SpellCastTargetFlags.unit);
            }

            if ((boolean) (implicitTargetMask.getValue() & (SpellCastTargetFlags.GAMEOBJECT.getValue() | SpellCastTargetFlags.GameobjectItem.getValue()).getValue())) {
                targets.setTargetFlag(SpellCastTargetFlags.GAMEOBJECT);
            }

            selectEffectImplicitTargets(spellEffectInfo, spellEffectInfo.targetA, processedAreaEffectsMask);
            selectEffectImplicitTargets(spellEffectInfo, spellEffectInfo.targetB, processedAreaEffectsMask);

            // Select targets of effect based on effect type
            // those are used when no valid target could be added for spell effect based on spell target type
            // some spell effects use explicit target as a default target added to target map (like SPELL_EFFECT_LEARN_SPELL)
            // some spell effects add target to target map only when target type specified (like SPELL_EFFECT_WEAPON)
            // some spell effects don't add anything to target map (confirmed with sniffs) (like SPELL_EFFECT_DESTROY_ALL_TOTEMS)
            selectEffectTypeImplicitTargets(spellEffectInfo);

            if (targets.getHasDst()) {
                addDestTarget(targets.getDst(), spellEffectInfo.effectIndex);
            }

            if (spellEffectInfo.targetA.getObjectType() == SpellTargetObjectTypes.unit || spellEffectInfo.targetA.getObjectType() == SpellTargetObjectTypes.UnitAndDest || spellEffectInfo.targetB.getObjectType() == SpellTargetObjectTypes.unit || spellEffectInfo.targetB.getObjectType() == SpellTargetObjectTypes.UnitAndDest) {
                if (spellInfo.hasAttribute(SpellAttr1.RequireAllTargets)) {
                    var noTargetFound = !uniqueTargetInfo.Any(target -> target.effects.contains(spellEffectInfo.effectIndex));

                    if (noTargetFound) {
                        sendCastResult(SpellCastResult.BadImplicitTargets);
                        finish(SpellCastResult.BadImplicitTargets);

                        return;
                    }
                }

                if (spellInfo.hasAttribute(SpellAttr2.FailOnAllTargetsImmune)) {
                    var anyNonImmuneTargetFound = uniqueTargetInfo.Any(target -> target.effects.contains(spellEffectInfo.effectIndex) && target.missCondition != SpellMissInfo.Immune && target.missCondition != SpellMissInfo.Immune2);

                    if (!anyNonImmuneTargetFound) {
                        sendCastResult(SpellCastResult.Immune);
                        finish(SpellCastResult.Immune);

                        return;
                    }
                }
            }

            if (spellInfo.isChanneled()) {
                // maybe do this for all spells?
                if (focusObject == null && uniqueTargetInfo.isEmpty() && uniqueGoTargetInfo.isEmpty() && uniqueItemInfo.isEmpty() && !targets.getHasDst()) {
                    sendCastResult(SpellCastResult.BadImplicitTargets);
                    finish(SpellCastResult.BadImplicitTargets);

                    return;
                }

                for (var ihit : uniqueTargetInfo) {
                    if (ihit.effects.contains(spellEffectInfo.effectIndex)) {
                        channelTargetEffectMask.add(spellEffectInfo.effectIndex);

                        break;
                    }
                }
            }
        }

        var dstDelay = calculateDelayMomentForDst(spellInfo.getLaunchDelay());

        if (dstDelay != 0) {
            delayMoment = dstDelay;
        }
    }

    public final void recalculateDelayMomentForDst() {
        delayMoment = calculateDelayMomentForDst(0.0f);
        caster.getEvents().ModifyEventTime(spellEvent, duration.ofSeconds(getDelayStart() + delayMoment));
    }

    public final GridMapTypeMask getSearcherTypeMask(SpellTargetObjectTypes objType, ArrayList<Condition> condList) {
        // this function selects which containers need to be searched for spell target
        var retMask = GridMapTypeMask.All;

        // filter searchers based on searched object type
        switch (objType) {
            case Unit:
            case UnitAndDest:
                retMask = GridMapTypeMask.forValue(retMask.getValue() & GridMapTypeMask.forValue(GridMapTypeMask.player.getValue() | GridMapTypeMask.CREATURE.getValue()).getValue());

                break;
            case Corpse:
            case CorpseEnemy:
            case CorpseAlly:
                retMask = GridMapTypeMask.forValue(retMask.getValue() & GridMapTypeMask.forValue(GridMapTypeMask.player.getValue() | GridMapTypeMask.Corpse.getValue().getValue() | GridMapTypeMask.CREATURE.getValue().getValue()).getValue());

                break;
            case Gobj:
            case GobjItem:
                retMask = GridMapTypeMask.forValue(retMask.getValue() & GridMapTypeMask.gameObject.getValue());

                break;
            default:
                break;
        }

        if (spellInfo.hasAttribute(SpellAttr3.OnlyOnPlayer)) {
            retMask = GridMapTypeMask.forValue(retMask.getValue() & GridMapTypeMask.forValue(GridMapTypeMask.Corpse.getValue() | GridMapTypeMask.player.getValue()).getValue());
        }

        if (spellInfo.hasAttribute(SpellAttr3.OnlyOnGhosts)) {
            retMask = GridMapTypeMask.forValue(retMask.getValue() & GridMapTypeMask.player.getValue());
        }

        if (spellInfo.hasAttribute(SpellAttr5.NotOnPlayer)) {
            retMask = GridMapTypeMask.forValue(retMask.getValue() & ~GridMapTypeMask.player.getValue());
        }

        if (condList != null) {
            retMask = GridMapTypeMask.forValue(retMask.getValue() & global.getConditionMgr().getSearcherTypeMaskForConditionList(condList).getValue());
        }

        return retMask;
    }

    public final void cleanupTargetList() {
        uniqueTargetInfo.clear();
        uniqueGoTargetInfo.clear();
        uniqueItemInfo.clear();
        delayMoment = 0;
    }

    public final long getUnitTargetCountForEffect(int effect) {
        return uniqueTargetInfo.size()
        (targetInfo -> targetInfo.missCondition == SpellMissInfo.NONE && targetInfo.effects.contains(effect));
    }

    public final long getGameObjectTargetCountForEffect(int effect) {
        return uniqueGoTargetInfo.size() (targetInfo -> targetInfo.effects.contains(effect));
    }

    public final long getItemTargetCountForEffect(int effect) {
        return uniqueItemInfo.size() (targetInfo -> targetInfo.effects.contains(effect));
    }

    public final long getCorpseTargetCountForEffect(int effect) {
        return uniqueCorpseTargetInfo.size() (targetInfo -> targetInfo.effects.contains(effect));
    }

    public final SpellMissInfo preprocessSpellHit(Unit unit, TargetInfo hitInfo) {
        if (unit == null) {
            return SpellMissInfo.Evade;
        }

        // Target may have begun evading between launch and hit phases - re-check now
        var creatureTarget = unit.toCreature();

        if (creatureTarget != null && creatureTarget.isEvadingAttacks()) {
            return SpellMissInfo.Evade;
        }

        // For delayed spells immunity may be applied between missile launch and hit - check immunity for that case
        if (spellInfo.getHasHitDelay() && unit.isImmunedToSpell(spellInfo, caster)) {
            return SpellMissInfo.Immune;
        }

        callScriptBeforeHitHandlers(hitInfo.missCondition);

        var player = unit.toPlayer();

        if (player != null) {
            player.startCriteriaTimer(CriteriaStartEvent.BeSpellTarget, spellInfo.getId());
            player.updateCriteria(CriteriaType.BeSpellTarget, spellInfo.getId(), 0, 0, caster);
            player.updateCriteria(CriteriaType.GainAura, spellInfo.getId());
        }

        var casterPlayer = caster.toPlayer();

        if (casterPlayer) {
            casterPlayer.startCriteriaTimer(CriteriaStartEvent.castSpell, spellInfo.getId());
            casterPlayer.updateCriteria(CriteriaType.LandTargetedSpellOnTarget, spellInfo.getId(), 0, 0, unit);
        }

        if (caster != unit) {
            // Recheck  UNIT_FLAG_NON_ATTACKABLE for delayed spells
            if (spellInfo.getHasHitDelay() && unit.hasUnitFlag(UnitFlag.NonAttackable) && ObjectGuid.opNotEquals(unit.getCharmerOrOwnerGUID(), caster.getGUID())) {
                return SpellMissInfo.Evade;
            }

            if (caster.isValidAttackTarget(unit, spellInfo)) {
                unit.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.HostileActionReceived);
            } else if (caster.isFriendlyTo(unit)) {
                // for delayed spells ignore negative spells (after duel end) for friendly targets
                if (spellInfo.getHasHitDelay() && unit.isPlayer() && !isPositive() && !caster.isValidAssistTarget(unit, spellInfo)) {
                    return SpellMissInfo.Evade;
                }

                // assisting case, healing and resurrection
                if (unit.hasUnitState(UnitState.AttackPlayer)) {
                    var playerOwner = caster.getCharmerOrOwnerPlayerOrPlayerItself();

                    if (playerOwner != null) {
                        playerOwner.setContestedPvP();
                        playerOwner.updatePvP(true);
                    }
                }

                if (originalCaster && unit.isInCombat() && spellInfo.getHasInitialAggro()) {
                    if (originalCaster.hasUnitFlag(UnitFlag.PlayerControlled)) // only do explicit combat forwarding for PvP enabled units
                    {
                        originalCaster.getCombatManager().inheritCombatStatesFrom(unit); // for creature v creature combat, the threat forward does it for us
                    }

                    unit.getThreatManager().forwardThreatForAssistingMe(originalCaster, 0.0f, null, true);
                }
            }
        }

        // original caster for auras
        var origCaster = caster;

        if (originalCaster) {
            origCaster = originalCaster;
        }

        // check immunity due to diminishing returns
        if (!aura.buildEffectMaskForOwner(spellInfo, SpellConst.MaxEffects, unit).isEmpty()) {
            for (var spellEffectInfo : spellInfo.getEffects()) {
                hitInfo.auraBasePoints.put(spellEffectInfo.effectIndex, (spellValue.customBasePointsMask & (1 << spellEffectInfo.effectIndex)) != 0 ? spellValue.effectBasePoints.get(spellEffectInfo.effectIndex) : spellEffectInfo.calcBaseValue(originalCaster, unit, castItemEntry, castItemLevel));
            }

            // Get Data Needed for Diminishing Returns, some effects may have multiple auras, so this must be done on spell hit, not aura add
            hitInfo.drGroup = spellInfo.getDiminishingReturnsGroupForSpell();

            var diminishLevel = DiminishingLevels.Level1;

            if (hitInfo.drGroup != 0) {
                diminishLevel = unit.getDiminishing(hitInfo.drGroup);
                var type = spellInfo.getDiminishingReturnsGroupType();

                // Increase Diminishing on unit, current informations for actually casts will use values above
                if (type == DiminishingReturnsType.All || (type == DiminishingReturnsType.player && unit.isAffectedByDiminishingReturns())) {
                    unit.incrDiminishing(spellInfo);
                }
            }

            // Now Reduce spell duration using data received at spell hit
            // check whatever effects we're going to apply, diminishing returns only apply to negative aura effects
            hitInfo.positive = true;

            if (origCaster == unit || !origCaster.isFriendlyTo(unit)) {
                for (var spellEffectInfo : spellInfo.getEffects()) {
                    // mod duration only for effects applying aura!
                    if (hitInfo.effects.contains(spellEffectInfo.effectIndex) && spellEffectInfo.isUnitOwnedAuraEffect() && !spellInfo.isPositiveEffect(spellEffectInfo.effectIndex)) {
                        hitInfo.positive = false;

                        break;
                    }
                }
            }

            hitInfo.auraDuration = aura.calcMaxDuration(spellInfo, origCaster);

            // unit is immune to aura if it was diminished to 0 duration
            tangible.RefObject<Integer> tempRef_AuraDuration = new tangible.RefObject<Integer>(hitInfo.auraDuration);
            if (!hitInfo.positive && !unit.applyDiminishingToDuration(spellInfo, tempRef_AuraDuration, origCaster, diminishLevel)) {
                hitInfo.auraDuration = tempRef_AuraDuration.refArgValue;
                if (spellInfo.getEffects().All(effInfo -> !effInfo.isEffect() || effInfo.isEffect(SpellEffectName.ApplyAura))) {
                    return SpellMissInfo.Immune;
                }
            } else {
                hitInfo.auraDuration = tempRef_AuraDuration.refArgValue;
            }
        }

        return SpellMissInfo.NONE;
    }

    public final void doSpellEffectHit(Unit unit, SpellEffectInfo spellEffectInfo, TargetInfo hitInfo) {
        var aura_effmask = Aura.buildEffectMaskForOwner(spellInfo, new HashSet<Integer>() {
            spellEffectInfo.EffectIndex
        }, unit);

        if (!aura_effmask.isEmpty()) {
            var caster = caster;

            if (originalCaster) {
                caster = originalCaster;
            }

            if (caster != null) {
                // delayed spells with multiple targets need to create a new aura object, otherwise we'll access a deleted aura
                if (hitInfo.hitAura == null) {
                    var resetPeriodicTimer = (spellInfo.getStackAmount() < 2) && !triggeredCastFlags.hasFlag(TriggerCastFlag.DontResetPeriodicTimer);
                    var allAuraEffectMask = aura.buildEffectMaskForOwner(spellInfo, SpellConst.MaxEffects, unit);

                    AuraCreateInfo createInfo = new AuraCreateInfo(castId, spellInfo, getCastDifficulty(), allAuraEffectMask, unit);
                    createInfo.setCasterGuid(caster.getGUID());
                    createInfo.setBaseAmount(hitInfo.auraBasePoints);
                    createInfo.setCastItem(castItemGuid, castItemEntry, castItemLevel);
                    createInfo.setPeriodicReset(resetPeriodicTimer);
                    createInfo.setOwnerEffectMask(aura_effmask);

                    var aura = aura.tryRefreshStackOrCreate(createInfo, false);

                    if (aura != null) {
                        hitInfo.hitAura = aura.toUnitAura();

                        // Set aura stack amount to desired second
                        if (spellValue.auraStackAmount > 1) {
                            if (!createInfo.isRefresh) {
                                hitInfo.hitAura.setStackAmount((byte) spellValue.auraStackAmount);
                            } else {
                                hitInfo.hitAura.modStackAmount(spellValue.auraStackAmount);
                            }
                        }

                        hitInfo.hitAura.setDiminishGroup(hitInfo.drGroup);

                        if (spellValue.duration == null) {
                            hitInfo.auraDuration = caster.modSpellDuration(spellInfo, unit, hitInfo.auraDuration, hitInfo.positive, hitInfo.hitAura.getAuraEffects().keySet().ToHashSet());

                            if (hitInfo.auraDuration > 0) {
                                hitInfo.AuraDuration *= (int) spellValue.durationMul;

                                // Haste modifies duration of channeled spells
                                if (spellInfo.isChanneled()) {
                                    tangible.RefObject<Integer> tempRef_AuraDuration = new tangible.RefObject<Integer>(hitInfo.auraDuration);
                                    caster.modSpellDurationTime(spellInfo, tempRef_AuraDuration, this);
                                    hitInfo.auraDuration = tempRef_AuraDuration.refArgValue;
                                } else if (spellInfo.hasAttribute(SpellAttr8.HasteAffectsDuration)) {
                                    var origDuration = hitInfo.auraDuration;
                                    hitInfo.auraDuration = 0;

                                    for (var auraEff : hitInfo.hitAura.getAuraEffects().entrySet()) {
                                        var period = auraEff.getValue().period;

                                        if (period != 0) // period is hastened by UNIT_MOD_CAST_SPEED
                                        {
                                            hitInfo.auraDuration = Math.max(Math.max(origDuration / period, 1) * period, hitInfo.auraDuration);
                                        }
                                    }

                                    // if there is no periodic effect
                                    if (hitInfo.auraDuration == 0) {
                                        hitInfo.auraDuration = (int) (origDuration * originalCaster.getUnitData().modCastingSpeed);
                                    }
                                }
                            }
                        } else {
                            hitInfo.auraDuration = spellValue.duration.intValue();
                        }

                        if (hitInfo.auraDuration != hitInfo.hitAura.getMaxDuration()) {
                            hitInfo.hitAura.setMaxDuration(hitInfo.auraDuration);
                            hitInfo.hitAura.setDuration(hitInfo.auraDuration);
                        }

                        if (createInfo.isRefresh) {
                            hitInfo.hitAura.addStaticApplication(unit, aura_effmask);
                        }
                    }
                } else {
                    hitInfo.hitAura.addStaticApplication(unit, aura_effmask);
                }
            }
        }

        spellAura = hitInfo.hitAura;
        handleEffects(unit, null, null, null, spellEffectInfo, SpellEffectHandleMode.HitTarget);
        spellAura = null;
    }

    public final void doTriggersOnSpellHit(Unit unit) {
        // handle SPELL_AURA_ADD_TARGET_TRIGGER auras
        // this is executed after spell proc spells on target hit
        // spells are triggered for each hit spell target
        // info confirmed with retail sniffs of permafrost and shadow weaving
        if (!hitTriggerSpells.isEmpty()) {
            var duration = 0;

            for (var hit : hitTriggerSpells) {
                if (canExecuteTriggersOnHit(unit, hit.triggeredByAura) && RandomUtil.randChance(hit.chance)) {
                    caster.castSpell(unit, hit.triggeredSpell.getId(), (new CastSpellExtraArgs(TriggerCastFlag.FullMask)).setTriggeringSpell(this).setCastDifficulty(hit.triggeredSpell.getDifficulty()));

                    Log.outDebug(LogFilter.spells, "Spell {0} triggered spell {1} by SPELL_AURA_ADD_TARGET_TRIGGER aura", spellInfo.getId(), hit.triggeredSpell.getId());

                    // SPELL_AURA_ADD_TARGET_TRIGGER auras shouldn't trigger auras without duration
                    // set duration of current aura to the triggered spell
                    if (hit.triggeredSpell.getDuration() == -1) {
                        var triggeredAur = unit.getAura(hit.triggeredSpell.getId(), caster.getGUID());

                        if (triggeredAur != null) {
                            // get duration from aura-only once
                            if (duration == 0) {
                                var aur = unit.getAura(spellInfo.getId(), caster.getGUID());
                                duration = aur != null ? aur.getDuration() : -1;
                            }

                            triggeredAur.setDuration(duration);
                        }
                    }
                }
            }
        }

        // trigger linked auras remove/apply
        // @todo remove/cleanup this, as this table is not documented and people are doing stupid things with it
        var spellTriggered = global.getSpellMgr().getSpellLinked(SpellLinkedType.hit, spellInfo.getId());

        if (spellTriggered != null) {
            for (var id : spellTriggered) {
                if (id < 0) {
                    unit.removeAura((int) -id);
                } else {
                    unit.castSpell(unit, (int) id, (new CastSpellExtraArgs(TriggerCastFlag.FullMask)).setOriginalCaster(caster.getGUID()).setTriggeringSpell(this));
                }
            }
        }
    }

    public final SpellCastResult prepare(SpellCastTargets targets) {
        return prepare(targets, null);
    }

    public final SpellCastResult prepare(SpellCastTargets targets, AuraEffect triggeredByAura) {
        if (castItem != null) {
            castItemGuid = castItem.getGUID();
            castItemEntry = castItem.getEntry();

            var owner = castItem.getOwnerUnit();

            if (owner) {
                castItemLevel = (int) castItem.getItemLevel(owner);
            } else if (Objects.equals(castItem.getOwnerGUID(), caster.getGUID())) {
                castItemLevel = (int) castItem.getItemLevel(caster.toPlayer());
            } else {
                sendCastResult(SpellCastResult.equippedItem);
                finish(SpellCastResult.equippedItem);

                return SpellCastResult.equippedItem;
            }
        }

        initExplicitTargets(targets);

        spellState = SpellState.Preparing;

        if (triggeredByAura != null) {
            triggeredByAuraSpell = triggeredByAura.getSpellInfo();
            castItemLevel = triggeredByAura.getBase().getCastItemLevel();
        }

        // create and add update event for this spell
        spellEvent = new SpellEvent(this);
        caster.getEvents().addEvent(spellEvent, caster.getEvents().CalculateTime(duration.ofSeconds(1)), true);

        // check disables
        if (global.getDisableMgr().isDisabledFor(DisableType.spell, spellInfo.getId(), caster)) {
            sendCastResult(SpellCastResult.SpellUnavailable);
            finish(SpellCastResult.SpellUnavailable);

            return SpellCastResult.SpellUnavailable;
        }

        // Prevent casting at cast another spell (ServerSide check)
        if (!triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreCastInProgress) && caster.toUnit() != null && caster.toUnit().isNonMeleeSpellCast(false, true, true, spellInfo.getId() == 75) && !castId.isEmpty()) {
            sendCastResult(SpellCastResult.SpellInProgress);
            finish(SpellCastResult.SpellInProgress);

            return SpellCastResult.SpellInProgress;
        }

        loadScripts();

        // Fill cost data (not use power for item casts
        if (castItem == null) {
            powerCosts = spellInfo.calcPowerCost(caster, spellSchoolMask, this);
        }

        // Set combo point requirement
        if ((boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreComboPoints.getValue()) || castItem != null) {
            needComboPoints = false;
        }

        int param1 = 0, param2 = 0;
        tangible.RefObject<Integer> tempRef_param1 = new tangible.RefObject<Integer>(param1);
        tangible.RefObject<Integer> tempRef_param2 = new tangible.RefObject<Integer>(param2);
        var result = checkCast(true, tempRef_param1, tempRef_param2);
        param2 = tempRef_param2.refArgValue;
        param1 = tempRef_param1.refArgValue;

        // target is checked in too many locations and with different results to handle each of them
        // handle just the general SPELL_FAILED_BAD_TARGETS result which is the default result for most DBC target checks
        if ((boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreTargetCheck.getValue()) && result == SpellCastResult.BadTargets) {
            result = SpellCastResult.SpellCastOk;
        }

        if (result != SpellCastResult.SpellCastOk) {
            // Periodic auras should be interrupted when aura triggers a spell which can't be cast
            // for example bladestorm aura should be removed on disarm as of patch 3.3.5
            // channeled periodic spells should be affected by this (arcane missiles, penance, etc)
            // a possible alternative sollution for those would be validating aura target on unit state change
            if (triggeredByAura != null && triggeredByAura.isPeriodic() && !triggeredByAura.getBase().isPassive()) {
                sendChannelUpdate(0);
                triggeredByAura.getBase().setDuration(0);
            }

            if (param1 != 0 || param2 != 0) {
                sendCastResult(result, param1, param2);
            } else {
                sendCastResult(result);
            }

            // queue autorepeat spells for future repeating
            if (getCurrentContainer() == CurrentSpellType.AutoRepeat && caster.isUnit()) {
                caster.toUnit().setCurrentCastSpell(this);
            }

            finish(result);

            return result;
        }

        // Prepare data for triggers
        prepareDataForTriggerSystem();

        casttime = callScriptCalcCastTimeHandlers(spellInfo.calcCastTime(this));

        for (var stage : empowerStages.entrySet()) {
            var ct = (int) stage.getValue().durationMs;
            tangible.RefObject<Integer> tempRef_ct = new tangible.RefObject<Integer>(ct);
            getCaster().modSpellCastTime(spellInfo, tempRef_ct);
            ct = tempRef_ct.refArgValue;
            stage.getValue().durationMs = (int) callScriptCalcCastTimeHandlers(ct);
        }

        if (caster.isUnit() && caster.toUnit().isMoving()) {
            result = checkMovement();

            if (result != SpellCastResult.SpellCastOk) {
                sendCastResult(result);
                finish(result);

                return result;
            }
        }

        // Creatures focus their target when possible
        if (casttime != 0 && caster.isCreature() && !spellInfo.isNextMeleeSwingSpell() && !isAutoRepeat() && !caster.toUnit().hasUnitFlag(UnitFlag.Possessed)) {
            // Channeled spells and some triggered spells do not focus a cast target. They face their target later on via channel object guid and via spell attribute or not at all
            var focusTarget = !spellInfo.isChanneled() && !triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreSetFacing);

            if (focusTarget && targets.getObjectTarget() && caster != targets.getObjectTarget()) {
                caster.toCreature().setSpellFocus(this, targets.getObjectTarget());
            } else {
                caster.toCreature().setSpellFocus(this, null);
            }
        }

        callScriptOnPrecastHandler();

        // set timer base at cast time
        reSetTimer();

        Log.outDebug(LogFilter.spells, "Spell.prepare: spell id {0} source {1} caster {2} customCastFlags {3} mask {4}", spellInfo.getId(), caster.getEntry(), originalCaster != null ? (int) originalCaster.getEntry() : -1, triggeredCastFlags, targets.getTargetMask());

        if (spellInfo.hasAttribute(SpellAttr12.StartCooldownOnCastStart)) {
            sendSpellCooldown();
        }

        //Containers for channeled spells have to be set
        // @todoApply this to all casted spells if needed
        // Why check duration? 29350: channelled triggers channelled
        if (triggeredCastFlags.hasFlag(TriggerCastFlag.CastDirectly) && (!spellInfo.isChanneled() || spellInfo.getMaxDuration() == 0)) {
            cast(true);
        } else {
            // commented out !m_spellInfo->startRecoveryTime, it forces instant spells with global cooldown to be processed in spell::update
            // as a result a spell that passed CheckCast and should be processed instantly may suffer from this delayed process
            // the easiest bug to observe is LoS check in AddUnitTarget, even if spell passed the CheckCast LoS check the situation can change in spell::update
            // because target could be relocated in the meantime, making the spell fly to the air (no targets can be registered, so no effects processed, nothing in combat log)
            var willCastDirectly = casttime == 0 && getCurrentContainer() == CurrentSpellType.generic;

            var unitCaster = caster.toUnit();

            if (unitCaster != null) {
                // stealth must be removed at cast starting (at show channel bar)
                // skip triggered spell (item equip spell casting and other not explicit character casts/item uses)
                if (!triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreAuraInterruptFlags) && !spellInfo.hasAttribute(SpellAttr2.NotAnAction)) {
                    unitCaster.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.action, spellInfo);
                }

                // Do not register as current spell when requested to ignore cast in progress
                // We don't want to interrupt that other spell with cast time
                if (!willCastDirectly || !triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreCastInProgress)) {
                    unitCaster.setCurrentCastSpell(this);
                }
            }

            sendSpellStart();

            if (!triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreGCD)) {
                triggerGlobalCooldown();
            }

            // Call CreatureAI hook OnSpellStart
            var caster = caster.toCreature();

            if (caster != null) {
                if (caster.isAIEnabled()) {
                    caster.getAI().onSpellStart(spellInfo);
                }
            }

            if (willCastDirectly) {
                cast(true);
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    public final void cancel() {
        if (spellState == SpellState.Finished) {
            return;
        }

        var oldState = spellState;
        spellState = SpellState.Finished;

        autoRepeat = false;

        switch (oldState) {
            case Preparing:
                cancelGlobalCooldown();
            case Delayed:
                sendInterrupted((byte) 0);
                sendCastResult(SpellCastResult.Interrupted);

                break;

            case Casting:
                for (var ihit : uniqueTargetInfo) {
                    if (ihit.missCondition == SpellMissInfo.NONE) {
                        var unit = Objects.equals(caster.getGUID(), ihit.targetGuid) ? caster.toUnit() : global.getObjAccessor().GetUnit(caster, ihit.targetGuid);

                        if (unit != null) {
                            unit.removeOwnedAura(spellInfo.getId(), originalCasterGuid, AuraRemoveMode.Cancel);
                        }
                    }
                }

                endEmpoweredSpell();
                sendChannelUpdate(0);
                sendInterrupted((byte) 0);
                sendCastResult(SpellCastResult.Interrupted);

                appliedMods.clear();
                break;

            default:
                break;
        }

        setReferencedFromCurrent(false);

        if (selfContainer != null && selfContainer == this) {
            selfContainer = null;
        }

        // originalcaster handles gameobjects/dynobjects for gob caster
        if (originalCaster != null) {
            originalCaster.removeDynObject(spellInfo.getId());

            if (spellInfo.isChanneled()) // if not channeled then the object for the current cast wasn't summoned yet
            {
                originalCaster.removeGameObject(spellInfo.getId(), true);
            }
        }

        //set state back so finish will be processed
        spellState = oldState;

        finish(SpellCastResult.Interrupted);
    }

    public final void cast() {
        cast(false);
    }

    public final void cast(boolean skipCheck) {
        var modOwner = caster.getSpellModOwner();
        Spell lastSpellMod = null;

        if (modOwner) {
            lastSpellMod = modOwner.getSpellModTakingSpell();

            if (lastSpellMod) {
                modOwner.setSpellModTakingSpell(lastSpellMod, false);
            }
        }

        cast(skipCheck);

        if (lastSpellMod) {
            modOwner.setSpellModTakingSpell(lastSpellMod, true);
        }
    }

    public final long handleDelayed(long offset) {
        if (!updatePointers()) {
            // finish the spell if updatePointers() returned false, something wrong happened there
            finish(SpellCastResult.Fizzle);

            return 0;
        }

        var single_missile = targets.getHasDst();
        long next_time = 0;

        if (!launchHandled) {
            var launchMoment = (long) Math.floor(spellInfo.getLaunchDelay() * 1000.0f);

            if (launchMoment > offset) {
                return launchMoment;
            }

            handleLaunchPhase();
            launchHandled = true;

            if (delayMoment > offset) {
                if (single_missile) {
                    return delayMoment;
                }

                next_time = delayMoment;

                if ((uniqueTargetInfo.size() > 2 || (uniqueTargetInfo.size() == 1 && Objects.equals(uniqueTargetInfo.get(0).targetGuid, caster.getGUID()))) || !uniqueGoTargetInfo.isEmpty()) {
                    offset = 0; // if LaunchDelay was present then the only target that has timeDelay = 0 is m_caster - and that is the only target we want to process now
                }
            }
        }

        if (single_missile && offset == 0) {
            return delayMoment;
        }

        var modOwner = caster.getSpellModOwner();

        if (modOwner != null) {
            modOwner.setSpellModTakingSpell(this, true);
        }

        prepareTargetProcessing();

        if (!immediateHandled && offset != 0) {
            handle_immediate_phase();
            immediateHandled = true;
        }

        {
            // now recheck units targeting correctness (need before any effects apply to prevent adding immunity at first effect not allow apply second spell effect and similar cases)
            ArrayList<TargetInfo> delayedTargets = new ArrayList<>();

            tangible.ListHelper.removeAll(uniqueTargetInfo, target ->
            {
                if (single_missile || target.timeDelay <= offset) {
                    target.timeDelay = offset;
                    delayedTargets.add(target);

                    return true;
                } else if (next_time == 0 || target.timeDelay < next_time) {
                    next_time = target.timeDelay;
                }

                return false;
            });

            doProcessTargetContainer(delayedTargets);

            if (next_time == 0) {
                callScriptOnHitHandlers();
            }
        }

        {
            // now recheck gameobject targeting correctness
            ArrayList<GOTargetInfo> delayedGOTargets = new ArrayList<>();

            tangible.ListHelper.removeAll(uniqueGoTargetInfo, goTarget ->
            {
                if (single_missile || goTarget.timeDelay <= offset) {
                    goTarget.timeDelay = offset;
                    delayedGOTargets.add(goTarget);

                    return true;
                } else if (next_time == 0 || goTarget.timeDelay < next_time) {
                    next_time = goTarget.timeDelay;
                }

                return false;
            });

            doProcessTargetContainer(delayedGOTargets);
        }

        finishTargetProcessing();

        if (modOwner) {
            modOwner.setSpellModTakingSpell(this, false);
        }

        // All targets passed - need finish phase
        if (next_time == 0) {
            // spell is finished, perform some last features of the spell here
            handle_finish_phase();

            finish(); // successfully finish spell cast

            // return zero, spell is finished now
            return 0;
        } else {
            // spell is unfinished, return next execution time
            return next_time;
        }
    }

    public final void update(int difftime) {
        if (!updatePointers()) {
            // cancel the spell if updatePointers() returned false, something wrong happened there
            cancel();

            return;
        }

        if (!targets.getUnitTargetGUID().isEmpty() && targets.getUnitTarget() == null) {
            Log.outDebug(LogFilter.spells, "Spell {0} is cancelled due to removal of target.", spellInfo.getId());
            cancel();

            return;
        }

        // check if the player caster has moved before the spell finished
        // with the exception of spells affected with SPELL_AURA_CAST_WHILE_WALKING effect
        if (timer != 0 && caster.isUnit() && caster.toUnit().isMoving() && checkMovement() != SpellCastResult.SpellCastOk) {
            // if charmed by creature, trust the AI not to cheat and allow the cast to proceed
            // @todo this is a hack, "creature" movesplines don't differentiate turning/moving right now
            // however, checking what type of movement the spline is for every single spline would be really expensive
            if (!caster.toUnit().getCharmerGUID().isCreature()) {
                cancel();
            }
        }

        switch (spellState) {
            case Preparing: {
                if (timer > 0) {
                    if (difftime >= timer) {
                        timer = 0;
                    } else {
                        _timer -= (int) difftime;
                    }
                }

                if (timer == 0 && !spellInfo.isNextMeleeSwingSpell()) {
                    // don't CheckCast for instant spells - done in spell.prepare, skip duplicate checks, needed for range checks for example
                    cast(casttime == 0);
                }

                break;
            }
            case Casting: {
                if (timer != 0) {
                    // check if there are alive targets left
                    if (!updateChanneledTargetList()) {
                        Log.outDebug(LogFilter.spells, "Channeled spell {0} is removed due to lack of targets", spellInfo.getId());
                        timer = 0;

                        // Also remove applied auras
                        for (var target : uniqueTargetInfo) {
                            var unit = Objects.equals(caster.getGUID(), target.targetGuid) ? caster.toUnit() : global.getObjAccessor().GetUnit(caster, target.targetGuid);

                            if (unit) {
                                unit.removeOwnedAura(spellInfo.getId(), originalCasterGuid, AuraRemoveMode.Cancel);
                            }
                        }
                    }

                    if (timer > 0) {
                        updateEmpoweredSpell(difftime);

                        if (difftime >= timer) {
                            timer = 0;
                        } else {
                            _timer -= (int) difftime;
                        }
                    }
                }

                if (timer == 0) {
                    endEmpoweredSpell();
                    sendChannelUpdate(0);
                    finish();

                    // We call the hook here instead of in Spell::finish because we only want to call it for completed channeling. Everything else is handled by interrupts
                    var creatureCaster = caster.toCreature();

                    if (creatureCaster != null) {
                        if (creatureCaster.isAIEnabled()) {
                            creatureCaster.getAI().onChannelFinished(spellInfo);
                        }
                    }
                }

                break;
            }
            default:
                break;
        }
    }

    public final void endEmpoweredSpell() {
        Player p;
        tangible.OutObject<Player> tempOut_p = new tangible.OutObject<Player>();
        if (getPlayerIfIsEmpowered(tempOut_p) && (spellInfo.getEmpowerStages().containsKey(empoweredSpellStage) && (var
        stageinfo = spellInfo.getEmpowerStages().get(empoweredSpellStage)) ==var stageinfo)) // ensure stage is valid
        {
            p = tempOut_p.outArgValue;
            var duration = spellInfo.getDuration();
            var timeCasted = spellInfo.getDuration() - timer;

            if (MathUtil.GetPctOf(timeCasted, duration) < p.getEmpoweredSpellMinHoldPct()) // ensure we held for long enough
            {
                return;
            }

            this.<ISpellOnEpowerSpellEnd>ForEachSpellScript(s -> s.EmpowerSpellEnd(stageinfo, empoweredSpellDelta));
            var stageUpdate = new SpellEmpowerStageUpdate();
            stageUpdate.caster = p.getGUID();
            stageUpdate.castID = castId;
            stageUpdate.timeRemaining = timer;
            var unusedDurations = new ArrayList<>();

            var nextStage = empoweredSpellStage;
            nextStage++;

            while (spellInfo.getEmpowerStages().containsKey(nextStage) && (var
            nextStageinfo = spellInfo.getEmpowerStages().get(nextStage)) ==var nextStageinfo)
            {
                unusedDurations.add(nextStageinfo.durationMs);
                nextStage++;
            }

            stageUpdate.remainingStageDurations = unusedDurations;
            p.sendPacket(stageUpdate);
        }
	else
        {
            p = tempOut_p.outArgValue;
        }
    }

    public final void finish() {
        finish(SpellCastResult.SpellCastOk);
    }

    public final void finish(SpellCastResult result) {
        if (spellState == SpellState.Finished) {
            return;
        }

        spellState = SpellState.Finished;

        if (!caster) {
            return;
        }

        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return;
        }

        // successful cast of the initial autorepeat spell is moved to idle state so that it is not deleted as long as autorepeat is active
        if (isAutoRepeat() && unitCaster.getCurrentSpell(CurrentSpellType.AutoRepeat) == this) {
            spellState = SpellState.IDLE;
        }

        if (spellInfo.isChanneled()) {
            unitCaster.updateInterruptMask();
        }

        if (unitCaster.hasUnitState(UnitState.Casting) && !unitCaster.isNonMeleeSpellCast(false, false, true)) {
            unitCaster.clearUnitState(UnitState.Casting);
        }

        // Unsummon summon as possessed creatures on spell cancel
        if (spellInfo.isChanneled() && unitCaster.isTypeId(TypeId.PLAYER)) {
            var charm = unitCaster.getCharmed();

            if (charm != null) {
                if (charm.isTypeId(TypeId.UNIT) && charm.toCreature().hasUnitTypeMask(UnitTypeMask.Puppet) && charm.getUnitData().createdBySpell == spellInfo.getId()) {
                    ((Puppet) charm).unSummon();
                }
            }
        }

        var creatureCaster = unitCaster.toCreature();

        if (creatureCaster != null) {
            creatureCaster.releaseSpellFocus(this);
        }

        if (!spellInfo.hasAttribute(SpellAttr3.SuppressCasterProcs)) {
            unit.procSkillsAndAuras(unitCaster, null, new ProcFlagsInit(procFlags.CastEnded), new ProcFlagsInit(), ProcFlagsSpellType.MaskAll, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, this, null, null);
        }

        if (result != SpellCastResult.SpellCastOk) {
            // on failure (or manual cancel) send TraitConfigCommitFailed to revert talent UI saved config selection
            if (caster.isPlayer() && spellInfo.hasEffect(SpellEffectName.ChangeActiveCombatTraitConfig)) {
                if (CustomArg instanceof TraitConfig) {
                    caster.toPlayer().sendPacket(new TraitConfigCommitFailed((CustomArg instanceof TraitConfig ? (TraitConfig) CustomArg : null).ID));
                }
            }

            return;
        }

        if (unitCaster.isTypeId(TypeId.UNIT) && unitCaster.toCreature().isSummon()) {
            // Unsummon statue
            int spell = unitCaster.getUnitData().createdBySpell;
            var spellInfo = global.getSpellMgr().getSpellInfo(spell, getCastDifficulty());

            if (spellInfo != null && spellInfo.getIconFileDataId() == 134230) {
                Log.outDebug(LogFilter.spells, "Statue {0} is unsummoned in spell {1} finish", unitCaster.getGUID().toString(), spellInfo.getId());

                // Avoid infinite loops with setDeathState(JUST_DIED) being called over and over
                // It might make sense to do this check in Unit::setDeathState() and all overloaded functions
                if (unitCaster.deathState != deathState.JustDied) {
                    unitCaster.setDeathState(deathState.JustDied);
                }

                return;
            }
        }

        if (isAutoActionResetSpell()) {
            if (!spellInfo.hasAttribute(SpellAttr2.DoNotResetCombatTimers)) {
                unitCaster.resetAttackTimer(WeaponAttackType.BaseAttack);

                if (unitCaster.haveOffhandWeapon()) {
                    unitCaster.resetAttackTimer(WeaponAttackType.OffAttack);
                }

                unitCaster.resetAttackTimer(WeaponAttackType.RangedAttack);
            }
        }

        // potions disabled by client, send event "not in combat" if need
        if (unitCaster.isTypeId(TypeId.PLAYER)) {
            if (triggeredByAuraSpell == null) {
                unitCaster.toPlayer().updatePotionCooldown(this);
            }
        }

        // Stop Attack for some spells
        if (spellInfo.hasAttribute(SpellAttr0.CancelsAutoAttackCombat)) {
            unitCaster.attackStop();
        }
    }

    public final void sendCastResult(SpellCastResult result, Integer param1) {
        sendCastResult(result, param1, null);
    }

    public final void sendCastResult(SpellCastResult result) {
        sendCastResult(result, null, null);
    }

    public final void sendCastResult(SpellCastResult result, Integer param1, Integer param2) {
        if (result == SpellCastResult.SpellCastOk) {
            return;
        }

        if (!caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        if (caster.toPlayer().isLoading()) // don't send cast results at loading time
        {
            return;
        }

        if (triggeredCastFlags.hasFlag(TriggerCastFlag.DontReportCastError)) {
            result = SpellCastResult.DontReport;
        }

        CastFailed castFailed = new CastFailed();
        castFailed.visual = spellVisual;
        fillSpellCastFailedArgs(castFailed, castId, spellInfo, result, customErrors, param1, param2, caster.toPlayer());
        caster.toPlayer().sendPacket(castFailed);
    }

    public final void sendPetCastResult(SpellCastResult result, Integer param1) {
        sendPetCastResult(result, param1, null);
    }

    public final void sendPetCastResult(SpellCastResult result) {
        sendPetCastResult(result, null, null);
    }

    public final void sendPetCastResult(SpellCastResult result, Integer param1, Integer param2) {
        if (result == SpellCastResult.SpellCastOk) {
            return;
        }

        var owner = caster.getCharmerOrOwner();

        if (!owner || !owner.isTypeId(TypeId.PLAYER)) {
            return;
        }

        if (triggeredCastFlags.hasFlag(TriggerCastFlag.DontReportCastError)) {
            result = SpellCastResult.DontReport;
        }

        PetCastFailed petCastFailed = new PetCastFailed();
        fillSpellCastFailedArgs(petCastFailed, castId, spellInfo, result, SpellCustomErrors.NONE, param1, param2, owner.toPlayer());
        owner.toPlayer().sendPacket(petCastFailed);
    }

    public final SpellLogEffect getExecuteLogEffect(SpellEffectName effect) {
        var spellLogEffect = executeLogEffects.get(effect);

        if (spellLogEffect != null) {
            return spellLogEffect;
        }

        SpellLogEffect executeLogEffect = new SpellLogEffect();
        executeLogEffect.effect = effect.getValue();
        executeLogEffects.put(effect, executeLogEffect);

        return executeLogEffect;
    }

    public final void sendChannelUpdate(int time) {
        // GameObjects don't channel
        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return;
        }

        if (time == 0) {
            unitCaster.clearChannelObjects();
            unitCaster.setChannelSpellId(0);
            unitCaster.setChannelVisual(new spellCastVisualField());
        }

        SpellChannelUpdate spellChannelUpdate = new SpellChannelUpdate();
        spellChannelUpdate.casterGUID = unitCaster.getGUID();
        spellChannelUpdate.timeRemaining = (int) time;
        unitCaster.sendMessageToSet(spellChannelUpdate, true);
    }

    public final void handleEffects(Unit pUnitTarget, Item pItemTarget, GameObject pGoTarget, Corpse pCorpseTarget, SpellEffectInfo spellEffectInfo, SpellEffectHandleMode mode) {
        effectHandleMode = mode;
        unitTarget = pUnitTarget;
        itemTarget = pItemTarget;
        gameObjTarget = pGoTarget;
        corpseTarget = pCorpseTarget;
        destTarget = destTargets.get(spellEffectInfo.effectIndex).position;
        effectInfo = spellEffectInfo;

        tangible.OutObject<Double> tempOut_Variance = new tangible.OutObject<Double>();
        damage = calculateDamage(spellEffectInfo, unitTarget, tempOut_Variance);
        variance = tempOut_Variance.outArgValue;

        var preventDefault = callScriptEffectHandlers(spellEffectInfo.effectIndex, mode);

        if (!preventDefault) {
            global.getSpellMgr().getSpellEffectHandler(spellEffectInfo.effect).invoke(this);
        }
    }

    public final SpellCastResult checkCast(boolean strict) {
        int param1 = 0, param2 = 0;

        tangible.RefObject<Integer> tempRef_param1 = new tangible.RefObject<Integer>(param1);
        tangible.RefObject<Integer> tempRef_param2 = new tangible.RefObject<Integer>(param2);
        var tempVar = checkCast(strict, tempRef_param1, tempRef_param2);
        param2 = tempRef_param2.refArgValue;
        param1 = tempRef_param1.refArgValue;
        return tempVar;
    }

    public final SpellCastResult checkCast(boolean strict, tangible.RefObject<Integer> param1, tangible.RefObject<Integer> param2) {
        SpellCastResult castResult;

        // check death state
        if (caster.toUnit() && !caster.toUnit().isAlive() && !spellInfo.isPassive() && !(spellInfo.hasAttribute(SpellAttr0.AllowCastWhileDead) || (isTriggered() && triggeredByAuraSpell == null))) {
            return SpellCastResult.CasterDead;
        }

        // Prevent cheating in case the player has an immunity effect and tries to interact with a non-allowed gameobject. The error message is handled by the client so we don't report anything here
        if (caster.isPlayer() && targets.getGOTarget() != null) {
            if (targets.getGOTarget().getTemplate().getNoDamageImmune() != 0 && caster.toUnit().hasUnitFlag(UnitFlag.Immune)) {
                return SpellCastResult.DontReport;
            }
        }

        // check cooldowns to prevent cheating
        if (!spellInfo.isPassive()) {
            var playerCaster = caster.toPlayer();

            if (playerCaster != null) {
                //can cast triggered (by aura only?) spells while have this flag
                if (!triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreCasterAurastate)) {
                    // These two auras check SpellFamilyName defined by db2 class data instead of current spell SpellFamilyName
                    if (playerCaster.hasAuraType(AuraType.DisableCastingExceptAbilities) && !spellInfo.hasAttribute(SpellAttr0.UsesRangedSlot) && !spellInfo.hasEffect(SpellEffectName.attack) && !spellInfo.hasAttribute(SpellAttr12.IgnoreCastingDisabled) && !playerCaster.hasAuraTypeWithFamilyFlags(AuraType.DisableCastingExceptAbilities, CliDB.ChrClassesStorage.get(playerCaster.getClass()).SpellClassSet, spellInfo.getSpellFamilyFlags())) {
                        return SpellCastResult.CantDoThatRightNow;
                    }

                    if (playerCaster.hasAuraType(AuraType.DisableAttackingExceptAbilities)) {
                        if (!playerCaster.hasAuraTypeWithFamilyFlags(AuraType.DisableAttackingExceptAbilities, CliDB.ChrClassesStorage.get(playerCaster.getClass()).SpellClassSet, spellInfo.getSpellFamilyFlags())) {
                            if (spellInfo.hasAttribute(SpellAttr0.UsesRangedSlot) || spellInfo.isNextMeleeSwingSpell() || spellInfo.hasAttribute(SpellAttr1.InitiatesCombatEnablesAutoAttack) || spellInfo.hasAttribute(SpellAttr2.InitiateCombatPostCastEnablesAutoAttack) || spellInfo.hasEffect(SpellEffectName.attack) || spellInfo.hasEffect(SpellEffectName.NormalizedWeaponDmg) || spellInfo.hasEffect(SpellEffectName.WeaponDamageNoSchool) || spellInfo.hasEffect(SpellEffectName.WeaponPercentDamage) || spellInfo.hasEffect(SpellEffectName.weaponDamage)) {
                                return SpellCastResult.CantDoThatRightNow;
                            }
                        }
                    }
                }

                // check if we are using a potion in combat for the 2nd+ time. Cooldown is added only after caster gets out of combat
                if (!isIgnoringCooldowns() && playerCaster.getLastPotionId() != 0 && castItem && (castItem.isPotion() || spellInfo.isCooldownStartedOnEvent())) {
                    return SpellCastResult.NotReady;
                }
            }

            if (!isIgnoringCooldowns() && caster.toUnit() != null) {
                if (!caster.toUnit().getSpellHistory().isReady(spellInfo, castItemEntry)) {
                    if (triggeredByAuraSpell != null) {
                        return SpellCastResult.DontReport;
                    } else {
                        return SpellCastResult.NotReady;
                    }
                }

                if ((isAutoRepeat() || spellInfo.getCategoryId() == 76) && !caster.toUnit().isAttackReady(WeaponAttackType.RangedAttack)) {
                    return SpellCastResult.DontReport;
                }
            }
        }

        if (spellInfo.hasAttribute(SpellAttr7.IsCheatSpell) && caster.isUnit() && !caster.toUnit().hasUnitFlag2(UnitFlag2.AllowCheatSpells)) {
            customErrors = SpellCustomErrors.GmOnly;

            return SpellCastResult.CustomError;
        }

        // Check global cooldown
        if (strict && !(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreGCD.getValue()) && hasGlobalCooldown()) {
            return !spellInfo.hasAttribute(SpellAttr0.CooldownOnEvent) ? SpellCastResult.NotReady : SpellCastResult.DontReport;
        }

        // only triggered spells can be processed an ended Battleground
        if (!isTriggered() && caster.isTypeId(TypeId.PLAYER)) {
            var bg = caster.toPlayer().getBattleground();

            if (bg) {
                if (bg.getStatus() == BattlegroundStatus.WaitLeave) {
                    return SpellCastResult.DontReport;
                }
            }
        }

        if (caster.isTypeId(TypeId.PLAYER) && global.getVMapMgr().isLineOfSightCalcEnabled()) {
            if (spellInfo.hasAttribute(SpellAttr0.OnlyOutdoors) && !caster.isOutdoors()) {
                return SpellCastResult.OnlyOutdoors;
            }

            if (spellInfo.hasAttribute(SpellAttr0.OnlyIndoors) && caster.isOutdoors()) {
                return SpellCastResult.OnlyIndoors;
            }
        }

        var unitCaster = caster.toUnit();

        if (unitCaster != null) {
            if (spellInfo.hasAttribute(SpellAttr5.NotAvailableWhileCharmed) && unitCaster.isCharmed()) {
                return SpellCastResult.Charmed;
            }

            // only check at first call, Stealth auras are already removed at second call
            // for now, ignore triggered spells
            if (strict && !triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreShapeshift)) {
                var checkForm = true;
                // Ignore form req aura
                var ignore = unitCaster.getAuraEffectsByType(AuraType.ModIgnoreShapeshift);

                for (var aurEff : ignore) {
                    if (!aurEff.isAffectingSpell(spellInfo)) {
                        continue;
                    }

                    checkForm = false;

                    break;
                }

                if (checkForm) {
                    // Cannot be used in this stance/form
                    var shapeError = spellInfo.checkShapeshift(unitCaster.getShapeshiftForm());

                    if (shapeError != SpellCastResult.SpellCastOk) {
                        return shapeError;
                    }

                    if (spellInfo.hasAttribute(SpellAttr0.OnlyStealthed) && !unitCaster.getHasStealthAura()) {
                        return SpellCastResult.OnlyStealthed;
                    }
                }
            }

            var reqCombat = true;
            var stateAuras = unitCaster.getAuraEffectsByType(AuraType.AbilityIgnoreAurastate);

            for (var aura : stateAuras) {
                if (aura.isAffectingSpell(spellInfo)) {
                    needComboPoints = false;

                    if (aura.getMiscValue() == 1) {
                        reqCombat = false;

                        break;
                    }
                }
            }

            // caster state requirements
            // not for triggered spells (needed by execute)
            if (!triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreCasterAurastate)) {
                if (spellInfo.getCasterAuraState() != 0 && !unitCaster.hasAuraState(spellInfo.getCasterAuraState(), spellInfo, unitCaster)) {
                    return SpellCastResult.CasterAurastate;
                }

                if (spellInfo.getExcludeCasterAuraState() != 0 && unitCaster.hasAuraState(spellInfo.getExcludeCasterAuraState(), spellInfo, unitCaster)) {
                    return SpellCastResult.CasterAurastate;
                }

                // Note: spell 62473 requres casterAuraSpell = triggering spell
                if (spellInfo.getCasterAuraSpell() != 0 && !unitCaster.hasAura(spellInfo.getCasterAuraSpell())) {
                    return SpellCastResult.CasterAurastate;
                }

                if (spellInfo.getExcludeCasterAuraSpell() != 0 && unitCaster.hasAura(spellInfo.getExcludeCasterAuraSpell())) {
                    return SpellCastResult.CasterAurastate;
                }

                if (spellInfo.getCasterAuraType() != 0 && !unitCaster.hasAuraType(spellInfo.getCasterAuraType())) {
                    return SpellCastResult.CasterAurastate;
                }

                if (spellInfo.getExcludeCasterAuraType() != 0 && unitCaster.hasAuraType(spellInfo.getExcludeCasterAuraType())) {
                    return SpellCastResult.CasterAurastate;
                }

                if (reqCombat && unitCaster.isInCombat() && !spellInfo.getCanBeUsedInCombat()) {
                    return SpellCastResult.AffectingCombat;
                }
            }

            // Check vehicle flags
            if (!(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreCasterMountedOrOnVehicle.getValue())) {
                var vehicleCheck = spellInfo.checkVehicle(unitCaster);

                if (vehicleCheck != SpellCastResult.SpellCastOk) {
                    return vehicleCheck;
                }
            }
        }

        {
            // check spell cast conditions from database
            ConditionSourceInfo condInfo = new ConditionSourceInfo(caster, targets.getObjectTarget());

            if (!global.getConditionMgr().isObjectMeetingNotGroupedConditions(ConditionSourceType.spell, spellInfo.getId(), condInfo)) {
                // mLastFailedCondition can be NULL if there was an error processing the condition in condition.Meets (i.e. wrong data for ConditionTarget or others)
                if (condInfo.mLastFailedCondition != null && condInfo.mLastFailedCondition.errorType != 0) {
                    if (condInfo.mLastFailedCondition.errorType == (int) SpellCastResult.CustomError.getValue()) {
                        customErrors = SpellCustomErrors.forValue(condInfo.mLastFailedCondition.errorTextId);
                    }

                    return SpellCastResult.forValue(condInfo.mLastFailedCondition.errorType);
                }

                if (condInfo.mLastFailedCondition == null || condInfo.mLastFailedCondition.conditionTarget == 0) {
                    return SpellCastResult.CasterAurastate;
                }

                return SpellCastResult.BadTargets;
            }
        }

        // Don't check explicit target for passive spells (workaround) (check should be skipped only for learn case)
        // those spells may have incorrect target entries or not filled at all (for example 15332)
        // such spells when learned are not targeting anyone using targeting system, they should apply directly to caster instead
        // also, such casts shouldn't be sent to client
        if (!(spellInfo.isPassive() && (targets.getUnitTarget() == null || targets.getUnitTarget() == caster))) {
            // Check explicit target for m_originalCaster - todo: get rid of such workarounds
            var caster = caster;

            // in case of gameobjects like traps, we need the gameobject itself to check target validity
            // otherwise, if originalCaster is far away and cannot detect the target, the trap would not hit the target
            if (originalCaster != null && !caster.isGameObject()) {
                caster = originalCaster;
            }

            castResult = spellInfo.checkExplicitTarget(caster, targets.getObjectTarget(), targets.getItemTarget());

            if (castResult != SpellCastResult.SpellCastOk) {
                return castResult;
            }
        }

        var unitTarget = targets.getUnitTarget();

        if (unitTarget != null) {
            castResult = spellInfo.checkTarget(caster, unitTarget, caster.isGameObject()); // skip stealth checks for GO casts

            if (castResult != SpellCastResult.SpellCastOk) {
                return castResult;
            }

            // If it's not a melee spell, check if vision is obscured by SPELL_AURA_INTERFERE_TARGETTING
            if (spellInfo.getDmgClass() != SpellDmgClass.Melee) {
                var unitCaster1 = caster.toUnit();

                if (unitCaster1 != null) {
                    for (var auraEffect : unitCaster1.getAuraEffectsByType(AuraType.InterfereTargetting)) {
                        if (!unitCaster1.isFriendlyTo(auraEffect.getCaster()) && !unitTarget.hasAura(auraEffect.getId(), auraEffect.getCasterGuid())) {
                            return SpellCastResult.VisionObscured;
                        }
                    }

                    for (var auraEffect : unitTarget.getAuraEffectsByType(AuraType.InterfereTargetting)) {
                        if (!unitCaster1.isFriendlyTo(auraEffect.getCaster()) && (!unitTarget.hasAura(auraEffect.getId(), auraEffect.getCasterGuid()) || !unitCaster1.hasAura(auraEffect.getId(), auraEffect.getCasterGuid()))) {
                            return SpellCastResult.VisionObscured;
                        }
                    }
                }
            }

            if (unitTarget != caster) {
                // Must be behind the target
                if (spellInfo.hasAttribute(SpellCustomAttributes.ReqCasterBehindTarget) && unitTarget.getLocation().hasInArc(MathUtil.PI, caster.getLocation())) {
                    return SpellCastResult.NotBehind;
                }

                // Target must be facing you
                if (spellInfo.hasAttribute(SpellCustomAttributes.ReqTargetFacingCaster) && !unitTarget.getLocation().hasInArc(MathUtil.PI, caster.getLocation())) {
                    return SpellCastResult.NotInfront;
                }

                // Ignore LOS for gameobjects casts
                if (!caster.isGameObject()) {
                    var losTarget = caster;

                    if (isTriggered() && triggeredByAuraSpell != null) {
                        var dynObj = caster.toUnit().getDynObject(triggeredByAuraSpell.getId());

                        if (dynObj) {
                            losTarget = dynObj;
                        }
                    }

                    if (!spellInfo.hasAttribute(SpellAttr2.IgnoreLineOfSight) && !global.getDisableMgr().isDisabledFor(DisableType.spell, spellInfo.getId(), null, (byte) DisableFlags.SPELLLOS.getValue()) && !unitTarget.isWithinLOSInMap(losTarget, LineOfSightChecks.All, ModelIgnoreFlags.M2)) {
                        return SpellCastResult.LineOfSight;
                    }
                }
            }
        }

        // Check for line of sight for spells with dest
        if (targets.getHasDst()) {
            if (!spellInfo.hasAttribute(SpellAttr2.IgnoreLineOfSight) && !global.getDisableMgr().isDisabledFor(DisableType.spell, spellInfo.getId(), null, (byte) DisableFlags.SPELLLOS.getValue()) && !caster.isWithinLOS(targets.getDstPos(), LineOfSightChecks.All, ModelIgnoreFlags.M2)) {
                return SpellCastResult.LineOfSight;
            }
        }

        // check pet presence
        if (unitCaster != null) {
            if (spellInfo.hasAttribute(SpellAttr2.NoActivePets)) {
                if (!unitCaster.getPetGUID().isEmpty()) {
                    return SpellCastResult.AlreadyHavePet;
                }
            }

            for (var spellEffectInfo : spellInfo.getEffects()) {
                if (spellEffectInfo.targetA.getTarget() == targets.UnitPet) {
                    if (unitCaster.getGuardianPet() == null) {
                        if (triggeredByAuraSpell != null) // not report pet not existence for triggered spells
                        {
                            return SpellCastResult.DontReport;
                        } else {
                            return SpellCastResult.NoPet;
                        }
                    }

                    break;
                }
            }
        }

        // Spell casted only on Battleground
        if (spellInfo.hasAttribute(SpellAttr3.OnlyBattlegrounds)) {
            if (!caster.getMap().isBattleground()) {
                return SpellCastResult.OnlyBattlegrounds;
            }
        }

        // do not allow spells to be cast in arenas or rated Battlegrounds
        var player = caster.toPlayer();

        if (player != null) {
            if (player.getInArena()) {
                castResult = checkArenaAndRatedBattlegroundCastRules();

                if (castResult != SpellCastResult.SpellCastOk) {
                    return castResult;
                }
            }
        }

        // zone check
        if (!caster.isPlayer() || !caster.toPlayer().isGameMaster()) {
            int zone;
            tangible.OutObject<Integer> tempOut_zone = new tangible.OutObject<Integer>();
            int area;
            tangible.OutObject<Integer> tempOut_area = new tangible.OutObject<Integer>();
            caster.getZoneAndAreaId(tempOut_zone, tempOut_area);
            area = tempOut_area.outArgValue;
            zone = tempOut_zone.outArgValue;

            var locRes = spellInfo.checkLocation(caster.getLocation().getMapId(), zone, area, caster.toPlayer());

            if (locRes != SpellCastResult.SpellCastOk) {
                return locRes;
            }
        }

        // not let players cast spells at mount (and let do it to creatures)
        if (!triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreCasterMountedOrOnVehicle)) {
            if (caster.isPlayer() && caster.toPlayer().isMounted() && !spellInfo.isPassive() && !spellInfo.hasAttribute(SpellAttr0.AllowWhileMounted)) {
                if (caster.toPlayer().isInFlight()) {
                    return SpellCastResult.NotOnTaxi;
                } else {
                    return SpellCastResult.NotMounted;
                }
            }
        }

        // check spell focus object
        if (spellInfo.getRequiresSpellFocus() != 0) {
            if (!caster.isUnit() || !caster.toUnit().hasAuraTypeWithMiscvalue(AuraType.ProvideSpellFocus, (int) spellInfo.getRequiresSpellFocus())) {
                focusObject = searchSpellFocus();

                if (!focusObject) {
                    return SpellCastResult.requiresSpellFocus;
                }
            }
        }

        // always (except passive spells) check items (focus object can be required for any type casts)
        if (!spellInfo.isPassive()) {
            castResult = checkItems(param1, param2);

            if (castResult != SpellCastResult.SpellCastOk) {
                return castResult;
            }
        }

        // Triggered spells also have range check
        // @todo determine if there is some flag to enable/disable the check
        castResult = checkRange(strict);

        if (castResult != SpellCastResult.SpellCastOk) {
            return castResult;
        }

        if (!(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnorePowerAndReagentCost.getValue())) {
            castResult = checkPower();

            if (castResult != SpellCastResult.SpellCastOk) {
                return castResult;
            }
        }

        if (!(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreCasterAuras.getValue())) {
            castResult = checkCasterAuras(param1);

            if (castResult != SpellCastResult.SpellCastOk) {
                return castResult;
            }
        }

        // script hook
        castResult = callScriptCheckCastHandlers();

        if (castResult != SpellCastResult.SpellCastOk) {
            return castResult;
        }

        var approximateAuraEffectMask = new HashSet<Integer>();
        int nonAuraEffectMask = 0;

        for (var spellEffectInfo : spellInfo.getEffects()) {
            // for effects of spells that have only one target
            switch (spellEffectInfo.effect) {
                case Dummy: {
                    if (spellInfo.getId() == 19938) // Awaken Peon
                    {
                        var unit = targets.getUnitTarget();

                        if (unit == null || !unit.hasAura(17743)) {
                            return SpellCastResult.BadTargets;
                        }
                    } else if (spellInfo.getId() == 31789) // Righteous Defense
                    {
                        if (!caster.isTypeId(TypeId.PLAYER)) {
                            return SpellCastResult.DontReport;
                        }

                        var target = targets.getUnitTarget();

                        if (target == null || !target.isFriendlyTo(caster) || target.getAttackers().isEmpty()) {
                            return SpellCastResult.BadTargets;
                        }
                    }

                    break;
                }
                case LearnSpell: {
                    if (spellEffectInfo.targetA.getTarget() != targets.UnitPet) {
                        break;
                    }

                    var pet = caster.toPlayer().getCurrentPet();

                    if (pet == null) {
                        return SpellCastResult.NoPet;
                    }

                    var learn_spellproto = global.getSpellMgr().getSpellInfo(spellEffectInfo.triggerSpell, Difficulty.NONE);

                    if (learn_spellproto == null) {
                        return SpellCastResult.NotKnown;
                    }

                    if (spellInfo.getSpellLevel() > pet.getLevel()) {
                        return SpellCastResult.Lowlevel;
                    }

                    break;
                }
                case UnlockGuildVaultTab: {
                    if (!caster.isTypeId(TypeId.PLAYER)) {
                        return SpellCastResult.BadTargets;
                    }

                    var guild = caster.toPlayer().getGuild();

                    if (guild != null) {
                        if (ObjectGuid.opNotEquals(guild.getLeaderGUID(), caster.toPlayer().getGUID())) {
                            return SpellCastResult.CantDoThatRightNow;
                        }
                    }

                    break;
                }
                case LearnPetSpell: {
                    // check target only for unit target case
                    var target = targets.getUnitTarget();

                    if (target != null) {
                        if (!caster.isTypeId(TypeId.PLAYER)) {
                            return SpellCastResult.BadTargets;
                        }

                        var pet = target.getAsPet();

                        if (pet == null || pet.getOwningPlayer() != caster) {
                            return SpellCastResult.BadTargets;
                        }

                        var learn_spellproto = global.getSpellMgr().getSpellInfo(spellEffectInfo.triggerSpell, Difficulty.NONE);

                        if (learn_spellproto == null) {
                            return SpellCastResult.NotKnown;
                        }

                        if (spellInfo.getSpellLevel() > pet.getLevel()) {
                            return SpellCastResult.Lowlevel;
                        }
                    }

                    break;
                }
                case ApplyGlyph: {
                    if (!caster.isTypeId(TypeId.PLAYER)) {
                        return SpellCastResult.GlyphNoSpec;
                    }

                    var caster = caster.toPlayer();

                    if (!caster.hasSpell(spellMisc.spellId)) {
                        return SpellCastResult.NotKnown;
                    }

                    var glyphId = (int) spellEffectInfo.miscValue;

                    if (glyphId != 0) {
                        var glyphProperties = CliDB.GlyphPropertiesStorage.get(glyphId);

                        if (glyphProperties == null) {
                            return SpellCastResult.InvalidGlyph;
                        }

                        var glyphBindableSpells = global.getDB2Mgr().GetGlyphBindableSpells(glyphId);

                        if (glyphBindableSpells.isEmpty()) {
                            return SpellCastResult.InvalidGlyph;
                        }

                        if (!glyphBindableSpells.contains(spellMisc.spellId)) {
                            return SpellCastResult.InvalidGlyph;
                        }

                        var glyphRequiredSpecs = global.getDB2Mgr().GetGlyphRequiredSpecs(glyphId);

                        if (!glyphRequiredSpecs.isEmpty()) {
                            if (caster.getPrimarySpecialization() == 0) {
                                return SpellCastResult.GlyphNoSpec;
                            }

                            if (!glyphRequiredSpecs.contains(caster.getPrimarySpecialization())) {
                                return SpellCastResult.GlyphInvalidSpec;
                            }
                        }

                        int replacedGlyph = 0;

                        for (var activeGlyphId : caster.getGlyphs(caster.getActiveTalentGroup())) {
                            var activeGlyphBindableSpells = global.getDB2Mgr().GetGlyphBindableSpells(activeGlyphId);

                            if (!activeGlyphBindableSpells.isEmpty()) {
                                if (activeGlyphBindableSpells.contains(spellMisc.spellId)) {
                                    replacedGlyph = activeGlyphId;

                                    break;
                                }
                            }
                        }

                        for (var activeGlyphId : caster.getGlyphs(caster.getActiveTalentGroup())) {
                            if (activeGlyphId == replacedGlyph) {
                                continue;
                            }

                            if (activeGlyphId == glyphId) {
                                return SpellCastResult.UniqueGlyph;
                            }

                            if (CliDB.GlyphPropertiesStorage.get(activeGlyphId).GlyphExclusiveCategoryID == glyphProperties.GlyphExclusiveCategoryID) {
                                return SpellCastResult.GlyphExclusiveCategory;
                            }
                        }
                    }

                    break;
                }
                case FeedPet: {
                    if (!caster.isTypeId(TypeId.PLAYER)) {
                        return SpellCastResult.BadTargets;
                    }

                    var foodItem = targets.getItemTarget();

                    if (!foodItem) {
                        return SpellCastResult.BadTargets;
                    }

                    var pet = caster.toPlayer().getCurrentPet();

                    if (!pet) {
                        return SpellCastResult.NoPet;
                    }

                    if (!pet.HaveInDiet(foodItem.getTemplate())) {
                        return SpellCastResult.WrongPetFood;
                    }

                    if (foodItem.getTemplate().getBaseItemLevel() + 30 <= pet.getLevel()) {
                        return SpellCastResult.FoodLowlevel;
                    }

                    if (caster.toPlayer().isInCombat() || pet.isInCombat()) {
                        return SpellCastResult.AffectingCombat;
                    }

                    break;
                }
                case Charge: {
                    if (unitCaster == null) {
                        return SpellCastResult.BadTargets;
                    }

                    if (!triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreCasterAuras) && unitCaster.hasUnitState(UnitState.Root)) {
                        return SpellCastResult.rooted;
                    }

                    if (spellInfo.getNeedsExplicitUnitTarget()) {
                        var target = targets.getUnitTarget();

                        if (target == null) {
                            return SpellCastResult.DontReport;
                        }

                        // first we must check to see if the target is in LoS. A path can usually be built but LoS matters for charge spells
                        if (!target.isWithinLOSInMap(unitCaster)) //Do full LoS/Path check. Don't exclude m2
                        {
                            return SpellCastResult.LineOfSight;
                        }

                        var objSize = target.getCombatReach();
                        var range = spellInfo.getMaxRange(true, unitCaster, this) * 1.5f + objSize; // can't be overly strict

                        preGeneratedPath = new PathGenerator(unitCaster);
                        preGeneratedPath.setPathLengthLimit(range);

                        // first try with raycast, if it fails fall back to normal path
                        var result = preGeneratedPath.calculatePath(target.getLocation(), false);

                        if (preGeneratedPath.getPathType().hasFlag(PathType.SHORT)) {
                            return SpellCastResult.NOPATH;
                        } else if (!result || preGeneratedPath.getPathType().hasFlag(PathType.NOPATH.getValue() | PathType.INCOMPLETE.getValue())) {
                            return SpellCastResult.NOPATH;
                        } else if (preGeneratedPath.isInvalidDestinationZ(target)) // Check position z, if not in a straight line
                        {
                            return SpellCastResult.NOPATH;
                        }

                        preGeneratedPath.shortenPathUntilDist(target.getLocation(), objSize); //move back
                    }

                    break;
                }
                case Skinning: {
                    if (!caster.isTypeId(TypeId.PLAYER) || targets.getUnitTarget() == null || !targets.getUnitTarget().isTypeId(TypeId.UNIT)) {
                        return SpellCastResult.BadTargets;
                    }

                    if (!targets.getUnitTarget().hasUnitFlag(UnitFlag.Skinnable)) {
                        return SpellCastResult.TargetUnskinnable;
                    }

                    var creature = targets.getUnitTarget().toCreature();
                    var loot = creature.getLootForPlayer(caster.toPlayer());

                    if (loot != null && (!loot.isLooted() || loot.loot_type == LootType.SKINNING)) {
                        return SpellCastResult.TargetNotLooted;
                    }

                    var skill = creature.getCreatureTemplate().getRequiredLootSkill();

                    var skillValue = caster.toPlayer().getSkillValue(skill);
                    var targetLevel = targets.getUnitTarget().getLevelForTarget(caster);
                    var ReqValue = (int) (skillValue < 100 ? (TargetLevel - 10) * 10 : TargetLevel * 5);

                    if (ReqValue > skillValue) {
                        return SpellCastResult.LowCastlevel;
                    }

                    break;
                }
                case OpenLock: {
                    if (spellEffectInfo.targetA.getTarget() != targets.GameobjectTarget && spellEffectInfo.targetA.getTarget() != targets.GameobjectItemTarget) {
                        break;
                    }

                    if (!caster.isTypeId(TypeId.PLAYER) || (spellEffectInfo.targetA.getTarget() == targets.GameobjectTarget && targets.getGOTarget() == null)) {
                        return SpellCastResult.BadTargets;
                    }

                    Item pTempItem = null;

                    if ((boolean) (targets.getTargetMask().getValue() & SpellCastTargetFlags.TradeItem.getValue())) {
                        var pTrade = caster.toPlayer().getTradeData();

                        if (pTrade != null) {
                            pTempItem = pTrade.getTraderData().getItem(TradeSlots.NonTraded);
                        }
                    } else if ((boolean) (targets.getTargetMask().getValue() & SpellCastTargetFlags.item.getValue())) {
                        pTempItem = caster.toPlayer().getItemByGuid(targets.getItemTargetGuid());
                    }

                    // we need a go target, or an openable item target in case of TARGET_GAMEOBJECT_ITEM_TARGET
                    if (spellEffectInfo.targetA.getTarget() == targets.GameobjectItemTarget && targets.getGOTarget() == null && (pTempItem == null || pTempItem.getTemplate().getLockID() == 0 || !pTempItem.isLocked())) {
                        return SpellCastResult.BadTargets;
                    }

                    if (spellInfo.getId() != 1842 || (targets.getGOTarget() != null && targets.getGOTarget().getTemplate().type != GameObjectTypes.trap)) {
                        if (caster.toPlayer().getInBattleground() && !caster.toPlayer().canUseBattlegroundObject(targets.getGOTarget())) {
                            return SpellCastResult.TryAgain;
                        }
                    }

                    // get the lock entry
                    int lockId = 0;
                    var go = targets.getGOTarget();
                    var itm = targets.getItemTarget();

                    if (go != null) {
                        lockId = go.getTemplate().getLockId();

                        if (lockId == 0) {
                            return SpellCastResult.BadTargets;
                        }

                        if (go.getTemplate().getNotInCombat() != 0 && caster.toUnit().isInCombat()) {
                            return SpellCastResult.AffectingCombat;
                        }
                    } else if (itm != null) {
                        lockId = itm.getTemplate().getLockID();
                    }

                    var skillId = SkillType.NONE;
                    var reqSkillValue = 0;
                    var skillValue = 0;

                    // check lock compatibility
                    tangible.RefObject<SkillType> tempRef_skillId = new tangible.RefObject<SkillType>(skillId);
                    tangible.RefObject<Integer> tempRef_reqSkillValue = new tangible.RefObject<Integer>(reqSkillValue);
                    tangible.RefObject<Integer> tempRef_skillValue = new tangible.RefObject<Integer>(skillValue);
                    var res = canOpenLock(spellEffectInfo, lockId, tempRef_skillId, tempRef_reqSkillValue, tempRef_skillValue);
                    skillValue = tempRef_skillValue.refArgValue;
                    reqSkillValue = tempRef_reqSkillValue.refArgValue;
                    skillId = tempRef_skillId.refArgValue;

                    if (res != SpellCastResult.SpellCastOk) {
                        return res;
                    }

                    break;
                }
                case ResurrectPet: {
                    var playerCaster = caster.toPlayer();

                    if (playerCaster == null || playerCaster.getPetStable1() == null) {
                        return SpellCastResult.BadTargets;
                    }

                    var pet = playerCaster.getCurrentPet();

                    if (pet != null && pet.isAlive()) {
                        return SpellCastResult.AlreadyHaveSummon;
                    }

                    var petStable = playerCaster.getPetStable1();
                    var deadPetInfo = petStable.ActivePets.FirstOrDefault(petInfo ->
                    {
                        if (petInfo != null) {
                            petInfo.health;
                        }
                    } == 0);

                    if (deadPetInfo == null) {
                        return SpellCastResult.BadTargets;
                    }

                    break;
                }
                // This is generic summon effect
                case Summon: {
                    if (unitCaster == null) {
                        break;
                    }

                    var SummonProperties = CliDB.SummonPropertiesStorage.get(spellEffectInfo.miscValueB);

                    if (SummonProperties == null) {
                        break;
                    }

                    switch (SummonProperties.Control) {
                        case SummonCategory.Pet:
                            if (!spellInfo.hasAttribute(SpellAttr1.DismissPetFirst) && !unitCaster.getPetGUID().isEmpty()) {
                                return SpellCastResult.AlreadyHaveSummon;
                            }

                        case SummonCategory.Puppet:
                            if (!unitCaster.getCharmedGUID().isEmpty()) {
                                return SpellCastResult.AlreadyHaveCharm;
                            }

                            break;
                    }

                    break;
                }
                case CreateTamedPet: {
                    if (targets.getUnitTarget() != null) {
                        if (!targets.getUnitTarget().isTypeId(TypeId.PLAYER)) {
                            return SpellCastResult.BadTargets;
                        }

                        if (!spellInfo.hasAttribute(SpellAttr1.DismissPetFirst) && !targets.getUnitTarget().getPetGUID().isEmpty()) {
                            return SpellCastResult.AlreadyHaveSummon;
                        }
                    }

                    break;
                }
                case SummonPet: {
                    if (unitCaster == null) {
                        return SpellCastResult.BadTargets;
                    }

                    if (!unitCaster.getPetGUID().isEmpty()) //let warlock do a replacement summon
                    {
                        if (unitCaster.isTypeId(TypeId.PLAYER)) {
                            if (strict) //starting cast, trigger pet stun (cast by pet so it doesn't attack player)
                            {
                                var pet = unitCaster.toPlayer().getCurrentPet();

                                if (pet != null) {
                                    pet.castSpell(pet, 32752, (new CastSpellExtraArgs(TriggerCastFlag.FullMask)).setOriginalCaster(pet.getGUID()).setTriggeringSpell(this));
                                }
                            }
                        } else if (!spellInfo.hasAttribute(SpellAttr1.DismissPetFirst)) {
                            return SpellCastResult.AlreadyHaveSummon;
                        }
                    }

                    if (!unitCaster.getCharmedGUID().isEmpty()) {
                        return SpellCastResult.AlreadyHaveCharm;
                    }

                    var playerCaster = unitCaster.toPlayer();

                    if (playerCaster != null && playerCaster.getPetStable1() != null) {
                        PetSaveMode petSlot = null;

                        if (spellEffectInfo.miscValue == 0) {
                            petSlot = PetSaveMode.forValue(spellEffectInfo.calcValue());

                            // No pet can be summoned if any pet is dead
                            for (var activePet : playerCaster.getPetStable1().ActivePets) {
                                if ((activePet == null ? null : activePet.health) == 0) {
                                    playerCaster.sendTameFailure(PetTameResult.Dead);

                                    return SpellCastResult.DontReport;
                                }
                            }
                        }

                        var info = pet.GetLoadPetInfo(playerCaster.getPetStable1(), (int) spellEffectInfo.miscValue, 0, petSlot);

                        if (info.Item1 != null) {
                            if (info.Item1.type == PetType.Hunter) {
                                var creatureInfo = global.getObjectMgr().getCreatureTemplate(info.Item1.creatureId);

                                if (creatureInfo == null || !creatureInfo.isTameable(playerCaster.getCanTameExoticPets())) {
                                    // if problem in exotic pet
                                    if (creatureInfo != null && creatureInfo.isTameable(true)) {
                                        playerCaster.sendTameFailure(PetTameResult.CantControlExotic);
                                    } else {
                                        playerCaster.sendTameFailure(PetTameResult.NoPetAvailable);
                                    }

                                    return SpellCastResult.DontReport;
                                }
                            }
                        } else if (spellEffectInfo.miscValue == 0) // when miscvalue is present it is allowed to create new pets
                        {
                            playerCaster.sendTameFailure(PetTameResult.NoPetAvailable);

                            return SpellCastResult.DontReport;
                        }
                    }

                    break;
                }
                case DismissPet: {
                    var playerCaster = caster.toPlayer();

                    if (playerCaster == null) {
                        return SpellCastResult.BadTargets;
                    }

                    var pet = playerCaster.getCurrentPet();

                    if (pet == null) {
                        return SpellCastResult.NoPet;
                    }

                    if (!pet.isAlive()) {
                        return SpellCastResult.TargetsDead;
                    }

                    break;
                }
                case SummonPlayer: {
                    if (!caster.isTypeId(TypeId.PLAYER)) {
                        return SpellCastResult.BadTargets;
                    }

                    if (caster.toPlayer().getTarget().isEmpty()) {
                        return SpellCastResult.BadTargets;
                    }

                    var target = global.getObjAccessor().findPlayer(caster.toPlayer().getTarget());

                    if (target == null || caster.toPlayer() == target || (!target.isInSameRaidWith(caster.toPlayer()) && spellInfo.getId() != 48955)) // refer-a-friend spell
                    {
                        return SpellCastResult.BadTargets;
                    }

                    if (target.getHasSummonPending()) {
                        return SpellCastResult.SummonPending;
                    }

                    // check if our map is dungeon
                    var map = caster.getMap().toInstanceMap();

                    if (map != null) {
                        var mapId = map.getId();
                        var difficulty = map.getDifficultyID();
                        var mapLock = map.getInstanceLock();

                        if (mapLock != null) {
                            if (global.getInstanceLockMgr().canJoinInstanceLock(target.getGUID(), new MapDb2Entries(mapId, difficulty), mapLock) != TransferAbortReason.NONE) {
                                return SpellCastResult.TargetLockedToRaidInstance;
                            }
                        }

                        if (!target.satisfy(global.getObjectMgr().getAccessRequirement(mapId, difficulty), mapId)) {
                            return SpellCastResult.BadTargets;
                        }
                    }

                    break;
                }
                // RETURN HERE
                case SummonRafFriend: {
                    if (!caster.isTypeId(TypeId.PLAYER)) {
                        return SpellCastResult.BadTargets;
                    }

                    var playerCaster = caster.toPlayer();

                    //
                    if (playerCaster.getTarget().isEmpty()) {
                        return SpellCastResult.BadTargets;
                    }

                    var target = global.getObjAccessor().findPlayer(playerCaster.getTarget());

                    if (target == null || !(target.getSession().getRecruiterId() == playerCaster.getSession().getAccountId() || target.getSession().getAccountId() == playerCaster.getSession().getRecruiterId())) {
                        return SpellCastResult.BadTargets;
                    }

                    break;
                }
                case Leap:
                case TeleportUnitsFaceCaster: {
                    //Do not allow to cast it before BG starts.
                    if (caster.isTypeId(TypeId.PLAYER)) {
                        var bg = caster.toPlayer().getBattleground();

                        if (bg) {
                            if (bg.getStatus() != BattlegroundStatus.inProgress) {
                                return SpellCastResult.TryAgain;
                            }
                        }
                    }

                    break;
                }
                case StealBeneficialBuff: {
                    if (targets.getUnitTarget() == null || targets.getUnitTarget() == caster) {
                        return SpellCastResult.BadTargets;
                    }

                    break;
                }
                case LeapBack: {
                    if (unitCaster == null) {
                        return SpellCastResult.BadTargets;
                    }

                    if (unitCaster.hasUnitState(UnitState.Root)) {
                        if (unitCaster.isTypeId(TypeId.PLAYER)) {
                            return SpellCastResult.rooted;
                        } else {
                            return SpellCastResult.DontReport;
                        }
                    }

                    break;
                }
                case Jump:
                case JumpDest: {
                    if (unitCaster == null) {
                        return SpellCastResult.BadTargets;
                    }

                    if (unitCaster.hasUnitState(UnitState.Root)) {
                        return SpellCastResult.rooted;
                    }

                    break;
                }
                case TalentSpecSelect: {
                    var spec = CliDB.ChrSpecializationStorage.get(spellMisc.specializationId);
                    var playerCaster = caster.toPlayer();

                    if (!playerCaster) {
                        return SpellCastResult.TargetNotPlayer;
                    }

                    if (spec == null || (spec.classID != (int) player.getClass().getValue() && !spec.IsPetSpecialization())) {
                        return SpellCastResult.NoSpec;
                    }

                    if (spec.IsPetSpecialization()) {
                        var pet = player.getCurrentPet();

                        if (!pet || pet.getPetType() != PetType.Hunter || pet.getCharmInfo() == null) {
                            return SpellCastResult.NoPet;
                        }
                    }

                    // can't change during already started arena/Battleground
                    var bg = player.getBattleground();

                    if (bg) {
                        if (bg.getStatus() == BattlegroundStatus.inProgress) {
                            return SpellCastResult.NotInBattleground;
                        }
                    }

                    break;
                }
                case RemoveTalent: {
                    var playerCaster = caster.toPlayer();

                    if (playerCaster == null) {
                        return SpellCastResult.BadTargets;
                    }

                    var talent = CliDB.TalentStorage.get(spellMisc.talentId);

                    if (talent == null) {
                        return SpellCastResult.DontReport;
                    }

                    if (playerCaster.getSpellHistory().hasCooldown(talent.spellID)) {
                        param1.refArgValue = (int) talent.spellID;

                        return SpellCastResult.CantUntalent;
                    }

                    break;
                }
                case GiveArtifactPower:
                case GiveArtifactPowerNoBonus: {
                    var playerCaster = caster.toPlayer();

                    if (playerCaster == null) {
                        return SpellCastResult.BadTargets;
                    }

                    var artifactAura = playerCaster.getAura(PlayerConst.ArtifactsAllWeaponsGeneralWeaponEquippedPassive);

                    if (artifactAura == null) {
                        return SpellCastResult.NoArtifactEquipped;
                    }

                    var artifact = playerCaster.toPlayer().getItemByGuid(artifactAura.castItemGuid);

                    if (artifact == null) {
                        return SpellCastResult.NoArtifactEquipped;
                    }

                    if (spellEffectInfo.effect == SpellEffectName.GiveArtifactPower) {
                        var artifactEntry = CliDB.ArtifactStorage.get(artifact.getTemplate().getArtifactID());

                        if (artifactEntry == null || artifactEntry.artifactCategoryID != spellEffectInfo.miscValue) {
                            return SpellCastResult.WrongArtifactEquipped;
                        }
                    }

                    break;
                }
                case ChangeBattlepetQuality:
                case GrantBattlepetLevel:
                case GrantBattlepetExperience: {
                    var playerCaster = caster.toPlayer();

                    if (playerCaster == null || !targets.getUnitTarget() || !targets.getUnitTarget().isCreature()) {
                        return SpellCastResult.BadTargets;
                    }

                    var battlePetMgr = playerCaster.getSession().getBattlePetMgr();

                    if (!battlePetMgr.getHasJournalLock()) {
                        return SpellCastResult.CantDoThatRightNow;
                    }

                    var creature = targets.getUnitTarget().toCreature();

                    if (creature != null) {
                        if (playerCaster.getSummonedBattlePetGUID().isEmpty() || creature.getBattlePetCompanionGUID().isEmpty()) {
                            return SpellCastResult.NoPet;
                        }

                        if (ObjectGuid.opNotEquals(playerCaster.getSummonedBattlePetGUID(), creature.getBattlePetCompanionGUID())) {
                            return SpellCastResult.BadTargets;
                        }

                        var battlePet = battlePetMgr.getPet(creature.getBattlePetCompanionGUID());

                        if (battlePet != null) {
                            var battlePetSpecies = CliDB.BattlePetSpeciesStorage.get(battlePet.packetInfo.species);

                            if (battlePetSpecies != null) {
                                var battlePetType = (int) spellEffectInfo.miscValue;

                                if (battlePetType != 0) {
                                    if ((battlePetType & (1 << battlePetSpecies.PetTypeEnum)) == 0) {
                                        return SpellCastResult.WrongBattlePetType;
                                    }
                                }

                                if (spellEffectInfo.effect == SpellEffectName.ChangeBattlepetQuality) {
                                    var qualityRecord = CliDB.BattlePetBreedQualityStorage.values().FirstOrDefault(a1 -> a1.MaxQualityRoll < spellEffectInfo.basePoints);

                                    var quality = battlePetBreedQuality.Poor;

                                    if (qualityRecord != null) {
                                        quality = battlePetBreedQuality.forValue(qualityRecord.QualityEnum);
                                    }

                                    if (battlePet.packetInfo.quality >= (byte) quality.getValue()) {
                                        return SpellCastResult.CantUpgradeBattlePet;
                                    }
                                }

                                if (spellEffectInfo.effect == SpellEffectName.GrantBattlepetLevel || spellEffectInfo.effect == SpellEffectName.GrantBattlepetExperience) {
                                    if (battlePet.packetInfo.level >= SharedConst.maxBattlePetLevel) {
                                        return SpellCastResult.GrantPetLevelFail;
                                    }
                                }

                                if (battlePetSpecies.getFlags().hasFlag(BattlePetSpeciesFlags.CantBattle)) {
                                    return SpellCastResult.BadTargets;
                                }
                            }
                        }
                    }

                    break;
                }
                default:
                    break;
            }

            if (spellEffectInfo.isAura()) {
                approximateAuraEffectMask.add(spellEffectInfo.effectIndex);
            } else if (spellEffectInfo.isEffect()) {
                nonAuraEffectMask |= 1 << spellEffectInfo.effectIndex;
            }
        }

        for (var spellEffectInfo : spellInfo.getEffects()) {
            switch (spellEffectInfo.applyAuraName) {
                case ModPossessPet: {
                    if (!caster.isTypeId(TypeId.PLAYER)) {
                        return SpellCastResult.NoPet;
                    }

                    var pet = caster.toPlayer().getCurrentPet();

                    if (pet == null) {
                        return SpellCastResult.NoPet;
                    }

                    if (!pet.getCharmerGUID().isEmpty()) {
                        return SpellCastResult.AlreadyHaveCharm;
                    }

                    break;
                }
                case ModPossess:
                case ModCharm:
                case AoeCharm: {
                    var unitCaster1 = (_originalCaster ? _originalCaster : caster.toUnit());

                    if (unitCaster1 == null) {
                        return SpellCastResult.BadTargets;
                    }

                    if (!unitCaster1.getCharmerGUID().isEmpty()) {
                        return SpellCastResult.AlreadyHaveCharm;
                    }

                    if (spellEffectInfo.applyAuraName == AuraType.ModCharm || spellEffectInfo.applyAuraName == AuraType.ModPossess) {
                        if (!spellInfo.hasAttribute(SpellAttr1.DismissPetFirst) && !unitCaster1.getPetGUID().isEmpty()) {
                            return SpellCastResult.AlreadyHaveSummon;
                        }

                        if (!unitCaster1.getCharmedGUID().isEmpty()) {
                            return SpellCastResult.AlreadyHaveCharm;
                        }
                    }

                    var target = targets.getUnitTarget();

                    if (target != null) {
                        if (target.isTypeId(TypeId.UNIT) && target.toCreature().isVehicle()) {
                            return SpellCastResult.BadImplicitTargets;
                        }

                        if (target.isMounted()) {
                            return SpellCastResult.CantBeCharmed;
                        }

                        if (!target.getCharmerGUID().isEmpty()) {
                            return SpellCastResult.Charmed;
                        }

                        if (target.getOwnerUnit() != null && target.getOwnerUnit().isTypeId(TypeId.PLAYER)) {
                            return SpellCastResult.TargetIsPlayerControlled;
                        }

                        var damage = calculateDamage(spellEffectInfo, target);

                        if (damage != 0 && target.getLevelForTarget(caster) > damage) {
                            return SpellCastResult.Highlevel;
                        }
                    }

                    break;
                }
                case Mounted: {
                    if (unitCaster == null) {
                        return SpellCastResult.BadTargets;
                    }

                    if (unitCaster.isInWater() && spellInfo.hasAura(AuraType.ModIncreaseMountedFlightSpeed)) {
                        return SpellCastResult.OnlyAbovewater;
                    }

                    if (unitCaster.isInDisallowedMountForm()) {
                        sendMountResult(MountResult.Shapeshifted); // mount result gets sent before the cast result

                        return SpellCastResult.DontReport;
                    }

                    break;
                }
                case RangedAttackPowerAttackerBonus: {
                    if (targets.getUnitTarget() == null) {
                        return SpellCastResult.BadImplicitTargets;
                    }

                    // can be casted at non-friendly unit or own pet/charm
                    if (caster.isFriendlyTo(targets.getUnitTarget())) {
                        return SpellCastResult.TargetFriendly;
                    }

                    break;
                }
                case Fly:
                case ModIncreaseFlightSpeed: {
                    // not allow cast fly spells if not have req. skills  (all spells is self target)
                    // allow always ghost flight spells
                    if (originalCaster != null && originalCaster.isTypeId(TypeId.PLAYER) && originalCaster.isAlive()) {
                        var Bf = global.getBattleFieldMgr().getBattlefieldToZoneId(originalCaster.getMap(), originalCaster.getZoneId());
                        var area = CliDB.AreaTableStorage.get(originalCaster.getAreaId());

                        if (area != null) {
                            if (area.hasFlag(AreaFlags.NoFlyZone) || (Bf != null && !Bf.canFlyIn())) {
                                return SpellCastResult.NotHere;
                            }
                        }
                    }

                    break;
                }
                case PeriodicManaLeech: {
                    if (spellEffectInfo.isTargetingArea()) {
                        break;
                    }

                    if (targets.getUnitTarget() == null) {
                        return SpellCastResult.BadImplicitTargets;
                    }

                    if (!caster.isTypeId(TypeId.PLAYER) || castItem != null) {
                        break;
                    }

                    if (targets.getUnitTarget().getDisplayPowerType() != powerType.mana) {
                        return SpellCastResult.BadTargets;
                    }

                    break;
                }
                default:
                    break;
            }

            // check if target already has the same type, but more powerful aura
            if (!spellInfo.hasAttribute(SpellAttr4.AuraNeverBounces) && (nonAuraEffectMask == 0 || spellInfo.hasAttribute(SpellAttr4.AuraBounceFailsSpell)) && approximateAuraEffectMask.contains(spellEffectInfo.effectIndex) && !spellInfo.isTargetingArea()) {
                var target = targets.getUnitTarget();

                if (target != null) {
                    if (!target.isHighestExclusiveAuraEffect(spellInfo, spellEffectInfo.applyAuraName, spellEffectInfo.calcValue(caster, spellValue.effectBasePoints.get(spellEffectInfo.effectIndex), null, castItemEntry, castItemLevel), approximateAuraEffectMask, false)) {
                        return SpellCastResult.AuraBounced;
                    }
                }
            }
        }

        // check trade slot case (last, for allow catch any another cast problems)
        if ((boolean) (targets.getTargetMask().getValue() & SpellCastTargetFlags.TradeItem.getValue())) {
            if (castItem != null) {
                return SpellCastResult.ItemEnchantTradeWindow;
            }

            if (spellInfo.hasAttribute(SpellAttr2.EnchantOwnItemOnly)) {
                return SpellCastResult.ItemEnchantTradeWindow;
            }

            if (!caster.isTypeId(TypeId.PLAYER)) {
                return SpellCastResult.NotTrading;
            }

            var my_trade = caster.toPlayer().getTradeData();

            if (my_trade == null) {
                return SpellCastResult.NotTrading;
            }

            var slot = TradeSlots.forValue(targets.getItemTargetGuid().lowValue());

            if (slot != TradeSlots.NonTraded) {
                return SpellCastResult.BadTargets;
            }

            if (!isTriggered()) {
                if (my_trade.getSpell() != 0) {
                    return SpellCastResult.ItemAlreadyEnchanted;
                }
            }
        }

        // check if caster has at least 1 combo point for spells that require combo points
        if (needComboPoints) {
            var plrCaster = caster.toPlayer();

            if (plrCaster != null) {
                if (plrCaster.getComboPoints() == 0) {
                    return SpellCastResult.NoComboPoints;
                }
            }
        }

        // all ok
        return SpellCastResult.SpellCastOk;
    }

    public final SpellCastResult checkPetCast(Unit target) {
        var unitCaster = caster.toUnit();

        if (unitCaster != null && unitCaster.hasUnitState(UnitState.Casting) && !triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreCastInProgress)) //prevent spellcast interruption by another spellcast
        {
            return SpellCastResult.SpellInProgress;
        }

        // dead owner (pets still alive when owners ressed?)
        var owner = caster.getCharmerOrOwner();

        if (owner != null) {
            if (!owner.isAlive()) {
                return SpellCastResult.CasterDead;
            }
        }

        if (target == null && targets.getUnitTarget() != null) {
            target = targets.getUnitTarget();
        }

        if (spellInfo.getNeedsExplicitUnitTarget()) {
            if (target == null) {
                return SpellCastResult.BadImplicitTargets;
            }

            targets.setUnitTarget(target);
        }

        // cooldown
        var creatureCaster = caster.toCreature();

        if (creatureCaster) {
            if (creatureCaster.getSpellHistory().hasCooldown(spellInfo.getId())) {
                return SpellCastResult.NotReady;
            }
        }

        // Check if spell is affected by GCD
        if (spellInfo.getStartRecoveryCategory() > 0) {
            if (unitCaster.getCharmInfo() != null && unitCaster.getSpellHistory().hasGlobalCooldown(spellInfo)) {
                return SpellCastResult.NotReady;
            }
        }

        return checkCast(true);
    }

    public final boolean canAutoCast(Unit target) {
        if (!target) {
            return (checkPetCast(target) == SpellCastResult.SpellCastOk);
        }

        var targetguid = target.getGUID();

        // check if target already has the same or a more powerful aura
        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (!spellEffectInfo.isAura()) {
                continue;
            }

            var auraType = spellEffectInfo.applyAuraName;
            var auras = target.getAuraEffectsByType(auraType);

            for (var eff : auras) {
                if (spellInfo.getId() == eff.getSpellInfo().getId()) {
                    return false;
                }

                switch (global.getSpellMgr().checkSpellGroupStackRules(spellInfo, eff.getSpellInfo())) {
                    case Exclusive:
                        return false;
                    case ExclusiveFromSameCaster:
                        if (getCaster() == eff.getCaster()) {
                            return false;
                        }

                        break;
                    case ExclusiveSameEffect: // this one has further checks, but i don't think they're necessary for autocast logic
                    case ExclusiveHighest:
                        if (Math.abs(spellEffectInfo.basePoints) <= Math.abs(eff.getAmount())) {
                            return false;
                        }

                        break;
                    case Default:
                    default:
                        break;
                }
            }
        }

        var result = checkPetCast(target);

        if (result == SpellCastResult.SpellCastOk || result == SpellCastResult.UnitNotInfront) {
            // do not check targets for ground-targeted spells (we target them on top of the intended target anyway)
            if (spellInfo.getExplicitTargetMask().hasFlag(SpellCastTargetFlags.DestLocation)) {
                return true;
            }

            selectSpellTargets();

            //check if among target units, our WANTED target is as well (.only self cast spells return false)
            for (var ihit : uniqueTargetInfo) {
                if (Objects.equals(ihit.targetGuid, targetguid)) {
                    return true;
                }
            }
        }

        // either the cast failed or the intended target wouldn't be hit
        return false;
    }

    public final void delayed() // only called in dealDamage()
    {
        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return;
        }

        if (isDelayableNoMore()) // Spells may only be delayed twice
        {
            return;
        }

        //check pushback reduce
        var delaytime = 500; // spellcasting delay is normally 500ms
        double delayReduce = 100; // must be initialized to 100 for percent modifiers

        var player = unitCaster.getSpellModOwner();

        if (player != null) {
            tangible.RefObject<Double> tempRef_delayReduce = new tangible.RefObject<Double>(delayReduce);
            player.applySpellMod(spellInfo, SpellModOp.ResistPushback, tempRef_delayReduce, this);
            delayReduce = tempRef_delayReduce.refArgValue;
        }

        delayReduce += unitCaster.getTotalAuraModifier(AuraType.ReducePushback) - 100;

        if (delayReduce >= 100) {
            return;
        }

        tangible.RefObject<Integer> tempRef_delaytime = new tangible.RefObject<Integer>(delaytime);
        MathUtil.AddPct(tempRef_delaytime, -delayReduce);
        delaytime = tempRef_delaytime.refArgValue;

        if (timer + delaytime > casttime) {
            delaytime = _casttime - timer;
            timer = casttime;
        } else {
            timer += delaytime;
        }

        SpellDelayed spellDelayed = new SpellDelayed();
        spellDelayed.caster = unitCaster.getGUID();
        spellDelayed.actualDelay = delaytime;

        unitCaster.sendMessageToSet(spellDelayed, true);
    }

    public final void delayedChannel() {
        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return;
        }

        if (spellState != SpellState.Casting) {
            return;
        }

        if (isDelayableNoMore()) // Spells may only be delayed twice
        {
            return;
        }

        //check pushback reduce
        // should be affected by modifiers, not take the dbc duration.
        var duration = ((channeledDuration > 0) ? _channeledDuration : spellInfo.getDuration());

        var delaytime = MathUtil.CalculatePct(duration, 25); // channeling delay is normally 25% of its time per hit
        double delayReduce = 100; // must be initialized to 100 for percent modifiers

        var player = unitCaster.getSpellModOwner();

        if (player != null) {
            tangible.RefObject<Double> tempRef_delayReduce = new tangible.RefObject<Double>(delayReduce);
            player.applySpellMod(spellInfo, SpellModOp.ResistPushback, tempRef_delayReduce, this);
            delayReduce = tempRef_delayReduce.refArgValue;
        }

        delayReduce += unitCaster.getTotalAuraModifier(AuraType.ReducePushback) - 100;

        if (delayReduce >= 100) {
            return;
        }

        tangible.RefObject<Integer> tempRef_delaytime = new tangible.RefObject<Integer>(delaytime);
        MathUtil.AddPct(tempRef_delaytime, -delayReduce);
        delaytime = tempRef_delaytime.refArgValue;

        if (timer <= delaytime) {
            delaytime = timer;
            timer = 0;
        } else {
            _timer -= delaytime;
        }

        for (var ihit : uniqueTargetInfo) {
            if (ihit.missCondition == SpellMissInfo.NONE) {
                var unit = (Objects.equals(unitCaster.getGUID(), ihit.targetGuid)) ? unitCaster : global.getObjAccessor().GetUnit(unitCaster, ihit.targetGuid);

                if (unit != null) {
                    unit.delayOwnedAuras(spellInfo.getId(), originalCasterGuid, delaytime);
                }
            }
        }

        // partially interrupt persistent area auras
        var dynObj = unitCaster.getDynObject(spellInfo.getId());

        if (dynObj != null) {
            dynObj.delay(delaytime);
        }

        sendChannelUpdate((int) timer);
    }

    public final boolean hasPowerTypeCost(Power power) {
        return getPowerTypeCostAmount(power) != null;
    }

    public final Integer getPowerTypeCostAmount(Power power) {
        var powerCost = tangible.ListHelper.find(powerCosts, cost -> cost.power == power);

        if (powerCost == null) {
            return null;
        }

        return powerCost.amount;
    }

    public final void setSpellValue(SpellValueMod mod, float value) {
        if (mod.ordinal() < SpellValueMod.BASE_POINT_END.ordinal()) {
            spellValue.effectBasePoints.put(mod.ordinal(), value);
            spellValue.customBasePointsMask |= 1 << mod.ordinal();

            return;
        }

        switch (mod) {
            case RADIUS_MOD:
                spellValue.radiusMod = value / 10000;

                break;
            case MAX_TARGETS:
                spellValue.maxAffectedTargets = (int) value;

                break;
            case AURA_STACK:
                spellValue.auraStackAmount = (int) value;

                break;
            case CRIT_CHANCE:
                spellValue.criticalChance = value / 100.0f; // @todo ugly /100 remove when basepoints are double

                break;
            case DURATION_PCT:
                spellValue.durationMul = value / 100.0f;

                break;
            case DURATION:
                spellValue.duration = (int) value;

                break;
            case Summon_Duration:
                spellValue.summonDuration = value;

                break;
        }
    }

    public final boolean checkTargetHookEffect(ITargetHookHandler th, int effIndexToCheck) {
        if (spellInfo.getEffects().size() <= effIndexToCheck) {
            return false;
        }

        return checkTargetHookEffect(th, spellInfo.getEffect(effIndexToCheck));
    }

    public final boolean checkTargetHookEffect(ITargetHookHandler th, SpellEffectInfo spellEffectInfo) {
        if (th.getTargetType() == 0) {
            return false;
        }

        if (spellEffectInfo.targetA.getTarget() != th.getTargetType() && spellEffectInfo.targetB.getTarget() != th.getTargetType()) {
            return false;
        }

        SpellImplicitTargetInfo targetInfo = new spellImplicitTargetInfo(th.getTargetType());

        switch (targetInfo.getSelectionCategory()) {
            case Channel: // SINGLE
                return !th.getArea();
            case Nearby: // BOTH
                return true;
            case Cone: // AREA
            case Line: // AREA
                return th.getArea();
            case Area: // AREA
                if (targetInfo.getObjectType() == SpellTargetObjectTypes.UnitAndDest) {
                    return th.getArea() || th.dest;
                }

                return th.getArea();
            case Default:
                switch (targetInfo.getObjectType()) {
                    case Src: // EMPTY
                        return false;
                    case Dest: // Dest
                        return th.dest;
                    default:
                        switch (targetInfo.getReferenceType()) {
                            case Caster: // SINGLE
                                return !th.getArea();
                            case Target: // BOTH
                                return true;
                            default:
                                break;
                        }

                        break;
                }

                break;
            default:
                break;
        }

        return false;
    }

    public final void callScriptBeforeHitHandlers(SpellMissInfo missInfo) {
        for (var script : this.<ISpellBeforeHit>GetSpellScripts()) {
            script._InitHit();
            script._PrepareScriptCall(SpellScriptHookType.BeforeHit);
            ((ISpellBeforeHit) script).BeforeHit(missInfo);
            script._FinishScriptCall();
        }
    }

    public final void callScriptOnHitHandlers() {
        for (var script : this.<ISpellOnHit>GetSpellScripts()) {
            script._PrepareScriptCall(SpellScriptHookType.hit);
            ((ISpellOnHit) script).OnHit();
            script._FinishScriptCall();
        }
    }

    public final void callScriptAfterHitHandlers() {
        for (var script : this.<ISpellAfterHit>GetSpellScripts()) {
            script._PrepareScriptCall(SpellScriptHookType.AfterHit);
            ((ISpellAfterHit) script).AfterHit();
            script._FinishScriptCall();
        }
    }

    public final void callScriptCalcCritChanceHandlers(Unit victim, tangible.RefObject<Double> critChance) {
        for (var loadedScript : this.<ISpellCalcCritChance>GetSpellScripts()) {
            loadedScript._PrepareScriptCall(SpellScriptHookType.CalcCritChance);

            ((ISpellCalcCritChance) loadedScript).calcCritChance(victim, critChance);

            loadedScript._FinishScriptCall();
        }
    }

    public final void callScriptOnResistAbsorbCalculateHandlers(DamageInfo damageInfo, tangible.RefObject<Double> resistAmount, tangible.RefObject<Double> absorbAmount) {
        for (var script : this.<ISpellCalculateResistAbsorb>GetSpellScripts()) {
            script._PrepareScriptCall(SpellScriptHookType.OnResistAbsorbCalculation);

            ((ISpellCalculateResistAbsorb) script).CalculateResistAbsorb(damageInfo, resistAmount, absorbAmount);

            script._FinishScriptCall();
        }
    }

    public final boolean canExecuteTriggersOnHit(Unit unit) {
        return canExecuteTriggersOnHit(unit, null);
    }

    public final boolean canExecuteTriggersOnHit(Unit unit, SpellInfo triggeredByAura) {
        var onlyOnTarget = triggeredByAura != null && triggeredByAura.hasAttribute(SpellAttr4.ClassTriggerOnlyOnTarget);

        if (!onlyOnTarget) {
            return true;
        }

        // If triggeredByAura has SPELL_ATTR4_CLASS_TRIGGER_ONLY_ON_TARGET then it can only proc on either noncaster units...
        if (unit != caster) {
            return true;
        }

        // ... or caster if it is the only target
        if (uniqueTargetInfo.size() == 1) {
            return true;
        }

        return false;
    }


//	public list<(ISpellScript, ISpellEffect)> getEffectScripts(SpellScriptHookType h, int index)
//		{
//			if (effectHandlers.TryGetValue(index, out var effDict) && effDict.TryGetValue(h, out var scripts))
//				return scripts;
//
//			return DUMMYSPELLEFFECTS;
//		}

    public final <T extends ISpellScript> ArrayList<ISpellScript> getSpellScripts() {
        TValue scripts;
        if (spellScriptsByType.containsKey(T.class) && (scripts = spellScriptsByType.get(T.class)) == scripts) {
            return scripts;
        }

        return DUMMY;
    }

    public final <T extends ISpellScript> void forEachSpellScript(tangible.Action1Param<T> action) {
        for (T script : this.<T>GetSpellScripts()) {
            try {
                action.invoke(script);
            } catch (RuntimeException e) {
                Log.outException(e, "");
            }
        }
    }

    public final SpellCastResult checkMovement() {
        if (isTriggered()) {
            return SpellCastResult.SpellCastOk;
        }

        var unitCaster = caster.toUnit();

        if (unitCaster != null) {
            if (!unitCaster.canCastSpellWhileMoving(spellInfo)) {
                if (getState() == SpellState.Preparing) {
                    if (casttime > 0 && spellInfo.getInterruptFlags().hasFlag(SpellInterruptFlags.movement)) {
                        return SpellCastResult.Moving;
                    }
                } else if (getState() == SpellState.Casting && !spellInfo.isMoveAllowedChannel()) {
                    return SpellCastResult.Moving;
                }
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    public final boolean isTriggeredByAura(SpellInfo auraSpellInfo) {
        return (auraSpellInfo == triggeredByAuraSpell);
    }


//	public static implicit operator bool(Spell spell)
//		{
//			return spell != null;
//		}

    public final void setReferencedFromCurrent(boolean yes) {
        referencedFromCurrentSpell = yes;
    }

    public final boolean getPlayerIfIsEmpowered(tangible.OutObject<Player> p) {
        p.outArgValue = null;

        return !spellInfo.getEmpowerStages().isEmpty() && caster.isPlayer(p);
    }

    public final void setEmpowerState(EmpowerState state) {
        if (empowerState != EmpowerState.Finished) {
            if (empowerState == EmpowerState.NONE && state == EmpowerState.Canceled) {
                empowerState = EmpowerState.CanceledStartup;
            } else if (empowerState == EmpowerState.CanceledStartup && state == EmpowerState.Empowering) {
                empowerState = EmpowerState.NONE;
            } else {
                empowerState = state;
            }
        }
    }

    public final boolean tryGetTotalEmpowerDuration(boolean includeBaseCast, tangible.OutObject<Integer> duration) {
        if (!empowerStages.isEmpty()) {
            duration.outArgValue = (int) (empowerStages.Sum(a -> a.value.durationMs) + (includeBaseCast ? 1000 : 0));
            return true;
        }

        duration.outArgValue = 0;
        return false;
    }

    private void selectExplicitTargets() {
        // here go all explicit target changes made to explicit targets after spell prepare phase is finished
        var target = targets.getUnitTarget();

        if (target != null) {
            // check for explicit target redirection, for Grounding Totem for example
            if (spellInfo.getExplicitTargetMask().hasFlag(SpellCastTargetFlags.UnitEnemy) || (spellInfo.getExplicitTargetMask().hasFlag(SpellCastTargetFlags.unit) && !caster.isFriendlyTo(target))) {
                Unit redirect = null;

                switch (spellInfo.getDmgClass()) {
                    case Magic:
                        redirect = caster.getMagicHitRedirectTarget(target, spellInfo);

                        break;
                    case Melee:
                    case Ranged:
                        // should gameobjects cast damagetype melee/ranged spells this needs to be changed
                        redirect = caster.toUnit().getMeleeHitRedirectTarget(target, spellInfo);

                        break;
                    default:
                        break;
                }

                if (redirect != null && (redirect != target)) {
                    targets.setUnitTarget(redirect);
                }
            }
        }
    }

    private long calculateDelayMomentForDst(float launchDelay) {
        if (targets.getHasDst()) {
            if (targets.getHasTraj()) {
                var speed = targets.getSpeedXY();

                if (speed > 0.0f) {
                    return (long) (Math.floor((targets.getDist2d() / speed + launchDelay) * 1000.0f));
                }
            } else if (spellInfo.hasAttribute(SpellAttr9.SpecialDelayCalculation)) {
                return (long) (Math.floor((spellInfo.getSpeed() + launchDelay) * 1000.0f));
            } else if (spellInfo.getSpeed() > 0.0f) {
                // We should not subtract caster size from dist calculation (fixes execution time desync with animation on client, eg. Malleable Goo cast by PP)
                var dist = caster.getLocation().getExactDist(targets.getDstPos());

                return (long) (Math.floor((dist / spellInfo.getSpeed() + launchDelay) * 1000.0f));
            }

            return (long) Math.floor(launchDelay * 1000.0f);
        }

        return 0;
    }

    private void selectEffectImplicitTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType, HashSet<Integer> processedEffectMask) {
        if (targetType.getTarget() == 0) {
            return;
        }

        // set the same target list for all effects
        // some spells appear to need this, however this requires more research
        switch (targetType.getSelectionCategory()) {
            case Nearby:
            case Cone:
            case Area:
            case Line: {
                // targets for effect already selected
                if (processedEffectMask.contains(spellEffectInfo.effectIndex)) {
                    return;
                }

                var j = 0;

                // choose which targets we can select at once
                for (var effect : spellInfo.getEffects()) {
                    if (effect.isEffect() && spellEffectInfo.targetA.getTarget() == effect.targetA.getTarget() && spellEffectInfo.targetB.getTarget() == effect.targetB.getTarget() && spellEffectInfo.implicitTargetConditions == effect.implicitTargetConditions && spellEffectInfo.calcRadius(caster) == effect.calcRadius(caster) && checkScriptEffectImplicitTargets(spellEffectInfo.effectIndex, j)) {
                        processedEffectMask.add(j);
                    }

                    j++;
                }


                break;
            }
            default:
                break;
        }

        switch (targetType.getSelectionCategory()) {
            case Channel:
                selectImplicitChannelTargets(spellEffectInfo, targetType);

                break;
            case Nearby:
                selectImplicitNearbyTargets(spellEffectInfo, targetType, processedEffectMask);

                break;
            case Cone:
                selectImplicitConeTargets(spellEffectInfo, targetType, processedEffectMask);

                break;
            case Area:
                selectImplicitAreaTargets(spellEffectInfo, targetType, processedEffectMask);

                break;
            case Traj:
                // just in case there is no dest, explanation in SelectImplicitDestDestTargets
                checkDst();

                selectImplicitTrajTargets(spellEffectInfo, targetType);

                break;
            case Line:
                selectImplicitLineTargets(spellEffectInfo, targetType, processedEffectMask);

                break;
            case Default:
                switch (targetType.getObjectType()) {
                    case Src:
                        switch (targetType.getReferenceType()) {
                            case Caster:
                                targets.setSrc(caster);

                                break;
                        }

                        break;
                    case Dest:
                        switch (targetType.getReferenceType()) {
                            case Caster:
                                selectImplicitCasterDestTargets(spellEffectInfo, targetType);

                                break;
                            case Target:
                                selectImplicitTargetDestTargets(spellEffectInfo, targetType);

                                break;
                            case Dest:
                                selectImplicitDestDestTargets(spellEffectInfo, targetType);

                                break;
                        }

                        break;
                    default:
                        switch (targetType.getReferenceType()) {
                            case Caster:
                                selectImplicitCasterObjectTargets(spellEffectInfo, targetType);

                                break;
                            case Target:
                                selectImplicitTargetObjectTargets(spellEffectInfo, targetType);

                                break;
                        }

                        break;
                }

                break;
            case Nyi:
                Log.outDebug(LogFilter.spells, "SPELL: target type {0}, found in spellID {1}, effect {2} is not implemented yet!", spellInfo.getId(), spellEffectInfo.effectIndex, targetType.getTarget());

                break;
        }
    }

    private void selectImplicitChannelTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType) {
        if (targetType.getReferenceType() != SpellTargetReferenceTypes.caster) {
            return;
        }

        var channeledSpell = originalCaster.getCurrentSpell(CurrentSpellType.Channeled);

        if (channeledSpell == null) {
            Log.outDebug(LogFilter.spells, "Spell.SelectImplicitChannelTargets: cannot find channel spell for spell ID {0}, effect {1}", spellInfo.getId(), spellEffectInfo.effectIndex);

            return;
        }

        switch (targetType.getTarget()) {
            case Framework.Constants.targets.UnitChannelTarget: {
                for (var channelTarget : originalCaster.getUnitData().channelObjects) {
                    WorldObject target = global.getObjAccessor().GetUnit(caster, channelTarget);
                    tangible.RefObject<WorldObject> tempRef_target = new tangible.RefObject<WorldObject>(target);
                    callScriptObjectTargetSelectHandlers(tempRef_target, spellEffectInfo.effectIndex, targetType);
                    target = tempRef_target.refArgValue;
                    // unit target may be no longer avalible - teleported out of map for example
                    var unitTarget = target ? target.toUnit() : null;

                    if (unitTarget) {
                        addUnitTarget(unitTarget, spellEffectInfo.effectIndex);
                    } else {
                        Log.outDebug(LogFilter.spells, "SPELL: cannot find channel spell target for spell ID {0}, effect {1}", spellInfo.getId(), spellEffectInfo.effectIndex);
                    }
                }

                break;
            }
            case Framework.Constants.targets.DestChannelTarget: {
                if (channeledSpell.targets.getHasDst()) {
                    targets.setDst(channeledSpell.targets);
                } else {
                    ArrayList<ObjectGuid> channelObjects = originalCaster.getUnitData().channelObjects;
                    var target = !channelObjects.isEmpty() ? global.getObjAccessor().GetWorldObject(caster, channelObjects.get(0)) : null;

                    if (target != null) {
                        tangible.RefObject<WorldObject> tempRef_target2 = new tangible.RefObject<WorldObject>(target);
                        callScriptObjectTargetSelectHandlers(tempRef_target2, spellEffectInfo.effectIndex, targetType);
                        target = tempRef_target2.refArgValue;

                        if (target) {
                            SpellDestination dest = new SpellDestination(target);

                            if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
                                dest.position.setO(spellEffectInfo.positionFacing);
                            }

                            tangible.RefObject<SpellDestination> tempRef_dest = new tangible.RefObject<SpellDestination>(dest);
                            callScriptDestinationTargetSelectHandlers(tempRef_dest, spellEffectInfo.effectIndex, targetType);
                            dest = tempRef_dest.refArgValue;
                            targets.setDst(dest);
                        }
                    } else {
                        Log.outDebug(LogFilter.spells, "SPELL: cannot find channel spell destination for spell ID {0}, effect {1}", spellInfo.getId(), spellEffectInfo.effectIndex);
                    }
                }

                break;
            }
            case Framework.Constants.targets.DestChannelCaster: {
                SpellDestination dest = new SpellDestination(channeledSpell.getCaster());

                if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
                    dest.position.setO(spellEffectInfo.positionFacing);
                }

                tangible.RefObject<SpellDestination> tempRef_dest2 = new tangible.RefObject<SpellDestination>(dest);
                callScriptDestinationTargetSelectHandlers(tempRef_dest2, spellEffectInfo.effectIndex, targetType);
                dest = tempRef_dest2.refArgValue;
                targets.setDst(dest);

                break;
            }
        }
    }

    private void selectImplicitNearbyTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType, HashSet<Integer> effMask) {
        if (targetType.getReferenceType() != SpellTargetReferenceTypes.caster) {
            return;
        }

        var range = 0.0f;

        switch (targetType.getCheckType()) {
            case Enemy:
                range = spellInfo.getMaxRange(false, caster, this);

                break;
            case Ally:
            case Party:
            case Raid:
            case RaidClass:
                range = spellInfo.getMaxRange(true, caster, this);

                break;
            case Entry:
            case Default:
                range = spellInfo.getMaxRange(isPositive(), caster, this);

                break;
        }

        var condList = spellEffectInfo.implicitTargetConditions;

        // handle emergency case - try to use other provided targets if no conditions provided
        if (targetType.getCheckType() == SpellTargetCheckTypes.entry && (condList == null || condList.isEmpty())) {
            Log.outDebug(LogFilter.spells, "Spell.SelectImplicitNearbyTargets: no conditions entry for target with TARGET_CHECK_ENTRY of spell ID {0}, effect {1} - selecting default targets", spellInfo.getId(), spellEffectInfo.effectIndex);

            switch (targetType.getObjectType()) {
                case Gobj:
                    if (spellInfo.getRequiresSpellFocus() != 0) {
                        if (focusObject != null) {
                            addGOTarget(focusObject, effMask);
                        } else {
                            sendCastResult(SpellCastResult.BadImplicitTargets);
                            finish(SpellCastResult.BadImplicitTargets);
                        }

                        return;
                    }

                    break;
                case Dest:
                    if (spellInfo.getRequiresSpellFocus() != 0) {
                        if (focusObject != null) {
                            SpellDestination dest = new SpellDestination(focusObject);

                            if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
                                dest.position.setO(spellEffectInfo.positionFacing);
                            }

                            tangible.RefObject<SpellDestination> tempRef_dest = new tangible.RefObject<SpellDestination>(dest);
                            callScriptDestinationTargetSelectHandlers(tempRef_dest, spellEffectInfo.effectIndex, targetType);
                            dest = tempRef_dest.refArgValue;
                            targets.setDst(dest);
                        } else {
                            sendCastResult(SpellCastResult.BadImplicitTargets);
                            finish(SpellCastResult.BadImplicitTargets);
                        }

                        return;
                    }

                    break;
                default:
                    break;
            }
        }

        var target = searchNearbyTarget(range, targetType.getObjectType(), targetType.getCheckType(), condList);

        if (target == null) {
            Log.outDebug(LogFilter.spells, "Spell.SelectImplicitNearbyTargets: cannot find nearby target for spell ID {0}, effect {1}", spellInfo.getId(), spellEffectInfo.effectIndex);
            sendCastResult(SpellCastResult.BadImplicitTargets);
            finish(SpellCastResult.BadImplicitTargets);

            return;
        }

        tangible.RefObject<WorldObject> tempRef_target = new tangible.RefObject<WorldObject>(target);
        callScriptObjectTargetSelectHandlers(tempRef_target, spellEffectInfo.effectIndex, targetType);
        target = tempRef_target.refArgValue;

        if (!target) {
            Log.outDebug(LogFilter.spells, String.format("Spell.SelectImplicitNearbyTargets: OnObjectTargetSelect script hook for spell Id %1$s set NULL target, effect %2$s", spellInfo.getId(), spellEffectInfo.effectIndex));
            sendCastResult(SpellCastResult.BadImplicitTargets);
            finish(SpellCastResult.BadImplicitTargets);

            return;
        }

        switch (targetType.getObjectType()) {
            case Unit:
                var unitTarget = target.toUnit();

                if (unitTarget != null) {
                    addUnitTarget(unitTarget, effMask, true, false);
                } else {
                    Log.outDebug(LogFilter.spells, String.format("Spell.SelectImplicitNearbyTargets: OnObjectTargetSelect script hook for spell Id %1$s set object of wrong type, expected unit, got %2$s, effect %3$s", spellInfo.getId(), target.getGUID().getHigh(), effMask));
                    sendCastResult(SpellCastResult.BadImplicitTargets);
                    finish(SpellCastResult.BadImplicitTargets);

                    return;
                }

                break;
            case Gobj:
                var gobjTarget = target.toGameObject();

                if (gobjTarget != null) {
                    addGOTarget(gobjTarget, effMask);
                } else {
                    Log.outDebug(LogFilter.spells, String.format("Spell.SelectImplicitNearbyTargets: OnObjectTargetSelect script hook for spell Id %1$s set object of wrong type, expected gameobject, got %2$s, effect %3$s", spellInfo.getId(), target.getGUID().getHigh(), effMask));
                    sendCastResult(SpellCastResult.BadImplicitTargets);
                    finish(SpellCastResult.BadImplicitTargets);

                    return;
                }

                break;
            case Corpse:
                var corpseTarget = target.toCorpse();

                if (corpseTarget != null) {
                    addCorpseTarget(corpseTarget, effMask);
                } else {
                    Log.outDebug(LogFilter.spells, String.format("Spell::SelectImplicitNearbyTargets: OnObjectTargetSelect script hook for spell Id %1$s set object of wrong type, expected corpse, got %2$s, effect %3$s", spellInfo.getId(), target.getGUID().getTypeId(), effMask));
                    sendCastResult(SpellCastResult.BadImplicitTargets);
                    finish(SpellCastResult.BadImplicitTargets);

                    return;
                }

                break;
            case Dest:
                SpellDestination dest = new SpellDestination(target);

                if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
                    dest.position.setO(spellEffectInfo.positionFacing);
                }

                tangible.RefObject<SpellDestination> tempRef_dest2 = new tangible.RefObject<SpellDestination>(dest);
                callScriptDestinationTargetSelectHandlers(tempRef_dest2, spellEffectInfo.effectIndex, targetType);
                dest = tempRef_dest2.refArgValue;
                targets.setDst(dest);

                break;
        }

        selectImplicitChainTargets(spellEffectInfo, targetType, target, effMask);
    }

    private void selectImplicitConeTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType, HashSet<Integer> effMask) {
        Position coneSrc = new Position(caster.getLocation());
        var coneAngle = spellInfo.getConeAngle();

        switch (targetType.getReferenceType()) {
            case Caster:
                break;
            case Dest:
                if (caster.getLocation().getExactdist2D(targets.getDstPos()) > 0.1f) {
                    coneSrc.setO(caster.getLocation().getAbsoluteAngle(targets.getDstPos()));
                }

                break;
            default:
                break;
        }

        switch (targetType.getTarget()) {
            case Framework.Constants.targets.UnitCone180DegEnemy:
                if (coneAngle == 0.0f) {
                    coneAngle = 180.0f;
                }

                break;
            default:
                break;
        }

        ArrayList<WorldObject> targets = new ArrayList<>();
        var objectType = targetType.getObjectType();
        var selectionType = targetType.getCheckType();

        var condList = spellEffectInfo.implicitTargetConditions;
        var radius = spellEffectInfo.calcRadius(caster) * spellValue.radiusMod;

        var containerTypeMask = getSearcherTypeMask(objectType, condList);

        if (containerTypeMask != 0) {
            var extraSearchRadius = radius > 0.0f ? SharedConst.ExtraCellSearchRadius : 0.0f;
            var spellCone = new WorldObjectSpellConeTargetCheck(coneSrc, MathUtil.DegToRad(coneAngle), spellInfo.getWidth() != 0 ? spellInfo.getWidth() : caster.getCombatReach(), radius, caster, spellInfo, selectionType, condList, objectType);
            var searcher = new WorldObjectListSearcher(caster, targets, spellCone, containerTypeMask);
            searchTargets(searcher, containerTypeMask, caster, caster.getLocation(), radius + extraSearchRadius);

            callScriptObjectAreaTargetSelectHandlers(targets, spellEffectInfo.effectIndex, targetType);

            if (!targets.isEmpty()) {
                // Other special target selection goes here
                var maxTargets = spellValue.maxAffectedTargets;

                if (maxTargets != 0) {
                    targets.RandomResize(maxTargets);
                }

                for (var obj : targets) {
                    if (obj.isUnit()) {
                        addUnitTarget(obj.toUnit(), effMask, false);
                    } else if (obj.isGameObject()) {
                        addGOTarget(obj.toGameObject(), effMask);
                    } else if (obj.isCorpse()) {
                        addCorpseTarget(obj.toCorpse(), effMask);
                    }
                }
            }
        }
    }

    private void selectImplicitAreaTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType, HashSet<Integer> effMask) {
        WorldObject referer = null;

        switch (targetType.getReferenceType()) {
            case Src:
            case Dest:
            case Caster:
                referer = caster;

                break;
            case Target:
                referer = targets.getUnitTarget();

                break;
            case Last: {
                referer = caster;

                // find last added target for this effect
                for (var target : uniqueTargetInfo) {
                    if (target.effects.contains(spellEffectInfo.effectIndex)) {
                        referer = global.getObjAccessor().GetUnit(caster, target.targetGuid);

                        break;
                    }
                }

                break;
            }
        }

        if (referer == null) {
            return;
        }

        Position center = caster.getLocation();

        switch (targetType.getReferenceType()) {
            case Src:
                center = targets.getSrcPos();

                break;
            case Dest:
                center = targets.getDstPos();

                break;
            case Caster:
            case Target:
            case Last:
                center = referer.getLocation();

                break;
        }

        var radius = spellEffectInfo.calcRadius(caster) * spellValue.radiusMod;
        ArrayList<WorldObject> targets = new ArrayList<>();

        switch (targetType.getTarget()) {
            case Framework.Constants.targets.UnitCasterAndPassengers:
                targets.add(caster);
                var unit = caster.toUnit();

                if (unit != null) {
                    var vehicleKit = unit.getVehicleKit();

                    if (vehicleKit != null) {
                        for (byte seat = 0; seat < SharedConst.MaxVehicleSeats; ++seat) {
                            var passenger = vehicleKit.GetPassenger(seat);

                            if (passenger != null) {
                                targets.add(passenger);
                            }
                        }
                    }
                }

                break;
            case Framework.Constants.targets.UnitTargetAllyOrRaid:
                var targetedUnit = targets.getUnitTarget();

                if (targetedUnit != null) {
                    if (!caster.isUnit() || !caster.toUnit().isInRaidWith(targetedUnit)) {
                        targets.add(targets.getUnitTarget());
                    } else {
                        searchAreaTargets(targets, radius, targetedUnit.getLocation(), referer, targetType.getObjectType(), targetType.getCheckType(), spellEffectInfo.implicitTargetConditions);
                    }
                }

                break;
            case Framework.Constants.targets.UnitCasterAndSummons:
                targets.add(caster);
                searchAreaTargets(targets, radius, center, referer, targetType.getObjectType(), targetType.getCheckType(), spellEffectInfo.implicitTargetConditions);

                break;
            default:
                searchAreaTargets(targets, radius, center, referer, targetType.getObjectType(), targetType.getCheckType(), spellEffectInfo.implicitTargetConditions);

                break;
        }

        if (targetType.getObjectType() == SpellTargetObjectTypes.UnitAndDest) {
            SpellDestination dest = new SpellDestination(referer);

            if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
                dest.position.setO(spellEffectInfo.positionFacing);
            }

            tangible.RefObject<SpellDestination> tempRef_dest = new tangible.RefObject<SpellDestination>(dest);
            callScriptDestinationTargetSelectHandlers(tempRef_dest, spellEffectInfo.effectIndex, targetType);
            dest = tempRef_dest.refArgValue;

            targets.modDst(dest);
        }

        callScriptObjectAreaTargetSelectHandlers(targets, spellEffectInfo.effectIndex, targetType);

        if (targetType.getTarget() == targets.UnitSrcAreaFurthestEnemy) {
            collections.sort(targets, new ObjectDistanceOrderPred(referer, false));
        }

        if (!targets.isEmpty()) {
            // Other special target selection goes here
            var maxTargets = spellValue.maxAffectedTargets;

            if (maxTargets != 0) {
                if (targetType.getTarget() != targets.UnitSrcAreaFurthestEnemy) {
                    targets.RandomResize(maxTargets);
                } else if (targets.size() > maxTargets) {
                    targets.Resize(maxTargets);
                }
            }

            for (var obj : targets) {
                if (obj.isUnit()) {
                    addUnitTarget(obj.toUnit(), effMask, false, true, center);
                } else if (obj.isGameObject()) {
                    addGOTarget(obj.toGameObject(), effMask);
                } else if (obj.isCorpse()) {
                    addCorpseTarget(obj.toCorpse(), effMask);
                }
            }
        }
    }

    private void selectImplicitCasterDestTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType) {
        SpellDestination dest = new SpellDestination(caster);

        switch (targetType.getTarget()) {
            case Framework.Constants.targets.DestCaster:
                break;
            case Framework.Constants.targets.DestHome:
                var playerCaster = caster.toPlayer();

                if (playerCaster != null) {
                    dest = new SpellDestination(playerCaster.getHomeBind());
                }

                break;
            case Framework.Constants.targets.DestDb:
                var st = global.getSpellMgr().getSpellTargetPosition(spellInfo.getId(), spellEffectInfo.effectIndex);

                if (st != null) {
                    // @todo fix this check
                    if (spellInfo.hasEffect(SpellEffectName.TeleportUnits) || spellInfo.hasEffect(SpellEffectName.TeleportWithSpellVisualKitLoadingScreen) || spellInfo.hasEffect(SpellEffectName.Bind)) {
                        dest = new SpellDestination(st.X, st.Y, st.Z, st.orientation, st.targetMapId);
                    } else if (st.targetMapId == caster.getLocation().getMapId()) {
                        dest = new SpellDestination(st.X, st.Y, st.Z, st.orientation);
                    }
                } else {
                    Log.outDebug(LogFilter.spells, "SPELL: unknown target coordinates for spell ID {0}", spellInfo.getId());
                    var target = targets.getObjectTarget();

                    if (target) {
                        dest = new SpellDestination(target);
                    }
                }

                break;
            case Framework.Constants.targets.DestCasterFishing: {
                var minDist = spellInfo.getMinRange(true);
                var maxDist = spellInfo.getMaxRange(true);
                var dis = (float) RandomUtil.randomFloat() * (maxDist - minDist) + minDist;
                var angle = (float) RandomUtil.randomFloat() * (MathUtil.PI * 35.0f / 180.0f) - (float) (Math.PI * 17.5f / 180.0f);
                var pos = new Position();
                caster.getClosePoint(pos, SharedConst.DefaultPlayerBoundingRadius, dis, angle);

                var ground = caster.getMapHeight(pos);
                var liquidLevel = MapDefine.VMAPInvalidHeightValue;

                LiquidData liquidData;
                tangible.OutObject<LiquidData> tempOut_liquidData = new tangible.OutObject<LiquidData>();
                if (caster.getMap().getLiquidStatus(caster.getPhaseShift(), pos, LiquidHeaderTypeFlags.AllLiquids, tempOut_liquidData, caster.getCollisionHeight()) != 0) {
                    liquidData = tempOut_liquidData.outArgValue;
                    liquidLevel = liquidData.level;
                } else {
                    liquidData = tempOut_liquidData.outArgValue;
                }

                if (liquidLevel <= ground) // When there is no liquid Map.GetWaterOrGroundLevel returns ground level
                {
                    sendCastResult(SpellCastResult.NotHere);
                    sendChannelUpdate(0);
                    finish(SpellCastResult.NotHere);

                    return;
                }

                if (ground + 0.75 > liquidLevel) {
                    sendCastResult(SpellCastResult.TooShallow);
                    sendChannelUpdate(0);
                    finish(SpellCastResult.TooShallow);

                    return;
                }

                dest = new SpellDestination(pos.getX(), pos.getY(), liquidLevel, caster.getLocation().getO());

                break;
            }
            case Framework.Constants.targets.DestCasterFrontLeap:
            case Framework.Constants.targets.DestCasterMovementDirection: {
                var unitCaster = caster.toUnit();

                if (unitCaster == null) {
                    break;
                }

                var dist = spellEffectInfo.calcRadius(unitCaster);
                var angle = targetType.calcDirectionAngle();

                if (targetType.getTarget() == targets.DestCasterMovementDirection) {
                    switch (caster.getMovementInfo().getMovementFlags().getValue() & (MovementFlag.Forward.getValue() | MovementFlag.Backward.getValue().getValue() | MovementFlag.StrafeLeft.getValue().getValue().getValue() | MovementFlag.StrafeRight.getValue().getValue().getValue()).getValue()) {
                        case None:
                        case Forward:
                        case Forward | Backward:
                        case StrafeLeft | StrafeRight:
                        case Forward | StrafeLeft | StrafeRight:
                        case Forward | Backward | StrafeLeft | StrafeRight:
                            angle = 0.0f;

                            break;
                        case Backward:
                        case Backward | StrafeLeft | StrafeRight:
                            angle = (float) Math.PI;

                            break;
                        case StrafeLeft:
                        case Forward | Backward | StrafeLeft:
                            angle = ((float) Math.PI / 2);

                            break;
                        case Forward | StrafeLeft:
                            angle = ((float) Math.PI / 4);

                            break;
                        case Backward | StrafeLeft:
                            angle = (3 * (float) Math.PI / 4);

                            break;
                        case StrafeRight:
                        case Forward | Backward | StrafeRight:
                            angle = (-(float) Math.PI / 2);

                            break;
                        case Forward | StrafeRight:
                            angle = (-(float) Math.PI / 4);

                            break;
                        case Backward | StrafeRight:
                            angle = (-3 * (float) Math.PI / 4);

                            break;
                        default:
                            angle = 0.0f;

                            break;
                    }
                }

                Position pos = new Position(dest.position);

                unitCaster.movePositionToFirstCollision(pos, dist, angle);
                dest.relocate(pos);

                break;
            }
            case Framework.Constants.targets.DestCasterGround:
            case Framework.Constants.targets.DestCasterGround2:
                dest.position.setZ(caster.getMapWaterOrGroundLevel(dest.position.getX(), dest.position.getY(), dest.position.getZ()));

                break;
            case Framework.Constants.targets.DestSummoner: {
                var unitCaster = caster.toUnit();

                if (unitCaster != null) {
                    var casterSummon = unitCaster.toTempSummon();

                    if (casterSummon != null) {
                        var summoner = casterSummon.getSummoner();

                        if (summoner != null) {
                            dest = new SpellDestination(summoner);
                        }
                    }
                }

                break;
            }
            default: {
                var dist = spellEffectInfo.calcRadius(caster);
                var angl = targetType.calcDirectionAngle();
                var objSize = caster.getCombatReach();

                switch (targetType.getTarget()) {
                    case Framework.Constants.targets.DestCasterSummon:
                        dist = SharedConst.PetFollowDist;

                        break;
                    case Framework.Constants.targets.DestCasterRandom:
                        if (dist > objSize) {
                            dist = objSize + (dist - objSize) * (float) RandomUtil.randomFloat();
                        }

                        break;
                    case Framework.Constants.targets.DestCasterFrontLeft:
                    case Framework.Constants.targets.DestCasterBackLeft:
                    case Framework.Constants.targets.DestCasterFrontRight:
                    case Framework.Constants.targets.DestCasterBackRight: {
                        var DefaultTotemDistance = 3.0f;

                        if (!spellEffectInfo.getHasRadius() && !spellEffectInfo.getHasMaxRadius()) {
                            dist = DefaultTotemDistance;
                        }

                        break;
                    }
                    default:
                        break;
                }

                if (dist < objSize) {
                    dist = objSize;
                }

                Position pos = new Position(dest.position);
                caster.movePositionToFirstCollision(pos, dist, angl);

                dest.relocate(pos);

                break;
            }
        }

        if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
            dest.position.setO(spellEffectInfo.positionFacing);
        }

        tangible.RefObject<SpellDestination> tempRef_dest = new tangible.RefObject<SpellDestination>(dest);
        callScriptDestinationTargetSelectHandlers(tempRef_dest, spellEffectInfo.effectIndex, targetType);
        dest = tempRef_dest.refArgValue;
        targets.setDst(dest);
    }

    private void selectImplicitTargetDestTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType) {
        var target = targets.getObjectTarget();

        SpellDestination dest = new SpellDestination(target);

        switch (targetType.getTarget()) {
            case Framework.Constants.targets.DestTargetEnemy:
            case Framework.Constants.targets.DestAny:
            case Framework.Constants.targets.DestTargetAlly:
                break;
            default: {
                var angle = targetType.calcDirectionAngle();
                var dist = spellEffectInfo.calcRadius(null);

                if (targetType.getTarget() == targets.DestRandom) {
                    dist *= (float) RandomUtil.randomFloat();
                }

                Position pos = new Position(dest.position);
                target.movePositionToFirstCollision(pos, dist, angle);

                dest.relocate(pos);
            }

            break;
        }

        if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
            dest.position.setO(spellEffectInfo.positionFacing);
        }

        tangible.RefObject<SpellDestination> tempRef_dest = new tangible.RefObject<SpellDestination>(dest);
        callScriptDestinationTargetSelectHandlers(tempRef_dest, spellEffectInfo.effectIndex, targetType);
        dest = tempRef_dest.refArgValue;
        targets.setDst(dest);
    }

    private void selectImplicitDestDestTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType) {
        // set destination to caster if no dest provided
        // can only happen if previous destination target could not be set for some reason
        // (not found nearby target, or channel target for example
        // maybe we should abort the spell in such case?
        checkDst();

        var dest = targets.getDst();

        switch (targetType.getTarget()) {
            case Framework.Constants.targets.DestDynobjEnemy:
            case Framework.Constants.targets.DestDynobjAlly:
            case Framework.Constants.targets.DestDynobjNone:
            case Framework.Constants.targets.DestDest:
                break;
            case Framework.Constants.targets.DestDestGround:
                dest.position.setZ(caster.getMapHeight(dest.position.getX(), dest.position.getY(), dest.position.getZ()));

                break;
            default: {
                var angle = targetType.calcDirectionAngle();
                var dist = spellEffectInfo.calcRadius(caster);

                if (targetType.getTarget() == targets.DestRandom) {
                    dist *= (float) RandomUtil.randomFloat();
                }

                Position pos = new Position(targets.getDstPos());
                caster.movePositionToFirstCollision(pos, dist, angle);

                dest.relocate(pos);
            }

            break;
        }

        if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
            dest.position.setO(spellEffectInfo.positionFacing);
        }

        tangible.RefObject<SpellDestination> tempRef_dest = new tangible.RefObject<SpellDestination>(dest);
        callScriptDestinationTargetSelectHandlers(tempRef_dest, spellEffectInfo.effectIndex, targetType);
        dest = tempRef_dest.refArgValue;
        targets.modDst(dest);
    }

    private void selectImplicitCasterObjectTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType) {
        WorldObject target = null;
        var checkIfValid = true;

        switch (targetType.getTarget()) {
            case Framework.Constants.targets.UnitCaster:
                target = caster;
                checkIfValid = false;

                break;
            case Framework.Constants.targets.UnitMaster:
                target = caster.getCharmerOrOwner();

                break;
            case Framework.Constants.targets.UnitPet: {
                var unitCaster = caster.toUnit();

                if (unitCaster != null) {
                    target = unitCaster.getGuardianPet();
                }

                break;
            }
            case Framework.Constants.targets.UnitSummoner: {
                var unitCaster = caster.toUnit();

                if (unitCaster != null) {
                    if (unitCaster.isSummon()) {
                        target = unitCaster.toTempSummon().getSummonerUnit();
                    }
                }

                break;
            }
            case Framework.Constants.targets.UnitVehicle: {
                var unitCaster = caster.toUnit();

                if (unitCaster != null) {
                    target = unitCaster.getVehicleBase();
                }

                break;
            }
            case Framework.Constants.targets.UnitPassenger0:
            case Framework.Constants.targets.UnitPassenger1:
            case Framework.Constants.targets.UnitPassenger2:
            case Framework.Constants.targets.UnitPassenger3:
            case Framework.Constants.targets.UnitPassenger4:
            case Framework.Constants.targets.UnitPassenger5:
            case Framework.Constants.targets.UnitPassenger6:
            case Framework.Constants.targets.UnitPassenger7:
                var vehicleBase = caster.toCreature();

                if (vehicleBase != null && vehicleBase.isVehicle()) {
                    target = vehicleBase.getVehicleKit().GetPassenger((byte) (targetType.getTarget() - targets.UnitPassenger0));
                }

                break;
            case Framework.Constants.targets.UnitTargetTapList:
                var creatureCaster = caster.toCreature();

                if (creatureCaster != null && !creatureCaster.getTapList().isEmpty()) {
                    target = global.getObjAccessor().GetWorldObject(creatureCaster, creatureCaster.getTapList().SelectRandom());
                }

                break;
            case Framework.Constants.targets.UnitOwnCritter: {
                var unitCaster = caster.toUnit();

                if (unitCaster != null) {
                    target = ObjectAccessor.GetCreatureOrPetOrVehicle(caster, unitCaster.getCritterGUID());
                }

                break;
            }
            default:
                break;
        }

        tangible.RefObject<WorldObject> tempRef_target = new tangible.RefObject<WorldObject>(target);
        callScriptObjectTargetSelectHandlers(tempRef_target, spellEffectInfo.effectIndex, targetType);
        target = tempRef_target.refArgValue;

        if (target) {
            if (target.isUnit()) {
                addUnitTarget(target.toUnit(), spellEffectInfo.effectIndex, checkIfValid);
            } else if (target.isGameObject()) {
                addGOTarget(target.toGameObject(), spellEffectInfo.effectIndex);
            } else if (target.isCorpse()) {
                addCorpseTarget(target.toCorpse(), spellEffectInfo.effectIndex);
            }
        }
    }

    private void selectImplicitTargetObjectTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType) {
        var target = targets.getObjectTarget();

        tangible.RefObject<WorldObject> tempRef_target = new tangible.RefObject<WorldObject>(target);
        callScriptObjectTargetSelectHandlers(tempRef_target, spellEffectInfo.effectIndex, targetType);
        target = tempRef_target.refArgValue;

        var item = targets.getItemTarget();

        if (target != null) {
            if (target.isUnit()) {
                addUnitTarget(target.toUnit(), spellEffectInfo.effectIndex, true, false);
            } else if (target.isGameObject()) {
                addGOTarget(target.toGameObject(), spellEffectInfo.effectIndex);
            } else if (target.isCorpse()) {
                addCorpseTarget(target.toCorpse(), spellEffectInfo.effectIndex);
            }

            selectImplicitChainTargets(spellEffectInfo, targetType, target, spellEffectInfo.effectIndex);
        }
        // Script hook can remove object target and we would wrongly land here
        else if (item != null) {
            addItemTarget(item, spellEffectInfo.effectIndex);
        }
    }

    private void selectImplicitChainTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType, WorldObject target, int effIndex) {
        selectImplicitChainTargets(spellEffectInfo, targetType, target, new HashSet<Integer>() {
            effIndex
        });
    }

    private void selectImplicitChainTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType, WorldObject target, HashSet<Integer> effMask) {
        var maxTargets = spellEffectInfo.chainTargets;
        var modOwner = caster.getSpellModOwner();

        if (modOwner) {
            tangible.RefObject<Integer> tempRef_maxTargets = new tangible.RefObject<Integer>(maxTargets);
            modOwner.applySpellMod(spellInfo, SpellModOp.chainTargets, tempRef_maxTargets, this);
            maxTargets = tempRef_maxTargets.refArgValue;
        }

        if (maxTargets > 1) {
            // mark damage multipliers as used
            for (var eff : spellInfo.getEffects()) {
                if (effMask.contains(eff.effectIndex)) {
                    damageMultipliers.put(spellEffectInfo.effectIndex, 1.0f);
                }
            }

            applyMultiplierMask.UnionWith(effMask);

            ArrayList<WorldObject> targets = new ArrayList<>();
            searchChainTargets(targets, (int) maxTargets - 1, target, targetType.getObjectType(), targetType.getCheckType(), spellEffectInfo, targetType.getTarget() == targets.UnitChainhealAlly);

            // Chain primary target is added earlier
            callScriptObjectAreaTargetSelectHandlers(targets, spellEffectInfo.effectIndex, targetType);

            Position losPosition = spellInfo.hasAttribute(SpellAttr2.ChainFromCaster) ? caster.getLocation() : target.getLocation();

            for (var obj : targets) {
                var unitTarget = obj.toUnit();

                if (unitTarget) {
                    addUnitTarget(unitTarget, effMask, false, true, losPosition);
                }

                if (!spellInfo.hasAttribute(SpellAttr2.ChainFromCaster) && !spellEffectInfo.effectAttributes.hasFlag(SpellEffectAttributes.ChainFromInitialTarget)) {
                    losPosition = obj.getLocation();
                }
            }
        }
    }

    private float tangent(float x) {
        x = (float) Math.tan(x);

        if (x < 100000.0f && x > -100000.0f) {
            return x;
        }
        if (x >= 100000.0f) {
            return 100000.0f;
        }
        if (x <= 100000.0f) {
            return -100000.0f;
        }

        return 0.0f;
    }

    private void selectImplicitTrajTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType) {
        if (!targets.getHasTraj()) {
            return;
        }

        var dist2d = targets.getDist2d();

        if (dist2d == 0) {
            return;
        }

        var srcPos = targets.getSrcPos();
        srcPos.setO(caster.getLocation().getO());
        var srcToDestDelta = targets.getDstPos().getZ() - srcPos.getZ();

        ArrayList<WorldObject> targets = new ArrayList<>();
        var spellTraj = new WorldObjectSpellTrajTargetCheck(dist2d, srcPos, caster, spellInfo, targetType.getCheckType(), spellEffectInfo.implicitTargetConditions, SpellTargetObjectTypes.NONE);
        var searcher = new WorldObjectListSearcher(caster, targets, spellTraj);
        searchTargets(searcher, GridMapTypeMask.All, caster, srcPos, dist2d);

        if (targets.isEmpty()) {
            return;
        }

        collections.sort(targets, new ObjectDistanceOrderPred(caster));

        var b = tangent(targets.getPitch());
        var a = (srcToDestDelta - dist2d * b) / (dist2d * dist2d);

        if (a > -0.0001f) {
            a = 0f;
        }

        // We should check if triggered spell has greater range (which is true in many cases, and initial spell has too short max range)
        // limit max range to 300 yards, sometimes triggered spells can have 50000yds
        var bestDist = spellInfo.getMaxRange(false);
        var triggerSpellInfo = global.getSpellMgr().getSpellInfo(spellEffectInfo.triggerSpell, getCastDifficulty());

        if (triggerSpellInfo != null) {
            bestDist = Math.min(Math.max(bestDist, triggerSpellInfo.getMaxRange(false)), Math.min(dist2d, 300.0f));
        }

        // GameObjects don't cast traj
        var unitCaster = caster.toUnit();

        for (var obj : targets) {
            if (spellInfo.checkTarget(unitCaster, obj, true) != SpellCastResult.SpellCastOk) {
                continue;
            }

            var unitTarget = obj.toUnit();

            if (unitTarget) {
                if (unitCaster == obj || unitCaster.isOnVehicle(unitTarget) || unitTarget.getVehicle1()) {
                    continue;
                }

                var creatureTarget = unitTarget.toCreature();

                if (creatureTarget) {
                    if (!creatureTarget.getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.CollideWithMissiles)) {
                        continue;
                    }
                }
            }

            var size = Math.max(obj.getCombatReach(), 1.0f);
            var objDist2d = srcPos.getExactdist2D(obj.getLocation());
            var dz = obj.getLocation().getZ() - srcPos.getZ();

            var horizontalDistToTraj = (float) Math.abs(objDist2d * Math.sin(srcPos.getRelativeAngle(obj.getLocation())));
            var sizeFactor = (float) Math.cos((horizontalDistToTraj / size) * (Math.PI / 2.0f));
            var distToHitPoint = (float) Math.max(objDist2d * Math.cos(srcPos.getRelativeAngle(obj.getLocation())) - size * sizeFactor, 0.0f);
            var height = distToHitPoint * (a * distToHitPoint + b);

            if (Math.abs(dz - height) > size + b / 2.0f + SpellConst.TrajectoryMissileSize) {
                continue;
            }

            if (distToHitPoint < bestDist) {
                bestDist = distToHitPoint;

                break;
            }
        }

        if (dist2d > bestDist) {
            var x = (float) (targets.getSrcPos().getX() + Math.cos(unitCaster.getLocation().getO()) * bestDist);
            var y = (float) (targets.getSrcPos().getY() + Math.sin(unitCaster.getLocation().getO()) * bestDist);
            var z = targets.getSrcPos().getZ() + bestDist * (a * bestDist + b);

            SpellDestination dest = new SpellDestination(x, y, z, unitCaster.getLocation().getO());

            if (spellInfo.hasAttribute(SpellAttr4.UseFacingFromSpell)) {
                dest.position.setO(spellEffectInfo.positionFacing);
            }

            tangible.RefObject<SpellDestination> tempRef_dest = new tangible.RefObject<SpellDestination>(dest);
            callScriptDestinationTargetSelectHandlers(tempRef_dest, spellEffectInfo.effectIndex, targetType);
            dest = tempRef_dest.refArgValue;
            targets.modDst(dest);
        }
    }

    private void selectImplicitLineTargets(SpellEffectInfo spellEffectInfo, SpellImplicitTargetInfo targetType, HashSet<Integer> effMask) {
        ArrayList<WorldObject> targets = new ArrayList<>();
        var objectType = targetType.getObjectType();
        var selectionType = targetType.getCheckType();

        Position dst = caster.getLocation();

        switch (targetType.getReferenceType()) {
            case Src:
                dst = targets.getSrcPos();

                break;
            case Dest:
                dst = targets.getDstPos();

                break;
            case Caster:
                dst = caster.getLocation();

                break;
            case Target:
                dst = targets.getUnitTarget().getLocation();

                break;
        }

        var condList = spellEffectInfo.implicitTargetConditions;
        var radius = spellEffectInfo.calcRadius(caster) * spellValue.radiusMod;

        var containerTypeMask = getSearcherTypeMask(objectType, condList);

        if (containerTypeMask != 0) {
            WorldObjectSpellLineTargetCheck check = new WorldObjectSpellLineTargetCheck(caster.getLocation(), dst, spellInfo.getWidth() != 0 ? spellInfo.getWidth() : caster.getCombatReach(), radius, caster, spellInfo, selectionType, condList, objectType);
            WorldObjectListSearcher searcher = new WorldObjectListSearcher(caster, targets, check, containerTypeMask);
            searchTargets(searcher, containerTypeMask, caster, caster.getLocation(), radius);

            callScriptObjectAreaTargetSelectHandlers(targets, spellEffectInfo.effectIndex, targetType);

            if (!targets.isEmpty()) {
                // Other special target selection goes here
                var maxTargets = spellValue.maxAffectedTargets;

                if (maxTargets != 0) {
                    if (maxTargets < targets.size()) {
                        collections.sort(targets, new ObjectDistanceOrderPred(caster));
                        targets.Resize(maxTargets);
                    }
                }

                for (var obj : targets) {
                    if (obj.isUnit()) {
                        addUnitTarget(obj.toUnit(), effMask, false);
                    } else if (obj.isGameObject()) {
                        addGOTarget(obj.toGameObject(), effMask);
                    } else if (obj.isCorpse()) {
                        addCorpseTarget(obj.toCorpse(), effMask);
                    }
                }
            }
        }
    }

    private void selectEffectTypeImplicitTargets(SpellEffectInfo spellEffectInfo) {
        // special case for SPELL_EFFECT_SUMMON_RAF_FRIEND and SPELL_EFFECT_SUMMON_PLAYER, queue them on map for later execution
        switch (spellEffectInfo.effect) {
            case SummonRafFriend:
            case SummonPlayer:
                if (caster.isTypeId(TypeId.PLAYER) && !caster.toPlayer().getTarget().isEmpty()) {
                    WorldObject rafTarget = global.getObjAccessor().findPlayer(caster.toPlayer().getTarget());

                    tangible.RefObject<WorldObject> tempRef_rafTarget = new tangible.RefObject<WorldObject>(rafTarget);
                    callScriptObjectTargetSelectHandlers(tempRef_rafTarget, spellEffectInfo.effectIndex, new spellImplicitTargetInfo());
                    rafTarget = tempRef_rafTarget.refArgValue;

                    // scripts may modify the target - recheck
                    if (rafTarget != null && rafTarget.isPlayer()) {
                        // target is not stored in target map for those spells
                        // since we're completely skipping AddUnitTarget logic, we need to check immunity manually
                        // eg. aura 21546 makes target immune to summons
                        var player = rafTarget.toPlayer();

                        if (player.isImmunedToSpellEffect(spellInfo, spellEffectInfo, null)) {
                            return;
                        }

                        var spell = this;
                        var targetGuid = rafTarget.getGUID();

                        rafTarget.getMap().addFarSpellCallback(map ->
                        {
                            var player = global.getObjAccessor().getPlayer(map, targetGuid);

                            if (player == null) {
                                return;
                            }

                            // check immunity again in case it changed during update
                            if (player.isImmunedToSpellEffect(spell.spellInfo, spellEffectInfo, null)) {
                                return;
                            }

                            spell.handleEffects(player, null, null, null, spellEffectInfo, SpellEffectHandleMode.HitTarget);
                        });
                    }
                }

                return;
            default:
                break;
        }

        // select spell implicit targets based on effect type
        if (spellEffectInfo.getImplicitTargetType() == 0) {
            return;
        }

        var targetMask = spellEffectInfo.getMissingTargetMask();

        if (targetMask == 0) {
            return;
        }

        WorldObject target = null;

        switch (spellEffectInfo.getImplicitTargetType()) {
            // add explicit object target or self to the target map
            case Explicit:
                // player which not released his spirit is unit, but target flag for it is TARGET_FLAG_CORPSE_MASK
                if ((boolean) (targetMask.getValue() & (SpellCastTargetFlags.UnitMask.getValue() | SpellCastTargetFlags.CorpseMask.getValue()).getValue())) {
                    var unitTarget = targets.getUnitTarget();

                    if (unitTarget != null) {
                        target = unitTarget;
                    } else if ((boolean) (targetMask.getValue() & SpellCastTargetFlags.CorpseMask.getValue())) {
                        var corpseTarget = targets.getCorpseTarget();

                        if (corpseTarget != null) {
                            target = corpseTarget;
                        }
                    } else //if (targetMask & TARGET_FLAG_UNIT_MASK)
                    {
                        target = caster;
                    }
                }

                if ((boolean) (targetMask.getValue() & SpellCastTargetFlags.itemMask.getValue())) {
                    var itemTarget = targets.getItemTarget();

                    if (itemTarget != null) {
                        addItemTarget(itemTarget, spellEffectInfo.effectIndex);
                    }

                    return;
                }

                if ((boolean) (targetMask.getValue() & SpellCastTargetFlags.GameobjectMask.getValue())) {
                    target = targets.getGOTarget();
                }

                break;
            // add self to the target map
            case Caster:
                if ((boolean) (targetMask.getValue() & SpellCastTargetFlags.UnitMask.getValue())) {
                    target = caster;
                }

                break;
            default:
                break;
        }

        tangible.RefObject<WorldObject> tempRef_target = new tangible.RefObject<WorldObject>(target);
        callScriptObjectTargetSelectHandlers(tempRef_target, spellEffectInfo.effectIndex, new spellImplicitTargetInfo());
        target = tempRef_target.refArgValue;

        if (target != null) {
            if (target.isUnit()) {
                addUnitTarget(target.toUnit(), spellEffectInfo.effectIndex, false);
            } else if (target.isGameObject()) {
                addGOTarget(target.toGameObject(), spellEffectInfo.effectIndex);
            } else if (target.isCorpse()) {
                addCorpseTarget(target.toCorpse(), spellEffectInfo.effectIndex);
            }
        }
    }

    private void searchTargets(IGridNotifier notifier, GridMapTypeMask containerMask, WorldObject referer, Position pos, float radius) {
        if (containerMask == 0) {
            return;
        }

        var searchInWorld = containerMask.hasFlag(GridMapTypeMask.CREATURE.getValue() | GridMapTypeMask.player.getValue().getValue() | GridMapTypeMask.Corpse.getValue().getValue().getValue() | GridMapTypeMask.gameObject.getValue().getValue().getValue());

        if (searchInWorld) {
            var x = pos.getX();
            var y = pos.getY();

            var p = MapDefine.computeCellCoord(x, y);
            Cell cell = new Cell(p);
            cell.setNoCreate();

            var map = referer.getMap();

            if (searchInWorld) {
                Cell.visitGrid(x, y, map, notifier, radius);
            }
        }
    }

    private WorldObject searchNearbyTarget(float range, SpellTargetObjectTypes objectType, SpellTargetCheckTypes selectionType, ArrayList<Condition> condList) {
        var containerTypeMask = getSearcherTypeMask(objectType, condList);

        if (containerTypeMask == 0) {
            return null;
        }

        var check = new WorldObjectSpellNearbyTargetCheck(range, caster, spellInfo, selectionType, condList, objectType);
        var searcher = new WorldObjectLastSearcher(caster, check, containerTypeMask);
        searchTargets(searcher, containerTypeMask, caster, caster.getLocation(), range);

        return searcher.getTarget();
    }

    private void searchAreaTargets(ArrayList<WorldObject> targets, float range, Position position, WorldObject referer, SpellTargetObjectTypes objectType, SpellTargetCheckTypes selectionType, ArrayList<Condition> condList) {
        var containerTypeMask = getSearcherTypeMask(objectType, condList);

        if (containerTypeMask == 0) {
            return;
        }

        var extraSearchRadius = range > 0.0f ? SharedConst.ExtraCellSearchRadius : 0.0f;
        var check = new WorldObjectSpellAreaTargetCheck(range, position, caster, referer, spellInfo, selectionType, condList, objectType);
        var searcher = new WorldObjectListSearcher(caster, targets, check, containerTypeMask);
        searchTargets(searcher, containerTypeMask, caster, position, range + extraSearchRadius);
    }

    private void searchChainTargets(ArrayList<WorldObject> targets, int chainTargets, WorldObject target, SpellTargetObjectTypes objectType, SpellTargetCheckTypes selectType, SpellEffectInfo spellEffectInfo, boolean isChainHeal) {
        // max dist for jump target selection
        var jumpRadius = 0.0f;

        switch (spellInfo.getDmgClass()) {
            case Ranged:
                // 7.5y for multi shot
                jumpRadius = 7.5f;

                break;
            case Melee:
                // 5y for swipe, cleave and similar
                jumpRadius = 5.0f;

                break;
            case None:
            case Magic:
                // 12.5y for chain heal spell since 3.2 patch
                if (isChainHeal) {
                    jumpRadius = 12.5f;
                }
                // 10y as default for magic chain spells
                else {
                    jumpRadius = 10.0f;
                }

                break;
        }

        var modOwner = caster.getSpellModOwner();

        if (modOwner) {
            tangible.RefObject<Float> tempRef_jumpRadius = new tangible.RefObject<Float>(jumpRadius);
            modOwner.applySpellMod(spellInfo, SpellModOp.ChainJumpDistance, tempRef_jumpRadius, this);
            jumpRadius = tempRef_jumpRadius.refArgValue;
        }

        float searchRadius;

        if (spellInfo.hasAttribute(SpellAttr2.ChainFromCaster)) {
            searchRadius = GetMinMaxRange(false).maxRange;
        } else if (spellEffectInfo.effectAttributes.hasFlag(SpellEffectAttributes.ChainFromInitialTarget)) {
            searchRadius = jumpRadius;
        } else {
            searchRadius = jumpRadius * chainTargets;
        }

        var chainSource = spellInfo.hasAttribute(SpellAttr2.ChainFromCaster) ? _caster : target;
        ArrayList<WorldObject> tempTargets = new ArrayList<>();
        searchAreaTargets(tempTargets, searchRadius, chainSource.getLocation(), caster, objectType, selectType, spellEffectInfo.implicitTargetConditions);
        tempTargets.remove(target);

        // remove targets which are always invalid for chain spells
        // for some spells allow only chain targets in front of caster (swipe for example)
        if (spellInfo.hasAttribute(SpellAttr5.MeleeChainTargeting)) {
            tangible.ListHelper.removeAll(tempTargets, obj -> !caster.getLocation().hasInArc((float) Math.PI, obj.location));
        }

        while (chainTargets != 0) {
            // try to get unit for next chain jump
            WorldObject found = null;

            // get unit with highest hp deficit in dist
            if (isChainHeal) {
                int maxHPDeficit = 0;

                for (var obj : tempTargets) {
                    var unitTarget = obj.toUnit();

                    if (unitTarget != null) {
                        var deficit = (int) (unitTarget.getMaxHealth() - unitTarget.getHealth());

                        if ((deficit > maxHPDeficit || found == null) && chainSource.isWithinDist(unitTarget, jumpRadius) && chainSource.isWithinLOSInMap(unitTarget, LineOfSightChecks.All, ModelIgnoreFlags.M2)) {
                            found = obj;
                            maxHPDeficit = deficit;
                        }
                    }
                }
            }
            // get closest object
            else {
                for (var obj : tempTargets) {
                    if (found == null) {
                        if (chainSource.isWithinDist(obj, jumpRadius) && chainSource.isWithinLOSInMap(obj, LineOfSightChecks.All, ModelIgnoreFlags.M2)) {
                            found = obj;
                        }
                    } else if (chainSource.getDistanceOrder(obj, found) && chainSource.isWithinLOSInMap(obj, LineOfSightChecks.All, ModelIgnoreFlags.M2)) {
                        found = obj;
                    }
                }
            }

            // not found any valid target - chain ends
            if (found == null) {
                break;
            }

            if (!spellInfo.hasAttribute(SpellAttr2.ChainFromCaster) && !spellEffectInfo.effectAttributes.hasFlag(SpellEffectAttributes.ChainFromInitialTarget)) {
                chainSource = found;
            }

            targets.add(found);
            tempTargets.remove(found);
            --chainTargets;
        }
    }

    private GameObject searchSpellFocus() {
        var check = new GameObjectFocusCheck(caster, spellInfo.getRequiresSpellFocus());
        var searcher = new GameObjectSearcher(caster, check, gridType.All);
        searchTargets(searcher, GridMapTypeMask.gameObject, caster, caster.getLocation(), caster.getVisibilityRange());

        return searcher.getTarget();
    }

    private void prepareDataForTriggerSystem() {
        //==========================================================================================
        // Now fill data for trigger system, need know:
        // Create base triggers flags for Attacker and victim (m_procAttacker, m_procVictim and m_hitMask)
        //==========================================================================================

        procVictim = procAttacker = new ProcFlagsInit();

        // Get data for type of attack and fill base info for trigger
        switch (spellInfo.getDmgClass()) {
            case Melee:
                procAttacker = new ProcFlagsInit(procFlags.DealMeleeAbility);

                if (attackType == WeaponAttackType.OffAttack) {
                    procAttacker.Or(procFlags.OffHandWeaponSwing);
                } else {
                    procAttacker.Or(procFlags.MainHandWeaponSwing);
                }

                procVictim = new ProcFlagsInit(procFlags.TakeMeleeAbility);

                break;
            case Ranged:
                // Auto attack
                if (spellInfo.hasAttribute(SpellAttr2.AutoRepeat)) {
                    procAttacker = new ProcFlagsInit(procFlags.DealRangedAttack);
                    procVictim = new ProcFlagsInit(procFlags.TakeRangedAttack);
                } else // Ranged spell attack
                {
                    procAttacker = new ProcFlagsInit(procFlags.DealRangedAbility);
                    procVictim = new ProcFlagsInit(procFlags.TakeRangedAbility);
                }

                break;
            default:
                if (spellInfo.getEquippedItemClass() == itemClass.Weapon && (boolean) (spellInfo.getEquippedItemSubClassMask() & (1 << ItemSubClassWeapon.wand.getValue())) && spellInfo.hasAttribute(SpellAttr2.AutoRepeat)) // Wands auto attack
                {
                    procAttacker = new ProcFlagsInit(procFlags.DealRangedAttack);
                    procVictim = new ProcFlagsInit(procFlags.TakeRangedAttack);
                }

                break;
            // For other spells trigger procflags are set in Spell::TargetInfo::DoDamageAndTriggers
            // Because spell positivity is dependant on target
        }
    }

    private void addUnitTarget(Unit target, int effIndex, boolean checkIfValid, boolean implicit) {
        addUnitTarget(target, effIndex, checkIfValid, implicit, null);
    }

    private void addUnitTarget(Unit target, int effIndex, boolean checkIfValid) {
        addUnitTarget(target, effIndex, checkIfValid, true, null);
    }

    private void addUnitTarget(Unit target, int effIndex) {
        addUnitTarget(target, effIndex, true, true, null);
    }

    private void addUnitTarget(Unit target, int effIndex, boolean checkIfValid, boolean implicit, Position losPosition) {
        addUnitTarget(target, new HashSet<Integer>() {
            effIndex
        }, checkIfValid, implicit, losPosition);
    }

    private void addUnitTarget(Unit target, HashSet<Integer> efftMask, boolean checkIfValid, boolean implicit) {
        addUnitTarget(target, efftMask, checkIfValid, implicit, null);
    }

    private void addUnitTarget(Unit target, HashSet<Integer> efftMask, boolean checkIfValid) {
        addUnitTarget(target, efftMask, checkIfValid, true, null);
    }

    private void addUnitTarget(Unit target, HashSet<Integer> efftMask) {
        addUnitTarget(target, efftMask, true, true, null);
    }

    private void addUnitTarget(Unit target, HashSet<Integer> efftMask, boolean checkIfValid, boolean implicit, Position losPosition) {
        var removeEffect = efftMask.ToHashSet();

        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (!spellEffectInfo.isEffect() || !checkEffectTarget(target, spellEffectInfo, losPosition)) {
                removeEffect.remove(spellEffectInfo.effectIndex);
            }
        }

        if (removeEffect.count == 0) {
            return;
        }

        if (checkIfValid) {
            if (spellInfo.checkTarget(caster, target, implicit) != SpellCastResult.SpellCastOk) // skip stealth checks for AOE
            {
                return;
            }
        }

        // Check for effect immune skip if immuned
        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (target.isImmunedToSpellEffect(spellInfo, spellEffectInfo, caster)) {
                removeEffect.remove(spellEffectInfo.effectIndex);
            }
        }

        var targetGUID = target.getGUID();

        // Lookup target in already in list
        var index = tangible.ListHelper.findIndex(uniqueTargetInfo, target -> Objects.equals(target.targetGuid, targetGUID));

        if (index != -1) // Found in list
        {
            // Immune effects removed from mask
            uniqueTargetInfo.get(index).effects.UnionWith(removeEffect);

            return;
        }

        // remove immunities

        // This is new target calculate data for him

        // Get spell hit result on target
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.targetGuid = targetGUID; // Store target GUID
        targetInfo.effects = removeEffect; // Store all effects not immune
        targetInfo.isAlive = target.isAlive();

        // Calculate hit result
        var caster = _originalCaster ? _originalCaster : caster;
        targetInfo.missCondition = caster.spellHitResult(target, spellInfo, canReflect && !(isPositive() && caster.isFriendlyTo(target)));

        // Spell have speed - need calculate incoming time
        // Incoming time is zero for self casts. At least I think so.
        if (caster != target) {
            var hitDelay = spellInfo.getLaunchDelay();
            var missileSource = caster;

            if (spellInfo.hasAttribute(SpellAttr4.BouncyChainMissiles)) {
                var mask = removeEffect.ToMask();
                var previousTargetInfo = tangible.ListHelper.findLast(uniqueTargetInfo, target -> (target.effects.ToMask() & mask) != 0);

                if (previousTargetInfo != null) {
                    hitDelay = 0.0f; // this is not the first target in chain, LaunchDelay was already included

                    var previousTarget = global.getObjAccessor().GetWorldObject(caster, previousTargetInfo.targetGuid);

                    if (previousTarget != null) {
                        missileSource = previousTarget;
                    }

                    targetInfo.timeDelay += previousTargetInfo.timeDelay;
                }
            }

            if (spellInfo.hasAttribute(SpellAttr9.SpecialDelayCalculation)) {
                hitDelay += spellInfo.getSpeed();
            } else if (spellInfo.getSpeed() > 0.0f) {
                // calculate spell incoming interval
                /** @todo this is a hack
                 */
                var dist = Math.max(missileSource.getDistance(target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ()), 5.0f);
                hitDelay += dist / spellInfo.getSpeed();
            }

            targetInfo.timeDelay += (long) Math.floor(hitDelay * 1000.0f);
        } else {
            targetInfo.timeDelay = 0L;
        }

        // If target reflect spell back to caster
        if (targetInfo.missCondition == SpellMissInfo.Reflect) {
            // Calculate reflected spell result on caster (shouldn't be able to reflect gameobject spells)
            var unitCaster = caster.toUnit();
            targetInfo.reflectResult = unitCaster.spellHitResult(unitCaster, spellInfo, false); // can't reflect twice

            // Proc spell reflect aura when missile hits the original target
            target.getEvents().addEvent(new ProcReflectDelayed(target, originalCasterGuid), target.getEvents().CalculateTime(duration.ofSeconds(targetInfo.timeDelay)), true);

            // Increase time interval for reflected spells by 1.5
            targetInfo.timeDelay += targetInfo.timeDelay >>> 1;
        } else {
            targetInfo.reflectResult = SpellMissInfo.NONE;
        }

        // Calculate minimum incoming time
        if (targetInfo.timeDelay != 0 && (delayMoment == 0 || delayMoment > targetInfo.timeDelay)) {
            delayMoment = targetInfo.timeDelay;
        }

        // Add target to list
        uniqueTargetInfo.add(targetInfo);
        uniqueTargetInfoOrgi.add(targetInfo);
    }

    private void addGOTarget(GameObject go, int effIndex) {
        addGOTarget(go, new HashSet<Integer>() {
            effIndex
        });
    }

    private void addGOTarget(GameObject go, HashSet<Integer> effMask) {
        var effectMask = effMask.ToHashSet();

        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (!spellEffectInfo.isEffect() || !checkEffectTarget(go, spellEffectInfo)) {
                effectMask.remove(spellEffectInfo.effectIndex);
            }
        }

        // no effects left
        if (effectMask.count == 0) {
            return;
        }

        var targetGUID = go.getGUID();

        // Lookup target in already in list
        var index = tangible.ListHelper.findIndex(uniqueGoTargetInfo, target -> Objects.equals(target.targetGUID, targetGUID));

        if (index != -1) // Found in list
        {
            // Add only effect mask
            uniqueGoTargetInfo.get(index).effects.UnionWith(effectMask);

            return;
        }

        // This is new target calculate data for him
        GOTargetInfo target = new GOTargetInfo();
        target.targetGUID = targetGUID;
        target.effects = effectMask;

        // Spell have speed - need calculate incoming time
        if (caster != go) {
            var hitDelay = spellInfo.getLaunchDelay();

            if (spellInfo.hasAttribute(SpellAttr9.SpecialDelayCalculation)) {
                hitDelay += spellInfo.getSpeed();
            } else if (spellInfo.getSpeed() > 0.0f) {
                // calculate spell incoming interval
                var dist = Math.max(caster.getDistance(go.getLocation().getX(), go.getLocation().getY(), go.getLocation().getZ()), 5.0f);
                hitDelay += dist / spellInfo.getSpeed();
            }

            target.timeDelay = (long) Math.floor(hitDelay * 1000.0f);
        } else {
            target.timeDelay = 0;
        }

        // Calculate minimum incoming time
        if (target.timeDelay != 0 && (delayMoment == 0 || delayMoment > target.timeDelay)) {
            delayMoment = target.timeDelay;
        }

        // Add target to list
        uniqueGoTargetInfo.add(target);
    }

    private void addItemTarget(Item item, int effectIndex) {
        addItemTarget(item, new HashSet<Integer>() {
            effectIndex
        });
    }

    private void addItemTarget(Item item, HashSet<Integer> effMask) {
        var effectMask = effMask.ToHashSet();

        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (!spellEffectInfo.isEffect() || !checkEffectTarget(item, spellEffectInfo)) {
                effectMask.remove(spellEffectInfo.effectIndex);
            }
        }

        // no effects left
        if (effectMask.count == 0) {
            return;
        }

        // Lookup target in already in list
        var index = tangible.ListHelper.findIndex(uniqueItemInfo, target -> target.targetItem == item);

        if (index != -1) // Found in list
        {
            // Add only effect mask
            uniqueItemInfo.get(index).effects.UnionWith(effectMask);

            return;
        }

        // This is new target add data

        ItemTargetInfo target = new ItemTargetInfo();
        target.targetItem = item;
        target.effects = effectMask;

        uniqueItemInfo.add(target);
    }

    private void addCorpseTarget(Corpse corpse, int effIndex) {
        addCorpseTarget(corpse, new HashSet<Integer>() {
            effIndex
        });
    }

    private void addCorpseTarget(Corpse corpse, HashSet<Integer> effMask) {
        var effectMask = effMask.ToHashSet();

        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (!spellEffectInfo.isEffect()) {
                effectMask.remove(spellEffectInfo.effectIndex);
            }
        }

        // no effects left
        if (effectMask.count == 0) {
            return;
        }

        var targetGUID = corpse.getGUID();

        // Lookup target in already in list
        var corpseTargetInfo = tangible.ListHelper.find(uniqueCorpseTargetInfo, target ->
        {
            return Objects.equals(target.targetGuid, targetGUID);
        });

        if (corpseTargetInfo != null) // Found in list
        {
            // Add only effect mask
            corpseTargetInfo.effects.UnionWith(effectMask);

            return;
        }

        // This is new target calculate data for him
        CorpseTargetInfo target = new CorpseTargetInfo();
        target.targetGuid = targetGUID;
        target.effects = effectMask;

        // Spell have speed - need calculate incoming time
        if (caster != corpse) {
            var hitDelay = spellInfo.getLaunchDelay();

            if (spellInfo.hasAttribute(SpellAttr9.SpecialDelayCalculation)) {
                hitDelay += spellInfo.getSpeed();
            } else if (spellInfo.getSpeed() > 0.0f) {
                // calculate spell incoming interval
                var dist = Math.max(caster.getDistance(corpse.getLocation().getX(), corpse.getLocation().getY(), corpse.getLocation().getZ()), 5.0f);
                hitDelay += dist / spellInfo.getSpeed();
            }

            target.timeDelay = (long) Math.floor(hitDelay * 1000.0f);
        } else {
            target.timeDelay = 0;
        }

        // Calculate minimum incoming time
        if (target.timeDelay != 0 && (delayMoment == 0 || delayMoment > target.timeDelay)) {
            delayMoment = target.timeDelay;
        }

        // Add target to list
        uniqueCorpseTargetInfo.add(target);
    }

    private void addDestTarget(SpellDestination dest, int effIndex) {
        destTargets.put(effIndex, dest);
    }

    private boolean updateChanneledTargetList() {
        // Not need check return true
        if (channelTargetEffectMask.isEmpty()) {
            return true;
        }

        var channelTargetEffectMask = channelTargetEffectMask;
        var channelAuraMask = new HashSet<Integer>();

        for (var spellEffectInfo : spellInfo.getEffects()) {
            if (spellEffectInfo.isEffect(SpellEffectName.ApplyAura)) {
                channelAuraMask.add(spellEffectInfo.effectIndex);
            }
        }

        channelAuraMask.IntersectWith(channelTargetEffectMask);

        float range = 0;

        if (!channelAuraMask.isEmpty()) {
            range = spellInfo.getMaxRange(isPositive());
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Float> tempRef_range = new tangible.RefObject<Float>(range);
                modOwner.applySpellMod(spellInfo, SpellModOp.Range, tempRef_range, this);
                range = tempRef_range.refArgValue;
            }

            // add little tolerance level
            range += Math.min(3.0f, range * 0.1f); // 10% but no more than 3.0f
        }

        for (var targetInfo : uniqueTargetInfo) {
            if (targetInfo.missCondition == SpellMissInfo.NONE && (boolean) (channelTargetEffectMask.ToMask() & targetInfo.effects.ToMask())) {
                var unit = Objects.equals(caster.getGUID(), targetInfo.targetGuid) ? caster.toUnit() : global.getObjAccessor().GetUnit(caster, targetInfo.targetGuid);

                if (unit == null) {
                    var unitCaster = caster.toUnit();

                    if (unitCaster != null) {
                        unitCaster.removeChannelObject(targetInfo.targetGuid);
                    }

                    continue;
                }

                if (isValidDeadOrAliveTarget(unit)) {
                    if ((boolean) (channelAuraMask.ToMask() & targetInfo.effects.ToMask())) {
                        var aurApp = unit.getAuraApplication(spellInfo.getId(), originalCasterGuid);

                        if (aurApp != null) {
                            if (caster != unit && !caster.isWithinDistInMap(unit, range)) {
                                targetInfo.effects.ExceptWith(aurApp.getEffectMask());
                                unit.removeAura(aurApp);
                                var unitCaster = caster.toUnit();

                                if (unitCaster != null) {
                                    unitCaster.removeChannelObject(targetInfo.targetGuid);
                                }

                                continue;
                            }
                        } else // aura is dispelled
                        {
                            var unitCaster = caster.toUnit();

                            if (unitCaster != null) {
                                unitCaster.removeChannelObject(targetInfo.targetGuid);
                            }

                            continue;
                        }
                    }

                    channelTargetEffectMask.ExceptWith(targetInfo.effects); // remove from need alive mask effect that have alive target
                }
            }
        }

        // is all effects from m_needAliveTargetMask have alive targets
        return channelTargetEffectMask.isEmpty();
    }

    private void cast() {
        cast(false);
    }

    private void cast(boolean skipCheck) {
        if (!updatePointers()) {
            // cancel the spell if updatePointers() returned false, something wrong happened there
            cancel();

            return;
        }

        // cancel at lost explicit target during cast
        if (!targets.getObjectTargetGUID().isEmpty() && targets.getObjectTarget() == null) {
            cancel();

            return;
        }

        var playerCaster = caster.toPlayer();

        if (playerCaster != null) {
            // now that we've done the basic check, now run the scripts
            // should be done before the spell is actually executed
            global.getScriptMgr().<IPlayerOnSpellCast>ForEach(playerCaster.getClass(), p -> p.onSpellCast(playerCaster, this, skipCheck));

            // As of 3.0.2 pets begin attacking their owner's target immediately
            // Let any pets know we've attacked something. Check DmgClass for harmful spells only
            // This prevents spells such as Hunter's Mark from triggering pet attack
            if (spellInfo.getDmgClass() != SpellDmgClass.NONE) {
                var target = targets.getUnitTarget();

                if (target != null) {
                    for (var controlled : playerCaster.getControlled()) {
                        var cControlled = controlled.toCreature();

                        if (cControlled != null) {
                            var controlledAI = cControlled.getAI();

                            if (controlledAI != null) {
                                controlledAI.ownerAttacked(target);
                            }
                        }
                    }
                }
            }
        }

        setExecutedCurrently(true);

        // Should this be done for original caster?
        var modOwner = caster.getSpellModOwner();

        if (modOwner != null) {
            // Set spell which will drop charges for triggered cast spells
            // if not successfully casted, will be remove in finish(false)
            modOwner.setSpellModTakingSpell(this, true);
        }

        callScriptBeforeCastHandlers();

        // skip check if done already (for instant cast spells for example)
        if (!skipCheck) {

//			void cleanupSpell(SpellCastResult result, System.Nullable<int> param1 = null, System.Nullable<int> param2 = null)
//				{
//					sendCastResult(result, param1, param2);
//					sendInterrupted(0);
//
//					if (modOwner)
//						modOwner.setSpellModTakingSpell(this, false);
//
//					finish(result);
//					setExecutedCurrently(false);
//				}

            int param1 = 0, param2 = 0;
            tangible.RefObject<Integer> tempRef_param1 = new tangible.RefObject<Integer>(param1);
            tangible.RefObject<Integer> tempRef_param2 = new tangible.RefObject<Integer>(param2);
            var castResult = checkCast(false, tempRef_param1, tempRef_param2);
            param2 = tempRef_param2.refArgValue;
            param1 = tempRef_param1.refArgValue;

            if (castResult != SpellCastResult.SpellCastOk) {
                cleanupSpell(castResult, param1, param2);

                return;
            }

            // additional check after cast bar completes (must not be in CheckCast)
            // if trade not complete then remember it in trade data
            if ((boolean) (targets.getTargetMask().getValue() & SpellCastTargetFlags.TradeItem.getValue())) {
                if (modOwner) {
                    var my_trade = modOwner.getTradeData();

                    if (my_trade != null) {
                        if (!my_trade.isInAcceptProcess()) {
                            // Spell will be casted at completing the trade. Silently ignore at this place
                            my_trade.setSpell(spellInfo.getId(), castItem);
                            cleanupSpell(SpellCastResult.DontReport);

                            return;
                        }
                    }
                }
            }

            // check diminishing returns (again, only after finish cast bar, tested on retail)
            var target = targets.getUnitTarget();

            if (target != null) {
                var isAura = false;

                for (var spellEffectInfo : spellInfo.getEffects()) {
                    if (spellEffectInfo.isUnitOwnedAuraEffect()) {
                        isAura = true;

                        break;
                    }
                }

                if (isAura) {
                    if (spellInfo.getDiminishingReturnsGroupForSpell() != 0) {
                        var type = spellInfo.getDiminishingReturnsGroupType();

                        if (type == DiminishingReturnsType.All || (type == DiminishingReturnsType.player && target.isAffectedByDiminishingReturns())) {
                            var caster1 = _originalCaster ? _originalCaster : caster.toUnit();

                            if (caster1 != null) {
                                if (target.hasStrongerAuraWithDR(spellInfo, caster1)) {
                                    cleanupSpell(SpellCastResult.AuraBounced);

                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        // The spell focusing is making sure that we have a valid cast target guid when we need it so only check for a guid second here.
        var creatureCaster = caster.toCreature();

        if (creatureCaster != null) {
            if (!creatureCaster.getTarget().isEmpty() && !creatureCaster.hasUnitFlag(UnitFlag.Possessed)) {
                WorldObject target = global.getObjAccessor().GetUnit(creatureCaster, creatureCaster.getTarget());

                if (target != null) {
                    creatureCaster.setInFront(target);
                }
            }
        }

        selectSpellTargets();

        // Spell may be finished after target map check
        if (spellState == SpellState.Finished) {
            sendInterrupted((byte) 0);

            if (caster.isTypeId(TypeId.PLAYER)) {
                caster.toPlayer().setSpellModTakingSpell(this, false);
            }

            finish(SpellCastResult.Interrupted);
            setExecutedCurrently(false);

            return;
        }

        var unitCaster = caster.toUnit();

        if (unitCaster != null) {
            if (spellInfo.hasAttribute(SpellAttr1.DismissPetFirst)) {
                var pet = ObjectAccessor.getCreature(caster, unitCaster.getPetGUID());

                if (pet != null) {
                    pet.despawnOrUnsummon();
                }
            }
        }

        prepareTriggersExecutedOnHit();

        callScriptOnCastHandlers();

        // traded items have trade slot instead of guid in m_itemTargetGUID
        // set to real guid to be sent later to the client
        targets.updateTradeSlotItem();

        var player = caster.toPlayer();

        if (player != null) {
            if (!(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreCastItem.getValue()) && castItem != null) {
                player.startCriteriaTimer(CriteriaStartEvent.UseItem, castItem.getEntry());
                player.updateCriteria(CriteriaType.UseItem, castItem.getEntry());
            }

            player.updateCriteria(CriteriaType.castSpell, spellInfo.getId());
        }

        var targetItem = targets.getItemTarget();

        if (!(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnorePowerAndReagentCost.getValue())) {
            // Powers have to be taken before SendSpellGo
            takePower();
            takeReagents(); // we must remove reagents before HandleEffects to allow place crafted item in same slot
        } else if (targetItem != null) {
            // Not own traded item (in trader trade slot) req. reagents including triggered spell case
            if (ObjectGuid.opNotEquals(targetItem.getOwnerGUID(), caster.getGUID())) {
                takeReagents();
            }
        }

        // CAST SPELL
        if (!spellInfo.hasAttribute(SpellAttr12.StartCooldownOnCastStart)) {
            sendSpellCooldown();
        }

        if (spellInfo.getLaunchDelay() == 0) {
            handleLaunchPhase();
            launchHandled = true;
        }

        // we must send smsg_spell_go packet before m_castItem delete in takeCastItem()...
        sendSpellGo();

        if (!spellInfo.isChanneled()) {
            if (creatureCaster != null) {
                creatureCaster.releaseSpellFocus(this);
            }
        }

        // Okay, everything is prepared. Now we need to distinguish between immediate and evented delayed spells
        if ((spellInfo.getHasHitDelay() && !spellInfo.isChanneled()) || spellInfo.hasAttribute(SpellAttr4.NoHarmfulThreat)) {
            // Remove used for cast item if need (it can be already NULL after TakeReagents call
            // in case delayed spell remove item at cast delay start
            takeCastItem();

            // Okay, maps created, now prepare flags
            immediateHandled = false;
            spellState = SpellState.Delayed;
            setDelayStart(0);

            unitCaster = caster.toUnit();

            if (unitCaster != null) {
                if (unitCaster.hasUnitState(UnitState.Casting) && !unitCaster.isNonMeleeSpellCast(false, false, true)) {
                    unitCaster.clearUnitState(UnitState.Casting);
                }
            }
        } else {
            // Immediate spell, no big deal
            handleImmediate();
        }

        callScriptAfterCastHandlers();

        var spell_triggered = global.getSpellMgr().getSpellLinked(SpellLinkedType.cast, spellInfo.getId());

        if (spell_triggered != null) {
            for (var spellId : spell_triggered) {
                if (spellId < 0) {
                    unitCaster = caster.toUnit();

                    if (unitCaster != null) {
                        unitCaster.removeAura((int) -spellId);
                    }
                } else {
                    caster.castSpell(targets.getUnitTarget() != null ? targets.getUnitTarget() : caster, (int) spellId, (new CastSpellExtraArgs(TriggerCastFlag.FullMask)).setTriggeringSpell(this));
                }
            }
        }

        if (modOwner != null) {
            modOwner.setSpellModTakingSpell(this, false);

            //Clear spell cooldowns after every spell is cast if .cheat cooldown is enabled.
            if (originalCaster != null && modOwner.getCommandStatus(PlayerCommandStates.cooldown)) {
                originalCaster.getSpellHistory().resetCooldown(spellInfo.getId(), true);
                originalCaster.getSpellHistory().restoreCharge(spellInfo.chargeCategoryId);
            }
        }

        setExecutedCurrently(false);

        if (!originalCaster) {
            return;
        }

        // Handle procs on cast
        var procAttacker = procAttacker;

        if (!procAttacker) {
            if (spellInfo.hasAttribute(SpellAttr3.TreatAsPeriodic)) {
                if (isPositive()) {
                    procAttacker.Or(procFlags.DealHelpfulPeriodic);
                } else {
                    procAttacker.Or(procFlags.DealHarmfulPeriodic);
                }
            } else if (spellInfo.hasAttribute(SpellAttr0.IsAbility)) {
                if (isPositive()) {
                    procAttacker.Or(procFlags.DealHelpfulAbility);
                } else {
                    procAttacker.Or(procFlags.DealHarmfulSpell);
                }
            } else {
                if (isPositive()) {
                    procAttacker.Or(procFlags.DealHelpfulSpell);
                } else {
                    procAttacker.Or(procFlags.DealHarmfulSpell);
                }
            }
        }

        procAttacker.Or(ProcFlags2.CastSuccessful);

        var hitMask = hitMask;

        if (!hitMask.hasFlag(ProcFlagsHit.critical)) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.NORMAL.getValue());
        }

        if (!triggeredCastFlags.hasFlag(TriggerCastFlag.IgnoreAuraInterruptFlags) && !spellInfo.hasAttribute(SpellAttr2.NotAnAction)) {
            originalCaster.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.ActionDelayed, spellInfo);
        }

        if (!spellInfo.hasAttribute(SpellAttr3.SuppressCasterProcs)) {
            unit.procSkillsAndAuras(originalCaster, null, procAttacker, new ProcFlagsInit(procFlags.NONE), ProcFlagsSpellType.MaskAll, ProcFlagsSpellPhase.cast, hitMask, this, null, null);
        }

        // Call CreatureAI hook OnSpellCast
        var caster = originalCaster.toCreature();

        if (caster) {
            if (caster.isAIEnabled()) {
                caster.getAI().onSpellCast(spellInfo);
            }
        }
    }

    private <T extends TargetInfoBase> void doProcessTargetContainer(ArrayList<T> targetContainer) {
        for (TargetInfoBase target : targetContainer) {
            target.preprocessTarget(this);
        }

        for (var spellEffectInfo : spellInfo.getEffects()) {
            for (TargetInfoBase target : targetContainer) {
                if (target.effects.contains(spellEffectInfo.effectIndex)) {
                    target.doTargetSpellHit(this, spellEffectInfo);
                }
            }
        }

        for (TargetInfoBase target : targetContainer) {
            target.doDamageAndTriggers(this);
        }
    }

    private void handleImmediate() {
        // start channeling if applicable
        if (spellInfo.isChanneled()) {
            int duration;
            tangible.OutObject<Integer> tempOut_duration = new tangible.OutObject<Integer>();
            if (!tryGetTotalEmpowerDuration(true, tempOut_duration)) {
                duration = tempOut_duration.outArgValue;
                duration = spellInfo.getDuration();
            } else {
                duration = tempOut_duration.outArgValue;
            }

            if (duration > 0 || spellValue.duration != null) {
                if (spellValue.duration == null) {
                    // First mod_duration then haste - see Missile Barrage
                    // Apply duration mod
                    var modOwner = caster.getSpellModOwner();

                    if (modOwner != null) {
                        tangible.RefObject<Integer> tempRef_duration = new tangible.RefObject<Integer>(duration);
                        modOwner.applySpellMod(spellInfo, SpellModOp.duration, tempRef_duration);
                        duration = tempRef_duration.refArgValue;
                    }

                    duration = (int) (duration * spellValue.durationMul);

                    // Apply haste mods
                    tangible.RefObject<Integer> tempRef_duration2 = new tangible.RefObject<Integer>(duration);
                    caster.modSpellDurationTime(spellInfo, tempRef_duration2, this);
                    duration = tempRef_duration2.refArgValue;
                } else {
                    duration = spellValue.duration.intValue();
                }

                channeledDuration = duration;
                sendChannelStart((int) duration);
            } else if (duration == -1) {

                sendChannelStart((int) duration);
            }

            if (duration != 0) {
                spellState = SpellState.Casting;

                // GameObjects shouldn't cast channeled spells
                if (caster.toUnit() != null) {
                    caster.toUnit().addInterruptMask(spellInfo.getChannelInterruptFlags(), spellInfo.getChannelInterruptFlags2());
                }
            }
        }

        prepareTargetProcessing();

        // process immediate effects (items, ground, etc.) also initialize some variables
        handle_immediate_phase();

        // consider spell hit for some spells without target, so they may proc on finish phase correctly
        if (uniqueTargetInfo.isEmpty()) {
            hitMask = ProcFlagsHit.NORMAL;
        } else {
            doProcessTargetContainer(uniqueTargetInfo);
        }

        doProcessTargetContainer(uniqueGoTargetInfo);

        doProcessTargetContainer(uniqueCorpseTargetInfo);
        callScriptOnHitHandlers();

        finishTargetProcessing();

        // spell is finished, perform some last features of the spell here
        handle_finish_phase();

        // Remove used for cast item if need (it can be already NULL after TakeReagents call
        takeCastItem();

        if (spellState != SpellState.Casting) {
            finish(); // successfully finish spell cast (not last in case autorepeat or channel spell)
        }
    }

    private void handle_immediate_phase() {
        // handle some immediate features of the spell here
        handleThreatSpells();

        // handle effects with SPELL_EFFECT_HANDLE_HIT mode
        for (var spellEffectInfo : spellInfo.getEffects()) {
            // don't do anything for empty effect
            if (!spellEffectInfo.isEffect()) {
                continue;
            }

            // call effect handlers to handle destination hit
            handleEffects(null, null, null, null, spellEffectInfo, SpellEffectHandleMode.hit);
        }

        // process items
        doProcessTargetContainer(uniqueItemInfo);
    }

    private void handle_finish_phase() {
        var unitCaster = caster.toUnit();

        if (unitCaster != null) {
            // Take for real after all targets are processed
            if (needComboPoints) {
                unitCaster.clearComboPoints();
            }

            // Real add combo points from effects
            if (comboPointGain != 0) {
                unitCaster.addComboPoints(comboPointGain);
            }

            if (spellInfo.hasEffect(SpellEffectName.AddExtraAttacks)) {
                unitCaster.setLastExtraAttackSpell(spellInfo.getId());
            }
        }

        // Handle procs on finish
        if (!originalCaster) {
            return;
        }

        var procAttacker = procAttacker;

        if (!procAttacker) {
            if (spellInfo.hasAttribute(SpellAttr3.TreatAsPeriodic)) {
                if (isPositive()) {
                    procAttacker.Or(procFlags.DealHelpfulPeriodic);
                } else {
                    procAttacker.Or(procFlags.DealHarmfulPeriodic);
                }
            } else if (spellInfo.hasAttribute(SpellAttr0.IsAbility)) {
                if (isPositive()) {
                    procAttacker.Or(procFlags.DealHelpfulAbility);
                } else {
                    procAttacker.Or(procFlags.DealHarmfulAbility);
                }
            } else {
                if (isPositive()) {
                    procAttacker.Or(procFlags.DealHelpfulSpell);
                } else {
                    procAttacker.Or(procFlags.DealHarmfulSpell);
                }
            }
        }

        if (!spellInfo.hasAttribute(SpellAttr3.SuppressCasterProcs)) {
            unit.procSkillsAndAuras(originalCaster, null, procAttacker, new ProcFlagsInit(procFlags.NONE), ProcFlagsSpellType.MaskAll, ProcFlagsSpellPhase.Finish, hitMask, this, null, null);
        }
    }

    private void sendSpellCooldown() {
        if (!caster.isUnit()) {
            return;
        }

        if (castItem) {
            caster.toUnit().getSpellHistory().handleCooldowns(spellInfo, castItem, this);
        } else {
            caster.toUnit().getSpellHistory().handleCooldowns(spellInfo, castItemEntry, this);
        }

        if (isAutoRepeat()) {
            caster.toUnit().resetAttackTimer(WeaponAttackType.RangedAttack);
        }
    }

    private void updateEmpoweredSpell(int difftime) {
        Player p;
        tangible.OutObject<Player> tempOut_p = new tangible.OutObject<Player>();
        if (getPlayerIfIsEmpowered(tempOut_p)) {
            p = tempOut_p.outArgValue;
            if (empowerState == EmpowerState.NONE && empoweredSpellDelta >= 1000) {
                empowerState = EmpowerState.Prepared;
                _empoweredSpellDelta -= 1000;
            }

            if (empowerState == EmpowerState.CanceledStartup && empoweredSpellDelta >= 1000) {
                empowerState = EmpowerState.Canceled;
            }

            TValue stageinfo;
            if (empowerState == EmpowerState.Prepared && empoweredSpellStage == 0 && (empowerStages.containsKey(empoweredSpellStage) && (stageinfo = empowerStages.get(empoweredSpellStage)) == stageinfo)) // send stage 0
            {
                this.<ISpellOnEpowerSpellStageChange>ForEachSpellScript(s -> s.EmpowerSpellStageChange(null, stageinfo));
                var stageZero = new SpellEmpowerSetStage();
                stageZero.stage = 0;
                stageZero.caster = p.getGUID();
                stageZero.castID = castId;
                p.sendPacket(stageZero);
                empowerState = EmpowerState.Empowering;
            }

            empoweredSpellDelta += difftime;

            if (empowerState == EmpowerState.Empowering && (empowerStages.containsKey(empoweredSpellStage) && (stageinfo = empowerStages.get(empoweredSpellStage)) == stageinfo) && empoweredSpellDelta >= stageinfo.durationMs) {
                var nextStageId = empoweredSpellStage;
                nextStageId++;

                TValue nextStage;
                if (empowerStages.containsKey(nextStageId) && (nextStage = empowerStages.get(nextStageId)) == nextStage) {
                    empoweredSpellStage = nextStageId;
                    _empoweredSpellDelta -= stageinfo.durationMs;
                    var stageUpdate = new SpellEmpowerSetStage();
                    stageUpdate.stage = 0;
                    stageUpdate.caster = p.getGUID();
                    stageUpdate.castID = castId;
                    p.sendPacket(stageUpdate);
                    this.<ISpellOnEpowerSpellStageChange>ForEachSpellScript(s -> s.EmpowerSpellStageChange(stageinfo, nextStage));
                } else {
                    empowerState = EmpowerState.Finished;
                }
            }

            if (empowerState == EmpowerState.Finished || empowerState == EmpowerState.Canceled) {
                timer = 0;
            }
        } else {
            p = tempOut_p.outArgValue;
        }
    }

    private void sendMountResult(MountResult result) {
        if (result == MountResult.Ok) {
            return;
        }

        if (!caster.isPlayer()) {
            return;
        }

        var caster = caster.toPlayer();

        if (caster.isLoading()) // don't send mount results at loading time
        {
            return;
        }

        MountResultPacket packet = new MountResultPacket();
        packet.result = (int) result.getValue();
        caster.sendPacket(packet);
    }

    private void sendSpellStart() {
        if (!isNeedSendToClient()) {
            return;
        }

        var castFlags = SpellCastFlags.HasTrajectory;
        int schoolImmunityMask = 0;
        long mechanicImmunityMask = 0;
        var unitCaster = caster.toUnit();

        if (unitCaster != null) {
            schoolImmunityMask = timer != 0 ? unitCaster.getSchoolImmunityMask() : 0;
            mechanicImmunityMask = timer != 0 ? spellInfo.getMechanicImmunityMask(unitCaster) : 0;
        }

        if (schoolImmunityMask != 0 || mechanicImmunityMask != 0) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.Immunity.getValue());
        }

        if (((isTriggered() && !spellInfo.isAutoRepeatRangedSpell()) || triggeredByAuraSpell != null) && !fromClient) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.Pending.getValue());
        }

        if (spellInfo.hasAttribute(SpellAttr0.UsesRangedSlot) || spellInfo.hasAttribute(SpellAttr10.UsesRangedSlotCosmeticOnly) || spellInfo.hasAttribute(SpellCustomAttributes.NeedsAmmoData)) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.Projectile.getValue());
        }

        if ((caster.isTypeId(TypeId.PLAYER) || (caster.isTypeId(TypeId.UNIT) && caster.toCreature().isPet())) && powerCosts.Any(cost -> cost.power != powerType.health)) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.PowerLeftSelf.getValue());
        }

        if (hasPowerTypeCost(powerType.runes)) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.NoGCD.getValue()); // not needed, but Blizzard sends it
        }

        SpellStart packet = new SpellStart();
        var castData = packet.cast;

        if (castItem) {
            castData.casterGUID = castItem.getGUID();
        } else {
            castData.casterGUID = caster.getGUID();
        }

        castData.casterUnit = caster.getGUID();
        castData.castID = castId;
        castData.originalCastID = originalCastId;
        castData.spellID = (int) spellInfo.getId();
        castData.visual = spellVisual;
        castData.castFlags = castFlags;
        castData.castFlagsEx = castFlagsEx;
        castData.castTime = (int) casttime;

        targets.write(castData.target);

        if (castFlags.hasFlag(SpellCastFlags.PowerLeftSelf)) {
            for (var cost : powerCosts) {
                SpellPowerData powerData = new SpellPowerData();
                powerData.type = cost.power;
                powerData.cost = caster.toUnit().getPower(cost.power);
                castData.remainingPower.add(powerData);
            }
        }

        if (castFlags.hasFlag(SpellCastFlags.RuneList)) // rune cooldowns list
        {
            castData.remainingRunes = new runeData();

            var runeData = castData.remainingRunes;
            //TODO: There is a crash caused by a spell with CAST_FLAG_RUNE_LIST casted by a creature
            //The creature is the mover of a player, so HandleCastSpellOpcode uses it as the caster

            var player = caster.toPlayer();

            if (player) {
                runeData.start = runesState; // runes state before
                runeData.count = player.getRunesState(); // runes state after

                for (byte i = 0; i < player.getMaxPower(powerType.runes); ++i) {
                    // float casts ensure the division is performed on floats as we need float result
                    float baseCd = player.getRuneBaseCooldown();
                    runeData.cooldowns.add((byte) ((baseCd - player.getRuneCooldown(i)) / baseCd * 255)); // rune cooldown passed
                }
            } else {
                runeData.start = 0;
                runeData.count = 0;

                for (byte i = 0; i < player.getMaxPower(powerType.runes); ++i) {
                    runeData.cooldowns.add((byte) 0);
                }
            }
        }

        updateSpellCastDataAmmo(castData.ammo);

        if (castFlags.hasFlag(SpellCastFlags.Immunity)) {
            castData.immunities.school = schoolImmunityMask;
            castData.immunities.value = (int) mechanicImmunityMask;
        }

        /** @todo implement heal prediction packet data
        if (castFlags & CAST_FLAG_HEAL_PREDICTION)
        {
        castData.predict.BeconGUID = ??
        castData.predict.points = 0;
        castData.predict.type = 0;
        }**/

        caster.sendMessageToSet(packet, true);
    }

    private void sendSpellGo() {
        // not send invisible spell casting
        if (!isNeedSendToClient()) {
            return;
        }

        Log.outDebug(LogFilter.spells, "Sending SMSG_SPELL_GO id={0}", spellInfo.getId());

        var castFlags = SpellCastFlags.unk9;

        // triggered spells with spell visual != 0
        if (((isTriggered() && !spellInfo.isAutoRepeatRangedSpell()) || triggeredByAuraSpell != null) && !fromClient) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.Pending.getValue());
        }

        if (spellInfo.hasAttribute(SpellAttr0.UsesRangedSlot) || spellInfo.hasAttribute(SpellAttr10.UsesRangedSlotCosmeticOnly) || spellInfo.hasAttribute(SpellCustomAttributes.NeedsAmmoData)) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.Projectile.getValue()); // arrows/bullets visual
        }

        if ((caster.isTypeId(TypeId.PLAYER) || (caster.isTypeId(TypeId.UNIT) && caster.toCreature().isPet())) && powerCosts.Any(cost -> cost.power != powerType.health)) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.PowerLeftSelf.getValue());
        }

        if (caster.isTypeId(TypeId.PLAYER) && caster.toPlayer().getClass() == playerClass.Deathknight && hasPowerTypeCost(powerType.runes) && !triggeredCastFlags.hasFlag(TriggerCastFlag.IgnorePowerAndReagentCost)) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.NoGCD.getValue()); // same as in SMSG_SPELL_START
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.RuneList.getValue()); // rune cooldowns list
        }

        if (targets.getHasTraj()) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.AdjustMissile.getValue());
        }

        if (spellInfo.getStartRecoveryTime() == 0) {
            castFlags = SpellCastFlags.forValue(castFlags.getValue() | SpellCastFlags.NoGCD.getValue());
        }

        SpellGo packet = new SpellGo();
        var castData = packet.cast;

        if (castItem != null) {
            castData.casterGUID = castItem.getGUID();
        } else {
            castData.casterGUID = caster.getGUID();
        }

        castData.casterUnit = caster.getGUID();
        castData.castID = castId;
        castData.originalCastID = originalCastId;
        castData.spellID = (int) spellInfo.getId();
        castData.visual = spellVisual;
        castData.castFlags = castFlags;
        castData.castFlagsEx = castFlagsEx;
        castData.castTime = System.currentTimeMillis();

        castData.hitTargets = new ArrayList<>();
        updateSpellCastDataTargets(castData);

        targets.write(castData.target);

        if ((boolean) (castFlags.getValue() & SpellCastFlags.PowerLeftSelf.getValue())) {
            castData.remainingPower = new ArrayList<>();

            for (var cost : powerCosts) {
                SpellPowerData powerData = new SpellPowerData();
                powerData.type = cost.power;
                powerData.cost = caster.toUnit().getPower(cost.power);
                castData.remainingPower.add(powerData);
            }
        }

        if ((boolean) (castFlags.getValue() & SpellCastFlags.RuneList.getValue())) // rune cooldowns list
        {
            castData.remainingRunes = new runeData();
            var runeData = castData.remainingRunes;

            var player = caster.toPlayer();
            runeData.start = runesState; // runes state before
            runeData.count = player.getRunesState(); // runes state after

            for (byte i = 0; i < player.getMaxPower(powerType.runes); ++i) {
                // float casts ensure the division is performed on floats as we need float result
                var baseCd = (float) player.getRuneBaseCooldown();
                runeData.cooldowns.add((byte) ((baseCd - (float) player.getRuneCooldown(i)) / baseCd * 255)); // rune cooldown passed
            }
        }

        if (castFlags.hasFlag(SpellCastFlags.AdjustMissile)) {
            castData.missileTrajectory.travelTime = (int) delayMoment;
            castData.missileTrajectory.pitch = targets.getPitch();
        }

        packet.logData.initialize(this);

        caster.sendCombatLogMessage(packet);

        Player p;
        tangible.OutObject<Player> tempOut_p = new tangible.OutObject<Player>();
        if (getPlayerIfIsEmpowered(tempOut_p)) {
            p = tempOut_p.outArgValue;
            this.<ISpellOnEpowerSpellStart>ForEachSpellScript(s -> s.EmpowerSpellStart());
            SpellEmpowerStart spellEmpowerSart = new SpellEmpowerStart();
            spellEmpowerSart.castID = packet.cast.castID;
            spellEmpowerSart.caster = packet.cast.casterGUID;
            spellEmpowerSart.targets = uniqueTargetInfo.Select(t -> t.targetGuid).ToList();
            spellEmpowerSart.spellID = spellInfo.getId();
            spellEmpowerSart.visual = packet.cast.visual;
            int dur;
            tangible.OutObject<Integer> tempOut_dur = new tangible.OutObject<Integer>();
            tryGetTotalEmpowerDuration(false, tempOut_dur);
            dur = tempOut_dur.outArgValue;
            spellEmpowerSart.duration = (int) dur;
            spellEmpowerSart.firstStageDuration = empowerStages.FirstOrDefault().value.durationMs;
            spellEmpowerSart.finalStageDuration = empowerStages.LastOrDefault().value.durationMs;
            spellEmpowerSart.stageDurations = empowerStages.ToDictionary(kvp -> kvp.key, kvp -> kvp.value.durationMs);

            var schoolImmunityMask = p.getSchoolImmunityMask();
            var mechanicImmunityMask = p.getMechanicImmunityMask();

            if (schoolImmunityMask != 0 || mechanicImmunityMask != 0) {
                SpellChannelStartInterruptImmunities interruptImmunities = new SpellChannelStartInterruptImmunities();
                interruptImmunities.schoolImmunities = (int) schoolImmunityMask;
                interruptImmunities.immunities = (int) mechanicImmunityMask;

                spellEmpowerSart.immunities = interruptImmunities;
            }

            p.sendPacket(spellEmpowerSart);
        } else {
            p = tempOut_p.outArgValue;
        }
    }

    // Writes miss and hit targets for a SMSG_SPELL_GO packet
    private void updateSpellCastDataTargets(SpellCastData data) {
        // This function also fill data for channeled spells:
        // m_needAliveTargetMask req for stop channelig if one target die
        for (var targetInfo : uniqueTargetInfo) {
            if (targetInfo.effects.isEmpty()) // No effect apply - all immuned add state
            {
                // possibly SPELL_MISS_IMMUNE2 for this??
                targetInfo.missCondition = SpellMissInfo.Immune2;
            }

            if (targetInfo.missCondition == SpellMissInfo.NONE || (targetInfo.missCondition == SpellMissInfo.Block && !spellInfo.hasAttribute(SpellAttr3.CompletelyBlocked))) // Add only hits and partial blocked
            {
                data.hitTargets.add(targetInfo.targetGuid);
                data.hitStatus.add(new SpellHitStatus(SpellMissInfo.NONE));

                channelTargetEffectMask.UnionWith(targetInfo.effects);
            } else // misses
            {
                data.missTargets.add(targetInfo.targetGuid);

                data.missStatus.add(new SpellMissStatus(targetInfo.missCondition, targetInfo.reflectResult));
            }
        }

        for (var targetInfo : uniqueGoTargetInfo) {
            data.hitTargets.add(targetInfo.targetGUID); // Always hits
        }

        for (var targetInfo : uniqueCorpseTargetInfo) {
            data.hitTargets.add(targetInfo.targetGuid); // Always hits
        }

        // Reset m_needAliveTargetMask for non channeled spell
        if (!spellInfo.isChanneled()) {
            channelTargetEffectMask.clear();
        }
    }

    private void updateSpellCastDataAmmo(SpellAmmo ammo) {
        InventoryType ammoInventoryType = inventoryType.forValue(0);
        int ammoDisplayID = 0;

        var playerCaster = caster.toPlayer();

        if (playerCaster != null) {
            var pItem = playerCaster.getWeaponForAttack(WeaponAttackType.RangedAttack);

            if (pItem) {
                ammoInventoryType = pItem.getTemplate().getInventoryType();

                if (ammoInventoryType == inventoryType.Thrown) {
                    ammoDisplayID = pItem.getDisplayId(playerCaster);
                } else if (playerCaster.hasAura(46699)) // Requires No Ammo
                {
                    ammoDisplayID = 5996; // normal arrow
                    ammoInventoryType = inventoryType.ammo;
                }
            }
        } else {
            var unitCaster = caster.toUnit();

            if (unitCaster != null) {
                int nonRangedAmmoDisplayID = 0;
                InventoryType nonRangedAmmoInventoryType = inventoryType.forValue(0);

                for (byte i = WeaponAttackType.BaseAttack.getValue(); i < WeaponAttackType.max.getValue(); ++i) {
                    var itemId = unitCaster.getVirtualItemId(i);

                    if (itemId != 0) {
                        var itemEntry = CliDB.ItemStorage.get(itemId);

                        if (itemEntry != null) {
                            if (itemEntry.classID == itemClass.Weapon) {
                                switch (ItemSubClassWeapon.forValue(itemEntry.SubclassID)) {
                                    case Thrown:
                                        ammoDisplayID = global.getDB2Mgr().GetItemDisplayId(itemId, unitCaster.getVirtualItemAppearanceMod(i));
                                        ammoInventoryType = inventoryType.forValue(itemEntry.inventoryType);

                                        break;
                                    case Bow:
                                    case Crossbow:
                                        ammoDisplayID = 5996; // is this need fixing?
                                        ammoInventoryType = inventoryType.ammo;

                                        break;
                                    case Gun:
                                        ammoDisplayID = 5998; // is this need fixing?
                                        ammoInventoryType = inventoryType.ammo;

                                        break;
                                    default:
                                        nonRangedAmmoDisplayID = global.getDB2Mgr().GetItemDisplayId(itemId, unitCaster.getVirtualItemAppearanceMod(i));
                                        nonRangedAmmoInventoryType = itemEntry.inventoryType;

                                        break;
                                }

                                if (ammoDisplayID != 0) {
                                    break;
                                }
                            }
                        }
                    }
                }

                if (ammoDisplayID == 0 && ammoInventoryType == 0) {
                    ammoDisplayID = nonRangedAmmoDisplayID;
                    ammoInventoryType = nonRangedAmmoInventoryType;
                }
            }
        }

        ammo.displayID = (int) ammoDisplayID;
        ammo.inventoryType = (byte) ammoInventoryType.getValue();
    }

    private void sendSpellExecuteLog() {
        if (executeLogEffects.isEmpty()) {
            return;
        }

        SpellExecuteLog spellExecuteLog = new SpellExecuteLog();

        spellExecuteLog.caster = caster.getGUID();
        spellExecuteLog.spellID = spellInfo.getId();
        spellExecuteLog.effects = executeLogEffects.values().ToList();
        spellExecuteLog.logData.initialize(this);

        caster.sendCombatLogMessage(spellExecuteLog);
    }

    private void executeLogEffectTakeTargetPower(SpellEffectName effect, Unit target, Power powerType, int points, double amplitude) {
        SpellLogEffectPowerDrainParams spellLogEffectPowerDrainParams = new SpellLogEffectPowerDrainParams();

        spellLogEffectPowerDrainParams.victim = target.getGUID();
        spellLogEffectPowerDrainParams.points = points;
        spellLogEffectPowerDrainParams.powerType = (int) powerType.getValue();
        spellLogEffectPowerDrainParams.amplitude = (float) amplitude;

        getExecuteLogEffect(effect).powerDrainTargets.add(spellLogEffectPowerDrainParams);
    }

    private void executeLogEffectExtraAttacks(SpellEffectName effect, Unit victim, int numAttacks) {
        SpellLogEffectExtraAttacksParams spellLogEffectExtraAttacksParams = new SpellLogEffectExtraAttacksParams();
        spellLogEffectExtraAttacksParams.victim = victim.getGUID();
        spellLogEffectExtraAttacksParams.numAttacks = numAttacks;

        getExecuteLogEffect(effect).extraAttacksTargets.add(spellLogEffectExtraAttacksParams);
    }

    private void sendSpellInterruptLog(Unit victim, int spellId) {
        SpellInterruptLog data = new SpellInterruptLog();
        data.caster = caster.getGUID();
        data.victim = victim.getGUID();
        data.interruptedSpellID = spellInfo.getId();
        data.spellID = spellId;

        caster.sendMessageToSet(data, true);
    }

    private void executeLogEffectDurabilityDamage(SpellEffectName effect, Unit victim, int itemId, int amount) {
        SpellLogEffectDurabilityDamageParams spellLogEffectDurabilityDamageParams = new SpellLogEffectDurabilityDamageParams();
        spellLogEffectDurabilityDamageParams.victim = victim.getGUID();
        spellLogEffectDurabilityDamageParams.itemID = itemId;
        spellLogEffectDurabilityDamageParams.amount = amount;

        getExecuteLogEffect(effect).durabilityDamageTargets.add(spellLogEffectDurabilityDamageParams);
    }

    private void executeLogEffectOpenLock(SpellEffectName effect, WorldObject obj) {
        SpellLogEffectGenericVictimParams spellLogEffectGenericVictimParams = new SpellLogEffectGenericVictimParams();
        spellLogEffectGenericVictimParams.victim = obj.getGUID();

        getExecuteLogEffect(effect).genericVictimTargets.add(spellLogEffectGenericVictimParams);
    }

    private void executeLogEffectCreateItem(SpellEffectName effect, int entry) {
        SpellLogEffectTradeSkillItemParams spellLogEffectTradeSkillItemParams = new SpellLogEffectTradeSkillItemParams();
        spellLogEffectTradeSkillItemParams.itemID = (int) entry;

        getExecuteLogEffect(effect).tradeSkillTargets.add(spellLogEffectTradeSkillItemParams);
    }

    private void executeLogEffectDestroyItem(SpellEffectName effect, int entry) {
        SpellLogEffectFeedPetParams spellLogEffectFeedPetParams = new SpellLogEffectFeedPetParams();
        spellLogEffectFeedPetParams.itemID = (int) entry;

        getExecuteLogEffect(effect).feedPetTargets.add(spellLogEffectFeedPetParams);
    }

    private void executeLogEffectSummonObject(SpellEffectName effect, WorldObject obj) {
        SpellLogEffectGenericVictimParams spellLogEffectGenericVictimParams = new SpellLogEffectGenericVictimParams();
        spellLogEffectGenericVictimParams.victim = obj.getGUID();

        getExecuteLogEffect(effect).genericVictimTargets.add(spellLogEffectGenericVictimParams);
    }

    private void executeLogEffectUnsummonObject(SpellEffectName effect, WorldObject obj) {
        SpellLogEffectGenericVictimParams spellLogEffectGenericVictimParams = new SpellLogEffectGenericVictimParams();
        spellLogEffectGenericVictimParams.victim = obj.getGUID();

        getExecuteLogEffect(effect).genericVictimTargets.add(spellLogEffectGenericVictimParams);
    }

    private void executeLogEffectResurrect(SpellEffectName effect, Unit target) {
        SpellLogEffectGenericVictimParams spellLogEffectGenericVictimParams = new SpellLogEffectGenericVictimParams();
        spellLogEffectGenericVictimParams.victim = target.getGUID();

        getExecuteLogEffect(effect).genericVictimTargets.add(spellLogEffectGenericVictimParams);
    }

    private void sendInterrupted(byte result) {
        SpellFailure failurePacket = new SpellFailure();
        failurePacket.casterUnit = caster.getGUID();
        failurePacket.castID = castId;
        failurePacket.spellID = spellInfo.getId();
        failurePacket.visual = spellVisual;
        failurePacket.reason = result;
        caster.sendMessageToSet(failurePacket, true);

        SpellFailedOther failedPacket = new SpellFailedOther();
        failedPacket.casterUnit = caster.getGUID();
        failedPacket.castID = castId;
        failedPacket.spellID = spellInfo.getId();
        failedPacket.visual = spellVisual;
        failedPacket.reason = result;
        caster.sendMessageToSet(failedPacket, true);
    }

    private void sendChannelStart(int duration) {
        // GameObjects don't channel
        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return;
        }

        SpellChannelStart spellChannelStart = new SpellChannelStart();
        spellChannelStart.casterGUID = unitCaster.getGUID();
        spellChannelStart.spellID = (int) spellInfo.getId();
        spellChannelStart.visual = spellVisual;
        spellChannelStart.channelDuration = duration;

        if (isEmpowered()) // remove the first second of casting time to display correctly
        {
            spellChannelStart.ChannelDuration -= 1000;
        }

        var schoolImmunityMask = unitCaster.getSchoolImmunityMask();
        var mechanicImmunityMask = unitCaster.getMechanicImmunityMask();

        if (schoolImmunityMask != 0 || mechanicImmunityMask != 0) {
            SpellChannelStartInterruptImmunities interruptImmunities = new SpellChannelStartInterruptImmunities();
            interruptImmunities.schoolImmunities = (int) schoolImmunityMask;
            interruptImmunities.immunities = (int) mechanicImmunityMask;

            spellChannelStart.interruptImmunities = interruptImmunities;
        }

        unitCaster.sendMessageToSet(spellChannelStart, true);

        timer = (int) duration;

        if (!targets.getHasDst()) {
            var channelAuraMask = new HashSet<Integer>();
            var explicitTargetEffectMask = SpellConst.MaxEffects;

            // if there is an explicit target, only add channel objects from effects that also hit ut
            if (!targets.getUnitTargetGUID().isEmpty()) {
                var explicitTarget = tangible.ListHelper.find(uniqueTargetInfo, target -> Objects.equals(target.targetGuid, targets.getUnitTargetGUID()));

                if (explicitTarget != null) {
                    explicitTargetEffectMask = explicitTarget.effects;
                }
            }

            for (var spellEffectInfo : spellInfo.getEffects()) {
                if (spellEffectInfo.effect == SpellEffectName.ApplyAura && explicitTargetEffectMask.contains(spellEffectInfo.effectIndex)) {
                    channelAuraMask.add(spellEffectInfo.effectIndex);
                }
            }

            var chanMask = channelAuraMask.ToMask();

            for (var target : uniqueTargetInfo) {
                if ((target.effects.ToMask() & chanMask) == 0) {
                    continue;
                }

                var requiredAttribute = ObjectGuid.opNotEquals(target.targetGuid, unitCaster.getGUID()) ? SpellAttr1.IsChannelled : SpellAttr1.IsSelfChannelled;

                if (!spellInfo.hasAttribute(requiredAttribute)) {
                    continue;
                }

                unitCaster.addChannelObject(target.targetGuid);
            }

            for (var target : uniqueGoTargetInfo) {
                if ((target.effects.ToMask() & chanMask) != 0) {
                    unitCaster.addChannelObject(target.targetGUID);
                }
            }
        } else if (spellInfo.hasAttribute(SpellAttr1.IsSelfChannelled)) {
            unitCaster.addChannelObject(unitCaster.getGUID());
        }

        var creatureCaster = unitCaster.toCreature();

        if (creatureCaster != null) {
            if (unitCaster.getUnitData().channelObjects.size() == 1 && unitCaster.getUnitData().channelObjects.get(0).isUnit()) {
                if (!creatureCaster.hasSpellFocus(this)) {
                    creatureCaster.setSpellFocus(this, global.getObjAccessor().GetWorldObject(creatureCaster, unitCaster.getUnitData().channelObjects.get(0)));
                }
            }
        }

        unitCaster.setChannelSpellId(spellInfo.getId());
        unitCaster.setChannelVisual(spellVisual);
    }

    private void sendResurrectRequest(Player target) {
        // get resurrector name for creature resurrections, otherwise packet will be not accepted
        // for player resurrections the name is looked up by guid
        var sentName = "";

        if (!caster.isPlayer()) {
            sentName = caster.getName(target.getSession().getSessionDbLocaleIndex());
        }

        ResurrectRequest resurrectRequest = new ResurrectRequest();
        resurrectRequest.resurrectOffererGUID = caster.getGUID();
        resurrectRequest.resurrectOffererVirtualRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
        resurrectRequest.name = sentName;
        resurrectRequest.sickness = caster.isUnit() && !caster.isTypeId(TypeId.PLAYER); // "you'll be afflicted with resurrection sickness"
        resurrectRequest.useTimer = !spellInfo.hasAttribute(SpellAttr3.NoResTimer);

        var pet = target.getCurrentPet();

        if (pet) {
            var charmInfo = pet.getCharmInfo();

            if (charmInfo != null) {
                resurrectRequest.petNumber = charmInfo.getPetNumber();
            }
        }

        resurrectRequest.spellID = spellInfo.getId();

        target.sendPacket(resurrectRequest);
    }

    private void takeCastItem() {
        if (castItem == null || !caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // not remove cast item at triggered spell (equipping, weapon damage, etc)
        if ((boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreCastItem.getValue())) {
            return;
        }

        var proto = castItem.getTemplate();

        if (proto == null) {
            // This code is to avoid a crash
            // I'm not sure, if this is really an error, but I guess every item needs a prototype
            Log.outError(LogFilter.spells, "Cast item has no item prototype {0}", castItem.getGUID().toString());

            return;
        }

        var expendable = false;
        var withoutCharges = false;

        for (var itemEffect : castItem.getEffects()) {
            if (itemEffect.LegacySlotIndex >= castItem.getItemData().spellCharges.getSize()) {
                continue;
            }

            // item has limited charges
            if (itemEffect.charges != 0) {
                if (itemEffect.charges < 0) {
                    expendable = true;
                }

                var charges = castItem.getSpellCharges(itemEffect.LegacySlotIndex);

                // item has charges left
                if (charges != 0) {
                    if (charges > 0) {
                        --charges;
                    } else {
                        ++charges;
                    }

                    if (proto.getMaxStackSize() == 1) {
                        castItem.setSpellCharges(itemEffect.LegacySlotIndex, charges);
                    }

                    castItem.setState(ItemUpdateState.changed, caster.toPlayer());
                }

                // all charges used
                withoutCharges = (charges == 0);
            }
        }

        if (expendable && withoutCharges) {
            int count = 1;
            tangible.RefObject<Integer> tempRef_count = new tangible.RefObject<Integer>(count);
            caster.toPlayer().destroyItemCount(castItem, tempRef_count, true);
            count = tempRef_count.refArgValue;

            // prevent crash at access to deleted m_targets.GetItemTarget
            if (castItem == targets.getItemTarget()) {
                targets.setItemTarget(null);
            }

            castItem = null;
            castItemGuid.clear();
            castItemEntry = 0;
        }
    }

    private void takePower() {
        // GameObjects don't use power
        var unitCaster = caster.toUnit();

        if (!unitCaster) {
            return;
        }

        if (castItem != null || triggeredByAuraSpell != null) {
            return;
        }

        //Don't take power if the spell is cast while .cheat power is enabled.
        if (unitCaster.isTypeId(TypeId.PLAYER)) {
            if (unitCaster.toPlayer().getCommandStatus(PlayerCommandStates.power)) {
                return;
            }
        }

        for (var cost : powerCosts) {
            var hit = true;

            if (unitCaster.isTypeId(TypeId.PLAYER)) {
                if (spellInfo.hasAttribute(SpellAttr1.DiscountPowerOnMiss)) {
                    var targetGUID = targets.getUnitTargetGUID();

                    if (!targetGUID.isEmpty()) {
                        var ihit = uniqueTargetInfo.FirstOrDefault(targetInfo -> Objects.equals(targetInfo.targetGuid, targetGUID) && targetInfo.missCondition != SpellMissInfo.NONE);

                        if (ihit != null) {
                            hit = false;
                            //lower spell cost on fail (by talent aura)
                            var modOwner = unitCaster.getSpellModOwner();

                            if (modOwner != null) {
                                tangible.RefObject<Integer> tempRef_Amount = new tangible.RefObject<Integer>(cost.amount);
                                modOwner.applySpellMod(spellInfo, SpellModOp.PowerCostOnMiss, tempRef_Amount);
                                cost.amount = tempRef_Amount.refArgValue;
                            }
                        }
                    }
                }
            }

            if (cost.power == powerType.runes) {
                takeRunePower(hit);

                continue;
            }

            if (cost.amount == 0) {
                continue;
            }

            // health as power used
            if (cost.power == powerType.health) {
                unitCaster.modifyHealth(-cost.amount);

                continue;
            }

            if (cost.power.getValue() >= powerType.max.getValue()) {
                Log.outError(LogFilter.spells, "Spell.TakePower: Unknown power type '{0}'", cost.power);

                continue;
            }

            unitCaster.modifyPower(cost.power, -cost.amount);
            this.<ISpellOnTakePower>ForEachSpellScript(a -> a.takePower(cost));
        }
    }

    private SpellCastResult checkRuneCost() {
        var runeCost = powerCosts.Sum(cost -> cost.power == powerType.Runes ? cost.Amount : 0);

        if (runeCost == 0) {
            return SpellCastResult.SpellCastOk;
        }

        var player = caster.toPlayer();

        if (!player) {
            return SpellCastResult.SpellCastOk;
        }

        if (player.getClass() != playerClass.Deathknight) {
            return SpellCastResult.SpellCastOk;
        }

        var readyRunes = 0;

        for (byte i = 0; i < player.getMaxPower(powerType.runes); ++i) {
            if (player.getRuneCooldown(i) == 0) {
                ++readyRunes;
            }
        }

        if (readyRunes < runeCost) {
            return SpellCastResult.NoPower; // not sure if result code is correct
        }

        return SpellCastResult.SpellCastOk;
    }

    private void takeRunePower(boolean didHit) {
        if (!caster.isTypeId(TypeId.PLAYER) || caster.toPlayer().getClass() != playerClass.Deathknight) {
            return;
        }

        var player = caster.toPlayer();
        runesState = player.getRunesState(); // store previous state

        var runeCost = powerCosts.Sum(cost -> cost.power == powerType.Runes ? cost.Amount : 0);

        for (byte i = 0; i < player.getMaxPower(powerType.runes); ++i) {
            if (player.getRuneCooldown(i) == 0 && runeCost > 0) {
                player.setRuneCooldown(i, didHit ? player.getRuneBaseCooldown() : RuneCooldowns.Miss);
                --runeCost;
            }
        }
    }

    private void takeReagents() {
        if (!caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // do not take reagents for these item casts
        if (castItem != null && castItem.getTemplate().hasFlag(ItemFlags.NoReagentCost)) {
            return;
        }

        var p_caster = caster.toPlayer();

        if (p_caster.canNoReagentCast(spellInfo)) {
            return;
        }

        for (var x = 0; x < SpellConst.MaxReagents; ++x) {
            if (spellInfo.Reagent[x] <= 0) {
                continue;
            }

            var itemid = (int) spellInfo.Reagent[x];
            var itemcount = spellInfo.ReagentCount[x];

            // if CastItem is also spell reagent
            if (castItem != null && castItem.getEntry() == itemid) {
                for (var itemEffect : castItem.getEffects()) {
                    if (itemEffect.LegacySlotIndex >= castItem.getItemData().spellCharges.getSize()) {
                        continue;
                    }

                    // CastItem will be used up and does not count as reagent
                    var charges = castItem.getSpellCharges(itemEffect.LegacySlotIndex);

                    if (itemEffect.charges < 0 && Math.abs(charges) < 2) {
                        ++itemcount;

                        break;
                    }
                }

                castItem = null;
                castItemGuid.clear();
                castItemEntry = 0;
            }

            // if GetItemTarget is also spell reagent
            if (targets.getItemTargetEntry() == itemid) {
                targets.setItemTarget(null);
            }

            p_caster.destroyItemCount(itemid, itemcount, true);
        }

        for (var reagentsCurrency : spellInfo.reagentsCurrency) {
            p_caster.removeCurrency(reagentsCurrency.CurrencyTypesID, -reagentsCurrency.currencyCount, CurrencyDestroyReason.spell);
        }
    }

    private void handleThreatSpells() {
        // wild GameObject spells don't cause threat
        var unitCaster = (_originalCaster ? _originalCaster : caster.toUnit());

        if (unitCaster == null) {
            return;
        }

        if (uniqueTargetInfo.isEmpty()) {
            return;
        }

        if (!spellInfo.getHasInitialAggro()) {
            return;
        }

        double threat = 0.0f;
        var threatEntry = global.getSpellMgr().getSpellThreatEntry(spellInfo.getId());

        if (threatEntry != null) {
            if (threatEntry.apPctMod != 0.0f) {
                threat += threatEntry.ApPctMod * unitCaster.getTotalAttackPowerValue(WeaponAttackType.BaseAttack);
            }

            threat += threatEntry.flatMod;
        } else if (!spellInfo.hasAttribute(SpellCustomAttributes.NoInitialThreat)) {
            threat += spellInfo.getSpellLevel();
        }

        // past this point only multiplicative effects occur
        if (threat == 0.0f) {
            return;
        }

        // since 2.0.1 threat from positive effects also is distributed among all targets, so the overall caused threat is at most the defined bonus
        threat /= uniqueTargetInfo.size();

        for (var ihit : uniqueTargetInfo) {
            var threatToAdd = threat;

            if (ihit.missCondition != SpellMissInfo.NONE) {
                threatToAdd = 0.0f;
            }

            var target = global.getObjAccessor().GetUnit(unitCaster, ihit.targetGuid);

            if (target == null) {
                continue;
            }

            // positive spells distribute threat among all units that are in combat with target, like healing
            if (isPositive()) {
                target.getThreatManager().forwardThreatForAssistingMe(unitCaster, threatToAdd, spellInfo);
            }
            // for negative spells threat gets distributed among affected targets
            else {
                if (!target.getCanHaveThreatList()) {
                    continue;
                }

                target.getThreatManager().addThreat(unitCaster, threatToAdd, spellInfo, true);
            }
        }

        Log.outDebug(LogFilter.spells, "Spell {0}, added an additional {1} threat for {2} {3} target(s)", spellInfo.getId(), threat, isPositive() ? "assisting" : "harming", uniqueTargetInfo.size());
    }

    private SpellCastResult checkCasterAuras(tangible.RefObject<Integer> param1) {
        var unitCaster = (_originalCaster ? _originalCaster : caster.toUnit());

        if (unitCaster == null) {
            return SpellCastResult.SpellCastOk;
        }

        // these attributes only show the spell as usable on the client when it has related aura applied
        // still they need to be checked against certain mechanics

        // SPELL_ATTR5_USABLE_WHILE_STUNNED by default only MECHANIC_STUN (ie no sleep, knockout, freeze, etc.)
        var usableWhileStunned = spellInfo.hasAttribute(SpellAttr5.AllowWhileStunned);

        // SPELL_ATTR5_USABLE_WHILE_FEARED by default only fear (ie no horror)
        var usableWhileFeared = spellInfo.hasAttribute(SpellAttr5.AllowWhileFleeing);

        // SPELL_ATTR5_USABLE_WHILE_CONFUSED by default only disorient (ie no polymorph)
        var usableWhileConfused = spellInfo.hasAttribute(SpellAttr5.AllowWhileConfused);

        // Check whether the cast should be prevented by any state you might have.
        var result = SpellCastResult.SpellCastOk;
        // Get unit state
        var unitflag = UnitFlag.forValue((int) unitCaster.getUnitData().flags);

        // this check should only be done when player does cast directly
        // (ie not when it's called from a script) Breaks for example PlayerAI when charmed
		/*if (!unitCaster.GetCharmerGUID().isEmpty())
		{
			Unit charmer = unitCaster.getCharmer();
			if (charmer)
				if (charmer.GetUnitBeingMoved() != unitCaster && !checkSpellCancelsCharm(ref param1))
					result = SpellCastResult.Charmed;
		}*/

        // spell has attribute usable while having a cc state, check if caster has allowed mechanic auras, another mechanic types must prevent cast spell

//		SpellCastResult mechanicCheck(AuraType auraType, ref int _param1)
//			{
//				var foundNotMechanic = false;
//				var auras = unitCaster.getAuraEffectsByType(auraType);
//
//				foreach (var aurEff in auras)
//				{
//					var mechanicMask = aurEff.spellInfo.getAllEffectsMechanicMask();
//
//					if (mechanicMask != 0 && !Convert.ToBoolean(mechanicMask & spellInfo.AllowedMechanicMask))
//					{
//						foundNotMechanic = true;
//
//						// fill up aura mechanic info to send client proper error message
//						_param1 = (int)aurEff.getSpellEffectInfo().mechanic;
//
//						if (_param1 == 0)
//							_param1 = (int)aurEff.spellInfo.mechanic;
//
//						break;
//					}
//				}
//
//				if (foundNotMechanic)
//					switch (auraType)
//					{
//						case AuraType.ModStun:
//						case AuraType.ModStunDisableGravity:
//							return SpellCastResult.Stunned;
//						case AuraType.ModFear:
//							return SpellCastResult.Fleeing;
//						case AuraType.ModConfuse:
//							return SpellCastResult.Confused;
//						default:
//							//ABORT();
//							return SpellCastResult.NotKnown;
//					}
//
//				return SpellCastResult.SpellCastOk;
//			}

        if (unitflag.hasFlag(UnitFlag.Stunned)) {
            if (usableWhileStunned) {
                var mechanicResult = mechanicCheck(AuraType.ModStun, param1);

                if (mechanicResult != SpellCastResult.SpellCastOk) {
                    result = mechanicResult;
                }
            } else if (!checkSpellCancelsStun(param1)) {
                result = SpellCastResult.Stunned;
            } else if ((spellInfo.getMechanic().getValue() & mechanics.ImmuneShield.getValue()) != 0 && caster.isUnit() && caster.toUnit().hasAuraWithMechanic(1 << mechanics.Banish.getValue())) {
                result = SpellCastResult.Stunned;
            }
        } else if (unitCaster.isSilenced(spellSchoolMask) && spellInfo.getPreventionType().hasFlag(SpellPreventionType.Silence) && !checkSpellCancelsSilence(param1)) {
            result = SpellCastResult.Silenced;
        } else if (unitflag.hasFlag(UnitFlag.Pacified) && spellInfo.getPreventionType().hasFlag(SpellPreventionType.Pacify) && !checkSpellCancelsPacify(param1)) {
            result = SpellCastResult.Pacified;
        } else if (unitflag.hasFlag(UnitFlag.Fleeing)) {
            if (usableWhileFeared) {
                var mechanicResult = mechanicCheck(AuraType.ModFear, param1);

                if (mechanicResult != SpellCastResult.SpellCastOk) {
                    result = mechanicResult;
                } else {
                    mechanicResult = mechanicCheck(AuraType.ModStunDisableGravity, param1);

                    if (mechanicResult != SpellCastResult.SpellCastOk) {
                        result = mechanicResult;
                    }
                }
            } else if (!checkSpellCancelsFear(param1)) {
                result = SpellCastResult.Fleeing;
            }
        } else if (unitflag.hasFlag(UnitFlag.Confused)) {
            if (usableWhileConfused) {
                var mechanicResult = mechanicCheck(AuraType.ModConfuse, param1);

                if (mechanicResult != SpellCastResult.SpellCastOk) {
                    result = mechanicResult;
                }
            } else if (!checkSpellCancelsConfuse(param1)) {
                result = SpellCastResult.Confused;
            }
        } else if (unitCaster.hasUnitFlag2(UnitFlag2.NoActions) && spellInfo.getPreventionType().hasFlag(SpellPreventionType.NoActions) && !checkSpellCancelsNoActions(param1)) {
            result = SpellCastResult.NoActions;
        }

        // Attr must make flag drop spell totally immune from all effects
        if (result != SpellCastResult.SpellCastOk) {
            return (param1.refArgValue != 0) ? SpellCastResult.PreventedByMechanic : result;
        }

        return SpellCastResult.SpellCastOk;
    }

    private boolean checkSpellCancelsAuraEffect(AuraType auraType, tangible.RefObject<Integer> param1) {
        var unitCaster = (_originalCaster ? _originalCaster : caster.toUnit());

        if (unitCaster == null) {
            return false;
        }

        // Checking auras is needed now, because you are prevented by some state but the spell grants immunity.
        var auraEffects = unitCaster.getAuraEffectsByType(auraType);

        if (auraEffects.isEmpty()) {
            return true;
        }

        for (var aurEff : auraEffects) {
            if (spellInfo.spellCancelsAuraEffect(aurEff)) {
                continue;
            }

            param1.refArgValue = aurEff.getSpellEffectInfo().mechanic.getValue();

            if (param1.refArgValue == 0) {
                param1.refArgValue = aurEff.getSpellInfo().getMechanic().getValue();
            }

            return false;
        }

        return true;
    }

    private boolean checkSpellCancelsCharm(tangible.RefObject<Integer> param1) {
        return checkSpellCancelsAuraEffect(AuraType.ModCharm, param1) || checkSpellCancelsAuraEffect(AuraType.AoeCharm, param1) || checkSpellCancelsAuraEffect(AuraType.ModPossess, param1);
    }

    private boolean checkSpellCancelsStun(tangible.RefObject<Integer> param1) {
        return checkSpellCancelsAuraEffect(AuraType.ModStun, param1) && checkSpellCancelsAuraEffect(AuraType.ModStunDisableGravity, param1);
    }

    private boolean checkSpellCancelsSilence(tangible.RefObject<Integer> param1) {
        return checkSpellCancelsAuraEffect(AuraType.ModSilence, param1) || checkSpellCancelsAuraEffect(AuraType.ModPacifySilence, param1);
    }

    private boolean checkSpellCancelsPacify(tangible.RefObject<Integer> param1) {
        return checkSpellCancelsAuraEffect(AuraType.ModPacify, param1) || checkSpellCancelsAuraEffect(AuraType.ModPacifySilence, param1);
    }

    private boolean checkSpellCancelsFear(tangible.RefObject<Integer> param1) {
        return checkSpellCancelsAuraEffect(AuraType.ModFear, param1);
    }

    private boolean checkSpellCancelsConfuse(tangible.RefObject<Integer> param1) {
        return checkSpellCancelsAuraEffect(AuraType.ModConfuse, param1);
    }

    private boolean checkSpellCancelsNoActions(tangible.RefObject<Integer> param1) {
        return checkSpellCancelsAuraEffect(AuraType.ModNoActions, param1);
    }

    private SpellCastResult checkArenaAndRatedBattlegroundCastRules() {
        var isRatedBattleground = false; // NYI
        var isArena = !isRatedBattleground;

        // check USABLE attributes
        // USABLE takes precedence over NOT_USABLE
        if (isRatedBattleground && spellInfo.hasAttribute(SpellAttr9.UsableInRatedBattlegrounds)) {
            return SpellCastResult.SpellCastOk;
        }

        if (isArena && spellInfo.hasAttribute(SpellAttr4.IgnoreDefaultArenaRestrictions)) {
            return SpellCastResult.SpellCastOk;
        }

        // check NOT_USABLE attributes
        if (spellInfo.hasAttribute(SpellAttr4.NotInArenaOrRatedBattleground)) {
            return isArena ? SpellCastResult.NotInArena : SpellCastResult.NotInBattleground;
        }

        if (isArena && spellInfo.hasAttribute(SpellAttr9.NotUsableInArena)) {
            return SpellCastResult.NotInArena;
        }

        // check cooldowns
        var spellCooldown = spellInfo.getRecoveryTime1();

        if (isArena && spellCooldown > 10 * time.Minute * time.InMilliseconds) // not sure if still needed
        {
            return SpellCastResult.NotInArena;
        }

        if (isRatedBattleground && spellCooldown > 15 * time.Minute * time.InMilliseconds) {
            return SpellCastResult.NotInBattleground;
        }

        return SpellCastResult.SpellCastOk;
    }


//	(float minRange, float maxRange) GetMinMaxRange(bool strict)
//		{
//			var rangeMod = 0.0f;
//			var minRange = 0.0f;
//			var maxRange = 0.0f;
//
//			if (strict && spellInfo.IsNextMeleeSwingSpell)
//				return (0.0f, 100.0f);
//
//			var unitCaster = caster.AsUnit;
//
//			if (spellInfo.rangeEntry != null)
//			{
//				var target = targets.unitTarget;
//
//				if (spellInfo.rangeEntry.flags.hasFlag(SpellRangeFlag.Melee))
//				{
//					// when the target is not a unit, take the caster's combat reach as the target's combat reach.
//					if (unitCaster)
//						rangeMod = unitCaster.getMeleeRange(target ? target : unitCaster);
//				}
//				else
//				{
//					var meleeRange = 0.0f;
//
//					if (spellInfo.rangeEntry.flags.hasFlag(SpellRangeFlag.Ranged))
//						// when the target is not a unit, take the caster's combat reach as the target's combat reach.
//						if (unitCaster != null)
//							meleeRange = unitCaster.getMeleeRange(target ? target : unitCaster);
//
//					minRange = caster.getSpellMinRangeForTarget(target, spellInfo) + meleeRange;
//					maxRange = caster.getSpellMaxRangeForTarget(target, spellInfo);
//
//					if (target || targets.corpseTarget)
//					{
//						rangeMod = caster.combatReach + (target ? target.CombatReach : caster.combatReach);
//
//						if (minRange > 0.0f && !spellInfo.rangeEntry.flags.hasFlag(SpellRangeFlag.Ranged))
//							minRange += rangeMod;
//					}
//				}
//
//				if (target != null && unitCaster != null && unitCaster.IsMoving && target.IsMoving && !unitCaster.IsWalking && !target.IsWalking && (spellInfo.rangeEntry.flags.hasFlag(SpellRangeFlag.Melee) || target.IsPlayer))
//					rangeMod += 8.0f / 3.0f;
//			}
//
//			if (spellInfo.hasAttribute(SpellAttr0.UsesRangedSlot) && caster.isTypeId(TypeId.PLAYER))
//			{
//				var ranged = caster.AsPlayer.getWeaponForAttack(WeaponAttackType.RangedAttack, true);
//
//				if (ranged)
//					maxRange *= ranged.template.RangedModRange * 0.01f;
//			}
//
//			var modOwner = caster.SpellModOwner;
//
//			if (modOwner)
//				modOwner.applySpellMod(spellInfo, SpellModOp.Range, ref maxRange, this);
//
//			maxRange += rangeMod;
//
//			return (minRange, maxRange);
//		}

    private SpellCastResult checkRange(boolean strict) {
        // Don't check for instant cast spells
        if (!strict && casttime == 0) {
            return SpellCastResult.SpellCastOk;
        }


        var(minRange, maxRange) = GetMinMaxRange(strict);

        // dont check max_range to strictly after cast
        if (spellInfo.getRangeEntry() != null && spellInfo.getRangeEntry().flags != SpellRangeFlag.Melee && !strict) {
            maxRange += Math.min(3.0f, maxRange * 0.1f); // 10% but no more than 3.0f
        }

        // get square values for sqr distance checks
        minRange *= minRange;
        maxRange *= maxRange;

        var target = targets.getUnitTarget();

        if (target && target != caster) {
            if (caster.getLocation().getExactDistSq(target.getLocation()) > maxRange) {
                return SpellCastResult.OutOfRange;
            }

            if (minRange > 0.0f && caster.getLocation().getExactDistSq(target.getLocation()) < minRange) {
                return SpellCastResult.OutOfRange;
            }

            if (caster.isTypeId(TypeId.PLAYER) && ((spellInfo.getFacingCasterFlags().hasFlag(1) && !caster.getLocation().hasInArc((float) Math.PI, target.getLocation())) && !caster.toPlayer().isWithinBoundaryRadius(target))) {
                return SpellCastResult.UnitNotInfront;
            }
        }

        var goTarget = targets.getGOTarget();

        if (goTarget != null) {
            if (!goTarget.isAtInteractDistance(caster.toPlayer(), spellInfo)) {
                return SpellCastResult.OutOfRange;
            }
        }

        if (targets.getHasDst() && !targets.getHasTraj()) {
            if (caster.getLocation().getExactDistSq(targets.getDstPos()) > maxRange) {
                return SpellCastResult.OutOfRange;
            }

            if (minRange > 0.0f && caster.getLocation().getExactDistSq(targets.getDstPos()) < minRange) {
                return SpellCastResult.OutOfRange;
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    private SpellCastResult checkPower() {
        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return SpellCastResult.SpellCastOk;
        }

        // item cast not used power
        if (castItem != null) {
            return SpellCastResult.SpellCastOk;
        }

        for (var cost : powerCosts) {
            // health as power used - need check health amount
            if (cost.power == powerType.health) {
                if (unitCaster.getHealth() <= cost.amount) {
                    return SpellCastResult.CasterAurastate;
                }

                continue;
            }

            // Check valid power type
            if (cost.power.getValue() >= powerType.max.getValue()) {
                Log.outError(LogFilter.spells, "Spell.CheckPower: Unknown power type '{0}'", cost.power);

                return SpellCastResult.unknown;
            }

            //check rune cost only if a spell has powerType == POWER_RUNES
            if (cost.power == powerType.runes) {
                var failReason = checkRuneCost();

                if (failReason != SpellCastResult.SpellCastOk) {
                    return failReason;
                }
            }

            // Check power amount
            if (unitCaster.getPower(cost.power) < cost.amount) {
                return SpellCastResult.NoPower;
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    private SpellCastResult checkItems(tangible.RefObject<Integer> param1, tangible.RefObject<Integer> param2) {
        var player = caster.toPlayer();

        if (!player) {
            return SpellCastResult.SpellCastOk;
        }

        if (castItem == null) {
            if (!castItemGuid.isEmpty()) {
                return SpellCastResult.ItemNotReady;
            }
        } else {
            var itemid = castItem.getEntry();

            if (!player.hasItemCount(itemid)) {
                return SpellCastResult.ItemNotReady;
            }

            var proto = castItem.getTemplate();

            if (proto == null) {
                return SpellCastResult.ItemNotReady;
            }

            for (var itemEffect : castItem.getEffects()) {
                if (itemEffect.LegacySlotIndex < castItem.getItemData().spellCharges.getSize() && itemEffect.charges != 0) {
                    if (castItem.getSpellCharges(itemEffect.LegacySlotIndex) == 0) {
                        return SpellCastResult.NoChargesRemain;
                    }
                }
            }

            // consumable cast item checks
            if (proto.getClass() == itemClass.Consumable && targets.getUnitTarget() != null) {
                // such items should only fail if there is no suitable effect at all - see Rejuvenation Potions for example
                var failReason = SpellCastResult.SpellCastOk;

                for (var spellEffectInfo : spellInfo.getEffects()) {
                    // skip check, pet not required like checks, and for TARGET_UNIT_PET m_targets.GetUnitTarget() is not the real target but the caster
                    if (spellEffectInfo.targetA.getTarget() == targets.UnitPet) {
                        continue;
                    }

                    if (spellEffectInfo.effect == SpellEffectName.Heal) {
                        if (targets.getUnitTarget().isFullHealth()) {
                            failReason = SpellCastResult.AlreadyAtFullHealth;

                            continue;
                        } else {
                            failReason = SpellCastResult.SpellCastOk;

                            break;
                        }
                    }

                    // Mana Potion, Rage Potion, Thistle Tea(Rogue), ...
                    if (spellEffectInfo.effect == SpellEffectName.Energize) {
                        if (spellEffectInfo.miscValue < 0 || spellEffectInfo.miscValue >= powerType.max.getValue()) {
                            failReason = SpellCastResult.AlreadyAtFullPower;

                            continue;
                        }

                        var power = powerType.forValue((byte) spellEffectInfo.miscValue);

                        if (targets.getUnitTarget().getPower(power) == targets.getUnitTarget().getMaxPower(power)) {
                            failReason = SpellCastResult.AlreadyAtFullPower;

                            continue;
                        } else {
                            failReason = SpellCastResult.SpellCastOk;

                            break;
                        }
                    }
                }

                if (failReason != SpellCastResult.SpellCastOk) {
                    return failReason;
                }
            }
        }

        // check target item
        if (!targets.getItemTargetGuid().isEmpty()) {
            var item = targets.getItemTarget();

            if (item == null) {
                return SpellCastResult.ItemGone;
            }

            if (!item.isFitToSpellRequirements(spellInfo)) {
                return SpellCastResult.equippedItemClass;
            }
        }
        // if not item target then required item must be equipped
        else {
            if (!(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreEquippedItemRequirement.getValue())) {
                if (caster.isTypeId(TypeId.PLAYER) && !caster.toPlayer().hasItemFitToSpellRequirements(spellInfo)) {
                    return SpellCastResult.equippedItemClass;
                }
            }
        }

        // do not take reagents for these item casts
        if (!(castItem != null && castItem.getTemplate().hasFlag(ItemFlags.NoReagentCost))) {
            var checkReagents = !(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnorePowerAndReagentCost.getValue()) && !player.canNoReagentCast(spellInfo);

            // Not own traded item (in trader trade slot) requires reagents even if triggered spell
            if (!checkReagents) {
                var targetItem = targets.getItemTarget();

                if (targetItem != null) {
                    if (ObjectGuid.opNotEquals(targetItem.getOwnerGUID(), player.getGUID())) {
                        checkReagents = true;
                    }
                }
            }

            // check reagents (ignore triggered spells with reagents processed by original spell) and special reagent ignore case.
            if (checkReagents) {
                for (byte i = 0; i < SpellConst.MaxReagents; i++) {
                    if (spellInfo.Reagent[i] <= 0) {
                        continue;
                    }

                    var itemid = (int) spellInfo.Reagent[i];
                    var itemcount = spellInfo.ReagentCount[i];

                    // if CastItem is also spell reagent
                    if (castItem != null && castItem.getEntry() == itemid) {
                        var proto = castItem.getTemplate();

                        if (proto == null) {
                            return SpellCastResult.ItemNotReady;
                        }

                        for (var itemEffect : castItem.getEffects()) {
                            if (itemEffect.LegacySlotIndex >= castItem.getItemData().spellCharges.getSize()) {
                                continue;
                            }

                            // CastItem will be used up and does not count as reagent
                            var charges = castItem.getSpellCharges(itemEffect.LegacySlotIndex);

                            if (itemEffect.charges < 0 && Math.abs(charges) < 2) {
                                ++itemcount;

                                break;
                            }
                        }
                    }

                    if (!player.hasItemCount(itemid, itemcount)) {
                        param1.refArgValue = (int) itemid;

                        return SpellCastResult.reagents;
                    }
                }

                for (var reagentsCurrency : spellInfo.reagentsCurrency) {
                    if (!player.hasCurrency(reagentsCurrency.CurrencyTypesID, reagentsCurrency.currencyCount)) {
                        param1.refArgValue = -1;
                        param2.refArgValue = reagentsCurrency.CurrencyTypesID;

                        return SpellCastResult.reagents;
                    }
                }
            }

            // check totem-item requirements (items presence in inventory)
            int totems = 2;

            for (var i = 0; i < 2; ++i) {
                if (spellInfo.Totem[i] != 0) {
                    if (player.hasItemCount(spellInfo.Totem[i])) {
                        totems -= 1;

                        continue;
                    }
                } else {
                    totems -= 1;
                }
            }

            if (totems != 0) {
                return SpellCastResult.totems;
            }

            // Check items for totemCategory (items presence in inventory)
            int totemCategory = 2;

            for (byte i = 0; i < 2; ++i) {
                if (spellInfo.TotemCategory[i] != 0) {
                    if (player.hasItemTotemCategory(spellInfo.TotemCategory[i])) {
                        totemCategory -= 1;

                        continue;
                    }
                } else {
                    totemCategory -= 1;
                }
            }

            if (totemCategory != 0) {
                return SpellCastResult.totemCategory;
            }
        }

        // special checks for spell effects
        for (var spellEffectInfo : spellInfo.getEffects()) {
            switch (spellEffectInfo.effect) {
                case CreateItem:
                case CreateLoot: {
                    // m_targets.GetUnitTarget() means explicit cast, otherwise we dont check for possible equip error
                    var target = targets.getUnitTarget() != null ? targets.getUnitTarget() : player;

                    if (target.isPlayer() && !isTriggered()) {
                        // SPELL_EFFECT_CREATE_ITEM_2 differs from SPELL_EFFECT_CREATE_ITEM in that it picks the random item to create from a pool of potential items,
                        // so we need to make sure there is at least one free space in the player's inventory
                        if (spellEffectInfo.effect == SpellEffectName.CreateLoot) {
                            if (target.toPlayer().getFreeInventorySpace() == 0) {
                                player.sendEquipError(InventoryResult.InvFull, null, null, spellEffectInfo.itemType);

                                return SpellCastResult.DontReport;
                            }
                        }

                        if (spellEffectInfo.itemType != 0) {
                            var itemTemplate = global.getObjectMgr().getItemTemplate(spellEffectInfo.itemType);

                            if (itemTemplate == null) {
                                return SpellCastResult.ItemNotFound;
                            }

                            var createCount = (int) Math.Clamp(spellEffectInfo.calcValue(), 1, itemTemplate.getMaxStackSize());

                            ArrayList<ItemPosCount> dest = new ArrayList<>();
                            var msg = target.toPlayer().canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, spellEffectInfo.itemType, createCount);

                            if (msg != InventoryResult.Ok) {
                                /** @todo Needs review
                                 */
                                if (itemTemplate.getItemLimitCategory() == 0) {
                                    player.sendEquipError(msg, null, null, spellEffectInfo.itemType);

                                    return SpellCastResult.DontReport;
                                } else {
                                    // Conjure Food/Water/Refreshment spells
                                    if (spellInfo.getSpellFamilyName() != SpellFamilyName.Mage || (!spellInfo.getSpellFamilyFlags().get(0).hasFlag(0x40000000))) {
                                        return SpellCastResult.TooManyOfItem;
                                    } else if (!target.toPlayer().hasItemCount(spellEffectInfo.itemType)) {
                                        player.sendEquipError(msg, null, null, spellEffectInfo.itemType);

                                        return SpellCastResult.DontReport;
                                    } else if (spellInfo.getEffects().size() > 1) {
                                        player.castSpell(player, (int) spellInfo.getEffect(1).calcValue(), (new CastSpellExtraArgs()).setTriggeringSpell(this)); // move this to anywhere
                                    }

                                    return SpellCastResult.DontReport;
                                }
                            }
                        }
                    }

                    break;
                }
                case EnchantItem:
                    if (spellEffectInfo.itemType != 0 && targets.getItemTarget() != null && targets.getItemTarget().isVellum()) {
                        // cannot enchant vellum for other player
                        if (targets.getItemTarget().getOwnerUnit() != player) {
                            return SpellCastResult.NotTradeable;
                        }

                        // do not allow to enchant vellum from scroll made by vellum-prevent exploit
                        if (castItem != null && castItem.getTemplate().hasFlag(ItemFlags.NoReagentCost)) {
                            return SpellCastResult.totemCategory;
                        }

                        ArrayList<ItemPosCount> dest = new ArrayList<>();
                        var msg = player.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, spellEffectInfo.itemType, 1);

                        if (msg != InventoryResult.Ok) {
                            player.sendEquipError(msg, null, null, spellEffectInfo.itemType);

                            return SpellCastResult.DontReport;
                        }
                    }

                case EnchantItemPrismatic: {
                    var targetItem = targets.getItemTarget();

                    if (targetItem == null) {
                        return SpellCastResult.ItemNotFound;
                    }

                    // required level has to be checked also! Exploit fix
                    if (targetItem.getItemLevel(targetItem.getOwnerUnit()) < spellInfo.getBaseLevel() || (targetItem.getRequiredLevel() != 0 && targetItem.getRequiredLevel() < spellInfo.getBaseLevel())) {
                        return SpellCastResult.Lowlevel;
                    }

                    var isItemUsable = false;

                    for (var itemEffect : targetItem.getEffects()) {
                        if (itemEffect.spellID != 0 && itemEffect.triggerType == ItemSpelltriggerType.OnUse) {
                            isItemUsable = true;

                            break;
                        }
                    }

                    var enchantEntry = CliDB.SpellItemEnchantmentStorage.get(spellEffectInfo.miscValue);

                    // do not allow adding usable enchantments to items that have use effect already
                    if (enchantEntry != null) {
                        for (var s = 0; s < ItemConst.MaxItemEnchantmentEffects; ++s) {
                            switch (enchantEntry.Effect[s]) {
                                case ItemEnchantmentType.UseSpell:
                                    if (isItemUsable) {
                                        return SpellCastResult.OnUseEnchant;
                                    }

                                    break;
                                case ItemEnchantmentType.PrismaticSocket: {
                                    int numSockets = 0;

                                    for (int socket = 0; socket < ItemConst.MaxGemSockets; ++socket) {
                                        if (targetItem.getSocketColor(socket) != 0) {
                                            ++numSockets;
                                        }
                                    }

                                    if (numSockets == ItemConst.MaxGemSockets || targetItem.getEnchantmentId(EnchantmentSlot.Prismatic) != 0) {
                                        return SpellCastResult.MaxSockets;
                                    }

                                    break;
                                }
                            }
                        }
                    }

                    // Not allow enchant in trade slot for some enchant type
                    if (targetItem.getOwnerUnit() != player) {
                        if (enchantEntry == null) {
                            return SpellCastResult.error;
                        }

                        if (enchantEntry.getFlags().hasFlag(SpellItemEnchantmentFlags.Soulbound)) {
                            return SpellCastResult.NotTradeable;
                        }
                    }

                    break;
                }
                case EnchantItemTemporary: {
                    var item = targets.getItemTarget();

                    if (item == null) {
                        return SpellCastResult.ItemNotFound;
                    }

                    // Not allow enchant in trade slot for some enchant type
                    if (item.getOwnerUnit() != player) {
                        var enchant_id = spellEffectInfo.miscValue;
                        var enchantEntry = CliDB.SpellItemEnchantmentStorage.get(enchant_id);

                        if (enchantEntry == null) {
                            return SpellCastResult.error;
                        }

                        if (enchantEntry.getFlags().hasFlag(SpellItemEnchantmentFlags.Soulbound)) {
                            return SpellCastResult.NotTradeable;
                        }
                    }

                    // Apply item level restriction if the enchanting spell has max level restrition set
                    if (castItem != null && spellInfo.getMaxLevel() > 0) {
                        if (item.getTemplate().getBaseItemLevel() < castItem.getTemplate().getBaseRequiredLevel()) {
                            return SpellCastResult.Lowlevel;
                        }

                        if (item.getTemplate().getBaseItemLevel() > spellInfo.getMaxLevel()) {
                            return SpellCastResult.Highlevel;
                        }
                    }

                    break;
                }
                case EnchantHeldItem:
                    // check item existence in effect code (not output errors at offhand hold item effect to main hand for example
                    break;
                case Disenchant: {
                    var item = targets.getItemTarget();

                    if (!item) {
                        return SpellCastResult.CantBeSalvaged;
                    }

                    // prevent disenchanting in trade slot
                    if (ObjectGuid.opNotEquals(item.getOwnerGUID(), player.getGUID())) {
                        return SpellCastResult.CantBeSalvaged;
                    }

                    var itemProto = item.getTemplate();

                    if (itemProto == null) {
                        return SpellCastResult.CantBeSalvaged;
                    }

                    var itemDisenchantLoot = item.getDisenchantLoot(caster.toPlayer());

                    if (itemDisenchantLoot == null) {
                        return SpellCastResult.CantBeSalvaged;
                    }

                    if (itemDisenchantLoot.SkillRequired > player.getSkillValue(SkillType.Enchanting)) {
                        return SpellCastResult.CantBeSalvagedSkill;
                    }

                    break;
                }
                case Prospecting: {
                    var item = targets.getItemTarget();

                    if (!item) {
                        return SpellCastResult.CantBeProspected;
                    }

                    //ensure item is a prospectable ore
                    if (!item.getTemplate().hasFlag(ItemFlags.IsProspectable)) {
                        return SpellCastResult.CantBeProspected;
                    }

                    //prevent prospecting in trade slot
                    if (ObjectGuid.opNotEquals(item.getOwnerGUID(), player.getGUID())) {
                        return SpellCastResult.CantBeProspected;
                    }

                    //Check for enough skill in jewelcrafting
                    var item_prospectingskilllevel = item.getTemplate().getRequiredSkillRank();

                    if (item_prospectingskilllevel > player.getSkillValue(SkillType.Jewelcrafting)) {
                        return SpellCastResult.LowCastlevel;
                    }

                    //make sure the player has the required ores in inventory
                    if (item.getCount() < 5) {
                        param1.refArgValue = (int) item.getEntry();
                        param2.refArgValue = 5;

                        return SpellCastResult.NeedMoreItems;
                    }

                    if (!LootStorage.PROSPECTING.haveLootFor(targets.getItemTargetEntry())) {
                        return SpellCastResult.CantBeProspected;
                    }

                    break;
                }
                case Milling: {
                    var item = targets.getItemTarget();

                    if (!item) {
                        return SpellCastResult.CantBeMilled;
                    }

                    //ensure item is a millable herb
                    if (!item.getTemplate().hasFlag(ItemFlags.IsMillable)) {
                        return SpellCastResult.CantBeMilled;
                    }

                    //prevent milling in trade slot
                    if (ObjectGuid.opNotEquals(item.getOwnerGUID(), player.getGUID())) {
                        return SpellCastResult.CantBeMilled;
                    }

                    //Check for enough skill in inscription
                    var item_millingskilllevel = item.getTemplate().getRequiredSkillRank();

                    if (item_millingskilllevel > player.getSkillValue(SkillType.Inscription)) {
                        return SpellCastResult.LowCastlevel;
                    }

                    //make sure the player has the required herbs in inventory
                    if (item.getCount() < 5) {
                        param1.refArgValue = (int) item.getEntry();
                        param2.refArgValue = 5;

                        return SpellCastResult.NeedMoreItems;
                    }

                    if (!LootStorage.MILLING.haveLootFor(targets.getItemTargetEntry())) {
                        return SpellCastResult.CantBeMilled;
                    }

                    break;
                }
                case WeaponDamage:
                case WeaponDamageNoSchool: {
                    if (attackType != WeaponAttackType.RangedAttack) {
                        break;
                    }

                    var item = player.getWeaponForAttack(attackType);

                    if (item == null || item.isBroken()) {
                        return SpellCastResult.equippedItem;
                    }

                    switch (ItemSubClassWeapon.forValue(item.getTemplate().getSubClass())) {
                        case Thrown: {
                            var ammo = item.getEntry();

                            if (!player.hasItemCount(ammo)) {
                                return SpellCastResult.NoAmmo;
                            }

                            break;
                        }
                        case Gun:
                        case Bow:
                        case Crossbow:
                        case Wand:
                            break;
                        default:
                            break;
                    }

                    break;
                }
                case RechargeItem: {
                    var itemId = spellEffectInfo.itemType;

                    var proto = global.getObjectMgr().getItemTemplate(itemId);

                    if (proto == null) {
                        return SpellCastResult.ItemAtMaxCharges;
                    }

                    var item = player.getItemByEntry(itemId);

                    if (item != null) {
                        for (var itemEffect : item.getEffects()) {
                            if (itemEffect.LegacySlotIndex <= item.getItemData().spellCharges.getSize() && itemEffect.charges != 0 && item.getSpellCharges(itemEffect.LegacySlotIndex) == itemEffect.charges) {
                                return SpellCastResult.ItemAtMaxCharges;
                            }
                        }
                    }

                    break;
                }
                case RespecAzeriteEmpoweredItem: {
                    var item = targets.getItemTarget();

                    if (item == null) {
                        return SpellCastResult.AzeriteEmpoweredOnly;
                    }

                    if (ObjectGuid.opNotEquals(item.getOwnerGUID(), caster.getGUID())) {
                        return SpellCastResult.DontReport;
                    }

                    var azeriteEmpoweredItem = item.getAsAzeriteEmpoweredItem();

                    if (azeriteEmpoweredItem == null) {
                        return SpellCastResult.AzeriteEmpoweredOnly;
                    }

                    var hasSelections = false;

                    for (var tier = 0; tier < SharedConst.MaxAzeriteEmpoweredTier; ++tier) {
                        if (azeriteEmpoweredItem.GetSelectedAzeritePower(tier) != 0) {
                            hasSelections = true;

                            break;
                        }
                    }

                    if (!hasSelections) {
                        return SpellCastResult.AzeriteEmpoweredNoChoicesToUndo;
                    }

                    if (!caster.toPlayer().hasEnoughMoney(azeriteEmpoweredItem.GetRespecCost())) {
                        return SpellCastResult.DontReport;
                    }

                    break;
                }
                default:
                    break;
            }
        }

        // check weapon presence in slots for main/offhand weapons
        if (!(boolean) (triggeredCastFlags.getValue() & TriggerCastFlag.IgnoreEquippedItemRequirement.getValue()) && spellInfo.getEquippedItemClass().getValue() >= 0) {
            var weaponCheck = (WeaponAttackType arg) ->
            {
                var item = player.toPlayer().getWeaponForAttack(attackType);

                // skip spell if no weapon in slot or broken
                if (!item || item.isBroken()) {
                    return SpellCastResult.equippedItemClass;
                }

                // skip spell if weapon not fit to triggered spell
                if (!item.isFitToSpellRequirements(spellInfo)) {
                    return SpellCastResult.equippedItemClass;
                }

                return SpellCastResult.SpellCastOk;
            };

            // main hand weapon required
            if (spellInfo.hasAttribute(SpellAttr3.RequiresMainHandWeapon)) {
                var mainHandResult = weaponCheck.invoke(WeaponAttackType.BaseAttack);

                if (mainHandResult != SpellCastResult.SpellCastOk) {
                    return mainHandResult;
                }
            }

            // offhand hand weapon required
            if (spellInfo.hasAttribute(SpellAttr3.RequiresOffHandWeapon)) {
                var offHandResult = weaponCheck.invoke(WeaponAttackType.OffAttack);

                if (offHandResult != SpellCastResult.SpellCastOk) {
                    return offHandResult;
                }
            }
        }

        return SpellCastResult.SpellCastOk;
    }

    private boolean updatePointers() {
        if (Objects.equals(originalCasterGuid, caster.getGUID())) {
            originalCaster = caster.toUnit();
        } else {
            originalCaster = global.getObjAccessor().GetUnit(caster, originalCasterGuid);

            if (originalCaster != null && !originalCaster.isInWorld()) {
                originalCaster = null;
            } else {
                originalCaster = caster.toUnit();
            }
        }

        if (!castItemGuid.isEmpty() && caster.isTypeId(TypeId.PLAYER)) {
            castItem = caster.toPlayer().getItemByGuid(castItemGuid);
            castItemLevel = -1;

            // cast item not found, somehow the item is no longer where we expected
            if (!castItem) {
                return false;
            }

            // check if the item is really the same, in case it has been wrapped for example
            if (castItemEntry != castItem.getEntry()) {
                return false;
            }

            castItemLevel = (int) castItem.getItemLevel(caster.toPlayer());
        }

        targets.update(caster);

        // further actions done only for dest targets
        if (!targets.getHasDst()) {
            return true;
        }

        // cache last transport
        WorldObject transport = null;

        // update effect destinations (in case of moved transport dest target)
        for (var spellEffectInfo : spellInfo.getEffects()) {
            var dest = destTargets.get(spellEffectInfo.effectIndex);

            if (dest.transportGuid.isEmpty()) {
                continue;
            }

            if (transport == null || ObjectGuid.opNotEquals(transport.getGUID(), dest.transportGuid)) {
                transport = global.getObjAccessor().GetWorldObject(caster, dest.transportGuid);
            }

            if (transport != null) {
                dest.position.relocate(transport.getLocation());
                dest.position.relocateOffset(dest.transportOffset);
            }
        }

        return true;
    }

    private boolean checkEffectTarget(Unit target, SpellEffectInfo spellEffectInfo, Position losPosition) {
        if (spellEffectInfo == null || !spellEffectInfo.isEffect()) {
            return false;
        }

        switch (spellEffectInfo.applyAuraName) {
            case ModPossess:
            case ModCharm:
            case ModPossessPet:
            case AoeCharm:
                if (target.getVehicleKit() && target.getVehicleKit().IsControllableVehicle()) {
                    return false;
                }

                if (target.isMounted()) {
                    return false;
                }

                if (!target.getCharmerGUID().isEmpty()) {
                    return false;
                }

                var damage = calculateDamage(spellEffectInfo, target);

                if (damage != 0) {
                    if (target.getLevelForTarget(caster) > damage) {
                        return false;
                    }
                }

                break;
            default:
                break;
        }

        // check for ignore LOS on the effect itself
        if (spellInfo.hasAttribute(SpellAttr2.IgnoreLineOfSight) || global.getDisableMgr().isDisabledFor(DisableType.spell, spellInfo.getId(), null, (byte) DisableFlags.SPELLLOS.getValue())) {
            return true;
        }

        // check if gameobject ignores LOS
        var gobCaster = caster.toGameObject();

        if (gobCaster != null) {
            if (gobCaster.getTemplate().getRequireLOS() == 0) {
                return true;
            }
        }

        // if spell is triggered, need to check for LOS disable on the aura triggering it and inherit that behaviour
        if (!spellInfo.hasAttribute(SpellAttr5.AlwaysLineOfSight) && isTriggered() && triggeredByAuraSpell != null && (triggeredByAuraSpell.hasAttribute(SpellAttr2.IgnoreLineOfSight) || global.getDisableMgr().isDisabledFor(DisableType.spell, triggeredByAuraSpell.getId(), null, (byte) DisableFlags.SPELLLOS.getValue()))) {
            return true;
        }

        // @todo shit below shouldn't be here, but it's temporary
        //Check targets for LOS visibility
        switch (spellEffectInfo.effect) {
            case SkinPlayerCorpse: {
                if (targets.getCorpseTargetGUID().isEmpty()) {
                    if (target.isWithinLOSInMap(caster, LineOfSightChecks.All, ModelIgnoreFlags.M2) && target.hasUnitFlag(UnitFlag.Skinnable)) {
                        return true;
                    }

                    return false;
                }

                var corpse = ObjectAccessor.getCorpse(caster, targets.getCorpseTargetGUID());

                if (!corpse) {
                    return false;
                }

                if (ObjectGuid.opNotEquals(target.getGUID(), corpse.getOwnerGUID())) {
                    return false;
                }

                if (!corpse.hasCorpseDynamicFlag(CorpseDynFlags.Lootable)) {
                    return false;
                }

                if (!corpse.isWithinLOSInMap(caster, LineOfSightChecks.All, ModelIgnoreFlags.M2)) {
                    return false;
                }

                break;
            }
            default: {
                if (losPosition == null || spellInfo.hasAttribute(SpellAttr5.AlwaysAoeLineOfSight)) {
                    // Get GO cast coordinates if original caster . GO
                    WorldObject caster = null;

                    if (originalCasterGuid.isGameObject()) {
                        caster = caster.getMap().getGameObject(originalCasterGuid);
                    }

                    if (!caster) {
                        caster = caster;
                    }

                    if (target != caster && !target.isWithinLOSInMap(caster, LineOfSightChecks.All, ModelIgnoreFlags.M2)) {
                        return false;
                    }
                }

                if (losPosition != null) {
                    if (!target.isWithinLOS(losPosition.getX(), losPosition.getY(), losPosition.getZ(), LineOfSightChecks.All, ModelIgnoreFlags.M2)) {
                        return false;
                    }
                }

                break;
            }
        }

        return true;
    }

    private boolean checkEffectTarget(GameObject target, SpellEffectInfo spellEffectInfo) {
        if (spellEffectInfo == null || !spellEffectInfo.isEffect()) {
            return false;
        }

        switch (spellEffectInfo.effect) {
            case GameObjectDamage:
            case GameobjectRepair:
            case GameobjectSetDestructionState:
                if (target.getGoType() != GameObjectTypes.destructibleBuilding) {
                    return false;
                }

                break;
            default:
                break;
        }

        return true;
    }

    private boolean checkEffectTarget(Item target, SpellEffectInfo spellEffectInfo) {
        if (spellEffectInfo == null || !spellEffectInfo.isEffect()) {
            return false;
        }

        return true;
    }

    private boolean isAutoActionResetSpell() {
        if (isTriggered()) {
            return false;
        }

        if (casttime == 0 && spellInfo.hasAttribute(SpellAttr6.DoesntResetSwingTimerIfInstant)) {
            return false;
        }

        return true;
    }

    private boolean isNeedSendToClient() {
        return spellVisual.spellXSpellVisualID != 0 || spellVisual.scriptVisualID != 0 || spellInfo.isChanneled() || spellInfo.hasAttribute(SpellAttr8.AuraSendAmount) || spellInfo.getHasHitDelay() || (triggeredByAuraSpell == null && !isTriggered());
    }

    private boolean isValidDeadOrAliveTarget(Unit target) {
        if (target.isAlive()) {
            return !spellInfo.isRequiringDeadTarget();
        }

        if (spellInfo.isAllowingDeadTarget()) {
            return true;
        }

        return false;
    }

    private void handleLaunchPhase() {
        // handle effects with SPELL_EFFECT_HANDLE_LAUNCH mode
        for (var spellEffectInfo : spellInfo.getEffects()) {
            // don't do anything for empty effect
            if (!spellEffectInfo.isEffect()) {
                continue;
            }

            handleEffects(null, null, null, null, spellEffectInfo, SpellEffectHandleMode.Launch);
        }

        prepareTargetProcessing();

        for (var target : uniqueTargetInfo) {
            preprocessSpellLaunch(target);
        }

        for (var spellEffectInfo : spellInfo.getEffects()) {
            double multiplier = 1.0f;

            if (applyMultiplierMask.contains(spellEffectInfo.effectIndex)) {
                multiplier = spellEffectInfo.calcDamageMultiplier(originalCaster, this);
            }

            for (var target : uniqueTargetInfo) {
                var mask = target.effects;

                if (!mask.contains(spellEffectInfo.effectIndex)) {
                    continue;
                }

                doEffectOnLaunchTarget(target, multiplier, spellEffectInfo);
            }
        }

        finishTargetProcessing();
    }

    private void preprocessSpellLaunch(TargetInfo targetInfo) {
        var targetUnit = Objects.equals(caster.getGUID(), targetInfo.targetGuid) ? caster.toUnit() : global.getObjAccessor().GetUnit(caster, targetInfo.targetGuid);

        if (targetUnit == null) {
            return;
        }

        // This will only cause combat - the target will engage once the projectile hits (in Spell::TargetInfo::PreprocessTarget)
        if (originalCaster && targetInfo.missCondition != SpellMissInfo.Evade && !originalCaster.isFriendlyTo(targetUnit) && (!spellInfo.isPositive() || spellInfo.hasEffect(SpellEffectName.dispel)) && (spellInfo.getHasInitialAggro() || targetUnit.isEngaged())) {
            originalCaster.setInCombatWith(targetUnit, true);
        }

        Unit unit = null;

        // In case spell hit target, do all effect on that target
        if (targetInfo.missCondition == SpellMissInfo.NONE) {
            unit = targetUnit;
        }
        // In case spell reflect from target, do all effect on caster (if hit)
        else if (targetInfo.missCondition == SpellMissInfo.Reflect && targetInfo.reflectResult == SpellMissInfo.NONE) {
            unit = caster.toUnit();
        }

        if (unit == null) {
            return;
        }

        double critChance = spellValue.criticalChance;

        if (originalCaster) {
            if (critChance == 0) {
                critChance = originalCaster.spellCritChanceDone(this, null, spellSchoolMask, attackType);
            }

            critChance = unit.spellCritChanceTaken(originalCaster, this, null, spellSchoolMask, critChance, attackType);
        }

        targetInfo.isCrit = RandomUtil.randChance(critChance);
    }

    private void doEffectOnLaunchTarget(TargetInfo targetInfo, double multiplier, SpellEffectInfo spellEffectInfo) {
        Unit unit = null;

        // In case spell hit target, do all effect on that target
        if (targetInfo.missCondition == SpellMissInfo.NONE || (targetInfo.missCondition == SpellMissInfo.Block && !spellInfo.hasAttribute(SpellAttr3.CompletelyBlocked))) {
            unit = Objects.equals(caster.getGUID(), targetInfo.targetGuid) ? caster.toUnit() : global.getObjAccessor().GetUnit(caster, targetInfo.targetGuid);
        }
        // In case spell reflect from target, do all effect on caster (if hit)
        else if (targetInfo.missCondition == SpellMissInfo.Reflect && targetInfo.reflectResult == SpellMissInfo.NONE) {
            unit = caster.toUnit();
        }

        if (!unit) {
            return;
        }

        damageInEffects = 0;
        healingInEffects = 0;

        handleEffects(unit, null, null, null, spellEffectInfo, SpellEffectHandleMode.LaunchTarget);

        if (originalCaster != null && damageInEffects > 0) {
            if (spellEffectInfo.isTargetingArea() || spellEffectInfo.isAreaAuraEffect() || spellEffectInfo.isEffect(SpellEffectName.PersistentAreaAura) || spellInfo.hasAttribute(SpellAttr5.TreatAsAreaEffect)) {
                damageInEffects = unit.calculateAOEAvoidance(damageInEffects, (int) spellInfo.getSchoolMask().getValue(), originalCaster.getGUID());

                if (originalCaster.isPlayer()) {
                    // cap damage of player AOE
                    var targetAmount = getUnitTargetCountForEffect(spellEffectInfo.effectIndex);

                    if (targetAmount > 20) {
                        damageInEffects = (int) (DamageInEffects * 20 / targetAmount);
                    }
                }
            }
        }

        if (applyMultiplierMask.contains(spellEffectInfo.effectIndex)) {
            damageInEffects = (int) (DamageInEffects * damageMultipliers.get(spellEffectInfo.effectIndex));
            healingInEffects = (int) (HealingInEffects * damageMultipliers.get(spellEffectInfo.effectIndex));

            damageMultipliers.put(spellEffectInfo.effectIndex, damageMultipliers.get(spellEffectInfo.effectIndex) * multiplier);
        }

        targetInfo.damage += damageInEffects;
        targetInfo.healing += healingInEffects;
    }

    private SpellCastResult canOpenLock(SpellEffectInfo effect, int lockId, tangible.RefObject<SkillType> skillId, tangible.RefObject<Integer> reqSkillValue, tangible.RefObject<Integer> skillValue) {
        if (lockId == 0) // possible case for GO and maybe for items.
        {
            return SpellCastResult.SpellCastOk;
        }

        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return SpellCastResult.BadTargets;
        }

        // Get LockInfo
        var lockInfo = CliDB.LockStorage.get(lockId);

        if (lockInfo == null) {
            return SpellCastResult.BadTargets;
        }

        var reqKey = false; // some locks not have reqs

        for (var j = 0; j < SharedConst.MaxLockCase; ++j) {
            switch (LockKeyType.forValue(lockInfo.LockType[j])) {
                // check first item (many fit cases can be)
                case Item:
                    if (lockInfo.Index[j] != 0 && castItem && castItem.getEntry() == lockInfo.Index[j]) {
                        return SpellCastResult.SpellCastOk;
                    }

                    reqKey = true;

                    break;
                // check first skill (only single first fit case can be)
                case Skill: {
                    reqKey = true;

                    // wrong locktype, skip
                    if (effect.miscValue != lockInfo.Index[j]) {
                        continue;
                    }

                    skillId.refArgValue = SharedConst.SkillByLockType(LockType.forValue(lockInfo.Index[j]));

                    if (skillId.refArgValue != SkillType.NONE || lockInfo.Index[j] == (int) LockType.Lockpicking.getValue()) {
                        reqSkillValue.refArgValue = lockInfo.Skill[j];

                        // castitem check: rogue using skeleton keys. the skill values should not be added in this case.
                        skillValue.refArgValue = 0;

                        if (!castItem && unitCaster.isTypeId(TypeId.PLAYER)) {
                            skillValue.refArgValue = unitCaster.toPlayer().getSkillValue(skillId.refArgValue);
                        } else if (lockInfo.Index[j] == (int) LockType.Lockpicking.getValue()) {
                            skillValue.refArgValue = (int) unitCaster.getLevel() * 5;
                        }

                        // skill bonus provided by casting spell (mostly item spells)
                        // add the effect base points modifier from the spell cast (cheat lock / skeleton first etc.)
                        if (effect.targetA.getTarget() == targets.GameobjectItemTarget || effect.targetB.getTarget() == targets.GameobjectItemTarget) {
                            skillValue.refArgValue += (int) effect.calcValue();
                        }

                        if (skillValue.refArgValue < reqSkillValue.refArgValue) {
                            return SpellCastResult.LowCastlevel;
                        }
                    }

                    return SpellCastResult.SpellCastOk;
                }
                case Spell:
                    if (spellInfo.getId() == lockInfo.Index[j]) {
                        return SpellCastResult.SpellCastOk;
                    }

                    reqKey = true;

                    break;
            }
        }

        if (reqKey) {
            return SpellCastResult.BadTargets;
        }

        return SpellCastResult.SpellCastOk;
    }

    private void prepareTargetProcessing() {
    }

    private void finishTargetProcessing() {
        sendSpellExecuteLog();
    }

    private void loadScripts() {
        loadedScripts = global.getScriptMgr().createSpellScripts(spellInfo.getId(), this);

        for (var script : loadedScripts) {
            Log.outDebug(LogFilter.spells, "Spell.LoadScripts: Script `{0}` for spell `{1}` is loaded now", script._GetScriptName(), spellInfo.getId());
            script.register();

            if (script instanceof ISpellScript) {
                for (var iFace : script.getClass().getInterfaces()) {
                    if (iFace.name.equals("ISpellScript") || iFace.name.equals("IBaseSpellScript")) {
                        continue;
                    }

                    TValue spellScripts;
                    if (!(spellScriptsByType.containsKey(iFace) && (spellScripts = spellScriptsByType.get(iFace)) == spellScripts)) {
                        spellScripts = new ArrayList<>();
                        spellScriptsByType.put(iFace, spellScripts);
                    }

                    spellScripts.add((ISpellScript) script);
                    registerSpellEffectHandler(script);
                }
            }
        }
    }

    private void registerSpellEffectHandler(SpellScript script) {
        if (script instanceof IHasSpellEffects hse) {
            for (var effect : hse.getSpellEffects()) {
                if (effect instanceof ISpellEffectHandler se) {
                    int mask = 0;

                    if (se.getEffectIndex() == SpellConst.EffectAll || se.getEffectIndex() == SpellConst.EffectFirstFound) {
                        for (var effInfo : spellInfo.getEffects()) {
                            if (se.getEffectIndex() == SpellConst.EffectFirstFound && mask != 0) {
                                break;
                            }

                            if (checkSpellEffectHandler(se, effInfo)) {
                                addSpellEffect(effInfo.effectIndex, script, se);
                            }
                        }
                    } else {
                        if (checkSpellEffectHandler(se, se.getEffectIndex())) {
                            addSpellEffect(se.getEffectIndex(), script, se);
                        }
                    }
                } else if (effect instanceof ITargetHookHandler th) {
                    int mask = 0;

                    if (th.getEffectIndex() == SpellConst.EffectAll || th.getEffectIndex() == SpellConst.EffectFirstFound) {
                        for (var effInfo : spellInfo.getEffects()) {
                            if (th.getEffectIndex() == SpellConst.EffectFirstFound && mask != 0) {
                                break;
                            }

                            if (checkTargetHookEffect(th, effInfo)) {
                                addSpellEffect(effInfo.effectIndex, script, th);
                            }
                        }
                    } else {
                        if (checkTargetHookEffect(th, th.getEffectIndex())) {
                            addSpellEffect(th.getEffectIndex(), script, th);
                        }
                    }
                }
            }
        }
    }

    private boolean checkSpellEffectHandler(ISpellEffectHandler se, int effIndex) {
        if (spellInfo.getEffects().size() <= effIndex) {
            return false;
        }

        var spellEffectInfo = spellInfo.getEffect(effIndex);

        return checkSpellEffectHandler(se, spellEffectInfo);
    }

    private boolean checkSpellEffectHandler(ISpellEffectHandler se, SpellEffectInfo spellEffectInfo) {
        if (spellEffectInfo.effect == 0 && se.getEffectName() == 0) {
            return true;
        }

        if (spellEffectInfo.effect == 0) {
            return false;
        }

        return se.getEffectName() == SpellEffectName.Any || spellEffectInfo.effect == se.getEffectName();
    }

    private void callScriptOnPrecastHandler() {
        for (var script : this.<ISpellOnPrecast>GetSpellScripts()) {
            try {
                script._PrepareScriptCall(SpellScriptHookType.OnPrecast);
                ((ISpellOnPrecast) script).OnPrecast();
                script._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    private void callScriptBeforeCastHandlers() {
        for (var script : this.<ISpellBeforeCast>GetSpellScripts()) {

            try {
                script._PrepareScriptCall(SpellScriptHookType.BeforeCast);

                ((ISpellBeforeCast) script).BeforeCast();

                script._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    private void callScriptOnCastHandlers() {
        for (var script : this.<ISpellOnCast>GetSpellScripts()) {
            try {
                script._PrepareScriptCall(SpellScriptHookType.OnCast);

                ((ISpellOnCast) script).OnCast();

                script._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    private void callScriptAfterCastHandlers() {
        for (var script : this.<ISpellAfterCast>GetSpellScripts()) {
            try {
                script._PrepareScriptCall(SpellScriptHookType.AfterCast);

                ((ISpellAfterCast) script).AfterCast();

                script._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    private SpellCastResult callScriptCheckCastHandlers() {
        var retVal = SpellCastResult.SpellCastOk;

        for (var script : this.<ISpellCheckCast>GetSpellScripts()) {
            try {
                script._PrepareScriptCall(SpellScriptHookType.CheckCast);

                var tempResult = ((ISpellCheckCast) script).checkCast();

                if (tempResult != SpellCastResult.SpellCastOk) {
                    retVal = tempResult;
                }

                script._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return retVal;
    }

    private int callScriptCalcCastTimeHandlers(int castTime) {
        for (var script : this.<ISpellCalculateCastTime>GetSpellScripts()) {
            try {

                script._PrepareScriptCall(SpellScriptHookType.CalcCastTime);
                castTime = ((ISpellCalculateCastTime) script).calcCastTime(castTime);
                script._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return castTime;
    }

    private boolean callScriptEffectHandlers(int effIndex, SpellEffectHandleMode mode) {
        // execute script effect handler hooks and check if effects was prevented
        var preventDefault = false;

        switch (mode) {
            case Launch:

                for (var script : getEffectScripts(SpellScriptHookType.Launch, effIndex)) {
                    preventDefault = processScript(effIndex, preventDefault, script.Item1, script.item2, SpellScriptHookType.Launch);
                }

                break;
            case LaunchTarget:

                for (var script : getEffectScripts(SpellScriptHookType.LaunchTarget, effIndex)) {
                    preventDefault = processScript(effIndex, preventDefault, script.Item1, script.item2, SpellScriptHookType.LaunchTarget);
                }

                break;
            case Hit:

                for (var script : getEffectScripts(SpellScriptHookType.hit, effIndex)) {
                    preventDefault = processScript(effIndex, preventDefault, script.Item1, script.item2, SpellScriptHookType.hit);
                }

                break;
            case HitTarget:

                for (var script : getEffectScripts(SpellScriptHookType.EffectHitTarget, effIndex)) {
                    preventDefault = processScript(effIndex, preventDefault, script.Item1, script.item2, SpellScriptHookType.EffectHitTarget);
                }

                break;
            default:
                return false;
        }

        return preventDefault;
    }

    private void callScriptSuccessfulDispel(int effIndex) {
        for (var script : getEffectScripts(SpellScriptHookType.EffectSuccessfulDispel, effIndex)) {
            try {
                script.Item1._PrepareScriptCall(SpellScriptHookType.EffectSuccessfulDispel);

                if (script.Item2 instanceof ISpellEffectHandler seh) {
                    seh.callEffect(effIndex);
                }

                script.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    private void callScriptObjectAreaTargetSelectHandlers(ArrayList<WorldObject> targets, int effIndex, SpellImplicitTargetInfo targetType) {
        for (var script : getEffectScripts(SpellScriptHookType.ObjectAreaTargetSelect, effIndex)) {
            try {
                script.Item1._PrepareScriptCall(SpellScriptHookType.ObjectAreaTargetSelect);

                if (script.Item2 instanceof ISpellObjectAreaTargetSelect oas) {
                    if (targetType.getTarget() == oas.getTargetType()) {
                        oas.filterTargets(targets);
                    }
                }

                script.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    private void callScriptObjectTargetSelectHandlers(tangible.RefObject<WorldObject> target, int effIndex, SpellImplicitTargetInfo targetType) {
        for (var script : getEffectScripts(SpellScriptHookType.ObjectTargetSelect, effIndex)) {
            try {
                script.Item1._PrepareScriptCall(SpellScriptHookType.ObjectTargetSelect);

                if (script.Item2 instanceof ISpellObjectTargetSelectHandler ots) {
                    if (targetType.getTarget() == ots.getTargetType()) {
                        ots.targetSelect(target.refArgValue);
                    }
                }

                script.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    private void callScriptDestinationTargetSelectHandlers(tangible.RefObject<SpellDestination> target, int effIndex, SpellImplicitTargetInfo targetType) {
        for (var script : getEffectScripts(SpellScriptHookType.DestinationTargetSelect, effIndex)) {
            try {
                script.Item1._PrepareScriptCall(SpellScriptHookType.DestinationTargetSelect);

                if (script.Item2 instanceof ISpellDestinationTargetSelectHandler dts) {
                    if (targetType.getTarget() == dts.getTargetType()) {
                        dts.setDest(target.refArgValue);
                    }
                }

                script.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    private boolean checkScriptEffectImplicitTargets(int effIndex, int effIndexToCheck) {
        // Skip if there are not any script
        if (loadedScripts.isEmpty()) {
            return true;
        }

        var otsTargetEffIndex = getEffectScripts(SpellScriptHookType.ObjectTargetSelect, effIndex).count > 0;
        var otsEffIndexCheck = getEffectScripts(SpellScriptHookType.ObjectTargetSelect, effIndexToCheck).count > 0;

        var oatsTargetEffIndex = getEffectScripts(SpellScriptHookType.ObjectAreaTargetSelect, effIndex).count > 0;
        var oatsEffIndexCheck = getEffectScripts(SpellScriptHookType.ObjectAreaTargetSelect, effIndexToCheck).count > 0;

        if ((otsTargetEffIndex && !otsEffIndexCheck) || (!otsTargetEffIndex && otsEffIndexCheck)) {
            return false;
        }

        if ((oatsTargetEffIndex && !oatsEffIndexCheck) || (!oatsTargetEffIndex && oatsEffIndexCheck)) {
            return false;
        }

        return true;
    }

    private void prepareTriggersExecutedOnHit() {
        var unitCaster = caster.toUnit();

        if (unitCaster == null) {
            return;
        }

        // handle SPELL_AURA_ADD_TARGET_TRIGGER auras:
        // save auras which were present on spell caster on cast, to prevent triggered auras from affecting caster
        // and to correctly calculate proc chance when combopoints are present
        var targetTriggers = unitCaster.getAuraEffectsByType(AuraType.AddTargetTrigger);

        for (var aurEff : targetTriggers) {
            if (!aurEff.isAffectingSpell(spellInfo)) {
                continue;
            }

            var spellInfo = global.getSpellMgr().getSpellInfo(aurEff.getSpellEffectInfo().triggerSpell, getCastDifficulty());

            if (spellInfo != null) {
                // calculate the chance using spell base amount, because aura amount is not updated on combo-points change
                // this possibly needs fixing
                var auraBaseAmount = aurEff.baseAmount;
                // proc chance is stored in effect amount
                var chance = unitCaster.calculateSpellDamage(null, aurEff.getSpellEffectInfo(), auraBaseAmount);
                chance *= aurEff.getBase().getStackAmount();

                // build trigger and add to the list
                hitTriggerSpells.add(new HitTriggerSpell(spellInfo, aurEff.getSpellInfo(), chance));
            }
        }
    }

    private boolean canHaveGlobalCooldown(WorldObject caster) {
        // Only players or controlled units have global cooldown
        if (!caster.isPlayer() && (!caster.isCreature() || caster.toCreature().getCharmInfo() == null)) {
            return false;
        }

        return true;
    }

    private boolean hasGlobalCooldown() {
        if (!canHaveGlobalCooldown(caster)) {
            return false;
        }

        return caster.toUnit().getSpellHistory().hasGlobalCooldown(spellInfo);
    }

    private void triggerGlobalCooldown() {
        if (!canHaveGlobalCooldown(caster)) {
            return;
        }

        var gcd = duration.ofSeconds(spellInfo.getStartRecoveryTime());

        if (system.duration.opEquals(gcd, duration.Zero) || spellInfo.getStartRecoveryCategory() == 0) {
            return;
        }

        if (caster.isTypeId(TypeId.PLAYER)) {
            if (caster.toPlayer().getCommandStatus(PlayerCommandStates.cooldown)) {
                return;
            }
        }

        var MinGCD = duration.ofSeconds(750);
        var MaxGCD = duration.ofSeconds(1500);

        // Global cooldown can't leave range 1..1.5 secs
        // There are some spells (mostly not casted directly by player) that have < 1 sec and > 1.5 sec global cooldowns
        // but as tests show are not affected by any spell mods.
        if (gcd >= MinGCD && gcd <= MaxGCD) {
            // gcd modifier auras are applied only to own spells and only players have such mods
            var modOwner = caster.getSpellModOwner();

            if (modOwner) {
                var intGcd = (int) gcd.TotalMilliseconds;
                tangible.RefObject<Integer> tempRef_intGcd = new tangible.RefObject<Integer>(intGcd);
                modOwner.applySpellMod(spellInfo, SpellModOp.StartCooldown, tempRef_intGcd, this);
                intGcd = tempRef_intGcd.refArgValue;
                gcd = duration.ofSeconds(intGcd);
            }

            var isMeleeOrRangedSpell = spellInfo.getDmgClass() == SpellDmgClass.Melee || spellInfo.getDmgClass() == SpellDmgClass.Ranged || spellInfo.hasAttribute(SpellAttr0.UsesRangedSlot) || spellInfo.hasAttribute(SpellAttr0.IsAbility);

            // Apply haste rating
            if (gcd > MinGCD && (spellInfo.getStartRecoveryCategory() == 133 && !isMeleeOrRangedSpell)) {
                gcd = duration.ofSeconds(gcd.TotalMilliseconds * caster.toUnit().getUnitData().modSpellHaste);
                var intGcd = (int) gcd.TotalMilliseconds;
                tangible.RefObject<Integer> tempRef_intGcd2 = new tangible.RefObject<Integer>(intGcd);
                MathUtil.RoundToInterval(tempRef_intGcd2, 750, 1500);
                intGcd = tempRef_intGcd2.refArgValue;
                gcd = duration.ofSeconds(intGcd);
            }

            if (gcd > MinGCD && caster.toUnit().hasAuraTypeWithAffectMask(AuraType.ModGlobalCooldownByHasteRegen, spellInfo)) {
                gcd = duration.ofSeconds(gcd.TotalMilliseconds * caster.toUnit().getUnitData().modHasteRegen);
                var intGcd = (int) gcd.TotalMilliseconds;
                tangible.RefObject<Integer> tempRef_intGcd3 = new tangible.RefObject<Integer>(intGcd);
                MathUtil.RoundToInterval(tempRef_intGcd3, 750, 1500);
                intGcd = tempRef_intGcd3.refArgValue;
                gcd = duration.ofSeconds(intGcd);
            }
        }

        caster.toUnit().getSpellHistory().addGlobalCooldown(spellInfo, gcd);
    }

    private void cancelGlobalCooldown() {
        if (!canHaveGlobalCooldown(caster)) {
            return;
        }

        if (spellInfo.getStartRecoveryTime() == 0) {
            return;
        }

        // Cancel global cooldown when interrupting current cast
        if (caster.toUnit().getCurrentSpell(CurrentSpellType.generic) != this) {
            return;
        }

        caster.toUnit().getSpellHistory().cancelGlobalCooldown(spellInfo);
    }

    private String getDebugInfo() {
        return String.format("Id: %1$s Name: '%2$s' OriginalCaster: %3$s State: %4$s", spellInfo.getId(), spellInfo.getSpellName().get(global.getWorldMgr().getDefaultDbcLocale()), originalCasterGuid, getState());
    }


    private void addSpellEffect(int index, ISpellScript script, ISpellEffect effect) {
        var effecTypes;

        if (!effectHandlers.TryGetValue(index, out effecTypes)) {
            effecTypes = new Dictionary<SpellScriptHookType, ArrayList<(ISpellScript, ISpellEffect) >> ();
            effectHandlers.add(index, effecTypes);
        }

        var effects;

        if (!effecTypes.TryGetValue(effect.getHookType(), out effects)) {
            effects = new ArrayList<(ISpellScript, ISpellEffect) > ();
            effecTypes.add(effect.getHookType(), effects);
        }

        effects.add((script, effect));
    }

    private double calculateDamage(SpellEffectInfo spellEffectInfo, Unit target) {
        tangible.OutObject<Double> tempOut__ = new tangible.OutObject<Double>();
        var tempVar = calculateDamage(spellEffectInfo, target, tempOut__);
        _ = tempOut__.outArgValue;
        return tempVar;
    }

    private double calculateDamage(SpellEffectInfo spellEffectInfo, Unit target, tangible.OutObject<Double> variance) {
        var needRecalculateBasePoints = (spellValue.customBasePointsMask & (1 << spellEffectInfo.effectIndex)) == 0;

        return caster.calculateSpellDamage(variance, target, spellEffectInfo, needRecalculateBasePoints ? null : spellValue.effectBasePoints.get(spellEffectInfo.effectIndex), castItemEntry, castItemLevel);
    }

    private void checkSrc() {
        if (!targets.getHasSrc()) {
            targets.setSrc(caster);
        }
    }

    private void checkDst() {
        if (!targets.getHasDst()) {
            targets.setDst(caster);
        }
    }

    private void reSetTimer() {
        timer = casttime > 0 ? _casttime : 0;
    }

    private void setExecutedCurrently(boolean yes) {
        executedCurrently = yes;
    }

    private boolean isDelayableNoMore() {
        if (delayAtDamageCount >= 2) {
            return true;
        }

        ++delayAtDamageCount;

        return false;
    }


    public final void doCreateItem(int itemId, ItemContext context) {
        doCreateItem(itemId, context, null);
    }

    public final void doCreateItem(int itemId) {
        doCreateItem(itemId, 0, null);
    }

    public final void doCreateItem(int itemId, ItemContext context, ArrayList<Integer> bonusListIds) {
        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();

        var newitemid = itemId;
        var pProto = global.getObjectMgr().getItemTemplate(newitemid);

        if (pProto == null) {
            player.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        var num_to_add = (int) damage;

        if (num_to_add < 1) {
            num_to_add = 1;
        }

        if (num_to_add > pProto.getMaxStackSize()) {
            num_to_add = pProto.getMaxStackSize();
        }

        // this is bad, should be done using spell_loot_template (and conditions)

        // the chance of getting a perfect result
        double perfectCreateChance = 0.0f;
        // the resulting perfect item if successful
        var perfectItemType = itemId;

        // get perfection capability and chance
        tangible.RefObject<Double> tempRef_perfectCreateChance = new tangible.RefObject<Double>(perfectCreateChance);
        tangible.RefObject<Integer> tempRef_perfectItemType = new tangible.RefObject<Integer>(perfectItemType);
        if (SkillPerfectItems.canCreatePerfectItem(player, spellInfo.getId(), tempRef_perfectCreateChance, tempRef_perfectItemType)) {
            perfectItemType = tempRef_perfectItemType.refArgValue;
            perfectCreateChance = tempRef_perfectCreateChance.refArgValue;
            if (RandomUtil.randChance(perfectCreateChance)) // if the roll succeeds...
            {
                newitemid = perfectItemType; // the perfect item replaces the regular one
            }
        } else {
            perfectItemType = tempRef_perfectItemType.refArgValue;
            perfectCreateChance = tempRef_perfectCreateChance.refArgValue;
        }

        // init items_count to 1, since 1 item will be created regardless of specialization
        var items_count = 1;
        // the chance to create additional items
        double additionalCreateChance = 0.0f;
        // the maximum number of created additional items
        byte additionalMaxNum = 0;

        // get the chance and maximum number for creating extra items
        tangible.RefObject<Double> tempRef_additionalCreateChance = new tangible.RefObject<Double>(additionalCreateChance);
        tangible.RefObject<Byte> tempRef_additionalMaxNum = new tangible.RefObject<Byte>(additionalMaxNum);
        if (SkillExtraItems.canCreateExtraItems(player, spellInfo.getId(), tempRef_additionalCreateChance, tempRef_additionalMaxNum)) {
            additionalMaxNum = tempRef_additionalMaxNum.refArgValue;
            additionalCreateChance = tempRef_additionalCreateChance.refArgValue;
            // roll with this chance till we roll not to create or we create the max num
            while (RandomUtil.randChance(additionalCreateChance) && items_count <= additionalMaxNum) {
                ++items_count;
            }
        } else {
            additionalMaxNum = tempRef_additionalMaxNum.refArgValue;
            additionalCreateChance = tempRef_additionalCreateChance.refArgValue;
        }

        // really will be created more items
        num_to_add *= (int) items_count;

        // can the player store the new item?
        ArrayList<ItemPosCount> dest = new ArrayList<>();
        int no_space;
        tangible.OutObject<Integer> tempOut_no_space = new tangible.OutObject<Integer>();
        var msg = player.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, newitemid, num_to_add, tempOut_no_space);
        no_space = tempOut_no_space.outArgValue;

        if (msg != InventoryResult.Ok) {
            // convert to possible store amount
            if (msg == InventoryResult.InvFull || msg == InventoryResult.ItemMaxCount) {
                num_to_add -= no_space;
            } else {
                // if not created by another reason from full inventory or unique items amount limitation
                player.sendEquipError(msg, null, null, newitemid);

                return;
            }
        }

        if (num_to_add != 0) {
            // create the new item and store it
            var pItem = player.storeNewItem(dest, newitemid, true, ItemEnchantmentManager.generateItemRandomBonusListId(newitemid), null, context, bonusListIds);

            // was it successful? return error if not
            if (pItem == null) {
                player.sendEquipError(InventoryResult.ItemNotFound);

                return;
            }

            // set the "Crafted by ..." property of the item
            if (pItem.getTemplate().getHasSignature()) {
                pItem.setCreator(player.getGUID());
            }

            // send info to the client
            player.sendNewItem(pItem, num_to_add, true, true);

            if (pItem.getQuality().getValue() > itemQuality.Epic.getValue() || (pItem.getQuality() == itemQuality.Epic && pItem.getItemLevel(player) >= GuildConst.MinNewsItemLevel)) {
                var guild = player.getGuild();

                if (guild != null) {
                    guild.addGuildNews(GuildNews.ItemCrafted, player.getGUID(), 0, pProto.getId());
                }
            }

            // we succeeded in creating at least one item, so a levelup is possible
            player.updateCraftSkill(spellInfo);
        }
    }

    
    public final void effectJoinOrLeavePlayerParty() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !caster || !unitTarget.isPlayer()) {
            return;
        }

        var player = unitTarget.toPlayer();

        var group = player.getGroup();
        var creature = caster.toCreature();

        if (creature == null) {
            return;
        }

        if (group == null) {
            group = new PlayerGroup();
            group.create(player);
            // group->ConvertToLFG(dungeon);
            group.setDungeonDifficultyID(spellInfo.getDifficulty());
            global.getGroupMgr().addGroup(group);
        } else if (group.isMember(creature.getGUID())) {
            return;
        }

		/* if (m_spellInfo->GetEffect(effIndex, m_diffMode)->miscValue == 1)
			group->AddCreatureMember(creature);
		else
			group->RemoveCreatureMember(creature->GetGUID());*/
    }

    
    public final void effectChangeItemBonuses() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var item = targets.getItemTarget();

        if (item == null || !item.isSoulBound()) {
            return;
        }

        var OldItemBonusTree = effectInfo.miscValue;
        var NewItemBonusTree = effectInfo.miscValue;

        if (OldItemBonusTree == NewItemBonusTree) // Not release
        {
            return;
        }

        var OldBonusTree = DB2Manager.getInstance().GetItemBonusSet((int) OldItemBonusTree);
        var NewBonusTre = DB2Manager.getInstance().GetItemBonusSet((int) NewItemBonusTree);

        if (OldBonusTree == null || NewBonusTre == null) {
            return;
        }

        var bonuses = NewBonusTre.Select(s -> s.ChildItemBonusListID).<Integer>cast().ToList();

        var _found = false;
        int _treeMod = 0;

        for (var bonus : bonuses) {
            for (var oldBonus : OldBonusTree) {
                if (bonus == oldBonus.ChildItemBonusListID) {
                    _found = true;
                    _treeMod = oldBonus.itemContext;

                    break;
                }
            }
        }

        if (!_found) {
            return;
        }

        var bonusesNew = new ArrayList<>();

        for (var bonus : bonuses) {
            var bonusDel = false;

            for (var oldBonus : OldBonusTree) {
                if (bonus == oldBonus.ChildItemBonusListID && _treeMod == oldBonus.itemContext) {
                    bonusDel = true;

                    break;
                }
            }

            if (!bonusDel) {
                bonusesNew.add(bonus);
            }
        }

        item.setBonusData(new bonusData(item.getTemplate()));

        for (var newBonus : NewBonusTre) {
            if (_treeMod == newBonus.itemContext) {
                bonusesNew.add(newBonus.ChildItemBonusListID);
            }
        }

        for (var bonusId : bonusesNew) {
            item.addBonuses(bonusId);
        }

        item.setState(ItemUpdateState.changed, player);
    }

    
    public final void effectSetCovenant() {
        Player player;
        tangible.OutObject<Player> tempOut_player = new tangible.OutObject<Player>();
        if (!unitTarget.isPlayer(tempOut_player)) {
            player = tempOut_player.outArgValue;
            return;
        } else {
            player = tempOut_player.outArgValue;
        }

        byte covenantId = 0; // TODO

        player.setCovenant(covenantId);
    }

    
    private void effectUnused() {
    }

    private void effectResurrectNew() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (corpseTarget == null && unitTarget == null) {
            return;
        }

        Player player = null;

        if (corpseTarget) {
            player = global.getObjAccessor().findPlayer(corpseTarget.getOwnerGUID());
        } else if (unitTarget) {
            player = unitTarget.toPlayer();
        }

        if (player == null || player.isAlive() || !player.isInWorld()) {
            return;
        }

        if (player.isResurrectRequested()) // already have one active request
        {
            return;
        }

        executeLogEffectResurrect(effectInfo.effect, player);
        player.setResurrectRequestData(caster, (int) damage, (int) effectInfo.miscValue, 0);
        sendResurrectRequest(player);
    }

    
    private void effectInstaKill() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive()) {
            return;
        }

        if (unitTarget.isTypeId(TypeId.PLAYER)) {
            if (unitTarget.toPlayer().getCommandStatus(PlayerCommandStates.God)) {
                return;
            }
        }

        if (caster == unitTarget) // prevent interrupt message
        {
            finish();
        }

        SpellInstakillLog data = new SpellInstakillLog();
        data.target = unitTarget.getGUID();
        data.caster = caster.getGUID();
        data.spellID = spellInfo.getId();
        caster.sendMessageToSet(data, true);

        unit.kill(getUnitCasterForEffectHandlers(), unitTarget, false);
    }

    
    private void effectEnvironmentalDMG() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive()) {
            return;
        }

        // CalcAbsorbResist already in Player::EnvironmentalDamage
        if (unitTarget.isTypeId(TypeId.PLAYER)) {
            unitTarget.toPlayer().environmentalDamage(EnviromentalDamage.Fire, damage);
        } else {
            var unitCaster = getUnitCasterForEffectHandlers();
            DamageInfo damageInfo = new DamageInfo(unitCaster, unitTarget, damage, spellInfo, spellInfo.getSchoolMask(), DamageEffectType.SpellDirect, WeaponAttackType.BaseAttack);
            unit.calcAbsorbResist(damageInfo);

            SpellNonMeleeDamage log = new SpellNonMeleeDamage(unitCaster, unitTarget, spellInfo, spellVisual, spellInfo.getSchoolMask(), castId);
            log.damage = damageInfo.getDamage();
            log.originalDamage = damage;
            log.absorb = damageInfo.getAbsorb();
            log.resist = damageInfo.getResist();

            if (unitCaster != null) {
                unitCaster.sendSpellNonMeleeDamageLog(log);
            }
        }
    }

    
    private void effectSchoolDmg() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget) {
            return;
        }

        if (unitTarget != null && unitTarget.isAlive()) {
            // Meteor like spells (divided damage to targets)
            if (spellInfo.hasAttribute(SpellCustomAttributes.ShareDamage)) {
                var count = getUnitTargetCountForEffect(effectInfo.effectIndex);

                // divide to all targets
                if (count != 0) {
                    Damage /= count;
                }
            }

            var unitCaster = getUnitCasterForEffectHandlers();

            if (unitCaster != null) {
                var bonus = unitCaster.spellDamageBonusDone(unitTarget, spellInfo, damage, DamageEffectType.SpellDirect, effectInfo, 1, this);
                damage = bonus + (bonus * variance);
                damage = unitTarget.spellDamageBonusTaken(unitCaster, spellInfo, damage, DamageEffectType.SpellDirect);
            }

            damageInEffects += damage;
        }
    }

    
    private void effectDummy() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null && gameObjTarget == null && itemTarget == null && corpseTarget == null) {
            return;
        }

        // pet auras
        if (caster.getObjectTypeId() == TypeId.PLAYER) {
            var petSpell = global.getSpellMgr().getPetAura(spellInfo.getId(), (byte) effectInfo.effectIndex);

            if (petSpell != null) {
                caster.toPlayer().addPetAura(petSpell);

                return;
            }
        }

        // normal DB scripted effect
        Log.outDebug(LogFilter.spells, "Spell ScriptStart spellid {0} in effectDummy({1})", spellInfo.getId(), effectInfo.effectIndex);
        caster.getMap().scriptsStart(ScriptsType.spell, (int) ((int) spellInfo.getId() | (int) (effectInfo.effectIndex << 24)), caster, unitTarget);
    }

    
    private void effectTriggerSpell() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget && effectHandleMode != SpellEffectHandleMode.Launch) {
            return;
        }

        var triggered_spell_id = effectInfo.triggerSpell;

        // @todo move those to spell scripts
        if (effectInfo.effect == SpellEffectName.triggerSpell && effectHandleMode == SpellEffectHandleMode.LaunchTarget) {
            // special cases
            switch (triggered_spell_id) {
                // Demonic Empowerment -- succubus
                case 54437: {
                    unitTarget.removeMovementImpairingAuras(true);
                    unitTarget.removeAurasByType(AuraType.ModStalked);
                    unitTarget.removeAurasByType(AuraType.ModStun);

                    // Cast Lesser Invisibility
                    unitTarget.castSpell(unitTarget, 7870, new CastSpellExtraArgs(this));

                    return;
                }
                // Brittle Armor - (need add max stack of 24575 Brittle armor)
                case 29284: {
                    // Brittle Armor
                    var spell = global.getSpellMgr().getSpellInfo(24575, getCastDifficulty());

                    if (spell == null) {
                        return;
                    }

                    for (int j = 0; j < spell.stackAmount; ++j) {
                        caster.castSpell(unitTarget, spell.id, new CastSpellExtraArgs(this));
                    }

                    return;
                }
                // Mercurial Shield - (need add max stack of 26464 Mercurial Shield)
                case 29286: {
                    // Mercurial Shield
                    var spell = global.getSpellMgr().getSpellInfo(26464, getCastDifficulty());

                    if (spell == null) {
                        return;
                    }

                    for (int j = 0; j < spell.stackAmount; ++j) {
                        caster.castSpell(unitTarget, spell.id, new CastSpellExtraArgs(this));
                    }

                    return;
                }
            }
        }

        if (triggered_spell_id == 0) {
            Log.outWarn(LogFilter.spells, String.format("Spell::EffectTriggerSpell: Spell %1$s [EffectIndex: %2$s] does not have triggered spell.", spellInfo.getId(), effectInfo.effectIndex));

            return;
        }

        // normal case
        var spellInfo = global.getSpellMgr().getSpellInfo(triggered_spell_id, getCastDifficulty());

        if (spellInfo == null) {
            Log.outDebug(LogFilter.spells, "Spell.EffectTriggerSpell spell {0} tried to trigger unknown spell {1}", spellInfo.getId(), triggered_spell_id);

            return;
        }

        SpellCastTargets targets = new SpellCastTargets();

        if (effectHandleMode == SpellEffectHandleMode.LaunchTarget) {
            if (!spellInfo.needsToBeTriggeredByCaster(spellInfo)) {
                return;
            }

            targets.setUnitTarget(unitTarget);
        } else //if (effectHandleMode == SpellEffectHandleMode.Launch)
        {
            if (spellInfo.needsToBeTriggeredByCaster(spellInfo) && effectInfo.getProvidedTargetMask().hasFlag(SpellCastTargetFlags.UnitMask)) {
                return;
            }

            if (spellInfo.getExplicitTargetMask().hasFlag(SpellCastTargetFlags.DestLocation)) {
                targets.setDst(targets);
            }

            var target = targets.getUnitTarget();

            if (target != null) {
                targets.setUnitTarget(target);
            } else {
                var unit = caster.toUnit();

                if (unit != null) {
                    targets.setUnitTarget(unit);
                } else {
                    var go = caster.toGameObject();

                    if (go != null) {
                        targets.setGOTarget(go);
                    }
                }
            }
        }

        var delay = duration.Zero;

        if (effectInfo.effect == SpellEffectName.triggerSpell) {
            delay = duration.ofSeconds(effectInfo.miscValue);
        }

        var caster = caster;
        var originalCaster = originalCasterGuid;
        var castItemGuid = castItemGuid;
        var originalCastId = castId;
        var triggerSpell = effectInfo.triggerSpell;
        var effect = effectInfo.effect;
        var value = damage;
        var itemLevel = castItemLevel;

        caster.getEvents().AddEventAtOffset(() ->
        {
            targets.update(caster); // refresh pointers stored in targets

            // original caster guid only for GO cast
            CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlag.FullMask);
            args.setOriginalCaster(originalCaster);
            args.originalCastId = originalCastId;
            args.originalCastItemLevel = itemLevel;

            var triggerSpellInfo = global.getSpellMgr().getSpellInfo(triggerSpell, caster.getMap().getDifficultyID());

            if (!castItemGuid.isEmpty() && triggerSpellInfo.hasAttribute(SpellAttr2.RetainItemCast)) {
                var triggeringAuraCaster = caster == null ? null : caster.toPlayer();

                if (triggeringAuraCaster != null) {
                    args.castItem = triggeringAuraCaster.getItemByGuid(castItemGuid);
                }
            }

            // set basepoints for trigger with second effect
            if (effect == SpellEffectName.TriggerSpellWithValue) {
                for (var eff : triggerSpellInfo.getEffects()) {
                    args.addSpellMod(SpellValueMod.BasePoint0 + eff.effectIndex, value);
                }
            }

            caster.castSpell(targets, triggerSpell, args);
        }, delay);
    }

    
    private void effectTriggerMissileSpell() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget && effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var triggered_spell_id = effectInfo.triggerSpell;

        if (triggered_spell_id == 0) {
            Log.outWarn(LogFilter.spells, String.format("Spell::EffectTriggerMissileSpell: Spell %1$s [EffectIndex: %2$s] does not have triggered spell.", spellInfo.getId(), effectInfo.effectIndex));

            return;
        }

        // normal case
        var spellInfo = global.getSpellMgr().getSpellInfo(triggered_spell_id, getCastDifficulty());

        if (spellInfo == null) {
            Log.outDebug(LogFilter.spells, "Spell.EffectTriggerMissileSpell spell {0} tried to trigger unknown spell {1}", spellInfo.getId(), triggered_spell_id);

            return;
        }

        SpellCastTargets targets = new SpellCastTargets();

        if (effectHandleMode == SpellEffectHandleMode.HitTarget) {
            if (!spellInfo.needsToBeTriggeredByCaster(spellInfo)) {
                return;
            }

            targets.setUnitTarget(unitTarget);
        } else //if (effectHandleMode == SpellEffectHandleMode.hit)
        {
            if (spellInfo.needsToBeTriggeredByCaster(spellInfo) && effectInfo.getProvidedTargetMask().hasFlag(SpellCastTargetFlags.UnitMask)) {
                return;
            }

            if (spellInfo.getExplicitTargetMask().hasFlag(SpellCastTargetFlags.DestLocation)) {
                targets.setDst(targets);
            }

            var unit = caster.toUnit();

            if (unit != null) {
                targets.setUnitTarget(unit);
            } else {
                var go = caster.toGameObject();

                if (go != null) {
                    targets.setGOTarget(go);
                }
            }
        }

        CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlag.FullMask);
        args.setOriginalCaster(originalCasterGuid);
        args.setTriggeringSpell(this);

        // set basepoints for trigger with second effect
        if (effectInfo.effect == SpellEffectName.TriggerMissileSpellWithValue) {
            for (var eff : spellInfo.getEffects()) {
                args.addSpellMod(SpellValueMod.BasePoint0 + eff.effectIndex, damage);
            }
        }

        // original caster guid only for GO cast
        caster.castSpell(targets, spellInfo.getId(), args);
    }

    
    private void effectForceCast() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        var triggered_spell_id = effectInfo.triggerSpell;

        if (triggered_spell_id == 0) {
            Log.outWarn(LogFilter.spells, String.format("Spell::EffectForceCast: Spell %1$s [EffectIndex: %2$s] does not have triggered spell.", spellInfo.getId(), effectInfo.effectIndex));

            return;
        }

        // normal case
        var spellInfo = global.getSpellMgr().getSpellInfo(triggered_spell_id, getCastDifficulty());

        if (spellInfo == null) {
            Log.outError(LogFilter.spells, "Spell.EffectForceCast of spell {0}: triggering unknown spell id {1}", spellInfo.getId(), triggered_spell_id);

            return;
        }

        if (effectInfo.effect == SpellEffectName.ForceCast && damage != 0) {
            switch (spellInfo.getId()) {
                case 52588: // Skeletal Gryphon Escape
                case 48598: // Ride Flamebringer Cue
                    unitTarget.removeAura((int) damage);

                    break;
                case 52463: // Hide In Mine Car
                case 52349: // Overtake
                {
                    CastSpellExtraArgs args1 = new CastSpellExtraArgs(TriggerCastFlag.FullMask);
                    args1.setOriginalCaster(originalCasterGuid);
                    args1.setTriggeringSpell(this);
                    args1.addSpellMod(SpellValueMod.BasePoint0, damage);
                    unitTarget.castSpell(unitTarget, spellInfo.getId(), args1);

                    return;
                }
            }
        }

        switch (spellInfo.getId()) {
            case 72298: // Malleable Goo Summon
                unitTarget.castSpell(unitTarget, spellInfo.getId(), (new CastSpellExtraArgs(TriggerCastFlag.FullMask)).setOriginalCaster(originalCasterGuid).setTriggeringSpell(this));

                return;
        }

        CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlag.FullMask);
        args.setTriggeringSpell(this);

        // set basepoints for trigger with second effect
        if (effectInfo.effect == SpellEffectName.ForceCastWithValue) {
            for (var eff : spellInfo.getEffects()) {
                args.addSpellMod(SpellValueMod.BasePoint0 + eff.effectIndex, damage);
            }
        }

        unitTarget.castSpell(caster, spellInfo.getId(), args);
    }

    
    private void effectTriggerRitualOfSummoning() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var triggered_spell_id = effectInfo.triggerSpell;

        if (triggered_spell_id == 0) {
            Log.outWarn(LogFilter.spells, String.format("Spell::EffectTriggerRitualOfSummoning: Spell %1$s [EffectIndex: %2$s] does not have triggered spell.", spellInfo.getId(), effectInfo.effectIndex));

            return;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(triggered_spell_id, getCastDifficulty());

        if (spellInfo == null) {
            Log.outError(LogFilter.spells, String.format("EffectTriggerRitualOfSummoning of spell %1$s: triggering unknown spell id %2$s", spellInfo.getId(), triggered_spell_id));

            return;
        }

        finish();

        caster.castSpell((unit) null, spellInfo.getId(), (new CastSpellExtraArgs()).setTriggeringSpell(this));
    }

    private void calculateJumpSpeeds(SpellEffectInfo effInfo, float dist, tangible.OutObject<Float> speedXY, tangible.OutObject<Float> speedZ) {
        var unitCaster = getUnitCasterForEffectHandlers();
        var runSpeed = unitCaster.isControlledByPlayer() ? SharedConst.playerBaseMoveSpeed[UnitMoveType.run.getValue()] : SharedConst.baseMoveSpeed[UnitMoveType.run.getValue()];
        var creature = unitCaster.toCreature();

        if (creature != null) {
            runSpeed *= creature.getCreatureTemplate().speedRun;
        }

        var multiplier = (float) effInfo.amplitude;

        if (multiplier <= 0.0f) {
            multiplier = 1.0f;
        }

        speedXY.outArgValue = Math.min(runSpeed * 3.0f * multiplier, Math.max(28.0f, unitCaster.getSpeed(UnitMoveType.run) * 4.0f));

        var duration = dist / speedXY.outArgValue;
        var durationSqr = duration * duration;
        var minHeight = effInfo.miscValue != 0 ? effInfo.MiscValue / 10.0f : 0.5f; // Lower bound is blizzlike
        var maxHeight = effInfo.miscValueB != 0 ? effInfo.MiscValueB / 10.0f : 1000.0f; // Upper bound is unknown
        float height;

        if (durationSqr < minHeight * 8 / MotionMaster.GRAVITY) {
            height = minHeight;
        } else if (durationSqr > maxHeight * 8 / MotionMaster.GRAVITY) {
            height = maxHeight;
        } else {
            height = (float) (MotionMaster.GRAVITY * durationSqr / 8);
        }

        speedZ.outArgValue = (float) Math.sqrt((float) (2 * MotionMaster.GRAVITY * height));
    }

    
    private void effectJump() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (unitCaster.isInFlight()) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        float speedXY;
        tangible.OutObject<Float> tempOut_speedXY = new tangible.OutObject<Float>();
        float speedZ;
        tangible.OutObject<Float> tempOut_speedZ = new tangible.OutObject<Float>();
        calculateJumpSpeeds(effectInfo, unitCaster.getLocation().getExactdist2D(unitTarget.getLocation()), tempOut_speedXY, tempOut_speedZ);
        speedZ = tempOut_speedZ.outArgValue;
        speedXY = tempOut_speedXY.outArgValue;
        JumpArrivalCastArgs arrivalCast = new JumpArrivalCastArgs();
        arrivalCast.spellId = effectInfo.triggerSpell;
        arrivalCast.target = unitTarget.getGUID();
        unitCaster.getMotionMaster().moveJump(unitTarget.getLocation(), speedXY, speedZ, eventId.jump, false, arrivalCast);
    }

    
    private void effectJumpDest() {
        if (effectHandleMode != SpellEffectHandleMode.Launch) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (unitCaster.isInFlight()) {
            return;
        }

        if (!targets.getHasDst()) {
            return;
        }

        float speedXY;
        tangible.OutObject<Float> tempOut_speedXY = new tangible.OutObject<Float>();
        float speedZ;
        tangible.OutObject<Float> tempOut_speedZ = new tangible.OutObject<Float>();
        calculateJumpSpeeds(effectInfo, unitCaster.getLocation().getExactdist2D(destTarget), tempOut_speedXY, tempOut_speedZ);
        speedZ = tempOut_speedZ.outArgValue;
        speedXY = tempOut_speedXY.outArgValue;
        JumpArrivalCastArgs arrivalCast = new JumpArrivalCastArgs();
        arrivalCast.spellId = effectInfo.triggerSpell;
        unitCaster.getMotionMaster().moveJump(destTarget, speedXY, speedZ, eventId.jump, !targets.getObjectTargetGUID().isEmpty(), arrivalCast);
    }

    
    private void effectTeleportUnits() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || unitTarget.isInFlight()) {
            return;
        }

        // If not exist data for dest location - return
        if (!targets.getHasDst()) {
            Log.outError(LogFilter.spells, "Spell.EffectTeleportUnits - does not have a destination for spellId {0}.", spellInfo.getId());

            return;
        }

        // Init dest coordinates
        WorldLocation targetDest = new worldLocation(destTarget);

        if (targetDest.getMapId() == 0xFFFFFFFF) {
            targetDest.setMapId(unitTarget.getLocation().getMapId());
        }

        if (targetDest.getO() == 0 && targets.getUnitTarget()) {
            targetDest.setO(targets.getUnitTarget().getLocation().getO());
        }

        var player = unitTarget.toPlayer();

        if (player != null) {
            // Custom loading screen
            var customLoadingScreenId = (int) effectInfo.miscValue;

            if (customLoadingScreenId != 0) {
                player.sendPacket(new CustomLoadScreen(spellInfo.getId(), customLoadingScreenId));
            }
        }

        if (targetDest.getMapId() == unitTarget.getLocation().getMapId()) {
            unitTarget.nearTeleportTo(targetDest, unitTarget == caster);
        } else if (player != null) {
            player.teleportTo(targetDest, unitTarget == _caster ? TeleportToOptions.Spell : 0);
        } else {
            Log.outError(LogFilter.spells, "Spell.EffectTeleportUnits - spellId {0} attempted to teleport creature to a different map.", spellInfo.getId());

            return;
        }
    }

    
    private void effectTeleportUnitsWithVisualLoadingScreen() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget) {
            return;
        }

        // If not exist data for dest location - return
        if (!targets.getHasDst()) {
            Log.outError(LogFilter.spells, String.format("Spell::EffectTeleportUnitsWithVisualLoadingScreen - does not have a destination for spellId %1$s.", spellInfo.getId()));

            return;
        }

        // Init dest coordinates
        WorldLocation targetDest = new worldLocation(destTarget);

        if (targetDest.getMapId() == 0xFFFFFFFF) {
            targetDest.setMapId(unitTarget.getLocation().getMapId());
        }

        if (targetDest.getO() == 0 && targets.getUnitTarget()) {
            targetDest.setO(targets.getUnitTarget().getLocation().getO());
        }

        if (effectInfo.miscValueB != 0) {
            var playerTarget = unitTarget.toPlayer();

            if (playerTarget != null) {
                playerTarget.sendPacket(new SpellVisualLoadScreen(effectInfo.miscValueB, effectInfo.miscValue));
            }
        }

        unitTarget.getEvents().AddEventAtOffset(new DelayedSpellTeleportEvent(unitTarget, targetDest, unitTarget == _caster ? TeleportToOptions.Spell : 0, spellInfo.getId()), duration.ofSeconds(effectInfo.miscValue));
    }

    
    private void effectApplyAura() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (spellAura == null || unitTarget == null) {
            return;
        }

        spellAura.setEmpoweredStage(getEmpoweredStage());

        // register target/effect on aura
        var aurApp = spellAura.getApplicationOfTarget(unitTarget.getGUID());

        if (aurApp == null) {
            aurApp = unitTarget._CreateAuraApplication(spellAura, new HashSet<Integer>() {
                EffectInfo.EffectIndex
            });
        } else {
            aurApp.getEffectsToApply().add(effectInfo.effectIndex);
            aurApp.updateApplyEffectMask(aurApp.getEffectsToApply(), false);
        }

        int dur;
        tangible.OutObject<Integer> tempOut_dur = new tangible.OutObject<Integer>();
        if (tryGetTotalEmpowerDuration(true, tempOut_dur)) {
            dur = tempOut_dur.outArgValue;
            spellAura.setDuration(dur, false, true);
        } else {
            dur = tempOut_dur.outArgValue;
        }
    }

    
    private void effectUnlearnSpecialization() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();
        var spellToUnlearn = effectInfo.triggerSpell;

        player.removeSpell(spellToUnlearn);

        Log.outDebug(LogFilter.spells, "Spell: Player {0} has unlearned spell {1} from NpcGUID: {2}", player.getGUID().toString(), spellToUnlearn, caster.getGUID().toString());
    }

    
    private void effectPowerDrain() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (effectInfo.miscValue < 0 || effectInfo.miscValue >= (byte) powerType.max.getValue()) {
            return;
        }

        var powerType = powerType.forValue((byte) effectInfo.miscValue);

        if (unitTarget == null || !unitTarget.isAlive() || unitTarget.getDisplayPowerType() != powerType || damage < 0) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        // add spell damage bonus
        if (unitCaster != null) {
            var bonus = unitCaster.spellDamageBonusDone(unitTarget, spellInfo, damage, DamageEffectType.SpellDirect, effectInfo, 1, this);
            damage = bonus + (bonus * variance);
            damage = unitTarget.spellDamageBonusTaken(unitCaster, spellInfo, damage, DamageEffectType.SpellDirect);
        }

        double newDamage = -(unitTarget.modifyPower(powerType, -Damage));

        // Don't restore from self drain
        double gainMultiplier = 0.0f;

        if (unitCaster != null && unitCaster != unitTarget) {
            gainMultiplier = effectInfo.calcValueMultiplier(unitCaster, this);
            var gain = newDamage * gainMultiplier;

            unitCaster.energizeBySpell(unitCaster, spellInfo, gain, powerType);
        }

        executeLogEffectTakeTargetPower(effectInfo.effect, unitTarget, powerType, (int) newDamage, gainMultiplier);
    }

    
    private void effectSendEvent() {
        // we do not handle a flag dropping or clicking on flag in Battlegroundby sendevent system
        if (effectHandleMode != SpellEffectHandleMode.HitTarget && effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        WorldObject target = null;

        // call events for object target if present
        if (effectHandleMode == SpellEffectHandleMode.HitTarget) {
            if (unitTarget != null) {
                target = unitTarget;
            } else if (gameObjTarget != null) {
                target = gameObjTarget;
            } else if (corpseTarget != null) {
                target = corpseTarget;
            }
        } else // if (effectHandleMode == SpellEffectHandleMode.hit)
        {
            // let's prevent executing effect handler twice in case when spell effect is capable of targeting an object
            // this check was requested by scripters, but it has some downsides:
            // now it's impossible to script (using sEventScripts) a cast which misses all targets
            // or to have an ability to script the moment spell hits dest (in a case when there are object targets present)
            if (effectInfo.getProvidedTargetMask().hasFlag(SpellCastTargetFlags.UnitMask.getValue() | SpellCastTargetFlags.GameobjectMask.getValue())) {
                return;
            }

            // some spells have no target entries in dbc and they use focus target
            if (focusObject != null) {
                target = focusObject;
            }
            // @todo there should be a possibility to pass dest target to event script
        }

        Log.outDebug(LogFilter.spells, "Spell ScriptStart {0} for spellid {1} in EffectSendEvent ", effectInfo.miscValue, spellInfo.getId());

        GameEvents.trigger((int) effectInfo.miscValue, caster, target);
    }

    
    private void effectPowerBurn() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (effectInfo.miscValue < 0 || effectInfo.miscValue >= powerType.max.getValue()) {
            return;
        }

        var powerType = powerType.forValue((byte) effectInfo.miscValue);

        if (unitTarget == null || !unitTarget.isAlive() || unitTarget.getDisplayPowerType() != powerType || damage < 0) {
            return;
        }

        double newDamage = -(unitTarget.modifyPower(powerType, -Damage));

        // NO - Not a typo - EffectPowerBurn uses effect second multiplier - not effect damage multiplier
        var dmgMultiplier = effectInfo.calcValueMultiplier(getUnitCasterForEffectHandlers(), this);

        // add log data before multiplication (need power amount, not damage)
        executeLogEffectTakeTargetPower(effectInfo.effect, unitTarget, powerType, (int) newDamage, 0.0f);

        newDamage = newDamage * dmgMultiplier;

        damageInEffects += newDamage;
    }

    
    private void effectHeal() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive() || damage < 0) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        // Skip if m_originalCaster not available
        if (unitCaster == null) {
            return;
        }

        var addhealth = damage;

        // Vessel of the Naaru (Vial of the Sunwell trinket)
        /**@todo: move this to scripts
         */
        if (spellInfo.getId() == 45064) {
            // Amount of heal - depends from stacked Holy Energy
            double damageAmount = 0;
            var aurEff = unitCaster.getAuraEffect(45062, 0);

            if (aurEff != null) {
                damageAmount += aurEff.amount;
                unitCaster.removeAura(45062);
            }

            addhealth += damageAmount;
        }
        // Death Pact - return pct of max health to caster
        else if (spellInfo.getSpellFamilyName() == SpellFamilyName.Deathknight && spellInfo.getSpellFamilyFlags().get(0).hasFlag(0x00080000)) {
            addhealth = unitCaster.spellHealingBonusDone(unitTarget, spellInfo, unitCaster.countPctFromMaxHealth(damage), DamageEffectType.Heal, effectInfo, 1, this);
        } else {
            var bonus = unitCaster.spellHealingBonusDone(unitTarget, spellInfo, addhealth, DamageEffectType.Heal, effectInfo, 1, this);
            addhealth = (bonus + (bonus * variance));
        }

        addhealth = (int) unitTarget.spellHealingBonusTaken(unitCaster, spellInfo, addhealth, DamageEffectType.Heal);

        // Remove Grievious bite if fully healed
        if (unitTarget.hasAura(48920) && ((unitTarget.getHealth() + addhealth) >= unitTarget.getMaxHealth())) {
            unitTarget.removeAura(48920);
        }

        healingInEffects += addhealth;
    }

    
    private void effectHealPct() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive() || damage < 0) {
            return;
        }

        var heal = (double) unitTarget.countPctFromMaxHealth(damage);
        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster) {
            heal = unitCaster.spellHealingBonusDone(unitTarget, spellInfo, heal, DamageEffectType.Heal, effectInfo, 1, this);
            heal = unitTarget.spellHealingBonusTaken(unitCaster, spellInfo, heal, DamageEffectType.Heal);
        }

        healingInEffects += heal;
    }

    
    private void effectHealMechanical() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive() || damage < 0) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();
        var heal = damage;

        if (unitCaster) {
            heal = unitCaster.spellHealingBonusDone(unitTarget, spellInfo, heal, DamageEffectType.Heal, effectInfo, 1, this);
        }

        heal += heal * variance;

        if (unitCaster) {
            heal = unitTarget.spellHealingBonusTaken(unitCaster, spellInfo, heal, DamageEffectType.Heal);
        }

        healingInEffects += heal;
    }

    
    private void effectHealthLeech() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive() || damage < 0) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();
        int bonus = 0;

        if (unitCaster != null) {
            unitCaster.spellDamageBonusDone(unitTarget, spellInfo, damage, DamageEffectType.SpellDirect, effectInfo, 1, this);
        }

        damage = bonus + (bonus * variance);

        if (unitCaster != null) {
            damage = unitTarget.spellDamageBonusTaken(unitCaster, spellInfo, damage, DamageEffectType.SpellDirect);
        }

        Log.outDebug(LogFilter.spells, "HealthLeech :{0}", damage);

        var healMultiplier = effectInfo.calcValueMultiplier(unitCaster, this);

        damageInEffects += damage;

        DamageInfo damageInfo = new DamageInfo(unitCaster, unitTarget, damage, spellInfo, spellInfo.getSchoolMask(), DamageEffectType.Direct, WeaponAttackType.BaseAttack);
        unit.calcAbsorbResist(damageInfo);
        var absorb = damageInfo.getAbsorb();
        Damage -= absorb;

        // get max possible damage, don't count overkill for heal
        var healthGain = (-UnitTarget.getHealthGain(-Damage) * healMultiplier);

        if (unitCaster != null && unitCaster.isAlive()) {
            healthGain = unitCaster.spellHealingBonusDone(unitCaster, spellInfo, healthGain, DamageEffectType.Heal, effectInfo, 1, this);
            healthGain = unitCaster.spellHealingBonusTaken(unitCaster, spellInfo, healthGain, DamageEffectType.Heal);

            HealInfo healInfo = new HealInfo(unitCaster, unitCaster, healthGain, spellInfo, spellSchoolMask);
            unitCaster.healBySpell(healInfo);
        }
    }

    
    private void effectCreateItem() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        doCreateItem(effectInfo.itemType, spellInfo.hasAttribute(SpellAttr0.IsTradeskill) ? itemContext.TradeSkill : itemContext.NONE);
        executeLogEffectCreateItem(effectInfo.effect, effectInfo.itemType);
    }

    
    private void effectCreateItem2() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();

        var context = spellInfo.hasAttribute(SpellAttr0.IsTradeskill) ? itemContext.TradeSkill : itemContext.NONE;

        // Pick a random item from spell_loot_template
        if (spellInfo.isLootCrafting()) {
            player.autoStoreLoot(spellInfo.getId(), LootStorage.spell, context, false, true);
            player.updateCraftSkill(spellInfo);
        } else // If there's no random loot entries for this spell, pick the item associated with this spell
        {
            var itemId = effectInfo.itemType;

            if (itemId != 0) {
                doCreateItem(itemId, context);
            }
        }

        // @todo executeLogEffectCreateItem(i, getEffect(i].itemType);
    }

    
    private void effectCreateRandomItem() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();

        // create some random items
        player.autoStoreLoot(spellInfo.getId(), LootStorage.spell, spellInfo.hasAttribute(SpellAttr0.IsTradeskill) ? itemContext.TradeSkill : itemContext.NONE);
        // @todo executeLogEffectCreateItem(i, getEffect(i].itemType);
    }

    
    private void effectPersistentAA() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        // only handle at last effect
        for (var i = effectInfo.effectIndex + 1; i < spellInfo.getEffects().size(); ++i) {
            if (spellInfo.getEffect(i).isEffect(SpellEffectName.PersistentAreaAura)) {
                return;
            }
        }

        var radius = effectInfo.calcRadius(unitCaster);

        // Caster not in world, might be spell triggered from aura removal
        if (!unitCaster.isInWorld()) {
            return;
        }

        DynamicObject dynObj = new DynamicObject(false);

        if (!dynObj.createDynamicObject(unitCaster.getMap().generateLowGuid(HighGuid.DynamicObject), unitCaster, spellInfo, destTarget, radius, DynamicObjectType.AreaSpell, spellVisual)) {
            dynObj.close();

            return;
        }

        AuraCreateInfo createInfo = new AuraCreateInfo(castId, spellInfo, getCastDifficulty(), SpellConst.MaxEffects, dynObj);
        createInfo.setCaster(unitCaster);
        createInfo.setBaseAmount(spellValue.effectBasePoints);
        createInfo.setCastItem(castItemGuid, castItemEntry, castItemLevel);

        var aura = aura.tryCreate(createInfo);

        if (aura != null) {
            dynObjAura = aura.toDynObjAura();
            dynObjAura._RegisterForTargets();
        } else {
            return;
        }

        dynObjAura._ApplyEffectForTargets(effectInfo.effectIndex);
    }

    
    private void effectEnergize() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null || unitTarget == null) {
            return;
        }

        if (!unitTarget.isAlive()) {
            return;
        }

        if (effectInfo.miscValue < 0 || effectInfo.miscValue >= (byte) powerType.max.getValue()) {
            return;
        }

        var power = powerType.forValue((byte) effectInfo.miscValue);

        if (unitTarget.getMaxPower(power) == 0) {
            return;
        }

        tangible.RefObject<Double> tempRef_Damage = new tangible.RefObject<Double>(damage);
        this.<ISpellEnergizedBySpell>ForEachSpellScript(a -> a.energizeBySpell(unitTarget, spellInfo, tempRef_Damage, power));
        damage = tempRef_Damage.refArgValue;

        unitCaster.energizeBySpell(unitTarget, spellInfo, damage, power);
    }

    
    private void effectEnergizePct() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null || unitTarget == null) {
            return;
        }

        if (!unitTarget.isAlive()) {
            return;
        }

        if (effectInfo.miscValue < 0 || effectInfo.miscValue >= (byte) powerType.max.getValue()) {
            return;
        }

        var power = powerType.forValue((byte) effectInfo.miscValue);
        var maxPower = (int) unitTarget.getMaxPower(power);

        if (maxPower == 0) {
            return;
        }

        var gain = (int) MathUtil.CalculatePct(maxPower, damage);
        unitCaster.energizeBySpell(unitTarget, spellInfo, gain, power);
    }

    
    private void effectOpenLock() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!caster.isTypeId(TypeId.PLAYER)) {
            Log.outDebug(LogFilter.spells, "WORLD: Open Lock - No Player caster!");

            return;
        }

        var player = caster.toPlayer();

        int lockId;
        ObjectGuid guid = ObjectGuid.EMPTY;

        // Get lockId
        if (gameObjTarget != null) {
            var goInfo = gameObjTarget.getTemplate();

            if (goInfo.getNoDamageImmune() != 0 && player.hasUnitFlag(UnitFlag.Immune)) {
                return;
            }

            // Arathi Basin banner opening. // @todo Verify correctness of this check
            if ((goInfo.type == GameObjectTypes.button && goInfo.button.noDamageImmune != 0) || (goInfo.type == GameObjectTypes.goober && goInfo.goober.requireLOS != 0)) {
                //CanUseBattlegroundObject() already called in checkCast()
                // in Battlegroundcheck
                var bg = player.getBattleground();

                if (bg) {
                    bg.eventPlayerClickedOnFlag(player, gameObjTarget);

                    return;
                }
            } else if (goInfo.type == GameObjectTypes.capturePoint) {
                gameObjTarget.assaultCapturePoint(player);

                return;
            } else if (goInfo.type == GameObjectTypes.flagStand) {
                //CanUseBattlegroundObject() already called in checkCast()
                // in Battlegroundcheck
                var bg = player.getBattleground();

                if (bg) {
                    if (bg.getTypeID(true) == BattlegroundTypeId.EY) {
                        bg.eventPlayerClickedOnFlag(player, gameObjTarget);
                    }

                    return;
                }
            } else if (goInfo.type == GameObjectTypes.newFlag) {
                gameObjTarget.use(player);

                return;
            } else if (spellInfo.getId() == 1842 && gameObjTarget.getTemplate().type == GameObjectTypes.trap && gameObjTarget.getOwnerUnit() != null) {
                gameObjTarget.setLootState(LootState.JustDeactivated);

                return;
            }
            // @todo Add script for spell 41920 - Filling, becouse server it freze when use this spell
            // handle outdoor pvp object opening, return true if go was registered for handling
            // these objects must have been spawned by outdoorpvp!
            else if (gameObjTarget.getTemplate().type == GameObjectTypes.goober && global.getOutdoorPvPMgr().handleOpenGo(player, gameObjTarget)) {
                return;
            }

            lockId = goInfo.getLockId();
            guid = gameObjTarget.getGUID();
        } else if (itemTarget != null) {
            lockId = itemTarget.getTemplate().getLockID();
            guid = itemTarget.getGUID();
        } else {
            Log.outDebug(LogFilter.spells, "WORLD: Open Lock - No GameObject/Item target!");

            return;
        }

        var skillId = SkillType.NONE;
        var reqSkillValue = 0;
        var skillValue = 0;

        tangible.RefObject<SkillType> tempRef_skillId = new tangible.RefObject<SkillType>(skillId);
        tangible.RefObject<Integer> tempRef_reqSkillValue = new tangible.RefObject<Integer>(reqSkillValue);
        tangible.RefObject<Integer> tempRef_skillValue = new tangible.RefObject<Integer>(skillValue);
        var res = canOpenLock(effectInfo, lockId, tempRef_skillId, tempRef_reqSkillValue, tempRef_skillValue);
        skillValue = tempRef_skillValue.refArgValue;
        reqSkillValue = tempRef_reqSkillValue.refArgValue;
        skillId = tempRef_skillId.refArgValue;

        if (res != SpellCastResult.SpellCastOk) {
            sendCastResult(res);

            return;
        }

        if (gameObjTarget != null) {
            gameObjTarget.use(player);
        } else if (itemTarget != null) {
            itemTarget.setItemFlag(ItemFieldFlags.unlocked);
            itemTarget.setState(ItemUpdateState.changed, itemTarget.getOwnerUnit());
        }

        // not allow use skill grow at item base open
        if (castItem == null && skillId != SkillType.NONE) {
            // update skill if really known
            int pureSkillValue = player.getPureSkillValue(skillId);

            if (pureSkillValue != 0) {
                if (gameObjTarget != null) {
                    // Allow one skill-up until respawned
                    if (!gameObjTarget.isInSkillupList(player.getGUID()) && player.updateGatherSkill(skillId, pureSkillValue, (int) reqSkillValue, 1, gameObjTarget)) {
                        gameObjTarget.addToSkillupList(player.getGUID());
                    }
                } else if (itemTarget != null) {
                    // Do one skill-up
                    player.updateGatherSkill(skillId, pureSkillValue, (int) reqSkillValue);
                }
            }
        }

        executeLogEffectOpenLock(effectInfo.effect, gameObjTarget != null ? GameObjTarget : (WorldObject) itemTarget);
    }

    
    private void effectSummonChangeItem() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (!caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = caster.toPlayer();

        // applied only to using item
        if (castItem == null) {
            return;
        }

        // ... only to item in own inventory/bank/equip_slot
        if (ObjectGuid.opNotEquals(castItem.getOwnerGUID(), player.getGUID())) {
            return;
        }

        var newitemid = effectInfo.itemType;

        if (newitemid == 0) {
            return;
        }

        var pos = castItem.getPos();

        var pNewItem = item.createItem(newitemid, 1, castItem.getContext(), player);

        if (pNewItem == null) {
            return;
        }

        for (var j = EnchantmentSlot.Perm; j.getValue() <= EnchantmentSlot.Temp.getValue(); ++j) {
            if (castItem.getEnchantmentId(j) != 0) {
                pNewItem.setEnchantment(j, castItem.getEnchantmentId(j), castItem.getEnchantmentDuration(j), (int) castItem.getEnchantmentCharges(j));
            }
        }

        if (castItem.getItemData().durability < castItem.getItemData().maxDurability) {
            double lossPercent = 1 - castItem.getItemData().Durability / castItem.getItemData().maxDurability;
            player.durabilityLoss(pNewItem, lossPercent);
        }

        if (player.isInventoryPos(pos)) {
            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = player.canStoreItem(castItem.getBagSlot(), castItem.getSlot(), dest, pNewItem, true);

            if (msg == InventoryResult.Ok) {
                player.destroyItem(castItem.getBagSlot(), castItem.getSlot(), true);

                // prevent crash at access and unexpected charges counting with item update queue corrupt
                if (castItem == targets.getItemTarget()) {
                    targets.setItemTarget(null);
                }

                castItem = null;
                castItemGuid.clear();
                castItemEntry = 0;
                castItemLevel = -1;

                player.storeItem(dest, pNewItem, true);
                player.sendNewItem(pNewItem, 1, true, false);
                player.itemAddedQuestCheck(newitemid, 1);

                return;
            }
        } else if (player.isBankPos(pos)) {
            ArrayList<ItemPosCount> dest = new ArrayList<>();
            var msg = player.canBankItem(castItem.getBagSlot(), castItem.getSlot(), dest, pNewItem, true);

            if (msg == InventoryResult.Ok) {
                player.destroyItem(castItem.getBagSlot(), castItem.getSlot(), true);

                // prevent crash at access and unexpected charges counting with item update queue corrupt
                if (castItem == targets.getItemTarget()) {
                    targets.setItemTarget(null);
                }

                castItem = null;
                castItemGuid.clear();
                castItemEntry = 0;
                castItemLevel = -1;

                player.bankItem(dest, pNewItem, true);

                return;
            }
        } else if (player.isEquipmentPos(pos)) {
            player.destroyItem(castItem.getBagSlot(), castItem.getSlot(), true);

            short dest;
            tangible.OutObject<SHORT> tempOut_dest = new tangible.OutObject<SHORT>();
            var msg = player.canEquipItem(castItem.getSlot(), tempOut_dest, pNewItem, true);
            dest = tempOut_dest.outArgValue;

            if (msg == InventoryResult.Ok || msg == InventoryResult.ClientLockedOut) {
                if (msg == InventoryResult.ClientLockedOut) {
                    dest = EquipmentSlot.MainHand;
                }

                // prevent crash at access and unexpected charges counting with item update queue corrupt
                if (castItem == targets.getItemTarget()) {
                    targets.setItemTarget(null);
                }

                castItem = null;
                castItemGuid.clear();
                castItemEntry = 0;
                castItemLevel = -1;

                player.equipItem(dest, pNewItem, true);
                player.autoUnequipOffhandIfNeed();
                player.sendNewItem(pNewItem, 1, true, false);
                player.itemAddedQuestCheck(newitemid, 1);

                return;
            }
        }
    }

    
    private void effectProficiency() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (!caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var p_target = caster.toPlayer();

        var subClassMask = (int) spellInfo.getEquippedItemSubClassMask();

        if (spellInfo.getEquippedItemClass() == itemClass.Weapon && !(boolean) (p_target.getWeaponProficiency() & subClassMask)) {
            p_target.addWeaponProficiency(subClassMask);
            p_target.sendProficiency(itemClass.Weapon, p_target.getWeaponProficiency());
        }

        if (spellInfo.getEquippedItemClass() == itemClass.armor && !(boolean) (p_target.getArmorProficiency() & subClassMask)) {
            p_target.addArmorProficiency(subClassMask);
            p_target.sendProficiency(itemClass.armor, p_target.getArmorProficiency());
        }
    }

    
    private void effectSummonType() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var entry = (int) effectInfo.miscValue;

        if (entry == 0) {
            return;
        }

        var properties = CliDB.SummonPropertiesStorage.get(effectInfo.miscValueB);

        if (properties == null) {
            Log.outError(LogFilter.spells, "EffectSummonType: Unhandled summon type {0}", effectInfo.miscValueB);

            return;
        }

        var caster = caster;

        if (originalCaster) {
            caster = originalCaster;
        }

        var privateObjectOwner = caster.getGUID();

        if (!properties.getFlags().hasFlag(SummonPropertiesFlags.OnlyVisibleToSummoner.getValue() | SummonPropertiesFlags.OnlyVisibleToSummonerGroup.getValue())) {
            privateObjectOwner = ObjectGuid.Empty;
        }

        if (caster.isPrivateObject()) {
            privateObjectOwner = caster.getPrivateObjectOwner();
        }

        if (properties.getFlags().hasFlag(SummonPropertiesFlags.OnlyVisibleToSummonerGroup)) {
            if (caster.isPlayer() && originalCaster.toPlayer().getGroup()) {
                privateObjectOwner = caster.toPlayer().getGroup().getGUID();
            }
        }

        var duration = spellInfo.calcDuration(caster);

        if (spellValue.summonDuration != null) {
            duration = (int) spellValue.summonDuration.doubleValue();
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        TempSummon summon = null;

        // determine how many units should be summoned
        int numSummons;

        // some spells need to summon many units, for those spells number of summons is stored in effect second
        // however so far noone found a generic check to find all of those (there's no related data in summonproperties.dbc
        // and in spell attributes, possibly we need to add a table for those)
        // so here's a list of MiscValueB values, which is currently most generic check
        switch (effectInfo.miscValueB) {
            case 64:
            case 61:
            case 1101:
            case 66:
            case 648:
            case 2301:
            case 1061:
            case 1261:
            case 629:
            case 181:
            case 715:
            case 1562:
            case 833:
            case 1161:
            case 713:
                numSummons = (int) (damage > 0 ? Damage : 1);

                break;
            default:
                numSummons = 1;

                break;
        }

        switch (properties.Control) {
            case SummonCategory.Wild:
            case SummonCategory.Ally:
            case SummonCategory.Unk:
                if (properties.getFlags().hasFlag(SummonPropertiesFlags.JoinSummonerSpawnGroup)) {
                    summonGuardian(effectInfo, entry, properties, numSummons, privateObjectOwner);

                    break;
                }

                switch (properties.title) {
                    case SummonTitle.Pet:
                    case SummonTitle.Guardian:
                    case SummonTitle.Runeblade:
                    case SummonTitle.Minion:
                        summonGuardian(effectInfo, entry, properties, numSummons, privateObjectOwner);

                        break;
                    // Summons a vehicle, but doesn't force anyone to enter it (see SUMMON_CATEGORY_VEHICLE)
                    case SummonTitle.Vehicle:
                    case SummonTitle.Mount: {
                        if (unitCaster == null) {
                            return;
                        }

                        summon = unitCaster.getMap().summonCreature(entry, destTarget, properties, (int) duration, unitCaster, spellInfo.getId());

                        break;
                    }
                    case SummonTitle.LightWell:
                    case SummonTitle.Totem: {
                        if (unitCaster == null) {
                            return;
                        }

                        summon = unitCaster.getMap().summonCreature(entry, destTarget, properties, (int) duration, unitCaster, spellInfo.getId(), 0, privateObjectOwner);

                        if (summon == null || !summon.isTotem()) {
                            return;
                        }

                        if (damage != 0) // if not spell info, DB values used
                        {
                            summon.setMaxHealth((int) damage);
                            summon.setHealth((int) damage);
                        }

                        break;
                    }
                    case SummonTitle.Companion: {
                        if (unitCaster == null) {
                            return;
                        }

                        summon = unitCaster.getMap().summonCreature(entry, destTarget, properties, (int) duration, unitCaster, spellInfo.getId(), 0, privateObjectOwner);

                        if (summon == null || !summon.hasUnitTypeMask(UnitTypeMask.minion)) {
                            return;
                        }

                        summon.setImmuneToAll(true);

                        break;
                    }
                    default: {
                        var radius = effectInfo.calcRadius();

                        var summonType = (duration == 0) ? TempSummonType.DeadDespawn : TempSummonType.TimedDespawn;

                        for (int count = 0; count < numSummons; ++count) {
                            Position pos;

                            if (count == 0) {
                                pos = destTarget;
                            } else {
                                // randomize position for multiple summons
                                pos = caster.getRandomPoint(destTarget, radius);
                            }

                            summon = caster.getMap().summonCreature(entry, pos, properties, (int) duration, unitCaster, spellInfo.getId(), 0, privateObjectOwner);

                            if (summon == null) {
                                continue;
                            }

                            summon.setTempSummonType(summonType);

                            if (properties.Control == SummonCategory.Ally) {
                                summon.setOwnerGUID(caster.getGUID());
                            }

                            executeLogEffectSummonObject(effectInfo.effect, summon);
                        }

                        return;
                    }
                } //switch

                break;
            case SummonCategory.Pet:
                summonGuardian(effectInfo, entry, properties, numSummons, privateObjectOwner);

                break;
            case SummonCategory.Puppet: {
                if (unitCaster == null) {
                    return;
                }

                summon = unitCaster.getMap().summonCreature(entry, destTarget, properties, (int) duration, unitCaster, spellInfo.getId(), 0, privateObjectOwner);

                break;
            }
            case SummonCategory.Vehicle: {
                if (unitCaster == null) {
                    return;
                }

                // Summoning spells (usually triggered by npc_spellclick) that spawn a vehicle and that cause the clicker
                // to cast a ride vehicle spell on the summoned unit.
                summon = unitCaster.getMap().summonCreature(entry, destTarget, properties, (int) duration, unitCaster, spellInfo.getId());

                if (summon == null || !summon.isVehicle()) {
                    return;
                }

                // The spell that this effect will trigger. It has SPELL_AURA_CONTROL_VEHICLE
                int spellId = SharedConst.VehicleSpellRideHardcoded;
                var basePoints = effectInfo.calcValue();

                if (basePoints > SharedConst.MaxVehicleSeats) {
                    var spellInfo = global.getSpellMgr().getSpellInfo((int) basePoints, getCastDifficulty());

                    if (spellInfo != null && spellInfo.hasAura(AuraType.ControlVehicle)) {
                        spellId = spellInfo.getId();
                    }
                }

                CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlag.FullMask);
                args.setTriggeringSpell(this);

                // if we have small second, it indicates seat position
                if (basePoints > 0 && basePoints < SharedConst.MaxVehicleSeats) {
                    args.addSpellMod(SpellValueMod.BasePoint0, basePoints);
                }

                unitCaster.castSpell(summon, spellId, args);

                break;
            }
        }

        if (summon != null) {
            summon.setCreatorGUID(caster.getGUID());
            executeLogEffectSummonObject(effectInfo.effect, summon);
        }
    }

    
    private void effectLearnSpell() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        if (!unitTarget.isTypeId(TypeId.PLAYER)) {
            if (unitTarget.isPet()) {
                effectLearnPetSpell();
            }

            return;
        }

        var player = unitTarget.toPlayer();

        if (castItem != null && effectInfo.triggerSpell == 0) {
            for (var itemEffect : castItem.getEffects()) {
                if (itemEffect.triggerType != ItemSpelltriggerType.OnLearn) {
                    continue;
                }

                var dependent = false;

                var speciesEntry = BattlePetMgr.getBattlePetSpeciesBySpell((int) itemEffect.spellID);

                if (speciesEntry != null) {
                    player.getSession().getBattlePetMgr().addPet(speciesEntry.id, BattlePetMgr.selectPetDisplay(speciesEntry), BattlePetMgr.rollPetBreed(speciesEntry.id), BattlePetMgr.getDefaultPetQuality(speciesEntry.id));
                    // If the spell summons a battle pet, we fake that it has been learned and the battle pet is added
                    // marking as dependent prevents saving the spell to database (intended)
                    dependent = true;
                }

                player.learnSpell((int) itemEffect.spellID, dependent);
            }
        }

        if (effectInfo.triggerSpell != 0) {
            player.learnSpell(effectInfo.triggerSpell, false);
            Log.outDebug(LogFilter.spells, String.format("Spell: %1$s has learned spell %2$s from %3$s", player.getGUID(), effectInfo.triggerSpell, caster.getGUID()));
        }
    }

    
    private void effectDispel() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        // Create dispel mask by dispel type
        var dispel_type = (int) effectInfo.miscValue;
        var dispelMask = spellInfo.getDispelMask(DispelType.forValue(dispel_type));

        var dispelList = unitTarget.getDispellableAuraList(caster, dispelMask, targetMissInfo == SpellMissInfo.Reflect);

        if (dispelList.isEmpty()) {
            return;
        }

        var remaining = dispelList.size();

        // Ok if exist some buffs for dispel try dispel it
        ArrayList<DispelableAura> successList = new ArrayList<>();

        DispelFailed dispelFailed = new DispelFailed();
        dispelFailed.casterGUID = caster.getGUID();
        dispelFailed.victimGUID = unitTarget.getGUID();
        dispelFailed.spellID = spellInfo.getId();

        // dispel N = damage buffs (or while exist buffs for dispel)
        for (var count = 0; count < damage && remaining > 0; ) {
            // Random select buff for dispel
            var dispelableAura = dispelList.get(RandomUtil.IRand(0, remaining - 1));

            if (dispelableAura.rollDispel()) {
                var successAura = tangible.ListHelper.find(successList, dispelAura ->
                {
                    if (dispelAura.getAura().id == dispelableAura.getAura().getId() && dispelAura.getAura().caster == dispelableAura.getAura().getCaster()) {
                        return true;
                    }

                    return false;
                });

                byte dispelledCharges = 1;

                if (dispelableAura.getAura().getSpellInfo().hasAttribute(SpellAttr1.DispelAllStacks)) {
                    dispelledCharges = dispelableAura.getDispelCharges();
                }

                if (successAura == null) {
                    successList.add(new DispelableAura(dispelableAura.getAura(), 0, dispelledCharges));
                } else {
                    successAura.incrementCharges();
                }

                if (!dispelableAura.decrementCharge(dispelledCharges)) {
                    --remaining;
                    dispelList.set(remaining, dispelableAura);
                }
            } else {
                dispelFailed.failedSpells.add(dispelableAura.getAura().getId());
            }

            ++count;
        }

        if (!dispelFailed.failedSpells.isEmpty()) {
            caster.sendMessageToSet(dispelFailed, true);
        }

        if (successList.isEmpty()) {
            return;
        }

        SpellDispellLog spellDispellLog = new SpellDispellLog();
        spellDispellLog.isBreak = false; // TODO: use me
        spellDispellLog.isSteal = false;

        spellDispellLog.targetGUID = unitTarget.getGUID();
        spellDispellLog.casterGUID = caster.getGUID();
        spellDispellLog.dispelledBySpellID = spellInfo.getId();

        for (var dispelableAura : successList) {
            var dispellData = new SpellDispellData();
            dispellData.spellID = dispelableAura.getAura().getId();
            dispellData.harmful = false; // TODO: use me

            unitTarget.removeAurasDueToSpellByDispel(dispelableAura.getAura().getId(), spellInfo.getId(), dispelableAura.getAura().getCasterGuid(), caster, dispelableAura.getDispelCharges());

            spellDispellLog.dispellData.add(dispellData);
        }

        caster.sendMessageToSet(spellDispellLog, true);

        callScriptSuccessfulDispel(effectInfo.effectIndex);
    }

    
    private void effectDualWield() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        unitTarget.setCanDualWield(true);

        if (unitTarget.isTypeId(TypeId.UNIT)) {
            unitTarget.toCreature().updateDamagePhysical(WeaponAttackType.OffAttack);
        }
    }

    
    private void effectDistract() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        // Check for possible target
        if (unitTarget == null || unitTarget.isEngaged()) {
            return;
        }

        // target must be OK to do this
        if (unitTarget.hasUnitState(UnitState.Confused.getValue() | UnitState.Stunned.getValue().getValue() | UnitState.Fleeing.getValue().getValue())) {
            return;
        }

        unitTarget.getMotionMaster().moveDistract((int) (Damage * time.InMilliseconds), unitTarget.getLocation().getAbsoluteAngle(destTarget));
    }

    
    private void effectPickPocket() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var creature = unitTarget == null ? null : unitTarget.toCreature();

        if (creature == null) {
            return;
        }

        if (creature.getCanGeneratePickPocketLoot()) {
            creature.startPickPocketRefillTimer();

            creature.setLoot(new loot(creature.getMap(), creature.getGUID(), LootType.PICKPOCKETING, null));
            var lootid = creature.getCreatureTemplate().pickPocketId;

            if (lootid != 0) {
                creature.getLoot().fillLoot(lootid, LootStorage.PICKPOCKETING, player, true);
            }

            // Generate extra money for pick pocket loot
            var a = RandomUtil.URand(0, creature.getLevel() / 2);
            var b = RandomUtil.URand(0, player.getLevel() / 2);
            creature.getLoot().gold = (int) (10 * (a + b) * WorldConfig.getFloatValue(WorldCfg.RateDropMoney));
        } else if (creature.getLoot() != null) {
            if (creature.getLoot().loot_type == LootType.PICKPOCKETING && creature.getLoot().isLooted()) {
                player.sendLootError(creature.getLoot().getGUID(), creature.getGUID(), LootError.AlreadPickPocketed);
            }

            return;
        }

        player.sendLoot(creature.getLoot());
    }

    
    private void effectAddFarsight() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var radius = effectInfo.calcRadius();
        var duration = spellInfo.calcDuration(caster);

        // Caster not in world, might be spell triggered from aura removal
        if (!player.isInWorld()) {
            return;
        }

        DynamicObject dynObj = new DynamicObject(true);

        if (!dynObj.createDynamicObject(player.getMap().generateLowGuid(HighGuid.DynamicObject), player, spellInfo, destTarget, radius, DynamicObjectType.FarsightFocus, spellVisual)) {
            return;
        }

        dynObj.setDuration(duration);
        dynObj.setCasterViewpoint();
    }

    
    private void effectUntrainTalents() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var guid = caster.getGUID();

        if (!guid.isEmpty()) // the trainer is the caster
        {
            unitTarget.toPlayer().sendRespecWipeConfirm(guid, unitTarget.toPlayer().getNextResetTalentsCost(), SpecResetType.talents);
        }
    }

    
    private void effectTeleUnitsFaceCaster() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        if (unitTarget.isInFlight()) {
            return;
        }

        if (targets.getHasDst()) {
            unitTarget.nearTeleportTo(destTarget.getX(), destTarget.getY(), destTarget.getZ(), destTarget.getAbsoluteAngle(caster.getLocation()), unitTarget == caster);
        }
    }

    
    private void effectLearnSkill() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        if (damage < 1) {
            return;
        }

        var skillid = (int) effectInfo.miscValue;
        var rcEntry = global.getDB2Mgr().GetSkillRaceClassInfo(skillid, unitTarget.getRace(), unitTarget.getClass());

        if (rcEntry == null) {
            return;
        }

        var tier = global.getObjectMgr().getSkillTier(rcEntry.SkillTierID);

        if (tier == null) {
            return;
        }

        var skillval = unitTarget.toPlayer().getPureSkillValue(SkillType.forValue(skillid));
        unitTarget.toPlayer().setSkill(skillid, (int) damage, Math.max(skillval, (short) 1), tier.Value[(int) Damage - 1]);
    }

    
    private void effectPlayMovie() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var movieId = (int) effectInfo.miscValue;

        if (!CliDB.MovieStorage.containsKey(movieId)) {
            return;
        }

        unitTarget.toPlayer().sendMovieStart(movieId);
    }

    
    private void effectTradeSkill() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (!caster.isTypeId(TypeId.PLAYER)) {
            return;
        }
        // uint skillid =  getEffect(i].miscValue;
        // ushort skillmax = unitTarget.ToPlayer().(skillid);
        // m_caster.ToPlayer().setSkill(skillid, skillval?skillval:1, skillmax+75);
    }

    
    private void effectEnchantItemPerm() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (itemTarget == null) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        // Handle vellums
        if (itemTarget.isVellum()) {
            // destroy one vellum from stack
            int count = 1;
            tangible.RefObject<Integer> tempRef_count = new tangible.RefObject<Integer>(count);
            player.destroyItemCount(itemTarget, tempRef_count, true);
            count = tempRef_count.refArgValue;
            unitTarget = player;
            // and add a scroll
            damage = 1;
            doCreateItem(effectInfo.itemType, spellInfo.hasAttribute(SpellAttr0.IsTradeskill) ? itemContext.TradeSkill : itemContext.NONE);
            itemTarget = null;
            targets.setItemTarget(null);
        } else {
            // do not increase skill if vellum used
            if (!(castItem && castItem.getTemplate().hasFlag(ItemFlags.NoReagentCost))) {
                player.updateCraftSkill(spellInfo);
            }

            var enchant_id = (int) effectInfo.miscValue;

            if (enchant_id == 0) {
                return;
            }

            var pEnchant = CliDB.SpellItemEnchantmentStorage.get(enchant_id);

            if (pEnchant == null) {
                return;
            }

            // item can be in trade slot and have owner diff. from caster
            var item_owner = itemTarget.getOwnerUnit();

            if (item_owner == null) {
                return;
            }

            if (item_owner != player && player.getSession().hasPermission(RBACPermissions.LogGmTrade)) {
                Log.outCommand(player.getSession().getAccountId(), "GM {0} (Account: {1}) enchanting(perm): {2} (Entry: {3}) for player: {4} (Account: {5})", player.getName(), player.getSession().getAccountId(), itemTarget.getTemplate().getName(), itemTarget.getEntry(), item_owner.getName(), item_owner.getSession().getAccountId());
            }

            // remove old enchanting before applying new if equipped
            item_owner.applyEnchantment(itemTarget, EnchantmentSlot.Perm, false);

            itemTarget.setEnchantment(EnchantmentSlot.Perm, enchant_id, 0, 0, caster.getGUID());

            // add new enchanting if equipped
            item_owner.applyEnchantment(itemTarget, EnchantmentSlot.Perm, true);

            item_owner.removeTradeableItem(itemTarget);
            itemTarget.clearSoulboundTradeable(item_owner);
        }
    }

    
    private void effectEnchantItemPrismatic() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (itemTarget == null) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var enchantId = (int) effectInfo.miscValue;

        if (enchantId == 0) {
            return;
        }

        var enchant = CliDB.SpellItemEnchantmentStorage.get(enchantId);

        if (enchant == null) {
            return;
        }

        {
            // support only enchantings with add socket in this slot
            var add_socket = false;

            for (byte i = 0; i < ItemConst.MaxItemEnchantmentEffects; ++i) {
                if (enchant.Effect[i] == ItemEnchantmentType.PrismaticSocket) {
                    add_socket = true;

                    break;
                }
            }

            if (!add_socket) {
                Log.outError(LogFilter.spells, "Spell.EffectEnchantItemPrismatic: attempt apply enchant spell {0} with SPELL_EFFECT_ENCHANT_ITEM_PRISMATIC ({1}) but without ITEM_ENCHANTMENT_TYPE_PRISMATIC_SOCKET ({2}), not suppoted yet.", spellInfo.getId(), SpellEffectName.EnchantItemPrismatic, ItemEnchantmentType.PrismaticSocket);

                return;
            }
        }

        // item can be in trade slot and have owner diff. from caster
        var item_owner = itemTarget.getOwnerUnit();

        if (item_owner == null) {
            return;
        }

        if (item_owner != player && player.getSession().hasPermission(RBACPermissions.LogGmTrade)) {
            Log.outCommand(player.getSession().getAccountId(), "GM {0} (Account: {1}) enchanting(perm): {2} (Entry: {3}) for player: {4} (Account: {5})", player.getName(), player.getSession().getAccountId(), itemTarget.getTemplate().getName(), itemTarget.getEntry(), item_owner.getName(), item_owner.getSession().getAccountId());
        }

        // remove old enchanting before applying new if equipped
        item_owner.applyEnchantment(itemTarget, EnchantmentSlot.Prismatic, false);

        itemTarget.setEnchantment(EnchantmentSlot.Prismatic, enchantId, 0, 0, caster.getGUID());

        // add new enchanting if equipped
        item_owner.applyEnchantment(itemTarget, EnchantmentSlot.Prismatic, true);

        item_owner.removeTradeableItem(itemTarget);
        itemTarget.clearSoulboundTradeable(item_owner);
    }

    
    private void effectEnchantItemTmp() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (itemTarget == null) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var enchant_id = (int) effectInfo.miscValue;

        if (enchant_id == 0) {
            Log.outError(LogFilter.spells, "Spell {0} Effect {1} (SPELL_EFFECT_ENCHANT_ITEM_TEMPORARY) have 0 as enchanting id", spellInfo.getId(), effectInfo.effectIndex);

            return;
        }

        var pEnchant = CliDB.SpellItemEnchantmentStorage.get(enchant_id);

        if (pEnchant == null) {
            Log.outError(LogFilter.spells, "Spell {0} Effect {1} (SPELL_EFFECT_ENCHANT_ITEM_TEMPORARY) have not existed enchanting id {2}", spellInfo.getId(), effectInfo.effectIndex, enchant_id);

            return;
        }

        // select enchantment duration
        var duration = (int) pEnchant.duration;

        // item can be in trade slot and have owner diff. from caster
        var item_owner = itemTarget.getOwnerUnit();

        if (item_owner == null) {
            return;
        }

        if (item_owner != player && player.getSession().hasPermission(RBACPermissions.LogGmTrade)) {
            Log.outCommand(player.getSession().getAccountId(), "GM {0} (Account: {1}) enchanting(temp): {2} (Entry: {3}) for player: {4} (Account: {5})", player.getName(), player.getSession().getAccountId(), itemTarget.getTemplate().getName(), itemTarget.getEntry(), item_owner.getName(), item_owner.getSession().getAccountId());
        }

        // remove old enchanting before applying new if equipped
        item_owner.applyEnchantment(itemTarget, EnchantmentSlot.Temp, false);

        itemTarget.setEnchantment(EnchantmentSlot.Temp, enchant_id, duration * 1000, 0, caster.getGUID());

        // add new enchanting if equipped
        item_owner.applyEnchantment(itemTarget, EnchantmentSlot.Temp, true);
    }

    
    private void effectTameCreature() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null || !unitCaster.getPetGUID().isEmpty()) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        if (!unitTarget.isTypeId(TypeId.UNIT)) {
            return;
        }

        var creatureTarget = unitTarget.toCreature();

        if (creatureTarget.isPet()) {
            return;
        }

        if (unitCaster.getClass() != playerClass.Hunter) {
            return;
        }

        // cast finish successfully
        finish();

        var pet = unitCaster.createTamedPetFrom(creatureTarget, spellInfo.getId());

        if (pet == null) // in very specific state like near world end/etc.
        {
            return;
        }

        // "kill" original creature
        creatureTarget.despawnOrUnsummon();

        var level = (creatureTarget.getLevelForTarget(caster) < (caster.getLevelForTarget(creatureTarget) - 5)) ? (caster.getLevelForTarget(creatureTarget) - 5) : creatureTarget.getLevelForTarget(caster);

        // prepare visual effect for levelup
        pet.setLevel(level - 1);

        // add to world
        pet.getMap().addToMap(pet.toCreature());

        // visual effect for levelup
        pet.setLevel(level);

        // caster have pet now
        unitCaster.setMinion(pet, true);

        if (caster.isTypeId(TypeId.PLAYER)) {
            pet.SavePetToDB(PetSaveMode.AsCurrent);
            unitCaster.toPlayer().petSpellInitialize();
        }
    }

    
    private void effectSummonPet() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        Player owner = null;

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster != null) {
            owner = unitCaster.toPlayer();

            if (owner == null && unitCaster.isTotem()) {
                owner = unitCaster.getCharmerOrOwnerPlayerOrPlayerItself();
            }
        }

        var petentry = (int) effectInfo.miscValue;

        if (owner == null) {
            var properties = CliDB.SummonPropertiesStorage.get(67);

            if (properties != null) {
                summonGuardian(effectInfo, petentry, properties, 1, ObjectGuid.Empty);
            }

            return;
        }

        var OldSummon = owner.getCurrentPet();

        // if pet requested type already exist
        if (OldSummon != null) {
            if (petentry == 0 || OldSummon.getEntry() == petentry) {
                // pet in corpse state can't be summoned
                if (OldSummon.isDead()) {
                    return;
                }

                var newPos = new Position();
                owner.getClosePoint(newPos, OldSummon.getCombatReach());
                newPos.setO(OldSummon.getLocation().getO());

                OldSummon.nearTeleportTo(newPos);

                if (owner.isTypeId(TypeId.PLAYER) && OldSummon.isControlled()) {
                    owner.toPlayer().petSpellInitialize();
                }

                return;
            }

            if (owner.isTypeId(TypeId.PLAYER)) {
                owner.toPlayer().removePet(OldSummon, PetSaveMode.NotInSlot, false);
            } else {
                return;
            }
        }

        PetSaveMode petSlot = null;

        if (petentry == 0) {
            petSlot = PetSaveMode.forValue(damage);
        }

        var combatPos = new Position();
        owner.getClosePoint(combatPos, owner.getCombatReach());
        combatPos.setO(owner.getLocation().getO());
        boolean isNew;
        tangible.OutObject<Boolean> tempOut_isNew = new tangible.OutObject<Boolean>();
        var pet = owner.summonPet(petentry, petSlot, combatPos, 0, tempOut_isNew);
        isNew = tempOut_isNew.outArgValue;

        if (pet == null) {
            return;
        }

        if (isNew) {
            if (caster.isCreature()) {
                if (caster.toCreature().isTotem()) {
                    pet.setReactState(ReactStates.Aggressive);
                } else {
                    pet.setReactState(ReactStates.Defensive);
                }
            }

            pet.setCreatedBySpell(spellInfo.getId());

            // generate new name for summon pet
            var new_name = global.getObjectMgr().generatePetName(petentry);

            if (!StringUtil.isEmpty(new_name)) {
                pet.setName(new_name);
            }
        }

        executeLogEffectSummonObject(effectInfo.effect, pet);
    }

    
    private void effectLearnPetSpell() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        if (unitTarget.toPlayer() != null) {
            effectLearnSpell();

            return;
        }

        var pet = unitTarget.getAsPet();

        if (pet == null) {
            return;
        }

        var learn_spellproto = global.getSpellMgr().getSpellInfo(effectInfo.triggerSpell, Difficulty.NONE);

        if (learn_spellproto == null) {
            return;
        }

        pet.learnSpell(learn_spellproto.getId());
        pet.SavePetToDB(PetSaveMode.AsCurrent);
        pet.getOwningPlayer().petSpellInitialize();
    }

    
    private void effectTaunt() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        // this effect use before aura Taunt apply for prevent taunt already attacking target
        // for spell as marked "non effective at already attacking target"
        if (!unitTarget || unitTarget.isTotem()) {
            sendCastResult(SpellCastResult.DontReport);

            return;
        }

        // Hand of Reckoning can hit some entities that can't have a threat list (including players' pets)
        if (spellInfo.getId() == 62124) {
            if (!unitTarget.isPlayer() && ObjectGuid.opNotEquals(unitTarget.getTarget(), unitCaster.getGUID())) {
                unitCaster.castSpell(unitTarget, 67485, true);
            }
        }

        if (!unitTarget.getCanHaveThreatList()) {
            sendCastResult(SpellCastResult.DontReport);

            return;
        }

        var mgr = unitTarget.getThreatManager();

        if (mgr.getCurrentVictim() == unitCaster) {
            sendCastResult(SpellCastResult.DontReport);

            return;
        }

        if (!mgr.isThreatListEmpty()) {
            // Set threat equal to highest threat currently on target
            mgr.matchUnitThreatToHighestThreat(unitCaster);
        }
    }

    
    private void effectWeaponDmg() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive()) {
            return;
        }

        // multiple weapon dmg effect workaround
        // execute only the last weapon damage
        // and handle all effects at once
        for (var j = effectInfo.effectIndex + 1; j < spellInfo.getEffects().size(); ++j) {
            switch (spellInfo.getEffect(j).effect) {
                case WeaponDamage:
                case WeaponDamageNoSchool:
                case NormalizedWeaponDmg:
                case WeaponPercentDamage:
                    return; // we must calculate only at last weapon effect
            }
        }

        // some spell specific modifiers
        double totalDamagePercentMod = 1.0f; // applied to final bonus+weapon damage
        double fixed_bonus = 0;
        double spell_bonus = 0; // bonus specific for spell

        switch (spellInfo.getSpellFamilyName()) {
            case Shaman: {
                // Skyshatter Harness item set bonus
                // Stormstrike
                var aurEff = unitCaster.isScriptOverriden(spellInfo, 5634);

                if (aurEff != null) {
                    unitCaster.castSpell((WorldObject) null, 38430, new CastSpellExtraArgs(aurEff));
                }

                break;
            }
        }

        var normalized = false;
        double weaponDamagePercentMod = 1.0f;

        for (var spellEffectInfo : spellInfo.getEffects()) {
            switch (spellEffectInfo.effect) {
                case WeaponDamage:
                case WeaponDamageNoSchool:
                    fixed_bonus += calculateDamage(spellEffectInfo, unitTarget);

                    break;
                case NormalizedWeaponDmg:
                    fixed_bonus += calculateDamage(spellEffectInfo, unitTarget);
                    normalized = true;

                    break;
                case WeaponPercentDamage:
                    tangible.RefObject<Double> tempRef_weaponDamagePercentMod = new tangible.RefObject<Double>(weaponDamagePercentMod);
                    MathUtil.ApplyPct(tempRef_weaponDamagePercentMod, calculateDamage(spellEffectInfo, unitTarget));
                    weaponDamagePercentMod = tempRef_weaponDamagePercentMod.refArgValue;

                    break;
                default:
                    break; // not weapon damage effect, just skip
            }
        }

        // if (addPctMods) { percent mods are added in Unit::CalculateDamage } else { percent mods are added in Unit::MeleeDamageBonusDone }
        // this distinction is neccessary to properly inform the client about his autoattack damage values from Script_UnitDamage
        var addPctMods = !spellInfo.hasAttribute(SpellAttr6.IgnoreCasterDamageModifiers) && spellSchoolMask.hasFlag(spellSchoolMask.NORMAL);

        if (addPctMods) {
            UnitMods unitMod;

            switch (attackType) {
                default:
                case BaseAttack:
                    unitMod = UnitMods.DamageMainHand;

                    break;
                case OffAttack:
                    unitMod = UnitMods.DamageOffHand;

                    break;
                case RangedAttack:
                    unitMod = UnitMods.DamageRanged;

                    break;
            }

            var weapon_total_pct = unitCaster.getPctModifierValue(unitMod, UnitModifierPctType.Total);

            if (fixed_bonus != 0) {
                fixed_bonus = fixed_bonus * weapon_total_pct;
            }

            if (spell_bonus != 0) {
                spell_bonus = spell_bonus * weapon_total_pct;
            }
        }

        var weaponDamage = unitCaster.calculateDamage(attackType, normalized, addPctMods);

        // Sequence is important
        for (var spellEffectInfo : spellInfo.getEffects()) {
            // We assume that a spell have at most one fixed_bonus
            // and at most one weaponDamagePercentMod
            switch (spellEffectInfo.effect) {
                case WeaponDamage:
                case WeaponDamageNoSchool:
                case NormalizedWeaponDmg:
                    weaponDamage += fixed_bonus;

                    break;
                case WeaponPercentDamage:
                    weaponDamage = weaponDamage * weaponDamagePercentMod;

                    break;
                default:
                    break; // not weapon damage effect, just skip
            }
        }

        weaponDamage += spell_bonus;
        weaponDamage = weaponDamage * totalDamagePercentMod;

        // prevent negative damage
        weaponDamage = Math.max(weaponDamage, 0);

        // Add melee damage bonuses (also check for negative)
        weaponDamage = unitCaster.meleeDamageBonusDone(unitTarget, weaponDamage, attackType, DamageEffectType.SpellDirect, spellInfo, effectInfo);
        damageInEffects += unitTarget.meleeDamageBonusTaken(unitCaster, weaponDamage, attackType, DamageEffectType.SpellDirect, spellInfo);
    }

    
    private void effectThreat() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null || !unitCaster.isAlive()) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        if (!unitTarget.getCanHaveThreatList()) {
            return;
        }

        unitTarget.getThreatManager().addThreat(unitCaster, damage, spellInfo, true);
    }

    
    private void effectHealMaxHealth() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive()) {
            return;
        }

        int addhealth;

        // damage == 0 - heal for caster max health
        if (damage == 0) {
            addhealth = (int) unitCaster.getMaxHealth();
        } else {
            addhealth = (int) (unitTarget.getMaxHealth() - unitTarget.getHealth());
        }

        healingInEffects += addhealth;
    }

    
    private void effectInterruptCast() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isAlive()) {
            return;
        }

        // @todo not all spells that used this effect apply cooldown at school spells
        // also exist case: apply cooldown to interrupted cast only and to all spells
        // there is no CURRENT_AUTOREPEAT_SPELL spells that can be interrupted
        for (var i = CurrentSpellType.generic; i.getValue() < CurrentSpellType.AutoRepeat.getValue(); ++i) {
            var spell = unitTarget.getCurrentSpell(i);

            if (spell != null) {
                var curSpellInfo = spell.spellInfo;

                // check if we can interrupt spell
                if ((spell.getState() == SpellState.Casting || (spell.getState() == SpellState.Preparing && spell.getCastTime() > 0.0f)) && curSpellInfo.canBeInterrupted(caster, unitTarget)) {
                    var duration = spellInfo.getDuration();
                    duration = unitTarget.modSpellDuration(spellInfo, unitTarget, duration, false, effectInfo.effectIndex);
                    unitTarget.getSpellHistory().lockSpellSchool(curSpellInfo.getSchoolMask(), duration.ofSeconds(duration));
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Interrupt.getValue());
                    sendSpellInterruptLog(unitTarget, curSpellInfo.getId());
                    var interuptedSpell = unitTarget.interruptSpell(i, false, true, spell);

                    if (interuptedSpell != null) {
                        this.<ISpellOnSucessfulInterrupt>ForEachSpellScript(s -> s.SucessfullyInterrupted(interuptedSpell));
                    }
                }
            }
        }
    }

    
    private void effectSummonObjectWild() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        WorldObject target = focusObject;

        if (target == null) {
            target = caster;
        }

        var pos = new Position();

        if (targets.getHasDst()) {
            pos = destTarget.Copy();
        } else {
            caster.getClosePoint(pos, SharedConst.DefaultPlayerBoundingRadius);
            pos.setO(target.getLocation().getO());
        }

        var map = target.getMap();

        var rotation = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(pos.getO(), 0.0f, 0.0f));
        var go = gameObject.createGameObject((int) effectInfo.miscValue, map, pos, rotation, 255, GOState.Ready);

        if (!go) {
            return;
        }

        PhasingHandler.inheritPhaseShift(go, caster);

        var duration = spellInfo.calcDuration(caster);

        go.setRespawnTime(duration > 0 ? duration / time.InMilliseconds : 0);
        go.setSpellId(spellInfo.getId());

        executeLogEffectSummonObject(effectInfo.effect, go);

        // Wild object not have owner and check clickable by players
        map.addToMap(go);

        if (go.getGoType() == GameObjectTypes.flagDrop) {
            var player = caster.toPlayer();

            if (player != null) {
                var bg = player.getBattleground();

                if (bg) {
                    bg.setDroppedFlagGUID(go.getGUID(), bg.getPlayerTeam(player.getGUID()) == Team.ALLIANCE ? TeamId.HORDE : TeamId.ALLIANCE);
                }
            }
        }

        var linkedTrap = go.getLinkedTrap();

        if (linkedTrap) {
            PhasingHandler.inheritPhaseShift(linkedTrap, caster);
            linkedTrap.setRespawnTime(duration > 0 ? duration / time.InMilliseconds : 0);
            linkedTrap.setSpellId(spellInfo.getId());

            executeLogEffectSummonObject(effectInfo.effect, linkedTrap);
        }
    }

    
    private void effectScriptEffect() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        // @todo we must implement hunter pet summon at login there (spell 6962)
        /** @todo: move this to scripts
         */
        switch (spellInfo.getSpellFamilyName()) {
            case Generic: {
                switch (spellInfo.getId()) {
                    case 45204: // Clone me!
                        caster.castSpell(unitTarget, (int) damage, new CastSpellExtraArgs(true));

                        break;
                    // Shadow Flame (All script effects, not just end ones to prevent player from dodging the last triggered spell)
                    case 22539:
                    case 22972:
                    case 22975:
                    case 22976:
                    case 22977:
                    case 22978:
                    case 22979:
                    case 22980:
                    case 22981:
                    case 22982:
                    case 22983:
                    case 22984:
                    case 22985: {
                        if (unitTarget == null || !unitTarget.isAlive()) {
                            return;
                        }

                        // Onyxia Scale Cloak
                        if (unitTarget.hasAura(22683)) {
                            return;
                        }

                        // Shadow Flame
                        caster.castSpell(unitTarget, 22682, new CastSpellExtraArgs(this));

                        return;
                    }
                    // Mug Transformation
                    case 41931: {
                        if (!caster.isTypeId(TypeId.PLAYER)) {
                            return;
                        }

                        byte bag = 19;
                        byte slot = 0;
                        Item item;

                        while (bag != 0) // 256 = 0 due to var type
                        {
                            item = caster.toPlayer().getItemByPos(bag, slot);

                            if (item != null && item.getEntry() == 38587) {
                                break;
                            }

                            ++slot;

                            if (slot == 39) {
                                slot = 0;
                                ++bag;
                            }
                        }

                        if (bag != 0) {
                            if (caster.toPlayer().getItemByPos(bag, slot).getCount() == 1) {
                                caster.toPlayer().removeItem(bag, slot, true);
                            } else {
                                caster.toPlayer().getItemByPos(bag, slot).setCount(caster.toPlayer().getItemByPos(bag, slot).getCount() - 1);
                            }

                            // Spell 42518 (Braufest - Gratisprobe des Braufest herstellen)
                            caster.castSpell(caster, 42518, new CastSpellExtraArgs(this));

                            return;
                        }

                        break;
                    }
                    // Brutallus - Burn
                    case 45141:
                    case 45151: {
                        //Workaround for Range ... should be global for every ScriptEffect
                        var radius = effectInfo.calcRadius();

                        if (unitTarget != null && unitTarget.isTypeId(TypeId.PLAYER) && unitTarget.getDistance(caster) >= radius && !unitTarget.hasAura(46394) && unitTarget != caster) {
                            unitTarget.castSpell(unitTarget, 46394, new CastSpellExtraArgs(this));
                        }

                        break;
                    }
                    // Emblazon Runeblade
                    case 51770: {
                        if (originalCaster == null) {
                            return;
                        }

                        originalCaster.castSpell(originalCaster, (int) damage, new CastSpellExtraArgs(false));

                        break;
                    }
                    // Summon Ghouls On Scarlet Crusade
                    case 51904: {
                        if (!targets.getHasDst()) {
                            return;
                        }

                        var radius = effectInfo.calcRadius();

                        for (byte i = 0; i < 15; ++i) {
                            caster.castSpell(caster.getRandomPoint(destTarget, radius), 54522, new CastSpellExtraArgs(this));
                        }

                        break;
                    }
                    case 52173: // Coyote Spirit Despawn
                    case 60243: // Blood Parrot Despawn
                        if (unitTarget.isTypeId(TypeId.UNIT) && unitTarget.toCreature().isSummon()) {
                            unitTarget.toTempSummon().unSummon();
                        }

                        return;
                    case 57347: // Retrieving (Wintergrasp RP-GG pickup spell)
                    {
                        if (unitTarget == null || !unitTarget.isTypeId(TypeId.UNIT) || !caster.isTypeId(TypeId.PLAYER)) {
                            return;
                        }

                        unitTarget.toCreature().despawnOrUnsummon();

                        return;
                    }
                    case 57349: // Drop RP-GG (Wintergrasp RP-GG at death drop spell)
                    {
                        if (!caster.isTypeId(TypeId.PLAYER)) {
                            return;
                        }

                        // Delete item from inventory at death
                        caster.toPlayer().destroyItemCount((int) damage, 5, true);

                        return;
                    }
                    case 58941: // Rock Shards
                        if (unitTarget != null && originalCaster != null) {
                            for (int i = 0; i < 3; ++i) {
                                originalCaster.castSpell(unitTarget, 58689, new CastSpellExtraArgs(true));
                                originalCaster.castSpell(unitTarget, 58692, new CastSpellExtraArgs(true));
                            }

                            if (originalCaster.getMap().getDifficultyID() == Difficulty.NONE) {
                                originalCaster.castSpell(unitTarget, 58695, new CastSpellExtraArgs(true));
                                originalCaster.castSpell(unitTarget, 58696, new CastSpellExtraArgs(true));
                            } else {
                                originalCaster.castSpell(unitTarget, 60883, new CastSpellExtraArgs(true));
                                originalCaster.castSpell(unitTarget, 60884, new CastSpellExtraArgs(true));
                            }
                        }

                        return;
                    case 62482: // Grab Crate
                    {
                        if (unitCaster == null) {
                            return;
                        }

                        if (unitTarget != null) {
                            var seat = unitCaster.getVehicleBase();

                            if (seat != null) {
                                var parent = seat.getVehicleBase();

                                if (parent != null) {
                                    // @todo a hack, range = 11, should after some time cast, otherwise too far
                                    unitCaster.castSpell(parent, 62496, new CastSpellExtraArgs(this));
                                    unitTarget.castSpell(parent, (int) damage, (new CastSpellExtraArgs()).setTriggeringSpell(this)); // DIFFICULTY_NONE, so effect always valid
                                }
                            }
                        }

                        return;
                    }
                }

                break;
            }
        }

        // normal DB scripted effect
        Log.outDebug(LogFilter.spells, "Spell ScriptStart spellid {0} in effectScriptEffect({1})", spellInfo.getId(), effectInfo.effectIndex);
        caster.getMap().scriptsStart(ScriptsType.spell, (int) ((int) spellInfo.getId() | (int) (effectInfo.effectIndex << 24)), caster, unitTarget);
    }

    
    private void effectSanctuary() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        if (unitTarget.isPlayer() && !unitTarget.getMap().isDungeon()) {
            // stop all pve combat for players outside dungeons, suppress pvp combat
            unitTarget.combatStop(false, false);
        } else {
            // in dungeons (or for nonplayers), reset this unit on all enemies' threat lists
            for (var pair : unitTarget.getThreatManager().getThreatenedByMeList().entrySet()) {
                pair.getValue().scaleThreat(0.0f);
            }
        }

        // makes spells cast before this time fizzle
        unitTarget.setLastSanctuaryTime(gameTime.GetGameTimeMS());
    }

    
    private void effectDuel() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !caster.isTypeId(TypeId.PLAYER) || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var caster = caster.toPlayer();
        var target = unitTarget.toPlayer();

        // caster or target already have requested duel
        if (caster.getDuel() != null || target.getDuel() != null || target.getSocial() == null || target.getSocial().hasIgnore(caster.getGUID(), caster.getSession().getAccountGUID())) {
            return;
        }

        // Players can only fight a duel in zones with this flag
        var casterAreaEntry = CliDB.AreaTableStorage.get(caster.getArea());

        if (casterAreaEntry != null && !casterAreaEntry.hasFlag(AreaFlags.AllowDuels)) {
            sendCastResult(SpellCastResult.NoDueling); // Dueling isn't allowed here

            return;
        }

        var targetAreaEntry = CliDB.AreaTableStorage.get(target.getAreaId());

        if (targetAreaEntry != null && !targetAreaEntry.hasFlag(AreaFlags.AllowDuels)) {
            sendCastResult(SpellCastResult.NoDueling); // Dueling isn't allowed here

            return;
        }

        //CREATE DUEL FLAG OBJECT
        var map = caster.getMap();

        class AnonymousType {
            public float X;
            public float Y;
            public float Z;
            public float orientation;

            public AnonymousType(float _X, float _Y, float _Z, float _Orientation) {
                X = _X;
                Y = _Y;
                Z = _Z;
                orientation = _Orientation;
            }
        }
        Position pos = new AnonymousType(caster.getLocation().getX() + (unitTarget.getLocation().getX() - caster.getLocation().getX()) / 2, caster.getLocation().getY() + (unitTarget.getLocation().getY() - caster.getLocation().getY()) / 2, caster.getLocation().getZ(), caster.getLocation().getO());

        var rotation = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(pos.orientation, 0.0f, 0.0f));

        var go = gameObject.createGameObject((int) effectInfo.miscValue, map, pos, rotation, 0, GOState.Ready);

        if (!go) {
            return;
        }

        PhasingHandler.inheritPhaseShift(go, caster);

        go.setFaction(caster.getFaction());
        go.setLevel(caster.getLevel() + 1);
        var duration = spellInfo.calcDuration(caster);
        go.setRespawnTime(duration > 0 ? duration / time.InMilliseconds : 0);
        go.setSpellId(spellInfo.getId());

        executeLogEffectSummonObject(effectInfo.effect, go);

        caster.addGameObject(go);
        map.addToMap(go);
        //END

        // Send request
        DuelRequested packet = new DuelRequested();
        packet.arbiterGUID = go.getGUID();
        packet.requestedByGUID = caster.getGUID();
        packet.requestedByWowAccount = caster.getSession().getAccountGUID();

        caster.sendPacket(packet);
        target.sendPacket(packet);

        // create duel-info
        var isMounted = (spellInfo.getId() == 62875);
        caster.setDuel(new DuelInfo(target, caster, isMounted));
        target.setDuel(new DuelInfo(caster, caster, isMounted));

        caster.setDuelArbiter(go.getGUID());
        target.setDuelArbiter(go.getGUID());

        global.getScriptMgr().<IPlayerOnDuelRequest>ForEach(p -> p.OnDuelRequest(target, caster));
    }

    
    private void effectStuck() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (!WorldConfig.getBoolValue(WorldCfg.CastUnstuck)) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        Log.outDebug(LogFilter.spells, "Spell Effect: Stuck");
        Log.outInfo(LogFilter.spells, "Player {0} (guid {1}) used auto-unstuck future at map {2} ({3}, {4}, {5})", player.getName(), player.getGUID().toString(), player.getLocation().getMapId(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());

        if (player.isInFlight()) {
            return;
        }

        // if player is dead without death timer is teleported to graveyard, otherwise not apply the effect
        if (player.isDead()) {
            if (player.getDeathTimer() == 0) {
                player.repopAtGraveyard();
            }

            return;
        }

        // the player dies if hearthstone is in cooldown, else the player is teleported to home
        if (player.getSpellHistory().hasCooldown(8690)) {
            player.killSelf();

            return;
        }

        player.teleportTo(player.getHomeBind(), TeleportToOptions.spell);

        // Stuck spell trigger Hearthstone cooldown
        var spellInfo = global.getSpellMgr().getSpellInfo(8690, getCastDifficulty());

        if (spellInfo == null) {
            return;
        }

        Spell spell = new spell(player, spellInfo, TriggerCastFlag.FullMask);
        spell.sendSpellCooldown();
    }

    
    private void effectSummonPlayer() {
        // workaround - this effect should not use target map
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        unitTarget.toPlayer().sendSummonRequestFrom(unitCaster);
    }

    
    private void effectActivateObject() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (gameObjTarget == null) {
            return;
        }

        gameObjTarget.activateObject(GameObjectActions.forValue(effectInfo.miscValue), effectInfo.miscValueB, caster, spellInfo.getId(), effectInfo.effectIndex);
    }

    
    private void effectApplyGlyph() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var glyphs = player.getGlyphs(player.getActiveTalentGroup());
        var replacedGlyph = glyphs.size();

        for (var i = 0; i < glyphs.size(); ++i) {
            var activeGlyphBindableSpells = global.getDB2Mgr().GetGlyphBindableSpells(glyphs.get(i));

            if (activeGlyphBindableSpells.contains(spellMisc.spellId)) {
                replacedGlyph = i;
                player.removeAura(CliDB.GlyphPropertiesStorage.get(glyphs.get(i)).spellID);

                break;
            }
        }

        var glyphId = (int) effectInfo.miscValue;

        if (replacedGlyph < glyphs.size()) {
            if (glyphId != 0) {
                glyphs.set(replacedGlyph, glyphId);
            } else {
                glyphs.remove(replacedGlyph);
            }
        } else if (glyphId != 0) {
            glyphs.add(glyphId);
        }

        player.removeAurasWithInterruptFlags(SpellAuraInterruptFlags2.ChangeGlyph);

        var glyphProperties = CliDB.GlyphPropertiesStorage.get(glyphId);

        if (glyphProperties != null) {
            player.castSpell(player, glyphProperties.spellID, new CastSpellExtraArgs(this));
        }

        ActiveGlyphs activeGlyphs = new ActiveGlyphs();
        activeGlyphs.glyphs.add(new GlyphBinding(spellMisc.spellId, (short) glyphId));
        activeGlyphs.isFullUpdate = false;
        player.sendPacket(activeGlyphs);
    }

    
    private void effectEnchantHeldItem() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        // this is only item spell effect applied to main-hand weapon of target player (players in area)
        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var item_owner = unitTarget.toPlayer();
        var item = item_owner.getItemByPos(InventorySlots.Bag0, EquipmentSlot.MainHand);

        if (item == null) {
            return;
        }

        // must be equipped
        if (!item.isEquipped()) {
            return;
        }

        if (effectInfo.miscValue != 0) {
            var enchant_id = (int) effectInfo.miscValue;
            var duration = spellInfo.getDuration(); //Try duration index first ..

            if (duration == 0) {
                duration = (int) damage; //+1;            //Base points after ..
            }

            if (duration == 0) {
                duration = 10 * time.InMilliseconds; //10 seconds for enchants which don't have listed duration
            }

            if (spellInfo.getId() == 14792) // Venomhide Poison
            {
                duration = 5 * time.Minute * time.InMilliseconds;
            }

            var pEnchant = CliDB.SpellItemEnchantmentStorage.get(enchant_id);

            if (pEnchant == null) {
                return;
            }

            // Always go to temp enchantment slot
            var slot = EnchantmentSlot.Temp;

            // Enchantment will not be applied if a different one already exists
            if (item.getEnchantmentId(slot) != 0 && item.getEnchantmentId(slot) != enchant_id) {
                return;
            }

            // Apply the temporary enchantment
            item.setEnchantment(slot, enchant_id, (int) duration, 0, caster.getGUID());
            item_owner.applyEnchantment(item, slot, true);
        }
    }

    
    private void effectDisEnchant() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var caster = caster.toPlayer();

        if (caster != null) {
            caster.updateCraftSkill(spellInfo);
            itemTarget.setLoot(new loot(caster.getMap(), itemTarget.getGUID(), LootType.Disenchanting, null));
            itemTarget.getLoot().fillLoot(itemTarget.getDisenchantLoot(caster).id, LootStorage.DISENCHANT, caster, true);
            caster.sendLoot(itemTarget.getLoot());
        }

        // item will be removed at disenchanting end
    }

    
    private void effectInebriate() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();
        var currentDrunk = player.getDrunkValue();
        var drunkMod = damage;

        if (currentDrunk + drunkMod > 100) {
            currentDrunk = 100;

            if (RandomUtil.randChance() < 25.0f) {
                player.castSpell(player, 67468, (new CastSpellExtraArgs()).setTriggeringSpell(this)); // Drunken Vomit
            }
        } else {
            currentDrunk += (byte) drunkMod;
        }

        player.setDrunkValue(currentDrunk, castItem != null ? castItem.getEntry() : 0);
    }

    
    private void effectFeedPet() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var foodItem = itemTarget;

        if (foodItem == null) {
            return;
        }

        var pet = player.getCurrentPet();

        if (pet == null) {
            return;
        }

        if (!pet.isAlive()) {
            return;
        }

        executeLogEffectDestroyItem(effectInfo.effect, foodItem.getEntry());

        int pct;
        var levelDiff = (int) pet.getLevel() - (int) foodItem.getTemplate().getBaseItemLevel();

        if (levelDiff >= 30) {
            return;
        } else if (levelDiff >= 20) {
            pct = (int) 12.5; // we can't pass double so keeping the cast here for future references
        } else if (levelDiff >= 10) {
            pct = 25;
        } else {
            pct = 50;
        }

        int count = 1;
        tangible.RefObject<Integer> tempRef_count = new tangible.RefObject<Integer>(count);
        player.destroyItemCount(foodItem, tempRef_count, true);
        count = tempRef_count.refArgValue;
        // @todo fix crash when a spell has two effects, both pointed at the same item target

        CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlag.FullMask);
        args.setTriggeringSpell(this);
        args.addSpellMod(SpellValueMod.BasePoint0, pct);
        caster.castSpell(pet, effectInfo.triggerSpell, args);
    }

    
    private void effectDismissPet() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isPet()) {
            return;
        }

        var pet = unitTarget.getAsPet();

        executeLogEffectUnsummonObject(effectInfo.effect, pet);
        pet.remove(PetSaveMode.NotInSlot);
    }

    
    private void effectSummonObject() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        var slot = (byte) (effectInfo.Effect - SpellEffectName.SummonObjectSlot1);
        var guid = unitCaster.getObjectSlot()[slot];

        if (!guid.isEmpty()) {
            var obj = unitCaster.getMap().getGameObject(guid);

            if (obj != null) {
                // Recast case - null spell id to make auras not be removed on object remove from world
                if (spellInfo.getId() == obj.getSpellId()) {
                    obj.setSpellId(0);
                }

                unitCaster.removeGameObject(obj, true);
            }

            unitCaster.getObjectSlot()[slot].clear();
        }

        var pos = new Position();

        // If dest location if present
        if (targets.getHasDst()) {
            pos = destTarget.Copy();
        }
        // Summon in random point all other units if location present
        else {
            unitCaster.getClosePoint(pos, SharedConst.DefaultPlayerBoundingRadius);
            pos.setO(unitCaster.getLocation().getO());
        }

        var map = caster.getMap();
        var rotation = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(pos.getO(), 0.0f, 0.0f));
        var go = gameObject.createGameObject((int) effectInfo.miscValue, map, pos, rotation, 255, GOState.Ready);

        if (!go) {
            return;
        }

        PhasingHandler.inheritPhaseShift(go, caster);

        go.setFaction(unitCaster.getFaction());
        go.setLevel(unitCaster.getLevel());
        var duration = spellInfo.calcDuration(caster);
        go.setRespawnTime(duration > 0 ? duration / time.InMilliseconds : 0);
        go.setSpellId(spellInfo.getId());
        unitCaster.addGameObject(go);

        executeLogEffectSummonObject(effectInfo.effect, go);

        map.addToMap(go);

        unitCaster.getObjectSlot()[slot] = go.getGUID();
    }

    
    private void effectResurrect() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (corpseTarget == null && unitTarget == null) {
            return;
        }

        Player player = null;

        if (corpseTarget) {
            player = global.getObjAccessor().findPlayer(corpseTarget.getOwnerGUID());
        } else if (unitTarget) {
            player = unitTarget.toPlayer();
        }

        if (player == null || player.isAlive() || !player.isInWorld()) {
            return;
        }

        if (player.isResurrectRequested()) // already have one active request
        {
            return;
        }

        var health = (int) player.countPctFromMaxHealth(damage);
        var mana = (int) MathUtil.CalculatePct(player.getMaxPower(powerType.mana), damage);

        executeLogEffectResurrect(effectInfo.effect, player);

        player.setResurrectRequestData(caster, health, mana, 0);
        sendResurrectRequest(player);
    }

    
    private void effectAddExtraAttacks() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isAlive()) {
            return;
        }

        unitTarget.addExtraAttacks((int) damage);

        executeLogEffectExtraAttacks(effectInfo.effect, unitTarget, (int) damage);
    }

    
    private void effectParry() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (caster.isTypeId(TypeId.PLAYER)) {
            caster.toPlayer().setCanParry(true);
        }
    }

    
    private void effectBlock() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (caster.isTypeId(TypeId.PLAYER)) {
            caster.toPlayer().setCanBlock(true);
        }
    }

    
    private void effectLeap() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || unitTarget.isInFlight()) {
            return;
        }

        if (!targets.getHasDst()) {
            return;
        }

        unitTarget.nearTeleportTo(destTarget.getX(), destTarget.getY(), destTarget.getZ(), destTarget.getO(), unitTarget == caster);
    }

    
    private void effectReputation() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();

        var repChange = (int) damage;

        var factionId = effectInfo.miscValue;

        var factionEntry = CliDB.FactionStorage.get(factionId);

        if (factionEntry == null) {
            return;
        }

        repChange = player.calculateReputationGain(ReputationSource.spell, 0, repChange, factionId);

        player.getReputationMgr().modifyReputation(factionEntry, repChange);
    }

    
    private void effectQuestComplete() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();

        var questId = (int) effectInfo.miscValue;

        if (questId != 0) {
            var quest = global.getObjectMgr().getQuestTemplate(questId);

            if (quest == null) {
                return;
            }

            var logSlot = player.findQuestSlot(questId);

            if (logSlot < SharedConst.MaxQuestLogSize) {
                player.areaExploredOrEventHappens(questId);
            } else if (quest.hasFlag(QuestFlag.Tracking)) // Check if the quest is used as a serverside flag.
            {
                player.setRewardedQuest(questId); // If so, set status to rewarded without broadcasting it to client.
            }
        }
    }

    
    private void effectForceDeselect() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        var dist = caster.getVisibilityRange();

        // clear focus
        PacketSenderOwning<BreakTarget> breakTarget = new PacketSenderOwning<BreakTarget>();
        breakTarget.getData().unitGUID = caster.getGUID();
        breakTarget.getData().write();

        var notifierBreak = new MessageDistDelivererToHostile<PacketSenderOwning<BreakTarget>>(unitCaster, breakTarget, dist, gridType.World);
        Cell.visitGrid(caster, notifierBreak, dist);

        // and selection
        PacketSenderOwning<ClearTarget> clearTarget = new PacketSenderOwning<ClearTarget>();
        clearTarget.getData().guid = caster.getGUID();
        clearTarget.getData().write();
        var notifierClear = new MessageDistDelivererToHostile<PacketSenderOwning<ClearTarget>>(unitCaster, clearTarget, dist, gridType.World);
        Cell.visitGrid(caster, notifierClear, dist);

        // we should also force pets to remove us from current target
        ArrayList<Unit> attackerSet = new ArrayList<>();

        for (var unit : unitCaster.getAttackers()) {
            if (unit.getObjectTypeId() == TypeId.UNIT && !unit.getCanHaveThreatList()) {
                attackerSet.add(unit);
            }
        }

        for (var unit : attackerSet) {
            unit.attackStop();
        }
    }

    
    private void effectSelfResurrect() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null || !player.isInWorld() || player.isAlive()) {
            return;
        }

        int health;
        var mana = 0;

        // flat case
        if (damage < 0) {
            health = (int) -Damage;
            mana = effectInfo.miscValue;
        }
        // percent case
        else {
            health = (int) player.countPctFromMaxHealth(damage);

            if (player.getMaxPower(powerType.mana) > 0) {
                mana = MathUtil.CalculatePct(player.getMaxPower(powerType.mana), damage);
            }
        }

        player.resurrectPlayer(0.0f);

        player.setHealth(health);
        player.setPower(powerType.mana, mana);
        player.setPower(powerType.Rage, 0);
        player.setFullPower(powerType.Energy);
        player.setPower(powerType.Focus, 0);

        player.spawnCorpseBones();
    }

    
    private void effectSkinning() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget.isTypeId(TypeId.UNIT)) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var creature = unitTarget.toCreature();
        var targetLevel = (int) creature.getLevelForTarget(caster);

        var skill = creature.getCreatureTemplate().getRequiredLootSkill();

        creature.setUnitFlag3(unitFlags3.AlreadySkinned);
        creature.setDynamicFlag(UnitDynFlags.Lootable);
        Loot loot = new loot(creature.getMap(), creature.getGUID(), LootType.SKINNING, null);

        if (loot != null) {
            creature.getPersonalLoot().put(player.getGUID(), loot);
        }
        loot.fillLoot(creature.getCreatureTemplate().skinLootId, LootStorage.SKINNING, player, true);
        player.sendLoot(loot);

        if (skill == SkillType.SKINNING) {
            int reqValue;

            if (targetLevel <= 10) {
                reqValue = 1;
            } else if (targetLevel < 16) {
                reqValue = (targetLevel - 10) * 10; // 60-110
            } else if (targetLevel <= 23) {
                reqValue = (int) (targetLevel * 4.8); // 110 - 185
            } else if (targetLevel < 39) {
                reqValue = targetLevel * 10 - 205; // 185-225
            } else if (targetLevel <= 44) {
                reqValue = targetLevel * 5 + 5; // 225-260
            } else if (targetLevel <= 52) {
                reqValue = targetLevel * 5; // 260-300
            } else {
                reqValue = 300;
            }

            // TODO: Specialize skillid for each expansion
            // new db field?
            // tied to one of existing expansion fields in creature_template?

            // Double chances for elites
            caster.toPlayer().updateGatherSkill(skill, (int) damage, (int) reqValue, (int) (creature.isElite() ? 2 : 1));
        }
    }

    
    private void effectCharge() {
        if (unitTarget == null) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (effectHandleMode == SpellEffectHandleMode.LaunchTarget) {
            // charge changes fall time
            if (unitCaster.isPlayer()) {
                unitCaster.toPlayer().setFallInformation(0, caster.getLocation().getZ());
            }

            var speed = MathUtil.fuzzyGt(spellInfo.getSpeed(), 0.0f) ? spellInfo.getSpeed() : MotionMaster.SPEED_CHARGE;
            SpellEffectExtraData spellEffectExtraData = null;

            if (effectInfo.miscValueB != 0) {
                spellEffectExtraData = new spellEffectExtraData();
                spellEffectExtraData.target = unitTarget.getGUID();
                spellEffectExtraData.spellVisualId = (int) effectInfo.miscValueB;
            }

            // Spell is not using explicit target - no generated path
            if (preGeneratedPath == null) {
                var pos = unitTarget.getFirstCollisionPosition(unitTarget.getCombatReach(), unitTarget.getLocation().getRelativeAngle(caster.getLocation()));

                if (MathUtil.fuzzyGt(spellInfo.getSpeed(), 0.0f) && spellInfo.hasAttribute(SpellAttr9.SpecialDelayCalculation)) {
                    speed = pos.getExactDist(caster.getLocation()) / speed;
                }

                unitCaster.getMotionMaster().moveCharge(pos.getX(), pos.getY(), pos.getZ(), speed, eventId.charge, false, unitTarget, spellEffectExtraData);
            } else {
                if (MathUtil.fuzzyGt(spellInfo.getSpeed(), 0.0f) && spellInfo.hasAttribute(SpellAttr9.SpecialDelayCalculation)) {
                    var pos = preGeneratedPath.getActualEndPosition();
                    speed = (new Position(pos.X, pos.Y, pos.Z)).getExactDist(caster.getLocation()) / speed;
                }

                unitCaster.getMotionMaster().moveCharge(preGeneratedPath, speed, unitTarget, spellEffectExtraData);
            }
        }

        if (effectHandleMode == SpellEffectHandleMode.HitTarget) {
            // not all charge effects used in negative spells
            if (!spellInfo.isPositive() && caster.isTypeId(TypeId.PLAYER)) {
                unitCaster.attack(unitTarget, true);
            }

            if (effectInfo.triggerSpell != 0) {
                caster.castSpell(unitTarget, effectInfo.triggerSpell, (new CastSpellExtraArgs(TriggerCastFlag.FullMask)).setOriginalCaster(originalCasterGuid).setTriggeringSpell(this));
            }
        }
    }

    
    private void effectChargeDest() {
        if (destTarget == null) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (effectHandleMode == SpellEffectHandleMode.Launch) {
            var pos = destTarget.Copy();

            if (!unitCaster.isWithinLOS(pos)) {
                var angle = unitCaster.getLocation().getRelativeAngle(pos.getX(), pos.getY());
                var dist = unitCaster.getDistance(pos);
                pos = unitCaster.getFirstCollisionPosition(dist, angle);
            }

            unitCaster.getMotionMaster().moveCharge(pos.getX(), pos.getY(), pos.getZ());
        } else if (effectHandleMode == SpellEffectHandleMode.hit) {
            if (effectInfo.triggerSpell != 0) {
                caster.castSpell(destTarget, effectInfo.triggerSpell, (new CastSpellExtraArgs(TriggerCastFlag.FullMask)).setOriginalCaster(originalCasterGuid).setTriggeringSpell(this));
            }
        }
    }

    
    private void effectKnockBack() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget) {
            return;
        }

        if (caster.getAffectingPlayer()) {
            var creatureTarget = unitTarget.toCreature();

            if (creatureTarget != null) {
                if (creatureTarget.isWorldBoss() || creatureTarget.isDungeonBoss()) {
                    return;
                }
            }
        }

        // Spells with SPELL_EFFECT_KNOCK_BACK (like thunderstorm) can't knockback target if target has ROOT/STUN
        if (unitTarget.hasUnitState(UnitState.Root.getValue() | UnitState.Stunned.getValue())) {
            return;
        }

        // Instantly interrupt non melee spells being casted
        if (unitTarget.isNonMeleeSpellCast(true)) {
            unitTarget.interruptNonMeleeSpells(true);
        }

        var ratio = 0.1f;
        var speedxy = effectInfo.MiscValue * ratio;
        var speedz = Damage * ratio;

        if (speedxy < 0.01f && speedz < 0.01f) {
            return;
        }

        Position origin;

        if (effectInfo.effect == SpellEffectName.KnockBackDest) {
            if (targets.getHasDst()) {
                origin = destTarget.Copy();
            } else {
                return;
            }
        } else //if (effectInfo.effect == SPELL_EFFECT_KNOCK_BACK)
        {
            origin = new Position(caster.getLocation());
        }

        unitTarget.knockbackFrom(origin, speedxy, (float) speedz);

        unit.procSkillsAndAuras(getUnitCasterForEffectHandlers(), unitTarget, new ProcFlagsInit(procFlags.NONE), new ProcFlagsInit(procFlags.NONE, ProcFlags2.Knockback), ProcFlagsSpellType.MaskAll, ProcFlagsSpellPhase.hit, ProcFlagsHit.NONE, null, null, null);
    }

    
    private void effectLeapBack() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        var speedxy = effectInfo.MiscValue / 10.0f;
        var speedz = Damage / 10.0f;
        // Disengage
        unitTarget.jumpTo(speedxy, (float) speedz, effectInfo.positionFacing);

        // changes fall time
        if (caster.getObjectTypeId() == TypeId.PLAYER) {
            caster.toPlayer().setFallInformation(0, caster.getLocation().getZ());
        }
    }

    
    private void effectQuestClear() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();

        var quest_id = (int) effectInfo.miscValue;

        var quest = global.getObjectMgr().getQuestTemplate(quest_id);

        if (quest == null) {
            return;
        }

        var oldStatus = player.getQuestStatus(quest_id);

        // Player has never done this quest
        if (oldStatus == QuestStatus.NONE) {
            return;
        }

        // remove all quest entries for 'entry' from quest log
        for (byte slot = 0; slot < SharedConst.MaxQuestLogSize; ++slot) {
            var logQuest = player.getQuestSlotQuestId(slot);

            if (logQuest == quest_id) {
                player.setQuestSlot(slot, 0);

                // we ignore unequippable quest items in this case, it's still be equipped
                player.takeQuestSourceItem(logQuest, false);

                if (quest.hasFlag(QuestFlag.Pvp)) {
                    player.pvpInfo.isHostile = player.pvpInfo.isInHostileArea || player.hasPvPForcingQuest();
                    player.updatePvPState();
                }
            }
        }

        player.removeActiveQuest(quest_id, false);
        player.removeRewardedQuest(quest_id);

        global.getScriptMgr().<IPlayerOnQuestStatusChange>ForEach(p -> p.OnQuestStatusChange(player, quest_id));
        global.getScriptMgr().<IQuestOnQuestStatusChange>RunScript(script -> script.OnQuestStatusChange(player, quest, oldStatus, QuestStatus.NONE), quest.getScriptId());
    }

    
    private void effectSendTaxi() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        unitTarget.toPlayer().activateTaxiPathTo((int) effectInfo.miscValue, spellInfo.getId());
    }

    
    private void effectPullTowards() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget) {
            return;
        }

        var pos = caster.getFirstCollisionPosition(caster.getCombatReach(), caster.getLocation().getRelativeAngle(unitTarget.getLocation()));

        // This is a blizzlike mistake: this should be 2D distance according to projectile motion formulas, but Blizzard erroneously used 3D distance.
        var distXY = unitTarget.getLocation().getExactDist(pos);

        // Avoid division by 0
        if (distXY < 0.001) {
            return;
        }

        var distZ = pos.getZ() - unitTarget.getLocation().getZ();
        var speedXY = effectInfo.miscValue != 0 ? effectInfo.MiscValue / 10.0f : 30.0f;
        var speedZ = (float) ((2 * speedXY * speedXY * distZ + MotionMaster.GRAVITY * distXY * distXY) / (2 * speedXY * distXY));

        if (!Float.IsFinite(speedZ)) {
            Log.outError(LogFilter.spells, String.format("Spell %1$s with SPELL_EFFECT_PULL_TOWARDS called with invalid speedZ. %2$s", spellInfo.getId(), getDebugInfo()));

            return;
        }

        unitTarget.jumpTo(speedXY, speedZ, 0.0f, pos);
    }

    
    private void effectPullTowardsDest() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget) {
            return;
        }

        if (!targets.getHasDst()) {
            Log.outError(LogFilter.spells, String.format("Spell %1$s with SPELL_EFFECT_PULL_TOWARDS_DEST has no dest target", spellInfo.getId()));

            return;
        }

        Position pos = targets.getDstPos();
        // This is a blizzlike mistake: this should be 2D distance according to projectile motion formulas, but Blizzard erroneously used 3D distance
        var distXY = unitTarget.getLocation().getExactDist(pos);

        // Avoid division by 0
        if (distXY < 0.001) {
            return;
        }

        var distZ = pos.getZ() - unitTarget.getLocation().getZ();

        var speedXY = effectInfo.miscValue != 0 ? effectInfo.MiscValue / 10.0f : 30.0f;
        var speedZ = (float) ((2 * speedXY * speedXY * distZ + MotionMaster.GRAVITY * distXY * distXY) / (2 * speedXY * distXY));

        if (!Float.IsFinite(speedZ)) {
            Log.outError(LogFilter.spells, String.format("Spell %1$s with SPELL_EFFECT_PULL_TOWARDS_DEST called with invalid speedZ. %2$s", spellInfo.getId(), getDebugInfo()));

            return;
        }

        unitTarget.jumpTo(speedXY, speedZ, 0.0f, pos);
    }

    
    private void effectChangeRaidMarker() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var player = caster.toPlayer();

        if (!player || !targets.getHasDst()) {
            return;
        }

        var group = player.getGroup();

        if (!group || (group.isRaidGroup() && !group.isLeader(player.getGUID()) && !group.isAssistant(player.getGUID()))) {
            return;
        }

        group.addRaidMarker((byte) damage, player.getLocation().getMapId(), destTarget.getX(), destTarget.getY(), destTarget.getZ());
    }

    
    private void effectDispelMechanic() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        var mechanic = effectInfo.miscValue;

        ArrayList<java.util.Map.entry<Integer, ObjectGuid>> dispel_list = new ArrayList<java.util.Map.entry<Integer, ObjectGuid>>();

        for (var aura : unitTarget.getOwnedAurasList()) {
            if (aura.getApplicationOfTarget(unitTarget.getGUID()) == null) {
                continue;
            }

            if (RandomUtil.randChance(aura.calcDispelChance(unitTarget, !unitTarget.isFriendlyTo(caster)))) {
                if ((aura.getSpellInfo().getAllEffectsMechanicMask() & (1 << mechanic)) != 0) {
                    dispel_list.add(new KeyValuePair<Integer, ObjectGuid>(aura.getId(), aura.getCasterGuid()));
                }
            }
        }

        while (!dispel_list.isEmpty()) {
            unitTarget.removeAura(dispel_list.get(0).getKey(), dispel_list.get(0).getValue(), AuraRemoveMode.EnemySpell);
            dispel_list.remove(0);
        }
    }

    
    private void effectResurrectPet() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (damage < 0) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        // Maybe player dismissed dead pet or pet despawned?
        var hadPet = true;

        if (player.getCurrentPet() == null) {
            var petStable = player.getPetStable1();
            var deadPetIndex = Array.FindIndex(petStable.ActivePets, petInfo ->
            {
                if (petInfo != null) {
                    petInfo.health;
                }
            } == 0);

            var slot = PetSaveMode.forValue(deadPetIndex);

            player.summonPet(0, slot, new Position(), 0);
            hadPet = false;
        }

        // TODO: Better to fail Hunter's "Revive Pet" at cast instead of here when casting ends
        var pet = player.getCurrentPet(); // Attempt to get current pet

        if (pet == null || pet.isAlive()) {
            return;
        }

        // If player did have a pet before reviving, teleport it
        if (hadPet) {
            // Reposition the pet's corpse before reviving so as not to grab aggro
            // We can use a different, more accurate version of getClosePoint() since we have a pet
            // Will be used later to reposition the pet if we have one
            var closePoint = new Position();
            player.getClosePoint(closePoint, pet.getCombatReach(), SharedConst.PetFollowDist, pet.getFollowAngle());
            closePoint.setO(player.getLocation().getO());
            pet.nearTeleportTo(closePoint);
            pet.getLocation().relocate(closePoint); // This is needed so saveStayPosition() will get the proper coords.
        }

        pet.replaceAllDynamicFlags(UnitDynFlags.NONE);
        pet.removeUnitFlag(UnitFlag.Skinnable);
        pet.setDeathState(deathState.Alive);
        pet.clearUnitState(UnitState.AllErasable);
        pet.setHealth(pet.countPctFromMaxHealth(damage));

        // Reset things for when the AI to takes over
        var ci = pet.getCharmInfo();

        if (ci != null) {
            // In case the pet was at stay, we don't want it running back
            ci.saveStayPosition();
            ci.setIsAtStay(ci.hasCommandState(CommandStates.Stay));

            ci.setIsFollowing(false);
            ci.setIsCommandAttack(false);
            ci.setIsCommandFollow(false);
            ci.setIsReturning(false);
        }

        pet.SavePetToDB(PetSaveMode.AsCurrent);
    }

    
    private void effectDestroyAllTotems() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        var mana = 0;

        for (byte slot = summonSlot.totem.getValue(); slot < SharedConst.MaxTotemSlot; ++slot) {
            if (unitCaster.getSummonSlot()[slot].isEmpty()) {
                continue;
            }

            var totem = unitCaster.getMap().getCreature(unitCaster.getSummonSlot()[slot]);

            if (totem != null && totem.isTotem()) {
                int spell_id = totem.getUnitData().createdBySpell;
                var spellInfo = global.getSpellMgr().getSpellInfo(spell_id, getCastDifficulty());

                if (spellInfo != null) {
                    var costs = spellInfo.calcPowerCost(unitCaster, spellInfo.getSchoolMask());
                    var m = tangible.ListHelper.find(costs, cost -> cost.power == powerType.mana);

                    if (m != null) {
                        mana += m.amount;
                    }
                }

                totem.toTotem().unSummon();
            }
        }

        tangible.RefObject<Integer> tempRef_mana = new tangible.RefObject<Integer>(mana);
        MathUtil.ApplyPct(tempRef_mana, damage);
        mana = tempRef_mana.refArgValue;

        if (mana != 0) {
            CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlag.FullMask);
            args.setTriggeringSpell(this);
            args.addSpellMod(SpellValueMod.BasePoint0, mana);
            unitCaster.castSpell(caster, 39104, args);
        }
    }

    
    private void effectDurabilityDamage() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var slot = effectInfo.miscValue;

        // -1 means all player equipped items and -2 all items
        if (slot < 0) {
            unitTarget.toPlayer().durabilityPointsLossAll(damage, (slot < -1));
            executeLogEffectDurabilityDamage(effectInfo.effect, unitTarget, -1, -1);

            return;
        }

        // invalid slot second
        if (slot >= InventorySlots.BagEnd) {
            return;
        }

        var item = unitTarget.toPlayer().getItemByPos(InventorySlots.Bag0, (byte) slot);

        if (item != null) {
            unitTarget.toPlayer().durabilityPointsLoss(item, damage);
            executeLogEffectDurabilityDamage(effectInfo.effect, unitTarget, (int) item.getEntry(), slot);
        }
    }

    
    private void effectDurabilityDamagePCT() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var slot = effectInfo.miscValue;

        // FIXME: some spells effects have second -1/-2
        // Possibly its mean -1 all player equipped items and -2 all items
        if (slot < 0) {
            unitTarget.toPlayer().durabilityLossAll(Damage / 100.0f, (slot < -1));

            return;
        }

        // invalid slot second
        if (slot >= InventorySlots.BagEnd) {
            return;
        }

        if (damage <= 0) {
            return;
        }

        var item = unitTarget.toPlayer().getItemByPos(InventorySlots.Bag0, (byte) slot);

        if (item != null) {
            unitTarget.toPlayer().durabilityLoss(item, Damage / 100.0f);
        }
    }

    
    private void effectModifyThreatPercent() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null || unitTarget == null) {
            return;
        }

        unitTarget.getThreatManager().modifyThreatByPercent(unitCaster, damage);
    }

    
    private void effectTransmitted() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        var name_id = (int) effectInfo.miscValue;

        var overrideSummonedGameObjects = unitCaster.getAuraEffectsByType(AuraType.OverrideSummonedObject);

        for (var aurEff : overrideSummonedGameObjects) {
            if (aurEff.getMiscValue() == name_id) {
                name_id = (int) aurEff.getMiscValueB();

                break;
            }
        }

        var goinfo = global.getObjectMgr().getGameObjectTemplate(name_id);

        if (goinfo == null) {
            Logs.SQL.error("Gameobject (Entry: {0}) not exist and not created at spell (ID: {1}) cast", name_id, spellInfo.getId());

            return;
        }

        var pos = new Position();

        if (targets.getHasDst()) {
            pos = destTarget.Copy();
        }
        //FIXME: this can be better check for most objects but still hack
        else if (effectInfo.getHasRadius() && spellInfo.getSpeed() == 0) {
            var dis = effectInfo.calcRadius(unitCaster);
            unitCaster.getClosePoint(pos, SharedConst.DefaultPlayerBoundingRadius, dis);
            pos.setO(unitCaster.getLocation().getO());
        } else {
            //GO is always friendly to it's creator, get range for friends
            var min_dis = spellInfo.getMinRange(true);
            var max_dis = spellInfo.getMaxRange(true);
            var dis = (float) RandomUtil.randomFloat() * (max_dis - min_dis) + min_dis;

            unitCaster.getClosePoint(pos, SharedConst.DefaultPlayerBoundingRadius, dis);
            pos.setO(unitCaster.getLocation().getO());
        }

        var cMap = unitCaster.getMap();

        // if gameobject is summoning object, it should be spawned right on caster's position
        if (goinfo.type == GameObjectTypes.ritual) {
            pos.relocate(unitCaster.getLocation());
        }

        var rotation = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(pos.getO(), 0.0f, 0.0f));

        var go = gameObject.createGameObject(name_id, cMap, pos, rotation, 255, GOState.Ready);

        if (!go) {
            return;
        }

        PhasingHandler.inheritPhaseShift(go, caster);

        var duration = spellInfo.calcDuration(caster);

        switch (goinfo.type) {
            case FishingNode: {
                go.setFaction(unitCaster.getFaction());
                var bobberGuid = go.getGUID();
                // client requires fishing bobber guid in channel object slot 0 to be usable
                unitCaster.setChannelObject(0, bobberGuid);
                unitCaster.addGameObject(go); // will removed at spell cancel

                // end time of range when possible catch fish (FISHING_BOBBER_READY_TIME..getDuration(m_spellInfo))
                // start time == fish-FISHING_BOBBER_READY_TIME (0..getDuration(m_spellInfo)-FISHING_BOBBER_READY_TIME)
                var lastSec = 0;

                switch (RandomUtil.IRand(0, 2)) {
                    case 0:
                        lastSec = 3;

                        break;
                    case 1:
                        lastSec = 7;

                        break;
                    case 2:
                        lastSec = 13;

                        break;
                }

                // Duration of the fishing bobber can't be higher than the Fishing channeling duration
                duration = Math.min(duration, duration - lastSec * time.InMilliseconds + 5 * time.InMilliseconds);

                break;
            }
            case Ritual: {
                if (unitCaster.isPlayer()) {
                    go.addUniqueUse(unitCaster.toPlayer());
                    unitCaster.addGameObject(go); // will be removed at spell cancel
                }

                break;
            }
            case DuelArbiter: // 52991
                unitCaster.addGameObject(go);

                break;
            case FishingHole:
            case Chest:
            default:
                break;
        }

        go.setRespawnTime(duration > 0 ? duration / time.InMilliseconds : 0);
        go.setOwnerGUID(unitCaster.getGUID());
        go.setSpellId(spellInfo.getId());

        executeLogEffectSummonObject(effectInfo.effect, go);

        Log.outDebug(LogFilter.spells, "AddObject at SpellEfects.cpp EffectTransmitted");

        cMap.addToMap(go);
        var linkedTrap = go.getLinkedTrap();

        if (linkedTrap != null) {
            PhasingHandler.inheritPhaseShift(linkedTrap, caster);
            linkedTrap.setRespawnTime(duration > 0 ? duration / time.InMilliseconds : 0);
            linkedTrap.setSpellId(spellInfo.getId());
            linkedTrap.setOwnerGUID(unitCaster.getGUID());

            executeLogEffectSummonObject(effectInfo.effect, linkedTrap);
        }
    }

    
    private void effectProspecting() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        if (itemTarget == null || !itemTarget.getTemplate().hasFlag(ItemFlags.IsProspectable)) {
            return;
        }

        if (itemTarget.getCount() < 5) {
            return;
        }

        if (WorldConfig.getBoolValue(WorldCfg.SkillProspecting)) {
            int SkillValue = player.getPureSkillValue(SkillType.Jewelcrafting);
            var reqSkillValue = itemTarget.getTemplate().getRequiredSkillRank();
            player.updateGatherSkill(SkillType.Jewelcrafting, SkillValue, reqSkillValue);
        }

        itemTarget.setLoot(new loot(player.getMap(), itemTarget.getGUID(), LootType.PROSPECTING, null));
        itemTarget.getLoot().fillLoot(itemTarget.getEntry(), LootStorage.PROSPECTING, player, true);
        player.sendLoot(itemTarget.getLoot());
    }

    
    private void effectMilling() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        if (itemTarget == null || !itemTarget.getTemplate().hasFlag(ItemFlags.IsMillable)) {
            return;
        }

        if (itemTarget.getCount() < 5) {
            return;
        }

        if (WorldConfig.getBoolValue(WorldCfg.SkillMilling)) {
            int SkillValue = player.getPureSkillValue(SkillType.Inscription);
            var reqSkillValue = itemTarget.getTemplate().getRequiredSkillRank();
            player.updateGatherSkill(SkillType.Inscription, SkillValue, reqSkillValue);
        }

        itemTarget.setLoot(new loot(player.getMap(), itemTarget.getGUID(), LootType.MILLING, null));
        itemTarget.getLoot().fillLoot(itemTarget.getEntry(), LootStorage.MILLING, player, true);
        player.sendLoot(itemTarget.getLoot());
    }

    
    private void effectSkill() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        Log.outDebug(LogFilter.spells, "WORLD: SkillEFFECT");
    }

    /* There is currently no need for this effect. We handle it in Battleground.cpp
		If we would handle the resurrection here, the spiritguide would instantly disappear as the
		player revives, and so we wouldn't see the spirit heal visual effect on the npc.
		This is why we use a half sec delay between the visual effect and the resurrection itself */
    private void effectSpiritHeal() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }
    }

    // remove insignia spell effect

    private void effectSkinPlayerCorpse() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        Log.outDebug(LogFilter.spells, "Effect: SkinPlayerCorpse");

        var player = caster.toPlayer();
        Player target = null;

        if (unitTarget != null) {
            target = unitTarget.toPlayer();
        } else if (corpseTarget != null) {
            target = global.getObjAccessor().findPlayer(corpseTarget.getOwnerGUID());
        }

        if (player == null || target == null || target.isAlive()) {
            return;
        }

        target.removedInsignia(player);
    }

    
    private void effectStealBeneficialBuff() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        Log.outDebug(LogFilter.spells, "Effect: StealBeneficialBuff");

        if (unitTarget == null || unitTarget == caster) // can't steal from self
        {
            return;
        }

        ArrayList<DispelableAura> stealList = new ArrayList<>();

        // Create dispel mask by dispel type
        var dispelMask = spellInfo.getDispelMask(DispelType.forValue(effectInfo.miscValue));

        for (var aura : unitTarget.getOwnedAurasList()) {
            var aurApp = aura.getApplicationOfTarget(unitTarget.getGUID());

            if (aurApp == null) {
                continue;
            }

            if ((boolean) (aura.getSpellInfo().getDispelMask() & dispelMask)) {
                // Need check for passive? this
                if (!aurApp.isPositive() || aura.isPassive() || aura.getSpellInfo().hasAttribute(SpellAttr4.CannotBeStolen)) {
                    continue;
                }

                // 2.4.3 Patch Notes: "Dispel effects will no longer attempt to remove effects that have 100% dispel resistance."
                var chance = aura.calcDispelChance(unitTarget, !unitTarget.isFriendlyTo(caster));

                if (chance == 0) {
                    continue;
                }

                // The charges / stack amounts don't count towards the total number of auras that can be dispelled.
                // Ie: A dispel on a target with 5 stacks of Winters Chill and a Polymorph has 1 / (1 + 1) . 50% chance to dispell
                // Polymorph instead of 1 / (5 + 1) . 16%.
                var dispelCharges = aura.getSpellInfo().hasAttribute(SpellAttr7.DispelCharges);
                var charges = dispelCharges ? aura.getCharges() : aura.getStackAmount();

                if (charges > 0) {
                    stealList.add(new DispelableAura(aura, chance, charges));
                }
            }
        }

        if (stealList.isEmpty()) {
            return;
        }

        var remaining = stealList.size();

        // Ok if exist some buffs for dispel try dispel it
        ArrayList<Tuple<Integer, ObjectGuid, Integer>> successList = new ArrayList<Tuple<Integer, ObjectGuid, Integer>>();

        DispelFailed dispelFailed = new DispelFailed();
        dispelFailed.casterGUID = caster.getGUID();
        dispelFailed.victimGUID = unitTarget.getGUID();
        dispelFailed.spellID = spellInfo.getId();

        // dispel N = damage buffs (or while exist buffs for dispel)
        for (var count = 0; count < damage && remaining > 0; ) {
            // Random select buff for dispel
            var dispelableAura = stealList.get(RandomUtil.IRand(0, remaining - 1));

            if (dispelableAura.rollDispel()) {
                byte stolenCharges = 1;

                if (dispelableAura.getAura().getSpellInfo().hasAttribute(SpellAttr1.DispelAllStacks)) {
                    stolenCharges = dispelableAura.getDispelCharges();
                }

                successList.add(Tuple.create(dispelableAura.getAura().getId(), dispelableAura.getAura().getCasterGuid(), (int) stolenCharges));

                if (!dispelableAura.decrementCharge(stolenCharges)) {
                    --remaining;
                    stealList.set(remaining, dispelableAura);
                }
            } else {
                dispelFailed.failedSpells.add(dispelableAura.getAura().getId());
            }

            ++count;
        }

        if (!dispelFailed.failedSpells.isEmpty()) {
            caster.sendMessageToSet(dispelFailed, true);
        }

        if (successList.isEmpty()) {
            return;
        }

        SpellDispellLog spellDispellLog = new SpellDispellLog();
        spellDispellLog.isBreak = false; // TODO: use me
        spellDispellLog.isSteal = true;

        spellDispellLog.targetGUID = unitTarget.getGUID();
        spellDispellLog.casterGUID = caster.getGUID();
        spellDispellLog.dispelledBySpellID = spellInfo.getId();


        for (var(spellId, auraCaster, stolenCharges) : successList) {
            var dispellData = new SpellDispellData();
            dispellData.spellID = spellId;
            dispellData.harmful = false; // TODO: use me

            unitTarget.removeAurasDueToSpellBySteal(spellId, auraCaster, caster, stolenCharges);

            spellDispellLog.dispellData.add(dispellData);
        }

        caster.sendMessageToSet(spellDispellLog, true);
    }

    
    private void effectKillCreditPersonal() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        unitTarget.toPlayer().killedMonsterCredit((int) effectInfo.miscValue);
    }

    
    private void effectKillCredit() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var creatureEntry = effectInfo.miscValue;

        if (creatureEntry != 0) {
            unitTarget.toPlayer().rewardPlayerAndGroupAtEvent((int) creatureEntry, unitTarget);
        }
    }

    
    private void effectQuestFail() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        unitTarget.toPlayer().failQuest((int) effectInfo.miscValue);
    }

    
    private void effectQuestStart() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget) {
            return;
        }

        var player = unitTarget.toPlayer();

        if (!player) {
            return;
        }

        var quest = global.getObjectMgr().getQuestTemplate((int) effectInfo.miscValue);

        if (quest != null) {
            if (!player.canTakeQuest(quest, false)) {
                return;
            }

            if (quest.isAutoAccept() && player.canAddQuest(quest, false)) {
                player.addQuestAndCheckCompletion(quest, null);
                player.getPlayerTalkClass().sendQuestGiverQuestDetails(quest, player.getGUID(), true, true);
            } else {
                player.getPlayerTalkClass().sendQuestGiverQuestDetails(quest, player.getGUID(), true, false);
            }
        }
    }

    
    private void effectCreateTamedPet() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER) || !unitTarget.getPetGUID().isEmpty() || unitTarget.getClass() != playerClass.Hunter) {
            return;
        }

        var creatureEntry = (int) effectInfo.miscValue;
        var pet = unitTarget.createTamedPetFrom(creatureEntry, spellInfo.getId());

        if (pet == null) {
            return;
        }

        // relocate
        var pos = new Position();
        unitTarget.getClosePoint(pos, pet.getCombatReach(), SharedConst.PetFollowDist, pet.getFollowAngle());
        pos.setO(unitTarget.getLocation().getO());
        pet.getLocation().relocate(pos);

        // add to world
        pet.getMap().addToMap(pet.toCreature());

        // unitTarget has pet now
        unitTarget.setMinion(pet, true);

        if (unitTarget.isTypeId(TypeId.PLAYER)) {
            pet.SavePetToDB(PetSaveMode.AsCurrent);
            unitTarget.toPlayer().petSpellInitialize();
        }
    }

    
    private void effectDiscoverTaxi() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var nodeid = (int) effectInfo.miscValue;

        if (CliDB.TaxiNodesStorage.containsKey(nodeid)) {
            unitTarget.toPlayer().getSession().sendDiscoverNewTaxiNode(nodeid);
        }
    }

    
    private void effectTitanGrip() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (caster.isTypeId(TypeId.PLAYER)) {
            caster.toPlayer().setCanTitanGrip(true, (int) effectInfo.miscValue);
        }
    }

    
    private void effectRedirectThreat() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (unitTarget != null) {
            unitCaster.getThreatManager().registerRedirectThreat(spellInfo.getId(), unitTarget.getGUID(), (int) damage);
        }
    }

    
    private void effectGameObjectDamage() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (gameObjTarget == null) {
            return;
        }

        var casterFaction = caster.getFactionTemplateEntry();
        var targetFaction = CliDB.FactionTemplateStorage.get(gameObjTarget.getFaction());

        // Do not allow to damage GO's of friendly factions (ie: Wintergrasp Walls/Ulduar Storm Beacons)
        if (targetFaction == null || (casterFaction != null && !casterFaction.isFriendlyTo(targetFaction))) {
            gameObjTarget.modifyHealth(-Damage, caster, spellInfo.getId());
        }
    }

    
    private void effectGameObjectRepair() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (gameObjTarget == null) {
            return;
        }

        gameObjTarget.modifyHealth(damage, caster);
    }

    
    private void effectGameObjectSetDestructionState() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (gameObjTarget == null) {
            return;
        }

        gameObjTarget.setDestructibleState(GameObjectDestructibleState.forValue(effectInfo.miscValue), caster, true);
    }

    private void summonGuardian(SpellEffectInfo effect, int entry, SummonPropertiesRecord properties, int numGuardians, ObjectGuid privateObjectOwner) {
        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (unitCaster.isTotem()) {
            unitCaster = unitCaster.toTotem().getOwnerUnit();
        }

        // in another case summon new
        var radius = 5.0f;
        var duration = spellInfo.calcDuration(originalCaster);

        //TempSummonType summonType = (duration == 0) ? TempSummonType.DeadDespawn : TempSummonType.TimedDespawn;
        var map = unitCaster.getMap();

        for (int count = 0; count < numGuardians; ++count) {
            Position pos;

            if (count == 0) {
                pos = destTarget;
            } else {
                // randomize position for multiple summons
                pos = unitCaster.getRandomPoint(destTarget, radius);
            }

            var summon = map.summonCreature(entry, pos, properties, (int) duration, unitCaster, spellInfo.getId(), 0, privateObjectOwner);

            if (summon == null) {
                return;
            }

            if (summon.hasUnitTypeMask(UnitTypeMask.Guardian)) {
                var level = summon.getLevel();

                if (properties != null && !properties.getFlags().hasFlag(SummonPropertiesFlags.UseCreatureLevel)) {
                    level = unitCaster.getLevel();
                }

                // level of pet summoned using engineering item based at engineering skill level
                if (castItem && unitCaster.isPlayer()) {
                    var proto = castItem.getTemplate();

                    if (proto != null) {
                        if (proto.getRequiredSkill() == (int) SkillType.Engineering.getValue()) {
                            var skill202 = unitCaster.toPlayer().getSkillValue(SkillType.Engineering);

                            if (skill202 != 0) {
                                level = skill202 / 5;
                            }
                        }
                    }
                }

                ((Guardian) summon).initStatsForLevel(level);
            }

            if (summon.hasUnitTypeMask(UnitTypeMask.minion) && targets.getHasDst()) {
                ((minion) summon).setFollowAngle(unitCaster.getLocation().getAbsoluteAngle(summon.getLocation()));
            }

            if (summon.getEntry() == 27893) {
                var weapon = caster.toPlayer().getPlayerData().visibleItems.get(EquipmentSlot.MainHand);

                if (weapon.itemID != 0) {
                    summon.setDisplayId(11686);
                    summon.setVirtualItem(0, weapon.itemID, weapon.itemAppearanceModID, weapon.itemVisual);
                } else {
                    summon.setDisplayId(1126);
                }
            }

            executeLogEffectSummonObject(effect.effect, summon);
        }
    }

    
    private void effectRenamePet() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.UNIT) || !unitTarget.isPet() || unitTarget.getAsPet().getPetType() != PetType.Hunter) {
            return;
        }

        unitTarget.setPetFlag(UnitPetFlags.CanBeRenamed);
    }

    
    private void effectPlayMusic() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var soundid = (int) effectInfo.miscValue;

        if (!CliDB.SoundKitStorage.containsKey(soundid)) {
            Log.outError(LogFilter.spells, "EffectPlayMusic: Sound (Id: {0}) not exist in spell {1}.", soundid, spellInfo.getId());

            return;
        }

        unitTarget.toPlayer().sendPacket(new PlayMusic(soundid));
    }

    
    private void effectActivateSpec() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();
        var specID = spellMisc.specializationId;
        var spec = CliDB.ChrSpecializationStorage.get(specID);

        // Safety checks done in Spell::CheckCast
        if (!spec.IsPetSpecialization()) {
            player.activateTalentGroup(spec);
        } else {
            player.getCurrentPet().SetSpecialization(specID);
        }
    }

    
    private void effectPlaySound() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget) {
            return;
        }

        var player = unitTarget.toPlayer();

        if (!player) {
            return;
        }

        switch (spellInfo.getId()) {
            case 91604: // Restricted Flight Area
                player.getSession().sendNotification(SysMessage.ZoneNoflyzone);

                break;
            default:
                break;
        }

        var soundId = (int) effectInfo.miscValue;

        if (!CliDB.SoundKitStorage.containsKey(soundId)) {
            Log.outError(LogFilter.spells, "EffectPlaySound: Sound (Id: {0}) not exist in spell {1}.", soundId, spellInfo.getId());

            return;
        }

        player.playDirectSound(soundId, player);
    }

    
    private void effectRemoveAura() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        // there may be need of specifying casterguid of removed auras
        unitTarget.removeAura(effectInfo.triggerSpell);
    }

    
    private void effectDamageFromMaxHealthPCT() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        damageInEffects += (int) unitTarget.countPctFromMaxHealth(damage);
    }

    
    private void effectGiveCurrency() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        if (!CliDB.CurrencyTypesStorage.containsKey(effectInfo.miscValue)) {
            return;
        }

        unitTarget.toPlayer().modifyCurrency((int) effectInfo.miscValue, (int) damage, CurrencyGainSource.spell, CurrencyDestroyReason.spell);
    }

    
    private void effectCastButtons() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var player = caster.toPlayer();

        if (player == null) {
            return;
        }

        var button_id = effectInfo.miscValue + 132;
        var n_buttons = effectInfo.miscValueB;

        for (; n_buttons != 0; --n_buttons, ++button_id) {
            var ab = player.getActionButton((byte) button_id);

            if (ab == null || ab.getButtonType() != ActionButtonType.spell) {
                continue;
            }

            //! Action button data is unverified when it's set so it can be "hacked"
            //! to contain invalid spells, so filter here.
            var spell_id = (int) ab.getAction();

            if (spell_id == 0) {
                continue;
            }

            var spellInfo = global.getSpellMgr().getSpellInfo(spell_id, getCastDifficulty());

            if (spellInfo == null) {
                continue;
            }

            if (!player.hasSpell(spell_id) || player.getSpellHistory().hasCooldown(spell_id)) {
                continue;
            }

            if (!spellInfo.hasAttribute(SpellAttr9.SummonPlayerTotem)) {
                continue;
            }

            CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlag.IgnoreGCD.getValue() | TriggerCastFlag.IgnoreCastInProgress.getValue().getValue() | TriggerCastFlag.CastDirectly.getValue().getValue().getValue() | TriggerCastFlag.DontReportCastError.getValue().getValue().getValue());
            args.originalCastId = castId;
            args.castDifficulty = getCastDifficulty();
            caster.castSpell(caster, spellInfo.getId(), args);
        }
    }

    
    private void effectRechargeItem() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null) {
            return;
        }

        var player = unitTarget.toPlayer();

        if (player == null) {
            return;
        }

        var item = player.getItemByEntry(effectInfo.itemType);

        if (item != null) {
            for (var itemEffect : item.getEffects()) {
                if (itemEffect.LegacySlotIndex <= item.getItemData().spellCharges.getSize()) {
                    item.setSpellCharges(itemEffect.LegacySlotIndex, itemEffect.charges);
                }
            }

            item.setState(ItemUpdateState.changed, player);
        }
    }

    
    private void effectBind() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();

        WorldLocation homeLoc = new worldLocation();
        var areaId = player.getAreaId();

        if (effectInfo.miscValue != 0) {
            areaId = (int) effectInfo.miscValue;
        }

        if (targets.getHasDst()) {
            homeLoc.WorldRelocate(destTarget);
        } else {
            homeLoc.relocate(player.getLocation());
            homeLoc.setMapId(player.getLocation().getMapId());
        }

        player.setHomebind(homeLoc, areaId);
        player.sendBindPointUpdate();

        Log.outDebug(LogFilter.spells, String.format("EffectBind: New homebind: %1$s, AreaId: %2$s", homeLoc, areaId));

        // zone update
        player.sendPlayerBound(caster.getGUID(), areaId);
    }

    
    private void effectTeleportToReturnPoint() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var player = unitTarget.toPlayer();

        if (player != null) {
            var dest = player.getStoredAuraTeleportLocation((int) effectInfo.miscValue);

            if (dest != null) {
                player.teleportTo(dest, unitTarget == _caster ? TeleportToOptions.spell.getValue() | TeleportToOptions.NotLeaveCombat.getValue() : 0);
            }
        }
    }

    
    private void effectIncreaseCurrencyCap() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (damage <= 0) {
            return;
        }

        if (unitTarget.toPlayer() != null) {
            unitTarget.toPlayer().increaseCurrencyCap((int) effectInfo.miscValue, (int) damage);
        }
    }

    
    private void effectSummonRaFFriend() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!caster.isTypeId(TypeId.PLAYER) || unitTarget == null || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        caster.castSpell(unitTarget, effectInfo.triggerSpell, new CastSpellExtraArgs(this));
    }

    
    private void effectUnlockGuildVaultTab() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        // Safety checks done in spell.CheckCast
        var caster = caster.toPlayer();
        var guild = caster.getGuild();

        if (guild != null) {
            guild.handleBuyBankTab(caster.getSession(), (byte) (Damage - 1)); // Bank tabs start at zero internally
        }
    }

    
    private void effectSummonPersonalGameObject() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var goId = (int) effectInfo.miscValue;

        if (goId == 0) {
            return;
        }

        Position pos = new Position();

        if (targets.getHasDst()) {
            pos = destTarget.Copy();
        } else {
            caster.getClosePoint(pos, SharedConst.DefaultPlayerBoundingRadius);
            pos.setO(caster.getLocation().getO());
        }

        var map = caster.getMap();
        var rot = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(pos.getO(), 0.0f, 0.0f));
        var go = gameObject.createGameObject(goId, map, pos, rot, 255, GOState.Ready);

        if (!go) {
            Log.outWarn(LogFilter.spells, String.format("SpellEffect Failed to summon personal gameobject. SpellId %1$s, effect %2$s", spellInfo.getId(), effectInfo.effectIndex));

            return;
        }

        PhasingHandler.inheritPhaseShift(go, caster);

        var duration = spellInfo.calcDuration(caster);

        go.setRespawnTime(duration > 0 ? duration / time.InMilliseconds : 0);
        go.setSpellId(spellInfo.getId());
        go.setPrivateObjectOwner(caster.getGUID());

        executeLogEffectSummonObject(effectInfo.effect, go);

        map.addToMap(go);

        var linkedTrap = go.getLinkedTrap();

        if (linkedTrap != null) {
            PhasingHandler.inheritPhaseShift(linkedTrap, caster);

            linkedTrap.setRespawnTime(duration > 0 ? duration / time.InMilliseconds : 0);
            linkedTrap.setSpellId(spellInfo.getId());

            executeLogEffectSummonObject(effectInfo.effect, linkedTrap);
        }
    }

    
    private void effectResurrectWithAura() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isInWorld()) {
            return;
        }

        var target = unitTarget.toPlayer();

        if (target == null) {
            return;
        }

        if (unitTarget.isAlive()) {
            return;
        }

        if (target.isResurrectRequested()) // already have one active request
        {
            return;
        }

        var health = (int) target.countPctFromMaxHealth(damage);
        var mana = (int) MathUtil.CalculatePct(target.getMaxPower(powerType.mana), damage);
        int resurrectAura = 0;

        if (global.getSpellMgr().hasSpellInfo(effectInfo.triggerSpell, Difficulty.NONE)) {
            resurrectAura = effectInfo.triggerSpell;
        }

        if (resurrectAura != 0 && target.hasAura(resurrectAura)) {
            return;
        }

        executeLogEffectResurrect(effectInfo.effect, target);
        target.setResurrectRequestData(caster, health, mana, resurrectAura);
        sendResurrectRequest(target);
    }

    
    private void effectCreateAreaTrigger() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null || !targets.getHasDst()) {
            return;
        }

        var duration = spellInfo.calcDuration(getCaster());
        areaTrigger.createAreaTrigger((int) effectInfo.miscValue, unitCaster, null, spellInfo, destTarget, duration, spellVisual, castId);
    }

    
    private void effectRemoveTalent() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var talent = CliDB.TalentStorage.get(spellMisc.talentId);

        if (talent == null) {
            return;
        }

        var player = UnitTarget ? unitTarget.toPlayer() : null;

        if (player == null) {
            return;
        }

        player.removeTalent(talent);
        player.sendTalentsInfoData();
    }

    
    private void effectDestroyItem() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var player = unitTarget.toPlayer();
        var item = player.getItemByEntry(effectInfo.itemType);

        if (item) {
            player.destroyItem(item.getBagSlot(), item.getSlot(), true);
        }
    }

    
    private void effectLearnGarrisonBuilding() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var garrison = unitTarget.toPlayer().getGarrison();

        if (garrison != null) {
            garrison.learnBlueprint((int) effectInfo.miscValue);
        }
    }

    
    private void effectRemoveAuraBySpellLabel() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget) {
            return;
        }

        unitTarget.getAppliedAurasQuery().hasLabel(new integer(effectInfo.miscValue)).execute(UnitTarget::RemoveAura);
    }

    
    private void effectCreateGarrison() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        unitTarget.toPlayer().createGarrison((int) effectInfo.miscValue);
    }

    
    private void effectCreateConversation() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null || !targets.getHasDst()) {
            return;
        }

        conversation.CreateConversation((int) effectInfo.miscValue, unitCaster, destTarget, ObjectGuid.Empty, spellInfo);
    }

    
    private void effectCancelConversation() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget) {
            return;
        }

        ArrayList<WorldObject> objs = new ArrayList<>();
        ObjectEntryAndPrivateOwnerIfExistsCheck check = new ObjectEntryAndPrivateOwnerIfExistsCheck(unitTarget.getGUID(), (int) effectInfo.miscValue);
        WorldObjectListSearcher checker = new WorldObjectListSearcher(unitTarget, objs, check, GridMapTypeMask.conversation, gridType.Grid);
        Cell.visitGrid(unitTarget, checker, 100.0f);

        for (var obj : objs) {
            var convo = obj.toConversation();

            if (convo != null) {
                convo.remove();
            }
        }
    }

    
    private void effectAddGarrisonFollower() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var garrison = unitTarget.toPlayer().getGarrison();

        if (garrison != null) {
            garrison.addFollower((int) effectInfo.miscValue);
        }
    }

    
    private void effectCreateHeirloomItem() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var player = caster.toPlayer();

        if (!player) {
            return;
        }

        var collectionMgr = player.getSession().getCollectionMgr();

        if (collectionMgr == null) {
            return;
        }

        ArrayList<Integer> bonusList = new ArrayList<>();
        bonusList.add(collectionMgr.getHeirloomBonus(spellMisc.data0));

        doCreateItem(spellMisc.data0, itemContext.NONE, bonusList);
        executeLogEffectCreateItem(effectInfo.effect, spellMisc.data0);
    }

    
    private void effectActivateGarrisonBuilding() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var garrison = unitTarget.toPlayer().getGarrison();

        if (garrison != null) {
            garrison.activateBuilding((int) effectInfo.miscValue);
        }
    }

    
    private void effectGrantBattlePetLevel() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var playerCaster = caster.toPlayer();

        if (playerCaster == null) {
            return;
        }

        if (unitTarget == null || !unitTarget.isCreature()) {
            return;
        }

        playerCaster.getSession().getBattlePetMgr().grantBattlePetLevel(unitTarget.getBattlePetCompanionGUID(), (short) damage);
    }

    
    private void effectGiveExperience() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var playerTarget = unitTarget == null ? null : unitTarget.toPlayer();

        if (!playerTarget) {
            return;
        }

        var xp = quest.XPValue(playerTarget, (int) effectInfo.miscValue, (int) effectInfo.miscValueB);
        playerTarget.giveXP(xp, null);
    }

    
    private void effectGiveRestedExperience() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var playerTarget = unitTarget == null ? null : unitTarget.toPlayer();

        if (!playerTarget) {
            return;
        }

        // effect second is number of resting hours
        playerTarget.getRestMgr().addRestBonus(RestTypes.XP, Damage * time.Hour * playerTarget.getRestMgr().calcExtraPerSec(RestTypes.XP, 0.125f));
    }

    
    private void effectHealBattlePetPct() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var battlePetMgr = unitTarget.toPlayer().getSession().getBattlePetMgr();

        if (battlePetMgr != null) {
            battlePetMgr.healBattlePetsPct((byte) damage);
        }
    }

    
    private void effectEnableBattlePets() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (unitTarget == null || !unitTarget.isPlayer()) {
            return;
        }

        var player = unitTarget.toPlayer();
        player.setPlayerFlag(playerFlags.PetBattlesUnlocked);
        player.getSession().getBattlePetMgr().unlockSlot(BattlePetSlot.Slot0);
    }

    
    private void effectChangeBattlePetQuality() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var playerCaster = caster.toPlayer();

        if (playerCaster == null) {
            return;
        }

        if (unitTarget == null || !unitTarget.isCreature()) {
            return;
        }

        var qualityRecord = CliDB.BattlePetBreedQualityStorage.values().FirstOrDefault(a1 -> a1.MaxQualityRoll < damage);

        var quality = battlePetBreedQuality.Poor;

        if (qualityRecord != null) {
            quality = battlePetBreedQuality.forValue(qualityRecord.QualityEnum);
        }

        playerCaster.getSession().getBattlePetMgr().changeBattlePetQuality(unitTarget.getBattlePetCompanionGUID(), quality);
    }

    
    private void effectLaunchQuestChoice() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isPlayer()) {
            return;
        }

        unitTarget.toPlayer().sendPlayerChoice(getCaster().getGUID(), effectInfo.miscValue);
    }

    
    private void effectUncageBattlePet() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (!castItem || !caster || !caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var speciesId = castItem.getModifier(ItemModifier.battlePetSpeciesId);
        var breed = (short) (castItem.getModifier(ItemModifier.BattlePetBreedData) & 0xFFFFFF);
        var quality = battlePetBreedQuality.forValue((castItem.getModifier(ItemModifier.BattlePetBreedData) >>> 24) & 0xFF);
        var level = (short) castItem.getModifier(ItemModifier.battlePetLevel);
        var displayId = castItem.getModifier(ItemModifier.BattlePetDisplayId);

        var speciesEntry = CliDB.BattlePetSpeciesStorage.get(speciesId);

        if (speciesEntry == null) {
            return;
        }

        var player = caster.toPlayer();
        var battlePetMgr = player.getSession().getBattlePetMgr();

        if (battlePetMgr == null) {
            return;
        }

        if (battlePetMgr.getMaxPetLevel() < level) {
            battlePetMgr.sendError(BattlePetError.TooHighLevelToUncage, speciesEntry.creatureID);
            sendCastResult(SpellCastResult.CantAddBattlePet);

            return;
        }

        if (battlePetMgr.hasMaxPetCount(speciesEntry, player.getGUID())) {
            battlePetMgr.sendError(BattlePetError.CantHaveMorePetsOfThatType, speciesEntry.creatureID);
            sendCastResult(SpellCastResult.CantAddBattlePet);

            return;
        }

        battlePetMgr.addPet(speciesId, displayId, breed, quality, level);

        player.sendPlaySpellVisual(player, SharedConst.SpellVisualUncagePet, 0, 0, 0.0f, false);

        player.destroyItem(castItem.getBagSlot(), castItem.getSlot(), true);
        castItem = null;
    }

    
    private void effectUpgradeHeirloom() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var player = caster.toPlayer();

        if (player) {
            var collectionMgr = player.getSession().getCollectionMgr();

            if (collectionMgr != null) {
                collectionMgr.upgradeHeirloom(spellMisc.data0, castItemEntry);
            }
        }
    }

    
    private void effectApplyEnchantIllusion() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!itemTarget) {
            return;
        }

        var player = caster.toPlayer();

        if (!player || ObjectGuid.opNotEquals(player.getGUID(), itemTarget.getOwnerGUID())) {
            return;
        }

        itemTarget.setState(ItemUpdateState.changed, player);
        itemTarget.setModifier(ItemModifier.EnchantIllusionAllSpecs, (int) effectInfo.miscValue);

        if (itemTarget.isEquipped()) {
            player.setVisibleItemSlot(itemTarget.getSlot(), itemTarget);
        }

        player.removeTradeableItem(itemTarget);
        itemTarget.clearSoulboundTradeable(player);
    }

    
    private void effectUpdatePlayerPhase() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        PhasingHandler.onConditionChange(unitTarget);
    }

    
    private void effectUpdateZoneAurasAndPhases() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isTypeId(TypeId.PLAYER)) {
            return;
        }

        unitTarget.toPlayer().updateAreaDependentAuras(unitTarget.getAreaId());
    }

    
    private void effectGiveArtifactPower() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget) {
            return;
        }

        var playerCaster = caster.toPlayer();

        if (playerCaster == null) {
            return;
        }

        var artifactAura = playerCaster.getAura(PlayerConst.ArtifactsAllWeaponsGeneralWeaponEquippedPassive);

        if (artifactAura != null) {
            var artifact = playerCaster.getItemByGuid(artifactAura.castItemGuid);

            if (artifact) {
                artifact.giveArtifactXp((long) damage, castItem, ArtifactCategory.forValue(effectInfo.miscValue));
            }
        }
    }

    
    private void effectGiveArtifactPowerNoBonus() {
        if (effectHandleMode != SpellEffectHandleMode.LaunchTarget) {
            return;
        }

        if (!unitTarget || !caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var artifactAura = unitTarget.getAura(PlayerConst.ArtifactsAllWeaponsGeneralWeaponEquippedPassive);

        if (artifactAura != null) {
            var artifact = unitTarget.toPlayer().getItemByGuid(artifactAura.castItemGuid);

            if (artifact) {
                artifact.giveArtifactXp((long) damage, castItem, 0);
            }
        }
    }

    
    private void effectPlaySceneScriptPackage() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (!caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        caster.toPlayer().getSceneMgr().playSceneByPackageId((int) effectInfo.miscValue, SceneFlags.PlayerNonInteractablePhased, destTarget);
    }

    private boolean isUnitTargetSceneObjectAura(Spell spell, TargetInfo target) {
        if (ObjectGuid.opNotEquals(target.targetGuid, spell.getCaster().getGUID())) {
            return false;
        }

        for (var spellEffectInfo : spell.spellInfo.getEffects()) {
            if (target.effects.contains(spellEffectInfo.effectIndex) && spellEffectInfo.isUnitOwnedAuraEffect()) {
                return true;
            }
        }

        return false;
    }

    
    private void effectCreateSceneObject() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (!unitCaster || !targets.getHasDst()) {
            return;
        }

        var sceneObject = sceneObject.createSceneObject((int) effectInfo.miscValue, unitCaster, destTarget, ObjectGuid.Empty);

        if (sceneObject != null) {
            var hasAuraTargetingCaster = uniqueTargetInfo.Any(target -> isUnitTargetSceneObjectAura(this, target));

            if (hasAuraTargetingCaster) {
                sceneObject.setCreatedBySpellCast(castId);
            }
        }
    }

    
    private void effectCreatePrivateSceneObject() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (!unitCaster || !targets.getHasDst()) {
            return;
        }

        var sceneObject = sceneObject.createSceneObject((int) effectInfo.miscValue, unitCaster, destTarget, unitCaster.getGUID());

        if (sceneObject != null) {
            var hasAuraTargetingCaster = uniqueTargetInfo.Any(target -> isUnitTargetSceneObjectAura(this, target));

            if (hasAuraTargetingCaster) {
                sceneObject.setCreatedBySpellCast(castId);
            }
        }
    }

    
    private void effectPlayScene() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        if (caster.getObjectTypeId() != TypeId.PLAYER) {
            return;
        }

        caster.toPlayer().getSceneMgr().playScene((int) effectInfo.miscValue, destTarget);
    }

    
    private void effectGiveHonor() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || unitTarget.getObjectTypeId() != TypeId.PLAYER) {
            return;
        }

        PvPCredit packet = new PvPCredit();
        packet.honor = (int) damage;
        packet.originalHonor = (int) damage;

        var playerTarget = unitTarget.toPlayer();
        playerTarget.addHonorXp((int) damage);
        playerTarget.sendPacket(packet);
    }

    
    private void effectJumpCharge() {
        if (effectHandleMode != SpellEffectHandleMode.Launch) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        if (unitCaster.isInFlight()) {
            return;
        }

        var jumpParams = global.getObjectMgr().getJumpChargeParams(effectInfo.miscValue);

        if (jumpParams == null) {
            return;
        }

        var speed = jumpParams.speed;

        if (jumpParams.treatSpeedAsMoveTimeSeconds) {
            speed = unitCaster.getLocation().getExactDist(destTarget) / jumpParams.speed;
        }

        JumpArrivalCastArgs arrivalCast = null;

        if (effectInfo.triggerSpell != 0) {
            arrivalCast = new JumpArrivalCastArgs();
            arrivalCast.spellId = effectInfo.triggerSpell;
        }

        SpellEffectExtraData effectExtra = null;

        if (jumpParams.spellVisualId != null || jumpParams.progressCurveId != null || jumpParams.parabolicCurveId != null) {
            effectExtra = new spellEffectExtraData();

            if (jumpParams.spellVisualId != null) {
                effectExtra.spellVisualId = jumpParams.spellVisualId.intValue();
            }

            if (jumpParams.progressCurveId != null) {
                effectExtra.progressCurveId = jumpParams.progressCurveId.intValue();
            }

            if (jumpParams.parabolicCurveId != null) {
                effectExtra.parabolicCurveId = jumpParams.parabolicCurveId.intValue();
            }
        }

        unitCaster.getMotionMaster().moveJumpWithGravity(destTarget, speed, jumpParams.jumpGravity, eventId.jump, false, arrivalCast, effectExtra);
    }

    
    private void effectLearnTransmogSet() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        if (!unitTarget || !unitTarget.isPlayer()) {
            return;
        }

        unitTarget.toPlayer().getSession().getCollectionMgr().addTransmogSet((int) effectInfo.miscValue);
    }

    
    private void effectLearnAzeriteEssencePower() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var playerTarget = unitTarget != null ? unitTarget.toPlayer() : null;

        if (!playerTarget) {
            return;
        }

        var heartOfAzeroth = playerTarget.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

        if (heartOfAzeroth == null) {
            return;
        }

        var azeriteItem = heartOfAzeroth.getAsAzeriteItem();

        if (azeriteItem == null) {
            return;
        }

        // remove old rank and apply new one
        if (azeriteItem.isEquipped()) {
            var selectedEssences = azeriteItem.GetSelectedAzeriteEssences();

            if (selectedEssences != null) {
                for (var slot = 0; slot < SharedConst.MaxAzeriteEssenceSlot; ++slot) {
                    if (selectedEssences.azeriteEssenceID.get(slot) == effectInfo.miscValue) {
                        var major = global.getDB2Mgr().GetAzeriteItemMilestonePower(slot).type == AzeriteItemMilestoneType.MajorEssence.getValue();
                        playerTarget.applyAzeriteEssence(azeriteItem, (int) effectInfo.miscValue, SharedConst.MaxAzeriteEssenceRank, major, false);
                        playerTarget.applyAzeriteEssence(azeriteItem, (int) effectInfo.miscValue, (int) effectInfo.miscValueB, major, false);

                        break;
                    }
                }
            }
        }

        azeriteItem.SetEssenceRank((int) effectInfo.miscValue, (int) effectInfo.miscValueB);
        azeriteItem.setState(ItemUpdateState.changed, playerTarget);
    }

    
    private void effectCreatePrivateConversation() {
        if (effectHandleMode != SpellEffectHandleMode.hit) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null || !unitCaster.isPlayer()) {
            return;
        }

        conversation.CreateConversation((int) effectInfo.miscValue, unitCaster, destTarget, unitCaster.getGUID(), spellInfo);
    }

    
    private void effectSendChatMessage() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var unitCaster = getUnitCasterForEffectHandlers();

        if (unitCaster == null) {
            return;
        }

        var broadcastTextId = (int) effectInfo.miscValue;

        if (!CliDB.BroadcastTextStorage.containsKey(broadcastTextId)) {
            return;
        }

        var chatType = ChatMsg.forValue(effectInfo.miscValueB);
        unitCaster.talk(broadcastTextId, chatType, global.getCreatureTextMgr().getRangeForChatType(chatType), unitTarget);
    }

    
    private void effectGrantBattlePetExperience() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var playerCaster = caster.toPlayer();

        if (playerCaster == null) {
            return;
        }

        if (!unitTarget || !unitTarget.isCreature()) {
            return;
        }

        playerCaster.getSession().getBattlePetMgr().grantBattlePetExperience(unitTarget.getBattlePetCompanionGUID(), (short) damage, BattlePetXpSource.SpellEffect);
    }

    
    private void effectLearnTransmogIllusion() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var player = unitTarget == null ? null : unitTarget.toPlayer();

        if (player == null) {
            return;
        }

        var illusionId = (int) effectInfo.miscValue;

        if (!CliDB.TransmogIllusionStorage.containsKey(illusionId)) {
            return;
        }

        player.getSession().getCollectionMgr().addTransmogIllusion(illusionId);
    }

    
    private void effectModifyAuraStacks() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var targetAura = unitTarget.getAura(effectInfo.triggerSpell);

        if (targetAura == null) {
            return;
        }

        switch (effectInfo.miscValue) {
            case 0:
                targetAura.modStackAmount(damage);

                break;
            case 1:
                targetAura.setStackAmount((byte) damage);

                break;
            default:
                break;
        }
    }

    
    private void effectModifyCooldown() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        unitTarget.getSpellHistory().modifyCooldown(effectInfo.triggerSpell, duration.ofSeconds(damage));
    }

    
    private void effectModifyCooldowns() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        unitTarget.getSpellHistory().modifyCoooldowns(itr ->
        {
            var spellOnCooldown = global.getSpellMgr().getSpellInfo(itr.spellId, Difficulty.NONE);

            if ((int) spellOnCooldown.spellFamilyName != effectInfo.miscValue) {
                return false;
            }

            var bitIndex = effectInfo.MiscValueB - 1;

            if (bitIndex < 0 || bitIndex >= (Integer.SIZE / Byte.SIZE) * 8) {
                return false;
            }

            FlagArray128 reqFlag = new flagArray128();
            reqFlag.set(bitIndex / 32, 1 << (bitIndex % 32));

            return (spellOnCooldown.spellFamilyFlags & reqFlag);
        }, duration.ofSeconds(damage));
    }

    
    private void effectModifyCooldownsByCategory() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        unitTarget.getSpellHistory().modifyCoooldowns(itr -> global.getSpellMgr().getSpellInfo(itr.spellId, Difficulty.NONE).categoryId == effectInfo.miscValue, duration.ofSeconds(damage));
    }

    
    private void effectModifySpellCharges() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        for (var i = 0; i < damage; ++i) {
            unitTarget.getSpellHistory().restoreCharge((int) effectInfo.miscValue);
        }
    }

    
    private void effectCreateTraitTreeConfig() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var target = unitTarget == null ? null : unitTarget.toPlayer();

        if (target == null) {
            return;
        }

        TraitConfigPacket newConfig = new traitConfigPacket();
        newConfig.type = TraitMgr.getConfigTypeForTree(effectInfo.miscValue);

        if (newConfig.type != TraitConfigType.generic) {
            return;
        }

        newConfig.traitSystemID = CliDB.TraitTreeStorage.get(effectInfo.miscValue).traitSystemID;
        target.createTraitConfig(newConfig);
    }

    
    private void effectChangeActiveCombatTraitConfig() {
        if (effectHandleMode != SpellEffectHandleMode.HitTarget) {
            return;
        }

        var target = unitTarget == null ? null : unitTarget.toPlayer();

        if (target == null) {
            return;
        }

        if (!(CustomArg instanceof TraitConfigPacket)) {
            return;
        }

        target.updateTraitConfig(CustomArg instanceof TraitConfigPacket ? (TraitConfigPacket) CustomArg : null, (int) damage, false);
    }
}
