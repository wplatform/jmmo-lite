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


@Table(name = "item_search_name_locale")
public class ItemSearchNameLocale {
    @Id

    @Column("ID")
    private int id;

    @Id
    @Column("locale")
    private String locale;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Display_lang")
    private String displayLang;

}