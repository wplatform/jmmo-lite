package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "guild_achievement")
public class GuildAchievement {
    @Id
    @Column("guildId")
    private Long guildId;

    @Id
    @Column("achievement")
    private Long achievement;


    @Column("date")
    private Long date;

    
    @Column("guids")
    private String guids;

}