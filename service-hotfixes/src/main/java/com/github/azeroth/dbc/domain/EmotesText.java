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


@Table(name = "emotes_text")
@Db2DataBind(name = "EmotesText.db2", layoutHash = 0xE85AFA10, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "emoteID", type = Db2Type.SHORT)
})
public class EmotesText implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private String name;

    @Column("EmoteID")
    private short emoteID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
