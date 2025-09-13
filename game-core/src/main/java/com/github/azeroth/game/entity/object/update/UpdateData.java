package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.common.Assert;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class UpdateData {
    private final int mapId;
    private int blockCount;
    private final Set<ObjectGuid> destroyGUIDs = new HashSet<>();
    private final Set<ObjectGuid> outOfRangeGUIDs = new HashSet<>();
    private ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();


    // Move constructor equivalent using Java's copy constructor pattern
    public UpdateData(UpdateData other) {
        this.mapId = other.mapId;
        this.blockCount = other.blockCount;
        this.outOfRangeGUIDs.addAll(other.outOfRangeGUIDs);
        this.destroyGUIDs.addAll(other.destroyGUIDs);
        this.buffer = other.buffer.duplicate();
    }

    public void addDestroyObject(ObjectGuid guid) {
        destroyGUIDs.add(guid);
    }

    public void addOutOfRangeGUID(Set<ObjectGuid> guids) {
        outOfRangeGUIDs.addAll(guids);
    }

    public void addOutOfRangeGUID(ObjectGuid guid) {
        outOfRangeGUIDs.add(guid);
    }

    public void addUpdateBlock() {
        blockCount++;
    }

    public WorldPacket buildPacket() {

        try {
            // Calculate packet capacity
            int estimatedSize = 4 + 2 + 1 + (2 + 4 + 17 * (destroyGUIDs.size() + outOfRangeGUIDs.size())) + buffer.readableBytes();

            WorldPacket packet =WorldPacket.newServerToClient(ServerOpCode.SMSG_UPDATE_OBJECT, estimatedSize);

            // Write packet content
            packet.writeInt16(mapId);
            packet.writeInt32(blockCount);
            packet.writeBit(true); // unk

            boolean hasGuidData = !outOfRangeGUIDs.isEmpty() || !destroyGUIDs.isEmpty();
            if (packet.writeBit(hasGuidData)) {
                packet.writeInt16(destroyGUIDs.size());
                packet.writeInt32(destroyGUIDs.size() + outOfRangeGUIDs.size());

                for (ObjectGuid guid : destroyGUIDs) {
                    packet.writeGuid(guid);
                }

                for (ObjectGuid guid : outOfRangeGUIDs) {
                    packet.writeGuid(guid);
                }
            }

            packet.writeInt32(buffer.readableBytes());
            packet.writeBytes(buffer);
            return packet;
        } finally {
            buffer.release();
        }
    }

    public boolean hasData() {
        return blockCount > 0 || !outOfRangeGUIDs.isEmpty() || !destroyGUIDs.isEmpty();
    }

    public void clear() {
        blockCount = 0;
        outOfRangeGUIDs.clear();
        destroyGUIDs.clear();
        buffer.clear();
    }

    public void release() {
        buffer.release();
    }
}
