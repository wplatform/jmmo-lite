package com.github.azeroth.game.networking.packet.system;


import java.util.ArrayList;


public class FeatureSystemStatus extends ServerPacket {
    public boolean voiceEnabled;
    public boolean browserEnabled;
    public boolean bpayStoreAvailable;
    public boolean bpayStoreEnabled;
    public sessionAlertConfig sessionAlert = null;
    public EuropaTicketConfig europaTicketSystemStatus = null;
    public int cfgRealmID;
    public byte complaintStatus;
    public int cfgRealmRecID;
    public int twitterPostThrottleLimit;
    public int twitterPostThrottleCooldown;
    public int tokenPollTimeSeconds;
    public long tokenBalanceAmount;
    public int bpayStoreProductDeliveryDelay;
    public int clubsPresenceUpdateTimer;
    public int hiddenUIClubsPresenceUpdateTimer; // Timer for updating club presence when communities ui frame is hidden
    public int kioskSessionMinutes;
    public int activeSeason; // Currently active Classic season
    public short maxPlayerNameQueriesPerPacket = 50;
    public short playerNameQueryTelemetryInterval = 600;
    public Duration playerNameQueryInterval = duration.FromSeconds(10);
    public boolean itemRestorationButtonEnabled;
    public boolean charUndeleteEnabled; // Implemented
    public boolean bpayStoreDisabledByParentalControls;
    public boolean twitterEnabled;
    public boolean commerceSystemEnabled;
    public boolean unk67;
    public boolean willKickFromWorld;
    public boolean restrictedAccount;
    public boolean tutorialsEnabled;
    public boolean NPETutorialsEnabled;
    public boolean kioskModeEnabled;
    public boolean competitiveModeEnabled;
    public boolean tokenBalanceEnabled;
    public boolean warModeFeatureEnabled;
    public boolean clubsEnabled;
    public boolean clubsBattleNetClubTypeAllowed;
    public boolean clubsCharacterClubTypeAllowed;
    public boolean clubsPresenceUpdateEnabled;
    public boolean voiceChatDisabledByParentalControl;
    public boolean voiceChatMutedByParentalControl;
    public boolean questSessionEnabled;
    public boolean isMuted;
    public boolean clubFinderEnabled;
    public boolean unknown901CheckoutRelated;
    public boolean textToSpeechFeatureEnabled;
    public boolean chatDisabledByDefault;
    public boolean chatDisabledByPlayer;
    public boolean LFGListCustomRequiresAuthenticator;
    public boolean addonsDisabled;
    public boolean unused1000;

    public socialQueueConfig quickJoinConfig = new socialQueueConfig();
    public squelchInfo squelch = new squelchInfo();
    public rafSystemFeatureInfo RAFSystem = new rafSystemFeatureInfo();
    public ArrayList<GameRuleValuePair> gameRuleValues = new ArrayList<>();

    public FeatureSystemStatus() {
        super(ServerOpcode.FeatureSystemStatus);
    }

