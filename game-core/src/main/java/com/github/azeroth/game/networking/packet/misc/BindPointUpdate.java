package com.github.azeroth.game.networking.packet.misc;


public class BindPointUpdate extends ServerPacket {
    public int bindMapID = (int) 0xFFFFFFFF;
    public Vector3 bindPosition;
    public int bindAreaID;

    public BindPointUpdate() {
        super(ServerOpcode.BindPointUpdate);
    }

    @Override
    public void write() {
        this.writeVector3(bindPosition);
        this.writeInt32(bindMapID);
        this.writeInt32(bindAreaID);
    }
}

//Structs

