package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import com.github.azeroth.game.networking.packet.item.ItemBonuses;
import com.github.azeroth.game.networking.packet.item.ItemInstance;
import com.github.azeroth.game.domain.player.PlayerChoiceResponse;
import com.github.azeroth.game.domain.player.PlayerChoiceResponseMawPower;
import com.github.azeroth.game.domain.player.PlayerChoiceResponseReward;

import java.util.ArrayList;

public class DisplayPlayerChoice extends ServerPacket {
    public ObjectGuid senderGUID = ObjectGuid.EMPTY;
    public int choiceID;
    public int uiTextureKitID;
    public int soundKitID;
    public int closeUISoundKitID;
    public byte numRerolls;
    public long duration;
    public String question;
    public String pendingChoiceText;
    public ArrayList<PlayerChoiceResponse> responses = new ArrayList<>();
    public boolean closeChoiceFrame;
    public boolean hideWarboardHeader;
    public boolean keepOpenAfterChoice;

    public DisplayPlayerChoice() {
        super(ServerOpCode.SMSG_DISPLAY_PLAYER_CHOICE);
    }

    @Override
    public void write() {
        this.writeInt32(choiceID);
        this.writeInt32(responses.size());
        this.writeGuid(senderGUID);
        this.writeInt32(uiTextureKitID);
        this.writeInt32(soundKitID);
        this.writeInt32(closeUISoundKitID);
        this.writeInt8(numRerolls);
        this.writeInt64(duration);
        this.writeBits(question.getBytes().length, 8);
        this.writeBits(pendingChoiceText.getBytes().length, 8);
        this.writeBit(closeChoiceFrame);
        this.writeBit(hideWarboardHeader);
        this.writeBit(keepOpenAfterChoice);
        this.flushBits();

        for (var response : responses) {
            writePlayerChoiceResponse(response);
        }

        this.writeString(question);
        this.writeString(pendingChoiceText);
    }

    private void writePlayerChoiceResponse(PlayerChoiceResponse response) {
        this.writeInt32(response.responseID);
        this.writeInt16(response.responseIdentifier);
        this.writeInt32(response.choiceArtFileID);
        this.writeInt32(response.flags);
        this.writeInt32(response.widgetSetID);
        this.writeInt32(response.uiTextureAtlasElementID);
        this.writeInt32(response.soundKitID);
        this.writeInt8(response.groupID);
        this.writeInt32(response.uiTextureKitID);

        this.writeBits(response.answer.getBytes().length, 9);
        this.writeBits(response.header.getBytes().length, 9);
        this.writeBits(response.subHeader.getBytes().length, 7);
        this.writeBits(response.buttonTooltip.getBytes().length, 9);
        this.writeBits(response.description.getBytes().length, 11);
        this.writeBits(response.confirmation.getBytes().length, 7);

        this.writeBit(response.rewardQuestID != null);
        this.writeBit(response.reward != null);
        this.writeBit(response.mawPower != null);
        this.flushBits();

        if (response.reward != null) {
            writePlayerChoiceResponseReward(response.reward);
        }

        this.writeString(response.answer);
        this.writeString(response.header);
        this.writeString(response.subHeader);
        this.writeString(response.buttonTooltip);
        this.writeString(response.description);
        this.writeString(response.confirmation);

        if (response.rewardQuestID != null) {
            this.writeInt32(response.rewardQuestID);
        }

        if (response.mawPower != null) {
            writePlayerChoiceResponseMawPower(response.mawPower);
        }
    }

    private void writePlayerChoiceResponseReward(PlayerChoiceResponseReward reward) {


        this.writeInt32(reward.titleID);
        this.writeInt32(reward.packageID);
        this.writeInt32(reward.skillLineID);
        this.writeInt32(reward.skillPointCount);
        this.writeInt32(reward.arenaPointCount);
        this.writeInt32(reward.honorPointCount);
        this.writeInt64(reward.money);
        this.writeInt32(reward.xp);

        this.writeInt32(reward.items.size());
        this.writeInt32(reward.currencies.size());
        this.writeInt32(reward.factions.size());
        this.writeInt32(reward.itemChoices.size());

        for (var item : reward.items) {
            ItemBonuses itemBonuses = new ItemBonuses();
            ItemInstance instance = new ItemInstance(item.itemId);
            item.write(data);
            item.write(data);
            this.writeInt32(item.quantity);
        }

        for (var currency : reward.currencies) {
            currency.write(data);
        }

        for (var faction : reward.factions) {
            faction.write(data);
        }

        for (var itemChoice : reward.itemChoices) {
            itemChoice.write(data);
        }


    }

    public void writePlayerChoiceResponseMawPower(PlayerChoiceResponseMawPower mawPower) {
        this.writeInt32(mawPower.unused901_1);
        this.writeInt32(mawPower.typeArtFileID);
        this.writeInt32(mawPower.unused901_2);
        this.writeInt32(mawPower.spellID);
        this.writeInt32(mawPower.maxStacks);
        this.writeBit(mawPower.rarity != null);
        this.writeBit(mawPower.rarityColor != null);
        this.flushBits();

        if (mawPower.rarity != null) {
            this.writeInt32(mawPower.rarity);
        }

        if (mawPower.rarityColor != null) {
            this.writeInt32(mawPower.rarityColor);
        }
    }
}
