package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_queststatus")
public class CharacterQueststatus {
    @Id

    @Column("guid")
    private Long guid;

    @Id

    @Column("quest")
    private Long quest;


    @Column("status")
    private Short status;


    @Column("explored")
    private Short explored;


    @Column("acceptTime")
    private Long acceptTime;


    @Column("endTime")
    private Long endTime;

}