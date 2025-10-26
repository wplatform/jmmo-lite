package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "gm_suggestion")
public class GmSuggestion {
    @Id
    @Column("id")
    private Long id;

    @Column("playerGuid")
    private Long playerGuid;

    
    @Column("note")
    private String note;

    
    @Column("createTime")
    private Long createTime;

    
    @Column("mapId")
    private Integer mapId;

    
    @Column("posX")
    private Float posX;

    
    @Column("posY")
    private Float posY;

    
    @Column("posZ")
    private Float posZ;

    
    @Column("facing")
    private Float facing;

    
    @Column("closedBy")
    private Long closedBy;

    
    @Column("assignedTo")
    private Long assignedTo;

    
    @Column("comment")
    private String comment;

}