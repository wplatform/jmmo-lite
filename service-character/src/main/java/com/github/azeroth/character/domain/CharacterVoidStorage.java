package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "character_void_storage")
public class CharacterVoidStorage {
    @Id
    @Column("itemId")
    private Long id;

    @Column("playerGuid")
    private Long playerGuid;

    @Column("itemEntry")
    private Long itemEntry;

    @Column("slot")
    private Short slot;


    @Column("creatorGuid")
    private Long creatorGuid;


    @Column("randomBonusListId")
    private Long randomBonusListId;


    @Column("fixedScalingLevel")
    private Long fixedScalingLevel;


    @Column("artifactKnowledgeLevel")
    private Long artifactKnowledgeLevel;


    @Column("context")
    private Short context;

    
    @Column("bonusListIDs")
    private String bonusListIDs;

}