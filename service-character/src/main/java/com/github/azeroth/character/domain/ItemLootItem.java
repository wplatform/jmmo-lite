package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "item_loot_items")
public class ItemLootItem {
    @Id

    @Column("container_id")
    private Long containerId;

    @Id

    @Column("item_type")
    private Byte itemType;

    @Id

    @Column("item_id")
    private Long itemId;


    @Column("item_count")
    private Integer itemCount;


    @Column("item_index")
    private Long itemIndex;


    @Column("follow_rules")
    private Boolean followRules = false;


    @Column("ffa")
    private Boolean ffa = false;


    @Column("blocked")
    private Boolean blocked = false;


    @Column("counted")
    private Boolean counted = false;


    @Column("under_threshold")
    private Boolean underThreshold = false;


    @Column("needs_quest")
    private Boolean needsQuest = false;


    @Column("rnd_bonus")
    private Long rndBonus;


    @Column("context")
    private Short context;

    
    @Column("bonus_list_ids")
    private String bonusListIds;

}