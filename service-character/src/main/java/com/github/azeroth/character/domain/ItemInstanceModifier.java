package com.github.azeroth.character.domain;

import org.springframework.data.relational.core.mapping.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "item_instance_modifiers")
public class ItemInstanceModifier {
    @Id
    @Column("itemGuid")
    private Long id;


    @Column("fixedScalingLevel")
    private Long fixedScalingLevel;


    @Column("artifactKnowledgeLevel")
    private Long artifactKnowledgeLevel;


    @Column("itemReforgeId")
    private Long itemReforgeId;

}