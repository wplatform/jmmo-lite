package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("realmlist")
public class RealmList {
    @Id
    @Column("id")
    private Integer id;

    @Column("name")
    private String name;

    @Column("address")
    private String address;
    
    @Column("localAddress")
    private String localAddress;

    @Column("address3")
    private String address3;

    @Column("address4")
    private String address4;
    
    @Column("localSubnetMask")
    private String localSubnetMask;
    
    @Column("port")
    private Integer port;

    @Column("icon")
    private Integer icon;

    @Column("flag")
    private Integer flag;

    @Column("timezone")
    private Integer timezone;

    @Column("allowedSecurityLevel")
    private Integer allowedSecurityLevel;

    @Column("population")
    private Float population;

    @Column("gamebuild")
    private Integer gameBuild;

    @Column("Region")
    private Byte region;

    @Column("Battlegroup")
    private Byte battlegroup;
}