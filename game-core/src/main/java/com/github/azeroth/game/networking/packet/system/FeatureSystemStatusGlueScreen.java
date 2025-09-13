package com.github.azeroth.game.networking.packet.system;


import java.util.ArrayList;


public class FeatureSystemStatusGlueScreen extends ServerPacket {
    public boolean bpayStoreAvailable; // NYI
    public boolean bpayStoreDisabledByParentalControls; // NYI
    public boolean charUndeleteEnabled;
    public boolean bpayStoreEnabled; // NYI
    public boolean commerceSystemEnabled; // NYI
    public boolean unk14; // NYI
    public boolean willKickFromWorld; // NYI
    public boolean isExpansionPreorderInStore; // NYI
    public boolean kioskModeEnabled; // NYI
    public boolean competitiveModeEnabled; // NYI
    public boolean trialBoostEnabled; // NYI
    public boolean tokenBalanceEnabled; // NYI
    public boolean liveRegionCharacterListEnabled; // NYI
    public boolean liveRegionCharacterCopyEnabled; // NYI
    public boolean liveRegionAccountCopyEnabled; // NYI
    public boolean liveRegionKeyBindingsCopyEnabled;
    public boolean unknown901CheckoutRelated; // NYI
    public boolean addonsDisabled;
    public boolean unused1000;
    public EuropaTicketConfig europaTicketSystemStatus = null;
    public ArrayList<Integer> liveRegionCharacterCopySourceRegions = new ArrayList<>();
    public int tokenPollTimeSeconds; // NYI
    public long tokenBalanceAmount; // NYI
    public int maxCharactersPerRealm;
    public int bpayStoreProductDeliveryDelay; // NYI
    public int activeCharacterUpgradeBoostType; // NYI
    public int activeClassTrialBoostType; // NYI
    public int minimumExpansionLevel;
    public int maximumExpansionLevel;
    public int kioskSessionMinutes;
    public int activeSeason; // Currently active Classic season
    public ArrayList<GameRuleValuePair> gameRuleValues = new ArrayList<>();
    public short maxPlayerNameQueriesPerPacket = 50;
    public short playerNameQueryTelemetryInterval = 600;
    public Duration playerNameQueryInterval = duration.FromSeconds(10);
    public Integer launchETA = null;

    public FeatureSystemStatusGlueScreen() {
        super(ServerOpcode.FeatureSystemStatusGlueScreen);
    }

    @Override
    public void write() {
        this.writeBit(bpayStoreEnabled);
        this.writeBit(bpayStoreAvailable);
        this.writeBit(bpayStoreDisabledByParentalControls);
        this.writeBit(charUndeleteEnabled);
        this.writeBit(commerceSystemEnabled);
        this.writeBit(unk14);
        this.writeBit(willKickFromWorld);
        this.writeBit(isExpansionPreorderInStore);

        this.writeBit(kioskModeEnabled);
        this.writeBit(competitiveModeEnabled);
        this.writeBit(false); // unused, 10.0.2
        this.writeBit(trialBoostEnabled);
        this.writeBit(tokenBalanceEnabled);
        this.writeBit(liveRegionCharacterListEnabled);
        this.writeBit(liveRegionCharacterCopyEnabled);
        this.writeBit(liveRegionAccountCopyEnabled);

        this.writeBit(liveRegionKeyBindingsCopyEnabled);
        this.writeBit(unknown901CheckoutRelated);
        this.writeBit(false); // unused, 10.0.2
        this.writeBit(europaTicketSystemStatus != null);
        this.writeBit(false); // unused, 10.0.2
        this.writeBit(launchETA != null);
        this.writeBit(addonsDisabled);
        this.writeBit(unused1000);
        this.flushBits();

        if (europaTicketSystemStatus != null) {
            europaTicketSystemStatus.getValue().write(this);
        }

        this.writeInt32(tokenPollTimeSeconds);
        this.writeInt32(kioskSessionMinutes);
        this.writeInt64(tokenBalanceAmount);
        this.writeInt32(maxCharactersPerRealm);
        this.writeInt32(liveRegionCharacterCopySourceRegions.size());
        this.writeInt32(bpayStoreProductDeliveryDelay);
        this.writeInt32(activeCharacterUpgradeBoostType);
        this.writeInt32(activeClassTrialBoostType);
        this.writeInt32(minimumExpansionLevel);
        this.writeInt32(maximumExpansionLevel);
        this.writeInt32(activeSeason);
        this.writeInt32(gameRuleValues.size());
        this.writeInt16(maxPlayerNameQueriesPerPacket);
        this.writeInt16(playerNameQueryTelemetryInterval);
        this.writeInt32((int) playerNameQueryInterval.TotalSeconds);

        if (launchETA != null) {
            this.writeInt32(launchETA.intValue());
        }

        for (var sourceRegion : liveRegionCharacterCopySourceRegions) {
            this.writeInt32(sourceRegion);
        }

        for (var gameRuleValue : gameRuleValues) {
            gameRuleValue.write(this);
        }
    }
}
