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


@Table(name = "achievement_category")
@Db2DataBind(name = "Achievement_Category.db2", layoutHash = 0xED226BC9, indexField = 3, parentIndexField = 2, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "parent", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "uiOrder", type = Db2Type.BYTE),
        @Db2Field(name = "id", type = Db2Type.INT, signed = true)
})
public class AchievementCategory implements DbcEntity {
    @Column("Name")
    private LocalizedString name;

    @Column("Parent")
    private short parent;

    @Column("UiOrder")
    private byte uiOrder;

    @Id
    
    @Column("ID")
    private int id;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
