package com.github.azeroth.portal.rpc;

import bnet.protocol.MethodOptionsProto;
import bnet.protocol.ServiceOptionsProto;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Service;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ServiceMetadata {


    public static final int CHALLENGE_NOTIFY = 0xBBDA171F;
    public static final int AUTHENTICATION_CLIENT = 0x71240E35;
    public static final int AUTHENTICATION_SERVER = 0xDECFC01;
    public static final int CONNECTION_SERVICE = 0x65446991;
    public static final int ACCOUNT_NOTIFY = 0x62DA0891;
    public static final int GAME_UTILITIES = 0x3FC1274D;

    private static final Map<String, Integer> SERVICE_HASH_MAPPING = Map.of(
            "bnet.protocol.challenge.ChallengeNotify", 0xBBDA171F,
            "bnet.protocol.authentication.AuthenticationClient", 0x71240E35,
            "bnet.protocol.authentication.AuthenticationServer", 0xDECFC01,
            "bnet.protocol.connection.ConnectionService", 0x65446991,
            "bnet.protocol.account.AccountNotify", 0x54DFDA17,
            "bnet.protocol.account.AccountService", 0x62DA0891,
            "bnet.protocol.game_utilities.GameUtilities", 0x3FC1274D

    );

    private final Service instance;
    private final ServiceOptionsProto.BGSServiceOptions serviceOptions;
    private final Map<Integer, Descriptors.MethodDescriptor> methodDescriptorMap;

    public ServiceMetadata(Service instance) {
        this.instance = instance;
        this.serviceOptions = instance.getDescriptorForType().getOptions().getExtension(ServiceOptionsProto.serviceOptions);
        List<Descriptors.MethodDescriptor> methods = instance.getDescriptorForType().getMethods();
        this.methodDescriptorMap = new HashMap<>(methods.size(), 1f);
        methods.forEach(methodDescriptor -> {
            MethodOptionsProto.BGSMethodOptions methodOptions = methodDescriptor.getOptions().getExtension(MethodOptionsProto.methodOptions);
            methodDescriptorMap.put(methodOptions.getId(), methodDescriptor);
        });
    }

    public Descriptors.MethodDescriptor getMethodDescriptor(int method_id) {
        return methodDescriptorMap.get(method_id);
    }

    public Integer getServiceHash() {
        return SERVICE_HASH_MAPPING.get(serviceOptions.getDescriptorName());
    }

    public static Integer getServiceHashByServiceName(String serviceName) {
        return SERVICE_HASH_MAPPING.get(serviceName);
    }

}
