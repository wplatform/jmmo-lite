package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
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


@Table(name = "barber_shop_style")
@Db2DataBind(name = "BarberShopStyle.db2", layoutHash = 0x670C71AE, indexField = 7, fields = {
        @Db2Field(name = "displayName", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "costModifier", type = Db2Type.FLOAT),
        @Db2Field(name = "type", type = Db2Type.BYTE),
        @Db2Field(name = "race", type = Db2Type.BYTE),
        @Db2Field(name = "sex", type = Db2Type.BYTE),
        @Db2Field(name = "data", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class BarberShopStyle implements DbcEntity {
    @Column("DisplayName")
    private LocalizedString displayName;

    @Column("Description")
    private LocalizedString description;

    @Column("CostModifier")
    private float costModifier;

    @Column("Type")
    private byte type;

    @Column("Race")
    private byte race;

    @Column("Sex")
    private byte sex;

    @Column("Data")
    private byte data;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
