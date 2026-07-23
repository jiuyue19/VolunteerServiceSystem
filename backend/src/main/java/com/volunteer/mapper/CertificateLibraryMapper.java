package com.volunteer.mapper;

import com.volunteer.entity.CertificateLibrary;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 证书库Mapper
 */
@Mapper
public interface CertificateLibraryMapper {
    
    /**
     * 插入证书记录
     */
    @Insert("INSERT INTO certificate_library (certificate_no, certificate_hash, volunteer_id, volunteer_name, wallet_address, " +
            "total_hours, replenish_hours, activity_count, chain_total_hours, chain_replenish_hours, " +
            "tx_hash, status, remark) " +
            "VALUES (#{certificateNo}, #{certificateHash}, #{volunteerId}, #{volunteerName}, #{walletAddress}, " +
            "#{totalHours}, #{replenishHours}, #{activityCount}, #{chainTotalHours}, #{chainReplenishHours}, " +
            "#{txHash}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CertificateLibrary certificate);
    
    /**
     * 根据ID查询证书
     */
    @Select("SELECT * FROM certificate_library WHERE id = #{id}")
    CertificateLibrary selectById(Long id);
    
    /**
     * 根据证书编号查询
     */
    @Select("SELECT * FROM certificate_library WHERE certificate_no = #{certificateNo}")
    CertificateLibrary selectByCertificateNo(String certificateNo);
    
    /**
     * 根据志愿者ID查询证书列表
     */
    @Select("SELECT * FROM certificate_library WHERE volunteer_id = #{volunteerId} ORDER BY created_at DESC")
    List<CertificateLibrary> selectByVolunteerId(Long volunteerId);
    
    /**
     * 根据钱包地址查询证书列表
     */
    @Select("SELECT * FROM certificate_library WHERE wallet_address = #{walletAddress} ORDER BY created_at DESC")
    List<CertificateLibrary> selectByWalletAddress(String walletAddress);
    
    /**
     * 根据志愿者姓名模糊查询
     */
    @Select("SELECT * FROM certificate_library WHERE volunteer_name LIKE CONCAT('%', #{name}, '%') ORDER BY created_at DESC")
    List<CertificateLibrary> selectByVolunteerName(String name);
    
    /**
     * 查询所有证书（支持状态筛选）
     */
    @Select("<script>" +
            "SELECT * FROM certificate_library " +
            "<where>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status}" +
            "</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<CertificateLibrary> selectAll(@Param("status") String status);
    
    /**
     * 更新证书状态
     */
    @Update("UPDATE certificate_library SET status = #{status}, issue_date = #{issueDate}, " +
            "issuer_id = #{issuerId} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, 
                     @Param("issueDate") java.util.Date issueDate, @Param("issuerId") Long issuerId);
    
    /**
     * 更新证书图片URL
     */
    @Update("UPDATE certificate_library SET certificate_image_url = #{url} WHERE id = #{id}")
    int updateImageUrl(@Param("id") Long id, @Param("url") String url);
    
    /**
     * 批量更新状态为已发放
     */
    @Update("<script>" +
            "UPDATE certificate_library SET status = 'issued', issue_date = #{issueDate}, issuer_id = #{issuerId} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchIssue(@Param("ids") List<Long> ids, @Param("issueDate") java.util.Date issueDate, 
                   @Param("issuerId") Long issuerId);
    
    /**
     * 统计证书数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM certificate_library " +
            "<where>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status}" +
            "</if>" +
            "</where>" +
            "</script>")
    int countByStatus(@Param("status") String status);
    
    /**
     * 删除证书
     */
    @Delete("DELETE FROM certificate_library WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 统计所有证书数量
     */
    @Select("SELECT COUNT(*) FROM certificate_library")
    Integer countAll();
}
