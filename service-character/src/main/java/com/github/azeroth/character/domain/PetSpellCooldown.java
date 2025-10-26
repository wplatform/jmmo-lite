package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "pet_spell_cooldown")
public class PetSpellCooldown {
    @Id

    @Column("guid")
    private Long guid;

    @Id

    @Column("spell")
    private Long spell;


    @Column("time")
    private Long time;


    @Column("categoryId")
    private Long categoryId;


    @Column("categoryEnd")
    private Long categoryEnd;

}