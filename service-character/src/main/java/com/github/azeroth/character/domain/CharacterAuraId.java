package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CharacterAuraId implements Serializable {
    public Long guid;

    public String casterGuid;

    public String itemGuid;

    public Long spell;

    public Long effectMask;


}