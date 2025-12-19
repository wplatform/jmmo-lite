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


@Table(name = "language_words")
@Db2DataBind(name = "LanguageWords.db2", layoutHash = 0xC15912BD, fields = {
        @Db2Field(name = "word", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "languageID", type = Db2Type.BYTE)
})
public class LanguageWord implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Word")
    private String word;

    @Column("LanguageID")
    private short languageID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
