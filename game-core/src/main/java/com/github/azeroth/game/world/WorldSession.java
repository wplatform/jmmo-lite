package com.github.azeroth.game.world;


import com.github.azeroth.auth.dto.RBACPermissions;
import com.github.azeroth.game.entity.player.Player;

import java.io.Closeable;

public interface WorldSession {

    WorldContext getWorldContext();

    Player getPlayer();

    boolean hasPermission(RBACPermissions rbacPermissions);
}
