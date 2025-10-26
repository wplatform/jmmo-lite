package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "character_declinedname")
public class CharacterDeclinedname {
    @Id

    @Column("guid")
    private int id;


    @Column("genitive")
    private String genitive;


    @Column("dative")
    private String dative;


    @Column("accusative")
    private String accusative;


    @Column("instrumental")
    private String instrumental;


    @Column("prepositional")
    private String prepositional;

}