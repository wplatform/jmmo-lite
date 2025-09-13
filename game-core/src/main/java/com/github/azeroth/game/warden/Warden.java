package game;


import Framework.Cryptography.*;

import java.math.BigInteger;

public abstract class Warden {
    public final SARC4 inputCrypto;
    public final SARC4 outputCrypto;
    public Worldsession session;

    public byte[] inputKey = new byte[16];

    public byte[] outputKey = new byte[16];

    public byte[] seed = new byte[16];

    public int checkTimer; // Timer for sending check requests

    public int clientResponseTimer; // Timer for client response delay
    public boolean dataSent;
    public ClientWardenmodule module;
    public boolean initialized;

    protected Warden() {
        inputCrypto = new SARC4();
        outputCrypto = new SARC4();
        checkTimer = 10 * time.InMilliseconds;
    }

    public final void makeModuleForClient() {
        Log.outDebug(LogFilter.Warden, "Make module for client");
        tangible.OutObject<ClientWardenModule> tempOut_Module = new tangible.OutObject<ClientWardenModule>();
        initializeModuleForClient(tempOut_Module);
        module = tempOut_Module.outArgValue;

        // md5 hash
        var ctx = MD5.create();
        ctx.initialize();
        ctx.TransformBlock(module.compressedData, 0, module.compressedData.length, module.compressedData, 0);
        ctx.TransformBlock(module.id, 0, module.id.length, module.id, 0);
    }

    public final void sendModuleToClient() {
        Log.outDebug(LogFilter.Warden, "Send module to client");

        // Create packet structure
        WardenModuleTransfer packet = new WardenModuleTransfer();

        var sizeLeft = module.compressedSize;
        var pos = 0;
        int burstSize;

        while (sizeLeft > 0) {
            burstSize = sizeLeft < 500 ? sizeLeft : 500;
            packet.command = WardenOpcodes.SmsgModuleCache;
            packet.dataSize = (short) burstSize;
            Buffer.BlockCopy(module.compressedData, pos, packet.data, 0, (int) burstSize);
            sizeLeft -= burstSize;
            pos += (int) burstSize;

            Warden3DataServer pkt1 = new Warden3DataServer();
            pkt1.data = encryptData(packet);
            session.sendPacket(pkt1);
        }
    }

    public final void requestModule() {
        Log.outDebug(LogFilter.Warden, "Request module");

        // Create packet structure
        WardenModuleUse request = new WardenModuleUse();
        request.command = WardenOpcodes.SmsgModuleUse;

        request.moduleId = module.id;
        request.moduleKey = module.key;
        request.size = module.compressedSize;

        Warden3DataServer packet = new Warden3DataServer();
        packet.data = encryptData(request);
        session.sendPacket(packet);
    }


    public final void update(int diff) {
        if (!initialized) {
            return;
        }

        if (dataSent) {
            var maxClientResponseDelay = WorldConfig.getUIntValue(WorldCfg.WardenClientResponseDelay);

            if (maxClientResponseDelay > 0) {
                // Kick player if client response delays more than set in config
                if (clientResponseTimer > maxClientResponseDelay * time.InMilliseconds) {
                    Log.outWarn(LogFilter.Warden, "{0} (latency: {1}, IP: {2}) exceeded Warden module response delay for more than {3} - disconnecting client", session.getPlayerInfo(), session.getLatency(), session.getRemoteAddress(), time.secsToTimeString(maxClientResponseDelay, TimeFormat.ShortText, false));

                    session.kickPlayer("Warden::Update Warden module response delay exceeded");
                } else {
                    clientResponseTimer += diff;
                }
            }
        } else {
            if (diff >= checkTimer) {
                requestChecks();
            } else {
                CheckTimer -= diff;
            }
        }
    }


    public final void decryptData(byte[] buffer) {
        inputCrypto.ProcessBuffer(buffer, buffer.length);
    }


    public final ByteBuffer encryptData(byte[] buffer) {
        outputCrypto.ProcessBuffer(buffer, buffer.length);

        return new byteBuffer(buffer);
    }


    public final boolean isValidCheckSum(int checksum, byte[] data, short length) {
        var newChecksum = buildChecksum(data, length);

        if (checksum != newChecksum) {
            Log.outDebug(LogFilter.Warden, "CHECKSUM IS NOT VALID");

            return false;
        } else {
            Log.outDebug(LogFilter.Warden, "CHECKSUM IS VALID");

            return true;
        }
    }


