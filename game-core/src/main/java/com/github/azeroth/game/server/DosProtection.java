package com.github.azeroth.game.server;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.HashMap;

public class DosProtection {
    private final Policy policy;
    private final Worldsession session;
    private final HashMap<Integer, PacketCounter> _PacketThrottlingMap = new HashMap<Integer, PacketCounter>();

    public DosProtection(WorldSession s) {
        session = s;
        policy = Policy.forValue(WorldConfig.getIntValue(WorldCfg.PacketSpoofPolicy));
    }

    //todo fix me
    public final boolean evaluateOpcode(WorldPacket packet, long time) {
        int maxPacketCounterAllowed = 0; // GetMaxPacketCounterAllowed(p.GetOpcode());

        // Return true if there no limit for the opcode
        if (maxPacketCounterAllowed == 0) {
            return true;
        }

        if (!_PacketThrottlingMap.containsKey(packet.GetOpcode())) {
            _PacketThrottlingMap.put(packet.GetOpcode(), new PacketCounter());
        }

        var packetCounter = _PacketThrottlingMap.get(packet.GetOpcode());

        if (packetCounter.lastReceiveTime != time) {
            packetCounter.lastReceiveTime = time;
            packetCounter.amountCounter = 0;
        }

        // Check if player is flooding some packets
        if (++packetCounter.amountCounter <= maxPacketCounterAllowed) {
            return true;
        }

        Log.outWarn(LogFilter.Network, "AntiDOS: Account {0}, IP: {1}, Ping: {2}, Character: {3}, flooding packet (opc: {4} (0x{4}), count: {5})", session.getAccountId(), session.getRemoteAddress(), session.getLatency(), session.getPlayerName(), packet.GetOpcode(), packetCounter.amountCounter);

        switch (policy) {
            case Log:
                return true;
            case Kick:
                Log.outInfo(LogFilter.Network, "AntiDOS: Player kicked!");

                return false;
            case Ban:
                var bm = BanMode.forValue(WorldConfig.getIntValue(WorldCfg.PacketSpoofBanmode));
                var duration = WorldConfig.getUIntValue(WorldCfg.PacketSpoofBanduration); // in seconds
                var nameOrIp = "";

                switch (bm) {
                    case Character: // not supported, ban account
                    case Account:
                        tangible.OutObject<String> tempOut_nameOrIp = new tangible.OutObject<String>();
                        global.getAccountMgr().getName(session.getAccountId(), tempOut_nameOrIp);
                        nameOrIp = tempOut_nameOrIp.outArgValue;

                        break;
                    case IP:
                        nameOrIp = session.getRemoteAddress();

                        break;
                }

                global.getWorldMgr().banAccount(bm, nameOrIp, duration, "DOS (Packet Flooding/Spoofing", "Server: AutoDOS");
                Log.outInfo(LogFilter.Network, "AntiDOS: Player automatically banned for {0} seconds.", duration);

                return false;
        }

        return true;
    }

    private enum Policy {
        Log,
        kick,
        Ban;

        public static final int SIZE = Integer.SIZE;

        public static Policy forValue(int value) {
            return values()[value];
        }

        public int getValue() {
            return this.ordinal();
        }
    }
}
