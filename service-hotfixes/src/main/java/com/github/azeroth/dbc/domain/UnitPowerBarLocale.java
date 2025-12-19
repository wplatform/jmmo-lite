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


@Table(name = "unit_power_bar_locale")
public class UnitPowerBarLocale {
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

    
    @Column("Cost_lang")
    private String costLang;

    
    @Column("OutOfError_lang")
    private String outoferrorLang;

    
    @Column("ToolTip_lang")
    private String tooltipLang;

}