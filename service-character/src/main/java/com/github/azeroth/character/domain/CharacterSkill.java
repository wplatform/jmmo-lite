package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_skills")
public class CharacterSkill {
    @Id
    @Column("guid")
    private Long guid;

    @Id
    @Column("skill")
    private Integer skill;

    @Column("value")
    private Integer value;

    @Column("max")
    private Integer max;

    
    @Column("professionSlot")
    private Byte professionSlot;

}