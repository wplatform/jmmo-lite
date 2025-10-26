package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "corpse")
public class Corpse {
    @Id

    @Column("guid")
    private Long id;


    @Column("posX")
    private Float posX;


    @Column("posY")
    private Float posY;


    @Column("posZ")
    private Float posZ;


    @Column("orientation")
    private Float orientation;


    @Column("mapId")
    private Integer mapId;


    @Column("displayId")
    private Long displayId;

    
    @Column("itemCache")
    private String itemCache;


    @Column("race")
    private Short race;


    @Column("class")
    private Short classField;


    @Column("gender")
    private Short gender;


    @Column("flags")
    private Short flags;


    @Column("dynFlags")
    private Short dynFlags;


    @Column("time")
    private Long time;


    @Column("corpseType")
    private Short corpseType;


    @Column("instanceId")
    private Long instanceId;

}