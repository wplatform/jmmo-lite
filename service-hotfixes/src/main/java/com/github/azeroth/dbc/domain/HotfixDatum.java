package com.github.azeroth.dbc.domain;

import org.springframework.data.relational.core.mapping.Column;
import jakarta.persistence.EmbeddedId;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)

@Table(name = "hotfix_data")
public class HotfixDatum {
    @EmbeddedId
    private HotfixDatumId id;

    
    @Column("UniqueId")
    private Long uniqueId;

    
    @Column("Status")
    private short status;

    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}