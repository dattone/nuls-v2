package io.nuls.base.api.provider.ledger;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ledger.facade.*;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 13:42
 * @Description: 功能描述
 */
public interface LedgerProvider {

    /**
     * 获取账户余额
     *
     * @param req
     * @return
     */
    Result<AccountBalanceInfo> getBalance(GetBalanceReq req);


    Result<Map> getLocalAsset(GetAssetReq req);

    Result<Map> getContractAsset(ContractAsset req);

    /**
     * 本地资产注册
     *
     * @param req
     * @return
     */
    Result<Map> regLocalAsset(RegLocalAssetReq req);

}
