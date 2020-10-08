/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.callable;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.*;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.tx.ContractReturnGasTransaction;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.service.ResultAnalyzer;
import io.nuls.contract.service.ResultHanlder;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramInvokeRegisterCmd;
import io.nuls.contract.vm.program.ProgramNewTx;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.core.model.LongUtils;
import io.nuls.core.model.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static io.nuls.core.constant.TxType.CROSS_CHAIN;
import static io.nuls.core.constant.TxType.DELETE_CONTRACT;


/**
 * @author: PierreLuo
 * @date: 2019-03-26
 */
public class ContractBatchEndCallable implements Callable<ContractPackageDto> {

    private int chainId;
    private int blockType;
    private long blockHeight;
    private ContractHelper contractHelper;
    private ResultAnalyzer resultAnalyzer;
    private ResultHanlder resultHanlder;

    public ContractBatchEndCallable(int chainId, int blockType, long blockHeight) {
        this.chainId = chainId;
        this.blockType = blockType;
        this.blockHeight = blockHeight;
        this.contractHelper = SpringLiteContext.getBean(ContractHelper.class);
        this.resultAnalyzer = SpringLiteContext.getBean(ResultAnalyzer.class);
        this.resultHanlder = SpringLiteContext.getBean(ResultHanlder.class);
    }

