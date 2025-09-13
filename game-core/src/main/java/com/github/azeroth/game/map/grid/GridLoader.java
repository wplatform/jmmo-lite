package com.github.azeroth.game.map.grid;


import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.spawn.SpawnData;
import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.globals.ObjectManager;
import com.github.azeroth.game.map.Map;


public class GridLoader {

    public Cell cell;
    public NGrid grid;
    public Map map;
    public int gameObjects;
    public int creatures;
    public int corpses;
    public int areaTriggers;

    public ObjectManager objectManager;

    public GridLoader(ObjectManager objectManager, NGrid grid, Map map, Cell cell) {
        this.objectManager = objectManager;
        this.grid = grid;
        this.map = map;
        this.cell = cell;

    }

    public final void loadN() {
        creatures = 0;
        gameObjects = 0;
        corpses = 0;
        areaTriggers = 0;




        var gridSpawnData = objectManager.getGridObjectGuids(map.getId(), map.getDifficultyID(), cell.getGridId());


        for (SpawnData data : gridSpawnData.spawnData) {

            if (!map.shouldBeSpawnedOnGridLoad(data.spawnId)) {
                continue;
            }

            var object = switch (data.type) {
                case CREATURE -> {
                    Creature creature = new Creature();
                    boolean isOk = creature.loadFromDB(data.spawnId, map, false, false);
                    yield isOk ? creature : null;
                }
                case GAME_OBJECT -> {
                    GameObject gameObject = new GameObject();
                    boolean isOk = gameObject.loadFromDB(data.spawnId, map, false, false);
                    yield isOk ? gameObject : null;
                }
                case AREA_TRIGGER -> {
                    AreaTrigger areaTrigger = new AreaTrigger();
                    boolean isOk = areaTrigger.loadFromDB(data.spawnId, map, false, false);
                    yield isOk ? areaTrigger : null;
                }
                case TYPES_WITH_DATA -> null;
                case NUM_SPAWN_TYPES -> null;
            };

            if (object == null) {
                continue;
            }

            map.addToGrid(object, cell);
            object.addToWorld();
            if (object.isActiveObject())
                map.addToActive(object);


            switch (data.type) {
                case CREATURE -> creatures++;
                case AREA_TRIGGER -> areaTriggers++;
                case GAME_OBJECT -> gameObjects++;
            }
        }

        Logs.MAPS.debug("{} GameObjects, {} Creatures, {} AreaTriggers, and {} Corpses/Bones loaded for grid {} on map {}",
                gameObjects, creatures, areaTriggers, corpses, grid.getGridId(), map.getId());
    }


}


