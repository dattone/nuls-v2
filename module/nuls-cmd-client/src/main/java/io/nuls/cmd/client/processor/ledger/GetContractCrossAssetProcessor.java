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

package io.nuls.cmd.client.processor.ledger;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.ledger.LedgerProvider;
import io.nuls.base.api.provider.ledger.facade.ContractAsset;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * @author: lanjinsheng
 */
@Component
public class GetContractCrossAssetProcessor implements CommandProcessor {

    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "getContractAsset";
    }

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Ledger;
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> the contract address - require");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getContractAsset <address>--get the assetId by contract address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 1);
        checkAddress(config.getChainId(), args[1]);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Result<Map> result = ledgerProvider.getContractAsset(new ContractAsset(address));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }

}
