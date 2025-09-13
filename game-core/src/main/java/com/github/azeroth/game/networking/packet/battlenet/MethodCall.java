package com.github.azeroth.game.networking.packet.battlenet;


public final class MethodCall {
    public long type;
    public long objectId;
    public int token;

    public int getServiceHash() {
        return (int) (type >>> 32);
    }

    public int getMethodId() {
        return (int) (type & 0xFFFFFFFF);
    }

    public void read(ByteBuffer data) {
        type = data.readUInt64();
        objectId = data.readUInt64();
        token = data.readUInt();
    }

    public void write(ByteBuffer data) {
        data.writeInt64(type);
        data.writeInt64(objectId);
        data.writeInt32(token);
    }

    public MethodCall clone() {
        MethodCall varCopy = new methodCall();

        varCopy.type = this.type;
        varCopy.objectId = this.objectId;
        varCopy.token = this.token;

        return varCopy;
    }
}
