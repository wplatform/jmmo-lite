package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RequiredArgsConstructor
public class SpellHistory {
    private final Unit owner;
    private final LoopSafeDictionary<Integer, CooldownEntry> spellCooldowns = new LoopSafeDictionary<Integer, CooldownEntry>();
    private final LoopSafeDictionary<Integer, CooldownEntry> categoryCooldowns = new LoopSafeDictionary<Integer, CooldownEntry>();
    private final LocalDateTime[] schoolLockouts = new LocalDateTime[SpellSchool.max.getValue()];
    private final MultiMap<Integer, ChargeEntry> categoryCharges = new MultiMap<Integer, ChargeEntry>();
    private final HashMap<Integer, LocalDateTime> globalCooldowns = new HashMap<Integer, LocalDateTime>();
    private HashMap<Integer, CooldownEntry> spellCooldownsBeforeDuel = new HashMap<Integer, CooldownEntry>();

    public final HashSet<Integer> getSpellsOnCooldown() {
        return spellCooldowns.keySet().ToHashSet();
    }

    public final <T extends WorldObject> void loadFromDb(SQLResult cooldownsResult, SQLResult chargesResult) {
        if (!cooldownsResult.isEmpty()) {
            do {
                CooldownEntry cooldownEntry = new CooldownEntry();
                cooldownEntry.spellId = cooldownsResult.<Integer>Read(0);

                if (!global.getSpellMgr().hasSpellInfo(cooldownEntry.spellId)) {
                    continue;
                }

                if (T.class == pet.class) {
                    cooldownEntry.cooldownEnd = time.UnixTimeToDateTime(cooldownsResult.<Long>Read(1));
                    cooldownEntry.itemId = 0;
                    cooldownEntry.categoryId = cooldownsResult.<Integer>Read(2);
                    cooldownEntry.categoryEnd = time.UnixTimeToDateTime(cooldownsResult.<Long>Read(3));
                } else {
                    cooldownEntry.cooldownEnd = time.UnixTimeToDateTime(cooldownsResult.<Long>Read(2));
                    cooldownEntry.itemId = cooldownsResult.<Integer>Read(1);
                    cooldownEntry.categoryId = cooldownsResult.<Integer>Read(3);
                    cooldownEntry.categoryEnd = time.UnixTimeToDateTime(cooldownsResult.<Long>Read(4));
                }

                spellCooldowns.set(cooldownEntry.spellId, cooldownEntry);

                if (cooldownEntry.categoryId != 0) {
                    categoryCooldowns.set(cooldownEntry.categoryId, spellCooldowns.get(cooldownEntry.spellId));
                }
            } while (cooldownsResult.NextRow());
        }

        if (!chargesResult.isEmpty()) {
            do {
                var categoryId = chargesResult.<Integer>Read(0);

                if (!CliDB.SpellCategoryStorage.containsKey(categoryId)) {
                    continue;
                }

                ChargeEntry charges = new ChargeEntry();
                charges.rechargeStart = time.UnixTimeToDateTime(chargesResult.<Long>Read(1));
                charges.rechargeEnd = time.UnixTimeToDateTime(chargesResult.<Long>Read(2));
                categoryCharges.add(categoryId, charges);
            } while (chargesResult.NextRow());
        }
    }

