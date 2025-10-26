package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


@Table(name = "character_aura_stored_location")
public class CharacterAuraStoredLocation {
    @Id
    @Column("Guid")
    private Integer guid;

    @Id
    @Column("Spell")
    private Integer spell;

    @Column("MapId")
    private Integer mapId;

    @Column("PositionX")
    private Float positionX;

    @Column("PositionY")
    private Float positionY;

    @Column("PositionZ")
    private Float positionZ;

    @Column("Orientation")
    private Float orientation;

}