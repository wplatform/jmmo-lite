package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "pet_spell_charges")
public class PetSpellCharge {

    @Column("categoryId")
    private Long categoryId;


    @Column("rechargeStart")
    private Long rechargeStart;


    @Column("rechargeEnd")
    private Long rechargeEnd;

}