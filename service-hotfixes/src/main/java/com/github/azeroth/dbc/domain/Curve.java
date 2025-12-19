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


@Table(name = "curve")
@Db2DataBind(name = "Curve.db2", layoutHash = 0x17EA5154, fields = {
        @Db2Field(name = "type", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE)
})
public class Curve implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Type")
    private byte type;

    @Column("Flags")
    private byte flags;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
