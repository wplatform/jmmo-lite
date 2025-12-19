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


@Table(name = "battlemaster_list_locale")
public class BattlemasterListLocale {
    @Id

    @Column("ID")
    private int id;

    @Id
    @Column("locale")
    private String locale;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Name_lang")
    private String nameLang;

    
    @Column("GameType_lang")
    private String gametypeLang;

    
    @Column("ShortDescription_lang")
    private String shortdescriptionLang;

    
    @Column("LongDescription_lang")
    private String longdescriptionLang;

}