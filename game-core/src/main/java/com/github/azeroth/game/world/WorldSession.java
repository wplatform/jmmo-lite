package com.github.azeroth.game.world;


import com.github.azeroth.auth.dto.RBACPermissions;
import com.github.azeroth.defines.Expansion;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.time.StopWatch;

public interface WorldSession {

    WorldContext getWorldContext();

    Player getPlayer();

    boolean hasPermission(RBACPermissions rbacPermissions);

    void setLatency(int latency);

    int getAccountId();

    int getBattleNetAccountId();

    Expansion getExpansion();

    void kickout(String reason);

    void setAttribute(String key, Object value);

    Object getAttribute(String key);

    Object removeAttribute(String key);

    ObjectGuid getBattleNetAccountGuid();

    StopWatch getStopWatch();
}
