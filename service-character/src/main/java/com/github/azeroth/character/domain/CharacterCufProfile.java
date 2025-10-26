package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_cuf_profiles")
public class CharacterCufProfile {
    @Id

    @Column("guid")
    private Integer guid;

    @Id
    @Column("id")
    private Short id;

    @Column("name")
    private String name;


    @Column("frameHeight")
    private Integer frameHeight;


    @Column("frameWidth")
    private Integer frameWidth;


    @Column("sortBy")
    private Short sortBy;


    @Column("healthText")
    private Short healthText;


    @Column("boolOptions")
    private Integer boolOptions;


    @Column("topPoint")
    private Short topPoint;


    @Column("bottomPoint")
    private Short bottomPoint;


    @Column("leftPoint")
    private Short leftPoint;


    @Column("topOffset")
    private Integer topOffset;


    @Column("bottomOffset")
    private Integer bottomOffset;


    @Column("leftOffset")
    private Integer leftOffset;

}