package com.github.azeroth.game.map;

class SharedInstanceLockData extends InstanceLockData {
    public int instanceId;

    protected void finalize() throws Throwable {
        // Cleanup database
        if (instanceId != 0) {
            global.getInstanceLockMgr().onSharedInstanceLockDataDelete(instanceId);
        }
    }
}
