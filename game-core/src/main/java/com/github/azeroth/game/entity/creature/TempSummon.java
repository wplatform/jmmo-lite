package com.github.azeroth.game.entity.creature;


import com.github.azeroth.dbc.domain.SummonProperty;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.domain.creature.TempSummonType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Objects;

@Getter
@Setter
public class TempSummon extends Creature {
    public SummonProperty summonProperty;
    private TempSummonType summonType = TempSummonType.values()[0];
    private int timer;
    private int lifetime;
    private ObjectGuid summonerGuid;
    private Integer creatureIdVisibleToSummoner = null;
    private Integer displayIdVisibleToSummoner = null;
    private boolean canFollowOwner;

    public TempSummon(SummonProperty propertiesRecord, WorldObject owner, boolean isWorldObject) {
        super(isWorldObject);
        summonProperty = propertiesRecord;
        summonType = TempSummonType.MANUAL_DESPAWN;

        summonerGuid = owner != null ? owner.getGUID() : ObjectGuid.EMPTY;
        setUnitTypeMask(UnitTypeMask.forValue(getUnitTypeMask().getValue() | getUnitTypeMask().summon.getValue()));
        canFollowOwner = true;
    }

    public final WorldObject getSummoner() {
        return !summonerGuid.isEmpty() ? global.getObjAccessor().GetWorldObject(this, summonerGuid) : null;
    }

    public final Unit getSummonerUnit() {
        var summoner = getSummoner();

        if (summoner != null) {
            return summoner.toUnit();
        }

        return null;
    }

    public final Creature getSummonerCreatureBase() {
        return !summonerGuid.isEmpty() ? ObjectAccessor.getCreature(this, summonerGuid) : null;
    }

    public final GameObject getSummonerGameObject() {
        var summoner = getSummoner();

        if (summoner != null) {
            return summoner.toGameObject();
        }

        return null;
    }

    @Override
    public float getDamageMultiplierForTarget(WorldObject target) {
        return 1.0f;
    }

    @Override
    public void update(int diff) {
        super.update(diff);

        if (getDeathState() == deathState.Dead) {
            unSummon();

            return;
        }

        switch (summonType) {
            case ManualDespawn:
            case DeadDespawn:
                break;
            case TimedDespawn: {
                if (timer <= diff) {
                    unSummon();

                    return;
                }

                _timer -= diff;

                break;
            }
            case TimedDespawnOutOfCombat: {
                if (!isInCombat()) {
                    if (timer <= diff) {
                        unSummon();

                        return;
                    }

                    _timer -= diff;
                } else if (timer != lifetime) {
                    timer = lifetime;
                }

                break;
            }

            case CorpseTimedDespawn: {
                if (getDeathState() == deathState.Corpse) {
                    if (timer <= diff) {
                        unSummon();

                        return;
                    }

                    _timer -= diff;
                }

                break;
            }
            case CorpseDespawn: {
                // if m_deathState is DEAD, CORPSE was skipped
                if (getDeathState() == deathState.Corpse) {
                    unSummon();

                    return;
                }

                break;
            }
            case TimedOrCorpseDespawn: {
                if (getDeathState() == deathState.Corpse) {
                    unSummon();

                    return;
                }

                if (!isInCombat()) {
                    if (timer <= diff) {
                        unSummon();

                        return;
                    } else {
                        _timer -= diff;
                    }
                } else if (timer != lifetime) {
                    timer = lifetime;
                }

                break;
            }
            case TimedOrDeadDespawn: {
                if (!isInCombat() && isAlive()) {
                    if (timer <= diff) {
                        unSummon();

                        return;
                    } else {
                        _timer -= diff;
                    }
                } else if (timer != lifetime) {
                    timer = lifetime;
                }

                break;
            }
            default:
                unSummon();
                Log.outError(LogFilter.unit, "Temporary summoned creature (entry: {0}) have unknown type {1} of ", getEntry(), summonType);

                break;
        }
    }

