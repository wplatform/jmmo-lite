package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "guild_newslog")
public class GuildNewslog {
    @Id

    @Column("guildid")
    private Long guildid;

    @Id

    @Column("LogGuid")
    private Long logGuid;


    @Column("EventType")
    private Short eventType;


    @Column("PlayerGuid")
    private Long playerGuid;


    @Column("Flags")
    private Long flags;


    @Column("Value")
    private Long value;


    @Column("TimeStamp")
    private Long timeStamp;

}