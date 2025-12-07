package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class VehicleAI extends CreatureAI {
    private static final int VEHICLE_CONDITION_CHECK_TIME = 1000;
    private static final int VEHICLE_DISMISS_TIME = 5000;

    private boolean hasConditions;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _conditionsTimer;
    private int conditionsTimer;
    private boolean doDismiss;
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _dismissTimer;
    private int dismissTimer;

    public VehicleAI(Creature creature) {
        super(creature);
        conditionsTimer = VEHICLE_CONDITION_CHECK_TIME;
        loadConditions();
        doDismiss = false;
        dismissTimer = VEHICLE_DISMISS_TIME;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        checkConditions(diff);

        if (doDismiss) {
            if (dismissTimer < diff) {
                doDismiss = false;
                me.despawnOrUnsummon();
            } else {
                dismissTimer -= diff;
            }
        }
    }
    @Override
    public void moveInLineOfSight(Unit who) {
    }

    @Override
    public void attackStart(Unit victim) {
    }

    @Override
    public void onCharmed(boolean isNew) {
        var charmed = me.isCharmed();

        if (!me.getVehicleKit1().isVehicleInUse() && !charmed && hasConditions) { //was used and has conditions
            doDismiss = true; //needs reset
        } else if (charmed) {
            doDismiss = false; //in use again
        }

        dismissTimer = VEHICLE_DISMISS_TIME; //reset timer
    }

    private void loadConditions() {
        hasConditions = Global.getConditionMgr().hasConditionsForNotGroupedEntry(ConditionSourceType.CreatureTemplateVehicle, me.getEntry());
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void CheckConditions(uint diff)
    private void checkConditions(int diff) {
        if (!hasConditions) {
            return;
        }

        if (conditionsTimer <= diff) {
            var vehicleKit = me.getVehicleKit1();

            if (vehicleKit) {
                for (var pair : vehicleKit.seats.entrySet()) {
                    var passenger = Global.getObjAccessor().getUnit(me, pair.getValue().Passenger.Guid);

                    if (passenger) {
                        var player = passenger.getAsPlayer();

                        if (player) {
                            if (!Global.getConditionMgr().IsObjectMeetingNotGroupedConditions(ConditionSourceType.CreatureTemplateVehicle, me.getEntry(), player, me)) {
                                player.exitVehicle();

                                return; //check other pessanger in next tick
                            }
                        }
                    }
                }
            }

            conditionsTimer = VEHICLE_CONDITION_CHECK_TIME;
        } else {
            conditionsTimer -= diff;
        }
    }
}