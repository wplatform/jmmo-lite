package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GuildAchievementId implements Serializable {
    public Long guildId;

    public Long achievement;


}