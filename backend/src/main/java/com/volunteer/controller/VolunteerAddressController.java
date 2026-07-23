package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.VolunteerAddress;
import com.volunteer.mapper.VolunteerAddressMapper;
import com.volunteer.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/address")
@CrossOrigin
public class VolunteerAddressController {

    @Autowired
    private VolunteerAddressMapper addressMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getCurrentVolunteerId(String token) {
        String actualToken = token.replace("Bearer ", "");
        Claims claims = jwtUtil.parseToken(actualToken);
        return claims.get("userId", Long.class);
    }

    @GetMapping("/list")
    public Result<List<VolunteerAddress>> getMyAddresses(@RequestHeader("Authorization") String token) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            List<VolunteerAddress> list = addressMapper.selectByVolunteerId(volunteerId);
            return Result.success(list);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("获取地址列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    @Transactional
    public Result<Void> addAddress(@RequestHeader("Authorization") String token,
                                   @RequestBody VolunteerAddress address) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            address.setVolunteerId(volunteerId);
            if (address.getIsDefault() == null) {
                address.setIsDefault(0);
            }
            if (address.getIsDefault() == 1) {
                addressMapper.clearDefaultByVolunteerId(volunteerId);
            }
            addressMapper.insert(address);
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("添加地址失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @Transactional
    public Result<Void> updateAddress(@RequestHeader("Authorization") String token,
                                      @RequestBody VolunteerAddress address) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            address.setVolunteerId(volunteerId);
            if (address.getIsDefault() == null) {
                address.setIsDefault(0);
            }
            if (address.getIsDefault() == 1) {
                addressMapper.clearDefaultByVolunteerId(volunteerId);
            }
            addressMapper.update(address);
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("更新地址失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAddress(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            int rows = addressMapper.deleteByIdAndVolunteerId(id, volunteerId);
            if (rows == 0) {
                return Result.error("地址不存在或无权限删除");
            }
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("删除地址失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestHeader("Authorization") String token,
                                    @RequestBody Map<String, List<Long>> body) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            List<Long> ids = body.get("ids");
            if (ids != null && !ids.isEmpty()) {
                addressMapper.batchDeleteByIdsAndVolunteerId(ids, volunteerId);
            }
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("批量删除地址失败: " + e.getMessage());
        }
    }

    @PostMapping("/set-default/{id}")
    @Transactional
    public Result<Void> setDefault(@RequestHeader("Authorization") String token,
                                   @PathVariable Long id) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            addressMapper.clearDefaultByVolunteerId(volunteerId);
            int rows = addressMapper.setDefaultByIdAndVolunteerId(id, volunteerId);
            if (rows == 0) {
                return Result.error("地址不存在或无权限设置默认");
            }
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("设置默认地址失败: " + e.getMessage());
        }
    }
}
