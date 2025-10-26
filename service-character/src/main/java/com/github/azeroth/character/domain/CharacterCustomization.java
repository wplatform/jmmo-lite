package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_customizations")
public class CharacterCustomization {
    @Id
    @Column("guid")
    private Integer guid;

    @Id
    @Column("chrCustomizationOptionID")
    private Integer chrCustomizationOptionID;


    @Column("chrCustomizationChoiceID")
    private Integer chrCustomizationChoiceID;

}