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


@Table(name = "garr_follower_locale")
public class GarrFollowerLocale {
    @Id

    @Column("ID")
    private Long id;

    @Id
    @Column("locale")
    private String locale;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("HordeSourceText_lang")
    private String hordesourcetextLang;

    
    @Column("AllianceSourceText_lang")
    private String alliancesourcetextLang;

    
    @Column("TitleName_lang")
    private String titlenameLang;

}