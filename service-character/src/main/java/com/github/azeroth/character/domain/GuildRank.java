package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "guild_rank")
public class GuildRank {
    @Id

    @Column("guildid")
    private Long guildid;

    @Id
    @Column("rid")
    private Short rid;


    @Column("RankOrder")
    private Short rankOrder;


    @Column("rname")
    private String rname;


    @Column("rights")
    private Long rights;


    @Column("BankMoneyPerDay")
    private Long bankMoneyPerDay;

}