package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


@Table(name = "guild_eventlog")
public class GuildEventlog {
    @Id
    @Column("guildid")
    private Long guildid;

    @Id
    @Column("LogGuid")
    private Long logGuid;

    @Column("EventType")
    private Short eventType;

    @Column("PlayerGuid1")
    private Long playerGuid1;

    @Column("PlayerGuid2")
    private Long playerGuid2;

    @Column("NewRank")
    private Short newRank;

    @Column("TimeStamp")
    private Long timeStamp;

}