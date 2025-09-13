package com.github.azeroth.portal.handler;


import bnet.protocol.MethodOptionsProto;
import bnet.protocol.RpcProto;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Service;
import com.github.azeroth.common.RpcErrorCode;
import com.github.azeroth.net.NettyInbound;
import com.github.azeroth.net.NettyOutbound;
import com.github.azeroth.portal.exception.MethodNotFoundException;
import com.github.azeroth.portal.exception.ServiceNotFoundException;
import com.github.azeroth.portal.proto.RpcPacket;
import com.github.azeroth.portal.rpc.DefaultRpcController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class DefaultPortalRpcHandler implements PortalRpcHandler {


    @Override
    public void handle(NettyInbound in, NettyOutbound out, Service service) {
        RpcPacket packet = in.receiveObject(RpcPacket.class);
        AtomicReference<DefaultRpcController> controller = new AtomicReference<>();
        in.withConnection(connection -> {
            controller.set(new DefaultRpcController(connection));
        });
        try {

            List<Descriptors.MethodDescriptor> methods = service.getDescriptorForType().getMethods();
            Descriptors.MethodDescriptor methodDescriptor = methods.stream().filter(e -> {
                MethodOptionsProto.BGSMethodOptions extension = e.getOptions().getExtension(MethodOptionsProto.methodOptions);
                return extension.getId() == packet.getHeader().getMethodId();
            }).findFirst().orElseThrow(() -> new MethodNotFoundException("MethodId:" + packet.getHeader().getMethodId()));

            Message prototype = service.getRequestPrototype(methodDescriptor);
            Message request = prototype.newBuilderForType().mergeFrom(packet.getProtoData()).build();

            service.callMethod(methodDescriptor, controller.get(), request, message -> {
                RpcPacket rpcPacket = buildResponseRpcPacket(packet, controller.get(), message);
                out.sendObject(rpcPacket);
            });

        } catch (Exception e) {
            int status = RpcErrorCode.ERROR_RPC_SERVER_ERROR;
            if (e instanceof ServiceNotFoundException) {
                status = RpcErrorCode.ERROR_RPC_SERVICE_NOT_BOUND;
            } else if (e instanceof InvalidProtocolBufferException) {
                status = RpcErrorCode.ERROR_RPC_MALFORMED_REQUEST;
            }

            RpcProto.Header header = RpcProto.Header.newBuilder()
                    .setToken(packet.getHeader().getToken())
                    .setServiceId(0xFE)
                    .setErrorReason(e.getMessage())
                    .setStatus(status).build();
            out.sendObject(header);
        }
    }

    private RpcPacket buildResponseRpcPacket(RpcPacket packet, DefaultRpcController controller, Message result) {
        RpcPacket response;
        RpcProto.Header.Builder builder = RpcProto.Header.newBuilder()
                .setToken(packet.getHeader().getToken())
                .setServiceId(0xFE);
        if (!controller.failed()) {
            if (!(result instanceof RpcProto.NO_RESPONSE)) {
                builder.setSize(result.getSerializedSize());
                response = new RpcPacket(builder.build(), result.toByteArray());
            } else {
                response = new RpcPacket(builder.build());
            }
        } else {
            builder.setStatus(controller.status());
            response = new RpcPacket(builder.build());
        }
        return response;
    }
}
