package com.github.azeroth.game.networking.packet.misc;


public class LevelUpInfo extends ServerPacket {
    public int level = 0;
    public int healthDelta = 0;
    public int[] powerDelta = new int[PowerType.MaxPerClass.getValue()];
    public int[] statDelta = new int[Stats.max.getValue()];
    public int numNewTalents;
    public int numNewPvpTalentSlots;

    public LevelUpInfo() {
        super(ServerOpcode.LevelUpInfo);
    }

    @Override
    public void write() {
        this.writeInt32(level);
        this.writeInt32(healthDelta);

        for (var power : powerDelta) {
            this.writeInt32(power);
        }

        for (var stat : statDelta) {
            this.writeInt32(stat);
        }

        this.writeInt32(numNewTalents);
        this.writeInt32(numNewPvpTalentSlots);
    }
}
