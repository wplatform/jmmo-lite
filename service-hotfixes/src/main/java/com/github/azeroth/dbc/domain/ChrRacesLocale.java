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


@Table(name = "chr_races_locale")
public class ChrRacesLocale {
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

    
    @Column("NameFemale_lang")
    private String namefemaleLang;

    
    @Column("NameLowercase_lang")
    private String namelowercaseLang;

    
    @Column("NameFemaleLowercase_lang")
    private String namefemalelowercaseLang;

    
    @Column("LoreName_lang")
    private String lorenameLang;

    
    @Column("LoreNameFemale_lang")
    private String lorenamefemaleLang;

    
    @Column("LoreNameLower_lang")
    private String lorenamelowerLang;

    
    @Column("LoreNameLowerFemale_lang")
    private String lorenamelowerfemaleLang;

    
    @Column("LoreDescription_lang")
    private String loredescriptionLang;

    
    @Column("ShortName_lang")
    private String shortnameLang;

    
    @Column("ShortNameFemale_lang")
    private String shortnamefemaleLang;

    
    @Column("ShortNameLower_lang")
    private String shortnamelowerLang;

    
    @Column("ShortNameLowerFemale_lang")
    private String shortnamelowerfemaleLang;

}