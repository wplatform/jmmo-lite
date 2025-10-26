package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "character_transmog_outfits")
public class CharacterTransmogOutfit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("setguid")
    private Long id;


    @Column("guid")
    private Long guid;


    @Column("setindex")
    private Short setindex;

    @Column("name")
    private String name;

    @Column("iconname")
    private String iconname;


    @Column("ignore_mask")
    private Integer ignoreMask;


    @Column("appearance0")
    private Integer appearance0;


    @Column("appearance1")
    private Integer appearance1;


    @Column("appearance2")
    private Integer appearance2;


    @Column("appearance3")
    private Integer appearance3;


    @Column("appearance4")
    private Integer appearance4;


    @Column("appearance5")
    private Integer appearance5;


    @Column("appearance6")
    private Integer appearance6;


    @Column("appearance7")
    private Integer appearance7;


    @Column("appearance8")
    private Integer appearance8;


    @Column("appearance9")
    private Integer appearance9;


    @Column("appearance10")
    private Integer appearance10;


    @Column("appearance11")
    private Integer appearance11;


    @Column("appearance12")
    private Integer appearance12;


    @Column("appearance13")
    private Integer appearance13;


    @Column("appearance14")
    private Integer appearance14;


    @Column("appearance15")
    private Integer appearance15;


    @Column("appearance16")
    private Integer appearance16;


    @Column("appearance17")
    private Integer appearance17;


    @Column("appearance18")
    private Integer appearance18;


    @Column("mainHandEnchant")
    private Integer mainHandEnchant;


    @Column("offHandEnchant")
    private Integer offHandEnchant;

}