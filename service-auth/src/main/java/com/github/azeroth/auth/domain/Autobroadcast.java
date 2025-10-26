package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("autobroadcast")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Autobroadcast {
    
    @Id
    @Column("realmid")
    private Integer realmId;
    
    @Column("id")
    private Byte id;
    
    @Column("weight")
    private Byte weight;
    
    @Column("text")
    private String text;
}
