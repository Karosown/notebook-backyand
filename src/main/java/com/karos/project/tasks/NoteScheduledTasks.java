/**
 * Title
 *
 * @ClassName: NoteScheduledTasks
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/1/5 3:32
 * @Blog: https://www.wzl1.top/
 */

package com.karos.project.tasks;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.karos.project.constant.LockConstant;
import com.karos.project.constant.RedisKeysConstant;
import com.karos.project.mapper.NotethumbrecordsMapper;
import com.karos.project.model.entity.Note;
import com.karos.project.model.entity.Notethumbrecords;
import com.karos.project.service.NoteService;
import com.karos.project.service.NotethumbrecordsService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.karos.project.constant.RedisKeysConstant.ThumbsHistoryHash;
import static com.karos.project.constant.RedisKeysConstant.ThumbsUserSet;

@Component

public class NoteScheduledTasks extends ScheduledTasks{
    @Resource
    RedisTemplate redisTemplate;
    @Resource
    NotethumbrecordsService notethumbrecordsService;
    @Resource
    NoteService noteService;
    @Scheduled(cron = "0 0 0/5 * * ? ")
    public void PersistenceThumbs(){
        //加锁
        lockUtil.DistributedLock(LockConstant.ThumbsLock_Pers,10L, TimeUnit.SECONDS);
        //持久化
            //list 用于获取点赞的用户
        SetOperations setOperations = redisTemplate.opsForSet();
        //hash 用于获取用户点赞数据
        HashOperations hashOperations = redisTemplate.opsForHash();
                //从缓存中取出点赞过的用户ID
        Long usersetsize = setOperations.size(ThumbsUserSet);
        //如果没有人点赞，那就释放锁，并且退出
        if (usersetsize<=0){
            lockUtil.DistributedUnLock(LockConstant.ThumbsLock_Pers.intern());
            return;
        }
        Set members = setOperations.members(ThumbsUserSet);
        Set<String> userlist =new HashSet<>();
        for(Object it:members){
            userlist.add(it.toString());
        }
        //清楚点过赞的用户
        redisTemplate.delete(ThumbsUserSet);
        ArrayList<CompletableFuture<Void> > futrueList=new ArrayList<>();
        //获取所有用户点赞过的列表
        List< List<Notethumbrecords>> thumblist = hashOperations.multiGet(ThumbsHistoryHash, userlist);
        Map entries = hashOperations.entries(RedisKeysConstant.ThumbsNum);
        int i=0;
        int j=0;
        Set set = entries.keySet();
        Iterator iterator = set.iterator();
        int size=set.size();
        while(true){
                  if (j>=thumblist.size())break;
                  ArrayList<Notethumbrecords> historyList=new ArrayList<>();
                  ArrayList<Note> countList=new ArrayList<>();
                 while(j<thumblist.size()&&(j==0||j%1000!=0)) {
                     List<Notethumbrecords> e = thumblist.get(j);
                     if (e==null) break;
                     CollectionUtil.addAll(historyList,e);
                     j++;
                 }
                 while(iterator.hasNext()&&(i==0||i%1000!=0)){
                     String noteID = (String) iterator.next();
                     Long thumbNum=((Integer) entries.get(noteID)).longValue();
                     Note temp=new Note();
                     temp.setId(noteID);
                     temp.setThumbNum(thumbNum);
                     countList.add(temp);
                     i++;
                 }
                  //开启多线程
                  CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                      //将点赞数据持久化到mysql
                      notethumbrecordsService.saveOrUpdateBatch(historyList, (historyList.size()/3)+1);
                      noteService.updateBatchById(countList,(countList.size()/3)+1);
                  });
                  futrueList.add(future);
              }
              CompletableFuture.allOf(futrueList.toArray(new CompletableFuture[]{})).join();
//                notethumbrecordsService.saveOrUpdateBatch(thumblist,10000);
        //释放锁
        lockUtil.DistributedUnLock(LockConstant.ThumbsLock_Pers.intern());
    }
}