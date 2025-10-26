package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "corpse_phases")
public class CorpsePhase {
    @Id

    @Column("OwnerGuid")
    private Long ownerGuid;

    @Id
    @Column("PhaseId")
    private Long phaseId;

}