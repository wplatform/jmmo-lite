package com.github.azeroth.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "secret_digest")
public class SecretDigest {
    @Id
    @Column("id")
    private Integer id;

    @Column("digest")
    private String digest;
}