package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "character_account_data")
public class CharacterAccountDatum {
    @Id

    @Column("guid")
    private Integer guid;

    @Id

    @Column("type")
    private Short type;


    @Column("time")
    private Long time;

    @Column("data")
    private byte[] data;

}