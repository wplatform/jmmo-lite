package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CharacterInstanceLockId implements Serializable {
    public Long guid;

    public Long mapId;

    public Long lockId;


}