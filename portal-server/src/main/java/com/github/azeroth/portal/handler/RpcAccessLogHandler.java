package com.github.azeroth.portal.handler;

import io.netty.channel.ChannelDuplexHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


public class RpcAccessLogHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger("rpc.server.AccessLog");


    private static final String MISSING_SERVICE = "-";
    private static final String MISSING_ADDR = "-";

    static String applyAddress(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
        } else {
            return MISSING_ADDR;
        }
    }
}
