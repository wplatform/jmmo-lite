package com.github.azeroth.game.service;


import Google.Protobuf.*;
import com.github.azeroth.game.networking.packet.MethodCall;
import game.WorldSession;

public class WorldServiceHandler {
    private final Delegate methodCaller;
    private final Class requestType;
    private final Class responseType;

    public WorldServiceHandler(java.lang.reflect.Method info, ParameterInfo[] parameters) {
        requestType = parameters[0].ParameterType;

        if (parameters.length > 1) {
            responseType = parameters[1].ParameterType;
        }

        if (responseType != null) {
            methodCaller = info.CreateDelegate(Expression.GetDelegateType(new Class[]{WorldSession.class, requestType, responseType, info.ReturnType}));
        } else {
            methodCaller = info.CreateDelegate(Expression.GetDelegateType(new Class[]{WorldSession.class, requestType, info.ReturnType}));
        }
    }

    public final void invoke(WorldSession session, MethodCall methodCall, CodedInputStream stream) {
        var request = (IMessage) requestType.newInstance();
        request.MergeFrom(stream);

        BattlenetRpcErrorCode status;

        if (responseType != null) {
            var response = (IMessage) responseType.newInstance();
            status = (BattlenetRpcErrorCode) methodCaller.DynamicInvoke(session, request, response);
            Log.outDebug(LogFilter.ServiceProtobuf, "{0} Client called server Method: {1}) Returned: {2} Status: {3}.", session.getRemoteAddress(), request, response, status);

            if (status == 0) {
                session.sendBattlenetResponse(methodCall.getServiceHash(), methodCall.getMethodId(), methodCall.token, response);
            } else {
                session.sendBattlenetResponse(methodCall.getServiceHash(), methodCall.getMethodId(), methodCall.token, status);
            }
        } else {
            status = (BattlenetRpcErrorCode) methodCaller.DynamicInvoke(session, request);
            Log.outDebug(LogFilter.ServiceProtobuf, "{0} Client called server Method: {1}) Status: {2}.", session.getRemoteAddress(), request, status);

            if (status != 0) {
                session.sendBattlenetResponse(methodCall.getServiceHash(), methodCall.getMethodId(), methodCall.token, status);
            }
        }
    }
}
