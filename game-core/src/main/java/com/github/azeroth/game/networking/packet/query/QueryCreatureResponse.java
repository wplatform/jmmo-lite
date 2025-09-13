package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.common.Locale;
import com.github.azeroth.game.domain.creature.CreatureTemplate;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import com.github.azeroth.utils.StringUtil;

public class QueryCreatureResponse extends ServerPacket {
    public boolean allow;
    public CreatureStats stats;
    public int creatureID;

    public QueryCreatureResponse() {
        super(ServerOpCode.SMSG_QUERY_CREATURE_RESPONSE);
    }

    public static QueryCreatureResponse create(CreatureTemplate template, Locale locale) {
        QueryCreatureResponse queryData = new QueryCreatureResponse();

        queryData.creatureID = template.entry;
        queryData.allow = true;

        CreatureStats stats = new CreatureStats();
        stats.leader = template.racialLeader;

        stats.name[0] = template.name.get(locale);
        stats.nameAlt[0]= template.femaleName.get(locale);

        stats.flags[0] = template.typeFlags.getFlag();
        stats.flags[1] = template.typeFlags2.getFlag();

        stats.creatureType = template.type.ordinal();
        stats.creatureFamily = template.family.ordinal();
        stats.classification = template.rank.ordinal();

        System.arraycopy(template.killCredit, 0, stats.proxyCreatureID, 0, CreatureTemplate.MAX_KILL_CREDIT);

        for (var model : template.models) {
            stats.display.totalProbability += model.probability;
            stats.display.creatureDisplay.add(new CreatureXDisplay(model.creatureDisplayId, model.displayScale, model.probability));
        }

        stats.hpMulti = template.modHealth;
        stats.energyMulti = template.modMana;

        stats.creatureMovementInfoID = template.movementId;
        stats.requiredExpansion = template.requiredExpansion;
        stats.healthScalingExpansion = template.healthScalingExpansion;
        stats.vignetteID = template.vignetteID;
        stats.unitClass = template.unitClass;
        stats.creatureDifficultyID = template.creatureDifficultyID;
        stats.widgetSetID = template.widgetSetID;
        stats.widgetSetUnitConditionID = template.widgetSetUnitConditionID;

        stats.title = template.subName.get(locale);
        stats.titleAlt = template.titleAlt.get(locale);
        stats.cursorName = template.iconName.get(locale);

        stats.questItems = template.questItems == null ? new int[0] : template.questItems;
        queryData.stats = stats;

        return queryData;
    }

    @Override
    public void write() {
        this.writeInt32(creatureID);
        this.writeBit(allow);
        this.flushBits();

        if (allow) {
            this.writeBits(stats.title.isEmpty() ? 0 : stats.title.getBytes().length + 1, 11);
            this.writeBits(stats.titleAlt.isEmpty() ? 0 : stats.titleAlt.getBytes().length + 1, 11);
            this.writeBits(stats.cursorName.isEmpty() ? 0 : stats.cursorName.getBytes().length + 1, 6);
            this.writeBit(stats.leader);

            for (var i = 0; i < CreatureTemplate.MAX_CREATURE_NAMES; ++i) {
                this.writeBits(stats.name[1].getBytes().length + 1, 11);
                this.writeBits(stats.nameAlt[1].getBytes().length + 1, 11);
            }

            for (var i = 0; i < CreatureTemplate.MAX_CREATURE_NAMES; ++i) {
                if (!StringUtil.isEmpty(stats.name[i])) {
                    this.writeCString(stats.name[i]);
                }

                if (!StringUtil.isEmpty(stats.nameAlt[i])) {
                    this.writeCString(stats.nameAlt[i]);
                }
            }

            for (var i = 0; i < 2; ++i) {
                this.writeInt32(stats.flags[i]);
            }

            this.writeInt32(stats.creatureType);
            this.writeInt32(stats.creatureFamily);
            this.writeInt32(stats.classification);

            for (var i = 0; i < CreatureTemplate.MAX_KILL_CREDIT; ++i) {
                this.writeInt32(stats.proxyCreatureID[i]);
            }

            this.writeInt32(stats.display.creatureDisplay.size());
            this.writeFloat(stats.display.totalProbability);

            for (var display : stats.display.creatureDisplay) {
                this.writeInt32(display.creatureDisplayID);
                this.writeFloat(display.scale);
                this.writeFloat(display.probability);
            }

            this.writeFloat(stats.hpMulti);
            this.writeFloat(stats.energyMulti);

            this.writeInt32(stats.questItems.length);
            this.writeInt32(stats.creatureMovementInfoID);
            this.writeInt32(stats.healthScalingExpansion);
            this.writeInt32(stats.requiredExpansion);
            this.writeInt32(stats.vignetteID);
            this.writeInt32(stats.unitClass);
            this.writeInt32(stats.creatureDifficultyID);
            this.writeInt32(stats.widgetSetID);
            this.writeInt32(stats.widgetSetUnitConditionID);

            if (!stats.title.isEmpty()) {
                this.writeCString(stats.title);
            }

            if (!stats.titleAlt.isEmpty()) {
                this.writeCString(stats.titleAlt);
            }

            if (!stats.cursorName.isEmpty()) {
                this.writeCString(stats.cursorName);
            }

            for (var questItem : stats.questItems) {
                this.writeInt32(questItem);
            }
        }
    }
}
