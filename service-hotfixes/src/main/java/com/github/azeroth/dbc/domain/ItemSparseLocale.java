package com.github.azeroth.dbc.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "item_sparse_locale")
public class ItemSparseLocale {
    @Id

    @Column("ID")
    private int id;

    @Id
    @Column("locale")
    private String locale;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Description_lang")
    private String descriptionLang;

    
    @Column("Display3_lang")
    private String display3Lang;

    
    @Column("Display2_lang")
    private String display2Lang;

    
    @Column("Display1_lang")
    private String display1Lang;

    
    @Column("Display_lang")
    private String displayLang;

}