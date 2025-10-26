package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "realmcharacters")
public class Realmcharacters {
    @Id
    @Column("realmid")
    private Integer realmid;

    @Id
    @Column("acctid")
    private Integer acctid;

    @Column("numchars")
    private Integer numchars;
}
