package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


@Table(name = "character_instance_lock")
public class CharacterInstanceLock {
    @Id
    @Column("guid")
    private Long guid;

    @Id
    @Column("mapId")
    private Long mapId;

    @Id
    @Column("lockId")
    private Long lockId;

    @Column("instanceId")
    private Long instanceId;

    @Column("difficulty")
    private Short difficulty;

    
    @Column("data")
    private String data;

    @Column("completedEncountersMask")
    private Long completedEncountersMask;

    @Column("entranceWorldSafeLocId")
    private Long entranceWorldSafeLocId;

    @Column("expiryTime")
    private Long expiryTime;

    @Column("extended")
    private Short extended;

}