    @Override
    public void write() {
        this.writeInt8(complaintStatus);

        this.writeInt32(cfgRealmID);
        this.writeInt32(cfgRealmRecID);

        this.writeInt32(RAFSystem.maxRecruits);
        this.writeInt32(RAFSystem.maxRecruitMonths);
        this.writeInt32(RAFSystem.maxRecruitmentUses);
        this.writeInt32(RAFSystem.daysInCycle);

        this.writeInt32(twitterPostThrottleLimit);
        this.writeInt32(twitterPostThrottleCooldown);

        this.writeInt32(tokenPollTimeSeconds);
        this.writeInt32(kioskSessionMinutes);
        this.writeInt64(tokenBalanceAmount);

        this.writeInt32(bpayStoreProductDeliveryDelay);
        this.writeInt32(clubsPresenceUpdateTimer);
        this.writeInt32(hiddenUIClubsPresenceUpdateTimer);

        this.writeInt32(activeSeason);
        this.writeInt32(gameRuleValues.size());

        this.writeInt16(maxPlayerNameQueriesPerPacket);
        this.writeInt16(playerNameQueryTelemetryInterval);
        this.writeInt32((int) playerNameQueryInterval.TotalSeconds);

        for (var gameRuleValue : gameRuleValues) {
            gameRuleValue.write(this);
        }

        this.writeBit(voiceEnabled);
        this.writeBit(europaTicketSystemStatus != null);
        this.writeBit(bpayStoreEnabled);
        this.writeBit(bpayStoreAvailable);
        this.writeBit(bpayStoreDisabledByParentalControls);
        this.writeBit(itemRestorationButtonEnabled);
        this.writeBit(browserEnabled);

        this.writeBit(sessionAlert != null);
        this.writeBit(RAFSystem.enabled);
        this.writeBit(RAFSystem.recruitingEnabled);
        this.writeBit(charUndeleteEnabled);
        this.writeBit(restrictedAccount);
        this.writeBit(commerceSystemEnabled);
        this.writeBit(tutorialsEnabled);
        this.writeBit(twitterEnabled);

        this.writeBit(unk67);
        this.writeBit(willKickFromWorld);
        this.writeBit(kioskModeEnabled);
        this.writeBit(competitiveModeEnabled);
        this.writeBit(tokenBalanceEnabled);
        this.writeBit(warModeFeatureEnabled);
        this.writeBit(clubsEnabled);
        this.writeBit(clubsBattleNetClubTypeAllowed);

        this.writeBit(clubsCharacterClubTypeAllowed);
        this.writeBit(clubsPresenceUpdateEnabled);
        this.writeBit(voiceChatDisabledByParentalControl);
        this.writeBit(voiceChatMutedByParentalControl);
        this.writeBit(questSessionEnabled);
        this.writeBit(isMuted);
        this.writeBit(clubFinderEnabled);
        this.writeBit(unknown901CheckoutRelated);

        this.writeBit(textToSpeechFeatureEnabled);
        this.writeBit(chatDisabledByDefault);
        this.writeBit(chatDisabledByPlayer);
        this.writeBit(LFGListCustomRequiresAuthenticator);
        this.writeBit(addonsDisabled);
        this.writeBit(unused1000);

        this.flushBits();

        {
            this.writeBit(quickJoinConfig.toastsDisabled);
            this.writeFloat(quickJoinConfig.toastDuration);
            this.writeFloat(quickJoinConfig.delayDuration);
            this.writeFloat(quickJoinConfig.queueMultiplier);
            this.writeFloat(quickJoinConfig.playerMultiplier);
            this.writeFloat(quickJoinConfig.playerFriendValue);
            this.writeFloat(quickJoinConfig.playerGuildValue);
            this.writeFloat(quickJoinConfig.throttleInitialThreshold);
            this.writeFloat(quickJoinConfig.throttleDecayTime);
            this.writeFloat(quickJoinConfig.throttlePrioritySpike);
            this.writeFloat(quickJoinConfig.throttleMinThreshold);
            this.writeFloat(quickJoinConfig.throttlePvPPriorityNormal);
            this.writeFloat(quickJoinConfig.throttlePvPPriorityLow);
            this.writeFloat(quickJoinConfig.throttlePvPHonorThreshold);
            this.writeFloat(quickJoinConfig.throttleLfgListPriorityDefault);
            this.writeFloat(quickJoinConfig.throttleLfgListPriorityAbove);
            this.writeFloat(quickJoinConfig.throttleLfgListPriorityBelow);
            this.writeFloat(quickJoinConfig.throttleLfgListIlvlScalingAbove);
            this.writeFloat(quickJoinConfig.throttleLfgListIlvlScalingBelow);
            this.writeFloat(quickJoinConfig.throttleRfPriorityAbove);
            this.writeFloat(quickJoinConfig.throttleRfIlvlScalingAbove);
            this.writeFloat(quickJoinConfig.throttleDfMaxItemLevel);
            this.writeFloat(quickJoinConfig.throttleDfBestPriority);
        }

        if (sessionAlert != null) {
            this.writeInt32(sessionAlert.getValue().delay);
            this.writeInt32(sessionAlert.getValue().period);
            this.writeInt32(sessionAlert.getValue().displayTime);
        }

        this.writeBit(squelch.isSquelched);
        this.writeGuid(squelch.bnetAccountGuid);
        this.writeGuid(squelch.guildGuid);

        if (europaTicketSystemStatus != null) {
            europaTicketSystemStatus.getValue().write(this);
        }
    }

    public final static class SessionAlertConfig {
        public int delay;
        public int period;
        public int displayTime;

        public SessionAlertConfig clone() {
            SessionAlertConfig varCopy = new SessionAlertConfig();

            varCopy.delay = this.delay;
            varCopy.period = this.period;
            varCopy.displayTime = this.displayTime;

            return varCopy;
        }
    }

