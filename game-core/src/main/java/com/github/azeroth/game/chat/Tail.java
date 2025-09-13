package com.github.azeroth.game.chat;

final class Tail {
    private String str;

    public boolean isEmpty() {
        return str.isEmpty();
    }


//	public static implicit operator string(Tail tail)
//		{
//			return tail.str;
//		}

    public ChatCommandResult tryConsume(CommandHandler handler, String args) {
        str = args;

        return new ChatCommandResult(str);
    }

    public Tail clone() {
        Tail varCopy = new tail();

        varCopy.str = this.str;

        return varCopy;
    }
}
