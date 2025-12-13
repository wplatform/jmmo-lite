package com.github.azeroth.game.movement;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
public final class MovementGeneratorInformation {
    public MovementGeneratorType type;
    public ObjectGuid targetGUID;
    public String targetName;


    public MovementGeneratorInformation(MovementGeneratorType type, ObjectGuid targetGUID) {
        this(type, targetGUID, "");
    }
}
