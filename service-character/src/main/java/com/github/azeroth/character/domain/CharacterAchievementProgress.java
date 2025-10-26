package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter


@Table(name = "character_achievement_progress")
public class CharacterAchievementProgress {
    @Id
    @Column("guid")
    private Integer guid;

    @Id
    @Column("criteria")
    private Integer criteria;

    @Column("counter")
    private Integer counter;

    
    @Column("date")
    private Instant date;

}