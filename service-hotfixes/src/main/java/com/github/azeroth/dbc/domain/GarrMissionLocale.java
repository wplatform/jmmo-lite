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


@Table(name = "garr_mission_locale")
public class GarrMissionLocale {
    @Id
    
    @Column("ID")
    private Long id;

    @Id
    @Column("locale")
    private String locale;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Name_lang")
    private String nameLang;

    
    @Column("Location_lang")
    private String locationLang;

    
    @Column("Description_lang")
    private String descriptionLang;

}