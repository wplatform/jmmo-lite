package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "guild_achievement_progress")
public class GuildAchievementProgress {
    @Id
    @Column("guildId")
    private Long guildId;

    @Id
    @Column("criteria")
    private Long criteria;

    @Column("counter")
    private Long counter;


    @Column("date")
    private Long date;


    @Column("completedGuid")
    private Long completedGuid;

}