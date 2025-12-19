package com.github.azeroth.game.ai;

import com.github.azeroth.game.domain.unit.UnitTypeMask;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.script.ScriptManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectAiFactory {

    public static final String AGGRESSOR_AI_NAME = "AggressorAI";
    public static final String ARCHER_AI_NAME = "ArcherAI";
    public static final String COMBAT_AI_NAME = "CombatAI";
    public static final String CRITTER_AI_NAME = "CritterAI";
    public static final String GUARD_AI_NAME = "GuardAI";
    public static final String NULL_CREATURE_AI_NAME = "NullCreatureAI";
    public static final String PASSIVE_AI_NAME = "PassiveAI";
    public static final String PET_AI_NAME = "PetAI";
    public static final String REACTOR_AI_NAME = "ReactorAI";
    public static final String SMART_AI_NAME = "SmartAI";
    public static final String TOTEM_AI_NAME = "TotemAI";
    public static final String TRIGGER_AI_NAME = "TriggerAI";
    public static final String TURRET_AI_NAME = "TurretAI";
    public static final String VEHICLE_AI_NAME = "VehicleAI";

    private final ScriptManager scriptManager;

    public static CreatureAI selectAI(Creature creature) {
        if (creature.isPet()) {
            return new PetAI(creature);
        }

        //scriptname in db
        var scriptedAI = Global.getScriptMgr().<ICreatureGetAI, CreatureAI>RunScriptRet(p -> p.GetAI(creature), creature.getScriptId());

        if (scriptedAI != null) {
            return scriptedAI;
        }

        // same as in new CreatureAIFactory<AIObject>(AIName)->RegisterSelf()
        switch (creature.getCreatureTemplate().aiName) {
            case AGGRESSOR_AI_NAME:
                return new AggressorAI(creature);
            case ARCHER_AI_NAME:
                return new ArcherAI(creature);
            case COMBAT_AI_NAME:
                return new CombatAI(creature);
            case CRITTER_AI_NAME:
                return new CritterAI(creature);
            case GUARD_AI_NAME:
                return new GuardAI(creature);
            case NULL_CREATURE_AI_NAME:
                return new NullCreatureAI(creature);
            case PASSIVE_AI_NAME:
                return new PassiveAI(creature);
            case PET_AI_NAME:
                return new PetAI(creature);
            case REACTOR_AI_NAME:
                return new ReactorAI(creature);
            case SMART_AI_NAME:
                return new SmartAI(creature);
            case TOTEM_AI_NAME:
                return new TotemAI(creature);
            case TRIGGER_AI_NAME:
                return new TriggerAI(creature);
            case TURRET_AI_NAME:
                return new TurretAI(creature);
            case VEHICLE_AI_NAME:
                return new VehicleAI(creature);
        }

        // select by NPC flags
        if (creature.isVehicle()) {
            return new VehicleAI(creature);
        } else if (creature.hasUnitTypeMask(UnitTypeMask.ControlableGuardian) && ((Guardian)creature).getOwnerUnit().isTypeId(TypeId.Player)) {
            return new PetAI(creature);
        } else if (creature.hasNpcFlag(NPCFlags.SpellClick)) {
            return new NullCreatureAI(creature);
        } else if (creature.isGuard()) {
            return new GuardAI(creature);
        } else if (creature.hasUnitTypeMask(UnitTypeMask.ControlableGuardian)) {
            return new PetAI(creature);
        } else if (creature.isTotem()) {
            return new TotemAI(creature);
        } else if (creature.isTrigger()) {
            if (creature.spells[0] != 0) {
                return new TriggerAI(creature);
            } else {
                return new NullCreatureAI(creature);
            }
        } else if (creature.isCritter() && !creature.hasUnitTypeMask(UnitTypeMask.Guardian)) {
            return new CritterAI(creature);
        }

        if (!creature.isCivilian() && !creature.isNeutralToAll()) {
            return new AggressorAI(creature);
        }

        if (creature.isCivilian() || creature.isNeutralToAll()) {
            return new ReactorAI(creature);
        }

        return new NullCreatureAI(creature);
    }

    public static MovementGenerator selectMovementGenerator(Unit unit) {
        var type = unit.getDefaultMovementType();
        var creature = unit.getAsCreature();

        if (creature != null && creature.getPlayerMovingMe1() == null) {
            type = creature.getDefaultMovementType();
        }

        return switch (type) {
            case Random -> new RandomMovementGenerator();
            case Waypoint -> new WaypointMovementGenerator();
            case Idle -> new IdleMovementGenerator();
            default -> null;
        };
    }

    public static GameObjectAI selectGameObjectAI(GameObject go) {
        // scriptname in db
        var scriptedAI = Global.getScriptMgr().<IGameObjectGetAI, GameObjectAI>RunScriptRet(p -> p.GetAI(go), go.getScriptId());

        if (scriptedAI != null) {
            return scriptedAI;
        }

        return switch (go.getAiName()) {
            case "SmartGameObjectAI" -> new SmartGameObjectAI(go);
            default -> new GameObjectAI(go);
        };
    }

}
