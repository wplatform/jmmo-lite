package com.github.azeroth.auth.repository;

import com.github.azeroth.auth.domain.BNetAccount;
import com.github.azeroth.auth.domain.BattlenetAccountBan;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface BNetAccountRepository extends CrudRepository<BNetAccount, Long> {

    @Query("FROM BNetAccount a LEFT JOIN FETCH a.accounts b WHERE a.loginTicket = :loginTicket")
    Optional<BNetAccount> queryByLoginTicket(@Param("loginTicket") String loginTicket);

    @Query("from BattlenetAccountBan b where b.id.id = :accountId")
    List<BattlenetAccountBan> findBattlenetAccountBanByAccount(Long accountId);

}