    public final static class SocialQueueConfig {
        public boolean toastsDisabled;
        public float toastDuration;
        public float delayDuration;
        public float queueMultiplier;
        public float playerMultiplier;
        public float playerFriendValue;
        public float playerGuildValue;
        public float throttleInitialThreshold;
        public float throttleDecayTime;
        public float throttlePrioritySpike;
        public float throttleMinThreshold;
        public float throttlePvPPriorityNormal;
        public float throttlePvPPriorityLow;
        public float throttlePvPHonorThreshold;
        public float throttleLfgListPriorityDefault;
        public float throttleLfgListPriorityAbove;
        public float throttleLfgListPriorityBelow;
        public float throttleLfgListIlvlScalingAbove;
        public float throttleLfgListIlvlScalingBelow;
        public float throttleRfPriorityAbove;
        public float throttleRfIlvlScalingAbove;
        public float throttleDfMaxItemLevel;
        public float throttleDfBestPriority;

        public SocialQueueConfig clone() {
            SocialQueueConfig varCopy = new socialQueueConfig();

            varCopy.toastsDisabled = this.toastsDisabled;
            varCopy.toastDuration = this.toastDuration;
            varCopy.delayDuration = this.delayDuration;
            varCopy.queueMultiplier = this.queueMultiplier;
            varCopy.playerMultiplier = this.playerMultiplier;
            varCopy.playerFriendValue = this.playerFriendValue;
            varCopy.playerGuildValue = this.playerGuildValue;
            varCopy.throttleInitialThreshold = this.throttleInitialThreshold;
            varCopy.throttleDecayTime = this.throttleDecayTime;
            varCopy.throttlePrioritySpike = this.throttlePrioritySpike;
            varCopy.throttleMinThreshold = this.throttleMinThreshold;
            varCopy.throttlePvPPriorityNormal = this.throttlePvPPriorityNormal;
            varCopy.throttlePvPPriorityLow = this.throttlePvPPriorityLow;
            varCopy.throttlePvPHonorThreshold = this.throttlePvPHonorThreshold;
            varCopy.throttleLfgListPriorityDefault = this.throttleLfgListPriorityDefault;
            varCopy.throttleLfgListPriorityAbove = this.throttleLfgListPriorityAbove;
            varCopy.throttleLfgListPriorityBelow = this.throttleLfgListPriorityBelow;
            varCopy.throttleLfgListIlvlScalingAbove = this.throttleLfgListIlvlScalingAbove;
            varCopy.throttleLfgListIlvlScalingBelow = this.throttleLfgListIlvlScalingBelow;
            varCopy.throttleRfPriorityAbove = this.throttleRfPriorityAbove;
            varCopy.throttleRfIlvlScalingAbove = this.throttleRfIlvlScalingAbove;
            varCopy.throttleDfMaxItemLevel = this.throttleDfMaxItemLevel;
            varCopy.throttleDfBestPriority = this.throttleDfBestPriority;

            return varCopy;
        }
    }

    public final static class SquelchInfo {
        public boolean isSquelched;
        public ObjectGuid bnetAccountGuid = ObjectGuid.EMPTY;
        public ObjectGuid guildGuid = ObjectGuid.EMPTY;

        public SquelchInfo clone() {
            SquelchInfo varCopy = new squelchInfo();

            varCopy.isSquelched = this.isSquelched;
            varCopy.bnetAccountGuid = this.bnetAccountGuid;
            varCopy.guildGuid = this.guildGuid;

            return varCopy;
        }
    }

    public final static class RafSystemFeatureInfo {
        public boolean enabled;
        public boolean recruitingEnabled;
        public int maxRecruits;
        public int maxRecruitMonths;
        public int maxRecruitmentUses;
        public int daysInCycle;

        public RafSystemFeatureInfo clone() {
            RafSystemFeatureInfo varCopy = new rafSystemFeatureInfo();

            varCopy.enabled = this.enabled;
            varCopy.recruitingEnabled = this.recruitingEnabled;
            varCopy.maxRecruits = this.maxRecruits;
            varCopy.maxRecruitMonths = this.maxRecruitMonths;
            varCopy.maxRecruitmentUses = this.maxRecruitmentUses;
            varCopy.daysInCycle = this.daysInCycle;

            return varCopy;
        }
    }
}
