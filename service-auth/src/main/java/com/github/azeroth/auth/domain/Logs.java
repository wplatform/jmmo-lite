package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Logs {
    
    @Id
    @Column("time")
    private Long time;
    
    @Column("realm")
    private Integer realm;
    
    @Column("type")
    private Byte type;
    
    @Column("level")
    private Byte level;
    
    @Column("string")
    private String string;
}
