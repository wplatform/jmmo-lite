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


@Table(name = "languages")
@Db2DataBind(name = "Languages.db2", layoutHash = 0x6FA5D0C4, indexField = 1, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "id", type = Db2Type.INT)
})
public class Language implements DbcEntity {
    @Column("Name")
    private LocalizedString name;

    @Id

    @Column("ID")
    private int id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
