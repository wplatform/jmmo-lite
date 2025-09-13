package com.github.azeroth.game.movement;


import com.github.azeroth.game.movement.enums.MovementGeneratorType;

public final class MovementGeneratorInformation {
    public MovementGeneratortype type = MovementGeneratorType.values()[0];
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public String targetName;


    public MovementGeneratorInformation(MovementGeneratorType type, ObjectGuid targetGUID) {
        this(type, targetGUID, "");
    }

    public MovementGeneratorInformation() {
    }

    public MovementGeneratorInformation(MovementGeneratorType type, ObjectGuid targetGUID, String targetName) {
        type = type;
        targetGUID = targetGUID;
        targetName = targetName;
    }

    public MovementGeneratorInformation clone() {
        MovementGeneratorInformation varCopy = new MovementGeneratorInformation();

        varCopy.type = this.type;
        varCopy.targetGUID = this.targetGUID;
        varCopy.targetName = this.targetName;

        return varCopy;
    }
}
