package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("uptime")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Uptime {
    
    @Id
    @Column("realmid")
    private Integer realmId;
    
    @Column("starttime")
    private Long startTime;
    
    @Column("uptime")
    private Long uptime;
    
    @Column("maxplayers")
    private Short maxPlayers;
    
    @Column("revision")
    private String revision;
}
