package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
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


@Table(name = "movie")
@Db2DataBind(name = "Movie.db2", layoutHash = 0xF3E9AE3B, fields = {
        @Db2Field(name = "audioFileDataID", type = Db2Type.INT),
        @Db2Field(name = "subtitleFileDataID", type = Db2Type.INT),
        @Db2Field(name = "volume", type = Db2Type.BYTE),
        @Db2Field(name = "keyID", type = Db2Type.BYTE)
})
public class Movie implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("AudioFileDataID")
    private int audioFileDataID;

    @Column("SubtitleFileDataID")
    private int subtitleFileDataID;

    @Column("Volume")
    private short volume;

    @Column("KeyID")
    private byte keyID;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
