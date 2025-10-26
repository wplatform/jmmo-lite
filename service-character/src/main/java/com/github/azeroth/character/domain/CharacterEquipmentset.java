package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "character_equipmentsets")
public class CharacterEquipmentset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("setguid")
    private int id;


    @Column("guid")
    private Long guid;


    @Column("setindex")
    private Short setindex;

    @Column("name")
    private String name;

    @Column("iconname")
    private String iconname;


    @Column("ignore_mask")
    private Long ignoreMask;


    @Column("AssignedSpecIndex")
    private Integer assignedSpecIndex;


    @Column("item0")
    private Long item0;


    @Column("item1")
    private Long item1;


    @Column("item2")
    private Long item2;


    @Column("item3")
    private Long item3;


    @Column("item4")
    private Long item4;


    @Column("item5")
    private Long item5;


    @Column("item6")
    private Long item6;


    @Column("item7")
    private Long item7;


    @Column("item8")
    private Long item8;


    @Column("item9")
    private Long item9;


    @Column("item10")
    private Long item10;


    @Column("item11")
    private Long item11;


    @Column("item12")
    private Long item12;


    @Column("item13")
    private Long item13;


    @Column("item14")
    private Long item14;


    @Column("item15")
    private Long item15;


    @Column("item16")
    private Long item16;


    @Column("item17")
    private Long item17;


    @Column("item18")
    private Long item18;

}