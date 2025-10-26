package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_aura_effect")
public class CharacterAuraEffect {
    @Id
    @Column("guid")
    private Integer guid;

    @Id
    @Column("casterGuid")
    private String casterGuid;

    @Id
    @Column("itemGuid")
    private String itemGuid;

    @Id
    @Column("spell")
    private Integer spell;

    @Id
    @Column("effectMask")
    private Integer effectMask;

    @Id
    @Column("effectIndex")
    private Short effectIndex;


    @Column("amount")
    private Integer amount;


    @Column("baseAmount")
    private Integer baseAmount;

}