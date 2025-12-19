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


@Table(name = "phase")
@Db2DataBind(name = "Phase.db2", layoutHash = 0x0043219C, fields = {
        @Db2Field(name = "flags", type = Db2Type.SHORT)
})
public class Phase implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Flags")
    private short flags;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
