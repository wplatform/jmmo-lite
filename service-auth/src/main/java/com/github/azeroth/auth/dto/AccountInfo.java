package com.github.azeroth.auth.dto;

import com.github.azeroth.auth.domain.BNetAccount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class AccountInfo {

    private Long id;
    private String email;
    private Short locked;
    private String lockCountry;
    private Long loginTicketExpiry;
    private String lastIp;
    private boolean banded;
    private boolean permanentlyBanned;
    private Map<Long, GameAccount> accounts;

    public AccountInfo(BNetAccount account) {
        this(
                account.getId(),
                account.getEmail(),
                account.getLocked(),
                account.getLockCountry(),
                account.getLoginTicketExpiry(),
                account.getLastIp(),
                false,
                false,
                account.getAccounts().stream().map(GameAccount::new).collect(Collectors.toUnmodifiableMap(GameAccount::getId, Function.identity(), (v1, v2) -> v1))
        );
    }

    public GameAccount getGameAccount(Long id) {
        return accounts.get(id);
    }

}