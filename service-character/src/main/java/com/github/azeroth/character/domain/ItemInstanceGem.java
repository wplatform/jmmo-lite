package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "item_instance_gems")
public class ItemInstanceGem {
    @Id
    @Column("itemGuid")
    private Long id;


    @Column("gemItemId1")
    private Long gemItemId1;

    
    @Column("gemBonuses1")
    private String gemBonuses1;


    @Column("gemContext1")
    private Short gemContext1;


    @Column("gemScalingLevel1")
    private Long gemScalingLevel1;


    @Column("gemItemId2")
    private Long gemItemId2;

    
    @Column("gemBonuses2")
    private String gemBonuses2;


    @Column("gemContext2")
    private Short gemContext2;


    @Column("gemScalingLevel2")
    private Long gemScalingLevel2;


    @Column("gemItemId3")
    private Long gemItemId3;

    
    @Column("gemBonuses3")
    private String gemBonuses3;


    @Column("gemContext3")
    private Short gemContext3;


    @Column("gemScalingLevel3")
    private Long gemScalingLevel3;

}