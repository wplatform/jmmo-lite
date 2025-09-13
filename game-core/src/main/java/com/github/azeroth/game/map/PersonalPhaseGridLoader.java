package com.github.azeroth.game.map;


import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.map.grid.NCell;
import com.github.azeroth.game.map.interfaces.*;


class PersonalPhaseGridLoader extends ObjectGridLoaderBase implements IGridNotifierCreature, IGridNotifierGameObject {
    private final ObjectGuid phaseOwner;
    private int phaseId;

    public PersonalPhaseGridLoader(NCell NCell, Map map, Cell cell, ObjectGuid phaseOwner, GridType gridType) {
        super(NCell, map, cell);
        phaseId = 0;
        phaseOwner = phaseOwner;
        setGridType(gridType);
    }    private gridType gridType = getGridType().values()[0];

    public final GridType getGridType() {
        return gridType;
    }

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<Creature> objs) {
        var cellCoord = i_cell.getCellCoord();
        var cell_guids = global.getObjectMgr().getCellPersonalObjectGuids(i_map.getId(), i_map.getDifficultyID(), phaseId, cellCoord.getId());

        if (cell_guids != null) {
            tangible.RefObject<Integer> tempRef_i_creatures = new tangible.RefObject<Integer>(i_creatures);
            this.<Creature>LoadHelper(cell_guids.creatures, cellCoord, tempRef_i_creatures, i_map, phaseId, phaseOwner);
            i_creatures = tempRef_i_creatures.refArgValue;
        }
    }

    public final void visit(list<GameObject> objs) {
        var cellCoord = i_cell.getCellCoord();
        var cell_guids = global.getObjectMgr().getCellPersonalObjectGuids(i_map.getId(), i_map.getDifficultyID(), phaseId, cellCoord.getId());

        if (cell_guids != null) {
            tangible.RefObject<Integer> tempRef_i_gameObjects = new tangible.RefObject<Integer>(i_gameObjects);
            this.<GameObject>LoadHelper(cell_guids.gameobjects, cellCoord, tempRef_i_gameObjects, i_map, phaseId, phaseOwner);
            i_gameObjects = tempRef_i_gameObjects.refArgValue;
        }
    }

    public final void load(int phaseId) {
        phaseId = phaseId;
        i_cell.data.celly = 0;

        for (int x = 0; x < MapDefine.MaxCells; ++x) {
            i_cell.data.cellx = x;

            for (int y = 0; y < MapDefine.MaxCells; ++y) {
                i_cell.data.celly = y;

                //Load creatures and game objects
                i_grid.visitGrid(x, y, this);
            }
        }
    }


}
