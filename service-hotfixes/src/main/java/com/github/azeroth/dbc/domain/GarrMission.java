package com.github.azeroth.dbc.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)


@Table(name = "garr_mission")
public class GarrMission {
    @Id

    @Column("ID")
    private Long id;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;


    @Column("Name")
    private String name;


    @Column("Location")
    private String location;


    @Column("Description")
    private String description;


    @Column("MapPosX")
    private float mapPosX;


    @Column("MapPosY")
    private float mapPosY;


    @Column("WorldPosX")
    private float worldPosX;


    @Column("WorldPosY")
    private float worldPosY;


    @Column("GarrTypeID")
    private short garrTypeID;


    @Column("GarrMissionTypeID")
    private short garrMissionTypeID;


    @Column("GarrFollowerTypeID")
    private short garrFollowerTypeID;


    @Column("MaxFollowers")
    private short maxFollowers;


    @Column("MissionCost")
    private Long missionCost;


    @Column("MissionCostCurrencyTypesID")
    private int missionCostCurrencyTypesID;


    @Column("OfferedGarrMissionTextureID")
    private short offeredGarrMissionTextureID;


    @Column("UiTextureKitID")
    private int uiTextureKitID;


    @Column("EnvGarrMechanicID")
    private Long envGarrMechanicID;


    @Column("EnvGarrMechanicTypeID")
    private short envGarrMechanicTypeID;


    @Column("PlayerConditionID")
    private Long playerConditionID;


    @Column("TargetLevel")
    private byte targetLevel;


    @Column("TargetItemLevel")
    private int targetItemLevel;


    @Column("MissionDuration")
    private int missionDuration;


    @Column("TravelDuration")
    private int travelDuration;


    @Column("OfferDuration")
    private Long offerDuration;


    @Column("BaseCompletionChance")
    private short baseCompletionChance;


    @Column("BaseFollowerXP")
    private Long baseFollowerXP;


    @Column("OvermaxRewardPackID")
    private Long overmaxRewardPackID;


    @Column("FollowerDeathChance")
    private short followerDeathChance;


    @Column("AreaID")
    private Long areaID;


    @Column("Flags")
    private Long flags;


    @Column("GarrMissionSetID")
    private Long garrMissionSetID;

}