package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {

        // 解决缓存穿透
//        Shop shop = queryWithPassThrough(id);
        // 封装redis工具类解决缓存穿透
//        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁解决缓存击穿
//        Shop shop = queryWithMutex(id);
        // 封装redis工具类，互斥锁解决缓存击穿
        Shop shop = cacheClient.queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 逻辑过期解决缓存击穿
//        Shop shop = queryWithLogicalExpire(id);
        // 封装redis工具类，逻辑过期解决缓存击穿
//        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        if(shop == null){
            return Result.fail("店铺不存在！");
        }

        return Result.ok(shop);
    }

    // 缓存空对象解决缓存穿透
//    public Shop queryWithPassThrough(Long id){
//        // 1.从redis查询商铺缓存
//        String key = CACHE_SHOP_KEY + id;
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2.判断是否存在 null, "", \t\n -> false
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 3.存在直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        // 判断是否为""
//        if(shopJson != null){
//            // 返回一个错误信息
//            return null;
//        }
//
//        // 4.不存在，根据id查询数据库
//        Shop shop = getById(id);
//
//        // 5.不存在，返回错误
//        if(shop == null){
//            // 将空值写入redis
//            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
//            // 返回错误信息
//            return null;
//        }
//
//        // 6.存在，写入redis
//        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL + RandomUtil.randomLong(0, 10), TimeUnit.MINUTES);
//
//        return shop;
//    }

//     互斥锁解决缓存击穿
//    public Shop queryWithMutex(Long id){
//
//        // 1.从redis查询商铺缓存
//        String key = CACHE_SHOP_KEY + id;
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2.判断是否存在 null, "", \t\n -> false
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 3.存在直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        // 判断是否为""
//        if(shopJson != null){
//            // 返回一个错误信息
//            return null;
//        }
//
//        String lockKey = null;
//        Shop shop = null;
//        try {
//            // 4.实现缓存重建
//            // 4.1 获取互斥锁
//            lockKey = LOCK_SHOP_KEY + id;
//            boolean isLock = tryLock(lockKey);
//
//            // 4.2 判断是否获取成功
//            if(!isLock){
//                // 4.3 获取失败，则休眠并重试
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//
//            // 4.4 获取成功，根据id查询数据库
//            shop = getById(id);
//            // 模拟重建的延时
////            Thread.sleep(200);
//
//            // 5.不存在，返回错误
//            if(shop == null){
//                // 将空值写入redis
//                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
//                // 返回错误信息
//                return null;
//            }
//
//            // 6.存在，写入redis
//            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL + RandomUtil.randomLong(0, 10), TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }finally {
//            // 7.释放互斥锁
//            unlock(lockKey);
//        }
//
//        return shop;
//    }

//    // 线程池
//    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
//    // 逻辑过期解决缓存击穿
//    public Shop queryWithLogicalExpire(Long id){
//        // 1.从redis查询商铺缓存
//        String key = CACHE_SHOP_KEY + id;
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2.判断是否存在
//        if (StrUtil.isBlank(shopJson)) {
//            // 3.不存在，直接返回null
//            return null;
//        }
//
//        // 4.存在，需要先把json反序列化为对象
//        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//        JSONObject data = (JSONObject) redisData.getData();
//        Shop shop = JSONUtil.toBean(data, Shop.class);
//        LocalDateTime expireTime = redisData.getExpireTime();
//
//        // 5.判断是否过期
//        if(expireTime.isAfter(LocalDateTime.now())){
//            // 5.1 未过期，直接返回店铺信息
//            return shop;
//        }
//
//        // 5.2 已过期，需要缓存重建
//        // 6.缓存重建
//        // 6.1 获取互斥锁
//        String lockKey = LOCK_SHOP_KEY + id;
//        boolean isLock = tryLock(lockKey);
//
//        // 6.2 判断是否获取成功
//        if(isLock){
//            // 6.3 获取锁成功，开启独立线程，实现缓存重建
//            CACHE_REBUILD_EXECUTOR.submit(() -> {
//                try {
//                    // 重建缓存
//                    this.saveShop2Redis(id, 20L);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }finally {
//                    // 释放锁
//                    unlock(lockKey);
//                }
//            });
//        }
//
//        // 6.4 返回过期的商铺信息
//        return shop;
//    }

    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        if(shop.getId() == null){
            return Result.fail("店铺id不能为空！");
        }

        // 1.更新数据库
        updateById(shop);

        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }

//    private boolean tryLock(String key){
//        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
//        return BooleanUtil.isTrue(flag);
//    }
//
//    private void unlock(String key){
//        stringRedisTemplate.delete(key);
//    }

//    public void saveShop2Redis(Long id, Long expireSeconds){
//        // 1.查询店铺信息
//        Shop shop = getById(id);
//        // 模拟重建延时
////        try {
////            Thread.sleep(200);
////        } catch (InterruptedException e) {
////            throw new RuntimeException(e);
////        }
//
//        // 2.封装逻辑过期时间
//        RedisData redisData = new RedisData();
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//
//        // 3.写入redis
//        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
//    }
}
