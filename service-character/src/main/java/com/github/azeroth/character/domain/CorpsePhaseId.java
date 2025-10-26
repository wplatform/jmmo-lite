package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CorpsePhaseId implements Serializable {
    public Long ownerGuid;

    public Long phaseId;


}