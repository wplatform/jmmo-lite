package com.github.azeroth.dbc.domain;

import com.github.azeroth.common.LocalizedString;
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


@Table(name = "mail_template")
@Db2DataBind(name = "MailTemplate.db2", layoutHash = 0x25C8D6CC, fields = {
        @Db2Field(name = "body", type = Db2Type.STRING)
})
public class MailTemplate implements DbcEntity {
    @Id

    @Column("ID")
    private int id;

    @Column("Body")
    private LocalizedString body;

    @Id

    @Column("VerifiedBuild")
    private int verifiedBuild;

}
