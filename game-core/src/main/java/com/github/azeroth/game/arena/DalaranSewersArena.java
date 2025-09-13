package com.github.azeroth.game.arena;


import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.entity.player.Player;

class DalaranSewersArena extends Arena {
    public DalaranSewersArena(BattlegroundTemplate battlegroundTemplate) {
        super(battlegroundTemplate);
        events = new eventMap();
    }

    @Override
    public void startingEventCloseDoors() {
        for (var i = DalaranSewersObjectTypes.DOOR1; i <= DalaranSewersObjectTypes.DOOR2; ++i) {
            spawnBGObject(i, BattlegroundConst.RespawnImmediately);
        }
    }

    @Override
    public void startingEventOpenDoors() {
        for (var i = DalaranSewersObjectTypes.DOOR1; i <= DalaranSewersObjectTypes.DOOR2; ++i) {
            doorOpen(i);
        }

        for (var i = DalaranSewersObjectTypes.BUFF1; i <= DalaranSewersObjectTypes.BUFF2; ++i) {
            spawnBGObject(i, 60);
        }

        events.ScheduleEvent(DalaranSewersEvents.WATERFALLWARNING, DalaranSewersData.WATERFALLTIMERMIN, DalaranSewersData.WATERFALLTIMERMAX);
        events.ScheduleEvent(DalaranSewersEvents.pipeKnockback, DalaranSewersData.PIPEKNOCKBACKFIRSTDELAY, 0, (byte) 0);

        spawnBGObject(DalaranSewersObjectTypes.WATER2, BattlegroundConst.RespawnImmediately);

        doorOpen(DalaranSewersObjectTypes.WATER1); // Turn off collision
        doorOpen(DalaranSewersObjectTypes.WATER2);

        // Remove effects of Demonic Circle Summon
        for (var pair : getPlayers().entrySet()) {
            var player = _GetPlayer(pair, "BattlegroundDS::StartingEventOpenDoors");

            if (player) {
                player.removeAura(DalaranSewersSpells.demonicCircle);
            }
        }
    }