    @Override
    public ContractPackageDto call() {
        try {
            ChainManager.chainHandle(chainId, blockType);
            BatchInfo batchInfo = contractHelper.getChain(chainId).getBatchInfo();
            BlockHeader currentBlockHeader = batchInfo.getCurrentBlockHeader();
            long blockTime = currentBlockHeader.getTime();

            LinkedHashMap<String, ContractContainer> contractContainerMap = batchInfo.getContractContainerMap();
            Collection<ContractContainer> containerList = contractContainerMap.values();
            CallerResult callerResult = new CallerResult();
            List<CallableResult> resultList = callerResult.getCallableResultList();
            for (ContractContainer container : containerList) {
                container.loadFutureList();
                resultList.add(container.getCallableResult());
            }

            ProgramExecutor batchExecutor = batchInfo.getBatchExecutor();
            String preStateRoot = batchInfo.getPreStateRoot();
            // 合约执行结果归类
            AnalyzerResult analyzerResult = resultAnalyzer.analysis(callerResult.getCallableResultList());
            // 重新执行冲突合约，处理失败合约的金额退还
            List<ContractResult> contractResultList = resultHanlder.handleAnalyzerResult(chainId, batchExecutor, analyzerResult, preStateRoot);
            // 归集[外部模块调用生成的交易]和[合约内部转账交易]
            List<byte[]> offlineTxHashList = new ArrayList<>();
            List<String> resultTxList = new ArrayList<>();
            List<String> resultOrginTxList = new ArrayList<>();
            List<ContractTransferTransaction> contractTransferList;
            List<ProgramInvokeRegisterCmd> invokeRegisterCmds;
            String newTx, newTxHash, orginTxHash;
            ProgramNewTx programNewTx;
            for (ContractResult contractResult : contractResultList) {
                //if (Log.isDebugEnabled()) {
                //    Log.debug("ContractResult Address is {}, Order is {}", AddressTool.getStringAddressByBytes(contractResult.getContractAddress()), contractResult.getTxOrder());
                //}
                orginTxHash = contractResult.getHash();
                // [外部模块调用生成的交易]
                invokeRegisterCmds = contractResult.getInvokeRegisterCmds();
                for (ProgramInvokeRegisterCmd invokeRegisterCmd : invokeRegisterCmds) {
                    if (!invokeRegisterCmd.getCmdRegisterMode().equals(CmdRegisterMode.NEW_TX)) {
                        continue;
                    }
                    programNewTx = invokeRegisterCmd.getProgramNewTx();
                    if (StringUtils.isNotBlank(newTxHash = programNewTx.getTxHash())) {
                        offlineTxHashList.add(RPCUtil.decode(newTxHash));
                    }
                    if (StringUtils.isNotBlank(newTx = programNewTx.getTxString())) {
                        resultTxList.add(newTx);
                        resultOrginTxList.add(orginTxHash);
                    }
                }
                // [合约内部转账交易]
                contractTransferList = contractResult.getContractTransferList();
                for(Transaction tx : contractTransferList) {
                    newTx = RPCUtil.encode(tx.serialize());
                    contractResult.getContractTransferTxStringList().add(newTx);
                    resultTxList.add(newTx);
                    resultOrginTxList.add(orginTxHash);
                    offlineTxHashList.add(tx.getHash().getBytes());
                }
            }
            // 生成退还剩余Gas的交易
            ContractReturnGasTransaction contractReturnGasTx = makeReturnGasTx(chainId, contractResultList, blockTime, contractHelper);
            if (contractReturnGasTx != null) {
                resultTxList.add(RPCUtil.encode(contractReturnGasTx.serialize()));
            }

            ContractPackageDto dto = new ContractPackageDto(offlineTxHashList, resultTxList, resultOrginTxList);
            dto.makeContractResultMap(contractResultList);
            batchInfo.setContractPackageDto(dto);

            Log.info("[Before End Contract Execution Cost Time] BlockHeight is {}, Total Cost Time is {}", currentBlockHeader.getHeight(), System.currentTimeMillis() - batchInfo.getBeginTime());
            return dto;
        } catch (IOException e) {
            Log.error("",e);
            return null;
        } catch (InterruptedException e) {
            Log.error("", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            Log.error("", e);
            return null;
        }
    }

    private static ContractReturnGasTransaction makeReturnGasTx(int chainId, List<ContractResult> resultList, long time, ContractHelper contractHelper) throws IOException {
        int assetsId = contractHelper.getChain(chainId).getConfig().getAssetId();
        ContractWrapperTransaction wrapperTx;
        ContractData contractData;
        Map<ByteArrayWrapper, BigInteger> returnMap = new HashMap<>();
        for (ContractResult contractResult : resultList) {
            wrapperTx = contractResult.getTx();
            // 终止合约不消耗Gas，跳过
            if (wrapperTx.getType() == DELETE_CONTRACT) {
                continue;
            }
            // add by pierre at 2019-12-03 代币跨链交易的合约调用是系统调用，不计算Gas消耗，跳过
            if (wrapperTx.getType() == CROSS_CHAIN) {
                continue;
            }
            // end code by pierre
            contractData = wrapperTx.getContractData();
            long realGasUsed = contractResult.getGasUsed();
            long txGasUsed = contractData.getGasLimit();
            long returnGas;

            BigInteger returnValue;
            if (txGasUsed > realGasUsed) {
                returnGas = txGasUsed - realGasUsed;
                returnValue = BigInteger.valueOf(LongUtils.mul(returnGas, contractData.getPrice()));

                ByteArrayWrapper sender = new ByteArrayWrapper(contractData.getSender());
                BigInteger senderValue = returnMap.get(sender);
                if (senderValue == null) {
                    senderValue = returnValue;
                } else {
                    senderValue = senderValue.add(returnValue);
                }
                returnMap.put(sender, senderValue);
            }
        }
        if (!returnMap.isEmpty()) {
            CoinData coinData = new CoinData();
            List<CoinTo> toList = coinData.getTo();
            Set<Map.Entry<ByteArrayWrapper, BigInteger>> entries = returnMap.entrySet();
            CoinTo returnCoin;
            for (Map.Entry<ByteArrayWrapper, BigInteger> entry : entries) {
                returnCoin = new CoinTo(entry.getKey().getBytes(), chainId, assetsId, entry.getValue(), 0L);
                toList.add(returnCoin);
            }
            ContractReturnGasTransaction tx = new ContractReturnGasTransaction();
            tx.setTime(time);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            return tx;
        }

        return null;
    }

}
