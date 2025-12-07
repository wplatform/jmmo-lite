package com.github.azeroth.game.event;

import com.github.azeroth.game.world.WorldContext;
import org.springframework.context.ApplicationEvent;

public abstract class WorldEvent extends ApplicationEvent {
    protected final WorldContext worldContext;

    public WorldEvent(Object source) {
        super(source);
    }
}
