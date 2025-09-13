package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.ChrCustomizationChoice;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.packet.SetSpellModifier;
import com.github.azeroth.game.networking.packet.SpellModifierData;
import com.github.azeroth.game.networking.packet.SpellModifierInfo;
import game.PhasingHandler;

import java.util.ArrayList;
import java.util.Objects;



class ModifyCommand {
    
    private static boolean handleModifyHPCommand(CommandHandler handler, int hp) {
        var target = handler.getSelectedPlayerOrSelf();
        var maxHp = hp;

        tangible.RefObject<Integer> tempRef_hp = new tangible.RefObject<Integer>(hp);
        tangible.RefObject<Integer> tempRef_maxHp = new tangible.RefObject<Integer>(maxHp);
        if (checkModifyResources(handler, target, tempRef_hp, tempRef_maxHp)) {
            maxHp = tempRef_maxHp.refArgValue;
            hp = tempRef_hp.refArgValue;
            notifyModification(handler, target, SysMessage.YouChangeHp, SysMessage.YoursHpChanged, hp, maxHp);
            target.setMaxHealth((int) maxHp);
            target.setHealth((int) hp);

            return true;
        } else {
            maxHp = tempRef_maxHp.refArgValue;
            hp = tempRef_hp.refArgValue;
        }

        return false;
    }

    
    private static boolean handleModifyManaCommand(CommandHandler handler, int mana) {
        var target = handler.getSelectedPlayerOrSelf();
        var maxMana = mana;

        tangible.RefObject<Integer> tempRef_mana = new tangible.RefObject<Integer>(mana);
        tangible.RefObject<Integer> tempRef_maxMana = new tangible.RefObject<Integer>(maxMana);
        if (checkModifyResources(handler, target, tempRef_mana, tempRef_maxMana)) {
            maxMana = tempRef_maxMana.refArgValue;
            mana = tempRef_mana.refArgValue;
            notifyModification(handler, target, SysMessage.YouChangeMana, SysMessage.YoursManaChanged, mana, maxMana);
            target.setMaxPower(powerType.mana, maxMana);
            target.setPower(powerType.mana, mana);

            return true;
        } else {
            maxMana = tempRef_maxMana.refArgValue;
            mana = tempRef_mana.refArgValue;
        }

        return false;
    }

    
    private static boolean handleModifyEnergyCommand(CommandHandler handler, int energy) {
        var target = handler.getSelectedPlayerOrSelf();
        byte energyMultiplier = 10;
        var maxEnergy = energy;

        tangible.RefObject<Integer> tempRef_energy = new tangible.RefObject<Integer>(energy);
        tangible.RefObject<Integer> tempRef_maxEnergy = new tangible.RefObject<Integer>(maxEnergy);
        if (checkModifyResources(handler, target, tempRef_energy, tempRef_maxEnergy, energyMultiplier)) {
            maxEnergy = tempRef_maxEnergy.refArgValue;
            energy = tempRef_energy.refArgValue;
            notifyModification(handler, target, SysMessage.YouChangeEnergy, SysMessage.YoursEnergyChanged, energy / energyMultiplier, maxEnergy / energyMultiplier);
            target.setMaxPower(powerType.Energy, maxEnergy);
            target.setPower(powerType.Energy, energy);

            return true;
        } else {
            maxEnergy = tempRef_maxEnergy.refArgValue;
            energy = tempRef_energy.refArgValue;
        }

        return false;
    }

    
    private static boolean handleModifyRageCommand(CommandHandler handler, int rage) {
        var target = handler.getSelectedPlayerOrSelf();
        byte rageMultiplier = 10;
        var maxRage = rage;

        tangible.RefObject<Integer> tempRef_rage = new tangible.RefObject<Integer>(rage);
        tangible.RefObject<Integer> tempRef_maxRage = new tangible.RefObject<Integer>(maxRage);
        if (checkModifyResources(handler, target, tempRef_rage, tempRef_maxRage, rageMultiplier)) {
            maxRage = tempRef_maxRage.refArgValue;
            rage = tempRef_rage.refArgValue;
            notifyModification(handler, target, SysMessage.YouChangeRage, SysMessage.YoursRageChanged, rage / rageMultiplier, maxRage / rageMultiplier);
            target.setMaxPower(powerType.Rage, maxRage);
            target.setPower(powerType.Rage, rage);

            return true;
        } else {
            maxRage = tempRef_maxRage.refArgValue;
            rage = tempRef_rage.refArgValue;
        }

        return false;
    }

    
    private static boolean handleModifyRunicPowerCommand(CommandHandler handler, int rune) {
        var target = handler.getSelectedPlayerOrSelf();
        byte runeMultiplier = 10;
        var maxRune = rune;

        tangible.RefObject<Integer> tempRef_rune = new tangible.RefObject<Integer>(rune);
        tangible.RefObject<Integer> tempRef_maxRune = new tangible.RefObject<Integer>(maxRune);
        if (checkModifyResources(handler, target, tempRef_rune, tempRef_maxRune, runeMultiplier)) {
            maxRune = tempRef_maxRune.refArgValue;
            rune = tempRef_rune.refArgValue;
            notifyModification(handler, target, SysMessage.YouChangeRunicPower, SysMessage.YoursRunicPowerChanged, rune / runeMultiplier, maxRune / runeMultiplier);
            target.setMaxPower(powerType.RunicPower, maxRune);
            target.setPower(powerType.RunicPower, rune);

            return true;
        } else {
            maxRune = tempRef_maxRune.refArgValue;
            rune = tempRef_rune.refArgValue;
        }

        return false;
    }

    
    private static boolean handleModifyFactionCommand(CommandHandler handler, StringArguments args) {
        var pfactionid = handler.extractKeyFromLink(args, "Hfaction");

        var target = handler.getSelectedCreature();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        int factionid;
        tangible.OutObject<Integer> tempOut_factionid = new tangible.OutObject<Integer>();
        if (!tangible.TryParseHelper.tryParseInt(pfactionid, tempOut_factionid)) {
            factionid = tempOut_factionid.outArgValue;
            var _factionid = target.getFaction();
            int _flag = target.getUnitData().flags;
            var _npcflag = (long) target.getUnitData().npcFlags.get(0) << 32 | target.getUnitData().npcFlags.get(1);
            int _dyflag = target.getObjectData().dynamicFlags;
            handler.sendSysMessage(SysMessage.CurrentFaction, target.getGUID().toString(), _factionid, _flag, _npcflag, _dyflag);

            return true;
        } else {
            factionid = tempOut_factionid.outArgValue;
        }

        int flag;
        tangible.OutObject<Integer> tempOut_flag = new tangible.OutObject<Integer>();
        if (!tangible.TryParseHelper.tryParseInt(args.NextString(" "), tempOut_flag)) {
            flag = tempOut_flag.outArgValue;
            flag = target.getUnitData().flags;
        } else {
            flag = tempOut_flag.outArgValue;
        }

        long npcflag;
        tangible.OutObject<Long> tempOut_npcflag = new tangible.OutObject<Long>();
        if (!tangible.TryParseHelper.tryParseLong(args.NextString(" "), tempOut_npcflag)) {
            npcflag = tempOut_npcflag.outArgValue;
            npcflag = (long) target.getUnitData().npcFlags.get(0) << 32 | target.getUnitData().npcFlags.get(1);
        } else {
            npcflag = tempOut_npcflag.outArgValue;
        }

        int dyflag;
        tangible.OutObject<Integer> tempOut_dyflag = new tangible.OutObject<Integer>();
        if (!tangible.TryParseHelper.tryParseInt(args.NextString(" "), tempOut_dyflag)) {
            dyflag = tempOut_dyflag.outArgValue;
            dyflag = target.getObjectData().dynamicFlags;
        } else {
            dyflag = tempOut_dyflag.outArgValue;
        }

        if (!CliDB.FactionTemplateStorage.containsKey(factionid)) {
            handler.sendSysMessage(SysMessage.WrongFaction, factionid);

            return false;
        }

        handler.sendSysMessage(SysMessage.YouChangeFaction, target.getGUID().toString(), factionid, flag, npcflag, dyflag);

        target.setFaction(factionid);
        target.replaceAllUnitFlags(UnitFlag.forValue(flag));
        target.replaceAllNpcFlags(NPCFlags.forValue(npcflag & 0xFFFFFFFF));
        target.replaceAllNpcFlags2(NPCFlags2.forValue(npcflag >> 32));
        target.replaceAllDynamicFlags(UnitDynFlags.forValue(dyflag));

        return true;
    }

    
    private static boolean handleModifySpellCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var spellflatid = args.NextByte(" ");

