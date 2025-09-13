package com.github.azeroth.game.networking.packet.misc;


public class WorldServerInfo extends ServerPacket {
    public int difficultyID;
    public boolean isTournamentRealm;
    public boolean XRealmPvpAlert;

    public boolean blockExitingLoadingScreen; // when set to true, sending SMSG_UPDATE_OBJECT with CreateObject Self bit = true will not hide loading screen

    // instead it will be done after this packet is sent again with false in this bit and SMSG_UPDATE_OBJECT Values for player
    public Integer restrictedAccountMaxLevel = null;
    public Long restrictedAccountMaxMoney = null;
    public Integer instanceGroupSize = null;

    public WorldServerInfo() {
        super(ServerOpcode.WorldServerInfo);
        instanceGroupSize = new int ? ();

        restrictedAccountMaxLevel = new int ? ();
        restrictedAccountMaxMoney = new long ? ();
    }

    @Override
    public void write() {
        this.writeInt32(difficultyID);
        this.writeBit(isTournamentRealm);
        this.writeBit(XRealmPvpAlert);
        this.writeBit(blockExitingLoadingScreen);
        this.writeBit(restrictedAccountMaxLevel != null);
        this.writeBit(restrictedAccountMaxMoney != null);
        this.writeBit(instanceGroupSize != null);
        this.flushBits();

        if (restrictedAccountMaxLevel != null) {
            this.writeInt32(restrictedAccountMaxLevel.intValue());
        }

        if (restrictedAccountMaxMoney != null) {
            this.writeInt64(restrictedAccountMaxMoney.longValue());
        }

        if (instanceGroupSize != null) {
            this.writeInt32(instanceGroupSize.intValue());
        }
    }
}