    public final <T extends WorldObject> void saveToDb(SQLTransaction trans) {
        PreparedStatement stmt;

        if (T.class == pet.class) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_PET_SPELL_COOLDOWNS);
            stmt.AddValue(0, owner.getCharmInfo().getPetNumber());
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_PET_SPELL_CHARGES);
            stmt.AddValue(0, owner.getCharmInfo().getPetNumber());
            trans.append(stmt);

            byte index;

            for (var pair : spellCooldowns) {
                if (!pair.value.onHold) {
                    index = 0;
                    stmt = DB.characters.GetPreparedStatement(CharStatements.INS_PET_SPELL_COOLDOWN);
                    stmt.AddValue(index++, owner.getCharmInfo().getPetNumber());
                    stmt.AddValue(index++, pair.key);
                    stmt.AddValue(index++, time.DateTimeToUnixTime(pair.value.cooldownEnd));
                    stmt.AddValue(index++, pair.value.categoryId);
                    stmt.AddValue(index++, time.DateTimeToUnixTime(pair.value.categoryEnd));
                    trans.append(stmt);
                }
            }

            for (var pair : categoryCharges.KeyValueList) {
                index = 0;
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_PET_SPELL_CHARGES);
                stmt.AddValue(index++, owner.getCharmInfo().getPetNumber());
                stmt.AddValue(index++, pair.key);
                stmt.AddValue(index++, time.DateTimeToUnixTime(pair.value.rechargeStart));
                stmt.AddValue(index++, time.DateTimeToUnixTime(pair.value.rechargeEnd));
                trans.append(stmt);
            }
        } else {
            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_SPELL_COOLDOWNS);
            stmt.AddValue(0, owner.getGUID().getCounter());
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_SPELL_CHARGES);
            stmt.AddValue(0, owner.getGUID().getCounter());
            trans.append(stmt);

            byte index;

            for (var pair : spellCooldowns) {
                if (!pair.value.onHold) {
                    index = 0;
                    stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_SPELL_COOLDOWN);
                    stmt.AddValue(index++, owner.getGUID().getCounter());
                    stmt.AddValue(index++, pair.key);
                    stmt.AddValue(index++, pair.value.itemId);
                    stmt.AddValue(index++, time.DateTimeToUnixTime(pair.value.cooldownEnd));
                    stmt.AddValue(index++, pair.value.categoryId);
                    stmt.AddValue(index++, time.DateTimeToUnixTime(pair.value.categoryEnd));
                    trans.append(stmt);
                }
            }

            for (var pair : categoryCharges.KeyValueList) {
                index = 0;
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_SPELL_CHARGES);
                stmt.AddValue(index++, owner.getGUID().getCounter());
                stmt.AddValue(index++, pair.key);
                stmt.AddValue(index++, time.DateTimeToUnixTime(pair.value.rechargeStart));
                stmt.AddValue(index++, time.DateTimeToUnixTime(pair.value.rechargeEnd));
                trans.append(stmt);
            }
        }
    }

    public final void update() {
        var now = GameTime.getSystemTime();

        for (var pair : categoryCooldowns) {
            if (pair.value.categoryEnd < now) {
                categoryCooldowns.QueueRemove(pair.key);
            }
        }

        categoryCooldowns.ExecuteRemove();


        for (var pair : spellCooldowns) {
            if (pair.value.cooldownEnd < now) {
                categoryCooldowns.remove(pair.value.categoryId);
                spellCooldowns.QueueRemove(pair.key);
            }
        }

        spellCooldowns.ExecuteRemove();

        categoryCharges.RemoveIfMatching((pair) -> pair.value.rechargeEnd <= now);
    }


    public final void handleCooldowns(SpellInfo spellInfo, Item item) {
        handleCooldowns(spellInfo, item, null);
    }

    public final void handleCooldowns(SpellInfo spellInfo, Item item, Spell spell) {
        handleCooldowns(spellInfo, item ? item.getEntry() : 0, spell);
    }


    public final void handleCooldowns(SpellInfo spellInfo, int itemId) {
        handleCooldowns(spellInfo, itemId, null);
    }

    public final void handleCooldowns(SpellInfo spellInfo, int itemId, Spell spell) {
        if (spell != null && spell.isIgnoringCooldowns()) {
            return;
        }

        if (consumeCharge(spellInfo.chargeCategoryId)) {
            return;
        }

        var player = owner.toPlayer();

        if (player) {
            // potions start cooldown until exiting combat
            var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

            if (itemTemplate != null) {
                if (itemTemplate.isPotion() || spellInfo.isCooldownStartedOnEvent()) {
                    player.setLastPotionId(itemId);

                    return;
                }
            }
        }

        if (spellInfo.isCooldownStartedOnEvent() || spellInfo.isPassive()) {
            return;
        }

        startCooldown(spellInfo, itemId, spell);
    }


    public final boolean isReady(SpellInfo spellInfo) {
        return isReady(spellInfo, 0);
    }

    public final boolean isReady(SpellInfo spellInfo, int itemId) {
        if (spellInfo.getPreventionType().hasFlag(SpellPreventionType.Silence)) {
            if (isSchoolLocked(spellInfo.getSchoolMask())) {
                return false;
            }
        }

        if (hasCooldown(spellInfo, itemId)) {
            return false;
        }

        if (!hasCharge(spellInfo.chargeCategoryId)) {
            return false;
        }

        return true;
    }

    public final void writePacket(SendSpellHistory sendSpellHistory) {
        var now = GameTime.getSystemTime();

        for (var p : spellCooldowns) {
            SpellHistoryEntry historyEntry = new SpellHistoryEntry();
            historyEntry.spellID = p.key;
            historyEntry.itemID = p.value.itemId;

            if (p.value.onHold) {
                historyEntry.onHold = true;
            } else {
                var cooldownDuration = p.value.CooldownEnd - now;

                if (cooldownDuration.TotalMilliseconds <= 0) {
                    continue;
                }

                var categoryDuration = p.value.CategoryEnd - now;

                if (categoryDuration.TotalMilliseconds > 0) {
                    historyEntry.category = p.value.categoryId;
                    historyEntry.categoryRecoveryTime = (int) categoryDuration.TotalMilliseconds;
                }

                if (cooldownDuration > categoryDuration) {
                    historyEntry.recoveryTime = (int) cooldownDuration.TotalMilliseconds;
                }
            }

            sendSpellHistory.entries.add(historyEntry);
        }
    }

    public final void writePacket(SendSpellCharges sendSpellCharges) {
        var now = GameTime.getSystemTime();

        for (var key : categoryCharges.keySet()) {
            var list = categoryCharges.get(key);

            if (!list.isEmpty()) {
                var cooldownDuration = list.FirstOrDefault().RechargeEnd - now;

                if (cooldownDuration.TotalMilliseconds <= 0) {
                    continue;
                }

                SpellChargeEntry chargeEntry = new SpellChargeEntry();
                chargeEntry.category = key;
                chargeEntry.nextRecoveryTime = (int) cooldownDuration.TotalMilliseconds;
                chargeEntry.consumedCharges = (byte) list.count;
                sendSpellCharges.entries.add(chargeEntry);
            }
        }
    }

    public final void writePacket(PetSpells petSpells) {
        var now = GameTime.getSystemTime();

        for (var pair : spellCooldowns) {
            PetSpellCooldown petSpellCooldown = new PetSpellCooldown();
            petSpellCooldown.spellID = pair.key;
            petSpellCooldown.category = (short) pair.value.categoryId;

            if (!pair.value.onHold) {
                var cooldownDuration = pair.value.CooldownEnd - now;

                if (cooldownDuration.TotalMilliseconds <= 0) {
                    continue;
                }

                petSpellCooldown.duration = (int) cooldownDuration.TotalMilliseconds;
                var categoryDuration = pair.value.CategoryEnd - now;

                if (categoryDuration.TotalMilliseconds > 0) {
                    petSpellCooldown.categoryDuration = (int) categoryDuration.TotalMilliseconds;
                }
            } else {
                petSpellCooldown.categoryDuration = (int) 0x80000000;
            }

            petSpells.cooldowns.add(petSpellCooldown);
        }

        for (var key : categoryCharges.keySet()) {
            var list = categoryCharges.get(key);

            if (!list.isEmpty()) {
                var cooldownDuration = list.FirstOrDefault().RechargeEnd - now;

                if (cooldownDuration.TotalMilliseconds <= 0) {
                    continue;
                }

                PetSpellHistory petChargeEntry = new PetSpellHistory();
                petChargeEntry.categoryID = key;
                petChargeEntry.recoveryTime = (int) cooldownDuration.TotalMilliseconds;
                petChargeEntry.consumedCharges = (byte) list.count;

                petSpells.spellHistory.add(petChargeEntry);
            }
        }
    }


    public final void startCooldown(SpellInfo spellInfo, int itemId, Spell spell, boolean onHold) {
        startCooldown(spellInfo, itemId, spell, onHold, null);
    }

    public final void startCooldown(SpellInfo spellInfo, int itemId, Spell spell) {
        startCooldown(spellInfo, itemId, spell, false, null);
    }

    public final void startCooldown(SpellInfo spellInfo, int itemId) {
        startCooldown(spellInfo, itemId, null, false, null);
    }

    public final void startCooldown(SpellInfo spellInfo, int itemId, Spell spell, boolean onHold, Duration forcedCooldown) {
        // init cooldown values
        int categoryId = 0;
        var cooldown = duration.Zero;
        var categoryCooldown = duration.Zero;

        var curTime = GameTime.getSystemTime();
        LocalDateTime catrecTime = LocalDateTime.MIN;
        LocalDateTime recTime = LocalDateTime.MIN;
        var needsCooldownPacket = false;

        if (!forcedCooldown != null) {
            tangible.RefObject<duration> tempRef_cooldown = new tangible.RefObject<duration>(cooldown);
            tangible.RefObject<Integer> tempRef_categoryId = new tangible.RefObject<Integer>(categoryId);
            tangible.RefObject<duration> tempRef_categoryCooldown = new tangible.RefObject<duration>(categoryCooldown);
            getCooldownDurations(spellInfo, itemId, tempRef_cooldown, tempRef_categoryId, tempRef_categoryCooldown);
            categoryCooldown = tempRef_categoryCooldown.refArgValue;
            categoryId = tempRef_categoryId.refArgValue;
            cooldown = tempRef_cooldown.refArgValue;
        } else {
            cooldown = forcedCooldown.getValue();
        }

        // overwrite time for selected category
        if (onHold) {
            // use +MONTH as infinite cooldown marker
            catrecTime = categoryCooldown > duration.Zero ? (curTime + PlayerConst.InfinityCooldownDelay) : curTime;
            recTime = cooldown > duration.Zero ? (curTime + PlayerConst.InfinityCooldownDelay) : catrecTime;
        } else {
            if (!forcedCooldown != null) {
                // Now we have cooldown data (if found any), time to apply mods
                var modOwner = owner.getSpellModOwner();

                if (modOwner) {

//					void applySpellMod(ref Duration second)
//						{
//							var intValue = (int)second.TotalMilliseconds;
//							modOwner.applySpellMod(spellInfo, SpellModOp.cooldown, ref intValue, spell);
//							second = duration.ofSeconds(intValue);
//						}

                    if (cooldown >= duration.Zero) {
                        tangible.RefObject<system.duration> tempRef_cooldown2 = new tangible.RefObject<system.duration>(cooldown);
                        applySpellMod(tempRef_cooldown2);
                        cooldown = tempRef_cooldown2.refArgValue;
                    }

                    if (categoryCooldown >= duration.Zero && !spellInfo.hasAttribute(SpellAttr6.NoCategoryCooldownMods)) {
                        tangible.RefObject<system.duration> tempRef_categoryCooldown2 = new tangible.RefObject<system.duration>(categoryCooldown);
                        applySpellMod(tempRef_categoryCooldown2);
                        categoryCooldown = tempRef_categoryCooldown2.refArgValue;
                    }
                }

                if (owner.hasAuraTypeWithAffectMask(AuraType.ModSpellCooldownByHaste, spellInfo)) {
                    cooldown = duration.ofSeconds(cooldown.TotalMilliseconds * owner.getUnitData().modSpellHaste);
                    categoryCooldown = duration.ofSeconds(categoryCooldown.TotalMilliseconds * owner.getUnitData().modSpellHaste);
                }

                if (owner.hasAuraTypeWithAffectMask(AuraType.ModCooldownByHasteRegen, spellInfo)) {
                    cooldown = duration.ofSeconds(cooldown.TotalMilliseconds * owner.getUnitData().modHasteRegen);
                    categoryCooldown = duration.ofSeconds(categoryCooldown.TotalMilliseconds * owner.getUnitData().modHasteRegen);
                }

                var cooldownMod = owner.getTotalAuraModifier(AuraType.modCooldown);

                if (cooldownMod != 0) {
                    // Apply SPELL_AURA_MOD_COOLDOWN only to own spells
                    var playerOwner = getPlayerOwner();

                    if (!playerOwner || playerOwner.hasSpell(spellInfo.getId())) {
                        needsCooldownPacket = true;
                        cooldown += duration.ofSeconds(cooldownMod); // SPELL_AURA_MOD_COOLDOWN does not affect category cooldows, verified with shaman shocks
                    }
                }

                // Apply SPELL_AURA_MOD_SPELL_CATEGORY_COOLDOWN modifiers
                // Note: This aura applies its modifiers to all cooldowns of spells with set category, not to category cooldown only
                if (categoryId != 0) {
                    var categoryModifier = owner.getTotalAuraModifierByMiscValue(AuraType.ModSpellCategoryCooldown, (int) categoryId);

                    if (categoryModifier != 0) {
                        if (cooldown > duration.Zero) {
                            cooldown += duration.ofSeconds(categoryModifier);
                        }

                        if (categoryCooldown > duration.Zero) {
                            categoryCooldown += duration.ofSeconds(categoryModifier);
                        }
                    }

                    var categoryEntry = CliDB.SpellCategoryStorage.get(categoryId);

                    if (categoryEntry.flags.hasFlag(SpellCategoryFlags.CooldownExpiresAtDailyReset)) {
                        categoryCooldown = time.UnixTimeToDateTime(global.getWorldMgr().getNextDailyQuestsResetTime()) - GameTime.getSystemTime();
                    }
                }
            } else {
                needsCooldownPacket = true;
            }

            // replace negative cooldowns by 0
            if (cooldown < duration.Zero) {
                cooldown = duration.Zero;
            }

            if (categoryCooldown < duration.Zero) {
                categoryCooldown = duration.Zero;
            }

            // no cooldown after applying spell mods
            if (system.duration.opEquals(cooldown, duration.Zero) && system.duration.opEquals(categoryCooldown, duration.Zero)) {
                return;
            }

            catrecTime = system.duration.opNotEquals(categoryCooldown, duration.Zero) ? curTime + categoryCooldown : curTime;
            recTime = system.duration.opNotEquals(cooldown, duration.Zero) ? curTime + cooldown : catrecTime;
        }

        // self spell cooldown
        if (!curTime.equals(recTime)) {
            var playerOwner = getPlayerOwner();

            if (playerOwner) {
                tangible.RefObject<LocalDateTime> tempRef_recTime = new tangible.RefObject<LocalDateTime>(recTime);
                tangible.RefObject<LocalDateTime> tempRef_catrecTime = new tangible.RefObject<LocalDateTime>(catrecTime);
                tangible.RefObject<Boolean> tempRef_onHold = new tangible.RefObject<Boolean>(onHold);
                global.getScriptMgr().<IPlayerOnCooldownStart>ForEach(playerOwner.getUnitClass(), c -> c.OnCooldownStart(playerOwner, spellInfo, itemId, categoryId, cooldown, tempRef_recTime, tempRef_catrecTime, tempRef_onHold));
                onHold = tempRef_onHold.refArgValue;
                catrecTime = tempRef_catrecTime.refArgValue;
                recTime = tempRef_recTime.refArgValue;
            }

            addCooldown(spellInfo.getId(), itemId, recTime, categoryId, catrecTime, onHold);

            if (playerOwner) {
                if (needsCooldownPacket) {
                    SpellCooldownPkt spellCooldown = new SpellCooldownPkt();
                    spellCooldown.caster = owner.getGUID();
                    spellCooldown.flags = SpellCooldownFlags.NONE;
                    spellCooldown.spellCooldowns.add(new SpellCooldownStruct(spellInfo.getId(), (int) cooldown.TotalMilliseconds));
                    playerOwner.sendPacket(spellCooldown);
                }
            }
        }
    }


    public final void sendCooldownEvent(SpellInfo spellInfo, int itemId, Spell spell) {
        sendCooldownEvent(spellInfo, itemId, spell, true);
    }

    public final void sendCooldownEvent(SpellInfo spellInfo, int itemId) {
        sendCooldownEvent(spellInfo, itemId, null, true);
    }

    public final void sendCooldownEvent(SpellInfo spellInfo) {
        sendCooldownEvent(spellInfo, 0, null, true);
    }

    public final void sendCooldownEvent(SpellInfo spellInfo, int itemId, Spell spell, boolean startCooldown) {
        var player = getPlayerOwner();

        if (player) {
            var category = spellInfo.getCategoryId();
            tangible.RefObject<Integer> tempRef_category = new tangible.RefObject<Integer>(category);
            getCooldownDurations(spellInfo, itemId, tempRef_category);
            category = tempRef_category.refArgValue;

            var categoryEntry = categoryCooldowns.get(category);

            if (categoryEntry != null && categoryEntry.spellId != spellInfo.getId()) {
                player.sendPacket(new CooldownEvent(player != owner, categoryEntry.spellId));

                if (startCooldown) {
                    startCooldown(global.getSpellMgr().getSpellInfo(categoryEntry.spellId, owner.getMap().getDifficultyID()), itemId, spell);
                }
            }

            player.sendPacket(new CooldownEvent(player != owner, spellInfo.getId()));
        }

        // start cooldowns at server side, if any
        if (startCooldown) {
            startCooldown(spellInfo, itemId, spell);
        }
    }

    
    public final <T extends Enum> void addCooldown(T spellId, int itemId, Duration cooldownDuration) {
        addCooldown((int) spellId, itemId, cooldownDuration);
    }

    public final void addCooldown(int spellId, int itemId, Duration cooldownDuration) {
        var now = GameTime.getSystemTime();
        addCooldown(spellId, itemId, now + cooldownDuration, 0, now);
    }


    public final void addCooldown(int spellId, int itemId, LocalDateTime cooldownEnd, int categoryId, LocalDateTime categoryEnd) {
        addCooldown(spellId, itemId, cooldownEnd, categoryId, categoryEnd, false);
    }

    public final void addCooldown(int spellId, int itemId, LocalDateTime cooldownEnd, int categoryId, LocalDateTime categoryEnd, boolean onHold) {
        CooldownEntry cooldownEntry = new CooldownEntry();

        // scripts can start multiple cooldowns for a given spell, only store the longest one
        if (cooldownEnd.compareTo(cooldownEntry.cooldownEnd) > 0 || categoryEnd.compareTo(cooldownEntry.categoryEnd) > 0 || onHold) {
            cooldownEntry.spellId = spellId;
            cooldownEntry.cooldownEnd = cooldownEnd;
            cooldownEntry.itemId = itemId;
            cooldownEntry.categoryId = categoryId;
            cooldownEntry.categoryEnd = categoryEnd;
            cooldownEntry.onHold = onHold;
            spellCooldowns.set(spellId, cooldownEntry);

            if (categoryId != 0) {
                categoryCooldowns.set(categoryId, cooldownEntry);
            }
        }
    }

    public final void modifySpellCooldown(int spellId, Duration cooldownMod, boolean withoutCategoryCooldown) {
        var cooldownEntry = spellCooldowns.get(spellId);

        if (cooldownMod.TotalMilliseconds == 0 || cooldownEntry == null) {
            return;
        }

        modifySpellCooldown(cooldownEntry, cooldownMod, withoutCategoryCooldown);
    }


    public final <T extends Enum> void modifyCooldown(T spellId, Duration cooldownMod) {
        modifyCooldown(spellId, cooldownMod, false);
    }

    
    public final <T extends Enum> void modifyCooldown(T spellId, Duration cooldownMod, boolean withoutCategoryCooldown) {
        modifyCooldown((int) spellId, cooldownMod, withoutCategoryCooldown);
    }


    public final void modifyCooldown(int spellId, Duration cooldownMod) {
        modifyCooldown(spellId, cooldownMod, false);
    }

    public final void modifyCooldown(int spellId, Duration cooldownMod, boolean withoutCategoryCooldown) {
        var spellInfo = global.getSpellMgr().getSpellInfo(spellId, owner.getMap().getDifficultyID());

        if (spellInfo != null) {
            modifyCooldown(spellInfo, cooldownMod, withoutCategoryCooldown);
        }
    }


    public final void modifyCooldown(SpellInfo spellInfo, Duration cooldownMod) {
        modifyCooldown(spellInfo, cooldownMod, false);
    }

    public final void modifyCooldown(SpellInfo spellInfo, Duration cooldownMod, boolean withoutCategoryCooldown) {
        if (duration.opEquals(cooldownMod, duration.Zero)) {
            return;
        }

        if (getChargeRecoveryTime(spellInfo.chargeCategoryId) > 0 && getMaxCharges(spellInfo.chargeCategoryId) > 0) {
            modifyChargeRecoveryTime(spellInfo.chargeCategoryId, cooldownMod);
        } else {
            modifySpellCooldown(spellInfo.getId(), cooldownMod, withoutCategoryCooldown);
        }
    }


    public final void modifyCoooldowns(Func<CooldownEntry, Boolean> predicate, Duration cooldownMod) {
        modifyCoooldowns(predicate, cooldownMod, false);
    }

    public final void modifyCoooldowns(tangible.Func1Param<CooldownEntry, Boolean> predicate, Duration cooldownMod, boolean withoutCategoryCooldown) {
        for (var cooldownEntry : spellCooldowns.VALUES.ToList()) {
            if (predicate.invoke(cooldownEntry)) {
                modifySpellCooldown(cooldownEntry, cooldownMod, withoutCategoryCooldown);
            }
        }
    }


    public final void resetCooldown(int spellId) {
        resetCooldown(spellId, false);
    }

    public final void resetCooldown(int spellId, boolean update) {
        var entry = spellCooldowns.get(spellId);

        if (entry == null) {
            return;
        }

        if (update) {
            var playerOwner = getPlayerOwner();

            if (playerOwner) {
                ClearCooldown clearCooldown = new ClearCooldown();
                clearCooldown.isPet = owner != playerOwner;
                clearCooldown.spellID = spellId;
                clearCooldown.clearOnHold = false;
                playerOwner.sendPacket(clearCooldown);
            }
        }

        categoryCooldowns.remove(entry.categoryId);
        spellCooldowns.remove(spellId);
    }


    public final void resetCooldowns(Func<Map.entry<Integer, CooldownEntry>, Boolean> predicate) {
        resetCooldowns(predicate, false);
    }

    public final void resetCooldowns(tangible.Func1Param<Map.entry<Integer, CooldownEntry>, Boolean> predicate, boolean update) {
        ArrayList<Integer> resetCooldowns = new ArrayList<>();

        for (var pair : spellCooldowns) {
            if (predicate.invoke(pair)) {
                resetCooldowns.add(pair.key);
                resetCooldown(pair.key);
            }
        }

        if (update && !resetCooldowns.isEmpty()) {
            sendClearCooldowns(resetCooldowns);
        }
    }

    public final void resetAllCooldowns() {
        var playerOwner = getPlayerOwner();

        if (playerOwner) {
            ArrayList<Integer> cooldowns = new ArrayList<>();

            for (var id : spellCooldowns.keySet()) {
                cooldowns.add(id);
            }

            sendClearCooldowns(cooldowns);
        }

        categoryCooldowns.clear();
        spellCooldowns.clear();
    }


    public final boolean hasCooldown(int spellId) {
        return hasCooldown(spellId, 0);
    }

    public final boolean hasCooldown(int spellId, int itemId) {
        return hasCooldown(global.getSpellMgr().getSpellInfo(spellId, owner.getMap().getDifficultyID()), itemId);
    }


    public final boolean hasCooldown(SpellInfo spellInfo) {
        return hasCooldown(spellInfo, 0);
    }

    public final boolean hasCooldown(SpellInfo spellInfo, int itemId) {
        if (spellCooldowns.ContainsKey(spellInfo.getId())) {
            return true;
        }

        if (spellInfo.getCooldownAuraSpellId() != 0 && owner.hasAura(spellInfo.getCooldownAuraSpellId())) {
            return true;
        }

        int category = 0;
        tangible.RefObject<Integer> tempRef_category = new tangible.RefObject<Integer>(category);
        getCooldownDurations(spellInfo, itemId, tempRef_category);
        category = tempRef_category.refArgValue;

        if (category == 0) {
            category = spellInfo.getCategoryId();
        }

        if (category == 0) {
            return false;
        }

        return categoryCooldowns.ContainsKey(category);
    }

    public final Duration getRemainingCooldown(SpellInfo spellInfo) {
        LocalDateTime end = LocalDateTime.MIN;
        var entry = spellCooldowns.get(spellInfo.getId());

        if (entry != null) {
            end = entry.cooldownEnd;
        } else {
            var cooldownEntry = categoryCooldowns.get(spellInfo.getCategoryId());

            if (cooldownEntry == null) {
                return duration.Zero;
            }

            end = cooldownEntry.categoryEnd;
        }

        var now = GameTime.getSystemTime();

        if (end.compareTo(now) < 0) {
            return duration.Zero;
        }

        var remaining = end - now;

        return remaining;
    }

    public final Duration getRemainingCategoryCooldown(int categoryId) {
        LocalDateTime end = LocalDateTime.MIN;
        var cooldownEntry = categoryCooldowns.get(categoryId);

        if (cooldownEntry == null) {
            return duration.Zero;
        }

        end = cooldownEntry.categoryEnd;

        var now = GameTime.getSystemTime();

        if (end.compareTo(now) < 0) {
            return duration.Zero;
        }

        var remaining = end - now;

        return remaining;
    }

    public final Duration getRemainingCategoryCooldown(SpellInfo spellInfo) {
        return getRemainingCategoryCooldown(spellInfo.getCategoryId());
    }

    public final void lockSpellSchool(SpellSchoolMask schoolMask, Duration lockoutTime) {
        var now = GameTime.getSystemTime();
        var lockoutEnd = now + lockoutTime;

        for (var i = 0; i < SpellSchool.max.getValue(); ++i) {
            if ((boolean) (spellSchoolMask.forValue(1 << i).getValue() & schoolMask.getValue())) {
                _schoolLockouts[i] = lockoutEnd;
            }
        }

        ArrayList<Integer> knownSpells = new ArrayList<>();
        var plrOwner = owner.toPlayer();

        if (plrOwner) {
            for (var p : plrOwner.getSpellMap().entrySet()) {
                if (p.getValue().state != PlayerSpellState.removed) {
                    knownSpells.add(p.getKey());
                }
            }
        } else if (owner.isPet()) {
            var petOwner = owner.toPet();

            for (var p : petOwner.spells.entrySet()) {
                if (p.getValue().state != PetSpellState.removed) {
                    knownSpells.add(p.getKey());
                }
            }
        } else {
            var creatureOwner = owner.toCreature();

            for (byte i = 0; i < SharedConst.MaxCreatureSpells; ++i) {
                if (creatureOwner.getSpells()[i] != 0) {
                    knownSpells.add(creatureOwner.getSpells()[i]);
                }
            }
        }

        SpellCooldownPkt spellCooldown = new SpellCooldownPkt();
        spellCooldown.caster = owner.getGUID();
        spellCooldown.flags = SpellCooldownFlags.LossOfControlUi;

        for (var spellId : knownSpells) {
            var spellInfo = global.getSpellMgr().getSpellInfo(spellId, owner.getMap().getDifficultyID());

            if (spellInfo.isCooldownStartedOnEvent()) {
                continue;
            }

            if (!spellInfo.getPreventionType().hasFlag(SpellPreventionType.Silence)) {
                continue;
            }

            if ((schoolMask.getValue() & spellInfo.getSchoolMask().getValue()) == 0) {
                continue;
            }

            if (getRemainingCooldown(spellInfo) < lockoutTime) {
                addCooldown(spellId, 0, lockoutEnd, 0, now);
            }

            // always send cooldown, even if it will be shorter than already existing cooldown for LossOfControl UI
            spellCooldown.spellCooldowns.add(new SpellCooldownStruct(spellId, (int) lockoutTime.TotalMilliseconds));
        }

        var player = getPlayerOwner();

        if (player) {
            if (!spellCooldown.spellCooldowns.isEmpty()) {
                player.sendPacket(spellCooldown);
            }
        }
    }

    public final boolean isSchoolLocked(SpellSchoolMask schoolMask) {
        var now = GameTime.getSystemTime();

        for (var i = 0; i < SpellSchool.max.getValue(); ++i) {
            if ((boolean) (spellSchoolMask.forValue(1 << i).getValue() & schoolMask.getValue())) {
                if (_schoolLockouts[i].compareTo(now) > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public final boolean consumeCharge(int chargeCategoryId) {
        if (!CliDB.SpellCategoryStorage.containsKey(chargeCategoryId)) {
            return false;
        }

        var chargeRecovery = getChargeRecoveryTime(chargeCategoryId);

        if (chargeRecovery > 0 && getMaxCharges(chargeCategoryId) > 0) {
            LocalDateTime recoveryStart = LocalDateTime.MIN;
            var charges = categoryCharges.get(chargeCategoryId);

            if (charges.isEmpty()) {
                recoveryStart = GameTime.getSystemTime();
            } else {
                recoveryStart = charges.get(charges.size() - 1).rechargeEnd;
            }

            var p = getPlayerOwner();

            if (p != null) {
                tangible.RefObject<Integer> tempRef_chargeRecovery = new tangible.RefObject<Integer>(chargeRecovery);
                global.getScriptMgr().<IPlayerOnChargeRecoveryTimeStart>ForEach(p.getUnitClass(), c -> c.OnChargeRecoveryTimeStart(p, chargeCategoryId, tempRef_chargeRecovery));
                chargeRecovery = tempRef_chargeRecovery.refArgValue;
            }

            categoryCharges.add(chargeCategoryId, new ChargeEntry(recoveryStart, duration.ofSeconds(chargeRecovery)));

            return true;
        }

        return false;
    }

    public final void restoreCharge(int chargeCategoryId) {
        var chargeList = categoryCharges.get(chargeCategoryId);

        if (!chargeList.isEmpty()) {
            chargeList.remove(chargeList.size() - 1);

            sendSetSpellCharges(chargeCategoryId, chargeList);

            if (chargeList.isEmpty()) {
                categoryCharges.remove(chargeCategoryId);
            }
        }
    }

    public final void resetCharges(int chargeCategoryId) {
        var chargeList = categoryCharges.get(chargeCategoryId);

        if (!chargeList.isEmpty()) {
            categoryCharges.remove(chargeCategoryId);

            var player = getPlayerOwner();

            if (player) {
                ClearSpellCharges clearSpellCharges = new ClearSpellCharges();
                clearSpellCharges.isPet = owner != player;
                clearSpellCharges.category = chargeCategoryId;
                player.sendPacket(clearSpellCharges);
            }
        }
    }

    public final void resetAllCharges() {
        categoryCharges.clear();

        var player = getPlayerOwner();

        if (player) {
            ClearAllSpellCharges clearAllSpellCharges = new ClearAllSpellCharges();
            clearAllSpellCharges.isPet = owner != player;
            player.sendPacket(clearAllSpellCharges);
        }
    }

    public final boolean hasCharge(int chargeCategoryId) {
        if (!CliDB.SpellCategoryStorage.containsKey(chargeCategoryId)) {
            return true;
        }

        // Check if the spell is currently using charges (untalented warlock Dark Soul)
        var maxCharges = getMaxCharges(chargeCategoryId);

        if (maxCharges <= 0) {
            return true;
        }

        var chargeList = categoryCharges.get(chargeCategoryId);

        return chargeList.isEmpty() || chargeList.size() < maxCharges;
    }

    public final int getMaxCharges(int chargeCategoryId) {
        var chargeCategoryEntry = CliDB.SpellCategoryStorage.get(chargeCategoryId);

        if (chargeCategoryEntry == null) {
            return 0;
        }

        int charges = chargeCategoryEntry.maxCharges;
        charges += (int) owner.getTotalAuraModifierByMiscValue(AuraType.ModMaxCharges, (int) chargeCategoryId);

        return (int) charges;
    }

    public final int getChargeRecoveryTime(int chargeCategoryId) {
        var chargeCategoryEntry = CliDB.SpellCategoryStorage.get(chargeCategoryId);

        if (chargeCategoryEntry == null) {
            return 0;
        }

        double recoveryTime = chargeCategoryEntry.ChargeRecoveryTime;
        recoveryTime += owner.getTotalAuraModifierByMiscValue(AuraType.ChargeRecoveryMod, (int) chargeCategoryId);

        var recoveryTimeF = recoveryTime;
        recoveryTimeF *= owner.getTotalAuraMultiplierByMiscValue(AuraType.ChargeRecoveryMultiplier, (int) chargeCategoryId);

        if (owner.hasAuraType(AuraType.ChargeRecoveryAffectedByHaste)) {
            recoveryTimeF *= owner.getUnitData().modSpellHaste;
        }

        if (owner.hasAuraType(AuraType.ChargeRecoveryAffectedByHasteRegen)) {
            recoveryTimeF *= owner.getUnitData().modHasteRegen;
        }

        return (int) Math.floor(recoveryTimeF);
    }

    public final boolean hasGlobalCooldown(SpellInfo spellInfo) {
        return globalCooldowns.containsKey(spellInfo.getStartRecoveryCategory()) && globalCooldowns.get(spellInfo.getStartRecoveryCategory()).compareTo(GameTime.getSystemTime()) > 0;
    }

    public final void addGlobalCooldown(SpellInfo spellInfo, Duration durationMs) {
        globalCooldowns.put(spellInfo.getStartRecoveryCategory(), GameTime.getSystemTime() + durationMs);
    }

    public final void cancelGlobalCooldown(SpellInfo spellInfo) {
        globalCooldowns.put(spellInfo.getStartRecoveryCategory(), LocalDateTime.MIN);
    }

    public final Player getPlayerOwner() {
        return owner.getCharmerOrOwnerPlayerOrPlayerItself();
    }

    public final void sendClearCooldowns(ArrayList<Integer> cooldowns) {
        var playerOwner = getPlayerOwner();

        if (playerOwner) {
            ClearCooldowns clearCooldowns = new ClearCooldowns();
            clearCooldowns.isPet = owner != playerOwner;
            clearCooldowns.spellID = cooldowns;
            playerOwner.sendPacket(clearCooldowns);
        }
    }

    public final void saveCooldownStateBeforeDuel() {
        spellCooldownsBeforeDuel = spellCooldowns;
    }

    public final void restoreCooldownStateAfterDuel() {
        var player = owner.toPlayer();

        if (player) {
            // add all profession CDs created while in duel (if any)
            for (var c : spellCooldowns) {
                var spellInfo = global.getSpellMgr().getSpellInfo(c.key);

                if (spellInfo.recoveryTime > 10 * time.Minute * time.InMilliseconds || spellInfo.categoryRecoveryTime > 10 * time.Minute * time.InMilliseconds) {
                    spellCooldownsBeforeDuel.put(c.key, spellCooldowns.get(c.key));
                }
            }

            // check for spell with onHold active before and during the duel
            for (var pair : spellCooldownsBeforeDuel.entrySet()) {
                if (!pair.getValue().onHold && spellCooldowns.ContainsKey(pair.getKey()) && !spellCooldowns.get(pair.getKey()).onHold) {
                    spellCooldowns.set(pair.getKey(), spellCooldownsBeforeDuel.get(pair.getKey()));
                }
            }

            // update the client: restore old cooldowns
            SpellCooldownPkt spellCooldown = new SpellCooldownPkt();
            spellCooldown.caster = owner.getGUID();
            spellCooldown.flags = SpellCooldownFlags.IncludeEventCooldowns;

            for (var c : spellCooldowns) {
                var now = GameTime.getSystemTime();
                var cooldownDuration = c.value.cooldownEnd > now ? (int) (c.value.CooldownEnd - now).TotalMilliseconds : 0;

                // cooldownDuration must be between 0 and 10 minutes in order to avoid any visual bugs
                if (cooldownDuration <= 0 || cooldownDuration > 10 * time.Minute * time.InMilliseconds || c.value.onHold) {
                    continue;
                }

                spellCooldown.spellCooldowns.add(new SpellCooldownStruct(c.key, cooldownDuration));
            }

            player.sendPacket(spellCooldown);
        }
    }

    public final void forceSendSpellCharge(SpellCategoryRecord chargeCategoryRecord) {
        var player = getPlayerOwner();

        if (player == null || categoryCharges.ContainsKey(chargeCategoryRecord.id)) {
            return;
        }

        var sendSpellCharges = new SendSpellCharges();
        var charges = categoryCharges.get(chargeCategoryRecord.id);

        var now = LocalDateTime.now();
        var cooldownDuration = (int) (charges.first().RechargeEnd - now).TotalMilliseconds;

        if (cooldownDuration <= 0) {
            return;
        }

        var chargeEntry = new SpellChargeEntry();
        chargeEntry.category = chargeCategoryRecord.id;
        chargeEntry.nextRecoveryTime = cooldownDuration;
        chargeEntry.consumedCharges = (byte) charges.count();
        sendSpellCharges.entries.add(chargeEntry);

        writePacket(sendSpellCharges);
    }

    private void modifySpellCooldown(CooldownEntry cooldownEntry, Duration cooldownMod, boolean withoutCategoryCooldown) {
        var now = GameTime.getSystemTime();

        cooldownEntry.cooldownEnd += cooldownMod;

        if (cooldownEntry.categoryId != 0) {
            if (!withoutCategoryCooldown) {
                cooldownEntry.categoryEnd += cooldownMod;
            }

            // Because category cooldown existence is tied to regular cooldown, we cannot allow a situation where regular cooldown is shorter than category
            if (cooldownEntry.cooldownEnd.compareTo(cooldownEntry.categoryEnd) < 0) {
                cooldownEntry.cooldownEnd = cooldownEntry.categoryEnd;
            }
        }

        var playerOwner = getPlayerOwner();

        if (playerOwner) {
            ModifyCooldown modifyCooldown = new modifyCooldown();
            modifyCooldown.isPet = owner != playerOwner;
            modifyCooldown.spellID = cooldownEntry.spellId;
            modifyCooldown.deltaTime = (int) cooldownMod.TotalMilliseconds;
            modifyCooldown.withoutCategoryCooldown = withoutCategoryCooldown;
            playerOwner.sendPacket(modifyCooldown);
        }

        if (cooldownEntry.cooldownEnd.compareTo(now) <= 0) {
            if (playerOwner) {
                global.getScriptMgr().<IPlayerOnCooldownEnd>ForEach(playerOwner.getUnitClass(), c -> c.OnCooldownEnd(playerOwner, global.getSpellMgr().getSpellInfo(cooldownEntry.spellId), cooldownEntry.itemId, cooldownEntry.categoryId));
            }

            categoryCooldowns.remove(cooldownEntry.categoryId);
            spellCooldowns.remove(cooldownEntry.spellId);
        }
    }

    private void modifyChargeRecoveryTime(int chargeCategoryId, Duration cooldownMod) {
        var chargeCategoryEntry = CliDB.SpellCategoryStorage.get(chargeCategoryId);

        if (chargeCategoryEntry == null) {
            return;
        }

        var chargeList = categoryCharges.get(chargeCategoryId);

        if (chargeList == null || chargeList.isEmpty()) {
            return;
        }

        var now = GameTime.getSystemTime();

        for (var i = 0; i < chargeList.size(); ++i) {
            var entry = chargeList[i];
            entry.rechargeStart += cooldownMod;
            entry.rechargeEnd += cooldownMod;
        }

        while (!chargeList.isEmpty() && chargeList[0].rechargeEnd.compareTo(now) < 0) {
            chargeList.remove(0);
        }

        sendSetSpellCharges(chargeCategoryId, chargeList);
    }

    private void sendSetSpellCharges(int chargeCategoryId, ArrayList<ChargeEntry> chargeCollection) {
        var player = getPlayerOwner();

        if (player != null) {
            SetSpellCharges setSpellCharges = new setSpellCharges();
            setSpellCharges.category = chargeCategoryId;

            if (!chargeCollection.isEmpty()) {
                setSpellCharges.nextRecoveryTime = (int) (chargeCollection.get(0).RechargeEnd - LocalDateTime.now()).TotalMilliseconds;
            }

            setSpellCharges.consumedCharges = (byte) chargeCollection.size();
            setSpellCharges.isPet = player != owner;
            player.sendPacket(setSpellCharges);
        }
    }

    private void getCooldownDurations(SpellInfo spellInfo, int itemId, tangible.RefObject<Integer> categoryId) {
        var notUsed = duration.Zero;
        tangible.RefObject<duration> tempRef_notUsed = new tangible.RefObject<duration>(notUsed);
        tangible.RefObject<duration> tempRef_notUsed2 = new tangible.RefObject<duration>(notUsed);
        getCooldownDurations(spellInfo, itemId, tempRef_notUsed, categoryId, tempRef_notUsed2);
        notUsed = tempRef_notUsed2.refArgValue;
        notUsed = tempRef_notUsed.refArgValue;
    }

    private void getCooldownDurations(SpellInfo spellInfo, int itemId, tangible.RefObject<duration> cooldown, tangible.RefObject<Integer> categoryId, tangible.RefObject<duration> categoryCooldown) {
        var tmpCooldown = duration.MinValue;
        int tmpCategoryId = 0;
        var tmpCategoryCooldown = duration.MinValue;

        // cooldown information stored in ItemEffect.db2, overriding normal cooldown and category
        if (itemId != 0) {
            var proto = global.getObjectMgr().getItemTemplate(itemId);

            if (proto != null) {
                for (var itemEffect : proto.getEffects()) {
                    if (itemEffect.spellID == spellInfo.getId()) {
                        tmpCooldown = duration.ofSeconds(itemEffect.CoolDownMSec);
                        tmpCategoryId = itemEffect.SpellCategoryID;
                        tmpCategoryCooldown = duration.ofSeconds(itemEffect.CategoryCoolDownMSec);

                        break;
                    }
                }
            }
        }

        // if no cooldown found above then base at DBC data
        if (tmpCooldown < duration.Zero && tmpCategoryCooldown < duration.Zero) {
            tmpCooldown = duration.ofSeconds(spellInfo.getRecoveryTime());
            tmpCategoryId = spellInfo.getCategoryId();
            tmpCategoryCooldown = duration.ofSeconds(spellInfo.getCategoryRecoveryTime());
        }

        cooldown.refArgValue = tmpCooldown;
        categoryId.refArgValue = tmpCategoryId;
        categoryCooldown.refArgValue = tmpCategoryCooldown;
    }

    public static class CooldownEntry {
        public int spellId;
        public LocalDateTime cooldownEnd = LocalDateTime.MIN;
        public int itemId;
        public int categoryId;
        public LocalDateTime categoryEnd = LocalDateTime.MIN;
        public boolean onHold;
    }

    public final static class ChargeEntry {
        public LocalDateTime rechargeStart = LocalDateTime.MIN;
        public LocalDateTime rechargeEnd = LocalDateTime.MIN;

        public ChargeEntry() {
        }
        public ChargeEntry(LocalDateTime startTime, Duration rechargeTime) {
            rechargeStart = startTime;
            rechargeEnd = startTime + rechargeTime;
        }

        public ChargeEntry clone() {
            ChargeEntry varCopy = new ChargeEntry();

            varCopy.rechargeStart = this.rechargeStart;
            varCopy.rechargeEnd = this.rechargeEnd;

            return varCopy;
        }
    }
}
