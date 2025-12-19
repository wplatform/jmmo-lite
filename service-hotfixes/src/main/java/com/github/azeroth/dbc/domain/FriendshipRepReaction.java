package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import com.github.azeroth.common.LocalizedString;
import com.github.azeroth.dbc.db2.Db2Field;
import com.github.azeroth.dbc.db2.Db2DataBind;
import com.github.azeroth.dbc.db2.Db2Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString


@Table(name = "friendship_rep_reaction")
@Db2DataBind(name = "FriendshipRepReaction.db2", layoutHash = 0x9C412E5B, parentIndexField = 2, fields = {
        @Db2Field(name = "reaction", type = Db2Type.STRING),
        @Db2Field(name = "reactionThreshold", type = Db2Type.SHORT),
        @Db2Field(name = "friendshipRepID", type = Db2Type.BYTE)
})
public class FriendshipRepReaction implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Reaction")
    private LocalizedString reaction;

    @Column("ReactionThreshold")
    private int reactionThreshold;

    @Column("FriendshipRepID")
    private short friendshipRepID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
