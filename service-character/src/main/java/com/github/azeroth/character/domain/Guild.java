package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "guild")
public class Guild {
    @Id

    @Column("guildid")
    private Long id;


    @Column("name")
    private String name;


    @Column("leaderguid")
    private Long leaderguid;


    @Column("EmblemStyle")
    private Short emblemStyle;


    @Column("EmblemColor")
    private Short emblemColor;


    @Column("BorderStyle")
    private Short borderStyle;


    @Column("BorderColor")
    private Short borderColor;


    @Column("BackgroundColor")
    private Short backgroundColor;


    @Column("info")
    private String info;


    @Column("motd")
    private String motd;


    @Column("createdate")
    private Long createdate;


    @Column("BankMoney")
    private Long bankMoney;

}