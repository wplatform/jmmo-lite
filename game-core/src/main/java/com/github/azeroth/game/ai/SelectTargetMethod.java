package com.github.azeroth.game.ai;

// Selection method used by SelectTarget
public enum SelectTargetMethod {
    Random,      // just pick a random target
    MaxThreat,   // prefer targets higher in the threat list
    MinThreat,   // prefer targets lower in the threat list
    MaxDistance, // prefer targets further from us
    MinDistance  // prefer targets closer to us
}
