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


@Table(name = "item_random_suffix")
@Db2DataBind(name = "ItemRandomSuffix.db2", layoutHash = 0x95CAB825, fields = {
        @Db2Field(name = "name", type = Db2Type.STRING),
        @Db2Field(name = {"enchantment1", "enchantment2", "enchantment3", "enchantment4", "enchantment5"}, type = Db2Type.SHORT),
        @Db2Field(name = {"allocationPct1", "allocationPct2", "allocationPct3", "allocationPct4", "allocationPct5"}, type = Db2Type.SHORT)
})
public class ItemRandomSuffix implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Name")
    private LocalizedString name;

    @Column("Enchantment1")
    private short enchantment1;

    @Column("Enchantment2")
    private short enchantment2;

    @Column("Enchantment3")
    private short enchantment3;

    @Column("Enchantment4")
    private short enchantment4;

    @Column("Enchantment5")
    private short enchantment5;

    @Column("AllocationPct1")
    private short allocationPct1;

    @Column("AllocationPct2")
    private short allocationPct2;

    @Column("AllocationPct3")
    private short allocationPct3;

    @Column("AllocationPct4")
    private short allocationPct4;

    @Column("AllocationPct5")
    private short allocationPct5;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
