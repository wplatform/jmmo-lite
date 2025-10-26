package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "corpse_customizations")
public class CorpseCustomization {
    @Id
    @Column("ownerGuid")
    private Long ownerGuid;

    @Id
    @Column("chrCustomizationOptionID")
    private Long chrCustomizationOptionID;

    
    @Column("chrCustomizationChoiceID")
    private Long chrCustomizationChoiceID;

}