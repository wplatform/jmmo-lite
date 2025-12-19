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


@Table(name = "gem_properties")
@Db2DataBind(name = "GemProperties.db2", layoutHash = 0x84558CAB, fields = {
        @Db2Field(name = "type", type = Db2Type.INT),
        @Db2Field(name = "enchantId", type = Db2Type.SHORT),
        @Db2Field(name = "minItemLevel", type = Db2Type.SHORT)
})
public class GemProperty implements DbcEntity {
    @Id
    @Column("ID")
    private int id;

    @Column("Type")
    private int type;

    @Column("EnchantId")
    private short enchantId;

    @Column("MinItemLevel")
    private short minItemLevel;

    @Id
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
