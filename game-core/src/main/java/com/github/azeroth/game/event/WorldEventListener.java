package com.github.azeroth.game.event;

import org.springframework.context.ApplicationListener;

public interface WorldEventListener extends ApplicationListener<WorldEvent> {

    void onWorldEvent(WorldEvent event);
}
