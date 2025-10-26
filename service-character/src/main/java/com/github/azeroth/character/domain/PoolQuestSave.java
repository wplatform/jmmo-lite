package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "pool_quest_save")
public class PoolQuestSave {
    @Id
    
    @Column("pool_id")
    private Long poolId;

    @Id
    
    @Column("quest_id")
    private Long questId;

}