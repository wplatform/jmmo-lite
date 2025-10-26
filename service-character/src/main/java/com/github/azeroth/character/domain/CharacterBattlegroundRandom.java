package com.github.azeroth.character.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter

@Table(name = "character_battleground_random")
public class CharacterBattlegroundRandom {
    @Id

    @Column("guid")
    private int id;

    //TODO [Reverse Engineering] generate columns from DB
}