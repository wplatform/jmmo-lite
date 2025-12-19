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

@Table(name = "hotfix_blob")
public class HotfixBlob {
    @EmbeddedId
    private HotfixBlobId id;

    @Column("`Blob`")
    private byte[] blob;

    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}