package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.map.interfaces.*;

public class DelayedUnitRelocation implements IGridNotifierCreature, IGridNotifierPlayer {
    private final Map map;
    private final Cell cell;
    private final CellCoord p;
    private final float radius;

    public DelayedUnitRelocation(Cell c, CellCoord pair, Map map, float radius, GridType gridType) {
        map = map;
        cell = c;
        p = pair;
        radius = radius;
        setGridType(gridType);
    }

    public final GridType getGridType() {
        return gridType;
    }    private gridType gridType = getGridType().values()[0];

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<Creature> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var creature = objs.get(i);

            if (!creature.isNeedNotify(NotifyFlag.VisibilityChanged)) {
                continue;
            }

            CreatureRelocationNotifier relocate = new CreatureRelocationNotifier(creature, gridType.All);

            cell.visit(p, relocate, map, creature, radius);
        }
    }

    public final void visit(list<Player> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var player = objs.get(i);
            var viewPoint = player.getSeerView();

            if (!viewPoint.isNeedNotify(NotifyFlag.VisibilityChanged)) {
                continue;
            }

            if (player != viewPoint && !viewPoint.getLocation().isPositionValid()) {
                continue;
            }

            var relocate = new PlayerRelocationNotifier(player, gridType.All);
            Cell.visitGrid(viewPoint, relocate, radius, false);

            relocate.sendToSelf();
        }
    }




}
