package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.movement.MovementIOUtil;

import java.util.ArrayList;

public class SpellCastRequest {
    public ObjectGuid castID = ObjectGuid.EMPTY;

    public int spellID;
    public int spellXSpellVisualID;

    public int sendCastFlags;
    public SpellTargetData target = new SpellTargetData();
    public MissileTrajectoryRequest missileTrajectory = new MissileTrajectoryRequest();
    public MovementInfo moveUpdate;
    public ArrayList<SpellWeight> weights = new ArrayList<>();

    public ObjectGuid craftingNPC = ObjectGuid.EMPTY;

    public int[] misc = new int[2];

    public final void read(WorldPacket data) {
        castID = data.readPackedGuid();
        misc[0] = data.readUInt32();
        misc[1] = data.readUInt32();
        spellID = data.readUInt32();

        spellXSpellVisualID = data.readUInt32();
        missileTrajectory.read(data);
        craftingNPC = data.readPackedGuid();
        sendCastFlags = data.readBit(5);
        var hasMoveUpdate = data.readBit();
        var weightCount = data.readBit(2);
        target.read(data);

        if (hasMoveUpdate) {
            moveUpdate = MovementIOUtil.readMovementInfo(data);
        }

        for (var i = 0; i < weightCount; ++i) {
            data.resetBitPos();
            SpellWeight weight = new SpellWeight();
            weight.type = data.readBit(2);
            weight.ID = data.readInt32();
            weight.quantity = data.readUInt32();
            this.weights.add(weight);
        }
    }
}
