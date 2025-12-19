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


@Table(name = "item_armor_quality")
@Db2DataBind(name = "ItemArmorQuality.db2", layoutHash = 0x85642CC0, fields = {
        @Db2Field(name = {"qualitymod1", "qualitymod2", "qualitymod3", "qualitymod4", "qualitymod5", "qualitymod6", "qualitymod7"}, type = Db2Type.FLOAT),
        @Db2Field(name = "itemLevel", type = Db2Type.SHORT, signed = true)
})
public class ItemArmorQuality implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Qualitymod1")
    private float qualitymod1;

    @Column("Qualitymod2")
    private float qualitymod2;

    @Column("Qualitymod3")
    private float qualitymod3;

    @Column("Qualitymod4")
    private float qualitymod4;

    @Column("Qualitymod5")
    private float qualitymod5;

    @Column("Qualitymod6")
    private float qualitymod6;

    @Column("Qualitymod7")
    private float qualitymod7;

    @Column("ItemLevel")
    private short itemLevel;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
