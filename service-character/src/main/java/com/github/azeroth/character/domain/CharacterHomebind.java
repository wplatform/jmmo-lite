package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "character_homebind")
public class CharacterHomebind {
    @Id

    @Column("guid")
    private Long id;


    @Column("mapId")
    private Integer mapId;


    @Column("zoneId")
    private Integer zoneId;


    @Column("posX")
    private Float posX;


    @Column("posY")
    private Float posY;


    @Column("posZ")
    private Float posZ;


    @Column("orientation")
    private Float orientation;

}