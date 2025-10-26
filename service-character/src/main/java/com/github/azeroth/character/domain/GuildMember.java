package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "guild_member")
public class GuildMember {
    @Column("guid")
    private Long guid;

    @Column("rank")
    private Short rank;


    @Column("pnote")
    private String pnote;


    @Column("offnote")
    private String offnote;

}