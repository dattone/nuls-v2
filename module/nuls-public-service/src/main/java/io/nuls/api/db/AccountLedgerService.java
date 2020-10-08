package io.nuls.api.db;

import io.nuls.api.model.po.AccountLedgerInfo;

import java.util.List;
import java.util.Map;

public interface AccountLedgerService {

    void initCache();

    AccountLedgerInfo getAccountLedgerInfo(int chainId, String key);

    void saveLedgerList(int chainId, Map<String, AccountLedgerInfo> accountLedgerInfoMap);

    List<AccountLedgerInfo> getAccountLedgerInfoList(int chainId, String address);

    List<AccountLedgerInfo> getAccountCrossLedgerInfoList(int chainId, String address);

}
