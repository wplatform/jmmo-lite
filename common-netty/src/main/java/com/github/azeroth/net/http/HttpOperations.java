package com.github.azeroth.net.http;

import com.github.azeroth.net.ChannelOperations;
import com.github.azeroth.net.CommonNetty;
import com.github.azeroth.net.Connection;
import com.github.azeroth.net.server.ConnectionObserver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class HttpOperations extends ChannelOperations<HttpServerRequest, HttpServerResponse>
        implements HttpServerRequest, HttpServerResponse {

    private final static Logger log = LoggerFactory.getLogger(HttpOperations.class);

    public static final int HTTP_CACHE_SECONDS = 60;
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(ZoneId.of("GMT"));

    private static final List<String> TEXTUAL_CONTENT_TYPE = Arrays.asList(
            "text/plain",
            "text/html",
            "text/xml",
            "text/css",
            "text/javascript",
            "application/json",
            "application/xml",
            "application/javascript",
            "application/x-javascript",
            "application/xhtml+xml",
            "application/x-www-form-urlencoded"
    );

    private Function<? super String, Map<String, String>> paramsResolver;


    private final HttpHeaders responseHeaders = new DefaultHttpHeaders();
    private HttpResponseStatus status = HttpResponseStatus.OK;
    private Map<String, String> params;
    private Map<String, Set<Cookie>> cachedCookies;
    final boolean validateHeaders = false;


    public HttpOperations(Channel channel, ConnectionObserver listener) {
        super(channel, listener);
    }

    @Override
    public <T> T receiveBody(Class<T> clazz) {
        return null;
    }

    @Override
    public Map<String, String> params() {
        if (this.params == null) {
            this.params = new HashMap<>();
            if (paramsResolver != null) {
                this.params.putAll(this.paramsResolver.apply(uri()));
            }
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri());
            Map<String, List<String>> parameters = queryStringDecoder.parameters();
            parameters.forEach((key, values) -> {
                this.params.put(key, String.join(", ", values));
            });
            return params;
        }

        return this.params;

    }

    @Override
    public Map<String, Set<Cookie>> cookies() {
        if (this.cachedCookies == null) {
            FullHttpRequest fullHttpRequest = receiveObject(FullHttpRequest.class);
            List<String> allCookieHeaders = fullHttpRequest.headers().getAll(HttpHeaderNames.COOKIE);
            Map<String, Set<Cookie>> cookies = new HashMap<>();
            for (String aCookieHeader : allCookieHeaders) {
                List<Cookie> decode = ServerCookieDecoder.STRICT.decodeAll(aCookieHeader);
                for (Cookie cookie : decode) {
                    Set<Cookie> existingCookiesOfNameSet = cookies.computeIfAbsent(cookie.name(), k -> new HashSet<>());
                    existingCookiesOfNameSet.add(cookie);
                }
            }
            this.cachedCookies = Collections.unmodifiableMap(cookies);
        }
        return this.cachedCookies;
    }


    public String receiveContent() {
        if (!Objects.equals(method(), HttpMethod.POST) || isFormUrlencoded() || isMultipart()) {
            throw new IllegalStateException("Request is not POST or does not have Content-Type with value 'application/x-www-form-urlencoded' or 'multipart/form-data'");
        }
        FullHttpRequest request = receiveObject(FullHttpRequest.class);
        ByteBuf content = request.content();
        if (content.isReadable()) {

        }
        return content.toString(CharsetUtil.UTF_8);

    }

    @Override
    public HttpServerRequest paramsResolver(Function<? super String, Map<String, String>> paramsResolver) {
        this.paramsResolver = paramsResolver;
        return this;
    }


    @Override
    public HttpServerResponse sendResponse(Object content, Charset charset) {

        ByteBuf body;

        if (content instanceof ByteBuf buf) {
            body = buf;
        } else if (content instanceof String strContent) {
            body = channel().alloc().buffer();
            body.writeCharSequence(strContent, charset);
        } else {
            body = channel().alloc().buffer();
            deserialize(content, body, charset);
        }
        FullHttpResponse response = prepareFullHttpResponse(body);
        sendFullHttpResponse(response);
        return this;
    }

    @Override
    public void sendFile(Path file, long position, long count) {
        FullHttpRequest request = receiveBody(FullHttpRequest.class);
        try (FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ)) {
            // Cache Validation
            String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {


                ZonedDateTime dateTime = ZonedDateTime.from(
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                                .parse(ifModifiedSince));

                // Only compare up to the second because the datetime format we send to the client
                // does not have milliseconds

                FileTime lastModifiedTime = Files.getLastModifiedTime(file);
                if (dateTime.toEpochSecond() == lastModifiedTime.toInstant().getEpochSecond()) {
                    sendNotModified();
                    return;
                }
            }
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
            HttpUtil.setContentLength(response, Files.size(file));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, Files.probeContentType(file));
            setDateAndCacheHeaders(response, file);
            if (!isKeepAlive()) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            } else if (protocol().equals(HttpVersion.HTTP_1_0)) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            // Write the initial line and the header.
            channel().write(response);
            if (CommonNetty.mustChunkFileTransfer(this, file)) {
                CommonNetty.addChunkedWriter(this);
                sendObject(new ChunkedNioFile(fileChannel, position, count, 1024));
            } else {
                sendObject(new DefaultFileRegion(fileChannel, position, count));
            }

            channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        } catch (IOException e) {
            log.error("access file {} error", file.getFileName(), e);
            sendNotFound();
        }
    }

    @Override
    public HttpServerResponse addCookie(Cookie cookie) {
        /*
         * response.headers().set(HttpHeaderNames.CONTENT_TYPE, Constants.JSON_TYPE);
         *             response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
         *             DefaultCookie cookie = new DefaultCookie(Constants.COOKIE_NAME, sessionIdEncoded);
         *             cookie.setDomain(domain);
         *             cookie.setMaxAge(maxAge);
         *             cookie.setPath("/");
         *             cookie.setHttpOnly(true);
         *             cookie.setSecure(true);
         */
        responseHeaders.set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
        return this;
    }

    @Override
    public HttpServerResponse addHeader(CharSequence name, CharSequence value) {
        responseHeaders.set(name, value);
        return this;
    }


    @Override
    public HttpServerResponse header(CharSequence name, CharSequence value) {
        this.responseHeaders.set(name, value);
        return this;
    }

    @Override
    public HttpServerResponse headers(HttpHeaders headers) {
        this.responseHeaders.set(headers);
        return this;
    }



    @Override
    public void sendNotFound() {
        this.status = HttpResponseStatus.NOT_FOUND;
        ByteBuf message = channel().alloc().buffer();
        message.writeCharSequence(status.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = prepareFullHttpResponse(message);
        sendFullHttpResponse(response);
    }

    @Override
    public void sendRedirect(String location) {
        this.status = HttpResponseStatus.FOUND;
        ByteBuf message = channel().alloc().buffer();
        message.writeCharSequence(status.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = prepareFullHttpResponse(message);
        sendFullHttpResponse(response);

    }

    @Override
    public void sendError(HttpResponseStatus status) {
        this.status = status;
        ByteBuf message = channel().alloc().buffer();
        message.writeCharSequence(status.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = prepareFullHttpResponse(message);
        sendFullHttpResponse(response);
    }


    @Override
    public HttpResponseStatus status() {
        return this.status;
    }

    @Override
    public HttpServerResponse status(HttpResponseStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public HttpServerResponse status(int status) {
        this.status = HttpResponseStatus.valueOf(status);
        return this;
    }

    @Override
    public HttpServerResponse trailerHeaders(Consumer<? super HttpHeaders> trailerHeaders) {
        return null;
    }


    @Override
    public HttpOperations withConnection(Consumer<? super Connection> withConnection) {
        Objects.requireNonNull(withConnection, "withConnection");
        withConnection.accept(this);
        return this;
    }



    protected abstract void deserialize(Object content, ByteBuf buffer, Charset charset);

    protected abstract  <T> T serialize(ByteBuf content, Class<T> clazz, Charset charset);

    private void sendFullHttpResponse(FullHttpResponse response) {

        cookies().forEach((key, value) -> {

        });

        HttpUtil.setContentLength(response, response.content().readableBytes());
        if (isKeepAlive()) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        } else {
            // We're going to close the connection as soon as the response is sent,
            // so we should also make it clear for the client.
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }
        ChannelFuture flushPromise = channel().writeAndFlush(response);
        if (!isKeepAlive()) {
            // Close the connection as soon as the response is sent.
            flushPromise.addListener(ChannelFutureListener.CLOSE);
        }


    }

    private void sendNotModified() {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateTimeFormatter.format(Instant.now()));
        sendFullHttpResponse(response);
    }


    private static void setDateAndCacheHeaders(HttpResponse response, Path file) throws IOException {
        Instant fileLastModified = Files.getLastModifiedTime(file).toInstant();
        Instant now = Instant.now();
        response.headers().set(HttpHeaderNames.DATE, dateTimeFormatter.format(now));
        response.headers().set(HttpHeaderNames.EXPIRES, dateTimeFormatter.format(now.plus(HTTP_CACHE_SECONDS, ChronoUnit.SECONDS)));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.LAST_MODIFIED, dateTimeFormatter.format(fileLastModified));
    }


    @Override
    protected void onInboundNext(ChannelHandlerContext ctx, Object msg) {

        if (!(msg instanceof FullHttpRequest request)) {
            CommonNetty.safeRelease(msg);
            throw new UnsupportedOperationException("Only type of FullHttpRequest message is supported, please config HttpObjectAggregator decoder for http.");
        }

        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
        }
        receivedMessage = msg;
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, alloc().buffer());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, new AsciiString("application/json; charset=utf-8"));
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
        }
        listener.onStateChange(this, ConnectionObserver.State.READ);

    }


    final FullHttpResponse prepareFullHttpResponse(ByteBuf buffer) {
        if (isContentAlwaysEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug(CommonNetty.format(channel(), "Dropped HTTP content, since response has " +
                        "1. [Content-Length: 0] or 2. there must be no content: {}"), buffer);
            }
            buffer.release();
            return newFullHttpResponse(Unpooled.EMPTY_BUFFER);
        } else {
            return newFullHttpResponse(buffer);
        }
    }

    final FullHttpResponse newFullHttpResponse(ByteBuf body) {
        if (!HttpMethod.HEAD.equals(method())) {
            responseHeaders.remove(HttpHeaderNames.TRANSFER_ENCODING);
            int code = status().code();
            if (!(HttpResponseStatus.NOT_MODIFIED.code() == code ||
                    HttpResponseStatus.NO_CONTENT.code() == code)) {
                responseHeaders.setInt(HttpHeaderNames.CONTENT_LENGTH, body.readableBytes());
            }
        }
        responseHeaders.remove(HttpHeaderNames.TRANSFER_ENCODING);

        return new DefaultFullHttpResponse(version(), status(), body, responseHeaders, DefaultHttpHeadersFactory.trailersFactory().withValidation(validateHeaders).newHeaders());
    }


    protected boolean isContentAlwaysEmpty() {
        int code = status().code();
        if (HttpResponseStatus.NOT_MODIFIED.code() == code) {
            responseHeaders.remove(HttpHeaderNames.TRANSFER_ENCODING)
                    .remove(HttpHeaderNames.CONTENT_LENGTH);
            return true;
        }
        return HttpResponseStatus.NO_CONTENT.code() == code ||
                HttpResponseStatus.RESET_CONTENT.code() == code;
    }


}
