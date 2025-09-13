package com.github.azeroth.game.networking;


class AccountInfo {
    public battleNet battleNet = new battleNet();
    public game game = new game();

    public AccountInfo(SQLFields fields) {
        //         0             1           2          3                4            5           6          7            8      9     10          11
        // SELECT a.id, a.sessionkey, ba.last_ip, ba.locked, ba.lock_country, a.expansion, a.mutetime, ba.locale, a.recruiter, a.os, ba.id, aa.gmLevel,
        //                                                              12                                                            13    14
        // bab.unbandate > UNIX_TIMESTAMP() OR bab.unbandate = bab.bandate, ab.unbandate > UNIX_TIMESTAMP() OR ab.unbandate = ab.bandate, r.id
        // FROM account a LEFT JOIN battlenet_accounts ba ON a.battlenet_account = ba.id LEFT JOIN account_access aa ON a.id = aa.id AND aa.RealmID IN (-1, ?)
        // LEFT JOIN battlenet_account_bans bab ON ba.id = bab.id LEFT JOIN account_banned ab ON a.id = ab.id LEFT JOIN account r ON a.id = r.recruiter
        // WHERE a.username = ? ORDER BY aa.RealmID DESC LIMIT 1
        game.id = fields.<Integer>Read(0);
        game.sessionKey = fields.<byte[]>Read(1);
        battleNet.lastIP = fields.<String>Read(2);
        battleNet.isLockedToIP = fields.<Boolean>Read(3);
        battleNet.lockCountry = fields.<String>Read(4);
        game.expansion = fields.<Byte>Read(5);
        game.muteTime = fields.<Long>Read(6);
        battleNet.locale = locale.forValue(fields.<Byte>Read(7));
        game.recruiter = fields.<Integer>Read(8);
        game.OS = fields.<String>Read(9);
        battleNet.id = fields.<Integer>Read(10);
        game.security = AccountTypes.forValue(fields.<Byte>Read(11));
        battleNet.isBanned = fields.<Integer>Read(12) != 0;
        game.isBanned = fields.<Integer>Read(13) != 0;
        game.isRectuiter = fields.<Integer>Read(14) != 0;

        if (battleNet.locale.getValue() >= locale.Total.getValue()) {
            battleNet.locale = locale.enUS;
        }
    }

    public final boolean isBanned() {
        return battleNet.isBanned || game.isBanned;
    }

    public final static class BattleNet {
        public int id;
        public boolean isLockedToIP;
        public String lastIP;
        public String lockCountry;
        public locale locale = Framework.Constants.locale.values()[0];
        public boolean isBanned;

        public BattleNet clone() {
            BattleNet varCopy = new battleNet();

            varCopy.id = this.id;
            varCopy.isLockedToIP = this.isLockedToIP;
            varCopy.lastIP = this.lastIP;
            varCopy.lockCountry = this.lockCountry;
            varCopy.locale = this.locale;
            varCopy.isBanned = this.isBanned;

            return varCopy;
        }
    }

    public final static class Game {
        public int id;
        public byte[] sessionKey;
        public byte expansion;
        public long muteTime;
        public String OS;
        public int recruiter;
        public boolean isRectuiter;
        public AccountTypes security = AccountTypes.values()[0];
        public boolean isBanned;

        public Game clone() {
            Game varCopy = new game();

            varCopy.id = this.id;
            varCopy.sessionKey = this.sessionKey;
            varCopy.expansion = this.expansion;
            varCopy.muteTime = this.muteTime;
            varCopy.OS = this.OS;
            varCopy.recruiter = this.recruiter;
            varCopy.isRectuiter = this.isRectuiter;
            varCopy.security = this.security;
            varCopy.isBanned = this.isBanned;

            return varCopy;
        }
    }
}
