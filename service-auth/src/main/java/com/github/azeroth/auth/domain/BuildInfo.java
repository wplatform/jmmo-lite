package com.github.azeroth.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "build_info")
public class BuildInfo {
    @Id
    @Column("build")
    private Integer build;

    @Column("majorVersion")
    private Integer majorVersion;

    @Column("minorVersion")
    private Integer minorVersion;

    @Column("bugfixVersion")
    private Integer bugfixVersion;

    @Column("hotfixVersion")
    private Integer hotfixVersion;
}
