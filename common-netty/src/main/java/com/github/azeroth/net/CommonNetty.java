package com.github.azeroth.net;

import com.github.azeroth.net.server.ConnectionObserver;
import com.github.azeroth.net.server.NettyPipeline;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Internal helpers for reactor-netty contracts.
 *
 * @author Stephane Maldini
 */
public final class CommonNetty {

    // System properties names
    private static final Logger log = LoggerFactory.getLogger(CommonNetty.class);

    /**
     * Specifies whether the channel ID will be prepended to the log message when possible.
     * By default, it will be prepended.
     */
    static final boolean LOG_CHANNEL_INFO =
            Boolean.parseBoolean(System.getProperty("netty.logChannelInfo", "true"));

    /**
     * Default worker thread count, fallback to available processor
     * (but with a minimum value of 4).
     */
    public static final String IO_WORKER_COUNT = "netty.ioWorkerCount";
    /**
     * Default selector thread count, fallback to -1 (no selector thread)
     * <p><strong>Note:</strong> In most use cases using a worker thread also as a selector thread works well.
     * A possible use case for specifying a separate selector thread might be when the worker threads are too busy
     * and connections cannot be accepted fast enough.
     * <p><strong>Note:</strong> Although more than 1 can be configured as a selector thread count, in reality
     * only 1 thread will be used as a selector thread.
     */
    public static final String IO_SELECT_COUNT = "netty.ioSelectCount";
    /**
     * Default worker thread count for UDP, fallback to available processor
     * (but with a minimum value of 4).
     */
    public static final String UDP_IO_THREAD_COUNT = "netty.udp.ioThreadCount";
    /**
     * Default quiet period that guarantees that the disposal of the underlying LoopResources
     * will not happen, fallback to 2 seconds.
     */
    public static final String SHUTDOWN_QUIET_PERIOD = "netty.ioShutdownQuietPeriod";
    /**
     * Default maximum amount of time to wait until the disposal of the underlying LoopResources
     * regardless if a task was submitted during the quiet period, fallback to 15 seconds.
     */
    public static final String SHUTDOWN_TIMEOUT = "netty.ioShutdownTimeout";

    /**
     * Default value whether the native transport (epoll, kqueue) will be preferred,
     * fallback it will be preferred when available.
     */
    public static final String NATIVE = "netty.native";

    /**
     * Default SSL handshake timeout (milliseconds), fallback to 10 seconds.
     */
    public static final String SSL_HANDSHAKE_TIMEOUT = "netty.tcp.sslHandshakeTimeout";
    /**
     * Default value whether the SSL debugging on the client side will be enabled/disabled,
     * fallback to SSL debugging disabled.
     */
    public static final String SSL_CLIENT_DEBUG = "netty.tcp.ssl.client.debug";
    /**
     * Default value whether the SSL debugging on the server side will be enabled/disabled,
     * fallback to SSL debugging disabled.
     */
    public static final String SSL_SERVER_DEBUG = "netty.tcp.ssl.server.debug";


    /**
     * Specifies whether the Http Server access log will be enabled.
     * By default, it is disabled.
     */
    public static final String ACCESS_LOG_ENABLED = "netty.http.server.accessLogEnabled";

    /**
     *  Specifies the zone id used by the access log.
     */
    public static final ZoneId ZONE_ID_SYSTEM = ZoneId.systemDefault();

    public static void safeRelease(Object msg) {
        if (msg instanceof ReferenceCounted referenceCounted) {
            if (referenceCounted.refCnt() > 0) {
                referenceCounted.release();
            }
        }
    }

