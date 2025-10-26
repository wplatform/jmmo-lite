package com.github.azeroth.dbc.domain;

import com.github.azeroth.cache.DbcEntity;
import lombok.Data;

@Data
public class ItemContextPickerEntry implements DbcEntity {
    private int id;
    private byte ItemCreationContext;
    private byte OrderIndex;
    private int pVal;
    private int flags;
    private int playerConditionID;
    private int itemContextPickerID;
}
