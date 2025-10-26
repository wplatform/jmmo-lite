package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PvpstatsPlayerId implements Serializable {
    public Long battlegroundId;

    public Long characterGuid;


}