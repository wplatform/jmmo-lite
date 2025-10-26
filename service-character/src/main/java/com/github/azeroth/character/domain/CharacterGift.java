package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "character_gifts")
public class CharacterGift {
    @Id
    
    @Column("item_guid")
    private int id;

    
    @Column("guid")
    private Integer guid;

    
    @Column("entry")
    private Integer entry;

    
    @Column("flags")
    private Integer flags;

}