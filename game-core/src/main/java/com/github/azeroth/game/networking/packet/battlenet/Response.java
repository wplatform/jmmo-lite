package com.github.azeroth.game.networking.packet.battlenet;


import com.github.azeroth.game.networking.ServerPacket;

public class Response extends ServerPacket {
    public BattlenetRpcErrorCode bnetStatus = BattlenetRpcErrorCode.Ok;
    public methodCall method = new methodCall();
    public byteBuffer data = new byteBuffer();

    public response() {
        super(ServerOpcode.BattlenetResponse);
    }

    @Override
    public void write() {
        this.writeInt32((int) bnetStatus.getValue());
        method.write(this);
        this.writeInt32(data.getSize());
        this.writeBytes(data);
    }
}
