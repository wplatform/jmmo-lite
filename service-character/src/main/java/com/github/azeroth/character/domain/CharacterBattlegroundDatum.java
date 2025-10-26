package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "character_battleground_data")
public class CharacterBattlegroundDatum {
    @Id

    @Column("guid")
    private int id;

    @Column("instanceId")
    private Integer instanceId;

    @Column("team")
    private Integer team;


    @Column("joinX")
    private Float joinX;


    @Column("joinY")
    private Float joinY;


    @Column("joinZ")
    private Float joinZ;


    @Column("joinO")
    private Float joinO;


    @Column("joinMapId")
    private Integer joinMapId;


    @Column("taxiStart")
    private Integer taxiStart;


    @Column("taxiEnd")
    private Integer taxiEnd;


    @Column("mountSpell")
    private Integer mountSpell;


    @Column("queueId")
    private Integer queueId;

}