package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "guild_bank_item")
public class GuildBankItem {
    @Id

    @Column("guildid")
    private Long guildid;

    @Id

    @Column("TabId")
    private Short tabId;

    @Id

    @Column("SlotId")
    private Short slotId;


    @Column("item_guid")
    private Long itemGuid;

}