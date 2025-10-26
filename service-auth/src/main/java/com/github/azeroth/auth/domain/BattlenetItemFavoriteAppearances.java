package com.github.azeroth.auth.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("battlenet_item_favorite_appearances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattlenetItemFavoriteAppearances {
    
    @Id
    @Column("battlenetAccountId")
    private Long battlenetAccountId;
    
    @Column("itemModifiedAppearanceId")
    private Integer itemModifiedAppearanceId;
}
