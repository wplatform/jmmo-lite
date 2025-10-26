package com.github.azeroth.character.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("characters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Character {
    
    @Id
    @Column("guid")
    private Long guid;
    
    @Column("account")
    private Integer account;
    
    @Column("name")
    private String name;
    
    @Column("slot")
    private Byte slot;
    
    @Column("race")
    private Byte race;
    
    @Column("clazz")
    private Byte clazz;
    
    @Column("gender")
    private Byte gender;
    
    @Column("level")
    private Byte level;
    
    @Column("xp")
    private Integer xp;
    
    @Column("money")
    private Long money;
    
    @Column("inventorySlots")
    private Byte inventorySlots;
    
    @Column("bankSlots")
    private Byte bankSlots;
    
    @Column("restState")
    private Byte restState;
    
    @Column("playerFlags")
    private Integer playerFlags;
    
    @Column("playerFlagsEx")
    private Integer playerFlagsEx;
    
    @Column("position_x")
    private Float positionX;
    
    @Column("position_y")
    private Float positionY;
    
    @Column("position_z")
    private Float positionZ;
    
    @Column("map")
    private Short map;
    
    @Column("instance_id")
    private Integer instanceId;
    
    @Column("dungeonDifficulty")
    private Byte dungeonDifficulty;
    
    @Column("raidDifficulty")
    private Byte raidDifficulty;
    
    @Column("legacyRaidDifficulty")
    private Byte legacyRaidDifficulty;
    
    @Column("orientation")
    private Float orientation;
    
    @Column("taximask")
    private String taximask;
    
    @Column("online")
    private Byte online;
    
    @Column("cinematic")
    private Byte cinematic;
    
    @Column("totaltime")
    private Integer totaltime;
    
    @Column("leveltime")
    private Integer leveltime;
    
    @Column("logout_time")
    private Integer logoutTime;
    
    @Column("is_logout_resting")
    private Byte isLogoutResting;
    
    @Column("rest_bonus")
    private Float restBonus;
    
    @Column("resettalents_cost")
    private Integer resettalentsCost;
    
    @Column("resettalents_time")
    private Integer resettalentsTime;
    
    @Column("numRespecs")
    private Byte numRespecs;
    
    @Column("primarySpecialization")
    private Integer primarySpecialization;
    
    @Column("trans_x")
    private Float transX;
    
    @Column("trans_y")
    private Float transY;
    
    @Column("trans_z")
    private Float transZ;
    
    @Column("trans_o")
    private Float transO;
    
    @Column("transguid")
    private Long transguid;
    
    @Column("extra_flags")
    private Short extraFlags;
    
    @Column("stable_slots")
    private Byte stableSlots;
    
    @Column("at_login")
    private Short atLogin;
    
    @Column("zone")
    private Short zone;
    
    @Column("death_expire_time")
    private Integer deathExpireTime;
    
    @Column("taxi_path")
    private String taxiPath;
    
    @Column("totalKills")
    private Integer totalKills;
    
    @Column("todayKills")
    private Short todayKills;
    
    @Column("yesterdayKills")
    private Short yesterdayKills;
    
    @Column("chosenTitle")
    private Integer chosenTitle;
    
    @Column("watchedFaction")
    private Integer watchedFaction;
    
    @Column("drunk")
    private Byte drunk;
    
    @Column("health")
    private Integer health;
    
    @Column("power1")
    private Integer power1;
    
    @Column("power2")
    private Integer power2;
    
    @Column("power3")
    private Integer power3;
    
    @Column("power4")
    private Integer power4;
    
    @Column("power5")
    private Integer power5;
    
    @Column("power6")
    private Integer power6;
    
    @Column("latency")
    private Integer latency;
    
    @Column("activeTalentGroup")
    private Byte activeTalentGroup;
    
    @Column("lootSpecId")
    private Integer lootSpecId;
    
    @Column("exploredZones")
    private String exploredZones;
    
    @Column("equipmentCache")
    private String equipmentCache;
    
    @Column("knownTitles")
    private String knownTitles;
    
    @Column("actionBars")
    private Byte actionBars;
    
    @Column("deleteInfos_Account")
    private Integer deleteInfosAccount;
    
    @Column("deleteInfos_Name")
    private String deleteInfosName;
    
    @Column("deleteDate")
    private Integer deleteDate;
    
    @Column("honor")
    private Integer honor;
    
    @Column("honorLevel")
    private Integer honorLevel;
    
    @Column("honorRestState")
    private Byte honorRestState;
    
    @Column("honorRestBonus")
    private Float honorRestBonus;
    
    @Column("lastLoginBuild")
    private Integer lastLoginBuild;
}