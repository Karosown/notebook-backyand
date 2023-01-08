/**
 * Title
 *
 * @ClassName: Init
 * @Description:
 * @author: 巫宗霖
 * @date: 2022/12/17 22:25
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.common;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class InitRedis{
    @Resource
    RedisTemplate redisTemplate;
    public InitRedis(){
            }

           public void init(){
                redisTemplate.expire("checkcode_img",1800000, TimeUnit.MILLISECONDS);
                redisTemplate.expire("checkcode_sms",1800000, TimeUnit.MILLISECONDS);

            }
}