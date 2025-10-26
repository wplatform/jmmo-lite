package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Table(name = "instance")
public class Instance {
    @Id
    @Column("instanceId")
    private Long id;

    
    @Column("data")
    private String data;

    @Column("completedEncountersMask")
    private Long completedEncountersMask;

    @Column("entranceWorldSafeLocId")
    private Long entranceWorldSafeLocId;

}