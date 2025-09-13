package game;


import Framework.Cryptography.*;

import java.math.BigInteger;
import java.util.ArrayList;

class WardenWin extends Warden {
    // GUILD is the shortest string that has no client validation (RAID only sends if in a raid group)
    private static final String LUAEVALPREFIX = "local S,T,R=SendAddonMessage,function()";
    private static final String LUAEVALMIDFIX = " end R=S and T()if R then S('_TW',";
    private static final String LUAEVALPOSTFIX = ",'GUILD')end";
    private final CategoryCheck[] checks = new CategoryCheck[WardenCheckCategory.max.getValue()];

    private int serverTicks;
    private ArrayList<SHORT> currentChecks = new ArrayList<>();

    public WardenWin() {
        for (var category : Enum.<WardenCheckCategory>GetValues()) {
            _checks[(int) category] = new CategoryCheck(global.getWardenCheckMgr().getAvailableChecks(category).shuffle().ToList());
        }
    }

    private static byte getCheckPacketBaseSize(WardenCheckType type) {
        return switch (type) {
            case Driver -> 1;
            case LuaEval ->
                    (byte) (1 + LUAEVALPREFIX.length() - 1 + LUAEVALMIDFIX.length() - 1 + 4 + LUAEVALPOSTFIX.length() - 1);
            case Mpq -> 1;
            case PageA -> 4 + 1;
            case PageB -> 4 + 1;
            case Module -> 4 + 20;
            case Mem -> 1 + 4 + 1;
            default -> 0;
        };
    }

    private static short getCheckPacketSize(WardenCheck check) {
        var size = 1 + getCheckPacketBaseSize(check.type); // 1 byte check type

        if (!check.str.isEmpty()) {
            size += (check.str.length() + 1); // 1 byte string length
        }

        if (!check.data.isEmpty()) {
            size += check.data.length;
        }

        return (short) size;
    }

    @Override
    public void init(WorldSession session, BigInteger k) {
        session = session;
        // Generate Warden Key
        SessionKeyGenerator WK = new SessionKeyGenerator(k.toByteArray(), 0);
        WK.generate(inputKey, 16);
        WK.generate(outputKey, 16);

        seed = WardenModuleWin.seed;

        inputCrypto.PrepareKey(inputKey);
        outputCrypto.PrepareKey(outputKey);
        Log.outDebug(LogFilter.Warden, "Server side warden for client {0} initializing...", session.getAccountId());
        Log.outDebug(LogFilter.Warden, "C->S Key: {0}", inputKey.ToHexString());
        Log.outDebug(LogFilter.Warden, "S->C Key: {0}", outputKey.ToHexString());
        Log.outDebug(LogFilter.Warden, "  Seed: {0}", seed.ToHexString());
        Log.outDebug(LogFilter.Warden, "Loading module...");

        makeModuleForClient();

        Log.outDebug(LogFilter.Warden, "Module Key: {0}", module.key.ToHexString());
        Log.outDebug(LogFilter.Warden, "Module ID: {0}", module.id.ToHexString());
        requestModule();
    }

    @Override
    public void initializeModuleForClient(tangible.OutObject<ClientWardenModule> module_Keyword) {
        // data assign
        module_Keyword.outArgValue = new ClientWardenModule();
        module_Keyword.outArgValue.compressedData = WardenModuleWin.module;
        module_Keyword.outArgValue.compressedSize = (int) WardenModuleWin.module.length;
        module_Keyword.outArgValue.key = WardenModuleWin.moduleKey;
    }

