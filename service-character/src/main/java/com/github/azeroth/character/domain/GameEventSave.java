package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "game_event_save")
public class GameEventSave {
    @Id
    @Column("eventEntry")
    private Short id;


    @Column("state")
    private Short state;


    @Column("next_start")
    private Long nextStart;

}