        if (spellflatid == 0) {
            return false;
        }

        var op = args.NextByte(" ");

        if (op == 0) {
            return false;
        }

        var val = args.NextUInt16(" ");

        if (val == 0) {
            return false;
        }

        short mark;
        tangible.OutObject<SHORT> tempOut_mark = new tangible.OutObject<SHORT>();
        if (!tangible.TryParseHelper.tryParseShort(args.NextString(" "), tempOut_mark)) {
            mark = tempOut_mark.outArgValue;
            mark = (short) 65535;
        } else {
            mark = tempOut_mark.outArgValue;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        handler.sendSysMessage(SysMessage.YouChangeSpellflatid, spellflatid, val, mark, handler.getNameLink(target));

        if (handler.needReportToTarget(target)) {
            target.sendSysMessage(SysMessage.YoursSpellflatidChanged, handler.getNameLink(), spellflatid, val, mark);
        }

        SetSpellModifier packet = new SetSpellModifier(ServerOpcode.SetFlatSpellModifier);
        SpellModifierInfo spellMod = new SpellModifierInfo();
        spellMod.modIndex = op;
        SpellModifierData modData = new SpellModifierData();
        modData.classIndex = spellflatid;
        modData.modifierValue = val;
        spellMod.modifierData.add(modData);
        packet.modifiers.add(spellMod);
        target.sendPacket(packet);

        return true;
    }

    
    private static boolean handleModifyTalentCommand(CommandHandler handler) {
        return false;
    }

    
    private static boolean handleModifyScaleCommand(CommandHandler handler, StringArguments args) {
        var target = handler.getSelectedUnit();

        float scale;
        tangible.OutObject<Float> tempOut_Scale = new tangible.OutObject<Float>();
        if (checkModifySpeed(args, handler, target, tempOut_Scale, 0.1f, 10.0f, false)) {
            scale = tempOut_Scale.outArgValue;
            notifyModification(handler, target, SysMessage.YouChangeSize, SysMessage.YoursSizeChanged, scale);
            var creatureTarget = target.toCreature();

            if (creatureTarget) {
                creatureTarget.setDisplayId(creatureTarget.getDisplayId(), scale);
            } else {
                target.setObjectScale(scale);
            }

            return true;
        } else {
            scale = tempOut_Scale.outArgValue;
        }

        return false;
    }

    
    private static boolean handleModifyMountCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        int mount;
        tangible.OutObject<Integer> tempOut_mount = new tangible.OutObject<Integer>();
        if (!tangible.TryParseHelper.tryParseInt(args.NextString(" "), tempOut_mount)) {
            mount = tempOut_mount.outArgValue;
            return false;
        } else {
            mount = tempOut_mount.outArgValue;
        }