    /**
     * Append channel ID to a log message for correlated traces.
     * @param channel current channel associated with the msg
     * @param msg the log msg
     * @return a formatted msg
     */
    public static String format(Channel channel, String msg) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(msg, "msg");
        if (LOG_CHANNEL_INFO) {
            String channelStr;
            StringBuilder result;
            Connection connection = Connection.from(channel);
            if (connection instanceof ChannelOperations<?,?>) {
                channelStr = connection.toString();
                if (channelStr.charAt(0) != TRACE_ID_PREFIX) {
                    result = new StringBuilder(1 + channelStr.length() + 2 + msg.length())
                            .append(CHANNEL_ID_PREFIX)
                            .append(channelStr)
                            .append(CHANNEL_ID_SUFFIX_1);
                }
                else {
                    result = new StringBuilder(channelStr.length() + 1 + msg.length())
                            .append(channelStr)
                            .append(CHANNEL_ID_SUFFIX_2);
                }
                return result.append(msg)
                        .toString();
            }
            else {
                channelStr = channel.toString();
                if (channelStr.charAt(0) == CHANNEL_ID_PREFIX) {
                    channelStr = channelStr.substring(ORIGINAL_CHANNEL_ID_PREFIX_LENGTH);
                    result = new StringBuilder(1 + channelStr.length() + 1 + msg.length())
                            .append(CHANNEL_ID_PREFIX)
                            .append(channelStr);
                }
                else {
                    int ind = channelStr.indexOf(ORIGINAL_CHANNEL_ID_PREFIX);
                    result = new StringBuilder(1 + (channelStr.length() - ORIGINAL_CHANNEL_ID_PREFIX_LENGTH) + 1 + msg.length())
                            .append(channelStr.substring(0, ind))
                            .append(CHANNEL_ID_PREFIX)
                            .append(channelStr.substring(ind + ORIGINAL_CHANNEL_ID_PREFIX_LENGTH));
                }
                return result.append(CHANNEL_ID_SUFFIX_2)
                        .append(msg)
                        .toString();
            }
        }
        else {
            return msg;
        }
    }

    /**
     * Pretty hex dump will be returned when the object is {@link ByteBuf} or {@link ByteBufHolder}.
     *
     * @deprecated as of 1.1.0. This will be removed in 2.0.0 as the functionality is not used anymore.
     */
    @Deprecated
    public static String toPrettyHexDump(Object msg) {
        Objects.requireNonNull(msg, "msg");
        String result;
        if (msg instanceof ByteBufHolder &&
                !Objects.equals(Unpooled.EMPTY_BUFFER, ((ByteBufHolder) msg).content())) {
            ByteBuf buffer = ((ByteBufHolder) msg).content();
            result = "\n" + ByteBufUtil.prettyHexDump(buffer);
        }
        else if (msg instanceof ByteBuf) {
            result = "\n" + ByteBufUtil.prettyHexDump((ByteBuf) msg);
        }
        else {
            result = msg.toString();
        }
        return result;
    }



    /**
     * Wrap possibly fatal or singleton exception into a new exception instance in order to propagate in reactor flows without side effect.
     *
     * @return a wrapped {@link RuntimeException}
     */
    public static RuntimeException wrapException(Throwable throwable) {
        return new InternalNettyException(Objects.requireNonNull(throwable));
    }

    public static void addChunkedWriter(Connection c) {
        if (c.channel()
                .pipeline()
                .get(ChunkedWriteHandler.class) == null) {
            c.addHandlerLast(NettyPipeline.ChunkedWriter, new ChunkedWriteHandler());
        }
    }

    /**
     * A common implementation for the {@link Connection#addHandlerLast(String, ChannelHandler)}
     * method that can be reused by other implementors.
     * <p>
     * This implementation will look for reactor added handlers on the right hand side of
     * the pipeline, provided they are identified with the {@link NettyPipeline#RIGHT}
     * prefix, and add the handler just before the first of these.
     *
     * @param context the {@link Connection} on which to add the decoder.
     * @param name the name of the decoder.
     * @param handler the decoder to add before the final reactor-specific handlers.
     * @see Connection#addHandlerLast(String, ChannelHandler)
     */
    static void addHandlerBeforeReactorEndHandlers(Connection context, String
            name,	ChannelHandler handler) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(handler, "handler");

        Channel channel = context.channel();
        boolean exists = channel.pipeline().get(name) != null;

        if (exists) {
            if (log.isDebugEnabled()) {
                log.debug(format(channel, "Handler [{}] already exists in the pipeline, decoder has been skipped"),
                        name);
            }
            return;
        }

        //we need to find the correct position
        String before = null;
        for (String s : channel.pipeline().names()) {
            if (s.startsWith(NettyPipeline.RIGHT)) {
                before = s;
                break;
            }
        }

        if (before == null) {
            channel.pipeline().addLast(name, handler);
        }
        else {
            channel.pipeline().addBefore(before, name, handler);
        }


        if (log.isDebugEnabled()) {
            log.debug(format(channel, "Added decoder [{}] at the end of the user pipeline, full pipeline: {}"),
                    name,
                    channel.pipeline().names());
        }
    }

    /**
     * A common implementation for the {@link Connection#addHandlerFirst(String, ChannelHandler)}
     * method that can be reused by other implementors.
     * <p>
     * This implementation will look for reactor added handlers on the left hand side of
     * the pipeline, provided they are identified with the {@link NettyPipeline#LEFT}
     * prefix, and add the handler just after the last of these.
     *
     * @param context the {@link Connection} on which to add the decoder.
     * @param name the name of the encoder.
     * @param handler the encoder to add after the initial reactor-specific handlers.
     * @see Connection#addHandlerFirst(String, ChannelHandler)
     */
    static void addHandlerAfterReactorCodecs(Connection context, String
            name,
                                             ChannelHandler handler) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(handler, "handler");

        Channel channel = context.channel();
        boolean exists = channel.pipeline().get(name) != null;

        if (exists) {
            if (log.isDebugEnabled()) {
                log.debug(format(channel, "Handler [{}] already exists in the pipeline, encoder has been skipped"),
                        name);
            }
            return;
        }

        //we need to find the correct position
        String after = null;
        for (String s : channel.pipeline().names()) {
            if (s.startsWith(NettyPipeline.LEFT)) {
                after = s;
            }
        }

        if (after == null) {
            channel.pipeline().addFirst(name, handler);
        }
        else {
            channel.pipeline().addAfter(after, name, handler);
        }


        if (log.isDebugEnabled()) {
            log.debug(format(channel, "Added encoder [{}] at the beginning of the user pipeline, full pipeline: {}"),
                    name,
                    channel.pipeline().names());
        }
    }

    public static boolean mustChunkFileTransfer(Connection c, Path file) {
        // if channel multiplexing a parent channel as an http2 stream
        if (c.channel().parent() != null && c.channel().parent().pipeline().get(NettyPipeline.H2MultiplexHandler) != null) {
            return true;
        }
        ChannelPipeline p = c.channel().pipeline();
        return p.get(SslHandler.class) != null  ||
                p.get(NettyPipeline.CompressionHandler) != null ||
                (!(c.channel().eventLoop() instanceof NioEventLoop) &&
                        !"file".equals(file.toUri().getScheme()));
    }

    static void removeHandler(Channel channel, String name) {
        if (channel.isActive() && channel.pipeline()
                .context(name) != null) {
            channel.pipeline()
                    .remove(name);
            if (log.isDebugEnabled()) {
                log.debug(format(channel, "Removed handler: {}, pipeline: {}"),
                        name,
                        channel.pipeline());
            }
        }
        else if (log.isDebugEnabled()) {
            log.debug(format(channel, "Non Removed handler: {}, context: {}, pipeline: {}"),
                    name,
                    channel.pipeline()
                            .context(name),
                    channel.pipeline());
        }
    }

    static void replaceHandler(Channel channel, String name, ChannelHandler handler) {
        if (channel.isActive() && channel.pipeline()
                .context(name) != null) {
            channel.pipeline()
                    .replace(name, name, handler);
            if (log.isDebugEnabled()) {
                log.debug(format(channel, "Replaced handler: {}, pipeline: {}"),
                        name,
                        channel.pipeline());
            }
        }
        else if (log.isDebugEnabled()) {
            log.debug(format(channel, "Non Replaced handler: {}, context: {}, pipeline: {}"),
                    name,
                    channel.pipeline()
                            .context(name),
                    channel.pipeline());
        }
    }

    public static boolean isConnectionReset(Throwable err) {
        return ((err instanceof IOException && (err.getMessage() == null ||
                        err.getMessage()
                                .contains("Broken pipe") ||
                        err.getMessage()
                                .contains("Connection reset by peer"))) ||
                (err instanceof SocketException && err.getMessage() != null &&
                        err.getMessage()
                                .contains("Connection reset by peer")));
    }

    CommonNetty() {
    }

    /**
     * Creates InetSocketAddress instance. Numeric IP addresses will be detected and
     * resolved without doing reverse DNS lookups.
     *
     * @param hostname ip-address or hostname
     * @param port port number
     * @param resolve when true, resolve given hostname at instance creation time
     * @return InetSocketAddress for given parameters
     */
    public static InetSocketAddress createInetSocketAddress(String hostname, int port, boolean resolve) {
        requireNonNull(hostname, "hostname");
        InetSocketAddress inetAddressForIpString = createForIpString(hostname, port);
        if (inetAddressForIpString != null) {
            return inetAddressForIpString;
        }
        else {
            return resolve ? new InetSocketAddress(hostname, port) : InetSocketAddress.createUnresolved(hostname, port);
        }
    }

    /**
     * Creates InetSocketAddress that is always resolved. Numeric IP addresses will be
     * detected and resolved without doing reverse DNS lookups.
     *
     * @param hostname ip-address or hostname
     * @param port port number
     * @return InetSocketAddress for given parameters
     */
    public static InetSocketAddress createResolved(String hostname, int port) {
        return createInetSocketAddress(hostname, port, true);
    }

    /**
     * Creates unresolved InetSocketAddress. Numeric IP addresses will be detected and
     * resolved.
     *
     * @param hostname ip-address or hostname
     * @param port port number
     * @return InetSocketAddress for given parameters
     */
    public static InetSocketAddress createUnresolved(String hostname, int port) {
        return createInetSocketAddress(hostname, port, false);
    }

    /**
     * Parse unresolved InetSocketAddress. Numeric IP addresses will be detected and resolved.
     *
     * @param address ip-address or hostname
     * @param defaultPort the default port
     * @return {@link InetSocketAddress} for given parameters, only numeric IP addresses will be resolved
     */
    public static InetSocketAddress parseAddress(String address, int defaultPort) {
        return parseAddress(address, defaultPort, false);
    }

    /**
     * Parse unresolved InetSocketAddress. Numeric IP addresses will be detected and resolved.
     *
     * @param address ip-address or hostname
     * @param defaultPort is used if the address does not contain a port,
     * or if the port cannot be parsed in non-strict mode
     * @param strict if true throws an exception when the address cannot be parsed,
     * otherwise an unresolved {@link InetSocketAddress} is returned. It can include the case of the host
     * having been parsed but not the port (replaced by {@code defaultPort})
     * @return {@link InetSocketAddress} for given parameters, only numeric IP addresses will be resolved
     */
    public static InetSocketAddress parseAddress(String address, int defaultPort, boolean strict) {
        requireNonNull(address, "address");
        String host = address;
        int port = defaultPort;
        int separatorIdx = address.lastIndexOf(':');
        int ipV6HostSeparatorIdx = address.lastIndexOf(']');
        if (separatorIdx > ipV6HostSeparatorIdx) {
            if (separatorIdx == address.indexOf(':') || ipV6HostSeparatorIdx > -1) {
                host = address.substring(0, separatorIdx);
                String portStr = address.substring(separatorIdx + 1);
                if (!portStr.isEmpty()) {
                    if (portStr.chars().allMatch(Character::isDigit)) {
                        port = Integer.parseInt(portStr);
                    }
                    else if (strict) {
                        throw new IllegalArgumentException("Failed to parse a port from " + address);
                    }
                }
            }
            else if (strict) {
                throw new IllegalArgumentException("Invalid IPv4 address " + address);
            }
        }
        return createUnresolved(host, port);
    }

    /**
     * Replaces an unresolved InetSocketAddress with a resolved instance in the case that
     * the passed address is a numeric IP address (both IPv4 and IPv6 are supported).
     *
     * @param inetSocketAddress socket address instance to process
     * @return processed socket address instance
     */
    public static InetSocketAddress replaceUnresolvedNumericIp(InetSocketAddress inetSocketAddress) {
        requireNonNull(inetSocketAddress, "inetSocketAddress");
        if (!inetSocketAddress.isUnresolved()) {
            return inetSocketAddress;
        }
        InetSocketAddress inetAddressForIpString = createForIpString(
                inetSocketAddress.getHostString(), inetSocketAddress.getPort());
        if (inetAddressForIpString != null) {
            return inetAddressForIpString;
        }
        else {
            return inetSocketAddress;
        }
    }

    /**
     * Replaces an unresolved InetSocketAddress with a resolved instance in the case that
     * the passed address is unresolved.
     *
     * @param inetSocketAddress socket address instance to process
     * @return resolved instance with same host string and port
     */
    public static InetSocketAddress replaceWithResolved(InetSocketAddress inetSocketAddress) {
        requireNonNull(inetSocketAddress, "inetSocketAddress");
        if (!inetSocketAddress.isUnresolved()) {
            return inetSocketAddress;
        }
        inetSocketAddress = replaceUnresolvedNumericIp(inetSocketAddress);
        if (!inetSocketAddress.isUnresolved()) {
            return inetSocketAddress;
        }
        else {
            return new InetSocketAddress(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
        }
    }

    /**
     * Update the provided address with the new host string.
     *
     * @param address the address supplier
     * @param host the new host string
     * @return the updated address
     */
    public static SocketAddress updateHost(Supplier<? extends SocketAddress> address, String host) {
        if (address == null) {
            return createUnresolved(host, 0);
        }

        SocketAddress socketAddress = address.get();
        if (socketAddress instanceof DomainSocketAddress) {
            throw new IllegalArgumentException("Cannot update DomainSocketAddress with host name [" + host + "].");
        }

        if (!(socketAddress instanceof InetSocketAddress)) {
            return createUnresolved(host, 0);
        }

        InetSocketAddress inet = (InetSocketAddress) socketAddress;

        return createUnresolved(host, inet.getPort());
    }

    /**
     * Update the provided address with the new port.
     *
     * @param address the address supplier
     * @param port the new port
     * @return the updated address
     */
    public static SocketAddress updatePort(Supplier<? extends SocketAddress> address, int port) {
        if (address == null) {
            return createUnresolved(NetUtil.LOCALHOST.getHostAddress(), port);
        }

        SocketAddress socketAddress = address.get();
        if (socketAddress instanceof DomainSocketAddress) {
            throw new IllegalArgumentException("Cannot update DomainSocketAddress with post number [" + port + "].");
        }

        if (!(address.get() instanceof InetSocketAddress)) {
            return createUnresolved(NetUtil.LOCALHOST.getHostAddress(), port);
        }

        InetSocketAddress inet = (InetSocketAddress) address.get();

        InetAddress addr = inet.getAddress();

        String host = addr == null ? inet.getHostName() : addr.getHostAddress();

        return createUnresolved(host, port);
    }

    static InetAddress attemptParsingIpString(String hostname) {
        byte[] ipAddressBytes = NetUtil.createByteArrayFromIpAddressString(hostname);

        if (ipAddressBytes != null) {
            try {
                if (ipAddressBytes.length == 4) {
                    return Inet4Address.getByAddress(ipAddressBytes);
                }
                else {
                    return Inet6Address.getByAddress(null, ipAddressBytes, -1);
                }
            }
            catch (UnknownHostException e) {
                throw new RuntimeException(e); // Should never happen
            }
        }

        return null;
    }

    static InetSocketAddress createForIpString(String hostname, int port) {
        InetAddress inetAddressForIpString = attemptParsingIpString(hostname);
        if (inetAddressForIpString != null) {
            return new InetSocketAddress(inetAddressForIpString, port);
        }
        return null;
    }


    static final class OutboundIdleStateHandler extends IdleStateHandler {

        final Runnable onWriteIdle;

        OutboundIdleStateHandler(long idleTimeout, Runnable onWriteIdle) {
            super(0, idleTimeout, 0, TimeUnit.MILLISECONDS);
            this.onWriteIdle = requireNonNull(onWriteIdle, "onWriteIdle");
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx,
                                   IdleStateEvent evt) throws Exception {
            if (evt.state() == IdleState.WRITER_IDLE) {
                onWriteIdle.run();
            }
            super.channelIdle(ctx, evt);
        }
    }

    static final class InboundIdleStateHandler extends IdleStateHandler {

        final Runnable onReadIdle;

        InboundIdleStateHandler(long idleTimeout, Runnable onReadIdle) {
            super(idleTimeout, 0, 0, TimeUnit.MILLISECONDS);
            this.onReadIdle = requireNonNull(onReadIdle, "onReadIdle");
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx,
                                   IdleStateEvent evt) throws Exception {
            if (evt.state() == IdleState.READER_IDLE) {
                onReadIdle.run();
            }
            super.channelIdle(ctx, evt);
        }
    }


    /**
     * A handler that can be used to extract {@link ByteBuf} out of {@link ByteBufHolder},
     * optionally also outputting additional messages.
     *
     * @author Stephane Maldini
     * @author Simon Baslé
     */
    @ChannelHandler.Sharable
    static final class ExtractorHandler extends ChannelInboundHandlerAdapter {


        final BiConsumer<? super ChannelHandlerContext, Object> extractor;
        ExtractorHandler(BiConsumer<? super ChannelHandlerContext, Object> extractor) {
            this.extractor = Objects.requireNonNull(extractor, "extractor");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            extractor.accept(ctx, msg);
        }

    }

    static NettyInbound unavailableInbound(Connection c) {
        return new NettyInbound() {
            @Override
            public ByteBuf receive() {
                throw new IllegalStateException("Receiver Unavailable");
            }

            @Override
            public <T> T receiveObject(Class<T> clazz) {
                throw new IllegalStateException("Receiver Unavailable");
            }


            @Override
            public NettyInbound withConnection(Consumer<? super Connection> withConnection) {
                withConnection.accept(c);
                return this;
            }
        };
    }

    static NettyOutbound unavailableOutbound(Connection c) {
        return new NettyOutbound() {

            @Override
            public ByteBufAllocator alloc() {
                return null;
            }

            @Override
            public NettyOutbound send(ByteBuf message) {
                return null;
            }

            @Override
            public NettyOutbound sendObject(Object message) {
                return null;
            }

            @Override
            public NettyOutbound withConnection(Consumer<? super Connection> withConnection) {
                return null;
            }
        };
    }

    static final class InternalNettyException extends RuntimeException {

        InternalNettyException(Throwable cause) {
            super(cause);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

        private static final long serialVersionUID = 6643227207055930902L;
    }


    static final ConnectionObserver NOOP_LISTENER = (connection, newState) -> {};

    static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("$CONNECTION");



    static final Predicate<ByteBuf>        PREDICATE_BB_FLUSH    = b -> false;

    static final Predicate<Object>         PREDICATE_FLUSH       = o -> false;

    static final ByteBuf                   BOUNDARY              = Unpooled.EMPTY_BUFFER;

    static final char CHANNEL_ID_PREFIX = '[';
    static final String CHANNEL_ID_SUFFIX_1 = "] ";
    static final char CHANNEL_ID_SUFFIX_2 = ' ';
    static final String ORIGINAL_CHANNEL_ID_PREFIX = "[id: 0x";
    static final int ORIGINAL_CHANNEL_ID_PREFIX_LENGTH = ORIGINAL_CHANNEL_ID_PREFIX.length();
    static final char TRACE_ID_PREFIX = '(';

    @SuppressWarnings("ReferenceEquality")
    //Design to use reference comparison here
    public static final Predicate<ByteBuf> PREDICATE_GROUP_FLUSH = b -> b == BOUNDARY;

}
