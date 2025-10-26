package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "pvpstats_players")
public class PvpstatsPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("battleground_id")
    private Long battlegroundId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("character_guid")
    private Long characterGuid;

    @Column("winner")
    private Boolean winner = false;

    @Column("score_killing_blows")
    private Long scoreKillingBlows;

    @Column("score_deaths")
    private Long scoreDeaths;

    @Column("score_honorable_kills")
    private Long scoreHonorableKills;

    @Column("score_bonus_honor")
    private Long scoreBonusHonor;

    @Column("score_damage_done")
    private Long scoreDamageDone;

    @Column("score_healing_done")
    private Long scoreHealingDone;


    @Column("attr_1")
    private Long attr1;


    @Column("attr_2")
    private Long attr2;


    @Column("attr_3")
    private Long attr3;


    @Column("attr_4")
    private Long attr4;


    @Column("attr_5")
    private Long attr5;

}