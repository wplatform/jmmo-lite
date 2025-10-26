package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter

@Table(name = "calendar_invites")
public class CalendarInvite {
    @Id

    @Column("InviteID")
    private int id;


    @Column("EventID")
    private Integer eventID;


    @Column("Invitee")
    private Integer invitee;


    @Column("Sender")
    private Integer sender;


    @Column("Status")
    private Short status;


    @Column("ResponseTime")
    private Instant responseTime;


    @Column("ModerationRank")
    private Short moderationRank;


    @Column("Note")
    private String note;

}