package com.github.azeroth.game.map;


import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.game.battleground.Battleground;
import com.github.azeroth.game.battleground.BattlegroundScript;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.listener.basescripts.BattlegroundMapScript;
import com.github.azeroth.game.world.World;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BattlegroundMap extends Map {

    private Battleground bg;

    private BattlegroundScript battlegroundScript;
    private BattlegroundMapScript battlegroundMapScript;


    public BattlegroundMap(World world, int id, int expiry, int instanceId, Difficulty spawnMode) {
        super(world, id, expiry, instanceId, spawnMode);
        initVisibilityDistance();
    }

    @Override
    public void initVisibilityDistance() {
        setVisibleDistance(isBattleArena() ? world.getWorldMgr().getMaxVisibleDistanceInArenas() : world.getWorldMgr().getMaxVisibleDistanceInBG());
        setVisibilityNotifyPeriod(isBattleArena() ? world.getWorldMgr().getVisibilityNotifyPeriodInArenas() : world.getWorldMgr().getVisibilityNotifyPeriodInBG());
    }

    @Override
    public TransferAbortParams cannotEnter(Player player) {
        if (player.getMap() == this) {
            Logs.MAPS.error("BGMap:CannotEnter - player {0} is already in map!", player.getGUID().toString());
            return new TransferAbortParams(TransferAbortReason.error);
        }

        if (player.getBattlegroundId() != getInstanceId()) {
            return new TransferAbortParams(TransferAbortReason.LockedToDifferentInstance);
        }

        return super.cannotEnter(player);
    }


    @Override
    public boolean addPlayerToMap(Player player) {
        return addPlayerToMap(player, true);
    }

    @Override
    public boolean addPlayerToMap(Player player, boolean initPlayer) {
        player.setInstanceValid(true);

        return super.addPlayerToMap(player, initPlayer);
    }

    @Override
    public void removePlayerFromMap(Player player, boolean remove) {
        Log.outInfo(LogFilter.Maps, "MAP: Removing player '{0}' from bg '{1}' of map '{2}' before relocating to another map", player.getName(), getInstanceId(), getMapName());

        super.removePlayerFromMap(player, remove);
    }

    public final void setUnload() {
        setUnloadTimer(1);
    }

    @Override
    public void removeAllPlayers() {
        if (havePlayers()) {
            for (var player : getActivePlayers()) {
                if (!player.isBeingTeleportedFar()) {
                    player.teleportTo(player.getBattlegroundEntryPoint());
                }
            }
        }
    }

    public final Battleground getBG() {
        return bg;
    }

    public final void setBG(Battleground bg) {
        bg = bg;
    }
}
