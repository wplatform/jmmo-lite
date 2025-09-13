package com.github.azeroth.game.movement.model;

import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.MovementFlag;
import com.github.azeroth.game.domain.unit.MovementFlag2;
import com.github.azeroth.game.domain.unit.MovementFlag3;
import lombok.Data;

@Data
public class MovementInfo {
    // common
    public ObjectGuid guid;
    public final EnumFlag<MovementFlag> flags = EnumFlag.of(MovementFlag.NONE);
    public final EnumFlag<MovementFlag2> flags2 = EnumFlag.of(MovementFlag2.NONE);
    public final EnumFlag<MovementFlag3> flags3 = EnumFlag.of(MovementFlag3.NONE);
    public final Position pos = new Position();
    public int time;
    public final TransportInfo transport = new TransportInfo();

    // swimming/flying
    public float pitch;

    public Inertia inertia;

    public final JumpInfo jump = new JumpInfo();
    // spline
    public float stepUpStartElevation;

    public AdvFlying advFlying;

    public ObjectGuid standingOnGameObjectGUID;
}



