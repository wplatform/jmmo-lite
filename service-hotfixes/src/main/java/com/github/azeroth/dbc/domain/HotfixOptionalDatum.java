package com.github.azeroth.dbc.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)

@Table(name = "hotfix_optional_data")
public class HotfixOptionalDatum {
    @Column("TableHash")
    private Long tableHash;

    @Column("RecordId")
    private Long recordId;

    @Column("locale")
    private String locale;

    @Id
    @Column("`Key`")
    private Long key;

    @Column("Data")
    private byte[] data;

    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}