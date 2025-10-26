package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_talent_group")
public class CharacterTalentGroup {
    @Id

    @Column("guid")
    private Long guid;

    @Id

    @Column("id")
    private Short id;


    @Column("talentTabId")
    private Long talentTabId;

}