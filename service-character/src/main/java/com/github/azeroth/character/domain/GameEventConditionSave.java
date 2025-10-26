package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "game_event_condition_save")
public class GameEventConditionSave {
    @Id
    @Column("eventEntry")
    private Short eventEntry;

    @Id

    @Column("condition_id")
    private Long conditionId;


    @Column("done")
    private Float done;

}