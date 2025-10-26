package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "arena_team")
public class ArenaTeam {
    @Id
    
    @Column("arenaTeamId")
    private int id;

    @Column("name")
    private String name;

    
    @Column("captainGuid")
    private Integer captainGuid;

    
    @Column("type")
    private Short type;

    
    @Column("rating")
    private Integer rating;

    
    @Column("seasonGames")
    private Integer seasonGames;

    
    @Column("seasonWins")
    private Integer seasonWins;

    
    @Column("weekGames")
    private Integer weekGames;

    
    @Column("weekWins")
    private Integer weekWins;

    
    @Column("rank")
    private Integer rank;

    
    @Column("backgroundColor")
    private Integer backgroundColor;

    
    @Column("emblemStyle")
    private Short emblemStyle;

    
    @Column("emblemColor")
    private Integer emblemColor;

    
    @Column("borderStyle")
    private Short borderStyle;

    
    @Column("borderColor")
    private Integer borderColor;

}