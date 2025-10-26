package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CharacterActionId implements Serializable {
    public Long guid;

    public Short spec;

    public Integer traitConfigId;

    public Short button;


}