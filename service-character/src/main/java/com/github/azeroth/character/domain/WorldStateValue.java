package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Table(name = "world_state_value")
public class WorldStateValue {
    @Id
    @Column("Id")
    private int id;

    @Column("Value")
    private Integer value;

}