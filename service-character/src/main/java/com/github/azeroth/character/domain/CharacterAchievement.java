package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter


@Table(name = "character_achievement")
public class CharacterAchievement {
    @Id
    @Column("guid")
    private Integer guid;

    @Id
    @Column("achievement")
    private Integer achievement;


    @Column("date")
    private Instant date;

}