package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_action")
public class CharacterAction {
    @Id

    @Column("guid")
    private Integer guid;

    @Id

    @Column("spec")
    private Short spec;

    @Id

    @Column("traitConfigId")
    private Integer traitConfigId;

    @Id

    @Column("button")
    private Short button;


    @Column("action")
    private Integer action;


    @Column("type")
    private Short type;

}