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


@Table(name = "guild_perk_spells")
@Db2DataBind(name = "GuildPerkSpells.db2", layoutHash = 0xC15D6E9F, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true)
})
public class GuildPerkSpell implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
