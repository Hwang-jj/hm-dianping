-- 1.参数列表：优惠券id 用户id 订单id
local voucherId = ARGV[1]
local userId = ARGV[2]
local orderId = ARGV[3]

-- 2.数据key：库存key 订单key
local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 判断库存是否充足
if(tonumber(redis.call('get', stockKey)) <= 0) then
    -- 库存不足
    return 1
end

-- 判断用户是否下单
if(redis.call('sismember', orderKey, userId) == 1) then
    -- 存在说明重复下单
    return 2
end

-- 扣库存
redis.call('incrby', stockKey, -1)
-- 下单（保存用户）
redis.call('sadd', orderKey, userId)
-- 发送消息到队列中 XADD stream.orders * k1 v1 k2 v2
redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0