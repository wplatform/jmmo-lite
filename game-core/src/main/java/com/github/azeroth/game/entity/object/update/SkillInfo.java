package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class SkillInfo extends UpdateMaskObject {
    public Array<SHORT> skillLineID = new Array<SHORT>(256, 0, 1);
    public Array<SHORT> skillStep = new Array<SHORT>(256, 0, 257);
    public Array<SHORT> skillRank = new Array<SHORT>(256, 0, 513);
    public Array<SHORT> skillStartingRank = new Array<SHORT>(256, 0, 769);
    public Array<SHORT> skillMaxRank = new Array<SHORT>(256, 0, 1025);
    public Array<SHORT> skillTempBonus = new Array<SHORT>(256, 0, 1281);
    public Array<SHORT> skillPermBonus = new Array<SHORT>(256, 0, 1537);

    public SkillInfo() {
        super(1793);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        for (var i = 0; i < 256; ++i) {
            data.writeInt16(skillLineID.get(i));
            data.writeInt16(skillStep.get(i));
            data.writeInt16(skillRank.get(i));
            data.writeInt16(skillStartingRank.get(i));
            data.writeInt16(skillMaxRank.get(i));
            data.writeInt16(skillTempBonus.get(i));
            data.writeInt16(skillPermBonus.get(i));
        }
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        for (int i = 0; i < 1; ++i) {
            data.writeInt32(changesMask.getBlocksMask(i));
        }

        data.writeBits(changesMask.getBlocksMask(1), 25);

        for (int i = 0; i < 57; ++i) {
            if (changesMask.getBlock(i) != 0) {
                data.writeBits(changesMask.getBlock(i), 32);
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            for (var i = 0; i < 256; ++i) {
                if (changesMask.get(1 + i)) {
                    data.writeInt16(skillLineID.get(i));
                }

                if (changesMask.get(257 + i)) {
                    data.writeInt16(skillStep.get(i));
                }

                if (changesMask.get(513 + i)) {
                    data.writeInt16(skillRank.get(i));
                }

                if (changesMask.get(769 + i)) {
                    data.writeInt16(skillStartingRank.get(i));
                }

                if (changesMask.get(1025 + i)) {
                    data.writeInt16(skillMaxRank.get(i));
                }

                if (changesMask.get(1281 + i)) {
                    data.writeInt16(skillTempBonus.get(i));
                }

                if (changesMask.get(1537 + i)) {
                    data.writeInt16(skillPermBonus.get(i));
                }
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(skillLineID);
        clearChangesMask(skillStep);
        clearChangesMask(skillRank);
        clearChangesMask(skillStartingRank);
        clearChangesMask(skillMaxRank);
        clearChangesMask(skillTempBonus);
        clearChangesMask(skillPermBonus);
        getChangesMask().resetAll();
    }
}