    public void initStats(int duration) {
        timer = duration;
        lifetime = duration;

        if (summonType == TempSummonType.ManualDespawn) {
            summonType = (duration == 0) ? TempSummonType.DeadDespawn : TempSummonType.TimedDespawn;
        }

        var owner = getSummonerUnit();

        if (owner != null && isTrigger() && getSpells()[0] != 0) {
            if (owner.isTypeId(TypeId.PLAYER)) {
                setControlledByPlayer(true);
            }
        }

        if (owner != null && owner.isPlayer()) {
            var summonedData = global.getObjectMgr().getCreatureSummonedData(getEntry());

            if (summonedData != null) {
                creatureIdVisibleToSummoner = summonedData.getCreatureIdVisibleToSummoner();

                if (summonedData.getCreatureIdVisibleToSummoner() != null) {
                    var creatureTemplateVisibleToSummoner = global.getObjectMgr().getCreatureTemplate(summonedData.getCreatureIdVisibleToSummoner().intValue());
                    displayIdVisibleToSummoner = ObjectManager.chooseDisplayId(creatureTemplateVisibleToSummoner, null).creatureDisplayId;
                }
            }
        }

        if (summonProperty == null) {
            return;
        }

        if (owner != null) {
            var slot = summonProperty.slot;

            if (slot > 0) {
                if (!owner.getSummonSlot()[slot].isEmpty() && ObjectGuid.opNotEquals(owner.getSummonSlot()[slot], getGUID())) {
                    var oldSummon = getMap().getCreature(owner.getSummonSlot()[slot]);

                    if (oldSummon != null && oldSummon.isSummon()) {
                        oldSummon.toTempSummon().unSummon();
                    }
                }

                owner.getSummonSlot()[slot] = getGUID();
            }

            if (!summonProperty.getFlags().hasFlag(SummonPropertiesFlags.UseCreatureLevel)) {
                setLevel(owner.getLevel());
            }
        }

        var faction = summonProperty.faction;

        if (owner && summonProperty.getFlags().hasFlag(SummonPropertiesFlags.UseSummonerFaction)) // TODO: Determine priority between faction and flag
        {
            faction = owner.getFaction();
        }

        if (faction != 0) {
            setFaction(faction);
        }

        if (summonProperty.getFlags().hasFlag(SummonPropertiesFlags.SummonFromBattlePetJournal)) {
            removeNpcFlag(NPCFlags.WildBattlePet);
        }
    }

    public void initSummon() {
        var owner = getSummoner();

        if (owner != null) {
            if (owner.isCreature()) {
                if (owner.toCreature().getAi() != null) {
                    owner.toCreature().getAi().justSummoned(this);
                }
            } else if (owner.isGameObject()) {
                if (owner.toGameObject().getAI() != null) {
                    owner.toGameObject().getAI().justSummoned(this);
                }
            }

            if (isAIEnabled()) {
                getAi().isSummonedBy(owner);
            }
        }
    }

    @Override
    public void updateObjectVisibilityOnCreate() {
        ArrayList<WorldObject> objectsToUpdate = new ArrayList<>();
        objectsToUpdate.add(this);

        var smoothPhasing = getSmoothPhasing();

        if (smoothPhasing != null) {
            var infoForSeer = smoothPhasing.getInfoForSeer(getDemonCreatorGUID());

            if (infoForSeer != null && infoForSeer.replaceObject != null && smoothPhasing.isReplacing(infoForSeer.replaceObject.getValue())) {
                var original = global.getObjAccessor().GetWorldObject(this, infoForSeer.replaceObject.getValue());

                if (original != null) {
                    objectsToUpdate.add(original);
                }
            }
        }

        VisibleChangesNotifier notifier = new VisibleChangesNotifier(objectsToUpdate, gridType.World);
        Cell.visitGrid(this, notifier, getVisibilityRange());
    }

