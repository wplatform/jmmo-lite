package com.github.azeroth.game.networking.packet.query;


public class QueryNPCTextResponse extends ServerPacket {
    public int textID;
    public boolean allow;
    public float[] probabilities = new float[SharedConst.MaxNpcTextOptions];
    public int[] broadcastTextID = new int[SharedConst.MaxNpcTextOptions];

    public QueryNPCTextResponse() {
        super(ServerOpcode.QueryNpcTextResponse);
    }

    @Override
    public void write() {
        this.writeInt32(textID);
        this.writeBit(allow);

        this.writeInt32(Allow ? SharedConst.MaxNpcTextOptions * (4 + 4) : 0);

        if (allow) {
            for (int i = 0; i < SharedConst.MaxNpcTextOptions; ++i) {
                this.writeFloat(Probabilities[i]);
            }

            for (int i = 0; i < SharedConst.MaxNpcTextOptions; ++i) {
                this.writeInt32(BroadcastTextID[i]);
            }
        }
    }
}
