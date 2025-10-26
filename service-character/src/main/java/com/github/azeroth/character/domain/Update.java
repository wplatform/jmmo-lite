package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter

@Table(name = "updates")
public class Update {
    @Id
    @Column("name")
    private String name;


    @Column("hash")
    private String hash;


    
    @Column("state")
    private String state;


    @Column("timestamp")
    private Instant timestamp;


    @Column("speed")
    private Long speed;

}