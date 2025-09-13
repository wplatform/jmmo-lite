package com.github.azeroth.portal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import org.springframework.util.StringUtils;

public class HttpResponseHandler extends ChannelOutboundHandlerAdapter {

    private static final String CONTENT_TYPE_VALUE = "application/json;charset=utf-8";
    private static final String HEAD_CONTENT_TYPE = "Content-Type";
    private static final String HEAD_SERVER = "Server";


    private static final String HEAD_CONTENT_LENGTH = "Content-Length";



    private static final String SERVER_VALUE = "portal-restful-server";

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //The netty headers name in lowercase. The server work with wow client which does not honour the HTTP header case insensitivity rule.
        if (msg instanceof HttpResponse) {
            HttpHeaders headers = ((HttpResponse) msg).headers();
            headers.set(HEAD_SERVER, SERVER_VALUE);

            String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
            if(StringUtils.hasText(contentType)) {
                headers.remove(HttpHeaderNames.CONTENT_TYPE);
                headers.set(HEAD_CONTENT_TYPE, contentType);
            } else {
                headers.set(HEAD_CONTENT_TYPE, CONTENT_TYPE_VALUE);
            }

            String contentLength = headers.get(HttpHeaderNames.CONTENT_LENGTH);
            headers.remove(HttpHeaderNames.CONTENT_LENGTH);
            headers.set(HEAD_CONTENT_LENGTH, contentLength);

            //the netty will not close the connection while the header connection is keepalive or null.
            //It always turn to the connection header with low case.
            //For wow client compatibility. do not set the connection header.
        }
        super.write(ctx, msg, promise);
    }
}
