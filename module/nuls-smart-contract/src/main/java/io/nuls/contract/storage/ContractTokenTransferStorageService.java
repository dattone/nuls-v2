/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.storage;


import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.core.basic.Result;

import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/28
 */
public interface ContractTokenTransferStorageService {

    Result saveTokenTransferInfo(int chainId, byte[] key, ContractTokenTransferInfoPo tx);

    Result deleteTokenTransferInfo(int chainId, byte[] infoKey) throws Exception;

    Result<ContractTokenTransferInfoPo> getTokenTransferInfo(int chainId, byte[] infoKey);

    List<ContractTokenTransferInfoPo> getTokenTransferInfoListByAddress(int chainId, byte[] address);

    List<ContractTokenTransferInfoPo> getTokenTransferInfoListByAddress(int chainId, byte[] address, byte[] txHash);
}
