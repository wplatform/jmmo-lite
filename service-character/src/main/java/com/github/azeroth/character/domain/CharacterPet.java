package com.github.azeroth.character.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("character_pet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterPet {
    
    @Id
    @Column("id")
    private Integer id;
    
    @Column("entry")
    private Integer entry;
    
    @Column("owner")
    private Long owner;
    
    @Column("modelid")
    private Integer modelid;
    
    @Column("CreatedBySpell")
    private Integer createdBySpell;
    
    @Column("PetType")
    private Byte petType;
    
    @Column("level")
    private Short level;
    
    @Column("exp")
    private Integer exp;
    
    @Column("Reactstate")
    private Byte reactstate;
    
    @Column("name")
    private String name;
    
    @Column("renamed")
    private Byte renamed;
    
    @Column("slot")
    private Byte slot;
    
    @Column("curhealth")
    private Integer curhealth;
    
    @Column("curmana")
    private Integer curmana;
    
    @Column("savetime")
    private Integer savetime;
    
    @Column("abdata")
    private String abdata;
    
    @Column("specialization")
    private Short specialization;
}