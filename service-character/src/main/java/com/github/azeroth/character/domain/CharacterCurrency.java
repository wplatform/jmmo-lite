package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_currency")
public class CharacterCurrency {
    @Id

    @Column("CharacterGuid")
    private Integer characterGuid;

    @Id
    @Column("Currency")
    private Integer currency;

    @Column("Quantity")
    private Integer quantity;

    @Column("WeeklyQuantity")
    private Integer weeklyQuantity;

    @Column("TrackedQuantity")
    private Integer trackedQuantity;


    @Column("IncreasedCapQuantity")
    private Integer increasedCapQuantity;


    @Column("EarnedQuantity")
    private Integer earnedQuantity;

    @Column("Flags")
    private Short flags;

}