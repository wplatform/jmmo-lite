package com.github.azeroth.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class RealmCharacterCountList {
    private List<RealmCharacterCountEntry> counts;

}
