package com.github.azeroth.game.ai;

// EnumUtils: DESCRIBE THIS
public enum EvadeReason {
    NoHostiles,     // the creature's threat list is empty
    Boundary,       // the creature has moved outside its evade boundary
    NoPath,         // the creature was unable to reach its target for over 5 seconds
    SequenceBreak,  // this is a boss and the pre-requisite encounters for engaging it are not defeated yet
    Other,          // anything else
}
