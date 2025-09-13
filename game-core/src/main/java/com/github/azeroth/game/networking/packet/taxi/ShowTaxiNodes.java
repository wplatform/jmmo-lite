package com.github.azeroth.game.networking.packet.taxi;


public class ShowTaxiNodes extends ServerPacket {
    public ShowTaxiNodeswindowInfo windowInfo = null;
    public byte[] canLandNodes = null; // Nodes known by player
    public byte[] canUseNodes = null; // Nodes available for use - this can temporarily disable a known node

    public ShowTaxiNodes() {
        super(ServerOpcode.ShowTaxiNodes);
    }

    @Override
    public void write() {
        this.writeBit(windowInfo != null);
        this.flushBits();

        this.writeInt32(canLandNodes.length);
        this.writeInt32(canUseNodes.length);

        if (windowInfo != null) {
            this.writeGuid(windowInfo.getValue().unitGUID);
            this.writeInt32(windowInfo.getValue().currentNode);
        }

        for (var node : canLandNodes) {
            this.writeInt8(node);
        }

        for (var node : canUseNodes) {
            this.writeInt8(node);
        }
    }
}
