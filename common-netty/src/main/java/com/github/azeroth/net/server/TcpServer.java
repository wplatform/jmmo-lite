package com.github.azeroth.net.server;

import com.github.azeroth.net.*;
import com.github.azeroth.net.ssl.SslProvider;
import com.github.azeroth.net.wiretap.AdvancedByteBufFormat;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TcpServer<T extends TcpServer<T>> {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    static final LoggingHandler LOGGING_HANDLER =
            AdvancedByteBufFormat.HEX_DUMP
                    .toLoggingHandler(TcpServer.class.getName(), LogLevel.DEBUG, Charset.defaultCharset());


    private final ServerBootstrap serverBootstrap;

    private Supplier<? extends SocketAddress> bindAddress;
    private ChannelGroup channelGroup;
    private ChannelPipelineConfigurer doOnChannelInit;
    private LoggingHandler loggingHandler;
    private DefaultLoopResources loopResources;
    private ConnectionObserver childObserver;
    private SslProvider sslProvider;
    private Consumer<? super DisposableServer> doOnBound;
    private ExecutorService taskExecutor;
    private boolean preferNative;
    private boolean waitForTasksToCompleteOnShutdown = true;

    protected TcpServer() {
        this.serverBootstrap = new ServerBootstrap();
    }

    protected TcpServer(ServerBootstrap serverBootstrap) {
        this.serverBootstrap = serverBootstrap;
    }


    public final DisposableServer bind() {
        ChannelFuture channelFuture = bind0();
        DisposableServer disposableServer = () -> {
            channelGroup.close().awaitUninterruptibly();
            Optional.ofNullable(channelGroup).ifPresent(e -> e.close().awaitUninterruptibly());
            channelFuture.channel().close().awaitUninterruptibly();
            loopResources.shutdownGracefully();
            shutdownTaskExecutor();
        };
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (doOnBound != null)
                doOnBound.accept(disposableServer);
        });
        return disposableServer;
    }


    public final DisposableServer bindNow() {
        try {
            ChannelFuture channelFuture = bind0().sync();
            DisposableServer disposableServer = () -> {
                Optional.ofNullable(channelGroup).ifPresent(e -> e.close().awaitUninterruptibly());
                channelFuture.channel().close().awaitUninterruptibly();
                loopResources.shutdownGracefully();
                shutdownTaskExecutor();
            };
            if (doOnBound != null)
                doOnBound.accept(disposableServer);
            return disposableServer;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }


    public final void bindUntilJavaShutdown(Consumer<DisposableServer> onStart) {
        DisposableServer facade = Objects.requireNonNull(bindNow(), "facade");
        if (onStart != null) {
            onStart.accept(facade);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(facade::disposeNow));

    }

    public T waitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        return self();
    }

    public <A> T attr(AttributeKey<A> key, A value) {
        Objects.requireNonNull(key, "key");
        serverBootstrap.attr(key, value);
        return self();
    }

    public <A> T childAttr(AttributeKey<A> key, A value) {
        Objects.requireNonNull(key, "key");
        serverBootstrap.childAttr(key, value);
        return self();
    }


    public T runOn(LoopResources channelResources) {
        Objects.requireNonNull(channelResources, "channelResources");
        return runOn(channelResources, LoopResources.DEFAULT_NATIVE);
    }

    public T runOn(LoopResources loopResources, boolean preferNative) {
        Objects.requireNonNull(loopResources, "loopResources");
        if (this.loopResources != null) {
            this.loopResources.shutdownGracefully();
        }
        this.loopResources = (DefaultLoopResources) loopResources;
        this.preferNative = preferNative;
        return self();
    }

    public T doOnChannelInit(ChannelPipelineConfigurer doOnChannelInit) {
        Objects.requireNonNull(doOnChannelInit, "doOnChannelInit");
        this.doOnChannelInit = this.doOnChannelInit == null ? doOnChannelInit : this.doOnChannelInit.andThen(doOnChannelInit);
        return self();
    }

    @SuppressWarnings("ReferenceEquality")
    public <O> T option(ChannelOption<O> key, O value) {
        Objects.requireNonNull(key, "key");
        serverBootstrap.option(key, value);
        return self();
    }

    public <A> T childOption(ChannelOption<A> key, A value) {
        Objects.requireNonNull(key, "key");
        // Reference comparison is deliberate
        serverBootstrap.childOption(key, value);
        return self();
    }


    public T doOnBound(Consumer<? super DisposableServer> doOnBound) {
        this.doOnBound = doOnBound;
        return self();
    }

    public T childObserve(ConnectionObserver observer) {
        Objects.requireNonNull(observer, "observer");
        childObserver = childObserver == null ? observer : childObserver.andThen(observer);
        return self();
    }

    public T doOnConnection(Consumer<? super Connection> other) {
        Objects.requireNonNull(other, "doOnConnected");
        ConnectionObserver newChildObserver = ((connection, newState) -> {
            if (newState == ConnectionObserver.State.CONFIGURED) {
                other.accept(connection);
            }
        });
        childObserver = childObserver == null ? newChildObserver : childObserver.andThen(newChildObserver);
        return self();
    }

    public T bindAddress(Supplier<? extends SocketAddress> bindAddressSupplier) {
        Objects.requireNonNull(bindAddressSupplier, "bindAddressSupplier");
        bindAddress = bindAddressSupplier;
        return self();
    }

    public T host(String host) {
        Objects.requireNonNull(host, "host");
        return bindAddress(() -> CommonNetty.updateHost(bindAddress, host));
    }

    public T port(int port) {
        return bindAddress(() -> CommonNetty.updatePort(bindAddress, port));
    }


    public T handle(BiConsumer<? super NettyInbound, ? super NettyOutbound> handler) {
        Objects.requireNonNull(handler, "handler");

        ConnectionObserver newChildObserver = ((connection, newState) -> {
            if (newState == ConnectionObserver.State.READ) {
                handler.accept(connection.inbound(), connection.outbound());
            }
        });
        childObserver = childObserver == null ? newChildObserver : childObserver.andThen(newChildObserver);
        return self();
    }


    public T secure(Consumer<? super SslProvider.SslContextSpec> sslProviderBuilder) {
        Objects.requireNonNull(sslProviderBuilder, "sslProviderBuilder");
        SslProvider.SslContextSpec builder = SslProvider.builder();
        sslProviderBuilder.accept(builder);
        this.sslProvider = ((SslProvider.Builder) builder).build();
        return self();
    }


    public T secure(SslProvider sslProvider) {
        Objects.requireNonNull(sslProvider, "sslProvider");
        this.sslProvider = sslProvider;
        return self();
    }


    public T wiretap(boolean enable) {
        if (enable) {
            this.loggingHandler = LOGGING_HANDLER;
        } else if (this.loggingHandler != null) {
            this.loggingHandler = null;
        }
        return self();
    }

    public T wiretap(String category) {
        Objects.requireNonNull(category, "category");
        return wiretap(category, LogLevel.DEBUG);
    }

    public T wiretap(String category, LogLevel level) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(level, "level");
        return wiretap(category, level, AdvancedByteBufFormat.HEX_DUMP);
    }

    public final T wiretap(String category, LogLevel level, AdvancedByteBufFormat format) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(format, "format");
        return wiretap(category, level, format, Charset.defaultCharset());
    }


    public final T wiretap(String category, LogLevel level, AdvancedByteBufFormat format, Charset charset) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(charset, "charset");
        LoggingHandler loggingHandler = format.toLoggingHandler(category, level, charset);
        if (loggingHandler.equals(this.loggingHandler)) {
            @SuppressWarnings("unchecked")
            T dup = (T) this;
            return dup;
        }
        this.loggingHandler = loggingHandler;
        return self();
    }

    public T channelGroup(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
        return self();
    }


    public T taskExecutor(ExecutorService taskExecutor) {
        if (this.taskExecutor != null) {
            this.taskExecutor.shutdown();
        }
        this.taskExecutor = taskExecutor;
        return self();
    }

    protected abstract T self();

    protected ChannelOperations.OnSetup onSetup() {
        return (ch, c, msg) -> new ChannelOperations<>(ch, c);
    }


    private void shutdownTaskExecutor() {
        log.debug("Shutting down TaskExecutor {} ", this.taskExecutor.getClass().getName());

        if (this.taskExecutor != null) {
            if (this.waitForTasksToCompleteOnShutdown) {
                this.taskExecutor.shutdown();
            } else {
                for (Runnable remainingTask : this.taskExecutor.shutdownNow()) {
                    if (remainingTask instanceof Future<?> future) {
                        future.cancel(true);
                    }
                }
            }
            try {
                if (!taskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Timed out while waiting for taskExecutor {} to terminate", this.taskExecutor.getClass().getName());
                }
            } catch (InterruptedException ex) {
                log.warn("Interrupted while waiting for taskExecutor {} to terminate", this.taskExecutor.getClass().getName());
                Thread.currentThread().interrupt();
            }

        }
    }

    private ChannelFuture bind0() {
        Objects.requireNonNull(bindAddress, "bindAddress");
        Objects.requireNonNull(loopResources, "loopResources");
        Objects.requireNonNull(taskExecutor, "taskExecutor");
        SocketAddress local = Objects.requireNonNull(bindAddress.get(), "Bind Address supplier returned null");
        if (local instanceof InetSocketAddress localInet) {
            if (localInet.isUnresolved()) {
                local = CommonNetty.createResolved(localInet.getHostName(), localInet.getPort());
            }
        }
        ConnectionObserver childObs = new ChildObserverProxy(this.childObserver, channelGroup, taskExecutor);
        serverBootstrap.group(loopResources.onServer(preferNative), loopResources.onServerSelect(preferNative))
                .channel(loopResources.channelType(preferNative))
                .childHandler(new TcpServerChannelInitializer(this.loggingHandler, this.sslProvider, childObs, this.onSetup(), this.doOnChannelInit));

        return serverBootstrap.bind(local);
    }


    final static class TcpServerChannelInitializer extends ChannelInitializer<Channel> {

        final LoggingHandler loggingHandler;
        final SslProvider sslProvider;
        final ConnectionObserver connectionObserver;

        final ChannelOperations.OnSetup opsFactory;

        final ChannelPipelineConfigurer doOnChannelInit;

        TcpServerChannelInitializer(LoggingHandler loggingHandler, SslProvider sslProvider, ConnectionObserver connectionObserver,
                                    ChannelOperations.OnSetup opsFactory, ChannelPipelineConfigurer doOnChannelInit) {
            this.loggingHandler = loggingHandler;
            this.sslProvider = sslProvider;
            this.connectionObserver = connectionObserver;
            this.opsFactory = opsFactory;
            this.doOnChannelInit = doOnChannelInit;
        }


        @Override
        protected void initChannel(Channel channel) {
            ChannelPipeline pipeline = channel.pipeline();

            if (loggingHandler != null) {
                pipeline.addFirst(NettyPipeline.LoggingHandler, loggingHandler);
            }

            ChannelOperations.addReactiveBridge(channel, opsFactory, connectionObserver);

            ChannelPipelineConfigurer _default = null;
            if (sslProvider != null) {
                _default = (observer, ch, remoteAddress) -> sslProvider.addSslHandler(ch, remoteAddress,
                        Boolean.parseBoolean(System.getProperty(CommonNetty.SSL_SERVER_DEBUG, "false")));
            }

            if (null == _default) {
                _default = doOnChannelInit;
            } else if (doOnChannelInit != null) {
                _default = _default.andThen(doOnChannelInit);
            }

            _default.onChannelInit(connectionObserver, channel, channel.remoteAddress());

            if (log.isDebugEnabled()) {
                log.debug(CommonNetty.format(channel, "Initialized pipeline {}"), pipeline);
            }
        }
    }


    record ChildObserverProxy(ConnectionObserver childObserver, ChannelGroup channelGroup,
                              ExecutorService taskExecutor) implements ConnectionObserver {

        @Override
        public void onUncaughtException(Connection connection, Throwable error) {
            ChannelOperations<?, ?> ops = ChannelOperations.get(connection.channel());
            if (ops == null && (error instanceof IOException || CommonNetty.isConnectionReset(error) ||
                    // DecoderException at this point might be SSL handshake issue or other TLS related issue.
                    // In case of HTTP if there is an HTTP decoding issue, it is propagated with
                    // io.netty.handler.codec.DecoderResultProvider
                    // and not with throwing an exception
                    error instanceof DecoderException)) {
                if (log.isDebugEnabled()) {
                    log.debug(CommonNetty.format(connection.channel(), "onUncaughtException(" + connection + ")"), error);
                }
            } else {
                log.error(CommonNetty.format(connection.channel(), "onUncaughtException(" + connection + ")"), error);
            }
            connection.close();
        }

        @Override
        public void onStateChange(Connection connection, State newState) {

            if (channelGroup != null && newState == State.CONNECTED) {
                channelGroup.add(connection.channel());
            }
            if (newState == State.DISCONNECTING) {
                if (connection.channel().isActive()) {
                    connection.close();
                }

            }
            taskExecutor.submit(() -> {
                try {
                    childObserver.onStateChange(connection, newState);
                } catch (Throwable throwable) {
                    log.error(CommonNetty.format(connection.channel(), "I/0 handler to process connection on " + newState + " error"), throwable);
                } finally {
                    Object received = connection.inbound().receiveObject(Object.class);
                    if (received != null) {
                        CommonNetty.safeRelease(received);
                    }
                }
            });
        }
    }

}
