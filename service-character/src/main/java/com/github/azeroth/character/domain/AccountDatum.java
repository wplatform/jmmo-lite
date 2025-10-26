package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "account_data")
public class AccountDatum {
    @Id

    @Column("accountId")
    private Integer accountId;

    @Id

    @Column("type")
    private Short type;


    @Column("time")
    private Integer time;

    @Column("data")
    private byte[] data;

}