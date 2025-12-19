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


@Table(name = "names_reserved")
@Db2DataBind(name = "NamesReserved.db2", layoutHash = 0xE4923C1F, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING_NOT_LOCALIZED)
})
public class NamesReserved implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Name")
    private String name;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
