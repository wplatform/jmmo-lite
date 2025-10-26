package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CharacterTalentId implements Serializable {
    public Long guid;

    public Long talentId;

    public Short talentGroup;


}