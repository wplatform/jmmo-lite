package com.github.azeroth.character.repository;

import com.github.azeroth.character.domain.*;
import com.github.azeroth.character.service.domain.*;
import com.github.azeroth.service.character.domain.*;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public interface CharacterRepository extends CrudRepository<Object, Long> {
    // Pool quest save operations
    @Modifying
    @Query("DELETE FROM pool_quest_save WHERE pool_id = :poolId AND quest_id = :questId")
    void deletePoolQuestSave(@Param("poolId") int poolId, @Param("questId") int questId);

    @Modifying
    @Query("INSERT INTO pool_quest_save (pool_id, quest_id) VALUES (:poolId, :questId)")
    void insertPoolQuestSave(@Param("poolId") int poolId, @Param("questId") int questId);

    // Guild bank item operations
    @Modifying
    @Query("DELETE FROM item_instance_modifiers WHERE itemGuid = :itemGuid")
    void deleteItemInstanceModifiers(@Param("itemGuid") long itemGuid);

    @Query("DELETE FROM item_instance_modifiers im LEFT JOIN item_instance ii ON im.itemGuid = ii.guid WHERE ii.owner_guid = :ownerGuid")
    void deleteItemInstanceModifiersByOwner(@Param("ownerGuid") long ownerGuid);

    @Query("INSERT INTO item_instance_modifiers (itemGuid, fixedScalingLevel, artifactKnowledgeLevel, itemReforgeId) VALUES (:itemGuid, :fixedScalingLevel, :artifactKnowledgeLevel, :itemReforgeId)")
    void insertItemInstanceModifiers(@Param("itemGuid") long itemGuid, @Param("fixedScalingLevel") int fixedScalingLevel, @Param("artifactKnowledgeLevel") int artifactKnowledgeLevel, @Param("itemReforgeId") int itemReforgeId);

    @Query("DELETE FROM guild_bank_item WHERE guildid = :guildId AND TabId = :tabId AND SlotId = :slotId")
    void deleteGuildBankItem(@Param("guildId") int guildId, @Param("tabId") int tabId, @Param("slotId") int slotId);

    @Modifying
    @Query("INSERT INTO auction_items (auctionId, itemGuid) VALUES (:auctionId, :itemGuid)")
    void insertAuctionItems(@Param("auctionId") long auctionId, @Param("itemGuid") long itemGuid);

    @Query("DELETE FROM auction_items WHERE itemGuid = :itemGuid")
    void deleteAuctionItemsByItem(@Param("itemGuid") long itemGuid);

    @Query("INSERT INTO guild_bank_item (guildid, TabId, SlotId, item_guid, count, enchanted, suffix, property_id, random_property_id, durability) VALUES (:guildId, :tabId, :slotId, :itemGuid, :count, :enchanted, :suffix, :propertyId, :randomPropertyId, :durability)")
    void insertGuildBankItem(@Param("guildId") int guildId, @Param("tabId") int tabId, @Param("slotId") int slotId, @Param("itemGuid") long itemGuid, @Param("count") int count, @Param("enchanted") int enchanted, @Param("suffix") int suffix, @Param("propertyId") int propertyId, @Param("randomPropertyId") int randomPropertyId, @Param("durability") int durability);

    // Character banned operations
    @Modifying
    @Query("DELETE FROM character_banned WHERE guid = :guid AND bandate = :bandate")
    void deleteCharacterBanned(@Param("guid") int guid, @Param("bandate") long bandate);

    @Modifying
    @Query("INSERT INTO character_banned (guid, bandate, unbandate, bannedby, bannedip, bannedaccount, reason, active) VALUES (:guid, :bandate, :unbandate, :bannedby, :bannedip, :bannedaccount, :reason, :active)")
    void insertCharacterBanned(@Param("guid") int guid, @Param("bandate") long bandate, @Param("unbandate") long unbandate, @Param("bannedby") String bannedby, @Param("bannedip") String bannedip, @Param("bannedaccount") String bannedaccount, @Param("reason") String reason, @Param("active") int active);

    // Character customization operations
    @Modifying
    @Query("UPDATE character_customizations SET customization = :customization WHERE guid = :guid")
    void updateCharacterCustomization(@Param("guid") int guid, @Param("customization") String customization);

    @Query("SELECT customization FROM character_customizations WHERE guid = :guid")
    String getCharacterCustomization(@Param("guid") int guid);

    // Item instance operations
    @Modifying
    @Query("DELETE FROM item_instance WHERE guid = :guid")
    void deleteItemInstance(@Param("guid") long guid);

    @Modifying
    @Query("INSERT INTO item_instance (guid, itemEntry, owner_guid, creatorGuid, giftCreatorGuid, count, duration, charges, flags, enchantments, randomPropertyId, durability, playedTime) VALUES (:guid, :itemEntry, :ownerGuid, :creatorGuid, :giftCreatorGuid, :count, :duration, :charges, :flags, :enchantments, :randomPropertyId, :durability, :playedTime)")
    void insertItemInstance(@Param("guid") long guid, @Param("itemEntry") int itemEntry, @Param("ownerGuid") int ownerGuid, @Param("creatorGuid") int creatorGuid, @Param("giftCreatorGuid") int giftCreatorGuid, @Param("count") int count, @Param("duration") int duration, @Param("charges") String charges, @Param("flags") int flags, @Param("enchantments") String enchantments, @Param("randomPropertyId") int randomPropertyId, @Param("durability") int durability, @Param("playedTime") int playedTime);

    // Mail operations
    @Modifying
    @Query("DELETE FROM mail WHERE id = :id")
    void deleteMail(@Param("id") int id);

    @Modifying
    @Query("INSERT INTO mail (id, messageType, stationery, sender, receiver, subject, body, has_items, expire_time, deliver_time, money) VALUES (:id, :messageType, :stationery, :sender, :receiver, :subject, :body, :hasItems, :expireTime, :deliverTime, :money)")
    void insertMail(@Param("id") int id, @Param("messageType") int messageType, @Param("stationery") int stationery, @Param("sender") int sender, @Param("receiver") int receiver, @Param("subject") String subject, @Param("body") String body, @Param("hasItems") int hasItems, @Param("expireTime") long expireTime, @Param("deliverTime") long deliverTime, @Param("money") long money);

    // Character pet operations
    @Modifying
    @Query("DELETE FROM character_pet WHERE id = :id")
    void deleteCharacterPet(@Param("id") int id);

    @Modifying
    @Query("INSERT INTO character_pet (id, entry, owner, modelid, level, exp, reactstate, name, renametime, slot, curhealth, curmana, curhappiness, loyaltypoints, trainpoint, spells) VALUES (:id, :entry, :owner, :modelid, :level, :exp, :reactstate, :name, :renametime, :slot, :curhealth, :curmana, :curhappiness, :loyaltypoints, :trainpoint, :spells)")
    void insertCharacterPet(@Param("id") int id, @Param("entry") int entry, @Param("owner") int owner, @Param("modelid") int modelid, @Param("level") int level, @Param("exp") long exp, @Param("reactstate") int reactstate, @Param("name") String name, @Param("renametime") long renametime, @Param("slot") int slot, @Param("curhealth") int curhealth, @Param("curmana") int curmana, @Param("curhappiness") int curhappiness, @Param("loyaltypoints") int loyaltypoints, @Param("trainpoint") int trainpoint, @Param("spells") String spells);

    // Guild member operations
    @Modifying
    @Query(value = "SELECT guildid, `rank` FROM guild_member WHERE guid = :guid")
    Map<String, Object> selectGuildMember(@Param("guid") long guid);

    @Query(value = "SELECT g.guildid, g.name, gr.rname, gr.rid, gm.pnote, gm.offnote FROM guild g JOIN guild_member gm ON g.guildid = gm.guildid JOIN guild_rank gr ON gm.rank = gr.rid WHERE gm.guid = :guid")
    Map<String, Object> selectGuildMemberExtended(@Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE guild_member SET pnote = :pnote WHERE guid = :guid")
    void updateGuildMemberPnote(@Param("guid") long guid, @Param("pnote") String pnote);

    @Modifying
    @Query(value = "UPDATE guild_member SET offnote = :offnote WHERE guid = :guid")
    void updateGuildMemberOffnote(@Param("guid") long guid, @Param("offnote") String offnote);

    @Modifying
    @Query(value = "UPDATE guild_member SET `rank` = :rank WHERE guid = :guid")
    void updateGuildMemberRank(@Param("guid") long guid, @Param("rank") int rank);

    @Modifying
    @Query("DELETE FROM guild_member WHERE guildid = :guildId AND guid = :guid")
    void deleteGuildMember(@Param("guildId") int guildId, @Param("guid") int guid);

    @Modifying
    @Query("INSERT INTO guild_member (guildid, guid, rank, pnote, offnote, last_online) VALUES (:guildId, :guid, :rank, :pnote, :offnote, :lastOnline)")
    void insertGuildMember(@Param("guildId") int guildId, @Param("guid") int guid, @Param("rank") int rank, @Param("pnote") String pnote, @Param("offnote") String offnote, @Param("lastOnline") long lastOnline);

    // Group member operations
    @Modifying
    @Query("DELETE FROM group_member WHERE groupId = :groupId AND memberGuid = :memberGuid")
    void deleteGroupMember(@Param("groupId") int groupId, @Param("memberGuid") int memberGuid);

    @Modifying
    @Query("INSERT INTO group_member (groupId, memberGuid, memberFlags, assistGuid) VALUES (:groupId, :memberGuid, :memberFlags, :assistGuid)")
    void insertGroupMember(@Param("groupId") int groupId, @Param("memberGuid") int memberGuid, @Param("memberFlags") int memberFlags, @Param("assistGuid") int assistGuid);

    // Character aura operations
    @Modifying
    @Query("DELETE FROM character_aura WHERE guid = :guid AND spell = :spell AND effectMask = :effectMask")
    void deleteCharacterAura(@Param("guid") int guid, @Param("spell") int spell, @Param("effectMask") int effectMask);

    @Modifying
    @Query("INSERT INTO character_aura (guid, spell, effectMask, stackCount, amount, maxDuration, remainTime, appliedAt, auraFlags, casterGuid) VALUES (:guid, :spell, :effectMask, :stackCount, :amount, :maxDuration, :remainTime, :appliedAt, :auraFlags, :casterGuid)")
    void insertCharacterAura(@Param("guid") int guid, @Param("spell") int spell, @Param("effectMask") int effectMask, @Param("stackCount") int stackCount, @Param("amount") int amount, @Param("maxDuration") int maxDuration, @Param("remainTime") int remainTime, @Param("appliedAt") long appliedAt, @Param("auraFlags") int auraFlags, @Param("casterGuid") int casterGuid);

    // Character spell operations
    @Modifying
    @Query("DELETE FROM character_spell WHERE guid = :guid AND spell = :spell")
    void deleteCharacterSpell(@Param("guid") int guid, @Param("spell") int spell);

    @Modifying
    @Query("INSERT INTO character_spell (guid, spell, active, disabled, cooldown, categoryCooldown) VALUES (:guid, :spell, :active, :disabled, :cooldown, :categoryCooldown)")
    void insertCharacterSpell(@Param("guid") int guid, @Param("spell") int spell, @Param("active") int active, @Param("disabled") int disabled, @Param("cooldown") long cooldown, @Param("categoryCooldown") long categoryCooldown);

    // Character quest status operations
    @Modifying
    @Query("DELETE FROM character_queststatus WHERE guid = :guid AND quest = :quest")
    void deleteCharacterQuestStatus(@Param("guid") int guid, @Param("quest") int quest);

    @Modifying
    @Query("INSERT INTO character_queststatus (guid, quest, status, timer, exploration, completed) VALUES (:guid, :quest, :status, :timer, :exploration, :completed)")
    void insertCharacterQuestStatus(@Param("guid") int guid, @Param("quest") int quest, @Param("status") int status, @Param("timer") long timer, @Param("exploration") String exploration, @Param("completed") int completed);

    // Character reputation operations
    @Modifying
    @Query("DELETE FROM character_reputation WHERE guid = :guid AND faction = :faction")
    void deleteCharacterReputation(@Param("guid") int guid, @Param("faction") int faction);

    @Modifying
    @Query("INSERT INTO character_reputation (guid, faction, standing, flags) VALUES (:guid, :faction, :standing, :flags)")
    void insertCharacterReputation(@Param("guid") int guid, @Param("faction") int faction, @Param("standing") int standing, @Param("flags") int flags);

    // Character inventory operations
    @Modifying
    @Query("DELETE FROM character_inventory WHERE guid = :guid AND bag = :bag AND slot = :slot")
    void deleteCharacterInventory(@Param("guid") int guid, @Param("bag") int bag, @Param("slot") int slot);

    @Modifying
    @Query("INSERT INTO character_inventory (guid, bag, slot, item, count, enchantment, durability, creator, gift_creator, stackcount) VALUES (:guid, :bag, :slot, :item, :count, :enchantment, :durability, :creator, :giftCreator, :stackcount)")
    void insertCharacterInventory(@Param("guid") int guid, @Param("bag") int bag, @Param("slot") int slot, @Param("item") long item, @Param("count") int count, @Param("enchantment") int enchantment, @Param("durability") int durability, @Param("creator") int creator, @Param("giftCreator") int giftCreator, @Param("stackcount") int stackcount);

    // Auctionhouse operations
    @Query("SELECT id, auctionHouseId, owner, bidder, minBid, buyoutOrUnitPrice, deposit, bidAmount, startTime, endTime, serverFlags FROM auctionhouse")
    List<Map<String, Object>> selectAuctions();

    @Query("SELECT auctionId, playerGuid FROM auction_bidders")
    List<Map<String, Object>> selectAuctionBidders();

    @Modifying
    @Query("INSERT INTO auction_bidders (auctionId, playerGuid) VALUES (:auctionId, :playerGuid)")
    void insertAuctionBidder(@Param("auctionId") long auctionId, @Param("playerGuid") long playerGuid);

    @Modifying
    @Query("DELETE FROM auction_bidders WHERE playerGuid = :playerGuid")
    void deleteAuctionBidderByPlayer(@Param("playerGuid") long playerGuid);

    @Modifying
    @Query("INSERT INTO auctionhouse (id, auctionHouseId, owner, bidder, minBid, buyoutOrUnitPrice, deposit, bidAmount, startTime, endTime, serverFlags) VALUES (:id, :auctionHouseId, :owner, :bidder, :minBid, :buyoutOrUnitPrice, :deposit, :bidAmount, :startTime, :endTime, :serverFlags)")
    void insertAuction(@Param("id") long id, @Param("auctionHouseId") int auctionHouseId, @Param("owner") long owner, @Param("bidder") long bidder, @Param("minBid") long minBid, @Param("buyoutOrUnitPrice") long buyoutOrUnitPrice, @Param("deposit") long deposit, @Param("bidAmount") long bidAmount, @Param("startTime") long startTime, @Param("endTime") long endTime, @Param("serverFlags") int serverFlags);

    @Modifying
    @Query("UPDATE auctionhouse SET bidder = :bidder, bidAmount = :bidAmount, serverFlags = :serverFlags WHERE id = :id")
    void updateAuctionBid(@Param("bidder") long bidder, @Param("bidAmount") long bidAmount, @Param("serverFlags") int serverFlags, @Param("id") long id);

    @Modifying
    @Query("UPDATE auctionhouse SET endTime = :endTime WHERE id = :id")
    void updateAuctionExpiration(@Param("endTime") long endTime, @Param("id") long id);
    @Modifying
    @Query("DELETE FROM auctionhouse WHERE id = :id")
    void deleteAuctionhouse(@Param("id") int id);

    @Modifying
    @Query("INSERT INTO auctionhouse (id, itemguid, owner, startbid, buyoutprice, starttime, endtime, bidder, bid, deposit, auctionhouseid, flags, context) VALUES (:id, :itemguid, :owner, :startbid, :buyoutprice, :starttime, :endtime, :bidder, :bid, :deposit, :auctionhouseid, :flags, :context)")
    void insertAuctionhouse(@Param("id") int id, @Param("itemguid") long itemguid, @Param("owner") int owner, @Param("startbid") long startbid, @Param("buyoutprice") long buyoutprice, @Param("starttime") long starttime, @Param("endtime") long endtime, @Param("bidder") int bidder, @Param("bid") long bid, @Param("deposit") long deposit, @Param("auctionhouseid") int auctionhouseid, @Param("flags") int flags, @Param("context") int context);

    // Item instance gems operations
    @Modifying
    @Query("DELETE FROM item_instance_gems WHERE item_guid = :itemGuid AND slot = :slot")
    void deleteItemInstanceGems(@Param("itemGuid") long itemGuid, @Param("slot") int slot);

    @Modifying
    @Query(value = "INSERT INTO item_instance_gems (itemGuid, gemItemId1, gemBonuses1, gemContext1, gemScalingLevel1, gemItemId2, gemBonuses2, gemContext2, gemScalingLevel2, gemItemId3, gemBonuses3, gemContext3, gemScalingLevel3) VALUES (:itemGuid, :gemItemId1, :gemBonuses1, :gemContext1, :gemScalingLevel1, :gemItemId2, :gemBonuses2, :gemContext2, :gemScalingLevel2, :gemItemId3, :gemBonuses3, :gemContext3, :gemScalingLevel3)")
    void insertItemInstanceGems(@Param("itemGuid") int itemGuid, @Param("gemItemId1") int gemItemId1, @Param("gemBonuses1") String gemBonuses1, @Param("gemContext1") int gemContext1, @Param("gemScalingLevel1") int gemScalingLevel1, @Param("gemItemId2") int gemItemId2, @Param("gemBonuses2") String gemBonuses2, @Param("gemContext2") int gemContext2, @Param("gemScalingLevel2") int gemScalingLevel2, @Param("gemItemId3") int gemItemId3, @Param("gemBonuses3") String gemBonuses3, @Param("gemContext3") int gemContext3, @Param("gemScalingLevel3") int gemScalingLevel3);

    @Query("DELETE FROM item_instance_gems WHERE item_guid = :itemGuid")
    void deleteItemInstanceGems(@Param("itemGuid") int itemGuid);

    @Query(value = "DELETE iig FROM item_instance_gems iig LEFT JOIN item_instance ii ON iig.itemGuid = ii.guid WHERE ii.owner_guid = :ownerGuid")
    void deleteItemInstanceGemsByOwner(@Param("ownerGuid") int ownerGuid);

    @Query(value = "INSERT INTO item_instance_transmog (itemGuid, itemModifiedAppearanceAllSpecs, itemModifiedAppearanceSpec1, itemModifiedAppearanceSpec2, itemModifiedAppearanceSpec3, itemModifiedAppearanceSpec4, itemModifiedAppearanceSpec5) VALUES (:itemGuid, :itemModifiedAppearanceAllSpecs, :itemModifiedAppearanceSpec1, :itemModifiedAppearanceSpec2, :itemModifiedAppearanceSpec3, :itemModifiedAppearanceSpec4, :itemModifiedAppearanceSpec5)")
    void insertItemInstanceTransmog(@Param("itemGuid") int itemGuid, @Param("itemModifiedAppearanceAllSpecs") int itemModifiedAppearanceAllSpecs, @Param("itemModifiedAppearanceSpec1") int itemModifiedAppearanceSpec1, @Param("itemModifiedAppearanceSpec2") int itemModifiedAppearanceSpec2, @Param("itemModifiedAppearanceSpec3") int itemModifiedAppearanceSpec3, @Param("itemModifiedAppearanceSpec4") int itemModifiedAppearanceSpec4, @Param("itemModifiedAppearanceSpec5") int itemModifiedAppearanceSpec5);

    @Query(value = "DELETE FROM item_instance_transmog WHERE itemGuid = :itemGuid")
    void deleteItemInstanceTransmog(@Param("itemGuid") int itemGuid);

    @Query(value = "DELETE iit FROM item_instance_transmog iit LEFT JOIN item_instance ii ON iit.itemGuid = ii.guid WHERE ii.owner_guid = :ownerGuid")
    void deleteItemInstanceTransmogByOwner(@Param("ownerGuid") int ownerGuid);

    @Query(value = "INSERT INTO mail_items(mail_id, item_guid, receiver) VALUES (:mailId, :itemGuid, :receiver)")
    void insertMailItem(@Param("mailId") int mailId, @Param("itemGuid") int itemGuid, @Param("receiver") int receiver);

    @Query(value = "DELETE FROM mail_items WHERE item_guid = :itemGuid")
    void deleteMailItemByGuid(@Param("itemGuid") int itemGuid);

    @Query(value = "UPDATE mail_items SET receiver = :receiver WHERE item_guid = :itemGuid")
    void updateMailItemReceiver(@Param("receiver") int receiver, @Param("itemGuid") int itemGuid);
    void insertItemInstanceGems(@Param("itemGuid") long itemGuid, @Param("slot") int slot, @Param("gemId") int gemId);

    // Guild bank tab operations
    @Modifying
    @Query("DELETE FROM guild_bank_tab WHERE guildid = :guildId AND TabId = :tabId")
    void deleteGuildBankTab(@Param("guildId") int guildId, @Param("tabId") int tabId);

    @Modifying
    @Query("INSERT INTO guild_bank_tab (guildid, TabId, Name, Icon, TabPermissions, Viewed) VALUES (:guildId, :tabId, :name, :icon, :tabPermissions, :viewed)")
    void insertGuildBankTab(@Param("guildId") int guildId, @Param("tabId") int tabId, @Param("name") String name, @Param("icon") String icon, @Param("tabPermissions") String tabPermissions, @Param("viewed") int viewed);

    // Character equipment sets
    @Modifying
    @Query("DELETE FROM character_equipmentsets WHERE guid = :guid AND setguid = :setguid")
    void deleteCharacterEquipmentSet(@Param("guid") int guid, @Param("setguid") int setguid);

    @Modifying
    @Query("INSERT INTO character_equipmentsets (guid, setguid, setname, seticon, items) VALUES (:guid, :setguid, :setname, :seticon, :items)")
    void insertCharacterEquipmentSet(@Param("guid") int guid, @Param("setguid") int setguid, @Param("setname") String setname, @Param("seticon") int seticon, @Param("items") String items);

    // Character battleground data operations
    @Query(value = "SELECT instanceId, team, joinX, joinY, joinZ, joinO, joinMapId, taxiStart, taxiEnd, mountSpell, queueId FROM character_battleground_data WHERE guid = :guid")
    Map<String, Object> selectCharacterBattlegroundData(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_battleground_data (guid, instanceId, team, joinX, joinY, joinZ, joinO, joinMapId, taxiStart, taxiEnd, mountSpell, queueId) VALUES (:guid, :instanceId, :team, :joinX, :joinY, :joinZ, :joinO, :joinMapId, :taxiStart, :taxiEnd, :mountSpell, :queueId)")
    void insertCharacterBattlegroundData(@Param("guid") long guid, @Param("instanceId") int instanceId, @Param("team") int team, @Param("joinX") float joinX, @Param("joinY") float joinY, @Param("joinZ") float joinZ, @Param("joinO") float joinO, @Param("joinMapId") int joinMapId, @Param("taxiStart") int taxiStart, @Param("taxiEnd") int taxiEnd, @Param("mountSpell") int mountSpell, @Param("queueId") int queueId);

    @Modifying
    @Query(value = "DELETE FROM character_battleground_data WHERE guid = :guid")
    void deleteCharacterBattlegroundData(@Param("guid") long guid);

    // Arena teams
    @Query(value = "SELECT arenaTeamId, weekGames, seasonGames, seasonWins, personalRating FROM arena_team_member WHERE guid = :guid")
    List<Object[]> selectCharacterArenaInfo(@Param("guid") Long guid);

    @Modifying
    @Query(value = "INSERT INTO arena_team (arenaTeamId, name, captainGuid, type, rating, backgroundColor, emblemStyle, emblemColor, borderStyle, borderColor) VALUES (:arenaTeamId, :name, :captainGuid, :type, :rating, :backgroundColor, :emblemStyle, :emblemColor, :borderStyle, :borderColor)")
    void insertArenaTeam(@Param("arenaTeamId") Integer arenaTeamId, @Param("name") String name, @Param("captainGuid") Long captainGuid, @Param("type") Integer type, @Param("rating") Integer rating, @Param("backgroundColor") Integer backgroundColor, @Param("emblemStyle") Integer emblemStyle, @Param("emblemColor") Integer emblemColor, @Param("borderStyle") Integer borderStyle, @Param("borderColor") Integer borderColor);

    @Modifying
    @Query(value = "INSERT INTO arena_team_member (arenaTeamId, guid, personalRating) VALUES (:arenaTeamId, :guid, :personalRating)")
    void insertArenaTeamMember(@Param("arenaTeamId") Integer arenaTeamId, @Param("guid") Long guid, @Param("personalRating") Integer personalRating);

    @Modifying
    @Query(value = "DELETE FROM arena_team WHERE arenaTeamId = :arenaTeamId")
    void deleteArenaTeam(@Param("arenaTeamId") Integer arenaTeamId);

    @Modifying
    @Query(value = "DELETE FROM arena_team_member WHERE arenaTeamId = :arenaTeamId")
    void deleteArenaTeamMembers(@Param("arenaTeamId") Integer arenaTeamId);

    @Modifying
    @Query(value = "UPDATE arena_team SET captainGuid = :captainGuid WHERE arenaTeamId = :arenaTeamId")
    void updateArenaTeamCaptain(@Param("arenaTeamId") Integer arenaTeamId, @Param("captainGuid") Long captainGuid);

    @Modifying
    @Query(value = "DELETE FROM arena_team_member WHERE arenaTeamId = :arenaTeamId AND guid = :guid")
    void deleteArenaTeamMember(@Param("arenaTeamId") Integer arenaTeamId, @Param("guid") Long guid);

    @Modifying
    @Query(value = "UPDATE arena_team SET rating = :rating, weekGames = :weekGames, weekWins = :weekWins, seasonGames = :seasonGames, seasonWins = :seasonWins, `rank` = :rank WHERE arenaTeamId = :arenaTeamId")
    void updateArenaTeamStats(@Param("arenaTeamId") Integer arenaTeamId, @Param("rating") Integer rating, @Param("weekGames") Integer weekGames, @Param("weekWins") Integer weekWins, @Param("seasonGames") Integer seasonGames, @Param("seasonWins") Integer seasonWins, @Param("rank") Integer rank);

    @Modifying
    @Query(value = "UPDATE arena_team_member SET personalRating = :personalRating, weekGames = :weekGames, weekWins = :weekWins, seasonGames = :seasonGames, seasonWins = :seasonWins WHERE arenaTeamId = :arenaTeamId AND guid = :guid")
    void updateArenaTeamMember(@Param("arenaTeamId") Integer arenaTeamId, @Param("guid") Long guid, @Param("personalRating") Integer personalRating, @Param("weekGames") Integer weekGames, @Param("weekWins") Integer weekWins, @Param("seasonGames") Integer seasonGames, @Param("seasonWins") Integer seasonWins);

    @Modifying
    @Query(value = "DELETE FROM character_arena_stats WHERE guid = :guid")
    void deleteCharacterArenaStats(@Param("guid") Long guid);

    @Modifying
    @Query(value = "REPLACE INTO character_arena_stats (guid, slot, matchMakerRating) VALUES (:guid, :slot, :matchMakerRating)")
    void replaceCharacterArenaStats(@Param("guid") Long guid, @Param("slot") Integer slot, @Param("matchMakerRating") Integer matchMakerRating);

    @Modifying
    @Query(value = "UPDATE arena_team SET name = :name WHERE arenaTeamId = :arenaTeamId")
    void updateArenaTeamName(@Param("arenaTeamId") Integer arenaTeamId, @Param("name") String name);

    @Modifying
    @Query("DELETE FROM arena_team WHERE arenaTeamId = :arenaTeamId")
    void deleteArenaTeam(@Param("arenaTeamId") int arenaTeamId);

    @Modifying
    @Query("INSERT INTO arena_team (arenaTeamId, name, captain, type, emblemStyle, emblemColor, borderStyle, borderColor, backgroundColor) VALUES (:arenaTeamId, :name, :captain, :type, :emblemStyle, :emblemColor, :borderStyle, :borderColor, :backgroundColor)")
    void insertArenaTeam(@Param("arenaTeamId") int arenaTeamId, @Param("name") String name, @Param("captain") int captain, @Param("type") int type, @Param("emblemStyle") int emblemStyle, @Param("emblemColor") int emblemColor, @Param("borderStyle") int borderStyle, @Param("borderColor") int borderColor, @Param("backgroundColor") int backgroundColor);

    // Character transmog outfits
    @Modifying
    @Query("DELETE FROM character_transmog_outfits WHERE guid = :guid AND outfit_id = :outfitId")
    void deleteCharacterTransmogOutfit(@Param("guid") int guid, @Param("outfitId") int outfitId);

    @Modifying
    @Query("INSERT INTO character_transmog_outfits (guid, outfit_id, name, items) VALUES (:guid, :outfitId, :name, :items)")
    void insertCharacterTransmogOutfit(@Param("guid") int guid, @Param("outfitId") int outfitId, @Param("name") String name, @Param("items") String items);

    // Character currency operations
    @Modifying
    @Query("DELETE FROM character_currency WHERE guid = :guid AND currency = :currency")
    void deleteCharacterCurrency(@Param("guid") int guid, @Param("currency") int currency);

    @Modifying
    @Query("INSERT INTO character_currency (guid, currency, count, weekly_count, season_count, flags) VALUES (:guid, :currency, :count, :weeklyCount, :seasonCount, :flags)")
    void insertCharacterCurrency(@Param("guid") int guid, @Param("currency") int currency, @Param("count") int count, @Param("weeklyCount") int weeklyCount, @Param("seasonCount") int seasonCount, @Param("flags") int flags);

    // Account data operations
    @Modifying
    @Query("DELETE FROM account_data WHERE account_id = :accountId AND type = :type")
    void deleteAccountData(@Param("accountId") int accountId, @Param("type") int type);

    @Modifying
    @Query("INSERT INTO account_data (account_id, type, time, data) VALUES (:accountId, :type, :time, :data)")
    void insertAccountData(@Param("accountId") int accountId, @Param("type") int type, @Param("time") long time, @Param("data") String data);

    // Character account data operations
    @Modifying
    @Query("DELETE FROM character_account_data WHERE guid = :guid AND type = :type")
    void deleteCharacterAccountData(@Param("guid") int guid, @Param("type") int type);

    @Modifying
    @Query("INSERT INTO character_account_data (guid, type, time, data) VALUES (:guid, :type, :time, :data)")
    void insertCharacterAccountData(@Param("guid") int guid, @Param("type") int type, @Param("time") long time, @Param("data") String data);

    // Game event save operations
    @Modifying
    @Query("DELETE FROM game_event_save WHERE eventEntry = :eventEntry")
    void deleteGameEventSave(@Param("eventEntry") int eventEntry);

    @Modifying
    @Query("INSERT INTO game_event_save (eventEntry, state, next_start) VALUES (:eventEntry, :state, :nextStart)")
    void insertGameEventSave(@Param("eventEntry") int eventEntry, @Param("state") int state, @Param("nextStart") long nextStart);

    // Petition operations
    @Modifying
    @Query("DELETE FROM petition WHERE petitionId = :petitionId")
    void deletePetition(@Param("petitionId") int petitionId);

    @Modifying
    @Query("INSERT INTO petition (petitionId, name, ownerGuid, type, allowedGuildID, signCount, maxSignCount, deadline, issueDate, isClosed) VALUES (:petitionId, :name, :ownerGuid, :type, :allowedGuildID, :signCount, :maxSignCount, :deadline, :issueDate, :isClosed)")
    void insertPetition(@Param("petitionId") int petitionId, @Param("name") String name, @Param("ownerGuid") int ownerGuid, @Param("type") int type, @Param("allowedGuildID") int allowedGuildID, @Param("signCount") int signCount, @Param("maxSignCount") int maxSignCount, @Param("deadline") long deadline, @Param("issueDate") long issueDate, @Param("isClosed") int isClosed);

    // Arena team member operations
    @Modifying
    @Query("DELETE FROM arena_team_member WHERE arenaTeamId = :arenaTeamId AND guid = :guid")
    void deleteArenaTeamMember(@Param("arenaTeamId") int arenaTeamId, @Param("guid") int guid);

    @Modifying
    @Query("INSERT INTO arena_team_member (arenaTeamId, guid, weekGames, weekWins, seasonGames, seasonWins, personalRating) VALUES (:arenaTeamId, :guid, :weekGames, :weekWins, :seasonGames, :seasonWins, :personalRating)")
    void insertArenaTeamMember(@Param("arenaTeamId") int arenaTeamId, @Param("guid") int guid, @Param("weekGames") int weekGames, @Param("weekWins") int weekWins, @Param("seasonGames") int seasonGames, @Param("seasonWins") int seasonWins, @Param("personalRating") int personalRating);

    // Petition sign operations
    @Modifying
    @Query(value = "SELECT achievement, date FROM character_achievement WHERE guid = :guid")
    List<Map<String, Object>> selectCharacterAchievements(@Param("guid") long guid);

    @Query(value = "SELECT criteria, counter, date FROM character_achievement_progress WHERE guid = :guid")
    List<Map<String, Object>> selectCharacterCriteriaProgress(@Param("guid") long guid);

    @Query(value = "SELECT talentGroup, talentId, `rank` FROM character_talent WHERE guid = :guid")
    List<Map<String, Object>> selectCharacterTalents(@Param("guid") long guid);

    @Query(value = "SELECT id, talentTabId FROM character_talent_group WHERE guid = :guid ORDER BY id ASC")
    List<Map<String, Object>> selectCharacterTalentGroups(@Param("guid") long guid);

    @Query("DELETE FROM petition_sign WHERE petitionId = :petitionId AND playerguid = :playerGuid")
    void deletePetitionSign(@Param("petitionId") int petitionId, @Param("playerGuid") int playerGuid);

    @Modifying
    @Query("INSERT INTO petition_sign (petitionId, playerguid, choice) VALUES (:petitionId, :playerGuid, :choice)")
    void insertPetitionSign(@Param("petitionId") int petitionId, @Param("playerGuid") int playerGuid, @Param("choice") int choice);

    // Character skills operations
    @Modifying
    @Query("DELETE FROM character_skills WHERE guid = :guid AND skill = :skill")
    void deleteCharacterSkill(@Param("guid") int guid, @Param("skill") int skill);

    @Modifying
    @Query("INSERT INTO character_skills (guid, skill, value, maxValue, step, rank, classIndex) VALUES (:guid, :skill, :value, :maxValue, :step, :rank, :classIndex)")
    void insertCharacterSkill(@Param("guid") int guid, @Param("skill") int skill, @Param("value") int value, @Param("maxValue") int maxValue, @Param("step") int step, @Param("rank") int rank, @Param("classIndex") int classIndex);

    // Character social operations
    @Modifying
    @Query(value = "SELECT cs.friend, c.account, cs.flags, cs.note FROM character_social cs JOIN characters c ON c.guid = cs.friend WHERE cs.guid = :guid AND c.deleteinfos_name IS NULL LIMIT 255")
    List<Map<String, Object>> selectCharacterSocialList(@Param("guid") int guid);

    @Query("DELETE FROM character_social WHERE guid = :guid AND friend = :friendGuid")
    void deleteCharacterSocial(@Param("guid") int guid, @Param("friendGuid") int friendGuid);

    @Modifying
    @Query("INSERT INTO character_social (guid, friend, flags, note) VALUES (:guid, :friendGuid, :flags, :note)")
    void insertCharacterSocial(@Param("guid") int guid, @Param("friendGuid") int friendGuid, @Param("flags") int flags, @Param("note") String note);

    // World state value operations
    @Modifying
    @Query("DELETE FROM world_state_value WHERE entry = :entry")
    void deleteWorldStateValue(@Param("entry") int entry);

    @Modifying
    @Query("INSERT INTO world_state_value (entry, value) VALUES (:entry, :value)")
    void insertWorldStateValue(@Param("entry") int entry, @Param("value") int value);

    // World variable operations
    @Modifying
    @Query("DELETE FROM world_variable WHERE name = :name")
    void deleteWorldVariable(@Param("name") String name);

    @Modifying
    @Query("INSERT INTO world_variable (name, value) VALUES (:name, :value)")
    void insertWorldVariable(@Param("name") String name, @Param("value") String value);

    // GM complaint operations
    @Modifying
    @Query("DELETE FROM gm_complaint WHERE complaintId = :complaintId")
    void deleteGMComplaint(@Param("complaintId") int complaintId);

    @Modifying
    @Query("INSERT INTO gm_complaint (complaintId, guid, reportedGuid, type, comment, createTime, mapId, posX, posY, posZ) VALUES (:complaintId, :guid, :reportedGuid, :type, :comment, :createTime, :mapId, :posX, :posY, :posZ)")
    void insertGMComplaint(@Param("complaintId") int complaintId, @Param("guid") int guid, @Param("reportedGuid") int reportedGuid, @Param("type") int type, @Param("comment") String comment, @Param("createTime") long createTime, @Param("mapId") int mapId, @Param("posX") float posX, @Param("posY") float posY, @Param("posZ") float posZ);

    // Respawn operations
    @Modifying
    @Query(value = "REPLACE INTO respawn (type, spawnId, respawnTime, mapId, instanceId) VALUES (:type, :spawnId, :respawnTime, :mapId, :instanceId)")
    void replaceRespawn(@Param("type") int type, @Param("spawnId") int spawnId, @Param("respawnTime") Long respawnTime, @Param("mapId") int mapId, @Param("instanceId") Integer instanceId);

    @Modifying
    @Query(value = "DELETE FROM respawn WHERE type = :type AND spawnId = :spawnId AND mapId = :mapId AND instanceId = :instanceId")
    @Transactional
    void deleteRespawn(@Param("type") int type, @Param("spawnId") int spawnId, @Param("mapId") int mapId, @Param("instanceId") Integer instanceId);

    @Modifying
    @Query(value = "DELETE FROM respawn WHERE mapId = :mapId AND instanceId = :instanceId")
    void deleteAllRespawns(@Param("mapId") int mapId, @Param("instanceId") Integer instanceId);

    // GM Bug operations
    @Query(value = "SELECT id, playerGuid, note, createTime, mapId, posX, posY, posZ, facing, closedBy, assignedTo, comment FROM gm_bug")
    List<Object[]> selectGMBugs();

    @Modifying
    @Query(value = "REPLACE INTO gm_bug (id, playerGuid, note, createTime, mapId, posX, posY, posZ, facing, closedBy, assignedTo, comment) VALUES (:id, :playerGuid, :note, UNIX_TIMESTAMP(NOW()), :mapId, :posX, :posY, :posZ, :facing, :closedBy, :assignedTo, :comment)")
    void replaceGMBug(@Param("id") Integer id, @Param("playerGuid") Long playerGuid, @Param("note") String note, @Param("mapId") int mapId, @Param("posX") Float posX, @Param("posY") Float posY, @Param("posZ") Float posZ, @Param("facing") Float facing, @Param("closedBy") Long closedBy, @Param("assignedTo") Long assignedTo, @Param("comment") String comment);

    @Modifying
    @Query(value = "DELETE FROM gm_bug WHERE id = :id")
    void deleteGMBug(@Param("id") Integer id);

    @Modifying
    @Query(value = "DELETE FROM gm_bug")
    void deleteAllGMBugs();

    // GM Complaint operations
    @Query(value = "SELECT id, playerGuid, note, createTime, mapId, posX, posY, posZ, facing, targetCharacterGuid, reportType, reportMajorCategory, reportMinorCategoryFlags, reportLineIndex, assignedTo, closedBy, comment FROM gm_complaint")
    List<Object[]> selectGMComplaints();

    @Modifying
    @Query(value = "REPLACE INTO gm_complaint (id, playerGuid, note, createTime, mapId, posX, posY, posZ, facing, targetCharacterGuid, reportType, reportMajorCategory, reportMinorCategoryFlags, reportLineIndex, assignedTo, closedBy, comment) VALUES (:id, :playerGuid, :note, UNIX_TIMESTAMP(NOW()), :mapId, :posX, :posY, :posZ, :facing, :targetCharacterGuid, :reportType, :reportMajorCategory, :reportMinorCategoryFlags, :reportLineIndex, :assignedTo, :closedBy, :comment)")
    void replaceGMComplaint(@Param("id") Integer id, @Param("playerGuid") Long playerGuid, @Param("note") String note, @Param("mapId") int mapId, @Param("posX") Float posX, @Param("posY") Float posY, @Param("posZ") Float posZ, @Param("facing") Float facing, @Param("targetCharacterGuid") Long targetCharacterGuid, @Param("reportType") Integer reportType, @Param("reportMajorCategory") Integer reportMajorCategory, @Param("reportMinorCategoryFlags") Integer reportMinorCategoryFlags, @Param("reportLineIndex") Integer reportLineIndex, @Param("assignedTo") Long assignedTo, @Param("closedBy") Long closedBy, @Param("comment") String comment);

    @Modifying
    @Query(value = "DELETE FROM gm_complaint WHERE id = :id")
    void deleteGMComplaint(@Param("id") Integer id);

    @Query(value = "SELECT timestamp, text FROM gm_complaint_chatlog WHERE complaintId = :complaintId ORDER BY lineId ASC")
    List<Object[]> selectGMComplaintChatlines(@Param("complaintId") Integer complaintId);

    @Modifying
    @Query(value = "INSERT INTO gm_complaint_chatlog (complaintId, lineId, timestamp, text) VALUES (:complaintId, :lineId, :timestamp, :text)")
    void insertGMComplaintChatline(@Param("complaintId") Integer complaintId, @Param("lineId") Integer lineId, @Param("timestamp") Long timestamp, @Param("text") String text);

    @Modifying
    @Query(value = "DELETE FROM gm_complaint_chatlog WHERE complaintId = :complaintId")
    void deleteGMComplaintChatlog(@Param("complaintId") Integer complaintId);

    @Modifying
    @Query(value = "DELETE FROM gm_complaint")
    void deleteAllGMComplaints();

    @Modifying
    @Query(value = "DELETE FROM gm_complaint_chatlog")
    void deleteAllGMComplaintChatlogs();

    // GM Suggestion operations
    @Query(value = "SELECT id, playerGuid, note, createTime, mapId, posX, posY, posZ, facing, closedBy, assignedTo, comment FROM gm_suggestion")
    List<Object[]> selectGMSuggestions();

    @Modifying
    @Query(value = "REPLACE INTO gm_suggestion (id, playerGuid, note, createTime, mapId, posX, posY, posZ, facing, closedBy, assignedTo, comment) VALUES (:id, :playerGuid, :note, UNIX_TIMESTAMP(NOW()), :mapId, :posX, :posY, :posZ, :facing, :closedBy, :assignedTo, :comment)")
    void replaceGMSuggestion(@Param("id") Integer id, @Param("playerGuid") Long playerGuid, @Param("note") String note, @Param("mapId") int mapId, @Param("posX") Float posX, @Param("posY") Float posY, @Param("posZ") Float posZ, @Param("facing") Float facing, @Param("closedBy") Long closedBy, @Param("assignedTo") Long assignedTo, @Param("comment") String comment);

    @Modifying
    @Query(value = "DELETE FROM gm_suggestion WHERE id = :id")
    void deleteGMSuggestion(@Param("id") Integer id);

    @Modifying
    @Query(value = "DELETE FROM gm_suggestion")
    void deleteAllGMSuggestions();

    // LFG Data operations
    @Modifying
    @Query(value = "INSERT INTO lfg_data (guid, dungeon, state) VALUES (:guid, :dungeon, :state)")
    void insertLFGData(@Param("guid") Long guid, @Param("dungeon") Integer dungeon, @Param("state") Integer state);

    @Modifying
    @Query(value = "DELETE FROM lfg_data WHERE guid = :guid")
    void deleteLFGData(@Param("guid") Long guid);

    // Player saving operations
    @Modifying
    @Query(value = "INSERT INTO characters (guid, account, name, race, class, gender, level, xp, money, inventorySlots, bankSlots, restState, playerFlags, playerFlagsEx, map, instance_id, dungeonDifficulty, raidDifficulty, legacyRaidDifficulty, position_x, position_y, position_z, orientation, trans_x, trans_y, trans_z, trans_o, transguid, taximask, createTime, createMode, cinematic, totaltime, leveltime, rest_bonus, logout_time, is_logout_resting, resettalents_cost, resettalents_time, primarySpecialization, extra_flags, summonedPetNumber, at_login, death_expire_time, taxi_path, totalKills, todayKills, yesterdayKills, chosenTitle, watchedFaction, drunk, health, power1, power2, power3, power4, power5, power6, power7, power8, power9, power10, latency, activeTalentGroup, lootSpecId, exploredZones, equipmentCache, knownTitles, actionBars, lastLoginBuild) VALUES (:guid, :account, :name, :race, :class, :gender, :level, :xp, :money, :inventorySlots, :bankSlots, :restState, :playerFlags, :playerFlagsEx, :map, :instance_id, :dungeonDifficulty, :raidDifficulty, :legacyRaidDifficulty, :position_x, :position_y, :position_z, :orientation, :trans_x, :trans_y, :trans_z, :trans_o, :transguid, :taximask, :createTime, :createMode, :cinematic, :totaltime, :leveltime, :rest_bonus, :logout_time, :is_logout_resting, :resettalents_cost, :resettalents_time, :primarySpecialization, :extra_flags, :summonedPetNumber, :at_login, :death_expire_time, :taxi_path, :totalKills, :todayKills, :yesterdayKills, :chosenTitle, :watchedFaction, :drunk, :health, :power1, :power2, :power3, :power4, :power5, :power6, :power7, :power8, :power9, :power10, :latency, :activeTalentGroup, :lootSpecId, :exploredZones, :equipmentCache, :knownTitles, :actionBars, :lastLoginBuild)")
    void insertCharacter(@Param("guid") Long guid, @Param("account") Integer account, @Param("name") String name, @Param("race") Integer race, @Param("class") Integer clazz, @Param("gender") Integer gender, @Param("level") Integer level, @Param("xp") Long xp, @Param("money") Long money, @Param("inventorySlots") Integer inventorySlots, @Param("bankSlots") Integer bankSlots, @Param("restState") Integer restState, @Param("playerFlags") Integer playerFlags, @Param("playerFlagsEx") Integer playerFlagsEx, @Param("map") Integer map, @Param("instance_id") Integer instance_id, @Param("dungeonDifficulty") Integer dungeonDifficulty, @Param("raidDifficulty") Integer raidDifficulty, @Param("legacyRaidDifficulty") Integer legacyRaidDifficulty, @Param("position_x") Float position_x, @Param("position_y") Float position_y, @Param("position_z") Float position_z, @Param("orientation") Float orientation, @Param("trans_x") Float trans_x, @Param("trans_y") Float trans_y, @Param("trans_z") Float trans_z, @Param("trans_o") Float trans_o, @Param("transguid") Long transguid, @Param("taximask") Long taximask, @Param("createTime") Long createTime, @Param("createMode") Integer createMode, @Param("cinematic") Integer cinematic, @Param("totaltime") Long totaltime, @Param("leveltime") Long leveltime, @Param("rest_bonus") Float rest_bonus, @Param("logout_time") Long logout_time, @Param("is_logout_resting") Integer is_logout_resting, @Param("resettalents_cost") Integer resettalents_cost, @Param("resettalents_time") Long resettalents_time, @Param("primarySpecialization") Integer primarySpecialization, @Param("extra_flags") Integer extra_flags, @Param("summonedPetNumber") Integer summonedPetNumber, @Param("at_login") Integer at_login, @Param("death_expire_time") Long death_expire_time, @Param("taxi_path") String taxi_path, @Param("totalKills") Integer totalKills, @Param("todayKills") Integer todayKills, @Param("yesterdayKills") Integer yesterdayKills, @Param("chosenTitle") Integer chosenTitle, @Param("watchedFaction") Integer watchedFaction, @Param("drunk") Integer drunk, @Param("health") Integer health, @Param("power1") Integer power1, @Param("power2") Integer power2, @Param("power3") Integer power3, @Param("power4") Integer power4, @Param("power5") Integer power5, @Param("power6") Integer power6, @Param("power7") Integer power7, @Param("power8") Integer power8, @Param("power9") Integer power9, @Param("power10") Integer power10, @Param("latency") Integer latency, @Param("activeTalentGroup") Integer activeTalentGroup, @Param("lootSpecId") Integer lootSpecId, @Param("exploredZones") String exploredZones, @Param("equipmentCache") String equipmentCache, @Param("knownTitles") String knownTitles, @Param("actionBars") String actionBars, @Param("lastLoginBuild") Integer lastLoginBuild);

    @Modifying
    @Query(value = "UPDATE characters SET name=:name,race=:race,class=:class,gender=:gender,level=:level,xp=:xp,money=:money,inventorySlots=:inventorySlots,bankSlots=:bankSlots,restState=:restState,playerFlags=:playerFlags,playerFlagsEx=:playerFlagsEx,map=:map,instance_id=:instance_id,dungeonDifficulty=:dungeonDifficulty,raidDifficulty=:raidDifficulty,legacyRaidDifficulty=:legacyRaidDifficulty,position_x=:position_x,position_y=:position_y,position_z=:position_z,orientation=:orientation,trans_x=:trans_x,trans_y=:trans_y,trans_z=:trans_z,trans_o=:trans_o,transguid=:transguid,taximask=:taximask,cinematic=:cinematic,totaltime=:totaltime,leveltime=:leveltime,rest_bonus=:rest_bonus,logout_time=:logout_time,is_logout_resting=:is_logout_resting,resettalents_cost=:resettalents_cost,resettalents_time=:resettalents_time,numRespecs=:numRespecs,primarySpecialization=:primarySpecialization,extra_flags=:extra_flags,summonedPetNumber=:summonedPetNumber,at_login=:at_login,zone=:zone,death_expire_time=:death_expire_time,taxi_path=:taxi_path,totalKills=:totalKills,todayKills=:todayKills,yesterdayKills=:yesterdayKills,chosenTitle=:chosenTitle,watchedFaction=:watchedFaction,drunk=:drunk,health=:health,power1=:power1,power2=:power2,power3=:power3,power4=:power4,power5=:power5,power6=:power6,power7=:power7,power8=:power8,power9=:power9,power10=:power10,latency=:latency,activeTalentGroup=:activeTalentGroup,lootSpecId=:lootSpecId,exploredZones=:exploredZones,equipmentCache=:equipmentCache,knownTitles=:knownTitles,actionBars=:actionBars,online=:online,honor=:honor,honorLevel=:honorLevel,honorRestState=:honorRestState,honorRestBonus=:honorRestBonus,lastLoginBuild=:lastLoginBuild WHERE guid=:guid")
    void updateCharacter(@Param("name") String name, @Param("race") Integer race, @Param("class") Integer klass, @Param("gender") Integer gender, @Param("level") Integer level, @Param("xp") Long xp, @Param("money") Long money, @Param("inventorySlots") Integer inventorySlots, @Param("bankSlots") Integer bankSlots, @Param("restState") Integer restState, @Param("playerFlags") Integer playerFlags, @Param("playerFlagsEx") Integer playerFlagsEx, @Param("map") Integer map, @Param("instance_id") Integer instance_id, @Param("dungeonDifficulty") Integer dungeonDifficulty, @Param("raidDifficulty") Integer raidDifficulty, @Param("legacyRaidDifficulty") Integer legacyRaidDifficulty, @Param("position_x") Float position_x, @Param("position_y") Float position_y, @Param("position_z") Float position_z, @Param("orientation") Float orientation, @Param("trans_x") Float trans_x, @Param("trans_y") Float trans_y, @Param("trans_z") Float trans_z, @Param("trans_o") Float trans_o, @Param("transguid") Long transguid, @Param("taximask") Long taximask, @Param("cinematic") Integer cinematic, @Param("totaltime") Long totaltime, @Param("leveltime") Long leveltime, @Param("rest_bonus") Float rest_bonus, @Param("logout_time") Long logout_time, @Param("is_logout_resting") Integer is_logout_resting, @Param("resettalents_cost") Integer resettalents_cost, @Param("resettalents_time") Long resettalents_time, @Param("numRespecs") Integer numRespecs, @Param("primarySpecialization") Integer primarySpecialization, @Param("extra_flags") Integer extra_flags, @Param("summonedPetNumber") Integer summonedPetNumber, @Param("at_login") Integer at_login, @Param("zone") Integer zone, @Param("death_expire_time") Long death_expire_time, @Param("taxi_path") String taxi_path, @Param("totalKills") Integer totalKills, @Param("todayKills") Integer todayKills, @Param("yesterdayKills") Integer yesterdayKills, @Param("chosenTitle") Integer chosenTitle, @Param("watchedFaction") Integer watchedFaction, @Param("drunk") Integer drunk, @Param("health") Integer health, @Param("power1") Integer power1, @Param("power2") Integer power2, @Param("power3") Integer power3, @Param("power4") Integer power4, @Param("power5") Integer power5, @Param("power6") Integer power6, @Param("power7") Integer power7, @Param("power8") Integer power8, @Param("power9") Integer power9, @Param("power10") Integer power10, @Param("latency") Integer latency, @Param("activeTalentGroup") Integer activeTalentGroup, @Param("lootSpecId") Integer lootSpecId, @Param("exploredZones") String exploredZones, @Param("equipmentCache") String equipmentCache, @Param("knownTitles") String knownTitles, @Param("actionBars") String actionBars, @Param("online") Integer online, @Param("honor") Integer honor, @Param("honorLevel") Integer honorLevel, @Param("honorRestState") Integer honorRestState, @Param("honorRestBonus") Integer honorRestBonus, @Param("lastLoginBuild") Integer lastLoginBuild, @Param("guid") Long guid);

    @Modifying
    @Query(value = "UPDATE characters SET at_login = at_login | :flag WHERE guid = :guid")
    void addAtLoginFlag(@Param("flag") Integer flag, @Param("guid") Long guid);

    @Modifying
    @Query(value = "UPDATE characters SET at_login = at_login & ~ :flag WHERE guid = :guid")
    void removeAtLoginFlag(@Param("flag") Integer flag, @Param("guid") Long guid);

    // GM bug operations
    @Modifying
    @Query("DELETE FROM gm_bug WHERE bugId = :bugId")
    void deleteGMBug(@Param("bugId") int bugId);

    @Modifying
    @Query("INSERT INTO gm_bug (bugId, guid, type, content, createTime, mapId, posX, posY, posZ) VALUES (:bugId, :guid, :type, :content, :createTime, :mapId, :posX, :posY, :posZ)")
    void insertGMBug(@Param("bugId") int bugId, @Param("guid") int guid, @Param("type") int type, @Param("content") String content, @Param("createTime") long createTime, @Param("mapId") int mapId, @Param("posX") float posX, @Param("posY") float posY, @Param("posZ") float posZ);
    // Tutorials
    @Modifying
    @Query(value = "UPDATE account_tutorial SET tut0 = :tut0, tut1 = :tut1, tut2 = :tut2, tut3 = :tut3, tut4 = :tut4, tut5 = :tut5, tut6 = :tut6, tut7 = :tut7 WHERE accountId = :accountId")
    void updateTutorials(@Param("tut0") Integer tut0, @Param("tut1") Integer tut1, @Param("tut2") Integer tut2, @Param("tut3") Integer tut3, @Param("tut4") Integer tut4, @Param("tut5") Integer tut5, @Param("tut6") Integer tut6, @Param("tut7") Integer tut7, @Param("accountId") Long accountId);

    @Modifying
    @Query(value = "DELETE FROM account_tutorial WHERE accountId = :accountId")
    void deleteTutorials(@Param("accountId") Long accountId);

    // Game event saves
    @Modifying
    @Query(value = "DELETE FROM game_event_save WHERE eventEntry = :eventEntry")
    void deleteGameEventSave(@Param("eventEntry") Integer eventEntry);

    @Modifying
    @Query(value = "INSERT INTO game_event_save (eventEntry, state, next_start) VALUES (:eventEntry, :state, :next_start)")
    void insertGameEventSave(@Param("eventEntry") Integer eventEntry, @Param("state") Integer state, @Param("next_start") Instant nextStart);

    // Game event condition saves
    @Modifying
    @Query(value = "DELETE FROM game_event_condition_save WHERE eventEntry = :eventEntry")
    void deleteAllGameEventConditionSave(@Param("eventEntry") Integer eventEntry);

    @Modifying
    @Query(value = "DELETE FROM game_event_condition_save WHERE eventEntry = :eventEntry AND condition_id = :conditionId")
    void deleteGameEventConditionSave(@Param("eventEntry") Integer eventEntry, @Param("conditionId") Integer conditionId);

    @Modifying
    @Query(value = "INSERT INTO game_event_condition_save (eventEntry, condition_id, done) VALUES (:eventEntry, :conditionId, :done)")
    void insertGameEventConditionSave(@Param("eventEntry") Integer eventEntry, @Param("conditionId") Integer conditionId, @Param("done") Integer done);

    // Petitions
    @Query(value = "SELECT ownerguid, name FROM petition WHERE petitionguid = :petitionguid")
    List<Object[]> selectPetition(@Param("petitionguid") Long petitionguid);

    @Query(value = "SELECT playerguid FROM petition_sign WHERE petitionguid = :petitionguid")
    List<Long> selectPetitionSignature(@Param("petitionguid") Long petitionguid);

    @Modifying
    @Query(value = "DELETE FROM petition_sign WHERE playerguid = :playerguid")
    void deleteAllPetitionSignatures(@Param("playerguid") Long playerguid);

    @Query(value = "SELECT petitionguid FROM petition WHERE ownerguid = :ownerguid")
    List<Long> selectPetitionByOwner(@Param("ownerguid") Long ownerguid);

    @Query(value = "SELECT ownerguid, (SELECT COUNT(playerguid) FROM petition_sign WHERE petition_sign.petitionguid = :petitionguid) AS signs FROM petition WHERE petitionguid = :petitionguid")
    List<Object[]> selectPetitionSignaturesCount(@Param("petitionguid") Long petitionguid);

    @Query(value = "SELECT playerguid FROM petition_sign WHERE player_account = :playerAccount AND petitionguid = :petitionguid")
    List<Long> selectPetitionSigByAccount(@Param("playerAccount") Long playerAccount, @Param("petitionguid") Long petitionguid);

    @Query(value = "SELECT ownerguid FROM petition WHERE petitionguid = :petitionguid")
    Long selectPetitionOwnerByGuid(@Param("petitionguid") Long petitionguid);

    @Query(value = "SELECT ownerguid, petitionguid FROM petition_sign WHERE playerguid = :playerguid")
    List<Object[]> selectPetitionSigByGuid(@Param("playerguid") Long playerguid);
    @Modifying
    @Query(value = "INSERT INTO bugreport (type, content) VALUES(:type, :content)")
    void insertBugReport(@Param("type") int type, @Param("content") String content);

    @Modifying
    @Query(value = "UPDATE petition SET name = :name WHERE petitionguid = :petitionGuid")
    void updatePetitionName(@Param("name") String name, @Param("petitionGuid") long petitionGuid);

    @Modifying
    @Query(value = "INSERT INTO petition_sign (ownerguid, petitionguid, playerguid, player_account) VALUES (:ownerGuid, :petitionGuid, :playerGuid, :playerAccount)")
    void insertPetitionSignature(@Param("ownerGuid") long ownerGuid, @Param("petitionGuid") long petitionGuid, @Param("playerGuid") long playerGuid, @Param("playerAccount") long playerAccount);

    @Modifying
    @Query(value = "UPDATE characters SET online = 0 WHERE account = :accountId")
    void updateAccountOnlineStatus(@Param("accountId") long accountId);

    @Modifying
    @Query(value = "INSERT INTO character_customizations (guid, chrCustomizationOptionID, chrCustomizationChoiceID) VALUES (:guid, :optionId, :choiceId)")
    void insertCharacterCustomization(@Param("guid") long guid, @Param("optionId") int optionId, @Param("choiceId") int choiceId);

    @Modifying
    @Query(value = "DELETE FROM character_customizations WHERE guid = :guid")
    void deleteCharacterCustomizations(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO `groups` (guid, leaderGuid, lootMethod, looterGuid, lootThreshold, icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8, groupType, difficulty, raidDifficulty, legacyRaidsDifficulty, masterLooterGuid) VALUES (:guid, :leaderGuid, :lootMethod, :looterGuid, :lootThreshold, :icon1, :icon2, :icon3, :icon4, :icon5, :icon6, :icon7, :icon8, :groupType, :difficulty, :raidDifficulty, :legacyRaidsDifficulty, :masterLooterGuid)")
    void insertGroup(@Param("guid") long guid, @Param("leaderGuid") long leaderGuid, @Param("lootMethod") int lootMethod, @Param("looterGuid") long looterGuid, @Param("lootThreshold") int lootThreshold, @Param("icon1") int icon1, @Param("icon2") int icon2, @Param("icon3") int icon3, @Param("icon4") int icon4, @Param("icon5") int icon5, @Param("icon6") int icon6, @Param("icon7") int icon7, @Param("icon8") int icon8, @Param("groupType") int groupType, @Param("difficulty") int difficulty, @Param("raidDifficulty") int raidDifficulty, @Param("legacyRaidsDifficulty") int legacyRaidsDifficulty, @Param("masterLooterGuid") long masterLooterGuid);

    @Modifying
    @Query(value = "INSERT INTO group_member (guid, memberGuid, memberFlags, subgroup, roles) VALUES(:guid, :memberGuid, :memberFlags, :subgroup, :roles)")
    void insertGroupMember(@Param("guid") long guid, @Param("memberGuid") long memberGuid, @Param("memberFlags") int memberFlags, @Param("subgroup") int subgroup, @Param("roles") int roles);

    @Modifying
    @Query(value = "DELETE FROM group_member WHERE memberGuid = :memberGuid")
    void deleteGroupMember(@Param("memberGuid") long memberGuid);

    @Modifying
    @Query(value = "UPDATE `groups` SET leaderGuid = :leaderGuid WHERE guid = :guid")
    void updateGroupLeader(@Param("leaderGuid") long leaderGuid, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE `groups` SET groupType = :groupType WHERE guid = :guid")
    void updateGroupType(@Param("groupType") int groupType, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE group_member SET subgroup = :subgroup WHERE memberGuid = :memberGuid")
    void updateGroupMemberSubgroup(@Param("subgroup") int subgroup, @Param("memberGuid") long memberGuid);

    @Modifying
    @Query(value = "UPDATE group_member SET memberFlags = :memberFlags WHERE memberGuid = :memberGuid")
    void updateGroupMemberFlags(@Param("memberFlags") int memberFlags, @Param("memberGuid") long memberGuid);

    @Modifying
    @Query(value = "UPDATE `groups` SET difficulty = :difficulty WHERE guid = :guid")
    void updateGroupDifficulty(@Param("difficulty") int difficulty, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE `groups` SET raidDifficulty = :raidDifficulty WHERE guid = :guid")
    void updateGroupRaidDifficulty(@Param("raidDifficulty") int raidDifficulty, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE `groups` SET legacyRaidsDifficulty = :legacyRaidsDifficulty WHERE guid = :guid")
    void updateGroupLegacyRaidDifficulty(@Param("legacyRaidsDifficulty") int legacyRaidsDifficulty, @Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_achievement_progress WHERE criteria = :criteria")
    void deleteInvalidAchievementProgressCriteria(@Param("criteria") int criteria);

    @Modifying
    @Query(value = "DELETE FROM guild_achievement_progress WHERE criteria = :criteria")
    void deleteInvalidAchievementProgressCriteriaGuild(@Param("criteria") int criteria);

    @Modifying
    @Query(value = "DELETE FROM character_achievement WHERE achievement = :achievement")
    void deleteInvalidAchievement(@Param("achievement") int achievement);

    @Modifying
    @Query(value = "DELETE FROM pet_spell WHERE spell = :spell")
    void deleteInvalidPetSpell(@Param("spell") int spell);

    @Modifying
    @Query(value = "UPDATE characters SET online = 1 WHERE guid = :guid")
    void updateCharacterOnlineStatus(@Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE characters SET name = :name, at_login = :atLogin WHERE guid = :guid")
    void updateCharacterNameAtLogin(@Param("name") String name, @Param("atLogin") int atLogin, @Param("guid") long guid);

    @Modifying
    @Query(value = "REPLACE INTO world_state_value (Id, Value) VALUES (:id, :value)")
    void replaceWorldStateValue(@Param("id") int id, @Param("value") int value);

    @Modifying
    @Query(value = "REPLACE INTO world_variable (Id, Value) VALUES (:id, :value)")
    void replaceWorldVariable(@Param("id") int id, @Param("value") int value);

    @Modifying
    @Query(value = "DELETE FROM character_skills WHERE guid = :guid AND skill = :skill")
    void deleteCharacterSkill(@Param("guid") long guid, @Param("skill") int skill);

    @Modifying
    @Query(value = "UPDATE character_social SET flags = :flags WHERE guid = :guid AND friend = :friend")
    void updateCharacterSocialFlags(@Param("flags") int flags, @Param("guid") long guid, @Param("friend") long friend);

    @Modifying
    @Query(value = "INSERT INTO character_social (guid, friend, flags) VALUES (:guid, :friend, :flags)")
    void insertCharacterSocial(@Param("guid") long guid, @Param("friend") long friend, @Param("flags") int flags);

    @Modifying
    @Query(value = "DELETE FROM character_social WHERE guid = :guid AND friend = :friend")
    void deleteCharacterSocial(@Param("guid") long guid, @Param("friend") long friend);

    @Modifying
    @Query(value = "UPDATE character_social SET note = :note WHERE guid = :guid AND friend = :friend")
    void updateCharacterSocialNote(@Param("note") String note, @Param("guid") long guid, @Param("friend") long friend);

    @Modifying
    @Query(value = "UPDATE characters SET zone = :zone WHERE guid = :guid")
    void updateCharacterZone(@Param("zone") int zone, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE characters SET level = :level, xp = 0 WHERE guid = :guid")
    void updateCharacterLevel(@Param("level") int level, @Param("guid") long guid);

    @Query(value = "SELECT characters.name, character_aura.remainTime FROM characters LEFT JOIN character_aura ON (characters.guid = character_aura.guid) WHERE character_aura.spell = 9454")
    List<Map<String, Object>> selectCharacterAuraFrozen();

    @Query(value = "SELECT name, account, map, zone FROM characters WHERE online > 0")
    List<Map<String, Object>> selectCharacterOnline();

    @Query(value = "SELECT guid, deleteInfos_Name, deleteInfos_Account, deleteDate FROM characters WHERE deleteDate IS NOT NULL AND guid = :guid")
    List<Map<String, Object>> selectCharacterDeleteInfoByGuid(@Param("guid") long guid);

    @Query(value = "SELECT guid, deleteInfos_Name, deleteInfos_Account, deleteDate FROM characters WHERE deleteDate IS NOT NULL AND deleteInfos_Name LIKE CONCAT('%', :name, '%')")
    List<Map<String, Object>> selectCharacterDeleteInfoByName(@Param("name") String name);

    @Query(value = "SELECT guid, deleteInfos_Name, deleteInfos_Account, deleteDate FROM characters WHERE deleteDate IS NOT NULL")
    List<Map<String, Object>> selectCharacterDeleteInfo();

    @Query(value = "SELECT guid FROM characters WHERE account = :accountId")
    List<Long> selectCharactersByAccountId(@Param("accountId") long accountId);

    @Query(value = "SELECT totaltime, level, money, account, race, class, map, zone, gender, health, playerFlags FROM characters WHERE guid = :guid")
    Map<String, Object> selectCharacterPInfo(@Param("guid") long guid);

    @Query(value = "SELECT unbandate, bandate = unbandate, bannedby, banreason FROM character_banned WHERE guid = :guid AND active ORDER BY bandate ASC LIMIT 1")
    Map<String, Object> selectPInfoBans(@Param("guid") long guid);

    @Query(value = "SELECT SUM(CASE WHEN (checked & 1) THEN 1 ELSE 0 END) AS 'readmail', COUNT(*) AS 'totalmail' FROM mail WHERE `receiver` = :receiver")
    Map<String, Object> selectPInfoMails(@Param("receiver") long receiver);

    @Query(value = "SELECT a.xp, b.guid FROM characters a LEFT JOIN guild_member b ON a.guid = b.guid WHERE a.guid = :guid")
    Map<String, Object> selectPInfoXp(@Param("guid") long guid);

    @Query(value = "SELECT mapId, zoneId, posX, posY, posZ, orientation FROM character_homebind WHERE guid = :guid")
    Map<String, Object> selectCharacterHomebind(@Param("guid") long guid);

    @Query(value = "SELECT name, race, class, gender, at_login FROM characters WHERE guid = :guid")
    Map<String, Object> selectCharacterCustomizeInfo(@Param("guid") long guid);

    @Query(value = "SELECT c.at_login, c.knownTitles, gm.guid FROM characters c LEFT JOIN group_member gm ON c.guid = gm.memberGuid WHERE c.guid = :guid")
    Map<String, Object> selectCharacterRaceOrFactionChangeInfos(@Param("guid") long guid);

    @Query(value = "SELECT id, messageType, mailTemplateId, sender, subject, body, money, has_items FROM mail WHERE receiver = :receiver AND has_items <> O AND cod <> O")
    List<Map<String, Object>> selectCharacterCodItemMail(@Param("receiver") long receiver);

    @Query(value = "SELECT DISTINCT guid FROM character_social WHERE friend = :friend")
    List<Long> selectCharacterSocial(@Param("friend") long friend);

    @Query(value = "SELECT guid, deleteInfos_Account FROM characters WHERE deleteDate IS NOT NULL AND deleteDate < :deleteDate")
    List<Map<String, Object>> selectCharacterOldChars(@Param("deleteDate") long deleteDate);

    @Query(value = "SELECT id, messageType, sender, receiver, subject, body, expire_time, deliver_time, money, cod, checked, stationery, mailTemplateId FROM mail WHERE receiver = :receiver ORDER BY id DESC")
    List<Map<String, Object>> selectMail(@Param("receiver") long receiver);

    @Modifying
    @Query(value = "DELETE FROM character_aura WHERE spell = 9454 AND guid = :guid")
    void deleteCharacterAuraFrozen(@Param("guid") long guid);

    @Query(value = "SELECT COUNT(itemEntry) FROM character_inventory ci INNER JOIN item_instance ii ON ii.guid = ci.item WHERE itemEntry = :itemEntry")
    Integer selectCharacterInventoryCountItem(@Param("itemEntry") int itemEntry);

    @Query(value = "SELECT COUNT(itemEntry) FROM mail_items mi INNER JOIN item_instance ii ON ii.guid = mi.item_guid WHERE itemEntry = :itemEntry")
    Integer selectMailCountItem(@Param("itemEntry") int itemEntry);

    @Query(value = "SELECT COUNT(*) FROM auction_items ai INNER JOIN item_instance ii ON ii.guid = ai.itemGuid WHERE ii.itemEntry = :itemEntry")
    Integer selectAuctionhouseCountItem(@Param("itemEntry") int itemEntry);

    @Query(value = "SELECT COUNT(itemEntry) FROM guild_bank_item gbi INNER JOIN item_instance ii ON ii.guid = gbi.item_guid WHERE itemEntry = :itemEntry")
    Integer selectGuildBankCountItem(@Param("itemEntry") int itemEntry);

    @Query(value = "SELECT ci.item, cb.slot AS bag, ci.slot, ci.guid, c.account, c.name FROM characters c INNER JOIN character_inventory ci ON ci.guid = c.guid INNER JOIN item_instance ii ON ii.guid = ci.item LEFT JOIN character_inventory cb ON cb.item = ci.bag WHERE ii.itemEntry = :itemEntry LIMIT :limit")
    List<Map<String, Object>> selectCharacterInventoryItemByEntry(@Param("itemEntry") int itemEntry, @Param("limit") int limit);

    @Query(value = "SELECT mi.item_guid, m.sender, m.receiver, cs.account AS sender_account, cs.name AS sender_name, cr.account AS receiver_account, cr.name AS receiver_name FROM mail m INNER JOIN mail_items mi ON mi.mail_id = m.id INNER JOIN item_instance ii ON ii.guid = mi.item_guid INNER JOIN characters cs ON cs.guid = m.sender INNER JOIN characters cr ON cr.guid = m.receiver WHERE ii.itemEntry = :itemEntry LIMIT :limit")
    List<Map<String, Object>> selectMailItemsByEntry(@Param("itemEntry") int itemEntry, @Param("limit") int limit);

    @Query(value = "SELECT ai.itemGuid, c.guid, c.account, c.name FROM auctionhouse ah INNER JOIN auction_items ai ON ah.id = ai.auctionId INNER JOIN characters c ON c.guid = ah.owner INNER JOIN item_instance ii ON ii.guid = ai.itemGuid WHERE ii.itemEntry = :itemEntry LIMIT :limit")
    List<Map<String, Object>> selectAuctionhouseItemByEntry(@Param("itemEntry") int itemEntry, @Param("limit") int limit);

    @Query(value = "SELECT gi.item_guid, gi.guildid, g.name FROM guild_bank_item gi INNER JOIN guild g ON g.guildid = gi.guildid INNER JOIN item_instance ii ON ii.guid = gi.item_guid WHERE ii.itemEntry = :itemEntry LIMIT :limit")
    List<Map<String, Object>> selectGuildBankItemByEntry(@Param("itemEntry") int itemEntry, @Param("limit") int limit);

    @Modifying
    @Query(value = "DELETE FROM character_achievement WHERE guid = :guid")
    void deleteCharacterAchievement(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_achievement_progress WHERE guid = :guid")
    void deleteCharacterAchievementProgress(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_achievement (guid, achievement, date) VALUES (:guid, :achievement, :date)")
    void insertCharacterAchievement(@Param("guid") long guid, @Param("achievement") int achievement, @Param("date") long date);

    @Modifying
    @Query(value = "DELETE FROM character_achievement_progress WHERE guid = :guid AND criteria = :criteria")
    void deleteCharacterAchievementProgressByCriteria(@Param("guid") long guid, @Param("criteria") int criteria);

    @Modifying
    @Query(value = "INSERT INTO character_achievement_progress (guid, criteria, counter, date) VALUES (:guid, :criteria :counter :date)")
    void insertCharacterAchievementProgress(@Param("guid") long guid, @Param("criteria") int criteria, @Param("counter") int counter, @Param("date") long date);

    @Modifying
    @Query(value = "DELETE FROM character_reputation WHERE guid = :guid AND faction = :faction")
    void deleteCharacterReputationByFaction(@Param("guid") long guid, @Param("faction") int faction);

    @Modifying
    @Query(value = "INSERT INTO character_reputation (guid faction, standing, flags) VALUES (:guid, :faction, :standing, :flags)")
    void insertCharacterReputationByFaction(@Param("guid") long guid, @Param("faction") int faction, @Param("standing") int standing, @Param("flags") int flags);

    @Modifying
    @Query(value = "DELETE FROM item_refund_instance WHERE item_guid = :itemGuid")
    void deleteItemRefundInstance(@Param("itemGuid") long itemGuid);

    @Modifying
    @Query(value = "INSERT INTO item_refund_instance (item_guid, player_guid, paidMoney, paidExtendedCost) VALUES (:itemGuid, :playerGuid, :paidMoney, :paidExtendedCost)")
    void insertItemRefundInstance(@Param("itemGuid") long itemGuid, @Param("playerGuid") long playerGuid, @Param("paidMoney") long paidMoney, @Param("paidExtendedCost") int paidExtendedCost);

    @Modifying
    @Query(value = "DELETE FROM `groups` WHERE guid = :guid")
    void deleteGroup(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM group_member WHERE guid = :guid")
    void deleteGroupMemberAll(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_gifts (guid, item_guid, entry, flags) VALUES (:guid, :itemGuid, :entry, :flags)")
    void insertCharacterGift(@Param("guid") long guid, @Param("itemGuid") long itemGuid, @Param("entry") int entry, @Param("flags") int flags);

    @Modifying
    @Query(value = "DELETE FROM mail_items WHERE mail_id = :mailId")
    void deleteMailItemById(@Param("mailId") long mailId);

    @Modifying
    @Query(value = "INSERT INTO petition (ownerguid, petitionguid, name) VALUES (:ownerGuid, :petitionGuid, :name)")
    void insertPetition(@Param("ownerGuid") long ownerGuid, @Param("petitionGuid") long petitionGuid, @Param("name") String name);

    @Modifying
    @Query(value = "DELETE FROM petition WHERE petitionguid = :petitionGuid")
    void deletePetitionByGuid(@Param("petitionGuid") long petitionGuid);

    @Modifying
    @Query(value = "DELETE FROM petition_sign WHERE petitionguid = :petitionGuid")
    void deletePetitionSignatureByGuid(@Param("petitionGuid") long petitionGuid);

    @Modifying
    @Query(value = "DELETE FROM character_declinedname WHERE guid = :guid")
    void deleteCharacterDeclinednameByGuid(@Param("guid") long guid);

    @Query(value = "SELECT genitive, dative, accusative, instrumental, prepositional FROM character_declinedname WHERE guid = :guid")
    CharacterDeclinedname selectCharacterDeclinednameByGuid(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_declinedname (guid, genitive, dative, accusative, instrumental, prepositional) VALUES (:guid, :genitive, :dative, :accusative, :instrumental, :prepositional)")
    void insertCharacterDeclinedName(@Param("guid") long guid, @Param("genitive") String genitive, @Param("dative") String dative, @Param("accusative") String accusative, @Param("instrumental") String instrumental, @Param("prepositional") String prepositional);

    @Modifying
    @Query(value = "UPDATE characters SET race = :race, extra_flags = extra_flags | :extraFlags WHERE guid = :guid")
    void updateCharacterRace(@Param("race") int race, @Param("extraFlags") int extraFlags, @Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_skills WHERE skill IN (98, 113, 759, 111, 313, 109, 115, 315, 673, 137) AND guid = :guid")
    void deleteCharacterSkillLanguages(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO `character_skills` (guid, skill, value, max) VALUES (:guid, :skill, 300, 300)")
    void insertCharacterSkillLanguage(@Param("guid") long guid, @Param("skill") int skill);

    @Modifying
    @Query(value = "UPDATE characters SET taxi_path = '' WHERE guid = :guid")
    void updateCharacterTaxiPath(@Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE characters SET taximask = :taximask WHERE guid = :guid")
    void updateCharacterTaximask(@Param("taximask") long taximask, @Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus WHERE guid = :guid")
    void deleteCharacterQueststatus(@Param("guid") long guid);

    @Query(value = "SELECT quest, status, explored, acceptTime, endTime FROM character_queststatus WHERE guid = :guid AND status <> 0")
    List<CharacterQueststatus> selectCharacterQueststatus(@Param("guid") long guid);

    @Query(value = "SELECT quest, time FROM character_queststatus_daily WHERE guid = :guid")
    List<CharacterQueststatusDaily> selectCharacterQueststatusDaily(@Param("guid") long guid);

    @Query(value = "SELECT quest, active FROM character_queststatus_rewarded WHERE guid = :guid")
    List<CharacterQueststatusRewarded> selectCharacterQueststatusRewarded(@Param("guid") long guid);

    @Query(value = "SELECT quest, event, completedTime FROM character_queststatus_seasonal WHERE guid = :guid")
    List<CharacterQueststatusSeasonal> selectCharacterQueststatusSeasonal(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_queststatus_seasonal (guid, quest, event, completedTime) VALUES (:guid, :quest, :event, :completedTime)")
    void insertCharacterQueststatusSeasonal(@Param("guid") long guid, @Param("quest") int quest, @Param("event") int event, @Param("completedTime") long completedTime);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_seasonal WHERE guid = :guid")
    void deleteCharacterQueststatusSeasonalByGuid(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_seasonal WHERE event = :event AND completedTime < :completedTime")
    void deleteCharacterQueststatusSeasonalByEventAndTime(@Param("event") int event, @Param("completedTime") long completedTime);

    @Query(value = "SELECT quest FROM character_queststatus_monthly WHERE guid = :guid")
    List<Integer> selectCharacterQueststatusMonthly(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_queststatus_monthly (guid, quest) VALUES (:guid, :quest)")
    void insertCharacterQueststatusMonthly(@Param("guid") long guid, @Param("quest") int quest);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_monthly WHERE guid = :guid")
    void deleteCharacterQueststatusMonthlyByGuid(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_monthly")
    void deleteAllCharacterQueststatusMonthly();

    @Query(value = "SELECT quest FROM character_queststatus_weekly WHERE guid = :guid")
    List<Integer> selectCharacterQueststatusWeekly(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_queststatus_weekly (guid, quest) VALUES (:guid, :quest)")
    void insertCharacterQueststatusWeekly(@Param("guid") long guid, @Param("quest") int quest);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_weekly WHERE guid = :guid")
    void deleteCharacterQueststatusWeeklyByGuid(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_weekly")
    void deleteAllCharacterQueststatusWeekly();

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_objectives WHERE guid = :guid")
    void deleteCharacterQueststatusObjectives(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_objectives_criteria WHERE guid = :guid")
    void deleteCharacterQueststatusObjectivesCriteria(@Param("guid") long guid);

    @Query(value = "SELECT questObjectiveId FROM character_queststatus_objectives_criteria WHERE guid = :guid")
    List<Integer> selectCharacterQueststatusObjectivesCriteria(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_objectives_criteria_progress WHERE guid = :guid")
    void deleteCharacterQueststatusObjectivesCriteriaProgress(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_objectives_criteria_progress WHERE guid = :guid AND criteriaId = :criteriaId")
    void deleteCharacterQueststatusObjectivesCriteriaProgressByGuidAndCriteriaId(@Param("guid") long guid, @Param("criteriaId") int criteriaId);

    @Query(value = "SELECT criteriaId, counter, date FROM character_queststatus_objectives_criteria_progress WHERE guid = :guid")
    List<CharacterQueststatusObjectivesCriteriaProgress> selectCharacterQueststatusObjectivesCriteriaProgress(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_social WHERE guid = :guid")
    void deleteCharacterSocialByGuid(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_social WHERE friend = :friend")
    void deleteCharacterSocialByFriend(@Param("friend") long friend);

    @Modifying
    @Query(value = "DELETE FROM character_achievement WHERE achievement = :achievement AND guid = :guid")
    void deleteCharacterAchievementByAchievement(@Param("achievement") int achievement, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE character_achievement SET achievement = :newAchievement WHERE achievement = :oldAchievement AND guid = :guid")
    void updateCharacterAchievement(@Param("newAchievement") int newAchievement, @Param("oldAchievement") int oldAchievement, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE item_instance ii, character_inventory ci SET ii.itemEntry = :newItemEntry WHERE ii.itemEntry = :oldItemEntry AND ci.guid = :guid AND ci.item = ii.guid")
    void updateCharacterInventoryFactionChange(@Param("newItemEntry") int newItemEntry, @Param("oldItemEntry") int oldItemEntry, @Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_spell WHERE spell = :spell AND guid = :guid")
    void deleteCharacterSpellBySpell(@Param("spell") int spell, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE character_spell SET spell = :newSpell WHERE spell = :oldSpell AND guid = :guid")
    void updateCharacterSpellFactionChange(@Param("newSpell") int newSpell, @Param("oldSpell") int oldSpell, @Param("guid") long guid);

    @Query(value = "SELECT standing FROM character_reputation WHERE faction = :faction AND guid = :guid")
    Integer selectCharacterRepByFaction(@Param("faction") int faction, @Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_reputation WHERE faction = :faction AND guid = :guid")
    void deleteCharacterRepByFaction(@Param("faction") int faction, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE character_reputation SET faction = :newFaction, standing = :standing WHERE faction = :oldFaction AND guid = :guid")
    void updateCharacterRepFactionChange(@Param("newFaction") int newFaction, @Param("standing") int standing, @Param("oldFaction") int oldFaction, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE characters SET knownTitles = :knownTitles WHERE guid = :guid")
    void updateCharacterTitlesFactionChange(@Param("knownTitles") long knownTitles, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE characters SET chosenTitle = 0 WHERE guid = :guid")
    void resetCharacterTitlesFactionChange(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_spell_cooldown WHERE guid = :guid")
    void deleteCharacterSpellCooldowns(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_spell_cooldown (guid, spell, item, time, categoryId, categoryEnd) VALUES (:guid, :spell, :item, :time, :categoryId, :categoryEnd)")
    void insertCharacterSpellCooldown(@Param("guid") long guid, @Param("spell") int spell, @Param("item") long item, @Param("time") long time, @Param("categoryId") int categoryId, @Param("categoryEnd") long categoryEnd);

    @Modifying
    @Query(value = "DELETE FROM character_spell_charges WHERE guid = :guid")
    void deleteCharacterSpellCharges(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_spell_charges (guid, categoryId, rechargeStart, rechargeEnd) VALUES (:guid, :categoryId, :rechargeStart, :rechargeEnd)")
    void insertCharacterSpellCharges(@Param("guid") long guid, @Param("categoryId") int categoryId, @Param("rechargeStart") long rechargeStart, @Param("rechargeEnd") long rechargeEnd);

    @Modifying
    @Query(value = "DELETE FROM characters WHERE guid = :guid")
    void deleteCharacter(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_action WHERE guid = :guid")
    void deleteCharacterAction(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_aura WHERE guid = :guid")
    void deleteCharacterAura(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_aura_effect WHERE guid = :guid")
    void deleteCharacterAuraEffect(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_gifts WHERE guid = :guid")
    void deleteCharacterGift(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_inventory WHERE guid = :guid")
    void deleteCharacterInventory(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_rewarded WHERE guid = :guid")
    void deleteCharacterQueststatusRewarded(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_reputation WHERE guid = :guid")
    void deleteCharacterReputation(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_spell WHERE guid = :guid")
    void deleteCharacterSpell(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM mail WHERE receiver = :receiver")
    void deleteMail(@Param("receiver") long receiver);

    @Modifying
    @Query(value = "DELETE FROM mail_items WHERE receiver = :receiver")
    void deleteMailItems(@Param("receiver") long receiver);

    @Modifying
    @Query(value = "DELETE FROM character_achievement WHERE guid = :guid AND achievement NOT IN (456,457,458,459,460,461,462,463,464,465,466,467,1400,1402,1404,1405,1406,1407,1408,1409,1410,1411,1412,1413,1414,1415,1416,1417,1418,1419,1420,1421,1422,1423,1424,1425,1426,1427,1463,3117,3259,4078,4576,4998,4999,5000,5001,5002,5003,5004,5005,5006,5007,5008,5381,5382,5383,5384,5385,5386,5387,5388,5389,5390,5391,5392,5393,5394,5395,5396,6433,6523,6524,6743,6744,6745,6746,6747,6748,6749,6750,6751,6752,6829,6859,6860,6861,6862,6863,6864,6865,6866,6867,6868,6869,6870,6871,6872,6873)")
    void deleteCharacterAchievements(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_equipmentsets WHERE guid = :guid")
    void deleteCharacterEquipmentsets(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_transmog_outfits WHERE guid = :guid")
    void deleteCharacterTransmogOutfits(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM guild_eventlog WHERE PlayerGuid1 = :playerGuid OR PlayerGuid2 = :playerGuid")
    void deleteGuildEventlogByPlayer(@Param("playerGuid") long playerGuid);

    @Modifying
    @Query(value = "DELETE FROM guild_bank_eventlog WHERE PlayerGuid = :playerGuid")
    void deleteGuildBankEventlogByPlayer(@Param("playerGuid") long playerGuid);

    @Modifying
    @Query(value = "DELETE FROM character_glyphs WHERE guid = :guid")
    void deleteCharacterGlyphs(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_talent WHERE guid = :guid")
    void deleteCharacterTalent(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_talent_group WHERE guid = :guid")
    void deleteCharacterTalentGroup(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_skills WHERE guid = :guid")
    void deleteCharacterSkills(@Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE characters SET money = :money WHERE guid = :guid")
    void updateCharacterMoney(@Param("money") long money, @Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_action (guid, spec, traitConfigId, button, action, type) VALUES (:guid, :spec, :traitConfigId, :button, :action, :type)")
    void insertCharacterAction(@Param("guid") long guid, @Param("spec") int spec, @Param("traitConfigId") int traitConfigId, @Param("button") int button, @Param("action") int action, @Param("type") int type);

    @Modifying
    @Query(value = "UPDATE character_action SET action = :action, type = :type WHERE guid = :guid AND button = :button AND spec = :spec AND traitConfigId = :traitConfigId")
    void updateCharacterAction(@Param("action") int action, @Param("type") int type, @Param("guid") long guid, @Param("button") int button, @Param("spec") int spec, @Param("traitConfigId") int traitConfigId);

    @Modifying
    @Query(value = "DELETE FROM character_action WHERE guid = :guid AND button = :button AND spec = :spec AND traitConfigId = :traitConfigId")
    void deleteCharacterActionByButtonSpec(@Param("guid") long guid, @Param("button") int button, @Param("spec") int spec, @Param("traitConfigId") int traitConfigId);

    @Modifying
    @Query(value = "DELETE FROM character_inventory WHERE item = :item")
    void deleteCharacterInventoryByItem(@Param("item") long item);

    @Modifying
    @Query(value = "DELETE FROM character_inventory WHERE bag = :bag AND slot = :slot AND guid = :guid")
    void deleteCharacterInventoryByBagSlot(@Param("bag") long bag, @Param("slot") int slot, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE mail SET has_items = :hasItems, expire_time = :expireTime, deliver_time = :deliverTime, money = :money, cod = :cod, checked = :checked WHERE id = :id")
    void updateMail(@Param("hasItems") int hasItems, @Param("expireTime") long expireTime, @Param("deliverTime") long deliverTime, @Param("money") long money, @Param("cod") long cod, @Param("checked") int checked, @Param("id") long id);

    @Modifying
    @Query(value = "REPLACE INTO character_queststatus (guid, quest, status, explored, acceptTime, endTime) VALUES (:guid, :quest, :status, :explored, :acceptTime, :endTime)")
    void replaceCharacterQueststatus(@Param("guid") long guid, @Param("quest") int quest, @Param("status") int status, @Param("explored") int explored, @Param("acceptTime") long acceptTime, @Param("endTime") long endTime);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus WHERE guid = :guid AND quest = :quest")
    void deleteCharacterQueststatusByQuest(@Param("guid") long guid, @Param("quest") int quest);

    @Modifying
    @Query(value = "REPLACE INTO character_queststatus_objectives (guid, quest, objective, data) VALUES (:guid, :quest, :objective, :data)")
    void replaceCharacterQueststatusObjectives(@Param("guid") long guid, @Param("quest") int quest, @Param("objective") int objective, @Param("data") int data);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_objectives WHERE guid = :guid AND quest = :quest")
    void deleteCharacterQueststatusObjectivesByQuest(@Param("guid") long guid, @Param("quest") int quest);

    @Query(value = "SELECT quest, objective, data FROM character_queststatus_objectives WHERE guid = :guid")
    List<CharacterQueststatusObjective> selectCharacterQueststatusObjectives(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_queststatus_objectives_criteria (guid, questObjectiveId) VALUES (:guid, :questObjectiveId)")
    void insertCharacterQueststatusObjectivesCriteria(@Param("guid") long guid, @Param("questObjectiveId") int questObjectiveId);

    @Modifying
    @Query(value = "INSERT INTO character_queststatus_objectives_criteria_progress (guid, criteriaId, counter, date) VALUES (:guid, :criteriaId, :counter, :date)")
    void insertCharacterQueststatusObjectivesCriteriaProgress(@Param("guid") long guid, @Param("criteriaId") int criteriaId, @Param("counter") int counter, @Param("date") long date);

    @Modifying
    @Query(value = "INSERT IGNORE INTO character_queststatus_rewarded (guid, quest, active) VALUES (:guid, :quest, 1)")
    void insertCharacterQueststatusRewarded(@Param("guid") long guid, @Param("quest") int quest);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_rewarded WHERE guid = :guid AND quest = :quest")
    void deleteCharacterQueststatusRewardedByQuest(@Param("guid") long guid, @Param("quest") int quest);

    @Modifying
    @Query(value = "UPDATE character_queststatus_rewarded SET quest = :newQuest WHERE quest = :oldQuest AND guid = :guid")
    void updateCharacterQueststatusRewardedFactionChange(@Param("newQuest") int newQuest, @Param("oldQuest") int oldQuest, @Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE character_queststatus_rewarded SET active = 1 WHERE guid = :guid")
    void updateCharacterQueststatusRewardedActive(@Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE character_queststatus_rewarded SET active = 0 WHERE quest = :quest AND guid = :guid")
    void updateCharacterQueststatusRewardedActiveByQuest(@Param("quest") int quest, @Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_queststatus_objectives_criteria WHERE questObjectiveId = :questObjectiveId")
    void deleteInvalidQuestProgressCriteria(@Param("questObjectiveId") int questObjectiveId);

    @Modifying
    @Query(value = "DELETE FROM character_skills WHERE guid = :guid AND skill = :skill")
    void deleteCharacterSkillBySkill(@Param("guid") long guid, @Param("skill") int skill);

    @Modifying
    @Query(value = "INSERT INTO character_skills (guid, skill, value, max, professionSlot) VALUES (:guid, :skill, :value, :max, :professionSlot)")
    void insertCharacterSkills(@Param("guid") long guid, @Param("skill") int skill, @Param("value") int value, @Param("max") int max, @Param("professionSlot") int professionSlot);

    @Modifying
    @Query(value = "UPDATE character_skills SET value = :value, max = :max, professionSlot = :professionSlot WHERE guid = :guid AND skill = :skill")
    void updateCharacterSkills(@Param("value") int value, @Param("max") int max, @Param("professionSlot") int professionSlot, @Param("guid") long guid, @Param("skill") int skill);

    @Modifying
    @Query(value = "INSERT INTO character_spell (guid, spell, active, disabled) VALUES (:guid, :spell, :active, :disabled)")
    void insertCharacterSpell(@Param("guid") long guid, @Param("spell") int spell, @Param("active") int active, @Param("disabled") int disabled);

    @Modifying
    @Query(value = "DELETE FROM character_spell_favorite WHERE guid = :guid AND spell = :spell")
    void deleteCharacterSpellFavorite(@Param("guid") long guid, @Param("spell") int spell);

    @Modifying
    @Query(value = "DELETE FROM character_spell_favorite WHERE guid = :guid")
    void deleteCharacterSpellFavoriteByChar(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_spell_favorite (guid, spell) VALUES (:guid, :spell)")
    void insertCharacterSpellFavorite(@Param("guid") long guid, @Param("spell") int spell);

    @Modifying
    @Query(value = "DELETE FROM character_stats WHERE guid = :guid")
    void deleteCharacterStats(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_stats (guid, maxhealth, maxpower1, maxpower2, maxpower3, maxpower4, maxpower5, maxpower6, maxpower7, maxpower8, maxpower9, maxpower10, strength, agility, stamina, intellect, armor, resHoly, resFire, resNature, resFrost, resShadow, resArcane, blockPct, dodgePct, parryPct, critPct, rangedCritPct, spellCritPct, attackPower, rangedAttackPower, spellPower, resilience, mastery, versatility) VALUES (:guid, :maxhealth, :maxpower1, :maxpower2, :maxpower3, :maxpower4, :maxpower5, :maxpower6, :maxpower7, :maxpower8, :maxpower9, :maxpower10, :strength, :agility, :stamina, :intellect, :armor, :resHoly, :resFire, :resNature, :resFrost, :resShadow, :resArcane, :blockPct, :dodgePct, :parryPct, :critPct, :rangedCritPct, :spellCritPct, :attackPower, :rangedAttackPower, :spellPower, :resilience, :mastery, :versatility)")
    void insertCharacterStats(@Param("guid") long guid, @Param("maxhealth") int maxhealth, @Param("maxpower1") int maxpower1, @Param("maxpower2") int maxpower2, @Param("maxpower3") int maxpower3, @Param("maxpower4") int maxpower4, @Param("maxpower5") int maxpower5, @Param("maxpower6") int maxpower6, @Param("maxpower7") int maxpower7, @Param("maxpower8") int maxpower8, @Param("maxpower9") int maxpower9, @Param("maxpower10") int maxpower10, @Param("strength") int strength, @Param("agility") int agility, @Param("stamina") int stamina, @Param("intellect") int intellect, @Param("armor") int armor, @Param("resHoly") int resHoly, @Param("resFire") int resFire, @Param("resNature") int resNature, @Param("resFrost") int resFrost, @Param("resShadow") int resShadow, @Param("resArcane") int resArcane, @Param("blockPct") float blockPct, @Param("dodgePct") float dodgePct, @Param("parryPct") float parryPct, @Param("critPct") float critPct, @Param("rangedCritPct") float rangedCritPct, @Param("spellCritPct") float spellCritPct, @Param("attackPower") int attackPower, @Param("rangedAttackPower") int rangedAttackPower, @Param("spellPower") int spellPower, @Param("resilience") int resilience, @Param("mastery") float mastery, @Param("versatility") float versatility);

    @Modifying
    @Query(value = "DELETE FROM petition WHERE ownerguid = :ownerguid")
    void deletePetitionByOwner(@Param("ownerguid") long ownerguid);

    @Modifying
    @Query(value = "DELETE FROM petition_sign WHERE ownerguid = :ownerguid")
    void deletePetitionSignatureByOwner(@Param("ownerguid") long ownerguid);

    @Modifying
    @Query(value = "INSERT INTO character_glyphs (guid, talentGroup, glyphSlot, glyphId) VALUES (:guid, :talentGroup, :glyphSlot, :glyphId)")
    void insertCharacterGlyphs(@Param("guid") long guid, @Param("talentGroup") int talentGroup, @Param("glyphSlot") int glyphSlot, @Param("glyphId") int glyphId);

    @Modifying
    @Query(value = "INSERT INTO character_talent (guid, talentGroup, talentId, `rank`) VALUES (:guid, :talentGroup, :talentId, :rank)")
    void insertCharacterTalent(@Param("guid") long guid, @Param("talentGroup") int talentGroup, @Param("talentId") int talentId, @Param("rank") int rank);

    @Modifying
    @Query(value = "INSERT INTO character_talent_group (guid, id, talentTabId) VALUES (:guid, :id, :talentTabId)")
    void insertCharacterTalentGroup(@Param("guid") long guid, @Param("id") int id, @Param("talentTabId") int talentTabId);

    @Modifying
    @Query(value = "UPDATE characters SET slot = :slot WHERE guid = :guid AND account = :account")
    void updateCharacterListSlot(@Param("slot") int slot, @Param("guid") long guid, @Param("account") long account);

    @Modifying
    @Query(value = "INSERT INTO character_fishingsteps (guid, fishingSteps) VALUES (:guid, :fishingSteps)")
    void insertCharacterFishingsteps(@Param("guid") long guid, @Param("fishingSteps") int fishingSteps);

    @Modifying
    @Query(value = "DELETE FROM character_fishingsteps WHERE guid = :guid")
    void deleteCharacterFishingsteps(@Param("guid") long guid);

    @Query(value = "SELECT itemId, itemEntry, slot, creatorGuid, randomBonusListId, fixedScalingLevel, artifactKnowledgeLevel, context, bonusListIDs FROM character_void_storage WHERE playerGuid = :playerGuid")
    List<Map<String, Object>> selectCharacterVoidStorage(@Param("playerGuid") long playerGuid);

    @Modifying
    @Query(value = "REPLACE INTO character_void_storage (itemId, playerGuid, itemEntry, slot, creatorGuid, randomBonusListId, fixedScalingLevel, artifactKnowledgeLevel, context, bonusListIDs) VALUES (:itemId, :playerGuid, :itemEntry, :slot, :creatorGuid, :randomBonusListId, :fixedScalingLevel, :artifactKnowledgeLevel, :context, :bonusListIDs)")
    void replaceCharacterVoidStorageItem(@Param("itemId") long itemId, @Param("playerGuid") long playerGuid, @Param("itemEntry") int itemEntry, @Param("slot") int slot, @Param("creatorGuid") long creatorGuid, @Param("randomBonusListId") int randomBonusListId, @Param("fixedScalingLevel") int fixedScalingLevel, @Param("artifactKnowledgeLevel") int artifactKnowledgeLevel, @Param("context") int context, @Param("bonusListIDs") String bonusListIDs);

    @Modifying
    @Query(value = "DELETE FROM character_void_storage WHERE playerGuid = :playerGuid")
    void deleteCharacterVoidStorageItemByCharGuid(@Param("playerGuid") long playerGuid);

    @Modifying
    @Query(value = "DELETE FROM character_void_storage WHERE slot = :slot AND playerGuid = :playerGuid")
    void deleteCharacterVoidStorageItemBySlot(@Param("slot") int slot, @Param("playerGuid") long playerGuid);

    @Query(value = "SELECT id, name, frameHeight, frameWidth, sortBy, healthText, boolOptions, topPoint, bottomPoint, leftPoint, topOffset, bottomOffset, leftOffset FROM character_cuf_profiles WHERE guid = :guid")
    List<Map<String, Object>> selectCharacterCufProfiles(@Param("guid") long guid);

    @Modifying
    @Query(value = "REPLACE INTO character_cuf_profiles (guid, id, name, frameHeight, frameWidth, sortBy, healthText, boolOptions, topPoint, bottomPoint, leftPoint, topOffset, bottomOffset, leftOffset) VALUES (:guid, :id, :name, :frameHeight, :frameWidth, :sortBy, :healthText, :boolOptions, :topPoint, :bottomPoint, :leftPoint, :topOffset, :bottomOffset, :leftOffset)")
    void replaceCharacterCufProfiles(@Param("guid") long guid, @Param("id") int id, @Param("name") String name, @Param("frameHeight") int frameHeight, @Param("frameWidth") int frameWidth, @Param("sortBy") int sortBy, @Param("healthText") int healthText, @Param("boolOptions") int boolOptions, @Param("topPoint") int topPoint, @Param("bottomPoint") int bottomPoint, @Param("leftPoint") int leftPoint, @Param("topOffset") int topOffset, @Param("bottomOffset") int bottomOffset, @Param("leftOffset") int leftOffset);

    @Modifying
    @Query(value = "DELETE FROM character_cuf_profiles WHERE guid = :guid AND id = :id")
    void deleteCharacterCufProfilesById(@Param("guid") long guid, @Param("id") int id);

    @Modifying
    @Query(value = "DELETE FROM character_cuf_profiles WHERE guid = :guid")
    void deleteCharacterCufProfiles(@Param("guid") long guid);

    @Query(value = "SELECT container_id, item_type, item_id, item_count, item_index, follow_rules, ffa, blocked, counted, under_threshold, needs_quest, rnd_bonus, context, bonus_list_ids FROM item_loot_items")
    List<Map<String, Object>> selectItemcontainerItems();

    @Modifying
    @Query(value = "DELETE FROM item_loot_items WHERE container_id = :containerId")
    void deleteItemcontainerItems(@Param("containerId") long containerId);

    @Modifying
    @Query(value = "DELETE FROM item_loot_items WHERE container_id = :containerId AND item_type = :itemType AND item_id = :itemId AND item_count = :itemCount AND item_index = :itemIndex")
    void deleteItemcontainerItem(@Param("containerId") long containerId, @Param("itemType") int itemType, @Param("itemId") long itemId, @Param("itemCount") int itemCount, @Param("itemIndex") int itemIndex);

    @Modifying
    @Query(value = "INSERT INTO item_loot_items (container_id, item_type, item_id, item_count, item_index, follow_rules, ffa, blocked, counted, under_threshold, needs_quest, rnd_bonus, context, bonus_list_ids) VALUES (:containerId, :itemType, :itemId, :itemCount, :itemIndex, :followRules, :ffa, :blocked, :counted, :underThreshold, :needsQuest, :rndBonus, :context, :bonusListIds)")
    void insertItemcontainerItems(@Param("containerId") long containerId, @Param("itemType") int itemType, @Param("itemId") long itemId, @Param("itemCount") int itemCount, @Param("itemIndex") int itemIndex, @Param("followRules") int followRules, @Param("ffa") int ffa, @Param("blocked") int blocked, @Param("counted") int counted, @Param("underThreshold") int underThreshold, @Param("needsQuest") int needsQuest, @Param("rndBonus") int rndBonus, @Param("context") int context, @Param("bonusListIds") String bonusListIds);

    @Query(value = "SELECT container_id, money FROM item_loot_money")
    List<Map<String, Object>> selectItemcontainerMoney();

    @Modifying
    @Query(value = "DELETE FROM item_loot_money WHERE container_id = :containerId")
    void deleteItemcontainerMoney(@Param("containerId") long containerId);

    @Modifying
    @Query(value = "INSERT INTO item_loot_money (container_id, money) VALUES (:containerId, :money)")
    void insertItemcontainerMoney(@Param("containerId") long containerId, @Param("money") long money);

    @Modifying
    @Query(value = "REPLACE INTO calendar_events (EventID, Owner, Title, Description, EventType, TextureID, Date, Flags, LockDate) VALUES (:eventId, :owner, :title, :description, :eventType, :textureId, :date, :flags, :lockDate)")
    void replaceCalendarEvent(@Param("eventId") long eventId, @Param("owner") long owner, @Param("title") String title, @Param("description") String description, @Param("eventType") int eventType, @Param("textureId") int textureId, @Param("date") long date, @Param("flags") int flags, @Param("lockDate") long lockDate);

    @Modifying
    @Query(value = "DELETE FROM calendar_events WHERE EventID = :eventId")
    void deleteCalendarEvent(@Param("eventId") long eventId);

    @Modifying
    @Query(value = "REPLACE INTO calendar_invites (InviteID, EventID, Invitee, Sender, Status, ResponseTime, ModerationRank, Note) VALUES (:inviteId, :eventId, :invitee, :sender, :status, :responseTime, :moderationRank, :note)")
    void replaceCalendarInvite(@Param("inviteId") long inviteId, @Param("eventId") long eventId, @Param("invitee") long invitee, @Param("sender") long sender, @Param("status") int status, @Param("responseTime") long responseTime, @Param("moderationRank") int moderationRank, @Param("note") String note);

    @Modifying
    @Query(value = "DELETE FROM calendar_invites WHERE InviteID = :inviteId")
    void deleteCalendarInvite(@Param("inviteId") long inviteId);

    @Query(value = "SELECT id FROM character_pet WHERE owner = :owner")
    List<Long> selectCharacterPetIds(@Param("owner") long owner);

    @Modifying
    @Query(value = "DELETE FROM character_pet_declinedname WHERE owner = :owner")
    void deleteCharacterPetDeclinednameByOwner(@Param("owner") long owner);

    @Modifying
    @Query(value = "DELETE FROM character_pet_declinedname WHERE id = :id")
    void deleteCharacterPetDeclinedname(@Param("id") long id);

    @Modifying
    @Query(value = "INSERT INTO character_pet_declinedname (id, owner, genitive, dative, accusative, instrumental, prepositional) VALUES (:id, :owner, :genitive, :dative, :accusative, :instrumental, :prepositional)")
    void insertCharacterPetDeclinedname(@Param("id") long id, @Param("owner") long owner, @Param("genitive") String genitive, @Param("dative") String dative, @Param("accusative") String accusative, @Param("instrumental") String instrumental, @Param("prepositional") String prepositional);

    @Query(value = "SELECT casterGuid, spell, effectMask, recalculateMask, difficulty, stackCount, maxDuration, remainTime, remainCharges FROM pet_aura WHERE guid = :guid")
    List<Map<String, Object>> selectPetAura(@Param("guid") long guid);

    @Query(value = "SELECT casterGuid, spell, effectMask, effectIndex, amount, baseAmount FROM pet_aura_effect WHERE guid = :guid")
    List<Map<String, Object>> selectPetAuraEffect(@Param("guid") long guid);

    @Query(value = "SELECT spell, active FROM pet_spell WHERE guid = :guid")
    List<Map<String, Object>> selectPetSpell(@Param("guid") long guid);

    @Query(value = "SELECT spell, time, categoryId, categoryEnd FROM pet_spell_cooldown WHERE guid = :guid AND time > UNIX_TIMESTAMP()")
    List<Map<String, Object>> selectPetSpellCooldown(@Param("guid") long guid);

    @Query(value = "SELECT genitive, dative, accusative, instrumental, prepositional FROM character_pet_declinedname WHERE owner = :owner AND id = :id")
    Map<String, Object> selectPetDeclinedName(@Param("owner") long owner, @Param("id") long id);

    @Modifying
    @Query(value = "DELETE FROM pet_aura WHERE guid = :guid")
    void deletePetAuras(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM pet_aura_effect WHERE guid = :guid")
    void deletePetAuraEffects(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM pet_spell WHERE guid = :guid")
    void deletePetSpells(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM pet_spell_cooldown WHERE guid = :guid")
    void deletePetSpellCooldowns(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO pet_spell_cooldown (guid, spell, time, categoryId, categoryEnd) VALUES (:guid, :spell, :time, :categoryId, :categoryEnd)")
    void insertPetSpellCooldown(@Param("guid") long guid, @Param("spell") int spell, @Param("time") long time, @Param("categoryId") int categoryId, @Param("categoryEnd") long categoryEnd);

    @Query(value = "SELECT categoryId, rechargeStart, rechargeEnd FROM pet_spell_charges WHERE guid = :guid AND rechargeEnd > UNIX_TIMESTAMP() ORDER BY rechargeEnd")
    List<Map<String, Object>> selectPetSpellCharges(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM pet_spell_charges WHERE guid = :guid")
    void deletePetSpellCharges(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO pet_spell_charges (guid, categoryId, rechargeStart, rechargeEnd) VALUES (:guid, :categoryId, :rechargeStart, :rechargeEnd)")
    void insertPetSpellCharges(@Param("guid") long guid, @Param("categoryId") int categoryId, @Param("rechargeStart") long rechargeStart, @Param("rechargeEnd") long rechargeEnd);

    @Modifying
    @Query(value = "DELETE FROM pet_spell WHERE guid = :guid and spell = :spell")
    void deletePetSpellBySpell(@Param("guid") long guid, @Param("spell") int spell);

    @Modifying
    @Query(value = "INSERT INTO pet_spell (guid, spell, active) VALUES (:guid, :spell, :active)")
    void insertPetSpell(@Param("guid") long guid, @Param("spell") int spell, @Param("active") int active);

    @Modifying
    @Query(value = "INSERT INTO pet_aura (guid, casterGuid, spell, effectMask, recalculateMask, difficulty, stackCount, maxDuration, remainTime, remainCharges) VALUES (:guid, :casterGuid, :spell, :effectMask, :recalculateMask, :difficulty, :stackCount, :maxDuration, :remainTime, :remainCharges)")
    void insertPetAura(@Param("guid") long guid, @Param("casterGuid") long casterGuid, @Param("spell") int spell, @Param("effectMask") int effectMask, @Param("recalculateMask") int recalculateMask, @Param("difficulty") int difficulty, @Param("stackCount") int stackCount, @Param("maxDuration") int maxDuration, @Param("remainTime") int remainTime, @Param("remainCharges") int remainCharges);

    @Modifying
    @Query(value = "INSERT INTO pet_aura_effect (guid, casterGuid, spell, effectMask, effectIndex, amount, baseAmount) VALUES (:guid, :casterGuid, :spell, :effectMask, :effectIndex, :amount, :baseAmount)")
    void insertPetAuraEffect(@Param("guid") long guid, @Param("casterGuid") long casterGuid, @Param("spell") int spell, @Param("effectMask") int effectMask, @Param("effectIndex") int effectIndex, @Param("amount") int amount, @Param("baseAmount") int baseAmount);

    @Query(value = "SELECT id, entry, modelid, level, exp, Reactstate, slot, name, renamed, curhealth, curmana, abdata, savetime, CreatedBySpell, PetType, specialization FROM character_pet WHERE owner = :owner")
    List<Map<String, Object>> selectCharacterPets(@Param("owner") long owner);

    @Modifying
    @Query(value = "DELETE FROM character_pet WHERE owner = :owner")
    void deleteCharacterPetByOwner(@Param("owner") long owner);

    @Modifying
    @Query(value = "UPDATE character_pet SET name = :name, renamed = 1 WHERE owner = :owner AND id = :id")
    void updateCharacterPetName(@Param("name") String name, @Param("owner") long owner, @Param("id") long id);

    @Modifying
    @Query(value = "UPDATE character_pet SET slot = :slot WHERE owner = :owner AND id = :id")
    void updateCharacterPetSlotById(@Param("slot") int slot, @Param("owner") long owner, @Param("id") long id);

    @Modifying
    @Query(value = "DELETE FROM character_pet WHERE id = :id")
    void deleteCharacterPetById(@Param("id") long id);

    @Modifying
    @Query(value = "DELETE FROM pet_spell WHERE guid in (SELECT id FROM character_pet WHERE owner=:owner)")
    void deleteAllPetSpellsByOwner(@Param("owner") long owner);

    @Modifying
    @Query(value = "UPDATE character_pet SET specialization = 0 WHERE owner=:owner")
    void updatePetSpecsByOwner(@Param("owner") long owner);

    @Modifying
    @Query(value = "INSERT INTO character_pet (id, entry, owner, modelid, level, exp, Reactstate, slot, name, renamed, curhealth, curmana, abdata, savetime, CreatedBySpell, PetType, specialization) VALUES (:id, :entry, :owner, :modelid, :level, :exp, :Reactstate, :slot, :name, :renamed, :curhealth, :curmana, :abdata, :savetime, :CreatedBySpell, :PetType, :specialization)")
    void insertPet(@Param("id") long id, @Param("entry") int entry, @Param("owner") long owner, @Param("modelid") int modelid, @Param("level") int level, @Param("exp") long exp, @Param("Reactstate") int Reactstate, @Param("slot") int slot, @Param("name") String name, @Param("renamed") int renamed, @Param("curhealth") int curhealth, @Param("curmana") int curmana, @Param("abdata") String abdata, @Param("savetime") long savetime, @Param("CreatedBySpell") int CreatedBySpell, @Param("PetType") int PetType, @Param("specialization") int specialization);

    @Query(value = "SELECT MAX(id) FROM pvpstats_battlegrounds")
    Long selectPvpstatsMaxId();

    @Modifying
    @Query(value = "INSERT INTO pvpstats_battlegrounds (id, winner_faction, bracket_id, type, date) VALUES (:id, :winnerFaction, :bracketId, :type, NOW())")
    void insertPvpstatsBattleground(@Param("id") long id, @Param("winnerFaction") int winnerFaction, @Param("bracketId") int bracketId, @Param("type") int type);

    @Modifying
    @Query(value = "INSERT INTO pvpstats_players (battleground_id, character_guid, winner, score_killing_blows, score_deaths, score_honorable_kills, score_bonus_honor, score_damage_done, score_healing_done, attr_1, attr_2, attr_3, attr_4, attr_5) VALUES (:battlegroundId, :characterGuid, :winner, :scoreKillingBlows, :scoreDeaths, :scoreHonorableKills, :scoreBonusHonor, :scoreDamageDone, :scoreHealingDone, :attr1, :attr2, :attr3, :attr4, :attr5)")
    void insertPvpstatsPlayer(@Param("battlegroundId") long battlegroundId, @Param("characterGuid") long characterGuid, @Param("winner") int winner, @Param("scoreKillingBlows") int scoreKillingBlows, @Param("scoreDeaths") int scoreDeaths, @Param("scoreHonorableKills") int scoreHonorableKills, @Param("scoreBonusHonor") int scoreBonusHonor, @Param("scoreDamageDone") int scoreDamageDone, @Param("scoreHealingDone") int scoreHealingDone, @Param("attr1") int attr1, @Param("attr2") int attr2, @Param("attr3") int attr3, @Param("attr4") int attr4, @Param("attr5") int attr5);

    @Query(value = "SELECT winner_faction, COUNT(*) AS count FROM pvpstats_battlegrounds WHERE DATEDIFF(NOW(), date) < 7 GROUP BY winner_faction ORDER BY winner_faction ASC")
    List<Map<String, Object>> selectPvpstatsFactionsOverall();

    @Modifying
    @Query(value = "INSERT INTO quest_tracker (id, character_guid, quest_accept_time, core_hash, core_revision) VALUES (:id, :characterGuid, NOW(), :coreHash, :coreRevision)")
    void insertQuestTrack(@Param("id") int id, @Param("characterGuid") long characterGuid, @Param("coreHash") String coreHash, @Param("coreRevision") String coreRevision);

    @Modifying
    @Query(value = "UPDATE quest_tracker SET completed_by_gm = 1 WHERE id = :id AND character_guid = :characterGuid ORDER BY quest_accept_time DESC LIMIT 1")
    void updateQuestTrackGmComplete(@Param("id") int id, @Param("characterGuid") long characterGuid);

    @Modifying
    @Query(value = "UPDATE quest_tracker SET quest_complete_time = NOW() WHERE id = :id AND character_guid = :characterGuid ORDER BY quest_accept_time DESC LIMIT 1")
    void updateQuestTrackCompleteTime(@Param("id") int id, @Param("characterGuid") long characterGuid);

    @Modifying
    @Query(value = "UPDATE quest_tracker SET quest_abandon_time = NOW() WHERE id = :id AND character_guid = :characterGuid ORDER BY quest_accept_time DESC LIMIT 1")
    void updateQuestTrackAbandonTime(@Param("id") int id, @Param("characterGuid") long characterGuid);

    @Query(value = "SELECT spell, active, disabled FROM character_spell WHERE guid = :guid")
    List<Object[]> selectCharacterSpells(@Param("guid") int guid);

    @Query(value = "SELECT spell FROM character_spell_favorite WHERE guid = :guid")
    List<Integer> selectCharacterSpellFavorites(@Param("guid") int guid);

    @Query(value = "SELECT spell, item, time, categoryId, categoryEnd FROM character_spell_cooldown WHERE guid = :guid AND time > UNIX_TIMESTAMP()")
    List<Object[]> selectCharacterSpellCooldowns(@Param("guid") int guid);

    @Query(value = "SELECT categoryId, rechargeStart, rechargeEnd FROM character_spell_charges WHERE guid = :guid AND rechargeEnd > UNIX_TIMESTAMP() ORDER BY rechargeEnd")
    List<Object[]> selectCharacterSpellCharges(@Param("guid") int guid);

    @Query(value = "SELECT skill, value, max, professionSlot FROM character_skills WHERE guid = :guid")
    List<Object[]> selectCharacterSkills(@Param("guid") int guid);

    @Query(value = "SELECT quest FROM character_queststatus_rewarded WHERE guid = :guid AND active = 1")
    List<Integer> selectCharacterQueststatusRewarded(@Param("guid") int guid);

    @Query(value = "SELECT marketId, currentBid, time, numBids, bidder FROM blackmarket_auctions")
    List<Map<String, Object>> selectBlackmarketAuctions();

    @Modifying
    @Query(value = "DELETE FROM blackmarket_auctions WHERE marketId = :marketId")
    void deleteBlackmarketAuctions(@Param("marketId") long marketId);

    @Query(value = "SELECT ii.guid, ii.itemEntry, ii.stackCount, ii.durability, ii.charges, ii.flags, ii.enchantment, ii.randomPropertyId, ii.randomSuffix, ii.itemText, ii.ownerGuid, ii.creatorGuid, ii.giftCreatorGuid, ii.count, ii.charges_used, ii.remaining_time, ii.itemInstanceMode, ii.spellCharges, ii.bonuses, ii.context, ii.scalingLevel, ii.artifactKnowledgeLevel, ii.modifiers, ii.transmogId, ii.appearanceModId, ii.overrideAppearance, ii.newItemId, ii.bag, ii.slot FROM character_inventory ci JOIN item_instance ii ON ci.item = ii.guid LEFT JOIN item_instance_gems ig ON ii.guid = ig.itemGuid LEFT JOIN item_instance_transmog iit ON ii.guid = iit.itemGuid LEFT JOIN item_instance_modifiers im ON ii.guid = im.itemGuid WHERE ci.guid = :guid ORDER BY (ii.flags &amp; 0x80000) ASC, bag ASC, slot ASC")
    List<Map<String, Object>> selectCharacterInventory(@Param("guid") int guid);

    @Query(value = "REPLACE INTO character_inventory (guid, bag, slot, item) VALUES (:guid, :bag, :slot, :item)")
    void replaceCharacterInventory(@Param("guid") int guid, @Param("bag") int bag, @Param("slot") int slot, @Param("item") int item);

    @Query(value = "REPLACE INTO item_instance (itemEntry, owner_guid, creatorGuid, giftCreatorGuid, count, duration, charges, flags, enchantments, randomBonusListId, durability, playedTime, createTime, text, battlePetSpeciesId, battlePetBreedData, battlePetLevel, battlePetDisplayId, context, bonusListIDs, randomPropertiesId, guid) VALUES (:itemEntry, :ownerGuid, :creatorGuid, :giftCreatorGuid, :count, :duration, :charges, :flags, :enchantments, :randomBonusListId, :durability, :playedTime, :createTime, :text, :battlePetSpeciesId, :battlePetBreedData, :battlePetLevel, :battlePetDisplayId, :context, :bonusListIDs, :randomPropertiesId, :guid)")
    void replaceItemInstance(@Param("itemEntry") int itemEntry, @Param("ownerGuid") int ownerGuid, @Param("creatorGuid") int creatorGuid, @Param("giftCreatorGuid") int giftCreatorGuid, @Param("count") int count, @Param("duration") int duration, @Param("charges") String charges, @Param("flags") int flags, @Param("enchantments") String enchantments, @Param("randomBonusListId") int randomBonusListId, @Param("durability") int durability, @Param("playedTime") int playedTime, @Param("createTime") int createTime, @Param("text") String text, @Param("battlePetSpeciesId") int battlePetSpeciesId, @Param("battlePetBreedData") int battlePetBreedData, @Param("battlePetLevel") int battlePetLevel, @Param("battlePetDisplayId") int battlePetDisplayId, @Param("context") int context, @Param("bonusListIDs") String bonusListIDs, @Param("randomPropertiesId") int randomPropertiesId, @Param("guid") int guid);

    @Query(value = "UPDATE item_instance SET itemEntry = :itemEntry, owner_guid = :ownerGuid, creatorGuid = :creatorGuid, giftCreatorGuid = :giftCreatorGuid, count = :count, duration = :duration, charges = :charges, flags = :flags, enchantments = :enchantments, randomBonusListId = :randomBonusListId, durability = :durability, playedTime = :playedTime, createTime = :createTime, text = :text, battlePetSpeciesId = :battlePetSpeciesId, battlePetBreedData = :battlePetBreedData, battlePetLevel = :battlePetLevel, battlePetDisplayId = :battlePetDisplayId, context = :context, bonusListIDs = :bonusListIDs, randomPropertiesId = :randomPropertiesId WHERE guid = :guid")
    void updateItemInstance(@Param("itemEntry") int itemEntry, @Param("ownerGuid") int ownerGuid, @Param("creatorGuid") int creatorGuid, @Param("giftCreatorGuid") int giftCreatorGuid, @Param("count") int count, @Param("duration") int duration, @Param("charges") String charges, @Param("flags") int flags, @Param("enchantments") String enchantments, @Param("randomBonusListId") int randomBonusListId, @Param("durability") int durability, @Param("playedTime") int playedTime, @Param("createTime") int createTime, @Param("text") String text, @Param("battlePetSpeciesId") int battlePetSpeciesId, @Param("battlePetBreedData") int battlePetBreedData, @Param("battlePetLevel") int battlePetLevel, @Param("battlePetDisplayId") int battlePetDisplayId, @Param("context") int context, @Param("bonusListIDs") String bonusListIDs, @Param("randomPropertiesId") int randomPropertiesId, @Param("guid") int guid);
    List<Map<String, Object>> selectCharacterInventory(@Param("guid") long guid);

    @Query(value = "SELECT faction, standing, flags, rank, reputation FROM character_reputation WHERE guid = :guid")
    List<Map<String, Object>> selectCharacterReputation(@Param("guid") long guid);

    @Modifying
    @Query(value = "UPDATE blackmarket_auctions SET currentBid = :currentBid, time = :time, numBids = :numBids, bidder = :bidder WHERE marketId = :marketId")
    void updateBlackmarketAuctions(@Param("currentBid") long currentBid, @Param("time") long time, @Param("numBids") int numBids, @Param("bidder") long bidder, @Param("marketId") long marketId);

    @Modifying
    @Query(value = "INSERT INTO blackmarket_auctions (marketId, currentBid, time, numBids, bidder) VALUES (:marketId, :currentBid, :time, :numBids, :bidder)")
    void insertBlackmarketAuctions(@Param("marketId") long marketId, @Param("currentBid") long currentBid, @Param("time") long time, @Param("numBids") int numBids, @Param("bidder") long bidder);

    @Query(value = "SELECT Spell, MapId, PositionX, PositionY, PositionZ, Orientation FROM character_aura_stored_location WHERE Guid = :guid")
    List<Map<String, Object>> selectCharacterAuraStoredLocations(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_aura_stored_location WHERE Guid = :guid")
    void deleteCharacterAuraStoredLocationsByGuid(@Param("guid") long guid);

    @Modifying
    @Query(value = "DELETE FROM character_aura_stored_location WHERE Guid = :guid AND Spell = :spell")
    void deleteCharacterAuraStoredLocation(@Param("guid") long guid, @Param("spell") int spell);

    @Modifying
    @Query(value = "INSERT INTO character_aura_stored_location (Guid, Spell, MapId, PositionX, PositionY, PositionZ, Orientation) VALUES (:guid, :spell, :mapId, :positionX, :positionY, :positionZ, :orientation)")
    void insertCharacterAuraStoredLocation(@Param("guid") long guid, @Param("spell") int spell, @Param("mapId") int mapId, @Param("positionX") float positionX, @Param("positionY") float positionY, @Param("positionZ") float positionZ, @Param("orientation") float orientation);

    @Query(value = "SELECT race, COUNT(guid) as count FROM characters WHERE ((playerFlags & :playerFlags) = :playerFlags) AND logout_time >= (UNIX_TIMESTAMP() - 604800) GROUP BY race")
    List<Map<String, Object>> selectWarModeTuning(@Param("playerFlags") int playerFlags);

    @Modifying
    @Query(value = "DELETE FROM character_instance_lock WHERE guid = :guid AND mapId = :mapId AND lockId = :lockId")
    void deleteCharacterInstanceLock(@Param("guid") long guid, @Param("mapId") int mapId, @Param("lockId") int lockId);

    @Modifying
    @Query(value = "DELETE FROM character_instance_lock WHERE guid = :guid")
    void deleteCharacterInstanceLockByGuid(@Param("guid") long guid);

    @Modifying
    @Query(value = "INSERT INTO character_instance_lock (guid, mapId, lockId, instanceId, difficulty, data, completedEncountersMask, entranceWorldSafeLocId, expiryTime, extended) VALUES (:guid, :mapId, :lockId, :instanceId, :difficulty, :data, :completedEncountersMask, :entranceWorldSafeLocId, :expiryTime, :extended)")
    void insertCharacterInstanceLock(@Param("guid") long guid, @Param("mapId") int mapId, @Param("lockId") int lockId, @Param("instanceId") int instanceId, @Param("difficulty") int difficulty, @Param("data") String data, @Param("completedEncountersMask") long completedEncountersMask, @Param("entranceWorldSafeLocId") int entranceWorldSafeLocId, @Param("expiryTime") long expiryTime, @Param("extended") int extended);

    @Modifying
    @Query(value = "UPDATE character_instance_lock SET extended = :extended WHERE guid = :guid AND mapId = :mapId AND lockId = :lockId")
    void updateCharacterInstanceLockExtension(@Param("extended") int extended, @Param("guid") long guid, @Param("mapId") int mapId, @Param("lockId") int lockId);

    @Modifying
    @Query(value = "UPDATE character_instance_lock SET expiryTime = :expiryTime, extended = 0 WHERE guid = :guid AND mapId = :mapId AND lockId = :lockId")
    void updateCharacterInstanceLockForceExpire(@Param("expiryTime") long expiryTime, @Param("guid") long guid, @Param("mapId") int mapId, @Param("lockId") int lockId);

    @Modifying
    @Query(value = "DELETE FROM instance WHERE instanceId = :instanceId")
    void deleteInstance(@Param("instanceId") int instanceId);

    @Modifying
    @Query(value = "INSERT INTO instance (instanceId, data, completedEncountersMask, entranceWorldSafeLocId) VALUES (:instanceId, :data, :completedEncountersMask, :entranceWorldSafeLocId)")
    void insertInstance(@Param("instanceId") int instanceId, @Param("data") String data, @Param("completedEncountersMask") long completedEncountersMask, @Param("entranceWorldSafeLocId") int entranceWorldSafeLocId);

}