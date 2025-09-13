package com.github.azeroth.portal.handler;

import com.google.protobuf.Service;
import com.github.azeroth.net.NettyInbound;
import com.github.azeroth.net.NettyOutbound;

@FunctionalInterface
public interface PortalRpcHandler {

    void handle(NettyInbound in, NettyOutbound out, Service service);

}
