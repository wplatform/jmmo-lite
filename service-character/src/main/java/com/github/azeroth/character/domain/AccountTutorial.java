package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "account_tutorial")
public class AccountTutorial {
    @Id

    @Column("accountId")
    private int id;


    @Column("tut0")
    private Integer tut0;


    @Column("tut1")
    private Integer tut1;


    @Column("tut2")
    private Integer tut2;


    @Column("tut3")
    private Integer tut3;


    @Column("tut4")
    private Integer tut4;


    @Column("tut5")
    private Integer tut5;


    @Column("tut6")
    private Integer tut6;


    @Column("tut7")
    private Integer tut7;

}