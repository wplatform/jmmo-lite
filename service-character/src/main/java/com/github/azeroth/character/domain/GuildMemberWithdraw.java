package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "guild_member_withdraw")
public class GuildMemberWithdraw {
    @Id
    @Column("guid")
    private Long id;


    @Column("tab0")
    private Long tab0;


    @Column("tab1")
    private Long tab1;


    @Column("tab2")
    private Long tab2;


    @Column("tab3")
    private Long tab3;


    @Column("tab4")
    private Long tab4;


    @Column("tab5")
    private Long tab5;


    @Column("tab6")
    private Long tab6;


    @Column("tab7")
    private Long tab7;


    @Column("money")
    private Long money;

}