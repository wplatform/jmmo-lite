package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("battle_pet_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattlePetSlots {
    
    @Id
    @Column("id")
    private Byte id;
    
    @Column("battlenetAccountId")
    private Long battlenetAccountId;
    
    @Column("battlePetGuid")
    private Long battlePetGuid;
    
    @Column("locked")
    private Byte locked;
}
