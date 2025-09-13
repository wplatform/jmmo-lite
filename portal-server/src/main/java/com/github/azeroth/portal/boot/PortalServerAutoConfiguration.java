package com.github.azeroth.portal.boot;

import bgs.protocol.account.v1.AccountServiceProto;
import bgs.protocol.authentication.v1.AuthenticationServiceProto;
import bnet.protocol.connection.v1.ConnectionServiceProto;
import bnet.protocol.game_utilities.v1.GameUtilitiesServiceProto;
import com.github.azeroth.config.RefreshableValue;
import com.github.azeroth.net.server.DisposableServer;
import com.github.azeroth.net.server.LoopResources;
import com.github.azeroth.net.ssl.TcpSslContextSpec;
import com.github.azeroth.portal.PortalRpcServer;
import com.github.azeroth.portal.handler.RpcProtocolDecoder;
import com.github.azeroth.portal.handler.RpcProtocolEncoder;
import com.github.azeroth.portal.rpc.service.AccountService;
import com.github.azeroth.portal.rpc.service.AuthenticationService;
import com.github.azeroth.portal.rpc.service.ConnectionService;
import com.github.azeroth.portal.rpc.service.GameUtilitiesService;
import com.github.azeroth.utils.SysProperties;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@AllArgsConstructor
@ComponentScan("com.pandaria.portal")
public class PortalServerAutoConfiguration {


    @Bean
    @ConfigurationProperties("bnetserver")
    PortalProperties portalProperties() {
        return new PortalProperties();
    }

    @Bean
    @RefreshableValue
    @ConfigurationProperties("bnetserver.loginrest")
    LoginRestProperties loginRestProperties() {
        return new LoginRestProperties();
    }

    @Bean
    @ConfigurationProperties("bnetserver.wrongpass")
    WrongPassProperties wrongPassProperties() {
        return new WrongPassProperties();
    }

    @Bean
    @RefreshableValue
    @ConfigurationProperties("bnetserver.updates")
    UpdatesProperties updatesProperties() {
        return new UpdatesProperties();
    }


    private final ConnectionService connectionService;
    private final AuthenticationService authenticationService;
    private final AccountService accountService;
    private final GameUtilitiesService gameUtilitiesService;


    @Bean(destroyMethod = "disposeNow")
    public DisposableServer bNetPortalRpcServer() {
        PortalProperties portalProperties = portalProperties();
        return PortalRpcServer.create()
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .option(ChannelOption.SO_RCVBUF, SysProperties.PORTAL_SERVER_IO_SO_RCVBUF)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(255, 2048, 65535))
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .childOption(ChannelOption.SO_RCVBUF, SysProperties.PORTAL_SERVER_IO_SO_RCVBUF)
                .childOption(ChannelOption.SO_SNDBUF, SysProperties.PORTAL_SERVER_IO_SO_SNDBUF)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .secure(sslContextSpec -> {
                    sslContextSpec.sslContext(TcpSslContextSpec.forServer(portalProperties.getCertificatesFile(), portalProperties.getPrivateKeyFile()));
                }).doOnChannelInit((observer, channel, address) -> {
                    channel.pipeline().addLast("rpc.IdleHandler", new IdleStateHandler(0, 0, 30, TimeUnit.MINUTES));
                    channel.pipeline().addLast("rpc.RpcDecoder", new RpcProtocolDecoder());
                    channel.pipeline().addLast("rpc.RpcEncoder", new RpcProtocolEncoder());
                }).route(router -> {
                    router.service(0x65446991, ConnectionServiceProto.ConnectionService.newReflectiveService(connectionService));
                    router.service(0xDECFC01, AuthenticationServiceProto.AuthenticationService.newReflectiveService(authenticationService));
                    router.service(0x62DA0891, AccountServiceProto.AccountService.newReflectiveService(accountService));
                    router.service(0x3FC1274D, GameUtilitiesServiceProto.GameUtilitiesService.newReflectiveService(gameUtilitiesService));
                }).bindAddress(() -> new InetSocketAddress(portalProperties.getBindIP(), portalProperties.getBattleNetPort()))
                .runOn(LoopResources.create(SysProperties.PORTAL_SERVER_RPC_IO_WORKER_THREAD_NAME,
                                portalProperties.getUseProcessors(), true),
                        SysProperties.PORTAL_SERVER_IO_PREFERNATIVE
                ).taskExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(SysProperties.PORTAL_SERVER_RPC_TASK_HANDLER_THREAD_NAME).factory()))
                .bindNow();
    }

}
