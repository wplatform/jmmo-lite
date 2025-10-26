package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Getter
@Setter

@Table(name = "blackmarket_auctions")
public class BlackmarketAuction {
    @Id

    @Column("marketId")
    private int id;


    @Column("currentBid")
    private Integer currentBid;


    @Column("time")
    private Instant time;


    @Column("numBids")
    private Integer numBids;


    @Column("bidder")
    private Integer bidder;

}