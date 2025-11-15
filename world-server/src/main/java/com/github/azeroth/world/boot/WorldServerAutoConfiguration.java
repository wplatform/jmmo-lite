package com.github.azeroth.world.boot;

import com.github.azeroth.game.world.setting.WorldSetting;
import com.github.azeroth.net.server.DisposableServer;
import com.github.azeroth.net.server.LoopResources;
import com.github.azeroth.net.server.NettyPipeline;
import com.github.azeroth.utils.SysProperties;
import com.github.azeroth.world.World;
import com.github.azeroth.world.router.RouteBuilder;
import com.github.azeroth.world.server.WorldServer;
import com.github.azeroth.world.traffic.WorldClientInitializer;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


@ComponentScan(basePackages = "com.github.azeroth.world",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".handler.*Handler"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RouteBuilder.class)
        })
@RequiredArgsConstructor
public class WorldServerAutoConfiguration {

    private final World world;
    private final WorldSetting worldSetting;
    private final RouteBuilder routeBuilder;


    @Bean(destroyMethod = "disposeNow")
    public DisposableServer worldServer() {
        return WorldServer.create()
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .option(ChannelOption.SO_RCVBUF, SysProperties.PORTAL_SERVER_IO_SO_RCVBUF)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(255, 2048, 65535))
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .childOption(ChannelOption.SO_RCVBUF, SysProperties.PORTAL_SERVER_IO_SO_RCVBUF)
                .childOption(ChannelOption.SO_SNDBUF, SysProperties.PORTAL_SERVER_IO_SO_SNDBUF)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .withWorldInstance(world)
                .doOnConnection(connection -> connection.onReadIdle(5, connection::close))
                .doOnChannelInit((observer, channel, address) -> channel.pipeline().addLast(NettyPipeline.WorldClientInitializer, new WorldClientInitializer()))
                .route(routeBuilder::configure)
                .bindAddress(() -> new InetSocketAddress(worldSetting.bindIP, worldSetting.worldServerPort))
                .runOn(LoopResources.create(SysProperties.World_SERVER_IO_WORKER_THREAD_NAME,
                                worldSetting.useProcessors, true),
                        SysProperties.PORTAL_SERVER_IO_PREFERNATIVE
                ).taskExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(SysProperties.WORLD_SERVER_TASK_HANDLER_THREAD_NAME).factory()))
                .bindNow();
    }


}
