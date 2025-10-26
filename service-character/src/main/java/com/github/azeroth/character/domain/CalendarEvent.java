package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter

@Table(name = "calendar_events")
public class CalendarEvent {
    @Id
    
    @Column("EventID")
    private int id;

    
    @Column("Owner")
    private Integer owner;

    
    @Column("Title")
    private String title;

    
    @Column("Description")
    private String description;

    
    @Column("EventType")
    private Short eventType;

    
    @Column("TextureID")
    private Integer textureID;

    
    @Column("Date")
    private Instant date;

    
    @Column("Flags")
    private Integer flags;

    
    @Column("LockDate")
    private Instant lockDate;

}