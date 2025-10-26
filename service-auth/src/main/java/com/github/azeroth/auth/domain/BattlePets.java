package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("battle_pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattlePets {
    
    @Id
    @Column("guid")
    private Long guid;
    
    @Column("battlenetAccountId")
    private Long battlenetAccountId;
    
    @Column("species")
    private Integer species;
    
    @Column("breed")
    private Short breed;
    
    @Column("level")
    private Short level;
    
    @Column("exp")
    private Short exp;
    
    @Column("health")
    private Integer health;
    
    @Column("quality")
    private Byte quality;
    
    @Column("flags")
    private Short flags;
    
    @Column("name")
    private String name;
}
