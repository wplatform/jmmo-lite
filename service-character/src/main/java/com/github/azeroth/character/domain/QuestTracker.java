package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter


@Table(name = "quest_tracker")
public class QuestTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column("id")
    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column("character_guid")
    private Long characterGuid;

    @Column("quest_accept_time")
    private Instant questAcceptTime;

    @Column("quest_complete_time")
    private Instant questCompleteTime;

    @Column("quest_abandon_time")
    private Instant questAbandonTime;

    
    @Column("completed_by_gm")
    private Boolean completedByGm = false;

    
    @Column("core_hash")
    private String coreHash;

    
    @Column("core_revision")
    private String coreRevision;

}