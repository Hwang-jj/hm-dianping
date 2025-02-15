package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_LIST;
import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {

        // 1.在缓存中查询
        List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range(CACHE_SHOPTYPE_LIST, 0, -1);

        // 2.判断是否存在
        if (shopTypeJsonList != null && !shopTypeJsonList.isEmpty()) {
            // 3.存在则直接返回
            List<ShopType> shopTypeList = new ArrayList<>();
            for (String shopTypeJson : shopTypeJsonList) {
                ShopType shopType = JSONUtil.toBean(shopTypeJson, ShopType.class);
                shopTypeList.add(shopType);
            }
            return Result.ok(shopTypeList);
        }

        // 4.不存在则查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();

        // 5.不存在，返回错误
        if (shopTypeList.isEmpty()) {
            return Result.fail("不存在分类！");
        }

        // 6.存在，则添加到缓存中
        for(ShopType shopType : shopTypeList) {
            stringRedisTemplate.opsForList().rightPush(CACHE_SHOPTYPE_LIST, JSONUtil.toJsonStr(shopType));
        }
        stringRedisTemplate.expire(CACHE_SHOPTYPE_LIST, CACHE_SHOPTYPE_TTL, TimeUnit.HOURS);

        // 7.返回
        return Result.ok(shopTypeList);
    }
}
