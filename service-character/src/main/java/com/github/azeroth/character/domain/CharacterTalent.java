package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_talent")
public class CharacterTalent {
    @Id
    @Column("guid")
    private Long guid;

    @Id
    @Column("talentId")
    private Long talentId;

    @Id

    @Column("talentGroup")
    private Short talentGroup;


    @Column("rank")
    private Short rank;

}