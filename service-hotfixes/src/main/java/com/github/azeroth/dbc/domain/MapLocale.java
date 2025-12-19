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


@Table(name = "map_locale")
public class MapLocale {
    @Id

    @Column("ID")
    private int id;

    @Id
    @Column("locale")
    private String locale;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("MapName_lang")
    private String mapnameLang;

    
    @Column("MapDescription0_lang")
    private String mapdescription0Lang;

    
    @Column("MapDescription1_lang")
    private String mapdescription1Lang;

    
    @Column("PvpShortDescription_lang")
    private String pvpshortdescriptionLang;

    
    @Column("PvpLongDescription_lang")
    private String pvplongdescriptionLang;

}