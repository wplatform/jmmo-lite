package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2Type;
import com.github.azeroth.dbc.defines.FactionMask;
import com.github.azeroth.dbc.defines.FactionTemplateFlag;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.util.Objects;

@Getter
@Setter
@ToString
@Table(name = "faction_template")
@Db2DataBind(name = "FactionTemplate.db2", layoutHash = 0x6F1D2135, fields = {
        @Db2Field(name = "faction", type = Db2Type.SHORT),
        @Db2Field(name = "flags", type = Db2Type.SHORT),
        @Db2Field(name = {"enemies1", "enemies2", "enemies3", "enemies4"}, type = Db2Type.SHORT),
        @Db2Field(name = {"friend1", "friend2", "friend3", "friend4"}, type = Db2Type.SHORT),
        @Db2Field(name = "factionGroup", type = Db2Type.BYTE),
        @Db2Field(name = "friendGroup", type = Db2Type.BYTE),
        @Db2Field(name = "enemyGroup", type = Db2Type.BYTE)
})
public class FactionTemplate implements DbcEntity {

    private static final byte MAX_FACTION_RELATIONS = 4;

    @Id
    
    @Column("ID")
    private int id;

    @Column("Faction")
    private short faction;

    @Column("Flags")
    private short flags;

    @Column("Enemies1")
    private short enemies1;

    @Column("Enemies2")
    private short enemies2;

    @Column("Enemies3")
    private short enemies3;

    @Column("Enemies4")
    private short enemies4;

    @Column("Friend1")
    private short friend1;

    @Column("Friend2")
    private short friend2;

    @Column("Friend3")
    private short friend3;

    @Column("Friend4")
    private short friend4;

    @Column("FactionGroup")
    private byte factionGroup;

    @Column("FriendGroup")
    private byte friendGroup;

    @Column("EnemyGroup")
    private byte enemyGroup;

    @Id
    @Column("VerifiedBuild")
    private Integer verifiedBuild;


    // helpers
    public boolean isFriendlyTo(FactionTemplate entry) {
        short[] enemies = {enemies1, enemies2, enemies3, enemies4};
        short[] friend = {friend1, friend2, friend3, friend4};
        if (this == entry)
            return true;
        if (entry.getFlags() > 0) {
            for (byte i = 0; i < MAX_FACTION_RELATIONS; ++i)
                if (Objects.equals(enemies[i], entry.getFaction()))
                    return false;
            for (byte i = 0; i < MAX_FACTION_RELATIONS; ++i)
                if (Objects.equals(friend[i], entry.getFaction()))
                    return true;
        }
        return (friendGroup & entry.getFactionGroup()) != 0 || (factionGroup & entry.friendGroup) != 0;
    }

    public boolean isHostileTo(FactionTemplate entry) {
        short[] enemies = {enemies1, enemies2, enemies3, enemies4};
        short[] friend = {friend1, friend2, friend3, friend4};
        if (this == entry)
            return false;
        if (entry.getFlags() > 0) {
            for (byte i = 0; i < MAX_FACTION_RELATIONS; ++i)
                if (Objects.equals(enemies[i], entry.getFaction()))
                    return true;
            for (byte i = 0; i < MAX_FACTION_RELATIONS; ++i)
                if (Objects.equals(friend[i], entry.getFaction()))
                    return false;
        }
        return (enemyGroup & entry.factionGroup) != 0;

    }

    public boolean isHostileToPlayers() {
        return (enemyGroup & FactionMask.PLAYER.value) != 0;
    }


    public boolean isNeutralToAll() {
        short[] enemies = {enemies1, enemies2, enemies3, enemies4};
        for (byte i = 0; i < MAX_FACTION_RELATIONS; ++i)
            if (enemies[i] != 0)
                return false;
        return enemyGroup == 0 && friendGroup == 0;
    }

    public boolean isContestedGuardFaction() {
        return (flags & FactionTemplateFlag.CONTESTED_GUARD.value) != 0;
    }

}
