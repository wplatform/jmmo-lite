package com.github.azeroth.auth.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface Repository extends ListCrudRepository<Object, Integer> {


    @Query("SELECT id, name, address, localAddress, address3, address4, port, icon, flag, timezone, allowedSecurityLevel, population, gamebuild, Region, Battlegroup FROM realmlist WHERE flag <> 3 ORDER BY name")
    List<Map<String, Object>> selectRealmList();


    @Query("SELECT ip, bandate, unbandate, bannedby, banreason FROM ip_banned WHERE (bandate = unbandate OR unbandate > UNIX_TIMESTAMP()) ORDER BY unbandate")
    List<Map<String, Object>> selectIpBannedAll();


    @Query("SELECT account.id, username FROM account, account_banned WHERE account.id = account_banned.id AND active = 1 GROUP BY account.id")
    List<Map<String, Object>> selectAccountBannedAll();

    @Query("SELECT account.id, username FROM account, account_banned WHERE account.id = account_banned.id AND active = 1 AND username LIKE CONCAT('%%', :username, '%%') GROUP BY account.id")
    List<Map<String, Object>> selectAccountBannedByFilter(@Param("username") String username);

    @Query("SELECT account.id, username FROM account, account_banned WHERE account.id = account_banned.id AND active = 1 AND username = :username GROUP BY account.id")
    List<Map<String, Object>> selectAccountBannedByUsername(@Param("username") String username);

    @Modifying
    @Query("UPDATE account SET session_key_bnet = :session_key_bnet WHERE id = :id")
    void updateAccountSessionKeyBnet(@Param("id") int id, @Param("session_key_bnet") String session_key_bnet);

    @Query("SELECT username, session_key_bnet FROM account WHERE id = :id AND LENGTH(session_key_bnet) = 40")
    Map<String, Object> selectAccountSessionKeyBnet(@Param("id") int id);


    @Query("""
            SELECT a.id AS aId, a.session_key_bnet, ba.last_ip, ba.locked, ba.lock_country, a.expansion, a.mutetime, a.client_build, a.locale, a.recruiter, a.os, a.timezone_offset, ba.id AS baId, aa.SecurityLevel,
            bab.unbandate > UNIX_TIMESTAMP() OR bab.unbandate = bab.bandate AS is_bnet_banned, ab.unbandate > UNIX_TIMESTAMP() OR ab.unbandate = ab.bandate AS is_banned, r.id AS rId, r.name AS rName
            FROM account a LEFT JOIN account r ON a.id = r.recruiter LEFT JOIN battlenet_accounts ba ON a.battlenet_account = ba.id "
            LEFT JOIN account_access aa ON a.id = aa.AccountID AND aa.RealmID IN (-1, :realmId) LEFT JOIN battlenet_account_bans bab ON ba.id = bab.id LEFT JOIN account_banned ab ON a.id = ab.id AND ab.active = 1
            WHERE a.username = :userName AND LENGTH(a.session_key_bnet) = 64 ORDER BY aa.RealmID DESC LIMIT 1
            """)
    Map<String, Object> selectAccountInfoByUserName(@Param("userName") String userName, @Param("realmId") int realmId);


    @Query("SELECT id, username FROM account WHERE email = :email")
    List<Map<String, Object>> selectAccountListByEmail(@Param("email") String email);

    @Query("SELECT id, username FROM account WHERE last_ip = :ip")
    List<Map<String, Object>> selectAccountListByIp(@Param("ip") String ip);

    @Query("SELECT id, username FROM account WHERE id = :id")
    List<Map<String, Object>> selectAccountListById(@Param("id") int id);

    @Query("INSERT INTO ip_banned (ip, bandate, unbandate, bannedby, banreason) VALUES (:ip, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()+:unbandate, :bannedby, :banreason)")
    void insertIpBanned(@Param("ip") String ip, @Param("unbandate") int unbandate, @Param("bannedby") String bannedby, @Param("banreason") String banreason);


    @Modifying
    @Query("INSERT INTO account_banned (id, bandate, unbandate, bannedby, banreason, active) VALUES (:id, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()+:unbandate, :bannedby, :banreason, 1)")
    void insertAccountBanned(@Param("id") int id, @Param("unbandate") int unbandate, @Param("bannedby") String bannedby, @Param("banreason") String banreason);

    @Modifying
    @Query("UPDATE account_banned SET active = 0 WHERE id = :id AND active != 0")
    void updateAccountBanned(@Param("id") int id);

    @Modifying
    @Query("DELETE FROM ip_banned WHERE ip = :ip")
    void deleteIpBanned(@Param("ip") String ip);



    @Modifying
    @Query("INSERT INTO logs (time, realm, type, level, string) VALUES (UNIX_TIMESTAMP(), :realm, :type, :level, :string)")
    void insertLog(@Param("realm") int realm, @Param("type") int type, @Param("level") int level, @Param("string") String string);

    @Modifying
    @Query("UPDATE uptime SET uptime = :uptime, maxplayers = :maxplayers WHERE realmid = :realmid AND starttime = :starttime")
    void updateUptime(@Param("uptime") int uptime, @Param("maxplayers") int maxplayers, @Param("realmid") int realmid, @Param("starttime") int starttime);

    @Modifying
    @Query("DELETE FROM logs WHERE (time + :time) < :time AND realm = :realm")
    void deleteOldLogs(@Param("time") int time, @Param("realm") int realm);

    @Query("SELECT 1 FROM account_access WHERE AccountID = :id AND SecurityLevel > :securityLevel")
    int checkAccountAccessSecLevelTest(@Param("id") int id, @Param("securityLevel") int securityLevel);

    @Query("SELECT a.id, aa.SecurityLevel, aa.RealmID FROM account a LEFT JOIN account_access aa ON a.id = aa.AccountID WHERE a.username = :username")
    List<Map<String, Object>> getAccountAccess(@Param("username") String username);

    @Modifying
    @Query("INSERT INTO logs_ip_actions (account_id, character_guid, realm_id, type, ip, systemnote, unixtime, time) VALUES (:id, 0, 0, 0, (SELECT last_attempt_ip FROM account WHERE id = :id), :systemnote, unix_timestamp(NOW()), NOW())")
    void insertFailedAccountLoginIpLogging(@Param("id") int id, @Param("systemnote") String systemnote);

    // 0: uint32, 1: uint32, 2: uint32, 3: uint8, 4: string, 5: string // Complete name: "Login_Insert_CharacterDelete_IP_Logging"
    @Modifying
    @Query("INSERT INTO logs_ip_actions (account_id, character_guid, realm_id, type, ip, systemnote, unixtime, time) VALUES (:id, 0, 0, 1, :ip, :systemnote, unix_timestamp(NOW()), NOW())")
    void insertCharacterDeleteIpLogging(@Param("id") int id, @Param("ip") String ip, @Param("systemnote") String systemnote);

    // 0: uint32, 1: string, 2: string                                 // Complete name: "Login_Insert_Failed_Account_Login_due_password_IP_Logging"
    @Modifying
    @Query("INSERT INTO logs_ip_actions (account_id, character_guid, realm_id, type, ip, systemnote, unixtime, time) VALUES (:id, 0, 0, 1, :ip, :systemnote, unix_timestamp(NOW()), NOW())")
    void insertFailedAccountLoginPasswordIpLogging(@Param("id") int id, @Param("ip") String ip, @Param("systemnote") String systemnote);

    @Query("SELECT SecurityLevel, RealmID FROM account_access WHERE AccountID = :id and (RealmID = :realmId OR RealmID = -1) ORDER BY SecurityLevel desc")
    List<Map<String, Object>> getAccountAccessById(@Param("id") int id, @Param("realmId") int realmId);

    @Query("SELECT permissionId, granted FROM rbac_account_permissions WHERE accountId = :accountId AND (realmId = :realmId OR realmId = -1) ORDER BY permissionId, realmId")
    List<Map<String, Object>> getRbacAccountPermissions(@Param("accountId") int accountId, @Param("realmId") int realmId);

    @Modifying
    @Query("INSERT INTO rbac_account_permissions (accountId, permissionId, granted, realmId) VALUES (:accountId, :permissionId, :granted, :realmId) ON DUPLICATE KEY UPDATE granted = VALUES(granted)")
    void insertRbacAccountPermission(@Param("accountId") int accountId, @Param("permissionId") int permissionId, @Param("granted") int granted, @Param("realmId") int realmId);

    @Modifying
    @Query("DELETE FROM rbac_account_permissions WHERE accountId = :accountId AND permissionId = :permissionId AND (realmId = :realmId OR realmId = -1)")
    void deleteRbacAccountPermission(@Param("accountId") int accountId, @Param("permissionId") int permissionId, @Param("realmId") int realmId);

    @Modifying
    @Query("INSERT INTO account_muted VALUES (:guid, UNIX_TIMESTAMP(), :mutetime, :mutereason, :mutedby)")
    void insertAccountMute(@Param("guid") int guid, @Param("mutetime") int mutetime, @Param("mutereason") String mutereason, @Param("mutedby") String mutedby);

    @Query("SELECT mutedate, mutetime, mutereason, mutedby FROM account_muted WHERE guid = :guid ORDER BY mutedate ASC")
    List<Map<String, Object>> getAccountMuteInfo(@Param("guid") int guid);

    @Modifying
    @Query("DELETE FROM account_muted WHERE guid = :guid")
    void deleteAccountMute(@Param("guid") int guid);

    @Query("SELECT digest FROM secret_digest WHERE id = :id")
    String getSecretDigest(@Param("id") int id);

    @Modifying
    @Query("INSERT INTO secret_digest (id, digest) VALUES (:id, :digest)")
    void insertSecretDigest(@Param("id") int id, @Param("digest") String digest);

    // 账号权限查询
    @Query("SELECT SecurityLevel, RealmID FROM account_access WHERE AccountID = :accountId and (RealmID = :realmId OR RealmID = -1) ORDER BY SecurityLevel desc")
    List<Map<String, Object>> findAccountAccessById(@Param("accountId") int accountId, @Param("realmId") int realmId);

    // RBAC账号权限查询
    @Query("SELECT permissionId, granted FROM rbac_account_permissions WHERE accountId = :accountId AND (realmId = :realmId OR realmId = -1) ORDER BY permissionId, realmId")
    List<Map<String, Object>> findRbacAccountPermissions(@Param("accountId") int accountId, @Param("realmId") int realmId);

    // 插入RBAC账号权限
    @Modifying
    @Query("INSERT INTO rbac_account_permissions (accountId, permissionId, granted, realmId) VALUES (:accountId, :permissionId, :granted, :realmId) ON DUPLICATE KEY UPDATE granted = VALUES(granted)")
    void insertRbacAccountPermission(@Param("accountId") int accountId, @Param("permissionId") int permissionId, @Param("granted") boolean granted, @Param("realmId") int realmId);

    // 查询账号静音信息
    @Query("SELECT mutedate, mutetime, mutereason, mutedby FROM account_muted WHERE guid = :guid ORDER BY mutedate ASC")
    List<Map<String, Object>> findAccountMuteInfo(@Param("guid") int guid);

    // 删除账号静音记录


    // 查询账号TOTP密钥
    @Query("SELECT totp_secret FROM account WHERE id = :id")
    List<Map<String, Object>> findAccountTotpSecret(@Param("id") int id);

    // 更新账号TOTP密钥
    @Modifying
    @Query("UPDATE account SET totp_secret = :totpSecret WHERE id = :id")
    void updateAccountTotpSecret(@Param("totpSecret") String totpSecret, @Param("id") int id);

    // 查询战网账号认证信息
    @Query("SELECT ba.id, ba.srp_version, COALESCE(ba.salt, 0x0000000000000000000000000000000000000000000000000000000000000000), ba.verifier, ba.failed_logins, ba.LoginTicket, ba.LoginTicketExpiry, bab.unbandate > UNIX_TIMESTAMP() OR bab.unbandate = bab.bandate FROM battlenet_accounts ba LEFT JOIN battlenet_account_bans bab ON ba.id = bab.id WHERE email = :email")
    List<Map<String, Object>> findBnetAuthentication(@Param("email") String email);

    // 更新战网账号认证信息
    @Modifying
    @Query("UPDATE battlenet_accounts SET LoginTicket = :loginTicket, LoginTicketExpiry = :loginTicketExpiry WHERE id = :id")
    void updateBnetAuthentication(@Param("loginTicket") String loginTicket, @Param("loginTicketExpiry") int loginTicketExpiry, @Param("id") int id);

    // 查询账号角色数量
    @Query("SELECT rc.acctid, rc.numchars, r.id, r.Region, r.Battlegroup FROM realmcharacters rc INNER JOIN realmlist r ON rc.realmid = r.id WHERE rc.acctid = :acctid")
    List<Map<String, Object>> findCharacterCountsByAccountId(@Param("acctid") int acctid);

    // 查询战网现有认证
    @Query("SELECT LoginTicketExpiry FROM battlenet_accounts WHERE LoginTicket = :loginTicket")
    List<Map<String, Object>> findBnetExistingAuthentication(@Param("loginTicket") String loginTicket);

    // 查询战网账号ID
    @Query("SELECT id FROM battlenet_accounts WHERE email = :email")
    List<Map<String, Object>> findBnetAccountIdByEmail(@Param("email") String email);

    // 更新战网账号锁定状态
    @Modifying
    @Query("UPDATE battlenet_accounts SET locked = :locked WHERE id = :id")
    void updateBnetAccountLock(@Param("locked") boolean locked, @Param("id") int id);

    // 查询战网宠物列表
    @Query("SELECT bp.guid, bp.species, bp.breed, bp.displayId, bp.level, bp.exp, bp.health, bp.quality, bp.flags, bp.name, bp.nameTimestamp, bp.owner, bp.ownerRealmId, dn.genitive, dn.dative, dn.accusative, dn.instrumental, dn.prepositional FROM battle_pets bp LEFT JOIN battle_pet_declinedname dn ON bp.guid = dn.guid WHERE bp.battlenetAccountId = :battlenetAccountId AND (bp.ownerRealmId IS NULL OR bp.ownerRealmId = :ownerRealmId)")
    List<Map<String, Object>> findBattlePets(@Param("battlenetAccountId") int battlenetAccountId, @Param("ownerRealmId") int ownerRealmId);

    // 插入战网宠物
    @Modifying
    @Query("INSERT INTO battle_pets (guid, battlenetAccountId, species, breed, displayId, level, exp, health, quality, flags, name, nameTimestamp, owner, ownerRealmId) VALUES (:guid, :battlenetAccountId, :species, :breed, :displayId, :level, :exp, :health, :quality, :flags, :name, :nameTimestamp, :owner, :ownerRealmId)")
    void insertBattlePet(@Param("guid") int guid, @Param("battlenetAccountId") int battlenetAccountId, @Param("species") int species, @Param("breed") int breed, @Param("displayId") int displayId, @Param("level") int level, @Param("exp") int exp, @Param("health") int health, @Param("quality") int quality, @Param("flags") int flags, @Param("name") String name, @Param("nameTimestamp") int nameTimestamp, @Param("owner") int owner, @Param("ownerRealmId") int ownerRealmId);

    // 查询账号玩具
    @Query("SELECT itemId, isFavourite, hasFanfare FROM battlenet_account_toys WHERE accountId = :accountId")
    List<Map<String, Object>> findAccountToys(@Param("accountId") int accountId);

    // 查询账号坐骑
    @Query("SELECT mountSpellId, flags FROM battlenet_account_mounts WHERE battlenetAccountId = :battlenetAccountId")
    List<Map<String, Object>> findAccountMounts(@Param("battlenetAccountId") int battlenetAccountId);

    // 查询幻化外观
    @Query("SELECT blobIndex, appearanceMask FROM battlenet_item_appearances WHERE battlenetAccountId = :battlenetAccountId ORDER BY blobIndex DESC")
    List<Map<String, Object>> findItemAppearances(@Param("battlenetAccountId") int battlenetAccountId);

    // 查询战网账号现有认证ByID
    @Query("SELECT LoginTicket FROM battlenet_accounts WHERE id = :id")
    List<Map<String, Object>> findBnetExistingAuthenticationById(@Param("id") int id);

    // 更新战网现有认证
    @Modifying
    @Query("UPDATE battlenet_accounts SET LoginTicketExpiry = :loginTicketExpiry WHERE LoginTicket = :loginTicket")
    void updateBnetExistingAuthentication(@Param("loginTicketExpiry") int loginTicketExpiry, @Param("loginTicket") String loginTicket);

    // 查询战网账号信息
    @Query("SELECT ba.id AS bnet_account_id, UPPER(ba.email), ba.locked, ba.lock_country, ba.last_ip, ba.LoginTicketExpiry, bab.unbandate > UNIX_TIMESTAMP() OR bab.unbandate = bab.bandate AS is_bnet_banned, bab.unbandate = bab.bandate AS is_bnet_permanently_banned, a.id AS account_id, a.username, ab.unbandate AS account_unbandate, ab.unbandate = ab.bandate AS is_banned, aa.SecurityLevel FROM battlenet_accounts ba LEFT JOIN battlenet_account_bans bab ON ba.id = bab.id LEFT JOIN account a ON ba.id = a.battlenet_account LEFT JOIN account_banned ab ON a.id = ab.id AND ab.active = 1 LEFT JOIN account_access aa ON a.id = aa.AccountID AND aa.RealmID = -1 WHERE ba.LoginTicket = :loginTicket ORDER BY a.id")
    List<Map<String, Object>> findBnetAccountInfo(@Param("loginTicket") String loginTicket);

    // 更新战网最后登录信息
    @Modifying
    @Query("UPDATE battlenet_accounts SET last_ip = :lastIp, last_login = NOW(), locale = :locale, failed_logins = 0, os = :os WHERE id = :id")
    void updateBnetLastLoginInfo(@Param("lastIp") String lastIp, @Param("locale") String locale, @Param("os") String os, @Param("id") int id);

    // 查询战网角色数量By战网ID
    @Query("SELECT rc.acctid, rc.numchars, r.id, r.Region, r.Battlegroup FROM realmcharacters rc INNER JOIN realmlist r ON rc.realmid = r.id LEFT JOIN account a ON rc.acctid = a.id WHERE a.battlenet_account = :battlenetAccountId")
    List<Map<String, Object>> findCharacterCountsByBnetId(@Param("battlenetAccountId") int battlenetAccountId);

    // 查询战网游戏账号列表
    @Query("SELECT a.username, a.expansion, ab.bandate, ab.unbandate, ab.banreason FROM account AS a LEFT JOIN account_banned AS ab ON a.id = ab.id AND ab.active = 1 INNER JOIN battlenet_accounts AS ba ON a.battlenet_account = ba.id WHERE ba.LoginTicket = :loginTicket ORDER BY a.id")
    List<Map<String, Object>> findBnetGameAccountList(@Param("loginTicket") String loginTicket);

    // 更新战网失败登录次数
    @Modifying
    @Query("UPDATE battlenet_accounts SET failed_logins = failed_logins + 1 WHERE id = :id")
    void updateBnetFailedLogins(@Param("id") int id);

    // 插入战网账号自动封禁
    @Modifying
    @Query("INSERT INTO battlenet_account_bans(id, bandate, unbandate, bannedby, banreason) VALUES(:id, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()+:duration, 'Trinity Auth', 'Failed login autoban')")
    void insertBnetAccountAutoBanned(@Param("id") int id, @Param("duration") int duration);

    // 查询战网最后登录角色
    @Query("SELECT lpc.accountId, lpc.region, lpc.battlegroup, lpc.realmId, lpc.characterName, lpc.characterGUID, lpc.lastPlayedTime FROM account_last_played_character lpc LEFT JOIN account a ON lpc.accountId = a.id WHERE a.battlenet_account = :battlenetAccountId")
    List<Map<String, Object>> findBnetLastPlayerCharacters(@Param("battlenetAccountId") int battlenetAccountId);

    // 删除战网最后登录角色
    @Modifying
    @Query("DELETE FROM account_last_played_character WHERE accountId = :accountId AND region = :region AND battlegroup = :battlegroup")
    void deleteBnetLastPlayerCharacters(@Param("accountId") int accountId, @Param("region") int region, @Param("battlegroup") int battlegroup);

    // 插入战网最后登录角色
    @Modifying
    @Query("INSERT INTO account_last_played_character (accountId, region, battlegroup, realmId, characterName, characterGUID, lastPlayedTime) VALUES (:accountId, :region, :battlegroup, :realmId, :characterName, :characterGUID, :lastPlayedTime)")
    void insertBnetLastPlayerCharacters(@Param("accountId") int accountId, @Param("region") int region, @Param("battlegroup") int battlegroup, @Param("realmId") int realmId, @Param("characterName") String characterName, @Param("characterGUID") int characterGUID, @Param("lastPlayedTime") int lastPlayedTime);

    // 查询战网宠物栏位
    @Query("SELECT id, battlePetGuid, locked FROM battle_pet_slots WHERE battlenetAccountId = :battlenetAccountId")
    List<Map<String, Object>> findBattlePetSlots(@Param("battlenetAccountId") int battlenetAccountId);

    // 插入战网宠物栏位
    @Modifying
    @Query("INSERT INTO battle_pet_slots (id, battlenetAccountId, battlePetGuid, locked) VALUES (:id, :battlenetAccountId, :battlePetGuid, :locked)")
    void insertBattlePetSlot(@Param("id") int id, @Param("battlenetAccountId") int battlenetAccountId, @Param("battlePetGuid") int battlePetGuid, @Param("locked") boolean locked);

    // 删除战网宠物栏位
    @Modifying
    @Query("DELETE FROM battle_pet_slots WHERE battlenetAccountId = :battlenetAccountId")
    void deleteBattlePetSlots(@Param("battlenetAccountId") int battlenetAccountId);

    // 查询账号传家宝
    @Query("SELECT itemId, flags FROM battlenet_account_heirlooms WHERE accountId = :accountId")
    List<Map<String, Object>> findAccountHeirlooms(@Param("accountId") int accountId);

    // 替换账号传家宝
    @Modifying
    @Query("REPLACE INTO battlenet_account_heirlooms (accountId, itemId, flags) VALUES (:accountId, :itemId, :flags)")
    void replaceAccountHeirlooms(@Param("accountId") int accountId, @Param("itemId") int itemId, @Param("flags") int flags);

    // 插入战网幻化幻象
    @Modifying
    @Query("INSERT INTO battlenet_account_transmog_illusions (battlenetAccountId, blobIndex, illusionMask) VALUES (:battlenetAccountId, :blobIndex, :illusionMask) ON DUPLICATE KEY UPDATE illusionMask = illusionMask | VALUES(illusionMask)")
    void insertBnetTransmogIllusions(@Param("battlenetAccountId") int battlenetAccountId, @Param("blobIndex") int blobIndex, @Param("illusionMask") int illusionMask);

    // 查询战网游戏账号
    @Query("SELECT id, battlenetAccountId, gameAccountId, gameAccountRegion, gameAccountBattlegroup, gameAccountRealmId, gameAccountName FROM battlenet_game_accounts WHERE battlenetAccountId = :battlenetAccountId")
    List<Map<String, Object>> findBnetGameAccounts(@Param("battlenetAccountId") int battlenetAccountId);

    // 更新战网游戏账号
    @Modifying
    @Query("UPDATE battlenet_game_accounts SET gameAccountName = :gameAccountName WHERE id = :id")
    void updateBnetGameAccountName(@Param("id") int id, @Param("gameAccountName") String gameAccountName);

    // 查询账号登录尝试
    @Query("SELECT attemptTime FROM account_login_attempts WHERE accountId = :accountId ORDER BY attemptTime DESC LIMIT 1")
    List<Map<String, Object>> findRecentLoginAttempt(@Param("accountId") int accountId);

    // 插入账号登录尝试
    @Modifying
    @Query("INSERT INTO account_login_attempts (accountId, attemptTime, ipAddress) VALUES (:accountId, UNIX_TIMESTAMP(), :ipAddress)")
    void insertLoginAttempt(@Param("accountId") int accountId, @Param("ipAddress") String ipAddress);

    // 查询账号活动会话
    @Query("SELECT id, accountId, sessionKey, loginTime, lastActiveTime, ipAddress FROM account_sessions WHERE accountId = :accountId AND isActive = 1")
    List<Map<String, Object>> findActiveSessions(@Param("accountId") int accountId);

    // 创建账号会话
    @Modifying
    @Query("INSERT INTO account_sessions (accountId, sessionKey, loginTime, lastActiveTime, ipAddress, isActive) VALUES (:accountId, :sessionKey, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), :ipAddress, 1)")
    void createSession(@Param("accountId") int accountId, @Param("sessionKey") String sessionKey, @Param("ipAddress") String ipAddress);

    // 更新会话活动时间
    @Modifying
    @Query("UPDATE account_sessions SET lastActiveTime = UNIX_TIMESTAMP() WHERE id = :sessionId")
    void updateSessionActivity(@Param("sessionId") int sessionId);

    // 结束账号会话
    @Modifying
    @Query("UPDATE account_sessions SET isActive = 0, logoutTime = UNIX_TIMESTAMP() WHERE id = :sessionId")
    void endSession(@Param("sessionId") int sessionId);

    // 查询账号安全问题
    @Query("SELECT id, question, answerHash FROM account_security_questions WHERE accountId = :accountId")
    List<Map<String, Object>> findSecurityQuestions(@Param("accountId") int accountId);

    // 验证安全问题答案
    @Query("SELECT COUNT(*) FROM account_security_questions WHERE accountId = :accountId AND id = :questionId AND answerHash = :answerHash")
    List<Map<String, Object>> verifySecurityAnswer(@Param("accountId") int accountId, @Param("questionId") int questionId, @Param("answerHash") String answerHash);

    // 更新安全问题答案
    @Modifying
    @Query("UPDATE account_security_questions SET answerHash = :answerHash WHERE accountId = :accountId AND id = :questionId")
    void updateSecurityAnswer(@Param("accountId") int accountId, @Param("questionId") int questionId, @Param("answerHash") String answerHash);

    // 创建密码重置令牌
    @Modifying
    @Query("INSERT INTO account_password_resets (accountId, token, expiresAt) VALUES (:accountId, :token, UNIX_TIMESTAMP() + 86400)")
    void createPasswordResetToken(@Param("accountId") int accountId, @Param("token") String token);

    // 验证密码重置令牌
    @Query("SELECT accountId FROM account_password_resets WHERE token = :token AND expiresAt > UNIX_TIMESTAMP()")
    List<Map<String, Object>> verifyPasswordResetToken(@Param("token") String token);

    // 使用令牌重置密码
    @Modifying
    @Query("UPDATE account SET sha_pass_hash = :newPasswordHash WHERE id = :accountId")
    void resetPasswordWithToken(@Param("accountId") int accountId, @Param("newPasswordHash") String newPasswordHash);

    // 删除过期密码重置令牌
    @Modifying
    @Query("DELETE FROM account_password_resets WHERE expiresAt <= UNIX_TIMESTAMP()")
    void deleteExpiredResetTokens();

    // 查询账号角色列表
    @Query("SELECT guid, name, race, class, level FROM characters WHERE account = :accountId")
    List<Map<String, Object>> findAccountCharacters(@Param("accountId") int accountId);

    // 更新角色最后登录时间
    @Modifying
    @Query("UPDATE characters SET last_login = NOW() WHERE guid = :characterGuid")
    void updateCharacterLastLogin(@Param("characterGuid") int characterGuid);

    // 查询角色公会信息
    @Query("SELECT g.guildid, g.name, g.motd FROM guild_member gm JOIN guild g ON gm.guildid = g.guildid WHERE gm.guid = :characterGuid")
    List<Map<String, Object>> findCharacterGuild(@Param("characterGuid") int characterGuid);

    // 插入角色成就
    @Modifying
    @Query("INSERT INTO character_achievement (guid, achievement, date) VALUES (:characterGuid, :achievementId, UNIX_TIMESTAMP()) ON DUPLICATE KEY UPDATE date = VALUES(date)")
    void insertCharacterAchievement(@Param("characterGuid") int characterGuid, @Param("achievementId") int achievementId);

    // 查询IP封禁记录
    @Query("SELECT ip, banDate, unbanDate, reason FROM ip_bans WHERE ip = :ipAddress")
    List<Map<String, Object>> findIpBan(@Param("ipAddress") String ipAddress);

    // 添加IP封禁
    @Modifying
    @Query("INSERT INTO ip_bans (ip, banDate, unbanDate, reason) VALUES (:ipAddress, UNIX_TIMESTAMP(), UNIX_TIMESTAMP() + :duration, :reason)")
    void insertIpBan(@Param("ipAddress") String ipAddress, @Param("duration") int duration, @Param("reason") String reason);

    // 解除IP封禁
    @Modifying
    @Query("DELETE FROM ip_bans WHERE ip = :ipAddress")
    void removeIpBan(@Param("ipAddress") String ipAddress);

    // 创建邮箱验证令牌
    @Modifying
    @Query("INSERT INTO account_email_verification (accountId, token, expiresAt) VALUES (:accountId, :token, UNIX_TIMESTAMP() + 86400)")
    void createEmailVerificationToken(@Param("accountId") int accountId, @Param("token") String token);

    // 验证邮箱令牌并更新邮箱
    @Modifying
    @Query("UPDATE account a JOIN account_email_verification ev ON a.id = ev.accountId SET a.email = :newEmail, a.emailVerified = 1 WHERE ev.token = :token AND ev.expiresAt > UNIX_TIMESTAMP()")
    void verifyEmailAndUpdate(@Param("token") String token, @Param("newEmail") String newEmail);

    // 账号静音
    @Modifying
    @Query("INSERT INTO account_muted (accountId, mutedBy, muteDate, unmuteDate, reason) VALUES (:accountId, :mutedBy, UNIX_TIMESTAMP(), UNIX_TIMESTAMP() + :duration, :reason)")
    void muteAccount(@Param("accountId") int accountId, @Param("mutedBy") int mutedBy, @Param("duration") int duration, @Param("reason") String reason);

    // 检查账号静音状态
    @Query("SELECT unmuteDate, reason FROM account_muted WHERE accountId = :accountId AND unmuteDate > UNIX_TIMESTAMP()")
    List<Map<String, Object>> checkAccountMuteStatus(@Param("accountId") int accountId);

    // 删除战网过期账号封禁
    @Modifying
    @Query("DELETE FROM battlenet_account_bans WHERE unbandate<>bandate AND unbandate<=UNIX_TIMESTAMP()")
    void deleteBnetExpiredAccountBanned();

    // 查询战网账号最后角色恢复时间
    @Query("SELECT LastCharacterUndelete FROM battlenet_accounts WHERE Id = :id")
    List<Map<String, Object>> findLastCharUndelete(@Param("id") int id);

    // 更新战网账号最后角色恢复时间
    @Modifying
    @Query("UPDATE battlenet_accounts SET LastCharacterUndelete = UNIX_TIMESTAMP() WHERE Id = :id")
    void updateLastCharUndelete(@Param("id") int id);

    // 替换账号玩具
    @Modifying
    @Query("REPLACE INTO battlenet_account_toys (accountId, itemId, isFavourite, hasFanfare) VALUES (:accountId, :itemId, :isFavourite, :hasFanfare)")
    void replaceAccountToys(@Param("accountId") int accountId, @Param("itemId") int itemId, @Param("isFavourite") boolean isFavourite, @Param("hasFanfare") boolean hasFanfare);

    // 删除战网宠物
    @Modifying
    @Query("DELETE FROM battle_pets WHERE battlenetAccountId = :battlenetAccountId AND guid = :guid")
    void deleteBattlePet(@Param("battlenetAccountId") int battlenetAccountId, @Param("guid") int guid);

    // 查询战网幻化幻象
    @Query("SELECT blobIndex, illusionMask FROM battlenet_account_transmog_illusions WHERE battlenetAccountId = :battlenetAccountId ORDER BY blobIndex DESC")
    List<Map<String, Object>> findTransmogIllusions(@Param("battlenetAccountId") int battlenetAccountId);

    // Transmog & Collection queries
    @Modifying
    @Query("REPLACE INTO battlenet_account_mounts (battlenetAccountId, mountSpellId, flags) VALUES (:battlenetAccountId, :mountSpellId, :flags)")
    void replaceAccountMounts(@Param("battlenetAccountId") int battlenetAccountId, @Param("mountSpellId") int mountSpellId, @Param("flags") int flags);

    @Query("SELECT itemModifiedAppearanceId FROM battlenet_item_favorite_appearances WHERE battlenetAccountId = :battlenetAccountId")
    List<Map<String, Object>> selectFavoriteItemAppearances(@Param("battlenetAccountId") int battlenetAccountId);

    @Modifying
    @Query("INSERT INTO battlenet_item_favorite_appearances (battlenetAccountId, itemModifiedAppearanceId) VALUES (:battlenetAccountId, :appearanceId)")
    void insertFavoriteItemAppearance(@Param("battlenetAccountId") int battlenetAccountId, @Param("appearanceId") int appearanceId);

    @Modifying
    @Query("DELETE FROM battlenet_item_favorite_appearances WHERE battlenetAccountId = :battlenetAccountId AND itemModifiedAppearanceId = :appearanceId")
    void deleteFavoriteItemAppearance(@Param("battlenetAccountId") int battlenetAccountId, @Param("appearanceId") int appearanceId);

    // IP Ban queries
    @Modifying
    @Query("DELETE FROM ip_banned WHERE unbandate <> bandate AND unbandate <= UNIX_TIMESTAMP()")
    void deleteExpiredIpBans();

    @Query("SELECT unbandate > UNIX_TIMESTAMP() OR unbandate = bandate AS banned, NULL as country FROM ip_banned WHERE ip = :ip")
    List<Map<String, Object>> selectIpInfo(@Param("ip") String ip);

    @Modifying
    @Query("INSERT INTO ip_banned (ip, bandate, unbandate, bannedby, banreason) VALUES (:ip, UNIX_TIMESTAMP(), UNIX_TIMESTAMP() + :duration, 'Trinity Auth', 'Failed login autoban')")
    void insertIpAutoBanned(@Param("ip") String ip, @Param("duration") int duration);

    @Query("SELECT ip, bandate, unbandate, bannedby, banreason FROM ip_banned WHERE (bandate = unbandate OR unbandate > UNIX_TIMESTAMP()) ORDER BY unbandate")
    List<Map<String, Object>> selectAllIpBanned();

    @Query("SELECT ip, bandate, unbandate, bannedby, banreason FROM ip_banned WHERE (bandate = unbandate OR unbandate > UNIX_TIMESTAMP()) AND ip LIKE CONCAT('%', :ip, '%') ORDER BY unbandate")
    List<Map<String, Object>> selectIpBannedByIp(@Param("ip") String ip);

    // Realm queries
    @Query("SELECT id, name, address, localAddress, address3, address4, port, icon, flag, timezone, allowedSecurityLevel, population, gamebuild, Region, Battlegroup FROM realmlist WHERE flag <> 3 ORDER BY name")
    List<Map<String, Object>> selectRealmlist();

    @Modifying
    @Query("UPDATE realmlist SET population = :population WHERE id = :id")
    void updateRealmPopulation(@Param("population") int population, @Param("id") int id);

    // 查询账号权限（按用户名）
    @Query("SELECT a.id, aa.SecurityLevel, aa.RealmID FROM account a LEFT JOIN account_access aa ON a.id = aa.AccountID WHERE a.username = :username")
    List<Map<String, Object>> selectAccountAccess(@Param("username") String username);

    // 查询账号详细信息（WHOIS）
    @Query("SELECT username, email, last_ip FROM account WHERE id = :accountId")
    List<Map<String, Object>> selectAccountWhois(@Param("accountId") int accountId);

    // 删除账号
    @Modifying
    @Query("DELETE FROM account WHERE id = :accountId")
    void deleteAccount(@Param("accountId") int accountId);

    // 查询自动广播信息
    @Query("SELECT id, weight, text FROM autobroadcast WHERE realmid = :realmId OR realmid = -1")
    List<Map<String, Object>> selectAutoBroadcast(@Param("realmId") int realmId);

    // 查询账号邮箱
    @Query("SELECT email FROM account WHERE id = :accountId")
    List<Map<String, Object>> getEmailById(@Param("accountId") int accountId);


}