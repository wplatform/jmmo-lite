package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RespawnId implements Serializable {
    public Integer type;

    public Long spawnId;

    public Long instanceId;


}