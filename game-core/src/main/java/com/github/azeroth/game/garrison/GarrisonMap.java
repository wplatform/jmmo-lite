package com.github.azeroth.game.garrison;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.map.grid.NCell;

class GarrisonMap extends Map {
    private final ObjectGuid owner;
    private Player loadingPlayer; // @workaround Player is not registered in ObjectAccessor during login


    public GarrisonMap(int id, long expiry, int instanceId, ObjectGuid owner) {
        super(id, expiry, instanceId, Difficulty.NORMAL);
        owner = owner;
        initVisibilityDistance();
    }

    @Override
    public void loadGridObjects(NCell NCell, Cell cell) {
        super.loadGridObjects(NCell, cell);

        GarrisonGridLoader loader = new GarrisonGridLoader(NCell, this, cell);
        loader.loadN();
    }

    public final Garrison getGarrison() {
        if (loadingPlayer) {
            return loadingPlayer.getGarrison();
        }

        var owner = global.getObjAccessor().findConnectedPlayer(owner);

        if (owner) {
            return owner.getGarrison();
        }

        return null;
    }

    @Override
    public void initVisibilityDistance() {
        //init visibility distance for instances
        setVisibleDistance(global.getWorldMgr().getMaxVisibleDistanceInInstances());
        setVisibilityNotifyPeriod(global.getWorldMgr().getVisibilityNotifyPeriodInInstances());
    }


    @Override
    public boolean addPlayerToMap(Player player) {
        return addPlayerToMap(player, true);
    }

    @Override
    public boolean addPlayerToMap(Player player, boolean initPlayer) {
        if (Objects.equals(player.getGUID(), owner)) {
            loadingPlayer = player;
        }

        var result = super.addPlayerToMap(player, initPlayer);

        if (Objects.equals(player.getGUID(), owner)) {
            loadingPlayer = null;
        }

        return result;
    }
}
