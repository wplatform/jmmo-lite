package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "lfg_data")
public class LfgDatum {
    @Id

    @Column("guid")
    private Long id;


    @Column("dungeon")
    private Long dungeon;


    @Column("state")
    private Short state;

}