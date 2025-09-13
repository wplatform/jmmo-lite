package com.github.azeroth.game.arena;


import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.entity.player.Player;

public class NagrandArena extends Arena {
    public NagrandArena(BattlegroundTemplate battlegroundTemplate) {
        super(battlegroundTemplate);
    }


    @Override
    public void postUpdateImpl(int diff) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        taskScheduler.update(diff);
    }

    @Override
    public void startingEventCloseDoors() {
        for (var i = NagrandArenaObjectTypes.DOOR1; i <= NagrandArenaObjectTypes.DOOR4; ++i) {
            spawnBGObject(i, BattlegroundConst.RespawnImmediately);
        }
    }

    @Override
    public void startingEventOpenDoors() {
        for (var i = NagrandArenaObjectTypes.DOOR1; i <= NagrandArenaObjectTypes.DOOR4; ++i) {
            doorOpen(i);
        }

        taskScheduler.Schedule(duration.FromSeconds(5), task ->
        {
            for (var i = NagrandArenaObjectTypes.DOOR1; i <= NagrandArenaObjectTypes.DOOR2; ++i) {
                delObject(i);
            }
        });

        for (var i = NagrandArenaObjectTypes.BUFF1; i <= NagrandArenaObjectTypes.BUFF2; ++i) {
            spawnBGObject(i, 60);
        }
    }


    @Override
    public void handleAreaTrigger(Player player, int trigger, boolean entered) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        switch (trigger) {
            case 4536: // buff trigger?
            case 4537: // buff trigger?
                break;
            default:
                super.handleAreaTrigger(player, trigger, entered);

                break;
        }
    }

    @Override
    public boolean setupBattleground() {
        var result = true;
        result &= addObject(NagrandArenaObjectTypes.DOOR1, NagrandArenaObjects.DOOR1, 4031.854f, 2966.833f, 12.6462f, -2.648788f, 0, 0, 0.9697962f, -0.2439165f, BattlegroundConst.RespawnImmediately);
        result &= addObject(NagrandArenaObjectTypes.DOOR2, NagrandArenaObjects.DOOR2, 4081.179f, 2874.97f, 12.39171f, 0.4928045f, 0, 0, 0.2439165f, 0.9697962f, BattlegroundConst.RespawnImmediately);
        result &= addObject(NagrandArenaObjectTypes.DOOR3, NagrandArenaObjects.DOOR3, 4023.709f, 2981.777f, 10.70117f, -2.648788f, 0, 0, 0.9697962f, -0.2439165f, BattlegroundConst.RespawnImmediately);
        result &= addObject(NagrandArenaObjectTypes.DOOR4, NagrandArenaObjects.DOOR4, 4090.064f, 2858.438f, 10.23631f, 0.4928045f, 0, 0, 0.2439165f, 0.9697962f, BattlegroundConst.RespawnImmediately);

        if (!result) {
            Logs.SQL.error("NagrandArena: Failed to spawn door object!");

            return false;
        }

        result &= addObject(NagrandArenaObjectTypes.BUFF1, NagrandArenaObjects.BUFF1, 4009.189941f, 2895.250000f, 13.052700f, -1.448624f, 0, 0, 0.6626201f, -0.7489557f, 120);
        result &= addObject(NagrandArenaObjectTypes.BUFF2, NagrandArenaObjects.BUFF2, 4103.330078f, 2946.350098f, 13.051300f, -0.06981307f, 0, 0, 0.03489945f, -0.9993908f, 120);

        if (!result) {
            Logs.SQL.error("NagrandArena: Failed to spawn buff object!");

            return false;
        }

        return true;
    }
}
