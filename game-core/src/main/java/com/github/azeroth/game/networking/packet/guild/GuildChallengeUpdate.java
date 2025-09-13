package com.github.azeroth.game.networking.packet.guild;


public class GuildChallengeUpdate extends ServerPacket {
    public int[] currentCount = new int[GuildConst.ChallengesTypes];
    public int[] maxCount = new int[GuildConst.ChallengesTypes];
    public int[] gold = new int[GuildConst.ChallengesTypes];
    public int[] maxLevelGold = new int[GuildConst.ChallengesTypes];

    public GuildChallengeUpdate() {
        super(ServerOpcode.GuildChallengeUpdate);
    }

    @Override
    public void write() {
        for (var i = 0; i < GuildConst.ChallengesTypes; ++i) {
            this.writeInt32(CurrentCount[i]);
        }

        for (var i = 0; i < GuildConst.ChallengesTypes; ++i) {
            this.writeInt32(MaxCount[i]);
        }

        for (var i = 0; i < GuildConst.ChallengesTypes; ++i) {
            this.writeInt32(MaxLevelGold[i]);
        }

        for (var i = 0; i < GuildConst.ChallengesTypes; ++i) {
            this.writeInt32(Gold[i]);
        }
    }
}
