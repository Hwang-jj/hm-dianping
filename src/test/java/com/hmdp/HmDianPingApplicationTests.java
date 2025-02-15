package com.hmdp;

import com.hmdp.service.impl.ShopServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


//    @Test
//    public void testSaveShop(){
//        shopService.saveShop2Redis(1L, 10L);
//    }

    @Test
    public void testHyperLogLog(){
        String[] users = new String[1000];
        int index = 0;
        for(int i = 1; i <= 1000000; i++){
            users[index++] = "user_" + i;
            if(i % 1000 == 0){
                index = 0;
                stringRedisTemplate.opsForHyperLogLog().add("hll1", users);
            }
        }
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hll1");
        System.out.println("size = " + size);
    }

}
