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


@Table(name = "import_price_shield")
@Db2DataBind(name = "ImportPriceShield.db2", layoutHash = 0x6F64793D, fields = {
        @Db2Field(name = "data", type = Db2Type.FLOAT)
})
public class ImportPriceShield implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Data")
    private float data;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
