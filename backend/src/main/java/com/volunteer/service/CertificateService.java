package com.volunteer.service;

import com.volunteer.entity.CertificateLibrary;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.CertificateLibraryMapper;
import com.volunteer.mapper.VolunteerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 证书管理服务
 */
@Service
public class CertificateService {
    
    @Autowired
    private CertificateLibraryMapper certificateMapper;
    
    @Autowired
    private VolunteerMapper volunteerMapper;
    
    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private VolunteerStatsService volunteerStatsService;
    
    /**
     * 生成证书编号
     * 格式：CERT-{钱包地址前4位}-{时间戳后4位}
     */
    public String generateCertificateNo(String walletAddress) {
        String prefix = walletAddress.substring(2, 6).toUpperCase();
        String suffix = String.valueOf(System.currentTimeMillis()).substring(9);
        return "CERT-" + prefix + "-" + suffix;
    }
    
    /**
     * 生成证书哈希
     * 基于：志愿者姓名 + 钱包地址 + 链上总时长 + 链上补录时长 + 时间戳
     * 使用 SHA-256 算法
     */
    public String generateCertificateHash(String volunteerName, String walletAddress, 
                                          BigDecimal chainTotalHours, BigDecimal chainReplenishHours, 
                                          long timestamp) {
        try {
            String data = volunteerName + walletAddress + chainTotalHours.toString() + 
                         chainReplenishHours.toString() + timestamp;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            // 转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成证书哈希失败: " + e.getMessage());
        }
    }
    
    /**
     * 为志愿者生成证书（基于链上数据）
     * @param volunteerId 志愿者ID
     * @param activityCount 活动数量（从数据库统计）
     * @param remark 备注
     * @return 证书记录
     */
    @Transactional
    public CertificateLibrary generateCertificate(Long volunteerId, Integer activityCount, String remark) {
        // 1. 获取志愿者信息
        Volunteer volunteer = volunteerMapper.selectById(volunteerId);
        if (volunteer == null) {
            throw new RuntimeException("志愿者不存在");
        }
        
        if (volunteer.getWalletAddress() == null || volunteer.getWalletAddress().isEmpty()) {
            throw new RuntimeException("志愿者未绑定钱包地址");
        }

        Integer consistentActivityCount = volunteerStatsService.getActivityCount(volunteerId);
        
        // 2. 从区块链获取最新数据
        BigDecimal chainTotalHours = blockchainService.getTotalHours(volunteer.getWalletAddress());
        BigDecimal chainReplenishHours = blockchainService.getReplenishHours(volunteer.getWalletAddress());
        
        // 3. 生成证书编号
        String certificateNo = generateCertificateNo(volunteer.getWalletAddress());
        
        // 4. 生成证书哈希（基于链上数据）
        long timestamp = System.currentTimeMillis();
        String certificateHash = generateCertificateHash(
            volunteer.getRealName(), 
            volunteer.getWalletAddress(), 
            chainTotalHours, 
            chainReplenishHours, 
            timestamp
        );
        
        // 5. 创建证书记录
        CertificateLibrary certificate = new CertificateLibrary();
        certificate.setCertificateNo(certificateNo);
        certificate.setCertificateHash(certificateHash);
        certificate.setVolunteerId(volunteerId);
        certificate.setVolunteerName(volunteer.getRealName());
        certificate.setWalletAddress(volunteer.getWalletAddress());
        certificate.setTotalHours(volunteer.getTotalHours());
        certificate.setReplenishHours(BigDecimal.ZERO); // 可以从数据库统计补录时长
        certificate.setActivityCount(consistentActivityCount != null ? consistentActivityCount : 0);
        certificate.setChainTotalHours(chainTotalHours);
        certificate.setChainReplenishHours(chainReplenishHours);
        certificate.setStatus("pending"); // 待发放
        certificate.setRemark(remark);
        
        // 6. 保存到证书库
        certificateMapper.insert(certificate);
        
        return certificate;
    }
    
    /**
     * 发放证书（单个）
     */
    @Transactional
    public void issueCertificate(Long certificateId, Long issuerId) {
        certificateMapper.updateStatus(certificateId, "issued", new Date(), issuerId);
    }
    
    /**
     * 批量发放证书
     */
    @Transactional
    public void batchIssueCertificates(List<Long> certificateIds, Long issuerId) {
        certificateMapper.batchIssue(certificateIds, new Date(), issuerId);
    }
    
