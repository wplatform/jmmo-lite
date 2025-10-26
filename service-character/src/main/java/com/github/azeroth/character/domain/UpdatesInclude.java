package com.github.azeroth.character.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

@Table(name = "updates_include")
public class UpdatesInclude {
    @Id
    @Column("path")
    private String path;


    
    @Column("state")
    private String state;

}