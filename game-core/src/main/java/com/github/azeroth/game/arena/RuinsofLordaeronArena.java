package com.github.azeroth.game.arena;


import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.entity.player.Player;

class RuinsofLordaeronArena extends Arena {
    public RuinsofLordaeronArena(BattlegroundTemplate battlegroundTemplate) {
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
    public boolean setupBattleground() {
        var result = true;
        result &= addObject(RuinsofLordaeronObjectTypes.DOOR1, RuinsofLordaeronObjectTypes.DOOR1, 1293.561f, 1601.938f, 31.60557f, -1.457349f, 0, 0, -0.6658813f, 0.7460576f);
        result &= addObject(RuinsofLordaeronObjectTypes.DOOR2, RuinsofLordaeronObjectTypes.DOOR2, 1278.648f, 1730.557f, 31.60557f, 1.684245f, 0, 0, 0.7460582f, 0.6658807f);

        if (!result) {
            Logs.SQL.error("RuinsofLordaeronArena: Failed to spawn door object!");

            return false;
        }

        result &= addObject(RuinsofLordaeronObjectTypes.BUFF1, RuinsofLordaeronObjectTypes.BUFF1, 1328.719971f, 1632.719971f, 36.730400f, -1.448624f, 0, 0, 0.6626201f, -0.7489557f, 120);
        result &= addObject(RuinsofLordaeronObjectTypes.BUFF2, RuinsofLordaeronObjectTypes.BUFF2, 1243.300049f, 1699.170044f, 34.872601f, -0.06981307f, 0, 0, 0.03489945f, -0.9993908f, 120);

        if (!result) {
            Logs.SQL.error("RuinsofLordaeronArena: Failed to spawn buff object!");

            return false;
        }

        return true;
    }

    @Override
    public void startingEventCloseDoors() {
        for (var i = RuinsofLordaeronObjectTypes.DOOR1; i <= RuinsofLordaeronObjectTypes.DOOR2; ++i) {
            spawnBGObject(i, BattlegroundConst.RespawnImmediately);
        }
    }

    @Override
    public void startingEventOpenDoors() {
        for (var i = RuinsofLordaeronObjectTypes.DOOR1; i <= RuinsofLordaeronObjectTypes.DOOR2; ++i) {
            doorOpen(i);
        }

        taskScheduler.Schedule(duration.FromSeconds(5), task ->
        {
            for (var i = RuinsofLordaeronObjectTypes.DOOR1; i <= RuinsofLordaeronObjectTypes.DOOR2; ++i) {
                delObject(i);
            }
        });

        for (var i = RuinsofLordaeronObjectTypes.BUFF1; i <= RuinsofLordaeronObjectTypes.BUFF2; ++i) {
            spawnBGObject(i, 60);
        }
    }

    @Override
    public void handleAreaTrigger(Player player, int trigger, boolean entered) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        switch (trigger) {
            case 4696: // buff trigger?
            case 4697: // buff trigger?
                break;
            default:
                super.handleAreaTrigger(player, trigger, entered);

                break;
        }
    }
}
