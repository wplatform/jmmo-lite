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


@Table(name = "artifact_category")
@Db2DataBind(name = "ArtifactCategory.db2", layoutHash = 0x21328475, fields = {
        @Db2Field(name = "xpMultCurrencyID", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "xpMultCurveID", type = Db2Type.SHORT, signed = true)
})
public class ArtifactCategory implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("XpMultCurrencyID")
    private short xpMultCurrencyID;

    @Column("XpMultCurveID")
    private short xpMultCurveID;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
