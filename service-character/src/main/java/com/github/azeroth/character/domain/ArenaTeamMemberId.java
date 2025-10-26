package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ArenaTeamMemberId implements Serializable {
    public Long arenaTeamId;

    public Long guid;


}