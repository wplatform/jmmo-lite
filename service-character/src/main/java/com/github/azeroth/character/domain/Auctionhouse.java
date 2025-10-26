package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter

@Table(name = "auctionhouse")
public class Auctionhouse {
    @Id
    
    @Column("id")
    private int id;

    
    @Column("auctionHouseId")
    private Integer auctionHouseId;

    
    @Column("owner")
    private Integer owner;

    
    @Column("bidder")
    private Integer bidder;

    
    @Column("minBid")
    private Integer minBid;

    
    @Column("buyoutOrUnitPrice")
    private Integer buyoutOrUnitPrice;

    
    @Column("deposit")
    private Integer deposit;

    
    @Column("bidAmount")
    private Integer bidAmount;

    
    @Column("startTime")
    private Instant startTime;

    
    @Column("endTime")
    private Instant endTime;

    
    @Column("serverFlags")
    private Short serverFlags;

}