        if (!CliDB.CreatureDisplayInfoStorage.HasRecord(mount)) {
            handler.sendSysMessage(SysMessage.NoMount);

            return false;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        float speed;
        tangible.OutObject<Float> tempOut_speed = new tangible.OutObject<Float>();
        if (!checkModifySpeed(args, handler, target, tempOut_speed, 0.1f, 50.0f)) {
            speed = tempOut_speed.outArgValue;
            return false;
        } else {
            speed = tempOut_speed.outArgValue;
        }

        notifyModification(handler, target, SysMessage.YouGiveMount, SysMessage.MountGived);
        target.mount(mount);
        target.setSpeedRate(UnitMoveType.run, speed);
        target.setSpeedRate(UnitMoveType.flight, speed);

        return true;
    }

    
    private static boolean handleModifyMoneyCommand(CommandHandler handler, StringArguments args) {
        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var moneyToAdd = args.NextInt64(" ");
        var targetMoney = target.getMoney();

        if (moneyToAdd < 0) {
            var newmoney = (long) targetMoney + moneyToAdd;

            Log.outDebug(LogFilter.ChatSystem, global.getObjectMgr().getSysMessage(SysMessage.CurrentMoney), targetMoney, moneyToAdd, newmoney);

            if (newmoney <= 0) {
                handler.sendSysMessage(SysMessage.YouTakeAllMoney, handler.getNameLink(target));

                if (handler.needReportToTarget(target)) {
                    target.sendSysMessage(SysMessage.YoursAllMoneyGone, handler.getNameLink());
                }

                target.setMoney(0);
            } else {
                var moneyToAddMsg = (long) (moneyToAdd * -1);

                if (newmoney > (long) PlayerConst.MaxMoneyAmount) {
                    newmoney = (long) PlayerConst.MaxMoneyAmount;
                }

                handler.sendSysMessage(SysMessage.YouTakeMoney, moneyToAddMsg, handler.getNameLink(target));

                if (handler.needReportToTarget(target)) {
                    target.sendSysMessage(SysMessage.YoursMoneyTaken, handler.getNameLink(), moneyToAddMsg);
                }

                target.setMoney((long) newmoney);
            }
        } else {
            handler.sendSysMessage(SysMessage.YouGiveMoney, moneyToAdd, handler.getNameLink(target));

            if (handler.needReportToTarget(target)) {
                target.sendSysMessage(SysMessage.YoursMoneyGiven, handler.getNameLink(), moneyToAdd);
            }

            if ((long) moneyToAdd >= PlayerConst.MaxMoneyAmount) {
                moneyToAdd = (long) PlayerConst.MaxMoneyAmount;
            }

            moneyToAdd = (long) Math.min((long) moneyToAdd, (PlayerConst.MaxMoneyAmount - targetMoney));

            target.modifyMoney(moneyToAdd);
        }

        Log.outDebug(LogFilter.ChatSystem, global.getObjectMgr().getSysMessage(SysMessage.NewMoney), targetMoney, moneyToAdd, target.getMoney());

        return true;
    }

    
    private static boolean handleModifyHonorCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var amount = args.NextInt32(" ");

