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
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "item_name_description")
@Db2DataBind(name = "ItemNameDescription.db2", layoutHash = 0x16760BD4, fields = {
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "color", type = Db2Type.INT, signed = true)
})
public class ItemNameDescription implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    
    @Column("Description")
    private LocalizedString description;


    @Column("Color")
    private int color;

}