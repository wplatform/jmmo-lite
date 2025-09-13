package com.github.azeroth.game.networking.packet.misc;


public class ExplorationExperience extends ServerPacket {
    public int experience;
    public int areaID;

    public ExplorationExperience(int experience, int areaID) {
        super(ServerOpcode.ExplorationExperience);
        experience = experience;
        areaID = areaID;
    }

    @Override
    public void write() {
        this.writeInt32(areaID);
        this.writeInt32(experience);
    }
}
