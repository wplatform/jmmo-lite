package com.github.azeroth.game.arena;


import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.entity.player.Player;

class RingofValorArena extends Arena {
    public RingofValorArena(BattlegroundTemplate battlegroundTemplate) {
        super(battlegroundTemplate);
        events = new eventMap();
    }

    @Override
    public boolean setupBattleground() {
        var result = true;
        result &= addObject(RingofValorObjectTypes.ELEVATOR1, RingofValorGameObjects.ELEVATOR1, 763.536377f, -294.535767f, 0.505383f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.ELEVATOR2, RingofValorGameObjects.ELEVATOR2, 763.506348f, -273.873352f, 0.505383f, 0.000000f, 0, 0, 0, 0);

        if (!result) {
            Logs.SQL.error("RingofValorArena: Failed to spawn elevator object!");

            return false;
        }

        result &= addObject(RingofValorObjectTypes.BUFF1, RingofValorGameObjects.BUFF1, 735.551819f, -284.794678f, 28.276682f, 0.034906f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.BUFF2, RingofValorGameObjects.BUFF2, 791.224487f, -284.794464f, 28.276682f, 2.600535f, 0, 0, 0, 0);

        if (!result) {
            Logs.SQL.error("RingofValorArena: Failed to spawn buff object!");

            return false;
        }

        result &= addObject(RingofValorObjectTypes.FIRE1, RingofValorGameObjects.FIRE1, 743.543457f, -283.799469f, 28.286655f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.FIRE2, RingofValorGameObjects.FIRE2, 782.971802f, -283.799469f, 28.286655f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.FIREDOOR1, RingofValorGameObjects.FIREDOOR1, 743.711060f, -284.099609f, 27.542587f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.FIREDOOR2, RingofValorGameObjects.FIREDOOR2, 783.221252f, -284.133362f, 27.535686f, 0.000000f, 0, 0, 0, 0);

        if (!result) {
            Logs.SQL.error("RingofValorArena: Failed to spawn fire/firedoor object!");

            return false;
        }

        result &= addObject(RingofValorObjectTypes.GEAR1, RingofValorGameObjects.GEAR1, 763.664551f, -261.872986f, 26.686588f, 0.000000f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.GEAR2, RingofValorGameObjects.GEAR2, 763.578979f, -306.146149f, 26.665222f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PULLEY1, RingofValorGameObjects.PULLEY1, 700.722290f, -283.990662f, 39.517582f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PULLEY2, RingofValorGameObjects.PULLEY2, 826.303833f, -283.996429f, 39.517582f, 0.000000f, 0, 0, 0, 0);

        if (!result) {
            Logs.SQL.error("RingofValorArena: Failed to spawn gear/pully object!");

            return false;
        }

        result &= addObject(RingofValorObjectTypes.PILAR1, RingofValorGameObjects.PILAR1, 763.632385f, -306.162384f, 25.909504f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PILAR2, RingofValorGameObjects.PILAR2, 723.644287f, -284.493256f, 24.648525f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PILAR3, RingofValorGameObjects.PILAR3, 763.611145f, -261.856750f, 25.909504f, 0.000000f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PILAR4, RingofValorGameObjects.PILAR4, 802.211609f, -284.493256f, 24.648525f, 0.000000f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PILARCOLLISION1, RingofValorGameObjects.PILARCOLLISION1, 763.632385f, -306.162384f, 30.639660f, 3.141593f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PILARCOLLISION2, RingofValorGameObjects.PILARCOLLISION2, 723.644287f, -284.493256f, 32.382710f, 0.000000f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PILARCOLLISION3, RingofValorGameObjects.PILARCOLLISION3, 763.611145f, -261.856750f, 30.639660f, 0.000000f, 0, 0, 0, 0);
        result &= addObject(RingofValorObjectTypes.PILARCOLLISION4, RingofValorGameObjects.PILARCOLLISION4, 802.211609f, -284.493256f, 32.382710f, 3.141593f, 0, 0, 0, 0);

        if (!result) {
            Logs.SQL.error("RingofValorArena: Failed to spawn pilar object!");

            return false;
        }

        return true;
    }

    @Override
    public void startingEventOpenDoors() {
        // Buff respawn
        spawnBGObject(RingofValorObjectTypes.BUFF1, 90);
        spawnBGObject(RingofValorObjectTypes.BUFF2, 90);
        // Elevators
        doorOpen(RingofValorObjectTypes.ELEVATOR1);
        doorOpen(RingofValorObjectTypes.ELEVATOR2);

        events.ScheduleEvent(RingofValorEvents.OPENFENCES, duration.FromSeconds(20));

        // Should be false at first, TogglePillarCollision will do it.
        togglePillarCollision(true);
    }

    @Override
    public void handleAreaTrigger(Player player, int trigger, boolean entered) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        switch (trigger) {
            case 5224:
            case 5226:
                // fire was removed in 3.2.0
            case 5473:
            case 5474:
                break;
            default:
                super.handleAreaTrigger(player, trigger, entered);

                break;
        }
    }

    @Override
    public void postUpdateImpl(int diff) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        events.update(diff);

        events.ExecuteEvents(eventId ->
        {
            switch (eventId) {
                case RingofValorEvents.OpenFences:
                    // Open fire (only at game start)
                    for (byte i = (byte) RingofValorObjectTypes.FIRE1; i <= RingofValorObjectTypes.FIREDOOR2; ++i) {
                        doorOpen(i);
                    }

                    events.ScheduleEvent(RingofValorEvents.CLOSEFIRE, duration.FromSeconds(5));

                    break;
                case RingofValorEvents.CloseFire:
                    for (byte i = (byte) RingofValorObjectTypes.FIRE1; i <= RingofValorObjectTypes.FIREDOOR2; ++i) {
                        doorClose(i);
                    }

                    // Fire got closed after five seconds, leaves twenty seconds before toggling pillars
                    events.ScheduleEvent(RingofValorEvents.SWITCHPILLARS, duration.FromSeconds(20));

                    break;
                case RingofValorEvents.SwitchPillars:
                    togglePillarCollision(true);
                    events.Repeat(duration.FromSeconds(25));

                    break;
            }
        });
    }

    private void togglePillarCollision(boolean enable) {
        // Toggle visual pillars, pulley, gear, and collision based on previous state
        for (var i = RingofValorObjectTypes.PILAR1; i <= RingofValorObjectTypes.GEAR2; ++i) {
            if (enable) {
                doorOpen(i);
            } else {
                doorClose(i);
            }
        }

        for (byte i = (byte) RingofValorObjectTypes.PILAR2; i <= RingofValorObjectTypes.PULLEY2; ++i) {
            if (enable) {
                doorClose(i);
            } else {
                doorOpen(i);
            }
        }

        for (byte i = (byte) RingofValorObjectTypes.PILAR1; i <= RingofValorObjectTypes.PILARCOLLISION4; ++i) {
            var go = getBGObject(i);

            if (go) {
                if (i >= RingofValorObjectTypes.PILARCOLLISION1) {
                    var state = ((go.getTemplate().door.startOpen != 0) == enable) ? GOState.Active : GOState.Ready;
                    go.setGoState(state);
                }

                for (var guid : getPlayers().keySet()) {
                    var player = global.getObjAccessor().findPlayer(guid);

                    if (player) {
                        go.sendUpdateToPlayer(player);
                    }
                }
            }
        }
    }
}
