package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CharacterGlyphId implements Serializable {
    public Long guid;

    public Short talentGroup;

    public Short glyphSlot;

    public Integer glyphId;


}