    @Override
    public void initializeModule() {
        Log.outDebug(LogFilter.Warden, "Initialize module");

        // Create packet structure
        WardenInitModuleRequest request = new WardenInitModuleRequest();
        request.command1 = WardenOpcodes.SmsgModuleInitialize;
        request.size1 = 20;
        request.unk1 = 1;
        request.unk2 = 0;
        request.type = 1;
        request.string_library1 = 0;
        request.Function1[0] = 0x00024F80; // 0x00400000 + 0x00024F80 SFileOpenFile
        request.Function1[1] = 0x000218C0; // 0x00400000 + 0x000218C0 SFileGetFileSize
        request.Function1[2] = 0x00022530; // 0x00400000 + 0x00022530 SFileReadFile
        request.Function1[3] = 0x00022910; // 0x00400000 + 0x00022910 SFileCloseFile

        request.checkSumm1 = buildChecksum(new byte[]{Request.Unk1}, 20);

        request.command2 = WardenOpcodes.SmsgModuleInitialize;
        request.size2 = 8;
        request.unk3 = 4;
        request.unk4 = 0;
        request.string_library2 = 0;
        request.function2 = 0x00419D40; // 0x00400000 + 0x00419D40 FrameScript::GetText
        request.function2_set = 1;

        request.checkSumm2 = buildChecksum(new byte[]{Request.Unk2}, 8);

        request.command3 = WardenOpcodes.SmsgModuleInitialize;
        request.size3 = 8;
        request.unk5 = 1;
        request.unk6 = 1;
        request.string_library3 = 0;
        request.function3 = 0x0046AE20; // 0x00400000 + 0x0046AE20 PerformanceCounter
        request.function3_set = 1;

        request.checkSumm3 = buildChecksum(new byte[]{Request.Unk5}, 8);

        Warden3DataServer packet = new Warden3DataServer();
        packet.data = encryptData(request);
        session.sendPacket(packet);
    }

    @Override
    public void requestHash() {
        Log.outDebug(LogFilter.Warden, "Request hash");

        // Create packet structure
        WardenHashRequest request = new WardenHashRequest();
        request.command = WardenOpcodes.SmsgHashRequest;
        request.seed = seed;

        Warden3DataServer packet = new Warden3DataServer();
        packet.data = encryptData(request);
        session.sendPacket(packet);
    }

    @Override
    public void handleHashResult(ByteBuffer buff) {
        // Verify key
        if (buff.readBytes(20) != WardenModuleWin.CLIENTKEYSEEDHASH) {
            var penalty = applyPenalty();
            Log.outWarn(LogFilter.Warden, "{0} failed hash reply. Action: {0}", session.getPlayerInfo(), penalty);

            return;
        }

        Log.outDebug(LogFilter.Warden, "Request hash reply: succeed");

        // Change keys here
        inputKey = WardenModuleWin.CLIENTKEYSEED;
        outputKey = WardenModuleWin.SERVERKEYSEED;

        inputCrypto.PrepareKey(inputKey);
        outputCrypto.PrepareKey(outputKey);

        initialized = true;
    }