    @Override
    public void updateObjectVisibilityOnDestroy() {
        ArrayList<WorldObject> objectsToUpdate = new ArrayList<>();
        objectsToUpdate.add(this);

        WorldObject original = null;
        var smoothPhasing = getSmoothPhasing();

        if (smoothPhasing != null) {
            var infoForSeer = smoothPhasing.getInfoForSeer(getDemonCreatorGUID());

            if (infoForSeer != null && infoForSeer.replaceObject != null && smoothPhasing.isReplacing(infoForSeer.replaceObject.getValue())) {
                original = global.getObjAccessor().GetWorldObject(this, infoForSeer.replaceObject.getValue());
            }

            if (original != null) {
                objectsToUpdate.add(original);

                // disable replacement without removing - it is still needed for next step (visibility update)
                var originalSmoothPhasing = original.getSmoothPhasing();

                if (originalSmoothPhasing != null) {
                    originalSmoothPhasing.disableReplacementForSeer(getDemonCreatorGUID());
                }
            }
        }

        VisibleChangesNotifier notifier = new VisibleChangesNotifier(objectsToUpdate, gridType.World);
        Cell.visitGrid(this, notifier, getVisibilityRange());

        if (original != null) // original is only != null when it was replaced
        {
            var originalSmoothPhasing = original.getSmoothPhasing();

            if (originalSmoothPhasing != null) {
                originalSmoothPhasing.clearViewerDependentInfo(getDemonCreatorGUID());
            }
        }
    }

    public final void setTempSummonType(TempSummonType type) {
        summonType = type;
    }

    public void unSummon() {
        unSummon(duration.Zero);
    }

    public void unSummon(Duration msTime) {
        if (duration.opNotEquals(msTime, duration.Zero)) {
            ForcedUnsummonDelayEvent pEvent = new ForcedUnsummonDelayEvent(this);

            getEvents().addEvent(pEvent, getEvents().CalculateTime(msTime));

            return;
        }

        if (isPet()) {
            toPet().remove(PetSaveMode.NotInSlot);
            return;
        }

        var owner = getSummoner();

        if (owner != null) {
            if (owner.isCreature()) {
                if (owner.toCreature().getAi() != null) {
                    owner.toCreature().getAi().summonedCreatureDespawn(this);
                }
            } else if (owner.isGameObject()) {
                if (owner.toGameObject().getAI() != null) {
                    owner.toGameObject().getAI().summonedCreatureDespawn(this);
                }
            }
        }

        addObjectToRemoveList();
    }

    @Override
    public void removeFromWorld() {
        if (!isInWorld()) {
            return;
        }

        if (summonProperty != null) {
            var slot = summonProperty.slot;

            if (slot > 0) {
                var owner = getSummonerUnit();

                if (owner != null) {
                    if (Objects.equals(owner.getSummonSlot()[slot], getGUID())) {
                        owner.getSummonSlot()[slot].clear();
                    }
                }
            }
        }

        if (!getOwnerGUID().isEmpty()) {
            Log.outError(LogFilter.unit, "Unit {0} has owner guid when removed from world", getEntry());
        }

        super.removeFromWorld();
    }

    @Override
    public String getDebugInfo() {
        return String.format("%1$s\nTempSummonType : %2$s Summoner: %3$s Timer: %4$s", super.getDebugInfo(), getSummonType(), getSummonerGUID(), getTimer());
    }

    @Override
    public void saveToDB(int mapid, ArrayList<Difficulty> spawnDifficulties) {
    }

    public final ObjectGuid getSummonerGUID() {
        return summonerGuid;
    }

    public final void setSummonerGUID(ObjectGuid summonerGUID) {
        summonerGuid = summonerGUID;
    }

    public final int getTimer() {
        return timer;
    }

    public final Integer getCreatureIdVisibleToSummoner() {
        return creatureIdVisibleToSummoner;
    }

    public final Integer getDisplayIdVisibleToSummoner() {
        return displayIdVisibleToSummoner;
    }

    public final boolean canFollowOwner() {
        return canFollowOwner;
    }

    public final void setCanFollowOwner(boolean can) {
        canFollowOwner = can;
    }

    private TempSummonType getSummonType() {
        return summonType;
    }
}
