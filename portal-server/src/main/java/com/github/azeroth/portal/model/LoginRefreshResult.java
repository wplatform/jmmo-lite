package com.github.azeroth.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRefreshResult {
    private long login_ticket_expiry;
    private boolean is_expired;
}
