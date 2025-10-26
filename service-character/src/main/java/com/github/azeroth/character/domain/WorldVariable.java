package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "world_variable")
public class WorldVariable {
    @Id
    @Column("ID")
    private String id;


    @Column("Value")
    private Integer value;

}