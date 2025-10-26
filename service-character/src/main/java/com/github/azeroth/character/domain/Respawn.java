package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


@Table(name = "respawn")
public class Respawn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("type")
    private Integer type;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("spawnId")
    private Long spawnId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("instanceId")
    private Long instanceId;

    @Column("respawnTime")
    private Long respawnTime;

    @Column("mapId")
    private Integer mapId;

}