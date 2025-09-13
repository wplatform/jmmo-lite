package com.github.azeroth.game.networking.packet.query;


public class QueryGameObjectResponse extends ServerPacket {
    public int gameObjectID;
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public boolean allow;
    public GameObjectstats stats;

    public QueryGameObjectResponse() {
        super(ServerOpcode.QueryGameObjectResponse);
    }

    @Override
    public void write() {
        this.writeInt32(gameObjectID);
        this.writeGuid(guid);
        this.writeBit(allow);
        this.flushBits();

        ByteBuffer statsData = new byteBuffer();

        if (allow) {
            statsData.writeInt32(stats.type);
            statsData.writeInt32(stats.displayID);

            for (var i = 0; i < 4; i++) {
                statsData.writeCString(stats.name.charAt(i));
            }

            statsData.writeCString(stats.iconName);
            statsData.writeCString(stats.castBarCaption);
            statsData.writeCString(stats.unkString);

            for (int i = 0; i < SharedConst.MaxGOData; i++) {
                statsData.writeInt32(stats.Data[i]);
            }

            statsData.writeFloat(stats.size);
            statsData.writeInt8((byte) stats.questItems.size());

            for (var questItem : stats.questItems) {
                statsData.writeInt32(questItem);
            }

            statsData.writeInt32(stats.contentTuningId);
        }

        this.writeInt32(statsData.getSize());

        if (statsData.getSize() != 0) {
            this.writeBytes(statsData);
        }
    }
}
