package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "group_member")
public class GroupMember {
    @Id
    @Column("memberGuid")
    private Long id;

    @Column("guid")
    private Long guid;


    @Column("memberFlags")
    private Short memberFlags;


    @Column("subgroup")
    private Short subgroup;


    @Column("roles")
    private Short roles;

}