package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


@Table(name = "gm_complaint_chatlog")
public class GmComplaintChatlog {
    @Id
    @Column("complaintId")
    private Long complaintId;

    @Id
    @Column("lineId")
    private Long lineId;

    @Column("timestamp")
    private Long timestamp;

    
    @Column("text")
    private String text;

}