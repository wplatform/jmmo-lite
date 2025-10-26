package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "groups")
public class Group {
    @Id
    @Column("guid")
    private Long id;

    @Column("leaderGuid")
    private Long leaderGuid;

    @Column("lootMethod")
    private Short lootMethod;

    @Column("looterGuid")
    private Long looterGuid;

    @Column("lootThreshold")
    private Short lootThreshold;

    @Column("icon1")
    private String icon1;

    @Column("icon2")
    private String icon2;

    @Column("icon3")
    private String icon3;

    @Column("icon4")
    private String icon4;

    @Column("icon5")
    private String icon5;

    @Column("icon6")
    private String icon6;

    @Column("icon7")
    private String icon7;

    @Column("icon8")
    private String icon8;

    @Column("groupType")
    private Integer groupType;


    @Column("difficulty")
    private Short difficulty;


    @Column("raidDifficulty")
    private Short raidDifficulty;


    @Column("legacyRaidDifficulty")
    private Short legacyRaidDifficulty;

    @Column("masterLooterGuid")
    private Long masterLooterGuid;

}