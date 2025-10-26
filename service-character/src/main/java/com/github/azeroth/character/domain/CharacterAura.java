package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter


@Table(name = "character_aura")
public class CharacterAura {
    @Id
    
    @Column("guid")
    private Long guid;

    @Id
    @Column("casterGuid")
    private String casterGuid;

    @Id
    @Column("itemGuid")
    private String itemGuid;

    @Id
    @Column("spell")
    private Long spell;

    @Id
    @Column("effectMask")
    private Long effectMask;

    
    @Column("recalculateMask")
    private Long recalculateMask;

    
    @Column("difficulty")
    private Short difficulty;

    
    @Column("stackCount")
    private Short stackCount;

    
    @Column("maxDuration")
    private Integer maxDuration;

    
    @Column("remainTime")
    private Instant remainTime;

    
    @Column("remainCharges")
    private Short remainCharges;

    
    @Column("castItemId")
    private Long castItemId;

    
    @Column("castItemLevel")
    private Integer castItemLevel;

}