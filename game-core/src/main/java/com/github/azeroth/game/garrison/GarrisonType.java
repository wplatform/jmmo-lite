package com.github.azeroth.game.garrison;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GarrisonType {
    GARRISON      (2),
    CLASS_ORDER   (3),
    WAR_CAMPAIGN  (9),
    COVENANT      (111);    
    
    
    public final int value;
}
