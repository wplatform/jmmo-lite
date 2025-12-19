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


@Table(name = "scene_script_global_text")
@Db2DataBind(name = "SceneScriptGlobalText.db2", layoutHash = 0xB9F8FDF1, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING_NOT_LOCALIZED),
        @Db2Field(name = "script", type = Db2Type.STRING_NOT_LOCALIZED)
})
public class SceneScriptGlobalText implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private String name;

    @Column("Script")
    private String script;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
