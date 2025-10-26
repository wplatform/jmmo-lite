package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "arena_team_member")
public class ArenaTeamMember {
    @Id

    @Column("arenaTeamId")
    private Integer arenaTeamId;

    @Id

    @Column("guid")
    private Integer guid;


    @Column("weekGames")
    private Integer weekGames;


    @Column("weekWins")
    private Integer weekWins;


    @Column("seasonGames")
    private Integer seasonGames;


    @Column("seasonWins")
    private Integer seasonWins;


    @Column("personalRating")
    private Integer personalRating;

}