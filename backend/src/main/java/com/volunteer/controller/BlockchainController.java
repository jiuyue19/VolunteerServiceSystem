package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.ActivityCheckinMapper;
import com.volunteer.mapper.CertificateLibraryMapper;
import com.volunteer.mapper.ServiceHourReplenishMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.service.BlockchainService;
import com.volunteer.service.PointsCalculationService;
import com.volunteer.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@CrossOrigin
public class BlockchainController {
    
    @Autowired
    private BlockchainService blockchainService;
    
    @Autowired
    private VolunteerMapper volunteerMapper;
    
    @Autowired
    private ActivityCheckinMapper checkinMapper;
    
    @Autowired
    private ServiceHourReplenishMapper replenishMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CertificateLibraryMapper certificateMapper;
    
    @Autowired
    private PointsCalculationService pointsCalculationService;
    
    /**
     * 同步志愿者活动记录到区块链
     * 注意：此方法已废弃，建议使用 /sync-volunteer-activities 接口
     */
    @PostMapping("/sync-hours")
    public Result<Map<String, String>> syncHours(@RequestBody Map<String, Object> request) {
        try {
            return Result.error("写链接口已迁移到前端 MetaMask，请在前端完成交易确认");
        } catch (Exception e) {
            return Result.error("上链失败: " + e.getMessage());
        }
    }
    
    /**
     * 补录时长上链
     */
    @PostMapping("/replenish-hours")
    public Result<Map<String, String>> replenishHours(@RequestBody Map<String, Object> request) {
        try {
            return Result.error("写链接口已迁移到前端 MetaMask，请在前端完成交易确认");
        } catch (Exception e) {
            return Result.error("补录上链失败: " + e.getMessage());
        }
    }
    
