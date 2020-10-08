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
package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.base.basic.AddressTool;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.vm.*;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.exception.ErrorException;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.program.*;
import io.nuls.contract.vm.program.impl.ProgramInvoke;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeAddress {

    public static final String TYPE = "io/nuls/contract/sdk/Address";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case balance:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return balance(methodCode, methodArgs, frame);
                }
            case totalBalance:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return totalBalance(methodCode, methodArgs, frame);
                }
            case transfer:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return transfer(methodCode, methodArgs, frame);
                }
            case call:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return call(methodCode, methodArgs, frame);
                }
            case callWithReturnValue:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return callWithReturnValue(methodCode, methodArgs, frame);
                }
            case valid:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return valid(methodCode, methodArgs, frame);
                }
            case isContract:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return isContract(methodCode, methodArgs, frame);
                }
            default:
                if (check) {
                    return NOT_SUPPORT_NATIVE;
                } else {
                    frame.nonsupportMethod(methodCode);
                    return null;
                }
        }
    }

    private static BigInteger balance(byte[] address, Frame frame) {
        //if (!frame.vm.getRepository().isExist(address)) {
        //    return BigInteger.ZERO;
        //} else {
        return frame.vm.getProgramExecutor().getAccount(address).getBalance();
        //}
    }

    private static BigInteger totalBalance(byte[] address, Frame frame) {
        //if (!frame.vm.getRepository().isExist(address)) {
        //    return BigInteger.ZERO;
        //} else {
        return frame.vm.getProgramExecutor().getAccount(address).getTotalBalance();
        //}
    }

    public static final String balance = TYPE + "." + "balance" + "()Ljava/math/BigInteger;";

    /**
     * native
     *
     * @see Address#balance()
     */
    private static Result balance(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        String address = frame.heap.runToString(objectRef);
        BigInteger balance = balance(NativeAddress.toBytes(address), frame);
        ObjectRef balanceRef = frame.heap.newBigInteger(balance.toString());
        Result result = NativeMethod.result(methodCode, balanceRef, frame);
        return result;
    }

    public static final String totalBalance = TYPE + "." + "totalBalance" + "()Ljava/math/BigInteger;";

    /**
     * native
     *
     * @see Address#totalBalance()
     */
    private static Result totalBalance(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        String address = frame.heap.runToString(objectRef);
        BigInteger totalBalance = totalBalance(NativeAddress.toBytes(address), frame);
        ObjectRef totalBalanceRef = frame.heap.newBigInteger(totalBalance.toString());
        Result result = NativeMethod.result(methodCode, totalBalanceRef, frame);
        return result;
    }

    public static final String transfer = TYPE + "." + "transfer" + "(Ljava/math/BigInteger;)V";

    /**
     * native
     *
     * @see Address#transfer(BigInteger)
     */
    private static Result transfer(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef addressRef = methodArgs.objectRef;
        ObjectRef valueRef = (ObjectRef) methodArgs.invokeArgs[0];
        String address = frame.heap.runToString(addressRef);
        BigInteger value = frame.heap.toBigInteger(valueRef);
        byte[] from = frame.vm.getProgramInvoke().getContractAddress();
        byte[] to = NativeAddress.toBytes(address);
        if (Arrays.equals(from, to)) {
            throw new ErrorException(String.format("Cannot transfer from %s to %s", NativeAddress.toString(from), address), frame.vm.getGasUsed(), null);
        }
        checkBalance(from, value, frame);

        frame.vm.addGasUsed(GasCost.TRANSFER);

        if (frame.heap.existContract(to)) {
            //String address;
            String methodName = "_payable";
            String methodDesc = "()V";
            String[][] args = null;
            //BigInteger value;
            call(address, methodName, methodDesc, args, value, frame);
        } else {
            frame.vm.getProgramExecutor().getAccount(from).addBalance(value.negate());
            ProgramTransfer programTransfer = new ProgramTransfer(from, to, value);
            frame.vm.getTransfers().add(programTransfer);
            // add by pierre at 2019-11-23 标记 按合约执行顺序添加合约生成交易，按此顺序处理合约生成交易的业务 不确定 需要协议升级
            frame.vm.getOrderedInnerTxs().add(programTransfer);
            // end code by pierre
        }

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String call = TYPE + "." + "call" + "(Ljava/lang/String;Ljava/lang/String;[[Ljava/lang/String;Ljava/math/BigInteger;)V";

    /**
     * native
     *
     * @see Address#call(String, String, String[][], BigInteger)
     */
    private static Result call(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        return call(methodCode, methodArgs, frame, false);
    }

    private static Result call(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean returnResult) {
        ObjectRef addressRef = methodArgs.objectRef;
        ObjectRef methodNameRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef methodDescRef = (ObjectRef) methodArgs.invokeArgs[1];
        ObjectRef argsRef = (ObjectRef) methodArgs.invokeArgs[2];
        ObjectRef valueRef = (ObjectRef) methodArgs.invokeArgs[3];

        String address = frame.heap.runToString(addressRef);
        String methodName = frame.heap.runToString(methodNameRef);
        String methodDesc = frame.heap.runToString(methodDescRef);
        String[][] args = getArgs(argsRef, frame);
        BigInteger value = frame.heap.toBigInteger(valueRef);
        if (value == null) {
            value = BigInteger.ZERO;
        }

        ProgramResult programResult = call(address, methodName, methodDesc, args, value, frame);

        if (!programResult.isSuccess()) {
            return new Result();
        }

        Object resultValue = null;
        if (returnResult && programResult.isSuccess()) {
            resultValue = frame.heap.newString(programResult.getResult());
        }

        Result result = NativeMethod.result(methodCode, resultValue, frame);
        return result;
    }

    public static final String callWithReturnValue = TYPE + "." + "callWithReturnValue" + "(Ljava/lang/String;Ljava/lang/String;[[Ljava/lang/String;Ljava/math/BigInteger;)Ljava/lang/String;";

    private static Result callWithReturnValue(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        return call(methodCode, methodArgs, frame, true);
    }

    private static String[][] getArgs(ObjectRef argsRef, Frame frame) {
        if (argsRef == null) {
            return null;
        }

        int length = argsRef.getDimensions()[0];
        String[][] array = new String[length][0];
        for (int i = 0; i < length; i++) {
            ObjectRef objectRef = (ObjectRef) frame.heap.getArray(argsRef, i);
            String[] ss = (String[]) frame.heap.getObject(objectRef);
            array[i] = ss;
        }

        return array;
    }

    public static ProgramResult call(String address, String methodName, String methodDesc, String[][] args, BigInteger value, Frame frame) {
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new ErrorException(String.format("amount less than zero, value=%s", value), frame.vm.getGasUsed(), null);
        }

        ProgramInvoke programInvoke = frame.vm.getProgramInvoke();
        ProgramCall programCall = new ProgramCall();
        programCall.setNumber(programInvoke.getNumber());
        programCall.setSender(programInvoke.getContractAddress());
        programCall.setValue(value != null ? value : BigInteger.ZERO);
        programCall.setGasLimit(programInvoke.getGasLimit() - frame.vm.getGasUsed());
        programCall.setPrice(programInvoke.getPrice());
        programCall.setContractAddress(NativeAddress.toBytes(address));
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);
        programCall.setEstimateGas(programInvoke.isEstimateGas());
        programCall.setViewMethod(programInvoke.isViewMethod());
        programCall.setInternalCall(true);

        if (programCall.getValue().compareTo(BigInteger.ZERO) > 0) {
            checkBalance(programCall.getSender(), programCall.getValue(), frame);
            frame.vm.getProgramExecutor().getAccount(programCall.getSender()).addBalance(programCall.getValue().negate());
            ProgramTransfer programTransfer = new ProgramTransfer(programCall.getSender(), programCall.getContractAddress(), programCall.getValue());
            frame.vm.getTransfers().add(programTransfer);
            // add by pierre at 2019-11-23 标记 按合约执行顺序添加合约生成交易，按此顺序处理合约生成交易的业务 不确定 需要协议升级
            frame.vm.getOrderedInnerTxs().add(programTransfer);
            // end code by pierre
        }

        ProgramInternalCall programInternalCall = new ProgramInternalCall();
        programInternalCall.setSender(programCall.getSender());
        programInternalCall.setValue(programCall.getValue());
        programInternalCall.setContractAddress(programCall.getContractAddress());
        programInternalCall.setMethodName(programCall.getMethodName());
        programInternalCall.setMethodDesc(programCall.getMethodDesc());
        programInternalCall.setArgs(programCall.getArgs());

        frame.vm.getInternalCalls().add(programInternalCall);

        ProgramResult programResult = frame.vm.getProgramExecutor().callProgramExecutor().call(programCall);

        frame.vm.addGasUsed(programResult.getGasUsed());
        if (programResult.isSuccess()) {
            frame.vm.getTransfers().addAll(programResult.getTransfers());
            frame.vm.getInternalCalls().addAll(programResult.getInternalCalls());
            frame.vm.getEvents().addAll(programResult.getEvents());
            frame.vm.getDebugEvents().addAll(programResult.getDebugEvents());
            frame.vm.getInvokeRegisterCmds().addAll(programResult.getInvokeRegisterCmds());
            frame.vm.getOrderedInnerTxs().addAll(programResult.getOrderedInnerTxs());
            return programResult;
        } else {
            frame.throwRuntimeException(programResult.getErrorMessage());
            return programResult;
        }
        //else if (programResult.isError()) {
        //    throw new ErrorException(programResult.getErrorMessage(), programResult.getGasUsed(), programResult.getStackTrace());
        //} else {
        //    throw new RuntimeException("error contract status");
        //}

    }

    private static void checkBalance(byte[] address, BigInteger value, Frame frame) {
        if (value == null || value.compareTo(BigInteger.ZERO) <= 0) {
            throw new ErrorException(String.format("transfer amount error, value=%s", value), frame.vm.getGasUsed(), null);
        }
        BigInteger balance = frame.vm.getProgramExecutor().getAccount(address).getBalance();
        if (balance.compareTo(value) < 0) {
            if (frame.vm.getProgramContext().isEstimateGas()) {
                balance = value;
            } else {
                throw new ErrorException(String.format("contract[%s] not enough balance", toString(address)), frame.vm.getGasUsed(), null);
            }
        }
    }

    public static final String valid = TYPE + "." + "valid" + "(Ljava/lang/String;)V";

    /**
     * native
     *
     * @see Address#valid(String)
     */
    private static Result valid(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String str = frame.heap.runToString(objectRef);
        boolean valided = validAddress(frame.vm.getProgramExecutor().getCurrentChainId(), str);
        if (!valided) {
            frame.throwRuntimeException(String.format("address[%s] error", str));
        }
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String isContract = TYPE + "." + "isContract" + "()Z";

    private static Result isContract(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef addressRef = methodArgs.objectRef;
        String address = frame.heap.runToString(addressRef);
        boolean verify = isContract(NativeAddress.toBytes(address), frame);
        Result result = NativeMethod.result(methodCode, verify, frame);
        return result;
    }

    public static String toString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return AddressTool.getStringAddressByBytes(bytes);
        } catch (Exception e) {
            throw new RuntimeException("address error", e);
        }
    }

    public static byte[] toBytes(String str) {
        if (str == null) {
            return null;
        }
        try {
            return AddressTool.getAddress(str);
        } catch (Exception e) {
            throw new RuntimeException("address error", e);
        }
    }

    public static boolean isContract(byte[] address, Frame frame) {
        byte[] contractAddress = frame.vm.getProgramInvoke().getContractAddress();
        if (Arrays.equals(contractAddress, address)) {
            return true;
        }
        if (frame.heap.existContract(address)) {
            return true;
        }
        return false;
    }

    public static boolean validAddress(int chainId, String str) {
        return AddressTool.validAddress(chainId, str);
    }

}
