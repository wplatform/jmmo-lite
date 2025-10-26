package com.github.azeroth.character.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("character_banned")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterBanned {
    
    @Id
    @Column("guid")
    private Long guid;
    
    @Column("bandate")
    private Integer banDate;
    
    @Column("unbandate")
    private Integer unbanDate;
    
    @Column("bannedby")
    private String bannedBy;
    
    @Column("banreason")
    private String banReason;
    
    @Column("active")
    private Byte active;
}