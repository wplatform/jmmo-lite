package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public class SpellCastRequest {
    public ObjectGuid castID = ObjectGuid.EMPTY;

    public int spellID;
    public SpellCastVisual visual = new SpellCastVisual();

    public int sendCastFlags;
    public SpellTargetData target = new SpellTargetData();
    public MissileTrajectoryRequest missileTrajectory = new MissileTrajectoryRequest();
    public MovementInfo moveUpdate;
    public ArrayList<SpellWeight> weight = new ArrayList<>();
    public ArrayList<SpellCraftingReagent> optionalReagents = new ArrayList<>(3);
    public ArrayList<SpellExtraCurrencyCost> optionalCurrencies = new ArrayList<>(5);

    public Long craftingOrderID = null;
    public ObjectGuid craftingNPC = ObjectGuid.EMPTY;

    public int[] misc = new int[2];

    public final void read(WorldPacket data) {
        castID = data.readPackedGuid();
        misc[0] = data.readUInt32();
        misc[1] = data.readUInt32();
        spellID = data.readUInt32();

        visual.read(data);

        missileTrajectory.read(data);
        craftingNPC = data.readPackedGuid();

        var optionalCurrencies = data.readUInt32();
        var optionalReagents = data.readUInt32();

        for (var i = 0; i < optionalCurrencies; ++i) {

            optionalCurrencies.get(i).read(data);
        }

        sendCastFlags = data.readBit(5);
        var hasMoveUpdate = data.readBit();
        var weightCount = data.readBit(2);
        var hasCraftingOrderID = data.readBit();

        target.read(data);

        if (hasCraftingOrderID) {
            craftingOrderID = data.readUInt64();
        }

        for (var i = 0; i < optionalReagents; ++i) {
            optionalReagents.get(i).read(data);
        }

        if (hasMoveUpdate) {
            moveUpdate = MovementExtensions.readMovementInfo(data);
        }

        for (var i = 0; i < weightCount; ++i) {
            data.resetBitPos();
            SpellWeight weight = new SpellWeight();
            weight.type = data.readBit(2);
            weight.ID = data.readInt32();
            weight.quantity = data.readUInt32();
            weight.add(weight);
        }
    }
}
