package com.github.azeroth.game.movement.enums;

public enum MovementGeneratorType {
    IDLE,     // IdleMovementGenerator.h
    RANDOM,     // RandomMovementGenerator.h
    WAYPOINT,     // WaypointMovementGenerator.h
    CONFUSED,     // ConfusedMovementGenerator.h
    CHASE,     // ChaseMovementGenerator.h
    HOME,     // HomeMovementGenerator.h
    FLIGHT,     // FlightPathMovementGenerator.h
    POINT,     // PointMovementGenerator.h
    FLEEING,     // FleeingMovementGenerator.h
    DISTRACT,    // IdleMovementGenerator.h
    ASSISTANCE,    // PointMovementGenerator.h
    ASSISTANCE_DISTRACT,    // IdleMovementGenerator.h
    TIMED_FLEEING,    // FleeingMovementGenerator.h
    FOLLOW,    // FollowMovementGenerator.h
    ROTATE,    // IdleMovementGenerator.h
    EFFECT,
    SPLINE_CHAIN,    // SplineChainMovementGenerator.h
    FORMATION;    // FormationMovementGenerator.h
    public static final byte MAX_DB_MOTION_TYPE = (byte) CONFUSED.ordinal();
}
