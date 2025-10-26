package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "channels")
public class Channel {
    @Id
    @Column("name")
    private String name;

    @Id
    @Column("team")
    private Integer team;


    @Column("announce")
    private Short announce;


    @Column("ownership")
    private Short ownership;

    @Column("password")
    private String password;

    
    @Column("bannedList")
    private String bannedList;

    @Column("lastUsed")
    private Integer lastUsed;

}