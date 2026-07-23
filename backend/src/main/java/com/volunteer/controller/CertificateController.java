package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.CertificateLibrary;
import com.volunteer.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 证书管理Controller
 */
@RestController
@RequestMapping("/api/certificate")
@CrossOrigin
public class CertificateController {
    
    @Autowired
    private CertificateService certificateService;
    
    /**
     * 生成证书（基于链上数据）
     */
    @PostMapping("/generate")
    public Result<CertificateLibrary> generateCertificate(@RequestBody Map<String, Object> request) {
        try {
            Long volunteerId = Long.valueOf(request.get("volunteerId").toString());
            Integer activityCount = request.containsKey("activityCount") ? 
                Integer.valueOf(request.get("activityCount").toString()) : 0;
            String remark = request.containsKey("remark") ? request.get("remark").toString() : null;
            
            CertificateLibrary certificate = certificateService.generateCertificate(volunteerId, activityCount, remark);
            return Result.success(certificate);
        } catch (Exception e) {
            return Result.error("生成证书失败: " + e.getMessage());
        }
    }
    
    /**
     * 发放证书（单个）
     */
    @PostMapping("/issue")
    public Result<String> issueCertificate(@RequestBody Map<String, Object> request) {
        try {
            Long certificateId = Long.valueOf(request.get("certificateId").toString());
            Long issuerId = request.containsKey("issuerId") ? 
                Long.valueOf(request.get("issuerId").toString()) : 1L; // 默认管理员ID为1
            
            certificateService.issueCertificate(certificateId, issuerId);
            return Result.success("证书发放成功");
        } catch (Exception e) {
            return Result.error("证书发放失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量发放证书
     */
    @PostMapping("/batch-issue")
    public Result<String> batchIssueCertificates(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> certificateIds = (List<Long>) request.get("certificateIds");
            Long issuerId = request.containsKey("issuerId") ? 
                Long.valueOf(request.get("issuerId").toString()) : 1L;
            
            certificateService.batchIssueCertificates(certificateIds, issuerId);
            return Result.success("批量发放成功，共发放 " + certificateIds.size() + " 张证书");
        } catch (Exception e) {
            return Result.error("批量发放失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据证书编号查询证书
     */
    @GetMapping("/by-no/{certificateNo}")
    public Result<CertificateLibrary> getCertificateByCertificateNo(@PathVariable String certificateNo) {
        try {
            CertificateLibrary certificate = certificateService.getCertificateByCertificateNo(certificateNo);
            if (certificate == null) {
                return Result.error("证书不存在");
            }
            return Result.success(certificate);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据志愿者姓名和/或钱包地址查询证书
     */
    @GetMapping("/query")
    public Result<List<CertificateLibrary>> queryCertificates(
            @RequestParam(required = false) String volunteerName,
            @RequestParam(required = false) String walletAddress) {
        try {
            List<CertificateLibrary> certificates = certificateService.getCertificateByNameAndWallet(
                volunteerName, walletAddress);
            return Result.success(certificates);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询志愿者的所有证书
     */
    @GetMapping("/volunteer/{volunteerId}")
    public Result<List<CertificateLibrary>> getCertificatesByVolunteerId(@PathVariable Long volunteerId) {
        try {
            List<CertificateLibrary> certificates = certificateService.getCertificatesByVolunteerId(volunteerId);
            return Result.success(certificates);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询所有证书（可按状态筛选）
     */
    @GetMapping("/list")
    public Result<List<CertificateLibrary>> getAllCertificates(
            @RequestParam(required = false) String status) {
        try {
            List<CertificateLibrary> certificates = certificateService.getAllCertificates(status);
            return Result.success(certificates);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 统计证书数量
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getCertificateStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", certificateService.countCertificates(null));
            stats.put("pending", certificateService.countCertificates("pending"));
            stats.put("issued", certificateService.countCertificates("issued"));
            stats.put("revoked", certificateService.countCertificates("revoked"));
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 证书验伪（基础版本 - 仅证书编号）
     */
    @GetMapping("/verify/{certificateNo}")
    public Result<Map<String, Object>> verifyCertificate(@PathVariable String certificateNo) {
        try {
            CertificateLibrary certificate = certificateService.verifyCertificate(certificateNo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("message", "证书真实有效");
            result.put("certificate", certificate);
            
            return Result.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", e.getMessage());
            result.put("certificate", null);
            
            return Result.success(result); // 返回成功但包含验证失败信息
        }
    }
    
    /**
     * 证书验伪（增强版本 - 姓名+编号+哈希）
     */
    @PostMapping("/verify-enhanced")
    public Result<Map<String, Object>> verifyCertificateEnhanced(@RequestBody Map<String, String> request) {
        try {
            String volunteerName = request.get("volunteerName");
            String certificateNo = request.get("certificateNo");
            String certificateHash = request.get("certificateHash");
            
            if (volunteerName == null || certificateNo == null || certificateHash == null) {
                throw new RuntimeException("请提供完整的验证信息：姓名、证书编号、证书哈希");
            }
            
            CertificateLibrary certificate = certificateService.verifyCertificateEnhanced(
                volunteerName, certificateNo, certificateHash);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("message", "✓ 证书真实有效，已通过三重验证（姓名+编号+哈希）和链上数据校验");
            result.put("certificate", certificate);
            
            return Result.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "✗ " + e.getMessage());
            result.put("certificate", null);
            
            return Result.success(result); // 返回成功但包含验证失败信息
        }
    }
    
    /**
     * 撤销证书
     */
    @PostMapping("/revoke")
    public Result<String> revokeCertificate(@RequestBody Map<String, Object> request) {
        try {
            Long certificateId = Long.valueOf(request.get("certificateId").toString());
            Long issuerId = request.containsKey("issuerId") ? 
                Long.valueOf(request.get("issuerId").toString()) : 1L;
            
            certificateService.revokeCertificate(certificateId, issuerId);
            return Result.success("证书已撤销");
        } catch (Exception e) {
            return Result.error("撤销失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除证书
     */
    @DeleteMapping("/{certificateId}")
    public Result<String> deleteCertificate(@PathVariable Long certificateId) {
        try {
            certificateService.deleteCertificate(certificateId);
            return Result.success("证书已删除");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
