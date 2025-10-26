package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter


@Table(name = "account_instance_times")
public class AccountInstanceTime {
    @Id
    @Column("accountId")
    private Integer accountId;

    @Id

    @Column("instanceId")
    private Integer instanceId;


    @Column("releaseTime")
    private Instant releaseTime;

}