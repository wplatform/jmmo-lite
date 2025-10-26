package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


@Table(name = "auction_items")
public class AuctionItem {
    @Id
    @Column("auctionId")
    private Integer auctionId;

    @Id
    @Column("itemGuid")
    private Integer itemGuid;

}