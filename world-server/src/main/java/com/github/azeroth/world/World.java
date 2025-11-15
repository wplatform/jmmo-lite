package com.github.azeroth.world;

import com.github.azeroth.auth.dto.AccountType;
import com.github.azeroth.defines.BattleNetRpcErrorCode;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.authentication.AuthResponse;
import com.github.azeroth.game.world.WorldContext;
import com.github.azeroth.game.world.WorldSession;
import com.github.azeroth.utils.SysProperties;
import com.github.azeroth.world.handler.CharacterHandler;
import com.github.azeroth.world.network.ConnectToKey;
import com.github.azeroth.world.network.ConnectionType;
import com.github.azeroth.world.network.NetworkOperations;
import com.github.azeroth.world.network.WorldConnection;
import com.github.azeroth.world.session.WorldServerSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Getter
@Setter
public class World implements WorldContext, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private AccountType playerSecurityLimit = AccountType.SEC_PLAYER;
    private final ExecutorService taskExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("WorldTaskExecutor").factory());
    private final ConcurrentHashMap<Integer, WorldSession> sessions = new ConcurrentHashMap<>();


    

    @Override
    public Player findConnectedPlayer(ObjectGuid guid) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Player findConnectedPlayerByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Player findPlayer(ObjectGuid guid) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Player findPlayerByLowGUID(int entry) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Player findPlayerByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }


    public boolean removeSession(WorldSession session) {
        WorldSession existingSession = sessions.get(session.getAccountId());
        if (existingSession != null) {
            if (existingSession.getAttribute("loadingPlayer") != null) {
                return false;
            }
            existingSession.kickout("World::RemoveSession");
        }
        return true;
    }

    public void addSession(WorldSession session) {
        //NOTE - Still there is race condition in WorldSession* being used in the Sockets

        //- kick already loaded player with same account (if any) and remove session
        //- if player is in loading and want to load again, return
        if (!removeSession(session)) {
            session.kickout("World::AddSession_ Couldn't remove the other session while on loading screen");
            return;
        }
        sessions.put(session.getAccountId(), session);
        ((WorldServerSession) session).initialize();


    }

    public void addInstanceConnection(WorldConnection conn) {
        if (!conn.isActive()) {
            return;
        }
        ConnectToKey key = new ConnectToKey(conn.getId());
        var session = (WorldServerSession)sessions.get(key.accountId);
        ConnectToKey instanceConnectKey = Optional.ofNullable(session)
                .map(e -> (ConnectToKey) e.getAttribute("instanceConnectKey"))
                .orElse(null);
        if (session == null || instanceConnectKey == null
                || instanceConnectKey.getRaw() != key.getRaw()) {
            conn.outbound().setWorldPacket(new AuthResponse(BattleNetRpcErrorCode.TIMED_OUT));
            return;
        }
        conn.as(NetworkOperations.class).setSession(session);
        session.getConnections().put(ConnectionType.INSTANCE, conn);
        getBean(CharacterHandler.class).handleContinuePlayerLogin(conn.inbound(), conn.outbound());
    }

}