    public final int buildChecksum(byte[] data, int length) {
        var sha = SHA1.create();

        var hash = sha.ComputeHash(data, 0, (int) length);
        int checkSum = 0;

        for (byte i = 0; i < 5; ++i) {
            checkSum ^= BitConverter.ToUInt32(hash, i * 4);
        }

        return checkSum;
    }


    public final String applyPenalty() {
        return applyPenalty(null);
    }

    public final String applyPenalty(WardenCheck check) {
        WardenActions action;

        if (check != null) {
            action = check.action;
        } else {
            action = WardenActions.forValue(WorldConfig.getIntValue(WorldCfg.WardenClientFailAction));
        }

        switch (action) {
            case Kick:
                session.kickPlayer("Warden::Penalty");

                break;
            case Ban: {
                String accountName;
                tangible.OutObject<String> tempOut_accountName = new tangible.OutObject<String>();
                global.getAccountMgr().getName(session.getAccountId(), tempOut_accountName);
                accountName = tempOut_accountName.outArgValue;
                var banReason = "Warden Anticheat Violation";

                // Check can be NULL, for example if the client sent a wrong signature in the warden packet (CHECKSUM FAIL)
                if (check != null) {
                    banReason += ": " + check.comment + " (CheckId: " + check.checkId + ")";
                }

                global.getWorldMgr().banAccount(BanMode.Account, accountName, WorldConfig.getUIntValue(WorldCfg.WardenClientBanDuration), banReason, "Server");

                break;
            }
            case Log:
            default:
                return "None";
        }

        return action.toString();
    }

    public final void handleData(ByteBuffer buff) {
        var data = buff.getData();
        decryptData(data);
        var opcode = data[0];
        Log.outDebug(LogFilter.Warden, String.format("Got packet, opcode 0x%1$X, size %2$s", opcode, data.Length - 1));

        switch (WardenOpcodes.forValue(opcode)) {
            case SmsgModuleUse:
            case CmsgModuleMissing:
                sendModuleToClient();

                break;
            case SmsgModuleCache:
            case CmsgModuleOk:
                requestHash();

                break;
            case SmsgCheatChecksRequest:
            case CmsgCheatChecksResult:
                handleCheckResult(buff);

                break;
            case SmsgModuleInitialize:
            case CmsgMemChecksResult:
                Log.outDebug(LogFilter.Warden, "NYI WARDEN_CMSG_MEM_CHECKS_RESULT received!");

                break;
            case SmsgMemChecksRequest:
            case CmsgHashResult:
                handleHashResult(buff);
                initializeModule();

                break;
            case SmsgHashRequest:
            case CmsgModuleFailed:
                Log.outDebug(LogFilter.Warden, "NYI WARDEN_CMSG_MODULE_FAILED received!");

                break;
            default:
                Log.outWarn(LogFilter.Warden, String.format("Got unknown warden opcode 0x%1$X of size %2$s.", opcode, data.Length - 1));

                break;
        }
    }

    public abstract void init(WorldSession session, BigInteger k);

    public abstract void initializeModule();

    public abstract void requestHash();

    public abstract void handleHashResult(ByteBuffer buff);

    public abstract void handleCheckResult(ByteBuffer buff);

    public abstract void initializeModuleForClient(tangible.OutObject<ClientWardenModule> module_Keyword);

    public abstract void requestChecks();

    private boolean processLuaCheckResponse(String msg) {
        var WARDEN_TOKEN = "_TW\t";

        if (!msg.startsWith(WARDEN_TOKEN)) {
            return false;
        }

        short id = 0;
        SHORT.parseShort(tangible.StringHelper.substring(msg, WARDEN_TOKEN.length() - 1, 10));

        if (id < global.getWardenCheckMgr().getMaxValidCheckId()) {
            var check = global.getWardenCheckMgr().getCheckData(id);

            if (check.type == WardenCheckType.LuaEval) {
                var penalty1 = applyPenalty(check);
                Log.outWarn(LogFilter.Warden, String.format("%1$s failed Warden check %2$s (%3$s). Action: %4$s", session.getPlayerInfo(), id, check.type, penalty1));

                return true;
            }
        }

        var penalty = applyPenalty(null);
        Log.outWarn(LogFilter.Warden, String.format("%1$s sent bogus Lua check response for Warden. Action: %2$s", session.getPlayerInfo(), penalty));

        return true;
    }
}
