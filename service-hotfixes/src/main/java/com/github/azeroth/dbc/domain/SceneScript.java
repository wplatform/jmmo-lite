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


@Table(name = "scene_script")
@Db2DataBind(name = "SceneScript.db2", layoutHash = 0xC694B81E, fields = {
        @Db2Field(name = "firstSceneScriptID", type = Db2Type.SHORT),
        @Db2Field(name = "nextSceneScriptID", type = Db2Type.SHORT)
})
public class SceneScript implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("FirstSceneScriptID")
    private short firstSceneScriptID;

    @Column("NextSceneScriptID")
    private short nextSceneScriptID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
