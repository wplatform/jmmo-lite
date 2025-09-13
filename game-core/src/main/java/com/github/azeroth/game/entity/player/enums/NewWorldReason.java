package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum NewWorldReason {
    NEW_WORLD_NORMAL(16),   // Normal map change
    NEW_WORLD_SEAMLESS(21);   // Teleport to another map without a loading screen, used for outdoor scenarios
    public final int value;
}
