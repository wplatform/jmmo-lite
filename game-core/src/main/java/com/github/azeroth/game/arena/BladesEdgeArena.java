package com.github.azeroth.game.arena;


import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.entity.player.Player;

public class BladesEdgeArena extends Arena {
    public BladesEdgeArena(BattlegroundTemplate battlegroundTemplate) {
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
        for (var i = BladeEdgeObjectTypes.DOOR1; i <= BladeEdgeObjectTypes.DOOR4; ++i) {
            spawnBGObject(i, BattlegroundConst.RespawnImmediately);
        }

        for (var i = BladeEdgeObjectTypes.BUFF1; i <= BladeEdgeObjectTypes.BUFF2; ++i) {
            spawnBGObject(i, BattlegroundConst.RespawnOneDay);
        }
    }

    @Override
    public void startingEventOpenDoors() {
        for (var i = BladeEdgeObjectTypes.DOOR1; i <= BladeEdgeObjectTypes.DOOR4; ++i) {
            doorOpen(i);
        }

        taskScheduler.Schedule(duration.FromSeconds(5), task ->
        {
            for (var i = BladeEdgeObjectTypes.DOOR1; i <= BladeEdgeObjectTypes.DOOR2; ++i) {
                delObject(i);
            }
        });

        for (var i = BladeEdgeObjectTypes.BUFF1; i <= BladeEdgeObjectTypes.BUFF2; ++i) {
            spawnBGObject(i, 60);
        }
    }

    @Override
    public void handleAreaTrigger(Player player, int trigger, boolean entered) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        switch (trigger) {
            case 4538: // buff trigger?
            case 4539: // buff trigger?
                break;
            default:
                super.handleAreaTrigger(player, trigger, entered);

                break;
        }
    }

    @Override
    public boolean setupBattleground() {
        var result = true;
        result &= addObject(BladeEdgeObjectTypes.DOOR1, BladeEfgeGameObjects.DOOR1, 6287.277f, 282.1877f, 3.810925f, -2.260201f, 0, 0, 0.9044551f, -0.4265689f, BattlegroundConst.RespawnImmediately);
        result &= addObject(BladeEdgeObjectTypes.DOOR2, BladeEfgeGameObjects.DOOR2, 6189.546f, 241.7099f, 3.101481f, 0.8813917f, 0, 0, 0.4265689f, 0.9044551f, BattlegroundConst.RespawnImmediately);
        result &= addObject(BladeEdgeObjectTypes.DOOR3, BladeEfgeGameObjects.DOOR3, 6299.116f, 296.5494f, 3.308032f, 0.8813917f, 0, 0, 0.4265689f, 0.9044551f, BattlegroundConst.RespawnImmediately);
        result &= addObject(BladeEdgeObjectTypes.DOOR4, BladeEfgeGameObjects.DOOR4, 6177.708f, 227.3481f, 3.604374f, -2.260201f, 0, 0, 0.9044551f, -0.4265689f, BattlegroundConst.RespawnImmediately);

        if (!result) {
            Logs.SQL.error("BatteGroundBE: Failed to spawn door object!");

            return false;
        }

        result &= addObject(BladeEdgeObjectTypes.BUFF1, BladeEfgeGameObjects.BUFF1, 6249.042f, 275.3239f, 11.22033f, -1.448624f, 0, 0, 0.6626201f, -0.7489557f, 120);
        result &= addObject(BladeEdgeObjectTypes.BUFF2, BladeEfgeGameObjects.BUFF2, 6228.26f, 249.566f, 11.21812f, -0.06981307f, 0, 0, 0.03489945f, -0.9993908f, 120);

        if (!result) {
            Logs.SQL.error("BladesEdgeArena: Failed to spawn buff object!");

            return false;
        }

        return true;
    }
}
