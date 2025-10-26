package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_arena_stats")
public class CharacterArenaStat {
    @Id
    
    @Column("guid")
    private Integer guid;

    @Id
    
    @Column("slot")
    private Short slot;

    
    @Column("matchMakerRating")
    private Integer matchMakerRating;

}