package com.github.azeroth.character.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GuildBankEventlogId implements Serializable {
    public Long guildid;

    public Long logGuid;

    public Short tabId;


}