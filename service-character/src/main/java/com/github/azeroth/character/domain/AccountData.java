package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter


@Table(name = "account_data")
public class AccountData {
    @Id
    @Column("accountId")
    private Integer accountId;

    @Id
    @Column("type")
    private short type;


    @Column("time")
    private Instant time;

    @Column("data")
    private byte[] data;

}