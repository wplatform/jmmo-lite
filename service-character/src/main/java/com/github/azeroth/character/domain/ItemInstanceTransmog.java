package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "item_instance_transmog")
public class ItemInstanceTransmog {
    @Id
    @Column("itemGuid")
    private Long id;


    @Column("itemModifiedAppearanceAllSpecs")
    private Integer itemModifiedAppearanceAllSpecs;


    @Column("itemModifiedAppearanceSpec1")
    private Integer itemModifiedAppearanceSpec1;


    @Column("itemModifiedAppearanceSpec2")
    private Integer itemModifiedAppearanceSpec2;


    @Column("itemModifiedAppearanceSpec3")
    private Integer itemModifiedAppearanceSpec3;


    @Column("itemModifiedAppearanceSpec4")
    private Integer itemModifiedAppearanceSpec4;


    @Column("itemModifiedAppearanceSpec5")
    private Integer itemModifiedAppearanceSpec5;


    @Column("spellItemEnchantmentAllSpecs")
    private Integer spellItemEnchantmentAllSpecs;


    @Column("spellItemEnchantmentSpec1")
    private Integer spellItemEnchantmentSpec1;


    @Column("spellItemEnchantmentSpec2")
    private Integer spellItemEnchantmentSpec2;


    @Column("spellItemEnchantmentSpec3")
    private Integer spellItemEnchantmentSpec3;


    @Column("spellItemEnchantmentSpec4")
    private Integer spellItemEnchantmentSpec4;


    @Column("spellItemEnchantmentSpec5")
    private Integer spellItemEnchantmentSpec5;


    @Column("secondaryItemModifiedAppearanceAllSpecs")
    private Integer secondaryItemModifiedAppearanceAllSpecs;


    @Column("secondaryItemModifiedAppearanceSpec1")
    private Integer secondaryItemModifiedAppearanceSpec1;


    @Column("secondaryItemModifiedAppearanceSpec2")
    private Integer secondaryItemModifiedAppearanceSpec2;


    @Column("secondaryItemModifiedAppearanceSpec3")
    private Integer secondaryItemModifiedAppearanceSpec3;


    @Column("secondaryItemModifiedAppearanceSpec4")
    private Integer secondaryItemModifiedAppearanceSpec4;


    @Column("secondaryItemModifiedAppearanceSpec5")
    private Integer secondaryItemModifiedAppearanceSpec5;

}