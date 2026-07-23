package com.volunteer.service;

import com.volunteer.config.BlockchainConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Service
public class BlockchainService {

    private static final String READONLY_CALLER = "0x0000000000000000000000000000000000000000";

    @Autowired
    private BlockchainConfig blockchainConfig;

    /**
     * 查询志愿者链上总服务时长
     * @param volunteerAddress 志愿者地址
     * @return 总时长（小时）
     */
    public BigDecimal getTotalHours(String volunteerAddress) {
        try {
            Function function = new Function(
                "getTotalMinutes",
                Arrays.asList(new Address(volunteerAddress)),
                Arrays.asList(new TypeReference<Uint256>() {})
            );

            BigInteger minutesInUnit = callContract(function);

            // 合约返回的是分钟*100，转换为小时
            // 例如: 9000 / 100 = 90分钟 / 60 = 1.5小时
            return new BigDecimal(minutesInUnit)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException("查询链上总时长失败: " + e.getMessage());
        }
    }

    /**
     * 查询志愿者补录时长
     * @param volunteerAddress 志愿者地址
     * @return 补录时长（小时）
     */
    public BigDecimal getReplenishHours(String volunteerAddress) {
        try {
            Function function = new Function(
                "getReplenishMinutes",
                Arrays.asList(new Address(volunteerAddress)),
                Arrays.asList(new TypeReference<Uint256>() {})
            );

            BigInteger minutesInUnit = callContract(function);

            // 转换为小时
            return new BigDecimal(minutesInUnit)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException("查询补录时长失败: " + e.getMessage());
        }
    }

    /**
     * 查询志愿者上链记录数量
     * @param volunteerAddress 志愿者地址
     * @return 记录数量
     */
    public int getRecordCount(String volunteerAddress) {
        try {
            Function function = new Function(
                "getRecordCount",
                Arrays.asList(new Address(volunteerAddress)),
                Arrays.asList(new TypeReference<Uint256>() {})
            );

            BigInteger count = callContract(function);
            return count.intValue();
        } catch (Exception e) {
            throw new RuntimeException("查询记录数量失败: " + e.getMessage());
        }
    }

    /**
     * 查询志愿者指定索引的活动记录
     * @param volunteerAddress 志愿者地址
     * @param index 记录索引
     * @return 记录详情 (activityName, duration, timestamp, isReplenish)
     */
    public Map<String, Object> getRecord(String volunteerAddress, int index) {
        try {
            Function function = new Function(
                "getRecord",
                Arrays.asList(new Address(volunteerAddress), new Uint256(index)),
                Arrays.asList(
                    new TypeReference<Utf8String>() {},  // activityName
                    new TypeReference<Uint256>() {},      // duration
                    new TypeReference<Uint256>() {},      // timestamp
                    new TypeReference<org.web3j.abi.datatypes.Bool>() {} // isReplenish
                )
            );

            String encodedFunction = FunctionEncoder.encode(function);
            EthCall response = blockchainConfig.getWeb3j().ethCall(
                Transaction.createEthCallTransaction(
                    READONLY_CALLER,
                    blockchainConfig.getContractAddress(),
                    encodedFunction
                ),
                DefaultBlockParameterName.LATEST
            ).send();

            List<Type> result = FunctionReturnDecoder.decode(
                response.getValue(),
                function.getOutputParameters()
            );

            if (result.isEmpty() || result.size() < 4) {
                return null;
            }

            String activityName = (String) result.get(0).getValue();
            BigInteger durationInUnit = (BigInteger) result.get(1).getValue();
            BigInteger timestamp = (BigInteger) result.get(2).getValue();
            Boolean isReplenish = (Boolean) result.get(3).getValue();

            // 转换时长为小时
            BigDecimal hours = new BigDecimal(durationInUnit)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);

            Map<String, Object> record = new HashMap<>();
            record.put("activityName", activityName);
            record.put("duration", hours);
            record.put("timestamp", timestamp.longValue());
            record.put("isReplenish", isReplenish);

            return record;
        } catch (Exception e) {
            throw new RuntimeException("查询活动记录失败: " + e.getMessage());
        }
    }

    /**
     * 调用合约查询方法的通用函数
     */
    private BigInteger callContract(Function function) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = blockchainConfig.getWeb3j().ethCall(
            Transaction.createEthCallTransaction(
                READONLY_CALLER,
                blockchainConfig.getContractAddress(),
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).send();

        List<Type> result = FunctionReturnDecoder.decode(
            response.getValue(),
            function.getOutputParameters()
        );

        if (result.isEmpty()) {
            return BigInteger.ZERO;
        }

        return (BigInteger) result.get(0).getValue();
    }

    /**
     * 获取合约地址
     */
    public String getContractAddress() {
        return blockchainConfig.getContractAddress();
    }
    
    /**
     * 获取RPC URL
     */
    public String getRpcUrl() {
        return blockchainConfig.getRpcUrl();
    }
    
    /**
     * 获取链ID
     */
    public long getChainId() {
        return blockchainConfig.getChainId();
    }
    
    /**
     * 获取管理员地址
     */
    public String getAdminAddress() {
        return "";
    }
}