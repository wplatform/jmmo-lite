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


@Table(name = "item_damage_two_hand_caster")
@Db2DataBind(name = "ItemDamageTwoHandCaster.db2", layoutHash = 0xC2186F95, fields = {
        @Db2Field(name = {"quality1", "quality2", "quality3", "quality4", "quality5", "quality6", "quality7"}, type = Db2Type.FLOAT),
        @Db2Field(name = "itemLevel", type = Db2Type.SHORT)
})
public class ItemDamageTwoHandCaster implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Quality1")
    private float quality1;

    @Column("Quality2")
    private float quality2;

    @Column("Quality3")
    private float quality3;

    @Column("Quality4")
    private float quality4;

    @Column("Quality5")
    private float quality5;

    @Column("Quality6")
    private float quality6;

    @Column("Quality7")
    private float quality7;

    @Column("ItemLevel")
    private short itemLevel;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
