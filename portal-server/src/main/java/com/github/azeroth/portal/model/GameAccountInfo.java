package com.github.azeroth.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameAccountInfo {

    private String display_name;
    private int expansion;
    private boolean is_suspended;
    private boolean is_banned;
    private long suspension_expires;
    private String suspension_reason;

}
