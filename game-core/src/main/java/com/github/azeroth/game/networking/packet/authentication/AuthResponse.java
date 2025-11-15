package com.github.azeroth.game.networking.packet.authentication;


import com.github.azeroth.defines.BattleNetRpcErrorCode;
import com.github.azeroth.game.domain.player.CharacterTemplate;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.domain.misc.RaceClassAvailability;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;

public class AuthResponse extends ServerPacket {
    public AuthSuccessInfo successInfo; // contains the packet data in case that it has account information (It is never set when WaitInfo is set), otherwise its contents are undefined.
    public AuthWaitInfo waitInfo; // contains the queue wait information in case the account is in the login queue.
    public BattleNetRpcErrorCode result; // the result of the authentication process, possible values are @ref BattlenetRpcErrorCode

    public AuthResponse() {
        super(ServerOpCode.SMSG_AUTH_RESPONSE);
    }

    public AuthResponse(BattleNetRpcErrorCode result) {
        super(ServerOpCode.SMSG_AUTH_RESPONSE);
        this.result = result;
    }

    @Override
    public void write() {
        this.writeInt32(result.code);
        this.writeBit(successInfo != null);
        this.writeBit(waitInfo != null);
        this.flushBits();

        if (successInfo != null) {
            this.writeInt32(successInfo.virtualRealmAddress);
            this.writeInt32(successInfo.virtualRealms.size());
            this.writeInt32(successInfo.timeRested);
            this.writeInt8(successInfo.activeExpansionLevel);
            this.writeInt8(successInfo.accountExpansionLevel);
            this.writeInt32(successInfo.timeSecondsUntilPCKick);
            this.writeInt32(successInfo.availableClasses.size());
            this.writeInt32(successInfo.templates.size());
            this.writeInt32(successInfo.currencyID);
            this.writeInt64(successInfo.time);

            for (var raceClassAvailability : successInfo.availableClasses) {
                this.writeInt8(raceClassAvailability.raceID.ordinal());
                this.writeInt32(raceClassAvailability.classes.size());

                for (var classAvailability : raceClassAvailability.classes) {
                    this.writeInt8(classAvailability.classID.ordinal());
                    this.writeInt8(classAvailability.activeExpansionLevel);
                    this.writeInt8(classAvailability.accountExpansionLevel);
                    this.writeInt8(classAvailability.minActiveExpansionLevel);
                }
            }

            this.writeBit(successInfo.isExpansionTrial);
            this.writeBit(successInfo.forceCharacterTemplate);
            this.writeBit(successInfo.numPlayersHorde != null);
            this.writeBit(successInfo.numPlayersAlliance != null);
            this.writeBit(successInfo.expansionTrialExpiration != null);
            this.flushBits();

            {
                this.writeInt32(successInfo.gameTimeInfo.billingPlan);
                this.writeInt32(successInfo.gameTimeInfo.timeRemain);
                this.writeInt32(successInfo.gameTimeInfo.unknown735);
                // 3x same bit is not a mistake - preserves legacy client behavior of BillingPlanFlags::SESSION_IGR
                this.writeBit(successInfo.gameTimeInfo.inGameRoom); // inGameRoom check in function checking which lua event to fire when remaining time is near end - BILLING_NAG_DIALOG vs IGR_BILLING_NAG_DIALOG
                this.writeBit(successInfo.gameTimeInfo.inGameRoom); // inGameRoom lua return from Script_GetBillingPlan
                this.writeBit(successInfo.gameTimeInfo.inGameRoom); // not used anywhere in the client
                this.flushBits();
            }

            if (successInfo.numPlayersHorde != null) {
                this.writeInt16(successInfo.numPlayersHorde.shortValue());
            }

            if (successInfo.numPlayersAlliance != null) {
                this.writeInt16(successInfo.numPlayersAlliance.shortValue());
            }

            if (successInfo.expansionTrialExpiration != null) {
                this.writeInt64(successInfo.expansionTrialExpiration.longValue());
            }

            for (var virtualRealm : successInfo.virtualRealms) {
                virtualRealm.write(this);
            }

            for (var template : successInfo.templates) {
                this.writeInt32(template.templateSetId);
                this.writeInt32(template.classes.size());

                for (var templateClass : template.classes) {
                    this.writeInt8(templateClass.classID);
                    this.writeInt8(templateClass.factionGroup);
                }

                this.writeBits(template.name.getBytes().length, 7);
                this.writeBits(template.description.getBytes().length, 10);
                this.flushBits();

                this.writeString(template.name);
                this.writeString(template.description);
            }
        }

        if (waitInfo != null) {
            waitInfo.write(this);
        }
    }

    public static class AuthSuccessInfo {
        public byte activeExpansionLevel; // the current server expansion, the possible values are in @ref Expansions
        public byte accountExpansionLevel; // the current expansion of this account, the possible values are in @ref Expansions
        public int timeRested; // affects the return value of the getBillingTimeRested() client API call, it is the number of seconds you have left until the experience points and loot you receive from creatures and quests is reduced. It is only used in the Asia region in retail, it's not implemented in TC and will probably never be.

        public int virtualRealmAddress; // a special identifier made from the index, BattleGroup and Region. @todo implement
        public int timeSecondsUntilPCKick; // @todo research
        public int currencyID; // this is probably used for the ingame shop. @todo implement
        public long time;

        public GameTime gameTimeInfo;

        public ArrayList<VirtualRealmInfo> virtualRealms = new ArrayList<>(); // list of realms connected to this one (inclusive) @todo implement
        public ArrayList<CharacterTemplate> templates = new ArrayList<>(); // list of pre-made character templates. @todo implement

        public ArrayList<RaceClassAvailability> availableClasses; // the minimum AccountExpansion required to select the classes

        public boolean isExpansionTrial;
        public boolean forceCharacterTemplate; // forces the client to always use a character template when creating a new character. @see templates. @todo implement
        public Short numPlayersHorde = null; // number of horde players in this realm. @todo implement
        public Short numPlayersAlliance = null; // number of alliance players in this realm. @todo implement
        public Long expansionTrialExpiration = null; // expansion trial expiration unix timestamp

        public final static class GameTime {
            public int billingPlan;
            public int timeRemain;
            public int unknown735;
            public boolean inGameRoom;
        }
    }
}
