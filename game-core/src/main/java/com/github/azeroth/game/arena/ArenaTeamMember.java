package com.github.azeroth.game.arena;


import com.github.azeroth.game.entity.player.Player;

public class ArenaTeamMember {
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public String name;
    public byte class;
    public short weekGames;
    public short weekWins;
    public short seasonGames;
    public short seasonWins;
    public short personalRating;
    public short matchMakerRating;

    public final void modifyPersonalRating(Player player, int mod, int type) {
        if (personalRating + mod < 0) {
            personalRating = 0;
        } else {
            personalRating += (short) mod;
        }

        if (player) {
            player.setArenaTeamInfoField(ArenaTeam.getSlotByType(type), ArenaTeamInfoType.personalRating, personalRating);
            player.updateCriteria(CriteriaType.EarnPersonalArenaRating, personalRating, type);
        }
    }

    public final void modifyMatchmakerRating(int mod, int slot) {
        if (matchMakerRating + mod < 0) {
            matchMakerRating = 0;
        } else {
            matchMakerRating += (short) mod;
        }
    }
}
