package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "guild_bank_right")
public class GuildBankRight {
    @Id

    @Column("guildid")
    private Long guildid;

    @Id

    @Column("TabId")
    private Short tabId;

    @Id

    @Column("rid")
    private Short rid;


    @Column("gbright")
    private Byte gbright;


    @Column("SlotPerDay")
    private Integer slotPerDay;

}