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


@Table(name = "spell_range_locale")
public class SpellRangeLocale {
    @Id
    
    @Column("ID")
    private int id;

    @Id
    @Column("locale")
    private String locale;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("DisplayName_lang")
    private String displaynameLang;

    
    @Column("DisplayNameShort_lang")
    private String displaynameshortLang;

}