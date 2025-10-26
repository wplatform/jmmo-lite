package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_spell")
public class CharacterSpell {
    @Id

    @Column("guid")
    private Long guid;

    @Id

    @Column("spell")
    private Long spell;


    @Column("active")
    private Short active;


    @Column("disabled")
    private Short disabled;

}