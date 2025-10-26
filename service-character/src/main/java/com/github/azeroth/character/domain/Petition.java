package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "petition")
public class Petition {
    @Id
    @Column("ownerguid")
    private Long id;


    @Column("petitionguid")
    private Long petitionguid;

    @Column("name")
    private String name;

}