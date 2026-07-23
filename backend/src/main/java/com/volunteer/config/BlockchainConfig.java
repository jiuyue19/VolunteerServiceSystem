package com.volunteer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class BlockchainConfig {
    
    @Value("${blockchain.rpc-url}")
    private String rpcUrl;
    
    @Value("${blockchain.contract-address}")
    private String contractAddress;
    
    @Value("${blockchain.chain-id}")
    private long chainId;
    
    public Web3j getWeb3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }
    
    public String getContractAddress() {
        return contractAddress;
    }
    
    public String getRpcUrl() {
        return rpcUrl;
    }
    
    public long getChainId() {
        return chainId;
    }
}