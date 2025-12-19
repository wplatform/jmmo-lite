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


@Table(name = "item_effect")
@Db2DataBind(name = "ItemEffect.db2", layoutHash = 0xA390FA40, parentIndexField = 8, fields = {
        @Db2Field(name = "spellID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "coolDownMSec", type = Db2Type.INT, signed = true),
        @Db2Field(name = "categoryCoolDownMSec", type = Db2Type.INT, signed = true),
        @Db2Field(name = "charges", type = Db2Type.SHORT, signed = true),
        @Db2Field(name = "spellCategoryID", type = Db2Type.SHORT),
        @Db2Field(name = "chrSpecializationID", type = Db2Type.SHORT),
        @Db2Field(name = "legacySlotIndex", type = Db2Type.BYTE),
        @Db2Field(name = "triggerType", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "parentItemID", type = Db2Type.INT, signed = true)
})
public class ItemEffect implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("SpellID")
    private int spellID;

    @Column("CoolDownMSec")
    private int coolDownMSec;

    @Column("CategoryCoolDownMSec")
    private int categoryCoolDownMSec;

    @Column("Charges")
    private short charges;

    @Column("SpellCategoryID")
    private short spellCategoryID;

    @Column("ChrSpecializationID")
    private short chrSpecializationID;

    @Column("LegacySlotIndex")
    private byte legacySlotIndex;

    @Column("TriggerType")
    private byte triggerType;

    @Column("ParentItemID")
    private int parentItemID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
