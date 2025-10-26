package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_glyphs")
public class CharacterGlyph {
    @Id
    @Column("guid")
    private Integer guid;

    @Id

    @Column("talentGroup")
    private Short talentGroup;

    @Id

    @Column("glyphSlot")
    private Short glyphSlot;

    @Id

    @Column("glyphId")
    private Integer glyphId;

}