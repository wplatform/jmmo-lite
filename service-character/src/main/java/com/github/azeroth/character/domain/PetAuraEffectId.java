package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PetAuraEffectId implements Serializable {
    public Long guid;

    public String casterGuid;

    public Long spell;

    public Long effectMask;

    public Short effectIndex;


}