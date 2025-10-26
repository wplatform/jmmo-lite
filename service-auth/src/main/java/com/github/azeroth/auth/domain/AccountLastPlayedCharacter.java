package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("account_last_played_character")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLastPlayedCharacter {
    
    @Id
    @Column("accountId")
    private Long accountId;
    
    @Column("region")
    private Byte region;
    
    @Column("battlegroup")
    private Byte battlegroup;
    
    @Column("realmId")
    private Integer realmId;
    
    @Column("characterName")
    private String characterName;
    
    @Column("characterGUID")
    private Long characterGuid;
    
    @Column("lastPlayedTime")
    private Long lastPlayedTime;
}
