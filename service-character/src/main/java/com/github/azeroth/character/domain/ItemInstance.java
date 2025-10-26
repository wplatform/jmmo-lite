package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "item_instance")
public class ItemInstance {
    @Id
    
    @Column("guid")
    private Long id;

    
    @Column("itemEntry")
    private Long itemEntry;

    
    @Column("owner_guid")
    private Long ownerGuid;

    
    @Column("creatorGuid")
    private Long creatorGuid;

    
    @Column("giftCreatorGuid")
    private Long giftCreatorGuid;

    
    @Column("count")
    private Long count;

    
    @Column("duration")
    private Integer duration;

    
    @Column("charges")
    private String charges;

    
    @Column("flags")
    private Long flags;

    
    @Column("enchantments")
    private String enchantments;

    
    @Column("randomBonusListId")
    private Long randomBonusListId;

    
    @Column("durability")
    private Integer durability;

    
    @Column("playedTime")
    private Long playedTime;

    
    @Column("createTime")
    private Long createTime;

    
    @Column("text")
    private String text;

    
    @Column("battlePetSpeciesId")
    private Long battlePetSpeciesId;

    
    @Column("battlePetBreedData")
    private Long battlePetBreedData;

    
    @Column("battlePetLevel")
    private Integer battlePetLevel;

    
    @Column("battlePetDisplayId")
    private Long battlePetDisplayId;

    
    @Column("context")
    private Short context;

    
    @Column("bonusListIDs")
    private String bonusListIDs;

}