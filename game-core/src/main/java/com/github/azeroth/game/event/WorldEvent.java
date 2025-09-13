package com.github.azeroth.game.event;

import com.github.azeroth.game.world.WorldContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public abstract class WorldEvent extends ApplicationEvent {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private WorldContext worldContext;

    public WorldEvent(Object source) {
        super(source);
    }

    public WorldEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
