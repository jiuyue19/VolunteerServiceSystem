package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.PointsCalculationService;
import com.volunteer.entity.ExchangeOrder;
import com.volunteer.entity.Goods;
import com.volunteer.entity.Volunteer;
import com.volunteer.entity.VolunteerAddress;
import com.volunteer.mapper.ExchangeOrderMapper;
import com.volunteer.mapper.GoodsMapper;
import com.volunteer.mapper.VolunteerAddressMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class ExchangeOrderController {

    @Autowired
    private ExchangeOrderMapper orderMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private VolunteerAddressMapper addressMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PointsCalculationService pointsCalculationService;

    private Long getCurrentVolunteerId(String token) {
        String actualToken = token.replace("Bearer ", "");
        Claims claims = jwtUtil.parseToken(actualToken);
        return claims.get("userId", Long.class);
    }

    private String generateOrderNumber(Long volunteerId) {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "EX" + timePart + String.format("%04d", volunteerId % 10000);
    }

    private String buildAddressString(VolunteerAddress address) {
        StringBuilder sb = new StringBuilder();
        if (address.getProvince() != null) {
            sb.append(address.getProvince());
        }
        if (address.getCity() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(address.getCity());
        }
        if (address.getDistrict() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(address.getDistrict());
        }
        if (address.getDetailAddress() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(address.getDetailAddress());
        }
        return sb.toString();
    }

    @GetMapping("/list")
    public Result<List<ExchangeOrder>> getList() {
        try {
            return Result.success(orderMapper.selectAll());
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody ExchangeOrder order) {
        try {
            orderMapper.insert(order);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加订单失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody ExchangeOrder order) {
        try {
            orderMapper.update(order);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新订单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            orderMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除订单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                orderMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除订单失败: " + e.getMessage());
        }
    }

    @PostMapping("/exchange")
    @Transactional
    public Result<ExchangeOrder> exchange(@RequestHeader("Authorization") String token,
                                          @RequestBody ExchangeOrder request) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);

            Goods goods = goodsMapper.selectById(request.getGoodsId());
            if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
                return Result.error("商品不存在或未上架");
            }

            Integer quantity = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
            int totalPoints = goods.getPoints() * quantity;

            Volunteer volunteer = volunteerMapper.selectById(volunteerId);
            if (volunteer == null) {
                return Result.error("志愿者不存在");
            }
            Integer currentPoints = volunteer.getPoints() != null ? volunteer.getPoints() : 0;
            if (currentPoints < totalPoints) {
                return Result.error("积分不足，无法兑换");
            }

            if (request.getAddressId() == null) {
                return Result.error("请选择收货地址");
            }
            VolunteerAddress address = addressMapper.selectByIdAndVolunteerId(request.getAddressId(), volunteerId);
            if (address == null) {
                return Result.error("收货地址不存在或无权限");
            }

            int affected = goodsMapper.reduceStock(goods.getId(), quantity);
            if (affected <= 0) {
                return Result.error("库存不足，兑换失败");
            }

            ExchangeOrder order = new ExchangeOrder();
            order.setVolunteerId(volunteerId);
            order.setGoodsId(goods.getId());
            order.setQuantity(quantity);
            order.setTotalPoints(totalPoints);
            order.setAddress(buildAddressString(address));
            order.setContactName(address.getContactName());
            order.setPhone(address.getContactPhone());
            order.setOrderNumber(generateOrderNumber(volunteerId));
            order.setTrackingNumber(null);
            order.setStatus("待发货");
            order.setCreatedAt(LocalDateTime.now());

            orderMapper.insert(order);

            // 兑换成功后即时同步积分
            pointsCalculationService.syncVolunteerPoints(volunteerId);

            ExchangeOrder resultOrder = orderMapper.selectById(order.getId());
            return Result.success(resultOrder != null ? resultOrder : order);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("兑换失败: " + e.getMessage());
        }
    }

    @GetMapping("/my-exchanges")
    public Result<List<ExchangeOrder>> getMyExchanges(@RequestHeader("Authorization") String token) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            List<ExchangeOrder> list = orderMapper.selectByVolunteerId(volunteerId);
            return Result.success(list);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("获取我的兑换记录失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm")
    @Transactional
    public Result<Void> confirmReceive(@RequestHeader("Authorization") String token,
                                       @PathVariable Long id) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            ExchangeOrder order = orderMapper.selectById(id);
            if (order == null || !volunteerId.equals(order.getVolunteerId())) {
                return Result.error("订单不存在或无权限操作");
            }
            if (!"已发货".equals(order.getStatus())) {
                return Result.error("当前状态不能确认收货");
            }
            orderMapper.updateStatus(id, "已签收");
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @Transactional
    public Result<Void> cancel(@RequestHeader("Authorization") String token,
                               @PathVariable Long id) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            ExchangeOrder order = orderMapper.selectById(id);
            if (order == null || !volunteerId.equals(order.getVolunteerId())) {
                return Result.error("订单不存在或无权限操作");
            }
            if (!"待发货".equals(order.getStatus())) {
                return Result.error("当前状态不能取消订单");
            }

            goodsMapper.increaseStock(order.getGoodsId(), order.getQuantity());
            // 取消订单后即时同步积分（不再使用累加方式）
            orderMapper.updateStatus(id, "已取消");
            pointsCalculationService.syncVolunteerPoints(volunteerId);
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 志愿者申请退款（仅积分退回，不退库存）
     */
    @PostMapping("/{id}/refund/apply")
    public Result<Void> applyRefund(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id,
                                    @RequestBody ExchangeOrder request) {
        try {
            Long volunteerId = getCurrentVolunteerId(token);
            ExchangeOrder order = orderMapper.selectById(id);
            if (order == null || !volunteerId.equals(order.getVolunteerId())) {
                return Result.error("订单不存在或无权限操作");
            }
            if (!"已完成".equals(order.getStatus())) {
                return Result.error("当前状态不能申请退款");
            }

            order.setRefundStatus("待审核");
            order.setRefundReason(request.getRefundReason());
            order.setRefundEvidence(request.getRefundEvidence());
            order.setRefundApplyTime(LocalDateTime.now());
            order.setRefundAuditTime(null);
            order.setRefundAdminId(null);
            orderMapper.update(order);

            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("申请退款失败: " + e.getMessage());
        }
    }

    /**
     * 管理员审核退款：通过则退还积分并将订单状态置为已退款
     */
    @PostMapping("/{id}/refund/review")
    public Result<Void> reviewRefund(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id,
                                     @RequestBody ExchangeOrder request) {
        try {
            // 这里简单通过 token 取管理员ID，不再做角色区分
            Long adminId = getCurrentVolunteerId(token);
            ExchangeOrder order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }

            String refundStatus = request.getRefundStatus();
            if (!"已通过".equals(refundStatus) && !"已拒绝".equals(refundStatus)) {
                return Result.error("退款状态不合法");
            }

            order.setRefundStatus(refundStatus);
            order.setRefundReason(request.getRefundReason());
            order.setRefundEvidence(request.getRefundEvidence());
            order.setRefundAuditTime(LocalDateTime.now());
            order.setRefundAdminId(adminId);

            if ("已通过".equals(refundStatus)) {
                // 标记订单为已退款（积分通过即时同步计算，不再手动累加）
                order.setStatus("已退款");
            }

            orderMapper.update(order);
            
            // 退款审核后即时同步积分
            if ("已通过".equals(refundStatus) && order.getVolunteerId() != null) {
                pointsCalculationService.syncVolunteerPoints(order.getVolunteerId());
            }

            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("审核退款失败: " + e.getMessage());
        }
    }
}