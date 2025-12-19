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


@Table(name = "ui_splash_screen_locale")
public class UiSplashScreenLocale {
    @Id
    
    @Column("ID")
    private int id;

    @Id
    @Column("locale")
    private String locale;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Header_lang")
    private String headerLang;

    
    @Column("TopLeftFeatureTitle_lang")
    private String topleftfeaturetitleLang;

    
    @Column("TopLeftFeatureDesc_lang")
    private String topleftfeaturedescLang;

    
    @Column("BottomLeftFeatureTitle_lang")
    private String bottomleftfeaturetitleLang;

    
    @Column("BottomLeftFeatureDesc_lang")
    private String bottomleftfeaturedescLang;

    
    @Column("RightFeatureTitle_lang")
    private String rightfeaturetitleLang;

    
    @Column("RightFeatureDesc_lang")
    private String rightfeaturedescLang;

}