    @Override
    public void handleAreaTrigger(Player player, int trigger, boolean entered) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        switch (trigger) {
            case 5347:
            case 5348:
                // Remove effects of Demonic Circle Summon
                player.removeAura(DalaranSewersSpells.demonicCircle);

                // Someone has get back into the pipes and the knockback has already been performed,
                // so we reset the knockback count for kicking the player again into the arena.
                events.ScheduleEvent(DalaranSewersEvents.pipeKnockback, DalaranSewersData.PIPEKNOCKBACKDELAY, 0, (byte) 0);

                break;
            default:
                super.handleAreaTrigger(player, trigger, entered);

                break;
        }
    }

    @Override
    public boolean setupBattleground() {
        var result = true;
        result &= addObject(DalaranSewersObjectTypes.DOOR1, DalaranSewersGameObjects.DOOR1, 1350.95f, 817.2f, 20.8096f, 3.15f, 0, 0, 0.99627f, 0.0862864f, BattlegroundConst.RespawnImmediately);
        result &= addObject(DalaranSewersObjectTypes.DOOR2, DalaranSewersGameObjects.DOOR2, 1232.65f, 764.913f, 20.0729f, 6.3f, 0, 0, 0.0310211f, -0.999519f, BattlegroundConst.RespawnImmediately);

        if (!result) {
            Logs.SQL.error("DalaranSewersArena: Failed to spawn door object!");

            return false;
        }

        // buffs
        result &= addObject(DalaranSewersObjectTypes.BUFF1, DalaranSewersGameObjects.BUFF1, 1291.7f, 813.424f, 7.11472f, 4.64562f, 0, 0, 0.730314f, -0.683111f, 120);
        result &= addObject(DalaranSewersObjectTypes.BUFF2, DalaranSewersGameObjects.BUFF2, 1291.7f, 768.911f, 7.11472f, 1.55194f, 0, 0, 0.700409f, 0.713742f, 120);

        if (!result) {
            Logs.SQL.error("DalaranSewersArena: Failed to spawn buff object!");

            return false;
        }

        result &= addObject(DalaranSewersObjectTypes.WATER1, DalaranSewersGameObjects.WATER1, 1291.56f, 790.837f, 7.1f, 3.14238f, 0, 0, 0.694215f, -0.719768f, 120);
        result &= addObject(DalaranSewersObjectTypes.WATER2, DalaranSewersGameObjects.WATER2, 1291.56f, 790.837f, 7.1f, 3.14238f, 0, 0, 0.694215f, -0.719768f, 120);
        result &= addCreature(DalaranSewersData.npcWaterSpout, DalaranSewersCreatureTypes.waterfallKnockback, 1292.587f, 790.2205f, 7.19796f, 3.054326f, TeamIds.Neutral, BattlegroundConst.RespawnImmediately);
        result &= addCreature(DalaranSewersData.npcWaterSpout, DalaranSewersCreatureTypes.PIPEKNOCKBACK1, 1369.977f, 817.2882f, 16.08718f, 3.106686f, TeamIds.Neutral, BattlegroundConst.RespawnImmediately);
        result &= addCreature(DalaranSewersData.npcWaterSpout, DalaranSewersCreatureTypes.PIPEKNOCKBACK2, 1212.833f, 765.3871f, 16.09484f, 0.0f, TeamIds.Neutral, BattlegroundConst.RespawnImmediately);

        if (!result) {
            Logs.SQL.error("DalaranSewersArena: Failed to spawn collision object!");

            return false;
        }

        return true;
    }

    @Override
    public void postUpdateImpl(int diff) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        events.ExecuteEvents(eventId ->
        {
            switch (eventId) {
                case DalaranSewersEvents.WaterfallWarning:
                    // Add the water
                    doorClose(DalaranSewersObjectTypes.WATER2);
                    events.ScheduleEvent(DalaranSewersEvents.waterfallOn, DalaranSewersData.WATERWARNINGDURATION, 0, (byte) 0);

                    break;
                case DalaranSewersEvents.WaterfallOn:
                    // Active collision and start knockback timer
                    doorClose(DalaranSewersObjectTypes.WATER1);
                    events.ScheduleEvent(DalaranSewersEvents.waterfallOff, DalaranSewersData.WATERFALLDURATION, 0, (byte) 0);
                    events.ScheduleEvent(DalaranSewersEvents.waterfallKnockback, DalaranSewersData.WATERFALLKNOCKBACKTIMER, 0, (byte) 0);

                    break;
                case DalaranSewersEvents.WaterfallOff:
                    // Remove collision and water
                    doorOpen(DalaranSewersObjectTypes.WATER1);
                    doorOpen(DalaranSewersObjectTypes.WATER2);
                    events.CancelEvent(DalaranSewersEvents.waterfallKnockback);
                    events.ScheduleEvent(DalaranSewersEvents.WATERFALLWARNING, DalaranSewersData.WATERFALLTIMERMIN, DalaranSewersData.WATERFALLTIMERMAX);

                    break;
                case DalaranSewersEvents.WaterfallKnockback: {
                    // Repeat knockback while the waterfall still active
                    var waterSpout = getBGCreature(DalaranSewersCreatureTypes.waterfallKnockback);

                    if (waterSpout) {
                        waterSpout.castSpell(waterSpout, DalaranSewersSpells.waterSpout, true);
                    }

                    events.ScheduleEvent(eventId, DalaranSewersData.WATERFALLKNOCKBACKTIMER);
                }

                break;
                case DalaranSewersEvents.PipeKnockback: {
                    for (var i = DalaranSewersCreatureTypes.PIPEKNOCKBACK1; i <= DalaranSewersCreatureTypes.PIPEKNOCKBACK2; ++i) {
                        var waterSpout = getBGCreature(i);

                        if (waterSpout) {
                            waterSpout.castSpell(waterSpout, DalaranSewersSpells.flush, true);
                        }
                    }
                }

                break;
            }
        });
    }
}
