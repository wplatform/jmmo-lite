package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_queststatus_objectives_criteria_progress")
public class CharacterQueststatusObjectivesCriteriaProgress {
    @Id
    @Column("guid")
    private Long guid;

    @Id
    @Column("criteriaId")
    private Long criteriaId;

    @Column("counter")
    private Long counter;


    @Column("date")
    private Long date;

}