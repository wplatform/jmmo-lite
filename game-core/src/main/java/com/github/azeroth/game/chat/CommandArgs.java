package com.github.azeroth.game.chat;


class CommandArgs {

    public static ChatCommandResult consumeFromOffset(dynamic[] tuple, int offset, ParameterInfo[] parameterInfos, CommandHandler handler, String args) {
        if (offset < tuple.length) {
            return tryConsumeTo(tuple, offset, parameterInfos, handler, args);
        } else if (!args.isEmpty()) // the entire string must be consumed
        {
            return null;
        } else {
            return new ChatCommandResult(args);
        }
    }


    public static ChatCommandResult tryConsume(tangible.OutObject<dynamic> val, Class type, CommandHandler handler, String args) {
        val.outArgValue = null;

        var hyperlinkResult = Hyperlink.tryParse(val, type, handler, args);

        if (hyperlinkResult.isSuccessful()) {
            return hyperlinkResult;
        }

        if (type.isEnum()) {
            type = type.GetEnumUnderlyingType();
        }


        var(token, tail) = args.Tokenize();

        switch (type.GetTypeCode(type)) {
            case Byte: {
                if (token.isEmpty()) {
                    return null;
                }

                byte tempValue;
                tangible.OutObject<Byte> tempOut_tempValue = new tangible.OutObject<Byte>();
                if (tangible.TryParseHelper.tryParseByte(token, tempOut_tempValue)) {
                    tempValue = tempOut_tempValue.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case Int16: {
                if (token.isEmpty()) {
                    return null;
                }

                short tempValue;
                tangible.OutObject<SHORT> tempOut_tempValue2 = new tangible.OutObject<SHORT>();
                if (tangible.TryParseHelper.tryParseShort(token, tempOut_tempValue2)) {
                    tempValue = tempOut_tempValue2.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue2.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case Int32: {
                if (token.isEmpty()) {
                    return null;
                }

                int tempValue;
                tangible.OutObject<Integer> tempOut_tempValue3 = new tangible.OutObject<Integer>();
                if (tangible.TryParseHelper.tryParseInt(token, tempOut_tempValue3)) {
                    tempValue = tempOut_tempValue3.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue3.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case Int64: {
                if (token.isEmpty()) {
                    return null;
                }

                long tempValue;
                tangible.OutObject<Long> tempOut_tempValue4 = new tangible.OutObject<Long>();
                if (tangible.TryParseHelper.tryParseLong(token, tempOut_tempValue4)) {
                    tempValue = tempOut_tempValue4.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue4.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case Byte: {
                if (token.isEmpty()) {
                    return null;
                }

                byte tempValue;
                tangible.OutObject<Byte> tempOut_tempValue5 = new tangible.OutObject<Byte>();
                if (tangible.TryParseHelper.tryParseByte(token, tempOut_tempValue5)) {
                    tempValue = tempOut_tempValue5.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue5.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case UInt16: {
                if (token.isEmpty()) {
                    return null;
                }

                short tempValue;
                tangible.OutObject<SHORT> tempOut_tempValue6 = new tangible.OutObject<SHORT>();
                if (tangible.TryParseHelper.tryParseShort(token, tempOut_tempValue6)) {
                    tempValue = tempOut_tempValue6.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue6.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case UInt32: {
                if (token.isEmpty()) {
                    return null;
                }

                int tempValue;
                tangible.OutObject<Integer> tempOut_tempValue7 = new tangible.OutObject<Integer>();
                if (tangible.TryParseHelper.tryParseInt(token, tempOut_tempValue7)) {
                    tempValue = tempOut_tempValue7.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue7.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case UInt64: {
                if (token.isEmpty()) {
                    return null;
                }

                long tempValue;
                tangible.OutObject<Long> tempOut_tempValue8 = new tangible.OutObject<Long>();
                if (tangible.TryParseHelper.tryParseLong(token, tempOut_tempValue8)) {
                    tempValue = tempOut_tempValue8.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue8.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case Single: {
                if (token.isEmpty()) {
                    return null;
                }

                float tempValue;
                tangible.OutObject<Float> tempOut_tempValue9 = new tangible.OutObject<Float>();
                if (tangible.TryParseHelper.tryParseFloat(token, tempOut_tempValue9)) {
                    tempValue = tempOut_tempValue9.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue9.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                if (!Float.IsFinite(val.outArgValue)) {
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                }

                return new ChatCommandResult(tail);
            }
            case String: {
                if (token.isEmpty()) {
                    return null;
                }

                val.outArgValue = token;

                return new ChatCommandResult(tail);
            }
            case Boolean: {
                if (token.isEmpty()) {
                    return null;
                }

                boolean tempValue;
                tangible.OutObject<Boolean> tempOut_tempValue10 = new tangible.OutObject<Boolean>();
                if (Boolean.tryParse(token, tempOut_tempValue10)) {
                    tempValue = tempOut_tempValue10.outArgValue;
                    val.outArgValue = tempValue;
                } else {
                    tempValue = tempOut_tempValue10.outArgValue;
                    if ((token.equals("1")) || token.Equals("y", StringComparison.OrdinalIgnoreCase) || token.Equals("on", StringComparison.OrdinalIgnoreCase) || token.Equals("yes", StringComparison.OrdinalIgnoreCase)) {
                        val.outArgValue = true;
                    } else if ((token.equals("0")) || token.Equals("n", StringComparison.OrdinalIgnoreCase) || token.Equals("off", StringComparison.OrdinalIgnoreCase) || token.Equals("no", StringComparison.OrdinalIgnoreCase)) {
                        val.outArgValue = false;
                    } else {
                        return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserStringValueInvalid, token, type.GetTypeCode(type)));
                    }
                }

                return new ChatCommandResult(tail);
            }
            case Object: {
                switch (type.getSimpleName()) {
                    case "Tail":
                        val.outArgValue = new tail();

                        return val.outArgValue.tryConsume(handler, args);
                    case "QuotedString":
                        val.outArgValue = new QuotedString();

                        return val.outArgValue.tryConsume(handler, args);
                    case "PlayerIdentifier":
                        val.outArgValue = new PlayerIdentifier();

                        return val.outArgValue.tryConsume(handler, args);
                    case "AccountIdentifier":
                        val.outArgValue = new AccountIdentifier();

                        return val.outArgValue.tryConsume(handler, args);
                    case "AchievementRecord": {
                        dynamic tempVal;
                        tangible.OutObject<dynamic> tempOut_tempVal = new tangible.OutObject<dynamic>();

                        var result = tryConsume(tempOut_tempVal, Integer.class, handler, args);
                        tempVal = tempOut_tempVal.outArgValue;

                        if (!result.isSuccessful() || (val.outArgValue = CliDB.AchievementStorage.get((int) tempVal)) != null) {
                            return result;
                        }

                        return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserAchievementNoExist, tempVal));
                    }
                    case "CurrencyTypesRecord": {
                        dynamic tempVal;
                        tangible.OutObject<dynamic> tempOut_tempVal2 = new tangible.OutObject<dynamic>();

                        var result = tryConsume(tempOut_tempVal2, Integer.class, handler, args);
                        tempVal = tempOut_tempVal2.outArgValue;

                        if (!result.isSuccessful() || (val.outArgValue = CliDB.CurrencyTypesStorage.get((int) tempVal)) != null) {
                            return result;
                        }

                        return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserCurrencyNoExist, tempVal));
                    }
                    case "GameTele": {
                        dynamic tempVal;
                        tangible.OutObject<dynamic> tempOut_tempVal3 = new tangible.OutObject<dynamic>();

                        var result = tryConsume(tempOut_tempVal3, Integer.class, handler, args);
                        tempVal = tempOut_tempVal3.outArgValue;

                        if (!result.isSuccessful()) {
                            tangible.OutObject<dynamic> tempOut_tempVal4 = new tangible.OutObject<dynamic>();
                            result = tryConsume(tempOut_tempVal4, String.class, handler, args);
                            tempVal = tempOut_tempVal4.outArgValue;
                        }

                        if (!result.isSuccessful() || (val.outArgValue = global.getObjectMgr().getGameTele(tempVal)) != null) {
                            return result;
                        }

                        if (tempVal instanceof Integer) {
                            return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserGameTeleIdNoExist, tempVal));
                        } else {
                            return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserGameTeleNoExist, tempVal));
                        }
                    }
                    case "ItemTemplate": {
                        dynamic tempVal;
                        tangible.OutObject<dynamic> tempOut_tempVal5 = new tangible.OutObject<dynamic>();

                        var result = tryConsume(tempOut_tempVal5, Integer.class, handler, args);
                        tempVal = tempOut_tempVal5.outArgValue;

                        if (!result.isSuccessful() || (val.outArgValue = global.getObjectMgr().getItemTemplate(tempVal)) != null) {
                            return result;
                        }

                        return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserItemNoExist, tempVal));
                    }
                    case "SpellInfo": {
                        dynamic tempVal;
                        tangible.OutObject<dynamic> tempOut_tempVal6 = new tangible.OutObject<dynamic>();

                        var result = tryConsume(tempOut_tempVal6, Integer.class, handler, args);
                        tempVal = tempOut_tempVal6.outArgValue;

                        if (!result.isSuccessful() || (val.outArgValue = global.getSpellMgr().getSpellInfo(tempVal, Difficulty.NONE)) != null) {
                            return result;
                        }

                        return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserSpellNoExist, tempVal));
                    }
                    case "Quest": {
                        dynamic tempVal;
                        tangible.OutObject<dynamic> tempOut_tempVal7 = new tangible.OutObject<dynamic>();

                        var result = tryConsume(tempOut_tempVal7, Integer.class, handler, args);
                        tempVal = tempOut_tempVal7.outArgValue;

                        if (!result.isSuccessful() || (val.outArgValue = global.getObjectMgr().getQuestTemplate(tempVal)) != null) {
                            return result;
                        }

                        return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserQuestNoExist, tempVal));
                    }
                }

                break;
            }
        }

        return null;
    }


    public static ChatCommandResult tryConsumeVariant(tangible.OutObject<dynamic> val, Class[] types, CommandHandler handler, String args) {
        var result = tryAtIndex(val, types, 0, handler, args);

        if (result.getHasErrorMessage() && (result.getErrorMessage().indexOf('\n') != -1)) {
            return ChatCommandResult.fromErrorMessage(String.format("%1$s %2$s", handler.getSysMessage(SysMessage.CmdparserEither), result.getErrorMessage()));
        }

        return result;
    }


    private static ChatCommandResult tryConsumeTo(dynamic[] tuple, int offset, ParameterInfo[] parameterInfos, CommandHandler handler, String args) {
        var optionalArgAttribute = parameterInfos[offset].<OptionalArgAttribute>GetCustomAttribute(true);

        if (optionalArgAttribute != null || parameterInfos[offset].ParameterType.IsGenericType && parameterInfos[offset].ParameterType.GetGenericTypeDefinition() == .
        class)
        {
            // try with the argument
            var tempVar = Nullable.GetUnderlyingType(parameterInfos[offset].ParameterType);
            var myArg = tempVar != null ? tempVar : parameterInfos[offset].ParameterType;

            tangible.OutObject<dynamic> tempOut_Object = new tangible.OutObject<dynamic>();
            var result1 = tryConsume(tempOut_Object, myArg, handler, args);
            tuple[offset] = tempOut_Object.outArgValue;

            if (result1.isSuccessful()) {
                if ((result1 = consumeFromOffset(tuple, offset + 1, parameterInfos, handler, result1)).IsSuccessful) {
                    return result1;
                }
            }

            // try again omitting the argument
            tuple[offset] = null;
            var result2 = consumeFromOffset(tuple, offset + 1, parameterInfos, handler, args);

            if (result2.isSuccessful()) {
                return result2;
            }

            if (result1.getHasErrorMessage() && result2.getHasErrorMessage()) {
                return ChatCommandResult.fromErrorMessage(String.format("%1$s \"%2$s\"\n%3$s \"%4$s\"", handler.getSysMessage(SysMessage.CmdparserEither), result2.getErrorMessage(), handler.getSysMessage(SysMessage.CmdparserOr), result1.getErrorMessage()));
            } else if (result1.getHasErrorMessage()) {
                return result1;
            } else {
                return result2;
            }
        }
		else
        {
            ChatCommandResult next = new ChatCommandResult();

            var variantArgAttribute = parameterInfos[offset].<VariantArgAttribute>GetCustomAttribute(true);

            if (variantArgAttribute != null) {
                tangible.OutObject<dynamic> tempOut_Object2 = new tangible.OutObject<dynamic>();
                next = tryConsumeVariant(tempOut_Object2, variantArgAttribute.types, handler, args);
                tuple[offset] = tempOut_Object2.outArgValue;
            } else {
                tangible.OutObject<dynamic> tempOut_Object3 = new tangible.OutObject<dynamic>();
                next = tryConsume(tempOut_Object3, parameterInfos[offset].ParameterType, handler, args);
                tuple[offset] = tempOut_Object3.outArgValue;
            }

            if (next.isSuccessful()) {
                return consumeFromOffset(tuple, offset + 1, parameterInfos, handler, next);
            } else {
                return next;
            }
        }
    }


    private static ChatCommandResult tryAtIndex(tangible.OutObject<dynamic> val, Class[] types, int index, CommandHandler handler, String args) {
        val.outArgValue = null;

        if (index < types.length) {
            var thisResult = tryConsume(val, types[index], handler, args);

            if (thisResult.isSuccessful()) {
                return thisResult;
            } else {
                var nestedResult = tryAtIndex(val, types, index + 1, handler, args);

                if (nestedResult.isSuccessful() || !thisResult.getHasErrorMessage()) {
                    return nestedResult;
                }

                if (!nestedResult.getHasErrorMessage()) {
                    return thisResult;
                }

                if (nestedResult.getErrorMessage().startsWith("\"")) {
                    return ChatCommandResult.fromErrorMessage(String.format("\"%1$s\"\n%2$s %3$s", thisResult.getErrorMessage(), handler.getSysMessage(SysMessage.CmdparserOr), nestedResult.getErrorMessage()));
                } else {
                    return ChatCommandResult.fromErrorMessage(String.format("\"%1$s\"\n%2$s \"%3$s\"", thisResult.getErrorMessage(), handler.getSysMessage(SysMessage.CmdparserOr), nestedResult.getErrorMessage()));
                }
            }
        } else {
            return null;
        }
    }
}
