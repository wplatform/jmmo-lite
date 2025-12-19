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


@Table(name = "artifact_tier")
@Db2DataBind(name = "ArtifactTier.db2", layoutHash = 0x1A5A50B9, fields = {
        @Db2Field(name = "artifactTier", type = Db2Type.INT),
        @Db2Field(name = "maxNumTraits", type = Db2Type.INT),
        @Db2Field(name = "maxArtifactKnowledge", type = Db2Type.INT),
        @Db2Field(name = "knowledgePlayerCondition", type = Db2Type.INT),
        @Db2Field(name = "minimumEmpowerKnowledge", type = Db2Type.INT)
})
public class ArtifactTier implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("ArtifactTier")
    private int artifactTier;

    @Column("MaxNumTraits")
    private int maxNumTraits;

    @Column("MaxArtifactKnowledge")
    private int maxArtifactKnowledge;

    @Column("KnowledgePlayerCondition")
    private int knowledgePlayerCondition;

    @Column("MinimumEmpowerKnowledge")
    private int minimumEmpowerKnowledge;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
