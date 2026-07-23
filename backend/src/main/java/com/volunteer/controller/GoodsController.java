package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Goods;
import com.volunteer.mapper.GoodsMapper;
import com.volunteer.mapper.ExchangeOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goods")
@CrossOrigin
public class GoodsController {

    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private ExchangeOrderMapper exchangeOrderMapper;

    @GetMapping("/list")
    public Result<List<Goods>> getList() {
        try {
            return Result.success(goodsMapper.selectAll());
        } catch (Exception e) {
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Goods goods) {
        try {
            goodsMapper.insert(goods);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加商品失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Goods goods) {
        try {
            goodsMapper.update(goods);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新商品失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            // 先删除相关的兑换订单
            exchangeOrderMapper.deleteByGoodsId(id);
            // 再删除商品
            goodsMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除商品失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                // 先删除相关的兑换订单
                exchangeOrderMapper.deleteByGoodsId(id);
                // 再删除商品
                goodsMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除商品失败: " + e.getMessage());
        }
    }
}