package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter


@Table(name = "petition_sign")
public class PetitionSign {
    @Id

    @Column("petitionguid")
    private Long petitionguid;

    @Id

    @Column("playerguid")
    private Long playerguid;

    @Column("ownerguid")
    private Long ownerguid;


    @Column("player_account")
    private Long playerAccount;

}