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

    public static byte TUTORIALS_FLAG_NONE = 0x00;
    public static byte TUTORIALS_FLAG_CHANGED = 0x01;
    public static byte TUTORIALS_FLAG_LOADED_FROM_DB = 0x02;


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

    public int[] getTutorials() {
        return new int[] {
            tut0, tut1, tut2, tut3, tut4, tut5, tut6, tut7
        };
    }
}