        //target.modifyCurrency(CurrencyTypes.HonorPoints, amount, true, true);
        handler.sendSysMessage("NOT IMPLEMENTED: {0} honor NOT added.", amount);

        //handler.sendSysMessage(SysMessage.CommandModifyHonor, handler.getNameLink(target), target.GetCurrency((uint)CurrencyTypes.HonorPoints));
        return true;
    }

    
    private static boolean handleModifyDrunkCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var drunklevel = args.NextByte(" ");

        if (drunklevel > 100) {
            drunklevel = 100;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (target) {
            target.setDrunkValue(drunklevel);
        }

        return true;
    }

    
    private static boolean handleModifyRepCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var factionTxt = handler.extractKeyFromLink(args, "Hfaction");

        if (StringUtil.isEmpty(factionTxt)) {
            return false;
        }

        int factionId;
        tangible.OutObject<Integer> tempOut_factionId = new tangible.OutObject<Integer>();
        if (!tangible.TryParseHelper.tryParseInt(factionTxt, tempOut_factionId)) {
            factionId = tempOut_factionId.outArgValue;
            return false;
        } else {
            factionId = tempOut_factionId.outArgValue;
        }

        var rankTxt = args.NextString(" ");

        int amount;
        tangible.OutObject<Integer> tempOut_amount = new tangible.OutObject<Integer>();
        if (factionId == 0 || !tangible.TryParseHelper.tryParseInt(rankTxt, tempOut_amount)) {
            amount = tempOut_amount.outArgValue;
            return false;
        } else {
            amount = tempOut_amount.outArgValue;
        }

        var factionEntry = CliDB.FactionStorage.get(factionId);

        if (factionEntry == null) {
            handler.sendSysMessage(SysMessage.CommandFactionUnknown, factionId);

            return false;
        }

        if (factionEntry.ReputationIndex < 0) {
            handler.sendSysMessage(SysMessage.CommandFactionNorepError, factionEntry.name.charAt(handler.getSessionDbcLocale()), factionId);

            return false;
        }

        // try to find rank by name
        if ((amount == 0) && !(amount < 0) && !rankTxt.IsNumber()) {
            var rankStr = rankTxt.toLowerCase();

            var i = 0;
            var r = 0;

            for (; i != ReputationMgr.REPUTATIONRANKTHRESHOLDS.length - 1; ++i, ++r) {
                var rank = handler.getSysMessage(ReputationMgr.ReputationRankStrIndex[r]);

                if (StringUtil.isEmpty(rank)) {
                    continue;
                }

                if (rank.equalsIgnoreCase(rankStr)) {
                    break;
                }

                if (i == ReputationMgr.REPUTATIONRANKTHRESHOLDS.length - 1) {
                    handler.sendSysMessage(SysMessage.CommandInvalidParam, rankTxt);

                    return false;
                }

                amount = ReputationMgr.ReputationRankThresholds[i];

                var deltaTxt = args.NextString(" ");

                if (!StringUtil.isEmpty(deltaTxt)) {
                    var toNextRank = 0;
                    var nextThresholdIndex = i;
                    ++nextThresholdIndex;

                    if (nextThresholdIndex != ReputationMgr.REPUTATIONRANKTHRESHOLDS.length - 1) {
                        toNextRank = nextThresholdIndex - i;
                    }

                    int delta;
                    tangible.OutObject<Integer> tempOut_delta = new tangible.OutObject<Integer>();
                    if (!tangible.TryParseHelper.tryParseInt(deltaTxt, tempOut_delta) || delta < 0 || delta >= toNextRank) {
                        delta = tempOut_delta.outArgValue;
                        handler.sendSysMessage(SysMessage.CommandFactionDelta, Math.max(0, toNextRank - 1));

                        return false;
                    } else {
                        delta = tempOut_delta.outArgValue;
                    }

                    amount += delta;
                }
            }
        }

        target.getReputationMgr().setOneFactionReputation(factionEntry, amount, false);
        target.getReputationMgr().sendState(target.getReputationMgr().getState(factionEntry));
        handler.sendSysMessage(SysMessage.CommandModifyRep, factionEntry.name.charAt(handler.getSessionDbcLocale()), factionId, handler.getNameLink(target), target.getReputationMgr().getReputation(factionEntry));

        return true;
    }

    
    private static boolean handleModifyPhaseCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var phaseId = args.NextUInt32(" ");
        var visibleMapId = args.NextUInt32(" ");

        if (phaseId != 0 && !CliDB.PhaseStorage.containsKey(phaseId)) {
            handler.sendSysMessage(SysMessage.PhaseNotfound);

            return false;
        }

        var target = handler.getSelectedUnit();

        if (visibleMapId != 0) {
            var visibleMap = CliDB.MapStorage.get(visibleMapId);

            if (visibleMap == null || visibleMap.ParentMapID != target.getLocation().getMapId()) {
                handler.sendSysMessage(SysMessage.PhaseNotfound);

                return false;
            }

            if (!target.getPhaseShift().hasVisibleMapId(visibleMapId)) {
                PhasingHandler.addVisibleMapId(target, visibleMapId);
            } else {
                PhasingHandler.removeVisibleMapId(target, visibleMapId);
            }
        }

        if (phaseId != 0) {
            if (!target.getPhaseShift().hasPhase(phaseId)) {
                PhasingHandler.addPhase(target, phaseId, true);
            } else {
                PhasingHandler.removePhase(target, phaseId, true);
            }
        }

        return true;
    }

    
    private static boolean handleModifyPowerCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var powerTypeToken = args.NextString(" ");

        if (powerTypeToken.isEmpty()) {
            return false;
        }

        var powerType = global.getDB2Mgr().GetPowerTypeByName(powerTypeToken);

        if (powerType == null) {
            handler.sendSysMessage(SysMessage.InvalidPowerName);

            return false;
        }

        if (target.getPowerIndex(powerType.PowerTypeEnum) == powerType.max.getValue()) {
            handler.sendSysMessage(SysMessage.InvalidPowerName);

            return false;
        }

        var powerAmount = args.NextInt32(" ");

        if (powerAmount < 1) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        notifyModification(handler, target, SysMessage.YouChangePower, SysMessage.YourPowerChanged, powerType.NameGlobalStringTag, powerAmount, powerAmount);
        powerAmount *= powerType.DisplayModifier;
        target.setMaxPower(powerType.PowerTypeEnum, powerAmount);
        target.setPower(powerType.PowerTypeEnum, powerAmount);

        return true;
    }

    
    private static boolean handleModifyStandStateCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var anim_id = args.NextUInt32(" ");
        handler.getSession().getPlayer().setEmoteState(emote.forValue(anim_id));

        return true;
    }

    
    private static boolean handleModifyGenderCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var info = global.getObjectMgr().getPlayerInfo(target.getRace(), target.getClass());

        if (info == null) {
            return false;
        }

        var gender_str = args.NextString(" ");
        Gender gender;

        if (Objects.equals(gender_str, "male")) // MALE
        {
            if (target.getGender() == gender.Male) {
                return true;
            }

            gender = gender.Male;
        } else if (Objects.equals(gender_str, "female")) // FEMALE
        {
            if (target.getGender() == gender.Female) {
                return true;
            }

            gender = gender.Female;
        } else {
            handler.sendSysMessage(SysMessage.MustMaleOrFemale);

            return false;
        }

        // Set gender
        target.setGender(gender);

        target.setNativeGender(gender);

        // Change display ID
        target.initDisplayIds();

        target.restoreDisplayId(false);
        global.getCharacterCacheStorage().updateCharacterGender(target.getGUID(), (byte) gender.getValue());

        // Generate random customizations
        ArrayList<ChrCustomizationChoice> customizations = new ArrayList<>();

        var options = global.getDB2Mgr().GetCustomiztionOptions(target.getRace(), gender);
        var worldSession = target.getSession();

        for (var option : options) {
            var optionReq = CliDB.ChrCustomizationReqStorage.get(option.ChrCustomizationReqID);

            if (optionReq != null && !worldSession.meetsChrCustomizationReq(optionReq, target.getClass(), false, customizations)) {
                continue;
            }

            // Loop over the options until the first one fits
            var choicesForOption = global.getDB2Mgr().GetCustomiztionChoices(option.id);

            for (var choiceForOption : choicesForOption) {
                var choiceReq = CliDB.ChrCustomizationReqStorage.get(choiceForOption.ChrCustomizationReqID);

                if (choiceReq != null && !worldSession.meetsChrCustomizationReq(choiceReq, target.getClass(), false, customizations)) {
                    continue;
                }

                var choiceEntry = choicesForOption.get(0);
                ChrCustomizationChoice choice = new ChrCustomizationChoice();
                choice.chrCustomizationOptionID = option.id;
                choice.chrCustomizationChoiceID = choiceEntry.id;
                customizations.add(choice);

                break;
            }
        }

        target.setCustomizations(customizations);

        handler.sendSysMessage(SysMessage.YouChangeGender, handler.getNameLink(target), gender);

        if (handler.needReportToTarget(target)) {
            target.sendSysMessage(SysMessage.YourGenderChanged, gender, handler.getNameLink());
        }

        return true;
    }

    
    private static boolean handleModifyCurrencyCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var currencyId = args.NextUInt32(" ");

        if (!CliDB.CurrencyTypesStorage.containsKey(currencyId)) {
            return false;
        }

        var amount = args.NextUInt32(" ");

        if (amount == 0) {
            return false;
        }

        target.modifyCurrency(currencyId, (int) amount, CurrencyGainSource.Cheat, CurrencyDestroyReason.Cheat);

        return true;
    }

    
    private static boolean handleModifyXPCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var xp = args.NextInt32(" ");

        if (xp < 1) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        var target = handler.getSelectedPlayerOrSelf();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        // we can run the command
        target.giveXP((int) xp, null);

        return true;
    }

    
    private static boolean handleModifyMorphCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var display_id = args.NextUInt32(" ");

        var target = handler.getSelectedUnit();

        if (!target) {
            target = handler.getSession().getPlayer();
        }

        // check online security
        else if (target.isTypeId(TypeId.PLAYER) && handler.hasLowerSecurity(target.toPlayer(), ObjectGuid.Empty)) {
            return false;
        }

        target.setDisplayId(display_id);

        return true;
    }

    
    private static boolean handleDeMorphCommand(CommandHandler handler) {
        var target = handler.getSelectedUnit();

        if (!target) {
            target = handler.getSession().getPlayer();
        }

        // check online security
        else if (target.isTypeId(TypeId.PLAYER) && handler.hasLowerSecurity(target.toPlayer(), ObjectGuid.Empty)) {
            return false;
        }

        target.deMorph();

        return true;
    }

    private static void notifyModification(CommandHandler handler, Unit target, SysMessage resourceMessage, SysMessage resourceReportMessage, object... args) {
        var player = target.toPlayer();

        if (player) {
            handler.sendSysMessage(resourceMessage, new Object[]{handler.getNameLink(player)}.Combine(args));

            if (handler.needReportToTarget(player)) {
                player.sendSysMessage(resourceReportMessage, new Object[]{handler.getNameLink()}.Combine(args));
            }
        }
    }


    private static boolean checkModifyResources(CommandHandler handler, Player target, tangible.RefObject<Integer> res, tangible.RefObject<Integer> resmax) {
        return checkModifyResources(handler, target, res, resmax, 1);
    }

    private static boolean checkModifyResources(CommandHandler handler, Player target, tangible.RefObject<Integer> res, tangible.RefObject<Integer> resmax, byte multiplier) {
        res.refArgValue *= multiplier;
        resmax.refArgValue *= multiplier;

        if (resmax.refArgValue == 0) {
            resmax.refArgValue = res.refArgValue;
        }

        if (res.refArgValue < 1 || resmax.refArgValue < 1 || resmax.refArgValue < res.refArgValue) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        return true;
    }


    private static boolean checkModifySpeed(StringArguments args, CommandHandler handler, Unit target, tangible.OutObject<Float> speed, float minimumBound, float maximumBound) {
        return checkModifySpeed(args, handler, target, speed, minimumBound, maximumBound, true);
    }

    private static boolean checkModifySpeed(StringArguments args, CommandHandler handler, Unit target, tangible.OutObject<Float> speed, float minimumBound, float maximumBound, boolean checkInFlight) {
        speed.outArgValue = 0f;

        if (args.isEmpty()) {
            return false;
        }

        speed.outArgValue = args.NextSingle(" ");

        if (speed.outArgValue > maximumBound || speed.outArgValue < minimumBound) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        var player = target.toPlayer();

        if (player) {
            // check online security
            if (handler.hasLowerSecurity(player, ObjectGuid.Empty)) {
                return false;
            }

            if (player.isInFlight() && checkInFlight) {
                handler.sendSysMessage(SysMessage.CharInFlight, handler.getNameLink(player));

                return false;
            }
        }

        return true;
    }

    
    private static class ModifySpeed {
        
        private static boolean handleModifySpeedCommand(CommandHandler handler, StringArguments args) {
            return handleModifyASpeedCommand(handler, args);
        }

        
        private static boolean handleModifyASpeedCommand(CommandHandler handler, StringArguments args) {
            var target = handler.getSelectedPlayerOrSelf();

            float allSpeed;
            tangible.OutObject<Float> tempOut_allSpeed = new tangible.OutObject<Float>();
            if (checkModifySpeed(args, handler, target, tempOut_allSpeed, 0.1f, 50.0f)) {
                allSpeed = tempOut_allSpeed.outArgValue;
                notifyModification(handler, target, SysMessage.YouChangeAspeed, SysMessage.YoursAspeedChanged, allSpeed);
                target.setSpeedRate(UnitMoveType.Walk, allSpeed);
                target.setSpeedRate(UnitMoveType.run, allSpeed);
                target.setSpeedRate(UnitMoveType.swim, allSpeed);
                target.setSpeedRate(UnitMoveType.flight, allSpeed);

                return true;
            } else {
                allSpeed = tempOut_allSpeed.outArgValue;
            }

            return false;
        }

        
        private static boolean handleModifySwimCommand(CommandHandler handler, StringArguments args) {
            var target = handler.getSelectedPlayerOrSelf();

            float swimSpeed;
            tangible.OutObject<Float> tempOut_swimSpeed = new tangible.OutObject<Float>();
            if (checkModifySpeed(args, handler, target, tempOut_swimSpeed, 0.1f, 50.0f)) {
                swimSpeed = tempOut_swimSpeed.outArgValue;
                notifyModification(handler, target, SysMessage.YouChangeSwimSpeed, SysMessage.YoursSwimSpeedChanged, swimSpeed);
                target.setSpeedRate(UnitMoveType.swim, swimSpeed);

                return true;
            } else {
                swimSpeed = tempOut_swimSpeed.outArgValue;
            }

            return false;
        }

        
        private static boolean handleModifyBWalkCommand(CommandHandler handler, StringArguments args) {
            var target = handler.getSelectedPlayerOrSelf();

            float backSpeed;
            tangible.OutObject<Float> tempOut_backSpeed = new tangible.OutObject<Float>();
            if (checkModifySpeed(args, handler, target, tempOut_backSpeed, 0.1f, 50.0f)) {
                backSpeed = tempOut_backSpeed.outArgValue;
                notifyModification(handler, target, SysMessage.YouChangeBackSpeed, SysMessage.YoursBackSpeedChanged, backSpeed);
                target.setSpeedRate(UnitMoveType.RunBack, backSpeed);

                return true;
            } else {
                backSpeed = tempOut_backSpeed.outArgValue;
            }

            return false;
        }

        
        private static boolean handleModifyFlyCommand(CommandHandler handler, StringArguments args) {
            var target = handler.getSelectedPlayerOrSelf();

            float flySpeed;
            tangible.OutObject<Float> tempOut_flySpeed = new tangible.OutObject<Float>();
            if (checkModifySpeed(args, handler, target, tempOut_flySpeed, 0.1f, 50.0f, false)) {
                flySpeed = tempOut_flySpeed.outArgValue;
                notifyModification(handler, target, SysMessage.YouChangeFlySpeed, SysMessage.YoursFlySpeedChanged, flySpeed);
                target.setSpeedRate(UnitMoveType.flight, flySpeed);

                return true;
            } else {
                flySpeed = tempOut_flySpeed.outArgValue;
            }

            return false;
        }

        
        private static boolean handleModifyWalkSpeedCommand(CommandHandler handler, StringArguments args) {
            var target = handler.getSelectedPlayerOrSelf();

            float speed;
            tangible.OutObject<Float> tempOut_Speed = new tangible.OutObject<Float>();
            if (checkModifySpeed(args, handler, target, tempOut_Speed, 0.1f, 50.0f)) {
                speed = tempOut_Speed.outArgValue;
                notifyModification(handler, target, SysMessage.YouChangeSpeed, SysMessage.YoursSpeedChanged, speed);
                target.setSpeedRate(UnitMoveType.run, speed);

                return true;
            } else {
                speed = tempOut_Speed.outArgValue;
            }

            return false;
        }
    }
}
