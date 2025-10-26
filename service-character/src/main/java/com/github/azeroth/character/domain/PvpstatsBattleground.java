package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter

@Table(name = "pvpstats_battlegrounds")
public class PvpstatsBattleground {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("id")
    private Long id;

    @Column("winner_faction")
    private Byte winnerFaction;

    @Column("bracket_id")
    private Short bracketId;

    @Column("type")
    private Long type;

    @Column("date")
    private Instant date;

}