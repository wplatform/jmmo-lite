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


@Table(name = "garr_class_spec_locale")
public class GarrClassSpecLocale {
    @Id

    @Column("ID")
    private Long id;

    @Id
    @Column("locale")
    private String locale;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Column("ClassSpec_lang")
    private String classspecLang;


    @Column("ClassSpecMale_lang")
    private String classspecmaleLang;


    @Column("ClassSpecFemale_lang")
    private String classspecfemaleLang;

}