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


@Table(name = "broadcast_text")
@Db2DataBind(name = "BroadcastText.db2", layoutHash = 0x51BF0C33, fields = {
        @Db2Field(name = "text", type = Db2Type.STRING),
        @Db2Field(name = "text1", type = Db2Type.STRING),
        @Db2Field(name = {"emoteID1", "emoteID2", "emoteID3"}, type = Db2Type.SHORT),
        @Db2Field(name = {"emoteDelay1", "emoteDelay2", "emoteDelay3"}, type = Db2Type.SHORT),
        @Db2Field(name = "emotesID", type = Db2Type.SHORT),
        @Db2Field(name = "languageID", type = Db2Type.BYTE),
        @Db2Field(name = "flags", type = Db2Type.BYTE),
        @Db2Field(name = "conditionID", type = Db2Type.INT, signed = true),
        @Db2Field(name = {"soundEntriesID1", "soundEntriesID2"}, type = Db2Type.INT)
})
public class BroadcastText implements DbcEntity {
    @Id
    
    @Column("ID")
    private int id;

    @Column("Text")
    private LocalizedString text;

    @Column("Text1")
    private LocalizedString text1;

    @Column("EmoteID1")
    private short emoteID1;

    @Column("EmoteID2")
    private short emoteID2;

    @Column("EmoteID3")
    private short emoteID3;

    @Column("EmoteDelay1")
    private short emoteDelay1;

    @Column("EmoteDelay2")
    private short emoteDelay2;

    @Column("EmoteDelay3")
    private short emoteDelay3;

    @Column("EmotesID")
    private short emotesID;

    @Column("LanguageID")
    private byte languageID;

    @Column("Flags")
    private byte flags;

    @Column("ConditionID")
    private int conditionID;

    @Column("SoundEntriesID1")
    private int soundEntriesID1;

    @Column("SoundEntriesID2")
    private int soundEntriesID2;

    @Id
    
    @Column("VerifiedBuild")
    private int verifiedBuild;

}