    @Override
    public void requestChecks() {
        Log.outDebug(LogFilter.Warden, String.format("Request data from %1$s (account %2$s) - loaded: %3$s", session.getPlayerName(), session.getAccountId(), session.getPlayer() && !session.getPlayerLoading()));

        // If all checks for a category are done, fill its todo list again
        for (var category : Enum.<WardenCheckCategory>GetValues()) {
            var checks = _checks[(int) category];

            if (checks.isAtEnd() && !checks.isEmpty()) {
                Log.outDebug(LogFilter.Warden, String.format("Finished all %1$s checks, re-shuffling", category));
                checks.shuffle();
            }
        }

        serverTicks = gameTime.GetGameTimeMS();
        currentChecks.clear();

        // Build check request
        ByteBuffer buff = new byteBuffer();
        buff.writeInt8((byte) WardenOpcodes.SmsgCheatChecksRequest.getValue());

        for (var category : Enum.<WardenCheckCategory>GetValues()) {
            if (WardenCheckManager.isWardenCategoryInWorldOnly(category) && !session.getPlayer()) {
                continue;
            }

            var checks = _checks[(int) category];

            for (int i = 0, n = WorldConfig.getUIntValue(WardenCheckManager.getWardenCategoryCountConfig(category)); i < n; ++i) {
                if (checks.isAtEnd()) // all checks were already sent, list will be re-filled on next update() run
                {
                    break;
                }

                currentChecks.add(checks.currentIndex++);
            }
        }

        currentChecks = currentChecks.shuffle().ToList();

        short expectedSize = 4;

        tangible.ListHelper.removeAll(currentChecks, id ->
        {
            var thisSize = getCheckPacketSize(global.getWardenCheckMgr().getCheckData(id));

            if ((expectedSize + thisSize) > 450) // warden packets are truncated to 512 bytes clientside
            {
                return true;
            }

            expectedSize += thisSize;

            return false;
        });

        for (var id : currentChecks) {
            var check = global.getWardenCheckMgr().getCheckData(id);

            if (check.type == WardenCheckType.LuaEval) {
                buff.writeInt8((byte) (LUAEVALPREFIX.length() - 1 + check.str.length() + LUAEVALMIDFIX.length() - 1 + check.idStr.length + LUAEVALPOSTFIX.length() - 1));
                buff.writeString(LUAEVALPREFIX);
                buff.writeString(check.str);
                buff.writeString(LUAEVALMIDFIX);
                buff.writeString(check.idStr.toString());
                buff.writeString(LUAEVALPOSTFIX);
            } else if (!check.str.isEmpty()) {
                buff.writeInt8((byte) check.str.getBytes().length);
                buff.writeString(check.str);
            }
        }

        var xorByte = InputKey[0];

        // Add TIMING_CHECK
        buff.writeInt8((byte) 0x00);
        buff.writeInt8((byte) (WardenCheckType.Timing.getValue() ^ xorByte));

        byte index = 1;

        for (var checkId : currentChecks) {
            var check = global.getWardenCheckMgr().getCheckData(checkId);

            var type = check.type;
            buff.writeInt8((byte) (type.getValue() ^ xorByte));

            switch (type) {
                case Mem: {
                    buff.writeInt8((byte) 0x00);
                    buff.writeInt32(check.address);
                    buff.writeInt8(check.length);

                    break;
                }
                case PageA:
                case PageB: {
                    buff.writeBytes(check.data);
                    buff.writeInt32(check.address);
                    buff.writeInt8(check.length);

                    break;
                }
                case Mpq:
                case LuaEval: {
                    buff.writeInt8(index++);

                    break;
                }
                case Driver: {
                    buff.writeBytes(check.data);
                    buff.writeInt8(index++);

                    break;
                }
                case Module: {
                    var seed = RandomUtil.Rand32();
                    buff.writeInt32(seed);
                    HmacHash hmac = new HmacHash(BitConverter.GetBytes(seed));
                    hmac.finish(check.str);
                    buff.writeBytes(hmac.digest);

                    break;
                }
				/*case PROC_CHECK:
				{
					buff.append(wd.i.AsByteArray(0, false).get(), wd.i.GetNumBytes());
					buff << uint8(index++);
					buff << uint8(index++);
					buff << uint32(wd.address);
					buff << uint8(wd.length);
					break;
				}*/
                default:
                    break; // Should never happen
            }
        }

        buff.writeInt8(xorByte);

        var idstring = "";

        for (var id : currentChecks) {
            idstring += String.format("%1$s ", id);
        }

        if (buff.getSize() == expectedSize) {
            Log.outDebug(LogFilter.Warden, String.format("Finished building warden packet, size is %1$s bytes", buff.getSize()));
            Log.outDebug(LogFilter.Warden, String.format("Sent checks: %1$s", idstring));
        } else {
            Log.outWarn(LogFilter.Warden, String.format("Finished building warden packet, size is %1$s bytes, but expected %2$s bytes!", buff.getSize(), expectedSize));
            Log.outWarn(LogFilter.Warden, String.format("Sent checks: %1$s", idstring));
        }

        Warden3DataServer packet = new Warden3DataServer();
        packet.data = encryptData(buff.getData());
        session.sendPacket(packet);

        dataSent = true;
    }

