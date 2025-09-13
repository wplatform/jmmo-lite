package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.interfaces.*;

class ObjectGridUnloader implements IGridNotifierWorldObject {
    public ObjectGridUnloader() {
        this(gridType.Grid);
    }

    public ObjectGridUnloader(GridType gridType) {
        setGridType(gridType);
    }    private gridType gridType = getGridType().values()[0];

    public final GridType getGridType() {
        return gridType;
    }

    public final void setGridType(GridType value) {
        gridType = value;
    }

    public final void visit(list<WorldObject> objs) {
        for (var i = 0; i < objs.size(); ++i) {
            var obj = objs.get(i);

            if (obj.isTypeId(TypeId.Corpse)) {
                continue;
            }

            //Some creatures may summon other temp summons in cleanupsBeforeDelete()
            //So we need this even after cleaner (maybe we can remove cleaner)
            //Example: Flame Leviathan Turret 33139 is summoned when a creature is deleted
            //TODO: Check if that script has the correct logic. Do we really need to summons something before deleting?
            obj.cleanupsBeforeDelete();
            obj.close();
        }
    }




}
