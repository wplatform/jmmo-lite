package com.github.azeroth.game.networking.packet.taxi;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class ShowTaxiNodes extends ServerPacket {
    public ShowTaxiNodesWindowInfo windowInfo = null;
    public byte[] canLandNodes = null; // Nodes known by player
    public byte[] canUseNodes = null; // Nodes available for use - this can temporarily disable a known node

    public ShowTaxiNodes() {
        super(ServerOpCode.SMSG_SHOW_TAXI_NODES);
    }

    @Override
    public void write() {
        this.writeBit(windowInfo != null);
        this.flushBits();

        this.writeInt32(canLandNodes.length);
        this.writeInt32(canUseNodes.length);

        if (windowInfo != null) {
            this.writeGuid(windowInfo.unitGUID);
            this.writeInt32(windowInfo.currentNode);
        }

        for (var node : canLandNodes) {
            this.writeInt8(node);
        }

        for (var node : canUseNodes) {
            this.writeInt8(node);
        }
    }
}
