package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.movement.*;
import game.scripting.interfaces.icreature.*;
import game.scripting.interfaces.igameobject.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class AISelector {
    public static CreatureAI selectAI(Creature creature) {
        if (creature.isPet()) {
            return new PetAI(creature);
        }

        //scriptname in db
        var scriptedAI = Global.getScriptMgr().<ICreatureGetAI, CreatureAI>RunScriptRet(p -> p.GetAI(creature), creature.getScriptId());

        if (scriptedAI != null) {
            return scriptedAI;
        }

        switch (creature.getTemplate().aIName) {
            case "AggressorAI":
                return new AggressorAI(creature);
            case "ArcherAI":
                return new ArcherAI(creature);
            case "CombatAI":
                return new CombatAI(creature);
            case "CritterAI":
                return new CritterAI(creature);
            case "GuardAI":
                return new GuardAI(creature);
            case "NullCreatureAI":
                return new NullCreatureAI(creature);
            case "PassiveAI":
                return new PassiveAI(creature);
            case "PetAI":
                return new PetAI(creature);
            case "ReactorAI":
                return new ReactorAI(creature);
            case "SmartAI":
                return new SmartAI(creature);
            case "TotemAI":
                return new TotemAI(creature);
            case "TriggerAI":
                return new TriggerAI(creature);
            case "TurretAI":
                return new TurretAI(creature);
            case "VehicleAI":
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