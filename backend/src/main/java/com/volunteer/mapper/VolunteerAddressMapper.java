package com.volunteer.mapper;

import com.volunteer.entity.VolunteerAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VolunteerAddressMapper {
    List<VolunteerAddress> selectByVolunteerId(@Param("volunteerId") Long volunteerId);

    VolunteerAddress selectByIdAndVolunteerId(@Param("id") Long id, @Param("volunteerId") Long volunteerId);

    int insert(VolunteerAddress address);

    int update(VolunteerAddress address);

    int deleteByIdAndVolunteerId(@Param("id") Long id, @Param("volunteerId") Long volunteerId);

    int batchDeleteByIdsAndVolunteerId(@Param("ids") List<Long> ids, @Param("volunteerId") Long volunteerId);

    int clearDefaultByVolunteerId(@Param("volunteerId") Long volunteerId);

    int setDefaultByIdAndVolunteerId(@Param("id") Long id, @Param("volunteerId") Long volunteerId);
    
    // 统计方法
    List<VolunteerAddress> selectAll();
}
