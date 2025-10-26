package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "character_stats")
public class CharacterStat {
    @Id
    
    @Column("guid")
    private Long id;

    
    @Column("maxhealth")
    private Long maxhealth;

    
    @Column("maxpower1")
    private Long maxpower1;

    
    @Column("maxpower2")
    private Long maxpower2;

    
    @Column("maxpower3")
    private Long maxpower3;

    
    @Column("maxpower4")
    private Long maxpower4;

    
    @Column("maxpower5")
    private Long maxpower5;

    
    @Column("maxpower6")
    private Long maxpower6;

    
    @Column("maxpower7")
    private Long maxpower7;

    
    @Column("maxpower8")
    private Long maxpower8;

    
    @Column("maxpower9")
    private Long maxpower9;

    
    @Column("maxpower10")
    private Long maxpower10;

    
    @Column("strength")
    private Long strength;

    
    @Column("agility")
    private Long agility;

    
    @Column("stamina")
    private Long stamina;

    
    @Column("intellect")
    private Long intellect;

    
    @Column("armor")
    private Long armor;

    
    @Column("resHoly")
    private Long resHoly;

    
    @Column("resFire")
    private Long resFire;

    
    @Column("resNature")
    private Long resNature;

    
    @Column("resFrost")
    private Long resFrost;

    
    @Column("resShadow")
    private Long resShadow;

    
    @Column("resArcane")
    private Long resArcane;

    
    @Column("attackPower")
    private Long attackPower;

    
    @Column("rangedAttackPower")
    private Long rangedAttackPower;

    
    @Column("spellPower")
    private Long spellPower;

    
    @Column("resilience")
    private Long resilience;

    
    @Column("mastery")
    private Float mastery;

    
    @Column("versatility")
    private Integer versatility;


    
    @Column("blockPct")
    private Float blockPct;

    
    @Column("dodgePct")
    private Float dodgePct;

 
    
    @Column("parryPct")
    private Float parryPct;
 

    
    @Column("critPct")
    private Float critPct;

    
    @Column("rangedCritPct")
    private Float rangedCritPct;
    
    
    @Column("spellCritPct")
    private Float spellCritPct;
}