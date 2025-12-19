package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "sandbox_scaling")
@Db2DataBind(name = "SandboxScaling.db2", layoutHash = 0x5200B7F5, fields = {
        @Db2Field(name = "minLevel", type = Db2Type.INT, signed = true),
        @Db2Field(name = "maxLevel", type = Db2Type.INT, signed = true),
        @Db2Field(name = "flags", type = Db2Type.INT, signed = true)
})
public class SandboxScaling implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("MinLevel")
    private int minLevel;

    @Column("MaxLevel")
    private int maxLevel;

    @Column("Flags")
    private int flags;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