    @Override
    public void handleCheckResult(ByteBuffer buff) {
        Log.outDebug(LogFilter.Warden, "Handle data");

        dataSent = false;
        clientResponseTimer = 0;

        var length = buff.readUInt16();
        var Checksum = buff.readUInt();

        if (!isValidCheckSum(Checksum, buff.getData())
        {
            var penalty = applyPenalty();
            Log.outWarn(LogFilter.Warden, "{0} failed checksum. Action: {1}", session.getPlayerInfo(), penalty);

            return;
        }

        {
            // TIMING_CHECK
            var result = buff.readUInt8();

            // @todo test it.
            if (result == 0x00) {
                var penalty = applyPenalty();
                Log.outWarn(LogFilter.Warden, "{0} failed timing check. Action: {1}", session.getPlayerInfo(), penalty);

                return;
            }

            var newClientTicks = buff.readUInt();

            var ticksNow = gameTime.GetGameTimeMS();
            var ourTicks = newClientTicks + (ticksNow - serverTicks);

            Log.outDebug(LogFilter.Warden, "ServerTicks {0}", ticksNow); // Now
            Log.outDebug(LogFilter.Warden, "RequestTicks {0}", serverTicks); // At request
            Log.outDebug(LogFilter.Warden, "Ticks {0}", newClientTicks); // At response
            Log.outDebug(LogFilter.Warden, "Ticks diff {0}", ourTicks - newClientTicks);
        }

        //BigInteger rs;
        //WardenCheck rd;
        // WardenCheckType type; // TODO unused.
        short checkFailed = 0;

        for (var id : currentChecks) {
            var check = global.getWardenCheckMgr().getCheckData(id);

            switch (check.type) {
                case Mem: {
                    var result = buff.readUInt8();

                    if (result != 0) {
                        Log.outDebug(LogFilter.Warden, String.format("RESULT MEM_CHECK not 0x00, CheckId %1$s account Id %2$s", id, session.getAccountId()));
                        checkFailed = id;

                        continue;
                    }

                    var expected = global.getWardenCheckMgr().getCheckResult(id);

                    if (buff.readBytes(new integer(expected.length)).compare(expected)) {
                        Log.outDebug(LogFilter.Warden, String.format("RESULT MEM_CHECK fail CheckId %1$s account Id %2$s", id, session.getAccountId()));
                        checkFailed = id;

                        continue;
                    }

                    Log.outDebug(LogFilter.Warden, String.format("RESULT MEM_CHECK passed CheckId %1$s account Id %2$s", id, session.getAccountId()));

                    break;
                }
                case PageA:
                case PageB:
                case Driver:
                case Module: {
                    if (buff.readUInt8() != 0xE9) {
                        Log.outDebug(LogFilter.Warden, String.format("RESULT %1$s fail, CheckId %2$s account Id %3$s", check.type, id, session.getAccountId()));
                        checkFailed = id;

                        continue;
                    }

                    Log.outDebug(LogFilter.Warden, String.format("RESULT %1$s passed CheckId %2$s account Id %3$s", check.type, id, session.getAccountId()));

                    break;
                }
                case LuaEval: {
                    var result = buff.readUInt8();

                    if (result == 0) {
                        buff.Skip(buff.readUInt8()); // discard attached string
                    }

                    Log.outDebug(LogFilter.Warden, String.format("LUA_EVAL_CHECK CheckId %1$s account Id %2$s got in-warden dummy response (%3$s)", id, session.getAccountId(), result));

                    break;
                }
                case Mpq: {
                    var result = buff.readUInt8();

                    if (result != 0) {
                        Log.outDebug(LogFilter.Warden, String.format("RESULT MPQ_CHECK not 0x00 account id %1$s", session.getAccountId()), session.getAccountId());
                        checkFailed = id;

                        continue;
                    }

                    if (!buff.readBytes(20).compare(global.getWardenCheckMgr().getCheckResult(id))) // SHA1
                    {
                        Log.outDebug(LogFilter.Warden, String.format("RESULT MPQ_CHECK fail, CheckId %1$s account Id %2$s", id, session.getAccountId()));
                        checkFailed = id;

                        continue;
                    }

                    Log.outDebug(LogFilter.Warden, String.format("RESULT MPQ_CHECK passed, CheckId %1$s account Id %2$s", id, session.getAccountId()));

                    break;
                }
                default: // Should never happen
                    break;
            }
        }

        if (checkFailed > 0) {
            var check = global.getWardenCheckMgr().getCheckData(checkFailed);
            var penalty = applyPenalty(check);
            Log.outWarn(LogFilter.Warden, String.format("%1$s failed Warden check %2$s. Action: %3$s", session.getPlayerInfo(), checkFailed, penalty));
        }

        // Set hold off timer, minimum timer should at least be 1 second
        var holdOff = WorldConfig.getUIntValue(WorldCfg.WardenClientCheckHoldoff);
        checkTimer = (holdOff < 1 ? 1 : holdOff) * time.InMilliseconds;
    }
}
