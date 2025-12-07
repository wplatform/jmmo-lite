package com.github.azeroth.game.event;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.map.Map;
import lombok.Getter;

@Getter
public class GameEvent extends WorldEvent {

    private final int gameEventId;
    private final WorldObject source;
    private final WorldObject target;
    private final Map map;

    public GameEvent(int gameEventId, Map map) {
        this(gameEventId, null, null, map);
    }

    public GameEvent(int gameEventId, WorldObject source) {
        this(gameEventId, source, null, null);
    }

    public GameEvent(int gameEventId, WorldObject source, WorldObject target) {
        this(gameEventId, source, target, null);
    }

    public GameEvent(int gameEventId, WorldObject source, WorldObject target, Map map) {
        super(source);
        this.gameEventId = gameEventId;
        this.source = source;
        this.target = target;
        this.map = map;
    }
}
