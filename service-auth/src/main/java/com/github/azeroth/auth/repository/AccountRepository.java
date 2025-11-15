package com.github.azeroth.auth.repository;

import com.github.azeroth.auth.domain.*;
import com.github.azeroth.auth.dto.AccountInfo;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public interface AccountRepository extends CrudRepository<Account, Integer> {


    @Query("from AccountBanned as a where a.id.id=:accountId and a.active = 1")
    List<AccountBanned> findBannedByAccountId(@Param("accountId") Long accountId);

    @Query("from AccountLastPlayedCharacter a where a.id.accountId in(:accountIds)")
    List<AccountLastPlayedCharacter> findLastPlayedCharacterByIdAccountIds(Set<Integer> accountIds);

    @Query("""
            SELECT rc, r
            FROM Realmcharacter rc INNER JOIN Realmlist r ON rc.id.realmid = r.id
            WHERE rc.id.acctid in (:acctids)
            """)
    Object[][] findByIdAccounts(Set<Integer> accounts);



    @Modifying
    @Query("DELETE FROM account_access WHERE AccountID = :id")
    void deleteAccountAccess(@Param("id") int id);

    @Modifying
    @Query("DELETE FROM account_access WHERE AccountID = :id AND (RealmID = :realmId OR RealmID = -1)")
    void deleteAccountAccessByRealm(@Param("id") int id, @Param("realmId") int realmId);

    @Modifying
    @Query("INSERT INTO account_access (AccountID, SecurityLevel, RealmID) VALUES (:id, :securityLevel, :realmId)")
    void insertAccountAccess(@Param("id") int id, @Param("securityLevel") int securityLevel, @Param("realmId") int realmId);

    @Query("SELECT id FROM account WHERE username = :username")
    int getAccountIdByUsername(@Param("username") String username);

    @Query("SELECT SecurityLevel FROM account_access WHERE AccountID = :id AND (RealmID = :realmId OR RealmID = -1) ORDER BY RealmID DESC")
    int getGmLevelByRealmId(@Param("id") int id, @Param("realmId") int realmId);

    @Query("SELECT username FROM account WHERE id = :id")
    String getUsernameById(@Param("id") int id);

    @Query("SELECT salt, verifier FROM account WHERE id = :id")
    Map<String, Object> getPasswordById(@Param("id") int id);

    @Query("SELECT salt, verifier FROM account WHERE username = :username")
    Map<String, Object> getPasswordByUsername(@Param("username") String username);

    @Query("SELECT a.username, aa.SecurityLevel, a.email, a.reg_mail, a.last_ip, DATE_FORMAT(a.last_login, '%Y-%m-%d %T'), a.mutetime, a.mutereason, a.muteby, a.failed_logins, a.locked, a.OS FROM account a LEFT JOIN account_access aa ON (a.id = aa.AccountID AND (aa.RealmID = :realmID OR aa.RealmID = -1)) WHERE a.id = :id")
    List<Map<String, Object>> selectPIInfo(@Param("realmID") int realmID, @Param("id") int id);

    @Query("SELECT unbandate, bandate = unbandate, bannedby, banreason FROM account_banned WHERE id = :id AND active ORDER BY bandate ASC LIMIT 1")
    Map<String, Object> getAccountBansById(@Param("id") int id);

    @Query("SELECT a.username, aa.SecurityLevel FROM account a, account_access aa WHERE a.id = aa.AccountID AND aa.SecurityLevel >= :securityLevel AND (aa.RealmID = -1 OR aa.RealmID = :realmId)")
    List<Map<String, Object>> getGmAccounts(@Param("securityLevel") int securityLevel, @Param("realmId") int realmId);

    @Query("SELECT a.username, a.last_ip, aa.SecurityLevel, a.expansion FROM account a LEFT JOIN account_access aa ON a.id = aa.AccountID WHERE a.id = :id")
    Map<String, Object> getAccountInfoById(@Param("id") int id);

    @Modifying
    @Query("DELETE FROM realmcharacters WHERE acctid = :acctid")
    void deleteRealmCharacters(@Param("acctid") int acctid);

    @Modifying
    @Query("REPLACE INTO realmcharacters (numchars, acctid, realmid) VALUES (:numchars, :acctid, :realmid)")
    void replaceRealmCharacters(@Param("numchars") int numchars, @Param("acctid") int acctid, @Param("realmid") int realmid);

    @Query("SELECT SUM(numchars) FROM realmcharacters WHERE acctid = :acctid")
    int selectSumRealmCharacters(@Param("acctid") int acctid);


    @Modifying
    @Query("INSERT INTO realmcharacters (realmid, acctid, numchars) SELECT realmlist.id, account.id, 0 FROM realmlist, account LEFT JOIN realmcharacters ON acctid = account.id WHERE acctid IS NULL")
    void insertRealmCharactersInit();

    @Modifying
    @Query("UPDATE account SET expansion = :expansion WHERE id = :id")
    void updateAccountExpansion(@Param("expansion") int expansion, @Param("id") int id);

    @Modifying
    @Query("UPDATE account SET locked = :locked WHERE id = :id")
    void updateAccountLock(@Param("locked") int locked, @Param("id") int id);

    @Modifying
    @Query("UPDATE account SET lock_country = :lock_country WHERE id = :id")
    void updateAccountLockCountry(@Param("lock_country") String lock_country, @Param("id") int id);

    @Query("SELECT account.id, username FROM account, account_banned WHERE account.id = account_banned.id AND active = 1 GROUP BY account.id")
    List<Map<String, Object>> selectAllBannedAccounts();

    @Query("SELECT account.id, username FROM account, account_banned WHERE account.id = account_banned.id AND active = 1 AND username LIKE CONCAT('%', :username, '%') GROUP BY account.id")
    List<Map<String, Object>> selectBannedAccountsByFilter(@Param("username") String username);

    @Query("SELECT account.id, username FROM account, account_banned WHERE account.id = account_banned.id AND active = 1 AND username = :username GROUP BY account.id")
    List<Map<String, Object>> selectBannedAccountByUsername(@Param("username") String username);

    @Modifying
    @Query("DELETE FROM account_banned WHERE id = :accountId")
    void deleteAccountBanned(@Param("accountId") int accountId);

    @Modifying
    @Query("DELETE FROM account_muted WHERE guid = :guid")
    void deleteAccountMuted(@Param("guid") int guid);


    @Modifying
    @Query("UPDATE account SET username = :username WHERE id = :id")
    void updateAccountUsername(@Param("username") String username, @Param("id") int id);

    @Modifying
    @Query("UPDATE account SET email = :email WHERE id = :id")
    void updateAccountEmail(@Param("email") String email, @Param("id") int id);

    @Modifying
    @Query("UPDATE account SET reg_mail = :reg_mail WHERE id = :id")
    void updateAccountRegEmail(@Param("reg_mail") String reg_mail, @Param("id") int id);

    @Modifying
    @Query("UPDATE account SET mutetime = :mutetime, mutereason = :mutereason, muteby = :muteby WHERE id = :id")
    void updateAccountMuteTime(@Param("mutetime") int mutetime, @Param("mutereason") String mutereason, @Param("muteby") String muteby, @Param("id") int id);

    @Modifying
    @Query("UPDATE account SET mutetime = :mutetime WHERE id = :id")
    void updateAccountMuteTimeLogin(@Param("mutetime") long mutetime, @Param("id") int id);

    @Modifying
    @Query("UPDATE account SET last_ip = :last_ip WHERE username = :username")
    void updateAccountLastIp(@Param("last_ip") String last_ip, @Param("username") String username);

    @Modifying
    @Query("UPDATE account SET last_attempt_ip = :last_attempt_ip WHERE username = :username")
    void updateAccountLastAttemptIp(@Param("last_attempt_ip") String last_attempt_ip, @Param("username") String username);

    @Modifying
    @Query("UPDATE account SET online = 1 WHERE id = :id")
    void updateAccountOnline(@Param("id") int id);

    @Modifying
    @Query("DELETE FROM secret_digest WHERE id = :id")
    void deleteSecretDigest(@Param("id") int id);

    @Query("SELECT totp_secret FROM account WHERE id = :id")
    String getAccountTotpSecret(@Param("id") int id);

    @Modifying
    @Query("UPDATE account SET totp_secret = :totpSecret WHERE id = :id")
    void updateAccountTotpSecret(@Param("id") int id, @Param("totpSecret") String totpSecret);


    // Account Audit queries
    @Modifying
    @Query("INSERT INTO logs_ip_actions (account_id, character_guid, realm_id, type, ip, systemnote, unixtime, time) VALUES (:accountId, :charGuid, :realmId, :type, (SELECT last_ip FROM account WHERE id = :accountId), :systemNote, UNIX_TIMESTAMP(NOW()), NOW())")
    void insertAccountIpLog(@Param("accountId") int accountId, @Param("charGuid") int charGuid, @Param("realmId") int realmId, @Param("type") int type, @Param("systemNote") String systemNote);

    @Query("SELECT last_attempt_ip FROM account WHERE id = :accountId")
    List<Map<String, Object>> selectLastAttemptIp(@Param("accountId") int accountId);

    @Query("SELECT last_ip FROM account WHERE id = :accountId")
    List<Map<String, Object>> selectLastIp(@Param("accountId") int accountId);

    // Account Info queries
    @Query("SELECT username, session_key_bnet FROM account WHERE id = :accountId AND LENGTH(session_key_bnet) = 40")
    Optional<Map<String, Object>> selectAccountContinuedSession(@Param("accountId") int accountId);

    @Modifying
    @Query("UPDATE account SET session_key_bnet = :sessionKey WHERE id = :accountId")
    void updateAccountContinuedSession(@Param("sessionKey") byte[] sessionKey, @Param("accountId") int accountId);

    @Modifying
    @Query("UPDATE account SET salt = :salt, verifier = :verifier WHERE id = :accountId")
    void updateAccountLogonInfo(@Param("salt") byte[] salt, @Param("verifier") byte[] verifier, @Param("accountId") int accountId);

    @Query("SELECT id FROM account WHERE username = :username")
    List<Map<String, Object>> selectAccountIdByName(@Param("username") String username);

    @Query("SELECT id, username FROM account WHERE username = :username")
    List<Map<String, Object>> selectAccountListByName(@Param("username") String username);

    @Query("SELECT a.username, a.last_ip, aa.SecurityLevel, a.expansion FROM account a LEFT JOIN account_access aa ON a.id = aa.AccountID WHERE a.id = :accountId")
    List<Map<String, Object>> selectAccountDetails(@Param("accountId") int accountId);

    @Query("SELECT 1 FROM account_access WHERE AccountID = :accountId AND SecurityLevel > :securityLevel")
    List<Map<String, Object>> testAccountAccessLevel(@Param("accountId") int accountId, @Param("securityLevel") int securityLevel);

    // Account Ban queries
    @Modifying
    @Query("UPDATE account_banned SET active = 0 WHERE active = 1 AND unbandate <> bandate AND unbandate <= UNIX_TIMESTAMP()")
    void updateExpiredAccountBans();

    @Query("SELECT id, name FROM rbac_permissions")
    List<RbacPermissions> findAllRbacPermissions();

    @Query("SELECT id, linkedId FROM rbac_linked_permissions")
    List<RbacLinkedPermissions> findAllRbacLinkedPermissions();

    @Query("SELECT id, secId, permissionId FROM rbac_default_permissions  WHERE (realmId = :realmId OR realmId = -1) ORDER BY secId ASC")
    List<RbacDefaultPermissions> queryDefaultPermissionsByRealmId(@Param("realmId") int realmId);

    @Query("""
            SELECT a.id AS accountId, a.username AS accountName, a.session_key_bnet as sessionKey, ba.last_ip, ba.locked, ba.lock_country, a.expansion, a.mutetime, a.client_build, a.locale, a.recruiter, a.os, a.timezone_offset, ba.id AS baId, aa.SecurityLevel,
            bab.unbandate > UNIX_TIMESTAMP() OR bab.unbandate = bab.bandate AS is_bnet_banned, ab.unbandate > UNIX_TIMESTAMP() OR ab.unbandate = ab.bandate AS is_banned, r.id AS rId, r.name AS rName
            FROM account a LEFT JOIN account r ON a.id = r.recruiter LEFT JOIN battlenet_accounts ba ON a.battlenet_account = ba.id "
            LEFT JOIN account_access aa ON a.id = aa.AccountID AND aa.RealmID IN (-1, :realmId) LEFT JOIN battlenet_account_bans bab ON ba.id = bab.id LEFT JOIN account_banned ab ON a.id = ab.id AND ab.active = 1
            WHERE a.username = :userName AND LENGTH(a.session_key_bnet) = 64 ORDER BY aa.RealmID DESC LIMIT 1
            """)
    Optional<AccountInfo> selectAccountInfoByUserName(@Param("userName") String userName, @Param("realmId") int realmId);



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

    @Query("SELECT rc.acctid, rc.numchars, r.id, r.Region, r.Battlegroup FROM realmcharacters rc INNER JOIN realmlist r ON rc.realmid = r.id WHERE rc.acctid = :acctid")
    List<Map<String, Object>> findCharacterCountsByAccountId(@Param("acctid") int acctid);
}