package com.github.azeroth.game.chat;

final class QuotedString {
    private String str;

    public boolean isEmpty() {
        return str.isEmpty();
    }


//	public static implicit operator string(QuotedString quotedString)
//		{
//			return quotedString.str;
//		}

    public ChatCommandResult tryConsume(CommandHandler handler, String args) {
        str = "";

        if (args.isEmpty()) {
            return ChatCommandResult.fromErrorMessage("");
        }

        if ((args.charAt(0) != '"') && (args.charAt(0) != '\'')) {
            dynamic str;
            tangible.OutObject<dynamic> tempOut_str = new tangible.OutObject<dynamic>();

            var tempVar = CommandArgs.tryConsume(tempOut_str, String.class, handler, args);
            str = tempOut_str.outArgValue;
            return tempVar;
        }

        var QUOTE = args.charAt(0);

        for (var i = 1; i < args.length(); ++i) {
            if (args.charAt(i) == QUOTE) {

                var(remainingToken, tail) = args.Substring(i + 1).Tokenize();

                if (remainingToken.isEmpty()) // if this is not empty, then we did not consume the full token
                {
                    return new ChatCommandResult(tail);
                } else {
                    return ChatCommandResult.fromErrorMessage("");
                }
            }

            if (args.charAt(i) == '\\') {
                ++i;

                if (!(i < args.length())) {
                    break;
                }
            }

            str += args.charAt(i);
        }

        // if we reach this, we did not find a closing quote
        return ChatCommandResult.fromErrorMessage("");
    }

    public QuotedString clone() {
        QuotedString varCopy = new QuotedString();

        varCopy.str = this.str;

        return varCopy;
    }
}
