package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "guild_bank_eventlog")
public class GuildBankEventlog {
    @Id

    @Column("guildid")
    private Long guildid;

    @Id

    @Column("LogGuid")
    private Long logGuid;

    @Id

    @Column("TabId")
    private Short tabId;


    @Column("EventType")
    private Short eventType;


    @Column("PlayerGuid")
    private Long playerGuid;


    @Column("ItemOrMoney")
    private Long itemOrMoney;


    @Column("ItemStackCount")
    private Integer itemStackCount;


    @Column("DestTabId")
    private Short destTabId;


    @Column("TimeStamp")
    private Long timeStamp;

}