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
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "spell_reagents_currency")
@Db2DataBind(name = "SpellReagentsCurrency.db2", layoutHash = 0xA7C3638C, parentIndexField = 0, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "currencyTypesID", type = Db2Type.INT),
        @Db2Field(name = "currencyCount", type = Db2Type.INT),
        @Db2Field(name = "", type = Db2Type.INT, signed = true)
})
public class SpellReagentsCurrency implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Column("SpellID")
    private Long spellID;


    @Column("CurrencyTypesID")
    private int currencyTypesID;


    @Column("CurrencyCount")
    private int currencyCount;

}