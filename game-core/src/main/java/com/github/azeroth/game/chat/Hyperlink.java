package com.github.azeroth.game.chat;


class Hyperlink {

    public static ChatCommandResult tryParse(tangible.OutObject<dynamic> value, Class type, CommandHandler handler, String arg) {
        value.outArgValue = null;

        var info = parseHyperlink(arg);

        // invalid hyperlinks cannot be consumed
        if (info == null) {
            return null;
        }

        var errorResult = ChatCommandResult.fromErrorMessage(handler.getSysMessage(SysMessage.CmdparserLinkdataInvalid));

        // store value
        switch (type.GetTypeCode(type)) {
            case UInt32: {
                int tempValue;
                tangible.OutObject<Integer> tempOut_tempValue = new tangible.OutObject<Integer>();
                if (!tangible.TryParseHelper.tryParseInt(info.data, tempOut_tempValue)) {
                    tempValue = tempOut_tempValue.outArgValue;
                    return errorResult;
                } else {
                    tempValue = tempOut_tempValue.outArgValue;
                }

                value.outArgValue = tempValue;

                break;
            }
            case UInt64: {
                long tempValue;
                tangible.OutObject<Long> tempOut_tempValue2 = new tangible.OutObject<Long>();
                if (!tangible.TryParseHelper.tryParseLong(info.data, tempOut_tempValue2)) {
                    tempValue = tempOut_tempValue2.outArgValue;
                    return errorResult;
                } else {
                    tempValue = tempOut_tempValue2.outArgValue;
                }

                value.outArgValue = tempValue;

                break;
            }
            case String: {
                value.outArgValue = info.data;

                break;
            }
            default:
                return errorResult;
        }

        // finally, skip any potential delimiters

        var(token, next) = info.tail.Tokenize();

        if (token.isEmpty()) // empty token = first character is delimiter, skip past it
        {
            return new ChatCommandResult(next);
        } else {
            return new ChatCommandResult(info.tail);
        }
    }

    public static boolean checkAllLinks(String str) {
        {
            // Step 1: Disallow all control sequences except ||, |H, |h, |c and |r
            var pos = 0;

            while ((pos = str.indexOf('|', pos)) != -1) {
                var next = str.charAt(pos + 1);

                if (next == 'H' || next == 'h' || next == 'c' || next == 'r' || next == '|') {
                    pos += 2;
                } else {
                    return false;
                }
            }
        }

        // Step 2: Parse all link sequences
        // They look like this: |c<color>|H<linktag>:<linkdata>|h[<linktext>]|h|r
        // - <color> is 8 hex character AARRGGBB
        // - <linktag> is arbitrary length [a-z_]
        // - <linkdata> is arbitrary length, no | contained
        // - <linktext> is printable
        {
            var pos = 0;

            while ((pos = str.indexOf('|', pos)) != -1) {
                if (str.charAt(pos + 1) == '|') // this is an escaped pipe character (||)
                {
                    pos += 2;

                    continue;
                }

                var info = parseHyperlink(str.substring(pos));

                if (info == null) // todo fix me || !ValidateLinkInfo(info))
                {
                    return false;
                }

                // tag is fine, find the next one
                pos = str.length() - info.tail.length();
            }
        }

        // all tags are valid
        return true;
    }

    //|color|Henchant:recipe_spell_id|h[prof_name: recipe_name]|h|r
    public static HyperlinkInfo parseHyperlink(String currentString) {
        if (currentString.isEmpty()) {
            return null;
        }

        var pos = 0;

        //color tag
        if (currentString.charAt(pos++) != '|' || currentString.charAt(pos++) != 'c') {
            return null;
        }

        int color = 0;

        for (byte i = 0; i < 8; ++i) {
            var hex = toHex(currentString.charAt(pos++));

            if (hex != 0) {
                color = (int) ((int) (color << 4) | (hex & 0xf));
            } else {
                return null;
            }
        }

        // link data start tag
        if (currentString.charAt(pos++) != '|' || currentString.charAt(pos++) != 'H') {
            return null;
        }

        // link tag, find next : or |
        var tagStart = pos;
        var tagLength = 0;

        while (pos < currentString.length() && currentString.charAt(pos) != '|' && currentString.charAt(pos++) != ':') // we only advance pointer to one past if the last thing is : (not for |), this is intentional!
        {
            ++tagLength;
        }

        // ok, link data, skip to next |
        var dataStart = pos;
        var dataLength = 0;

        while (pos < currentString.length() && currentString.charAt(pos++) != '|') {
            ++dataLength;
        }

        // ok, next should be link data end tag...
        if (currentString.charAt(pos++) != 'h') {
            return null;
        }

        // then visible link text, starts with [
        if (currentString.charAt(pos++) != '[') {
            return null;
        }

        // skip until we hit the next ], abort on unexpected |
        var textStart = pos;
        var textLength = 0;

        while (pos < currentString.length()) {
            if (currentString.charAt(pos) == '|') {
                return null;
            }

            if (currentString.charAt(pos++) == ']') {
                break;
            }

            ++textLength;
        }

        // link end tag
        if (currentString.charAt(pos++) != '|' || currentString.charAt(pos++) != 'h' || currentString.charAt(pos++) != '|' || currentString.charAt(pos++) != 'r') {
            return null;
        }

        // ok, valid hyperlink, return info
        return new HyperlinkInfo(currentString.substring(pos), color, currentString.substring(tagStart, tagStart + tagLength), currentString.substring(dataStart, dataStart + dataLength), currentString.substring(textStart, textStart + textLength));
    }

    private static byte toHex(char c) {
        return (byte) ((c >= '0' && c <= '9') ? c - '0' + 0x10 : (c >= 'a' && c <= 'f') ? c - 'a' + 0x1a : 0x00);
    }
}
