package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_favorite_auctions")
public class CharacterFavoriteAuction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("guid")
    private Integer guid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column("`order`")
    private Integer order;


    @Column("itemId")
    private Integer itemId;


    @Column("itemLevel")
    private Integer itemLevel;


    @Column("battlePetSpeciesId")
    private Long battlePetSpeciesId;


    @Column("suffixItemNameDescriptionId")
    private Long suffixItemNameDescriptionId;

}