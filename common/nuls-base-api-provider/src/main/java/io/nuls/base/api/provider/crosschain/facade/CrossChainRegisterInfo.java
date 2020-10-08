package io.nuls.base.api.provider.crosschain.facade;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 11:41
 * @Description: 功能描述
 */
public class CrossChainRegisterInfo {

    /**
     * chainId : 链标识
     * chainName : 链名称
     * addressType : 链上创建的账户的地址类型：1生态内 2非生态内
     * magicNumber : 网络魔法参数
     * minAvailableNodeNum : 最小可用节点数量
     * txConfirmedBlockNum : 跨链交易确认块数
     * regAddress : NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp
     * regTxHash : FFFFF
     * selfAssetKeyList : 链下注册的资产列表，由chainId_assetId 组合的资产key值
     * totalAssetKeyList : 链下流通的资产列表，由chainId_assetId 组合的资产key值
     * createTime : 创建时间
     * seeds : xxx.xxx.xxx.xxx:8001,xxx.xxx.xxx.xxx:8002
     */

    private int chainId;
    private String chainName;
    private String addressType;
    private String addressPrefix;
    private long magicNumber;
    private int minAvailableNodeNum;
    private String regAddress;
    private String regTxHash;
    private long createTime;
    List<String> verifierList;
    /**
     * 按100来计算拜占庭比例
     */
    int signatureByzantineRatio = 0;
    /**
     * 最大签名数量
     */
    int maxSignatureCount = 0;

    private List<String> selfAssetKeyList;
    private List<String> totalAssetKeyList;

    String mainNetVerifierSeeds = "";
    String mainNetCrossConnectSeeds = "";

    private boolean enable = true;


    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getMinAvailableNodeNum() {
        return minAvailableNodeNum;
    }

    public void setMinAvailableNodeNum(int minAvailableNodeNum) {
        this.minAvailableNodeNum = minAvailableNodeNum;
    }

    public String getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(String regAddress) {
        this.regAddress = regAddress;
    }

    public String getRegTxHash() {
        return regTxHash;
    }

    public void setRegTxHash(String regTxHash) {
        this.regTxHash = regTxHash;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }


    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    public List<String> getSelfAssetKeyList() {
        return selfAssetKeyList;
    }

    public void setSelfAssetKeyList(List<String> selfAssetKeyList) {
        this.selfAssetKeyList = selfAssetKeyList;
    }

    public List<String> getTotalAssetKeyList() {
        return totalAssetKeyList;
    }

    public void setTotalAssetKeyList(List<String> totalAssetKeyList) {
        this.totalAssetKeyList = totalAssetKeyList;
    }

    public List<String> getVerifierList() {
        return verifierList;
    }

    public void setVerifierList(List<String> verifierList) {
        this.verifierList = verifierList;
    }

    public int getSignatureByzantineRatio() {
        return signatureByzantineRatio;
    }

    public void setSignatureByzantineRatio(int signatureByzantineRatio) {
        this.signatureByzantineRatio = signatureByzantineRatio;
    }

    public int getMaxSignatureCount() {
        return maxSignatureCount;
    }

    public void setMaxSignatureCount(int maxSignatureCount) {
        this.maxSignatureCount = maxSignatureCount;
    }

    public String getMainNetVerifierSeeds() {
        return mainNetVerifierSeeds;
    }

    public void setMainNetVerifierSeeds(String mainNetVerifierSeeds) {
        this.mainNetVerifierSeeds = mainNetVerifierSeeds;
    }

    public String getMainNetCrossConnectSeeds() {
        return mainNetCrossConnectSeeds;
    }

    public void setMainNetCrossConnectSeeds(String mainNetCrossConnectSeeds) {
        this.mainNetCrossConnectSeeds = mainNetCrossConnectSeeds;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
