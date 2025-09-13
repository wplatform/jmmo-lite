package com.github.azeroth.game.service;

public class WorldServiceManager {
	private final java.util.concurrent.ConcurrentHashMap<(
    int serviceHash, int methodId),WorldServiceHandler>serviceHandlers;

    private WorldServiceManager() {
        serviceHandlers = new java.util.concurrent.ConcurrentHashMap<( int serviceHash, int methodId),
        WorldServiceHandler > ();

        var currentAsm = Assembly.GetExecutingAssembly();

        for (var type : currentAsm.GetTypes()) {
            for (var methodInfo : type.GetMethods(BindingFlags.instance.getValue() | BindingFlags.NonPublic.getValue())) {
                for (var serviceAttr : methodInfo.<ServiceAttribute>GetCustomAttributes()) {
                    if (serviceAttr == null) {
                        continue;
                    }

                    var key = (serviceAttr.serviceHash, serviceAttr.methodId);

                    if (serviceHandlers.containsKey(key)) {
                        Log.outError(LogFilter.Network, String.format("Tried to override ServiceHandler: %1$s with %2$s (ServiceHash: %3$s MethodId: %4$s)", serviceHandlers.get(key), methodInfo.name, serviceAttr.serviceHash, serviceAttr.methodId));

                        continue;
                    }

                    var parameters = methodInfo.GetParameters();

                    if (parameters.length == 0) {
                        Log.outError(LogFilter.Network, String.format("Method: %1$s needs atleast one paramter", methodInfo.name));

                        continue;
                    }

                    serviceHandlers.put(key, new WorldServiceHandler(methodInfo, parameters));
                }
            }
        }
    }

    public final WorldServiceHandler getHandler(int serviceHash, int methodId) {
        return serviceHandlers.get((serviceHash, methodId));
    }
}
