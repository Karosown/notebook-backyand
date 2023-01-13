/**
 * Title
 *
 * @ClassName: Init
 * @Description:
 * @author: Karos
 * @date: 2022/12/17 22:25
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.common;

import com.karos.project.constant.RedisKeysConstant;
import com.karos.project.model.entity.Note;
import com.karos.project.service.NoteService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Component
public class InitRedis{
    @Resource
    RedisTemplate redisTemplate;
    @Resource
    NoteService noteService;
    public InitRedis(){
            }

           public void init(){
               HashOperations hashOperations = redisTemplate.opsForHash();
               redisTemplate.expire("checkcode_img",1800000, TimeUnit.MILLISECONDS);
                redisTemplate.expire("checkcode_sms",1800000, TimeUnit.MILLISECONDS);
               ArrayList<Note> list = (ArrayList<Note>) noteService.list();
               for (Note k:list){
                   hashOperations.put(RedisKeysConstant.ThumbsNum,k.getId(),k.getThumbNum());
               }
            }
}