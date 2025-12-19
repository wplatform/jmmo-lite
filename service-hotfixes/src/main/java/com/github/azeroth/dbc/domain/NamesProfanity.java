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


@Table(name = "names_profanity")
@Db2DataBind(name = "NamesProfanity.db2", layoutHash = 0xDFB56E0E, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "language", type = Db2Type.BYTE, signed = true)
})
public class NamesProfanity implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private String name;

    @Column("Language")
    private byte language;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