    /**
     * 活动记录上链
     */
    @PostMapping("/activity-record")
    public Result<Map<String, String>> activityRecord(@RequestBody Map<String, Object> request) {
        try {
            return Result.error("写链接口已迁移到前端 MetaMask，请在前端完成交易确认");
        } catch (Exception e) {
            return Result.error("活动记录上链失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询链上总时长
     */
    @GetMapping("/total-hours/{volunteerId}")
    public Result<Map<String, Object>> getTotalHours(@PathVariable Long volunteerId) {
        try {
            Volunteer volunteer = volunteerMapper.selectById(volunteerId);
            
            if (volunteer == null) {
                return Result.error("志愿者不存在");
            }
            
            if (volunteer.getWalletAddress() == null || volunteer.getWalletAddress().isEmpty()) {
                return Result.error("志愿者未绑定钱包地址");
            }
            
            BigDecimal chainHours = blockchainService.getTotalHours(volunteer.getWalletAddress());
            BigDecimal replenishHours = blockchainService.getReplenishHours(volunteer.getWalletAddress());
            BigDecimal dbNormalHours = getSafeHours(checkinMapper.getTotalHoursByVolunteer(volunteerId));
            BigDecimal dbReplenishHours = getSafeHours(replenishMapper.getTotalHoursByVolunteer(volunteerId));
            BigDecimal dbTotalHours = dbNormalHours.add(dbReplenishHours);
            
            Map<String, Object> data = new HashMap<>();
            data.put("walletAddress", volunteer.getWalletAddress());
            data.put("chainTotalHours", chainHours);
            data.put("chainReplenishHours", replenishHours);
            data.put("dbNormalHours", dbNormalHours);
            data.put("dbReplenishHours", dbReplenishHours);
            data.put("dbTotalHours", dbTotalHours);
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询链上记录数量
     */
    @GetMapping("/record-count/{volunteerId}")
    public Result<Map<String, Object>> getRecordCount(@PathVariable Long volunteerId) {
        try {
            Volunteer volunteer = volunteerMapper.selectById(volunteerId);
            
            if (volunteer == null) {
                return Result.error("志愿者不存在");
            }
            
            if (volunteer.getWalletAddress() == null || volunteer.getWalletAddress().isEmpty()) {
                return Result.error("志愿者未绑定钱包地址");
            }
            
            int count = blockchainService.getRecordCount(volunteer.getWalletAddress());
            
            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 绑定钱包地址
     */
    @PostMapping("/bind-wallet")
    public Result<String> bindWallet(@RequestBody Map<String, Object> request) {
        try {
            Long volunteerId = Long.valueOf(request.get("volunteerId").toString());
            String walletAddress = request.get("walletAddress").toString();
            
            Volunteer volunteer = volunteerMapper.selectById(volunteerId);
            
            if (volunteer == null) {
                return Result.error("志愿者不存在");
            }
            
            // 验证钱包地址格式（以太坊地址格式：0x开头，42位字符）
            if (!walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
                return Result.error("钱包地址格式不正确");
            }
            
            volunteer.setWalletAddress(walletAddress);
            volunteerMapper.update(volunteer);
            
            return Result.success("钱包地址绑定成功");
        } catch (Exception e) {
            return Result.error("绑定失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取区块链统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getBlockchainStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 统计已上链志愿者数量（已绑定钱包的志愿者）
            int totalVolunteers = volunteerMapper.countVolunteersWithWallet();
            
            // 统计总服务时长（与数据大屏保持一致，使用实时累计时长）
            Double totalHours = volunteerMapper.getTotalHours();
            if (totalHours == null) {
                totalHours = 0.0;
            }
            
            // 统计上链记录总数
            int totalRecords = totalVolunteers;
            
            // 统计已颁发证书数量
            int totalCertificates = certificateMapper.countByStatus("issued");
            
            stats.put("totalRecords", totalRecords);
            stats.put("totalVolunteers", totalVolunteers);
            stats.put("totalCertificates", totalCertificates);
            // 保留两位小数，避免前端出现过长小数
            stats.put("totalHours", Math.round(totalHours * 100.0) / 100.0);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取合约配置信息
     */
    @GetMapping("/contract-info")
    public Result<Map<String, String>> getContractInfo() {
        try {
            Map<String, String> contractInfo = new HashMap<>();
            
            contractInfo.put("contractAddress", blockchainService.getContractAddress());
            contractInfo.put("rpcUrl", blockchainService.getRpcUrl());
            contractInfo.put("chainId", String.valueOf(blockchainService.getChainId()));
            contractInfo.put("adminAddress", blockchainService.getAdminAddress());
            
            return Result.success(contractInfo);
        } catch (Exception e) {
            return Result.error("获取合约信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询志愿者的上链记录详情
     */
    @GetMapping("/chain-records/{volunteerId}")
    public Result<java.util.List<Map<String, Object>>> getChainRecords(@PathVariable Long volunteerId) {
        try {
            Volunteer volunteer = volunteerMapper.selectById(volunteerId);
            
            if (volunteer == null) {
                return Result.error("志愿者不存在");
            }
            
            if (volunteer.getWalletAddress() == null || volunteer.getWalletAddress().isEmpty()) {
                return Result.error("志愿者未绑定钱包地址");
            }
            
            // 获取记录数量
            int recordCount = blockchainService.getRecordCount(volunteer.getWalletAddress());
            
            java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();
            
            // 这里应该调用区块链服务获取详细记录
            // 由于合约的getAllRecords方法返回结构较复杂，暂时返回基本信息
            // 后续可以通过调用getRecord(address, index)方法逐个获取记录详情
            
            for (int i = 0; i < recordCount; i++) {
                try {
                    // 调用合约获取详细记录
                    Map<String, Object> chainRecord = blockchainService.getRecord(volunteer.getWalletAddress(), i);
                    
                    if (chainRecord != null) {
                        chainRecord.put("volunteerName", volunteer.getRealName());
                        chainRecord.put("walletAddress", volunteer.getWalletAddress());
                        chainRecord.put("index", i);
                        records.add(chainRecord);
                    }
                } catch (Exception e) {
                    System.err.println("查询记录 " + i + " 失败: " + e.getMessage());
                }
            }
            
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询所有志愿者的上链记录
     */
    @GetMapping("/all-chain-records")
    public Result<java.util.List<Map<String, Object>>> getAllChainRecords() {
        try {
            // 查询所有已绑定钱包的志愿者
            java.util.List<Volunteer> volunteers = volunteerMapper.selectVolunteersWithWallet();
            
            java.util.List<Map<String, Object>> allRecords = new java.util.ArrayList<>();
            
            for (Volunteer volunteer : volunteers) {
                try {
                    int recordCount = blockchainService.getRecordCount(volunteer.getWalletAddress());
                    
                    for (int i = 0; i < recordCount; i++) {
                        try {
                            Map<String, Object> chainRecord = blockchainService.getRecord(volunteer.getWalletAddress(), i);
                            
                            if (chainRecord != null) {
                                chainRecord.put("volunteerName", volunteer.getRealName());
                                chainRecord.put("walletAddress", volunteer.getWalletAddress());
                                chainRecord.put("index", i);
                                allRecords.add(chainRecord);
                            }
                        } catch (Exception ex) {
                            System.err.println("查询记录失败: " + ex.getMessage());
                        }
                    }
                } catch (Exception e) {
                    // 记录错误但继续处理其他志愿者
                    System.err.println("查询志愿者 " + volunteer.getRealName() + " 的记录失败: " + e.getMessage());
                }
            }
            
            return Result.success(allRecords);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据志愿者ID查询钱包地址
     */
    @GetMapping("/wallet-address/{volunteerId}")
    public Result<Map<String, String>> getWalletAddress(@PathVariable Long volunteerId) {
        try {
            Volunteer volunteer = volunteerMapper.selectById(volunteerId);
            
            if (volunteer == null) {
                return Result.error("志愿者不存在");
            }
            
            Map<String, String> data = new HashMap<>();
            data.put("volunteerId", String.valueOf(volunteerId));
            data.put("volunteerName", volunteer.getRealName());
            data.put("walletAddress", volunteer.getWalletAddress());
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据钱包地址查询链上数据（支持JWT token获取当前志愿者信息）
     */
    @GetMapping("/chain-data-by-wallet")
    public Result<Map<String, Object>> getChainDataByWallet(
            @RequestParam String walletAddress,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("[Blockchain] 开始查询链上数据，钱包地址: " + walletAddress);
            
            // 验证钱包地址格式
            if (walletAddress == null || walletAddress.isEmpty()) {
                System.err.println("[Blockchain] 错误：钱包地址为空");
                return Result.error("钱包地址不能为空");
            }
            
            if (!walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
                System.err.println("[Blockchain] 错误：钱包地址格式不正确 - " + walletAddress);
                return Result.error("钱包地址格式不正确");
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("walletAddress", walletAddress);
            
            // 尝试查询对应的志愿者信息
            Volunteer volunteer = null;
            try {
                System.out.println("[Blockchain] 开始查询志愿者信息...");
                
                // 优先使用JWT token获取当前登录志愿者信息
                if (token != null && !token.trim().isEmpty()) {
                    try {
                        String actualToken = token.replace("Bearer ", "");
                        Claims claims = jwtUtil.parseToken(actualToken);
                        Long userId = claims.get("userId", Long.class);
                        volunteer = volunteerMapper.selectById(userId);
                        System.out.println("[Blockchain] 通过JWT获取当前志愿者信息: " + (volunteer != null ? volunteer.getRealName() : "未找到"));
                    } catch (Exception e) {
                        System.err.println("[Blockchain] JWT解析失败，回退到钱包地址查询: " + e.getMessage());
                        // JWT解析失败，回退到钱包地址查询
                        volunteer = volunteerMapper.selectByWalletAddress(walletAddress);
                    }
                } else {
                    // 没有JWT token，使用钱包地址查询
                    volunteer = volunteerMapper.selectByWalletAddress(walletAddress);
                }
                
                System.out.println("[Blockchain] 志愿者信息查询结果: " + (volunteer != null ? volunteer.getRealName() : "未找到"));
            } catch (Exception e) {
                System.err.println("[Blockchain] 查询志愿者信息失败: " + e.getMessage());
                e.printStackTrace();
                // 继续执行，不因为这个错误而中断
            }
            
            // 查询链上数据（可能失败）
            try {
                System.out.println("[Blockchain] 开始查询区块链服务...");
                
                BigDecimal chainTotalHours = blockchainService.getTotalHours(walletAddress);
                BigDecimal chainReplenishHours = blockchainService.getReplenishHours(walletAddress);
                int recordCount = blockchainService.getRecordCount(walletAddress);
                
                System.out.println("[Blockchain] 链上数据查询成功:");
                System.out.println("  - 总时长: " + chainTotalHours + " 小时");
                System.out.println("  - 补录时长: " + chainReplenishHours + " 小时");
                System.out.println("  - 记录数: " + recordCount);
                
                data.put("chainTotalHours", chainTotalHours);
                data.put("chainReplenishHours", chainReplenishHours);
                data.put("recordCount", recordCount);
                
                // 查询记录详情
                java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();
                for (int i = 0; i < recordCount; i++) {
                    try {
                        Map<String, Object> record = blockchainService.getRecord(walletAddress, i);
                        if (record != null) {
                            record.put("index", i);
                            records.add(record);
                        }
                    } catch (Exception e) {
                        System.err.println("[Blockchain] 查询记录 " + i + " 失败: " + e.getMessage());
                    }
                }
                data.put("records", records);
                
            } catch (Exception e) {
                System.err.println("[Blockchain] 区块链查询失败: " + e.getMessage());
                e.printStackTrace();
                
                // 区块链查询失败时，返回默认值
                data.put("chainTotalHours", BigDecimal.ZERO);
                data.put("chainReplenishHours", BigDecimal.ZERO);
                data.put("recordCount", 0);
                data.put("records", new java.util.ArrayList<>());
                data.put("blockchainError", "区块链节点暂时不可用，显示的是默认值");
            }
            
            // 添加数据库中的志愿者信息
            // 优先使用通过钱包地址查询到的志愿者信息
            if (volunteer != null) {
                System.out.println("[Blockchain] 使用钱包地址查询到的志愿者信息: ID=" + volunteer.getId() + ", 姓名=" + volunteer.getRealName());
                
                // 直接调用PointsCalculationService计算实时累计时长，不依赖数据库中的total_hours字段
                BigDecimal realTimeTotalHours = pointsCalculationService.calculateTotalHours(volunteer.getId());
                BigDecimal dbNormalHours = getSafeHours(checkinMapper.getTotalHoursByVolunteer(volunteer.getId()));
                BigDecimal dbReplenishHours = getSafeHours(replenishMapper.getTotalHoursByVolunteer(volunteer.getId()));
                System.out.println("[Blockchain] 实时计算累计时长: " + realTimeTotalHours + " 小时");
                
                data.put("volunteerId", volunteer.getId());
                data.put("volunteerName", volunteer.getRealName());
                data.put("dbNormalHours", dbNormalHours);
                data.put("dbReplenishHours", dbReplenishHours);
                data.put("dbTotalHours", realTimeTotalHours);
                data.put("points", volunteer.getPoints());
            } else {
                System.err.println("[Blockchain] 警告：未找到钱包地址对应的志愿者信息，钱包地址: " + walletAddress);
                
                // 尝试通过JWT token获取当前登录志愿者的总时长
                BigDecimal currentVolunteerHours = BigDecimal.ZERO;
                BigDecimal currentVolunteerNormalHours = BigDecimal.ZERO;
                BigDecimal currentVolunteerReplenishHours = BigDecimal.ZERO;
                Long currentVolunteerId = null;
                String currentVolunteerName = "未知志愿者";
                
                if (token != null && !token.trim().isEmpty()) {
                    try {
                        String actualToken = token.replace("Bearer ", "");
                        Claims claims = jwtUtil.parseToken(actualToken);
                        Long userId = claims.get("userId", Long.class);
                        Volunteer currentVolunteer = volunteerMapper.selectById(userId);
                        if (currentVolunteer != null) {
                            // 直接调用PointsCalculationService计算实时累计时长
                            currentVolunteerHours = pointsCalculationService.calculateTotalHours(userId);
                            currentVolunteerNormalHours = getSafeHours(checkinMapper.getTotalHoursByVolunteer(userId));
                            currentVolunteerReplenishHours = getSafeHours(replenishMapper.getTotalHoursByVolunteer(userId));
                            currentVolunteerId = currentVolunteer.getId();
                            currentVolunteerName = currentVolunteer.getRealName();
                            System.out.println("[Blockchain] 使用JWT token获取当前登录志愿者信息: ID=" + currentVolunteerId + ", 姓名=" + currentVolunteerName + ", 实时累计时长=" + currentVolunteerHours + " 小时");
                        } else {
                            System.err.println("[Blockchain] 通过JWT token未找到志愿者信息，用户ID: " + userId);
                        }
                    } catch (Exception e) {
                        System.err.println("[Blockchain] 无法获取当前登录志愿者信息: " + e.getMessage());
                    }
                } else {
                    System.err.println("[Blockchain] 没有提供JWT token，无法获取当前登录志愿者信息");
                }
                
                data.put("volunteerId", currentVolunteerId);
                data.put("volunteerName", currentVolunteerName);
                data.put("dbNormalHours", currentVolunteerNormalHours);
                data.put("dbReplenishHours", currentVolunteerReplenishHours);
                data.put("dbTotalHours", currentVolunteerHours);
                data.put("points", 0);
            }
            
            System.out.println("[Blockchain] 查询完成，返回数据");
            return Result.success(data);
            
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            String errorDetail = errorMsg != null ? errorMsg : e.getClass().getName();
            
            System.err.println("[Blockchain] 查询链上数据失败: " + errorDetail);
            System.err.println("[Blockchain] 异常类型: " + e.getClass().getName());
            e.printStackTrace();
            
            return Result.error("查询链上数据失败: " + errorDetail);
        }
    }

    private BigDecimal getSafeHours(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}