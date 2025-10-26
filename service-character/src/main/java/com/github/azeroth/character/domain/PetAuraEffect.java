package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "pet_aura_effect")
public class PetAuraEffect {
    @Id
    @Column("guid")
    private Long guid;

    @Id
    @Column("casterGuid")
    private String casterGuid;

    @Id
    @Column("spell")
    private Long spell;

    @Id
    @Column("effectMask")
    private Long effectMask;

    @Id
    @Column("effectIndex")
    private Short effectIndex;


    @Column("amount")
    private Integer amount;


    @Column("baseAmount")
    private Integer baseAmount;

}