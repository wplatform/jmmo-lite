package com.github.azeroth.portal.rpc;

import bnet.protocol.MethodOptionsProto;
import bnet.protocol.RpcProto;
import bnet.protocol.ServiceOptionsProto;
import com.google.protobuf.*;
import com.github.azeroth.portal.proto.RpcPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultRpcChannel implements RpcChannel {

    private int token;

    @Override
    public void callMethod(Descriptors.MethodDescriptor method, RpcController controller,
                           Message request, Message responsePrototype, RpcCallback<Message> done) {

        DefaultRpcController rpcController = (DefaultRpcController) controller;
        ServiceOptionsProto.BGSServiceOptions serviceOptions = method.getService().getOptions().getExtension(ServiceOptionsProto.serviceOptions);
        MethodOptionsProto.BGSMethodOptions methodOptions = method.getOptions().getExtension(MethodOptionsProto.methodOptions);
        Integer serviceHash = ServiceMetadata.getServiceHashByServiceName(serviceOptions.getDescriptorName());
        if (serviceHash == null) {
            throw new IllegalStateException("Not have service hash for " + serviceOptions.getDescriptorName());
        }
        byte[] messageBody = request.toByteArray();
        RpcProto.Header header = RpcProto.Header.newBuilder()
                .setServiceId(0)
                .setServiceHash(serviceHash)
                .setMethodId(methodOptions.getId())
                .setSize(messageBody.length)
                .setToken(token++).build();
        RpcPacket packet = new RpcPacket(header, messageBody);
        rpcController.offer(packet);
        if(log.isInfoEnabled()) {
            String logMsg = " <--" + " service: " + Integer.toHexString(serviceHash) + "-" + serviceOptions.getDescriptorName() + "." + method.getName() +
                    " token: " + header.getToken() +
                    " status: " + header.getStatus();
            log.info(logMsg);
        }
    }
}
