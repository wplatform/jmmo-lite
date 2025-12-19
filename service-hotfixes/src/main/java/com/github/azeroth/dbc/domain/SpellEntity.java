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


@Table(name = "spell")
@Db2DataBind(name = "Spell.db2", layoutHash = 0x2273DFFF, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = "nameSubtext", type = Db2Type.STRING),
        @Db2Field(name = "description", type = Db2Type.STRING),
        @Db2Field(name = "auraDescription", type = Db2Type.STRING)
})
public class SpellEntity implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("NameSubtext")
    private LocalizedString nameSubtext;

    @Column("Description")
    private LocalizedString description;

    @Column("AuraDescription")
    private LocalizedString auraDescription;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
