package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "guild_bank_tab")
public class GuildBankTab {
    @Id
    
    @Column("guildid")
    private Long guildid;

    @Id
    
    @Column("TabId")
    private Short tabId;

    
    @Column("TabName")
    private String tabName;

    
    @Column("TabIcon")
    private String tabIcon;

    @Column("TabText")
    private String tabText;

}