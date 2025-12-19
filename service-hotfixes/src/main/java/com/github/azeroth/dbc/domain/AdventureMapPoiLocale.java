package com.github.azeroth.dbc.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "adventure_map_poi_locale")
public class AdventureMapPoiLocale {
    @Id
    
    @Column("ID")
    private Long id;

    @Id
    @Column("locale")
    private String locale;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Title_lang")
    private String titleLang;

    
    @Column("Description_lang")
    private String descriptionLang;

}