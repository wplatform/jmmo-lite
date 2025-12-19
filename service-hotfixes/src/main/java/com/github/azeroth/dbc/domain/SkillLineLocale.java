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


@Table(name = "skill_line_locale")
public class SkillLineLocale {
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

    
    @Column("AlternateVerb_lang")
    private String alternateverbLang;

    
    @Column("Description_lang")
    private String descriptionLang;

    
    @Column("HordeDisplayName_lang")
    private String hordedisplaynameLang;

}