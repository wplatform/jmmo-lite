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


@Table(name = "item_level_selector_quality")
@Db2DataBind(name = "ItemLevelSelectorQuality.db2", layoutHash = 0xB7174A51, parentIndexField = 2, fields = {
        @Db2Field(name = "qualityItemBonusListID", type = Db2Type.INT, signed = true),
        @Db2Field(name = "quality", type = Db2Type.BYTE, signed = true),
        @Db2Field(name = "parentILSQualitySetID", type = Db2Type.SHORT, signed = true)
})
public class ItemLevelSelectorQuality implements DbcEntity, Comparable<ItemLevelSelectorQuality> {
    @Id

    @Column("ID")
    private int id;

    @Column("QualityItemBonusListID")
    private int qualityItemBonusListID;

    @Column("Quality")
    private byte quality;

    @Column("ParentILSQualitySetID")
    private short parentILSQualitySetID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

    @Override
    public int compareTo(ItemLevelSelectorQuality o) {
        return Byte.compare(this.quality, o.quality);
    }
}
