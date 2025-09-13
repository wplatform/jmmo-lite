package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class VehicleAI extends CreatureAI {
    private static final int VEHICLE_CONDITION_CHECK_TIME = 1000;
    private static final int VEHICLE_DISMISS_TIME = 5000;

    private boolean hasConditions;
    private int conditionsTimer;
    private boolean doDismiss;
    private int dismissTimer;

    public VehicleAI(Creature creature) {
        super(creature);
        conditionsTimer = VEHICLE_CONDITION_CHECK_TIME;
        loadConditions();
        doDismiss = false;
        dismissTimer = VEHICLE_DISMISS_TIME;
    }

    @Override
    public void updateAI(int diff) {
        checkConditions(diff);

        if (doDismiss) {
            if (dismissTimer < diff) {
                doDismiss = false;
                me.despawnOrUnsummon();
            } else {
                _dismissTimer -= diff;
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

        if (!me.getVehicleKit().IsVehicleInUse() && !charmed && hasConditions) //was used and has conditions
        {
            doDismiss = true; //needs reset
        } else if (charmed) {
            doDismiss = false; //in use again
        }

        dismissTimer = VEHICLE_DISMISS_TIME; //reset timer
    }

    private void loadConditions() {
        hasConditions = global.getConditionMgr().hasConditionsForNotGroupedEntry(ConditionSourceType.CreatureTemplateVehicle, me.getEntry());
    }

    private void checkConditions(int diff) {
        if (!hasConditions) {
            return;
        }

        if (conditionsTimer <= diff) {
            var vehicleKit = me.getVehicleKit();

            if (vehicleKit) {
                for (var pair : vehicleKit.Seats.entrySet()) {
                    var passenger = global.getObjAccessor().GetUnit(me, pair.getValue().passenger.guid);

                    if (passenger) {
                        var player = passenger.toPlayer();

                        if (player) {
                            if (!global.getConditionMgr().isObjectMeetingNotGroupedConditions(ConditionSourceType.CreatureTemplateVehicle, me.getEntry(), player, me)) {
                                player.exitVehicle();

                                return; //check other pessanger in next tick
                            }
                        }
                    }
                }
            }

            conditionsTimer = VEHICLE_CONDITION_CHECK_TIME;
        } else {
            _conditionsTimer -= diff;
        }
    }
}
