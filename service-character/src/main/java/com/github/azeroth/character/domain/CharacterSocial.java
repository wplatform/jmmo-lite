package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_social")
public class CharacterSocial {
    @Id
    
    @Column("guid")
    private Long guid;

    @Id
    
    @Column("friend")
    private Long friend;

    @Id
    
    @Column("flags")
    private Short flags;

    
    @Column("note")
    private String note;

}