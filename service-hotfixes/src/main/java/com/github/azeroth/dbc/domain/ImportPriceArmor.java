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


@Table(name = "import_price_armor")
@Db2DataBind(name = "ImportPriceArmor.db2", layoutHash = 0x1F7A850F, fields = {
        @Db2Field(name = "clothModifier", type = Db2Type.FLOAT),
        @Db2Field(name = "leatherModifier", type = Db2Type.FLOAT),
        @Db2Field(name = "chainModifier", type = Db2Type.FLOAT),
        @Db2Field(name = "plateModifier", type = Db2Type.FLOAT)
})
public class ImportPriceArmor implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ClothModifier")
    private float clothModifier;

    @Column("LeatherModifier")
    private float leatherModifier;

    @Column("ChainModifier")
    private float chainModifier;

    @Column("PlateModifier")
    private float plateModifier;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