    /**
     * 根据证书编号查询证书
     */
    public CertificateLibrary getCertificateByCertificateNo(String certificateNo) {
        return certificateMapper.selectByCertificateNo(certificateNo);
    }
    
    /**
     * 根据志愿者姓名和钱包地址查询证书
     */
    public List<CertificateLibrary> getCertificateByNameAndWallet(String volunteerName, String walletAddress) {
        if (walletAddress != null && !walletAddress.isEmpty()) {
            // 优先按钱包地址查询（更准确）
            return certificateMapper.selectByWalletAddress(walletAddress);
        } else if (volunteerName != null && !volunteerName.isEmpty()) {
            // 按姓名模糊查询
            return certificateMapper.selectByVolunteerName(volunteerName);
        }
        throw new RuntimeException("请提供志愿者姓名或钱包地址");
    }
    
    /**
     * 查询志愿者的所有证书
     */
    public List<CertificateLibrary> getCertificatesByVolunteerId(Long volunteerId) {
        return certificateMapper.selectByVolunteerId(volunteerId);
    }
    
    /**
     * 查询所有证书（可按状态筛选）
     */
    public List<CertificateLibrary> getAllCertificates(String status) {
        return certificateMapper.selectAll(status);
    }
    
    /**
     * 统计证书数量
     */
    public int countCertificates(String status) {
        return certificateMapper.countByStatus(status);
    }
    
    /**
     * 撤销证书
     */
    @Transactional
    public void revokeCertificate(Long certificateId, Long issuerId) {
        certificateMapper.updateStatus(certificateId, "revoked", new Date(), issuerId);
    }
    
    /**
     * 删除证书
     */
    @Transactional
    public void deleteCertificate(Long certificateId) {
        certificateMapper.deleteById(certificateId);
    }
    
    /**
     * 验证证书真伪（基础版本 - 仅证书编号）
     * @param certificateNo 证书编号
     * @return 验证结果
     */
    public CertificateLibrary verifyCertificate(String certificateNo) {
        CertificateLibrary certificate = certificateMapper.selectByCertificateNo(certificateNo);
        if (certificate == null) {
            throw new RuntimeException("证书不存在，可能是伪造证书");
        }
        
        // 验证链上数据是否匹配
        try {
            BigDecimal chainHours = blockchainService.getTotalHours(certificate.getWalletAddress());
            if (chainHours.compareTo(certificate.getChainTotalHours()) != 0) {
                throw new RuntimeException("证书数据与链上数据不一致，可能已被篡改");
            }
        } catch (Exception e) {
            throw new RuntimeException("无法验证链上数据：" + e.getMessage());
        }
        
        return certificate;
    }
    
    /**
     * 验证证书真伪（增强版本 - 姓名+编号+哈希）
     * @param volunteerName 志愿者姓名
     * @param certificateNo 证书编号
     * @param certificateHash 证书哈希
     * @return 验证结果
     */
    public CertificateLibrary verifyCertificateEnhanced(String volunteerName, String certificateNo, String certificateHash) {
        // 1. 根据证书编号查询证书
        CertificateLibrary certificate = certificateMapper.selectByCertificateNo(certificateNo);
        if (certificate == null) {
            throw new RuntimeException("证书不存在，可能是伪造证书");
        }
        
        // 2. 验证志愿者姓名
        if (!certificate.getVolunteerName().equals(volunteerName)) {
            throw new RuntimeException("证书姓名不匹配，验证失败");
        }
        
        // 3. 验证证书哈希
        if (!certificate.getCertificateHash().equals(certificateHash)) {
            throw new RuntimeException("证书哈希不匹配，可能是伪造或篡改的证书");
        }
        
        // 4. 验证链上数据是否匹配
        try {
            BigDecimal chainHours = blockchainService.getTotalHours(certificate.getWalletAddress());
            BigDecimal chainReplenishHours = blockchainService.getReplenishHours(certificate.getWalletAddress());
            
            if (chainHours.compareTo(certificate.getChainTotalHours()) != 0) {
                throw new RuntimeException("证书总时长与链上数据不一致");
            }
            
            if (chainReplenishHours.compareTo(certificate.getChainReplenishHours()) != 0) {
                throw new RuntimeException("证书补录时长与链上数据不一致");
            }
        } catch (Exception e) {
            throw new RuntimeException("无法验证链上数据：" + e.getMessage());
        }
        
        return certificate;
    }
}
